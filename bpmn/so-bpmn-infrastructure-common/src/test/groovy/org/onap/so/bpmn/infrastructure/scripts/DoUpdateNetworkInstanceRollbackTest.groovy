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

import static org.mockito.ArgumentMatchers.eq
import static org.mockito.ArgumentMatchers.refEq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoUpdateNetworkInstanceRollbackTest  {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    def utils = new MsoUtils()
    String Prefix = "UPDNETIR_"


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
    private String processDefinitionKey = "DoUpdateNetworkInstanceRollback"

// - - - - - - - -


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    public void initializeVariables(DelegateExecution mockExecution) {

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
    public void preProcessRequest() {

        println "************ preProcessRequest ************* "

        WorkflowException workflowException = new WorkflowException(processDefinitionKey, 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")
        Map<String, String> rollbackData = new HashMap<String, String>();
        rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
        rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)

        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
        when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
        when(mockExecution.getVariable("rollbackData")).thenReturn(rollbackData)
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")
        when(mockExecution.getVariable("mso.adapters.po.auth"))
                .thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

        when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:28090/SDNCAdapter")
        when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:28090/networks/NetworkAdapter")
        when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:28090/SDNCAdapterRpc")

        DoUpdateNetworkInstanceRollback DoUpdateNetworkInstanceRollback = new DoUpdateNetworkInstanceRollback()
        DoUpdateNetworkInstanceRollback.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)

        //verify variable initialization
        initializeVariables(mockExecution)
    }


    @Test
    public void validateRollbackResponses_Good() {

        WorkflowException workflowException = new WorkflowException(processDefinitionKey, 2500, "AAI Update Contrail Failed.  Error 404.")
        WorkflowException expectedWorkflowException = new WorkflowException(processDefinitionKey, 2500, "AAI Update Contrail Failed.  Error 404. + PO Network rollback completed. + SNDC changeassign rollback completed.")

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
        verify(mockExecution).setVariable(eq("workflowException"), refEq(expectedWorkflowException))
    }

    @Test
    public void validateRollbackResponses_FullRollback() {

        Map<String, String> rollbackData = new HashMap<String, String>();
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
        when(mockProcessDefinition.getKey()).thenReturn(processDefinitionKey)
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn(processDefinitionKey)
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables

        when(mockExecution.getProcessDefinitionId()).thenReturn(processDefinitionKey)
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

}
