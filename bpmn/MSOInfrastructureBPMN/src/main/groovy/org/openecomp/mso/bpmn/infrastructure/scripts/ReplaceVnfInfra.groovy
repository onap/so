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
import groovy.util.XmlParser;
import groovy.xml.QName

import java.io.Serializable;
import java.util.UUID;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.cmd.AbstractSetVariableCmd
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.client.aai.*

import org.openecomp.mso.client.appc.ApplicationControllerOrchestrator
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.LockInput
import org.onap.appc.client.lcm.model.UnlockInput
import org.onap.appc.client.lcm.model.HealthCheckInput
import org.onap.appc.client.lcm.model.StartInput
import org.onap.appc.client.lcm.model.Flags
import org.onap.appc.client.lcm.model.Status



public class ReplaceVnfInfra extends VnfCmBase {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()		
	def prefix = "RPLVnfI_"

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'RPLVnfI_')
		execution.setVariable('Request', null)
		execution.setVariable('requestInfo', null)		
		execution.setVariable('source', null)
		execution.setVariable('vnfInputs', null)		
		execution.setVariable('tenantId', null)		
		execution.setVariable('vnfParams', null)
		execution.setVariable('cloudConfiguration', null)		
		execution.setVariable('ReplaceVnfSuccessIndicator', false)
		execution.setVariable('serviceType', null)
		execution.setVariable('nfRole', null)
		execution.setVariable('currentActivity', 'RPLVnfI')
		execution.setVariable('workStep', null)
		execution.setVariable('failedActivity', null)
		execution.setVariable('errorCode', "0")
		execution.setVariable('errorText', null)
		execution.setVariable('healthCheckIndex', 1)
		execution.setVariable('retainResources', true)
		execution.setVariable('productFamilyId', null)
		execution.setVariable('healthCheckIndex0', 0)
		execution.setVariable('healthCheckIndex1', 1)
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
			
			execution.setVariable('serviceInstanceId', serviceInstanceId)			
			execution.setVariable("isVidRequest", "true")
			execution.setVariable('serviceType', 'Mobility')
			execution.setVariable('retainResources', true)
			execution.setVariable('disableRollback', true)
			execution.setVariable('payload', "")
			execution.setVariable('actionLock', Action.Lock)
			execution.setVariable('actionUnlock', Action.Unlock)
			execution.setVariable('actionHealthCheck', Action.HealthCheck)
			execution.setVariable('actionStart', Action.Start)
			execution.setVariable('actionStop', Action.Stop)
			
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
			
			execution.setVariable('asdcServiceModelVersion', asdcServiceModelVersion)
			execution.setVariable('serviceModelInfo', serviceModelInfo)			
			def vnfModelInfo = jsonOutput.toJson(reqMap.requestDetails?.modelInfo)
			execution.setVariable('vnfModelInfo', vnfModelInfo)
			def vnfModelInvariantUuid = jsonUtils.getJsonValue(vnfModelInfo, "modelInvariantUuid")
			execution.setVariable('vnfModelInvariantUuid', vnfModelInvariantUuid)	
			logDebug("vnfModelInvariantUuid: " + vnfModelInvariantUuid, isDebugLogEnabled)	
			
			def vnfType = execution.getVariable('vnfType')
			execution.setVariable('vnfType', vnfType)	
			
			def userParams = reqMap.requestDetails?.requestParameters?.userParams					
			
			Map<String, String> userParamsMap = [:]
			if (userParams != null) {
				userParams.each { userParam ->
					userParamsMap.put(userParam.name, userParam.value.toString())
				}							
			}		
						
			utils.log("DEBUG", 'Processed user params: ' + userParamsMap, isDebugLogEnabled)		
			
			execution.setVariable('vfModuleInputParams', userParamsMap)			
						
			def requestId = execution.getVariable("requestId")		
			execution.setVariable('msoRequestId', requestId)
			utils.log("DEBUG", "requestId is: " + requestId, isDebugLogEnabled)
			
			def vnfName = reqMap.requestDetails?.requestInfo?.instanceName ?: null
			execution.setVariable('vnfName', vnfName)
			
			def requestorId = reqMap.requestDetails?.requestInfo?.requestorId ?: null
			execution.setVariable('requestorId', requestorId)
			
			def usePreload = reqMap.requestDetails?.requestParameters?.usePreload
			execution.setVariable('usePreload', usePreload)
			
			def productFamilyId = reqMap.requestDetails?.requestInfo?.productFamilyId ?: null
			execution.setVariable('productFamilyId', productFamilyId)
			
			def cloudConfiguration = jsonOutput.toJson(reqMap.requestDetails?.cloudConfiguration)		
			execution.setVariable('cloudConfiguration', cloudConfiguration)
			def lcpCloudRegionId	= jsonUtils.getJsonValue(cloudConfiguration, "lcpCloudRegionId")
			execution.setVariable('lcpCloudRegionId', lcpCloudRegionId)
			def tenantId = jsonUtils.getJsonValue(cloudConfiguration, "tenantId")
			execution.setVariable('tenantId', tenantId)
			
			def globalSubscriberId = reqMap.requestDetails?.subscriberInfo?.globalSubscriberId ?: ''
			execution.setVariable('globalSubscriberId', globalSubscriberId)
			
			execution.setVariable('sdncVersion', '1702')

			execution.setVariable("ReplaceVnfInfraSuccessIndicator", false)
						
			execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)			
			
			def source = reqMap.requestDetails?.requestInfo?.source
			execution.setVariable("source", source)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>REPLACE</action>
					<source>${source}</source>
				   </request-info>"""
			
			execution.setVariable("requestInfo", requestInfo)			
			
			logDebug('RequestInfo: ' + execution.getVariable("requestInfo"), isDebugLogEnabled)		
			
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
	public void sendSynchResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)


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

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
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
	public void getVnfResourceDecomposition(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.getVnfResourceDecomposition(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			String vnfModelInvariantUuid = execution.getVariable('vnfModelInvariantUuid')
			logDebug("vnfModelInvariantUuid: " + vnfModelInvariantUuid, isDebugLogEnabled)
			List<VnfResource> vnfResources = serviceDecomposition.getServiceVnfs()
			
			for (i in 0..vnfResources.size()-1) {
				ModelInfo modelInfo = vnfResources[i].getModelInfo()
				String modelInvariantUuidFromDecomposition = modelInfo.getModelInvariantUuid()
				logDebug("modelInvariantUuidFromDecomposition: " + modelInvariantUuidFromDecomposition, isDebugLogEnabled)
				
				if (vnfModelInvariantUuid.equals(modelInvariantUuidFromDecomposition)) {
					VnfResource vnfResourceDecomposition = vnfResources[i]
					execution.setVariable('vnfResourceDecomposition', vnfResourceDecomposition)
					def nfRole = vnfResourceDecomposition.getNfRole()					
					execution.setVariable('nfRole', nfRole)
					logDebug("vnfResourceDecomposition: " + vnfResourceDecomposition.toJsonString(), isDebugLogEnabled)					
					break
				}
				else {
					//exception!
				}
				
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
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
	public void checkIfVnfInMaintInAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.checkIfVnfInMaintInAAI(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable('errorCode', "0")
		execution.setVariable("workStep", "checkIfVnfInMaintInAAI")
		execution.setVariable("failedActivity", "AAI")
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIValidatorImpl aaiValidator = new AAIValidatorImpl()
			aaiValidator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			boolean isInMaint = aaiValidator.isVNFLocked(vnfId, transactionLoggingUuid)
			logDebug("isInMaint result: " + isInMaint, isDebugLogEnabled)
			execution.setVariable('isVnfInMaintenance', isInMaint)
			
			if (isInMaint) {
				execution.setVariable("errorCode", "1003")
				execution.setVariable("errorText", "VNF is in maintenance in A&AI")
			}


			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable('errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("workStep", "checkIfPserversInMaintInAAI")
		execution.setVariable("failedActivity", "AAI")

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIValidatorImpl aaiValidator = new AAIValidatorImpl()
			aaiValidator.setClient(client)
			def vnfId = execution.getVariable("vnfId")			
			boolean areLocked = aaiValidator.isPhysicalServerLocked(vnfId, transactionLoggingUuid)
			logDebug("areLocked result: " + areLocked, isDebugLogEnabled)
			execution.setVariable('arePserversLocked', areLocked)
			
			if (areLocked) {
				execution.setVariable("errorCode", "1003")
				execution.setVariable("errorText", "pServers are locked in A&AI")
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable('errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)
		if (inMaint) {
			execution.setVariable("workStep", "setVnfInMaintFlagInAAI")
		}
		else {
			execution.setVariable("workStep", "unsetVnfInMaintFlagInAAI")
		}
		execution.setVariable("failedActivity", "AAI")

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			AAIRestClientImpl client = new AAIRestClientImpl()
			AAIUpdatorImpl aaiUpdator = new AAIUpdatorImpl()
			aaiUpdator.setClient(client)
			def vnfId = execution.getVariable("vnfId")
			if (inMaint) {
				aaiUpdator.updateVnfToLocked(vnfId, transactionLoggingUuid)
				execution.setVariable("rollbackSetVnfInMaintenanceFlag", true)
			}
			else {
				aaiUpdator.updateVnfToUnLocked(vnfId, transactionLoggingUuid)
			}
							
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
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
	public void runAppcCommand(DelegateExecution execution, Action action) {
		def method = getClass().getSimpleName() + '.runAppcCommand(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable('errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)		
		
		try {
			logDebug("Running APP-C action: " + action.toString(), isDebugLogEnabled)
			String vnfId = execution.getVariable('vnfId')
			String msoRequestId = execution.getVariable('requestId')
			execution.setVariable('msoRequestId', msoRequestId)
			execution.setVariable("failedActivity", "APP-C")
			execution.setVariable("workStep", action.toString() + "VNF")
			
			ApplicationControllerOrchestrator appcClient = new ApplicationControllerOrchestrator()			
			Status appcStatus = null
			switch(action) {
				case Action.Lock:
					execution.setVariable('workStep', "LockVNF")
					appcStatus = appcClient.runCommand(Action.Lock,msoRequestId,vnfId,null)
					break
				case Action.Unlock:
					execution.setVariable('workStep', "UnlockVNF")
					appcStatus = appcClient.runCommand(Action.Unlock,msoRequestId,vnfId,null)
					break
				case Action.HealthCheck:
					def healthCheckIndex = execution.getVariable('healthCheckIndex')
					execution.setVariable('workStep', "HealthCheckVNF" + healthCheckIndex)
					execution.setVariable('healthCheckIndex', healthCheckIndex + 1)
					appcStatus = appcClient.runCommand(Action.HealthCheck,msoRequestId,vnfId,null)
					break
				case Action.Start:
					execution.setVariable('workStep', "StartVNF")
					appcStatus = appcClient.runCommand(Action.Start,msoRequestId,vnfId,null)
					break
				case Action.Stop:
					execution.setVariable('workStep', "StopVNF")
					appcStatus = appcClient.runCommand(Action.Stop,msoRequestId,vnfId,null)
					break
				default:
					break
			}
			logDebug("Completed AppC request", isDebugLogEnabled)
			int appcCode = appcStatus.getCode()
			logDebug("AppC status code is: " + appcCode, isDebugLogEnabled)
			logDebug("AppC status message is: " + appcStatus.getMessage(), isDebugLogEnabled)
			if (support.getCategoryOf(appcStatus) == ApplicationControllerSupport.StatusCategory.ERROR) {
				execution.setVariable("errorCode", Integer.toString(appcCode))
				execution.setVariable("errorText", appcStatus.getMessage())
			}
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())			
		} catch (java.lang.NoSuchMethodError e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())				
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())			
		}
	}


	
	/**
	* Prepare DoDeleteVnfAndModules call.
	*
	*
	* @param execution The flow's execution instance.
	*/
   public void prepDoDeleteVnfAndModules(DelegateExecution execution) {
	   def method = getClass().getSimpleName() + '.prepDoDeleteVnfAndModules(' +
		   'execution=' + execution.getId() +
		   ')'
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled') 
	   execution.setVariable('errorCode', "0")
	   logDebug('Entered ' + method, isDebugLogEnabled)
	   execution.setVariable("workStep", "doDeleteVnfAndModules")
	   execution.setVariable("failedActivity", "MSO Delete VNF")
	   logDebug('Exited ' + method, isDebugLogEnabled)
	   
   }
   
   /**
	* Prepare DoCreateVnfAndModules call.
	*
	*
	* @param execution The flow's execution instance.
	*/
   public void prepDoCreateVnfAndModules(DelegateExecution execution) {
	   def method = getClass().getSimpleName() + '.prepDoReplaceVnfAndModules(' +
		   'execution=' + execution.getId() +
		   ')'
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	   execution.setVariable('errorCode', "0")
	   logDebug('Entered ' + method, isDebugLogEnabled)
	   execution.setVariable("workStep", "doCreateVnfAndModules")
	   execution.setVariable("failedActivity", "MSO Create VNF")
	   logDebug('Exited ' + method, isDebugLogEnabled)
	   
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		def errorText = execution.getVariable("errorText")
		def errorCode = execution.getVariable("errorCode")
		
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode as Integer, errorText)
	}
	
	/**
	 * Handle Manual disposition from RainyDayHandler
	 *
	 * @param execution The flow's execution instance.
	 */
	public void manualProcessing(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.manualProcessing(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		def taskId = execution.getVariable("taskId")
		
		exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Processing halted - manual task created: " + taskId)
	}

	
}
