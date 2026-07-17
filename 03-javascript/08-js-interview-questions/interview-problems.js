/**
 * TOPIC: The JS problem bank — debounce, throttle, event emitter, pipe,
 * deep flatten, plus the output-prediction gauntlet. Run: node interview-problems.js
 */

// =====================================================================
// 1. DEBOUNCE ⭐⭐ — reset the timer on every call; fire after silence
// =====================================================================
function debounce(fn, wait) {
  let timer;
  return function (...args) {                 // regular fn: forward `this` too
    clearTimeout(timer);                      // cancel the pending run
    timer = setTimeout(() => fn.apply(this, args), wait);
  };
}
// Use: searchInput.addEventListener('input', debounce(callApi, 300))

// =====================================================================
// 2. THROTTLE ⭐⭐ — at most one call per interval
// =====================================================================
function throttle(fn, interval) {
  let last = 0;
  return function (...args) {
    const now = Date.now();
    if (now - last >= interval) {
      last = now;
      fn.apply(this, args);
    }
  };
}
// Use: window.addEventListener('scroll', throttle(updatePosition, 100))

// Demo of the difference:
let dCount = 0, tCount = 0;
const d = debounce(() => dCount++, 30);
const t = throttle(() => tCount++, 30);
const burst = setInterval(() => { d(); t(); }, 10);       // rapid-fire events
setTimeout(() => {
  clearInterval(burst);
  setTimeout(() =>
    console.log(`after burst -> debounce fired ${dCount} time(s), throttle ${tCount} times`), 50);
  // debounce: 1 (only after silence); throttle: several (steady beat)
}, 150);

// =====================================================================
// 3. EVENT EMITTER ⭐ — observer pattern (on/off/once/emit)
// =====================================================================
class EventEmitter {
  #listeners = new Map();                     // event -> Set<fn>

  on(event, fn) {
    if (!this.#listeners.has(event)) this.#listeners.set(event, new Set());
    this.#listeners.get(event).add(fn);
    return () => this.off(event, fn);         // return unsubscribe (React-style)
  }
  off(event, fn)  { this.#listeners.get(event)?.delete(fn); }
  once(event, fn) {
    const wrapper = (...args) => { this.off(event, wrapper); fn(...args); };
    this.on(event, wrapper);
  }
  emit(event, ...args) {
    this.#listeners.get(event)?.forEach(fn => fn(...args));
  }
}
const bus = new EventEmitter();
const unsub = bus.on('order', id => console.log('email service got', id));
bus.once('order', id => console.log('audit (once) got', id));
bus.emit('order', 'ORD-1');                   // both fire
bus.emit('order', 'ORD-2');                   // only email fires
unsub();
bus.emit('order', 'ORD-3');                   // silence

// =====================================================================
// 4. PIPE / COMPOSE — function composition
// =====================================================================
const pipe = (...fns) => x => fns.reduce((acc, fn) => fn(acc), x);
const compose = (...fns) => x => fns.reduceRight((acc, fn) => fn(acc), x);

const trim = s => s.trim(), lower = s => s.toLowerCase(), kebab = s => s.replaceAll(' ', '-');
console.log(pipe(trim, lower, kebab)('  Hello World JS  '));      // "hello-world-js"
console.log(compose(kebab, lower, trim)('  Hello World JS  '));   // same, reversed order

// =====================================================================
// 5. FLATTEN OBJECT to dot-paths (the harder flatten)
// =====================================================================
function flattenObject(obj, prefix = '') {
  return Object.entries(obj).reduce((acc, [key, val]) => {
    const path = prefix ? `${prefix}.${key}` : key;
    if (val && typeof val === 'object' && !Array.isArray(val)) {
      Object.assign(acc, flattenObject(val, path));      // recurse
    } else {
      acc[path] = val;
    }
    return acc;
  }, {});
}
console.log(flattenObject({ user: { name: 'S', address: { city: 'Pune' } }, active: true }));
// { 'user.name': 'S', 'user.address.city': 'Pune', active: true }

// =====================================================================
// 6. EVENT DELEGATION ⭐ (browser concept — sketch)
// =====================================================================
// document.querySelector('#todo-list').addEventListener('click', (e) => {
//   const item = e.target.closest('li.todo');   // works even for items added LATER
//   if (item) toggleDone(item.dataset.id);      // 1 listener instead of N ⭐
// });

// =====================================================================
// 7. OUTPUT-PREDICTION GAUNTLET (answers inline — cover them & predict)
// =====================================================================
console.log('--- gauntlet ---');

// 7a. coercion
console.log('5' + 3);        // "53"   (+ = concat when a string is present)
console.log('5' - 3);        // 2      (- is numeric)
console.log('5' + 3 - 1);    // 52     ("53" - 1)
console.log([] == false);    // true   ([] -> "" -> 0 == 0)
console.log(typeof NaN);     // "number" (!)

// 7b. reference vs value
const cfg = { retries: 3 };
function tweak(c) { c.retries = 99; c = { retries: 0 }; }   // mutation visible, reassignment not
tweak(cfg);
console.log(cfg.retries);    // 99 — same as the Java pass-by-value lesson!

// 7c. closure counter
function makeAdder() { let total = 0; return n => total += n; }
const add = makeAdder();
add(5); add(10);
console.log(add(0));         // 15 — state lives in the closure

// 7d. prototype shadowing
const proto = { greet: () => 'proto' };
const child = Object.create(proto);
console.log(child.greet());  // "proto" (chain)
child.greet = () => 'own';
console.log(child.greet());  // "own"   (shadowed, proto untouched)

// 7e. the async ordering finale
setTimeout(() => console.log('g5: timeout'), 0);
Promise.resolve().then(() => console.log('g3: micro'));
(async () => { console.log('g1: async sync part'); await 0; console.log('g4: after await'); })();
console.log('g2: sync');
// g1, g2, g3, g4, g5 — narrate WHY and the interview is won
