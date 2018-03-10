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

package org.openecomp.mso.bpmn.common.scripts

import org.junit.runner.RunWith
import static org.mockito.Mockito.*
import static org.junit.Assert.*

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.common.scripts.FalloutHandler

@RunWith(MockitoJUnitRunner.class)
class FalloutHandlerTest {
	
	public MsoUtils utils = new MsoUtils()
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this)
	}

	private String falloutHandlerRequest = """
				<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1" xmlns:ns7="http://org.openecomp/mso/request/types/v1">
						<ns7:request-information>
							<ns7:request-id>uCPE1020_STUW105_5002</ns7:request-id>
							<ns7:request-action>Layer3ServiceActivateRequest</ns7:request-action>
							<ns7:request-sub-action>CANCEL</ns7:request-sub-action>
							<ns7:source>OMX</ns7:source>
							<ns7:order-number>10205000</ns7:order-number>
							<ns7:order-version>1</ns7:order-version>
						</ns7:request-information>
						<sdncadapterworkflow:WorkflowException>
							<sdncadapterworkflow:ErrorMessage>Some Error Message - Fallout Handler</sdncadapterworkflow:ErrorMessage>
							<sdncadapterworkflow:ErrorCode>Some Error Code - Fallout Handler</sdncadapterworkflow:ErrorCode>
							<sdncadapterworkflow:SourceSystemErrorCode>Some Source System Error Code- Fallout Handler</sdncadapterworkflow:SourceSystemErrorCode>
						</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
		"""

	private String falloutHandlerResponse = """<workflow:FalloutHandlerResponse xmlns:workflow="http://org.openecomp/mso/workflow/schema/v1">
  <workflow:out>Fallout Handler Failed</workflow:out>
</workflow:FalloutHandlerResponse>"""

	@Test
	public void testPreProcessRequest() {

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)

		when(mockExecution.getVariable("FalloutHandlerRequest")).thenReturn(falloutHandlerRequest)
		when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
		when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

		FalloutHandler falloutHandler = new FalloutHandler()
		falloutHandler.preProcessRequest(mockExecution)

		/* Initialize all the process request variables in this block */
		verify(mockExecution).setVariable("prefix","FH_")
		//verify(mockExecution).setVariable("getLayer3ServiceDetailsV1Response","")

		//These variables are form the input Message to the BPMN
		verify(mockExecution).setVariable("FH_request_id","")
		verify(mockExecution).setVariable("FH_request_action","")
		verify(mockExecution).setVariable("FH_notification-url","")
		verify(mockExecution).setVariable("FH_mso-bpel-name","")
		verify(mockExecution).setVariable("FH_ErrorCode", "")
		verify(mockExecution).setVariable("FH_ErrorMessage", "")

		verify(mockExecution).setVariable("FH_notification-url-Ok", false)
		verify(mockExecution).setVariable("FH_request_id-Ok", false)

		//These variables are for Get Mso Aai Password Adapter
		verify(mockExecution).setVariable("FH_deliveryStatus", true)

		//update Response Status to pending ...Adapter variables
		verify(mockExecution).setVariable("FH_updateResponseStatusPayload", null)
		verify(mockExecution).setVariable("FH_updateResponseStatusResponse", null)

		//update Request Gamma ...Adapter variables
		verify(mockExecution).setVariable("FH_updateRequestGammaPayload", "")
		verify(mockExecution).setVariable("FH_updateRequestGammaResponse", null)
		verify(mockExecution).setVariable("FH_updateRequestGammaResponseCode", null)

		//update Request Infra ...Adapter variables
		verify(mockExecution).setVariable("FH_updateRequestInfraPayload", "")
		verify(mockExecution).setVariable("FH_updateRequestInfraResponse", null)
		verify(mockExecution).setVariable("FH_updateRequestInfraResponseCode", null)

		//assign False to success variable
		verify(mockExecution).setVariable("FH_success", true)

		//Set notify status to Failed variable
		verify(mockExecution).setVariable("FH_NOTIFY_STATUS", "SUCCESS")

		//Set DB update variable
		verify(mockExecution).setVariable("FH_updateRequestPayload", "")
		verify(mockExecution).setVariable("FH_updateRequestResponse", null)
		verify(mockExecution).setVariable("FH_updateRequestResponseCode", null)

		//Auth variables
		verify(mockExecution).setVariable("BasicAuthHeaderValue","")

		//Response variables
		verify(mockExecution).setVariable("FalloutHandlerResponse","")
		verify(mockExecution).setVariable("FH_ErrorResponse", null)
		verify(mockExecution).setVariable("FH_ResponseCode", "")

		verify(mockExecution).setVariable("FH_request_id-Ok",true)
		verify(mockExecution).setVariable("FH_request_id","uCPE1020_STUW105_5002")
		verify(mockExecution).setVariable("FH_request_action","Layer3ServiceActivateRequest")
		verify(mockExecution).setVariable("FH_source","OMX")
		verify(mockExecution).setVariable("FH_ErrorCode","Some Error Code - Fallout Handler")
		verify(mockExecution).setVariable("FH_ErrorMessage","Some Error Message - Fallout Handler")

	}

	@Test
	public void testpostProcessResponse(){

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("FH_success")).thenReturn(false)

		FalloutHandler falloutHandler = new FalloutHandler()
		falloutHandler.postProcessResponse(mockExecution)

		// Capture the arguments to setVariable
		ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class)
		ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class)
		
		verify(mockExecution, times(4)).setVariable(captor1.capture(), captor2.capture())
		List<String> arg2List = captor2.getAllValues()
		String payloadResponseActual = arg2List.get(1)
		
		assertEquals(falloutHandlerResponse.replaceAll("\\s+", ""), payloadResponseActual.replaceAll("\\s+", ""))
		
		verify(mockExecution).setVariable("FH_ResponseCode","500")
	}

	private String updateRequestPayload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			   <req:updateRequest>
				  <requestId>testReqId</requestId>
				  <lastModifiedBy>BPEL</lastModifiedBy>
				  <finalErrorMessage>ErrorMessage</finalErrorMessage>
				  <finalErrorCode>ErrorCode</finalErrorCode>
				  <status>FAILED</status>
				  <responseStatus>NotifyStatus</responseStatus>
			   </req:updateRequest>
			</soapenv:Body>
		 </soapenv:Envelope>
		"""
	
		@Test
		public void testupdateRequestPayload(){
	
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			when(mockExecution.getVariable("FH_request_id")).thenReturn("testReqId")
			when(mockExecution.getVariable("FH_ErrorMessage")).thenReturn("ErrorMessage")
			when(mockExecution.getVariable("FH_ErrorCode")).thenReturn("ErrorCode")
			when(mockExecution.getVariable("FH_NOTIFY_STATUS")).thenReturn("NotifyStatus")
	
			FalloutHandler falloutHandler = new FalloutHandler()
			falloutHandler.updateRequestPayload(mockExecution)
			
			// Capture the arguments to setVariable
			ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class)
			ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class)
			
			verify(mockExecution, times(1)).setVariable(captor1.capture(), captor2.capture())
			List<String> arg2List = captor2.getAllValues()
			String payloadRequestActual = arg2List.get(0)
			
			assertEquals(updateRequestPayload.replaceAll("\\s+", ""), payloadRequestActual.replaceAll("\\s+", ""))
		}
		
		private String updateRequestInfraPayload = """
							<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
							   <soapenv:Header/>
							   <soapenv:Body>
							      <req:updateInfraRequest>
							         <requestId>testReqId</requestId>
							         <lastModifiedBy>BPEL</lastModifiedBy>
									 <statusMessage>ErrorMessage</statusMessage>
									 <requestStatus>FAILED</requestStatus>
									 <progress>100</progress>
							      </req:updateInfraRequest>
							   </soapenv:Body>
							</soapenv:Envelope>
			"""
		
			@Test
			public void testupdateRequestInfraPayload(){
		
				ExecutionEntity mockExecution = mock(ExecutionEntity.class)
				when(mockExecution.getVariable("FH_request_id")).thenReturn("testReqId")
				when(mockExecution.getVariable("FH_ErrorMessage")).thenReturn("ErrorMessage")
		
				FalloutHandler falloutHandler = new FalloutHandler()
				falloutHandler.updateRequestInfraPayload(mockExecution)
				
				// Capture the arguments to setVariable
				ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class)
				ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class)
				
				verify(mockExecution, times(1)).setVariable(captor1.capture(), captor2.capture())
				List<String> arg2List = captor2.getAllValues()
				String payloadRequestActual = arg2List.get(0)
		
				assertEquals(updateRequestInfraPayload.replaceAll("\\s+", ""), payloadRequestActual.replaceAll("\\s+", ""))
			}
			
			private String updateRequestGammaPayload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			   <req:updateRequest>
				  <requestId>testReqId</requestId>
				  <lastModifiedBy>BPEL</lastModifiedBy>
				  <finalErrorMessage>ErrorMessage</finalErrorMessage>
				  <finalErrorCode>ErrorCode</finalErrorCode>
				  <status>FAILED</status>
			   </req:updateRequest>
			</soapenv:Body>
		 </soapenv:Envelope>
		"""
			
		@Test
		public void testupdateRequestGammaPayload(){
	
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			when(mockExecution.getVariable("FH_request_id")).thenReturn("testReqId")
			when(mockExecution.getVariable("FH_ErrorMessage")).thenReturn("ErrorMessage")
			when(mockExecution.getVariable("FH_ErrorCode")).thenReturn("ErrorCode")
			when(mockExecution.getVariable("URN_mso_default_adapter_namespace")).thenReturn("http://org.openecomp.mso")

			FalloutHandler falloutHandler = new FalloutHandler()
			falloutHandler.updateRequestGammaPayload(mockExecution)
	
			// Capture the arguments to setVariable
			ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class)
			ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class)
			
			verify(mockExecution, times(1)).setVariable(captor1.capture(), captor2.capture())
			List<String> arg2List = captor2.getAllValues()
			String payloadRequestActual = arg2List.get(0)
			
			assertEquals(updateRequestGammaPayload.replaceAll("\\s+", ""), payloadRequestActual.replaceAll("\\s+", ""))
		}
	
		
		String updateResponseStatusPayload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			   <req:updateResponseStatus>
				  <requestId>testReqId</requestId>
				  <lastModifiedBy>BPEL</lastModifiedBy>
				  <responseStatus>SENDING_FINAL_NOTIFY</responseStatus>
			   </req:updateResponseStatus>
			</soapenv:Body>
		 </soapenv:Envelope>
		"""
		
		@Test
		public void testupdateResponseStatusPayload(){
	
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			when(mockExecution.getVariable("FH_request_id")).thenReturn("testReqId")

			FalloutHandler falloutHandler = new FalloutHandler()
			falloutHandler.updateResponseStatusPayload(mockExecution)
			
			// Capture the arguments to setVariable
			ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class)
			ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class)
			
			verify(mockExecution, times(1)).setVariable(captor1.capture(), captor2.capture())
			List<String> arg2List = captor2.getAllValues()
			String payloadResponseActual = arg2List.get(0)
	
			assertEquals(updateResponseStatusPayload.replaceAll("\\s+", ""), payloadResponseActual.replaceAll("\\s+", ""))
		}

}