/**
 * TOPIC: Functions & `this` — the 5 rules, lost-this trap + fixes,
 * hand-written bind, currying. Run: node functions-this.js
 */
'use strict';

// ===== 1. The 5 binding rules ⭐ =====
const user = {
  name: 'Shilpak',
  hello() { return `hi, ${this.name}`; },            // implicit: this = left of dot
  helloArrow: () => `arrow this: ${typeof this}`,    // arrow: NOT user — encloses module scope ⭐
};
console.log(user.hello());                           // "hi, Shilpak"      (implicit)
console.log(user.helloArrow());                      // arrow ignores the object!

const detached = user.hello;
try { console.log(detached()); }                     // TypeError in strict (this=undefined) ⭐
catch (e) { console.log('lost this ->', e.constructor.name); }

console.log(detached.call({ name: 'CallCtx' }));     // explicit: "hi, CallCtx"
const bound = detached.bind({ name: 'BoundCtx' });
console.log(bound());                                // "hi, BoundCtx"
console.log(bound.call({ name: 'Ignored' }));        // still BoundCtx — bind is permanent ⭐

function Person(name) { this.name = name; }          // new-binding
console.log(new Person('NewCtx').name);              // "NewCtx"

// ===== 2. The classic setTimeout trap + all 3 fixes ⭐⭐ =====
const timer = {
  seconds: 0,
  startBroken() {
    setTimeout(function () {
      // this.seconds++  ->  this is undefined/global here — BROKEN
      console.log('broken: this is', typeof this);
    }, 5);
  },
  startArrow() {                                     // FIX 1: arrow inherits method's this
    setTimeout(() => { this.seconds++; console.log('arrow fix:', this.seconds); }, 10);
  },
  startBind() {                                      // FIX 2: bind
    setTimeout(function () { this.seconds++; console.log('bind fix:', this.seconds); }.bind(this), 15);
  },
  startSelf() {                                      // FIX 3 (legacy): const self = this
    const self = this;
    setTimeout(function () { self.seconds++; console.log('self fix:', self.seconds); }, 20);
  },
};
timer.startBroken(); timer.startArrow(); timer.startBind(); timer.startSelf();

// ===== 3. Implement bind yourself ⭐ (closure + apply) =====
Function.prototype.myBind = function (ctx, ...preset) {
  const fn = this;                                   // the function myBind was called on
  return function (...later) {
    return fn.apply(ctx, [...preset, ...later]);     // closure remembers ctx + preset args
  };
};
function greet(greeting, punct) { return `${greeting}, ${this.name}${punct}`; }
const hiRavi = greet.myBind({ name: 'Ravi' }, 'Hello');
console.log(hiRavi('!'));                            // "Hello, Ravi!" — partial application too

// ===== 4. Currying ⭐ =====
const sum = a => b => c => a + b + c;                // fixed arity
console.log('sum(1)(2)(3) =', sum(1)(2)(3));         // 6

function curry(fn) {                                 // generic curry for any arity
  return function curried(...args) {
    return args.length >= fn.length
      ? fn(...args)
      : (...more) => curried(...args, ...more);
  };
}
const add3 = curry((a, b, c) => a + b + c);
console.log(add3(1)(2)(3), add3(1, 2)(3), add3(1)(2, 3));  // 6 6 6

// ===== 5. Higher-order functions + rest/spread/default =====
const withLogging = fn => (...args) => {             // HOF: decorates any function
  console.log(`calling ${fn.name}(${args})`);
  return fn(...args);
};
const multiply = (a, b = 2) => a * b;                // default param
console.log(withLogging(multiply)(5));               // logs, then 10

// ===== 6. arguments vs rest =====
function oldStyle() { return Array.from(arguments).length; }   // array-LIKE
const newStyle = (...args) => args.length;                     // real array; arrows lack arguments
console.log(oldStyle(1, 2, 3), newStyle(1, 2, 3));   // 3 3
