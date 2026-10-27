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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.scripts.CompleteMsoProcess;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class CompleteMsoProcessContractTest {
    private static final String PROCESS = "CompleteMsoProcess";
    private static final String PATH = "/infraActiveRequests/request-1";
    private static final String[] RESOURCE_IDS =
            {"vnfId", "networkId", "configurationId", "serviceInstanceId", "vfModuleId", "volumeGroupId", "pnfName"};
    private static ProcessEngine engine;
    @Rule
    public WireMockRule requestsDb = new WireMockRule(0);

    @BeforeClass
    public static void deployProductionProcess() throws Exception {
        String name = "completion-contract-" + UUID.randomUUID();
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJavaSerializationFormatEnabled(true);
        configuration.setJobExecutorActivate(false);
        configuration.getProcessEnginePlugins().add(new ConnectProcessEnginePlugin());
        engine = configuration.buildProcessEngine();
        byte[] production = productionResource();
        engine.getRepositoryService().createDeployment()
                .addInputStream("CompleteMsoProcess.bpmn", new ByteArrayInputStream(production)).deploy();
        ProcessDefinition definition = engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS).singleResult();
        try (InputStream deployed = engine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(),
                definition.getResourceName())) {
            assertArrayEquals(production, deployed.readAllBytes());
        }
    }

    @AfterClass
    public static void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void successfulCompletionPreservesExistingMetadataAndWritesTypedProgress() throws Exception {
        for (int status : new int[] {200, 201, 204}) {
            requestsDb.resetAll();
            existingRequest();
            acceptUpdate(status);
            ProcessInstance process =
                    start(request("<w:status-message>東京 &amp; &lt;controller&gt;</w:status-message>"));
            assertSuccess(process);
            JsonNode update = assertUpdate("東京 & <controller>");
            assertEquals("service-before", update.path("serviceInstanceId").asText());
            assertEquals("VID", update.path("source").asText());
            assertEquals("keep-name", update.path("vnfName").asText());
            assertHttpActions(1, 1);
        }
    }

    @Test
    public void eachSupportedResourceIdentifierIsCopiedWithoutDiscardingUnrelatedState() throws Exception {
        for (String resource : RESOURCE_IDS) {
            requestsDb.resetAll();
            existingRequest();
            acceptUpdate(204);
            ProcessInstance process = start(request("<w:" + resource + ">resource-1</w:" + resource + ">"));
            assertSuccess(process);
            JsonNode update = assertUpdate("Resource Completed Successfully");
            assertEquals(resource, "resource-1", update.path(resource).asText());
            assertEquals("serviceInstanceId".equals(resource) ? "resource-1" : "service-before",
                    update.path("serviceInstanceId").asText());
            assertEquals("VID", update.path("source").asText());
            assertHttpActions(1, 1);
        }
    }

    @Test
    public void multipleResourceFieldsRetainTheExistingVnfFirstSelectionContract() throws Exception {
        existingRequest();
        acceptUpdate(204);
        ProcessInstance process = start(request("<w:vnfId>vnf-selected</w:vnfId>"
                + "<w:networkId>network-not-selected</w:networkId><w:serviceInstanceId>service-not-selected</w:serviceInstanceId>"));
        assertSuccess(process);
        JsonNode update = assertUpdate("Resource Completed Successfully");
        assertEquals("vnf-selected", update.path("vnfId").asText());
        assertTrue(update.path("networkId").isMissingNode() || update.path("networkId").isNull());
        assertEquals("service-before", update.path("serviceInstanceId").asText());
        assertHttpActions(1, 1);
    }

    @Test
    public void explicitEmptyStatusTextIsDistinctFromAnOmittedStatusText() throws Exception {
        existingRequest();
        acceptUpdate(204);
        ProcessInstance process = start(request("<w:status-message/>"));
        assertSuccess(process);
        assertUpdate("");
        assertHttpActions(1, 1);
    }

    @Test
    public void aMissingRequestRecordCreatesTheCompletionRecordWithTheOriginalRequestId() throws Exception {
        requestsDb.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(404)));
        acceptUpdate(204);
        ProcessInstance process = start(request("<w:pnfName>pnf-1</w:pnfName>"));
        assertSuccess(process);
        JsonNode update = assertUpdate("Resource Completed Successfully");
        assertEquals("pnf-1", update.path("pnfName").asText());
        assertTrue(update.path("source").isMissingNode() || update.path("source").isNull());
        assertHttpActions(1, 1);
    }

    @Test
    public void emptySuccessfulLookupAlsoBuildsANewCompletionRecord() throws Exception {
        requestsDb.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(204)));
        acceptUpdate(204);
        ProcessInstance process = start(request("<w:volumeGroupId>volume-1</w:volumeGroupId>"));
        assertSuccess(process);
        assertEquals("volume-1", assertUpdate("Resource Completed Successfully").path("volumeGroupId").asText());
        assertHttpActions(1, 1);
    }

    @Test
    public void unsupportedRequestInformationUsesError500BeforeAnyDatabaseAction() {
        ProcessInstance process = start(request("<w:request-information/>"));
        assertError(process, 500, "does not support this request type", false);
        assertHttpActions(0, 0);
    }

    @Test
    public void missingRequestIdCannotReadOrOverwriteAnUnidentifiedRequest() {
        assertInvalidIdentifier(request("").replace("<w:request-id>request-1</w:request-id>", ""));
    }

    @Test
    public void emptyRequestIdCannotReadOrOverwriteAnUnidentifiedRequest() {
        assertInvalidIdentifier(request("").replace("<w:request-id>request-1</w:request-id>", "<w:request-id/>"));
    }

    @Test
    public void malformedXmlUsesPreprocessingError2000BeforeAnyDatabaseAction() {
        ProcessInstance process = start("<invalid>");
        assertError(process, 2000, "preprocess", false);
        assertHttpActions(0, 0);
    }

    @Test
    public void failedLookupCannotWriteOrCompleteTheRequest() {
        for (int status : new int[] {400, 503}) {
            requestsDb.resetAll();
            requestsDb.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status)));
            ProcessInstance process = start(request(""));
            assertError(process, 2000, "update infra request db", true);
            assertHttpActions(1, 0);
        }
    }

    @Test
    public void malformedSuccessfulLookupCannotWriteOrCompleteTheRequest() {
        requestsDb.stubFor(get(urlEqualTo(PATH)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("not-json")));
        ProcessInstance process = start(request(""));
        assertError(process, 2000, "update infra request db", true);
        assertHttpActions(1, 0);
    }

    @Test
    public void failedStatusWriteUsesTheProductionErrorRouteAfterExactlyOneUpdate() {
        existingRequest();
        acceptUpdate(500);
        ProcessInstance process = start(request(""));
        assertError(process, 2000, "update infra request db", true);
        assertHttpActions(1, 1);
    }

    private String request(String fields) {
        return "<w:MsoCompletionRequest xmlns:w='http://org.onap/so/workflow/schema/v1'>"
                + "<w:request-info><w:request-id>request-1</w:request-id></w:request-info>"
                + "<w:mso-bpel-name>CreateVnf &amp; 東京</w:mso-bpel-name>" + fields + "</w:MsoCompletionRequest>";
    }

    private void assertInvalidIdentifier(String request) {
        Assume.assumeTrue("SO-4446: completion accesses the database with a missing or empty request ID",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = start(request);
        assertEnded(process);
        assertTrue(historic(process, "WorkflowException") instanceof WorkflowException);
        assertActivity(process, "EndEvent_1", 0);
        assertEquals("Missing or empty request IDs must be rejected before accessing the database", 0,
                requestsDb.getAllServeEvents().size());
    }

    private ProcessInstance start(String request) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("CompleteMsoProcessRequest", request);
        try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class)) {
            properties.when(() -> UrnPropertiesReader.getSoBasicAuthHeader(any(DelegateExecution.class)))
                    .thenReturn("Basic test");
            properties.when(() -> UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint"))
                    .thenReturn("http://localhost:" + requestsDb.port());
            return engine.getRuntimeService().startProcessInstanceByMessage("CompleteMsoProcessRequest",
                    UUID.randomUUID().toString(), inputs);
        }
    }

    private void existingRequest() {
        requestsDb.stubFor(get(urlEqualTo(PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
                        "{\"requestStatus\":\"IN_PROGRESS\",\"progress\":42,\"serviceInstanceId\":\"service-before\","
                                + "\"source\":\"VID\",\"vnfName\":\"keep-name\"}")));
    }

    private void acceptUpdate(int status) {
        requestsDb.stubFor(put(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status)));
    }

    private JsonNode assertUpdate(String message) throws Exception {
        JsonNode update = new ObjectMapper()
                .readTree(requestsDb.findAll(putRequestedFor(urlEqualTo(PATH))).get(0).getBodyAsString());
        assertEquals("request-1", update.path("requestId").asText());
        assertEquals("COMPLETE", update.path("requestStatus").asText());
        assertEquals("BPMN", update.path("lastModifiedBy").asText());
        assertEquals(message, update.path("statusMessage").asText());
        assertTrue(update.path("progress").isIntegralNumber());
        assertEquals(100, update.path("progress").asInt());
        return update;
    }

    private void assertHttpActions(int reads, int writes) {
        requestsDb.verify(reads, getRequestedFor(urlEqualTo(PATH)).withHeader("Authorization", equalTo("Basic test"))
                .withHeader("Accept", containing("application/json")));
        requestsDb.verify(writes, putRequestedFor(urlEqualTo(PATH)).withHeader("Authorization", equalTo("Basic test"))
                .withHeader("Content-Type", containing("application/json")));
        assertEquals(reads + writes, requestsDb.getAllServeEvents().size());
    }

    private void assertSuccess(ProcessInstance process) {
        assertEnded(process);
        assertEquals("request-1", historic(process, "CMSO_request_id"));
        assertEquals("CreateVnf & 東京", historic(process, "CMSO_mso-bpel-name"));
        assertNull(historic(process, "WorkflowException"));
        assertActivity(process, "preProcessRequest", 1);
        assertActivity(process, "ScriptTask_3", 1);
        assertActivity(process, "EndEvent_1", 1);
        assertActivity(process, "endEventExceptionCompleteMSO", 0);
    }

    private void assertError(ProcessInstance process, int code, String message, boolean updateAttempted) {
        assertEnded(process);
        Object stored = historic(process, "WorkflowException");
        assertTrue(stored instanceof WorkflowException);
        WorkflowException failure = (WorkflowException) stored;
        assertEquals(PROCESS, failure.getProcessKey());
        assertEquals(code, failure.getErrorCode());
        assertTrue(failure.getErrorMessage().contains(message));
        assertActivity(process, "endEventExceptionCompleteMSO", 1);
        assertActivity(process, "EndEvent_1", 0);
        assertActivity(process, "ScriptTask_3", updateAttempted ? 1 : 0);
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

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance variable = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private static byte[] productionResource() throws Exception {
        String name = "subprocess/CompleteMsoProcess.bpmn";
        Path location = Paths.get(CompleteMsoProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location)) {
            return Files.readAllBytes(location.resolve(name));
        }
        try (JarFile jar = new JarFile(location.toFile())) {
            assertNotNull(name, jar.getJarEntry(name));
            try (InputStream input = jar.getInputStream(jar.getJarEntry(name))) {
                return input.readAllBytes();
            }
        }
    }
}
