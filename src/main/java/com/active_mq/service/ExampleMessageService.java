package com.active_mq.service;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.model.dto.ExampleMessage;
import com.active_mq.model.enums.MessagePriority;
import com.active_mq.model.enums.MessageType;
import com.active_mq.service.jms.producer.JMSQueueProducer;
import com.active_mq.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ExampleMessageService implements BaseMessageService<ExampleMessage> {

    Logger log = LoggerFactory.getLogger(ExampleMessageService.class);

    private final JMSProperties jmsProperties;

    private final JMSQueueProducer jmsQueueProducer;

    public ExampleMessageService(JMSProperties jmsProperties, JMSQueueProducer jmsQueueProducer) {
        this.jmsProperties = jmsProperties;
        this.jmsQueueProducer = jmsQueueProducer;
    }

    @Transactional
    public void queuePublishMessage(String msg) {
        ExampleMessage exampleMessage = generateMessage();
        exampleMessage.setContent(msg);
        jmsQueueProducer.sendMessage(exampleMessage);
    }

    @Transactional
    public void topicPublishMessage(String msg) {
        ExampleMessage exampleMessage = generateMessage();
        exampleMessage.setContent(msg);
        exampleMessage.setDestination(jmsProperties.getDestination().messageTopic());
        jmsQueueProducer.sendMessage(exampleMessage);
    }

    @Override
    public void processReceivedData(BaseMessage baseMessage) {
        log.info("Received example message from {} and message is: {}", baseMessage.getSender(), baseMessage.getContent());
    }

    @Override
    public ExampleMessage generateMessage() {
        ExampleMessage message = new ExampleMessage();
        message.setMessageId(MessageUtils.createMessageId());
        message.setSender(ExampleMessageService.class.getSimpleName());
        message.setRecipient(getType());
        message.setDestination(jmsProperties.getDestination().messageQueue());
        message.setPriority(MessagePriority.DEFAULT);
        message.setMessageType(MessageType.SYSTEM);
        message.setExpirationDate(MessageUtils.defaultExpirationDate());
        return message;
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }


}
