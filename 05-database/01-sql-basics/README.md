# 01 — SQL Basics 🟢⭐

## Real-life analogy
A database table is a **school register**: columns are the fixed headings (name, roll no, class), each row is one student. SQL is the **question language you use with the clerk**: "give me all students of class 10 (WHERE), only their names (SELECT list), sorted by marks (ORDER BY), top 5 only (FETCH FIRST)." **NULL is a blank cell** — not zero, not empty string: *unknown*. You can't ask "is blank equal to blank?" (`NULL = NULL` is not true) — you can only ask "is this cell blank?" (`IS NULL`).

## Command categories ⭐ (asked as "what's the difference")
| Category | Commands | Notes |
|---|---|---|
| **DDL** | CREATE, ALTER, DROP, TRUNCATE | auto-commits in Oracle ⭐ |
| **DML** | SELECT, INSERT, UPDATE, DELETE, MERGE | transactional — can rollback |
| **DCL** | GRANT, REVOKE | permissions |
| **TCL** | COMMIT, ROLLBACK, SAVEPOINT | transaction control |

**DELETE vs TRUNCATE vs DROP** ⭐⭐: DELETE = row-by-row DML, WHERE-able, rollback-able, fires triggers, slow. TRUNCATE = DDL, deallocates all rows, no rollback (Oracle), fast, resets storage. DROP = removes the table itself.

## Query anatomy & logical execution order ⭐⭐
```
SELECT [DISTINCT] cols      -- 5
FROM tables                 -- 1
WHERE row_filter            -- 2
GROUP BY cols               -- 3
HAVING group_filter         -- 4
ORDER BY cols               -- 6
FETCH FIRST n ROWS ONLY     -- 7  (Oracle 12c+; old: ROWNUM ⭐)
```
Logical order = FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY. Explains: why you can't use a SELECT alias in WHERE ⭐, why WHERE can't see aggregates.

## NULL rules ⭐⭐ (fail people constantly)
- Any comparison with NULL → UNKNOWN (not true): `NULL = NULL`, `salary <> 500` misses NULLs.
- `IS NULL / IS NOT NULL`; Oracle functions: `NVL(x, default)`, `NVL2`, `COALESCE(a, b, c)` (first non-null, standard ⭐), `NULLIF(a, b)`.
- Aggregates **ignore** NULLs (except `COUNT(*)`) ⭐: `COUNT(col)` counts non-null only; `AVG` divides by non-null count.
- `NOT IN` + a NULL in the list → returns NOTHING ⭐⭐ (the killer trap — see subqueries topic).
- Oracle quirk: `''` (empty string) IS NULL ⭐.
- Sorting: NULLs last by default in Oracle ASC; control with `NULLS FIRST/LAST`.

## Operators & functions toolbox
`BETWEEN` (inclusive ⭐), `IN`, `LIKE 'A%'` (`%` any, `_` one char), `AND/OR/NOT` precedence (NOT > AND > OR — use parentheses!).
Strings: `UPPER LOWER SUBSTR INSTR LENGTH TRIM REPLACE ||` (Oracle concat), `LPAD/RPAD`.
Numbers: `ROUND TRUNC MOD CEIL FLOOR`. Dates: `SYSDATE`, `ADD_MONTHS`, `MONTHS_BETWEEN`, `TRUNC(date)`, `TO_DATE/TO_CHAR` ⭐, `EXTRACT`.
Conditional: `CASE WHEN ... THEN ... ELSE ... END` ⭐ (also inside aggregates — pivot trick), `DECODE` (Oracle legacy).

## Top interview questions
1. **DELETE vs TRUNCATE vs DROP?** ⭐⭐
2. **Logical order of execution — why can't WHERE use a SELECT alias?** ⭐
3. **`COUNT(*)` vs `COUNT(col)` vs `COUNT(DISTINCT col)`?** ⭐
4. **Why does `WHERE salary != 500` skip NULL salaries?**
5. **NVL vs COALESCE?** Oracle-only 2-arg vs standard n-arg, short-circuits.
6. **Find rows where email is missing.** `IS NULL` (not `= NULL` ⭐).
7. **WHERE vs HAVING?** Row filter before grouping vs group filter after.
8. **Top 5 salaries in Oracle — old and new syntax?** ROWNUM subquery vs `FETCH FIRST 5 ROWS ONLY` (and the ROWNUM-before-ORDER BY trap ⭐).

➡️ Code: [`basics.sql`](./basics.sql) — includes the shared schema used by ALL database topics.
