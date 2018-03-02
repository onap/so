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

import java.io.ObjectInputStream.BlockDataInputStream
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hibernate.jpa.criteria.predicate.IsEmptyPredicate

import static org.apache.commons.lang3.StringUtils.*;

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ModuleResource
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.WorkflowException
import org.springframework.web.util.UriUtils;

/**
 * This class supports the VID Flow
 * with the update of a generic vnf and related VF modules.
 */
class DoUpdateVnfAndModules extends AbstractServiceTaskProcessor {

	String Prefix="DUVAM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()	

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
		utils.log("DEBUG", " *** STARTED DoUpdateVnfAndModules PreProcessRequest Process*** ", isDebugEnabled)

		try{
			// Get Variables				
			
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("requestId", requestId)			
			execution.setVariable("mso-request-id", requestId)
			utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")			
			utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

			String vnfId = execution.getVariable("vnfId")			
			utils.log("DEBUG", "Incoming Vnf Id is: " + vnfId, isDebugEnabled)			
			
			String source = "VID"
			execution.setVariable("DUVAM_source", source)
			utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)
			
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DUVAM_sdncVersion", sdncVersion)
			utils.log("DEBUG", "Incoming Sdnc Version is: " + sdncVersion, isDebugEnabled)
			
			VnfResource vnfResource = (VnfResource) execution.getVariable("vnfResourceDecomposition")			
			
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			String serviceModelInfo = execution.getVariable("serviceModelInfo")
				
			String serviceId = execution.getVariable("productFamilyId")
			execution.setVariable("DUVAM_serviceId", serviceId)
			utils.log("DEBUG", "Incoming Service Id is: " + serviceId, isDebugEnabled)				
			
			String modelUuid = jsonUtil.getJsonValue(vnfModelInfo, "modelUuid")			
			execution.setVariable("DUVAM_modelUuid", modelUuid)
			utils.log("DEBUG", "Incoming modelUuid is: " + modelUuid, isDebugEnabled)				
				
			String modelCustomizationUuid = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationUuid")			
			execution.setVariable("DUVAM_modelCustomizationUuid", modelCustomizationUuid)
			utils.log("DEBUG", "Incoming Model Customization Uuid is: " + modelCustomizationUuid, isDebugEnabled)
					
			String cloudSiteId = execution.getVariable("lcpCloudRegionId")
			execution.setVariable("DUVAM_cloudSiteId", cloudSiteId)
			utils.log("DEBUG", "Incoming Cloud Site Id is: " + cloudSiteId, isDebugEnabled)
					
			String tenantId = execution.getVariable("tenantId")
			execution.setVariable("DUVAM_tenantId", tenantId)
			utils.log("DEBUG", "Incoming Tenant Id is: " + tenantId, isDebugEnabled)
				
			String globalSubscriberId = execution.getVariable("globalSubscriberId")
			if (globalSubscriberId == null) {
				globalSubscriberId = ""
			}
			execution.setVariable("DUVAM_globalSubscriberId", globalSubscriberId)
			utils.log("DEBUG", "Incoming Global Subscriber Id is: " + globalSubscriberId, isDebugEnabled)
			
