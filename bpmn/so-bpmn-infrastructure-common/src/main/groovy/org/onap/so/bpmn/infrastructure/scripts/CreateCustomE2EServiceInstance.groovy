/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved. 
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
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.springframework.web.util.UriUtils

import groovy.json.*

/**
 * This groovy class supports the <class>CreateServiceInstance.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Create
 *
 */
public class CreateCustomE2EServiceInstance extends AbstractServiceTaskProcessor {
	String Prefix="CRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateCustomE2EServiceInstance.class);
	

	public void preProcessRequest (DelegateExecution execution) {
		msoLogger.trace("start preProcessRequest")
		execution.setVariable("prefix",Prefix)
		String msg = ""

		try {
			String siRequest = execution.getVariable("bpmnRequest")
			msoLogger.debug(siRequest)

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			msoLogger.debug("Input Request:" + siRequest + " reqId:" + requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)) {
				serviceInstanceId = UUID.randomUUID().toString()
			}
			msoLogger.debug("Generated new Service Instance:" + serviceInstanceId)
			serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceInstanceId)

			//subscriberInfo
			String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "requestDetails.subscriberInfo.globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}

			//requestInfo
			execution.setVariable("source", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.source"))
			execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName"))
			execution.setVariable("disableRollback", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.suppressRollback"))
			String productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
			if (isBlank(productFamilyId))
			{
				msg = "Input productFamilyId is null"
				msoLogger.debug(msg)
				//exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("productFamilyId", productFamilyId)
			}

			//modelInfo
			String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
			if (isBlank(serviceModelInfo)) {
				msg = "Input serviceModelInfo is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("serviceModelInfo", serviceModelInfo)
			}

			msoLogger.debug("modelInfo: " + serviceModelInfo)

			//requestParameters
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
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
			def userParamsList = reqMap.requestDetails?.requestParameters?.userParams

			Map<String, String> inputMap = [:]
			if (userParamsList) {
				for (def i=0; i<userParamsList.size(); i++) {
					def userParams1 = userParamsList.get(i)
					userParams1.each { param -> inputMap.put(param.key, param.value)}
				}
			}
			
			msoLogger.debug("User Input Parameters map: " + inputMap.toString())
			execution.setVariable("serviceInputParams", inputMap)
			execution.setVariable("uuiRequest", inputMap.get("UUIRequest"))

			//TODO
			//execution.setVariable("serviceInputParams", jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.userParams"))
			//execution.setVariable("failExists", true)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("finished preProcessRequest")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		msoLogger.trace("start sendSyncResponse")
		try {
			String operationId = execution.getVariable("operationId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			// RESTResponse for API Handler (APIH) Reply Task
			String createServiceRestRequest = """{"service":{"serviceId":"${serviceInstanceId}","operationId":"${operationId}"}}""".trim()
			msoLogger.debug(" sendSyncResponse to APIH:" + "\n" + createServiceRestRequest)
			sendWorkflowResponse(execution, 202, createServiceRestRequest)
			execution.setVariable("sentSyncResponse", true)
		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("finished sendSyncResponse")
	}


	public void sendSyncError (DelegateExecution execution) {
		msoLogger.trace("start sendSyncError")
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

			msoLogger.debug(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			msoLogger.debug("Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}
		msoLogger.trace("finished sendSyncError")
	}

	public void prepareCompletionRequest (DelegateExecution execution) {
		msoLogger.trace("start prepareCompletionRequest")
		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String source = execution.getVariable("source")
			
			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>CREATE</action>
							<source>${MsoUtils.xmlEscape(source)}</source>
			   			</request-info>
						<status-message>Service Instance was created successfully.</status-message>
						<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
			   			<mso-bpel-name>CreateGenericALaCarteServiceInstance</mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			execution.setVariable("completionRequest", xmlMsoCompletionRequest)
			msoLogger.debug("Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("finished prepareCompletionRequest")
	}

	public void prepareFalloutRequest(DelegateExecution execution){
		msoLogger.trace("start prepareFalloutRequest")
		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			msoLogger.debug("Input Workflow Exception: " + wfex.toString())
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String requestInfo =
					"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>CREATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			msoLogger.debug("Exception prepareFalloutRequest:" + ex.getMessage())
			String errorException = "  Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
					"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>CREATE</action>
					      <source>UUI</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorException)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable("falloutRequest", falloutRequest)
		}
		msoLogger.trace("finished prepareFalloutRequest")
	}
	
	/**
	 * Init the service Operation Status
	 */
	public void prepareInitServiceOperationStatus(DelegateExecution execution){
		msoLogger.trace("start prepareInitServiceOperationStatus")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = UUID.randomUUID().toString()
            String operationType = "CREATE"
            String userId = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            msoLogger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint",execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            msoLogger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

            String payload =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:initServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
            msoLogger.debug("Outgoing updateServiceOperStatusRequest: \n" + payload)
            msoLogger.debug("CreateVfModuleInfra Outgoing updateServiceOperStatusRequest Request: " + payload)

        }catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing prepareInitServiceOperationStatus.", "BPMN", MsoLogger.getServiceName(),
				MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareInitServiceOperationStatus Method:\n" + e.getMessage())
        }
		msoLogger.trace("finished prepareInitServiceOperationStatus")
	}
	
}
