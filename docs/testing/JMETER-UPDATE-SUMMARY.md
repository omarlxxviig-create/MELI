# 🔄 Actualización de JMeter - Resumen de Cambios

**Fecha**: 27 de Octubre, 2025  
**Tarea**: Actualización de tests JMeter para sistema de transporte

---

## ✅ Archivos Actualizados

### 1. **Jmeter.jmx** - Test Plan Principal

### 2. **SmokeTest.jmx** - Smoke Test

---

## 📝 Cambios Realizados

### Endpoints Actualizados

| Antes (Inventario)       | Después (Transporte)     | Método |
| ------------------------ | ------------------------ | ------ |
| `/api/inventory/reserve` | `/api/reservations`      | POST   |
| `/api/inventory/commit`  | `/api/reservations/{id}` | GET    |

### Payloads Actualizados

#### ❌ ANTES (Sistema de Inventario):

```json
{
  "storeId": "store-001",
  "productId": "prod-001",
  "quantity": 1,
  "transactionId": "${transaction_id}"
}
```

#### ✅ DESPUÉS (Sistema de Transporte):

```json
{
  "servicePostId": 1,
  "userId": 2,
  "seatsRequested": 1,
  "pickupLocation": "Punto A",
  "dropoffLocation": "Punto B"
}
```

### Variables JSON Actualizadas

| Antes                | Después                                       | Descripción                             |
| -------------------- | --------------------------------------------- | --------------------------------------- |
| `$.reservationId`    | `$.id`                                        | ID de la reserva creada                 |
| "Insufficient stock" | "No seats available" / "asientos disponibles" | Mensaje de error                        |
| HTTP 409 (solo)      | HTTP 400 / 409                                | Códigos de error para asientos agotados |

### Nombres de Pasos Actualizados

| Antes                     | Después                      |
| ------------------------- | ---------------------------- |
| "Reservar"                | "Crear Reserva"              |
| "Confirmar"               | "Consultar Reserva"          |
| "Detener si no hay stock" | "Detener si no hay asientos" |
| "Agotar Inventario"       | "Agotar Asientos"            |

---

## 🎯 Títulos de Test Plans

### Jmeter.jmx

- **Antes**: "Inventory Service - Test Plan"
- **Después**: "Transport Booking Service - Test Plan"
- **Descripción**: "Plan para probar reservas de asientos en servicios de transporte"

### SmokeTest.jmx

- **Antes**: "Inventory Service - Smoke Test"
- **Después**: "Transport Booking Service - Smoke Test"
- **Descripción**: "Prueba para agotar asientos disponibles y verificar manejo de capacidad"

---

## ⚠️ Notas Importantes

### 1. **Datos de Prueba Requeridos**

Para que los tests funcionen correctamente, necesitas:

- **Service Post existente con ID 1**:

  ```sql
  -- Debe existir en la BD antes de ejecutar el test
  INSERT INTO service_posts (id, driver_id, origin, destination, departure_time, available_seats, price_per_seat, status)
  VALUES (1, 1, 'Ciudad A', 'Ciudad B', CURRENT_TIMESTAMP + INTERVAL '1 DAY', 50, 25.50, 'ACTIVE');
  ```

- **Usuarios registrados**:
  - Los usuarios del archivo `test-users.csv` deben existir
  - Usuario con ID 2 debe existir para las reservas

### 2. **Comportamiento del Test**

#### **Jmeter.jmx** (Load Test):

- 500 usuarios concurrentes
- Cada usuario hace login una vez
- Cada usuario realiza 20 ciclos de:
  1. Crear reserva de 1 asiento
  2. Consultar estado de la reserva
- Target: 10,000 transacciones por minuto (166 req/sec)
- Se detiene cuando no hay asientos disponibles

#### **SmokeTest.jmx** (Smoke Test):

- 1 solo usuario
- Reserva asientos de 1 en 1 hasta agotarlos
- Espera 500ms entre cada reserva
- Muestra total de asientos reservados al final
- Útil para verificar control de capacidad

### 3. **Logs y Mensajes**

Los mensajes de log ahora usan terminología de transporte:

- "No hay asientos disponibles"
- "Asientos agotados"
- "Total de asientos reservados"

### 4. **Respuestas HTTP Esperadas**

| Escenario           | Código    | Acción         |
| ------------------- | --------- | -------------- |
| Reserva exitosa     | 200 o 201 | Continuar      |
| No hay asientos     | 400 o 409 | Detener thread |
| Error de validación | 400       | Detener thread |

---

## 🚀 Cómo Ejecutar

### Antes de Ejecutar

