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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_ID;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.workflow.context.WorkflowContext;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class PnfResponseContractTest {
    private static final String WORKFLOW_NS = "http://org.onap/so/workflow/schema/v1";
    private static final String REQUEST_NS = "http://org.onap/so/infra/vnf-request/v1";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final RecordingPnf processor = new RecordingPnf();
    private final WorkflowContextHolder holder = mock(WorkflowContextHolder.class);
    private MockedStatic<WorkflowContextHolder> contexts;

    @Before
    public void prepareExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        when(execution.getProcessDefinitionId()).thenReturn("PnfFlow:1:definition");
        variables.put(REQUEST_ID, "request-1");
        variables.put(PNF_CORRELATION_ID, "pnf-1");
        variables.put("testProcessKey", "PnfFlow");
        contexts = mockStatic(WorkflowContextHolder.class);
        contexts.when(WorkflowContextHolder::getInstance).thenReturn(holder);
        when(holder.getWorkflowContext(anyString())).thenReturn(mock(WorkflowContext.class));
    }

    @After
    public void restoreContextLookup() {
        if (contexts != null) {
            contexts.close();
        }
    }

    @Test
    public void theAcceptedResponseRetainsBothRequestAndPnfIdentities() {
        processor.sendResponse(execution);
        assertEquals(200, processor.response.status);
        assertEquals(1, processor.response.calls);
        JSONObject body = new JSONObject(processor.response.body);
        assertEquals(1, body.length());
        JSONObject references = body.getJSONObject("requestReferences");
        assertEquals("request-1", references.getString("requestId"));
        assertEquals("pnf-1", references.getString("instanceId"));
        assertEquals(2, references.length());
    }

    @Test
    public void quotedPnfIdentitiesRemainValidJsonStringValues() {
        Assume.assumeTrue("SO-4457: the PNF response interpolates its identity without JSON escaping",
                Boolean.getBoolean("groovy.migration.verification"));
        String name = "node \"quoted\" \\ Gr\u00fc\u00dfe";
        variables.put(PNF_CORRELATION_ID, name);
        processor.sendResponse(execution);
        assertEquals(name,
                new JSONObject(processor.response.body).getJSONObject("requestReferences").getString("instanceId"));
    }

    @Test
    public void aHealthyStringFlagReturnsTheUnmodifiedSuccessBody() {
        RecordingHealth health = new RecordingHealth();
        variables.put("healthyStatus", "true");
        variables.put("healthcheckmessage", "healthy Gr\u00fc\u00dfe & SO");
        health.sendResponse(execution);
        assertEquals(200, health.response.status);
        assertEquals("healthy Gr\u00fc\u00dfe & SO", health.response.body);
        assertEquals(1, health.response.calls);
    }

    @Test
    public void anUnhealthyStringFlagReturnsServiceUnavailable() {
        RecordingHealth health = new RecordingHealth();
        variables.put("healthyStatus", "false");
        variables.put("healthcheckmessage", "health check failed");
        health.sendResponse(execution);
        assertEquals(503, health.response.status);
        assertEquals("health check failed", health.response.body);
        assertEquals(1, health.response.calls);
    }

    @Test
    public void anAbsentHealthFlagCannotProduceASuccessResponse() {
        RecordingHealth health = new RecordingHealth();
        variables.put("healthcheckmessage", "status unavailable");
        health.sendResponse(execution);
        assertEquals(503, health.response.status);
        assertEquals("status unavailable", health.response.body);
    }

    @Test
    public void completionXmlPreservesEscapedRequestIdentityAndProcessIdentity() throws Exception {
        variables.put(REQUEST_ID, "request <&> Gr\u00fc\u00dfe");
        processor.prepareCompletion(execution);
        Document body = xml((String) variables.get("Generic_CompleteMsoProcessRequest"));
        assertEquals(WORKFLOW_NS, body.getDocumentElement().getNamespaceURI());
        assertEquals("MsoCompletionRequest", body.getDocumentElement().getLocalName());
        assertEquals("request <&> Gr\u00fc\u00dfe", text(body, REQUEST_NS, "request-id"));
        assertEquals("UPDATE", text(body, REQUEST_NS, "action"));
        assertEquals("VID", text(body, REQUEST_NS, "source"));
        assertEquals("PnfFlow:1:definition", text(body, WORKFLOW_NS, "mso-bpel-name"));
        assertEquals("Activity is successful.", text(body, WORKFLOW_NS, "status-message"));
    }

    @Test
    public void falloutXmlPreservesTheOriginalErrorWithoutSendingASecondResponse() throws Exception {
        WorkflowException error = new WorkflowException("original", 5001, "failed <&> Gr\u00fc\u00dfe");
        variables.put("WorkflowException", error);
        processor.prepareFalloutHandler(execution);
        Document body = xml((String) variables.get("Generic_FalloutHandlerRequest"));
        assertEquals(WORKFLOW_NS, body.getDocumentElement().getNamespaceURI());
        assertEquals("FalloutHandlerRequest", body.getDocumentElement().getLocalName());
        assertEquals("request-1", text(body, REQUEST_NS, "request-id"));
        assertEquals("5001", text(body, WORKFLOW_NS, "ErrorCode"));
        assertEquals("failed <&> Gr\u00fc\u00dfe", text(body, WORKFLOW_NS, "ErrorMessage"));
        assertSame(error, variables.get("WorkflowException"));
        assertEquals(0, processor.response.calls);
        verify(holder).getWorkflowContext("request-1");
    }

    @Test
    public void falloutBeforeTheInitialResponseStillReturnsTheRequestReferences() throws Exception {
        when(holder.getWorkflowContext("request-1")).thenReturn(null);
        variables.put("WorkflowException", new WorkflowException("original", 5001, "failed"));
        processor.prepareFalloutHandler(execution);
        assertEquals(1, processor.response.calls);
        assertEquals(200, processor.response.status);
        assertEquals("request-1",
                new JSONObject(processor.response.body).getJSONObject("requestReferences").getString("requestId"));
        assertEquals("5001",
                text(xml((String) variables.get("Generic_FalloutHandlerRequest")), WORKFLOW_NS, "ErrorCode"));
    }

    @Test
    public void missingFalloutErrorProducesATypedPreparationFailure() {
        BpmnError failure = assertThrows(BpmnError.class, () -> processor.prepareFalloutHandler(execution));
        assertEquals("2000", failure.getErrorCode());
        assertEquals(2000, ((WorkflowException) variables.get("WorkflowException")).getErrorCode());
    }

    @Test
    public void contextLookupUsesTheCurrentRequestIdentity() {
        WorkflowContext expected = mock(WorkflowContext.class);
        variables.put(REQUEST_ID, "request-2");
        when(holder.getWorkflowContext("request-2")).thenReturn(expected);
        assertSame(expected, GenericPnfTaskProcessor.getWorkflowContext(execution));
        verify(holder).getWorkflowContext("request-2");
    }

    private static Document xml(String value) throws Exception {
        assertNotNull(value);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static String text(Document value, String namespace, String localName) {
        assertEquals(1, value.getElementsByTagNameNS(namespace, localName).getLength());
        return value.getElementsByTagNameNS(namespace, localName).item(0).getTextContent();
    }

    private static class Response {
        Object status;
        String body;
        int calls;

        void record(Object value, String content) {
            status = value;
            body = content;
            calls++;
        }
    }

    private static class RecordingPnf extends GenericPnfTaskProcessor {
        final Response response = new Response();

        @Override
        protected void sendWorkflowResponse(DelegateExecution execution, Object status, String body) {
            response.record(status, body);
        }
    }

    private static class RecordingHealth extends HealthCheckActivate {
        final Response response = new Response();

        @Override
        protected void sendWorkflowResponse(DelegateExecution execution, Object status, String body) {
            response.record(status, body);
        }
    }
}
