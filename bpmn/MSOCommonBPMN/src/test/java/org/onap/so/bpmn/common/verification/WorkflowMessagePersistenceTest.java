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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.onap.so.client.policy.PolicyClientImpl;
import org.onap.so.bpmn.common.scripts.ReceiveWorkflowMessage;
import org.onap.so.bpmn.common.scripts.TrinityExceptionUtil;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class WorkflowMessagePersistenceTest {
    private static final long START_TIME = Instant.parse("2020-01-01T00:00:00Z").toEpochMilli();
    private static DB maria;
    private static Path mariaDataDirectory;
    private static byte[] productionBpmn;
    private static byte[] manualHandlingBpmn;
    private static byte[] rainyDayBpmn;
    private static byte[] cnfAdapterBpmn;
    private ProcessEngine engine;
    private String database;
    private String jdbcUrl;

    @BeforeClass
    public static void configureBackendAndProductionResource() throws Exception {
        String backend = System.getProperty("groovy.migration.persistence.backend", "h2");
        assertTrue("Expected h2 or mariadb persistence backend", Arrays.asList("h2", "mariadb").contains(backend));
        productionBpmn = productionResource("subprocess/ReceiveWorkflowMessage.bpmn");
        manualHandlingBpmn = productionResource("subprocess/BuildingBlock/ManualHandling.bpmn");
        rainyDayBpmn = productionResource("subprocess/BuildingBlock/RainyDayHandler.bpmn");
        cnfAdapterBpmn = productionResource("subprocess/CNFAdapterAsyncCall.bpmn");
        if ("mariadb".equals(backend)) {
            Path directory = Paths.get("target", "groovy-migration", "persistence").toAbsolutePath();
            Files.createDirectories(directory);
            DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
            config.setPort(0);
            config.setBaseDir(directory.resolve("mariadb-base").toString());
            mariaDataDirectory = Files.createTempDirectory(directory, "db-");
            config.setDataDir(mariaDataDirectory.toString());
            config.addArg("--lower_case_table_names=1");
            maria = DB.newEmbeddedDB(config.build());
            maria.start();
        }
    }

    @AfterClass
    public static void stopBackend() throws Exception {
        if (maria != null) {
            maria.stop();
            maria = null;
        }
        if (mariaDataDirectory != null) {
            try (Stream<Path> files = Files.walk(mariaDataDirectory)) {
                for (Path file : (Iterable<Path>) files.sorted(Comparator.reverseOrder())::iterator) {
                    Files.delete(file);
                }
            }
            mariaDataDirectory = null;
        }
    }

    @Before
    public void createEngine() throws Exception {
        ClockUtil.setCurrentTime(new Date(START_TIME));
        database = "groovy_" + UUID.randomUUID().toString().replace("-", "");
        if (maria == null) {
            jdbcUrl = "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1";
        } else {
            try (Connection connection = DriverManager.getConnection(maria.getConfiguration().getURL(""), "root", "")) {
                connection.createStatement().execute("CREATE DATABASE " + database);
            }
            jdbcUrl = maria.getConfiguration().getURL(database);
        }
        reopenEngine();
        engine.getRepositoryService().createDeployment()
                .addInputStream("ReceiveWorkflowMessage.bpmn", new ByteArrayInputStream(productionBpmn))
                .addInputStream("ManualHandling.bpmn", new ByteArrayInputStream(manualHandlingBpmn))
                .addInputStream("RainyDayHandler.bpmn", new ByteArrayInputStream(rainyDayBpmn))
                .addInputStream("CNFAdapterAsyncCall.bpmn", new ByteArrayInputStream(cnfAdapterBpmn)).deploy();
    }

    @After
    public void closeEngineAndDatabase() throws Exception {
        try {
            if (engine != null) {
                engine.close();
            }
            if (jdbcUrl != null) {
                if (maria == null) {
                    try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "")) {
                        connection.createStatement().execute("SHUTDOWN");
                    }
                } else {
                    try (Connection connection =
                            DriverManager.getConnection(maria.getConfiguration().getURL(""), "root", "")) {
                        connection.createStatement().execute("DROP DATABASE " + database);
                    }
                }
            }
        } finally {
            ClockUtil.reset();
        }
    }

    @Test
    public void callbackResumesAfterRestartWithTypedAndScopedVariables() {
        Map<String, Object> input = inputs("request-1");
        input.put("zero", 0);
        input.put("disabled", false);
        input.put("nullable", null);
        input.put("scope-marker", "parent");
        input.put("original-error", new WorkflowException("BeforeWait", 5100, "Grüße", "prepare"));
        input.put("trinity-error", legacyErrorResponse());
        RollbackData rollback = new RollbackData();
        rollback.put("VNF", "rollbackVnfCreate", "false");
        input.put("rollbackData", rollback);
        ProcessInstance process = start(input);
        Execution waiting = engine.getRuntimeService().createExecutionQuery().processInstanceId(process.getId())
                .activityId("IntermediateCatchEvent_1").singleResult();
        assertNotNull(waiting);
        engine.getRuntimeService().setVariableLocal(waiting.getId(), "scope-marker", "child");
        restart();

        assertEquals(1, engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey("ReceiveWorkflowMessage").count());
        Map<String, Object> restored = engine.getRuntimeService().getVariables(process.getId());
        assertEquals(Integer.class, restored.get("zero").getClass());
        assertEquals(0, restored.get("zero"));
        assertEquals(Boolean.FALSE, restored.get("disabled"));
        assertTrue(restored.containsKey("nullable"));
        assertNull(restored.get("nullable"));
        assertFalse(restored.containsKey("absent"));
        assertTrue(restored.get("original-error") instanceof WorkflowException);
        WorkflowException original = (WorkflowException) restored.get("original-error");
        assertEquals("BeforeWait", original.getProcessKey());
        assertEquals(5100, original.getErrorCode());
        assertEquals("Grüße", original.getErrorMessage());
        assertEquals("prepare", original.getWorkStep());
        Object legacyError = restored.get("trinity-error");
        assertEquals("org.codehaus.groovy.runtime.GStringImpl", legacyError.getClass().getName());
        assertTrue(legacyError.toString().contains("SVC0001"));
        ObjectValue serialized = engine.getRuntimeService().getVariableTyped(process.getId(), "trinity-error");
        assertEquals("application/x-java-serialized-object", serialized.getSerializationDataFormat());
        assertEquals("org.codehaus.groovy.runtime.GStringImpl", serialized.getObjectTypeName());
        assertTrue(restored.get("rollbackData") instanceof RollbackData);
        assertEquals("false", ((RollbackData) restored.get("rollbackData")).get("VNF", "rollbackVnfCreate"));
        assertEquals("parent", engine.getRuntimeService().getVariableLocal(process.getId(), "scope-marker"));
        assertEquals("child", engine.getRuntimeService().getVariableLocal(waiting.getId(), "scope-marker"));
        assertEquals(1,
                engine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getManagementService().createJobQuery().processInstanceId(process.getId()).count());
        rejectCallback("wrong-request");
        assertFalse(engine.getRuntimeService().getVariables(process.getId()).containsKey("WorkflowResponse"));

        String response = "<response>Grüße &amp; complete</response>";
        correlate("request-1", response);
        assertEnded(process.getId());
        assertEquals(response, historic(process.getId(), "WorkflowResponse"));
        assertEquals(Boolean.TRUE, historic(process.getId(), "RCVWFMSG_SuccessIndicator"));
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(process.getId()).count());
        rejectCallback("request-1");
        assertActivityCount(process.getId(), "ScriptTask_setSuccess", 1);
    }

    @Test
    public void timerSurvivesRestartAndExecutesTheProductionTimeoutRoute() {
        ProcessInstance process = start(inputs("timer-request"));
        Job timer = engine.getManagementService().createJobQuery().processInstanceId(process.getId()).singleResult();
        assertNotNull(timer);
        assertEquals(new Date(START_TIME + 60000), timer.getDuedate());
        restart();
        Job restored = engine.getManagementService().createJobQuery().processInstanceId(process.getId()).singleResult();
        assertEquals(timer.getId(), restored.getId());
        assertEquals(timer.getDuedate(), restored.getDuedate());
        ClockUtil.setCurrentTime(new Date(START_TIME + 61000));
        assertTrue(restored.getDuedate().before(ClockUtil.getCurrentTime()));
        engine.getManagementService().executeJob(restored.getId());

        assertEnded(process.getId());
        assertEquals(Boolean.FALSE, historic(process.getId(), "RCVWFMSG_SuccessIndicator"));
        Object stored = historic(process.getId(), "WorkflowException");
        assertTrue(stored instanceof WorkflowException);
        WorkflowException error = (WorkflowException) stored;
        assertEquals("ReceiveWorkflowMessage", error.getProcessKey());
        assertEquals(7010, error.getErrorCode());
        assertEquals("Receive Workflow Message Timeout Error", error.getErrorMessage());
        assertActivityCount(process.getId(), "BoundaryEvent_1", 1);
        assertActivityCount(process.getId(), "ScriptTask_5", 1);
        assertActivityCount(process.getId(), "EndEvent_8", 1);
        assertActivityCount(process.getId(), "ScriptTask_setSuccess", 0);
        rejectCallback("timer-request");
    }

    @Test
    public void correlationSelectsOnlyTheMatchingWaitingRequest() {
        ProcessInstance first = start(inputs("first"));
        ProcessInstance second = start(inputs("second"));
        rejectCallback("unknown");
        assertEquals(2, engine.getRuntimeService().createEventSubscriptionQuery().eventName("WorkflowMessage").count());
        correlate("second", "second-response");
        assertEnded(second.getId());
        assertEquals("second-response", historic(second.getId(), "WorkflowResponse"));
        assertEquals(1,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(first.getId()).count());
        assertEquals(1,
                engine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(first.getId()).count());
        correlate("first", "first-response");
        assertEnded(first.getId());
        assertEquals("first-response", historic(first.getId(), "WorkflowResponse"));
    }

    @Test
    public void invalidInputsProduceTypedErrorsWithoutCreatingWaitStates() {
        for (String name : new String[] {"RCVWFMSG_timeout", "RCVWFMSG_messageType", "RCVWFMSG_correlator"}) {
            for (Object invalid : Arrays.asList(null, "")) {
                Map<String, Object> input = inputs("invalid-request");
                input.put(name, invalid);
                ProcessInstance process = start(input);
                assertEnded(process.getId());
                Object stored = historic(process.getId(), "WorkflowException");
                assertTrue(stored instanceof WorkflowException);
                WorkflowException error = (WorkflowException) stored;
                assertEquals(2000, error.getErrorCode());
                assertEquals("ReceiveWorkflowMessage: Missing or empty input variable '" + name + "'",
                        error.getErrorMessage());
                assertEquals(0, engine.getRuntimeService().createEventSubscriptionQuery()
                        .processInstanceId(process.getId()).count());
                assertEquals(0,
                        engine.getManagementService().createJobQuery().processInstanceId(process.getId()).count());
            }
        }
    }

    @Test
    public void manualTaskListenersSurviveRestartWithoutRepeatingDatabaseUpdates() throws Exception {
        WireMockServer requests = new WireMockServer(0);
        requests.start();
        try {
            requests.stubFor(post(urlEqualTo("/requests")).willReturn(aResponse().withStatus(204)));
            for (String response : new String[] {"retry", "abort"}) {
                requests.resetRequests();
                String requestId = "manual-" + response;
                Map<String, Object> input = new HashMap<>();
                input.put("msoRequestId", requestId);
                input.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + requests.port() + "/requests");
                input.put("mso.adapters.db.auth", "test:test");
                input.put("mso.msoKey", "");
                input.put("serviceType", "Service");
                input.put("vnfType", "router");
                input.put("currentActivity", "allocate");
                input.put("workStep", "create");
                input.put("failedActivity", "Controller");
                input.put("errorCode", "7000");
                input.put("errorText", "Grüße & controller failure");
                input.put("requestorId", "operator");
                input.put("validResponses", "Retry,Abort");
                input.put("WorkflowException", new WorkflowException("DoCreateVnf", 7000, "Original request failure"));
                ProcessInstance process =
                        engine.getRuntimeService().startProcessInstanceByKey("ManualHandling", requestId, input);
                Task task = engine.getTaskService().createTaskQuery().processInstanceId(process.getId())
                        .taskDefinitionKey("ManualHandlingUserTask").singleResult();
                assertNotNull(task);
                engine.getTaskService().claim(task.getId(), "operator");
                assertDatabaseUpdates(requests, requestId, "PENDING_MANUAL_TASK");
                restart();
                Task restored = engine.getTaskService().createTaskQuery().taskId(task.getId()).singleResult();
                assertNotNull(restored);
                assertEquals("operator", restored.getAssignee());
                assertEquals(task.getCreateTime(), restored.getCreateTime());
                Map<String, Object> taskVariables = engine.getTaskService().getVariables(restored.getId());
                assertEquals("fallout", taskVariables.get("type"));
                assertEquals(requestId, taskVariables.get("originalRequestId"));
                assertEquals("operator", taskVariables.get("originalRequestorId"));
                assertEquals("Controller", taskVariables.get("errorSource"));
                assertEquals("7000", taskVariables.get("errorCode"));
                assertEquals("Grüße & controller failure", taskVariables.get("errorMessage"));
                assertEquals("Retry,Abort", taskVariables.get("validResponses"));
                assertTrue(taskVariables.containsKey("description"));
                assertEquals("", taskVariables.get("description"));
                assertTrue(taskVariables.containsKey("timeout"));
                assertEquals("", taskVariables.get("timeout"));
                assertDatabaseUpdates(requests, requestId, "PENDING_MANUAL_TASK");

                engine.getTaskService().complete(restored.getId(), Collections.singletonMap("responseValue", response));
                assertEnded(process.getId());
                assertEquals("retry".equals(response) ? "Retry" : "Abort", historic(process.getId(), "responseValue"));
                assertEquals(Integer.valueOf(204), historic(process.getId(), "updateRequestResponseCode"));
                assertEquals("", historic(process.getId(), "updateRequestResponse"));
                WorkflowException original = (WorkflowException) historic(process.getId(), "WorkflowException");
                assertEquals("DoCreateVnf", original.getProcessKey());
                assertEquals(7000, original.getErrorCode());
                assertEquals("Original request failure", original.getErrorMessage());
                assertDatabaseUpdates(requests, requestId, "PENDING_MANUAL_TASK", "IN_PROGRESS");
                assertActivityCount(process.getId(), "ManualHandlingUserTask", 1);
            }
        } finally {
            requests.stop();
        }
    }

    @Test
    public void rainyDayManualCallMappingsSurviveRestartWithTaskLocalResponses() throws Exception {
        WireMockServer requests = new WireMockServer(0);
        requests.start();
        try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS);
                MockedConstruction<PolicyClientImpl> policy = mockConstruction(PolicyClientImpl.class)) {
            properties.when(() -> UrnPropertiesReader.getVariable(anyString(), any(DelegateExecution.class)))
                    .thenAnswer(call -> {
                        String name = call.getArgument(0);
                        DelegateExecution execution = call.getArgument(1);
                        Object value = execution.getVariable(name);
                        if (value != null) {
                            return String.valueOf(value);
                        }
                        switch (name) {
                            case "mso.adapters.openecomp.db.endpoint":
                                return "http://localhost:" + requests.port() + "/requests";
                            case "mso.adapters.db.auth":
                                return "test:test";
                            case "mso.msoKey":
                                return "";
                            default:
                                return null;
                        }
                    });
            requests.stubFor(post(urlEqualTo("/requests"))
                    .willReturn(aResponse().withStatus(204).withHeader("Connection", "close")));
            for (String response : new String[] {"retry", "abort"}) {
                requests.resetRequests();
                String requestId = "rainy-manual-" + response;
                Map<String, Object> input = rainyDayRequest(requestId, "Manual");
                ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("RainyDayHandler",
                        UUID.randomUUID().toString(), input);
                ProcessInstance child = engine.getRuntimeService().createProcessInstanceQuery()
                        .superProcessInstanceId(process.getId()).singleResult();
                assertNotNull(child);
                assertEquals("ManualHandling",
                        engine.getRepositoryService().getProcessDefinition(child.getProcessDefinitionId()).getKey());
                Task task = engine.getTaskService().createTaskQuery().processInstanceId(child.getId()).singleResult();
                assertNotNull(task);
                assertEquals(requestId, engine.getTaskService().getVariable(task.getId(), "originalRequestId"));
                assertEquals("operator", engine.getTaskService().getVariable(task.getId(), "originalRequestorId"));
                assertEquals("5300", engine.getTaskService().getVariable(task.getId(), "errorCode"));
                assertEquals("rollback, skip, manual, abort",
                        engine.getTaskService().getVariable(task.getId(), "validResponses"));
                engine.getTaskService().claim(task.getId(), "operator");
                engine.getTaskService().setVariableLocal(task.getId(), "responseValue", response);
                assertNull(engine.getRuntimeService().getVariable(process.getId(), "responseValue"));
                assertDatabaseUpdates(requests, requestId, "PENDING_MANUAL_TASK");
                restart();
                Task restored = engine.getTaskService().createTaskQuery().taskId(task.getId()).singleResult();
                assertNotNull(restored);
                assertEquals(child.getId(), restored.getProcessInstanceId());
                assertEquals("operator", restored.getAssignee());
                assertEquals(response, engine.getTaskService().getVariableLocal(restored.getId(), "responseValue"));
                assertEquals("Manual", engine.getRuntimeService().getVariable(process.getId(), "handlingCode"));
                assertDatabaseUpdates(requests, requestId, "PENDING_MANUAL_TASK");
                engine.getTaskService().complete(restored.getId());
                assertEnded(process.getId());
                assertEnded(child.getId());
                assertEquals("retry".equals(response) ? "Retry" : "Abort", historic(process.getId(), "handlingCode"));
                assertEquals("RDH_", historic(process.getId(), "prefix"));
                assertActivityCount(process.getId(), "preProcessRequest_ScriptTask", 1);
                assertActivityCount(process.getId(), "QueryPolicyEngine", 1);
                assertActivityCount(process.getId(), "Task_1qu3p0x", 1);
                assertActivityCount(child.getId(), "ManualHandlingUserTask", 1);
                assertDatabaseUpdates(requests, requestId, "PENDING_MANUAL_TASK", "IN_PROGRESS");
            }
            assertTrue("Configured disposition must bypass the policy client", policy.constructed().isEmpty());
        } finally {
            requests.stop();
        }
    }

    @Test
    public void rainyDayNonManualDispositionsUseTheProductionGatewayWithoutCreatingTasks() {
        try (MockedConstruction<PolicyClientImpl> policy = mockConstruction(PolicyClientImpl.class)) {
            for (String disposition : new String[] {"Retry", "Abort", "Skip", "Rollback"}) {
                Map<String, Object> input = rainyDayRequest("default-" + disposition, disposition);
                input.put("WorkflowException", new WorkflowException("DoCreateVnf", 5300, "Original request error"));
                ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("RainyDayHandler",
                        UUID.randomUUID().toString(), input);
                assertEnded(process.getId());
                assertEquals(disposition, historic(process.getId(), "handlingCode"));
                assertEquals("rollback, skip, manual, abort", historic(process.getId(), "validResponses"));
                assertEquals("RDH_", historic(process.getId(), "prefix"));
                Object stored = historic(process.getId(), "WorkflowException");
                assertTrue(stored instanceof WorkflowException);
                WorkflowException original = (WorkflowException) stored;
                assertEquals("DoCreateVnf", original.getProcessKey());
                assertEquals(5300, original.getErrorCode());
                assertEquals("Original request error", original.getErrorMessage());
                assertActivityCount(process.getId(), "preProcessRequest_ScriptTask", 1);
                assertActivityCount(process.getId(), "QueryPolicyEngine", 1);
                assertActivityCount(process.getId(), "ExclusiveGateway_0kqwevj", 1);
                assertActivityCount(process.getId(), "Task_1qu3p0x", 0);
                assertActivityCount(process.getId(), "EndEvent_3", 1);
                assertEquals(0, engine.getTaskService().createTaskQuery().processInstanceId(process.getId()).count());
            }
            assertTrue("Configured dispositions must bypass the policy client", policy.constructed().isEmpty());
        }
    }

    @Test
    public void cnfCallMappingsAndCallbackSurviveRestartWithoutRepeatingTheHttpPost() {
        WireMockServer requests = new WireMockServer(0);
        requests.start();
        String correlator = "cnf-resume";
        try (MockedStatic<UrnPropertiesReader> properties = cnfConfiguration(null)) {
            stubCnf(requests, correlator, 202);
            Map<String, Object> input = cnfInputs(requests, correlator);
            input.put("zero", 0);
            input.put("disabled", false);
            ProcessInstance parent =
                    engine.getRuntimeService().startProcessInstanceByKey("CNFAdapterAsyncCall", correlator, input);
            ProcessInstance child = engine.getRuntimeService().createProcessInstanceQuery()
                    .superProcessInstanceId(parent.getId()).singleResult();
            assertNotNull(child);
            assertEquals("ReceiveWorkflowMessage",
                    engine.getRepositoryService().getProcessDefinition(child.getProcessDefinitionId()).getKey());
            assertEquals("CNF", engine.getRuntimeService().getVariableLocal(child.getId(), "RCVWFMSG_messageType"));
            assertEquals(correlator, engine.getRuntimeService().getVariableLocal(child.getId(), "RCVWFMSG_correlator"));
            assertEquals("PT30M", engine.getRuntimeService().getVariableLocal(child.getId(), "RCVWFMSG_timeout"));
            assertEquals(Boolean.FALSE,
                    engine.getRuntimeService().getVariableLocal(child.getId(), "RCVWFMSG_SuccessIndicator"));
            assertFalse(engine.getRuntimeService().getVariablesLocal(child.getId()).containsKey("cnfRequestPayload"));
            assertFalse(engine.getRuntimeService().getVariablesLocal(parent.getId()).containsKey("CNF_CORRELATOR"));
            assertFalse(engine.getRuntimeService().getVariablesLocal(parent.getId()).containsKey("prefix"));
            assertCnfPost(requests, correlator);
            restart();

            ProcessInstance restored = engine.getRuntimeService().createProcessInstanceQuery()
                    .superProcessInstanceId(parent.getId()).singleResult();
            assertNotNull(restored);
            assertEquals(child.getId(), restored.getId());
            assertEquals("PT30M", engine.getRuntimeService().getVariable(parent.getId(), "timeout"));
            Object payload = engine.getRuntimeService().getVariable(parent.getId(), "cnfRequestPayload");
            assertEquals(String.class, payload.getClass());
            assertEquals(cnfPayload(correlator), payload);
            assertEquals(Integer.valueOf(0), engine.getRuntimeService().getVariable(parent.getId(), "zero"));
            assertEquals(Boolean.FALSE, engine.getRuntimeService().getVariable(parent.getId(), "disabled"));
            rejectCnfCallback("wrong-cnf-request");
            assertCnfPost(requests, correlator);
            String response = "{\"status\":\"ok\",\"name\":\"München & 雪\",\"count\":0,\"enabled\":false}";
            correlateCnf(correlator, response);

            assertEnded(child.getId());
            assertEnded(parent.getId());
            assertEquals(response, historic(parent.getId(), "asyncCallbackResponse"));
            assertEquals(String.class, historic(parent.getId(), "asyncCallbackResponse").getClass());
            assertNull(historic(parent.getId(), "WorkflowException"));
            assertEquals(Boolean.TRUE, historic(child.getId(), "RCVWFMSG_SuccessIndicator"));
            assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(child.getId()).count());
            assertActivityCount(parent.getId(), "Activity_0lttdg6", 1);
            assertActivityCount(parent.getId(), "Activity_1ttieoz", 1);
            assertActivityCount(parent.getId(), "Activity_0fhymrt", 1);
            assertActivityCount(parent.getId(), "Event_15f5cnf", 1);
            assertActivityCount(parent.getId(), "Event_1t6t0h6", 0);
            rejectCnfCallback(correlator);
            assertCnfPost(requests, correlator);
            assertEquals(1, requests.getAllServeEvents().size());
        } finally {
            requests.stop();
        }
    }

    @Test
    public void cnfCallbackTimeoutPropagatesThePersistedChildErrorWithoutRepeatingTheHttpPost() {
        WireMockServer requests = new WireMockServer(0);
        requests.start();
        String correlator = "cnf-timeout";
        try (MockedStatic<UrnPropertiesReader> properties = cnfConfiguration("PT1M")) {
            stubCnf(requests, correlator, 202);
            ProcessInstance parent = engine.getRuntimeService().startProcessInstanceByKey("CNFAdapterAsyncCall",
                    correlator, cnfInputs(requests, correlator));
            ProcessInstance child = engine.getRuntimeService().createProcessInstanceQuery()
                    .superProcessInstanceId(parent.getId()).singleResult();
            assertNotNull(child);
            Job timer = engine.getManagementService().createJobQuery().processInstanceId(child.getId()).singleResult();
            assertNotNull(timer);
            assertEquals(new Date(START_TIME + 60000), timer.getDuedate());
            restart();
            Job restored =
                    engine.getManagementService().createJobQuery().processInstanceId(child.getId()).singleResult();
            assertNotNull(restored);
            assertEquals(timer.getId(), restored.getId());
            assertEquals(timer.getDuedate(), restored.getDuedate());
            assertCnfPost(requests, correlator);
            ClockUtil.setCurrentTime(new Date(START_TIME + 61000));
            assertTrue(restored.getDuedate().before(ClockUtil.getCurrentTime()));
            engine.getManagementService().executeJob(restored.getId());

            assertEnded(child.getId());
            assertEnded(parent.getId());
            Object stored = historic(parent.getId(), "WorkflowException");
            assertEquals(WorkflowException.class, stored.getClass());
            WorkflowException error = (WorkflowException) stored;
            assertEquals("ReceiveWorkflowMessage", error.getProcessKey());
            assertEquals(7010, error.getErrorCode());
            assertEquals("Receive Workflow Message Timeout Error", error.getErrorMessage());
            assertNull(historic(parent.getId(), "asyncCallbackResponse"));
            assertActivityCount(child.getId(), "BoundaryEvent_1", 1);
            assertActivityCount(child.getId(), "ScriptTask_setSuccess", 0);
            assertActivityCount(parent.getId(), "Activity_0lttdg6", 1);
            assertActivityCount(parent.getId(), "Activity_1ttieoz", 1);
            assertActivityCount(child.getId(), "EndEvent_8", 1);
            assertActivityCount(parent.getId(), "Event_15f5cnf", 0);
            assertActivityCount(parent.getId(), "Event_1t6t0h6", 1);
            rejectCnfCallback(correlator);
            assertCnfPost(requests, correlator);
            assertEquals(1, requests.getAllServeEvents().size());
        } finally {
            requests.stop();
        }
    }

    @Test
    public void cnfValidationUsesTheProductionErrorSubprocessBeforeCallingHttpOrCreatingAChild() {
        WireMockServer requests = new WireMockServer(0);
        requests.start();
        try (MockedStatic<UrnPropertiesReader> properties = cnfConfiguration("PT1M")) {
            for (String name : new String[] {"apiPath", "cnfRequestPayload", "correlator", "messageType"}) {
                Map<String, Object> input = cnfInputs(requests, "cnf-invalid-" + name);
                input.remove(name);
                ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("CNFAdapterAsyncCall",
                        UUID.randomUUID().toString(), input);
                assertEnded(process.getId());
                Object stored = historic(process.getId(), "WorkflowException");
                assertEquals(WorkflowException.class, stored.getClass());
                WorkflowException error = (WorkflowException) stored;
                assertEquals("CNFAdapterAsyncCall", error.getProcessKey());
                assertEquals(500, error.getErrorCode());
                String label = "apiPath".equals(name) ? "API PATH" : name;
                assertEquals("Cannot process CNF adapter call : " + label + " is null", error.getErrorMessage());
                assertActivityCount(process.getId(), "Event_1t6t0h6", 1);
                assertActivityCount(process.getId(), "Event_15f5cnf", 0);
                assertActivityCount(process.getId(), "Activity_1ttieoz", 0);
                assertEquals(0, engine.getHistoryService().createHistoricProcessInstanceQuery()
                        .superProcessInstanceId(process.getId()).count());
            }
            assertEquals(0, requests.getAllServeEvents().size());
        } finally {
            requests.stop();
        }
    }

    @Test
    public void cnfRejectedHttpAcknowledgementsRetainTheirCodeAndNeverCreateCallbackWaits() {
        WireMockServer requests = new WireMockServer(0);
        requests.start();
        try (MockedStatic<UrnPropertiesReader> properties = cnfConfiguration("PT1M")) {
            for (int status : new int[] {400, 500}) {
                String correlator = "cnf-rejected-" + status;
                stubCnf(requests, correlator, status);
                ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("CNFAdapterAsyncCall",
                        correlator, cnfInputs(requests, correlator));
                assertEnded(process.getId());
                Object stored = historic(process.getId(), "WorkflowException");
                assertEquals(WorkflowException.class, stored.getClass());
                WorkflowException error = (WorkflowException) stored;
                assertEquals("CNFAdapterAsyncCall", error.getProcessKey());
                assertEquals(status, error.getErrorCode());
                assertEquals("Received a Bad Sync Response from CNF.", error.getErrorMessage());
                assertActivityCount(process.getId(), "Event_1t6t0h6", 1);
                assertActivityCount(process.getId(), "Event_15f5cnf", 0);
                assertActivityCount(process.getId(), "Activity_0fhymrt", 0);
                assertEquals(0, engine.getHistoryService().createHistoricProcessInstanceQuery()
                        .superProcessInstanceId(process.getId()).count());
                assertCnfPost(requests, correlator);
            }
            assertEquals(2, requests.getAllServeEvents().size());
        } finally {
            requests.stop();
        }
    }

    private static MockedStatic<UrnPropertiesReader> cnfConfiguration(String timeout) {
        MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS);
        properties.when(
                () -> UrnPropertiesReader.getVariable(eq("mso.adapters.cnf.timeout"), any(DelegateExecution.class)))
                .thenReturn(timeout);
        return properties;
    }

    private static Map<String, Object> cnfInputs(WireMockServer requests, String correlator) {
        Map<String, Object> input = new HashMap<>();
        input.put("apiPath", "http://localhost:" + requests.port() + cnfPath(correlator));
        input.put("cnfRequestPayload", cnfPayload(correlator));
        input.put("correlator", correlator);
        input.put("messageType", "CNF");
        return input;
    }

    private static String cnfPath(String correlator) {
        return "/cnf/requests?instance=" + correlator;
    }

    private static String cnfPayload(String correlator) {
        return "{\"requestId\":\"" + correlator + "\",\"count\":0,\"enabled\":false,\"name\":\"München & 雪\"}";
    }

    private static void stubCnf(WireMockServer requests, String correlator, int status) {
        requests.stubFor(post(urlEqualTo(cnfPath(correlator)))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")));
    }

    private static void assertCnfPost(WireMockServer requests, String correlator) {
        requests.verify(1,
                postRequestedFor(urlEqualTo(cnfPath(correlator)))
                        .withHeader("Content-Type", containing("application/json"))
                        .withRequestBody(equalToJson(cnfPayload(correlator))));
    }

    private void correlateCnf(String correlator, String response) {
        engine.getRuntimeService().createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("CNF_CORRELATOR", correlator).setVariable("CNF_MESSAGE", response)
                .correlate();
    }

    private void rejectCnfCallback(String correlator) {
        try {
            correlateCnf(correlator, "unexpected-cnf-response");
            fail("CNF callback should not correlate: " + correlator);
        } catch (MismatchingMessageCorrelationException expected) {
            assertTrue(expected.getMessage().contains("WorkflowMessage"));
        }
    }

    private Map<String, Object> rainyDayRequest(String requestId, String disposition) {
        Map<String, Object> input = new HashMap<>();
        input.put("msoRequestId", requestId);
        input.put("policy.default.disposition", disposition);
        input.put("serviceType", "Service");
        input.put("vnfType", "router");
        input.put("currentActivity", "allocate");
        input.put("workStep", "create");
        input.put("failedActivity", "Controller");
        input.put("errorCode", "5300");
        input.put("errorText", "東京 & controller failure");
        input.put("requestorId", "operator");
        input.put("isDebugLogEnabled", false);
        return input;
    }

    private static void assertDatabaseUpdates(WireMockServer requests, String requestId, String... statuses)
            throws Exception {
        var calls = requests.findAll(postRequestedFor(urlEqualTo("/requests")));
        assertEquals(statuses.length, calls.size());
        requests.verify(statuses.length,
                postRequestedFor(urlEqualTo("/requests")).withHeader("Content-Type", containing("application/soap+xml"))
                        .withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0")));
        Map<String, Long> expected = new HashMap<>();
        for (String status : statuses) {
            expected.merge(status, 1L, Long::sum);
        }
        Map<String, Long> actual = new HashMap<>();
        for (var call : calls) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(call.getBody()));
            assertEquals("http://schemas.xmlsoap.org/soap/envelope/", document.getDocumentElement().getNamespaceURI());
            assertEquals(1,
                    document.getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateInfraRequest").getLength());
            assertEquals(requestId, document.getElementsByTagName("requestId").item(0).getTextContent());
            assertEquals("ManualHandling", document.getElementsByTagName("lastModifiedBy").item(0).getTextContent());
            actual.merge(document.getElementsByTagName("requestStatus").item(0).getTextContent(), 1L, Long::sum);
        }
        assertEquals(expected, actual);
    }

    private void reopenEngine() {
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration();
        configuration.setProcessEngineName(database);
        configuration.setJdbcUrl(jdbcUrl);
        configuration.setJdbcDriver(maria == null ? "org.h2.Driver" : "org.mariadb.jdbc.Driver");
        configuration.setJdbcUsername(maria == null ? "sa" : "root");
        configuration.setJdbcPassword("");
        configuration.setDatabaseType(maria == null ? "h2" : "mysql");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setJdbcBatchProcessing(false);
        configuration.setEnforceHistoryTimeToLive(false);
        // SO's application.yaml keeps MariaDB's default isolation and disables this check.
        configuration.setSkipIsolationLevelCheck(maria != null);
        configuration.setJavaSerializationFormatEnabled(true);
        configuration.setJobExecutorActivate(false);
        configuration.setProcessEnginePlugins(Collections.singletonList(new ConnectProcessEnginePlugin()));
        configuration.setBeans(Collections.singletonMap("UrnPropertiesReader", new UrnPropertiesReader()));
        engine = configuration.buildProcessEngine();
    }

    private void restart() {
        engine.close();
        engine = null;
        reopenEngine();
    }

    private ProcessInstance start(Map<String, Object> input) {
        return engine.getRuntimeService().startProcessInstanceByKey("ReceiveWorkflowMessage", input);
    }

    private void correlate(String correlator, String response) {
        engine.getRuntimeService().createMessageCorrelation("WorkflowMessage")
                .processInstanceVariableEquals("SDNC_CORRELATOR", correlator).setVariable("SDNC_MESSAGE", response)
                .correlate();
    }

    private void rejectCallback(String correlator) {
        try {
            correlate(correlator, "unexpected-response");
            fail("Callback should not correlate: " + correlator);
        } catch (MismatchingMessageCorrelationException expected) {
            assertTrue(expected.getMessage().contains("WorkflowMessage"));
        }
    }

    private Object historic(String processId, String name) {
        HistoricVariableInstance variable = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processId).variableName(name).singleResult();
        assertNotNull(name, variable);
        return variable.getValue();
    }

    private void assertEnded(String processId) {
        assertEquals(0, engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processId).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processId)
                .finished().count());
    }

    private void assertActivityCount(String processId, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(processId).activityId(activity).finished().count());
    }

    private static Object legacyErrorResponse() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("prefix", "Compat_");
        variables.put("Compat_err", TrinityExceptionUtil.Error.SVC_GENERAL_SERVICE_ERROR);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        new TrinityExceptionUtil().buildException(execution);
        return variables.get("Compat_ErrorResponse");
    }

    private static Map<String, Object> inputs(String correlator) {
        Map<String, Object> input = new HashMap<>();
        input.put("RCVWFMSG_timeout", "PT1M");
        input.put("RCVWFMSG_messageType", "SDNC");
        input.put("RCVWFMSG_correlator", correlator);
        return input;
    }

    private static byte[] productionResource(String name) throws Exception {
        Path location =
                Paths.get(ReceiveWorkflowMessage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location)) {
            return Files.readAllBytes(location.resolve(name));
        }
        try (JarFile jar = new JarFile(location.toFile())) {
            JarEntry entry = jar.getJarEntry(name);
            assertNotNull("Missing packaged production resource " + name, entry);
            try (var input = jar.getInputStream(entry)) {
                return input.readAllBytes();
            }
        }
    }
}
