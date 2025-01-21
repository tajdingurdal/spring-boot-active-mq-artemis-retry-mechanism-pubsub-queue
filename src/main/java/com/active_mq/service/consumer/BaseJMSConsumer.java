package com.active_mq.service.consumer;

import com.active_mq.core.service.BaseMessageService;
import com.active_mq.service.MessageAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BaseJMSConsumer {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final MessageAuditService auditService;
    protected final List<BaseMessageService> messageServices;

    public BaseJMSConsumer(MessageAuditService auditService, List<BaseMessageService> messageServices) {
        this.auditService = auditService;
        this.messageServices = messageServices;
    }

    protected BaseMessageService getService(String sender) {
        return messageServices.stream()
                .filter(service -> service.getType().equals(sender))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not found for sender: " + sender));
    }

}
