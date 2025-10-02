package com.bellpatra.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String recipientEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlContent;
    
    @Column(columnDefinition = "TEXT")
    private String textContent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailType emailType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.PENDING;
    
    @Column
    private Integer retryCount = 0;
    
    @Column
    private Integer maxRetries = 3;
    
    @Column
    private String errorMessage;
    
    @Column
    private LocalDateTime scheduledAt;
    
    @Column
    private LocalDateTime processedAt;
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data like IP address, device info, etc.
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EmailType {
        WELCOME_EMAIL,
        LOGIN_SUCCESS,
        PASSWORD_RESET,
        ACCOUNT_STATUS_CHANGE,
        NOTIFICATION
    }
    
    public enum EmailStatus {
        PENDING,
        PROCESSING,
        SENT,
        FAILED,
        CANCELLED
    }
}
