package com.bellpatra.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinResponse {
    
    private boolean hasPin;
    private boolean isLocked;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private int failedAttempts;
    private String message;
}

