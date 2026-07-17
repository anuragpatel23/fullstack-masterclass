# 01 — Spring Core: IoC & Dependency Injection 🟢⭐

## Real-life analogy
**IoC is a restaurant vs cooking at home.** At home you buy ingredients, cook, and plate everything yourself (`new` everywhere = you control construction). In a restaurant, you just declare what you want ("paneer tikka") and the kitchen (**IoC container**) sources ingredients, prepares dependencies, and serves the finished dish to your table (**injection**). "Inversion" = you no longer call the kitchen; the kitchen calls you when it's ready.

## Core concepts

### IoC (Inversion of Control)
The container — not your code — creates, wires, configures, and destroys objects (**beans**). Your classes declare *what* they need; Spring decides *how/when* to provide it. Benefits: loose coupling, testability (inject mocks), centralized config, lifecycle management.

### DI types ⭐
| Type | How | Verdict |
|---|---|---|
| **Constructor** | via constructor params | ✅ **Recommended**: immutable (`final` fields), fails fast, impossible to create half-wired object, obvious when a class has too many deps |
| Setter | via setters | optional/re-settable deps only |
| Field | `@Autowired` on field | ❌ avoid: hides deps, needs reflection, hard to test without container |

Since Spring 4.3: a single constructor needs **no** `@Autowired` at all.

### BeanFactory vs ApplicationContext ⭐
- `BeanFactory`: bare container — lazy bean creation, basic DI.
- `ApplicationContext`: superset — eager singleton creation, events, i18n, AOP integration, `@Configuration` processing, environment/profiles. **Always used in practice.**

### Bean scopes ⭐
`singleton` (default — one per container, NOT per JVM!), `prototype` (new instance per container request), and web scopes: `request`, `session`, `application`, `websocket`.
⚠️ **Prototype-inside-singleton trap**: the prototype is injected ONCE at singleton creation. Fixes: `@Lookup` method, `ObjectProvider<T>`, scoped proxy.

### Bean lifecycle ⭐ (asked constantly)
1. Instantiate (constructor)
2. Populate properties (DI)
3. `Aware` callbacks (BeanNameAware, ApplicationContextAware)
4. `BeanPostProcessor.postProcessBeforeInitialization`
5. `@PostConstruct` → `InitializingBean.afterPropertiesSet()` → `initMethod`
6. `BeanPostProcessor.postProcessAfterInitialization` ← **AOP proxies are created here!**
7. Bean ready → container shutdown → `@PreDestroy` → `DisposableBean.destroy()` (NOT called for prototypes!)

### Circular dependencies ⭐
A→B→A. Field/setter injection was historically resolved via early references (3-level cache); **constructor injection fails fast** (good!). Spring Boot 2.6+ **disallows circular refs by default**. Fixes: redesign (best), `@Lazy` on one side, setter injection, `ObjectProvider`.

### Resolving ambiguity (multiple beans of same type) ⭐
`@Primary` (default winner), `@Qualifier("name")` (explicit pick), inject `List<Interface>` / `Map<String, Interface>` (get ALL — runtime strategy pattern!), `@ConditionalOn...`.

## Top interview questions
1. **Explain IoC and DI to a junior.** (Restaurant analogy + "don't call us, we'll call you".)
2. **Why is constructor injection preferred?** Immutability, fail-fast, testability, design feedback (too many params = SRP violation).
3. **Bean lifecycle in order?** (7 steps — mention where proxies are created for bonus points.)
4. **Singleton bean = singleton pattern?** No — one per *container per bean name*, enforced by Spring, not by the class.
5. **Are singleton beans thread-safe?** NO — Spring adds no safety; keep beans stateless.
6. **Inject a prototype into a singleton correctly?** ObjectProvider / @Lookup / scoped proxy.
7. **How does Spring resolve circular dependencies?** 3-level cache for setter/field; constructor = exception; 2.6+ rejects by default.
8. **@Autowired resolution order?** byType → @Qualifier → byName (field/param name) → NoUniqueBeanDefinitionException.

➡️ Code: [`IocDiDemo.java`](./IocDiDemo.java)
