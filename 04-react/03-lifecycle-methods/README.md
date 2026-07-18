# 03 — Lifecycle: Class Methods → Hook Equivalents 🟡⭐

## Real-life analogy
A component's life is a **hotel stay**: check-in and room setup (**mounting** — constructor/render/componentDidMount), housekeeping visits whenever your needs change (**updating** — re-render/componentDidUpdate), and checkout where you must return the key and settle bills (**unmounting** — componentWillUnmount = **cleanup**, or the hotel leaks rooms). Hooks replaced the fixed hotel schedule with **subscriptions per concern**: instead of "do everything at 10 AM daily" (one big componentDidUpdate), each concern (data, timer, listener) declares its own schedule (its own `useEffect` with deps).

## Class lifecycle (recognize + order) ⭐
**Mount**: `constructor` → `getDerivedStateFromProps` → `render` → *(DOM committed)* → `componentDidMount` (fetch/subscribe here).
**Update** (props/state change): `getDerivedStateFromProps` → `shouldComponentUpdate` (perf gate ⭐) → `render` → `getSnapshotBeforeUpdate` → `componentDidUpdate(prevProps, prevState)` (compare before re-fetching! ⭐).
**Unmount**: `componentWillUnmount` (clear timers/listeners/subscriptions).
**Errors**: `static getDerivedStateFromError` + `componentDidCatch` → **error boundaries are still class-only** ⭐ (or use react-error-boundary lib).
Deprecated trio (recognize): `componentWillMount/WillReceiveProps/WillUpdate`.

## The mapping table ⭐⭐ (the actual interview question)
| Class | Hook equivalent |
|---|---|
| `componentDidMount` | `useEffect(fn, [])` |
| `componentDidUpdate` | `useEffect(fn, [deps])` (runs on mount too — guard with a ref if needed ⭐) |
| `componentWillUnmount` | `useEffect(() => { return cleanup }, [])` |
| `shouldComponentUpdate` | `React.memo` (+ useMemo/useCallback) |
| `constructor` state init | `useState(init)` / `useState(() => expensive())` lazy ⭐ |
| `getSnapshotBeforeUpdate` | `useLayoutEffect` (sync, before paint) |
| instance fields | `useRef` |

## Effect timing nuances ⭐
- `useEffect` runs **after paint** (async); `useLayoutEffect` runs **before paint** (sync — measure DOM, avoid flicker; blocks rendering ⭐).
- **Cleanup runs before every re-run** of that effect, not only at unmount ⭐⭐ — the detail that separates candidates. Sequence: render → cleanup(old deps) → effect(new deps).
- StrictMode dev: mount → cleanup → mount again — exposes missing cleanups.

## Mental-model shift (say this)
Don't think "lifecycle events"; think **synchronization**: `useEffect` synchronizes an external system (server, timer, DOM, subscription) with the component's current state. Deps = "when is this sync stale?" — never lie about deps to "control timing."

## Top interview questions
1. **Lifecycle order on mount and update?** (class list above)
2. **Hook equivalents of didMount/didUpdate/willUnmount?** ⭐⭐ (+ the "runs on mount too" caveat)
3. **When does effect cleanup run?** Before next effect run AND at unmount ⭐.
4. **useEffect vs useLayoutEffect?** Post-paint async vs pre-paint sync; measuring/flicker.
5. **Why did my effect run twice on mount?** StrictMode (dev) — fix the effect, not StrictMode.
6. **Where do you fetch data in class / function components?** didMount / useEffect (mention race-condition cleanup, topic 04).
7. **What are error boundaries and their limits?** Catch render errors of children; NOT event handlers, async code, SSR, or errors in itself ⭐.
8. **What was wrong with componentWillReceiveProps?** Ran before render with unsafe assumptions → deprecated for derived-state patterns.

➡️ Code: [`lifecycle.jsx`](./lifecycle.jsx)
