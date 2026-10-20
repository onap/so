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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
    private ProcessEngine engine;
    private String database;
    private String jdbcUrl;

    @BeforeClass
    public static void configureBackendAndProductionResource() throws Exception {
        String backend = System.getProperty("groovy.migration.persistence.backend", "h2");
        assertTrue("Expected h2 or mariadb persistence backend", Arrays.asList("h2", "mariadb").contains(backend));
        productionBpmn = productionResource("subprocess/ReceiveWorkflowMessage.bpmn");
        manualHandlingBpmn = productionResource("subprocess/BuildingBlock/ManualHandling.bpmn");
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
                .addInputStream("ManualHandling.bpmn", new ByteArrayInputStream(manualHandlingBpmn)).deploy();
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
