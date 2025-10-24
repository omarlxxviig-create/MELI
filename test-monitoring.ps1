#!/usr/bin/env pwsh
# Script de diagnóstico para Grafana, Prometheus y Redis

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  DIAGNÓSTICO DE MONITOREO Y REDIS" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

# 1. Verificar contenedores Docker
Write-Host "1. Estado de contenedores Docker:" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String -Pattern "redis|prometheus|grafana"

# 2. Verificar conectividad Redis
Write-Host "`n2. Pruebas de Redis:" -ForegroundColor Cyan
Write-Host "   - Conectividad al puerto 6379:"
Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue | Select-Object ComputerName, RemotePort, TcpTestSucceeded

Write-Host "`n   - Ejecutar comando PING en Redis:"
docker exec redis redis-cli PING

Write-Host "`n   - Información del servidor Redis:"
docker exec redis redis-cli INFO server | Select-String -Pattern "redis_version|uptime_in_seconds|tcp_port"

Write-Host "`n   - Prueba de escritura/lectura en Redis:"
docker exec redis redis-cli SET test_key "Hola desde PowerShell" | Out-Null
$value = docker exec redis redis-cli GET test_key
Write-Host "   Valor guardado: $value" -ForegroundColor Yellow
docker exec redis redis-cli DEL test_key | Out-Null

Write-Host "`n   - Listar todas las keys en Redis:"
$keys = docker exec redis redis-cli KEYS "*"
if ($keys) {
    Write-Host "   Keys encontradas:" -ForegroundColor Yellow
    Write-Host "   $keys"
}
else {
    Write-Host "   No hay keys almacenadas en Redis" -ForegroundColor Yellow
}

# 3. Verificar Prometheus
Write-Host "`n3. Pruebas de Prometheus:" -ForegroundColor Cyan
Write-Host "   - Conectividad al puerto 9090:"
Test-NetConnection -ComputerName localhost -Port 9090 -WarningAction SilentlyContinue | Select-Object ComputerName, RemotePort, TcpTestSucceeded

Write-Host "`n   - Verificar targets de Prometheus:"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/targets" -Method Get
    $targets = $response.data.activeTargets
    Write-Host "   Targets configurados:" -ForegroundColor Yellow
    foreach ($target in $targets) {
        $status = if ($target.health -eq "up") { "[OK]" } else { "[ERROR]" }
        $color = if ($target.health -eq "up") { "Green" } else { "Red" }
        Write-Host "   $status Job: $($target.labels.job) - $($target.scrapeUrl) - Health: $($target.health)" -ForegroundColor $color
        if ($target.lastError) {
            Write-Host "     Error: $($target.lastError)" -ForegroundColor Red
        }
    }
}
catch {
    Write-Host "   Error al conectar con Prometheus: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Verificar Grafana
Write-Host "`n4. Pruebas de Grafana:" -ForegroundColor Cyan
Write-Host "   - Conectividad al puerto 3000:"
Test-NetConnection -ComputerName localhost -Port 3000 -WarningAction SilentlyContinue | Select-Object ComputerName, RemotePort, TcpTestSucceeded

Write-Host "`n   - Verificar datasources de Grafana:"
try {
    $auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
    $headers = @{
        Authorization = "Basic $auth"
    }
    $datasources = Invoke-RestMethod -Uri "http://localhost:3000/api/datasources" -Method Get -Headers $headers
    if ($datasources.Count -gt 0) {
        Write-Host "   Datasources configurados:" -ForegroundColor Yellow
        foreach ($ds in $datasources) {
            Write-Host "   - Nombre: $($ds.name) | Tipo: $($ds.type) | URL: $($ds.url)" -ForegroundColor Green
        }
    }
    else {
        Write-Host "   ⚠ No hay datasources configurados en Grafana" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "   Error al conectar con Grafana: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Credenciales: admin/admin" -ForegroundColor Yellow
}

# 5. Verificar aplicación Spring Boot
Write-Host "`n5. Verificar aplicación Spring Boot:" -ForegroundColor Cyan
Write-Host "   - Verificar si está corriendo en puerto 8080:"
$appRunning = Test-NetConnection -ComputerName localhost -Port 8080 -WarningAction SilentlyContinue
if ($appRunning.TcpTestSucceeded) {
    Write-Host "   [OK] Aplicación corriendo en puerto 8080" -ForegroundColor Green
    
    Write-Host "`n   - Verificar endpoint de actuator:"
    try {
        $actuator = Invoke-RestMethod -Uri "http://localhost:8080/actuator" -Method Get
        Write-Host "   ✓ Actuator disponible" -ForegroundColor Green
        Write-Host "   Endpoints: $($actuator._links.Keys -join ', ')"
    }
    catch {
        Write-Host "   ✗ Error al acceder a actuator: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host "`n   - Verificar endpoint de métricas de Prometheus:"
    try {
        $metrics = Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -Method Get -UseBasicParsing
        $metricsLines = $metrics.Content -split "`n" | Where-Object { $_ -match "^[a-z]" } | Select-Object -First 10
        Write-Host "   ✓ Métricas de Prometheus disponibles" -ForegroundColor Green
        Write-Host "   Primeras métricas:" -ForegroundColor Yellow
        $metricsLines | ForEach-Object { Write-Host "     $_" }
    }
    catch {
        Write-Host "   ✗ Error al acceder a métricas: $($_.Exception.Message)" -ForegroundColor Red
    }
}
else {
    Write-Host "   ✗ La aplicación NO está corriendo en puerto 8080" -ForegroundColor Red
    Write-Host "   Para iniciarla ejecuta: .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
}

# 6. Resumen y recomendaciones
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  RESUMEN Y RECOMENDACIONES" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "URLs importantes:" -ForegroundColor Cyan
Write-Host "  - Grafana:        http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host "  - Prometheus:     http://localhost:9090" -ForegroundColor White
Write-Host "  - Redis Insight:  http://localhost:5540" -ForegroundColor White
Write-Host "  - Aplicación:     http://localhost:8080" -ForegroundColor White
Write-Host "  - Swagger:        http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  - Métricas:       http://localhost:8080/actuator/prometheus" -ForegroundColor White

Write-Host "`nPasos siguientes:" -ForegroundColor Cyan
if (-not $appRunning.TcpTestSucceeded) {
    Write-Host "  1. Inicia la aplicación: .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
    Write-Host "  2. Espera 30 segundos para que Prometheus recolecte métricas" -ForegroundColor Yellow
    Write-Host "  3. Configura el datasource en Grafana: http://prometheus:9090" -ForegroundColor Yellow
}
else {
    Write-Host "  1. Verifica que Prometheus esté recolectando datos del inventory-service" -ForegroundColor Yellow
    Write-Host "  2. En Grafana, añade Prometheus como datasource si no existe" -ForegroundColor Yellow
    Write-Host "  3. Crea o importa dashboards para visualizar las métricas" -ForegroundColor Yellow
}

Write-Host "`n"
