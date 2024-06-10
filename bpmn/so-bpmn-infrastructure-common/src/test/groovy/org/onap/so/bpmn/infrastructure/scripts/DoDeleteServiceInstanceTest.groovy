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
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.vcpe.scripts.GroovyTestBase

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoDeleteServiceInstanceTest {

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void preProcessRequestTest() {

        ExecutionEntity mex = setupMock()
        when(mex.getVariable(GroovyTestBase.DBGFLAG)).thenReturn("true")
        when(mex.getVariable("serviceInstanceId")).thenReturn("e151059a-d924-4629-845f-264db19e50b4")
        when(mex.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("/mso/sdncadapter/")
        when(mex.getVariable("globalSubscriberId")).thenReturn("4993921112123")

        DoDeleteServiceInstance instance = new DoDeleteServiceInstance()
        instance.preProcessRequest(mex)

        Mockito.verify(mex).setVariable("sdncCallbackUrl", "/mso/sdncadapter/")
        Mockito.verify(mex).setVariable("siParamsXml", "")
    }

   
    @Test
    public void testPostProcessAAIGET() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
        when(mockExecution.getVariable("GENGS_FoundIndicator")).thenReturn(true)
        when(mockExecution.getVariable("sdnc.si.svc.types")).thenReturn("")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId_test")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("subscriptionServiceType_test")

        String aaiGetResponse = FileUtil.readResourceFile("__files/GenericFlows/aaiGetResponse.xml")
        when(mockExecution.getVariable("GENGS_service")).thenReturn(aaiGetResponse)
        when(mockExecution.getVariable("GENGS_siResourceLink")).thenReturn("/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')

        mockData()
        DoDeleteServiceInstance instance = new DoDeleteServiceInstance()
        instance.postProcessAAIGET(mockExecution)

        Mockito.verify(mockExecution).setVariable("sendToSDNC", true)
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoDeleteServiceInstance")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoDeleteServiceInstance")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoDeleteServiceInstance")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoDeleteServiceInstance")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

    private void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/.*"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("")))
    }
}
