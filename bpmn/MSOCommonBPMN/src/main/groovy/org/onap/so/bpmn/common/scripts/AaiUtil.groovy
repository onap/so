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
import org.onap.so.logging.filter.base.ErrorCode

import java.util.regex.Matcher
import java.util.regex.Pattern

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.GenericVnf
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.HttpClient
import org.onap.aaiclient.client.aai.AAIVersion
import org.onap.aaiclient.client.aai.entities.uri.AAIUri
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.openpojo.rules.HasToStringRule
import org.onap.so.logging.filter.base.ONAPComponents;

@Deprecated
class AaiUtil {
    private static final Logger logger = LoggerFactory.getLogger( AaiUtil.class);


	public MsoUtils utils = new MsoUtils()
	public static final String AAI_NAMESPACE_STRING_KEY = 'mso.workflow.global.default.aai.namespace'
	public static final String DEFAULT_VERSION_KEY = 'mso.workflow.global.default.aai.version'

	private String aaiNamespace = null;

	private AbstractServiceTaskProcessor taskProcessor

	public AaiUtil(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	public String createAaiUri(AAIUri uri) {
		return createAaiUri(AAIVersion.valueOf('V' + UrnPropertiesReader.getVariable(DEFAULT_VERSION_KEY)), uri)
	}
	public String createAaiUri(AAIVersion version, AAIUri uri) {
		String endpoint = UrnPropertiesReader.getVariable("aai.endpoint")
		String result = UriBuilder.fromUri(endpoint).path('aai').path(version.toString()).build().toString()
		return UriBuilder.fromUri(result + uri.build().toString()).build().toString()
	}

	public String getNamespace() {
		return getNamespace(AAIVersion.valueOf('V' + UrnPropertiesReader.getVariable(DEFAULT_VERSION_KEY)))
	}

	public String getNamespace(AAIVersion version) {
		String namespace = UrnPropertiesReader.getVariable(AAI_NAMESPACE_STRING_KEY)
		if (namespace == null) {
		   throw new Exception('Internal Error: AAI Namespace has not been set yet. A getUri() method needs to be invoked first.')
		}

		return namespace + version
	}

	/* Utility to get the Cloud Region from AAI
	 * Returns String cloud region id, (ie, cloud-region-id)
	 * @param execution
	 * @param url  - url for AAI get cloud region
	 * @param backend - "PO" - real region, or "SDNC" - v2.5 (fake region).
	 */

	public String getAAICloudReqion(DelegateExecution execution, String url, String backend, inputCloudRegion){
		String regionId = ""
		try{
			URL Url = new URL(url)
			HttpClient client = new HttpClientFactory().newXmlClient(Url, ONAPComponents.AAI)
			client.addBasicAuthHeader(UrnPropertiesReader.getVariable("aai.auth", execution), UrnPropertiesReader.getVariable("mso.msoKey", execution))
			client.addAdditionalHeader("X-FromAppId", "MSO")
			client.addAdditionalHeader("X-TransactionId", utils.getRequestID())
			client.addAdditionalHeader("Accept", MediaType.APPLICATION_XML)

			Response apiResponse = client.get()

			String returnCode = apiResponse.getStatus()
			String aaiResponseAsString = apiResponse.readEntity(String.class)
			logger.debug("Call AAI Cloud Region Return code: " + returnCode)
			execution.setVariable(execution.getVariable("prefix")+"queryCloudRegionReturnCode", returnCode)

			if(returnCode == "200"){
				logger.debug("Call AAI Cloud Region is Successful.")

				String regionVersion = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-version")
				logger.debug("Cloud Region Version from AAI for " + backend + " is: " + regionVersion)
				if (backend == "PO") {
					regionId = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-id")
				} else { // backend not "PO"
					if (regionVersion == "2.5" ) {
						regionId = "AAIAIC25"
					} else {
						regionId = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-id")
					}
				}
				if(regionId == null){
					throw new BpmnError("MSOWorkflowException")
				}
				logger.debug("Cloud Region Id from AAI " + backend + " is: " + regionId)
			}else if (returnCode == "404"){ // not 200
				if (backend == "PO") {
					regionId = inputCloudRegion
				}else{  // backend not "PO"
					regionId = "AAIAIC25"
				}
				logger.debug("Cloud Region value for code='404' of " + backend + " is: " + regionId)
			}else{
				logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
						"Call AAI Cloud Region is NOT Successful.", "BPMN",
						ErrorCode.UnknownError.getValue());
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(Exception e) {
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception occured while getting the Cloud Reqion.", "BPMN",
					ErrorCode.UnknownError.getValue(), e.getMessage());
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return regionId
	}

	/**
	 * Get the lowest unused VF Module index from AAI response for a given module type. The criteria for
	 * determining module type is specified by "key" parameter (for example, "persona-model-id"),
	 * the value for filtering is specified in "value" parameter
	 *
	 * @param execution
	 * @param aaiVnfResponse
	 * @param key
	 * @param value
	 *
	 * @return moduleIndex
	 *
	 */
	public int getLowestUnusedVfModuleIndexFromAAIVnfResponse(DelegateExecution execution, GenericVnf aaiVnfResponse, String key, String value) {
		if (aaiVnfResponse != null) {
			String vfModulesText = taskProcessor.utils.getNodeXml(aaiVnfResponse, "vf-modules")
			if (aaiVnfResponse.getVfModules() == null || aaiVnfResponse.getVfModules().getVfModule().isEmpty()) {
				logger.debug("There are no VF modules in this VNF yet")
				return 0
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
					def keyFromAAI = taskProcessor.utils.getNodeText(vfModuleXml, key)
					if (keyFromAAI != null && keyFromAAI.equals(value)) {
						matchingVfModules = matchingVfModules + taskProcessor.utils.removeXmlPreamble(vfModuleXml)
					}
				}
				matchingVfModules = matchingVfModules + "</vfModules>"
				logger.debug("Matching VF Modules: " + matchingVfModules)
				String lowestUnusedIndex = taskProcessor.utils.getLowestUnusedIndex(matchingVfModules)
				return Integer.parseInt(lowestUnusedIndex)
			}
		}
		else {
			return 0
		}
	}
}
