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


import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class SniroHomingV1Test {
    String subsInfo = "{\"globalSubscriberId\": \"SUB12_0322_DS_1201\",\"subscriberCommonSiteId\": \"DALTX0101\",\"subscriberName\": \"SUB_12_0322_DS_1201\"}"
    ServiceDecomposition serviceDecomp

    {
        serviceDecomp = new ServiceDecomposition("{\"serviceResources\":{}}", "123")
        ModelInfo modelInfo = new ModelInfo()
        serviceDecomp.modelInfo = modelInfo

        AllottedResource allottedResource = new AllottedResource()
        allottedResource.setModelInfo(modelInfo)
        List allottedResourceList = new ArrayList()
        allottedResourceList.add(allottedResource)

        VnfResource vnfResource = new VnfResource()
        vnfResource.setModelInfo(modelInfo)
        List vnfResourceList = new ArrayList()
        vnfResourceList.add(vnfResource)

        serviceDecomp.serviceAllottedResources = allottedResourceList
        serviceDecomp.setVnfResources(vnfResourceList)
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    public void testCallSniro() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("subscriberInfo")).thenReturn(subsInfo)
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomp)
        when(mockExecution.getVariable("mso.sniro.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("mso.adapters.workflow.message.endpoint")).thenReturn('http://localhost:18080/workflows/messages/message/')
        when(mockExecution.getVariable("mso.service.agnostic.sniro.endpoint")).thenReturn("/sniro")
        when(mockExecution.getVariable("mso.service.agnostic.sniro.host")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.GenericPutVnf.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()

        SniroHomingV1 obj = new SniroHomingV1()
        obj.callSniro(mockExecution)

        Mockito.verify(mockExecution, times(10)).setVariable(captor.capture(), captor.capture())
        Assert.assertEquals(200, captor.getAllValues().get(17))

    }

    @Test
    public void testCallSniroMissingAuth() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("subscriberInfo")).thenReturn(subsInfo)
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomp)
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("mso.adapters.workflow.message.endpoint")).thenReturn('http://localhost:18080/workflows/messages/message/')
        when(mockExecution.getVariable("mso.service.agnostic.sniro.endpoint")).thenReturn("/sniro")
        when(mockExecution.getVariable("mso.service.agnostic.sniro.host")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.GenericPutVnf.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()

        try {
            SniroHomingV1 obj = new SniroHomingV1()
            obj.callSniro(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(401, workflowException.getErrorCode())
        Assert.assertEquals("Internal Error - BasicAuth value null", workflowException.getErrorMessage())
    }

    @Test
    public void testCallSniroHostNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("subscriberInfo")).thenReturn(subsInfo)
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomp)
        when(mockExecution.getVariable("mso.sniro.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("mso.adapters.workflow.message.endpoint")).thenReturn('http://localhost:18080/workflows/messages/message/')
        when(mockExecution.getVariable("mso.service.agnostic.sniro.endpoint")).thenReturn("/sniro")
        when(mockExecution.getVariable("mso.service.agnostic.sniro.host")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.GenericPutVnf.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        try {
            SniroHomingV1 obj = new SniroHomingV1()
            obj.callSniro(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(9)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(2500, workflowException.getErrorCode())
        Assert.assertEquals("Internal Error - Occured in Homing CallSniro: org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("Homing")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("Homing")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("Homing")
        when(mockExecution.getProcessInstanceId()).thenReturn("Homing")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

    private static void mockData() {
        stubFor(post(urlMatching(".*/sniro"))
                .willReturn(aResponse()
                .withStatus(200)
                .withBody("")))
    }
}
