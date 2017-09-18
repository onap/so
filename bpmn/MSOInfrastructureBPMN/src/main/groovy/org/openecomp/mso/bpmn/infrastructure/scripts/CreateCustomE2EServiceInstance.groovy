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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>CreateServiceInstance.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Create
 *
 */
public class CreateCustomE2EServiceInstance extends AbstractServiceTaskProcessor {
	String Prefix="CRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()

	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		utils.log("DEBUG", " *** preProcessRequest() *** ", isDebugEnabled)

		try {

			String siRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(siRequest)

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			utils.log("DEBUG", "Input Request:" + siRequest + " reqId:" + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)) {
				serviceInstanceId = UUID.randomUUID().toString()
			}
			utils.log("DEBUG", "Generated new Service Instance:" + serviceInstanceId, isDebugEnabled)
			serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceInstanceId)

			//parameters
			String p_domainHost = jsonUtil.getJsonValue(siRequest, "service.parameters.domainHost")
			if (isBlank(p_domainHost)) {
				msg = "Input parameters domainHost is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("p_domainHost", p_domainHost)
			}

			String p_nodeTemplateName = jsonUtil.getJsonValue(siRequest, "service.parameters.nodeTemplateName")
			if (isBlank(p_nodeTemplateName)) {
				msg = "Input parameters nodeTemplateName is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("p_nodeTemplateName", p_nodeTemplateName)
			}
			
			String p_nodeType = jsonUtil.getJsonValue(siRequest, "service.parameters.nodeType")
			if (isBlank(p_nodeType)) {
				msg = "Input parameters nodeType is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("p_nodeType", p_nodeType)
			}

			//segments
			def jsonSlurper = new JsonSlurper()
			//def jsonOutput = new JsonOutput()

			Map reqMap = jsonSlurper.parseText(siRequest)		
			
			def p_segments = reqMap.service?.parameters?.segments
			
			//List<Segment> segList = []
			//if (p_segments) {
			//    p_segments.each {
			//		p_segment -> segList.add(p_segment)
			//		//p_segment.domainHost
			//	}
			//}
			//execution.setVariable("segments", segList)
			
			//location Constraints
			def p_locationConstraints = reqMap.service?.parameters?.nsParameters?.locationConstraints
			if(p_locationConstraints){
			//Copy data. no data available now so ignoring
			}
			
			//additionalParamForNs
			String p_param1 = jsonUtil.getJsonValue(siRequest, "service.parameters.nsParameters.additionalParamForNs.E2EServcie.param1")
			if (isBlank(p_param1)) {
				msg = "Input parameters p_param1 is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("p_param1", p_param1)
			}
			
			String p_param2 = jsonUtil.getJsonValue(siRequest, "service.parameters.nsParameters.additionalParamForNs.E2EServcie.param2")
			if (isBlank(p_param2)) {
				msg = "Input parameters p_param2 is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("p_param2", p_param2)
			}

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
				utils.log("DEBUG", msg, isDebugEnabled)
				//exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("productFamilyId", productFamilyId)
			}

			//modelInfo
			String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
			if (isBlank(serviceModelInfo)) {
				msg = "Input serviceModelInfo is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("serviceModelInfo", serviceModelInfo)
			}

			utils.log("DEBUG", "modelInfo" + serviceModelInfo,  isDebugEnabled)

			//requestParameters
			String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			}

			//TODO
			//execution.setVariable("serviceInputParams", jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.userParams"))
			//execution.setVariable("failExists", true)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** sendSyncResponse *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			// RESTResponse for API Handler (APIH) Reply Task
			String createServiceRestRequest = """{"service":{"serviceId":"${serviceInstanceId}","operationId":"${requestId}"}}""".trim()
			utils.log("DEBUG", " sendSyncResponse to APIH:" + "\n" + createServiceRestRequest, isDebugEnabled)
			sendWorkflowResponse(execution, 202, createServiceRestRequest)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
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
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String source = execution.getVariable("source")
			
			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
								xmlns:ns="http://org.openecomp/mso/request/types/v1">
						<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
							<request-id>${requestId}</request-id>
							<action>CREATE</action>
							<source>${source}</source>
			   			</request-info>
						<status-message>Service Instance was created successfully.</status-message>
						<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
			   			<mso-bpel-name>CreateGenericALaCarteServiceInstance</mso-bpel-name>
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
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			utils.log("DEBUG", "Exception prepareFalloutRequest:" + ex.getMessage(), isDebugEnabled)
			String errorException = "  Bpmn error encountered in CreateGenericALaCarteServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
					"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>CREATE</action>
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
}