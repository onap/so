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
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCustomer;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutServiceInstance;
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

/**
 * Unit test cases for DoCreateServiceInstance.bpmn
 */
public class DoCreateServiceInstanceTest extends WorkflowTest {
    private static final String EOL = "\n";
    private final CallbackSet callbacks = new CallbackSet();
    private final String sdncAdapterCallback =
            "<output xmlns=\"com:att:sdnctl:l3api\">" + EOL +
                    "  <svc-request-id>((REQUEST-ID))</svc-request-id>" + EOL +
                    "  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
                    "</output>" + EOL;

    public DoCreateServiceInstanceTest() throws IOException {
        callbacks.put("assign", sdncAdapterCallback);
    }

    /**
     * Sunny day VID scenario.
     *
     * @throws Exception
     */
    //@Ignore // File not found - unable to run the test.  Also, Stubs need updating..
    @Test
    @Deployment(resources = {
            "subprocess/DoCreateServiceInstance.bpmn",
            "subprocess/SDNCAdapterV1.bpmn",
            "subprocess/GenericGetService.bpmn",
            "subprocess/GenericPutService.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/DoCreateServiceInstanceRollback.bpmn",
            "subprocess/FalloutHandler.bpmn"})
    public void sunnyDay() throws Exception {

        logStart();

        //AAI
        MockGetCustomer("MCBH-1610", "CreateServiceInstance/createServiceInstance_queryGlobalCustomerId_AAIResponse_Success.xml");
        MockPutServiceInstance("MCBH-1610", "viprsvc", "RaaTest-si-id", "");
        MockGetServiceInstance("MCBH-1610", "viprsvc", "RaaTest-si-id", "GenericFlows/getServiceInstance.xml");
        MockNodeQueryServiceInstanceByName("RAATest-si", "");

        MockNodeQueryServiceInstanceById("RaaTest-si-id", "");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSINoRelations.xml");
        MockNodeQueryServiceInstanceById("MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
        //SDNC
        mockSDNCAdapter(200);
        //DB
        mockUpdateRequestDB(200, "DBUpdateResponse.xml");
        String businessKey = UUID.randomUUID().toString();

        Map<String, Object> variables = new HashMap<>();
        setupVariables(variables);
        invokeSubProcess("DoCreateServiceInstance", businessKey, variables);
        injectSDNCCallbacks(callbacks, "assign");
        waitForProcessEnd(businessKey, 10000);
        Assert.assertTrue(isProcessEnded(businessKey));
        String workflowException = BPMNUtil.getVariable(processEngineRule, "DoCreateServiceInstance", "WorkflowException");
        System.out.println("workflowException:\n" + workflowException);
        assertEquals(null, workflowException);

        logEnd();
    }

    // Success Scenario
    private void setupVariables(Map<String, Object> variables) {
        variables.put("mso-request-id", "RaaDSITest1");
        variables.put("isDebugLogEnabled", "true");
        variables.put("msoRequestId", "RaaDSITestRequestId-1");
        variables.put("serviceInstanceId", "RaaTest-si-id");
        variables.put("serviceModelInfo", "{\"modelType\":\"service\",\"modelInvariantUuid\":\"uuid-miu-svc-011-abcdef\",\"modelVersionUuid\":\"ASDC_TOSCA_UUID\",\"modelName\":\"SIModelName1\",\"modelVersion\":\"2\"}");
        variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
        variables.put("globalSubscriberId", "MCBH-1610");
        variables.put("subscriptionServiceType", "viprsvc");
        variables.put("instanceName", "RAATest-1");
    }
}