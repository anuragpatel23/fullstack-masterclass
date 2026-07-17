# 09 — Actuator & Observability 🟡⭐

## Real-life analogy
Actuator is the **dashboard + OBD port of a car**: while driving (app running), the dashboard shows speed and fuel (**metrics**), warning lights say if something's wrong (**health checks**), and a mechanic can plug into the OBD port to read detailed diagnostics (**endpoints**) without opening the engine (no code change/redeploy). You wouldn't let strangers plug into your OBD port → **secure your actuator endpoints**.

## Setup
`spring-boot-starter-actuator`. By default only `/actuator/health` is exposed over HTTP ⭐.
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
```

## Endpoints you must know ⭐
| Endpoint | Purpose |
|---|---|
| `/health` | UP/DOWN + component details (db, disk, redis…) — used by k8s probes ⭐ |
| `/metrics` / `/metrics/{name}` | Micrometer metrics (jvm.memory.used, http.server.requests ⭐) |
| `/prometheus` | metrics in Prometheus scrape format |
| `/info` | build/git info |
| `/env` | resolved config properties (SENSITIVE ⭐) |
| `/beans`, `/conditions` | all beans; auto-config decisions report |
| `/mappings` | all @RequestMapping routes |
| `/loggers` | **change log levels at runtime — no restart** ⭐ |
| `/threaddump`, `/heapdump` | diagnostics (heapdump = sensitive!) |
| `/shutdown` | disabled by default — leave it that way |

## Health checks in depth ⭐
- Built-in indicators auto-register (DataSource, Redis, RabbitMQ…). Aggregate status = worst of all.
- **Custom `HealthIndicator`**: implement `health()` — check a downstream API, queue depth, cert expiry (see code).
- **Liveness vs readiness** ⭐ (k8s): `/actuator/health/liveness` (restart me if failing) vs `/readiness` (stop sending traffic). Enable: `management.endpoint.health.probes.enabled=true`.

## Micrometer (the SLF4J of metrics) ⭐
- Facade over Prometheus/Datadog/etc. `MeterRegistry` → `Counter` (events), `Gauge` (current value), `Timer` (latency + count), `DistributionSummary` (sizes).
- `@Timed` on methods; tags/dimensions (`orders.placed{channel=web}`) for slicing.
- Out of the box: JVM memory/GC, CPU, HTTP request latencies, connection pool stats.

## The three pillars (senior framing)
**Metrics** (aggregates, alerting) + **Logs** (discrete events — structured JSON + correlation id) + **Traces** (request path across services — Micrometer Tracing/OpenTelemetry, trace id propagated via headers). Modern answer mentions OTel ⭐.

## Securing actuator ⭐
Expose selectively; require `ACTUATOR` role via SecurityFilterChain; or serve on a separate management port (`management.server.port=8081`) firewalled internally. `/env`, `/heapdump` leak secrets — the classic pen-test finding.

## Top interview questions
1. **What is Actuator and which endpoints have you used in production?** (health for probes, metrics, loggers for live log-level change = real-world credibility)
2. **How do you write a custom health check?** HealthIndicator bean (see code).
3. **Liveness vs readiness — and what happens if you get them wrong?** Restart loops vs black-holed traffic.
4. **How does metrics collection work? Counter vs Gauge vs Timer?**
5. **How do you secure actuator endpoints?** (exposure config + auth + separate port)
6. **How do you trace one request across 5 microservices?** Correlation/trace id propagation, OTel/Zipkin; MDC for logs.
7. **A prod issue needs DEBUG logs but you can't restart — what do you do?** POST to `/actuator/loggers/com.myapp` `{"configuredLevel":"DEBUG"}` ⭐.

➡️ Code: [`ActuatorDemo.java`](./ActuatorDemo.java)
