#!/bin/bash

echo "Adding 50 users with working script..."

created=0
failed=0

# Get current timestamp for unique data
timestamp=$(date +%s)

for i in {1..50}; do
    echo "Creating user $i..."
    
    # Create user with completely unique data using timestamp and index
    response=$(curl -s -X POST http://localhost:8060/api/v1/users \
        -H "Content-Type: application/json" \
        -d "{
            \"firstName\": \"User${timestamp}_$i\",
            \"lastName\": \"Test${timestamp}_$i\",
            \"email\": \"user${timestamp}_$i@example.com\",
            \"password\": \"password123\",
            \"phoneNumber\": \"+1${timestamp}${i}\",
            \"gender\": \"MALE\",
            \"birthDate\": \"1990-01-01\",
            \"role\": \"USER\",
            \"status\": \"ACTIVE\"
        }")
    
    if echo "$response" | grep -q '"status":"success"'; then
        created=$((created + 1))
        echo "✅ User $i created successfully"
    else
        failed=$((failed + 1))
        echo "❌ User $i failed"
        echo "Response: $response"
    fi
    
    sleep 0.1
done

echo ""
echo "=========================================="
echo "User creation completed!"
echo "Created: $created"
echo "Failed: $failed"
echo "Total attempted: $((created + failed))"
echo "=========================================="

# Verify by getting user count
echo ""
echo "Verifying final user count..."
totalUsers=$(curl -s -X GET "http://localhost:8060/api/v1/users/paginated?page=0&size=1" | grep -o '"totalElements":[0-9]*' | cut -d: -f2)
echo "Total users in database: $totalUsers"

# Show some sample users
echo ""
echo "Sample of created users:"
curl -s -X GET "http://localhost:8060/api/v1/users/paginated?page=0&size=3" | jq '.data.content[] | {firstName, lastName, email, phoneNumber, gender, role, status}'
