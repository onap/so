/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.scripts

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.QName

import java.beans.MetaData.java_lang_Class_PersistenceDelegate
import java.io.Serializable
import java.util.UUID
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.cmd.AbstractSetVariableCmd
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.client.aai.*

import org.openecomp.mso.client.appc.ApplicationControllerClient
import org.openecomp.mso.client.appc.ApplicationControllerSupport
import org.openecomp.appc.client.lcm.model.Action
import org.openecomp.appc.client.lcm.model.ActionIdentifiers
import org.openecomp.appc.client.lcm.model.LockInput
import org.openecomp.appc.client.lcm.model.UnlockInput
import org.openecomp.appc.client.lcm.model.HealthCheckInput
import org.openecomp.appc.client.lcm.model.StartInput
import org.openecomp.appc.client.lcm.model.StopInput
import org.openecomp.appc.client.lcm.model.Flags
import org.openecomp.appc.client.lcm.model.Status



public class UpdateVnfInfra extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()
	ApplicationControllerClient appcClient = new ApplicationControllerClient()
	def prefix = "UPDVnfI_"

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'UPDVnfI_')
		execution.setVariable('UPDVnfI_Request', null)
		execution.setVariable('UPDVnfI_requestInfo', null)
		execution.setVariable('UPDVnfI_requestId', null)
		execution.setVariable('UPDVnfI_source', null)
		execution.setVariable('UPDVnfI_vnfInputs', null)
		execution.setVariable('UPDVnfI_vnfId', null)		
		execution.setVariable('UPDVnfI_tenantId', null)		
		execution.setVariable('UPDVnfI_vnfParams', null)		
		execution.setVariable('UpdateVnfSuccessIndicator', false)
		execution.setVariable('UPDVnfI_serviceType', null)
		execution.setVariable('UPDVnfI_nfRole', null)
		execution.setVariable('UPDVnfI_currentActivity', 'UPDVnfI')
		execution.setVariable('UPDVnfI_workStep', null)
		execution.setVariable('UPDVnfI_failedActivity', null)
		execution.setVariable('UPDVnfI_errorCode', "0")
		execution.setVariable('UPDVnfI_errorText', null)
		execution.setVariable('UPDVnfI_healthCheckIndex', 1)
	}

	/**
	 * Check for missing elements in the received request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
		'execution=' + execution.getId() +
		')'
		initProcessVariables(execution)
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		initProcessVariables(execution)		

		def incomingRequest = execution.getVariable('bpmnRequest')

		utils.log("DEBUG", "Incoming Infra Request: " + incomingRequest, isDebugLogEnabled)
		try {
			def jsonSlurper = new JsonSlurper()
			def jsonOutput = new JsonOutput()
			Map reqMap = jsonSlurper.parseText(incomingRequest)
			utils.log("DEBUG", " Request is in JSON format.", isDebugLogEnabled)

			def serviceInstanceId = execution.getVariable('serviceInstanceId')
			def vnfId = execution.getVariable('vnfId')
			
			execution.setVariable(prefix + 'serviceInstanceId', serviceInstanceId)
			execution.setVariable(prefix + 'vnfId', vnfId)
			execution.setVariable("isVidRequest", "true")			
			
			def asdcServiceModelVersion = ''
			def serviceModelInfo = null
			
			def relatedInstanceList = reqMap.requestDetails?.relatedInstanceList
						
			if (relatedInstanceList != null) {
				relatedInstanceList.each {
					if (it.relatedInstance.modelInfo?.modelType == 'service') {
						utils.log("DEBUG", "PROCESSING SERVICE INFO", isDebugLogEnabled)
						asdcServiceModelVersion = it.relatedInstance.modelInfo?.modelVersion
						serviceModelInfo = jsonOutput.toJson(it.relatedInstance.modelInfo)
						utils.log("DEBUG", "ServiceModelInfo: " + serviceModelInfo, isDebugLogEnabled)
						def modelInvariant = jsonUtils.getJsonValue(serviceModelInfo, "modelInvariantUuid")
						utils.log("DEBUG", "modelInvariant: " + modelInvariant, isDebugLogEnabled)
					}
					
				}
			}		
			
			execution.setVariable(prefix + 'asdcServiceModelVersion', asdcServiceModelVersion)
			execution.setVariable(prefix + 'serviceModelInfo', serviceModelInfo)
			def vnfModelInfo = jsonOutput.toJson(reqMap.requestDetails?.modelInfo)
			execution.setVariable(prefix + 'vnfModelInfo', vnfModelInfo)
			def vnfModelInvariantUuid = jsonUtils.getJsonValue(vnfModelInfo, "modelInvariantUuid")
			execution.setVariable(prefix + 'vnfModelInvariantUuid', vnfModelInvariantUuid)	
			logDebug("vnfModelInvariantUuid: " + vnfModelInvariantUuid, isDebugLogEnabled)	
			
			def vnfType = execution.getVariable('vnfType')
			execution.setVariable(prefix + 'vnfType', vnfType)
			
			def userParams = reqMap.requestDetails?.requestParameters?.userParams					
			
			Map<String, String> userParamsMap = [:]
			if (userParams != null) {
				userParams.each { userParam ->
					userParamsMap.put(userParam.name, userParam.value)
				}							
			}		
						
			utils.log("DEBUG", 'Processed user params: ' + userParamsMap, isDebugLogEnabled)		
			
			execution.setVariable(prefix + 'vfModuleInputParams', userParamsMap)			
						
			def requestId = execution.getVariable("mso-request-id")
			execution.setVariable(prefix + 'requestId', requestId)
			execution.setVariable('msoRequestId', requestId)
			
			
			def vnfName = reqMap.requestDetails?.requestInfo?.instanceName ?: null
			execution.setVariable(prefix + 'vnfName', vnfName)
			
			def requestorId = reqMap.requestDetails?.requestInfo?.requestorId ?: null
			execution.setVariable(prefix + 'requestorId', requestorId)
			
			def usePreload = reqMap.requestDetails?.requestParameters?.usePreload
			execution.setVariable(prefix + 'usePreload', usePreload)
			
			def cloudConfiguration = reqMap.requestDetails?.cloudConfiguration
			def lcpCloudRegionId	= cloudConfiguration.lcpCloudRegionId
			execution.setVariable(prefix + 'lcpCloudRegionId', lcpCloudRegionId)
			def tenantId = cloudConfiguration.tenantId
			execution.setVariable(prefix + 'tenantId', tenantId)
			
			def globalSubscriberId = reqMap.requestDetails?.subscriberInfo?.globalSubscriberId ?: ''
			execution.setVariable(prefix + 'globalSubscriberId', globalSubscriberId)
			
			execution.setVariable(prefix + 'sdncVersion', '1702')

			execution.setVariable("UpdateVnfInfraSuccessIndicator", false)
						
			execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)			
			
			def source = reqMap.requestDetails?.requestInfo?.source
			execution.setVariable(prefix + "source", source)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>UPDATE</action>
					<source>${source}</source>
				   </request-info>"""
			
			execution.setVariable(prefix + "requestInfo", requestInfo)			
			
			logDebug('RequestInfo: ' + execution.getVariable(prefix + "requestInfo"), isDebugLogEnabled)		
			
			logDebug('Exited ' + method, isDebugLogEnabled)

		}
		catch(groovy.json.JsonException je) {
			utils.log("DEBUG", " Request is not in JSON format.", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Invalid request format")

		}
		catch(Exception e) {
			String restFaultMessage = e.getMessage()
			utils.log("ERROR", " Exception Encountered - " + "\n" + restFaultMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, restFaultMessage)
		}	
	}

	/**
	 * Prepare and send the sychronous response for this flow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(Execution execution) {
		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)


		try {
			def requestInfo = execution.getVariable('UPDVnfI_requestInfo')
			def requestId = execution.getVariable('UPDVnfI_requestId')
			def source = execution.getVariable('UPDVnfI_source')
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

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendResponse(): ' + e.getMessage())
		}
	}
	
	

	/**
	 * Get VnfResource decomposition object for this VNF.
	 *	
	 *
	 * @param execution The flow's execution instance.
	 */
	public void getVnfResourceDecomposition(Execution execution) {
		def method = getClass().getSimpleName() + '.getVnfResourceDecomposition(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			String vnfModelInvariantUuid = execution.getVariable(prefix + 'vnfModelInvariantUuid')
			logDebug("vnfModelInvariantUuid: " + vnfModelInvariantUuid, isDebugLogEnabled)
			List<VnfResource> vnfResources = serviceDecomposition.getServiceVnfs()
			
			for (i in 0..vnfResources.size()-1) {
				ModelInfo modelInfo = vnfResources[i].getModelInfo()
				String modelInvariantUuidFromDecomposition = modelInfo.getModelInvariantUuid()
				logDebug("modelInvariantUuidFromDecomposition: " + modelInvariantUuidFromDecomposition, isDebugLogEnabled)
				
				if (vnfModelInvariantUuid.equals(modelInvariantUuidFromDecomposition)) {
					VnfResource vnfResourceDecomposition = vnfResources[i]
					execution.setVariable(prefix + 'vnfResourceDecomposition', vnfResourceDecomposition)
					def nfRole = vnfResourceDecomposition.getNfRole()					
					execution.setVariable(prefix + 'nfRole', nfRole)
					logDebug("vnfResourceDecomposition: " + vnfResourceDecomposition.toJsonString(), isDebugLogEnabled)					
					break
				}
				else {
					//exception!
				}
				
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in getVnfResourceDecomposition(): ' + e.getMessage())
		}
	}
	
	/**
	 * Check if this VNF is already in maintenance in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void checkIfVnfInMaintInAAI(Execution execution) {
		def method = getClass().getSimpleName() + '.checkIfVnfInMaintInAAI(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable(prefix + 'errorCode', "0")
		execution.setVariable(prefix + "workStep", "checkIfVnfInMaintInAAI")
		execution.setVariable(prefix + "failedActivity", "AAI")
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIValidatorImpl aaiValidator = new AAIValidatorImpl()
			aaiValidator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			boolean isInMaint = aaiValidator.isVNFLocked(vnfId, transactionLoggingUuid)
			logDebug("isInMaint result: " + isInMaint, isDebugLogEnabled)
			execution.setVariable(prefix + 'isVnfInMaintenance', isInMaint)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)			
			execution.setVariable(prefix + "errorCode", "1002")
			execution.setVariable(prefix + "errorText", e.getMessage())
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in checkIfVnfInMaintInAAI(): ' + e.getMessage())
		}
	}
	
	
	/**
	 * Check if this VNF's pservers are locked in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void checkIfPserversInMaintInAAI(Execution execution) {
		def method = getClass().getSimpleName() + '.checkIfPserversInMaintInAAI(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable(prefix + 'errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable(prefix + "workStep", "checkIfPserversInMaintInAAI")
		execution.setVariable(prefix + "failedActivity", "AAI")

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIValidatorImpl aaiValidator = new AAIValidatorImpl()
			aaiValidator.setClient(client)
			def vnfId = execution.getVariable("vnfId")			
			boolean areLocked = aaiValidator.isPhysicalServerLocked(vnfId, transactionLoggingUuid)
			logDebug("areLocked result: " + areLocked, isDebugLogEnabled)
			execution.setVariable(prefix + 'arePserversLocked', areLocked)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable(prefix + "errorCode", "1002")
			execution.setVariable(prefix + "errorText", e.getMessage())
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
	public void setVnfInMaintFlagInAAI(Execution execution, boolean inMaint) {
		def method = getClass().getSimpleName() + '.setVnfInMaintFlagInAAI(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable(prefix + 'errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)
		if (inMaint) {
			execution.setVariable(prefix + "workStep", "setVnfInMaintFlagInAAI")
		}
		else {
			execution.setVariable(prefix + "workStep", "unsetVnfInMaintFlagInAAI")
		}
		execution.setVariable(prefix + "failedActivity", "AAI")

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIUpdatorImpl aaiUpdator = new AAIUpdatorImpl()
			aaiUpdator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			if (inMaint) {
				aaiUpdator.updateVnfToLocked(vnfId, transactionLoggingUuid)
			}
			else {
				aaiUpdator.updateVnfToUnLocked(vnfId, transactionLoggingUuid)
			}
							
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable(prefix + "errorCode", "1002")
			execution.setVariable(prefix + "errorText", e.getMessage())
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in setVnfInMaintFlagInAAI(): ' + e.getMessage())
		}
	}
	
	/**
	 * Call APP-C client to execute specified APP-C command for this VNF.
	 *
	 *
	 * @param execution The flow's execution instance.
	 * @param action The action to take in APP-C.
	 */
	public void runAppcCommand(Execution execution, Action action) {
		def method = getClass().getSimpleName() + '.runAppcCommand(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable(prefix + 'errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		try {
			logDebug("Running APP-C action: " + action.toString(), isDebugLogEnabled)
			String vnfId = execution.getVariable('vnfId')
			String msoRequestId = execution.getVariable(prefix + 'requestId')
			execution.setVariable('msoRequestId', msoRequestId)			
			execution.setVariable(prefix + "failedActivity", "APP-C")			
				
			ApplicationControllerSupport support = new ApplicationControllerSupport()			
			appcClient.appCSupport=support			
			org.springframework.test.util.ReflectionTestUtils.setField(support, "lcmModelPackage", "org.openecomp.appc.client.lcm.model")
			Flags flags = new Flags()
			ActionIdentifiers actionIdentifiers = new ActionIdentifiers()
			actionIdentifiers.setVnfId(vnfId)
			Status appcStatus
			switch(action) {
				case Action.Lock:
					execution.setVariable(prefix + 'workStep', "LockVNF")
					appcStatus = appcClient.runCommand(Action.Lock,actionIdentifiers,flags,null,msoRequestId)					
					break
				case Action.Unlock:
					execution.setVariable(prefix + 'workStep', "UnlockVNF")
					appcStatus = appcClient.runCommand(Action.Unlock,actionIdentifiers,flags,null,msoRequestId)					
					break
				case Action.HealthCheck:
					def healthCheckIndex = execution.getVariable(prefix + 'healthCheckIndex')
					execution.setVariable(prefix + 'workStep', "HealthCheckVNF" + healthCheckIndex)
					execution.setVariable(prefix + 'healthCheckIndex', healthCheckIndex + 1)
					appcStatus = appcClient.runCommand(Action.HealthCheck,actionIdentifiers,flags,null,msoRequestId)					
					break
				case Action.Start:
					execution.setVariable(prefix + 'workStep', "StartVNF")
					appcStatus = appcClient.runCommand(Action.Start,actionIdentifiers,flags,null,msoRequestId)					
					break
				case Action.Stop:
					execution.setVariable(prefix + 'workStep', "StopVNF")
					appcStatus = appcClient.runCommand(Action.Stop,actionIdentifiers,flags,null,msoRequestId)					
					break
				default:
					break
			}
			logDebug("Completed AppC request", isDebugLogEnabled)			
			int appcCode = appcStatus.getCode()
			logDebug("AppC status code is: " + appcCode, isDebugLogEnabled)
			logDebug("AppC status message is: " + appcStatus.getMessage(), isDebugLogEnabled)
			if (support.getCategoryOf(appcStatus) == ApplicationControllerSupport.StatusCategory.ERROR) {
				execution.setVariable(prefix + "errorCode", Integer.toString(appcCode))
				execution.setVariable(prefix + "errorText", appcStatus.getMessage())				
			}		
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable(prefix + "errorCode", "1002")
			execution.setVariable(prefix + "errorText", e.getMessage())
			//throw e;
		} catch (java.lang.NoSuchMethodError e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable(prefix + "errorCode", "1002")
			execution.setVariable(prefix + "errorText", e.getMessage())
			//throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable(prefix + "errorCode", "1002")
			execution.setVariable(prefix + "errorText", e.getMessage())
			
			//exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in ' + method + ': ' + e.getMessage())
		}
	}



	

	/**
	 * Builds a "CompletionHandler" request and stores it in the specified execution variable.
	 *
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void completionHandlerPrep(Execution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.completionHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)		
		

		try {
			appcClient.shutdownclient()
			def requestInfo = getVariable(execution, 'UPDVnfI_requestInfo')

			String content = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1">
					${requestInfo}
					<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
			"""

			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, 'Internal Error')
		}
	}
	
	/**
	* Prepare DoUpdateVnfAndModules call.
	*
	*
	* @param execution The flow's execution instance.
	*/
   public void prepDoUpdateVnfAndModules(Execution execution) {
	   def method = getClass().getSimpleName() + '.prepDoUpdateVnfAndModules(' +
		   'execution=' + execution.getId() +
		   ')'
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	   execution.setVariable(prefix + 'errorCode', "0")
	   logDebug('Entered ' + method, isDebugLogEnabled)
	   execution.setVariable(prefix + "workStep", "doUpdateVnfAndModules")
	   execution.setVariable(prefix + "failedActivity", "MSO Update VNF")
	   logDebug('Exited ' + method, isDebugLogEnabled)
	   
   }
	
	/**
	 * Builds a "FalloutHandler" request and stores it in the specified execution variable.
	 *
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void falloutHandlerPrep(Execution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.falloutHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def prefix = execution.getVariable('prefix')
			def request = getVariable(execution, prefix+'Request')
			def requestInformation = execution.getVariable(prefix + "requestInfo")		
			
			appcClient.shutdownclient()

			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			}

			String content = """
				<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1"
						xmlns:msoservtypes="http://org.openecomp/mso/request/types/v1"
						xmlns:structuredtypes="http://org.openecomp/mso/structured/types/v1">
					${requestInformation}
					<sdncadapterworkflow:WorkflowException>
						<sdncadapterworkflow:ErrorMessage>${encErrorResponseMsg}</sdncadapterworkflow:ErrorMessage>
						<sdncadapterworkflow:ErrorCode>${errorResponseCode}</sdncadapterworkflow:ErrorCode>
					</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
			"""
			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildWorkflowException(execution, 2000, 'Internal Error')
		}
	}
	
	/**
	 * Handle Abort disposition from RainyDayHandler
	 *
	 * @param execution The flow's execution instance.	 
	 */
	public void abortProcessing(Execution execution) {
		def method = getClass().getSimpleName() + '.abortProcessing(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		def errorText = execution.getVariable(prefix + "errorText")
		def errorCode = execution.getVariable(prefix + "errorCode")
		
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode as Integer, errorText)
	}
	
	/**
	 * Handle Manual disposition from RainyDayHandler
	 *
	 * @param execution The flow's execution instance. 
	 */
	public void manualProcessing(Execution execution) {
		def method = getClass().getSimpleName() + '.manualProcessing(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		def taskId = execution.getVariable("UPDVnfI_taskId")		
		
		exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Processing halted - manual task created: " + taskId)
	}

	
}
