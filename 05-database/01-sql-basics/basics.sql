-- ============================================================================
-- TOPIC: SQL Basics + THE SHARED SCHEMA used across all 05-database topics.
-- Oracle syntax (works on Oracle XE / gvenzl/oracle-free docker image).
-- ============================================================================

-- ===== The classic interview schema: EMPLOYEES + DEPARTMENTS =====
CREATE TABLE departments (
  dept_id    NUMBER PRIMARY KEY,
  dept_name  VARCHAR2(50) NOT NULL,
  location   VARCHAR2(50)
);

CREATE TABLE employees (
  emp_id     NUMBER PRIMARY KEY,
  emp_name   VARCHAR2(50) NOT NULL,
  salary     NUMBER(10,2),
  dept_id    NUMBER REFERENCES departments(dept_id),
  manager_id NUMBER REFERENCES employees(emp_id),   -- self-reference (for SELF JOIN)
  hire_date  DATE,
  email      VARCHAR2(100)
);

INSERT INTO departments VALUES (10, 'IT',      'Pune');
INSERT INTO departments VALUES (20, 'HR',      'Mumbai');
INSERT INTO departments VALUES (30, 'Finance', 'Delhi');
INSERT INTO departments VALUES (40, 'Legal',   'Pune');      -- NO employees (outer-join demo)

INSERT INTO employees VALUES (1, 'Asha',  95000, 10, NULL, DATE '2018-03-15', 'asha@co.in');
INSERT INTO employees VALUES (2, 'Ravi',  55000, 20, 1,    DATE '2020-07-01', NULL);
INSERT INTO employees VALUES (3, 'Meera', 120000,10, 1,    DATE '2016-01-20', 'meera@co.in');
INSERT INTO employees VALUES (4, 'Kiran', 80000, 30, 3,    DATE '2021-11-11', 'kiran@co.in');
INSERT INTO employees VALUES (5, 'Divya', 95000, 10, 3,    DATE '2019-05-30', NULL);
INSERT INTO employees VALUES (6, 'Farhan',NULL,  NULL, 1,  DATE '2023-02-14', 'farhan@co.in'); -- NULL salary+dept!
COMMIT;

-- ============================================================================
-- 1. SELECT anatomy — every clause in play
-- ============================================================================
SELECT   DISTINCT dept_id, salary
FROM     employees
WHERE    salary > 50000                  -- row filter (no aggregates, no SELECT aliases!)
ORDER BY salary DESC NULLS LAST          -- Oracle: control NULL position
FETCH FIRST 3 ROWS ONLY;                 -- 12c+ pagination

-- Old-style Top-N with ROWNUM — and THE TRAP:
SELECT * FROM employees WHERE ROWNUM <= 3 ORDER BY salary DESC;   -- ❌ WRONG: rownum BEFORE sort
SELECT * FROM (SELECT * FROM employees ORDER BY salary DESC)
WHERE  ROWNUM <= 3;                                               -- ✔ sort first, then limit

-- ============================================================================
-- 2. NULL behavior ⭐⭐
-- ============================================================================
SELECT emp_name FROM employees WHERE salary <> 90000;    -- Farhan (NULL salary) MISSING!
SELECT emp_name FROM employees
WHERE  salary <> 90000 OR salary IS NULL;                -- ✔ include unknowns explicitly

SELECT COUNT(*), COUNT(salary), COUNT(email) FROM employees;      -- 6, 5, 4 ⭐
SELECT AVG(salary), SUM(salary)/COUNT(*) FROM employees;          -- differ! AVG ignores NULL

SELECT emp_name,
       NVL(email, 'no-email')                 AS nvl_way,         -- Oracle
       COALESCE(email, 'no-email')            AS standard_way,    -- portable ⭐
       NVL2(email, 'has email', 'missing')    AS nvl2_way,
       NULLIF(salary, 95000)                  AS null_if_95k      -- NULL when equal
FROM   employees;

-- ============================================================================
-- 3. Operators & pattern matching
-- ============================================================================
SELECT emp_name FROM employees WHERE salary BETWEEN 55000 AND 95000;  -- INCLUSIVE ⭐
SELECT emp_name FROM employees WHERE dept_id IN (10, 30);
SELECT emp_name FROM employees WHERE emp_name LIKE '_i%';             -- 2nd letter i
SELECT emp_name FROM employees WHERE hire_date >= DATE '2020-01-01';

-- Precedence trap: NOT > AND > OR
SELECT emp_name FROM employees
WHERE  dept_id = 10 OR dept_id = 20 AND salary > 60000;   -- AND binds first!
-- means: dept 10 (any salary) OR (dept 20 AND >60k). Parenthesize what you mean.

-- ============================================================================
-- 4. CASE — conditional logic (and the pivot trick preview)
-- ============================================================================
SELECT emp_name, salary,
       CASE
         WHEN salary >= 100000 THEN 'A-band'
         WHEN salary >=  80000 THEN 'B-band'
         WHEN salary IS NULL   THEN 'unbanded'
         ELSE 'C-band'
       END AS band
FROM   employees;

-- CASE inside aggregate = conditional counting ⭐ (pivot preview)
SELECT COUNT(CASE WHEN salary >= 90000 THEN 1 END) AS high_paid,
       COUNT(CASE WHEN salary <  90000 THEN 1 END) AS rest
FROM   employees;

-- ============================================================================
-- 5. Strings, numbers, dates — the everyday toolkit
-- ============================================================================
SELECT UPPER(emp_name)                          AS shouted,
       SUBSTR(emp_name, 1, 3)                   AS first3,
       INSTR(email, '@')                        AS at_pos,
       emp_name || ' <' || NVL(email,'-') || '>' AS display,   -- Oracle concat
       LPAD(TO_CHAR(salary), 10, '*')           AS padded
FROM   employees;

SELECT emp_name,
       TO_CHAR(hire_date, 'DD-Mon-YYYY')            AS pretty,
       ROUND(MONTHS_BETWEEN(SYSDATE, hire_date)/12, 1) AS years_service,
       ADD_MONTHS(hire_date, 6)                     AS confirmation_date,
       EXTRACT(YEAR FROM hire_date)                 AS join_year
FROM   employees;

-- ============================================================================
-- 6. DML + TCL round-trip
-- ============================================================================
INSERT INTO employees (emp_id, emp_name, salary, dept_id, hire_date)
VALUES (7, 'Temp', 30000, 20, SYSDATE);

SAVEPOINT before_raise;
UPDATE employees SET salary = salary * 1.10 WHERE dept_id = 20;
ROLLBACK TO before_raise;               -- undo the raise, keep the insert
DELETE FROM employees WHERE emp_id = 7;
COMMIT;

-- TRUNCATE TABLE employees;   -- DDL: instant, no rollback, no triggers (don't run!)
