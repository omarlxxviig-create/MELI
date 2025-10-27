# 🚀 Guía de Transformación: Inventory Service → Transport Booking Service

## 📋 Resumen

Este documento describe la transformación completa del proyecto `inventory-service` a `transport-booking`, adaptando la lógica de gestión de inventario de productos a un sistema de reservas de transporte compartido (carpooling).

---

## ✅ Archivos Creados/Modificados

### 1. Modelos de Dominio (Domain Layer)

#### ✨ Nuevos Archivos

- `ServicePost.java` - Entidad principal que representa una publicación de viaje
- `Reservation.java` - **MODIFICADO** - Adaptado para reservas de asientos
- `CreateServicePostCommand.java` - Command para crear publicaciones
- `CreateReservationCommand.java` - Command para crear reservas

**Ubicación**: `src/main/java/com/meli/inventory_service/domain/model/`

#### 📝 Características Clave

- **ServicePost**:

  - UUID como ID
  - Gestión de `seatsTotal` y `seatsAvailable`
  - Estados: DRAFT, PUBLISHED, CANCELLED, COMPLETED
  - Métodos de negocio: `reserveSeats()`, `releaseSeats()`, `publish()`, etc.
  - Optimistic locking con `@Version`

- **Reservation**:
  - Relación con `ServicePost` vía `postId`
  - Estados: PENDING, CONFIRMED, CANCELLED, EXPIRED
  - Expiración automática con `expiresAt`
  - Métodos: `confirm()`, `cancel()`, `expire()`, `isExpired()`

---

### 2. Puertos (Interfaces de Dominio)

#### Puertos de Entrada (Use Cases)

- `BookingPort.java` - Interface principal del caso de uso

#### Puertos de Salida (Persistencia)

- `ServicePostPort.java` - Interface de persistencia para ServicePost
- `ReservationPort.java` - **MODIFICADO** - Interface de persistencia para Reservation

**Ubicación**:

- `src/main/java/com/meli/inventory_service/domain/ports/in/`
- `src/main/java/com/meli/inventory_service/domain/ports/out/`

---

### 3. Capa de Aplicación (Application Layer)

#### Casos de Uso

- `BookingUseCase.java` - **PRINCIPAL** - Orquesta toda la lógica de negocio

**Características**:

- Transaccional con `@Transactional`
- Retry automático en caso de `OptimisticLockingFailureException`
- Integración con Outbox pattern
- Métricas: `reservations_created_total`, `reservation_conflicts_total`, `outbox_pending_count`
- Lock pesimista: `findByIdForUpdate()` para actualizaciones atómicas

**Ubicación**: `src/main/java/com/meli/inventory_service/application/service/`

---

### 4. Adaptadores de Infraestructura

#### Repositorios JPA

- `ServicePostJpaRepository.java` - Spring Data JPA repository
- `ReservationJpaRepository.java` - Spring Data JPA repository

**Queries Importantes**:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM ServicePost p WHERE p.id = :id")
Optional<ServicePost> findByIdForUpdate(@Param("id") String id);

@Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);
```

#### Adaptadores

- `ServicePostAdapter.java` - Implementa `ServicePostPort`
- `ReservationAdapter.java` - Implementa `ReservationPort`

**Ubicación**:

- Repositories: `src/main/java/com/meli/inventory_service/infrastructure/persistence/spring/`
- Adapters: `src/main/java/com/meli/inventory_service/infrastructure/persistence/adapter/`

---

### 5. Controllers REST

#### Endpoints Implementados

**PostController.java** (`/api/v1/posts`):

- `POST /posts` - Crear publicación
- `POST /posts/{id}/publish` - Publicar servicio
- `GET /posts` - Listar disponibles
- `GET /posts/{id}` - Ver detalle
- `GET /posts/search` - Buscar por ruta/fecha

**ReservationController.java** (`/api/v1`):

- `POST /posts/{postId}/reserve` - Crear reserva
- `PUT /reservations/{id}/confirm` - Confirmar
- `PUT /reservations/{id}/cancel` - Cancelar
- `GET /reservations/{id}` - Ver detalle
- `GET /users/{userId}/reservations` - Historial usuario
- `GET /posts/{postId}/reservations` - Reservas de un post

#### DTOs

- `CreatePostRequest.java`
- `ServicePostResponse.java`
- `CreateReservationRequest.java`
- `ReservationResponse.java`

**Ubicación**: `src/main/java/com/meli/inventory_service/infrastructure/rest/`

---

### 6. Base de Datos

#### Scripts SQL

**001_create_posts_reservations.sql** (Migration):

```sql
CREATE TABLE service_post (...)
CREATE TABLE reservation (...)
```

**data-transport.sql** (Datos iniciales):

- 5 posts de ejemplo (Zipaquirá-Bogotá, Bogotá-Cajicá, etc.)
- 3 reservas de ejemplo en diferentes estados

**Ubicación**:

- Migrations: `docs/migrations/`
- Data: `src/main/resources/`

---

### 7. Tests

#### Tests Unitarios

**BookingUseCaseTest.java**:

- ✅ Crear service post
- ✅ Publicar post
- ✅ Crear reserva exitosa
- ✅ Fallo por asientos insuficientes
- ✅ Cancelar reserva y liberar asientos
- ✅ Confirmar reserva
- ✅ Manejo de errores (not found)

#### Tests de Integración/Concurrencia

**BookingUseCaseConcurrencyIT.java**:

- ✅ 10 threads reservando simultáneamente, solo 5 exitosos
- ✅ Reservas de diferentes tamaños concurrentes
- ✅ Cancelaciones concurrentes
- ✅ Validación de no overselling

**Ubicación**: `src/test/java/com/meli/inventory_service/application/service/`

---

### 8. Documentación

#### Archivos de Documentación

- `TRANSPORT-README.md` - README completo actualizado
- `transport-data-model.mmd` - Diagrama Mermaid del modelo de datos
- `transport-sample-requests.http` - Ejemplos de requests HTTP
- `001_create_posts_reservations.sql` - Migration SQL

**Ubicación**: `docs/`

---

## 🔧 Pasos para Aplicar la Transformación

### 1. Validar Compilación

```bash
mvn clean compile
```

Si hay errores de compilación, revisar imports y dependencias.

### 2. Ejecutar Migraciones SQL

Opción A: Ejecutar manual contra H2:

```bash
# Aplicar migration en H2 Console (http://localhost:8080/h2-console)
# Ejecutar: docs/migrations/001_create_posts_reservations.sql
```

Opción B: Usar Flyway/Liquibase (agregar dependencia si es necesario).

### 3. Actualizar application.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:transportdb
  jpa:
    hibernate:
      ddl-auto: create-drop
  sql:
    init:
      mode: always
      data-locations: classpath:data-transport.sql
```

### 4. Compilar y Ejecutar Tests

```bash
# Ejecutar tests unitarios
mvn test -Dtest=BookingUseCaseTest

# Ejecutar tests de concurrencia
mvn test -Dtest=BookingUseCaseConcurrencyIT

# Todos los tests
mvn test
```

### 5. Iniciar Aplicación

```bash
mvn spring-boot:run
```

### 6. Verificar Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html

# Listar posts disponibles
curl http://localhost:8080/api/v1/posts
```

### 7. Probar Flujo Completo

Usar el archivo `docs/transport-sample-requests.http` con REST Client de VS Code:

1. Crear post
2. Publicar post
3. Crear reserva
4. Confirmar reserva
5. Verificar métricas

---

## 📊 Validaciones de Calidad

### ✅ Checklist de Validación

- [ ] Compilación exitosa sin errores
- [ ] Todos los tests pasan (unitarios + integración)
- [ ] Aplicación inicia correctamente
- [ ] Swagger UI accesible
- [ ] Endpoints responden correctamente
- [ ] Métricas Prometheus funcionan
- [ ] Base de datos carga datos iniciales
- [ ] Manejo de errores funciona (409 Conflict, 404 Not Found)
- [ ] Optimistic locking previene overselling
- [ ] Outbox messages se crean correctamente

### 🧪 Tests de Humo (Smoke Tests)

```bash
# 1. Crear post
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Content-Type: application/json" \
  -d '{
    "ownerId": 1,
    "origin": "Zipaquirá",
    "destination": "Bogotá",
    "departureDateTime": "2025-11-15T08:00:00",
    "seatsTotal": 4,
    "price": 15000.00
  }'

