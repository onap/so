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

import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.bpmn.common.adapter.sdnc.CallbackHeader;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterResponse;
import org.openecomp.mso.bpmn.common.workflow.service.SDNCAdapterCallbackServiceImpl;
import org.openecomp.mso.bpmn.common.workflow.service.SDNCAdapterCallbackServiceImpl.SDNCAdapterErrorResponse;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResource;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit test cases for SDNCAdapterV1.bpmn
 */
public class SDNCAdapterV1Test extends WorkflowTest {

	private String sdncAdapterWorkflowRequest;
	private String sdncAdapterWorkflowRequestAct;
	private String sdncAdapterCallbackRequestData;
	private String sdncAdapterCallbackRequestDataNonfinal;

	public SDNCAdapterV1Test() throws IOException {
		sdncAdapterWorkflowRequest = FileUtil.readResourceFile("sdncadapterworkflowrequest.xml");
		sdncAdapterWorkflowRequestAct = FileUtil.readResourceFile("sdncadapterworkflowrequest-act.xml");
		sdncAdapterCallbackRequestData = FileUtil.readResourceFile("sdncadaptercallbackrequestdata.text");
		sdncAdapterCallbackRequestDataNonfinal = FileUtil.readResourceFile("sdncadaptercallbackrequestdata-nonfinal.text");
	}

	/**
	 * End-to-End flow - Unit test for SDNCAdapterV1.bpmn
	 *  - String input & String response
	 */

	private WorkflowResponse invokeFlow(String workflowRequest) {

		Map<String, Object>valueMap = new HashMap<>();
		valueMap.put("value", workflowRequest);
		Map<String, Object> variables = new HashMap<>();
		variables.put("sdncAdapterWorkflowRequest", valueMap);

		Map<String, Object> valueMap2 = new HashMap<>();
		valueMap2.put("value", "true");
		variables.put("isDebugLogEnabled", valueMap2);

		VariableMapImpl varMap = new VariableMapImpl();
		varMap.put("variables", variables);

		//System.out.println("Invoking the flow");

		WorkflowResource workflowResource = new WorkflowResource();
		workflowResource.setProcessEngineServices4junit(processEngineRule);

		Response response = workflowResource.startProcessInstanceByKey("sdncAdapter", varMap);
		WorkflowResponse workflowResponse = (WorkflowResponse) response.getEntity();

		//String pid = workflowResponse.getProcessInstanceID();
		//System.out.println("Back from executing process instance with pid=" + pid);
		return workflowResponse;
	}

	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void sunnyDay() throws InterruptedException {

		mockSDNCAdapter(200);

		//System.out.println("SDNCAdapter sunny day flow Started!");

		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequest);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		String pid = getPid();

		assertProcessInstanceNotFinished(pid);

