# 02 — Functions & `this` 🟡⭐

## Real-life analogy
`this` is the word **"home"**: its meaning depends on *who says it and from where* — when you say "home" it's your house; when your colleague says it, it's theirs (**call-site determines `this`**). An **arrow function** is like a child who, when asked "where's home?", always points to their parents' house (**inherits `this` lexically** — no home of its own). `bind` is writing your address on a card and handing it over: no matter who reads the card later, "home" is permanently your house.

## The 5 binding rules ⭐⭐ (in precedence order)
1. **`new`** — `this` = the freshly created object.
2. **Explicit** — `call`/`apply`/`bind` set it directly.
3. **Implicit** — `obj.method()` → `this` = obj (the thing **left of the dot at the call site**).
4. **Default** — plain `fn()` → `undefined` (strict) / `window` (sloppy).
5. **Arrow functions ignore all of the above** — `this` is captured from the enclosing scope at *definition* time; call/bind can't change it ⭐.

### The classic lost-`this` trap ⭐
```js
const fn = obj.getName;  fn();          // undefined — method detached from obj
setTimeout(obj.getName, 100);           // same loss
```
Fixes: `obj.getName.bind(obj)`, wrap in arrow `() => obj.getName()`, or define as arrow using enclosing `this`.

## call / apply / bind ⭐
- `fn.call(ctx, a, b)` — invoke now, args listed.
- `fn.apply(ctx, [a, b])` — invoke now, args as array ("**a**pply = **a**rray").
- `fn.bind(ctx, a)` — returns a NEW function, permanently bound (+ partial application). Re-bind attempts are ignored.

## Function forms
- Declaration (hoisted) / expression / **arrow**.
- Arrow limitations ⭐: no own `this`, no `arguments`, no `new` (not a constructor), no `prototype` property. Don't use as object methods when you need `this`.
- **Default params** `function f(x = 10)`, **rest** `(...args)` (real array, replaces `arguments`), spread at call site `f(...arr)`.
- **IIFE** `(function(){ ... })()` — pre-module encapsulation; still seen in interviews.
- **First-class functions** → higher-order functions (accept/return functions) → enables callbacks, closures, currying, React HOCs.
- Pure functions (same input → same output, no side effects) — foundation for React/Redux thinking ⭐.

## Top interview questions
1. **The 5 rules of `this` with examples.** (above — say "call site" a lot)
2. **Predict output**: object method vs detached reference vs arrow method (see code file — run them!).
3. **call vs apply vs bind?**
4. **Why do arrow functions fix the setTimeout-in-method bug?** Lexical `this` — no rebinding at call time.
5. **Implement `bind` yourself.** ⭐ (in code — uses closures + apply)
6. **Arrow function as object method — what's `this`?** Enclosing (module/global) scope, NOT the object ⭐.
7. **What is currying? Write `sum(1)(2)(3)`.** (in code)
8. **`arguments` vs rest params?** Array-like vs real array; arrows have neither own.

➡️ Code: [`functions-this.js`](./functions-this.js)
