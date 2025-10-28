Write-Host "Iniciando entorno de pruebas..."

# 1. Verificar hosts file
if (-not (Select-String -Path "C:\Windows\System32\drivers\etc\hosts" -Pattern "kafka")) {
    Write-Host "Agregando kafka a hosts file..."
    Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "`n127.0.0.1 kafka" -Force
}

# 2. Verificar Docker containers
docker-compose ps
if ($LASTEXITCODE -ne 0) {
    Write-Host "Iniciando containers..."
    docker-compose up -d
    Start-Sleep -Seconds 30  # Esperar que los servicios inicien
}

# 3. Iniciar aplicación
Write-Host "Iniciando aplicación..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev"

# 4. Esperar que la app inicie
Write-Host "Esperando que la aplicación inicie..."
Start-Sleep -Seconds 25

# 5. Verificar que la app responde
try {
    Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health'
    Write-Host "Aplicación lista para pruebas"
}
catch {
    Write-Host "Error: La aplicación no responde"
    exit 1
}
