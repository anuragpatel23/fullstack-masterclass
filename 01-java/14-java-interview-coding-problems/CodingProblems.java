/**
 * TOPIC: The rapid-fire Java coding problems — loop AND stream flavors.
 */
import java.util.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;

public class CodingProblems {

    record Employee(int id, String name, String dept, double salary) {}
    static final List<Employee> EMPS = List.of(
        new Employee(1, "Asha", "IT", 95_000), new Employee(2, "Ravi", "HR", 55_000),
        new Employee(3, "Meera", "IT", 120_000), new Employee(4, "Kiran", "Fin", 80_000));

    public static void main(String[] args) {
        // 1. Reverse words in a sentence (words reversed, not letters)
        String s = "crack the java interview";
        List<String> words = Arrays.asList(s.split(" "));
        Collections.reverse(words);
        System.out.println(String.join(" ", words));                 // interview java the crack

        // 2. Duplicates in an array — three ways
        int[] arr = {4, 2, 7, 2, 4, 9, 4};
        Set<Integer> seen = new HashSet<>(), dups = new HashSet<>();
        for (int n : arr) if (!seen.add(n)) dups.add(n);             // Set trick: add() returns false on dup
        System.out.println("dups: " + dups);                         // [2, 4]

        Map<Integer, Long> withCounts = Arrays.stream(arr).boxed()
            .collect(groupingBy(n -> n, counting()));
        System.out.println("dup counts: " + withCounts.entrySet().stream()
            .filter(e -> e.getValue() > 1).collect(toList()));       // stream flavor

        // 3. Second highest — no library sort (interviewers often ban sorting)
        int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
        for (int n : arr) {
            if (n > first)        { second = first; first = n; }
            else if (n > second && n != first) second = n;
        }
        System.out.println("second highest: " + second);             // 7

        // 4. Fibonacci — memoized recursion
        long[] memo = new long[50];
        System.out.println("fib(40) = " + fib(40, memo));

        // 5. Swap without temp
        int a = 5, b = 9;
        a = a + b; b = a - b; a = a - b;                             // or XOR: a^=b; b^=a; a^=b;
        System.out.println("swapped: a=" + a + " b=" + b);

        // 6. List<Employee> -> Map<id, name> — and the DUPLICATE-KEY trap ⭐
        Map<Integer, String> byId = EMPS.stream()
            .collect(toMap(Employee::id, Employee::name));           // throws on duplicate keys!
        Map<String, Double> maxByDept = EMPS.stream()
            .collect(toMap(Employee::dept, Employee::salary, Double::max));  // 3-arg merge fixes it
        System.out.println(byId + " | dept max: " + maxByDept);

        // 7. Top-N (top 2 salaries) with a min-heap — O(n log k)
        PriorityQueue<Employee> heap = new PriorityQueue<>(Comparator.comparingDouble(Employee::salary));
        for (Employee e : EMPS) { heap.offer(e); if (heap.size() > 2) heap.poll(); }
        System.out.println("top-2 paid: " + heap.stream().map(Employee::name).collect(toList()));

        // 8. Multi-field sort
        List<Employee> sorted = EMPS.stream()
            .sorted(Comparator.comparing(Employee::dept)
                              .thenComparing(Employee::salary, Comparator.reverseOrder()))
            .collect(toList());
        System.out.println(sorted.stream().map(Employee::name).collect(joining(" > ")));

        // 9. Count word frequency in a sentence (order of first appearance)
        String text = "to be or not to be that is the question to be";
        Map<String, Long> freq = Arrays.stream(text.split(" "))
            .collect(groupingBy(w -> w, LinkedHashMap::new, counting()));
        System.out.println("most frequent: " +
            Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey());  // to (or be)

        // 10. Prime check + Armstrong (classic warm-ups)
        System.out.println("17 prime? " + isPrime(17) + ", 153 armstrong? " + isArmstrong(153));

        // 11. Missing number 1..n (sum formula — O(1) space)
        int[] nums = {1, 2, 4, 5, 6};
        int n = 6, missing = n * (n + 1) / 2 - Arrays.stream(nums).sum();
        System.out.println("missing: " + missing);                   // 3
    }

    static long fib(int n, long[] memo) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];
        return memo[n] = fib(n - 1, memo) + fib(n - 2, memo);
    }

    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    static boolean isArmstrong(int n) {
        int digits = String.valueOf(n).length(), sum = 0;
        for (int t = n; t > 0; t /= 10) sum += (int) Math.pow(t % 10, digits);
        return sum == n;
    }
}
