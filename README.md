# 🚀 Full-Stack Interview Masterclass — Java • Spring Boot • React • Oracle SQL

> **The one-stop-shop roadmap to crack product-company full-stack interviews.**
> Every topic folder contains: `README.md` (theory + **one real-life analogy** + interview Q&A) and a **code file** with runnable illustrations.

---

## How to use this repo

1. Follow domains in order: **Java → Spring Boot → JavaScript → React → Database**.
2. In each topic: read the README, run/trace the code file, then answer the interview questions from memory.
3. Revisit the ⭐ topics 48 hours before any interview — they cover ~80% of what's actually asked.

**Legend:** 🟢 Beginner 🟡 Intermediate 🔴 Advanced ⭐ Interview hot-spot

---

## 1️⃣ `01-java/` — Core & Advanced Java

| # | Topic folder | Level | Covers |
|---|---|---|---|
| 01 | `01-java-basics` | 🟢 | JVM/JRE/JDK, memory model (heap/stack/metaspace), data types, String pool & immutability, wrapper classes, autoboxing, `equals` vs `==`, `hashCode` contract ⭐ |
| 02 | `02-oops` | 🟢⭐ | Encapsulation, inheritance, polymorphism (overloading vs overriding), abstraction, interfaces vs abstract classes, composition vs inheritance, SOLID |
| 03 | `03-strings-and-immutability` | 🟢⭐ | String vs StringBuilder vs StringBuffer, intern(), immutability benefits, common string interview problems |
| 04 | `04-collections-framework` | 🟡⭐ | List/Set/Map/Queue internals, **HashMap internals (hashing, treeification)**, ArrayList vs LinkedList, ConcurrentHashMap, fail-fast vs fail-safe, Comparable vs Comparator |
| 05 | `05-exception-handling` | 🟢⭐ | Checked vs unchecked, try-with-resources, custom exceptions, exception hierarchy, best practices, finally vs finalize |
| 06 | `06-generics` | 🟡 | Type erasure, bounded types, wildcards (`? extends` / `? super`), PECS rule |
| 07 | `07-multithreading-and-concurrency` | 🔴⭐ | Thread lifecycle, Runnable/Callable, synchronized, volatile, locks, ExecutorService, CompletableFuture, deadlock/livelock, ThreadLocal, atomic classes, producer-consumer |
| 08 | `08-java8-features` | 🟡⭐ | Lambdas, functional interfaces, **Streams API in depth**, Optional, method references, default/static methods, new Date-Time API |
| 09 | `09-java-memory-and-gc` | 🔴 | GC algorithms (G1, ZGC), memory leaks, strong/weak/soft references, JVM tuning flags, OutOfMemoryError types |
| 10 | `10-java-versions-9-to-24` | 🟡⭐ | Modules (9), var (10), records & text blocks (14/15), sealed classes (17), pattern matching & switch (17-21), **virtual threads (21)**, sequenced collections (21), Stream gatherers & unnamed variables (22-24) |
| 11 | `11-design-patterns` | 🔴⭐ | Singleton (thread-safe), Factory, Builder, Strategy, Observer, Decorator, Adapter, Proxy — with Java library examples |
| 12 | `12-io-serialization-networking` | 🟡 | IO vs NIO, serialization & serialVersionUID, transient, sockets basics |
| 13 | `13-reflection-and-annotations` | 🔴 | Reflection API, custom annotations, how frameworks (Spring) use them |
| 14 | `14-java-interview-coding-problems` | ⭐ | Top asked: string/array/collection manipulation, stream one-liners, immutable class, custom HashMap, singleton variants, producer-consumer |

## 2️⃣ `02-springboot/` — Spring Boot Framework

