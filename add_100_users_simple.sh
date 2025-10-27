#!/bin/bash

echo "Adding 100 users (simple version)..."

created=0
failed=0

for i in {1..100}; do
    echo "Creating user $i..."
    
    # Create user with sequential data
    response=$(curl -s -X POST http://localhost:8060/api/v1/users \
        -H "Content-Type: application/json" \
        -d "{
            \"firstName\": \"User$i\",
            \"lastName\": \"Test$i\",
            \"email\": \"user$i@example.com\",
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
    fi
    
    sleep 0.1
done

echo ""
echo "Created: $created"
echo "Failed: $failed"
