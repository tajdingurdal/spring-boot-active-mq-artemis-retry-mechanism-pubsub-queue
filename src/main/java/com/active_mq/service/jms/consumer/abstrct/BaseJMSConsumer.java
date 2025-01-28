package com.active_mq.service.jms.consumer.abstrct;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.ConsumerType;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.base.BaseMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;

/**
 * Abstract base class for JMS message consumers.
 * Provides functionality for auditing, managing services, and processing messages.
 */
public abstract class BaseJMSConsumer {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final MessageAuditService auditService;
    protected final List<BaseMessageService> messageServices;
    protected final JMSProperties jmsProperties;
    private final JmsTemplate jmsTemplate;

    public BaseJMSConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSProperties jmsProperties, JmsTemplate jmsTemplate) {
        this.auditService = auditService;
        this.messageServices = messageServices;

        this.jmsProperties = jmsProperties;
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Updates the status of a message by its ID.
     *
     * @param messageId The unique ID of the message.
     * @param status    The new status of the message.
     */
    public void updateMessageStatusByMessageId(String messageId, MessageStatus status) {
        auditService.updateStatusByMessageId(messageId, status);
    }

    /**
     * Retrieves the appropriate service for processing a message based on the sender.
     *
     * @param sender The identifier of the sender.
     * @return The service responsible for handling the sender's messages.
     * @throws RuntimeException If no service matches the sender.
     */
    protected BaseMessageService getService(String sender) {
        return messageServices.stream()
                .filter(service -> service.getType().equals(sender))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not found for sender: " + sender));
    }

    /**
     * Processes the main message received by this consumer.
     * Must be implemented by subclasses to define custom message handling logic.
     *
     * @param baseMessage The message to process.
     */
    public abstract void processMainMessage(BaseMessage baseMessage);

    /**
     * Gets the type of consumer this class represents.
     * Must be implemented by subclasses to specify the consumer type.
     *
     * @return The consumer type.
     */
    public abstract ConsumerType consumerType();
}
