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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DeleteNetworkInstanceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    String Prefix = "DELNI_"
    def utils = new MsoUtils()


    String falloutHandlerRequestObj =
            """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>b69c9054-da09-4a2c-adf5-51042b62bfac</request-id>
					      <action>DELETE</action>
					      <source>PORTAL</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>Received error from SDN-C: No availability zone available</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

    String completeMsoProcessRequest =
            """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                            xmlns:ns="http://org.onap/so/request/types/v1"
                            xmlns="http://org.onap/so/infra/vnf-request/v1">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>VID</source>
   </request-info>
   <aetgt:status-message>Network has been deleted successfully.</aetgt:status-message>
   <aetgt:mso-bpel-name>BPMN Network action: DELETE</aetgt:mso-bpel-name>
</aetgt:MsoCompletionRequest>"""


    String deleteDBRequestErrorObj =
            """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
								<requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>Received error from SDN-C: No availability zone available</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<progress></progress>
								<vnfOutputs>&lt;network-outputs xmlns="http://org.onap/so/infra/vnf-request/v1" xmlns:aetgt="http://org.onap/so/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"/&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

    String jsonIncomingRequest =
            """{ "requestDetails": {
	      "modelInfo": {
			"modelType": "networkTyp",
  			"modelId": "modelId",
  			"modelNameVersionId": "modelNameVersionId",
  			"modelName": "CONTRAIL_EXTERNAL",
  			"modelVersion": "1"
		  },
		  "cloudConfiguration": {
  			"lcpCloudRegionId": "RDM2WAGPLCP",
  			"tenantId": "7dd5365547234ee8937416c65507d266"
		  },
		  "requestInfo": {
  			"instanceName": "MNS-25180-L-01-dmz_direct_net_1",
  			"source": "VID",
  			"callbackUrl": "",
            "suppressRollback": true,
	        "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb"
		  },
		  "relatedInstanceList": [ 
		  	{  
    	  		"relatedInstance": {
       				"instanceId": "f70e927b-6087-4974-9ef8-c5e4d5847ca4",
       				"modelInfo": {   
          				"modelType": "serviceT",
          				"modelId": "modelI",
          				"modelNameVersionId": "modelNameVersionI",
          				"modelName": "modleNam",
          				"modelVersion": "1"
       	  			}
        		}
     		}
		  ],
		  "requestParameters": {
  			"userParams": [	
               {
				 "name": "someUserParam1",
				 "value": "someValue1"
			   }
            ]
		  }
  }}"""


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    public void preProcessRequest() {

        println "************ preProcessRequest_Payload ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.preProcessRequest(mockExecution)

        verify(mockExecution).getVariable("isDebugLogEnabled")
        verify(mockExecution).setVariable("prefix", Prefix)

        //verify variable initialization
        initializeVariables(mockExecution)
    }

    public void initializeVariables(DelegateExecution mockExecution) {


        verify(mockExecution).setVariable(Prefix + "Success", false)

        verify(mockExecution).setVariable(Prefix + "CompleteMsoProcessRequest", "")
        verify(mockExecution).setVariable(Prefix + "FalloutHandlerRequest", "")
        verify(mockExecution).setVariable(Prefix + "isSilentSuccess", false)

    }


    @Test
    public void getNetworkModelInfo() {

        println "************ getNetworkModelInfo() ************* "

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.getNetworkModelInfo(mockExecution)

        verify(mockExecution).getVariable("isDebugLogEnabled")
        verify(mockExecution).setVariable("prefix", Prefix)
    }

    @Test
    public void sendSyncResponse() {

        println "************ sendSyncResponse ************* "

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("isAsyncProcess")).thenReturn(true)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.sendSyncResponse(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable("DeleteNetworkInstanceResponseCode", "202")
    }


    @Test
    public void sendSyncError() {

        println "************ sendSyncError ************* "

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("isAsyncProcess")).thenReturn(true)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.sendSyncError(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable("DeleteNetworkInstanceResponseCode", "500")

    }


    @Test
    public void prepareCompletion() {

        println "************ postProcessResponse ************* "
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("PORTAL")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.prepareCompletion(mockExecution)

        verify(mockExecution).getVariable("isDebugLogEnabled")
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "CompleteMsoProcessRequest", completeMsoProcessRequest)

    }


    @Test
    public void prepareDBRequestError() {

        println "************ prepareDBRequestError ************* "

        WorkflowException sndcWorkflowExceptionObj =
                new WorkflowException("CreateNetworkV2", 500, "Received error from SDN-C: No availability zone available")

        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowExceptionObj)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.adapters.db.auth")).
                thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C");
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7");

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.prepareDBRequestError(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "deleteDBRequest", deleteDBRequestErrorObj)

    }


    @Test
    public void postProcessResponse() {

        println "************ postProcessResponse ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("CMSO_ResponseCode")).thenReturn("200")

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.postProcessResponse(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "Success", true)
    }


    @Test
    public void buildErrorResponse_FalloutHandlerRequest() {

        println "************ buildErrorResponse ************* "

        WorkflowException sndcWorkflowExceptionObj =
                new WorkflowException("DeleteNetworkInstance", 7000, "Received error from SDN-C: No availability zone available")

        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("b69c9054-da09-4a2c-adf5-51042b62bfac")
        when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowExceptionObj)
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("PORTAL")

        DeleteNetworkInstance DeleteNetworkInstance = new DeleteNetworkInstance()
        DeleteNetworkInstance.buildErrorResponse(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "Success", false)
        verify(mockExecution).setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequestObj)

    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DeleteNetworkInstance")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DeleteNetworkInstance")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getProcessDefinitionId()).thenReturn("DeleteNetworkInstance")
        when(mockExecution.getProcessInstanceId()).thenReturn("DeleteNetworkInstance")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().
                getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
