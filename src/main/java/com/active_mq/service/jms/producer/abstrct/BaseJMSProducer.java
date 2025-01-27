package com.active_mq.service.jms.producer.abstrct;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Message;

import static com.active_mq.model.enums.ChannelType.TOPIC;

/**
 * Abstract base class for JMS message producers
 */
public abstract class BaseJMSProducer {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final JmsTemplate jmsTemplate;
    protected final MessageAuditService auditService;
    @Autowired
    private JMSProperties jmsProperties;

    protected BaseJMSProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        this.jmsTemplate = jmsTemplate;
        this.auditService = auditService;
    }

    public abstract ChannelType getChannelType();

    public <T extends BaseMessage> void sendMessage(final T message) {
        validateMessage(message);
        if (message.getChannelType().equals(TOPIC)) {
            convertAndSendToTopic(message, message.getDestination());
        } else {
            convertAndSend(message, message.getDestination());
        }
    }

    public <T extends BaseMessage> void convertAndSend(final T message, final String destination) {
        try {
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
            if (!destination.equals(jmsProperties.getDestination().deadLetterQueue()))
                sendStartSignal(destination);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    public <T extends BaseMessage> void convertAndSendToTopic(final T message, final String destination) {
        try {
            jmsTemplate.setPubSubDomain(true);
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
            jmsTemplate.setPubSubDomain(false);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    public <T extends BaseMessage> void sendMessageWithPriority(final T message) {
        validateMessage(message);
        convertAndSendWithPriority(message, message.getDestination(), message.getPriority().getLevel());
    }

    public <T extends BaseMessage> void convertAndSendWithPriority(final T message, final String destination, final int priority) {
        try {
            jmsTemplate.setPriority(priority);
            convertAndSend(message, destination);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        } finally {
            jmsTemplate.setPriority(Message.DEFAULT_PRIORITY);
        }
    }

    public <T extends BaseMessage> void sendDelayedMessage(final T message, final String destination, final long deliveryDelay) {
        try {
            jmsTemplate.setDeliveryDelay(deliveryDelay);
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        } finally {
            jmsTemplate.setDeliveryDelay(0);
        }
    }

    private void sendStartSignal(String specific) {
        jmsTemplate.convertAndSend(jmsProperties.getDestination().startSignalQueue(), specific);
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
        log.info("At Producer: {} destination: {}", message.getMessageId(), message.getDestination());
        auditService.updateStatusByMessageId(message.getMessageId(), message.getStatus());
    }

    protected <T extends BaseMessage> void handleSendError(T message, Exception e) {
        log.error("Failed to send message with ID: {} to destination: {}. Error: {}", message.getMessageId(), message.getDestination(), e.getMessage(), e);
        auditService.updateStatusByMessageId(message.getMessageId(), MessageStatus.ERROR);
        throw new MessageProcessingException("Failed to send message: " + e.getMessage(), e);
    }

}
