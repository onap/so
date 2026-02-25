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
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.db.request.beans.InfraActiveRequests
import org.onap.so.db.request.client.RequestsDbClient
import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatThrownBy
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class CompleteMsoProcessTest {

    @Mock
    RequestsDbClient requestsDbClient;

    @Spy
    CompleteMsoProcess completeMsoProcess;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

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
        when(mockExecution.getVariable("CompleteMsoProcessRequest")).thenReturn(completeMsoNetworkProcessRequest)

        completeMsoProcess.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("CMSO_request_id", "bd631913-cfc6-488b-ba22-6b98504f703d")
    }


    @Test
    public void testUpdateInfraRequestDB(){

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getVariable("CMSO_request_id")).thenReturn("testReqId")
        when(mockExecution.getVariable("CompleteMsoProcessRequest")).thenReturn(completeMsoNetworkProcessRequest);
        when(completeMsoProcess.getDbClient()).thenReturn(requestsDbClient)
        completeMsoProcess.updateInfraRequestDB(mockExecution)

        InfraActiveRequests request = new InfraActiveRequests()
        request.setRequestId("testReqId")

        completeMsoProcess.updateInfraRequestDB(mockExecution)

        request.setRequestStatus("COMPLETED")
        request.setStatusMessage("Resource Completed Successfully")
        request.setProgress(100)
        request.setLastModifiedBy("BPMN")
        request.setNetworkId("bd631913-cfc6-488b-ba22-6b98504f703d")


        verify(requestsDbClient, times(2)).updateInfraActiveRequests(request, null, null)
    }
}