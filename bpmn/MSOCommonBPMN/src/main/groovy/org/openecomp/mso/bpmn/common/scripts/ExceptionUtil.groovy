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

package org.openecomp.mso.bpmn.common.scripts

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.core.WorkflowException

/**
 * @version 1.0
 */
class ExceptionUtil extends AbstractServiceTaskProcessor {


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
	WorkflowException MapAAIExceptionToWorkflowException(String response, Execution execution)
	{
		def utils=new MsoUtils()
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def prefix=execution.getVariable("prefix")
		def errorMsg = execution.getVariable(prefix+"ErrorResponse")
		utils.log("DEBUG","=========== Begin MapAAIExceptionToWorkflowException ===========",isDebugEnabled)
		String text = null
		def variables
		String errorCode = '5000'
		WorkflowException wfex
		utils.log("DEBUG","response: " + response, isDebugEnabled)
		try{
		try {
			//String msg = utils.getNodeXml(response, "Fault")
			variables = utils.getMultNodes(response, "variable")
			text = utils.getNodeText1(response, "text")
		} catch (Exception ex) {
			//Ignore the exception - cases include non xml payload
			utils.log("DEBUG","error mapping error, ignoring: " + ex,isDebugEnabled)
		}

		if(text != null) {
			if(variables.size()>=4){
				text = text.replaceFirst("%1", variables[0])
				text = text.replaceFirst("%2", variables[1])
				text = text.replaceFirst("%3", variables[2])
				text = text.replaceFirst("%4", variables[3])
			}
			String modifiedErrorMessage = 'Received error from A&amp;AI (' + text +')'
			utils.log("DEBUG", "ModifiedErrorMessage " + modifiedErrorMessage, isDebugEnabled)
			// let $ModifiedErrorMessage := concat( 'Received error from A',$exceptionaai:ampersand,'AI (' ,functx:replace-multi($ErrorMessage,$from,$Variables ),')')
			buildWorkflowException(execution, 5000, modifiedErrorMessage)

			wfex = execution.getVariable("WorkflowException")
			utils.log("ERROR","Fault:"+ wfex)
			return wfex
		} else {
			try {
				errorCode = MapErrorCode(errorMsg)
				String mappedErrorMessage = MapErrorMessage(errorMsg, errorCode)

				int errorCodeInt = Integer.parseInt(errorCode)
				buildWorkflowException(execution, errorCodeInt, mappedErrorMessage)

				utils.log("DEBUG", "mappedErrorMessage " + mappedErrorMessage, isDebugEnabled)
				wfex = execution.getVariable("WorkflowException")
				utils.log("ERROR","Fault:"+ wfex, isDebugEnabled)
				return wfex
			} catch(Exception ex) {
				utils.log("DEBUG","error mapping error, return null: " + ex, isDebugEnabled)
				return null

			}
		}
		}catch(Exception e){
			utils.log("DEBUG", "Exception occured during MapAAIExceptionToWorkflowException: " + e, isDebugEnabled)
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
	WorkflowException MapAAIExceptionToWorkflowExceptionGeneric(Execution execution, String response, int resCode){
		def utils=new MsoUtils()
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", "Start MapAAIExceptionToWorkflowExceptionGeneric Process", isDebugLogEnabled)

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
			utils.log("DEBUG", "Exception Occured during MapAAIExceptionToWorkflowExceptionGeneric: " + ex, isDebugLogEnabled)
			buildWorkflowException(execution, resCode, "Internal Error - Occured in MapAAIExceptionToWorkflowExceptionGeneric")

		}
		utils.log("DEBUG", "Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"), isDebugLogEnabled)
		utils.log("DEBUG", "Completed MapAAIExceptionToWorkflowExceptionGeneric Process", isDebugLogEnabled)
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
			String mes = wfex.getErrorMessage()
			int code = wfex.getErrorCode()
			xml =
			"""<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${mes}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>${code}</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""

		}else{
		 	xml =
			"""<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
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
		errorMessage = errorMessage.toLowerCase()
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
					return 'Could not communicate with A&amp;AI'
		}else if (errorCode.equals('5020')){
			return 'No response from A&amp;AI'
		}else{
			errorMessage = errorMessage.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			return 'Received error from A&amp;AI (' +errorMessage +')'
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
	public void buildWorkflowException(Execution execution, int errorCode, String errorMessage) {
		MsoUtils utils = new MsoUtils()
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String processKey = getProcessKey(execution)
		utils.log("DEBUG", "Building a WorkflowException for " + processKey, isDebugLogEnabled)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage)
		execution.setVariable("WorkflowException", exception)
		utils.log("DEBUG", "Outgoing WorkflowException is " + exception, isDebugLogEnabled)
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
	public void buildAndThrowWorkflowException(Execution execution, int errorCode, String errorMessage) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String processKey = getProcessKey(execution)
		utils.log("Building a WorkflowException for Subflow " + processKey, isDebugLogEnabled)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage)
		execution.setVariable("WorkflowException", exception)
		utils.log("DEBUG", "Outgoing WorkflowException is " + exception, isDebugLogEnabled)
		utils.log("DEBUG", "Throwing MSOWorkflowException", isDebugLogEnabled)
		throw new BpmnError("MSOWorkflowException")
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
	public void processSubflowsBPMNException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String processKey = getProcessKey(execution)
		try{
			utils.log("DEBUG", "Started ProcessSubflowsBPMNException Method", isDebugEnabled)
			if(execution.getVariable("WorkflowException") == null){
				buildWorkflowException(execution, 2500, "Internal Error - Occured During " + processKey)
			}

			utils.log("DEBUG", processKey + " Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"), isDebugEnabled)
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during ProcessSubflowsBPMNException Method: " + e, isDebugEnabled)
		}
		utils.log("DEBUG", "Completed ProcessSubflowsBPMNException Method", isDebugEnabled)
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
	public String processMainflowsBPMNException(Execution execution, String requestInfo){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String processKey = getProcessKey(execution)
		try{
			utils.log("DEBUG", "Started ProcessMainflowBPMNException Method", isDebugEnabled)
			if(execution.getVariable("WorkflowException") == null || isBlank(requestInfo)){
				buildWorkflowException(execution, 2500, "Internal Error - WorkflowException Object and/or RequestInfo is null! " + processKey)
			}
			requestInfo = utils.removeXmlPreamble(requestInfo)
			WorkflowException wfex = execution.getVariable("WorkflowException")
			String errorMessage = wfex.getErrorMessage()
			int errorCode = wfex.getErrorCode()

			String falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   ${requestInfo}
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.log("DEBUG", processKey + " Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"), isDebugEnabled)
			utils.log("DEBUG", processKey + "Completed ProcessMainflowBPMNException Outgoing FalloutHandler Request is: " + falloutHandlerRequest, isDebugEnabled)
			return falloutHandlerRequest

		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during ProcessMainflowBPMNException Method: " + e, isDebugEnabled)
			return null
		}
	}

	/**
	 *
	 * This method is executed after an Java Exception is caught
	 * It sets the WorkflowException variable. The method can be used in either mainflow or subflows.
	 *
	 * @param - execution
	 *
	 */
	public void processJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String processKey = getProcessKey(execution)
		try{
			utils.log("DEBUG", "Caught a Java Exception in " + processKey, isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			buildWorkflowException(execution, 2500, "Catch a Java Lang Exception in " + processKey)

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			buildWorkflowException(execution, 2500, "Internal Error - During Process Java Exception")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}


	public void preProcessRequest(Execution execution) {
		// TODO Auto-generated method stub

	}
}
