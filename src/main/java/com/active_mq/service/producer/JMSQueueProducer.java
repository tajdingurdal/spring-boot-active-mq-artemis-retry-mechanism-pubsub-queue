package com.active_mq.service.producer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.List;


@Service
public class JMSQueueProducer {

    Logger log = LoggerFactory.getLogger(JMSQueueProducer.class);

    private final JmsTemplate jmsTemplate;
    private final MessageAuditService auditService;

    public JMSQueueProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        this.jmsTemplate = jmsTemplate;
        this.auditService = auditService;
    }

    public <T extends BaseMessage> void sendMessage(final T message) {
        validateMessage(message);
        convertAndSend(message, message.getDestination());
    }

    public <T extends BaseMessage> void sendMessageBySpecialPriority(final T message, final String queue) {
        validateMessage(message);
        convertAndSendBySpecialPriority(message, queue, message.getPriority().getLevel());
    }

    @Async
    public <T extends BaseMessage> void convertAndSend(final T message, final String destination) {
        try {
            jmsTemplate.send(destination, session -> {
                ObjectMessage objectMessage = (ObjectMessage) session.createObjectMessage(message);
                return (jakarta.jms.Message) objectMessage;
            });
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Async
    public <T extends BaseMessage> void convertAndSendBySpecialPriority(final T message, final String destination, final int priority) {
        try {
            jmsTemplate.setPriority(priority);
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Async
    public <T extends BaseMessage> void convertAndSendDelayMessage(final T message, final String destination, final long deliveryDelay) {
        try {
            jmsTemplate.setDeliveryDelay(deliveryDelay);
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    private static <T extends BaseMessage> void validateMessage(T message) {
        if (message == null) {
            throw new MessageProcessingException("Message is null, therefore cannot send message to the destination");
        }
    }

    private <T extends BaseMessage> void logSuccessAndAudit(T message) {
        log.info("Message sent to the destination successfully with ID: {}", message.getMessageId());
        auditService.persist(message, MessageStatus.QUEUED);
    }

    private <T extends BaseMessage> void handleSendError(T message, Exception e) {
        log.error("Error sending message: {}", e.getMessage());
        auditService.persist(message, MessageStatus.ERROR);
        throw new MessageProcessingException("Failed to send message", e);
    }

}
