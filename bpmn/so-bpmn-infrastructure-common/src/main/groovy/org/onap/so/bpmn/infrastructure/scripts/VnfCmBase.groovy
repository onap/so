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

import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aaiclient.client.aai.*
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.appc.client.lcm.model.Action
import org.onap.appc.client.lcm.model.ActionIdentifiers
import org.onap.appc.client.lcm.model.Flags
import org.onap.appc.client.lcm.model.Status
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.appc.ApplicationControllerClient
import org.onap.so.client.appc.ApplicationControllerSupport
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public abstract class VnfCmBase extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(VnfCmBase.class)

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
	 * Get VnfResource decomposition object for this VNF.
	 *	
	 *
	 * @param execution The flow's execution instance.
	 */
	public void getVnfResourceDecomposition(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.getVnfResourceDecomposition(' +
			'execution=' + execution.getId() +
			')'
        logger.trace('Entered {}', method)

		try {
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			String vnfModelInvariantUuid = execution.getVariable('vnfModelInvariantUuid')
            logger.debug("vnfModelInvariantUuid: {}", vnfModelInvariantUuid)
			List<VnfResource> vnfResources = serviceDecomposition.getVnfResources()
			
			for (i in 0..vnfResources.size()-1) {
				ModelInfo modelInfo = vnfResources[i].getModelInfo()
				String modelInvariantUuidFromDecomposition = modelInfo.getModelInvariantUuid()
                logger.debug("modelInvariantUuidFromDecomposition: {}", modelInvariantUuidFromDecomposition)
				
				if (vnfModelInvariantUuid.equals(modelInvariantUuidFromDecomposition)) {
					VnfResource vnfResourceDecomposition = vnfResources[i]
					execution.setVariable('vnfResourceDecomposition', vnfResourceDecomposition)
					def nfRole = vnfResourceDecomposition.getNfRole()
					execution.setVariable('nfRole', nfRole)
                    logger.debug("vnfResourceDecomposition: {}", vnfResourceDecomposition.toJsonString())
					break
				}
				else {
					//exception!
				}
				
			}

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
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
	 * Get VNF info from A&AI.
	 *
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIForVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.queryAAIForVnf(' +
			'execution=' + execution.getId() +
			')'

        logger.trace('Entered {}', method)

		try {
			def transactionLoggingUuid = UUID.randomUUID().toString()
			def vnfId = execution.getVariable("vnfId")
            logger.debug("vnfId is: {}", vnfId)
			def cloudRegionId = execution.getVariable("lcpCloudRegionId")
            logger.debug("cloudRegionId is: {}", cloudRegionId)
			
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri genericVnfUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
			// Check if this VNF exists
			if (!client.exists(genericVnfUri)) {
                logger.debug("VNF with vnfId {} does not exist in A&AI", vnfId)
				exceptionUtil.buildAndThrowWorkflowException(execution, 404, "VNF with vnfId " + vnfId + " does not exist in A&AI")
			}
			
			AAIResultWrapper aaiRW = client.get(genericVnfUri)
			
			Map<String, Object> result = aaiRW.asMap()
			
			String vnfName = result.get("vnf-name")
            logger.debug("vnfName from A&AI is: {}", vnfName)
			execution.setVariable("vnfName", vnfName)
			String nfRole = result.get("nf-role")
            logger.debug("nfRole from A&AI is: {}", nfRole)
			execution.setVariable("nfRole", nfRole)
			String vnfHostIpAddress = result.get("ipv4-oam-address")
            logger.debug("vnfHostIpAddress from A&AI is: {}", vnfHostIpAddress)
			execution.setVariable("vnfHostIpAddress", vnfHostIpAddress)
			execution.setVariable("vmIdList", null)
			if (aaiRW.getRelationships() != null) {
				Relationships relationships = aaiRW.getRelationships().get()
				if (relationships != null) {
						
					List<AAIResourceUri> vserverUris = relationships.getRelatedUris(Types.VSERVER)
					JSONArray vserverIds = new JSONArray()
					JSONArray vserverSelfLinks = new JSONArray()
				
					for (AAIResourceUri j in vserverUris) {
						
						String vserverId = j.getURIKeys().get(AAIFluentTypeBuilder.Types.VSERVER.getUriParams().vserverId)
						String vserverJson = client.get(j).getJson()
                        logger.debug("Retrieved vserverJson from AAI: {}", vserverJson)
						String vserverSelfLink = jsonUtils.getJsonValue(vserverJson, "vserver-selflink")
						
						vserverIds.put(vserverId)
						vserverSelfLinks.put(vserverSelfLink)
					}
				
					JSONObject vmidsArray = new JSONObject()
					JSONObject vserveridsArray = new JSONObject()
					vmidsArray.put("vmIds", vserverSelfLinks.toString())
					vserveridsArray.put("vserverIds", vserverIds.toString())

                    logger.debug("vmidsArray is: {}", vmidsArray.toString())
                    logger.debug("vserveridsArray is: {}", vserveridsArray.toString())
			
					execution.setVariable("vmIdList", vmidsArray.toString())
					execution.setVariable("vserverIdList", vserveridsArray.toString())
				}
			}					
			
			// preserve relationships if exist
			Optional<Relationships> relationships = aaiRW.getRelationships()
			
			if(relationships.isPresent()) {
                logger.debug("relationships are present")
				String rs = relationships.get().getJson()
				def jsonSlurper = new JsonSlurper()
				def map = jsonSlurper.parseText(rs)
				if (map instanceof Map) {
					List<Map<String, Object>> relationshipsList = (List<Map<String, Object>>)map.get("relationship");
					for (Map<String, Object> relationship : relationshipsList) {
						final String relatedTo = (String)relationship.get("related-to");
						if (relatedTo.equals("platform")) {
							List<Map<String, Object>> relationshipDataList = (List<Map<String, Object>>)relationship.get("relationship-data")
                            logger.debug("Found platform entry")
							for (Map<String, Object> relationshipData : relationshipDataList) {
								String relationshipKey = (String)relationshipData.get("relationship-key");
								if (relationshipKey.equals("platform.platform-name")) {
									String platformName = (String) relationshipData.get("relationship-value")
                                    logger.debug("platform from A&AI is: {}", platformName)
									execution.setVariable("platform", platformName)
									break
								}
							}
						}
						if (relatedTo.equals("line-of-business")) {
							List<Map<String, Object>> relationshipDataList = (List<Map<String, Object>>)relationship.get("relationship-data")
                            logger.debug("Found line-of-business entry")
							for (Map<String, Object> relationshipData : relationshipDataList) {
								String relationshipKey = (String)relationshipData.get("relationship-key");
								if (relationshipKey.equals("line-of-business.line-of-business-name")) {
									String lineOfBusinessName = (String) relationshipData.get("relationship-value")
                                    logger.debug("lineOfBusiness from A&AI is: {}", lineOfBusinessName)
									execution.setVariable("lineOfBusiness", lineOfBusinessName)
									break
								}
							}
						}
					}
					
				}				
				
			}

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
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
			execution.setVariable("rollbackSetClosedLoopDisabledFlag", false)
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

		execution.setVariable('errorCode', "0")
        logger.trace('Entered {}', method)
		
		ApplicationControllerClient appcClient = null
		
		try {
            logger.debug("Running APP-C action: {}", action.toString())
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
            logger.debug("Completed AppC request")
			int appcCode = appcStatus.getCode()
            logger.debug("AppC status code is: {}", appcCode)
            logger.debug("AppC status message is: {}", appcStatus.getMessage())
			if (support.getCategoryOf(appcStatus) == ApplicationControllerSupport.StatusCategory.ERROR) {
				execution.setVariable("errorCode", Integer.toString(appcCode))
				execution.setVariable("errorText", appcStatus.getMessage())				
			}
				
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())
			
		} catch (java.lang.NoSuchMethodError e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
			execution.setVariable("errorCode", "1002")
			execution.setVariable("errorText", e.getMessage())		
			
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
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

		execution.setVariable('errorCode', "0")
        logger.trace('Entered {}', method)
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

        logger.trace('Entered {}', method)

		try {
			
			def requestInfo = execution.getVariable('requestInfo')

			String content = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
						xmlns:reqtype="http://org.onap/so/request/types/v1">
					${requestInfo}
					<sdncadapterworkflow:status-message>Vnf has been updated successfully.</sdncadapterworkflow:status-message>
					<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
			"""

			content = utils.formatXml(content)
            logger.debug('{} = {}{}', resultVar, System.lineSeparator(), content)
			execution.setVariable(resultVar, content)

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
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

	   execution.setVariable('errorCode', "0")
       logger.trace('Entered {}', method)
	   execution.setVariable("workStep", "doUpdateVnfAndModules")
	   execution.setVariable("failedActivity", "MSO Update VNF")
       logger.trace('Exited {}', method)
	   
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

        logger.trace('Entered {}', method)

		try {
			def prefix = execution.getVariable('prefix')			
			def requestInformation = execution.getVariable("requestInfo")		
			
			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg
			}

			String content = """
				<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
						xmlns:reqtype="http://org.onap/so/request/types/v1"
						xmlns:msoservtypes="http://org.onap/so/request/types/v1"
						xmlns:structuredtypes="http://org.onap/so/structured/types/v1">
					${requestInformation}
					<sdncadapterworkflow:WorkflowException>
						<sdncadapterworkflow:ErrorMessage>${MsoUtils.xmlEscape(encErrorResponseMsg)}</sdncadapterworkflow:ErrorMessage>
						<sdncadapterworkflow:ErrorCode>${MsoUtils.xmlEscape(errorResponseCode)}</sdncadapterworkflow:ErrorCode>
					</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
			"""
			content = utils.formatXml(content)
            logger.debug('{} = {}{}', resultVar, System.lineSeparator(), content)
			execution.setVariable(resultVar, content)

            logger.trace('Exited {}', method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
            logger.error("{} {} Caught exception in {}\n ", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    ErrorCode.UnknownError.getValue(), method, e)
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


    public void preProcessRollback (DelegateExecution execution) {
        logger.trace("preProcessRollback ")
		try {
			
			Object workflowException = execution.getVariable("WorkflowException");
 
			if (workflowException instanceof WorkflowException) {
                logger.debug("Prev workflowException: {}", workflowException.getErrorMessage())
				execution.setVariable("prevWorkflowException", workflowException);
				//execution.setVariable("WorkflowException", null);
			}
		} catch (BpmnError e) {
            logger.debug("BPMN Error during preProcessRollback")
		} catch(Exception ex) {
            logger.debug("Exception in preProcessRollback. {}", ex.getMessage())
		}
        logger.trace("Exit preProcessRollback ")
	}
 
	public void postProcessRollback (DelegateExecution execution) {
        logger.trace("postProcessRollback ")
		try {
			Object workflowException = execution.getVariable("prevWorkflowException");
			if (workflowException instanceof WorkflowException) {
                logger.debug("Setting prevException to WorkflowException: ")
				execution.setVariable("WorkflowException", workflowException);
			}
			
		} catch (BpmnError b) {
            logger.debug("BPMN Error during postProcessRollback")
			throw b;
		} catch(Exception ex) {
            logger.debug("Exception in postProcessRollback. {}", ex.getMessage())
		}
        logger.trace("Exit postProcessRollback ")
    }

}
