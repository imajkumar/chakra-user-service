#!/bin/bash

echo "Adding 100 users with unique emails..."

created=0
failed=0

# Get current timestamp for unique emails
timestamp=$(date +%s)

for i in {1..100}; do
    echo "Creating user $i..."
    
    # Create user with unique email using timestamp
    response=$(curl -s -X POST http://localhost:8060/api/v1/users \
        -H "Content-Type: application/json" \
        -d "{
            \"firstName\": \"User$i\",
            \"lastName\": \"Test$i\",
            \"email\": \"user${timestamp}_$i@example.com\",
            \"password\": \"password123\",
            \"phoneNumber\": \"+123456789$i\",
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
