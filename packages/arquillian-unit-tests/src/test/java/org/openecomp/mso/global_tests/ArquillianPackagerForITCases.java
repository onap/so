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

package org.openecomp.mso.global_tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class ArquillianPackagerForITCases {

	public static Archive<?> createPackageFromExistingOne(String path, String globPattern, String newPackageName) {
		Path dir = Paths.get(path);

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, globPattern)) {
			Iterator<Path> it = stream.iterator();
			if (it.hasNext()) {

				if (newPackageName.endsWith(".war")) {
					File archive = it.next().toFile();
					WebArchive webArchive = ShrinkWrap.create(WebArchive.class, newPackageName);
					webArchive.merge((ShrinkWrap.createFromZipFile(WebArchive.class, archive)));
					return webArchive;
				} else if (newPackageName.endsWith(".jar")) {
					File archive = it.next().toFile();
					JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, newPackageName);
					javaArchive.merge((ShrinkWrap.createFromZipFile(JavaArchive.class, archive)));
					return javaArchive;
				} else if (newPackageName.endsWith(".ear")) {
					File archive = it.next().toFile();
					EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class, newPackageName);
					earArchive.merge((ShrinkWrap.createFromZipFile(EnterpriseArchive.class, archive)));
					return earArchive;
				} else {
					return null;
				}

			} else {
				return null;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
}
