# 12 — Testing Spring Boot 🟡⭐

## Real-life analogy
Testing layers are **car quality checks**: testing a spark plug on a bench = **unit test** (fast, isolated, thousands per day). Testing the assembled engine on a rig = **slice test** (`@WebMvcTest` — real carburetor, fake fuel tank). A full test-drive = **integration test** (`@SpringBootTest` — everything real, slow, few). Crash-testing with a dummy = **mocking** (controlled stand-in to verify interactions).

## The pyramid ⭐
Many fast unit tests → some slice tests → few full integration tests → fewer E2E. Inverting it = slow, flaky CI.

## Unit tests (no Spring context — the fast majority)
JUnit 5 + Mockito. Constructor injection makes this trivial ⭐ (the payoff of the DI lesson):
- `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`.
- `when(repo.findById(1L)).thenReturn(...)` — stub. `verify(repo).save(any())` — interaction check.
- `@Mock` vs `@Spy` (full fake vs partial real), `ArgumentCaptor` (inspect what was passed ⭐), `when..thenThrow`.
- Mock roles: dummy/stub/mock — mock only what you own/what's slow (DB, HTTP, clock).

## Slice tests ⭐ (load a SLICE of the context)
| Annotation | Loads | Use with |
|---|---|---|
| `@WebMvcTest(OrderController.class)` | MVC layer only (controllers, advice, filters) | `MockMvc` + `@MockBean` services ⭐ |
| `@DataJpaTest` | repositories + in-memory DB + tx (rolls back per test ⭐) | `TestEntityManager` |
| `@JsonTest` | Jackson layer | serialization contracts |
| `@RestClientTest` | your REST client | `MockRestServiceServer` |

`@MockBean` (Boot 3.4+: `@MockitoBean`) replaces a bean IN the context with a Mockito mock — the bridge between Spring and Mockito ⭐. (Caveat: each unique @MockBean combo = new context = slower CI.)

## Integration tests
- `@SpringBootTest` — full context; `webEnvironment = RANDOM_PORT` + `TestRestTemplate`/`WebTestClient` for real HTTP.
- **Testcontainers** ⭐ (the modern answer): real Oracle/Postgres/Kafka/Redis in Docker per test run — kills "works on H2, fails on Oracle" bugs. `@ServiceConnection` (Boot 3.1+) auto-wires the container's URL.
- `@Transactional` on tests: auto-rollback keeps DB clean (know that it can hide flush bugs), `@Sql` scripts for fixtures, `@ActiveProfiles("test")`.

## What to test at each layer (say this)
Controller: status codes, validation errors, JSON shape, security rules (`@WithMockUser` ⭐). Service: business rules with mocked repos. Repository: only custom queries (derived ones are Spring's problem). Integration: one happy path per critical flow + the wiring.

## Top interview questions
1. **@Mock vs @MockBean?** Plain Mockito object (no Spring) vs replacement inside the Spring context.
2. **@WebMvcTest vs @SpringBootTest?** Slice speed vs full-wiring confidence; what each loads.
3. **How do you test a controller without starting a server?** MockMvc — servlet-layer simulation (see code).
4. **Why Testcontainers over H2?** Dialect differences (Oracle SQL, sequences, locking) — H2 green ≠ prod green ⭐.
5. **How do you test @Transactional rollback behavior?** Integration test asserting state after a thrown exception.
6. **How do you test secured endpoints?** `@WithMockUser(roles="ADMIN")` / SecurityMockMvcRequestPostProcessors.jwt().
7. **Verify a method was called with specific args?** `verify` + `ArgumentCaptor`.
8. **Why did adding a test slow the whole suite?** New context cache key (different @MockBeans/properties) → context restart.

➡️ Code: [`TestingDemo.java`](./TestingDemo.java)
