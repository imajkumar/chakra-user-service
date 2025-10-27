#!/bin/bash

echo "Adding 1000 users with unique data..."

created=0
failed=0

# Arrays for random data
firstNames=("John" "Jane" "Michael" "Sarah" "David" "Emily" "James" "Jessica" "Robert" "Ashley" "William" "Amanda" "Richard" "Jennifer" "Charles" "Lisa" "Joseph" "Nancy" "Thomas" "Karen" "Christopher" "Betty" "Daniel" "Helen" "Paul" "Sandra" "Mark" "Donna" "Donald" "Carol" "George" "Ruth" "Kenneth" "Sharon" "Steven" "Michelle" "Edward" "Laura" "Brian" "Sarah" "Ronald" "Kimberly" "Anthony" "Deborah" "Kevin" "Dorothy" "Jason" "Lisa" "Matthew" "Nancy" "Gary" "Karen" "Timothy" "Betty" "Jose" "Helen" "Larry" "Sandra" "Jeffrey" "Donna" "Frank" "Carol" "Scott" "Ruth" "Eric" "Sharon" "Stephen" "Michelle" "Andrew" "Laura" "Raymond" "Sarah" "Gregory" "Kimberly" "Joshua" "Deborah" "Jerry" "Dorothy" "Dennis" "Lisa" "Walter" "Nancy" "Patrick" "Karen" "Peter" "Betty" "Harold" "Helen" "Douglas" "Sandra" "Henry" "Donna" "Carl" "Carol" "Arthur" "Ruth" "Ryan" "Sharon" "Roger" "Michelle")

lastNames=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez" "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson" "Martin" "Lee" "Perez" "Thompson" "White" "Harris" "Sanchez" "Clark" "Ramirez" "Lewis" "Robinson" "Walker" "Young" "Allen" "King" "Wright" "Scott" "Torres" "Nguyen" "Hill" "Flores" "Green" "Adams" "Nelson" "Baker" "Hall" "Rivera" "Campbell" "Mitchell" "Carter" "Roberts" "Gomez" "Phillips" "Evans" "Turner" "Diaz" "Parker" "Cruz" "Edwards" "Collins" "Reyes" "Stewart" "Morris" "Morales" "Murphy" "Cook" "Rogers" "Gutierrez" "Ortiz" "Morgan" "Cooper" "Peterson" "Bailey" "Reed" "Kelly" "Howard" "Ramos" "Kim" "Cox" "Ward" "Richardson" "Watson" "Brooks" "Chavez" "Wood" "James" "Bennett" "Gray" "Mendoza" "Ruiz" "Hughes" "Price" "Alvarez" "Castillo" "Sanders" "Patel" "Myers" "Long" "Ross" "Foster" "Jimenez")

genders=("MALE" "FEMALE" "OTHER" "PREFER_NOT_TO_SAY")
roles=("USER" "USER" "USER" "MANAGER" "ADMIN")
statuses=("ACTIVE" "ACTIVE" "ACTIVE" "INACTIVE" "SUSPENDED")
domains=("gmail.com" "yahoo.com" "hotmail.com" "outlook.com" "example.com" "test.com" "demo.com" "sample.com")
phonePrefixes=("+1" "+44" "+33" "+49" "+91" "+86" "+81" "+55" "+61" "+7")

# Get current timestamp for unique data
timestamp=$(date +%s)

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
    local prefix=$(get_random "${phonePrefixes[@]}")
    local number=""
    for i in {1..10}; do
        number+=$((RANDOM % 10))
    done
    echo "${prefix}${number}"
}

echo "Creating 1000 users with random realistic data..."

for i in {1..1000}; do
    # Generate random data
    firstName=$(get_random "${firstNames[@]}")
    lastName=$(get_random "${lastNames[@]}")
    gender=$(get_random "${genders[@]}")
    role=$(get_random "${roles[@]}")
    status=$(get_random "${statuses[@]}")
    domain=$(get_random "${domains[@]}")
    birthDate=$(get_random_birth_date)
    phoneNumber=$(get_random_phone)
    
    # Create unique email and names with timestamp and index
    email=$(echo "${firstName,,}.${lastName,,}${timestamp}_$i@${domain}")
    uniqueFirstName="${firstName}${timestamp}_$i"
    uniqueLastName="${lastName}${timestamp}_$i"
    
    # Create user JSON
    userJson=$(cat <<EOF
{
    "firstName": "${uniqueFirstName}",
    "lastName": "${uniqueLastName}",
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

    if [ $((i % 50)) -eq 0 ]; then
        echo "Progress: $i/1000 users processed..."
    fi
    
    # Make the API call
    response=$(curl -s -X POST http://localhost:8060/api/v1/users \
        -H "Content-Type: application/json" \
        -d "$userJson")
    
    # Check if the response contains success
    if echo "$response" | grep -q '"status":"success"'; then
        created=$((created + 1))
        if [ $((i % 100)) -eq 0 ]; then
            echo "✅ Created $i users so far..."
        fi
    else
        failed=$((failed + 1))
        if [ $failed -le 10 ]; then  # Only show first 10 failures
            echo "❌ User $i failed: $(echo "$response" | grep -o '"message":"[^"]*"' | head -1)"
        fi
    fi
    
    # Small delay to avoid overwhelming the server
    sleep 0.05
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
