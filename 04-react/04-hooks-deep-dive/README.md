# 04 — Hooks Deep-Dive 🔴⭐⭐ (the React interview core)

## Real-life analogy
Hooks are **numbered lockers at a swimming pool**: React hands lockers out **in call order** — first useState gets locker 1, second gets locker 2. That's why hooks **can't be inside ifs/loops** ⭐: skip a locker one visit and everyone's stuff shifts into the wrong lockers. A **stale closure** is a **printed photo of the scoreboard**: your effect/handler captured the score *at the moment it was created*; if it never re-subscribes (deps), it keeps showing the old photo while the real game moved on.

## The rules of hooks ⭐ (and WHY)
1. Top level only — no conditions/loops/nested functions (the locker/order mechanism: state is matched by call index).
2. Only from React functions (components/custom hooks).

## useState
- Lazy init: `useState(() => expensive())` runs once ⭐.
- Functional updates `setX(x => ...)` (batching-safe).
- Same value (`Object.is`) → render bail-out.

## useEffect ⭐⭐ (where candidates die)
- Deps array: `[]` mount-only / `[a,b]` when those change / **missing = every render** ⭐.
- **Never lie about deps** — include everything you read. Lint rule exists for a reason.
- **Stale closure** ⭐⭐: `setInterval(() => setCount(count+1), 1000)` with `[]` → count stuck at 1. Fixes: functional update, correct deps, or ref (all in code).
- Cleanup before each re-run + unmount; **fetch race-condition** guard (`cancelled` flag / AbortController) ⭐.
- Don't useEffect for: derived values (compute in render), event responses (handlers), state syncing chains.

## useContext
Subscribe to nearest Provider; re-renders on value change — **new object literal in Provider value = re-render every consumer** ⭐ (memoize the value; split contexts).

## useReducer
`const [state, dispatch] = useReducer(reducer, init)` — multi-field state, dependent transitions, testable pure reducer; dispatch identity is stable (nice for deps) ⭐. It's mini-Redux inside a component.

## useMemo / useCallback ⭐
- `useMemo(() => compute(a), [a])` — cache a **value**; `useCallback(fn, deps)` = `useMemo(() => fn, deps)` — cache the **function**.
- Purpose #1: stable references so `React.memo` children / effect deps don't churn ⭐. Purpose #2: skip expensive computation.
- Don't sprinkle everywhere — each has bookkeeping cost; measure first (senior answer).

## useRef ⭐
Mutable `{current}` that survives renders and **never triggers re-render**. Uses: DOM access (focus/scroll/measure), instance variables (timer ids, previous values, latest-value escape hatch for stale closures). `ref.current` change ≠ render ⭐.

## Custom hooks ⭐⭐
`useXxx` functions composing other hooks — the code-reuse story that replaced HOCs/mixins. **Each call = independent state** (locker sets per user!). Must-writes: `useDebounce`, `useFetch`, `useLocalStorage`, `usePrevious`, `useToggle` (all in code).

## Also name-drop
`useLayoutEffect` (pre-paint), `useId` (SSR-safe ids), `useSyncExternalStore` (external stores — how Redux subscribes), `useTransition`/`useDeferredValue` (concurrent, topic 07), React 19: `use()`, `useOptimistic`, `useActionState`.

## Top interview questions
1. **Why can't hooks be conditional?** Call-order/locker mechanism ⭐.
2. **The setInterval stale-closure bug — explain and fix 3 ways.** ⭐⭐ (code)
3. **useMemo vs useCallback vs React.memo?** value / function / component ⭐.
4. **useState vs useReducer — when switch?**
5. **useRef vs useState?** No-render mutable box vs render-driving state.
6. **How do custom hooks share logic — do two components share state?** No — logic shared, state independent ⭐.
7. **Fetch in useEffect: handle unmount + fast re-fires.** cancelled flag/AbortController (code).
8. **What is `usePrevious` and how does it work?** ref updated in effect after render (code).

➡️ Code: [`hooks.jsx`](./hooks.jsx)
