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
package org.openecomp.mso.adapters.vnf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openecomp.mso.vdu.utils.VduBlueprint;

import com.google.common.io.Files;

/**
 * The purpose of this class is to create a CSAR byte array from Vdu inputs for the purpose
 * of forwarding to a TOSCA orchestrator.
 * 
 * @author DeWayne
 *
 */
public class CSAR {
	private static final String MANIFEST_FILENAME = "MANIFEST.MF";

	private VduBlueprint blueprint;


	public CSAR(VduBlueprint blueprint) {
		this.blueprint = blueprint;
	}

	/**
	 * Creates a byte array representation of a CSAR corresponding to the VduBlueprint arg in the
	 * constructor.  
	 * 
	 * @return
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
		 * Write template files
		 */
		OutputStream ofs = null;
		try {
			ofs = new FileOutputStream(new File(dir, blueprint.getMainTemplateName()));
			ofs.write(blueprint.getTemplateFiles().get(blueprint.getMainTemplateName()));
			ofs.close();

			/**
			 * Write other files
			 */
			if (blueprint.getTemplateFiles() != null) {
				for (Map.Entry<String, byte[]> entry : blueprint.getTemplateFiles().entrySet()) {
					if (!entry.getKey().equals(blueprint.getMainTemplateName())) {
						ofs = new FileOutputStream(new File(dir, entry.getKey()));
						ofs.write(entry.getValue());
						ofs.close();
					}
				}
			}

			/**
			 * Write attached files
			 */
			if (blueprint.getAttachedFiles() != null) {
				for (Map.Entry<String, byte[]> entry : blueprint.getAttachedFiles().entrySet()) {
					ofs = new FileOutputStream(new File(dir, entry.getKey()));
					ofs.write(entry.getValue());
					ofs.close();
				}
			}

			/**
			 * Create manifest
			 */
			PrintStream mfstream = new PrintStream(new File(metadir.getAbsolutePath() + "/" + MANIFEST_FILENAME));
			mfstream.println("TOSCA-Meta-File-Version: 1.0");
			mfstream.println("CSAR-Version: 1.1");
			mfstream.println("Created-by: ONAP");
			mfstream.println("Entry-Definitions: " + blueprint.getMainTemplateName());
			mfstream.close();

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
				String newpath = path + f.getName() + "/";
				ZipEntry entry = new ZipEntry(newpath);
				zos.putNextEntry(entry);
				zos.closeEntry();
				compressTree(zos, newpath, basedir, f);
			} else {
				ZipEntry ze = new ZipEntry(
						f.getAbsolutePath().substring(basedir.getAbsolutePath().length() + 1).replaceAll("\\\\", "/"));
				zos.putNextEntry(ze);
				// read the file and write to ZipOutputStream
				FileInputStream fis = new FileInputStream(f);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				zos.closeEntry();
				fis.close();
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
