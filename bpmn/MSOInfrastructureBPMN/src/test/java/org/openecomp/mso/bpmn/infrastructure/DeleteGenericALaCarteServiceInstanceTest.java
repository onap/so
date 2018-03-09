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
import static org.junit.Assert.assertNotNull;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Unit test cases for DelServiceInstance.bpmn
 */
public class DeleteGenericALaCarteServiceInstanceTest extends WorkflowTest {

    public DeleteGenericALaCarteServiceInstanceTest() throws IOException {
    }

    /**
     * Sunny day VID scenario.
     *
     * @throws Exception
     */
    @Ignore // IGNORED FOR 1710 MERGE TO ONAP
    @Test
    @Deployment(resources = {
            "process/DeleteGenericALaCarteServiceInstance.bpmn",
            "subprocess/DoDeleteServiceInstance.bpmn",
            "subprocess/GenericDeleteService.bpmn",
            "subprocess/GenericGetService.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/FalloutHandler.bpmn"})
    public void sunnyDayAlaCarte() throws Exception {

        logStart();

        //AAI
        MockDeleteServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "");
        MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSINoRelations.xml");
        MockNodeQueryServiceInstanceById("MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
        //DB
        mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

        String businessKey = UUID.randomUUID().toString();

        Map<String, String> variables = setupVariables();
        WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "DeleteGenericALaCarteServiceInstance", variables);
        waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

        String workflowResp = BPMNUtil.getVariable(processEngineRule, "DeleteGenericALaCarteServiceInstance", "WorkflowResponse");
        //assertNotNull(workflowResp);
        System.out.println("Workflow (Synch) Response:\n" + workflowResp);
        String workflowException = BPMNUtil.getVariable(processEngineRule, "DeleteGenericALaCarteServiceInstance", "WorkflowException");
        String completionReq = BPMNUtil.getVariable(processEngineRule, "DeleteGenericALaCarteServiceInstance", "completionRequest");
        System.out.println("completionReq:\n" + completionReq);
        System.out.println("workflowException:\n" + workflowException);
        assertNotNull(completionReq);
        assertEquals(null, workflowException);

        logEnd();
    }

    // Success Scenario
    private Map<String, String> setupVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put("isDebugLogEnabled", "true");
        variables.put("bpmnRequest", getRequest());
        variables.put("mso-request-id", "RaaTestRequestId-1");
        variables.put("serviceInstanceId", "MIS%252F1604%252F0026%252FSW_INTERNET");
        return variables;
    }

    public String getRequest() {
        String request = "{\"requestDetails\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantUuid\":\"uuid-miu-svc-011-abcdef\",\"modelUuid\":\"ASDC_TOSCA_UUID\",\"modelName\":\"SIModelName1\",\"modelVersion\":\"2\"},\"subscriberInfo\":{\"globalSubscriberId\":\"SDN-ETHERNET-INTERNET\",\"subscriberName\":\"\"},\"requestInfo\":{\"instanceName\":\"1604-MVM-26\",\"source\":\"VID\",\"suppressRollback\":\"true\",\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"8b1df54faa3b49078e3416e21370a3ba\"},\"requestParameters\":{\"subscriptionServiceType\":\"123456789\",\"aLaCarte\":\"false\",\"userParams\":\"somep\"}}}";
        return request;
    }

}
