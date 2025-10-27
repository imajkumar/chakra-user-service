package com.bellpatra.userservice.dto;

import com.bellpatra.userservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    private String searchTerm;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private User.Gender gender;
    private LocalDate birthDateFrom;
    private LocalDate birthDateTo;
    private User.UserRole role;
    private User.UserStatus status;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
    @Builder.Default
    private String sortBy = "createdAt";
    @Builder.Default
    private String sortDirection = "desc";
}
