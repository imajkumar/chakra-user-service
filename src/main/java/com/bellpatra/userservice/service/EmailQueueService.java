package com.bellpatra.userservice.service;

import com.bellpatra.userservice.entity.EmailQueue;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.repository.EmailQueueRepository;
import com.bellpatra.userservice.repository.PasswordResetTokenRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    private final EmailQueueRepository emailQueueRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailQueue queueWelcomeEmail(User user) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userId", user.getId().toString());
            metadata.put("userRole", user.getRole().name());
            metadata.put("registrationTime", user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString());
            metadata.put("loginUrl", "http://localhost:8060/login");

            EmailQueue emailQueue = EmailQueue.builder()
                    .recipientEmail(user.getEmail())
                    .subject("🎉 Welcome to ChakraERP - Your Account is Ready!")
                    .htmlContent("") // Will be populated by processor
                    .textContent("") // Will be populated by processor
                    .emailType(EmailQueue.EmailType.WELCOME_EMAIL)
                    .status(EmailQueue.EmailStatus.PENDING)
                    .scheduledAt(LocalDateTime.now())
                    .metadata(convertToJson(metadata))
                    .build();

            EmailQueue savedEmail = emailQueueRepository.save(emailQueue);
            log.info("Welcome email queued for user: {} with ID: {}", user.getEmail(), savedEmail.getId());
            return savedEmail;

        } catch (Exception e) {
            log.error("Failed to queue welcome email for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to queue welcome email", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailQueue queueLoginSuccessEmail(User user, String ipAddress, String deviceInfo) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userId", user.getId().toString());
            metadata.put("userRole", user.getRole().name());
            metadata.put("ipAddress", ipAddress);
            metadata.put("deviceInfo", deviceInfo);
            metadata.put("loginTime", LocalDateTime.now().toString());

            EmailQueue emailQueue = EmailQueue.builder()
                    .recipientEmail(user.getEmail())
                    .subject("🔐 Login Successful - ChakraERP Security Alert")
                    .htmlContent("") // Will be populated by processor
                    .textContent("") // Will be populated by processor
                    .emailType(EmailQueue.EmailType.LOGIN_SUCCESS)
                    .status(EmailQueue.EmailStatus.PENDING)
                    .scheduledAt(LocalDateTime.now())
                    .metadata(convertToJson(metadata))
                    .build();

            EmailQueue savedEmail = emailQueueRepository.save(emailQueue);
            log.info("Login success email queued for user: {} with ID: {}", user.getEmail(), savedEmail.getId());
            return savedEmail;

        } catch (Exception e) {
            log.error("Failed to queue login success email for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to queue login success email", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailQueue queuePasswordResetEmail(User user, String otp, String ipAddress) {
        try {
            // Create metadata
            Map<String, Object> metadata = Map.of(
                "otp", otp,
                "ipAddress", ipAddress
            );

            EmailQueue emailQueue = EmailQueue.builder()
                .recipientEmail(user.getEmail())
                .subject("🔐 Password Reset OTP - ChakraERP")
                .emailType(EmailQueue.EmailType.PASSWORD_RESET)
                .htmlContent("") // Will be populated by template
                .textContent("Your password reset OTP is: " + otp)
                .metadata(objectMapper.writeValueAsString(metadata))
                .status(EmailQueue.EmailStatus.PENDING)
                .scheduledAt(LocalDateTime.now())
                .maxRetries(3)
                .retryCount(0)
                .build();

            EmailQueue savedEmail = emailQueueRepository.save(emailQueue);
            log.info("Password reset email queued for user: {} with ID: {}", user.getEmail(), savedEmail.getId());
            return savedEmail;

        } catch (Exception e) {
            log.error("Failed to queue password reset email for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to queue password reset email", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailQueue queuePasswordChangeEmail(User user, String ipAddress, String deviceInfo) {
        try {
            // Create metadata
            Map<String, Object> metadata = Map.of(
                "ipAddress", ipAddress,
                "deviceInfo", deviceInfo
            );

            EmailQueue emailQueue = EmailQueue.builder()
                .recipientEmail(user.getEmail())
                .subject("✅ Password Changed Successfully - ChakraERP Security Alert")
                .emailType(EmailQueue.EmailType.PASSWORD_CHANGE)
                .htmlContent("") // Will be populated by template
                .textContent("Your password has been successfully changed.")
                .metadata(objectMapper.writeValueAsString(metadata))
                .status(EmailQueue.EmailStatus.PENDING)
                .scheduledAt(LocalDateTime.now())
                .maxRetries(3)
                .retryCount(0)
                .build();

            EmailQueue savedEmail = emailQueueRepository.save(emailQueue);
            log.info("Password change email queued for user: {} with ID: {}", user.getEmail(), savedEmail.getId());
            return savedEmail;

        } catch (Exception e) {
            log.error("Failed to queue password change email for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to queue password change email", e);
        }
    }

    public List<EmailQueue> getPendingEmails(int limit) {
        return emailQueueRepository.findByStatusAndScheduledAtLessThanEqualOrderByCreatedAtAsc(
                EmailQueue.EmailStatus.PENDING, 
                LocalDateTime.now()
        );
    }

    public List<EmailQueue> getFailedEmails(int limit) {
        return emailQueueRepository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                EmailQueue.EmailStatus.FAILED, 
                3 // max retries
        );
    }

    @Transactional
    public void markAsProcessing(UUID emailId) {
        emailQueueRepository.updateStatus(emailId, EmailQueue.EmailStatus.PROCESSING, LocalDateTime.now());
    }

    @Transactional
    public void markAsSent(UUID emailId) {
        emailQueueRepository.updateStatus(emailId, EmailQueue.EmailStatus.SENT, LocalDateTime.now());
    }

    @Transactional
    public void markAsFailed(UUID emailId, String errorMessage) {
        emailQueueRepository.updateFailedStatus(emailId, EmailQueue.EmailStatus.FAILED, errorMessage);
    }

    @Transactional
    public void cleanupOldEmails() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Keep emails for 30 days
        emailQueueRepository.deleteOldProcessedEmails(EmailQueue.EmailStatus.SENT, cutoffDate);
        log.info("Cleaned up old processed emails");
    }

    public long getPendingEmailCount() {
        return emailQueueRepository.countByStatus(EmailQueue.EmailStatus.PENDING);
    }

    public long getFailedEmailCount() {
        return emailQueueRepository.countByStatus(EmailQueue.EmailStatus.FAILED);
    }

    public List<EmailQueue> getUserEmails(String recipientEmail) {
        return emailQueueRepository.findByRecipientEmailAndStatusOrderByCreatedAtDesc(
                recipientEmail, 
                EmailQueue.EmailStatus.SENT
        );
    }

    public Map<String, Object> getEmailQueueStats() {
        try {
            long totalEmails = emailQueueRepository.count();
            long pendingEmails = emailQueueRepository.countByStatus(EmailQueue.EmailStatus.PENDING);
            long sentEmails = emailQueueRepository.countByStatus(EmailQueue.EmailStatus.SENT);
            long failedEmails = emailQueueRepository.countByStatus(EmailQueue.EmailStatus.FAILED);

            return Map.of(
                    "totalEmails", totalEmails,
                    "pendingEmails", pendingEmails,
                    "sentEmails", sentEmails,
                    "failedEmails", failedEmails
            );
        } catch (Exception e) {
            log.error("Failed to get email queue stats", e);
            return Map.of(
                    "totalEmails", 0L,
                    "pendingEmails", 0L,
                    "sentEmails", 0L,
                    "failedEmails", 0L
            );
        }
    }

    private String convertToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert metadata to JSON", e);
            return "{}";
        }
    }
}
