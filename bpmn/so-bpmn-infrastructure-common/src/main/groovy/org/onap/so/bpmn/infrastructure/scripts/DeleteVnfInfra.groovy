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

package org.onap.so.bpmn.infrastructure.scripts


import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.VidUtils;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils;
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * This class supports the DeleteVnfInfra Flow
 * with the Deletion of a generic vnf for
 * infrastructure.
 */
class DeleteVnfInfra extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger( DeleteVnfInfra.class);
	
	String Prefix="DELVI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		logger.trace("STARTED DeleteVnfInfra PreProcessRequest Process")

		execution.setVariable("DELVI_SuccessIndicator", false)
		execution.setVariable("DELVI_vnfInUse", false)

		try{
			// Get Variables
			String deleteVnfRequest = execution.getVariable("bpmnRequest")
			execution.setVariable("DELVI_DeleteVnfRequest", deleteVnfRequest)
			logger.debug("Incoming DeleteVnfInfra Request is: \n" + deleteVnfRequest)

			if(deleteVnfRequest != null){

				String requestId = execution.getVariable("mso-request-id")
				execution.setVariable("DELVI_requestId", requestId)

				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("DELVI_serviceInstanceId", serviceInstanceId)
				logger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

				String vnfId = execution.getVariable("vnfId")
				execution.setVariable("DELVI_vnfId", vnfId)
				logger.debug("Incoming Vnf(Instance) Id is: " + vnfId)

				String source = jsonUtil.getJsonValue(deleteVnfRequest, "requestDetails.requestInfo.source")
				execution.setVariable("DELVI_source", source)
				logger.debug("Incoming Source is: " + source)
				
				def cloudConfiguration = jsonUtil.getJsonValue(deleteVnfRequest, "requestDetails.cloudConfiguration")
				execution.setVariable("DELVI_cloudConfiguration", cloudConfiguration)
				
				boolean cascadeDelete = false
				Boolean cascadeDeleteObj = jsonUtil.getJsonRawValue(deleteVnfRequest, "requestDetails.requestParameters.cascadeDelete")
				if(cascadeDeleteObj!=null){
					cascadeDelete = cascadeDeleteObj.booleanValue()
				}
				execution.setVariable("DELVI_cascadeDelete", cascadeDelete)
				logger.debug("Incoming cascadeDelete is: " + cascadeDelete)

				//For Completion Handler & Fallout Handler
				String requestInfo =
				"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>DELETE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

				execution.setVariable("DELVI_requestInfo", requestInfo)

				// Setting for sub flow calls
				execution.setVariable("DELVI_type", "generic-vnf")

			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Bpmn Request is Null.")
			}

		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.debug(" Error Occured in DeleteVnfInfra PreProcessRequest method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteVnfInfra PreProcessRequest")

		}
		logger.trace("COMPLETED DeleteVnfInfra PreProcessRequest Process")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED DeleteVnfInfra SendSyncResponse Process")

		try {
			String requestId = execution.getVariable("DELVI_requestId")
			String vnfId = execution.getVariable("DELVI_vnfId")

			String DeleteVnfResponse = """{"requestReferences":{"instanceId":"${vnfId}","requestId":"${requestId}"}}""".trim()

			logger.debug("DeleteVnfInfra Sync Response is: \n"  + DeleteVnfResponse)
			execution.setVariable("DELVI_sentSyncResponse", true)

			sendWorkflowResponse(execution, 202, DeleteVnfResponse)

		} catch (Exception ex) {
			logger.debug("Error Occured in DeleteVnfInfra SendSyncResponse Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteVnfInfra SendSyncResponse Process")

		}
		logger.trace("COMPLETED DeleteVnfInfra SendSyncResponse Process")
	}

	public void prepareCompletionHandlerRequest(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED DeleteVnfInfra PrepareCompletionHandlerRequest Process")

		try {
			String requestInfo = execution.getVariable("DELVI_requestInfo")
			requestInfo = utils.removeXmlPreamble(requestInfo)
			String vnfId = execution.getVariable("DELVI_vnfId")

			String request =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
									xmlns:ns="http://org.onap/so/request/types/v1">
							${requestInfo}
							<status-message>Vnf has been deleted successfully.</status-message>
							<vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
							<mso-bpel-name>DeleteVnfInfra</mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			execution.setVariable("DELVI_completionHandlerRequest", request)
			logger.debug("Completion Handler Request is: " + request)

			execution.setVariable("WorkflowResponse", "Success") // for junits

		} catch (Exception ex) {
			logger.debug("Error Occured in DeleteVnfInfra PrepareCompletionHandlerRequest Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteVnfInfra PrepareCompletionHandlerRequest Process")

		}
		logger.trace("COMPLETED DeleteVnfInfra PrepareCompletionHandlerRequest Process")
	}

	public void sendErrorResponse(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED DeleteVnfInfra sendErrorResponse Process")
		try {
			def sentSyncResponse = execution.getVariable("DELVI_sentSyncResponse")
			if(sentSyncResponse == false){
				logger.debug("Sending a Sync Error Response")
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)

				logger.debug(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				logger.debug("A Sync Response has already been sent. Skipping Send Sync Error Response.")
			}

		} catch(Exception ex) {
			logger.debug("Error Occured in DeleteVnfInfra sendErrorResponse Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteVnfInfra sendErrorResponse Process")
		}
		logger.trace("COMPLETED DeleteVnfInfra sendErrorResponse Process")
	}



}