1. **Iniciar la aplicación**:

   ```powershell
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Verificar que existan datos de prueba**:

   - Service Post con ID 1 y asientos disponibles
   - Usuarios en la base de datos

3. **Asegurarse que test-users.csv tenga usuarios válidos**

4. **Verificar configuración de seguridad**:
   - CSRF debe estar deshabilitado o configurado correctamente en JMeter
   - Cookie Manager debe estar activo en JMeter
   - Session persistence debe estar habilitada

### Ejecutar Load Test (GUI)

```bash
jmeter -t docs/testing/Jmeter.jmx
```

### Ejecutar Load Test (CLI)

```bash
jmeter -n -t docs/testing/Jmeter.jmx -l results.jtl -e -o report/
```

### Ejecutar Smoke Test

```bash
jmeter -n -t docs/testing/SmokeTest.jmx -l smoke-results.jtl
```

---

## 🔧 Troubleshooting

### ❌ Error: 403 Forbidden al Crear Reserva

**Síntomas**:

```
Response code: 403
Response message: (vacío)
Body size: 0 bytes
```

**Causas Comunes**:

1. **Falta de autenticación/autorización**

   - El token JWT no se está enviando correctamente
   - La sesión expiró
   - El usuario no tiene permisos

2. **CSRF token faltante**
   - Spring Security requiere CSRF token
   - Cookie Manager no está configurado

**Soluciones**:

#### ✅ Solución 1: Verificar HTTP Cookie Manager

En JMeter, asegúrate que el **HTTP Cookie Manager** esté:

- Agregado al nivel de Thread Group
- Configurado con `Cookie Policy: compatibility` o `standard`
- Policy: `CookieManager.policy=compatibility`

```xml
<!-- Debe existir en tu .jmx -->
<CookieManager guiclass="CookiePanel" testclass="CookieManager" testname="HTTP Cookie Manager">
  <collectionProp name="CookieManager.cookies"/>
  <boolProp name="CookieManager.clearEachIteration">false</boolProp>
  <stringProp name="CookieManager.policy">compatibility</stringProp>
</CookieManager>
```

#### ✅ Solución 2: Configurar HTTP Header Manager

Agregar headers necesarios para autenticación:

```xml
<HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager">
  <collectionProp name="HeaderManager.headers">
    <elementProp name="" elementType="Header">
      <stringProp name="Header.name">Content-Type</stringProp>
      <stringProp name="Header.value">application/json</stringProp>
    </elementProp>
    <elementProp name="" elementType="Header">
      <stringProp name="Header.name">Accept</stringProp>
      <stringProp name="Header.value">application/json</stringProp>
    </elementProp>
  </collectionProp>
</HeaderManager>
```

#### ✅ Solución 3: Deshabilitar CSRF en Development

En `application-dev.yml`:

```yaml
spring:
  security:
    csrf:
      enabled: false # Solo para testing local
```

O en tu configuración de Security:

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Para testing
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
            );
        return http.build();
    }
}
```

#### ✅ Solución 4: Verificar Login Request

El Login debe retornar:

- **Cookie de sesión** (JSESSIONID)
- **Token JWT** (en header o body)

Agrega un **JSON Extractor** después del Login:

```xml
<JSONPostProcessor guiclass="JSONPostProcessorGui" testclass="JSONPostProcessor">
  <stringProp name="JSONPostProcessor.referenceNames">auth_token</stringProp>
  <stringProp name="JSONPostProcessor.jsonPathExprs">$.token</stringProp>
  <stringProp name="JSONPostProcessor.match_numbers">1</stringProp>
</JSONPostProcessor>
```

Y luego usa `${auth_token}` en los requests subsecuentes.

---

### ❌ Error: Socket Closed en Login

**Síntomas**:

```
Response code: Non HTTP response code: java.net.SocketException
Response message: Non HTTP response message: Socket closed
```

**Causas Comunes**:

1. **Servidor cerró la conexión prematuramente**

   - Timeout del servidor
   - Servidor no está escuchando
   - Puerto incorrecto

2. **Payload inválido**

   - JSON malformado
   - Campos requeridos faltantes
   - Content-Type incorrecto

3. **Demasiadas conexiones simultáneas**
   - Pool de conexiones agotado
   - Rate limiting activo

**Soluciones**:

#### ✅ Solución 1: Verificar que el servidor esté corriendo

```powershell
# Verificar que la app esté escuchando en el puerto correcto
netstat -ano | findstr :8080

# Probar con curl
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"testuser","password":"password123"}'
```

#### ✅ Solución 2: Validar formato del Login Request

El payload debe ser válido JSON:

```json
{
  "username": "${username}",
  "password": "${password}"
}
```

Verifica en JMeter:

- Body Data está en formato JSON válido
- Content-Type header = `application/json`
- No hay caracteres especiales sin escapar

