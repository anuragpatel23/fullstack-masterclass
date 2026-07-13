/**
 * TOPIC: Generics — bounded types, PECS wildcards, erasure workarounds,
 * and a generic method + generic class you can explain line by line.
 */
import java.util.*;
import java.util.function.Supplier;

public class GenericsDemo {

    public static void main(String[] args) {
        // Generic method with bounded type
        System.out.println(max(List.of(3, 9, 4)));            // 9
        System.out.println(max(List.of("pear", "apple")));    // pear

        pecsDemo();
        erasureWorkaround();
        genericClassDemo();
        arrayCovarianceTrap();
    }

    /** Bounded generic method: T must be comparable to itself (or a supertype). */
    static <T extends Comparable<? super T>> T max(List<? extends T> list) {
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }

    // ===== PECS =====
    static class Fruit { public String toString() { return getClass().getSimpleName(); } }
    static class Apple extends Fruit {}
    static class GreenApple extends Apple {}

    /** src PRODUCES T (extends), dest CONSUMES T (super) — the canonical signature. */
    static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T t : src) dest.add(t);
    }

    static void pecsDemo() {
        List<Apple> apples = new ArrayList<>(List.of(new Apple(), new GreenApple()));
        List<Fruit> fruits = new ArrayList<>();
        GenericsDemo.<Apple>copy(apples, fruits);   // Apple producer -> Fruit consumer
        System.out.println("copied: " + fruits);

        List<? extends Fruit> producer = apples;
        Fruit f = producer.get(0);                  // reading OK
        // producer.add(new Apple());               // COMPILE ERROR — can't write to extends

        List<? super Apple> consumer = fruits;
        consumer.add(new GreenApple());             // writing OK
        Object read = consumer.get(0);              // reads come out as Object
        System.out.println("consumer read as: " + read);
    }

    /** Can't do `new T()` — pass a Supplier (or Class<T>) instead. */
    static <T> List<T> filledList(int n, Supplier<T> factory) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(factory.get());
        return list;
    }
    static void erasureWorkaround() {
        List<StringBuilder> l = filledList(3, StringBuilder::new);
        System.out.println("erasure workaround, size=" + l.size());
        // Proof of erasure: same runtime class
        System.out.println(new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass()); // true
    }

    /** A typed generic class: a simple Pair with a swap utility. */
    record Pair<K, V>(K key, V value) {
        Pair<V, K> swap() { return new Pair<>(value, key); }
    }
    static void genericClassDemo() {
        Pair<String, Integer> p = new Pair<>("age", 30);
        System.out.println(p + " swapped -> " + p.swap());
    }

    /** Arrays are covariant (unsafe at runtime); generics are invariant (safe at compile time). */
    static void arrayCovarianceTrap() {
        Object[] arr = new String[2];       // legal — arrays covariant
        try {
            arr[0] = 42;                    // compiles, EXPLODES at runtime
        } catch (ArrayStoreException e) {
            System.out.println("ArrayStoreException — why generics chose invariance ✔");
        }
        // List<Object> list = new ArrayList<String>();  // would not even compile
    }
}
