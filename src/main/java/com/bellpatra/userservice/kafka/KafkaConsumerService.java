package com.bellpatra.userservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "notifications", groupId = "user-service-group")
    public void handleNotification(String message) {
        try {
            logger.info("Received notification: {}", message);
            
            // Parse the notification message
            Map<String, Object> notification = objectMapper.readValue(message, Map.class);
            
            // Process notification based on type
            String type = (String) notification.get("type");
            String userId = (String) notification.get("userId");
            
            switch (type) {
                case "MENTION":
                    handleMentionNotification(notification);
                    break;
                case "MESSAGE":
                    handleMessageNotification(notification);
                    break;
                case "CALL":
                    handleCallNotification(notification);
                    break;
                default:
                    logger.warn("Unknown notification type: {}", type);
            }
            
        } catch (Exception e) {
            logger.error("Error processing notification: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-presence", groupId = "user-service-group")
    public void handleUserPresence(String message) {
        try {
            logger.info("Received user presence update: {}", message);
            
            // Parse the presence message
            Map<String, Object> presence = objectMapper.readValue(message, Map.class);
            String userId = (String) presence.get("userId");
            String status = (String) presence.get("status");
            
            // Update user presence in database or cache
            logger.info("User {} is now {}", userId, status);
            
        } catch (Exception e) {
            logger.error("Error processing user presence: {}", e.getMessage(), e);
        }
    }

    private void handleMentionNotification(Map<String, Object> notification) {
        String userId = (String) notification.get("userId");
        String mentionedBy = (String) notification.get("mentionedBy");
        String content = (String) notification.get("content");
        
        logger.info("User {} was mentioned by {} in message: {}", userId, mentionedBy, content);
        
        // Here you could:
        // 1. Store notification in database
        // 2. Send email notification
        // 3. Send push notification
        // 4. Update user's notification count
    }

    private void handleMessageNotification(Map<String, Object> notification) {
        String userId = (String) notification.get("userId");
        String channelId = (String) notification.get("channelId");
        
        logger.info("New message notification for user {} in channel {}", userId, channelId);
        
        // Here you could:
        // 1. Store notification in database
        // 2. Send push notification if user is offline
        // 3. Update unread message count
    }

    private void handleCallNotification(Map<String, Object> notification) {
        String userId = (String) notification.get("userId");
        String callType = (String) notification.get("callType");
        
        logger.info("Call notification for user {}: {}", userId, callType);
        
        // Here you could:
        // 1. Store call history
        // 2. Send push notification
        // 3. Update call status
    }
}
