/**
 * TOPIC: Microservices — circuit breaker + retry + fallback (Resilience4j),
 * outbox pattern, idempotent consumer, saga orchestration sketch.
 */
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MicroservicesDemo {

    // =====================================================================
    // 1. RESILIENT CLIENT: circuit breaker + retry + timeout + fallback ⭐
    // =====================================================================
    @Service
    static class InventoryClient {
        private final RestClient rest = RestClient.builder()
                .baseUrl("http://inventory-service").build();   // discovery resolves the name

        /*  application.yml:
         *  resilience4j.circuitbreaker.instances.inventory:
         *    failure-rate-threshold: 50        # % failures to trip OPEN
         *    sliding-window-size: 20
         *    wait-duration-in-open-state: 10s  # before HALF_OPEN probe
         *  resilience4j.retry.instances.inventory:
         *    max-attempts: 3
         *    wait-duration: 200ms
         *    enable-exponential-backoff: true
         */
        @CircuitBreaker(name = "inventory", fallbackMethod = "stockFallback")
        @Retry(name = "inventory")
        @TimeLimiter(name = "inventory")                        // ALWAYS have a timeout ⭐
        public CompletableFuture<Integer> availableStock(String sku) {
            return CompletableFuture.supplyAsync(() ->
                    rest.get().uri("/stock/{sku}", sku).retrieve().body(Integer.class));
        }

        /** Fallback = graceful degradation, NOT hiding bugs. Same signature + Throwable. */
        CompletableFuture<Integer> stockFallback(String sku, Throwable t) {
            System.out.println("[fallback] inventory down (" + t.getClass().getSimpleName()
                    + ") -> serving cached/pessimistic value");
            return CompletableFuture.completedFuture(0);        // e.g., "assume out of stock"
        }
    }

    // =====================================================================
    // 2. OUTBOX PATTERN ⭐ — never lose an event
    // =====================================================================
    @Service
    static class OrderService {
        // Problem: save order (DB tx) then publish to Kafka — crash in between = lost event.
        // Solution: event goes into an OUTBOX TABLE in the SAME local transaction.

        @Transactional
        public void placeOrder(String orderId, double amount) {
            // 1. INSERT INTO orders (...)            — business write
            // 2. INSERT INTO outbox_events (id, type, payload, published=false)
            //    -> both commit or roll back ATOMICALLY ✔
        }
    }

    @Component
    static class OutboxRelay {
        /** Separate poller (or Debezium CDC) publishes and marks rows. At-least-once! */
        @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 1000)
        public void publishPending() {
            // SELECT * FROM outbox_events WHERE published = false ORDER BY id LIMIT 100
            // kafkaTemplate.send(...); UPDATE outbox_events SET published = true
            // crash between send & update -> duplicate send -> consumers MUST be idempotent
        }
    }

    // =====================================================================
    // 3. IDEMPOTENT CONSUMER ⭐⭐ — at-least-once delivery survival kit
    // =====================================================================
    @Service
    static class PaymentEventConsumer {
        private final Set<String> processedEventIds = new HashSet<>(); // real: DB table w/ unique constraint

        // @KafkaListener(topics = "payments")
        @Transactional
        public void onPaymentCompleted(String eventId, String orderId) {
            // Dedupe check INSIDE the same tx as the side effect:
            if (!processedEventIds.add(eventId)) {              // real: INSERT .. processed_events
                System.out.println("[consumer] duplicate " + eventId + " — skipping");
                return;                                          // unique-constraint violation = skip
            }
            // UPDATE orders SET status='PAID' WHERE id=?       — the actual effect, exactly once
            System.out.println("[consumer] order " + orderId + " marked PAID");
        }
    }

    // =====================================================================
    // 4. SAGA (orchestration) — distributed tx via compensation ⭐
    // =====================================================================
    @Service
    static class OrderSagaOrchestrator {
        record SagaStep(String name, Runnable action, Runnable compensation) {}

        public void run(String orderId) {
            Deque<SagaStep> completed = new ArrayDeque<>();
            List<SagaStep> steps = List.of(
                new SagaStep("reserve-inventory",
                        () -> System.out.println("inventory reserved"),
                        () -> System.out.println("COMPENSATE: release inventory")),
                new SagaStep("charge-payment",
                        () -> { System.out.println("charging card"); throw new IllegalStateException("card declined"); },
                        () -> System.out.println("COMPENSATE: refund payment")),
                new SagaStep("create-shipment",
                        () -> System.out.println("shipment created"),
                        () -> System.out.println("COMPENSATE: cancel shipment")));

            for (SagaStep step : steps) {
                try {
                    step.action().run();
                    completed.push(step);
                } catch (RuntimeException e) {
                    System.out.println("[saga] step '" + step.name() + "' failed: " + e.getMessage());
                    completed.forEach(done -> done.compensation().run());  // undo in REVERSE order
                    // mark order FAILED; notify user; eventual consistency restored
                    return;
                }
            }
            System.out.println("[saga] order " + orderId + " completed");
        }
    }

    public static void main(String[] args) {
        new OrderSagaOrchestrator().run("ORD-9");
        /* prints:
           inventory reserved
           charging card
           [saga] step 'charge-payment' failed: card declined
           COMPENSATE: release inventory
        */
    }
}
