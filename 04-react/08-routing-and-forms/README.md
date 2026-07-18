# 08 — Routing & Forms 🟡

## Real-life analogy
Client-side routing is a **stage play vs filming on location**: a traditional website travels to a new location for every scene (full page load from the server); an SPA is one stage (single HTML page) where **stagehands swap the sets** (React swaps components) while the marquee outside updates the scene name (**History API changes the URL without a reload**). A **protected route** is the backstage door with a guard: no pass (auth) → redirected to the box office (login), and the guard remembers where you were headed (redirect state).

## React Router v6+ ⭐
- `<BrowserRouter>` (History API) vs `<HashRouter>` (`#/path` — static hosting fallback).
- `<Routes><Route path="/orders/:id" element={<Order/>}/></Routes>` — best-match ranking (no more `exact`), `path="*"` for 404.
- **Hooks**: `useParams()` (`:id` ⭐), `useNavigate()` (imperative nav; `navigate(-1)`), `useSearchParams()` (query string), `useLocation()`.
- `<Link>`/`<NavLink>` (active styling) — never `<a href>` internally (full reload ⭐).
- **Nested routes + `<Outlet/>`** ⭐: layout routes render children in the outlet; `index` route for defaults.
- **Protected routes** ⭐: wrapper checks auth → `<Navigate to="/login" state={{from}}/>` → after login, return to `from` (in code).
- Lazy route modules (ties to code splitting). Data APIs (loaders/actions) exist in 6.4+/v7 — name-drop.
- **Why does refreshing a deep URL 404 on production?** ⭐ Server must rewrite all paths to `index.html` (classic deploy question).

## Forms ⭐
- **Controlled forms** (topic 02 recap): value from state, validation on change/blur/submit; single handler for many fields via `name` + computed keys (in code).
- Validation layers: HTML attrs (`required`, `pattern`) → JS rules → **always re-validate server-side** ⭐ (bridges to Spring `@Valid`!).
- Submission: `e.preventDefault()` ⭐, disable during submit, optimistic vs pessimistic updates, error mapping (field vs form-level).
- Big/complex forms: per-keystroke re-renders of a controlled mega-form get slow → **React Hook Form** (uncontrolled + refs — minimal re-renders ⭐) + schema validation (Zod/Yup). Know why RHF is fast.
- File inputs are always uncontrolled; multi-step wizards = state machine/reducer.

## Top interview questions
1. **How does client-side routing work without page reloads?** History API `pushState` + popstate listening; router maps URL → component ⭐.
2. **Implement a protected route.** (in code)
3. **useParams vs useSearchParams vs useLocation?**
4. **Nested routes and `<Outlet/>`?** (layout pattern — in code)
5. **Why does deep-linking break on my deployed SPA?** Server rewrite to index.html ⭐.
6. **Controlled vs uncontrolled forms; why is React Hook Form fast?** ⭐
7. **How do you validate?** Layered client + mandatory server-side.
8. **Link vs anchor tag?** SPA navigation vs full reload.

➡️ Code: [`routing-forms.jsx`](./routing-forms.jsx)
