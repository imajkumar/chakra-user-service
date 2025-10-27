#!/bin/bash

echo "Testing user creation..."

# Test 1: Health check
echo "1. Testing health endpoint..."
curl -X GET http://localhost:8060/api/v1/users/health
echo -e "\n\n"

# Test 2: Simple user creation
echo "2. Testing simple user creation..."
curl -X POST http://localhost:8060/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "password123"
  }'
echo -e "\n\n"

# Test 3: User with all fields
echo "3. Testing user with all fields..."
curl -X POST http://localhost:8060/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "phoneNumber": "+1234567890",
    "gender": "MALE",
    "birthDate": "1990-01-15",
    "role": "USER",
    "status": "ACTIVE"
  }'
echo -e "\n\n"

# Test 4: Check if users were created
echo "4. Checking created users..."
curl -X GET "http://localhost:8060/api/v1/users/paginated?page=0&size=5"
echo -e "\n\n"

echo "Test completed!"
