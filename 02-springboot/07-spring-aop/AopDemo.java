/**
 * TOPIC: AOP — practical aspects you can defend in an interview:
 * timing (@Around), audit (@annotation pointcut), retry, exception logging.
 */
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.*;
import java.lang.annotation.*;

public class AopDemo {

    // ===== 0. A custom annotation to target (the cleanest pointcut style) =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Audited { String action(); }

    // ===== 1. Business code stays PURE — no logging/timing noise =====
    @Service
    static class TransferService {
        @Audited(action = "MONEY_TRANSFER")
        public void transfer(String from, String to, double amount) {
            if (amount > 100_000) throw new IllegalArgumentException("limit exceeded");
            System.out.println("core logic: moved " + amount);
        }
    }

    // ===== 2. Timing aspect — the canonical @Around =====
    @Aspect
    @Component
    @Order(1)                                     // lower = OUTERMOST wrapper
    static class TimingAspect {

        /** Shared pointcut definition — reuse across advices. */
        @Pointcut("execution(* AopDemo.TransferService.*(..))")
        void serviceMethods() {}

        @Around("serviceMethods()")
        Object time(ProceedingJoinPoint pjp) throws Throwable {
            long start = System.nanoTime();
            try {
                return pjp.proceed();             // <- invokes target (or next aspect)
            } finally {
                System.out.printf("[timing] %s took %d µs%n",
                        pjp.getSignature().toShortString(), (System.nanoTime() - start) / 1000);
            }
        }
    }

    // ===== 3. Audit aspect — bind the annotation itself to get its attributes ⭐ =====
    @Aspect
    @Component
    @Order(2)                                     // runs INSIDE TimingAspect
    static class AuditAspect {

        @Before("@annotation(audited)")           // 'audited' param = the actual annotation instance
        void beforeAudited(JoinPoint jp, Audited audited) {
            System.out.printf("[audit] action=%s method=%s args=%s%n",
                    audited.action(), jp.getSignature().getName(),
                    java.util.Arrays.toString(jp.getArgs()));
        }

        @AfterReturning(pointcut = "@annotation(audited)", returning = "result", argNames = "audited,result")
        void afterSuccess(Audited audited, Object result) {
            System.out.println("[audit] " + audited.action() + " SUCCEEDED");
        }

        @AfterThrowing(pointcut = "@annotation(audited)", throwing = "ex", argNames = "audited,ex")
        void afterFailure(Audited audited, Throwable ex) {
            System.out.println("[audit] " + audited.action() + " FAILED: " + ex.getMessage());
        }
    }

    // ===== 4. Retry aspect — why @Around is irreplaceable (calls proceed() N times) =====
    @Aspect
    @Component
    static class RetryAspect {
        @Around("execution(* *..ExternalClient.*(..))")
        Object retry(ProceedingJoinPoint pjp) throws Throwable {
            RuntimeException last = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    return pjp.proceed();                    // may run multiple times!
                } catch (RuntimeException e) {
                    last = e;
                    System.out.println("[retry] attempt " + attempt + " failed: " + e.getMessage());
                    Thread.sleep(100L * attempt);            // backoff
                }
            }
            throw last;
        }
    }

    @Component
    static class ExternalClient {
        private int calls = 0;
        public String flakyCall() {
            if (++calls < 3) throw new IllegalStateException("timeout");
            return "success on call " + calls;
        }
    }

    /*
     * EXECUTION ORDER for transfer() with both aspects (Order 1 outer, 2 inner):
     *   TimingAspect.around BEGIN
     *     AuditAspect.before
     *       transfer() body
     *     AuditAspect.afterReturning / afterThrowing
     *   TimingAspect.around END (finally)
     *
     * DEBUG CHECKLIST when an aspect doesn't fire:
     * 1. self-invocation? 2. private/final method? 3. bean actually Spring-managed?
     * 4. pointcut typo (test with `within`)? 5. @EnableAspectJAutoProxy / starter-aop present?
     */
}
