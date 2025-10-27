package com.bellpatra.userservice.service;

import com.bellpatra.userservice.dto.PagedResponse;
import com.bellpatra.userservice.dto.UserDTO;
import com.bellpatra.userservice.dto.UserSearchRequest;
import com.bellpatra.userservice.entity.User;
import com.bellpatra.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        if (user.getPhoneNumber() != null && userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new RuntimeException("User with phone number " + user.getPhoneNumber() + " already exists");
        }
        return userRepository.save(user);
    }
    
    public User updateUser(UUID id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setGender(userDetails.getGender());
        user.setBirthDate(userDetails.getBirthDate());
        user.setRole(userDetails.getRole());
        user.setStatus(userDetails.getStatus());
        
        return userRepository.save(user);
    }
    
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> getUsersByStatus(User.UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    // New pagination and search methods
    public PagedResponse<UserDTO> getAllUsersPaginated(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);
        
        return buildPagedResponse(userPage);
    }
    
    public PagedResponse<UserDTO> searchUsers(UserSearchRequest searchRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.getSortDirection()), searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
        
        Page<User> userPage = userRepository.findUsersWithFilters(
                searchRequest.getSearchTerm(),
                searchRequest.getFirstName(),
                searchRequest.getLastName(),
                searchRequest.getEmail(),
                searchRequest.getPhoneNumber(),
                searchRequest.getGender(),
                searchRequest.getBirthDateFrom(),
                searchRequest.getBirthDateTo(),
                searchRequest.getRole(),
                searchRequest.getStatus(),
                pageable
        );
        
        return buildPagedResponse(userPage);
    }
    
    public PagedResponse<UserDTO> getUsersByRole(User.UserRole role, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findByRole(role, pageable);
        
        return buildPagedResponse(userPage);
    }
    
    public PagedResponse<UserDTO> getUsersByStatus(User.UserStatus status, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findByStatus(status, pageable);
        
        return buildPagedResponse(userPage);
    }
    
    public PagedResponse<UserDTO> getUsersByGender(User.Gender gender, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findByGender(gender, pageable);
        
        return buildPagedResponse(userPage);
    }
    
    public void updateLastLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
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
    
    // Helper method to build PagedResponse
    private PagedResponse<UserDTO> buildPagedResponse(Page<User> userPage) {
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponse.<UserDTO>builder()
                .content(userDTOs)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }
}
