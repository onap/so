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
package org.openecomp.mso.bpmn.infrastructure.scripts

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse

import java.util.UUID
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import static org.apache.commons.lang3.StringUtils.*

import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>DelServiceInstance.bpmn</class> process.
 *
 */
public class DeleteGenericALaCarteServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()
	
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		
		utils.log("DEBUG", " *** preProcessRequest Request *** ", isDebugEnabled)

		try {
			// check for incoming json message/input
			String siRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(siRequest)
			

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			utils.log("DEBUG", "Input Request:" + siRequest + " reqId:" + requestId, isDebugEnabled)
			
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)) {
				msg = "Input serviceInstanceId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
		
			//String xmlRequestDetails = vidUtils.getJsonRequestDetailstoXml(siRequest)
			//execution.setVariable("requestDetails", xmlRequestDetails)
			
			//modelInfo
			String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
			if (isBlank(serviceModelInfo)) {
				msg = "Input serviceModelInfo is null"
				utils.log("DEBUG", msg, isDebugEnabled)
			} else
			{
				execution.setVariable("serviceModelInfo", serviceModelInfo)
				//utils.log("DEBUG", "modelInfo" + serviceModelInfo,  isDebugEnabled)
			}
			
			//requestInfo
			String productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
			if (isBlank(productFamilyId))
			{
				msg = "Input productFamilyId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				//exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("productFamilyId", productFamilyId)
			}
			String source = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.source")
			execution.setVariable("source", source)
			
			//subscriberInfo
			String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "requestDetails.subscriberInfo.globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				utils.log("DEBUG", msg, isDebugEnabled)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}
			
			//requestParameters
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				//exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			}
			
			/*
			 * Extracting User Parameters from incoming Request and converting into a Map
			 */
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()

			Map reqMap = jsonSlurper.parseText(siRequest)

			//InputParams
			def userParams = reqMap.requestDetails?.requestParameters?.userParams

			Map<String, String> inputMap = [:]
			if (userParams) {
				userParams.each {
					userParam -> inputMap.put(userParam.name, userParam.value)
				}
			}
			
			utils.log("DEBUG", "User Input Parameters map: " + userParams.toString(), isDebugEnabled)
			execution.setVariable("serviceInputParams", inputMap)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncResponse  *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()
			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String msg  = "Exception in sendSyncResponse: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		utils.log("DEBUG"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}
	
	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncError *** ", isDebugEnabled)

		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
				"""<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			utils.logAudit(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			utils.log("DEBUG", " Sending Sync Error Activity Failed. " + "\n" + ex.getMessage(), isDebugEnabled)
		}

	}
	
	public void prepareCompletionRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** prepareCompletion *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
								xmlns:ns="http://org.openecomp/mso/request/types/v1">
						<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
							<request-id>${requestId}</request-id>
							<action>DELETE</action>
							<source>${source}</source>
			   			</request-info>
						<aetgt:status-message>Service Instance was deleted successfully.</aetgt:status-message>
			   			<aetgt:mso-bpel-name>DeleteGenericALaCarteServiceInstance</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			execution.setVariable("completionRequest", xmlMsoCompletionRequest)
			utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG", "*** Exit prepareCompletionRequest ***", isDebugEnabled)
	}
	
	public void prepareFalloutRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** prepareFalloutRequest *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Input Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>DELETE</action>
					<source>${source}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			utils.log("DEBUG", "Exception prepareFalloutRequest:" + ex.getMessage(), isDebugEnabled)
			String errorException = "  Bpmn error encountered in CreateServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>DELETE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorException}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable("falloutRequest", falloutRequest)
		}
		utils.log("DEBUG", "*** Exit prepareFalloutRequest ***", isDebugEnabled)
	}
	

	// *******************************
	//     Build DB request Section
	// *******************************
	public void prepareDBRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequest of DeleteGenericALaCarteServiceInstance ***** ", isDebugEnabled)

			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = "Service Instance successfully deleted."

			//TODO - verify the format for Service Instance Delete,
			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>COMPLETED</requestStatus>
								<progress>100</progress>
							</ns:updateInfraRequest>
						   </soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("DELSI_createDBRequest", buildDeleteDBRequestAsString)
		   utils.logAudit(buildDeleteDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteGenericALaCarteServiceInstance flow. Unexpected Error from method prepareDBRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	// *******************************
	//     Build Error Section
	// *******************************
	public void prepareDBRequestError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareDBRequestError of DeleteGenericALaCarteServiceInstance ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				statusMessage = wfe.getErrorMessage()

			} else {
				statusMessage = "Encountered Error during DeleteGenericALaCarteServiceInstance proccessing. "
			}

			//TODO - verify the format for Service Instance Create,
			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
							</ns:updateInfraRequest>
						   </soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("DELSI_createDBInfraErrorRequest", buildDBRequestAsString)
		   utils.logAudit(buildDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteGenericALaCarteServiceInstance flow. Unexpected Error from method prepareDBRequestError() - " + ex.getMessage()
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }
}
