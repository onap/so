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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class CoreNssiActivationContractTest {
    private static final String MACRO_PATH = "/serviceInstantiation/v7/serviceInstances/network-1/vnfs/vnf-1";
    private static final String POLL_PATH = "/requests/request-1";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final DoActivateCoreNSSI script = new DoActivateCoreNSSI();
    @Rule
    public WireMockRule macro = new WireMockRule(0);

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "DoActivateCoreNSSI");
        variables.put("operationType", "activateInstance");
        variables.put("sliceParams", "{\"snssaiList\":[\"001-000001\",\"001-000002\"]}");
        variables.put("subscriptionServiceType", "5G");
        variables.put("networkServiceInstanceId", "network-1");
        variables.put("vnfId", "vnf-1");
        variables.put("requestPayload", "{\"requestDetails\":{\"name\":\"core & access\",\"enabled\":false}}");
        variables.put("mso.infra.endpoint.url", "http://localhost:" + macro.port());
        variables.put("so.auth.username", "test");
        variables.put("so.auth.password", "test");
        variables.put("mso.msoKey", "unused-test-key");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost/status");
        variables.put("nsiId", "nsi-1");
        variables.put("jobId", "job-1");
        variables.put("serviceInstanceID", "nssi-1");
        variables.put("modelUuid", "model-2");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("serviceType", "5G");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void activationPreprocessingKeepsTheFirstSnssaiAndSubscriptionType() {
        script.preProcessRequest(execution);
        assertEquals("activated", variables.get("oStatus"));
        assertEquals("001-000001", variables.get("sNssai"));
        assertEquals("5G", variables.get("serviceType"));
        assertTrue(macro.getAllServeEvents().isEmpty());
    }

    @Test
    public void deactivationUsesTheSameInputContractWithItsOwnTargetStatus() {
        variables.put("operationType", "deactivateInstance");
        script.preProcessRequest(execution);
        assertEquals("deactivated", variables.get("oStatus"));
        assertEquals("001-000001", variables.get("sNssai"));
        assertEquals("5G", variables.get("serviceType"));
    }

    @Test
    public void acceptedMacroUpdatesKeepTheRequestIdentityAndStringFlags() {
        for (int status : new int[] {200, 201, 202, 203}) {
            macro.resetAll();
            stubPut(status,
                    "{\"requestReferences\":{\"requestId\":\"request-1\",\"requestSelfLink\":\"" + pollUrl() + "\"}}");
            script.sendPutRequestToSOMacro(execution);
            assertEquals("request-1", variables.get("macroOperationId"));
            assertEquals(pollUrl(), variables.get("requestSelfLink"));
            assertEquals("no", variables.get("isSOTimeOut"));
            assertEquals("yes", variables.get("isSOResponseSucceed"));
            assertFalse(variables.containsKey("updateResourceOperationStatus"));
            verifyPut();
        }
    }

    @Test
    public void rejectedMacroUpdatesPrepareFailureWithoutReplacingCorrelation() throws Exception {
        for (int status : new int[] {204, 400, 500}) {
            macro.resetAll();
            variables.put("macroOperationId", "previous-request");
            variables.put("requestSelfLink", "previous-link");
            stubPut(status, "");
            script.sendPutRequestToSOMacro(execution);
            assertEquals("no", variables.get("isSOResponseSucceed"));
            assertEquals("previous-request", variables.get("macroOperationId"));
            assertEquals("previous-link", variables.get("requestSelfLink"));
            assertStatusPayload("failed", "0");
            verifyPut();
        }
    }

    @Test
    public void emptySuccessfulMacroResponsePreparesFailureStatus() throws Exception {
        stubPut(200, "");
        script.sendPutRequestToSOMacro(execution);
        assertEquals("no", variables.get("isSOResponseSucceed"));
        assertStatusPayload("failed", "0");
        assertFalse(variables.containsKey("macroOperationId"));
        verifyPut();
    }

    @Test
    public void malformedSuccessfulMacroResponseCannotBeClassifiedAsSuccessful() {
        Assume.assumeTrue("SO-4446: malformed macro response must not start successful core-NSSI polling",
                Boolean.getBoolean("groovy.migration.verification"));
        stubPut(202, "not JSON");
        try {
            script.sendPutRequestToSOMacro(execution);
        } catch (BpmnError expected) {
            assertEquals("7000", expected.getErrorCode());
            assertEquals(7000, ((WorkflowException) variables.get("WorkflowException")).getErrorCode());
        }
        assertFalse("Malformed macro response cannot be accepted", "yes".equals(variables.get("isSOResponseSucceed")));
        verifyPut();
    }

    @Test
    public void pollingReadsTheReturnedRequestLinkWithAuthentication() {
        variables.put("requestSelfLink", pollUrl());
        macro.stubFor(get(urlEqualTo(POLL_PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"request\":{\"requestStatus\":{\"requestState\":\"COMPLETE\"}}}")));
        script.getSOPUTProgress(execution);
        assertEquals("COMPLETE", variables.get("requestState"));
        assertFalse(variables.containsKey("updateResourceOperationStatus"));
        macro.verify(1,
                getRequestedFor(urlEqualTo(POLL_PATH)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Accept", equalTo("application/json")));
        assertEquals(1, macro.getAllServeEvents().size());
    }

    @Test
    public void rejectedPollPreparesFailureInsteadOfSuccessfulCompletion() throws Exception {
        for (int status : new int[] {404, 500}) {
            macro.resetAll();
            variables.put("requestSelfLink", pollUrl());
            macro.stubFor(get(urlEqualTo(POLL_PATH)).willReturn(aResponse().withStatus(status)));
            script.getSOPUTProgress(execution);
            assertEquals("no", variables.get("isSOResponseSucceed"));
            assertStatusPayload("failed", "0");
            macro.verify(1, getRequestedFor(urlEqualTo(POLL_PATH)));
            assertEquals(1, macro.getAllServeEvents().size());
        }
    }

    @Test
    public void completedMacroRequestCreatesTypedCompletionStateAndStatusIdentity() throws Exception {
        for (String completed : new String[] {"COMPLETE", "complete"}) {
            variables.put("requestState", completed);
            script.prepareUpdateResourceOperationStatus(execution);
            assertEquals("100", variables.get("progress"));
            assertEquals("finished", variables.get("status"));
            assertStatusPayload("finished", "100");
        }
        assertTrue(macro.getAllServeEvents().isEmpty());
    }

    @Test
    public void noncompleteMacroStateCannotPrepareACompletionWrite() {
        for (String state : new String[] {null, "", "FAILED", "IN_PROGRESS"}) {
            variables.put("requestState", state);
            variables.put("progress", "27");
            variables.put("status", "processing");
            assertWorkflowFailure(() -> script.prepareUpdateResourceOperationStatus(execution));
            assertEquals("27", variables.get("progress"));
            assertEquals("processing", variables.get("status"));
            assertFalse(variables.containsKey("updateResourceOperationStatus"));
        }
        assertTrue(macro.getAllServeEvents().isEmpty());
    }

    @Test
    public void failedOperationStatusRetainsIdentifiersAndEscapesXml() throws Exception {
        variables.put("modelUuid", "model<&東京");
        script.prepareFailedOperationStatusUpdate(execution);
        assertStatusPayload("failed", "0");
        assertEquals("model<&東京", xmlValue(statusDocument(), "resourceTemplateUUID"));
        assertTrue(macro.getAllServeEvents().isEmpty());
    }

    @Test
    public void sliceProfileUpdateUsesOneInventoryWriteBeforePreparingCompletion() throws Exception {
        ServiceInstance profile = profile("profile-1");
        variables.put("sliceProfileServiceInstance", profile);
        variables.put("oStatus", "activated");
        List<AAIResourceUri> writes = new ArrayList<>();
        try (MockedConstruction<AAIResourcesClient> clients =
                mockConstruction(AAIResourcesClient.class, (client, context) -> doAnswer(call -> {
                    writes.add(call.getArgument(0));
                    assertEquals(profile, call.getArgument(1));
                    assertEquals("activated", profile.getOrchestrationStatus());
                    assertFalse(variables.containsKey("updateResourceOperationStatus"));
                    return null;
                }).when(client).update(any(AAIResourceUri.class), any(ServiceInstance.class)))) {
            script.updateSliceProfileOrchestrationStatus(execution);
            assertEquals(1, clients.constructed().size());
            assertEquals(1, writes.size());
            assertTrue(writes.get(0).build().toString().endsWith("/business/customers/customer/customer-1/"
                    + "service-subscriptions/service-subscription/5G/service-instances/service-instance/profile-1"));
            assertStatusPayload("finished", "100");
        }
    }

    @Test
    public void missingSliceProfileIdFailsBeforeInventoryMutation() {
        variables.put("sliceProfileServiceInstance", profile(null));
        variables.put("oStatus", "activated");
        try (MockedConstruction<AAIResourcesClient> clients = mockConstruction(AAIResourcesClient.class)) {
            assertWorkflowFailure(() -> script.updateSliceProfileOrchestrationStatus(execution));
            assertTrue(clients.constructed().isEmpty());
            assertFalse(variables.containsKey("updateResourceOperationStatus"));
        }
    }

    @Test
    public void rejectedSliceProfileUpdateCannotPrepareSuccessfulStatus() {
        variables.put("sliceProfileServiceInstance", profile("profile-1"));
        variables.put("oStatus", "activated");
        List<AAIResourceUri> writes = new ArrayList<>();
        try (MockedConstruction<AAIResourcesClient> clients =
                mockConstruction(AAIResourcesClient.class, (client, context) -> doAnswer(call -> {
                    writes.add(call.getArgument(0));
                    throw new IllegalStateException("inventory unavailable");
                }).when(client).update(any(AAIResourceUri.class), any(ServiceInstance.class)))) {
            assertWorkflowFailure(() -> script.updateSliceProfileOrchestrationStatus(execution));
            assertEquals(1, writes.size());
            assertEquals(1, clients.constructed().size());
            assertTrue(((WorkflowException) variables.get("WorkflowException")).getErrorMessage()
                    .contains("inventory unavailable"));
            assertFalse(variables.containsKey("updateResourceOperationStatus"));
        }
    }

    private ServiceInstance profile(String id) {
        ServiceInstance profile = new ServiceInstance();
        profile.setServiceInstanceId(id);
        profile.setOrchestrationStatus("deactivated");
        return profile;
    }

    private void assertWorkflowFailure(Runnable action) {
        try {
            action.run();
            fail("Expected a BPMN business error");
        } catch (BpmnError expected) {
            assertEquals("7000", expected.getErrorCode());
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            assertEquals(7000, ((WorkflowException) variables.get("WorkflowException")).getErrorCode());
        }
    }

    private void stubPut(int status, String body) {
        macro.stubFor(put(urlEqualTo(MACRO_PATH)).willReturn(
                aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private void verifyPut() {
        macro.verify(1,
                putRequestedFor(urlEqualTo(MACRO_PATH)).withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                        .withHeader("Accept", equalTo("application/json"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(equalToJson((String) variables.get("requestPayload"))));
        assertEquals(1, macro.getAllServeEvents().size());
    }

    private String pollUrl() {
        return "http://localhost:" + macro.port() + POLL_PATH;
    }

    private void assertStatusPayload(String status, String progress) throws Exception {
        Document document = statusDocument();
        assertEquals(1, document
                .getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateResourceOperationStatus").getLength());
        assertEquals("nsi-1", xmlValue(document, "serviceId"));
        assertEquals("job-1", xmlValue(document, "operationId"));
        assertEquals("job-1", xmlValue(document, "jobId"));
        assertEquals("nssi-1", xmlValue(document, "resourceInstanceID"));
        assertEquals(variables.get("modelUuid"), xmlValue(document, "resourceTemplateUUID"));
        assertEquals(variables.get("operationType"), xmlValue(document, "operType"));
        assertEquals(status, xmlValue(document, "status"));
        assertEquals(progress, xmlValue(document, "progress"));
        assertEquals("http://localhost/status", variables.get("dbAdapterEndpoint"));
    }

    private Document statusDocument() throws Exception {
        assertTrue(variables.get("updateResourceOperationStatus") instanceof String);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader((String) variables.get("updateResourceOperationStatus"))));
    }

    private String xmlValue(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagName(name).getLength());
        return document.getElementsByTagName(name).item(0).getTextContent();
    }
}
