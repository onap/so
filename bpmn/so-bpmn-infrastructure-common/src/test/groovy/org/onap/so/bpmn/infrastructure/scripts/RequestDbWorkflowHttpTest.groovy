package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Rule
import org.junit.Test
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.WorkflowException
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.apache.commons.lang3.tuple.ImmutablePair

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

class RequestDbWorkflowHttpTest {

    @Rule
    public WireMockRule requestDb = new WireMockRule(0)

    @Test
    void servicePreparationsPreserveFieldsAndSelectInitializationOrUpdate() {
        def scenarios = [
                [new DeleteCustomE2EServiceInstance(), 'prepareInitServiceOperationStatus'],
                [new DoCustomDeleteE2EServiceInstanceV2(), 'preUpdateServiceOperationStatus'],
                [new CreateCustomE2EServiceInstance(), 'prepareInitServiceOperationStatus'],
                [new UpdateCustomE2EServiceInstance(), 'prepareInitServiceOperationStatus'],
                [new UpdateCustomE2EServiceInstance(), 'preUpdateServiceOperationStatus'],
                [new DoDeleteE2EServiceInstance(), 'prepareUpdateServiceOperationStatus'],
                [new ScaleCustomE2EServiceInstance(), 'prepareInitServiceOperationStatus']]
        scenarios.each { scenario ->
            def initial = [serviceInstanceId: 'service%id/part', operationId: 'operation%id/part',
                    serviceInstanceName: 'service name', progress: '51', operationStatus: 'running',
                    operationType: 'UPDATE', operationResult: 'processing', operationReason: '',
                    result: 'processing', operationContent: 'progress update']
            def legacy = new LinkedHashMap(initial)
            scenario[0].invokeMethod(scenario[1], execution(legacy))
            def expected = new XmlSlurper().parseText(legacy.CVFMI_updateServiceOperStatusRequest)
                    .Body.children()[0].children().collectEntries { [(it.name()): it.text()] }
            expected.remove('serviceName')
            def current = new LinkedHashMap(initial)
            def currentExecution = execution(current)
            scenario[0].class.newInstance().invokeMethod(scenario[1] + 'Http', currentExecution)
            assertTrue(requestDb.allServeEvents.empty)
            boolean initialization = scenario[0] instanceof CreateCustomE2EServiceInstance
            if (initialization) {
                UUID.fromString(current.operationId)
                expected.operationId = current.operationId
            }
            assertEquals(scenario[0].class.simpleName, expected, current.CVFMI_updateServiceOperStatusRequestHttp)
            assertEquals(legacy.serviceInstanceId, current.serviceInstanceId)
            assertSerializable(current.CVFMI_updateServiceOperStatusRequestHttp)
            assertEquals(initialization ? 'initServiceOperationStatus' : 'updateOperationStatus',
                    current.CVFMI_updateServiceOperStatusRequestHttpOperation)
            [204, 401, 403, 404, 500].each { status ->
                requestDb.resetAll()
                String serviceQuery = scenario[0] instanceof ScaleCustomE2EServiceInstance ?
                        'service%25id%2Fpart' : 'service%2525id%252Fpart'
                String path = initialization ? '/v1/request-db/service-operations' :
                        '/v1/request-db/service-operations?serviceId=' + serviceQuery + '&operationId=operation%25id%2Fpart'
                requestDb.stubFor((initialization ? post(urlEqualTo(path)) : put(urlEqualTo(path)))
                        .willReturn(aResponse().withStatus(status)))
                new RequestDBUtil().invokeHttp(currentExecution, current.CVFMI_updateServiceOperStatusRequestHttpOperation,
                        'dbResponse', 'dbStatus', 'CVFMI_updateServiceOperStatusRequestHttp')
                assertEquals(status, current.dbStatus)
                assertEquals(status == 204 ? '' : null, current.dbResponse)
                requestDb.verify(1, (initialization ? postRequestedFor(urlEqualTo(path)) : putRequestedFor(urlEqualTo(path)))
                        .withHeader('Content-Type', containing('application/json'))
                        .withHeader('Authorization', equalTo(org.onap.so.utils.BasicAuth.header(
                                current['so.auth.username'], current['so.auth.password'])))
                        .withRequestBody(equalToJson(groovy.json.JsonOutput.toJson(expected), true, true)))
            }
            requestDb.resetAll()
        }
    }

