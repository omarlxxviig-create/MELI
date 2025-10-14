# JWT Authentication & RBAC - Guía de Uso

## 📋 Resumen

Se ha implementado exitosamente un sistema completo de autenticación JWT con control de acceso basado en roles (RBAC) en el servicio de inventario.

## 🔑 Credenciales de Prueba

| Usuario     | Contraseña     | Rol                  | Descripción                                 |
| ----------- | -------------- | -------------------- | ------------------------------------------- |
| `admin`     | `admin123`     | ROLE_ADMIN           | Acceso total al sistema                     |
| `manager`   | `manager123`   | ROLE_MANAGER         | Gestión de inventario, productos y reportes |
| `warehouse` | `warehouse123` | ROLE_WAREHOUSE_STAFF | Gestión de inventario básico                |
| `user`      | `user123`      | ROLE_USER            | Solo lectura                                |
| `apiclient` | `api123`       | ROLE_API_CLIENT      | Cliente API                                 |

## 🚀 Cómo Probar

### 1. Acceder a Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### 2. Login (Obtener Token JWT)

**Endpoint:** `POST /api/auth/login`

**Request Body:**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "email": "admin@meli.com",
  "roles": ["ROLE_ADMIN"]
}
```

### 3. Usar el Token en Requests

#### Opción A: En Swagger UI

1. Copia el `accessToken` de la respuesta del login
2. Click en el botón **"Authorize"** (🔓) en la parte superior de Swagger
3. Ingresa: `Bearer <tu_access_token>`
4. Click en "Authorize"
5. Ahora puedes usar cualquier endpoint protegido

#### Opción B: En Postman o cURL

**Header:**

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Ejemplo cURL:**

```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 4. Registrar Nuevo Usuario

**Endpoint:** `POST /api/auth/register`

**Request Body:**

```json
{
  "username": "testuser",
  "email": "test@meli.com",
  "password": "Test123!",
  "firstName": "Test",
  "lastName": "User",
  "roles": ["ROLE_USER"]
}
```

### 5. Renovar Token (Refresh)

**Endpoint:** `POST /api/auth/refresh`

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 6. Obtener Usuario Actual

**Endpoint:** `GET /api/auth/me`

**Requiere:** Token JWT válido en el header `Authorization`

## 🛡️ Permisos por Rol

### ROLE_ADMIN

- ✅ Acceso total a todos los endpoints
- ✅ Gestión de usuarios
- ✅ Gestión de inventario, productos y reportes
- ✅ Configuración del sistema

### ROLE_MANAGER

- ✅ Gestión completa de inventario y productos
- ✅ Lectura de usuarios
- ✅ Generación de reportes
- ❌ No puede gestionar usuarios ni roles

### ROLE_WAREHOUSE_STAFF

- ✅ Lectura, creación y actualización de inventario
- ✅ Reserva y liberación de inventario
- ✅ Lectura y creación de productos
- ❌ No puede eliminar productos ni gestionar usuarios

### ROLE_USER

- ✅ Solo lectura de inventario y productos
- ❌ No puede modificar nada

### ROLE_API_CLIENT

- ✅ Acceso completo a API de inventario y productos
- ❌ No puede gestionar usuarios ni roles

## 📊 Configuración de Seguridad

### Endpoints Públicos (No requieren autenticación)

- `/api/auth/**` - Login, registro, refresh
- `/swagger-ui/**` - Documentación Swagger
- `/actuator/**` - Métricas y health checks
- `/h2-console/**` - Consola H2

### Endpoints Protegidos

#### Inventario (`/api/inventory/**`)

- **GET**: Requiere `USER`, `WAREHOUSE_STAFF`, `MANAGER` o `ADMIN`
- **POST/PUT**: Requiere `WAREHOUSE_STAFF`, `MANAGER` o `ADMIN`
- **DELETE**: Requiere `MANAGER` o `ADMIN`

#### Productos (`/api/products/**`)

- **GET**: Requiere `USER`, `WAREHOUSE_STAFF`, `MANAGER` o `ADMIN`
- **POST/PUT**: Requiere `MANAGER` o `ADMIN`
- **DELETE**: Solo `ADMIN`

#### Usuarios (`/api/users/**`)

- **Todos los métodos**: Solo `ADMIN`

#### Reportes (`/api/reports/**`)

- **Todos los métodos**: Requiere `MANAGER` o `ADMIN`

## 🔧 Configuración JWT

**Archivo:** `application.yml`

```yaml
jwt:
  secret: "your-secret-key-should-be-at-least-512-bits-long-for-HS512-algorithm"
  expirationMs: 86400000 # 24 horas
  refreshExpirationMs: 604800000 # 7 días
  issuer: "meli-inventory-service"
  tokenPrefix: "Bearer "
  headerString: "Authorization"
```

## 🗄️ Datos de Prueba

Los datos de seguridad se cargan automáticamente desde:

