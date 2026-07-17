# 05 — Spring Data JPA 🟡⭐

## Real-life analogy
JPA/Hibernate is a **professional translator with a notebook**: you speak Java (objects), the database speaks SQL (rows). The translator also keeps a notebook (**persistence context / first-level cache**) of everyone you've talked about this meeting (transaction) — mention the same person twice and it reuses its notes (no second SELECT), and at the end of the meeting it writes down all agreed changes at once (**dirty checking + flush**). The **N+1 problem** is asking the translator about 100 customers, then asking "and their orders?" one customer at a time — 101 conversations instead of 1 with a JOIN.

## Layer cake
JDBC (raw) → JPA (spec: EntityManager, JPQL, annotations) → Hibernate (implementation) → Spring Data JPA (repository abstraction on top ⭐).

## Repositories ⭐
`Repository` → `CrudRepository` → `PagingAndSortingRepository` → `JpaRepository` (adds flush, batch, `saveAll`).
- **Derived queries**: `findByStatusAndCreatedAtAfter(String s, Instant t)` — parsed from the method name.
- `@Query("select o from Order o where o.total > :min")` — JPQL (entities, portable); `nativeQuery = true` — real SQL (Oracle hints, window functions).
- `@Modifying @Transactional` for UPDATE/DELETE queries (+ `clearAutomatically = true` — stale persistence context ⭐).
- **Projections**: interface/record projections fetch only needed columns.
- Pagination: `Page<T>` (runs a count query) vs `Slice<T>` (no count — cheaper) ⭐.

## Entity states & persistence context ⭐
`transient` (new, unknown) → `managed` (attached; **dirty checking**: changes auto-flushed, no save() needed! ⭐) → `detached` (tx ended) → `removed`.
`save()` = `persist` (new) or `merge` (detached copy → managed). First-level cache: `findById` twice in one tx = one SELECT.

## Relationships & fetching ⭐⭐
- Defaults: `@ManyToOne/@OneToOne` = **EAGER** (bad — override to LAZY!), `@OneToMany/@ManyToMany` = LAZY.
- **LazyInitializationException** ⭐: accessing a lazy field after the session closed (e.g., in Jackson serialization) — fix with fetch join / DTO mapping inside tx, NOT `spring.jpa.open-in-view=true` (know why OSIV is an anti-pattern: connection held for whole request).
- **N+1 problem & fixes** ⭐⭐: `join fetch` in JPQL, `@EntityGraph`, `@BatchSize`/`default_batch_fetch_size`. Detect: log SQL in tests, count queries.
- Bidirectional: the `@ManyToOne` side owns the FK; `mappedBy` on the other; keep both sides in sync with helper methods.
- `cascade = ALL` + `orphanRemoval = true` for parent-child lifecycles.

## Locking & concurrency ⭐
- **Optimistic** (default choice): `@Version` column — stale update → `OptimisticLockException` → retry. Best for low contention.
- **Pessimistic**: `@Lock(PESSIMISTIC_WRITE)` → `SELECT ... FOR UPDATE`. Best for hot rows (inventory).

## Caching
L1 = persistence context (per-tx, always on). L2 = shared, opt-in (Ehcache/Redis via provider) — entity cache + query cache; invalidation complexity means most teams cache at service layer instead.

## ID generation (Oracle angle ⭐)
`IDENTITY` (MySQL-style auto-inc; disables JDBC batching!) vs `SEQUENCE` (Oracle native, batch-friendly, `allocationSize` for fewer roundtrips) vs `TABLE` (avoid) vs `UUID`.

## Top interview questions
1. **Explain the N+1 problem and all fixes.** ⭐⭐ (see analogy + code)
2. **How does dirty checking work? Why no save() call needed?** Managed entities are snapshotted; flush compares & issues UPDATEs.
3. **LazyInitializationException — cause and proper fix?** (fetch within tx, DTOs; OSIV trade-off discussion = senior points)
4. **save() vs saveAndFlush()? persist vs merge?** Flush timing; new vs detached semantics.
5. **Page vs Slice?** Count query cost.
6. **Optimistic vs pessimistic locking — when each?** Contention level; @Version mechanics.
7. **Why prefer SEQUENCE over IDENTITY with Oracle/Hibernate?** Batching + pre-allocated ids.
8. **JPQL vs native query?** Portability + entity mapping vs DB-specific power.
9. **What is the owning side of a relationship?** The side with the FK (`@JoinColumn`); `mappedBy` marks the inverse.

➡️ Code: [`JpaDemo.java`](./JpaDemo.java)
