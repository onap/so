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

package org.onap.so.bpmn.vcpe.scripts;

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.*;
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse

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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateAllottedResourceTXC.class);

	String Prefix="DCARTXC_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {


		String msg = ""
		msoLogger.trace("start preProcessRequest")

		try {
			String msoRequestId	 = execution.getVariable("msoRequestId")
			msoLogger.debug(" msoRequestId  = " + msoRequestId)
			
			execution.setVariable("prefix", Prefix)

			//Config Inputs
			String sdncCallbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "mso.workflow.sdncadapter.callback is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			msoLogger.debug("SDNC Callback URL: " + sdncCallbackUrl)

			String sdncReplDelay = UrnPropertiesReader.getVariable("mso.workflow.sdnc.replication.delay",execution)
			if (isBlank(sdncReplDelay)) {
				msg = "mso.workflow.sdnc.replication.delay is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncReplDelay", sdncReplDelay)
			msoLogger.debug("SDNC replication delay: " + sdncReplDelay)

			//Request Inputs
			if (isBlank(execution.getVariable("serviceInstanceId"))){
				msg = "Input serviceInstanceId is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("parentServiceInstanceId"))) {
				msg = "Input parentServiceInstanceId is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceModelInfo"))) {
				msg = "Input allottedResourceModelInfo is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("brgWanMacAddress"))) {
				msg = "Input brgWanMacAddress is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceRole"))) {
				msg = "Input allottedResourceRole is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceType"))) {
				msg = "Input allottedResourceType is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end preProcessRequest")
	}

	public void getAaiAR (DelegateExecution execution) {


		msoLogger.trace("start getAaiAR")

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
			msoLogger.debug(errorMsg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, errorMsg)
		}
		msoLogger.trace("end getAaiAR")
	}

	public void createAaiAR(DelegateExecution execution) {


		msoLogger.trace("start createAaiAR")
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
			AAIResourceUri siResourceLink= execution.getVariable("PSI_resourceLink")
			AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
			arUrl = arUtils.createARUrl(execution, siResourceLink, allottedResourceId)
			execution.setVariable("aaiARPath", arUrl)
			msoLogger.debug("PUT AllottedResource AAI URL is:\n" + arUrl)

			String namespace = aaiUriUtil.getNamespaceFromUri(execution, arUrl)

			String arType = execution.getVariable("allottedResourceType")
			String arRole = execution.getVariable("allottedResourceRole")
			String CSI_resourceLink = execution.getVariable("CSI_resourceLink")
			String arModelInfo = execution.getVariable("allottedResourceModelInfo")
			msoLogger.debug("arModelInfo is:\n" + arModelInfo)
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
				<id>${MsoUtils.xmlEscape(allottedResourceId)}</id>
				<description></description>
				<type>${MsoUtils.xmlEscape(arType)}</type>
				<role>${MsoUtils.xmlEscape(arRole)}</role>
				<selflink></selflink>
				<model-invariant-id>${MsoUtils.xmlEscape(modelInvariantId)}</model-invariant-id>
				<model-version-id>${MsoUtils.xmlEscape(modelVersionId)}</model-version-id>
				<model-customization-id>${MsoUtils.xmlEscape(modelCustomizationId)}</model-customization-id>
				<orchestration-status>PendingCreate</orchestration-status>
				<operation-status></operation-status>
				<relationship-list>
					<relationship>
               			<related-to>service-instance</related-to>
               			<related-link>${MsoUtils.xmlEscape(CSI_resourceLink)}</related-link>
					</relationship>
				</relationship-list>
			</allotted-resource>""".trim()

			execution.setVariable("AaiARPayload", payload)
			msoLogger.debug(" payload to create AllottedResource in AAI:" + "\n" + payload)

			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, arUrl, payload)
			int responseCode = response.getStatusCode()
			msoLogger.debug("AllottedResource AAI PUT responseCode:" + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			msoLogger.debug("AllottedResource AAI PUT responseStr:" + aaiResponse)

			//200 OK 201 CREATED 202 ACCEPTED
			if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			{
				msoLogger.debug("AAI PUT AllottedResource received a Good Response")
			}
			else{
				msoLogger.debug("AAI Put AllottedResouce received a Bad Response Code: " + responseCode)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		} catch (Exception ex) {
			msg = "Exception in createAaiAR " + ex.getMessage()
			msoLogger.debug(msg)
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
		msoLogger.trace("end createAaiAR")
	}

	public String buildSDNCRequest(DelegateExecution execution, String action, String sdncRequestId) {


		String msg = ""
		msoLogger.trace("start buildSDNCRequest")
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
						<request-action>CreateTunnelXConnInstance</request-action>
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
							<brg-wan-mac-address>${MsoUtils.xmlEscape(brgWanMacAddress)}</brg-wan-mac-address>
					</tunnelxconn-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			msoLogger.debug("sdncRequest:\n" + sdncReq)
			sdncReq = utils.formatXml(sdncReq)

		} catch(Exception ex) {
			msg = "Exception in buildSDNCRequest. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end buildSDNCRequest")
		return sdncReq
	}

	public void preProcessSDNCAssign(DelegateExecution execution) {


		String msg = ""
		msoLogger.trace("start preProcessSDNCAssign")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncAssignReq = buildSDNCRequest(execution, "assign", sdncRequestId)
			execution.setVariable("sdncAssignRequest", sdncAssignReq)
			msoLogger.debug("sdncAssignRequest:  " + sdncAssignReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncAssignRollbackReq = sdncAssignReq.replace(">assign<", ">unassign<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncAssignRollbackReq", sdncAssignRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			msoLogger.debug("sdncAssignRollbackReq:\n" + sdncAssignRollbackReq)
			msoLogger.debug("rollbackData:\n" + rollbackData.toString())

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCAssign. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.debug("end preProcessSDNCAssign")
	}

	public void preProcessSDNCCreate(DelegateExecution execution) {


		String msg = ""
		msoLogger.trace("start preProcessSDNCCreate")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncCreateReq = buildSDNCRequest(execution, "create", sdncRequestId)
			execution.setVariable("sdncCreateRequest", sdncCreateReq)
			msoLogger.debug("sdncCreateReq:  " + sdncCreateReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncCreateRollbackReq = sdncCreateReq.replace(">create<", ">delete<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncCreateRollbackReq", sdncCreateRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			msoLogger.debug("sdncCreateRollbackReq:\n" + sdncCreateRollbackReq)
			msoLogger.debug("rollbackData:\n" + rollbackData.toString())

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCCreate. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end preProcessSDNCCreate")
	}

	public void preProcessSDNCActivate(DelegateExecution execution) {


		String msg = ""
		msoLogger.trace("start preProcessSDNCActivate")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncActivateReq = buildSDNCRequest(execution, "activate", sdncRequestId)
			execution.setVariable("sdncActivateRequest", sdncActivateReq)
			msoLogger.debug("sdncActivateReq:  " + sdncActivateReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncActivateRollbackReq = sdncActivateReq.replace(">activate<", ">deactivate<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncActivateRollbackReq", sdncActivateRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			msoLogger.debug("sdncActivateRollbackReq:\n" + sdncActivateRollbackReq)
			msoLogger.debug("rollbackData:\n" + rollbackData.toString())

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCActivate. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end preProcessSDNCActivate")
	}

	public void validateSDNCResp(DelegateExecution execution, String response, String method){


		msoLogger.trace("start ValidateSDNCResponse Process")
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			msoLogger.debug("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			msoLogger.debug("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msoLogger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + response)

				if (!"get".equals(method))
				{
					def rollbackData = execution.getVariable("rollbackData")
					rollbackData.put(Prefix, "rollback" +  "SDNC" + method, "true")
					execution.setVariable("rollbackData", rollbackData)
				}

			}else{
				msoLogger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in validateSDNCResp. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end ValidateSDNCResp Process")
	}

	public void preProcessSDNCGet(DelegateExecution execution){

		msoLogger.trace("start preProcessSDNCGet")
		try{

			def callbackUrl = execution.getVariable("sdncCallbackUrl")
			// serviceOperation (URI for topology GET) will be retrieved from "selflink" from AAI if active AR exists in AAI
			// or from  "object-path" in SDNC response for assign when AR does not exist in AA

			String serviceOperation = ""

			if (execution.getVariable("foundActiveAR")) {
				def aaiQueryResponse = execution.getVariable("aaiARGetResponse")
				serviceOperation = utils.getNodeText(aaiQueryResponse, "selflink")
				msoLogger.debug("AR service operation/aaiARSelfLink: " + serviceOperation)
			}
			else
			{
				String response = execution.getVariable("sdncAssignResponse")
				String data = utils.getNodeXml(response, "response-data")
				msoLogger.debug("Assign responseData: " + data)
				serviceOperation = utils.getNodeText(data, "object-path")
				msoLogger.debug("AR service operation:" + serviceOperation)
			}

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String sdncRequestId = UUID.randomUUID().toString()
			
			//neeed the same url as used by vfmodules
			String SDNCGetRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
											xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${MsoUtils.xmlEscape(sdncRequestId)}</sdncadapter:RequestId>
					<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>vfmodule</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			execution.setVariable("sdncGetRequest", SDNCGetRequest)

		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occurred Processing preProcessSDNCGetRequest.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + e.getMessage())
		}
		msoLogger.trace("end preProcessSDNCGet")
	}
	
	public void updateAaiAROrchStatus(DelegateExecution execution, String status){

		msoLogger.trace("start updateAaiAROrchStatus")
		String aaiARPath = execution.getVariable("aaiARPath") //set during query (existing AR) or create
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		msoLogger.trace("end updateAaiAROrchStatus")
	}
	
	public void generateOutputs(DelegateExecution execution)
	{

		msoLogger.trace("start generateOutputs")
		try {
			String sdncGetResponse = execution.getVariable("enhancedCallbackRequestData") //unescaped
			msoLogger.debug("resp:" + sdncGetResponse)
			String arData = utils.getNodeXml(sdncGetResponse, "tunnelxconn-topology")
			arData = utils.removeXmlNamespaces(arData)
		
			String txca = utils.getNodeXml(arData, "tunnelxconn-assignments")
			execution.setVariable("vni", utils.getNodeText(txca, "vni"))
			execution.setVariable("vgmuxBearerIP", utils.getNodeText(txca, "vgmux-bearer-ip"))
			execution.setVariable("vgmuxLanIP", utils.getNodeText(txca, "vgmux-lan-ip"))
			
			String ari = utils.getNodeXml(arData, "allotted-resource-identifiers")
			execution.setVariable("allotedResourceName", utils.getNodeText(ari, "allotted-resource-name"))
		} catch (BpmnError e) {
			msoLogger.debug("BPMN Error in generateOutputs ")
		} catch(Exception ex) {
			String msg = "Exception in generateOutputs " + ex.getMessage()
			msoLogger.debug(msg)
		}
		msoLogger.trace("end generateOutputs")
		
	}

	public void preProcessRollback (DelegateExecution execution) {

		msoLogger.trace("start preProcessRollback")
		try {

			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				msoLogger.debug("Prev workflowException: " + workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			msoLogger.debug("BPMN Error during preProcessRollback")
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			msoLogger.debug(msg)
		}
		msoLogger.trace("end preProcessRollback")
	}

	public void postProcessRollback (DelegateExecution execution) {

		msoLogger.trace("start postProcessRollback")
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				msoLogger.debug("Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException);
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			msoLogger.debug("BPMN Error during postProcessRollback")
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			msoLogger.debug(msg)
		}
		msoLogger.trace("end postProcessRollback")
	}

}
