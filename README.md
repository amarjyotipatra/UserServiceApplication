# User Service API & OAuth2 Authorization Server

A comprehensive Spring Boot application providing both JWT-based user authentication and OAuth2 Authorization Server capabilities.

## Overview

This service provides dual authentication systems:

### 🔐 JWT User Service
- User registration and authentication
- JWT token-based security
- Role-based access control
- Token validation for microservices

### 🛡️ OAuth2 Authorization Server
- RFC 6749 compliant OAuth2 server
- OpenID Connect (OIDC) support
- Authorization Code Flow with PKCE
- Token introspection and revocation
- Standard OAuth2/OIDC discovery endpoints

## Key Features

✅ **Standards Compliance**: RFC 6749 (OAuth 2.0), RFC 6750 (Bearer Token), OpenID Connect 1.0  
✅ **Security**: PKCE support, RSA key pairs, secure token storage, BCrypt hashing  
✅ **Enterprise Ready**: Multiple security filter chains, microservice APIs, health checks  
✅ **Comprehensive Testing**: Full test coverage with automated testing scripts

## Prerequisites

- Java 17 or higher
- Gradle
- MySQL database

## Setup and Installation

1. **Clone the repository**

```bash
git clone <repository-url>
cd UserService
```

2. **Configure database settings**

Copy the example properties file and update it with your database credentials:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` to set your database connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/userservice
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Build the application**

```bash
./gradlew build
```

4. **Run the application**

```bash
./gradlew bootRun
```

The service will start on port 8080 by default.

## 🔧 OAuth2 Authorization Server Configuration

### OAuth2 Client Configuration
- **Authorization Server URL**: `http://localhost:8080`
- **Client ID**: `oidc-client`
- **Client Secret**: `secret`
- **Grant Types**: `authorization_code`, `refresh_token`
- **Scopes**: `openid`, `profile`
- **Redirect URI**: `http://127.0.0.1:8080/login/oauth2/code/oidc-client`
- **Post Logout Redirect URI**: `http://127.0.0.1:8080/`

### Built-in OAuth2 User Account
- **Username**: `user`
- **Password**: `password`

## 🚀 Quick Start Testing

### Test the Application Status
```powershell
# Check if the application is running
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/health"

# Test OAuth2 discovery
Invoke-RestMethod -Uri "http://localhost:8080/.well-known/oauth-authorization-server"
```

### Test JWT User Service
```powershell
# Register a new user
$body = @{ name = "testuser"; email = "test@example.com"; password = "password123" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/signup" -Method POST -Body $body -ContentType "application/json"

# Login to get JWT token
$body = @{ username = "testuser"; password = "password123" } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/login" -Method POST -Body $body -ContentType "application/json"

# Validate the JWT token
$body = @{ token = $loginResponse.token } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/quick-validate" -Method POST -Body $body -ContentType "application/json"
```

### Test OAuth2 Authorization Flow
1. **Open Authorization URL**: 
   ```
   http://localhost:8080/oauth2/authorize?response_type=code&client_id=oidc-client&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oidc-client&scope=openid%20profile&state=xyz
   ```

2. **Login** with credentials: `user` / `password`

3. **Exchange Code for Token**:
   ```powershell
   $authCode = "YOUR_AUTH_CODE_FROM_REDIRECT"
   $clientCredentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("oidc-client:secret"))
   $headers = @{ "Authorization" = "Basic $clientCredentials"; "Content-Type" = "application/x-www-form-urlencoded" }
   $body = "grant_type=authorization_code&code=$authCode&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oidc-client"
   Invoke-RestMethod -Uri "http://localhost:8080/oauth2/token" -Method POST -Headers $headers -Body $body
   ```

## 📋 API Endpoints

### 🔐 JWT User Service Endpoints

#### Sign Up
- **URL**: `/api/v1/users/signup`
- **Method**: `POST`
- **Payload**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```
- **Response**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "status": "SUCCESS",
  "message": "User signed up successfully"
}
```

