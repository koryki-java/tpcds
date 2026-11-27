WITH aggd1 (i1, i2, k1, c1) AS (
SELECT
  DISTINCT ss.ss_item_sk AS i1
, ss.ss_ticket_number AS i2
, s.s_state AS k1
, ss.ss_ext_sales_price AS c1
FROM
 store_sales ss
  INNER JOIN store s ON
   ss.ss_store_sk = s.s_store_sk
)
, aggm1 (k1, v1) AS (
SELECT
  x.k1 AS k1
, sum(x.c1) AS v1
FROM
 aggd1 x
GROUP BY
  x.k1
)
, aggd2 (i1, k1, c1) AS (
SELECT
  DISTINCT s.s_store_sk AS i1
, s.s_state AS k1
, s.s_number_employees AS c1
FROM
 store_sales ss
  INNER JOIN store s ON
   ss.ss_store_sk = s.s_store_sk
)
, aggm2 (k1, v2) AS (
SELECT
  x.k1 AS k1
, sum(x.c1) AS v2
FROM
 aggd2 x
GROUP BY
  x.k1
)
, aggu (k1, v1, v2) AS (
 SELECT
   a.k1 AS k1
 , a.v1 AS v1
 , CAST(NULL AS INTEGER) AS v2
 FROM
  aggm1 a
UNION ALL
 SELECT
   a.k1 AS k1
 , CAST(NULL AS DECIMAL(18, 4)) AS v1
 , a.v2 AS v2
 FROM
  aggm2 a
)
SELECT
  t.k1 AS state
, max(t.v1) / NULLIF(nullif(max(t.v2), 0), 0) AS per_employee
FROM
 aggu t
GROUP BY
  t.k1