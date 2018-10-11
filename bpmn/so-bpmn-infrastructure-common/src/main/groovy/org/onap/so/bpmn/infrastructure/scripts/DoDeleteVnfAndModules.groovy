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

package org.onap.so.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.so.bpmn.common.scripts.AaiUtil
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
import org.onap.so.client.graphinventory.entities.uri.Depth
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils;

/**
 * This class supports the macro VID Flow
 * with the deletion of a generic vnf and related VF modules.
 */
class DoDeleteVnfAndModules extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteVnfAndModules.class);

	String Prefix="DDVAM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *	
	 */
	public void preProcessRequest(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED DoDeleteVnfAndModules PreProcessRequest Process")

		try{
			// Get Variables				
			
			String cloudConfiguration = execution.getVariable("cloudConfiguration")		
			msoLogger.debug("Cloud Configuration: " + cloudConfiguration)	
			
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("requestId", requestId)			
			execution.setVariable("mso-request-id", requestId)
			msoLogger.debug("Incoming Request Id is: " + requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")			
			msoLogger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

			String vnfId = execution.getVariable("vnfId")			
			msoLogger.debug("Incoming Vnf Id is: " + vnfId)			
			
			String source = "VID"
			execution.setVariable("DDVAM_source", source)
			msoLogger.debug("Incoming Source is: " + source)
			
			execution.setVariable("DDVAM_isVidRequest", "true")
			
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DDVAM_sdncVersion", sdncVersion)
			msoLogger.debug("Incoming Sdnc Version is: " + sdncVersion)
			
			// Set aLaCarte flag to false
			execution.setVariable("aLaCarte", false)
			
			String sdncCallbackUrl = (String) UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback", execution)
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'mso.workflow.sdncadapter.callback\' is missing'
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception");
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			msoLogger.debug("SDNC Callback URL: " + sdncCallbackUrl)
			msoLogger.debug("SDNC Callback URL is: " + sdncCallbackUrl)	
			
			
			if (!sdncVersion.equals("1702")) {
				//String vnfModelInfo = execution.getVariable("vnfModelInfo")
				//String serviceModelInfo = execution.getVariable("serviceModelInfo")
				
				String serviceId = execution.getVariable("productFamilyId")
				execution.setVariable("DDVAM_serviceId", serviceId)
				msoLogger.debug("Incoming Service Id is: " + serviceId)
				
					
				//String modelInvariantId = jsonUtil.getJsonValue(vnfModelInfo, "modelInvariantId")
				//execution.setVariable("DDVAM_modelInvariantId", modelInvariantId)
				//msoLogger.debug("Incoming Invariant Id is: " + modelInvariantId)
				
				//String modelVersionId = jsonUtil.getJsonValue(vnfModelInfo, "modelVersionId")
				//if (modelVersionId == null) {
				//	modelVersionId = ""
				//}
				//execution.setVariable("DDVAM_modelVersionId", modelVersionId)
				//msoLogger.debug("Incoming Version Id is: " + modelVersionId)
	
				//String modelVersion = jsonUtil.getJsonValue(vnfModelInfo, "modelVersion")
				//execution.setVariable("DDVAM_modelVersion", modelVersion)
				//msoLogger.debug("Incoming Model Version is: " + modelVersion)
				
				//String modelName = jsonUtil.getJsonValue(vnfModelInfo, "modelName")
				//execution.setVariable("DDVAM_modelName", modelName)
				//msoLogger.debug("Incoming Model Name is: " + modelName)
				
				//String modelCustomizationId = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationId")
				//if (modelCustomizationId == null) {
				//	modelCustomizationId = ""
				//}
				//execution.setVariable("DDVAM_modelCustomizationId", modelCustomizationId)
				//msoLogger.debug("Incoming Model Customization Id is: " + modelCustomizationId)
					
				String cloudSiteId = execution.getVariable("lcpCloudRegionId")
				execution.setVariable("DDVAM_cloudSiteId", cloudSiteId)
				msoLogger.debug("Incoming Cloud Site Id is: " + cloudSiteId)
					
				String tenantId = execution.getVariable("tenantId")
				execution.setVariable("DDVAM_tenantId", tenantId)
				msoLogger.debug("Incoming Tenant Id is: " + tenantId)
				
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				if (globalSubscriberId == null) {
					globalSubscriberId = ""
				}
				execution.setVariable("DDVAM_globalSubscriberId", globalSubscriberId)
				msoLogger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)		
				
			}
			execution.setVariable("DDVAM_vfModulesFromDecomposition", null)
			// Retrieve serviceDecomposition if present
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			if (serviceDecomposition != null) {
				msoLogger.debug("Getting Catalog DB data from ServiceDecomposition object: " + serviceDecomposition.toJsonString())
				List<VnfResource> vnfs = serviceDecomposition.getVnfResources()
				msoLogger.debug("Read vnfs")
				if (vnfs == null) {
					msoLogger.debug("Error - vnfs are empty in serviceDecomposition object")
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in preProcessRequest - vnfs are empty")
				}
				VnfResource vnf = vnfs[0]
				
				if (vnf == null) {
					msoLogger.debug("Error - vnf is empty in serviceDecomposition object")
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in preProcessRequest - vnf is empty")
				}
				
				List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()
				
				execution.setVariable("DDVAM_vfModulesFromDecomposition", vfModules)				
			}
			
			execution.setVariable("DDVAM_moduleCount", 0)
			execution.setVariable("DDVAM_nextModule", 0)
			
			
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		msoLogger.trace("COMPLETED DoDeleteVnfAndModules PreProcessRequest Process ")
	}	

	
	
	public void preProcessAddOnModule(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessAddOnModule ")
		
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
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing preProcessAddOnModule." + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED preProcessSDNCAssignRequest ")
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
		msoLogger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE)
			String endPoint = aaiUriUtil.createAaiUri(uri)
			
			msoLogger.debug("AAI endPoint: " + endPoint)

			try {
				msoLogger.debug("createVfModule - invoking httpGet() to AAI")
				APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)

				def responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					msoLogger.debug("Received generic VNF data: " + responseData)

				}

				msoLogger.debug("createVfModule - queryAAIVfModule Response: " + responseData)
				msoLogger.debug("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DCVFM_queryAAIVfModuleResponse', responseData)
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
				//Map<String, String>[] vfModules = new HashMap<String,String>[]
				List<ModuleResource> vfModulesFromDecomposition = execution.getVariable("DDVAM_vfModulesFromDecomposition")
				def vfModulesList = new ArrayList<Map<String,String>>()
				def vfModules = null
				def vfModuleBaseEntry = null
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					msoLogger.debug('Parsing the VNF data to find base module info')
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						msoLogger.debug("vModulesText: " + vfModulesText)
						if (vfModulesText != null && !vfModulesText.trim().isEmpty()) {
							def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
							vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
							execution.setVariable("DDVAM_moduleCount", vfModules.size())
							int vfModulesSize = 0
							ModelInfo vfModuleModelInfo = null
							for (i in 0..vfModules.size()-1) {
								def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							
								Map<String, String> vfModuleEntry = new HashMap<String, String>()
								def vfModuleId = utils.getNodeText(vfModuleXml, "vf-module-id")
								vfModuleEntry.put("vfModuleId", vfModuleId)
								def vfModuleName = utils.getNodeText(vfModuleXml, "vf-module-name")      
								vfModuleEntry.put("vfModuleName", vfModuleName)
								
								// Find the model for this vf module in decomposition if specified
								if (vfModulesFromDecomposition != null) {
									msoLogger.debug("vfModulesFromDecomposition is not null")
									def vfModuleUuid = utils.getNodeText(vfModuleXml, "model-version-id")
									if (vfModuleUuid == null) {
										vfModuleUuid = utils.getNodeText(vfModuleXml, "persona-model-version")
									}
									msoLogger.debug("vfModule UUID is: " + vfModuleUuid)
									for (j in 0..vfModulesFromDecomposition.size()-1) {
										ModuleResource mr = vfModulesFromDecomposition[j]
										if (mr.getModelInfo().getModelUuid() == vfModuleUuid) {
											msoLogger.debug("Found modelInfo")
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
								
								
								def isBaseVfModule = utils.getNodeText(vfModuleXml, "is-base-vf-module")
								// Save base vf module for last
								if (isBaseVfModule == "true") {
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
						
					}					
				}
				execution.setVariable("DDVAM_vfModules", vfModulesList)
			} catch (Exception ex) {
				ex.printStackTrace()
				msoLogger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}
	
	public void prepareNextModuleToDelete(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED prepareNextModuleToDelete ")
		
		try {
			int i = execution.getVariable("DDVAM_nextModule")
			def vfModules = execution.getVariable("DDVAM_vfModules")
			def vfModule = vfModules[i]
			
			def vfModuleId = vfModule.get("vfModuleId")
			execution.setVariable("DDVAM_vfModuleId", vfModuleId)
			
			def vfModuleName = vfModule.get("vfModuleName")
			execution.setVariable("DDVAM_vfModuleName", vfModuleName)			
			
			def vfModuleModelInfo = vfModule.get("vfModuleModelInfo")
			msoLogger.debug("vfModuleModelInfo for module delete: " + vfModuleModelInfo)
			execution.setVariable("DDVAM_vfModuleModelInfo", vfModuleModelInfo)			
			
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing preProcessAddOnModule." + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareNextModuleToDelete Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED prepareNextModuleToDelete ")
	}
	
	public void preProcessSDNCDeactivateRequest(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessSDNCDeactivateRequest ")
		def vnfId = execution.getVariable("vnfId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")		

		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")

			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable("DDVAM_deactivateSDNCRequest", deactivateSDNCRequest)
			msoLogger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)
			msoLogger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)

		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing preProcessSDNCDeactivateRequest." + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED preProcessSDNCDeactivateRequest ")
	}
	
	public void preProcessSDNCUnassignRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'

		msoLogger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessSDNCUnassignRequest Process ")
		try{
			String vnfId = execution.getVariable("vnfId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")

			execution.setVariable("DDVAM_unassignSDNCRequest", unassignSDNCRequest)
			msoLogger.debug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest)
			msoLogger.debug("Outgoing UnassignSDNCRequest is: \n"  + unassignSDNCRequest)

		}catch(Exception e){
			msoLogger.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED  preProcessSDNCUnassignRequest Process ")
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
		
			msoLogger.debug("sdncRequest:  " + sdncRequest)
			return sdncRequest
	}
		
	public void validateSDNCResponse(DelegateExecution execution, String response, String method){

		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		msoLogger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		msoLogger.debug("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			msoLogger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)			
		}else{
			msoLogger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		msoLogger.trace("COMPLETED ValidateSDNCResponse Process")
	}
	
	
	
	
	
	
	
}
