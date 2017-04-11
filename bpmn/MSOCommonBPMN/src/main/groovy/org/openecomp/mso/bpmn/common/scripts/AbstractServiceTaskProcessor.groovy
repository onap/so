/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.bpmn.common.scripts;

import groovy.json.JsonSlurper

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowCallbackResponse
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowContextHolder
import org.openecomp.mso.bpmn.core.WorkflowException
import org.springframework.web.util.UriUtils

public abstract class AbstractServiceTaskProcessor implements ServiceTaskProcessor {
	public MsoUtils utils = new MsoUtils()


	/**
	 * Logs a message at the ERROR level.
	 * @param message the message
	 */
	public void logError(String message) {
		log('ERROR', message, null, "true")
	}

	/**
	 * Logs a message at the ERROR level.
	 * @param message the message
	 * @param cause the cause (stracktrace will be included in the output)
	 */
	public void logError(String message, Throwable cause) {
		log('ERROR', message, cause, "true")
	}

	/**
	 * Logs a message at the WARN level.
	 * @param message the message
	 */
	public void logWarn(String message) {
		log('WARN', message, null, "true")
	}

	/**
	 * Logs a message at the WARN level.
	 * @param message the message
	 * @param cause the cause (stracktrace will be included in the output)
	 */
	public void logWarn(String message, Throwable cause) {
		log('WARN', message, cause, "true")
	}

	/**
	 * Logs a message at the INFO level.
	 * @param message the message
	 */
	public void logInfo(String message) {
		log('INFO', message, null, "true")
	}

	/**
	 * Logs a message at the INFO level.
	 * @param message the message
	 * @param cause the cause (stracktrace will be included in the output)
	 */
	public void logInfo(String message, Throwable cause) {
		log('INFO', message, cause, "true")
	}

	/**
	 * Logs a message at the DEBUG level.
	 * @param message the message
	 * @param isDebugLogEnabled a flag indicating if DEBUG level is enabled
	 */
	public void logDebug(String message, String isDebugLogEnabled) {
		log('DEBUG', message, null, isDebugLogEnabled)
	}

	/**
	 * Logs a message at the DEBUG level.
	 * @param message the message
	 * @param cause the cause (stracktrace will be included in the output)
	 * @param isDebugLogEnabled a flag indicating if DEBUG level is enabled
	 */
	public void logDebug(String message, Throwable cause, String isDebugLogEnabled) {
		log('DEBUG', message, cause, isDebugLogEnabled)
	}

	/**
	 * Logs a message at the specified level.
	 * @param level the level (DEBUG, INFO, WARN, ERROR)
	 * @param message the message
	 * @param isLevelEnabled a flag indicating if the level is enabled
	 *        (used only at the DEBUG level)
	 */
	public void log(String level, String message, String isLevelEnabled) {
		log(level, message,  null, isLevelEnabled)
	}

	/**
	 * Logs a message at the specified level.
	 * @param level the level (DEBUG, INFO, WARN, ERROR)
	 * @param message the message
	 * @param cause the cause (stracktrace will be included in the output)
	 * @param isLevelEnabled a flag indicating if the level is enabled
	 *        (used only at the DEBUG level)
	 */
	public void log(String level, String message, Throwable cause, String isLevelEnabled) {
		if (cause == null) {
			utils.log(level, message, isLevelEnabled);
		} else {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			printWriter.println(message);
			cause.printStackTrace(printWriter);
			utils.log(level, stringWriter.toString(), isLevelEnabled);
			printWriter.close();
		}
	}

	/**
	 * Logs a WorkflowException at the ERROR level with the specified message.
	 * @param execution the execution
	 */
	public void logWorkflowException(Execution execution, String message) {
		def workflowException = execution.getVariable("WorkflowException")

		if (workflowException == null) {
			logError(message);
		} else {
			logError(message + ": " + workflowException)
		}
	}

	/**
	 * Saves the WorkflowException in the execution to the specified variable,
	 * clearing the WorkflowException variable so the workflow can continue
	 * processing (perhaps catching another WorkflowException).
	 * @param execution the execution
	 * @return the name of the destination variable
	 */
	public saveWorkflowException(Execution execution, String variable) {
		if (variable == null) {
			throw new NullPointerException();
		}

		execution.setVariable(variable, execution.getVariable("WorkflowException"))
		execution.setVariable("WorkflowException", null)
	}

