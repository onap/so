/*
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

package org.onap.so.bpmn.vcpe.scripts

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AllottedResourceUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoDeleteAllottedResourceTXC.bpmn</class> process.
 *
 * @author
 * 
 * Inputs:
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 * @param - disableRollback - O ignored
 * @param - failNotfound  - O 
 * @param - serviceInstanceId
 * @param - globalCustomerId - O
 * @param - subscriptionServiceType - O
 * @param - parentServiceInstanceId
 * @param - allottedResourceId 
 *
 * Outputs:
 * @param - rollbackData - N/A
 * @param - rolledBack - true if no deletions performed
 * @param - WorkflowException - O
 * @param - wasDeleted - O (ie not silentSuccess)
 *
 */
public class DoDeleteAllottedResourceTXC extends AbstractServiceTaskProcessor{
	private static final Logger logger = LoggerFactory.getLogger(DoDeleteAllottedResourceTXC.class);

	String Prefix="DDARTXC_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest (DelegateExecution execution) {

		String msg = ""
		logger.trace("start preProcessRequest")

		try {
			execution.setVariable("prefix", Prefix)

			//Config Inputs
			String sdncCallbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "mso.workflow.sdncadapter.callback is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			logger.debug("SDNC Callback URL: " + sdncCallbackUrl)

			//Request Inputs
			if (isBlank(execution.getVariable("serviceInstanceId"))){
				msg = "Input serviceInstanceId is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceId"))){
				msg = "Input allottedResourceId is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end preProcessRequest")
	}

	public void getAaiAR (DelegateExecution execution) {

		logger.trace("start getAaiAR")

		String allottedResourceId = execution.getVariable("allottedResourceId")

		AllottedResourceUtils arUtils = getAllottedResourceUtils()
		boolean ifExistsAR = arUtils.ifExistsAR(execution, allottedResourceId)

		String errorMsg = ""
		if (ifExistsAR){
			String aaiARPath = execution.getVariable("aaiARPath")
			String parentServiceInstanceId = arUtils.getPSIFmARLink(execution, aaiARPath)
			execution.setVariable("parentServiceInstanceId", parentServiceInstanceId)
		}
		else{
			errorMsg = "Allotted resource not found in AAI with AllottedResourceId:" + allottedResourceId
		}
		if (!isBlank(errorMsg)) {
			logger.debug(errorMsg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, errorMsg)
		}
		logger.trace("end getAaiAR")

	}

	public AllottedResourceUtils getAllottedResourceUtils(){
		return new AllottedResourceUtils(this)
	}

	// aaiARPath set during query (existing AR)
	public void updateAaiAROrchStatus(DelegateExecution execution, String status){
		logger.trace("start updateAaiAROrchStatus")
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String aaiARPath = execution.getVariable("aaiARPath") //set during query (existing AR) 
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		logger.trace("end updateAaiAROrchStatus")
	}

	public String buildSDNCRequest(DelegateExecution execution, String action, String sdncRequestId) {

		String msg = ""
		logger.trace("start buildSDNCRequest")
		String sdncReq = null

		try {

			String allottedResourceId = execution.getVariable("allottedResourceId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String parentServiceInstanceId = execution.getVariable("parentServiceInstanceId")
			String globalCustomerId = execution.getVariable("globalCustomerId")
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

			String callbackUrl = execution.getVariable("sdncCallbackUrl")
			String requestId = execution.getVariable("msoRequestId")
			String modelInvariantId = ""
			String modelVersion = ""
			String modelUUId = ""
			String modelCustomizationId = ""
			String modelName = ""


			sdncReq =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${MsoUtils.xmlEscape(sdncRequestId)}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>tunnelxconn-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						<request-action>DeleteTunnelXConnInstance</request-action>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
					</request-information>
					<service-information>
						<service-id></service-id>
						<subscription-service-type>${MsoUtils.xmlEscape(subscriptionServiceType)}</subscription-service-type>
						<onap-model-information></onap-model-information>
						<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${MsoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
					</service-information>
					<allotted-resource-information>
						<allotted-resource-id>${MsoUtils.xmlEscape(allottedResourceId)}</allotted-resource-id>    
						<allotted-resource-type>tunnelxconn</allotted-resource-type>
						<parent-service-instance-id>${MsoUtils.xmlEscape(parentServiceInstanceId)}</parent-service-instance-id>   
						<onap-model-information>
							<model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantId)}</model-invariant-uuid>
							<model-uuid>${MsoUtils.xmlEscape(modelUUId)}</model-uuid>
							<model-customization-uuid>${MsoUtils.xmlEscape(modelCustomizationId)}</model-customization-uuid>
							<model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
							<model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
						</onap-model-information>
					</allotted-resource-information>
					<tunnelxconn-request-input>
					</tunnelxconn-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			logger.debug("sdncRequest:\n" + sdncReq)
			sdncReq = utils.formatXml(sdncReq)

		} catch(Exception ex) {
			msg = "Exception in buildSDNCRequest. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end buildSDNCRequest")
		return sdncReq
	}

	public void preProcessSDNCUnassign(DelegateExecution execution) {

		String msg = ""
		logger.trace("start preProcessSDNCUnassign")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncUnassignReq = buildSDNCRequest(execution, "unassign", sdncRequestId)
			execution.setVariable("sdncUnassignRequest", sdncUnassignReq)
			logger.debug("sdncUnassignRequest:  " + sdncUnassignReq)
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCUnassign. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end preProcessSDNCUnassign")
	}

	public void preProcessSDNCDelete(DelegateExecution execution) {

		String msg = ""
		logger.trace("start preProcessSDNCDelete")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncDeleteReq = buildSDNCRequest(execution, "delete", sdncRequestId)
			execution.setVariable("sdncDeleteRequest", sdncDeleteReq)
			logger.debug("sdncDeleteReq:  " + sdncDeleteReq)
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end preProcessSDNCDelete")
	}

	public void preProcessSDNCDeactivate(DelegateExecution execution) {

		String msg = ""
		logger.trace("start preProcessSDNCDeactivate")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncDeactivateReq = buildSDNCRequest(execution, "deactivate", sdncRequestId)
			execution.setVariable("sdncDeactivateRequest", sdncDeactivateReq)
			logger.debug("sdncDeactivateReq:  " + sdncDeactivateReq)
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDeactivate. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end preProcessSDNCDeactivate")
	}

	public void validateSDNCResp(DelegateExecution execution, String response, String method){

		logger.trace("start ValidateSDNCResponse Process")
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			logger.debug("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			logger.debug("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n"
						+ response)
			}else{
				String sdncRespCode = execution.getVariable(Prefix + 'sdncRequestDataResponseCode')
				logger.debug(method + " AllottedResource received error response from SDNC. ResponseCode:"
						+ sdncRespCode)
				if (sdncRespCode.equals("404") && "deactivate".equals(method))
				{
					execution.setVariable("ARNotFoundInSDNC", true)
					if ("true".equals(execution.getVariable("failNotFound")))
					{
						msg = "Allotted Resource Not found in SDNC"
						logger.debug(msg)
						exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
					}
					else
					{
						execution.setVariable("wasDeleted", false)
					}
				}
				else
				{
					throw new BpmnError("MSOWorkflowException")
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in validateSDNCResp. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end Exit ValidateSDNCResp Process")
	}

	public void deleteAaiAR(DelegateExecution execution){
		logger.trace("start deleteAaiAR")
		
		try{
			AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
			String arLink = execution.getVariable("aaiARPath")
			arUtils.deleteAR(execution, arLink)
		} catch (BpmnError e) {
			throw e;
		}catch(Exception ex){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occurred Processing preProcessSDNCGetRequest.", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + ex);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + ex.getMessage())
		}
		logger.trace("end deleteAaiAR")
	}

}
