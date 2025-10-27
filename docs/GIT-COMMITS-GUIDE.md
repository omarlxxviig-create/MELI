# 🔄 Suggested Git Commits for Transport Booking Transformation

This file contains suggested atomic commits for the transformation of `inventory-service` to `transport-booking`.

---

## Commit Strategy

Follow this order to maintain a clean git history:

---

### 1️⃣ Domain Layer - Models

```bash
git add src/main/java/com/meli/inventory_service/domain/model/ServicePost.java
git add src/main/java/com/meli/inventory_service/domain/model/Reservation.java
git commit -m "feat(domain): add ServicePost and update Reservation entities

- Add ServicePost entity to represent transport service publications
- Fields: id (UUID), ownerId, origin, destination, departureDateTime, seatsTotal, seatsAvailable, price, description, status
- States: DRAFT, PUBLISHED, CANCELLED, COMPLETED
- Business methods: reserveSeats(), releaseSeats(), publish(), cancel(), complete()
- Add optimistic locking with @Version

- Update Reservation entity for seat reservations
- Fields: id (UUID), postId, userId, seats, status, expiresAt
- States: PENDING, CONFIRMED, CANCELLED, EXPIRED
- Business methods: confirm(), cancel(), expire(), isExpired(), isActive()
- Add optimistic locking with @Version

Part of: Transport Booking Service transformation"
```

---

### 2️⃣ Domain Layer - Ports and Commands

```bash
git add src/main/java/com/meli/inventory_service/domain/ports/in/CreateServicePostCommand.java
git add src/main/java/com/meli/inventory_service/domain/ports/in/CreateReservationCommand.java
git add src/main/java/com/meli/inventory_service/domain/ports/in/BookingPort.java
git add src/main/java/com/meli/inventory_service/domain/ports/out/ServicePostPort.java
git add src/main/java/com/meli/inventory_service/domain/ports/out/ReservationPort.java

git commit -m "feat(domain): add ports and commands for booking use case

- Add CreateServicePostCommand for creating transport posts
- Add CreateReservationCommand for seat reservations
- Add BookingPort interface (use case contract)
- Add ServicePostPort interface (persistence contract)
- Update ReservationPort interface with new methods

Methods include:
- createPost(), publishPost(), searchPosts()
- createReservation(), confirmReservation(), cancelReservation()
- processExpiredReservations()

Part of: Hexagonal architecture separation of concerns"
```

---

### 3️⃣ Application Layer - Use Cases

```bash
git add src/main/java/com/meli/inventory_service/application/service/BookingUseCase.java

git commit -m "feat(application): implement BookingUseCase with transactional logic

- Implement all BookingPort methods
- Transactional operations with @Transactional
- Retry on OptimisticLockingFailureException (max 3 attempts)
- Pessimistic locking for critical sections (findByIdForUpdate)
- Integration with Outbox pattern for event publishing
- Metrics integration: reservations_created_total, reservation_conflicts_total, outbox_pending_count, reservation_duration_seconds

Core business logic:
- createReservation(): atomic stock check + decrement + reservation creation
- cancelReservation(): release seats + update reservation status
- processExpiredReservations(): cleanup expired pending reservations

Part of: Transport Booking Service core logic"
```

---

### 4️⃣ Infrastructure Layer - Persistence

```bash
git add src/main/java/com/meli/inventory_service/infrastructure/persistence/spring/ServicePostJpaRepository.java
git add src/main/java/com/meli/inventory_service/infrastructure/persistence/spring/ReservationJpaRepository.java
git add src/main/java/com/meli/inventory_service/infrastructure/persistence/adapter/ServicePostAdapter.java
git add src/main/java/com/meli/inventory_service/infrastructure/persistence/adapter/ReservationAdapter.java

git commit -m "feat(infrastructure): add JPA repositories and adapters

- Add ServicePostJpaRepository with custom queries:
  * findByRouteAndDateRange() for search functionality
  * findByIdForUpdate() with PESSIMISTIC_WRITE lock

- Add ReservationJpaRepository with custom queries:
  * findExpiredReservations() for cleanup job
  * findByIdForUpdate() with PESSIMISTIC_WRITE lock

- Add ServicePostAdapter implementing ServicePostPort
- Add ReservationAdapter implementing ReservationPort

Part of: Hexagonal architecture - persistence adapters"
```

---

### 5️⃣ Infrastructure Layer - REST API

