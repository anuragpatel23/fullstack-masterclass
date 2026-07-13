# 05 — Exception Handling 🟢⭐

## Real-life analogy
An **airport security chain**: a problem at the gate (exception thrown) is escalated up the chain of command (call stack) until someone with authority handles it. **Checked exceptions** are like visa requirements — you must prove *at planning time* (compile time) that you've handled them. **Unchecked exceptions** are unexpected incidents (a fire alarm — programming bugs); nobody plans for them at every step. `finally` is the cleanup crew that shows up whether or not the flight took off.

## Hierarchy ⭐
```
Throwable
├── Error                 (JVM-level: OutOfMemoryError, StackOverflowError — don't catch)
└── Exception
    ├── RuntimeException  (UNCHECKED: NPE, IllegalArgument, IndexOutOfBounds, ClassCast, Arithmetic)
    └── everything else   (CHECKED: IOException, SQLException, InterruptedException)
```

- **Checked** = recoverable external failures → compiler forces `throws`/catch.
- **Unchecked** = programming bugs → fix the code, don't catch everywhere.

## Must-know rules ⭐
- Catch order: **child before parent**, else compile error ("already caught").
- Multi-catch: `catch (IOException | SQLException e)` — types must not be in the same hierarchy; `e` is implicitly final.
- **finally always runs** — except `System.exit()`, JVM crash, or infinite loop in try. If both try and finally return, **finally's return wins** (and swallows exceptions — never return from finally!).
- **try-with-resources** (Java 7+): any `AutoCloseable`; resources closed in *reverse* order; original exception preserved, close-failures attached as **suppressed exceptions** (`getSuppressed()`).
- Overriding: a subclass method can throw *narrower or fewer* checked exceptions, never broader.
- `throw` (throws an instance) vs `throws` (declares). `final` vs `finally` vs `finalize` (deprecated GC hook).

## Custom exceptions — best practice
- Extend `RuntimeException` for business rule violations in modern services (Spring rolls back on unchecked by default — links to `@Transactional`!).
- Always keep the **cause**: `super(message, cause)` — never swallow stack traces.
- Add context fields (orderId, errorCode) for structured API error responses.

## Anti-patterns interviewers probe
- `catch (Exception e) {}` — swallowing. At minimum log; ideally translate & rethrow.
- Catch-and-rethrow with `new RuntimeException(e.getMessage())` — loses the stack trace; pass `e` as cause.
- Using exceptions for flow control (they're ~1000× costlier than an `if`; stack trace capture is expensive).
- Logging AND rethrowing everywhere → duplicate logs.

## Top interview questions
1. **Checked vs unchecked — and when do you create each?** (See above; checked = caller can recover; unchecked = bug or unrecoverable.)
2. **Can `finally` be skipped?** `System.exit`, JVM kill, daemon thread death.
3. **try-return vs finally-return?** finally wins; exception in try is silently discarded — see code.
4. **What are suppressed exceptions?** Close-time failures attached to the primary exception in try-with-resources.
5. **Exception in catch AND finally?** finally's exception propagates; catch's is lost (unless suppressed manually).
6. **Why did try-with-resources improve on finally-close?** Old idiom lost the original exception when `close()` also threw; TWR keeps original + suppresses close failure.
7. **How does Spring's `@Transactional` interact with exceptions?** Default rollback on RuntimeException/Error only, NOT on checked → classic production bug (covered again in Spring module).
8. **StackOverflowError vs OutOfMemoryError?** Deep/infinite recursion (stack) vs heap exhaustion — both Errors, not Exceptions.

➡️ Code: [`ExceptionsDemo.java`](./ExceptionsDemo.java)
