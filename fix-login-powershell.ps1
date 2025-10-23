# Script para probar login con PowerShell correctamente

Write-Host "1. Intentando login con testadmin/admin123..." -ForegroundColor Yellow

try {
    $loginBody = @{
        username = "testadmin"
        password = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    Write-Host "✅ Login exitoso!" -ForegroundColor Green
    Write-Host "Token: $($loginResponse.accessToken)" -ForegroundColor Cyan
    
    # Guardar el token para usarlo después
    $token = $loginResponse.accessToken
    
    # Probar endpoint protegido
    Write-Host "`n2. Probando endpoint protegido con el token..." -ForegroundColor Yellow
    
    $headers = @{
        Authorization = "Bearer $token"
    }
    
    $me = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/auth/me" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
        
    Write-Host "✅ Datos del usuario:" -ForegroundColor Green
    $me | ConvertTo-Json
    
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        $reader.Close()
        
        Write-Host "Detalle del error:" -ForegroundColor Red
        Write-Host $responseBody -ForegroundColor Red
    }
}
