package com.active_mq.model.enums;


public enum MessageStatus {

    CREATED,
    QUEUED,
    TOPIC,
    DELIVERED,
    FAILED,
    ERROR,
    REJECTED,
    DLQ,
    EXPIRED,
    RETRYING,
    HANDLED_AT_DLQ_AND_FAILED;

}
