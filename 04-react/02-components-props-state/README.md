# 02 — Components, Props & State 🟢⭐

## Real-life analogy
A component is a **coffee machine**: **props are the order the customer hands in** (size, milk, sugar — the machine can't change the order slip: **props are read-only** ⭐), **state is what the machine tracks internally** (water level, beans remaining — it manages and changes these itself). "**Lifting state up**" is two baristas needing the same order queue — you put the queue board where **both can see it: their common manager** (closest common ancestor).

## Props vs State ⭐⭐ (the table you must know cold)
| | Props | State |
|---|---|---|
| Owner | parent passes down | component itself |
| Mutable? | **read-only** (never mutate!) | via setter only (`setX`) |
| Triggers re-render? | when parent passes new ones | when set to a *different* value |
| Analogy | function arguments | function's local memory |

## State rules ⭐⭐
1. **Never mutate**: `user.name = 'x'; setUser(user)` won't re-render (same reference — `Object.is` check). Always create new: `setUser({...user, name: 'x'})` — ties directly to the ES6 spread/shallow-copy lesson.
2. **Updates are batched & asynchronous-ish** ⭐: `setCount(count + 1)` three times = +1 (stale closure value). **Functional updater**: `setCount(c => c + 1)` three times = +3 (see code).
3. State is **per component instance**, preserved across re-renders, tied to position in the tree (why keys reset state ⭐).
4. Don't mirror props into state (stale copy anti-pattern); derive during render instead.

## Component patterns
- **Functional components** (the standard). Class components: legacy — recognize `this.state`, `this.setState`, lifecycle methods (topic 03).
- **Controlled vs uncontrolled** ⭐⭐: input value driven by React state (`value` + `onChange` — single source of truth, validation on every keystroke) vs DOM keeps it (`defaultValue` + ref — less code, fine for simple forms). Guaranteed question.
- **Lifting state up** ⭐: shared state lives in the closest common ancestor; siblings communicate **only through the parent** (data down / events up).
- **Prop drilling** ⭐: passing props through many uninvolved layers — smells; fixes: composition (pass components as children), Context (topic 06).
- `children` prop — composition slots; presentational vs container split (conceptually alive even post-hooks).

## Events
SyntheticEvent (cross-browser wrapper), camelCase handlers, pass function not call (`onClick={fn}` not `fn()` ⭐), event pooling is gone (17+).

## Top interview questions
1. **Props vs state?** (table above)
2. **Why is directly mutating state a bug?** Reference equality check — no re-render; also breaks memo/PureComponent.
3. **`setCount(count+1)` twice adds 1 — why? Fix?** Closure captures render-time value; functional updater ⭐⭐.
4. **Controlled vs uncontrolled components? When each?** ⭐
5. **How do sibling components share data?** Lift state up.
6. **How do you pass data from child → parent?** Callback prop.
7. **Why does changing a component's `key` reset its state?** Identity change → unmount + remount ⭐ (also the trick to force-reset a form).
8. **Can a child modify the props object?** Never — one-way data flow keeps UIs predictable.

➡️ Code: [`components-props-state.jsx`](./components-props-state.jsx)
