# OAuth2 Authorization Server Testing Script
# This script provides automated testing for the OAuth2 authorization server
# Usage: .\test-oauth2.ps1 [-BaseUrl "http://localhost:8080"] [-ClientId "oidc-client"] [-ClientSecret "secret"]

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ClientId = "oidc-client",
    [string]$ClientSecret = "secret",
    [string]$RedirectUri = "http://127.0.0.1:8080/login/oauth2/code/oidc-client",
    [switch]$SkipUserTests = $false,
    [switch]$Verbose = $false
)

# Base64 encode client credentials
$clientCredentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${ClientId}:${ClientSecret}"))

Write-Host "=== OAuth2 Authorization Server Testing Script ===" -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "Client ID: $ClientId" -ForegroundColor Yellow
Write-Host "Redirect URI: $RedirectUri" -ForegroundColor Yellow
Write-Host ""

# Test if server is running
try {
    $response = Invoke-WebRequest -Uri $BaseUrl -Method HEAD -TimeoutSec 5 -UseBasicParsing
    Write-Host "✓ Server is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Server is not running at $BaseUrl" -ForegroundColor Red
    Write-Host "Please ensure the application is started with: .\gradlew bootRun" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    Write-Host "Testing: $Name" -ForegroundColor Cyan
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $response = if ($Body) {
            Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers -Body $Body -ContentType "application/json"
        } else {
            Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers
        }
          Write-Host "✓ Success" -ForegroundColor Green
        if ($Verbose) {
            $response | ConvertTo-Json -Depth 3 | Write-Host
        } else {
            Write-Host "Response received (use -Verbose for details)" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        }
    }
    Write-Host ""
}

# Test 1: OAuth2 Authorization Server Discovery
Test-Endpoint -Name "OAuth2 Authorization Server Discovery" -Url "$BaseUrl/.well-known/oauth-authorization-server"

# Test 2: OpenID Connect Discovery
Test-Endpoint -Name "OpenID Connect Discovery" -Url "$BaseUrl/.well-known/openid_configuration"

# Test 3: JWKS (JSON Web Key Set)
Test-Endpoint -Name "JSON Web Key Set (JWKS)" -Url "$BaseUrl/oauth2/jwks"

# Test 4: Health Check for Token Validation Service
Test-Endpoint -Name "Token Validation Health Check" -Url "$BaseUrl/api/v1/auth/health"

if (-not $SkipUserTests) {
    # Test 5: User Registration (JWT Service)
    $signupBody = @{
        name = "testuser_$(Get-Random)"
        email = "test_$(Get-Random)@example.com"
        password = "password123"
    } | ConvertTo-Json

    Test-Endpoint -Name "User Registration (JWT Service)" -Url "$BaseUrl/api/v1/users/signup" -Method "POST" -Body $signupBody
}

# Test 6: User Login (JWT Service)
$loginBody = @{
    name = "testuser"
    password = "password123"
} | ConvertTo-Json

Write-Host "=== Manual Steps Required ===" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. OAuth2 Authorization Flow:" -ForegroundColor Cyan
Write-Host "   Open this URL in your browser:" -ForegroundColor White
Write-Host "   $BaseUrl/oauth2/authorize?response_type=code`&client_id=$ClientId`&redirect_uri=$RedirectUri`&scope=openid%20profile`&state=xyz" -ForegroundColor Yellow
Write-Host ""
Write-Host "2. Login with credentials:" -ForegroundColor Cyan
Write-Host "   Username: user" -ForegroundColor White
Write-Host "   Password: password" -ForegroundColor White
Write-Host ""
Write-Host "3. After authorization, you'll get a code in the redirect URL" -ForegroundColor Cyan
Write-Host ""
Write-Host "4. To exchange the code for tokens, run:" -ForegroundColor Cyan
Write-Host "   curl -X POST `"$BaseUrl/oauth2/token`" \" -ForegroundColor Yellow
Write-Host "     -H `"Content-Type: application/x-www-form-urlencoded`" \" -ForegroundColor Yellow
Write-Host "     -H `"Authorization: Basic $clientCredentials`" \" -ForegroundColor Yellow
Write-Host "     -d `"grant_type=authorization_code&code=YOUR_AUTH_CODE&redirect_uri=$RedirectUri`"" -ForegroundColor Yellow
Write-Host ""

Write-Host "=== Additional Testing Commands ===" -ForegroundColor Yellow
Write-Host ""
Write-Host "Token Introspection:" -ForegroundColor Cyan
Write-Host "curl -X POST `"$BaseUrl/oauth2/introspect`" -H `"Authorization: Basic $clientCredentials`" -d `"token=YOUR_TOKEN`"" -ForegroundColor Yellow
Write-Host ""
Write-Host "UserInfo Endpoint:" -ForegroundColor Cyan
Write-Host "curl -X GET `"$BaseUrl/userinfo`" -H `"Authorization: Bearer YOUR_ACCESS_TOKEN`"" -ForegroundColor Yellow
Write-Host ""
Write-Host "Token Revocation:" -ForegroundColor Cyan
Write-Host "curl -X POST `"$BaseUrl/oauth2/revoke`" -H `"Authorization: Basic $clientCredentials`" -d `"token=YOUR_TOKEN`"" -ForegroundColor Yellow
Write-Host ""

Write-Host "=== Testing Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "For more detailed testing information, see:" -ForegroundColor Yellow
Write-Host "- README.md (complete documentation)" -ForegroundColor White
Write-Host "- oauth2-testing-explained.md (detailed testing guide)" -ForegroundColor White
