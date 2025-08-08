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

package org.onap.so.apihandler.common;

import static org.junit.Assert.assertEquals;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ResponseBuilderTest extends BaseTest {

    @Autowired
    private ResponseBuilder builder;

    @Test
    public void testBuildResponseResponse() {

        String requestId = null;
        String apiVersion = "1";
        String jsonResponse = "Successfully started the process";

        Response response = builder.buildResponse(HttpStatus.SC_ACCEPTED, requestId, jsonResponse, apiVersion);

        assertEquals(202, response.getStatus());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));

    }

    @Test
    public void testBuildResponseVersion() {

        String requestId = "123456-67889";
        String apiVersion = "v5";
        String jsonResponse = "Successfully started the process";

        Response response = builder.buildResponse(HttpStatus.SC_CREATED, requestId, jsonResponse, apiVersion);

        assertEquals(201, response.getStatus());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("5.0.0", response.getHeaders().get("X-LatestVersion").get(0));

    }

}
