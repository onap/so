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

package org.onap.so.bpmn.vcpe.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.AllottedResource
import org.onap.so.bpmn.common.scripts.*
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.UriBuilder

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoCreateAllottedResourceBRG.bpmn</class> process.
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
 * @param - vni
 * @param - vgmuxBearerIP
 *
 * Outputs:
 * @param - rollbackData (localRB->null)
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 * @param - WorkflowException - O
 * @param - allottedResourceId
 * @param - allottedResourceName
 *
 */
public class DoCreateAllottedResourceBRG extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateAllottedResourceBRG.class);

	String Prefix="DCARBRG_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {


		String msg = ""
		msoLogger.trace("start preProcessRequest")

		try {
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
			if (isBlank(execution.getVariable("vni"))) {
				msg = "Input vni is null"
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("vgmuxBearerIP"))) {
				msg = "Input vgmuxBearerIP is null"
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

	/**
	 * Gets the service instance uri from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		msoLogger.trace("getServiceInstance ")
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

			if(resourceClient.exists(uri)){
				execution.setVariable("CSI_resourceLink", uri.build().toString())
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
			}

		}catch(BpmnError e) {
			throw e;
		}catch (Exception ex){
			String msg = "Exception in getServiceInstance. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit getServiceInstance ")
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

	public void getParentServiceInstance(DelegateExecution execution) {
		msoLogger.trace("getParentServiceInstance ")
		try {
			String serviceInstanceId = execution.getVariable('parentServiceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

			try {
				//just to make sure the serviceInstance exists
				uri.build()
				execution.setVariable("PSI_resourceLink", uri)
			} catch (NotFoundException e) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
			}

		}catch(BpmnError e) {
			throw e;
		}catch (Exception ex){
			String msg = "Exception in getParentServiceInstance. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit getParentServiceInstance ")
	}


	public void createAaiAR(DelegateExecution execution) {


		msoLogger.trace("start createAaiAR")

		String allottedResourceId = execution.getVariable("allottedResourceId")
		if (isBlank(allottedResourceId))
		{
			allottedResourceId = UUID.randomUUID().toString()
			execution.setVariable("allottedResourceId", allottedResourceId)
		}
		try {

			AAIResourceUri siResourceLink= execution.getVariable("PSI_resourceLink")

			AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceFromParentURI(siResourceLink, AAIObjectType.ALLOTTED_RESOURCE, allottedResourceId)

			execution.setVariable("aaiARPath", allottedResourceUri.build().toString());
			String arType = execution.getVariable("allottedResourceType")
			String arRole = execution.getVariable("allottedResourceRole")
			String CSI_resourceLink = execution.getVariable("CSI_resourceLink")

			String arModelInfo = execution.getVariable("allottedResourceModelInfo")
			String modelInvariantId = jsonUtil.getJsonValue(arModelInfo, "modelInvariantUuid")
			String modelVersionId = jsonUtil.getJsonValue(arModelInfo, "modelUuid")

			AllottedResource resource = new AllottedResource()
			resource.setId(allottedResourceId)
			resource.setType(arType)
			resource.setRole(arRole)
			resource.setModelInvariantId(modelInvariantId)
			resource.setModelVersionId(modelVersionId)
			getAAIClient().create(allottedResourceUri, resource)
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.SERVICE_INSTANCE, UriBuilder.fromPath(CSI_resourceLink).build())
			getAAIClient().connect(allottedResourceUri,serviceInstanceUri)
		}catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception in createAaiAR " + ex.getMessage())
		}

		//start rollback set up
		RollbackData rollbackData = new RollbackData()
		def disableRollback = execution.getVariable("disableRollback")
		rollbackData.put(Prefix, "disableRollback", disableRollback.toString())
		rollbackData.put(Prefix, "rollbackAAI", "true")
		rollbackData.put(Prefix, "allottedResourceId", allottedResourceId)
		rollbackData.put(Prefix, "serviceInstanceId", execution.getVariable("serviceInstanceId"))
		rollbackData.put(Prefix, "parentServiceInstanceId", execution.getVariable("parentServiceInstanceId"))
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
			String callbackUrl = execution.getVariable("sdncCallbackUrl")
			String requestId = execution.getVariable("msoRequestId")

			String brgWanMacAddress = execution.getVariable("brgWanMacAddress")
			String vni = execution.getVariable("vni")
			String vgmuxBearerIP = execution.getVariable("vgmuxBearerIP")

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
							<sdncadapter:SvcOperation>brg-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						<request-action>CreateBRGInstance</request-action>
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
						<allotted-resource-type>brg</allotted-resource-type>
						<parent-service-instance-id>${MsoUtils.xmlEscape(parentServiceInstanceId)}</parent-service-instance-id>
						<onap-model-information>
							<model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantId)}</model-invariant-uuid>
							<model-uuid>${MsoUtils.xmlEscape(modelUUId)}</model-uuid>
							<model-customization-uuid>${MsoUtils.xmlEscape(modelCustomizationId)}</model-customization-uuid>
							<model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
							<model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
						</onap-model-information>
					</allotted-resource-information>
					<brg-request-input>
							<brg-wan-mac-address>${MsoUtils.xmlEscape(brgWanMacAddress)}</brg-wan-mac-address>
							<vni>${MsoUtils.xmlEscape(vni)}</vni>
							<vgmux-bearer-ip>${MsoUtils.xmlEscape(vgmuxBearerIP)}</vgmux-bearer-ip>
					</brg-request-input>
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
			String sdncAssignRollbackReq = sdncAssignReq.replace(">assign<", ">unassign<").replace(">CreateBRGInstance<", ">DeleteBRGInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
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
		msoLogger.trace("end preProcessSDNCAssign")
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
			String sdncCreateRollbackReq = sdncCreateReq.replace(">create<", ">delete<").replace(">CreateBRGInstance<", ">DeleteBRGInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
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
			String sdncActivateRollbackReq = sdncActivateReq.replace(">activate<", ">deactivate<").replace(">CreateBRGInstance<", ">DeleteBRGInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
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


		msoLogger.trace("ValidateSDNCResponse Process")
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
		msoLogger.trace("End ValidateSDNCResp Process")
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
			String arData = utils.getNodeXml(sdncGetResponse, "brg-topology")
			arData = utils.removeXmlNamespaces(arData)

			String brga = utils.getNodeXml(arData, "brg-assignments")
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
