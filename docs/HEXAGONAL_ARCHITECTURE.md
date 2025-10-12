# Arquitectura Hexagonal - Inventory Service

## 📐 Visión General

Este proyecto implementa **Arquitectura Hexagonal** (también conocida como Puertos y Adaptadores), un patrón arquitectónico que promueve el desacoplamiento entre la lógica de negocio y los detalles técnicos de implementación.

## 🎯 Principios Aplicados

### 1. Independencia del Dominio

El núcleo de negocio no depende de frameworks, bases de datos o APIs externas.

### 2. Inversión de Dependencias

Las capas externas dependen de las internas, nunca al revés.

### 3. Separación de Responsabilidades

Cada capa tiene un propósito claro y bien definido.

## 📦 Estructura de Capas

com.meli.inventory_service/
├── domain/ ← NÚCLEO (sin dependencias externas)
│ ├── model/ ← Entidades
│ ├── ports/in/ ← Casos de uso (interfaces)
│ ├── ports/out/ ← Repositorios (interfaces)
│ └── exception/ ← Excepciones de dominio
│
├── application/ ← ORQUESTACIÓN
│ ├── service/ ← Implementación de casos de uso
│ ├── dto/ ← DTOs de aplicación
│ └── jobs/ ← Tareas programadas
│
└── infrastructure/ ← ADAPTADORES
├── rest/ ← Adaptador REST (entrada)
├── persistence/ ← Adaptadores JPA (salida)
├── messaging/ ← Adaptador Kafka (salida)
└── metrics/ ← Adaptador métricas (salida)

## 📦 Estructura de Capas Detallada

```
com.meli.inventory_service/
├── domain/                          # CAPA DE DOMINIO (Núcleo)
│   ├── model/                       # Entidades y objetos de valor
│   │   ├── Product.java
│   │   ├── StoreInventory.java
│   │   ├── Reservation.java
│   │   ├── OutboxMessage.java
│   │   └── ProcessedMessage.java
│   ├── ports/                       # Interfaces (Contratos)
│   │   ├── in/                      # Puertos de entrada (Use Cases)
│   │   │   ├── InventoryUseCasePort.java
│   │   │   └── ProductUseCase.java
│   │   └── out/                     # Puertos de salida (Repositorios, etc.)
│   │       ├── InventoryPort.java
│   │       ├── ProductPort.java
│   │       ├── ReservationPort.java
│   │       ├── OutboxPort.java
│   │       └── ProcessedMessagePort.java
│   └── exception/                   # Excepciones de dominio
│       └── ProductDomainException.java
│
├── application/                     # CAPA DE APLICACIÓN (Orquestación)
│   ├── service/                     # Implementación de casos de uso
│   │   ├── InventoryUseCase.java
│   │   └── ProductService.java
│   ├── dto/                         # DTOs de aplicación
│   │   ├── ReserveRequest.java
│   │   └── ReserveResponse.java
│   └── jobs/                        # Tareas programadas
│       └── ReservationExpirationJob.java
│
└── infrastructure/                  # CAPA DE INFRAESTRUCTURA (Adaptadores)
    ├── rest/                        # Adaptador REST (Entrada)
    │   ├── InventoryController.java
    │   ├── ProductController.java
    │   └── dto/                     # DTOs específicos de REST
    │       ├── ProductRequest.java
    │       ├── ProductResponse.java
    │       └── StoreInventoryResponse.java
    ├── persistence/                 # Adaptadores de persistencia (Salida)
    │   ├── JpaInventoryAdapter.java
    │   ├── JpaProductAdapter.java
    │   ├── JpaReservationAdapter.java
    │   ├── JpaOutboxAdapter.java
    │   ├── JpaProcessedMessageAdapter.java
    │   └── spring/                  # Repositorios Spring Data
    │       ├── StoreInventoryRepository.java
    │       ├── ProductRepository.java
    │       ├── ReservationRepository.java
    │       ├── OutboxRepository.java
    │       └── ProcessedMessageRepository.java
    ├── messaging/                   # Adaptador Kafka (Salida)
    │   └── KafkaOutboxPublisher.java
    └── metrics/                     # Adaptador de métricas (Salida)
        ├── InventoryMetrics.java
        └── InventoryGauges.java
```

