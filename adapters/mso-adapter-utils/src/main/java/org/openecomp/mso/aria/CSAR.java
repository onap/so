/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2017 Cloudify.co.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END====================================================
 */
package org.openecomp.mso.aria;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.openecomp.mso.adapters.vdu.VduArtifact;
import org.openecomp.mso.adapters.vdu.VduArtifact.ArtifactType;
import org.openecomp.mso.adapters.vdu.VduModelInfo;

/**
 * The purpose of this class is to create a CSAR byte array from Vdu inputs for the purpose of forwarding to a TOSCA
 * orchestrator.
 *
 * @author DeWayne
 */
public class CSAR {

    private static final String MANIFEST_FILENAME = "MANIFEST.MF";
    private VduModelInfo vduModel;

    public CSAR(VduModelInfo model) {
        this.vduModel = model;
    }

    /**
     * Creates a byte array representation of a CSAR corresponding to the VduBlueprint arg in the constructor.
     */
    public byte[] create() {
        File dir = Files.createTempDir();

        /**
         * Create subdir
         */
        File metadir = new File(dir.getAbsolutePath() + "/" + "TOSCA-Metadata");
        if (!metadir.mkdir()) {
            throw new RuntimeException("CSAR TOSCA-Metadata directory create failed");
        }

        /**
         * Organize model info for consumption
         */
        VduArtifact mainTemplate = null;
        List<VduArtifact> extraFiles = new ArrayList<>();
        for (VduArtifact artifact : vduModel.getArtifacts()) {
            if (artifact.getType() == ArtifactType.MAIN_TEMPLATE) {
                mainTemplate = artifact;
            } else {
                extraFiles.add(artifact);
            }
        }

        try {
            writeMainTemplateFile(dir, mainTemplate);

            writeExtraFiles(dir, extraFiles);

            createManifest(metadir, mainTemplate);

            try (TreeCompressor compressor = new TreeCompressor()) {
                return compressor.compressTree("", dir, dir);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to create CSAR: " + e.getMessage());
        } finally {

            /**
             * Clean up tmpdir
             */
            deleteDirectory(dir);
        }
    }

    private void writeMainTemplateFile(File dir, VduArtifact mainTemplate) throws IOException {
        if (mainTemplate != null) {
            writeFile(dir, mainTemplate);
        }
    }

    private void writeExtraFiles(File dir, List<VduArtifact> extraFiles) throws IOException {
        if (extraFiles != null) {
            for (VduArtifact artifact : extraFiles) {
                writeFile(dir, artifact);
            }
        }
    }

    private void writeFile(File dir, VduArtifact artifact) throws IOException {
        try (OutputStream fos = new FileOutputStream(new File(dir, artifact.getName()))) {
            fos.write(artifact.getContent());
        }
    }

    private void createManifest(File metadir, VduArtifact mainTemplate) throws FileNotFoundException {
        if (mainTemplate != null) {
            try (PrintStream mfstream = new PrintStream(
                new File(metadir.getAbsolutePath() + "/" + MANIFEST_FILENAME))) {
                mfstream.println("TOSCA-Meta-File-Version: 1.0");
                mfstream.println("CSAR-Version: 1.1");
                mfstream.println("Created-by: ONAP");
                mfstream.println("Entry-Definitions: " + mainTemplate.getName());
            }
        }
    }

    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }
}
