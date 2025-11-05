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
- **Notification** *(futuro)* → para manejar eventos en tiempo real con WebSocket o microservicio en Go.

---

## 🧭 Instrucciones para Copilot

Copilot, tu rol es actuar como **asistente técnico** para desarrollar este backend.  
Sigue estas reglas:

1. Escribe código limpio, modular y fácil de probar.  
2. Aplica principios **DDD (Domain-Driven Design)** cuando tenga sentido.  
3. Sugiere siempre pruebas unitarias básicas con JUnit o Mockito.  
4. Usa DTOs para evitar exponer directamente entidades de dominio.  
5. Configura CORS para permitir comunicación desde el frontend Flutter.  
6. Todos los endpoints deben devolver respuestas con formato JSON y `ResponseEntity`.  
7. Documenta los endpoints con **Swagger/OpenAPI**.  
8. No uses hardcoded secrets ni claves de Firebase en el código.

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