/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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


import static org.apache.commons.lang3.StringUtils.*;

import groovy.xml.XmlUtil
import groovy.json.*

import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient

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
public class DoCreateE2EServiceInstanceRollback extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoCreateE2EServiceInstanceRollback.class);
	String Prefix="DCRESIRB_"

	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		logger.trace("Start preProcessRequest")
		execution.setVariable("rollbackAAI",false)
		execution.setVariable("rollbackSDNC",false)

		try {
			def rollbackData = execution.getVariable("rollbackData")
			logger.debug("RollbackData:" + rollbackData)

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
					logger.debug("sdncDeactivate:\n" + sdncDeactivate)
					logger.debug("sdncDelete:\n" + sdncDelete)
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
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg) //TODO how does this even compile without exceptionUtil being declared
		}
		logger.trace("Exit preProcessRequest ")
	}

	public void validateSDNCResponse(DelegateExecution execution, String response, String method) {

		logger.trace("validateSDNCResponse")
		String msg = ""
		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			logger.debug("SDNCResponse: " + response)
			logger.debug("workflowException: " + workflowException)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				msg = "SDNC Adapter service-instance rollback successful for " + method
				logger.debug(msg)
			}else{
				execution.setVariable("rolledBack", false)
				msg =  "Error Response from SDNC Adapter service-instance rollback for " + method
				execution.setVariable("rollbackError", msg)
				logger.debug(msg)
				throw new BpmnError("MSOWorkflowException")
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in Create ServiceInstance rollback for "  + method  + " Exception:" + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit validateSDNCResponse ")
	}

	/**
	 * Deletes the service instance in aai
	 */
	public void deleteServiceInstance(DelegateExecution execution) {
		logger.trace("Started Delete Service Instance")
		try {
			String globalCustId = execution.getVariable("globalSubscriberId")
			String serviceType = execution.getVariable("subscriptionServiceType")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			AAIResourcesClient resourceClient = new AAIResourcesClient();
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustId, serviceType, serviceInstanceId)
			resourceClient.delete(serviceInstanceUri)

			logger.trace("Completed Delete Service Instance")
		}catch(Exception e){
			logger.debug("Error occured within deleteServiceInstance method: " + e)
		}
	}

	public void postProcessRequest(DelegateExecution execution) {

		logger.trace("postProcessRequest")
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
					logger.debug("Error deleting service-instance in AAI for rollback", + serviceInstanceId)
				}
			}
			logger.trace("Exit postProcessRequest ")

		} catch (BpmnError e) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + e.getMessage()
			logger.debug(msg)
		} catch (Exception ex) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + ex.getMessage()
			logger.debug(msg)
		}

	}

	public void processRollbackException(DelegateExecution execution){

		logger.trace("processRollbackException")
		try{
			logger.debug("Caught an Exception in DoCreateServiceInstanceRollback")
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught exception in ServiceInstance Create Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			logger.debug("BPMN Error during processRollbackExceptions Method: ")
		}catch(Exception e){
			logger.debug("Caught Exception during processRollbackExceptions Method: " + e.getMessage())
		}

		logger.trace(" Exit processRollbackException")
	}

	public void processRollbackJavaException(DelegateExecution execution){

		logger.trace("processRollbackJavaException")
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught Java exception in ServiceInstance Create Rollback")
			logger.debug("Caught Exception in processRollbackJavaException")

		}catch(Exception e){
			logger.debug("Caught Exception in processRollbackJavaException " + e.getMessage())
		}
		logger.trace("Exit processRollbackJavaException")
	}

}