    @Test
    void resourceUpdatesPreserveLegacyFieldsWithoutPreparingRequestsOnTheWire() {
        def scenarios = [
                [new CreateDeviceResource(), 'prepareUpdateProgress'],
                [new DeleteDeviceResource(), 'prepareUpdateProgress'],
                [new Create3rdONAPE2EServiceInstance(), 'prepareUpdateProgress'],
                [new Delete3rdONAPE2EServiceInstance(), 'prepareUpdateProgress'],
                [new CreateSDNCNetworkResource(), 'prepareUpdateBeforeCreateSDNCResource'],
                [new CreateSDNCNetworkResource(), 'prepareUpdateAfterCreateSDNCResource'],
                [new DeleteSDNCNetworkResource(), 'prepareUpdateBeforeDeleteSDNCResource'],
                [new DeleteSDNCNetworkResource(), 'prepareUpdateAfterDeleteSDNCResource'],
                [new ActivateSDNCNetworkResource(), 'prepareUpdateAfterActivateSDNCResource'],
                [new DeActivateSDNCNetworkResource(), 'prepareUpdateAfterDeActivateSDNCResource'],
                [new HandlePNF(), 'postProcessRequest'],
                [new DoDeleteResourcesV1(), 'prepareFinishedProgressForResource']]
        scenarios.each { scenario ->
            def model = new ModelInfo(modelCustomizationUuid: 'resource%id/part', modelName: 'model')
            def resource = new ResourceInput(serviceInstanceId: 'service%id/part', operationId: 'operation%id/part',
                    operationType: 'DELETE', resourceModelInfo: model)
            def initial = [serviceInstanceId: resource.serviceInstanceId, operationId: resource.operationId,
                    operationType: resource.operationType, progress: '51', status: 'processing',
                    statusDescription: 'progress', currentResource: new VnfResource(modelInfo: model),
                    resourceInput: new ObjectMapper().writeValueAsString(resource)]
            if (!(scenario[0] instanceof DoDeleteResourcesV1)) {
                initial[scenario[0].Prefix + 'ResourceInput'] = resource
                initial[scenario[0].Prefix + 'resourceInput'] = new ObjectMapper().writeValueAsString(resource)
            }
            def legacy = new LinkedHashMap(initial)
            scenario[0].invokeMethod(scenario[1], execution(legacy))
            def expected = new XmlSlurper().parseText(legacy.CVFMI_updateResOperStatusRequest)
                    .Body.children()[0].children().collectEntries { [(it.name()): it.text()] }
            def current = new LinkedHashMap(initial)
            def currentExecution = execution(current)
            scenario[0].class.newInstance().invokeMethod(scenario[1] + 'Http', currentExecution)
            assertTrue(requestDb.allServeEvents.empty)
            assertEquals(scenario[0].class.simpleName, expected, current.CVFMI_updateResOperStatusRequestHttp)
            assertEquals(legacy.serviceInstanceId, current.serviceInstanceId)
            assertSerializable(current.CVFMI_updateResOperStatusRequestHttp)
            def originalFailure = new WorkflowException('test', 500, 'original')
            current.WorkflowException = originalFailure
            [204, 401, 403, 404, 500].each { status ->
                requestDb.resetAll()
                String path = '/v1/request-db/resource-operations?serviceId=service%25id%2Fpart' +
                        '&operationId=operation%25id%2Fpart&resourceTemplateUUID=resource%25id%2Fpart'
                requestDb.stubFor(put(urlEqualTo(path)).willReturn(aResponse().withStatus(status)))
                new RequestDBUtil().invokeHttp(currentExecution, 'updateResourceOperationStatus',
                        'dbResponse', 'dbStatus', 'CVFMI_updateResOperStatusRequestHttp')
                assertEquals(status, current.dbStatus)
                assertEquals(status == 204 ? '' : null, current.dbResponse)
                assertSame(originalFailure, current.WorkflowException)
                def fields = expected.findAll { name, value ->
                    !(name in ['serviceId', 'operationId', 'resourceTemplateUUID'])
                }
                requestDb.verify(1, putRequestedFor(urlEqualTo(path))
                        .withHeader('Content-Type', containing('application/json'))
                        .withRequestBody(equalToJson(groovy.json.JsonOutput.toJson(fields), true, true)))
            }
            requestDb.resetAll()
        }
    }

