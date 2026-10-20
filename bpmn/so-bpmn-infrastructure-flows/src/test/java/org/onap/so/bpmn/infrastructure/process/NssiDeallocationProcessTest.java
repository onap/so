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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.bpmn.core.WorkflowException;

public class NssiDeallocationProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoDeallocateNSSIV1";
    private static final String ADAPTER_PATH = "/api/rest/provMns/v1/NSS/SliceProfiles/profile-1";
    private static final String JOB_PATH = "/api/rest/provMns/v1/NSS/jobs/job-1";
    private static final String STATUS_PATH = "/nssi-status";
    private static final String SERVICE_PATH =
            "/aai/v[0-9]+/business/customers/customer/customer-1/service-subscriptions/"
                    + "service-subscription/5G/service-instances/service-instance/profile-1";
    private static final String PROFILE_PATH = SERVICE_PATH + "/slice-profiles/slice-profile/slice-1";
    private final List<String> instances = new ArrayList<>();

    @After
    public void removeOwnedInstances() {
        for (String instance : instances) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(instance).count() != 0) {
                runtimeService.deleteProcessInstance(instance, "Test cleanup");
            }
        }
    }

    @Before
    public void prepareContracts() throws Exception {
        assertProductionResource(PROCESS, "subprocess/DoDeallocateNSSI.bpmn");
        assertProductionResource("QueryJobStatus", "subprocess/QueryJobStatus.bpmn");
        assertProductionResource("DecomposeService", "subprocess/BuildingBlock/DecomposeService.bpmn");
        wireMockServer.resetAll();
        variables.clear();
        Map<String, Object> properties = new HashMap<>();
        properties.put("mso.adapters.nssmf.endpoint", "http://localhost:" + wireMockPort);
        properties.put("mso.adapters.db.auth", "test:test");
        setWorkflowProperties(properties);
        Map<String, Object> nssi = new HashMap<>();
        nssi.put("modelInvariantId", "invariant-1");
        nssi.put("modelVersionId", "model-2");
        nssi.put("nssiServiceInstanceId", "nssi-1");
        nssi.put("nsiServiceInstanceId", "nsi-1");
        nssi.put("snssai", "001-000001");
        nssi.put("profileId", "profile-1");
        nssi.put("globalSubscriberId", "customer-1");
        nssi.put("serviceType", "5G");
        nssi.put("jobProgress", Integer.valueOf(0));
        nssi.put("proportion", Integer.valueOf(30));
        nssi.put("e2eServiceInstanceId", "service-1");
        nssi.put("operationId", "operation-1");
        variables.put("currentNSSI", nssi);
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceId", "nssi-1");
        variables.put("scriptName", "deallocation-script");
        variables.put("mso.adapters.nssmf.endpoint", "http://localhost:" + wireMockPort);
        variables.put("mso.adapters.db.auth", "test:test");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS_PATH);
        JSONObject service = new JSONObject().put("modelInfo", new JSONObject().put("modelUuid", "model-2"))
                .put("serviceRole", "vendor-1").put("serviceCategory", "CN").put("serviceVnfs", new JSONArray())
                .put("serviceNetworks", new JSONArray()).put("serviceAllottedResources", new JSONArray());
        wireMockServer.stubFor(
                get(urlPathEqualTo("/v2/serviceResources")).withQueryParam("serviceModelUuid", equalTo("model-2"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBody(new JSONObject().put("serviceResources", service).toString())));
        adapterResponse(202);
        jobResponse("finished", 100);
        wireMockServer.stubFor(
                post(urlEqualTo(STATUS_PATH)).willReturn(aResponse().withStatus(200).withHeader("Connection", "close")
                        .withHeader("Content-Type", "application/soap+xml").withBody("<accepted/>")));
        inventory(SERVICE_PATH + "/slice-profiles", new JSONObject()
                .put("slice-profile", new JSONArray().put(new JSONObject().put("profile-id", "slice-1"))).toString());
        inventory(PROFILE_PATH,
                new JSONObject().put("profile-id", "slice-1").put("resource-version", "profile-version").toString());
        inventory(SERVICE_PATH, new JSONObject().put("service-instance-id", "profile-1")
                .put("resource-version", "service-version").toString());
        wireMockServer.stubFor(delete(urlPathMatching(PROFILE_PATH)).willReturn(aResponse().withStatus(204)));
        wireMockServer.stubFor(delete(urlPathMatching(SERVICE_PATH)).willReturn(aResponse().withStatus(204)));
    }

    @Test
    public void completedJobDeletesOnlyItsProfileAndProfileServiceAfterBothRealCallActivities() {
        ProcessInstance process = start();
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Boolean.TRUE, historic(process, "isNSSIDeAllocated"));
        assertEquals(Boolean.TRUE, historic(process, "isNeedUpdateDB"));
        assertTrue(historic(process, "currentNSSI") instanceof Map);
        Map<?, ?> state = (Map<?, ?>) historic(process, "currentNSSI");
        assertEquals(NetworkType.CORE, state.get("domainType"));
        assertEquals("vendor-1", state.get("vendor"));
        assertEquals("job-1", state.get("jobId"));
        assertEquals(Integer.valueOf(100), state.get("jobProgress"));
        assertChild(process, "DecomposeService");
        assertChild(process, "QueryJobStatus");
        for (String activity : new String[] {"Task_1giechg", "Task_15ut397", "Task_0vi4ijv", "Task_1x3et9h",
                "Task_1v3sdzq", "Task_0amt4hu", "Task_13vaezk", "Task_0ocemjg", "EndEvent_1f579t4"}) {
            assertActivity(process, activity, 1);
        }
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(ADAPTER_PATH)));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(JOB_PATH)));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(PROFILE_PATH)).withQueryParam("resource-version",
                equalTo("profile-version")));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(SERVICE_PATH)).withQueryParam("resource-version",
                equalTo("service-version")));
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(".*/service-instance/nssi-1")));
    }

    @Test
    public void adapterRejectionStopsBeforePollingStatusUpdatesOrInventoryDeletion() {
        adapterResponse(500);
        ProcessInstance process = start();
        assertError(process, 7000);
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(ADAPTER_PATH)));
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(JOB_PATH)));
        assertNoStatusOrDeletion(process);
    }

    @Test
    public void failedPollingStopsBeforeStatusUpdatesAndInventoryDeletion() {
        jobResponse("failed", 0);
        ProcessInstance process = start();
        assertError(process, 7000);
        assertChild(process, "QueryJobStatus");
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(JOB_PATH)));
        assertNoStatusOrDeletion(process);
    }

    @Test
    public void rejectedProfileDeletionStopsBeforeDeletingItsService() {
        wireMockServer.stubFor(delete(urlPathMatching(PROFILE_PATH)).willReturn(aResponse().withStatus(503)));
        ProcessInstance process = start();
        assertError(process, 7000);
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(PROFILE_PATH)));
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(SERVICE_PATH)));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)));
        assertActivity(process, "Task_0ocemjg", 0);
        assertActivity(process, "EndEvent_1f579t4", 0);
    }

    @Test
    public void rejectedServiceDeletionDoesNotRepeatTheAlreadyDeletedProfile() {
        wireMockServer.stubFor(delete(urlPathMatching(SERVICE_PATH)).willReturn(aResponse().withStatus(503)));
        ProcessInstance process = start();
        assertError(process, 7000);
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(PROFILE_PATH)));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(SERVICE_PATH)));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)));
        assertActivity(process, "EndEvent_1f579t4", 0);
    }

    @Test
    public void missingCurrentNssiStopsBeforeAnyExternalRequest() {
        variables.remove("currentNSSI");
        ProcessInstance process = start();
        assertError(process, 500);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        instances.add(process.getId());
        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void adapterResponse(int status) {
        wireMockServer.stubFor(post(urlEqualTo(ADAPTER_PATH))
                .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "application/json")
                        .withBody("{\"jobId\":\"job-1\",\"nssiId\":\"nssi-1\"}")));
    }

    private void jobResponse(String status, int progress) {
        JSONObject descriptor = new JSONObject().put("status", status).put("statusDescription", "deallocation result")
                .put("progress", progress);
        wireMockServer.stubFor(post(urlEqualTo(JOB_PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(new JSONObject().put("responseDescriptor", descriptor).toString())));
    }

    private void inventory(String path, String response) {
        wireMockServer.stubFor(get(urlPathMatching(path)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
    }

    private void assertNoStatusOrDeletion(ProcessInstance process) {
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(STATUS_PATH)));
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(SERVICE_PATH)));
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(PROFILE_PATH)));
        assertActivity(process, "Task_13vaezk", 0);
        assertActivity(process, "Task_0ocemjg", 0);
        assertActivity(process, "EndEvent_1f579t4", 0);
    }

    private void assertError(ProcessInstance process, int code) {
        assertTrue(historic(process, "WorkflowException") instanceof WorkflowException);
        assertEquals(code, ((WorkflowException) historic(process, "WorkflowException")).getErrorCode());
    }

    private void assertChild(ProcessInstance process, String key) {
        assertEquals(key, 1, historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(process.getId())
                .processDefinitionKey(key).finished().count());
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private void assertProductionResource(String process, String path) throws Exception {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(process)
                .latestVersion().singleResult();
        assertNotNull("Missing production process " + process, definition);
        try (InputStream original = getClass().getClassLoader().getResourceAsStream(path);
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertNotNull("Missing production resource " + path, original);
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }
}
