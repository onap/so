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

package org.onap.so.client.sdn.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.LinkedHashMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SdnCommonTasks;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


public class SdnCommonTasksTest {


    SdnCommonTasks sdnCommonTasks = new SdnCommonTasks();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void buildJsonRequestTest() throws MapperException {
        String jsonStr = sdnCommonTasks.buildJsonRequest("");
        Assert.assertNotNull(jsonStr);
    }

    @Test
    public void buildJsonRequestTestException() throws MapperException {
        expectedException.expect(MapperException.class);
        sdnCommonTasks.buildJsonRequest(new Object());
    }

    @Test
    public void getHttpHeadersTest() {
        HttpHeaders result = sdnCommonTasks.getHttpHeaders("auth", true);

        assertEquals("auth", result.getFirst("Authorization"));
        assertEquals(MediaType.APPLICATION_JSON.toString(), result.getFirst("Content-Type"));
        assertEquals(MediaType.APPLICATION_JSON.toString(), result.getFirst("Accept"));
    }

    @Test
    public void getHttpHeadersGetRequestTest() {
        HttpHeaders result = sdnCommonTasks.getHttpHeaders("auth", false);

        assertEquals("auth", result.getFirst("Authorization"));
        assertEquals(MediaType.APPLICATION_JSON.toString(), result.getFirst("Accept"));
        assertFalse(result.containsKey("Content-Type"));
    }

    @Test
    public void validateSDNResponseTest() throws BadResponseException {
        String jsonResponse = "{\"output\":{\"response-code\":\"0\",\"response-message\":\"success\"}}";
        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> output = new LinkedHashMap<>();
        output.put("response-code", "0");
        output.put("response-message", "success");
        responseMap.put("output", output);
        assertEquals(jsonResponse, sdnCommonTasks.validateSDNResponse(responseMap));
    }

    @Test
    public void validateSDNResponseTestException() throws BadResponseException {
        expectedException.expect(BadResponseException.class);
        LinkedHashMap responseMap = new LinkedHashMap();
        Assert.assertNotNull(sdnCommonTasks.validateSDNResponse(responseMap));
    }

    @Test
    public void validateSDNResponseTestRespCodeNot200() throws BadResponseException {
        expectedException.expect(BadResponseException.class);
        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> output = new LinkedHashMap<>();
        output.put("response-code", "300");
        output.put("response-message", "Failed");
        responseMap.put("output", output);
        sdnCommonTasks.validateSDNResponse(responseMap);
    }

}
