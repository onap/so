/*
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
package org.openecomp.mso.bpmn.vcpe.scripts;

import org.openecomp.mso.bpmn.common.scripts.*;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.springframework.web.util.UriUtils;
import static org.apache.commons.lang3.StringUtils.*

/**
 * This groovy class supports the <class>DoDeleteAllottedResourceBRG.bpmn</class> process.
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
public class DoDeleteAllottedResourceBRG extends AbstractServiceTaskProcessor{

	private static final String DebugFlag = "isDebugLogEnabled"

	String Prefix="DDARBRG_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest (DelegateExecution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest *****",  isDebugEnabled)

		try {
			execution.setVariable("prefix", Prefix)

			//Config Inputs
			String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (isBlank(sdncCallbackUrl)) {
				msg = "URN_mso_workflow_sdncadapter_callback is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			utils.log("DEBUG","SDNC Callback URL: " + sdncCallbackUrl, isDebugEnabled)

			//Request Inputs
			if (isBlank(execution.getVariable("serviceInstanceId"))){
				msg = "Input serviceInstanceId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceId"))){
				msg = "Input allottedResourceId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	public void getAaiAR (DelegateExecution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** getAaiAR ***** ", isDebugEnabled)

		String allottedResourceId = execution.getVariable("allottedResourceId")

		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String ar = arUtils.getARbyId(execution, allottedResourceId)

		String errorMsg = ""
		if (isBlank(ar)) // AR was !found
		{
			errorMsg = "Allotted resource not found in AAI with AllottedResourceId:" + allottedResourceId
		}
		else
		{
			String aaiARPath = execution.getVariable("aaiARPath")
			String parentServiceInstanceId = arUtils.getPSIFmARLink(execution, aaiARPath)
			execution.setVariable("parentServiceInstanceId", parentServiceInstanceId)
		}
		if (!isBlank(errorMsg)) {
			utils.log("DEBUG", errorMsg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, errorMsg)
		}
		utils.log("DEBUG"," ***** getAaiAR *****",  isDebugEnabled)

	}

	// aaiARPath set during query (existing AR)
	public void updateAaiAROrchStatus(DelegateExecution execution, String status){
		def isDebugEnabled = execution.getVariable(DebugFlag)
		utils.log("DEBUG", " *** updateAaiAROrchStatus *** ", isDebugEnabled)
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String aaiARPath = execution.getVariable("aaiARPath") //set during query (existing AR) 
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		utils.log("DEBUG", " *** Exit updateAaiAROrchStatus *** ", isDebugEnabled)
	}

	public String buildSDNCRequest(DelegateExecution execution, String action, String sdncRequestId) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** buildSDNCRequest *****", isDebugEnabled)
		String sdncReq = null

		try {

			String allottedResourceId = execution.getVariable("allottedResourceId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String parentServiceInstanceId = execution.getVariable("parentServiceInstanceId")
			String globalCustomerId = execution.getVariable("globalCustomerId")
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

			String callbackUrl = execution.getVariable("sdncCallbackUrl")
			String requestId = execution.getVariable("msoRequestId")

			String serviceChainServiceInstanceId = ""
			String sourceNetworkId = ""
			String sourceNetworkRole = ""
			String allottedResourceRole = ""

			String arModelInfo = ""
			String modelInvariantId = ""
			String modelVersion = ""
			String modelUUId = ""
			String modelCustomizationId = ""
			String modelName = ""


			sdncReq =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${sdncRequestId}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>brg-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${requestId}</request-id>
						<request-action>DeleteBRGInstance</request-action>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
					</request-information>
					<service-information>
						<service-id></service-id>
						<subscription-service-type>${subscriptionServiceType}</subscription-service-type>
						<ecomp-model-information></ecomp-model-information>
						<service-instance-id>${serviceInstanceId}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${globalCustomerId}</global-customer-id>
					</service-information>
					<allotted-resource-information>
						<allotted-resource-id>${allottedResourceId}</allotted-resource-id>    
						<allotted-resource-type>brg</allotted-resource-type>
						<parent-service-instance-id>${parentServiceInstanceId}</parent-service-instance-id>   
						<ecomp-model-information>
							<model-invariant-uuid>${modelInvariantId}</model-invariant-uuid>
							<model-uuid>${modelUUId}</model-uuid>
							<model-customization-uuid>${modelCustomizationId}</model-customization-uuid>
							<model-version>${modelVersion}</model-version>
							<model-name>${modelName}</model-name>
						</ecomp-model-information>
					</allotted-resource-information>
					<brg-request-input>
					</brg-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			utils.log("DEBUG","sdncRequest:\n" + sdncReq, isDebugEnabled)
			sdncReq = utils.formatXml(sdncReq)

		} catch(Exception ex) {
			msg = "Exception in buildSDNCRequest. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit buildSDNCRequest *****", isDebugEnabled)
		return sdncReq
	}

	public void preProcessSDNCUnassign(DelegateExecution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCUnassign *****", isDebugEnabled)

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncUnassignReq = buildSDNCRequest(execution, "unassign", sdncRequestId)
			execution.setVariable("sdncUnassignRequest", sdncUnassignReq)
			utils.logAudit("sdncUnassignRequest:  " + sdncUnassignReq)
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCUnassign. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCUnassign *****", isDebugEnabled)
	}

	public void preProcessSDNCDelete(DelegateExecution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCDelete *****", isDebugEnabled)

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncDeleteReq = buildSDNCRequest(execution, "delete", sdncRequestId)
			execution.setVariable("sdncDeleteRequest", sdncDeleteReq)
			utils.logAudit("sdncDeleteReq:  " + sdncDeleteReq)
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCDelete *****", isDebugEnabled)
	}

	public void preProcessSDNCDeactivate(DelegateExecution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCDeactivate *****", isDebugEnabled)

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncDeactivateReq = buildSDNCRequest(execution, "deactivate", sdncRequestId)
			execution.setVariable("sdncDeactivateRequest", sdncDeactivateReq)
			utils.logAudit("sdncDeactivateReq:  " + sdncDeactivateReq)
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDeactivate. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCDeactivate *****", isDebugEnabled)
	}

	public void validateSDNCResp(DelegateExecution execution, String response, String method){

		def isDebugLogEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " *** ValidateSDNCResponse Process*** ", isDebugLogEnabled)
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			utils.logAudit("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			utils.logAudit("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				utils.log("DEBUG", "Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + response, isDebugLogEnabled)

			}else{
				String sdncRespCode = execution.getVariable(Prefix + 'sdncRequestDataResponseCode')
				utils.log("DEBUG", method + " AllottedResource received error response from SDNC. ResponseCode:" +  sdncRespCode, isDebugLogEnabled)
				if (sdncRespCode.equals("404") && "deactivate".equals(method))
				{
					execution.setVariable("ARNotFoundInSDNC", true)
					if ("true".equals(execution.getVariable("failNotFound")))
					{
						msg = "Allotted Resource Not found in SDNC"
						utils.log("DEBUG", msg, isDebugLogEnabled)
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
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logDebug(" *** Exit ValidateSDNCResp Process*** ", isDebugLogEnabled)
	}

	public void deleteAaiAR(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable(DebugFlag)
		try{
			utils.log("DEBUG", " *** deleteAaiAR *** ", isDebugLogEnabled)
			AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
			String ar = null //need to get resource-version again 
			String arLink = execution.getVariable("aaiARPath")
			if (!isBlank(arLink))
			{
				ar = arUtils.getARbyLink(execution, arLink, "")
			}
			arUtils.deleteAR(execution, arLink + '?resource-version=' + UriUtils.encode(execution.getVariable("aaiARResourceVersion"),"UTF-8"))
		} catch (BpmnError e) {
			throw e;
		}catch(Exception ex){
			utils.log("ERROR", "Exception Occurred Processing preProcessSDNCGetRequest. Exception is:\n" + ex, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + ex.getMessage())
		}
		utils.log("DEBUG", " *** Exit deleteAaiAR *** ", isDebugLogEnabled)
	}

}