# 2. Listar posts
curl http://localhost:8080/api/v1/posts

# 3. Reservar (debe funcionar)
curl -X POST http://localhost:8080/api/v1/posts/{postId}/reserve \
  -H "Content-Type: application/json" \
  -d '{"userId": 10, "seats": 2}'

# 4. Reservar más de lo disponible (debe fallar con 409)
curl -X POST http://localhost:8080/api/v1/posts/{postId}/reserve \
  -H "Content-Type: application/json" \
  -d '{"userId": 11, "seats": 10}'
```

---

## 🔍 Troubleshooting

### Problema: Tests de concurrencia fallan

**Solución**: Verificar que `@DataJpaTest` y transacciones estén correctamente configuradas.

```java
@DataJpaTest
@ActiveProfiles("test")
class BookingUseCaseConcurrencyIT { ... }
```

### Problema: Optimistic Locking no funciona

**Solución**: Asegurar que `@Version` está en las entidades y usar `findByIdForUpdate()`:

```java
@Version
private Integer version;
```

### Problema: Data.sql no se carga

**Solución**: Verificar `application.yml`:

```yaml
spring:
  sql:
    init:
      mode: always
      data-locations: classpath:data-transport.sql
```

### Problema: Swagger no accesible

**Solución**: Verificar SecurityConfig permita acceso:

```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

---

## 🎯 Próximos Pasos Recomendados

### Mejoras Inmediatas

1. **Scheduler para expiración automática**:

   ```java
   @Scheduled(fixedRate = 60000) // Cada 1 minuto
   public void processExpiredReservations() {
       bookingUseCase.processExpiredReservations();
   }
   ```

2. **Error handling global**:

   ```java
   @RestControllerAdvice
   public class GlobalExceptionHandler { ... }
   ```

3. **Paginación en listados**:
   ```java
   @GetMapping
   public Page<ServicePostResponse> listPosts(Pageable pageable) { ... }
   ```

### Mejoras a Mediano Plazo

- Migrar a PostgreSQL
- Implementar cache (Redis) para búsquedas frecuentes
- Circuit breakers con Resilience4j
- Rate limiting
- Eventos asíncronos con Kafka consumer

### Mejoras a Largo Plazo

- WebSocket para notificaciones real-time
- App móvil (Flutter)
- Sistema de pagos integrado
- Geolocalización
- Sistema de reputación (ratings)

---

## 📚 Referencias

- [Arquitectura Hexagonal](docs/HEXAGONAL_ARCHITECTURE.md)
- [Modelo de Datos](docs/transport-data-model.mmd)
- [Ejemplos HTTP](docs/transport-sample-requests.http)
- [README Principal](docs/TRANSPORT-README.md)

---

## ✨ Conclusión

Esta transformación mantiene los patrones de arquitectura limpia y distribuida del proyecto original, adaptándolos a un dominio diferente (transporte compartido) con las siguientes ventajas:

✅ **Arquitectura Hexagonal**: Dominio independiente de infraestructura
✅ **Optimistic Locking**: Manejo eficiente de concurrencia
✅ **Outbox Pattern**: Garantía de publicación de eventos
✅ **Observabilidad**: Métricas y logs detallados
✅ **Tests Completos**: Unitarios + Integración + Concurrencia

El sistema está listo para escalar y añadir nuevas funcionalidades de forma incremental.

**Happy Coding! 🚗💨**
