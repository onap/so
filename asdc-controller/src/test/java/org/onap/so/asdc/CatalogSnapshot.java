/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CatalogSnapshot {

    private static final Path GOLDEN_DIR = Paths.get("src/test/resources/golden/catalog");

    private CatalogSnapshot() {}

    // Auto-increment ids and timestamps depend on what earlier tests inserted, so they are left out of the row.
    private static boolean isVolatile(ResultSetMetaData md, int col) throws SQLException {
        String name = md.getColumnLabel(col).toUpperCase();
        int type = md.getColumnType(col);
        boolean numeric = type == Types.INTEGER || type == Types.BIGINT || type == Types.SMALLINT;
        boolean temporal = type == Types.TIMESTAMP || type == Types.DATE || type == Types.TIME
                || type == Types.TIMESTAMP_WITH_TIMEZONE;
        return temporal || (numeric && (name.equals("ID") || name.endsWith("_ID")));
    }

    public static Map<String, List<String>> capture(Connection connection) throws SQLException {
        Map<String, List<String>> tables = new TreeMap<>();
        List<String> names = new ArrayList<>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'")) {
            while (rs.next()) {
                String name = rs.getString(1);
                if (!name.toUpperCase().startsWith("DATABASECHANGELOG")) {
                    names.add(name);
                }
            }
        }
        for (String table : names) {
            List<String> rows = new ArrayList<>();
            try (Statement st = connection.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int c = 1; c <= md.getColumnCount(); c++) {
                        if (isVolatile(md, c)) {
                            continue;
                        }
                        String v = rs.getString(c);
                        row.append(md.getColumnLabel(c).toUpperCase()).append('=')
                                .append(v == null ? "<null>"
                                        : v.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r"))
                                .append(" | ");
                    }
                    rows.add(row.toString());
                }
            }
            Collections.sort(rows);
            tables.put(table.toUpperCase(), rows);
        }
        return tables;
    }

    public static Map<String, List<String>> delta(Map<String, List<String>> before, Map<String, List<String>> after) {
        Map<String, List<String>> delta = new TreeMap<>();
        after.forEach((table, rows) -> {
            List<String> added = new ArrayList<>(rows);
            for (String old : before.getOrDefault(table, Collections.emptyList())) {
                added.remove(old);
            }
            if (!added.isEmpty()) {
                delta.put(table, added);
            }
        });
        return delta;
    }

    public static void assertMatchesGolden(String name, Map<String, List<String>> delta) throws IOException {
        assertFalse("Distribution '" + name + "' wrote no catalog rows. Either the snapshot read the wrong "
                + "connection, or this is a surefire rerun of an already-deployed CSAR; check the first attempt.",
                delta.isEmpty());
        StringBuilder actual = new StringBuilder();
        delta.forEach((table, rows) -> {
            actual.append("## ").append(table).append('\n');
            rows.forEach(r -> actual.append(r).append('\n'));
        });
        Path golden = GOLDEN_DIR.resolve(name + ".txt");
        if (Boolean.getBoolean("updateGolden")) {
            Files.createDirectories(GOLDEN_DIR);
            Files.write(golden, actual.toString().getBytes(StandardCharsets.UTF_8));
            return;
        }
        String expected = new String(Files.readAllBytes(golden), StandardCharsets.UTF_8);
        assertEquals("Catalog rows for '" + name + "' changed", expected, actual.toString());
    }
}