	/**
	 * Builds a success response from the specified message content and numeric
	 * response code.  The response code may be an integer or a string representation
	 * of an integer.  The response is stored in the execution where it may be
	 * picked up by the Workflow service.
	 * <p>
	 * IMPORTANT: the activity that executes this method should have an
	 * asynchronous continuation after it to ensure the execution variables
	 * are persisted to the database.
	 * @param execution the execution
	 * @param content the message content
	 * @param responseCode the message response code
	 */
	@Deprecated
	public void buildResponse(Execution execution, String content, Object responseCode) {
		buildResponse(execution, content, responseCode, true)
	}

	/**
	 * Builds a standard error response containing the specified error message and
	 * numeric response code.  The response code may be an integer or a string
	 * representation of an integer.  The response is stored in the execution where
	 * it may be picked up by the Workflow service.
	 * <p>
	 * IMPORTANT: the activity that executes this method should have an
	 * asynchronous continuation after it to ensure the execution variables
	 * are persisted to the database.
	 * @param execution the execution
	 * @param content the message content
	 * @param errorCode the message response code
	 */
	@Deprecated
	public void buildErrorResponse(Execution execution, String errorMessage, Object errorCode) {

		def encErrorMessage = errorMessage.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

		def content = """
			<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
			<aetgt:ErrorMessage>${encErrorMessage}</aetgt:ErrorMessage>
			<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
			</aetgt:WorkflowException>
		  """

		buildResponse(execution, content, errorCode, false)
	}

	// BEGIN LEGACY SUPPORT.  TODO: REMOVE THIS CODE
	/**
	 * Builds a standard error response containing the specified error message
	 * and a numeric response code.  The response code is obtained from the
	 * prefix+"ResponseCode" execution variable. The response is stored in the
	 * execution where it may be picked up by the Workflow service.
	 * <p>
	 * IMPORTANT: the activity that executes this method should have an
	 * asynchronous continuation after it to ensure the execution variables
	 * are persisted to the database.
	 * <p>
	 * This method is deprecated. Methods that accept a response code should
	 * be used instead.
	 * @param execution the execution
	 * @param errorMessage the error message for the error response
	 */
	@Deprecated
	public void buildErrorResponse(Execution execution, String errorMessage) {
		buildErrorResponse(execution, errorMessage, null)
	}
	// END LEGACY SUPPORT.  TODO: REMOVE THIS CODE

	/**
	 * Builds a response from the specified message content and numeric response
	 * code.  The response code may be an integer or a string representation of
	 * an integer.  The response is stored in the execution where it may be
	 * picked up by the Workflow service.
	 * <p>
	 * IMPORTANT: the activity that executes this method should have an
	 * asynchronous continuation after it to ensure the execution variables
	 * are persisted to the database.
	 * @param execution the execution
	 * @param content the message content
	 * @param responseCode the message response code
	 * @param isSuccess true if this is a success response
	 */
	@Deprecated
	protected void buildResponse(Execution execution, String content, Object responseCode,
			boolean isSuccess) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')

		String processKey = getProcessKey(execution);
		logDebug("Building " + processKey + " response", isDebugLogEnabled)

		Map<String, Object> responseMap = new HashMap<String, Object>()

		if (isSuccess) {
			responseMap.put("Status", "Success")
		} else {
			responseMap.put("Status", "Fail")
		}

		// BEGIN LEGACY SUPPORT.  TODO: REMOVE THIS CODE
		def prefix = execution.getVariable("prefix")

		if (responseCode == null) {
			responseCode = execution.getVariable(prefix+"ResponseCode")
		} else {
			execution.setVariable(prefix+"ResponseCode", String.valueOf(responseCode))
		}
		// END LEGACY SUPPORT.  TODO: REMOVE THIS CODE

		responseMap.put("ResponseCode", String.valueOf(responseCode))

