package com.active_mq.service.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

@Service
public class JMSDLQConsumer {

    Logger log = LoggerFactory.getLogger(JMSDLQConsumer.class);
    private final MessageAuditService auditService;

    public JMSDLQConsumer(MessageAuditService auditService) {
        this.auditService = auditService;
    }

    @JmsListener(destination = "${spring.activemq.queue.dead-letter-queue}")
    public void processDLQMessage(final Message message) throws JMSException {
        BaseMessage baseMessage = (BaseMessage) ((ObjectMessage) message).getObject();
        log.warn("Message {} received in DLQ", baseMessage.getMessageId());
        // notify somewhere....

        auditService.persist(baseMessage, MessageStatus.FAILED);
    }

}
