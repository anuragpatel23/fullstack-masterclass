/**
 * TOPIC: Modern Java (9→24) — records, sealed types, pattern matching,
 * switch expressions, virtual threads. Requires JDK 21+.
 */
import java.util.*;
import java.util.concurrent.*;

public class ModernJavaDemo {

    public static void main(String[] args) throws Exception {
        recordsDemo();
        sealedAndPatternMatching();
        switchExpressions();
        virtualThreadsDemo();
        smallGoodies();
    }

    // ===== Records (16): immutable data carrier with validation =====
    record Money(String currency, long paise) {
        Money {                                          // compact constructor = validation
            if (paise < 0) throw new IllegalArgumentException("negative amount");
            currency = currency.toUpperCase();           // normalize before field assignment
        }
        Money add(Money other) {                         // records can have behavior
            if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
            return new Money(currency, paise + other.paise);
        }
    }
    static void recordsDemo() {
        Money a = new Money("inr", 50_00), b = new Money("INR", 25_50);
        System.out.println(a.add(b));                    // auto toString: Money[currency=INR, paise=7550]
        System.out.println(a.equals(new Money("INR", 50_00)));  // auto equals: true
    }

    // ===== Sealed hierarchy (17) + record patterns in switch (21) =====
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    static double area(Shape s) {
        return switch (s) {                              // EXHAUSTIVE — no default needed!
            case Circle(double r)            -> Math.PI * r * r;      // record pattern destructuring
            case Rectangle(double w, double h) -> w * h;
            case Triangle(double b, double h)  -> 0.5 * b * h;
        };
    }
    static void sealedAndPatternMatching() {
        List<Shape> shapes = List.of(new Circle(1), new Rectangle(2, 3), new Triangle(4, 5));
        shapes.forEach(s -> System.out.printf("%s area = %.2f%n", s.getClass().getSimpleName(), area(s)));

        // Pattern matching for instanceof (16) — no cast needed
        Object o = "hello";
        if (o instanceof String str && str.length() > 3) System.out.println("pattern-matched: " + str.toUpperCase());
    }

    // ===== Switch expressions (14) with guards (21) =====
    static void switchExpressions() {
        int day = 6;
        String type = switch (day) {
            case 1, 2, 3, 4, 5 -> "weekday";             // multi-label, no fall-through
            case 6, 7          -> "weekend";
            default            -> "invalid";
        };
        System.out.println("day type: " + type);

        Object obj = 42;
        String desc = switch (obj) {
            case Integer i when i > 40 -> "big int " + i;   // guarded pattern (21)
            case Integer i             -> "int " + i;
            case String s              -> "string " + s;
            default                    -> "something else";
        };
        System.out.println(desc);
    }

    // ===== Virtual threads (21): 10k concurrent "blocking" tasks, trivially =====
    static void virtualThreadsDemo() throws Exception {
        long start = System.currentTimeMillis();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(100);                   // blocking is CHEAP — thread unmounts
                    return null;
                });
            }
        } // close() waits for completion
        System.out.printf("10,000 blocking tasks in %d ms (platform threads would need ~10k threads)%n",
                System.currentTimeMillis() - start);
        System.out.println("is virtual? " + Thread.ofVirtual().unstarted(() -> {}).isVirtual());
    }

    static void smallGoodies() {
        var names = List.of("ana", "bo", "cy");          // var (10) + List.of (9)
        System.out.println(names.reversed());            // sequenced collections (21)

        String json = """
            { "role": "fullstack", "stack": ["java", "spring", "react"] }
            """;                                          // text block (15)
        System.out.println(json.strip().lines().count() + " line(s)");

        System.out.println("ha".repeat(3));               // String.repeat (11)
    }
}
