/**
 * TOPIC: Design Patterns — the ones you'll be asked to WRITE:
 * all 5 singleton variants, builder, strategy, observer, decorator, factory.
 */
import java.util.*;
import java.util.function.Function;

public class PatternsDemo {

    public static void main(String[] args) {
        System.out.println(HolderSingleton.getInstance() == HolderSingleton.getInstance()); // true
        System.out.println(EnumSingleton.INSTANCE.hashCode() == EnumSingleton.INSTANCE.hashCode());
        builderDemo();
        strategyDemo();
        observerDemo();
        decoratorDemo();
        factoryDemo();
    }

    // ===================== SINGLETON — all variants =====================

    /** 1. Eager: simple, thread-safe, but created even if never used. */
    static class EagerSingleton {
        private static final EagerSingleton INSTANCE = new EagerSingleton();
        private EagerSingleton() {}
        static EagerSingleton getInstance() { return INSTANCE; }
    }

    /** 2. Double-checked locking: lazy + fast. `volatile` is MANDATORY
     *  (prevents seeing a partially-constructed object due to reordering). */
    static class DclSingleton {
        private static volatile DclSingleton instance;
        private DclSingleton() {}
        static DclSingleton getInstance() {
            if (instance == null) {                    // 1st check: no lock on hot path
                synchronized (DclSingleton.class) {
                    if (instance == null) {            // 2nd check: inside lock
                        instance = new DclSingleton();
                    }
                }
            }
            return instance;
        }
    }

    /** 3. Static holder (Bill Pugh): lazy via classloading guarantees. Best plain-class option. */
    static class HolderSingleton {
        private HolderSingleton() {}
        private static class Holder { static final HolderSingleton I = new HolderSingleton(); }
        static HolderSingleton getInstance() { return Holder.I; }   // Holder loads on first call
    }

    /** 4. Enum (Effective Java): immune to reflection, serialization, cloning. */
    enum EnumSingleton {
        INSTANCE;
        void doWork() { System.out.println("enum singleton working"); }
    }

    // ===================== BUILDER =====================
    static class HttpRequest {
        private final String url, method; private final Map<String, String> headers; private final int timeout;
        private HttpRequest(Builder b) { url = b.url; method = b.method; headers = Map.copyOf(b.headers); timeout = b.timeout; }
        static Builder builder(String url) { return new Builder(url); }
        static class Builder {
            private final String url; private String method = "GET";
            private final Map<String, String> headers = new HashMap<>(); private int timeout = 30;
            Builder(String url) { this.url = url; }
            Builder method(String m)             { this.method = m; return this; }
            Builder header(String k, String v)   { headers.put(k, v); return this; }
            Builder timeout(int t)               { this.timeout = t; return this; }
            HttpRequest build() {
                if (timeout <= 0) throw new IllegalStateException("bad timeout");  // validate once
                return new HttpRequest(this);
            }
        }
        @Override public String toString() { return method + " " + url + " " + headers + " t=" + timeout; }
    }
    static void builderDemo() {
        System.out.println(HttpRequest.builder("https://api.example.com/orders")
            .method("POST").header("Auth", "Bearer x").timeout(10).build());
    }

    // ===================== STRATEGY (lambda-friendly) =====================
    static void strategyDemo() {
        Map<String, Function<Double, Double>> discountStrategies = Map.of(
            "REGULAR", amt -> amt,
            "GOLD",    amt -> amt * 0.90,
            "PLATINUM",amt -> amt * 0.80);
        String tier = "GOLD";
        System.out.println("payable: " + discountStrategies.get(tier).apply(1000.0)); // 900.0
    }

    // ===================== OBSERVER (pub-sub) =====================
    interface OrderListener { void onOrderPlaced(String orderId); }
    static class OrderService {
        private final List<OrderListener> listeners = new ArrayList<>();
        void subscribe(OrderListener l) { listeners.add(l); }
        void placeOrder(String id) {
            System.out.println("order placed: " + id);
            listeners.forEach(l -> l.onOrderPlaced(id));   // notify all subscribers
        }
    }
    static void observerDemo() {
        var svc = new OrderService();
        svc.subscribe(id -> System.out.println("  email sent for " + id));
        svc.subscribe(id -> System.out.println("  inventory reserved for " + id));
        svc.placeOrder("ORD-1");
    }

    // ===================== DECORATOR (wrap, same interface) =====================
    interface Coffee { double cost(); String desc(); }
    record Espresso() implements Coffee {
        public double cost() { return 100; } public String desc() { return "espresso"; }
    }
    record WithMilk(Coffee base) implements Coffee {
        public double cost() { return base.cost() + 30; } public String desc() { return base.desc() + "+milk"; }
    }
    record WithCaramel(Coffee base) implements Coffee {
        public double cost() { return base.cost() + 50; } public String desc() { return base.desc() + "+caramel"; }
    }
    static void decoratorDemo() {
        Coffee order = new WithCaramel(new WithMilk(new Espresso()));  // stack like java.io!
        System.out.println(order.desc() + " = ₹" + order.cost());     // espresso+milk+caramel = ₹180
    }

    // ===================== FACTORY METHOD =====================
    interface Notification { void send(String msg); }
    static Notification create(String channel) {
        return switch (channel) {                          // creation logic in ONE place
            case "EMAIL" -> msg -> System.out.println("email: " + msg);
            case "SMS"   -> msg -> System.out.println("sms: " + msg);
            case "PUSH"  -> msg -> System.out.println("push: " + msg);
            default      -> throw new IllegalArgumentException(channel);
        };
    }
    static void factoryDemo() { create("SMS").send("OTP 123456"); }
}
