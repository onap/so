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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class HandlePnfContractTest {
    private final HandlePNF script = new HandlePNF();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "HandlePNF");
        variables.put("mso-request-id", "request-1");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingRetainsCorrelationAndServiceIdentityIncludingUnicode() {
        variables.put("resourceInput", resource("PNF Grüße", "service-1").toString());

        script.preProcessRequest(execution);

        assertEquals("PNF Grüße", variables.get("pnfCorrelationId"));
        assertEquals("service-1", variables.get("serviceInstanceId"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void absentAndEmptyCorrelationIdsFailBeforeTheServiceIdentityIsStored() {
        for (Object correlation : new Object[] {null, ""}) {
            variables.put("resourceInput", resource(correlation, "service-1").toString());

            assertBusinessError("correlation id is not provided");

            assertFalse(variables.containsKey("pnfCorrelationId"));
            assertFalse(variables.containsKey("serviceInstanceId"));
        }
    }

    @Test
    public void absentAndEmptyServiceIdsProduceTheTypedValidationError() {
        for (Object service : new Object[] {null, ""}) {
            variables.put("resourceInput", resource("PNF-1", service).toString());

            assertBusinessError("serviceInstance ID is not provided");

            assertEquals("PNF-1", variables.get("pnfCorrelationId"));
            assertFalse(variables.containsKey("serviceInstanceId"));
        }
    }

    @Test
    public void malformedResourceInputUsesTheCorrelationValidationError() {
        variables.put("resourceInput", "not-json");

        assertBusinessError("correlation id is not provided");
    }

    @Test
    public void explicitJsonNullCorrelationIdMustNotBecomeTheLiteralStringNull() {
        Assume.assumeTrue("SO-4446: HandlePNF JSON-null validation is a migration-readiness blocker",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("resourceInput", resource(JSONObject.NULL, "service-1").toString());

        assertBusinessError("correlation id is not provided");
    }

    @Test
    public void explicitJsonNullServiceIdMustNotBecomeTheLiteralStringNull() {
        Assume.assumeTrue("SO-4446: HandlePNF JSON-null validation is a migration-readiness blocker",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("resourceInput", resource("PNF-1", JSONObject.NULL).toString());

        assertBusinessError("serviceInstance ID is not provided");
    }

    @Test
    public void completionPayloadPreservesSoapNamespacesEscapesAndOperationIdentifiers() throws Exception {
        JSONObject input = resource("PNF-1", "service Grüße & <SO>");
        input.put("operationType", "create & activate");
        input.put("operationId", "operation & <1>");
        input.put("resourceModelInfo", new JSONObject().put("modelCustomizationUuid", "model & <1>"));
        variables.put("resourceInput", input.toString());
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost/status");

        script.postProcessRequest(execution);

        assertEquals("http://localhost/status", variables.get("CVFMI_dbAdapterEndpoint"));
        Object stored = variables.get("CVFMI_updateResOperStatusRequest");
        assertTrue(stored instanceof String);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(((String) stored).getBytes(StandardCharsets.UTF_8)));
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", document.getDocumentElement().getNamespaceURI());
        assertEquals(1, document
                .getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateResourceOperationStatus").getLength());
        assertElement(document, "operType", "create & activate");
        assertElement(document, "operationId", "operation & <1>");
        assertElement(document, "serviceId", "service Grüße & <SO>");
        assertElement(document, "resourceTemplateUUID", "model & <1>");
        assertElement(document, "progress", "100");
        assertElement(document, "status", "finished");
        assertElement(document, "statusDescription", "SDCN resource creation and activation completed");
    }

    @Test
    public void synchronousAcceptanceKeepsTypedFlagsAndDispatchesOnlyOneCallback() {
        variables.put("isAsyncProcess", true);
        WorkflowContextHolder holder = mock(WorkflowContextHolder.class);
        try (MockedStatic<WorkflowContextHolder> singleton = mockStatic(WorkflowContextHolder.class)) {
            singleton.when(WorkflowContextHolder::getInstance).thenReturn(holder);

            script.sendSyncResponse(execution);
            script.sendSyncResponse(execution);

            assertSame(Boolean.TRUE, variables.get("sentSyncResponse"));
            assertEquals("true", variables.get("HandlePNFWorkflowResponseSent"));
            assertEquals("202", variables.get("HandlePNFResponseCode"));
            assertEquals("Success", variables.get("HandlePNFStatus"));
            JSONObject response = new JSONObject((String) variables.get("WorkflowResponse"));
            assertEquals(1, response.length());
            assertEquals("finished", response.getString("operationStatus"));
            ArgumentCaptor<WorkflowCallbackResponse> callback = ArgumentCaptor.forClass(WorkflowCallbackResponse.class);
            verify(holder).processCallback(eq("HandlePNF"), eq("process-1"), eq("request-1"), callback.capture());
            assertEquals(202, callback.getValue().getStatusCode());
            assertEquals("Success", callback.getValue().getMessage());
            assertEquals(variables.get("WorkflowResponse"), callback.getValue().getResponse());
            verifyNoMoreInteractions(holder);
        }
    }

    private void assertBusinessError(String message) {
        try {
            script.preProcessRequest(execution);
            fail("Expected validation error: " + message);
        } catch (BpmnError error) {
            assertEquals("7000", error.getErrorCode());
            Object stored = variables.get("WorkflowException");
            assertTrue(stored instanceof WorkflowException);
            WorkflowException workflowError = (WorkflowException) stored;
            assertEquals("HandlePNF", workflowError.getProcessKey());
            assertEquals(7000, workflowError.getErrorCode());
            assertEquals(message, workflowError.getErrorMessage());
        }
    }

    private static JSONObject resource(Object correlation, Object service) {
        JSONObject requestInputs = new JSONObject().put("ont_ont_pnf_name", correlation);
        JSONObject nested = new JSONObject().put("service",
                new JSONObject().put("parameters", new JSONObject().put("requestInputs", requestInputs)));
        return new JSONObject().put("requestsInputs", nested.toString()).put("serviceInstanceId", service);
    }

    private static void assertElement(Document document, String name, String value) {
        assertEquals(name, 1, document.getElementsByTagNameNS("", name).getLength());
        assertEquals(value, document.getElementsByTagNameNS("", name).item(0).getTextContent());
    }
}
