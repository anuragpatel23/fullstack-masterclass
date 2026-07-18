/**
 * TOPIC: Components/Props/State — batching + functional updates, immutable
 * updates, lifting state up, controlled vs uncontrolled, key-reset trick.
 */
import { useState, useRef } from 'react';

// ===== 1. Batching & stale-closure counter ⭐⭐ =====
export function CounterTraps() {
  const [count, setCount] = useState(0);

  const brokenTriple = () => {
    setCount(count + 1);      // all three read the SAME render-time `count`
    setCount(count + 1);      // batched: last write wins
    setCount(count + 1);      // net effect: +1  ❌
  };

  const correctTriple = () => {
    setCount(c => c + 1);     // functional updater: each gets the LATEST value
    setCount(c => c + 1);
    setCount(c => c + 1);     // net effect: +3  ✔
  };

  return (
    <div>
      <p>count: {count}</p>
      <button onClick={brokenTriple}>+3 (broken: adds 1)</button>
      <button onClick={correctTriple}>+3 (functional: adds 3)</button>
    </div>
  );
}

// ===== 2. Immutable updates ⭐ (nested state without mutation) =====
export function ProfileEditor() {
  const [profile, setProfile] = useState({
    name: 'Shilpak',
    skills: ['java', 'react'],
    address: { city: 'Pune', pin: '411001' },
  });

  // ❌ profile.address.city = 'Mumbai'; setProfile(profile)  -> same ref, NO re-render
  const moveCity = () =>
    setProfile(p => ({ ...p, address: { ...p.address, city: 'Mumbai' } }));  // new refs ✔

  const addSkill = skill =>
    setProfile(p => ({ ...p, skills: [...p.skills, skill] }));    // never push!

  const removeSkill = skill =>
    setProfile(p => ({ ...p, skills: p.skills.filter(s => s !== skill) }));

  return (
    <div>
      <p>{profile.name} — {profile.address.city} — {profile.skills.join(', ')}</p>
      <button onClick={moveCity}>Move to Mumbai</button>
      <button onClick={() => addSkill('sql')}>+sql</button>
      <button onClick={() => removeSkill('java')}>-java</button>
    </div>
  );
}

// ===== 3. Lifting state up ⭐ — siblings talk through the parent =====
function CurrencyInput({ label, amount, onAmountChange }) {   // "dumb": props in, events out
  return (
    <label>
      {label}: <input type="number" value={amount}
                      onChange={e => onAmountChange(Number(e.target.value))} />
    </label>
  );
}

export function CurrencyConverter() {
  const [inr, setInr] = useState(0);              // SINGLE source of truth in the parent
  const RATE = 83;
  return (
    <div>
      {/* Both children render from the same state; either can update it */}
      <CurrencyInput label="INR" amount={inr}            onAmountChange={setInr} />
      <CurrencyInput label="USD" amount={inr / RATE}     onAmountChange={usd => setInr(usd * RATE)} />
    </div>
  );
}

// ===== 4. Controlled vs uncontrolled ⭐⭐ =====
export function TwoForms() {
  // CONTROLLED: React state drives the input — validate/transform every keystroke
  const [email, setEmail] = useState('');
  const valid = /\S+@\S+\.\S+/.test(email);

  // UNCONTROLLED: DOM holds the value; read it when needed via ref
  const nameRef = useRef(null);

  const submit = e => {
    e.preventDefault();
    console.log('controlled email:', email, '| uncontrolled name:', nameRef.current.value);
  };

  return (
    <form onSubmit={submit}>
      <input value={email} onChange={e => setEmail(e.target.value.trim())}
             style={{ borderColor: valid || !email ? 'gray' : 'red' }}
             placeholder="controlled email" />
      <input ref={nameRef} defaultValue="Shilpak" placeholder="uncontrolled name" />
      <button disabled={!valid}>Submit</button>
    </form>
  );
}

// ===== 5. key-reset trick ⭐ — remount to wipe state =====
export function ResettableForm() {
  const [version, setVersion] = useState(0);
  return (
    <div>
      {/* Changing key => React unmounts old TwoForms and mounts a FRESH one
          (all its internal state gone). The idiomatic "reset form" trick. */}
      <TwoForms key={version} />
      <button onClick={() => setVersion(v => v + 1)}>Reset form</button>
    </div>
  );
}

/*
 * TALKING POINTS:
 * - Child -> parent: parent passes a callback prop; child calls it (events up).
 * - Don't copy props into state: <Avatar user={user}> should render from props;
 *   useState(props.user) freezes the FIRST value forever (stale copy anti-pattern).
 * - State updates are async-ish: reading `count` right after setCount gives the old
 *   value — the new one exists only in the NEXT render's closure.
 */
