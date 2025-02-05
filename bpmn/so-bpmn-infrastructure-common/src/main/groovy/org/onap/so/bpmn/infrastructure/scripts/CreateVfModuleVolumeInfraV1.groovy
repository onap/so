/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


class CreateVfModuleVolumeInfraV1 extends AbstractServiceTaskProcessor {
	
    private static final Logger logger = LoggerFactory.getLogger( CreateVfModuleVolumeInfraV1.class);
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
		logger.debug(createVolumeIncoming)

		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(createVolumeIncoming)
			setupVariables(execution, reqMap, isDebugEnabled)
			logger.debug("XML request:\n" + createVolumeIncoming)
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
		logger.debug("Generated volumeGroupId: " + volumeGroupId)
		
		// volumeGroupName
		def volGrpName = requestMap.requestDetails.requestInfo?.instanceName ?: ''
		execution.setVariable('volumeGroupName', volGrpName)

		// vfModuleModelInfo
		def vfModuleModelInfo = jsonOutput.toJson(requestMap.requestDetails?.modelInfo)
		execution.setVariable('vfModuleModelInfo', vfModuleModelInfo)
		
		// lcpCloudRegonId
		def lcpCloudRegionId = requestMap.requestDetails.cloudConfiguration.lcpCloudRegionId
		execution.setVariable('lcpCloudRegionId', lcpCloudRegionId)
		
		// cloudOwner
		def cloudOwner = requestMap.requestDetails.cloudConfiguration.cloudOwner
		execution.setVariable('cloudOwner', cloudOwner)
		
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
		logger.debug('disableRollback (suppressRollback) from request: ' + disableRollback)
		
	}

	
	
	public void sendSyncResponse (DelegateExecution execution, isDebugEnabled) {
		def volumeGroupId = execution.getVariable('volumeGroupId')
		def requestId = execution.getVariable("mso-request-id")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")

		String syncResponse = """{"requestReferences":{"instanceId":"${volumeGroupId}","requestId":"${requestId}"}}""".trim()

		logger.debug("Sync Response: " + "\n" + syncResponse)
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
		logger.debug(errorMessage)
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
					<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
						<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
						<lastModifiedBy>BPMN</lastModifiedBy>
						<statusMessage>${MsoUtils.xmlEscape(statusMessage)}</statusMessage>
						<responseBody></responseBody>
						<requestStatus>${MsoUtils.xmlEscape(requestStatus)}</requestStatus>
						<progress>${MsoUtils.xmlEscape(progress)}</progress>
						<vnfOutputs>${MsoUtils.xmlEscape(dbVnfOutputs)}</vnfOutputs>
						<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
					</ns:updateInfraRequest>
			   	</soapenv:Body>
			   </soapenv:Envelope>"""

		String buildDBRequestAsString = utils.formatXml(dbRequest)
		execution.setVariable(prefix+"createDBRequest", buildDBRequestAsString)
		logger.debug("DB Infra Request: " + buildDBRequestAsString)
	}


	/**
	 * Build CommpleteMsoProcess request
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void postProcessResponse (DelegateExecution execution, isDebugEnabled) {

		def dbReturnCode = execution.getVariable(prefix+'dbReturnCode')
		def createDBResponse =  execution.getVariable(prefix+'createDBResponse')

		logger.debug('DB return code: ' + dbReturnCode)
		logger.debug('DB response: ' + createDBResponse)

		def requestId = execution.getVariable("mso-request-id")
		def source = execution.getVariable(prefix+'source')

		String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
							xmlns:ns="http://org.onap/so/request/types/v1">
					<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
						<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						<action>CREATE</action>
						<source>${MsoUtils.xmlEscape(source)}</source>
		   			</request-info>
					<aetgt:status-message>Volume Group has been created successfully.</aetgt:status-message>
		   			<aetgt:mso-bpel-name>BPMN VF Module Volume action: CREATE</aetgt:mso-bpel-name>
				</aetgt:MsoCompletionRequest>"""

		String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

		execution.setVariable(prefix+'Success', true)
		execution.setVariable(prefix+'CompleteMsoProcessRequest', xmlMsoCompletionRequest)
		logger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

	}

	public void prepareFalloutHandlerRequest(DelegateExecution execution, isDebugEnabled) {

		WorkflowException we = execution.getVariable('WorkflowException')
		def errorCode = we?.getErrorCode()
		def errorMessage = we?.getErrorMessage()

		def requestId = execution.getVariable("mso-request-id")
		def source = execution.getVariable(prefix+'source')

		String falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
				                             xmlns:ns="http://org.onap/so/request/types/v1"
				                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
				   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
				      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
				      <action>CREATE</action>
				      <source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>
					   <aetgt:WorkflowException>
					      <aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					      <aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
						</aetgt:WorkflowException>

				</aetgt:FalloutHandlerRequest>"""

		// Format Response
		String xmlHandlerRequest = utils.formatXml(falloutHandlerRequest)

		execution.setVariable(prefix+'FalloutHandlerRequest', xmlHandlerRequest)
		logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), "Overall Error Response " +
				"going to FalloutHandler", "BPMN", ErrorCode.UnknownError.getValue(), "\n" + xmlHandlerRequest);
	}


	/**
	 * Query AAI service instance
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIServiceInstance(DelegateExecution execution, isDebugEnabled) {

		def request = execution.getVariable(prefix+"Request")
		def serviceInstanceId = utils.getNodeText(request, "service-instance-id")
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		try {

			AAIResourceUri uri = AAIUriFactory.createNodesUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
			if(getAAIClient().exists(uri)){
				logger.debug('Service instance ' + serviceInstanceId + ' found in AAI.')
			}else{
				def message = 'Service instance ' + serviceInstanceId + ' was not found in AAI. Return code: 404.'
				logger.debug(message)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, message)
			}
		}catch(BpmnError bpmnError){
			throw bpmnError
		}catch(Exception ex){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, ex.getMessage())
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
