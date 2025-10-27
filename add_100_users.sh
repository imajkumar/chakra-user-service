#!/bin/bash

echo "Starting to add 100 users..."

# Arrays for random data
firstNames=("John" "Jane" "Michael" "Sarah" "David" "Emily" "James" "Jessica" "Robert" "Ashley" "William" "Amanda" "Richard" "Jennifer" "Charles" "Lisa" "Joseph" "Nancy" "Thomas" "Karen" "Christopher" "Betty" "Daniel" "Helen" "Paul" "Sandra" "Mark" "Donna" "Donald" "Carol" "George" "Ruth" "Kenneth" "Sharon" "Steven" "Michelle" "Edward" "Laura" "Brian" "Sarah" "Ronald" "Kimberly" "Anthony" "Deborah" "Kevin" "Dorothy" "Jason" "Lisa" "Matthew" "Nancy")
lastNames=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez" "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson" "Martin" "Lee" "Perez" "Thompson" "White" "Harris" "Sanchez" "Clark" "Ramirez" "Lewis" "Robinson" "Walker" "Young" "Allen" "King" "Wright" "Scott" "Torres" "Nguyen" "Hill" "Flores" "Green" "Adams" "Nelson" "Baker" "Hall" "Rivera" "Campbell" "Mitchell" "Carter" "Roberts" "Gomez")
genders=("MALE" "FEMALE" "OTHER" "PREFER_NOT_TO_SAY")
roles=("USER" "USER" "USER" "MANAGER" "ADMIN")
statuses=("ACTIVE" "ACTIVE" "ACTIVE" "INACTIVE" "SUSPENDED")
domains=("gmail.com" "yahoo.com" "hotmail.com" "outlook.com" "example.com" "test.com")

# Counters
created=0
failed=0

# Function to get random element from array
get_random() {
    local arr=("$@")
    local len=${#arr[@]}
    echo "${arr[$((RANDOM % len))]}"
}

# Function to generate random birth date
get_random_birth_date() {
    local year=$((1990 + RANDOM % 30))
    local month=$((1 + RANDOM % 12))
    local day=$((1 + RANDOM % 28))
    printf "%04d-%02d-%02d" $year $month $day
}

# Function to generate random phone number
get_random_phone() {
    local prefixes=("+1" "+44" "+33" "+49" "+91" "+86" "+81" "+55" "+61" "+7")
    local prefix=$(get_random "${prefixes[@]}")
    local number=""
    for i in {1..10}; do
        number+=$((RANDOM % 10))
    done
    echo "${prefix}${number}"
}

echo "Creating 100 users with random data..."

for i in {1..100}; do
    # Generate random data
    firstName=$(get_random "${firstNames[@]}")
    lastName=$(get_random "${lastNames[@]}")
    gender=$(get_random "${genders[@]}")
    role=$(get_random "${roles[@]}")
    status=$(get_random "${statuses[@]}")
    domain=$(get_random "${domains[@]}")
    birthDate=$(get_random_birth_date)
    phoneNumber=$(get_random_phone)
    
    # Create unique email
    email="${firstName,,}.${lastName,,}${i}@${domain}"
    
    # Create user JSON
    userJson=$(cat <<EOF
{
    "firstName": "${firstName}${i}",
    "lastName": "${lastName}${i}",
    "email": "${email}",
    "password": "password123",
    "phoneNumber": "${phoneNumber}",
    "gender": "${gender}",
    "birthDate": "${birthDate}",
    "role": "${role}",
    "status": "${status}"
}
EOF
)

    echo "Creating user $i: ${email}"
    
    # Make the API call
    response=$(curl -s -X POST http://localhost:8060/api/v1/users \
        -H "Content-Type: application/json" \
        -d "$userJson")
    
    # Check if the response contains success
    if echo "$response" | grep -q '"success":true'; then
        created=$((created + 1))
        echo "✅ User $i created successfully"
    else
        failed=$((failed + 1))
        echo "❌ User $i failed to create"
        echo "Response: $response"
    fi
    
    # Small delay to avoid overwhelming the server
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
echo "Verifying users..."
curl -s -X GET "http://localhost:8060/api/v1/users/paginated?page=0&size=5" | jq '.data.totalElements // "Unable to get count"'
