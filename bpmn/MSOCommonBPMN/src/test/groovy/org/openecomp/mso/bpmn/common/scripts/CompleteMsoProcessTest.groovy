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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.core.WorkflowException

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class CompleteMsoProcessTest {
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this)
	}

	private String completeMsoProcessRequest = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:ns="http://org.openecomp/mso/request/types/v1" xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
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

	@Test
	public void testPreProcessRequest() {

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("CompleteMsoProcessRequest")).thenReturn(completeMsoProcessRequest)
		when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
		when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

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
						<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.openecomp.mso/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateInfraRequest>
						         <requestId>testReqId</requestId>
						         <lastModifiedBy>BPEL</lastModifiedBy>
						         <statusMessage>Resource Completed Successfully</statusMessage>
						         <requestStatus>COMPLETE</requestStatus>
								 <progress>100</progress>
								 
						      </req:updateInfraRequest>
						   </soapenv:Body>
						</soapenv:Envelope>"""

	@Test
	public void testsetUpdateDBstatustoSuccessPayload(){

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("CMSO_request_id")).thenReturn("testReqId")
		when(mockExecution.getVariable("CMSO_mso-bpel-name")).thenReturn("BPEL")
		when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
		when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

		CompleteMsoProcess completeMsoProcess = new CompleteMsoProcess()
		completeMsoProcess.setUpdateDBstatustoSuccessPayload(mockExecution)

		verify(mockExecution).setVariable("CMSO_setUpdateDBstatustoSuccessPayload",setUpdateDBstatustoSuccessPayload)
	}

	private String msoCompletionResponse = """onse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
   <sdncadapterworkflow:out>BPEL BPEL-NAME FAILED</sdncadapterworkflow:out>
</sdncadapterworkflow:MsoCompletionResponse>"""

/*
	private String msoCompletionResponse = """<sdncadapterworkflow:MsoCompletionResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
   <sdncadapterworkflow:out>BPEL BPEL-NAME FAILED</sdncadapterworkflow:out>
</sdncadapterworkflow:MsoCompletionResponse>"""
*/
	@Test
    void testBuildDataError() {
		// given
		def message = "Some-Message"

		def mockExecution = mock ExecutionEntity.class
		when mockExecution.getVariable("CMSO_mso-bpel-name") thenReturn "BPEL-NAME"
		when mockExecution.getVariable("testProcessKey") thenReturn "CompleteMsoProcess"

		def completeMsoProcess = new CompleteMsoProcess()
		// when
		assertThatThrownBy { completeMsoProcess.buildDataError(mockExecution, message) } isInstanceOf BpmnError
		// then
		verify mockExecution setVariable("CompleteMsoProcessResponse", msoCompletionResponse)
		def argumentCaptor = ArgumentCaptor.forClass WorkflowException.class
		verify mockExecution setVariable(eq("WorkflowException"), argumentCaptor.capture())
		def capturedException = argumentCaptor.value

		assertThat capturedException.processKey isEqualTo "CompleteMsoProcess"
		assertThat capturedException.errorCode isEqualTo 500
		assertThat capturedException.errorMessage isEqualTo message
    }
}