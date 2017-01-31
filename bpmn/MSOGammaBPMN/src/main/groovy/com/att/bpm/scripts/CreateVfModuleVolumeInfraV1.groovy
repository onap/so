/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package com.att.bpm.scripts

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;
import org.apache.commons.lang3.*

class CreateVfModuleVolumeInfraV1 extends AbstractServiceTaskProcessor {

	public static final String  prefix='CVMVINFRAV1_'

	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		preProcessRequest(execution, isDebugEnabled)
	}


	/**
	 * Perform initial processing, such as request validation, initialization of variables, etc.
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void preProcessRequest (Execution execution, isDebugEnabled) {

		execution.setVariable("prefix",prefix)
		setSuccessIndicator(execution, false)
		execution.setVariable(prefix+'syncResponseSent', false)

		String createVolumeIncoming = validateRequest(execution, 'vnfId')
		utils.logAudit(createVolumeIncoming)

		try {
			def jsonSlurper = new JsonSlurper()
			Map reqMap = jsonSlurper.parseText(createVolumeIncoming)

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')

			def vidUtils = new VidUtils(this)
			createVolumeIncoming = vidUtils.createXmlVolumeRequest(reqMap, 'CREATE_VF_MODULE_VOL', serviceInstanceId)

			execution.setVariable(prefix+'Request', createVolumeIncoming)
			execution.setVariable(prefix+'vnfId', vnfId)
			execution.setVariable(prefix+'isVidRequest', true)

			utils.log("DEBUG", "XML request:\n" + createVolumeIncoming, isDebugEnabled)

		}
		catch(groovy.json.JsonException je) {
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 2500, 'Request is not a valid JSON document')
		}

		execution.setVariable(prefix+'source', utils.getNodeText1(createVolumeIncoming, "source"))
		execution.setVariable(prefix+'volumeGroupName', utils.getNodeText1(createVolumeIncoming, 'volume-group-name'))
		execution.setVariable(prefix+'volumeOutputs', utils.getNodeXml(createVolumeIncoming, 'volume-outputs', false))

		execution.setVariable(prefix+'serviceType', 'service-instance')
		execution.setVariable(prefix+'serviceInstanceId', utils.getNodeText1(createVolumeIncoming, "service-instance-id"))

		// Generate volume group id
		String volumeGroupId = UUID.randomUUID()
		utils.log("DEBUG", "Generated volume group id: " + volumeGroupId, isDebugEnabled)

		def testGroupId = execution.getVariable('test-volume-group-id')
		if (testGroupId != null && testGroupId.trim() != '') {
			volumeGroupId = testGroupId
		}

		execution.setVariable(prefix+'volumeGroupId', volumeGroupId)

	}


	public void sendSyncResponse (Execution execution, isDebugEnabled) {
		def volumeGroupId = execution.getVariable(prefix+'volumeGroupId')
		def requestId = execution.getVariable("att-mso-request-id")
		def serviceInstanceId = execution.getVariable("serviceInstanceId")

		String syncResponse = """{"requestReferences":{"instanceId":"${volumeGroupId}","requestId":"${requestId}"}}""".trim()

		utils.log("DEBUG", "Sync Response: " + "\n" + syncResponse, isDebugEnabled)
		sendWorkflowResponse(execution, 200, syncResponse)

		execution.setVariable(prefix+'syncResponseSent', true)
	}


	public void sendSyncError (Execution execution, isDebugEnabled) {
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
	public void buildWorkflowException(Execution execution, int errorCode, errorMessage, isDebugEnabled) {
		utils.log("DEBUG", errorMessage, isDebugEnabled)
		(new ExceptionUtil()).buildWorkflowException(execution, 2500, errorMessage)
	}


	public void prepareDbInfraSuccessRequest(Execution execution, isDebugEnabled) {
		def dbVnfOutputs = execution.getVariable(prefix+'volumeOutputs')
		def requestId = execution.getVariable('att-mso-request-id')
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
					<ns:updateInfraRequest xmlns:ns="http://com.att.mso/requestsdb">
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

		String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		execution.setVariable(prefix+"createDBRequest", buildDeleteDBRequestAsString)

		utils.logAudit(buildDeleteDBRequestAsString)
	}





	public void postProcessResponse (Execution execution, isDebugEnabled) {

		def dbReturnCode = execution.getVariable(prefix+'dbReturnCode')
		def createDBResponse =  execution.getVariable(prefix+'createDBResponse')

		utils.logAudit('DB return code: ' + dbReturnCode)
		utils.logAudit('DB response: ' + createDBResponse)

		def requestId = execution.getVariable("att-mso-request-id")
		def source = execution.getVariable(prefix+'source')

		String msoCompletionRequest =
			"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
							xmlns:ns="http://ecomp.att.com/mso/request/types/v1">
					<request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
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

	public void prepareFalloutHandlerRequest(Execution execution, isDebugEnabled) {

		WorkflowException we = execution.getVariable('WorkflowException')
		def errorCode = we?.getErrorCode()
		def errorMessage = we?.getErrorMessage()

		def requestId = execution.getVariable("att-mso-request-id")
		def source = execution.getVariable(prefix+'source')

		String falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
				                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
				                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
				   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
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
	public void callRESTQueryAAIServiceInstance(Execution execution, isDebugEnabled) {

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
}
