# 01 — React Fundamentals 🟢⭐

## Real-life analogy
The Virtual DOM is an **architect's blueprint vs the actual building**: renovating the real building (real DOM) is slow and disruptive, so the architect first edits a cheap paper blueprint (virtual DOM), **compares old vs new blueprint** (diffing), and hands the construction crew the *minimal work order* — "replace window on floor 2" — instead of rebuilding the block (**reconciliation → minimal DOM mutations**). Declarative UI is telling the architect *what the house should look like*, not *which bricks to move* (imperative jQuery).

## What React is
A library (not a framework ⭐) for building UIs from **components** — reusable functions mapping **state + props → UI**. Core idea: `UI = f(state)`. One-way data flow: data down (props), events up (callbacks).

## JSX ⭐
- Syntax sugar for `React.createElement(type, props, children)` → element objects (the virtual DOM nodes). Not HTML: `className`, `htmlFor`, camelCase events (`onClick`), `style={{}}` objects.
- Expressions in `{}`; conditionals via ternary/`&&` (⚠️ `0 && ...` renders `0` ⭐); lists via `map` + **keys**.
- Must return a single root → fragments `<>...</>`.
- Auto-escaping = XSS protection by default (`dangerouslySetInnerHTML` is the escape hatch — name says it).

## Rendering & reconciliation ⭐⭐
1. State changes → React re-runs the component (calls the function) → new virtual tree.
2. **Diffing heuristics**: different element type → tear down subtree; same type → update changed attributes only; lists compared **by key** ⭐.
3. Commit: minimal real-DOM operations.

**Keys** ⭐⭐: stable identity for list items so React can match old↔new. Array **index as key breaks** when the list reorders/inserts — state (input values!) sticks to the wrong rows. Use stable ids.

**Fiber** (name-drop): the internal architecture that makes rendering interruptible/prioritized — enables concurrent features.

**Render ≠ DOM update** ⭐: a "wasted render" is a function re-run whose diff finds nothing — cheap-ish, but the reason memoization exists (topic 07).

## React 18/19 highlights ⭐
- 18: **automatic batching** (multiple setStates in async handlers = one render), `createRoot`, concurrent features (`startTransition`, `useDeferredValue`), Suspense improvements, StrictMode double-invoke in dev (why your effect runs twice ⭐).
- 19: `use()` hook, Actions/`useActionState`, `useOptimistic`, ref as prop (no forwardRef needed), Server Components (know the concept: components that run only on the server, zero JS shipped).

## CSR vs SSR vs SSG (verbal question)
CSR: empty HTML + JS renders (SPA default). SSR: server renders HTML per request + **hydration** ⭐ (attach listeners to server HTML). SSG: HTML at build time. Frameworks: Next.js/Remix. Trade-offs: TTFB/SEO vs server cost.

## Top interview questions
1. **What is the virtual DOM and why is it fast(er)?** (blueprint story; batch + minimal mutations; also honest: it's about *predictability* + good-enough perf)
2. **Explain reconciliation & the diffing heuristics.**
3. **Why do keys matter? Why is index-as-key a bug?** ⭐⭐ (demo in code)
4. **What is JSX compiled to?**
5. **Is a re-render the same as a DOM update?** No — render = function call + diff.
6. **What is hydration?**
7. **Why does my component render twice in dev?** StrictMode — intentional, surfaces impure renders/effects ⭐.
8. **React vs Angular/Vue one-liner?** Library + ecosystem freedom vs batteries-included framework.

➡️ Code: [`fundamentals.jsx`](./fundamentals.jsx)
