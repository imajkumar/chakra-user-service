package com.bellpatra.userservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishUserEvent(String eventType, Map<String, Object> data) {
        try {
            data.put("eventType", eventType);
            data.put("timestamp", System.currentTimeMillis());
            
            String message = objectMapper.writeValueAsString(data);
            kafkaTemplate.send("user-events", message);
            
            logger.info("Published user event {}: {}", eventType, data.get("userId"));
        } catch (Exception e) {
            logger.error("Error publishing user event: {}", e.getMessage(), e);
        }
    }

    public void publishUserLogin(String userId, String email, String ipAddress) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "ipAddress", ipAddress,
            "action", "LOGIN"
        );
        publishUserEvent("USER_LOGIN", data);
    }

    public void publishUserLogout(String userId, String email) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "action", "LOGOUT"
        );
        publishUserEvent("USER_LOGOUT", data);
    }

    public void publishUserRegistration(String userId, String email, String firstName, String lastName) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "firstName", firstName,
            "lastName", lastName,
            "action", "REGISTRATION"
        );
        publishUserEvent("USER_REGISTRATION", data);
    }

    public void publishPasswordChange(String userId, String email) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "action", "PASSWORD_CHANGE"
        );
        publishUserEvent("PASSWORD_CHANGE", data);
    }

    public void publishProfileUpdate(String userId, String email, Map<String, Object> changes) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "changes", changes,
            "action", "PROFILE_UPDATE"
        );
        publishUserEvent("PROFILE_UPDATE", data);
    }
}
