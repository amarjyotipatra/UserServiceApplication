# User Service API

A Spring Boot application for user management with JWT authentication.

## Overview

This service provides user management functionality including:
- User registration and authentication
- JWT token-based security
- Role-based access control

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

The service will start on port 6000 by default.

## API Endpoints

### Authentication

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
  "token": "jwt_token_here",
  "message": "User registered successfully",
  "status": "SUCCESS"
}
```

#### Login
- **URL**: `/api/v1/users/login`
- **Method**: `POST`
- **Payload**:
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```
- **Response**:
```json
{
  "token": "jwt_token_here",
  "message": "Login successful",
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

### Actuator Endpoints (Admin Only)

#### Health Check
- **URL**: `/actuator/health`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {jwt_token}` (with ADMIN role)
- **Response**:
```json
{
  "status": "UP"
}
```

#### Other Actuator Endpoints
- **URL**: `/actuator/**`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {jwt_token}` (with ADMIN role)

## Security

- JWT tokens are used for authentication
- Passwords are encrypted using BCrypt
- Actuator endpoints are restricted to ADMIN users only
- API endpoints are secured based on user roles

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

## Testing

Run the tests with Gradle:

```bash
./gradlew test
```

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