| # | Topic folder | Level | Covers |
|---|---|---|---|
| 01 | `01-spring-core-ioc-di` | 🟢⭐ | IoC container, DI types, ApplicationContext vs BeanFactory, bean scopes & lifecycle, circular dependencies |
| 02 | `02-springboot-fundamentals` | 🟢⭐ | Auto-configuration internals, starters, `@SpringBootApplication`, profiles, externalized config, embedded servers |
| 03 | `03-annotations-deep-dive` | 🟡⭐ | **Every must-know annotation**: stereotype, injection, config, web, JPA, validation — with when/why |
| 04 | `04-rest-api-development` | 🟢⭐ | Controllers, request/response handling, validation, exception handling (`@ControllerAdvice`), ResponseEntity, versioning, HATEOAS |
| 05 | `05-spring-data-jpa` | 🟡⭐ | Repositories, JPQL/native queries, entity relationships, N+1 problem, pagination, projections, Hibernate caching |
| 06 | `06-transactional-deep-dive` | 🔴⭐ | `@Transactional` internals (proxies!), propagation levels, isolation levels, rollback rules, self-invocation pitfall |
| 07 | `07-spring-aop` | 🔴⭐ | Aspects, advice types, pointcuts, JDK vs CGLIB proxies, real use-cases (logging, auditing, metrics) |
| 08 | `08-spring-security` | 🔴⭐ | Filter chain, authentication vs authorization, JWT, OAuth2, method security, CSRF/CORS, password encoding |
| 09 | `09-actuator-and-observability` | 🟡⭐ | Endpoints, custom health indicators, metrics, Micrometer, info contributors, securing actuator |
| 10 | `10-scheduling-and-async` | 🟡⭐ | `@Scheduled` (cron), `@Async`, thread pools, `@EnableScheduling` internals, distributed scheduling pitfalls |
| 11 | `11-caching` | 🟡 | `@Cacheable`/`@CacheEvict`/`@CachePut`, cache providers, Redis integration, cache-aside pattern |
| 12 | `12-testing` | 🟡⭐ | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, MockMvc, Mockito, Testcontainers |
| 13 | `13-microservices-patterns` | 🔴⭐ | Service discovery, config server, API gateway, circuit breaker (Resilience4j), Feign/RestClient, saga, idempotency, event-driven with Kafka |
| 14 | `14-springboot-interview-questions` | ⭐ | Rapid-fire Q&A: bean lifecycle, proxy pitfalls, startup flow, filter vs interceptor, `@Component` vs `@Bean`, etc. |

## 3️⃣ `03-javascript/` — JavaScript & ES6+

| # | Topic folder | Level | Covers |
|---|---|---|---|
| 01 | `01-js-fundamentals` | 🟢⭐ | var/let/const, hoisting, scopes, TDZ, data types, `==` vs `===`, type coercion, truthy/falsy |
| 02 | `02-functions-and-this` | 🟡⭐ | Function types, arrow functions, `this` binding rules, call/apply/bind, IIFE, default/rest params |
| 03 | `03-closures-and-scope` | 🟡⭐ | Lexical scope, closures, module pattern, common closure interview traps (loop + setTimeout) |
| 04 | `04-prototypes-and-classes` | 🔴 | Prototype chain, `__proto__` vs `prototype`, ES6 classes, inheritance, `new` keyword internals |
| 05 | `05-es6-plus-features` | 🟡⭐ | Destructuring, spread/rest, template literals, modules, Map/Set, Symbols, iterators/generators, optional chaining, nullish coalescing |
| 06 | `06-async-javascript` | 🔴⭐ | **Event loop (macro/micro tasks)**, callbacks, Promises, async/await, Promise.all/race/allSettled, error handling |
| 07 | `07-arrays-objects-methods` | 🟡⭐ | map/filter/reduce, shallow vs deep copy, object methods, polyfills (write your own map/bind/debounce) |
| 08 | `08-js-interview-questions` | ⭐ | Debounce/throttle, currying, memoization, flatten array, event delegation, output-prediction questions |

## 4️⃣ `04-react/` — React

