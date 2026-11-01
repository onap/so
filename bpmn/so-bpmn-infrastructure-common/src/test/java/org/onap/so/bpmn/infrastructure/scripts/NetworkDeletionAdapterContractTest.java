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
package org.onap.so.bpmn.infrastructure.scripts;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.aai.domain.yang.L3Network;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class NetworkDeletionAdapterContractTest {
    private static final String PREFIX = "DELNWKI_";
    private final DoDeleteNetworkInstance script = new DoDeleteNetworkInstance();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final L3Network network = new L3Network();
    @Rule
    public WireMockRule adapter = new WireMockRule(0);

    @Before
    public void configureInventoryAndAdapterBoundary() {
        network.setNetworkId("network-1");
        network.setNetworkType("private & shared");
        network.setHeatStackId("stack-1");
        variables.put("testProcessKey", "DoDeleteNetworkInstance");
        variables.put(PREFIX + "queryAAIResponse", network);
        variables.put(PREFIX + "networkRequest",
                "<network-request><networkModelInfo>"
                        + "<modelCustomizationUuid>model &amp; \u96ea</modelCustomizationUuid>"
                        + "</networkModelInfo></network-request>");
        variables.put(PREFIX + "cloudRegionPo", "region & \u96ea");
        variables.put(PREFIX + "tenantId", "tenant <one>");
        variables.put(PREFIX + "messageId", "message-1");
        variables.put("msoRequestId", "request & one");
        variables.put("serviceInstanceId", "service <one>");
        variables.put("mso.adapters.network.rest.endpoint", "http://localhost:" + adapter.port() + "/networks");
        variables.put("BasicAuthHeaderValuePO", "Basic dGVzdDp0ZXN0");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void adapterPayloadPreservesEscapedIdentityAndModelFields() throws Exception {
        script.prepareNetworkRequest(execution);
        assertEquals(PREFIX, variables.get("prefix"));
        Document payload = payload();
        assertEquals("deleteNetworkRequest", payload.getDocumentElement().getLocalName());
        field(payload, "cloudSiteId", "region & \u96ea");
        field(payload, "tenantId", "tenant <one>");
        field(payload, "networkId", "network-1");
        field(payload, "networkType", "private & shared");
        field(payload, "networkStackId", "stack-1");
        field(payload, "modelCustomizationUuid", "model & \u96ea");
        field(payload, "skipAAI", "true");
        field(payload, "requestId", "request & one");
        field(payload, "serviceInstanceId", "service <one>");
        field(payload, "messageId", "message-1");
        field(payload, "notificationUrl", "");
        assertEquals("http://localhost:" + adapter.port() + "/networks/network-1",
                variables.get("mso.adapters.network.rest.endpoint"));
        assertNull(variables.get("WorkflowException"));
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    @Test
    public void absentEmptyAndLiteralNullStackIdsSelectForceDelete() throws Exception {
        for (String stack : Arrays.asList(null, "", "null")) {
            network.setHeatStackId(stack);
            variables.put("mso.adapters.network.rest.endpoint", "http://localhost:" + adapter.port() + "/networks");
            script.prepareNetworkRequest(execution);
            field(payload(), "networkStackId", "force_delete");
        }
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    @Test
    public void modernRequestIdTakesPrecedenceAndTheLegacyIdRemainsSupported() throws Exception {
        variables.put("mso-request-id", "older-request");
        script.prepareNetworkRequest(execution);
        field(payload(), "requestId", "request & one");
        assertEquals("request & one", variables.get("mso-request-id"));
        variables.remove("msoRequestId");
        variables.put("mso-request-id", "legacy & request");
        variables.put("mso.adapters.network.rest.endpoint", "http://localhost:" + adapter.port() + "/networks");
        script.prepareNetworkRequest(execution);
        field(payload(), "requestId", "legacy & request");
    }

    @Test
    public void theOlderModelCustomizationElementRemainsSupported() throws Exception {
        variables.put(PREFIX + "networkRequest", "<network-request>"
                + "<modelCustomizationId>legacy &amp; model</modelCustomizationId></network-request>");
        script.prepareNetworkRequest(execution);
        field(payload(), "modelCustomizationUuid", "legacy & model");
    }

    @Test
    public void deletionSendsThePreparedXmlAndAuthorizationExactlyOnce() {
        script.prepareNetworkRequest(execution);
        String xml = (String) variables.get(PREFIX + "deleteNetworkRequest");
        String response = "<deleteNetworkResponse><networkId>network-1</networkId></deleteNetworkResponse>";
        adapter.stubFor(delete(urlEqualTo("/networks/network-1")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/xml").withHeader("Connection", "close").withBody(response)));
        script.sendRequestToVnfAdapter(execution);
        assertEquals(Integer.valueOf(200), variables.get(PREFIX + "networkReturnCode"));
        assertEquals(response, variables.get(PREFIX + "deleteNetworkResponse"));
        assertNull(variables.get("WorkflowException"));
        adapter.verify(1,
                deleteRequestedFor(urlEqualTo("/networks/network-1"))
                        .withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Content-Type", containing("application/xml"))
                        .withHeader("Accept", containing("application/xml")).withRequestBody(equalToXml(xml)));
        assertEquals(1, adapter.getAllServeEvents().size());
    }

    @Test
    public void downstreamStatusesAndEmptyBodiesRemainTypedForTheWorkflowGateway() {
        script.prepareNetworkRequest(execution);
        for (int status : Arrays.asList(202, 204, 400, 404, 409, 500, 503)) {
            adapter.resetAll();
            variables.remove(PREFIX + "networkReturnCode");
            variables.remove(PREFIX + "deleteNetworkResponse");
            String body = status == 202 ? "<response>accepted</response>" : "";
            adapter.stubFor(delete(urlEqualTo("/networks/network-1")).willReturn(aResponse().withStatus(status)
                    .withHeader("Content-Type", "application/xml").withHeader("Connection", "close").withBody(body)));
            script.sendRequestToVnfAdapter(execution);
            assertEquals("HTTP " + status, Integer.valueOf(status), variables.get(PREFIX + "networkReturnCode"));
            assertEquals(body, variables.get(PREFIX + "deleteNetworkResponse"));
            assertNull(variables.get("WorkflowException"));
            adapter.verify(1, deleteRequestedFor(urlEqualTo("/networks/network-1")));
            assertEquals(1, adapter.getAllServeEvents().size());
        }
    }

    @Test
    public void malformedAdapterUrlsFailBeforeAnyDelete() {
        variables.put("mso.adapters.network.rest.endpoint", "not a URL");
        businessError(() -> script.sendRequestToVnfAdapter(execution), "sendRequestToVnfAdapter()");
        assertTrue(adapter.getAllServeEvents().isEmpty());
        assertNull(variables.get(PREFIX + "networkReturnCode"));
    }

    @Test
    public void brokenTransportReportsABusinessErrorWithoutPublishingASuccessfulStatus() {
        script.prepareNetworkRequest(execution);
        adapter.stubFor(delete(urlEqualTo("/networks/network-1"))
                .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
        businessError(() -> script.sendRequestToVnfAdapter(execution), "sendRequestToVnfAdapter()");
        assertNull(variables.get(PREFIX + "networkReturnCode"));
        assertNull(variables.get(PREFIX + "deleteNetworkResponse"));
        // RestProperties defaults to two transport retries, including DELETE.
        adapter.verify(3, deleteRequestedFor(urlEqualTo("/networks/network-1")));
    }

    @Test
    public void missingInventoryFailsBeforePreparingAnAdapterRequest() {
        variables.remove(PREFIX + "queryAAIResponse");
        businessError(() -> script.prepareNetworkRequest(execution), "Unexpected Response from AAI");
        assertFalse(variables.containsKey(PREFIX + "deleteNetworkRequest"));
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    @Test
    public void rollbackHandoffContainsOnlyPreparedCompensationsAndKeepsTheOriginalError() {
        WorkflowException original = new WorkflowException("DoDeleteNetworkInstance", 5310, "delete failed", "delete");
        variables.put("WorkflowException", original);
        for (String absent : Arrays.asList(null, "")) {
            variables.put(PREFIX + "rollbackNetworkRequest", "<restore>network &amp; \u96ea</restore>");
            variables.put(PREFIX + "rollbackDeactivateSDNCRequest", absent);
            Map<String, String> previous = new HashMap<>();
            previous.put("old", "handoff");
            variables.put("rollbackData", previous);
            script.prepareRollbackData(execution);
            Map<?, ?> rollback = (Map<?, ?>) variables.get("rollbackData");
            assertEquals(1, rollback.size());
            assertEquals("<restore>network &amp; \u96ea</restore>", rollback.get("rollbackNetworkRequest"));
            assertFalse(rollback.containsKey("rollbackDeactivateSDNCRequest"));
            assertNotSame(previous, rollback);
            assertEquals("handoff", previous.get("old"));
            assertSame(original, variables.get("WorkflowException"));
        }
    }

    private Document payload() throws Exception {
        assertTrue(variables.get(PREFIX + "deleteNetworkRequest") instanceof String);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader((String) variables.get(PREFIX + "deleteNetworkRequest"))));
    }

    private void field(Document document, String name, String expected) {
        assertEquals(name, 1, document.getElementsByTagNameNS("*", name).getLength());
        assertEquals(name, expected, document.getElementsByTagNameNS("*", name).item(0).getTextContent());
    }

    private void businessError(Runnable action, String detail) {
        BpmnError failure = assertThrows(BpmnError.class, action::run);
        assertEquals("7000", failure.getErrorCode());
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(7000, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(detail));
    }
}
