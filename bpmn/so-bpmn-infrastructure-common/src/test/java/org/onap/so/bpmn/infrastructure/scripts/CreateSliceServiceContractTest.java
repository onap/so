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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.graphinventory.entities.uri.HttpAwareUri;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class CreateSliceServiceContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private CreateSliceService script;

    @Before
    public void configureExecution() {
        clients = mockConstruction(AAIResourcesClient.class);
        script = new CreateSliceService();
        variables.put("testProcessKey", "CreateSliceService");
        variables.put("serviceInstanceId", "service-1");
        variables.put("serviceInstanceName", "東京 & \"slice\"");
        variables.put("operationId", "operation<&1");
        variables.put("globalSubscriberId", "customer<&1");
        variables.put("operationType", "CREATE");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost/status");
        when(execution.getBusinessKey()).thenReturn("task-1");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @After
    public void closeClients() {
        clients.close();
    }

    @Test
    public void preprocessingPreservesRequestIdentityAndNestedProfileTypes() {
        variables.put("bpmnRequest", request().toString());
        variables.put("mso-request-id", "request-1");
        script.preProcessRequest(execution);
        assertEquals("CRESS_", variables.get("prefix"));
        assertEquals("request-1", variables.get("msoRequestId"));
        assertEquals("service-1", variables.get("serviceInstanceId"));
        UUID.fromString((String) variables.get("operationId"));
        assertEquals("customer-1", variables.get("globalSubscriberId"));
        assertEquals("UUI", variables.get("source"));
        assertEquals("false", variables.get("disableRollback"));
        assertEquals("5G", variables.get("subscriptionServiceType"));
        assertEquals(profile(), variables.get("serviceProfile"));
        assertTrue(variables.get("serviceInputParams") instanceof Map);
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void preprocessingGeneratesAnIdOnlyWhenTheSuppliedServiceIdIsBlank() {
        for (String id : new String[] {null, "", " \t"}) {
            variables.put("serviceInstanceId", id);
            variables.put("bpmnRequest", request().toString());
            script.preProcessRequest(execution);
            UUID.fromString((String) variables.get("serviceInstanceId"));
            UUID.fromString((String) variables.get("operationId"));
        }
    }

    @Test
    public void missingSubscriberOrModelFailsValidationBeforeAnyInventoryAction() {
        for (String field : new String[] {"subscriberInfo", "modelInfo"}) {
            JSONObject input = request();
            input.getJSONObject("requestDetails").remove(field);
            variables.put("bpmnRequest", input.toString());
            assertPreprocessingFailure(500);
        }
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void malformedNestedUserInputFailsBeforeAnyInventoryAction() {
        JSONObject input = request();
        input.getJSONObject("requestDetails").getJSONObject("requestParameters").getJSONArray("userParams")
                .getJSONObject(0).put("UUIRequest", "not-json");
        variables.put("bpmnRequest", input.toString());
        assertPreprocessingFailure(7000);
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void initialStatusUsesTheRequestIdentityAndSoapNamespaces() throws Exception {
        script.prepareInitServiceOperationStatus(execution);
        Document xml = statusXml();
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", xml.getDocumentElement().getNamespaceURI());
        assertEquals(1,
                xml.getElementsByTagNameNS("http://org.onap.so/requestsdb", "initServiceOperationStatus").getLength());
        assertStatus(xml, "processing", "0", "", "Prepare service creation");
        assertEquals("CREATE", variables.get("operationType"));
        assertEquals("http://localhost/status", variables.get("CSSOS_dbAdapterEndpoint"));
        Map<?, ?> headers = (Map<?, ?>) variables.get("CSSOS_headerMap");
        assertEquals("application/soap+xml", headers.get("content-type"));
        assertTrue(headers.containsKey("Authorization"));
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void initialStatusEncodesTheServiceIdentifierBeforeBuildingXml() throws Exception {
        variables.put("serviceInstanceId", "service /東京&1");
        script.prepareInitServiceOperationStatus(execution);
        String encoded = "service%20%2F%E6%9D%B1%E4%BA%AC%261";
        assertEquals(encoded, variables.get("serviceInstanceId"));
        assertEquals(encoded, text(statusXml(), "serviceId"));
    }

    @Test
    public void finalStatusPreservesFailureDetailsAndDoesNotChangeTheIdentifiers() throws Exception {
        variables.put("operationResult", "failed");
        variables.put("operationProgress", "0");
        variables.put("operationReason", "東京 & <controller> \"failed\"");
        script.prepareUpdateServiceOperationStatus(execution);
        assertStatus(statusXml(), "failed", "0", "東京 & <controller> \"failed\"", "service: failed progress: 0");
        assertEquals("service-1", variables.get("serviceInstanceId"));
        assertEquals("operation<&1", variables.get("operationId"));
    }

    @Test
    public void finalStatusSupportsEmptyReasonAndStringCompletionProgress() throws Exception {
        variables.put("operationResult", "finished");
        variables.put("operationProgress", "100");
        variables.put("operationReason", "");
        script.prepareUpdateServiceOperationStatus(execution);
        assertStatus(statusXml(), "finished", "100", "", "service: finished progress: 100");
        assertEquals("100", variables.get("operationProgress"));
    }

    @Test
    public void taskCreationUsesTheBusinessKeyAndPreservesProfileValueTypes() {
        variables.put("serviceProfile", profile());
        script.prepareCreateOrchestrationTask(execution);
        assertEquals("task-1", variables.get("orchestrationTaskId"));
        assertEquals("task-1", variables.get("CSSOT_taskId"));
        assertEquals("SliceServiceTask", variables.get("CSSOT_name"));
        assertEquals("Planning", variables.get("CSSOT_status"));
        assertEquals("false", variables.get("CSSOT_isManual"));
        assertEquals("POST", variables.get("CSSOT_requestMethod"));
        assertTaskPayload();
    }

    @Test
    public void taskUpdatesAdvanceAnIntegerCounterAndSerializeTheCurrentSelection() {
        variables.put("serviceProfile", profile());
        script.prepareCreateOrchestrationTask(execution);
        variables.put("nsstCount", 0);
        variables.put("taskStatus", "Allocated");
        script.prepareUpdateOrchestrationTask(execution);
        assertEquals(Integer.valueOf(1), variables.get("nsstCount"));
        assertEquals("Allocated", variables.get("CSSOT_status"));
        assertEquals("PUT", variables.get("CSSOT_requestMethod"));
        assertTaskPayload();
        script.prepareUpdateOrchestrationTask(execution);
        assertEquals(Integer.valueOf(2), variables.get("nsstCount"));
    }

    @Test
    public void userOptionsDeserializeNestedTaskParametersWithoutLosingFalseOrZero() throws Exception {
        variables.put("serviceProfile", profile());
        script.prepareCreateOrchestrationTask(execution);
        String parameters = (String) variables.get("CSSOT_paramJson");
        variables.remove("sliceTaskParams");
        variables.put("CSSOT_dbResponse", new JSONObject().put("params", parameters).toString());
        script.processUserOptions(execution);
        SliceTaskParamsAdapter task = (SliceTaskParamsAdapter) variables.get("sliceTaskParams");
        assertEquals("service-1", task.getServiceId());
        assertEquals("東京 & \"slice\"", task.getServiceName());
        assertEquals(profile(), task.getServiceProfile());
    }

    @Test
    public void selectionRequestPreservesCorrelationAndProfileTypesWithoutPublishingTheLocalProfileId() {
        Map<String, Object> profile = profile();
        profile.put("profileId", "local-profile-1");
        profile.put("name", "東京 & slice");
        variables.put("serviceProfile", profile);
        variables.put("msoRequestId", "request-1");
        variables.put("mso.adapters.oof.timeout", "PT5M");
        try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS)) {
            properties.when(() -> UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint"))
                    .thenReturn("http://localhost/callback");
            script.prepareSelectNSTRequest(execution);
        }
        assertEquals("/api/oof/v1/selection/nst", variables.get("nstSelectionUrl"));
        assertEquals("NSTSelectionResponse", variables.get("nstSelection_messageType"));
        assertEquals("request-1", variables.get("nstSelection_correlator"));
        assertEquals("PT5M", variables.get("nstSelection_timeout"));
        JSONObject request = new JSONObject((String) variables.get("nstSelection_oofRequest"));
        JSONObject information = request.getJSONObject("requestInfo");
        assertEquals("request-1", information.get("requestId"));
        assertEquals("request-1", information.get("transactionId"));
        assertEquals("so", information.get("sourceId"));
        assertEquals("http://localhost/callback/NSTSelectionResponse/request-1", information.get("callbackUrl"));
        JSONObject transmitted = request.getJSONObject("serviceProfile");
        assertFalse(transmitted.has("profileId"));
        assertEquals(Boolean.FALSE, transmitted.get("enabled"));
        assertEquals(Integer.valueOf(0), transmitted.get("count"));
        assertEquals("", transmitted.get("empty"));
        assertEquals("東京 & slice", transmitted.get("name"));
        assertEquals("001", transmitted.getJSONArray("items").get(0));
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void userOptionsLookupInitializesTheGetMethodAndCommitActionForAFreshTask() {
        script.prepareGetUserOptions(execution);
        assertEquals("GET", variables.get("CSSOT_requestMethod"));
        assertEquals("commit", variables.get("taskAction"));
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void templateSelectionUsesTheFirstSolutionAndKeepsAllTemplateIdentifiers() {
        variables.put("sliceTaskParams", new SliceTaskParamsAdapter());
        JSONObject first = new JSONObject().put("UUID", "nst-1").put("invariantUUID", "invariant-1").put("NSTName",
                "東京 & template");
        variables.put("nstSelection_oofResponse", new JSONObject()
                .put("solutions", new JSONArray().put(first).put(new JSONObject().put("UUID", "nst-2"))).toString());
        script.processNSTSolutions(execution);
        SliceTaskParamsAdapter task = (SliceTaskParamsAdapter) variables.get("sliceTaskParams");
        assertEquals("nst-1", task.getNstId());
        assertEquals("東京 & template", task.getNstName());
        assertEquals("nst-1", task.getNSTInfo().getUUID());
        assertEquals("invariant-1", task.getNSTInfo().getInvariantUUID());
        assertEquals("東京 & template", task.getNSTInfo().getName());
        assertEquals("nst-1", ((Map<?, ?>) variables.get("nstSolution")).get("UUID"));
    }

    @Test
    public void missingEmptyOrMalformedTemplateSolutionsProduceWorkflowError401() {
        for (String response : new String[] {null, "", "not-json", "{}", "{\"solutions\":[]}"}) {
            variables.put("nstSelection_oofResponse", response);
            variables.put("sliceTaskParams", new SliceTaskParamsAdapter());
            variables.remove("WorkflowException");
            try {
                script.processNSTSolutions(execution);
                fail("Invalid template solutions must fail");
            } catch (BpmnError expected) {
                assertEquals("401", expected.getErrorCode());
                WorkflowException failure = (WorkflowException) variables.get("WorkflowException");
                assertEquals(401, failure.getErrorCode());
                assertEquals("Failed to get NST solution suggested by OOF.", failure.getErrorMessage());
            }
            assertNull(((SliceTaskParamsAdapter) variables.get("sliceTaskParams")).getNstId());
        }
        verifyNoInteractions(clients.constructed().get(0));
    }

    @Test
    public void inventoryStatusUpdatesOnlyTheOwnedServiceAndRequestedOrchestrationField() {
        variables.put("orchestrationStatus", "Active");
        script.updateAAIOrchStatus(execution);
        ArgumentCaptor<AAIResourceUri> uri = ArgumentCaptor.forClass(AAIResourceUri.class);
        ArgumentCaptor<ServiceInstance> payload = ArgumentCaptor.forClass(ServiceInstance.class);
        AAIResourcesClient client = clients.constructed().get(0);
        verify(client).update(uri.capture(), payload.capture());
        assertTrue(((HttpAwareUri) uri.getValue()).buildNoNetwork().toString()
                .endsWith("/service-instances/service-instance/service-1"));
        assertEquals("Active", payload.getValue().getOrchestrationStatus());
        assertNull(payload.getValue().getServiceInstanceId());
        assertNull(payload.getValue().getModelVersionId());
        verifyNoMoreInteractions(client);
    }

    @Test
    public void inventoryUpdateFailurePreservesTheCauseInWorkflowError7000() {
        AAIResourcesClient client = clients.constructed().get(0);
        doThrow(new IllegalStateException("inventory unavailable")).when(client).update(any(AAIResourceUri.class),
                any(ServiceInstance.class));
        variables.put("orchestrationStatus", "Active");
        try {
            script.updateAAIOrchStatus(execution);
            fail("Inventory update failure must fail the workflow");
        } catch (BpmnError expected) {
            assertEquals("7000", expected.getErrorCode());
            WorkflowException failure = (WorkflowException) variables.get("WorkflowException");
            assertEquals(7000, failure.getErrorCode());
            assertTrue(failure.getErrorMessage().contains("inventory unavailable"));
        }
        verify(client).update(any(AAIResourceUri.class), any(ServiceInstance.class));
        verifyNoMoreInteractions(client);
    }

    @Test
    public void completionRequestRetainsNamespaceScopedRequestIdentityAndEscapedValues() throws Exception {
        variables.put("msoRequestId", "request<&1");
        variables.put("source", "東京 & source");
        script.prepareCompletionRequest(execution);
        Document xml = parse((String) variables.get("completionRequest"));
        assertEquals("http://org.onap/so/workflow/schema/v1", xml.getDocumentElement().getNamespaceURI());
        assertEquals("request<&1", xml.getElementsByTagNameNS("http://org.onap/so/infra/vnf-request/v1", "request-id")
                .item(0).getTextContent());
        assertEquals("東京 & source", xml.getElementsByTagNameNS("http://org.onap/so/infra/vnf-request/v1", "source")
                .item(0).getTextContent());
        assertEquals("service-1", text(xml, "serviceInstanceId"));
        assertEquals("CREATE", xml.getElementsByTagNameNS("http://org.onap/so/infra/vnf-request/v1", "action").item(0)
                .getTextContent());
    }

    private Map<String, Object> profile() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("enabled", false);
        profile.put("count", 0);
        profile.put("empty", "");
        profile.put("items", new ArrayList<>(Arrays.asList("001", "002")));
        return profile;
    }

    private JSONObject request() {
        JSONObject uui = new JSONObject().put("service", new JSONObject().put("serviceType", "5G").put("parameters",
                new JSONObject().put("requestInputs", profile())));
        return new JSONObject().put("requestDetails",
                new JSONObject()
                        .put("modelInfo", new JSONObject().put("modelVersionId", "model-1").put("modelType", "service"))
                        .put("subscriberInfo", new JSONObject().put("globalSubscriberId", "customer-1"))
                        .put("requestInfo",
                                new JSONObject().put("source", "UUI").put("instanceName", "東京 & slice")
                                        .put("suppressRollback", false))
                        .put("requestParameters", new JSONObject().put("userParams",
                                new JSONArray().put(new JSONObject().put("UUIRequest", uui.toString())))));
    }

    private void assertPreprocessingFailure(int code) {
        variables.remove("WorkflowException");
        try {
            script.preProcessRequest(execution);
            fail("Invalid slice input must fail");
        } catch (BpmnError expected) {
            assertEquals(Integer.toString(code), expected.getErrorCode());
            assertEquals(code, ((WorkflowException) variables.get("WorkflowException")).getErrorCode());
        }
    }

    private void assertTaskPayload() {
        assertTrue(variables.get("sliceTaskParams") instanceof SliceTaskParamsAdapter);
        JSONObject task = new JSONObject((String) variables.get("CSSOT_paramJson"));
        assertEquals("service-1", task.get("serviceId"));
        assertEquals("東京 & \"slice\"", task.get("serviceName"));
        JSONObject profile = task.getJSONObject("serviceProfile");
        assertEquals(Boolean.FALSE, profile.get("enabled"));
        assertEquals(Integer.valueOf(0), profile.get("count"));
        assertEquals("", profile.get("empty"));
        assertEquals("001", profile.getJSONArray("items").get(0));
        assertEquals("002", profile.getJSONArray("items").get(1));
    }

    private void assertStatus(Document xml, String result, String progress, String reason, String content) {
        assertEquals("service-1", text(xml, "serviceId"));
        assertEquals("operation<&1", text(xml, "operationId"));
        assertEquals("CREATE", text(xml, "operationType"));
        assertEquals("customer<&1", text(xml, "userId"));
        assertEquals(result, text(xml, "result"));
        assertEquals(progress, text(xml, "progress"));
        assertEquals(reason, text(xml, "reason"));
        assertEquals(content, text(xml, "operationContent"));
    }

    private Document statusXml() throws Exception {
        return parse((String) variables.get("CSSOS_updateServiceOperStatusRequest"));
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static String text(Document xml, String name) {
        assertEquals("Expected one " + name, 1, xml.getElementsByTagNameNS("", name).getLength());
        return xml.getElementsByTagNameNS("", name).item(0).getTextContent();
    }
}
