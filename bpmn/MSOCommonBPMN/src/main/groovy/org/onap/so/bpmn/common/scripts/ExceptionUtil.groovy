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

package org.onap.so.bpmn.common.scripts

import org.onap.so.logger.LoggingAnchor
import org.onap.so.logging.filter.base.ErrorCode

import static org.apache.commons.lang3.StringUtils.*

import com.google.common.xml.XmlEscapers

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.logger.MessageEnum
import org.onap.so.logging.filter.base.ONAPComponentsList
import org.slf4j.Logger
import org.slf4j.LoggerFactory



/**
 * @version 1.0
 */
class ExceptionUtil extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( ExceptionUtil.class);



	/**
	 * This error handling method maps an AAI Exception response to a
	 * WorkflowException Object.  It then sets the WorkflowException Object
	 * on the execution as "WorkflowException".
	 *
	 * This method formats the exception from AAI into the WorkflowException's
	 * errorMessage that CCD expects.
	 *
	 * @param execution the execution
	 * @param response the aai exception
	 */
	WorkflowException MapAAIExceptionToWorkflowException(String response, DelegateExecution execution)
	{
		def utils=new MsoUtils()
		def prefix=execution.getVariable("prefix")
		def errorMsg = execution.getVariable(prefix+"ErrorResponse")
		logger.trace("Begin MapAAIExceptionToWorkflowException ")
		String text = null
		def variables
		String errorCode = '5000'
		WorkflowException wfex
		logger.debug("response: " + response)
		try{
		try {
			//String msg = utils.getNodeXml(response, "Fault")
			variables = utils.getMultNodes(response, "variable")
			text = utils.getNodeText(response, "text")
		} catch (Exception ex) {
			//Ignore the exception - cases include non xml payload
			logger.debug("error mapping error, ignoring: " + ex)
		}

		if(text != null) {
			if(variables.size()>=4){
				text = text.replaceFirst("%1", variables[0])
				text = text.replaceFirst("%2", variables[1])
				text = text.replaceFirst("%3", variables[2])
				text = text.replaceFirst("%4", variables[3])
			}
			String modifiedErrorMessage = 'Received error from A&amp;AI (' + text +')'
			logger.debug("ModifiedErrorMessage " + modifiedErrorMessage)
			// let $ModifiedErrorMessage := concat( 'Received error from A',$exceptionaai:ampersand,'AI (' ,functx:replace-multi($ErrorMessage,$from,$Variables ),')')
			buildWorkflowException(execution, 5000, modifiedErrorMessage)

			wfex = execution.getVariable("WorkflowException")
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Fault", "BPMN",
					ErrorCode.UnknownError.getValue(), wfex.errorMessage);
			return wfex
		} else {
			try {
				errorCode = MapErrorCode(errorMsg)
				String mappedErrorMessage = MapErrorMessage(errorMsg, errorCode)

				int errorCodeInt = Integer.parseInt(errorCode)
				buildWorkflowException(execution, errorCodeInt, mappedErrorMessage)

				logger.debug("mappedErrorMessage " + mappedErrorMessage)
				wfex = execution.getVariable("WorkflowException")
				logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Fault", "BPMN",
						ErrorCode.UnknownError.getValue(), wfex.errorMessage);
				return wfex
			} catch(Exception ex) {
				logger.debug("error mapping error, return null: " + ex)
				return null

			}
		}
		}catch(Exception e){
			logger.debug("Exception occured during MapAAIExceptionToWorkflowException: " + e)
			buildWorkflowException(execution, 5000, "Error mapping AAI Response to WorkflowException")
		}
	}

	/**
	 * This error handling method maps an AAI Exception response to a
	 * WorkflowException Object.  It then sets the WorkflowException Object
	 * on the execution as "WorkflowException".
	 *
	 * This method takes the exact exception inside the <Fault> tags from AAI Response
	 * and puts it into the WorkflowException's errorMessage.
	 *
	 * @param execution the execution
	 * @param response the aai exception
	 */
	WorkflowException MapAAIExceptionToWorkflowExceptionGeneric(DelegateExecution execution, String response, int resCode){
		def utils=new MsoUtils()
		logger.debug("Start MapAAIExceptionToWorkflowExceptionGeneric Process")

		WorkflowException wfex
		try {
			if(utils.nodeExists(response, "Fault")){
				String fault = utils.getNodeXml(response, "Fault")
				fault = utils.removeXmlPreamble(fault)
				fault = fault.replace("<Fault>", "").replace("</Fault>", "")
				fault = fault.replaceAll("\\s+\\s+", "") // Removes extra white spaces
				buildWorkflowException(execution, resCode, fault)
			}else if(utils.nodeExists(response, "RESTFault")){
				String rFault = utils.getNodeXml(response, "RESTFault")
				buildWorkflowException(execution, resCode, rFault)
			}else{
				buildWorkflowException(execution, resCode, "Received a bad response from AAI")
			}
		} catch (Exception ex) {
			logger.debug("Exception Occured during MapAAIExceptionToWorkflowExceptionGeneric: " + ex)
			buildWorkflowException(execution, resCode, "Internal Error - Occured in MapAAIExceptionToWorkflowExceptionGeneric")

		}
		logger.debug("Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"))
		logger.debug("Completed MapAAIExceptionToWorkflowExceptionGeneric Process")
	}

	/**
	 * This method takes a WorkflowException Object and builds
	 * WorkflowException Xml. This method should only be used
	 * for the purpose of sending a sync error response or for
	 * creating a FalloutHandler request.
	 *
	 *@param - WorkflowException Object
	 *
	 *@return - String WorkflowException Xml
	 *
	 *
	 */
	String buildErrorResponseXml(WorkflowException wfex) {
		String xml
		if(wfex != null){
			String mes = XmlEscapers.xmlContentEscaper().escape(wfex.getErrorMessage())
			int code = wfex.getErrorCode()
			xml =
			"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(mes)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>${MsoUtils.xmlEscape(code)}</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""

		}else{
		 	xml =
			"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>Internal Error</aetgt:ErrorMessage>
					<aetgt:ErrorCode>2500</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""
		}
		return xml
	}

	/*
	5000    Received error from A&AI ($A&AI_ERROR)  Asynchronous    During orchestration of the recipe, A&AI returned an error. The error returned by A&AI is passed through in $A&AI_ERROR.
	5010    Could not communicate with A&AI Asynchronous    During orchestration of the recipe, a connection with A&AI could not be established.
	5020    No response from A&AI   Asynchronous    During orchestration of the recipe, communication was established with A&AI, but no response was received within the configured timeout.
	*/
	/**
	 *
	 * Utility Method for MapAAIExceptionToWorkflowException
	 *
	 *@param - String ErrorMessage
	 *
	 *@return - String ErrorCode
	 *
	 */
	private String MapErrorCode(String errorMessage)
	{
		if(errorMessage==null){
			return '5000'
		}
		errorMessage = errorMessage.toLowerCase();
		if(errorMessage.contains('timed out') || errorMessage.contains('timeout'))
			return '5020'
		else if (errorMessage.contains('connection'))
			return '5010'
		else
			return '5000'
	}

	/**
	 *
	 * Utility Method for MapAAIExceptionToWorkflowException
	 *
	 *@param - String ErrorMessage
	 *@param - String ErrorCode
	 *
	 *@return - String ErrorMessage
	 *
	 */
	private String MapErrorMessage(String errorMessage, String errorCode)
	{
		if(errorMessage == null){
			errorMessage=""
		}
		if( errorCode.equals('5010')){
					return 'Could not communicate with A&AI'
		}else if (errorCode.equals('5020')){
			return 'No response from A&AI'
		}else{
			return 'Received error from A&AI (' +errorMessage +')'
		}
	}

	/**
	 *
	 * Utility Method for Mapping SDNC
	 * Adapter Response Codes
	 *
	 *@param - String sdncResponseCode
	 *
	 *@return - String code
	 *
	 */
	String MapSDNCResponseCodeToErrorCode(String sdncResponseCode)
	{
		if (sdncResponseCode == '500') {
			return '5310'
		} else if ( sdncResponseCode == '408') {
			 return '5320'
		} else if ( sdncResponseCode == '60010') {
			 return '5350'
		} else {
		   return '5300'
		}
	}

	/**
	 * This error handling method builds a WorkflowException Object.  It sets it on
	 * the execution as "WorkflowException".
	 *
	 * @param execution the execution
	 * @param errorCode the error code
	 * @param errorMessage the error message
	 */
	public void buildWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
		MsoUtils utils = new MsoUtils()
		String processKey = getProcessKey(execution);
		logger.debug("Building a WorkflowException for " + processKey)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
		execution.setVariable("WorkflowException", exception);
		logger.debug("Outgoing WorkflowException is " + exception)
	}
	
	public void buildWorkflowException(DelegateExecution execution, int errorCode, String errorMessage, ONAPComponentsList extSystemErrorSource) {
		MsoUtils utils = new MsoUtils()
		String processKey = getProcessKey(execution);
		logger.debug("Building a WorkflowException for " + processKey)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage, extSystemErrorSource);
		execution.setVariable("WorkflowException", exception);
		logger.debug("Outgoing WorkflowException is " + exception)
	}

	/**
	 * This error handling method builds a WorkflowException Object and throws a
	 * MSOWorkflowException.  It throws a "MSOWorkflowException" BpmnError after
	 * setting the WorkflowException Object on the execution as "WorkflowException".
	 *
	 * @param execution the execution
	 * @param errorCode the error code
	 * @param errorMessage the error message
	 */
	public void buildAndThrowWorkflowException(DelegateExecution execution, int errorCode, String errorMessage) {
		String processKey = getProcessKey(execution);
		logger.debug("Building a WorkflowException for Subflow " + processKey)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
		execution.setVariable("WorkflowException", exception);
		logger.debug("Outgoing WorkflowException is " + exception)
		logger.debug("Throwing MSOWorkflowException")
		throw new BpmnError(errorCode.toString(), String.format("MSOWorkflowException: %s", errorMessage))
	}

	/**
	 * This method is executed after an MSOWorkflowException is caught by a
	 * subflow (during subflows "Error Handling Sub Process").
	 * It ensures the WorkflowException variable is populated before ending the
	 * subflow and also logs the subflows outgoing WorkflowException Variable.
	 *
	 * @param - execution
	 *
	 */
	public void processSubflowsBPMNException(DelegateExecution execution){
		String processKey = getProcessKey(execution)
		try{
			logger.debug("Started ProcessSubflowsBPMNException Method")
			if(execution.getVariable("WorkflowException") == null){
				buildWorkflowException(execution, 2500, "Internal Error - Occured During " + processKey)
			}

			logger.debug(processKey + " Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"))
		}catch(Exception e){
			logger.debug("Caught Exception during ProcessSubflowsBPMNException Method: " + e)
		}
		logger.debug("Completed ProcessSubflowsBPMNException Method")
	}

	/**
	 * This method is executed after an MSOWorkflowException is caught by a
	 * Mainflow. It builds and returns a FalloutHandler Request. It also
	 * verifies the WorkflowException variable is populated.
	 *
	 * @param - execution
	 * @param - requestInfo
	 *
	 * @return - falloutHandlerRequest
	 *
	 */
	public String processMainflowsBPMNException(DelegateExecution execution, String requestInfo){
		String processKey = getProcessKey(execution)
		try{
			logger.debug("Started ProcessMainflowBPMNException Method")
			if(execution.getVariable("WorkflowException") == null || isBlank(requestInfo)){
				buildWorkflowException(execution, 2500, "Internal Error - WorkflowException Object and/or RequestInfo is null! " + processKey)
			}
			requestInfo = utils.removeXmlPreamble(requestInfo)
			WorkflowException wfex = execution.getVariable("WorkflowException")
			String errorMessage = XmlEscapers.xmlContentEscaper().escape(wfex.getErrorMessage())
			int errorCode = wfex.getErrorCode()

			String falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   ${requestInfo}
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			logger.debug(processKey + " Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"))
			logger.debug(processKey + " Outgoing FalloutHandler Request is: " + falloutHandlerRequest)

			return falloutHandlerRequest

		}catch(Exception e){
			logger.debug("Caught Exception during ProcessMainflowBPMNException Method: " + e)
			return null
		}
		logger.debug("Completed ProcessMainflowBPMNException Method")
	}

	/**
	 *
	 * This method is executed after an Java Exception is caught
	 * It sets the WorkflowException variable. The method can be used in either mainflow or subflows.
	 *
	 * @param - execution
	 *
	 */
	public void processJavaException(DelegateExecution execution){
		String processKey = getProcessKey(execution)
		try{
			logger.debug("Caught a Java Exception in " + processKey)
			logger.debug("Started processJavaException Method")
			// if the BPMN flow java error handler sets "BPMN_javaExpMsg", append it to the WFE
			String javaExpMsg = execution.getVariable("BPMN_javaExpMsg")
            String errorMessage = execution.getVariable("gUnknownError")
			String wfeExpMsg = "Catch a Java Lang Exception in " + processKey
			if (javaExpMsg != null && !javaExpMsg.empty) {
				wfeExpMsg = wfeExpMsg + ": " + javaExpMsg
			}
            if (errorMessage != null && !errorMessage.empty) {
                logger.error("Unknown Error: " + errorMessage);
            }
            logger.error("Java Error: " + wfeExpMsg);
			buildWorkflowException(execution, 2500, wfeExpMsg)

		}catch(BpmnError b){
            logger.error(b);
			throw b
		}catch(Exception e){
            logger.error(e);
			logger.debug("Caught Exception during processJavaException Method: " + e)
			buildWorkflowException(execution, 2500, "Internal Error - During Process Java Exception")
		}
		logger.debug("Completed processJavaException Method")
	}


	public void preProcessRequest(DelegateExecution execution) {
		// TODO Auto-generated method stub

	}
	
	public String getErrorMessage(WorkflowException wfe, String processKey) {
		if(wfe == null) {
			return "Unexpected error encountered in " + processKey
		}
		else {
			return wfe.getErrorMessage()
		}
	}
	
	public int getErrorCode(WorkflowException wfe) {
		if(wfe == null) {
			return 2500
		}
		else {
			return wfe.getErrorCode()
		}
	}
}
