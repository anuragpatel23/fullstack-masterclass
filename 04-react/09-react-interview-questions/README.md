# 09 — React Rapid-Fire Q&A ⭐ (final revision sheet)

30-second answers. Deep dives live in topics 01–08.

## Core
1. **Virtual DOM?** In-memory element tree; diff old vs new → minimal real-DOM ops.
2. **Reconciliation heuristics?** Different type → rebuild subtree; same type → patch props; lists → match by key.
3. **Why keys? Why not index?** Stable identity across renders; index breaks state/DOM matching on reorder/insert.
4. **JSX is…?** Sugar for `createElement` calls returning element objects.
5. **Render vs DOM update?** Function re-run + diff vs actual mutation — renders can be "wasted" but cheap.
6. **Fragment?** Group children without a wrapper node (`<>...</>`).
7. **StrictMode double render?** Dev-only, surfaces impure renders/missing cleanups.
8. **Fiber?** Internal rearchitecture: interruptible, prioritized rendering → concurrent features.
9. **Hydration?** Attaching listeners/state to server-rendered HTML.
10. **CSR vs SSR vs SSG?** Browser-rendered / per-request server HTML / build-time HTML.

## Props & state
11. **Props vs state?** Parent-owned read-only inputs vs component-owned mutable-via-setter memory.
12. **Why never mutate state?** Reference-equality checks — no re-render, breaks memo.
13. **setState twice with `count+1` adds 1 — why?** Closure captured render value; use `setX(x => x+1)`.
14. **Controlled vs uncontrolled input?** State-driven (`value`+`onChange`) vs DOM-held (`defaultValue`+ref).
15. **Child → parent data?** Callback props. **Siblings?** Lift state up.
16. **Changing `key` on a component?** Unmount + fresh mount — the state-reset trick.

## Hooks
17. **Rules of hooks & why?** Top-level, React functions only — state matched by call order.
18. **useEffect deps: `[]` vs none vs `[a]`?** Mount-only / every render / when a changes.
19. **When does cleanup run?** Before each re-run of that effect + at unmount.
20. **Stale closure in setInterval?** Effect captured old state; fix: functional update / deps / ref.
21. **useMemo vs useCallback vs React.memo?** Cache value / cache function / skip component render.
22. **useRef?** Mutable box surviving renders without triggering them; DOM access + instance vars.
23. **useState vs useReducer?** Simple independent values vs multi-field/dependent transitions.
24. **Custom hooks share state?** No — logic reuse, independent state per call.
25. **useEffect vs useLayoutEffect?** After paint (async) vs before paint (sync, measuring).
26. **useTransition?** Mark updates non-urgent — keep input responsive during heavy renders.

## Patterns & architecture
27. **HOC?** Function: component in → enhanced component out (`withAuth`).
28. **Render props?** Prop is a function that returns JSX; component owns logic, caller owns UI.
29. **What replaced both?** Custom hooks.
30. **Context vs Redux?** Transport for low-churn values vs full store (middleware, devtools, selectors).
31. **Why pure reducers/immutability in Redux?** Predictability, time-travel, reference-compare change detection.
32. **Thunk vs saga?** Function actions (simple) vs generator orchestration (complex flows).
33. **React Query vs Redux?** Server cache (staleness, refetch, dedupe) vs client state.
34. **Prop drilling fixes?** Composition (children), Context.
35. **Compound components?** `<Tabs><Tabs.Tab/></Tabs>` sharing implicit context state.

## Performance
36. **Parent renders → children render?** Yes by default; memo/children-as-props/state-colocation stop it.
37. **Memo'd child still re-renders?** Unstable function/object props — pair with useCallback/useMemo.
38. **Huge list?** Virtualize (react-window).
39. **Bundle too big?** Route-level code splitting: lazy + Suspense.
40. **First tool for a perf bug?** DevTools Profiler — measure before memoizing.

## Misc essentials
41. **Error boundaries?** Class components catching render errors of children; not handlers/async.
42. **Portals?** Render children outside the parent DOM (modals) while keeping React tree context.
43. **SyntheticEvent?** Cross-browser event wrapper.
44. **Refs to child components?** ref as prop (19) / forwardRef (18-).
45. **Why can't browsers run JSX?** Needs transpilation (Babel/SWC/TS) to JS.
46. **`0 && <X/>` renders…?** `0`! Use `count > 0 && <X/>`.
47. **Server Components?** Run only server-side, ship zero JS; different from SSR (which hydrates).
48. **React 19 headliners?** `use()`, Actions/`useActionState`, `useOptimistic`, ref-as-prop, compiler (auto-memoization).
49. **Testing React?** React Testing Library — query by role/text (user behavior), `user-event`, MSW for API mocks; snapshot tests sparingly.
50. **Machine-coding favorites?** Todo app, tabs, modal, autocomplete/typeahead (debounce!), star rating, pagination, infinite scroll, countdown timer — practice the first four minimum.

➡️ Code: [`machine-coding.jsx`](./machine-coding.jsx) — autocomplete + modal-with-portal + star rating + pagination, the four most-asked machine-coding widgets.
