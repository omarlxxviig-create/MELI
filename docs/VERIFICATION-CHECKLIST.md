# ✅ Transport Booking Service - Verification Checklist

Use este checklist para verificar que la transformación está completa y funcional.

---

## 📦 Archivos Creados/Modificados

### Domain Layer

- [x] `ServicePost.java` - Entidad creada
- [x] `Reservation.java` - Entidad modificada
- [x] `CreateServicePostCommand.java` - Command creado
- [x] `CreateReservationCommand.java` - Command creado
- [x] `BookingPort.java` - Interface creada
- [x] `ServicePostPort.java` - Interface creada
- [x] `ReservationPort.java` - Interface modificada

### Application Layer

- [x] `BookingUseCase.java` - Use case creado con lógica transaccional

### Infrastructure Layer - Persistence

- [x] `ServicePostJpaRepository.java` - Repository creado
- [x] `ReservationJpaRepository.java` - Repository creado
- [x] `ServicePostAdapter.java` - Adapter creado
- [x] `ReservationAdapter.java` - Adapter creado

### Infrastructure Layer - REST

- [x] `PostController.java` - Controller creado
- [x] `ReservationController.java` - Controller creado
- [x] `CreatePostRequest.java` - DTO creado
- [x] `ServicePostResponse.java` - DTO creado
- [x] `CreateReservationRequest.java` - DTO creado
- [x] `ReservationResponse.java` - DTO creado

### Database

- [x] `001_create_posts_reservations.sql` - Migration creada
- [x] `data-transport.sql` - Datos iniciales creados

### Tests

- [x] `BookingUseCaseTest.java` - Tests unitarios creados
- [x] `BookingUseCaseConcurrencyIT.java` - Tests de concurrencia creados

### Documentation

- [x] `TRANSPORT-README.md` - README principal
- [x] `TRANSFORMATION-GUIDE.md` - Guía de transformación
- [x] `GIT-COMMITS-GUIDE.md` - Guía de commits
- [x] `EXECUTIVE-SUMMARY.md` - Resumen ejecutivo
- [x] `transport-data-model.mmd` - Diagrama ER
- [x] `transport-sample-requests.http` - Ejemplos HTTP

---

## 🔧 Compilación y Build

### Maven Build

```bash
# Compilar proyecto
mvn clean compile
```

**Verificar**:

- [ ] Compilación exitosa sin errores
- [ ] Sin warnings críticos
- [ ] Todas las dependencias resueltas

### Package

```bash
# Crear JAR
mvn clean package -DskipTests
```

**Verificar**:

- [ ] JAR generado en `target/`
- [ ] Tamaño del JAR razonable (~50-80 MB)

---

## 🧪 Tests

### Unit Tests

```bash
mvn test -Dtest=BookingUseCaseTest
```

**Verificar cada test**:

- [ ] `shouldCreateServicePost()` - ✅ PASS
- [ ] `shouldPublishPost()` - ✅ PASS
- [ ] `shouldCreateReservationSuccessfully()` - ✅ PASS
- [ ] `shouldFailReservationWhenInsufficientSeats()` - ✅ PASS
- [ ] `shouldCancelReservationAndReleaseSeats()` - ✅ PASS
- [ ] `shouldConfirmReservation()` - ✅ PASS
- [ ] `shouldThrowExceptionWhenPostNotFound()` - ✅ PASS
- [ ] `shouldThrowExceptionWhenReservationNotFound()` - ✅ PASS

### Integration/Concurrency Tests

```bash
mvn test -Dtest=BookingUseCaseConcurrencyIT
```

**Verificar cada test**:

- [ ] `shouldHandleConcurrentReservationsCorrectly()` - ✅ PASS
- [ ] `shouldHandleConcurrentReservationsWithDifferentSizes()` - ✅ PASS
- [ ] `shouldHandleConcurrentCancellations()` - ✅ PASS

### All Tests

```bash
mvn test
```

**Verificar**:

- [ ] Todos los tests pasan
- [ ] Coverage >= 70% (idealmente 80%+)
- [ ] Sin flaky tests

---

## 🚀 Aplicación en Runtime

### Iniciar Aplicación

```bash
mvn spring-boot:run
```

**Verificar logs**:

