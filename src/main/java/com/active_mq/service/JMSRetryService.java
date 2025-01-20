package com.active_mq.service;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.producer.JMSQueueProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JMSRetryService {

    Logger log = LoggerFactory.getLogger(JMSRetryService.class);

    private final JMSProperties jmsProperties;
    private final MessageAuditService auditService;
    private final JMSQueueProducer queueProducer;

    private final Map<String, AtomicInteger> retryCountMap = new ConcurrentHashMap<>();

    public JMSRetryService(MessageAuditService messageAuditService, JMSProperties jmsProperties, JMSQueueProducer queueProducer) {
        this.auditService = messageAuditService;
        this.jmsProperties = jmsProperties;
        this.queueProducer = queueProducer;
    }

    public void handleProcessingError(final BaseMessage baseMessage) {
        String messageId = baseMessage.getMessageId();
        int redeliveryCount = getRedeliveryCountFromMap(messageId).get();
        log.error("Error processing message {} attempt: {}", messageId, redeliveryCount);
        if (shouldRetryMessage(baseMessage, redeliveryCount)) {
            handleRetry(baseMessage);
        } else {
            handleDeadLetter(baseMessage);
        }
    }

    public void handleRetry(BaseMessage message) {
        String messageId = message.getMessageId();
        AtomicInteger retryCount = getRedeliveryCountFromMap(messageId);
        try {
            long delay = calculateRetryDelay(retryCount.get());
            queueProducer.sendDelayedMessage(message, message.getDestination(), delay);
            auditService.updateStatusByMessageId(messageId, MessageStatus.RETRYING);
            retryCount.incrementAndGet();
            log.info("Scheduled retry for message {} with delay {} ms, attempt {}", messageId, delay, retryCount.get());
        } catch (Exception e) {
            log.error("Failed to handle retry for message: {}", messageId, e);
            handleDeadLetter(message);
        }
    }

    private long calculateRetryDelay(int retryCount) {

        int oneMinute = 60 * 1000;

        if (retryCount == 0) {
            return oneMinute;
        } else if (retryCount == 1) {
            return oneMinute * 2;
        } else {
            return oneMinute * 3;
        }
    }

    public void handleDeadLetter(BaseMessage baseMessage) {
        String messageId = baseMessage.getMessageId();
        try {
            auditService.updateStatusByMessageId(messageId, MessageStatus.DLQ);
            queueProducer.convertAndSend(baseMessage, jmsProperties.getDeadLetterQueue());
            log.warn("Message {} moved to DLQ", messageId);
        } catch (Exception e) {
            log.error("Failed to move message {} to DLQ", messageId, e);
        } finally {
            retryCountMap.remove(messageId);
        }
    }

    private boolean shouldRetryMessage(BaseMessage message, final int redeliveryCount) {

        if (redeliveryCount >= jmsProperties.getRedelivery().maxAttempts()) {
            log.warn("Max retry attemps ({}) reached for message {}", redeliveryCount, message.getMessageId());
            return false;
        }

        if (message.getExpirationDate() != null && LocalDateTime.now().isAfter(message.getExpirationDate())) {
            log.warn("Message {} has expired at {}", message.getMessageId(), message.getExpirationDate());
            return false;
        }

        return true;
    }

    public Map<String, AtomicInteger> getRetryCountMap() {
        return retryCountMap;
    }

    public void removeRedeliveryCountFromMap(String messageId) {
        getRetryCountMap().remove(messageId);
    }

    public AtomicInteger getRedeliveryCountFromMap(String messageId) {
        return getRetryCountMap().computeIfAbsent(messageId, k -> new AtomicInteger(0));
    }
}