		if (isSuccess) {
			responseMap.put("Status", "Success")
			// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
			execution.setVariable("WorkflowResponse", content)
			// BEGIN LEGACY SUPPORT.  TODO: REMOVE THIS CODE
			execution.setVariable(processKey+"Response", content)
			execution.setVariable(prefix+"ErrorResponse", null)
			// END LEGACY SUPPORT.  TODO: REMOVE THIS CODE
		} else {
			responseMap.put("Status", "Fail")
			// BEGIN LEGACY SUPPORT.  TODO: REMOVE THIS CODE
			execution.setVariable(prefix+"ErrorResponse", content)
			execution.setVariable(prefix+"Response", null)
			// END LEGACY SUPPORT.  TODO: REMOVE THIS CODE
		}

		responseMap.put("Response", content)

		logDebug(processKey
			+ " ResponseCode=" + responseMap.get("ResponseCode")
			+ " Status=" + responseMap.get("Status")
			+ " Response=\n" + responseMap.get("Response"),
			isDebugLogEnabled)

		execution.setVariable(processKey + "ResponseMap", responseMap)
	}

	/**
	 * Builds an error response (if one has not already been built) and throws
	 * a BpmnError of type "MSOWorkflowException" that can be caught as a
	 * boundary event.
	 * @param execution the execution
	 * @param errorMessage the error message for the error response
	 * @param responseCode the message response code
	 */
	@Deprecated
	public void workflowException(Execution execution, String errorMessage, Object responseCode) {
		String processKey = getProcessKey(execution);

		buildErrorResponse(execution, errorMessage, responseCode)
		throw new BpmnError("MSOWorkflowException")
	}

	/**
	 * Puts a WorkflowException into the execution
	 * @param execution the execution
	 * @param errorCode the error code (normally a 4-digit number)
	 * @param errorMessage the error message
	 */
	@Deprecated
	public void newWorkflowException(Execution execution, int errorCode, String errorMessage) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String processKey = getProcessKey(execution);
		logDebug("Building a " + processKey + " WorkflowException", isDebugLogEnabled)

		if (errorCode < 1000) {
			throw new IllegalArgumentException("ErrorCode must be a number greater than 1000");
		}

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
		execution.setVariable("WorkflowException", exception);
	}

	/**
	 * Puts a WorkflowException into the execution and throws an MSOWorkflowException event.
	 * @param execution the execution
	 * @param errorCode the error code (normally a 4-digit number)
	 * @param errorMessage the error message
	 */
	// TODO: rename this method to be throwWorkflowException
	@Deprecated
	public void createWorkflowException(Execution execution, int errorCode, String errorMessage) {
		newWorkflowException(execution, errorCode, errorMessage)
		throw new BpmnError("MSOWorkflowException", "errorCode:" + errorCode + ", errorMessage:" + errorMessage)
	}

	/**
	 * Puts a WorkflowException into the execution and throws an MSOWorkflowException event.
	 * @param execution the execution
	 * @param errorCode the error code (normally a 4-digit number)
	 * @param errorMessage the error message
	 */
	@Deprecated
	public void commonWorkflowException(Execution execution, int errorCode, String errorMessage) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String processKey = getProcessKey(execution);
		logDebug("Building a " + processKey + " WorkflowException", isDebugLogEnabled)
		logError(errorMessage)
		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
		execution.setVariable("WorkflowException", exception);
		throw new BpmnError("MSOWorkflowException","errorCode:" + errorCode + ",errorMessage:" + errorMessage)
	}

	/**
	 * Puts a WorkflowException into the execution and throws an MSOWorkflowException event.
	 * @param execution the execution
	 * @param errorCode the error code (normally a 4-digit number)
	 * @param errorMessage the error message
	 */
	@Deprecated
	public void commonWorkflowException(Execution execution, String errorCode, String errorMessage) {
		int intRespCode
		try{
			intRespCode = Integer.parseInt(errorCode)
		}catch (Exception e){
			intRespCode = 400
		}
		commonWorkflowException(execution, intRespCode, errorMessage)
	}



	/**
	 * Validates that the request exists and that the mso-request-id variable is set.
	 * Additional required variables may be checked by specifying their names.
	 * NOTE: services requiring mso-service-instance-id must specify it explicitly!
	 * If a problem is found, buildAndThrowWorkflowException builds a WorkflowException
	 * and throws an MSOWorkflowException.  This method also sets up the log context for
	 * the workflow.
	 *
	 * @param execution the execution
	 * @return the validated request
	 */
	public String validateRequest(Execution execution, String... requiredVariables) {
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		def method = getClass().getSimpleName() + '.validateRequest(' +
			'execution=' + execution.getId() +
			', requredVariables=' + requiredVariables +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String processKey = getProcessKey(execution)
		def prefix = execution.getVariable("prefix")

		if (prefix == null) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " prefix is null")
		}

		try {
			def request = execution.getVariable(prefix + 'Request')

			if (request == null) {
				request = execution.getVariable(processKey + 'Request')

				if (request == null) {
					request = execution.getVariable('bpmnRequest')
				}

				setVariable(execution, processKey + 'Request', null)
				setVariable(execution, 'bpmnRequest', null)
				setVariable(execution, prefix + 'Request', request)
			}

			if (request == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request is null")
			}

			// All requests must have a request ID.
			// Some requests (e.g. SDN-MOBILITY) do not have a service instance ID.

			String requestId = null
			String serviceInstanceId = null

			List<String> allRequiredVariables = new ArrayList<String>()
			allRequiredVariables.add("mso-request-id")

			if (requiredVariables != null) {
				for (String variable : requiredVariables) {
					if (!allRequiredVariables.contains(variable)) {
						allRequiredVariables.add(variable)
					}
				}
			}

			for (String variable : allRequiredVariables) {
				def value = execution.getVariable(variable)
				if (value == null || ((value instanceof CharSequence) && value.length() == 0)) {
					exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey +
						" request was received with no '" + variable + "' variable")
				}

				if ("mso-request-id".equals(variable)) {
					requestId = (String) value
				} else if ("mso-service-instance-id".equals(variable)) {
					serviceInstanceId = (String) value
				}
			}

			if (serviceInstanceId == null) {
				serviceInstanceId = (String) execution.getVariable("mso-service-instance-id")
			}

			utils.logContext(requestId, serviceInstanceId)
			logDebug('Incoming message: ' + System.lineSeparator() + request, isDebugLogEnabled)
			logDebug('Exited ' + method, isDebugLogEnabled)
			return request
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Invalid Message")
		}
	}

	/**
	 * gets vars stored in a JSON object in prefix+Request and returns as a LazyMap
	 * setting log context here too
	 * @param execution the execution
	 * @return the inputVars
	 */
	public Map validateJSONReq(Execution execution) {
		def method = getClass().getSimpleName() + '.validateJSONReq(' +
				'execution=' + execution.getId() +
				')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String processKey = getProcessKey(execution);
		def prefix = execution.getVariable("prefix")

		def requestId =getVariable(execution, "mso-request-id")
		def serviceInstanceId = getVariable(execution, "mso-service-instance-id")
		if(requestId!=null && serviceInstanceId!=null){
			utils.logContext(requestId, serviceInstanceId)
		}


		def request = getVariable(execution, prefix + 'Request')

		if (request == null) {
			request = getVariable(execution, processKey + 'Request')

			if (request == null) {
				request = getVariable(execution, 'bpmnRequest')
			}
			execution.setVariable(prefix + 'Request', request)
		}

		def jsonSlurper = new JsonSlurper()
		def parsed = jsonSlurper.parseText(request)


		logDebug('Incoming message: ' + System.lineSeparator() + request, isDebugLogEnabled)
		logDebug('Exited ' + method, isDebugLogEnabled)
		return parsed

	}




	/**
	 * Sends a response to the workflow service that invoked the process.  This method
	 * may only be used by top-level processes that were directly invoked by the
	 * asynchronous workflow service.
	 * @param execution the execution
	 * @param responseCode the response code
	 * @param content the message content
	 * @throws IllegalArgumentException if the response code is invalid
	 *         by HTTP standards
	 * @throws UnsupportedOperationException if not invoked by an asynchronous,
	 *         top-level process
	 * @throws IllegalStateException if a response has already been sent
	 */
	protected void sendWorkflowResponse(Execution execution, Object responseCode, String response) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		try {
			String processKey = getProcessKey(execution);

			// isAsyncProcess is injected by the workflow service that started the flow
			if (!String.valueOf(execution.getVariable("isAsyncProcess")).equals("true")) {
				throw new UnsupportedOperationException(processKey + ": " +
					"sendWorkflowResponse is valid only in asynchronous workflows");
			}

			if (String.valueOf(execution.getVariable(processKey + "WorkflowResponseSent")).equals("true")) {
					logDebug("Sync response has already been sent for " + processKey, isDebugLogEnabled)
			}else{

				logDebug("Building " + processKey + " response ", isDebugLogEnabled)

				int intResponseCode;

				try {
					intResponseCode = Integer.parseInt(String.valueOf(responseCode));

					if (intResponseCode < 100 || intResponseCode > 599) {
						throw new NumberFormatException(String.valueOf(responseCode));
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Process " + processKey
						+ " provided an invalid HTTP response code: " + responseCode);
				}

				// Only 2XX responses are considered "Success"
				String status = (intResponseCode >= 200 && intResponseCode <= 299) ?
					"Success" : "Fail";

				// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
				execution.setVariable(processKey + "ResponseCode", String.valueOf(intResponseCode))
				execution.setVariable(processKey + "Response", response);
				execution.setVariable(processKey + "Status", status);
				execution.setVariable("WorkflowResponse", response)

				logDebug("Sending response for " + processKey
					+ " ResponseCode=" + intResponseCode
					+ " Status=" + status
					+ " Response=\n" + response,
					isDebugLogEnabled)

				// TODO: ensure that this flow was invoked asynchronously?

				WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse()
				callbackResponse.setStatusCode(intResponseCode)
				callbackResponse.setMessage(status)
				callbackResponse.setResponse(response)

				// TODO: send this data with HTTP POST

				WorkflowContextHolder.getInstance().processCallback(
					processKey,
					execution.getProcessInstanceId(),
					execution.getVariable("mso-request-id"),
					callbackResponse)

				execution.setVariable(processKey + "WorkflowResponseSent", "true");
			}

		} catch (Exception ex) {
			logError("Unable to send workflow response to client ....", ex)
		}
	}

	/**
	 * Returns true if a workflow response has already been sent.
	 * @param execution the execution
	 */
	protected boolean isWorkflowResponseSent(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String processKey = getProcessKey(execution);
		return String.valueOf(execution.getVariable(processKey + "WorkflowResponseSent")).equals("true");
	}

	/**
	 * Returns the process definition key (i.e. the process name) of the
	 * current process.
	 * 
	 * @param execution the execution
	 */
	public String getProcessKey(Execution execution) {
		def testKey = execution.getVariable("testProcessKey")
		if(testKey!=null){
			return testKey
		}
		return execution.getProcessEngineServices().getRepositoryService()
			.getProcessDefinition(execution.getProcessDefinitionId()).getKey()
	}

	/**
	 * Returns the process definition key (i.e. the process name) of the
	 * top-level process.
	 * @param execution the execution
	 */
	public String getMainProcessKey(Execution execution) {
		Execution exec = execution

		while (true) {
			Execution parent = exec.getSuperExecution()

			if (parent == null) {
				parent = exec.getParent()

				if (parent == null) {
					break
				}
			}

			exec = parent
		}

		return execution.getProcessEngineServices().getRepositoryService()
			.getProcessDefinition(exec.getProcessDefinitionId()).getKey()
	}

	/**
	 * Gets the node for the named element from the given xml. If the element
	 * does not exist in the xml or is empty, a WorkflowException is created
	 * (and as a result, a MSOWorkflowException event is thrown).
	 *
	 * @param execution The flow's execution.
	 * @param xml Xml to search.
	 * @param elementName Name of element to search for.
	 * @return The element node, if found in the xml.
	 */
	protected String getRequiredNodeXml(Execution execution, String xml, String elementName) {
		def element = utils.getNodeXml(xml, elementName, false)
		if (element.trim().isEmpty()) {
			def msg = 'Required element \'' + elementName + '\' is missing or empty'
			logError(msg)
			createWorkflowException(execution, 2000, msg)
		} else {
			return element
		}
	}

	/**
	 * Gets the value of the named element from the given xml. If the element
	 * does not exist in the xml or is empty, a WorkflowException is created
	 * (and as a result, a MSOWorkflowException event is thrown).
	 *
	 * @param execution The flow's execution.
	 * @param xml Xml to search.
	 * @param elementName Name of element to whose value to get.
	 * @return The value of the element, if found in the xml.
	 */
	protected String getRequiredNodeText(Execution execution, String xml, String elementName) {
		def elementText = utils.getNodeText1(xml, elementName)
		if (elementText == null) {
			def msg = 'Required element \'' + elementName + '\' is missing or empty'
			logError(msg)
			createWorkflowException(execution, 2000, msg)
		} else {
			return elementText
		}
	}

	/**
	 * Get the text for the specified element from the specified xml.  If
	 * the element does not exist, return an empty string.
	 *
	 * @param xml Xml from which to get the element's text.
	 * @param elementName Name of element whose text to get.
	 * @return the element's text or an empty string if the element does not
	 * exist in the given xml.
	 */
	protected String getNodeTextForce(String xml, String elementName) {
		def nodeText = utils.getNodeText1(xml, elementName)
		return (nodeText == null) ? '' : nodeText
	}

	/**
	 * Sends the empty, synchronous response back to the API Handler.
	 * @param execution the execution
	 */
	@Deprecated
	public void sendResponse(Execution execution) {
		def method = getClass().getSimpleName() + '.sendResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			buildResponse(execution, "", 200)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			workflowException(execution, 'Internal Error', 9999) // TODO: what message and error code?
		}
	}

	/**
	*Store the variable as typed with java serialization type
	*@param execution
	*@param name
	*@param value
	*/
	public void setVariable(Execution execution, String name, Object value) {
		VariableMap variables = Variables.createVariables()
		variables.putValueTyped('payload', Variables.objectValue(value)
		.serializationDataFormat(SerializationDataFormats.JAVA) // tells the engine to use java serialization for persisting the value
		.create())
		execution.setVariable(name,variables)
	}

	//TODO not sure how this will look in Cockpit

	/**
	 * Returns the variable map
	*@param execution
	*@param name
	*@return
	**/
	public String getVariable(Execution execution, String name) {
		def myObj = execution.getVariable(name)
		if(myObj instanceof VariableMap){
			VariableMap serializedObjectMap = (VariableMap) myObj
			ObjectValueImpl payloadObj = serializedObjectMap.getValueTyped('payload')
			return payloadObj.getValue()
		}else{
			return myObj
		}
	}


	/**
	 * Returns true if a value equals one of the provided set. Equality is
	 * determined by using the equals method if the value object and the
	 * object in the provided set have the same class. Otherwise, the objects
	 * are converted to strings and then compared.  Nulls are permitted for
	 * the value as well as in the provided set
	 * Example:
	 * <pre>
	 *     def statusCode = getStatusCode()
	 *     isOneOf(statusCode, 200, 201, 204)
	 * </pre>
	 * @param value the value to test
	 * @param these a set of permissable values
	 * @return true if the value is in the provided set
	 */
	public boolean isOneOf(Object value, Object... these) {
		for (Object thisOne : these) {
			if (thisOne == null) {
				if (value == null) {
					return true
				}
			} else {
				if (value != null) {
					if (value.getClass() == thisOne.getClass()) {
						if (value.equals(thisOne)) {
							return true
						}
					} else {
						if (String.valueOf(value).equals(String.valueOf(thisOne))) {
							return true
						}
					}
				}
			}
		}

		return false
	}

	public void setSuccessIndicator(Execution execution, boolean isSuccess) {
		String prefix = execution.getVariable('prefix')
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')

		logDebug('Entered SetSuccessIndicator Method', isDebugLogEnabled)
		execution.setVariable(prefix+'SuccessIndicator', isSuccess)
		logDebug('Outgoing SuccessIndicator is: ' + execution.getVariable(prefix+'SuccessIndicator') + '', isDebugLogEnabled)
	}


	public void sendSyncError(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String requestId = execution.getVariable("mso-request-id")
		logDebug('sendSyncError, requestId: ' + requestId, isDebugEnabled)
		WorkflowException workflowExceptionObj = execution.getVariable("WorkflowException")
		if (workflowExceptionObj != null) {
			String errorMessage = workflowExceptionObj.getErrorMessage()
			def errorCode = workflowExceptionObj.getErrorCode()
			logDebug('sendSyncError, requestId: '  + requestId + ' | errorMessage: ' + errorMessage + ' | errorCode: ' + errorCode, isDebugEnabled)
			sendWorkflowResponse(execution, errorCode, errorMessage)
		}
	}
	
	/**
	 * Create a WorkflowException - uses ExceptionUtil to build a WorkflowException
	 * @param execution
	 * @param errorCode
	 * @param errorMessage
	 * @param isDebugEnabled
	 */
	public void buildWorkflowException(Execution execution, int errorCode, String errorMessage, boolean isDebugEnabled) {
		(new ExceptionUtil()).buildWorkflowException(execution, errorCode, errorMessage)
	}
	
	/**
	 * Executes a named groovy script method in the current object
	 */
	public void executeMethod(String methodName, Object... args) {
		
		if (args != null && args.size() > 0) {
			
			// First argument of method to call is always the execution object
			Execution execution = (Execution) args[0]

			def classAndMethod = getClass().getSimpleName() + '.' + methodName + '(execution=' + execution.getId() + ')'
			def isDebugEnabled =  execution.getVariable('isDebugLogEnabled')

			logDebug('Entered ' + classAndMethod, isDebugEnabled)
			logDebug('Received parameters: ' + args, isDebugEnabled)

			try{
				def methodToCall = this.metaClass.getMetaMethod(methodName, args)
				logDebug('Method to call: ' + methodToCall, isDebugEnabled)
				methodToCall?.invoke(this, args)
			}
			catch(BpmnError bpmnError) {
				logDebug('Rethrowing BpmnError ' + bpmnError.getMessage(), isDebugEnabled)
				throw bpmnError
			}
			catch(Exception e) {
				e.printStackTrace()
				logDebug('Unexpected error encountered - ' + e.getMessage(), isDebugEnabled)
				(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
			}
			finally {
				logDebug('Exited ' + classAndMethod, isDebugEnabled)
			}
		}
	}
	
	/**
	 *This method determines and adds the appropriate ending to come
	 *after a number (-st, -nd, -rd, or -th)
	 *
	 *@param int n
	 *
	 *@return String ending - number with suffix
	 */

	public static String labelMaker(Object n) {
		Integer num
		if(n instanceof String){
			num = Integer.parseInt(n)
		}else{
			num = n
		}

		String ending = ""; //the end to be added to the number
		if(num != null){
			if ((num % 10 == 1) && (num != 11)) {
				ending = num + "st";
			} else if ((num % 10 == 2) && (num != 12)) {
				ending = num + "nd";
			} else if ((num % 10 == 3) && (num != 13)) {
				ending = num + "rd";
			} else {
				ending = num + "th";
			}
		}
		return ending
	}

	/**
	 *
	 *This method gets and decodes SDNC Response's "RequestData".
	 *
	 *@param response - the sdnc response
	 *
	 *@return data - the response "RequestData" decoded
	 *
	 */
	public String getRequestDataDecoded(String response){
		String data = utils.getNodeText1(response, "RequestData")
		if(data != null){
			data = data.replaceAll("&lt;", "<")
			data = data.replaceAll("&gt;", ">")
		}

		return data
	}


	/**
	 * Constructs a workflow message callback URL for the specified message type and correlator.
	 * @param messageType the message type (e.g. SDNCAResponse or VNFAResponse)
	 * @param correlator the correlator value (e.g. a request ID)
	 */
	public String createCallbackURL(Execution execution, String messageType, String correlator) {
		String endpoint = (String) execution.getVariable('URN_mso_workflow_message_endpoint')

		if (endpoint == null || endpoint.isEmpty()) {
			ExceptionUtil exceptionUtil = new ExceptionUtil()
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000,
				'mso:workflow:message:endpoint URN mapping is not set')
		}

		while (endpoint.endsWith('/')) {
			endpoint = endpoint.substring(0, endpoint.length()-1)
		}

		return endpoint +
			'/' + UriUtils.encodePathSegment(messageType, 'UTF-8') +
			'/' + UriUtils.encodePathSegment(correlator, 'UTF-8')
	}

}
