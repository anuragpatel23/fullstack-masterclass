# 11 — Design Patterns 🔴⭐

## Real-life analogy
Patterns are **standard architectural blueprints**: you don't reinvent "how to build a staircase" for every house. A **Singleton** is the one national power grid; a **Factory** is a restaurant kitchen — you order "pasta" and don't care which chef or pan produces it; a **Builder** is a Subway sandwich counter (choose each layer, then "build"); an **Observer** is a YouTube subscription (creator publishes, subscribers get notified); a **Decorator** is adding toppings to a base pizza; a **Strategy** is choosing car/bike/walk in Google Maps for the same "navigate" request.

## The ones interviews actually ask

### Creational
- **Singleton** ⭐⭐: one instance, global access. Must know ALL variants: eager, lazy (broken), synchronized (slow), **double-checked locking with volatile**, static holder (best lazy), **enum (Josh Bloch's choice — reflection & serialization safe)**. Know how reflection/serialization/cloning can break the others.
- **Factory Method**: creation behind a method — `LoggerFactory.getLogger`, `Calendar.getInstance`.
- **Abstract Factory**: families of related objects.
- **Builder** ⭐: many optional params, immutable result, fluent API — `StringBuilder`, `Stream.builder()`, Lombok `@Builder`. Kills telescoping constructors.
- **Prototype**: clone expensive objects.

### Structural
- **Adapter**: convert one interface to another — `Arrays.asList`, `InputStreamReader`.
- **Decorator** ⭐: wrap to add behavior, same interface — the entire `java.io` stack (`new BufferedReader(new InputStreamReader(...))`), `Collections.unmodifiableList`.
- **Proxy** ⭐: same interface, controls access (lazy load, security, remote). **This is how Spring AOP/@Transactional works** — the crossover question.
- **Facade**: simple front over complex subsystem (service layer over DAOs).
- **Composite**: tree of part-whole (UI components, file system).

### Behavioral
- **Strategy** ⭐: swap algorithm at runtime — `Comparator`, payment methods. Lambda-friendly.
- **Observer** ⭐: pub-sub — listeners, `PropertyChangeListener`, Kafka conceptually, React state → re-render!
- **Template Method**: skeleton in abstract class, steps in subclasses — `JdbcTemplate` (name!), servlet `service()`.
- **Chain of Responsibility**: handlers in a chain — servlet filters, Spring Security filter chain ⭐.
- **Command**: encapsulate request as object — `Runnable`.
- **Iterator**, **State**, **Mediator** — recognize on sight.

## JDK/Spring examples table (memorize — instant senior credibility)
| Pattern | In the wild |
|---|---|
| Singleton | `Runtime.getRuntime()`, Spring beans (default scope) |
| Factory | `Integer.valueOf`, `Executors.newFixedThreadPool` |
| Builder | `StringBuilder`, `HttpRequest.newBuilder()` |
| Decorator | `java.io` streams, `Collections.synchronizedList` |
| Proxy | Spring AOP, JPA lazy loading, mocks |
| Strategy | `Comparator`, `RejectedExecutionHandler` |
| Observer | listeners, reactive streams |
| Template Method | `JdbcTemplate`, `AbstractList` |
| Adapter | `Arrays.asList`, `Collections.list(Enumeration)` |
| Chain of Resp. | servlet filters, Spring Security |

## Top interview questions
1. **Write a thread-safe Singleton.** Show double-checked locking + `volatile`, then say "in practice: enum or static holder" (see code, all 5 variants).
2. **Why volatile in DCL?** Without it, another thread may see a *partially constructed* object due to instruction reordering.
3. **Singleton vs static class?** Lazy init, interfaces/polymorphism, serialization control, DI-friendly.
4. **Strategy vs Template Method?** Composition + runtime swap vs inheritance + fixed skeleton.
5. **Decorator vs Proxy vs Adapter?** Adds behavior / controls access / converts interface — same wrapping shape, different intent.
6. **Which patterns does Spring use?** Singleton (beans), Factory (BeanFactory), Proxy (AOP), Template (JdbcTemplate), Observer (events), Adapter (HandlerAdapter), Chain (filters).
7. **How can a Singleton be broken?** Reflection (setAccessible on constructor), serialization (fix: `readResolve`), cloning, multiple classloaders. Enum survives all.

➡️ Code: [`PatternsDemo.java`](./PatternsDemo.java)
