/*- 
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
 * ================================================================================ 
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved. 
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.onap.so.bpmn.common.BPMNUtil.waitForWorkflowToFinish;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for FalloutHandler.bpmn.
 */

public class FalloutHandlerIT extends BaseIntegrationTest {
	
	Logger logger = LoggerFactory.getLogger(FalloutHandlerIT.class);
	
	
	private void setupMocks() {
		stubFor(post(urlEqualTo("/dbadapters/AttRequestsDbAdapter"))
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
	
	private String executeFlow(String inputRequestFile) throws InterruptedException {	
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("FalloutHandlerRequest",inputRequestFile);		variables.put("mso-request-id", UUID.randomUUID().toString());
		String processId = invokeSubProcess( "FalloutHandler", variables);
		waitForWorkflowToFinish(processEngine,processId);
		logEnd();
		return processId;
	}	
	
	@Test		
	public void msoFalloutHandlerWithNotificationurl_200() throws Exception{		
		//Setup Mocks
		setupMocks();
		//Execute Flow
		String processId = executeFlow(gMsoFalloutHandlerWithNotificationurl());
		//Verify Error
		String FH_ResponseCode = BPMNUtil.getVariable(processEngine, "FalloutHandler", "FH_ResponseCode",processId);
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "FalloutHandler", "FH_SuccessIndicator",processId)); 
	}
	
	public String gMsoFalloutHandlerWithNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns7=\"http://org.onap/so/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id>1020_STUW105_5002</ns7:request-id>"
				+ "			<ns7:request-action>requestAction</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>source</ns7:source>"
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
	public void msoFalloutHandlerWithNoNotificationurl() throws Exception{
	
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNoNotificationurl());
		//Verify Error
		String FH_ResponseCode = BPMNUtil.getVariable(processEngine, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "FalloutHandler", "FH_SuccessIndicator")); 
	}
	
	public String gMsoFalloutHandlerWithNoNotificationurl() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns7=\"http://org.onap/so/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id>1020_STUW105_5002</ns7:request-id>"
				+ "			<ns7:request-action>requestAction</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>source</ns7:source>"
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
	
	public void msoFalloutHandlerWithNotificationurlNoRequestId() throws Exception{
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		logger.debug("STARTED TEST: {}", method);
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNotificationurlNoRequestId());
		//Verify Error		
		String FH_ResponseCode = BPMNUtil.getVariable(processEngine, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "FalloutHandler", "FH_SuccessIndicator")); 
	}

	public String gMsoFalloutHandlerWithNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns7=\"http://org.onap/so/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id></ns7:request-id>"
				+ "			<ns7:request-action>requestAction</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>source</ns7:source>"
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
	
	public void msoFalloutHandlerWithNoNotificationurlNoRequestId() throws Exception{
		String method = getClass().getSimpleName() + "." + new Object() {
		}.getClass().getEnclosingMethod().getName();
		logger.debug("STARTED TEST: {}", method);
		//Setup Mocks
		setupMocks();
		//Execute Flow
		executeFlow(gMsoFalloutHandlerWithNoNotificationurlNoRequestId());
		//Verify Error
		String FH_ResponseCode = BPMNUtil.getVariable(processEngine, "FalloutHandler", "FH_ResponseCode");
		Assert.assertEquals("200", FH_ResponseCode);
		Assert.assertTrue((boolean) BPMNUtil.getRawVariable(processEngine, "FalloutHandler", "FH_SuccessIndicator")); 
	}	
	
	public String gMsoFalloutHandlerWithNoNotificationurlNoRequestId() {
		//Generated the below XML from ActiveVOS moduler ... Using the generate sample XML feature in ActiveVOS
		String xml = ""
				+ "<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow=\"http://org.onap/so/workflow/schema/v1\" xmlns:ns7=\"http://org.onap/so/request/types/v1\">"
				+ "		<ns7:request-information>"
				+ "			<ns7:request-id></ns7:request-id>"
				+ "			<ns7:request-action>requestAction</ns7:request-action>"
				+ "			<ns7:request-sub-action>CANCEL</ns7:request-sub-action>"
				+ "			<ns7:source>source</ns7:source>"
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

