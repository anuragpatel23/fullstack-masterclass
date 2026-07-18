# 06 — State Management 🔴⭐

## Real-life analogy
Component state is **cash in your wallet** (private, fast). Lifting state up is **the family's shared drawer at home**. Context is the **noticeboard in the building lobby** — everyone in the building can read it, but every time ANY notice changes, everyone who looks at the board gets pinged ⭐. Redux is the **bank**: one vault (single store), you can't reach into the vault — you submit **slips (actions)** processed by strict **tellers (pure reducers)**, and every transaction appears on a statement (**DevTools time travel**). React Query is a **newspaper subscription for the bank's data** — it fetches, caches, and refreshes *someone else's* information (server state) on a schedule you configure.

## The decision ladder ⭐ (say this framework)
1. Local `useState` — default; keep state as close to usage as possible.
2. Lift up — shared by siblings.
3. **Context** — low-frequency global-ish data: theme, auth user, locale.
4. **Server state → React Query/SWR** ⭐ — API data is a *cache*, not app state.
5. **Client global state → Redux Toolkit / Zustand** — genuinely global, frequently-updated client data (cart, complex filters, multi-step wizards).

## Context ⭐ — what it is and isn't
Solves prop drilling, is NOT a state manager (no store, no middleware, no selectors). **Every consumer re-renders when `value` changes** — and an inline `value={{user, setUser}}` object is new each render ⭐. Mitigations: memoize value, split contexts (state vs dispatch), keep high-churn data out.

## Redux (classic questions still asked) ⭐
- Principles: **single source of truth**, state is **read-only** (dispatch actions), changes via **pure reducers** ⭐.
- Flow: `UI → dispatch(action) → middleware → reducer(state, action) → new state → subscribed components re-render (via selectors)`.
- **Redux Toolkit is the standard** ⭐: `configureStore`, `createSlice` (actions+reducer together), Immer inside (write "mutating" code, get immutable updates ⭐), `createAsyncThunk` (pending/fulfilled/rejected), RTK Query (built-in server-state).
- Middleware: where side effects live — **thunk** (functions as actions — simple, 90% of needs) vs **saga** (generators, complex orchestration/cancellation) ⭐ classic compare.
- `useSelector` (subscribe to a slice — keep selectors narrow ⭐, or reference-equality re-renders bite) + `useDispatch`.

## Zustand (the modern lightweight answer)
`create()` a store hook; components subscribe to slices via selector — no providers, ~1KB, less boilerplate. Naming it (+ Jotai/Recoil as atom-based) = current-ecosystem awareness.

## React Query / TanStack Query ⭐ (server state)
`useQuery({queryKey, queryFn})` — caching, deduping, background refetch, stale-while-revalidate; `useMutation` + invalidation; optimistic updates. Kills the hand-rolled `loading/error/data` useEffect boilerplate. **Server state ≠ client state** is the senior insight.

## Top interview questions
1. **Context vs Redux?** ⭐⭐ (transport vs store+middleware+devtools+selectors; re-render behavior)
2. **Redux data flow?** (the one-way loop, aloud)
3. **Why must reducers be pure? Why immutable updates?** Predictability, time travel, cheap change detection (reference compare) ⭐.
4. **Thunk vs saga?**
5. **What did Redux Toolkit fix?** Boilerplate, accidental mutation (Immer), store setup, async patterns.
6. **When is Context a performance problem & fixes?** ⭐ (memoized value, split contexts)
7. **Why React Query if I have Redux?** Server cache concerns (refetch, staleness, dedupe) aren't reducer problems.
8. **Where would you put: input text / auth user / product list / cart?** local / context / React Query / RTK-Zustand — the ladder ⭐.

➡️ Code: [`state-management.jsx`](./state-management.jsx)
