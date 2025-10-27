# User Service API Documentation

## Overview
This document describes the REST API endpoints for the User Service with comprehensive CRUD operations, search functionality, and pagination support.

## Base URL
```
http://localhost:8080/api/v1/users
```

## User Entity Fields
- `id` (UUID) - Primary key
- `firstName` (String) - User's first name
- `lastName` (String) - User's last name
- `email` (String) - User's email (unique)
- `phoneNumber` (String) - User's phone number (unique, optional)
- `gender` (Enum) - User's gender (MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY)
- `birthDate` (LocalDate) - User's birth date (optional)
- `lastLogin` (LocalDateTime) - Last login timestamp (optional)
- `role` (Enum) - User role (ADMIN, USER, MANAGER)
- `status` (Enum) - User status (ACTIVE, INACTIVE, SUSPENDED)
- `createdAt` (LocalDateTime) - Account creation timestamp
- `updatedAt` (LocalDateTime) - Last update timestamp

## API Endpoints

### 1. Get All Users
**GET** `/api/v1/users`

Returns all users without pagination.

**Response:**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [...],
  "metadata": {
    "totalCount": 10,
    "timestamp": 1234567890
  }
}
```

### 2. Get All Users (Paginated)
**GET** `/api/v1/users/paginated`

Returns paginated list of users.

**Query Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 10) - Page size
- `sortBy` (String, default: "createdAt") - Sort field
- `sortDirection` (String, default: "desc") - Sort direction (asc/desc)

**Example:**
```
GET /api/v1/users/paginated?page=0&size=5&sortBy=firstName&sortDirection=asc
```

**Response:**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [...],
    "page": 0,
    "size": 5,
    "totalElements": 25,
    "totalPages": 5,
    "first": true,
    "last": false,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 3. Get User by ID
**GET** `/api/v1/users/{id}`

Returns a specific user by UUID.

**Path Parameters:**
- `id` (UUID) - User ID

### 4. Get User by Email
**GET** `/api/v1/users/email/{email}`

Returns a user by email address.

**Path Parameters:**
- `email` (String) - User's email

### 5. Create User
**POST** `/api/v1/users`

Creates a new user.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "phoneNumber": "+1234567890",
  "gender": "MALE",
  "birthDate": "1990-01-15",
  "role": "USER",
  "status": "ACTIVE"
}
```

### 6. Update User
**PUT** `/api/v1/users/{id}`

Updates an existing user.

**Path Parameters:**
- `id` (UUID) - User ID

**Request Body:** Same as create user

### 7. Delete User
**DELETE** `/api/v1/users/{id}`

Deletes a user by ID.

**Path Parameters:**
- `id` (UUID) - User ID

### 8. Get Users by Role
**GET** `/api/v1/users/role/{role}`

Returns all users with a specific role.

**Path Parameters:**
- `role` (String) - User role (ADMIN, USER, MANAGER)

### 9. Get Users by Role (Paginated)
**GET** `/api/v1/users/role/{role}/paginated`

Returns paginated users by role.

**Path Parameters:**
- `role` (String) - User role

**Query Parameters:** Same as paginated users

### 10. Get Users by Status
**GET** `/api/v1/users/status/{status}`

Returns all users with a specific status.

**Path Parameters:**
- `status` (String) - User status (ACTIVE, INACTIVE, SUSPENDED)

### 11. Get Users by Status (Paginated)
**GET** `/api/v1/users/status/{status}/paginated`

Returns paginated users by status.

**Path Parameters:**
- `status` (String) - User status

**Query Parameters:** Same as paginated users

### 12. Get Users by Gender (Paginated)
**GET** `/api/v1/users/gender/{gender}/paginated`

Returns paginated users by gender.

**Path Parameters:**
- `gender` (String) - User gender (MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY)

**Query Parameters:** Same as paginated users

### 13. Search Users by Name
**GET** `/api/v1/users/search`

Simple search by name (first name or last name).

**Query Parameters:**
- `name` (String) - Search term

### 14. Advanced Search (POST)
**POST** `/api/v1/users/search`

Advanced search with multiple filters using request body.

**Request Body:**
```json
{
  "searchTerm": "john",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "1234567890",
  "gender": "MALE",
  "birthDateFrom": "1990-01-01",
  "birthDateTo": "2000-12-31",
  "role": "USER",
  "status": "ACTIVE",
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

### 15. Advanced Search (GET)
**GET** `/api/v1/users/search/advanced`

Advanced search with query parameters.

**Query Parameters:**
- `searchTerm` (String, optional) - General search term
- `firstName` (String, optional) - First name filter
- `lastName` (String, optional) - Last name filter
- `email` (String, optional) - Email filter
- `phoneNumber` (String, optional) - Phone number filter
- `gender` (String, optional) - Gender filter
- `birthDateFrom` (String, optional) - Birth date from (YYYY-MM-DD)
- `birthDateTo` (String, optional) - Birth date to (YYYY-MM-DD)
- `role` (String, optional) - Role filter
- `status` (String, optional) - Status filter
- `page` (int, default: 0) - Page number
- `size` (int, default: 10) - Page size
- `sortBy` (String, default: "createdAt") - Sort field
- `sortDirection` (String, default: "desc") - Sort direction

**Example:**
```
GET /api/v1/users/search/advanced?searchTerm=john&gender=MALE&role=USER&page=0&size=5
```

### 16. Update Last Login
**PUT** `/api/v1/users/{id}/last-login`

Updates the last login timestamp for a user.

**Path Parameters:**
- `id` (UUID) - User ID

### 17. Get User Statistics
**GET** `/api/v1/users/statistics`

Returns user statistics.

**Response:**
```json
{
  "success": true,
  "message": "User statistics retrieved successfully",
  "data": {
    "totalUsers": 100,
    "activeUsers": 85,
    "inactiveUsers": 10,
    "suspendedUsers": 5,
    "adminUsers": 3,
    "regularUsers": 90,
    "managerUsers": 7,
    "timestamp": 1234567890
  }
}
```

### 18. Health Check
**GET** `/api/v1/users/health`

Returns service health status.

## Error Responses

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "metadata": null
}
```

**Common HTTP Status Codes:**
- `200 OK` - Success
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Examples

### Create a new user:
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "password": "securePassword123",
    "phoneNumber": "+1987654321",
    "gender": "FEMALE",
    "birthDate": "1995-05-20",
    "role": "USER",
    "status": "ACTIVE"
  }'
```

### Search users with pagination:
```bash
curl -X GET "http://localhost:8080/api/v1/users/search/advanced?searchTerm=john&page=0&size=5&sortBy=firstName&sortDirection=asc"
```

### Get paginated users by role:
```bash
curl -X GET "http://localhost:8080/api/v1/users/role/USER/paginated?page=0&size=10"
```
