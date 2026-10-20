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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.w3c.dom.Document;


public class CloudLeasedLineDeleteProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoCloudLeasedLineDelete";
    private static final String RESOURCE = "subprocess/DoCloudLeasedLineDelete.bpmn";
    private static final String STATUS_PATH = "/cll-status";
    private static final String BUSINESS_PATH = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/";
    private String cllId;
    private String resourcePath;

    @Before
    public void prepareProcessContracts() throws Exception {
        assertProductionResource();
        wireMockServer.resetAll();
        variables.clear();
        cllId = "cll-" + UUID.randomUUID();
        resourcePath = "/aai/v[0-9]+" + BUSINESS_PATH + cllId;
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceID", cllId);
        variables.put("servicename", "leased Gr\u00fc\u00dfe & SO");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("modelUuid", "model-1");
        variables.put("nsiId", "nsi-1");
        variables.put("jobId", "job-1");
        variables.put("subscriptionServiceType", "service-type");
        variables.put("serviceType", "leased-line");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("serviceIntentParams", "{\"enableSdnc\":false}");
        variables.put("mso.workflow.sdncadapter.callback", "http://localhost:" + wireMockPort + "/sdnc-callback");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS_PATH);
        stubRead(200);
        stubDelete(204);
        stubStatusUpdate(200);
    }

    @Test
    public void disabledSdncDeletesTheVersionedResourceAndRecordsCompletion() throws Exception {
        ProcessInstance process = start();

        assertNull(historic(process, "WorkflowException"));
        assertEquals("DCLL_", historic(process, "prefix"));
        assertEquals("false", historic(process, "enableSdnc"));
        assertSame(Boolean.TRUE, historic(process, "isDebugLogEnabled"));
        assertTrue(historic(process, "startTime") instanceof Long);
        assertEquals(cllId, historic(process, "cllId"));
        assertEquals("leased Gr\u00fc\u00dfe & SO", historic(process, "cllName"));
        assertEquals(Integer.valueOf(200), historic(process, "NSSMF_dbResponseCode"));
        assertEquals("model-1", new JSONObject((String) historic(process, "serviceModelInfo")).getString("modelUuid"));
        assertActivity(process, "ScriptTask_1tc44ge", 1);
        assertActivity(process, "Activity_013rjwc", 1);
        assertActivity(process, "ScriptTask_1ssh2l9", 1);
        assertActivity(process, "Activity_0rgeefb", 1);
        assertActivity(process, "EndEvent_05h01gx", 1);
        assertActivity(process, "ScriptTask_1swzdpw", 0);
        assertActivity(process, "Activity_1tw8eyy", 0);
        assertActivity(process, "Activity_0p20esb", 0);
        assertDeleteCall();
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)).withHeader("Content-Type",
                containing("application/soap+xml")));
        Document payload =
                xml(wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS_PATH))).get(0).getBodyAsString());
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", payload.getDocumentElement().getNamespaceURI());
        assertEquals(1, payload.getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateResourceOperationStatus")
                .getLength());
        assertEquals("nsi-1", text(payload, "serviceId"));
        assertEquals("job-1", text(payload, "operationId"));
        assertEquals("model-1", text(payload, "resourceTemplateUUID"));
        assertEquals(cllId, text(payload, "resourceInstanceID"));
        assertEquals("DELETE", text(payload, "operType"));
        assertEquals("FINISHED", text(payload, "status"));
        assertEquals("100", text(payload, "progress"));
        assertEquals(3, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void rejectedDeletionDoesNotRecordSuccessfulCompletion() {
        stubDelete(503);
        ProcessInstance process = start();
        assertDeleteCall();
        assertFailed(process);
        assertEquals(2, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void missingInventoryStopsBeforeDeletionAndCompletion() {
        stubRead(404);
        ProcessInstance process = start();
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(resourcePath)));
        assertFailed(process);
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void missingCallbackStopsBeforeExternalMutation() {
        variables.put("mso.workflow.sdncadapter.callback", "");
        ProcessInstance process = start();
        assertFailed(process);
        assertActivity(process, "Activity_013rjwc", 0);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    private void assertFailed(ProcessInstance process) {
        assertNotNull(historic(process, "WorkflowException"));
        assertActivity(process, "ScriptTask_1swzdpw", 1);
        assertActivity(process, "Activity_1tw8eyy", 0);
        assertActivity(process, "Activity_0p20esb", 0);
        assertActivity(process, "ScriptTask_1ssh2l9", 0);
        assertActivity(process, "Activity_0rgeefb", 0);
        assertActivity(process, "EndEvent_05h01gx", 0);
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(STATUS_PATH)));
    }

    private void assertDeleteCall() {
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(resourcePath))
                .withQueryParam("resource-version", equalTo("version-7")).withHeader("X-FromAppId", equalTo("MSO")));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath)));
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void stubRead(int status) {
        wireMockServer.stubFor(get(urlPathMatching(resourcePath))
                .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "application/json")
                        .withBody(status == 200
                                ? "{\"service-instance-id\":\"" + cllId + "\",\"resource-version\":\"version-7\"}"
                                : "{}")));
    }

    private void stubDelete(int status) {
        wireMockServer.stubFor(delete(urlPathMatching(resourcePath)).willReturn(aResponse().withStatus(status)));
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

    private void stubStatusUpdate(int status) {
        wireMockServer.stubFor(post(urlEqualTo(STATUS_PATH))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")
                        .withHeader("Content-Type", "application/soap+xml").withBody("<updated/>")));
    }

    private void assertProductionResource() throws Exception {
        URL resource = getClass().getClassLoader().getResource(RESOURCE);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + RESOURCE)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS)
                .latestVersion().singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals("The engine must execute the packaged production BPMN", original.readAllBytes(),
                    deployed.readAllBytes());
        }
    }

    private String text(Document document, String localName) {
        assertEquals(localName, 1, document.getElementsByTagNameNS("", localName).getLength());
        return document.getElementsByTagNameNS("", localName).item(0).getTextContent();
    }

    private Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
