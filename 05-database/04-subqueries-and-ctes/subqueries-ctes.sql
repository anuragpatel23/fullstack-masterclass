-- ============================================================================
-- TOPIC: Subqueries & CTEs — scalar, correlated, EXISTS, the NOT IN trap,
-- recursive hierarchies. Uses the schema from 01-sql-basics/basics.sql
-- ============================================================================

-- ===== 1. Scalar subquery: above company average ⭐ =====
SELECT emp_name, salary
FROM   employees
WHERE  salary > (SELECT AVG(salary) FROM employees);   -- inner runs once

-- ===== 2. Correlated: above THEIR OWN department's average ⭐⭐ =====
SELECT e.emp_name, e.salary, e.dept_id
FROM   employees e
WHERE  e.salary > (SELECT AVG(x.salary)
                   FROM   employees x
                   WHERE  x.dept_id = e.dept_id);      -- re-evaluated per outer row

-- Same thing, CTE style — compute once, join (usually faster + clearer):
WITH dept_avg AS (
  SELECT dept_id, AVG(salary) AS avg_sal
  FROM   employees
  GROUP BY dept_id
)
SELECT e.emp_name, e.salary, ROUND(a.avg_sal) AS dept_avg
FROM   employees e
JOIN   dept_avg a ON a.dept_id = e.dept_id
WHERE  e.salary > a.avg_sal;

-- ===== 3. Top earner PER department (correlated MAX) =====
SELECT e.emp_name, e.dept_id, e.salary
FROM   employees e
WHERE  e.salary = (SELECT MAX(x.salary) FROM employees x
                   WHERE  x.dept_id = e.dept_id);

-- ===== 4. EXISTS (semi-join) vs IN =====
-- Departments having at least one 90k+ earner:
SELECT d.dept_name FROM departments d
WHERE  EXISTS (SELECT 1 FROM employees e                -- stops at FIRST match ⭐
               WHERE  e.dept_id = d.dept_id AND e.salary >= 90000);

SELECT d.dept_name FROM departments d
WHERE  d.dept_id IN (SELECT e.dept_id FROM employees e WHERE e.salary >= 90000);

-- ===== 5. THE NOT IN NULL TRAP ⭐⭐ =====
-- Goal: departments with no employees.
SELECT d.dept_name FROM departments d
WHERE  d.dept_id NOT IN (SELECT e.dept_id FROM employees e);
-- ^ RETURNS ZERO ROWS! Farhan's dept_id is NULL ->
--   dept_id NOT IN (10,20,30,NULL) is never TRUE for anything.

-- ✔ Fix 1: NOT EXISTS (NULL-safe — the right default)
SELECT d.dept_name FROM departments d
WHERE  NOT EXISTS (SELECT 1 FROM employees e WHERE e.dept_id = d.dept_id);

-- ✔ Fix 2: filter the NULLs
SELECT d.dept_name FROM departments d
WHERE  d.dept_id NOT IN (SELECT e.dept_id FROM employees e
                         WHERE  e.dept_id IS NOT NULL);

-- ===== 6. ANY / ALL =====
SELECT emp_name, salary FROM employees
WHERE  salary > ALL (SELECT salary FROM employees WHERE dept_id = 20);  -- > HR's max
SELECT emp_name, salary FROM employees
WHERE  salary > ANY (SELECT salary FROM employees WHERE dept_id = 10);  -- > IT's min

-- ===== 7. Nth highest salary — correlated classic (pre-window era) ⭐ =====
-- "N-1 distinct salaries are higher than mine" -> N=2: second highest
SELECT DISTINCT salary
FROM   employees e
WHERE  1 = (SELECT COUNT(DISTINCT x.salary)              -- 1 salary higher => 2nd
            FROM   employees x
            WHERE  x.salary > e.salary);

-- ===== 8. Multi-CTE pipeline (how real reports are built) =====
WITH salaried AS (
  SELECT * FROM employees WHERE salary IS NOT NULL
),
dept_stats AS (
  SELECT dept_id, COUNT(*) AS cnt, AVG(salary) AS avg_sal
  FROM   salaried GROUP BY dept_id
),
ranked AS (
  SELECT s.*, d.avg_sal,
         CASE WHEN s.salary > d.avg_sal THEN 'above' ELSE 'at/below' END AS vs_avg
  FROM   salaried s JOIN dept_stats d ON d.dept_id = s.dept_id
)
SELECT dept_id, vs_avg, COUNT(*) AS people
FROM   ranked
GROUP BY dept_id, vs_avg
ORDER BY dept_id, vs_avg;

-- ===== 9. RECURSIVE CTE: the org chart ⭐ =====
WITH org (emp_id, emp_name, manager_id, lvl, path) AS (
  -- anchor: the CEO (no manager)
  SELECT emp_id, emp_name, manager_id, 1, CAST(emp_name AS VARCHAR2(500))
  FROM   employees
  WHERE  manager_id IS NULL
  UNION ALL
  -- recursive: everyone whose manager is already in `org`
  SELECT e.emp_id, e.emp_name, e.manager_id, o.lvl + 1,
         o.path || ' -> ' || e.emp_name
  FROM   employees e
  JOIN   org o ON e.manager_id = o.emp_id
)
SELECT LPAD(' ', (lvl-1)*2) || emp_name AS org_chart, lvl, path
FROM   org
ORDER BY path;

-- Oracle-classic equivalent: CONNECT BY ⭐
SELECT LPAD(' ', (LEVEL-1)*2) || emp_name AS org_chart,
       LEVEL,
       SYS_CONNECT_BY_PATH(emp_name, ' -> ') AS path
FROM   employees
START WITH manager_id IS NULL
CONNECT BY PRIOR emp_id = manager_id
ORDER SIBLINGS BY emp_name;
