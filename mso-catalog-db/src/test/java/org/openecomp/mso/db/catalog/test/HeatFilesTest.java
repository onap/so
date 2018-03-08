/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.HeatFiles;

/**
 */

public class HeatFilesTest {

    @Test
    public final void heatFilesTest() {

        HeatFiles heatFiles = new HeatFiles();
        heatFiles.setFileBody("testBody");
        heatFiles.setArtifactUuid(UUID.randomUUID().toString());
        assertTrue(heatFiles.getFileBody().equals("testBody"));
        assertTrue(!heatFiles.toString().contains("8 chars"));
        heatFiles.setFileBody(null);
        assertTrue(!heatFiles.toString().contains("Not defined"));
        heatFiles.setVersion("12");
        assertTrue(heatFiles.getVersion().equals("12"));

        heatFiles.setFileName("File");
        assertTrue(heatFiles.getFileName().equalsIgnoreCase("File"));

        heatFiles.setCreated(null);
        assertTrue(heatFiles.getCreated() == null);
        heatFiles.setAsdcUuid("asdc");

        assertTrue(heatFiles.getAsdcUuid().equalsIgnoreCase("asdc"));

        heatFiles.setDescription("desc");
        assertTrue(heatFiles.getDescription().equalsIgnoreCase("desc"));


        heatFiles.setArtifactChecksum("artifactChecksum");
        assertTrue(heatFiles.getArtifactChecksum().equalsIgnoreCase("artifactChecksum"));
        File tempFile;
        try {
            tempFile = File.createTempFile("heatFiles", "test");
            tempFile.deleteOnExit();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "utf-8"))) {
                writer.write("something\n");
                writer.write("something2\n");
            }
            heatFiles.setFileBody(tempFile.getAbsolutePath());
            assertTrue(heatFiles.getFileBody().contains("test"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception caught");
        }

    }

}
