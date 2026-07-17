/**
 * TOPIC: Boot fundamentals — main class, custom auto-configuration (write your own
 * to PROVE you understand it), type-safe config, profiles, runners.
 */
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.*;

// ===== 1. The entry point — three annotations in one =====
@SpringBootApplication   // = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
@ConfigurationPropertiesScan
public class BootFundamentalsDemo {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BootFundamentalsDemo.class);
        app.setAdditionalProfiles("dev");        // programmatic profile activation
        app.run(args);
    }

    // ===== 2. Type-safe configuration ⭐ (@ConfigurationProperties > @Value) =====
    /*  application.yml:
     *  app:
     *    payment:
     *      provider: stripe
     *      timeout-seconds: 30        # relaxed binding: timeoutSeconds / TIMEOUT_SECONDS all work
     *      retries: 3
     */
    @Validated
    @ConfigurationProperties(prefix = "app.payment")
    record PaymentProps(
            @NotBlank String provider,
            @Min(1) @Max(120) int timeoutSeconds,
            int retries) {}                       // records = immutable config (Boot 2.6+)

    // ===== 3. Profiles: different beans per environment =====
    interface EmailSender { void send(String to, String body); }

    @Configuration
    static class EmailConfig {
        @Bean @Profile("dev")
        EmailSender consoleSender() {             // dev: just log it
            return (to, body) -> System.out.println("DEV email to " + to + ": " + body);
        }
        @Bean @Profile("prod")
        EmailSender smtpSender() {                // prod: the real thing
            return (to, body) -> {/* SMTP call */};
        }
    }

    // ===== 4. Write your OWN auto-configuration (interview gold ⭐) =====
    // In a real lib this class is listed in:
    // META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    static class AuditService {
        void audit(String msg) { System.out.println("AUDIT: " + msg); }
    }

    @AutoConfiguration
    @ConditionalOnProperty(prefix = "app.audit", name = "enabled", havingValue = "true",
                           matchIfMissing = true)       // on by default, property can disable
    static class AuditAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean                        // ⭐ back off if the user defined their own
        AuditService auditService() { return new AuditService(); }
    }
    // Now: any app with this jar gets an AuditService bean automatically —
    // unless they define their own or set app.audit.enabled=false.
    // That's ALL auto-configuration is. DataSourceAutoConfiguration works the same way.

    // ===== 5. Run-after-startup hooks =====
    @Bean
    CommandLineRunner seedData(PaymentProps props) {
        return args -> System.out.println("started with provider=" + props.provider()
                + " timeout=" + props.timeoutSeconds() + "s");
    }

    /*
     * TALKING POINTS:
     * - Package layout: this class's package is the component-scan ROOT.
     * - Override anything: define your own bean of the same type -> auto-config backs off
     *   (because of @ConditionalOnMissingBean, not magic).
     * - Debug why a bean did(n't) appear: run with --debug -> Conditions Evaluation Report.
     * - Boot 3: jakarta.* imports (above), Java 17+, AOT/native support.
     */
}
