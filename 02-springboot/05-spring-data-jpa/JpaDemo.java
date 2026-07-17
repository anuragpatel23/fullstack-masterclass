/**
 * TOPIC: Spring Data JPA — entities done right (Oracle-friendly), N+1 fixes,
 * dirty checking, projections, optimistic locking, derived queries.
 */
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class JpaDemo {

    // ===== Entities: bidirectional Customer 1..N Order =====
    @Entity @Table(name = "customers")
    static class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cust_seq")
        @SequenceGenerator(name = "cust_seq", sequenceName = "CUSTOMER_SEQ", allocationSize = 50)
        private Long id;                       // SEQUENCE: Oracle-native + batch-friendly ⭐

        private String name;

        @OneToMany(mappedBy = "customer",      // inverse side — Order owns the FK
                   cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Order> orders = new ArrayList<>();

        /** Helper keeps BOTH sides in sync — interviewers look for this. */
        void addOrder(Order o) { orders.add(o); o.customer = this; }

        Long getId() { return id; }
        List<Order> getOrders() { return orders; }
    }

    @Entity @Table(name = "orders")
    static class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ord_seq")
        @SequenceGenerator(name = "ord_seq", sequenceName = "ORDER_SEQ", allocationSize = 50)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)     // ⭐ override the EAGER default!
        @JoinColumn(name = "customer_id")      // owning side — has the FK column
        private Customer customer;

        private BigDecimal total;
        private String status;
        private Instant createdAt;

        @Version                               // optimistic locking ⭐
        private Long version;

        BigDecimal getTotal() { return total; }
        void setStatus(String s) { this.status = s; }
    }

    // ===== Repository: every query style =====
    interface OrderRepository extends JpaRepository<Order, Long> {

        // 1. Derived query — parsed from method name
        List<Order> findByStatusAndCreatedAtAfter(String status, Instant after);

        // 2. JPQL with JOIN FETCH — THE N+1 fix ⭐
        @Query("select distinct c from JpaDemo$Customer c join fetch c.orders where c.id in :ids")
        List<Customer> findCustomersWithOrders(@Param("ids") List<Long> ids);

        // 3. @EntityGraph — declarative fetch plan (alternative N+1 fix)
        @EntityGraph(attributePaths = "customer")
        List<Order> findByStatus(String status);

        // 4. Native query — when you need Oracle-specific SQL
        @Query(value = """
                SELECT * FROM (
                  SELECT o.*, RANK() OVER (ORDER BY o.total DESC) rnk FROM orders o
                ) WHERE rnk <= :n
                """, nativeQuery = true)
        List<Order> topNByTotal(@Param("n") int n);

        // 5. Bulk update — @Modifying + clear stale persistence context ⭐
        @Modifying(clearAutomatically = true)
        @Query("update JpaDemo$Order o set o.status = 'ARCHIVED' where o.createdAt < :cutoff")
        int archiveOlderThan(@Param("cutoff") Instant cutoff);

        // 6. Projection — fetch only needed columns
        interface OrderSummary { Long getId(); BigDecimal getTotal(); }
        List<OrderSummary> findByCustomerId(Long customerId);

        // 7. Pessimistic lock for hot rows (inventory-style)
        @org.springframework.data.jpa.repository.Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select o from JpaDemo$Order o where o.id = :id")
        Optional<Order> findForUpdate(@Param("id") Long id);
    }

    // ===== Service: dirty checking + pagination + N+1 demonstration =====
    @Service
    static class OrderService {
        private final OrderRepository repo;
        OrderService(OrderRepository repo) { this.repo = repo; }

        /** DIRTY CHECKING ⭐: no save() call — managed entity flushes automatically. */
        @Transactional
        public void markShipped(Long orderId) {
            Order order = repo.findById(orderId).orElseThrow();  // now MANAGED
            order.setStatus("SHIPPED");                          // just mutate...
            // ...no repo.save(order) needed: flush at commit issues the UPDATE
        }

        /** N+1 in action vs fixed. */
        @Transactional(readOnly = true)
        public void nPlusOneDemo(List<Long> customerIds) {
            // BAD: 1 query for customers + 1 query PER customer for orders
            // customers.forEach(c -> c.getOrders().size());

            // GOOD: single query with join fetch
            List<Customer> customers = repo.findCustomersWithOrders(customerIds);
            customers.forEach(c -> System.out.println(c.getId() + " -> " + c.getOrders().size() + " orders"));
        }

        /** Pagination: Page runs a count query; use Slice for infinite scroll. */
        public Page<Order> recent(int page) {
            return repo.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        /** Optimistic-lock retry pattern. */
        @Transactional
        public void updateWithRetry(Long id) {
            try {
                markShipped(id);
            } catch (OptimisticLockException e) {
                markShipped(id);        // real code: bounded retries / @Retryable
            }
        }
    }

    /*
     * TALKING POINTS:
     * - Why LAZY on @ManyToOne, and what LazyInitializationException means.
     * - open-in-view=false + map to DTOs inside the transaction (OSIV anti-pattern).
     * - SEQUENCE + allocationSize=50: one DB roundtrip per 50 ids, enables insert batching.
     * - @Version column: WHERE id=? AND version=? — 0 rows updated -> exception -> retry.
     */
}
