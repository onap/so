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
package org.onap.so.db.catalog.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.springframework.http.HttpHeaders;

/**
 * Integration tests for {@link CatalogDbClient} that verify HTTP headers sent by the bowman client interceptors.
 * <p>
 * Uses WireMock to capture actual HTTP requests and assert that:
 * <ul>
 * <li>The {@code Authorization} header is set to the configured auth value</li>
 * <li>The {@code X-Target-Entity} header is set to {@code SO:CatalogDB}</li>
 * </ul>
 * These assertions ensure that header configuration is preserved when the underlying client is replaced.
 */
public class CatalogDbClientHttpConfigTest {

    private static final String AUTH = "Basic dGVzdDp0ZXN0";
    private static final String TARGET_ENTITY_HEADER = "X-Target-Entity";
    private static final String EXPECTED_TARGET_ENTITY = "SO:CatalogDB";

    @Rule
    public WireMockRule wireMock = new WireMockRule(0);

    private CatalogDbClient catalogDbClient;
    private String baseUrl;

    @Before
    public void setUp() {
        baseUrl = "http://localhost:" + wireMock.port();
        catalogDbClient = new CatalogDbClient(baseUrl, AUTH);
        catalogDbClient.setEndpoint(baseUrl);
        catalogDbClient.init();
    }

    // ==================== getSingleResource — header verification ====================

    @Test
    public void getServiceByID_shouldSendAuthAndTargetEntityHeaders() {
        stubSingleResource("/service/test-uuid", serviceJson("test-uuid", "TestService"));

        Service result = catalogDbClient.getServiceByID("test-uuid");

        assertNotNull(result);
        assertEquals("TestService", result.getModelName());
        verifyGetHeaders("/service/test-uuid");
    }

    @Test
    public void getVnfResourceByModelUUID_shouldSendAuthAndTargetEntityHeaders() {
        stubSingleResource("/vnfResource/vnf-uuid", vnfResourceJson("vnf-uuid", "TestVnf"));

        VnfResource result = catalogDbClient.getVnfResourceByModelUUID("vnf-uuid");

        assertNotNull(result);
        assertEquals("TestVnf", result.getModelName());
        verifyGetHeaders("/vnfResource/vnf-uuid");
    }

    @Test
    public void getCloudSite_shouldSendAuthAndTargetEntityHeaders() {
        stubSingleResource("/cloudSite/cloud-1", cloudSiteJson("cloud-1"));

        CloudSite result = catalogDbClient.getCloudSite("cloud-1");

        assertNotNull(result);
        verifyGetHeaders("/cloudSite/cloud-1");
    }

