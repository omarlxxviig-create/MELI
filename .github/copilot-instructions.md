# 🧭 Copilot Instructions – Backend (Java / Spring Boot)

## 💡 Contexto General

Este proyecto forma parte de la aplicación **ViajeComún**, un sistema de reservas de transporte compartido entre usuarios.  
Los conductores pueden publicar viajes (por ejemplo, de Zipaquirá a Bogotá) y los pasajeros pueden buscar, reservar y gestionar sus viajes directamente desde la app, sin depender de WhatsApp.

El objetivo es construir una API moderna, segura y escalable con **Spring Boot**, conectada a una base de datos relacional o NoSQL, y autenticada con **Google Firebase Auth**.

---

## ⚙️ Stack Técnico

- **Lenguaje:** Java 17+
- **Framework principal:** Spring Boot 3+
- **Dependencias clave:**
  - Spring Web (REST APIs)
  - Spring Data JPA o Spring Data MongoDB
  - Spring Security
  - Firebase Admin SDK (para validación de JWTs de Google)
  - Lombok
  - MapStruct (opcional para DTOs)
- **Base de datos:** PostgreSQL (o MongoDB según el microservicio)
- **Build tool:** Maven o Gradle
- **Contenedores:** Docker (para despliegue local y futuro Kubernetes)

---

## 🧩 Estructura esperada

viajecomun-backend/
│
├── src/main/java/com/viajecomun/
│ ├── auth/ # Validación de tokens Firebase
│ ├── users/ # Gestión de usuarios
│ ├── trips/ # Publicación y consulta de viajes
│ ├── reservations/ # Creación y gestión de reservas
│ ├── common/ # Utilidades compartidas
│ └── ViajeComunApplication.java
│
├── src/main/resources/
│ ├── application.yml
│ └── firebase-service-account.json (no subir al repo público)
│
└── pom.xml

yaml
Copiar código

---

## 🔐 Autenticación con Firebase (Google Sign-In)

1. El frontend (Flutter) realiza el login con **Firebase Authentication (Google)**.
2. Firebase devuelve un **JWT (ID token)** firmado.
3. El frontend envía este token en cada request (`Authorization: Bearer <token>`).
4. El backend valida el token usando el **Firebase Admin SDK**.
   - Verifica la firma y la expiración.
   - Extrae el `uid`, `email` y `displayName` del usuario autenticado.

👉 Copilot debe generar controladores y filtros de seguridad que **solo permitan acceso autenticado** usando este flujo.

---

## 🧱 Entidades principales

- **User** → representa al usuario autenticado (nombre, email, rol, historial de reservas).
- **Trip** → viaje publicado por un conductor (origen, destino, fecha, hora, cupos disponibles, precio opcional).
- **Reservation** → vínculo entre usuario y viaje (estado, fecha de reserva, comentarios).
- **Notification** _(futuro)_ → para manejar eventos en tiempo real con WebSocket o microservicio en Go.

---

## 🧭 Instrucciones para Copilot

Copilot, tu rol es actuar como **asistente técnico** para desarrollar este backend.  
Sigue estas reglas:

1. Escribe código limpio, modular y fácil de probar.
2. Aplica principios **DDD (Domain-Driven Design)** cuando tenga sentido.
3. Usa DTOs para evitar exponer directamente entidades de dominio.
4. Configura CORS para permitir comunicación desde el frontend Flutter.
5. Todos los endpoints deben devolver respuestas con formato JSON y `ResponseEntity`.
6. Documenta los endpoints con **Swagger/OpenAPI**.
7. No uses hardcoded secrets ni claves de Firebase en el código.
8. Antes de crear un documento revisa si este ya existe para evitar duplicados.

Revisar Documentos 