#### ✅ Solución 3: Agregar Connection Timeout y Response Timeout

En JMeter, configura timeouts más largos:

```xml
<stringProp name="HTTPSampler.connect_timeout">10000</stringProp>
<stringProp name="HTTPSampler.response_timeout">10000</stringProp>
```

#### ✅ Solución 4: Reducir carga inicial

Si estás ejecutando con 500 threads:

1. **Agregar Ramp-up period**: 60-120 segundos
2. **Reducir threads inicialmente**: Empezar con 10-50 threads
3. **Agregar delays**: Constant Timer de 100-500ms entre requests

```xml
<ConstantTimer guiclass="ConstantTimerGui" testclass="ConstantTimer">
  <stringProp name="ConstantTimer.delay">500</stringProp>
</ConstantTimer>
```

#### ✅ Solución 5: Verificar logs del servidor

Revisa los logs de Spring Boot:

```powershell
# Ver logs en tiempo real
./mvnw spring-boot:run | Select-String -Pattern "ERROR|WARN|Exception"
```

Busca:

- `AuthenticationException`
- `BadCredentialsException`
- `HikariPool connection timeout`
- `SocketTimeoutException`

---

### ⚠️ Errores Comunes Adicionales

#### Error: No response data (0 bytes)

**Causa**: Request no llegó al servidor o servidor no respondió

**Solución**:

- Verificar que la URL sea correcta: `http://localhost:8080/api/reservations`
- Verificar que el método HTTP sea correcto (POST, GET, etc.)
- Revisar firewall/antivirus

#### Error: 401 Unauthorized

**Causa**: Token expirado o inválido

**Solución**:

- Hacer login nuevamente
- Verificar que el token se esté extrayendo correctamente
- Aumentar tiempo de expiración del token en desarrollo

#### Error: 400 Bad Request

**Causa**: Payload inválido

**Solución**:

- Validar JSON con un validador online
- Verificar que todos los campos requeridos estén presentes
- Revisar tipos de datos (números vs strings)

---

## 🔒 Configuración de Autenticación en JMeter

### Opción 1: Autenticación con JWT Token

1. **Login Request** extrae el token:

```xml
<JSONPostProcessor>
  <stringProp name="JSONPostProcessor.referenceNames">jwt_token</stringProp>
  <stringProp name="JSONPostProcessor.jsonPathExprs">$.token</stringProp>
</JSONPostProcessor>
```

2. **Subsequent Requests** usan el token:

```xml
<HeaderManager>
  <elementProp name="Authorization" elementType="Header">
    <stringProp name="Header.name">Authorization</stringProp>
    <stringProp name="Header.value">Bearer ${jwt_token}</stringProp>
  </elementProp>
</HeaderManager>
```

### Opción 2: Autenticación con Session Cookie

1. **Agregar HTTP Cookie Manager** al Thread Group
2. **Login Request** establece la cookie JSESSIONID automáticamente
3. **Requests subsecuentes** usan la cookie automáticamente

### Opción 3: Deshabilitar seguridad (Solo para pruebas locales)

En `application-dev.yml`:

```yaml
spring:
  security:
    enabled: false
```

O permitir todos los endpoints:

```java
http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```

---

## 📊 Métricas a Observar

Durante la ejecución del test, monitorea:

1. **Response Time**: Tiempo de respuesta (debe ser < 200ms para el p95)
2. **Throughput**: Transacciones por segundo (target: 166 req/sec)
3. **Error Rate**: % de errores (debe ser < 1%)
4. **Active Threads**: Usuarios concurrentes activos
5. **Database Connections**: Conexiones HikariCP en uso

### Verificar en Grafana

Si tienes Grafana configurado, revisa:

- Dashboard de HTTP requests
- Métricas de base de datos
- Pool de conexiones
- Métricas de JVM (heap, GC)

---

## ✅ Checklist de Validación

Después de actualizar, verifica:

- [x] Endpoints cambiados a `/api/reservations`
- [x] Payloads usan `servicePostId`, `userId`, `seatsRequested`
- [x] JSON path extractor usa `$.id` en lugar de `$.reservationId`
- [x] Mensajes de error actualizados ("No seats available")
- [x] Códigos HTTP incluyen 400 y 409 para errores
- [x] Títulos y descripciones actualizados
- [x] Nombres de pasos reflejan transporte, no inventario
- [x] Logs usan terminología de asientos/transporte

---

## 🔗 Referencias

- **API Endpoints**: `../transport-sample-requests.http`
- **Documentación**: `../TRANSPORT-README.md`
- **Guía de Testing**: `README.md`

---

**Estado**: ✅ Archivos JMeter actualizados para sistema de transporte  
**Próximo Paso**: Ejecutar tests y validar funcionamiento
