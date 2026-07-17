/**
 * TOPIC: JS Fundamentals — hoisting, TDZ, coercion traps, const mutation.
 * Run: node fundamentals.js
 */

// ===== 1. Hoisting & TDZ ⭐ =====
console.log(hoistedVar);        // undefined (var hoisted + initialized)
// console.log(tdzLet);         // ReferenceError: Cannot access before initialization
var hoistedVar = 1;
let tdzLet = 2;

sayHi();                        // works — function DECLARATIONS hoist fully
function sayHi() { console.log('hi from hoisted declaration'); }
// sayBye();                    // TypeError — expression not assigned yet
const sayBye = () => console.log('bye');

// ===== 2. var vs let in the CLASSIC loop trap ⭐⭐ =====
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log('var i =', i), 10);   // 3, 3, 3 — one shared function-scoped i
}
for (let j = 0; j < 3; j++) {
  setTimeout(() => console.log('let j =', j), 20);   // 0, 1, 2 — new binding per iteration
}

// ===== 3. const = fixed BINDING, not frozen VALUE ⭐ =====
const user = { name: 'Shilpak' };
user.name = 'S';                // ✔ mutation fine
// user = {};                   // ✘ TypeError: assignment to constant
const frozen = Object.freeze({ a: 1, nested: { b: 2 } });
frozen.a = 99;                  // silently ignored (throws in strict mode)
frozen.nested.b = 99;           // ✔ works — freeze is SHALLOW ⭐
console.log('frozen:', frozen.a, 'nested (shallow!):', frozen.nested.b);  // 1, 99

// ===== 4. Coercion output questions ⭐ =====
console.log(1 + '2');           // "12"   (+ prefers strings)
console.log('3' - 1);           // 2      (- is numeric only)
console.log(1 + 2 + '3');       // "33"   (left-to-right: 3 + '3')
console.log([] + []);           // ""     (both -> "")
console.log([] + {});           // "[object Object]"
console.log(0 == '0');          // true   (coercion)
console.log(0 == []);           // true   ([] -> "" -> 0)
console.log('0' == []);         // false  ("" != "0") — the killer combo
console.log(null == undefined); // true   (special case, only each other)
console.log(null == 0);         // false  (null only == undefined)
console.log(NaN === NaN);       // false ⭐
console.log(Number.isNaN(NaN), Object.is(NaN, NaN));  // true true

// ===== 5. Truthy/falsy ⭐ =====
const falsies = [false, 0, -0, 0n, '', null, undefined, NaN];
console.log('all falsy:', falsies.every(v => !v));           // true
console.log('but truthy:', Boolean('0'), Boolean([]), Boolean({}));  // true true true

// ===== 6. Floating point =====
console.log(0.1 + 0.2 === 0.3);                              // false ⭐
console.log(Math.abs(0.1 + 0.2 - 0.3) < Number.EPSILON);     // true — the right compare
console.log(9007199254740992n + 1n);                         // BigInt beyond MAX_SAFE_INTEGER

// ===== 7. typeof cheat table =====
console.log(typeof undefined, typeof null, typeof [], typeof function(){}, typeof Symbol());
// "undefined" "object"(bug!) "object" "function" "symbol"
console.log(Array.isArray([]));                              // the RIGHT array check ⭐

// ===== 8. undefined vs null vs undeclared =====
let a;                          // declared, undefined
let b = null;                   // intentional empty
console.log(a === undefined, b === null, a == b);  // true true true (== bridges them)
