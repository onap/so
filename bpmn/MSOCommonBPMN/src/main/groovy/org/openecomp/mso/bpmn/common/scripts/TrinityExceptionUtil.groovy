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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*

class TrinityExceptionUtil {
	
	
	
	public static enum Error {
		SVC_GENERAL_SERVICE_ERROR("SVC0001","Internal Error"),
		SVC_BAD_PARAMETER("SVC0002", "Invalid input value for message part %1"),
		SVC_NO_SERVER_RESOURCES("SVC1000", "No server resources available to process the request"),
		SVC_DETAILED_SERVICE_ERROR("SVC2000", "The following service error occurred: %1. Error code is %2."),
		POL_GENERAL_POLICY_ERROR("POL0001", "A policy error occurred."),
		POL_USER_NOT_PROVISIONED("POL1009", "User has not been provisioned for service"),
		POL_USER_SUSPENDED("POL1010", "User has been suspended from service"),
		POL_DETAILED_POLICY_ERROR("POL2000", "The following policy error occurred: %1. Error code is %2."),
		POL_MSG_SIZE_EXCEEDS_LIMIT("POL9003", "Message content size exceeds the allowable limit")
	

		private final String msgId
		private final String msgTxt

		private Error(String msgId, String msgTxt) {
			this.msgId = msgId
			this.msgTxt = msgTxt
		}

		public String getMsgId() {
		     return msgId
		  }

		public String getMsgTxt() {
			return msgTxt
		}

	}


	
	
	String mapAdapterExecptionToCommonException(String response, DelegateExecution execution)
	{
		def utils=new MsoUtils()
		def method = getClass().getSimpleName() + '.mapAdapterExecptionToCommonException(' +
			'execution=' + execution.getId() +
			')'

		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG",'Entered ' + method, isDebugLogEnabled)

		
		def errorCode

		
		try {
			  errorCode = MapCategoryToErrorCode(utils.getNodeText(response, "category")) 
			  execution.setVariable(prefix+"err",errorCode)
			  String message = buildException(response, execution)
			  utils.log("DEBUG","=========== End MapAdapterExecptionToWorkflowException ===========",isDebugLogEnabled)
			  return message
		}catch (Exception ex) {
			//Ignore the exception - cases include non xml payload
			utils.log("DEBUG","error mapping error, ignoring: " + ex,isDebugLogEnabled)
			utils.log("DEBUG","=========== End MapAdapterExecptionToWorkflowException ===========",isDebugLogEnabled)
			return buildException(response, execution)
		} 
	}
	
	/**
	 * @param response
	 * @param execution
	 * @return mapped exception
	 */
	String mapAOTSExecptionToCommonException(String response, DelegateExecution execution)
	{
		def utils=new MsoUtils()

		def prefix=execution.getVariable("prefix")
		def method = getClass().getSimpleName() + '.mapAOTSExecptionToCommonException(' +
			'execution=' + execution.getId() +
			')'

		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG",'Entered ' + method, isDebugLogEnabled)
		
		
		try {
			  def errorCode = utils.getNodeText(response,"code")
			  def descr = utils.getNodeText(response, "description")
			  def mappedErr = mapErrorCodetoError(errorCode, descr)
			  if(mappedErr == Error.SVC_DETAILED_SERVICE_ERROR || mappedError == Error.POL_DETAILED_POLICY_ERROR){
				  ArrayList myVars = new ArrayList()
				  myVars.add(descr)
				  myVars.add(errorCode)
				  execution.setVariable(prefix+"errVariables", myVars)
			  }
			  execution.setVariable(prefix+"err",mappedErr)
			  def message = buildException("Received error from AOTS: " + descr, execution)
			  utils.log("DEBUG","=========== End MapAOTSExecptionToCommonException ===========",isDebugLogEnabled)
			  return message
		}catch (Exception ex) {
			//Ignore the exception - cases include non xml payload
			utils.log("DEBUG","error mapping error, ignoring: " + ex,isDebugLogEnabled)
			utils.log("DEBUG","=========== End MapAOTSExecptionToCommonException ===========",isDebugLogEnabled)
			return buildException(response, execution)
		}
	}
	
