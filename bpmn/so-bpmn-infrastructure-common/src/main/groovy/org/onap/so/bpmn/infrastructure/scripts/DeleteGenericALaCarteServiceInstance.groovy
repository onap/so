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

package org.onap.so.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.*

/**
 * This groovy class supports the <class>DelServiceInstance.bpmn</class> process.
 *
 */
public class DeleteGenericALaCarteServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()
	
    private static final Logger logger = LoggerFactory.getLogger( DeleteGenericALaCarteServiceInstance.class);
	
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		
		logger.trace("Start preProcessRequest")

		try {
			// check for incoming json message/input
			String siRequest = execution.getVariable("bpmnRequest")
			
			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			logger.debug("Input Request:" + siRequest + " reqId:" + requestId)
			
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
				logger.debug(msg)
			} else
			{
				execution.setVariable("serviceModelInfo", serviceModelInfo)
				//logger.debug("modelInfo" + serviceModelInfo)
			}
			
			//requestInfo
			String productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
			if (isBlank(productFamilyId))
			{
				msg = "Input productFamilyId is null"
				logger.debug(msg)
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
				logger.debug(msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}
			
			//requestParameters
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				logger.debug(msg)
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
					userParam -> inputMap.put(userParam.name, userParam.value.toString())
				}
			}
			
			logger.debug("User Input Parameters map: " + userParams.toString())
			execution.setVariable("serviceInputParams", inputMap)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.trace("Start sendSyncResponse")

		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()
			logger.debug(" sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String msg  = "Exception in sendSyncResponse: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.trace("Exit sendSyncResopnse")
	}
	
	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.trace("Start sendSyncError")

		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
				"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			logger.info(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			logger.debug(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}

	}
	
	public void prepareCompletionRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.trace("Start prepareCompletion")

		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String source = execution.getVariable("source")
			String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>DELETE</action>
							<source>${MsoUtils.xmlEscape(source)}</source>
			   			</request-info>
						<status-message>Service Instance was deleted successfully.</status-message>
						<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
			   			<mso-bpel-name>DeleteGenericALaCarteServiceInstance</mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			execution.setVariable("completionRequest", xmlMsoCompletionRequest)
			logger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit prepareCompletionRequest")
	}
	
	public void prepareFalloutRequest(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.trace("Start prepareFalloutRequest")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			logger.debug(" Input Workflow Exception: " + wfex.toString())
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String requestInfo =
			"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>DELETE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			logger.debug("Exception prepareFalloutRequest:" + ex.getMessage())
			String errorException = "  Bpmn error encountered in CreateServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>DELETE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorException)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable("falloutRequest", falloutRequest)
		}
		logger.trace("Exit prepareFalloutRequest ")
	}
	

	// *******************************
	//     Build DB request Section
	// *******************************
	public void prepareDBRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			logger.trace("Inside prepareDBRequest of DeleteGenericALaCarteServiceInstance ")

			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = "Service Instance successfully deleted."

			//TODO - verify the format for Service Instance Delete,
			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
								<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${MsoUtils.xmlEscape(statusMessage)}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>COMPLETED</requestStatus>
								<progress>100</progress>
							</ns:updateInfraRequest>
						   </soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("DELSI_createDBRequest", buildDeleteDBRequestAsString)
		   logger.info(buildDeleteDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteGenericALaCarteServiceInstance flow. Unexpected Error from method prepareDBRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	// *******************************
	//     Build Error Section
	// *******************************
	public void prepareDBRequestError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.trace("Inside prepareDBRequestError of DeleteGenericALaCarteServiceInstance ")

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
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
								<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${MsoUtils.xmlEscape(statusMessage)}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
							</ns:updateInfraRequest>
						   </soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("DELSI_createDBInfraErrorRequest", buildDBRequestAsString)
		   logger.info(buildDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteGenericALaCarteServiceInstance flow. Unexpected Error from method prepareDBRequestError() - " + ex.getMessage()
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }
}
