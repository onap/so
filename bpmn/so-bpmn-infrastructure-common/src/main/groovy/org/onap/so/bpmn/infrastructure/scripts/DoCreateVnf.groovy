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

import static org.apache.commons.lang3.StringUtils.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.common.util.OofInfraUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.catalog.beans.HomingInstance
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class supports the DoCreateVnf building block subflow
 * with the creation of a generic vnf for
 * infrastructure.
 *
 */
class DoCreateVnf extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger( DoCreateVnf.class);
	String Prefix="DoCVNF_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
	OofInfraUtils oofInfraUtils = new OofInfraUtils()

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
		logger.debug("STARTED DoCreateVnf PreProcessRequest Process")

		// DISABLE SDNC INTERACTION FOR NOW
		execution.setVariable("SDNCInteractionEnabled", false)


		/*******************/
		try{
			// Get Variables

			def rollbackData = execution.getVariable("rollbackData")
			if (rollbackData == null) {
				rollbackData = new RollbackData()
			}

			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			String serviceModelInfo = execution.getVariable("serviceModelInfo")

			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("DoCVNF_requestId", requestId)
			execution.setVariable("mso-request-id", requestId)
			logger.debug("Incoming Request Id is: " + requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			execution.setVariable("DoCVNF_serviceInstanceId", serviceInstanceId)
			rollbackData.put("VNF", "serviceInstanceId", serviceInstanceId)
			logger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

			String vnfType = execution.getVariable("vnfType")
			execution.setVariable("DoCVNF_vnfType", vnfType)
			logger.debug("Incoming Vnf Type is: " + vnfType)

			String vnfName = execution.getVariable("vnfName")
			if (vnfName.equals("") || vnfName.equals("null")) {
				AAIResourcesClient resourceClient = new AAIResourcesClient()
				AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(execution.getVariable("vnfId")))
				if(resourceClient.exists(uri)){
					exceptionUtil.buildWorkflowException(execution, 5000, "Generic Vnf Already Exist.")
				}


			}
			execution.setVariable("DoCVNF_vnfName", vnfName)
			logger.debug("Incoming Vnf Name is: " + vnfName)

			String serviceId = execution.getVariable("productFamilyId")
			execution.setVariable("DoCVNF_serviceId", serviceId)
			logger.debug("Incoming Service Id is: " + serviceId)

			String source = "VID"
			execution.setVariable("DoCVNF_source", source)
			rollbackData.put("VNF", "source", source)
			logger.debug("Incoming Source is: " + source)

			String suppressRollback = execution.getVariable("disableRollback")
			execution.setVariable("DoCVNF_suppressRollback", suppressRollback)
			logger.debug("Incoming Suppress Rollback is: " + suppressRollback)

			String modelInvariantId = jsonUtil.getJsonValue(vnfModelInfo, "modelInvariantUuid")
			execution.setVariable("DoCVNF_modelInvariantId", modelInvariantId)
			logger.debug("Incoming Invariant Id is: " + modelInvariantId)

			String modelVersionId = jsonUtil.getJsonValue(vnfModelInfo, "modelUuid")
			if (modelVersionId == null) {
				modelVersionId = ""
			}
			execution.setVariable("DoCVNF_modelVersionId", modelVersionId)
			logger.debug("Incoming Version Id is: " + modelVersionId)

			String modelVersion = jsonUtil.getJsonValue(vnfModelInfo, "modelVersion")
			execution.setVariable("DoCVNF_modelVersion", modelVersion)
			logger.debug("Incoming Model Version is: " + modelVersion)

			String modelName = jsonUtil.getJsonValue(vnfModelInfo, "modelName")
			execution.setVariable("DoCVNF_modelName", modelName)
			logger.debug("Incoming Model Name is: " + modelName)

			String modelCustomizationId = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationUuid")
			if (modelCustomizationId == null) {
				modelCustomizationId = ""
			}
			execution.setVariable("DoCVNF_modelCustomizationId", modelCustomizationId)
			logger.debug("Incoming Model Customization Id is: " + modelCustomizationId)

			String cloudSiteId = execution.getVariable("lcpCloudRegionId")
			execution.setVariable("DoCVNF_cloudSiteId", cloudSiteId)
			rollbackData.put("VNF", "cloudSiteId", cloudSiteId)
			logger.debug("Incoming Cloud Site Id is: " + cloudSiteId)

			String tenantId = execution.getVariable("tenantId")
			execution.setVariable("DoCVNF_tenantId", tenantId)
			rollbackData.put("VNF", "tenantId", tenantId)
			logger.debug("Incoming Tenant Id is: " + tenantId)

			String globalSubscriberId = execution.getVariable("globalSubscriberId")
			if (globalSubscriberId == null) {
				globalSubscriberId = ""
			}
			execution.setVariable("DoCVNF_globalSubscriberId", globalSubscriberId)
			logger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)

			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DoCVNF_sdncVersion", sdncVersion)
			logger.debug("Incoming Sdnc Version is: " + sdncVersion)

			//For Completion Handler & Fallout Handler
			String requestInfo =
				"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>CREATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			execution.setVariable("DoCVNF_requestInfo", requestInfo)
			//TODO: Orch Status - TBD, will come from SDN-C Response in 1702
			String orchStatus = "Created"
			execution.setVariable("DoCVNF_orchStatus", orchStatus)

			//TODO: Equipment Role - Should come from SDN-C Response in 1702
			String equipmentRole = " "
			execution.setVariable("DoCVNF_equipmentRole", equipmentRole)
			String vnfId = execution.getVariable("testVnfId") // for junits
			if(isBlank(vnfId)){
				vnfId = execution.getVariable("vnfId")
				if (isBlank(vnfId)) {
					vnfId = UUID.randomUUID().toString()
					logger.debug("Generated Vnf Id is: " + vnfId)
				}
			}
			execution.setVariable("DoCVNF_vnfId", vnfId)

			// Setting for Sub Flow Calls
			execution.setVariable("DoCVNF_type", "generic-vnf")
			execution.setVariable("GENGS_type", "service-instance")

			String sdncCallbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue());
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable("DoCVNF_sdncCallbackUrl", sdncCallbackUrl)
			rollbackData.put("VNF", "sdncCallbackUrl", sdncCallbackUrl)
			logger.debug("SDNC Callback URL is: " + sdncCallbackUrl)

			VnfResource vnfResource = (VnfResource) execution.getVariable((String)"vnfResourceDecomposition")
			String nfRole = vnfResource.getNfRole()

			execution.setVariable("DoCVNF_nfRole", nfRole)
			logger.debug("NF Role is: " + nfRole)

			String nfNamingCode = vnfResource.getNfNamingCode()
			execution.setVariable("DoCVNF_nfNamingCode", nfNamingCode)
			logger.debug("NF Naming Code is: " + nfNamingCode)

			String nfType = vnfResource.getNfType()
			execution.setVariable("DoCVNF_nfType", nfType)
			logger.debug("NF Type is: " + nfType)

			String nfFunction = vnfResource.getNfFunction()
			execution.setVariable("DoCVNF_nfFunction", nfFunction)
			logger.debug("NF Function is: " + nfFunction)

			// Set Homing Info
			try {
				HomingInstance homingInstance = oofInfraUtils.getHomingInstance(serviceInstanceId, execution)
				if (homingInstance != null) {
					execution.setVariable("DoCVNF_cloudSiteId", homingInstance.getCloudRegionId())
					rollbackData.put("VNF", "cloudSiteId", homingInstance.getCloudRegionId())
					logger.debug("Overwriting cloudSiteId with homing cloudSiteId: " +
							homingInstance.getCloudRegionId())
				}
			} catch (Exception exception) {
				logger.debug("Could not find homing information for service instance: " + serviceInstanceId +
						"... continuing")
				logger.debug("Could not find homing information for service instance error: " + exception)
			}

			rollbackData.put("VNF", "rollbackSDNCAssign", "false")
			rollbackData.put("VNF", "rollbackSDNCActivate", "false")
			rollbackData.put("VNF", "rollbackVnfCreate", "false")

			execution.setVariable("rollbackData", rollbackData)

		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.debug(" Error Occured in DoCreateVnf PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		logger.trace("COMPLETED DoCreateVnf PreProcessRequest Process")
	}

	/**
	 * Gets the service instance from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('DoCVNF_serviceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))

			if(resourceClient.exists(uri)){
				Map<String, String> keys = uri.getURIKeys()
				execution.setVariable("globalCustomerId", keys.get(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId))
				execution.setVariable("serviceType", keys.get(AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType))
				execution.setVariable("GENGS_siResourceLink", uri.build().toString())

			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
			}

		}catch(BpmnError e) {
			throw e;
		}catch(Exception ex) {
			String msg = "Exception in getServiceInstance. " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	private Object getVariableEnforced(DelegateExecution execution, String name){
		Object enforced = execution.getVariable(name)
		if(!enforced){
		return "";
		}
		return enforced;
	}

	public void createGenericVnf (DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		logger.trace("STARTED DoCreateVnf CreateGenericVnf Process")
		try {
			//Get Vnf Info
			String vnfId = getVariableEnforced(execution, "DoCVNF_vnfId")
			String vnfName = getVariableEnforced(execution, "DoCVNF_vnfName")
			if (vnfName == null) {
				vnfName = "sdncGenerated"
				logger.debug("Sending a dummy VNF name to AAI - the name will be generated by SDNC: " + vnfName)
			}
			String vnfType = getVariableEnforced(execution, "DoCVNF_vnfType")
			String serviceId = getVariableEnforced(execution, "DoCVNF_serviceId")
			String orchStatus = getVariableEnforced(execution, "DoCVNF_orchStatus")
			String modelInvariantId = getVariableEnforced(execution, "DoCVNF_modelInvariantId")
			String modelVersionId = getVariableEnforced(execution, "DoCVNF_modelVersionId")
			String modelCustomizationId = getVariableEnforced(execution, "DoCVNF_modelCustomizationId")
			String equipmentRole = getVariableEnforced(execution, "DoCVNF_equipmentRole")
			String nfType = getVariableEnforced(execution, "DoCVNF_nfType")
			String nfRole = getVariableEnforced(execution, "DoCVNF_nfRole")
			String nfFunction = getVariableEnforced(execution, "DoCVNF_nfFunction")
			String nfNamingCode = getVariableEnforced(execution, "DoCVNF_nfNamingCode")

			//Get Service Instance Info
			String serviceInstanceId = getVariableEnforced(execution, "DoCVNF_serviceInstanceId")

			String globalCustId = execution.getVariable("globalCustomerId")
			String serviceType = execution.getVariable("serviceType")

			Map<String, String> payload = new LinkedHashMap<>();
			payload.put("vnf-id", vnfId);
			payload.put("vnf-name", vnfName);
			payload.put("service-id", serviceId);
			payload.put("vnf-type", vnfType);
			payload.put("prov-status", "PREPROV");
			payload.put("orchestration-status", orchStatus);
			payload.put("model-invariant-id", modelInvariantId);
			payload.put("model-version-id", modelVersionId);
			payload.put("model-customization-id", modelCustomizationId);
			payload.put("nf-type", nfType);
			payload.put("nf-role", nfRole);
			payload.put("nf-function", nfFunction);
			payload.put("nf-naming-code", nfNamingCode);

			AAIResourcesClient resourceClient = new AAIResourcesClient();
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
			resourceClient.create(uri, payload)

			AAIResourceUri siUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalCustId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
			resourceClient.connect(uri, siUri)

		}catch(Exception ex) {
			logger.debug("Error Occured in DoCreateVnf CreateGenericVnf Process: {}", ex.getMessage(), ex)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf CreateGenericVnf Process")
		}
		logger.trace("COMPLETED DoCreateVnf CreateGenericVnf Process")
	}

	public void postProcessCreateGenericVnf (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		logger.trace("STARTED DoCreateVnf PostProcessCreateGenericVnf Process")
		try {
			//Get Vnf Info
			String vnfId = execution.getVariable("DoCVNF_vnfId")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put("VNF", "vnfId", vnfId)
			rollbackData.put("VNF", "rollbackVnfCreate", "true")
			execution.setVariable("rollbackData", rollbackData)
		}catch(Exception ex) {
			logger.debug("Error Occured in DoCreateVnf PostProcessCreateGenericVnf Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PostProcessCreateGenericVnf Process")
		}
		logger.trace("COMPLETED DoCreateVnf PostProcessCreateGenericVnf Process")
	}


	public void preProcessSDNCAssignRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCAssignRequest")
		def vnfId = execution.getVariable("DoCVNF_vnfId")
		def serviceInstanceId = execution.getVariable("DoCVNF_serviceInstanceId")
		logger.debug("NEW VNF ID: " + vnfId)

		try{
			//Build SDNC Request

			String assignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "assign")

			assignSDNCRequest = utils.formatXml(assignSDNCRequest)
			execution.setVariable("DoCVNF_assignSDNCRequest", assignSDNCRequest)
			logger.debug("Outgoing AssignSDNCRequest is: \n" + assignSDNCRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessSDNCAssignRequest", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCAssignRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCAssignRequest")
	}

	public void preProcessSDNCActivateRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCActivateRequest Process")
		try{
			String vnfId = execution.getVariable("DoCVNF_vnfId")
			String serviceInstanceId = execution.getVariable("DoCVNF_serviceInstanceId")

			String activateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "activate")

			execution.setVariable("DoCVNF_activateSDNCRequest", activateSDNCRequest)
			logger.debug("Outgoing CommitSDNCRequest is: \n" + activateSDNCRequest)

		}catch(Exception e){
			logger.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
	}

	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){

				String uuid = execution.getVariable('testReqId') // for junits
				if(uuid==null){
					uuid = execution.getVariable("DoCVNF_requestId") + "-" +  	System.currentTimeMillis()
				}
				def callbackURL = execution.getVariable("DoCVNF_sdncCallbackUrl")
				def requestId = execution.getVariable("DoCVNF_requestId")
				def serviceId = execution.getVariable("DoCVNF_serviceId")
				def vnfType = execution.getVariable("DoCVNF_vnfType")
				def vnfName = execution.getVariable("DoCVNF_vnfName")
				// Only send vnfName to SDNC if it is not null, otherwise it will get generated by SDNC on Assign
				String vnfNameString = ""
				if (vnfName != null) {
					vnfNameString = """<vnf-name>${MsoUtils.xmlEscape(vnfName)}</vnf-name>"""
				}
				def tenantId = execution.getVariable("DoCVNF_tenantId")
				def source = execution.getVariable("DoCVNF_source")
				def vnfId = execution.getVariable("DoCVNF_vnfId")
				def cloudSiteId = execution.getVariable("DoCVNF_cloudSiteId")
				def modelCustomizationId = execution.getVariable("DoCVNF_modelCustomizationId")
				def serviceModelInfo = execution.getVariable("serviceModelInfo")
				def vnfModelInfo = execution.getVariable("vnfModelInfo")
				String serviceEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(serviceModelInfo)
				String vnfEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vnfModelInfo)
				def globalSubscriberId = execution.getVariable("DoCVNF_globalSubscriberId")
				def sdncVersion = execution.getVariable("DoCVNF_sdncVersion")

				String sdncVNFParamsXml = ""

				if(execution.getVariable("DoCVNF_vnfParamsExistFlag") == true){
					sdncVNFParamsXml = buildSDNCParamsXml(execution)
				}else{
					sdncVNFParamsXml = ""
				}

				String sdncRequest =
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>CreateVnfInstance</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			<subscription-service-type>${MsoUtils.xmlEscape(serviceId)}</subscription-service-type>
			${serviceEcompModelInformation}
			<service-instance-id>${MsoUtils.xmlEscape(svcInstId)}</service-instance-id>
			<global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
		</service-information>
		<vnf-information>
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
			${vnfEcompModelInformation}
		</vnf-information>
		<vnf-request-input>
			${vnfNameString}
			<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
			<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
			${sdncVNFParamsXml}
		</vnf-request-input>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			logger.debug("sdncRequest:  " + sdncRequest)
			return sdncRequest
	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
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
			logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
			if(method.equals("get")){
				String topologyGetResponse = execution.getVariable("DoCVNF_getSDNCAdapterResponse")
				String data = utils.getNodeXml(topologyGetResponse, "response-data")
				logger.debug("topologyGetResponseData: " + data)
				String vnfName = utils.getNodeText(data, "vnf-name")
				logger.debug("vnfName received from SDNC: " + vnfName)
				execution.setVariable("vnfName", vnfName)
				execution.setVariable("DoCVNF_vnfName", vnfName)
			}
			def rollbackData = execution.getVariable("rollbackData")
			if (method.equals("assign")) {
				rollbackData.put("VNF", "rollbackSDNCAssign", "true")
			}
			else if (method.equals("activate")) {
				rollbackData.put("VNF", "rollbackSDNCActivate", "true")
			}
			execution.setVariable("rollbackData", rollbackData)


		}else{
			logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		logger.debug("COMPLETED ValidateSDNCResponse Process")
	}

	public void preProcessSDNCGetRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCGetRequest Process")
		try{
			def serviceInstanceId = execution.getVariable('DoCVNF_serviceInstanceId')

			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("mso-request-id") + "-" +  System.currentTimeMillis()
			}

			def callbackUrl = execution.getVariable("DoCVFM_sdncCallbackUrl")
			logger.debug("callbackUrl:" + callbackUrl)

			def vnfId = execution.getVariable('DCVFM_vnfId')

			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vnfId
			}
			else {
				svcInstId = serviceInstanceId
			}
			// serviceOperation will be retrieved from "object-path" element
			// in SDNC Assign Response for VNF
			String response = execution.getVariable("DoCVNF_assignSDNCAdapterResponse")
			logger.debug("DoCVNF_assignSDNCAdapterResponse is: \n" + response)

			String serviceOperation = ""

			String data = utils.getNodeXml(response, "response-data")
			logger.debug("responseData: " + data)
			serviceOperation = utils.getNodeText(data, "object-path")
			logger.debug("VNF with sdncVersion of 1707 or later - service operation: " + serviceOperation)


			//!!!! TEMPORARY WORKAROUND FOR SDNC REPLICATION ISSUE
			sleep(5000)

			String SDNCGetRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
											xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
											xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
					<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
					<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
					<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
					<sdncadapter:SvcOperation>${MsoUtils.xmlEscape(serviceOperation)}</sdncadapter:SvcOperation>
					<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
					<sdncadapter:MsoAction>vfmodule</sdncadapter:MsoAction>
				</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData></sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""


			execution.setVariable("DoCVNF_getSDNCRequest", SDNCGetRequest)
			logger.debug("Outgoing GetSDNCRequest is: \n" + SDNCGetRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occurred Processing preProcessSDNCGetRequest. ", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during prepareProvision Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCGetRequest Process")
	}

	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('DoCVNF_vnfId')
			logger.debug("VNF ID: " + vnfId)

			String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						<orchestration-status>Active</orchestration-status>
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DoCVNF_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				logger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Caught exception in " + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}


}
