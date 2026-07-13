/**
 * TOPIC: Java Basics — code illustrations for every interview trap.
 * Run: javac Basics.java && java Basics
 */
public class Basics {

    public static void main(String[] args) {
        integerCacheTrap();
        passByValueDemo();
        equalsHashCodeContract();
        stringPoolDemo();
        staticVsInstance();
    }

    /** TRAP 1: Integer cache (-128..127). Classic output-prediction question. */
    static void integerCacheTrap() {
        Integer a = 127, b = 127;
        Integer c = 128, d = 128;
        System.out.println("127 == 127 -> " + (a == b));          // true  (cached)
        System.out.println("128 == 128 -> " + (c == d));          // false (new objects)
        System.out.println("equals     -> " + c.equals(d));       // true  (always compare with equals)
    }

    /** TRAP 2: Java is ALWAYS pass-by-value. */
    static void passByValueDemo() {
        StringBuilder sb = new StringBuilder("hello");
        mutate(sb);     // mutating the object the reference points to -> visible
        reassign(sb);   // reassigning the copied reference -> NOT visible
        System.out.println(sb);  // "hello world"  (not "replaced")
    }
    static void mutate(StringBuilder s)   { s.append(" world"); }
    static void reassign(StringBuilder s) { s = new StringBuilder("replaced"); }

    /** TRAP 3: equals/hashCode contract. */
    static void equalsHashCodeContract() {
        record Point(int x, int y) {}            // records auto-generate correct equals+hashCode
        var set = new java.util.HashSet<Point>();
        set.add(new Point(1, 2));
        set.add(new Point(1, 2));
        System.out.println("Set size (record): " + set.size()); // 1 — contract honored

        // BrokenPoint overrides equals but NOT hashCode -> duplicates sneak into HashSet
        var broken = new java.util.HashSet<BrokenPoint>();
        broken.add(new BrokenPoint(1, 2));
        broken.add(new BrokenPoint(1, 2));
        System.out.println("Set size (broken): " + broken.size()); // 2 — bug!
    }

    static class BrokenPoint {
        final int x, y;
        BrokenPoint(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            return o instanceof BrokenPoint p && p.x == x && p.y == y;
        }
        // hashCode intentionally NOT overridden — inherits identity hash. DON'T do this.
    }

    /** TRAP 4: String pool vs heap. */
    static void stringPoolDemo() {
        String s1 = "java";                // pool
        String s2 = "java";                // same pool object
        String s3 = new String("java");    // forced new heap object
        System.out.println(s1 == s2);           // true
        System.out.println(s1 == s3);           // false
        System.out.println(s1 == s3.intern());  // true — intern() returns pool reference
    }

    /** static = one copy per class; instance = one per object. */
    static void staticVsInstance() {
        Counter.create(); Counter.create(); Counter.create();
        System.out.println("Objects created: " + Counter.count); // 3
    }
    static class Counter {
        static int count;                 // shared across all instances
        Counter() { count++; }
        static Counter create() { return new Counter(); }
    }
}
