/**
 * TOPIC: Hooks — stale closure bug + 3 fixes, every core hook in action,
 * and the 5 must-write custom hooks.
 */
import {
  useState, useEffect, useRef, useMemo, useCallback, useReducer, useContext, createContext,
} from 'react';

// =====================================================================
// 1. THE STALE CLOSURE BUG + 3 FIXES ⭐⭐
// =====================================================================
export function StaleClosureDemo() {
  const [broken, setBroken] = useState(0);
  const [fixed, setFixed] = useState(0);
  const latest = useRef(0);

  // ❌ BUG: effect runs once ([]), its closure captured broken=0 forever
  useEffect(() => {
    const id = setInterval(() => setBroken(broken + 1), 1000);  // always 0 + 1
    return () => clearInterval(id);
  }, []);                                                        // count sticks at 1

  // ✔ FIX 1: functional update — no need to read the closure at all
  useEffect(() => {
    const id = setInterval(() => setFixed(f => f + 1), 1000);
    return () => clearInterval(id);
  }, []);

  // ✔ FIX 2: honest deps — effect re-subscribes every change (fine for cheap setups)
  // useEffect(() => { ... }, [fixed]);

  // ✔ FIX 3: ref as "latest value" escape hatch (for callbacks you can't re-create)
  useEffect(() => { latest.current = fixed; });                  // updated after every render
  useEffect(() => {
    const id = setInterval(() => console.log('latest via ref:', latest.current), 5000);
    return () => clearInterval(id);
  }, []);

  return <p>broken: {broken} (stuck at 1) | fixed: {fixed} (ticking)</p>;
}

// =====================================================================
// 2. Fetch effect done right ⭐ (race guard + abort)
// =====================================================================
export function UserCard({ userId }) {
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    setUser(null);
    fetch(`/api/users/${userId}`, { signal: controller.signal })
      .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.json(); })
      .then(setUser)
      .catch(e => { if (e.name !== 'AbortError') setError(e.message); });

    return () => controller.abort();     // fast userId switches: cancel the stale request ⭐
  }, [userId]);                          // honest deps

  if (error) return <p>failed: {error}</p>;
  return user ? <p>{user.name}</p> : <p>loading…</p>;
}

// =====================================================================
// 3. useReducer — mini-Redux for multi-field state
// =====================================================================
function cartReducer(state, action) {
  switch (action.type) {
    case 'add':    return { ...state, items: [...state.items, action.item], count: state.count + 1 };
    case 'remove': return { ...state,
                            items: state.items.filter(i => i.id !== action.id),
                            count: state.count - 1 };
    case 'clear':  return { items: [], count: 0 };
    default:       throw new Error('unknown action ' + action.type);
  }
}
export function Cart() {
  const [cart, dispatch] = useReducer(cartReducer, { items: [], count: 0 });
  return (
    <div>
      <p>{cart.count} items</p>
      <button onClick={() => dispatch({ type: 'add', item: { id: Date.now(), sku: 'X' } })}>add</button>
      <button onClick={() => dispatch({ type: 'clear' })}>clear</button>
    </div>
  );
}

// =====================================================================
// 4. useContext + memoized provider value ⭐
// =====================================================================
const ThemeContext = createContext({ theme: 'light', toggle: () => {} });

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');
  const toggle = useCallback(() => setTheme(t => (t === 'light' ? 'dark' : 'light')), []);
  // ⭐ memoize the value: `value={{theme, toggle}}` inline would be a NEW object
  // every render -> every consumer re-renders even when nothing changed.
  const value = useMemo(() => ({ theme, toggle }), [theme, toggle]);
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
export function ThemeButton() {
  const { theme, toggle } = useContext(ThemeContext);
  return <button onClick={toggle}>theme: {theme}</button>;
}

// =====================================================================
// 5. The 5 must-write custom hooks ⭐⭐
// =====================================================================
export function useToggle(initial = false) {
  const [on, setOn] = useState(initial);
  const toggle = useCallback(() => setOn(o => !o), []);
  return [on, toggle];
}

export function useDebounce(value, delay = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);       // key press before delay -> cancel + restart
  }, [value, delay]);
  return debounced;                       // use: const q = useDebounce(searchText) -> effect on q
}

export function usePrevious(value) {
  const ref = useRef();
  useEffect(() => { ref.current = value; });   // runs AFTER render
  return ref.current;                           // so render sees the PREVIOUS value ⭐
}

export function useLocalStorage(key, initial) {
  const [value, setValue] = useState(() => {          // lazy init: read storage once ⭐
    try { return JSON.parse(localStorage.getItem(key)) ?? initial; }
    catch { return initial; }
  });
  useEffect(() => { localStorage.setItem(key, JSON.stringify(value)); }, [key, value]);
  return [value, setValue];
}

export function useFetch(url) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  useEffect(() => {
    const controller = new AbortController();
    setState({ data: null, loading: true, error: null });
    fetch(url, { signal: controller.signal })
      .then(r => r.json())
      .then(data => setState({ data, loading: false, error: null }))
      .catch(error => { if (error.name !== 'AbortError') setState({ data: null, loading: false, error }); });
    return () => controller.abort();
  }, [url]);
  return state;    // NOTE: two components calling useFetch have INDEPENDENT state ⭐
}

// Search box tying it together:
export function SearchBox() {
  const [text, setText] = useState('');
  const query = useDebounce(text, 400);
  const { data, loading } = useFetch(`/api/search?q=${encodeURIComponent(query)}`);
  return (
    <div>
      <input value={text} onChange={e => setText(e.target.value)} placeholder="search…" />
      {loading ? 'searching…' : JSON.stringify(data)}
    </div>
  );
}
