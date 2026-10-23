# koryki tpcds catalog

```
src/main/resources/ai/koryki/duckdb/databases/tpcds/
    db.json                 physical schema: tables, columns, DuckDB types, keys, relations
    model/en/model.json     semantic layer: entities, attributes, links
```

The path is the classpath path `CatalogLoader` looks under. It is per dialect (`duckdb`) because
`db.json` carries DuckDB's type names.

## Names in the model

- **Entities** are the tables: `store_sales`, `date_dim`, `customer`, ...
- **Attributes** drop the table prefix: `ss_sold_date_sk` is `sold_date_sk` of `store_sales`. The
  physical name stays in `column`.
- **Links** are named after the role of the foreign key and are shared across tables: `sold_date`
  joins store, catalog and web sales to `date_dim`; `bill_customer` and `ship_customer` are the two
  customers of an order; `sale` joins a return to its sale on item and ticket/order number.
  Several roles can lead to the same dimension, so a query names the role it means.

## Paths over several tables

Transitive joins are chains of single links, e.g. `store_sales` to `customer` to
`customer_address`. The sale row also holds its own address (`addr`, the address at the time of
the purchase), which can differ from the customer's current one (`current_addr`).
