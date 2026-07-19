-- ============================================================================
-- TOPIC: Joins — every type + the classic puzzles.
-- Uses the employees/departments schema from 01-sql-basics/basics.sql
-- (dept 40 'Legal' has no employees; Farhan has NULL dept_id — by design!)
-- ============================================================================

-- ===== 1. INNER: only matches (Farhan disappears — NULL dept never matches) =====
SELECT e.emp_name, d.dept_name
FROM   employees e
JOIN   departments d ON e.dept_id = d.dept_id;

-- ===== 2. LEFT: keep ALL employees (Farhan appears, dept_name NULL) ⭐ =====
SELECT e.emp_name, d.dept_name
FROM   employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id;

-- ===== 3. FULL OUTER: dept-less employees AND employee-less depts =====
SELECT e.emp_name, d.dept_name
FROM   employees e
FULL OUTER JOIN departments d ON e.dept_id = d.dept_id;

-- ===== 4. THE ON-vs-WHERE TRAP ⭐⭐ =====
-- Goal: all departments + their Pune-office employees (if any).
-- ✔ Condition in ON: Legal & others still appear (with NULLs)
SELECT d.dept_name, e.emp_name
FROM   departments d
LEFT JOIN employees e
       ON e.dept_id = d.dept_id AND e.salary > 90000;

-- ❌ Same condition in WHERE: NULL salary rows fail the filter
--    -> LEFT silently becomes INNER; departments without high-earners vanish!
SELECT d.dept_name, e.emp_name
FROM   departments d
LEFT JOIN employees e ON e.dept_id = d.dept_id
WHERE  e.salary > 90000;

-- ============================================================================
-- CLASSIC PUZZLES
-- ============================================================================

-- ===== P1. Departments with NO employees — three ways ⭐ =====
-- (a) anti-join via LEFT + IS NULL
SELECT d.dept_name
FROM   departments d
LEFT JOIN employees e ON e.dept_id = d.dept_id
WHERE  e.emp_id IS NULL;

-- (b) NOT EXISTS (usually best plan, NULL-safe ⭐)
SELECT d.dept_name FROM departments d
WHERE  NOT EXISTS (SELECT 1 FROM employees e WHERE e.dept_id = d.dept_id);

-- (c) NOT IN — works here, but if the subquery EVER returns a NULL,
--     the whole query returns ZERO rows ⭐⭐ (see topic 04). Prefer (b).
SELECT d.dept_name FROM departments d
WHERE  d.dept_id NOT IN (SELECT e.dept_id FROM employees e
                         WHERE  e.dept_id IS NOT NULL);   -- the mandatory guard

-- ===== P2. Employees earning more than their manager ⭐ (self join) =====
SELECT e.emp_name  AS employee,  e.salary AS emp_sal,
       m.emp_name  AS manager,   m.salary AS mgr_sal
FROM   employees e
JOIN   employees m ON e.manager_id = m.emp_id     -- same table, two roles
WHERE  e.salary > m.salary;                        -- Meera(120k) > Asha(95k)

-- ===== P3. Same-department pairs, no duplicates/self-pairs =====
SELECT e1.emp_name, e2.emp_name, e1.dept_id
FROM   employees e1
JOIN   employees e2 ON  e1.dept_id = e2.dept_id
                    AND e1.emp_id  < e2.emp_id;    -- '<' kills (A,A) and (B,A) ⭐

-- ===== P4. Headcount per department INCLUDING zero ⭐ =====
SELECT d.dept_name, COUNT(e.emp_id) AS headcount    -- COUNT(e.emp_id) not COUNT(*) ⭐
FROM   departments d                                -- (COUNT(*) would count the NULL row as 1!)
LEFT JOIN employees e ON e.dept_id = d.dept_id
GROUP BY d.dept_name
ORDER BY headcount DESC;

-- ===== P5. Row-multiplication bug and fix ⭐ =====
-- BUG: joining orders (1..N) then SUMming employee salary double-counts.
-- Demo shape (assume orders table): fix = pre-aggregate the N-side first:
--   WITH order_totals AS (
--     SELECT emp_id, SUM(amount) AS total FROM orders GROUP BY emp_id
--   )
--   SELECT e.emp_name, e.salary, NVL(t.total, 0)
--   FROM employees e LEFT JOIN order_totals t ON t.emp_id = e.emp_id;

-- ===== P6. Semi-join: EXISTS vs JOIN =====
-- "Departments that HAVE at least one 90k+ earner" — one row per dept, no dupes:
SELECT d.dept_name FROM departments d
WHERE  EXISTS (SELECT 1 FROM employees e
               WHERE  e.dept_id = d.dept_id AND e.salary >= 90000);
-- INNER JOIN would repeat the department once per matching employee.

-- ===== P7. CROSS JOIN legit use: generate a matrix =====
SELECT d.dept_name, g.grade
FROM   departments d
CROSS JOIN (SELECT 'A' AS grade FROM dual UNION ALL
            SELECT 'B' FROM dual UNION ALL
            SELECT 'C' FROM dual) g
ORDER BY d.dept_name, g.grade;                     -- every dept × every grade

-- ===== Oracle legacy outer-join syntax (recognize, don't write) =====
SELECT e.emp_name, d.dept_name
FROM   employees e, departments d
WHERE  e.dept_id = d.dept_id(+);                   -- (+) side = the NULL-padded side
