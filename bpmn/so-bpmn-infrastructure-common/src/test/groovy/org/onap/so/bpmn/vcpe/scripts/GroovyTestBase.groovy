/*
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

package org.onap.so.bpmn.vcpe.scripts

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.AllottedResourceUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.aaiclient.client.aai.AAIResourcesClient

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class GroovyTestBase {

    static final int PORT = 28090
    static final String LOCAL_URI = "http://localhost:" + PORT

    static final String CUST = "SDN-ETHERNET-INTERNET"
    static final String SVC = "123456789"
    static final String INST = "MIS%252F1604%252F0026%252FSW_INTERNET"
    static final String ARID = "arId-1"
    static final String VERS = "myvers"

    static final String DBGFLAG = "isDebugLogEnabled"

    static String aaiUriPfx

    String processName

    AllottedResourceUtils allottedResourceUtils_MOCK

    @Mock
    AAIResourcesClient client_MOCK

    public static void setUpBeforeClass() {
        aaiUriPfx = UrnPropertiesReader.getVariable("aai.endpoint")
    }

    public GroovyTestBase(String processName) {
        this.processName = processName
    }

    public boolean doBpmnError(def func) {

        try {
            func()
            return false;

        } catch (BpmnError e) {
            return true;
        }
    }

    public ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn(processName)
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn(processName)
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mex = mock(ExecutionEntity.class)

        when(mex.getProcessDefinitionId()).thenReturn(processName)
        when(mex.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mex.getProcessEngineServices().getRepositoryService().getProcessDefinition(mex.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        when(mex.getVariable("isAsyncProcess")).thenReturn("true")
        when(mex.getVariable(processName + "WorkflowResponseSent")).thenReturn("false")

        return mex
    }

    public Map<String, Object> setupMap(ExecutionEntity mex) {
        MapSetter mapset = new MapSetter();
        doAnswer(mapset).when(mex).setVariable(any(), any())
        return mapset.getMap();
    }

    void initAllottedResourceMock() {
        allottedResourceUtils_MOCK = spy(new AllottedResourceUtils(mock(AbstractServiceTaskProcessor.class)))
    }

}
