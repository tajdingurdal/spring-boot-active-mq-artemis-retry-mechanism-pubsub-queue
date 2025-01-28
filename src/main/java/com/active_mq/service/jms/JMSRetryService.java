package com.active_mq.service.jms;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.jms.producer.JMSQueueProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service class responsible for managing retry logic for JMS message processing errors.
 */
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

    /**
     * Handles a processing error for the given message. Determines whether to retry the message or send it to the dead-letter queue (DLQ).
     *
     * @param baseMessage The message that failed to process.
     */
    @Async
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

    /**
     * Handles the retry process for a message, including delay calculation and message re-sending.
     *
     * @param message The message to be retried.
     */
    public void handleRetry(BaseMessage message) {
        String messageId = message.getMessageId();
        try {
            int countAsInt = redeliveryCountManager.getRedeliveryCountAsInt(messageId);
            redeliveryCountManager.incrementRedeliveryCount(messageId);
            long delay = calculateRetryDelay(countAsInt);
            queueProducer.sendDelayedMessage(message, message.getDestination(), delay);
            auditService.updateStatusByMessageId(messageId, MessageStatus.RETRYING);
            log.info("Scheduled retry for message {} with delay {} ms, attempt {}", messageId, delay, countAsInt);
        } catch (Exception e) {
            log.error("Failed to handle retry for message: {}", messageId, e);
            dlqService.handleDeadLetter(message);
        }
    }

    /**
     * Calculates the delay for the next retry attempt based on the retry count.
     *
     * @param retryCount The number of previous retry attempts.
     * @return The delay in milliseconds before the next retry.
     */
    private long calculateRetryDelay(int retryCount) {
        int oneMinute = 60 * 1000;

        if (retryCount == 1) {
            return oneMinute;
        } else if (retryCount == 2) {
            return oneMinute * 2;
        } else {
            return oneMinute * 3;
        }
    }

    /**
     * Determines if the message is eligible for retry based on redelivery count and expiration.
     *
     * @param message        The message being evaluated.
     * @param redeliveryCount The number of times the message has already been retried.
     * @return True if the message can be retried, false otherwise.
     */
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
