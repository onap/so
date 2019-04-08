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



import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContext;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.common.workflow.context.WorkflowResponse;


public class WorkflowContextHolderTest {


    @Test
    public void testProcessCallback() throws Exception {
        String requestId = UUID.randomUUID().toString();
        String message = "TEST MESSATGE";
        String responseMessage = "Successfully processed request";
        int testCode = 200;


        WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();

        WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
        callbackResponse.setMessage(message);
        callbackResponse.setResponse(responseMessage);
        callbackResponse.setStatusCode(testCode);

        contextHolder.processCallback("testAsyncProcess", "process-instance-id", requestId, callbackResponse);

        // same object returned
        WorkflowContext contextFound = contextHolder.getWorkflowContext(requestId);
        if (contextFound == null)
            throw new Exception("Expected to find Context Object");

        WorkflowResponse testResponse = contextFound.getWorkflowResponse();
        Assert.assertEquals(200, testResponse.getMessageCode());
        Assert.assertEquals(message, testResponse.getMessage());
        Assert.assertEquals(responseMessage, testResponse.getResponse());



    }

}
