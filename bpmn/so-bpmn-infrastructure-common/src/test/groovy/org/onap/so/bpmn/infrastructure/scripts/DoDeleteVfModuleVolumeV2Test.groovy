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
class DoDeleteVfModuleVolumeV2Test {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCallRESTQueryAAICloudRegion() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn("DDVMV_")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVfModuleVolumeV2.aai.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
        obj.callRESTQueryAAICloudRegion(mockExecution, "true")
        Mockito.verify(mockExecution).setVariable("DDVMV_queryCloudRegionReturnCode", "200")
        Mockito.verify(mockExecution).setVariable("DDVMV_aicCloudRegion", "RDM2WAGPLCP")
    }

    @Test
    public void testCallRESTQueryAAICloudRegionAAiEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn("DDVMV_")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVfModuleVolumeV2.aai.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        try {
            DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
            obj.callRESTQueryAAICloudRegion(mockExecution, "true")

        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution,atLeastOnce()).setVariable(captor.capture(),captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(9999, workflowException.getErrorCode())
    }

    @Test
    public void testPrepareVnfAdapterDeleteRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn("DDVMV_")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("aicCloudRegion")).thenReturn("RegionOne")
        when(mockExecution.getVariable("tenantId")).thenReturn("12345")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("12345")
        when(mockExecution.getVariable("volumeGroupHeatStackId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn("http://localhost:18080/mso/WorkflowMessage")

        DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
        obj.prepareVnfAdapterDeleteRequest(mockExecution, "true")

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        String str = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV2/vnfAdapterDeleteRequest.xml")
        XmlComparator.assertXMLEquals(str, captor.getValue(),"messageId","notificationUrl")
    }


    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoDeleteVfModuleVolumeV2")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoDeleteVfModuleVolumeV2")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables

        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoDeleteVfModuleVolumeV2")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoDeleteVfModuleVolumeV2")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution

    }

    private void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/12345"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("DoCreateVfModuleVolumeV2/cloudRegion_AAIResponse_Success.xml")))
    }
}
