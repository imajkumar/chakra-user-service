package com.bellpatra.userservice.repository;

import com.bellpatra.userservice.entity.UserPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPinRepository extends JpaRepository<UserPin, UUID> {
    
    Optional<UserPin> findByUserIdAndIsActiveTrue(UUID userId);
    
    Optional<UserPin> findByUserId(UUID userId);
    
    boolean existsByUserIdAndIsActiveTrue(UUID userId);
    
    @Modifying
    @Query("UPDATE UserPin up SET up.failedAttempts = 0, up.lockedUntil = null WHERE up.userId = :userId")
    void resetFailedAttempts(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE UserPin up SET up.failedAttempts = up.failedAttempts + 1 WHERE up.userId = :userId")
    void incrementFailedAttempts(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE UserPin up SET up.lockedUntil = :lockedUntil WHERE up.userId = :userId")
    void lockPin(@Param("userId") UUID userId, @Param("lockedUntil") LocalDateTime lockedUntil);
    
    @Modifying
    @Query("UPDATE UserPin up SET up.lastUsedAt = :lastUsedAt WHERE up.userId = :userId")
    void updateLastUsedAt(@Param("userId") UUID userId, @Param("lastUsedAt") LocalDateTime lastUsedAt);
    
    @Modifying
    @Query("UPDATE UserPin up SET up.isActive = false WHERE up.userId = :userId")
    void deactivatePin(@Param("userId") UUID userId);
}

