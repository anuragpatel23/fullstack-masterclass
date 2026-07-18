/**
 * TOPIC: State management — Context done right, Redux Toolkit slice,
 * Zustand store, React Query patterns (reference shapes).
 */
import { createContext, useContext, useMemo, useReducer } from 'react';

// =====================================================================
// 1. CONTEXT done right ⭐ — split state/dispatch, memoized values
// =====================================================================
const AuthStateContext = createContext(null);
const AuthDispatchContext = createContext(null);      // split: dispatch never changes ⭐

function authReducer(state, action) {
  switch (action.type) {
    case 'login':  return { user: action.user };
    case 'logout': return { user: null };
    default:       return state;
  }
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, { user: null });
  const stateValue = useMemo(() => state, [state]);
  return (
    <AuthStateContext.Provider value={stateValue}>
      <AuthDispatchContext.Provider value={dispatch}>
        {children}
      </AuthDispatchContext.Provider>
    </AuthStateContext.Provider>
  );
}
export const useAuthState = () => useContext(AuthStateContext);
export const useAuthDispatch = () => useContext(AuthDispatchContext);
// A button that only DISPATCHES (LogoutButton) subscribes to dispatch-context only
// -> it does NOT re-render when the user object changes ⭐

// =====================================================================
// 2. REDUX TOOLKIT — the modern slice shape ⭐
// =====================================================================
/*
import { configureStore, createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { useSelector, useDispatch, Provider } from 'react-redux';

// Async thunk: pending/fulfilled/rejected actions auto-generated
export const fetchCart = createAsyncThunk('cart/fetch', async (userId) => {
  const res = await fetch(`/api/cart/${userId}`);
  return res.json();
});

const cartSlice = createSlice({
  name: 'cart',
  initialState: { items: [], status: 'idle' },
  reducers: {
    // Looks like mutation — Immer makes it an immutable update underneath ⭐
    itemAdded(state, action)   { state.items.push(action.payload); },
    itemRemoved(state, action) { state.items = state.items.filter(i => i.id !== action.payload); },
  },
  extraReducers: (builder) => builder
    .addCase(fetchCart.pending,   (state)         => { state.status = 'loading'; })
    .addCase(fetchCart.fulfilled, (state, action) => { state.status = 'idle'; state.items = action.payload; })
    .addCase(fetchCart.rejected,  (state)         => { state.status = 'error'; }),
});

export const { itemAdded, itemRemoved } = cartSlice.actions;
export const store = configureStore({ reducer: { cart: cartSlice.reducer } });

function CartBadge() {
  // NARROW selector ⭐: subscribe to the count, not the whole slice —
  // re-renders only when count actually changes.
  const count = useSelector(state => state.cart.items.length);
  const dispatch = useDispatch();
  return <button onClick={() => dispatch(itemAdded({ id: 1 }))}>cart ({count})</button>;
}
// <Provider store={store}><App/></Provider>
*/

// =====================================================================
// 3. ZUSTAND — same feature, ~10 lines ⭐
// =====================================================================
/*
import { create } from 'zustand';

const useCartStore = create((set) => ({
  items: [],
  add:    (item) => set(s => ({ items: [...s.items, item] })),
  remove: (id)   => set(s => ({ items: s.items.filter(i => i.id !== id) })),
  clear:  ()     => set({ items: [] }),
}));

function CartBadgeZ() {
  const count = useCartStore(s => s.items.length);   // selector subscription, no Provider
  const add = useCartStore(s => s.add);
  return <button onClick={() => add({ id: Date.now() })}>cart ({count})</button>;
}
*/

// =====================================================================
// 4. REACT QUERY — server state ⭐ (kills useEffect-fetch boilerplate)
// =====================================================================
/*
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

function ProductList({ category }) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['products', category],        // cache key — refetch when category changes
    queryFn: () => fetch(`/api/products?cat=${category}`).then(r => r.json()),
    staleTime: 60_000,                       // fresh for 1 min: no refetch spam
  });
  if (isLoading) return <p>loading…</p>;
  if (error) return <p>error!</p>;
  return <ul>{data.map(p => <li key={p.id}>{p.name}</li>)}</ul>;
}

function AddProduct() {
  const qc = useQueryClient();
  const mutation = useMutation({
    mutationFn: (p) => fetch('/api/products', { method: 'POST', body: JSON.stringify(p) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['products'] }),  // refresh lists ⭐
  });
  return <button onClick={() => mutation.mutate({ name: 'New' })}>add</button>;
}
*/

/*
 * INTERVIEW SUMMARY — "where does this state live?"
 *   search input text            -> useState (local)
 *   theme / locale / auth user   -> Context (low churn)
 *   products, orders, profile    -> React Query (server cache!)
 *   cart, wizard steps, filters  -> RTK slice or Zustand (global client state)
 *
 * REDUX FLOW (recite): dispatch(action) -> middleware -> reducers produce NEW state
 * -> store notifies subscribers -> narrow selectors decide who re-renders.
 */
