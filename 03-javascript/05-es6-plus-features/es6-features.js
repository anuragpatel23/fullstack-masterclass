/**
 * TOPIC: ES6+ — destructuring, spread traps, Map/Set, generators,
 * optional chaining, nullish coalescing. Run: node es6-features.js
 */

// ===== 1. Destructuring, all flavors ⭐⭐ =====
const user = { name: 'Shilpak', role: 'fullstack', address: { city: 'Pune', pin: '411001' } };

const { name, role: job, level = 'senior', address: { city } } = user;  // pick, rename, default, nested
console.log(name, job, level, city);              // Shilpak fullstack senior Pune

const scores = [98, 87, 91, 79, 85];
const [top, , third, ...others] = scores;         // skip + rest
console.log(top, third, others);                  // 98 91 [79, 85]

let a = 1, b = 2;
[a, b] = [b, a];                                  // swap without temp
console.log('swapped:', a, b);                    // 2 1

// Param destructuring = how React components receive props ⭐
const Badge = ({ name, role = 'guest' }) => `${name} (${role})`;
console.log(Badge(user));                         // Shilpak (fullstack)

// ===== 2. Spread: merge, copy... and the SHALLOW trap ⭐⭐ =====
const defaults = { theme: 'light', lang: 'en' };
const prefs = { ...defaults, theme: 'dark' };     // later keys win
console.log(prefs);                                // { theme: 'dark', lang: 'en' }

const { pin, ...withoutPin } = user.address;      // "omit a key" idiom
console.log(withoutPin);                           // { city: 'Pune' }

const original = { config: { retries: 3 } };
const copy = { ...original };                     // SHALLOW!
copy.config.retries = 99;
console.log('original mutated!:', original.config.retries);   // 99 — the React state bug ⭐

const deep = structuredClone(original);           // real deep copy (modern answer)
deep.config.retries = 3;
console.log('after structuredClone fix:', original.config.retries, deep.config.retries); // 99 3

// Immutable nested update (the React way):
const fixed = { ...original, config: { ...original.config, retries: 5 } };
console.log('immutable update:', fixed.config.retries, original.config.retries);  // 5 99

// ===== 3. ?? vs || ⭐ =====
const pageSize = 0;
console.log(pageSize || 20);                      // 20 — WRONG, 0 was a valid value!
console.log(pageSize ?? 20);                      // 0  — ?? only replaces null/undefined ⭐

const maybe = { profile: null };
console.log(maybe.profile?.email ?? 'no email');  // optional chaining short-circuits to undefined

// ===== 4. Map & Set ⭐ =====
const votes = new Map();
const alice = { id: 1 }, bob = { id: 2 };
votes.set(alice, 10).set(bob, 7).set('anonymous', 3);   // ANY key type, chainable
console.log(votes.get(alice), votes.size);        // 10 3
for (const [voter, count] of votes) void [voter, count];  // insertion-ordered, iterable

console.log([...new Set([1, 2, 2, 3, 1])]);       // [1, 2, 3] — dedup one-liner ⭐

// WeakMap: metadata that never blocks GC
const secrets = new WeakMap();
secrets.set(alice, 'token-xyz');                  // when alice is GC'd, entry vanishes

// ===== 5. Object utilities you use daily =====
const stock = { laptop: 5, mouse: 42, monitor: 0 };
console.log(Object.entries(stock).filter(([, qty]) => qty > 0).map(([item]) => item)); // in-stock
console.log(Object.fromEntries(Object.entries(stock).map(([k, v]) => [k, v * 2])));    // map an object

// ===== 6. Generators ⭐ =====
function* idGenerator() {                          // infinite lazy sequence
  let id = 1;
  while (true) yield id++;
}
const ids = idGenerator();
console.log(ids.next().value, ids.next().value, ids.next().value);  // 1 2 3

function* take(n, iterable) {                      // generators compose
  let i = 0;
  for (const x of iterable) { if (i++ >= n) return; yield x; }
}
console.log([...take(4, idGenerator())]);          // [1, 2, 3, 4]

// ===== 7. Custom iterable via Symbol.iterator =====
const playlist = {
  songs: ['A', 'B', 'C'],
  *[Symbol.iterator]() { yield* this.songs; },     // makes for...of / spread work
};
console.log([...playlist]);                        // ['A', 'B', 'C']

// ===== 8. Template literals & misc =====
const rows = [['java', 95], ['react', 88]];
console.log(rows.map(([skill, score]) => `${skill.padEnd(6)} ${score}%`).join('\n'));
console.log([10, 20, 30].at(-1));                  // 30 — last element, cleanly
const key = 'dynamic';
console.log({ [key + 'Key']: true });              // computed property names
