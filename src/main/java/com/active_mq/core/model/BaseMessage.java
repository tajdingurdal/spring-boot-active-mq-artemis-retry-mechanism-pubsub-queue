package com.active_mq.core.model;

import com.active_mq.model.enums.MessagePriority;
import com.active_mq.model.enums.MessageType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;

@Component
public class BaseMessage implements IMessage, Serializable {
    private String messageId;
    private String sender;
    private String recipient;
    private String destination;
    private String content;
    private MessagePriority priority;
    private MessageType messageType;
    private Instant expirationDate;

    public BaseMessage() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "messageId='" + messageId + '\'' +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", content='" + content + '\'' +
                ", priority=" + priority +
                ", messageType=" + messageType +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
