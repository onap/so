/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import org.json.JSONObject;
import org.json.JSONArray;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;

import static org.apache.commons.lang3.StringUtils.*;

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException



/**
 * This class supports the macro VID Flow
 * with the creation of a generic vnf and related VF modules.
 */
class DoCreateVnfAndModules extends AbstractServiceTaskProcessor {

	String Prefix="DCVAM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	CatalogDbUtils cutils = new CatalogDbUtils()

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoCreateVnfAndModules PreProcessRequest Process*** ", isDebugEnabled)

		try{
			// Get Variables	
			
			
			String cloudConfiguration = execution.getVariable("cloudConfiguration")
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			
			String requestId = execution.getVariable("requestId")
			execution.setVariable("mso-request-id", requestId)
			utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")			
			utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

			String vnfType = execution.getVariable("vnfType")			
			utils.log("DEBUG", "Incoming Vnf Type is: " + vnfType, isDebugEnabled)

			String vnfName = execution.getVariable("vnfName")
			execution.setVariable("CREVI_vnfName", vnfName)
			utils.log("DEBUG", "Incoming Vnf Name is: " + vnfName, isDebugEnabled)

			String productFamilyId = execution.getVariable("productFamilyId")			
			utils.log("DEBUG", "Incoming Product Family Id is: " + productFamilyId, isDebugEnabled)

			String source = "VID"
			execution.setVariable("source", source)
			utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)

			String disableRollback = execution.getVariable("disableRollback")			
			utils.log("DEBUG", "Incoming Disable Rollback is: " + disableRollback, isDebugEnabled)
			
			String asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
			utils.log("DEBUG", "Incoming asdcServiceModelVersion: " + asdcServiceModelVersion, isDebugEnabled)

			String vnfId = execution.getVariable("testVnfId") // for junits
			if(isBlank(vnfId)){
				vnfId = execution.getVariable("vnfId")
				if (isBlank(vnfId)) {
					vnfId = UUID.randomUUID().toString()
					utils.log("DEBUG", "Generated Vnf Id is: " + vnfId, isDebugEnabled)
				}
			}
			execution.setVariable("vnfId", vnfId)			
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoCreateVnfAndModules PreProcessRequest Process ***", isDebugEnabled)
	}	

	
	public void queryCatalogDB (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED DoCreateVnfAndModules QueryCatalogDB Process *** ", isDebugEnabled)
		try {
			//Get Vnf Info
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			String vnfModelCustomizationUuid = jsonUtil.getJsonValueForKey(vnfModelInfo, "modelCustomizationId")
			utils.log("DEBUG", "querying Catalog DB by vnfModelCustomizationUuid: " + vnfModelCustomizationUuid)
			String catalogDbEndpoint = execution.getVariable("URN_mso_catalog_db_endpoint")
			
			JSONArray vnfs = cutils.getAllVnfsByVnfModelCustomizationUuid(catalogDbEndpoint, 
							vnfModelCustomizationUuid)
			utils.log("DEBUG", "obtained VNF list")
			// Only one match here
			JSONObject vnf = vnfs[0]
			JSONArray vfModules = vnf.getJSONArray("vfModules")
			JSONArray addOnModules = new JSONArray()
			
			// Set up base Vf Module info
			for (int i = 0; i < vfModules.length(); i++) {
				utils.log("DEBUG", "handling VF Module ")
				JSONObject vfModule = vfModules[i]
				String isBase = jsonUtil.getJsonValueForKey(vfModule, "isBase")
				if (isBase.equals("true")) {
					JSONObject baseVfModuleModelInfoObject = vfModule.getJSONObject("modelInfo")
					String baseVfModuleModelInfo = baseVfModuleModelInfoObject.toString()
					execution.setVariable("baseVfModuleModelInfo", baseVfModuleModelInfo)
					String baseVfModuleLabel = jsonUtil.getJsonValueForKey(vfModule, "vfModuleLabel")
					execution.setVariable("baseVfModuleLabel", baseVfModuleLabel)
					String basePersonaModelId = jsonUtil.getJsonValueForKey(baseVfModuleModelInfoObject, "modelInvariantId")
					execution.setVariable("basePersonaModelId", basePersonaModelId)					
				}
				else {
					addOnModules.add(vfModules[i])
				}			
			}
			
			execution.setVariable("addOnModules", addOnModules)
			execution.setVariable("addOnModulesToDeploy", addOnModules.length())
			execution.setVariable("addOnModulesDeployed", 0)			
			
		}catch(Exception ex) {
			utils.log("DEBUG", "Error Occured in DoCreateVnfAndModules QueryCatalogDB Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnfAndModules QueryCatalogDB Process")
		}
		
		// Generate vfModuleId for base VF Module
		def baseVfModuleId = UUID.randomUUID().toString()
		execution.setVariable("baseVfModuleId", baseVfModuleId)
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra PrepareCreateGenericVnf Process ***", isDebugEnabled)
	}
	
	public void preProcessAddOnModule(Execution execution){
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
			String addOnPersonaModelId = jsonUtil.getJsonValueForKey(addOnVfModuleModelInfoObject, "modelInvariantId")
			execution.setVariable("addOnPersonaModelId", addOnPersonaModelId)
			String addOnInitialCount = jsonUtil.getJsonValueForKey(addOnModule, "initialCount")
			execution.setVariable("initialCount", addOnInitialCount)
					
		
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	public void validateAddOnModule(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED validateAddOnModule ======== ", isDebugLogEnabled)
		
		try {
			int instancesOfThisModuleDeployed = execution.getVariable("instancesOfThisModuleDeployed")
			execution.setVariable("instancesOfThisModuleDeployed", instancesOfThisModuleDeployed + 1)
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	public void finishProcessingInitialCountDeployment(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED finishProcessingInitialCountDeployment ======== ", isDebugLogEnabled)
		
		try {
			int addOnModulesDeployed = execution.getVariable("addOnModulesDeployed")
			execution.setVariable("addOnModulesDeployed", addOnModulesDeployed + 1)			
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCAssignRequest ======== ", isDebugLogEnabled)
	}
	
	
	
}
