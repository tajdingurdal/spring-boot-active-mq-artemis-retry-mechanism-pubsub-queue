package com.active_mq.service.jms.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.jms.JMSRetryService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import java.util.List;

@Service
public class JMSQueueConsumer extends BaseJMSConsumer {

    private final JMSRetryService jmsRetryService;
    private final RedeliveryCountManager redeliveryCountManager;

    public JMSQueueConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSRetryService jmsRetryService, RedeliveryCountManager redeliveryCountManager) {
        super(auditService, messageServices);
        this.jmsRetryService = jmsRetryService;
        this.redeliveryCountManager = redeliveryCountManager;
    }

    @JmsListener(destination = "${spring.artemis.destination.message-queue}",
            containerFactory = "containerFactory")
    @Transactional
    public <T extends BaseMessage> void receiveMessage(final BaseMessage baseMessage) throws MessageProcessingException, JMSException {
        String messageId = baseMessage.getMessageId();
        try {
            log.info("At Consumer: {}", messageId);
            auditService.updateStatusByMessageId(baseMessage.getMessageId(), MessageStatus.DELIVERED);
            getService(baseMessage.getSender()).processReceivedData(baseMessage);
            redeliveryCountManager.removeRedeliveryCountFromMap(messageId);
        } catch (Exception e) {
            log.info("At Consumer: Error processing queue message: {}", messageId);
            jmsRetryService.handleProcessingError(baseMessage);
        }
    }
}
