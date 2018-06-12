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

package org.openecomp.mso.bpmn.common.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.openecomp.mso.rest.HttpHeader
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.junit.Before
import org.openecomp.mso.bpmn.common.scripts.AaiUtil;
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
import org.openecomp.mso.bpmn.common.scripts.ConfirmVolumeGroupName
import org.openecomp.mso.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class ConfirmVolumeGroupNameTest extends MsoGroovyTest {

	@Captor
	ArgumentCaptor<ExecutionEntity> captor=  ArgumentCaptor.forClass(ExecutionEntity.class);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

	@Test
	public void testQueryAAIForVolumeGroupId() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
		when(mockExecution.getVariable("CVGN_volumeGroupGetEndpoint")).thenReturn('/aai/test/volume-groups/volume-group/testVolumeGroup')
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')

		mockData()

		ConfirmVolumeGroupName confirmVolumeGroupName = new ConfirmVolumeGroupName()
		confirmVolumeGroupName.queryAAIForVolumeGroupId(mockExecution)
		verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponseCode",200)
		verify(mockExecution).setVariable("CVGN_queryVolumeGroupResponse","")
	}

	@Test
	public void testQueryAAIForVolumeGroupId_404() {

		ExecutionEntity mockExecution = setupMock()
		try {
			when(mockExecution.getVariable("CVGN_volumeGroupGetEndpoint")).thenReturn('/aai/test/volume-groups/volume-group/testVolumeGroup')
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')

			mockData()

			ConfirmVolumeGroupName confirmVolumeGroupName = new ConfirmVolumeGroupName()
			confirmVolumeGroupName.queryAAIForVolumeGroupId(mockExecution)
		}
		catch(Exception ex){

		}
		Mockito.verify(mockExecution,times(4)).setVariable(captor.capture(),captor.capture())
		WorkflowException workflowException = captor.getValue()
		Assert.assertEquals("AAI GET Failed",workflowException.getErrorMessage())
		Assert.assertEquals(500,workflowException.getErrorCode())
	}
	private void  mockData() {
		stubFor(get(urlMatching("/aai/test/volume-groups/volume-group/testVolumeGroup"))
				.willReturn(aResponse()
				.withStatus(200)))
	}

	private ExecutionEntity setupMock() {

		ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
		when(mockProcessDefinition.getKey()).thenReturn("ConfirmVolumeGroupName")
		RepositoryService mockRepositoryService = mock(RepositoryService.class)
		when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
		when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("ConfirmVolumeGroupName")
		when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
		ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
		when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getId()).thenReturn("100")
		when(mockExecution.getProcessDefinitionId()).thenReturn("ConfirmVolumeGroupName")
		when(mockExecution.getProcessInstanceId()).thenReturn("ConfirmVolumeGroupName")
		when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
		when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
		return mockExecution
	}
}
