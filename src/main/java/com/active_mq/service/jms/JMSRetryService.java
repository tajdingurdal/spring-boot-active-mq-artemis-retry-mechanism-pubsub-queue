package com.active_mq.service.jms;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.jms.producer.JMSQueueProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JMSRetryService {

    Logger log = LoggerFactory.getLogger(JMSRetryService.class);

    private final JMSProperties jmsProperties;
    private final MessageAuditService auditService;
    private final JMSQueueProducer queueProducer;
    private final JMSDLQService dlqService;
    private final RedeliveryCountManager redeliveryCountManager;

    public JMSRetryService(MessageAuditService messageAuditService, JMSProperties jmsProperties, JMSQueueProducer queueProducer, JMSDLQService dlqService, RedeliveryCountManager redeliveryCountManager) {
        this.auditService = messageAuditService;
        this.jmsProperties = jmsProperties;
        this.queueProducer = queueProducer;
        this.dlqService = dlqService;
        this.redeliveryCountManager = redeliveryCountManager;
    }

    public void handleProcessingError(final BaseMessage baseMessage) {
        String messageId = baseMessage.getMessageId();
        int redeliveryCount = redeliveryCountManager.getRedeliveryCountFromMap(messageId).get();
        log.error("\nError processing message {} attempt: {}", messageId, redeliveryCount);
        if (shouldRetryMessage(baseMessage, redeliveryCount)) {
            handleRetry(baseMessage);
        } else {
            dlqService.handleDeadLetter(baseMessage);
        }
    }

    public void handleRetry(BaseMessage message) {
        String messageId = message.getMessageId();
        try {
            int countAsInt = redeliveryCountManager.getRedeliveryCountAsInt(messageId);
            long delay = calculateRetryDelay(countAsInt);
            queueProducer.sendDelayedMessage(message, message.getDestination(), delay);
            auditService.updateStatusByMessageId(messageId, MessageStatus.RETRYING);
            redeliveryCountManager.incrementRedeliveryCount(messageId);
            log.info("Scheduled retry for message {} with delay {} ms, attempt {}", messageId, delay, countAsInt);
        } catch (Exception e) {
            log.error("Failed to handle retry for message: {}", messageId, e);
            dlqService.handleDeadLetter(message);
        }
    }

    private long calculateRetryDelay(int retryCount) {
/*
        int oneMinute = 60 * 1000;

        if (retryCount == 0) {
            return oneMinute;
        } else if (retryCount == 1) {
            return oneMinute * 2;
        } else {
            return oneMinute * 3;
        }

 */
        return 500;
    }

    private boolean shouldRetryMessage(BaseMessage message, final int redeliveryCount) {

        if (redeliveryCount >= jmsProperties.getRedelivery().maxAttempts()) {
            log.warn("Max retry attemps ({}) reached for message {}", redeliveryCount, message.getMessageId());
            return false;
        }

        if (message.getExpirationDate() != null && Instant.now().isAfter(message.getExpirationDate())) {
            log.warn("Message {} has expired at {}", message.getMessageId(), message.getExpirationDate());
            return false;
        }

        return true;
    }
}
