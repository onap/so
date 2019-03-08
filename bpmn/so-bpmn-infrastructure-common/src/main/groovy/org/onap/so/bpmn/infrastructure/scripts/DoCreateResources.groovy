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

package org.onap.so.bpmn.infrastructure.scripts

import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.infrastructure.properties.BPMNProperties
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.onap.so.bpmn.common.recipe.BpmnRestClient
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.NetworkResource
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.logger.MsoLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory



/**
 * This groovy class supports the <class>DoCreateResources.bpmn</class> process.
 * 
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId - O
 * @param - subscriptionServiceType - O
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion 
 *
 * @param - addResourceList
 *
 * Outputs:
 * @param - WorkflowException
 */
public class DoCreateResources extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( DoCreateResources.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

    public void preProcessRequest(DelegateExecution execution) {
        logger.trace("preProcessRequest ")
        String msg = ""

        List addResourceList = execution.getVariable("addResourceList")
        if (addResourceList == null) {
            msg = "Input addResourceList is null"
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        else if (addResourceList.size() == 0) {
            msg = "No resource in addResourceList"
            logger.info(msg)
        }
        logger.trace("Exit preProcessRequest ")
    }

    public void sequenceResoure(DelegateExecution execution) {
        logger.trace("Start sequenceResoure Process ")
        
        String incomingRequest = execution.getVariable("uuiRequest")
        String serviceModelUuid = jsonUtil.getJsonValue(incomingRequest,"service.serviceUuid")

        List<Resource> addResourceList = execution.getVariable("addResourceList")

        List<NetworkResource> networkResourceList = new ArrayList<NetworkResource>()

        List<Resource> sequencedResourceList = new ArrayList<Resource>()

        String serviceDecompose = execution.getVariable("serviceDecomposition")
        String serviceModelName = jsonUtil.getJsonValue(serviceDecompose, "serviceResources.modelInfo.modelName")

        // get Sequence from properties        
        def resourceSequence = BPMNProperties.getResourceSequenceProp(serviceModelName)

        // get Sequence from csar(model)  
        if(resourceSequence == null) {
            resourceSequence = ResourceRequestBuilder.getResourceSequence(serviceModelUuid)
            logger.info("Get Sequence from csar : " + resourceSequence)
        }

        if(resourceSequence != null) {
            for (resourceType in resourceSequence) {
                for (resource in addResourceList) {
                    if (StringUtils.containsIgnoreCase(resource.getModelInfo().getModelName(), resourceType)) {
                        sequencedResourceList.add(resource)

                        if (resource instanceof NetworkResource) {
                            networkResourceList.add(resource)
                        }
                    }
                }
            }
        } else {

            //define sequenced resource list, we deploy vf first and then network and then ar
            //this is defaule sequence
            List<VnfResource> vnfResourceList = new ArrayList<VnfResource>()
            List<AllottedResource> arResourceList = new ArrayList<AllottedResource>()

            for (Resource rc : addResourceList){
                if (rc instanceof VnfResource) {
                    vnfResourceList.add(rc)
                } else if (rc instanceof NetworkResource) {
                    networkResourceList.add(rc)
                } else if (rc instanceof AllottedResource) {
                    arResourceList.add(rc)
                }
            }
            sequencedResourceList.addAll(vnfResourceList)
            sequencedResourceList.addAll(networkResourceList)
            sequencedResourceList.addAll(arResourceList)
        }

        String isContainsWanResource = networkResourceList.isEmpty() ? "false" : "true"
        //if no networkResource, get SDNC config from properties file
        if( "false".equals(isContainsWanResource)) {
            String serviceNeedSDNC = "mso.workflow.custom." + serviceModelName + ".sdnc.need";
            isContainsWanResource = BPMNProperties.getProperty(serviceNeedSDNC, isContainsWanResource)
        }

        execution.setVariable("isContainsWanResource", isContainsWanResource)
        execution.setVariable("currentResourceIndex", 0)
        execution.setVariable("sequencedResourceList", sequencedResourceList)
        logger.info("sequencedResourceList: " + sequencedResourceList)
        logger.trace("COMPLETED sequenceResoure Process ")
    }

    public prepareServiceTopologyRequest(DelegateExecution execution) {

        logger.trace("======== Start prepareServiceTopologyRequest Process ======== ")

        String serviceDecompose = execution.getVariable("serviceDecomposition")

        execution.setVariable("operationType", "create")
        execution.setVariable("resourceType", "")

        String serviceInvariantUuid = jsonUtil.getJsonValue(serviceDecompose, "serviceResources.modelInfo.modelInvariantUuid")
        String serviceUuid = jsonUtil.getJsonValue(serviceDecompose, "serviceResources.modelInfo.modelUuid")
        String serviceModelName = jsonUtil.getJsonValue(serviceDecompose, "serviceResources.modelInfo.modelName")

        execution.setVariable("modelInvariantUuid", serviceInvariantUuid)
        execution.setVariable("modelUuid", serviceUuid)
        execution.setVariable("serviceModelName", serviceModelName)

        logger.trace("======== End prepareServiceTopologyRequest Process ======== ")
    }

    public void getCurrentResoure(DelegateExecution execution){
        logger.trace("Start getCurrentResoure Process ")
        def currentIndex = execution.getVariable("currentResourceIndex")
        List<Resource> sequencedResourceList = execution.getVariable("sequencedResourceList")
        Resource currentResource = sequencedResourceList.get(currentIndex)
        execution.setVariable("resourceType", currentResource.getModelInfo().getModelName())
        logger.info("Now we deal with resouce:" + currentResource.getModelInfo().getModelName())
        logger.trace("COMPLETED getCurrentResoure Process ")
    }

    public void parseNextResource(DelegateExecution execution){
        logger.trace("Start parseNextResource Process ")
        def currentIndex = execution.getVariable("currentResourceIndex")
        def nextIndex =  currentIndex + 1
        execution.setVariable("currentResourceIndex", nextIndex)
        List<String> sequencedResourceList = execution.getVariable("sequencedResourceList")
        if(nextIndex >= sequencedResourceList.size()){
            execution.setVariable("allResourceFinished", "true")
        }else{
            execution.setVariable("allResourceFinished", "false")
        }
        logger.trace("COMPLETED parseNextResource Process ")
    }

    public void prepareResourceRecipeRequest(DelegateExecution execution){
        logger.trace("Start prepareResourceRecipeRequest Process ")
        ResourceInput resourceInput = new ResourceInput()
        String serviceInstanceName = execution.getVariable("serviceInstanceName")
        String resourceType = execution.getVariable("resourceType")
        String resourceInstanceName = resourceType + "_" + serviceInstanceName
        resourceInput.setResourceInstanceName(resourceInstanceName)
        logger.info("Prepare Resource Request resourceInstanceName:" + resourceInstanceName)
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String operationId = execution.getVariable("operationId")
        String operationType = "createInstance"
        resourceInput.setGlobalSubscriberId(globalSubscriberId)
        resourceInput.setServiceType(serviceType)
        resourceInput.setServiceInstanceId(serviceInstanceId)
        resourceInput.setOperationId(operationId)
        resourceInput.setOperationType(operationType);
        def currentIndex = execution.getVariable("currentResourceIndex")
        List<Resource> sequencedResourceList = execution.getVariable("sequencedResourceList")
        Resource currentResource = sequencedResourceList.get(currentIndex)
        resourceInput.setResourceModelInfo(currentResource.getModelInfo());
        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        resourceInput.setServiceModelInfo(serviceDecomposition.getModelInfo());
        def String resourceCustomizationUuid = currentResource.getModelInfo().getModelCustomizationUuid();

        String incomingRequest = execution.getVariable("uuiRequest")
        //set the requestInputs from tempalte  To Be Done
        String serviceModelUuid = jsonUtil.getJsonValue(incomingRequest,"service.serviceUuid")
        String serviceParameters = jsonUtil.getJsonValue(incomingRequest, "service.parameters")
        String resourceParameters = ResourceRequestBuilder.buildResourceRequestParameters(execution, serviceModelUuid, resourceCustomizationUuid, serviceParameters)
        resourceInput.setResourceParameters(resourceParameters)
        resourceInput.setRequestsInputs(incomingRequest)
        execution.setVariable("resourceInput", resourceInput.toString())
        execution.setVariable("resourceModelUUID", resourceInput.getResourceModelInfo().getModelUuid())
        logger.trace("COMPLETED prepareResourceRecipeRequest Process ")
    }

    public void executeResourceRecipe(DelegateExecution execution){
        logger.trace("Start executeResourceRecipe Process ")

        try {
            String requestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String serviceType = execution.getVariable("serviceType")
            String resourceInput = execution.getVariable("resourceInput")
            String resourceModelUUID = execution.getVariable("resourceModelUUID")

            // requestAction is action, not opertiontype
            //String requestAction = resourceInput.getOperationType()
            String requestAction = "createInstance"
            JSONObject resourceRecipe = catalogDbUtils.getResourceRecipe(execution, resourceModelUUID, requestAction)

            if (resourceRecipe != null) {
                String recipeURL = BPMNProperties.getProperty("bpelURL", "http://so-bpmn-infra.onap:8081") + resourceRecipe.getString("orchestrationUri")
                int recipeTimeOut = resourceRecipe.getInt("recipeTimeout")
                String recipeParamXsd = resourceRecipe.get("paramXSD")

                BpmnRestClient bpmnRestClient = new BpmnRestClient()
                HttpResponse resp = bpmnRestClient.post(recipeURL, requestId, recipeTimeOut, requestAction, serviceInstanceId, serviceType, resourceInput, recipeParamXsd)
            } else {
                String exceptionMessage = "Resource receipe is not found for resource modeluuid: " + resourceModelUUID
                logger.trace(exceptionMessage)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, exceptionMessage)
            }

            logger.trace("======== end executeResourceRecipe Process ======== ")
        }catch(BpmnError b){
            logger.debug("Rethrowing MSOWorkflowException")
            throw b
        }catch(Exception e){
            logger.debug("Error occured within DoCreateResources executeResourceRecipe method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured during DoCreateResources executeResourceRecipe Catalog")
        }
    }

    public void postConfigRequest(DelegateExecution execution){
        //now do noting
    }
}
