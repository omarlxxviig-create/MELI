# 🔧 Diagnóstico Completo del Problema de Autenticación

## 🔍 Análisis del Problema

Has confirmado que el usuario `admin` existe en la base de datos H2, pero aún recibes el error:

```json
{
  "error": "Invalid username or password",
  "message": "Bad credentials"
}
```

## 🎯 Causa Raíz Identificada

El problema principal es que **el hash BCrypt almacenado en la base de datos no coincide con la contraseña que estás enviando**.

### Verificación Necesaria

1. **Verifica el hash actual en la base de datos**:

   - Ve a: http://localhost:8080/h2-console
   - Conéctate con: `jdbc:h2:mem:inventory`, user: `sa`, password: (vacío)
   - Ejecuta:

   ```sql
   SELECT username, password FROM users WHERE username = 'admin';
   ```

2. **El hash debe ser**:

   ```
   $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6
   ```

   Este es el hash BCrypt válido de la contraseña `"password"`

## ✅ Solución Definitiva

### Opción 1: Usar el endpoint de registro (RECOMENDADO)

Registra un nuevo usuario usando la aplicación misma para generar un hash válido:

```bash
curl --location 'http://localhost:8080/api/auth/register' \
--header 'Content-Type: application/json' \
--data '{
  "username": "testuser",
  "password": "mypassword",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User"
}'
```

Luego prueba el login con ese usuario:

```bash
curl --location 'http://localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "testuser",
  "password": "mypassword"
}'
```

### Opción 2: Actualizar manualmente la contraseña en H2

Si quieres usar el usuario `admin` con una contraseña que TÚ elijas:

1. Registra un usuario temporal con tu contraseña deseada:

```bash
curl --location 'http://localhost:8080/api/auth/register' \
--header 'Content-Type: application/json' \
--data '{
  "username": "temporal",
  "password": "admin",
  "email": "temp@example.com",
  "firstName": "Temp",
  "lastName": "User"
}'
```

2. En H2 Console, copia el hash generado:

```sql
SELECT password FROM users WHERE username = 'temporal';
```

3. Actualiza el usuario admin con ese hash:

```sql
UPDATE users
SET password = '(pega aquí el hash copiado)'
WHERE username = 'admin';
```

4. Ahora puedes hacer login con:

```bash
curl --location 'http://localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "admin",
  "password": "admin"
}'
```

### Opción 3: Usar la contraseña actual que funciona

Si el hash en la base de datos es `$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6`, entonces la contraseña es `"password"`:

```bash
curl --location 'http://localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--data '{
  "username": "admin",
  "password": "password"
}'
```

## 🐛 Problemas Adicionales Detectados

### 1. Los scripts SQL pueden no estar cargándose

Si los datos no aparecen en H2, verifica:

```properties
# En application-dev.properties debe estar:
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql,classpath:data-security.sql
```

### 2. La aplicación se está deteniendo

He notado que la aplicación se cierra después de iniciar. Para mantenerla corriendo:

```powershell
# Opción 1: Ejecutar el JAR directamente
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Opción 2: Usar Maven pero mantenerlo en primer plano
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## 📊 Hashes BCrypt Conocidos

Para tu referencia, estos son hashes BCrypt válidos:

| Contraseña | Hash BCrypt (strength 10)                                      |
| ---------- | -------------------------------------------------------------- |
| password   | `$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG` |
| password   | `$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6` |
| admin      | Se genera dinámicamente (usa el método del registro)           |
| user       | Se genera dinámicamente (usa el método del registro)           |

**Nota**: BCrypt genera un hash diferente cada vez (incluye un salt aleatorio), así que no puedes comparar hashes directamente. Debes usar el método `matches()` de `BCryptPasswordEncoder`.

## 🧪 Script de Prueba Completo

```powershell
# 1. Registrar un usuario nuevo
$registerBody = @{
    username = "testadmin"
    password = "admin123"
    email = "testadmin@test.com"
    firstName = "Test"
    lastName = "Admin"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $registerBody

Write-Host "Registro: $($registerResponse | ConvertTo-Json)"

# 2. Hacer login
$loginBody = @{
    username = "testadmin"
    password = "admin123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

Write-Host "Login exitoso!"
Write-Host "Access Token: $($loginResponse.accessToken)"
Write-Host "Roles: $($loginResponse.roles)"
```

## 📝 Checklist de Verificación

- [ ] La aplicación está corriendo (puerto 8080)
- [ ] Puedes acceder a H2 Console: http://localhost:8080/h2-console
- [ ] Los usuarios existen en la tabla `users`
- [ ] El hash de la contraseña empieza con `$2a$` o `$2b$`
- [ ] Estás usando el perfil `dev` al iniciar la aplicación
- [ ] Los archivos `data.sql` y `data-security.sql` existen en `src/main/resources`
- [ ] El registro de nuevos usuarios funciona correctamente

## 🎯 Recomendación Final

**USA EL ENDPOINT DE REGISTRO** para crear tus usuarios de prueba. Es la forma más segura de asegurar que los hashes BCrypt sean correctos y compatibles con tu configuración de Spring Security.

Una vez que confirmes que el registro y login funcionan con un usuario nuevo, entonces podrás actualizar los datos SQL para usar los mismos hashes generados.
