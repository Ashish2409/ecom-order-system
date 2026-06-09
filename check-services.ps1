Write-Host "`n=== Microservices Status Check ===" -ForegroundColor Cyan

$services = @(
    @{Name="User Service"; Port=8081; Url="http://localhost:8081/actuator/health"},
    @{Name="Product Service"; Port=8082; Url="http://localhost:8082/actuator/health"},
    @{Name="Order Service"; Port=8083; Url="http://localhost:8083/actuator/health"},
    @{Name="Notification Service"; Port=8084; Url="http://localhost:8084/actuator/health"},
    @{Name="API Gateway"; Port=8080; Url="http://localhost:8080/actuator/health"}
)

Write-Host "`n"
foreach ($service in $services) {
    Write-Host "$($service.Name) (Port $($service.Port)):" -NoNewline
    
    # Check if port is listening
    $connection = Get-NetTCPConnection -LocalPort $service.Port -State Listen -ErrorAction SilentlyContinue
    
    if ($connection) {
        # Port is listening, check health
        try {
            $response = Invoke-RestMethod -Uri $service.Url -Method Get -TimeoutSec 2 -ErrorAction Stop
            if ($response.status -eq "UP") {
                Write-Host " ✅ UP" -ForegroundColor Green
            } else {
                Write-Host " ⚠️  Running but not healthy" -ForegroundColor Yellow
            }
        } catch {
            Write-Host " ⏳ Starting..." -ForegroundColor Yellow
        }
    } else {
        Write-Host " ❌ NOT RUNNING" -ForegroundColor Red
    }
}

Write-Host "`n=== Quick Commands ===" -ForegroundColor Cyan
Write-Host "Start all:  .\start-services.ps1" -ForegroundColor White
Write-Host "Stop all:   .\stop-services.ps1" -ForegroundColor White
Write-Host "Test:       .\test-gateway-complete.ps1" -ForegroundColor White
