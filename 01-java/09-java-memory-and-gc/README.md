# 09 — Java Memory Model & Garbage Collection 🔴

## Real-life analogy
GC is a **hotel housekeeping service**: guests (objects) check into rooms (heap). Housekeeping never asks guests to leave — it only cleans rooms whose guests are *unreachable* (no key card held by anyone). New guests go to a small floor (**Eden**); ones who stay a few nights get promoted to long-stay floors (**old generation**). "Stop-the-world" = briefly locking the lobby so housekeeping can count occupants accurately.

## Heap layout (generational hypothesis: most objects die young)
```
Young Gen:  Eden  | Survivor S0 | Survivor S1     (minor GC — frequent, fast)
Old Gen:    long-lived objects                    (major/full GC — slower)
Metaspace:  class metadata (native memory, replaced PermGen in 8)
```
Object flow: allocated in Eden → survives minor GC → copied between survivors (age++) → tenuring threshold reached → promoted to old gen.

## How GC finds garbage
**Reachability** from GC roots (thread stacks, static fields, JNI refs) — mark & sweep/copy/compact. Not reference counting (cycles would leak).

## Collectors you should name-drop
| Collector | Design | When |
|---|---|---|
| Serial | single-threaded | tiny heaps, containers with 1 CPU |
| Parallel | throughput, multi-threaded STW | batch jobs |
| **G1** (default 9+) | heap in regions, predictable pauses | general server default |
| **ZGC** | colored pointers, <1ms pauses, TB heaps | latency-critical |
| Shenandoah | concurrent compaction | similar goals to ZGC |

## Reference strength ⭐
- **Strong** — normal; never collected while reachable.
- **Soft** — collected only under memory pressure (memory-sensitive caches).
- **Weak** — collected at next GC (`WeakHashMap` — canonical for metadata maps).
- **Phantom** — enqueued after finalization; for cleanup hooks (replaces `finalize()`, which is deprecated — use `Cleaner`/try-with-resources).

## Memory leaks in a GC'd language ⭐ (favorite senior question)
Objects still *reachable* but never *used*:
1. `static` collections that only grow.
2. Unremoved listeners/callbacks.
3. **ThreadLocal in thread pools** (thread lives forever → value lives forever).
4. Unclosed resources (native memory).
5. HashMap keys with broken/mutated hashCode (unreachable via API but strongly held).
6. Inner classes holding implicit outer references.

## OOM flavors
`Java heap space` (heap), `GC overhead limit exceeded` (98% time in GC), `Metaspace` (classloader leaks — hot redeploys), `unable to create new native thread` (thread limits), `Direct buffer memory` (NIO).

## Tuning flags worth knowing
`-Xms/-Xmx` (heap min/max — set equal in containers), `-Xss` (stack), `-XX:MaxMetaspaceSize`, `-XX:+HeapDumpOnOutOfMemoryError`, `-Xlog:gc*`. Analyze dumps with Eclipse MAT / VisualVM; `jstat -gcutil`, `jmap`, `jcmd` for live inspection.

## Top interview questions
1. **How does GC decide what to collect?** Reachability from GC roots, not reference counts.
2. **Minor vs major GC?** Young-gen (fast, frequent) vs old-gen/full (slow, STW heavier).
3. **Why generational GC?** Weak generational hypothesis — most objects die young; collecting Eden is cheap (copy survivors, wipe the rest).
4. **Can you force GC?** `System.gc()` is only a *hint* — never rely on it.
5. **Soft vs weak reference use cases?** Cache vs canonical mapping (see code).
6. **How would you debug a memory leak in production?** Heap dump on OOME → MAT dominator tree → find growing retained set; correlate with `jstat` trends.
7. **Why can ThreadLocal leak?** Pool threads never die; entry value stays strongly referenced. Always `remove()` in a finally.
8. **Is String pool in heap or metaspace?** Heap (since Java 7).

➡️ Code: [`MemoryDemo.java`](./MemoryDemo.java)
