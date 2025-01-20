package com.active_mq.service.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.exception.MessageProcessingException;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.model.enums.MessageType;
import com.active_mq.service.ExampleMessageService;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JMSTopicConsumer {

    Logger log = LoggerFactory.getLogger(JMSTopicConsumer.class);
    private final MessageAuditService auditService;
    private final List<BaseMessageService> messageServices;

    public JMSTopicConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices) {
        this.auditService = auditService;
        this.messageServices = messageServices;
    }

    @JmsListener(
            destination = "${spring.activemq.topic.message.name}",
            containerFactory = "jmsTopicListenerContainerFactory",
            subscription = "exampleSubscription"
    )
    @Async
    public <T extends BaseMessage> void receiveMessage(final BaseMessage baseMessage) throws MessageProcessingException {
        log.info("Received message {} from topic for processing", baseMessage.getMessageId());
        try {
            getService(baseMessage.getSender())
                    .processReceivedData(baseMessage);
            auditService.updateStatusByMessageId(baseMessage.getMessageId(), MessageStatus.DELIVERED);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            auditService.updateStatusByMessageId(baseMessage.getMessageId(), MessageStatus.FAILED);
            throw new MessageProcessingException("Failed to process message", e);
        }
    }

    private BaseMessageService getService(String sender) {
        return messageServices.stream()
                .filter(service -> service.getType().equals(sender))
                .findFirst()
                .orElseThrow();
    }
}
