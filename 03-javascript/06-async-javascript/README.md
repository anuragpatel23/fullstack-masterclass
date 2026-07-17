# 06 — Async JavaScript & the Event Loop 🔴⭐⭐ (the JS interview boss fight)

## Real-life analogy
A **single chef restaurant** (JS is single-threaded): the chef (call stack) cooks one dish at a time. Slow jobs — oven baking, deliveries (network/timers) — are handed to kitchen appliances/delivery boys (**Web APIs / libuv**), and the chef keeps cooking. When the oven dings, the finished job's follow-up goes into a queue. The chef checks queues **only between dishes** (when the stack is empty). Crucially there are TWO queues: **VIP orders (microtasks — promise callbacks)** are ALL served before any regular order (**macrotasks — setTimeout, I/O**) gets a turn.

## The event loop ⭐⭐ (recite this)
1. Run all synchronous code (call stack).
2. Stack empty → drain the **microtask queue completely** (`.then/.catch/.finally`, `await` continuations, `queueMicrotask`).
3. Take ONE **macrotask** (setTimeout/setInterval callback, I/O) → run it → drain microtasks again → repeat.

**The output question everyone gets** ⭐⭐: sync → all microtasks → macrotasks:
```js
console.log('1');  setTimeout(() => console.log('2'));
Promise.resolve().then(() => console.log('3'));  console.log('4');
// 1 4 3 2
```
`setTimeout(fn, 0)` = "at least 0ms, after sync + microtasks" — never immediate ⭐.

## Promises ⭐
States: pending → fulfilled | rejected (**settled = immutable**).
- `.then` returns a NEW promise → chaining; return value wraps, thrown error rejects, returned promise is adopted (flattening).
- `.catch` catches everything above it; `.finally` for cleanup.
- Static combinators ⭐: **`Promise.all`** (all-or-first-rejection ⭐), **`allSettled`** (never rejects — status report), **`race`** (first settle — timeouts!), **`any`** (first fulfilment).
- Anti-patterns: nested `.then` pyramids; forgetting to `return` inside then; the constructor anti-pattern (`new Promise` around an existing promise).

## async/await ⭐
- `async fn` ALWAYS returns a promise; `await` pauses the *function* (not the thread), the rest runs as a microtask continuation.
- try/catch works naturally; always `catch` or the rejection is unhandled.
- **The sequential-await trap** ⭐⭐: `await a(); await b();` = serial. Independent work: `const [x, y] = await Promise.all([a(), b()])`.
- `await` in loops: `for...of` + await = sequential (sometimes desired); `map` + `Promise.all` = parallel.
- Top-level await (ES2022, modules).

## Callbacks → promises → async/await (the history question)
Callback hell (pyramid + inversion of control) → promises (composition + trust) → async/await (sync-looking flow). Convert callback APIs: promisification (in code).

## Node vs browser nuance (senior flavor)
Same model; Node adds phases (timers → poll → check/`setImmediate`) and `process.nextTick` (runs before other microtasks).

## Top interview questions
1. **Explain the event loop.** ⭐⭐ (queues + microtask priority — use the chef story)
2. **Predict output** — the mixed setTimeout/promise/async question (several in code — practice!).
3. **`Promise.all` vs `allSettled` vs `race` vs `any`?** + when each (all: fail-fast batch; allSettled: independent jobs; race: timeout).
4. **Is JS multithreaded during await?** No — single thread; waiting happens outside the thread (Web APIs); Web Workers for real parallelism.
5. **Implement `Promise.all` yourself.** ⭐ (in code)
6. **Sequential vs parallel await — fix slow code.** (in code)
7. **How do you timeout a fetch?** `Promise.race` with a reject-timer or `AbortController` ⭐.
8. **Write `promisify(fn)`.** (in code)
9. **Retry with exponential backoff.** (in code — pairs with the Spring Retry story!)

➡️ Code: [`async-eventloop.js`](./async-eventloop.js)
