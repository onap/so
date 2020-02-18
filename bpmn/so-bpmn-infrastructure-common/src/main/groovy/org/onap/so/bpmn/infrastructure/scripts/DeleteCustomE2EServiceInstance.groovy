/*-

 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved. 
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.onap.so.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import groovy.json.*

/**
 * This groovy class supports the <class>DelE2EServiceInstance.bpmn</class> process.
 *
 */
public class DeleteCustomE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()
    private static final Logger logger = LoggerFactory.getLogger( DeleteCustomE2EServiceInstance.class)
	
	public void preProcessRequest (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		String msg = ""
		
		logger.info("Starting preProcessRequest")

		try {
			// check for incoming json message/input
			String siRequest = execution.getVariable("bpmnRequest")
			logger.debug(siRequest)
			
			
			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			logger.info("Input Request:" + siRequest + " reqId:" + requestId)
			
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)) {
				msg = "Input serviceInstanceId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
		
				
			//requestInfo
//			String productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
//			if (isBlank(productFamilyId))
//			{
//				msg = "Input productFamilyId is null"
//				logger.info( msg)
//				//exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
//			} else {
//				execution.setVariable("productFamilyId", productFamilyId)
//			}
			String source = jsonUtil.getJsonValue(siRequest, "source")
			execution.setVariable("source", source)
			
			//subscriberInfo
			String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				logger.info(msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}
			
			//requestParameters
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "serviceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				logger.debug(msg)
			} else {
				execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			}
			String operationId = jsonUtil.getJsonValue(siRequest, "operationId")
			execution.setVariable("operationId", operationId)
			
			execution.setVariable("operationType", "DELETE") 
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		logger.trace("Staring sendSyncResponse")

		try {
			String operationId = execution.getVariable("operationId")
			String syncResponse = """{"operationId":"${operationId}"}""".trim()
			logger.info("sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String msg  = "Exception in sendSyncResponse: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.trace("Exit sendSyncResopnse")
	}
	
	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info("Starting sendSyncError")

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
			logger.info("Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}

	}
	
	public void prepareCompletionRequest (DelegateExecution execution) {
		logger.trace("Starting prepareCompletion")

		try {
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>DELETE</action>
							<source>${MsoUtils.xmlEscape(source)}</source>
			   			</request-info>
						<aetgt:status-message>E2E Service Instance was deleted successfully.</aetgt:status-message>
			   			<aetgt:mso-bpel-name>DeleteCustomE2EServiceInstance</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			execution.setVariable("completionRequest", xmlMsoCompletionRequest)
			logger.info(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit prepareCompletionRequest")
	}
	
	public void prepareFalloutRequest(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.trace("Starting prepareFalloutRequest ")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			logger.info(" Input Workflow Exception: " + wfex.toString())
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
			logger.info("Exception prepareFalloutRequest:" + ex.getMessage())
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
		logger.trace("Exit prepareFalloutRequest")
	}
	

	// *******************************
	//     Build DB request Section
	// *******************************
	public void prepareDBRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			logger.info("Starting prepareDBRequest")

			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = "E2E Service Instance successfully deleted."

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
			String exceptionMessage = "Bpmn error encountered in DeleteCustomE2EServiceInstance flow. Unexpected Error from method prepareDBRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	// *******************************
	//     Build Error Section
	// *******************************
	public void prepareDBRequestError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.trace("Start prepareDBRequestError")

		try {
			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				statusMessage = wfe.getErrorMessage()

			} else {
				statusMessage = "Encountered Error during DeleteCustomE2EServiceInstance proccessing. "
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
			String exceptionMessage = "Bpmn error encountered in DeleteCustomE2EServiceInstance flow. Unexpected Error from method prepareDBRequestError() - " + ex.getMessage()
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	public void processJavaException(DelegateExecution execution) {
		//TODO:
	}

	/**
	 * Init the service Operation Status
	 */
	public void prepareInitServiceOperationStatus(DelegateExecution execution){
		logger.debug("======== STARTED prepareInitServiceOperationStatus Process ======== ")
		try{
			String serviceId = execution.getVariable("serviceInstanceId")
			String operationId = execution.getVariable("operationId")
			String userId = ""
			String result = "Processing"
			String progress = "0"
			String reason = ""
			String operationContent = "Prepare service deletion"
			logger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
			serviceId = UriUtils.encode(serviceId,"UTF-8")

			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)

			String payload =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>DELETE</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
			logger.debug("Outgoing updateServiceOperStatusRequest: \n" + payload)

		}catch(Exception e){
			logger.error("Exception Occured Processing prepareInitServiceOperationStatus. Exception is:\n" + e)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareInitServiceOperationStatus Method:\n" + e.getMessage())
		}
		logger.debug("======== COMPLETED prepareInitServiceOperationStatus Process ======== ")
	}
}
