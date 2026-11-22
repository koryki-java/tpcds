WITH aggd1 (i1, i2, k1, c1) AS (
SELECT
  DISTINCT ss.ss_item_sk AS i1
, ss.ss_ticket_number AS i2
, i.i_brand AS k1
, ss.ss_ext_sales_price AS c1
FROM
 store_sales ss
  INNER JOIN item i ON
   ss.ss_item_sk = i.i_item_sk
WHERE
  i.i_category = 'Music'
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
SELECT
  t.k1 AS brand
, max(t.v1) AS total_sales
FROM
 aggm1 t
GROUP BY
  t.k1
ORDER BY
  max(t.v1) DESC
FETCH FIRST 10 ROWS ONLY