WITH aggd1 (i1, i2, c1) AS (
SELECT
  DISTINCT ss.ss_item_sk AS i1
, ss.ss_ticket_number AS i2
, ss.ss_ext_sales_price AS c1
FROM
 store_sales ss
  INNER JOIN customer c ON
   ss.ss_customer_sk = c.c_customer_sk
)
, aggm1 (v1) AS (
SELECT
  sum(x.c1) AS v1
FROM
 aggd1 x
)
, aggd2 (i1, c1) AS (
SELECT
  DISTINCT c.c_customer_sk AS i1
, c.c_customer_sk AS c1
FROM
 store_sales ss
  INNER JOIN customer c ON
   ss.ss_customer_sk = c.c_customer_sk
)
, aggm2 (v2) AS (
SELECT
  count(x.c1) AS v2
FROM
 aggd2 x
)
, aggu (v1, v2) AS (
 SELECT
   a.v1 AS v1
 , CAST(NULL AS INTEGER) AS v2
 FROM
  aggm1 a
UNION ALL
 SELECT
   CAST(NULL AS DECIMAL(18, 4)) AS v1
 , a.v2 AS v2
 FROM
  aggm2 a
)
SELECT
  max(t.v1) / NULLIF(max(t.v2), 0) AS value_per_customer
FROM
 aggu t