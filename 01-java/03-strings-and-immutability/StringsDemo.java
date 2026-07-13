/**
 * TOPIC: Strings & Immutability — pool traps, builder performance, immutable class,
 * and the classic string coding questions.
 */
import java.util.*;

public class StringsDemo {

    public static void main(String[] args) {
        poolTraps();
        builderPerformance();
        immutableClassDemo();

        // Classic coding questions
        System.out.println(reverse("interview"));                    // weivretni
        System.out.println(isPalindrome("A man, a plan, a canal: Panama")); // true
        System.out.println(firstNonRepeating("swiss"));              // w
        System.out.println(isAnagram("listen", "silent"));           // true
        System.out.println(charFrequency("banana"));                 // {a=3, b=1, n=2}
    }

    static void poolTraps() {
        String a = "ja" + "va";              // compile-time folded -> pool "java"
        String b = "java";
        String c = new String("java");       // heap copy
        String d = "ja"; String e = d + "va";// runtime concat -> heap
        System.out.println(a == b);          // true
        System.out.println(b == c);          // false
        System.out.println(b == e);          // false
        System.out.println(b == e.intern()); // true
    }

    static void builderPerformance() {
        long t = System.nanoTime();
        String s = "";
        for (int i = 0; i < 20_000; i++) s += i;            // O(n^2) — new object each time
        long slow = System.nanoTime() - t;

        t = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20_000; i++) sb.append(i);      // O(n) amortized
        long fast = System.nanoTime() - t;
        System.out.printf("concat: %dms, builder: %dms%n", slow / 1_000_000, fast / 1_000_000);
    }

    /** The canonical "write an immutable class" answer. */
    static void immutableClassDemo() {
        List<String> skills = new ArrayList<>(List.of("Java", "SQL"));
        Employee emp = new Employee("Shilpak", skills);
        skills.add("Hacked!");                       // caller mutates original list...
        System.out.println(emp.skills());            // [Java, SQL] — safe (defensive copy)
    }

    static final class Employee {                    // 1. final class
        private final String name;                   // 2. private final fields
        private final List<String> skills;

        Employee(String name, List<String> skills) {
            this.name = name;
            this.skills = List.copyOf(skills);       // 4. defensive copy IN
        }
        public String name()          { return name; }
        public List<String> skills()  { return skills; } // List.copyOf is already unmodifiable
        // 3. no setters
    }

    // ===== Classic interview problems =====
    static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();   // manual: two-pointer swap on char[]
    }

    static boolean isPalindrome(String s) {
        String clean = s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        int i = 0, j = clean.length() - 1;
        while (i < j) if (clean.charAt(i++) != clean.charAt(j--)) return false;
        return true;
    }

    static char firstNonRepeating(String s) {
        Map<Character, Integer> freq = new LinkedHashMap<>();   // preserves insertion order
        for (char c : s.toCharArray()) freq.merge(c, 1, Integer::sum);
        return freq.entrySet().stream()
                   .filter(e -> e.getValue() == 1)
                   .map(Map.Entry::getKey)
                   .findFirst().orElse('-');
    }

    static boolean isAnagram(String a, String b) {
        char[] x = a.toCharArray(), y = b.toCharArray();
        Arrays.sort(x); Arrays.sort(y);
        return Arrays.equals(x, y);
    }

    static Map<Character, Long> charFrequency(String s) {     // Stream flavor (Java 8 topic!)
        return s.chars().mapToObj(c -> (char) c)
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c, TreeMap::new, java.util.stream.Collectors.counting()));
    }
}