		System.out.println("Injecting SDNC Adapter asynchronous callback message to continue processing");
		String generatedRequestId = (String) processEngineRule.getRuntimeService().getVariable(pid, "SDNCA_requestId");
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(generatedRequestId);
		callbackHeader.setResponseCode("200");
		callbackHeader.setResponseMessage("OK");
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestData);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertFalse(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertProcessInstanceFinished(pid);

		//System.out.println("SDNCAdapter sunny day flow Completed!");
	}

	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void nonFinalWithTimeout() throws InterruptedException {

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBAdapter.xml");

		//System.out.println("SDNCAdapter interim status processing flow Started!");

		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequestAct);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		String pid = getPid();

		assertProcessInstanceNotFinished(pid);

		//System.out.println("Injecting SDNC Adapter asynchronous callback message to continue processing");
		String generatedRequestId = (String) processEngineRule.getRuntimeService().getVariable(pid, "SDNCA_requestId");
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(generatedRequestId);
		callbackHeader.setResponseCode("200");
		callbackHeader.setResponseMessage("OK");
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestDataNonfinal);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertFalse(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertProcessInstanceNotFinished(pid);

		checkForTimeout(pid);

		assertEquals(true, (Boolean) (getVariable(pid, "continueListening")));
		assertEquals(false, (Boolean) (getVariable(pid, "SDNCA_InterimNotify")));


		//System.out.println("SDNCAdapter interim status processing flow Completed!");
	}

	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void nonFinalThenFinal() throws InterruptedException {

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBAdapter.xml");

		//System.out.println("SDNCAdapter non-final then final processing flow Started!");

		// Start the flow
		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequestAct);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		String pid = getPid();

		assertProcessInstanceNotFinished(pid);

		// Inject a "non-final" SDNC Adapter asynchronous callback message
		//System.out.println("Injecting SDNC Adapter asynchronous callback message to continue processing");
		String generatedRequestId = (String) processEngineRule.getRuntimeService().getVariable(pid, "SDNCA_requestId");
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(generatedRequestId);
		callbackHeader.setResponseCode("200");
		callbackHeader.setResponseMessage("OK");
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestDataNonfinal);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertFalse(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertProcessInstanceNotFinished(pid);
		assertEquals(true, (Boolean) (getVariable(pid, "continueListening")));
		assertEquals(false, (Boolean) (getVariable(pid, "SDNCA_InterimNotify")));

		// Inject a "final" SDNC Adapter asynchronous callback message
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestData);
		sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertFalse(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertProcessInstanceFinished(pid);
		assertEquals(false, (Boolean) (getVariable(pid, "continueListening")));
		assertEquals(false, (Boolean) (getVariable(pid, "SDNCA_InterimNotify")));

		//System.out.println("SDNCAdapter non-final then final processing flow Completed!");
	}


	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void nonFinalThenFinalWithNotify() throws InterruptedException {

		mockSDNCAdapter(200);
		mockUpdateRequestDB(200, "Database/DBAdapter.xml");

		//System.out.println("SDNCAdapter non-final then final processing flow Started!");

		String modSdncAdapterWorkflowRequestAct = sdncAdapterWorkflowRequestAct;
		try {
			// only service-type "uCPE-VMS" is applicable to notification, so modify the test request
			modSdncAdapterWorkflowRequestAct = XmlTool.modifyElement(sdncAdapterWorkflowRequestAct, "tag0:service-type", "uCPE-VMS").get();
			System.out.println("modified request: " + modSdncAdapterWorkflowRequestAct);
		} catch (Exception e) {
			System.out.println("request modification failed");
			//e.printStackTrace();
		}

		// Start the flow
		ProcessExecutionThread thread = new ProcessExecutionThread(modSdncAdapterWorkflowRequestAct);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		String pid = getPid();

		assertProcessInstanceNotFinished(pid);

		// Inject a "non-final" SDNC Adapter asynchronous callback message
		//System.out.println("Injecting SDNC Adapter asynchronous callback message to continue processing");
		String generatedRequestId = (String) processEngineRule.getRuntimeService().getVariable(pid, "SDNCA_requestId");
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(generatedRequestId);
		callbackHeader.setResponseCode("200");
		callbackHeader.setResponseMessage("OK");
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestDataNonfinal);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertFalse(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertProcessInstanceNotFinished(pid);
		assertEquals(true, (Boolean) (getVariable(pid, "continueListening")));
		assertEquals(true, (Boolean) (getVariable(pid, "SDNCA_InterimNotify")));

		// Inject a "final" SDNC Adapter asynchronous callback message
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestData);
		sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertFalse(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertProcessInstanceFinished(pid);
		assertEquals(false, (Boolean) (getVariable(pid, "continueListening")));
		assertEquals(false, (Boolean) (getVariable(pid, "SDNCA_InterimNotify")));

		//System.out.println("SDNCAdapter non-final then final processing flow Completed!");
	}


	private void waitForExecutionToStart(String processDefintion, int count) throws InterruptedException {
		//System.out.println(processEngineRule.getRuntimeService().createExecutionQuery().processDefinitionKey(processDefintion).count());
		while (processEngineRule.getRuntimeService().createExecutionQuery().processDefinitionKey(processDefintion).count() != count) {
			Thread.sleep(200);
		}
	}

	@Test
	@Ignore // Ignored because PropertyConfigurationSetup is timing out
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void badCorrelationIdTest() throws InterruptedException, IOException {

		mockSDNCAdapter(200);

		Map<String, String> urnProperties = PropertyConfigurationSetup.createBpmnUrnProperties();
		urnProperties.put("mso.correlation.timeout", "5");
		PropertyConfigurationSetup.addProperties(urnProperties, 10000);

		//System.out.println("SDNCAdapter bad RequestId test Started!");

		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequest);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		String pid = getPid();
		assertProcessInstanceNotFinished(pid);

		//System.out.println("Injecting SDNC Adapter asynchronous callback message to continue processing");
		String badRequestId = "This is not the RequestId that was used";
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(badRequestId);
		callbackHeader.setResponseCode("200");
		callbackHeader.setResponseMessage("OK");
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		sdncAdapterCallbackRequest.setRequestData(sdncAdapterCallbackRequestData);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertTrue(sdncAdapterResponse instanceof SDNCAdapterErrorResponse);
		assertTrue(((SDNCAdapterErrorResponse) sdncAdapterResponse).getError().contains("No process is waiting for sdncAdapterCallbackRequest"));
		assertProcessInstanceNotFinished(pid);

		//System.out.println("SDNCAdapter bad RequestId test Completed!");
	}

	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void badSynchronousResponse() throws IOException, InterruptedException {

		mockSDNCAdapter(404);

		//System.out.println("SDNCAdapter bad synchronous response flow Started!");

		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequest);
		thread.start();
		while (thread.isAlive()) {
			Thread.sleep(200);
		}
		WorkflowResponse response = thread.workflowResponse;
		Assert.assertNotNull(response);
		Assert.assertEquals("404 error", response.getMessageCode(),7000);
