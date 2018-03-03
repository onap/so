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

import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Unit test for CompleteMsoProcess.bpmn.
 */
public class CompleteMsoProcessTest extends WorkflowTest {
	
	private void executeFlow(String inputRequestFile) throws InterruptedException {
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		//String changeFeatureActivateRequest = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/ChangeFeatureActivateV1/" + inputRequestFile);
		Map<String, String> variables = new HashMap<>();
		variables.put("CompleteMsoProcessRequest",inputRequestFile);
		
		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CompleteMsoProcess", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
		logEnd();
	}	
	
	@Test		
	@Deployment(resources = {"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoCompletionRequestWithNotificationurl_200() throws Exception {
		logStart();	
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNotificationurl());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngineRule, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}
	
	@Test		
	@Ignore // BROKEN TEST
	@Deployment(resources = {"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoCompletionRequestWithNotificationurl_500() throws Exception {
		logStart();
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNotificationurl());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngineRule, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("500", CMSO_ResponseCode);
		Assert.assertFalse((boolean) BPMNUtil.getRawVariable(processEngineRule, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}	

	@Test		
	@Deployment(resources = {"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoCompletionRequestWithNoNotificationurl() throws Exception {
		logStart();	
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNoNotificationurl());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngineRule, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);	
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}

	@Test		
	@Deployment(resources = {"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoCompletionRequestWithNotificationurlNoRequestId() throws Exception {
		logStart();	
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNotificationurlNoRequestId());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngineRule, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();	
	}
	
	@Test		
	@Deployment(resources = {"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoCompletionRequestWithNoNotificationurlNoRequestId() throws Exception {
		logStart();
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNoNotificationurlNoRequestId());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngineRule, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}	

	public String gMsoCompletionRequestWithNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://ecomp.openecomp.org.com/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id>uCPE1020_STUW105_5002</ns:request-id>"
				+ "			<ns:request-action>Layer3ServiceActivateRequest</ns:request-action>"				
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>OMX</ns:source>"
				+ "			<ns:notification-url>https://t3nap1a1.snt.bst.bls.com:9004/sdncontroller-sdncontroller-inbound-ws-war/sdncontroller-sdncontroller-inbound-ws.wsdl</ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>UCPELayer3ServiceActivateV1</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}
		


	public String gMsoCompletionRequestWithNoNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id>uCPE1020_STUW105_5002</ns:request-id>"
				+ "			<ns:request-action>Layer3ServiceActivateRequest</ns:request-action>"				
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>OMX</ns:source>"
				+ "			<ns:notification-url></ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>UCPELayer3ServiceActivateV1</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}
	
	public String gMsoCompletionRequestWithNoNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id></ns:request-id>"
				+ "			<ns:request-action>Layer3ServiceActivateRequest</ns:request-action>"
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>OMX</ns:source>"
				+ "			<ns:notification-url></ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>UCPELayer3ServiceActivateV1</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}	
	
	public String gMsoCompletionRequestWithNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id></ns:request-id>"
				+ "			<ns:request-action>Layer3ServiceActivateRequest</ns:request-action>"				
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>OMX</ns:source>"
				+ "			<ns:notification-url>https://t3nap1a1.snt.bst.bls.com:9004/sdncontroller-sdncontroller-inbound-ws-war/sdncontroller-sdncontroller-inbound-ws.wsdl</ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>UCPELayer3ServiceActivateV1</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}	
}

