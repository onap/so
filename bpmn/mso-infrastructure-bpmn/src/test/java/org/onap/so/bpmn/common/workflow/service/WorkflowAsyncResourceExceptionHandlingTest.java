/*- 
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.workflow.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.Test;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;


public class WorkflowAsyncResourceExceptionHandlingTest {

    @Test
    @Deployment(resources = {"testAsyncResource.bpmn"})
    public void asyncRequestSuccess() {
        VariableMapImpl variableMap = new VariableMapImpl();

        Map<String, Object> variableValueType = new HashMap<>();

        Map<String, Object> requestMsg = new HashMap<>();
        requestMsg.put("value", "");
        requestMsg.put("type", "String");

        Map<String, Object> msorequestId = new HashMap<>();
        msorequestId.put("type", "String");
        msorequestId.put("value", UUID.randomUUID().toString());

        Map<String, Object> timeout = new HashMap<>();
        timeout.put("type", "String");
        timeout.put("value", "5");

        variableValueType.put("testAsyncRequestMsg", requestMsg);
        variableValueType.put("mso-request-id", msorequestId);
        variableValueType.put("mso-service-request-timeout", timeout);

        variableMap.put("variables", variableValueType);
        WorkflowAsyncResource workflowAsyncResource = new WorkflowAsyncResource();
        workflowAsyncResource.setProcessor(new WorkflowProcessor());
        Response res = workflowAsyncResource.startProcessInstanceByKey("randomKey", variableMap);
        assertEquals(500, res.getStatus());
        WorkflowResponse workflowResponse = (WorkflowResponse) res.getEntity();
        assertNotNull(workflowResponse);
        assertEquals(500, workflowResponse.getMessageCode());
        assertTrue(workflowResponse.getResponse().startsWith("Error occurred while executing the process:"));
        assertEquals("Fail", workflowResponse.getMessage());


    }

}