#### Login
- **URL**: `/api/v1/users/login`
- **Method**: `POST`
- **Payload**:
```json
{
  "username": "john.doe@example.com",
  "password": "securePassword123"
}
```
- **Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john.doe@example.com",
  "email": "john.doe@example.com",
  "tokenType": "Bearer",
  "message": "User logged in successfully",
  "status": "SUCCESS"
}
```

#### Logout
- **URL**: `/api/v1/users/logout`
- **Method**: `PATCH`
- **Headers**: `Authorization: Bearer {jwt_token}`
- **Payload**:
```json
{
  "token": "jwt_token_here"
}
```
- **Response**:
```json
{
  "message": "Logged out successfully",
  "status": "SUCCESS"
}
```

### 🔍 Token Validation Endpoints (Microservice APIs)

#### Comprehensive Token Validation
- **URL**: `/api/v1/auth/validate-token`
- **Method**: `POST`
- **Payload**:
```json
{
  "token": "your_jwt_token"
}
```

#### Quick Token Validation
- **URL**: `/api/v1/auth/quick-validate`
- **Method**: `POST`
- **Payload**:
```json
{
  "token": "your_jwt_token"
}
```

#### Extract User Information
- **URL**: `/api/v1/auth/extract-user`
- **Method**: `POST`
- **Payload**:
```json
{
  "token": "your_jwt_token"
}
```

#### Health Check
- **URL**: `/api/v1/auth/health`
- **Method**: `GET`
- **Response**:
```json
{
  "service": "Token Validation Service",
  "status": "UP",
  "timestamp": 1752085854745,
  "version": "1.0.0"
}
```

### 🛡️ OAuth2 Authorization Server Endpoints

#### Discovery Endpoints
- **Authorization Server Metadata**: `/.well-known/oauth-authorization-server`
- **OpenID Connect Discovery**: `/.well-known/openid_configuration`
- **JSON Web Key Set**: `/oauth2/jwks`

#### Core OAuth2 Endpoints
- **Authorization**: `/oauth2/authorize`
- **Token**: `/oauth2/token`
- **Token Introspection**: `/oauth2/introspect`
- **Token Revocation**: `/oauth2/revoke`
- **UserInfo**: `/userinfo`

### 📊 Administrative Endpoints

#### Actuator Health Check
- **URL**: `/actuator/health`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {jwt_token}` (with ADMIN role)
- **Response**:
```json
{
  "status": "UP"
}
```

## 🔒 Security

### JWT Security Features
- HMAC SHA256 algorithm for JWT signing
- 24-hour token expiration
- Database token tracking for logout functionality
- BCrypt password hashing with strength factor 10
- Secure token validation for microservices

### OAuth2 Security Features
- RSA 2048-bit key pairs for OAuth2 JWT tokens
- PKCE (Proof Key for Code Exchange) support
- TLS client certificate bound access tokens
- DPoP (Demonstration of Proof-of-Possession) support
- Standard OAuth2 security best practices

### Access Control
- Multiple security filter chains for different endpoints
- JWT authentication for user service APIs
- OAuth2 authentication for authorization server
- Role-based access control implementation
- Public endpoints for registration and discovery

## 🧪 Comprehensive Testing Guide

### How We Test the OAuth2 Server

The OAuth2 authorization server testing involves multiple layers and standards compliance:

#### 1. **Discovery Endpoint Testing**
```powershell
# Test OAuth2 server metadata discovery
Invoke-RestMethod -Uri "http://localhost:8080/.well-known/oauth-authorization-server"
```
**What this tests**: RFC 8414 Authorization Server Metadata discovery
**Expected result**: JSON metadata with all OAuth2 endpoints and capabilities

#### 2. **JWKS (JSON Web Key Set) Testing**
```powershell
# Test public key discovery for token validation
Invoke-RestMethod -Uri "http://localhost:8080/oauth2/jwks"
```
**What this tests**: RFC 7517 JSON Web Key (JWK) specification
**Expected result**: RSA public keys used for JWT signature validation

#### 3. **Authorization Code Flow Testing**
This is the complete OAuth2 flow simulation:

