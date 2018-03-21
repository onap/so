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

import org.json.JSONArray
import org.json.JSONObject

import java.io.Serializable;
import java.util.List
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
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.client.aai.*

import org.openecomp.mso.client.appc.ApplicationControllerClient;
import org.openecomp.mso.client.appc.ApplicationControllerSupport;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper
import org.openecomp.mso.client.aai.entities.Relationships
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri
import org.openecomp.mso.client.aai.entities.uri.AAIUri
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.ActionIdentifiers;
import org.onap.appc.client.lcm.model.LockInput
import org.onap.appc.client.lcm.model.UnlockInput
import org.onap.appc.client.lcm.model.HealthCheckInput
import org.onap.appc.client.lcm.model.StartInput
import org.onap.appc.client.lcm.model.StopInput
import org.onap.appc.client.lcm.model.Flags
import org.onap.appc.client.lcm.model.Status



public abstract class VnfCmBase extends AbstractServiceTaskProcessor {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtils = new JsonUtils()	
	def prefix = "VnfIPU_"

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	
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
	 * Get VNF info from A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIForVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.queryAAIForVnf(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			def vnfId = execution.getVariable("vnfId")
			logDebug("vnfId is: " + vnfId, isDebugLogEnabled)
			def cloudRegionId = execution.getVariable("lcpCloudRegionId")
			logDebug("cloudRegionId is: " + cloudRegionId, isDebugLogEnabled)
			
			AAIResourcesClient client = new AAIResourcesClient()
			
			AAIUri genericVnfUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
			// Check if this VNF exists
			if (!client.exists(genericVnfUri)) {
				logDebug("VNF with vnfId " + vnfId + " does not exist in A&AI", isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 404, "VNF with vnfId " + vnfId + " does not exist in A&AI")
			}
			
			AAIResultWrapper aaiRW = client.get(genericVnfUri)
			
			Map<String, Object> result = aaiRW.asMap()
			
			String vnfName = result.get("vnf-name")
			logDebug("vnfName from A&AI is: " + vnfName, isDebugLogEnabled)
			execution.setVariable("vnfName", vnfName)
			String nfRole = result.get("nf-role")
			logDebug("nfRole from A&AI is: " + nfRole, isDebugLogEnabled)
			execution.setVariable("nfRole", nfRole)
			String vnfHostIpAddress = result.get("ipv4-oam-address")
			logDebug("vnfHostIpAddress from A&AI is: " + vnfHostIpAddress, isDebugLogEnabled)
			execution.setVariable("vnfHostIpAddress", vnfHostIpAddress)
			execution.setVariable("vmIdList", null)
			if (aaiRW.getRelationships() != null) {
				Relationships relationships = aaiRW.getRelationships().get()
				if (relationships != null) {
						
					List<AAIResourceUri> vserverUris = relationships.getRelatedAAIUris(AAIObjectType.VSERVER)
					JSONArray vserverIds = new JSONArray()
				
					for (AAIResourceUri j in vserverUris) {
						
						String vserverId = j.getURIKeys().get('vserver-id')
						vserverIds.put(vserverId)					
					}
				
					JSONObject vmidsArray = new JSONObject()
					vmidsArray.put("vmIds", vserverIds.toString())
				
					logDebug("vmidsArray is: " + vmidsArray.toString(), isDebugLogEnabled)								
			
					execution.setVariable("vmIdList", vmidsArray.toString())
				}
			}
						
			if (cloudRegionId != null) {			
				AAIUri cloudRegionUri = AAIUriFactory.createResourceUri(AAIObjectType.DEFAULT_CLOUD_REGION, cloudRegionId)				
				// Check if this client region exists
				if (!client.exists(cloudRegionUri)) {
					logDebug("Cloud Region with cloudRegionId " + cloudRegionId + " does not exist in A&AI", isDebugLogEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 404, "Cloud Region with cloudRegionId " + cloudRegionId + " does not exist in A&AI")
				}
			
				AAIResultWrapper aaiRWCloud = client.get(cloudRegionUri)
			
				Map<String, Object> resultCloud = aaiRWCloud.asMap()			
			
				String aicIdentity = resultCloud.get("identity-url")
				logDebug("aicIdentity from A&AI is: " + aicIdentity, isDebugLogEnabled)
				execution.setVariable("aicIdentity", aicIdentity)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIForVnf(): ' + e.getMessage())
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
				execution.setVariable("rollbackSetVnfInMaintenanceFlag", false)
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
	 * Check if VF Closed Loop Disabled in A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void checkIfClosedLoopDisabledInAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.checkIfClosedLoopDisabledInAAI(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable('errorCode', "0")
		execution.setVariable("workStep", "checkClosedLoopDisabledFlagInAAI")
		execution.setVariable("failedActivity", "AAI")
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()			
			def vnfId = execution.getVariable("vnfId")
			logDebug("vnfId is: " + vnfId, isDebugLogEnabled)
			AAIResourcesClient client = new AAIResourcesClient()			
			AAIUri genericVnfUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
			AAIResultWrapper aaiRW = client.get(genericVnfUri)
			Map<String, Object> result = aaiRW.asMap()
			boolean isClosedLoopDisabled = result.getOrDefault("is-closed-loop-disabled", false)
		
			logDebug("isClosedLoopDisabled result: " + isClosedLoopDisabled, isDebugLogEnabled)
			execution.setVariable('isClosedLoopDisabled', isClosedLoopDisabled)
			
			if (isClosedLoopDisabled) {
				execution.setVariable("errorCode", "1004")
				execution.setVariable("errorText", "closedLoop is disabled in A&AI")
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())		
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		execution.setVariable('errorCode', "0")
		if (setDisabled) {
			execution.setVariable("workStep", "setClosedLoopDisabledFlagInAAI")
			execution.setVariable("rollbackSetClosedLoopDisabledFlag", true)
		}
		else {
			execution.setVariable("workStep", "unsetClosedLoopDisabledFlagInAAI")
			execution.setVariable("rollbackSetClosedLoopDisabledFlag", false)
		}
		
		execution.setVariable("failedActivity", "AAI")
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			def vnfId = execution.getVariable("vnfId")
			AAIResourcesClient client = new AAIResourcesClient()			
			AAIUri genericVnfUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
			
			Map<String, Boolean> request = new HashMap<>()
			request.put("is-closed-loop-disabled", setDisabled)
			client.update(genericVnfUri, request)
			logDebug("set isClosedLoop to: " + setDisabled, isDebugLogEnabled)		

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())			
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
		
		ApplicationControllerClient appcClient = null
		
		try {
			logDebug("Running APP-C action: " + action.toString(), isDebugLogEnabled)
			String vnfId = execution.getVariable('vnfId')
			String msoRequestId = execution.getVariable('requestId')
			execution.setVariable('msoRequestId', msoRequestId)			
			execution.setVariable("failedActivity", "APP-C")
			
			appcClient = new ApplicationControllerClient()				
			ApplicationControllerSupport support = new ApplicationControllerSupport()			
			appcClient.appCSupport=support			
			org.springframework.test.util.ReflectionTestUtils.setField(support, "lcmModelPackage", "org.onap.appc.client.lcm.model");			
			Flags flags = new Flags();			
			ActionIdentifiers actionIdentifiers = new ActionIdentifiers();			
			actionIdentifiers.setVnfId(vnfId);
			Status appcStatus
			switch(action) {
				case Action.Lock:
					execution.setVariable('workStep', "LockVNF")
					appcStatus = appcClient.runCommand(Action.Lock,actionIdentifiers,null,msoRequestId)					
					break
				case Action.Unlock:
					execution.setVariable('workStep', "UnlockVNF")
					appcStatus = appcClient.runCommand(Action.Unlock,actionIdentifiers,null,msoRequestId)					
					break
				case Action.HealthCheck:
					def healthCheckIndex = execution.getVariable('healthCheckIndex')
					execution.setVariable('workStep', "HealthCheckVNF" + healthCheckIndex)
					execution.setVariable('healthCheckIndex', healthCheckIndex + 1)
					appcStatus = appcClient.runCommand(Action.HealthCheck,actionIdentifiers,null,msoRequestId)					
					break
				case Action.Start:
					execution.setVariable('workStep', "StartVNF")
					appcStatus = appcClient.runCommand(Action.Start,actionIdentifiers,null,msoRequestId)					
					break
				case Action.Stop:
					execution.setVariable('workStep', "StopVNF")
					appcStatus = appcClient.runCommand(Action.Stop,actionIdentifiers,null,msoRequestId)					
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
	 * Placeholder for a call to APP-C client to execute specified APP-C command for this VNF.
	 *
	 *
	 * @param execution The flow's execution instance.
	 * @param action The action to take in APP-C.
	 */
	public void runAppcCommandPlaceholder(DelegateExecution execution, String action) {
		def method = getClass().getSimpleName() + '.runAppcCommandPlaceholder(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')		
		execution.setVariable('errorCode', "0")
		logDebug('Entered ' + method, isDebugLogEnabled)		
		execution.setVariable("failedActivity", "APP-C")
		execution.setVariable("workStep", action)		
	}





	

	/**
	 * Builds a "CompletionHandler" request and stores it in the specified execution variable.
	 *
	 * @param execution the execution
	 * @param resultVar the execution variable in which the result will be stored
	 */
	public void completionHandlerPrep(DelegateExecution execution, String resultVar) {
		def method = getClass().getSimpleName() + '.completionHandlerPrep(' +
			'execution=' + execution.getId() +
			', resultVar=' + resultVar +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)		

		try {
			
			def requestInfo = execution.getVariable('requestInfo')

			String content = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1">
					${requestInfo}
					<sdncadapterworkflow:status-message>Vnf has been updated successfully.</sdncadapterworkflow:status-message>
					<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
			"""

			content = utils.formatXml(content)
			logDebug(resultVar + ' = ' + System.lineSeparator() + content, isDebugLogEnabled)
			execution.setVariable(resultVar, content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
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
   public void prepDoUpdateVnfAndModules(DelegateExecution execution) {
	   def method = getClass().getSimpleName() + '.prepDoUpdateVnfAndModules(' +
		   'execution=' + execution.getId() +
		   ')'
	   def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	   execution.setVariable('errorCode', "0")
	   logDebug('Entered ' + method, isDebugLogEnabled)
	   execution.setVariable("workStep", "doUpdateVnfAndModules")
	   execution.setVariable("failedActivity", "MSO Update VNF")
	   logDebug('Exited ' + method, isDebugLogEnabled)
	   
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
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def prefix = execution.getVariable('prefix')			
			def requestInformation = execution.getVariable("requestInfo")		
			
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
			throw e;
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
	 * Increment Retry Count for Current Work Step
	 *
	 * @param execution The flow's execution instance.
	 */
	public void incrementRetryCount(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.incrementRetryCount(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		
		String retryCountVariableName = execution.getVariable("workStep") + "RetryCount"
		execution.setVariable("retryCountVariableName", retryCountVariableName)
		
		def retryCountVariable = execution.getVariable(retryCountVariableName)
		int retryCount = 0
		
		if (retryCountVariable != null) {
			retryCount = (int) retryCountVariable
		}
		
		retryCount += 1
		
		execution.setVariable(retryCountVariableName, retryCount)
		
		logDebug("value of " + retryCountVariableName + " is " + retryCount, isDebugLogEnabled)
		logDebug('Exited ' + method, isDebugLogEnabled)
			
		
	}
	
	public void preProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** preProcessRollback ***** ", isDebugEnabled)
		try {
			
			Object workflowException = execution.getVariable("WorkflowException");
 
			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Prev workflowException: " + workflowException.getErrorMessage(), isDebugEnabled)
				execution.setVariable("prevWorkflowException", workflowException);
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
 
	public void postProcessRollback (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG"," ***** postProcessRollback ***** ", isDebugEnabled)
		String msg = ""
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
				utils.log("DEBUG", "Setting prevException to WorkflowException: ", isDebugEnabled)
				execution.setVariable("WorkflowException", workflowException);
			}
			
		} catch (BpmnError b) {
			utils.log("DEBUG", "BPMN Error during postProcessRollback", isDebugEnabled)
			throw b;
		} catch(Exception ex) {
			msg = "Exception in postProcessRollback. " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
		}
		utils.log("DEBUG"," *** Exit postProcessRollback *** ", isDebugEnabled)
	}
 

	
}
