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

package org.openecomp.mso.bpmn.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.RollbackData;

/**
 * Unit test cases for DoCreateServiceInstanceRollback.bpmn
 */
public class DoCreateSIRollbackTest extends WorkflowTest {
    private static final String EOL = "\n";
    private final CallbackSet callbacks = new CallbackSet();
    private final String sdncAdapterCallback =
            "<output xmlns=\"com:att:sdnctl:l3api\">" + EOL +
                    "  <svc-request-id>((REQUEST-ID))</svc-request-id>" + EOL +
                    "  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
                    "</output>" + EOL;

    public DoCreateSIRollbackTest() throws IOException {
        callbacks.put("deactivate", sdncAdapterCallback);
        callbacks.put("delete", sdncAdapterCallback);
    }

    /**
     * Sunny day VID scenario.
     *
     * @throws Exception
     */
    //@Ignore // File not found - unable to run the test.  Also, Stubs need updating..
    @Test
    @Deployment(resources = {
            "subprocess/DoCreateServiceInstanceRollback.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/GenericDeleteService.bpmn",
            "subprocess/GenericGetService.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/FalloutHandler.bpmn"})
    public void sunnyDay() throws Exception {

        logStart();

        //AAI
        MockDeleteServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSINoRelations.xml");
        MockNodeQueryServiceInstanceById("MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
        //SDNC
        mockSDNCAdapter(200);
        //DB
        mockUpdateRequestDB(200, "DBUpdateResponse.xml");
        String businessKey = UUID.randomUUID().toString();

        Map<String, Object> variables = new HashMap<>();
        setupVariables(variables);
        invokeSubProcess("DoCreateServiceInstanceRollback", businessKey, variables);
        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "delete");
        waitForProcessEnd(businessKey, 10000);
        Assert.assertTrue(isProcessEnded(businessKey));
        String workflowException = BPMNUtil.getVariable(processEngineRule, "DoCreateServiceInstanceRollback", "WorkflowException");
        System.out.println("workflowException:\n" + workflowException);
        assertEquals(null, workflowException);

        logEnd();
    }

