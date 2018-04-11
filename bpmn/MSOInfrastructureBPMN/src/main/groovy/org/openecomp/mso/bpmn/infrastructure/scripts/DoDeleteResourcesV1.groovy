
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
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.infrastructure.properties.BPMNProperties

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isBlank

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

    public void sequenceResource(execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        utils.log("INFO", " ======== STARTED sequenceResource Process ======== ", isDebugEnabled)
        List<String> nsResources = new ArrayList<String>()
        List<String> wanResources = new ArrayList<String>()
        List<String> resourceSequence = new  ArrayList<String>()

        // get delete resource list and order list
        List<Resource> delResourceList = execution.getVariable("delResourceList")
        // existing resource list
        List<ServiceInstance> existResourceList = execution.getVariable("realNSRessources")

        for(ServiceInstance rc_e : existResourceList){

            String muuid = rc_e.getModelInfo().getModelUuid()
            String mIuuid = rc_e.getModelInfo().getModelInvariantUuid()
            String mCuuid = rc_e.getModelInfo().getModelCustomizationUuid()
            rcType = rc_e.getInstanceName()

            for(Resource rc_d : delResourceList){

                if(rc_d.getModelInfo().getModelUuid() == muuid
                        && rc_d.getModelInfo().getModelInvariantUuid() == mIuuid
                        && rc_d.getModelInfo().getModelCustomizationUuid() == mCuuid) {

                    if(StringUtils.containsIgnoreCase(rcType, "overlay")
                            || StringUtils.containsIgnoreCase(rcType, "underlay")){
                        wanResources.add(rcType)
                    }else{
                        nsResources.add(rcType)
                    }

                }
            }

        }

        resourceSequence.addAll(wanResources)
        resourceSequence.addAll(nsResources)
        String isContainsWanResource = wanResources.isEmpty() ? "false" : "true"
        execution.setVariable("isContainsWanResource", isContainsWanResource)
        execution.setVariable("currentResourceIndex", 0)
        execution.setVariable("resourceSequence", resourceSequence)
        utils.log("INFO", "resourceSequence: " + resourceSequence, isDebugEnabled)
        execution.setVariable("wanResources", wanResources)
        utils.log("INFO", " ======== END sequenceResource Process ======== ", isDebugEnabled)
    }

    /**
     * prepare delete parameters
     */
    public void preResourceDelete(execution, resourceName){

        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        utils.log("INFO", " ======== STARTED preResourceDelete Process ======== ", isDebugEnabled)

        List<ServiceInstance> existResourceList = execution.getVariable("realNSRessources")

        for(ServiceInstance rc_e : existResourceList){

            if(StringUtils.containsIgnoreCase(rc_e.getInstanceName(), resourceName)) {

                String resourceInstanceUUID = rc_e.getInstanceId()
                String resourceTemplateUUID = rc_e.getModelInfo().getModelUuid()
                execution.setVariable("resourceInstanceId", resourceInstanceUUID)
                execution.setVariable("resourceTemplateId", resourceTemplateUUID)
                execution.setVariable("resourceType", resourceName)
                utils.log("INFO", "Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: "
                        + resourceInstanceUUID + " resourceType: " + resourceName, isDebugEnabled)
            }
        }

        utils.log("INFO", " ======== END preResourceDelete Process ======== ", isDebugEnabled)
    }


    /**
     * Execute delete workflow for resource
     */
    public void executeResourceDelete(execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start executeResourceDelete Process ======== ", isDebugEnabled)
        String requestId = execution.getVariable("msoRequestId")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceType = execution.getVariable("serviceType")
        ResourceInput resourceInput = execution.getVariable("resourceInput")
        String requestAction = resourceInput.getOperationType()
        JSONObject resourceRecipe = cutils.getResourceRecipe(execution, resourceInput.getResourceUuid(), requestAction)
        String recipeUri = resourceRecipe.getString("orchestrationUri")
        String recipeTimeOut = resourceRecipe.getString("recipeTimeout")
        String recipeParamXsd = resourceRecipe.get("paramXSD")
        HttpResponse resp = BpmnRestClient.post(recipeUri, requestId, recipeTimeout, requestAction, serviceInstanceId, serviceType, resourceInput.toString(), recipeParamXsd)
        utils.log("INFO", " ======== END executeResourceDelete Process ======== ", isDebugEnabled)
    }


    public void parseNextResource(execution){
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start parseNextResource Process ======== ", isDebugEnabled)
        def currentIndex = execution.getVariable("currentResourceIndex")
        def nextIndex =  currentIndex + 1
        execution.setVariable("currentResourceIndex", nextIndex)
        List<String> resourceSequence = execution.getVariable("resourceSequence")
        if(nextIndex >= resourceSequence.size()){
            execution.setVariable("allResourceFinished", "true")
        }else{
            execution.setVariable("allResourceFinished", "false")
        }
        utils.log("INFO", "======== COMPLETED parseNextResource Process ======== ", isDebugEnabled)
    }
}
