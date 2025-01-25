package com.active_mq.service.jms.consumer;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.jms.JMSRetryService;
import com.active_mq.service.jms.consumer.abstrct.BaseJMSConsumer;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class JMSQueueConsumer extends BaseJMSConsumer {

    private final JMSRetryService jmsRetryService;
    private final RedeliveryCountManager redeliveryCountManager;

    public JMSQueueConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSRetryService jmsRetryService, JmsTemplate jmsTemplate, RedeliveryCountManager redeliveryCountManager, JMSProperties jmsProperties) {
        super(auditService, messageServices, jmsProperties, jmsTemplate);
        this.jmsRetryService = jmsRetryService;
        this.redeliveryCountManager = redeliveryCountManager;
    }

    @JmsListener(destination = "${spring.artemis.destination.start-signal-queue}",
            containerFactory = "containerFactory")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public <T extends BaseMessage> void signal(final String str) throws MessageProcessingException {
        String messageQueue = jmsProperties.getDestination().messageQueue();
        if (!str.equals(messageQueue)) {
            return;
        }
        BaseMessage mainMessage = receiveMainMessage(messageQueue);
        assert mainMessage != null;
        processMainMessage(mainMessage);
    }

    @Override
    public void processMainMessage(BaseMessage baseMessage) {
        String messageId = baseMessage.getMessageId();
        try {
            log.info("At Queue Consumer: {}", messageId);
            getService(baseMessage.getSender()).processReceivedData(baseMessage);
            updateMessageStatusByMessageId(messageId, MessageStatus.DELIVERED);
            redeliveryCountManager.removeRedeliveryCountFromMap(messageId);
        } catch (Exception e) {
            log.info("At Queue Consumer: Error processing queue message: {}", messageId);
            //        jmsRetryService.handleProcessingError(baseMessage);
        }
    }
}
