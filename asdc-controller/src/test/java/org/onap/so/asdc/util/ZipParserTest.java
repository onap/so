/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.util;

import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipParserTest {

    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("zipparsertest");
    }

    @After
    public void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    public void testSingletonInstance() {
        ZipParser instance1 = ZipParser.getInstance();
        ZipParser instance2 = ZipParser.getInstance();
        assertSame("Both instances must be same (singleton)", instance1, instance2);
    }

    @Test
    public void testParseJsonForZip_withJsonFile() throws IOException {
        // Create a zip file with one JSON file inside
        Path zipFilePath = tempDir.resolve("testJson.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            ZipEntry entry = new ZipEntry("data.json");
            zos.putNextEntry(entry);
            String jsonContent = "{ \"name\": \"test\", \"value\": 123 }";
            zos.write(jsonContent.getBytes("UTF-8"));
            zos.closeEntry();
        }

        String parsedContent = ZipParser.getInstance().parseJsonForZip(zipFilePath.toString());
        // Expected content with quotes escaped and all whitespace removed
        String expected = "{\\\"name\\\":\\\"test\\\",\\\"value\\\":123}";
        assertEquals(expected, parsedContent);
    }

    @Test
    public void testParseJsonForZip_noJsonFile() throws IOException {
        // Create a zip file without any JSON files inside
        Path zipFilePath = tempDir.resolve("testNoJson.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            ZipEntry entry = new ZipEntry("readme.txt");
            zos.putNextEntry(entry);
            String textContent = "This is a text file, no json here.";
            zos.write(textContent.getBytes("UTF-8"));
            zos.closeEntry();
        }

        String parsedContent = ZipParser.getInstance().parseJsonForZip(zipFilePath.toString());
        assertNull("No JSON file, should return null", parsedContent);
    }

    @Test(expected = IOException.class)
    public void testParseJsonForZip_invalidPath() throws IOException {
        // Non-existent file path should throw IOException
        ZipParser.getInstance().parseJsonForZip("non_existent_file.zip");
    }
}
