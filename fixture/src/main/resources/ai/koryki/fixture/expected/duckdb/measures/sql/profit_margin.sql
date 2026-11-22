WITH aggd1 (i1, i2, k1, c1, c2) AS (
SELECT
  DISTINCT ss.ss_item_sk AS i1
, ss.ss_ticket_number AS i2
, d.d_year AS k1
, ss.ss_net_profit AS c1
, ss.ss_ext_sales_price AS c2
FROM
 store_sales ss
  INNER JOIN date_dim d ON
   ss.ss_sold_date_sk = d.d_date_sk
WHERE
  d.d_year = 2001
)
, aggm1 (k1, v1, v2) AS (
SELECT
  x.k1 AS k1
, sum(x.c1) AS v1
, sum(x.c2) AS v2
FROM
 aggd1 x
GROUP BY
  x.k1
)
SELECT
  t.k1 AS year
, max(t.v1) / NULLIF(max(t.v2), 0) AS margin
FROM
 aggm1 t
GROUP BY
  t.k1