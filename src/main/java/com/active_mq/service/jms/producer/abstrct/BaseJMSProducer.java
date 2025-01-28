package com.active_mq.service.jms.producer.abstrct;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.dto.SignalMessage;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.ConsumerType;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Message;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.active_mq.model.enums.ChannelType.TOPIC;

/**
 * Abstract base class for JMS message producers.
 * Provides core functionality for sending, auditing, and handling errors for messages.
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

    /**
     * Sends a message to its specified destination.
     * Handles topic-based and queue-based destinations.
     *
     * @param message The message to send.
     * @param <T>     The type of the message extending BaseMessage.
     */
    public <T extends BaseMessage> void sendMessage(final T message) {
        validateMessage(message);
        if (message.getChannelType().equals(TOPIC)) {
            convertAndSendToTopic(message, message.getDestination());
        } else {
            convertAndSend(message, message.getDestination());
        }
    }

    /**
     * Sends a message to a queue destination.
     *
     * @param message     The message to send.
     * @param destination The destination queue.
     * @param <T>         The type of the message extending BaseMessage.
     */
    public <T extends BaseMessage> void convertAndSend(final T message, final String destination) {
        try {
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
            sendStartSignal(destination, message.getConsumerType());
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    /**
     * Sends a message to a topic destination with Pub/Sub enabled.
     *
     * @param message     The message to send.
     * @param destination The destination topic.
     * @param <T>         The type of the message extending BaseMessage.
     */
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

    /**
     * Sends a message with priority settings to its destination.
     *
     * @param message The message to send.
     * @param <T>     The type of the message extending BaseMessage.
     */
    public <T extends BaseMessage> void sendMessageWithPriority(final T message) {
        validateMessage(message);
        convertAndSendWithPriority(message, message.getDestination(), message.getPriority().getLevel());
    }

    /**
     * Sends a message with a specific priority level.
     *
     * @param message     The message to send.
     * @param destination The destination queue.
     * @param priority    The priority level.
     * @param <T>         The type of the message extending BaseMessage.
     */
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

    /**
     * Sends a delayed message to its destination.
     *
     * @param message       The message to send.
     * @param destination   The destination queue.
     * @param deliveryDelay The delay in milliseconds before delivery.
     * @param <T>           The type of the message extending BaseMessage.
     */
    public <T extends BaseMessage> void sendDelayedMessage(final T message, final String destination, final long deliveryDelay) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            jmsTemplate.setDeliveryDelay(deliveryDelay);
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);

            scheduler.schedule(() -> sendStartSignal(message.getDestination(), message.getConsumerType()), deliveryDelay, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            handleSendError(message, e);
        } finally {
            scheduler.shutdown();
            jmsTemplate.setDeliveryDelay(0);
        }
    }

    /**
     * Sends a start signal message to the specified queue.
     *
     * @param specific     The specific destination for the signal.
     * @param consumerType The consumer type for the signal message.
     */
    private void sendStartSignal(String specific, ConsumerType consumerType) {
        final SignalMessage signal = new SignalMessage(specific, consumerType);
        jmsTemplate.convertAndSend(jmsProperties.getDestination().startSignalQueue(), signal);
    }

    /**
     * Validates the message for required fields.
     *
     * @param message The message to validate.
     * @param <T>     The type of the message extending BaseMessage.
     * @throws MessageProcessingException If the message is invalid.
     */
    protected static <T extends BaseMessage> void validateMessage(T message) {
        if (message == null) {
            throw new MessageProcessingException("Message cannot be null");
        }
        if (message.getDestination() == null || message.getDestination().trim().isEmpty()) {
            throw new MessageProcessingException("Message destination cannot be null or empty");
        }
    }

    /**
     * Logs a successful send and updates the message audit.
     *
     * @param message The successfully sent message.
     * @param <T>     The type of the message extending BaseMessage.
     */
    protected <T extends BaseMessage> void logSuccessAndAudit(T message) {
        log.info("At Producer: {} destination: {}", message.getMessageId(), message.getDestination());
        auditService.updateStatusByMessageId(message.getMessageId(), message.getStatus());
    }

    /**
     * Handles errors encountered while sending a message.
     *
     * @param message The message that failed to send.
     * @param e       The exception encountered.
     * @param <T>     The type of the message extending BaseMessage.
     */
    protected <T extends BaseMessage> void handleSendError(T message, Exception e) {
        log.error("Failed to send message with ID: {} to destination: {}. Error: {}", message.getMessageId(), message.getDestination(), e.getMessage(), e);
        auditService.updateStatusByMessageId(message.getMessageId(), MessageStatus.ERROR);
        throw new MessageProcessingException("Failed to send message: " + e.getMessage(), e);
    }

}
