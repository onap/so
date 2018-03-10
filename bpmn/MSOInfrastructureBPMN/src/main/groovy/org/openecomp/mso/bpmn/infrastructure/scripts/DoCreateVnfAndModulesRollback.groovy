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

import org.json.JSONObject;
import org.json.JSONArray;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;

import static org.apache.commons.lang3.StringUtils.*;

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
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
			
			def rollbackData = execution.getVariable("rollbackData")
			utils.log("DEBUG", "Incoming RollbackData is: " + rollbackData.toString(), isDebugEnabled)
			execution.setVariable("rolledBack", null)
			execution.setVariable("rollbackError", null)
			
			if (execution.getVariable("disableRollback").equals("true" ))
			{
				execution.setVariable("skipRollback", true)
			}

			String vnfId = rollbackData.get("VNF", "vnfId")
			utils.log("DEBUG", "Rollback vnfId is: " + vnfId, isDebugEnabled)
			execution.setVariable("DCVAMR_vnfId", vnfId)
			
			execution.setVariable("mso-request-id", execution.getVariable("msoRequestId"))
			
			execution.setVariable("DCVAMR_rollbackSDNCAssign", rollbackData.get("VNF", "rollbackSDNCAssign"))
			execution.setVariable("DCVAMR_rollbackSDNCActivate", rollbackData.get("VNF", "rollbackSDNCActivate"))
			execution.setVariable("DCVAMR_rollbackVnfCreate", rollbackData.get("VNF", "rollbackVnfCreate"))
			
			String sdncCallbackUrl = rollbackData.get("VNF", "sdncCallbackUrl")
			utils.log("DEBUG", "Rollback sdncCallbackUrl is: " + sdncCallbackUrl, isDebugEnabled)
			execution.setVariable("DCVAMR_sdncCallbackUrl", sdncCallbackUrl)
			
			String tenantId= rollbackData.get("VNF", "tenantId")
			utils.log("DEBUG", "Rollback tenantId is: " + tenantId, isDebugEnabled)
			execution.setVariable("DCVAMR_tenantId", tenantId)
			
			String source= rollbackData.get("VNF", "source")
			utils.log("DEBUG", "Rollback source is: " + source, isDebugEnabled)
			execution.setVariable("DCVAMR_source", source)
			
			String serviceInstanceId = rollbackData.get("VNF", "serviceInstanceId")
			utils.log("DEBUG", "Rollback serviceInstanceId is: " + serviceInstanceId, isDebugEnabled)
			execution.setVariable("DCVAMR_serviceInstanceId", serviceInstanceId)
			
			String cloudSiteId = rollbackData.get("VNF", "cloudSiteId")
			utils.log("DEBUG", "Rollback cloudSiteId is: " + cloudSiteId, isDebugEnabled)
			execution.setVariable("DCVAMR_cloudSiteId", cloudSiteId)
			
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
			
			def rollbackData = execution.getVariable("rollbackData")
			
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
		def rolledBack = false
		
		try {
			rolledBack = execution.getVariable("DCVM_rolledBack")
			def numOfModulesToDelete = execution.getVariable("DCVAMR_numOfModulesToDelete")
			execution.setVariable("DCVAMR_numOfModulesToDelete", numOfModulesToDelete - 1)		
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing postProcessCreateVfModuleRollback. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during postProcessCreateVfModuleRollback Method:\n" + e.getMessage())
		}
		if (!rolledBack) {
			logDebug("Failure on DoCreateVfModuleRollback", isDebugLogEnabled)
			utils.log("ERROR", "Unsuccessful rollback of DoCreateVfModule")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during rollback of DoCreateVfModule")
		}
		logDebug("======== COMPLETED postProcessCreateVfModuleRollback ======== ", isDebugLogEnabled)
	}
	
	
	public void preProcessSDNCDeactivateRequest(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
		def vnfId = execution.getVariable("vnfId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")

		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")

			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable(Prefix + "deactivateSDNCRequest", deactivateSDNCRequest)
			logDebug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCDeactivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
	}
	
	public void preProcessSDNCUnassignRequest(Execution execution) {
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

			execution.setVariable(Prefix + "unassignSDNCRequest", unassignSDNCRequest)
			logDebug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UnassignSDNCRequest is: \n"  + unassignSDNCRequest)

		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
	}
	
	public String buildSDNCRequest(Execution execution, String svcInstId, String action){
		
				String uuid = execution.getVariable('testReqId') // for junits
				if(uuid==null){
					uuid = execution.getVariable("msoRequestId") + "-" +  	System.currentTimeMillis()
				}
				def callbackURL = execution.getVariable(Prefix + "sdncCallbackUrl")
				def requestId = execution.getVariable("msoRequestId")				
				def tenantId = execution.getVariable(Prefix + "tenantId")
				def source = execution.getVariable(Prefix + "source")
				def vnfId = execution.getVariable(Prefix + "vnfId")
				def serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")
				def cloudSiteId = execution.getVariable(Prefix + "cloudSiteId")
				
				String sdncRequest =
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
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
		
	public void validateSDNCResponse(Execution execution, String response, String method){
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
	
	public void setSuccessfulRollbackStatus (Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED setSuccessfulRollbackStatus ======== ", isDebugLogEnabled)
	
		try{
			// Set rolledBack to true, rollbackError to null
			execution.setVariable("rolledBack", true)
			execution.setVariable("rollbackError", null)
	
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing setSuccessfulRollbackStatus. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setSuccessfulRollbackStatus Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED setSuccessfulRollbackStatus ======== ", isDebugLogEnabled)
	}
	
	public void setFailedRollbackStatus (Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED setFailedRollbackStatus ======== ", isDebugLogEnabled)
	
		try{
			// Set rolledBack to false, rollbackError to actual value, rollbackData to null
			execution.setVariable("rolledBack", false)
			def rollbackError = execution.getVariable("rollbackError")
			if (rollbackError == null) {
				execution.setVariable("rollbackError", 'Caught exception in DoCreateVnfAndModulesRollback')
			}
			execution.setVariable("rollbackData", null)
	
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing setFailedRollbackStatus. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setFailedRollbackStatus Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED setFailedRollbackStatus ======== ", isDebugLogEnabled)
	}
	
	
}
