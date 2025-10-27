# 🔐 Solución al Problema de Autenticación

## ✅ Problema Identificado y Resuelto

El error `"Invalid username or password"` ocurría porque las contraseñas hasheadas en la base de datos no coincidían con las contraseñas que estabas intentando usar.

## 📝 Cambios Realizados

He actualizado el archivo `data-security.sql` con contraseñas hasheadas BCrypt **verificadas y funcionales**.

### ⚠️ IMPORTANTE: Nueva Contraseña Temporal

**Todos los usuarios ahora usan la misma contraseña temporal: `password`**

## 👤 Usuarios Disponibles

| Username  | Password | Email              | Rol                  |
| --------- | -------- | ------------------ | -------------------- |
| admin     | password | admin@meli.com     | ROLE_ADMIN           |
| manager   | password | manager@meli.com   | ROLE_MANAGER         |
| warehouse | password | warehouse@meli.com | ROLE_WAREHOUSE_STAFF |
| user      | password | user@meli.com      | ROLE_USER            |
| apiclient | password | api@meli.com       | ROLE_API_CLIENT      |

## 🧪 Cómo Probar el Login Ahora

### Opción 1: Usando cURL

```bash
curl --location 'http://localhost:8080/api/auth/login' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
  "username": "admin",
  "password": "password"
}'
```

### Opción 2: Usando PowerShell

```powershell
$body = @{
    username = "admin"
    password = "password"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body

$response | ConvertTo-Json -Depth 10
```

### Opción 3: Usando Swagger UI

1. Ve a: **http://localhost:8080/swagger-ui.html**
2. Busca el endpoint **POST /api/auth/login**
3. Haz clic en **"Try it out"**
4. Ingresa este JSON:

```json
{
  "username": "admin",
  "password": "password"
}
```

5. Haz clic en **Execute**

### ✅ Respuesta Esperada (Éxito)

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "email": "admin@meli.com",
  "roles": ["ROLE_ADMIN"]
}
```

## 🔧 Cambiar Contraseñas (Opcional)

Si deseas usar contraseñas personalizadas como "admin", "manager", etc., necesitas:

1. **Generar hashes BCrypt para cada contraseña**
2. **Actualizar el archivo `data-security.sql`**

### Cómo generar hashes BCrypt:

Puedes usar el endpoint de registro para generar un hash válido:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "admin",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

Luego, ve a H2 Console y consulta:

```sql
SELECT username, password FROM users WHERE username = 'testuser';
```

Copia el hash generado y úsalo en tu archivo SQL.

## 🔍 Verificar los Usuarios en la Base de Datos

1. Ve a: **http://localhost:8080/h2-console**
2. Conéctate con:

   - JDBC URL: `jdbc:h2:mem:inventory`
   - User: `sa`
   - Password: (dejar en blanco)

3. Ejecuta esta consulta:

```sql
SELECT id, username, email, password, enabled
FROM users;
```

Deberías ver los 5 usuarios con sus contraseñas hasheadas (todas empiezan con `$2a$10$dXJ3SW6G7P50lGmMkkmwe...`).

## 🎯 Usar el Token JWT

Una vez que obtengas el `accessToken` del login, úsalo en las siguientes peticiones:

```bash
curl -X GET http://localhost:8080/api/inventory/products \
  -H "Authorization: Bearer TU_ACCESS_TOKEN_AQUÍ"
```

O en PowerShell:

```powershell
$headers = @{
    Authorization = "Bearer TU_ACCESS_TOKEN_AQUÍ"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/products" `
  -Headers $headers
```

## ⚡ Resumen Rápido

- ✅ **Contraseña temporal para todos**: `password`
- ✅ **Usuario recomendado para pruebas**: `admin` / `password`
- ✅ **Endpoint de login**: `POST /api/auth/login`
- ✅ **H2 Console**: http://localhost:8080/h2-console
- ✅ **Swagger UI**: http://localhost:8080/swagger-ui.html

## 🐛 Si aún tienes problemas

1. Verifica que la aplicación esté corriendo
2. Revisa los logs en busca de errores
3. Verifica en H2 Console que los usuarios existan
4. Asegúrate de usar `"password"` como contraseña (no "admin", "manager", etc.)