	String mapSDNCAdapterExceptionToErrorResponse(String sdncAdapterCallbackRequest, DelegateExecution execution) {
		def utils=new MsoUtils()
		def prefix=execution.getVariable("prefix")
		def method = getClass().getSimpleName() + '.mapSDNCAdapterExceptionToErrorResponse(' +
			'execution=' + execution.getId() +
			')'

		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG",'Entered ' + method, isDebugLogEnabled)
		
		def sdncResponseCode
		String responseCode = execution.getVariable(prefix+"ResponseCode")
		utils.log("DEBUG",'responseCode to map: ' + responseCode, isDebugLogEnabled)
		def errorMessage
		
		try {
			
			if(utils.nodeExists(sdncAdapterCallbackRequest, "RequestData")) {
				def reqDataXml = StringEscapeUtils.unescapeXml(utils.getNodeXml(sdncAdapterCallbackRequest, "RequestData"))
				errorMessage = utils.getNodeText(reqDataXml, "response-message")
				sdncResponseCode = utils.getNodeText(reqDataXml, "response-code")
			}else{
				errorMessage = utils.getNodeText(sdncAdapterCallbackRequest, "ResponseMessage")
				sdncResponseCode = responseCode
		    }
			def mappedErr = mapErrorCodetoError(responseCode, errorMessage)
			errorMessage = errorMessage.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			def modifiedErrorMessage = "Received error from SDN-C: " + errorMessage
			if(mappedErr == Error.SVC_DETAILED_SERVICE_ERROR || mappedErr == Error.POL_DETAILED_POLICY_ERROR){
				ArrayList myVars = new ArrayList()
				myVars.add(errorMessage)
				myVars.add(sdncResponseCode)
				execution.setVariable(prefix+"errVariables", myVars)
			}
			execution.setVariable(prefix+"err",mappedErr)
			def message = buildException(modifiedErrorMessage, execution)

			
			utils.log("DEBUG","=========== End MapSDNCAdapterException ===========",isDebugLogEnabled)
		    return message
		}catch (Exception ex) {
			//Ignore the exception - cases include non xml payload
			utils.log("DEBUG","error mapping sdnc error, ignoring: " + ex,isDebugLogEnabled)
			utils.log("DEBUG","=========== End MapSDNCAdapterException ===========",isDebugLogEnabled)
			return null
		} 
		
	}
	
	/**
	 * @param response message from called component (ex: AAI)
	 * @param execution
	 * @return an error response conforming to the common 
	 */
	String mapAAIExceptionTCommonException(String response, DelegateExecution execution)
	{
		def utils=new MsoUtils()
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def prefix=execution.getVariable("prefix")
		def method = getClass().getSimpleName() + '.mapAAIExceptionTCommonException(' +
			'execution=' + execution.getId() +
			')'

		utils.log("DEBUG",'Entered ' + method, isDebugLogEnabled)
		def variables
		def message
		String errorCode = 'SVC0001'
		utils.log("DEBUG","response: " + response, isDebugLogEnabled)
		//they use the same format we do, pass their error along
		//TODO add Received error from A&AI at beg of text
		try {
			 message = utils.getNodeXml(response, "requestError")
			 message = utils.removeXmlNamespaces(message)
		} catch (Exception ex) {
			//Ignore the exception - cases include non xml payload
				message = buildException("Received error from A&AI, unable to parse",execution)
			utils.log("DEBUG","error mapping error, ignoring: " + ex,isDebugLogEnabled)
		}
		
		if(message != null) { 
			 execution.setVariable(prefix+"ErrorResponse",message)
			 utils.log("ERROR","Fault:"+ execution.getVariable(prefix+"ErrorResponse"))
			 return message
		} else {
			
				return null
			
		}
	}
	
	/**
	 * @param execution
	 * @return an error response conforming to the common API with default text msg
	 */
	String buildException(execution){
		return buildException(null, execution)
	}
	
