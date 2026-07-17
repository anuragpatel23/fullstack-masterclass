/**
 * TOPIC: The 5 Spring snippets interviewers most often ask you to WRITE LIVE.
 * Practice until you can produce each from a blank editor in under 5 minutes.
 */
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

public class RapidFireSnippets {

    // =====================================================================
    // SNIPPET 1: "Write a REST controller for a TODO resource" (CRUD shape)
    // =====================================================================
    record Todo(Long id, String title, boolean done) {}
    record CreateTodo(@NotBlank String title) {}

    @RestController
    @RequestMapping("/api/todos")
    static class TodoController {
        private final Map<Long, Todo> db = new HashMap<>();
        private long seq;

        @PostMapping
        ResponseEntity<Todo> create(@Valid @RequestBody CreateTodo req) {
            var t = new Todo(++seq, req.title(), false);
            db.put(t.id(), t);
            return ResponseEntity.status(HttpStatus.CREATED).body(t);   // 201
        }

        @GetMapping("/{id}")
        ResponseEntity<Todo> get(@PathVariable Long id) {
            return Optional.ofNullable(db.get(id))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());          // 404
        }

        @PutMapping("/{id}")
        ResponseEntity<Todo> update(@PathVariable Long id, @Valid @RequestBody CreateTodo req) {
            if (!db.containsKey(id)) return ResponseEntity.notFound().build();
            var t = new Todo(id, req.title(), db.get(id).done());
            db.put(id, t);
            return ResponseEntity.ok(t);
        }

        @DeleteMapping("/{id}")
        ResponseEntity<Void> delete(@PathVariable Long id) {
            db.remove(id);
            return ResponseEntity.noContent().build();                   // 204
        }
    }

    // =====================================================================
    // SNIPPET 2: "Write a global exception handler"
    // =====================================================================
    static class NotFoundException extends RuntimeException {
        NotFoundException(String m) { super(m); }
    }

    @RestControllerAdvice
    static class GlobalErrors {
        record ApiError(String code, String message) {}

        @ExceptionHandler(NotFoundException.class)
        ResponseEntity<ApiError> notFound(NotFoundException e) {
            return ResponseEntity.status(404).body(new ApiError("NOT_FOUND", e.getMessage()));
        }

        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        ResponseEntity<ApiError> invalid(Exception e) {
            return ResponseEntity.badRequest().body(new ApiError("VALIDATION", "invalid input"));
        }
    }

    // =====================================================================
    // SNIPPET 3: "Money transfer service — make it transactional and safe"
    // =====================================================================
    @Service
    static class TransferService {
        // interface AccountRepo { Account findForUpdate(String id); ... }

        @Transactional(rollbackFor = Exception.class)   // mention WHY: checked exceptions
        public void transfer(String fromId, String toId, java.math.BigDecimal amount) {
            if (amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");

            // Lock rows in a CONSISTENT ORDER to avoid deadlock ⭐ (talk about this!)
            String first = fromId.compareTo(toId) < 0 ? fromId : toId;
            String second = first.equals(fromId) ? toId : fromId;
            // var a = repo.findForUpdate(first);   // SELECT ... FOR UPDATE
            // var b = repo.findForUpdate(second);

            // debit(from, amount); credit(to, amount);   — both or neither (atomicity)
        }
    }

    // =====================================================================
    // SNIPPET 4: "Service+cache: read-through product lookup"
    // =====================================================================
    @Service
    static class ProductLookup {
        @org.springframework.cache.annotation.Cacheable(
                value = "products", key = "#id", unless = "#result == null", sync = true)
        public Todo findProduct(Long id) { return null; /* repo call */ }

        @org.springframework.cache.annotation.CacheEvict(value = "products", key = "#id")
        @Transactional
        public void updateProduct(Long id) { /* write + invalidate */ }
    }

    // =====================================================================
    // SNIPPET 5: "Call another service with retry + fallback" (resilience shape)
    // =====================================================================
    @Service
    static class QuoteClient {
        private final org.springframework.web.client.RestClient rest =
                org.springframework.web.client.RestClient.create("http://quote-service");

        public String quoteWithFallback(String symbol) {
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    return rest.get().uri("/quotes/{s}", symbol).retrieve().body(String.class);
                } catch (Exception e) {
                    try { Thread.sleep(100L * attempt); }               // backoff
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
            return "CACHED_QUOTE";   // graceful degradation (in prod: Resilience4j annotations)
        }
    }
}
