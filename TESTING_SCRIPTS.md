# Testing Scripts Documentation

This directory contains automated testing scripts to validate the OAuth2 Authorization Server functionality.

## Available Scripts

### 🪟 Windows PowerShell Script: `test-oauth2.ps1`

A comprehensive PowerShell script for testing OAuth2 endpoints on Windows systems.

#### Features:
- ✅ Parameter support for custom configurations
- ✅ Server availability checking
- ✅ Automated endpoint testing
- ✅ Verbose mode for detailed responses
- ✅ Option to skip user registration tests
- ✅ Colored output for better readability
- ✅ Error handling and status reporting

#### Usage:
```powershell
# Basic usage
.\test-oauth2.ps1

# Custom configuration
.\test-oauth2.ps1 -BaseUrl "https://myserver.com" -ClientId "my-client" -ClientSecret "my-secret"

# Skip user tests (for production environments)
.\test-oauth2.ps1 -SkipUserTests

# Verbose output (shows full JSON responses)
.\test-oauth2.ps1 -Verbose

# Combine options
.\test-oauth2.ps1 -BaseUrl "http://localhost:8080" -SkipUserTests -Verbose
```

#### Parameters:
- `BaseUrl`: OAuth2 server base URL (default: http://localhost:8080)
- `ClientId`: OAuth2 client ID (default: oidc-client)
- `ClientSecret`: OAuth2 client secret (default: secret)
- `RedirectUri`: OAuth2 redirect URI (default: http://127.0.0.1:8080/login/oauth2/code/oidc-client)
- `SkipUserTests`: Skip user registration tests (useful for production)
- `Verbose`: Show detailed JSON responses

### 🐧 Linux/macOS Bash Script: `test-oauth2.sh`

A cross-platform bash script for Unix-like systems.

#### Features:
- ✅ POSIX-compliant bash script
- ✅ JSON formatting with `jq` (optional)
- ✅ Colored output support
- ✅ Environment variable configuration
- ✅ HTTP status code validation
- ✅ Cross-platform compatibility

#### Usage:
```bash
# Make executable (first time only)
chmod +x test-oauth2.sh

# Basic usage
./test-oauth2.sh

# Custom parameters
./test-oauth2.sh "https://myserver.com" "my-client" "my-secret"

# Environment variables
VERBOSE=true ./test-oauth2.sh
SKIP_USER_TESTS=true ./test-oauth2.sh

# Combined usage
BASE_URL="http://localhost:8080" VERBOSE=true ./test-oauth2.sh
```

#### Environment Variables:
- `VERBOSE`: Show detailed responses (true/false)
- `SKIP_USER_TESTS`: Skip user registration tests (true/false)

## What the Scripts Test

### 🔍 OAuth2 Discovery Endpoints
1. **Authorization Server Metadata** (`/.well-known/oauth-authorization-server`)
   - Validates RFC 8414 compliance
   - Checks endpoint availability
   - Verifies supported grant types

2. **OpenID Connect Discovery** (`/.well-known/openid_configuration`)
   - Tests OIDC metadata endpoint
   - Validates OIDC compliance

3. **JSON Web Key Set** (`/oauth2/jwks`)
   - Tests public key availability
   - Validates JWKS format
   - Ensures RSA key accessibility

### 🔐 Service Health Checks
4. **Token Validation Service Health** (`/api/v1/auth/health`)
   - Validates microservice health
   - Tests service availability
   - Checks response format

### 👤 User Management (Optional)
5. **User Registration** (`/api/v1/users/signup`)
   - Tests JWT user service
   - Validates user creation
   - Tests input validation

## Script Output

### ✅ Success Indicators
- Green checkmarks (✓) for successful tests
- HTTP status codes 200-299
- Proper JSON responses
- Service availability confirmation

### ❌ Failure Indicators
- Red X marks (✗) for failed tests
- HTTP error status codes
- Connection timeouts
- Malformed responses

### 📊 Example Output
```
=== OAuth2 Authorization Server Testing Script ===
Base URL: http://localhost:8080
Client ID: oidc-client

✓ Server is running

Testing: OAuth2 Authorization Server Discovery
URL: http://localhost:8080/.well-known/oauth-authorization-server
✓ Success (HTTP 200)
Response received (use -Verbose for details)

Testing: JSON Web Key Set (JWKS)
URL: http://localhost:8080/oauth2/jwks
✓ Success (HTTP 200)
Response received (use -Verbose for details)

Testing: Token Validation Health Check
URL: http://localhost:8080/api/v1/auth/health
✓ Success (HTTP 200)
Response received (use -Verbose for details)

=== Manual Steps Required ===
1. OAuth2 Authorization Flow:
   Open this URL in your browser:
   http://localhost:8080/oauth2/authorize?response_type=code&client_id=oidc-client&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oidc-client&scope=openid%20profile&state=xyz

=== Testing Complete ===
```

## Integration with CI/CD

### GitHub Actions Example
```yaml
name: OAuth2 Server Tests
on: [push, pull_request]

jobs:
  test-oauth2:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start OAuth2 Server
        run: |
          ./gradlew bootRun &
          sleep 30
      - name: Test OAuth2 Endpoints
        run: |
          chmod +x test-oauth2.sh
          SKIP_USER_TESTS=true ./test-oauth2.sh
```

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    stages {
        stage('Test OAuth2 Server') {
            steps {
                script {
                    sh './gradlew bootRun &'
                    sleep 30
                    sh 'chmod +x test-oauth2.sh'
                    sh 'SKIP_USER_TESTS=true ./test-oauth2.sh'
                }
            }
        }
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Server Not Running
```
✗ Server is not running at http://localhost:8080
```
**Solution**: Ensure the application is started with `./gradlew bootRun`

#### 2. Permission Denied (Linux/macOS)
```
bash: ./test-oauth2.sh: Permission denied
```
**Solution**: Make the script executable with `chmod +x test-oauth2.sh`

#### 3. PowerShell Execution Policy (Windows)
```
cannot be loaded because running scripts is disabled on this system
```
**Solution**: Run `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

#### 4. Missing Dependencies (Linux/macOS)
```
jq: command not found
```
**Solution**: Install jq with `sudo apt-get install jq` (Ubuntu) or `brew install jq` (macOS)

### Debug Mode

#### PowerShell
```powershell
# Enable verbose output
.\test-oauth2.ps1 -Verbose

# Check PowerShell version
$PSVersionTable.PSVersion
```

#### Bash
```bash
# Enable verbose output
VERBOSE=true ./test-oauth2.sh

# Debug bash script execution
bash -x ./test-oauth2.sh
```

## Best Practices

### 🔒 Security Considerations
- **Never commit secrets**: Use environment variables for production secrets
- **Use HTTPS**: Always use HTTPS in production environments
- **Rotate credentials**: Regularly rotate OAuth2 client secrets
- **Limit scope**: Use minimal required scopes for testing

### 🧪 Testing Strategy
- **Automated testing**: Include scripts in CI/CD pipelines
- **Environment-specific**: Use different configurations for dev/staging/prod
- **Regular validation**: Run tests after configuration changes
- **Monitoring integration**: Combine with health check monitoring

### 📝 Documentation
- **Keep scripts updated**: Update when adding new endpoints
- **Document parameters**: Clearly explain configuration options
- **Version control**: Track script changes alongside application changes
- **Usage examples**: Provide clear examples for different scenarios

## Contributing

When modifying these scripts:

1. **Test on both platforms**: Ensure Windows and Unix compatibility
2. **Update documentation**: Keep this file current with changes
3. **Follow conventions**: Maintain consistent coding style
4. **Add error handling**: Ensure graceful failure handling
5. **Update README**: Reflect changes in main documentation

## Support

For issues with the testing scripts:
1. Check this documentation first
2. Verify server is running and accessible
3. Test endpoints manually with curl/Postman
4. Check application logs for errors
5. Create an issue with script output and environment details
