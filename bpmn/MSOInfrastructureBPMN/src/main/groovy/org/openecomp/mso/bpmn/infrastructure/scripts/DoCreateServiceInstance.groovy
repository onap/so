/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import static org.apache.commons.lang3.StringUtils.*
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.ServiceInstance
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.springframework.web.util.UriUtils

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
public class DoCreateServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DCRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** preProcessRequest *****",  isDebugEnabled)

		try {
			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("prefix", Prefix)
			
			//Inputs
			//requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
			String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId

			//requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

			//requestDetails.requestParameters. for SDNC assignTopology
			String productFamilyId = execution.getVariable("productFamilyId") //AAI productFamilyId

			if (isBlank(globalSubscriberId)) {
				msg = "Input globalSubscriberId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			
			if (isBlank(subscriptionServiceType)) {
				msg = "Input subscriptionServiceType is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			
			if (productFamilyId == null) {
				execution.setVariable("productFamilyId", "")
			}
			
			String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (isBlank(sdncCallbackUrl)) {
				msg = "URN_mso_workflow_sdncadapter_callback is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			utils.log("DEBUG","SDNC Callback URL: " + sdncCallbackUrl, isDebugEnabled)

			//requestDetails.modelInfo.for AAI PUT servieInstanceData & SDNC assignTopology
			String modelInvariantUuid = ""
			String modelVersion = ""
			String modelUuid = ""
			String modelName = ""
			String serviceInstanceName = "" 
			//Generated in parent.for AAI PUT
			String serviceInstanceId = ""
			String serviceType = ""
			String serviceRole = ""
			
			ServiceDecomposition serviceDecomp = (ServiceDecomposition) execution.getVariable("serviceDecomposition")
			if (serviceDecomp != null)
			{
				serviceType = serviceDecomp.getServiceType()
				if (serviceType == null)
				{
					utils.log("DEBUG", "null serviceType", isDebugEnabled)
					serviceType = ""
				}
				else
				{
					utils.log("DEBUG", "serviceType:" + serviceType, isDebugEnabled)
				}
				serviceRole = serviceDecomp.getServiceRole()
				if (serviceRole == null)
				{
					serviceRole = ""
				}
				
				ServiceInstance serviceInstance = serviceDecomp.getServiceInstance()
				if (serviceInstance != null)
				{
					serviceInstanceId = serviceInstance.getInstanceId()
					serviceInstanceName = serviceInstance.getInstanceName()
					execution.setVariable("serviceInstanceId", serviceInstanceId)
					execution.setVariable("serviceInstanceName", serviceInstanceName)
				}
				
				ModelInfo modelInfo = serviceDecomp.getModelInfo()
				if (modelInfo != null)
				{
					modelInvariantUuid = modelInfo.getModelInvariantUuid()
					modelVersion = modelInfo.getModelVersion()
					modelUuid = modelInfo.getModelUuid()
					modelName = modelInfo.getModelName()
				}
				else 
				{
					msg = "Input serviceModelInfo is null"
					utils.log("DEBUG", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
			}
			else
			{
				//requestDetails.requestInfo. for AAI GET/PUT serviceInstanceData & SDNC assignToplology
				serviceInstanceName = execution.getVariable("serviceInstanceName")
				serviceInstanceId = execution.getVariable("serviceInstanceId")
				
				String serviceModelInfo = execution.getVariable("serviceModelInfo")
				if (isBlank(serviceModelInfo)) {
					msg = "Input serviceModelInfo is null"
					utils.log("DEBUG", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
				modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
				modelVersion = jsonUtil.getJsonValue(serviceModelInfo, "modelVersion")
				modelUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelUuid")
				modelName = jsonUtil.getJsonValue(serviceModelInfo, "modelName")
				//modelCustomizationUuid NA for SI
	
			}
			execution.setVariable("serviceType", serviceType)
			execution.setVariable("serviceRole", serviceRole)
			
			if (serviceInstanceName == null) {
				execution.setVariable("serviceInstanceName", "")
				serviceInstanceName = ""
			}
			if (isBlank(serviceInstanceId)){
				msg = "Input serviceInstanceId is null"
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			
			if (modelInvariantUuid == null) {
				modelInvariantUuid = ""
			}
			if (modelUuid == null) {
				modelUuid = ""
			}
			if (modelVersion == null) {
				modelVersion = ""
			}
			if (modelName == null) {
				modelName = ""
			}
			
			execution.setVariable("modelInvariantUuid", modelInvariantUuid)
			execution.setVariable("modelVersion", modelVersion)
			execution.setVariable("modelUuid", modelUuid)
			execution.setVariable("modelName", modelName)
			
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

			//AAI PUT
			String oStatus = execution.getVariable("initialStatus") ?: ""
			if ("TRANSPORT".equalsIgnoreCase(serviceType))
			{
				oStatus = "Created"
			}

			String statusLine = isBlank(oStatus) ? "" : "<orchestration-status>${oStatus}</orchestration-status>"
			String serviceTypeLine = isBlank(serviceType) ? "" : "<service-type>${serviceType}</service-type>"
			String serviceRoleLine = isBlank(serviceRole) ? "" : "<service-role>${serviceRole}</service-role>"
				
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String namespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
			String serviceInstanceData =
					"""<service-instance xmlns=\"${namespace}\">
					<service-instance-name>${serviceInstanceName}</service-instance-name>
					${serviceTypeLine}
					${serviceRoleLine}
					${statusLine}
				    <model-invariant-id>${modelInvariantUuid}</model-invariant-id>
				    <model-version-id>${modelUuid}</model-version-id>
					</service-instance>""".trim()

			execution.setVariable("serviceInstanceData", serviceInstanceData)
			utils.logAudit(serviceInstanceData)
			utils.log("DEBUG", " 'payload' to create Service Instance in AAI - " + "\n" + serviceInstanceData, isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," ***** Exit preProcessRequest *****",  isDebugEnabled)
	}

	//TODO: Will be able to replace with call to GenericGetService
	public void getAAICustomerById (Execution execution) {
		// https://{aaiEP}/aai/v8/business/customers/customer/{globalCustomerId}
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		try {

			String globalCustomerId = execution.getVariable("globalSubscriberId") //VID to AAI name map
			utils.log("DEBUG"," ***** getAAICustomerById ***** globalCustomerId:" + globalCustomerId, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			if (isBlank(aai_endpoint) || isBlank(aai_uri))
			{
				msg = "AAI URL is invalid. Endpoint:" + aai_endpoint + aai_uri
				utils.log("DEBUG", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			String getAAICustomerUrl = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(globalCustomerId,"UTF-8")

			utils.logAudit(getAAICustomerUrl)
			utils.log("DEBUG", "getAAICustomerById Url:" + getAAICustomerUrl, isDebugEnabled)
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getAAICustomerUrl)
			String returnCode = response.getStatusCode()
			String aaiResponseAsString = response.getResponseBodyAsString()

			msg = "getAAICustomerById ResponseCode:" + returnCode + " ResponseString:" + aaiResponseAsString
			utils.log("DEBUG",msg, isDebugEnabled)
			utils.logAudit(msg)

			if (returnCode=='200') {
				// Customer found by ID. FLow to proceed.
				utils.log("DEBUG",msg, isDebugEnabled)

				//TODO Deferred
				//we might verify that service-subscription with matching name exists
				//and throw error if not. If not checked, we will get exception in subsequent step on Create call
				//in 1610 we assume both customer & service subscription were pre-created

			} else {
				if (returnCode=='404') {
					msg = "GlobalCustomerId:" + globalCustomerId + " not found (404) in AAI"
					utils.log("DEBUG", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)

				} else {
					if (aaiResponseAsString.contains("RESTFault")) {
						utils.log("ERROR", aaiResponseAsString)
						WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
						execution.setVariable("WorkflowException", workflowException)
						throw new BpmnError("MSOWorkflowException")

					} else {
						// aai all errors
						msg = "Error in getAAICustomerById ResponseCode:" + returnCode
						utils.log("DEBUG", msg, isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
					}
				}
			}

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in getAAICustomerById. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit getAAICustomerById *****", isDebugEnabled)

	}

	public void postProcessAAIGET(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessAAIGET ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(succInAAI != true){
				utils.log("DEBUG","Error getting Service-instance from AAI", + serviceInstanceName, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
					utils.log("DEBUG", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
			else
			{
				boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
				if(foundInAAI == true){
					utils.log("DEBUG","Found Service-instance in AAI", isDebugEnabled)
					msg = "ServiceInstance already exists in AAI:" + serviceInstanceName
					utils.log("DEBUG", msg, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *** Exit postProcessAAIGET *** ", isDebugEnabled)
	}

	public void postProcessAAIPUT(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessAAIPUT ***** ", isDebugEnabled)
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean succInAAI = execution.getVariable("GENPS_SuccessIndicator")
			if(succInAAI != true){
				utils.log("DEBUG","Error putting Service-instance in AAI", + serviceInstanceId, isDebugEnabled)
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
			throw e
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIDEL. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *** Exit postProcessAAIPUT *** ", isDebugEnabled)
	}

	public void preProcessSDNCAssignRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String msg = ""
		utils.log("DEBUG"," ***** preProcessSDNCAssignRequest *****", isDebugEnabled)

		try {
			def serviceInstanceId = execution.getVariable("serviceInstanceId")
			def serviceInstanceName = execution.getVariable("serviceInstanceName")
			def callbackURL = execution.getVariable("sdncCallbackUrl")
			def requestId = execution.getVariable("msoRequestId")
			def serviceId = execution.getVariable("productFamilyId")
			def subscriptionServiceType = execution.getVariable("subscriptionServiceType")
			def globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
			def serviceType = execution.getVariable("serviceType")

			def modelInvariantUuid = execution.getVariable("modelInvariantUuid")
			def modelVersion = execution.getVariable("modelVersion")
			def modelUuid = execution.getVariable("modelUuid")
			def modelName = execution.getVariable("modelName")
			
			def sdncRequestId = UUID.randomUUID().toString()
			
			def siParamsXml = execution.getVariable("siParamsXml")
			
			String sdncAssignRequest =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${sdncRequestId}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>assign</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
							<sdncadapter:MsoAction>${serviceType}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${requestId}</request-id>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
						<request-action>CreateServiceInstance</request-action>
					</request-information>
					<service-information>
						<service-id>${serviceId}</service-id>
						<subscription-service-type>${subscriptionServiceType}</subscription-service-type>
						<onap-model-information>
					         <model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
					         <model-uuid>${modelUuid}</model-uuid>
					         <model-version>${modelVersion}</model-version>
					         <model-name>${modelName}</model-name>
					    </onap-model-information>
						<service-instance-id>${serviceInstanceId}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${globalSubscriberId}</global-customer-id>
					</service-information>
					<service-request-input>
						<service-instance-name>${serviceInstanceName}</service-instance-name>
						${siParamsXml}
					</service-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			utils.log("DEBUG","sdncAssignRequest:\n" + sdncAssignRequest, isDebugEnabled)
			sdncAssignRequest = utils.formatXml(sdncAssignRequest)
			execution.setVariable("sdncAssignRequest", sdncAssignRequest)
			utils.logAudit("sdncAssignRequest:  " + sdncAssignRequest)

			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncDelete = sdncAssignRequest.replace(">assign<", ">delete<").replace(">CreateServiceInstance<", ">DeleteServiceInstance<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			def sdncRequestId3 = UUID.randomUUID().toString()
			String sdncDeactivate = sdncDelete.replace(">delete<", ">deactivate<").replace(">${sdncRequestId2}<", ">${sdncRequestId3}<")
			def rollbackData = execution.getVariable("rollbackData")
			rollbackData.put("SERVICEINSTANCE", "sdncDeactivate", sdncDeactivate)
			rollbackData.put("SERVICEINSTANCE", "sdncDelete", sdncDelete)
			execution.setVariable("rollbackData", rollbackData)
			
			utils.log("DEBUG","rollbackData:\n" + rollbackData.toString(), isDebugEnabled)

		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCAssignRequest. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *****Exit preProcessSDNCAssignRequest *****", isDebugEnabled)
	}
	
	public void postProcessSDNCAssign (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessSDNCAssign ***** ", isDebugEnabled)
		try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			utils.logAudit("workflowException: " + workflowException)

			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

			String response = execution.getVariable("sdncAdapterResponse")
			utils.logAudit("SDNCResponse: " + response)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
				utils.log("DEBUG","Good response from SDNC Adapter for service-instance  topology assign: \n" + response, isDebugEnabled)

				def rollbackData = execution.getVariable("rollbackData")
				rollbackData.put("SERVICEINSTANCE", "rollbackSDNC", "true")
				execution.setVariable("rollbackData", rollbackData)

			}else{
				utils.log("DEBUG","Bad Response from SDNC Adapter for service-instance assign", isDebugEnabled)
				throw new BpmnError("MSOWorkflowException")
			}

		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNCAssign. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *** Exit postProcessSDNCAssign *** ", isDebugEnabled)
	}
	
	public void postProcessAAIGET2(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessAAIGET2 ***** ", isDebugEnabled)
		String msg = ""

		try {
			String serviceInstanceName = execution.getVariable("serviceInstanceName")
			boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
			if(succInAAI != true){
				utils.log("DEBUG","Error getting Service-instance from AAI in postProcessAAIGET2", + serviceInstanceName, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					msg = "Failure in postProcessAAIGET2 GENGS_SuccessIndicator:" + succInAAI
					utils.log("DEBUG", msg, isDebugEnabled)
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
						utils.log("DEBUG","Found Service-instance in AAI.serviceInstanceName:" + execution.getVariable("serviceInstanceName"), isDebugEnabled)
					}
				}
			}
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in DoCreateServiceInstance.postProcessAAIGET2 " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("DEBUG"," *** Exit postProcessAAIGET2 *** ", isDebugEnabled)
	}

	public void preProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** preProcessRollback ***** ", isDebugEnabled)
		try {
			
			Object workflowException = execution.getVariable("WorkflowException")

			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugEnabled)
				execution.setVariable("prevWorkflowException", workflowException)
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
			utils.log("DEBUG", "BPMN Error during preProcessRollback", isDebugEnabled)
		} catch(Exception ex) {
			String msg = "Exception in preProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit preProcessRollback *** ", isDebugEnabled)
	}

	public void postProcessRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessRollback ***** ", isDebugEnabled)
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException")
			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Setting prevException to WorkflowException: ", isDebugEnabled)
				execution.setVariable("WorkflowException", workflowException)
			}
			execution.setVariable("rollbackData", null)
		} catch (BpmnError b) {
			utils.log("DEBUG", "BPMN Error during postProcessRollback", isDebugEnabled)
			throw b
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit postProcessRollback *** ", isDebugEnabled)
	}

}
