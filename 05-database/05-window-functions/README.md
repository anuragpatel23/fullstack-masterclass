# 05 — Window Functions 🔴⭐⭐ (the modern SQL interview centerpiece)

## Real-life analogy
GROUP BY **melts each pile of exam papers into a single summary card** — individual papers are gone. A window function is a teacher who **walks along the line of students** and, *standing next to each one*, announces something about that student's surroundings — "you're ranked 2nd in your section", "the student before you scored 85" — **every student (row) keeps standing; each just gains extra context**. The `PARTITION BY` is deciding which section the teacher compares you within; `ORDER BY` is the direction she walks.

## Anatomy ⭐
```sql
function() OVER (PARTITION BY dept_id ORDER BY salary DESC
                 ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
```
- `PARTITION BY` — restart the calculation per group (omit = whole result set).
- `ORDER BY` — ordering within the partition (required for ranking/offsets).
- Frame (`ROWS/RANGE BETWEEN ...`) — sliding window for aggregates (default with ORDER BY = range-to-current-row ⭐ subtle!).
- Windows evaluate **after WHERE/GROUP BY/HAVING, before ORDER BY** → can't filter on them directly → wrap in CTE/subquery + `QUALIFY`-less Oracle idiom ⭐.

## The ranking trio ⭐⭐ (know the difference COLD)
For salaries 100, 100, 90:
| Function | Output | Behavior |
|---|---|---|
| `ROW_NUMBER()` | 1,2,3 | arbitrary tiebreak, always unique |
| `RANK()` | 1,1,3 | ties share, next rank **skips** |
| `DENSE_RANK()` | 1,1,2 | ties share, **no gaps** |
Plus `NTILE(4)` (quartiles), `PERCENT_RANK`, `CUME_DIST`.

## Offset functions ⭐
`LAG(col, n, default)` / `LEAD(col, n, default)` — previous/next row's value: month-over-month deltas, gap detection, sessionization.
`FIRST_VALUE / LAST_VALUE` (⚠️ LAST_VALUE needs an explicit frame `ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING` ⭐ — famous gotcha), `NTH_VALUE`.

## Windowed aggregates ⭐
`SUM(x) OVER (ORDER BY d)` — **running total**; `AVG(x) OVER (ORDER BY d ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)` — **moving average**; `SUM(x) OVER (PARTITION BY g)` — group total on every row (percentage-of-group in one pass ⭐); `COUNT(*) OVER ()` — total alongside each row (pagination metadata trick).

## Nth highest salary — EVERY variant ⭐⭐ (the eternal question, all in code)
1. `DENSE_RANK` in a subquery (the canonical modern answer — handles ties).
2. `ROW_NUMBER` (when ties shouldn't share).
3. Correlated subquery (topic 04 recap).
4. `FETCH FIRST ... OFFSET` with DISTINCT.
5. Per-department Nth: add `PARTITION BY dept_id` — one line change ⭐ (this is WHY windows win).

## Top interview questions
1. **ROW_NUMBER vs RANK vs DENSE_RANK?** ⭐⭐ (with the 100,100,90 example)
2. **Nth highest salary — company-wide and per department.** ⭐⭐
3. **GROUP BY vs window functions?** Collapse vs annotate (analogy).
4. **Running total / cumulative sum of daily orders.**
5. **Month-over-month growth %.** (LAG)
6. **Top 3 earners in each department.** (partition + rank + wrap ⭐)
7. **Delete duplicates keeping one — window version.** (ROW_NUMBER > 1 — in topic 10 too)
8. **Why can't I use RANK() in WHERE?** Evaluation order — wrap in a subquery/CTE.
9. **Gaps & islands** (consecutive login days) — the ROW_NUMBER date-diff trick ⭐ (in code).

➡️ Code: [`window-functions.sql`](./window-functions.sql)
