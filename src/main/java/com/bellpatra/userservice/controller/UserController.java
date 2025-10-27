package com.bellpatra.userservice.controller;

import com.bellpatra.userservice.dto.ApiResponse;
import com.bellpatra.userservice.dto.PagedResponse;
import com.bellpatra.userservice.dto.UserDTO;
import com.bellpatra.userservice.dto.UserSearchRequest;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> metadata = Map.of(
                    "totalCount", users.size(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully", metadata));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            PagedResponse<UserDTO> users = userService.getAllUsersPaginated(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable UUID id) {
        try {
            return userService.getUserById(id)
                    .map(user -> ResponseEntity.ok(ApiResponse.success(convertToDTO(user), "User retrieved successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.notFound("User not found with id: " + id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve user: " + e.getMessage()));
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        try {
            return userService.getUserByEmail(email)
                    .map(user -> ResponseEntity.ok(ApiResponse.success(convertToDTO(user), "User retrieved successfully")))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.notFound("User not found with email: " + email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve user: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            UserDTO userDTO = convertToDTO(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(userDTO, "User created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to create user: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedUser), "User updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Failed to update user: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Failed to delete user: " + e.getMessage()));
        }
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable User.UserRole role) {
        try {
            List<User> users = userService.getUsersByRole(role);
            Map<String, Object> metadata = Map.of(
                    "role", role.toString(),
                    "totalCount", users.size(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved by role successfully", metadata));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users by role: " + e.getMessage()));
        }
    }
    
    @GetMapping("/role/{role}/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getUsersByRolePaginated(
            @PathVariable User.UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            PagedResponse<UserDTO> users = userService.getUsersByRole(role, page, size, sortBy, sortDirection);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved by role successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users by role: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByStatus(@PathVariable User.UserStatus status) {
        try {
            List<User> users = userService.getUsersByStatus(status);
            Map<String, Object> metadata = Map.of(
                    "status", status.toString(),
                    "totalCount", users.size(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved by status successfully", metadata));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users by status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getUsersByStatusPaginated(
            @PathVariable User.UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            PagedResponse<UserDTO> users = userService.getUsersByStatus(status, page, size, sortBy, sortDirection);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved by status successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users by status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/gender/{gender}/paginated")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getUsersByGenderPaginated(
            @PathVariable User.Gender gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            PagedResponse<UserDTO> users = userService.getUsersByGender(gender, page, size, sortBy, sortDirection);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved by gender successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve users by gender: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsersByName(@RequestParam String name) {
        try {
            List<User> users = userService.searchUsersByName(name);
            Map<String, Object> metadata = Map.of(
                    "searchTerm", name,
                    "totalCount", users.size(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(ApiResponse.success(users, "Users searched successfully", metadata));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to search users: " + e.getMessage()));
        }
    }
    
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> searchUsers(@RequestBody UserSearchRequest searchRequest) {
        try {
            PagedResponse<UserDTO> users = userService.searchUsers(searchRequest);
            return ResponseEntity.ok(ApiResponse.success(users, "Users searched successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to search users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> searchUsersAdvanced(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) User.Gender gender,
            @RequestParam(required = false) String birthDateFrom,
            @RequestParam(required = false) String birthDateTo,
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(required = false) User.UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            UserSearchRequest searchRequest = UserSearchRequest.builder()
                    .searchTerm(searchTerm)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .gender(gender)
                    .birthDateFrom(birthDateFrom != null ? java.time.LocalDate.parse(birthDateFrom) : null)
                    .birthDateTo(birthDateTo != null ? java.time.LocalDate.parse(birthDateTo) : null)
                    .role(role)
                    .status(status)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            PagedResponse<UserDTO> users = userService.searchUsers(searchRequest);
            return ResponseEntity.ok(ApiResponse.success(users, "Users searched successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to search users: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/last-login")
    public ResponseEntity<ApiResponse<String>> updateLastLogin(@PathVariable UUID id) {
        try {
            userService.updateLastLogin(id);
            return ResponseEntity.ok(ApiResponse.success("Last login updated successfully", "Last login updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("Failed to update last login: " + e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        try {
            Map<String, Object> statistics = Map.of(
                    "totalUsers", userService.getAllUsers().size(),
                    "activeUsers", userService.getUsersByStatus(User.UserStatus.ACTIVE).size(),
                    "inactiveUsers", userService.getUsersByStatus(User.UserStatus.INACTIVE).size(),
                    "suspendedUsers", userService.getUsersByStatus(User.UserStatus.SUSPENDED).size(),
                    "adminUsers", userService.getUsersByRole(User.UserRole.ADMIN).size(),
                    "regularUsers", userService.getUsersByRole(User.UserRole.USER).size(),
                    "managerUsers", userService.getUsersByRole(User.UserRole.MANAGER).size(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(ApiResponse.success(statistics, "User statistics retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to retrieve user statistics: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = Map.of(
                "service", "User Service",
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(ApiResponse.success(healthData, "User service is running"));
    }
    
    @PostMapping("/seed-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedTestUsers() {
        try {
            // Create 5 test users
            for (int i = 1; i <= 5; i++) {
                User user = new User();
                user.setFirstName("Test" + i);
                user.setLastName("User" + i);
                user.setEmail("test" + i + "@example.com");
                user.setPassword("password123");
                user.setPhoneNumber("+123456789" + i);
                user.setGender(User.Gender.MALE);
                user.setRole(User.UserRole.USER);
                user.setStatus(User.UserStatus.ACTIVE);
                
                userService.createUser(user);
            }
            
            Map<String, Object> result = Map.of(
                "message", "5 test users created successfully",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(ApiResponse.success(result, "Test users created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to create test users: " + e.getMessage()));
        }
    }
    
    @PostMapping("/seed-users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedUsers(
            @RequestParam(defaultValue = "100") int count) {
        try {
            int created = 0;
            int skipped = 0;
            
            // Sample data arrays
            String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emily", "James", "Jessica", "Robert", "Ashley",
                "William", "Amanda", "Richard", "Jennifer", "Charles", "Lisa", "Joseph", "Nancy", "Thomas", "Karen"};
            String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};
            User.Gender[] genders = {User.Gender.MALE, User.Gender.FEMALE, User.Gender.OTHER, User.Gender.PREFER_NOT_TO_SAY};
            User.UserRole[] roles = {User.UserRole.USER, User.UserRole.USER, User.UserRole.USER, User.UserRole.MANAGER, User.UserRole.ADMIN};
            User.UserStatus[] statuses = {User.UserStatus.ACTIVE, User.UserStatus.ACTIVE, User.UserStatus.ACTIVE, User.UserStatus.INACTIVE, User.UserStatus.SUSPENDED};
            
            for (int i = 0; i < count; i++) {
                try {
                    User user = new User();
                    user.setFirstName(firstNames[i % firstNames.length] + (i + 1));
                    user.setLastName(lastNames[i % lastNames.length] + (i + 1));
                    user.setEmail("user" + (i + 1) + "@example.com");
                    user.setPassword("password123");
                    user.setPhoneNumber("+123456789" + String.format("%03d", i + 1));
                    user.setGender(genders[i % genders.length]);
                    user.setRole(roles[i % roles.length]);
                    user.setStatus(statuses[i % statuses.length]);
                    user.setBirthDate(java.time.LocalDate.of(1990 + (i % 30), 1 + (i % 12), 1 + (i % 28)));
                    
                    userService.createUser(user);
                    created++;
                } catch (Exception e) {
                    skipped++;
                    // Continue with next user
                }
            }
            
            Map<String, Object> result = Map.of(
                "message", "Users seeded successfully",
                "created", created,
                "skipped", skipped,
                "requested", count,
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(ApiResponse.success(result, "Seeder completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("Failed to seed users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/seed-users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedUsersGet(
            @RequestParam(defaultValue = "100") int count) {
        return seedUsers(count);
    }
    
    // Helper method to convert User to UserDTO
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .lastLogin(user.getLastLogin())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
