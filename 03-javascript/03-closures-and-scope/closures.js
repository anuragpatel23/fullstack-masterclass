/**
 * TOPIC: Closures — counter, module pattern, loop trap + fixes, once(),
 * variables-not-values proof, memoize. Run: node closures.js
 */

// ===== 1. The canonical closure: private counter ⭐ =====
function createCounter() {
  let count = 0;                                   // private — invisible from outside
  return {
    increment: () => ++count,
    decrement: () => --count,
    get: () => count,
  };
}
const c1 = createCounter(), c2 = createCounter();
c1.increment(); c1.increment(); c2.increment();
console.log('c1:', c1.get(), 'c2:', c2.get());     // 2 1 — independent backpacks ⭐
console.log('count is private:', c1.count);        // undefined

// ===== 2. Module pattern: hidden state, public API =====
const wallet = (function () {
  let balance = 0;                                 // truly private
  const history = [];
  return {
    deposit(amt) { if (amt > 0) { balance += amt; history.push(`+${amt}`); } return balance; },
    withdraw(amt) {
      if (amt > balance) throw new Error('insufficient funds');
      balance -= amt; history.push(`-${amt}`); return balance;
    },
    statement: () => [...history],                 // defensive copy out
  };
})();
wallet.deposit(100); wallet.withdraw(30);
console.log('wallet:', wallet.statement());        // ['+100', '-30']

// ===== 3. THE loop trap + all 3 fixes ⭐⭐ =====
for (var i = 0; i < 3; i++)
  setTimeout(() => console.log('var trap:', i), 10);      // 3 3 3

for (let j = 0; j < 3; j++)                               // FIX 1: let = binding per iteration
  setTimeout(() => console.log('let fix:', j), 20);       // 0 1 2

for (var k = 0; k < 3; k++)
  ((copy) => setTimeout(() => console.log('IIFE fix:', copy), 30))(k);  // FIX 2: copy via IIFE

for (var m = 0; m < 3; m++)
  setTimeout((val) => console.log('arg fix:', val), 40, m);            // FIX 3: extra setTimeout args

// ===== 4. Closures capture VARIABLES, not values ⭐ =====
function makeGetter() {
  let secret = 'original';
  const get = () => secret;                        // closes over the VARIABLE
  secret = 'changed AFTER creation';
  return get;
}
console.log(makeGetter());                          // "changed AFTER creation" — live reference!

// ===== 5. once(fn) — interview one-liner =====
function once(fn) {
  let called = false, result;
  return (...args) => {
    if (!called) { called = true; result = fn(...args); }
    return result;                                  // subsequent calls: cached first result
  };
}
const init = once(() => { console.log('initializing…'); return 42; });
console.log(init(), init(), init());                // logs once → 42 42 42

// ===== 6. memoize(fn) — closure over a cache =====
function memoize(fn) {
  const cache = new Map();
  return (...args) => {
    const key = JSON.stringify(args);
    if (!cache.has(key)) cache.set(key, fn(...args));
    return cache.get(key);
  };
}
let calls = 0;
const slowSquare = memoize(n => { calls++; return n * n; });
slowSquare(9); slowSquare(9); slowSquare(9);
console.log('computed only', calls, 'time(s)');     // 1

// ===== 7. Function factory =====
const multiplyBy = factor => n => n * factor;       // factor lives in the backpack
const double = multiplyBy(2), triple = multiplyBy(3);
console.log(double(5), triple(5));                  // 10 15

// ===== 8. Output-prediction classic =====
function outer() {
  let x = 10;
  function middle() {
    x += 5;                                         // mutates OUTER's x through the chain
    function inner() { console.log('chain sees x =', x); }
    inner();
  }
  middle(); middle();
}
outer();                                            // 15, then 20 — shared environment
