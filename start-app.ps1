# Script para iniciar la aplicación y verificar el estado
Write-Host "Iniciando aplicación Spring Boot..." -ForegroundColor Cyan

# Iniciar la aplicación en background usando Start-Process
$process = Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -NoNewWindow -PassThru

Write-Host "Proceso iniciado con PID: $($process.Id)" -ForegroundColor Green
Write-Host "Esperando 60 segundos para que la aplicación arranque..." -ForegroundColor Yellow

# Esperar y verificar cada 10 segundos
for ($i = 1; $i -le 6; $i++) {
    Start-Sleep -Seconds 10
    $testConnection = Test-NetConnection -ComputerName localhost -Port 8080 -WarningAction SilentlyContinue
    
    if ($testConnection.TcpTestSucceeded) {
        Write-Host "`n[OK] Aplicación iniciada correctamente en el puerto 8080!" -ForegroundColor Green
        
        # Verificar endpoint de actuator
        Start-Sleep -Seconds 5
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
            Write-Host "[OK] Health check: $($health.status)" -ForegroundColor Green
        }
        catch {
            Write-Host "[WARNING] No se pudo verificar el health check" -ForegroundColor Yellow
        }
        
        # Verificar métricas
        try {
            $metricsResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/prometheus" -Method Get -UseBasicParsing
            Write-Host "[OK] Endpoint de métricas disponible" -ForegroundColor Green
            Write-Host "`nLa aplicación está lista. URLs importantes:" -ForegroundColor Cyan
            Write-Host "  - Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor White
            Write-Host "  - Métricas: http://localhost:8080/actuator/prometheus" -ForegroundColor White
            Write-Host "  - Health: http://localhost:8080/actuator/health" -ForegroundColor White
        }
        catch {
            Write-Host "[WARNING] No se pudo verificar el endpoint de métricas" -ForegroundColor Yellow
        }
        
        Write-Host "`nEspera 30 segundos más para que Prometheus empiece a recolectar datos..." -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host "  Intento $i/6: La aplicación aún no está lista..." -ForegroundColor Yellow
}

Write-Host "`n[ERROR] La aplicación no se inició en el tiempo esperado" -ForegroundColor Red
Write-Host "Verifica los logs en la otra terminal" -ForegroundColor Yellow
