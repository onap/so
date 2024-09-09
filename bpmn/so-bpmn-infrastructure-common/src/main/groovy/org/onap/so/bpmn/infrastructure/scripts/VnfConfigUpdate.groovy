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
import org.onap.appc.client.lcm.model.Action
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.*
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class VnfConfigUpdate extends VnfCmBase {
    private static final Logger logger = LoggerFactory.getLogger( VnfConfigUpdate.class)

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()
	def prefix = "VnfIPU_"

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'VnfCU_')
		execution.setVariable('Request', null)
		execution.setVariable('source', null)
		execution.setVariable('controllerType', null)
		execution.setVariable('UpdateVnfSuccessIndicator', false)
		execution.setVariable('serviceType', null)
		execution.setVariable('nfRole', null)
		execution.setVariable('currentActivity', 'VnfCU')
		execution.setVariable('workStep', null)
		execution.setVariable('failedActivity', null)
		execution.setVariable('errorCode', "0")
		execution.setVariable('errorText', null)
		execution.setVariable('healthCheckIndex0', 0)
		execution.setVariable('healthCheckIndex1', 1)
		execution.setVariable('maxRetryCount', 3)
		execution.setVariable('retryCount', 0)
		execution.setVariable("lcpCloudRegionId", null)
		execution.setVariable("rollbackSetClosedLoopDisabledFlag", false)
		execution.setVariable("rollbackVnfStop", false)
		execution.setVariable("rollbackVnfLock", false)
		execution.setVariable("rollbackQuiesceTraffic", false)
		execution.setVariable("rollbackSetVnfInMaintenanceFlag", false)
	}

	/**
	 * Check for missing elements in the received request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
		'execution=' + execution.getId() +
		')'
		initProcessVariables(execution)

        logger.trace('Entered {}', method)

		initProcessVariables(execution)

		def incomingRequest = execution.getVariable('bpmnRequest')

        logger.debug("Incoming Infra Request: {}", incomingRequest)
		try {
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
            logger.debug(" Request is in JSON format.")

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')

			execution.setVariable('serviceInstanceId', serviceInstanceId)
			execution.setVariable('vnfId', vnfId)
			execution.setVariable('serviceType', 'Mobility')
			execution.setVariable('payload', "")
			execution.setVariable('actionHealthCheck', Action.HealthCheck)
			execution.setVariable('actionConfigModify', Action.ConfigModify)


			def controllerType = reqMap.requestDetails?.requestParameters?.controllerType
			execution.setVariable('controllerType', controllerType)

            logger.debug('Controller Type: {}', controllerType)

			def payload = reqMap.requestDetails?.requestParameters?.payload
			execution.setVariable('payload', payload)

            logger.debug('Processed payload: {}', payload)

			def requestId = execution.getVariable("mso-request-id")
			execution.setVariable('requestId', requestId)
			execution.setVariable('msoRequestId', requestId)

			def requestorId = reqMap.requestDetails?.requestInfo?.requestorId ?: null
			execution.setVariable('requestorId', requestorId)

			execution.setVariable('sdncVersion', '1702')

			execution.setVariable("UpdateVnfInfraSuccessIndicator", false)



			def source = reqMap.requestDetails?.requestInfo?.source
			execution.setVariable("source", source)

			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>UPDATE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			execution.setVariable("requestInfo", requestInfo)

            logger.debug('RequestInfo: {}', execution.getVariable("requestInfo"))

            logger.trace('Exited {}', method)

		}
		catch(groovy.json.JsonException je) {
            logger.debug(" Request is not in JSON format.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Invalid request format")

		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
            logger.error("{} {} Exception Encountered - \n {} \n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), restFaultMessage, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, restFaultMessage)
		}
	}

	/**
	 * Prepare and send the sychronous response for this flow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
			'execution=' + execution.getId() +
			')'

        logger.trace('Entered {}', method)


		try {
			def requestInfo = execution.getVariable('requestInfo')
			def requestId = execution.getVariable('requestId')
			def source = execution.getVariable('source')
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}

			// RESTResponse (for API Handler (APIH) Reply Task)
			def vnfId = execution.getVariable("vnfId")
			String synchResponse = """{"requestReferences":{"instanceId":"${vnfId}","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 200, synchResponse)

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}


	/**
	 * Check if this VNF is already in maintenance in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void checkIfVnfInMaintInAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.checkIfVnfInMaintInAAI(' +
			'execution=' + execution.getId() +
			')'

		execution.setVariable('errorCode', "0")
		execution.setVariable("workStep", "checkIfVnfInMaintInAAI")
		execution.setVariable("failedActivity", "AAI")
        logger.trace('Entered {}', method)

		try {
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIValidatorImpl aaiValidator = new AAIValidatorImpl()
			aaiValidator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			boolean isInMaint = aaiValidator.isVNFLocked(vnfId)
            logger.debug("isInMaint result: {}", isInMaint)
			execution.setVariable('isVnfInMaintenance', isInMaint)

			if (isInMaint) {
				execution.setVariable("errorCode", "1003")
				execution.setVariable("errorText", "VNF is in maintenance in A&AI")
			}

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in checkIfVnfInMaintInAAI(): ' + e.getMessage())
		}
	}


	/**
	 * Check if this VNF's pservers are locked in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void checkIfPserversInMaintInAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.checkIfPserversInMaintInAAI(' +
			'execution=' + execution.getId() +
			')'

		execution.setVariable('errorCode', "0")
        logger.trace('Entered {}', method)
		execution.setVariable("workStep", "checkIfPserversInMaintInAAI")
		execution.setVariable("failedActivity", "AAI")

		try {
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIValidatorImpl aaiValidator = new AAIValidatorImpl()
			aaiValidator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			boolean areLocked = aaiValidator.isPhysicalServerLocked(vnfId)
            logger.debug("areLocked result: {}", areLocked)
			execution.setVariable('arePserversLocked', areLocked)

			if (areLocked) {
				execution.setVariable("errorCode", "1003")
				execution.setVariable("errorText", "pServers are locked in A&AI")
			}

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in checkIfPserversInMaintInAAI(): ' + e.getMessage())
		}
	}

	/**
	 * Set inMaint flag for this VNF to the specified value in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 * @param inMaint The boolean value of the flag to set
	 */
	public void setVnfInMaintFlagInAAI(DelegateExecution execution, boolean inMaint) {
		def method = getClass().getSimpleName() + '.setVnfInMaintFlagInAAI(' +
			'execution=' + execution.getId() +
			')'

		execution.setVariable('errorCode', "0")
        logger.trace('Entered {}', method)
		if (inMaint) {
			execution.setVariable("workStep", "setVnfInMaintFlagInAAI")
			execution.setVariable("rollbackSetVnfInMaintenanceFlag", true)
		}
		else {
			execution.setVariable("workStep", "unsetVnfInMaintFlagInAAI")
		}
		execution.setVariable("failedActivity", "AAI")

		try {
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIUpdatorImpl aaiUpdator = new AAIUpdatorImpl()
			aaiUpdator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			if (inMaint) {
				aaiUpdator.updateVnfToLocked(vnfId)
				execution.setVariable("rollbackSetVnfInMaintenanceFlag", true)
			}
			else {
				aaiUpdator.updateVnfToUnLocked(vnfId)
			}

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
		}
	}

	/**
	 * Check if VF Closed Loop Disabled in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void checkIfClosedLoopDisabledInAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.checkIfClosedLoopDisabledInAAI(' +
			'execution=' + execution.getId() +
			')'

		execution.setVariable('errorCode', "0")
		execution.setVariable("workStep", "checkClosedLoopDisabledFlagInAAI")
		execution.setVariable("failedActivity", "AAI")
        logger.trace('Entered {}', method)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			def vnfId = execution.getVariable("vnfId")
            logger.debug("vnfId is: {}", vnfId)
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri genericVnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
			AAIResultWrapper aaiRW = client.get(genericVnfUri)
			Map<String, Object> result = aaiRW.asMap()
			boolean isClosedLoopDisabled = result.getOrDefault("is-closed-loop-disabled", false)

            logger.debug("isClosedLoopDisabled result: {}", isClosedLoopDisabled)
			execution.setVariable('isClosedLoopDisabled', isClosedLoopDisabled)

			if (isClosedLoopDisabled) {
				execution.setVariable("errorCode", "1004")
				execution.setVariable("errorText", "closedLoop is disabled in A&AI")
			}

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in checkIfVnfInMaintInAAI(): ' + e.getMessage())
		}
	}

	/**
	 * Set VF Closed Loop Disabled Flag in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void setClosedLoopDisabledInAAI(DelegateExecution execution, boolean setDisabled) {
		def method = getClass().getSimpleName() + '.setClosedLoopDisabledInAAI(' +
			'execution=' + execution.getId() +
			')'

		execution.setVariable('errorCode', "0")
		if (setDisabled) {
			execution.setVariable("workStep", "setClosedLoopDisabledFlagInAAI")
			execution.setVariable("rollbackSetClosedLoopDisabledFlag", true)
		}
		else {
			execution.setVariable("workStep", "unsetClosedLoopDisabledFlagInAAI")
		}

		execution.setVariable("failedActivity", "AAI")
        logger.trace('Entered {}', method)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			def vnfId = execution.getVariable("vnfId")
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri genericVnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))

			Map<String, Boolean> request = new HashMap<>()
			request.put("is-closed-loop-disabled", setDisabled)
			client.update(genericVnfUri, request)
            logger.debug("set isClosedLoop to: {}", setDisabled)

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in checkIfVnfInMaintInAAI(): ' + e.getMessage())
		}
	}


	/**
	 * Handle Abort disposition from RainyDayHandler
	 *
	 * @param execution The flow's execution instance.
	 */
	public void abortProcessing(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.abortProcessing(' +
			'execution=' + execution.getId() +
			')'

        logger.trace('Entered {}', method)

		def errorText = execution.getVariable("errorText")
		def errorCode = execution.getVariable("errorCode")

		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode as Integer, errorText)
	}

	/**
	 * Increment Retry Count for Current Work Step
	 *
	 * @param execution The flow's execution instance.
	 */
	public void incrementRetryCount(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.incrementRetryCount(' +
			'execution=' + execution.getId() +
			')'

        logger.trace('Entered {}', method)

		String retryCountVariableName = execution.getVariable("workStep") + "RetryCount"
		execution.setVariable("retryCountVariableName", retryCountVariableName)

		def retryCountVariable = execution.getVariable(retryCountVariableName)
		int retryCount = 0

		if (retryCountVariable != null) {
			retryCount = (int) retryCountVariable
		}

		retryCount += 1

		execution.setVariable(retryCountVariableName, retryCount)

        logger.debug("value of {} is {}", retryCountVariableName, retryCount)
        logger.trace('Exited {}', method)
    }

}
