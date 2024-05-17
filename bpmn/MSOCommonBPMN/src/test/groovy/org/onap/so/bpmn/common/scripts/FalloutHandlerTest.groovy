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

import static org.mockito.Mockito.*
import static org.junit.Assert.*;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor;
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.db.request.beans.InfraActiveRequests
import org.onap.so.db.request.client.RequestsDbClient

@RunWith(MockitoJUnitRunner.class)
class FalloutHandlerTest {

    public MsoUtils utils = new MsoUtils()

    @Spy
    FalloutHandler falloutHandler = new FalloutHandler()

    @Mock
    RequestsDbClient requestsDbClient;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    private String falloutHandlerRequest = """
				<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1" xmlns:ns7="http://org.onap/so/request/types/v1">
						<ns7:request-info>
							<ns7:request-id>uCPE1020_STUW105_5002</ns7:request-id>
							<ns7:request-action>Layer3ServiceActivateRequest</ns7:request-action>
							<ns7:request-sub-action>CANCEL</ns7:request-sub-action>
							<ns7:source>OMX</ns7:source>
							<ns7:order-number>10205000</ns7:order-number>
							<ns7:order-version>1</ns7:order-version>
						</ns7:request-info>
						<sdncadapterworkflow:WorkflowException>
							<sdncadapterworkflow:ErrorMessage>Some Error Message - Fallout Handler</sdncadapterworkflow:ErrorMessage>
							<sdncadapterworkflow:ErrorCode>Some Error Code - Fallout Handler</sdncadapterworkflow:ErrorCode>
							<sdncadapterworkflow:SourceSystemErrorCode>Some Source System Error Code- Fallout Handler</sdncadapterworkflow:SourceSystemErrorCode>
						</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
		"""

    private String falloutHandlerResponse = """<workflow:FalloutHandlerResponse xmlns:workflow="http://org.onap/so/workflow/schema/v1">
  <workflow:out>Fallout Handler Failed</workflow:out>
</workflow:FalloutHandlerResponse>"""

    @Test
    public void testPreProcessRequest() {

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        when(mockExecution.getVariable("FalloutHandlerRequest")).thenReturn(falloutHandlerRequest)

        falloutHandler.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("FH_success", true)
        verify(mockExecution).setVariable("FH_request_id","uCPE1020_STUW105_5002")
        verify(mockExecution).setVariable("FH_ErrorCode","Some Error Code - Fallout Handler")
        verify(mockExecution).setVariable("FH_ErrorMessage","Some Error Message - Fallout Handler")
    }

    @Test
    public void testpostProcessResponse(){

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        when(mockExecution.getVariable("FH_success")).thenReturn(false)

        falloutHandler.postProcessResponse(mockExecution)

        // Capture the arguments to setVariable
        ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);

        verify(mockExecution, times(4)).setVariable(captor1.capture(), captor2.capture())
        List<String> arg2List = captor2.getAllValues()
        String payloadResponseActual = arg2List.get(1)

        assertEquals(falloutHandlerResponse.replaceAll("\\s+", ""), payloadResponseActual.replaceAll("\\s+", ""))

        verify(mockExecution).setVariable("FH_ResponseCode","500")
    }


    @Test
    public void testUpdateInfraRequestDB(){

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getVariable("FH_request_id")).thenReturn("testReqId")
        when(mockExecution.getVariable("FH_ErrorMessage")).thenReturn("ErrorMessage")
        when(falloutHandler.getDbClient()).thenReturn(requestsDbClient)

        falloutHandler.updateInfraRequestDB(mockExecution)

        InfraActiveRequests infraRequest = new InfraActiveRequests();
        infraRequest.setLastModifiedBy("BPMN")
        infraRequest.setStatusMessage("ErrorMessage")
        infraRequest.setRequestStatus("FAILED")
        infraRequest.setProgress(100)

        verify(requestsDbClient, times(1)).updateInfraActiveRequests(infraRequest, null, null)
    }
}