| # | Topic folder | Level | Covers |
|---|---|---|---|
| 01 | `01-react-fundamentals` | 🟢⭐ | Virtual DOM & reconciliation, JSX, components, rendering flow, React 18/19 features |
| 02 | `02-components-props-state` | 🟢⭐ | Functional vs class components, props vs state, lifting state up, controlled vs uncontrolled, prop drilling |
| 03 | `03-lifecycle-methods` | 🟡⭐ | Class lifecycle phases, hook equivalents, mounting/updating/unmounting flow |
| 04 | `04-hooks-deep-dive` | 🔴⭐ | useState, useEffect (deps & cleanup!), useContext, useReducer, useMemo, useCallback, useRef, custom hooks, rules of hooks |
| 05 | `05-hoc-render-props-composition` | 🔴⭐ | Higher-Order Components, render props, composition patterns, when to use each |
| 06 | `06-state-management` | 🔴⭐ | Context API vs Redux, Redux Toolkit, middleware (thunk/saga), Zustand, server state (React Query) |
| 07 | `07-performance-optimization` | 🔴⭐ | React.memo, memoization, code splitting, lazy loading, list virtualization, avoiding re-renders, profiler |
| 08 | `08-routing-and-forms` | 🟡 | React Router v6+, protected routes, form handling, validation |
| 09 | `09-react-interview-questions` | ⭐ | Full Q&A bank: keys, refs, error boundaries, portals, fiber, SSR vs CSR, common output questions |

## 5️⃣ `05-database/` — SQL & Oracle

| # | Topic folder | Level | Covers |
|---|---|---|---|
| 01 | `01-sql-basics` | 🟢⭐ | SELECT, WHERE, ORDER BY, DISTINCT, LIMIT/FETCH, NULL handling, operators, DDL/DML/DCL/TCL |
| 02 | `02-joins-mastery` | 🟡⭐ | INNER/LEFT/RIGHT/FULL/CROSS/SELF joins, join pitfalls, anti-joins, top interview join puzzles |
| 03 | `03-aggregations-grouping` | 🟡⭐ | GROUP BY, HAVING, aggregate functions, ROLLUP/CUBE, common traps |
| 04 | `04-subqueries-and-ctes` | 🟡⭐ | Correlated subqueries, EXISTS vs IN, CTEs (WITH), recursive CTEs, derived tables |
| 05 | `05-window-functions` | 🔴⭐ | ROW_NUMBER, RANK, DENSE_RANK, LAG/LEAD, running totals, **Nth highest salary — every variant** |
| 06 | `06-indexes-and-performance` | 🔴⭐ | B-tree/bitmap/function-based indexes, execution plans, EXPLAIN PLAN, hints, when indexes are ignored |
| 07 | `07-transactions-and-locking` | 🔴 | ACID, isolation levels, dirty/phantom reads, Oracle MVCC, deadlocks |
| 08 | `08-plsql-and-oracle-specifics` | 🟡 | PL/SQL blocks, procedures, functions, triggers, cursors, packages, sequences, MERGE, Oracle data types |
| 09 | `09-schema-design-normalization` | 🟡 | Normal forms (1NF-BCNF), denormalization, keys, constraints, ER modeling |
| 10 | `10-sql-interview-questions` | ⭐ | The classic problem bank: duplicates, Nth highest, department-wise max, gaps & islands, pivoting |

---

## 📅 Suggested 10-week plan

| Week | Focus |
|---|---|
| 1–2 | Java 01–07 (basics → concurrency) |
| 3 | Java 08–14 (Java 8+, patterns, coding problems) |
| 4–5 | Spring Boot 01–08 (core → security) |
| 6 | Spring Boot 09–14 (actuator → microservices) |
| 7 | JavaScript 01–08 |
| 8 | React 01–09 |
| 9 | Database 01–10 |
| 10 | Revision: all ⭐ folders + mock interviews |

## 🎯 Final-week revision checklist (the 80/20)

- Java: HashMap internals, concurrency, Streams, virtual threads, immutability, singleton
- Spring: `@Transactional` proxy pitfalls, bean lifecycle, security filter chain, AOP, annotations
- JS: event loop, closures, `this`, promises, debounce/throttle
- React: hooks rules, useEffect cleanup, re-render optimization, reconciliation & keys
- SQL: joins on NULLs, window functions, Nth highest salary, indexing decisions
