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

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONArray;
import org.json.JSONObject;
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.springframework.web.util.UriUtils;

import groovy.json.*

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
public class DoCreateE2EServiceInstanceV2 extends AbstractServiceTaskProcessor {

	String Prefix="DCRESI_"
	private static final String DebugFlag = "isDebugEnabled"
	
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
	    //only for dug
		execution.setVariable("isDebugLogEnabled","true")
		execution.setVariable("unit_test", "true")
		execution.setVariable("skipVFC", "true")
		
		def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String msg = ""
		utils.log("INFO"," ***** Enter DoCreateE2EServiceInstanceV2 preProcessRequest *****",  isDebugEnabled)
		
		utils.log("INFO","  unit test : " + execution.getVariable("unit_test"),  isDebugEnabled)	

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
			utils.log("INFO","uuiRequest: " + uuiRequest, isDebugEnabled)
			
			String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceDefId")
			utils.log("INFO","modelInvariantUuid: " + modelInvariantUuid, isDebugEnabled)
			
			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.templateId")
			utils.log("INFO","modelUuid: " + modelUuid, isDebugEnabled)
			
			String serviceModelName = jsonUtil.getJsonValue(uuiRequest, "service.parameters.templateName")
			utils.log("INFO","serviceModelName: " + serviceModelName, isDebugEnabled)
			execution.setVariable("serviceModelName", serviceModelName)
			
            //aai serviceType and Role can be setted as fixed value now.
			String aaiServiceType = serviceType
			String aaiServiceRole = serviceType+"Role"
			
			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("modelUuid", modelUuid)

			//AAI PUT
			String oStatus = execution.getVariable("initialStatus") ?: ""
			utils.log("INFO","oStatus: " + oStatus, isDebugEnabled)
			if ("TRANSPORT".equalsIgnoreCase(serviceType))
			{
				oStatus = "Created"
			}
			
			

			String statusLine = isBlank(oStatus) ? "" : "<orchestration-status>${oStatus}</orchestration-status>"
			utils.log("INFO","statusLine: " + statusLine, isDebugEnabled)	
			AaiUtil aaiUriUtil = new AaiUtil(this)
			utils.log("INFO","start create aai uri: " + aaiUriUtil, isDebugEnabled)	
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			utils.log("INFO","aai_uri: " + aai_uri, isDebugEnabled)
			String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
			utils.log("INFO","namespace: " + namespace, isDebugEnabled)
			/*
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
            */
            //begin only for test
			String serviceInstanceData =
					"""<service-instance xmlns=\"${namespace}\">
			        <service-instance-id>${serviceInstanceId}</service-instance-id>
			        <service-instance-name>${serviceInstanceName}</service-instance-name>
					<service-type>${aaiServiceType}</service-type>
					<service-role>${aaiServiceRole}</service-role>
					${statusLine}
					</service-instance>""".trim()
			//end only for test
			execution.setVariable("serviceInstanceData", serviceInstanceData)
			utils.log("INFO","serviceInstanceData: " + serviceInstanceData, isDebugEnabled)
			utils.logAudit(serviceInstanceData)
			utils.log("INFO", " aai_uri " + aai_uri + " namespace:" + namespace, isDebugEnabled)
			utils.log("INFO", " 'payload' to create Service Instance in AAI - " + "\n" + serviceInstanceData, isDebugEnabled)
			
