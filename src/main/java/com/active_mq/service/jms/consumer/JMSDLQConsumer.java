package com.active_mq.service.jms.consumer;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.ConsumerType;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.base.BaseMessageService;
import com.active_mq.service.jms.consumer.abstrct.BaseJMSConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JMSDLQConsumer extends BaseJMSConsumer {

    Logger log = LoggerFactory.getLogger(JMSDLQConsumer.class);
    private final RedeliveryCountManager redeliveryCountManager;

    public JMSDLQConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JmsTemplate jmsTemplate, JMSProperties jmsProperties, RedeliveryCountManager redeliveryCountManager) {
        super(auditService, messageServices, jmsProperties, jmsTemplate);
        this.redeliveryCountManager = redeliveryCountManager;
    }

//    @JmsListener(destination = "${spring.artemis.destination.dead-letter-queue}",
//            containerFactory = "dlqJmsListenerContainerFactory")
//    @Async
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void receiveDLQMsg(final BaseMessage baseMessage) throws JMSException {
//        log.warn("Message {} received in DLQ", baseMessage.getMessageId());
//        // notify somewhere....
//
//        updateMessageStatusByMessageId(baseMessage.getMessageId(), MessageStatus.HANDLED_AT_DLQ_AND_FAILED);
//    }

    @Override
    public void processMainMessage(BaseMessage baseMessage) {
        String messageId = baseMessage.getMessageId();
        log.warn("Message {} received in DLQ", messageId);
        updateMessageStatusByMessageId(messageId, MessageStatus.HANDLED_AT_DLQ_AND_FAILED);
        redeliveryCountManager.remove(messageId);
    }

    @Override
    public ConsumerType consumerType() {
        return ConsumerType.DLQ;
    }
}
