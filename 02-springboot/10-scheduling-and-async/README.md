# 10 — Scheduling & Async 🟡⭐

## Real-life analogy
`@Scheduled` is a **office cleaning crew on a timetable**: every night at 2 AM (cron) they run whether anyone asks or not. `@Async` is **handing a task to a colleague**: "email the customer while I finish the checkout" — you return immediately; they work in parallel. The trap: if there's only **one cleaner** (default single-threaded scheduler ⭐) and Monday's job takes 26 hours, Tuesday's job silently waits.

## @Scheduled ⭐
Enable with `@EnableScheduling`. Method must be no-arg; bean must be Spring-managed.

| Attribute | Meaning |
|---|---|
| `fixedRate = 5000` | start-to-start every 5s (runs can pile up conceptually — actually next waits if same thread busy) |
| `fixedDelay = 5000` | END-to-start: 5s after previous finishes ⭐ (the difference is a guaranteed question) |
| `initialDelay` | wait before first run |
| `cron = "0 0 2 * * MON-FRI"` | second minute hour day month weekday ⭐ (6 fields in Spring — not 5!) |

Cron examples: `0 */15 * * * *` every 15 min; `0 0 9 1 * *` 9 AM on the 1st; `zone = "Asia/Kolkata"`.

### The pitfalls ⭐
1. **Single-threaded by default**: all `@Scheduled` methods share ONE thread — a slow job delays every other job. Fix: configure `ThreadPoolTaskScheduler` (pool size) or `spring.task.scheduling.pool.size`.
2. **Multiple instances = duplicate runs** ⭐⭐: 3 pods → job fires 3×. Fixes: **ShedLock** (db-lock per run), Quartz clustered mode, leader election, or move to a proper job platform.
3. Exceptions don't kill the schedule but are swallowed silently — always try/catch + log/alert.
4. No overlap protection between *different* triggers of a slow fixedRate job across pool threads.

## @Async ⭐
Enable with `@EnableAsync`. Method returns `void` or `CompletableFuture<T>`.
- Works via **AOP proxy** → same rules: no self-invocation, public methods only ⭐.
- Default executor: `SimpleAsyncTaskExecutor`-ish behavior via `applicationTaskExecutor` (bounded in Boot); **always define your own `ThreadPoolTaskExecutor`** (core/max/queue/rejection) and reference: `@Async("emailExecutor")`.
- **Exceptions**: void async = swallowed (configure `AsyncUncaughtExceptionHandler`); CompletableFuture = surface via the future ⭐.
- **Context propagation** ⭐: SecurityContext, MDC (trace ids), and transactions do NOT cross threads by default — classic prod bug. `@Transactional` + `@Async` on the same method: the async method runs in its own tx (if any), never the caller's.

## Thread pool sizing (say the formula)
CPU-bound: cores. IO-bound: cores × (1 + wait/compute). Bounded queue + `CallerRunsPolicy` for backpressure.

## Top interview questions
1. **fixedRate vs fixedDelay?** Start-to-start vs end-to-start.
2. **Spring cron fields?** SIX: sec min hour dom month dow.
3. **Two scheduled jobs, one takes an hour — what happens to the other?** Blocked (single default thread) → pool config.
4. **How do you stop a scheduled job running on all 3 replicas?** ShedLock/Quartz cluster/leader election ⭐.
5. **Why didn't my @Async method run asynchronously?** Self-invocation / missing @EnableAsync / not public.
6. **Where do @Async exceptions go?** void → AsyncUncaughtExceptionHandler; CompletableFuture → exceptionally/handle.
7. **Is the caller's transaction/security context available inside @Async?** No — ThreadLocal-bound; use DelegatingSecurityContextAsyncTaskExecutor / task decorators for MDC.
8. **@Scheduled + @Async together?** Allows overlapping executions of the same job — sometimes desired, usually not.

➡️ Code: [`SchedulingAsyncDemo.java`](./SchedulingAsyncDemo.java)
