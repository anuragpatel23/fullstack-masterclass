/**
 * TOPIC: Actuator & Observability — custom HealthIndicator, custom metrics
 * (Counter/Gauge/Timer), custom info contributor, custom endpoint.
 */
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.*;
import io.micrometer.core.instrument.*;
import io.micrometer.core.annotation.Timed;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ActuatorDemo {

    // ===== 1. Custom HealthIndicator ⭐ — contributes to /actuator/health =====
    @Component("paymentGateway")                    // appears as "paymentGateway" in health JSON
    static class PaymentGatewayHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            long latency = pingGateway();
            if (latency < 0) {
                return Health.down()
                        .withDetail("error", "gateway unreachable")
                        .build();                    // DOWN drags the AGGREGATE status down
            }
            if (latency > 2000) {
                return Health.status("DEGRADED")     // custom status
                        .withDetail("latencyMs", latency).build();
            }
            return Health.up().withDetail("latencyMs", latency).build();
        }
        private long pingGateway() { return 120; }   // real code: HTTP ping with timeout
    }

    // ===== 2. Custom metrics via MeterRegistry ⭐ =====
    @Service
    static class OrderMetricsService {
        private final Counter ordersPlaced;          // monotonically increasing events
        private final Timer checkoutTimer;           // latency distribution + count
        private final AtomicInteger activeCarts = new AtomicInteger();  // backing for Gauge

        OrderMetricsService(MeterRegistry registry) {
            this.ordersPlaced = Counter.builder("shop.orders.placed")
                    .tag("channel", "web")           // dimensions for slicing in Grafana
                    .description("Orders successfully placed")
                    .register(registry);
            this.checkoutTimer = Timer.builder("shop.checkout.duration")
                    .publishPercentiles(0.5, 0.95, 0.99)   // p50/p95/p99 ⭐
                    .register(registry);
            Gauge.builder("shop.carts.active", activeCarts, AtomicInteger::get)
                    .register(registry);             // gauge = current value, sampled
        }

        public void placeOrder(Runnable businessLogic) {
            checkoutTimer.record(businessLogic);     // times the lambda
            ordersPlaced.increment();
        }

        public void cartOpened()  { activeCarts.incrementAndGet(); }
        public void cartClosed()  { activeCarts.decrementAndGet(); }

        @Timed(value = "shop.inventory.sync", histogram = true)   // annotation flavor
        public void syncInventory() { /* ... */ }
    }

    // ===== 3. /actuator/info contributor =====
    @Component
    static class TeamInfoContributor implements InfoContributor {
        @Override
        public void contribute(Info.Builder builder) {
            builder.withDetail("team", Map.of("name", "checkout-squad", "oncall", "#checkout-alerts"));
        }
    }

    // ===== 4. Fully custom endpoint: /actuator/features =====
    @Component
    @Endpoint(id = "features")                       // expose via management.endpoints.web.exposure.include=features
    static class FeatureFlagsEndpoint {
        private final Map<String, Boolean> flags = new java.util.concurrent.ConcurrentHashMap<>(
                Map.of("newCheckout", true, "darkMode", false));

        @ReadOperation                                // GET /actuator/features
        public Map<String, Boolean> flags() { return flags; }

        @WriteOperation                               // POST /actuator/features
        public void toggle(String name, boolean enabled) { flags.put(name, enabled); }
    }

    /*
     * PRODUCTION NOTES TO MENTION:
     * - k8s: livenessProbe -> /actuator/health/liveness, readinessProbe -> /readiness.
     * - Change log level w/o restart: POST /actuator/loggers/com.myapp {"configuredLevel":"DEBUG"}.
     * - NEVER expose /env, /heapdump publicly (secrets). Separate management port + auth.
     * - http.server.requests gives you RED metrics (rate/errors/duration) for free.
     */
}
