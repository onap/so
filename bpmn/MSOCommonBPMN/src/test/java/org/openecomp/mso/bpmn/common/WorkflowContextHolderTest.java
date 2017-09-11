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

package org.openecomp.mso.bpmn.common;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.AsynchronousResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowCallbackResponse;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowContext;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowContextHolder;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

public class WorkflowContextHolderTest {

	private WorkflowContext createContext(AsynchronousResponse asyncResponse) {
		WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();
		String requestId = UUID.randomUUID().toString();
		WorkflowContext workflowContext = new WorkflowContext("testAsyncProcess",
			requestId, asyncResponse, 1000L);
		contextHolder.put(workflowContext);
		return workflowContext;
	}

	@Test
	@Ignore // BROKEN TEST (previously ignored)
	public void testContextExpiry() throws InterruptedException {

		WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();
		AsynchronousResponse asyncResponse = mock(AsynchronousResponse.class);
		WorkflowContext workflowContext = createContext(asyncResponse);
		String requestId = workflowContext.getRequestId();
		WorkflowContext context1 = contextHolder.getWorkflowContext(requestId);

		Assert.assertNotNull(context1);
		Assert.assertEquals(requestId, context1.getRequestId());
		Assert.assertEquals(workflowContext.getProcessKey(), context1.getProcessKey());
		Assert.assertEquals(workflowContext.getStartTime(), context1.getStartTime());

		Thread.sleep(1000);
		//context should not be available after a second
		WorkflowContext context2 = contextHolder.getWorkflowContext(requestId);
		Assert.assertNull(context2);
	}

	@Test
	public void testProcessCallback() {
		WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();
		AsynchronousResponse asyncResponse = mock(AsynchronousResponse.class);
		WorkflowContext workflowContext = createContext(asyncResponse);

		WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
		callbackResponse.setMessage("Success");
		callbackResponse.setResponse("Successfully processed request");
		callbackResponse.setStatusCode(200);

		Response response = contextHolder.processCallback("testAsyncProcess",
			"process-instance-id", workflowContext.getRequestId(),
			callbackResponse);
		WorkflowResponse response1 = (WorkflowResponse) response.getEntity();
		Assert.assertNotNull(response1.getMessage());
		Assert.assertEquals(200,response1.getMessageCode());
		Assert.assertEquals("Success", response1.getMessage());
		Assert.assertEquals("Successfully processed request", response1.getResponse());
		verify(asyncResponse).setResponse(any(Response.class));

		WorkflowContext context1 = contextHolder.getWorkflowContext(workflowContext.getRequestId());
		Assert.assertNull(context1);
	}

}
