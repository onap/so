package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.mockito.Mockito.*

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith

import static org.junit.Assert.*

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule

import java.util.HashMap
import java.util.Map
import java.util.UUID
import org.apache.commons.lang3.*


@RunWith(MockitoJUnitRunner.class)
class DoUpdateNetworkInstanceRollbackTest  {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(28090)

		def utils = new MsoUtils()
		String Prefix="UPDNETIR_"


		String rollbackNetworkRequest =
		"""<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.openecomp.mso/network">
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
		"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                  xmlns:ns5="http://org.openecomp/mso/request/types/v1"
                                  xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
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
		"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                  xmlns:ns5="http://org.openecomp/mso/request/types/v1"
                                  xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
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

		public void initializeVariables (Execution mockExecution) {

			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", null)
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", null)
			verify(mockExecution).setVariable(Prefix + "WorkflowException", null)

			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", "")
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkReturnCode", "")

			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCReturnCode", "")

			verify(mockExecution).setVariable(Prefix + "Success", false)
			verify(mockExecution).setVariable(Prefix + "fullRollback", false)


		}

		@Test
		//@Ignore
		public void preProcessRequest() {

			println "************ preProcessRequest ************* "

			WorkflowException workflowException = new WorkflowException("DoUpdateNetworkInstanceRollback", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")
			Map<String, String> rollbackData = new HashMap<String, String>()
			rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
			rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
			when(mockExecution.getVariable("rollbackData")).thenReturn(rollbackData)
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			when(mockExecution.getVariable("URN_mso_adapters_sdnc_endpoint")).thenReturn("http://localhost:28090/SDNCAdapter")
			when(mockExecution.getVariable("URN_mso_adapters_network_rest_endpoint")).thenReturn("http://localhost:28090/networks/NetworkAdapter")
			when(mockExecution.getVariable("URN_mso_adapters_sdnc_resource_endpoint")).thenReturn("http://localhost:28090/SDNCAdapterRpc")


			// preProcessRequest(Execution execution)
			DoUpdateNetworkInstanceRollback DoUpdateNetworkInstanceRollback = new DoUpdateNetworkInstanceRollback()
			DoUpdateNetworkInstanceRollback.preProcessRequest(mockExecution)

			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

		}


		@Test
		@Ignore
		public void validateRollbackResponses_Good() {

			WorkflowException workflowException = new WorkflowException("DoUpdateNetworkInstanceRollback", 2500, "AAI Update Contrail Failed.  Error 404.")
			WorkflowException expectedWorkflowException = new WorkflowException("DoUpdateNetworkInstanceRollback", 2500, "AAI Update Contrail Failed.  Error 404. + SNDC activate rollback completed. + PO Network rollback completed. + SNDC assign rollback completed.")

			println "************ validateRollbackResponses_Good() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "WorkflowException")).thenReturn(workflowException)
			when(mockExecution.getVariable(Prefix + "fullRollback")).thenReturn(false)

			DoUpdateNetworkInstanceRollback DoUpdateNetworkInstanceRollback = new DoUpdateNetworkInstanceRollback()
			DoUpdateNetworkInstanceRollback.validateRollbackResponses(mockExecution)

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

			Map<String, String> rollbackData = new HashMap<String, String>()
			rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
			rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)

			println "************ validateRollbackResponses_FullRollback() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn("Good")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable(Prefix + "WorkflowException")).thenReturn(null)
			when(mockExecution.getVariable(Prefix + "fullRollback")).thenReturn(true)
			when(mockExecution.getVariable("rollbackData")).thenReturn(rollbackData)

			DoUpdateNetworkInstanceRollback DoUpdateNetworkInstanceRollback = new DoUpdateNetworkInstanceRollback()
			DoUpdateNetworkInstanceRollback.validateRollbackResponses(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(1)).setVariable("rollbackSuccessful", true)
			verify(mockExecution, atLeast(1)).setVariable("rollbackError", false)

		}


		private ExecutionEntity setupMock() {

			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("DoUpdateNetworkInstanceRollback")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoUpdateNetworkInstanceRollback")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables

			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("DoUpdateNetworkInstanceRollback")
			when(mockExecution.getProcessInstanceId()).thenReturn("DoUpdateNetworkInstanceRollback")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

			return mockExecution
		}

}
