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

import static org.onap.so.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.onap.so.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;

/**
 * Unit test for CompleteMsoProcess.bpmn.
 */

public class CompleteMsoProcessIT extends BaseIntegrationTest {
	
	private void executeFlow(String inputRequestFile) throws InterruptedException {
		mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");
		Map<String, Object> variables = new HashMap<>();
		variables.put("CompleteMsoProcessRequest",inputRequestFile);
		variables.put("mso-request-id", UUID.randomUUID().toString());
		String processId = invokeSubProcess( "CompleteMsoProcess", variables);
		waitForWorkflowToFinish(processEngine,processId);
		logEnd();
	}	
	
	@Test
	public void msoCompletionRequestWithNotificationUrl_200() throws Exception {
		logStart();	
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNotificationurl());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngine, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}

	@Test
	public void msoCompletionRequestWithNoNotificationurl() throws Exception {
		logStart();	
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNoNotificationurl());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngine, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);	
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}

	@Test		
	public void msoCompletionRequestWithNotificationurlNoRequestId() throws Exception {
		logStart();	
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNotificationurlNoRequestId());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngine, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();	
	}
	
	@Test		
	public void msoCompletionRequestWithNoNotificationurlNoRequestId() throws Exception {
		logStart();
		
		//Execute Flow
		executeFlow(gMsoCompletionRequestWithNoNotificationurlNoRequestId());
		
		//Verify Error
		String CMSO_ResponseCode = BPMNUtil.getVariable(processEngine, "CompleteMsoProcess", "CMSO_ResponseCode");
		Assert.assertEquals("200", CMSO_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "CompleteMsoProcess", "CMSO_SuccessIndicator"));
		logEnd();
	}	

	public String gMsoCompletionRequestWithNotificationurl() {		
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://ecomp.openecomp.org.com/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id>STUW105_5002</ns:request-id>"
				+ "			<ns:request-action>RequestAction</ns:request-action>"				
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>SOURCE</ns:source>"
				+ "			<ns:notification-url>https://t3nap1a1.snt.bst.bls.com:9004/sdncontroller-sdncontroller-inbound-ws-war/sdncontroller-sdncontroller-inbound-ws.wsdl</ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}
		


	public String gMsoCompletionRequestWithNoNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id>STUW105_5002</ns:request-id>"
				+ "			<ns:request-action>RequestAction</ns:request-action>"				
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>SOURCE</ns:source>"
				+ "			<ns:notification-url></ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}
	
	public String gMsoCompletionRequestWithNoNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id></ns:request-id>"
				+ "			<ns:request-action>RequestAction</ns:request-action>"
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>SOURCE</ns:source>"
				+ "			<ns:notification-url></ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}	
	
	public String gMsoCompletionRequestWithNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:MsoCompletionRequest xmlns:ns=\"http://openecomp.org/mso/request/types/v1\" xmlns:sdncadapterworkflow=\"http://openecomp.org/mso/workflow/schema/v1\">"
				+ "		<ns:request-information>"
				+ "			<ns:request-id></ns:request-id>"
				+ "			<ns:request-action>RequestAction</ns:request-action>"				
				+ "			<ns:request-sub-action>COMPLETE</ns:request-sub-action>"
				+ "			<ns:source>SOURCE</ns:source>"
				+ "			<ns:notification-url>https://t3nap1a1.snt.bst.bls.com:9004/sdncontroller-sdncontroller-inbound-ws-war/sdncontroller-sdncontroller-inbound-ws.wsdl</ns:notification-url>"				
				+ "			<ns:order-number>10205000</ns:order-number>"				
				+ "			<ns:order-version>1</ns:order-version>"
				+ "		</ns:request-information>"				
				+ "		<sdncadapterworkflow:mso-bpel-name>BPELNAME</sdncadapterworkflow:mso-bpel-name>"
				+ "</sdncadapterworkflow:MsoCompletionRequest>";
		
		return xml;
	}	
}

