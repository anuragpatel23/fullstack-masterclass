/**
 * TOPIC: REST API — a production-shaped controller: DTOs, validation, pagination,
 * ResponseEntity, global RFC-7807 errors, idempotency key, versioning.
 */
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RestApiDemo {

    // ===== DTOs (never expose entities ⭐) =====
    record CreateProductRequest(
            @NotBlank @Size(max = 100) String name,
            @Positive double price,
            @NotNull @Pattern(regexp = "BOOKS|ELECTRONICS|GROCERY") String category) {}

    record ProductResponse(long id, String name, double price, String category, Instant createdAt) {}

    // ===== Controller =====
    @RestController
    @RequestMapping("/api/v1/products")                 // URI versioning
    static class ProductController {

        private final Map<Long, ProductResponse> store = new ConcurrentHashMap<>();
        private final Map<String, Long> idempotencyKeys = new ConcurrentHashMap<>();
        private long seq = 0;

        /** CREATE: 201 + Location header + idempotency-key support ⭐ */
        @PostMapping
        ResponseEntity<ProductResponse> create(
                @Valid @RequestBody CreateProductRequest req,
                @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {

            if (idemKey != null && idempotencyKeys.containsKey(idemKey)) {   // safe retry
                return ResponseEntity.ok(store.get(idempotencyKeys.get(idemKey)));
            }
            long id = ++seq;
            var product = new ProductResponse(id, req.name(), req.price(), req.category(), Instant.now());
            store.put(id, product);
            if (idemKey != null) idempotencyKeys.put(idemKey, id);

            return ResponseEntity
                    .created(URI.create("/api/v1/products/" + id))   // 201 + Location ⭐
                    .body(product);
        }

        /** READ one: 200 or 404 */
        @GetMapping("/{id}")
        ResponseEntity<ProductResponse> get(@PathVariable long id) {
            var p = store.get(id);
            return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
        }

        /** LIST: pagination + filtering + sorting via query params */
        @GetMapping
        Page<ProductResponse> list(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") @Max(100) int size,   // cap page size!
                @RequestParam(required = false) String category) {
            List<ProductResponse> filtered = store.values().stream()
                    .filter(p -> category == null || p.category().equals(category))
                    .sorted(Comparator.comparing(ProductResponse::id))
                    .toList();
            var pageable = PageRequest.of(page, size);
            int from = Math.min((int) pageable.getOffset(), filtered.size());
            int to = Math.min(from + size, filtered.size());
            return new PageImpl<>(filtered.subList(from, to), pageable, filtered.size());
        }

        /** PUT = full replace (idempotent). PATCH would take a partial body. */
        @PutMapping("/{id}")
        ResponseEntity<ProductResponse> replace(@PathVariable long id,
                                                @Valid @RequestBody CreateProductRequest req) {
            if (!store.containsKey(id)) return ResponseEntity.notFound().build();
            var updated = new ProductResponse(id, req.name(), req.price(), req.category(), Instant.now());
            store.put(id, updated);
            return ResponseEntity.ok(updated);
        }

        /** DELETE: 204 No Content */
        @DeleteMapping("/{id}")
        ResponseEntity<Void> delete(@PathVariable long id) {
            return store.remove(id) != null
                    ? ResponseEntity.noContent().build()      // 204
                    : ResponseEntity.notFound().build();      // idempotent effect either way
        }
    }

    // ===== Global error handling with RFC-7807 ProblemDetail (Spring 6) ⭐ =====
    @RestControllerAdvice
    static class ApiExceptionHandler {

        /** Validation failures -> 400 with per-field messages. */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        ProblemDetail onValidation(MethodArgumentNotValidException ex) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Validation failed");
            Map<String, String> fields = new HashMap<>();
            ex.getBindingResult().getFieldErrors()
              .forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
            pd.setProperty("fieldErrors", fields);
            return pd;
        }

        /** Anything unexpected -> 500 WITHOUT leaking internals. */
        @ExceptionHandler(Exception.class)
        ProblemDetail onGeneric(Exception ex) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            pd.setTitle("Internal error");
            pd.setProperty("traceId", UUID.randomUUID().toString());  // correlate with logs
            return pd;   // log full stack server-side; never echo it to clients
        }
    }

    /*
     * TALKING POINTS:
     * - Why ResponseEntity: explicit status + headers (Location, ETag, Cache-Control).
     * - Why DTO records: no lazy-loading leaks, stable contract, validation lives on input DTO.
     * - Idempotency-Key: makes POST retry-safe (payments!).
     * - ProblemDetail: standard error body (RFC 7807) instead of ad-hoc JSON.
     */
}
