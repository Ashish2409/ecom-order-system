Write-Host "`n=== Complete System Startup (All Services + Gateway) ===" -ForegroundColor Cyan

# Step 1: Check Docker
Write-Host "`n1. Checking Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "   ✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit
}

# Step 2: Start infrastructure (Postgres, Redis, Kafka)
Write-Host "`n2. Starting Infrastructure (Postgres, Redis, Kafka)..." -ForegroundColor Yellow
docker-compose up -d postgres redis kafka zookeeper
Start-Sleep -Seconds 5
Write-Host "   ✅ Infrastructure started" -ForegroundColor Green

# Step 3: Start microservices
Write-Host "`n3. Starting Microservices..." -ForegroundColor Yellow
& "$PSScriptRoot\start-services.ps1"

# Step 4: Start API Gateway
Write-Host "`n4. Starting API Gateway..." -ForegroundColor Yellow
docker-compose up -d api-gateway
Start-Sleep -Seconds 10
Write-Host "   ✅ API Gateway started" -ForegroundColor Green

# Step 5: Final status check
Write-Host "`n5. Final System Status..." -ForegroundColor Yellow
& "$PSScriptRoot\check-services.ps1"

Write-Host "`n🎉 Complete system is ready!" -ForegroundColor Green
Write-Host "`nAccess points:" -ForegroundColor Cyan
Write-Host "  API Gateway:  http://localhost:8080" -ForegroundColor White
Write-Host "  Health Check: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "  Swagger UI:   http://localhost:8080/swagger-ui.html" -ForegroundColor White
