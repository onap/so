/*-
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

package org.openecomp.mso.bpmn.infrastructure.scripts

import java.util.UUID

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution

import static org.apache.commons.lang3.StringUtils.*
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.json.JSONObject
import org.json.JSONArray

import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils


/**
 * This class supports the CreateVnfInfra Flow
 * with the creation of a generic vnf for
 * infrastructure.
 */
class CreateVnfInfra extends AbstractServiceTaskProcessor {

	String Prefix="CREVI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	CatalogDbUtils cutils = new CatalogDbUtils()

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED CreateVnfInfra PreProcessRequest Process*** ", isDebugEnabled)
		
		setBasicDBAuthHeader(execution, isDebugEnabled)
		execution.setVariable("CREVI_sentSyncResponse", false)

		try{
			// Get Variables
			String createVnfRequest = execution.getVariable("bpmnRequest")
			execution.setVariable("CREVI_createVnfRequest", createVnfRequest)
			utils.logAudit("Incoming CreateVnfInfra Request is: \n" + createVnfRequest)

			if(createVnfRequest != null){

				String requestId = execution.getVariable("mso-request-id")
				execution.setVariable("CREVI_requestId", requestId)
				utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("CREVI_serviceInstanceId", serviceInstanceId)
				utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

				String vnfType = execution.getVariable("vnfType")
				execution.setVariable("CREVI_vnfType", vnfType)
				utils.log("DEBUG", "Incoming Vnf Type is: " + vnfType, isDebugEnabled)

				String vnfName = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.instanceName")
				execution.setVariable("CREVI_vnfName", vnfName)
				utils.log("DEBUG", "Incoming Vnf Name is: " + vnfName, isDebugEnabled)

				String serviceId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.productFamilyId")
				execution.setVariable("CREVI_serviceId", serviceId)
				utils.log("DEBUG", "Incoming Service Id is: " + serviceId, isDebugEnabled)

				String source = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.source")
				execution.setVariable("CREVI_source", source)
				utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)

				String suppressRollback = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.suppressRollback")
				execution.setVariable("CREVI_suppressRollback", suppressRollback)
				utils.log("DEBUG", "Incoming Suppress Rollback is: " + suppressRollback, isDebugEnabled)
				
				def vnfModelInfo = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo")
				execution.setVariable("CREVI_vnfModelInfo", vnfModelInfo)

				String modelInvariantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelInvariantUuid")
				execution.setVariable("CREVI_modelInvariantId", modelInvariantId)
				utils.log("DEBUG", "Incoming Invariant Id is: " + modelInvariantId, isDebugEnabled)

				String modelVersion = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelVersion")
				execution.setVariable("CREVI_modelVersion", modelVersion)
				utils.log("DEBUG", "Incoming Model Version is: " + modelVersion, isDebugEnabled)
				
				def cloudConfiguration = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration")
				execution.setVariable("CREVI_cloudConfiguration", cloudConfiguration)
				
				String cloudSiteId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
				execution.setVariable("CREVI_cloudSiteId", cloudSiteId)
				utils.log("DEBUG", "Incoming Cloud Site Id is: " + cloudSiteId, isDebugEnabled)
				
				String tenantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration.tenantId")
				execution.setVariable("CREVI_tenantId", tenantId)
				utils.log("DEBUG", "Incoming Tenant Id is: " + tenantId, isDebugEnabled)

				//For Completion Handler & Fallout Handler
				String requestInfo =
				"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""

				execution.setVariable("CREVI_requestInfo", requestInfo)

				//TODO: Orch Status - TBD, will come from SDN-C Response in 1702
				String orchStatus = "Created"
				execution.setVariable("CREVI_orchStatus", orchStatus)

				//TODO: Equipment Role - Should come from SDN-C Response in 1702
				String equipmentRole = " "
				execution.setVariable("CREVI_equipmentRole", equipmentRole)

				String vnfId = execution.getVariable("testVnfId") // for junits
				if(isBlank(vnfId)){
					vnfId = UUID.randomUUID().toString()
					utils.log("DEBUG", "Generated Vnf Id is: " + vnfId, isDebugEnabled)
				}
				execution.setVariable("CREVI_vnfId", vnfId)

				// Setting for Sub Flow Calls
				execution.setVariable("CREVI_type", "generic-vnf")
				execution.setVariable("GENGS_type", "service-instance")
				
				String sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
				if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
					def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
					logError(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}
				execution.setVariable("CREVI_sdncCallbackUrl", sdncCallbackUrl)
				
				def vnfInputParameters = null
				try {
					vnfInputParameters = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestParameters.userParams")
				}
				catch (Exception e) {
					utils.log("DEBUG", "userParams are not present in the request", isDebugEnabled)
				}
				execution.setVariable("CREVI_vnfInputParameters", vnfInputParameters)
				
				
				utils.logAudit("SDNC Callback URL: " + sdncCallbackUrl)
				logDebug("SDNC Callback URL is: " + sdncCallbackUrl, isDebugEnabled)

			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Bpmn Request is Null.")
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in CreateVnfInfra PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra PreProcessRequest Process ***", isDebugEnabled)
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra SendSyncResponse Process *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("CREVI_requestId")
			String vnfId = execution.getVariable("CREVI_vnfId")

			String createVnfResponse = """{"requestReferences":{"instanceId":"${vnfId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " CreateVnfInfra Sync Response is: \n"  + createVnfResponse, isDebugEnabled)

			sendWorkflowResponse(execution, 202, createVnfResponse)

			execution.setVariable("CREVI_sentSyncResponse", true)

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra SendSyncResponse Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra SendSyncResponse Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra SendSyncResponse Process ***", isDebugEnabled)
	}

	
	public void preProcessSDNCAssignRequest(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
		def vnfId = execution.getVariable("CREVI_vnfId")		
		def serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")
		logDebug("NEW VNF ID: " + vnfId, isDebugLogEnabled)
		utils.logAudit("NEW VNF ID: " + vnfId)

		try{
			//Build SDNC Request
			
			String assignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "assign")

			assignSDNCRequest = utils.formatXml(assignSDNCRequest)
			execution.setVariable("CREVI_assignSDNCRequest", assignSDNCRequest)
			logDebug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCAssignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareProvision Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	public void preProcessSDNCActivateRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCActivateRequest Process ======== ", isDebugLogEnabled)
		try{
			String vnfId = execution.getVariable("CREVI_vnfId")			
			String serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")

			String activateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "activate")

			execution.setVariable("CREVI_activateSDNCRequest", activateSDNCRequest)
			logDebug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing CommitSDNCRequest is: \n"  + activateSDNCRequest)

		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCActivateRequest Process ======== ", isDebugLogEnabled)
	}
	
	public String buildSDNCRequest(Execution execution, String svcInstId, String action){
		
				String uuid = execution.getVariable('testReqId') // for junits
				if(uuid==null){
					uuid = execution.getVariable("mso-request-id") + "-" +  	System.currentTimeMillis()
				}
				def callbackURL = execution.getVariable("CREVI_sdncCallbackUrl")
				def requestId = execution.getVariable("CREVI_requestId")
				def serviceId = execution.getVariable("CREVI_serviceId")
				def vnfType = execution.getVariable("CREVI_vnfType")
				def vnfName = execution.getVariable("CREVI_vnfName")
				def tenantId = execution.getVariable("CREVI_tenantId")
				def source = execution.getVariable("CREVI_source")				
				def vnfId = execution.getVariable("CREVI_vnfId")
				def cloudSiteId = execution.getVariable("CREVI_cloudSiteId")
		
				String sdncVNFParamsXml = ""
		
				if(execution.getVariable("CREVI_vnfParamsExistFlag") == true){
					sdncVNFParamsXml = buildSDNCParamsXml(execution)
				}else{
					sdncVNFParamsXml = ""
				}
		
				String sdncRequest =
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>VNFActivateRequest</request-action>
			<source>${source}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${serviceId}</service-id>
			<service-type>${serviceId}</service-type>
			<service-instance-id>${svcInstId}</service-instance-id>
			<subscriber-name>notsurewecare</subscriber-name>
		</service-information>
		<vnf-request-information>			
			<vnf-id>${vnfId}</vnf-id>
			<vnf-name>${vnfName}</vnf-name>
			<vnf-type>${vnfType}</vnf-type>
			<aic-cloud-region>${cloudSiteId}</aic-cloud-region>
			<tenant>${tenantId}</tenant>
		${sdncVNFParamsXml}
		</vnf-request-information>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
		
			utils.logAudit("sdncRequest:  " + sdncRequest)
			return sdncRequest		
	}
		
	public void validateSDNCResponse(Execution execution, String response, String method){
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		logDebug(" *** STARTED ValidateSDNCResponse Process*** ", isDebugLogEnabled)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		utils.logAudit("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		utils.logAudit("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logDebug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse, isDebugLogEnabled)
			
		}else{
			logDebug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		logDebug(" *** COMPLETED ValidateSDNCResponse Process*** ", isDebugLogEnabled)
	}

	public void prepareCompletionHandlerRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra PrepareCompletionHandlerRequest Process *** ", isDebugEnabled)

		try {
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			String vnfId = execution.getVariable("CREVI_vnfId")
			requestInfo = utils.removeXmlPreamble(requestInfo)

			String request =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
									xmlns:ns="http://org.openecomp/mso/request/types/v1">
							${requestInfo}
							<status-message>Vnf has been created successfully.</status-message>
							<vnfId>${vnfId}</vnfId>
							<mso-bpel-name>CreateVnfInfra</mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			execution.setVariable("CREVI_completionHandlerRequest", request)
			utils.log("DEBUG", "Completion Handler Request is: " + request, isDebugEnabled)

			execution.setVariable("WorkflowResponse", "Success") // for junits

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra PrepareCompletionHandlerRequest Process ***", isDebugEnabled)
	}

	public void sendErrorResponse(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra sendErrorResponse Process *** ", isDebugEnabled)
		try {
			def sentSyncResponse = execution.getVariable("CREVI_sentSyncResponse")
			if(sentSyncResponse == false){
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)

				utils.logAudit(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				utils.log("DEBUG", "Not Sending Error Response.  Sync Response Already Sent", isDebugEnabled)
			}

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra sendErrorResponse Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra sendErrorResponse Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra sendErrorResponse Process ***", isDebugEnabled)
	}

	public void prepareFalloutRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra prepareFalloutRequest Process *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Incoming Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			utils.log("DEBUG", " Incoming Request Info: " + requestInfo, isDebugEnabled)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable("CREVI_falloutRequest", falloutRequest)


		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra prepareFalloutRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra prepareFalloutRequest Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra prepareFalloutRequest Process ***", isDebugEnabled)
	}

	
	public void queryCatalogDB (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra QueryCatalogDB Process *** ", isDebugEnabled)
		try {
			//Get Vnf Info
			String vnfModelInfo = execution.getVariable("CREVI_vnfModelInfo")
			String vnfModelCustomizationUuid = jsonUtil.getJsonValueForKey(vnfModelInfo, "modelCustomizationUuid")
			utils.log("DEBUG", "querying Catalog DB by vnfModelCustomizationUuid: " + vnfModelCustomizationUuid, isDebugEnabled)
						
			JSONArray vnfs = cutils.getAllVnfsByVnfModelCustomizationUuid(execution,
							vnfModelCustomizationUuid, "v2")
			utils.log("DEBUG", "obtained VNF list: " + vnfs, isDebugEnabled)			
			execution.setVariable("CREVI_vnfs", vnfs)
			
			if (vnfs == null) {
				utils.log("ERROR", "No matching VNFs in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "No matching VNFs in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid)
			}
			
			// Only one match here
			JSONObject vnf = vnfs.get(0)
			
			if (vnf == null) {
				utils.log("ERROR", "No matching VNF in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "No matching VNF in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid)
			}			
			
			VnfResource vnfResource = new VnfResource()
			String nfType = jsonUtil.getJsonValueForKey(vnf, "nfType")
			vnfResource.setNfType(nfType)
			String nfRole = jsonUtil.getJsonValueForKey(vnf, "nfRole")
			vnfResource.setNfRole(nfRole)
			String nfFunction = jsonUtil.getJsonValueForKey(vnf, "nfFunction")
			vnfResource.setNfFunction(nfFunction)
			String nfNamingCode = jsonUtil.getJsonValueForKey(vnf, "nfNamingCode")
			vnfResource.setNfNamingCode(nfNamingCode)
			
			execution.setVariable("CREVI_vnfResourceDecomposition", vnfResource)
			
		}catch(BpmnError e) {
			throw e
		}catch(Exception ex) {
			utils.log("DEBUG", "Error Occurred in CreateVnfInfra QueryCatalogDB Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in CreateVnfInfra QueryCatalogDB Process")
		}
		
		
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra QueryCatalogDb Process ***", isDebugEnabled)
	}
}
