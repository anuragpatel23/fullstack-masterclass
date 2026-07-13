/**
 * TOPIC: Collections — internals traps, comparators, fail-fast, LRU cache design.
 */
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionsDemo {

    public static void main(String[] args) {
        hashMapCollisionDemo();
        mutableKeyTrap();
        failFastVsFailSafe();
        comparatorPatterns();
        lruCacheDemo();
        frequencyAndDedup();
    }

    /** Force collisions to show chaining + why equals() matters. */
    static void hashMapCollisionDemo() {
        record BadHash(String name) {
            @Override public int hashCode() { return 42; }   // every key -> same bucket
        }
        Map<BadHash, Integer> map = new HashMap<>();
        for (int i = 0; i < 5; i++) map.put(new BadHash("k" + i), i);
        // Still works correctly (equals disambiguates) — just degrades to O(n)/O(log n)
        System.out.println("collided map size: " + map.size() + ", k3=" + map.get(new BadHash("k3")));
    }

    /** TRAP: mutate a key after put -> entry becomes unreachable. */
    static void mutableKeyTrap() {
        List<String> key = new ArrayList<>(List.of("a"));
        Map<List<String>, String> map = new HashMap<>();
        map.put(key, "value");
        key.add("b");                                    // hashCode changed!
        System.out.println("after mutating key: " + map.get(key));            // null
        System.out.println("original key form:  " + map.get(List.of("a")));   // null too — lost
    }

    static void failFastVsFailSafe() {
        // FAIL-FAST: ConcurrentModificationException
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            for (Integer i : list) if (i == 2) list.remove(i);   // structural change mid-iteration
        } catch (ConcurrentModificationException e) {
            System.out.println("fail-fast triggered ✔");
        }
        // Correct ways:
        list.removeIf(i -> i == 2);
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) if (it.next() == 3) it.remove();
        System.out.println("after safe removal: " + list);       // [1, 4]

        // FAIL-SAFE: ConcurrentHashMap iterates without exception
        Map<String, Integer> chm = new ConcurrentHashMap<>(Map.of("a", 1, "b", 2));
        for (String k : chm.keySet()) chm.put("new-" + k, 0);    // no exception
        System.out.println("fail-safe survived ✔ size=" + chm.size());
    }

    static void comparatorPatterns() {
        record Emp(String name, String dept, double salary) {}
        List<Emp> emps = new ArrayList<>(List.of(
            new Emp("Asha", "IT", 90), new Emp("Ravi", "HR", 70),
            new Emp("Meera", "IT", 90), new Emp("Kiran", "HR", 85)));

        // Multi-level sort: dept asc, then salary desc, then name
        emps.sort(Comparator.comparing(Emp::dept)
                            .thenComparing(Emp::salary, Comparator.reverseOrder())
                            .thenComparing(Emp::name));
        System.out.println(emps);
    }

    /** Interview classic: LRU cache in ~6 lines using LinkedHashMap. */
    static void lruCacheDemo() {
        var cache = new LinkedHashMap<Integer, String>(16, 0.75f, /*accessOrder=*/true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Integer, String> e) {
                return size() > 3;                       // capacity 3
            }
        };
        cache.put(1, "A"); cache.put(2, "B"); cache.put(3, "C");
        cache.get(1);                                    // touch 1 -> most recent
        cache.put(4, "D");                               // evicts 2 (least recently used)
        System.out.println("LRU state: " + cache.keySet());   // [3, 1, 4]
    }

    static void frequencyAndDedup() {
        int[] nums = {3, 1, 3, 2, 1, 3};
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);
        System.out.println("freq: " + freq);                       // {1=2, 2=1, 3=3}

        // Dedup preserving order: LinkedHashSet
        System.out.println(new LinkedHashSet<>(List.of(3, 1, 3, 2, 1))); // [3, 1, 2]

        // PriorityQueue: top-2 frequent elements
        PriorityQueue<Map.Entry<Integer, Integer>> heap =
            new PriorityQueue<>(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        heap.addAll(freq.entrySet());
        System.out.println("most frequent: " + heap.poll().getKey());   // 3
    }
}
