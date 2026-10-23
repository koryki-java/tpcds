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
package ai.koryki.tpcds.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** db.json and model.json must agree: what the model names has to exist in the schema. */
class CatalogConsistencyTest {

    private static final Path DIR = Path.of("src/main/resources/ai/koryki/duckdb/databases/tpcds");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void modelAndSchemaAgree() throws IOException {
        JsonNode db = JSON.readTree(DIR.resolve("db.json").toFile());
        JsonNode model = JSON.readTree(DIR.resolve("model/en/model.json").toFile());

        Map<String, Set<String>> columns = new HashMap<>();
        for (JsonNode table : db.get("tables")) {
            Set<String> names = new HashSet<>();
            table.get("columns").forEach(c -> names.add(c.get("name").asString()));
            columns.put(table.get("name").asString(), names);
        }
        assertEquals(24, columns.size());

        Set<String> relations = new HashSet<>();
        for (JsonNode r : db.get("relations")) {
            relations.add(r.get("name").asString());
            String start = r.get("startTable").asString();
            String end = r.get("endTable").asString();
            r.get("startColumns")
                    .forEach(
                            c ->
                                    assertTrue(
                                            columns.get(start).contains(c.asString()),
                                            r.get("name").asString()
                                                    + ": no "
                                                    + start
                                                    + "."
                                                    + c.asString()));
            r.get("endColumns")
                    .forEach(
                            c ->
                                    assertTrue(
                                            columns.get(end).contains(c.asString()),
                                            r.get("name").asString()
                                                    + ": no "
                                                    + end
                                                    + "."
                                                    + c.asString()));
            assertEquals(r.get("startColumns").size(), r.get("endColumns").size());
        }

        for (JsonNode entity : model.get("entities")) {
            String table = entity.get("table").asString();
            assertTrue(columns.containsKey(table), "entity of unknown table " + table);
            for (JsonNode a : entity.get("attributes")) {
                assertTrue(
                        columns.get(table).contains(a.get("column").asString()),
                        table + ": unknown column " + a.get("column").asString());
            }
        }

        Set<String> entityNames = new HashSet<>();
        model.get("entities").forEach(e -> entityNames.add(e.get("name").asString()));

        Set<String> linked = new HashSet<>();
        Set<String> linkNames = new HashSet<>();
        for (JsonNode link : model.get("links")) {
            String linkName = link.get("name").asString();
            assertTrue(linkName.startsWith("same_"), linkName);
            assertTrue(!entityNames.contains(linkName), "link clashes with an entity: " + linkName);
            assertTrue(linkNames.add(linkName), "duplicate link " + linkName);
            for (JsonNode name : link.get("relations")) {
                assertTrue(relations.contains(name.asString()), "unknown relation " + name);
                assertTrue(linked.add(name.asString()), name + " is in two links");
            }
        }
        assertEquals(relations, linked, "every relation must be reachable through a link");
    }
}
