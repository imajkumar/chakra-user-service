package com.bellpatra.userservice.service;

import com.bellpatra.userservice.dto.ForgotPasswordRequest;
import com.bellpatra.userservice.dto.ResetPasswordRequest;
import com.bellpatra.userservice.entity.PasswordResetToken;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.repository.PasswordResetTokenRepository;
import com.bellpatra.userservice.repository.UserRepository;
import com.bellpatra.userservice.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailQueueService emailQueueService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        try {
            log.info("Processing forgot password request for email: {}", request.getEmail());

            // Check if user exists
            User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

            if (user == null) {
                log.warn("Forgot password requested for non-existent email: {}", request.getEmail());
                // Return success even if user doesn't exist for security reasons
                return ApiResponse.success(
                    "If an account with this email exists, a password reset OTP has been sent.",
                    "Please check your email for the 6-digit verification code"
                );
            }

            // Mark any existing password reset tokens for this user as used
            passwordResetTokenRepository.markAllTokensAsUsedForUser(user.getEmail());
            log.info("Marked existing tokens as used for user: {}", user.getEmail());

            // Generate 6-digit OTP
            String otp = generateOTP();
            log.info("Generated OTP for user: {}", user.getEmail());

            // Create password reset token
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .otp(otp)
                .userEmail(user.getEmail())
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // 10 minutes expiry
                .used(false)
                .build();

            // Save the token
            passwordResetTokenRepository.save(resetToken);
            log.info("Password reset token saved for user: {}", user.getEmail());

            // Queue password reset email
            try {
                String ipAddress = getClientIPAddress(); // You can implement this method
                emailQueueService.queuePasswordResetEmail(user, otp, ipAddress);
                log.info("Password reset email queued successfully for: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to queue password reset email for: {}", user.getEmail(), e);
                // Don't fail the request if email queuing fails
            }

            return ApiResponse.success(
                "If an account with this email exists, a password reset OTP has been sent.",
                "Please check your email for the 6-digit verification code"
            );

        } catch (Exception e) {
            log.error("Error processing forgot password request for email: {}", request.getEmail(), e);
            return ApiResponse.badRequest("Failed to process password reset request");
        }
    }

    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        try {
            log.info("Processing password reset for email: {}", request.getEmail());

            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ApiResponse.badRequest("Passwords do not match");
            }

            // Find valid OTP
            PasswordResetToken resetToken = passwordResetTokenRepository
                .findByOtpAndUsedFalse(request.getOtp())
                .orElse(null);

            if (resetToken == null) {
                log.warn("Invalid or used OTP: {}", request.getOtp());
                return ApiResponse.badRequest("Invalid or expired OTP");
            }

            // Check if OTP is for the correct email
            if (!resetToken.getUserEmail().equals(request.getEmail())) {
                log.warn("OTP {} does not match email {}", request.getOtp(), request.getEmail());
                return ApiResponse.badRequest("Invalid OTP for this email");
            }

            // Check if OTP is expired
            if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Expired OTP: {}", request.getOtp());
                return ApiResponse.badRequest("OTP has expired. Please request a new one");
            }

            // Find user
            User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

            if (user == null) {
                log.warn("User not found for email: {}", request.getEmail());
                return ApiResponse.badRequest("User not found");
            }

            // Update password
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
            log.info("Password updated successfully for user: {}", user.getEmail());

            // Mark OTP as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            log.info("OTP marked as used for user: {}", user.getEmail());

            // Queue password change confirmation email
            try {
                String ipAddress = getClientIPAddress(); // You can implement this method
                String deviceInfo = "Web Browser"; // You can enhance this with actual device detection
                emailQueueService.queuePasswordChangeEmail(user, ipAddress, deviceInfo);
                log.info("Password change confirmation email queued successfully for: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to queue password change confirmation email for: {}", user.getEmail(), e);
                // Don't fail the password reset if email queuing fails
            }

            return ApiResponse.success(
                "Password reset successfully",
                "Your password has been changed. Please login with your new password"
            );

        } catch (Exception e) {
            log.error("Error processing password reset for email: {}", request.getEmail(), e);
            return ApiResponse.badRequest("Failed to reset password");
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<String> verifyOTP(String email, String otp) {
        try {
            log.info("Verifying OTP for email: {}, OTP: {}", email, otp);

            // Debug: Check all tokens for this user
            var allUserTokens = passwordResetTokenRepository.findByUserEmailAndUsedFalse(email);
            log.info("Found {} unused tokens for user: {}", allUserTokens.isPresent() ? 1 : 0, email);
            allUserTokens.ifPresent(token -> 
                log.info("Token - OTP: {}, Expires: {}, Used: {}", 
                    token.getOtp(), token.getExpiresAt(), token.getUsed()));

            // Debug: Check all unused tokens with this OTP
            var allOtpTokens = passwordResetTokenRepository.findByOtpAndUsedFalse(otp);
            log.info("Found {} unused tokens with OTP: {}", allOtpTokens.isPresent() ? 1 : 0, otp);
            allOtpTokens.ifPresent(token -> 
                log.info("Token - Email: {}, Expires: {}, Used: {}", 
                    token.getUserEmail(), token.getExpiresAt(), token.getUsed()));

            PasswordResetToken resetToken = passwordResetTokenRepository
                .findByOtpAndUsedFalse(otp)
                .orElse(null);

            if (resetToken == null) {
                log.warn("No unused token found with OTP: {}", otp);
                return ApiResponse.badRequest("Invalid OTP");
            }

            log.info("Found token for OTP: {}, Email: {}, Expires: {}", 
                otp, resetToken.getUserEmail(), resetToken.getExpiresAt());

            if (!resetToken.getUserEmail().equals(email)) {
                log.warn("OTP {} does not match email {}. Token email: {}", 
                    otp, email, resetToken.getUserEmail());
                return ApiResponse.badRequest("Invalid OTP for this email");
            }

            if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("OTP {} has expired. Expires at: {}, Current time: {}", 
                    otp, resetToken.getExpiresAt(), LocalDateTime.now());
                return ApiResponse.badRequest("OTP has expired");
            }

            log.info("OTP verification successful for email: {}", email);
            return ApiResponse.success(
                "OTP verified successfully",
                "You can now proceed to reset your password"
            );

        } catch (Exception e) {
            log.error("Error verifying OTP for email: {}", email, e);
            return ApiResponse.badRequest("Failed to verify OTP");
        }
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates 6-digit number
        return String.valueOf(otp);
    }

    private String getClientIPAddress() {
        // You can implement this method to get the actual client IP
        // For now, return a placeholder
        return "192.168.1.100";
    }
}
