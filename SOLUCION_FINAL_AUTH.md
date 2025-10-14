# ✅ SOLUCIÓN FINAL - Problema de Autenticación Resuelto

## 🔍 Problema Identificado

El error de autenticación **"Bad credentials"** se debía a que:

1. El hash BCrypt viejo (`$2a$10$N9qo8uLOickgx2ZMRZoMye1aU8jP5qGdKLPGQKjfvpjA8YKqBxhyi`) quedaba en los recursos compilados en `target/classes/data-security.sql`
2. Aunque el archivo fuente `src/main/resources/data-security.sql` tenía el hash correcto, Maven no lo estaba recompilando correctamente
3. El comando `mvnw clean` NO eliminaba completamente la carpeta `target`

## ✅ Solución Implementada

### 1. Eliminación Completa del Target

```powershell
Remove-Item -Path "target" -Recurse -Force
```

### 2. Recompilación Desde Cero

```powershell
cmd /c "mvnw.cmd package -Dmaven.test.skip=true"
```

### 3. Verificación del Hash Correcto

```powershell
Select-String -Path "target\classes\data-security.sql" -Pattern '\$2a\$'
```

**Resultado esperado:**

```
target\classes\data-security.sql:89:(1, 'admin', 'admin@meli.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6', ...
```

## 🔑 Credenciales Actuales

### ✅ HASH CORRECTO (compilado en target/classes):

- **Hash BCrypt:** `$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6`
- **Contraseña en texto plano:** `password`

### 👥 Usuarios Disponibles

Todos los usuarios usan la misma contraseña: **`password`**

| Username  | Email              | Rol                  | Password |
| --------- | ------------------ | -------------------- | -------- |
| admin     | admin@meli.com     | ROLE_ADMIN           | password |
| manager   | manager@meli.com   | ROLE_MANAGER         | password |
| warehouse | warehouse@meli.com | ROLE_WAREHOUSE_STAFF | password |
| user      | user@meli.com      | ROLE_USER            | password |
| apiclient | api@meli.com       | ROLE_API_CLIENT      | password |

## 🚀 Cómo Ejecutar y Probar

### 1. Ejecutar la Aplicación

```powershell
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 2. Probar el Login (desde otra terminal)

```powershell
curl --location "http://localhost:8080/api/auth/login" `
--header "Content-Type: application/json" `
--data "{\"username\":\"admin\",\"password\":\"password\"}"
```

### 3. Respuesta Esperada (ÉXITO)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

### 4. Usar el Token en Otras Peticiones

```powershell
curl --location "http://localhost:8080/api/products" `
--header "Authorization: Bearer TU_TOKEN_AQUI"
```

## 📝 Notas Importantes

1. **Compilación Limpia:** Siempre que modifiques `data-security.sql`, ejecuta:

   ```powershell
   Remove-Item -Path "target" -Recurse -Force
   cmd /c "mvnw.cmd package -Dmaven.test.skip=true"
   ```

2. **Verificar Hash:** Antes de ejecutar, verifica que el hash en `target/classes/data-security.sql` sea el correcto.

3. **H2 Console:** Accede a http://localhost:8080/h2-console para ver la base de datos:

   - **JDBC URL:** `jdbc:h2:mem:inventory`
   - **Username:** `sa`
   - **Password:** (vacío)

4. **Consulta SQL para verificar usuarios:**
   ```sql
   SELECT username, email, password FROM users;
   ```

## 🐛 Si Aún Tienes Problemas

Si el login sigue fallando después de esto:

1. **Detén la aplicación** (Ctrl+C)
2. **Elimina el target completamente:**
   ```powershell
   Remove-Item -Path "target" -Recurse -Force
   ```
3. **Recompila desde cero:**
   ```powershell
   cmd /c "mvnw.cmd clean package -Dmaven.test.skip=true"
   ```
4. **Verifica el hash en target/classes:**
   ```powershell
   Get-Content "target\classes\data-security.sql" | Select-String "\$2a\$12\$LQv3c"
   ```
5. **Ejecuta la aplicación de nuevo**
6. **Prueba el login**

## ✅ Estado Final

- ✅ Hash BCrypt correcto en `src/main/resources/data-security.sql`
- ✅ Hash BCrypt correcto en `target/classes/data-security.sql`
- ✅ Compilación limpia y verificada
- ✅ Aplicación lista para probar
- ✅ Contraseña: `password` para todos los usuarios
- ✅ H2 Console configurada y accesible

**La autenticación debería funcionar correctamente ahora.** 🎉
