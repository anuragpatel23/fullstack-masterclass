/**
 * TOPIC: @Transactional — every pitfall as runnable-shaped code:
 * self-invocation, checked exceptions, REQUIRED vs REQUIRES_NEW, rollback-only.
 */
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalDemo {

    // =====================================================================
    // PITFALL 1: SELF-INVOCATION ⭐⭐
    // =====================================================================
    @Service
    static class BrokenService {
        public void outerNotTransactional() {
            // this.* call goes DIRECTLY to the target object, NOT through the proxy.
            this.innerTransactional();     // ❌ NO transaction is started here!
        }

        @Transactional
        public void innerTransactional() { /* writes happen WITHOUT a tx when self-invoked */ }
    }

    @Service
    static class FixedService {
        private final AuditService auditService;              // FIX A: separate bean -> proxied call
        private final TransactionTemplate txTemplate;         // FIX B: programmatic tx

        FixedService(AuditService auditService, TransactionTemplate txTemplate) {
            this.auditService = auditService;
            this.txTemplate = txTemplate;
        }

        public void outer() {
            auditService.writeAudit("done");                  // ✔ goes through proxy
            txTemplate.executeWithoutResult(status -> {       // ✔ explicit boundary, no proxy needed
                /* transactional work */
            });
        }
    }

    // =====================================================================
    // PITFALL 2: CHECKED EXCEPTIONS DON'T ROLL BACK ⭐⭐
    // =====================================================================
    @Service
    static class PaymentService {

        @Transactional                                        // default: rollback on RuntimeException/Error ONLY
        public void payBroken() throws Exception {
            // debit(); credit();
            throw new Exception("network glitch");            // ❌ COMMITS anyway! (checked)
        }

        @Transactional(rollbackFor = Exception.class)         // ✔ the fix
        public void payFixed() throws Exception {
            throw new Exception("network glitch");            // now rolls back
        }
    }

    // =====================================================================
    // PITFALL 3: REQUIRED vs REQUIRES_NEW — the inner-failure scenario ⭐
    // =====================================================================
    @Service
    static class OrderService {
        private final AuditService audit;
        private final InventoryService inventory;
        OrderService(AuditService a, InventoryService i) { audit = a; inventory = i; }

        @Transactional
        public void placeOrder() {
            // ... insert order row (part of THIS tx)
            try {
                inventory.reserveStock_REQUIRED();            // joins THIS tx
            } catch (RuntimeException e) {
                // ❌ TRAP: you caught it, but the SHARED tx is already marked
                // rollback-only. Commit at method end throws UnexpectedRollbackException,
                // and the order insert is rolled back anyway.
            }

            try {
                audit.writeAudit_REQUIRES_NEW("order attempted");  // separate tx
            } catch (RuntimeException e) {
                // ✔ audit tx rolled back independently; THIS tx is untouched.
            }
        }
    }

    @Service
    static class InventoryService {
        @Transactional(propagation = Propagation.REQUIRED)    // joins caller's tx
        public void reserveStock_REQUIRED() {
            throw new IllegalStateException("out of stock");  // poisons the shared tx
        }
    }

    @Service
    static class AuditService {
        /** Audit must survive even if the business tx rolls back -> REQUIRES_NEW ⭐ */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void writeAudit(String msg) { /* insert into audit_log */ }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void writeAudit_REQUIRES_NEW(String msg) {
            /* independent tx: caller suspended, new connection, own commit/rollback */
        }
    }

    // =====================================================================
    // Good hygiene: readOnly for queries, timeout for safety
    // =====================================================================
    @Service
    static class ReportService {
        @Transactional(readOnly = true, timeout = 5)          // no dirty-check snapshots; fail slow queries
        public Object monthlyReport() { return null; }
    }

    /*
     * WHITEBOARD SUMMARY (say this out loud in the interview):
     * caller -> [PROXY: begin tx, bind conn to ThreadLocal] -> target method
     *        -> repository calls reuse bound conn -> return
     *        -> [PROXY: commit | rollback if RuntimeException/Error]
     *
     * The proxy explains ALL pitfalls:
     *   self-invocation (no proxy hop), private/final (can't override),
     *   @Async (different thread, no bound conn), checked exceptions (rollback rules).
     */
}
