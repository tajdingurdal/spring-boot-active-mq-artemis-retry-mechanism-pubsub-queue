package com.active_mq.service.consumer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.List;

public class BaseJMSConsumer {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final MessageAuditService auditService;
    protected final List<BaseMessageService> messageServices;

    public BaseJMSConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices) {
        this.auditService = auditService;
        this.messageServices = messageServices;
    }

    protected BaseMessage extractMessage(Message message) throws JMSException {
        return (BaseMessage) ((ObjectMessage) message).getObject();
    }

    protected BaseMessageService getService(String sender) {
        return messageServices.stream()
                .filter(service -> service.getType().equals(sender))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not found for sender: " + sender));
    }

}
