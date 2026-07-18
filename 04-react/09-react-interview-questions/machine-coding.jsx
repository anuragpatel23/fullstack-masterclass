/**
 * TOPIC: React machine-coding round — the four most-asked widgets,
 * written the way interviewers want to see them (a11y + edge cases narrated).
 */
import { useState, useEffect, useRef, useMemo } from 'react';
import { createPortal } from 'react-dom';

// =====================================================================
// 1. AUTOCOMPLETE / TYPEAHEAD ⭐⭐ (debounce + race guard + keyboard nav)
// =====================================================================
export function Autocomplete({ fetchSuggestions }) {
  const [text, setText] = useState('');
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const [highlight, setHighlight] = useState(-1);

  useEffect(() => {
    if (!text.trim()) { setItems([]); return; }
    const controller = new AbortController();
    const timer = setTimeout(async () => {              // debounce ⭐
      try {
        const results = await fetchSuggestions(text, controller.signal);
        setItems(results); setOpen(true); setHighlight(-1);
      } catch (e) { if (e.name !== 'AbortError') setItems([]); }
    }, 300);
    return () => { clearTimeout(timer); controller.abort(); };  // cancel stale ⭐
  }, [text, fetchSuggestions]);

  const select = value => { setText(value); setOpen(false); };

  const onKeyDown = e => {                              // keyboard support = bonus points
    if (!open) return;
    if (e.key === 'ArrowDown') setHighlight(h => Math.min(h + 1, items.length - 1));
    else if (e.key === 'ArrowUp') setHighlight(h => Math.max(h - 1, 0));
    else if (e.key === 'Enter' && highlight >= 0) select(items[highlight]);
    else if (e.key === 'Escape') setOpen(false);
  };

  return (
    <div style={{ position: 'relative', width: 250 }}>
      <input value={text} onChange={e => setText(e.target.value)} onKeyDown={onKeyDown}
             placeholder="search cities…" aria-autocomplete="list" aria-expanded={open} />
      {open && items.length > 0 && (
        <ul role="listbox" style={{ position: 'absolute', width: '100%', margin: 0,
                                    padding: 0, listStyle: 'none', border: '1px solid #ccc' }}>
          {items.map((item, i) => (
            <li key={item} role="option" aria-selected={i === highlight}
                onMouseDown={() => select(item)}       // mousedown fires before input blur ⭐
                style={{ padding: 4, background: i === highlight ? '#eef' : 'white' }}>
              {item}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
// usage: <Autocomplete fetchSuggestions={(q, signal) => fetch(`/api/cities?q=${q}`, {signal}).then(r => r.json())} />

// =====================================================================
// 2. MODAL with PORTAL ⭐ (escape key, overlay click, focus, scroll lock)
// =====================================================================
export function Modal({ open, onClose, title, children }) {
  const dialogRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    const onKey = e => e.key === 'Escape' && onClose();
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';            // scroll lock
    dialogRef.current?.focus();
    return () => {
      document.removeEventListener('keydown', onKey);   // cleanup ⭐
      document.body.style.overflow = '';
    };
  }, [open, onClose]);

  if (!open) return null;

  return createPortal(                                   // escapes overflow/z-index traps ⭐
    <div onClick={onClose}                               // overlay click closes
         style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,.5)',
                  display: 'grid', placeItems: 'center' }}>
      <div ref={dialogRef} tabIndex={-1} role="dialog" aria-modal="true" aria-label={title}
           onClick={e => e.stopPropagation()}            // clicks inside don't close ⭐
           style={{ background: 'white', padding: 24, borderRadius: 8, minWidth: 320 }}>
        <h3>{title}</h3>
        {children}
        <button onClick={onClose}>Close</button>
      </div>
    </div>,
    document.body
  );
}

// =====================================================================
// 3. STAR RATING ⭐ (hover preview vs committed value)
// =====================================================================
export function StarRating({ max = 5, value, onChange }) {
  const [hover, setHover] = useState(0);
  const shown = hover || value;                          // preview wins while hovering

  return (
    <div role="radiogroup" aria-label="rating" onMouseLeave={() => setHover(0)}>
      {Array.from({ length: max }, (_, i) => i + 1).map(star => (
        <button key={star} role="radio" aria-checked={value === star}
                onMouseEnter={() => setHover(star)}
                onClick={() => onChange(star === value ? 0 : star)}   // click again clears
                style={{ border: 'none', background: 'none', cursor: 'pointer',
                         fontSize: 24, color: star <= shown ? 'gold' : '#ccc' }}>
          ★
        </button>
      ))}
    </div>
  );
}

// =====================================================================
// 4. PAGINATION ⭐ (derive, don't store, the visible slice)
// =====================================================================
export function PaginatedTable({ rows, pageSize = 10 }) {
  const [page, setPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(rows.length / pageSize));

  // DERIVED data: computed in render — never mirrored into state ⭐
  const visible = useMemo(
    () => rows.slice((page - 1) * pageSize, page * pageSize),
    [rows, page, pageSize]
  );

  // clamp when data shrinks (edge case interviewers poke at)
  useEffect(() => { if (page > totalPages) setPage(totalPages); }, [page, totalPages]);

  return (
    <div>
      <table>
        <tbody>
          {visible.map(r => <tr key={r.id}><td>{r.name}</td></tr>)}
        </tbody>
      </table>
      <div style={{ display: 'flex', gap: 8 }}>
        <button disabled={page === 1} onClick={() => setPage(p => p - 1)}>prev</button>
        <span>page {page} / {totalPages}</span>
        <button disabled={page === totalPages} onClick={() => setPage(p => p + 1)}>next</button>
      </div>
    </div>
  );
}

/*
 * WHAT INTERVIEWERS GRADE IN MACHINE CODING:
 * 1. Working core fast — then edge cases (empty, loading, error, keyboard).
 * 2. Correct state design: derive computed data; colocate; no prop mirroring.
 * 3. Cleanups in every effect (timers, listeners, aborts).
 * 4. Accessibility instincts: roles, aria-*, keyboard paths.
 * 5. Narrate trade-offs while typing — silence is the real failure mode.
 */
