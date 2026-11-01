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
package org.onap.so.bpmn.common.verification;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class SdncAdapterProcessPersistenceTest extends WorkflowPersistenceSupport {
    private WireMockServer adapter;
    private MockedStatic<UrnPropertiesReader> properties;

    @Before
    public void deployProductionResourcesAndConfigureAdapter() throws Exception {
        for (String name : new String[] {"SDNCAdapterRestV2", "GenericNotificationService"}) {
            String path = "subprocess/" + name + ".bpmn";
            URL resource = getClass().getClassLoader().getResource(path);
            assertNotNull(path, resource);
            String location = resource.toExternalForm();
            assertTrue(location,
                    location.contains("/MSOCommonBPMN/target/classes/")
                            || (location.matches("jar:.*/MSOCommonBPMN-[^/]+\\.jar!/" + path)
                                    && !location.contains("-tests.jar!/")));
            try (InputStream input = resource.openStream()) {
                engine.getRepositoryService().createDeployment().addInputStream(path, input).deploy();
            }
            var definition = engine.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(name)
                    .singleResult();
            try (InputStream original = resource.openStream();
                    InputStream deployed = engine.getRepositoryService()
                            .getResourceAsStream(definition.getDeploymentId(), definition.getResourceName())) {
                assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
            }
        }
        adapter = new WireMockServer(0);
        adapter.start();
        adapter.stubFor(post(urlEqualTo("/sdnc")).willReturn(aResponse().withStatus(202).withBody("{}")));
        properties = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS);
        properties.when(() -> UrnPropertiesReader.getVariable(eq("mso.adapters.sdnc.rest.endpoint"),
                any(DelegateExecution.class))).thenReturn("http://localhost:" + adapter.port() + "/sdnc///");
        properties.when(() -> UrnPropertiesReader.getVariable(eq("mso.workflow.notification.name"),
                any(DelegateExecution.class))).thenReturn("GenericNotificationService");
    }

    @After
    public void closeAdapter() {
        try {
            if (properties != null) {
                properties.close();
            }
        } finally {
            if (adapter != null) {
                adapter.stop();
            }
        }
    }

    @Test
    public void concurrentRequestsResumeTheirOwnCallbacksWithoutRepeatingPosts() {
        ProcessInstance first = start("first", null);
        ProcessInstance second = start("second", null);
        assertEquals(Integer.valueOf(202),
                engine.getRuntimeService().getVariable(first.getId(), "SDNCREST_sdncAdapterStatusCode"));
        restart();
        reject("unknown");
        callback("second", response("Y"));
        completeAsync(second);
        assertNotNull(engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(first.getId())
                .singleResult());
        callback("first", response("Y"));
        completeAsync(first);
        assertEquals(response("Y"), historic(first, "WorkflowResponse"));
        assertEquals(Boolean.TRUE, historic(first, "SDNCREST_SuccessIndicator"));
        assertEquals("first", historic(first, "SDNCAResponse_CORRELATOR"));
        assertNoCallbackMessage(first);
        reject("first");
        assertPosts(2);
    }

    @Test
    public void interimNotificationRunsTheRealCallActivityBeforeTheFinalCallback() {
        String notification =
                "{\"variableList\":[{\"variable\":{\"name\":\"notificationText\",\"value\":\"Gr\\u00fc\\u00dfe & SO\"}}]}";
        ProcessInstance process = start("interim", notification);
        callback("interim", response("N"));
        assertEquals(1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId("Task_1ashvem").finished().count());
        assertEquals("Gr\u00fc\u00dfe & SO",
                engine.getRuntimeService().getVariable(process.getId(), "notificationText"));
        restart();
        callback("interim", response("Y"));
        completeAsync(process);
        assertEquals(response("N"), historic(process, "SDNCREST_callback1"));
        assertEquals(response("Y"), historic(process, "SDNCREST_callback2"));
        assertEquals(response("Y"), historic(process, "WorkflowResponse"));
        assertPosts(1);
    }

    @Test
    public void malformedCallbackUsesTheProductionErrorSubprocess() {
        ProcessInstance process = start("malformed", null);
        callback("malformed", "not-json");
        assertError(process, 5300);
        assertEquals("not-json", historic(process, "SDNCREST_callback1"));
        assertNoCallbackMessage(process);
        assertPosts(1);
    }

    @Test
    public void rejectedHttpRequestFinishesWithoutWaitingForCallbacks() {
        adapter.stubFor(post(urlEqualTo("/sdnc")).willReturn(aResponse().withStatus(404).withBody("missing")));
        ProcessInstance process = start("missing", null);
        assertError(process, 5300);
        assertEquals(0,
                engine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(process.getId()).count());
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(process.getId()).count());
        assertPosts(1);
    }

    @Test
    public void persistedTimeoutKeepsItsDeadlineAndRejectsLateCallbacks() {
        ProcessInstance process = start("timeout", null);
        Job timer = engine.getManagementService().createJobQuery().processInstanceId(process.getId()).timers()
                .singleResult();
        assertEquals(new Date(START_TIME + 60000), timer.getDuedate());
        restart();
        Job restored = engine.getManagementService().createJobQuery().jobId(timer.getId()).singleResult();
        assertEquals(timer.getDuedate(), restored.getDuedate());
        ClockUtil.setCurrentTime(new Date(START_TIME + 61000));
        engine.getManagementService().executeJob(restored.getId());
        assertError(process, 5320);
        reject("timeout");
        assertPosts(1);
    }

    @Test
    public void configuredAuthorizationSurvivesRestartWithoutRepeatingTheRequest() {
        properties.when(() -> UrnPropertiesReader.getVariable("so.auth.username")).thenReturn("contract");
        properties.when(() -> UrnPropertiesReader.getVariable("so.auth.password")).thenReturn("contract");
        ProcessInstance process = start("authorized", null);
        restart();
        Object stored = engine.getRuntimeService().getVariable(process.getId(), "SDNCREST_basicAuthHeaderValue");
        assertEquals(String.class, stored.getClass());
        assertEquals("Basic Y29udHJhY3Q6Y29udHJhY3Q=", stored);
        callback("authorized", response("Y"));
        completeAsync(process);
        adapter.verify(1, postRequestedFor(urlEqualTo("/sdnc")).withHeader("Authorization",
                equalTo("Basic Y29udHJhY3Q6Y29udHJhY3Q=")));
        assertPosts(1);
    }

    private ProcessInstance start(String id, String notification) {
        String request = "{\"SDNCServiceRequest\":{\"sdncRequestId\":\"" + id
                + "\",\"bpNotificationUrl\":\"http://callback.invalid/" + id + "\",\"bpTimeout\":\"PT1M\","
                + "\"capacity\":0,\"enabled\":false}}";
        Map<String, Object> input = new HashMap<>();
        input.put("mso-request-id", "request-" + id);
        input.put("mso-service-instance-id", "service-" + id);
        input.put("bpmnRequest", request);
        if (notification != null) {
            input.put("SDNCREST_InterimNotification1", notification);
        }
        ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("SDNCAdapterRestV2", id, input);
        adapter.verify(1,
                postRequestedFor(urlEqualTo("/sdnc")).withHeader("mso-request-id", equalTo("request-" + id))
                        .withHeader("mso-service-instance-id", equalTo("service-" + id))
                        .withRequestBody(matchingJsonPath("$.SDNCServiceRequest.sdncRequestId", equalTo(id))));
        return process;
    }

    private String response(String indicator) {
        return "{\"SDNCServiceResponse\":{\"responseCode\":\"200\",\"ackFinalIndicator\":\"" + indicator + "\"}}";
    }

    private void callback(String id, String body) {
        engine.getRuntimeService().createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("SDNCAResponse_CORRELATOR", id)
                .setVariable("SDNCAResponse_MESSAGE", body).correlate();
    }

    private void reject(String id) {
        try {
            callback(id, response("Y"));
            fail("Unexpected callback correlation: " + id);
        } catch (MismatchingMessageCorrelationException expected) {
            assertTrue(expected.getMessage().contains("WorkflowMessage"));
        }
    }

    private void completeAsync(ProcessInstance process) {
        Job job = engine.getManagementService().createJobQuery().processInstanceId(process.getId()).singleResult();
        assertNotNull(job);
        engine.getManagementService().executeJob(job.getId());
        assertEnded(process);
    }

    private void assertEnded(ProcessInstance process) {
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
    }

    private void assertError(ProcessInstance process, int code) {
        assertEnded(process);
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertEquals(code, error.getErrorCode());
        assertEquals(Boolean.FALSE, historic(process, "SDNCREST_SuccessIndicator"));
        assertEquals(1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId("ScriptTask_8").finished().count());
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        assertNotNull(name, value);
        return value.getValue();
    }

    private void assertNoCallbackMessage(ProcessInstance process) {
        assertEquals(0, engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName("SDNCAResponse_MESSAGE").count());
    }

    private void assertPosts(int count) {
        adapter.verify(count,
                postRequestedFor(urlEqualTo("/sdnc")).withHeader("Content-Type", containing("application/json"))
                        .withRequestBody(matchingJsonPath("$.SDNCServiceRequest.capacity", equalTo("0")))
                        .withRequestBody(matchingJsonPath("$.SDNCServiceRequest.enabled", equalTo("false"))));
        assertEquals(count, adapter.getAllServeEvents().size());
    }
}
