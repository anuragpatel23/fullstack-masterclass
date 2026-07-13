# 13 — Reflection & Annotations 🔴 (how Spring works under the hood)

## Real-life analogy
Reflection is an **X-ray machine + robotic surgeon**: normally you interact with a person by talking (public API); with an X-ray you can inspect their internal structure (private fields/methods), and the robot can even operate on them (invoke/modify) without the person's cooperation. **Annotations are sticky notes** on files in an office: the note itself does nothing — but the mailroom staff (Spring, JPA, Jackson) *read* the notes and act on them: "SCAN ME" (`@Component`), "FRAGILE — wrap in transaction" (`@Transactional`).

## Reflection API
- Entry points: `obj.getClass()`, `MyClass.class`, `Class.forName("com.x.MyClass")`.
- Inspect: `getDeclaredFields/Methods/Constructors` (all, incl. private) vs `getFields/...` (public incl. inherited).
- Act: `field.setAccessible(true); field.get(obj)`, `method.invoke(obj, args)`, `constructor.newInstance(...)`.
- Costs: slower than direct calls, breaks encapsulation, no compile-time safety, blocked by JPMS strong encapsulation for JDK internals (`--add-opens`).
- Modern alternative for hot paths: `MethodHandles` / `LambdaMetafactory`.

## Annotations
- Meta-annotations you must know ⭐:
  - `@Retention`: `SOURCE` (Lombok, `@Override`) / `CLASS` (default) / **`RUNTIME`** (Spring, JPA — readable via reflection).
  - `@Target`: TYPE, METHOD, FIELD, PARAMETER, ANNOTATION_TYPE...
  - `@Inherited`, `@Documented`, `@Repeatable`.
- Elements: primitives, String, Class, enums, annotations, arrays of those. `value()` gets shorthand syntax.

## How frameworks use this ⭐ (the senior-level answer)
1. **Spring component scan**: classpath scanning → find classes annotated `@Component` → instantiate via reflection → inject `@Autowired` fields (`setAccessible(true)`).
2. **@Transactional/AOP**: reflection discovers annotated methods → wraps bean in a **proxy** → proxy opens/commits transaction around `method.invoke()`.
3. **JPA**: reads `@Entity/@Column` at runtime to map rows ↔ fields.
4. **Jackson**: reflects on getters/fields (or `@JsonProperty`) to serialize.
5. **JUnit**: finds `@Test` methods reflectively and invokes them.

The code file builds a **mini dependency-injection container** — the single best interview story for this topic.

## Top interview questions
1. **What is reflection and where have you seen it used?** (Frameworks list above; also debuggers, serializers.)
2. **Downsides of reflection?** Performance, safety, encapsulation, module restrictions.
3. **Write a custom annotation and process it.** Requires `@Retention(RUNTIME)` — see code.
4. **Why does Spring need a no-arg constructor / how does it inject private fields?** `setAccessible(true)` — no setter needed.
5. **Difference between `Class.forName` and `ClassLoader.loadClass`?** forName runs static initializers by default; loadClass doesn't.
6. **How does reflection break Singleton?** Access private constructor → second instance. Enum is immune (JVM forbids reflective enum construction).
7. **`@Retention` levels and which one Spring annotations use?** RUNTIME — must be visible to the container.

➡️ Code: [`ReflectionDemo.java`](./ReflectionDemo.java)
