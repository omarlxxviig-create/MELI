# Inventory Service — Sistema Distribuido de Gestión de Inventario

## 📝 ¿Qué es este proyecto?

**Inventory Service** es un microservicio de ejemplo para la gestión distribuida de inventario, diseñado para entornos de alta concurrencia y consistencia eventual.  
Permite reservar, confirmar y liberar stock de productos en múltiples tiendas, optimizando la disponibilidad y la resiliencia ante fallos.  
Incluye patrones modernos como Outbox, Kafka, métricas Prometheus, y locking optimista para demostrar buenas prácticas en sistemas distribuidos.

**Arquitectura**: Este proyecto implementa **Arquitectura Hexagonal** (Ports & Adapters) para mantener el dominio desacoplado de la infraestructura. Ver [documentación completa de arquitectura](docs/HEXAGONAL_ARCHITECTURE.md).

---

## 📚 Índice

1. [Resumen](#resumen)
2. [Arquitectura](#arquitectura)
3. [Stack Técnico](#stack-técnico)
4. [Cómo Ejecutar](#-cómo-ejecutar-localmente)
5. [API y Ejemplos](#api-documentation)
6. [Monitoreo y Métricas](#monitoreo)
7. [Decisiones y Trade-offs](#trade-offs-y-decisiones)
8. [Limitaciones y Roadmap](#limitaciones-y-próximos-pasos)
9. [Diagramas](#diagramas-de-arquitectura)
10. [Pruebas](#pruebas)
11. [Colección Postman](#colección-postman)
12. [Repositorio](#repositorio)

---

## 📌 Resumen

Este proyecto es un prototipo de un **sistema distribuido de gestión de inventario** desarrollado en **Java 17 / Spring Boot** con arquitectura hexagonal.  
Demuestra mejoras en:

- **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación e infraestructura
- **Consistencia eventual**: patrón Outbox + Kafka + idempotencia.
- **Latencia baja**: eventos en tiempo real.
- **Observabilidad**: métricas con Micrometer, Prometheus y Grafana.
- **Seguridad básica**: endpoints protegidos y limitados al entorno local.

---

## 🏗️ Arquitectura

Este proyecto sigue los principios de **Arquitectura Hexagonal** con tres capas claramente definidas:

### 📦 Capas

1. **Domain (Núcleo)**: Lógica de negocio pura, sin dependencias externas

   - Modelos: `Product`, `StoreInventory`, `Reservation`, `OutboxMessage`
   - Puertos: Interfaces que definen contratos (`InventoryPort`, `ProductPort`, etc.)

2. **Application (Orquestación)**: Casos de uso que coordinan el dominio

   - `InventoryUseCase`: Gestión de reservas
   - `ProductService`: Gestión de productos
   - DTOs de aplicación

3. **Infrastructure (Adaptadores)**: Implementaciones técnicas
   - REST Controllers
   - Adaptadores JPA
   - Publicador Kafka
   - Métricas

**Ver documentación detallada**: [docs/HEXAGONAL_ARCHITECTURE.md](docs/HEXAGONAL_ARCHITECTURE.md)

---

## ⚙️ Stack Técnico

- **Java 17 / Spring Boot 3**
- **H2 Database** (memoria) + JPA
- **Kafka + Zookeeper** (mensajería)
- **Prometheus + Grafana + Alertmanager** (monitoring)
- **JUnit 5 + Mockito** (tests)
- **Docker Compose** para orquestación

---

## 🚀 Cómo ejecutar localmente

### 1. Construcción del backend

```bash
# Compilar el proyecto
mvn clean package -DskipTests

# Verificar la construcción
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --version
```

### 2. Iniciar Infraestructura

```bash
# Iniciar todos los servicios (Kafka + Monitoreo)
docker-compose -f docker-compose.yml -f docs/docker-compose-monitoring.yml up -d

# Verificar que todos los servicios están corriendo
docker-compose ps
```

Servicios disponibles:

- Kafka: localhost:9092
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- AlertManager: http://localhost:9093

### 3. Iniciar la Aplicación

```bash
./mvnw spring-boot:run
```

### 4. Verificar acceso

Una vez que la aplicación esté corriendo, prueba estas URLs en tu navegador:

1. **Health Check**: http://localhost:8080/actuator/health
2. **Swagger UI**: http://localhost:8080/swagger-ui.html
3. **Swagger UI (alternativa)**: http://localhost:8080/swagger-ui/index.html
4. **OpenAPI Docs**: http://localhost:8080/v3/api-docs

---

## API Documentation

La documentación OpenAPI está disponible en:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Swagger UI (alternativa)**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 🔓 Configuración de Seguridad

El proyecto incluye Spring Security configurado para:

- ✅ Acceso público a Swagger UI y documentación OpenAPI
- ✅ Acceso público a endpoints de Actuator (health, metrics)
- ✅ Acceso público a todos los endpoints de API (modo desarrollo)

**Nota de Producción**: Para ambientes productivos, modifica `SecurityConfig.java` para requerir autenticación en los endpoints de negocio:

```java
.requestMatchers("/api/**", "/inventory/**").authenticated()
```

### 🔧 Troubleshooting Swagger

Si no puedes acceder a Swagger:

1. **Verifica que la aplicación esté corriendo**: http://localhost:8080/actuator/health
2. **Prueba las URLs alternativas**:
   - http://localhost:8080/swagger-ui.html
   - http://localhost:8080/swagger-ui/index.html
3. **Revisa los logs** en busca de errores de Spring Security
4. **Verifica SecurityConfig.java** que incluya los paths de Swagger en `.permitAll()`

### Ejemplos de Uso

#### Crear Reserva

```bash
curl --location 'http://localhost:8080/inventory/reserve' \
--header 'Content-Type: application/json' \
--data '{ "storeId": "store-001", "productId": "sku-100", "quantity": 2, "transactionId": "tx-123" }'
```

#### Confirmar Reserva

```bash
curl --location --request POST 'http://localhost:8080/inventory/commit?reservationId=...'
```

#### Liberar Reserva

```bash
curl --location --request POST 'http://localhost:8080/inventory/release' \
--header 'Content-Type: application/json' \
--data '{ "reservationId": "...", "reason": "customer_cancelled" }'
```

#### Consultar Inventario

```bash
curl --location 'http://localhost:8080/inventory'
```

#### Manejo de Errores

- Stock insuficiente: `{ "message": "Insufficient stock: Available: 5, Requested: 10" }`
- Producto no encontrado: `{ "message": "Product not found" }`

---

## 🛠️ ¿Qué hace el servicio?

- **Reserva de inventario**: Permite reservar stock de productos en tiendas específicas, evitando sobreventa.
- **Confirmación y liberación**: Las reservas pueden confirmarse (checkout) o liberarse (cancelación/expiración).
- **Consistencia eventual**: Los cambios se publican como eventos en Kafka usando el patrón Outbox, asegurando que otros servicios reciban actualizaciones.
- **Optimistic Locking**: Evita bloqueos pesados y mejora el rendimiento en escenarios concurrentes.
- **Métricas y monitoreo**: Expone métricas clave para Prometheus y Grafana, facilitando la observabilidad y alertas.
- **Expiración automática**: Las reservas pendientes se liberan automáticamente tras un TTL configurable.

---

## 📊 Monitoreo

### Métricas Disponibles

- `inventory_reservations_total`: Total de reservas creadas
- `inventory_reservation_duration_seconds`: Duración de las reservas
- `inventory_stock_level`: Nivel actual de stock por producto

### Logs Importantes

```log
2024-01-24 12:34:56 INFO  Created reservation id=xxx, transactionId=yyy
2024-01-24 12:34:57 INFO  Committed reservation id=xxx
2024-01-24 12:34:58 WARN  Insufficient stock for reservation
```

---

## 💡 Trade-offs y Decisiones

1. **Consistencia Eventual vs Fuerte**:  
   Se eligió consistencia eventual para el checkout usando el patrón Outbox.  
   Beneficio: mejor disponibilidad y escalabilidad.  
   Costo: ventana de inconsistencia temporal.

2. **Optimistic Locking**:  
   Versioning optimista en lugar de locks pesimistas.  
   Beneficio: mejor concurrencia y throughput.  
   Costo: retries ocasionales.

3. **In-Memory Database**:  
   H2 para prototipado rápido.  
   Trade-off: simplicidad vs durabilidad.

4. **Implementación de Productos**:  
   Se incluye entidad Product completa solo para demostración.  
   En producción: solo referencias a productId.

---

## 🚧 Limitaciones y Próximos Pasos

### Limitaciones Actuales

- Sin persistencia durable (H2 in-memory)
- Implementación simplificada de productos (sin integración con Product Service)

### Roadmap

1. Migrar a PostgreSQL
2. Añadir circuit breakers y rate limiting
3. Logging estructurado
4. Healthchecks más robustos
5. Integrar con Product Service centralizado
6. Implementar cache de datos de productos

---

## 📐 Diagramas de Arquitectura

Diagramas generados con [Mermaid CLI](https://github.com/mermaid-js/mermaid-cli):

- Flujo de requests HTTP
- Procesamiento de comandos
- Publicación de eventos vía Outbox pattern
- Job de expiración de reservas

![Diagrama de Arquitectura](docs/architecture-diagram.png)
![Diagrama de Secuencia](docs/sequence-diagram.png)
![Diagrama de Paquetes](doc/packages-diagram.png)
![Event Flow](docs/event-flow.png)
![Data Model](docs/data-model.png)

---

## 🧪 Pruebas

### Ejecutar Tests

```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar pruebas unitarias específicas
./mvnw test -Dtest=InventoryUseCaseTest

# Ejecutar pruebas de integración
./mvnw test -Dtest=*IntegrationTest

# Ejecutar pruebas de carga
./mvnw test -Dtest=*LoadTest

# Generar reporte de cobertura
./mvnw test jacoco:report
```

- Reporte JUnit: `target/surefire-reports/`
- Cobertura de código: `target/site/jacoco/index.html`

### Tipos de Pruebas

- **Unitarias**: Casos de uso aislados, mocks de dependencias.
- **Integración**: Flujos completos, base de datos en memoria.
- **Carga**: Concurrencia, locking optimista, rendimiento bajo estrés.
- **API**: Endpoints REST, validación de requests/responses.

---

## 📬 Colección Postman

Incluye una colección con todos los endpoints y ejemplos:  
[MELI.postman_collection.json](MELI.postman_collection.json)

---

## 📦 Repositorio

El código está disponible públicamente en GitHub:  
https://github.com/o0410acut-spec/MELI

```bash
git clone https://github.com/o0410acut-spec/MELI.git
cd MELI
```

---

## 🛠️ Comando para correr docker compose

```bash
docker-compose -f docker-compose.yml -f docs/docker-compose-monitoring.yml up -d
```

- Casos de uso aislados
- Mocks de dependencias
- Validación de lógica de negocio

2. **Integración** (`*IntegrationTest.java`)

   - Flujos completos
   - Base de datos en memoria
   - Validación de transacciones

3. **Carga** (`*LoadTest.java`)

   - Concurrencia
   - Optimistic locking
   - Rendimiento bajo estrés

4. **API** (`*ApiTest.java`)
   - Endpoints REST
   - Validación de requests/responses
   - Manejo de errores

## 📄 Ejemplos de Requests (REST Client)

Puedes usar estos ejemplos con el plugin REST Client de VS Code o importarlos en Postman:

```http
### Crear un producto
POST http://localhost:8080/api/v1/products
Content-Type: application/json

{
  "name": "Laptop Dell",
  "sku": "sku-001",
  "price": 1200.0,
  "stock": 10
}

###

### Listar productos
GET http://localhost:8080/api/v1/products
Accept: application/json

###

### Crear inventario (opcional, si API lo expone)
POST http://localhost:8080/api/v1/inventory
Content-Type: application/json

{
  "storeId": "store-1",
  "productId": "sku-001",
  "stock": 5
}

###

### Reservar producto
POST http://localhost:8080/api/v1/inventory/reserve
Content-Type: application/json

{
  "storeId": "store-1",
  "productId": "sku-001",
  "quantity": 2
}

###

### Verificar health
GET http://localhost:8080/actuator/health

###

### Ver métricas Prometheus
GET http://localhost:8080/actuator/prometheus
```

También se puede encontrar estos ejemplos en el archivo [docs/sample-requests.http](docs/sample-requests.http).

## 📬 Colección Postman

Se incluye una colección de Postman con todos los endpoints y ejemplos:
[MELI.postman_collection.json](MELI.postman_collection.json)

Para importar:

1. Abrir Postman
2. Clic en "Import"
3. Seleccionar el archivo MELI.postman_collection.json
4. Los endpoints estarán disponibles en una nueva colección

## 📦 Repositorio

El código está disponible públicamente en GitHub:
https://github.com/o0410acut-spec/MELI

### Para clonar el repositorio:

```bash
git clone https://github.com/o0410acut-spec/MELI.git
cd MELI
```
