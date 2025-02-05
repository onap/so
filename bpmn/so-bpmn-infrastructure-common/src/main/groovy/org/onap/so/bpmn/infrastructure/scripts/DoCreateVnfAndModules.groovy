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
import org.json.JSONObject;
import org.json.JSONArray;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ModuleResource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.DecomposeJsonUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.aai.groovyflows.AAICreateResources
import org.onap.so.logging.filter.base.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.logger.MessageEnum

import static org.apache.commons.lang3.StringUtils.*;



/**
* This class supports the macro VID Flow
* with the creation of a generic vnf and related VF modules.
*/
class DoCreateVnfAndModules extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger( DoCreateVnfAndModules.class);
   String Prefix="DCVAM_"
   ExceptionUtil exceptionUtil = new ExceptionUtil()
   JsonUtils jsonUtil = new JsonUtils()
   VidUtils vidUtils = new VidUtils(this)
   CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

   /**
	* This method gets and validates the incoming
	* request.
	*
	* @param - execution
	*/
   public void preProcessRequest(DelegateExecution execution) {
	   
	   execution.setVariable("prefix",Prefix)
	   logger.trace("STARTED DoCreateVnfAndModules PreProcessRequest Process")
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	   
	   setBasicDBAuthHeader(execution, isDebugLogEnabled)
	   try{
		   // Get Variables

		   ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

		   String vnfModelInfo = execution.getVariable("vnfModelInfo")

		   String requestId = execution.getVariable("msoRequestId")
		   execution.setVariable("requestId", requestId)
		   execution.setVariable("mso-request-id", requestId)
		   logger.debug("Incoming Request Id is: " + requestId)

		   String serviceInstanceId = execution.getVariable("serviceInstanceId")
		   logger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

		   String vnfName = execution.getVariable("vnfName")
		   execution.setVariable("CREVI_vnfName", vnfName)
		   logger.debug("Incoming Vnf Name is: " + vnfName)

		   String productFamilyId = execution.getVariable("productFamilyId")
		   logger.debug("Incoming Product Family Id is: " + productFamilyId)

		   String source = "VID"
		   execution.setVariable("source", source)
		   logger.debug("Incoming Source is: " + source)

		   String lcpCloudRegionId = execution.getVariable("lcpCloudRegionId")
		  logger.debug("Incoming LCP Cloud Region Id is: " + lcpCloudRegionId)

		   String tenantId = execution.getVariable("tenantId")
		   logger.debug("Incoming Tenant Id is: " + tenantId)

		   String disableRollback = execution.getVariable("disableRollback")
		   logger.debug("Incoming Disable Rollback is: " + disableRollback)

		   String asdcServiceModelVersion = execution.getVariable("asdcServiceModelVersion")
		   logger.debug("Incoming asdcServiceModelVersion: " + asdcServiceModelVersion)

		   String vnfId = execution.getVariable("testVnfId") // for junits
		   if(isBlank(vnfId)){
			   vnfId = execution.getVariable("vnfId")
			   if (isBlank(vnfId)) {
				   vnfId = UUID.randomUUID().toString()
				   logger.debug("Generated Vnf Id is: " + vnfId)
			   }
		   }
		   execution.setVariable("vnfId", vnfId)

		   Map<String,String> vfModuleNames = execution.getVariable("vfModuleNames")
		   logger.debug("Incoming vfModuleNames: " + vfModuleNames)

		   // Set aLaCarte to false
		   execution.setVariable("aLaCarte", false)

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

		   String delayMS = execution.getVariable("delayMS")
		   long longDelayMS = 20000;

		   if (delayMS != null && !delayMS.isEmpty()) {
			   longDelayMS = Long.parseLong(delayMS);
		   }

		   if (longDelayMS > 0) {
			   logger.debug("Delaying workflow " + longDelayMS + "ms");
			   sleep(longDelayMS)
		   }
	   }catch(BpmnError b){
		   logger.debug("Rethrowing MSOWorkflowException")
		   throw b
	   }catch(Exception e){
		   logger.debug(" Error Occured in DoCreateVnfAndModules PreProcessRequest method!" + e.getMessage())
		   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf PreProcessRequest")

	   }
	   logger.trace("COMPLETED DoCreateVnfAndModules PreProcessRequest Process")
   }

   public void queryCatalogDB (DelegateExecution execution) {
	 
	   execution.setVariable("prefix",Prefix)

	   logger.trace("STARTED DoCreateVnfAndModules QueryCatalogDB Process")
	   try {
		   VnfResource vnf = null
		   // if serviceDecomposition is specified, get info from serviceDecomposition
		   ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
		   if (serviceDecomposition != null) {
			   logger.debug("Getting Catalog DB data from ServiceDecomposition object: " + serviceDecomposition.toJsonString())
			   List<VnfResource> vnfs = serviceDecomposition.getVnfResources()
			   logger.debug("Read vnfs")
			   if (vnfs == null) {
				   logger.debug("Error - vnfs are empty in serviceDecomposition object")
				   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf queryCatalogDB, vnfs are empty")
			   }
			   vnf = vnfs[0]
			   String serviceModelName = serviceDecomposition.getModelInfo().getModelName()
			   vnf.constructVnfType(serviceModelName)
			   String vnfType = vnf.getVnfType()
			   logger.debug("Incoming Vnf Type is: " + vnfType)
			   execution.setVariable("vnfType", vnfType)
		   }
		   else {
			   //Get Vnf Info
			   String vnfModelInfo = execution.getVariable("vnfModelInfo")
			   logger.debug("vnfModelInfo: " + vnfModelInfo)
			   String vnfModelCustomizationUuid = jsonUtil.getJsonValueForKey(vnfModelInfo, "modelCustomizationUuid")
			   if (vnfModelCustomizationUuid == null) {
					   vnfModelCustomizationUuid = ""
			   }
			   logger.debug("querying Catalog DB by vnfModelCustomizationUuid: " + vnfModelCustomizationUuid)
			  
			   JSONArray vnfs = catalogDbUtils.getAllVnfsByVnfModelCustomizationUuid(execution,
							   vnfModelCustomizationUuid, "v1")
			   logger.debug("obtained VNF list")
			   // Only one match here
			   JSONObject vnfObject = vnfs[0]
			   vnf = DecomposeJsonUtil.jsonToVnfResource(vnfObject.toString())
		   }
		   logger.debug("Read vnfResource")
		   if (vnf == null) {
			   logger.debug("Error - vnf is empty in serviceDecomposition object")
			   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf queryCatalogDB, vnf is null")
		   }
		   execution.setVariable("vnfResourceDecomposition", vnf)

		   List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()
		   logger.debug("Read vfModules")
		   if (vfModules == null) {
			   logger.debug("Error - vfModules are empty in serviceDecomposition object")
			   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnf queryCatalogDB, vf modules are empty")
		   }
			   			  
		   ModuleResource baseVfModule = null
		   Map<String,String> vfModuleNames = execution.getVariable("vfModuleNames")

		   for (int i = 0; i < vfModules.size; i++) {
			   logger.debug("handling VF Module ")
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
					   String baseVfModuleName = getPredefinedVfModuleName(execution, basePersonaModelId)
					   execution.setVariable("baseVfModuleName", baseVfModuleName)
					   baseVfModule = vfModule
					   break
			   }		   
				
			}
			   
			List<ModuleResource>addOnModules = vfModules - baseVfModule
			   
			int addOnModulesToDeploy = 0
			if (addOnModules != null) {				
				   addOnModulesToDeploy = addOnModules.size
			}
			   

			execution.setVariable("addOnModules", addOnModules)
			execution.setVariable("addOnModulesToDeploy", addOnModulesToDeploy)
			execution.setVariable("addOnModulesDeployed", 0)	  

	   }catch(Exception ex) {
		   logger.debug("Error Occured in DoCreateVnfAndModules QueryCatalogDB Process " + ex.getMessage())
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

	   logger.trace("COMPLETED DoCreateVnfAndModules QueryCatalogDB Process")
   }

   public void preProcessAddOnModule(DelegateExecution execution){
	   
	   execution.setVariable("prefix", Prefix)
	   logger.debug("STARTED preProcessAddOnModule")

	   try {
		   List<ModuleResource>addOnModules = execution.getVariable("addOnModules")
		   int addOnIndex = (int) execution.getVariable("addOnModulesDeployed")

		   ModuleResource addOnModule = addOnModules[addOnIndex]
		   
		   logger.debug("Got addon module")

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
		   String addOnVfModuleName = getPredefinedVfModuleName(execution, addOnPersonaModelId)
		   execution.setVariable("addOnVfModuleName", addOnVfModuleName)
		   int addOnInitialCount = addOnModule.getInitialCount()
		   execution.setVariable("initialCount", addOnInitialCount)


	   }catch(Exception e){
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   "Exception Occured Processing preProcessAddOnModule ", "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
	   }
	   logger.trace("COMPLETED preProcessAddOnModule")
   }

   public void postProcessAddOnModule(DelegateExecution execution){
	   
	   execution.setVariable("prefix", Prefix)
	   logger.trace("STARTED postProcessAddOnModule")

	   try {
		   int addOnModulesDeployed = execution.getVariable("addOnModulesDeployed")
		   execution.setVariable("addOnModulesDeployed", addOnModulesDeployed + 1)

	   }catch(Exception e){
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   "Exception Occured Processing postProcessAddOnModule ", "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during postProcessAddOnModule Method:\n" + e.getMessage())
	   }
	   logger.trace("COMPLETED postProcessAddOnModule")
   }
   
   public void validateBaseModule(DelegateExecution execution){
	   
	   execution.setVariable("prefix", Prefix)
	   logger.trace("STARTED validateBaseModule")

	   try {
		   def baseRollbackData = execution.getVariable("DCVAM_baseRollbackData")
		   def rollbackData = execution.getVariable("rollbackData")

		   def baseModuleMap = baseRollbackData.get("VFMODULE")
		   baseModuleMap.each{ k, v -> rollbackData.put("VFMODULE_BASE", "${k}","${v}") }
		   execution.setVariable("rollbackData", rollbackData)
		   logger.debug("addOnModulesDeployed: " + execution.getVariable("addOnModulesDeployed"))
		   logger.debug("addOnModulesToDeploy: " + execution.getVariable("addOnModulesToDeploy"))
		   if (execution.getVariable("addOnModulesDeployed") <  execution.getVariable("addOnModulesToDeploy")) {
			   logger.debug("More add on modules to deploy")
		   }
		   else {
			   logger.debug("No more add on modules to deploy")
		   }

	   }catch(Exception e){
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   "Exception Occured Processing validateBaseModule ", "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during validateBaseModule Method:\n" + e.getMessage())
	   }
	   logger.trace("COMPLETED validateBaseModule")
   }

   public void validateAddOnModule(DelegateExecution execution){
	   
	   execution.setVariable("prefix", Prefix)
	   logger.trace("STARTED validateAddOnModule")

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
		   logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				   "Exception Occured Processing preProcessAddOnModule ", "BPMN",
				   ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
		   exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessAddOnModule Method:\n" + e.getMessage())
	   }
	   logger.trace("COMPLETED validateAddOnModule")
   }   
   
   public void preProcessRollback (DelegateExecution execution) {
	 
	   logger.trace("preProcessRollback")
	   try {
		   
		   Object workflowException = execution.getVariable("WorkflowException");

		   if (workflowException instanceof WorkflowException) {
			   logger.debug("Prev workflowException: " + workflowException.getErrorMessage())
			   execution.setVariable("prevWorkflowException", workflowException);
			   //execution.setVariable("WorkflowException", null);
		   }
	   } catch (BpmnError e) {
		   logger.debug("BPMN Error during preProcessRollback")
	   } catch(Exception ex) {
		   String msg = "Exception in preProcessRollback. " + ex.getMessage()
		   logger.debug(msg)
	   }
	   logger.trace("Exit preProcessRollback")
   }

   public void postProcessRollback (DelegateExecution execution) {
	 
	   logger.trace("postProcessRollback")
	   String msg = ""
	   try {
		   Object workflowException = execution.getVariable("prevWorkflowException");
		   if (workflowException instanceof WorkflowException) {
			   logger.debug("Setting prevException to WorkflowException: ")
			   execution.setVariable("WorkflowException", workflowException);
		   }
		   execution.setVariable("rollbackData", null)
	   } catch (BpmnError b) {
		   logger.debug("BPMN Error during postProcessRollback")
		   throw b;
	   } catch(Exception ex) {
		   msg = "Exception in postProcessRollback. " + ex.getMessage()
		   logger.debug(msg)
	   }
	   logger.trace("Exit postProcessRollback")
   }
   
   public void createPlatform (DelegateExecution execution) {
	 
	   logger.trace("START createPlatform")
	   
	   String platformName = execution.getVariable("platformName")
	   String vnfId = execution.getVariable("vnfId")
   
	   logger.debug("Platform NAME: " + platformName)
	   logger.debug("VnfID: " + vnfId)
	   
	   if(isBlank(platformName)){
		   logger.debug("platformName was not found. Continuing on with flow...")
	   }else{
		   logger.debug("platformName was found.")
		   try{
			   AAICreateResources aaiCR = new AAICreateResources()
			   aaiCR.createAAIPlatform(platformName, vnfId)
		   }catch(Exception ex){
			   String msg = "Exception in createPlatform. " + ex.getMessage();
			   logger.debug(msg)
		   }
	   }
	   logger.trace("Exit createPlatform")
   }
   
   public void createLineOfBusiness (DelegateExecution execution) {
	 
	   logger.trace("START createLineOfBusiness")
	   
	   String lineOfBusiness = execution.getVariable("lineOfBusiness")
	   String vnfId = execution.getVariable("vnfId")
   
	   logger.debug("LineOfBusiness NAME: " + lineOfBusiness)
	   logger.debug("VnfID: " + vnfId)
	   
	   if(isBlank(lineOfBusiness)){
		   logger.debug("LineOfBusiness was not found. Continuing on with flow...")
	   }else{
		   logger.debug("LineOfBusiness was found.")
		   try{
			   AAICreateResources aaiCR = new AAICreateResources()
			   aaiCR.createAAILineOfBusiness(lineOfBusiness, vnfId)
		   }catch(Exception ex){
			   String msg = "Exception in LineOfBusiness. " + ex.getMessage();
			    logger.debug(msg)
		   }
	   }
	   logger.trace("Exit createLineOfBusiness")
   }

   public String getPredefinedVfModuleName(DelegateExecution execution, String vfModuleModelInvariantUuid) {
	   Map<String,String> vfModuleNames = execution.getVariable("vfModuleNames")
		   
	   if (vfModuleNames == null) {
		   return null
	   }

	   String vfModuleName = vfModuleNames.get(vfModuleModelInvariantUuid)

	   if (vfModuleName != null) {
		   logger.debug("Using vfModuleName='" + vfModuleName + "' for vfModuleModelInvariantUuid=" + vfModuleModelInvariantUuid)
	   }

	   return vfModuleName
   }
}
