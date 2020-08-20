/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, Wipro Limited.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.apache.commons.lang3.StringUtils.isBlank

class AllocateSliceSubnet extends AbstractServiceTaskProcessor {

	String Prefix="ASS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	RequestDBUtil requestDBUtil = new RequestDBUtil()
	JsonUtils jsonUtil = new JsonUtils()
	private static final Logger logger = LoggerFactory.getLogger(AllocateSliceSubnet.class)

	@Override
	void preProcessRequest(DelegateExecution execution) {
		logger.debug(Prefix + "preProcessRequest Start")
		execution.setVariable("prefix", Prefix)
		execution.setVariable("startTime", System.currentTimeMillis())
		def msg
		try {
			// get request input
			String subnetInstanceReq = execution.getVariable("bpmnRequest")
			logger.debug(subnetInstanceReq)

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			logger.debug("Input Request:" + subnetInstanceReq + " reqId:" + requestId)

			//modelInfo
			String modelInvariantUuid = jsonUtil.getJsonValue(subnetInstanceReq, "modelInvariantUuid")
			if (isBlank(modelInvariantUuid)) {
				msg = "Input modelInvariantUuid is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			}

			logger.debug("modelInvariantUuid: " + modelInvariantUuid)

			String modelUuid = jsonUtil.getJsonValue(subnetInstanceReq, "modelUuid")
			if (isBlank(modelUuid)) {
				msg = "Input modelUuid is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("modelUuid", modelUuid)
			}

			logger.debug("modelUuid: " + modelUuid)


			//subscriberInfo
			String globalSubscriberId = jsonUtil.getJsonValue(subnetInstanceReq, "globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}
			String dummyServiceId = new UUID(0,0).toString();
			execution.setVariable("dummyServiceId", dummyServiceId)
			logger.debug("dummyServiceId: " + dummyServiceId)
			String servicename = jsonUtil.getJsonValue(subnetInstanceReq, "name")
			execution.setVariable("servicename", servicename)

			String nsiId = jsonUtil.getJsonValue(subnetInstanceReq, "additionalProperties.nsiInfo.nsiId")
			if (isBlank(nsiId)) {
				msg = "Input nsiId is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("nsiId", nsiId)
			}

			String networkType = jsonUtil.getJsonValue(subnetInstanceReq, "networkType")
			if (isBlank(networkType)) {
				msg = "Input networkType is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("networkType", networkType.toUpperCase())
			}

			//requestParameters, subscriptionServiceType is 5G
			String subscriptionServiceType = jsonUtil.getJsonValue(subnetInstanceReq, "subscriptionServiceType")
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			}

			String jobId = UUID.randomUUID().toString()
			execution.setVariable("jobId", jobId)

			String sliceParams = jsonUtil.getJsonValue(subnetInstanceReq, "additionalProperties")
			execution.setVariable("sliceParams", sliceParams)

		} catch(BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in AllocateSliceSubnet.preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(Prefix + "preProcessRequest Exit")
	}


	/**
	 * create operation status in request db
	 *
	 * Init the Operation Status
	 */
	def prepareInitOperationStatus = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareInitOperationStatus Start")

		String serviceId = execution.getVariable("dummyServiceId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		logger.debug("Generated new job for Service Instance serviceId:" + serviceId + " jobId:" + jobId)

		ResourceOperationStatus initStatus = new ResourceOperationStatus()
		initStatus.setServiceId(serviceId)
		initStatus.setOperationId(jobId)
		initStatus.setResourceTemplateUUID(nsiId)
		initStatus.setOperType("Allocate")
		requestDBUtil.prepareInitResourceOperationStatus(execution, initStatus)

		logger.debug(Prefix + "prepareInitOperationStatus Exit")
	}


	/**
	 * return sync response
	 */
	def sendSyncResponse = { DelegateExecution execution ->
		logger.debug(Prefix + "sendSyncResponse Start")
		try {
			String jobId = execution.getVariable("jobId")
			String allocateSyncResponse = """{"jobId": "${jobId}","status": "processing"}"""
												.trim().replaceAll(" ", "").trim().replaceAll(" ", "")

			logger.debug("sendSyncResponse to APIH:" + "\n" + allocateSyncResponse)
			sendWorkflowResponse(execution, 202, allocateSyncResponse)

			execution.setVariable("sentSyncResponse", true)
		} catch (Exception ex) {
			String msg = "Exception in sendSyncResponse:" + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(Prefix + "sendSyncResponse Exit")
	}
	
	//Method to be removed after NSSMF code integration
	void dummypreProcessRequest(DelegateExecution execution) {
		String type = execution.getVariable("networkType")
		logger.debug( type +" dummypreProcessRequest")
	}
}
