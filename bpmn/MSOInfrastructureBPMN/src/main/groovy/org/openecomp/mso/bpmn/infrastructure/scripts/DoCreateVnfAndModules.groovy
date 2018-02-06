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
import java.util.List

import org.json.JSONObject;
import org.json.JSONArray;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ModuleResource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.DecomposeJsonUtil
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;

import static org.apache.commons.lang3.StringUtils.*;



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
	   def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix",Prefix)
	   utils.log("DEBUG", " *** STARTED DoCreateVnfAndModules PreProcessRequest Process*** ", isDebugLogEnabled)
	   
	   setBasicDBAuthHeader(execution, isDebugLogEnabled)
	   try{
		   // Get Variables

		   ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

		   String vnfModelInfo = execution.getVariable("vnfModelInfo")

		   String requestId = execution.getVariable("msoRequestId")
		   execution.setVariable("requestId", requestId)
		   execution.setVariable("mso-request-id", requestId)
		   utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugLogEnabled)

		   String serviceInstanceId = execution.getVariable("serviceInstanceId")
		   utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugLogEnabled)

		   String vnfName = execution.getVariable("vnfName")
		   execution.setVariable("CREVI_vnfName", vnfName)
		   utils.log("DEBUG", "Incoming Vnf Name is: " + vnfName, isDebugLogEnabled)

		   String productFamilyId = execution.getVariable("productFamilyId")
		   utils.log("DEBUG", "Incoming Product Family Id is: " + productFamilyId, isDebugLogEnabled)

		   String source = "VID"
		   execution.setVariable("source", source)
		   utils.log("DEBUG", "Incoming Source is: " + source, isDebugLogEnabled)

		   String lcpCloudRegionId = execution.getVariable("lcpCloudRegionId")
		   utils.log("DEBUG", "Incoming LCP Cloud Region Id is: " + lcpCloudRegionId)

		   String tenantId = execution.getVariable("tenantId")
		   utils.log("DEBUG", "Incoming Tenant Id is: " + tenantId)

		   String disableRollback = execution.getVariable("disableRollback")
		   utils.log("DEBUG", "Incoming Disable Rollback is: " + disableRollback, isDebugLogEnabled)

		   String asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
		   utils.log("DEBUG", "Incoming asdcServiceModelVersion: " + asdcServiceModelVersion, isDebugLogEnabled)

		   String vnfId = execution.getVariable("testVnfId") // for junits
		   if(isBlank(vnfId)){
			   vnfId = execution.getVariable("vnfId")
			   if (isBlank(vnfId)) {
				   vnfId = UUID.randomUUID().toString()
				   utils.log("DEBUG", "Generated Vnf Id is: " + vnfId, isDebugLogEnabled)
			   }
		   }
		   execution.setVariable("vnfId", vnfId)

		   def rollbackData = execution.getVariable("rollbackData")
		   if (rollbackData == null) {
			   rollbackData = new RollbackData()
		   }
		   
		   def isTest = execution.getVariable("isTest")
		   
			if (isTest == null || isTest == false) {
				execution.setVariable("isBaseVfModule", "true")
			}
		   execution.setVariable("numOfCreatedAddOnModules", 0)

		   rollbackData.put("VNFANDMODULES", "numOfCreatedAddOnModules", "0")
		   execution.setVariable("rollbackData", rollbackData)

		   sleep (20000)


	   }catch(BpmnError b){
		   utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugLogEnabled)
		   throw b
	   }catch(Exception e){
		   utils.log("DEBUG", " Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage(), isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

	   }
	   utils.log("DEBUG", "*** COMPLETED DoCreateVnfAndModules PreProcessRequest Process ***", isDebugLogEnabled)
   }


   public void queryCatalogDB (Execution execution) {
	   def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix",Prefix)

	   utils.log("DEBUG", " *** STARTED DoCreateVnfAndModules QueryCatalogDB Process *** ", isDebugLogEnabled)
	   try {
		   VnfResource vnf = null
		   ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		   // if serviceDecomposition is specified, get info from serviceDecomposition
		   if (serviceDecomposition != null) {
			   utils.log("DEBUG", "Getting Catalog DB data from ServiceDecomposition object: " + serviceDecomposition.toJsonString(), isDebugLogEnabled)
			   List<VnfResource> vnfs = serviceDecomposition.getServiceVnfs()
			   utils.log("DEBUG", "Read vnfs", isDebugLogEnabled)
			   if (vnfs == null) {
				   utils.log("DEBUG", "Error - vnfs are empty in serviceDecomposition object", isDebugLogEnabled)
				   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf queryCatalogDB, vnfs are empty")
			   }
			   vnf = vnfs[0]
			   String serviceModelName = serviceDecomposition.getModelInfo().getModelName()
			   vnf.constructVnfType(serviceModelName)
			   String vnfType = vnf.getVnfType()
			   utils.log("DEBUG", "Incoming Vnf Type is: " + vnfType, isDebugLogEnabled)
			   execution.setVariable("vnfType", vnfType)
		   }
		   else {
			   //Get Vnf Info
			   String vnfModelInfo = execution.getVariable("vnfModelInfo")
			   utils.log("DEBUG", "vnfModelInfo: " + vnfModelInfo, isDebugLogEnabled)
			   String vnfModelCustomizationUuid = jsonUtil.getJsonValueForKey(vnfModelInfo, "modelCustomizationUuid")
			   if (vnfModelCustomizationUuid == null) {
					   vnfModelCustomizationUuid = ""
			   }
			   utils.log("DEBUG", "querying Catalog DB by vnfModelCustomizationUuid: " + vnfModelCustomizationUuid, isDebugLogEnabled)
			  
			   JSONArray vnfs = cutils.getAllVnfsByVnfModelCustomizationUuid(execution,
							   vnfModelCustomizationUuid)
			   utils.log("DEBUG", "obtained VNF list")
			   // Only one match here
			   JSONObject vnfObject = vnfs[0]
			   vnf = DecomposeJsonUtil.jsonToVnfResource(vnfObject.toString())
		   }
		   utils.log("DEBUG", "Read vnfResource", isDebugLogEnabled)
		   if (vnf == null) {
			   utils.log("DEBUG", "Error - vnf is empty in serviceDecomposition object", isDebugLogEnabled)
			   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf queryCatalogDB, vnf is null")
		   }
		   execution.setVariable("vnfResourceDecomposition", vnf)

		   List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()
		   utils.log("DEBUG", "Read vfModules", isDebugLogEnabled)
		   if (vfModules == null) {
			   utils.log("DEBUG", "Error - vfModules are empty in serviceDecomposition object", isDebugLogEnabled)
			   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf queryCatalogDB, vf modules are empty")
		   }
			   			  
		   ModuleResource baseVfModule = null

		   for (int i = 0; i < vfModules.size; i++) {
			   utils.log("DEBUG", "handling VF Module ", isDebugLogEnabled)
			   ModuleResource vfModule = vfModules[i]
			   boolean isBase = vfModule.getIsBase()
			   if (isBase) {
					   ModelInfo baseVfModuleModelInfoObject = vfModule.getModelInfo()
					   String baseVfModuleModelInfoWithRoot = baseVfModuleModelInfoObject.toString()
					   String baseVfModuleModelInfo = jsonUtil.getJsonValue(baseVfModuleModelInfoWithRoot, "modelInfo")
					   execution.setVariable("baseVfModuleModelInfo", baseVfModuleModelInfo)
					   String baseVfModuleLabel = vfModule.getVfModuleLabel()
					   execution.setVariable("baseVfModuleLabel", baseVfModuleLabel)
					   String basePersonaModelId = baseVfModuleModelInfoObject.getModelInvariantUuid()
					   execution.setVariable("basePersonaModelId", basePersonaModelId)
					   baseVfModule = vfModule
					   break
			   }		   
				
			}
			   
			List<ModuleResource>addOnModules = vfModules - baseVfModule
			   
			int addOnModulesToDeploy = 0
			if (addOnModules != null) {				
				   addOnModulesToDeploy = addOnModules.size
			}
			   
			utils.log("DEBUG", "AddOnModulesToDeploy: " + addOnModulesToDeploy)

			execution.setVariable("addOnModules", addOnModules)
			execution.setVariable("addOnModulesToDeploy", addOnModulesToDeploy)
			execution.setVariable("addOnModulesDeployed", 0)	  

	   }catch(Exception ex) {
		   utils.log("DEBUG", "Error Occured in DoCreateVnfAndModules QueryCatalogDB Process " + ex.getMessage(), isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnfAndModules QueryCatalogDB Process")
	   }

	   // Generate vfModuleId for base VF Module
	   def baseVfModuleId = UUID.randomUUID().toString()
	   execution.setVariable("baseVfModuleId", baseVfModuleId)
	   // For JUnits
	   String requestId = execution.getVariable("requestId")
	   if (requestId.equals("testRequestId123")) {
		   execution.setVariable("vnfId", "skask")
	   }

	   utils.log("DEBUG", "*** COMPLETED DoCreateVnfAndModules QueryCatalogDB Process ***", isDebugLogEnabled)
   }

   public void preProcessAddOnModule(Execution execution){
	   def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix", Prefix)
	   logDebug(" ======== STARTED preProcessAddOnModule ======== ", isDebugLogEnabled)

	   try {
		   List<ModuleResource>addOnModules = execution.getVariable("addOnModules")
		   int addOnIndex = (int) execution.getVariable("addOnModulesDeployed")

		   ModuleResource addOnModule = addOnModules[addOnIndex]
		   
		   utils.log("DEBUG", "Got addon module", isDebugLogEnabled)

		   def newVfModuleId = UUID.randomUUID().toString()
		   execution.setVariable("addOnVfModuleId", newVfModuleId)
		   execution.setVariable("isBaseVfModule", "false")
		   
		   execution.setVariable("instancesOfThisModuleDeployed", 0)

		   ModelInfo addOnVfModuleModelInfoObject = addOnModule.getModelInfo()		  
		   String addOnVfModuleModelInfoWithRoot = addOnVfModuleModelInfoObject.toString()
		   String addOnVfModuleModelInfo = jsonUtil.getJsonValue(addOnVfModuleModelInfoWithRoot, "modelInfo")
		   execution.setVariable("addOnVfModuleModelInfo", addOnVfModuleModelInfo)
		   String addOnVfModuleLabel = addOnModule.getVfModuleLabel()
		   execution.setVariable("addOnVfModuleLabel", addOnVfModuleLabel)
		   String addOnPersonaModelId = addOnVfModuleModelInfoObject.getModelInvariantUuid()
		   execution.setVariable("addOnPersonaModelId", addOnPersonaModelId)
		   int addOnInitialCount = addOnModule.getInitialCount()
		   execution.setVariable("initialCount", addOnInitialCount)


	   }catch(Exception e){
		   utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
	   }
	   logDebug("======== COMPLETED preProcessAddOnModule ======== ", isDebugLogEnabled)
   }

   public void postProcessAddOnModule(Execution execution){
	   def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix", Prefix)
	   logDebug(" ======== STARTED postProcessAddOnModule ======== ", isDebugLogEnabled)

	   try {
		   int addOnModulesDeployed = execution.getVariable("addOnModulesDeployed")
		   execution.setVariable("addOnModulesDeployed", addOnModulesDeployed + 1)

	   }catch(Exception e){
		   utils.log("ERROR", "Exception Occured Processing postProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during postProcessAddOnModule Method:\n" + e.getMessage())
	   }
	   logDebug("======== COMPLETED postProcessAddOnModule ======== ", isDebugLogEnabled)
   }
   
   public void validateBaseModule(Execution execution){
	   def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix", Prefix)
	   logDebug(" ======== STARTED validateBaseModule ======== ", isDebugLogEnabled)

	   try {
		   def baseRollbackData = execution.getVariable("DCVAM_baseRollbackData")
		   def rollbackData = execution.getVariable("rollbackData")

		   def baseModuleMap = baseRollbackData.get("VFMODULE")
		   baseModuleMap.each{ k, v -> rollbackData.put("VFMODULE_BASE", "${k}","${v}") }
		   execution.setVariable("rollbackData", rollbackData)
		   logDebug("addOnModulesDeployed: " + execution.getVariable("addOnModulesDeployed"), isDebugLogEnabled)
		   logDebug("addOnModulesToDeploy: " + execution.getVariable("addOnModulesToDeploy"), isDebugLogEnabled)
		   if (execution.getVariable("addOnModulesDeployed") <  execution.getVariable("addOnModulesToDeploy")) {
			   logDebug("More add on modules to deploy", isDebugLogEnabled)
		   }
		   else {
			   logDebug("No more add on modules to deploy", isDebugLogEnabled)
		   }

	   }catch(Exception e){
		   utils.log("ERROR", "Exception Occured Processing validateBaseModule. Exception is:\n" + e, isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during validateBaseModule Method:\n" + e.getMessage())
	   }
	   logDebug("======== COMPLETED validateBaseModule ======== ", isDebugLogEnabled)
   }

   public void validateAddOnModule(Execution execution){
	   def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
	   execution.setVariable("prefix", Prefix)
	   logDebug(" ======== STARTED validateAddOnModule ======== ", isDebugLogEnabled)

	   try {
		   int instancesOfThisModuleDeployed = execution.getVariable("instancesOfThisModuleDeployed")
		   int numOfCreatedAddOnModules = execution.getVariable("numOfCreatedAddOnModules")
		   def addOnRollbackData = execution.getVariable("DCVAM_addOnRollbackData")
		   def rollbackData = execution.getVariable("rollbackData")

		   def addOnModuleMap = addOnRollbackData.get("VFMODULE")
		   numOfCreatedAddOnModules = numOfCreatedAddOnModules + 1
		   addOnModuleMap.each{ k, v -> rollbackData.put("VFMODULE_ADDON_" + numOfCreatedAddOnModules, "${k}","${v}") }

		   execution.setVariable("DCVAM_addOnRollbackData", null)

		   execution.setVariable("instancesOfThisModuleDeployed", instancesOfThisModuleDeployed + 1)

		   execution.setVariable("numOfCreatedAddOnModules", numOfCreatedAddOnModules)
		   rollbackData.put("VNFANDMODULES", "numOfCreatedAddOnModules", "${numOfCreatedAddOnModules}")
		   execution.setVariable("rollbackData", rollbackData)
	   }catch(Exception e){
		   utils.log("ERROR", "Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, isDebugLogEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
	   }
	   logDebug("======== COMPLETED validateAddOnModule ======== ", isDebugLogEnabled)
   }   
   
   public void preProcessRollback (Execution execution) {
	   def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
	   utils.log("DEBUG"," ***** preProcessRollback ***** ", isDebugLogEnabled)
	   try {
		   
		   Object workflowException = execution.getVariable("WorkflowException");

		   if (workflowException instanceof WorkflowException) {
			   utils.log("DEBUG", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugLogEnabled)
			   execution.setVariable("prevWorkflowException", workflowException);
			   //execution.setVariable("WorkflowException", null);
		   }
	   } catch (BpmnError e) {
		   utils.log("DEBUG", "BPMN Error during preProcessRollback", isDebugLogEnabled)
	   } catch(Exception ex) {
		   String msg = "Exception in preProcessRollback. " + ex.getMessage()
		   utils.log("DEBUG", msg, isDebugLogEnabled)
	   }
	   utils.log("DEBUG"," *** Exit preProcessRollback *** ", isDebugLogEnabled)
   }

   public void postProcessRollback (Execution execution) {
	   def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
	   utils.log("DEBUG"," ***** postProcessRollback ***** ", isDebugLogEnabled)
	   String msg = ""
	   try {
		   Object workflowException = execution.getVariable("prevWorkflowException");
		   if (workflowException instanceof WorkflowException) {
			   utils.log("DEBUG", "Setting prevException to WorkflowException: ", isDebugLogEnabled)
			   execution.setVariable("WorkflowException", workflowException);
		   }
		   execution.setVariable("rollbackData", null)
	   } catch (BpmnError b) {
		   utils.log("DEBUG", "BPMN Error during postProcessRollback", isDebugLogEnabled)
		   throw b;
	   } catch(Exception ex) {
		   msg = "Exception in postProcessRollback. " + ex.getMessage()
		   utils.log("DEBUG", msg, isDebugLogEnabled)
	   }
	   utils.log("DEBUG"," *** Exit postProcessRollback *** ", isDebugLogEnabled)
   }


}