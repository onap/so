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
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class OofWorkflowPersistenceTest extends WorkflowPersistenceSupport {
    private WireMockServer requests;
    private MockedStatic<UrnPropertiesReader> properties;

    @Before
    public void deployOofAndConfigureItsExternalBoundary() throws Exception {
        String resourceName = "subprocess/DoHandleOofRequest.bpmn";
        URL resource = getClass().getClassLoader().getResource(resourceName);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + resourceName)
                                && !location.contains("-tests.jar!/")));
        try (InputStream input = resource.openStream()) {
            engine.getRepositoryService().createDeployment().addInputStream(resourceName, input).deploy();
        }
        ProcessDefinition definition = engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey("DoHandleOofRequest").singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = engine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
        requests = new WireMockServer(0);
        requests.start();
        requests.stubFor(
                post(urlEqualTo("/oof")).willReturn(aResponse().withStatus(202).withHeader("Connection", "close")));
        properties = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS);
        properties.when(
                () -> UrnPropertiesReader.getVariable(eq("mso.adapters.oof.endpoint"), any(DelegateExecution.class)))
                .thenReturn("http://localhost:" + requests.port() + "/oof");
    }

    @After
    public void closeExternalBoundary() {
        try {
            if (properties != null) {
                properties.close();
            }
        } finally {
            if (requests != null) {
                requests.stop();
            }
        }
    }

    @Test
    public void persistedOofParentsResumeOnlyTheirMatchingChildWithoutRepeatingHttp() {
        ProcessInstance first = start("first-oof");
        ProcessInstance firstChild = child(first);
        ProcessInstance second = start("second-oof");
        ProcessInstance secondChild = child(second);
        engine.getRuntimeService().setVariableLocal(firstChild.getId(), "scope-marker", "child");
        String payload = (String) engine.getRuntimeService().getVariable(first.getId(), "oofRequestPayload");
        assertEquals("OOF", engine.getRuntimeService().getVariableLocal(firstChild.getId(), "RCVWFMSG_messageType"));
        assertEquals("first-oof", engine.getRuntimeService().getVariableLocal(firstChild.getId(), "OOF_CORRELATOR"));
        assertEquals("PT1M", engine.getRuntimeService().getVariableLocal(firstChild.getId(), "RCVWFMSG_timeout"));
        assertFalse(engine.getRuntimeService().getVariablesLocal(firstChild.getId()).containsKey("oofRequest"));
        assertFalse(engine.getRuntimeService().getVariablesLocal(first.getId()).containsKey("OOF_CORRELATOR"));
        assertPost("first-oof");
        assertPost("second-oof");
        restart();

        assertEquals(firstChild.getId(), child(first).getId());
        assertEquals(secondChild.getId(), child(second).getId());
        assertEquals("parent", engine.getRuntimeService().getVariableLocal(first.getId(), "scope-marker"));
        assertEquals("child", engine.getRuntimeService().getVariableLocal(firstChild.getId(), "scope-marker"));
        assertEquals(Integer.valueOf(0), engine.getRuntimeService().getVariable(first.getId(), "zero"));
        assertEquals(Boolean.FALSE, engine.getRuntimeService().getVariable(first.getId(), "disabled"));
        Map<String, Object> restored = engine.getRuntimeService().getVariablesLocal(first.getId());
        assertTrue(restored.containsKey("nullable"));
        assertNull(restored.get("nullable"));
        assertEquals("", restored.get("empty"));
        assertEquals(String.class, restored.get("oofRequestPayload").getClass());
        assertEquals(payload, restored.get("oofRequestPayload"));
        assertTrue(restored.get("oofRequest") instanceof Map);
        assertEquals(Boolean.FALSE, ((Map<?, ?>) restored.get("oofRequest")).get("shared"));
        rejectCallback("unknown-oof");
        correlate("second-oof", "second-response");
        assertEnded(second);
        assertEquals("second-response", historic(second, "asyncCallbackResponse"));
        assertNotNull(child(first));
        String response = "{\"status\":\"ok\",\"name\":\"Gr\u00fc\u00dfe & \u96ea\",\"count\":0,\"enabled\":false}";
        correlate("first-oof", response);
        assertEnded(first);
        assertEquals(response, historic(first, "asyncCallbackResponse"));
        assertNull(historic(first, "WorkflowException"));
        assertEquals(Boolean.TRUE, historic(firstChild, "RCVWFMSG_SuccessIndicator"));
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(firstChild.getId()).count());
        rejectCallback("first-oof");
        assertPost("first-oof");
        assertPost("second-oof");
        assertEquals(2, requests.getAllServeEvents().size());
        assertActivity(first, "Activity_0tki17o", 1);
        assertActivity(first, "Activity_08ozlfa", 1);
        assertActivity(first, "Activity_1yq993l", 1);
        assertActivity(first, "Event_179tp7r", 1);
    }

    @Test
    public void persistedOofTimerResumesTheProductionErrorRouteWithoutRepeatingHttp() {
        ProcessInstance parent = start("timer-oof");
        ProcessInstance child = child(parent);
        Job timer = engine.getManagementService().createJobQuery().processInstanceId(child.getId()).singleResult();
        assertNotNull(timer);
        assertEquals(new Date(START_TIME + 60000), timer.getDuedate());
        restart();
        Job restored = engine.getManagementService().createJobQuery().processInstanceId(child.getId()).singleResult();
        assertNotNull(restored);
        assertEquals(timer.getId(), restored.getId());
        assertEquals(timer.getDuedate(), restored.getDuedate());
        assertPost("timer-oof");
        ClockUtil.setCurrentTime(new Date(START_TIME + 61000));
        engine.getManagementService().executeJob(restored.getId());

        assertEnded(child);
        assertEnded(parent);
        WorkflowException childError = (WorkflowException) historic(child, "WorkflowException");
        assertEquals(7010, childError.getErrorCode());
        assertEquals("Receive Workflow Message Timeout Error", childError.getErrorMessage());
        assertEquals(WorkflowException.class, historic(parent, "WorkflowException").getClass());
        assertNull(historic(parent, "asyncCallbackResponse"));
        assertActivity(child, "BoundaryEvent_1", 1);
        assertActivity(child, "ScriptTask_5", 1);
        assertActivity(child, "ScriptTask_setSuccess", 0);
        assertActivity(parent, "Activity_11rvko0", 1);
        assertActivity(parent, "Event_179tp7r", 0);
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(child.getId()).count());
        rejectCallback("timer-oof");
        assertPost("timer-oof");
        assertEquals(1, requests.getAllServeEvents().size());
    }

    private ProcessInstance start(String correlator) {
        Map<String, Object> input = new HashMap<>();
        input.put("msoRequestId", "request-" + correlator);
        input.put("apiPath", "/nst/selection");
        input.put("correlator", correlator);
        input.put("messageType", "OOF");
        input.put("timeout", "PT1M");
        input.put("scope-marker", "parent");
        input.put("zero", 0);
        input.put("disabled", false);
        input.put("nullable", null);
        input.put("empty", "");
        Map<String, Object> details = new HashMap<>();
        details.put("requestId", correlator);
        details.put("capacity", 0);
        details.put("shared", false);
        details.put("name", "Gr\u00fc\u00dfe & \u96ea");
        input.put("oofRequest", details);
        return engine.getRuntimeService().startProcessInstanceByKey("DoHandleOofRequest", correlator, input);
    }

    private ProcessInstance child(ProcessInstance parent) {
        ProcessInstance value = engine.getRuntimeService().createProcessInstanceQuery()
                .superProcessInstanceId(parent.getId()).singleResult();
        assertNotNull(value);
        return value;
    }

    private void correlate(String correlator, String response) {
        engine.getRuntimeService().createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("OOF_CORRELATOR", correlator).setVariable("OOF_MESSAGE", response)
                .correlate();
    }

    private void rejectCallback(String correlator) {
        try {
            correlate(correlator, "unexpected-response");
            fail("Callback must not correlate: " + correlator);
        } catch (MismatchingMessageCorrelationException expected) {
            assertTrue(expected.getMessage().contains("WorkflowMessage"));
        }
    }

    private void assertPost(String correlator) {
        requests.verify(1,
                postRequestedFor(urlEqualTo("/oof")).withHeader("Content-Type", containing("application/json"))
                        .withRequestBody(matchingJsonPath("$.apiPath", equalTo("/nst/selection")))
                        .withRequestBody(matchingJsonPath("$.requestDetails.requestId", equalTo(correlator)))
                        .withRequestBody(matchingJsonPath("$.requestDetails.capacity", equalTo("0")))
                        .withRequestBody(matchingJsonPath("$.requestDetails.shared", equalTo("false"))).withRequestBody(
                                matchingJsonPath("$.requestDetails.name", equalTo("Gr\u00fc\u00dfe & \u96ea"))));
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        assertNotNull(name, value);
        return value.getValue();
    }

    private void assertEnded(ProcessInstance process) {
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
    }

    private void assertActivity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }
}
