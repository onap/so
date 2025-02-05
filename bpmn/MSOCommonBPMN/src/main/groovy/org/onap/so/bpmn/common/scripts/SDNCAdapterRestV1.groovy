/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.onap.so.bpmn.common.scripts

import org.onap.so.logger.LoggingAnchor
import org.onap.so.client.HttpClientFactory
import org.onap.so.logging.filter.base.ErrorCode

import java.text.SimpleDateFormat
import javax.ws.rs.core.Response
import java.net.URLEncoder

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

import groovy.json.*

import org.json.JSONObject

import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.RollbackData
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.logging.filter.base.ONAPComponents;

class SDNCAdapterRestV1 extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( SDNCAdapterRestV1.class)


    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    /**
     * Processes the incoming request.
     */
    public void preProcessRequest (DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessRequest(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        def prefix="SDNCREST_"
        execution.setVariable("prefix", prefix)
        setSuccessIndicator(execution, false)

        try {
            // Determine the request type and log the request

            String request = validateRequest(execution, "mso-request-id")
            String requestType = jsonUtil.getJsonRootProperty(request)
            execution.setVariable(prefix + 'requestType', requestType)
            logger.debug(getProcessKey(execution) + ': ' + prefix + 'requestType = ' + requestType)

            // Determine the SDNCAdapter endpoint

            String sdncAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.sdnc.rest.endpoint", execution)

            if (sdncAdapterEndpoint == null || sdncAdapterEndpoint.isEmpty()) {
                String msg = getProcessKey(execution) + ': mso:adapters:sdnc:rest:endpoint URN mapping is not defined'
                logger.debug(msg)
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue())
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
            }

            while (sdncAdapterEndpoint.endsWith('/')) {
                sdncAdapterEndpoint = sdncAdapterEndpoint.substring(0, sdncAdapterEndpoint.length()-1)
            }

            String sdncAdapterMethod = null
            String sdncAdapterUrl = null
            String sdncAdapterRequest = request

            if ('SDNCServiceRequest'.equals(requestType)) {
                // Get the sdncRequestId from the request

                String sdncRequestId = jsonUtil.getJsonValue(request, requestType + ".sdncRequestId")

                if (sdncRequestId == null || sdncRequestId.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no sdncRequestId in ' + requestType
                    logger.debug(msg)
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue())
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
                }

                execution.setVariable('SDNCAResponse_CORRELATOR', sdncRequestId)
                logger.debug(getProcessKey(execution) + ': SDNCAResponse_CORRELATOR = ' + sdncRequestId)

                // Get the bpNotificationUrl from the request (just to make sure it's there)

                String bpNotificationUrl = jsonUtil.getJsonValue(request, requestType + ".bpNotificationUrl")

                if (bpNotificationUrl == null || bpNotificationUrl.isEmpty()) {
                    String msg = getProcessKey(execution) + ': no bpNotificationUrl in ' + requestType
                    logger.debug(msg)
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                            ErrorCode.UnknownError.getValue())
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
                }

                sdncAdapterMethod = 'POST'
                sdncAdapterUrl = sdncAdapterEndpoint

                RollbackData rollbackData = new RollbackData()
                rollbackData.setRequestId(sdncRequestId)
                rollbackData.getAdditionalData().put("service", jsonUtil.getJsonValue(request, requestType + ".sdncService"))
                rollbackData.getAdditionalData().put("operation", jsonUtil.getJsonValue(request, requestType + ".sdncOperation"))
                execution.setVariable("RollbackData", rollbackData)

            } else {
                String msg = getProcessKey(execution) + ': Unsupported request type: ' + requestType
                logger.debug(msg)
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue())
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
            }

            execution.setVariable(prefix + 'sdncAdapterMethod', sdncAdapterMethod)
            execution.setVariable(prefix + 'sdncAdapterUrl', sdncAdapterUrl)
            execution.setVariable(prefix + 'sdncAdapterRequest', sdncAdapterRequest)

            // Get the Basic Auth credentials for the SDNCAdapter (yes... we ARE using the PO adapters credentials)

            String basicAuthValue = UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

            if (basicAuthValue == null || basicAuthValue.isEmpty()) {
                logger.debug(getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined")
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                        getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined", "BPMN",
                        ErrorCode.UnknownError.getValue())
            } else {
                try {
                    def encodedString = utils.getBasicAuth(basicAuthValue, UrnPropertiesReader.getVariable("mso.msoKey", execution))
                    execution.setVariable(prefix + 'basicAuthHeaderValue', encodedString)
                } catch (IOException ex) {
                    logger.debug(getProcessKey(execution) + ": Unable to encode BasicAuth credentials for SDNCAdapter")
                    logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                            getProcessKey(execution) + ": Unable to encode BasicAuth credentials for SDNCAdapter",
                            "BPMN", ErrorCode.UnknownError.getValue(), ex)
                }
            }

            // Set the timeout value, e.g. PT5M. It may be specified in the request as the
            // bpTimeout value.  If it's not in the request, use the URN mapping value.

            String timeout = jsonUtil.getJsonValue(request, requestType + ".bpTimeout")

            // in addition to null/empty, also need to verify that the timer value is a valid duration "P[n]T[n]H|M|S"
            String timerRegex = "PT[0-9]+[HMS]"
            if (timeout == null || timeout.isEmpty() || !timeout.matches(timerRegex)) {
                logger.debug(getProcessKey(execution) + ': preProcessRequest(): null/empty/invalid bpTimeout value. Using "mso.adapters.sdnc.timeout"')
                timeout = UrnPropertiesReader.getVariable("mso.adapters.sdnc.timeout", execution)
            }

            // the timeout could still be null at this point if the config parm is missing/undefined
            // forced to log (so OPs can fix the config) and temporarily use a hard coded value of 10 seconds
            if (timeout == null) {
                msoLogger.warnSimple('preProcessRequest()', 'property "mso.adapters.sdnc.timeout" is missing/undefined. Using "PT10S"')
                timeout = "PT10S"
            }

            execution.setVariable(prefix + 'timeout', timeout)

            Boolean failOnCallbackError = execution.getVariable("failOnCallbackError")
            if(failOnCallbackError == null) {
                execution.setVariable("failOnCallbackError", true)
            }

            logger.debug(getProcessKey(execution) + ': ' + prefix + 'timeout = ' + timeout)
        } catch (BpmnError e) {
            throw e
        } catch (Exception e) {
            String msg = 'Caught exception in ' + method + ": " + e
            logger.debug(msg)
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue())
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
        }
    }

    /**
     * Sends the request to the SDNC adapter.
     */
    public void sendRequestToSDNCAdapter(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.sendRequestToSDNCAdapter(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        String prefix = execution.getVariable('prefix')

        try {
            String sdncAdapterMethod = execution.getVariable(prefix + 'sdncAdapterMethod')
            logger.debug("SDNC Method is: " + sdncAdapterMethod)
            String sdncAdapterUrl = execution.getVariable(prefix + 'sdncAdapterUrl')
            logger.debug("SDNC Url is: " + sdncAdapterUrl)
            String sdncAdapterRequest = execution.getVariable(prefix + 'sdncAdapterRequest')
            logger.debug("SDNC Rest Request is: " + sdncAdapterRequest)

            URL url = new URL(sdncAdapterUrl)

            HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.SDNC_ADAPTER)
            httpClient.addAdditionalHeader("mso-request-id", execution.getVariable("mso-request-id"))
            httpClient.addAdditionalHeader("mso-service-instance-id", execution.getVariable("mso-service-instance-id"))
            httpClient.addAdditionalHeader("Authorization", execution.getVariable(prefix + "basicAuthHeaderValue"))

            Response response

            if ("GET".equals(sdncAdapterMethod)) {
                response = httpClient.get()
            } else if ("PUT".equals(sdncAdapterMethod)) {
                response = httpClient.put(sdncAdapterRequest)
            } else if ("POST".equals(sdncAdapterMethod)) {
                response = httpClient.post(sdncAdapterRequest)
            } else if ("DELETE".equals(sdncAdapterMethod)) {
                response = httpClient.delete(sdncAdapterRequest)
            } else {
                String msg = 'Unsupported HTTP method "' + sdncAdapterMethod + '" in ' + method + ": " + e
                logger.debug(msg)
                logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                        ErrorCode.UnknownError.getValue())
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
            }

            execution.setVariable(prefix + "sdncAdapterStatusCode", response.getStatus())
            if(response.hasEntity()){
                execution.setVariable(prefix + "sdncAdapterResponse", response.readEntity(String.class))
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception e) {
            String msg = 'Caught exception in ' + method + ": " + e
            logger.debug(msg, e)
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue())
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
        }
    }

    /**
     * Processes a callback.
     */
    public void processCallback(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.processCallback(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        String prefix = execution.getVariable('prefix')
        String callback = execution.getVariable('SDNCAResponse_MESSAGE')
        logger.debug("Incoming SDNC Rest Callback is: " + callback)

        try {
            int callbackNumber = 1
            while (execution.getVariable(prefix + 'callback' + callbackNumber) != null) {
                ++callbackNumber
            }

            execution.setVariable(prefix + 'callback' + callbackNumber, callback)
            execution.removeVariable('SDNCAResponse_MESSAGE')

            String responseType = jsonUtil.getJsonRootProperty(callback)

            // Get the ackFinalIndicator and make sure it's either Y or N.  Default to Y.
            String ackFinalIndicator = jsonUtil.getJsonValue(callback, responseType + ".ackFinalIndicator")

            if (!'N'.equals(ackFinalIndicator)) {
                ackFinalIndicator = 'Y'
            }

            execution.setVariable(prefix + "ackFinalIndicator", ackFinalIndicator)

            if (responseType.endsWith('Error')) {
                Boolean failOnCallbackError = execution.getVariable("failOnCallbackError")
                if(failOnCallbackError) {
                    sdncAdapterBuildWorkflowException(execution, callback)
                }else {
                    if(ackFinalIndicator.equals('Y')) {
                        sdncAdapterBuildWorkflowException(execution, callback)
                    }
                }
            }

        } catch (Exception e) {
            callback = callback == null || String.valueOf(callback).isEmpty() ? "NONE" : callback
            String msg = "Received error from SDNCAdapter: " + callback
            logger.debug(getProcessKey(execution) + ': ' + msg)
            exceptionUtil.buildWorkflowException(execution, 5300, msg)
        }
    }

    /**
     * Tries to parse the response as XML to extract the information to create
     * a WorkflowException.  If the response cannot be parsed, a more generic
     * WorkflowException is created.
     */
    public void sdncAdapterBuildWorkflowException(DelegateExecution execution, String response) {
        try {
            String responseType = jsonUtil.getJsonRootProperty(response)
            String responseCode = jsonUtil.getJsonValue(response, responseType + ".responseCode")
            String responseMessage = jsonUtil.getJsonValue(response, responseType + ".responseMessage")

            String info = ""

            if (responseCode != null && !responseCode.isEmpty()) {
                info += " responseCode='" + responseCode + "'"
            }

            if (responseMessage != null && !responseMessage.isEmpty()) {
                info += " responseMessage='" + responseMessage + "'"
            }

            // Note: the mapping function handles a null or empty responseCode
            int mappedResponseCode = Integer.parseInt(exceptionUtil.MapSDNCResponseCodeToErrorCode(responseCode))
            exceptionUtil.buildWorkflowException(execution, mappedResponseCode, "Received " + responseType +
                    " from SDNCAdapter:" + info)
        } catch (Exception e) {
            response = response == null || String.valueOf(response).isEmpty() ? "NONE" : response
            exceptionUtil.buildWorkflowException(execution, 5300, "Received error from SDNCAdapter: " + response)
        }
    }

    /**
     * Gets the last callback request from the execution, or null if there was no callback.
     */
    public String getLastCallback(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.getLastCallback(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        String prefix = execution.getVariable('prefix')

        try {
            int callbackNumber = 1
            String callback = null

            while (true) {
                String thisCallback = (String) execution.getVariable(prefix + 'callback' + callbackNumber)

                if (thisCallback == null) {
                    break
                }

                callback = thisCallback
                ++callbackNumber
            }

            return callback
        } catch (Exception e) {
            String msg = 'Caught exception in ' + method + ": " + e
            logger.debug(msg)
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue())
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
        }
    }

    /**
     * Sets the timeout value to wait for the next notification.
     */
    public void setTimeoutValue(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.setTimeoutValue(' +
                'execution=' + execution.getId() +
                ')'
        logger.trace('Entered ' + method)

        String prefix = execution.getVariable('prefix')

        try {
            def timeoutValue = UrnPropertiesReader.getVariable("mso.adapters.sdnc.timeout", execution)

            if (execution.getVariable(prefix + 'callback1') != null) {
                // Waiting for subsequent notifications
            }
        } catch (Exception e) {
            String msg = 'Caught exception in ' + method + ": " + e
            logger.debug(msg)
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                    ErrorCode.UnknownError.getValue())
            exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
        }
    }

    public Logger getLogger() {
        return logger
    }
}
