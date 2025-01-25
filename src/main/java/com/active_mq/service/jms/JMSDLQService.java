package com.active_mq.service.jms;

import com.active_mq.config.JMSProperties;
import com.active_mq.core.model.BaseMessage;
import com.active_mq.core.service.BaseMessageService;
import com.active_mq.model.dto.DLQMessage;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.MessagePriority;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.model.enums.MessageType;
import com.active_mq.service.RedeliveryCountManager;
import com.active_mq.service.jms.producer.abstrct.BaseJMSProducer;
import com.active_mq.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JMSDLQService implements BaseMessageService<DLQMessage> {

    Logger log = LoggerFactory.getLogger(JMSDLQService.class);

    private final JMSProperties jmsProperties;
    private final RedeliveryCountManager redeliveryCountManager;
    private final List<BaseJMSProducer> producers;

    public JMSDLQService(JMSProperties jmsProperties, RedeliveryCountManager redeliveryCountManager, List<BaseJMSProducer> producers) {
        this.jmsProperties = jmsProperties;
        this.redeliveryCountManager = redeliveryCountManager;
        this.producers = producers;
    }

    public void handleDeadLetter(BaseMessage message) {
        ChannelType channelType = ChannelType.DLQ;
        message.setChannelType(channelType);
        message.setStatus(MessageStatus.DLQ);
        message.setDestination(jmsProperties.getDestination().deadLetterQueue());
        doSend(message, channelType);
        redeliveryCountManager.remove(message.getMessageId());
    }

    @Override
    public DLQMessage generateMessage(String destination) {
        DLQMessage message = new DLQMessage();
        message.setMessageId(MessageUtils.createUniqueMessageId());
        message.setSender(getType());
        message.setRecipient(getType());
        message.setDestination(destination);
        message.setPriority(MessagePriority.DEFAULT);
        message.setMessageType(MessageType.SYSTEM);
        message.setStatus(MessageStatus.DLQ);
        message.setExpirationDate(MessageUtils.defaultExpirationDate(jmsProperties.getMessageExpireTime()));
        return message;
    }

    @Override
    public void processReceivedData(BaseMessage baseMessage) {

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
    public String getType() {
        return this.getClass().getSimpleName();
    }
}
