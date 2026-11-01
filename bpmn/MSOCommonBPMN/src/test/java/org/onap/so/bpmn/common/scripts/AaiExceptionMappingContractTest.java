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
package org.onap.so.bpmn.common.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class AaiExceptionMappingContractTest {
    private static final String WORKFLOW = "http://org.onap/so/workflow/schema/v1";
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final ExceptionUtil script = new ExceptionUtil();

    @Before
    public void useStatefulWorkflowErrorVariables() {
        variables.put("testProcessKey", "inventory-consumer");
        variables.put("prefix", "Inventory_");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void structuredFaultSubstitutesItsFourBusinessValues() {
        WorkflowException returned = script.MapAAIExceptionToWorkflowException(
                "<Fault><text>%1 rejected %2 in %3 for %4</text><variable>AAI</variable>"
                        + "<variable>vnf-1</variable><variable>東京</variable><variable>request-1</variable></Fault>",
                execution);
        assertSame(returned, error(5000, "Received error from A&amp;AI (AAI rejected vnf-1 in 東京 for request-1)"));
    }

    @Test
    public void structuredTextWithoutArgumentsRemainsTheBusinessMessage() {
        WorkflowException returned =
                script.MapAAIExceptionToWorkflowException("<Fault><text>Rejected 東京</text></Fault>", execution);
        assertSame(returned, error(5000, "Received error from A&amp;AI (Rejected 東京)"));
    }

    @Test
    public void structuredFaultTakesPrecedenceOverTheTransportFallback() {
        variables.put("Inventory_ErrorResponse", "Connection timed out");
        script.MapAAIExceptionToWorkflowException("<Fault><text>resource rejected</text></Fault>", execution);
        error(5000, "Received error from A&amp;AI (resource rejected)");
    }

    @Test
    public void structuredFaultTreatsDollarSignsAsLiteralBusinessData() {
        Assume.assumeTrue("SO-4457: structured AAI fault variables are interpreted as regex replacements",
                Boolean.getBoolean("groovy.migration.verification"));
        script.MapAAIExceptionToWorkflowException("<Fault><text>%1 %2 %3 %4</text>"
                + "<variable>price $2</variable><variable>vnf</variable><variable>region</variable><variable>request</variable></Fault>",
                execution);
        error(5000, "Received error from A&amp;AI (price $2 vnf region request)");
    }

    @Test
    public void structuredFaultPreservesBackslashesInBusinessData() {
        Assume.assumeTrue("SO-4457: structured AAI fault variables are interpreted as regex replacements",
                Boolean.getBoolean("groovy.migration.verification"));
        script.MapAAIExceptionToWorkflowException("<Fault><text>%1 %2 %3 %4</text>"
                + "<variable>domain\\tenant</variable><variable>vnf</variable><variable>region</variable><variable>request</variable></Fault>",
                execution);
        error(5000, "Received error from A&amp;AI (domain\\tenant vnf region request)");
    }

    @Test
    public void aMissingFallbackMessageProducesTheDefinedGenericError() {
        WorkflowException returned = script.MapAAIExceptionToWorkflowException("not XML", execution);
        assertSame(returned, error(5000, "Received error from A&AI ()"));
    }

    @Test
    public void genericFaultRetainsTheSuppliedCodeAndNestedMessage() throws Exception {
        script.MapAAIExceptionToWorkflowExceptionGeneric(execution, "<Fault><message>Rejected 東京</message></Fault>",
                4097);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(4097, error.getErrorCode());
        Document detail = xml(error.getErrorMessage());
        assertEquals("message", detail.getDocumentElement().getNodeName());
        assertEquals("Rejected 東京", detail.getDocumentElement().getTextContent());
    }

    @Test
    public void genericRestFaultRetainsItsWrapperAndNestedContent() throws Exception {
        script.MapAAIExceptionToWorkflowExceptionGeneric(execution,
                "<RESTFault><message>inventory unavailable</message></RESTFault>", 5037);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(5037, error.getErrorCode());
        Document detail = xml(error.getErrorMessage());
        assertEquals("RESTFault", detail.getDocumentElement().getNodeName());
        assertEquals("inventory unavailable", text(detail, "*", "message"));
    }

    @Test
    public void successfulXmlWithoutAFaultUsesTheBadResponseFallback() {
        script.MapAAIExceptionToWorkflowExceptionGeneric(execution, "<response/>", 5027);
        error(5027, "Received a bad response from AAI");
    }

    @Test
    public void malformedGenericFaultUsesTheBadResponseFallback() {
        script.MapAAIExceptionToWorkflowExceptionGeneric(execution, "<Fault>", 5027);
        error(5027, "Received a bad response from AAI");
    }

    @Test
    public void mainflowRequestKeepsTheOriginalErrorAndRequestNamespace() throws Exception {
        WorkflowException original = new WorkflowException("child-workflow", 5017, "Rejected 東京", "query");
        variables.put("WorkflowException", original);
        Document request = xml(script.processMainflowsBPMNException(execution,
                "<?xml version='1.0'?><r:request-info xmlns:r='urn:onap:request'><r:request-id>request-1</r:request-id></r:request-info>"));
        assertSame(original, variables.get("WorkflowException"));
        assertEquals(WORKFLOW, request.getDocumentElement().getNamespaceURI());
        assertEquals("request-1", text(request, "urn:onap:request", "request-id"));
        assertEquals("5017", text(request, WORKFLOW, "ErrorCode"));
        assertEquals("Rejected 東京", text(request, WORKFLOW, "ErrorMessage"));
        assertEquals("child-workflow", original.getProcessKey());
        assertEquals("query", original.getWorkStep());
    }

    @Test
    public void mainflowWithoutAnErrorBuildsItsTypedFallbackBeforeReturningXml() throws Exception {
        Document request = xml(script.processMainflowsBPMNException(execution, "<request-info/>"));
        error(2500, "Internal Error - WorkflowException Object and/or RequestInfo is null! inventory-consumer");
        assertEquals("2500", text(request, WORKFLOW, "ErrorCode"));
    }

    private WorkflowException error(int code, String message) {
        Object value = variables.get("WorkflowException");
        assertNotNull(value);
        assertEquals(WorkflowException.class, value.getClass());
        WorkflowException result = (WorkflowException) value;
        assertEquals("inventory-consumer", result.getProcessKey());
        assertEquals(code, result.getErrorCode());
        assertEquals(message, result.getErrorMessage());
        return result;
    }

    private String text(Document document, String namespace, String name) {
        var node = document.getElementsByTagNameNS(namespace, name).item(0);
        assertNotNull("Missing " + name, node);
        return node.getTextContent();
    }

    private Document xml(String source) throws Exception {
        assertNotNull(source);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(source)));
    }
}
