#!/bin/bash

# OAuth2 Authorization Server Testing Script (Bash version)
# This script provides automated testing for the OAuth2 authorization server
# Usage: ./test-oauth2.sh [base_url] [client_id] [client_secret]

BASE_URL="${1:-http://localhost:8080}"
CLIENT_ID="${2:-oidc-client}"
CLIENT_SECRET="${3:-secret}"
REDIRECT_URI="http://127.0.0.1:8080/login/oauth2/code/oidc-client"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

# Base64 encode client credentials
CLIENT_CREDENTIALS=$(echo -n "${CLIENT_ID}:${CLIENT_SECRET}" | base64)

echo -e "${GREEN}=== OAuth2 Authorization Server Testing Script ===${NC}"
echo -e "${YELLOW}Base URL: $BASE_URL${NC}"
echo -e "${YELLOW}Client ID: $CLIENT_ID${NC}"
echo -e "${YELLOW}Redirect URI: $REDIRECT_URI${NC}"
echo ""

# Check if server is running
echo -e "${CYAN}Checking if server is running...${NC}"
if curl -s --head --fail "$BASE_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}"
else
    echo -e "${RED}✗ Server is not running at $BASE_URL${NC}"
    echo -e "${YELLOW}Please ensure the application is started with: ./gradlew bootRun${NC}"
    exit 1
fi
echo ""

# Function to test endpoints
test_endpoint() {
    local name="$1"
    local url="$2"
    local method="${3:-GET}"
    local headers="${4:-}"
    local body="${5:-}"
    
    echo -e "${CYAN}Testing: $name${NC}"
    echo -e "${GRAY}URL: $url${NC}"
    
    if [ "$method" = "POST" ] && [ -n "$body" ]; then
        response=$(curl -s -X "$method" "$url" \
            -H "Content-Type: application/json" \
            ${headers:+-H "$headers"} \
            -d "$body" \
            -w "HTTPSTATUS:%{http_code}")
    else
        response=$(curl -s -X "$method" "$url" \
            ${headers:+-H "$headers"} \
            -w "HTTPSTATUS:%{http_code}")
    fi
    
    http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ Success (HTTP $http_code)${NC}"
        if [ "$VERBOSE" = "true" ]; then
            echo "$body" | jq . 2>/dev/null || echo "$body"
        else
            echo -e "${GRAY}Response received (set VERBOSE=true for details)${NC}"
        fi
    else
        echo -e "${RED}✗ Error (HTTP $http_code)${NC}"
        echo "$body"
    fi
    echo ""
}

# Test 1: OAuth2 Authorization Server Discovery
test_endpoint "OAuth2 Authorization Server Discovery" "$BASE_URL/.well-known/oauth-authorization-server"

# Test 2: OpenID Connect Discovery
test_endpoint "OpenID Connect Discovery" "$BASE_URL/.well-known/openid_configuration"

# Test 3: JWKS (JSON Web Key Set)
test_endpoint "JSON Web Key Set (JWKS)" "$BASE_URL/oauth2/jwks"

# Test 4: Health Check for Token Validation Service
test_endpoint "Token Validation Health Check" "$BASE_URL/api/v1/auth/health"

# Test 5: User Registration (JWT Service) - if not skipped
if [ "$SKIP_USER_TESTS" != "true" ]; then
    RANDOM_ID=$RANDOM
    signup_body="{\"name\":\"testuser_$RANDOM_ID\",\"email\":\"test_$RANDOM_ID@example.com\",\"password\":\"password123\"}"
    test_endpoint "User Registration (JWT Service)" "$BASE_URL/api/v1/users/signup" "POST" "" "$signup_body"
fi

echo -e "${YELLOW}=== Manual Steps Required ===${NC}"
echo ""
echo -e "${CYAN}1. OAuth2 Authorization Flow:${NC}"
echo -e "${WHITE}   Open this URL in your browser:${NC}"
echo -e "${YELLOW}   $BASE_URL/oauth2/authorize?response_type=code&client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=openid%20profile&state=xyz${NC}"
echo ""
echo -e "${CYAN}2. Login with credentials:${NC}"
echo -e "${WHITE}   Username: user${NC}"
echo -e "${WHITE}   Password: password${NC}"
echo ""
echo -e "${CYAN}3. After authorization, you'll get a code in the redirect URL${NC}"
echo ""
echo -e "${CYAN}4. To exchange the code for tokens, run:${NC}"
echo -e "${YELLOW}   curl -X POST \"$BASE_URL/oauth2/token\" \\${NC}"
echo -e "${YELLOW}     -H \"Content-Type: application/x-www-form-urlencoded\" \\${NC}"
echo -e "${YELLOW}     -H \"Authorization: Basic $CLIENT_CREDENTIALS\" \\${NC}"
echo -e "${YELLOW}     -d \"grant_type=authorization_code&code=YOUR_AUTH_CODE&redirect_uri=$REDIRECT_URI\"${NC}"
echo ""

echo -e "${YELLOW}=== Additional Testing Commands ===${NC}"
echo ""
echo -e "${CYAN}Token Introspection:${NC}"
echo -e "${YELLOW}curl -X POST \"$BASE_URL/oauth2/introspect\" -H \"Authorization: Basic $CLIENT_CREDENTIALS\" -d \"token=YOUR_TOKEN\"${NC}"
echo ""
echo -e "${CYAN}UserInfo Endpoint:${NC}"
echo -e "${YELLOW}curl -X GET \"$BASE_URL/userinfo\" -H \"Authorization: Bearer YOUR_ACCESS_TOKEN\"${NC}"
echo ""
echo -e "${CYAN}Token Revocation:${NC}"
echo -e "${YELLOW}curl -X POST \"$BASE_URL/oauth2/revoke\" -H \"Authorization: Basic $CLIENT_CREDENTIALS\" -d \"token=YOUR_TOKEN\"${NC}"
echo ""

echo -e "${GREEN}=== Testing Complete ===${NC}"
echo ""
echo -e "${YELLOW}For more detailed testing information, see:${NC}"
echo -e "${WHITE}- README.md (complete documentation)${NC}"
echo -e "${WHITE}- oauth2-testing-explained.md (detailed testing guide)${NC}"
