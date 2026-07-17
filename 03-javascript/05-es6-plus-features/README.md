# 05 — ES6+ Features 🟡⭐ (the modern-JS toolkit React assumes)

## Real-life analogy
ES6 was **moving from hand-tools to power-tools**: the furniture (language semantics) is largely the same, but destructuring is a **multi-head screwdriver** (grab exactly the screws you need from the box in one motion), spread is a **photocopier for boxes** (shallow copy!), and modules replaced "everyone dumps tools in one shared garage" (globals) with **labeled toolboxes with packing lists** (explicit imports/exports).

## Destructuring ⭐⭐ (React props live on this)
```js
const { name, age = 18, address: { city } = {} } = user;   // rename, default, nested
const [first, , third, ...rest] = arr;                     // skip, rest
function greet({ name, role = 'dev' }) {}                  // param destructuring = React props ⭐
const [a, b] = [b, a];                                     // swap
```

## Spread & rest ⭐⭐
- Spread `...` expands: `[...arr1, ...arr2]`, `{ ...obj, status: 'new' }` (merge — later keys win ⭐), `fn(...args)`.
- Rest `...` collects: `(...args)`, `const { id, ...others } = obj` (omit a key idiom).
- ⚠️ **SHALLOW copy** ⭐: nested objects are shared references — the #1 immutability bug in React state updates.

## Template literals
`` `Hello ${name}` `` — interpolation, multi-line; tagged templates (recognize: styled-components).

## Modules (ESM) ⭐
`export const x` / `export default` / `import { x } from './m.js'` / `import * as m`.
- Named vs default: named = strict name match (typo-safe, tree-shakable ⭐).
- ESM vs CommonJS (`require`): static & async-loaded & live bindings vs dynamic & sync & value copies.
- `import()` dynamic — code splitting (ties to React.lazy ⭐).

## New collections ⭐
- **Map** vs plain object: any key type, insertion order, `.size`, no prototype collisions, iterable. Use for real key-value data.
- **Set**: uniqueness — `[...new Set(arr)]` dedup one-liner ⭐.
- **WeakMap/WeakSet**: object keys held weakly (GC-able) — metadata/private data without leaks (parallel to Java's WeakHashMap!).

## Iterators & generators
- Protocol: object with `next() → {value, done}`; `Symbol.iterator` makes anything `for...of`-able.
- `function*` + `yield` — lazy sequences, pausable functions; recognize as the engine behind redux-saga.

## Also know
- Optional chaining `user?.address?.city`, nullish coalescing `x ?? 'default'` (vs `||` which also kills `0`/`''` ⭐).
- `Symbol` — unique keys, well-known symbols.
- Getter/setter shorthand, computed keys `{ [key]: v }`, property shorthand `{ name }`.
- `Array.from`, `Object.entries/keys/values/fromEntries`, `structuredClone` (real deep copy ⭐), `at(-1)`, `Promise` (topic 06).

## Top interview questions
1. **Destructure a nested object with rename + default.** (in code)
2. **Spread copy is shallow — demonstrate the bug and the fixes.** ⭐ (structuredClone / manual nested spread)
3. **`??` vs `||`?** Only null/undefined vs all falsies — `count || 10` breaks for 0 ⭐.
4. **Map vs object — when and why?**
5. **Named vs default exports; why do bundlers prefer named?** Tree-shaking.
6. **What is a generator? Write an infinite id generator.** (in code)
7. **Remove duplicates from an array — one line.** `[...new Set(arr)]`
8. **How does `for...of` know how to iterate your custom object?** `Symbol.iterator` (in code).

➡️ Code: [`es6-features.js`](./es6-features.js)