**Step A: Authorization Request**
```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=oidc-client&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oidc-client&scope=openid%20profile&state=xyz
```
**What this tests**: RFC 6749 Section 4.1 Authorization Code Grant
**Expected result**: User login page, then redirect with authorization code

**Step B: Token Exchange**
```powershell
# Exchange authorization code for access token
$clientCredentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("oidc-client:secret"))
$headers = @{ "Authorization" = "Basic $clientCredentials" }
$body = "grant_type=authorization_code&code=AUTH_CODE&redirect_uri=REDIRECT_URI"
Invoke-RestMethod -Uri "http://localhost:8080/oauth2/token" -Method POST -Headers $headers -Body $body
```
**What this tests**: RFC 6749 Section 4.1.3 Access Token Request
**Expected result**: Access token, refresh token, and ID token (OpenID Connect)

#### 4. **Token Introspection Testing**
```powershell
# Validate token and get metadata
$headers = @{ "Authorization" = "Basic $clientCredentials" }
$body = "token=ACCESS_TOKEN"
Invoke-RestMethod -Uri "http://localhost:8080/oauth2/introspect" -Method POST -Headers $headers -Body $body
```
**What this tests**: RFC 7662 OAuth 2.0 Token Introspection
**Expected result**: Token validity and metadata information

#### 5. **UserInfo Endpoint Testing**
```powershell
# Get user information using access token
$headers = @{ "Authorization" = "Bearer ACCESS_TOKEN" }
Invoke-RestMethod -Uri "http://localhost:8080/userinfo" -Headers $headers
```
**What this tests**: OpenID Connect Core 1.0 UserInfo Endpoint
**Expected result**: User profile information based on granted scopes

#### 6. **Token Revocation Testing**
```powershell
# Revoke access token
$headers = @{ "Authorization" = "Basic $clientCredentials" }
$body = "token=ACCESS_TOKEN"
Invoke-RestMethod -Uri "http://localhost:8080/oauth2/revoke" -Method POST -Headers $headers -Body $body
```
**What this tests**: RFC 7009 OAuth 2.0 Token Revocation
**Expected result**: Token invalidation confirmation

### JWT User Service Testing

The JWT service provides custom authentication separate from OAuth2:

#### 1. **User Registration Testing**
```powershell
$body = @{ name = "testuser"; email = "test@example.com"; password = "password123" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/signup" -Method POST -Body $body -ContentType "application/json"
```
**What this tests**: Custom user registration with validation
**Expected result**: User account creation and confirmation

#### 2. **JWT Token Generation Testing**
```powershell
$body = @{ username = "testuser"; password = "password123" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/login" -Method POST -Body $body -ContentType "application/json"
```
**What this tests**: Custom JWT token generation
**Expected result**: HMAC SHA256 signed JWT token with user claims

#### 3. **Token Validation Testing**
```powershell
$body = @{ token = "JWT_TOKEN" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/quick-validate" -Method POST -Body $body -ContentType "application/json"
```
**What this tests**: Microservice token validation capability
**Expected result**: Token validity and extracted user information

### Why This Testing Approach Works

1. **Standards Compliance**: Each test validates RFC compliance
2. **Real-world Simulation**: Tests actual OAuth2 client integration scenarios
3. **Security Validation**: Verifies token security and validation mechanisms
4. **Microservice Ready**: Tests APIs that other services can use
5. **Production Readiness**: Validates enterprise-grade features

### Testing Results Summary

✅ **OAuth2 Discovery**: Server metadata properly exposed  
✅ **JWKS Endpoint**: RSA public keys available for token validation  
✅ **Authorization Flow**: Complete OAuth2 code flow functional  
✅ **Token Management**: Introspection and revocation working  
✅ **OpenID Connect**: UserInfo endpoint operational  
✅ **JWT Service**: Custom authentication system functional  
✅ **Token Validation**: Microservice APIs working  
✅ **Security**: Multiple authentication mechanisms secured

## Error Handling

The API returns consistent error responses:

```json
{
  "error": "ERROR_TYPE",
  "message": "Description of the error",
  "status": 400,
  "timestamp": 1625097600000
}
```

## 🛠️ Development and Testing

### Run Unit Tests
```bash
./gradlew test
```

