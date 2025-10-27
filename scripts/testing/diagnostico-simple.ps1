# Script de diagnóstico simple para Grafana, Prometheus y Redis

Write-Host "`n========================================"
Write-Host "  DIAGNOSTICO DE MONITOREO Y REDIS"
Write-Host "========================================`n"

# 1. Verificar contenedores Docker
Write-Host "1. Estado de contenedores Docker:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String -Pattern "redis|prometheus|grafana"

# 2. Verificar Redis
Write-Host "`n2. Pruebas de Redis:"
Write-Host "   - Test de conectividad:"
docker exec redis redis-cli PING

Write-Host "`n   - Prueba de escritura/lectura:"
docker exec redis redis-cli SET test_key "Hola_desde_PowerShell" | Out-Null
$value = docker exec redis redis-cli GET test_key
Write-Host "   Valor guardado: $value"
docker exec redis redis-cli DEL test_key | Out-Null

Write-Host "`n   - Keys almacenadas en Redis:"
$keys = docker exec redis redis-cli KEYS "*"
if ($keys) {
    Write-Host "   Keys: $keys"
}
else {
    Write-Host "   No hay keys almacenadas"
}

# 3. Verificar Prometheus
Write-Host "`n3. Pruebas de Prometheus:"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/targets" -Method Get
    $targets = $response.data.activeTargets
    Write-Host "   Targets configurados:"
    foreach ($target in $targets) {
        Write-Host "   - Job: $($target.labels.job)"
        Write-Host "     URL: $($target.scrapeUrl)"
        Write-Host "     Health: $($target.health)"
        if ($target.lastError) {
            Write-Host "     Error: $($target.lastError)"
        }
    }
}
catch {
    Write-Host "   Error al conectar con Prometheus"
}

# 4. Verificar Grafana
Write-Host "`n4. Verificar Grafana:"
Test-NetConnection -ComputerName localhost -Port 3000 -WarningAction SilentlyContinue | Select-Object ComputerName, RemotePort, TcpTestSucceeded

# 5. Verificar aplicación Spring Boot
Write-Host "`n5. Verificar aplicación Spring Boot:"
$appRunning = Test-NetConnection -ComputerName localhost -Port 8080 -WarningAction SilentlyContinue
if ($appRunning.TcpTestSucceeded) {
    Write-Host "   [OK] Aplicación corriendo en puerto 8080"
    
    try {
        $actuator = Invoke-RestMethod -Uri "http://localhost:8080/actuator" -Method Get
        Write-Host "   [OK] Actuator disponible"
        Write-Host "   Endpoints: $($actuator._links.Keys -join ', ')"
    }
    catch {
        Write-Host "   Error al acceder a actuator"
    }
    
    try {
        $metrics = Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -Method Get -UseBasicParsing
        $metricsCount = ($metrics.Content -split "`n" | Where-Object { $_ -match "^[a-z]" }).Count
        Write-Host "   [OK] Metricas de Prometheus disponibles ($metricsCount metricas)"
    }
    catch {
        Write-Host "   Error al acceder a metricas"
    }
}
else {
    Write-Host "   [ERROR] La aplicación NO esta corriendo"
    Write-Host "   Para iniciarla: .\mvnw.cmd spring-boot:run"
}

# 6. URLs importantes
Write-Host "`n========================================"
Write-Host "  URLs IMPORTANTES"
Write-Host "========================================`n"
Write-Host "  - Grafana:        http://localhost:3000 (admin/admin)"
Write-Host "  - Prometheus:     http://localhost:9090"
Write-Host "  - Redis Insight:  http://localhost:5540"
Write-Host "  - Aplicacion:     http://localhost:8080"
Write-Host "  - Swagger:        http://localhost:8080/swagger-ui.html"
Write-Host "  - Metricas:       http://localhost:8080/actuator/prometheus"
Write-Host ""
