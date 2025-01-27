package com.active_mq.model.dto;

import com.active_mq.model.enums.ConsumerType;

public class SignalMessage {
    private String destination;
    private ConsumerType consumerType;

    public SignalMessage(String destination, ConsumerType consumerType) {
        this.destination = destination;
        this.consumerType = consumerType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ConsumerType getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(ConsumerType consumerType) {
        this.consumerType = consumerType;
    }
}
