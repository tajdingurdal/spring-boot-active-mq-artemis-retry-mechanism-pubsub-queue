package com.active_mq.mapper;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.entity.MessageAuditEntity;
import com.active_mq.model.enums.MessageStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class MessageAuditMapper {

    public MessageAuditEntity toEntity(BaseMessage baseMessage, MessageStatus status) throws JsonProcessingException {
        MessageAuditEntity messageAuditEntity = new MessageAuditEntity();
        messageAuditEntity.setMessageId(baseMessage.getMessageId());
        messageAuditEntity.setSender(baseMessage.getSender());
        messageAuditEntity.setRecipient(baseMessage.getRecipient());
        messageAuditEntity.setMessageContent(baseMessage.getContent());
        messageAuditEntity.setPriority(baseMessage.getPriority());
        messageAuditEntity.setMessageType(baseMessage.getMessageType());
        messageAuditEntity.setStatus(status);
        messageAuditEntity.setMetadata(buildMetadata(baseMessage));

        return messageAuditEntity;
    }

    private Map<String, String> buildMetadata(BaseMessage baseMessage) {
        return Collections.emptyMap();
    }

}
