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

import java.text.SimpleDateFormat

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution


public class CompleteMsoProcess extends AbstractServiceTaskProcessor {

	String Prefix="CMSO_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	// Complete MSO Request processing
	public initializeProcessVariables(Execution execution){

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {

			/* Initialize all the process request variables in this block */
			execution.setVariable("prefix",Prefix)
			//execution.setVariable("getLayer3ServiceDetailsV1Response","")
			execution.setVariable("CMSO_request_id","")
			execution.setVariable("CMSO_notification-url","")
			execution.setVariable("CMSO_mso-bpel-name","")
			execution.setVariable("CMSO_request_action","")

			execution.setVariable("CMSO_notification-url-Ok", false)
			execution.setVariable("CMSO_request_id-Ok", false)

			//These variabels are for Get Mso Aai Password Adapter
			execution.setVariable("CMSO_deliveryStatus", false)

			//updateRequest Adapter process variables
			execution.setVariable("CMSO_updateRequestResponse", "")
			execution.setVariable("CMSO_updateRequestResponseCode", "")
			execution.setVariable("CMSO_updateFinalNotifyAckStatusSuccessPayload", "")
			execution.setVariable("CMSO_updateFinalNotifyAckStatusFailedPayload", "")
			execution.setVariable("CMSO_gSendManagedNetworkStatusNotificationResponse", "")

			//Set DB adapter variables here
			execution.setVariable("CMSO_updateFinalStatusSuccessPayload", "")
			execution.setVariable("CMSO_updateDBStatusToSuccessPayload", "")
			execution.setVariable("CMSO_updateInfraRequestDBPayload", "")
			execution.setVariable("CMSO_setUpdateDBstatustoSuccessPayload", "")
			execution.setVariable("CMSO_setUpdateFinalNotifyAckStatusPayload", "")

			//NotifyOMXSuccessFailureViaCSI Adapter variables
			execution.setVariable("CMSO_notifyOMXSuccessFailureViaCSIPayload", "")
			execution.setVariable("CMSO_notifyOMXSuccessFailureViaCSIResponseCode", null)
			execution.setVariable("CMSO_notifyOMXSuccessFailureViaCSIResponse", "")

			//Auth variables
			execution.setVariable("BasicAuthHeaderValue","")

			//Response variables
			execution.setVariable("CompletionHandlerResponse","")
			execution.setVariable("CMSO_ErrorResponse", null)
			execution.setVariable("CMSO_ResponseCode", "")

			setSuccessIndicator(execution, false)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

	}

	public void preProcessRequest (Execution execution) {

		initializeProcessVariables(execution)
		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		utils.log("DEBUG", "*** Started CompleteMsoProcess preProcessRequest Method ***", isDebugLogEnabled);
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {
			def xml = execution.getVariable("CompleteMsoProcessRequest")

			utils.logAudit("CompleteMsoProcess Request: " + xml)
			utils.log("DEBUG", "Incoming Request is: "+ xml, isDebugLogEnabled)

			//mso-bpel-name from the incoming request
			def msoBpelName = utils.getNodeText1(xml,"mso-bpel-name")
			execution.setVariable("CMSO_mso-bpel-name",msoBpelName)

			//Check the incoming request type
			//Incoming request can be ACTIVE_REQUESTS (request-information node) or  INFRA_ACTIVE_REQUESTS (request-info node)
			if (utils.nodeExists(xml, "request-information")) {
				execution.setVariable("CMSO_request_id-Ok", true) // Incoming request is for ACTIVE_REQUESTS
			}

			//Check notification-url for the incoming request type
			//ACTIVE_REQUESTS may have notificationurl node
			//INFRA_ACTIVE_REQUESTS notificationurl node does not exist
			def notificationurl = ""
			if (utils.nodeExists(xml, "notification-url")) {
				notificationurl = utils.getNodeText(xml,"notification-url")
				if(notificationurl != null && !notificationurl.isEmpty()) {
					execution.setVariable("CMSO_notification-url-Ok", true)
					execution.setVariable("CMSO_notification-url",notificationurl)
				}
			}

			//Check request_id for the incoming request type
			//For INFRA_ACTIVE_REQUESTS payload request-id IS optional (Not sure why this is option since req id is primary key ... also tried exe through SOAP UI to check if MSO code handles null like auto generated seq not it does not)
			//For ACTIVE_REQUESTS payload request-id is NOT optional
			def request_id = ""
			if (utils.nodeExists(xml, "request-id")) {
				execution.setVariable("CMSO_request_id",utils.getNodeText(xml,"request-id"))
			}


			// INFRA_ACTIVE_REQUESTS	 have "action" element ... mandatory
			// ACTIVE_REQUEST have "request-action" ... mandatory
			if (utils.nodeExists(xml, "request-action")) {
				execution.setVariable("CMSO_request_action",utils.getNodeText(xml,"request-action"))
			} else if (utils.nodeExists(xml, "action")) {
				execution.setVariable("CMSO_request_action",utils.getNodeText(xml,"action"))
			}

			//Check source for the incoming request type
			//For INFRA_ACTIVE_REQUESTS payload source IS optional
			//For ACTIVE_REQUESTS payload source is NOT optional
			def source = ""
			if (utils.nodeExists(xml, "source")) {
				execution.setVariable("CMSO_source",utils.getNodeText(xml,"source"))
			}

			utils.log("DEBUG", "CMSO_notification-url-Ok --> " + execution.getVariable("CMSO_notification-url-Ok"), isDebugLogEnabled)
			utils.log("DEBUG", "CMSO_request_id-Ok --> " + execution.getVariable("CMSO_request_id-Ok"), isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			utils.log("DEBUG", "Exception Occured During PreProcessRequest: " + e, isDebugLogEnabled);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in " + method)
		}

		utils.log("DEBUG", "*** Completed CompleteMsoProcess preProcessRequest Method ***", isDebugLogEnabled);
	}

	public void postProcessResponse (Execution execution) {

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		utils.log("DEBUG", "*** Started CompleteMsoProcess PostProcessRequest Method ***", isDebugLogEnabled);
		try {

			def msoCompletionResponse = """
			<sdncadapterworkflow:MsoCompletionResponse xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1">
			   <sdncadapterworkflow:out>BPEL ${execution.getVariable("CMSO_mso-bpel-name")} completed</sdncadapterworkflow:out>
			</sdncadapterworkflow:MsoCompletionResponse>
			""".trim()

			// Format Response
			def xmlMsoCompletionResponse = utils.formatXML(msoCompletionResponse)
			String buildMsoCompletionResponseAsString = xmlMsoCompletionResponse.drop(38).trim()
			// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
			execution.setVariable("WorkflowResponse", buildMsoCompletionResponseAsString)
			utils.logAudit("CompleteMsoProcess Response: " + buildMsoCompletionResponseAsString)
			execution.setVariable("CompleteMsoProcessResponse", buildMsoCompletionResponseAsString)
			execution.setVariable("CMSO_ResponseCode", "200")

			setSuccessIndicator(execution, true)

			utils.log("DEBUG", "@@ CompleteMsoProcess Response @@ " + "\n" + execution.getVariable("CompleteMsoProcessResponse"), isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
		utils.log("DEBUG", "*** Completed CompleteMsoProcess PostProcessRequest Method ***", isDebugLogEnabled);

	}

	public void updateFinalNotifyAckStatusSuccessPayload (Execution execution){
		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {

			def deliveryStatus = execution.getVariable("CMSO_deliveryStatus")
			String responseStatus = ""
			String responseCode = ""
			String responseBodyXml = ""

			if(deliveryStatus == true){
				responseStatus = "SUCCESS"
				responseCode = "200"
				responseBodyXml = """<responseBody>${execution.getVariable("CMSO_gSendManagedNetworkStatusNotificationResponse")}</responseBody>"""
			}else{
				responseStatus = "FAILED"
				responseCode = execution.getVariable("CCDStatusCode")
			}

			String payload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://com.att.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			   <req:updateRequest>
				  <requestId>${execution.getVariable("CMSO_request_id")}</requestId>
				  <lastModifiedBy>BPEL</lastModifiedBy>
				  <responseStatus>${responseStatus}</responseStatus>
				  <responseCode>${responseCode}</responseCode>
				  ${responseBodyXml}
			   </req:updateRequest>
			</soapenv:Body>
		 </soapenv:Envelope>"""

			execution.setVariable("CMSO_updateFinalNotifyAckStatusSuccessPayload", payload)
			utils.logAudit("updateFinalNotifyAckStatusPayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
	}


	public String updateFinalStatusSuccessPayload (Execution execution){

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {
			String payload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://com.att.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			   <req:updateRequest>
				  <requestId>${execution.getVariable("CMSO_request_id")}</requestId>
				  <lastModifiedBy>BPEL</lastModifiedBy>
				  <responseStatus>SUCCESS</responseStatus>
			   </req:updateRequest>
			</soapenv:Body>
		 </soapenv:Envelope>
		"""
			execution.setVariable("CMSO_updateFinalStatusSuccessPayload", payload)
			utils.logAudit("updateFinalStatusSuccessPayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)
			//println("CMSO_updateFinalStatusSuccessPayload --> " + execution.getVariable("CMSO_updateFinalStatusSuccessPayload"))

			return execution.getVariable("CMSO_updateFinalStatusSuccessPayload")

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
	}

	public void updateDBStatusToSuccessPayload (Execution execution){
		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {

			String payload = """
			<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://com.att.mso/requestsdb">
			<soapenv:Header/>
			<soapenv:Body>
			   <req:updateStatus>
				  <requestId>${execution.getVariable("CMSO_request_id")}</requestId>
				  <lastModifiedBy>BPEL</lastModifiedBy>
				  <status>COMPLETED</status>
			   </req:updateStatus>
			</soapenv:Body>
		 </soapenv:Envelope>
		"""
			execution.setVariable("CMSO_updateDBStatusToSuccessPayload", payload)
			utils.logAudit("updateDBStatusToSuccessPayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)
			//println("CMSO_updateDBStatusToSuccessPayload --> " + execution.getVariable("CMSO_updateDBStatusToSuccessPayload"))

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
	}

	public void setUpdateDBstatustoSuccessPayload (Execution execution){

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {

			def xml = execution.getVariable("CompleteMsoProcessRequest")

			//Get statusMessage if exists
			def statusMessage
			if(utils.nodeExists(xml, "status-message")){
				statusMessage = utils.getNodeText1(xml, "status-message")
			}else{
				statusMessage = "Resource Completed Successfully"
			}

			//Get instance Id if exist
			String idXml = ""
			if(utils.nodeExists(xml, "vnfId")){
				idXml = utils.getNodeXml(xml, "vnfId")
			}else if(utils.nodeExists(xml, "networkId")){
				idXml = utils.getNodeXml(xml, "networkId")
			}else if(utils.nodeExists(xml, "serviceInstanceId")){
				idXml = utils.getNodeXml(xml, "serviceInstanceId")
			}else if(utils.nodeExists(xml, "vfModuleId")){
				idXml = utils.getNodeXml(xml, "vfModuleId")
			}else if(utils.nodeExists(xml, "volumeGroupId")){
				idXml = utils.getNodeXml(xml, "volumeGroupId")
			}else{
				idXml = ""
			}
			idXml = utils.removeXmlPreamble(idXml)
			utils.log("DEBUG", "Incoming Instance Id Xml: " + idXml, isDebugLogEnabled)

			String payload = """
						<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://com.att.mso/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateInfraRequest>
						         <requestId>${execution.getVariable("CMSO_request_id")}</requestId>
						         <lastModifiedBy>${execution.getVariable("CMSO_mso-bpel-name")}</lastModifiedBy>
						         <statusMessage>${statusMessage}</statusMessage>
						         <requestStatus>COMPLETE</requestStatus>
								 <progress>100</progress>
								 ${idXml}
						      </req:updateInfraRequest>
						   </soapenv:Body>
						</soapenv:Envelope>"""

			execution.setVariable("CMSO_setUpdateDBstatustoSuccessPayload", payload)
			utils.log("DEBUG", "Outgoing Update Mso Request Payload is: " + payload, isDebugLogEnabled)
			utils.logAudit("setUpdateDBstatustoSuccessPayload: " + payload)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}
		logDebug('Exited ' + method, isDebugLogEnabled)
	}

	public String updateFinalNotifyAckStatusPayload (Execution execution){

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {
			String payload = """
						<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:req="http://com.att.mso/requestsdb">
						   <soapenv:Header/>
						   <soapenv:Body>
						      <req:updateResponseStatus>
						         <requestId>${execution.getVariable("CMSO_request_id")}</requestId>
						         <lastModifiedBy>BPEL</lastModifiedBy>
						         <responseStatus>SENDING_FINAL_NOTIFY</responseStatus>
						      </req:updateResponseStatus>
						   </soapenv:Body>
						</soapenv:Envelope>
		"""
			execution.setVariable("CMSO_setUpdateFinalNotifyAckStatusPayload", payload)
			utils.logAudit("updateFinalNotifyAckStatusPayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)

			return execution.getVariable("CMSO_setUpdateFinalNotifyAckStatusPayload")

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

	}

	//	public void decryptMsoPassword (Execution execution) {
	//
	//		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
	//		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	//		logDebug('Entered ' + method, isDebugLogEnabled)
	//		try {
	//			def encryptedPwd=execution.getVariable("URN_mso_csi_pwd")
	//			def msoKey = execution.getVariable("URN_mso_msoKey")
	//
	//			String basicAuthValue = utils.getBasicAuth(encryptedPwd, msoKey)
	//			execution.setVariable("BasicAuthHeaderValueCSI",basicAuthValue)
	//			logDebug('Exited ' + method, isDebugLogEnabled)
	//		} catch (BpmnError e) {
	//			throw e;
	//		} catch (IOException e) {
	//			logError('Caught exception in ' + method, e)
	//			workflowException(execution, 'Internal Error', 2000)
	//		}
	//	}

	public String notifyOMXSuccessFailureViaCSIPayload (Execution execution) {

		return "";
	}

	public String notifyCCDSuccessPayload (Execution execution) {

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {

			String payload ="""
		<ns:status-notification xmlns:ns="http://ecomp.att.com/mso/statusnotification/schema/v1" xmlns:msoservtypes="http://ecomp.att.com/mso/request/types/v1">
				<msoservtypes:request-id>${execution.getVariable("CMSO_request_id")}</msoservtypes:request-id>
				<msoservtypes:request-action>${execution.getVariable("CMSO_request_action")}</msoservtypes:request-action>
				<msoservtypes:source>${execution.getVariable("CMSO_source")}</msoservtypes:source>
				<msoservtypes:ack-final-indicator>Y</msoservtypes:ack-final-indicator>
		</ns:status-notification>
		"""
			execution.setVariable("CMSO_notifyCCDSuccessPayload", payload)
			utils.logAudit("notifyCCDSuccessPayload: " + payload)
			logDebug('Exited ' + method, isDebugLogEnabled)

			return execution.getVariable("CMSO_notifyCCDSuccessPayload")

		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}


	}

	public void buildDataError (Execution execution, String message) {

		def method = getClass().getSimpleName() + '.sendResponse(' +'execution=' + execution.getId() +')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		try {

			def msoCompletionResponse = """
			<sdncadapterworkflow:MsoCompletionResponse xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1">
			   <sdncadapterworkflow:out>BPEL ${execution.getVariable("CMSO_mso-bpel-name")} FAILED</sdncadapterworkflow:out>
			</sdncadapterworkflow:MsoCompletionResponse>
			""".trim()

			// Format Response
			def xmlMsoCompletionResponse = utils.formatXml(msoCompletionResponse)
			String buildMsoCompletionResponseAsString = xmlMsoCompletionResponse.drop(38).trim()
			utils.logAudit("CompleteMsoProcess Response: " + buildMsoCompletionResponseAsString)
			execution.setVariable("CompleteMsoProcessResponse", buildMsoCompletionResponseAsString)
			utils.log("DEBUG", "@@ CompleteMsoProcess Response @@ " + "\n" + execution.getVariable("CompletionHandlerResponse"), isDebugLogEnabled)

			exceptionUtil.buildAndThrowWorkflowException(execution, 500, message)

		} catch (BpmnError e) {
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugLogEnabled)
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
		}

	}

}
