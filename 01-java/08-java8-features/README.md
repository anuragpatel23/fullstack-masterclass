# 08 — Java 8 Features 🟡⭐ (guaranteed interview round)

## Real-life analogy
**Streams are a factory conveyor belt**: raw items (collection) enter, pass through stations (filter, map — each station only *describes* what it does), and nothing actually moves until someone at the end presses START (**terminal operation** — lazy evaluation!). A **lambda** is a work instruction you hand a worker written on a sticky note, instead of hiring (declaring) a whole named employee (class) for one job.

## Lambdas & Functional Interfaces ⭐
- Lambda = implementation of a **functional interface** (exactly one abstract method, `@FunctionalInterface`).
- Built-ins you must know cold:

| Interface | Signature | Example |
|---|---|---|
| `Predicate<T>` | T → boolean | `s -> s.isEmpty()` |
| `Function<T,R>` | T → R | `String::length` |
| `Consumer<T>` | T → void | `System.out::println` |
| `Supplier<T>` | () → T | `ArrayList::new` |
| `BiFunction<T,U,R>` | (T,U) → R | `(a,b) -> a+b` |
| `UnaryOperator<T>` | T → T | `String::trim` |

- Method references: `Type::static`, `obj::instance`, `Type::instanceMethod` (first arg = receiver), `Type::new`.
- Lambdas capture **effectively final** locals only ⭐ (stack frame may be gone when lambda runs).

## Streams API ⭐⭐
- **Intermediate** (lazy, return Stream): `filter map flatMap distinct sorted peek limit skip`.
- **Terminal** (trigger execution): `collect forEach reduce count anyMatch findFirst min max toList`.
- Streams are single-use; don't mutate the source inside operations.
- `flatMap` flattens nested structures (`List<List<T>>` → Stream<T>) ⭐ favorite question.
- **Collectors**: `toList toSet toMap joining counting averagingInt summingInt groupingBy partitioningBy mapping`.
  - `groupingBy(classifier, downstream)` — e.g., dept → avg salary. THE most-asked stream pattern.
- `reduce(identity, accumulator)` for folding; prefer specialized (`sum()`, collectors) where possible.
- Primitive streams (`IntStream.range`, `mapToInt`) avoid boxing.
- **Parallel streams**: ForkJoinPool.commonPool; only for large, CPU-bound, stateless, non-ordered work. Never for IO.

## Optional ⭐
- Container for maybe-absent value; **return type, not field/param**.
- `map / flatMap / filter / orElse / orElseGet (lazy!) / orElseThrow / ifPresent`.
- ⚠️ `orElse(expensive())` ALWAYS evaluates; `orElseGet(() -> expensive())` only when empty.
- Anti-patterns: `opt.get()` without check, `Optional.of(nullable)` (use `ofNullable`), Optional fields.

## Interface default & static methods
Allowed Java to evolve `Collection` (add `stream()`) without breaking implementors. Diamond conflicts must be overridden (`A.super.m()`).

## New Date-Time API (java.time) ⭐
`LocalDate/LocalTime/LocalDateTime` (no zone), `ZonedDateTime`, `Instant` (epoch), `Duration` (time) vs `Period` (dates), `DateTimeFormatter` (thread-safe, unlike SimpleDateFormat ⭐).

## Top interview questions
1. **map vs flatMap?** 1→1 transform vs 1→many flatten (see code).
2. **Intermediate vs terminal? Why "lazy"?** Nothing runs until terminal op; enables short-circuiting (`findFirst` + `filter` processes minimum elements).
3. **Group employees by dept with average salary** — `groupingBy(Employee::dept, averagingDouble(Employee::salary))`.
4. **Find second-highest salary via streams** — `sorted(reversed()).skip(1).findFirst()` or `distinct()` first.
5. **Why effectively final?** Captured by value; mutation would create races and stale copies.
6. **`orElse` vs `orElseGet`?** Eager vs lazy — production performance bug.
7. **When are parallel streams harmful?** Small data, IO-bound tasks, shared mutable state, ordered ops; common-pool starvation in web apps.
8. **Can you reuse a stream?** No — IllegalStateException. Re-create from source.

➡️ Code: [`Java8Demo.java`](./Java8Demo.java)
