# 🔍 Instrucciones para Acceder a H2 Console

## ✅ La aplicación está corriendo correctamente

### 📌 Paso 1: Acceder a H2 Console

1. Abre tu navegador web
2. Ve a: **http://localhost:8080/h2-console**

### 📌 Paso 2: Configurar la conexión

En la pantalla de login de H2, usa estos datos:

```
Driver Class: org.h2.Driver
JDBC URL: jdbc:h2:mem:inventory
User Name: sa
Password: (dejar en blanco)
```

**⚠️ IMPORTANTE**: El nombre de la base de datos es **`inventory`**, NO `testdb`

### 📌 Paso 3: Verificar los usuarios registrados

Una vez conectado, ejecuta esta consulta SQL para ver todos los usuarios:

```sql
SELECT * FROM users;
```

### 👥 Usuarios disponibles para autenticación

Según los scripts SQL cargados, estos son los usuarios disponibles:

| Username  | Password | Email              | Role                 |
| --------- | -------- | ------------------ | -------------------- |
| admin     | admin    | admin@meli.com     | ROLE_ADMIN           |
| manager   | manager  | manager@meli.com   | ROLE_MANAGER         |
| warehouse | user     | warehouse@meli.com | ROLE_WAREHOUSE_STAFF |
| user      | user     | user@meli.com      | ROLE_USER            |
| apiclient | user     | api@meli.com       | ROLE_API_CLIENT      |

**Nota**: Las contraseñas están hasheadas con BCrypt en la base de datos, pero las contraseñas en texto plano son las indicadas arriba.

### 🔐 Probar la autenticación

#### Opción 1: Usando Swagger UI

1. Ve a: **http://localhost:8080/swagger-ui.html**
2. Busca el endpoint `/api/v1/auth/login`
3. Usa este JSON para probar:

```json
{
  "username": "admin",
  "password": "admin"
}
```

#### Opción 2: Usando cURL

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```

#### Opción 3: Usando PowerShell

```powershell
$body = @{
    username = "admin"
    password = "admin"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

### 🔍 Consultas útiles en H2 Console

```sql
-- Ver todos los usuarios
SELECT * FROM users;

-- Ver todos los roles
SELECT * FROM roles;

-- Ver los permisos de cada rol
SELECT r.name as role_name, p.name as permission_name
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON p.id = rp.permission_id
ORDER BY r.name, p.name;

-- Ver los roles asignados a cada usuario
SELECT u.username, u.email, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
ORDER BY u.username;

-- Ver productos
SELECT * FROM product;

-- Ver inventario
SELECT * FROM store_inventory;
```

### ⚠️ Solución de problemas

#### Si no puedes conectarte a H2 Console:

1. **Verifica que la aplicación esté corriendo**:
   - Deberías ver en los logs: `H2 console available at '/h2-console'`
2. **Verifica el puerto**:

   - La aplicación corre en el puerto 8080
   - URL: http://localhost:8080/h2-console

3. **Verifica el nombre de la base de datos**:
   - El nombre correcto es `inventory`
   - JDBC URL completa: `jdbc:h2:mem:inventory`

#### Si la autenticación no funciona:

1. **Verifica que los datos se cargaron correctamente**:

   ```sql
   SELECT COUNT(*) FROM users;
   ```

   Deberías ver 5 usuarios.

2. **Verifica las contraseñas hasheadas**:

   ```sql
   SELECT username, password FROM users WHERE username = 'admin';
   ```

3. **Revisa los logs de la aplicación** para ver errores de autenticación.

### 📝 Notas adicionales

- La base de datos H2 es **en memoria**, por lo que todos los datos se perderán cuando detengas la aplicación.
- Los scripts `data.sql` y `data-security.sql` se ejecutan automáticamente al iniciar la aplicación.
- El perfil `dev` está configurado para recargar los datos cada vez que inicias la aplicación.
