# 03 — Strings & Immutability 🟢⭐

## Real-life analogy
A String is like a **printed book**: you cannot edit a printed page — to "change" it you print a new book. A `StringBuilder` is the **manuscript in a word processor** — edit in place, then print (call `toString()`) when done. The **string pool** is a library: if two people ask for the same title, they get the same shared copy instead of printing twice.

## Key concepts

### Why is String immutable? ⭐
1. **String pool** — sharing is only safe if nobody can mutate the shared object.
2. **Security** — class names, file paths, DB URLs passed as Strings can't be changed after validation.
3. **Thread-safety** — immutable ⇒ freely shareable across threads.
4. **hashCode caching** — computed once, reused (fast HashMap keys).

### Pool mechanics
- Literals (`"abc"`) go to the pool (interned automatically).
- `new String("abc")` creates an extra heap object; `.intern()` returns the pooled one.
- Compile-time constant concatenation (`"a" + "b"`) is folded into `"ab"` (pooled). Runtime concatenation is not.

### String vs StringBuilder vs StringBuffer ⭐
| | String | StringBuilder | StringBuffer |
|---|---|---|---|
| Mutable | ✘ | ✔ | ✔ |
| Thread-safe | ✔ (immutable) | ✘ | ✔ (synchronized) |
| Speed | — | fastest | slower |
| Use | constants, keys | loops, building | legacy/multi-threaded builds |

⚠️ `+` in a loop creates a new builder + string **per iteration** → O(n²). Use one StringBuilder.
(Since Java 9 strings are backed by `byte[]` with compact-strings — Latin-1 uses 1 byte/char.)

### How to write an immutable class ⭐ (asked constantly)
1. `final` class (no subclass can add mutability).
2. All fields `private final`.
3. No setters.
4. **Defensive copies** of mutable fields in constructor AND getters (e.g., `Date`, `List`).
5. Or just use a `record` (Java 16+) — but note records do NOT deep-copy for you.

## Top interview questions
1. **How many objects does `String s = new String("hi")` create?** Up to 2 — `"hi"` in the pool (if absent) + one heap object.
2. **Why are Strings good HashMap keys?** Immutable (hash never changes) + cached hashCode.
3. **`s1 == s2` vs `s1.equals(s2)`?** Reference vs content. Literals may share pool refs.
4. **What does `intern()` do?** Returns the canonical pooled instance, adding it if absent.
5. **Reverse a string / check palindrome / first non-repeating char** — see code file.
6. **Is `String` concatenation in a single expression slow?** No — compiler uses `StringConcatFactory` (indified concat) / a single builder. Loops are the problem.
7. **Can we make an immutable class containing a `List`?** Yes — defensive copy in and `List.copyOf` out.

➡️ Code: [`StringsDemo.java`](./StringsDemo.java)
