package com.active_mq.service.jms.producer;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.service.MessageAuditService;
import com.active_mq.service.jms.producer.abstrct.BaseJMSProducer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


@Service
public class JMSTopicProducer extends BaseJMSProducer {

    public JMSTopicProducer(JmsTemplate jmsTemplate, MessageAuditService auditService) {
        super(jmsTemplate, auditService);
    }

    @Override
    public <T extends BaseMessage> void convertAndSend(T message, String destination) {
        super.convertAndSend(message, destination);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.TOPIC;
    }
}
