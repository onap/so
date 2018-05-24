/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.aria;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Test;

public class TreeCompressorTest {

    @Test
    public void testShouldReturnEmptyByteArrayWhenFileCompressed() throws Exception {
        // given
        Path filePath = createTempFile("", "tempFile.txt");
        File file = filePath.toFile();
        writeTempTextToFIle(filePath);

        // when/then
        try (TreeCompressor compressor = new TreeCompressor()) {
            byte[] bytes = compressor.compressTree("", file, file);

            assertArrayEquals(bytes, new byte[0]);
        }

        // cleanup
        Files.delete(filePath);
    }

    @Test
    public void testCompressingEmptyDir() throws Exception {
        // given
        Path dirPath = createTempDirectory("");
        File dir = dirPath.toFile();

        // when/then
        try (TreeCompressor compressor = new TreeCompressor()) {
            byte[] bytes = compressor.compressTree("", dir, dir);

            assertArrayEquals(bytes, new byte[0]);
        }

        // cleanup
        Files.delete(dirPath);
    }

    @Test
    public void testCompressingFileInDir() throws Exception {
        // given
        Path dirPath = createTempDirectory("");
        File dir = dirPath.toFile();
        Path filePath = createTempFile(dirPath, "temp", ".txt");
        writeTempTextToFIle(filePath);

        // when/then
        try (TreeCompressor compressor = new TreeCompressor()) {
            byte[] compressedData = compressor.compressTree("", dir, dir);

            try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(compressedData))) {
                String fileName = getNextZipEntryName(zipInputStream);
                assertEquals(dirPath + "/" + fileName, filePath.toString());
            }
        }

        // cleanup
        Files.delete(filePath);
        Files.delete(dirPath);
    }

    @Test
    public void testCompressingMultilevelDirs() throws Exception {
        // given
        Path baseDirPath = createTempDirectory("");
        File baseDir = baseDirPath.toFile();
        Path secondDirPath = createTempDirectory(baseDirPath, "");
        Path filePath = createTempFile(secondDirPath, "temp", ".txt");
        writeTempTextToFIle(filePath);

        // when/then
        try (TreeCompressor compressor = new TreeCompressor()) {
            byte[] compressedData = compressor.compressTree("", baseDir, baseDir);
            try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(compressedData))) {

                String secondDirName = getNextZipEntryName(zipInputStream);
                String fileName = getNextZipEntryName(zipInputStream);

                assertEquals(baseDirPath + "/" + secondDirName, secondDirPath.toString() + '/');
                assertEquals(baseDirPath + "/" + fileName, filePath.toString());
            }
        }

        // cleanup
        Files.delete(filePath);
        Files.delete(secondDirPath);
        Files.delete(baseDirPath);
    }

    private String getNextZipEntryName(ZipInputStream zipInputStream) throws IOException {
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        return zipEntry.getName();
    }

    private void writeTempTextToFIle(Path filePath) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(filePath.toString())) {
            printWriter.write("Lorem ipsum");
            printWriter.write("dolor sit amet");
        }
    }
}
