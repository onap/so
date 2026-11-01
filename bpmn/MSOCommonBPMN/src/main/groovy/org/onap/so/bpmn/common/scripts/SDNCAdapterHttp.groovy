package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.adapters.sdncrest.SdncRestRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class SDNCAdapterHttp extends SDNCAdapter {
    @Override
    void preProcessRequest(DelegateExecution execution) {
        try {
            execution.setVariable('prefix', Prefix)
            execution.setVariable('sdncAdapterResponse', '')
            execution.setVariable('asynchronousResponseTimeout', false)
            execution.setVariable('continueListening', false)
            execution.setVariable('SDNCA_SuccessIndicator', false)
            execution.setVariable('SDNCA_InterimNotify', false)
            if (StringUtils.isBlank(execution.getVariable('mso-request-id') as String)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 400,
                    'mso-request-id not provided by calling flow')
            }
            execution.setVariable('BasicAuthHeaderValue', UrnPropertiesReader.getSoBasicAuthHeader(execution))
            String workflow = execution.getVariable('sdncAdapterWorkflowRequest') as String
            String header = utils.getNodeXml(workflow, 'RequestHeader')
            String callback = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.http.callback', execution)
            if (UrnPropertiesReader.getVariable('mso.use.qualified.host', execution) == 'true') {
                callback = new MsoUtils().getQualifiedHostNameForCallback(callback)
            }
            String endpoint = UrnPropertiesReader.getVariable('mso.adapters.sdnc.http.endpoint', execution)
            if (StringUtils.isBlank(callback) || StringUtils.isBlank(endpoint)) {
                throw new IllegalArgumentException('SDNC HTTP endpoint and callback must be configured')
            }
            String serviceId = utils.getNodeText(header, 'SvcInstanceId')
            String action = utils.getNodeText(header, 'SvcAction')
            String operation = utils.getNodeText(header, 'SvcOperation')
            String requestId = utils.getNodeText(header, 'RequestId')
            String data = utils.getChildNodes(workflow, 'SDNCRequestData')
                .replace('<?xml version="1.0" encoding="UTF-8"?>\n', '')
                .replaceAll('tag0:', '').replaceAll(':tag0', '')
            String serviceType = utils.nodeExists(workflow, 'service-type') ?
                utils.getNodeText(workflow, 'service-type') : ''
            boolean activate = action == 'activate' && operation == 'service-configuration-operation' &&
                serviceType == 'uCPE-VMS'
            execution.setVariable('serviceConfigActivate', activate)
            execution.setVariable('source', activate && utils.nodeExists(workflow, 'source') ?
                utils.getNodeText(workflow, 'source') : '')
            execution.setVariable(Prefix + 'requestId', requestId)
            if (serviceId != null) {
                execution.setVariable('serviceInstanceId', serviceId)
            }
            def request = new SdncRestRequest(requestId: requestId, serviceInstanceId: serviceId,
                svcAction: action, svcOperation: operation, msoAction: utils.getNodeText(header, 'MsoAction'),
                callbackUrl: callback, requestDataXml: '<RequestData>' + data + '</RequestData>')
            def mapper = new ObjectMapper().disable(SerializationFeature.WRAP_ROOT_VALUE)
            execution.setVariable('sdncHttpRequest', mapper.writeValueAsString(request))
            execution.setVariable('sdncHttpEndpoint', endpoint)
        } catch (Exception failure) {
            LoggerFactory.getLogger(SDNCAdapterHttp).debug('SDNC HTTP preprocessing failed', failure)
            exceptionUtil.buildAndThrowWorkflowException(execution, 9999,
                'Internal Error occured during PreProcess Method')
        }
    }
}