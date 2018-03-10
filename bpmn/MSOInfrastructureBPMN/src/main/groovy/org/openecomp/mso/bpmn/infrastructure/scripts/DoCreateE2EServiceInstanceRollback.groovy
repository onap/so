/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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


import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;
/**
 * This groovy class supports the <class>DoCreateServiceInstanceRollback.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - rollbackData with
 *          globalCustomerId
 * 			subscriptionServiceType
 * 			serviceInstanceId
 * 			disableRollback
 * 			rollbackAAI
 * 			rollbackSDNC
 * 			sdncRollbackRequest
 * 
 *
 * Outputs:
 * @param - rollbackError
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 *
 */
public class DoCreateE2EServiceInstanceRollback extends AbstractServiceTaskProcessor{

	String Prefix="DCRESIRB_"

	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest *****",  isDebugEnabled)
		execution.setVariable("rollbackAAI",false)
		execution.setVariable("rollbackSDNC",false)

		try {
			def rollbackData = execution.getVariable("rollbackData")
			utils.log("DEBUG", "RollbackData:" + rollbackData, isDebugEnabled)

			if (rollbackData != null) {
				if (rollbackData.hasType("SERVICEINSTANCE")) {

					def serviceInstanceId = rollbackData.get("SERVICEINSTANCE", "serviceInstanceId")
					execution.setVariable("serviceInstanceId", serviceInstanceId)

					def subscriptionServiceType = rollbackData.get("SERVICEINSTANCE", "subscriptionServiceType")
					execution.setVariable("subscriptionServiceType", subscriptionServiceType)

					def globalSubscriberId  = rollbackData.get("SERVICEINSTANCE", "globalSubscriberId")
					execution.setVariable("globalSubscriberId", globalSubscriberId)

					def rollbackAAI = rollbackData.get("SERVICEINSTANCE", "rollbackAAI")
					if ("true".equals(rollbackAAI))
					{
						execution.setVariable("rollbackAAI",true)
					}

					def rollbackSDNC = rollbackData.get("SERVICEINSTANCE", "rollbackSDNC")
					if ("true".equals(rollbackSDNC))
					{
						execution.setVariable("rollbackSDNC", true)
					}

					if (execution.getVariable("rollbackAAI") != true && execution.getVariable("rollbackSDNC") != true)
					{
						execution.setVariable("skipRollback", true)
					}

					def sdncDelete = rollbackData.get("SERVICEINSTANCE", "sdncDelete")
					execution.setVariable("sdncDelete", sdncDelete)
					def sdncDeactivate = rollbackData.get("SERVICEINSTANCE", "sdncDeactivate")
					execution.setVariable("sdncDeactivate", sdncDeactivate)
					utils.log("DEBUG","sdncDeactivate:\n" + sdncDeactivate, isDebugEnabled)
					utils.log("DEBUG","sdncDelete:\n" + sdncDelete, isDebugEnabled)
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

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in Create ServiceInstance Rollback preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	public void validateSDNCResponse(Execution execution, String response, String method) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** validateSDNCResponse ***** ", isDebugEnabled)
		String msg = ""
		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			utils.log("DEBUG", "SDNCResponse: " + response, isDebugEnabled)
			utils.log("DEBUG", "workflowException: " + workflowException, isDebugEnabled)
			
			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msg = "SDNC Adapter service-instance rollback successful for " + method
				utils.log("DEBUG", msg, isDebugEnabled)
			}else{
				execution.setVariable("rolledBack", false)
				msg =  "Error Response from SDNC Adapter service-instance rollback for " + method
				execution.setVariable("rollbackError", msg)
				utils.log("DEBUG", msg, isDebugEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in Create ServiceInstance rollback for "  + method  + " Exception:" + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit validateSDNCResponse ***** ", isDebugEnabled)
	}

	public void postProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessRequest ***** ", isDebugEnabled)
		String msg = ""
		try {
			execution.setVariable("rollbackData", null)
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean rollbackAAI = execution.getVariable("rollbackAAI")
			boolean rollbackSDNC = execution.getVariable("rollbackSDNC")
			if (rollbackAAI || rollbackSDNC)
			{
				execution.setVariable("rolledBack", true)
			}
			if (rollbackAAI)
			{
				boolean succInAAI = execution.getVariable("GENDS_SuccessIndicator")
				if(!succInAAI){
					execution.setVariable("rolledBack", false) //both sdnc and aai must be successful to declare rollback Succesful
					execution.setVariable("rollbackError", "Error deleting service-instance in AAI for rollback")
					utils.log("DEBUG","Error deleting service-instance in AAI for rollback", + serviceInstanceId, isDebugEnabled)
				}
			}
			utils.log("DEBUG","*** Exit postProcessRequest ***", isDebugEnabled)

		} catch (BpmnError e) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + e.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		} catch (Exception ex) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}

	}

	public void processRollbackException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** processRollbackException ***** ", isDebugEnabled)
		try{
			utils.log("DEBUG", "Caught an Exception in DoCreateServiceInstanceRollback", isDebugEnabled)
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught exception in ServiceInstance Create Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			utils.log("DEBUG", "BPMN Error during processRollbackExceptions Method: ", isDebugEnabled)
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processRollbackExceptions Method: " + e.getMessage(), isDebugEnabled)
		}

		utils.log("DEBUG", " Exit processRollbackException", isDebugEnabled)
	}

	public void processRollbackJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** processRollbackJavaException ***** ", isDebugEnabled)
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught Java exception in ServiceInstance Create Rollback")
			utils.log("DEBUG", "Caught Exception in processRollbackJavaException", isDebugEnabled)

		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception in processRollbackJavaException " + e.getMessage(), isDebugEnabled)
		}
		utils.log("DEBUG", "***** Exit processRollbackJavaException *****", isDebugEnabled)
	}

}
