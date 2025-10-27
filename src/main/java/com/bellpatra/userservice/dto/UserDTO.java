package com.bellpatra.userservice.dto;

import com.bellpatra.userservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private User.Gender gender;
    private LocalDate birthDate;
    private LocalDateTime lastLogin;
    private User.UserRole role;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Exclude password for security
    // private String password;
}