    // Success Scenario
    private void setupVariables(Map<String, Object> variables) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "RaaTestRequestId-1");
        variables.put("mso-request-id", "RaaTestRequestId-1");
        variables.put("serviceInstanceId", "MIS%252F1604%252F0026%252FSW_INTERNET");

        RollbackData rollbackData = new RollbackData();

        rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", "MIS%252F1604%252F0026%252FSW_INTERNET");
        rollbackData.put("SERVICEINSTANCE", "globalCustomerId", "SDN-ETHERNET-INTERNET");
        rollbackData.put("SERVICEINSTANCE", "serviceSubscriptionType", "123456789");
        rollbackData.put("SERVICEINSTANCE", "disablerollback", "false");
        rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true");
        rollbackData.put("SERVICEINSTANCE", "rollbackSDNC", "true");

        String req = "<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5=\"http://org.openecomp/mso/request/types/v1\"" + EOL +
                "xmlns:sdncadapter=\"http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1\" " + EOL +
                "xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\">" + EOL +
                "<sdncadapter:RequestHeader>" + EOL +
                "<sdncadapter:RequestId>b043d290-140d-4a38-a9b6-95d3b8bd27d4</sdncadapter:RequestId>" + EOL +
                "<sdncadapter:SvcInstanceId>MIS%252F1604%252F0026%252FSW_INTERNET</sdncadapter:SvcInstanceId>" + EOL +
                "<sdncadapter:SvcAction>deactivate</sdncadapter:SvcAction>" + EOL +
                "<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>" + EOL +
                "<sdncadapter:CallbackUrl>http://localhost:8080/mso/SDNCAdapterCallbackService</sdncadapter:CallbackUrl>" + EOL +
                "</sdncadapter:RequestHeader>" + EOL +
                "<sdncadapterworkflow:SDNCRequestData>" + EOL +
                "<request-information>" + EOL +
                "<request-id>RaaTestRequestId-1</request-id>" + EOL +
                "<source>MSO</source>" + EOL +
                "<notification-url/>" + EOL +
                "<order-number/>" + EOL +
                "<order-version/>" + EOL +
                "<request-action>DeleteServiceInstance</request-action>" + EOL +
                "</request-information>" + EOL +
                "<service-information>" + EOL +
                "<service-id/>" + EOL +
                "<subscription-service-type>123456789</subscription-service-type>" + EOL +
                "<onap-model-information>" + EOL +
                "<model-invariant-uuid/>" + EOL +
                "<model-uuid/>" + EOL +
                "<model-version/>" + EOL +
                "<model-name/>" + EOL +
                "</onap-model-information>" + EOL +
                "<service-instance-id>MIS%252F1604%252F0026%252FSW_INTERNET</service-instance-id>" + EOL +
                "<subscriber-name/>" + EOL +
                "<global-customer-id>SDN-ETHERNET-INTERNET</global-customer-id>" + EOL +
                "</service-information>" + EOL +
                "<service-request-input>" + EOL +
                "<service-instance-name/>" + EOL +
                "</service-request-input>" + EOL +
                "</sdncadapterworkflow:SDNCRequestData>" + EOL +
                "</sdncadapterworkflow:SDNCAdapterWorkflowRequest>";

        String req1 = "<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5=\"http://org.openecomp/mso/request/types/v1\"" + EOL +
                "xmlns:sdncadapter=\"http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1\" " + EOL +
                "xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\">" + EOL +
                "<sdncadapter:RequestHeader>" + EOL +
                "<sdncadapter:RequestId>bca4fede-0804-4c13-af69-9e80b378150f</sdncadapter:RequestId>" + EOL +
                "<sdncadapter:SvcInstanceId>MIS%252F1604%252F0026%252FSW_INTERNET</sdncadapter:SvcInstanceId>" + EOL +
                "<sdncadapter:SvcAction>delete</sdncadapter:SvcAction>" + EOL +
                "<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>" + EOL +
                "<sdncadapter:CallbackUrl>http://localhost:8080/mso/SDNCAdapterCallbackService</sdncadapter:CallbackUrl>" + EOL +
                "</sdncadapter:RequestHeader>" + EOL +
                "<sdncadapterworkflow:SDNCRequestData>" + EOL +
                "<request-information>" + EOL +
                "<request-id>RaaTestRequestId-1</request-id>" + EOL +
                "<source>MSO</source>" + EOL +
                "<notification-url/>" + EOL +
                "<order-number/>" + EOL +
                "<order-version/>" + EOL +
                "<request-action>DeleteServiceInstance</request-action>" + EOL +
                "</request-information>" + EOL +
                "<service-information>" + EOL +
                "<service-id/>" + EOL +
                "<subscription-service-type>123456789</subscription-service-type>" + EOL +
                "<onap-model-information>" + EOL +
                "<model-invariant-uuid/>" + EOL +
                "<model-uuid/>" + EOL +
                "<model-version/>" + EOL +
                "<model-name/>" + EOL +
                "</onap-model-information>" + EOL +
                "<service-instance-id>MIS%252F1604%252F0026%252FSW_INTERNET</service-instance-id>" + EOL +
                "<subscriber-name/>" + EOL +
                "<global-customer-id>SDN-ETHERNET-INTERNET</global-customer-id>" + EOL +
                "</service-information>" + EOL +
                "<service-request-input>" + EOL +
                "<service-instance-name/>" + EOL +
                "</service-request-input>" + EOL +
                "</sdncadapterworkflow:SDNCRequestData>" + EOL +
                "</sdncadapterworkflow:SDNCAdapterWorkflowRequest>";

        rollbackData.put("SERVICEINSTANCE", "sdncDeactivate", req);

        rollbackData.put("SERVICEINSTANCE", "sdncDelete", req1);
        variables.put("rollbackData", rollbackData);

    }
}
