/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.vnf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import com.google.common.io.Files;

/**
 * The purpose of this class is to create a CSAR byte array from Vdu inputs for the purpose of forwarding to a TOSCA
 * orchestrator.
 * 
 * @author DeWayne
 *
 */
public class CSAR {
    private static final String MANIFEST_FILENAME = "MANIFEST.MF";
    private VduModelInfo vduModel;

    public CSAR(VduModelInfo model) {
        this.vduModel = model;
    }

    /**
     * Creates a byte array representation of a CSAR corresponding to the VduBlueprint arg in the constructor.
     * 
     * @return
     * @throws VnfException
     */
    public byte[] create() {
        File dir = Files.createTempDir();

        /**
         * Create subdir
         */
        File metadir = new File(dir.getAbsolutePath() + "/TOSCA-Metadata");
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

        if (mainTemplate == null) { // make a dummy to avoid null pointers
            mainTemplate = new VduArtifact("", new byte[0], null);
        }

        /**
         * Write template files
         */
        try (OutputStream ofs = new FileOutputStream(new File(dir, mainTemplate.getName()));
                PrintStream mfstream =
                        new PrintStream(new File(metadir.getAbsolutePath() + '/' + MANIFEST_FILENAME));) {
            ofs.write(mainTemplate.getContent());

            /**
             * Write other files
             */
            if (!extraFiles.isEmpty()) {
                for (VduArtifact artifact : extraFiles) {
                    try (OutputStream out = new FileOutputStream(new File(dir, artifact.getName()));) {
                        out.write(artifact.getContent());
                    }
                }
            }


            /**
             * Create manifest
             */
            mfstream.println("TOSCA-Meta-File-Version: 1.0");
            mfstream.println("CSAR-Version: 1.1");
            mfstream.println("Created-by: ONAP");
            mfstream.println("Entry-Definitions: " + mainTemplate.getName());

            /**
             * ZIP it up
             */
            ByteArrayOutputStream zipbytes = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipbytes);
            compressTree(zos, "", dir, dir);
            zos.close();
            return zipbytes.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create CSAR: " + e.getMessage());
        } finally {
            /**
             * Clean up tmpdir
             */
            deleteDirectory(dir);
        }
    }

    /**
     * Private methods
     */

    /**
     * Compresses (ZIPs) a directory tree
     * 
     * @param dir
     * @throws IOException
     */
    private void compressTree(ZipOutputStream zos, String path, File basedir, File dir) throws IOException {
        if (!dir.isDirectory())
            return;

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                String newpath = path + f.getName() + '/';
                ZipEntry entry = new ZipEntry(newpath);
                zos.putNextEntry(entry);
                zos.closeEntry();
                compressTree(zos, newpath, basedir, f);
            } else {
                ZipEntry ze = new ZipEntry(
                        f.getAbsolutePath().substring(basedir.getAbsolutePath().length() + 1).replaceAll("\\\\", "/"));
                zos.putNextEntry(ze);
                // read the file and write to ZipOutputStream
                try (FileInputStream fis = new FileInputStream(f);) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
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
