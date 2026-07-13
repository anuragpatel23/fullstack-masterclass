/**
 * TOPIC: Java 8 — every stream pattern interviewers actually ask,
 * plus Optional and Date-Time API essentials.
 */
import java.time.*;
import java.util.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;

public class Java8Demo {

    record Employee(String name, String dept, double salary, List<String> skills) {}

    static final List<Employee> EMPS = List.of(
        new Employee("Asha",  "IT",      95_000, List.of("Java", "SQL")),
        new Employee("Ravi",  "HR",      55_000, List.of("Excel")),
        new Employee("Meera", "IT",     120_000, List.of("Java", "React")),
        new Employee("Kiran", "Finance", 80_000, List.of("SQL")),
        new Employee("Divya", "IT",      95_000, List.of("React", "SQL")));

    public static void main(String[] args) {
        streamPatterns();
        lazinessProof();
        optionalPatterns();
        dateTimeEssentials();
    }

    static void streamPatterns() {
        // 1. GROUPING — the most asked pattern
        Map<String, Double> avgByDept = EMPS.stream()
            .collect(groupingBy(Employee::dept, averagingDouble(Employee::salary)));
        System.out.println("avg salary by dept: " + avgByDept);

        // 2. Highest-paid PER department (groupingBy + downstream max)
        Map<String, Optional<Employee>> topByDept = EMPS.stream()
            .collect(groupingBy(Employee::dept, maxBy(Comparator.comparingDouble(Employee::salary))));
        topByDept.forEach((d, e) -> System.out.println(d + " top earner: " + e.map(Employee::name).orElse("-")));

        // 3. Second-highest DISTINCT salary ⭐
        Optional<Double> second = EMPS.stream().map(Employee::salary).distinct()
            .sorted(Comparator.reverseOrder()).skip(1).findFirst();
        System.out.println("2nd highest salary: " + second.orElse(0.0));

        // 4. flatMap: all unique skills across employees ⭐
        Set<String> skills = EMPS.stream()
            .flatMap(e -> e.skills().stream())      // Stream<List<String>> -> Stream<String>
            .collect(toCollection(TreeSet::new));
        System.out.println("all skills: " + skills);

        // 5. partitioningBy: boolean split
        Map<Boolean, List<String>> highPaid = EMPS.stream()
            .collect(partitioningBy(e -> e.salary() >= 90_000, mapping(Employee::name, toList())));
        System.out.println("high paid: " + highPaid.get(true) + ", rest: " + highPaid.get(false));

        // 6. joining + counting
        System.out.println("IT team: " + EMPS.stream().filter(e -> e.dept().equals("IT"))
            .map(Employee::name).collect(joining(", ", "[", "]")));
        System.out.println("headcount: " + EMPS.stream().collect(groupingBy(Employee::dept, counting())));

        // 7. reduce: total payroll (and the primitive-stream alternative)
        double total = EMPS.stream().map(Employee::salary).reduce(0.0, Double::sum);
        double total2 = EMPS.stream().mapToDouble(Employee::salary).sum();  // no boxing
        System.out.println("payroll: " + total + " == " + total2);

        // 8. First non-repeated character via streams (classic combo question)
        String word = "swiss";
        word.chars().mapToObj(c -> (char) c)
            .collect(groupingBy(c -> c, LinkedHashMap::new, counting()))
            .entrySet().stream().filter(e -> e.getValue() == 1).map(Map.Entry::getKey)
            .findFirst().ifPresent(c -> System.out.println("first unique char: " + c));
    }

    /** Streams are LAZY + short-circuiting: watch how few elements are processed. */
    static void lazinessProof() {
        Optional<Integer> first = Stream.of(1, 2, 3, 4, 5)
            .peek(n -> System.out.println("  inspecting " + n))   // runs only 3 times, not 5
            .filter(n -> n % 3 == 0)
            .findFirst();
        System.out.println("lazy result: " + first.orElse(-1));
    }

    static void optionalPatterns() {
        Optional<Employee> found = EMPS.stream().filter(e -> e.name().equals("Meera")).findFirst();

        // Chain instead of null checks
        String dept = found.map(Employee::dept).orElse("UNKNOWN");
        System.out.println("Meera's dept: " + dept);

        // orElse vs orElseGet — the production trap
        Optional<String> present = Optional.of("cached");
        present.orElse(expensiveDefault());          // expensiveDefault RUNS anyway!
        present.orElseGet(Java8Demo::expensiveDefault); // does NOT run — lazy ✔
    }
    static String expensiveDefault() { System.out.println("  (expensive default computed)"); return "computed"; }

    static void dateTimeEssentials() {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusWeeks(2);
        System.out.println("days to prepare: " + Period.between(today, deadline).getDays());

        ZonedDateTime meetingIST = ZonedDateTime.of(
            LocalDate.now(), LocalTime.of(18, 30), ZoneId.of("Asia/Kolkata"));
        System.out.println("in New York: " + meetingIST.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalTime());

        // DateTimeFormatter is THREAD-SAFE (SimpleDateFormat is not — interview point)
        System.out.println(today.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
    }
}