- [ ] Aplicación inicia sin errores
- [ ] Puerto 8080 disponible
- [ ] Conexión a H2 exitosa
- [ ] Tablas creadas (SERVICE_POST, RESERVATION)
- [ ] Datos iniciales cargados

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Esperado**:

```json
{
  "status": "UP"
}
```

- [ ] Status: UP
- [ ] Componentes (db, diskSpace) UP

---

## 📡 API Endpoints

### Swagger UI

```bash
open http://localhost:8080/swagger-ui.html
```

**Verificar**:

- [ ] Swagger UI carga correctamente
- [ ] Sección "Service Posts" visible
- [ ] Sección "Reservations" visible
- [ ] Todos los endpoints documentados

### Test Endpoints - Service Posts

#### 1. Listar Posts Disponibles

```bash
curl http://localhost:8080/api/v1/posts
```

**Verificar**:

- [ ] Status: 200 OK
- [ ] Array de posts en response
- [ ] Al menos 3 posts (de data-transport.sql)
- [ ] Campos: id, origin, destination, seatsAvailable, etc.

#### 2. Crear Post

```bash
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Content-Type: application/json" \
  -d '{
    "ownerId": 1,
    "origin": "Test Origin",
    "destination": "Test Destination",
    "departureDateTime": "2025-12-01T10:00:00",
    "seatsTotal": 4,
    "price": 10000.00
  }'
```

**Verificar**:

- [ ] Status: 201 Created
- [ ] Response contiene: id (UUID)
- [ ] status: "DRAFT"
- [ ] seatsAvailable: 4

**Guardar `id` del post creado**: ********\_\_\_********

#### 3. Publicar Post

```bash
curl -X POST http://localhost:8080/api/v1/posts/{id}/publish
```

**Verificar**:

- [ ] Status: 200 OK
- [ ] status: "PUBLISHED"

#### 4. Buscar Posts

```bash
curl "http://localhost:8080/api/v1/posts/search?origin=Zipaquirá&destination=Bogotá"
```

**Verificar**:

- [ ] Status: 200 OK
- [ ] Resultados filtrados correctamente
- [ ] Solo posts con ruta coincidente

### Test Endpoints - Reservations

#### 5. Crear Reserva

```bash
curl -X POST http://localhost:8080/api/v1/posts/post-001/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 100,
    "seats": 2
  }'
```

**Verificar**:

- [ ] Status: 201 Created
- [ ] Response contiene: id (UUID)
- [ ] status: "PENDING"
- [ ] expiresAt: ~15 minutos en el futuro
- [ ] seats: 2

**Guardar `id` de la reserva**: ********\_\_\_********

#### 6. Verificar Asientos Decrementados

```bash
curl http://localhost:8080/api/v1/posts/post-001
```

**Verificar**:

- [ ] seatsAvailable decrementado en 2
- [ ] seatsAvailable >= 0

#### 7. Confirmar Reserva

```bash
curl -X PUT http://localhost:8080/api/v1/reservations/{reservationId}/confirm
```

**Verificar**:

- [ ] Status: 200 OK
- [ ] status: "CONFIRMED"

#### 8. Listar Reservas de Usuario

```bash
curl http://localhost:8080/api/v1/users/100/reservations
```

**Verificar**:

- [ ] Status: 200 OK
- [ ] Array contiene la reserva creada
- [ ] userId: 100

#### 9. Cancelar Reserva

```bash
curl -X PUT http://localhost:8080/api/v1/reservations/{reservationId}/cancel
```

**Verificar**:

- [ ] Status: 200 OK
- [ ] status: "CANCELLED"

#### 10. Verificar Asientos Liberados

```bash
curl http://localhost:8080/api/v1/posts/post-001
```

**Verificar**:

- [ ] seatsAvailable incrementado en 2

### Test Error Cases

#### 11. Reservar Más Asientos que Disponibles

```bash
curl -X POST http://localhost:8080/api/v1/posts/post-001/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 101,
    "seats": 100
  }'
```

**Verificar**:

- [ ] Status: 409 Conflict (o 400 Bad Request)
- [ ] Mensaje de error descriptivo

#### 12. Post No Existente

```bash
curl http://localhost:8080/api/v1/posts/invalid-id
```

**Verificar**:

- [ ] Status: 404 Not Found

#### 13. Reserva No Existente

