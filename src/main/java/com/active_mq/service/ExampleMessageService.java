package com.active_mq.service;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.model.dto.ExampleMessage;
import com.active_mq.model.enums.MessagePriority;
import com.active_mq.model.enums.MessageType;
import com.active_mq.service.producer.JMSQueueProducer;
import com.active_mq.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ExampleMessageService implements BaseMessageService<ExampleMessage> {

    Logger log = LoggerFactory.getLogger(ExampleMessageService.class);

    private final JMSProperties jmsProperties;

    private final JMSQueueProducer JMSQueueProducer;

    public ExampleMessageService(JMSProperties jmsProperties, JMSQueueProducer JMSQueueProducer) {
        this.jmsProperties = jmsProperties;
        this.JMSQueueProducer = JMSQueueProducer;
    }

    public void publishMessage(String msg) {
        ExampleMessage exampleMessage = generateMessage();
        exampleMessage.setContent(msg);
        JMSQueueProducer.sendMessage(exampleMessage);
    }

    @Override
    public void processReceivedData(BaseMessage baseMessage) {
        log.info("Received example message from {}", baseMessage.getSender());
    }

    @Override
    public ExampleMessage generateMessage() {
        ExampleMessage message = new ExampleMessage();
        message.setMessageId(MessageUtils.createMessageId(new Date()));
        message.setSender(ExampleMessageService.class.getSimpleName());
        message.setRecipient(getType());
        message.setDestination(jmsProperties.getMessageQueue());
        message.setPriority(MessagePriority.DEFAULT);
        message.setMessageType(MessageType.SYSTEM);
        message.setExpirationDate(MessageUtils.defaultExpirationDate());
        return message;
    }

    @Override
    public String getType() {
        return ExampleMessageService.class.getSimpleName();
    }


}
