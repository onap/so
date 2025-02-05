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
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.VfModule
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ModuleResource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class supports the macro VID Flow
 * with the deletion of a generic vnf and related VF modules.
 */
class DoDeleteVnfAndModules extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteVnfAndModules.class);

	String Prefix="DDVAM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *	
	 */
	public void preProcessRequest(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		logger.trace("STARTED DoDeleteVnfAndModules PreProcessRequest Process")

		try{
			// Get Variables				
			
			String cloudConfiguration = execution.getVariable("cloudConfiguration")		
			logger.debug("Cloud Configuration: " + cloudConfiguration)	
			
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("requestId", requestId)			
			execution.setVariable("mso-request-id", requestId)
			logger.debug("Incoming Request Id is: " + requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")			
			logger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

			String vnfId = execution.getVariable("vnfId")			
			logger.debug("Incoming Vnf Id is: " + vnfId)			
			
			String source = "VID"
			execution.setVariable("DDVAM_source", source)
			logger.debug("Incoming Source is: " + source)
			
			execution.setVariable("DDVAM_isVidRequest", "true")
			
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DDVAM_sdncVersion", sdncVersion)
			logger.debug("Incoming Sdnc Version is: " + sdncVersion)
			
			// Set aLaCarte flag to false
			execution.setVariable("aLaCarte", false)
			
			String sdncCallbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback", execution)
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
				logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
						ErrorCode.UnknownError.getValue(), "Exception");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			logger.debug("SDNC Callback URL: " + sdncCallbackUrl)
			logger.debug("SDNC Callback URL is: " + sdncCallbackUrl)	
			
			
			if (!sdncVersion.equals("1702")) {
				//String vnfModelInfo = execution.getVariable("vnfModelInfo")
				//String serviceModelInfo = execution.getVariable("serviceModelInfo")
				
				String serviceId = execution.getVariable("productFamilyId")
				execution.setVariable("DDVAM_serviceId", serviceId)
				logger.debug("Incoming Service Id is: " + serviceId)
				
					
				//String modelInvariantId = jsonUtil.getJsonValue(vnfModelInfo, "modelInvariantId")
				//execution.setVariable("DDVAM_modelInvariantId", modelInvariantId)
				//logger.debug("Incoming Invariant Id is: " + modelInvariantId)
				
				//String modelVersionId = jsonUtil.getJsonValue(vnfModelInfo, "modelVersionId")
				//if (modelVersionId == null) {
				//	modelVersionId = ""
				//}
				//execution.setVariable("DDVAM_modelVersionId", modelVersionId)
				//logger.debug("Incoming Version Id is: " + modelVersionId)
	
				//String modelVersion = jsonUtil.getJsonValue(vnfModelInfo, "modelVersion")
				//execution.setVariable("DDVAM_modelVersion", modelVersion)
				//logger.debug("Incoming Model Version is: " + modelVersion)
				
				//String modelName = jsonUtil.getJsonValue(vnfModelInfo, "modelName")
				//execution.setVariable("DDVAM_modelName", modelName)
				//logger.debug("Incoming Model Name is: " + modelName)
				
				//String modelCustomizationId = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationId")
				//if (modelCustomizationId == null) {
				//	modelCustomizationId = ""
				//}
				//execution.setVariable("DDVAM_modelCustomizationId", modelCustomizationId)
				//logger.debug("Incoming Model Customization Id is: " + modelCustomizationId)
					
				String cloudSiteId = execution.getVariable("lcpCloudRegionId")
				execution.setVariable("DDVAM_cloudSiteId", cloudSiteId)
				logger.debug("Incoming Cloud Site Id is: " + cloudSiteId)
					
				String tenantId = execution.getVariable("tenantId")
				execution.setVariable("DDVAM_tenantId", tenantId)
				logger.debug("Incoming Tenant Id is: " + tenantId)
				
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				if (globalSubscriberId == null) {
					globalSubscriberId = ""
				}
				execution.setVariable("DDVAM_globalSubscriberId", globalSubscriberId)
				logger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)		
				
			}
			execution.setVariable("DDVAM_vfModulesFromDecomposition", null)
			// Retrieve serviceDecomposition if present
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			if (serviceDecomposition != null) {
				logger.debug("Getting Catalog DB data from ServiceDecomposition object: " + serviceDecomposition.toJsonString())
				List<VnfResource> vnfs = serviceDecomposition.getVnfResources()
				logger.debug("Read vnfs")
				if (vnfs == null) {
					logger.debug("Error - vnfs are empty in serviceDecomposition object")
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in preProcessRequest - vnfs are empty")
				}
				VnfResource vnf = vnfs[0]
				
				if (vnf == null) {
					logger.debug("Error - vnf is empty in serviceDecomposition object")
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in preProcessRequest - vnf is empty")
				}
				
				List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()
				
				execution.setVariable("DDVAM_vfModulesFromDecomposition", vfModules)				
			}
			
			execution.setVariable("DDVAM_moduleCount", 0)
			execution.setVariable("DDVAM_nextModule", 0)
			
			
		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.debug(" Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		logger.trace("COMPLETED DoDeleteVnfAndModules PreProcessRequest Process ")
	}	

	
	
	public void preProcessAddOnModule(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessAddOnModule ")
		
		try {			
			JSONArray addOnModules = (JSONArray) execution.getVariable("addOnModules")
			int addOnIndex = (int) execution.getVariable("addOnModulesDeployed")
			
			JSONObject addOnModule = addOnModules[addOnIndex]
			
			def newVfModuleId = UUID.randomUUID().toString()
			execution.setVariable("addOnVfModuleId", newVfModuleId)
			
			execution.setVariable("instancesOfThisModelDeployed", 0)
			
			JSONObject addOnVfModuleModelInfoObject = jsonUtil.getJsonValueForKey(addOnModule, "modelInfo")
			String addOnVfModuleModelInfo = addOnVfModuleModelInfoObject.toString()
			execution.setVariable("addOnVfModuleModelInfo", addOnVfModuleModelInfo)
			String addOnVfModuleLabel = jsonUtil.getJsonValueForKey(addOnModule, "vfModuleLabel")
			execution.setVariable("addOnVfModuleLabel", addOnVfModuleLabel)
			String addOnPersonaModelId = jsonUtil.getJsonValueForKey(addOnVfModuleModelInfoObject, "modelInvariantUuid")
			execution.setVariable("addOnPersonaModelId", addOnPersonaModelId)
			String addOnInitialCount = jsonUtil.getJsonValueForKey(addOnModule, "initialCount")
			execution.setVariable("initialCount", addOnInitialCount)
					
		
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessAddOnModule." + e, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCAssignRequest ")
	}
	
	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.queryAAIVfModule(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')
			
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)).depth(Depth.ONE)

			try {
				Optional<GenericVnf> genericVnfOp = getAAIClient().get(GenericVnf.class,uri)
				//Map<String, String>[] vfModules = new HashMap<String,String>[]
				List<ModuleResource> vfModulesFromDecomposition = execution.getVariable("DDVAM_vfModulesFromDecomposition")
				def vfModulesList = new ArrayList<Map<String,String>>()
				def vfModuleBaseEntry = null
				if (genericVnfOp.isPresent()) {
					execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', 200)
					execution.setVariable('DCVFM_queryAAIVfModuleResponse', genericVnfOp.get())

					// Parse the VNF record from A&AI to find base module info
						if (genericVnfOp.get().getVfModules()!= null && !genericVnfOp.get().getVfModules().getVfModule().isEmpty() ) {
							List<VfModule> vfModules = genericVnfOp.get().getVfModules().getVfModule()
							execution.setVariable("DDVAM_moduleCount", vfModules.size())
							ModelInfo vfModuleModelInfo = null
							for (VfModule vfModule : vfModules) {
								Map<String, String> vfModuleEntry = new HashMap<String, String>()
								vfModuleEntry.put("vfModuleId", vfModule.getVfModuleId())
								vfModuleEntry.put("vfModuleName", vfModule.getVfModuleName())
								
								// Find the model for this vf module in decomposition if specified
								if (vfModulesFromDecomposition != null) {
									logger.debug("vfModulesFromDecomposition is not null")
									def vfModuleUuid = vfModule.getModelVersionId()
									if (vfModuleUuid == null) {
										vfModuleUuid = vfModule.getPersonaModelVersion()
									}
									logger.debug("vfModule UUID is: " + vfModuleUuid)
									for (j in 0..vfModulesFromDecomposition.size()-1) {
										ModuleResource mr = vfModulesFromDecomposition[j]
										if (mr.getModelInfo().getModelUuid() == vfModuleUuid) {
											logger.debug("Found modelInfo")
											vfModuleModelInfo = mr.getModelInfo()
											break											
										}
										
									}									
								}
								if (vfModuleModelInfo != null) {
									String vfModuleModelInfoString = vfModuleModelInfo.toString()
									def vfModuleModelInfoValue = jsonUtil.getJsonValue(vfModuleModelInfoString, "modelInfo")
									vfModuleEntry.put("vfModuleModelInfo", vfModuleModelInfoValue)
								}
								else {
									vfModuleEntry.put("vfModuleModelInfo", null)
								}
								
								
								// Save base vf module for last
								if (vfModule.isIsBaseVfModule()) {
									vfModuleBaseEntry = vfModuleEntry
								}
								else {						
									vfModulesList.add(vfModuleEntry)
								}
							}
							if (vfModuleBaseEntry != null) {
								vfModulesList.add(vfModuleBaseEntry)
							}
						}
				}else{
					execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', 404)
					execution.setVariable('DCVFM_queryAAIVfModuleResponse', "Generic Vnf not found")

				}
				execution.setVariable("DDVAM_vfModules", vfModulesList)
			} catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI GET: {}', ex.getMessage(), ex)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}
	
	public void prepareNextModuleToDelete(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED prepareNextModuleToDelete ")
		
		try {
			int i = execution.getVariable("DDVAM_nextModule")
			def vfModules = execution.getVariable("DDVAM_vfModules")
			def vfModule = vfModules[i]
			
			def vfModuleId = vfModule.get("vfModuleId")
			execution.setVariable("DDVAM_vfModuleId", vfModuleId)
			
			def vfModuleName = vfModule.get("vfModuleName")
			execution.setVariable("DDVAM_vfModuleName", vfModuleName)			
			
			def vfModuleModelInfo = vfModule.get("vfModuleModelInfo")
			logger.debug("vfModuleModelInfo for module delete: " + vfModuleModelInfo)
			execution.setVariable("DDVAM_vfModuleModelInfo", vfModuleModelInfo)			
			
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessAddOnModule." + e, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareNextModuleToDelete Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED prepareNextModuleToDelete ")
	}
	
	public void preProcessSDNCDeactivateRequest(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCDeactivateRequest ")
		def vnfId = execution.getVariable("vnfId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")		

		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")

			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable("DDVAM_deactivateSDNCRequest", deactivateSDNCRequest)
			logger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)
			logger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessSDNCDeactivateRequest." + e, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCDeactivateRequest ")
	}
	
	public void preProcessSDNCUnassignRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCUnassignRequest Process ")
		try{
			String vnfId = execution.getVariable("vnfId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")

			execution.setVariable("DDVAM_unassignSDNCRequest", unassignSDNCRequest)
			logger.debug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest)
			logger.debug("Outgoing UnassignSDNCRequest is: \n"  + unassignSDNCRequest)

		}catch(Exception e){
			logger.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  preProcessSDNCUnassignRequest Process ")
	}
	
	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){
		
				String uuid = execution.getVariable('testReqId') // for junits
				if(uuid==null){
					uuid = execution.getVariable("msoRequestId") + "-" +  	System.currentTimeMillis()
				}
				def callbackURL = execution.getVariable("sdncCallbackUrl")
				def requestId = execution.getVariable("msoRequestId")
				def serviceId = execution.getVariable("DDVAM_serviceId")				
				def tenantId = execution.getVariable("DDVAM_tenantId")
				def source = execution.getVariable("DDVAM_source")
				def vnfId = execution.getVariable("vnfId")
				def serviceInstanceId = execution.getVariable("serviceInstanceId")
				def cloudSiteId = execution.getVariable("DDVAM_cloudSiteId")				
				def modelCustomizationId = execution.getVariable("DDVAM_modelCustomizationId")				
				//def serviceModelInfo = execution.getVariable("serviceModelInfo")
				//def vnfModelInfo = execution.getVariable("vnfModelInfo")
				//String serviceEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(serviceModelInfo)
				//String vnfEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vnfModelInfo)				
				def globalSubscriberId = execution.getVariable("DDVAM_globalSubscriberId")
				def sdncVersion = execution.getVariable("DDVAM_sdncVersion")						
				
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
			<request-action>DeleteVnfInstance</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id/>
			<subscription-service-type/>			
			<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			<global-customer-id/>
		</service-information>
		<vnf-information>
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-type/>			
		</vnf-information>
		<vnf-request-input>			
			<vnf-name/>
			<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
			<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>			
		</vnf-request-input>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
		
			logger.debug("sdncRequest:  " + sdncRequest)
			return sdncRequest
	}
		
	public void validateSDNCResponse(DelegateExecution execution, String response, String method){

		execution.setVariable("prefix",Prefix)
		logger.trace("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		logger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		logger.debug("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)			
		}else{
			logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		logger.trace("COMPLETED ValidateSDNCResponse Process")
	}
	
	
	
	
	
	
	
}
