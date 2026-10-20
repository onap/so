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
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class TnTransportLifecycleProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoActivateTransportNSSI";
    private static final String RESOURCE = "subprocess/DoActivateTransportNSSI.bpmn";
    private static final String STATUS_PATH = "/tn-status";
    private static final String BUSINESS_PATH = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/";
    private String nssiId;
    private String resourcePath;
    private String nodesPath;

    @Before
    public void prepareProcessContracts() throws Exception {
        assertProductionResource();
        wireMockServer.resetAll();
        variables.clear();
        nssiId = "tn-nssi-" + UUID.randomUUID();
        resourcePath = "/aai/v[0-9]+" + BUSINESS_PATH + nssiId;
        nodesPath = "/aai/v[0-9]+/nodes/service-instances/service-instance/" + nssiId;
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceID", nssiId);
        variables.put("servicename", "transport Gr\u00fc\u00dfe & SO");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("modelUuid", "model-1");
        variables.put("nsiId", "nsi-1");
        variables.put("jobId", "job-1");
        variables.put("subscriptionServiceType", "service-type");
        variables.put("serviceType", "transport");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("sliceParams", "{\"enableSdnc\":false}");
        variables.put("mso.workflow.sdncadapter.callback", "http://localhost:" + wireMockPort + "/sdnc-callback");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS_PATH);
        String link = "http://localhost:" + wireMockPort + "/aai/" + AAIVersion.LATEST + BUSINESS_PATH + nssiId;
        JSONObject node = new JSONObject().put("resource-type", "service-instance").put("resource-link", link);
        wireMockServer.stubFor(get(urlPathMatching(nodesPath)).withQueryParam("format", equalTo("pathed"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(new JSONObject().put("results", new JSONArray().put(node)).toString())));
        stubAaiUpdate(200);
        stubStatusUpdate(200);
    }

    @Test
    public void activationWithSdncDisabledUpdatesAaiAndRecordsTheFinishedJob() throws Exception {
        assertSuccessful("activateInstance", "activate", "activated");
    }

    @Test
    public void deactivationWithSdncDisabledUpdatesAaiAndRecordsTheFinishedJob() throws Exception {
        assertSuccessful("deactivateInstance", "deactivate", "deactivated");
    }

    @Test
    public void missingCallbackRetainsItsBusinessErrorThroughTheProductionErrorHandler() {
        Assume.assumeTrue("SO-4446: preserve the TN callback validation error through the production error subprocess",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("mso.workflow.sdncadapter.callback", "");

        ProcessInstance process = start("activateInstance");

        assertActivity(process, "ScriptTask_1swzdpw", 1);
        assertActivity(process, "ScriptTask_19uxoi8", 0);
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(STATUS_PATH)));
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertNotNull(error);
        assertEquals(500, error.getErrorCode());
        assertEquals("mso.workflow.sdncadapter.callback is null", error.getErrorMessage());
    }

    @Test
    public void aaiUpdateFailureRetainsItsOriginalErrorAndPreventsACompletedStatusWrite() {
        Assume.assumeTrue("SO-4446: preserve the TN AAI error through the production error subprocess",
                Boolean.getBoolean("groovy.migration.verification"));
        stubAaiUpdate(503);

        ProcessInstance process = start("activateInstance");

        assertActivity(process, "ScriptTask_19uxoi8", 1);
        assertActivity(process, "ScriptTask_1swzdpw", 1);
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(STATUS_PATH)));
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertNotNull(error);
        assertEquals(7000, error.getErrorCode());
    }

    @Test
    public void rejectedStatusWriteUsesTheWorkflowErrorRoute() {
        Assume.assumeTrue("SO-4446: a rejected TN status write must not complete without a workflow error",
                Boolean.getBoolean("groovy.migration.verification"));
        stubStatusUpdate(500);

        ProcessInstance process = start("activateInstance");

        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)));
        assertEquals(Integer.valueOf(500), historic(process, "NSSMF_dbResponseCode"));
        assertNotNull("The rejected status write must remain visible as a workflow failure",
                historic(process, "WorkflowException"));
        assertActivity(process, "ScriptTask_1swzdpw", 1);
        assertActivity(process, "EndEvent_05h01gx", 0);
    }

    private void assertSuccessful(String operation, String action, String status) throws Exception {
        ProcessInstance process = start(operation);

        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertNull(historic(process, "WorkflowException"));
        assertEquals("TNACT_", historic(process, "prefix"));
        assertEquals("false", historic(process, "enableSdnc"));
        assertSame(Boolean.TRUE, historic(process, "isDebugLogEnabled"));
        assertTrue(historic(process, "startTime") instanceof Long);
        assertEquals(action, historic(process, "actionType"));
        assertEquals(status, historic(process, "orchestrationStatus"));
        assertEquals(Integer.valueOf(200), historic(process, "NSSMF_dbResponseCode"));
        assertActivity(process, "ScriptTask_1tc44ge", 1);
        assertActivity(process, "ScriptTask_19uxoi8", 1);
        assertActivity(process, "ScriptTask_1ssh2l9", 1);
        assertActivity(process, "Activity_0nhxd67", 1);
        assertActivity(process, "EndEvent_05h01gx", 1);
        assertActivity(process, "Activity_1tw8eyy", 0);
        assertActivity(process, "Activity_0p20esb", 0);
        wireMockServer.verify(1,
                getRequestedFor(urlPathMatching(nodesPath)).withQueryParam("format", equalTo("pathed")));
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath))
                .withRequestBody(matchingJsonPath("$['orchestration-status']", equalTo(status))));
        List<LoggedRequest> updates = wireMockServer.findAll(postRequestedFor(urlPathMatching(resourcePath)));
        assertEquals("PATCH", updates.get(0).getHeader("X-HTTP-Method-Override"));
        assertTrue(updates.get(0).getHeader("Content-Type").contains("application/merge-patch+json"));
        assertEquals(status, new JSONObject(updates.get(0).getBodyAsString()).getString("orchestration-status"));
        wireMockServer.verify(1,
                postRequestedFor(urlEqualTo(STATUS_PATH)).withHeader("Content-Type", containing("application/soap+xml"))
                        .withHeader("Authorization", containing("Basic ")));
        Document payload =
                xml(wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS_PATH))).get(0).getBodyAsString());
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", payload.getDocumentElement().getNamespaceURI());
        assertEquals(1, payload.getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateResourceOperationStatus")
                .getLength());
        assertEquals("nsi-1", text(payload, "serviceId"));
        assertEquals("job-1", text(payload, "operationId"));
        assertEquals("model-1", text(payload, "resourceTemplateUUID"));
        assertEquals(nssiId, text(payload, "resourceInstanceID"));
        assertEquals(action.toUpperCase(Locale.ROOT), text(payload, "operType"));
        assertEquals("FINISHED", text(payload, "status"));
        assertEquals("100", text(payload, "progress"));
    }

    private ProcessInstance start(String operation) {
        variables.put("operationType", operation);
        return runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
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

    private void stubAaiUpdate(int status) {
        wireMockServer
                .stubFor(post(urlPathMatching(resourcePath)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                        .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "application/json")
                                .withBody(status == 200 ? "{}" : "{\"message\":\"AAI unavailable\"}")));
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
