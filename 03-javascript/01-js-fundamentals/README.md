# 01 — JavaScript Fundamentals 🟢⭐

## Real-life analogy
`var`, `let`, `const` are three kinds of **name badges at a conference**: `var` badges are photocopied and stuck on the building's front door before the event even starts (**hoisting to function scope** — anyone can claim it early, even twice). `let` badges exist inside one room only (**block scope**) and are printed the moment you register — walk in before registration and security stops you (**temporal dead zone**). `const` is a `let` badge laminated: you can't swap the badge (no reassignment), but if the badge points to a bag (object), you can still put things in the bag (**mutation allowed**).

## var / let / const ⭐⭐
| | var | let | const |
|---|---|---|---|
| Scope | function | block | block |
| Hoisted | yes, initialized `undefined` | yes, but **TDZ** (ReferenceError before decl) | TDZ |
| Re-declare | ✔ | ✘ | ✘ |
| Re-assign | ✔ | ✔ | ✘ (object *contents* still mutable ⭐) |
| Global decl attaches to `window` | ✔ | ✘ | ✘ |

**Hoisting** ⭐: declarations move to the top of their scope during the *creation phase*. Function **declarations** hoist fully (callable before definition); function **expressions/arrow consts** don't.

## Types & coercion ⭐
- 7 primitives: `string number bigint boolean undefined symbol null` + `object` (arrays/functions are objects).
- `typeof null === "object"` — historic bug ⭐. `typeof undefined === "undefined"`.
- `undefined` (declared, no value) vs `null` (intentional absence) vs undeclared (ReferenceError).
- **`==` coerces, `===` doesn't** ⭐: `0 == "0"` true, `0 == []` true(!), `null == undefined` true (only each other), `NaN == NaN` false (use `Number.isNaN`).
- Falsy values (memorize — only 6-ish): `false 0 -0 0n "" null undefined NaN`. Everything else truthy — including `"0"`, `[]`, `{}` ⭐.
- Coercion gotchas: `1 + "2" = "12"` (string wins with +), `"3" - 1 = 2` (- is numeric), `[] + [] = ""`, `[] + {} = "[object Object]"`.

## Numbers
`0.1 + 0.2 !== 0.3` ⭐ (IEEE-754 binary floats — same as Java double; fix: `Number.EPSILON` compare or integer cents). `Number.MAX_SAFE_INTEGER` (2^53−1) then use `BigInt`.

## Execution model (sets up everything else)
Each call creates an **execution context** (variable environment + scope chain + `this`). Contexts stack up (call stack). JS is **single-threaded**; concurrency comes from the event loop (topic 06).

## Strict mode
`'use strict'`: assignments to undeclared vars throw, `this` in plain functions = `undefined` (not window), silent errors become loud. Modules/classes are strict by default.

## Top interview questions
1. **Output?** `console.log(x); var x = 5;` → `undefined` (hoisted). With `let` → ReferenceError (TDZ).
2. **`==` vs `===`?** + classic outputs: `[] == false` (true), `"" == 0` (true), `null == 0` (false).
3. **Is `const obj = {}` fully immutable?** No — binding is fixed, object is mutable; `Object.freeze` for shallow freeze ⭐.
4. **Why `typeof null === 'object'`?**
5. **Why does `0.1 + 0.2 !== 0.3`? How do you compare?**
6. **What are the falsy values?** (all 6-ish, and `[]`/`"0"` are truthy!)
7. **Function declaration vs expression — hoisting difference?**
8. **What happens without `var/let/const`?** Implicit global (or ReferenceError in strict mode).

➡️ Code: [`fundamentals.js`](./fundamentals.js)