## 🔄 Flujo de Dependencias

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE INFRAESTRUCTURA                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  REST API    │    │  Persistence │    │   Messaging  │  │
│  │ (Controllers)│    │  (Adapters)  │    │    (Kafka)   │  │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘  │
│         │                   │                   │           │
│         ▼                   ▼                   ▼           │
├─────────────────────────────────────────────────────────────┤
│                    CAPA DE APLICACIÓN                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │            Casos de Uso (Services)                   │   │
│  │  - InventoryUseCase                                  │   │
│  │  - ProductService                                    │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                       │
│                     ▼                                       │
├─────────────────────────────────────────────────────────────┤
│                     CAPA DE DOMINIO                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Modelos (Entities)        Puertos (Interfaces)     │   │
│  │  - Product                 - InventoryPort          │   │
│  │  - StoreInventory          - ProductPort            │   │
│  │  - Reservation             - ReservationPort        │   │
│  │  - OutboxMessage           - OutboxPort             │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🔌 Puertos y Adaptadores

### Puertos de Entrada (Driving Ports)

Definen **lo que el sistema puede hacer**:

- `InventoryUseCasePort`: Operaciones de inventario
- `ProductUseCase`: Operaciones de productos

### Adaptadores de Entrada (Driving Adapters)

Invocan los puertos de entrada:

- `InventoryController`: API REST para inventario
- `ProductController`: API REST para productos
- `ReservationExpirationJob`: Tarea programada

### Puertos de Salida (Driven Ports)

Definen **lo que el sistema necesita**:

- `InventoryPort`: Persistencia de inventario
- `ProductPort`: Persistencia de productos
- `ReservationPort`: Persistencia de reservas
- `OutboxPort`: Persistencia de mensajes outbox
- `ProcessedMessagePort`: Control de idempotencia

### Adaptadores de Salida (Driven Adapters)

Implementan los puertos de salida:

- `JpaInventoryAdapter`: Implementación JPA
- `JpaProductAdapter`: Implementación JPA
- `JpaReservationAdapter`: Implementación JPA
- `JpaOutboxAdapter`: Implementación JPA
- `JpaProcessedMessageAdapter`: Implementación JPA
- `KafkaOutboxPublisher`: Publicación de eventos

## 📋 Reglas de Dependencia

### ✅ Permitido

```
Infrastructure → Application → Domain
Infrastructure → Domain
Application → Domain
```

### ❌ Prohibido

```
Domain → Application
Domain → Infrastructure
Application → Infrastructure (excepto para inyección de dependencias)
```

## 🎯 Ventajas de esta Arquitectura

1. **Testabilidad**: El dominio se puede probar sin dependencias externas
2. **Mantenibilidad**: Cambios en infraestructura no afectan el dominio
3. **Flexibilidad**: Fácil cambio de tecnologías (BD, frameworks, etc.)
4. **Claridad**: Separación clara de responsabilidades
5. **Escalabilidad**: Permite evolucionar el sistema sin acoplamiento

## 🧪 Testing por Capas

### Tests de Dominio

```java
@Test
void testReservation() {
    // Solo lógica de dominio, sin dependencias
    Reservation r = new Reservation();
    r.setQuantity(10);
    assertEquals(10, r.getQuantity());
}
```

### Tests de Aplicación (con Mocks)

```java
@Test
void testReserveWithMocks() {
    // Mock de puertos de salida
    InventoryPort mockPort = mock(InventoryPort.class);
    InventoryUseCase useCase = new InventoryUseCase(mockPort, ...);

    // Test del caso de uso
    ReserveResponse response = useCase.reserve(request);

    verify(mockPort).save(any());
}
```

### Tests de Infraestructura (Integración)

```java
@SpringBootTest
void testJpaAdapter() {
    // Test con base de datos real (H2)
    Product saved = productAdapter.save(product);
    assertNotNull(saved.getId());
}
```

## 🔄 Flujo de una Petición

Ejemplo: Crear una reserva

```
1. HTTP POST /inventory/reserve
   └─> InventoryController (Infrastructure)
       └─> InventoryUseCasePort.reserve() (Domain Port)
           └─> InventoryUseCase.reserve() (Application)
               ├─> InventoryPort.findByStoreAndProduct() (Domain Port)
               │   └─> JpaInventoryAdapter (Infrastructure)
               │       └─> StoreInventoryRepository (Spring Data)
               │
               ├─> InventoryPort.save() (Domain Port)
               │   └─> JpaInventoryAdapter (Infrastructure)
               │
               ├─> ReservationPort.save() (Domain Port)
               │   └─> JpaReservationAdapter (Infrastructure)
               │
               └─> OutboxPort.save() (Domain Port)
                   └─> JpaOutboxAdapter (Infrastructure)
```

## 📚 Referencias

- [Hexagonal Architecture (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Ports and Adapters Pattern](https://herbertograca.com/2017/09/14/ports-adapters-architecture/)

## 🛠️ Mejoras Futuras

- [ ] Separar DTOs de entrada/salida por adaptador
- [ ] Implementar Value Objects en el dominio
- [ ] Agregar más validaciones en el dominio
- [ ] Implementar Domain Events
- [ ] Agregar más adaptadores (GraphQL, gRPC, etc.)
