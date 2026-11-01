package org.onap.so.bpmn.common.scripts

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.Assert.assertThrows
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Test

class SDNCAdapterHttpTest {
    private final String workflow = '''<SDNCAdapterWorkflowRequest>
      <RequestHeader><RequestId>operation-1</RequestId><SvcInstanceId>service/Gr&#252;&#223;e</SvcInstanceId>
      <SvcAction>activate</SvcAction><SvcOperation>service-configuration-operation</SvcOperation>
      <MsoAction>generic-resource</MsoAction><CallbackUrl>http://localhost/old-callback</CallbackUrl></RequestHeader>
      <SDNCRequestData><service-type>uCPE-VMS</service-type><source>API &amp; SO</source>
      <value>Gr&#252;&#223;e &amp; SO</value></SDNCRequestData></SDNCAdapterWorkflowRequest>'''

    @Test
    void jsonPreservesLegacyFieldsAndCorrelationWithoutASoapEnvelope() {
        def legacy = context(workflow)
        def current = context(workflow)
        new SDNCAdapter().preProcessRequest(legacy)

        new SDNCAdapterHttp().preProcessRequest(current)

        Map payload = new JsonSlurper().parseText(current.getVariable('sdncHttpRequest') as String)
        def soap = new XmlSlurper().parseText(legacy.getVariable('sdncAdapterRequest') as String)
        def header = soap.Body.SDNCAdapterRequest.RequestHeader
        assertThat(payload.requestId).isEqualTo(header.RequestId.text())
        assertThat(payload.serviceInstanceId).isEqualTo(header.SvcInstanceId.text())
        assertThat(payload.svcAction).isEqualTo(header.SvcAction.text())
        assertThat(payload.svcOperation).isEqualTo(header.SvcOperation.text())
        assertThat(payload.msoAction).isEqualTo(header.MsoAction.text())
        assertThat(payload.callbackUrl).isEqualTo('http://localhost:8081/mso/sdnc/callbacks/v1')
        assertThat(current.getVariable('sdncHttpEndpoint')).isEqualTo('http://localhost:8086/adapters/rest/v2/sdnc/requests')
        def data = new XmlSlurper().parseText(payload.requestDataXml as String)
        assertThat(data.name()).isEqualTo('RequestData')
        assertThat(data.value.text()).isEqualTo(soap.Body.SDNCAdapterRequest.RequestData.value.text())
        assertThat(payload.keySet()).containsExactlyInAnyOrder('requestId', 'serviceInstanceId', 'svcAction',
            'svcOperation', 'msoAction', 'callbackUrl', 'requestDataXml')
        ['prefix', 'sdncAdapterResponse', 'asynchronousResponseTimeout', 'continueListening', 'SDNCA_SuccessIndicator',
            'SDNCA_InterimNotify', 'SDNCA_requestId', 'serviceConfigActivate', 'source', 'serviceInstanceId',
            'BasicAuthHeaderValue'].each { key ->
                assertThat(current.getVariable(key)).isEqualTo(legacy.getVariable(key))
        }
        assertThat(current.getVariable('sdncAdapterRequest')).isNull()
        assertThat(current.getVariable('sdncHttpRequest') as String).doesNotContain('Envelope', 'SOAP-ENV')
    }

    @Test
    void absentOptionalHeaderFieldsStayNull() {
        def current = context(workflow.replace('<SvcInstanceId>service/Gr&#252;&#223;e</SvcInstanceId>', '')
            .replace('<MsoAction>generic-resource</MsoAction>', ''))

        new SDNCAdapterHttp().preProcessRequest(current)

        Map payload = new JsonSlurper().parseText(current.getVariable('sdncHttpRequest') as String)
        assertThat(payload.serviceInstanceId).isNull()
        assertThat(payload.msoAction).isNull()
    }

    @Test
    void missingCallerRequestIdPreservesTheLegacyPreprocessingError() {
        def current = context(workflow, '')

        assertThrows(BpmnError) { new SDNCAdapterHttp().preProcessRequest(current) }

        assertThat(current.getVariable('WorkflowException').errorCode).isEqualTo(9999)
        assertThat(current.getVariable('sdncHttpRequest')).isNull()
    }

    private DelegateExecution context(String xml, String requestId = 'caller-1') {
        Map values = ['sdncAdapterWorkflowRequest': xml, 'mso-request-id': requestId,
            'testProcessKey': 'sdncAdapter',
            'mso.workflow.sdncadapter.callback': 'http://localhost:8081/mso/SDNCAdapterCallbackService',
            'mso.workflow.sdncadapter.http.callback': 'http://localhost:8081/mso/sdnc/callbacks/v1',
            'mso.adapters.sdnc.http.endpoint': 'http://localhost:8086/adapters/rest/v2/sdnc/requests',
            'so.auth.username': 'test-user', 'so.auth.password': 'synthetic-password']
        def execution = mock(DelegateExecution)
        when(execution.getVariable(anyString())).thenAnswer { invocation -> values[invocation.getArgument(0)] }
        doAnswer { invocation -> values[invocation.getArgument(0)] = invocation.getArgument(1); null }
            .when(execution).setVariable(anyString(), any())
        return execution
    }
}