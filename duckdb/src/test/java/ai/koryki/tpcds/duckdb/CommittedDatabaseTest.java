/*
 * Copyright 2025-2026 Johannes Zemlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ai.koryki.tpcds.duckdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The committed {@code tpcds.duckdb} is a build product of {@code BuildDatabase}; this fails when
 * it and the catalog beside it have drifted apart, since a harness reads the file and the catalog
 * and never the builder.
 *
 * <p>It does not rebuild the file: that needs the {@code tpcds} extension, which is fetched from
 * the network, and a test that reaches out is a different test on every run.
 */
class CommittedDatabaseTest {

    private static final Path DATABASE =
            Path.of("src/main/resources/ai/koryki/duckdb/databases/tpcds/tpcds.duckdb");
    private static final Path CATALOG =
            Path.of("../catalog/src/main/resources/ai/koryki/duckdb/databases/tpcds/db.json");

    private static Connection connection;
    private static JsonNode catalog;

    @BeforeAll
    static void open() throws Exception {
        Properties readOnly = new Properties();
        readOnly.setProperty("duckdb.read_only", "true");
        connection =
                DriverManager.getConnection("jdbc:duckdb:" + DATABASE.toAbsolutePath(), readOnly);
        catalog = JsonMapper.builder().build().readTree(CATALOG.toFile());
    }

    @AfterAll
    static void close() throws SQLException {
        connection.close();
    }

    @Test
    void tablesAndColumnsAreThoseOfTheCatalog() throws SQLException {
        List<String> inCatalog = new ArrayList<>();
        for (JsonNode table : catalog.get("tables")) {
            for (JsonNode column : table.get("columns")) {
                inCatalog.add(table.get("name").asString() + "." + column.get("name").asString());
            }
        }
        List<String> inFile =
                strings(
                        "SELECT table_name || '.' || column_name FROM duckdb_columns() "
                                + "WHERE database_name = current_database()");
        assertEquals(
                new TreeSet<>(inCatalog),
                new TreeSet<>(inFile),
                "tables or columns differ -- run ./gradlew :duckdb:updateDatabase or regenerate the catalog");
    }

    @Test
    void everyTableHasRows() throws SQLException {
        List<String> empty = new ArrayList<>();
        for (JsonNode table : catalog.get("tables")) {
            String name = table.get("name").asString();
            if (count("SELECT count(*) FROM " + name) == 0) {
                empty.add(name);
            }
        }
        assertTrue(empty.isEmpty(), () -> "tables without rows: " + empty);
    }

    /** The catalog declares the keys the file does not; the data must agree with every one. */
    @Test
    void everyRelationOfTheCatalogFindsItsTarget() throws SQLException {
        List<String> orphans = new ArrayList<>();
        for (JsonNode r : catalog.get("relations")) {
            StringBuilder on = new StringBuilder();
            StringBuilder notNull = new StringBuilder();
            for (int i = 0; i < r.get("startColumns").size(); i++) {
                String from = r.get("startColumns").get(i).asString();
                String to = r.get("endColumns").get(i).asString();
                on.append(i > 0 ? " AND " : "").append("f." + from + " = p." + to);
                notNull.append(i > 0 ? " AND " : "").append("f." + from + " IS NOT NULL");
            }
            String sql =
                    "SELECT count(*) FROM "
                            + r.get("startTable").asString()
                            + " f LEFT JOIN "
                            + r.get("endTable").asString()
                            + " p ON "
                            + on
                            + " WHERE "
                            + notNull
                            + " AND p."
                            + r.get("endColumns").get(0).asString()
                            + " IS NULL";
            long missing = count(sql);
            if (missing > 0) {
                orphans.add(r.get("name").asString() + ": " + missing);
            }
        }
        assertTrue(orphans.isEmpty(), () -> "relations with orphans: " + orphans);
    }

    private static List<String> strings(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        }
        return values;
    }

    private static long count(String sql) throws SQLException {
        try (Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
