# 07 — Arrays, Objects & Methods 🟡⭐

## Real-life analogy
`map`/`filter`/`reduce` are an **assembly line**: `map` is a station that transforms every item passing through (same count out), `filter` is a quality inspector who discards rejects (fewer items, unchanged), `reduce` is the **packing station at the end** that combines everything into one box (any shape: number, object, another array). Shallow vs deep copy: photocopying a **folder of house keys** — you get a new folder (new array/object) but the keys still open the SAME houses (shared nested references).

## The big three ⭐⭐
- `map(fn)` — transform, same length, NEW array.
- `filter(fn)` — keep matching, NEW array.
- `reduce(fn, init)` — fold to anything. **Always pass the initial value** ⭐. Everything below is implementable with reduce (group-by, count-by, flatten — the show-off answers).

## Full method inventory (know return value + mutates?)
| Mutates original ⚠️ | Returns new / value ✔ |
|---|---|
| `push pop shift unshift` | `slice concat` |
| `splice` ⭐ (add/remove anywhere) | `map filter reduce flat flatMap` |
| `sort` ⭐ `reverse` `fill` | `toSorted toReversed toSpliced with` (ES2023 immutable twins ⭐) |
| | `find findIndex findLast some every includes indexOf` |
| | `join keys values entries at` |

⭐ Classics: `slice` (copy) vs `splice` (mutate); `sort()` is **lexicographic by default** — `[10,9,1].sort()` → `[1,10,9]`; numbers need `(a,b) => a-b`; sort mutates (use `toSorted`).
`some` (≥1 matches) vs `every` (all match); `find` (element) vs `filter` (array); `flat(Infinity)`, `flatMap`.

## Shallow vs deep copy ⭐⭐
- Shallow: spread `{...o}` / `[...a]`, `Object.assign`, `slice` — nested objects SHARED.
- Deep: `structuredClone(o)` (modern ⭐), `JSON.parse(JSON.stringify(o))` (loses functions/undefined/Dates ⭐ — know the flaws), recursive clone (write it — in code).
- Equality: `===` is reference equality for objects; deep-equal must be written/imported (in code).

## Object operations
`Object.keys/values/entries/fromEntries` ⭐, `Object.assign`, `Object.freeze` (shallow!), `hasOwn`,
property shorthand, computed keys, getters/setters, `delete` vs rest-omit `const {x, ...rest} = o` (immutable ⭐).

## Iteration styles
`for...of` (values, iterables, await-friendly, break-able) vs `for...in` (keys + inherited — avoid for arrays ⭐) vs `forEach` (no break, no await ⭐ — `forEach(async...)` doesn't wait!).

## Top interview questions
1. **map vs forEach?** Returns new array vs undefined; chainable; intent.
2. **Implement your own `map`/`filter` (polyfill).** ⭐ (in code)
3. **Group an array of objects by a property with reduce.** ⭐⭐ (in code — also `Object.groupBy` ES2024)
4. **Why does `[10, 9, 1].sort()` give `[1, 10, 9]`?** String comparison default.
5. **Shallow vs deep copy — show a bug and three fixes.** ⭐
6. **Flatten a nested array without `flat`.** (recursive reduce — in code)
7. **`JSON.parse(JSON.stringify())` drawbacks?** functions/undefined dropped, Dates → strings, circular refs throw.
8. **Second largest / remove falsy / chunk / intersection** — mini drills (in code).
9. **Why is `forEach` + `async/await` broken?** Doesn't await callbacks; use `for...of` or `Promise.all(map)`.

➡️ Code: [`arrays-objects.js`](./arrays-objects.js)