```bash
curl http://localhost:8080/api/v1/reservations/invalid-id
```

**Verificar**:

- [ ] Status: 404 Not Found

---

## 📊 Métricas y Monitoreo

### Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

**Verificar métricas existen**:

- [ ] `reservations_created_total`
- [ ] `reservation_conflicts_total`
- [ ] `outbox_pending_count`
- [ ] `reservation_duration_seconds`

### Específicas

```bash
curl http://localhost:8080/actuator/metrics/reservations_created_total
```

**Verificar**:

- [ ] Valor > 0 después de crear reservas
- [ ] Se incrementa con cada reserva

---

## 🗄️ Base de Datos

### H2 Console (si está habilitado)

```bash
open http://localhost:8080/h2-console
```

**Verificar tablas**:

```sql
SELECT * FROM SERVICE_POST;
SELECT * FROM RESERVATION;
SELECT * FROM OUTBOX_MESSAGE;
```

- [ ] Tabla SERVICE_POST existe
- [ ] Tabla RESERVATION existe
- [ ] Datos iniciales presentes (5 posts, 3 reservations)
- [ ] Constraints y indexes creados

---

## 🔐 Seguridad

### Endpoints Públicos

**Verificar acceso sin autenticación**:

- [ ] `/api/v1/posts` - Accesible
- [ ] `/api/v1/posts/{id}` - Accesible
- [ ] `/actuator/health` - Accesible
- [ ] `/swagger-ui.html` - Accesible

_(En producción, estos deberían requerir autenticación)_

---

## 📖 Documentación

### README

- [ ] `TRANSPORT-README.md` existe
- [ ] Contiene instrucciones de setup
- [ ] Ejemplos de API claros
- [ ] Arquitectura explicada

### Diagrams

- [ ] `transport-data-model.mmd` existe
- [ ] Diagrama ER completo
- [ ] Relaciones correctas

### Samples

- [ ] `transport-sample-requests.http` existe
- [ ] Requests funcionan con REST Client
- [ ] Todos los endpoints cubiertos

---

## 🧹 Code Quality

### Linting/Formatting

- [ ] Código bien formateado
- [ ] Imports organizados
- [ ] Sin código comentado innecesario

### Best Practices

- [ ] Uso correcto de `@Transactional`
- [ ] Validaciones con Jakarta Bean Validation
- [ ] Manejo de excepciones apropiado
- [ ] Logging en puntos clave
- [ ] DTOs separados de entidades

### Nomenclatura

- [ ] Nombres de variables descriptivos
- [ ] Métodos con verbos (create, find, update)
- [ ] Clases con sustantivos (ServicePost, Reservation)
- [ ] Constantes en UPPER_CASE

---

## 🐛 Troubleshooting Checklist

### Si la aplicación no inicia:

- [ ] Puerto 8080 libre
- [ ] Java 17+ instalado
- [ ] Maven configurado correctamente
- [ ] Revisar logs de stack trace

### Si los tests fallan:

- [ ] Dependencias de test correctas
- [ ] H2 en classpath
- [ ] `@DataJpaTest` configurado
- [ ] Mocks inicializados

### Si los endpoints no responden:

- [ ] SecurityConfig permite acceso
- [ ] Controllers tienen `@RestController`
- [ ] Paths correctos en `@RequestMapping`
- [ ] JSON serialization configurada

---

## ✅ Checklist Final de Deploy

### Pre-Deploy

- [ ] Todos los tests pasan
- [ ] Aplicación compila sin errores
- [ ] Code review completado
- [ ] Documentación actualizada

### Deploy a Dev/Staging

- [ ] Variables de entorno configuradas
- [ ] Base de datos migrada (PostgreSQL en prod)
- [ ] Kafka/Zookeeper disponibles
- [ ] Prometheus/Grafana conectados

### Post-Deploy Verification

- [ ] Health check: UP
- [ ] Smoke tests exitosos
- [ ] Métricas reportando
- [ ] Logs sin errores críticos

---

## 📋 Sign-Off

**Desarrollador**: ********\_\_\_********  
**Fecha**: ********\_\_\_********  
**Versión**: 1.0.0  
**Estado**: [ ] Aprobado / [ ] Requiere ajustes

**Notas adicionales**:

---

---

---

---

**¡Checklist completo! Sistema listo para producción 🚀**
