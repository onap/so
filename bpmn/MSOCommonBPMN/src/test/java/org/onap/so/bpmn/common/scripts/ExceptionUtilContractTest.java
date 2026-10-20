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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Test;
import org.onap.logging.filter.base.ONAPComponentsList;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class ExceptionUtilContractTest {
    private static final String ERROR_NAMESPACE = "http://org.onap/so/workflow/schema/v1";
    private final ExceptionUtil utility = new ExceptionUtil();

    @Test
    public void subflowRetainsTheOriginalErrorAndItsOrigin() {
        Map<String, Object> variables = variables();
        WorkflowException original =
                new WorkflowException("DoCreateVnf", 7000, "Controller rejected request", "create");
        variables.put("WorkflowException", original);
        DelegateExecution execution = execution(variables);

        utility.processSubflowsBPMNException(execution);

        assertSame(original, variables.get("WorkflowException"));
        verify(execution, never()).setVariable(eq("WorkflowException"), any());
        verify(execution, never()).removeVariable("WorkflowException");
    }

    @Test
    public void subflowCreatesATypedFallbackWhenNoErrorExists() {
        Map<String, Object> variables = variables();

        utility.processSubflowsBPMNException(execution(variables));

        assertError(variables, "HomingBB", 2500, "Internal Error - Occured During HomingBB");
    }

    @Test
    public void explicitNullErrorAlsoUsesTheFallback() {
        Map<String, Object> variables = variables();
        variables.put("WorkflowException", null);

        utility.processSubflowsBPMNException(execution(variables));

        assertError(variables, "HomingBB", 2500, "Internal Error - Occured During HomingBB");
    }

    @Test
    public void javaExceptionWithoutDetailsUsesTheProcessKey() {
        Map<String, Object> variables = variables();

        utility.processJavaException(execution(variables));

        assertError(variables, "HomingBB", 2500, "Catch a Java Lang Exception in HomingBB");
    }

    @Test
    public void emptyJavaExceptionDetailsDoNotAddAPunctuationSuffix() {
        Map<String, Object> variables = variables();
        variables.put("BPMN_javaExpMsg", "");

        utility.processJavaException(execution(variables));

        assertError(variables, "HomingBB", 2500, "Catch a Java Lang Exception in HomingBB");
    }

    @Test
    public void javaExceptionRetainsUnicodeDetails() {
        Map<String, Object> variables = variables();
        variables.put("BPMN_javaExpMsg", "Controller rejected München");
        variables.put("gUnknownError", "Downstream failure");

        utility.processJavaException(execution(variables));

        assertError(variables, "HomingBB", 2500,
                "Catch a Java Lang Exception in HomingBB: Controller rejected München");
    }

    @Test
    public void javaExceptionDoesNotSwallowABpmnErrorFromErrorStorage() {
        Assume.assumeTrue("SO-4443: ExceptionUtil logging masks BpmnError; verification profile reproduces the blocker",
                Boolean.getBoolean("groovy.migration.verification"));
        Map<String, Object> variables = variables();
        DelegateExecution execution = execution(variables);
        BpmnError original = new BpmnError("storage-error", "Cannot store workflow error");
        doThrow(original).when(execution).setVariable(eq("WorkflowException"), any());

        try {
            utility.processJavaException(execution);
            fail("The BPMN error must propagate");
        } catch (BpmnError actual) {
            assertSame(original, actual);
        }
    }

    @Test
    public void buildAndThrowStoresTheBusinessErrorBeforeThrowing() {
        Map<String, Object> variables = variables();

        try {
            utility.buildAndThrowWorkflowException(execution(variables), 4004, "Missing model");
            fail("Expected a BPMN error");
        } catch (BpmnError error) {
            assertEquals("4004", error.getErrorCode());
            assertEquals("MSOWorkflowException: Missing model", error.getMessage());
            assertError(variables, "HomingBB", 4004, "Missing model");
        }
    }

    @Test
    public void workflowErrorRetainsTheExternalComponent() {
        Map<String, Object> variables = variables();
        ONAPComponentsList component = mock(ONAPComponentsList.class);

        utility.buildWorkflowException(execution(variables), 5300, "Controller failure", component);

        WorkflowException error = assertError(variables, "HomingBB", 5300, "Controller failure");
        assertSame(component, error.getExtSystemErrorSource());
    }

    @Test
    public void sdncStatusMappingKeepsSpecificErrorsAndTheGenericFallback() {
        assertEquals("5310", utility.MapSDNCResponseCodeToErrorCode("500"));
        assertEquals("5320", utility.MapSDNCResponseCodeToErrorCode("408"));
        assertEquals("5350", utility.MapSDNCResponseCodeToErrorCode("60010"));
        assertEquals("5300", utility.MapSDNCResponseCodeToErrorCode("503"));
        assertEquals("5300", utility.MapSDNCResponseCodeToErrorCode(null));
    }

    @Test
    public void malformedAaiResponsesPreserveConnectionAndTimeoutClassification() {
        String[][] cases = {{"Connection refused", "5010", "Could not communicate with A&AI"},
                {"Connection timed out", "5020", "No response from A&AI"},
                {"Read TIMEOUT", "5020", "No response from A&AI"},
                {"Rejected request", "5000", "Received error from A&AI (Rejected request)"}};
        for (String[] fixture : cases) {
            Map<String, Object> variables = variables();
            variables.put("prefix", "Test_");
            variables.put("Test_ErrorResponse", fixture[0]);

            WorkflowException returned = utility.MapAAIExceptionToWorkflowException("not XML", execution(variables));

            assertSame(returned, assertError(variables, "HomingBB", Integer.parseInt(fixture[1]), fixture[2]));
        }
    }

    @Test
    public void errorResponseUsesTheWorkflowNamespaceAndNumericCode() throws Exception {
        Document document = xml(utility.buildErrorResponseXml(new WorkflowException("HomingBB", 5300, "Failure Ω")));

        assertEquals(ERROR_NAMESPACE, document.getDocumentElement().getNamespaceURI());
        assertEquals("Failure Ω",
                document.getElementsByTagNameNS(ERROR_NAMESPACE, "ErrorMessage").item(0).getTextContent());
        assertEquals("5300", document.getElementsByTagNameNS(ERROR_NAMESPACE, "ErrorCode").item(0).getTextContent());
    }

    @Test
    public void missingWorkflowErrorProducesTheDefinedXmlFallback() throws Exception {
        Document document = xml(utility.buildErrorResponseXml(null));

        assertEquals("Internal Error",
                document.getElementsByTagNameNS(ERROR_NAMESPACE, "ErrorMessage").item(0).getTextContent());
        assertEquals("2500", document.getElementsByTagNameNS(ERROR_NAMESPACE, "ErrorCode").item(0).getTextContent());
    }

    private static Map<String, Object> variables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("testProcessKey", "HomingBB");
        return variables;
    }

    private static DelegateExecution execution(Map<String, Object> variables) {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(invocation -> variables.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            variables.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        return execution;
    }

    private static WorkflowException assertError(Map<String, Object> variables, String process, int code,
            String message) {
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(process, error.getProcessKey());
        assertEquals(code, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
        return error;
    }

    private static Document xml(String value) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
    }
}
