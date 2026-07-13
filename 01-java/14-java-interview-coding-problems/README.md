# 14 — Java Interview Coding Problems ⭐ (the rapid-fire round)

These are the problems product companies actually ask in the **Java machine-coding / pair-programming round** — distinct from DSA rounds. They test *idiomatic Java*, not algorithms.

## The problem bank (all solved in the code files)

### Tier 1 — must be able to write blind
1. Reverse a string / words in a sentence (in place).
2. Check palindrome, anagram.
3. First non-repeating character.
4. Find duplicates in an array/list (Set, frequency map, and Stream `groupingBy` versions).
5. Second highest number / Nth highest — loop AND stream versions.
6. Fibonacci & factorial — iterative, recursive, memoized.
7. Swap two numbers without temp; count digits; prime check; Armstrong number.

### Tier 2 — Java-idiom testers
8. **Write an immutable class** (with a mutable field — defensive copies).
9. **Thread-safe Singleton** — DCL + enum (from patterns topic).
10. **Producer-consumer** — wait/notify (from concurrency topic).
11. Sort employees by multiple fields with Comparator chains.
12. **Stream one-liners**: group by dept, avg salary, join names, flatten skills, partition, top-N.
13. Convert `List<Employee>` → `Map<id, Employee>` (and handle duplicate keys ⭐ — `toMap` 3-arg merge).
14. Custom `equals`/`hashCode` and explain the contract.
15. Print odd/even (or 1-2-3) alternately with two/three threads ⭐ (synchronization test).

### Tier 3 — design-flavored
16. **Implement your own HashMap** (array of buckets + chaining) ⭐ — see `MyHashMap.java`.
17. **LRU cache** — LinkedHashMap version and doubly-linked-list + HashMap version.
18. Custom ArrayList (grow-on-demand array).
19. Rate limiter sketch (token bucket), BoundedBlockingQueue using wait/notify.
20. Parse & process a CSV of orders with streams (grouping, summing) — mimics real machine-coding.

## How interviewers evaluate (from the other side of the table)
- Do you reach for the right collection without prompting? (frequency → HashMap, order-preserving dedup → LinkedHashSet, top-N → PriorityQueue)
- Do you handle edge cases out loud? (empty, single element, nulls, duplicates)
- Do you know both the "loop way" and the "stream way" and when each reads better?
- In concurrency problems: while-loop around wait, notifyAll vs notify, volatile vs synchronized choices.

## Files
- [`CodingProblems.java`](./CodingProblems.java) — Tiers 1 & 2 solved with commentary.
- [`MyHashMap.java`](./MyHashMap.java) — build-your-own HashMap with chaining + resize.
- [`AlternatePrinting.java`](./AlternatePrinting.java) — odd/even threads classic.