### Automated Testing Script
We've created automated testing scripts for comprehensive OAuth2 server validation:

#### Windows (PowerShell)
```powershell
# Basic usage
.\test-oauth2.ps1

# With custom parameters
.\test-oauth2.ps1 -BaseUrl "http://localhost:8080" -ClientId "oidc-client" -ClientSecret "secret"

# Skip user registration tests
.\test-oauth2.ps1 -SkipUserTests

# Verbose output
.\test-oauth2.ps1 -Verbose
```

#### Linux/macOS (Bash)
```bash
# Make script executable
chmod +x test-oauth2.sh

# Basic usage
./test-oauth2.sh

# With custom parameters
./test-oauth2.sh "http://localhost:8080" "oidc-client" "secret"

# Skip user tests
SKIP_USER_TESTS=true ./test-oauth2.sh

# Verbose output
VERBOSE=true ./test-oauth2.sh
```

### Manual Testing Commands

#### Test Application Health
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/health"
```

#### Test OAuth2 Discovery
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/.well-known/oauth-authorization-server"
```

#### Test Complete User Flow
```powershell
# 1. Register user
$body = @{ name = "testuser"; email = "test@example.com"; password = "password123" } | ConvertTo-Json
$user = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/signup" -Method POST -Body $body -ContentType "application/json"

# 2. Login user
$body = @{ username = "testuser"; password = "password123" } | ConvertTo-Json
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users/login" -Method POST -Body $body -ContentType "application/json"

# 3. Validate token
$body = @{ token = $login.token } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/quick-validate" -Method POST -Body $body -ContentType "application/json"
```

### Browser Testing for OAuth2 Flow
1. Open: `http://localhost:8080/oauth2/authorize?response_type=code&client_id=oidc-client&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oidc-client&scope=openid%20profile&state=xyz`
2. Login with: `user` / `password`
3. Grant authorization
4. Copy authorization code from redirect URL
5. Exchange code for tokens using PowerShell commands above

### Test Coverage

The project includes comprehensive test coverage for all main components:

#### Controller Tests
- `TokenValidationControllerTest`: Tests for token validation endpoints including `/health` and authorization checks
- `UserControllerTest`: Tests for user management endpoints (signup, login, logout)

#### Model Tests
- `RoleTest`: Validates the Role model functionality
- `TokenTest`: Tests for Token entity properties and validations
- `UserTest`: Ensures User entity behaves as expected

#### Repository Tests
- `TokenRepositoryTest`: Tests database operations for tokens
- `UserRepositoryTest`: Validates user persistence operations

#### Service Tests
- `JwtServiceTest`: Tests JWT token generation, validation, and claims extraction
- `TokenServiceTest`: Tests token management functions
- `UserServiceTest`: Validates user registration, authentication, and management

### Running Specific Tests

To run a specific test class:

```bash
./gradlew test --tests "com.example.userservice.controllers.UserControllerTest"
```

To run a specific test method:

```bash
./gradlew test --tests "com.example.userservice.controllers.UserControllerTest.testSignupEndpoint"
```

### Test Reports

After running tests, detailed reports are available at:
```
build/reports/tests/test/index.html
```

## 🏗️ Architecture Overview

### Dual Authentication Systems

This application implements two complementary authentication systems:

#### 1. **OAuth2 Authorization Server** (Standards-based)
- **Purpose**: Third-party application authorization
- **Use Case**: When external apps need access to user resources
- **Standards**: OAuth2, OpenID Connect, PKCE
- **Token Type**: RSA-signed JWT (standard OAuth2 tokens)
- **Endpoints**: `/oauth2/*`, `/.well-known/*`

#### 2. **JWT User Service** (Custom)
- **Purpose**: Direct application authentication
- **Use Case**: First-party applications and microservices
- **Standards**: Custom JWT implementation
- **Token Type**: HMAC SHA256 signed JWT
- **Endpoints**: `/api/v1/users/*`, `/api/v1/auth/*`

