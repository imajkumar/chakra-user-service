package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ForgotPasswordRequest;
import com.bellpatra.userservice.dto.ResetPasswordRequest;
import com.bellpatra.userservice.service.PasswordResetService;
import com.bellpatra.userservice.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Received forgot password request for email: {}", request.getEmail());
        ApiResponse<String> response = passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOTP(@RequestParam String email, @RequestParam String otp) {
        log.info("Received OTP verification request for email: {}", email);
        ApiResponse<String> response = passwordResetService.verifyOTP(email, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Received password reset request for email: {}", request.getEmail());
        ApiResponse<String> response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
