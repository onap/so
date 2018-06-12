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

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;

import static org.apache.commons.lang3.StringUtils.*;
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils;
import org.json.JSONObject;
import org.json.JSONArray;

import org.openecomp.mso.bpmn.common.scripts.AaiUtil;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.bpmn.infrastructure.aai.groovyflows.AAICreateResources;
import org.openecomp.mso.bpmn.core.UrnPropertiesReader
import org.openecomp.mso.logger.MsoLogger
import org.openecomp.mso.logger.MessageEnum


/**
 * This class supports the CreateVnfInfra Flow
 * with the creation of a generic vnf for
 * infrastructure.
 */
class CreateVnfInfra extends AbstractServiceTaskProcessor {
	
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateVnfInfra.class);
	

	String Prefix="CREVI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	CatalogDbUtils cutils = new CatalogDbUtils()
	AAICreateResources aaiCR = new AAICreateResources()

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED CreateVnfInfra PreProcessRequest Process")
		
		setBasicDBAuthHeader(execution, isDebugEnabled)
		execution.setVariable("CREVI_sentSyncResponse", false)

		try{
			// Get Variables
			String createVnfRequest = execution.getVariable("bpmnRequest")
			execution.setVariable("CREVI_createVnfRequest", createVnfRequest)
			msoLogger.debug("Incoming CreateVnfInfra Request is: \n" + createVnfRequest)

			if(createVnfRequest != null){

				String requestId = execution.getVariable("mso-request-id")
				execution.setVariable("CREVI_requestId", requestId)
				msoLogger.debug("Incoming Request Id is: " + requestId)

				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("CREVI_serviceInstanceId", serviceInstanceId)
				msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

				String vnfType = execution.getVariable("vnfType")
				execution.setVariable("CREVI_vnfType", vnfType)
				msoLogger.debug("Incoming Vnf Type is: " + vnfType)

				String vnfName = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.instanceName")
				execution.setVariable("CREVI_vnfName", vnfName)
				msoLogger.debug("Incoming Vnf Name is: " + vnfName)

				String serviceId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.productFamilyId")
				execution.setVariable("CREVI_serviceId", serviceId)
				msoLogger.debug("Incoming Service Id is: " + serviceId)

				String source = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.source")
				execution.setVariable("CREVI_source", source)
				msoLogger.debug("Incoming Source is: " + source)

				String suppressRollback = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.suppressRollback")
				execution.setVariable("CREVI_suppressRollback", suppressRollback)
				msoLogger.debug("Incoming Suppress Rollback is: " + suppressRollback)
				
				def vnfModelInfo = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo")
				execution.setVariable("CREVI_vnfModelInfo", vnfModelInfo)

				String modelInvariantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelInvariantUuid")
				execution.setVariable("CREVI_modelInvariantId", modelInvariantId)
				msoLogger.debug("Incoming Invariant Id is: " + modelInvariantId)

				String modelVersion = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelVersion")
				execution.setVariable("CREVI_modelVersion", modelVersion)
				msoLogger.debug("Incoming Model Version is: " + modelVersion)
				
				def cloudConfiguration = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration")
				execution.setVariable("CREVI_cloudConfiguration", cloudConfiguration)
				
				String cloudSiteId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
				execution.setVariable("CREVI_cloudSiteId", cloudSiteId)
				msoLogger.debug("Incoming Cloud Site Id is: " + cloudSiteId)
				
				String tenantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration.tenantId")
				execution.setVariable("CREVI_tenantId", tenantId)
				msoLogger.debug("Incoming Tenant Id is: " + tenantId)

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
					msoLogger.debug("Generated Vnf Id is: " + vnfId)
				}
				execution.setVariable("CREVI_vnfId", vnfId)

				// Setting for Sub Flow Calls
				execution.setVariable("CREVI_type", "generic-vnf")
				execution.setVariable("GENGS_type", "service-instance")
				
				String sdncCallbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
				if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
					def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
					msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError);
					
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}
				execution.setVariable("CREVI_sdncCallbackUrl", sdncCallbackUrl)
				
				def vnfInputParameters = null
				try {
					vnfInputParameters = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestParameters.userParams")
				}
				catch (Exception e) {
					msoLogger.debug("userParams are not present in the request")
				}
				execution.setVariable("CREVI_vnfInputParameters", vnfInputParameters)
				
				
				msoLogger.debug("SDNC Callback URL: " + sdncCallbackUrl)
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Bpmn Request is Null.")
			}

		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error Occurred in CreateVnfInfra PreProcessRequest method", "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in CreateVnfInfra PreProcessRequest")

		}
		msoLogger.trace("COMPLETED CreateVnfInfra PreProcessRequest Process")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)

		msoLogger.trace("STARTED CreateVnfInfra SendSyncResponse Process")

		try {
			String requestId = execution.getVariable("CREVI_requestId")
			String vnfId = execution.getVariable("CREVI_vnfId")

			String createVnfResponse = """{"requestReferences":{"instanceId":"${vnfId}","requestId":"${requestId}"}}""".trim()

			msoLogger.debug("CreateVnfInfra Sync Response is: \n"  + createVnfResponse)

			sendWorkflowResponse(execution, 202, createVnfResponse)

			execution.setVariable("CREVI_sentSyncResponse", true)

		} catch (Exception ex) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error Occurred in CreateVnfInfra SendSyncResponse Process", "BPMN", MsoLogger.getServiceName(),
				MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra SendSyncResponse Process")

		}
		msoLogger.trace("COMPLETED CreateVnfInfra SendSyncResponse Process")
	}

	
	public void preProcessSDNCAssignRequest(DelegateExecution execution){
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessSDNCAssignRequest")
		def vnfId = execution.getVariable("CREVI_vnfId")		
		def serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")
		msoLogger.debug("NEW VNF ID: " + vnfId)

		try{
			//Build SDNC Request
			
			String assignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "assign")

			assignSDNCRequest = utils.formatXml(assignSDNCRequest)
			execution.setVariable("CREVI_assignSDNCRequest", assignSDNCRequest)
			msoLogger.debug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occurred Processing preProcessSDNCAssignRequest", "BPMN", MsoLogger.getServiceName(),
				MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareProvision Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED preProcessSDNCAssignRequest")
	}
	
	public void preProcessSDNCActivateRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessSDNCActivateRequest Process")
		try{
			String vnfId = execution.getVariable("CREVI_vnfId")			
			String serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")

			String activateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "activate")

			execution.setVariable("CREVI_activateSDNCRequest", activateSDNCRequest)
			msoLogger.debug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest)

		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing preProcessSDNCActivateRequest", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
	}
	
	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){
		
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
		
			msoLogger.debug("sdncRequest:  " + sdncRequest)
			return sdncRequest		
	}
		
	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
		execution.setVariable("prefix",Prefix)
		msoLogger.debug("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		msoLogger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		msoLogger.debug("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			msoLogger.trace("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
			
		}else{
			msoLogger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		msoLogger.trace("COMPLETED ValidateSDNCResponse Process")
	}

	public void prepareCompletionHandlerRequest(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		msoLogger.trace("STARTED CreateVnfInfra PrepareCompletionHandlerRequest Process")

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
			msoLogger.debug("Completion Handler Request is: " + request)

			execution.setVariable("WorkflowResponse", "Success") // for junits

		} catch (Exception ex) {
			msoLogger.debug("Error Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process")

		}
		msoLogger.trace("COMPLETED CreateVnfInfra PrepareCompletionHandlerRequest Process")
	}

	public void sendErrorResponse(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		msoLogger.trace("STARTED CreateVnfInfra sendErrorResponse Process")
		try {
			def sentSyncResponse = execution.getVariable("CREVI_sentSyncResponse")
			if(sentSyncResponse == false){
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)

				msoLogger.debug(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				msoLogger.debug("Not Sending Error Response.  Sync Response Already Sent")
			}

		} catch (Exception ex) {
			msoLogger.debug("Error Occured in CreateVnfInfra sendErrorResponse Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra sendErrorResponse Process")

		}
		msoLogger.trace("COMPLETED CreateVnfInfra sendErrorResponse Process")
	}

	public void prepareFalloutRequest(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		msoLogger.trace("STARTED CreateVnfInfra prepareFalloutRequest Process")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			msoLogger.debug(" Incoming Workflow Exception: " + wfex.toString())
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			msoLogger.debug(" Incoming Request Info: " + requestInfo)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable("CREVI_falloutRequest", falloutRequest)


		} catch (Exception ex) {
			msoLogger.debug("Error Occured in CreateVnfInfra prepareFalloutRequest Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra prepareFalloutRequest Process")

		}
		msoLogger.trace("COMPLETED CreateVnfInfra prepareFalloutRequest Process")
	}

	
	public void queryCatalogDB (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)

		msoLogger.trace("STARTED CreateVnfInfra QueryCatalogDB Process")
		try {
			//Get Vnf Info
			String vnfModelInfo = execution.getVariable("CREVI_vnfModelInfo")
			String vnfModelCustomizationUuid = jsonUtil.getJsonValueForKey(vnfModelInfo, "modelCustomizationUuid")
			msoLogger.debug("querying Catalog DB by vnfModelCustomizationUuid: " + vnfModelCustomizationUuid)
						
			JSONArray vnfs = cutils.getAllVnfsByVnfModelCustomizationUuid(execution,
							vnfModelCustomizationUuid, "v2")
			msoLogger.debug("obtained VNF list: " + vnfs)
			
			if (vnfs == null) {
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "No matching VNFs in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "No matching VNFs in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid)
			}
			
			// Only one match here
			JSONObject vnf = vnfs.get(0)
			
			if (vnf == null) {
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "No matching VNF in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
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
			throw e;			
		}catch(Exception ex) {
			msoLogger.debug("Error Occurred in CreateVnfInfra QueryCatalogDB Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in CreateVnfInfra QueryCatalogDB Process")
		}
		
		
		msoLogger.trace("COMPLETED CreateVnfInfra QueryCatalogDb Process")
	}
	public void createPlatform (DelegateExecution execution) {
		msoLogger.trace("START createPlatform")
		
		String request = execution.getVariable("bpmnRequest")
		String platformName = jsonUtil.getJsonValue(request, "requestDetails.platform.platformName")
		String vnfId = execution.getVariable("CREVI_vnfId")
	
		msoLogger.debug("Platform NAME: " + platformName)
		msoLogger.debug("VnfID: " + vnfId)
		
		if(platformName == null||platformName.equals("")){
			String msg = "Exception in createPlatform. platformName was not found in the request.";
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}else{
			msoLogger.debug("platformName was found.")
			try{
				AAICreateResources aaiCR = new AAICreateResources()
				aaiCR.createAAIPlatform(platformName, vnfId)
			}catch(Exception ex){
				String msg = "Exception in createPlatform. " + ex.getMessage();
				msoLogger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
		msoLogger.trace("Exit createPlatform")
	}
	public void createLineOfBusiness (DelegateExecution execution) {
		msoLogger.trace("START createLineOfBusiness")
		
		String request = execution.getVariable("bpmnRequest")
		String lineOfBusiness = jsonUtil.getJsonValue(request, "requestDetails.lineOfBusiness.lineOfBusinessName")
		String vnfId = execution.getVariable("CREVI_vnfId")
	
		msoLogger.debug("LineOfBusiness NAME: " + lineOfBusiness)
		msoLogger.debug("VnfID: " + vnfId)
		
		if(lineOfBusiness == null || lineOfBusiness.equals("")){
			msoLogger.debug("LineOfBusiness was not found. Continuing on with flow...")
		}else{
			msoLogger.debug("LineOfBusiness was found.")
			try{
				AAICreateResources aaiCR = new AAICreateResources()
				aaiCR.createAAILineOfBusiness(lineOfBusiness, vnfId)
			}catch(Exception ex){
				String msg = "Exception in LineOfBusiness. " + ex.getMessage();
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + ex)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
		msoLogger.trace("Exit createLineOfBusiness")
	}
}
