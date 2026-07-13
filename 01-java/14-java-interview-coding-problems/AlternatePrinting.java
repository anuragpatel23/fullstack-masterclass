/**
 * TOPIC: The classic "print odd/even alternately with two threads" —
 * the go-to test of wait/notify understanding. Plus a Semaphore variant.
 */
import java.util.concurrent.Semaphore;

public class AlternatePrinting {

    public static void main(String[] args) throws InterruptedException {
        waitNotifyVersion();
        semaphoreVersion();
    }

    // ===== Version 1: shared monitor + wait/notify =====
    static class Printer {
        private int current = 1;
        private final int max;
        Printer(int max) { this.max = max; }

        synchronized void printOdd() throws InterruptedException {
            while (current <= max) {
                while (current % 2 == 0) wait();          // not my turn -> release lock & sleep
                if (current > max) break;
                System.out.println("odd-thread : " + current++);
                notifyAll();                               // wake the even thread
            }
        }
        synchronized void printEven() throws InterruptedException {
            while (current <= max) {
                while (current % 2 == 1) wait();
                if (current > max) break;
                System.out.println("even-thread: " + current++);
                notifyAll();
            }
        }
    }

    static void waitNotifyVersion() throws InterruptedException {
        Printer p = new Printer(8);
        Thread odd = new Thread(() -> { try { p.printOdd(); } catch (InterruptedException ignored) {} });
        Thread even = new Thread(() -> { try { p.printEven(); } catch (InterruptedException ignored) {} });
        odd.start(); even.start();
        odd.join(); even.join();
        // Talking points: WHILE not IF (spurious wakeups), notifyAll vs notify,
        // wait() releases the monitor, sleep() does not.
    }

    // ===== Version 2: two Semaphores as turn-taking batons (cleaner, extends to N threads) =====
    static void semaphoreVersion() throws InterruptedException {
        Semaphore oddTurn = new Semaphore(1);    // odd goes first
        Semaphore evenTurn = new Semaphore(0);   // even waits

        Thread odd = new Thread(() -> {
            for (int i = 1; i <= 8; i += 2) {
                try {
                    oddTurn.acquire();
                    System.out.println("sem-odd : " + i);
                    evenTurn.release();          // hand the baton over
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        Thread even = new Thread(() -> {
            for (int i = 2; i <= 8; i += 2) {
                try {
                    evenTurn.acquire();
                    System.out.println("sem-even: " + i);
                    oddTurn.release();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        odd.start(); even.start();
        odd.join(); even.join();
    }
}