    @Test
    void resourceInitializationPreservesTemplateListsAndIdentifiers() {
        def owners = [new DoCreateE2EServiceInstance(), new DoCustomDeleteE2EServiceInstance(),
                new DoCustomDeleteE2EServiceInstanceV2(), new DoDeleteE2EServiceInstance(),
                new DoScaleE2EServiceInstance(), new DoUpdateE2EServiceInstance()]
        owners.each { owner ->
            def resource = new VnfResource(modelInfo: new ModelInfo(modelCustomizationUuid: 'resource%id/part'))
            def initial = [serviceInstanceId: 'service%id/part', operationId: 'operation%id/part',
                    operationType: 'DELETE', resourceTemplateUUIDs: 'resource%id/part::',
                    serviceDecomposition: new ServiceDecomposition(vnfResources: [resource]),
                    addResourceList: [resource], delResourceList: [],
                    deleteResourceList: [new ImmutablePair(resource, [])],
                    serviceRelationShip: '[{"resourceInstanceId":"resource%id/part"}]']
            def legacy = new LinkedHashMap(initial)
            owner.preInitResourcesOperStatus(execution(legacy))
            Map expected
            if (owner instanceof DoUpdateE2EServiceInstance) {
                assertNull(legacy.CVFMI_initResOperStatusRequest)
                assertTrue(legacy.CVFMI_ErrorResponse.contains('No such property: UrnPropertiesReader'))
                assertEquals('service%25id%2Fpart', legacy.serviceInstanceId)
                expected = [serviceId: legacy.serviceInstanceId, operationId: initial.operationId,
                        operationType: 'DELETE', resourceTemplateUUIDs: 'resource%id/part:']
            } else {
                assertNotNull(owner.class.simpleName, legacy.CVFMI_initResOperStatusRequest)
                expected = new XmlSlurper().parseText(legacy.CVFMI_initResOperStatusRequest)
                        .Body.children()[0].children().collectEntries { [(it.name()): it.text()] }
            }
            def current = new LinkedHashMap(initial)
            def currentExecution = execution(current)
            owner.class.newInstance().preInitResourcesOperStatusHttp(currentExecution)
            assertEquals(owner.class.simpleName, expected, current.CVFMI_initResOperStatusRequestHttp)
            assertEquals(owner.class.simpleName, legacy.serviceInstanceId, current.serviceInstanceId)
            assertSerializable(current.CVFMI_initResOperStatusRequestHttp)
            assertTrue(requestDb.allServeEvents.empty)
            [204, 401, 403, 404, 500].each { status ->
                requestDb.resetAll()
                String path = '/v1/request-db/resource-operations'
                requestDb.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(status)))
                new RequestDBUtil().invokeHttp(currentExecution, 'initResourceOperationStatus',
                        'dbResponse', 'dbStatus', 'CVFMI_initResOperStatusRequestHttp')
                assertEquals(status, current.dbStatus)
                assertEquals(status == 204 ? '' : null, current.dbResponse)
                requestDb.verify(1, postRequestedFor(urlEqualTo(path))
                        .withHeader('Content-Type', containing('application/json'))
                        .withRequestBody(equalToJson(groovy.json.JsonOutput.toJson(expected), true, true)))
            }
            requestDb.resetAll()
            assertResourceUpdateUsesInitializedKey(legacy, current)
            requestDb.resetAll()
        }
    }

    private void assertResourceUpdateUsesInitializedKey(Map legacy, Map current) {
        legacy.currentResource = new VnfResource(modelInfo: new ModelInfo(modelCustomizationUuid: 'resource%id/part'))
        current.currentResource = new VnfResource(modelInfo: new ModelInfo(modelCustomizationUuid: 'resource%id/part'))
        new DoDeleteResourcesV1().prepareFinishedProgressForResource(execution(legacy))
        Map expected = new XmlSlurper().parseText(legacy.CVFMI_updateResOperStatusRequest)
                .Body.children()[0].children().collectEntries { [(it.name()): it.text()] }
        def context = execution(current)
        new DoDeleteResourcesV1().prepareFinishedProgressForResourceHttp(context)
        assertEquals(expected, current.CVFMI_updateResOperStatusRequestHttp)
        assertEquals(current.CVFMI_initResOperStatusRequestHttp.serviceId, expected.serviceId)
        assertEquals('service%25id%2Fpart', expected.serviceId)
        assertSerializable(current.CVFMI_updateResOperStatusRequestHttp)
        String path = '/v1/request-db/resource-operations?serviceId=service%2525id%252Fpart' +
                '&operationId=operation%25id%2Fpart&resourceTemplateUUID=resource%25id%2Fpart'
        requestDb.stubFor(post(urlEqualTo('/v1/request-db/resource-operations'))
                .withRequestBody(matchingJsonPath('$.serviceId', equalTo(expected.serviceId)))
                .inScenario('initialized-key').whenScenarioStateIs('Started').willSetStateTo('initialized')
                .willReturn(aResponse().withStatus(204)))
        requestDb.stubFor(put(urlEqualTo(path)).inScenario('initialized-key').whenScenarioStateIs('initialized')
                .willReturn(aResponse().withStatus(204)))
        def helper = new RequestDBUtil()
        helper.invokeHttp(context, 'initResourceOperationStatus', 'dbResponse', 'dbStatus',
                'CVFMI_initResOperStatusRequestHttp')
        assertEquals(204, current.dbStatus)
        helper.invokeHttp(context, 'updateResourceOperationStatus', 'dbResponse', 'dbStatus',
                'CVFMI_updateResOperStatusRequestHttp')
        assertEquals(204, current.dbStatus)
        requestDb.verify(1, postRequestedFor(urlEqualTo('/v1/request-db/resource-operations')))
        requestDb.verify(1, putRequestedFor(urlEqualTo(path)).withRequestBody(equalToJson(
                groovy.json.JsonOutput.toJson(expected.findAll { name, value ->
                    !(name in ['serviceId', 'operationId', 'resourceTemplateUUID'])
                }))))
    }

        @Test
        void currentDefinitionInvocationsPreserveOutputsAndOnlyRepeatWhenExecutedAgain() {
                def commands = [
                                CVFMI_updateServiceOperStatusRequestHttp: [operation: 'updateOperationStatus',
                                                fields: [serviceId: 'service', operationId: 'operation', result: 'processing'],
                                                path: '/v1/request-db/service-operations?serviceId=service&operationId=operation', method: 'PUT'],
                                CVFMI_initResOperStatusRequestHttp: [operation: 'initResourceOperationStatus',
                                                fields: [serviceId: 'service', operationId: 'operation', operationType: 'CREATE',
                                                                resourceTemplateUUIDs: 'resource::'],
                                                path: '/v1/request-db/resource-operations', method: 'POST'],
                                CVFMI_updateResOperStatusRequestHttp: [operation: 'updateResourceOperationStatus',
                                                fields: [serviceId: 'service', operationId: 'operation', resourceTemplateUUID: 'resource',
                                                                progress: '100', status: 'finished'],
                                                path: '/v1/request-db/resource-operations?serviceId=service&operationId=operation&resourceTemplateUUID=resource',
                                                method: 'PUT']]
                int nodes = 0
                new File('../so-bpmn-infrastructure-flows/src/main/resources').eachFileRecurse { file ->
                        if (file.name.endsWith('.bpmn')) {
                                def definition = new XmlSlurper().parse(file)
                                definition.depthFirst().findAll { it.name() == 'scriptTask' }.each { node ->
                                        String script = node.script.text()
                                        def entry = commands.find { name, command -> script.contains("'" + name + "')") }
                                        if (entry != null && script.contains('requestDb.invokeHttp')) {
                                                nodes++
                                                def command = entry.value
                                                [204, 500].each { status ->
                                                        requestDb.resetAll()
                                                            requestDb.stubFor((command.method == 'POST' ? post(urlEqualTo(command.path)) :
                                                                    put(urlEqualTo(command.path)))
                                                                        .willReturn(aResponse().withStatus(status)))
                                                        def values = [(entry.key): command.fields, (entry.key + 'Operation'): command.operation]
                                                        def currentExecution = execution(values)
                                                        def shell = new GroovyShell(new Binding(execution: currentExecution))
                                                        (1..2).each { attempts ->
                                                                shell.evaluate(script)
                                                                assertEquals(file.name + '/' + node.@id, status, values.CVFMI_dbResponseCode)
                                                                assertEquals(status == 204 ? '' : null, values.CVFMI_dbResponse)
                                                                requestDb.verify(attempts, (command.method == 'POST' ?
                                                                        postRequestedFor(urlEqualTo(command.path)) : putRequestedFor(urlEqualTo(command.path)))
                                                                                .withHeader('Content-Type', containing('application/json'))
                                                                                .withRequestBody(notMatching('(?s).*Envelope.*')))
                                                        }
                                                }
                                                requestDb.resetAll()
                                        }
                                }
                        }
                }
                assertEquals(35, nodes)
        }

        private void assertSerializable(Map command) {
                def bytes = new ByteArrayOutputStream()
                new ObjectOutputStream(bytes).withCloseable { it.writeObject(command) }
                new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).withCloseable {
                        assertEquals(command, it.readObject())
                }
        }

                private DelegateExecution execution(Map values) {
        values['mso.adapters.requestDb.endpoint'] = 'http://localhost:' + requestDb.port()
        values['so.auth.username'] = 'test-user'
        values['so.auth.password'] = UUID.randomUUID().toString()
        DelegateExecution result = mock(DelegateExecution)
        when(result.getVariable(anyString())).thenAnswer { values[it.getArgument(0)] }
        doAnswer { values[it.getArgument(0)] = it.getArgument(1); null }.when(result).setVariable(anyString(), any())
        return result
    }
}