# koryki tpcds

The TPC-DS decision-support schema as a koryki sample database: the physical schema (`db.json`)
and the semantic layer (`model.json`) of the DuckDB flavour, written for testing SQL/KQL query
engines against a wide, snowflake-shaped schema.

| Module | Holds | Consumed as |
|---|---|---|
| [`catalog`](catalog/README.md) | `db.json` and `model/en/model.json` of the TPC-DS database for DuckDB | Maven artifact `ai.koryki.tpcds:koryki-tpcds-catalog` |

## The database

24 tables: seven fact tables (store, catalog and web sales and returns, plus inventory) and 17
dimensions. The data is **not** in this repository; DuckDB generates it:

```sql
INSTALL tpcds; LOAD tpcds;
CALL dsdgen(sf = 1);
```

The generated tables have **no declared keys**. The catalog supplies them: primary keys, 104
foreign keys from surrogate keys (`*_sk`) to dimensions, and three composite ones from each returns
table to its sales table (item plus ticket or order number).

## How the catalog was made

Extracted with the introspection of `incubator/ingest` (`JdbcSchemaReader`, `SchemaModelBuilder`)
by `incubator/tpcds`, which adds what DuckDB does not declare:

- keys, derived from the column naming and **checked against the data** (no foreign key value
  misses its target);
- the English wording, in `wording.properties` of that module.

Regenerate with `./gradlew :tpcds:extractCatalog -Pkoryki.tpcds.out=<catalog resources>/ai/koryki/duckdb/databases/tpcds`
in `incubator`.

See [NOTICE](NOTICE) for the TPC-DS attribution.
