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

package com.att.bpm.scripts

import org.openecomp.mso.bpmn.core.WorkflowException

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @version 1.0
 */
class ExceptionUtil extends AbstractServiceTaskProcessor {

	/**
	 *
	 * @Deprecated
	 *
	 * Instead use <method>MapAAIExceptionToWorkflowException</method>
	 *
	 * To Be Removed Once Refactoring Main Flow Error Handling Is Complete
	 *
	 *
	 */
	@Deprecated
	String MapAAIExceptionToWorkflowExceptionOld(String response, Execution execution)
	{
		def utils=new MsoUtils()
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def prefix=execution.getVariable("prefix")
		def errorMsg = execution.getVariable(prefix+"ErrorResponse")
		utils.log("DEBUG","=========== Begin MapAAIExceptionToWorkflowException ===========",isDebugEnabled)
		String text = null
		def variables
		String errorCode = '5000'
		utils.log("DEBUG","response: " + response, isDebugEnabled)
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
			String message = """<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
				<aetgt:ErrorMessage>$modifiedErrorMessage</aetgt:ErrorMessage>
				<aetgt:ErrorCode>$errorCode</aetgt:ErrorCode>
			</aetgt:WorkflowException>"""
			 execution.setVariable(prefix+"ErrorResponse",message)
			 utils.log("ERROR","Fault:"+ execution.getVariable(prefix+"ErrorResponse"))
			 return message
		} else {
			try {
				errorCode = MapErrorCode(errorMsg)
				String mappedErrorMessage = MapErrorMessage(errorMsg, errorCode)

				String message = """<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>$mappedErrorMessage</aetgt:ErrorMessage>
					<aetgt:ErrorCode>$errorCode</aetgt:ErrorCode>
				</aetgt:WorkflowException>"""
				utils.log("DEBUG", "mappedErrorMessage " + mappedErrorMessage, isDebugEnabled)
				 execution.setVariable(prefix+"ErrorResponse",message)
				 utils.log("ERROR","Fault:"+ execution.getVariable(prefix+"ErrorResponse"))
				 return message
			} catch(Exception ex) {
				utils.log("DEBUG","error mapping error, return null: " + ex,isDebugEnabled)
				return null

			}
		}
	}

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
	 *
	 *This method build a WorkflowException using the adapters response.
	 *
	 *@param String response
	 *@param String adapter
	 *
	 *@return WorkflowException wfex
	 */
	WorkflowException MapAdapterExecptionToWorkflowException(String response, Execution execution, String adapter){
		def utils=new MsoUtils()
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def prefix=execution.getVariable("prefix")
		utils.log("DEBUG","=========== Start MapAdapterExecptionToWorkflowException Process ===========",isDebugEnabled)

		String errorCode
		def errorMessage
		WorkflowException wfex
		try {
			errorCode = MapCategoryToErrorCode(utils.getNodeText(response, "category"))
			errorMessage = MapAdapterErrorMessage(utils.getNodeText(response, "message"), errorCode, adapter)

			int errorCodeInt = Integer.parseInt(errorCode)
			buildWorkflowException(execution, errorCodeInt, errorMessage)
		}catch (Exception ex) {
			utils.log("DEBUG", "Exception Occured during MapAdapterExecptionToWorkflowException: " + ex, isDebugEnabled)
			buildWorkflowException(execution, 2500, "Internal Error - Occured in MapAdapterExecptionToWorkflowException")
		}
		wfex = execution.getVariable("WorkflowException")
		return wfex
		utils.log("DEBUG","=========== Completed MapAdapterExecptionToWorkflowException Process ===========",isDebugEnabled)
	}


	/**
	 *
	 * @Deprecated
	 *
	 * Instead use <method>buildWorkflowException(Execution execution, int errorCode, String errorMessage)</method> method below
	 *
	 * To Be Removed Once Refactoring Of Main Flow Error Handling Is Complete
	 *
	 */
	@Deprecated
	String buildWorkflowExceptionXml(String errorCode, String errorMessage) {
		return """<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""
	}

	/**
	 * This method takes a WorkflowException Object and builds
	 * WorkflowException Xml. This method should only be used
	 * for the purpose of sending an error response.
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
			"""<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${mes}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>${code}</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""

		}else{
		 	xml =
			"""<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
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
	String MapErrorCode(String errorMessage)
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

	String MapErrorMessage(String errorMessage, String errorCode)
	{
		if(errorMessage == null){
			errorMessage=""
		}
		if( errorCode.equals('5010'))
					return 'Could not communicate with A&amp;AI'
		else if (errorCode.equals('5020'))
			return 'No response from A&amp;AI'
		else
			return 'Received error from A&amp;AI (' +errorMessage +')'
	}

	String MapCategoryToErrorCode(String errorCategory)
	{
		if(errorCategory.equals('OPENSTACK'))
			return '5100'
		else if (errorCategory.equals('IO'))
			return '5110'
		else if (errorCategory.equals('INTERNAL'))
			return '7020'
		else if (errorCategory.equals('USERDATA'))
			return '7020'
		else
			return '7020'
	}


	String MapAdapterErrorMessage(String errorMessage, String errorCode, String adapter)
	{
		if(errorCode.equals('5100'))
			return 'Received error from Platform Orchestrator: ' + errorMessage
		else if(errorCode.equals('5110'))
		    return 'Could not communicate with Platform Orchestrator'
		else
		    return 'Received error from ' + adapter + ': ' + errorMessage
	}

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
		String processKey = getProcessKey(execution);
		utils.log("DEBUG", "Building a WorkflowException for " + processKey, isDebugLogEnabled)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
		execution.setVariable("WorkflowException", exception);
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
		String processKey = getProcessKey(execution);
		utils.log("Building a WorkflowException for Subflow " + processKey, isDebugLogEnabled)

		WorkflowException exception = new WorkflowException(processKey, errorCode, errorMessage);
		execution.setVariable("WorkflowException", exception);
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
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   ${requestInfo}
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.log("DEBUG", processKey + " Outgoing WorkflowException is: " + execution.getVariable("WorkflowException"), isDebugEnabled)
			utils.log("DEBUG", processKey + " Outgoing FalloutHandler Request is: " + falloutHandlerRequest, isDebugEnabled)

			return falloutHandlerRequest

		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during ProcessMainflowBPMNException Method: " + e, isDebugEnabled)
			return null
		}
		utils.log("DEBUG", "Completed ProcessMainflowBPMNException Method", isDebugEnabled)
	}

	/**
	 * This method is executed after an Java Exception is caught.
	 * It sets the WorkflowException variable and throws an MSOWorkflowException.
	 *
	 * @param - execution
	 *
	 */
	public void processSubflowsJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String processKey = getProcessKey(execution)
		try{
			utils.log("DEBUG", "Caught a Java Exception in " + processKey, isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			buildAndThrowWorkflowException(execution, 2500, "Catch a Java Lang Exception in " + processKey)

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			buildAndThrowWorkflowException(execution, 2500, "Internal Error - During Process Java Exception")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}


	public void preProcessRequest(Execution execution) {
		// TODO Auto-generated method stub

	}




}
