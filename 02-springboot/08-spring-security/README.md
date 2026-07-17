# 08 — Spring Security 🔴⭐

## Real-life analogy
A **corporate office building**: the reception desk checks WHO you are via ID (**authentication**), your badge color decides WHICH floors you may enter (**authorization**). The building has a fixed corridor of checkpoints everyone passes — metal detector, badge scan, visitor log (**the filter chain**). A JWT is a **tamper-proof wristband from a concert**: issued once at entry, verified by signature at every gate — no need to call the ticket office (DB/session) each time. That's what makes it stateless.

## Authentication vs Authorization ⭐
- **AuthN**: who are you? (credentials → `Authentication` object) → 401 when missing/bad.
- **AuthZ**: what may you do? (authorities/roles checked) → 403 when insufficient.

## Architecture ⭐⭐ (draw this)
```
Request → Servlet Filter Chain → [SecurityFilterChain]
  → UsernamePasswordAuthenticationFilter / BearerTokenAuthenticationFilter / your JWT filter
  → AuthenticationManager (ProviderManager)
      → AuthenticationProvider (e.g., DaoAuthenticationProvider)
          → UserDetailsService.loadUserByUsername()  ← you implement this
          → PasswordEncoder.matches()                ← BCrypt
  → success: SecurityContextHolder.setAuthentication(auth)   (ThreadLocal!)
  → FilterSecurityInterceptor / AuthorizationFilter: check rules → 403 or proceed
```
Key names to drop: `SecurityFilterChain`, `SecurityContextHolder` (ThreadLocal ⭐), `UserDetailsService`, `AuthenticationProvider`, `GrantedAuthority`.

## Session vs JWT ⭐
| | Session | JWT |
|---|---|---|
| State | server-side (or shared store) | **stateless** — claims in the token |
| Scale | sticky sessions / Redis | any node validates via signature |
| Revocation | instant (kill session) | hard ⭐ — short TTL + refresh tokens / blocklist |
| Storage (browser) | HttpOnly cookie | localStorage (XSS risk ⭐) vs HttpOnly cookie (CSRF, needs protection) |

JWT = `header.payload.signature` (Base64Url). Signature (HMAC/RSA) prevents tampering; payload is **readable** — never put secrets in it. Validate: signature, `exp`, `iss`, `aud`.

## Must-know configuration (Boot 3 style — no WebSecurityConfigurerAdapter! ⭐)
`SecurityFilterChain` bean + lambda DSL (see code): permit public endpoints, secure the rest, stateless session policy, add JWT filter before `UsernamePasswordAuthenticationFilter`.

## Method security
`@EnableMethodSecurity` → `@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("#userId == authentication.name")` (SpEL ownership check ⭐), `@PostAuthorize`, `@Secured`. Works via AOP proxies → same self-invocation caveat!

## CSRF & CORS ⭐
- **CSRF**: browser auto-sends cookies → attacker site can forge state-changing requests. Defense: synchronizer token. **Disable for stateless token APIs** (no cookie auth) — know WHY you disable it, not just the line of config.
- **CORS**: browser blocks cross-origin JS calls unless server allows via headers; preflight OPTIONS. CORS is not security against servers — only a browser policy.

## Password storage ⭐
BCrypt (adaptive, salted, slow by design — `BCryptPasswordEncoder`), or Argon2. Never MD5/SHA-x alone. `{bcrypt}` prefix = DelegatingPasswordEncoder.

## Top interview questions
1. **AuthN vs AuthZ; 401 vs 403?**
2. **Walk through what happens when a login request hits Spring Security.** (architecture above ⭐)
3. **How does JWT work? Structure? How is it validated? Downsides?** (revocation problem = senior answer)
4. **Where is the authenticated user stored? Thread-safety?** SecurityContextHolder ThreadLocal; cleared per request; @Async needs propagation.
5. **How to secure with roles at method level?** @PreAuthorize + SpEL; mention proxy caveat.
6. **Why disable CSRF for REST APIs? When must you NOT?** No cookie-based auth vs cookie-session apps.
7. **How do you store passwords?** BCrypt + why fast hashes fail (GPU brute force).
8. **Access token vs refresh token flow?** Short-lived access + long-lived refresh, rotation, revocation at refresh.
9. **OAuth2 roles?** Resource owner, client, authorization server, resource server; Authorization Code + PKCE for SPAs ⭐.

➡️ Code: [`SecurityDemo.java`](./SecurityDemo.java)
