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

import static org.mockito.Mockito.*

import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition

abstract class MsoGroovyTest {
	
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
	
	
}
