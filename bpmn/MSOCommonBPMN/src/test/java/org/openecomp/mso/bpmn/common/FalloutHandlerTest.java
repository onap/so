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

import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Unit test for FalloutHandler.bpmn.
 */
public class FalloutHandlerTest extends WorkflowTest {
	private void setupMocks() {
		stubFor(post(urlEqualTo("/dbadapters/MsoRequestsDbAdapter"))
				.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBody("<DbTag>Notified</DbTag>")));
		stubFor(post(urlEqualTo("/dbadapters/RequestsDbAdapter"))
				.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBody("<DbTag>Notified</DbTag>")));
	}	
	
	private void executeFlow(String inputRequestFile) throws InterruptedException {
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		System.out.println("STARTED TEST: " + method);
        
		//String changeFeatureActivateRequest = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/ChangeFeatureActivateV1/" + inputRequestFile);
		Map<String, String> variables = new HashMap<>();
		variables.put("FalloutHandlerRequest",inputRequestFile);
		
		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "FalloutHandler", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
		System.out.println("ENDED TEST: " + method);
	}	
	
	@Test		
	@Deployment(resources = {"subprocess/FalloutHandler.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoFalloutHandlerWithNotificationurl_200() throws Exception{
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		System.out.println("STARTED TEST: " + method);
		
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNotificationurl());
		//Verify Error
		String FH_ResponseCode = BPMNUtil.getVariable(processEngineRule, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "FalloutHandler", "FH_SuccessIndicator")); 
	}
	
	public String gMsoFalloutHandlerWithNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns7=\"http://org.openecomp/mso/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id>uCPE1020_STUW105_5002</ns7:request-id>"
				+ "			<ns7:request-action>Layer3ServiceActivateRequest</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>OMX</ns7:source>"
				+ "			<ns7:notification-url>http://localhost:28090/CCD/StatusNotification</ns7:notification-url>"
				+ "			<ns7:order-number>10205000</ns7:order-number>"
				+ "			<ns7:order-version>1</ns7:order-version>"
				+ "		</ns7:request-information>"
				+ "		<sdncadapterworkflow:WorkflowException>"
				+ "			<sdncadapterworkflow:ErrorMessage>Some Error Message - Fallout Handler</sdncadapterworkflow:ErrorMessage>"
				+ "			<sdncadapterworkflow:ErrorCode>Some Error Code - Fallout Handler</sdncadapterworkflow:ErrorCode>"
				+ "			<sdncadapterworkflow:SourceSystemErrorCode>Some Source System Error Code- Fallout Handler</sdncadapterworkflow:SourceSystemErrorCode>"
				+ "		</sdncadapterworkflow:WorkflowException>"
				+ "</sdncadapterworkflow:FalloutHandlerRequest>";
		
		return xml;

	}	
	



	@Test		
	@Deployment(resources = {"subprocess/FalloutHandler.bpmn"})
	public void msoFalloutHandlerWithNoNotificationurl() throws Exception{
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		System.out.println("STARTED TEST: " + method);		
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNoNotificationurl());
		//Verify Error
		String FH_ResponseCode = BPMNUtil.getVariable(processEngineRule, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "FalloutHandler", "FH_SuccessIndicator")); 
	}
	
	public String gMsoFalloutHandlerWithNoNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns7=\"http://org.openecomp/mso/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id>uCPE1020_STUW105_5002</ns7:request-id>"
				+ "			<ns7:request-action>Layer3ServiceActivateRequest</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>OMX</ns7:source>"
				+ "			<ns7:notification-url></ns7:notification-url>"
				+ "			<ns7:order-number>10205000</ns7:order-number>"
				+ "			<ns7:order-version>1</ns7:order-version>"
				+ "		</ns7:request-information>"
				+ "		<sdncadapterworkflow:WorkflowException>"
				+ "			<sdncadapterworkflow:ErrorMessage>Some Error Message - Fallout Handler</sdncadapterworkflow:ErrorMessage>"
				+ "			<sdncadapterworkflow:ErrorCode>Some Error Code - Fallout Handler</sdncadapterworkflow:ErrorCode>"
				+ "			<sdncadapterworkflow:SourceSystemErrorCode>Some Source System Error Code- Fallout Handler</sdncadapterworkflow:SourceSystemErrorCode>"
				+ "		</sdncadapterworkflow:WorkflowException>"
				+ "</sdncadapterworkflow:FalloutHandlerRequest>";
		
		return xml;
	}	
	
	@Test		
	@Deployment(resources = {"subprocess/FalloutHandler.bpmn",
			"subprocess/GenericNotificationService.bpmn"
			})
	public void msoFalloutHandlerWithNotificationurlNoRequestId() throws Exception{
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		System.out.println("STARTED TEST: " + method);		
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNotificationurlNoRequestId());
		//Verify Error		
		String FH_ResponseCode = BPMNUtil.getVariable(processEngineRule, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "FalloutHandler", "FH_SuccessIndicator")); 
	}

	public String gMsoFalloutHandlerWithNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns7=\"http://org.openecomp/mso/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id></ns7:request-id>"
				+ "			<ns7:request-action>Layer3ServiceActivateRequest</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>OMX</ns7:source>"
				+ "			<ns7:notification-url>www.test.com</ns7:notification-url>"
				+ "			<ns7:order-number>10205000</ns7:order-number>"
				+ "			<ns7:order-version>1</ns7:order-version>"
				+ "		</ns7:request-information>"
				+ "		<sdncadapterworkflow:WorkflowException>"
				+ "			<sdncadapterworkflow:ErrorMessage>Some Error Message - Fallout Handler</sdncadapterworkflow:ErrorMessage>"
				+ "			<sdncadapterworkflow:ErrorCode>Some Error Code - Fallout Handler</sdncadapterworkflow:ErrorCode>"
				+ "			<sdncadapterworkflow:SourceSystemErrorCode>Some Source System Error Code- Fallout Handler</sdncadapterworkflow:SourceSystemErrorCode>"
				+ "		</sdncadapterworkflow:WorkflowException>"
				+ "</sdncadapterworkflow:FalloutHandlerRequest>";
		
		return xml;
	}		
	
	@Test		
	@Deployment(resources = {"subprocess/FalloutHandler.bpmn"})
	public void msoFalloutHandlerWithNoNotificationurlNoRequestId() throws Exception{
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		System.out.println("STARTED TEST: " + method);		
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNoNotificationurlNoRequestId());
		//Verify Error
		String FH_ResponseCode = BPMNUtil.getVariable(processEngineRule, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngineRule, "FalloutHandler", "FH_SuccessIndicator")); 
	}	
	
	public String gMsoFalloutHandlerWithNoNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns7=\"http://org.openecomp/mso/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id></ns7:request-id>"
				+ "			<ns7:request-action>Layer3ServiceActivateRequest</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>OMX</ns7:source>"
				+ "			<ns7:notification-url></ns7:notification-url>"
				+ "			<ns7:order-number>10205000</ns7:order-number>"
				+ "			<ns7:order-version>1</ns7:order-version>"
				+ "		</ns7:request-information>"
				+ "		<sdncadapterworkflow:WorkflowException>"
				+ "			<sdncadapterworkflow:ErrorMessage>Some Error Message - Fallout Handler</sdncadapterworkflow:ErrorMessage>"
				+ "			<sdncadapterworkflow:ErrorCode>Some Error Code - Fallout Handler</sdncadapterworkflow:ErrorCode>"
				+ "			<sdncadapterworkflow:SourceSystemErrorCode>Some Source System Error Code- Fallout Handler</sdncadapterworkflow:SourceSystemErrorCode>"
				+ "		</sdncadapterworkflow:WorkflowException>"
				+ "</sdncadapterworkflow:FalloutHandlerRequest>";
		
		return xml;
	}	
	
}

