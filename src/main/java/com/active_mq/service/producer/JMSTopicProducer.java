package com.active_mq.service.producer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.producer.abstrct.AbstractJMSProducer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


@Service
public class JMSTopicProducer extends AbstractJMSProducer {

    public JMSTopicProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        super(jmsTemplate, auditService);
    }

    @Override
    protected MessageStatus getSuccessStatus() {
        return MessageStatus.TOPIC;
    }

    @Override
    protected <T extends BaseMessage> void doSend(T message, String destination) {
        jmsTemplate.convertAndSend(destination, message);
    }

}
