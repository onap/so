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

package org.onap.so.bpmn.common.scripts

import java.text.SimpleDateFormat
import java.net.URLEncoder

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

import groovy.json.*

import org.json.JSONObject

import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger



/**
 * This version of SDNCAdapterRest allows for interim notifications to be sent for
 * any non-final response received from SDNC.
 */
class SDNCAdapterRestV2 extends SDNCAdapterRestV1 {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCAdapterRestV2.class);


	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	/**
	 * Processes the incoming request.
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		def prefix="SDNCREST_"
		execution.setVariable("prefix", prefix)
		setSuccessIndicator(execution, false)

		try {
			// Determine the request type and log the request

			String request = validateRequest(execution, "mso-request-id")
			String requestType = jsonUtil.getJsonRootProperty(request)
			execution.setVariable(prefix + 'requestType', requestType)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'requestType = ' + requestType)
			msoLogger.debug('SDNCAdapterRestV2, request: ' + request)

			// Determine the SDNCAdapter endpoint

			String sdncAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.sdnc.rest.endpoint",execution)

			if (sdncAdapterEndpoint == null || sdncAdapterEndpoint.isEmpty()) {
				String msg = getProcessKey(execution) + ': mso:adapters:sdnc:rest:endpoint URN mapping is not defined'
				msoLogger.debug(msg)
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
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
					msoLogger.debug(msg)
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				execution.setVariable('SDNCAResponse_CORRELATOR', sdncRequestId)
				msoLogger.debug(getProcessKey(execution) + ': SDNCAResponse_CORRELATOR = ' + sdncRequestId)

				// Get the bpNotificationUrl from the request (just to make sure it's there)

				String bpNotificationUrl = jsonUtil.getJsonValue(request, requestType + ".bpNotificationUrl")

				if (bpNotificationUrl == null || bpNotificationUrl.isEmpty()) {
					String msg = getProcessKey(execution) + ': no bpNotificationUrl in ' + requestType
					msoLogger.debug(msg)
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				sdncAdapterMethod = 'POST'
				sdncAdapterUrl = sdncAdapterEndpoint

			} else {
				String msg = getProcessKey(execution) + ': Unsupported request type: ' + requestType
				msoLogger.debug(msg)
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable(prefix + 'sdncAdapterMethod', sdncAdapterMethod)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'sdncAdapterMethod = ' + sdncAdapterMethod)
			execution.setVariable(prefix + 'sdncAdapterUrl', sdncAdapterUrl)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'sdncAdapterUrl = ' + sdncAdapterUrl)
			execution.setVariable(prefix + 'sdncAdapterRequest', sdncAdapterRequest)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'sdncAdapterRequest = \n' + sdncAdapterRequest)

			// Get the Basic Auth credentials for the SDNCAdapter (yes... we ARE using the PO adapters credentials)

			String basicAuthValue = UrnPropertiesReader.getVariable("mso.adapters.po.auth",execution)

			if (basicAuthValue == null || basicAuthValue.isEmpty()) {
				msoLogger.debug(getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined")
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			} else {
				try {
					def encodedString = utils.getBasicAuth(basicAuthValue, UrnPropertiesReader.getVariable("mso.msoKey", execution))
					execution.setVariable(prefix + 'basicAuthHeaderValue', encodedString)
				} catch (IOException ex) {
					msoLogger.debug(getProcessKey(execution) + ": Unable to encode BasicAuth credentials for SDNCAdapter")
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, getProcessKey(execution) + ": Unable to encode BasicAuth credentials for SDNCAdapter", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				}
			}

			// Set the timeout value, e.g. PT5M. It may be specified in the request as the
			// bpTimeout value.  If it's not in the request, use the URN mapping value.

			String timeout = jsonUtil.getJsonValue(request, requestType + ".bpTimeout")

			// in addition to null/empty, also need to verify that the timer value is a valid duration "P[n]T[n]H|M|S"
			String timerRegex = "PT[0-9]+[HMS]";
			if (timeout == null || timeout.isEmpty() || !timeout.matches(timerRegex)) {
				msoLogger.debug(getProcessKey(execution) + ': preProcessRequest(): null/empty/invalid bpTimeout value. Using "mso.adapters.sdnc.timeout"')
				timeout = UrnPropertiesReader.getVariable("mso.adapters.sdnc.timeout", execution)
			}

			// the timeout could still be null at this point if the config parm is missing/undefined
			// forced to log (so OPs can fix the config) and temporarily use a hard coded value of 10 seconds
			if (timeout == null) {
				msoLogger.warnSimple('preProcessRequest()', 'property "mso.adapters.sdnc.timeout" is missing/undefined. Using "PT10S"')
				timeout = "PT10S"
			}

			execution.setVariable(prefix + 'timeout', timeout)
			msoLogger.debug(getProcessKey(execution) + ': ' + prefix + 'timeout = ' + timeout)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			msoLogger.debug(msg)
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
		}
	}

	/**
	 * Processes a callback. Check for possible interim notification.
	 */
	public void processCallback(DelegateExecution execution){
		def method = getClass().getSimpleName() + '.processCallback(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)

		String prefix = execution.getVariable('prefix')
		String callback = execution.getVariable('SDNCAResponse_MESSAGE')
		msoLogger.debug("Incoming SDNC Rest Callback is: " + callback)

		try {
			msoLogger.debug(getProcessKey(execution) + ": received callback:\n" + callback)

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
				sdncAdapterBuildWorkflowException(execution, callback)
			}
			
			// Check for possible interim notification
			execution.setVariable(prefix + "interimNotification", null)
			execution.setVariable(prefix + "doInterimNotification", false)
			if ('N'.equals(ackFinalIndicator)) {
				def interimNotification = execution.getVariable(prefix + "InterimNotification" + callbackNumber)
				if (interimNotification != null) {
					execution.setVariable(prefix + "interimNotification", interimNotification)
					execution.setVariable(prefix + "doInterimNotification", true)
				}
			}
			
		} catch (Exception e) {
			callback = callback == null || String.valueOf(callback).isEmpty() ? "NONE" : callback
			String msg = "Received error from SDNCAdapter: " + callback
			msoLogger.debug(getProcessKey(execution) + ': ' + msg)
			exceptionUtil.buildWorkflowException(execution, 5300, msg)
		}
	}
	
	/**
	 * Prepare to send an interim notification by extracting the variable/value definitions
	 * in the interimNotification JSON object and placing them in the execution.  These
	 * variable/value definitions will be passed to the notification service.
	 */
	public void prepareInterimNotification(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepareInterimNotification(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
		
		String prefix = execution.getVariable('prefix')
		msoLogger.debug("Preparing Interim Notification")

		try {
			def interimNotification = execution.getVariable(prefix + "interimNotification")
			msoLogger.debug("Preparing Interim Notification:\n" + JsonUtils.prettyJson(interimNotification))
			
			for (int i = 0; ; i++) {
				def variable = JsonUtils.getJsonParamValue(interimNotification, 'variableList', 'variable', i)
				
				if (variable == null) {
					break
				}
				
				def String variableName = JsonUtils.getJsonValue(variable, "name")
				if ((variableName != null) && !variableName.isEmpty()) {
					def variableValue = JsonUtils.getJsonValue(variable, "value")
					execution.setVariable(variableName, variableValue)
					msoLogger.debug("Setting "+ variableName + "=" + variableValue)
				}
			}
			
		} catch (Exception e) {
			String msg = "Error preparing interim notification"
			msoLogger.debug(getProcessKey(execution) + ': ' + msg)
			exceptionUtil.buildWorkflowException(execution, 5300, msg)
		}
	}
}
