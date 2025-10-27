# Scripts

Esta carpeta contiene todos los scripts de automatización del proyecto.

## Estructura

- **setup/** - Scripts de configuración inicial y setup del ambiente

  - `configurar-grafana.ps1` - Configura Grafana automáticamente
  - `configure-powershell.ps1` - Configura PowerShell para el proyecto
  - `fix-login-powershell.ps1` - Corrige problemas de login

- **testing/** - Scripts de prueba y diagnóstico
  - `curl-login.cmd` - Prueba de login usando curl (CMD)
  - `diagnostico-simple.ps1` - Diagnóstico simple del sistema
  - `generar-metricas.ps1` - Genera métricas de prueba
  - `login-test.ps1` - Prueba de funcionalidad de login
  - `test-login.ps1` - Otro script de prueba de login
  - `test-monitoring.ps1` - Prueba el sistema de monitoreo

## Script Principal

- **start-app.ps1** - Inicia la aplicación con todos sus servicios

## Uso

Para ejecutar cualquier script PowerShell:

```powershell
.\nombre-del-script.ps1
```

Para ejecutar scripts CMD:

```cmd
nombre-del-script.cmd
```