- `src/main/resources/data-security.sql`

Incluye:

- 22 permisos granulares
- 5 roles predefinidos
- 5 usuarios de prueba
- Asignación de roles y permisos

## 🧪 Ejemplos de Prueba

### 1. Login como Admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Obtener Información del Usuario Actual

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <token>"
```

### 3. Intentar Acceder sin Token (debe fallar)

```bash
curl -X GET http://localhost:8080/api/inventory
# Debe retornar 401 Unauthorized
```

### 4. Acceder con Token Válido

```bash
curl -X GET http://localhost:8080/api/inventory \
  -H "Authorization: Bearer <token>"
# Debe retornar 200 OK con los datos
```

## 🔍 Troubleshooting

### Error: "Invalid username or password"

**Posibles causas:**

1. **Usuario no existe en la base de datos**

   - Verifica que el archivo `data-security.sql` se esté cargando
   - Revisa los logs al iniciar la aplicación
   - Consulta: `SELECT * FROM users WHERE username = 'admin';`

2. **Contraseña incorrecta**

   - Las contraseñas deben estar hasheadas con BCrypt
   - Formato: `$2a$10$...`
   - Asegúrate de usar las credenciales correctas (ver tabla arriba)

3. **Base de datos no inicializada**
   - Verifica `application.yml`:
   ```yaml
   spring:
     sql:
       init:
         mode: always
   ```

### Verificar Usuarios Creados

```bash
# Si usas H2 Console (http://localhost:8080/h2-console)
SELECT u.username, u.email, u.enabled
FROM users u;

# Ver roles asignados
SELECT u.username, r.name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
```

### Recrear Usuarios Manualmente

Si los usuarios no existen, puedes crearlos con este script SQL:

```sql
-- Eliminar usuarios existentes (opcional)
DELETE FROM user_roles;
DELETE FROM users;

-- Crear usuario admin
-- Contraseña: admin123 (BCrypt hash)
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, created_at, updated_at)
VALUES (1, 'admin', 'admin@meli.com', '$2a$10$YourBCryptHashHere', 'Admin', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Asignar rol ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT 1, id FROM roles WHERE name = 'ROLE_ADMIN';
```

### Generar Hash BCrypt para Contraseñas

Puedes usar este código Java para generar hashes:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("manager123: " + encoder.encode("manager123"));
        System.out.println("warehouse123: " + encoder.encode("warehouse123"));
        System.out.println("user123: " + encoder.encode("user123"));
        System.out.println("api123: " + encoder.encode("api123"));
    }
}
```

### Logs Útiles

Revisa los logs de la aplicación para:

```
# Usuario encontrado pero contraseña incorrecta
WARN: Bad credentials for user: admin

# Usuario no encontrado
WARN: User not found: admin

# Carga de datos exitosa
INFO: Loaded XX users from data-security.sql
```

## 🛠️ Compilar y Ejecutar

```powershell
# Limpiar base de datos y recompilar
cmd /c "mvnw.cmd clean package -Dmaven.test.skip=true"

# Ejecutar con perfil dev (carga datos de prueba)
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Verificar que los datos se cargaron
# Revisa los logs para: "Executing SQL script from URL..."
```

## 📝 Notas Importantes

1. **Contraseñas**: Las contraseñas en `data-security.sql` DEBEN estar hasheadas con BCrypt
2. **Tokens JWT**: Los access tokens expiran en 24 horas, los refresh tokens en 7 días
3. **Algoritmo**: HS512 (HMAC con SHA-512)
4. **Secret Key**: Debe cambiarse en producción a una clave segura de al menos 512 bits
5. **Stateless**: No se mantiene sesión en el servidor, todo está en el token
6. **CORS**: Configurar según sea necesario para aplicaciones frontend
7. **Inicialización de Datos**: Asegúrate que `spring.sql.init.mode=always` esté configurado

## 🔒 Seguridad en Producción

Para producción, asegúrate de:

1. ✅ Cambiar `jwt.secret` a un valor aleatorio y seguro
2. ✅ Usar HTTPS para todas las comunicaciones
3. ✅ Implementar rate limiting en endpoints de autenticación
4. ✅ Habilitar CORS solo para dominios confiables
5. ✅ Cambiar las contraseñas por defecto
6. ✅ Implementar políticas de contraseñas fuertes
7. ✅ Considerar almacenar refresh tokens en base de datos
8. ✅ Implementar revocación de tokens
9. ✅ Agregar logging de intentos de autenticación
10. ✅ Implementar 2FA para usuarios administrativos

## 📞 Soporte

Para problemas o preguntas sobre la implementación JWT/RBAC, consulta:

- Código fuente en `src/main/java/com/meli/inventory_service/infrastructure/security/`
- Configuración en `SecurityConfig.java`
- DTOs de autenticación en `infrastructure/rest/dto/`

---

✅ **Sistema JWT y RBAC completamente funcional e implementado**
