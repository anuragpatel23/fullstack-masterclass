# 02 — Joins Mastery 🟡⭐⭐

## Real-life analogy
Joins are **matching two guest lists at a wedding**: the bride's list (table A) and groom's list (table B). **INNER JOIN** = guests on *both* lists. **LEFT JOIN** = everyone from the bride's list, with groom-side details filled in *if they exist, blanks (NULLs) otherwise*. **FULL OUTER** = everyone from either list. **CROSS JOIN** = every bride-guest paired with every groom-guest (a dance-pairing chart — m×n). **SELF JOIN** = matching the employee list *against itself* to find each person's mentor, who is also on the same list.

## The join types ⭐⭐
| Join | Returns |
|---|---|
| `INNER JOIN` | only matching rows |
| `LEFT [OUTER] JOIN` | all left rows + matches (NULLs when none) ⭐ |
| `RIGHT [OUTER] JOIN` | mirror (rarely used — rewrite as LEFT) |
| `FULL [OUTER] JOIN` | all rows both sides |
| `CROSS JOIN` | Cartesian product m×n ⭐ |
| `SELF JOIN` | table joined to itself (aliases mandatory) ⭐ |
| Anti-join | rows in A with NO match in B (`LEFT JOIN ... WHERE b.id IS NULL` / `NOT EXISTS`) ⭐⭐ |
| Semi-join | rows in A having ≥1 match (`EXISTS`) — no duplicates, unlike INNER ⭐ |

Oracle legacy syntax (recognize in old codebases): `WHERE e.dept_id = d.dept_id(+)` = LEFT JOIN.

## THE trap: WHERE vs ON for outer joins ⭐⭐ (the #1 join interview filter)
```sql
FROM a LEFT JOIN b ON a.id = b.a_id AND b.status = 'ACTIVE'   -- filter DURING join: keeps all a
FROM a LEFT JOIN b ON a.id = b.a_id WHERE b.status = 'ACTIVE' -- filter AFTER: NULLs fail → INNER!
```
Putting a right-table condition in WHERE silently converts LEFT → INNER. Every senior interview probes this.

## More things they probe
- **Row multiplication** ⭐: joining 1→N duplicates parent data — `SUM` after such a join double-counts (fix: aggregate in a subquery/CTE first, then join).
- Join on NULL keys: NULL never equals NULL → those rows silently drop out of INNER joins.
- Duplicate column names → always alias tables and qualify columns.
- `USING(col)` vs `ON a.col = b.col`; NATURAL JOIN (never use — implicit ⭐).
- **How the engine executes**: nested loops (small/indexed), hash join (big unsorted ⭐), sort-merge — name them for senior roles.

## Classic puzzles (all solved in code)
1. Employees with their department names (+ keep dept-less employees).
2. Departments with NO employees ⭐ (anti-join, 3 ways).
3. Employees earning more than their manager ⭐ (self join — the LeetCode 181 classic).
4. Pairs of employees in the same department (self join, avoid duplicates with `<`).
5. Customers who never ordered (anti-join pattern).
6. Second table row counts per parent WITHOUT losing zero-count parents (LEFT + GROUP BY ⭐).

## Top interview questions
1. **Explain each join with the guest-list analogy + when you'd use it.**
2. **ON vs WHERE for a LEFT JOIN filter?** ⭐⭐ (the silent INNER conversion)
3. **Table A has 5 rows, B has 3, join on a non-unique key — max/min rows out?** (INNER: 0–15; CROSS: 15; understand multiplication ⭐)
4. **Find departments with no employees — three ways.** NOT EXISTS / LEFT-IS NULL / NOT IN (+ its NULL trap).
5. **Employees earning more than their managers?** (self join)
6. **INNER JOIN vs EXISTS — difference?** Duplicates + projection; semi-join stops at first match.
7. **Why did your SUM double after adding a join?** Row multiplication — pre-aggregate.
8. **Hash join vs nested loops?** Big-table equijoins vs small driving set with index.

➡️ Code: [`joins.sql`](./joins.sql)
