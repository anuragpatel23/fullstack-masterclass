# 07 — Performance Optimization 🔴⭐

## Real-life analogy
Re-renders are **fire drills in an office tower**: when the alarm rings on floor 10 (state change), by default **every floor below it evacuates too** (all children re-render) even though nothing changed for them. `React.memo` is a **floor warden who checks "did anything on MY floor change?"** before evacuating (props compare). But the warden compares **by reference** — hand him a freshly photocopied identical memo (`new object/function each render`) and he evacuates anyway ⭐ — that's why `useMemo`/`useCallback` exist: **hand the same physical memo each time**.

## Rule 0 ⭐: parent re-renders → ALL children re-render by default (props unchanged or not). Most "optimization" is stopping pointless cascades — after measuring.

## The memo trio ⭐⭐
- **`React.memo(Component)`** — skip re-render if props are shallow-equal.
- **`useCallback(fn, deps)`** — stable function reference (or memo'd children see a "new" prop every time ⭐).
- **`useMemo(() => value, deps)`** — stable object/array references + skip expensive computes.
- They work **as a team**: memo without stable references does nothing (the demo in code proves it).
- Don't blanket-apply — each adds comparison/bookkeeping cost. **Measure with React DevTools Profiler first** (senior answer ⭐).

## Free wins before memoization (say these!)
1. **Push state down** — put the search text in the component that uses it, not the page root.
2. **children as props** ⭐: `<Wrapper>{expensive}</Wrapper>` — children created by the *parent's* render don't re-create when only Wrapper's own state changes (in code — interviewer favorite).
3. Stable keys, no inline heavy work in render.

## Code splitting & lazy loading ⭐
`const Admin = React.lazy(() => import('./Admin'))` + `<Suspense fallback>` — route-level splitting cuts initial bundle (dynamic `import()` from the ES6 topic!). Also: image lazy-loading, prefetch on hover.

## Long lists → virtualization ⭐
10k rows = 10k DOM nodes = death. Render only the visible window (`react-window` / `@tanstack/react-virtual`). The "how do you render a huge table?" answer.

## Concurrent features (18+) ⭐
- `useTransition` / `startTransition` — mark expensive updates non-urgent; typing stays snappy while results render at leisure.
- `useDeferredValue` — deferred copy of a churning value.
- Distinguish from debounce: debounce delays *the work*; transitions deprioritize *the render*.

## Expensive renders themselves
Profile → memoize computations, move work out of render, web workers for CPU-heavy transforms, uncontrolled inputs for huge forms.

## Bundle & network
Tree-shaking (named exports), analyze bundle, compress images, CDN + caching headers, `React.PureComponent` (legacy class = memo).

## Top interview questions
1. **React.memo vs useMemo vs useCallback?** ⭐⭐ (component/value/function + they team up)
2. **Memo'd child still re-renders — why?** New function/object/JSX-children reference each render ⭐⭐ (code demo).
3. **How would you optimize a slow list page?** Ladder: profile → push state down → memo team → virtualization → transitions ⭐.
4. **What is code splitting; how in React?** lazy + Suspense + route-level.
5. **useTransition vs debouncing?**
6. **Does parent re-render always re-render children? How to stop it?** Yes by default; memo / children-as-props / state colocation.
7. **How do you FIND the slow part?** DevTools Profiler flamegraph, "why did this render", React 19 compiler note (auto-memoization — worth name-dropping ⭐).
8. **Key-related perf bugs?** index keys on reorder → DOM thrash + state bleed.

➡️ Code: [`performance.jsx`](./performance.jsx)
