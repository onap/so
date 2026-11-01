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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.beans.nsmf.EsrInfo;
import org.onap.so.beans.nsmf.JobStatusResponse;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.beans.nsmf.ServiceInfo;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.core.WorkflowException;

public class DoAllocateNssiProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoAllocateNSSI";
    private static final String ALLOCATION = "/api/rest/provMns/v1/NSS/SliceProfiles";
    private static final String JOB = "/api/rest/provMns/v1/NSS/jobs/job-1";
    private static final String TASK = "/orchestrationTask/task-1";
    private final List<String> instances = new ArrayList<>();

    @Before
    public void configureProductionProcessesAndExternalBoundaries() throws Exception {
        assertProductionResource(PROCESS);
        assertProductionResource("HandleOrchestrationTask");
        Map<String, Object> properties = new HashMap<>();
        properties.put("so.auth.username", "test");
        properties.put("so.auth.password", "test");
        properties.put("mso.adapters.nssmf.endpoint", "http://localhost:" + wireMockPort);
        properties.put("mso.adapters.requestDb.endpoint", "http://localhost:" + wireMockPort);
        setWorkflowProperties(properties);
        wireMockServer.resetAll();
        allocation(202, "{\"jobId\":\"job-1\",\"nssiId\":\"nssi-1\",\"status\":\"accepted\"}");
        job("FINISHED", "complete Gr\u00fc\u00dfe & \u96ea");
        task(200);
    }

    @After
    public void removeOwnedRuntimeInstances() {
        for (String id : instances) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "Test cleanup");
            }
        }
    }

    @Test
    public void eachSupportedDomainCompletesThroughTheRealOrchestrationSubprocess() {
        for (SubnetType domain : new SubnetType[] {SubnetType.CN, SubnetType.AN, SubnetType.TN_BH, SubnetType.TN_FH,
                SubnetType.TN_MH}) {
            wireMockServer.resetRequests();
            ProcessInstance process = start(inputs(domain, false));
            assertCompleted(process);
            assertRequests("allocate", domain);
            JSONObject parameters = taskParameters();
            assertEquals("100", parameters.getJSONObject(slot(domain)).get("progress"));
            assertEquals("finished", parameters.getJSONObject(slot(domain)).get("status"));
            assertEquals("complete Gr\u00fc\u00dfe & \u96ea",
                    parameters.getJSONObject(slot(domain)).get("statusDescription"));
            assertFalse(parameters.getJSONObject("serviceProfile").getBoolean("shared"));
            assertEquals(Integer.valueOf(0), parameters.getJSONObject("serviceProfile").get("count"));
            assertTrue(parameters.getJSONObject("serviceProfile").has("optional"));
            assertTrue(parameters.getJSONObject("serviceProfile").isNull("optional"));
            assertTypedResult(process, domain);
        }
    }

    @Test
    public void existingSlicesKeepTheirIdentityAndUseTheModificationActionAcrossTheCallMappings() {
        ProcessInstance process = start(inputs(SubnetType.TN_BH, true));
        assertCompleted(process);
        assertRequests("modify", SubnetType.TN_BH);
        JSONObject allocation = body(ALLOCATION);
        assertEquals("nssi-1", allocation.getJSONObject("serviceInfo").get("nssiId"));
        assertEquals("nssi-1", body(JOB).getJSONObject("serviceInfo").get("nssiId"));
        assertTypedResult(process, SubnetType.TN_BH);
    }

    @Test
    public void pollingPublishesTheNssiIdentityWhenAllocationInitiallyReturnsOnlyAJob() {
        allocation(202, "{\"jobId\":\"job-1\",\"status\":\"accepted\"}");
        ProcessInstance process = start(inputs(SubnetType.CN, false));
        assertCompleted(process);
        assertRequests("allocate", SubnetType.CN, false);
        assertTypedResult(process, SubnetType.CN);
    }

    @Test
    public void aTerminalAdapterFailureRetainsItsFailureDetailsInThePublishedProgress() {
        job("FAILED", "NSSMF rejected the slice Gr\u00fc\u00dfe");
        ProcessInstance process = start(inputs(SubnetType.CN, false));
        assertCompleted(process);
        assertRequests("allocate", SubnetType.CN);
        JSONObject info = taskParameters().getJSONObject("cnSliceTaskInfo");
        assertEquals("failed", info.get("status"));
        assertEquals("NSSMF rejected the slice Gr\u00fc\u00dfe", info.get("statusDescription"));
        JobStatusResponse result = (JobStatusResponse) historic(process.getId(), "nssiAllocateStatus");
        assertEquals("FAILED", result.getResponseDescriptor().getStatus());
        assertEquals("adapter-detail", result.getResponseDescriptor().getErrorCode());
        assertEquals(Boolean.TRUE, historic(process.getId(), "jobFinished"));
    }

    @Test
    public void rejectedAllocationCannotQueryAJobOrPublishSuccessfulProgress() {
        allocation(503, "{\"jobId\":\"must-not-use\",\"nssiId\":\"must-not-use\"}");
        assertAllocationStopsBeforePolling();
    }

    @Test
    public void malformedAllocationCannotQueryAJobOrPublishSuccessfulProgress() {
        allocation(200, "not-json");
        assertAllocationStopsBeforePolling();
    }

    @Test
    public void rejectedOrchestrationWritesStopTheParentAndRetainTheChildBusinessError() {
        Assume.assumeTrue("SO-4457: DoAllocateNSSI completes after its orchestration subprocess rejects the update",
                Boolean.getBoolean("groovy.migration.verification"));
        task(500);
        ProcessInstance process = start(inputs(SubnetType.CN, false));
        assertEnded(process);
        assertRequests("allocate", SubnetType.CN);
        assertActivity(process.getId(), "CallActivity_0knipxf", 1);
        assertActivity(process.getId(), "ScriptTask_1s7bt3s", 0);
        HistoricProcessInstance child = child(process);
        WorkflowException error = (WorkflowException) historic(child.getId(), "WorkflowException");
        assertNotNull(error);
        assertEquals(7000, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains("statusCode: 500"));
        assertActivity(child.getId(), "EndEvent_18t5h42", 0);
        assertActivity(process.getId(), "IntermediateThrowEvent_10xe1qh", 0);
    }

    @Test
    public void accessNetworkFunctionProgressMustReachThePublishedDomainSlot() {
        Assume.assumeTrue("SO-4457: DoAllocateNSSI omits AN_NF progress from the published orchestration parameters",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = start(inputs(SubnetType.AN_NF, false));
        assertCompleted(process);
        assertRequests("allocate", SubnetType.AN_NF);
        assertEquals("100", taskParameters().getJSONObject("anNFSliceTaskInfo").get("progress"));
    }

    private Map<String, Object> inputs(SubnetType domain, boolean modification) {
        ServiceInfo service = new ServiceInfo();
        service.setNsiId("nsi-1");
        service.setNssiName("slice Gr\u00fc\u00dfe & \u96ea");
        if (modification) {
            service.setNssiId("nssi-1");
        }
        EsrInfo esr = new EsrInfo();
        esr.setVendor("vendor-1");
        esr.setNetworkType(domain.getNetworkType());
        NssmfAdapterNBIRequest request = new NssmfAdapterNBIRequest();
        request.setServiceInfo(service);
        request.setEsrInfo(esr);
        SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
        parameters.setServiceId("service-1");
        parameters.getServiceProfile().put("shared", false);
        parameters.getServiceProfile().put("count", 0);
        parameters.getServiceProfile().put("optional", null);
        Map<String, Object> input = new HashMap<>();
        input.put("nbiRequest", request);
        input.put("isNSIOptionAvailable", modification);
        input.put("subnetType", domain);
        input.put("sliceTaskInfo", new SliceTaskInfo<SliceProfileAdapter>());
        input.put("sliceTaskParams", parameters);
        input.put("msoRequestId", "orchestration-request-1");
        input.put("mso-request-id", "wire-request-1");
        input.put("CSSOT_taskId", "task-1");
        input.put("CSSOT_name", "allocate Gr\u00fc\u00dfe & \u96ea");
        input.put("CSSOT_status", "running");
        input.put("CSSOT_isManual", "false");
        return input;
    }

    private ProcessInstance start(Map<String, Object> input) {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), input);
        instances.add(process.getId());
        return process;
    }

    private void assertAllocationStopsBeforePolling() {
        String businessKey = UUID.randomUUID().toString();
        ProcessInstance process = null;
        try {
            process = runtimeService.startProcessInstanceByKey(PROCESS, businessKey, inputs(SubnetType.CN, false));
            instances.add(process.getId());
        } catch (ProcessEngineException rejected) {
            assertEquals(1, wireMockServer.findAll(postRequestedFor(urlEqualTo(ALLOCATION))).size());
        }
        if (process != null) {
            assertActivity(process.getId(), "IntermediateThrowEvent_10xe1qh", 0);
            assertFalse(Boolean.TRUE.equals(historic(process.getId(), "jobFinished")));
        }
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).count());
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(ALLOCATION)));
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(JOB)));
        wireMockServer.verify(0, putRequestedFor(urlEqualTo(TASK)));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    private void assertCompleted(ProcessInstance process) {
        assertEnded(process);
        assertEquals(Boolean.TRUE, historic(process.getId(), "jobFinished"));
        assertEquals(Integer.valueOf(0), historic(process.getId(), "currentCycle"));
        assertEquals(Integer.valueOf(200), historic(process.getId(), "CSSOT_dbResponseCode"));
        assertEquals("{\"updated\":true}", historic(process.getId(), "CSSOT_dbResponse"));
        for (String activity : new String[] {"ScriptTask_0n2z0z7", "ScriptTask_15a7mua", "Task_1ym947s",
                "ScriptTask_0ki1c4k", "CallActivity_0knipxf", "ExclusiveGateway_11gv703",
                "IntermediateThrowEvent_10xe1qh"}) {
            assertActivity(process.getId(), activity, 1);
        }
        assertActivity(process.getId(), "ScriptTask_1s7bt3s", 0);
        HistoricProcessInstance child = child(process);
        for (String activity : new String[] {"ScriptTask_0r0a9ga", "ServiceTask_1iat8g5", "ScriptTask_119zm52",
                "EndEvent_18t5h42"}) {
            assertActivity(child.getId(), activity, 1);
        }
        assertNull(historic(child.getId(), "WorkflowException"));
    }

    private void assertTypedResult(ProcessInstance process, SubnetType domain) {
        assertTrue(historic(process.getId(), "nbiRequest") instanceof NssmfAdapterNBIRequest);
        assertTrue(historic(process.getId(), "sliceTaskParams") instanceof SliceTaskParamsAdapter);
        assertTrue(historic(process.getId(), "sliceTaskInfo") instanceof SliceTaskInfo);
        assertTrue(historic(process.getId(), "serviceInfo") instanceof ServiceInfo);
        EsrInfo esr = (EsrInfo) historic(process.getId(), "esrInfo");
        assertEquals("vendor-1", esr.getVendor());
        assertEquals(domain.getNetworkType(), esr.getNetworkType());
        NssiResponse result = (NssiResponse) historic(process.getId(), "nssiAllocateResult");
        assertEquals("job-1", result.getJobId());
        assertEquals("nssi-1", result.getNssiId());
        JobStatusResponse status = (JobStatusResponse) historic(process.getId(), "nssiAllocateStatus");
        assertEquals(100, status.getResponseDescriptor().getProgress());
    }

    private void assertRequests(String action, SubnetType domain) {
        assertRequests(action, domain, true);
    }

    private void assertRequests(String action, SubnetType domain, boolean identityKnownBeforePolling) {
        wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(ALLOCATION)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0")));
        wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(JOB)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0")));
        wireMockServer.verify(1,
                putRequestedFor(urlEqualTo(TASK)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Content-Type", containing("application/json")));
        assertEquals(3, wireMockServer.getAllServeEvents().size());
        assertEquals(action, body(ALLOCATION).getJSONObject("serviceInfo").get("actionType"));
        assertEquals(domain.getNetworkType().getNetworkType(),
                body(ALLOCATION).getJSONObject("esrInfo").get("networkType"));
        if (identityKnownBeforePolling) {
            assertEquals("nssi-1", body(JOB).getJSONObject("serviceInfo").get("nssiId"));
        } else {
            assertFalse(body(JOB).getJSONObject("serviceInfo").has("nssiId"));
        }
        JSONObject payload =
                new JSONObject(wireMockServer.findAll(putRequestedFor(urlEqualTo(TASK))).get(0).getBodyAsString());
        assertEquals("task-1", payload.get("taskId"));
        assertEquals("orchestration-request-1", payload.get("requestId"));
        assertEquals("allocate Gr\u00fc\u00dfe & \u96ea", payload.get("name"));
        assertEquals("false", payload.get("isManual"));
        assertTrue(payload.get("createdTime") instanceof Number);
    }

    private void assertEnded(ProcessInstance process) {
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(process.getId())
                .finished().count());
    }

    private HistoricProcessInstance child(ProcessInstance process) {
        HistoricProcessInstance child = historyService.createHistoricProcessInstanceQuery()
                .superProcessInstanceId(process.getId()).processDefinitionKey("HandleOrchestrationTask").singleResult();
        assertNotNull(child);
        return child;
    }

    private JSONObject body(String path) {
        return new JSONObject(wireMockServer.findAll(postRequestedFor(urlEqualTo(path))).get(0).getBodyAsString());
    }

    private JSONObject taskParameters() {
        JSONObject payload =
                new JSONObject(wireMockServer.findAll(putRequestedFor(urlEqualTo(TASK))).get(0).getBodyAsString());
        return new JSONObject(payload.getString("params"));
    }

    private String slot(SubnetType type) {
        return Map.of(SubnetType.CN, "cnSliceTaskInfo", SubnetType.AN, "anSliceTaskInfo", SubnetType.TN_BH,
                "tnBHSliceTaskInfo", SubnetType.TN_FH, "tnFHSliceTaskInfo", SubnetType.TN_MH, "tnMHSliceTaskInfo",
                SubnetType.AN_NF, "anNFSliceTaskInfo").get(type);
    }

    private Object historic(String processId, String name) {
        var variable = historyService.createHistoricVariableInstanceQuery().processInstanceId(processId)
                .variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private void assertActivity(String processId, String activity, long expected) {
        assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processId).activityId(activity).finished().count());
    }

    private void allocation(int status, String response) {
        wireMockServer.stubFor(post(urlEqualTo(ALLOCATION)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody(response)));
    }

    private void job(String status, String description) {
        JSONObject descriptor = new JSONObject().put("progress", 100).put("status", status).put("nssiId", "nssi-1")
                .put("statusDescription", description)
                .put("errorCode", "FAILED".equals(status) ? "adapter-detail" : "0");
        wireMockServer.stubFor(post(urlEqualTo(JOB)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close")
                .withBody(new JSONObject().put("responseDescriptor", descriptor).toString())));
    }

    private void task(int status) {
        wireMockServer.stubFor(put(urlEqualTo(TASK)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close")
                .withBody(status == 200 ? "{\"updated\":true}" : "rejected orchestration task")));
    }

    private void assertProductionResource(String process) throws Exception {
        String path = "subprocess/" + process + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(process)
                .latestVersion().singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }
}
