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
package org.openecomp.mso.bpmn.vcpe.scripts

import org.openecomp.mso.bpmn.common.scripts.*
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.springframework.web.util.UriUtils
import static org.apache.commons.lang3.StringUtils.*


/**
 * This groovy class supports the <class>DoCreateAllottedResourceTXC.bpmn</class> process.
 *
 * @author
 * 
 * Inputs:
 * @param - msoRequestId
 * @param - isDEbugLogEnabled
 * @param - disableRollback
 * @param - failExists  - O
 * @param - serviceInstanceId
 * @param - globalCustomerId - O
 * @param - subscriptionServiceType - O
 * @param - parentServiceInstanceId
 * @param - allottedReourceId - O
 * @param - allottedResourceModelInfo
 * @param - allottedResourceRole
 * @param - allottedResourceType
 * @param - brgWanMacAddress
 *
 * Outputs:
 * @param - rollbackData (localRB->null)
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 * @param - WorkflowException - O
 * @param - allottedResourceId
 * @param - allottedResourceName 
 * @param - vni 
 * @param - vgmuxBearerIP 
 * @param - vgmuxLanIP 
 *
 */
public class DoCreateAllottedResourceTXC extends AbstractServiceTaskProcessor{

	private static final String DebugFlag = "isDebugLogEnabled"

