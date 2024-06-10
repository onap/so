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

package org.onap.so.bpmn.common.scripts

import org.onap.so.logger.LoggingAnchor

import org.onap.so.client.HttpClientFactory
import org.onap.logging.filter.base.ErrorCode
import jakarta.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.client.HttpClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class GenerateVfModuleName extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( GenerateVfModuleName.class);

	def Prefix="GVFMN_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()



	public void preProcessRequest(DelegateExecution execution) {
		try {
			def vnfId = execution.getVariable("vnfId")
			logger.debug("vnfId is " + vnfId)
			def vnfName = execution.getVariable("vnfName")
			logger.debug("vnfName is " + vnfName)
			def vfModuleLabel = execution.getVariable("vfModuleLabel")
			logger.debug("vfModuleLabel is " + vfModuleLabel)
			def personaModelId = execution.getVariable("personaModelId")
			logger.debug("personaModelId is " + personaModelId)
			execution.setVariable("GVFMN_vfModuleXml", "")
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in initVariables method!")
		}
	}


	public void queryAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.queryAAI(' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')
			def personaModelId = execution.getVariable('personaModelId')

			AaiUtil aaiUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
			uri.depth(Depth.ONE)
			String endPoint = aaiUtil.createAaiUri(uri)

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
				logger.debug("GenerateVfModuleName - invoking httpGet() to AAI")

				responseData = response.readEntity(String.class)
				if (responseData != null) {
					logger.debug("Received generic VNF data: " + responseData)

				}

				logger.debug("GenerateVfModuleName - queryAAIVfModule Response: " + responseData)
				logger.debug("GenerateVfModuleName - queryAAIVfModule ResponseCode: " + response.getStatus())

				execution.setVariable('GVFMN_queryAAIVfModuleResponseCode', response.getStatus())
				execution.setVariable('GVFMN_queryAAIVfModuleResponse', responseData)
				logger.debug('Response code:' + response.getStatus())
				logger.debug('Response:' + System.lineSeparator() + responseData)
				if (response.getStatus() == 200) {
					// Set the VfModuleXML
					if (responseData != null) {
						String vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						if (vfModulesText == null || vfModulesText.isEmpty()) {
							logger.debug("There are no VF modules in this VNF yet")
							execution.setVariable("GVFMN_vfModuleXml", null)
						}
						else {
							def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
							def vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
							int vfModulesSize = 0
							if (vfModules != null) {
								vfModulesSize = vfModules.size()
							}
							String matchingVfModules = "<vfModules>"
							for (i in 0..vfModulesSize-1) {
								def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
								def personaModelIdFromAAI = utils.getNodeText(vfModuleXml, "model-invariant-id")
								if (!personaModelIdFromAAI) {
									// check old attribute name
								   personaModelIdFromAAI = utils.getNodeText(vfModuleXml, "persona-model-id")
								}
								if (personaModelIdFromAAI != null && personaModelIdFromAAI.equals(personaModelId)) {
									matchingVfModules = matchingVfModules + utils.removeXmlPreamble(vfModuleXml)
								}
							}
							matchingVfModules = matchingVfModules + "</vfModules>"
							logger.debug("Matching VF Modules: " + matchingVfModules)
							execution.setVariable("GVFMN_vfModuleXml", matchingVfModules)
						}
					}
				}
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAI(): ' + e.getMessage())
		}

	}

	public void generateName (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.generateName() ' +
			'execution=' + execution.getId() +
			')'
		logger.trace('Entered ' + method)

		String vfModuleXml = execution.getVariable("GVFMN_vfModuleXml")

		String moduleIndex = utils.getLowestUnusedIndex(vfModuleXml)
		logger.debug("moduleIndex is: " + moduleIndex)
		def vnfName = execution.getVariable("vnfName")
		def vfModuleLabel = execution.getVariable("vfModuleLabel")
		def vfModuleName = vnfName + "_" + vfModuleLabel + "_" + moduleIndex
		logger.debug("vfModuleName is: " + vfModuleName)
		execution.setVariable("vfModuleName", vfModuleName)
	}
}
