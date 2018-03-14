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

/**
 * This version of SDNCAdapterRest allows for interim notifications to be sent for
 * any non-final response received from SDNC.
 */
class SDNCAdapterRestV2 extends SDNCAdapterRestV1 {

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
			utils.logAudit('SDNCAdapterRestV2, request: ' + request)

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
	 * Processes a callback. Check for possible interim notification.
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
			logDebug(getProcessKey(execution) + ': ' + msg, isDebugLogEnabled)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		String prefix = execution.getVariable('prefix')
		utils.logAudit("Preparing Interim Notification")

		try {
			def interimNotification = execution.getVariable(prefix + "interimNotification")
			logDebug("Preparing Interim Notification:\n" + JsonUtils.prettyJson(interimNotification), isDebugLogEnabled)
			
			for (int i = 0; ; i++) {
				def variable = JsonUtils.getJsonParamValue(interimNotification, 'variableList', 'variable', i)
				
				if (variable == null) {
					break
				}
				
				def String variableName = JsonUtils.getJsonValue(variable, "name")
				if ((variableName != null) && !variableName.isEmpty()) {
					def variableValue = JsonUtils.getJsonValue(variable, "value")
					execution.setVariable(variableName, variableValue)
					logDebug("Setting "+ variableName + "=" + variableValue, isDebugLogEnabled)
				}
			}
			
		} catch (Exception e) {
			String msg = "Error preparing interim notification"
			logDebug(getProcessKey(execution) + ': ' + msg, isDebugLogEnabled)
			exceptionUtil.buildWorkflowException(execution, 5300, msg)
		}
	}
}
