/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;

public class VfModuleAaiFixtureTest {
    private static final String PREFIX = "/aai/v24/network/generic-vnfs";
    private static final String VNF = PREFIX + "/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721";
    private static final String MODULE = VNF + "/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();
    private WireMockServer server;

    @Before
    public void startFixtureServer() throws Exception {
        Path root = temporary.newFolder("wiremock").toPath();
        for (String name : Arrays.asList("aaiFault.xml", "GenericFlows/vf-module-vnf-1-collection.json",
                "GenericFlows/vf-module-vnf-1.json", "GenericFlows/vf-module-vnf-2-collection.json",
                "GenericFlows/vf-module-vnf-2.json", "GenericFlows/vf-module-vnf-3.json",
                "GenericFlows/vf-module-vnf-4.json", "GenericFlows/vf-module-vnf-5.json",
                "GenericFlows/vf-module-vnf-6.json")) {
            Path target = root.resolve("__files").resolve(name);
            Files.createDirectories(target.getParent());
            try (InputStream source = getClass().getResourceAsStream("/__files/" + name)) {
                assertNotNull(name, source);
                Files.copy(source, target);
            }
        }
        server = new WireMockServer(wireMockConfig().dynamicPort().withRootDirectory(root.toString()));
        server.start();
        new MockAAIGenericVnfSearch(server);
        new MockAAIDeleteVfModule(server);
        new MockAAIDeleteGenericVnf(server);
        CreateAAIVfModuleIT.MockAAICreateGenericVnf(server);
        CreateAAIVfModuleIT.MockAAIVfModulePUT(server, true);
    }

    @After
    public void stopFixtureServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void searchResponsesAreTypedCollectionsRegardlessOfQueryParameterOrder() throws Exception {
        Response response = request("GET", PREFIX + "?depth=1&vnf-name=STMTN5MMSC21", null);
        assertEquals(200, response.status);
        JsonNode collection = MAPPER.readTree(response.body).path("generic-vnf");
        assertTrue(collection.isArray());
        assertEquals(1, collection.size());
        assertEquals("STMTN5MMSC21", collection.get(0).path("vnf-name").asText());
        assertTrue(collection.get(0).path("in-maint").isBoolean());
        assertTrue(collection.get(0).path("vf-modules").path("vf-module").isArray());
        assertEquals("1508691", collection.get(0).path("resource-version").asText());
    }

    @Test
    public void directLookupsReturnOneTypedResourceWithOrWithoutDepth() throws Exception {
        for (String suffix : Arrays.asList("", "?depth=1")) {
            Response response = request("GET", VNF + suffix, null);
            assertEquals(200, response.status);
            JsonNode resource = MAPPER.readTree(response.body);
            assertEquals("a27ce5a9-29c4-4c22-a017-6615ac73c721", resource.path("vnf-id").asText());
            assertEquals("0000021", resource.path("resource-version").asText());
            assertTrue(resource.path("vf-modules").path("vf-module").get(0).path("is-base-vf-module").isBoolean());
        }
    }

    @Test
    public void moduleLookupsReturnTheirOwnIdentityAndVersion() throws Exception {
        Response base = request("GET", MODULE, null);
        assertEquals(200, base.status);
        JsonNode module = MAPPER.readTree(base.body);
        assertEquals("973ed047-d251-4fb9-bf1a-65b8949e0a73", module.path("vf-module-id").asText());
        assertEquals("0000073", module.path("resource-version").asText());
        assertTrue(module.path("is-base-vf-module").isBoolean());
        String addOn = PREFIX + "/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720/vf-modules/vf-module/"
                + "973ed047-d251-4fb9-bf1a-65b8949e0a75";
        Response response = request("GET", addOn, null);
        assertEquals(200, response.status);
        JsonNode child = MAPPER.readTree(response.body);
        assertEquals("0000075", child.path("resource-version").asText());
        assertEquals("973ed047-d251-4fb9-bf1a-65b8949e0a75", child.path("vf-module-id").asText());
        assertEquals(404, request("GET", VNF + "/vf-modules/vf-module/missing", null).status);
    }

    @Test
    public void missingAndUnavailableRecordsKeepTheirIntendedStatusCodes() throws Exception {
        assertEquals(404, request("GET", PREFIX + "?vnf-name=STMTN5MMSC22&depth=1", null).status);
        assertEquals(500, request("GET", PREFIX + "?vnf-name=STMTN5MMSC23&depth=1", null).status);
        assertEquals(404, request("GET", PREFIX + "/generic-vnf/768073c7-f41f-4822-9323-b75962763d74", null).status);
        assertEquals(500, request("GET", PREFIX + "/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c723", null).status);
    }

    @Test
    public void jsonServiceIdsSelectCreationAndRejectionWithoutMatchingModuleWrites() throws Exception {
        String path = PREFIX + "/generic-vnf/generated-id";
        assertEquals(201, request("PUT", path, "{\"service-id\":\"00000000-0000-0000-0000-000000000000\"}").status);
        assertEquals(500, request("PUT", path, "{\"service-id\":\"99999999-9999-9999-9999-999999999999\"}").status);
        assertEquals(404, request("PUT", path, "{\"service-id\":\"unknown\"}").status);
        assertEquals(201, request("PUT", MODULE,
                "{\"vf-module-name\":\"MMSC module\",\"service-id\":\"99999999-9999-9999-9999-999999999999\"}").status);
    }

    @Test
    public void moduleDeletesRequireTheRightVersionOnTheCurrentPath() throws Exception {
        assertEquals(200, request("DELETE", MODULE + "?resource-version=0000073", null).status);
        assertEquals(404, request("DELETE", MODULE + "?resource-version=wrong", null).status);
    }

    @Test
    public void genericDeletesKeepSuccessAndFailureFixturesDistinct() throws Exception {
        assertEquals(200, request("DELETE", VNF + "?resource-version=0000021", null).status);
        assertEquals(500, request("DELETE",
                PREFIX + "/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718?resource-version=0000018", null).status);
        assertEquals(404, request("DELETE", VNF + "?resource-version=wrong", null).status);
    }

    private Response request(String method, String path, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(server.baseUrl() + path).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            try (InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                return new Response(status,
                        stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    private static class Response {
        private final int status;
        private final String body;

        private Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}