PS C:\Users\omaroalvaradoc\Documents\Personal\Proyectos\MELI\inventory-service\src> tree /F                                            
Listado de rutas de carpetas
El número de serie del volumen es B2DB-78BF
C:.
├───main
│   ├───java
│   │   └───com
│   │       └───meli
│   │           └───inventory_service
│   │               │   InventoryServiceApplication.java
│   │               │   
│   │               ├───application
│   │               │   ├───dto
│   │               │   │       ReserveRequest.java
│   │               │   │       ReserveResponse.java
│   │               │   │       
│   │               │   ├───jobs
│   │               │   │       ReservationExpirationJob.java
│   │               │   │       
│   │               │   └───service
│   │               │           BookingUseCase.java
│   │               │           GoogleAuthService.java
│   │               │           InventoryUseCase.java.bak
│   │               │           ProductService.java
│   │               │
│   │               ├───config
│   │               │       ApplicationConfig.java
│   │               │       JacksonConfig.java
│   │               │       JwtProperties.java
│   │               │       OpenAPIConfig.java
│   │               │       SecurityConfig.java
│   │               │
│   │               ├───domain
│   │               │   ├───exception
│   │               │   │       ProductDomainException.java
│   │               │   │
│   │               │   ├───model
│   │               │   │       OutboxMessage.java
│   │               │   │       Permission.java
│   │               │   │       ProcessedMessage.java
│   │               │   │       Product.java
│   │               │   │       Reservation.java
│   │               │   │       Role.java
│   │               │   │       ServicePost.java
│   │               │   │       StoreInventory.java
│   │               │   │       UpdateStockRequest.java
│   │               │   │       User.java
│   │               │   │
│   │               │   └───ports
│   │               │       ├───in
│   │               │       │       BookingPort.java
│   │               │       │       CreateReservationCommand.java
│   │               │       │       CreateServicePostCommand.java
│   │               │       │       InventoryUseCasePort.java
│   │               │       │       ProductUseCase.java
│   │               │       │
│   │               │       └───out
│   │               │               InventoryPort.java
│   │               │               OutboxPort.java
│   │               │               ProcessedMessagePort.java
│   │               │               ProductPort.java
│   │               │               ReservationPort.java
│   │               │               ServicePostPort.java
│   │               │
│   │               └───infrastructure
│   │                   ├───config
│   │                   │       RedisConfig.java
│   │                   │       RetryConfiguration.java
│   │                   │
│   │                   ├───messaging
│   │                   │       KafkaOutboxPublisher.java
│   │                   │
│   │                   ├───metrics
│   │                   │       InventoryGauges.java
│   │                   │       InventoryMetrics.java
│   │                   │
│   │                   ├───persistence
│   │                   │   │   JpaInventoryAdapter.java
│   │                   │   │   JpaOutboxAdapter.java
│   │                   │   │   JpaProcessedMessageAdapter.java
│   │                   │   │   JpaProductAdapter.java
│   │                   │   │   JpaReservationAdapter.java.bak
│   │                   │   │   StoreInventoryRepository.java
│   │                   │   │
│   │                   │   ├───adapter
│   │                   │   │       ReservationAdapter.java
│   │                   │   │       ServicePostAdapter.java
│   │                   │   │
│   │                   │   └───spring
│   │                   │           OutboxRepository.java
│   │                   │           PermissionRepository.java
│   │                   │           ProcessedMessageRepository.java
│   │                   │           ProductRepository.java
│   │                   │           ReservationJpaRepository.java
│   │                   │           ReservationRepository.java
│   │                   │           RoleRepository.java
│   │                   │           ServicePostJpaRepository.java
│   │                   │           UserRepository.java
│   │                   │
│   │                   ├───rest
│   │                   │   │   AuthController.java
│   │                   │   │   CreatePostRequest.java
│   │                   │   │   CreateReservationRequest.java
│   │                   │   │   InventoryController.java
│   │                   │   │   PostController.java
│   │                   │   │   ProductController.java
│   │                   │   │   ReservationController.java
│   │                   │   │   ReservationResponse.java
│   │                   │   │   ServicePostResponse.java
│   │                   │   │
│   │                   │   └───dto
│   │                   │           GoogleAuthRequest.java
│   │                   │           GoogleAuthResponse.java
│   │                   │           GoogleTokenInfo.java
│   │                   │           LoginRequest.java
│   │                   │           LoginResponse.java
│   │                   │           ProductRequest.java
│   │                   │           ProductResponse.java
│   │                   │           RefreshTokenRequest.java
│   │                   │           RegisterRequest.java
│   │                   │           ReserveRequest.java
│   │                   │           ReserveResponse.java
│   │                   │           StoreInventory.java
│   │                   │           StoreInventoryResponse.java
│   │                   │           UpdateStockRequest.java
│   │                   │
│   │                   └───security
│   │                       │   SecurityConfig.java
│   │                       │   UserDetailsServiceImpl.java
│   │                       │
│   │                       └───jwt
│   │                               JwtAuthenticationFilter.java
│   │                               JwtTokenProvider.java
│   │
│   └───resources
│       │   application-dev.properties
│       │   application-dev.yml
│       │   application-jwt.yml
│       │   application.properties
│       │   application.yml
│       │   data-security.sql
│       │   data-transport.sql
│       │   data.sql
│       │   logback-spring.xml
│       │
│       ├───db
│       │   └───migration
│       │           V1__initial_schema.sql
│       │           V3__add_google_oauth_fields.sql
│       │           V3__add_version_to_reservations.sql
│       │           V4__add_performance_indexes.sql
│       │
│       ├───static
│       └───templates
└───test
    ├───java
    │   └───com
    │       └───meli
    │           └───inventory_service
    │               │   InventoryServiceApplicationTests.java
    │               │
    │               ├───application
    │               │   └───service
    │               │           BookingUseCaseConcurrencyIT.java
    │               │           BookingUseCaseTest.java
    │               │           GoogleAuthServiceTest.java
    │               │           InventoryUseCaseConcurrencyIT.java.bak
    │               │           InventoryUseCaseTest.java.bak
    │               │
    │               ├───infrastructure
    │               │   └───rest
    │               ├───integration
    │               ├───load
    │               │       InventoryLoadTest.java
    │               │
    │               └───performance
    └───resources
            test-data.sql

---

## 🚀 Futuras extensiones

- Microservicio de notificaciones (Go)
- Integración con Kafka o RabbitMQ para eventos (RESERVATION_CREATED, TRIP_CANCELLED)
- Panel de administración web
- Sistema de monetización mediante anuncios (AdMob en frontend)

---

## 🧠 Prompt base sugerido (para usar dentro de Copilot Chat)

@workspace
Actúa como asistente técnico senior. Estoy construyendo el backend del proyecto “ViajeComún” en Java Spring Boot.
Quiero que me ayudes a crear los controladores, servicios, repositorios y seguridad para gestionar usuarios, viajes y reservas.
El sistema se autentica mediante tokens JWT de Firebase (Google Sign-In).
Sugiere buenas prácticas, tests unitarios y documentación Swagger.

yaml
Copiar código

---

📘 **Autor:** Omar Alvarado  
📅 **Proyecto:** ViajeComún  
🏗️ **Propósito:** Backend base para el sistema de reservas de transporte comunitario.
