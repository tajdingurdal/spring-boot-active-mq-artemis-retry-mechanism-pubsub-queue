package com.active_mq.service.jms.consumer;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
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

@Component
public class JMSDLQConsumer extends BaseJMSConsumer {

    Logger log = LoggerFactory.getLogger(JMSDLQConsumer.class);

    public JMSDLQConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JmsTemplate jmsTemplate, JMSProperties jmsProperties) {
        super(auditService, messageServices, jmsProperties, jmsTemplate);
    }

    @JmsListener(destination = "${spring.artemis.destination.dead-letter-queue}",
            containerFactory = "dlqJmsListenerContainerFactory")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void receiveDLQMsg(final BaseMessage baseMessage) throws JMSException {
        log.warn("Message {} received in DLQ", baseMessage.getMessageId());
        // notify somewhere....

        updateMessageStatusByMessageId(baseMessage.getMessageId(), MessageStatus.HANDLED_AT_DLQ_AND_FAILED);
    }

    @Override
    protected <T extends BaseMessage> void signal(String str) {

    }

    @Override
    protected void processMainMessage(BaseMessage baseMessage) {

    }
}
