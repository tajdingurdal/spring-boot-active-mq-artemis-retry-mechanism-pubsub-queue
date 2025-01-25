package com.active_mq.model.dto;

import com.active_mq.core.model.BaseMessage;
import org.springframework.stereotype.Component;

@Component
public class DLQMessage extends BaseMessage {

    public DLQMessage() {
    }
}
