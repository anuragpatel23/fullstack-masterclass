# 04 — REST API Development 🟢⭐

## Real-life analogy
A REST API is a **hotel front desk**: standardized requests ("check in", "room service" = HTTP verbs) on identified resources (room 204 = URI), the desk never remembers your last conversation (**stateless** — bring your key card/token every time), and responses use standard signals (green light / "room not found" / "not your room" = status codes).

## REST principles interviewers check
- **Resources, not actions**: `POST /orders` not `POST /createOrder`.
- **Statelessness** ⭐: every request self-contained → horizontal scaling; session state belongs to the client (JWT) or a shared store.
- Uniform interface, cacheability, layered system.
- **Richardson maturity**: L0 RPC → L1 resources → L2 verbs+status codes (where most real APIs live) → L3 HATEOAS.

## HTTP verbs & idempotency ⭐⭐
| Verb | Use | Idempotent? | Safe? |
|---|---|---|---|
| GET | read | ✔ | ✔ |
| POST | create / non-idempotent ops | ✘ ⭐ | ✘ |
| PUT | full replace | ✔ ⭐ | ✘ |
| PATCH | partial update | not guaranteed | ✘ |
| DELETE | remove | ✔ (2nd call → 404, still no new effect) | ✘ |

**PUT vs PATCH vs POST** is a guaranteed question. Idempotency = N identical calls, same end state. For payment APIs: **idempotency keys** on POST.

## Status codes you must use correctly ⭐
`200` OK, `201` Created (+ `Location` header), `204` No Content (delete), `400` malformed/validation, `401` unauthenticated ⭐, `403` unauthorized ⭐, `404` not found, `405` wrong verb, `409` conflict (duplicate/version clash), `422` semantic error, `429` rate-limited, `500` server bug, `503` unavailable.
**401 vs 403** and **400 vs 422** are the classic pairs.

## Spring MVC request flow ⭐ (how a request actually travels)
`Client → Servlet Filters → DispatcherServlet (front controller) → HandlerMapping (find controller) → HandlerAdapter → [Interceptors preHandle] → ArgumentResolvers (@PathVariable/@RequestBody via HttpMessageConverters/Jackson) → controller method → return value → HttpMessageConverter → [Interceptors postHandle/afterCompletion] → response`.
**Filter vs Interceptor** ⭐: servlet-spec, before DispatcherServlet, sees raw request (auth, CORS, logging) vs Spring-managed, knows the target handler (audit, timing).

## Design essentials
- **Validation**: `@Valid @RequestBody` + constraint annotations; errors → 400 via `MethodArgumentNotValidException` in `@RestControllerAdvice` ⭐.
- **Global error shape**: consistent `{code, message, fieldErrors[], traceId}` — use `ProblemDetail` (RFC 7807, Spring 6).
- **Versioning** ⭐: URI (`/v1/...`, most common), header, media-type — know trade-offs.
- **Pagination**: `?page=0&size=20&sort=createdAt,desc` → `Page<T>`; return total counts + links.
- **DTO vs Entity** ⭐: never expose entities (lazy-loading serialization bombs, over-exposure, tight coupling) — map with MapStruct or record DTOs.
- **ResponseEntity**: full control of status/headers/body.
- **CORS**: browser preflight (OPTIONS); fix server-side via `@CrossOrigin`/global config — ties into React module.

## Top interview questions
1. **PUT vs PATCH vs POST?** (table + idempotency reasoning)
2. **Why must REST be stateless? What breaks if not?** Scaling, failover, caching; sticky sessions as the smell.
3. **Walk a request through Spring MVC.** (DispatcherServlet story ⭐)
4. **Filter vs Interceptor vs AOP?** Servlet level / handler level / method level.
5. **How does @RequestBody JSON become an object?** HttpMessageConverters — Jackson's `MappingJackson2HttpMessageConverter` selected via Content-Type.
6. **How do you handle validation errors globally?** `@RestControllerAdvice` + `@ExceptionHandler(MethodArgumentNotValidException)` → 400 + field errors.
7. **How would you version a breaking change?** URI versioning + deprecation window; contract tests.
8. **401 vs 403? 400 vs 422? When 409?** (above)
9. **How do you make POST /payments safe to retry?** Idempotency-Key header + dedupe store.

➡️ Code: [`RestApiDemo.java`](./RestApiDemo.java)