```bash
git add src/main/java/com/meli/inventory_service/infrastructure/rest/PostController.java
git add src/main/java/com/meli/inventory_service/infrastructure/rest/ReservationController.java
git add src/main/java/com/meli/inventory_service/infrastructure/rest/CreatePostRequest.java
git add src/main/java/com/meli/inventory_service/infrastructure/rest/ServicePostResponse.java
git add src/main/java/com/meli/inventory_service/infrastructure/rest/CreateReservationRequest.java
git add src/main/java/com/meli/inventory_service/infrastructure/rest/ReservationResponse.java

git commit -m "feat(api): add REST controllers and DTOs for transport booking

PostController endpoints:
- POST /api/v1/posts - Create service post
- POST /api/v1/posts/{id}/publish - Publish post
- GET /api/v1/posts - List available posts
- GET /api/v1/posts/{id} - Get post details
- GET /api/v1/posts/search - Search by route and date

ReservationController endpoints:
- POST /api/v1/posts/{postId}/reserve - Create reservation
- PUT /api/v1/reservations/{id}/confirm - Confirm reservation
- PUT /api/v1/reservations/{id}/cancel - Cancel reservation
- GET /api/v1/reservations/{id} - Get reservation details
- GET /api/v1/users/{userId}/reservations - User reservations history
- GET /api/v1/posts/{postId}/reservations - Post reservations list

All endpoints include:
- Jakarta validation (@Valid)
- Proper HTTP status codes (201 Created, 404 Not Found, 409 Conflict)
- Swagger/OpenAPI annotations
- Comprehensive logging

Part of: REST API implementation"
```

---

### 6️⃣ Database Migrations and Initial Data

```bash
git add docs/migrations/001_create_posts_reservations.sql
git add src/main/resources/data-transport.sql

git commit -m "feat(database): add migration scripts and initial data

Migration 001:
- CREATE TABLE service_post with indexes and constraints
- CREATE TABLE reservation with FK to service_post
- Add comments on tables and columns
- Constraints: CHECK on seats, status values, seats_available <= seats_total

Initial data (data-transport.sql):
- 5 sample service posts (Zipaquirá-Bogotá, Bogotá-Cajicá, etc.)
- 3 sample reservations in different states (PENDING, CONFIRMED)
- Realistic test data for demo purposes

Part of: Database schema for transport booking"
```

---

### 7️⃣ Unit Tests

```bash
git add src/test/java/com/meli/inventory_service/application/service/BookingUseCaseTest.java

git commit -m "test: add unit tests for BookingUseCase

Tests implemented:
- shouldCreateServicePost()
- shouldPublishPost()
- shouldCreateReservationSuccessfully()
- shouldFailReservationWhenInsufficientSeats()
- shouldCancelReservationAndReleaseSeats()
- shouldConfirmReservation()
- shouldThrowExceptionWhenPostNotFound()
- shouldThrowExceptionWhenReservationNotFound()

Coverage:
- Happy paths and error cases
- Mock all dependencies (ports)
- Verify interactions with repositories and outbox
- Assert business logic correctness

Part of: Test suite for core use cases"
```

---

### 8️⃣ Integration/Concurrency Tests

```bash
git add src/test/java/com/meli/inventory_service/application/service/BookingUseCaseConcurrencyIT.java

git commit -m "test: add concurrency integration tests for BookingUseCase

Tests implemented:
- shouldHandleConcurrentReservationsCorrectly()
  * 10 threads reserving 1 seat each on a 5-seat post
  * Validates exactly 5 succeed, 5 fail
  * No overselling

- shouldHandleConcurrentReservationsWithDifferentSizes()
  * Multiple threads reserving different quantities
  * Total reserved <= available

- shouldHandleConcurrentCancellations()
  * Concurrent cancellations releasing seats correctly
  * Validates seat count accuracy

Uses:
- @DataJpaTest for real JPA transactions
- ExecutorService for thread management
- CountDownLatch for synchronization
- AtomicInteger for thread-safe counting

Part of: Validation of optimistic locking and concurrency handling"
```

---

### 9️⃣ Documentation

```bash
git add docs/TRANSPORT-README.md
git add docs/transport-data-model.mmd
git add docs/transport-sample-requests.http
git add docs/TRANSFORMATION-GUIDE.md

git commit -m "docs: add comprehensive documentation for transport booking

- TRANSPORT-README.md: Complete project documentation
  * Architecture overview (Hexagonal)
  * API endpoints with examples
  * Setup instructions
  * Monitoring and metrics
  * Trade-offs and decisions

- transport-data-model.mmd: Mermaid ER diagram
  * SERVICE_POST, RESERVATION tables
  * Relationships and constraints
  * OUTBOX_MESSAGE integration

- transport-sample-requests.http: HTTP request examples
  * Full CRUD operations
  * Error scenarios
  * Concurrency tests
  * Monitoring endpoints

- TRANSFORMATION-GUIDE.md: Step-by-step transformation guide
  * Files created/modified
  * Setup instructions
  * Validation checklist
  * Troubleshooting
  * Next steps roadmap

Part of: Project documentation"
```

