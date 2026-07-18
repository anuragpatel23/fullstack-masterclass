/**
 * TOPIC: Performance — the memo-team demo (why memo alone fails),
 * children-as-props trick, lazy+Suspense, useTransition, virtualization sketch.
 */
import React, {
  useState, useMemo, useCallback, memo, lazy, Suspense, useTransition,
} from 'react';

// =====================================================================
// 1. THE MEMO-TEAM DEMO ⭐⭐ — memo fails without stable references
// =====================================================================
const TodoList = memo(function TodoList({ todos, onToggle }) {
  console.log('TodoList rendered');                 // watch this log!
  return (
    <ul>
      {todos.map(t => (
        <li key={t.id} onClick={() => onToggle(t.id)}
            style={{ textDecoration: t.done ? 'line-through' : 'none' }}>
          {t.text}
        </li>
      ))}
    </ul>
  );
});

export function TodoPage() {
  const [todos, setTodos] = useState([
    { id: 1, text: 'learn memo', done: false },
    { id: 2, text: 'crack interview', done: false },
  ]);
  const [theme, setTheme] = useState('light');      // UNRELATED state

  // ❌ Without useCallback: new function identity every render
  //    -> memo's shallow compare sees a "changed" prop -> TodoList re-renders
  //    even when only `theme` changed. memo was USELESS.
  const onToggle = useCallback(
    id => setTodos(ts => ts.map(t => (t.id === id ? { ...t, done: !t.done } : t))),
    []                                              // functional update -> no deps needed ⭐
  );

  // Same story for derived arrays/objects: stabilize with useMemo
  const pending = useMemo(() => todos.filter(t => !t.done), [todos]);

  return (
    <div className={theme}>
      <button onClick={() => setTheme(t => (t === 'light' ? 'dark' : 'light'))}>
        toggle theme (TodoList should NOT re-render)
      </button>
      <p>{pending.length} pending</p>
      <TodoList todos={todos} onToggle={onToggle} />
    </div>
  );
}

// =====================================================================
// 2. children-as-props: free optimization without memo ⭐
// =====================================================================
function BlinkingWrapper({ children }) {            // has its own churning state
  const [tick, setTick] = useState(0);
  React.useEffect(() => {
    const id = setInterval(() => setTick(t => t + 1), 1000);
    return () => clearInterval(id);
  }, []);
  // `children` was created by the PARENT's render — same element reference here,
  // so React BAILS OUT of re-rendering it even though Wrapper renders every second ⭐
  return <div data-tick={tick}>{children}</div>;
}
export function Page() {
  return (
    <BlinkingWrapper>
      <ExpensiveTree />   {/* does NOT re-render on wrapper ticks */}
    </BlinkingWrapper>
  );
}
function ExpensiveTree() {
  console.log('ExpensiveTree rendered');            // logs once
  return <p>expensive subtree</p>;
}

// =====================================================================
// 3. Code splitting: lazy + Suspense ⭐
// =====================================================================
const AdminDashboard = lazy(() => import('./AdminDashboard'));  // separate chunk

export function AppRoutes({ isAdmin }) {
  return (
    <Suspense fallback={<p>loading module…</p>}>
      {isAdmin && <AdminDashboard />}               {/* chunk downloads on first need */}
    </Suspense>
  );
}

// =====================================================================
// 4. useTransition ⭐ — keep typing snappy while a heavy list renders
// =====================================================================
export function FilterHugeList({ items }) {
  const [text, setText] = useState('');
  const [query, setQuery] = useState('');
  const [isPending, startTransition] = useTransition();

  const onChange = e => {
    setText(e.target.value);                        // URGENT: input echoes instantly
    startTransition(() => setQuery(e.target.value)); // NON-URGENT: filtering can lag
  };

  const filtered = useMemo(
    () => items.filter(i => i.name.includes(query)),
    [items, query]
  );

  return (
    <div>
      <input value={text} onChange={onChange} placeholder="filter 10k items…" />
      {isPending && <span> updating…</span>}
      <ul>{filtered.slice(0, 100).map(i => <li key={i.id}>{i.name}</li>)}</ul>
    </div>
  );
}

// =====================================================================
// 5. Virtualization sketch ⭐ — render only the visible window
// =====================================================================
export function VirtualList({ items, rowHeight = 30, viewHeight = 300 }) {
  const [scrollTop, setScrollTop] = useState(0);

  const start = Math.floor(scrollTop / rowHeight);
  const visibleCount = Math.ceil(viewHeight / rowHeight) + 1;
  const visible = items.slice(start, start + visibleCount);      // ~11 rows, not 10,000

  return (
    <div style={{ height: viewHeight, overflow: 'auto' }}
         onScroll={e => setScrollTop(e.currentTarget.scrollTop)}>
      <div style={{ height: items.length * rowHeight, position: 'relative' }}>
        {visible.map((item, i) => (
          <div key={item.id}
               style={{ position: 'absolute', top: (start + i) * rowHeight, height: rowHeight }}>
            {item.name}
          </div>
        ))}
      </div>
    </div>
  );
}
// Production: react-window / @tanstack/react-virtual — but writing this sketch
// in an interview demonstrates you understand WHAT they do.
