package com.active_mq.service.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JMSTopicConsumer extends BaseJMSConsumer {


    public JMSTopicConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices) {
        super(auditService, messageServices);
    }

    @JmsListener(
            destination = "${spring.activemq.topic.message.name}",
            containerFactory = "jmsTopicListenerContainerFactory",
            subscription = "exampleSubscription"
    )
    @Async
    public <T extends BaseMessage> void receiveMessage(final BaseMessage baseMessage) throws MessageProcessingException {
        log.info("Processing topic message: {}", baseMessage.getMessageId());
        try {
            getService(baseMessage.getSender()).processReceivedData(baseMessage);
            auditService.updateStatusByMessageId(baseMessage.getMessageId(), MessageStatus.DELIVERED);
        } catch (Exception e) {
            log.info("Error processing topic message: {}", baseMessage.getMessageId());
            auditService.updateStatusByMessageId(baseMessage.getMessageId(), MessageStatus.FAILED);
        }
    }
}