---

### 🔟 Optional: Legacy Endpoints Deprecation

```bash
# If you want to mark old inventory endpoints as deprecated

git commit -m "chore: deprecate legacy inventory endpoints

- Mark old Product/Inventory controllers as @Deprecated
- Add migration notices in responses
- Return HTTP 410 Gone with migration instructions
- Update Swagger to hide deprecated endpoints

Part of: Backward compatibility strategy"
```

---

## 📋 Complete Workflow

To apply all commits at once:

```bash
# 1. Stage and commit domain models
git add src/main/java/com/meli/inventory_service/domain/model/
git commit -m "feat(domain): add ServicePost and update Reservation entities [detailed message]"

# 2. Stage and commit domain ports
git add src/main/java/com/meli/inventory_service/domain/ports/
git commit -m "feat(domain): add ports and commands for booking use case"

# 3. Stage and commit application layer
git add src/main/java/com/meli/inventory_service/application/
git commit -m "feat(application): implement BookingUseCase with transactional logic"

# 4. Stage and commit infrastructure persistence
git add src/main/java/com/meli/inventory_service/infrastructure/persistence/
git commit -m "feat(infrastructure): add JPA repositories and adapters"

# 5. Stage and commit REST API
git add src/main/java/com/meli/inventory_service/infrastructure/rest/
git commit -m "feat(api): add REST controllers and DTOs for transport booking"

# 6. Stage and commit database
git add docs/migrations/ src/main/resources/data-transport.sql
git commit -m "feat(database): add migration scripts and initial data"

# 7. Stage and commit unit tests
git add src/test/java/com/meli/inventory_service/application/service/BookingUseCaseTest.java
git commit -m "test: add unit tests for BookingUseCase"

# 8. Stage and commit integration tests
git add src/test/java/com/meli/inventory_service/application/service/BookingUseCaseConcurrencyIT.java
git commit -m "test: add concurrency integration tests"

# 9. Stage and commit documentation
git add docs/
git commit -m "docs: add comprehensive documentation for transport booking"
```

---

## 🌿 Branch Strategy

Suggested branch structure:

```bash
# Create feature branch
git checkout -b feature/transport-booking-transformation

# Make all commits following the order above

# Push to remote
git push origin feature/transport-booking-transformation

# Create Pull Request with description from PR_TEMPLATE.md
```

---

## 📝 Pull Request Template

Title: `feat: Transform inventory service to transport booking system`

Description:

````markdown
## 🎯 Objective

Transform the inventory management service into a transport booking (carpooling) system while maintaining hexagonal architecture and distributed system patterns.

## 📦 Changes Summary

### Domain Layer

- ✅ New `ServicePost` entity (replaces Product/StoreInventory)
- ✅ Updated `Reservation` entity for seat reservations
- ✅ Commands and Ports for booking operations

### Application Layer

- ✅ `BookingUseCase` with transactional logic
- ✅ Optimistic locking handling
- ✅ Outbox pattern integration
- ✅ Metrics integration

### Infrastructure Layer

- ✅ JPA repositories with custom queries
- ✅ REST Controllers with full CRUD
- ✅ DTOs for request/response
- ✅ Database migrations

### Tests

- ✅ Unit tests for BookingUseCase
- ✅ Concurrency integration tests
- ✅ Coverage: happy paths + error cases

### Documentation

- ✅ Updated README
- ✅ ER diagrams (Mermaid)
- ✅ HTTP request examples
- ✅ Transformation guide

## 🧪 Testing

```bash
mvn clean test
mvn spring-boot:run
curl http://localhost:8080/api/v1/posts
```
````

## ✅ Checklist

- [x] All tests pass
- [x] Code compiles without errors
- [x] API endpoints documented (Swagger)
- [x] Database migrations included
- [x] README updated
- [x] No breaking changes to existing auth/security
- [x] Metrics and logging implemented

## 📸 Screenshots

[Optional: Add Swagger UI screenshot, Grafana dashboard, etc.]

## 🔗 Related Issues

Closes #XXX

## 🚀 Deployment Notes

1. Run migration: `docs/migrations/001_create_posts_reservations.sql`
2. Update `application.yml` to use `data-transport.sql`
3. Restart application
4. Verify `/api/v1/posts` endpoint

## 👥 Reviewers

@reviewer1 @reviewer2

```

---

## 🎓 Commit Message Conventions

Following [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation changes
- `test:` Test additions/modifications
- `refactor:` Code refactoring
- `chore:` Maintenance tasks

Scopes used:
- `domain` - Domain layer
- `application` - Application layer
- `infrastructure` - Infrastructure layer
- `api` - REST API
- `database` - Database changes

---

**Happy Committing! 🚀**
```
