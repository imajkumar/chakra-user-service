package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import com.bellpatra.userservice.dto.CreatePinRequest;
import com.bellpatra.userservice.dto.PinResponse;
import com.bellpatra.userservice.dto.VerifyPinRequest;
import com.bellpatra.userservice.service.UserPinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pin")
@RequiredArgsConstructor
public class UserPinController {
    
    private final UserPinService userPinService;
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PinResponse>> createPin(
            @Valid @RequestBody CreatePinRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            PinResponse response = userPinService.createPin(userEmail, request);
            return ResponseEntity.ok(ApiResponse.success(response, "PIN created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to create PIN: " + e.getMessage()));
        }
    }
    
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<PinResponse>> updatePin(
            @Valid @RequestBody CreatePinRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            PinResponse response = userPinService.updatePin(userEmail, request);
            return ResponseEntity.ok(ApiResponse.success(response, "PIN updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to update PIN: " + e.getMessage()));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PinResponse>> verifyPin(
            @Valid @RequestBody VerifyPinRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            PinResponse response = userPinService.verifyPin(userEmail, request);
            
            if (response.isLocked()) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(ApiResponse.badRequest(response.getMessage()));
            } else if (!response.getMessage().contains("successfully")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest(response.getMessage()));
            } else {
                return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to verify PIN: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PinResponse>> getPinStatus(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            PinResponse response = userPinService.getPinStatus(userEmail);
            return ResponseEntity.ok(ApiResponse.success(response, "PIN status retrieved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to get PIN status: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<PinResponse>> deletePin(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            PinResponse response = userPinService.deletePin(userEmail);
            return ResponseEntity.ok(ApiResponse.success(response, "PIN deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to delete PIN: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<PinResponse>> resetPin(
            @Valid @RequestBody CreatePinRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            PinResponse response = userPinService.resetPin(userEmail, request);
            return ResponseEntity.ok(ApiResponse.success(response, "PIN reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to reset PIN: " + e.getMessage()));
        }
    }
}

