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
 * with the rollback of a creation of a generic vnf and related VF modules.
 */
class DoCreateVnfAndModulesRollback extends AbstractServiceTaskProcessor {

	String Prefix="DCVAMR_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED DoCreateVnfAndModulesRollback PreProcessRequest Process*** ", isDebugEnabled)

		try{
			// Get Rollback Variables
			
			def rollbackData = execution.getVariable("RollbackData")
			utils.log("DEBUG", "Incoming RollbackData is: " + rollbackData.toString(), isDebugEnabled)
			String vnfId = rollbackData.get("VNF", "vnfId")
			utils.log("DEBUG", "Rollback vnfId is: " + vnfId, isDebugEnabled)
			execution.setVariable("DCVAMR_vnfId", vnfId)
			
			def numOfAddOnModulesString = rollbackData.get("VNFANDMODULES", "numOfCreatedAddOnModules")
			int numOfAddOnModules = 0
			if (numOfAddOnModulesString != null) {
				numOfAddOnModules = Integer.parseInt(numOfAddOnModulesString)				
			}
			execution.setVariable("DCVAMR_numOfAddOnModules", numOfAddOnModules)
			
			def baseVfModuleRollbackMap = rollbackData.get("VFMODULE_BASE")
			if (baseVfModuleRollbackMap == null) {
				// there are no VF Modules to delete
				execution.setVariable("DCVAMR_numOfModulesToDelete", 0)
			}
			else {
				execution.setVariable("DCVAMR_numOfModulesToDelete", numOfAddOnModules + 1)				
			}			
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in DoCreateVnfAndModulesRollback PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnfAndModulesRollback PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED DoCreateVnfAndModulesRollback PreProcessRequest Process ***", isDebugEnabled)
	}

	
	
	public void preProcessCreateVfModuleRollback(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessCreateVfModuleRollback ======== ", isDebugLogEnabled)
		
		try {
			
			def rollbackData = execution.getVariable("RollbackData")
			
			def vfModuleRollbackData = new RollbackData()
			
			def numOfModulesToDelete = execution.getVariable("DCVAMR_numOfModulesToDelete")
			logDebug("numOfModulesToDelete: " + numOfModulesToDelete, isDebugLogEnabled)
			def moduleMap = null
			
			if (numOfModulesToDelete > 1) {
				int addOnModuleIndex = numOfModulesToDelete - 1
				moduleMap = rollbackData.get("VFMODULE_ADDON_" + addOnModuleIndex)
				logDebug("Removing ADDON VF module # " + addOnModuleIndex, isDebugLogEnabled)
			}
			else {
				moduleMap = rollbackData.get("VFMODULE_BASE")
				logDebug("Removing BASE VF module", isDebugLogEnabled)
			}
			moduleMap.each{ k, v -> vfModuleRollbackData.put("VFMODULE", "${k}","${v}") }
			execution.setVariable("DCVAMR_RollbackData", vfModuleRollbackData)								
			
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessCreateVfModuleRollback. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessCreateVfModuleRollback Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessCreateVfModuleRollback ======== ", isDebugLogEnabled)
	}
	
	
	public void postProcessCreateVfModuleRollback(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED postProcessCreateVfModuleRollback ======== ", isDebugLogEnabled)
		
		try {			
			def numOfModulesToDelete = execution.getVariable("DCVAMR_numOfModulesToDelete")
			execution.setVariable("DCVAMR_numOfModulesToDelete", numOfModulesToDelete - 1)		
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing postProcessCreateVfModuleRollback. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during postProcessCreateVfModuleRollback Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED postProcessCreateVfModuleRollback ======== ", isDebugLogEnabled)
	}
	
	
	
}
