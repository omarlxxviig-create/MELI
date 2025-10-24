# Script para generar tráfico y métricas en la aplicación

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  GENERAR METRICAS DE PRUEBA" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

Write-Host "1. Verificando que la aplicación esté disponible..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    Write-Host "   [OK] Aplicación disponible - Status: $($health.status)" -ForegroundColor Green
}
catch {
    Write-Host "   [ERROR] La aplicación no está disponible" -ForegroundColor Red
    exit 1
}

Write-Host "`n2. Generando tráfico a diferentes endpoints..." -ForegroundColor Yellow
Write-Host "   (Esto generará métricas que podrás ver en Grafana)" -ForegroundColor Gray

$requests = 0
$errors = 0

# Lista de endpoints para probar
$endpoints = @(
    "/actuator/health",
    "/actuator/metrics",
    "/actuator/prometheus",
    "/actuator/info",
    "/api/products",
    "/api/reservations",
    "/swagger-ui.html"
)

Write-Host "`n   Haciendo 50 requests..." -ForegroundColor Cyan

for ($i = 1; $i -le 50; $i++) {
    $endpoint = $endpoints | Get-Random
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$endpoint" -Method Get -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        $requests++
        
        if ($i % 10 -eq 0) {
            Write-Host "   - Request $i : $endpoint -> $($response.StatusCode)" -ForegroundColor Green
        }
    }
    catch {
        $errors++
        if ($i % 10 -eq 0) {
            Write-Host "   - Request $i : $endpoint -> Error" -ForegroundColor Red
        }
    }
    
    Start-Sleep -Milliseconds 200
}

Write-Host "`n3. Resumen de requests:" -ForegroundColor Yellow
Write-Host "   - Total requests: $requests" -ForegroundColor White
Write-Host "   - Exitosos: $($requests - $errors)" -ForegroundColor Green
Write-Host "   - Errores: $errors" -ForegroundColor $(if ($errors -gt 0) { "Red" } else { "Green" })

Write-Host "`n4. Verificando métricas generadas..." -ForegroundColor Yellow
try {
    $metrics = Invoke-WebRequest -Uri "$baseUrl/actuator/prometheus" -Method Get -UseBasicParsing
    $httpMetrics = $metrics.Content -split "`n" | Where-Object { $_ -match "http_server_requests" -and $_ -notmatch "#" }
    
    Write-Host "   [OK] Métricas HTTP generadas:" -ForegroundColor Green
    $httpMetrics | Select-Object -First 5 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    Write-Host "     ... y más" -ForegroundColor Gray
}
catch {
    Write-Host "   [WARNING] No se pudieron obtener las métricas" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  AHORA PUEDES VER LAS METRICAS" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "En Prometheus (http://localhost:9090):" -ForegroundColor Yellow
Write-Host "  - Ve a Graph" -ForegroundColor White
Write-Host "  - Escribe: rate(http_server_requests_seconds_count[1m])" -ForegroundColor White
Write-Host "  - Click en 'Execute' y luego 'Graph'" -ForegroundColor White

Write-Host "`nEn Grafana (http://localhost:3000):" -ForegroundColor Yellow
Write-Host "  - Crea un nuevo panel" -ForegroundColor White
Write-Host "  - Selecciona Prometheus como datasource" -ForegroundColor White
Write-Host "  - Usa la misma query: rate(http_server_requests_seconds_count[1m])" -ForegroundColor White

Write-Host "`nMétricas interesantes para visualizar:" -ForegroundColor Yellow
Write-Host "  - Requests por segundo:" -ForegroundColor Cyan
Write-Host "    rate(http_server_requests_seconds_count[1m])" -ForegroundColor White
Write-Host "`n  - Latencia promedio:" -ForegroundColor Cyan
Write-Host "    rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])" -ForegroundColor White
Write-Host "`n  - Memoria heap usada:" -ForegroundColor Cyan
Write-Host "    jvm_memory_used_bytes{area=`"heap`"}" -ForegroundColor White
Write-Host "`n  - CPU del proceso:" -ForegroundColor Cyan
Write-Host "    process_cpu_usage" -ForegroundColor White
Write-Host ""
