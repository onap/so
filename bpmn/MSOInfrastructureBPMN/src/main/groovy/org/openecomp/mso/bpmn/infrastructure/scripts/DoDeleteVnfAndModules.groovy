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

import java.awt.Component.BaselineResizeBehavior
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;

import static org.apache.commons.lang3.StringUtils.*;

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ModuleResource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.springframework.web.util.UriUtils;

/**
 * This class supports the macro VID Flow
 * with the deletion of a generic vnf and related VF modules.
 */
class DoDeleteVnfAndModules extends AbstractServiceTaskProcessor {

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
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoDeleteVnfAndModules PreProcessRequest Process*** ", isDebugEnabled)

		try{
			// Get Variables				
			
			String cloudConfiguration = execution.getVariable("cloudConfiguration")		
			utils.log("DEBUG", "Cloud Configuration: " + cloudConfiguration, isDebugEnabled)	
			
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("requestId", requestId)			
			execution.setVariable("mso-request-id", requestId)
			utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")			
			utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

			String vnfId = execution.getVariable("vnfId")			
			utils.log("DEBUG", "Incoming Vnf Id is: " + vnfId, isDebugEnabled)			
			
			String source = "VID"
			execution.setVariable("DDVAM_source", source)
			utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)
			
			execution.setVariable("DDVAM_isVidRequest", "true")
			
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DDVAM_sdncVersion", sdncVersion)
			utils.log("DEBUG", "Incoming Sdnc Version is: " + sdncVersion, isDebugEnabled)
			
			// Set aLaCarte flag to false
			execution.setVariable("aLaCarte", false)
			
