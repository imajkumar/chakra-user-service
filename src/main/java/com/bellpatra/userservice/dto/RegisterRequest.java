package com.bellpatra.userservice.dto;

import com.bellpatra.userservice.entity.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private User.UserRole role = User.UserRole.USER;
}
