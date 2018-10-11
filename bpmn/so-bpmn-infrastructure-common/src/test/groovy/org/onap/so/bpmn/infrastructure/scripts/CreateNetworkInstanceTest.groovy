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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import com.github.tomakehurst.wiremock.junit.WireMockRule

class CreateNetworkInstanceTest  {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);
	
		String Prefix="CRENI_"
		def utils = new MsoUtils()
	
		String createDBRequestError =
"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
								<requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>Received error from SDN-C: No availability zone available</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs>&lt;network-id&gt;&lt;/network-id&gt;&lt;network-name&gt;&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

	  String falloutHandlerRequest =
					   """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>b69c9054-da09-4a2c-adf5-51042b62bfac</request-id>
					      <action>CREATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>Received error from SDN-C: No availability zone available.</aetgt:ErrorMessage>
							<aetgt:ErrorCode>5300</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
					   
	   String completeMsoProcessRequest =
					   """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                            xmlns:ns="http://org.onap/so/request/types/v1"
                            xmlns="http://org.onap/so/infra/vnf-request/v1">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>CREATE</action>
      <source>VID</source>
   </request-info>
   <aetgt:status-message>Network has been created successfully.</aetgt:status-message>
   <aetgt:networkId>fb03f6f0-9012-41c4-87d8-1dbf3c22b889</aetgt:networkId>
   <aetgt:mso-bpel-name>BPMN Network action: CREATE</aetgt:mso-bpel-name>
</aetgt:MsoCompletionRequest>"""


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
		public void init()
		{
			MockitoAnnotations.initMocks(this)
		}

		public void initializeVariables(DelegateExecution mockExecution) {
			
			verify(mockExecution).setVariable(Prefix + "Success", false)
			
			verify(mockExecution).setVariable(Prefix + "CompleteMsoProcessRequest", "")
			verify(mockExecution).setVariable(Prefix + "FalloutHandlerRequest", "")
			verify(mockExecution).setVariable(Prefix + "isSilentSuccess", false)
				
		}
				
		@Test
		//@Ignore  
		public void preProcessRequest() {
			
			println "************ preProcessRequest() ************* " 
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)
			
									
			// preProcessRequest(DelegateExecution execution)						
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.preProcessRequest(mockExecution)

			//verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)
			
			initializeVariables(mockExecution)
			//verify(mockExecution).setVariable(Prefix + "Success", false)
							
		}
		
		
		@Test
		//@Ignore
		public void getNetworkModelInfo() {
			
			println "************ getNetworkModelInfo() ************* "
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
									
			// preProcessRequest(DelegateExecution execution)
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.getNetworkModelInfo(mockExecution)

			//verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)
							
		}
		
		@Test
		//@Ignore
		public void sendSyncResponse() {
			
			println "************ sendSyncResponse ************* "
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("isAsyncProcess")).thenReturn(true)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			
			// preProcessRequest(DelegateExecution execution)
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.sendSyncResponse(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable("CreateNetworkInstanceResponseCode", "202")
			
		}

		@Test
		//@Ignore
		public void sendSyncError() {
			
			println "************ sendSyncError ************* "
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("isAsyncProcess")).thenReturn(true)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.sendSyncError(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable("CreateNetworkInstanceResponseCode", "500")
			
		}
		
		@Test
		//@Ignore
		public void prepareDBRequestError() {
			
			println "************ prepareDBRequestError ************* "
			
			WorkflowException sndcWorkflowException = new WorkflowException("CreateNetworkInstance", 500, "Received error from SDN-C: No availability zone available")
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowException)
			//when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("networkId")).thenReturn("")
			when(mockExecution.getVariable("networkName")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.adapters.db.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			// preProcessRequest(DelegateExecution execution)
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.prepareDBRequestError(mockExecution)
			
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "createDBRequest", createDBRequestError)
		
		}
		

		@Test
		//@Ignore
		public void prepareCompletion() {
			
			println "************ postProcessResponse ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable(Prefix + "dbReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("networkId")).thenReturn("fb03f6f0-9012-41c4-87d8-1dbf3c22b889")
			
			// postProcessResponse(DelegateExecution execution)
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.prepareCompletion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "Success", true)
			verify(mockExecution).setVariable(Prefix + "CompleteMsoProcessRequest", completeMsoProcessRequest)
		
		}

		@Test
		//@Ignore
		public void buildErrorResponse() {
			
			println "************ buildErrorResponse ************* "
			
			
			WorkflowException sndcWorkflowException = new WorkflowException("CreateNetworkInstance", 5300, "Received error from SDN-C: No availability zone available.")
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("b69c9054-da09-4a2c-adf5-51042b62bfac")
			//when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowException)
			when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowException)
			
			// buildErrorResponse(DelegateExecution execution)
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.buildErrorResponse(mockExecution)
			
			verify(mockExecution, atLeast(1)).setVariable("prefix", "CRENI_")
			verify(mockExecution).setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

		}
		
		@Test
		//@Ignore
		public void postProcessResponse() {
			
			println "************ postProcessResponse() ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("CMSO_ResponseCode")).thenReturn("200")
			
			// postProcessResponse(DelegateExecution execution)						
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.postProcessResponse(mockExecution)

			//verify(mockExecution).getVariable("isDebugLogEnabled")
			//verify(mockExecution).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "Success", true)
		
		}
		
		@Test
		//@Ignore
		public void processRollbackData() {
			
			println "************ callDBCatalog() ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
									
			// preProcessRequest(DelegateExecution execution)
			CreateNetworkInstance CreateNetworkInstance = new CreateNetworkInstance()
			CreateNetworkInstance.processRollbackData(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)
							
		}
		
		private ExecutionEntity setupMock() {
			
			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("CreateNetworkInstance")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("CreateNetworkInstance")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			
			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("CreateNetworkInstance")
			when(mockExecution.getProcessInstanceId()).thenReturn("CreateNetworkInstance")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
			
			return mockExecution
		}
		
}
