package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class TestEmailController {

    private final EmailQueueService emailQueueService;

    @PostMapping("/send-test-email")
    public ResponseEntity<ApiResponse<String>> sendTestEmail(@RequestParam String email) {
        try {
            // Create a test user object for the email
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setEmail(email);
            testUser.setPassword("password123");
            testUser.setRole(User.UserRole.USER);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());

            // Queue a welcome email
            emailQueueService.queueWelcomeEmail(testUser);
            
            log.info("Test welcome email queued for: {}", email);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Test email queued successfully", 
                    "Welcome email has been queued for " + email
            ));
            
        } catch (Exception e) {
            log.error("Failed to queue test email for: {}", email, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to queue test email: " + e.getMessage()));
        }
    }

    @PostMapping("/send-login-email")
    public ResponseEntity<ApiResponse<String>> sendTestLoginEmail(@RequestParam String email) {
        try {
            // Create a test user object for the email
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setEmail(email);
            testUser.setPassword("password123");
            testUser.setRole(User.UserRole.USER);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());

            // Queue a login success email
            emailQueueService.queueLoginSuccessEmail(testUser, "192.168.1.100", "Test Browser");
            
            log.info("Test login email queued for: {}", email);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Test login email queued successfully", 
                    "Login success email has been queued for " + email
            ));
            
        } catch (Exception e) {
            log.error("Failed to queue test login email for: {}", email, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to queue test login email: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "service", "Test Email Service",
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(healthData, "Test email service is running"));
    }
}
