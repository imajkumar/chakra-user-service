package com.bellpatra.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private String status;
    private int statusCode;
    private String message;
    private T data;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    
    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .statusCode(200)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .statusCode(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .status("success")
                .statusCode(200)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Error responses
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(statusCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, int statusCode, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(statusCode)
                .message(message)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Created responses
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .statusCode(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Not found responses
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(404)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Bad request responses
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Unauthorized responses
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(401)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Forbidden responses
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(403)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // Internal server error responses
    public static <T> ApiResponse<T> internalError(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .statusCode(500)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
