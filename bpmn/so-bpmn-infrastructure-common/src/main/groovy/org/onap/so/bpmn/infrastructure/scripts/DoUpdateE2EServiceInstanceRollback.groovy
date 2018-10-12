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
package org.onap.so.bpmn.infrastructure.scripts


import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MsoLogger
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import groovy.json.*
import org.onap.so.bpmn.common.scripts.AaiUtil;

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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoUpdateE2EServiceInstanceRollback.class);


	String Prefix="DUPDSIRB_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		String msg = ""
		msoLogger.trace("preProcessRequest ")
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
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit preProcessRequest ")
	}

	public void postProcessRequest(DelegateExecution execution) {
		msoLogger.trace("postProcessRequest ")
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
					msoLogger.debug("Error deleting service-instance in AAI for rollback", + serviceInstanceId)
				}
			}
			msoLogger.trace("Exit postProcessRequest ")

		} catch (BpmnError e) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + e.getMessage()
			msoLogger.debug(msg)
		} catch (Exception ex) {
			msg = "Exception in Create ServiceInstance Rollback postProcessRequest. " + ex.getMessage()
			msoLogger.debug(msg)
		}
	}


    public void preProcessForAddResource(DelegateExecution execution) {
    }

    public void postProcessForAddResource(DelegateExecution execution) {
    }

    public void preProcessForDeleteResource(DelegateExecution execution) {
    }

    public void postProcessForDeleteResource(DelegateExecution execution) {
    }

	public void preProcessAAIPUT(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		msoLogger.info("Entered " + method)
		String msg = ""
		msoLogger.trace("preProcessAAIPUT ")

		String serviceInstanceVersion = execution.getVariable("serviceInstanceVersion_n")
//		execution.setVariable("GENPS_serviceResourceVersion", serviceInstanceVersion)

		//requestDetails.modelInfo.for AAI PUT servieInstanceData
		//requestDetails.requestInfo. for AAI GET/PUT serviceInstanceData
		String serviceInstanceName = execution.getVariable("serviceInstanceName")
		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		//aai serviceType and Role can be setted as fixed value now.
		String aaiServiceType = "E2E Service"
		String aaiServiceRole = "E2E Service"
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelUuid = execution.getVariable("model-version-id-original")

		org.onap.aai.domain.yang.ServiceInstance si = new org.onap.aai.domain.yang.ServiceInstance()
		si.setServiceInstanceId(serviceInstanceId)
		si.setServiceInstanceName(serviceInstanceName)
		si.setServiceType(aaiServiceType)
		si.setServiceRole(aaiServiceRole)
		si.setModelInvariantId(modelInvariantUuid)
		si.setModelVersionId(modelUuid)

		execution.setVariable("serviceInstanceData", si)

		msoLogger.info("Exited " + method)
	}

	public void updateServiceInstance(DelegateExecution execution) {
		msoLogger.trace("updateServiceInstance ")
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			org.onap.aai.domain.yang.ServiceInstance si = execution.getVariable("serviceInstanceData")

			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)
			client.update(uri, si)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.updateServiceInstance. " + ex.getMessage()
			msoLogger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		msoLogger.trace("Exit updateServiceInstance ")
	}

	public void processRollbackException(DelegateExecution execution){
		msoLogger.trace("processRollbackException ")
		try{
			msoLogger.debug("Caught an Exception in DoUpdateE2EServiceInstanceRollback")
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught exception in ServiceInstance Update Rollback")
			execution.setVariable("WorkflowException", null)

		}catch(BpmnError b){
			msoLogger.debug("BPMN Error during processRollbackExceptions Method: ")
		}catch(Exception e){
			msoLogger.debug("Caught Exception during processRollbackExceptions Method: " + e.getMessage())
		}

		msoLogger.debug(" Exit processRollbackException")
	}

	public void processRollbackJavaException(DelegateExecution execution){
		msoLogger.trace("processRollbackJavaException ")
		try{
			execution.setVariable("rollbackData", null)
			execution.setVariable("rollbackError", "Caught Java exception in ServiceInstance Update Rollback")
			msoLogger.debug("Caught Exception in processRollbackJavaException")

		}catch(Exception e){
			msoLogger.debug("Caught Exception in processRollbackJavaException " + e.getMessage())
		}
		msoLogger.trace("Exit processRollbackJavaException ")
	}

}
