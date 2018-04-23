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
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.rest.APIResponse

import java.util.List;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.json.JSONArray
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>UpdateCustomE2EServiceInstance.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Update
 *
 */
public class UpdateCustomE2EServiceInstance extends AbstractServiceTaskProcessor {
	String Prefix="UPDSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()


	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		utils.log("INFO", " *** preProcessRequest() *** ", isDebugEnabled)

		try {

			String siRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(siRequest)

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			utils.log("INFO", "Input Request:" + siRequest + " reqId:" + requestId, isDebugEnabled)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if (isBlank(serviceInstanceId)) {
				msg = "Input serviceInstanceId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
						
			//subscriberInfo for aai
			String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "requestDetails.subscriberInfo.globalSubscriberId")
			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId' is null"
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("globalSubscriberId", globalSubscriberId)
			}

			//requestDetails
			execution.setVariable("source", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.source"))
			execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName"))
			execution.setVariable("disableRollback", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.suppressRollback"))
			String productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
			if (isBlank(productFamilyId))
			{
				msg = "Input productFamilyId is null"
				utils.log("INFO", msg, isDebugEnabled)				
			} else {
				execution.setVariable("productFamilyId", productFamilyId)
			}
			
			 //user params
	         String userParams = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.userParams")      
             utils.log("INFO", "userParams:" + userParams, isDebugEnabled)
	         List<String> paramList = jsonUtil.StringArrayToList(execution, userParams)
	         String uuiRequest = jsonUtil.getJsonValue(paramList.get(0), "UUIRequest")			
			if (isBlank(uuiRequest)) {
				msg = "Input uuiRequest is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("uuiRequest", uuiRequest)
			}

			utils.log("INFO", "uuiRequest:\n" + uuiRequest,  isDebugEnabled)

			//serviceType for aai
			String serviceType = jsonUtil.getJsonValue(uuiRequest, "service.serviceType")
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("serviceType", serviceType)
			}
			
			// target model info
			String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
			utils.log("INFO","modelInvariantUuid: " + modelInvariantUuid, isDebugEnabled)
			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("model-invariant-id-target", modelInvariantUuid)
			
			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
			utils.log("INFO","modelUuid: " + modelUuid, isDebugEnabled)
			execution.setVariable("modelUuid", modelUuid)
			execution.setVariable("model-version-id-target", modelUuid)
			
			String serviceModelName = jsonUtil.getJsonValue(uuiRequest, "service.parameters.templateName")
			utils.log("INFO","serviceModelName: " + serviceModelName, isDebugEnabled)
			if(serviceModelName == null) {
				serviceModelName = ""
			}
			execution.setVariable("serviceModelName", serviceModelName)			
			
			//operationId
			String operationId = jsonUtil.getJsonValue(siRequest, "operationId")
		 	if (isBlank(operationId)) {
		 		operationId = UUID.randomUUID().toString()
		 	 }   
			execution.setVariable("operationId", operationId) 
			execution.setVariable("operationType", "update")
			execution.setVariable("hasResourcetoUpdate", false)
			
			execution.setVariable("URN_mso_adapters_openecomp_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
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

				utils.log("INFO", "SI Data" + siData, isDebugEnabled)
				
				// serviceInstanceName
				String serviceInstanceName = execution.getVariable("serviceInstanceName")
				if(isBlank(serviceInstanceName) && utils.nodeExists(siData, "service-instance-name")) {
					serviceInstanceName =  utils.getNodeText1(siData, "service-instance-name")
					execution.setVariable("serviceInstanceName", serviceInstanceName)
				}

				// Get Template uuid and version
				if (utils.nodeExists(siData, "model-invariant-id") && utils.nodeExists(siData, "model-version-id") ) {
					utils.log("INFO", "SI Data model-invariant-id and model-version-id exist:", isDebugEnabled)

					def modelInvariantId  = utils.getNodeText1(siData, "model-invariant-id")
					def modelVersionId  = utils.getNodeText1(siData, "model-version-id")

					// Set Original Template info
					execution.setVariable("model-invariant-id-original", modelInvariantId)
					execution.setVariable("model-version-id-original", modelVersionId)
				}
				
				//get related service instances (vnf/network or volume) for delete
				if (utils.nodeExists(siData, "relationship-list")) {
					utils.log("INFO", "SI Data relationship-list exists:", isDebugEnabled)

					JSONArray jArray = new JSONArray()

					XmlParser xmlParser = new XmlParser()
					Node root = xmlParser.parseText(siData)
					def relation_list = utils.getChildNode(root, 'relationship-list')
					def relationships = utils.getIdenticalChildren(relation_list, 'relationship')

					for (def relation: relationships) {
						def jObj = getRelationShipData(relation, isDebugEnabled)
						jArray.put(jObj)
					}

					execution.setVariable("serviceRelationShip", jArray.toString())
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
	
	private JSONObject getRelationShipData(node, isDebugEnabled){
		JSONObject jObj = new JSONObject()
		
		def relation  = utils.nodeToString(node)
		def rt  = utils.getNodeText1(relation, "related-to")
		
		def rl  = utils.getNodeText1(relation, "related-link")
		utils.log("INFO", "ServiceInstance Related NS/Configuration :" + rl, isDebugEnabled)
		
		def rl_datas = utils.getIdenticalChildren(node, "relationship-data")
		for(def rl_data : rl_datas) {
			def eKey =  utils.getChildNodeText(rl_data, "relationship-key")
			def eValue = utils.getChildNodeText(rl_data, "relationship-value")

			if ((rt == "service-instance" && eKey.equals("service-instance.service-instance-id"))
			//for overlay/underlay
			|| (rt == "configuration" && eKey.equals("configuration.configuration-id"))){
				jObj.put("resourceInstanceId", eValue)
			}
		}

		def rl_props = utils.getIdenticalChildren(node, "related-to-property")
		for(def rl_prop : rl_props) {
			def eKey =  utils.getChildNodeText(rl_prop, "property-key")
			def eValue = utils.getChildNodeText(rl_prop, "property-value")
			if((rt == "service-instance" && eKey.equals("service-instance.service-instance-name"))
			//for overlay/underlay
			|| (rt == "configuration" && eKey.equals("configuration.configuration-type"))){
				jObj.put("resourceType", eValue)
			}
		}

		utils.log("INFO", "Relationship related to Resource:" + jObj.toString(), isDebugEnabled)

		return jObj
	}
	
	
	public void preCompareModelVersions(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
	}

	public void postCompareModelVersions(DelegateExecution execution) {		
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ======== STARTED postCompareModelVersions Process ======== ", isDebugEnabled)
		
		def hasResourcetoUpdate = false
		def hasResourcetoAdd = false
		def hasResourcetoDelete = false
		List<Resource> addResourceList =  execution.getVariable("addResourceList")
		List<Resource> delResourceList =  execution.getVariable("delResourceList")
		
		if(addResourceList != null && !addResourceList.isEmpty()) {
			hasResourcetoAdd = true
		}
				
		if(delResourceList != null && !delResourceList.isEmpty()) {
			hasResourcetoDelete = true
		}
		
		hasResourcetoUpdate = hasResourcetoAdd || hasResourcetoDelete
		execution.setVariable("hasResourcetoUpdate", hasResourcetoUpdate)
		
		utils.log("DEBUG", "======== COMPLETED postCompareModelVersions Process ======== ", isDebugEnabled)		
	}
	
	/**
	 * Init the service Operation Status
	 */
	public void prepareInitServiceOperationStatus(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ======== STARTED prepareInitServiceOperationStatus Process ======== ", isDebugEnabled)
		try{
			String serviceId = execution.getVariable("serviceInstanceId")
			String operationId = execution.getVariable("operationId")
			String operationType = execution.getVariable("operationType")
			String userId = ""
			String result = "processing"
			String progress = "0"
			String reason = ""
			String operationContent = "Prepare service updating"
			utils.log("DEBUG", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId, isDebugEnabled)
			serviceId = UriUtils.encode(serviceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceId)
			execution.setVariable("operationId", operationId)
			execution.setVariable("operationType", operationType)

			def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			utils.log("DEBUG", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

			String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.openecomp.mso/requestsdb">
                            <serviceId>${serviceId}</serviceId>
                            <operationId>${operationId}</operationId>
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
			utils.log("DEBUG", "Outgoing updateServiceOperStatusRequest: \n" + payload, isDebugEnabled)
			utils.logAudit("CreateVfModuleInfra Outgoing updateServiceOperStatusRequest Request: " + payload)

		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing prepareInitServiceOperationStatus. Exception is:\n" + e, isDebugEnabled)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareInitServiceOperationStatus Method:\n" + e.getMessage())
		}
		utils.log("DEBUG", "======== COMPLETED prepareInitServiceOperationStatus Process ======== ", isDebugEnabled)
	}
	
	/**
	 * Update the service Operation Status
	 */
	public void preUpdateServiceOperationStatus(DelegateExecution execution){		
		def method = getClass().getSimpleName() + '.preUpdateServiceOperationStatus(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		
		try{
			String serviceId = execution.getVariable("serviceInstanceId")
			String operationId = execution.getVariable("operationId")
			String operationType = execution.getVariable("operationType")
			String serviceName = execution.getVariable("serviceInstanceName")
			String result = execution.getVariable("operationResult")						
			String progress = execution.getVariable("progress")
			String reason = execution.getVariable("operationReason")
			String userId = ""
			utils.log("INFO", "progress: " + progress , isDebugEnabled)

			String operationContent = "Prepare service : " + execution.getVariable("operationStatus")
			
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

	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO", " *** sendSyncResponse *** ", isDebugEnabled)

		try {
			String operationId = execution.getVariable("operationId")
			def hasResourcetoUpdate = execution.getVariable("hasResourcetoUpdate")
			
			String updateServiceResp = ""
			if(hasResourcetoUpdate) {
				// RESTResponse for API Handler (APIH) Reply Task
				updateServiceResp = """{"operationId":"${operationId}"}""".trim()
			}
			else {
				updateServiceResp =  """{"OperationResult":"No Resource to Add or Delete or Service Instance not found in AAI."}"""
			}
			
			utils.log("INFO", " sendSyncResponse to APIH:" + "\n" + updateServiceResp, isDebugEnabled)
			sendWorkflowResponse(execution, 202, updateServiceResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," ***** Exit sendSyncResopnse *****",  isDebugEnabled)
	}

	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO", " *** sendSyncError *** ", isDebugEnabled)

		try {
			String errorMessage = ""
			int errorCode = 7000
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
				errorCode = wfe.getErrorCode()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
					"""<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			utils.logAudit(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			utils.log("INFO", " Sending Sync Error Activity Failed. " + "\n" + ex.getMessage(), isDebugEnabled)
		}

	}

	public void prepareCompletionRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO", " *** prepareCompletion *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("msoRequestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String source = execution.getVariable("source")
			
			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
								xmlns:ns="http://org.openecomp/mso/request/types/v1">
						<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
							<request-id>${requestId}</request-id>
							<action>UPDATE</action>
							<source>${source}</source>
			   			</request-info>
						<status-message>Service Instance was updated successfully.</status-message>
						<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
			   			<mso-bpel-name>UpdateCustomE2EServiceInstance</mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			execution.setVariable("completionRequest", xmlMsoCompletionRequest)
			utils.log("INFO", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "*** Exit prepareCompletionRequest ***", isDebugEnabled)
	}

	public void prepareFalloutRequest(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO", " *** prepareFalloutRequest *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("INFO", " Input Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestId = execution.getVariable("msoRequestId")
			String source = execution.getVariable("source")
			String requestInfo =
					"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>UPDATE</action>
					<source>${source}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			utils.log("INFO", "Exception prepareFalloutRequest:" + ex.getMessage(), isDebugEnabled)
			String errorException = "  Bpmn error encountered in UpdateCustomE2EServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
					"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>UPDATE</action>
					      <source>UUI</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorException}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable("falloutRequest", falloutRequest)
		}
		utils.log("INFO", "*** Exit prepareFalloutRequest ***", isDebugEnabled)
	}	
}
