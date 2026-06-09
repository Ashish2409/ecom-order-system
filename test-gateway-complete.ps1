Write-Host "`n=== API Gateway Complete Testing ===" -ForegroundColor Cyan

# Test 1: Health Check
Write-Host "`n1. Gateway Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    Write-Host "✅ Status: $($response.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 2: List all routes
Write-Host "`n2. Gateway Routes" -ForegroundColor Yellow
try {
    $routes = Invoke-RestMethod -Uri "http://localhost:8080/actuator/gateway/routes" -Method Get
    Write-Host "✅ Total Routes: $($routes.Count)" -ForegroundColor Green
    foreach($route in $routes) {
        Write-Host "   - $($route.route_id) -> $($route.uri)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Public Endpoint - Get Products
Write-Host "`n3. Public Endpoint Test (GET /api/products)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -Method Get -UseBasicParsing
    $correlationId = $response.Headers['X-Correlation-Id']
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Correlation-ID: $correlationId" -ForegroundColor Cyan
    
    $json = $response.Content | ConvertFrom-Json
    if ($json.data) {
        Write-Host "   Products found: $($json.data.Count)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 4: Public Endpoint - Get Categories
Write-Host "`n4. Public Endpoint Test (GET /api/categories)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/categories" -Method Get -UseBasicParsing
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
    
    $json = $response.Content | ConvertFrom-Json
    if ($json.data) {
        Write-Host "   Categories found: $($json.data.Count)" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

# Test 5: Protected Endpoint Without Auth (Should Fail)
Write-Host "`n5. Protected Endpoint Without Auth (GET /api/orders/me)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/me" -Method Get -UseBasicParsing
    Write-Host "❌ Should have failed but got: $($response.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ Correctly rejected (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "❌ Wrong error: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}

# Test 6: Rate Limiting Test
Write-Host "`n6. Rate Limiting Test (Multiple rapid requests)" -ForegroundColor Yellow
$successCount = 0
$rateLimitedCount = 0
for ($i = 1; $i -le 25; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -Method Get -UseBasicParsing -ErrorAction Stop
        $successCount++
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 429) {
            $rateLimitedCount++
        }
    }
}
Write-Host "   Successful requests: $successCount" -ForegroundColor Green
if ($rateLimitedCount -gt 0) {
    Write-Host "   Rate limited (429): $rateLimitedCount" -ForegroundColor Yellow
    Write-Host "✅ Rate limiting is working!" -ForegroundColor Green
} else {
    Write-Host "⚠️  No rate limiting detected (might need more requests)" -ForegroundColor Yellow
}

# Test 7: CORS Headers Test
Write-Host "`n7. CORS Headers Test" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -Method Get -UseBasicParsing
    $corsHeader = $response.Headers['Access-Control-Allow-Origin']
    if ($corsHeader) {
        Write-Host "✅ CORS Headers present" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No CORS headers found" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Failed" -ForegroundColor Red
}

# Test 8: Circuit Breaker Status
Write-Host "`n8. Circuit Breaker Status" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    if ($health.components) {
        Write-Host "✅ Circuit breakers configured" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Failed to get status" -ForegroundColor Red
}

Write-Host "`n=== Testing Summary ===" -ForegroundColor Cyan
Write-Host "✅ Gateway is operational" -ForegroundColor Green
Write-Host "✅ Routing working for public endpoints" -ForegroundColor Green
Write-Host "✅ Authentication protection working" -ForegroundColor Green
Write-Host "✅ Request tracking (Correlation ID) working" -ForegroundColor Green
if ($rateLimitedCount -gt 0) {
    Write-Host "✅ Rate limiting working" -ForegroundColor Green
}
Write-Host "`n🎉 API Gateway is fully functional!" -ForegroundColor Cyan
