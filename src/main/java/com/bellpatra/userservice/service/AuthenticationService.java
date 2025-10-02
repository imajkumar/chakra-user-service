package com.bellpatra.userservice.service;

import com.bellpatra.userservice.dto.AuthResponse;
import com.bellpatra.userservice.dto.LoginRequest;
import com.bellpatra.userservice.dto.RefreshTokenRequest;
import com.bellpatra.userservice.dto.RegisterRequest;
import com.bellpatra.userservice.entity.RefreshToken;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.repository.RefreshTokenRepository;
import com.bellpatra.userservice.repository.UserRepository;
import com.bellpatra.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailQueueService emailQueueService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Starting user registration for email: {}", request.getEmail());
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("User with email {} already exists", request.getEmail());
                throw new RuntimeException("User with email " + request.getEmail() + " already exists");
            }
            log.info("Email {} is available", request.getEmail());
            
            // Check if phone number already exists (if provided)
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    log.warn("User with phone number {} already exists", request.getPhoneNumber());
                    throw new RuntimeException("User with phone number " + request.getPhoneNumber() + " already exists");
                }
                log.info("Phone number {} is available", request.getPhoneNumber());
            }

            // Create new user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            // Only set phoneNumber if it's not null or empty to avoid unique constraint issues
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                user.setPhoneNumber(request.getPhoneNumber());
                log.info("Phone number set: {}", request.getPhoneNumber());
            } else {
                log.info("Phone number not provided or empty");
            }
            user.setRole(request.getRole());
            user.setStatus(User.UserStatus.ACTIVE);
            log.info("User object created, attempting to save...");

            User savedUser = userRepository.save(user);
            log.info("User saved successfully with ID: {}", savedUser.getId());

            // Queue welcome email
            try {
                emailQueueService.queueWelcomeEmail(savedUser);
                log.info("Welcome email queued successfully for: {}", savedUser.getEmail());
            } catch (Exception e) {
                log.error("Failed to queue welcome email for: {}", savedUser.getEmail(), e);
                // Don't fail registration if email queuing fails - this is non-critical
            }

            // Generate tokens
            log.info("Generating access token...");
            String accessToken = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
            log.info("Access token generated successfully");
            
            log.info("Generating refresh token...");
            String refreshToken = generateRefreshToken(savedUser.getEmail());
            log.info("Refresh token generated successfully");

            log.info("Creating AuthResponse...");
            AuthResponse response = new AuthResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    86400000L, // 24 hours
                    savedUser.getEmail(),
                    savedUser.getRole().name(),
                    savedUser // Include complete user data
            );
            
            log.info("Registration completed successfully for: {}", savedUser.getEmail());
            return response;
            
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }

        // Revoke existing refresh tokens
        refreshTokenRepository.revokeByUserEmail(user.getEmail());

        // Queue login success email
        try {
            String ipAddress = getClientIPAddress(); // You can implement this method
            String deviceInfo = "Web Browser"; // You can enhance this with actual device detection
            emailQueueService.queueLoginSuccessEmail(user, ipAddress, deviceInfo);
            log.info("Login success email queued successfully for: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to queue login success email for: {}", user.getEmail(), e);
            // Don't fail login if email queuing fails
        }

        // Generate new tokens
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                86400000L, // 24 hours
                user.getEmail(),
                user.getRole().name(),
                user // Include complete user data
        );
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        User user = userRepository.findByEmail(refreshToken.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }

        // Revoke current refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = generateRefreshToken(user.getEmail());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                86400000L, // 24 hours
                user.getEmail(),
                user.getRole().name(),
                user // Include complete user data
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    private String generateRefreshToken(String userEmail) {
        log.info("Creating refresh token for user: {}", userEmail);
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7); // 7 days

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserEmail(userEmail);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(false);

        log.info("Saving refresh token to database...");
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token saved successfully");
        return token;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private String getClientIPAddress() {
        // For now, return a placeholder. In a real application, you would get this from the HTTP request
        // You can inject HttpServletRequest and extract the real IP address
        return "192.168.1.100";
    }
}
