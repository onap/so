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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class CreateVfModuleInfraTest {

    @Rule
    public WireMockRule requestDb = new WireMockRule(0)

    @Test
    void terminalUpdateUsesRestWithoutChangingTheLegacyProducer() {
    def values = ['CVFMI_requestInfo': '<requestInfo><request-id>request%id</request-id></requestInfo>',
        'CVFMI_vnfId': 'vnf-id', 'CVFMI_vfModuleId': 'module-id', 'CVFMI_vfModuleName': 'module & name',
        'mso.adapters.requestDb.endpoint': 'http://localhost:' + requestDb.port(),
        'so.auth.username': 'test-user', 'so.auth.password': UUID.randomUUID().toString()]
    def execution = setupMock()
    when(execution.getVariable(Mockito.anyString())).thenAnswer { values[it.getArgument(0)] }
    Mockito.doAnswer { values[it.getArgument(0)] = it.getArgument(1); null }
        .when(execution).setVariable(Mockito.anyString(), Mockito.any())
    def workflow = new CreateVfModuleInfra()
    workflow.prepareUpdateInfraRequestHttp(execution)
    org.junit.Assert.assertTrue(requestDb.allServeEvents.empty)
    def command = values['CVFMI_updateInfraRequestHttp']
    org.junit.Assert.assertEquals('request%id', command.requestId)
    org.junit.Assert.assertEquals('module & name', command.vfModuleName)
    org.junit.Assert.assertEquals('COMPLETE', command.requestStatus)
    org.junit.Assert.assertEquals('100', command.progress)
    org.junit.Assert.assertFalse(command.vnfOutputs.contains('Envelope'))
    [204, 404, 500].each { status ->
        requestDb.resetAll()
        requestDb.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(
            com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo('/v1/request-db/infra-requests/request%25id/updates'))
            .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(status)))
        new org.onap.so.bpmn.common.scripts.RequestDBUtil().invokeHttp(execution,
            'updateInfraRequest', 'restResponse', 'restStatus', 'CVFMI_updateInfraRequestHttp')
        org.junit.Assert.assertEquals(status, values.restStatus)
        requestDb.verify(1, com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor(
            com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo('/v1/request-db/infra-requests/request%25id/updates'))
            .withHeader('Content-Type', com.github.tomakehurst.wiremock.client.WireMock.containing('application/json'))
            .withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath('$.vfModuleName',
                com.github.tomakehurst.wiremock.client.WireMock.equalTo('module & name'))))
    }
    workflow.prepareUpdateInfraRequest(execution)
    org.junit.Assert.assertTrue(values['CVFMI_updateInfraRequest'].contains('soapenv:Envelope'))
    }

    def prefix = "CVFMI_"
    def requestInfo = "<requestInfo><request-id>12345</request-id></requestInfo>"

    @Test
    void infraHttpPreparationsPreserveLegacyFieldsIncludingEmptyValues() {
        def cases = [
                [new CreateVfModuleVolumeInfraV1(), 'prepareDbInfraSuccessRequest', 'CVMVINFRAV1_createDBRequest', true],
                [new DeleteVfModuleVolumeInfraV1(), 'prepareDBRequest', 'DELVfModVol_updateInfraRequest', true],
                [new DeleteVfModuleInfra(), 'prepUpdateInfraRequest', 'DELVfModI_updateInfraRequest', false],
                [new UpdateVfModuleVolumeInfraV1(), 'prepDbInfraDbRequest', 'UPDVfModVol_updateInfraRequest', true],
                [new org.onap.so.bpmn.vcpe.scripts.CreateVcpeResCustService(), 'postProcessServiceInstanceCreate',
                        'CVRCS_setUpdateDbInstancePayload', false]]
        cases.each { scenario ->
            [null, '', 'value%with/slash & text'].each { value ->
                def values = ['mso-request-id': 'request%id', 'CVMVINFRAV1_volumeOutputs': value,
                        'CVMVINFRAV1_volumeGroupId': value, 'DELVfModVol_requestId': 'request%id',
                        'DELVfModVol_volumeOutputs': value, 'DELVfModI_requestId': 'request%id',
                        'UPDVfModVol_requestId': 'request%id', 'serviceInstanceId': value, 'serviceInstanceName': value]
                def execution = setupMock()
                when(execution.getVariable(Mockito.anyString())).thenAnswer { values[it.getArgument(0)] }
                Mockito.doAnswer { values[it.getArgument(0)] = it.getArgument(1); null }
                        .when(execution).setVariable(Mockito.anyString(), Mockito.any())
                def arguments = scenario[3] ? [execution, false] : [execution]
                scenario[0].invokeMethod(scenario[1], arguments as Object[])
                def envelope = new XmlSlurper().parseText(values[scenario[2]])
                def expected = envelope.Body.children()[0].children().collectEntries { [(it.name()): it.text()] }
                scenario[0].invokeMethod(scenario[1] + 'Http', arguments as Object[])
                org.junit.Assert.assertEquals(scenario[0].class.simpleName, expected, values[scenario[2] + 'Http'])
            }
        }
    }

    @Captor
    static ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);
    }

   @Test
    void testPrepareUpdateInfraRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "requestInfo")).thenReturn(requestInfo)
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "vfModuleName")).thenReturn("12345")
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint")).thenReturn("http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

        CreateVfModuleInfra obj = new CreateVfModuleInfra()
        obj.prepareUpdateInfraRequest(mockExecution)
		Mockito.verify(mockExecution).setVariable(prefix + "dbAdapterEndpoint", "http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")
   }



    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("CreateVfModuleInfra")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("CreateVfModuleInfra")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("CreateVfModuleInfra")
        when(mockExecution.getProcessInstanceId()).thenReturn("CreateVfModuleInfra")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
