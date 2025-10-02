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

    public void sendSimpleEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send simple email", e);
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
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("deviceInfo", deviceInfo);
            context.setVariable("loginTime", java.time.LocalDateTime.now());
            context.setVariable("dashboardUrl", "http://localhost:8060/dashboard");

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

    public void sendPasswordResetEmail(User user, String otp, String ipAddress) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("🔐 Password Reset OTP - ChakraERP");

            // Prepare the context for Thymeleaf template
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("otp", otp);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("requestTime", java.time.LocalDateTime.now());
            context.setVariable("expiresAt", java.time.LocalDateTime.now().plusMinutes(10));

            // Process the template
            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordChangeEmail(User user, String ipAddress, String deviceInfo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ajayit2020@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("✅ Password Changed Successfully - ChakraERP Security Alert");

            // Prepare the context for Thymeleaf template
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("deviceInfo", deviceInfo);
            context.setVariable("changeTime", java.time.LocalDateTime.now());
            context.setVariable("loginUrl", "http://localhost:8060/login");

            // Process the template
            String htmlContent = templateEngine.process("password-change-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password change email sent successfully to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send password change email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password change email", e);
        }
    }
}
