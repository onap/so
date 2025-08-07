/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common.scripts;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.HttpClientFactory
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.springframework.web.util.UriUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonSlurper

public abstract class AbstractServiceTaskProcessor implements ServiceTaskProcessor {
	private static final Logger logger = LoggerFactory.getLogger( MsoUtils.class);

	public MsoUtils utils = new MsoUtils()

	/**
	 * Logs a WorkflowException at the ERROR level with the specified message.
	 * @param execution the execution
	 */
	public void logWorkflowException(DelegateExecution execution, String message) {
		def workflowException = execution.getVariable("WorkflowException")

		if (workflowException == null) {
			logger.error(message);
		} else {
			logger.error('{}: {}', message, workflowException)
		}
	}
	
	/**
	 * Saves the WorkflowException in the execution to the specified variable,
	 * clearing the WorkflowException variable so the workflow can continue
	 * processing (perhaps catching another WorkflowException).
	 * @param execution the execution
	 * @return the name of the destination variable
	 */
	public saveWorkflowException(DelegateExecution execution, String variable) {
		if (variable == null) {
			throw new NullPointerException();
		}

		execution.setVariable(variable, execution.getVariable("WorkflowException"))
		execution.setVariable("WorkflowException", null)
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
	public String validateRequest(DelegateExecution execution, String... requiredVariables) {
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		def method = getClass().getSimpleName() + '.validateRequest(' +
			'execution=' + execution.getId() +
			', requredVariables=' + requiredVariables +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logger.debug('Entered ' + method)

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
				} else if ("mso-service-instance-id".equals(variable)) {
					serviceInstanceId = (String) value
				}
			}

			if (serviceInstanceId == null) {
				serviceInstanceId = (String) execution.getVariable("mso-service-instance-id")
			}

