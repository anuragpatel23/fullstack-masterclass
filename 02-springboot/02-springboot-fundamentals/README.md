# 02 — Spring Boot Fundamentals 🟢⭐

## Real-life analogy
Spring (classic) is **buying land and building a house**: possible, but you choose every brick (XML config, server setup, dependency versions). Spring Boot is a **fully furnished serviced apartment**: opinionated defaults (auto-configuration), utilities pre-connected (embedded server), a fixed rent covering everything (starters), and you can still replace any furniture you don't like (override any bean/property).

## What Spring Boot actually adds ⭐
1. **Auto-configuration** — beans configured based on what's on the classpath.
2. **Starters** — curated, version-aligned dependency bundles (`spring-boot-starter-web`, `-data-jpa`, `-security`, `-test`, `-actuator`).
3. **Embedded server** — Tomcat/Jetty/Netty inside the jar (`java -jar app.jar`); no WAR deploys.
4. **Production goodies** — Actuator, metrics, externalized config, profiles.

## `@SpringBootApplication` ⭐ = three annotations
```java
@SpringBootConfiguration  // it's a @Configuration class
@EnableAutoConfiguration  // trigger the auto-config machinery
@ComponentScan            // scan THIS package and below (why package layout matters!)
```
⚠️ Classes outside the main class's package are **not scanned** — the #1 "my bean isn't found" bug.

## How auto-configuration works ⭐⭐ (the differentiator question)
1. `@EnableAutoConfiguration` imports `AutoConfigurationImportSelector`.
2. It reads candidate config classes from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (was `spring.factories` pre-2.7).
3. Each candidate is gated by **conditional annotations**:
   - `@ConditionalOnClass(DataSource.class)` — only if jar present
   - `@ConditionalOnMissingBean` — **only if YOU haven't defined one** ← how overriding works
   - `@ConditionalOnProperty`, `@ConditionalOnWebApplication`...
4. Result: add H2 to classpath → DataSource appears. Define your own `DataSource` bean → Boot backs off.

Debug it: `--debug` flag prints the CONDITIONS EVALUATION REPORT (matched/unmatched). Exclude: `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)`.

## Externalized configuration ⭐
Precedence (later wins... highest first at runtime): command-line args > env vars > `application-{profile}.properties/yml` > `application.properties` > defaults.
- **Profiles**: `application-dev.yml`, activate via `spring.profiles.active=dev`; `@Profile("dev")` on beans.
- **`@ConfigurationProperties(prefix="app")`** — type-safe config objects (preferred) vs `@Value("${app.x}")` (one-offs, SpEL).
- Relaxed binding: `app.max-size` == `APP_MAXSIZE` == `app.maxSize`.

## Startup flow (know the story)
`SpringApplication.run()` → create environment → print banner → create ApplicationContext → run `ApplicationContextInitializer`s → load bean definitions (scan + auto-config) → refresh context (instantiate singletons) → start embedded server → run `CommandLineRunner`/`ApplicationRunner` → app ready.

## Other essentials
- **DevTools**: auto-restart on classpath changes.
- Fat jar: nested-jar layout + `JarLauncher` — why `java -jar` just works.
- Boot 3.x: Jakarta EE 9+ namespace (`jakarta.*` not `javax.*` ⭐), Java 17 baseline, GraalVM native image support.

## Top interview questions
1. **What problems does Boot solve over Spring?** Config hell, dependency version conflicts, server deployment, prod-readiness.
2. **Explain auto-configuration internals.** (4-step story + conditional annotations. The imports file + `@ConditionalOnMissingBean` = senior answer.)
3. **How do you override an auto-configured bean?** Just declare your own — `@ConditionalOnMissingBean` backs off. Or exclude the auto-config class.
4. **`@SpringBootApplication` is made of...?** (3 annotations + component-scan root implication.)
5. **application.properties vs yml? Profile precedence?** Same capability; profile-specific file beats default; command line beats all.
6. **`@Value` vs `@ConfigurationProperties`?** One-off + SpEL vs type-safe, validated (`@Validated`), IDE-completed groups.
7. **Can you change embedded Tomcat to Jetty?** Exclude tomcat starter, add jetty starter (starters = swappable furniture).
8. **CommandLineRunner vs ApplicationRunner?** Both run after startup; raw `String... args` vs parsed `ApplicationArguments`.

➡️ Code: [`BootFundamentalsDemo.java`](./BootFundamentalsDemo.java)
