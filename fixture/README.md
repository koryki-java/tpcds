# fixture

The test corpus for the **measures** of the TPC-DS catalog, in the layout of the corpus of
[koryki-java/northwind](https://github.com/koryki-java/northwind) (read its `SPEC.md`; the schema `measures` is
described there). Only that schema exists here:

```
queries/kql/measures/<name>.kql                  a query with agg(); `invalid*` ones must be refused
expected/kql/measures/rewrite/<name>.kql         the plain KQL the rewriter returns
expected/kql/measures/provenance/<name>.txt      output column : table.measure
expected/kql/measures/refusal/<name>.txt         why an invalid* query is refused
expected/kql/measures/csv/<name>.csv, .json      result and column types, on DuckDB
expected/duckdb/measures/sql/<name>.sql          the SQL on DuckDB
```

The measures are `catalog/src/main/resources/ai/koryki/duckdb/databases/tpcds/measures.json`. They follow the model
`examples/tpcds_semantic_model.yaml` of Apache Ossie: `total_sales`, `total_profit`, `total_quantity` (store_sales),
and the two ingredients of its multi-dataset metrics: `customers` (customer) and `total_employees` (store).

The goldens are written and checked by the test `ai.koryki.measure.generator.tpcds.AggRewriterFixturesTest` of
[koryki-java/measures](https://github.com/koryki-java/measures) (`./gradlew tpcdsTest`; `-Dfixtures.write=true` writes
them). `./gradlew :fixture:test` here checks the corpus alone.
