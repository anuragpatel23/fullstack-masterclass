# 07 — Spring AOP 🔴⭐

## Real-life analogy
AOP is **airport security applied to every gate**: security screening (logging, auth, metrics) is needed at every boarding gate (method), but you don't make every gate agent do pat-downs — a **separate security team** (aspect) is stationed at checkpoints (**pointcuts**) and screens passengers before/after they pass (**advice**). Gates stay focused on boarding (business logic). The passenger never notices they walked through a wrapper (**proxy**).

## Vocabulary ⭐ (get these crisp)
- **Cross-cutting concern**: logic needed everywhere (logging, security, tx, metrics) that would otherwise be duplicated.
- **Aspect**: the class packaging that concern (`@Aspect`).
- **Join point**: a point where an aspect *can* apply — in Spring AOP: method executions only.
- **Pointcut**: expression selecting *which* join points.
- **Advice**: *what* runs — `@Before`, `@After` (finally), `@AfterReturning`, `@AfterThrowing`, `@Around` (most powerful — controls whether/how the target runs).
- **Weaving**: linking aspects to code. Spring = **runtime proxy-based**; AspectJ = compile/load-time bytecode weaving (more powerful: fields, constructors, self-calls).

## Proxy mechanics ⭐⭐ (same engine as @Transactional)
- Target implements an interface → **JDK dynamic proxy** (implements the interface).
- No interface → **CGLIB** subclass proxy (Boot defaults to CGLIB for everything: `proxyTargetClass=true`).
- Hence the same limitations: **no self-invocation interception, no private/final methods** ⭐.
- Proxies are created in `BeanPostProcessor.postProcessAfterInitialization` (ties back to bean lifecycle ⭐).

## Pointcut expressions ⭐
```java
execution(* com.shop.service.*.*(..))          // any method in service package
execution(public * *..OrderService.place*(..)) // name pattern
@annotation(com.shop.Audited)                  // methods with custom annotation ← most practical
within(com.shop.service..*)                    // package tree
bean(*Service)                                 // bean-name pattern
args(java.lang.String, ..)                     // by runtime arg types
```
Combine: `execution(...) && !within(...)`.

## Real-world uses (name 3+ in interviews)
1. **@Transactional** — TransactionInterceptor is Around advice.
2. Logging/tracing with correlation ids; **execution-time metrics** (`@Timed`).
3. Security (`@PreAuthorize` — MethodSecurityInterceptor).
4. Retries (`@Retryable`), rate limiting, idempotency.
5. Auditing (who changed what), caching (`@Cacheable`).

## Top interview questions
1. **What problem does AOP solve?** Duplication + tangling of cross-cutting concerns; SRP at scale.
2. **Spring AOP vs AspectJ?** Proxy-based runtime (methods of Spring beans only, self-call blind spot) vs bytecode weaving (everything, faster, more setup).
3. **JDK proxy vs CGLIB?** Interface-based vs subclassing; final classes/methods break CGLIB; Boot defaults CGLIB.
4. **Order of multiple aspects?** `@Order` / `Ordered` — lower value = outer wrapper.
5. **@Around vs @Before — when must you use Around?** Need to control invocation: timing, retry, short-circuit (cache), modify args/return.
6. **Why doesn't my aspect fire?** Self-invocation, private method, final method, bean not Spring-managed, wrong pointcut — the debugging checklist ⭐.
7. **What's `ProceedingJoinPoint`?** Handle to the intercepted call in Around advice — `proceed()` invokes the target (0..N times).

➡️ Code: [`AopDemo.java`](./AopDemo.java)
