/**
 * TOPIC: React fundamentals — JSX, rendering, keys (incl. the index-as-key bug),
 * conditional rendering traps. Paste into a Vite/CRA app or read as reference.
 */
import { useState } from 'react';

// ===== 1. UI = f(state): the smallest complete component =====
export default function App() {
  return (
    <>
      {/* Fragments: one root without extra divs */}
      <Greeting name="Shilpak" />
      <ConditionalTraps unread={0} />
      <KeyBugDemo />
    </>
  );
}

// ===== 2. JSX is createElement calls =====
function Greeting({ name }) {
  // This JSX:   <h1 className="title">Hi {name}</h1>
  // compiles to: React.createElement('h1', { className: 'title' }, 'Hi ', name)
  // -> a plain JS object (virtual DOM node). React diffs these objects.
  const style = { color: 'teal', fontWeight: 'bold' };   // style is an OBJECT, not a string
  return <h1 className="title" style={style}>Hi {name}</h1>;
}

// ===== 3. Conditional rendering + the `0 &&` trap ⭐ =====
function ConditionalTraps({ unread }) {
  return (
    <div>
      {/* ✔ ternary */}
      {unread > 0 ? <b>{unread} unread</b> : <i>inbox zero</i>}

      {/* ⚠️ TRAP: when unread === 0 this renders a literal "0" on screen! */}
      {unread && <b>{unread} unread (buggy version)</b>}

      {/* ✔ fix: force a boolean */}
      {unread > 0 && <b>{unread} unread (fixed)</b>}
    </div>
  );
}

// ===== 4. THE key demo ⭐⭐: index-as-key corrupts row state on insert =====
function KeyBugDemo() {
  const [users, setUsers] = useState([
    { id: 'u1', name: 'Asha' },
    { id: 'u2', name: 'Ravi' },
  ]);

  const addToTop = () =>
    setUsers([{ id: 'u' + Date.now(), name: 'NEW USER' }, ...users]);

  return (
    <div style={{ display: 'flex', gap: 24 }}>
      <div>
        <h3>❌ key=index</h3>
        {/* Type in the inputs, then click add: the TEXT STAYS on row 0
            because React matches old row 0 to new row 0 by key=0.
            The uncontrolled input's DOM state belongs to the KEY, not the data. */}
        {users.map((u, index) => (
          <div key={index}>
            {u.name}: <input placeholder="type here, then add" />
          </div>
        ))}
      </div>

      <div>
        <h3>✔ key=id</h3>
        {/* Stable identity: React moves the DOM nodes WITH their data. */}
        {users.map(u => (
          <div key={u.id}>
            {u.name}: <input placeholder="type here, then add" />
          </div>
        ))}
      </div>

      <button onClick={addToTop}>Add user to top</button>
    </div>
  );
}

/*
 * WHITEBOARD NOTES:
 *
 * RENDER PIPELINE:
 *   setState -> schedule re-render -> call component fn -> new element tree
 *   -> DIFF vs previous tree (reconciliation):
 *        different type?  replace subtree
 *        same type?       patch changed props
 *        list?            match children BY KEY  ⭐
 *   -> COMMIT minimal DOM ops -> browser paints
 *
 * REACT 18 AUTOMATIC BATCHING:
 *   const onClick = async () => {
 *     setA(1); setB(2);              // 18-: two renders in promises/timeouts; 18+: ONE
 *     await fetch(...);
 *     setC(3); setD(4);              // also batched in 18+
 *   };
 *
 * STRICTMODE (dev only): components render twice, effects mount->unmount->mount
 * once — intentionally, to expose impure renders and missing effect cleanups.
 */
