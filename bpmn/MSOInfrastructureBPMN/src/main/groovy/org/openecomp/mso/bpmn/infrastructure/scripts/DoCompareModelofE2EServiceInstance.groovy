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
package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.CompareModelsResult
import org.openecomp.mso.bpmn.core.domain.ResourceModelInfo
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig


import java.util.List
import java.util.Map
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
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
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String msg = ""
		utils.log("INFO"," ***** Enter DoCompareModelofE2EServiceInstance preProcessRequest *****",  isDebugEnabled)

		execution.setVariable("prefix", Prefix)
		//Inputs
		
		//subscriberInfo. for AAI GET
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		utils.log("INFO"," ***** globalSubscriberId *****" + globalSubscriberId,  isDebugEnabled)
		
		String serviceType = execution.getVariable("serviceType")
		utils.log("INFO"," ***** serviceType *****" + serviceType,  isDebugEnabled)

		if (isBlank(globalSubscriberId)) {
			msg = "Input globalSubscriberId is null"
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		if (isBlank(serviceType)) {
			msg = "Input serviceType is null"
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String serviceInstanceId = execution.getVariable("serviceInstanceId")
		if (isBlank(serviceInstanceId)){
			msg = "Input serviceInstanceId is null"
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String modelInvariantUuid = execution.getVariable("modelInvariantIdTarget")
		if (isBlank(modelInvariantUuid)){
			msg = "Input modelInvariantUuid is null"
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		String modelUuid = execution.getVariable("modelVersionIdTarget")
		if (isBlank(modelUuid)){
			msg = "Input modelUuid is null"
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		// Set Target Template info
		execution.setVariable("model-invariant-id-target", modelInvariantUuid)
		execution.setVariable("model-version-id-target", modelUuid)


		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	public void postProcessAAIGET(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIGET ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
			String serviceType = ""

			if(foundInAAI){
				utils.log("INFO","Found Service-instance in AAI", isDebugEnabled)

				String siData = execution.getVariable("GENGS_service")
				utils.log("INFO", "SI Data", isDebugEnabled)
				if (isBlank(siData))
				{
					msg = "Could not retrive ServiceInstance data from AAI, Id:" + serviceInstanceId
					utils.log("INFO", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
				else
				{
					utils.log("INFO", "SI Data" + siData, isDebugEnabled)
					
					// Get Template uuid and version
					if (utils.nodeExists(siData, "model-invariant-id") && utils.nodeExists(siData, "model-version-id") ) {
					    utils.log("INFO", "SI Data model-invariant-id and model-version-id exist", isDebugEnabled)
						
					    def modelInvariantId  = utils.getNodeText1(siData, "model-invariant-id")
					    def modelVersionId  = utils.getNodeText1(siData, "model-version-id")
					    
					    // Set Original Template info
					    execution.setVariable("model-invariant-id-original", modelInvariantId)
					    execution.setVariable("model-version-id-original", modelVersionId)					
					}					
				}
			}else{
				boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
				if(!succInAAI){
					utils.log("INFO","Error getting Service-instance from AAI", + serviceInstanceId, isDebugEnabled)
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

				utils.log("INFO","Service-instance NOT found in AAI. Silent Success", isDebugEnabled)
			}
		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.postProcessAAIGET. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIGET *** ", isDebugEnabled)
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
			
			Map<String, Object> resourceParameters = ResourceRequestBuilder.buildResouceRequest(serviceModelUuid, resourceCustomizationUuid, null)
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
	
