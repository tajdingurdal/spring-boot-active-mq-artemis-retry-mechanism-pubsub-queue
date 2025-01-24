package com.active_mq.service.jms.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;

@Service
public class JMSDLQConsumer {

    Logger log = LoggerFactory.getLogger(JMSDLQConsumer.class);
    private final MessageAuditService auditService;

    public JMSDLQConsumer(MessageAuditService auditService) {
        this.auditService = auditService;
    }

    @JmsListener(destination = "${spring.artemis.destination.dead-letter-queue}",
            containerFactory = "dlqJmsListenerContainerFactory")
    public void receiveDLQMsg(final BaseMessage baseMessage) throws JMSException {
        log.warn("Message {} received in DLQ", baseMessage.getMessageId());
        // notify somewhere....

        auditService.persist(baseMessage, MessageStatus.HANDLED_AT_DLQ_AND_FAILED);
    }

}
