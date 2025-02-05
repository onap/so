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
import org.onap.so.logging.filter.base.ErrorCode
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class supports the macro VID Flow
 * with the rollback of a creation of a generic vnf and related VF modules.
 */
class DoCreateVnfAndModulesRollback extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger( DoCreateVnfAndModulesRollback.class);
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
	public void preProcessRequest(DelegateExecution execution) {
		
		execution.setVariable("prefix",Prefix)
		logger.debug("STARTED DoCreateVnfAndModulesRollback PreProcessRequest Process")

		try{
			// Get Rollback Variables
			
			def rollbackData = execution.getVariable("rollbackData")
			logger.debug("Incoming RollbackData is: " + rollbackData.toString())
			execution.setVariable("rolledBack", null)
			execution.setVariable("rollbackError", null)
			
			if (execution.getVariable("disableRollback").equals("true" ))
			{
				execution.setVariable("skipRollback", true)
			}

			String vnfId = rollbackData.get("VNF", "vnfId")
			logger.debug("Rollback vnfId is: " + vnfId)
			execution.setVariable("DCVAMR_vnfId", vnfId)
			
			execution.setVariable("mso-request-id", execution.getVariable("msoRequestId"))
			
			execution.setVariable("DCVAMR_rollbackSDNCAssign", rollbackData.get("VNF", "rollbackSDNCAssign"))
			execution.setVariable("DCVAMR_rollbackSDNCActivate", rollbackData.get("VNF", "rollbackSDNCActivate"))
			execution.setVariable("DCVAMR_rollbackVnfCreate", rollbackData.get("VNF", "rollbackVnfCreate"))
			
			String sdncCallbackUrl = rollbackData.get("VNF", "sdncCallbackUrl")
			logger.debug("Rollback sdncCallbackUrl is: " + sdncCallbackUrl)
			execution.setVariable("DCVAMR_sdncCallbackUrl", sdncCallbackUrl)
			
			String tenantId= rollbackData.get("VNF", "tenantId")
			logger.debug("Rollback tenantId is: " + tenantId)
			execution.setVariable("DCVAMR_tenantId", tenantId)
			
			String source= rollbackData.get("VNF", "source")
			logger.debug("Rollback source is: " + source)
			execution.setVariable("DCVAMR_source", source)
			
			String serviceInstanceId = rollbackData.get("VNF", "serviceInstanceId")
			logger.debug("Rollback serviceInstanceId is: " + serviceInstanceId)
			execution.setVariable("DCVAMR_serviceInstanceId", serviceInstanceId)
			
			String cloudSiteId = rollbackData.get("VNF", "cloudSiteId")
			logger.debug("Rollback cloudSiteId is: " + cloudSiteId)
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
			
			// Set aLaCarte to false
			execution.setVariable("DCVAMR_aLaCarte", false)			
			
		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.debug(" Error Occured in DoCreateVnfAndModulesRollback PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoCreateVnfAndModulesRollback PreProcessRequest")

		}
		logger.trace("COMPLETED DoCreateVnfAndModulesRollback PreProcessRequest Process")
	}

	
	
	public void preProcessCreateVfModuleRollback(DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessCreateVfModuleRollback")
		
		try {
			
			def rollbackData = execution.getVariable("rollbackData")
			
			def vfModuleRollbackData = new RollbackData()
			
			def numOfModulesToDelete = execution.getVariable("DCVAMR_numOfModulesToDelete")
			logger.debug("numOfModulesToDelete: " + numOfModulesToDelete)
			def moduleMap = null
			
			if (numOfModulesToDelete > 1) {
				int addOnModuleIndex = numOfModulesToDelete - 1
				moduleMap = rollbackData.get("VFMODULE_ADDON_" + addOnModuleIndex)
				logger.debug("Removing ADDON VF module # " + addOnModuleIndex)
			}
			else {
				moduleMap = rollbackData.get("VFMODULE_BASE")
				logger.debug("Removing BASE VF module")
			}
			moduleMap.each{ k, v -> vfModuleRollbackData.put("VFMODULE", "${k}","${v}") }
			execution.setVariable("DCVAMR_RollbackData", vfModuleRollbackData)								
			
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessCreateVfModuleRollback ", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessCreateVfModuleRollback Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessCreateVfModuleRollback")
	}
	
	
	public void postProcessCreateVfModuleRollback(DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED postProcessCreateVfModuleRollback")
		def rolledBack = false
		
		try {
			rolledBack = execution.getVariable("DCVM_rolledBack")
			def numOfModulesToDelete = execution.getVariable("DCVAMR_numOfModulesToDelete")
			execution.setVariable("DCVAMR_numOfModulesToDelete", numOfModulesToDelete - 1)		
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing postProcessCreateVfModuleRollback ", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during postProcessCreateVfModuleRollback Method:\n" + e.getMessage())
		}
		if (rolledBack == false) {
			logger.debug("Failure on DoCreateVfModuleRollback")
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Unsuccessful rollback of DoCreateVfModule ", "BPMN",
					ErrorCode.UnknownError.getValue());
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during rollback of DoCreateVfModule")
		}
		logger.trace("COMPLETED postProcessCreateVfModuleRollback")
	}
	
	
	public void preProcessSDNCDeactivateRequest(DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCDeactivateRequest")
		def vnfId = execution.getVariable("vnfId")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")

		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")

			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable(Prefix + "deactivateSDNCRequest", deactivateSDNCRequest)
			logger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessSDNCDeactivateRequest ", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED preProcessSDNCDeactivateRequest")
	}
	
	public void preProcessSDNCUnassignRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED preProcessSDNCUnassignRequest Process")
		try{
			String vnfId = execution.getVariable("vnfId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")

			execution.setVariable(Prefix + "unassignSDNCRequest", unassignSDNCRequest)
			logger.debug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest)

		}catch(Exception e){
			logger.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED  preProcessSDNCUnassignRequest Process")
	}
	
	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){
		
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
				"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
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
		logger.debug("STARTED ValidateSDNCResponse Process")

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		logger.debug("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		String sdncResponse = response
		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
		}else{
			logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
			throw new BpmnError("MSOWorkflowException")
		}
		logger.debug("COMPLETED ValidateSDNCResponse Process")
	}
	
	public void setSuccessfulRollbackStatus (DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED setSuccessfulRollbackStatus")
	
		try{
			// Set rolledBack to true, rollbackError to null
			execution.setVariable("rolledBack", true)
			execution.setVariable("rollbackError", null)
	
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Exception Occured " +
					"Processing setSuccessfulRollbackStatus ", "BPMN", ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setSuccessfulRollbackStatus Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED setSuccessfulRollbackStatus")
	}
	
	public void setFailedRollbackStatus (DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED setFailedRollbackStatus")
	
		try{
			// Set rolledBack to false, rollbackError to actual value, rollbackData to null
			execution.setVariable("rolledBack", false)
			def rollbackError = execution.getVariable("rollbackError")
			if (rollbackError == null) {
				execution.setVariable("rollbackError", 'Caught exception in DoCreateVnfAndModulesRollback')
			}
			execution.setVariable("rollbackData", null)
	
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Exception Occured " +
					"Processing setFailedRollbackStatus. ", "BPMN",ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setFailedRollbackStatus Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED setFailedRollbackStatus")
	}
	
	
}
