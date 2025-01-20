package com.active_mq.core.service;


import com.active_mq.core.model.BaseMessage;

public interface BaseMessageService<T extends BaseMessage> {

    public T generateMessage();

    public String getType();

    public void processReceivedData(BaseMessage baseMessage);
}
