package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.service.EmailService;
import com.bellpatra.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/send-welcome/{userId}")
    public ResponseEntity<ApiResponse<String>> sendWelcomeEmail(@PathVariable UUID userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            emailService.sendWelcomeEmail(user);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Welcome email sent successfully to " + user.getEmail(),
                "Email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to send email: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEmail() {
        try {
            // Create a test user for email testing
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setEmail("test@example.com");
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setRole(User.UserRole.USER);
            testUser.setStatus(User.UserStatus.ACTIVE);
            
            emailService.sendWelcomeEmail(testUser);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Test email sent successfully",
                "Email functionality is working"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Email test failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "service", "Email Service",
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(healthData, "Email service is running"));
    }
}
