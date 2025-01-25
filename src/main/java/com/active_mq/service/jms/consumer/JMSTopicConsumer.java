package com.active_mq.service.jms.consumer;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.jms.consumer.abstrct.BaseJMSConsumer;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class JMSTopicConsumer extends BaseJMSConsumer {

    public JMSTopicConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSProperties jmsProperties, JmsTemplate jmsTemplate) {
        super(auditService, messageServices, jmsProperties, jmsTemplate);
    }

    @JmsListener(
            destination = "${spring.artemis.destination.message-topic}",
            containerFactory = "jmsSubscriberListenerContainerFactory",
            subscription = "default-topic-subscription"
    )
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void processMainMessage(BaseMessage baseMessage) {
        log.info("Processing topic message: {}", baseMessage.getMessageId());
        try {
            getService(baseMessage.getSender()).processReceivedData(baseMessage);
            updateMessageStatusByMessageId(baseMessage.getMessageId(), MessageStatus.DELIVERED);
        } catch (Exception e) {
            log.info("Error processing topic message: {}", baseMessage.getMessageId());
            updateMessageStatusByMessageId(baseMessage.getMessageId(), MessageStatus.FAILED);
        }
    }

    @Override
    protected <T extends BaseMessage> void signal(String str) {
        return;
    }
}