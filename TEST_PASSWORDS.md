# Test de Contraseñas BCrypt

## Hashes Encontrados

### Hash en el ERROR (el que está en la DB actual):

```
$2a$10$N9qo8uLOickgx2ZMRZoMye1aU8jP5qGdKLPGQKjfvpjA8YKqBxhyi
```

### Hash en data-security.sql actual:

```
$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6
```

## Pruebas a realizar

1. Login con admin/password
2. Login con admin/password123
3. Login con admin/admin123

## Solución

El problema es que el hash viejo (`$2a$10$...`) está quedando en cache o en los recursos compilados.

### Pasos:

1. **Limpiar completamente el proyecto:**

   ```powershell
   cmd /c "mvnw.cmd clean"
   Remove-Item -Path "target" -Recurse -Force -ErrorAction SilentlyContinue
   ```

2. **Compilar de nuevo:**

   ```powershell
   cmd /c "mvnw.cmd clean package -Dmaven.test.skip=true"
   ```

3. **Ejecutar la aplicación:**

   ```powershell
   java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
   ```

4. **Probar login con admin/password:**
   ```powershell
   curl --location "http://localhost:8080/api/auth/login" --header "Content-Type: application/json" --data "{\"username\":\"admin\",\"password\":\"password\"}"
   ```
