/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;
/**
 * This groovy class supports the <class>DoUpdateE2EServiceInstanceRollback.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - rollbackData with
 *          globalCustomerId
 * 			subscriptionServiceType
 * 			serviceInstanceId
 * 			disableRollback
 * 			rollbackAAI
 * 			rollbackAdded
 * 			rollbackDeleted
 *
 *
 * Outputs:
 * @param - rollbackError
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 *
 */
public class DoUpdateE2EServiceInstanceRollback extends AbstractServiceTaskProcessor{

	String Prefix="DUPDSIRB_"

	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest *****",  isDebugEnabled)
		execution.setVariable("rollbackAAI",false)
		execution.setVariable("rollbackAdded",false)
		execution.setVariable("rollbackDeleted",false)
		
		List addResourceList = execution.getVariable("addResourceList")
        List delResourceList = execution.getVariable("delResourceList")
        execution.setVariable("addResourceList_o",  addResourceList)
        execution.setVariable("delResourceList_o",  delResourceList)
        //exchange add and delete resource list
        execution.setVariable("addResourceList",  delResourceList)        
        execution.setVariable("delResourceList",  addResourceList)

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

					def rollbackAdded = rollbackData.get("SERVICEINSTANCE", "rollbackAdded")
					if ("true".equals(rollbackAdded))
					{
						execution.setVariable("rollbackAdded", true)
					}
					
					def rollbackDeleted = rollbackData.get("SERVICEINSTANCE", "rollbackDeleted")
					if ("true".equals(rollbackDeleted))
					{
						execution.setVariable("rollbackDeleted", true)
					}					

					if (execution.getVariable("rollbackAAI") != true && execution.getVariable("rollbackAdded") != true 
					  && execution.getVariable("rollbackDeleted") != true)
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

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in Update ServiceInstance Rollback preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	public void postProcessRequest(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessRequest ***** ", isDebugEnabled)
		String msg = ""
		try {
			execution.setVariable("rollbackData", null)
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean rollbackAAI = execution.getVariable("rollbackAAI")
			boolean rollbackAdded = execution.getVariable("rollbackAdded")
			boolean rollbackDeleted = execution.getVariable("rollbackDeleted")
			
			List addResourceList = execution.getVariable("addResourceList_o")
			List delResourceList = execution.getVariable("delResourceList_o")
			execution.setVariable("addResourceList",  addResourceList)
			execution.setVariable("delResourceList",  delResourceList)

			if (rollbackAAI || rollbackAdded || rollbackDeleted)
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
	
    
    public void preProcessForAddResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
    }

    public void postProcessForAddResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
    
    }
    
    public void preProcessForDeleteResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

    }

    public void postProcessForDeleteResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
    
    } 

	public void preProcessAAIGET(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")	
	}
    	
	public void postProcessAAIGET(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIGET ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				utils.log("INFO","Error getting Service-instance from AAI in postProcessAAIGET", + serviceInstanceName, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
					utils.log("INFO", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					String aaiService = execution.getVariable("GENGS_service")
					if (!isBlank(aaiService) && (utils.nodeExists(aaiService, "resource-version"))) {
						execution.setVariable("serviceInstanceVersion_n",  utils.getNodeText1(aaiService, "resource-version"))
						utils.log("INFO","Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"), isDebugEnabled)
					}
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIGET *** ", isDebugEnabled)
	}    

	public void preProcessAAIPUT(DelegateExecution execution) {		
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String msg = ""
		utils.log("INFO"," ***** preProcessAAIPUT *****",  isDebugEnabled)

		String modelUuid = execution.getVariable("model-version-id-original")
		String serviceInstanceVersion = execution.getVariable("serviceInstanceVersion_n")
		execution.setVariable("GENPS_serviceResourceVersion", serviceInstanceVersion)

		String serviceInstanceData =
				"""<service-instance xmlns=\"${namespace}\">
			       <resource-version">${modelUuid}</resource-version>
				 </service-instance>""".trim()

		execution.setVariable("serviceInstanceData", serviceInstanceData)
		utils.log("INFO","serviceInstanceData: " + serviceInstanceData, isDebugEnabled)
		utils.logAudit(serviceInstanceData)
		utils.log("INFO", " aai_uri " + aai_uri + " namespace:" + namespace, isDebugEnabled)
		utils.log("INFO", " 'payload' to update Service Instance in AAI - " + "\n" + serviceInstanceData, isDebugEnabled)
	
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}	
	
	public void postProcessAAIPUT(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIPUT ***** ", isDebugEnabled)
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean succInAAI = execution.getVariable("GENPS_SuccessIndicator")
			if(!succInAAI){
				utils.log("INFO","Error putting Service-instance in AAI", + serviceInstanceId, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
			}
			else
			{
				
			}

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIDEL. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIPUT *** ", isDebugEnabled)
	}	

	public void processRollbackException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** processRollbackException ***** ", isDebugEnabled)
		try{
			utils.log("DEBUG", "Caught an Exception in DoUpdateE2EServiceInstanceRollback", isDebugEnabled)
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught exception in ServiceInstance Update Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			utils.log("DEBUG", "BPMN Error during processRollbackExceptions Method: ", isDebugEnabled)
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processRollbackExceptions Method: " + e.getMessage(), isDebugEnabled)
		}

		utils.log("DEBUG", " Exit processRollbackException", isDebugEnabled)
	}

	public void processRollbackJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** processRollbackJavaException ***** ", isDebugEnabled)
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught Java exception in ServiceInstance Update Rollback")
			utils.log("DEBUG", "Caught Exception in processRollbackJavaException", isDebugEnabled)

		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception in processRollbackJavaException " + e.getMessage(), isDebugEnabled)
		}
		utils.log("DEBUG", "***** Exit processRollbackJavaException *****", isDebugEnabled)
	}

}
