# 06 — Generics 🟡

## Real-life analogy
A **labeled shipping container**: a container marked "ELECTRONICS ONLY" (`List<Electronics>`) prevents someone tossing in furniture at the port (compile time), so nothing surprising falls out at the destination (no `ClassCastException` at runtime). **Type erasure** means once the ship sails (after compilation), the labels are painted over — the crane (JVM) just sees generic containers.

## Key concepts

### Why generics?
Compile-time type safety + no casts. Pre-Java-5 `List` held `Object` → runtime `ClassCastException`.

### Type erasure ⭐
The compiler erases type parameters: `List<String>` → `List` (bounded `T extends Number` → `Number`). Consequences:
- `new T()`, `new T[]`, `T.class` — illegal.
- `list instanceof List<String>` — illegal (only `List<?>`).
- `List<String>` and `List<Integer>` are the **same class** at runtime → can't overload on them.
- Compiler inserts casts + **bridge methods** to keep polymorphism working.

### Bounded types
- `<T extends Number>` — upper bound (T is Number or subtype; can call Number methods).
- `<T extends Comparable<T> & Serializable>` — multiple bounds (class first, then interfaces).

### Wildcards & PECS ⭐ (the interview favorite)
**P**roducer **E**xtends, **C**onsumer **S**uper:
- `List<? extends Fruit>` — you **read** Fruits out (producer). You can't add (except null) — compiler can't prove which subtype it really is.
- `List<? super Apple>` — you **write** Apples in (consumer). Reads come out as `Object`.
- `List<?>` — unknown; read-only as Object. Use when you only need size/clear etc.

`Collections.copy(List<? super T> dest, List<? extends T> src)` — the canonical PECS signature.

### Invariance ⭐
`List<Dog>` is **NOT** a `List<Animal>` (generics are invariant) — otherwise you could add a Cat into a Dog list. Arrays ARE covariant (`Dog[]` is `Animal[]`) and pay for it with runtime `ArrayStoreException` — a classic "generics vs arrays" question.

## Top interview questions
1. **What is type erasure and why does Java use it?** Backward compatibility with pre-generics bytecode; consequences above.
2. **Explain PECS with an example.** (Producer extends, consumer super — see code.)
3. **Why can't we do `new T()`?** T is erased at runtime; JVM wouldn't know what to instantiate. Workaround: pass `Class<T>` or a `Supplier<T>`.
4. **Why is `List<String>` not assignable to `List<Object>`?** Invariance — would break type safety on write.
5. **Difference between `List<?>`, `List<Object>`, raw `List`?** Unknown-but-safe / exactly-Object / unchecked legacy (compiler warnings, no safety).
6. **What are bridge methods?** Compiler-generated synthetic methods that reconcile erased signatures with overriding.
7. **Can generic type info survive at runtime?** Only in class metadata for *super types* (`getGenericSuperclass`) — the trick libraries like Jackson's `TypeReference` use.

➡️ Code: [`GenericsDemo.java`](./GenericsDemo.java)
