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

package org.onap.so.bpmn.vcpe.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.AllottedResource
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.AllottedResourceUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

import static org.apache.commons.lang3.StringUtils.isBlank

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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateAllottedResourceBRGRollback.class);

	String Prefix="DCARBRGRB_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest (DelegateExecution execution) {
		

		String msg = ""
		msoLogger.trace("start preProcessRequest")
		execution.setVariable("prefix", Prefix)
		String rbType = "DCARBRG_"
		try {
			
			def rollbackData = execution.getVariable("rollbackData")
			msoLogger.debug("RollbackData:" + rollbackData)

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
						
						msoLogger.debug("sdncDeactivate:\n" + execution.getVariable("deactivateSdnc") )
						msoLogger.debug("sdncDelete:\n" + execution.getVariable("deleteSdnc"))
						msoLogger.debug("sdncUnassign:\n" + execution.getVariable("unassignSdnc"))
						
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
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end preProcessRequest")
	}

	// aaiARPath set during query (existing AR)
	public void updateAaiAROrchStatus(DelegateExecution execution, String status){

		String msg = null;
		msoLogger.trace("start updateAaiAROrchStatus")
		AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
		String aaiARPath  = execution.getVariable("aaiARPath")
		msoLogger.debug(" aaiARPath:" + aaiARPath)
		Optional<AllottedResource> ar = Optional.empty(); //need this for getting resourceVersion for delete
		if (!isBlank(aaiARPath))
		{
			ar = arUtils.getARbyLink(execution, aaiARPath, "")
		}
		if(!ar.isPresent())
		{
			msg = "AR not found in AAI at:" + aaiARPath
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}
		String orchStatus = arUtils.updateAROrchStatus(execution, status, aaiARPath)
		msoLogger.trace("end updateAaiAROrchStatus")
	}

	public void validateSDNCResp(DelegateExecution execution, String response, String method){


		msoLogger.trace("start ValidateSDNCResponse Process")
		String msg = ""

		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			msoLogger.debug("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			msoLogger.debug("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msoLogger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + response)

			}else{

				msoLogger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			if ("404".contentEquals(e.getErrorCode()))
			{
				msg = "SDNC rollback " + method + " returned a 404. Proceding with rollback"
				msoLogger.debug(msg)
			}
			else {
				throw e;
			}
		} catch(Exception ex) {
			msg = "Exception in validateSDNCResp. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("end ValidateSDNCResp Process")
	}

	public void deleteAaiAR(DelegateExecution execution){

		try{
			msoLogger.trace("start deleteAaiAR")
			AllottedResourceUtils arUtils = new AllottedResourceUtils(this)
			String arLink = execution.getVariable("aaiARPath")
			arUtils.deleteAR(execution, arLink )
		} catch (BpmnError e) {
			throw e;
		}catch(Exception ex){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occurred Processing preProcessSDNCGetRequest.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + ex);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during SDNC GET Method:\n" + ex.getMessage())
		}
		msoLogger.trace("end deleteAaiAR")
	}
	
	public void postProcessRequest(DelegateExecution execution) {

		msoLogger.trace("start postProcessRequest")
		String msg = ""
		try {
			execution.setVariable("rollbackData", null)
			boolean skipRollback = execution.getVariable("skipRollback")
			if (skipRollback != true)
			{
				execution.setVariable("rolledBack", true)
				msoLogger.debug("rolledBack")
			}
			msoLogger.trace("end postProcessRequest")

		} catch (BpmnError e) {
			msg = "Bpmn Exception in  postProcessRequest. "
			msoLogger.debug(msg)
		} catch (Exception ex) {
			msg = "Exception in postProcessRequest. " + ex.getMessage()
			msoLogger.debug(msg)
		}

	}
	
	public void processRollbackException(DelegateExecution execution){

		msoLogger.trace("start processRollbackException")
		try{
			msoLogger.debug("Caught an Exception in DoCreateAllottedResourceRollback")
			execution.setVariable("rollbackData", null)
			execution.setVariable("rolledBack", false)
			execution.setVariable("rollbackError", "Caught exception in AllottedResource Create Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			msoLogger.debug("BPMN Error during processRollbackExceptions Method: ")
		}catch(Exception e){
			msoLogger.debug("Caught Exception during processRollbackExceptions Method: " + e.getMessage())
		}

		msoLogger.trace("end processRollbackException")
	}

	public void processRollbackJavaException(DelegateExecution execution){

		msoLogger.trace("start processRollbackJavaException")
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rolledBack", false)
			execution.setVariable("rollbackError", "Caught Java exception in AllottedResource Create Rollback")
			msoLogger.debug("Caught Exception in processRollbackJavaException")

		}catch(Exception e){
			msoLogger.debug("Caught Exception in processRollbackJavaException " + e.getMessage())
		}
		msoLogger.trace("end processRollbackJavaException")
	}

}
