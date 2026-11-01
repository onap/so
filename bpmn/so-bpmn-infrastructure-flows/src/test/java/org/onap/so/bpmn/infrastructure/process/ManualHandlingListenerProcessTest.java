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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.utils.BasicAuth;
import org.springframework.beans.factory.annotation.Autowired;

public class ManualHandlingListenerProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "ManualHandling";
    private static final String USER_TASK = "ManualHandlingUserTask";
    private static final String PATH = "/v1/request-db/infra-requests/request%3C%261/updates";
    private String authorization;
    private final List<String> instances = new ArrayList<>();
    @Autowired
    private TaskService tasks;

    @Before
    public void configureManualTaskBoundary() throws Exception {
        wireMockServer.resetAll();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS)
                .latestVersion().singleResult();
        assertNotNull("Missing production manual-handling process", definition);
        try (InputStream original =
                getClass().getClassLoader().getResourceAsStream("subprocess/BuildingBlock/ManualHandling.bpmn");
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertNotNull(original);
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
        variables.put("msoRequestId", "request<&1");
        variables.put("serviceType", "5G");
        variables.put("vnfType", "core");
        variables.put("requestorId", "requestor-1");
        variables.put("failedActivity", "controller-1");
        variables.put("currentActivity", "CreateNetwork");
        variables.put("workStep", "activate");
        variables.put("errorCode", "5300");
        variables.put("errorText", "東京 & <controller> failed");
        variables.put("validResponses", "retry,abort");
        variables.put("mso.adapters.requestDb.endpoint", "http://localhost:" + wireMockPort);
        String password = UUID.randomUUID().toString();
        variables.put("so.auth.username", "test-user");
        variables.put("so.auth.password", password);
        authorization = BasicAuth.header("test-user", password);
        wireMockServer.stubFor(
                post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(204).withHeader("Connection", "close")));
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
    public void createListenerPublishesTypedTaskMetadataAtTheRealUserTaskWaitState() {
        ProcessInstance process = startAtUserTask();
        Task task = task(process);
        Map<String, Object> values = tasks.getVariables(task.getId());
        Map<String, String> expected = new HashMap<>();
        expected.put("type", "fallout");
        expected.put("nfRole", "core");
        expected.put("subscriptionServiceType", "5G");
        expected.put("originalRequestId", "request<&1");
        expected.put("originalRequestorId", "requestor-1");
        expected.put("errorSource", "controller-1");
        expected.put("errorCode", "5300");
        expected.put("errorMessage", "東京 & <controller> failed");
        expected.put("buildingBlockName", "CreateNetwork");
        expected.put("buildingBlockStep", "activate");
        expected.put("validResponses", "retry,abort");
        expected.put("description", "");
        expected.put("timeout", "");
        expected.forEach((name, value) -> assertEquals(name, value, values.get(name)));
        assertEquals(task.getId(), runtimeService.getVariable(process.getId(), "taskId"));
        assertEquals(USER_TASK, task.getTaskDefinitionKey());
        assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void localRetryResponseIsPromotedByTheCompleteListenerAndStatusIsWrittenOnce() throws Exception {
        assertCompletedResponse("retry", "Retry");
    }

    @Test
    public void localAbortResponseReturnsToTheCallerAfterTheInProgressStatusWrite() throws Exception {
        assertCompletedResponse("abort", "Abort");
    }

    @Test
    public void missingResponseUsesTheProductionErrorSubprocessBeforeAnyStatusMutation() {
        assertResponseError(null);
    }

    @Test
    public void emptyResponseUsesTheProductionErrorSubprocessBeforeAnyStatusMutation() {
        assertResponseError("");
    }

    private ProcessInstance startAtUserTask() {
        ProcessInstance process =
                runtimeService.createProcessInstanceByKey(PROCESS).businessKey(UUID.randomUUID().toString())
                        .setVariables(variables).startBeforeActivity(USER_TASK).execute();
        instances.add(process.getId());
        return process;
    }

    private Task task(ProcessInstance process) {
        Task task = tasks.createTaskQuery().processInstanceId(process.getId()).singleResult();
        assertNotNull("Expected manual user task", task);
        return task;
    }

    private void assertCompletedResponse(String supplied, String expected) throws Exception {
        ProcessInstance process = startAtUserTask();
        Task task = task(process);
        tasks.setVariableLocal(task.getId(), "responseValue", supplied);
        assertEquals(supplied, tasks.getVariableLocal(task.getId(), "responseValue"));
        assertNull(runtimeService.getVariable(process.getId(), "responseValue"));
        tasks.complete(task.getId());
        assertEquals(expected, historic(process, "responseValue"));
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(0, tasks.createTaskQuery().processInstanceId(process.getId()).count());
        assertActivity(process, USER_TASK, 1);
        assertActivity(process, "Task_0r60vxk", 1);
        assertActivity(process, "Task_02xybqx", 1);
        assertActivity(process, "EndEvent_3", 1);
        assertActivity(process, "processJavaError", 0);
        assertEquals(Integer.valueOf(204), historic(process, "updateRequestResponseCode"));
        assertEquals("", historic(process, "updateRequestResponse"));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(PATH)).withHeader("Authorization", equalTo(authorization))
                .withHeader("Content-Type", equalTo("application/json")));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
        String payload = wireMockServer.findAll(postRequestedFor(urlEqualTo(PATH))).get(0).getBodyAsString();
        JsonNode json = new ObjectMapper().readTree(payload);
        assertEquals(2, json.size());
        assertEquals("IN_PROGRESS", json.get("requestStatus").asText());
        assertEquals("ManualHandling", json.get("lastModifiedBy").asText());
        try {
            tasks.complete(task.getId());
            fail("A completed manual task must not be completed twice");
        } catch (ProcessEngineException expectedDuplicate) {
            assertEquals(1, wireMockServer.getAllServeEvents().size());
        }
    }

    private void assertResponseError(String response) {
        Assume.assumeTrue("SO-4446: manual-task listener error 7000 is replaced by generic error 2500",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = startAtUserTask();
        Task task = task(process);
        if (response != null) {
            tasks.setVariableLocal(task.getId(), "responseValue", response);
        }
        tasks.complete(task.getId());
        Object stored = historic(process, "WorkflowException");
        assertTrue(stored instanceof WorkflowException);
        assertActivity(process, "processJavaError", 1);
        assertActivity(process, "Task_02xybqx", 0);
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
        assertEquals(7000, ((WorkflowException) stored).getErrorCode());
    }

    private Object historic(ProcessInstance process, String name) {
        List<HistoricVariableInstance> variables = new ArrayList<>();
        for (HistoricVariableInstance variable : historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).list()) {
            if (variable.getTaskId() == null) {
                variables.add(variable);
            }
        }
        assertTrue("Ambiguous process variable " + name, variables.size() <= 1);
        return variables.isEmpty() ? null : variables.get(0).getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }
}
