package com.active_mq.core.service;


import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.enums.ChannelType;

public interface BaseMessageService<T extends BaseMessage> {

    public T generateMessage(String destination);

    public String getType();

    public void processReceivedData(BaseMessage baseMessage);

    public void doSend(BaseMessage message, ChannelType type);
}
