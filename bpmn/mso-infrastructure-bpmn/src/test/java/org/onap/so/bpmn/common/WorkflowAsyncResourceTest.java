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

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;
import org.onap.so.bpmn.common.workflow.service.WorkflowAsyncResource;

@Ignore
public class WorkflowAsyncResourceTest extends WorkflowTest {

    @Test
    @Deployment(resources = {"testAsyncResource.bpmn"})
    public void asyncRequestSuccess() throws InterruptedException {
        // it can be any request which asynchronously processed by the workflow
        String request =
                "<aetgt:CreateTenantRequest xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\" xmlns:sdncadapterworkflow=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns5=\"http://org.onap/so/request/types/v1\">  <msoservtypes:service-information xmlns:msoservtypes=\"http://org.onap/so/request/types/v1\">    <msoservtypes:service-type>SDN-ETHERNET-INTERNET</msoservtypes:service-type>    <msoservtypes:service-instance-id>HI/VLXM/950604//SW_INTERNET</msoservtypes:service-instance-id>    <msoservtypes:subscriber-name>SubName01</msoservtypes:subscriber-name> </msoservtypes:service-information> </aetgt:CreateTenantRequest>";

        Map<String, String> variables = new HashMap<>();
        variables.put("testAsyncRequestMsg", request);
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("mso-service-request-timeout", "5");

        WorkflowResponse workflowResponse =
                BPMNUtil.executeAsyncWorkflow(processEngineRule, "testAsyncProcess", variables);
        assertEquals(
                "Received the request, the process is getting executed, request message<aetgt:CreateTenantRequest xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\" xmlns:sdncadapterworkflow=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns5=\"http://org.onap/so/request/types/v1\">  <msoservtypes:service-information xmlns:msoservtypes=\"http://org.onap/so/request/types/v1\">    <msoservtypes:service-type>SDN-ETHERNET-INTERNET</msoservtypes:service-type>    <msoservtypes:service-instance-id>HI/VLXM/950604//SW_INTERNET</msoservtypes:service-instance-id>    <msoservtypes:subscriber-name>SubName01</msoservtypes:subscriber-name> </msoservtypes:service-information> </aetgt:CreateTenantRequest>",
                workflowResponse.getResponse());
        assertEquals(200, workflowResponse.getMessageCode());
    }

    private void executeWorkflow(String request, String requestId, String processKey) {
        WorkflowAsyncResource workflowResource = new WorkflowAsyncResource();
        VariableMapImpl variableMap = new VariableMapImpl();

        Map<String, Object> variableValueType = new HashMap<>();

        Map<String, Object> requestMsg = new HashMap<>();
        requestMsg.put("value", request);
        requestMsg.put("type", "String");

        Map<String, Object> msorequestId = new HashMap<>();
        msorequestId.put("type", "String");
        msorequestId.put("value", requestId);

        Map<String, Object> timeout = new HashMap<>();
        timeout.put("type", "String");
        timeout.put("value", "5");

        variableValueType.put("testAsyncRequestMsg", requestMsg);
        variableValueType.put("mso-request-id", msorequestId);
        variableValueType.put("mso-service-request-timeout", timeout);

        variableMap.put("variables", variableValueType);

        workflowResource.setProcessEngineServices4junit(processEngineRule);
        workflowResource.startProcessInstanceByKey(processKey, variableMap);
    }


}
