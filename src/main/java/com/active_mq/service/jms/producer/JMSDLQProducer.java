package com.active_mq.service.jms.producer;

import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.jms.producer.abstrct.BaseJMSProducer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


@Service
public class JMSDLQProducer extends BaseJMSProducer {

    public JMSDLQProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        super(jmsTemplate, auditService);
    }

    @Override
    protected MessageStatus getType() {
        return MessageStatus.DLQ;
    }
}
