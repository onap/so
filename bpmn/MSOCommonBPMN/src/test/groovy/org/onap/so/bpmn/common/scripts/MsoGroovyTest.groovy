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
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.client.aai.AAIResourcesClient

@RunWith(MockitoJUnitRunner.class)
abstract class MsoGroovyTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none()
	protected ExecutionEntity mockExecution
	protected AAIResourcesClient client

	protected void init(String procName){
		mockExecution = setupMock(procName)
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		client = mock(AAIResourcesClient.class)
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
		return mockExecution
	}
}
