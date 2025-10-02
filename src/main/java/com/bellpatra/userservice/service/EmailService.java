package com.bellpatra.userservice.service;

import com.bellpatra.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendWelcomeEmail(User user) {
        sendWelcomeEmail(user, "http://localhost:8060/login");
    }

    public void sendWelcomeEmail(User user, String loginUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("🎉 Welcome to ChakraERP - Your Account is Ready!");

            // Prepare the context for Thymeleaf template
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("appName", "ChakraERP");
            context.setVariable("supportEmail", "support@chakraerp.com");
            context.setVariable("loginUrl", loginUrl);

            // Process the template
            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("🔐 Password Reset Request - ChakraERP");

            String htmlContent = buildPasswordResetEmail(user, resetToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendAccountStatusChangeEmail(User user, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("📢 Account Status Update - ChakraERP");

            String htmlContent = buildAccountStatusChangeEmail(user, status);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Account status change email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send account status change email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send account status change email", e);
        }
    }

    public void sendLoginSuccessEmail(User user, String ipAddress, String deviceInfo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("🔐 Login Successful - ChakraERP Security Alert");

            // Prepare the context for Thymeleaf template
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("loginTime", java.time.LocalDateTime.now());
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("deviceInfo", deviceInfo);
            context.setVariable("appName", "ChakraERP");
            context.setVariable("supportEmail", "support@chakraerp.com");

            // Process the template
            String htmlContent = templateEngine.process("login-success-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Login success email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send login success email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send login success email", e);
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
