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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class CompleteMsoProcessTest {
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this)
	}

	private String completeMsoProcessRequest = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:ns="http://org.onap/so/request/types/v1" xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
						<ns:request-information>
							<ns:request-id>uCPE1020_STUW105_5002</ns:request-id>
							<ns:request-action>Layer3ServiceActivateRequest</ns:request-action>				
							<ns:request-sub-action>COMPLETE</ns:request-sub-action>
							<ns:source>OMX</ns:source>
							<ns:notification-url>http://localhost:28090/CCD/StatusNotification</ns:notification-url>				
							<ns:order-number>10205000</ns:order-number>				
							<ns:order-version>1</ns:order-version>
						</ns:request-information>				
						<sdncadapterworkflow:mso-bpel-name>UCPELayer3ServiceActivateV1</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
		"""
	
		private String completeMsoNetworkProcessRequest = """
					<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>bd631913-cfc6-488b-ba22-6b98504f703d</request-id>
							<action>CREATE</action>
							<source>VID</source>
			   			</request-info>
						<aetgt:status-message>Resource Completed Successfully</aetgt:status-message>
                        <aetgt:networkId>bd631913-cfc6-488b-ba22-6b98504f703d</aetgt:networkId>
			   			<aetgt:mso-bpel-name>BPMN Network action: CREATE</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

	@Test
	public void testPreProcessRequest() {

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("CompleteMsoProcessRequest")).thenReturn(completeMsoProcessRequest)
		when(mockExecution.getVariable("mso.adapters.db.auth")).thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C");
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7");

		CompleteMsoProcess completeMsoProcess = new CompleteMsoProcess()
		completeMsoProcess.preProcessRequest(mockExecution)

		/* Initialize all the process request variables in this block */
		verify(mockExecution).setVariable("prefix","CMSO_")
		//verify(mockExecution).setVariable("getLayer3ServiceDetailsV1Response","")
		verify(mockExecution).setVariable("CMSO_request_id","")
		verify(mockExecution).setVariable("CMSO_notification-url","")
		verify(mockExecution).setVariable("CMSO_mso-bpel-name","")
		verify(mockExecution).setVariable("CMSO_request_action","")

		verify(mockExecution).setVariable("CMSO_notification-url-Ok", false)
		verify(mockExecution).setVariable("CMSO_request_id-Ok", false)

		//updateRequest Adapter process variables
		verify(mockExecution).setVariable("CMSO_updateRequestResponse", "")
		verify(mockExecution).setVariable("CMSO_updateRequestResponseCode", "")
		verify(mockExecution).setVariable("CMSO_updateFinalNotifyAckStatusFailedPayload", "")

		//Set DB adapter variables here
		verify(mockExecution).setVariable("CMSO_updateDBStatusToSuccessPayload", "")
		verify(mockExecution).setVariable("CMSO_updateInfraRequestDBPayload", "")
		verify(mockExecution).setVariable("CMSO_setUpdateDBstatustoSuccessPayload", "")

		//Auth variables
		verify(mockExecution).setVariable("BasicAuthHeaderValue","")

		//Response variables
		verify(mockExecution).setVariable("CompletionHandlerResponse","")
		verify(mockExecution).setVariable("CMSO_ErrorResponse", null)
		verify(mockExecution).setVariable("CMSO_ResponseCode", "")

		verify(mockExecution).setVariable("CMSO_notification-url-Ok",true)
		verify(mockExecution).setVariable("CMSO_request_id-Ok",true)
		verify(mockExecution).setVariable("CMSO_notification-url","http://localhost:28090/CCD/StatusNotification")
		verify(mockExecution).setVariable("CMSO_request_id","uCPE1020_STUW105_5002")
		verify(mockExecution).setVariable("CMSO_request_action","Layer3ServiceActivateRequest")
		verify(mockExecution).setVariable("CMSO_source","OMX")

	}

	private String setUpdateDBstatustoSuccessPayload = """
						<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateInfraRequest>
						         <requestId>testReqId</requestId>
						         <lastModifiedBy>BPEL</lastModifiedBy>
						         <statusMessage>Resource Completed Successfully</statusMessage>
						         <requestStatus>COMPLETE</requestStatus>
								 <progress>100</progress>
								 <networkId>bd631913-cfc6-488b-ba22-6b98504f703d</networkId>
						      </req:updateInfraRequest>
						   </soapenv:Body>
						</soapenv:Envelope>"""

	@Test
	public void testsetUpdateDBstatustoSuccessPayload(){

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("CMSO_request_id")).thenReturn("testReqId")
		when(mockExecution.getVariable("CMSO_mso-bpel-name")).thenReturn("BPEL")
		when(mockExecution.getVariable("mso.adapters.db.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC");
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7");
		when(mockExecution.getVariable("CompleteMsoProcessRequest")).thenReturn(completeMsoNetworkProcessRequest);
		
		CompleteMsoProcess completeMsoProcess = new CompleteMsoProcess()
		completeMsoProcess.setUpdateDBstatustoSuccessPayload(mockExecution)

		verify(mockExecution).setVariable("CMSO_setUpdateDBstatustoSuccessPayload",setUpdateDBstatustoSuccessPayload)
	}

	private String msoCompletionResponse = """onse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
   <sdncadapterworkflow:out>BPEL BPEL-NAME FAILED</sdncadapterworkflow:out>
</sdncadapterworkflow:MsoCompletionResponse>"""


	@Test
	void postProcessResponse_successful() {
		DelegateExecution mockExecution = mock(DelegateExecution.class)
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("CMSO_mso-bpel-name")).thenReturn("mso-bpel-test")
		new CompleteMsoProcess().postProcessResponse(mockExecution)

		String expectedResponse = "<sdncadapterworkflow:MsoCompletionResponse xmlns:sdncadapterworkflow=\"http://ecomp.com/mso/workflow/schema/v1\">\n" +
				"  <sdncadapterworkflow:out>BPEL mso-bpel-test completed</sdncadapterworkflow:out>\n" +
				"</sdncadapterworkflow:MsoCompletionResponse>"

		verify(mockExecution).setVariable("WorkflowResponse", expectedResponse)
		verify(mockExecution).setVariable("CompleteMsoProcessResponse", expectedResponse)
		verify(mockExecution).setVariable("CMSO_ResponseCode", "200")
	}
}