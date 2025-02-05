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

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.v12.GenericVnf;
import org.onap.appc.client.lcm.model.Action
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils;
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.aai.AAICreateResources
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.*
import javax.xml.parsers.*
import org.xml.sax.InputSource
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class CreateVfModuleInfra extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( CreateVfModuleInfra.class);

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	private AbstractServiceTaskProcessor taskProcessor
	
	public SDNCAdapterUtils(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}	

	/**
	 * Validates the request message and sets up the workflow.
	 * @param execution the execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'

		logger.debug('Started ' + method)
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		
		execution.setVariable("CVFMI_sentSyncResponse", false)
		
		def prefix = 'CVFMI_'
		execution.setVariable('prefix', prefix)
		execution.setVariable("isVidRequest", "false")

		def rollbackData = execution.getVariable("RollbackData")
		if (rollbackData == null) {
			rollbackData = new RollbackData()
		}
		execution.setVariable("RollbackData", rollbackData)
		
		def incomingRequest = execution.getVariable('bpmnRequest')
		logger.debug("Incoming Infra Request: " + incomingRequest)
		logger.debug("CreateVfModule Infra incoming Request: " + incomingRequest)

		setBasicDBAuthHeader(execution, isDebugLogEnabled)
		
		// check if request is xml or json
		try {
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			logger.debug(" Request is in JSON format.")

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')
			
			execution.setVariable(prefix + 'serviceInstanceId', serviceInstanceId)
			execution.setVariable(prefix+'vnfId', vnfId)
			execution.setVariable("isVidRequest", "true")
			
			def vnfName = ''
			def asdcServiceModelVersion = ''
			def serviceModelInfo = null
			def vnfModelInfo = null
			
			def relatedInstanceList = reqMap.requestDetails?.relatedInstanceList
						
			if (relatedInstanceList != null) {
				relatedInstanceList.each {
					if (it.relatedInstance.modelInfo?.modelType == 'service') {
						asdcServiceModelVersion = it.relatedInstance.modelInfo?.modelVersion
						serviceModelInfo = jsonOutput.toJson(it.relatedInstance.modelInfo)
						
					}
					if (it.relatedInstance.modelInfo.modelType == 'vnf') {
						vnfName = it.relatedInstance.instanceName ?: ''
						vnfModelInfo = jsonOutput.toJson(it.relatedInstance.modelInfo)
					}
				}
			}
			
			execution.setVariable(prefix + 'vnfName', vnfName)
			execution.setVariable(prefix + 'asdcServiceModelVersion', asdcServiceModelVersion)
			execution.setVariable(prefix + 'serviceModelInfo', serviceModelInfo)
			execution.setVariable(prefix + 'vnfModelInfo', vnfModelInfo)
			
			
			def vnfType = execution.getVariable('vnfType')
			execution.setVariable(prefix + 'vnfType', vnfType)	
			def vfModuleId = execution.getVariable('vfModuleId')
			execution.setVariable(prefix + 'vfModuleId', vfModuleId)
			def volumeGroupId = execution.getVariable('volumeGroupId')
			execution.setVariable(prefix + 'volumeGroupId', volumeGroupId)
			def userParams = reqMap.requestDetails?.requestParameters?.userParams					
			
			Map<String, String> userParamsMap = [:]
			if (userParams != null) {
				userParams.each { userParam ->
					userParamsMap.put(userParam.name, userParam.value.toString())
				}							
			}		
						
			logger.debug('Processed user params: ' + userParamsMap)		
			
			execution.setVariable(prefix + 'vfModuleInputParams', userParamsMap)
			
			def isBaseVfModule = "false"
			if (execution.getVariable('isBaseVfModule') == true) {
				isBaseVfModule = "true"
			}			
			
			execution.setVariable(prefix + 'isBaseVfModule', isBaseVfModule)
						
			def requestId = execution.getVariable("mso-request-id")
			execution.setVariable(prefix + 'requestId', requestId)
			
			def vfModuleModelInfo = jsonOutput.toJson(reqMap.requestDetails?.modelInfo)
			execution.setVariable(prefix + 'vfModuleModelInfo', vfModuleModelInfo)
			
			def suppressRollback = reqMap.requestDetails?.requestInfo?.suppressRollback
			
			
			def backoutOnFailure = ""
			if(suppressRollback != null){
				if ( suppressRollback == true) {
					backoutOnFailure = "false"
				} else if ( suppressRollback == false) {
					backoutOnFailure = "true"
				}
			}
			
			execution.setVariable('disableRollback', suppressRollback)
			
			def vfModuleName = reqMap.requestDetails?.requestInfo?.instanceName ?: null
			execution.setVariable(prefix + 'vfModuleName', vfModuleName)
			
			def serviceId = reqMap.requestDetails?.requestParameters?.serviceId ?: ''
			execution.setVariable(prefix + 'serviceId', serviceId)
			
			def usePreload = reqMap.requestDetails?.requestParameters?.usePreload
			execution.setVariable(prefix + 'usePreload', usePreload)
			
			// This is aLaCarte flow, so aLaCarte flag is always on				
			execution.setVariable(prefix + 'aLaCarte', true)
			
			def cloudConfiguration = reqMap.requestDetails?.cloudConfiguration
			def lcpCloudRegionId	= cloudConfiguration.lcpCloudRegionId
			execution.setVariable(prefix + 'lcpCloudRegionId', lcpCloudRegionId)
			
			def cloudOwner	= cloudConfiguration.cloudOwner
			execution.setVariable(prefix + 'cloudOwner', cloudOwner)
			
			def tenantId = cloudConfiguration.tenantId
			execution.setVariable(prefix + 'tenantId', tenantId)
			
			def globalSubscriberId = reqMap.requestDetails?.subscriberInfo?.globalSubscriberId ?: ''
			execution.setVariable(prefix + 'globalSubscriberId', globalSubscriberId)
			
			execution.setVariable(prefix + 'sdncVersion', '1702')

			execution.setVariable("CreateVfModuleInfraSuccessIndicator", false)
			execution.setVariable("RollbackCompleted", false)
			
			execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
			
			
			def source = reqMap.requestDetails?.requestInfo?.source
			execution.setVariable("CVFMI_source", source)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>CREATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""
			
			execution.setVariable("CVFMI_requestInfo", requestInfo)
			
			//backoutOnFailure
			
			execution.setVariable("CVFMI_originalWorkflowException", null)
			

			def newVfModuleId = UUID.randomUUID().toString()
			execution.setVariable("newVfModuleId", newVfModuleId)
			execution.setVariable(prefix + 'vfModuleId', newVfModuleId)
			execution.setVariable('actionHealthCheck', Action.HealthCheck)
			execution.setVariable('actionConfigScaleOut', Action.ConfigScaleOut)
			execution.setVariable('controllerType', "APPC")
			def controllerType = execution.getVariable('controllerType')
			execution.setVariable(prefix + 'controllerType', controllerType)
			execution.setVariable('healthCheckIndex0', 0)

			logger.debug('RequestInfo: ' + execution.getVariable("CVFMI_requestInfo"))			
			
			logger.debug('rollbackEnabled: ' + execution.getVariable("CVFMI_rollbackEnabled"))

			logger.trace('Finished ' + method)
		} catch (BpmnError bpmnError) {
			throw bpmnError
		}
		catch(groovy.json.JsonException je) {
			logger.debug("Request is not in JSON format.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - During PreProcessRequest")
		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			//execution.setVariable("CVFMODVOL2_RESTFault", restFaultMessage)
			//execution.setVariable("CVFMODVOL2_isDataOk", false)
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					" Exception Encountered - " + "\n" + restFaultMessage, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Internal Error - During PreProcessRequest")
		}

	}

	/**
	 * Validates a workflow response.
	 * @param execution the execution
	 * @param responseVar the execution variable in which the response is stored
	 * @param responseCodeVar the execution variable in which the response code is stored
	 * @param errorResponseVar the execution variable in which the error response is stored
	 */
	public void validateWorkflowResponse(DelegateExecution execution, String responseVar,
			String responseCodeVar, String errorResponseVar) {
		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
		sdncAdapterUtils.validateSDNCResponse(execution, responseVar, responseCodeVar, errorResponseVar)
	}


	/**
	 * Sends the empty, synchronous response back to the API Handler.
	 * @param execution the execution
	 */
	public void sendResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendResponse(' +
			'execution=' + execution.getId() +
			')'
		
		logger.trace('Started ' + method)

		try {
			def requestInfo = execution.getVariable('CVFMI_requestInfo')
			def requestId = execution.getVariable('CVFMI_requestId')
			def source = execution.getVariable('CVFMI_source')			
			
			// RESTResponse (for API Handler (APIH) Reply Task)
			def newVfModuleId = execution.getVariable("newVfModuleId")
			String synchResponse = """{"requestReferences":{"instanceId":"${newVfModuleId}","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 200, synchResponse)

			execution.setVariable("CVFMI_sentSyncResponse", true)
			logger.debug("CreateVfModule Infra Response: " + synchResponse)
			logger.trace('Finished ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Encountered ", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}

	/**
	 * Query AAI for vnf orchestration status to determine if health check and config scaling should be run
	 */
	public void queryAAIForVnfOrchestrationStatus(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		def vnfId = execution.getVariable("CVFMI_vnfId")
		execution.setVariable("runHealthCheck", false);
		execution.setVariable("runConfigScaleOut", false);
		AAICreateResources aaiCreateResources = new AAICreateResources();
		Optional<GenericVnf> vnf = aaiCreateResources.getVnfInstance(vnfId);
		if(vnf.isPresent()){
			def vnfOrchestrationStatus = vnf.get().getOrchestrationStatus();
			if("active".equalsIgnoreCase(vnfOrchestrationStatus)){
				execution.setVariable("runHealthCheck", false);
				execution.setVariable("runConfigScaleOut", true);
			}
		}
	}
	
	/**
	 * Retrieve data for ConfigScaleOut from SDNC topology
	 */
	
	public void retreiveConfigScaleOutData(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		def vfModuleId = execution.getVariable("CVFMI_vfModuleId")
		String ipAddress = "";
		String oamIpAddress = "";
		String vnfHostIpAddress = "";

		String vnfGetSDNC = execution.getVariable("DCVFM_getSDNCAdapterResponse");

		String data = utils.getNodeXml(vnfGetSDNC, "response-data")
		data = data.replaceAll("&lt;", "<")
		data = data.replaceAll("&gt;", ">")

		InputSource source = new InputSource(new StringReader(data));
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true)
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
		Document responseXml = docBuilder.parse(source)

		NodeList paramsList = responseXml.getElementsByTagNameNS("*", "vnf-parameters")
		for (int z = 0; z < paramsList.getLength(); z++) {
			Node node = paramsList.item(z)
			Element eElement = (Element) node
			String vnfParameterName = utils.getElementText(eElement, "vnf-parameter-name")
			String vnfParameterValue = utils.getElementText(eElement, "vnf-parameter-value")
			if (vnfParameterName.equals("vlb_private_ip_1")) {
				vnfHostIpAddress = vnfParameterValue
			}
			else if (vnfParameterName.equals("vdns_private_ip_0")) {
				ipAddress = vnfParameterValue
			}
			else if (vnfParameterName.equals("vdns_private_ip_1")) {			
				oamIpAddress = vnfParameterValue
			}
		}

		String payload = "{\"request-parameters\":{\"vnf-host-ip-address\":\"" + vnfHostIpAddress + "\",\"vf-module-id\":\"" + vfModuleId + "\"},\"configuration-parameters\":{\"ip-addr\":\"" + ipAddress +"\", \"oam-ip-addr\":\""+ oamIpAddress +"\",\"enabled\":\"true\"}}"
		execution.setVariable("payload", payload);
	}

	/**
	 *
	 * @param execution the execution
	 */
	public void postProcessResponse(DelegateExecution execution){
		logger.trace("STARTED PostProcessResponse Process")
		try{			
			def requestInfo = execution.getVariable("CVFMI_requestInfo")
			def action = utils.getNodeText(requestInfo, "action")

			logger.debug("requestInfo is: " + requestInfo)
			logger.debug("action is: " + action)

			String payload =
					"""  <aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                               xmlns:ns="http://org.onap/so/request/types/v1"
                               xmlns:ns8="http://org.onap/so/workflow/schema/v1">
			<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
			${requestInfo}
			</request-info>
			<ns8:status-message>Vf Module has been created successfully.</ns8:status-message>
			<ns8:mso-bpel-name>BPMN</ns8:mso-bpel-name>
			</aetgt:MsoCompletionRequest>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_SuccessFlag", true)
			execution.setVariable("CVFMI_msoCompletionRequest", payload)
			logger.debug("CreateVfModuleInfra completion request: " + payload)
			logger.debug("Outgoing MsoCompletionRequest: \n" + payload)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing PostProcessResponse - " + "\n", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			execution.setVariable("CVFMI_ErrorResponse", "Error Occured during PostProcessResponse Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED PostProcessResponse Process")
	}





	/**
	 * Validates the request, request id and service instance id.  If a problem is found,
	 * a WorkflowException is generated and an MSOWorkflowException event is thrown. This
	 * method also sets up the log context for the workflow.
	 * @param execution the execution
	 * @return the validated request
	 */
	public String validateInfraRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateInfraRequest(' +
			'execution=' + execution.getId() +
			')'
		
		logger.trace('Started ' + method)

		String processKey = getProcessKey(execution);
		def prefix = execution.getVariable("prefix")

		if (prefix == null) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " prefix is null")
		}

		try {
			def request = execution.getVariable(prefix + 'Request')

			if (request == null) {
				request = execution.getVariable(processKey + 'Request')

				if (request == null) {
					request = execution.getVariable('bpmnRequest')
				}

				setVariable(execution, processKey + 'Request', null);
				setVariable(execution, 'bpmnRequest', null);
				setVariable(execution, prefix + 'Request', request);
			}

			if (request == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request is null")
			}

			/*

			def requestId = execution.getVariable("mso-request-id")

			if (requestId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request has no mso-request-id")
			}

			setVariable(execution, prefix + 'requestId', requestId)

			def serviceInstanceId = execution.getVariable("mso-service-instance-id")

			if (serviceInstanceId == null) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, processKey + " request message has no mso-service-instance-id")
			}

			utils.logContext(requestId, serviceInstanceId)
			*/
			logger.debug("CreateVfModule incoming request: " + request)
			logger.debug('Incoming message: ' + System.lineSeparator() + request)
			logger.trace('Finished ' + method)
			return request
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Caught exception in " + method , "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Invalid Message")
		}
	}

	public void prepareUpdateInfraRequest(DelegateExecution execution){
		logger.trace("STARTED prepareUpdateInfraRequest Process")
		try{
			
			String requestInfo = execution.getVariable("CVFMI_requestInfo")			
			def aicCloudRegion	= execution.getVariable("CVFMI_lcpCloudRegionId")
			def tenantId = execution.getVariable("CVFMI_tenantId")
			def requestId = utils.getNodeText(requestInfo, "request-id")
			def vnfId = execution.getVariable("CVFMI_vnfId")
			def vfModuleId = execution.getVariable("CVFMI_vfModuleId")
			// vfModuleName may be generated by DoCreateVfModule subprocess if it is not specified on the input
			def vfModuleName = execution.getVariable("CVFMI_vfModuleName")

			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint",execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

			String payload =
				"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
						xmlns:ns="http://org.onap.so/requestsdb">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
							<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
							<lastModifiedBy>BPMN</lastModifiedBy>
							<statusMessage>VF Module successfully created</statusMessage>
							<responseBody></responseBody>
							<requestStatus>COMPLETE</requestStatus>
							<progress>100</progress>
							<vnfOutputs>&lt;vnf-outputs xmlns="http://org.onap/so/infra/vnf-request/v1" xmlns:aetgt="http://org.onap/so/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"&gt;&lt;vnf-id&gt;${MsoUtils.xmlEscape(vnfId)}&lt;/vnf-id&gt;&lt;vf-module-id&gt;${MsoUtils.xmlEscape(vfModuleId)}&lt;/vf-module-id&gt;&lt;/vnf-outputs&gt;</vnfOutputs>
							<vfModuleId>${MsoUtils.xmlEscape(vfModuleId)}</vfModuleId>
							<vfModuleName>${MsoUtils.xmlEscape(vfModuleName)}</vfModuleName>
						</ns:updateInfraRequest>
					</soapenv:Body>
				</soapenv:Envelope>"""

			payload = utils.formatXml(payload)
			execution.setVariable("CVFMI_updateInfraRequest", payload)
			logger.debug("Outgoing UpdateInfraRequest: \n" + payload)
			logger.debug("CreateVfModuleInfra Outgoing UpdateInfra Request: " + payload)

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing prepareUpdateInfraRequest.", "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareUpdateInfraRequest Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED prepareUpdateInfraRequest Process")
	}

	/**
	 * Builds a "FalloutHandler" request and stores it in the specified execution variable.
	 *
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void falloutHandlerPrep(DelegateExecution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.falloutHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		
		logger.trace("Started " + method)


		try {
			def WorkflowException workflowException = execution.getVariable("WorkflowException")			
			def requestInformation = execution.getVariable("CVFMI_requestInfo")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg
			}

			String content = """
				<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
						xmlns:reqtype="http://org.onap/so/request/types/v1"
						xmlns:msoservtypes="http://org.onap/so/request/types/v1"
						xmlns:structuredtypes="http://org.onap/so/structured/types/v1">
						${requestInformation}
					<aetgt:WorkflowException>
						<aetgt:ErrorMessage>${MsoUtils.xmlEscape(encErrorResponseMsg)}</aetgt:ErrorMessage>
						<aetgt:ErrorCode>${MsoUtils.xmlEscape(errorResponseCode)}</aetgt:ErrorCode>
					</aetgt:WorkflowException>
				</aetgt:FalloutHandlerRequest>
			"""

			logger.debug("CONTENT before translation: " + content)
			content = utils.formatXml(content)
			logger.debug(resultVar + ' = ' + System.lineSeparator() + content)
			logger.debug("CreateVfModuleInfra FallOutHander Request: " + content)
			execution.setVariable(resultVar, content)

			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Caught exception in " + method , "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildWorkflowException(execution, 2000, 'Internal Error')
		}
	}

	public void logAndSaveOriginalException(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateRollbackResponse(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		logWorkflowException(execution, 'CreateVfModuleInfra caught an event')
		saveWorkflowException(execution, 'CVFMI_originalWorkflowException')
	}

	public void validateRollbackResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.validateRollbackResponse(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)
		def originalException = execution.getVariable("CVFMI_originalWorkflowException")
		execution.setVariable("WorkflowException", originalException)

		execution.setVariable("RollbackCompleted", true)

	}
	
	public void sendErrorResponse(DelegateExecution execution){
		logger.trace("STARTED CreateVfModulenfra sendErrorResponse Process")
		try {
			def sentSyncResponse = execution.getVariable("CVFMI_sentSyncResponse")
			if(sentSyncResponse == false){
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)
				logger.debug(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				logger.debug("Not Sending Error Response.  Sync Response Already Sent")
			}

		} catch (Exception ex) {
			logger.debug("Error Occured in CreateVfModuleInfra sendErrorResponse Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVfModuleInfra sendErrorResponse Process")
		}
		logger.trace("COMPLETED CreateVfModuleInfra sendErrorResponse Process")
	}


}
