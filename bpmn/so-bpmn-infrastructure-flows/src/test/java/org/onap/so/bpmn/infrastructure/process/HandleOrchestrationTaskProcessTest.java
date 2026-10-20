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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;

public class HandleOrchestrationTaskProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "HandleOrchestrationTask";
    private static final String COLLECTION = "/orchestrationTask/";
    private static final String TASK = COLLECTION + "task-1";
    private final List<String> instances = new ArrayList<>();

    @Before
    public void configureContracts() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("mso.adapters.requestDb.auth", "Basic dGVzdDp0ZXN0");
        setWorkflowProperties(properties);
        wireMockServer.resetAll();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS)
                .latestVersion().singleResult();
        assertNotNull("Missing production orchestration-task process", definition);
        try (InputStream original =
                getClass().getClassLoader().getResourceAsStream("subprocess/HandleOrchestrationTask.bpmn");
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertNotNull(original);
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }

    @After
    public void removeOwnedInstances() {
        for (String id : instances) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "Test cleanup");
            }
        }
    }

    @Test
    public void postUsesTheCollectionAndExecutesTheGroovyHeaderInputScript() {
        wireMockServer.stubFor(post(urlEqualTo(COLLECTION)).willReturn(
                aResponse().withStatus(201).withHeader("Connection", "close").withBody("{\"taskId\":\"task-1\"}")));
        ProcessInstance process = start(inputs("POST"));
        assertSuccess(process, 201);
        wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(COLLECTION)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Content-Type", equalTo("application/json")));
        JSONObject payload = new JSONObject(
                wireMockServer.findAll(postRequestedFor(urlEqualTo(COLLECTION))).get(0).getBodyAsString());
        assertPayload(payload);
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void putTargetsTheTaskAndPreservesStringFlagsAndNestedParameterTypes() {
        wireMockServer.stubFor(put(urlEqualTo(TASK)).willReturn(
                aResponse().withStatus(200).withHeader("Connection", "close").withBody("{\"updated\":true}")));
        ProcessInstance process = start(inputs("PUT"));
        assertSuccess(process, 200);
        wireMockServer.verify(1,
                putRequestedFor(urlEqualTo(TASK)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Content-Type", equalTo("application/json")));
        assertPayload(
                new JSONObject(wireMockServer.findAll(putRequestedFor(urlEqualTo(TASK))).get(0).getBodyAsString()));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void getNeedsNoMutationFieldsAndRetainsTheResponse() {
        Map<String, Object> inputs = inputs("GET");
        for (String name : new String[] {"requestId", "taskName", "taskStatus", "isManual", "paramJson"}) {
            inputs.remove(name);
        }
        String response = "{\"taskId\":\"task-1\",\"name\":\"東京 & task\",\"complete\":false}";
        wireMockServer.stubFor(get(urlEqualTo(TASK)).willReturn(aResponse().withStatus(200)
                .withHeader("Connection", "close").withHeader("Content-Type", "application/json").withBody(response)));
        ProcessInstance process = start(inputs);
        assertSuccess(process, 200);
        assertEquals("", historic(process, "payload"));
        assertEquals(response, historic(process, "response"));
        wireMockServer.verify(1,
                getRequestedFor(urlEqualTo(TASK)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0")));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void rejectedResponsesUseTheBusinessErrorAfterOneConnectorAction() {
        for (int status : new int[] {202, 400, 500}) {
            wireMockServer.resetAll();
            wireMockServer.stubFor(put(urlEqualTo(TASK)).willReturn(aResponse().withStatus(status)
                    .withHeader("Connection", "close").withBody(status == 204 ? "" : "rejected task")));
            ProcessInstance process = start(inputs("PUT"));
            assertBusinessError(process);
            assertEquals(Integer.valueOf(status), historic(process, "statusCode"));
            assertActivity(process, "ServiceTask_1iat8g5", 1);
            assertActivity(process, "EndEvent_18t5h42", 0);
            wireMockServer.verify(1, putRequestedFor(urlEqualTo(TASK)));
            assertEquals(1, wireMockServer.getAllServeEvents().size());
        }
    }

    @Test
    public void emptyNoContentResponseUsesTheBusinessErrorRoute() {
        Assume.assumeTrue("SO-4446: an empty connector response must reach orchestration-task error handling",
                Boolean.getBoolean("groovy.migration.verification"));
        wireMockServer.stubFor(
                put(urlEqualTo(TASK)).willReturn(aResponse().withStatus(204).withHeader("Connection", "close")));
        try {
            ProcessInstance process = start(inputs("PUT"));
            assertBusinessError(process);
            assertActivity(process, "EndEvent_18t5h42", 0);
        } catch (ProcessEngineException unexpected) {
            throw new AssertionError("Empty HTTP 204 response must use workflow error 7000", unexpected);
        }
        wireMockServer.verify(1, putRequestedFor(urlEqualTo(TASK)));
    }

    @Test
    public void unsupportedMethodsFailBeforeTheConnector() {
        for (String method : new String[] {null, "DELETE", "get"}) {
            assertValidationFailure(inputs(method));
        }
    }

    @Test
    public void absentOrBlankTaskIdsFailBeforeTheConnector() {
        for (String id : new String[] {null, "", " \t"}) {
            Map<String, Object> inputs = inputs("GET");
            inputs.put("taskId", id);
            assertValidationFailure(inputs);
        }
    }

    @Test
    public void eachMutationFieldIsRequiredForBothPostAndPut() {
        for (String method : new String[] {"POST", "PUT"}) {
            for (String name : new String[] {"requestId", "taskName", "taskStatus", "isManual"}) {
                for (String missing : new String[] {null, "", " \t"}) {
                    Map<String, Object> inputs = inputs(method);
                    inputs.put(name, missing);
                    assertValidationFailure(inputs);
                }
            }
        }
    }

    private Map<String, Object> inputs(String method) {
        Map<String, Object> result = new HashMap<>();
        result.put("mso.adapters.requestDb.endpoint", "http://localhost:" + wireMockPort);
        result.put("mso-request-id", "request-1");
        result.put("method", method);
        result.put("taskId", "task-1");
        result.put("requestId", "request-1");
        result.put("taskName", "東京 & \"manual\"");
        result.put("taskStatus", "pending");
        result.put("isManual", "false");
        result.put("paramJson", "{\"enabled\":false,\"count\":0,\"items\":[1,2],\"empty\":\"\"}");
        return result;
    }

    private ProcessInstance start(Map<String, Object> inputs) {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), inputs);
        instances.add(process.getId());
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(process.getId())
                .finished().count());
        return process;
    }

    private void assertPayload(JSONObject payload) {
        assertEquals("task-1", payload.get("taskId"));
        assertEquals("request-1", payload.get("requestId"));
        assertEquals("東京 & \"manual\"", payload.get("name"));
        assertEquals("pending", payload.get("status"));
        assertEquals("false", payload.get("isManual"));
        assertTrue(payload.get("createdTime") instanceof Number);
        assertTrue(payload.getLong("createdTime") > 0);
        assertTrue(payload.get("params") instanceof String);
        JSONObject parameters = new JSONObject(payload.getString("params"));
        assertEquals(Boolean.FALSE, parameters.get("enabled"));
        assertEquals(Integer.valueOf(0), parameters.get("count"));
        assertEquals("", parameters.get("empty"));
        assertEquals(2, parameters.getJSONArray("items").length());
    }

    private void assertValidationFailure(Map<String, Object> inputs) {
        ProcessInstance process = start(inputs);
        assertBusinessError(process);
        assertActivity(process, "ServiceTask_1iat8g5", 0);
        assertActivity(process, "EndEvent_18t5h42", 0);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    private void assertBusinessError(ProcessInstance process) {
        assertTrue(historic(process, "WorkflowException") instanceof WorkflowException);
        assertEquals(7000, ((WorkflowException) historic(process, "WorkflowException")).getErrorCode());
    }

    private void assertSuccess(ProcessInstance process, int status) {
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Integer.valueOf(status), historic(process, "statusCode"));
        assertTrue(historic(process, "headerMap") instanceof Map);
        assertActivity(process, "ScriptTask_0r0a9ga", 1);
        assertActivity(process, "ServiceTask_1iat8g5", 1);
        assertActivity(process, "ScriptTask_119zm52", 1);
        assertActivity(process, "EndEvent_18t5h42", 1);
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private void assertActivity(ProcessInstance process, String name, long expected) {
        assertEquals(name, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(name).finished().count());
    }
}
