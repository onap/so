/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import jakarta.ws.rs.NotFoundException

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.CompareModelsResult
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ResourceModelInfo
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.*

/**
 * This groovy class supports the <class>DoCompareModelofE2EServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId
 * @param - subscriptionServiceType
 * @param - serviceInstanceId
 * @param - modelInvariantIdTarget
 * @param - modelVersionIdTarget
 *
 * Outputs:
 * @param - compareModelsResult CompareModelsResult

 */
public class DoCompareModelofE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DCMPMDSI_"
	private static final String DebugFlag = "isDebugEnabled"
	private static final Logger logger = LoggerFactory.getLogger( DeleteNetworkInstance.class);

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		logger.info("Entered " + method)
		String msg = ""
		logger.info(" ***** Enter DoCompareModelofE2EServiceInstance preProcessRequest *****")

		execution.setVariable("prefix", Prefix)
		//Inputs

		//subscriberInfo. for AAI GET
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		logger.info(" ***** globalSubscriberId *****" + globalSubscriberId)

		String serviceType = execution.getVariable("serviceType")
		logger.info(" ***** serviceType *****" + serviceType)

		if (isBlank(globalSubscriberId)) {
			msg = "Input globalSubscriberId is null"
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		if (isBlank(serviceType)) {
			msg = "Input serviceType is null"
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		if (isBlank(serviceInstanceId)){
			msg = "Input serviceInstanceId is null"
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String modelInvariantUuid = execution.getVariable("modelInvariantIdTarget")
		if (isBlank(modelInvariantUuid)){
			msg = "Input modelInvariantUuid is null"
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String modelUuid = execution.getVariable("modelVersionIdTarget")
		if (isBlank(modelUuid)){
			msg = "Input modelUuid is null"
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		// Set Target Template info
		execution.setVariable("model-invariant-id-target", modelInvariantUuid)
		execution.setVariable("model-version-id-target", modelUuid)


		logger.info( "Exited " + method)
	}

	/**
	 * Gets the service instance from aai
	 *
	 * @author cb645j
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')
			String globalSubscriberId = execution.getVariable('globalSubscriberId')
			String serviceType = execution.getVariable('serviceType')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
			AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)

			Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
			execution.setVariable("model-invariant-id-original", si.get().getModelInvariantId())
			execution.setVariable("model-version-id-original", si.get().getModelVersionId())

		}catch(BpmnError e) {
			throw e;
		}catch(NotFoundException e) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 404, "Service-instance does not exist AAI")
		}catch(Exception ex) {
			String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	public void postCompareModelVersions(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")


		List<Resource> addResourceList = execution.getVariable("addResourceList")
		List<Resource> delResourceList = execution.getVariable("delResourceList")

		CompareModelsResult cmpResult = new CompareModelsResult()
		List<ResourceModelInfo> addedResourceList = new ArrayList<ResourceModelInfo>()
		List<ResourceModelInfo> deletedResourceList = new ArrayList<ResourceModelInfo>()


		String serviceModelUuid = execution.getVariable("model-version-id-target")
        List<String> requestInputs = new ArrayList<String>()
		ModelInfo mi = null;
		for(Resource rc : addResourceList) {
			mi = rc.getModelInfo()
			String resourceCustomizationUuid = mi.getModelCustomizationUuid()
			ResourceModelInfo rmodel = new ResourceModelInfo()
			rmodel.setResourceName(mi.getModelName())
			rmodel.setResourceInvariantUuid(mi.getModelInvariantUuid())
			rmodel.setResourceUuid(mi.getModelUuid())
			rmodel.setResourceCustomizationUuid(resourceCustomizationUuid)
			addedResourceList.add(rmodel)

			Map<String, Object> resourceParameters = ResourceRequestBuilder.buildResouceRequest(rc, null, null)
			requestInputs.addAll(resourceParameters.keySet())
		}

		for(Resource rc : delResourceList) {
			mi = rc.getModelInfo()
			String resourceCustomizationUuid = mi.getModelCustomizationUuid()
			ResourceModelInfo rmodel = new ResourceModelInfo()
			rmodel.setResourceName(mi.getModelName())
			rmodel.setResourceInvariantUuid(mi.getModelInvariantUuid())
			rmodel.setResourceUuid(mi.getModelUuid())
			rmodel.setResourceCustomizationUuid(resourceCustomizationUuid)
			deletedResourceList.add(rmodel)
		}

		cmpResult.setAddedResourceList(addedResourceList)
		cmpResult.setDeletedResourceList(deletedResourceList)
		cmpResult.setRequestInputs(requestInputs)

		execution.setVariable("compareModelsResult", cmpResult)
	}

}

