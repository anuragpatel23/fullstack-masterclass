/**
 * TOPIC: Caching — cache-aside with @Cacheable, correct invalidation on writes,
 * stampede protection, Caffeine TTL config, evict-after-commit pattern.
 */
import org.springframework.cache.annotation.*;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.*;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;

public class CachingDemo {

    // ===== 1. Production cache config: TTL + max size (never the default map!) =====
    @Configuration
    @EnableCaching
    static class CacheConfig {
        @Bean
        CacheManager cacheManager() {
            var mgr = new CaffeineCacheManager("products", "prices");
            mgr.setCaffeine(Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(10))   // TTL: the invalidation safety net
                    .maximumSize(10_000)                        // bounded: no OOM
                    .recordStats());                            // hit-rate metrics -> actuator
            return mgr;
        }
        // Redis flavor: RedisCacheManager + entryTtl(Duration.ofMinutes(10)) — shared across pods.
    }

    record Product(Long id, String name, double price) {}

    // ===== 2. The service: read-through + correct write invalidation =====
    @Service
    @CacheConfig(cacheNames = "products")
    static class ProductService {

        /**
         * Cache-aside read. sync=true ⭐: on a cold/expired hot key only ONE thread
         * loads from DB; the rest wait — stampede protection.
         */
        @Cacheable(key = "#id", unless = "#result == null", sync = true)
        public Product find(Long id) {
            System.out.println("DB HIT for product " + id);     // printed only on cache miss
            return new Product(id, "Laptop", 74_999);           // real code: repository call
        }

        /** Composite key with SpEL. */
        @Cacheable(cacheNames = "prices", key = "#id + ':' + #region")
        public double priceFor(Long id, String region) { return 74_999; }

        /** UPDATE: run method AND refresh cache entry (write-through style). */
        @CachePut(key = "#product.id")
        @Transactional
        public Product update(Product product) {
            // repository.save(product);
            return product;                                     // return value replaces cache entry
        }

        /** DELETE: remove the entry. */
        @CacheEvict(key = "#id")
        @Transactional
        public void delete(Long id) { /* repository.deleteById(id); */ }

        /** Bulk import: nuke the whole cache. */
        @CacheEvict(allEntries = true)
        public void bulkImport() { /* ... */ }
    }

    // ===== 3. Senior pattern ⭐: evict AFTER the transaction commits =====
    // Problem: @CacheEvict fires when the method returns — but if eviction happens
    // before commit, another thread can re-cache the OLD value read from the
    // still-uncommitted DB state. Fix: publish an event, evict after commit.
    record ProductChangedEvent(Long id) {}

    @Service
    static class SafeProductWriter {
        private final org.springframework.context.ApplicationEventPublisher events;
        SafeProductWriter(org.springframework.context.ApplicationEventPublisher events) { this.events = events; }

        @Transactional
        public void updatePrice(Long id, double price) {
            // repository.updatePrice(id, price);
            events.publishEvent(new ProductChangedEvent(id));   // recorded, not yet delivered
        }
    }

    @org.springframework.stereotype.Component
    static class CacheInvalidator {
        private final CacheManager cacheManager;
        CacheInvalidator(CacheManager cm) { this.cacheManager = cm; }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)  // ⭐ only after commit
        public void onProductChanged(ProductChangedEvent e) {
            var cache = cacheManager.getCache("products");
            if (cache != null) cache.evict(e.id());
        }
    }

    /*
     * TALKING POINTS:
     * - @Cacheable is an AOP proxy: self-invocation (this.find(id)) skips the cache.
     * - unless="#result == null": don't cache misses (or DO cache them briefly to
     *   block cache-penetration attacks — know both sides).
     * - Local vs distributed: Caffeine per-pod (stale across pods) vs Redis shared
     *   (network hop). Common combo: Caffeine L1 + Redis L2.
     */
}
