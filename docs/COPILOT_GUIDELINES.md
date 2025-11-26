📌 Principios Generales

El proyecto usa Java 17 y Spring Boot 3.x.

La arquitectura es hexagonal (ports & adapters):

domain: modelos, excepciones, interfaces (ports).

application: casos de uso (lógica de negocio orquestada).

infrastructure: adaptadores (persistence, messaging, rest).

Todo el código debe seguir SOLID y Clean Code.

📦 Persistencia

Repositorios deben implementarse como adapters en infrastructure.persistence.

Interfaces (ports) deben estar en domain.ports.out.

Usar Spring Data JPA para la persistencia.

Evitar lógica de negocio en entidades JPA.

🔄 Integración con Kafka (Outbox Pattern)

Cada cambio importante debe generar un evento en la Outbox.

Publicar eventos de la Outbox en Kafka de forma asíncrona.

Evitar publicar directo a Kafka desde la capa de aplicación.

Usar KafkaOutboxPublisher como adapter.

🌐 Exposición de API (REST)

Endpoints en infrastructure.rest.

DTOs en infrastructure.rest.dto.

Los Controllers solo llaman casos de uso (application.service).

Validaciones con @Valid y javax.validation.

⚠️ Manejo de Errores

Lanzar excepciones de dominio (ProductDomainException, etc.).

Capturar y mapear a respuestas HTTP claras en @ControllerAdvice.

Incluir logs con contexto (productoId, requestId, etc.).

🧪 Pruebas

Unit tests con JUnit 5 + Mockito.

Tests deben cubrir:

Casos de uso (application).

Adaptadores de persistencia y mensajería (infrastructure).

Controladores REST.

Usar InMemoryRepository para tests rápidos.

🛠️ Estilo y Calidad

Usar nombres descriptivos en clases y métodos.

Evitar lógica duplicada.

Métodos cortos (< 30 líneas).

Documentar reglas de negocio clave con Javadoc.