			String sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
				logError(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2000, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			utils.logAudit("SDNC Callback URL: " + sdncCallbackUrl)
			logDebug("SDNC Callback URL is: " + sdncCallbackUrl, isDebugEnabled)	
			
			
			if (!sdncVersion.equals("1702")) {
				//String vnfModelInfo = execution.getVariable("vnfModelInfo")
				//String serviceModelInfo = execution.getVariable("serviceModelInfo")
				
				String serviceId = execution.getVariable("productFamilyId")
				execution.setVariable("DDVAM_serviceId", serviceId)
				utils.log("DEBUG", "Incoming Service Id is: " + serviceId, isDebugEnabled)
				
					
				//String modelInvariantId = jsonUtil.getJsonValue(vnfModelInfo, "modelInvariantId")
				//execution.setVariable("DDVAM_modelInvariantId", modelInvariantId)
				//utils.log("DEBUG", "Incoming Invariant Id is: " + modelInvariantId, isDebugEnabled)
				
				//String modelVersionId = jsonUtil.getJsonValue(vnfModelInfo, "modelVersionId")
				//if (modelVersionId == null) {
				//	modelVersionId = ""
				//}
				//execution.setVariable("DDVAM_modelVersionId", modelVersionId)
				//utils.log("DEBUG", "Incoming Version Id is: " + modelVersionId, isDebugEnabled)
	
				//String modelVersion = jsonUtil.getJsonValue(vnfModelInfo, "modelVersion")
				//execution.setVariable("DDVAM_modelVersion", modelVersion)
				//utils.log("DEBUG", "Incoming Model Version is: " + modelVersion, isDebugEnabled)
				
				//String modelName = jsonUtil.getJsonValue(vnfModelInfo, "modelName")
				//execution.setVariable("DDVAM_modelName", modelName)
				//utils.log("DEBUG", "Incoming Model Name is: " + modelName, isDebugEnabled)
				
				//String modelCustomizationId = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationId")
				//if (modelCustomizationId == null) {
				//	modelCustomizationId = ""
				//}
				//execution.setVariable("DDVAM_modelCustomizationId", modelCustomizationId)
				//utils.log("DEBUG", "Incoming Model Customization Id is: " + modelCustomizationId, isDebugEnabled)
					
				String cloudSiteId = execution.getVariable("lcpCloudRegionId")
				execution.setVariable("DDVAM_cloudSiteId", cloudSiteId)
				utils.log("DEBUG", "Incoming Cloud Site Id is: " + cloudSiteId, isDebugEnabled)
					
				String tenantId = execution.getVariable("tenantId")
				execution.setVariable("DDVAM_tenantId", tenantId)
				utils.log("DEBUG", "Incoming Tenant Id is: " + tenantId, isDebugEnabled)
				
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				if (globalSubscriberId == null) {
					globalSubscriberId = ""
				}
				execution.setVariable("DDVAM_globalSubscriberId", globalSubscriberId)
				utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)		
				
			}
			execution.setVariable("DDVAM_vfModulesFromDecomposition", null)
			// Retrieve serviceDecomposition if present
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			if (serviceDecomposition != null) {
				utils.log("DEBUG", "Getting Catalog DB data from ServiceDecomposition object: " + serviceDecomposition.toJsonString(), isDebugEnabled)
				List<VnfResource> vnfs = serviceDecomposition.getServiceVnfs()
				utils.log("DEBUG", "Read vnfs", isDebugEnabled)
				if (vnfs == null) {
					utils.log("DEBUG", "Error - vnfs are empty in serviceDecomposition object", isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in preProcessRequest - vnfs are empty")
				}
				VnfResource vnf = vnfs[0]
				
				if (vnf == null) {
					utils.log("DEBUG", "Error - vnf is empty in serviceDecomposition object", isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in preProcessRequest - vnf is empty")
				}
				
				List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()
				
				execution.setVariable("DDVAM_vfModulesFromDecomposition", vfModules)				
			}
			
			execution.setVariable("DDVAM_moduleCount", 0)
			execution.setVariable("DDVAM_nextModule", 0)
			
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoDeleteVnfAndModules PreProcessRequest Process ***", isDebugEnabled)
	}	

	
	
	public void preProcessAddOnModule(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessAddOnModule ======== ", isDebugLogEnabled)
		
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
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(DelegateExecution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.queryAAIVfModule(' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('vnfId')
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			utils.logAudit("AAI endPoint: " + endPoint)

			try {
				utils.logAudit("createVfModule - invoking httpGet() to AAI")
				APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)

				def responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DCVFM_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DCVFM_queryAAIVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				//Map<String, String>[] vfModules = new HashMap<String,String>[]
				List<ModuleResource> vfModulesFromDecomposition = execution.getVariable("DDVAM_vfModulesFromDecomposition")
				def vfModulesList = new ArrayList<Map<String,String>>()
				def vfModules = null
				def vfModuleBaseEntry = null
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logDebug('Parsing the VNF data to find base module info', isDebugLogEnabled)
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						logDebug("vModulesText: " + vfModulesText, isDebugLogEnabled)
						if (vfModulesText != null && !vfModulesText.trim().isEmpty()) {
							def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
							vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
							execution.setVariable("DDVAM_moduleCount", vfModules.size())
							int vfModulesSize = 0
							ModelInfo vfModuleModelInfo = null
							for (i in 0..vfModules.size()-1) {
								def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							
								Map<String, String> vfModuleEntry = new HashMap<String, String>()
								def vfModuleId = utils.getNodeText1(vfModuleXml, "vf-module-id")
								vfModuleEntry.put("vfModuleId", vfModuleId)
								def vfModuleName = utils.getNodeText1(vfModuleXml, "vf-module-name")      
								vfModuleEntry.put("vfModuleName", vfModuleName)
								
								// Find the model for this vf module in decomposition if specified
								if (vfModulesFromDecomposition != null) {
									logDebug("vfModulesFromDecomposition is not null", isDebugLogEnabled)
									def vfModuleUuid = utils.getNodeText1(vfModuleXml, "model-version-id")
									if (vfModuleUuid == null) {
										vfModuleUuid = utils.getNodeText1(vfModuleXml, "persona-model-version")
									}
									logDebug("vfModule UUID is: " + vfModuleUuid, isDebugLogEnabled)
									for (j in 0..vfModulesFromDecomposition.size()-1) {
										ModuleResource mr = vfModulesFromDecomposition[j]
										if (mr.getModelInfo().getModelUuid() == vfModuleUuid) {
											logDebug("Found modelInfo", isDebugLogEnabled)
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
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}
	
	public void prepareNextModuleToDelete(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED prepareNextModuleToDelete ======== ", isDebugLogEnabled)
		
		try {
			int i = execution.getVariable("DDVAM_nextModule")
			def vfModules = execution.getVariable("DDVAM_vfModules")
			def vfModule = vfModules[i]
			
			def vfModuleId = vfModule.get("vfModuleId")
			execution.setVariable("DDVAM_vfModuleId", vfModuleId)
			
			def vfModuleName = vfModule.get("vfModuleName")
			execution.setVariable("DDVAM_vfModuleName", vfModuleName)			
			
			def vfModuleModelInfo = vfModule.get("vfModuleModelInfo")
			logDebug("vfModuleModelInfo for module delete: " + vfModuleModelInfo, isDebugLogEnabled)
			execution.setVariable("DDVAM_vfModuleModelInfo", vfModuleModelInfo)			
			
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareNextModuleToDelete Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED prepareNextModuleToDelete ======== ", isDebugLogEnabled)
	}
	
	public void preProcessSDNCDeactivateRequest(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
		def vnfId = execution.getVariable("vnfId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")		

		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")

			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable("DDVAM_deactivateSDNCRequest", deactivateSDNCRequest)
			logDebug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCDeactivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
	}
	
	public void preProcessSDNCUnassignRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
		try{
			String vnfId = execution.getVariable("vnfId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")

			execution.setVariable("DDVAM_unassignSDNCRequest", unassignSDNCRequest)
			logDebug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UnassignSDNCRequest is: \n"  + unassignSDNCRequest)

		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
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
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>DeleteVnfInstance</request-action>
			<source>${source}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id/>
			<subscription-service-type/>			
			<service-instance-id>${serviceInstanceId}</service-instance-id>
			<global-customer-id/>
		</service-information>
		<vnf-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type/>			
		</vnf-information>
		<vnf-request-input>			
			<vnf-name/>
			<tenant>${tenantId}</tenant>
			<aic-cloud-region>${cloudSiteId}</aic-cloud-region>			
		</vnf-request-input>
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
		
			utils.logAudit("sdncRequest:  " + sdncRequest)
			return sdncRequest
	}
		
	public void validateSDNCResponse(DelegateExecution execution, String response, String method){
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
	
	
	
	
	
	
	
}
