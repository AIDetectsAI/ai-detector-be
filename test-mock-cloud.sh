#!/bin/bash

# Quick test for Mock Cloud Storage Integration

BASE_URL="http://localhost:8081"
TEST_USER="cloudtest$(date +%s)"
TEST_EMAIL="${TEST_USER}@example.com"
PASSWORD="TestPass123!"
IMAGE_PATH="/tmp/test-image.jpg"

echo "=== Mock Cloud Storage Test ==="
echo ""

# 1. Register
echo "1. Registering user: $TEST_USER"
REG_RESP=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"$TEST_USER\",\"password\":\"$PASSWORD\",\"email\":\"$TEST_EMAIL\"}")

echo "Registration: $REG_RESP"
echo ""

# 2. Login
echo "2. Logging in..."
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"$TEST_USER\",\"password\":\"$PASSWORD\",\"email\":\"$TEST_EMAIL\"}")

TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"token":"\K[^"]+')

if [ -z "$TOKEN" ]; then
    echo "ERROR: Failed to get token"
    echo "$LOGIN_RESP"
    exit 1
fi

echo "✓ Got token: ${TOKEN:0:30}..."
echo ""

# 3. Test /useModel endpoint
echo "3. Uploading image to /useModel..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/useModel" \
  -H "Authorization: Bearer $TOKEN" \
  -F "image=@$IMAGE_PATH")

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# 4. Check for imageUrl in response
IMAGE_URL=$(echo "$RESPONSE" | grep -oP '"imageUrl":"\K[^"]+')

if [ -n "$IMAGE_URL" ]; then
    echo "✅ SUCCESS! Mock Cloud Storage is working!"
    echo "   Image URL: $IMAGE_URL"
    echo ""
    echo "Expected format: https://mock-cloud-storage.example.com/images/<UUID>.jpg"
else
    echo "❌ FAILED: No imageUrl in response"
fi
