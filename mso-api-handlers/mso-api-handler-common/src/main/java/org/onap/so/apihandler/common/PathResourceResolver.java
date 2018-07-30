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

package org.onap.so.apihandler.common;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.onap.so.logger.MsoLogger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class PathResourceResolver implements LSResourceResolver {
	
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.APIH, PathResourceResolver.class);

    private String path;
	
    public PathResourceResolver(String path) {
    	
    	this.path = path;
    }
    
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
 
        LSInputImpl input = new LSInputImpl();
 
        InputStream stream = null;
		try {
			stream = new FileInputStream(path + systemId);
		} catch (FileNotFoundException e) {
		    msoLogger.debug ("Could not resolve resource based on file: " + path + systemId, e);
		}
 
        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setCharacterStream(new InputStreamReader(stream));
 
        return input;
    }
}
