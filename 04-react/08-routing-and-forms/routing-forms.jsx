/**
 * TOPIC: Routing & Forms — full app shape: nested routes, protected routes,
 * params/search/navigation, and a production-grade controlled form.
 */
import { useState } from 'react';
import {
  BrowserRouter, Routes, Route, Link, NavLink, Outlet, Navigate,
  useParams, useNavigate, useSearchParams, useLocation,
} from 'react-router-dom';

// =====================================================================
// 1. App routing table — nested + protected + lazy-ready + 404
// =====================================================================
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>              {/* layout route */}
          <Route index element={<Home />} />               {/* default child */}
          <Route path="products" element={<ProductList />} />
          <Route path="products/:productId" element={<ProductDetail />} />
          <Route path="login" element={<Login />} />

          <Route element={<RequireAuth />}>                {/* guard wraps children ⭐ */}
            <Route path="account" element={<Account />} />
            <Route path="admin" element={<Admin />} />
          </Route>

          <Route path="*" element={<h2>404 — nothing here</h2>} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

function Layout() {
  return (
    <div>
      <nav style={{ display: 'flex', gap: 12 }}>
        {/* NavLink knows when it's active */}
        <NavLink to="/" style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}>
          Home
        </NavLink>
        <NavLink to="/products">Products</NavLink>
        <NavLink to="/account">Account</NavLink>
      </nav>
      <Outlet />                                           {/* children render HERE ⭐ */}
    </div>
  );
}

// =====================================================================
// 2. Protected route ⭐ — redirect to login, come back after
// =====================================================================
function RequireAuth() {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) {
    // remember where they were going ⭐
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return <Outlet />;                                       // authorized: render children
}

function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname ?? '/';      // return destination

  return (
    <button onClick={() => { login('shilpak'); navigate(from, { replace: true }); }}>
      Log in & continue to {from}
    </button>
  );
}

// =====================================================================
// 3. Params + search params ⭐
// =====================================================================
function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();     // ?category=books&page=2
  const category = searchParams.get('category') ?? 'all';
  const page = Number(searchParams.get('page') ?? 1);

  return (
    <div>
      <h2>{category} — page {page}</h2>
      <button onClick={() => setSearchParams({ category, page: page + 1 })}>next page</button>
      <Link to="/products/42">Product 42</Link>            {/* never <a href> ⭐ */}
    </div>
  );
}

function ProductDetail() {
  const { productId } = useParams();                       // ":productId" segment ⭐
  const navigate = useNavigate();
  return (
    <div>
      <h2>Product {productId}</h2>
      <button onClick={() => navigate(-1)}>back</button>
    </div>
  );
}

// =====================================================================
// 4. Production-shaped controlled form ⭐ — one handler, layered validation
// =====================================================================
export function SignupForm() {
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  // ONE handler for all fields: name attribute + computed key (ES6!) ⭐
  const onChange = e =>
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const validate = f => {
    const errs = {};
    if (!f.name.trim()) errs.name = 'name is required';
    if (!/\S+@\S+\.\S+/.test(f.email)) errs.email = 'invalid email';
    if (f.password.length < 8) errs.password = 'min 8 characters';
    return errs;
  };

  const onSubmit = async e => {
    e.preventDefault();                                    // stop the full-page POST ⭐
    const errs = validate(form);
    setErrors(errs);
    if (Object.keys(errs).length) return;

    setSubmitting(true);                                   // disable double-submit
    try {
      const res = await fetch('/api/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      if (!res.ok) {
        const body = await res.json();                     // map SERVER field errors back ⭐
        setErrors(body.fieldErrors ?? { form: 'signup failed' });
        return;
      }
      // navigate('/welcome')
    } finally {
      setSubmitting(false);
    }
  };

  const field = (name, type = 'text') => (
    <label style={{ display: 'block' }}>
      {name}:
      <input name={name} type={type} value={form[name]} onChange={onChange} />
      {errors[name] && <span style={{ color: 'red' }}> {errors[name]}</span>}
    </label>
  );

  return (
    <form onSubmit={onSubmit} noValidate>
      {field('name')} {field('email', 'email')} {field('password', 'password')}
      {errors.form && <p style={{ color: 'red' }}>{errors.form}</p>}
      <button disabled={submitting}>{submitting ? 'creating…' : 'Sign up'}</button>
    </form>
  );
}
// NOTE for interviews: huge forms -> React Hook Form (uncontrolled + refs =
// re-render per SUBMIT not per keystroke) + Zod schema; server must re-validate.

// --- fake auth store for the demo ---
let fakeUser = null;
function useAuth() {
  const [, force] = useState(0);
  return {
    user: fakeUser,
    login: name => { fakeUser = { name }; force(x => x + 1); },
    logout: () => { fakeUser = null; force(x => x + 1); },
  };
}
function Home()    { return <h2>Home</h2>; }
function Account() { return <h2>My account (protected)</h2>; }
function Admin()   { return <h2>Admin (protected)</h2>; }
