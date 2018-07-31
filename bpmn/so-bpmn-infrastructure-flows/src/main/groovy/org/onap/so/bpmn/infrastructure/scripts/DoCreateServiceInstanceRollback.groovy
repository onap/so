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

package org.onap.so.bpmn.infrastructure.scripts


import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.rest.APIResponse;
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.onap.so.logger.MsoLogger
import org.onap.so.logger.MessageEnum

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
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
public class DoCreateServiceInstanceRollback extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateServiceInstanceRollback.class);
	

	String Prefix="DCRESIRB_"

	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		msoLogger.trace("preProcessRequest")
		execution.setVariable("rollbackAAI",false)
		execution.setVariable("rollbackSDNC",false)

		try {
			def rollbackData = execution.getVariable("rollbackData")
			msoLogger.debug("RollbackData:" + rollbackData)

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
					msoLogger.debug("sdncDeactivate:\n" + sdncDeactivate)
					msoLogger.debug("sdncDelete:\n" + sdncDelete)
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
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest")
	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method) {
		
		msoLogger.trace("validateSDNCResponse")
		String msg = ""
		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			msoLogger.debug("SDNCResponse: " + response)
			msoLogger.debug("workflowException: " + workflowException)
			
			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msg = "SDNC Adapter service-instance rollback successful for " + method
				msoLogger.debug(msg)
			}else{
				execution.setVariable("rolledBack", false)
				msg =  "Error Response from SDNC Adapter service-instance rollback for " + method
				execution.setVariable("rollbackError", msg)
				msoLogger.debug(msg)
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in Create ServiceInstance rollback for "  + method  + " Exception:" + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit validateSDNCResponse")
	}

	public void postProcessRequest(DelegateExecution execution) {
		
		msoLogger.trace("postProcessRequest")
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
					msoLogger.debug("Error deleting service-instance in AAI for rollback", + serviceInstanceId)
				}
			}
			msoLogger.trace("Exit postProcessRequest")

		} catch (BpmnError e) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + e.getMessage()
			msoLogger.debug(msg)
		} catch (Exception ex) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + ex.getMessage()
			msoLogger.debug(msg)
		}

	}

	public void processRollbackException(DelegateExecution execution){
		
		msoLogger.trace("processRollbackException")
		try{
			msoLogger.debug("Caught an Exception in DoCreateServiceInstanceRollback")
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught exception in ServiceInstance Create Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			msoLogger.debug("BPMN Error during processRollbackExceptions Method: ")
		}catch(Exception e){
			msoLogger.debug("Caught Exception during processRollbackExceptions Method: " + e.getMessage())
		}

		msoLogger.debug("Exit processRollbackException")
	}

	public void processRollbackJavaException(DelegateExecution execution){
		
		msoLogger.trace("processRollbackJavaException")
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught Java exception in ServiceInstance Create Rollback")
			msoLogger.debug("Caught Exception in processRollbackJavaException")

		}catch(Exception e){
			msoLogger.debug("Caught Exception in processRollbackJavaException " + e.getMessage())
		}
		msoLogger.trace("Exit processRollbackJavaException")
	}

}
