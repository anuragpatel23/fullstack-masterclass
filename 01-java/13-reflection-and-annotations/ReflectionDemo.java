/**
 * TOPIC: Reflection & Annotations — custom annotations + a MINI DI CONTAINER
 * that mimics what Spring does with @Component/@Autowired/@Transactional.
 */
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionDemo {

    // ===== 1. Custom annotations (RUNTIME retention = readable via reflection) =====
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    @interface MiniComponent {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface MiniAutowired {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface MiniTransactional {}

    // ===== 2. Application classes using our annotations =====
    @MiniComponent
    static class OrderRepository {
        void save(String order) { System.out.println("    [repo] saved " + order); }
    }

    @MiniComponent
    static class OrderService {
        @MiniAutowired OrderRepository repository;      // private-ish field, no setter!

        @MiniTransactional
        void placeOrder(String order) {
            repository.save(order);
        }
    }

    // ===== 3. The mini container: scan -> instantiate -> inject -> proxy =====
    static class MiniContainer {
        private final Map<Class<?>, Object> beans = new HashMap<>();

        void scan(Class<?>... candidates) throws Exception {
            // Phase 1: create instances of @MiniComponent classes (like component scan)
            for (Class<?> c : candidates) {
                if (c.isAnnotationPresent(MiniComponent.class)) {
                    Constructor<?> ctor = c.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    beans.put(c, ctor.newInstance());
                    System.out.println("[container] registered bean: " + c.getSimpleName());
                }
            }
            // Phase 2: field injection (like @Autowired)
            for (Object bean : beans.values()) {
                for (Field f : bean.getClass().getDeclaredFields()) {
                    if (f.isAnnotationPresent(MiniAutowired.class)) {
                        f.setAccessible(true);                       // works on private fields
                        f.set(bean, beans.get(f.getType()));
                        System.out.println("[container] injected " + f.getType().getSimpleName()
                                + " into " + bean.getClass().getSimpleName());
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> type) { return (T) beans.get(type); }

        /** Phase 3: wrap @MiniTransactional methods — EXACTLY how Spring AOP proxies work. */
        Object transactionalInvoke(Object bean, String methodName, Object... args) throws Exception {
            Method m = Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(x -> x.getName().equals(methodName)).findFirst().orElseThrow();
            if (m.isAnnotationPresent(MiniTransactional.class)) {
                System.out.println("  [tx] BEGIN transaction");
                try {
                    Object result = m.invoke(bean, args);            // reflective call
                    System.out.println("  [tx] COMMIT");
                    return result;
                } catch (InvocationTargetException e) {
                    System.out.println("  [tx] ROLLBACK — " + e.getCause().getMessage());
                    throw e;
                }
            }
            return m.invoke(bean, args);
        }
    }

    public static void main(String[] args) throws Exception {
        var container = new MiniContainer();
        container.scan(OrderRepository.class, OrderService.class);

        OrderService service = container.getBean(OrderService.class);
        container.transactionalInvoke(service, "placeOrder", "ORD-77");

        inspectAnything();
        breakSingletonWithReflection();
    }

    /** Generic inspection — what debuggers and serializers do. */
    static void inspectAnything() {
        record Point(int x, int y) {}
        Object obj = new Point(3, 4);
        System.out.println("\nInspecting " + obj.getClass().getSimpleName() + ":");
        for (Field f : obj.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                System.out.println("  " + f.getType().getSimpleName() + " " + f.getName() + " = " + f.get(obj));
            } catch (Exception ignored) {}
        }
    }

    /** Classic question: reflection breaks (non-enum) singletons. */
    static class NaiveSingleton {
        private static final NaiveSingleton INSTANCE = new NaiveSingleton();
        private NaiveSingleton() {}
        static NaiveSingleton getInstance() { return INSTANCE; }
    }
    static void breakSingletonWithReflection() throws Exception {
        Constructor<NaiveSingleton> ctor = NaiveSingleton.class.getDeclaredConstructor();
        ctor.setAccessible(true);                              // bypass private!
        NaiveSingleton hacked = ctor.newInstance();
        System.out.println("\nsingleton broken: " + (hacked != NaiveSingleton.getInstance())); // true
        // Fix: throw from constructor if INSTANCE != null, or use enum singleton.
    }
}