			logger.debug('Incoming message: ' + System.lineSeparator() + request)
			logger.debug('Exited ' + method)
			return request
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logger.error('Caught exception in {}: {}', method, e.getMessage(), e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Invalid Message")
		}
	}

	/**
	 * gets vars stored in a JSON object in prefix+Request and returns as a LazyMap
	 * setting log context here too
	 * @param execution the execution
	 * @return the inputVars
	 */
	public Map validateJSONReq(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateJSONReq(' +
				'execution=' + execution.getId() +
				')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logger.debug('Entered ' + method)

		String processKey = getProcessKey(execution);
		def prefix = execution.getVariable("prefix")

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


		logger.debug('Incoming message: ' + System.lineSeparator() + request)
		logger.debug('Exited ' + method)
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
	protected void sendWorkflowResponse(DelegateExecution execution, Object responseCode, String response) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		try {
			String processKey = getProcessKey(execution);

			// isAsyncProcess is injected by the workflow service that started the flow
			if (!String.valueOf(execution.getVariable("isAsyncProcess")).equals("true")) {
				throw new UnsupportedOperationException(processKey + ": " +
					"sendWorkflowResponse is valid only in asynchronous workflows");
			}

			if (String.valueOf(execution.getVariable(processKey + "WorkflowResponseSent")).equals("true")) {
					logger.debug("Sync response has already been sent for " + processKey)
			}else{

				logger.debug("Building " + processKey + " response ")

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

				logger.debug("Sending response for " + processKey
					+ " ResponseCode=" + intResponseCode
					+ " Status=" + status
					+ " Response=\n" + response)

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
			logger.error("Unable to send workflow response to client ....", ex)
		}
	}

	/**
	 * Returns true if a workflow response has already been sent.
	 * @param execution the execution
	 */
	protected boolean isWorkflowResponseSent(DelegateExecution execution) {
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
	public String getProcessKey(DelegateExecution execution) {
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
	public String getMainProcessKey(DelegateExecution execution) {
		DelegateExecution exec = execution

		while (true) {
			DelegateExecution parent = exec.getSuperExecution()

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
	protected String getRequiredNodeXml(DelegateExecution execution, String xml, String elementName) {
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		def element = utils.getNodeXml(xml, elementName, false)
		if (element.trim().isEmpty()) {
			def msg = 'Required element \'' + elementName + '\' is missing or empty'
			logger.error(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
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
	 * @return The non-empty value of the element, if found in the xml.
	 */
	protected String getRequiredNodeText(DelegateExecution execution, String xml, String elementName) {
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		def elementText = utils.getNodeText(xml, elementName)
		if ((elementText == null) || (elementText.isEmpty())) {
			def msg = 'Required element \'' + elementName + '\' is missing or empty'
			logger.error(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
		} else {
			return elementText
		}
	}

	/**
	 * Get the text for the specified element from the specified xml.  If
	 * the element does not exist, return the specified default value.
	 *
	 * @param xml Xml from which to get the element's text
	 * @param elementName Name of element whose text to get
	 * @param defaultValue the default value
	 * @return the element's text or the default value if the element does not
	 * exist in the given xml
	 */
	protected String getNodeText(String xml, String elementName, String defaultValue) {
		def nodeText = utils.getNodeText(xml, elementName)
		return (nodeText == null) ? defaultValue : nodeText
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
		return getNodeText(xml, elementName, '');
	}

	/**
	*Store the variable as typed with java serialization type
	*@param execution
	*@param name
	*@param value
	*/
	public void setVariable(DelegateExecution execution, String name, Object value) {
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
	public static String getVariable(DelegateExecution execution, String name) {
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

	/**
	 * Sets flows success indicator variable.
	 *
	 */
	public void setSuccessIndicator(DelegateExecution execution, boolean isSuccess) {
		String prefix = execution.getVariable('prefix')
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')

		logger.debug('Entered SetSuccessIndicator Method')
		execution.setVariable(prefix+'SuccessIndicator', isSuccess)
		logger.debug('Outgoing SuccessIndicator is: ' + execution.getVariable(prefix+'SuccessIndicator') + '')
	}

	/**
	 * Sends a Error Sync Response
	 *
	 */
	public void sendSyncError(DelegateExecution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String requestId = execution.getVariable("mso-request-id")
		logger.debug('sendSyncError, requestId: ' + requestId)
		WorkflowException workflowExceptionObj = execution.getVariable("WorkflowException")
		if (workflowExceptionObj != null) {
			String errorMessage = workflowExceptionObj.getErrorMessage()
			def errorCode = workflowExceptionObj.getErrorCode()
			logger.debug('sendSyncError, requestId: '  + requestId + ' | errorMessage: ' + errorMessage + ' | errorCode: ' + errorCode)
			sendWorkflowResponse(execution, errorCode, errorMessage)
		}
	}

	/**
	 * Executes a named groovy script method in the current object
	 */
	public void executeMethod(String methodName, Object... args) {

		if (args != null && args.size() > 0) {

			// First argument of method to call is always the execution object
			DelegateExecution execution = (DelegateExecution) args[0]

			def classAndMethod = getClass().getSimpleName() + '.' + methodName + '(execution=' + execution.getId() + ')'
			def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')

			logger.debug('Entered ' + classAndMethod)
			logger.debug('Received parameters: ' + args)

			try{
				def methodToCall = this.metaClass.getMetaMethod(methodName, args)
				logger.debug('Method to call: ' + methodToCall)
				methodToCall?.invoke(this, args)
			}
			catch(BpmnError bpmnError) {
				logger.debug('Rethrowing BpmnError ' + bpmnError.getMessage())
				throw bpmnError
			}
			catch(Exception e) {
				logger.debug('Unexpected error encountered - {}', e.getMessage(), e)
				(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
			}
			finally {
				logger.debug('Exited ' + classAndMethod)
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
	 * Constructs a workflow message callback URL for the specified message type and correlator.
	 * This type of callback URL is used when a workflow wants an MSO adapter (like the SDNC
	 * adapter) to call it back.  In other words, this is for callbacks internal to the MSO
	 * complex.  Use <code>createWorkflowMessageAdapterCallbackURL</code> if the callback
	 * will come from outside the MSO complex.
	 * @param messageType the message type (e.g. SDNCAResponse or VNFAResponse)
	 * @param correlator the correlator value (e.g. a request ID)
	 */
	public String createCallbackURL(DelegateExecution execution, String messageType, String correlator) {
		String endpoint = UrnPropertiesReader.getVariable("mso.workflow.message.endpoint", execution)

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

	/**
	 *
	 * Constructs a workflow message callback URL for the specified message type and correlator.
	 * This type of callback URL is used when a workflow wants a system outside the MSO complex
	 * to call it back through the Workflow Message Adapter.
	 * @param messageType the message type (e.g. SNIROResponse)
	 * @param correlator the correlator value (e.g. a request ID)
	 */
	public String createWorkflowMessageAdapterCallbackURL(DelegateExecution execution, String messageType, String correlator) {
		String endpoint = UrnPropertiesReader.getVariable("mso.adapters.workflow.message.endpoint", execution)

		if (endpoint == null || endpoint.isEmpty()) {
			ExceptionUtil exceptionUtil = new ExceptionUtil()
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000,
				'mso:adapters:workflow:message:endpoint URN mapping is not set')
		}

		while (endpoint.endsWith('/')) {
			endpoint = endpoint.substring(0, endpoint.length()-1)
		}

		return endpoint +
			'/' + UriUtils.encodePathSegment(messageType, 'UTF-8') +
			'/' + UriUtils.encodePathSegment(correlator, 'UTF-8')
	}

	public void setRollbackEnabled(DelegateExecution execution, isDebugLogEnabled) {

		// Rollback settings
		def prefix = execution.getVariable('prefix')
		def disableRollback = execution.getVariable("disableRollback")
		def defaultRollback = UrnPropertiesReader.getVariable("mso.rollback", execution).toBoolean()

		logger.debug('disableRollback: ' + disableRollback)
		logger.debug('defaultRollback: ' + defaultRollback)

		def rollbackEnabled

		if(disableRollback == null || disableRollback == '' ) {
			// get from default urn settings for mso_rollback
			disableRollback = !defaultRollback
			rollbackEnabled = defaultRollback
			logger.debug('disableRollback is null or empty!')
		}
		else {
			if(disableRollback == true) {
				rollbackEnabled = false
			}
			else if(disableRollback == false){
				rollbackEnabled = true
			}
			else {
				rollbackEnabled = defaultRollback
			}
		}

		execution.setVariable(prefix+"backoutOnFailure", rollbackEnabled)
		logger.debug('rollbackEnabled (aka backoutOnFailure): ' + rollbackEnabled)
	}

	public void setBasicDBAuthHeader(DelegateExecution execution, isDebugLogEnabled) {
		try {
			String basicAuthValueDB = UrnPropertiesReader.getVariable("mso.adapters.db.auth", execution)
			def encodedString = utils.getBasicAuth(basicAuthValueDB, UrnPropertiesReader.getVariable("mso.msoKey", execution))
			execution.setVariable("BasicAuthHeaderValueDB",encodedString)
		} catch (IOException ex) {
			String dataErrorMessage = " Unable to encode Catalog DB user/password string - " + ex.getMessage()
			logger.debug(dataErrorMessage)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
		}
	}
    public AAIResourcesClient getAAIClient(){
        return  new AAIResourcesClient();
    }

    HttpClientFactory getHttpClientFactory(){
        return new HttpClientFactory()
    }
}
