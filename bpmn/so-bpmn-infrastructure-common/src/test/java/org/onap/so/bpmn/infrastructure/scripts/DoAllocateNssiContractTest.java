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
package org.onap.so.bpmn.infrastructure.scripts;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.beans.nsmf.EsrInfo;
import org.onap.so.beans.nsmf.JobStatusResponse;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest;
import org.onap.so.beans.nsmf.ResponseDescriptor;
import org.onap.so.beans.nsmf.ServiceInfo;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;

public class DoAllocateNssiContractTest {
    private static final String ALLOCATION = "/api/rest/provMns/v1/NSS/SliceProfiles";
    private static final String JOB = "/api/rest/provMns/v1/NSS/jobs/job-1";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Rule
    public WireMockRule adapter = new WireMockRule(0);
    private final DoAllocateNSSI script = new DoAllocateNSSI();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final NssmfAdapterNBIRequest request = new NssmfAdapterNBIRequest();

    @Before
    public void configureRequestAndExecution() {
        ServiceInfo service = new ServiceInfo();
        service.setNsiId("nsi-1");
        service.setGlobalSubscriberId("subscriber-1");
        service.setNssiName("slice Gr\u00fc\u00dfe & \u96ea \\\"quoted\\\"");
        EsrInfo esr = new EsrInfo();
        esr.setVendor("vendor-1");
        esr.setNetworkType(NetworkType.CORE);
        request.setServiceInfo(service);
        request.setEsrInfo(esr);
        variables.put("nbiRequest", request);
        variables.put("mso.adapters.nssmf.endpoint", "http://localhost:" + adapter.port());
        variables.put("so.auth.username", "contract");
        variables.put("so.auth.password", "contract");
        variables.put("isNSIOptionAvailable", Boolean.FALSE);
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void aFreshNsiAllocatesEvenWhenTheInputCarriesAnExistingNssiId() {
        request.getServiceInfo().setNssiId("existing-1");
        variables.put("currentCycle", 9);
        assertPreprocessing("allocate");
    }

    @Test
    public void anAvailableNsiWithoutAnNssiIdentityStillAllocates() {
        variables.put("isNSIOptionAvailable", Boolean.TRUE);
        for (String identity : new String[] {null, "", "  "}) {
            request.getServiceInfo().setNssiId(identity);
            variables.put("nbiRequest", request);
            assertPreprocessing("allocate");
        }
    }

    @Test
    public void anAvailableNsiWithAnNssiIdentitySelectsModification() {
        variables.put("isNSIOptionAvailable", Boolean.TRUE);
        request.getServiceInfo().setNssiId("existing-1");
        assertPreprocessing("modify");
    }

    @Test
    public void acceptedAllocationPostsTheTypedRequestAndRetainsTheAdapterIdentities() throws Exception {
        for (int status : new int[] {200, 201, 202}) {
            adapter.resetAll();
            respond(ALLOCATION, status, "{\"jobId\":\"job-1\",\"nssiId\":\"nssi-1\",\"status\":\"accepted\"}");
            script.preProcessRequest(execution);
            JsonNode expected = MAPPER.valueToTree(variables.get("nbiRequest"));
            script.sendCreateRequestNSSMF(execution);
            NssiResponse result = (NssiResponse) variables.get("nssiAllocateResult");
            assertEquals("job-1", result.getJobId());
            assertEquals("nssi-1", result.getNssiId());
            assertEquals("accepted", result.getStatus());
            assertAdapterContext(expected);
            assertPost(ALLOCATION);
            JsonNode sent = body(ALLOCATION);
            assertEquals(expected, sent);
            assertEquals("allocate", sent.path("serviceInfo").path("actionType").asText());
            assertEquals(request.getServiceInfo().getNssiName(), sent.path("serviceInfo").path("nssiName").asText());
            assertEquals("nsi-1", sent.path("serviceInfo").path("nsiId").asText());
            assertEquals("cn", sent.path("esrInfo").path("networkType").asText());
            assertEquals("vendor-1", sent.path("esrInfo").path("vendor").asText());
        }
    }

    @Test
    public void rejectedAllocationDoesNotPublishAResultFromTheErrorBody() {
        for (int status : new int[] {400, 404, 500, 503}) {
            adapter.resetAll();
            variables.remove("nssiAllocateResult");
            respond(ALLOCATION, status, "{\"jobId\":\"error-job\",\"nssiId\":\"must-not-use\"}");
            JsonNode expected = MAPPER.valueToTree(variables.get("nbiRequest"));
            script.sendCreateRequestNSSMF(execution);
            assertFalse(variables.containsKey("nssiAllocateResult"));
            assertAdapterContext(expected);
            assertPost(ALLOCATION);
        }
    }

    @Test
    public void malformedSuccessfulAllocationResponsesAreRejectedBeforePublishingAResult() {
        for (String response : new String[] {"not-json", ""}) {
            adapter.resetAll();
            respond(ALLOCATION, 200, response);
            assertThrows(JsonProcessingException.class, () -> script.sendCreateRequestNSSMF(execution));
            assertFalse(variables.containsKey("nssiAllocateResult"));
            assertPost(ALLOCATION);
        }
    }

    @Test
    public void pollingPostsTheKnownSliceIdentityAndRetainsTheTypedProgress() throws Exception {
        preparePolling("existing-1");
        respond(JOB, 200, progress(25, "queued", "existing-1"));
        script.queryNSSIStatus(execution);
        assertResult("existing-1");
        assertFalse(Boolean.TRUE.equals(variables.get("jobFinished")));
        JobStatusResponse status = (JobStatusResponse) variables.get("nssiAllocateStatus");
        assertEquals(25, status.getResponseDescriptor().getProgress());
        assertEquals("queued", status.getResponseDescriptor().getStatus());
        assertEquals("existing-1", body(JOB).path("serviceInfo").path("nssiId").asText());
        assertEquals("vendor-1", body(JOB).path("esrInfo").path("vendor").asText());
        assertPost(JOB);
    }

    @Test
    public void pollingDiscoversAnInitiallyMissingNssiWithoutCompletingTheJob() {
        preparePolling(null);
        respond(JOB, 200, progress(40, "processing", "discovered-1"));
        script.queryNSSIStatus(execution);
        assertResult("discovered-1");
        assertFalse(Boolean.TRUE.equals(variables.get("jobFinished")));
        assertPost(JOB);
    }

    @Test
    public void completedPollingUsesABooleanFlagAndPreservesAnExistingNssiIdentity() {
        preparePolling("existing-1");
        respond(JOB, 200, progress(100, "finished", "other-1"));
        script.queryNSSIStatus(execution);
        assertEquals(Boolean.TRUE, variables.get("jobFinished"));
        assertEquals(Boolean.class, variables.get("jobFinished").getClass());
        assertResult("existing-1");
        assertEquals("existing-1", ((ServiceInfo) variables.get("serviceInfo")).getNssiId());
        assertPost(JOB);
    }

    @Test
    public void completedPollingPublishesADiscoveredIdentityForNullOrBlankInitialIds() {
        for (String identity : new String[] {null, "", "  "}) {
            adapter.resetAll();
            variables.remove("jobFinished");
            variables.remove("nssiAllocateStatus");
            preparePolling(identity);
            respond(JOB, 200, progress(100, "finished", "discovered-1"));
            script.queryNSSIStatus(execution);
            assertResult("discovered-1");
            assertEquals(Boolean.TRUE, variables.get("jobFinished"));
            assertEquals(Boolean.class, variables.get("jobFinished").getClass());
            assertPost(JOB);
        }
    }

    @Test
    public void rejectedPollingCannotPublishCompletionFromAnErrorResponse() {
        preparePolling("existing-1");
        for (int status : new int[] {400, 500, 503}) {
            adapter.resetAll();
            respond(JOB, status, progress(100, "finished", "must-not-use"));
            script.queryNSSIStatus(execution);
            assertFalse(variables.containsKey("nssiAllocateStatus"));
            assertFalse(Boolean.TRUE.equals(variables.get("jobFinished")));
            assertResult("existing-1");
            assertPost(JOB);
        }
    }

    @Test
    public void malformedSuccessfulPollingCannotPublishAJobResult() {
        preparePolling("existing-1");
        for (String response : new String[] {"not-json", ""}) {
            adapter.resetAll();
            respond(JOB, 200, response);
            assertThrows(JsonProcessingException.class, () -> script.queryNSSIStatus(execution));
            assertFalse(variables.containsKey("nssiAllocateStatus"));
            assertFalse(Boolean.TRUE.equals(variables.get("jobFinished")));
            assertPost(JOB);
        }
    }

    @Test
    public void orchestrationUpdatesSelectOnlyTheRequestedCoreAccessOrTransportDomain() throws Exception {
        for (SubnetType type : new SubnetType[] {SubnetType.CN, SubnetType.AN, SubnetType.TN_BH, SubnetType.TN_FH,
                SubnetType.TN_MH}) {
            SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
            parameters.setServiceName("Gr\u00fc\u00dfe & \u96ea");
            parameters.getServiceProfile().put("shared", false);
            parameters.getServiceProfile().put("count", 0);
            Map<SubnetType, JsonNode> original = new HashMap<>();
            domains(parameters).forEach((domain, info) -> {
                info.setProgress("7");
                info.setStatus("previous-" + domain.name());
                info.setSliceInstanceId("preserved-" + domain.name());
                original.put(domain, MAPPER.valueToTree(info));
            });
            orchestration(parameters, type);
            ObjectNode expectedTask = MAPPER.valueToTree(variables.get("sliceTaskInfo"));
            expectedTask.put("progress", "25");
            expectedTask.put("status", "queued");
            expectedTask.put("statusDescription", "Gr\u00fc\u00dfe & \u96ea");
            script.prepareUpdateOrchestrationTask(execution);
            assertTrue(variables.get("sliceTaskParams") instanceof SliceTaskParamsAdapter);
            assertTrue(variables.get("sliceTaskInfo") instanceof SliceTaskInfo);
            SliceTaskParamsAdapter stored = (SliceTaskParamsAdapter) variables.get("sliceTaskParams");
            assertEquals(expectedTask, MAPPER.valueToTree(variables.get("sliceTaskInfo")));
            assertEquals(expectedTask, MAPPER.valueToTree(domains(stored).get(type)));
            for (SubnetType other : original.keySet()) {
                if (other != type) {
                    assertEquals(other.name(), original.get(other), MAPPER.valueToTree(domains(stored).get(other)));
                }
            }
            assertEquals("PUT", variables.get("CSSOT_requestMethod"));
            JsonNode payload = MAPPER.readTree((String) variables.get("CSSOT_paramJson"));
            assertEquals(MAPPER.valueToTree(stored), payload);
            assertEquals("Gr\u00fc\u00dfe & \u96ea", payload.path("serviceName").asText());
            assertTrue(payload.path("serviceProfile").path("shared").isBoolean());
            assertFalse(payload.path("serviceProfile").path("shared").asBoolean());
            assertTrue(payload.path("serviceProfile").path("count").isIntegralNumber());
            assertEquals(0, payload.path("serviceProfile").path("count").asInt());
        }
    }

    @Test
    public void accessNetworkFunctionProgressReachesItsOwnOrchestrationSlot() {
        Assume.assumeTrue("SO-4457: DoAllocateNSSI does not update the AN_NF orchestration slot",
                Boolean.getBoolean("groovy.migration.verification"));
        SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
        orchestration(parameters, SubnetType.AN_NF);
        script.prepareUpdateOrchestrationTask(execution);
        SliceTaskParamsAdapter stored = (SliceTaskParamsAdapter) variables.get("sliceTaskParams");
        assertEquals("25", stored.getAnNFSliceTaskInfo().getProgress());
        assertEquals("queued", stored.getAnNFSliceTaskInfo().getStatus());
    }

    @Test
    public void anInterruptedDelayDoesNotConsumeAnotherPollingAttempt() {
        variables.put("currentCycle", 7);
        boolean interrupted = Thread.currentThread().isInterrupted();
        try {
            // The pending interrupt makes the production delay throw before it waits.
            Thread.currentThread().interrupt();
            script.timeDelay(execution);
            assertEquals(Integer.valueOf(7), variables.get("currentCycle"));
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            } else {
                Thread.interrupted();
            }
        }
    }