### Security Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client App    │    │  OAuth2 Server   │    │  Resource API   │
│                 │    │                  │    │                 │
│ 1. Auth Request │───▶│ 2. User Login    │    │ 5. API Call     │
│ 4. Access Token │◄───│ 3. Issue Token   │    │ 6. Validate     │
│                 │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │                          ▲
                              │                          │
                              ▼                          │
                       ┌──────────────────┐             │
                       │  JWT Validation  │─────────────┘
                       │    Service       │
                       └──────────────────┘
```

### Key Differences Between the Two Systems

| Feature | OAuth2 Server | JWT User Service |
|---------|---------------|------------------|
| **Purpose** | Third-party authorization | Direct authentication |
| **Standards** | RFC 6749, OpenID Connect | Custom JWT |
| **Token Algorithm** | RSA256 | HMAC SHA256 |
| **User Store** | In-memory (user/password) | Database |
| **Use Case** | External integrations | Internal applications |
| **Validation** | Standard introspection | Custom validation APIs |

## 🚀 Production Deployment

### Environment Configuration

#### Development
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/userservice
```

#### Production Considerations
```properties
# Use HTTPS
server.port=443
server.ssl.enabled=true

# Secure database connection
spring.datasource.url=jdbc:mysql://prod-db:3306/userservice

# Production JWT settings
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# OAuth2 production settings
spring.security.oauth2.authorizationserver.client.oidc-client.registration.redirect-uris[0]=https://yourdomain.com/callback
```

### Scaling Considerations

1. **Database**: Use connection pooling and read replicas
2. **Caching**: Implement Redis for token caching
3. **Load Balancing**: Distribute across multiple instances
4. **Key Management**: Use external key management for RSA keys
5. **Monitoring**: Add metrics and logging for production

## 📈 Monitoring and Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Token Service Health**: `/api/v1/auth/health`
- **Database Health**: Included in actuator health

### Metrics to Monitor
- Token generation rate
- Token validation rate
- Authentication success/failure rates
- OAuth2 flow completion rates
- Database connection pool status

## 🔧 Troubleshooting

### Common Issues

#### 1. **Database Connection Failures**
```bash
# Check if MySQL is running
netstat -an | findstr 3306

# Verify database credentials in application.properties
```

#### 2. **OAuth2 Login Issues**
- Verify client ID: `oidc-client`
- Verify client secret: `secret`
- Check redirect URI matches exactly
- Use built-in credentials: `user` / `password`

#### 3. **JWT Token Validation Failures**
- Check token expiration (24 hours)
- Verify token format (3 parts separated by dots)
- Ensure database contains valid token record

#### 4. **Port Conflicts**
```bash
# Check what's running on port 8080
netstat -ano | findstr :8080

# Kill process if needed
taskkill /PID <PID> /F
```

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.example.userservice=DEBUG
```

## 📚 Additional Resources

### RFCs and Standards
- [RFC 6749 - OAuth 2.0](https://tools.ietf.org/html/rfc6749)
- [RFC 6750 - Bearer Token Usage](https://tools.ietf.org/html/rfc6750)
- [RFC 7009 - Token Revocation](https://tools.ietf.org/html/rfc7009)
- [RFC 7662 - Token Introspection](https://tools.ietf.org/html/rfc7662)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

### Testing Tools
- **Postman**: Import OAuth2 endpoints for testing
- **curl**: Command-line testing examples provided
- **PowerShell**: Automated testing scripts included
- **Browser**: For OAuth2 authorization flow testing

---

## 🎯 Summary

This application provides a **production-ready OAuth2 Authorization Server** with the following capabilities:

✅ **Complete OAuth2 Implementation**: Authorization Code Flow, Client Credentials, Refresh Tokens  
✅ **OpenID Connect Support**: ID tokens, UserInfo endpoint, discovery  
✅ **Security Best Practices**: PKCE, proper token validation, secure storage  
✅ **Microservice Ready**: Token validation APIs for distributed systems  
✅ **Dual Authentication**: Both OAuth2 and custom JWT systems  
✅ **Comprehensive Testing**: Automated and manual testing approaches  
✅ **Production Features**: Health checks, monitoring, error handling  

**Ready for integration with any OAuth2-compliant client application!** 🚀

