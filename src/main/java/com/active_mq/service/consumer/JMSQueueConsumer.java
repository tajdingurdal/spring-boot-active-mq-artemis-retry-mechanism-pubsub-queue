package com.active_mq.service.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.JMSRetryService;
import com.active_mq.service.MessageAuditService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;

@Service
public class JMSQueueConsumer extends BaseJMSConsumer {

    private final JMSRetryService jmsRetryService;

    public JMSQueueConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSRetryService jmsRetryService) {
        super(auditService, messageServices);
        this.jmsRetryService = jmsRetryService;
    }

    @JmsListener(destination = "${spring.activemq.queue.message.name}")
    @Async
    public <T extends BaseMessage> void receiveMessage(final Message message) throws MessageProcessingException, JMSException {
        BaseMessage baseMessage = extractMessage(message);
        String messageId = baseMessage.getMessageId();
        log.info("Processing queue message: {}", messageId);

        try {
            getService(baseMessage.getSender()).processReceivedData(baseMessage);
            auditService.updateStatusByMessageId(messageId, MessageStatus.DELIVERED);
            jmsRetryService.removeRedeliveryCountFromMap(messageId);
        } catch (Exception e) {
            log.info("Error processing queue message: {}", messageId);
            jmsRetryService.handleProcessingError(baseMessage);
        }
    }

    @JmsListener(destination = "${spring.activemq.queue.expiry-queue}")
    @Async
    public <T extends BaseMessage> void receivedExpiryMessage(final BaseMessage baseMessage) throws MessageProcessingException {
        auditService.persist(baseMessage, MessageStatus.EXPIRED);
        log.info("Processed expired message: {}", baseMessage.getMessageId());
    }
}
