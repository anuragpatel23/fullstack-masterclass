# 10 — Java 9 → 24: Modern Java 🟡⭐

## Real-life analogy
Java's evolution is like **smartphone upgrades**: Java 8 was the touchscreen moment (lambdas changed everything). Records are like contact cards auto-filled from a form (no manual data entry = no boilerplate). **Virtual threads** are like switching from assigning one full-time employee per customer call (platform threads — expensive, limited) to a call-center system where thousands of calls share a few agents, parking calls on hold (unmounted) while waiting.

## Version-by-version (interview-relevant only)

### Java 9–11 (11 = first big LTS after 8)
- **Modules (JPMS, 9)**: `module-info.java`, strong encapsulation — know the concept.
- Collection factories (9): `List.of / Set.of / Map.of` — immutable, null-hostile ⭐.
- `Optional.ifPresentOrElse`, `Stream.takeWhile/dropWhile/iterate(seed,hasNext,next)` (9).
- **`var` (10)**: local-variable type inference — compile-time only, NOT dynamic typing ⭐.
- `String` methods (11): `isBlank strip lines repeat`; `Files.readString`; standard **HttpClient** (11); run single files: `java App.java`.

### Java 12–17 (17 = LTS)
- **Switch expressions (14)**: arrow labels, `yield`, exhaustive, no fall-through ⭐.
- **Text blocks (15)**: `"""` multi-line strings.
- **Records (16)** ⭐: transparent immutable data carriers — auto constructor/accessors/equals/hashCode/toString; **compact constructor** for validation; can implement interfaces, can't extend classes.
- **Sealed classes (17)** ⭐: `sealed interface Shape permits Circle, Square` — closed hierarchies enabling exhaustive pattern matching.
- Pattern matching for `instanceof` (16): `if (o instanceof String s)`.

### Java 18–21 (21 = current major LTS — expect deep questions)
- **Virtual threads (21)** ⭐⭐: JVM-managed lightweight threads (not OS threads). Millions possible; carrier threads mount/unmount at blocking points. Ideal for IO-bound servers ("thread-per-request is back"). Don't pool them; watch for **pinning** (long `synchronized` blocks — use ReentrantLock).
- **Record patterns + pattern matching for switch (21)** ⭐: destructure in `case Circle(double r) ->`; sealed + records = exhaustive ADT-style code.
- Sequenced collections (21): `getFirst/getLast/reversed` on List/Deque/LinkedHashMap.
- Structured concurrency & scoped values (preview in 21).

### Java 22–24 (latest, non-LTS — mention-worthy)
- Unnamed variables `_` (22): `catch (Exception _)`.
- **Stream gatherers (22–24, final in 24)**: custom intermediate ops (`Gatherers.windowFixed`, `fold`, `scan`).
- Statements before `super(...)` (22+ preview), simplified main/implicit classes (preview), class-file API (24), ahead-of-time class loading improvements (24). Generational ZGC default-ish since 23.

## Top interview questions
1. **Records vs Lombok vs classes?** Language-level, immutable by design, pattern-matching friendly; Lombok mutable options & annotation magic.
2. **Virtual vs platform threads?** M:N scheduling on carrier threads; blocking is cheap (unmount); don't pool; pinning caveat with synchronized.
3. **Will virtual threads make code faster?** No — they improve *throughput/scalability* of IO-bound work, not CPU speed.
4. **Can records have validation?** Yes — compact constructor (see code).
5. **Why sealed classes?** Controlled hierarchies + compiler-checked exhaustive switch (no default needed).
6. **`var` pitfalls?** Readability with unclear RHS; can't use for fields/params/returns; still statically typed.
7. **What did switch expressions fix?** Fall-through bugs, non-exhaustiveness, statement-vs-value awkwardness.

➡️ Code: [`ModernJavaDemo.java`](./ModernJavaDemo.java) *(requires JDK 21+)*
