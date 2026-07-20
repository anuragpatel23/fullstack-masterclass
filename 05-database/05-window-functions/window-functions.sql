-- ============================================================================
-- TOPIC: Window Functions — ranking trio, Nth highest (all variants),
-- LAG/LEAD, running totals, top-N per group, gaps & islands.
-- Uses employees/departments from 01-sql-basics/basics.sql
-- ============================================================================

-- ===== 1. The ranking trio, side by side ⭐⭐ =====
SELECT emp_name, salary,
       ROW_NUMBER() OVER (ORDER BY salary DESC) AS row_num,     -- 1,2,3,4,5 (unique)
       RANK()       OVER (ORDER BY salary DESC) AS rnk,         -- 1,2,2,4   (gap)
       DENSE_RANK() OVER (ORDER BY salary DESC) AS dense_rnk    -- 1,2,2,3   (no gap)
FROM   employees
WHERE  salary IS NOT NULL;
-- (Asha & Divya both at 95000 show the tie behavior)

-- ===== 2. NTH HIGHEST SALARY — every variant ⭐⭐ =====

-- (a) THE canonical answer: DENSE_RANK (ties handled) — N = 2
SELECT salary
FROM  (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS dr
       FROM   employees WHERE salary IS NOT NULL)
WHERE  dr = 2;

-- (b) FETCH/OFFSET on distinct salaries (12c+)
SELECT DISTINCT salary FROM employees WHERE salary IS NOT NULL
ORDER BY salary DESC
OFFSET 1 ROW FETCH FIRST 1 ROW ONLY;

-- (c) Correlated subquery (pre-window classic)
SELECT DISTINCT salary FROM employees e
WHERE 1 = (SELECT COUNT(DISTINCT salary) FROM employees x WHERE x.salary > e.salary);

-- (d) PER DEPARTMENT: one line change — this is why windows win ⭐
SELECT dept_id, emp_name, salary
FROM  (SELECT e.*, DENSE_RANK() OVER (PARTITION BY dept_id
                                      ORDER BY salary DESC) AS dr
       FROM   employees e WHERE salary IS NOT NULL AND dept_id IS NOT NULL)
WHERE  dr = 2;

-- ===== 3. Top 3 earners in EACH department ⭐ =====
SELECT dept_id, emp_name, salary, rn
FROM  (SELECT e.*, ROW_NUMBER() OVER (PARTITION BY dept_id
                                      ORDER BY salary DESC, emp_id) AS rn
       FROM   employees e WHERE dept_id IS NOT NULL)
WHERE  rn <= 3
ORDER BY dept_id, rn;

-- ===== 4. LAG / LEAD: compare with neighbors ⭐ =====
SELECT emp_name, hire_date, salary,
       LAG(salary)  OVER (ORDER BY hire_date)               AS prev_hire_sal,
       salary - LAG(salary, 1, 0) OVER (ORDER BY hire_date) AS delta_vs_prev,
       LEAD(emp_name) OVER (ORDER BY hire_date)             AS next_hired
FROM   employees
WHERE  salary IS NOT NULL;

-- ===== 5. Running totals & moving averages (needs an orders-like table) =====
WITH daily_sales (d, amount) AS (
  SELECT DATE '2026-01-01', 100 FROM dual UNION ALL
  SELECT DATE '2026-01-02', 150 FROM dual UNION ALL
  SELECT DATE '2026-01-03',  90 FROM dual UNION ALL
  SELECT DATE '2026-01-04', 200 FROM dual UNION ALL
  SELECT DATE '2026-01-05', 120 FROM dual
)
SELECT d, amount,
       SUM(amount) OVER (ORDER BY d)                                    AS running_total, -- ⭐
       ROUND(AVG(amount) OVER (ORDER BY d ROWS BETWEEN 2 PRECEDING
                                             AND CURRENT ROW))          AS moving_avg_3d,
       SUM(amount) OVER ()                                              AS grand_total,
       ROUND(100 * amount / SUM(amount) OVER (), 1)                     AS pct_of_total
FROM   daily_sales;

-- ===== 6. Month-over-month growth % (LAG pattern) ⭐ =====
WITH monthly (mon, revenue) AS (
  SELECT 'Jan', 1000 FROM dual UNION ALL
  SELECT 'Feb', 1200 FROM dual UNION ALL
  SELECT 'Mar',  900 FROM dual
)
SELECT mon, revenue,
       LAG(revenue) OVER (ORDER BY CASE mon WHEN 'Jan' THEN 1 WHEN 'Feb' THEN 2 ELSE 3 END) AS prev_rev,
       ROUND(100 * (revenue - LAG(revenue) OVER (ORDER BY CASE mon WHEN 'Jan' THEN 1 WHEN 'Feb' THEN 2 ELSE 3 END))
             / LAG(revenue) OVER (ORDER BY CASE mon WHEN 'Jan' THEN 1 WHEN 'Feb' THEN 2 ELSE 3 END), 1) AS growth_pct
FROM   monthly;

-- ===== 7. FIRST_VALUE / LAST_VALUE gotcha ⭐ =====
SELECT emp_name, dept_id, salary,
       FIRST_VALUE(emp_name) OVER (PARTITION BY dept_id ORDER BY salary DESC) AS top_earner,
       -- ❌ default frame stops at CURRENT ROW -> "last" is just yourself:
       LAST_VALUE(emp_name)  OVER (PARTITION BY dept_id ORDER BY salary DESC) AS broken_last,
       -- ✔ explicit full frame:
       LAST_VALUE(emp_name)  OVER (PARTITION BY dept_id ORDER BY salary DESC
                                   ROWS BETWEEN UNBOUNDED PRECEDING
                                            AND UNBOUNDED FOLLOWING)          AS lowest_earner
FROM   employees
WHERE  dept_id IS NOT NULL AND salary IS NOT NULL;

-- ===== 8. GAPS & ISLANDS ⭐ (consecutive-days streaks) =====
WITH logins (user_id, login_day) AS (
  SELECT 1, DATE '2026-01-01' FROM dual UNION ALL
  SELECT 1, DATE '2026-01-02' FROM dual UNION ALL
  SELECT 1, DATE '2026-01-03' FROM dual UNION ALL
  SELECT 1, DATE '2026-01-06' FROM dual UNION ALL   -- gap!
  SELECT 1, DATE '2026-01-07' FROM dual
),
tagged AS (
  -- THE TRICK: date minus row_number is CONSTANT within a consecutive run ⭐
  SELECT user_id, login_day,
         login_day - ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_day) AS island_key
  FROM   logins
)
SELECT user_id,
       MIN(login_day) AS streak_start,
       MAX(login_day) AS streak_end,
       COUNT(*)       AS streak_len
FROM   tagged
GROUP BY user_id, island_key
ORDER BY streak_start;                -- two islands: 3-day and 2-day streaks

-- ===== 9. Dedup with ROW_NUMBER (delete duplicates keeping one) ⭐ =====
-- DELETE FROM employees WHERE rowid IN (
--   SELECT rid FROM (
--     SELECT rowid AS rid,
--            ROW_NUMBER() OVER (PARTITION BY emp_name, dept_id ORDER BY emp_id) rn
--     FROM employees)
--   WHERE rn > 1);
