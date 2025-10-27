@echo off
REM Script para probar login usando curl (CMD, no PowerShell)

echo Intentando login con newadmin...
curl -X POST ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"newadmin\",\"password\":\"admin123\"}" ^
  http://localhost:8080/api/auth/login

echo.
echo Intentando login con admin...
curl -X POST ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"password\"}" ^
  http://localhost:8080/api/auth/login
