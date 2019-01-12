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

import org.onap.so.bpmn.core.UrnPropertiesReader

import java.text.SimpleDateFormat

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

public class FalloutHandler extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, FalloutHandler.class);

	String Prefix="FH_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	
	public initializeProcessVariables(DelegateExecution execution){
		def method = getClass().getSimpleName() + '.initializeProcessVariables(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		try {
			execution.setVariable("prefix",Prefix)
	
			//These variables are form the input Message to the BPMN
			execution.setVariable("FH_request_id","")
			execution.setVariable("FH_request_action","")
			execution.setVariable("FH_notification-url","")
			execution.setVariable("FH_mso-bpel-name","")
			execution.setVariable("FH_ErrorCode", "")
			execution.setVariable("FH_ErrorMessage", "")

			execution.setVariable("FH_notification-url-Ok", false)
			execution.setVariable("FH_request_id-Ok", false)

			//These variables are for Get Mso Aai Password Adapter
			execution.setVariable("FH_deliveryStatus", true)
	
			//update Response Status to pending ...Adapter variables
			execution.setVariable("FH_updateResponseStatusPayload", null)
			execution.setVariable("FH_updateResponseStatusResponse", null)
	
			//update Request Gamma ...Adapter variables
			execution.setVariable("FH_updateRequestGammaPayload", "")
			execution.setVariable("FH_updateRequestGammaResponse", null)
			execution.setVariable("FH_updateRequestGammaResponseCode", null)
	
			//update Request Infra ...Adapter variables
			execution.setVariable("FH_updateRequestInfraPayload", "")
			execution.setVariable("FH_updateRequestInfraResponse", null)
			execution.setVariable("FH_updateRequestInfraResponseCode", null)
	
			//assign True to success variable
			execution.setVariable("FH_success", true)
	
			//Set notify status to Failed variable
			execution.setVariable("FH_NOTIFY_STATUS", "SUCCESS")
	
			//Set DB update variable
			execution.setVariable("FH_updateRequestPayload", "")
			execution.setVariable("FH_updateRequestResponse", null)
			execution.setVariable("FH_updateRequestResponseCode", null)
	
			//Auth variables
			execution.setVariable("BasicAuthHeaderValue","")
	
			//Parameter list
			execution.setVariable("FH_parameterList",  "")
	
			//Response variables
			execution.setVariable("FalloutHandlerResponse","")
			execution.setVariable("FH_ErrorResponse", null)
			execution.setVariable("FH_ResponseCode", "")
	
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}
	
	public void preProcessRequest (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		// Initialize flow variables
		initializeProcessVariables(execution)
		setSuccessIndicator(execution, false)
	
		setBasicDBAuthHeader(execution, execution.getVariable('isDebugLogEnabled'))
		
		try {
			def xml = execution.getVariable("FalloutHandlerRequest")
			msoLogger.debug(" XML --> " + xml)
			msoLogger.debug("FalloutHandler request: " + xml)
	
			//Check the incoming request type
			//Incoming request can be ACTIVE_REQUESTS (request-information node) or  INFRA_ACTIVE_REQUESTS (request-info node)
			if (utils.nodeExists(xml, "request-information")) {
				execution.setVariable("FH_request_id-Ok", true) // Incoming request is for ACTIVE_REQUESTS
			}
	
			//Check notification-url for the incoming request type
			//ACTIVE_REQUESTS may have notificationurl node
			//INFRA_ACTIVE_REQUESTS notificationurl node does not exist
			def notificationurl = ""
			if (utils.nodeExists(xml, "notification-url")) {
				notificationurl = utils.getNodeText(xml,"notification-url")
				if(notificationurl != null && !notificationurl.isEmpty()) {
					msoLogger.debug("********** Incoming notification Url is: " + notificationurl);
					execution.setVariable("FH_notification-url-Ok", true)
					execution.setVariable("FH_notification-url",notificationurl)
				}
			}
	
			//Check request_id for the incoming request type
			//For INFRA_ACTIVE_REQUESTS payload request-id IS optional (Not sure why this is option since req id is primary key ... also tried exe through SOAP UI to check if MSO code handles null like auto generated seq not it does not)
			//For ACTIVE_REQUESTS payload request-id is NOT optional
			def request_id = ""
			if (utils.nodeExists(xml, "request-id")) {
				execution.setVariable("FH_request_id",utils.getNodeText(xml,"request-id"))
			}
			msoLogger.debug("FH_request_id: " + execution.getVariable("FH_request_id"))
	
			// INFRA_ACTIVE_REQUESTS	 have "action" element ... mandatory
			// ACTIVE_REQUEST have "request-action" ... mandatory
			if (utils.nodeExists(xml, "request-action")) {
				execution.setVariable("FH_request_action",utils.getNodeText(xml,"request-action"))
			} else if (utils.nodeExists(xml, "action")) {
				execution.setVariable("FH_request_action",utils.getNodeText(xml,"action"))
			}
	
	
			//Check source for the incoming request type
			//For INFRA_ACTIVE_REQUESTS payload source IS optional
			//For ACTIVE_REQUESTS payload source is NOT optional
			def source = ""
			if (utils.nodeExists(xml, "source")) {
				execution.setVariable("FH_source",utils.getNodeText(xml,"source"))
			}
	
			//Check if ErrorCode node exists. If yes, initialize it from request xml, if no, it will stay with defaulf value already set in initializeProcessVariables() method above.
			def errorCode = ""
			if (utils.nodeExists(xml, "ErrorCode")) {
				errorCode = utils.getNodeText(xml,"ErrorCode")
				if(errorCode != null && !errorCode.isEmpty()) {
					execution.setVariable("FH_ErrorCode", errorCode)
				}
			}
			msoLogger.debug("FH_ErrorCode: " + errorCode)
	
			//Check if ErrorMessage node exists. If yes, initialize it from request xml, if no, it will stay with defaulf value already set in initializeProcessVariables() method above.
			def errorMessage = ""
			if (utils.nodeExists(xml, "ErrorMessage")) {
				errorCode = utils.getNodeText(xml,"ErrorMessage")
				if(errorCode != null && !errorCode.isEmpty()) {
					errorCode = errorCode
					execution.setVariable("FH_ErrorMessage", errorCode)
				}
			}
	
			//Check for Parameter List
			if (utils.nodeExists(xml, "parameter-list")) {
				def parameterList = utils.getNodeXml(xml, "parameter-list", false)
				execution.setVariable("FH_parameterList", parameterList)
			}
	
			msoLogger.trace("--> " + execution.getVariable(""))
			msoLogger.debug("FH_request_id-OK --> " + execution.getVariable("FH_request_id-Ok"))
	
			// set the DHV/Service Instantiation values if specified in the request
			execution.setVariable("FH_is_srv_inst_req", String.valueOf("true".equals(utils.getNodeText(xml, "is-srv-inst-req"))))
			msoLogger.trace("--> " + execution.getVariable(""))
			execution.setVariable("FH_is_json_content", String.valueOf("JSON".equals(utils.getNodeText(xml, "resp-content-type"))))
			msoLogger.trace("--> " + execution.getVariable(""))
			execution.setVariable("FH_service_inst_id", utils.getNodeText(xml, "service-instance-id"))
			msoLogger.trace("--> " + execution.getVariable(""))
			execution.setVariable("FH_start_time", utils.getNodeText(xml, "start-time"))
			msoLogger.trace("--> " + execution.getVariable(""))
			// this variable is used by the camunda flow to set the Content-Type for the async response
			if (execution.getVariable("FH_is_srv_inst_req").equals("true") &&
				execution.getVariable("FH_is_json_content").equals("true")) {
				execution.setVariable("FH_content_type", "application/json")
			} else {
				execution.setVariable("FH_content_type", "text/xml")
			}
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
	
		msoLogger.debug("OUTOF --> Initialize Variables Fallout Handler #########");
	}
	
	public String updateRequestPayload (DelegateExecution execution){
		def method = getClass().getSimpleName() + '.updateRequestPayload(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		try {
			String payload = """
					<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateRequest>
							<requestId>${MsoUtils.xmlEscape(execution.getVariable("FH_request_id"))}</requestId>
							<lastModifiedBy>BPEL</lastModifiedBy>
							<finalErrorMessage>${MsoUtils.xmlEscape(execution.getVariable("FH_ErrorMessage"))}</finalErrorMessage>
							<finalErrorCode>${MsoUtils.xmlEscape(execution.getVariable("FH_ErrorCode"))}</finalErrorCode>
							<status>FAILED</status>
							<responseStatus>${MsoUtils.xmlEscape(execution.getVariable("FH_NOTIFY_STATUS"))}</responseStatus>
						</req:updateRequest>
					</soapenv:Body>
					</soapenv:Envelope>
				"""
	
			msoLogger.debug("updateRequestPayload: " + payload)
			execution.setVariable("FH_updateRequestPayload", payload)
			return execution.getVariable("FH_updateRequestPayload")
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}
	
	public String updateRequestInfraPayload (DelegateExecution execution){
		def method = getClass().getSimpleName() + '.updateRequestInfraPayload(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		try {
			String payload = """
					<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateInfraRequest>
							<requestId>${MsoUtils.xmlEscape(execution.getVariable("FH_request_id"))}</requestId>
							<lastModifiedBy>BPEL</lastModifiedBy>
							<statusMessage>${MsoUtils.xmlEscape(execution.getVariable("FH_ErrorMessage"))}</statusMessage>
							<requestStatus>FAILED</requestStatus>
							<progress>100</progress>
						</req:updateInfraRequest>
					</soapenv:Body>
					</soapenv:Envelope>
				"""
	
			execution.setVariable("FH_updateRequestInfraPayload", payload)
			msoLogger.debug("updateRequestInfraPayload: " + payload)
			return execution.getVariable("FH_updateRequestInfraPayload")
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}
	
	public String updateRequestGammaPayload (DelegateExecution execution){
		def method = getClass().getSimpleName() + '.updateRequestGammaPayload(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		try {
			String payload = """
					<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="${UrnPropertiesReader.getVariable("mso.default.adapter.namespace", execution)}/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateRequest>
							<requestId>${MsoUtils.xmlEscape(execution.getVariable("FH_request_id"))}</requestId>
							<lastModifiedBy>BPEL</lastModifiedBy>
							<finalErrorMessage>${MsoUtils.xmlEscape(execution.getVariable("FH_ErrorMessage"))}</finalErrorMessage>
							<finalErrorCode>${MsoUtils.xmlEscape(execution.getVariable("FH_ErrorCode"))}</finalErrorCode>
							<status>FAILED</status>
						</req:updateRequest>
					</soapenv:Body>
					</soapenv:Envelope>
				"""
	
			execution.setVariable("FH_updateRequestGammaPayload", payload)
			msoLogger.debug("updateRequestGammaPayload: " + payload)
			return execution.getVariable("FH_updateRequestGammaPayload")
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}
	
	public String updateResponseStatusPayload (DelegateExecution execution){
		def method = getClass().getSimpleName() + '.updateResponseStatusPayload(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		try {
			String payload = """
					<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://org.onap.so/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateResponseStatus>
							<requestId>${MsoUtils.xmlEscape(execution.getVariable("FH_request_id"))}</requestId>
							<lastModifiedBy>BPEL</lastModifiedBy>
							<responseStatus>SENDING_FINAL_NOTIFY</responseStatus>
						</req:updateResponseStatus>
					</soapenv:Body>
					</soapenv:Envelope>
				"""
	
			execution.setVariable("FH_updateResponseStatusPayload", payload)
			msoLogger.debug("updateResponseStatusPayload: " + payload)
			return execution.getVariable("FH_updateResponseStatusPayload")
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}
	
	public void buildDBWorkflowException(DelegateExecution execution, String responseCodeVariable) {
		def method = getClass().getSimpleName() + '.buildDBWorkflowException(' +
			'execution=' + execution.getId() +
			', responseCodeVariable=' + responseCodeVariable + ')'
		msoLogger.trace('Entered ' + method)
	
		try {
			def responseCode = execution.getVariable(responseCodeVariable)
			// If the HTTP response code was null, it means a connection fault occurred (a java exception)
			def errorMessage = responseCode == null ? "Could not connect to DB Adapter" : "DB Adapter returned ${responseCode} response"
			def errorCode = responseCode == null ? 7000 : 7020
		//	exceptionUtil.buildWorkflowException(execution, errorCode, errorMessage)
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		//	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}
	}
	
	/**
	 * Used to create a workflow response in success and failure cases.
	 */
	public void postProcessResponse (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.postProcessResponse(' +'execution=' + execution.getId() +')'
		msoLogger.trace('Entered ' + method)
	
		try {
			Boolean success = (Boolean) execution.getVariable("FH_success")
			String out = success ? "Fallout Handler Succeeded" : "Fallout Handler Failed";
	
			String falloutHandlerResponse = """
					<workflow:FalloutHandlerResponse xmlns:workflow="http://org.onap/so/workflow/schema/v1">
					   <workflow:out>${MsoUtils.xmlEscape(out)}</workflow:out>
					</workflow:FalloutHandlerResponse>
				"""
	
			falloutHandlerResponse = utils.formatXml(falloutHandlerResponse)
			msoLogger.debug("FalloutHandler Response: " + falloutHandlerResponse);
	
			execution.setVariable("FalloutHandlerResponse", falloutHandlerResponse)
			execution.setVariable("WorkflowResponse", falloutHandlerResponse)
			execution.setVariable("FH_ResponseCode", success ? "200" : "500")
			setSuccessIndicator(execution, success)
	
			msoLogger.debug("FalloutHandlerResponse =\n" + falloutHandlerResponse)
		} catch (Exception e) {
			// Do NOT throw WorkflowException!
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
		}
	}
}