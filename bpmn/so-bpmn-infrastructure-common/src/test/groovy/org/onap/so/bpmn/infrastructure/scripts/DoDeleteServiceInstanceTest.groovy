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
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.vcpe.scripts.GroovyTestBase
import org.springframework.mock.env.MockEnvironment

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class DoDeleteServiceInstanceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    @Captor
    static ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);
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
    public void testGetServiceInstance() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("e151059a-d924-4629-845f-264db19e50b4")
        when(mockExecution.getVariable("sdnc.si.svc.types")).thenReturn("")
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1707")

        mockData()
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(new MockEnvironment().withProperty("aai.endpoint", "http://localhost:" + wireMockRule.port()))
        try {
            DoDeleteServiceInstance instance = new DoDeleteServiceInstance()
            instance.getServiceInstance(mockExecution)
        } finally {
            urnPropertiesReader.setEnvironment(null)
        }

        Mockito.verify(mockExecution).setVariable("GENGS_FoundIndicator", true)
        Mockito.verify(mockExecution).setVariable("globalSubscriberId", "MSO_1610_dev")
        Mockito.verify(mockExecution).setVariable("subscriptionServiceType", "MSO-dev-service-type")
        Mockito.verify(mockExecution).setVariable("serviceType", "testservicetype")
        Mockito.verify(mockExecution).setVariable("serviceRole", "testservicerole")
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
        String siPath = "/aai/v[0-9]+/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/e151059a-d924-4629-845f-264db19e50b4"
        wireMockRule.stubFor(get(urlPathMatching("/aai/v[0-9]+/nodes/service-instances/service-instance/e151059a-d924-4629-845f-264db19e50b4"))
                .willReturn(okJson("""{"results":[{"resource-type":"service-instance","resource-link":"/aai/v19/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/e151059a-d924-4629-845f-264db19e50b4"}]}""")))
        wireMockRule.stubFor(get(urlPathMatching(siPath))
                .willReturn(okJson("""{"service-instance-id":"e151059a-d924-4629-845f-264db19e50b4","service-type":"testservicetype","service-role":"testservicerole","orchestration-status":"Active","resource-version":"1508838121849","relationship-list":{"relationship":[{"related-to":"service-instance","related-link":"/aai/v19/business/customers/customer/test_customer/service-subscriptions/service-subscription/example-service-type/service-instances/service-instance/1234_1"}]}}""")))
    }
}
