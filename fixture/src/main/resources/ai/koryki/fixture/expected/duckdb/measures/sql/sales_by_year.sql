WITH aggd1 (i1, i2, k1, c1, c2, c3) AS (
SELECT
  DISTINCT ss.ss_item_sk AS i1
, ss.ss_ticket_number AS i2
, d.d_year AS k1
, ss.ss_ext_sales_price AS c1
, ss.ss_net_profit AS c2
, ss.ss_quantity AS c3
FROM
 store_sales ss
  INNER JOIN date_dim d ON
   ss.ss_sold_date_sk = d.d_date_sk
)
, aggm1 (k1, v1, v2, v3) AS (
SELECT
  x.k1 AS k1
, sum(x.c1) AS v1
, sum(x.c2) AS v2
, sum(x.c3) AS v3
FROM
 aggd1 x
GROUP BY
  x.k1
)
SELECT
  t.k1 AS year
, max(t.v1) AS total_sales
, max(t.v2) AS total_profit
, max(t.v3) AS total_quantity
FROM
 aggm1 t
GROUP BY
  t.k1