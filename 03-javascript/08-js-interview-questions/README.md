# 08 — JavaScript Interview Problem Bank ⭐ (write these blind)

The utility functions and output-prediction questions that appear in nearly every frontend/fullstack screen. Everything is implemented in the code file with commentary.

## Tier 1 — the utilities (memorize the shapes)
1. **Debounce** ⭐⭐ — "fire after the user STOPS" (search box). Closure + clearTimeout.
2. **Throttle** ⭐⭐ — "fire at most once per interval" (scroll/resize).
   *Debounce = wait for silence; throttle = steady drumbeat.* Know the difference cold.
3. **Once** — run only the first time.
4. **Memoize** — cache by arguments.
5. **Curry** — `sum(1)(2)(3)`, generic curry.
6. **Deep clone** (recursive) and **deep equal**.
7. **Flatten** array / **flatten nested object** to dot-paths.
8. **promisify**, **my Promise.all**, **retry with backoff** (topic 06).
9. **Polyfills**: `map`, `filter`, `bind` (topics 02/07), `call`/`apply` sketch.
10. **Event emitter** ⭐ — on/off/emit/once (the observer pattern in JS).
11. **pipe / compose** — function composition.
12. **Chunk, groupBy, unique, intersection** (topic 07).

## Tier 2 — output prediction (the trap gauntlet)
All in the code file with explanations:
- Hoisting + TDZ combos; the `var` loop; closure counters.
- `this` in method / detached / arrow / bind chains.
- Event-loop ordering: sync vs microtask vs macrotask (+ async/await mixed).
- Coercion: `[] == false`, `'5' + 3 - 1`, `0.1 + 0.2`.
- Reference vs value: mutated objects across functions, frozen objects.
- Prototype lookups & shadowing.

## Tier 3 — DOM/browser concepts (asked verbally)
- **Event delegation** ⭐: one listener on the parent; `e.target` + `closest()` — works for dynamically added children, saves memory (code sketch included).
- **Bubbling vs capturing**; `stopPropagation` vs `preventDefault` ⭐.
- `localStorage` vs `sessionStorage` vs cookies (size, expiry, sent-to-server ⭐).
- `defer` vs `async` script loading; DOMContentLoaded vs load.
- Critical rendering path basics; reflow vs repaint.
- CORS from the browser side; XSS vs CSRF one-liners (links to Spring Security topic).
- `==` SPA questions: history API, why React needs a router.

## How to practice
For each utility: read the solution once → close the file → write it from a blank editor → run and compare. Interviewers watch *how* you build (start with the closure skeleton, narrate the edge cases: leading/trailing calls, `this` forwarding, argument passing).

➡️ Code: [`interview-problems.js`](./interview-problems.js)
