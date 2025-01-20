package com.active_mq.service.producer.abstrct;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;

/**
 * Abstract base class for JMS message producers
 */
public abstract class AbstractJMSProducer {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final JmsTemplate jmsTemplate;
    protected final MessageAuditService auditService;

    protected AbstractJMSProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        this.jmsTemplate = jmsTemplate;
        this.auditService = auditService;
    }

    protected abstract MessageStatus getSuccessStatus();

    protected abstract <T extends BaseMessage> void doSend(T message, String destination);

    @Async
    public <T extends BaseMessage> void sendMessage(final T message) {
        validateMessage(message);
        convertAndSend(message, message.getDestination());
    }

    @Async
    public <T extends BaseMessage> void sendMessageWithPriority(final T message) {
        validateMessage(message);
        convertAndSendWithPriority(message, message.getDestination(), message.getPriority().getLevel());
    }

    @Async
    public <T extends BaseMessage> void convertAndSend(final T message, final String destination) {
        try {
            doSend(message, destination);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Async
    public <T extends BaseMessage> void convertAndSendWithPriority(final T message, final String destination, final int priority) {
        try {
            jmsTemplate.setPriority(priority);
            doSend(message, destination);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        } finally {
            jmsTemplate.setPriority(-1);
        }
    }

    @Async
    public <T extends BaseMessage> void sendDelayedMessage(final T message, final String destination, final long deliveryDelay) {
        try {
            jmsTemplate.setDeliveryDelay(deliveryDelay);
            doSend(message, destination);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        } finally {
            jmsTemplate.setDeliveryDelay(-1);
        }
    }

    protected static <T extends BaseMessage> void validateMessage(T message) {
        if (message == null) {
            throw new MessageProcessingException("Message cannot be null");
        }
        if (message.getDestination() == null || message.getDestination().trim().isEmpty()) {
            throw new MessageProcessingException("Message destination cannot be null or empty");
        }
    }

    protected <T extends BaseMessage> void logSuccessAndAudit(T message) {
        log.info("Message sent successfully with ID: {} to destination: {}", message.getMessageId(), message.getDestination());
        auditService.persist(message, getSuccessStatus());
    }

    protected <T extends BaseMessage> void handleSendError(T message, Exception e) {
        log.error("Failed to send message with ID: {} to destination: {}. Error: {}", message.getMessageId(), message.getDestination(), e.getMessage(), e);
        auditService.persist(message, MessageStatus.ERROR);
        throw new MessageProcessingException("Failed to send message: " + e.getMessage(), e);
    }

}
