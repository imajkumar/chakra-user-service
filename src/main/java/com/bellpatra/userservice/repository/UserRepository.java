package com.bellpatra.userservice.repository;

import com.bellpatra.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByStatus(User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Pagination methods
    Page<User> findAll(Pageable pageable);
    
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    
    Page<User> findByGender(User.Gender gender, Pageable pageable);
    
    // Advanced search methods
    @Query("SELECT u FROM User u WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phoneNumber IS NULL OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%'))) AND " +
           "(:gender IS NULL OR u.gender = :gender) AND " +
           "(:birthDateFrom IS NULL OR u.birthDate >= :birthDateFrom) AND " +
           "(:birthDateTo IS NULL OR u.birthDate <= :birthDateTo) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber,
            @Param("gender") User.Gender gender,
            @Param("birthDateFrom") LocalDate birthDateFrom,
            @Param("birthDateTo") LocalDate birthDateTo,
            @Param("role") User.UserRole role,
            @Param("status") User.UserStatus status,
            Pageable pageable);
    
    // Count methods for statistics
    long countByRole(User.UserRole role);
    
    long countByStatus(User.UserStatus status);
    
    long countByGender(User.Gender gender);
}
