package com.active_mq.service.jms.consumer.abstrct;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;

public abstract class BaseJMSConsumer {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final MessageAuditService auditService;
    protected final List<BaseMessageService> messageServices;
    protected final JMSProperties jmsProperties;
    private final JmsTemplate jmsTemplate;

    public BaseJMSConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices, JMSProperties jmsProperties, JmsTemplate jmsTemplate) {
        this.auditService = auditService;
        this.messageServices = messageServices;
        this.jmsProperties = jmsProperties;
        this.jmsTemplate = jmsTemplate;
    }


    protected abstract <T extends BaseMessage> void signal(final String str);

    protected abstract void processMainMessage(BaseMessage baseMessage);

    public void updateMessageStatusByMessageId(String messageId, MessageStatus status) {
        auditService.updateStatusByMessageId(messageId, status);
    }

    protected BaseMessage receiveMainMessage(String destination) {
        BaseMessage baseMessage = (BaseMessage) jmsTemplate.receiveAndConvert(destination);

        log.info("Receiving the main message.... {}", baseMessage);
        return baseMessage;
    }

    protected BaseMessageService getService(String sender) {
        return messageServices.stream()
                .filter(service -> service.getType().equals(sender))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not found for sender: " + sender));
    }


}
