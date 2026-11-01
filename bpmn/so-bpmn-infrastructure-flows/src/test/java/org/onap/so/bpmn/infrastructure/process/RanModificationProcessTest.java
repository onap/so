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
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.springframework.beans.factory.annotation.Autowired;

public class RanModificationProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoModifyRanNfNssi";
    private static final String TOPIC = "/events/ran-requests";
    private final List<String> owned = new ArrayList<>();
    @Autowired
    private ManagementService jobs;

    @Before
    public void configureProductionProcessesAndBrokerBoundary() throws Exception {
        production(PROCESS, "so-bpmn-infrastructure-flows");
        production("DoHandleSdnrDmaapRequest", "so-bpmn-infrastructure-flows");
        production("ReceiveWorkflowMessage", "MSOCommonBPMN");
        Map<String, Object> properties = new HashMap<>();
        properties.put("sdnc.dmaap.host", "http://localhost:" + wireMockPort);
        properties.put("sdnc.dmaap.publisher.topic", "ran-requests");
        properties.put("mso.workflow.message.endpoint", "http://localhost:" + wireMockPort + "/callbacks");
        setWorkflowProperties(properties);
        wireMockServer.resetAll();
        broker(202);
        variables.clear();
        variables.put("msoRequestId", UUID.randomUUID().toString());
        variables.put("serviceInstanceID", "ran-nssi-1");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "subscription-1");
        variables.put("sliceProfileId", "profile-1");
        variables.put("snssaiList", Arrays.asList("001-000001", "001-000002"));
        variables.put("modifyAction", "allocate");
        variables.put("additionalProperties", "{\"maxNumberofPDUSession\":0,\"expDataRateUL\":0,"
                + "\"expDataRateDL\":12,\"enabled\":false,\"name\":\"Gr\u00fc\u00dfe & \u96ea\"}");
    }

    @After
    public void removeOwnedInstances() {
        for (String id : owned) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "RAN test cleanup");
            }
        }
    }

    @Test
    public void allThreeActionsUseTheRealPublisherAndCallbackSubprocessWithTypedResults() {
        for (String action : Arrays.asList("allocate", "deallocate", "reconfigure")) {
            wireMockServer.resetRequests();
            variables.put("modifyAction", action);
            variables.put("msoRequestId", UUID.randomUUID().toString());
            ProcessInstance parent = start();
            ProcessInstance callback = waiting(parent);
            String request = (String) variables.get("msoRequestId");
            assertEquals(request, runtimeService.getVariable(callback.getId(), "AsyncSdnrResponse_CORRELATOR"));
            JSONObject outbound = outbound();
            assertEquals(request + "-1", outbound.getString("correlation-id"));
            assertEquals("instantiateRANSlice", outbound.getString("rpc-name"));
            JSONObject input = outbound.getJSONObject("body").getJSONObject("input");
            assertEquals("reconfigure".equals(action) ? action : "modify-" + action, input.getString("action"));
            JSONObject payload = new JSONObject(input.getString("payload")).getJSONObject("input");
            assertEquals("ran-nssi-1", payload.getString("RANNFNSSIId"));
            assertEquals("profile-1", payload.getJSONObject("sliceProfile").getString("sliceProfileId"));
            if ("allocate".equals(action)) {
                assertEquals(Integer.valueOf(0), payload.getJSONObject("sliceProfile").get("maxNumberofConns"));
                assertFalse(payload.getJSONObject("sliceProfile").getBoolean("enabled"));
            }
            correlate(request, success("nf-2", "nf-1"));
            completed(parent, "nf-2", "nf-1");
            onePublish();
            activity(parent, "Activity_03xcmka", 1);
            reject(request);
        }
    }

    @Test
    public void callbacksSelectOnlyTheMatchingWaitAndDuplicateDeliveryCannotRepeatPublishing() {
        ProcessInstance first = start();
        String firstRequest = (String) variables.get("msoRequestId");
        variables.put("msoRequestId", UUID.randomUUID().toString());
        String secondRequest = (String) variables.get("msoRequestId");
        ProcessInstance second = start();
        waiting(first);
        waiting(second);
        reject("unknown-" + firstRequest);
        correlate(secondRequest, success("second-nf"));
        completed(second, "second-nf");
        waiting(first);
        correlate(firstRequest, success("first-nf"));
        completed(first, "first-nf");
        reject(firstRequest);
        reject(secondRequest);
        wireMockServer.verify(2, postRequestedFor(urlPathEqualTo(TOPIC)));
    }

    @Test
    public void failedCallbacksRetainTheBusinessErrorAndNeverPublishSuccessfulIdentifiers() {
        ProcessInstance parent = start();
        waiting(parent);
        correlate((String) variables.get("msoRequestId"), "{\"status\":\"failed\",\"reason\":\"controller rejected\"}");
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertNull(value(parent, "ranNfIdsJson"));
        assertNull(value(parent, "ranNfStatus"));
        assertTrue(value(parent, "WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) value(parent, "WorkflowException");
        assertEquals(7000, error.getErrorCode());
        assertEquals("received failed status from SDNR controller rejected", error.getErrorMessage());
        activity(parent, "Event_086qr2t", 0);
        onePublish();
    }

    @Test
    public void malformedCallbacksRollBackCorrelationAndAllowACorrectedCallbackWithoutRepublishing() {
        ProcessInstance parent = start();
        waiting(parent);
        String request = (String) variables.get("msoRequestId");
        assertThrows(ProcessEngineException.class, () -> correlate(request, "not-json"));
        waiting(parent);
        assertNull(runtimeService.getVariable(parent.getId(), "ranNfIdsJson"));
        correlate(request, success("corrected-nf"));
        completed(parent, "corrected-nf");
        onePublish();
    }

    @Test
    public void missingRequiredSliceInputsStopBeforePublishingOrCreatingCallbackWaits() {
        variables.put("snssaiList", new ArrayList<>());
        ProcessInstance parent = start();
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertEquals(500, ((WorkflowException) value(parent, "WorkflowException")).getErrorCode());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
        activity(parent, "Activity_1rs1l5c", 0);
        assertEquals(0,
                historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(parent.getId()).count());
    }

    @Test
    public void timeoutPreservesTheCallbackBusinessErrorAndDoesNotRepublish() {
        Assume.assumeTrue("SO-4457: the RAN timeout evaluates a null callback instead of retaining its business error",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance parent = start();
        ProcessInstance callback = waiting(parent);
        var timer = jobs.createJobQuery().processInstanceId(callback.getId()).singleResult();
        assertNotNull(timer);
        jobs.executeJob(timer.getId());
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertTrue(value(parent, "WorkflowException") instanceof WorkflowException);
        assertNull(value(parent, "ranNfIdsJson"));
        activity(parent, "Event_086qr2t", 0);
        onePublish();
        reject((String) variables.get("msoRequestId"));
    }

    @Test
    public void rejectedBrokerWritesUseABusinessErrorBeforeCreatingCallbackWaits() {
        Assume.assumeTrue("SO-4457: a rejected RAN broker write escapes as WebApplicationException",
                Boolean.getBoolean("groovy.migration.verification"));
        broker(503);
        ProcessInstance parent = start();
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertTrue(value(parent, "WorkflowException") instanceof WorkflowException);
        assertNull(value(parent, "ranNfIdsJson"));
        activity(parent, "Event_086qr2t", 0);
        onePublish();
    }

    private ProcessInstance start() {
        String key = UUID.randomUUID().toString();
        ProcessInstance parent = runtimeService.startProcessInstanceByKey(PROCESS, key, variables);
        owned.add(parent.getId());
        return parent;
    }

    private ProcessInstance waiting(ProcessInstance parent) {
        assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        ProcessInstance adapter =
                runtimeService.createProcessInstanceQuery().superProcessInstanceId(parent.getId()).singleResult();
        assertNotNull(adapter);
        ProcessInstance callback =
                runtimeService.createProcessInstanceQuery().superProcessInstanceId(adapter.getId()).singleResult();
        assertNotNull(callback);
        assertEquals(1, runtimeService.createEventSubscriptionQuery().processInstanceId(callback.getId())
                .eventName("WorkflowMessage").count());
        assertEquals("PT10M", runtimeService.getVariable(callback.getId(), "RCVWFMSG_timeout"));
        assertEquals(1, jobs.createJobQuery().processInstanceId(callback.getId()).count());
        return callback;
    }

    private void completed(ProcessInstance parent, String... identifiers) {
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertEquals("success", value(parent, "ranNfStatus"));
        assertTrue(value(parent, "ranNfIdsJson") instanceof String);
        assertTrue(new JSONArray(Arrays.asList(identifiers))
                .similar(new JSONArray((String) value(parent, "ranNfIdsJson"))));
        assertNull(value(parent, "WorkflowException"));
        activity(parent, "Event_086qr2t", 1);
    }

    private void correlate(String request, String response) {
        runtimeService.createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("AsyncSdnrResponse_CORRELATOR", request)
                .setVariable("AsyncSdnrResponse_MESSAGE", response).correlate();
    }

    private void reject(String request) {
        assertThrows(MismatchingMessageCorrelationException.class, () -> correlate(request, "unexpected-response"));
    }

    private String success(String... identifiers) {
        return new JSONObject().put("status", "success").put("nfIds", new JSONArray(Arrays.asList(identifiers)))
                .toString();
    }

    private JSONObject outbound() {
        onePublish();
        return new JSONObject(wireMockServer.findAll(postRequestedFor(urlPathEqualTo(TOPIC))).get(0).getBodyAsString());
    }

    private void onePublish() {
        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo(TOPIC)).withQueryParam("timeout", equalTo("55000"))
                .withHeader("Content-Type", containing("application/json")));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    private void broker(int status) {
        wireMockServer.stubFor(post(urlPathEqualTo(TOPIC)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody("{}")));
    }

    private Object value(ProcessInstance process, String name) {
        HistoricVariableInstance value = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private void activity(ProcessInstance parent, String id, long count) {
        assertEquals(id, count, historyService.createHistoricActivityInstanceQuery().processInstanceId(parent.getId())
                .activityId(id).finished().count());
    }

    private void production(String key, String module) throws Exception {
        String path = "subprocess/" + key + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/" + module + "/target/classes/")
                        || (location.matches("jar:.*/" + module + "-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }
}