			execution.setVariable("DUVAM_moduleCount", 0)
			execution.setVariable("DUVAM_nextModule", 0)
			
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoUpdateVnfAndModules PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoUpdateVnfAndModules PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoUpdateVnfAndModules PreProcessRequest Process ***", isDebugEnabled)
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
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("createVfModule - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DUVAM_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DUVAM_queryAAIVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				//Map<String, String>[] vfModules = new HashMap<String,String>[]
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
							execution.setVariable("DUVAM_moduleCount", vfModules.size())
							int vfModulesSize = 0
							for (i in 0..vfModules.size()-1) {
								def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							
								Map<String, String> vfModuleEntry = new HashMap<String, String>()
								def vfModuleId = utils.getNodeText1(vfModuleXml, "vf-module-id")
								vfModuleEntry.put("vfModuleId", vfModuleId)
								def vfModuleName = utils.getNodeText1(vfModuleXml, "vf-module-name")
								vfModuleEntry.put("vfModuleName", vfModuleName)
								def modelInvariantUuid = utils.getNodeText1(vfModuleXml, "model-invariant-id")
								vfModuleEntry.put("modelInvariantUuid", modelInvariantUuid)
								def modelUuid = utils.getNodeText1(vfModuleXml, "model-version-id")
								vfModuleEntry.put("modelUuid", modelUuid)
								def modelCustomizationUuid = utils.getNodeText1(vfModuleXml, "model-customization-id")
								vfModuleEntry.put("modelCustomizationUuid", modelCustomizationUuid)
														
								def isBaseVfModule = utils.getNodeText(vfModuleXml, "is-base-vf-module")
								vfModuleEntry.put("isBaseVfModule", isBaseVfModule)
								
								String volumeGroupId = ''
								
								logDebug("Next module!", isDebugLogEnabled)
								def vfModuleRelationships = vfModules[i].'**'.findAll {it.name() == 'relationship-data'}
								if (vfModuleRelationships.size() > 0) {
									for (j in 0..vfModuleRelationships.size()-1) {										
										if (vfModuleRelationships[j] != null) {
									
											def relationshipKey = vfModuleRelationships[j].'**'.findAll {it.name() == 'relationship-key'}											
										
											if (relationshipKey[0] == 'volume-group.volume-group-id') {
												def relationshipValue = vfModuleRelationships[j].'**'.findAll {it.name() == 'relationship-value'}
												volumeGroupId = relationshipValue[0]
												break
											}
										}
									}
								}
								
								vfModuleEntry.put("volumeGroupId", volumeGroupId)
								logDebug("volumeGroupId is: " + volumeGroupId, isDebugLogEnabled)

								// Save base vf module to add it to the start of the list later
								if (isBaseVfModule == "true") {									
									vfModuleBaseEntry = vfModuleEntry
								}
								else {						
									vfModulesList.add(vfModuleEntry)
								}
							}
							// Start the list with the base module if any
							if (vfModuleBaseEntry != null) {
								vfModulesList.add(0, vfModuleBaseEntry)
							}					
						}
						
					}					
				}
				else {
					logDebug('Response code from AAI GET is: ' + response.getStatusCode(), isDebugLogEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Response code from AAI GET is: ' + response.getStatusCode())
				}
				execution.setVariable("DUVAM_vfModules", vfModulesList)
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
	
	public void prepareNextModuleToUpdate(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED prepareNextModuleToUpdate ======== ", isDebugLogEnabled)
		
		try {
			int i = execution.getVariable("DUVAM_nextModule")
			def vfModules = execution.getVariable("DUVAM_vfModules")
			def vfModule = vfModules[i]
			
			def vfModuleId = vfModule.get("vfModuleId")
			execution.setVariable("DUVAM_vfModuleId", vfModuleId)
			
			def vfModuleName = vfModule.get("vfModuleName")
			execution.setVariable("DUVAM_vfModuleName", vfModuleName)
			
			def isBaseVfModule = vfModule.get("isBaseVfModule")
			execution.setVariable("DUVAM_isBaseVfModule", isBaseVfModule)
			
			String modelInvariantUuid = vfModule.get("modelInvariantUuid")
			logDebug("ModelInvariantUuid: " + modelInvariantUuid, isDebugLogEnabled)			
			
			def volumeGroupId = vfModule.get("volumeGroupId")
			execution.setVariable("DUVAM_volumeGroupId", volumeGroupId)

			execution.setVariable("DUVAM_volumeGroupName", "")
			
			VnfResource vnfResource = (VnfResource) execution.getVariable("vnfResourceDecomposition")
			List<ModuleResource> moduleResources = vnfResource.getVfModules()
			
			for (j in 0..moduleResources.size()-1) {				
				ModelInfo modelInfo = moduleResources[j].getModelInfo()
				String modelInvariantUuidFromDecomposition = modelInfo.getModelInvariantUuid()
				logDebug("modelInvariantUuidFromDecomposition: " + modelInvariantUuidFromDecomposition, isDebugLogEnabled)
				
				if (modelInvariantUuid.equals(modelInvariantUuidFromDecomposition)) {
					String vfModuleModelInfo = modelInfo.toJsonString()
					String vfModuleModelInfoValue = jsonUtil.getJsonValue(vfModuleModelInfo, "modelInfo")
					execution.setVariable("DUVAM_vfModuleModelInfo", vfModuleModelInfoValue)
					logDebug("vfModuleModelInfo: " + vfModuleModelInfoValue, isDebugLogEnabled)
					break
				}
				
			}			
			
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareNextModuleToUpdate Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED prepareNextModuleToUpdate ======== ", isDebugLogEnabled)
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
		logDebug('Entered ' + method, isDebugLogEnabled)
 
		try {			
			def vnfId = execution.getVariable('vnfId')
			VnfResource vnfResource = (VnfResource) execution.getVariable("vnfResourceDecomposition")
			ModelInfo vnfDecompModelInfo = vnfResource.getModelInfo()
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			String modelUuid = execution.getVariable("DUVAM_modelUuid")
			if (modelUuid == null || modelUuid.isEmpty()) {
				modelUuid = vnfDecompModelInfo.getModelUuid()			
			}
			String modelCustomizationUuid = execution.getVariable("DUVAM_modelCustomizationUuid")
			if (modelCustomizationUuid == null || modelCustomizationUuid.isEmpty()) {
				modelCustomizationUuid = vnfDecompModelInfo.getModelCustomizationUuid()
			}				
			String nfType = vnfResource.getNfType()
			String nfTypeString = ''
			if (nfType != null && !nfType.isEmpty()) {
				nfTypeString = "<nf-type>" + nfType + "</nf-type>"
			}			
			String nfRole = vnfResource.getNfRole()
			String nfRoleString = ''
			if (nfRole != null && !nfRole.isEmpty()) {
				nfRoleString = "<nf-role>" + nfRole + "</nf-role>"
			}
			String nfFunction = vnfResource.getNfFunction()
			String nfFunctionString = ''
			if (nfFunction != null && !nfFunction.isEmpty()) {
				nfFunctionString = "<nf-function>" + nfFunction + "</nf-function>"
			}
			String nfNamingCode = vnfResource.getNfNamingCode()
			String nfNamingCodeString = ''
			if (nfNamingCode != null && !nfNamingCode.isEmpty()) {
				nfNamingCodeString = "<nf-naming-code>" + nfNamingCode + "</nf-naming-code>"
			}			
 
			String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${vnfId}</vnf-id>
						<model-version-id>${modelUuid}</model-version-id>
						<model-customization-id>${modelCustomizationUuid}</model-customization-id>
						${nfTypeString}
						${nfRoleString}
						${nfFunctionString}
						${nfNamingCodeString}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DUVAM_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				utils.logAudit("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logDebug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest, isDebugLogEnabled)
 
 
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}
	
	/**
	 * APP-C Call - placeholder.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void callAppCf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.callAppC(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)	
	}
}
