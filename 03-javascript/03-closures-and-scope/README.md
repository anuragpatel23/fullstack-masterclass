# 03 — Closures & Scope 🟡⭐⭐ (asked in ~every JS interview)

## Real-life analogy
A closure is a **backpack**: when a function is created inside another function, it packs a backpack with every variable it can see around it (**lexical environment**). Even after the outer function has left the building (returned, its stack frame gone), the inner function still carries the backpack and can open it anytime. Two backpacks from two trips (two calls of the outer function) are **independent** — that's why two counters don't share state.

## Definitions to say verbatim
- **Lexical scope**: what a function can access is decided by **where it is written**, not where/how it is called.
- **Scope chain**: local → enclosing function(s) → module/global; lookup walks outward.
- **Closure** ⭐: a function bundled with (a reference to) its lexical environment — it "remembers" the variables of the scope it was born in, even after that scope has finished executing.

Key subtlety ⭐: closures capture **variables (live references), not values** — if the outer variable changes, every closure over it sees the new value. This single fact explains the loop trap.

## The famous loop trap ⭐⭐
```js
for (var i = 0; i < 3; i++) setTimeout(() => console.log(i));   // 3 3 3
```
All three callbacks close over the SAME function-scoped `i`, which is 3 by the time they run.
**Fixes:** `let i` (new binding per iteration), IIFE `(i => ...)(i)` (copy per iteration), `setTimeout(cb, 0, i)` (pass as arg).

## What closures are FOR (name real uses)
1. **Data privacy / encapsulation** ⭐: counter/wallet with truly private state (pre-`#private` fields).
2. **Module pattern**: return an API object over hidden state.
3. **Function factories**: `multiplyBy(3)`, configured validators.
4. **Once / memoize / debounce / throttle** — all closures (see topic 08).
5. **Callbacks that carry context**: every event handler and React hook depends on closures. React's **stale closure** problem in `useEffect` is literally this topic ⭐ (cross-link to React module).

## Memory note
A closure keeps its captured variables alive (can't be GC'd) → holding big objects in long-lived closures (listeners!) is the JS version of the Java listener leak.

## Top interview questions
1. **What is a closure? Give a practical use.** (definition + counter/privacy)
2. **The `var` loop question — output and 3 fixes.** ⭐⭐
3. **Write `createCounter()` returning increment/decrement/get with private state.** (in code)
4. **Do two calls to the factory share state?** No — new environment per call.
5. **Output-prediction chains** — nested functions modifying outer vars (in code).
6. **Write `once(fn)` — runs only the first time.** (in code)
7. **Closures capture variables or values?** Variables — demonstrate with post-creation mutation.
8. **How can closures leak memory?** Long-lived handlers capturing large scopes; detach/null out.

➡️ Code: [`closures.js`](./closures.js)
