/**
 * TOPIC: HOC, render props, composition, compound components —
 * the same "mouse position" logic in all three styles, plus withAuth and Tabs.
 */
import { useState, useEffect, useContext, createContext, useMemo } from 'react';

// =====================================================================
// 1. HOC ⭐ — function in, enhanced component out
// =====================================================================
export function withLoading(Component) {
  function WithLoading({ isLoading, ...props }) {     // intercept one prop...
    if (isLoading) return <p>loading…</p>;
    return <Component {...props} />;                  // ...pass the rest through ⭐
  }
  WithLoading.displayName = `withLoading(${Component.displayName || Component.name})`;
  return WithLoading;
}

export function withAuth(Component, requiredRole) {
  return function WithAuth(props) {
    const { user } = useAuth();                       // hooks work inside HOCs
    if (!user) return <a href="/login">Please log in</a>;
    if (requiredRole && user.role !== requiredRole) return <p>403 — not allowed</p>;
    return <Component {...props} user={user} />;      // inject a prop
  };
}

// Usage — note: applied ONCE at module level, never inside render ⭐
function AdminPanelBase({ user }) { return <h2>Welcome, {user.name}</h2>; }
export const AdminPanel = withAuth(AdminPanelBase, 'ADMIN');

// =====================================================================
// 2. The SAME logic three ways: mouse tracking
// =====================================================================

// (a) Render prop version — component owns logic, YOU own rendering
export function MouseTracker({ render }) {
  const [pos, setPos] = useState({ x: 0, y: 0 });
  useEffect(() => {
    const onMove = e => setPos({ x: e.clientX, y: e.clientY });
    window.addEventListener('mousemove', onMove);
    return () => window.removeEventListener('mousemove', onMove);
  }, []);
  return render(pos);                                 // <- the "render prop"
}
// <MouseTracker render={({x, y}) => <p>{x},{y}</p>} />

// (b) HOC version
export function withMouse(Component) {
  return function WithMouse(props) {
    return <MouseTracker render={pos => <Component {...props} mouse={pos} />} />;
  };
}

// (c) Custom hook version ⭐ — what you'd write TODAY
export function useMouse() {
  const [pos, setPos] = useState({ x: 0, y: 0 });
  useEffect(() => {
    const onMove = e => setPos({ x: e.clientX, y: e.clientY });
    window.addEventListener('mousemove', onMove);
    return () => window.removeEventListener('mousemove', onMove);
  }, []);
  return pos;
}
export function Crosshair() {
  const { x, y } = useMouse();                        // no wrappers, no collisions ⭐
  return <p>cursor at {x}, {y}</p>;
}

// =====================================================================
// 3. Composition: slots + avoiding prop drilling ⭐
// =====================================================================
export function Layout({ sidebar, children }) {       // named slot + default slot
  return (
    <div style={{ display: 'flex' }}>
      <aside style={{ width: 200 }}>{sidebar}</aside>
      <main>{children}</main>
    </div>
  );
}
// Prop-drilling fix: the TOP component composes the user-aware piece directly —
// Layout never learns about `user`:
//   <Layout sidebar={<Nav />}><Profile user={user} /></Layout>

// =====================================================================
// 4. Compound components (Tabs) ⭐ — implicit shared state via context
// =====================================================================
const TabsContext = createContext(null);

export function Tabs({ defaultTab, children }) {
  const [active, setActive] = useState(defaultTab);
  const value = useMemo(() => ({ active, setActive }), [active]);
  return <TabsContext.Provider value={value}>{children}</TabsContext.Provider>;
}

Tabs.List = function TabList({ children }) {
  return <div role="tablist" style={{ display: 'flex', gap: 8 }}>{children}</div>;
};

Tabs.Tab = function Tab({ id, children }) {
  const { active, setActive } = useContext(TabsContext);
  return (
    <button role="tab" aria-selected={active === id}
            style={{ fontWeight: active === id ? 'bold' : 'normal' }}
            onClick={() => setActive(id)}>
      {children}
    </button>
  );
};

Tabs.Panel = function TabPanel({ id, children }) {
  const { active } = useContext(TabsContext);
  return active === id ? <div role="tabpanel">{children}</div> : null;
};

// Usage reads like HTML — pieces coordinate invisibly through context:
export function ProfilePage() {
  return (
    <Tabs defaultTab="posts">
      <Tabs.List>
        <Tabs.Tab id="posts">Posts</Tabs.Tab>
        <Tabs.Tab id="about">About</Tabs.Tab>
      </Tabs.List>
      <Tabs.Panel id="posts">…post list…</Tabs.Panel>
      <Tabs.Panel id="about">…bio…</Tabs.Panel>
    </Tabs>
  );
}

// fake auth hook for the demo
function useAuth() { return { user: { name: 'Shilpak', role: 'ADMIN' } }; }
