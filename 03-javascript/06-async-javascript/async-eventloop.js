/**
 * TOPIC: Async & Event Loop — output-prediction drills, combinators,
 * my Promise.all, promisify, timeout, retry. Run: node async-eventloop.js
 */

// ===== 1. THE output question ⭐⭐ (predict before running!) =====
console.log('A: sync 1');
setTimeout(() => console.log('E: macrotask'), 0);
Promise.resolve().then(() => console.log('C: microtask 1'))
                 .then(() => console.log('D: microtask 2'));   // chained micro runs before macro too
console.log('B: sync 2');
// Order: A B C D E — sync first, then ALL microtasks, then macrotask

// ===== 2. async/await version of the same drill =====
async function drill() {
  console.log('1: fn sync start');            // runs synchronously until first await
  await null;                                  // rest of fn = microtask continuation
  console.log('3: after await (microtask)');
}
console.log('0: before call');
drill();
console.log('2: after call');
// Order: 0, 1, 2, 3

// ===== 3. Sequential vs parallel await ⭐⭐ =====
const fakeApi = (name, ms) =>
  new Promise(res => setTimeout(() => res(`${name} data`), ms));

async function slowWay() {
  const t = Date.now();
  const u = await fakeApi('user', 150);        // waits 150
  const o = await fakeApi('orders', 150);      // THEN waits another 150
  console.log(`sequential: ${Date.now() - t}ms`, u, o);        // ~300ms
}
async function fastWay() {
  const t = Date.now();
  const [u, o] = await Promise.all([fakeApi('user', 150), fakeApi('orders', 150)]);
  console.log(`parallel  : ${Date.now() - t}ms`, u, o);        // ~150ms ⭐
}

// ===== 4. Combinators ⭐ =====
async function combinators() {
  const ok = ms => fakeApi('ok', ms);
  const bad = ms => new Promise((_, rej) => setTimeout(() => rej(new Error('boom')), ms));

  console.log(await Promise.all([ok(10), ok(20)]));            // both values (fail-fast if any rejects)
  console.log(await Promise.allSettled([ok(10), bad(20)]));    // status report, never rejects ⭐
  console.log(await Promise.race([ok(10), ok(500)]));          // first to SETTLE
  console.log(await Promise.any([bad(10), ok(30)]));           // first to FULFILL
}

// ===== 5. Implement Promise.all yourself ⭐ =====
function myPromiseAll(promises) {
  return new Promise((resolve, reject) => {
    const results = new Array(promises.length);
    let remaining = promises.length;
    if (remaining === 0) return resolve([]);
    promises.forEach((p, i) => {
      Promise.resolve(p).then(value => {        // Promise.resolve: accept plain values too
        results[i] = value;                     // preserve ORDER, not completion order ⭐
        if (--remaining === 0) resolve(results);
      }, reject);                               // first rejection wins
    });
  });
}

// ===== 6. promisify: callback API -> promise API ⭐ =====
function promisify(fn) {                         // assumes node-style cb(err, data)
  return (...args) => new Promise((resolve, reject) =>
    fn(...args, (err, data) => err ? reject(err) : resolve(data)));
}
const readConfigCb = (path, cb) => setTimeout(() => cb(null, `{cfg for ${path}}`), 10);
const readConfig = promisify(readConfigCb);

// ===== 7. Timeout wrapper via race ⭐ =====
const withTimeout = (promise, ms) =>
  Promise.race([
    promise,
    new Promise((_, rej) => setTimeout(() => rej(new Error(`timeout after ${ms}ms`)), ms)),
  ]);

// ===== 8. Retry with exponential backoff (the Spring-Retry of JS) =====
async function retry(fn, attempts = 3, baseDelay = 50) {
  for (let i = 1; i <= attempts; i++) {
    try { return await fn(); }
    catch (e) {
      if (i === attempts) throw e;
      const delay = baseDelay * 2 ** (i - 1) + Math.random() * 20;   // backoff + jitter
      console.log(`attempt ${i} failed (${e.message}), retrying in ~${delay | 0}ms`);
      await new Promise(r => setTimeout(r, delay));
    }
  }
}

// ===== Run everything sequentially so the output reads clean =====
(async () => {
  await new Promise(r => setTimeout(r, 50));     // let section 1-2 output settle
  await slowWay();
  await fastWay();
  await combinators();
  console.log('myPromiseAll:', await myPromiseAll([fakeApi('a', 30), fakeApi('b', 10), 42]));
  console.log('promisified:', await readConfig('/app.yml'));
  try { await withTimeout(fakeApi('slow', 500), 100); }
  catch (e) { console.log('timeout works:', e.message); }

  let flaky = 0;
  const result = await retry(async () => {
    if (++flaky < 3) throw new Error('transient');
    return 'recovered!';
  });
  console.log('retry:', result);
})();
