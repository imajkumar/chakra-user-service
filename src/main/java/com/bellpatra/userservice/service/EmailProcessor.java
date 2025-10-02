package com.bellpatra.userservice.service;

import com.bellpatra.userservice.entity.EmailQueue;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.repository.EmailQueueRepository;
import com.bellpatra.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessor {

    private final EmailQueueRepository emailQueueRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public void processPendingEmails() {
        try {
            List<EmailQueue> pendingEmails = emailQueueRepository.findByStatusAndScheduledAtLessThanEqualOrderByCreatedAtAsc(
                    EmailQueue.EmailStatus.PENDING, 
                    LocalDateTime.now()
            );

            if (pendingEmails.isEmpty()) {
                log.debug("No pending emails to process");
                return;
            }

            log.info("Processing {} pending emails", pendingEmails.size());

            for (EmailQueue emailQueue : pendingEmails) {
                processEmail(emailQueue);
            }

        } catch (Exception e) {
            log.error("Error processing pending emails", e);
        }
    }

    public void processFailedEmails() {
        try {
            List<EmailQueue> failedEmails = emailQueueRepository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                    EmailQueue.EmailStatus.FAILED, 
                    3 // max retries
            );

            if (failedEmails.isEmpty()) {
                log.debug("No failed emails to retry");
                return;
            }

            log.info("Retrying {} failed emails", failedEmails.size());

            for (EmailQueue emailQueue : failedEmails) {
                processEmail(emailQueue);
            }

        } catch (Exception e) {
            log.error("Error processing failed emails", e);
        }
    }

    @Async
    public void processEmail(EmailQueue emailQueue) {
        try {
            log.info("Starting to process email ID: {}, Type: {}, Recipient: {}", 
                    emailQueue.getId(), emailQueue.getEmailType(), emailQueue.getRecipientEmail());
            
            // Mark as processing
            emailQueueRepository.updateStatus(emailQueue.getId(), EmailQueue.EmailStatus.PROCESSING, LocalDateTime.now());

            // Get user data if needed
            User user = null;
            if (emailQueue.getEmailType() == EmailQueue.EmailType.WELCOME_EMAIL || 
                emailQueue.getEmailType() == EmailQueue.EmailType.LOGIN_SUCCESS ||
                emailQueue.getEmailType() == EmailQueue.EmailType.PASSWORD_RESET ||
                emailQueue.getEmailType() == EmailQueue.EmailType.PASSWORD_CHANGE) {
                user = userRepository.findByEmail(emailQueue.getRecipientEmail()).orElse(null);
                log.info("User lookup for email {}: {}", emailQueue.getRecipientEmail(), user != null ? "Found" : "Not found");
            }

            // Process based on email type
            switch (emailQueue.getEmailType()) {
                case WELCOME_EMAIL:
                    log.info("Processing WELCOME_EMAIL for user: {}", user != null ? user.getEmail() : "null");
                    processWelcomeEmail(emailQueue, user);
                    break;
                case LOGIN_SUCCESS:
                    log.info("Processing LOGIN_SUCCESS for user: {}", user != null ? user.getEmail() : "null");
                    processLoginSuccessEmail(emailQueue, user);
                    break;
                case PASSWORD_RESET:
                    log.info("Processing PASSWORD_RESET for user: {}", user != null ? user.getEmail() : "null");
                    processPasswordResetEmail(emailQueue, user);
                    break;
                case PASSWORD_CHANGE:
                    log.info("Processing PASSWORD_CHANGE for user: {}", user != null ? user.getEmail() : "null");
                    processPasswordChangeEmail(emailQueue, user);
                    break;
                default:
                    log.warn("Unknown email type: {}", emailQueue.getEmailType());
                    emailQueueRepository.updateFailedStatus(emailQueue.getId(), EmailQueue.EmailStatus.FAILED, "Unknown email type");
                    return;
            }

            // Mark as sent
            emailQueueRepository.updateStatus(emailQueue.getId(), EmailQueue.EmailStatus.SENT, LocalDateTime.now());
            log.info("Successfully processed email ID: {} for recipient: {}", emailQueue.getId(), emailQueue.getRecipientEmail());

        } catch (Exception e) {
            log.error("Failed to process email ID: {} for recipient: {}", emailQueue.getId(), emailQueue.getRecipientEmail(), e);
            emailQueueRepository.updateFailedStatus(emailQueue.getId(), EmailQueue.EmailStatus.FAILED, e.getMessage());
        }
    }

    private void processWelcomeEmail(EmailQueue emailQueue, User user) {
        if (user == null) {
            throw new RuntimeException("User not found for welcome email");
        }

        // Parse metadata to get login URL
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String loginUrl = (String) metadata.getOrDefault("loginUrl", "http://localhost:8060/login");

        // Send email with login URL
        emailService.sendWelcomeEmail(user, loginUrl);
    }

    private void processLoginSuccessEmail(EmailQueue emailQueue, User user) {
        if (user == null) {
            log.error("User not found for login success email. EmailQueue ID: {}, Recipient: {}", 
                     emailQueue.getId(), emailQueue.getRecipientEmail());
            throw new RuntimeException("User not found for login success email");
        }

        log.info("Processing login success email for user: {} (ID: {})", user.getEmail(), user.getId());

        // Parse metadata
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String ipAddress = (String) metadata.getOrDefault("ipAddress", "Unknown");
        String deviceInfo = (String) metadata.getOrDefault("deviceInfo", "Unknown");

        log.info("Login success email metadata - IP: {}, Device: {}", ipAddress, deviceInfo);

        try {
            // Use the EmailService method with Thymeleaf template
            emailService.sendLoginSuccessEmail(user, ipAddress, deviceInfo);
            log.info("Login success email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send login success email to: {}", user.getEmail(), e);
            throw e;
        }
    }

    private void processPasswordResetEmail(EmailQueue emailQueue, User user) {
        if (user == null) {
            log.error("User not found for password reset email. EmailQueue ID: {}, Recipient: {}", 
                     emailQueue.getId(), emailQueue.getRecipientEmail());
            throw new RuntimeException("User not found for password reset email");
        }

        log.info("Processing password reset email for user: {} (ID: {})", user.getEmail(), user.getId());

        // Parse metadata
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String otp = (String) metadata.getOrDefault("otp", "000000");
        String ipAddress = (String) metadata.getOrDefault("ipAddress", "Unknown");

        log.info("Password reset email metadata - OTP: {}, IP: {}", otp, ipAddress);

        try {
            // Use the EmailService method with Thymeleaf template
            emailService.sendPasswordResetEmail(user, otp, ipAddress);
            log.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw e;
        }
    }

    private void processPasswordChangeEmail(EmailQueue emailQueue, User user) {
        if (user == null) {
            log.error("User not found for password change email. EmailQueue ID: {}, Recipient: {}", 
                     emailQueue.getId(), emailQueue.getRecipientEmail());
            throw new RuntimeException("User not found for password change email");
        }

        log.info("Processing password change email for user: {} (ID: {})", user.getEmail(), user.getId());

        // Parse metadata
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String ipAddress = (String) metadata.getOrDefault("ipAddress", "Unknown");
        String deviceInfo = (String) metadata.getOrDefault("deviceInfo", "Unknown");

        log.info("Password change email metadata - IP: {}, Device: {}", ipAddress, deviceInfo);

        try {
            // Use the EmailService method with Thymeleaf template
            emailService.sendPasswordChangeEmail(user, ipAddress, deviceInfo);
            log.info("Password change email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password change email to: {}", user.getEmail(), e);
            throw e;
        }
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse metadata JSON", e);
            return Map.of();
        }
    }

}
