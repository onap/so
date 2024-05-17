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

import static org.mockito.Mockito.*

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
//import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.junit.Before
import org.junit.Rule;
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith

import static org.onap.so.bpmn.mock.StubResponseNetworkAdapter.MockNetworkAdapterRestRollbackDelete;
import static org.junit.Assert.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.*


@RunWith(MockitoJUnitRunner.class)
class DoCreateNetworkInstanceRollbackTest  {
	
	def utils = new MsoUtils()
		String Prefix="CRENWKIR_"


		String rollbackNetworkRequest =
		"""<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.onap.so/network">
   <rollback>
      <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
      <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
      <networkStackId/>
      <networkType>CONTRAIL_EXTERNAL</networkType>
      <networkCreated>true</networkCreated>
      <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
      <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
      <msoRequest>
         <requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
         <serviceInstanceId/>
      </msoRequest>
   </rollback>
</NetworkAdapter:rollbackNetwork>"""			
								
					String rollbackActivateSDNCRequest =
		"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>rollback</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
      <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>CreateNetworkInstance</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <service-type>MSO-dev-service-type</service-type>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>invariant-uuid</model-invariant-uuid>
            <model-customization-uuid>customization-uuid</model-customization-uuid>
            <model-uuid>uuid</model-uuid>
            <model-version>version</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""			
		
					String rollbackSDNCRequest =
		"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>rollback</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
      <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>CreateNetworkInstance</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <service-type>MSO-dev-service-type</service-type>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>invariant-uuid</model-invariant-uuid>
            <model-customization-uuid>customization-uuid</model-customization-uuid>
            <model-uuid>uuid</model-uuid>
            <model-version>version</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""			
		
// - - - - - - - -


	    @Before
		public void init()
		{
			MockitoAnnotations.initMocks(this)
			
		}
		
		public void initializeVariables (DelegateExecution mockExecution) {

			verify(mockExecution).setVariable(Prefix + "WorkflowException", null)
			
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", null)
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkReturnCode", "")
	
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", null)
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCReturnCode", "")
			
			verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCRequest", null)
			verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCReturnCode", "")
	
			verify(mockExecution).setVariable(Prefix + "Success", false)
			verify(mockExecution).setVariable(Prefix + "fullRollback", false)
			verify(mockExecution).setVariable(Prefix + "networkId", "")
			verify(mockExecution).setVariable(Prefix + "urlRollbackPoNetwork", "")
			
		}
		
		@Test
		//@Ignore  
		public void preProcessRequest() {
			
			println "************ preProcessRequest ************* " 
			
			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")
			Map<String, String> rollbackData = new HashMap<String, String>();
			rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
			rollbackData.put("rollbackActivateSDNCRequest", rollbackActivateSDNCRequest)
			rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)
					
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
			when(mockExecution.getVariable("rollbackData")).thenReturn(rollbackData)
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
						
			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:28090/SDNCAdapter")
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:28090/networks/NetworkAdapter")
			when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:28090/SDNCAdapterRpc")
			
			
			// preProcessRequest(DelegateExecution execution)						
			DoCreateNetworkInstanceRollback DoCreateNetworkInstanceRollback = new DoCreateNetworkInstanceRollback()
			DoCreateNetworkInstanceRollback.preProcessRequest(mockExecution)
			
//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)
			
			//verify variable initialization
			initializeVariables(mockExecution)
								
		}
		
		@Test
		//@Ignore
		public void callPONetworkAdapter() {

			MockNetworkAdapterRestRollbackDelete("deleteNetworkResponse_Success.xml","8abc633a-810b-4ca5-8b3a-09511d13a2ce");
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn( rollbackNetworkRequest)
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(rollbackSDNCRequest)
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:28090/networks/NetworkAdapter")
			
			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstanceRollback DoCreateNetworkInstanceRollback = new DoCreateNetworkInstanceRollback()
			DoCreateNetworkInstanceRollback.callPONetworkAdapter(mockExecution)
			
			verify(mockExecution, atLeast(1)).setVariable(Prefix + "urlRollbackPoNetwork", "http://localhost:28090/networks/NetworkAdapter/8abc633a-810b-4ca5-8b3a-09511d13a2ce/rollback")
			
		}
		
		@Test
		//@Ignore
		public void validateRollbackResponses_Good() {
			
			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstanceRollback", 2500, "AAI Update Contrail Failed.  Error 404.")
			WorkflowException expectedWorkflowException = new WorkflowException("DoCreateNetworkInstanceRollback", 2500, "AAI Update Contrail Failed.  Error 404. + SNDC activate rollback completed. + PO Network rollback completed. + SNDC assign rollback completed.")
				  
			println "************ validateRollbackResponses_Good() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "WorkflowException")).thenReturn(workflowException)
			when(mockExecution.getVariable(Prefix + "fullRollback")).thenReturn(false)
									
			DoCreateNetworkInstanceRollback DoCreateNetworkInstanceRollback = new DoCreateNetworkInstanceRollback()
			DoCreateNetworkInstanceRollback.validateRollbackResponses(mockExecution)
			
			// verify set prefix = Prefix + ""
			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(1)).setVariable("rolledBack", true)
			verify(mockExecution, atLeast(1)).setVariable("wasDeleted", true)
			verify(mockExecution).setVariable("WorkflowException", refEq(expectedWorkflowException, any(WorkflowException.class)))
			//verify(mockExecution).setVariable("WorkflowException", expectedWorkflowException)
		}
		
		@Test
		//@Ignore
		public void validateRollbackResponses_FullRollback() {
			
			Map<String, String> rollbackData = new HashMap<String, String>();
			rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
			rollbackData.put("rollbackActivateSDNCRequest", rollbackActivateSDNCRequest)
			rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)
				  
			println "************ validateRollbackResponses_FullRollback() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "workflowException")).thenReturn(null)
			when(mockExecution.getVariable(Prefix + "fullRollback")).thenReturn(true)
			when(mockExecution.getVariable("rollbackData")).thenReturn(rollbackData)
									
			DoCreateNetworkInstanceRollback DoCreateNetworkInstanceRollback = new DoCreateNetworkInstanceRollback()
			DoCreateNetworkInstanceRollback.validateRollbackResponses(mockExecution)
			
			// verify set prefix = Prefix + ""
			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(1)).setVariable("rollbackSuccessful", true)
			verify(mockExecution, atLeast(1)).setVariable("rollbackError", false)
			
		}
		
		
		private ExecutionEntity setupMock() {
			
			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("DoCreateNetworkInstanceRollback")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateNetworkInstanceRollback")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			
			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateNetworkInstanceRollback")
			when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateNetworkInstanceRollback")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
			
			return mockExecution
		}
		
}
