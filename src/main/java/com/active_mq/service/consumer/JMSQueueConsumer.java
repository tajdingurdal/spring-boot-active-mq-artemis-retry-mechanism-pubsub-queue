package com.active_mq.service.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.JMSRetryService;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.List;

@Service
public class JMSQueueConsumer {

    Logger log = LoggerFactory.getLogger(JMSQueueConsumer.class);

    private final MessageAuditService auditService;
    private final List<BaseMessageService> messageServices;
    private final JMSRetryService jmsRetryService;

    public JMSQueueConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSRetryService jmsRetryService) {
        this.auditService = auditService;
        this.messageServices = messageServices;
        this.jmsRetryService = jmsRetryService;
    }

    @JmsListener(destination = "${spring.activemq.queue.expiry-queue}")
    @Async
    public <T extends BaseMessage> void receivedExpiryMessage(final BaseMessage baseMessage) throws MessageProcessingException {
        auditService.persist(baseMessage, MessageStatus.EXPIRED);
    }

    @JmsListener(destination = "${spring.activemq.queue.message.name}")
    @Async
    public <T extends BaseMessage> void receiveMessage(final Message message) throws MessageProcessingException, JMSException {
        BaseMessage baseMessage = (BaseMessage) ((ObjectMessage) message).getObject();
        String messageId = baseMessage.getMessageId();

        log.info("Received message {} from queue for processing", messageId);
        try {
            getService(baseMessage.getSender()).processReceivedData(baseMessage);
            auditService.updateStatusByMessageId(messageId, MessageStatus.DELIVERED);
            jmsRetryService.removeRedeliveryCountFromMap(messageId);
        } catch (Exception e) {
            jmsRetryService.handleProcessingError(baseMessage);
        }
    }

    private BaseMessageService getService(String sender) {
        return messageServices.stream()
                .filter(service -> service.getType().equals(sender))
                .findFirst()
                .orElseThrow();
    }
}
