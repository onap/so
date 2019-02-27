/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.mock;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * File utility class.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP  Sep 15, 2017
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    
	/**
	 * Read the specified resource file and return the contents as a String.
	 * 
	 * @param fileName Name of the resource file
	 * @return the contents of the resource file as a String
	 * @throws IOException if there is a problem reading the file
	 */
	public static String readResourceFile(String fileName) {
		InputStream stream;
		try {
			stream = getResourceAsStream(fileName);
			byte[] bytes;
			bytes = new byte[stream.available()];
			if(stream.read(bytes) > 0) {
				stream.close();
				return new String(bytes);
			} else {
				stream.close();
				return "";
			}
		} catch (IOException e) {
		    logger.debug("Exception:", e);
			return "";
		}
	}
	
	/**
	 * Get an InputStream for the resource specified.
	 * 
	 * @param resourceName Name of resource for which to get InputStream.
	 * @return an InputStream for the resource specified.
	 * @throws IOException If we can't get the InputStream for whatever reason.
	 */
	private static InputStream getResourceAsStream(String resourceName) throws IOException {
		InputStream stream =
				FileUtil.class.getClassLoader().getResourceAsStream(resourceName);
		if (stream == null) {
			throw new IOException("Can't access resource '" + resourceName + "'");
		}
		return stream;
	}		
}
