# Warehouse Inventory Reservation System

A production-grade REST API for managing warehouse inventory reservations under concurrent load.
Built with Java 17, Spring Boot 3, PostgreSQL, Redis, and Liquibase.

---

## 1. Challenge

**Challenge 1 — Warehouse Inventory Reservation System** was chosen because it presents the most
interesting backend engineering problems: concurrency control under contention, lifecycle state
machine design, and schema modelling with strict invariants.

---

## 2. Architecture

The codebase follows **Clean Architecture** with four strictly separated layers:

```
api/            ← HTTP: controllers, DTOs, global exception handler
application/    ← Use cases: service interfaces + implementations, mappers
domain/         ← Business rules: entities, state pattern, factory, repository interfaces
infrastructure/ ← Runtime adapters: Spring Data JPA (auto-implements repository interfaces)
```

Dependencies always point inward — `api` depends on `application`, `application` depends on
`domain`. The `domain` package has zero dependencies on any other layer.

**Standalone Liquibase** runs as a dedicated Docker service (`liquibase/liquibase:4.24.0`) that
applies all migrations before the application starts. Spring Boot's embedded Liquibase is disabled
(`spring.liquibase.enabled: false`). The migration files live in `database/changelog/` at the
project root — separate from application source code — following the principle that schema
ownership belongs to infrastructure, not the application.

---

## 3. Design Patterns

### State Pattern — `domain/model/state/`

Controls valid reservation lifecycle transitions. Each status maps to a concrete `ReservationState`
implementation:

| State class | `confirm()` | `cancel()` |
|-------------|-------------|------------|
| `PendingState` | allowed | allowed |
| `ConfirmedState` | throws | throws |
| `CancelledState` | throws | throws |

The `Reservation` entity resolves its current state at runtime via `currentState()` and delegates
`confirm()` / `cancel()` to it. Adding a new state (e.g. `ON_HOLD`) requires only a new class —
no changes to existing code.

### Factory Pattern — `domain/factory/ReservationFactory`

Centralises `Reservation` entity construction: sets the initial `PENDING` status, wires the
bidirectional `Reservation ↔ ReservationItem` relationship, and keeps this logic out of the
service layer (Single Responsibility Principle).

---

## 4. SOLID Principles

| Principle | Where |
|-----------|-------|
| **S** — Single Responsibility | `ReservationController` handles HTTP only; `ReservationLockFacade` handles Redis locking only; `ReservationTransactionService` handles DB transaction only |
| **O** — Open/Closed | New reservation states can be added by implementing `ReservationState` without modifying any existing state class |
| **L** — Liskov Substitution | `PendingState`, `ConfirmedState`, `CancelledState` are interchangeable behind the `ReservationState` interface |
| **I** — Interface Segregation | `InventoryController` injects only `InventoryService`; it has no dependency on `ReservationService` |
| **D** — Dependency Inversion | `ReservationLockFacade` depends on the `ReservationService` interface, not on any concrete implementation |

---

## 5. Database Design

```text
  +-----------------------------+             +-----------------------------+
  |          products           |             |          inventory          |
  +-----------------------------+             +-----------------------------+
  | PK  sku           (VARCHAR) | <---------+ | PK,FK sku         (VARCHAR) |
  |     name          (VARCHAR) |             |       total_stock     (INT) |
  |     description      (TEXT) |             |       available_stock (INT) |
  +-----------------------------+             |       reserved_stock  (INT) |
                |                             +-----------------------------+
                | 1
                |
                | 0..*
  +-----------------------------+             +-----------------------------+
  |      reservation_items      |             |        reservations         |
  +-----------------------------+             +-----------------------------+
  | PK,FK1 reservation_id(UUID) | ----------> | PK  id               (UUID) |
  | PK,FK2 sku        (VARCHAR) |             | UK  order_id      (VARCHAR) |
  |        quantity       (INT) |             |     status        (VARCHAR) |
  +-----------------------------+             |     created_at(TIMESTAMPTZ) |
                                              |     updated_at(TIMESTAMPTZ) |
                                              +-----------------------------+
```

**Key decisions:**

- **Composite PK on `reservation_items`** enforces at the database level that the same SKU cannot
  appear twice in one reservation. No additional unique index is needed.
- **`order_id UNIQUE`** prevents duplicate reservations for the same order.
- **`reservations.updated_at`** is not in the spec but is retained because `reservations` is the
  only table that undergoes state transitions. Knowing when a status changed has operational value.
  The reason is documented in the migration file.
- **Invariant:** `available_stock + reserved_stock = total_stock` is maintained by the
  `Inventory.reserve()` and `Inventory.release()` domain methods, enforced inside a transaction.

---

### Concurrency Architecture (Two-Tier Locking)

The system uses a **two-tier locking strategy** to handle high concurrency correctly and efficiently.

#### Tier 1 — Redis Distributed Lock (Application-level Gatekeeper)

**Location:** `ReservationLockFacade`

When a reservation request arrives, the facade acquires a per-SKU Redis lock **before** touching the database:

```java
boolean acquired = lock.tryLock(500ms wait, 10s lease, MILLISECONDS);
if (!acquired) throw new SystemBusyException(...); // → HTTP 429
```

