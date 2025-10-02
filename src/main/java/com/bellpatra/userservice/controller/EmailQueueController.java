package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import com.bellpatra.userservice.entity.EmailQueue;
import com.bellpatra.userservice.service.EmailQueueService;
import com.bellpatra.userservice.service.EmailProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/email-queue")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailQueueController {

    private final EmailQueueService emailQueueService;
    private final EmailProcessor emailProcessor;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailStats() {
        try {
            long pendingCount = emailQueueService.getPendingEmailCount();
            long failedCount = emailQueueService.getFailedEmailCount();
            
            Map<String, Object> stats = Map.of(
                    "pendingEmails", pendingCount,
                    "failedEmails", failedCount,
                    "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(ApiResponse.success(stats, "Email queue statistics retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve email statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<List<EmailQueue>>> getUserEmails(@PathVariable String email) {
        try {
            List<EmailQueue> userEmails = emailQueueService.getUserEmails(email);
            return ResponseEntity.ok(ApiResponse.success(userEmails, "User emails retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve user emails: " + e.getMessage()));
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupOldEmails() {
        try {
            emailQueueService.cleanupOldEmails();
            return ResponseEntity.ok(ApiResponse.success("Old emails cleaned up successfully", "Cleanup completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to cleanup old emails: " + e.getMessage()));
        }
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<String>> processPendingEmails() {
        try {
            emailProcessor.processPendingEmails();
            return ResponseEntity.ok(ApiResponse.success("Pending emails processed successfully", "Email processing completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to process pending emails: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "service", "Email Queue Service",
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(healthData, "Email queue service is running"));
    }
}
