
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

import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.openecomp.mso.bpmn.common.recipe.BpmnRestClient
import org.openecomp.mso.bpmn.common.recipe.ResourceInput
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.domain.AllottedResource
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.NetworkResource
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.infrastructure.properties.BPMNProperties

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * input for script :
 * msoRequestId
 * isDebugLogEnabled
 * globalSubscriberId
 * serviceType
 * serviceInstanceId
 * URN_mso_workflow_sdncadapter_callback
 * serviceInputParams
 * deleteResourceList
 * resourceInstanceIDs
 *
 * output from script:
 *
 */

public class DoDeleteResourcesV1 extends AbstractServiceTaskProcessor {

    String Prefix="DDR_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    CatalogDbUtils cutils = new CatalogDbUtils()

    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** preProcessRequest *****",  isDebugEnabled)
        String msg = ""

        try {
            String requestId = execution.getVariable("msoRequestId")
            execution.setVariable("prefix",Prefix)

            //Inputs
            //requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
            String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
            if (globalSubscriberId == null)
            {
                execution.setVariable("globalSubscriberId", "")
            }

            //requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
            String serviceType = execution.getVariable("serviceType")
            if (serviceType == null)
            {
                execution.setVariable("serviceType", "")
            }

            //Generated in parent for AAI PUT
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)){
                msg = "Input serviceInstanceId is null"
                utils.log("INFO", msg, isDebugEnabled)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
            if (isBlank(sdncCallbackUrl)) {
                msg = "URN_mso_workflow_sdncadapter_callback is null"
                utils.log("INFO", msg, isDebugEnabled)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
            utils.log("INFO","SDNC Callback URL: " + sdncCallbackUrl, isDebugEnabled)

            StringBuilder sbParams = new StringBuilder()
            Map<String, String> paramsMap = execution.getVariable("serviceInputParams")
            if (paramsMap != null)
            {
                sbParams.append("<service-input-parameters>")
                for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                    String paramsXml
                    String paramName = entry.getKey()
                    String paramValue = entry.getValue()
                    paramsXml =
                            """	<param>
							<name>${paramName}</name>
							<value>${paramValue}</value>
							</param>
							"""
                    sbParams.append(paramsXml)
                }
                sbParams.append("</service-input-parameters>")
            }
            String siParamsXml = sbParams.toString()
            if (siParamsXml == null)
                siParamsXml = ""
            execution.setVariable("siParamsXml", siParamsXml)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            utils.log("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
    }

    public void sequenceResource(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        utils.log("INFO", " ======== STARTED sequenceResource Process ======== ", isDebugEnabled)
        List<Resource> sequencedResourceList = new ArrayList<Resource>()
        List<Resource> wanResources = new ArrayList<Resource>()

        // get delete resource list and order list
        List<Resource> delResourceList = execution.getVariable("deleteResourceList")
        
        def resourceSequence = BPMNProperties.getResourceSequenceProp()

        if(resourceSequence != null) {
            for (resourceType in resourceSequence.reverse()) {
                for (resource in delResourceList) {
                    if (StringUtils.containsIgnoreCase(resource.getModelInfo().getModelName(), resourceType)) {
                        sequencedResourceList.add(resource)

                        if (resource instanceof NetworkResource) {
                            wanResources.add(resource)
                        }
                    }
                }
            }
        }else {
            //define sequenced resource list, we deploy vf first and then network and then ar
            //this is defaule sequence
            List<VnfResource> vnfResourceList = new ArrayList<VnfResource>()
            List<AllottedResource> arResourceList = new ArrayList<AllottedResource>()
            for (Resource rc : delResourceList) {
                if (rc instanceof VnfResource) {
                    vnfResourceList.add(rc)
                } else if (rc instanceof NetworkResource) {
                	wanResources.add(rc)
                } else if (rc instanceof AllottedResource) {
                    arResourceList.add(rc)
                }
            }

            sequencedResourceList.addAll(arResourceList)
            sequencedResourceList.addAll(wanResources)
            sequencedResourceList.addAll(vnfResourceList)
        }

        String isContainsWanResource = wanResources.isEmpty() ? "false" : "true"
        execution.setVariable("isContainsWanResource", isContainsWanResource)
        execution.setVariable("currentResourceIndex", 0)
        execution.setVariable("sequencedResourceList", sequencedResourceList)
        utils.log("INFO", "resourceSequence: " + resourceSequence, isDebugEnabled)
        utils.log("INFO", " ======== END sequenceResource Process ======== ", isDebugEnabled)
    }

    /**
     * prepare delete parameters
     */
    public void preResourceDelete(DelegateExecution execution){

        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        utils.log("INFO", " ======== STARTED preResourceDelete Process ======== ", isDebugEnabled)

        List<Resource> sequencedResourceList = execution.getVariable("sequencedResourceList")

        int currentIndex = execution.getVariable("currentResourceIndex")
        Resource curResource = sequencedResourceList.get(currentIndex);

        String resourceInstanceUUID = curResource.getResourceId()
        String resourceTemplateUUID = curResource.getModelInfo().getModelUuid()
        execution.setVariable("resourceInstanceId", resourceInstanceUUID)
        execution.setVariable("currentResource", curResource)
        utils.log("INFO", "Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: "
                + resourceInstanceUUID + " resourceModelName: " + curResource.getModelInfo().getModelName(), isDebugEnabled)

        utils.log("INFO", " ======== END preResourceDelete Process ======== ", isDebugEnabled)
    }


    /**
     * Execute delete workflow for resource
     */
    public void executeResourceDelete(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start executeResourceDelete Process ======== ", isDebugEnabled)
        String requestId = execution.getVariable("msoRequestId")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceType = execution.getVariable("serviceType")

        String resourceInstanceId = execution.getVariable("resourceInstanceId")

        Resource currentResource = execution.getVariable("currentResource")
        String action = "deleteInstance"
        JSONObject resourceRecipe = cutils.getResourceRecipe(execution, currentResource.getModelInfo().getModelUuid(), action)
        String recipeUri = resourceRecipe.getString("orchestrationUri")
        int recipeTimeout = resourceRecipe.getInt("recipeTimeout")
        String recipeParamXsd = resourceRecipe.get("paramXSD")


        ResourceInput resourceInput = new ResourceInput();
        resourceInput.setServiceInstanceId(serviceInstanceId)
        resourceInput.setResourceInstanceName(currentResource.getResourceInstanceName())
        resourceInput.setResourceInstancenUuid(currentResource.getResourceId())
        resourceInput.setOperationId(execution.getVariable("operationId"))
        String globalSubscriberId = execution.getVariable("globalSubscriberId") 
        resourceInput.setGlobalSubscriberId(globalSubscriberId)
        resourceInput.setResourceModelInfo(currentResource.getModelInfo());
   	    ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
   		resourceInput.setServiceModelInfo(serviceDecomposition.getModelInfo());
        resourceInput.setServiceType(serviceType)

        String recipeURL = BPMNProperties.getProperty("bpelURL", "http://mso:8080") + recipeUri

        HttpResponse resp = BpmnRestClient.post(recipeURL, requestId, recipeTimeout, action, serviceInstanceId, serviceType, resourceInput.toString(), recipeParamXsd)
        utils.log("INFO", " ======== END executeResourceDelete Process ======== ", isDebugEnabled)
    }


    public void parseNextResource(DelegateExecution execution){
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start parseNextResource Process ======== ", isDebugEnabled)
        def currentIndex = execution.getVariable("currentResourceIndex")
        def nextIndex =  currentIndex + 1
        execution.setVariable("currentResourceIndex", nextIndex)
        List<String> sequencedResourceList = execution.getVariable("sequencedResourceList")
        if(nextIndex >= sequencedResourceList.size()){
            execution.setVariable("allResourceFinished", "true")
        }else{
            execution.setVariable("allResourceFinished", "false")
        }
        utils.log("INFO", "======== COMPLETED parseNextResource Process ======== ", isDebugEnabled)
    }
    
    public void prepareFinishedProgressForResource(execution) {

        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceType = execution.getVariable("serviceType")
        String resourceInstanceId = execution.getVariable("resourceInstanceId")
        Resource currentResource = execution.getVariable("currentResource")
        String resourceCustomizationUuid = currentResource.getModelInfo().getModelCustomizationUuid()
        String resourceModelName = currentResource.getModelInfo().getModelName()
        String operationType = execution.getVariable("operationType")
        String progress = "100"
        String status = "finished"
        String statusDescription = "The resource instance does not exist for " + resourceModelName
        String operationId = execution.getVariable("operationId")

        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${operationType}</operType>
                               <operationId>${operationId}</operationId>
                               <progress>${progress}</progress>
                               <resourceTemplateUUID>${resourceCustomizationUuid}</resourceTemplateUUID>
                               <serviceId>${ServiceInstanceId}</serviceId>
                               <status>${status}</status>
                               <statusDescription>${statusDescription}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>""";

        def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
        execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
        execution.setVariable("CVFMI_updateResOperStatusRequest", body)
    }

    public void prepareServiceTopologyDeletion(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** prepareServiceTopologyDeletion "  + " *****", isDebugEnabled)

        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        execution.setVariable("modelInvariantUuid", serviceDecomposition.getModelInfo().getModelInvariantUuid())
        execution.setVariable("modelVersion", serviceDecomposition.getModelInfo().getModelVersion())
        execution.setVariable("modelUuid", serviceDecomposition.getModelInfo().getModelUuid())
        execution.setVariable("serviceModelName", serviceDecomposition.getModelInfo().getModelName())

        // set operation type and resource type is required to form request body
        execution.setVariable("operationType", "DELETE")
        execution.setVariable("resourceType", "SDNC-SERVICE-TOPOLOGY")

        utils.log("INFO"," ***** prepareServiceTopologyDeletion "  + " *****", isDebugEnabled)
    }
}
