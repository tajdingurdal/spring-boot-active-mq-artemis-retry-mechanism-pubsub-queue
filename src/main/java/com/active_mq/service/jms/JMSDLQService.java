package com.active_mq.service.jms;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.jms.producer.JMSDLQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JMSDLQService {

    Logger log = LoggerFactory.getLogger(JMSDLQService.class);

    private final MessageAuditService messageAuditService;
    private final JMSProperties jmsProperties;
    private final JMSDLQProducer dlqProducer;
    private final RedeliveryCountManager redeliveryCountManager;

    public JMSDLQService(MessageAuditService messageAuditService, JMSProperties jmsProperties, JMSDLQProducer dlqProducer, RedeliveryCountManager redeliveryCountManager) {
        this.messageAuditService = messageAuditService;
        this.jmsProperties = jmsProperties;
        this.dlqProducer = dlqProducer;
        this.redeliveryCountManager = redeliveryCountManager;
    }

    public void handleDeadLetter(BaseMessage baseMessage) {
        String messageId = baseMessage.getMessageId();
        try {
            messageAuditService.updateStatusByMessageId(messageId, MessageStatus.DLQ);
            dlqProducer.convertAndSend(baseMessage, jmsProperties.getDestination().deadLetterQueue());
            log.warn("Message {} moved to DLQ", messageId);
        } catch (Exception e) {
            log.error("Failed to move message {} to DLQ", messageId, e);
        } finally {
            redeliveryCountManager.remove(messageId);
        }
    }
}
