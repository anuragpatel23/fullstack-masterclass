/**
 * TOPIC: Annotations — one compact class per category so you can SEE each
 * annotation in its natural habitat, plus a custom composed annotation.
 */
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.lang.annotation.*;
import java.util.List;

public class AnnotationsShowcase {

    // ===== 1. A JPA entity with the must-know mappings =====
    @Entity
    @Table(name = "orders", indexes = @Index(columnList = "customer_id"))
    static class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)   // Oracle-friendly: SEQUENCE
        private Long id;

        @Column(nullable = false, length = 30)
        private String status;

        @Enumerated(EnumType.STRING)                          // NEVER ORDINAL (reorder = data corruption)
        private Channel channel;

        @ManyToOne(fetch = FetchType.LAZY)                    // ⭐ LAZY by default for ToOne? NO — must set!
        @JoinColumn(name = "customer_id")
        private Customer customer;

        @Version                                              // optimistic locking ⭐
        private Long version;

        @Transient                                            // computed, not persisted
        private double displayTotal;

        enum Channel { WEB, MOBILE }
    }

    @Entity
    static class Customer {
        @Id @GeneratedValue private Long id;
        @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Order> orders;                           // bidirectional; Order owns the FK
    }

    // ===== 2. Repository + Service with tx/cache/security/async =====
    @Repository                                               // + SQLException -> DataAccessException
    interface OrderRepository extends org.springframework.data.jpa.repository.JpaRepository<Order, Long> {}

    @Service
    static class OrderService {
        private final OrderRepository repo;
        OrderService(OrderRepository repo) { this.repo = repo; }

        @Transactional                                        // proxy wraps: begin..commit/rollback
        @PreAuthorize("hasRole('ADMIN')")                     // method security
        public void cancel(Long id) { /* ... */ }

        @Cacheable(value = "orders", key = "#id")             // cache-aside read
        public Order find(Long id) { return repo.findById(id).orElseThrow(); }

        @CacheEvict(value = "orders", key = "#id")            // invalidate on write
        @Transactional
        public void update(Long id) { /* ... */ }

        @Async                                                // runs on task executor thread
        public void sendConfirmationEmail(Long id) { /* ... */ }

        @Scheduled(cron = "0 0 2 * * *")                      // 2 AM daily
        public void archiveOldOrders() { /* ... */ }
    }

    // ===== 3. REST controller: the full binding toolkit =====
    record CreateOrderRequest(@NotBlank String product, @Min(1) int qty, @Email String email) {}

    @RestController                                           // = @Controller + @ResponseBody
    @RequestMapping("/api/v1/orders")
    static class OrderController {

        @GetMapping("/{id}")                                  // path identity
        Order get(@PathVariable Long id,
                  @RequestHeader(value = "X-Tenant", required = false) String tenant) {
            return null; /* ... */
        }

        @GetMapping                                           // filters via query params
        List<Order> search(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String status) {
            return List.of();
        }

        @PostMapping
        @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
        Order create(@Valid @RequestBody CreateOrderRequest req) {   // JSON -> object + validation
            return null; /* ... */
        }
    }

    // ===== 4. Global exception handling =====
    @RestControllerAdvice                                     // applies to ALL controllers ⭐
    static class GlobalExceptionHandler {
        record ApiError(String code, String message) {}

        @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
        @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
        ApiError notFound(Exception e) { return new ApiError("NOT_FOUND", e.getMessage()); }

        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
        ApiError badRequest(Exception e) { return new ApiError("VALIDATION", "invalid request"); }
    }

    // ===== 5. Build your OWN composed annotation (meta-annotation trick ⭐) =====
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Service                                                  // carries @Service's meaning
    @Transactional(readOnly = true)                           // + read-only tx default
    @interface ReadOnlyService {}                             // exactly how @RestController is built

    @ReadOnlyService                                          // one annotation, two behaviors
    static class ReportingService { /* queries only */ }
}
