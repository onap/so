/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.ls.LSInput;

public class PathResourceResolverTest {

    private static final String EXISTING_PATH = "src/test/resources/PathResourceResolverTest/";
    private static final String EXISTING_FILE = "filename";
    private static final String UNEXISTING_PATH = "this/path/does/not/exist/";

    @Test
    public void shouldResolveResourceIfFileExists() {

        PathResourceResolver resolver = new PathResourceResolver(EXISTING_PATH);

        String publicId = "publicId";
        String baseUri = "baseUri";
        String type = "type";
        String namespaceURI = "namespaceURI";
        LSInput lsinput = resolver.resolveResource(type, namespaceURI, publicId, EXISTING_FILE, baseUri);

        assertEquals(publicId, lsinput.getPublicId());
        assertEquals(EXISTING_FILE, lsinput.getSystemId());
        assertEquals(baseUri, lsinput.getBaseURI());
    }

    @Test
    public void shouldReturnNullIfFileDoesNotExist() {

        PathResourceResolver resolver = new PathResourceResolver(UNEXISTING_PATH);

        String publicId = "publicId";
        String baseUri = "baseUri";
        String type = "type";
        String namespaceURI = "namespaceURI";

        assertNull(resolver.resolveResource(type, namespaceURI, publicId, EXISTING_FILE, baseUri));
    }

}