- **Why Redis before DB?** Under Flash Sale load, if many threads go directly to the database,
  they all queue behind the PostgreSQL row lock. This exhausts the HikariCP connection pool and
  causes the application to hang (Thundering Herd). Redis rejects excess threads instantly at the
  application layer — no DB connection consumed, no pool pressure.
- **Fail-fast (500ms):** If a lock cannot be acquired within 500ms, the request immediately returns
  `HTTP 429 SYSTEM_BUSY`. This is intentional — under genuine overload, fast rejection is better
  than silent queuing.
- **Lease (10s):** Even if the JVM crashes mid-request, Redis automatically releases the lock after
  10 seconds, preventing deadlock.

#### Tier 2 — PostgreSQL Pessimistic Write Lock (Database-level Safety Net)

**Location:** `InventoryRepository.findBySkuInWithLock()`

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
@Query("SELECT i FROM Inventory i WHERE i.sku IN :skus ORDER BY i.sku")
List<Inventory> findBySkuInWithLock(List<String> skus);
```

- **`SELECT … FOR UPDATE`** ensures that even if two requests slip through Redis simultaneously,
  only one can modify an inventory row at a time. This is the final data integrity guarantee.
- **5-second DB timeout** prevents indefinite waiting if the PostgreSQL lock cannot be acquired.

#### Critical Ordering Rule

The Redis lock **must strictly wrap** the DB transaction — not be inside it:

```
Redis.tryLock()
  └─ ReservationTransactionService.createReservation()   ← @Transactional (DB tx starts + commits)
Redis.unlock()    ← called in finally, AFTER DB commit is complete
```

If the lock were released before the DB transaction committed, a second thread could acquire the
lock, read stale inventory data (before the first commit is visible), and produce an oversell.

#### Deadlock Prevention (both tiers)

Both Redis locks and DB locks are always acquired in **alphabetical SKU order**. This eliminates
the circular-wait condition: if thread A holds `lock:A100` and thread B holds `lock:B200`,
they both need the other lock in the same direction — so one always waits and the other completes.

---

## 6. How to Run

**Prerequisites:** Docker and Docker Compose

```bash
git clone <repo-url>
cd inventory-reservation
docker compose up --build
```

The startup sequence is: `postgres` → `liquibase` (schema + seed data) → `redis` → `app`.
The API is available at `http://localhost:8080` once the `app` container is running.

**Example requests:**

```bash
# Reserve inventory
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1001","items":[{"sku":"A100","quantity":5},{"sku":"B200","quantity":3}]}'

# Get reservation
curl http://localhost:8080/api/v1/reservations/<id>

# Confirm
curl -X POST http://localhost:8080/api/v1/reservations/<id>/confirm

# Cancel
curl -X POST http://localhost:8080/api/v1/reservations/<id>/cancel

# Check stock
curl http://localhost:8080/api/v1/inventory/A100
```

**Seed data loaded on startup:** A100 (100 units), B200 (50 units), C300 (200 units).

---

## 7. How to Run Tests

Docker must be running (Testcontainers pulls a PostgreSQL image for the integration test).

```bash
./mvnw test
```

Tests include:
- `ReservationStateTest` — unit tests for each State Pattern class (no Spring context)
- `ReservationTransactionServiceTest` — unit tests for all service business rules (mocked repositories)
- `ConcurrentReservationIT` — integration test with a real PostgreSQL container that fires two
  concurrent reservation requests exceeding available stock and asserts exactly one succeeds

---

## 8. Trade-offs

| Decision | Trade-off |
|----------|-----------|
| **Two-tier locking (Redis + PostgreSQL)** | Redis provides fail-fast application-level protection; PostgreSQL provides the final data integrity guarantee. The cost is added infrastructure (Redis) and more complex locking logic. |
| **Pessimistic DB lock** | Simpler correctness guarantee than optimistic lock; at the cost of reduced throughput under high contention. Optimistic lock would require client-side retry logic. |
| **Standalone Liquibase** | Migrations run once at startup, not embedded in the app. More infrastructure complexity but matches production practice where the app user has no DDL privileges. |
| **Composite PK on `reservation_items`** | Removes the need for a surrogate key and a separate unique index. The trade-off is slightly more complex JPA mapping (`@IdClass`). |
| **UUID for reservation ID** | Globally unique, safe to expose in URLs. Slightly larger index than a bigserial but avoids ID enumeration attacks. |
| **Manual mappers** | Explicit, easy to debug, no annotation processor required. The trade-off is more boilerplate compared to MapStruct. |

---

## 9. What Would Break at Scale

| Problem | Fix |
|---------|-----|
| **Single Redis node is a SPOF** | Use Redis Cluster or Redis Sentinel. Redisson supports both out of the box. |
| **Redis + DB two-phase failure window** | Between Redis unlock and the caller receiving the response, a crash could cause ambiguity. An outbox/saga pattern would make this fully atomic. |
| **Single PostgreSQL writer** | Introduce read replicas for GET endpoints. Consider sharding by warehouse or SKU range for write-heavy workloads. |
| **Synchronous reservation blocks the caller** | Move to an event-driven model: publish a `ReservationRequested` event, process it asynchronously, notify via webhook or polling. |
| **No idempotency beyond `order_id` uniqueness** | Add an idempotency key header so clients can safely retry failed requests without creating duplicates. |
