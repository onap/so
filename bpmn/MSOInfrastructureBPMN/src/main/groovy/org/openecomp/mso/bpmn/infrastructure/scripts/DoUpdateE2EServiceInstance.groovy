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
import groovy.util.XmlParser

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

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


/**
 * This groovy class supports the <class>DoUpdateE2EServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId
 * @param - serviceType
 * @param - serviceInstanceId
 * @param - serviceInstanceName
 * @param - serviceModelInfo
 * @param - productFamilyId
 * @param - uuiRequest
 * @param - serviceDecomposition_Target
 * @param - serviceDecomposition_Original
 * @param - addResourceList
 * @param - delResourceList
 * 
 * Outputs:
 * @param - rollbackData (localRB->null)
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 * @param - WorkflowException
 */
public class DoUpdateE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DUPDSI_"
	private static final String DebugFlag = "isDebugEnabled"
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Enter DoUpdateE2EServiceInstance preProcessRequest *****",  isDebugEnabled)
		
		String msg = ""	

		try {
			execution.setVariable("prefix", Prefix)
			//Inputs
			//for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			utils.log("INFO"," ***** globalSubscriberId *****" + globalSubscriberId,  isDebugEnabled)
			
			//for AAI PUT & SDNC assignTopology
			String serviceType = execution.getVariable("serviceType")
			utils.log("INFO"," ***** serviceType *****" + serviceType,  isDebugEnabled)
			
			//for SDNC assignTopology
			String productFamilyId = execution.getVariable("productFamilyId") //AAI productFamilyId

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
			
			//Generated in parent for AAI 
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)){
				msg = "Input serviceInstanceId is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			
			// user params
			String uuiRequest = execution.getVariable("uuiRequest")
			// target model uuid
			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
			execution.setVariable("modelUuid", modelUuid)
			
			utils.log("INFO","modelUuid: " + modelUuid, isDebugEnabled)
				
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "======== COMPLETED preProcessRequest Process ======== ", isDebugEnabled)  
	}

	
	public void preInitResourcesOperStatus(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        utils.log("INFO", " ======== STARTED preInitResourcesOperStatus Process ======== ", isDebugEnabled)
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service updating"
            utils.log("INFO", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType, isDebugEnabled)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

			List<Resource> resourceList = new ArrayList<String>()
			List<Resource> addResourceList =  execution.getVariable("addResourceList")
			List<Resource> delResourceList =  execution.getVariable("delResourceList")
			resourceList.addAll(addResourceList)
			resourceList.addAll(delResourceList)
			for(Resource resource : resourceList){
				resourceTemplateUUIDs  = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
			}
			
			def dbAdapterEndpoint = "http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter"
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			utils.log("INFO", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

			String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initResourceOperationStatus xmlns:ns="http://org.openecomp.mso/requestsdb">
								<serviceId>${serviceId}</serviceId>
								<operationId>${operationId}</operationId>
								<operationType>${operationType}</operationType>
								<resourceTemplateUUIDs>${resourceTemplateUUIDs}</resourceTemplateUUIDs>
                            </ns:initResourceOperationStatus>
                    	</soapenv:Body>
                	</soapenv:Envelope>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_initResOperStatusRequest", payload)
			utils.log("INFO", "Outgoing initResourceOperationStatus: \n" + payload, isDebugEnabled)
			utils.logAudit("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing preInitResourcesOperStatus. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        utils.log("INFO", "======== COMPLETED preInitResourcesOperStatus Process ======== ", isDebugEnabled)  
    }
    

    public void preProcessForAddResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** preProcessForAddResource ***** ", isDebugEnabled)
		
	    execution.setVariable("operationType", "create")
		
		execution.setVariable("hasResourcetoAdd", false)
		List<Resource> addResourceList =  execution.getVariable("addResourceList")
		if(addResourceList != null && !addResourceList.isEmpty()) {
			execution.setVariable("hasResourcetoAdd", true)			
		}
	
		utils.log("INFO"," *** Exit preProcessForAddResource *** ", isDebugEnabled)
    }

    public void postProcessForAddResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessForAddResource ***** ", isDebugEnabled)
		
		execution.setVariable("operationType", "update")

		utils.log("INFO"," *** Exit postProcessForAddResource *** ", isDebugEnabled)
    }
    
    public void preProcessForDeleteResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** preProcessForDeleteResource ***** ", isDebugEnabled)
		
		execution.setVariable("operationType", "delete")
		
		execution.setVariable("hasResourcetoDelete", false)
		List<Resource> delResourceList =  execution.getVariable("delResourceList")
		if(delResourceList != null && !delResourceList.isEmpty()) {
			execution.setVariable("hasResourcetoDelete", true)
		}			
		
		execution.setVariable("resourceInstanceIDs", execution.getVariable("serviceRelationShip"))
		
		utils.log("INFO"," *** Exit preProcessForDeleteResource *** ", isDebugEnabled)

    }

    public void postProcessForDeleteResource(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessForDeleteResource ***** ", isDebugEnabled)
		
		execution.setVariable("operationType", "update")

		utils.log("INFO"," *** Exit postProcessForDeleteResource *** ", isDebugEnabled)
    } 
    
	public void preProcessAAIGET(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")	
	}
    	
	public void postProcessAAIGET(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIGET ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				utils.log("INFO","Error getting Service-instance from AAI in postProcessAAIGET", + serviceInstanceName, isDebugEnabled)
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
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					String aaiService = execution.getVariable("GENGS_service")
					if (!isBlank(aaiService) && (utils.nodeExists(aaiService, "resource-version"))) {
						execution.setVariable("serviceInstanceVersion",  utils.getNodeText1(aaiService, "resource-version"))
						utils.log("INFO","Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"), isDebugEnabled)
					}
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoUpdateE2EServiceInstance.postProcessAAIGET " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIGET *** ", isDebugEnabled)
	}

	public void preProcessAAIPUT(DelegateExecution execution) {		
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String msg = ""
		utils.log("INFO"," ***** preProcessAAIPUT *****",  isDebugEnabled)

		String modelUuid = execution.getVariable("modelUuid")
		String serviceInstanceVersion = execution.getVariable("serviceInstanceVersion")
		execution.setVariable("GENPS_serviceResourceVersion", serviceInstanceVersion)

		AaiUtil aaiUriUtil = new AaiUtil(this)
		utils.log("INFO","start create aai uri: " + aaiUriUtil, isDebugEnabled)	
		String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
		utils.log("INFO","aai_uri: " + aai_uri, isDebugEnabled)
		String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
		utils.log("INFO","namespace: " + namespace, isDebugEnabled)

		//update target model to aai
		String serviceInstanceData =
				"""<service-instance xmlns=\"${namespace}\">
			       <model-version-id">${modelUuid}</model-version-id>
				 </service-instance>""".trim()

		execution.setVariable("serviceInstanceData", serviceInstanceData)
		utils.log("INFO","serviceInstanceData: " + serviceInstanceData, isDebugEnabled)
		utils.logAudit(serviceInstanceData)
		utils.log("INFO", " aai_uri " + aai_uri + " namespace:" + namespace, isDebugEnabled)
		utils.log("INFO", " 'payload' to update Service Instance in AAI - " + "\n" + serviceInstanceData, isDebugEnabled)
	
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}	
	
	public void postProcessAAIPUT(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIPUT ***** ", isDebugEnabled)
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean succInAAI = execution.getVariable("GENPS_SuccessIndicator")
			if(!succInAAI){
				utils.log("INFO","Error putting Service-instance in AAI", + serviceInstanceId, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
			}
			else
			{
				//start rollback set up
				RollbackData rollbackData = new RollbackData()
				def disableRollback = execution.getVariable("disableRollback")
				rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
				rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
				rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", serviceInstanceId)
				rollbackData.put("SERVICEINSTANCE", "serviceType", execution.getVariable("serviceType"))
				rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
				execution.setVariable("rollbackData", rollbackData)
			}

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIDEL. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIPUT *** ", isDebugEnabled)
	}	

	public void preProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** preProcessRollback ***** ", isDebugEnabled)
		try {
			
			Object workflowException = execution.getVariable("WorkflowException");

			if (workflowException instanceof WorkflowException) {
				utils.log("INFO", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugEnabled)
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			utils.log("INFO", "BPMN Error during preProcessRollback", isDebugEnabled)
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
		}
		utils.log("INFO"," *** Exit preProcessRollback *** ", isDebugEnabled)
	}

	public void postProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessRollback ***** ", isDebugEnabled)
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				utils.log("INFO", "Setting prevException to WorkflowException: ", isDebugEnabled)
				execution.setVariable("WorkflowException", workflowException);
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			utils.log("INFO", "BPMN Error during postProcessRollback", isDebugEnabled)
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
		}
		utils.log("INFO"," *** Exit postProcessRollback *** ", isDebugEnabled)
	}

        
	public void postConfigRequest(execution){
	    //now do noting
	}

	
}
	
