# 06 — @Transactional Deep-Dive 🔴⭐⭐ (the senior-filter question)

## Real-life analogy
A transaction is a **wedding ceremony**: either ALL rituals complete and the marriage is registered (**commit**), or if anything fails midway, everyone goes home as if nothing happened (**rollback**) — there's no "half married." `@Transactional` is the **wedding planner who wraps the event**: your service method is the ceremony; the planner (proxy) opens the venue before, and registers or cancels everything after. **Self-invocation pitfall**: if the priest starts a side-ritual himself mid-ceremony without telling the planner, the planner doesn't even know it happened — no separate contract for it.

## How it ACTUALLY works ⭐⭐ (this is what they want to hear)
1. Spring sees `@Transactional` → wraps the bean in a **proxy** (JDK dynamic proxy if interface, else CGLIB subclass).
2. Caller invokes → hits the **proxy first**: `TransactionInterceptor` → `PlatformTransactionManager.getTransaction()` → obtains connection, `setAutoCommit(false)`, binds it to the **thread** (`ThreadLocal` via `TransactionSynchronizationManager`).
3. Your method runs; every repository call in the same thread reuses that bound connection.
4. Return → commit. RuntimeException → rollback. Then connection released.

### The consequences (all the classic pitfalls)
| Pitfall | Why |
|---|---|
| **Self-invocation doesn't work** ⭐⭐ | `this.innerTx()` bypasses the proxy — no transaction! Fix: move to another bean, or self-inject / `TransactionTemplate` |
| **private/final/static methods** | proxies can't intercept them (CGLIB can't override final/private) |
| **Checked exceptions DON'T roll back by default** ⭐⭐ | rollback rules = RuntimeException + Error only. Fix: `rollbackFor = Exception.class` |
| **Catching the exception swallows rollback** | unless already marked rollback-only → then `UnexpectedRollbackException` on commit ⭐ |
| **@Transactional on private method silently ignored** | proxy never sees it |
| **Different thread = no transaction** | tx is ThreadLocal-bound; `@Async`/new threads don't inherit it ⭐ |

## Propagation ⭐ (know all 7, master 4)
| Propagation | Behavior |
|---|---|
| **REQUIRED** (default) | join existing, else create new |
| **REQUIRES_NEW** | suspend current, run in NEW tx (audit logs that must survive rollback ⭐) |
| **NESTED** | savepoint within same tx — inner rollback rolls to savepoint only |
| **SUPPORTS** | join if exists, else non-transactional |
| NOT_SUPPORTED | suspend tx, run without |
| MANDATORY | must have existing tx, else exception |
| NEVER | must NOT have tx, else exception |

⭐ REQUIRED vs REQUIRES_NEW inner-failure question: with REQUIRED, inner failure marks the WHOLE tx rollback-only (outer catch can't save it). With REQUIRES_NEW, inner commits/rolls back independently.

## Isolation (bridges to the SQL module)
`DEFAULT` (DB's — Oracle = READ_COMMITTED), `READ_UNCOMMITTED` (dirty reads — Oracle doesn't support), `READ_COMMITTED` (non-repeatable reads possible), `REPEATABLE_READ`, `SERIALIZABLE`.
Attributes: `readOnly = true` (flush-mode optimization + intent), `timeout`, `rollbackFor/noRollbackFor`.

## Top interview questions
1. **How does @Transactional work internally?** (proxy → interceptor → tx manager → ThreadLocal connection ⭐)
2. **Why doesn't @Transactional work on a method called from the same class?** Self-invocation bypasses proxy.
3. **Checked exception thrown — rollback?** No! Default = unchecked only; `rollbackFor` to change.
4. **REQUIRED vs REQUIRES_NEW — inner method fails, outer catches. What happens?** rollback-only marking vs independent tx (see code).
5. **What is UnexpectedRollbackException?** Outer commit attempted after inner REQUIRED tx marked rollback-only.
6. **Does @Transactional work with @Async?** No — new thread, no bound tx; the async method needs its own.
7. **readOnly=true — what does it actually do?** Hint to Hibernate (no dirty-check snapshots, FlushMode.MANUAL) + possible driver optimizations; NOT a security guarantee.
8. **Where should @Transactional live?** Service layer (business unit of work), not controller/repository.

➡️ Code: [`TransactionalDemo.java`](./TransactionalDemo.java)
