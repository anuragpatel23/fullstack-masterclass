/**
 * TOPIC: Exception Handling — finally traps, try-with-resources, suppressed
 * exceptions, and a production-grade custom exception.
 */
public class ExceptionsDemo {

    public static void main(String[] args) {
        System.out.println("finallyWins() -> " + finallyWins());   // 99, not 1!
        tryWithResourcesDemo();
        multiCatchDemo();
        customExceptionDemo();
    }

    /** TRAP: finally's return overrides try's return AND swallows exceptions. */
    @SuppressWarnings("finally")
    static int finallyWins() {
        try {
            return 1;                      // evaluated... then discarded
        } finally {
            return 99;                     // NEVER do this in real code
        }
    }

    /** Try-with-resources: reverse-order close + suppressed exceptions. */
    static void tryWithResourcesDemo() {
        class Resource implements AutoCloseable {
            final String name; final boolean failOnClose;
            Resource(String name, boolean failOnClose) {
                this.name = name; this.failOnClose = failOnClose;
                System.out.println("open " + name);
            }
            @Override public void close() {
                System.out.println("close " + name);   // closes in REVERSE order
                if (failOnClose) throw new IllegalStateException("close failed: " + name);
            }
        }
        try (var r1 = new Resource("db", false);
             var r2 = new Resource("file", true)) {
            throw new RuntimeException("primary failure in body");
        } catch (RuntimeException e) {
            System.out.println("caught primary : " + e.getMessage());
            for (Throwable s : e.getSuppressed())      // close-failure is NOT lost
                System.out.println("suppressed     : " + s.getMessage());
        }
    }

    static void multiCatchDemo() {
        for (String input : new String[]{"42", "oops", null}) {
            try {
                System.out.println("parsed: " + Integer.parseInt(input.trim()));
            } catch (NumberFormatException | NullPointerException e) {  // multi-catch
                System.out.println("bad input (" + e.getClass().getSimpleName() + ")");
            }
        }
    }

    // ===== Production-style custom exception =====
    /** Unchecked business exception with context + error code + preserved cause. */
    static class OrderNotFoundException extends RuntimeException {
        private final String orderId;
        private final String errorCode = "ORD-404";

        OrderNotFoundException(String orderId, Throwable cause) {
            super("Order not found: " + orderId, cause);   // ALWAYS keep the cause
            this.orderId = orderId;
        }
        public String orderId()   { return orderId; }
        public String errorCode() { return errorCode; }
    }

    static void customExceptionDemo() {
        try {
            findOrder("ORD-123");
        } catch (OrderNotFoundException e) {
            System.out.println(e.errorCode() + " -> " + e.getMessage());
            System.out.println("root cause: " + e.getCause().getMessage()); // stack preserved
        }
    }

    static void findOrder(String id) {
        try {
            throw new java.util.NoSuchElementException("row absent in DB");  // simulated DAO failure
        } catch (java.util.NoSuchElementException dbError) {
            // Translate low-level exception -> domain exception, KEEPING the cause
            throw new OrderNotFoundException(id, dbError);
        }
    }
}
