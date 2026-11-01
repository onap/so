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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.springframework.beans.factory.annotation.Autowired;

public class OofRequestProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoHandleOofRequest";
    private static final String PATH = "/oof-adapter";
    private String correlator;
    private final List<ProcessInstance> started = new ArrayList<>();

    @Autowired
    private ManagementService managementService;

    @Before
    public void prepareContracts() throws Exception {
        assertProductionResource(PROCESS, "so-bpmn-infrastructure-flows");
        assertProductionResource("ReceiveWorkflowMessage", "MSOCommonBPMN");
        wireMockServer.resetAll();
        variables.clear();
        correlator = "oof-" + UUID.randomUUID();
        variables.put("msoRequestId", "request-1");
        variables.put("apiPath", "/nst/selection");
        variables.put("correlator", correlator);
        variables.put("messageType", "OOF");
        variables.put("timeout", "PT1M");
        variables.put("mso.adapters.oof.endpoint", "http://localhost:" + wireMockPort + PATH);
        Map<String, Object> details = new HashMap<>();
        details.put("capacity", 0);
        details.put("shared", false);
        details.put("name", "Gr\u00fc\u00dfe & \u96ea");
        variables.put("oofRequest", details);
        stubResponse(202);
    }

    @Test
    public void acceptedRequestWaitsForTheMatchingCallbackAndPreservesTypedPayloads() {
        ProcessInstance parent = start();
        ProcessInstance child = assertWaiting(parent);
        JSONObject payload = requestBody();
        assertEquals("/nst/selection", payload.getString("apiPath"));
        assertEquals(2, payload.length());
        JSONObject details = payload.getJSONObject("requestDetails");
        assertEquals(Integer.valueOf(0), details.get("capacity"));
        assertEquals(Boolean.FALSE, details.get("shared"));
        assertEquals("Gr\u00fc\u00dfe & \u96ea", details.getString("name"));
        assertEquals("PT1M", runtimeService.getVariable(child.getId(), "RCVWFMSG_timeout"));
        rejectCallback("wrong-" + correlator);
        assertWaiting(parent);
        String response = "<response>Gr\u00fc\u00dfe &amp; complete</response>";
        correlate(correlator, response);
        assertCompleted(parent, response);
        rejectCallback(correlator);
        assertOneRequest();
    }

    @Test
    public void anEmptyAcceptedHttpBodyStillWaitsForAndReturnsAnEmptyCallback() {
        stubResponse(204);
        ProcessInstance parent = start();
        assertWaiting(parent);
        correlate(correlator, "");
        assertCompleted(parent, "");
        assertOneRequest();
    }

    @Test
    public void outOfOrderCallbacksResumeOnlyTheMatchingRequest() {
        ProcessInstance first = start();
        String secondCorrelator = "second-" + correlator;
        variables.put("correlator", secondCorrelator);
        ProcessInstance second = start();
        assertWaiting(first);
        assertWaiting(second);
        correlate(secondCorrelator, "second-response");
        assertCompleted(second, "second-response");
        assertWaiting(first);
        correlate(correlator, "first-response");
        assertCompleted(first, "first-response");
        wireMockServer.verify(2, postRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    public void theProductionTimerEndsTheWaitAndRejectsALateCallback() {
        long before = System.currentTimeMillis();
        ProcessInstance parent = start();
        ProcessInstance child = assertWaiting(parent);
        Job timer = managementService.createJobQuery().processInstanceId(child.getId()).singleResult();
        long after = System.currentTimeMillis();
        assertNotNull(timer);
        assertTrue(timer.getDuedate().getTime() >= before + 59000);
        assertTrue(timer.getDuedate().getTime() <= after + 61000);
        managementService.executeJob(timer.getId());
        assertFailed(parent);
        assertEquals(0, managementService.createJobQuery().processInstanceId(child.getId()).count());
        assertEquals(0, runtimeService.createEventSubscriptionQuery().processInstanceId(child.getId()).count());
        rejectCallback(correlator);
        assertOneRequest();
    }

    @Test
    public void aRejectedClientRequestStopsBeforeCreatingACallbackWait() {
        stubResponse(400);
        assertFailed(start());
        assertOneRequest();
    }

    @Test
    public void aRejectedServerRequestStopsBeforeCreatingACallbackWait() {
        stubResponse(503);
        assertFailed(start());
        assertOneRequest();
    }

    @Test
    public void missingApiPathStopsBeforeAnyOutboundAction() {
        variables.remove("apiPath");
        assertFailed(start());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void missingCorrelatorStopsBeforeAnyOutboundAction() {
        variables.remove("correlator");
        assertFailed(start());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void missingMessageTypeStopsBeforeAnyOutboundAction() {
        variables.remove("messageType");
        assertFailed(start());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void blankRequiredInputsStopBeforeAnyOutboundAction() {
        Map<String, Object> input = new HashMap<>(variables);
        for (String key : new String[] {"apiPath", "correlator", "messageType"}) {
            variables.clear();
            variables.putAll(input);
            variables.put(key, " \t");
            assertFailed(start());
        }
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void configuredOofTimeoutReachesTheRealCallbackSubprocess() {
        Assume.assumeTrue("SO-4457: the OOF helper does not pass its configured timeout to the callback subprocess",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.remove("timeout");
        variables.put("mso.adapters.oof.timeout", "PT2M");
        ProcessInstance parent = start();
        try {
            ProcessInstance child = assertWaiting(parent);
            assertEquals("PT2M", runtimeService.getVariable(child.getId(), "RCVWFMSG_timeout"));
        } finally {
            deleteIfRunning(parent);
        }
    }

    @Test
    public void theDefaultOofTimeoutReachesTheRealCallbackSubprocess() {
        Assume.assumeTrue("SO-4457: the OOF helper does not pass its default timeout to the callback subprocess",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.remove("timeout");
        ProcessInstance parent = start();
        try {
            ProcessInstance child = assertWaiting(parent);
            assertEquals("PT30M", runtimeService.getVariable(child.getId(), "RCVWFMSG_timeout"));
        } finally {
            deleteIfRunning(parent);
        }
    }

    @Test
    public void theErrorRoutePreservesTheRejectedHttpStatusCode() {
        Assume.assumeTrue("SO-4457: the OOF error handler replaces the rejected HTTP status code",
                Boolean.getBoolean("groovy.migration.verification"));
        stubResponse(503);
        ProcessInstance parent = start();
        assertFailed(parent);
        assertEquals(503, ((WorkflowException) historic(parent, "WorkflowException")).getErrorCode());
    }

    private ProcessInstance start() {
        ProcessInstance parent =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        started.add(parent);
        return parent;
    }

    @After
    public void removeUnfinishedTestProcesses() {
        started.forEach(this::deleteIfRunning);
    }

    private ProcessInstance assertWaiting(ProcessInstance parent) {
        assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        ProcessInstance child =
                runtimeService.createProcessInstanceQuery().superProcessInstanceId(parent.getId()).singleResult();
        assertNotNull(child);
        assertEquals(1, runtimeService.createEventSubscriptionQuery().processInstanceId(child.getId())
                .eventName("WorkflowMessage").count());
        assertEquals(1, managementService.createJobQuery().processInstanceId(child.getId()).count());
        assertNull(runtimeService.getVariable(parent.getId(), "WorkflowException"));
        assertActivity(parent, "Activity_0tki17o", 1);
        assertActivity(parent, "Activity_08ozlfa", 1);
        return child;
    }

    private void assertCompleted(ProcessInstance parent, String response) {
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertEquals(response, historic(parent, "asyncCallbackResponse"));
        assertNull(historic(parent, "WorkflowException"));
        assertActivity(parent, "Activity_1yq993l", 1);
        assertActivity(parent, "Event_179tp7r", 1);
    }

    private void assertFailed(ProcessInstance parent) {
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(parent.getId()).count());
        assertNotNull(historic(parent, "WorkflowException"));
        assertEquals(WorkflowException.class, historic(parent, "WorkflowException").getClass());
        assertActivity(parent, "Activity_11rvko0", 1);
        assertActivity(parent, "Event_179tp7r", 0);
    }

    private void correlate(String request, String response) {
        runtimeService.createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("OOF_CORRELATOR", request).setVariable("OOF_MESSAGE", response)
                .correlate();
    }

    private void rejectCallback(String request) {
        try {
            correlate(request, "unexpected-response");
            fail("Callback must not correlate: " + request);
        } catch (MismatchingMessageCorrelationException expected) {
            assertTrue(expected.getMessage().contains("WorkflowMessage"));
        }
    }

    private void assertOneRequest() {
        wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(PATH)).withHeader("Content-Type", containing("application/json")));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    private JSONObject requestBody() {
        assertOneRequest();
        return new JSONObject(wireMockServer.findAll(postRequestedFor(urlEqualTo(PATH))).get(0).getBodyAsString());
    }

    private void stubResponse(int status) {
        wireMockServer.stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close")));
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long expected) {
        assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private void deleteIfRunning(ProcessInstance process) {
        if (runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count() != 0) {
            runtimeService.deleteProcessInstance(process.getId(), "OOF test cleanup");
        }
    }

    private void assertProductionResource(String key, String module) throws Exception {
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
            assertArrayEquals("The engine must execute the packaged production BPMN", original.readAllBytes(),
                    deployed.readAllBytes());
        }
    }
}
