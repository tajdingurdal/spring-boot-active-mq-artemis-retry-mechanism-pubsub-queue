package com.active_mq.service;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.mapper.MessageAuditMapper;
import com.active_mq.model.entity.MessageAuditEntity;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.respository.MessageAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageAuditService {

    Logger log = LoggerFactory.getLogger(MessageAuditService.class);

    private final MessageAuditRepository repository;
    private final MessageAuditMapper messageAuditMapper;

    public MessageAuditService(MessageAuditRepository repository, MessageAuditMapper messageAuditMapper) {
        this.repository = repository;
        this.messageAuditMapper = messageAuditMapper;
    }

    @Transactional
    public void persist(BaseMessage baseMessage, MessageStatus status) {
        try {
            MessageAuditEntity audit = messageAuditMapper.toEntity(baseMessage, status);
            repository.save(audit);
        } catch (Exception e) {
            log.error("Failed to create audit log message {}", baseMessage.getMessageId(), e);
        }
    }

    @Transactional
    public void updateStatusByMessageId(String messageId, MessageStatus status) {
        repository.updateStatusByMessageId(messageId, status);
    }


}
