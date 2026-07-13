/**
 * TOPIC: Concurrency — race conditions & fixes, producer-consumer (both styles),
 * executor pools, CompletableFuture pipelines, deadlock demo+fix.
 */
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyDemo {

    public static void main(String[] args) throws Exception {
        raceConditionAndFixes();
        producerConsumerClassic();
        producerConsumerBlockingQueue();
        executorDemo();
        completableFutureDemo();
        deadlockAndFix();
    }

    /** 1. The classic lost-update race + three fixes. */
    static int unsafeCount = 0;
    static int syncCount = 0;
    static final AtomicInteger atomicCount = new AtomicInteger();
    static final Object LOCK = new Object();

    static void raceConditionAndFixes() throws InterruptedException {
        Runnable work = () -> {
            for (int i = 0; i < 100_000; i++) {
                unsafeCount++;                                  // NOT atomic: read+add+write
                synchronized (LOCK) { syncCount++; }            // fix 1: mutual exclusion
                atomicCount.incrementAndGet();                  // fix 2: lock-free CAS
            }
        };
        Thread t1 = new Thread(work), t2 = new Thread(work);
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.printf("unsafe=%d (lost updates!), sync=%d, atomic=%d%n",
                unsafeCount, syncCount, atomicCount.get());
    }

    /** 2a. Producer-consumer with wait/notify — the whiteboard classic. */
    static void producerConsumerClassic() throws InterruptedException {
        class Buffer {
            private final java.util.Queue<Integer> q = new java.util.ArrayDeque<>();
            private final int capacity = 3;
            synchronized void put(int x) throws InterruptedException {
                while (q.size() == capacity) wait();     // WHILE, not if (spurious wakeups)
                q.add(x);
                notifyAll();                             // wake consumers
            }
            synchronized int take() throws InterruptedException {
                while (q.isEmpty()) wait();
                int x = q.poll();
                notifyAll();                             // wake producers
                return x;
            }
        }
        Buffer buf = new Buffer();
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) buf.put(i); } catch (InterruptedException ignored) {}
        });
        Thread consumer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) System.out.println("consumed " + buf.take()); }
            catch (InterruptedException ignored) {}
        });
        producer.start(); consumer.start(); producer.join(); consumer.join();
    }

    /** 2b. Same thing, modern style — BlockingQueue does the waiting for you. */
    static void producerConsumerBlockingQueue() throws InterruptedException {
        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(3);
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) q.put(i * 10); } catch (InterruptedException ignored) {}
        });
        Thread consumer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) System.out.println("BQ consumed " + q.take()); }
            catch (InterruptedException ignored) {}
        });
        producer.start(); consumer.start(); producer.join(); consumer.join();
    }

    /** 3. ExecutorService + Callable + Future. */
    static void executorDemo() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            Callable<Integer> task = () -> {
                TimeUnit.MILLISECONDS.sleep(50);
                return ThreadLocalRandom.current().nextInt(100);
            };
            var futures = pool.invokeAll(java.util.List.of(task, task, task));
            for (Future<Integer> f : futures) System.out.println("result: " + f.get());
        } finally {
            pool.shutdown();                              // ALWAYS shut down
        }
    }

    /** 4. CompletableFuture pipeline: compose, combine, recover. */
    static void completableFutureDemo() throws Exception {
        CompletableFuture<String> user =
            CompletableFuture.supplyAsync(() -> "user-42")
                .thenCompose(id -> CompletableFuture.supplyAsync(() -> id + ":Shilpak")) // flatMap
                .exceptionally(ex -> "fallback-user");                                    // recover

        CompletableFuture<Integer> orders = CompletableFuture.supplyAsync(() -> 7);

        // Combine two independent async results (parallel fan-out, then join)
        String summary = user.thenCombine(orders, (u, o) -> u + " has " + o + " orders").get();
        System.out.println(summary);
    }

    /** 5. Deadlock demo (with timeout escape) + the fix: consistent lock ordering. */
    static void deadlockAndFix() throws InterruptedException {
        Object a = new Object(), b = new Object();
        // FIX shown: both threads acquire in the SAME order (a then b) -> no deadlock.
        Runnable t1 = () -> { synchronized (a) { sleep(10); synchronized (b) { print("t1 done"); } } };
        Runnable t2 = () -> { synchronized (a) { sleep(10); synchronized (b) { print("t2 done"); } } };
        // The DEADLOCK version would be t2 locking (b) then (a) — circular wait.
        Thread x = new Thread(t1), y = new Thread(t2);
        x.start(); y.start(); x.join(); y.join();
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
    static void print(String s) { System.out.println(s); }
}
