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

package org.onap.so.client.sdno;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;
import org.onap.so.client.sdno.beans.SDNO;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDNOHealthCheckClientTest {



    private final String fileLocation = "src/test/resources/org/onap/so/client/sdno/health-check/";
    private static final String userId = "test-user";
    private static final Optional<String> clliCode = Optional.of("test-clli");
    private static final String requestId = "test-request-id";
    private static final String configurationId = "test-configuration-id";
    private static final String interfaceId = "test-interface-id";

    @Test
    public void verfyLPortMirrorPreCheckRequest() throws IOException {
        String content = this.getJson("custom-lport-mirror-pre-check-request.json");
        ObjectMapper mapper = new ObjectMapper();
        SDNO expected = mapper.readValue(content, SDNO.class);
        SDNOHealthCheckClient client = new SDNOHealthCheckClient();
        String actual =
                client.buildLPortMirrorCheckPreRequest(userId, requestId, clliCode, configurationId, interfaceId);
        assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);
    }

    @Test
    public void verfyLPortMirrorPostCheckRequest() throws IOException {
        String content = this.getJson("custom-lport-mirror-post-check-request.json");
        ObjectMapper mapper = new ObjectMapper();
        SDNO expected = mapper.readValue(content, SDNO.class);
        SDNOHealthCheckClient client = new SDNOHealthCheckClient();
        String actual =
                client.buildLPortMirrorCheckPostRequest(userId, requestId, clliCode, configurationId, interfaceId);
        assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);
    }


    @Test
    public void verifyPortMirrorPostCheckRequest() throws IOException {
        String content = this.getJson("custom-port-mirror-post-check-request.json");
        ObjectMapper mapper = new ObjectMapper();
        SDNO expected = mapper.readValue(content, SDNO.class);
        SDNOHealthCheckClient client = new SDNOHealthCheckClient();
        String actual = client.buildPortMirrorPostCheckRequest(userId, requestId, clliCode, configurationId);

        assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);

    }

    @Test
    public void verifyPortMirrorPreCheckRequest() throws IOException {
        String content = this.getJson("custom-port-mirror-pre-check-request.json");
        ObjectMapper mapper = new ObjectMapper();
        SDNO expected = mapper.readValue(content, SDNO.class);
        SDNOHealthCheckClient client = new SDNOHealthCheckClient();
        String actual = client.buildPortMirrorPreCheckRequest(userId, requestId, clliCode, configurationId);

        assertEquals("payloads are equal", mapper.writeValueAsString(expected), actual);

    }

    private String getJson(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileLocation + filename)));
    }

}
