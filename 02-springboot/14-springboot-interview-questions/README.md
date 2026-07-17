# 14 — Spring Boot Rapid-Fire Q&A ⭐ (final revision sheet)

One-liner answers you should deliver in under 30 seconds each. Deep dives live in the topic folders.

## Core & container
1. **What is a bean?** An object managed (created/wired/destroyed) by the Spring container.
2. **@Component vs @Bean?** Auto-scanned class annotation vs manual factory method in @Configuration (for 3rd-party/conditional beans).
3. **Default bean scope? Thread-safe?** Singleton per container; NOT thread-safe — keep beans stateless.
4. **Bean lifecycle one-breath version:** instantiate → inject → Aware → BPP-before → @PostConstruct → BPP-after (proxies here!) → ready → @PreDestroy.
5. **@Autowired on a field vs constructor?** Constructor: final, fail-fast, testable. Field: reflection magic, avoid.
6. **Circular dependency fix?** Redesign; else @Lazy one side. Boot 2.6+ rejects them by default.
7. **@Primary vs @Qualifier?** Default candidate vs explicit selection (qualifier wins).
8. **BeanFactory vs ApplicationContext?** Basic lazy container vs full-featured (events, AOP, auto-config) — always the latter.

## Boot machinery
9. **@SpringBootApplication =** @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan.
10. **Auto-config in one breath:** classpath + conditions (`@ConditionalOnClass/OnMissingBean`) decide which candidate configs from the imports file create beans; your own beans win.
11. **Override an auto-configured bean?** Define your own of the same type — auto-config backs off.
12. **Config precedence?** CLI args > env vars > profile yml > application.yml > defaults.
13. **@Value vs @ConfigurationProperties?** One-off SpEL vs type-safe validated groups.
14. **Change embedded server?** Swap starter dependencies.
15. **How does `java -jar` work for a fat jar?** Nested-jar layout + Boot's JarLauncher.

## Web
16. **Request flow?** Filters → DispatcherServlet → HandlerMapping → ArgumentResolvers/Jackson → controller → MessageConverter → response.
17. **Filter vs Interceptor?** Servlet-spec before DispatcherServlet vs Spring handler-aware around controller.
18. **@Controller vs @RestController?** View resolution vs @ResponseBody serialization.
19. **PUT vs PATCH? Idempotent verbs?** Full replace (idempotent) vs partial; GET/PUT/DELETE idempotent, POST not.
20. **401 vs 403?** Unauthenticated vs unauthorized.
21. **Global exception handling?** @RestControllerAdvice + @ExceptionHandler (+ ProblemDetail).
22. **Validation?** @Valid @RequestBody + jakarta constraints → 400 with field errors.

## Data
23. **N+1 problem?** Lazy children loaded per-parent; fix: join fetch / @EntityGraph / batch size.
24. **Dirty checking?** Managed entities auto-flush changes at commit — no save() needed.
25. **LazyInitializationException?** Lazy access after session close; fetch in tx, map to DTO; OSIV off.
26. **Page vs Slice?** Page runs count query; Slice doesn't.
27. **Optimistic vs pessimistic lock?** @Version+retry (low contention) vs SELECT FOR UPDATE (hot rows).
28. **IDENTITY vs SEQUENCE for Oracle?** SEQUENCE — batching + preallocated ids.

## @Transactional (the favorite)
29. **How does it work?** Proxy wraps method: begin → invoke → commit/rollback; connection bound to ThreadLocal.
30. **Self-invocation?** Bypasses proxy — no transaction. Move to another bean.
31. **Checked exception → rollback?** No (unchecked only). `rollbackFor = Exception.class`.
32. **REQUIRES_NEW use case?** Audit log that must survive business rollback.
33. **UnexpectedRollbackException?** Inner REQUIRED tx marked rollback-only; outer tried to commit.
34. **@Transactional + @Async?** Tx doesn't cross threads — async method needs its own.

## AOP / Security / Ops
35. **Spring AOP vs AspectJ?** Runtime proxies (Spring beans, methods) vs bytecode weaving (everything).
36. **JDK vs CGLIB proxy?** Interface-based vs subclass; Boot defaults CGLIB.
37. **Where is the logged-in user stored?** SecurityContextHolder (ThreadLocal).
38. **JWT parts + weakness?** header.payload.signature; revocation is hard → short TTL + refresh.
39. **Why BCrypt?** Salted + deliberately slow → brute-force resistant.
40. **CSRF off for REST APIs — why OK?** No cookie-based auth; token in header isn't auto-sent cross-site.
41. **Actuator endpoints you actually use?** health (probes), metrics/prometheus, loggers (live log level!), info.
42. **Liveness vs readiness?** Restart me vs don't route traffic to me.
43. **fixedRate vs fixedDelay? Cron fields?** Start-to-start vs end-to-start; 6 fields (seconds first).
44. **Scheduled job on 3 replicas?** ShedLock/Quartz cluster/leader election.
45. **@Cacheable pitfalls?** Proxy self-invocation, default manager has no TTL, stampede (sync=true).

## Microservices
46. **Circuit breaker states?** CLOSED → OPEN (fail fast) → HALF_OPEN (probe) → back.
47. **Distributed transaction?** Saga with compensations; 2PC blocks — avoided.
48. **Exactly-once delivery?** Practically: at-least-once + idempotent consumers.
49. **Outbox pattern?** Event row committed with business row; relay publishes — no lost events.
50. **RestTemplate vs WebClient vs RestClient?** Legacy blocking / reactive / modern blocking-fluent (Boot 3.2+).

➡️ Code: [`RapidFireSnippets.java`](./RapidFireSnippets.java) — the 5 snippets most often asked to be *written live*.
