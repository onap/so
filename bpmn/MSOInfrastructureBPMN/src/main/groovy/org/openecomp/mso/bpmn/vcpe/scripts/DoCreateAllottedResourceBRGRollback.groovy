/*
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
package org.openecomp.mso.bpmn.vcpe.scripts;

import org.openecomp.mso.bpmn.common.scripts.*;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.springframework.web.util.UriUtils;
import static org.apache.commons.lang3.StringUtils.*

/**
 * This groovy class supports the <class>CreateAllottedResourceBRGRollback.bpmn</class> process.
 *
 * @author
 * 
 * Inputs:
 * @param - msoRequestId
 * @param - isDebugLogEnabled
 * @param - disableRollback - O 
 * @param - rollbackData
 *
 * Outputs:
 * @param - rollbackError 
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 *
 */
public class DoCreateAllottedResourceBRGRollback extends AbstractServiceTaskProcessor{

	private static final String DebugFlag = "isDebugLogEnabled"

	String Prefix="DCARBRGRB_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest (DelegateExecution execution) {
		
		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest *****",  isDebugEnabled)
		execution.setVariable("prefix", Prefix)
		String rbType = "DCARBRG_"
		try {
			
			def rollbackData = execution.getVariable("rollbackData")
			utils.log("DEBUG", "RollbackData:" + rollbackData, isDebugEnabled)

			if (rollbackData != null) {
				if (rollbackData.hasType(rbType)) {
					
					execution.setVariable("serviceInstanceId", rollbackData.get(rbType, "serviceInstanceId"))
					execution.setVariable("parentServiceInstanceId", rollbackData.get(rbType, "parentServiceInstanceId"))
					execution.setVariable("allottedResourceId", rollbackData.get("SERVICEINSTANCE", "allottedResourceId"))
					
					
					def rollbackAAI = rollbackData.get(rbType, "rollbackAAI")
					if ("true".equals(rollbackAAI))
					{
						execution.setVariable("rollbackAAI",true)
						execution.setVariable("aaiARPath", rollbackData.get(rbType, "aaiARPath"))
						
					}
					def rollbackSDNC = rollbackData.get(rbType, "rollbackSDNCassign")
					if ("true".equals(rollbackSDNC))
					{
						execution.setVariable("rollbackSDNC", true)
						execution.setVariable("deactivateSdnc", rollbackData.get(rbType, "rollbackSDNCactivate"))
						execution.setVariable("deleteSdnc",  rollbackData.get(rbType, "rollbackSDNCcreate"))
						execution.setVariable("unassignSdnc", rollbackData.get(rbType, "rollbackSDNCassign"))
						
						utils.log("DEBUG","sdncDeactivate:\n" + execution.getVariable("deactivateSdnc") , isDebugEnabled)
						utils.log("DEBUG","sdncDelete:\n" + execution.getVariable("deleteSdnc"), isDebugEnabled)
						utils.log("DEBUG","sdncUnassign:\n" + execution.getVariable("unassignSdnc"), isDebugEnabled)
						
						execution.setVariable("sdncDeactivateRequest", rollbackData.get(rbType, "sdncActivateRollbackReq"))
						execution.setVariable("sdncDeleteRequest",  rollbackData.get(rbType, "sdncCreateRollbackReq"))
						execution.setVariable("sdncUnassignRequest", rollbackData.get(rbType, "sdncAssignRollbackReq"))
					}

					if (execution.getVariable("rollbackAAI") != true && execution.getVariable("rollbackSDNC") != true)
					{
						execution.setVariable("skipRollback", true)
					}
				}
				else {
					execution.setVariable("skipRollback", true)
				}
			}
			else {
				execution.setVariable("skipRollback", true)
			}
			if (execution.getVariable("disableRollback").equals("true" ))
			{
				execution.setVariable("skipRollback", true)
			}
			
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	// aaiARPath set during query (existing AR)
	public void updateAaiAROrchStatus(DelegateExecution execution, String status){
		def isDebugEnabled = execution.getVariable(DebugFlag)
		String msg = null;
		utils.log("DEBUG", " *** updateAaiAROrchStatus ***", isDebugEnabled)
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String aaiARPath  = execution.getVariable("aaiARPath")
		utils.log("DEBUG", " aaiARPath:" + aaiARPath, isDebugEnabled)
		String ar = null; //need this for getting resourceVersion for delete
		if (!isBlank(aaiARPath))
		{
			ar = arUtils.getARbyLink(execution, aaiARPath, "")
		}
		if (isBlank(ar))
		{
			msg = "AR not found in AAI at:" + aaiARPath
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		utils.log("DEBUG", " *** Exit updateAaiAROrchStatus *** ", isDebugEnabled)
	}

	public void validateSDNCResp(DelegateExecution execution, String response, String method){

		def isDebugLogEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " *** ValidateSDNCResponse Process*** ", isDebugLogEnabled)
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			utils.logAudit("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			utils.logAudit("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				utils.log("DEBUG", "Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + response, isDebugLogEnabled)

			}else{

				utils.log("DEBUG", "Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			if ("404".contentEquals(e.getErrorCode()))
			{
				msg = "SDNC rollback " + method + " returned a 404. Proceding with rollback"
				utils.log("DEBUG", msg, isDebugLogEnabled)
			}
			else {
				throw e;
			}
		} catch(Exception ex) {
			msg = "Exception in validateSDNCResp. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logDebug(" *** Exit ValidateSDNCResp Process*** ", isDebugLogEnabled)
	}

	public void deleteAaiAR(DelegateExecution execution){
		def isDebugLogEnabled = execution.getVariable(DebugFlag)
		try{
			utils.log("DEBUG", " *** deleteAaiAR *** ", isDebugLogEnabled)
			AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
			String ar = null //need to get resource-version 
			String arLink = execution.getVariable("aaiARPath")
			if (!isBlank(arLink))
			{
				ar = arUtils.getARbyLink(execution, arLink, "")
			}
			arUtils.deleteAR(execution, arLink + '?resource-version=' + UriUtils.encode(execution.getVariable("aaiARResourceVersion"),"UTF-8"))
		} catch (BpmnError e) {
			throw e;
		}catch(Exception ex){
			utils.log("ERROR", "Exception Occurred Processing preProcessSDNCGetRequest. Exception is:\n" + ex, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + ex.getMessage())
		}
		utils.log("DEBUG", " *** Exit deleteAaiAR *** ", isDebugLogEnabled)
	}
	
	public void postProcessRequest(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** postProcessRequest ***** ", isDebugEnabled)
		String msg = ""
		try {
			execution.setVariable("rollbackData", null)
			boolean skipRollback = execution.getVariable("skipRollback")
			if (skipRollback != true)
			{
				execution.setVariable("rolledBack", true)
				utils.log("DEBUG","rolledBack", isDebugEnabled)
			}
			utils.log("DEBUG","*** Exit postProcessRequest ***", isDebugEnabled)

		} catch (BpmnError e) {
			msg = "Bpmn Exception in  postProcessRequest. "
			utils.log("DEBUG", msg, isDebugEnabled)
		} catch (Exception ex) {
			msg = "Exception in postProcessRequest. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}

	}
	
	public void processRollbackException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** processRollbackException ***** ", isDebugEnabled)
		try{
			utils.log("DEBUG", "Caught an Exception in DoCreateAllottedResourceRollback", isDebugEnabled)
			execution.setVariable("rollbackData", null)
			execution.setVariable("rolledBack", false)
			execution.setVariable("rollbackError", "Caught exception in AllottedResource Create Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			utils.log("DEBUG", "BPMN Error during processRollbackExceptions Method: ", isDebugEnabled)
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processRollbackExceptions Method: " + e.getMessage(), isDebugEnabled)
		}

		utils.log("DEBUG", " Exit processRollbackException", isDebugEnabled)
	}

	public void processRollbackJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG"," ***** processRollbackJavaException ***** ", isDebugEnabled)
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rolledBack", false)
			execution.setVariable("rollbackError", "Caught Java exception in AllottedResource Create Rollback")
			utils.log("DEBUG", "Caught Exception in processRollbackJavaException", isDebugEnabled)

		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception in processRollbackJavaException " + e.getMessage(), isDebugEnabled)
		}
		utils.log("DEBUG", "***** Exit processRollbackJavaException *****", isDebugEnabled)
	}

}
