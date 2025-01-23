package com.active_mq.service.jms.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class JMSExpiryConsumer {

    Logger log = LoggerFactory.getLogger(JMSExpiryConsumer.class);
    private final MessageAuditService auditService;

    public JMSExpiryConsumer(MessageAuditService auditService) {
        this.auditService = auditService;
    }

    @JmsListener(destination = "${spring.activemq.destination.expiry-queue}",
            containerFactory = "expiryJmsListenerContainerFactory")
    @Async
    public <T extends BaseMessage> void receivedExpiryMessage(final BaseMessage baseMessage) throws MessageProcessingException {
        auditService.persist(baseMessage, MessageStatus.EXPIRED);
        log.info("Processed expired message: {}", baseMessage.getMessageId());
    }
}
