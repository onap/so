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

import static org.apache.commons.lang3.StringUtils.isBlank
import jakarta.ws.rs.core.UriBuilder
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.*;
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
	private static final Logger logger = LoggerFactory.getLogger(DoCreateAllottedResourceTXC.class);

	String Prefix="DCARTXC_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {


		String msg = ""
		logger.trace("start preProcessRequest")

		try {
			String msoRequestId	 = execution.getVariable("msoRequestId")
			logger.debug(" msoRequestId  = " + msoRequestId)
			
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

			String sdncReplDelay = UrnPropertiesReader.getVariable("mso.workflow.sdnc.replication.delay",execution)
			if (isBlank(sdncReplDelay)) {
				msg = "mso.workflow.sdnc.replication.delay is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncReplDelay", sdncReplDelay)
			logger.debug("SDNC replication delay: " + sdncReplDelay)

			//Request Inputs
			if (isBlank(execution.getVariable("serviceInstanceId"))){
				msg = "Input serviceInstanceId is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("parentServiceInstanceId"))) {
				msg = "Input parentServiceInstanceId is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceModelInfo"))) {
				msg = "Input allottedResourceModelInfo is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("brgWanMacAddress"))) {
				msg = "Input brgWanMacAddress is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceRole"))) {
				msg = "Input allottedResourceRole is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			if (isBlank(execution.getVariable("allottedResourceType"))) {
				msg = "Input allottedResourceType is null"
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
			logger.debug(errorMsg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, errorMsg)
		}
		logger.trace("end getAaiAR")
	}

	public void createAaiAR(DelegateExecution execution) {


		logger.trace("start createAaiAR")

		String allottedResourceId = execution.getVariable("allottedResourceId")
		if (isBlank(allottedResourceId))
		{
			allottedResourceId = UUID.randomUUID().toString()
			execution.setVariable("allottedResourceId", allottedResourceId)
		}
		try {

			AAIResourceUri siResourceLink= execution.getVariable("PSI_resourceLink")

			AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceFromParentURI(siResourceLink, Types.ALLOTTED_RESOURCE.getFragment(allottedResourceId))

			execution.setVariable("aaiARPath", allottedResourceUri.build().toString());
			String arType = execution.getVariable("allottedResourceType")
			String arRole = execution.getVariable("allottedResourceRole")
			String CSI_resourceLink = execution.getVariable("CSI_resourceLink")
			String arModelInfo = execution.getVariable("allottedResourceModelInfo")
			logger.debug("arModelInfo is:\n" + arModelInfo)
			String modelInvariantId = jsonUtil.getJsonValue(arModelInfo, "modelInvariantUuid")
			String modelVersionId = jsonUtil.getJsonValue(arModelInfo, "modelUuid")

			AllottedResource resource = new AllottedResource()
			resource.setId(allottedResourceId)
			resource.setType(arType)
			resource.setRole(arRole)
			resource.setModelInvariantId(modelInvariantId)
			resource.setModelVersionId(modelVersionId)
			getAAIClient().create(allottedResourceUri, resource)
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceFromExistingURI(Types.SERVICE_INSTANCE, UriBuilder.fromPath(CSI_resourceLink).build())
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
		logger.trace("end createAaiAR")
	}

	public String buildSDNCRequest(DelegateExecution execution, String action, String sdncRequestId) {


		String msg = ""
		logger.trace("start buildSDNCRequest")
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

	public void preProcessSDNCAssign(DelegateExecution execution) {


		String msg = ""
		logger.trace("start preProcessSDNCAssign")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncAssignReq = buildSDNCRequest(execution, "assign", sdncRequestId)
			execution.setVariable("sdncAssignRequest", sdncAssignReq)
			logger.debug("sdncAssignRequest:  " + sdncAssignReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncAssignRollbackReq = sdncAssignReq.replace(">assign<", ">unassign<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncAssignRollbackReq", sdncAssignRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			logger.debug("sdncAssignRollbackReq:\n" + sdncAssignRollbackReq)
			logger.debug("rollbackData:\n" + rollbackData.toString())

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCAssign. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("end preProcessSDNCAssign")
	}

	public void preProcessSDNCCreate(DelegateExecution execution) {


		String msg = ""
		logger.trace("start preProcessSDNCCreate")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncCreateReq = buildSDNCRequest(execution, "create", sdncRequestId)
			execution.setVariable("sdncCreateRequest", sdncCreateReq)
			logger.debug("sdncCreateReq:  " + sdncCreateReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncCreateRollbackReq = sdncCreateReq.replace(">create<", ">delete<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncCreateRollbackReq", sdncCreateRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			logger.debug("sdncCreateRollbackReq:\n" + sdncCreateRollbackReq)
			logger.debug("rollbackData:\n" + rollbackData.toString())

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCCreate. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end preProcessSDNCCreate")
	}

	public void preProcessSDNCActivate(DelegateExecution execution) {


		String msg = ""
		logger.trace("start preProcessSDNCActivate")

		try {
			String sdncRequestId = UUID.randomUUID().toString()
			String sdncActivateReq = buildSDNCRequest(execution, "activate", sdncRequestId)
			execution.setVariable("sdncActivateRequest", sdncActivateReq)
			logger.debug("sdncActivateReq:  " + sdncActivateReq)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncActivateRollbackReq = sdncActivateReq.replace(">activate<", ">deactivate<").replace(">CreateTunnelXConnInstance<", ">DeleteTunnelXConnInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put(Prefix, "sdncActivateRollbackReq", sdncActivateRollbackReq)
			execution.setVariable("rollbackData", rollbackData)

			logger.debug("sdncActivateRollbackReq:\n" + sdncActivateRollbackReq)
			logger.debug("rollbackData:\n" + rollbackData.toString())

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCActivate. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end preProcessSDNCActivate")
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
				logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + response)

				if (!"get".equals(method))
				{
					def rollbackData = execution.getVariable("rollbackData")
					rollbackData.put(Prefix, "rollback" +  "SDNC" + method, "true")
					execution.setVariable("rollbackData", rollbackData)
				}

			}else{
				logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in validateSDNCResp. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("end ValidateSDNCResp Process")
	}

	public void preProcessSDNCGet(DelegateExecution execution){

		logger.trace("start preProcessSDNCGet")
		try{

			def callbackUrl = execution.getVariable("sdncCallbackUrl")
			// serviceOperation (URI for topology GET) will be retrieved from "selflink" from AAI if active AR exists in AAI
			// or from  "object-path" in SDNC response for assign when AR does not exist in AA

			String serviceOperation = ""

			if (execution.getVariable("foundActiveAR")) {
				def aaiQueryResponse = execution.getVariable("aaiARGetResponse")
				serviceOperation = utils.getNodeText(aaiQueryResponse, "selflink")
				logger.debug("AR service operation/aaiARSelfLink: " + serviceOperation)
			}
			else
			{
				String response = execution.getVariable("sdncAssignResponse")
				String data = utils.getNodeXml(response, "response-data")
				logger.debug("Assign responseData: " + data)
				serviceOperation = utils.getNodeText(data, "object-path")
				logger.debug("AR service operation:" + serviceOperation)
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
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occurred Processing preProcessSDNCGetRequest.", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + e.getMessage())
		}
		logger.trace("end preProcessSDNCGet")
	}
	
	public void updateAaiAROrchStatus(DelegateExecution execution, String status){

		logger.trace("start updateAaiAROrchStatus")
		String aaiARPath = execution.getVariable("aaiARPath") //set during query (existing AR) or create
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		logger.trace("end updateAaiAROrchStatus")
	}
	
	public void generateOutputs(DelegateExecution execution)
	{

		logger.trace("start generateOutputs")
		try {
			String sdncGetResponse = execution.getVariable("enhancedCallbackRequestData") //unescaped
			logger.debug("resp:" + sdncGetResponse)
			String arData = utils.getNodeXml(sdncGetResponse, "tunnelxconn-topology")
			arData = utils.removeXmlNamespaces(arData)
		
			String txca = utils.getNodeXml(arData, "tunnelxconn-assignments")
			execution.setVariable("vni", utils.getNodeText(txca, "vni"))
			execution.setVariable("vgmuxBearerIP", utils.getNodeText(txca, "vgmux-bearer-ip"))
			execution.setVariable("vgmuxLanIP", utils.getNodeText(txca, "vgmux-lan-ip"))
			
			String ari = utils.getNodeXml(arData, "allotted-resource-identifiers")
			execution.setVariable("allotedResourceName", utils.getNodeText(ari, "allotted-resource-name"))
		} catch (BpmnError e) {
			logger.debug("BPMN Error in generateOutputs ")
		} catch(Exception ex) {
			String msg = "Exception in generateOutputs " + ex.getMessage()
			logger.debug(msg)
		}
		logger.trace("end generateOutputs")
		
	}

	public void preProcessRollback (DelegateExecution execution) {

		logger.trace("start preProcessRollback")
		try {

			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				logger.debug("Prev workflowException: " + workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			logger.debug("BPMN Error during preProcessRollback")
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			logger.debug(msg)
		}
		logger.trace("end preProcessRollback")
	}

	public void postProcessRollback (DelegateExecution execution) {

		logger.trace("start postProcessRollback")
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				logger.debug("Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException);
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			logger.debug("BPMN Error during postProcessRollback")
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			logger.debug(msg)
		}
		logger.trace("end postProcessRollback")
	}

}
