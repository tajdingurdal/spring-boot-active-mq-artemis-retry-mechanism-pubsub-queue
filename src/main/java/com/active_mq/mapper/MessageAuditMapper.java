package com.active_mq.mapper;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.model.entity.MessageAuditEntity;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.MessageStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Mapper(componentModel = "spring")
@Component
public abstract class MessageAuditMapper {

    public MessageAuditEntity toEntity(BaseMessage baseMessage, ChannelType channelType) throws JsonProcessingException {
        MessageAuditEntity entity = new MessageAuditEntity();
        entity.setMessageId(baseMessage.getMessageId());
        entity.setSender(baseMessage.getSender());
        entity.setRecipient(baseMessage.getRecipient());
        entity.setMessageContent(baseMessage.getContent());
        entity.setPriority(baseMessage.getPriority());
        entity.setMessageType(baseMessage.getMessageType());
        entity.setStatus(baseMessage.getStatus());
        entity.setChannelType(channelType);
        entity.setConsumerType(baseMessage.getConsumerType());
        entity.setMetadata(buildMetadata(baseMessage));

        return entity;
    }

    private Map<String, String> buildMetadata(BaseMessage baseMessage) {
        return Collections.emptyMap();
    }

}