			execution.setVariable("serviceSDNCCreate", "false")
			execution.setVariable("operationStatus", "Waiting deploy resource...")			

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}

	public void postProcessAAIGET(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessAAIGET(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** Enter DoCreateE2EServiceInstanceV2 postProcessAAIGET ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(succInAAI != true){
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
				if(foundInAAI == true){
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
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}

	public void postProcessAAIPUT(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessAAIPUT(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** Enter DoCreateE2EServiceInstanceV2 postProcessAAIPUT ***** ", isDebugEnabled)
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean succInAAI = execution.getVariable("GENPS_SuccessIndicator")
			if(succInAAI != true){
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
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	public void postProcessAAIGET2(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessAAIGET2(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** Enter DoCreateE2EServiceInstanceV2 postProcessAAIGET2 ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(succInAAI != true){
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
				if(foundInAAI == true){
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
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}

	public void preProcessRollback (Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessRollback(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** Enter DoCreateE2EServiceInstanceV2 preProcessRollback ***** ", isDebugEnabled)
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
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}

	public void postProcessRollback (Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessRollback(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** Enter DoCreateE2EServiceInstanceV2 postProcessRollback ***** ", isDebugEnabled)
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
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	/**
	 * Init the service Operation Status
	 */
	public void preUpdateServiceOperationStatus(Execution execution){
        def method = getClass().getSimpleName() + '.preUpdateServiceOperationStatus(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String serviceName = execution.getVariable("serviceInstanceName")
            String operationType = "CREATE"
            String userId = ""
            String result = "processing"
            String progress = execution.getVariable("progress")
			utils.log("INFO", "progress: " + progress , isDebugEnabled)
			if ("100".equalsIgnoreCase(progress))
			{
				result = "finished"
			}
            String reason = ""
            String operationContent = "Prepare service creation : " + execution.getVariable("operationStatus")
			
            utils.log("INFO", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId, isDebugEnabled)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = "http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter"
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            utils.log("INFO", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

            execution.setVariable("URN_mso_openecomp_adapters_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")
			String payload =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.openecomp.mso/requestsdb">
                            <serviceId>${serviceId}</serviceId>
                            <operationId>${operationId}</operationId>
                            <serviceName>${serviceName}</serviceName>
                            <operationType>${operationType}</operationType>
                            <userId>${userId}</userId>
                            <result>${result}</result>
                            <operationContent>${operationContent}</operationContent>
                            <progress>${progress}</progress>
                            <reason>${reason}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
            utils.log("INFO", "Outgoing preUpdateServiceOperationStatus: \n" + payload, isDebugEnabled)
           

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing preUpdateServiceOperationStatus. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preUpdateServiceOperationStatus Method:\n" + e.getMessage())
        }
        utils.log("INFO", "======== COMPLETED preUpdateServiceOperationStatus Process ======== ", isDebugEnabled)  
        utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	

	public void preInitResourcesOperStatus(Execution execution){
        def method = getClass().getSimpleName() + '.preInitResourcesOperStatus(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)

        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 preInitResourcesOperStatus Process ======== ", isDebugEnabled)
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
            String incomingRequest = execution.getVariable("uuiRequest")
            String resourcesStr = jsonUtil.getJsonValue(incomingRequest, "service.parameters.resources")  
            List<String> resourceList = jsonUtil.StringArrayToList(execution, resourcesStr)   
            for(String resource : resourceList){
                    resourceTemplateUUIDs  = resourceTemplateUUIDs + jsonUtil.getJsonValue(resource, "resourceId") + ":"
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
            utils.logAudit("DoCustomDeleteE2EServiceInstanceV2 Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing preInitResourcesOperStatus. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	/**
	 * prepare resource create request
	 */
	public void preResourceRequest(execution){
	    def method = getClass().getSimpleName() + '.preResourceRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 preResourceRequest Process ======== ", isDebugEnabled)
		try {
			String resourceType = execution.getVariable("resourceType")
	    	String serviceInstanceName = execution.getVariable("serviceInstanceName")
	    	String nsServiceName = resourceType + "_" + serviceInstanceName
	    	execution.setVariable("nsServiceName", nsServiceName)
	    	utils.log("INFO", "Prepare VFC Request nsServiceName:" + nsServiceName, isDebugEnabled)
        	String globalSubscriberId = execution.getVariable("globalSubscriberId")
        	String serviceType = execution.getVariable("serviceType")
     		String serviceId = execution.getVariable("serviceInstanceId")
        	execution.setVariable("serviceId", serviceId)
        	String operationId = execution.getVariable("operationId")
        	String incomingRequest = execution.getVariable("uuiRequest")
        	String resourcesStr = jsonUtil.getJsonValue(incomingRequest, "service.parameters.resources")  
        	String nsServiceDescription = jsonUtil.getJsonValue(incomingRequest, "service.description")  
        	execution.setVariable("nsServiceDescription", nsServiceDescription)
        	utils.log("INFO", "Prepare VFC Request nsServiceDescription:" + nsServiceDescription, isDebugEnabled)
        	List<String> resourceList = jsonUtil.StringArrayToList(execution, resourcesStr)   
        	for(String resource : resourceList){
            	String resourceName = jsonUtil.getJsonValue(resource, "resourceName")  
            	if(StringUtils.containsIgnoreCase(resourceName, resourceType)){
                	String resourceUUID  = jsonUtil.getJsonValue(resource, "resourceId")
                	String resourceInvariantUUID  = jsonUtil.getJsonValue(resource, "resourceDefId")
                	String resourceParameters = jsonUtil.getJsonValue(resource, "nsParameters")                
                	execution.setVariable("resourceUUID", resourceUUID)
                	execution.setVariable("resourceInvariantUUID", resourceInvariantUUID)
                	execution.setVariable("resourceParameters", resourceParameters)
                	utils.log("INFO", "Prepare VFC Request resourceType:" + resourceType, isDebugEnabled)
                	utils.log("INFO", "Prepare VFC Request resourceUUID:" + resourceUUID, isDebugEnabled)
                	utils.log("INFO", "Prepare VFC Request resourceParameters:" + resourceParameters, isDebugEnabled)
            	} 
        	}
		} catch (BpmnError b) {
			utils.log("INFO", "BPMN Error during preResourceRequest", isDebugEnabled)
			throw b;
		} catch(Exception ex) {
			msg = "Exception in preResourceRequest. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
		}
	   utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	 /**
     * post config request.
     */
	public void postConfigRequest(execution){
	    //now do noting
	}
	
    /***********************************************************************************************/

	private void loadResourcesProperties(Execution execution) {
		def method = getClass().getSimpleName() + '.loadResourcesProperties(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String loadFilePath = "/etc/mso/config.d/reources.json"
		try{
			def jsonPayload = new File(loadFilePath).text
			utils.log("INFO","jsonPayload: " + jsonPayload, isDebugEnabled)

			String resourcesProperties = jsonUtil.prettyJson(jsonPayload.toString())
			utils.log("INFO","resourcesProperties: " + resourcesProperties, isDebugEnabled)
			
			String createResourceSort = jsonUtil.getJsonValue(resourcesProperties, "CreateResourceSort")
			//utils.log("INFO","createResourceSort: " + createResourceSort, isDebugEnabled)
			execution.setVariable("createResourceSort", createResourceSort)
			
			String deleteResourceSort = jsonUtil.getJsonValue(resourcesProperties, "DeleteResourceSort")
			//utils.log("INFO","deleteResourceSort: " + deleteResourceSort, isDebugEnabled)
			execution.setVariable("deleteResourceSort", deleteResourceSort)
			
			
			String resourceControllerType = jsonUtil.getJsonValue(resourcesProperties, "ResourceControllerType")
			//utils.log("INFO","resourceControllerType: " + resourceControllerType, isDebugEnabled)
			execution.setVariable("resourceControllerType", resourceControllerType)			
			
			
		}catch(Exception ex){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in " + method + " - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	private sortCreateResource(Execution execution) {
		def method = getClass().getSimpleName() + '.sortCreateResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String createResourceSortDef = """[
                {
                    "resourceType":"vEPC"
                },
                {
                    "resourceType":"vIMS"
                },
                {
                    "resourceType":"vCPE"
                },
                {
                    "resourceType":"vFW"
                },
				{
                    "resourceType":"Underlay"
                },
                {
                    "resourceType":"Overlay"
                },
				{
                    "resourceType":"GRE_AAR"
                },
                {
                    "resourceType":"APN_AAR"
                },
                {
                    "resourceType":"VPN_SAR"
                },
                {
                    "resourceType":"GRE_SAR"
                }             
                
            ]""".trim()
		
        try{

			loadResourcesProperties(execution)
			String createResourceSort = execution.getVariable("createResourceSort")
			if (isBlank(createResourceSort)) {
				createResourceSort = createResourceSortDef;
			}
			
			List<String> sortResourceList = jsonUtil.StringArrayToList(execution, createResourceSort)
	        utils.log("INFO", "sortResourceList : " + sortResourceList, isDebugEnabled) 		 

			JSONArray newResourceList      = new JSONArray()
			int resSortCount = sortResourceList.size()
  
			
			for ( int currentResource = 0 ; currentResource < resSortCount ; currentResource++ ) { 
				String sortResource = sortResourceList[currentResource]
				String resourceType = jsonUtil.getJsonValue(sortResource, "resourceType")				
				List<String> resourceList = execution.getVariable(Prefix+"resourceList")

				for (String resource : resourceList) {
					//utils.log("INFO", "resource : " + resource, isDebugEnabled)
					String resourceName = jsonUtil.getJsonValue(resource, "resourceName")
					//utils.log("INFO", "resource Name : " + resourceName, isDebugEnabled)
					String[] split = resourceName.split("_")
					
					utils.log("INFO", "split : " + split, isDebugEnabled)
					int strLen = split.size()
					String allottedResourceType = ""
			
					if (strLen <2) {
						allottedResourceType = split[0]
					}
					else {
						allottedResourceType = split[0] + "_" + split[1]
					}
			
					if (StringUtils.containsIgnoreCase(allottedResourceType, resourceType)) {
						utils.log("INFO", "allottedResourceType : " + allottedResourceType + " resourceType : " + resourceType, isDebugEnabled)
						utils.log("INFO", "resource : " + resource , isDebugEnabled)
						JSONObject jsonObj = new JSONObject(resource)
						newResourceList.put(jsonObj)
						
					}
					utils.log("INFO", "Get next sort type " , isDebugEnabled)
				}
			} 
			utils.log("INFO", "newResourceList : " + newResourceList, isDebugEnabled)
            String newResourceStr = newResourceList.toString()		
            List<String> newResourceListStr = jsonUtil.StringArrayToList(execution, newResourceStr)			
		
			execution.setVariable(Prefix+"resourceList", newResourceListStr)
			utils.log("INFO", "newResourceList : " + newResourceListStr, isDebugEnabled) 
			
		}catch(Exception ex){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in " + method + " - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)
		
	}
	/**
	 * get service resources
	 */
	public void getServiceResources(Execution execution){
        def method = getClass().getSimpleName() + '.getServiceResources(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 getServiceResources Process ======== ", isDebugEnabled)
        try{
            execution.setVariable(Prefix+"resourceCount", 0)
			execution.setVariable(Prefix+"nextResource", 0)

			String incomingRequest = execution.getVariable("uuiRequest")
            String resourcesStr = jsonUtil.getJsonValue(incomingRequest, "service.parameters.resources")  
            utils.log("INFO", "Resources String : " + resourcesStr, isDebugEnabled)
			if (!isBlank(resourcesStr)) {
				List<String> resourceList = jsonUtil.StringArrayToList(execution, resourcesStr)   
            	utils.log("INFO", "Resource List : " + resourceList, isDebugEnabled)
				execution.setVariable(Prefix+"resourceList", resourceList)
				execution.setVariable(Prefix+"resourceCount", resourceList.size())
				execution.setVariable(Prefix+"nextResource", 0)
			}
			
			int resourceNum = execution.getVariable(Prefix+"nextResource")
			utils.log("DEBUG", "Current Resource count:"+ execution.getVariable(Prefix+"nextResource"), isDebugEnabled)
			
			int resourceCount = execution.getVariable(Prefix+"resourceCount")
			utils.log("DEBUG", "Total Resource count:"+ execution.getVariable(Prefix+"resourceCount"), isDebugEnabled)

            if (resourceNum < resourceCount) {
				execution.setVariable(Prefix+"resourceFinish", false)
			}
			else {
			    execution.setVariable(Prefix+"resourceFinish", true)
			}
			sortCreateResource(execution)

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing getServiceResources. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable(Prefix+"ErrorResponse", "Error Occurred during getServiceResources Method:\n" + e.getMessage())
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}
	
	/**
	 * prepare Decompose next resource to create request
	 */
	public void preProcessDecomposeNextResource(Execution execution){
        def method = getClass().getSimpleName() + '.preProcessDecomposeNextResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 preProcessDecomposeNextResource Process ======== ", isDebugEnabled)
        try{
            int resourceNum = execution.getVariable(Prefix+"nextResource")
			List<String> resourceList = execution.getVariable(Prefix+"resourceList")
			utils.log("INFO", "Resource List : " + resourceList, isDebugEnabled)
			
			String resource = resourceList[resourceNum]
            execution.setVariable(Prefix+"resource", resource)
			utils.log("INFO", "Current Resource : " + resource, isDebugEnabled)

            String resourceName = jsonUtil.getJsonValue(resource, "resourceName")  
			execution.setVariable(Prefix+"resourceName", resourceName)
			utils.log("INFO", "resource Name : " + resourceName, isDebugEnabled)
			
            String resourceUUID  = jsonUtil.getJsonValue(resource, "resourceId")
			execution.setVariable("resourceUUID", resourceUUID)
			utils.log("INFO", "resource UUID : " + resourceUUID, isDebugEnabled)
			
            String resourceInvariantUUID  = jsonUtil.getJsonValue(resource, "resourceDefId")
			execution.setVariable("resourceInvariantUUID", resourceInvariantUUID)
			
			
            String resourceParameters = jsonUtil.getJsonValue(resource, "nsParameters")             
            execution.setVariable("resourceParameters", resourceParameters)
			utils.log("INFO", "resource Parameters : " + resourceParameters, isDebugEnabled)

			execution.setVariable(Prefix+"nextResource", resourceNum + 1)
			utils.log("INFO", "next Resource num : " + execution.getVariable(Prefix+"nextResource"), isDebugEnabled)
			
			int resourceCount = execution.getVariable(Prefix+"resourceCount")
			if (resourceCount >0 ){
			    int progress = (resourceNum*100) / resourceCount
				execution.setVariable("progress", progress.toString() )
			}
			
			execution.setVariable("operationStatus", resourceName )

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing preProcessDecomposeNextResource. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable(Prefix+"ErrorResponse", "Error Occurred during preProcessDecomposeNextResource Method:\n" + e.getMessage())
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)   
	}
	/**
	 * post Decompose next resource to create request
	 */
	public void postProcessDecomposeNextResource(Execution execution){
        def method = getClass().getSimpleName() + '.postProcessDecomposeNextResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== STARTED DoCreateE2EServiceInstanceV2 postProcessDecomposeNextResource Process ======== ", isDebugEnabled)
        try{
            String resourceName = execution.getVariable(Prefix+"resourceName")
			
			int resourceNum = execution.getVariable(Prefix+"nextResource")
			utils.log("DEBUG", "Current Resource count:"+ execution.getVariable(Prefix+"nextResource"), isDebugEnabled)
			
			int resourceCount = execution.getVariable(Prefix+"resourceCount")
			utils.log("DEBUG", "Total Resource count:"+ execution.getVariable(Prefix+"resourceCount"), isDebugEnabled)

            if (resourceNum < resourceCount) {
				execution.setVariable(Prefix+"resourceFinish", false)
			}
			else {
			    execution.setVariable(Prefix+"resourceFinish", true)
			}
			
			utils.log("DEBUG", "Resource Finished:"+ execution.getVariable(Prefix+"resourceFinish"), isDebugEnabled)
			
			if (resourceCount >0 ){
			    int progress = (resourceNum*100) / resourceCount
				execution.setVariable("progress", progress.toString() )
				utils.log("DEBUG", "progress :"+ execution.getVariable("progress"), isDebugEnabled)
			}
			execution.setVariable("operationStatus", resourceName )

        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in "+method + "- " + e.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}
	/**
	* prepare check Resource Type 
	*/
	public void checkResourceType(Execution execution){
        def method = getClass().getSimpleName() + '.checkResourceType(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String resourceControllerTypeDef = """[
                {
                    "resourceType":"vEPC",
                    "controllerType":"VFC"
                },
                {
                    "resourceType":"vIMS",
                    "controllerType":"VFC"
                },
                {
                    "resourceType":"vCPE",
                    "controllerType":"VFC"
                },
                {
                    "resourceType":"vFW",
                    "controllerType":"VFC"
                },
                {
                    "resourceType":"Underlay",
                    "controllerType":"SDNC"
                },
                {
                    "resourceType":"Overlay",
                    "controllerType":"SDNC"
                },
                {
                    "resourceType":"VPN_SAR",
                    "controllerType":"SDNC"
                },
                {
                    "resourceType":"GRE_AAR",
                    "controllerType":"APPC"
                },
                {
                    "resourceType":"GRE_SAR",
                    "controllerType":"SDNC"
                }  ,
                {
                    "resourceType":"APN_AAR",
                    "controllerType":"APPC"
                }               
                
            ]""".trim()

        try{

            String resourceName = execution.getVariable(Prefix+"resourceName") 
			utils.log("INFO", "resourceName : " + resourceName, isDebugEnabled)
			execution.setVariable("resourceName", resourceName)
			
			String[] split = resourceName.split("_")
			
			utils.log("INFO", "split : " + split, isDebugEnabled)
			int strLen = split.size()
			String allottedResourceType = ""
			
			if (strLen <2) {
				allottedResourceType = split[0]
			}
			else {
				allottedResourceType = split[0] + "_" + split[1]
			}

			loadResourcesProperties(execution)
			String resourceControllerType= execution.getVariable("resourceControllerType") 
			if (isBlank(resourceControllerType)) {
				resourceControllerType = resourceControllerTypeDef;
			}
			utils.log("INFO", "resourceControllerType: " + resourceControllerType, isDebugEnabled)
		
			List<String> ResourceTypeList = jsonUtil.StringArrayToList(execution, resourceControllerType)
	        utils.log("INFO", "ResourceTypeList : " + ResourceTypeList, isDebugEnabled) 		 
			execution.setVariable("controllerType", "Other") 
			execution.setVariable(Prefix+"resourceType", "")
			for (String resourceMap : ResourceTypeList) {
				String resourceType = jsonUtil.getJsonValue(resourceMap, "resourceType")				
				String controllerType = jsonUtil.getJsonValue(resourceMap, "controllerType")
				//utils.log("INFO", "resourceMap.resourceType   : " + resourceType, isDebugEnabled)
				//utils.log("INFO", "resourceMap.controllerType : " + controllerType, isDebugEnabled)
				//utils.log("INFO", "resourceName               : " + resourceName, isDebugEnabled)
				//utils.log("INFO", "allottedResourceType       : " + allottedResourceType, isDebugEnabled )
				
				if (StringUtils.containsIgnoreCase(allottedResourceType, resourceType)) {
					execution.setVariable("controllerType", controllerType)
					execution.setVariable(Prefix+"resourceType", resourceType)
					utils.log("INFO", "found controller type : " + controllerType, isDebugEnabled)
					break
				}
			}
			utils.log("INFO", "controller Type : " + execution.getVariable("controllerType"), isDebugEnabled)
			utils.log("INFO", "resource Type : " + execution.getVariable(Prefix+"resourceType"), isDebugEnabled)
			
			if (execution.getVariable("controllerType") == "") {
				String exceptionMessage = "Resource name can not find controller type,please check the resource Name: "+ resourceName
				utils.log("DEBUG", exceptionMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			}
			if (execution.getVariable(Prefix+"resourceType") == "vCPE") {
				execution.setVariable("skipVFC", "false")
				utils.log("INFO", "vCPE will deploy ", isDebugEnabled)
			}

        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in "+ method + " - " + e.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}
	/**
	* prepare post Unkown Resource Type 
	*/
	public void postOtherControllerType(Execution execution){
        def method = getClass().getSimpleName() + '.postOtherControllerType(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 postOtherControllerType Process ======== ", isDebugEnabled)
        try{

            String resourceName = execution.getVariable(Prefix+"resourceName") 
			String resourceType = execution.getVariable(Prefix+"resourceType") 
			String controllerType = execution.getVariable("controllerType")
			
		    String msg = "Resource name: "+ resourceName + " resource Type: " + resourceType+ " controller Type: " + controllerType + " can not be processed  n the workflow"
			utils.log("DEBUG", msg, isDebugEnabled)
			
        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in "+ method + " - " + e.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)   
	}
	
	/**
	 * prepare Controller resource create request
	 */
	public void preProcessResourceRequestForController(execution){
        def method = getClass().getSimpleName() + '.preProcessResourceRequestForController(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 preProcessResourceRequestForController Process ======== ", isDebugEnabled)
        try{
            String resourceName = execution.getVariable(Prefix+"resourceName") 
	        String resourceType = execution.getVariable(Prefix+"resourceType")
			String serviceInstanceName =execution.getVariable("serviceInstanceName") 
			String nsServiceName = resourceType + "_" + serviceInstanceName
	        execution.setVariable("nsServiceName", nsServiceName)
	        utils.log("INFO", "Prepare Controller Request nsServiceName:" + nsServiceName, isDebugEnabled)

            String serviceId = execution.getVariable("serviceInstanceId")
            execution.setVariable("serviceId", serviceId)
	        utils.log("INFO", "Prepare Controller Request serviceId:" + serviceId, isDebugEnabled) 
			
			String globalSubscriberId = execution.getVariable("globalSubscriberId")
			utils.log("INFO", "Prepare Controller Request globalSubscriberId:" + globalSubscriberId, isDebugEnabled) 
			
			String incomingRequest = execution.getVariable("uuiRequest")
			String nsServiceDescription = jsonUtil.getJsonValue(incomingRequest, "service.description")  
            execution.setVariable("nsServiceDescription", nsServiceDescription)
            utils.log("INFO", "Prepare Controller Request nsServiceDescription:" + nsServiceDescription, isDebugEnabled)

            String resourceUUID  = execution.getVariable("resourceUUID")
         
            utils.log("INFO", "Prepare Controller Request resourceUUID:" + resourceUUID, isDebugEnabled)
			
            String resourceInvariantUUID  = execution.getVariable("resourceInvariantUUID")
			utils.log("INFO", "Prepare Controller Request resourceInvariantUUID:" + resourceInvariantUUID, isDebugEnabled)

            String resourceParameters = execution.getVariable("resourceParameters")              
            execution.setVariable("resourceParameters", resourceParameters)
            utils.log("INFO", "Prepare Controller Request resourceParameters:" + resourceParameters, isDebugEnabled)



        }catch(Exception e){
            String exceptionMessage = "Bpmn error encountered in "+ method + " - " + e.getMessage()
			utils.log("ERROR", exceptionMessage, isDebugEnabled)
            execution.setVariable(Prefix+"ErrorResponse", exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}
	/**
	 * post process VFC resource create request
	 */
	public void postProcessResourceRequestForVFC(execution){
        def method = getClass().getSimpleName() + '.postProcessResourceRequestForVFC(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 postProcessResourceRequestForVFC Process ======== ", isDebugEnabled)
        try{
            

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing postProcessResourceRequestForVFC. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable(Prefix+"ErrorResponse", "Error Occurred during postProcessResourceRequestForVFC Method:\n" + e.getMessage())
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}


	/**
	 * post process SDNC resource create request
	 */
	public void postProcessResourceRequestForSDNC(execution){
        def method = getClass().getSimpleName() + '.postProcessResourceRequestForSDNC(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== Enter DoCreateE2EServiceInstanceV2 postProcessResourceRequestForSDNC Process ======== ", isDebugEnabled)
        try{
		
          execution.setVariable("serviceSDNCCreate", "true")

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing postProcessResourceRequestForSDNC. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable(Prefix+"ErrorResponse", "Error Occurred during postProcessResourceRequestForSDNC Method:\n" + e.getMessage())
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	

	
}
	
