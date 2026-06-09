Write-Host "`n=== Stopping Complete System ===" -ForegroundColor Cyan

# Step 1: Stop microservices
Write-Host "`n1. Stopping Microservices..." -ForegroundColor Yellow
$ports = @(8081, 8082, 8083, 8084)
foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        $pid = $connection[0].OwningProcess
        $processName = (Get-Process -Id $pid -ErrorAction SilentlyContinue).ProcessName
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Write-Host "   Stopped $processName on port $port" -ForegroundColor Green
    }
}

# Step 2: Stop Docker services
Write-Host "`n2. Stopping Docker Services..." -ForegroundColor Yellow
docker-compose down
Write-Host "   ✅ All Docker services stopped" -ForegroundColor Green

Write-Host "`n✅ System shutdown complete!" -ForegroundColor Green
