/**
 * TOPIC: Arrays & Objects — the method drills, polyfills, group-by,
 * deep copy/equal, and classic mini-problems. Run: node arrays-objects.js
 */

const employees = [
  { name: 'Asha',  dept: 'IT',  salary: 95 },
  { name: 'Ravi',  dept: 'HR',  salary: 55 },
  { name: 'Meera', dept: 'IT',  salary: 120 },
  { name: 'Kiran', dept: 'Fin', salary: 80 },
];

// ===== 1. map / filter / reduce ⭐⭐ =====
console.log(employees.map(e => e.name));                          // transform
console.log(employees.filter(e => e.salary > 80).map(e => e.name)); // chain
console.log(employees.reduce((sum, e) => sum + e.salary, 0));     // 350 — ALWAYS pass init ⭐

// GROUP BY with reduce ⭐⭐ (the most-asked one)
const byDept = employees.reduce((acc, e) => {
  (acc[e.dept] ??= []).push(e.name);         // ??= : create bucket on first sight
  return acc;
}, {});
console.log(byDept);                          // { IT: ['Asha','Meera'], HR: ['Ravi'], Fin: ['Kiran'] }
// ES2024 built-in: Object.groupBy(employees, e => e.dept)

// countBy + maxBy in one pass each
console.log(employees.reduce((acc, e) => (acc[e.dept] = (acc[e.dept] ?? 0) + 1, acc), {}));
console.log(employees.reduce((top, e) => e.salary > top.salary ? e : top).name);   // Meera

// ===== 2. sort traps ⭐ =====
console.log([10, 9, 1].sort());               // [1, 10, 9] — lexicographic!
console.log([10, 9, 1].sort((a, b) => a - b)); // [1, 9, 10]
const nums = [3, 1, 2];
const sorted = nums.toSorted();               // ES2023: immutable twin ⭐
console.log(nums, sorted);                    // [3,1,2] [1,2,3] — original untouched

// Multi-field object sort (dept asc, salary desc)
console.log([...employees]
  .sort((a, b) => a.dept.localeCompare(b.dept) || b.salary - a.salary)
  .map(e => `${e.dept}:${e.name}`));

// ===== 3. Polyfills: write map & filter ⭐ =====
Array.prototype.myMap = function (fn) {
  const out = [];
  for (let i = 0; i < this.length; i++)
    if (i in this) out.push(fn(this[i], i, this));   // `i in this`: skip holes like the real one
  return out;
};
Array.prototype.myFilter = function (fn) {
  const out = [];
  for (let i = 0; i < this.length; i++)
    if (i in this && fn(this[i], i, this)) out.push(this[i]);
  return out;
};
console.log([1, 2, 3].myMap(x => x * 2), [1, 2, 3, 4].myFilter(x => x % 2 === 0));

// ===== 4. Flatten without flat() ⭐ =====
const flatten = arr => arr.reduce(
  (acc, x) => acc.concat(Array.isArray(x) ? flatten(x) : x), []);
console.log(flatten([1, [2, [3, [4]], 5]]));   // [1,2,3,4,5]  (built-in: arr.flat(Infinity))

// ===== 5. Deep copy & deep equal ⭐⭐ =====
const order = { id: 1, items: [{ sku: 'A', qty: 2 }], placed: new Date('2026-01-01') };

const json = JSON.parse(JSON.stringify(order));
console.log('JSON trick broke Date:', typeof json.placed);        // "string" ⭐ flaw

const good = structuredClone(order);
good.items[0].qty = 99;
console.log('structuredClone isolated:', order.items[0].qty, good.items[0].qty);  // 2 99

function deepEqual(a, b) {                     // the write-it-yourself version
  if (a === b) return true;
  if (typeof a !== 'object' || typeof b !== 'object' || a === null || b === null) return false;
  const ka = Object.keys(a), kb = Object.keys(b);
  if (ka.length !== kb.length) return false;
  return ka.every(k => deepEqual(a[k], b[k]));
}
console.log(deepEqual({ x: { y: [1] } }, { x: { y: [1] } }));      // true (=== would be false)

// ===== 6. Mini-drills =====
console.log([...new Set([1, 5, 2, 5, 1])]);                        // dedup
console.log([0, 'a', '', null, 'b', NaN, false].filter(Boolean));  // remove falsy ⭐
console.log([...new Set([1, 2, 3])].filter(x => new Set([2, 3, 4]).has(x))); // intersection

const chunk = (arr, size) =>
  Array.from({ length: Math.ceil(arr.length / size) }, (_, i) => arr.slice(i * size, i * size + size));
console.log(chunk([1, 2, 3, 4, 5], 2));                            // [[1,2],[3,4],[5]]

const secondLargest = arr => [...new Set(arr)].sort((a, b) => b - a)[1];
console.log(secondLargest([4, 9, 2, 9, 7]));                       // 7

// ===== 7. forEach + async is broken ⭐ =====
const delay = ms => new Promise(r => setTimeout(r, ms));
(async () => {
  const t = Date.now();
  await Promise.all([50, 60, 70].map(async ms => delay(ms)));      // parallel + awaited ✔
  console.log('Promise.all(map) waited properly:', Date.now() - t >= 70);
  // [1,2].forEach(async () => await delay(100))  ← returns immediately, awaits NOTHING ⚠️
})();

// ===== 8. for...in vs for...of on arrays =====
const arr = ['a', 'b'];
arr.extra = 'surprise';
for (const k in arr) console.log('for-in sees:', k);     // 0, 1, extra ⚠️ (keys + custom props)
for (const v of arr) console.log('for-of sees:', v);     // a, b ✔ (values only)
