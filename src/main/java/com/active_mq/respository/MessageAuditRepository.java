package com.active_mq.respository;

import com.active_mq.model.entity.MessageAuditEntity;
import com.active_mq.model.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAuditRepository extends JpaRepository<MessageAuditEntity, Long> {


    @Modifying
    @Query("UPDATE MessageAuditEntity ma SET ma.status =:status WHERE ma.messageId =:messageId")
    void updateStatusByMessageId(@Param("messageId") String messageId, @Param("status") MessageStatus status);
}
