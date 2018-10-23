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

import com.github.tomakehurst.wiremock.junit.WireMockRule

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.onap.so.rest.HttpHeader
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.junit.Before
import org.onap.so.bpmn.common.scripts.AaiUtil;
import org.junit.Rule;
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.Before;
import org.junit.Test;
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.ConfirmVolumeGroupTenant
import org.onap.so.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class ConfirmVolumeGroupTenantTest extends MsoGroovyTest {

    @Captor
    ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Test
    public void testpreProcessRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
        when(mockExecution.getVariable("volumeGroupId")).thenReturn('testVolumeGroupId')
        when(mockExecution.getVariable("volumeGroupName")).thenReturn('testVolumeGroupName')
        when(mockExecution.getVariable("tenantId")).thenReturn('tenantId')
        when(mockExecution.getVariable("aicCloudRegion")).thenReturn('aicCloudRegion')
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn('/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')


        mockData()


        ConfirmVolumeGroupTenant confirmVolumeGroupTenant = new ConfirmVolumeGroupTenant()
        confirmVolumeGroupTenant.preProcessRequest(mockExecution)
      /*  Mockito.verify(mockExecution, times(5)).setVariable(captor.capture(), captor.capture())*/
        verify(mockExecution).setVariable("prefix", "CVGT_")
        verify(mockExecution).setVariable("queryVolumeGroupResponseCode", 200)
        verify(mockExecution).setVariable("queryAAIVolumeGroupResponse", "<volume-group xmlns=\"http://org.openecomp.aai.inventory/v10\"><volume-group-id>17ef4658-bd1f-4ef0-9ca0-ea76e2bf122c</volume-group-id><volume-group-name>MSOTESTVOL103a-vSAMP12_base_module-0_vol</volume-group-name><heat-stack-id>9d1f53e3-3158-44f8-8032-a6bf40bbc9db</heat-stack-id><vnf-type>pcrf-capacity</vnf-type><orchestration-status>Active</orchestration-status><resource-version>0000020</resource-version><relationship-list><relationship><related-to>tenant</related-to><relationship-data><relationship-key>cloud-region.cloud-owner</relationship-key><relationship-value>CloudOwner</relationship-value></relationship-data><relationship-data><relationship-key>cloud-region.cloud-region-id</relationship-key><relationship-value>RegionOne</relationship-value></relationship-data><relationship-data><relationship-key>tenant.tenant-id</relationship-key><relationship-value>22eb191dd41a4f3c9be370fc638322f4</relationship-value></relationship-data></relationship></relationship-list></volume-group>")
        verify(mockExecution).setVariable("tenantIdsMatch", false)
        verify(mockExecution).setVariable("groupNamesMatch", false)
    }


    private void mockData() {
        stubFor(get(urlMatching("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/aicCloudRegion/volume-groups/volume-group/testVolumeGroupId"))
                .willReturn(aResponse()
                .withStatus(200)
                .withBody("<volume-group xmlns=\"http://org.openecomp.aai.inventory/v10\"><volume-group-id>17ef4658-bd1f-4ef0-9ca0-ea76e2bf122c</volume-group-id><volume-group-name>MSOTESTVOL103a-vSAMP12_base_module-0_vol</volume-group-name><heat-stack-id>9d1f53e3-3158-44f8-8032-a6bf40bbc9db</heat-stack-id><vnf-type>pcrf-capacity</vnf-type><orchestration-status>Active</orchestration-status><resource-version>0000020</resource-version><relationship-list><relationship><related-to>tenant</related-to><relationship-data><relationship-key>cloud-region.cloud-owner</relationship-key><relationship-value>CloudOwner</relationship-value></relationship-data><relationship-data><relationship-key>cloud-region.cloud-region-id</relationship-key><relationship-value>RegionOne</relationship-value></relationship-data><relationship-data><relationship-key>tenant.tenant-id</relationship-key><relationship-value>22eb191dd41a4f3c9be370fc638322f4</relationship-value></relationship-data></relationship></relationship-list></volume-group>")))
    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("ConfirmVolumeGroupTenant")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("ConfirmVolumeGroupTenant")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("ConfirmVolumeGroupTenant")
        when(mockExecution.getProcessInstanceId()).thenReturn("ConfirmVolumeGroupTenant")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