//		assertProcessInstanceFinished(response.getProcessInstanceID());
		//System.out.println("SDNCAdapter bad synchronous response flow Completed!");
	}

	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void sdncNotFound() throws IOException, InterruptedException {
		mockSDNCAdapter(200);
		mockSDNCAdapter("/sdncAdapterMock/404", 400, "sdncCallbackErrorResponse.xml");

		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequest);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		String pid = getPid();

		//System.out.println("Injecting SDNC Adapter asynchronous callback message to continue processing");
		String generatedRequestId = (String) processEngineRule.getRuntimeService().getVariable(pid, "SDNCA_requestId");
		CallbackHeader callbackHeader = new CallbackHeader();
		callbackHeader.setRequestId(generatedRequestId);
		callbackHeader.setResponseCode("404");
		callbackHeader.setResponseMessage("Error processing request to SDNC. Not Found. https://sdncodl.it.us.aic.cip.com:8443/restconf/config/L3SDN-API:services/layer3-service-list/AS%2FVLXM%2F000199%2F%2FSB_INTERNET. SDNC Returned-[error-type:application, error-tag:data-missing, error-message:Request could not be completed because the relevant data model content does not exist.]");
		SDNCAdapterCallbackRequest sdncAdapterCallbackRequest = new SDNCAdapterCallbackRequest();
		sdncAdapterCallbackRequest.setCallbackHeader(callbackHeader);
		SDNCAdapterCallbackServiceImpl callbackService = new SDNCAdapterCallbackServiceImpl();
		callbackService.setProcessEngineServices4junit(processEngineRule);
		SDNCAdapterResponse sdncAdapterResponse = callbackService.sdncAdapterCallback(sdncAdapterCallbackRequest);
		//System.out.println("Back from executing process again");

		assertProcessInstanceFinished(pid);
		assertNotNull(sdncAdapterResponse);
		//TODO query history to see SDNCA_ResponseCode, SDNCA_ErrorResponse
		//System.out.println("SDNCAdapter SDNC Notfound test Completed!");
	}

	@Test
	@Deployment(resources = {"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void asynchronousMessageTimeout() throws IOException, InterruptedException {
		mockSDNCAdapter(200);
		//System.out.println("SDNCAdapter asynchronous message timeout flow Started!");
		ProcessExecutionThread thread = new ProcessExecutionThread(sdncAdapterWorkflowRequest);
		thread.start();
		waitForExecutionToStart("sdncAdapter", 3);
		checkForTimeout(getPid());
	}

	private void checkForTimeout(String pid) throws InterruptedException {

		assertProcessInstanceNotFinished(pid);

		ProcessEngineConfigurationImpl processEngineConfiguration =
			(ProcessEngineConfigurationImpl) processEngineRule.getProcessEngine().getProcessEngineConfiguration();
		assertTrue(processEngineConfiguration.getJobExecutor().isActive());

	    Job timerJob = processEngineRule.getManagementService().createJobQuery().processInstanceId(pid).singleResult();
	    assertNotNull(timerJob);

	    processEngineRule.getManagementService().executeJob(timerJob.getId());

	    assertProcessInstanceFinished(pid);

		//System.out.println("SDNCAdapter asynchronous message timeout flow Completed!");
	}

	class ProcessExecutionThread extends Thread {

		private String workflowRequest;
		private WorkflowResponse workflowResponse;

		public ProcessExecutionThread(String workflowRequest) {
			this.workflowRequest = workflowRequest;
		}

		public void run() {
			workflowResponse = invokeFlow(workflowRequest);
			workflowResponse.getProcessInstanceID();
		}
	}

	private String getPid() {
		return processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().list().get(0).getId();
	}

	private Object getVariable(String pid, String variableName) {
		try {
			return
				processEngineRule
					.getHistoryService()
					.createHistoricVariableInstanceQuery()
					.processInstanceId(pid).variableName(variableName)
					.singleResult()
					.getValue();
		} catch(Exception ex) {
			return null;
		}
	}

	private void assertProcessInstanceFinished(String pid) {
	    assertEquals(1, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(pid).finished().count());
	}

	private void assertProcessInstanceNotFinished(String pid) {
	    assertEquals(0, processEngineRule.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(pid).finished().count());
	}

}
