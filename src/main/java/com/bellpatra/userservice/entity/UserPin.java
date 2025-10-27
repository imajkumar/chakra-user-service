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
@Table(name = "user_pins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "pin_hash", nullable = false, length = 255)
    private String pinHash;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
