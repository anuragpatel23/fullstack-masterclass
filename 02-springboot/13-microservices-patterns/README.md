# 13 — Microservices Patterns 🔴⭐ (the system-design bridge)

## Real-life analogy
Monolith = **one giant department store**: everything under one roof, one entrance, renovate anything and the whole store closes. Microservices = **a shopping mall**: independent stores (services) with their own staff and tills (DB per service), a common entrance and directory (**API gateway + service discovery**), and if the food court catches fire, clothing stores keep selling (**fault isolation**) — provided the fire doors work (**circuit breakers**).

## Monolith vs microservices (be balanced — senior signal)
Microservices buy: independent deploys/scaling, tech freedom, team autonomy, fault isolation.
They cost: network failure modes, **distributed transactions**, observability complexity, infra overhead, data duplication. "Start with a modular monolith" is a respected answer ⭐.

## The core patterns ⭐

### Communication
- **Sync**: REST (`RestClient`/`WebClient` — RestTemplate is legacy ⭐), gRPC (internal, high-perf), **OpenFeign** (declarative interface clients).
- **Async**: events via Kafka/RabbitMQ — decoupling, buffering, replay (Kafka). Know: at-least-once delivery ⇒ **consumers must be idempotent** ⭐⭐.

### Resilience ⭐⭐ (Resilience4j)
- **Circuit breaker**: CLOSED → (failure rate > threshold) → OPEN (fail fast, no calls) → after wait, HALF_OPEN (probe) → CLOSED/OPEN. Prevents cascade failures & thread-pool exhaustion.
- **Retry** (+ exponential backoff + jitter; only for transient/idempotent ops!), **Timeout** (always set), **Bulkhead** (isolate thread pools per dependency), **Rate limiter**, **Fallback** (degraded response, cache, default).

### Data
- **Database per service** ⭐: no shared DB — services integrate via APIs/events, never each other's tables.
- **Saga** ⭐⭐ (distributed transactions without 2PC): sequence of local txs + **compensating actions** on failure. **Choreography** (each service reacts to events — simple, but flow is implicit) vs **Orchestration** (central coordinator — explicit, single place to reason).
- **Outbox pattern** ⭐: write business row + event row in ONE local tx; a relay publishes events — solves "DB updated but event lost."
- **CQRS**, **event sourcing** — know the concepts.

### Infrastructure
- **API Gateway** (Spring Cloud Gateway): routing, auth, rate limiting, single entry ⭐.
- **Service discovery** (Eureka/Consul/K8s DNS), **Config server** (centralized config; K8s ConfigMaps as alternative).
- **Distributed tracing**: trace id propagated across services (Micrometer Tracing/OTel) ⭐.

### Correctness
- **Idempotency** ⭐⭐: retries + at-least-once delivery make duplicates inevitable — dedupe keys, upserts, version checks.
- Eventual consistency: embrace, design UIs for it.

## Top interview questions
1. **How do services communicate and when do you choose async?** (coupling, spikes, multi-consumers, no response needed)
2. **Explain circuit breaker states & why it exists.** (fail fast > hang; thread exhaustion story ⭐)
3. **How do you handle a transaction across 3 services?** Saga + compensation; why not 2PC (blocking, availability).
4. **Choreography vs orchestration?** Trade-offs above.
5. **How do you guarantee an event is published when the DB commit succeeds?** Outbox pattern ⭐.
6. **Consumer got the same Kafka message twice — what happens?** Idempotent handler design.
7. **One slow downstream service is taking your whole app down. Diagnose & fix.** Missing timeouts → thread exhaustion → circuit breaker + bulkhead + timeout.
8. **How do you trace a request across services?** Trace/span ids, header propagation, OTel + Zipkin/Tempo.
9. **How do services find each other?** Discovery/registry or K8s service DNS.

➡️ Code: [`MicroservicesDemo.java`](./MicroservicesDemo.java)
