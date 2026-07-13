# 07 — Multithreading & Concurrency 🔴⭐ (senior-level filter topic)

## Real-life analogy
A **restaurant kitchen**: chefs (threads) share one pantry (heap). Two chefs grabbing the last egg simultaneously = **race condition**. A lock on the pantry door = **synchronized** (safe but chefs queue up). A whiteboard everyone reads instantly when updated = **volatile** (visibility, not atomicity). The head chef handing tickets to a fixed team = **ExecutorService**. Two chefs each holding one of two pans the other needs, forever = **deadlock**.

## Fundamentals
- **Thread lifecycle**: NEW → RUNNABLE → (BLOCKED | WAITING | TIMED_WAITING) → TERMINATED.
- Create: extend `Thread` (avoid) vs implement `Runnable` (no result) vs `Callable<V>` (result + checked exceptions, returns `Future<V>`).
- `start()` creates a new thread; `run()` is just a method call on the current thread ⭐ classic trap.
- Daemon threads die with the JVM; user threads keep it alive.

## The three concurrency problems ⭐
1. **Atomicity**: `count++` = read+modify+write → lost updates. Fix: `synchronized`, `AtomicInteger` (CAS), locks.
2. **Visibility**: without happens-before, a thread may never see another's write (CPU caches/reordering). Fix: `volatile`, synchronized, final fields.
3. **Ordering**: JIT/CPU reorder instructions. `volatile`/locks insert memory barriers.

## Synchronization toolbox
| Tool | Use | Notes |
|---|---|---|
| `synchronized` | mutual exclusion + visibility | method (this / Class) or block; reentrant |
| `volatile` | visibility flag, no compound ops | perfect for `running = false` flags |
| `ReentrantLock` | tryLock, timed, interruptible, fair | must unlock in `finally` |
| `ReadWriteLock` | many readers / one writer | read-heavy caches |
| `Atomic*` | lock-free counters via **CAS** | `incrementAndGet`, `compareAndSet` |
| `ThreadLocal` | per-thread state (userContext, SimpleDateFormat) | **memory-leak risk in pools — always `remove()`** |

## Coordination
- `wait()/notify()/notifyAll()` — must own the monitor; **wait in a while-loop** (spurious wakeups). `wait` releases the lock; `sleep` doesn't. ⭐
- `CountDownLatch` (one-shot gate), `CyclicBarrier` (reusable meeting point), `Semaphore` (N permits), `BlockingQueue` (producer-consumer without manual wait/notify).

## Executor framework ⭐
- Never `new Thread()` in production → `ExecutorService`.
- `ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAlive, workQueue, rejectionHandler)` — know the flow: core threads → queue → extra threads up to max → reject. ⭐
- `submit` returns `Future`; `future.get()` blocks. Always `shutdown()`.
- Sizing: CPU-bound ≈ #cores; IO-bound ≈ #cores × (1 + wait/compute).

## CompletableFuture ⭐ (modern async — heavily asked)
`supplyAsync → thenApply (transform) → thenCompose (flatMap another future) → thenCombine (merge two) → exceptionally/handle (recover) → allOf/anyOf (fan-in)`.
Default pool = ForkJoinPool.commonPool; pass a custom executor for IO work.

## Deadlock / livelock / starvation
Deadlock = circular lock wait (fix: global lock ordering, tryLock with timeout). Livelock = threads keep yielding to each other. Starvation = low-priority thread never scheduled. Detect deadlocks: thread dump / `ThreadMXBean.findDeadlockedThreads()`.

## Top interview questions
1. **`start()` vs `run()`?** New call stack vs plain method call.
2. **`synchronized` vs `volatile`?** Mutual exclusion + visibility vs visibility only.
3. **Producer-consumer without BlockingQueue** — wait/notify in while loop (see code).
4. **How does ConcurrentHashMap achieve thread safety?** CAS for empty bins, synchronized per-bin for collisions; no global lock; reads lock-free.
5. **`Future` vs `CompletableFuture`?** Blocking get-only vs composable, non-blocking callbacks, exception recovery, manual completion.
6. **What is CAS? ABA problem?** Compare-and-swap CPU instruction; ABA fixed by `AtomicStampedReference`.
7. **ThreadPoolExecutor task flow when queue is full?** Spawn up to max threads, then RejectedExecutionHandler (AbortPolicy default; CallerRunsPolicy for backpressure).
8. **Why call wait() inside a loop?** Spurious wakeups + recheck condition after re-acquiring lock.
9. **Virtual threads?** → covered in `10-java-versions-9-to-24`.

➡️ Code: [`ConcurrencyDemo.java`](./ConcurrencyDemo.java)
