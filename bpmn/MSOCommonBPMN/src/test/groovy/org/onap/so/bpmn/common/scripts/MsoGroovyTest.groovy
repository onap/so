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

import static org.mockito.Mockito.*

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.GenericVnf
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.client.HttpClient
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.graphinventory.entities.uri.Depth
import org.onap.so.constants.Defaults

@RunWith(MockitoJUnitRunner.Silent.class)
abstract class MsoGroovyTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none()

    protected ExecutionEntity mockExecution
    protected AAIResourcesClient client
    protected AllottedResourceUtils allottedResourceUtils_MOCK
    protected final String SEARCH_RESULT_AAI_WITH_RESULTDATA =
    FileUtil.readResourceFile("__files/aai/searchResults.json")
    protected static final String CLOUD_OWNER = Defaults.CLOUD_OWNER.toString();

    protected void init(String procName){
        //    mockExecution = setupMock(procName)
        //   when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        client = mock(AAIResourcesClient.class)
        mockExecution = mock(ExecutionEntity.class)
    }

    protected ExecutionEntity setupMock(String procName) {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn(procName)

        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn(procName)
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")

        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)

        HttpClient httpClient = mock(HttpClient.class)

        return mockExecution
    }

    protected ExecutionEntity setupMockWithPrefix(String procName, String prefix) {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        when(mockExecution.getVariable("prefix")).thenReturn(prefix)

        ProcessEngineServices processEngineServices = mock(ProcessEngineServices.class)
        RepositoryService repositoryService = mock(RepositoryService.class)
        ProcessDefinition processDefinition = mock(ProcessDefinition.class)

        when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices)
        when(processEngineServices.getRepositoryService()).thenReturn(repositoryService)
        when(repositoryService.getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(processDefinition)
        when(processDefinition.getKey()).thenReturn(procName)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        return mockExecution
    }

    protected <T> Optional<T> getAAIObjectFromJson(Class<T> clazz , String file){
        String json = FileUtil.readResourceFile(file)
        AAIResultWrapper resultWrapper = new AAIResultWrapper(json)
        return resultWrapper.asBean(clazz)
    }

    protected Optional<GenericVnf> mockAAIGenericVnf(String vnfId){
        return mockAAIGenericVnf(vnfId,"__files/aai/GenericVnf.json")
    }
	
    protected Optional<GenericVnf> mockAAIGenericVnf(String vnfId,String file){
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
        AAIResourceUri resourceUriDepthOne = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE)
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,file);
        when(client.get(GenericVnf.class, resourceUri)).thenReturn(genericVnf)
        when(client.get(GenericVnf.class, resourceUriDepthOne)).thenReturn(genericVnf)
        return genericVnf
    }

    protected Optional<GenericVnf> mockAAIGenericVnfByName(String vnfName){
        AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName)
        AAIPluralResourceUri resourceUriDepthOne = AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName).depth(Depth.ONE)
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnf.json");
        when(client.get(GenericVnf.class, resourceUri)).thenReturn(genericVnf)
        when(client.get(GenericVnf.class, resourceUriDepthOne)).thenReturn(genericVnf)
        return genericVnf
    }

    protected void mockAAIGenericVnfNotFound(String vnfId){
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE)
        AAIResourceUri resourceUriDepthOne = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE)
        when(client.get(GenericVnf.class, resourceUri)).thenReturn(Optional.empty())
        when(client.get(GenericVnf.class, resourceUriDepthOne)).thenReturn(Optional.empty())
    }

    protected void mockAAIGenericVnfByNameNotFound(String vnfName){
        AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName)
        AAIPluralResourceUri resourceUriDepthOne = AAIUriFactory.createResourceUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName).depth(Depth.ONE)
        when(client.get(GenericVnf.class, resourceUri)).thenReturn(Optional.empty())
        when(client.get(GenericVnf.class, resourceUriDepthOne)).thenReturn(Optional.empty())
    }

    protected AAIResultWrapper mockVolumeGroupWrapper(String region, String volumeGroupId, String file){
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP,CLOUD_OWNER, region,volumeGroupId)
        String json = FileUtil.readResourceFile(file)
        AAIResultWrapper resultWrapper = new AAIResultWrapper(json)
        when(client.get(resourceUri)).thenReturn(resultWrapper)
        return resultWrapper
    }

    void initAR(String procName){
        init(procName)
        allottedResourceUtils_MOCK = spy(new AllottedResourceUtils(mock(AbstractServiceTaskProcessor.class)))
        when(allottedResourceUtils_MOCK.getAAIClient()).thenReturn(client)
    }
}
