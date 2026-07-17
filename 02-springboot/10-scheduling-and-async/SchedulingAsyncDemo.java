/**
 * TOPIC: @Scheduled & @Async — proper pool configuration, cron patterns,
 * async with CompletableFuture, exception handling, distributed-lock note.
 */
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;

public class SchedulingAsyncDemo {

    // ===== 1. Configuration: NEVER ship the defaults ⭐ =====
    @Configuration
    @EnableScheduling
    @EnableAsync
    static class TaskConfig implements AsyncConfigurer {

        /** Scheduler pool: default is ONE thread for ALL @Scheduled methods! */
        @Bean
        ThreadPoolTaskScheduler taskScheduler() {
            var scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(4);                          // jobs no longer block each other
            scheduler.setThreadNamePrefix("sched-");
            scheduler.setErrorHandler(t ->                     // scheduled exceptions are silent by default!
                    System.err.println("[scheduler] job failed: " + t.getMessage()));
            return scheduler;
        }

        /** Dedicated executor for @Async — sized, bounded, with backpressure. */
        @Bean("emailExecutor")
        ThreadPoolTaskExecutor emailExecutor() {
            var ex = new ThreadPoolTaskExecutor();
            ex.setCorePoolSize(4);
            ex.setMaxPoolSize(8);
            ex.setQueueCapacity(100);                          // bounded! unbounded queue = OOM risk
            ex.setThreadNamePrefix("email-");
            ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // backpressure
            ex.initialize();
            return ex;
        }

        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            // catches exceptions from VOID @Async methods (otherwise vanish ⭐)
            return (ex, method, params) ->
                    System.err.println("[async] " + method.getName() + " failed: " + ex.getMessage());
        }
    }

    // ===== 2. Scheduled jobs: every trigger style =====
    @Service
    static class MaintenanceJobs {

        @Scheduled(fixedRate = 30_000)                         // every 30s, start-to-start
        public void refreshCache() { /* ... */ }

        @Scheduled(fixedDelay = 60_000, initialDelay = 10_000) // 60s AFTER previous run ENDS
        public void pollExternalSystem() { /* ... */ }

        // Spring cron = 6 fields: sec min hour day-of-month month day-of-week ⭐
        @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Kolkata")   // 02:00 IST daily
        public void nightlyArchive() {
            try {
                /* archive rows */
            } catch (Exception e) {
                // ALWAYS catch+log in jobs: an uncaught exception is swallowed,
                // the schedule survives, but you'll never know it failed.
            }
        }

        @Scheduled(cron = "0 */15 9-18 * * MON-FRI")           // every 15 min, business hours
        public void syncOrders() { /* ... */ }

        /*
         * MULTI-INSTANCE NOTE ⭐: with 3 replicas this fires 3×.
         * Production fix — ShedLock:
         *   @Scheduled(cron = "0 0 2 * * *")
         *   @SchedulerLock(name = "nightlyArchive", lockAtMostFor = "50m")
         * One pod acquires a DB/Redis lock; others skip this run.
         */
    }

    // ===== 3. @Async patterns =====
    @Service
    static class NotificationService {

        /** Fire-and-forget — exceptions go to AsyncUncaughtExceptionHandler. */
        @Async("emailExecutor")
        public void sendEmail(String to, String body) {
            System.out.println(Thread.currentThread().getName() + " sending email to " + to);
        }

        /** Result-returning — exceptions surface through the future ⭐. */
        @Async("emailExecutor")
        public CompletableFuture<String> renderTemplate(String template) {
            return CompletableFuture.completedFuture("rendered:" + template);
        }
    }

    @Service
    static class CheckoutService {
        private final NotificationService notifications;
        CheckoutService(NotificationService n) { notifications = n; }

        public void checkout(String userEmail) {
            // 1. Critical path: save order synchronously (with its own @Transactional)
            // 2. Non-critical: fan out async work — caller returns immediately
            notifications.sendEmail(userEmail, "Order confirmed!");

            // Parallel fan-out + join when you DO need results:
            var invoice = notifications.renderTemplate("invoice");
            var receipt = notifications.renderTemplate("receipt");
            CompletableFuture.allOf(invoice, receipt).join();
            System.out.println(invoice.join() + " & " + receipt.join());
        }

        // ❌ ANTI-PATTERN: calling this.sendEmailLocal() where sendEmailLocal is @Async
        //    -> self-invocation bypasses the proxy -> runs SYNCHRONOUSLY, silently.
    }
}
