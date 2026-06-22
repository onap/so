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

package org.onap.so.bpmn.infrastructure.scripts;

import static org.apache.commons.lang3.StringUtils.*;

import jakarta.ws.rs.NotFoundException

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
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.springframework.web.util.UriUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.*

/**
 * This groovy class supports the <class>UpdateCustomE2EServiceInstance.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Update
 *
 */
public class UpdateCustomE2EServiceInstance extends AbstractServiceTaskProcessor {
	private static final Logger logger = LoggerFactory.getLogger( UpdateCustomE2EServiceInstance.class);

	String Prefix="UPDSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()


	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		String msg = ""
		logger.info( " *** preProcessRequest() *** ")

		try {

			String siRequest = execution.getVariable("bpmnRequest")

			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)
			logger.info( "Input Request:" + siRequest + " reqId:" + requestId)

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
				logger.info( msg)
			} else {
				execution.setVariable("productFamilyId", productFamilyId)
			}

			 //user params
	         String userParams = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.userParams")
             logger.info( "userParams:" + userParams)
	         List<String> paramList = jsonUtil.StringArrayToList(execution, userParams)
	         String uuiRequest = jsonUtil.getJsonValue(paramList.get(0), "UUIRequest")
			if (isBlank(uuiRequest)) {
				msg = "Input uuiRequest is null"
				logger.info( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else
			{
				execution.setVariable("uuiRequest", uuiRequest)
			}

			logger.info( "uuiRequest:\n" + uuiRequest)

			//serviceType for aai
			String serviceType = jsonUtil.getJsonValue(uuiRequest, "service.serviceType")
			if (isBlank(serviceType)) {
				msg = "Input serviceType is null"
				logger.info( msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("serviceType", serviceType)
			}

			// target model info
			String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
			logger.info("modelInvariantUuid: " + modelInvariantUuid)
			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("model-invariant-id-target", modelInvariantUuid)

			String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
			logger.info("modelUuid: " + modelUuid)
			execution.setVariable("modelUuid", modelUuid)
			execution.setVariable("model-version-id-target", modelUuid)

			String serviceModelName = jsonUtil.getJsonValue(uuiRequest, "service.parameters.templateName")
			logger.info("serviceModelName: " + serviceModelName)
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
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info(" ***** Exit preProcessRequest *****")
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
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
			AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)

			Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
			execution.setVariable("serviceInstanceName", si.get().getServiceInstanceName())
			execution.setVariable("model-invariant-id-original", si.get().getModelInvariantId())
			execution.setVariable("model-version-id-original", si.get().getModelVersionId())

			JSONObject ob = new JSONObject(wrapper.getJson())
			JSONArray ar = ob.getJSONObject("relationship-list").getJSONArray("relationship")

			execution.setVariable("serviceInstanceData-original", si.get())
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
		logger.debug( " ======== STARTED postCompareModelVersions Process ======== ")

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

		logger.debug( "======== COMPLETED postCompareModelVersions Process ======== ")
	}

	/**
	 * Init the service Operation Status
	 */
	public void prepareInitServiceOperationStatus(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		logger.debug( " ======== STARTED prepareInitServiceOperationStatus Process ======== ")
		try{
			String serviceId = execution.getVariable("serviceInstanceId")
			String operationId = execution.getVariable("operationId")
			String operationType = execution.getVariable("operationType")
			String userId = ""
			String result = "processing"
			String progress = "0"
			String reason = ""
			String operationContent = "Prepare service updating"
			logger.debug( "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
			serviceId = UriUtils.encode(serviceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceId)
			execution.setVariable("operationId", operationId)
			execution.setVariable("operationType", operationType)

			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			logger.debug( "DB Adapter Endpoint is: " + dbAdapterEndpoint)

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
			logger.error( "Outgoing updateServiceOperStatusRequest: \n" + payload)

		}catch(Exception e){
			logger.debug( "Exception Occured Processing prepareInitServiceOperationStatus. Exception is:\n" + e)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareInitServiceOperationStatus Method:\n" + e.getMessage())
		}
		logger.debug( "======== COMPLETED prepareInitServiceOperationStatus Process ======== ")
	}

	/**
	 * Update the service Operation Status
	 */
	public void preUpdateServiceOperationStatus(DelegateExecution execution){
		def method = getClass().getSimpleName() + '.preUpdateServiceOperationStatus(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		logger.info("Entered " + method)

		try{
			String serviceId = execution.getVariable("serviceInstanceId")
			String operationId = execution.getVariable("operationId")
			String operationType = execution.getVariable("operationType")
			String serviceName = execution.getVariable("serviceInstanceName")
			String result = execution.getVariable("operationResult")
			String progress = execution.getVariable("progress")
			String reason = execution.getVariable("operationReason")
			String userId = ""
			logger.info( "progress: " + progress )

			String operationContent = "Prepare service : " + execution.getVariable("operationStatus")

			logger.info( "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
			serviceId = UriUtils.encode(serviceId,"UTF-8")
			execution.setVariable("serviceInstanceId", serviceId)
			execution.setVariable("operationId", operationId)
			execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			logger.info( "DB Adapter Endpoint is: " + dbAdapterEndpoint)

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
			logger.error( "Outgoing preUpdateServiceOperationStatus: \n" + payload)


		}catch(Exception e){
			logger.info( "Exception Occured Processing preUpdateServiceOperationStatus. Exception is:\n" + e)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preUpdateServiceOperationStatus Method:\n" + e.getMessage())
		}
		logger.info( "======== COMPLETED preUpdateServiceOperationStatus Process ======== ")
		logger.info( "Exited " + method)
	}

	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info( " *** sendSyncResponse *** ")

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

			logger.info( " sendSyncResponse to APIH:" + "\n" + updateServiceResp)
			sendWorkflowResponse(execution, 202, updateServiceResp)
			execution.setVariable("sentSyncResponse", true)

		} catch (Exception ex) {
			String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info(" ***** Exit sendSyncResopnse *****")
	}

	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info( " *** sendSyncError *** ")

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

			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			logger.info( " Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}

	}

	public void prepareCompletionRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info( " *** prepareCompletion *** ")

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
			logger.info( " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

		} catch (Exception ex) {
			String msg = " Exception in prepareCompletion:" + ex.getMessage()
			logger.info( msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info( "*** Exit prepareCompletionRequest ***")
	}

	public void prepareFalloutRequest(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		logger.info( " *** prepareFalloutRequest *** ")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			logger.info( " Input Workflow Exception: " + wfex.toString())
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
			logger.info( "Exception prepareFalloutRequest:" + ex.getMessage())
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
		logger.info( "*** Exit prepareFalloutRequest ***")
	}
}
