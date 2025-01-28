package com.active_mq.service;

import com.active_mq.core.model.BaseMessage;
import com.active_mq.mapper.MessageAuditMapper;
import com.active_mq.model.entity.MessageAuditEntity;
import com.active_mq.model.enums.ChannelType;
import com.active_mq.model.enums.MessageStatus;
import com.active_mq.respository.MessageAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling message auditing operations.
 */
@Service
public class MessageAuditService {

    Logger log = LoggerFactory.getLogger(MessageAuditService.class);

    private final MessageAuditRepository repository;
    private final MessageAuditMapper messageAuditMapper;

    public MessageAuditService(MessageAuditRepository repository, MessageAuditMapper messageAuditMapper) {
        this.repository = repository;
        this.messageAuditMapper = messageAuditMapper;
    }

    /**
     * Persists the given message and its associated channel type(TOPIC, QUEUE, DLQ...) as an audit record.
     *
     * @param baseMessage The message to be audited.
     * @param channelType The channel type associated with the message.
     */
    @Transactional
    public void persist(BaseMessage baseMessage, ChannelType channelType) {
        try {
            MessageAuditEntity audit = messageAuditMapper.toEntity(baseMessage, channelType);
            repository.save(audit);
            repository.flush();
            log.info("Message audit created successfully. {} Status: {}", baseMessage.getMessageId(), audit.getStatus());
        } catch (Exception e) {
            log.error("Failed to create audit log message {}", baseMessage.getMessageId(), e);
        }
    }

    /**
     * Updates the status of a message audit record based on its message ID.
     *
     * @param messageId The ID of the message to update.
     * @param status    The new status to set.
     */
    @Transactional
    public void updateStatusByMessageId(String messageId, MessageStatus status) {
        repository.updateStatusByMessageId(messageId, status);
    }

    /**
     * Retrieves a message audit record by its message ID, or throws an exception if not found.
     *
     * @param messageId The ID of the message to retrieve.
     * @return The MessageAuditEntity corresponding to the given message ID.
     * @throws RuntimeException if no record is found for the given ID.
     */
    public MessageAuditEntity getOneByMessageIdOrFail(String messageId) {
        return repository.findByMessageId(messageId).orElseThrow(() -> new RuntimeException("Message not found by message id: " + messageId));
    }

    /**
     * Checks if a message audit record exists for the given message ID.
     *
     * @param messageId The ID of the message to check.
     * @return True if a record exists, false otherwise.
     */
    public boolean existsByMessageId(String messageId) {
        return repository.existsByMessageId(messageId);
    }
}
