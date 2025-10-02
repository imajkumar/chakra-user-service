package com.bellpatra.userservice.repository;

import com.bellpatra.userservice.entity.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, UUID> {
    
    List<EmailQueue> findByStatusAndScheduledAtLessThanEqualOrderByCreatedAtAsc(
            EmailQueue.EmailStatus status, 
            LocalDateTime scheduledAt
    );
    
    List<EmailQueue> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            EmailQueue.EmailStatus status, 
            Integer maxRetries
    );
    
    List<EmailQueue> findByRecipientEmailAndStatusOrderByCreatedAtDesc(
            String recipientEmail, 
            EmailQueue.EmailStatus status
    );
    
    List<EmailQueue> findByEmailTypeAndStatusOrderByCreatedAtDesc(
            EmailQueue.EmailType emailType, 
            EmailQueue.EmailStatus status
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE EmailQueue eq SET eq.status = :status, eq.processedAt = :processedAt WHERE eq.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") EmailQueue.EmailStatus status, @Param("processedAt") LocalDateTime processedAt);
    
    @Modifying
    @Transactional
    @Query("UPDATE EmailQueue eq SET eq.status = :status, eq.retryCount = eq.retryCount + 1, eq.errorMessage = :errorMessage WHERE eq.id = :id")
    void updateFailedStatus(@Param("id") UUID id, @Param("status") EmailQueue.EmailStatus status, @Param("errorMessage") String errorMessage);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailQueue eq WHERE eq.status = :status AND eq.processedAt < :cutoffDate")
    void deleteOldProcessedEmails(@Param("status") EmailQueue.EmailStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    long countByStatus(EmailQueue.EmailStatus status);
    
    long countByRecipientEmailAndStatus(String recipientEmail, EmailQueue.EmailStatus status);
}
