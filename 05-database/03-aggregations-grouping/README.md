# 03 — Aggregations & Grouping 🟡⭐

## Real-life analogy
`GROUP BY` is **sorting exam papers into piles by class section**: once papers are in piles, you can only make statements about **whole piles** (average per section, count, max) — you can no longer talk about one specific paper (individual row) unless it's a property of the pile. That's exactly why **selecting a non-grouped column errors** ⭐: "what's THE student name of pile 10-A?" is a nonsense question. `HAVING` is **discarding entire piles** after they're formed ("only sections averaging above 80"), while `WHERE` removed individual papers before piling.

## Aggregate functions
`COUNT SUM AVG MIN MAX` + `LISTAGG(name, ',') WITHIN GROUP (ORDER BY name)` (Oracle string-agg ⭐), `STDDEV`, `MEDIAN`.
- All ignore NULLs except `COUNT(*)` ⭐ → `AVG(col)` ≠ `SUM(col)/COUNT(*)` when NULLs exist.
- `COUNT(DISTINCT col)`; conditional aggregation: `SUM(CASE WHEN ... THEN 1 ELSE 0 END)` ⭐⭐ — the pivot workhorse.

## GROUP BY rules ⭐
1. Every SELECT column must be **grouped or aggregated** (ORA-00979).
2. WHERE → before grouping (can't use aggregates ⭐); HAVING → after (can).
3. Group by multiple columns = piles per combination.
4. `GROUP BY` + NULLs: all NULLs form ONE group ⭐ (unlike joins where NULL≠NULL!).

## WHERE vs HAVING ⭐⭐ (guaranteed)
```sql
SELECT dept_id, AVG(salary)
FROM   employees
WHERE  hire_date > DATE '2018-01-01'   -- filter ROWS first (cheap, index-friendly)
GROUP BY dept_id
HAVING AVG(salary) > 80000;            -- filter GROUPS after
```
Rule of thumb: filter rows in WHERE whenever possible — smaller piles, faster query.

## ROLLUP / CUBE / GROUPING SETS (senior flavor)
- `GROUP BY ROLLUP(dept, job)` — subtotals per dept + grand total (hierarchy).
- `CUBE(dept, job)` — all combinations of subtotals.
- `GROUPING(col)` distinguishes "real NULL" from "subtotal row" ⭐.

## Patterns to drill (all in code)
1. Count/avg per group; multi-column grouping.
2. **Find duplicates** ⭐⭐: `GROUP BY col HAVING COUNT(*) > 1` (and delete them keeping one — topic 10).
3. Groups with all/none matching a condition (`HAVING COUNT(*) = SUM(CASE...)`).
4. **Pivot with conditional aggregation** ⭐ (+ Oracle `PIVOT` clause).
5. Percentage of total per group.
6. First/last per group — needs window functions (next topic teaser: `KEEP (DENSE_RANK FIRST)` Oracle trick).

## Top interview questions
1. **WHERE vs HAVING?** ⭐⭐ (+ why prefer WHERE)
2. **Why can't I select emp_name with GROUP BY dept_id?** (pile logic)
3. **Find duplicate emails in a users table.** ⭐⭐
4. **`COUNT(*)` vs `COUNT(col)` in a LEFT JOIN + GROUP BY?** ⭐ (zero-count parents!)
5. **Departments where ALL employees earn > 50k?** `HAVING MIN(salary) > 50000` (elegant) or count-compare.
6. **Rows → columns (pivot) without PIVOT syntax?** CASE inside aggregates.
7. **What does ROLLUP add?** Subtotals + grand total in one pass.
8. **Can HAVING be used without GROUP BY?** Yes — whole table as one group.

➡️ Code: [`aggregations.sql`](./aggregations.sql)
