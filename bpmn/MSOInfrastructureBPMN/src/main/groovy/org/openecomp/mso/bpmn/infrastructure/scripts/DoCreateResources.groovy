/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved. 
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
 
package org.openecomp.mso.bpmn.infrastructure.scripts

import org.openecomp.mso.bpmn.infrastructure.properties.BPMNProperties

import java.util.ArrayList
import java.util.Iterator
import java.util.List
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.codehaus.groovy.runtime.ArrayUtil
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter
import org.codehaus.groovy.runtime.callsite.CallSite
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation
import org.codehaus.groovy.runtime.typehandling.ShortTypeHandling
import org.json.JSONArray
import org.json.JSONObject
import org.openecomp.mso.bpmn.common.recipe.BpmnRestClient
import org.openecomp.mso.bpmn.common.recipe.ResourceInput
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.domain.AllottedResource
import org.openecomp.mso.bpmn.core.domain.NetworkResource
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder

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
public class DoCreateResources extends AbstractServiceTaskProcessor
{
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils cutils = new CatalogDbUtils()

    public void preProcessRequest(DelegateExecution execution)
    {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** preProcessRequest *****",    isDebugEnabled)
		String msg = ""
		
        List addResourceList = execution.getVariable("addResourceList")
        if (addResourceList == null)
        {
            msg = "Input addResourceList is null"
            utils.log("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)  
        }
        else if (addResourceList.size() == 0)
        {
            msg = "No resource in addResourceList"
            utils.log("INFO", msg, isDebugEnabled)
        }
        utils.log("INFO", " ***** Exit preProcessRequest *****", isDebugEnabled)
    }
    
    public void sequenceResoure(DelegateExecution execution)
    {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start sequenceResoure Process ======== ", isDebugEnabled)
        
        String serviceModelUUID = execution.getVariable("modelUuid")      
               
        List<Resource> addResourceList = execution.getVariable("addResourceList")        

        List<NetworkResource> networkResourceList = new ArrayList<NetworkResource>()

        //define sequenced resource list, we deploy vf first and then network and then ar 
        //this is defaule sequence
        List<Resource> sequencedResourceList = new ArrayList<Resource>()
        def resourceSequence = BPMNProperties.getResourceSequenceProp()

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

        String isContainsWanResource = networkResourceList.isEmpty() ? "false" : "true"
        execution.setVariable("isContainsWanResource", isContainsWanResource)
        execution.setVariable("currentResourceIndex", 0)
        execution.setVariable("sequencedResourceList", sequencedResourceList)
        utils.log("INFO", "sequencedResourceList: " + sequencedResourceList, isDebugEnabled) 
        utils.log("INFO", "======== COMPLETED sequenceResoure Process ======== ", isDebugEnabled)
    }   
   
    public void getCurrentResoure(DelegateExecution execution){
	    def isDebugEnabled=execution.getVariable("isDebugLogEnabled")   
        utils.log("INFO", "======== Start getCurrentResoure Process ======== ", isDebugEnabled)    
	    def currentIndex = execution.getVariable("currentResourceIndex")
	    List<Resource> sequencedResourceList = execution.getVariable("sequencedResourceList")  
	    Resource currentResource = sequencedResourceList.get(currentIndex)
        execution.setVariable("resourceType", currentResource.getModelInfo().getModelName())
	    utils.log("INFO", "Now we deal with resouce:" + currentResource.getModelInfo().getModelName(), isDebugEnabled)  
        utils.log("INFO", "======== COMPLETED getCurrentResoure Process ======== ", isDebugEnabled)  
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

	 public void prepareResourceRecipeRequest(DelegateExecution execution){
		 def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		 utils.log("INFO", "======== Start prepareResourceRecipeRequest Process ======== ", isDebugEnabled)
		 ResourceInput resourceInput = new ResourceInput()
		 String serviceInstanceName = execution.getVariable("serviceInstanceName")
         String resourceType = execution.getVariable("resourceType")
		 String resourceInstanceName = resourceType + "_" + serviceInstanceName
		 resourceInput.setResourceInstanceName(resourceInstanceName)
		 utils.log("INFO", "Prepare Resource Request resourceInstanceName:" + resourceInstanceName, isDebugEnabled)
		 String globalSubscriberId = execution.getVariable("globalSubscriberId")
		 String serviceType = execution.getVariable("serviceType")
		 String serviceInstanceId = execution.getVariable("serviceInstanceId")
		 String operationId = execution.getVariable("operationId")
		 String operationType = execution.getVariable("operationType")
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
		 execution.setVariable("resourceInput", resourceInput)
		 utils.log("INFO", "======== COMPLETED prepareResourceRecipeRequest Process ======== ", isDebugEnabled)
	 }
	 
	 public void executeResourceRecipe(DelegateExecution execution){
		 def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		 utils.log("INFO", "======== Start executeResourceRecipe Process ======== ", isDebugEnabled)
		 String requestId = execution.getVariable("msoRequestId")
		 String serviceInstanceId = execution.getVariable("serviceInstanceId")
		 String serviceType = execution.getVariable("serviceType")
		 ResourceInput resourceInput = execution.getVariable("resourceInput")
		 String requestAction = resourceInput.getOperationType()
		 JSONObject resourceRecipe = cutils.getResourceRecipe(execution, resourceInput.getResourceModelInfo().getModelUuid(), requestAction)
         String recipeURL = BPMNProperties.getProperty("bpelURL", "http://mso:8080") + resourceRecipe.getString("orchestrationUri")
		 int recipeTimeOut = resourceRecipe.getInt("recipeTimeout")
		 String recipeParamXsd = resourceRecipe.get("paramXSD")
		 HttpResponse resp = BpmnRestClient.post(recipeURL, requestId, recipeTimeOut, requestAction, serviceInstanceId, serviceType, resourceInput.toString(), recipeParamXsd)
         utils.log("INFO", "======== end executeResourceRecipe Process ======== ", isDebugEnabled)
	 }
    
     public void postConfigRequest(DelegateExecution execution){
         //now do noting
     }
}
