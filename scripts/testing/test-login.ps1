# Script para probar login con PowerShell

# 1. Crear correctamente el objeto JSON de login
$loginBody = @{
    username = "newadmin"
    password = "admin123"
} | ConvertTo-Json

Write-Host "Intentando login con newadmin/admin123..." -ForegroundColor Yellow

# 2. Realizar la petición correctamente
try {
    $response = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop

    Write-Host "✅ Login exitoso!" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 4)
    
    # Guardar el token para usarlo en otras peticiones
    $token = $response.accessToken
    Write-Host "`nToken para usar en otras peticiones:" -ForegroundColor Cyan
    Write-Host "Authorization: Bearer $token"
    
}
catch {
    Write-Host "❌ Error de login: $_" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        $reader.Close()
        
        Write-Host "Respuesta del servidor:" -ForegroundColor Red
        Write-Host $responseBody -ForegroundColor Red
    }
}
