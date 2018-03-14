/*
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
package org.openecomp.mso.bpmn.vcpe.scripts


import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.Ignore
import org.mockito.MockitoAnnotations
import org.camunda.bpm.engine.delegate.BpmnError
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.patch
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.junit.Assert.*;
import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.vcpe.scripts.MapSetter

import com.github.tomakehurst.wiremock.junit.WireMockRule

class GroovyTestBase {
	
	static final int PORT = 28090
	static final String LOCAL_URI = "http://localhost:" + PORT
	
	static final String CUST = "SDN-ETHERNET-INTERNET"
	static final String SVC = "123456789"
	static final String INST = "MIS%252F1604%252F0026%252FSW_INTERNET"
	static final String ARID = "arId-1"
	static final String VERS = "myvers"
	
	static final String DBGFLAG = "isDebugLogEnabled"
	
	static Properties urnProps = new Properties()
	static String aaiUriPfx
	
	String processName

	static {
		def fr = new FileReader("src/test/resources/mso.bpmn.urn.properties")
		urnProps.load(fr)
		fr.close()

		aaiUriPfx = urnProps.get("aai.endpoint")
	}

	public static void setUpBeforeClass() {
		// moved to the above static block to get the static aaiUriPfx assignment correctly.
	}
	
	public GroovyTestBase(String processName) {
		this.processName = processName
	}
	
	public boolean doBpmnError(def func) {
		
		try {
			func()
			return false;
			
		} catch(BpmnError e) {
			return true;
		}
	}
	
	public ExecutionEntity setupMock() {
		
		ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
		when(mockProcessDefinition.getKey()).thenReturn(processName)
		RepositoryService mockRepositoryService = mock(RepositoryService.class)
		when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
		when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn(processName)
		when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
		ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
		when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
		
		ExecutionEntity mex = mock(ExecutionEntity.class)
		
		when(mex.getId()).thenReturn("100")
		when(mex.getProcessDefinitionId()).thenReturn(processName)
		when(mex.getProcessInstanceId()).thenReturn(processName)
		when(mex.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
		when(mex.getProcessEngineServices().getRepositoryService().getProcessDefinition(mex.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
		
		when(mex.getVariable("isAsyncProcess")).thenReturn("true")
		when(mex.getVariable(processName+"WorkflowResponseSent")).thenReturn("false")
		
		return mex
	}
	
	public Map<String,Object> setupMap(ExecutionEntity mex) {
		MapSetter mapset = new MapSetter();
		doAnswer(mapset).when(mex).setVariable(any(), any())
		return mapset.getMap();
	}
		
}