	/**
	 * @param response message from called component (ex: AAI)
	 * @param execution
	 * @return an error response conforming to the common
	 */
	String buildException(response, execution){
		def utils=new MsoUtils()
		def method = getClass().getSimpleName() + '.buildException(' +
			'execution=' + execution.getId() +
			')'

		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG",'Entered ' + method, isDebugLogEnabled)
		def prefix=execution.getVariable("prefix")
		def responseCode = String.valueOf(execution.getVariable(prefix+"ResponseCode"))
		def variables
		utils.log("DEBUG","response: " + response, isDebugLogEnabled)
		
			try {
				utils.log("DEBUG","formatting error message" ,isDebugLogEnabled)
				def msgVars = execution.getVariable(prefix+"errVariables")
				def myErr = execution.getVariable(prefix+"err")
				def messageTxt = execution.getVariable(prefix+"errTxt")
				def messageId = null
				
				if(myErr == null){
					utils.log("DEBUG","mapping response code: " + responseCode, isDebugLogEnabled)
					myErr = mapErrorCodetoError(responseCode, response)
					if(myErr == null){
						//not a service or policy error, just return error code
						return ""
					}
				}
				messageId = myErr.getMsgId()
				
				if(messageTxt == null){
					if(myErr!=null){
						messageTxt = myErr.getMsgTxt()
					}else{
						messageTxt = response
					}
				}
				
				if(msgVars==null && (myErr == Error.SVC_DETAILED_SERVICE_ERROR || myErr == Error.POL_DETAILED_POLICY_ERROR)){
					msgVars = new ArrayList()
					msgVars.add(response)
					msgVars.add(responseCode)
				}
				
				def msgVarsXML=""
				StringBuffer msgVarsBuff = new StringBuffer()
				if(msgVars!=null){
					for(String msgVar : msgVars){
						msgVarsBuff.append(
							"""
			<tns:variables>${msgVar}</tns:variables>""")
					}
					
				}
				def message = ""
				if(messageId.startsWith("SVC")){
					message = """<tns:requestError xmlns:tns="http://org.openecomp/mso/request/types/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://org.openecomp/mso/request/types/v1 MsoServiceInstanceTypesV1.xsd">
	<tns:serviceException>
		<tns:messageId>${messageId}</tns:messageId>
		<tns:text>${messageTxt}</tns:text>${msgVarsBuff}
	</tns:serviceException>
</tns:requestError>""" 
				}else{
					message ="""<tns:requestError xmlns:tns="http://org.openecomp/mso/request/types/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://org.openecomp/mso/request/types/v1 MsoServiceInstanceTypesV1.xsd">
	<tns:policyException>
		<tns:messageId>${messageId}</tns:messageId>
		<tns:text>${messageTxt}</tns:text>${msgVarsBuff}
	</tns:policyException>
</tns:requestError>""" 
				}
				 utils.log("DEBUG", "message " + message, isDebugLogEnabled)
				 execution.setVariable(prefix+"ErrorResponse",message)
				 execution.setVariable(prefix+"err", myErr)
				 execution.setVariable(prefix+"errTxt", messageTxt)
				 execution.setVariable(prefix+"errVariables", msgVars)
				 utils.log("ERROR","Fault:"+ execution.getVariable(prefix+"ErrorResponse"))
				 return message
			}catch(Exception ex) {
				utils.log("DEBUG","error mapping error, return null: " + ex,isDebugLogEnabled)
				return null
			}

	}
	
	String parseError(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def utils=new MsoUtils()
		def prefix=execution.getVariable("prefix")
		def text = execution.getVariable(prefix+"errTxt")
		def msgVars = execution.getVariable(prefix+"errVariables")
		utils.log("DEBUG",'parsing message: ' + text, isDebugLogEnabled)
		if(text == null){
			return 'failed'
		}
		if(msgVars!=null && !msgVars.isEmpty()){
			for(int i=0; i<msgVars.size(); i++){
				text = text.replaceFirst("%"+(i+1), msgVars[i])
			}
		}
		utils.log("DEBUG",'parsed message is: ' + text, isDebugLogEnabled)
		return text
	}
	
	

	Error mapErrorCodetoError(responseCode, descr)
	{
		
		if(responseCode==null || responseCode=='0' || responseCode=='500' || responseCode =='408'){
			return Error.SVC_NO_SERVER_RESOURCES
		}else if(responseCode == '401' || responseCode == '405' || responseCode == '409' || responseCode == '503'){
			return null
		}else if(responseCode == '400'){
			if(descr==null){
				return Error.SVC_GENERAL_SERVICE_ERROR
			}else{
				return Error.SVC_DETAILED_SERVICE_ERROR
			}
		}else if(responseCode == '401'){
			if(descr==null){
				return Error.POL_GENERAL_POLICY_ERROR
			}else{
				return Error.POL_DETAILED_POLICY_ERROR
			}
		}else{
			return Error.SVC_NO_SERVER_RESOURCES
		}
	}
	
	String mapCategoryToErrorCode(String errorCategory)
	{
		if(errorCategory.equals('OPENSTACK'))
			return Error.SVC_NO_SERVER_RESOURCES
		else if (errorCategory.equals('IO'))
			return Error.SVC_NO_SERVER_RESOURCES
		else if (errorCategory.equals('INTERNAL'))
			return Error.SVC_NO_SERVER_RESOURCES
		else if (errorCategory.equals('USERDATA'))
			return Error.SVC_GENERAL_SERVICE_ERROR
		else
			return Error.SVC_GENERAL_SERVICE_ERROR
	}
	
	
	

	
	
}