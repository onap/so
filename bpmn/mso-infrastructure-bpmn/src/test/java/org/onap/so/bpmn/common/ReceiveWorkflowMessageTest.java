/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.*;


/**
 * Unit tests for SDNCAdapterRestV1.
 */
@Ignore
public class ReceiveWorkflowMessageTest extends WorkflowTest {

    private static final String EOL = "\n";

    private final CallbackSet callbacks = new CallbackSet();

    public ReceiveWorkflowMessageTest() throws IOException {
        callbacks.put("sdnc-event-success", JSON, "SDNCAEvent",
                "{" + EOL + "  \"SDNCEvent\": {" + EOL + "    \"eventType\": \"evenType\"," + EOL
                        + "    \"eventCorrelatorType\": \"HOST-NAME\"," + EOL
                        + "    \"eventCorrelator\": \"((CORRELATOR))\"," + EOL + "    \"params\": {" + EOL
                        + "      \"success-indicator\":\"Y\"" + EOL + "	 }" + EOL + "  }" + EOL + "}" + EOL);

        callbacks.put("sdnc-event-fail", JSON, "SDNCAEvent", "{" + EOL + "  \"SDNCEvent\": {" + EOL
                + "    \"eventType\": \"evenType\"," + EOL + "    \"eventCorrelatorType\": \"HOST-NAME\"," + EOL
                + "    \"eventCorrelator\": \"((CORRELATOR))\"," + EOL + "    \"params\": {" + EOL
                + "      \"success-indicator\":\"N\"," + EOL + "      \"error-message\":\"SOMETHING BAD HAPPENED\""
                + EOL + "	 }" + EOL + "  }" + EOL + "}" + EOL);
    }

    /**
     * Test the happy path.
     */
    @Test
    @Deployment(resources = {"subprocess/ReceiveWorkflowMessage.bpmn"})
    public void happyPath() throws Exception {

        logStart();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", "dffbae0e-5588-4bd6-9749-b0f0adb52312");
        variables.put("isDebugLogEnabled", "true");
        variables.put("RCVWFMSG_timeout", "PT1M");
        variables.put("RCVWFMSG_messageType", "SDNCAEvent");
        variables.put("RCVWFMSG_correlator", "USOSTCDALTX0101UJZZ31");

        invokeSubProcess("ReceiveWorkflowMessage", businessKey, variables);
        injectWorkflowMessages(callbacks, "sdnc-event-success");
        waitForProcessEnd(businessKey, 10000);

        String response = (String) getVariableFromHistory(businessKey, "WorkflowResponse");
        System.out.println("Response:\n" + response);
        assertTrue(response.contains("\"SDNCEvent\""));
        assertTrue((boolean) getVariableFromHistory(businessKey, "RCVWFMSG_SuccessIndicator"));

        logEnd();
    }

    /**
     * Test the timeout scenario.
     */
    @Test
    @Deployment(resources = {"subprocess/ReceiveWorkflowMessage.bpmn"})
    public void timeout() throws Exception {

        logStart();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", "dffbae0e-5588-4bd6-9749-b0f0adb52312");
        variables.put("isDebugLogEnabled", "true");
        variables.put("RCVWFMSG_timeout", "PT5S");
        variables.put("RCVWFMSG_messageType", "SDNCAEvent");
        variables.put("RCVWFMSG_correlator", "USOSTCDALTX0101UJZZ31");

        invokeSubProcess("ReceiveWorkflowMessage", businessKey, variables);

        // No injection

        waitForProcessEnd(businessKey, 10000);

        // There is no response from SDNC, so the flow doesn't set WorkflowResponse.
        String response = (String) getVariableFromHistory(businessKey, "WorkflowResponse");
        assertNull(response);
        WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        assertNotNull(wfe);
        System.out.println(wfe.toString());
        assertEquals("Receive Workflow Message Timeout Error", wfe.getErrorMessage());
        assertFalse((boolean) getVariableFromHistory(businessKey, "RCVWFMSG_SuccessIndicator"));

        logEnd();
    }
}
