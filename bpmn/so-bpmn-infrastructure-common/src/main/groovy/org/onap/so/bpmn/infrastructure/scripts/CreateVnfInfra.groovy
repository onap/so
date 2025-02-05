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

import org.onap.so.logger.LoggingAnchor
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.logging.filter.base.ErrorCode

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.CatalogDbUtils;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils;
import org.onap.so.bpmn.common.scripts.VidUtils;
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.infrastructure.aai.groovyflows.AAICreateResources;
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * This class supports the CreateVnfInfra Flow
 * with the creation of a generic vnf for
 * infrastructure.
 */
class CreateVnfInfra extends AbstractServiceTaskProcessor {
	
    private static final Logger logger = LoggerFactory.getLogger( CreateVnfInfra.class);
	

	String Prefix="CREVI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

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
		logger.trace("STARTED CreateVnfInfra PreProcessRequest Process")
		
		setBasicDBAuthHeader(execution, isDebugEnabled)
		execution.setVariable("CREVI_sentSyncResponse", false)

		try{
			// Get Variables
			String createVnfRequest = execution.getVariable("bpmnRequest")
			execution.setVariable("CREVI_createVnfRequest", createVnfRequest)
			logger.debug("Incoming CreateVnfInfra Request is: \n" + createVnfRequest)

			if(createVnfRequest != null){

				String requestId = execution.getVariable("mso-request-id")
				execution.setVariable("CREVI_requestId", requestId)
				logger.debug("Incoming Request Id is: " + requestId)

				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("CREVI_serviceInstanceId", serviceInstanceId)
				logger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

				String vnfType = execution.getVariable("vnfType")
				execution.setVariable("CREVI_vnfType", vnfType)
				logger.debug("Incoming Vnf Type is: " + vnfType)

				String vnfName = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.instanceName")
				execution.setVariable("CREVI_vnfName", vnfName)
				logger.debug("Incoming Vnf Name is: " + vnfName)

				String serviceId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.productFamilyId")
				execution.setVariable("CREVI_serviceId", serviceId)
				logger.debug("Incoming Service Id is: " + serviceId)

				String source = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.source")
				execution.setVariable("CREVI_source", source)
				logger.debug("Incoming Source is: " + source)

				String suppressRollback = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.suppressRollback")
				execution.setVariable("CREVI_suppressRollback", suppressRollback)
				logger.debug("Incoming Suppress Rollback is: " + suppressRollback)
				
				def vnfModelInfo = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo")
				execution.setVariable("CREVI_vnfModelInfo", vnfModelInfo)

				String modelInvariantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelInvariantUuid")
				execution.setVariable("CREVI_modelInvariantId", modelInvariantId)
				logger.debug("Incoming Invariant Id is: " + modelInvariantId)

				String modelVersion = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelVersion")
				execution.setVariable("CREVI_modelVersion", modelVersion)
				logger.debug("Incoming Model Version is: " + modelVersion)
				
				def cloudConfiguration = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration")
				execution.setVariable("CREVI_cloudConfiguration", cloudConfiguration)
				
				String cloudSiteId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
				execution.setVariable("CREVI_cloudSiteId", cloudSiteId)
				logger.debug("Incoming Cloud Site Id is: " + cloudSiteId)
				
				String tenantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.cloudConfiguration.tenantId")
				execution.setVariable("CREVI_tenantId", tenantId)
				logger.debug("Incoming Tenant Id is: " + tenantId)

				//For Completion Handler & Fallout Handler
				String requestInfo =
				"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>CREATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
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
					logger.debug("Generated Vnf Id is: " + vnfId)
				}
				execution.setVariable("CREVI_vnfId", vnfId)

				// Setting for Sub Flow Calls
				execution.setVariable("CREVI_type", "generic-vnf")
				execution.setVariable("GENGS_type", "service-instance")
				
				String sdncCallbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
				if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
					def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
					logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
							ErrorCode.UnknownError.getValue());
					
					exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
				}
				execution.setVariable("CREVI_sdncCallbackUrl", sdncCallbackUrl)
				
				def vnfInputParameters = null
				try {
					vnfInputParameters = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestParameters.userParams")
				}
				catch (Exception e) {
					logger.debug("userParams are not present in the request")
				}
				execution.setVariable("CREVI_vnfInputParameters", vnfInputParameters)
				
				
				logger.debug("SDNC Callback URL: " + sdncCallbackUrl)
			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Bpmn Request is Null.")
			}

		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), " Error Occurred in " +
					"CreateVnfInfra PreProcessRequest method", "BPMN", ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in CreateVnfInfra PreProcessRequest")

		}
		logger.trace("COMPLETED CreateVnfInfra PreProcessRequest Process")
	}

	public void sendSyncResponse (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED CreateVnfInfra SendSyncResponse Process")

		try {
			String requestId = execution.getVariable("CREVI_requestId")
			String vnfId = execution.getVariable("CREVI_vnfId")

			String createVnfResponse = """{"requestReferences":{"instanceId":"${vnfId}","requestId":"${requestId}"}}""".trim()

			logger.debug("CreateVnfInfra Sync Response is: \n"  + createVnfResponse)

			sendWorkflowResponse(execution, 202, createVnfResponse)

			execution.setVariable("CREVI_sentSyncResponse", true)

		} catch (Exception ex) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), " Error Occurred in CreateVnfInfra SendSyncResponse Process", "BPMN",
				ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra SendSyncResponse Process")

		}
		logger.trace("COMPLETED CreateVnfInfra SendSyncResponse Process")
	}

	
	public void preProcessSDNCAssignRequest(DelegateExecution execution){
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCAssignRequest")
		def vnfId = execution.getVariable("CREVI_vnfId")		
		def serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")
		logger.debug("NEW VNF ID: " + vnfId)

		try{
			//Build SDNC Request
			
			String assignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "assign")

			assignSDNCRequest = utils.formatXml(assignSDNCRequest)
			execution.setVariable("CREVI_assignSDNCRequest", assignSDNCRequest)
			logger.debug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Exception Occurred Processing preProcessSDNCAssignRequest", "BPMN",
				ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareProvision Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCAssignRequest")
	}
	
	public void preProcessSDNCActivateRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCActivateRequest Process")
		try{
			String vnfId = execution.getVariable("CREVI_vnfId")			
			String serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")

			String activateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "activate")

			execution.setVariable("CREVI_activateSDNCRequest", activateSDNCRequest)
			logger.debug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Exception Occured " +
					"Processing preProcessSDNCActivateRequest", "BPMN", ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
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
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(requestId)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>VNFActivateRequest</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
		</request-information>
		<service-information>
			<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			<service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
			<service-instance-id>${MsoUtils.xmlEscape(svcInstId)}</service-instance-id>
			<subscriber-name>notsurewecare</subscriber-name>
		</service-information>
		<vnf-request-information>			
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-name>${MsoUtils.xmlEscape(vnfName)}</vnf-name>
			<vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
			<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
			<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
		${sdncVNFParamsXml}
		</vnf-request-information>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
		
			logger.debug("sdncRequest:  " + sdncRequest)
			return sdncRequest		
	}
		
	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
		execution.setVariable("prefix",Prefix)
		logger.debug("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		logger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		logger.debug("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logger.trace("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
			
		}else{
			logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		logger.trace("COMPLETED ValidateSDNCResponse Process")
	}

	public void prepareCompletionHandlerRequest(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED CreateVnfInfra PrepareCompletionHandlerRequest Process")

		try {
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			String vnfId = execution.getVariable("CREVI_vnfId")
			requestInfo = utils.removeXmlPreamble(requestInfo)

			String request =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
									xmlns:ns="http://org.onap/so/request/types/v1">
							${requestInfo}
							<status-message>Vnf has been created successfully.</status-message>
							<vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
							<mso-bpel-name>CreateVnfInfra</mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			execution.setVariable("CREVI_completionHandlerRequest", request)
			logger.debug("Completion Handler Request is: " + request)

			execution.setVariable("WorkflowResponse", "Success") // for junits

		} catch (Exception ex) {
			logger.debug("Error Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process")

		}
		logger.trace("COMPLETED CreateVnfInfra PrepareCompletionHandlerRequest Process")
	}

	public void sendErrorResponse(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED CreateVnfInfra sendErrorResponse Process")
		try {
			def sentSyncResponse = execution.getVariable("CREVI_sentSyncResponse")
			if(sentSyncResponse == false){
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)

				logger.debug(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				logger.debug("Not Sending Error Response.  Sync Response Already Sent")
			}

		} catch (Exception ex) {
			logger.debug("Error Occured in CreateVnfInfra sendErrorResponse Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra sendErrorResponse Process")

		}
		logger.trace("COMPLETED CreateVnfInfra sendErrorResponse Process")
	}

	public void prepareFalloutRequest(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED CreateVnfInfra prepareFalloutRequest Process")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			logger.debug(" Incoming Workflow Exception: " + wfex.toString())
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			logger.debug(" Incoming Request Info: " + requestInfo)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable("CREVI_falloutRequest", falloutRequest)


		} catch (Exception ex) {
			logger.debug("Error Occured in CreateVnfInfra prepareFalloutRequest Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra prepareFalloutRequest Process")

		}
		logger.trace("COMPLETED CreateVnfInfra prepareFalloutRequest Process")
	}

	
	public void queryCatalogDB (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED CreateVnfInfra QueryCatalogDB Process")
		try {
			//Get Vnf Info
			String vnfModelInfo = execution.getVariable("CREVI_vnfModelInfo")
			String vnfModelCustomizationUuid = jsonUtil.getJsonValueForKey(vnfModelInfo, "modelCustomizationUuid")
			logger.debug("querying Catalog DB by vnfModelCustomizationUuid: " + vnfModelCustomizationUuid)
						
			JSONArray vnfs = catalogDbUtils.getAllVnfsByVnfModelCustomizationUuid(execution,
							vnfModelCustomizationUuid, "v2")
			logger.debug("obtained VNF list: " + vnfs)
			
			if (vnfs == null) {
				logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "No matching " +
						"VNFs in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid, "BPMN", ErrorCode.UnknownError.getValue(), "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "No matching VNFs in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid)
			}
			
			// Only one match here
			JSONObject vnf = vnfs.get(0)
			
			if (vnf == null) {
				logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "No matching VNF" +
						" in Catalog DB for vnfModelCustomizationUuid=" + vnfModelCustomizationUuid, "BPMN", ErrorCode.UnknownError.getValue(), "");
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
			logger.debug("Error Occurred in CreateVnfInfra QueryCatalogDB Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occurred in CreateVnfInfra QueryCatalogDB Process")
		}
		
		
		logger.trace("COMPLETED CreateVnfInfra QueryCatalogDb Process")
	}
	public void createPlatform (DelegateExecution execution) {
		logger.trace("START createPlatform")
		
		String request = execution.getVariable("bpmnRequest")
		String platformName = jsonUtil.getJsonValue(request, "requestDetails.platform.platformName")
		String vnfId = execution.getVariable("CREVI_vnfId")
	
		logger.debug("Platform NAME: " + platformName)
		logger.debug("VnfID: " + vnfId)
		
		if(platformName == null||platformName.equals("")){
			String msg = "Exception in createPlatform. platformName was not found in the request.";
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}else{
			logger.debug("platformName was found.")
			try{
				AAICreateResources aaiCR = new AAICreateResources()
				aaiCR.createAAIPlatform(platformName, vnfId)
			}catch(Exception ex){
				String msg = "Exception in createPlatform. " + ex.getMessage();
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
		logger.trace("Exit createPlatform")
	}
	public void createLineOfBusiness (DelegateExecution execution) {
		logger.trace("START createLineOfBusiness")
		
		String request = execution.getVariable("bpmnRequest")
		String lineOfBusiness = jsonUtil.getJsonValue(request, "requestDetails.lineOfBusiness.lineOfBusinessName")
		String vnfId = execution.getVariable("CREVI_vnfId")
	
		logger.debug("LineOfBusiness NAME: " + lineOfBusiness)
		logger.debug("VnfID: " + vnfId)
		
		if(lineOfBusiness == null || lineOfBusiness.equals("")){
			logger.debug("LineOfBusiness was not found. Continuing on with flow...")
		}else{
			logger.debug("LineOfBusiness was found.")
			try{
				AAICreateResources aaiCR = new AAICreateResources()
				aaiCR.createAAILineOfBusiness(lineOfBusiness, vnfId)
			}catch(Exception ex){
				String msg = "Exception in LineOfBusiness. " + ex.getMessage();
				logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue(), "Exception is:\n" + ex)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
		logger.trace("Exit createLineOfBusiness")
	}
}
