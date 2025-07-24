/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.configuration.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onap.so.configuration.rest.BasicHttpHeadersProvider.AUTHORIZATION_HEADER;
import java.util.Arrays;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


/**
 * @author waqas.ikram@est.tech
 *
 */
public class BasicHttpHeadersProviderTest {

    private static final String BASIC_AUTH_VALUE = "Basic AuthValue";

    @Test
    public void test_getHttpHeaders_ContentTypeIsJson() {
        final HttpHeadersProvider objUnderTest = new BasicHttpHeadersProvider();
        final HttpHeaders actualHttpHeaders = objUnderTest.getHttpHeaders();
        assertNull(actualHttpHeaders.get(AUTHORIZATION_HEADER));
        assertEquals(MediaType.APPLICATION_JSON, actualHttpHeaders.getContentType());
    }

    @Test
    public void test_getHttpHeaders_ContainAuthorizationHeader() {
        final HttpHeadersProvider objUnderTest = new BasicHttpHeadersProvider(BASIC_AUTH_VALUE);
        final HttpHeaders actualHttpHeaders = objUnderTest.getHttpHeaders();
        assertEquals(Arrays.asList(BASIC_AUTH_VALUE), actualHttpHeaders.get(AUTHORIZATION_HEADER));
        assertEquals(MediaType.APPLICATION_JSON, actualHttpHeaders.getContentType());
    }

}