    private void assertPreprocessing(String action) {
        ObjectNode expected = MAPPER.valueToTree(variables.get("nbiRequest"));
        ((ObjectNode) expected.get("serviceInfo")).put("actionType", action);
        script.preProcessRequest(execution);
        assertTrue(variables.get("nbiRequest") instanceof NssmfAdapterNBIRequest);
        assertEquals(expected, MAPPER.valueToTree(variables.get("nbiRequest")));
        assertEquals(Integer.valueOf(0), variables.get("currentCycle"));
        assertEquals(Integer.class, variables.get("currentCycle").getClass());
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    private void assertAdapterContext(JsonNode expected) {
        assertEquals(expected, MAPPER.valueToTree(variables.get("nbiRequest")));
        assertTrue(variables.get("serviceInfo") instanceof ServiceInfo);
        assertTrue(variables.get("esrInfo") instanceof EsrInfo);
        assertEquals(expected.get("serviceInfo"), MAPPER.valueToTree(variables.get("serviceInfo")));
        assertEquals(expected.get("esrInfo"), MAPPER.valueToTree(variables.get("esrInfo")));
    }

    private void assertResult(String identity) {
        assertTrue(variables.get("nssiAllocateResult") instanceof NssiResponse);
        NssiResponse result = (NssiResponse) variables.get("nssiAllocateResult");
        assertEquals("job-1", result.getJobId());
        assertEquals("accepted", result.getStatus());
        assertEquals(identity, result.getNssiId());
    }

    private void preparePolling(String identity) {
        NssiResponse result = new NssiResponse();
        result.setJobId("job-1");
        result.setNssiId(identity);
        result.setStatus("accepted");
        variables.put("nssiAllocateResult", result);
        variables.put("serviceInfo", request.getServiceInfo());
        variables.put("esrInfo", request.getEsrInfo());
    }

    private SliceTaskInfo<SliceProfileAdapter> orchestration(SliceTaskParamsAdapter parameters, SubnetType type) {
        ResponseDescriptor descriptor = new ResponseDescriptor();
        descriptor.setProgress(25);
        descriptor.setStatus("QUEUED");
        descriptor.setStatusDescription("Gr\u00fc\u00dfe & \u96ea");
        JobStatusResponse status = new JobStatusResponse();
        status.setResponseDescriptor(descriptor);
        SliceTaskInfo<SliceProfileAdapter> task = new SliceTaskInfo<>();
        task.setSliceInstanceId("slice-1");
        variables.put("sliceTaskParams", parameters);
        variables.put("sliceTaskInfo", task);
        variables.put("nssiAllocateStatus", status);
        variables.put("subnetType", type);
        return task;
    }

    private Map<SubnetType, SliceTaskInfo<?>> domains(SliceTaskParamsAdapter parameters) {
        Map<SubnetType, SliceTaskInfo<?>> slots = new HashMap<>();
        slots.put(SubnetType.CN, parameters.getCnSliceTaskInfo());
        slots.put(SubnetType.AN, parameters.getAnSliceTaskInfo());
        slots.put(SubnetType.TN_BH, parameters.getTnBHSliceTaskInfo());
        slots.put(SubnetType.TN_FH, parameters.getTnFHSliceTaskInfo());
        slots.put(SubnetType.TN_MH, parameters.getTnMHSliceTaskInfo());
        slots.put(SubnetType.AN_NF, parameters.getAnNFSliceTaskInfo());
        return slots;
    }

    private void respond(String path, int status, String response) {
        adapter.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody(response)));
    }

    private String progress(int progress, String status, String identity) {
        return "{\"responseDescriptor\":{\"progress\":" + progress + ",\"status\":\"" + status + "\",\"nssiId\":\""
                + identity + "\",\"statusDescription\":\"working\"}}";
    }

    private void assertPost(String path) {
        adapter.verify(1,
                postRequestedFor(urlEqualTo(path))
                        .withHeader("Authorization", equalTo("Basic Y29udHJhY3Q6Y29udHJhY3Q="))
                        .withHeader("Content-Type", containing("application/json")));
        assertEquals(1, adapter.getAllServeEvents().size());
    }

    private JsonNode body(String path) throws Exception {
        return MAPPER.readTree(adapter.findAll(postRequestedFor(urlEqualTo(path))).get(0).getBodyAsString());
    }
}
