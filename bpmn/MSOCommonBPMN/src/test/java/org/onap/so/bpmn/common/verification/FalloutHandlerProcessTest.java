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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.scripts.FalloutHandler;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class FalloutHandlerProcessTest {
    private static final String REQUEST_PATH = "/infraActiveRequests/request-1";
    private static final String WORKFLOW_NS = "http://org.onap/so/workflow/schema/v1";
    private static ProcessEngine engine;

    @Rule
    public WireMockRule requestsDb = new WireMockRule(0);

    @BeforeClass
    public static void deployProductionProcess() throws Exception {
        String name = "fallout-contract-" + UUID.randomUUID();
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJavaSerializationFormatEnabled(true);
        configuration.setJobExecutorActivate(false);
        engine = configuration.buildProcessEngine();
        engine.getRepositoryService().createDeployment()
                .addInputStream("FalloutHandler.bpmn", new ByteArrayInputStream(productionResource())).deploy();
    }

    @AfterClass
    public static void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void successfulFailureRecordingPreservesIdentityAndWritesTheFailureContract() throws Exception {
        existingRequest();
        requestsDb.stubFor(put(urlEqualTo(REQUEST_PATH)).willReturn(aResponse().withStatus(204)));

        ProcessInstance process = start(true);

        assertOutcome(process, true);
        requestsDb.verify(1,
                getRequestedFor(urlEqualTo(REQUEST_PATH)).withHeader("Authorization", equalTo("Basic test"))
                        .withHeader("Accept", containing("application/json")));
        requestsDb.verify(1,
                putRequestedFor(urlEqualTo(REQUEST_PATH)).withHeader("Authorization", equalTo("Basic test"))
                        .withHeader("Content-Type", containing("application/json")));
        JsonNode update = updateBody();
        assertEquals("request-1", update.path("requestId").asText());
        assertEquals("service-1", update.path("serviceInstanceId").asText());
        assertEquals("FAILED", update.path("requestStatus").asText());
        assertEquals("BPMN", update.path("lastModifiedBy").asText());
        assertTrue(update.path("progress").isIntegralNumber());
        assertEquals(100, update.path("progress").asInt());
        assertEquals("Grüße &amp; &lt;controller&gt;", update.path("statusMessage").asText());
        assertEquals("5300", historic(process, "FH_ErrorCode"));
        assertEquals("Grüße & <controller>", historic(process, "FH_ErrorMessage"));
    }

    @Test
    public void failedRequestLookupProducesAFailedHandlerResponseWithoutAnUpdate() throws Exception {
        requestsDb.stubFor(get(urlEqualTo(REQUEST_PATH)).willReturn(aResponse().withStatus(503)));

        ProcessInstance process = start(true);

        assertOutcome(process, false);
        requestsDb.verify(1, getRequestedFor(urlEqualTo(REQUEST_PATH)));
        requestsDb.verify(0, putRequestedFor(urlEqualTo(REQUEST_PATH)));
    }

    @Test
    public void failedStatusWriteCannotBeReportedAsSuccessfulFailureHandling() throws Exception {
        existingRequest();
        requestsDb.stubFor(put(urlEqualTo(REQUEST_PATH)).willReturn(aResponse().withStatus(500)));

        ProcessInstance process = start(true);

        assertOutcome(process, false);
        requestsDb.verify(1, getRequestedFor(urlEqualTo(REQUEST_PATH)));
        requestsDb.verify(1, putRequestedFor(urlEqualTo(REQUEST_PATH)));
    }

    @Test
    public void malformedSuccessfulLookupResponseFailsBeforeWritingTheRequest() throws Exception {
        requestsDb.stubFor(get(urlEqualTo(REQUEST_PATH)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("not-json")));

        ProcessInstance process = start(true);

        assertOutcome(process, false);
        requestsDb.verify(1, getRequestedFor(urlEqualTo(REQUEST_PATH)));
        requestsDb.verify(0, putRequestedFor(urlEqualTo(REQUEST_PATH)));
    }

    @Test
    public void omittedErrorFieldsUseTheDefinedFallbackCodeAndMessage() throws Exception {
        existingRequest();
        requestsDb.stubFor(put(urlEqualTo(REQUEST_PATH)).willReturn(aResponse().withStatus(204)));

        ProcessInstance process = start(false);

        assertOutcome(process, true);
        assertEquals("500", historic(process, "FH_ErrorCode"));
        assertEquals("Internal Error occured in MSO, unable to determine error message",
                historic(process, "FH_ErrorMessage"));
        assertEquals(historic(process, "FH_ErrorMessage"), updateBody().path("statusMessage").asText());
    }

    private void existingRequest() {
        requestsDb.stubFor(get(urlEqualTo(REQUEST_PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
                        "{\"serviceInstanceId\":\"service-1\",\"requestStatus\":\"IN_PROGRESS\",\"progress\":42}")));
    }

    private ProcessInstance start(boolean errorDetails) {
        Map<String, Object> input = new HashMap<>();
        input.put("prefix", "FH_");
        input.put("WorkflowException", new WorkflowException("CreateService", 5300, "Original failure", "activate"));
        String details = errorDetails
                ? "<w:ErrorCode>5300</w:ErrorCode><w:ErrorMessage>Grüße &amp; &lt;controller&gt;</w:ErrorMessage>"
                : "";
        input.put("FalloutHandlerRequest",
                "<w:FalloutHandlerRequest xmlns:w='" + WORKFLOW_NS
                        + "'><w:request-info><w:request-id>request-1</w:request-id></w:request-info>" + details
                        + "</w:FalloutHandlerRequest>");
        try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class)) {
            properties.when(() -> UrnPropertiesReader.getVariable("mso.adapters.requestDb.auth"))
                    .thenReturn("Basic test");
            properties.when(() -> UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint"))
                    .thenReturn("http://localhost:" + requestsDb.port());
            return engine.getRuntimeService().startProcessInstanceByKey("FalloutHandler", input);
        }
    }

    private void assertOutcome(ProcessInstance process, boolean success) throws Exception {
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
        for (String activity : new String[] {"ScriptTask_8", "ScriptTask_6", "ScriptTask_2", "EndEvent_6"}) {
            assertEquals(activity, 1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(process.getId()).activityId(activity).finished().count());
        }
        assertEquals(0, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId("ScriptTask_4").count());
        assertEquals(Boolean.valueOf(success), historic(process, "FH_success"));
        assertEquals(Boolean.valueOf(success), historic(process, "FH_SuccessIndicator"));
        assertEquals(success ? "200" : "500", historic(process, "FH_ResponseCode"));
        assertEquals(historic(process, "FalloutHandlerResponse"), historic(process, "WorkflowResponse"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document response = factory.newDocumentBuilder().parse(new ByteArrayInputStream(
                ((String) historic(process, "FalloutHandlerResponse")).getBytes(StandardCharsets.UTF_8)));
        assertEquals(WORKFLOW_NS, response.getDocumentElement().getNamespaceURI());
        assertEquals(success ? "Fallout Handler Succeeded" : "Fallout Handler Failed",
                response.getElementsByTagNameNS(WORKFLOW_NS, "out").item(0).getTextContent());
        Object stored = historic(process, "WorkflowException");
        assertTrue(stored instanceof WorkflowException);
        WorkflowException original = (WorkflowException) stored;
        assertEquals("CreateService", original.getProcessKey());
        assertEquals(5300, original.getErrorCode());
        assertEquals("Original failure", original.getErrorMessage());
        assertEquals("activate", original.getWorkStep());
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        assertNotNull(name, value);
        return value.getValue();
    }

    private JsonNode updateBody() throws Exception {
        return new ObjectMapper()
                .readTree(requestsDb.findAll(putRequestedFor(urlEqualTo(REQUEST_PATH))).get(0).getBodyAsString());
    }

    private static byte[] productionResource() throws Exception {
        String name = "subprocess/FalloutHandler.bpmn";
        Path location = Paths.get(FalloutHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
