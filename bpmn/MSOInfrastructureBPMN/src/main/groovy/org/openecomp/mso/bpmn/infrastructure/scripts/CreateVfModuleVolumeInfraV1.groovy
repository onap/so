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

import org.openecomp.mso.bpmn.common.scripts.AaiUtil;
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import java.util.Map;

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.*

class CreateVfModuleVolumeInfraV1 extends AbstractServiceTaskProcessor {

	public static final String  prefix='CVMVINFRAV1_'

	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		setBasicDBAuthHeader(execution, isDebugEnabled)
		preProcessRequest(execution, isDebugEnabled)
	}


	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void preProcessRequest (DelegateExecution execution, isDebugEnabled) {

		execution.setVariable("prefix",prefix)
		setSuccessIndicator(execution, false)
		execution.setVariable(prefix+'syncResponseSent', false)

		String createVolumeIncoming = validateRequest(execution, 'vnfId')
		utils.logAudit(createVolumeIncoming)

		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(createVolumeIncoming)
			setupVariables(execution, reqMap, isDebugEnabled)
			utils.log("DEBUG", "XML request:\n" + createVolumeIncoming, isDebugEnabled)
		}
		catch(groovy.json.JsonException je) {
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, 'Request is not a valid JSON document')
		}

		// For rollback in this flow
		setBasicDBAuthHeader(execution, isDebugEnabled)
		setRollbackEnabled(execution, isDebugEnabled)
	}

	
	/**
	 * Set up variables that will be passed to the BB DoCreatevfModuleVolume flow 
	 * @param execution
	 * @param requestMap
	 * @param serviceInstanceId
	 * @param isDebugLogEnabled
	 */
	public void setupVariables(DelegateExecution execution, Map requestMap, isDebugLogEnabled) {
		
		def jsonOutput = new JsonOutput()
		
		// volumeGroupId - is generated
		String volumeGroupId = UUID.randomUUID()
		execution.setVariable('volumeGroupId', volumeGroupId)
		utils.log("DEBUG", "Generated volumeGroupId: " + volumeGroupId, isDebugLogEnabled)
		
		// volumeGroupName
		def volGrpName = requestMap.requestDetails.requestInfo?.instanceName ?: ''
		execution.setVariable('volumeGroupName', volGrpName)

		// vfModuleModelInfo
		def vfModuleModelInfo = jsonOutput.toJson(requestMap.requestDetails?.modelInfo)
		execution.setVariable('vfModuleModelInfo', vfModuleModelInfo)
		
		// lcpCloudRegonId
		def lcpCloudRegionId = requestMap.requestDetails.cloudConfiguration.lcpCloudRegionId
		execution.setVariable('lcpCloudRegionId', lcpCloudRegionId)
		
		// tenant
		def tenantId = requestMap.requestDetails.cloudConfiguration.tenantId
		execution.setVariable('tenantId', tenantId)
		
		// source
		def source = requestMap.requestDetails.requestInfo.source
		execution.setVariable(prefix+'source', source)
		
		// vnfType and asdcServiceModelVersion
		
		def serviceName = ''
		def asdcServiceModelVersion = ''
		def modelCustomizationName = ''
		
		def relatedInstanceList = requestMap.requestDetails.relatedInstanceList
		relatedInstanceList.each {
			if (it.relatedInstance.modelInfo?.modelType == 'service') {
				serviceName = it.relatedInstance.modelInfo?.modelName
				asdcServiceModelVersion = it.relatedInstance.modelInfo?.modelVersion
			}
			if (it.relatedInstance.modelInfo?.modelType == 'vnf') {
				modelCustomizationName = it.relatedInstance.modelInfo?.modelCustomizationName
			}
		}
		
		def vnfType = serviceName + '/' + modelCustomizationName
		execution.setVariable('vnfType', vnfType)
		execution.setVariable('asdcServiceModelVersion', asdcServiceModelVersion)
		
		// vfModuleInputParams
		def userParams = requestMap.requestDetails?.requestParameters?.userParams
		Map<String, String> vfModuleInputMap = [:]
		
		userParams.each { userParam ->
			vfModuleInputMap.put(userParam.name, userParam.value.toString())
		}
		execution.setVariable('vfModuleInputParams', vfModuleInputMap)

		// disableRollback (true or false)
		def disableRollback = requestMap.requestDetails.requestInfo.suppressRollback
		execution.setVariable('disableRollback', disableRollback)
		utils.log("DEBUG", 'disableRollback (suppressRollback) from request: ' + disableRollback, isDebugLogEnabled)
		
	}

	
	
	public void sendSyncResponse (DelegateExecution execution, isDebugEnabled) {
		def volumeGroupId = execution.getVariable('volumeGroupId')
		def requestId = execution.getVariable("mso-request-id")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")

		String syncResponse = """{"requestReferences":{"instanceId":"${volumeGroupId}","requestId":"${requestId}"}}""".trim()

		utils.log("DEBUG", "Sync Response: " + "\n" + syncResponse, isDebugEnabled)
		sendWorkflowResponse(execution, 200, syncResponse)

		execution.setVariable(prefix+'syncResponseSent', true)
	}


	public void sendSyncError (DelegateExecution execution, isDebugEnabled) {
		WorkflowException we = execution.getVariable('WorkflowException')
		def errorCode = we?.getErrorCode()
		def errorMessage = we?.getErrorMessage()
		//default to 400 since only invalid request will trigger this method
		sendWorkflowResponse(execution, 400, errorMessage)
	}


	/**
	 * Create a WorkflowException
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void buildWorkflowException(DelegateExecution execution, int errorCode, errorMessage, isDebugEnabled) {
		utils.log("DEBUG", errorMessage, isDebugEnabled)
		(new ExceptionUtil()).buildWorkflowException(execution, 2500, errorMessage)
	}


	/**
	 * Build Infra DB Request
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void prepareDbInfraSuccessRequest(DelegateExecution execution, isDebugEnabled) {
		def dbVnfOutputs = execution.getVariable(prefix+'volumeOutputs')
		def requestId = execution.getVariable('mso-request-id')
		def statusMessage = "VolumeGroup successfully created."
		def requestStatus = "COMPLETED"
		def progress = "100"
		
		/*
		from: $gVolumeGroup/aai:volume-group-id/text()
		to: vnfreq:volume-outputs/vnfreq:volume-group-id
		*/
		// for now assume, generated volumeGroupId is accepted
		def volumeGroupId = execution.getVariable(prefix+'volumeGroupId')

		String dbRequest =
			"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
				<soapenv:Header/>
				<soapenv:Body>
					<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
						<requestId>${requestId}</requestId>
						<lastModifiedBy>BPMN</lastModifiedBy>
						<statusMessage>${statusMessage}</statusMessage>
						<responseBody></responseBody>
						<requestStatus>${requestStatus}</requestStatus>
						<progress>${progress}</progress>
						<vnfOutputs>${dbVnfOutputs}</vnfOutputs>
						<volumeGroupId>${volumeGroupId}</volumeGroupId>
					</ns:updateInfraRequest>
			   	</soapenv:Body>
			   </soapenv:Envelope>"""

		String buildDBRequestAsString = utils.formatXml(dbRequest)
		execution.setVariable(prefix+"createDBRequest", buildDBRequestAsString)
		utils.log("DEBUG", "DB Infra Request: " + buildDBRequestAsString, isDebugEnabled)
		utils.logAudit(buildDBRequestAsString)
	}


	/**
	 * Build CommpleteMsoProcess request
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void postProcessResponse (DelegateExecution execution, isDebugEnabled) {

		def dbReturnCode = execution.getVariable(prefix+'dbReturnCode')
		def createDBResponse =  execution.getVariable(prefix+'createDBResponse')

		utils.logAudit('DB return code: ' + dbReturnCode)
		utils.logAudit('DB response: ' + createDBResponse)

		def requestId = execution.getVariable("mso-request-id")
		def source = execution.getVariable(prefix+'source')

		String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
							xmlns:ns="http://org.openecomp/mso/request/types/v1">
					<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
						<request-id>${requestId}</request-id>
						<action>CREATE</action>
						<source>${source}</source>
		   			</request-info>
					<aetgt:status-message>Volume Group has been created successfully.</aetgt:status-message>
		   			<aetgt:mso-bpel-name>BPMN VF Module Volume action: CREATE</aetgt:mso-bpel-name>
				</aetgt:MsoCompletionRequest>"""

		String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

		utils.logAudit(createDBResponse)
		utils.logAudit(xmlMsoCompletionRequest)
		execution.setVariable(prefix+'Success', true)
		execution.setVariable(prefix+'CompleteMsoProcessRequest', xmlMsoCompletionRequest)
		utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

	}

	public void prepareFalloutHandlerRequest(DelegateExecution execution, isDebugEnabled) {

		WorkflowException we = execution.getVariable('WorkflowException')
		def errorCode = we?.getErrorCode()
		def errorMessage = we?.getErrorMessage()

		def requestId = execution.getVariable("mso-request-id")
		def source = execution.getVariable(prefix+'source')

		String falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
				                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
				                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
				   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
				      <request-id>${requestId}</request-id>
				      <action>CREATE</action>
				      <source>${source}</source>
				   </request-info>
					   <aetgt:WorkflowException>
					      <aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
					      <aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>

				</aetgt:FalloutHandlerRequest>"""

		// Format Response
		String xmlHandlerRequest = utils.formatXml(falloutHandlerRequest)
		utils.logAudit(xmlHandlerRequest)

		execution.setVariable(prefix+'FalloutHandlerRequest', xmlHandlerRequest)
		utils.log("ERROR", "Overall Error Response going to FalloutHandler: " + "\n" + xmlHandlerRequest, isDebugEnabled)
	}


	/**
	 * Query AAI service instance
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIServiceInstance(DelegateExecution execution, isDebugEnabled) {

		def request = execution.getVariable(prefix+"Request")
		def serviceInstanceId = utils.getNodeText1(request, "service-instance-id")

		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = aaiUtil.getSearchNodesQueryEndpoint(execution)

		def String queryAAIRequest = aaiEndpoint + "?search-node-type=service-instance&filter=service-instance-id:EQUALS:" + serviceInstanceId
		utils.logAudit("AAI query service instance request: " + queryAAIRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()
		aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)

		utils.logAudit("AAI query service instance return code: " + returnCode)
		utils.logAudit("AAI query service instance response: " + aaiResponseAsString)

		utils.log("DEBUG", "AAI query service instance return code: " + returnCode, isDebugEnabled)
		utils.log("DEBUG", "AAI query service instance response: " + aaiResponseAsString, isDebugEnabled)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		if (returnCode=='200') {
			utils.log("DEBUG", 'Service instance ' + serviceInstanceId + ' found in AAI.', isDebugEnabled)
		} else {
			if (returnCode=='404') {
				def message = 'Service instance ' + serviceInstanceId + ' was not found in AAI. Return code: 404.'
				utils.log("DEBUG", message, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, message)
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}
	
	public void logAndSaveOriginalException(DelegateExecution execution, isDebugLogEnabled) {
		logWorkflowException(execution, 'CreateVfModuleVolumeInfraV1 caught an event')
		saveWorkflowException(execution, 'CVMVINFRAV1_originalWorkflowException')
	}
	
	public void validateRollbackResponse(DelegateExecution execution, isDebugLogEnabled) {

		def originalException = execution.getVariable("CVMVINFRAV1_originalWorkflowException")
		execution.setVariable("WorkflowException", originalException)
		execution.setVariable("RollbackCompleted", true)

	}
}
