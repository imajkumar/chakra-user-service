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
            // Mark as processing
            emailQueueRepository.updateStatus(emailQueue.getId(), EmailQueue.EmailStatus.PROCESSING, LocalDateTime.now());

            // Get user data if needed
            User user = null;
            if (emailQueue.getEmailType() == EmailQueue.EmailType.WELCOME_EMAIL || 
                emailQueue.getEmailType() == EmailQueue.EmailType.LOGIN_SUCCESS ||
                emailQueue.getEmailType() == EmailQueue.EmailType.ACCOUNT_STATUS_CHANGE) {
                user = userRepository.findByEmail(emailQueue.getRecipientEmail()).orElse(null);
            }

            // Process based on email type
            switch (emailQueue.getEmailType()) {
                case WELCOME_EMAIL:
                    processWelcomeEmail(emailQueue, user);
                    break;
                case LOGIN_SUCCESS:
                    processLoginSuccessEmail(emailQueue, user);
                    break;
                case PASSWORD_RESET:
                    processPasswordResetEmail(emailQueue, user);
                    break;
                case ACCOUNT_STATUS_CHANGE:
                    processAccountStatusChangeEmail(emailQueue, user);
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
            throw new RuntimeException("User not found for login success email");
        }

        // Parse metadata
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String ipAddress = (String) metadata.getOrDefault("ipAddress", "Unknown");
        String deviceInfo = (String) metadata.getOrDefault("deviceInfo", "Unknown");

        // Send email directly
        emailService.sendLoginSuccessEmail(user, ipAddress, deviceInfo);
    }

    private void processPasswordResetEmail(EmailQueue emailQueue, User user) {
        if (user == null) {
            throw new RuntimeException("User not found for password reset email");
        }

        // Parse metadata
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String resetToken = (String) metadata.get("resetToken");

        String htmlContent = buildPasswordResetEmail(user, resetToken);
        String textContent = "Password reset requested. Please use the provided token to reset your password.";

        // Update email content
        emailQueue.setHtmlContent(htmlContent);
        emailQueue.setTextContent(textContent);
        emailQueueRepository.save(emailQueue);

        // Send email
        emailService.sendPasswordResetEmail(user, resetToken);
    }

    private void processAccountStatusChangeEmail(EmailQueue emailQueue, User user) {
        if (user == null) {
            throw new RuntimeException("User not found for account status change email");
        }

        // Parse metadata
        Map<String, Object> metadata = parseMetadata(emailQueue.getMetadata());
        String status = (String) metadata.get("newStatus");

        String htmlContent = buildAccountStatusChangeEmail(user, status);
        String textContent = "Your account status has been updated to: " + status;

        // Update email content
        emailQueue.setHtmlContent(htmlContent);
        emailQueue.setTextContent(textContent);
        emailQueueRepository.save(emailQueue);

        // Send email
        emailService.sendAccountStatusChangeEmail(user, status);
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse metadata JSON", e);
            return Map.of();
        }
    }

    private String buildPasswordResetEmail(User user, String resetToken) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .container { background: #f8f9fa; border-radius: 10px; padding: 30px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 24px; font-weight: bold; color: #667eea; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🔐 ChakraERP</div>
                        <h2>Password Reset Request</h2>
                    </div>
                    <p>Hello %s,</p>
                    <p>We received a request to reset your password for your ChakraERP account.</p>
                    <p>Click the button below to reset your password:</p>
                    <div style="text-align: center;">
                        <a href="#" class="button">Reset Password</a>
                    </div>
                    <p><strong>Reset Token:</strong> %s</p>
                    <p><small>This token will expire in 1 hour. If you didn't request this, please ignore this email.</small></p>
                </div>
            </body>
            </html>
            """, user.getFirstName() + " " + user.getLastName(), resetToken);
    }

    private String buildAccountStatusChangeEmail(User user, String status) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .container { background: #f8f9fa; border-radius: 10px; padding: 30px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 24px; font-weight: bold; color: #667eea; }
                    .status { padding: 10px; border-radius: 5px; text-align: center; font-weight: bold; }
                    .active { background: #d4edda; color: #155724; }
                    .inactive { background: #f8d7da; color: #721c24; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">📢 ChakraERP</div>
                        <h2>Account Status Update</h2>
                    </div>
                    <p>Hello %s,</p>
                    <p>Your account status has been updated:</p>
                    <div class="status %s">
                        Status: %s
                    </div>
                    <p>If you have any questions about this change, please contact our support team.</p>
                </div>
            </body>
            </html>
            """, user.getFirstName() + " " + user.getLastName(), 
                 status.toLowerCase(), status);
    }
}
