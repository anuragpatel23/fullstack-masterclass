# 03 — Annotations Deep-Dive 🟡⭐ (the complete must-know list)

## Real-life analogy
Annotations are **airport luggage tags**: the tag itself does nothing to your bag, but each handler along the way reads it and acts — "FRAGILE" (wrap in transaction), "PRIORITY" (@Primary), "TRANSFER TO GATE 7" (@RequestMapping routes it). Spring is the airport staff that actually reads the tags.

## 1. Stereotype (bean-defining)
| Annotation | Meaning |
|---|---|
| `@Component` | generic bean (all below are specializations of it ⭐) |
| `@Service` | business logic layer (semantic marker) |
| `@Repository` | DAO layer + **exception translation** (SQLException → DataAccessException ⭐) |
| `@Controller` | MVC controller returning views |
| `@RestController` | `@Controller + @ResponseBody` — returns serialized body ⭐ |
| `@Configuration` | bean-definition class; CGLIB-proxied so `@Bean` methods return singletons ⭐ |

## 2. Injection & wiring
| Annotation | Notes |
|---|---|
| `@Autowired` | byType; optional on single constructor; `required=false` for optional |
| `@Qualifier("name")` | disambiguate multiple beans of a type ⭐ |
| `@Primary` | default candidate when ambiguous |
| `@Value("${prop:default}")` | property/SpEL injection |
| `@Lazy` | create on first use; breaks circular deps |
| `@Lookup` | inject prototype into singleton |
| `@Bean` | method-level bean in `@Configuration` (3rd-party classes) ⭐ |
| `@Scope("prototype")` | bean scope |
| `@DependsOn`, `@Order` | creation ordering / list ordering |

## 3. Configuration & conditionals
`@ComponentScan`, `@PropertySource`, `@Profile`, `@Import`,
`@ConfigurationProperties(prefix=...)` ⭐, `@EnableConfigurationProperties`,
`@ConditionalOnClass / OnMissingBean / OnProperty / OnBean / OnWebApplication` ⭐ (auto-config building blocks).

## 4. Web / REST ⭐
| Annotation | Notes |
|---|---|
| `@RequestMapping` | class/method routing; method-specific: `@GetMapping @PostMapping @PutMapping @PatchMapping @DeleteMapping` |
| `@PathVariable` | `/users/{id}` → method param |
| `@RequestParam` | `?page=2` query params; `required`, `defaultValue` |
| `@RequestBody` | JSON body → object (via HttpMessageConverter/Jackson) ⭐ |
| `@ResponseBody` | return value → body (implied by @RestController) |
| `@RequestHeader`, `@CookieValue` | header/cookie binding |
| `@ResponseStatus` | fixed status for method/exception |
| `@ExceptionHandler` | handle exception in this controller |
| `@ControllerAdvice` / `@RestControllerAdvice` | GLOBAL exception handling ⭐ |
| `@CrossOrigin` | CORS per controller/method |
| `@Valid` / `@Validated` | trigger bean validation ⭐ (`@NotNull @NotBlank @Size @Email @Min @Pattern @Positive @Past`...) |

## 5. Data / JPA ⭐
`@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy=IDENTITY/SEQUENCE/AUTO)`, `@Column`,
`@OneToMany(mappedBy, cascade, orphanRemoval)`, `@ManyToOne(fetch=LAZY)` ⭐, `@ManyToMany`, `@JoinColumn`,
`@Transient` (not persisted), `@Enumerated(STRING)`, `@Embedded/@Embeddable`, `@Version` (optimistic locking ⭐),
`@Query`, `@Modifying`, `@Param`, `@EntityGraph` (N+1 fix ⭐), `@CreatedDate/@LastModifiedDate` + `@EnableJpaAuditing`.

## 6. Transactions, AOP, async, cache
`@Transactional(propagation, isolation, readOnly, rollbackFor)` ⭐⭐ (own topic 06),
`@EnableTransactionManagement`,
`@Aspect @Before @After @AfterReturning @AfterThrowing @Around @Pointcut` (topic 07),
`@EnableAsync + @Async`, `@EnableScheduling + @Scheduled(cron=...)` (topic 10),
`@EnableCaching + @Cacheable @CachePut @CacheEvict` (topic 11).

## 7. Security (topic 08)
`@EnableWebSecurity`, `@EnableMethodSecurity`, `@PreAuthorize("hasRole('ADMIN')")` ⭐, `@PostAuthorize`, `@Secured`, `@AuthenticationPrincipal`.

## 8. Testing (topic 12)
`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `@MockBean` (now `@MockitoBean`), `@Testcontainers`, `@ActiveProfiles`, `@Sql`.

## The questions these generate ⭐
1. **`@Component` vs `@Bean`?** Class-level auto-scan on code you own vs method-level factory for 3rd-party/conditional objects.
2. **`@Component` vs `@Service` vs `@Repository`?** Functionally same bean; semantics + `@Repository` adds exception translation.
3. **`@Controller` vs `@RestController`?** View resolution vs direct body serialization.
4. **`@RequestParam` vs `@PathVariable`?** Query string vs URI template segment (identity → path, filters/pagination → params).
5. **`@Valid` vs `@Validated`?** JSR-380 standard (nested with cascade) vs Spring's (validation groups + method-level on params).
6. **How does `@Autowired` fail and how do you fix ambiguity?** NoUniqueBeanDefinition → @Qualifier/@Primary/Map injection.
7. **Why is `@Configuration` proxied?** So `@Bean` method cross-calls return the same singleton instead of new instances.
8. **What meta-annotation trick does Spring use?** Composed annotations — `@RestController` is literally annotated with `@Controller` + `@ResponseBody`; you can build your own (see code).

➡️ Code: [`AnnotationsShowcase.java`](./AnnotationsShowcase.java)
