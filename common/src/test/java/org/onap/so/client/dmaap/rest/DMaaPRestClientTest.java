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

package org.onap.so.client.dmaap.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.constants.HeaderConstants;
import org.slf4j.MDC;

public class DMaaPRestClientTest {

    URL url;
    private final String contentType = "application/json";
    private final String auth = "";
    private final String key = "";


    @Test
    public void headerMapTest() {

        try {
            url = new URL("http://testhost.com");
        } catch (MalformedURLException e) {

            throw new RuntimeException(e);
        }
        DMaaPRestClient client = new DMaaPRestClient(url, contentType, auth, key);
        Map<String, String> map = new HashMap<>();
        client.initializeHeaderMap(map);
        map.put(ONAPLogConstants.MDCs.REQUEST_ID, "1234");
        assertNotNull(map);
        assertEquals("Found expected RequesttId", "1234", map.get(ONAPLogConstants.MDCs.REQUEST_ID));
        assertEquals("Found expected X-ClientId", "SO", map.get(HeaderConstants.CLIENT_ID));

    }

    @Test
    public void headerMapInvocationIdNotNullTest() {

        try {
            url = new URL("http://testhost.com");
        } catch (MalformedURLException e) {

            throw new RuntimeException(e);
        }
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, "1234");
        DMaaPRestClient client = new DMaaPRestClient(url, contentType, auth, key);
        Map<String, String> map = new HashMap<>();
        client.initializeHeaderMap(map);

        assertNotNull(map);
        assertEquals("Found expected RequestId", "1234", map.get(ONAPLogConstants.Headers.INVOCATION_ID));
        assertEquals("Found expected X-ClientId", "SO", map.get(HeaderConstants.CLIENT_ID));

    }

    @Test
    public void headerMapInvocationIdNullTest() {

        try {
            url = new URL("http://testhost.com");
        } catch (MalformedURLException e) {

            throw new RuntimeException(e);
        }

        DMaaPRestClient client = new DMaaPRestClient(url, contentType, auth, key);
        Map<String, String> map = new HashMap<>();
        client.initializeHeaderMap(map);

        assertNotNull(map);
        assertEquals("header not found as expected", null, map.get(ONAPLogConstants.Headers.INVOCATION_ID));
        assertEquals("Found expected X-ClientId", "SO", map.get(HeaderConstants.CLIENT_ID));

    }
}
