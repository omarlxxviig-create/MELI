# Script para probar login usando PowerShell correctamente

Write-Host "1. Creando objeto de credenciales..." -ForegroundColor Yellow
$body = @{
    username = "testadmin"
    password = "admin123"
} | ConvertTo-Json

Write-Host "2. Intentando login con testadmin/admin123..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop

    Write-Host "✅ Login exitoso!" -ForegroundColor Green
    $loginResponse | ConvertTo-Json
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Código de estado HTTP: $statusCode" -ForegroundColor Red
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            $reader.Close()
            
            Write-Host "Respuesta del servidor:" -ForegroundColor Red
            Write-Host $responseBody -ForegroundColor Red
        }
        catch {
            Write-Host "No se pudo leer la respuesta" -ForegroundColor Red
        }
    }
    
    Write-Host "`n🔍 Verificando usuario en la base de datos..." -ForegroundColor Yellow
    Write-Host "Por favor accede a http://localhost:8080/h2-console y ejecuta:"
    Write-Host "SELECT username, email, password FROM users WHERE username = 'testadmin';" -ForegroundColor Cyan
}

# También intentemos probar con el usuario original admin/password
Write-Host "`n3. Intentando login con admin/password..." -ForegroundColor Yellow
$adminBody = @{
    username = "admin"
    password = "password"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $adminBody `
        -ErrorAction Stop
    
    Write-Host "✅ Login exitoso con admin!" -ForegroundColor Green
    $adminResponse | ConvertTo-Json
}
catch {
    Write-Host "❌ Error con admin: $_" -ForegroundColor Red
}
