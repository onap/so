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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.utils.XmlComparator
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class UpdateVfModuleVolumeInfraV1Test {
	
    def prefix = "UPDVfModVol_"
    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(28090);
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
    }
		
    @Test
    void testQueryAAIForVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("UPDVfModVol_relatedVfModuleLink")).thenReturn("/aai/v8/network/generic-vnfs/generic-vnf/12345/vf-modules/vf-module/12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        mockData()
        UpdateVfModuleVolumeInfraV1 obj = new UpdateVfModuleVolumeInfraV1()
        obj.queryAAIForVfModule(mockExecution, "true")

        Mockito.verify(mockExecution, atLeastOnce()).setVariable("UPDVfModVol_personaModelId", "ff5256d2-5a33-55df-13ab-12abad84e7ff")
    }

      @Test
    void testPrepVnfAdapterRest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "aicCloudRegion")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(prefix + "tenantId")).thenReturn("")
        when(mockExecution.getVariable(prefix + "aaiVolumeGroupResponse")).thenReturn(FileUtil.readResourceFile("__files/UpdateVfModuleVolumeInfraV1/queryVolumeId_AAIResponse_Success.xml"))
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vnf1")
        when(mockExecution.getVariable(prefix + "vnfVersion")).thenReturn("1")
        when(mockExecution.getVariable(prefix + "AAIQueryGenericVfnResponse")).thenReturn(FileUtil.readResourceFile("__files/GenericFlows/getGenericVnfByNameResponse.xml"))
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn('http://localhost:28080/mso/WorkflowMessage')
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")

        mockData()
        UpdateVfModuleVolumeInfraV1 obj = new UpdateVfModuleVolumeInfraV1()
        obj.prepVnfAdapterRest(mockExecution, "true")

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        def updateVolumeGroupRequest = captor.getValue()
        String expectedValue = FileUtil.readResourceFile("__files/UpdateVfModuleVolumeInfraV1/updateVolumeGroupRequest.xml")
        XmlComparator.assertXMLEquals(expectedValue, updateVolumeGroupRequest, "messageId", "notificationUrl")
    }


    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("UpdateVfModuleVolumeInfraV1")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("UpdateVfModuleVolumeInfraV1")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("UpdateVfModuleVolumeInfraV1")
        when(mockExecution.getProcessInstanceId()).thenReturn("UpdateVfModuleVolumeInfraV1")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

    private static void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/12345/vf-modules/vf-module/.*"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("UpdateVfModuleVolumeInfraV1/vf_module_aai_response.xml")))
	}
}
