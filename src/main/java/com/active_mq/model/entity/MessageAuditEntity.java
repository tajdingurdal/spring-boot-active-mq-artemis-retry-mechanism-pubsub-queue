package com.active_mq.model.entity;

import com.active_mq.core.converter.MapConverter;
import com.active_mq.core.model.BaseEntity;
import com.active_mq.model.enums.MessagePriority;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.model.enums.MessageType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "message_audit")
public class MessageAuditEntity extends BaseEntity implements Serializable {

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Enumerated(EnumType.STRING)
    private MessagePriority priority;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "meta_data", columnDefinition = "json")
    @Convert(converter = MapConverter.class)
    @Type(JsonType.class)
    private Map<String, String> metadata;

    public MessageAuditEntity() {
    }

    public MessageAuditEntity(String messageId, MessageStatus status, MessageType messageType, String sender, String recipient, String messageContent, MessagePriority priority, String errorMessage, Map<String, String> metadata) {
        this.messageId = messageId;
        this.status = status;
        this.messageType = messageType;
        this.sender = sender;
        this.recipient = recipient;
        this.messageContent = messageContent;
        this.priority = priority;
        this.errorMessage = errorMessage;
        this.metadata = metadata;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
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

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}