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
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
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
        MultivaluedMap<String, Pair<String, String>> map = new MultivaluedHashMap<>();
        client.initializeHeaderMap(map);
        map.add("ALL", Pair.with(ONAPLogConstants.MDCs.REQUEST_ID, "1234"));
        assertNotNull(map);
        assertEquals("Found expected RequestId", true,
                map.get("ALL").contains(Pair.with(ONAPLogConstants.MDCs.REQUEST_ID, "1234")));

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
        MultivaluedMap<String, Pair<String, String>> map = new MultivaluedHashMap<>();
        client.initializeHeaderMap(map);

        assertNotNull(map);
        assertEquals("Found expected RequestId", true,
                map.get("ALL").contains(Pair.with(ONAPLogConstants.Headers.INVOCATION_ID, "1234")));

    }

    @Test
    public void headerMapInvocationIdNullTest() {

        try {
            url = new URL("http://testhost.com");
        } catch (MalformedURLException e) {

            throw new RuntimeException(e);
        }

        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, null);
        DMaaPRestClient client = new DMaaPRestClient(url, contentType, auth, key);
        MultivaluedMap<String, Pair<String, String>> map = new MultivaluedHashMap<>();
        client.initializeHeaderMap(map);

        assertNotNull(map);
        assertEquals("header not found as expected", false,
                map.get("ALL").contains(Pair.with(ONAPLogConstants.Headers.INVOCATION_ID, "1234")));

    }
}
