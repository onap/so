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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;

public class SdncResponseContractTest {
    private final SDNCAdapterUtils script = new SDNCAdapterUtils();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("prefix", "contract_");
        variables.put("testProcessKey", "SdncContract");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void successCodesRetainStringCodesAndBooleanSuccessFlags() {
        for (String code : new String[] {"0", "200", "201", "202", "204", "299"}) {
            script.validateSDNCResponse(execution, response(code, "accepted", ""), null, true);
            assertEquals(code, variables.get("contract_sdncRequestDataResponseCode"));
            assertSame(Boolean.TRUE, variables.get("contract_sdncResponseSuccess"));
            assertFalse(variables.containsKey("WorkflowException"));
        }
    }

    @Test
    public void absentAndEmptyResponseCodesUseTheExistingZeroSuccessContract() {
        for (String body : new String[] {"<response/>", response("", "accepted", "")}) {
            script.validateSDNCResponse(execution, body, null, true);
            assertEquals("0", variables.get("contract_sdncRequestDataResponseCode"));
            assertSame(Boolean.TRUE, variables.get("contract_sdncResponseSuccess"));
        }
    }

    @Test
    public void escapedInnerResponseTakesPrecedenceOverTheAdapterHeader() {
        String inner = "<output xmlns='urn:onap:sdnc'><response-code>204</response-code>"
                + "<response-message>Gr\u00fc\u00dfe &amp; &lt;SO&gt;</response-message></output>";
        script.validateSDNCResponse(execution, response("503", "outer failure", MsoUtils.xmlEscape(inner)), null, true);

        assertEquals("204", variables.get("contract_sdncRequestDataResponseCode"));
        assertSame(Boolean.TRUE, variables.get("contract_sdncResponseSuccess"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void absentEmptyAndWhitespaceResponsesFailWithTheDefinedWorkflowError() {
        for (String response : new String[] {null, "", " \n\t"}) {
            assertError(response, null, true, 500, "SDNCAdapter Workflow Response is Empty");
            assertFalse(variables.containsKey("contract_sdncRequestDataResponseCode"));
        }
    }

    @Test
    public void unsuccessfulSubflowPreservesTheSuppliedBusinessError() {
        WorkflowException original = new WorkflowException("child", 7010, "timeout Gr\u00fc\u00dfe & SO");

        assertError(null, original, false, 7010, original.getErrorMessage());
        assertFalse(variables.containsKey("contract_sdncRequestDataResponseCode"));
    }

    @Test
    public void unsuccessfulSubflowWithoutAnErrorCreatesTheInternalFallback() {
        assertError(null, null, false, 500, "Internal Error - SDNCAdapter Subflow did NOT complete successfully.");
    }

    @Test
    public void aNonnumericResponseCodeFailsValidationWithoutClaimingSuccess() {
        assertError(response("not-a-code", "bad response", ""), null, true, 500,
                "Internal Error- Unable to validate SDNC Response ");
        assertFalse(variables.containsKey("contract_sdncRequestDataResponseCode"));
    }

    private void assertError(String response, WorkflowException original, boolean success, int code, String message) {
        try {
            script.validateSDNCResponse(execution, response, original, success);
            fail("Expected BPMN error " + code);
        } catch (BpmnError error) {
            assertEquals(Integer.toString(code), error.getErrorCode());
        }
        assertSame(Boolean.FALSE, variables.get("contract_sdncResponseSuccess"));
        WorkflowException stored = (WorkflowException) variables.get("WorkflowException");
        assertEquals(code, stored.getErrorCode());
        assertEquals(message, stored.getErrorMessage());
    }

    private static String response(String code, String message, String escapedData) {
        return "<w:SDNCAdapterWorkflowResponse xmlns:w='http://org.onap/so/workflow/schema/v1'"
                + " xmlns:a='http://org.onap/workflow/sdnc/adapter/schema/v1'><w:response-data>"
                + "<a:CallbackHeader><a:ResponseCode>" + MsoUtils.xmlEscape(code) + "</a:ResponseCode>"
                + "<a:ResponseMessage>" + MsoUtils.xmlEscape(message) + "</a:ResponseMessage></a:CallbackHeader>"
                + "<a:RequestData>" + escapedData
                + "</a:RequestData></w:response-data></w:SDNCAdapterWorkflowResponse>";
    }
}
