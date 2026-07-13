/**
 * TOPIC: Memory & GC — reference types, a real leak you can watch,
 * WeakHashMap behavior, and the ThreadLocal leak pattern.
 * Run with: java -Xmx64m MemoryDemo   (small heap makes effects visible)
 */
import java.lang.ref.*;
import java.util.*;

public class MemoryDemo {

    public static void main(String[] args) throws Exception {
        referenceStrengths();
        weakHashMapDemo();
        staticCollectionLeak();
        threadLocalPattern();
    }

    /** Strong vs Soft vs Weak references under GC. */
    static void referenceStrengths() {
        byte[] strongData = new byte[1024];                       // strong: survives GC
        SoftReference<byte[]> soft = new SoftReference<>(new byte[1024]);
        WeakReference<byte[]> weak = new WeakReference<>(new byte[1024]);

        System.gc();                                              // hint only!
        sleep(100);
        System.out.println("strong alive: " + (strongData != null));
        System.out.println("soft alive  : " + (soft.get() != null));  // usually true (no pressure)
        System.out.println("weak alive  : " + (weak.get() != null));  // usually false after GC
    }

    /** WeakHashMap: entries vanish when the KEY is no longer strongly referenced. */
    static void weakHashMapDemo() {
        Map<Object, String> metadata = new WeakHashMap<>();
        Object key = new Object();
        metadata.put(key, "attached-info");
        System.out.println("before: size=" + metadata.size());   // 1
        key = null;                                               // drop last strong ref to key
        System.gc(); sleep(100);
        System.out.println("after GC: size=" + metadata.size()); // usually 0 — auto-cleanup ✔
    }

    /** LEAK PATTERN 1: ever-growing static collection. */
    static final List<byte[]> CACHE = new ArrayList<>();          // static root — never collected
    static void staticCollectionLeak() {
        // Simulate a "cache" without eviction — retained set grows forever.
        for (int i = 0; i < 100; i++) CACHE.add(new byte[10_000]);
        System.out.println("leaked ~" + (CACHE.size() * 10_000 / 1024) + " KB held by static root");
        CACHE.clear();  // the fix: bounded cache / eviction (LinkedHashMap LRU, Caffeine)
    }

    /** LEAK PATTERN 2 (and fix): ThreadLocal must be removed in pooled threads. */
    static final ThreadLocal<StringBuilder> CTX =
        ThreadLocal.withInitial(() -> new StringBuilder("request-context"));

    static void threadLocalPattern() throws Exception {
        Runnable handler = () -> {
            try {
                CTX.get().append(":user-42");                      // per-thread state, no locking
                System.out.println(Thread.currentThread().getName() + " -> " + CTX.get());
            } finally {
                CTX.remove();   // ⭐ CRITICAL in thread pools — else value outlives the request
            }
        };
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        pool.submit(handler); pool.submit(handler);
        pool.shutdown();
        pool.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
