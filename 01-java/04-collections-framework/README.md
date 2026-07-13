# 04 — Collections Framework 🟡⭐ (the #1 Java interview area)

## Real-life analogy
A **library**: an `ArrayList` is a numbered bookshelf (instant access by position, but inserting in the middle means shifting every book after it). A `LinkedList` is a treasure hunt where each book points to the next. A `HashMap` is the catalog desk — tell the librarian the title (key), she hashes it to a shelf number and walks straight there. A `TreeMap` keeps everything alphabetized, so range queries ("all authors A–C") are easy but each insert costs a bit more.

## Hierarchy cheat-sheet
```
Iterable → Collection → List (ArrayList, LinkedList, Vector/Stack)
                      → Set  (HashSet, LinkedHashSet, TreeSet)
                      → Queue/Deque (PriorityQueue, ArrayDeque)
Map (separate!) → HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap
```

## HashMap internals ⭐⭐ (asked in EVERY interview)
1. `put(k,v)`: compute `hash(k)` (key's hashCode **spread**: `h ^ (h >>> 16)` to mix high bits).
2. Index = `(n - 1) & hash` (n = table length, power of 2 — cheap modulo).
3. Empty bucket → new Node. Collision → compare via `equals()`; append to **linked list**.
4. **Java 8 treeification**: list length > 8 (and table ≥ 64) → converts to red-black tree → worst case O(log n) instead of O(n).
5. **Resize**: size > capacity × loadFactor (default 16 × 0.75 = 12) → double table, rehash (each node goes to `index` or `index + oldCap`).
6. Null: one null key allowed (bucket 0), many null values.

⚠️ Mutating a key after insertion "loses" the entry. ⚠️ Pre-Java-8 concurrent resize could create infinite loops — the reason "never use HashMap across threads."

## Comparisons you must nail
| | ArrayList | LinkedList |
|---|---|---|
| get(i) | O(1) | O(n) |
| add/remove middle | O(n) shift | O(1) *after* O(n) traversal |
| memory | compact array | node overhead ×3 |
| verdict | **default choice**; LinkedList rarely wins in practice | |

| | HashMap | Hashtable | ConcurrentHashMap |
|---|---|---|---|
| Thread-safe | ✘ | ✔ (whole-table lock) | ✔ (CAS + per-bin lock, Java 8+) |
| Nulls | 1 null key | ✘ | ✘ |
| Verdict | single-thread | legacy, avoid | concurrent default |

| | HashSet | LinkedHashSet | TreeSet |
|---|---|---|---|
| Order | none | insertion | sorted |
| Backing | HashMap | LinkedHashMap | TreeMap (red-black) |
| Ops | O(1) | O(1) | O(log n) |

## Other must-knows
- **Comparable** (`compareTo`, natural order, in the class) vs **Comparator** (external, many strategies, `Comparator.comparing(...).thenComparing(...)`).
- **Fail-fast** iterators (ArrayList/HashMap — `ConcurrentModificationException` via modCount) vs **fail-safe** (ConcurrentHashMap, CopyOnWriteArrayList — iterate over snapshot). Use `iterator.remove()` or `removeIf` to delete while iterating.
- `Arrays.asList` = fixed-size view; `List.of` = fully immutable + null-hostile.
- `PriorityQueue` = binary heap; peek O(1), poll O(log n). `ArrayDeque` > `Stack`/`LinkedList` for stack/queue.
- `Collections.unmodifiableList` = read-only **view** (original can still change); `List.copyOf` = independent immutable copy.

## Top interview questions
1. **Explain HashMap put/get internals.** (Walk through the 6 steps above — with treeification and resize.)
2. **Why must capacity be a power of 2?** `(n-1) & hash` replaces expensive `%` and spreads bits evenly.
3. **What if two keys have the same hashCode?** Same bucket → equals() disambiguates → chained/tree node.
4. **Why is ConcurrentHashMap faster than Hashtable?** Lock striping/per-bin synchronization + lock-free reads.
5. **How does HashSet check duplicates?** Backed by HashMap — element is the key (hashCode + equals).
6. **ConcurrentModificationException — why and how to avoid?** modCount check; use iterator.remove/removeIf/concurrent collections.
7. **Design an LRU cache.** `LinkedHashMap(capacity, 0.75f, true)` + override `removeEldestEntry` — see code.
8. **TreeMap vs HashMap?** Sorted order + range queries (`headMap`, `ceilingKey`) vs raw O(1) speed.

➡️ Code: [`CollectionsDemo.java`](./CollectionsDemo.java)
