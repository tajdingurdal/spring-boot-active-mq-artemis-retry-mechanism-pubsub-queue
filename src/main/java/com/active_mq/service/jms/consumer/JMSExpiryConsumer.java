package com.active_mq.service.jms.consumer;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.service.base.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.jms.consumer.abstrct.BaseJMSConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.jms.JMSException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JMSExpiryConsumer extends BaseJMSConsumer {

    Logger log = LoggerFactory.getLogger(JMSExpiryConsumer.class);
    Set<String> latest10Message = ConcurrentHashMap.newKeySet(10);

    public JMSExpiryConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JmsTemplate jmsTemplate, JMSProperties jmsProperties) {
        super(auditService, messageServices, jmsProperties, jmsTemplate);
    }

    @JmsListener(destination = "${spring.artemis.destination.expiry-queue}",
            containerFactory = "expiryJmsListenerContainerFactory")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public <T extends BaseMessage> void receivedExpiryMessage(final BaseMessage baseMessage) throws MessageProcessingException, JMSException {
        String messageId = baseMessage.getMessageId();
        if (latest10Message.contains(messageId)) {
            return;
        }

        log.info("At expired message consumer: {}", messageId);
        updateMessageStatusByMessageId(messageId, MessageStatus.EXPIRED);
        latest10Message.add(messageId);
    }

    @Override
    protected <T extends BaseMessage> void signal(String str) {

    }

    @Override
    protected void processMainMessage(BaseMessage baseMessage) {

    }
}
