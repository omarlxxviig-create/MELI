# Script para configurar permisos de PowerShell
# Ejecutar UNA VEZ como Administrador

Write-Host "Configurando politica de ejecucion de PowerShell..." -ForegroundColor Green

# Establecer politica de ejecucion para el usuario actual
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force

Write-Host "¡Configuracion completada!" -ForegroundColor Green
Write-Host "Ahora puedes ejecutar scripts sin problemas." -ForegroundColor Yellow
Write-Host ""
Write-Host "Si aun ves solicitudes de ALLOW en Copilot:" -ForegroundColor Cyan
Write-Host "1. Cierra y reabre VS Code" -ForegroundColor White
Write-Host "2. Verifica que este workspace este marcado como 'Confiable'" -ForegroundColor White
Write-Host "   (File > Trust Workspace)" -ForegroundColor White

pause
