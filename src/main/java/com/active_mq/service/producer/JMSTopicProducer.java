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


@Service
public class JMSTopicProducer {

    Logger log = LoggerFactory.getLogger(JMSTopicProducer.class);

    private final JmsTemplate jmsTemplate;
    private final MessageAuditService auditService;

    public JMSTopicProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        this.jmsTemplate = jmsTemplate;
        this.auditService = auditService;
    }

    public <T extends BaseMessage> void sendMessage(final T message, final String topic) {
        validateMessage(message);
        convertAndSend(message, topic);
    }

    /**
     * Bu metotta durable olmasını sağla. Eğer bir subscriber down olduğunda, okuyacak mesajı kaybolmaması için durable olmalı.
     * Bunu nasıl yapacağına bak. Çünkü topicden mesajlar subscriberlara gönderilir ve silinir sonra. Dolayısıyla down olan subscriber mesaj silindiği için göremez.
     * https://medium.com/@amanbhala813/durable-subscriber-in-apache-activemq-artemis-3b3bbd7f94b3
     * <p>
     * Ayrıca JMSContext default olarak AUTO_ACKNOWLEDGE olarak ayarlıdır. Durable'de buna ayarlarsan, mesaj iletim durumu daha sağlam olacak.
     * Bir consuemr çökse bile yinede durable'da ki ayar sayesinde mesaj iletilecek eğer consumer ayağa kalkarsa.
     *
     * @param message
     * @param topic
     * @param <T>
     */
    public <T extends BaseMessage> void sendMessageBySpecialPriority(final T message, final String topic) {
        validateMessage(message);
        convertAndSendBySpecialPriority(message, topic, message.getPriority().getLevel());
    }

    @Async
    public <T extends BaseMessage> void convertAndSend(final T message, final String destination) {
        try {
            jmsTemplate.convertAndSend(destination, message);
            logSuccessAndAudit(message);
        } catch (Exception e) {
            handleSendError(message, e);
        }
    }

    @Async
    public <T extends BaseMessage> void convertAndSendBySpecialPriority(final T message, final String destination, final int priority) {
        try {
            jmsTemplate.setPriority(priority);
            convertAndSend(message, destination);
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
        auditService.persist(message, MessageStatus.TOPIC);
    }

    private <T extends BaseMessage> void handleSendError(T message, Exception e) {
        log.error("Error sending message: {}", e.getMessage());
        auditService.persist(message, MessageStatus.ERROR);
        throw new MessageProcessingException("Failed to send message", e);
    }

}
