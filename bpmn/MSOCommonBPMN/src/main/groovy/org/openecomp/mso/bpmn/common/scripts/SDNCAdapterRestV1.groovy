package org.openecomp.mso.bpmn.common.scripts

import java.text.SimpleDateFormat
import java.net.URLEncoder

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

import groovy.json.*

import org.json.JSONObject

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig


class SDNCAdapterRestV1 extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	/**
	 * Processes the incoming request.
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		def prefix="SDNCREST_"
		execution.setVariable("prefix", prefix)
		setSuccessIndicator(execution, false)

		try {
			// Determine the request type and log the request

			String request = validateRequest(execution, "mso-request-id")
			String requestType = jsonUtil.getJsonRootProperty(request)
			execution.setVariable(prefix + 'requestType', requestType)
			logDebug(getProcessKey(execution) + ': ' + prefix + 'requestType = ' + requestType, isDebugLogEnabled)
			utils.logAudit('SDNCAdapterRestV1, request: ' + request)

			// Determine the SDNCAdapter endpoint

			String sdncAdapterEndpoint = execution.getVariable("URN_mso_adapters_sdnc_rest_endpoint")

			if (sdncAdapterEndpoint == null || sdncAdapterEndpoint.isEmpty()) {
				String msg = getProcessKey(execution) + ': mso:adapters:sdnc:rest:endpoint URN mapping is not defined'
				logDebug(msg, isDebugLogEnabled)
				logError(msg)
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
					logDebug(msg, isDebugLogEnabled)
					logError(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				execution.setVariable('SDNCAResponse_CORRELATOR', sdncRequestId)
				logDebug(getProcessKey(execution) + ': SDNCAResponse_CORRELATOR = ' + sdncRequestId, isDebugLogEnabled)

				// Get the bpNotificationUrl from the request (just to make sure it's there)

				String bpNotificationUrl = jsonUtil.getJsonValue(request, requestType + ".bpNotificationUrl")

				if (bpNotificationUrl == null || bpNotificationUrl.isEmpty()) {
					String msg = getProcessKey(execution) + ': no bpNotificationUrl in ' + requestType
					logDebug(msg, isDebugLogEnabled)
					logError(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}

				sdncAdapterMethod = 'POST'
				sdncAdapterUrl = sdncAdapterEndpoint + '/services'

			} else {
				String msg = getProcessKey(execution) + ': Unsupported request type: ' + requestType
				logDebug(msg, isDebugLogEnabled)
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable(prefix + 'sdncAdapterMethod', sdncAdapterMethod)
			logDebug(getProcessKey(execution) + ': ' + prefix + 'sdncAdapterMethod = ' + sdncAdapterMethod, isDebugLogEnabled)
			execution.setVariable(prefix + 'sdncAdapterUrl', sdncAdapterUrl)
			logDebug(getProcessKey(execution) + ': ' + prefix + 'sdncAdapterUrl = ' + sdncAdapterUrl, isDebugLogEnabled)
			execution.setVariable(prefix + 'sdncAdapterRequest', sdncAdapterRequest)
			logDebug(getProcessKey(execution) + ': ' + prefix + 'sdncAdapterRequest = \n' + sdncAdapterRequest, isDebugLogEnabled)

			// Get the Basic Auth credentials for the SDNCAdapter (yes... we ARE using the PO adapters credentials)

			String basicAuthValue = execution.getVariable("URN_mso_adapters_po_auth")

			if (basicAuthValue == null || basicAuthValue.isEmpty()) {
				logDebug(getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined", isDebugLogEnabled)
				logError(getProcessKey(execution) + ": mso:adapters:po:auth URN mapping is not defined")
			} else {
				logDebug(getProcessKey(execution) + ": Obtained BasicAuth credentials for SDNCAdapter:" +
					basicAuthValue, isDebugLogEnabled)
				try {
					def encodedString = utils.getBasicAuth(basicAuthValue, execution.getVariable("URN_mso_msoKey"))
					execution.setVariable(prefix + 'basicAuthHeaderValue', encodedString)
				} catch (IOException ex) {
					logDebug(getProcessKey(execution) + ": Unable to encode BasicAuth credentials for SDNCAdapter", isDebugLogEnabled)
					logError(getProcessKey(execution) + ": Unable to encode BasicAuth credentials for SDNCAdapter")
				}
			}

			// Set the timeout value, e.g. PT5M. It may be specified in the request as the
			// bpTimeout value.  If it's not in the request, use the URN mapping value.

			String timeout = jsonUtil.getJsonValue(request, requestType + ".bpTimeout")

			if (timeout == null || timeout.isEmpty()) {
				timeout = execution.getVariable("URN_mso_sdnc_timeout")
			}

			execution.setVariable(prefix + 'timeout', timeout)
			logDebug(getProcessKey(execution) + ': ' + prefix + 'timeout = ' + timeout, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			logDebug(msg, isDebugLogEnabled)
			logError(msg)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String prefix = execution.getVariable('prefix')

		try {
			String sdncAdapterMethod = execution.getVariable(prefix + 'sdncAdapterMethod')
			String sdncAdapterUrl = execution.getVariable(prefix + 'sdncAdapterUrl')
			String sdncAdapterRequest = execution.getVariable(prefix + 'sdncAdapterRequest')
			utils.logAudit("Outgoing SDNC Rest Request is: " + sdncAdapterRequest)

			RESTConfig config = new RESTConfig(sdncAdapterUrl)
			RESTClient client = new RESTClient(config).
				addHeader("Content-Type", "application/json").
				addAuthorizationHeader(execution.getVariable(prefix + "basicAuthHeaderValue"))

			APIResponse response

			if ("GET".equals(sdncAdapterMethod)) {
				response = client.httpGet()
			} else if ("PUT".equals(sdncAdapterMethod)) {
				response = client.httpPut(sdncAdapterRequest)
			} else if ("POST".equals(sdncAdapterMethod)) {
				response = client.httpPost(sdncAdapterRequest)
			} else if ("DELETE".equals(sdncAdapterMethod)) {
				response = client.httpDelete(sdncAdapterRequest)
			} else {
				String msg = 'Unsupported HTTP method "' + sdncAdapterMethod + '" in ' + method + ": " + e
				logDebug(msg, isDebugLogEnabled)
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}

			execution.setVariable(prefix + "sdncAdapterStatusCode", response.getStatusCode())
			execution.setVariable(prefix + "sdncAdapterResponse", response.getResponseBodyAsString())
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			logDebug(msg, isDebugLogEnabled)
			logError(msg)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String prefix = execution.getVariable('prefix')
		String callback = execution.getVariable('SDNCAResponse_MESSAGE')
		utils.logAudit("Incoming SDNC Rest Callback is: " + callback)

		try {
			logDebug(getProcessKey(execution) + ": received callback:\n" + callback, isDebugLogEnabled)

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
		} catch (Exception e) {
			callback = callback == null || String.valueOf(callback).isEmpty() ? "NONE" : callback
			String msg = "Received error from SDNCAdapter: " + callback
			logDebug(getProcessKey(execution) + ': ' + msg, isDebugLogEnabled)
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
			int mappedResponseCode = Integer.parseInt(exceptionUtil.MapSDNCResponseCodeToErrorCode(responseCode));
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

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
			logDebug(msg, isDebugLogEnabled)
			logError(msg)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String prefix = execution.getVariable('prefix')

		try {
			def timeoutValue = execution.getVariable("URN_mso_sdnc_timeout")

			if (execution.getVariable(prefix + 'callback1') != null) {
				// Waiting for subsequent notifications
			}
		} catch (Exception e) {
			String msg = 'Caught exception in ' + method + ": " + e
			logDebug(msg, isDebugLogEnabled)
			logError(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
		}
	}
}
