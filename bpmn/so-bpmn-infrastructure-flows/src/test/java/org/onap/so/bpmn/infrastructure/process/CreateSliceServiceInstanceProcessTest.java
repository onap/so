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
package org.onap.so.bpmn.infrastructure.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;

public class CreateSliceServiceInstanceProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoCreateSliceServiceInstance";
    private static final String INVARIANT = "11111111-1111-4111-8111-111111111111";
    private static final String VERSION = "22222222-2222-4222-8222-222222222222";
    private static final String ROOT = "/aai/v[0-9]+/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription-1/service-instances/service-instance/instance-1";
    private static final String PROFILES = ROOT + "/service-profiles/service-profile/[^/]+";
    private static final String ALLOTTED = ROOT + "/allotted-resources/allotted-resource/[^/]+";

    @Before
    public void configureProductionProcessesAndHttpBoundaries() throws Exception {
        production(PROCESS, "subprocess/DoCreateSliceServiceInstance.bpmn", "so-bpmn-infrastructure-flows");
        production("DecomposeService", "subprocess/BuildingBlock/DecomposeService.bpmn", "MSOCommonBPMN");
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("aai.endpoint", "http://localhost:" + wireMockPort);
        configuration.put("aai.username", "contract");
        configuration.put("aai.password", "contract");
        setWorkflowProperties(configuration);
        wireMockServer.resetAll();
        variables.clear();
        variables.put("msoRequestId", UUID.randomUUID().toString());
        variables.put("serviceInstanceId", "instance-1");
        variables.put("serviceInstanceName", "slice Gr\u00fc\u00dfe & \u96ea");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "subscription-1");
        variables.put("uuiRequest",
                new JSONObject()
                        .put("service",
                                new JSONObject().put("serviceInvariantUuid", INVARIANT).put("serviceUuid", VERSION))
                        .toString());
        SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
        Map<String, Object> profile = parameters.getServiceProfile();
        profile.put("sST", "1");
        profile.put("pLMNIdList", "310-410");
        profile.put("sNSSAI", "001-000001");
        for (String key : Arrays.asList("latency", "maxNumberofUEs", "dLThptPerSlice", "dLThptPerUE", "uLThptPerSlice",
                "uLThptPerUE", "activityFactor", "maxNumberofConns", "jitter")) {
            profile.put(key, "0");
        }
        profile.put("coverageAreaTAList", "000001,000002");
        profile.put("uEMobilityLevel", "stationary");
        profile.put("resourceSharingLevel", "shared");
        variables.put("sliceTaskParams", parameters);
        catalog(200, decomposition(false));
        respond(ROOT, 201);
        respond(PROFILES, 201);
        respond(ALLOTTED, 201);
    }

    @Test
    public void emptyResourceDecompositionCreatesTheInstanceAndTypedProfileThroughTheRealChild() {
        ProcessInstance process = start();
        assertNull(value(process, "WorkflowException"));
        assertTrue(value(process, "serviceProfileDecomposition") instanceof ServiceDecomposition);
        ServiceDecomposition result = (ServiceDecomposition) value(process, "serviceProfileDecomposition");
        assertEquals(VERSION, result.getModelInfo().getModelUuid());
        assertTrue(result.getAllottedResources().isEmpty());
        assertEquals(Boolean.TRUE, value(process, "isDebugLogEnabled"));
        assertEquals(1, historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(process.getId())
                .processDefinitionKey("DecomposeService").finished().count());
        for (String id : Arrays.asList("PreprocessIncomingRequest_task", "ScriptTask_1g378se", "CallActivity_0svmkxh",
                "instantiate_NSTask", "ScriptTask_18rzwzb", "Task_09nzhwk", "EndEvent_1x6k78c")) {
            activity(process, id, 1);
        }
        assertWrites(1, 0, 1);
        JSONObject instance = payload(ROOT);
        assertEquals("instance-1", instance.getString("service-instance-id"));
        assertEquals(variables.get("serviceInstanceName"), instance.getString("service-instance-name"));
        assertEquals(INVARIANT, instance.getString("model-invariant-id"));
        assertEquals(VERSION, instance.getString("model-version-id"));
        assertEquals("deactivated", instance.getString("orchestration-status"));
        assertEquals("service-profile", instance.getString("service-role"));
        JSONObject profile = payload(PROFILES);
        assertEquals(Integer.valueOf(0), profile.get("latency"));
        assertEquals(Integer.valueOf(0), profile.get("max-number-of-UEs"));
        UUID.fromString(profile.getString("profile-id"));
        SliceTaskParamsAdapter parameters = (SliceTaskParamsAdapter) value(process, "sliceTaskParams");
        assertEquals(profile.getString("profile-id"), parameters.getServiceProfile().get("profileId"));
        for (String key : Arrays.asList("rollbackData", "RollbackData")) {
            assertTrue(value(process, key) instanceof RollbackData);
            RollbackData rollback = (RollbackData) value(process, key);
            assertEquals("true", rollback.get("SERVICEINSTANCE", "rollbackAAI"));
            assertEquals("instance-1", rollback.get("SERVICEINSTANCE", "serviceInstanceId"));
        }
    }

    @Test
    public void aCatalogAllottedResourceIsCreatedBeforeTheProfileWithItsModelIdentity() {
        catalog(200, decomposition(true));
        ProcessInstance process = start();
        assertNull(value(process, "WorkflowException"));
        assertWrites(1, 1, 1);
        JSONObject allotted = payload(ALLOTTED);
        assertEquals("slice", allotted.getString("type"));
        assertEquals("service-profile", allotted.getString("role"));
        assertEquals(INVARIANT, allotted.getString("model-invariant-id"));
        assertEquals(VERSION, allotted.getString("model-version-id"));
        assertEquals(allotted.getString("id"), value(process, "allottedResourceId"));
        String uri = wireMockServer.findAll(putRequestedFor(urlPathMatching(ALLOTTED))).get(0).getUrl();
        assertTrue(uri, uri.endsWith("/" + allotted.getString("id")));
        var writes = wireMockServer.getAllServeEvents().stream()
                .filter(event -> event.getRequest().getMethod().getName().equals("PUT"))
                .sorted(java.util.Comparator.comparing(event -> event.getRequest().getLoggedDate()))
                .map(event -> event.getRequest().getUrl()).toArray(String[]::new);
        assertTrue(writes[0], writes[0].matches(ROOT));
        assertTrue(writes[1], writes[1].matches(ALLOTTED));
        assertTrue(writes[2], writes[2].matches(PROFILES));
    }

    @Test
    public void rejectedCatalogCannotCreateAnyInventoryResource() {
        Assume.assumeTrue("SO-4457: failed decomposition continues into instance creation with a null model",
                Boolean.getBoolean("groovy.migration.verification"));
        catalog(503, "{}");
        ProcessInstance process = start();
        assertTrue(value(process, "WorkflowException") instanceof WorkflowException);
        assertWrites(0, 0, 0);
        activity(process, "instantiate_NSTask", 0);
        activity(process, "Task_09nzhwk", 0);
    }

    @Test
    public void rejectedInstanceCreationCannotCreateAProfileOrClaimRollbackOwnership() {
        respond(ROOT, 503);
        ProcessInstance process = start();
        assertTrue(value(process, "WorkflowException") instanceof WorkflowException);
        assertEquals(7000, ((WorkflowException) value(process, "WorkflowException")).getErrorCode());
        assertNull(value(process, "RollbackData"));
        assertNull(value(process, "rollbackData"));
        assertWrites(1, 0, 0);
        activity(process, "Task_09nzhwk", 0);
    }

    @Test
    public void rejectedProfileCreationRetainsTheInstanceRollbackContractAndStopsCompletion() {
        respond(PROFILES, 503);
        ProcessInstance process = start();
        assertTrue(value(process, "WorkflowException") instanceof WorkflowException);
        assertEquals(7000, ((WorkflowException) value(process, "WorkflowException")).getErrorCode());
        assertWrites(1, 0, 1);
        RollbackData rollback = (RollbackData) value(process, "RollbackData");
        assertEquals("instance-1", rollback.get("SERVICEINSTANCE", "serviceInstanceId"));
        activity(process, "EndEvent_1x6k78c", 0);
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/v2/serviceResources"))
                .withQueryParam("serviceModelUuid", equalTo(VERSION)));
        return process;
    }

    private String decomposition(boolean allotted) {
        JSONArray resources = new JSONArray();
        if (allotted) {
            resources.put(new JSONObject().put("allottedResourceType", "slice")
                    .put("allottedResourceRole", "service-profile")
                    .put("modelInfo", new JSONObject().put("modelUuid", VERSION).put("modelInvariantUuid", INVARIANT)));
        }
        JSONObject service = new JSONObject()
                .put("modelInfo", new JSONObject().put("modelUuid", VERSION).put("modelInvariantUuid", INVARIANT))
                .put("serviceVnfs", new JSONArray()).put("serviceNetworks", new JSONArray())
                .put("serviceAllottedResources", resources);
        return new JSONObject().put("serviceResources", service).toString();
    }

    private void catalog(int status, String body) {
        wireMockServer.stubFor(get(urlPathEqualTo("/v2/serviceResources"))
                .withQueryParam("serviceModelUuid", equalTo(VERSION)).willReturn(
                        aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private void respond(String path, int status) {
        wireMockServer.stubFor(put(urlPathMatching(path)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody("{}")));
    }

    private void assertWrites(int instances, int allotted, int profiles) {
        for (Object[] expected : new Object[][] {{ROOT, instances}, {ALLOTTED, allotted}, {PROFILES, profiles}}) {
            wireMockServer.verify((int) expected[1],
                    putRequestedFor(urlPathMatching((String) expected[0]))
                            .withHeader("Authorization", equalTo("Basic Y29udHJhY3Q6Y29udHJhY3Q="))
                            .withHeader("Content-Type", containing("application/json")));
        }
        assertEquals(1 + instances + allotted + profiles, wireMockServer.getAllServeEvents().size());
    }

    private JSONObject payload(String path) {
        return new JSONObject(wireMockServer.findAll(putRequestedFor(urlPathMatching(path))).get(0).getBodyAsString());
    }

    private Object value(ProcessInstance process, String name) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private void activity(ProcessInstance process, String id, long count) {
        assertEquals(id, count, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId(id).finished().count());
    }

    private void production(String key, String name, String module) throws Exception {
        URL resource = getClass().getClassLoader().getResource(name);
        assertNotNull(name, resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/" + module + "/target/classes/")
                        || (location.matches("jar:.*/" + module + "-[^/]+\\.jar!/" + name)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult();
        assertNotNull(key, definition);
        try (InputStream source = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals("The engine must execute production BPMN", source.readAllBytes(),
                    deployed.readAllBytes());
        }
    }
}
