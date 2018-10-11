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

package org.onap.so.bpmn.infrastructure.scripts
 

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.aai.AAIValidatorImpl
import org.onap.so.client.appc.ApplicationControllerClient
import org.onap.so.logger.MsoLogger

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class UpdateVfModuleInfraV2 {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, UpdateVfModuleInfraV2.class);

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	boolean preProcessRequestCheck = true;
	boolean sendSynchResponseCheck = true;
	boolean checkPserverFlagCheck = true;
	boolean vfFlagCheckSetCheck = true;
	boolean lockAppCCheck = true;
	boolean healthDiagnosticSDNOCheck = true;
	boolean healthCheckAppCCheck = true;
	boolean stopVfModuleControllerCheck = true;
	boolean healthCheckControllerCheck = true;
	boolean doUpdateVfModulePrepCheck = true;
	boolean completionHandlerPrepCheck = true;
	boolean startVfModuleControllerCheck = true;
	boolean vFFlagUnsetCheck = true;
	boolean unlockAppCCheck = true;
	boolean postUpgradeHealthCheckControllerCheck = true;



	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'UPDVfModI_')
		execution.setVariable('UPDVfModI_Request', null)
		execution.setVariable('UPDVfModI_requestInfo', null)
		execution.setVariable('UPDVfModI_requestId', null)
		execution.setVariable('UPDVfModI_source', null)
		execution.setVariable('UPDVfModI_vnfInputs', null)
		execution.setVariable('UPDVfModI_vnfId', null)
		execution.setVariable('UPDVFModI_moduleUuid', null)
		execution.setVariable('UPDVfModI_vfModuleId', null)
		execution.setVariable('UPDVfModI_tenantId', null)
		execution.setVariable('UPDVfModI_volumeGroupId', null)
		execution.setVariable('UPDVfModI_vnfParams', null)
		execution.setVariable('UPDVfModI_updateInfraRequest', null)
		execution.setVariable('UpdateVfModuleSuccessIndicator', false)
	}

	/**
	 * Check for missing elements in the received request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(DelegateExecution execution) {
		System.out.print("*****************************PreProcessRequest**************************")

		def method = getClass().getSimpleName() + '.preProcessRequest(' +
				'execution=' + execution.getId() +
				')'

		//msoLogger.trace('Entered ' + method)

		initProcessVariables(execution)

		def prefix = "UPDVfModI_"

		def incomingRequest = execution.getVariable('bpmnRequest')

		//msoLogger.debug("Incoming Infra Request: " + incomingRequest)
		try {
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			//msoLogger.debug(" Request is in JSON format.")

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')
			def moduleUuid = execution.getVariable('moduleUuid')
			execution.setVariable(prefix + 'moduleUuid',moduleUuid)
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

			//msoLogger.debug('Processed user params: ' + userParamsMap)

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

			execution.setVariable("UpdateVfModuleInfraSuccessIndicator", false)




			def source = reqMap.requestDetails?.requestInfo?.source
			execution.setVariable(prefix + "source", source)

			//For Completion Handler & Fallout Handler
			String requestInfo =
					"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>UPDATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			execution.setVariable(prefix + "requestInfo", requestInfo)

			//backoutOnFailure

			//msoLogger.debug('RequestInfo: ' + execution.getVariable(prefix + "requestInfo"))

			//msoLogger.trace('Exited ' + method)

		}
		catch(groovy.json.JsonException je) {
			//msoLogger.debug(" Request is not in JSON format.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Invalid request format")
		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			//msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Exception Encountered - " + "\n" + restFaultMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, restFaultMessage)
		}
	}

	/**
	 * Prepare and send the synchronous response for this flow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(DelegateExecution execution) {
		System.out.print("*****************************SendSynchResponse**************************")

		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
				'execution=' + execution.getId() +
				')'

		//msoLogger.trace('Entered ' + method)


		try {
			def requestInfo = execution.getVariable('UPDVfModI_requestInfo')
			def requestId = execution.getVariable('UPDVfModI_requestId')
			def source = execution.getVariable('UPDVfModI_source')
			
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}
			// RESTResponse (for API Handler (APIH) Reply Task)
			def vfModuleId = execution.getVariable("vfModuleId")
			String synchResponse = """{"requestReferences":{"instanceId":"${vfModuleId}","requestId":"${requestId}"}}""".trim()
			sendWorkflowResponse(execution, 200, synchResponse)
			//msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			//msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}

	//check to see if the Pserver Flag is locked
	public void checkPserverFlag(DelegateExecution execution) {

		System.out.println("*****************************CheckingPserverFlag*************************")
		String vnfId = (String)execution.getVariable('vnfId')
		String uuid = (String)execution.getVariable('moduleUuid')
		AAIValidatorImpl aaiVI = new AAIValidatorImpl()
		boolean flag = aaiVI.isPhysicalServerLocked(vnfId, uuid)
	}

	//check to see if the VFFlag is locked
	public void vfFlagCheck(DelegateExecution execution) {

		System.out.print("*****************************VfFlagCheck*************************")
		String vnfId = (String)execution.getVariable('vnfId')
		String uuid = (String)execution.getVariable('moduleUuid')
		AAIValidatorImpl aaiVI = new AAIValidatorImpl()
		boolean flag = aaiVI.isVNFLocked(vnfId, uuid)

	}
	//lock the VF Flag
	public void vfFlagSet(DelegateExecution execution) {

		System.out.print("*****************************VfFlagSet*************************")
		String vnfId = (String)execution.getVariable('vnfId')
		String uuid = (String)execution.getVariable('moduleUuid')
		AAIValidatorImpl aaiVI = new AAIValidatorImpl()
		aaiVI.updateVnfToLocked(vnfId,uuid);
		
	}

	//Lock AppC
	public void lockAppC(DelegateExecution execution) {

		System.out.print("*****************************lockAppC*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("Lock",vfModuleId)


	}
	//run health check
	public void healthCheckAppC(DelegateExecution execution) {

		System.out.print("*****************************healthCheckAppC*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("HealthCheck",vfModuleId)

	}
	//SDNO health diagnostic
	public void healthDiagnosticSDNO(DelegateExecution execution) {

		System.out.print("*****************************healthDiagnosticSDNO is currently ignored*************************")
		//SDNOValidatorImpl.healthDiagnostic("","");

	}
	//stop VF module controller
	public void stopVfModuleController(DelegateExecution execution) {

		System.out.print("*****************************stopVfModuleController*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("Stop",vfModuleId)


	}

	public void doUpdateVfModulePrep(DelegateExecution execution) {

		System.out.print("*****************************doUpdateVfModulePrep*************************")
		def method = getClass().getSimpleName() + '.prepDoUpdateVfModule(' +
				'execution=' + execution.getId() +
				')'

		//msoLogger.trace('Entered ' + method)

		try {

			//msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			//msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepDoUpdateVfModule(): ' + e.getMessage())

		}

	}

	public void completionHandlerPrep(DelegateExecution execution,String resultVar) {

		System.out.print("*****************************completionHandlerPrep*************************")
		def method = getClass().getSimpleName() + '.completionHandlerPrep(' +
				'execution=' + execution.getId() +
				', resultVar=' + resultVar +
				')'

		//msoLogger.trace('Entered ' + method)

		try {
			def requestInfo = getVariable(execution, 'UPDVfModI_requestInfo')

			String content = """
					<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
							xmlns:reqtype="http://org.onap/so/request/types/v1">
						${requestInfo}
						<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
					</sdncadapterworkflow:MsoCompletionRequest>
				"""

			content = utils.formatXml(content)
			//msoLogger.debug(resultVar + ' = ' + System.lineSeparator() + content)
			execution.setVariable(resultVar, content)

			//msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			//msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, 'Internal Error')

		}

	}

	public void healthCheckController(DelegateExecution execution) {

		System.out.print("*****************************healthCheckController*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("HealthCheck",vfModuleId)

	}

	public void startVfModuleController(DelegateExecution execution) {

		System.out.print("*****************************startVfModuleController*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("Start",vfModuleId)

	}

	public void vFFlagUnset(DelegateExecution execution) {

		System.out.print("*****************************vFFlagUnset*************************")
		String vnfId = (String)execution.getVariable('vnfId')
		String uuid = (String)execution.getVariable('moduleUuid')
		AAIValidatorImpl aaiVI = new AAIValidatorImpl()
		aaiVI.updateVnfToUnLocked(vnfId,uuid);


	}

	public void unlockAppC(DelegateExecution execution) {

		System.out.print("*****************************unlockAppC*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("Unlock",vfModuleId)

	}

	public void postUpgradeHealthCheckController(DelegateExecution execution) {

		System.out.print("*****************************postUpgradeHealthCheckController*************************")
		def vfModuleId = ""
		ApplicationControllerClient aCC = new ApplicationControllerClient(getLCMProperties())
		def status = aCC.runCommand("HealthCheck",vfModuleId)

	}

    Properties getLCMProperties() {
        Properties properties = new Properties()

        properties.put("topic.read", UrnPropertiesReader.getVariable("appc.client.topic.read.name"))
        properties.put("topic.read.timeout", UrnPropertiesReader.getVariable("appc.client.topic.read.timeout"))
        properties.put("client.response.timeout", UrnPropertiesReader.getVariable("appc.client.response.timeout"))
        properties.put("topic.write", UrnPropertiesReader.getVariable("appc.client.topic.write"))
        properties.put("poolMembers", UrnPropertiesReader.getVariable("appc.client.poolMembers"))
        properties.put("client.key", UrnPropertiesReader.getVariable("appc.client.key"))
        properties.put("client.secret", UrnPropertiesReader.getVariable("appc.client.secret"))
        properties.put("client.name", "MSO")
        properties.put("service", UrnPropertiesReader.getVariable("appc.client.service"))
        return properties
    }

}

