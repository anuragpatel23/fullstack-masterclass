-- ============================================================================
-- TOPIC: Aggregations & Grouping — duplicates, pivots, rollups, group filters.
-- Uses the employees/departments schema from 01-sql-basics/basics.sql
-- ============================================================================

-- ===== 1. The basics per department =====
SELECT d.dept_name,
       COUNT(e.emp_id)             AS headcount,        -- counts matches only ⭐
       ROUND(AVG(e.salary))        AS avg_sal,          -- ignores NULL salaries
       MIN(e.salary), MAX(e.salary),
       LISTAGG(e.emp_name, ', ') WITHIN GROUP (ORDER BY e.emp_name) AS team  -- Oracle ⭐
FROM   departments d
LEFT JOIN employees e ON e.dept_id = d.dept_id
GROUP BY d.dept_name
ORDER BY headcount DESC;

-- ===== 2. WHERE vs HAVING in one query ⭐⭐ =====
SELECT dept_id, ROUND(AVG(salary)) AS avg_sal, COUNT(*) AS cnt
FROM   employees
WHERE  hire_date >= DATE '2017-01-01'      -- ROW filter first (cheap)
GROUP BY dept_id
HAVING AVG(salary) > 70000                 -- GROUP filter after
ORDER BY avg_sal DESC;

-- ===== 3. FIND DUPLICATES ⭐⭐ (the most-asked aggregation question) =====
-- setup: pretend emp_name can repeat; find names appearing more than once
SELECT emp_name, COUNT(*) AS occurrences
FROM   employees
GROUP BY emp_name
HAVING COUNT(*) > 1;

-- Duplicate SALARY values (real duplicates in our data: 95000 twice)
SELECT salary, COUNT(*) AS times
FROM   employees
WHERE  salary IS NOT NULL
GROUP BY salary
HAVING COUNT(*) > 1;                        -- 95000 | 2

-- ===== 4. Conditional aggregation = manual pivot ⭐⭐ =====
-- Rows -> columns: salary bands per department, one row per dept
SELECT d.dept_name,
       COUNT(CASE WHEN e.salary >= 100000                       THEN 1 END) AS band_a,
       COUNT(CASE WHEN e.salary >=  80000 AND e.salary < 100000 THEN 1 END) AS band_b,
       COUNT(CASE WHEN e.salary <   80000                       THEN 1 END) AS band_c
FROM   departments d
LEFT JOIN employees e ON e.dept_id = d.dept_id
GROUP BY d.dept_name;

-- Same with Oracle's PIVOT clause (11g+):
SELECT * FROM (
  SELECT d.dept_name,
         CASE WHEN e.salary >= 100000 THEN 'A'
              WHEN e.salary >=  80000 THEN 'B'
              ELSE 'C' END AS band
  FROM   employees e JOIN departments d ON d.dept_id = e.dept_id
)
PIVOT ( COUNT(*) FOR band IN ('A' AS band_a, 'B' AS band_b, 'C' AS band_c) );

-- ===== 5. Group-wide conditions =====
-- Departments where ALL employees earn > 50k (elegant version ⭐)
SELECT dept_id
FROM   employees
WHERE  salary IS NOT NULL
GROUP BY dept_id
HAVING MIN(salary) > 50000;

-- Departments having at least 2 people hired since 2018
SELECT dept_id
FROM   employees
GROUP BY dept_id
HAVING SUM(CASE WHEN hire_date >= DATE '2018-01-01' THEN 1 ELSE 0 END) >= 2;

-- ===== 6. Percentage of total ⭐ (window teaser) =====
SELECT dept_id,
       SUM(salary) AS dept_total,
       ROUND(100 * SUM(salary) / SUM(SUM(salary)) OVER (), 1) AS pct_of_payroll
FROM   employees
WHERE  salary IS NOT NULL AND dept_id IS NOT NULL
GROUP BY dept_id;

-- ===== 7. ROLLUP: subtotals + grand total in one pass =====
SELECT NVL(TO_CHAR(dept_id), 'ALL DEPTS') AS dept,
       COUNT(*)     AS cnt,
       SUM(salary)  AS total,
       GROUPING(dept_id) AS is_subtotal_row      -- 1 = generated total row ⭐
FROM   employees
GROUP BY ROLLUP(dept_id);

-- ===== 8. HAVING without GROUP BY (whole table = one group) =====
SELECT COUNT(*) FROM employees HAVING COUNT(*) > 3;

-- ===== 9. NULL grouping quirk ⭐ =====
SELECT dept_id, COUNT(*)
FROM   employees
GROUP BY dept_id;          -- Farhan's NULL dept forms its OWN single group
                           -- (GROUP BY treats NULLs as equal; joins do NOT)
