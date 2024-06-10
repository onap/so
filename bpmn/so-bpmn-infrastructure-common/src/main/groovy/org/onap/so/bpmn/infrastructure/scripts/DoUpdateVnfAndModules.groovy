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
import org.onap.so.client.HttpClientFactory
import org.onap.logging.filter.base.ErrorCode

import jakarta.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ModuleResource
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.so.client.HttpClient
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.logging.filter.base.ONAPComponents;

/**
 * This class supports the VID Flow
 * with the update of a generic vnf and related VF modules.
 */
class DoUpdateVnfAndModules extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoUpdateVnfAndModules.class);

	String Prefix="DUVAM_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		logger.trace("STARTED DoUpdateVnfAndModules PreProcessRequest Process")

		try{
			// Get Variables

			String requestId = execution.getVariable("msoRequestId")
			execution.setVariable("requestId", requestId)
			execution.setVariable("mso-request-id", requestId)
			logger.debug("Incoming Request Id is: " + requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			logger.debug("Incoming Service Instance Id is: " + serviceInstanceId)

			String vnfId = execution.getVariable("vnfId")
			logger.debug("Incoming Vnf Id is: " + vnfId)

			String source = "VID"
			execution.setVariable("DUVAM_source", source)
			logger.debug("Incoming Source is: " + source)

			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null) {
				sdncVersion = "1702"
			}
			execution.setVariable("DUVAM_sdncVersion", sdncVersion)
			logger.debug("Incoming Sdnc Version is: " + sdncVersion)

			VnfResource vnfResource = (VnfResource) execution.getVariable("vnfResourceDecomposition")

			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			String serviceModelInfo = execution.getVariable("serviceModelInfo")

			String serviceId = execution.getVariable("productFamilyId")
			execution.setVariable("DUVAM_serviceId", serviceId)
			logger.debug("Incoming Service Id is: " + serviceId)

			String modelUuid = jsonUtil.getJsonValue(vnfModelInfo, "modelUuid")
			execution.setVariable("DUVAM_modelUuid", modelUuid)
			logger.debug("Incoming modelUuid is: " + modelUuid)

			String modelCustomizationUuid = jsonUtil.getJsonValue(vnfModelInfo, "modelCustomizationUuid")
			execution.setVariable("DUVAM_modelCustomizationUuid", modelCustomizationUuid)
			logger.debug("Incoming Model Customization Uuid is: " + modelCustomizationUuid)

			String cloudSiteId = execution.getVariable("lcpCloudRegionId")
			execution.setVariable("DUVAM_cloudSiteId", cloudSiteId)
			logger.debug("Incoming Cloud Site Id is: " + cloudSiteId)

			String tenantId = execution.getVariable("tenantId")
			execution.setVariable("DUVAM_tenantId", tenantId)
			logger.debug("Incoming Tenant Id is: " + tenantId)

			String globalSubscriberId = execution.getVariable("globalSubscriberId")
			if (globalSubscriberId == null) {
				globalSubscriberId = ""
			}
			execution.setVariable("DUVAM_globalSubscriberId", globalSubscriberId)
			logger.debug("Incoming Global Subscriber Id is: " + globalSubscriberId)

			execution.setVariable("DUVAM_moduleCount", 0)
			execution.setVariable("DUVAM_nextModule", 0)


		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.debug(" Error Occured in DoUpdateVnfAndModules PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoUpdateVnfAndModules PreProcessRequest")

		}
		logger.trace("COMPLETED DoUpdateVnfAndModules PreProcessRequest Process ")
	}

	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.queryAAIVfModule(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId)).depth(Depth.ONE)
			String endPoint = aaiUriUtil.createAaiUri(uri)
			logger.debug("AAI endPoint: " + endPoint)

			try {
				HttpClient client = new HttpClientFactory().newXmlClient(new URL(endPoint), ONAPComponents.AAI)
				client.addAdditionalHeader('X-TransactionId', UUID.randomUUID().toString())
				client.addAdditionalHeader('X-FromAppId', 'MSO')
				client.addAdditionalHeader('Content-Type', 'application/xml')
				client.addAdditionalHeader('Accept','application/xml')

				def responseData = ''

				logger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				Response response = client.get()
				logger.debug("createVfModule - invoking httpGet() to AAI")

				responseData = response.readEntity(String.class)
				if (responseData != null) {
					logger.debug("Received generic VNF data: " + responseData)

				}

				logger.debug("createVfModule - queryAAIVfModule Response: " + responseData)
				logger.debug("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatus())

				execution.setVariable('DUVAM_queryAAIVfModuleResponseCode', response.getStatus())
				execution.setVariable('DUVAM_queryAAIVfModuleResponse', responseData)
				logger.debug('Response code:' + response.getStatus())
				logger.debug('Response:' + System.lineSeparator() + responseData)
				//Map<String, String>[] vfModules = new HashMap<String,String>[]
				def vfModulesList = new ArrayList<Map<String,String>>()
				def vfModules = null
				def vfModuleBaseEntry = null
				if (response.getStatus() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logger.debug('Parsing the VNF data to find base module info')
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						logger.debug("vModulesText: " + vfModulesText)
						if (vfModulesText != null && !vfModulesText.trim().isEmpty()) {
							def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
							vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
							execution.setVariable("DUVAM_moduleCount", vfModules.size())
							int vfModulesSize = 0
							for (i in 0..vfModules.size()-1) {
								def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])

								Map<String, String> vfModuleEntry = new HashMap<String, String>()
								def vfModuleId = utils.getNodeText(vfModuleXml, "vf-module-id")
								vfModuleEntry.put("vfModuleId", vfModuleId)
								def vfModuleName = utils.getNodeText(vfModuleXml, "vf-module-name")
								vfModuleEntry.put("vfModuleName", vfModuleName)
								def modelInvariantUuid = utils.getNodeText(vfModuleXml, "model-invariant-id")
								vfModuleEntry.put("modelInvariantUuid", modelInvariantUuid)
								def modelUuid = utils.getNodeText(vfModuleXml, "model-version-id")
								vfModuleEntry.put("modelUuid", modelUuid)
								def modelCustomizationUuid = utils.getNodeText(vfModuleXml, "model-customization-id")
								vfModuleEntry.put("modelCustomizationUuid", modelCustomizationUuid)

								def isBaseVfModule = utils.getNodeText(vfModuleXml, "is-base-vf-module")
								vfModuleEntry.put("isBaseVfModule", isBaseVfModule)

								String volumeGroupId = ''

								logger.debug("Next module!")
								def vfModuleRelationships = vfModules[i].'**'.findAll {it.name() == 'relationship-data'}
								if (vfModuleRelationships.size() > 0) {
									for (j in 0..vfModuleRelationships.size()-1) {
										if (vfModuleRelationships[j] != null) {

											def relationshipKey = vfModuleRelationships[j].'**'.findAll {it.name() == 'relationship-key'}

											if (relationshipKey[0] == 'volume-group.volume-group-id') {
												def relationshipValue = vfModuleRelationships[j].'**'.findAll {it.name() == 'relationship-value'}
												volumeGroupId = relationshipValue[0]
												break
											}
										}
									}
								}

								vfModuleEntry.put("volumeGroupId", volumeGroupId)
								logger.debug("volumeGroupId is: " + volumeGroupId)

								// Save base vf module to add it to the start of the list later
								if (isBaseVfModule == "true") {
									vfModuleBaseEntry = vfModuleEntry
								}
								else {
									vfModulesList.add(vfModuleEntry)
								}
							}
							// Start the list with the base module if any
							if (vfModuleBaseEntry != null) {
								vfModulesList.add(0, vfModuleBaseEntry)
							}
						}

					}
				}
				else {
					logger.debug('Response code from AAI GET is: ' + response.getStatusCode())
					exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Response code from AAI GET is: ' + response.getStatusCode())
				}
				execution.setVariable("DUVAM_vfModules", vfModulesList)
			} catch (Exception ex) {
				logger.debug('Exception occurred while executing AAI GET: {}', ex.getMessage(), ex)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}

	public void prepareNextModuleToUpdate(DelegateExecution execution){

		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED prepareNextModuleToUpdate ")

		try {
			int i = execution.getVariable("DUVAM_nextModule")
			def vfModules = execution.getVariable("DUVAM_vfModules")
			def vfModule = vfModules[i]

			def vfModuleId = vfModule.get("vfModuleId")
			execution.setVariable("DUVAM_vfModuleId", vfModuleId)

			def vfModuleName = vfModule.get("vfModuleName")
			execution.setVariable("DUVAM_vfModuleName", vfModuleName)

			def isBaseVfModule = vfModule.get("isBaseVfModule")
			execution.setVariable("DUVAM_isBaseVfModule", isBaseVfModule)

			String modelInvariantUuid = vfModule.get("modelInvariantUuid")
			logger.debug("ModelInvariantUuid: " + modelInvariantUuid)

			def volumeGroupId = vfModule.get("volumeGroupId")
			execution.setVariable("DUVAM_volumeGroupId", volumeGroupId)

			execution.setVariable("DUVAM_volumeGroupName", "")

			VnfResource vnfResource = (VnfResource) execution.getVariable("vnfResourceDecomposition")
			List<ModuleResource> moduleResources = vnfResource.getVfModules()

			if (moduleResources != null && !moduleResources.isEmpty()) {

				for (j in 0..moduleResources.size()-1) {
					ModelInfo modelInfo = moduleResources[j].getModelInfo()
					String modelInvariantUuidFromDecomposition = modelInfo.getModelInvariantUuid()
					logger.debug("modelInvariantUuidFromDecomposition: " + modelInvariantUuidFromDecomposition)

					if (modelInvariantUuid.equals(modelInvariantUuidFromDecomposition)) {
						String vfModuleModelInfo = modelInfo.toJsonString()
						String vfModuleModelInfoValue = jsonUtil.getJsonValue(vfModuleModelInfo, "modelInfo")
						execution.setVariable("DUVAM_vfModuleModelInfo", vfModuleModelInfoValue)
						logger.debug("vfModuleModelInfo: " + vfModuleModelInfoValue)
						break
					}

				}
			}

		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preProcessAddOnModule. Exception is:\n" + e, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during prepareNextModuleToUpdate Method:\n" + e.getMessage())
		}
		logger.trace("COMPLETED prepareNextModuleToUpdate ")
	}


	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')
			VnfResource vnfResource = (VnfResource) execution.getVariable("vnfResourceDecomposition")
			ModelInfo vnfDecompModelInfo = vnfResource.getModelInfo()
			String vnfModelInfo = execution.getVariable("vnfModelInfo")
			String modelUuid = execution.getVariable("DUVAM_modelUuid")
			if (modelUuid == null || modelUuid.isEmpty()) {
				modelUuid = vnfDecompModelInfo.getModelUuid()
			}
			String modelCustomizationUuid = execution.getVariable("DUVAM_modelCustomizationUuid")
			if (modelCustomizationUuid == null || modelCustomizationUuid.isEmpty()) {
				modelCustomizationUuid = vnfDecompModelInfo.getModelCustomizationUuid()
			}
			String nfType = vnfResource.getNfType()
			String nfTypeString = ''
			if (nfType != null && !nfType.isEmpty()) {
				nfTypeString = "<nf-type>" + nfType + "</nf-type>"
			}
			String nfRole = vnfResource.getNfRole()
			String nfRoleString = ''
			if (nfRole != null && !nfRole.isEmpty()) {
				nfRoleString = "<nf-role>" + nfRole + "</nf-role>"
			}
			String nfFunction = vnfResource.getNfFunction()
			String nfFunctionString = ''
			if (nfFunction != null && !nfFunction.isEmpty()) {
				nfFunctionString = "<nf-function>" + nfFunction + "</nf-function>"
			}
			String nfNamingCode = vnfResource.getNfNamingCode()
			String nfNamingCodeString = ''
			if (nfNamingCode != null && !nfNamingCode.isEmpty()) {
				nfNamingCodeString = "<nf-naming-code>" + nfNamingCode + "</nf-naming-code>"
			}

			String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						<model-version-id>${MsoUtils.xmlEscape(modelUuid)}</model-version-id>
						<model-customization-id>${MsoUtils.xmlEscape(modelCustomizationUuid)}</model-customization-id>
						${nfTypeString}
						${nfRoleString}
						${nfFunctionString}
						${nfNamingCodeString}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DUVAM_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				logger.debug("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


			logger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					'Caught exception in ' + method, "BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}

	/**
	 * APP-C Call - placeholder.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void callAppCf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.callAppC(' +
			'execution=' + execution.getId() +
			')'

		logger.trace('Entered ' + method)
	}
}