	String Prefix="DCARTXC_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (Execution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest *****",  isDebugEnabled)

		try {
			String msoRequestId	 = execution.getVariable("msoRequestId")
			utils.log("DEBUG", " msoRequestId  = " + msoRequestId,  isDebugEnabled)
			
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

			String sdncReplDelay = execution.getVariable('URN_mso_workflow_sdnc_replication_delay')
			if (isBlank(sdncReplDelay)) {
				msg = "URN_mso_workflow_sdnc_replication_delay is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncReplDelay", sdncReplDelay)
			utils.log("DEBUG","SDNC replication delay: " + sdncReplDelay, isDebugEnabled)

			//Request Inputs
			if (isBlank(execution.getVariable("serviceInstanceId"))){
				msg = "Input serviceInstanceId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("parentServiceInstanceId"))) {
				msg = "Input parentServiceInstanceId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceModelInfo"))) {
				msg = "Input allottedResourceModelInfo is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("brgWanMacAddress"))) {
				msg = "Input brgWanMacAddress is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceRole"))) {
				msg = "Input allottedResourceRole is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceType"))) {
				msg = "Input allottedResourceType is null"
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

	public void getAaiAR (Execution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** getAaiAR ***** ", isDebugEnabled)

		String arType = execution.getVariable("allottedResourceType")
		String arRole = execution.getVariable("allottedResourceRole")

		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String orchStatus = arUtils.getAROrchStatus(execution)

		String errorMsg = ""

		if (orchStatus != null) // AR was found
		{
			if ("true".equals(execution.getVariable("failExists")))
			{
				errorMsg = "Allotted resource " + arType + " with Role " + arRole + " already exists"
			}
			else
			{
				if ("Active".equals(orchStatus))
				{
					execution.setVariable("foundActiveAR", true)
				}
				else // blanks included
				{
					errorMsg = "Allotted Resource " + arType + " with Role " + arRole + " already exists in an incomplete state -"  +  orchStatus
				}
			}
		}
		if (!isBlank(errorMsg)) {
			utils.log("DEBUG", errorMsg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, errorMsg)
		}
		utils.log("DEBUG"," *****Exit getAaiAR *****", isDebugEnabled)
	}

	public void createAaiAR(Execution execution) {

		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** createAaiAR ***** ", isDebugEnabled)
		String msg = ""

		String allottedResourceId = execution.getVariable("allottedResourceId")
		if (isBlank(allottedResourceId))
		{
			allottedResourceId = UUID.randomUUID().toString()
			execution.setVariable("allottedResourceId", allottedResourceId)
		}
		String arUrl = ""
		try {

			//AAI PUT
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aaiEndpoint = execution.getVariable("URN_aai_endpoint")
			String siResourceLink= execution.getVariable("PSI_resourceLink")

			String siUri = ""
			utils.log("DEBUG", "PSI_resourceLink:" + siResourceLink, isDebugEnabled)

			if(!isBlank(siResourceLink)) {
				utils.log("DEBUG", "Incoming PSI Resource Link is: " + siResourceLink, isDebugEnabled)
				String[] split = siResourceLink.split("/aai/")
				siUri = "/aai/" + split[1]
			}
			else
			{
				msg = "Parent Service Link in AAI is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			arUrl = "${aaiEndpoint}${siUri}"  + "/allotted-resources/allotted-resource/" + UriUtils.encode(allottedResourceId,"UTF-8")
			execution.setVariable("aaiARPath", arUrl)
			utils.log("DEBUG", "GET AllottedResource AAI URL is:\n" + arUrl, isDebugEnabled)

			String namespace = aaiUriUtil.getNamespaceFromUri(execution, arUrl)

			String arType = execution.getVariable("allottedResourceType")
			String arRole = execution.getVariable("allottedResourceRole")
			String CSI_resourceLink = execution.getVariable("CSI_resourceLink")
			String arModelInfo = execution.getVariable("allottedResourceModelInfo")
			utils.log("DEBUG", "arModelInfo is:\n" + arModelInfo, isDebugEnabled)
			String modelInvariantId = jsonUtil.getJsonValue(arModelInfo, "modelInvariantUuid")
			String modelVersionId = jsonUtil.getJsonValue(arModelInfo, "modelUuid")
			String modelCustomizationId = jsonUtil.getJsonValue(arModelInfo, "modelCustomizationUuid")

			if (modelInvariantId == null) {
				modelInvariantId = ""
			}
			if (modelVersionId == null) {
				modelVersionId = ""
			}
			if (modelCustomizationId == null) {
				modelCustomizationId = ""
			}

			String payload =
			"""<allotted-resource xmlns="${namespace}">
				<id>${allottedResourceId}</id>
				<description></description>
				<type>${arType}</type>
				<role>${arRole}</role>
				<selflink></selflink>
				<model-invariant-id>${modelInvariantId}</model-invariant-id>
				<model-version-id>${modelVersionId}</model-version-id>
				<model-customization-id>${modelCustomizationId}</model-customization-id>
				<orchestration-status>PendingCreate</orchestration-status>
				<operation-status></operation-status>
				<relationship-list>
					<relationship>
               			<related-to>service-instance</related-to>
               			<related-link>${CSI_resourceLink}</related-link>
					</relationship>
				</relationship-list>
			</allotted-resource>""".trim()

			execution.setVariable("AaiARPayload", payload)
			utils.log("DEBUG", " payload to create AllottedResource in AAI:" + "\n" + payload, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, arUrl, payload)
			int responseCode = response.getStatusCode()
			utils.log("DEBUG", "AllottedResource AAI PUT responseCode:" + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			utils.log("DEBUG", "AllottedResource AAI PUT responseStr:" + aaiResponse, isDebugEnabled)

			//200 OK 201 CREATED 202 ACCEPTED
			if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			{
				utils.log("DEBUG", "AAI PUT AllottedResource received a Good Response", isDebugEnabled)
			}
			else{
				utils.log("DEBUG", "AAI Put AllottedResouce received a Bad Response Code: " + responseCode, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		} catch (Exception ex) {
			msg = "Exception in createAaiAR " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}

		//start rollback set up
		RollbackData rollbackData = new RollbackData()
		def disableRollback = execution.getVariable("disableRollback")
		rollbackData.put(Prefix, "disableRollback", disableRollback.toString())
		rollbackData.put(Prefix, "rollbackAAI", "true")
		rollbackData.put(Prefix, "allottedResourceId", allottedResourceId)
		rollbackData.put(Prefix, "serviceInstanceId", execution.getVariable("serviceInstanceId"))
		rollbackData.put(Prefix, "parentServiceInstanceId", execution.getVariable("parentServiceInstanceId"))
		rollbackData.put(Prefix, "aaiARPath", arUrl)
		execution.setVariable("rollbackData", rollbackData)
		utils.log("DEBUG"," *** Exit createAaiAR*** ", isDebugEnabled)
	}

	public String buildSDNCRequest(Execution execution, String action, String sdncRequestId) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** buildSDNCRequest *****", isDebugEnabled)
		String sdncReq = null

		try {

			String allottedResourceId = execution.getVariable("allottedResourceId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String globalCustomerId = execution.getVariable("globalCustomerId")
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
			String parentServiceInstanceId = execution.getVariable("parentServiceInstanceId")
			String serviceChainServiceInstanceId = execution.getVariable("serviceChainServiceInstanceId")
			String callbackUrl = execution.getVariable("sdncCallbackUrl")
			String requestId = execution.getVariable("msoRequestId")

			String brgWanMacAddress = execution.getVariable("brgWanMacAddress")

			String arModelInfo = execution.getVariable("allottedResourceModelInfo")
			String modelInvariantId = jsonUtil.getJsonValue(arModelInfo, "modelInvariantUuid")
			String modelVersion = jsonUtil.getJsonValue(arModelInfo, "modelVersion")
			String modelUUId = jsonUtil.getJsonValue(arModelInfo, "modelUuid")
			String modelCustomizationId = jsonUtil.getJsonValue(arModelInfo, "modelCustomizationUuid")
			String modelName = jsonUtil.getJsonValue(arModelInfo, "modelName")

			if (modelInvariantId == null) {
				modelInvariantId = ""
			}
			if (modelVersion == null) {
				modelVersion = ""
			}
			if (modelUUId == null) {
				modelUUId = ""
			}
			if (modelName == null) {
				modelName = ""
			}
			if (modelCustomizationId == null) {
				modelCustomizationId = ""
			}

			sdncReq =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${sdncRequestId}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>tunnelxconn-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${requestId}</request-id>
						<request-action>CreateTunnelXConnInstance</request-action>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
					</request-information>
					<service-information>
						<service-id></service-id>
						<subscription-service-type>${subscriptionServiceType}</subscription-service-type>
						<onap-model-information></onap-model-information>
						<service-instance-id>${serviceInstanceId}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${globalCustomerId}</global-customer-id>
					</service-information>
					<allotted-resource-information>
						<allotted-resource-id>${allottedResourceId}</allotted-resource-id>    
						<allotted-resource-type>tunnelxconn</allotted-resource-type>
						<parent-service-instance-id>${parentServiceInstanceId}</parent-service-instance-id>   
						<onap-model-information>
							<model-invariant-uuid>${modelInvariantId}</model-invariant-uuid>
							<model-uuid>${modelUUId}</model-uuid>
							<model-customization-uuid>${modelCustomizationId}</model-customization-uuid>
							<model-version>${modelVersion}</model-version>
							<model-name>${modelName}</model-name>
						</onap-model-information>
					</allotted-resource-information>
					<tunnelxconn-request-input>
							<brg-wan-mac-address>${brgWanMacAddress}</brg-wan-mac-address>
					</tunnelxconn-request-input>
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

	public void preProcessSDNCAssign(Execution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCAssign *****", isDebugEnabled)

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncAssignReq = buildSDNCRequest(execution, "assign", sdncRequestId)
			execution.setVariable("sdncAssignRequest", sdncAssignReq)
			utils.logAudit("sdncAssignRequest:  " + sdncAssignReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncAssignRollbackReq = sdncAssignReq.replace(">assign<", ">unassign<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncAssignRollbackReq", sdncAssignRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			utils.log("DEBUG","sdncAssignRollbackReq:\n" + sdncAssignRollbackReq, isDebugEnabled)
			utils.log("DEBUG","rollbackData:\n" + rollbackData.toString(), isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCAssign. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCAssign *****", isDebugEnabled)
	}

	public void preProcessSDNCCreate(Execution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCCreate *****", isDebugEnabled)

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncCreateReq = buildSDNCRequest(execution, "create", sdncRequestId)
			execution.setVariable("sdncCreateRequest", sdncCreateReq)
			utils.logAudit("sdncCreateReq:  " + sdncCreateReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncCreateRollbackReq = sdncCreateReq.replace(">create<", ">delete<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncCreateRollbackReq", sdncCreateRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			utils.log("DEBUG","sdncCreateRollbackReq:\n" + sdncCreateRollbackReq, isDebugEnabled)
			utils.log("DEBUG","rollbackData:\n" + rollbackData.toString(), isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCCreate. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCCreate *****", isDebugEnabled)
	}

	public void preProcessSDNCActivate(Execution execution) {

		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCActivate *****", isDebugEnabled)

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncActivateReq = buildSDNCRequest(execution, "activate", sdncRequestId)
			execution.setVariable("sdncActivateRequest", sdncActivateReq)
			utils.logAudit("sdncActivateReq:  " + sdncActivateReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncActivateRollbackReq = sdncActivateReq.replace(">activate<", ">deactivate<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncActivateRollbackReq", sdncActivateRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			utils.log("DEBUG","sdncActivateRollbackReq:\n" + sdncActivateRollbackReq, isDebugEnabled)
			utils.log("DEBUG","rollbackData:\n" + rollbackData.toString(), isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCActivate. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCActivate *****", isDebugEnabled)
	}

	public void validateSDNCResp(Execution execution, String response, String method){

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

				if (!"get".equals(method))
				{
					def rollbackData = execution.getVariable("rollbackData")
					rollbackData.put(Prefix, "rollback" +  "SDNC" + method, "true")
					execution.setVariable("rollbackData", rollbackData)
				}

			}else{
				utils.log("DEBUG", "Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in validateSDNCResp. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logDebug(" *** Exit ValidateSDNCResp Process*** ", isDebugLogEnabled)
	}

	public void preProcessSDNCGet(Execution execution){
		def isDebugLogEnabled = execution.getVariable(DebugFlag)
		utils.log("DEBUG", "*** preProcessSDNCGet *** ", isDebugLogEnabled)
		try{

			def callbackUrl = execution.getVariable("sdncCallbackUrl")
			// serviceOperation (URI for topology GET) will be retrieved from "selflink" from AAI if active AR exists in AAI
			// or from  "object-path" in SDNC response for assign when AR does not exist in AA

			String serviceOperation = ""

			if (execution.getVariable("foundActiveAR")) {
				def aaiQueryResponse = execution.getVariable("aaiARGetResponse")
				serviceOperation = utils.getNodeText1(aaiQueryResponse, "selflink")
				utils.log("DEBUG", "AR service operation/aaiARSelfLink: " + serviceOperation, isDebugLogEnabled)
			}
			else
			{
				String response = execution.getVariable("sdncAssignResponse")
				String data = utils.getNodeXml(response, "response-data")
				data = data.replaceAll("&lt;", "<")
				data = data.replaceAll("&gt;", ">")
				utils.log("DEBUG", "Assign responseData: " + data, isDebugLogEnabled)
				serviceOperation = utils.getNodeText1(data, "object-path")
				utils.log("DEBUG", "AR service operation:" + serviceOperation, isDebugLogEnabled)
			}

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String sdncRequestId = UUID.randomUUID().toString()

			//neeed the same url as used by vfmodules
			String SDNCGetRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
											xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${sdncRequestId}</sdncadapter:RequestId>
					<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${serviceOperation}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>vfmodule</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			execution.setVariable("sdncGetRequest", SDNCGetRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occurred Processing preProcessSDNCGetRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "*** Exit preProcessSDNCGet *** ", isDebugLogEnabled)
	}
	
	public void updateAaiAROrchStatus(Execution execution, String status){
		def isDebugEnabled = execution.getVariable(DebugFlag)
		utils.log("DEBUG", " *** updateAaiAROrchStatus *** ", isDebugEnabled)
		String aaiARPath = execution.getVariable("aaiARPath") //set during query (existing AR) or create
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		utils.log("DEBUG", " *** Exit updateAaiAROrchStatus *** ", isDebugEnabled)
	}
	
	public void generateOutputs(Execution execution)
	{
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** generateOutputs ***** ", isDebugEnabled)
		try {
			String sdncGetResponse = execution.getVariable("enhancedCallbackRequestData") //unescaped
			utils.log("DEBUG", "resp:" + sdncGetResponse, isDebugEnabled)
			String arData = utils.getNodeXml(sdncGetResponse, "tunnelxconn-topology")
			arData = utils.removeXmlNamespaces(arData)
		
			String txca = utils.getNodeXml(arData, "tunnelxconn-assignments")
			execution.setVariable("vni", utils.getNodeText1(txca, "vni"))
			execution.setVariable("vgmuxBearerIP", utils.getNodeText1(txca, "vgmux-bearer-ip"))
			execution.setVariable("vgmuxLanIP", utils.getNodeText1(txca, "vgmux-lan-ip"))
			
			String ari = utils.getNodeXml(arData, "allotted-resource-identifiers")
			execution.setVariable("allotedResourceName", utils.getNodeText1(ari, "allotted-resource-name"))
		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN Error in generateOutputs ", isDebugEnabled)
		} catch(Exception ex) {
			String msg = "Exception in generateOutputs " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit generateOutputs *** ", isDebugEnabled)
		
	}

	public void preProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** preProcessRollback ***** ", isDebugEnabled)
		try {

			Object workflowException = execution.getVariable("WorkflowException")

			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugEnabled)
				execution.setVariable("prevWorkflowException", workflowException)
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN Error during preProcessRollback", isDebugEnabled)
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit preProcessRollback *** ", isDebugEnabled)
	}

	public void postProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** postProcessRollback ***** ", isDebugEnabled)
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException")
			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Setting prevException to WorkflowException: ", isDebugEnabled)
				execution.setVariable("WorkflowException", workflowException)
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			utils.log("DEBUG", "BPMN Error during postProcessRollback", isDebugEnabled)
			throw b
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit postProcessRollback *** ", isDebugEnabled)
	}

}
