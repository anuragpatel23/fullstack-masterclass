/**
 * TOPIC: IoC & DI — constructor injection, scopes, lifecycle hooks, qualifiers,
 * the prototype-in-singleton fix, and strategy-pattern injection.
 * (Reference code — needs spring-boot-starter to compile.)
 */
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;

public class IocDiDemo {

    // ===== 1. Constructor injection (the right way) =====
    interface PaymentGateway { String charge(double amount); }

    @Component("razorpay")
    class RazorpayGateway implements PaymentGateway {
        public String charge(double amt) { return "razorpay charged " + amt; }
    }

    @Component("stripe")
    @Primary                                     // default winner when type is ambiguous
    class StripeGateway implements PaymentGateway {
        public String charge(double amt) { return "stripe charged " + amt; }
    }

    @Service
    class CheckoutService {
        private final PaymentGateway gateway;            // final = immutable, test-friendly

        // Single constructor -> @Autowired optional (Spring 4.3+)
        CheckoutService(@Qualifier("razorpay") PaymentGateway gateway) {  // explicit pick beats @Primary
            this.gateway = gateway;
        }
        String checkout(double amt) { return gateway.charge(amt); }
    }

    // ===== 2. Inject ALL implementations — runtime strategy pattern ⭐ =====
    @Service
    class PaymentRouter {
        private final Map<String, PaymentGateway> gateways;   // key = bean name!

        PaymentRouter(Map<String, PaymentGateway> gateways) { this.gateways = gateways; }

        String route(String provider, double amt) {
            PaymentGateway g = gateways.get(provider);
            if (g == null) throw new IllegalArgumentException("unknown provider " + provider);
            return g.charge(amt);
        }
    }

    // ===== 3. Lifecycle hooks =====
    @Component
    class ConnectionPool {
        @PostConstruct                                    // after DI is complete
        void init() { System.out.println("pool warmed up"); }

        @PreDestroy                                       // on context shutdown (singletons only!)
        void shutdown() { System.out.println("pool drained"); }
    }

    // ===== 4. Scopes + the prototype-in-singleton trap =====
    @Component
    @Scope("prototype")                                   // new instance per container request
    class ReportBuilder {
        private final long createdAt = System.nanoTime(); // has state -> can't be a shared singleton
        long id() { return createdAt; }
    }

    @Service
    class ReportService {                                 // singleton (default)
        // WRONG: `private final ReportBuilder builder` — would be created ONCE, defeating prototype.
        private final ObjectProvider<ReportBuilder> builders;   // FIX: ask container each time

        ReportService(ObjectProvider<ReportBuilder> builders) { this.builders = builders; }

        void generate() {
            ReportBuilder fresh = builders.getObject();   // new prototype per call ✔
            System.out.println("report " + fresh.id());
        }
    }

    // ===== 5. @Bean in @Configuration — for classes you don't own =====
    @Configuration
    class AppConfig {
        @Bean
        @Scope("singleton")
        java.time.Clock clock() { return java.time.Clock.systemUTC(); }  // 3rd-party type as a bean

        @Bean(initMethod = "start", destroyMethod = "stop")               // lifecycle for non-annotated class
        LegacyEngine legacyEngine() { return new LegacyEngine(); }
    }
    static class LegacyEngine { void start() {} void stop() {} }

    /*
     * INTERVIEW TALKING POINTS while walking this file:
     * - @Component vs @Bean: class you own & can annotate vs 3rd-party/conditional construction.
     * - @Configuration is CGLIB-proxied so bean() calls inside it return the SAME singleton
     *   (calling clock() twice does not create two clocks) — @Configuration(proxyBeanMethods=false) disables.
     * - Map<String, Iface> injection is how you kill giant switch statements.
     * - Prototype beans: Spring never calls @PreDestroy on them — you manage cleanup.
     */
}
