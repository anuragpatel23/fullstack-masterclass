# 05 — HOC, Render Props & Composition 🔴⭐

## Real-life analogy
An **HOC is a phone case factory**: give it any phone (component), it returns the same phone wrapped with extra protection/features (logging, auth) — the phone doesn't know it's wrapped. **Render props** is hiring a **driver who handles the car but asks YOU how to use the trip**: the component manages the hard part (state/tracking) and calls *your function* to decide what to show. **Composition with `children`** is a **picture frame**: the frame (Card/Layout) doesn't care what photo you slot in.

## Higher-Order Components ⭐
`const Enhanced = withX(Component)` — a **function taking a component, returning a new component** with injected props/behavior. (Direct application of JS higher-order functions — closure topic!)
- Classics: `withRouter`, `connect(mapState)(Comp)` (Redux), `withAuth`, `withLogging`.
- Conventions: pass through unrelated props (`{...props}` ⭐), set `displayName` for DevTools, **don't apply inside render** (new type every render → remount ⭐), static methods don't copy over.
- Pitfalls: wrapper hell, prop-name collisions, indirection.

## Render Props ⭐
A prop whose value is a **function that returns JSX**; the component owns the logic and calls your function with data: `<MouseTracker render={pos => <Cursor {...pos} />}` (or use `children` as the function). More explicit than HOC (no hidden prop injection), but nests deeply.

## Custom hooks replaced both (say this!) ⭐⭐
`useMouse()`, `useAuth()`, `useFetch()` deliver the same reuse with no wrapper components, no prop collisions, no nesting. HOC/render-props remain: in legacy code, when you must wrap *rendering* (error boundaries, providers, Suspense), and in interviews. Timeline: mixins → HOCs → render props → **hooks**.

## Composition patterns ⭐
- **children as slots**: `<Card>{anything}</Card>`; multiple slots via props: `<Layout sidebar={<Nav/>} content={<Feed/>}>`.
- **Composition over prop drilling** ⭐: instead of forwarding `user` down 4 layers, the top component composes `<Layout><Profile user={user}/></Layout>` — layers in between never see it.
- **Compound components** ⭐ (senior pattern): `<Tabs><Tabs.List/><Tabs.Panel/></Tabs>` sharing implicit state via context (in code).
- "Composition over inheritance" — React's official stance: never `extends` another component.

## Top interview questions
1. **What is an HOC? Write `withLoading`/`withAuth`.** ⭐ (in code)
2. **HOC vs render props vs hooks — compare & when each.** ⭐⭐ (history + trade-offs above)
3. **Why not create an HOC-wrapped component inside render?** New component identity each render → unmount/remount, state loss ⭐.
4. **What problems did hooks solve that HOCs had?** Wrapper hell, prop collisions, static typing pain, hidden data source.
5. **How does React favor composition over inheritance?** children/slots; no class extension.
6. **Build compound components (Tabs).** (in code — context-based)
7. **How do you avoid prop drilling without Context?** Component composition — pass the composed JSX, not the data.

➡️ Code: [`patterns.jsx`](./patterns.jsx)
