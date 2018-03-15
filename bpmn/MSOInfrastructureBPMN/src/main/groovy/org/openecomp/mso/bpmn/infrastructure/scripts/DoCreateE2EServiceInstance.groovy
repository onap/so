/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved. 
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
import org.apache.http.HttpResponse
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.AllottedResource
import org.openecomp.mso.bpmn.core.domain.NetworkResource
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.common.recipe.ResourceInput
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder;
import org.openecomp.mso.bpmn.common.recipe.BpmnRestClient
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.List;
import java.util.UUID;

import javax.mail.Quota.Resource;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>DoCreateServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId
 * @param - subscriptionServiceType
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceModelInfo
 * @param - productFamilyId
 * @param - disableRollback
 * @param - failExists - TODO
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion ("1610")
 * @param - serviceDecomposition - Decomposition for R1710 
 * (if macro provides serviceDecompsition then serviceModelInfo, serviceInstanceId & serviceInstanceName will be ignored)
 *
 * Outputs:
 * @param - rollbackData (localRB->null)
 * @param - rolledBack (no localRB->null, localRB F->false, localRB S->true)
 * @param - WorkflowException
 * @param - serviceInstanceName - (GET from AAI if null in input)
 *
 */
public class DoCreateE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DCRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils cutils = new CatalogDbUtils()

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("INFO"," ***** preProcessRequest *****",  isDebugEnabled)

		try {
			execution.setVariable("prefix", Prefix)
			//Inputs
			//requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			utils.log("INFO"," ***** globalSubscriberId *****" + globalSubscriberId,  isDebugEnabled)
			//requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
			String serviceType = execution.getVariable("serviceType")
			utils.log("INFO"," ***** serviceType *****" + serviceType,  isDebugEnabled)
			//requestDetails.requestParameters. for SDNC assignTopology
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
			
			if (productFamilyId == null) {
				execution.setVariable("productFamilyId", "")
			}
			
			String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (isBlank(sdncCallbackUrl)) {
				msg = "URN_mso_workflow_sdncadapter_callback is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			utils.log("INFO","SDNC Callback URL: " + sdncCallbackUrl, isDebugEnabled)

			//requestDetails.modelInfo.for AAI PUT servieInstanceData 			
			//requestDetails.requestInfo. for AAI GET/PUT serviceInstanceData 
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String uuiRequest = execution.getVariable("uuiRequest")
			String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceDefId")
			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.templateId")
			String serviceModelName = jsonUtil.getJsonValue(uuiRequest, "service.parameters.templateName")
			execution.setVariable("serviceModelName", serviceModelName)
			//aai serviceType and Role can be setted as fixed value now.
			String aaiServiceType = "E2E Service"
			String aaiServiceRole = "E2E Service"
			
			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("modelUuid", modelUuid)

			//AAI PUT
			String oStatus = execution.getVariable("initialStatus") ?: ""
			if ("TRANSPORT".equalsIgnoreCase(serviceType))
			{
				oStatus = "Created"
			}

			String statusLine = isBlank(oStatus) ? "" : "<orchestration-status>${oStatus}</orchestration-status>"
				
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
			String serviceInstanceData =
					"""<service-instance xmlns=\"${namespace}\">
			        <service-instance-id>${serviceInstanceId}</service-instance-id>
			        <service-instance-name>${serviceInstanceName}</service-instance-name>
					<service-type>${aaiServiceType}</service-type>
					<service-role>${aaiServiceRole}</service-role>
					${statusLine}
				    <model-invariant-id>${modelInvariantUuid}</model-invariant-id>
				    <model-version-id>${modelUuid}</model-version-id>
					</service-instance>""".trim()					
			execution.setVariable("serviceInstanceData", serviceInstanceData)
			utils.logAudit(serviceInstanceData)
			utils.log("INFO", " aai_uri " + aai_uri + " namespace:" + namespace, isDebugEnabled)
			utils.log("INFO", " 'payload' to create Service Instance in AAI - " + "\n" + serviceInstanceData, isDebugEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}
	
   public void prepareDecomposeService(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        try {
            utils.log("DEBUG", " ***** Inside prepareDecomposeService of create generic e2e service ***** ", isDebugEnabled)
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            utils.log("DEBUG", " ***** Completed prepareDecomposeService of  create generic e2e service ***** ", isDebugEnabled)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
     }

    public void processDecomposition(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
    
        utils.log("DEBUG", " ***** Inside processDecomposition() of  create generic e2e service flow ***** ", isDebugEnabled)    
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
            utils.log("DEBUG", exceptionMessage, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }
    
    public void doServiceHoming(DelegateExecution execution) {
        //Now Homing is not clear. So to be implemented.
    }
    
	public void postProcessAAIGET(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIGET ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				utils.log("INFO","Error getting Service-instance from AAI", + serviceInstanceName, isDebugEnabled)
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
					utils.log("INFO","Found Service-instance in AAI", isDebugEnabled)
					msg = "ServiceInstance already exists in AAI:" + serviceInstanceName
					utils.log("INFO", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIGET *** ", isDebugEnabled)
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
				rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
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
	
	public void postProcessAAIGET2(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIGET2 ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(!succInAAI){
				utils.log("INFO","Error getting Service-instance from AAI in postProcessAAIGET2", + serviceInstanceName, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET2 GENGS_SuccessIndicator:" + succInAAI
					utils.log("INFO", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI){
					String aaiService = execution.getVariable("GENGS_service")
					if (!isBlank(aaiService) && (utils.nodeExists(aaiService, "service-instance-name"))) {
						execution.setVariable("serviceInstanceName",  utils.getNodeText1(aaiService, "service-instance-name"))
						utils.log("INFO","Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"), isDebugEnabled)
					}
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET2 " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIGET2 *** ", isDebugEnabled)
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
            String operationContent = "Prepare service creation"
            utils.log("INFO", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType, isDebugEnabled)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            List<Resource>  resourceList = serviceDecomposition.getServiceResources()
            
            for(String resource : resourceList){
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
	

	/**
	 * sequence resource. we should analyze resource sequence from service template
	 * Here we make VF first, and then network for E2E service.
	 */
	public void sequenceResoure(execution){
	    def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO", "======== Start sequenceResoure Process ======== ", isDebugEnabled)  
	    String serviceModelUUID = execution.getVariable("modelUuid")
        JSONArray networks = cutils.getAllNetworksByServiceModelUuid(execution, serviceModelUUID)
        utils.log("DEBUG", "obtained Network list: " + networks, isDebugEnabled)            
        if (networks == null) {
            utils.log("INFO", "No matching networks in Catalog DB for serviceModelUUID=" + serviceModelUUID, isDebugEnabled)
        }
        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
                
        //we use VF to define a network service
        List<VnfResource>  vnfResourceList= serviceDecomposition.getServiceVnfs()
        
        //here wan is defined as a network resource        
        List<NetworkResource> networkResourceList = serviceDecomposition.getServiceNetworks()

        //allotted resource
        List<AllottedResource> arResourceList= serviceDecomposition.getServiceAllottedResources()
        //define sequenced resource list, we deploy vf first and then network and then ar
        //this is defaule sequence
        List<Resource>  sequencedResourceList = new ArrayList<Resource>();
        if(null != vnfResourceList){
            sequencedResourceList.addAll(vnfResourceList)
        }
        if(null != networkResourceList){
            sequencedResourceList.addAll(networkResourceList)
        }
        if(null != arResourceList){
            sequencedResourceList.addAll(arResourceList)
        }

        String isContainsWanResource = networkResourceList.isEmpty() ? "false" : "true"
        execution.setVariable("isContainsWanResource", isContainsWanResource)
        execution.setVariable("currentResourceIndex", 0)
        execution.setVariable("sequencedResourceList", sequencedResourceList)
        utils.log("INFO", "sequencedResourceList: " + sequencedResourceList, isDebugEnabled)  
        utils.log("INFO", "======== COMPLETED sequenceResoure Process ======== ", isDebugEnabled)  
	}
	
	public void getCurrentResoure(execution){
	    def isDebugEnabled=execution.getVariable("isDebugLogEnabled")   
        utils.log("INFO", "======== Start getCurrentResoure Process ======== ", isDebugEnabled)    
	    def currentIndex = execution.getVariable("currentResourceIndex")
	    List<Resource> sequencedResourceList = execution.getVariable("sequencedResourceList")  
	    Resource currentResource = sequencedResourceList.get(currentIndex)
	    utils.log("INFO", "Now we deal with resouce:" + currentResource.getModelInfo().getModelName(), isDebugEnabled)  
        utils.log("INFO", "======== COMPLETED getCurrentResoure Process ======== ", isDebugEnabled)  
    }

	   /**
     * sequence resource
     */
    public void parseNextResource(execution){
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
    
      /**
      * post config request.
      */
     public void postConfigRequest(execution){
         //now do noting
     } 
     
     public void prepareResourceRecipeRequest(execution){
         def isDebugEnabled=execution.getVariable("isDebugLogEnabled")                 
         utils.log("INFO", "======== Start prepareResourceRecipeRequest Process ======== ", isDebugEnabled) 
         ResourceInput resourceInput = new ResourceInput()         
         String serviceInstanceName = execution.getVariable("serviceInstanceName")
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
         String resourceCustomizationUuid = currentResource.getModelInfo().getModelCustomizationUuid()
         resourceInput.setResourceCustomizationUuid(resourceCustomizationUuid);
         String resourceInvariantUuid = currentResource.getModelInfo().getModelInvariantUuid()
         resourceInput.setResourceInvariantUuid(resourceInvariantUuid)
         String resourceUuid = currentResource.getModelInfo().getModelUuid()
         resourceInput.setResourceUuid(resourceUuid)
         
         String incomingRequest = execution.getVariable("uuiRequest")
         //set the requestInputs from tempalte  To Be Done
         String serviceModelUuid = execution.getVariable("modelUuid")
         String serviceParameters = jsonUtil.getJsonValue(incomingRequest, "service.parameters")
         String resourceParameters = ResourceRequestBuilder.buildResourceRequestParameters(execution, serviceModelUuid, resourceCustomizationUuid, serviceParameters)
         resourceInput.setResourceParameters(resourceParameters)
         execution.setVariable("resourceInput", resourceInput)
         utils.log("INFO", "======== COMPLETED prepareResourceRecipeRequest Process ======== ", isDebugEnabled)      
     }
     
     public void executeResourceRecipe(Execution execution){
         def isDebugEnabled=execution.getVariable("isDebugLogEnabled")                 
         utils.log("INFO", "======== Start executeResourceRecipe Process ======== ", isDebugEnabled) 
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
         
     }
}
