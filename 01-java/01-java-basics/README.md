# 01 — Java Basics 🟢⭐

## Real-life analogy
Think of the **JDK as a full kitchen** (tools to cook = compile & run), the **JRE as a dining room** (you can only eat = run), and the **JVM as the chef** who takes a standard recipe (bytecode) and adapts it to whatever local ingredients (OS/CPU) are available. That's why Java is "write once, run anywhere" — the recipe never changes, only the chef's execution does.

## Core concepts

### JDK vs JRE vs JVM
- **JVM**: executes bytecode; provides GC, JIT compilation, memory management. Platform-dependent.
- **JRE**: JVM + core libraries. Enough to *run* Java.
- **JDK**: JRE + compiler (`javac`), tools (`jar`, `javadoc`, `jconsole`). Needed to *develop*.

### Compilation flow
`.java` → `javac` → `.class` (bytecode) → ClassLoader → Bytecode Verifier → JIT/Interpreter → machine code.

### JVM memory model ⭐
| Area | Stores | Shared? |
|---|---|---|
| **Heap** | Objects, instance fields (young gen: Eden + survivors; old gen) | Yes |
| **Stack** | Local variables, references, method frames (one per thread) | No |
| **Metaspace** | Class metadata, static structure (replaced PermGen in Java 8) | Yes |
| **PC Register** | Current instruction per thread | No |
| **Native method stack** | JNI calls | No |

### Data types
- 8 primitives: `byte(1) short(2) int(4) long(8) float(4) double(8) char(2) boolean(~1)`.
- Wrapper classes + **autoboxing/unboxing**. ⚠️ Integer cache: `-128..127` are cached → `Integer a=127, b=127; a==b` is `true`, but `128==128` (boxed) is `false`!

### `==` vs `equals()` vs `hashCode()` ⭐
- `==` compares **references** (or primitive values).
- `equals()` compares **logical content** (if overridden; default is reference equality).
- **Contract**: if `a.equals(b)` then `a.hashCode() == b.hashCode()`. Reverse not required. Break this → HashMap lookups silently fail.

### Other must-knows
- `final` (variable = constant, method = no override, class = no inheritance), `static` (class-level, loaded once), `this`/`super`.
- Pass-by-value ONLY: Java always copies the value — for objects, the *reference* is copied. You can mutate the object, but reassigning the parameter doesn't affect the caller.
- `main` is `public static void` so JVM can call it without creating an instance.

## Top interview questions
1. **Why is Java platform-independent but JVM platform-dependent?** Bytecode is standard; each OS has its own JVM implementation.
2. **Is Java pass-by-reference?** No. Always pass-by-value (reference values are copied). See code file.
3. **What happens if you override `equals()` but not `hashCode()`?** Equal objects may land in different HashMap buckets → duplicates in Sets, failed lookups.
4. **Why is `String` immutable?** Security (class loading, URLs), thread-safety, string-pool reuse, stable hashCode caching.
5. **`Integer a=127,b=127; a==b?` vs `a=128,b=128`?** true / false (IntegerCache).
6. **Difference between stack and heap?** Stack = per-thread frames & locals (fast, auto-freed); heap = shared objects (GC-managed).
7. **What is JIT?** Just-In-Time compiler converts hot bytecode paths to native code at runtime for speed.
8. **Can `main` be overloaded / final / private?** Overloaded yes (JVM only calls the standard one). Declaring private/non-static compiles but JVM won't launch it.

➡️ Code: [`Basics.java`](./Basics.java)
