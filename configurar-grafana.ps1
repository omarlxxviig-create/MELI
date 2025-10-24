# Script para configurar Grafana con Prometheus como datasource

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  CONFIGURAR GRAFANA" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

$grafanaUrl = "http://localhost:3000"
$user = "admin"
$pass = "admin"

# Crear credenciales Basic Auth
$pair = "$($user):$($pass)"
$encodedCreds = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes($pair))
$headers = @{
    Authorization  = "Basic $encodedCreds"
    "Content-Type" = "application/json"
}

# 1. Verificar si Grafana está disponible
Write-Host "1. Verificando conexión con Grafana..." -ForegroundColor Cyan
try {
    $grafanaHealth = Invoke-RestMethod -Uri "$grafanaUrl/api/health" -Method Get
    Write-Host "   [OK] Grafana está disponible" -ForegroundColor Green
}
catch {
    Write-Host "   [ERROR] No se puede conectar a Grafana: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Asegúrate de que Grafana esté corriendo: docker ps | findstr grafana" -ForegroundColor Yellow
    exit 1
}

# 2. Verificar datasources existentes
Write-Host "`n2. Verificando datasources existentes..." -ForegroundColor Cyan
try {
    $datasources = Invoke-RestMethod -Uri "$grafanaUrl/api/datasources" -Method Get -Headers $headers
    
    $prometheusDs = $datasources | Where-Object { $_.type -eq "prometheus" }
    
    if ($prometheusDs) {
        Write-Host "   [INFO] Ya existe un datasource de Prometheus:" -ForegroundColor Yellow
        foreach ($ds in $prometheusDs) {
            Write-Host "     - Nombre: $($ds.name) | URL: $($ds.url)" -ForegroundColor White
        }
        
        $response = Read-Host "`n   ¿Deseas crear un nuevo datasource de todas formas? (s/n)"
        if ($response -ne "s" -and $response -ne "S") {
            Write-Host "   Operación cancelada" -ForegroundColor Yellow
            exit 0
        }
    }
    else {
        Write-Host "   [INFO] No hay datasources de Prometheus configurados" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "   [WARNING] Error al obtener datasources: $($_.Exception.Message)" -ForegroundColor Yellow
}

# 3. Crear nuevo datasource de Prometheus
Write-Host "`n3. Creando datasource de Prometheus..." -ForegroundColor Cyan

$datasourceConfig = @{
    name      = "Prometheus-Inventory-Service"
    type      = "prometheus"
    url       = "http://prometheus:9090"
    access    = "proxy"
    isDefault = $true
    jsonData  = @{
        httpMethod   = "POST"
        timeInterval = "5s"
    }
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "$grafanaUrl/api/datasources" -Method Post -Headers $headers -Body $datasourceConfig
    Write-Host "   [OK] Datasource creado exitosamente" -ForegroundColor Green
    Write-Host "     - ID: $($result.id)" -ForegroundColor White
    Write-Host "     - Nombre: $($result.name)" -ForegroundColor White
    Write-Host "     - URL: $($result.url)" -ForegroundColor White
}
catch {
    $errorMessage = $_.Exception.Message
    if ($errorMessage -like "*already exists*") {
        Write-Host "   [INFO] El datasource ya existe" -ForegroundColor Yellow
    }
    else {
        Write-Host "   [ERROR] Error al crear datasource: $errorMessage" -ForegroundColor Red
        exit 1
    }
}

# 4. Verificar la conexión del datasource
Write-Host "`n4. Verificando conexión con Prometheus..." -ForegroundColor Cyan
Start-Sleep -Seconds 2

try {
    $datasources = Invoke-RestMethod -Uri "$grafanaUrl/api/datasources" -Method Get -Headers $headers
    $promDs = $datasources | Where-Object { $_.type -eq "prometheus" } | Select-Object -First 1
    
    if ($promDs) {
        $testUrl = "$grafanaUrl/api/datasources/$($promDs.id)/health"
        $healthCheck = Invoke-RestMethod -Uri $testUrl -Method Get -Headers $headers
        
        if ($healthCheck.status -eq "OK") {
            Write-Host "   [OK] Conexión con Prometheus exitosa" -ForegroundColor Green
        }
        else {
            Write-Host "   [WARNING] Estado de conexión: $($healthCheck.status)" -ForegroundColor Yellow
        }
    }
}
catch {
    Write-Host "   [WARNING] No se pudo verificar la conexión: $($_.Exception.Message)" -ForegroundColor Yellow
}

# 5. Resumen
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  RESUMEN" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Grafana está configurado y listo para usar!" -ForegroundColor Cyan
Write-Host "`nPróximos pasos:" -ForegroundColor Cyan
Write-Host "  1. Abre Grafana: http://localhost:3000" -ForegroundColor White
Write-Host "  2. Inicia sesión con: admin / admin" -ForegroundColor White
Write-Host "  3. Ve a 'Dashboards' > 'New' > 'New Dashboard'" -ForegroundColor White
Write-Host "  4. Agrega un panel y selecciona métricas de 'Prometheus-Inventory-Service'" -ForegroundColor White
Write-Host "`nMétricas disponibles de tu aplicación:" -ForegroundColor Cyan
Write-Host "  - http_server_requests_seconds_count" -ForegroundColor White
Write-Host "  - jvm_memory_used_bytes" -ForegroundColor White
Write-Host "  - system_cpu_usage" -ForegroundColor White
Write-Host "  - process_uptime_seconds" -ForegroundColor White
Write-Host ""
