package com.active_mq.service;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.model.dto.ExampleMessage;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.MessagePriority;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.model.enums.MessageType;
import com.active_mq.service.jms.producer.abstrct.BaseJMSProducer;
import com.active_mq.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExampleMessageService implements BaseMessageService<ExampleMessage> {

    private final MessageAuditService messageAuditService;
    Logger log = LoggerFactory.getLogger(ExampleMessageService.class);

    private final JMSProperties jmsProperties;

    private final List<BaseJMSProducer> producers;

    public ExampleMessageService(JMSProperties jmsProperties, List<BaseJMSProducer> producers, MessageAuditService messageAuditService) {
        this.jmsProperties = jmsProperties;
        this.producers = producers;
        this.messageAuditService = messageAuditService;
    }

    public void queuePublishMessage(String msg) {
        ChannelType channelType = ChannelType.QUEUE;
        ExampleMessage message = generateMessage(jmsProperties.getDestination().messageQueue());
        message.setContent(msg);
        message.setChannelType(channelType);
        message.setStatus(MessageStatus.CREATED);
        messageAuditService.persist(message, channelType);
        doSend(message, channelType);
    }


    public void topicPublishMessage(String msg) {
        ChannelType channelType = ChannelType.TOPIC;
        ExampleMessage message = generateMessage(jmsProperties.getDestination().messageTopic());
        message.setContent(msg);
        message.setChannelType(channelType);
        message.setStatus(MessageStatus.CREATED);
        messageAuditService.persist(message, channelType);
        doSend(message, channelType);
    }

    @Override
    public void doSend(BaseMessage message, ChannelType type) {
        producers.stream()
                .filter(service -> service.getChannelType().equals(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producer not found: " + type))
                .sendMessage(message);
    }

    @Override
    public void processReceivedData(BaseMessage baseMessage) {
        log.info("Received example message from {} and message is: {}", baseMessage.getSender(), baseMessage.getContent());
    }

    @Override
    public ExampleMessage generateMessage(String destination) {
        ExampleMessage message = new ExampleMessage();
        message.setMessageId(MessageUtils.createUniqueMessageId());
        message.setSender(getType());
        message.setRecipient(getType());
        message.setDestination(destination);
        message.setPriority(MessagePriority.DEFAULT);
        message.setMessageType(MessageType.SYSTEM);
        message.setStatus(MessageStatus.CREATED);
        message.setExpirationDate(MessageUtils.defaultExpirationDate(jmsProperties.getMessageExpireTime()));
        return message;
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }


}
