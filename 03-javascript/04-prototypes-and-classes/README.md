# 04 — Prototypes & Classes 🔴

## Real-life analogy
The prototype chain is **asking up the family tree**: you ask a kid "can you make biryani?" — if they can't, they ask their mother, then grandmother, up the chain until someone knows the recipe or the chain ends (`null`). Nobody photocopies the recipe into every child (**no copying — live delegation**). ES6 `class` is just **writing the family tree in a formal register**: nicer notation, same tree underneath (**syntactic sugar** — mostly).

## The core model ⭐⭐
- Every object has an internal link `[[Prototype]]` (exposed as `__proto__`, properly accessed via `Object.getPrototypeOf`).
- Property lookup: own properties → prototype → prototype's prototype → … → `null`.
- **`prototype` vs `__proto__`** ⭐: `Fn.prototype` is a property ON functions — the object that will become `[[Prototype]]` of instances created with `new Fn()`. `obj.__proto__` is the actual link on an instance. So: `rabbit.__proto__ === Rabbit.prototype`.

## What `new` does ⭐ (write it!)
1. Create empty object.
2. Link it: `obj.__proto__ = Fn.prototype`.
3. Call `Fn` with `this = obj`.
4. Return `obj` (unless Fn explicitly returns an object).

## ES6 classes
- `class`, `constructor`, methods (go on `.prototype` — shared, not per-instance ⭐), `static` (on the constructor itself), getters/setters, `#private` fields (real privacy, runtime-enforced), public field syntax.
- `extends` + `super()` ⭐: subclass constructor MUST call `super()` before using `this`.
- Method overriding + `super.method()` calls.
- Not *pure* sugar: classes are TDZ'd (no hoisting-use), always strict mode, methods non-enumerable, constructor requires `new`.

## Older patterns you should recognize
- Constructor functions + `Fn.prototype.method = ...` (pre-2015 code, interview output questions).
- `Object.create(proto)` — direct prototypal creation; `Object.create(null)` = no prototype (pure dict).
- Prototypal vs classical inheritance talking point: JS delegates to live objects; Java copies structure via classes.

## instanceof / property checks ⭐
- `x instanceof Fn` — walks the chain looking for `Fn.prototype`.
- `obj.hasOwnProperty(k)` (or `Object.hasOwn(obj, k)`) vs `k in obj` (includes inherited) ⭐.
- `for...in` iterates inherited enumerables too — filter with hasOwnProperty; `Object.keys` = own only.

## Top interview questions
1. **Explain prototypal inheritance & the lookup chain.**
2. **`prototype` vs `__proto__`?** ⭐ (the one that separates candidates)
3. **What exactly does `new` do?** (4 steps — then implement it, see code)
4. **Are ES6 classes just sugar?** Mostly — over prototypes; list the real differences.
5. **Where do class methods live?** On the prototype — one shared copy (memory question).
6. **How do you create an object without a prototype, and why?** `Object.create(null)` — safe maps, no `toString` collisions.
7. **How does `instanceof` work? Can it lie?** Chain walk; yes — cross-realm objects, or Symbol.hasInstance.
8. **Implement inheritance without `class`.** (constructor functions + Object.create — in code)

➡️ Code: [`prototypes.js`](./prototypes.js)
