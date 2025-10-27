#!/bin/bash

echo "Adding 100 users with completely unique data..."

created=0
failed=0

# Get current timestamp for unique data
timestamp=$(date +%s)

for i in {1..100}; do
    echo "Creating user $i..."
    
    # Create user with completely unique data
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
        echo "✅ User $i created"
    else
        failed=$((failed + 1))
        echo "❌ User $i failed"
        echo "Response: $response"
    fi
    
    sleep 0.1
done

echo ""
echo "Created: $created"
echo "Failed: $failed"

# Check final count
echo ""
echo "Checking final user count..."
curl -s -X GET "http://localhost:8060/api/v1/users/paginated?page=0&size=1" | grep -o '"totalElements":[0-9]*'
