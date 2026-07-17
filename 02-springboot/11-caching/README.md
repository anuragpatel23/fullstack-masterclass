# 11 — Caching 🟡

## Real-life analogy
A cache is the **snack drawer next to your desk**: instead of walking to the supermarket (database) for every craving, you keep frequent items within arm's reach. The hard problems aren't storing snacks — they're **staleness** (the biscuits expired = TTL/invalidation) and **space** (drawer full = eviction policy). And if the whole office shares one drawer (Redis), everyone sees the same snacks (consistency across instances).

## Spring Cache Abstraction ⭐
Enable with `@EnableCaching` — works via **AOP proxies** (same self-invocation caveat as @Transactional!).

| Annotation | Behavior |
|---|---|
| `@Cacheable("products")` | check cache first; on miss, run method + store result ⭐ |
| `@CachePut` | ALWAYS run method, refresh the cache entry (updates) |
| `@CacheEvict` | remove entry; `allEntries = true` to flush the cache |
| `@Caching` | combine several on one method |
| `@CacheConfig` | class-level defaults |

Key control: `key = "#id"`, `key = "#user.id + ':' + #region"` (SpEL), `condition = "#id > 0"`, `unless = "#result == null"` ⭐ (don't cache empty results).

## Providers
Default = in-memory `ConcurrentHashMap` (no TTL, no eviction — dev only!). Production: **Caffeine** (local, high-perf, TTL+size) or **Redis** (distributed, shared across instances, survives restarts) — often both as L1+L2.

## Patterns ⭐ (system-design crossover)
- **Cache-aside** (what @Cacheable implements): app checks cache → miss → DB → populate.
- **Write-through / write-behind**: write to cache+DB together / async.
- **TTL** as the safety net for all invalidation bugs.
- **Stampede protection** ⭐: hot key expires → 1000 threads hit DB at once. Fixes: `sync = true` on @Cacheable (one loader, rest wait), jittered TTLs, early refresh.
- Eviction policies: LRU/LFU/FIFO (link to the LRU-cache coding question in Java module!).

## Invalidation strategy (the hard part — say this in interviews)
Write path must evict/refresh: `@CacheEvict` on update/delete methods keyed the same way. Multi-instance with local caches → stale entries on other nodes → use Redis or a pub-sub invalidation broadcast. "There are only two hard things in CS: cache invalidation and naming things."

## Top interview questions
1. **How does @Cacheable work internally?** AOP proxy intercepts, builds key, checks CacheManager, short-circuits on hit.
2. **@Cacheable vs @CachePut?** Skip-method-on-hit vs always-run-and-refresh.
3. **Why is the default cache manager unfit for production?** No TTL/eviction/size bound → memory leak.
4. **Local (Caffeine) vs distributed (Redis) cache?** Latency vs consistency across instances; L1/L2 combo.
5. **What is cache stampede and how do you prevent it?** (`sync=true`, jitter, locks)
6. **How do you keep cache and DB consistent?** Evict on write + TTL backstop; discuss ordering (evict after commit — `@TransactionalEventListener(AFTER_COMMIT)` for the senior answer ⭐).
7. **What must be true of cached objects?** Serializable (Redis), immutable ideally, cheap keys with proper equals/hashCode.

➡️ Code: [`CachingDemo.java`](./CachingDemo.java)