    @Test
    public void getFirstByServiceModelUUIDAndAction_shouldSendHeaders() {
        String path = "/serviceRecipe/search/findFirstByServiceModelUUIDAndAction"
                + "?serviceModelUUID=model-uuid&action=createInstance";
        stubSingleResource(path, serviceRecipeJson());

        ServiceRecipe result = catalogDbClient.getFirstByServiceModelUUIDAndAction("model-uuid", "createInstance");

        assertNotNull(result);
        wireMock.verify(getRequestedFor(urlEqualTo(path)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    // ==================== getMultipleResources — header verification ====================

    @Test
    public void getOrchestrationFlowByAction_shouldSendAuthAndTargetEntityHeaders() {
        String path = "/orchestrationFlow/search/findByAction?action=createInstance";
        stubCollectionResource(path, "orchestrationFlow", orchestrationFlowJson("1", "AssignServiceBB"));

        List<OrchestrationFlow> result = catalogDbClient.getOrchestrationFlowByAction("createInstance");

        assertNotNull(result);
        assertEquals(1, result.size());
        wireMock.verify(getRequestedFor(urlEqualTo(path)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    @Test
    public void findWorkflowBySource_shouldSendAuthAndTargetEntityHeaders() {
        String path = "/workflow/search/findBySource?source=native";
        stubCollectionResource(path, "workflow", workflowJson("wf-1", "TestWF"));

        List<Workflow> result = catalogDbClient.findWorkflowBySource("native");

        assertNotNull(result);
        assertEquals(1, result.size());
        wireMock.verify(getRequestedFor(urlEqualTo(path)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    @Test
    public void getCloudSites_shouldSendAuthAndTargetEntityHeaders() {
        stubCollectionResource("/cloudSite?size=1000", "cloudSite", cloudSiteJson("site-1"));

        List<CloudSite> result = catalogDbClient.getCloudSites();

        assertNotNull(result);
        wireMock.verify(
                getRequestedFor(urlEqualTo("/cloudSite?size=1000")).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                        .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    // ==================== postSingleResource — header verification ====================

    @Test
    public void postOofHomingCloudSite_shouldSendAuthAndTargetEntityHeaders() {
        wireMock.stubFor(
                post(urlMatching("/cloudSite.*")).willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withHeader("Location", baseUrl + "/cloudSite/new-id").withStatus(201)));

        CloudSite cloudSite = new CloudSite();
        cloudSite.setId("new-id");
        catalogDbClient.postOofHomingCloudSite(cloudSite);

        wireMock.verify(
                postRequestedFor(urlMatching("/cloudSite.*")).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                        .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    @Test
    public void postHomingInstance_shouldSendAuthAndTargetEntityHeaders() {
        wireMock.stubFor(post(urlMatching("/homingInstance.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withHeader("Location", baseUrl + "/homingInstance/new-id").withStatus(201)));

        HomingInstance hi = new HomingInstance();
        catalogDbClient.postHomingInstance(hi);

        wireMock.verify(
                postRequestedFor(urlMatching("/homingInstance.*")).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                        .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    // ==================== deleteSingleResource — header verification ====================

    @Test
    public void deleteServiceRecipe_shouldSendAuthAndTargetEntityHeaders() {
        wireMock.stubFor(delete(urlEqualTo("/serviceRecipe/recipe-123")).willReturn(aResponse().withStatus(204)));

        catalogDbClient.deleteServiceRecipe("recipe-123");

        wireMock.verify(deleteRequestedFor(urlEqualTo("/serviceRecipe/recipe-123"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    // ==================== helpers ====================

    private void stubSingleResource(String path, String jsonBody) {
        wireMock.stubFor(get(urlEqualTo(path)).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(jsonBody).withStatus(200)));
    }

    private void stubCollectionResource(String path, String embeddedKey, String itemJson) {
        String body = "{ \"_embedded\": { \"" + embeddedKey + "\": [" + itemJson + "] } }";
        wireMock.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(body).withStatus(200)));
    }

    private void verifyGetHeaders(String path) {
        wireMock.verify(getRequestedFor(urlEqualTo(path)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH))
                .withHeader(TARGET_ENTITY_HEADER, equalTo(EXPECTED_TARGET_ENTITY)));
    }

    private String serviceJson(String uuid, String name) {
        return "{" + "\"modelName\": \"" + name + "\"," + "\"modelUUID\": \"" + uuid + "\","
                + "\"modelVersion\": \"1.0\"," + "\"_links\": {" + "  \"self\": { \"href\": \"" + baseUrl + "/service/"
                + uuid + "\" }," + "  \"service\": { \"href\": \"" + baseUrl + "/service/" + uuid + "\" }" + "}" + "}";
    }

    private String vnfResourceJson(String uuid, String name) {
        return "{" + "\"modelName\": \"" + name + "\"," + "\"modelUUID\": \"" + uuid + "\","
                + "\"modelVersion\": \"1.0\"," + "\"_links\": {" + "  \"self\": { \"href\": \"" + baseUrl
                + "/vnfResource/" + uuid + "\" }," + "  \"vnfResource\": { \"href\": \"" + baseUrl + "/vnfResource/"
                + uuid + "\" }" + "}" + "}";
    }

    private String cloudSiteJson(String id) {
        return "{" + "\"id\": \"" + id + "\"," + "\"regionId\": \"region-1\"," + "\"cloudVersion\": \"2.5\","
                + "\"_links\": {" + "  \"self\": { \"href\": \"" + baseUrl + "/cloudSite/" + id + "\" },"
                + "  \"cloudSite\": { \"href\": \"" + baseUrl + "/cloudSite/" + id + "\" }" + "}" + "}";
    }

    private String serviceRecipeJson() {
        return "{" + "\"id\": 1," + "\"action\": \"createInstance\","
                + "\"orchestrationUri\": \"/mso/async/services/CreateGenericALaCarteServiceInstance\","
                + "\"_links\": {" + "  \"self\": { \"href\": \"" + baseUrl + "/serviceRecipe/1\" },"
                + "  \"serviceRecipe\": { \"href\": \"" + baseUrl + "/serviceRecipe/1\" }" + "}" + "}";
    }

    private String orchestrationFlowJson(String id, String flowName) {
        return "{" + "\"id\": " + id + "," + "\"flowName\": \"" + flowName + "\"," + "\"flowVersion\": 1.0,"
                + "\"_links\": {" + "  \"self\": { \"href\": \"" + baseUrl + "/orchestrationFlow/" + id + "\" },"
                + "  \"orchestrationFlow\": { \"href\": \"" + baseUrl + "/orchestrationFlow/" + id + "\" }" + "}" + "}";
    }

    private String workflowJson(String id, String name) {
        return "{" + "\"artifactUUID\": \"" + id + "\"," + "\"name\": \"" + name + "\"," + "\"version\": 1.0,"
                + "\"_links\": {" + "  \"self\": { \"href\": \"" + baseUrl + "/workflow/" + id + "\" },"
                + "  \"workflow\": { \"href\": \"" + baseUrl + "/workflow/" + id + "\" }" + "}" + "}";
    }
}
