# User Service - ChakraERP Backend

A Spring Boot microservice for user management with JWT authentication, PostgreSQL database, and RESTful APIs.

## 🚀 Features

- **User Management**: CRUD operations for users
- **JWT Authentication**: Secure login/register with access and refresh tokens
- **PostgreSQL Integration**: Persistent data storage with JPA/Hibernate
- **RESTful APIs**: Well-structured REST endpoints
- **Security**: Spring Security with JWT token validation
- **Password Encryption**: BCrypt password hashing
- **Role-based Access**: User roles (ADMIN, USER, MANAGER)
- **Refresh Token Support**: Secure token refresh mechanism
- **Email Notifications**: Beautiful HTML email templates with Gmail SMTP
- **Welcome Emails**: Automatic welcome emails sent after registration
- **Login Success Notifications**: Security alerts sent after successful login
- **Email Queue System**: Asynchronous email processing with Quartz scheduler
- **UUID Support**: User IDs using UUID instead of auto-incrementing integers

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 4.0.0-M3**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JSON Web Tokens)**
- **Maven**
- **Lombok**

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd user-service
```

### 2. Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE user_service_db;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE user_service_db TO postgres;
```

### 3. Configuration
Update `src/main/resources/application.properties` if needed:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/user_service_db
spring.datasource.username=postgres
spring.datasource.password=password

# Server Configuration
server.port=8060

# JWT Configuration
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 4. Run the Application
```bash
./mvnw spring-boot:run
```

The application will start on port **8060**.

## 📚 API Documentation

### Standard API Response Format

All API responses follow a consistent structure:

```json
{
  "status": "success|error",
  "statusCode": 200,
  "message": "Operation completed successfully",
  "data": { ... },
  "metadata": {
    "totalCount": 10,
    "timestamp": 1759395182584
  },
  "timestamp": "2025-10-02T14:23:02.584665"
}
```

**Response Fields:**
- `status`: "success" or "error"
- `statusCode`: HTTP status code (200, 201, 400, 401, 403, 404, 500, etc.)
- `message`: Human-readable message describing the operation result
- `data`: The actual response data (varies by endpoint)
- `metadata`: Additional information (counts, timestamps, search terms, etc.)
- `timestamp`: ISO timestamp of the response

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "USER"
}
```

**Success Response:**
```json
{
  "status": "success",
  "statusCode": 201,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "email": "user@example.com",
    "role": "USER",
    "user": {
      "id": "bca85c1e-e464-4ba9-a6af-60885a9fb51d",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phoneNumber": "+1234567890",
      "role": "USER",
      "status": "ACTIVE",
      "createdAt": "2025-10-02T14:24:40.13267",
      "updatedAt": "2025-10-02T14:24:40.132753"
    }
  },
  "timestamp": "2025-10-02T14:23:02.584665"
}
```

**Error Response:**
```json
{
  "status": "error",
  "statusCode": 400,
  "message": "Registration failed: User with email user@example.com already exists",
  "timestamp": "2025-10-02T14:23:11.052978"
}
```

#### Login User
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Logout
```http
POST /api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### User Management Endpoints

#### Get All Users
```http
GET /api/v1/users
Authorization: Bearer <access_token>
```

#### Get User by ID
```http
GET /api/v1/users/{uuid}
Authorization: Bearer <access_token>
```

**Example:**
```http
GET /api/v1/users/bca85c1e-e464-4ba9-a6af-60885a9fb51d
Authorization: Bearer <access_token>
```

#### Get User by Email
```http
GET /api/v1/users/email/{email}
Authorization: Bearer <access_token>
```

#### Create User
```http
POST /api/v1/users
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "email": "newuser@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "password": "password123",
  "phoneNumber": "+1234567890",
  "role": "USER"
}
```

#### Update User
```http
PUT /api/v1/users/{uuid}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "firstName": "Updated Name",
  "lastName": "Updated LastName",
  "email": "updated@example.com",
  "phoneNumber": "+9876543210",
  "role": "MANAGER",
  "status": "ACTIVE"
}
```

**Example:**
```http
PUT /api/v1/users/bca85c1e-e464-4ba9-a6af-60885a9fb51d
Authorization: Bearer <access_token>
Content-Type: application/json
```

#### Delete User
```http
DELETE /api/v1/users/{uuid}
Authorization: Bearer <access_token>
```

**Example:**
```http
DELETE /api/v1/users/bca85c1e-e464-4ba9-a6af-60885a9fb51d
Authorization: Bearer <access_token>
```

#### Search Users by Name
```http
GET /api/v1/users/search?name=John
Authorization: Bearer <access_token>
```

#### Get Users by Role
```http
GET /api/v1/users/role/{role}
Authorization: Bearer <access_token>
```

#### Get Users by Status
```http
GET /api/v1/users/status/{status}
Authorization: Bearer <access_token>
```

### Health Check
```http
GET /api/v1/users/health
GET /api/v1/auth/health
```

### Email Queue Management

#### Get Email Queue Statistics
```http
GET /api/v1/email-queue/stats
```

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Email queue statistics retrieved successfully",
  "data": {
    "pendingEmails": 0,
    "failedEmails": 0,
    "timestamp": 1759406907335
  },
  "timestamp": "2025-10-02T10:30:00.123456"
}
```

#### Process Pending Emails
```http
POST /api/v1/email-queue/process
```

#### Cleanup Old Emails
```http
POST /api/v1/email-queue/cleanup
```

#### Email Queue Health Check
```http
GET /api/v1/email-queue/health
```

## 🔐 Security

### JWT Token Structure
- **Access Token**: Short-lived (24 hours), used for API authentication
- **Refresh Token**: Long-lived (7 days), used to generate new access tokens
- **Token Type**: Bearer

### User Roles
- **ADMIN**: Full system access
- **MANAGER**: Management-level access
- **USER**: Standard user access

### User Status
- **ACTIVE**: User can login and access the system
- **INACTIVE**: User account is disabled
- **SUSPENDED**: User account is temporarily suspended

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
```

## 🧪 Testing

### Using cURL

#### Register a new user:
```bash
curl -X POST http://localhost:8060/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890"
  }'
```

#### Login:
```bash
curl -X POST http://localhost:8060/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Get all users (with authentication):
```bash
curl -X GET http://localhost:8060/api/v1/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 🔧 Configuration

### Environment Variables
You can override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/user_service_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=password
export SERVER_PORT=8060
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=86400000
export JWT_REFRESH_EXPIRATION=604800000
```

### Profiles
The application supports different profiles for different environments:

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🚀 Deployment

### Docker (Optional)
Create a `Dockerfile`:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8060
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
./mvnw clean package
docker build -t user-service .
docker run -p 8060:8060 user-service
```

## 📝 Development

### Project Structure
```
src/
├── main/
│   ├── java/com/bellpatra/userservice/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security components
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       └── application.properties
└── test/
    └── java/
```

### Adding New Features
1. Create entity in `entity/` package
2. Create repository in `repository/` package
3. Create service in `service/` package
4. Create controller in `controller/` package
5. Add security configuration if needed

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact the development team

## 🔄 Version History

- **v0.0.1-SNAPSHOT**: Initial release with JWT authentication and user management
