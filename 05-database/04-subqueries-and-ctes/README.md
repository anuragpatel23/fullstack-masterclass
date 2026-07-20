# 04 — Subqueries & CTEs 🟡⭐

## Real-life analogy
A **subquery** is answering "who earns above average?" by first sending an intern to compute the average (**inner query runs conceptually first**), then comparing everyone against that number. A **correlated subquery** is a *different* errand: "is this employee the top earner **of their own department**?" — the intern must run back to the archive **once per employee**, carrying that employee's department with them (outer row drives inner query ⭐). A **CTE (`WITH`)** is doing the prep work on a **labeled whiteboard first** — named intermediate results anyone can read, instead of nesting parentheses five levels deep.

## Subquery placements
- WHERE: `salary > (SELECT AVG(salary) ...)` — scalar ⭐.
- FROM: inline view / derived table `FROM (SELECT ...) t`.
- SELECT: scalar subquery per row (watch performance).
- Multi-row operators: `IN`, `NOT IN`, `ANY`, `ALL` (`> ALL (...)` = greater than max).

## Correlated subqueries ⭐⭐
Inner references outer's columns → logically re-executed per outer row:
```sql
SELECT e.* FROM employees e
WHERE e.salary = (SELECT MAX(salary) FROM employees x
                  WHERE x.dept_id = e.dept_id);      -- top earner per dept
```
`EXISTS (SELECT 1 ...)` ⭐ — stops at first match (semi-join), NULL-safe, usually optimal for "has at least one".

## THE `NOT IN` NULL trap ⭐⭐ (repeat until reflexive)
`x NOT IN (1, 2, NULL)` → `x≠1 AND x≠2 AND x≠NULL` → last term is UNKNOWN → **whole predicate never true → zero rows returned**. Any nullable column in the subquery's SELECT poisons `NOT IN`. **Use `NOT EXISTS`** (NULL-safe) or filter `IS NOT NULL`. This single question eliminates half of candidates.

## CTEs — `WITH` ⭐
```sql
WITH dept_stats AS (
  SELECT dept_id, AVG(salary) avg_sal FROM employees GROUP BY dept_id
)
SELECT e.emp_name, e.salary, s.avg_sal
FROM employees e JOIN dept_stats s ON s.dept_id = e.dept_id
WHERE e.salary > s.avg_sal;
```
Benefits: readability, reuse the same block twice, build pipelines step by step (multiple CTEs), self-document with names. Not automatically materialized (optimizer decides; Oracle hint `/*+ materialize */`).

## Recursive CTEs ⭐ (senior differentiator)
Anchor member `UNION ALL` recursive member referencing the CTE itself — walks hierarchies (org chart, folders, BOM):
```sql
WITH org (emp_id, emp_name, lvl) AS (
  SELECT emp_id, emp_name, 1 FROM employees WHERE manager_id IS NULL    -- anchor
  UNION ALL
  SELECT e.emp_id, e.emp_name, o.lvl + 1
  FROM employees e JOIN org o ON e.manager_id = o.emp_id                -- recurse
)
SELECT * FROM org;
```
Oracle-classic alternative: `CONNECT BY PRIOR` + `LEVEL` + `START WITH` ⭐ (know both).

## Top interview questions
1. **Employees earning above company average / above THEIR department's average.** ⭐⭐ (scalar vs correlated — in code)
2. **IN vs EXISTS — difference and performance?** List-compare vs stop-at-first-match; EXISTS for correlated/semi-join; NULL behavior ⭐.
3. **The NOT IN returns-nothing bug — explain.** ⭐⭐
4. **Nth highest salary with a correlated subquery.** (the pre-window-function classic — in code)
5. **What's a CTE and why use over nested subqueries?**
6. **Walk an org hierarchy — recursive CTE or CONNECT BY.** ⭐
7. **Derived table vs CTE vs view?** Inline once / named per-query / stored definition.
8. **`> ALL` vs `> ANY`?** Greater than max vs greater than min.

➡️ Code: [`subqueries-ctes.sql`](./subqueries-ctes.sql)
