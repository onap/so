/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved. 
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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.CompareModelsResult
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory




/**
 * This groovy class supports the <class>CompareModelofE2EServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId
 * @param - subscriptionServiceType
 * @param - serviceInstanceId
 * @param - modelInvariantIdTarget
 * @param - modelVersionIdTarget

 *
 * Outputs:
 * @param - WorkflowException
 */
public class CompareModelofE2EServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( CompareModelofE2EServiceInstance.class)

	String Prefix="CMPMDSI_"
	private static final String DebugFlag = "isDebugEnabled"
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()

	 void preProcessRequest (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		String msg = ""
		
		logger.trace("preProcessRequest Request ")
	
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

			//subscriberInfo
			String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				logger.info( msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}
	
			//subscriptionServiceType
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "serviceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				logger.debug( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("serviceType", subscriptionServiceType)
			}
	
			//modelInvariantIdTarget
			String modelInvariantIdTarget = jsonUtil.getJsonValue(siRequest, "modelInvariantIdTarget")
			if (isBlank(modelInvariantIdTarget)) {
				msg = "Input modelInvariantIdTarget' is null"
				logger.info( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("modelInvariantIdTarget", modelInvariantIdTarget)
			}
	
			//modelVersionIdTarget
			String modelVersionIdTarget = jsonUtil.getJsonValue(siRequest, "modelVersionIdTarget")
			if (isBlank(modelVersionIdTarget)) {
				msg = "Input modelVersionIdTarget is null"
				logger.debug( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("modelVersionIdTarget", modelVersionIdTarget)
				}

			execution.setVariable("operationType", "CompareModel") 
	
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest ")
	}

	 void sendSyncResponse (DelegateExecution execution) {
		logger.trace("sendSyncResponse  ")

		try {
			CompareModelsResult compareModelsResult = execution.getVariable("compareModelsResult")
			
			// RESTResponse (for API Handler(APIH) Reply Task)
			String syncResponse = compareModelsResult.toJsonStringNoRootName()
			logger.info(" sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String msg  = "Exception in sendSyncResponse: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit sendSyncResopnse ")
	}
	
	 void sendSyncError (DelegateExecution execution) {
		logger.trace("sendSyncError ")

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

			logger.debug(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			logger.info(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}

	}
	
	 void prepareCompletionRequest (DelegateExecution execution) {
		logger.trace("prepareCompletion ")

		try {
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>COMPAREMODEL</action>
							<source>${MsoUtils.xmlEscape(source)}</source>
			   			</request-info>
						<aetgt:status-message>E2E Service Instance Compare model successfully.</aetgt:status-message>
			   			<aetgt:mso-bpel-name>CompareModelofE2EServiceInstance</aetgt:mso-bpel-name>
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
		logger.trace("Exit prepareCompletionRequest ")
	}
	
	 void prepareFalloutRequest(DelegateExecution execution){
		logger.trace("prepareFalloutRequest ")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			logger.info(" Input Workflow Exception: " + wfex.toString())
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String requestInfo =
			"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>COMPAREMODEL</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			logger.info("Exception prepareFalloutRequest:" + ex.getMessage())
			String errorException = "  Bpmn error encountered in CompareModelofE2EServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>COMPAREMODEL</action>
					      <source>UUI</source>
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

}
	
