# koryki tpcds

The TPC-DS decision-support schema as a koryki sample database: the physical schema (`db.json`)
and the semantic layer (`model.json`) of the DuckDB flavour, written for testing SQL/KQL query
engines against a wide, snowflake-shaped schema.

| Module | Holds | Consumed as |
|---|---|---|
| [`catalog`](catalog/README.md) | `db.json` and `model/en/model.json` of the TPC-DS database for DuckDB | Maven artifact `ai.koryki.tpcds:koryki-tpcds-catalog` |
| `duckdb` | the prebuilt `tpcds.duckdb`, generated at scale factor 0.1 (about 38 MB) | Maven artifact `ai.koryki.tpcds:koryki-tpcds-duckdb` |

## The database

24 tables: seven fact tables (store, catalog and web sales and returns, plus inventory) and 17
dimensions. The `duckdb` module carries the data at scale factor **0.1**, about 10 percent of the
standard 1 GB data set (288,000 store sales, 10,000 customers, 1,800 items): small enough to travel
in a jar and to fit a Cloud Run container, large enough for grouped questions to have something to
group. DuckDB's
`dsdgen` generates it, deterministically:

```bash
./gradlew :duckdb:updateDatabase                       # rebuild the committed file
./gradlew :duckdb:updateDatabase -Pkoryki.tpcds.sf=1   # another scale factor, e.g. for a local run
```

The build fetches DuckDB's `tpcds` extension once; reading the committed file needs no network. A
file beyond this size does not belong in this repository. The standard data set lives in the
git-ignored `data/` directory and is recreated by script on any machine:

```bash
scripts/restore-data.sh        # data/sf1/tpcds.duckdb, scale factor 1 (about 315 MB file)
scripts/restore-data.sh 10     # another scale factor
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
