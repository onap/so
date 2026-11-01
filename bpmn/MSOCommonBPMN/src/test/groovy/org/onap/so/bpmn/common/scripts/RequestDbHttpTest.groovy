package org.onap.so.bpmn.common.scripts

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static org.assertj.core.api.Assertions.assertThat
import static org.junit.Assert.assertThrows
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.WireMockServer
import java.net.http.HttpTimeoutException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Rule
import org.junit.Test
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.db.request.client.RequestDbCommands
import org.onap.so.utils.BasicAuth
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

class RequestDbHttpTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort())

    private DelegateExecution execution(String readTimeout = '1000', String connectTimeout = '1000') {
        def values = [
            'mso.adapters.requestDb.endpoint': 'http://localhost:' + wireMock.port(),
            'mso.adapters.requestDb.http.connectTimeoutMillis': connectTimeout,
            'mso.adapters.requestDb.http.readTimeoutMillis': readTimeout,
            'so.auth.username': 'test-user',
            'so.auth.password': UUID.randomUUID().toString()
        ]
        DelegateExecution execution = mock(DelegateExecution)
        when(execution.getVariable(anyString())).thenAnswer { invocation -> values[invocation.getArgument(0)] }
        doAnswer { invocation -> values[invocation.getArgument(0)] = invocation.getArgument(1); null }
            .when(execution).setVariable(anyString(), any())
        return execution
    }

    @Test
    void writeDoesNotReuseAnUnrelatedClientsConnectionAfterServerRestart() {
        def context = execution()
        int port = wireMock.port()
        wireMock.stubFor(get(urlEqualTo('/earlier-read')).willReturn(aResponse().withStatus(204)))
        new RestTemplate().getForEntity('http://localhost:' + port + '/earlier-read', Void)
        wireMock.stop()
        def replacement = new WireMockServer(port)
        replacement.start()
        try {
            replacement.stubFor(put(urlPathEqualTo('/v1/request-db/resource-operations'))
                .willReturn(aResponse().withStatus(204)))

            RequestDbHttp.createClient(context).updateResourceOperationStatus('service', 'operation', 'template',
                new RequestDbCommands.ResourceOperationUpdate(status: 'FINISHED', progress: '100'))

            replacement.verify(1, putRequestedFor(urlPathEqualTo('/v1/request-db/resource-operations'))
                .withQueryParam('serviceId', equalTo('service'))
                .withRequestBody(matchingJsonPath('$.status', equalTo('FINISHED'))))
        } finally {
            replacement.stop()
        }
    }

    @Test
    void timedOutWriteIsNotRetried() {
        wireMock.stubFor(put(urlPathEqualTo('/v1/request-db/resource-operations'))
            .willReturn(aResponse().withStatus(204).withFixedDelay(500)))
        def client = RequestDbHttp.createClient(execution('50'))

        assertThrows(ResourceAccessException) {
            client.updateResourceOperationStatus('service', 'operation', 'template',
                new RequestDbCommands.ResourceOperationUpdate(status: 'FINISHED', progress: '100'))
        }

        wireMock.verify(1, putRequestedFor(urlPathEqualTo('/v1/request-db/resource-operations')))
    }

    @Test
    void configuredCredentialsReachRequestDb() {
        DelegateExecution context = execution()
        wireMock.stubFor(get(urlEqualTo('/v1/request-db/sites/site/status'))
                .willReturn(aResponse().withHeader('Content-Type', 'application/json').withBody('{"enabled":true}')))

        assertThat(RequestDbHttp.createClient(context).getSiteStatus('site')).isTrue()

        wireMock.verify(1, getRequestedFor(urlEqualTo('/v1/request-db/sites/site/status'))
                .withHeader('Authorization', equalTo(BasicAuth.header(
                        context.getVariable('so.auth.username') as String,
                        context.getVariable('so.auth.password') as String))))
        org.mockito.Mockito.verify(context, never()).setVariable(eq('so.auth.password'), any())
    }

    @Test
    void readTimeoutIsAppliedWithoutRetry() {
        wireMock.stubFor(get(urlEqualTo('/v1/request-db/sites/site/status'))
                .willReturn(aResponse().withFixedDelay(500).withHeader('Content-Type', 'application/json')
                        .withBody('{"enabled":true}')))
        def client = RequestDbHttp.createClient(execution('50'))

        ResourceAccessException failure = assertThrows(ResourceAccessException) { client.getSiteStatus('site') }

        assertThat(failure).hasCauseInstanceOf(HttpTimeoutException)
        wireMock.verify(1, getRequestedFor(urlEqualTo('/v1/request-db/sites/site/status')))
    }

    @Test
        void sharedHelperWritesJsonAtInvocationOnly() {
        def context = execution()
        def helper = RequestDBUtil.forHttp()
        helper.prepareUpdateOperationStatus(context, new OperationStatus(serviceId: 'service/%20',
            operationId: 'operation/%25', operation: 'CREATE', result: 'processing', progress: '0',
            operationContent: '<business>value & more</business>'))
        helper.prepareInitResourceOperationStatus(context, new ResourceOperationStatus(serviceId: 'service/%20',
            operationId: 'operation/%25', operType: 'CREATE', resourceTemplateUUID: 'first::second:'))
        helper.prepareUpdateResourceOperationStatus(context, new ResourceOperationStatus(serviceId: 'service/%20',
            operationId: 'operation/%25', resourceTemplateUUID: 'template/%', resourceInstanceID: 'instance',
            operType: 'DELETE', jobId: 'job', status: 'failed', progress: '0'))
        assertThat(wireMock.allServeEvents).isEmpty()
        wireMock.stubFor(put(urlPathEqualTo('/v1/request-db/service-operations')).willReturn(aResponse().withStatus(204)))
        wireMock.stubFor(post(urlPathEqualTo('/v1/request-db/resource-operations')).willReturn(aResponse().withStatus(204)))
        wireMock.stubFor(put(urlPathEqualTo('/v1/request-db/resource-operations')).willReturn(aResponse().withStatus(204)))

        ['updateOperationStatus', 'initResourceOperationStatus', 'updateResourceOperationStatus'].each { operation ->
            helper.invokeHttp(context, operation, 'dbResponse', 'dbResponseCode')
            assertThat(context.getVariable('dbResponseCode')).isEqualTo(204)
            assertThat(context.getVariable('dbResponse')).isEqualTo('')
        }
        wireMock.verify(1, putRequestedFor(urlPathEqualTo('/v1/request-db/service-operations'))
            .withQueryParam('serviceId', equalTo('service%2F%2520'))
            .withQueryParam('operationId', equalTo('operation/%25'))
            .withHeader('Content-Type', containing('application/json'))
            .withRequestBody(matchingJsonPath('$.operationContent', equalTo('<business>value & more</business>')))
            .withRequestBody(matchingJsonPath('$.reason', equalTo(''))))
        wireMock.verify(1, postRequestedFor(urlPathEqualTo('/v1/request-db/resource-operations'))
            .withRequestBody(matchingJsonPath('$.serviceId', equalTo('service%2F%2520')))
            .withRequestBody(matchingJsonPath('$.resourceTemplateUUIDs', equalTo('first::second:'))))
        wireMock.verify(1, putRequestedFor(urlPathEqualTo('/v1/request-db/resource-operations'))
            .withQueryParam('serviceId', equalTo('service%2F%2520'))
            .withQueryParam('resourceTemplateUUID', equalTo('template/%'))
            .withRequestBody(equalToJson('{"operType":"DELETE","resourceInstanceID":"instance",' +
                '"jobId":"job","status":"failed","progress":"0","errorCode":"","statusDescription":""}')))
        assertThat(wireMock.allServeEvents).hasSize(3)
        }

        @Test
        void sharedHelperReturnsTypedEntityAndMissingLookupStatus() {
        def context = execution()
        def helper = RequestDBUtil.forHttp()
        helper.getOperationStatus(context, 'service/%20', 'operation/%25')
        wireMock.stubFor(get(urlPathEqualTo('/v1/request-db/service-operations')).willReturn(okJson(
            '{"serviceId":"service%2F%2520","operationId":"operation%2F%2525","result":"finished","progress":"100"}')))
        helper.invokeHttp(context, 'getOperationStatus', 'dbResponse', 'dbResponseCode')
        assertThat(context.getVariable('dbResponse')).isInstanceOf(OperationStatus)
        assertThat((context.getVariable('dbResponse') as OperationStatus).result).isEqualTo('finished')
        assertThat(context.getVariable('dbResponseCode')).isEqualTo(200)
        wireMock.stubFor(get(urlPathEqualTo('/v1/request-db/service-operations')).willReturn(aResponse().withStatus(404)))
        helper.invokeHttp(context, 'getOperationStatus', 'dbResponse', 'dbResponseCode')
        assertThat(context.getVariable('dbResponse')).isNull()
        assertThat(context.getVariable('dbResponseCode')).isEqualTo(404)
        wireMock.verify(2, getRequestedFor(urlPathEqualTo('/v1/request-db/service-operations'))
            .withQueryParam('serviceId', equalTo('service%2F%2520'))
            .withQueryParam('operationId', equalTo('operation%2F%2525')))
        }

    @Test
    void sharedWriteLookupAndUpdateUseTheLegacyPersistedKey() {
        def legacy = execution()
        def current = execution()
        String serviceId = 'service%id/part'
        String operationId = '99eb6097-3200-42bf-9a73-938672c47d2c'
        def fields = [serviceId: serviceId, operationId: operationId, operation: 'UPDATE',
            result: 'processing', progress: '0']
        def legacyStatus = new OperationStatus(fields)
        def currentStatus = new OperationStatus(fields)
        new RequestDBUtil().prepareUpdateOperationStatus(legacy, legacyStatus)
        Map expected = soapFields(legacy.getVariable('updateOperationStatus') as String)
        assertThat(expected.serviceId).isEqualTo('service%25id%2Fpart')
        String path = '/v1/request-db/service-operations?serviceId=service%2525id%252Fpart&operationId=' + operationId
        wireMock.stubFor(put(urlEqualTo(path))
            .withRequestBody(equalToJson(groovy.json.JsonOutput.toJson(expected)))
            .inScenario('legacy-key').whenScenarioStateIs('Started').willSetStateTo('written')
            .willReturn(aResponse().withStatus(204)))
        wireMock.stubFor(get(urlEqualTo(path)).inScenario('legacy-key').whenScenarioStateIs('written')
            .willSetStateTo('read').willReturn(okJson(groovy.json.JsonOutput.toJson(
                [serviceId: expected.serviceId, operationId: expected.operationId, progress: '0']))))
        wireMock.stubFor(put(urlEqualTo(path)).inScenario('legacy-key').whenScenarioStateIs('read')
            .withRequestBody(matchingJsonPath('$.progress', equalTo('100')))
            .willReturn(aResponse().withStatus(204)))

        def helper = RequestDBUtil.forHttp()
        helper.prepareUpdateOperationStatus(current, currentStatus)
        helper.invokeHttp(current, 'updateOperationStatus', 'response', 'code')
        assertThat(current.getVariable('code')).isEqualTo(204)
        new RequestDBUtil().getOperationStatus(legacy, serviceId, operationId)
        helper.getOperationStatus(current, serviceId, operationId)
        Map lookup = soapFields(legacy.getVariable('getOperationStatus') as String)
        assertThat(current.getVariable('getOperationStatusHttp')).isEqualTo(lookup)
        assertThat(lookup.serviceId).isEqualTo(expected.serviceId)
        helper.invokeHttp(current, 'getOperationStatus', 'response', 'code')
        assertThat(current.getVariable('code')).isEqualTo(200)
        assertThat((current.getVariable('response') as OperationStatus).serviceId).isEqualTo(expected.serviceId)
        currentStatus.progress = '100'
        helper.prepareUpdateOperationStatus(current, currentStatus)
        assertThat((current.getVariable('updateOperationStatusHttp') as Map).serviceId).isEqualTo(expected.serviceId)
        helper.invokeHttp(current, 'updateOperationStatus', 'response', 'code')
        assertThat(current.getVariable('code')).isEqualTo(204)
        assertThat(currentStatus.serviceId).isEqualTo(legacyStatus.serviceId).isEqualTo(serviceId)
        wireMock.verify(2, putRequestedFor(urlEqualTo(path)))
        wireMock.verify(1, getRequestedFor(urlEqualTo(path)))
    }

    private Map soapFields(String payload) {
        new XmlSlurper().parseText(payload).Body.children()[0].children()
            .collectEntries { [(it.name()): it.text()] }
    }

        @Test
        void sharedHelperPreservesFailureCodesWithoutRetryOrReplacingWorkflowError() {
        def context = execution()
        def helper = RequestDBUtil.forHttp()
        def workflowError = new WorkflowException('process', 7000, 'original failure')
        context.setVariable('WorkflowException', workflowError)
        helper.prepareUpdateOperationStatus(context, new OperationStatus(serviceId: 'service', operationId: 'operation'))
        [401, 403, 404, 500].each { code ->
            wireMock.stubFor(put(urlPathEqualTo('/v1/request-db/service-operations')).willReturn(aResponse().withStatus(code)))
            helper.invokeHttp(context, 'updateOperationStatus', 'dbResponse', 'dbResponseCode')
            assertThat(context.getVariable('dbResponseCode')).isEqualTo(code)
            assertThat(context.getVariable('dbResponse')).isNull()
            assertThat(context.getVariable('WorkflowException')).isSameAs(workflowError)
        }
        wireMock.verify(4, putRequestedFor(urlPathEqualTo('/v1/request-db/service-operations')))
        }

        @Test
    void nonPositiveTimeoutsAreRejected() {
        assertThrows(IllegalArgumentException) { RequestDbHttp.createClient(execution('0')) }
        assertThrows(IllegalArgumentException) { RequestDbHttp.createClient(execution('1000', '-1')) }
    }
}