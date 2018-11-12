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

package org.onap.so.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import javax.ws.rs.NotFoundException

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.springframework.web.util.UriUtils

import groovy.json.*

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

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	/**
	 * Gets the service instance and its relationships from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')
			String globalSubscriberId = execution.getVariable('globalSubscriberId')
			String serviceType = execution.getVariable('serviceType')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, serviceInstanceId)
			AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)

			Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
			execution.setVariable("serviceInstanceName", si.get().getServiceInstanceName())
			execution.setVariable("model-invariant-id-original", si.get().getModelInvariantId())
			execution.setVariable("model-version-id-original", si.get().getModelVersionId())

			JSONObject ob = new JSONObject(wrapper.getJson())
			JSONArray ar = ob.getJSONObject("relationship-list").getJSONArray("relationship")

			execution.setVariable("serviceRelationShip", ar.toString())


		}catch(BpmnError e) {
			throw e;
		}catch(NotFoundException e) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 404, "Service-instance does not exist AAI")
		}catch(Exception ex) {
			String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
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

			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			utils.log("DEBUG", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

			String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
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

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			utils.log("INFO", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

			String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
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
					"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
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
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>UPDATE</action>
							<source>${MsoUtils.xmlEscape(source)}</source>
			   			</request-info>
						<status-message>Service Instance was updated successfully.</status-message>
						<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
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
					"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>UPDATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
			execution.setVariable("falloutRequest", falloutRequest)
		} catch (Exception ex) {
			utils.log("INFO", "Exception prepareFalloutRequest:" + ex.getMessage(), isDebugEnabled)
			String errorException = "  Bpmn error encountered in UpdateCustomE2EServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			String requestId = execution.getVariable("msoRequestId")
			String falloutRequest =
					"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>UPDATE</action>
					      <source>UUI</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorException)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable("falloutRequest", falloutRequest)
		}
		utils.log("INFO", "*** Exit prepareFalloutRequest ***", isDebugEnabled)
	}
}
