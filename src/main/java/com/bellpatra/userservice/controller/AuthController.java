package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import com.bellpatra.userservice.dto.AuthResponse;
import com.bellpatra.userservice.dto.LoginRequest;
import com.bellpatra.userservice.dto.RefreshTokenRequest;
import com.bellpatra.userservice.dto.RegisterRequest;
import com.bellpatra.userservice.service.AuthenticationService;
import com.bellpatra.userservice.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authenticationService.register(request);
            
            // Publish user registration event to Kafka
            kafkaProducerService.publishUserRegistration(
                response.getUser().getId().toString(),
                response.getUser().getEmail(),
                response.getUser().getFirstName(),
                response.getUser().getLastName()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(response, "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Extract IP address from request
            String ipAddress = getClientIpAddress(httpRequest);
            request.setIpAddress(ipAddress);
            
            AuthResponse response = authenticationService.login(request);
            
            // Publish user login event to Kafka
            kafkaProducerService.publishUserLogin(
                response.getUser().getId().toString(),
                response.getUser().getEmail(),
                ipAddress
            );
            
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authenticationService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody RefreshTokenRequest request) {
        try {
            authenticationService.logout(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "Logout successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Logout failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "service", "Auth Service",
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(healthData, "Auth service is running"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
