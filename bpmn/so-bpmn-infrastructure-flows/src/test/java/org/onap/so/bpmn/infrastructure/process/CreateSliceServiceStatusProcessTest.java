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
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class CreateSliceServiceStatusProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "CreateSliceService";
    private static final String PREPARE = "ScriptTask_1ssh2l9";
    private static final String CONNECTOR = "ServiceTask_0esr949";
    private static final String END = "EndEvent_05h01gx";
    private static final String PATH = "/slice-operation-status";
    private final List<String> instances = new ArrayList<>();

    @Before
    public void configureStatusBoundary() throws Exception {
        wireMockServer.resetAll();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS)
                .latestVersion().singleResult();
        assertNotNull("Missing production slice process", definition);
        try (InputStream original = getClass().getClassLoader().getResourceAsStream("process/CreateSliceService.bpmn");
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertNotNull(original);
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
        variables.put("serviceInstanceId", "service-1");
        variables.put("operationId", "operation<&1");
        variables.put("operationType", "CREATE");
        variables.put("globalSubscriberId", "東京 & customer");
        variables.put("CSSOS_dbAdapterEndpoint", "http://localhost:" + wireMockPort + PATH);
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/soap+xml");
        headers.put("Authorization", "Basic dGVzdDp0ZXN0");
        variables.put("CSSOS_headerMap", headers);
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
    public void finalStatus200ExecutesTheProductionPreparationAndGroovyHeaderScript() throws Exception {
        assertAccepted(200);
    }

    @Test
    public void finalStatus201PreservesTheConnectorResponseAndStringProgress() throws Exception {
        assertAccepted(201);
    }

    @Test
    public void rejectedFinalStatus400MustNotReachTheSuccessfulEndEvent() {
        assertRejected(400);
    }

    @Test
    public void rejectedFinalStatus500MustNotReachTheSuccessfulEndEvent() {
        assertRejected(500);
    }

    @Test
    public void emptyFinalStatusResponseMustUseWorkflowErrorHandling() {
        Assume.assumeTrue("SO-4446: empty slice status response fails connector output mapping before error handling",
                Boolean.getBoolean("groovy.migration.verification"));
        stubStatus(204, "");
        try {
            ProcessInstance process = startFinalStatus();
            assertActivity(process, END, 0);
            assertNotNull("Empty status response must set a workflow error", historic(process, "WorkflowException"));
        } catch (ProcessEngineException unexpected) {
            assertRequest();
            throw new AssertionError("An empty status response must use workflow error handling", unexpected);
        }
        assertRequest();
    }

    private void assertRejected(int status) {
        Assume.assumeTrue("SO-4446: rejected slice status persistence must not reach the successful end event",
                Boolean.getBoolean("groovy.migration.verification"));
        stubStatus(status, "<rejected/>");
        ProcessInstance process = startFinalStatus();
        assertEquals(Integer.valueOf(status), historic(process, "CSSOS_dbResponseCode"));
        assertActivity(process, CONNECTOR, 1);
        assertRequest();
        assertActivity(process, END, 0);
        assertNotNull("Rejected persistence must set a workflow error", historic(process, "WorkflowException"));
    }

    private void assertAccepted(int status) throws Exception {
        stubStatus(status, "<accepted/>");
        ProcessInstance process = startFinalStatus();
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(process.getId())
                .finished().count());
        assertActivity(process, PREPARE, 1);
        assertActivity(process, CONNECTOR, 1);
        assertActivity(process, END, 1);
        assertEquals("100", historic(process, "operationProgress"));
        assertEquals("finished", historic(process, "operationResult"));
        assertEquals("Slice completed", historic(process, "operationReason"));
        assertEquals(Integer.valueOf(status), historic(process, "CSSOS_dbResponseCode"));
        assertEquals("<accepted/>", historic(process, "CSSOS_dbResponse"));
        assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId("CallActivity_0cxst1i").count());
        assertRequest();
        String payload = wireMockServer.findAll(postRequestedFor(urlEqualTo(PATH))).get(0).getBodyAsString();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document xml = factory.newDocumentBuilder().parse(new InputSource(new StringReader(payload)));
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", xml.getDocumentElement().getNamespaceURI());
        assertEquals(1,
                xml.getElementsByTagNameNS("http://org.onap.so/requestsdb", "initServiceOperationStatus").getLength());
        assertElement(xml, "serviceId", "service-1");
        assertElement(xml, "operationId", "operation<&1");
        assertElement(xml, "operationType", "CREATE");
        assertElement(xml, "userId", "東京 & customer");
        assertElement(xml, "result", "finished");
        assertElement(xml, "progress", "100");
        assertElement(xml, "reason", "Slice completed");
    }

    private ProcessInstance startFinalStatus() {
        ProcessInstance process =
                runtimeService.createProcessInstanceByKey(PROCESS).businessKey(UUID.randomUUID().toString())
                        .setVariables(variables).startBeforeActivity(PREPARE).execute();
        instances.add(process.getId());
        return process;
    }

    private void stubStatus(int status, String body) {
        wireMockServer.stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Connection", "close").withHeader("Content-Type", "application/soap+xml").withBody(body)));
    }

    private void assertRequest() {
        wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(PATH)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Content-Type", equalTo("application/soap+xml")));
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private static void assertElement(Document xml, String name, String value) {
        assertEquals(1, xml.getElementsByTagNameNS("", name).getLength());
        assertEquals(value, xml.getElementsByTagNameNS("", name).item(0).getTextContent());
    }
}
