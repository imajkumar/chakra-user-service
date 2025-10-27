package com.bellpatra.userservice.service;

import com.bellpatra.userservice.dto.CreatePinRequest;
import com.bellpatra.userservice.dto.PinResponse;
import com.bellpatra.userservice.dto.VerifyPinRequest;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.entity.UserPin;
import com.bellpatra.userservice.repository.UserPinRepository;
import com.bellpatra.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPinService {
    
    private final UserPinRepository userPinRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_TIME_MINUTES = 15;
    
    @Transactional
    public PinResponse createPin(String userEmail, CreatePinRequest request) {
        // Validate PIN confirmation
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new RuntimeException("PIN and confirm PIN do not match");
        }
        
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has an active PIN
        Optional<UserPin> existingPin = userPinRepository.findByUserIdAndIsActiveTrue(user.getId());
        if (existingPin.isPresent()) {
            throw new RuntimeException("User already has an active PIN. Please update existing PIN instead.");
        }
        
        // Hash the PIN
        String pinHash = passwordEncoder.encode(request.getPin());
        
        // Create new PIN
        UserPin userPin = UserPin.builder()
                .userId(user.getId())
                .pinHash(pinHash)
                .isActive(true)
                .failedAttempts(0)
                .build();
        
        userPinRepository.save(userPin);
        
        log.info("PIN created successfully for user: {}", userEmail);
        
        return PinResponse.builder()
                .hasPin(true)
                .isLocked(false)
                .createdAt(userPin.getCreatedAt())
                .failedAttempts(0)
                .message("PIN created successfully")
                .build();
    }
    
    @Transactional
    public PinResponse updatePin(String userEmail, CreatePinRequest request) {
        // Validate PIN confirmation
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new RuntimeException("PIN and confirm PIN do not match");
        }
        
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get existing PIN
        UserPin existingPin = userPinRepository.findByUserIdAndIsActiveTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("No active PIN found. Please create a new PIN first."));
        
        // Update existing PIN
        existingPin.setPinHash(passwordEncoder.encode(request.getPin()));
        existingPin.setFailedAttempts(0);
        existingPin.setLockedUntil(null);
        existingPin.setIsActive(true);
        
        userPinRepository.save(existingPin);
        
        log.info("PIN updated successfully for user: {}", userEmail);
        
        return PinResponse.builder()
                .hasPin(true)
                .isLocked(false)
                .createdAt(existingPin.getCreatedAt())
                .failedAttempts(0)
                .message("PIN updated successfully")
                .build();
    }
    
    @Transactional
    public PinResponse verifyPin(String userEmail, VerifyPinRequest request) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's PIN
        UserPin userPin = userPinRepository.findByUserIdAndIsActiveTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("No active PIN found"));
        
        // Check if PIN is locked
        if (userPin.getLockedUntil() != null && userPin.getLockedUntil().isAfter(LocalDateTime.now())) {
            return PinResponse.builder()
                    .hasPin(true)
                    .isLocked(true)
                    .lockedUntil(userPin.getLockedUntil())
                    .failedAttempts(userPin.getFailedAttempts())
                    .message("PIN is locked due to too many failed attempts. Please try again later.")
                    .build();
        }
        
        // Verify PIN
        if (passwordEncoder.matches(request.getPin(), userPin.getPinHash())) {
            // PIN is correct - reset failed attempts and update last used
            userPinRepository.resetFailedAttempts(user.getId());
            userPinRepository.updateLastUsedAt(user.getId(), LocalDateTime.now());
            
            log.info("PIN verified successfully for user: {}", userEmail);
            
            return PinResponse.builder()
                    .hasPin(true)
                    .isLocked(false)
                    .lastUsedAt(LocalDateTime.now())
                    .failedAttempts(0)
                    .message("PIN verified successfully")
                    .build();
        } else {
            // PIN is incorrect - increment failed attempts
            userPinRepository.incrementFailedAttempts(user.getId());
            
            // Get updated PIN to check if it should be locked
            UserPin updatedPin = userPinRepository.findByUserIdAndIsActiveTrue(user.getId())
                    .orElseThrow(() -> new RuntimeException("PIN not found"));
            
            if (updatedPin.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                // Lock the PIN
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES);
                userPinRepository.lockPin(user.getId(), lockUntil);
                
                log.warn("PIN locked for user: {} due to {} failed attempts", userEmail, updatedPin.getFailedAttempts());
                
                return PinResponse.builder()
                        .hasPin(true)
                        .isLocked(true)
                        .lockedUntil(lockUntil)
                        .failedAttempts(updatedPin.getFailedAttempts())
                        .message("PIN is now locked due to too many failed attempts. Please try again in " + LOCK_TIME_MINUTES + " minutes.")
                        .build();
            } else {
                int remainingAttempts = MAX_FAILED_ATTEMPTS - updatedPin.getFailedAttempts();
                log.warn("Invalid PIN attempt for user: {}. {} attempts remaining", userEmail, remainingAttempts);
                
                return PinResponse.builder()
                        .hasPin(true)
                        .isLocked(false)
                        .failedAttempts(updatedPin.getFailedAttempts())
                        .message("Invalid PIN. " + remainingAttempts + " attempts remaining.")
                        .build();
            }
        }
    }
    
    public PinResponse getPinStatus(String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user's PIN
        Optional<UserPin> userPin = userPinRepository.findByUserIdAndIsActiveTrue(user.getId());
        
        if (userPin.isEmpty()) {
            return PinResponse.builder()
                    .hasPin(false)
                    .isLocked(false)
                    .message("No PIN set up for your account")
                    .build();
        }
        
        UserPin pin = userPin.get();
        boolean isLocked = pin.getLockedUntil() != null && pin.getLockedUntil().isAfter(LocalDateTime.now());
        
        return PinResponse.builder()
                .hasPin(true)
                .isLocked(isLocked)
                .lockedUntil(pin.getLockedUntil())
                .lastUsedAt(pin.getLastUsedAt())
                .createdAt(pin.getCreatedAt())
                .failedAttempts(pin.getFailedAttempts())
                .message(isLocked ? "PIN is locked" : "PIN is active")
                .build();
    }
    
    @Transactional
    public PinResponse deletePin(String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Deactivate PIN
        userPinRepository.deactivatePin(user.getId());
        
        log.info("PIN deleted successfully for user: {}", userEmail);
        
        return PinResponse.builder()
                .hasPin(false)
                .isLocked(false)
                .message("PIN deleted successfully")
                .build();
    }
    
    @Transactional
    public PinResponse resetPin(String userEmail, CreatePinRequest request) {
        // Validate PIN confirmation
        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new RuntimeException("PIN and confirm PIN do not match");
        }
        
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get existing PIN
        Optional<UserPin> existingPin = userPinRepository.findByUserIdAndIsActiveTrue(user.getId());
        
        if (existingPin.isPresent()) {
            // Update existing PIN
            UserPin userPin = existingPin.get();
            userPin.setPinHash(passwordEncoder.encode(request.getPin()));
            userPin.setFailedAttempts(0);
            userPin.setLockedUntil(null);
            userPin.setLastUsedAt(null);
            userPin.setUpdatedAt(LocalDateTime.now());
            
            userPinRepository.save(userPin);
        } else {
            // Create new PIN if none exists
            String pinHash = passwordEncoder.encode(request.getPin());
            
            UserPin userPin = UserPin.builder()
                    .userId(user.getId())
                    .pinHash(pinHash)
                    .isActive(true)
                    .failedAttempts(0)
                    .build();
            
            userPinRepository.save(userPin);
        }
        
        log.info("PIN reset successfully for user: {}", userEmail);
        
        return PinResponse.builder()
                .hasPin(true)
                .isLocked(false)
                .createdAt(LocalDateTime.now())
                .failedAttempts(0)
                .message("PIN reset successfully")
                .build();
    }
}
