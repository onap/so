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

package org.onap.so.bpmn.common.scripts
import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.ws.rs.core.UriBuilder

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.client.aai.AAIVersion
import org.onap.so.client.aai.entities.uri.AAIUri
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.openpojo.rules.HasToStringRule
import org.onap.so.rest.APIResponse;
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig

@Deprecated
class AaiUtil {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AaiUtil.class);


	public MsoUtils utils = new MsoUtils()
	public static final String AAI_NAMESPACE_STRING_KEY = 'mso.workflow.global.default.aai.namespace'
	public static final String DEFAULT_VERSION_KEY = 'mso.workflow.global.default.aai.version'

	private String aaiNamespace = null;

	private AbstractServiceTaskProcessor taskProcessor

	public AaiUtil(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	public String getBusinessSPPartnerUri(DelegateExecution execution) {
		def uri = getUri(execution, 'sp-partner')
		msoLogger.debug('AaiUtil.getBusinessSPPartnerUri() - AAI URI: ' + uri)
		return uri
	}

	public String getVersion(DelegateExecution execution, resourceName, processKey) {
		def versionWithResourceKey = "mso.workflow.default.aai.${resourceName}.version"
		def versionWithProcessKey = "mso.workflow.custom.${processKey}.aai.version"

		def version = UrnPropertiesReader.getVariable(versionWithProcessKey, execution)
		if (version) {
			msoLogger.debug("AaiUtil.getVersion() - using flow specific ${versionWithProcessKey}=${version}")
			return version
		}

		version = UrnPropertiesReader.getVariable(versionWithResourceKey, execution)
		if (version) {
			msoLogger.debug("AaiUtil.getVersion() - using resource specific ${versionWithResourceKey}=${version}")
			return version
		}

		version = UrnPropertiesReader.getVariable(DEFAULT_VERSION_KEY, execution)
		if (version) {
			msoLogger.debug("AaiUtil.getVersion() - using default version ${DEFAULT_VERSION_KEY}=${version}")
			return version
		}

		(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, "Internal Error: One of the following should be defined in MSO URN properties file: ${versionWithResourceKey}, ${versionWithProcessKey}, ${DEFAULT_VERSION_KEY}")
	}

	public String createAaiUri(AAIUri uri) {
		return createAaiUri(AAIVersion.valueOf('V' + UrnPropertiesReader.getVariable(DEFAULT_VERSION_KEY)), uri)
	}
	public String createAaiUri(AAIVersion version, AAIUri uri) {
		String endpoint = UrnPropertiesReader.getVariable("aai.endpoint")
		String result = UriBuilder.fromUri(endpoint).path('aai').path(version.toString()).build().toString()
		return UriBuilder.fromUri(result + uri.build().toString()).build().toString()
	}

	public String setNamespace(DelegateExecution execution) {
		def key = AAI_NAMESPACE_STRING_KEY
		aaiNamespace = UrnPropertiesReader.getVariable(key, execution)
		if (aaiNamespace == null ) {
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, 'Internal Error: AAI URI entry for ' + key + ' not defined in the MSO URN properties file')
		}
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
	/**
	 * This method can be used for getting the building namespace out of uri.
	 *  NOTE: A getUri() method needs to be invoked first.
	 *        Alternative method is the getNamespaceFromUri(DelegateExecution execution, String uri)
	 * return namespace (plus version from uri)
	 *
	 * @param url
	 *
	 * @return namespace
	 */
	public String getNamespaceFromUri(String uri) {
		String namespace = UrnPropertiesReader.getVariable(AAI_NAMESPACE_STRING_KEY)
		 if (namespace == null) {
			throw new Exception('Internal Error: AAI Namespace has not been set yet. A getUri() method needs to be invoked first.')
		}
		if(uri!=null){
			String version = getVersionFromUri(uri)
			return namespace + "v"+version
		}else{
			return namespace
		}
	}

	/**
	 * This method can be used for building namespace with aai version out of uri.
	 *   NOTE: 2 arguments: DelegateExecution execution & String uri
	 * @param execution
	 * @param url
	 *
	 * @return namespace
	 */
	public String getNamespaceFromUri(DelegateExecution execution, String uri) {
	   String namespace = UrnPropertiesReader.getVariable(AAI_NAMESPACE_STRING_KEY, execution)
	   if (namespace == null ) {
		   (new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, 'Internal Error: AAI URI entry for ' + AAI_NAMESPACE_STRING_KEY + ' not defined in the MSO URN properties file')
	   }
	   if(uri!=null){
		   String version = getVersionFromUri(uri)
		   return namespace + "v"+version
	   }else{
		   return namespace
	   }
   }

	/**
	 * This is used to extract the version from uri.
	 *
	 * @param uri
	 *
	 * @return version
	 */
	private String getVersionFromUri(String uri) {

		Matcher versionRegEx = Pattern.compile("/v(\\d+)").matcher(uri)
		if (versionRegEx.find()) {
			return versionRegEx.group(1);
		}

		return "";
	}


	/**
	 * This reusable method can be used for making AAI Get Calls. The url should
	 * be passed as a parameter along with the execution.  The method will
	 * return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIGetCall(DelegateExecution execution, String url){
		msoLogger.trace("STARTED Execute AAI Get Process ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug("Generated uuid is: " + uuid)
			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml");

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpGet()

			msoLogger.trace("COMPLETED Execute AAI Get Process ")
		}catch(Exception e){
			msoLogger.debug("Exception occured while executing AAI Get Call. Exception is: \n" + e)
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}


	/**
	 * This reusable method can be used for making AAI httpPut Calls. The url should
	 * be passed as a parameter along with the execution and payload.  The method will
	 * return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 * @param payload
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIPutCall(DelegateExecution execution, String url, String payload){
		msoLogger.trace("Started Execute AAI Put Process ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug("Generated uuid is: " + uuid)
			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/xml").addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPut(payload)

			msoLogger.trace("Completed Execute AAI Put Process ")
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Put Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}

	/**
	 * This reusable method can be used for making AAI httpPatch Calls. The url should
	 * be passed as a parameter along with the execution and payload.  The method will
	 * return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 * @param payload
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIPatchCall(DelegateExecution execution, String url, String payload){
		msoLogger.trace("Started Execute AAI Patch Process ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug("Generated uuid is: " + uuid)

			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/merge-patch+json").addHeader("Accept","application/json");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPatch(payload)

			msoLogger.trace("Completed Execute AAI Patch Process ")
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Patch Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}


	/**
	 * This reusable method can be used for making AAI Delete Calls. The url should
	 * be passed as a parameter along with the execution.  The method will
	 * return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIDeleteCall(DelegateExecution execution, String url){
		msoLogger.trace("Started Execute AAI Delete Process ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug("Generated uuid is: " + uuid)
			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.delete()

			msoLogger.trace("Completed Execute AAI Delete Process ")
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Delete Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}

	/**
	 * This reusable method can be used for making AAI Delete Calls. The url should
	 * be passed as a parameter along with the execution.  The method will
	 * return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 * @param payload
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIDeleteCall(DelegateExecution execution, String url, String payload, String authHeader){
		msoLogger.trace("Started Execute AAI Delete Process ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug("Generated uuid is: " + uuid)

			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))
			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml").addAuthorizationHeader(authHeader);
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpDelete(payload)

			msoLogger.trace("Completed Execute AAI Delete Process ")
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Delete Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}

	/**
	 * This reusable method can be used for making AAI Post Calls. The url
	 * and payload should be passed as a parameters along with the execution.
	 * The method will return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 * @param payload
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIPostCall(DelegateExecution execution, String url, String payload){
		msoLogger.trace("Started Execute AAI Post Process ")
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			msoLogger.debug("Generated uuid is: " + uuid)
			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))
			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml");

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPost(payload)

			msoLogger.trace("Completed Execute AAI Post Process ")
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Post Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
	}

	/**
	 * This reusable method can be used for making AAI Post Calls. The url
	 * and payload should be passed as a parameters along with the execution.
	 * The method will return an APIResponse.
	 *
	 * @param execution
	 * @param url
	 * @param payload
	 * @param authenticationHeader - addAuthenticationHeader value
	 * @param headerName - name of header you want to add, i.e. addHeader(headerName, headerValue)
	 * @param headerValue - the header's value, i.e. addHeader(headerName, headerValue)
	 *
	 * @return APIResponse
	 *
	 */
	public APIResponse executeAAIPostCall(DelegateExecution execution, String url, String payload, String authenticationHeaderValue, String headerName, String headerValue){
		msoLogger.trace("Started Execute AAI Post Process ")
		APIResponse apiResponse = null
		try{
			msoLogger.debug("URL to be used is: " + url)

			String basicAuthCred = utils.getBasicAuth(UrnPropertiesReader.getVariable("aai.auth", execution),UrnPropertiesReader.getVariable("mso.msoKey", execution))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addAuthorizationHeader(authenticationHeaderValue).addHeader(headerName, headerValue)
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPost(payload)

			msoLogger.trace("Completed Execute AAI Post Process ")
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while executing AAI Post Call.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return apiResponse
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
			APIResponse apiResponse = executeAAIGetCall(execution, url)
			String returnCode = apiResponse.getStatusCode()
			String aaiResponseAsString = apiResponse.getResponseBodyAsString()
			msoLogger.debug("Call AAI Cloud Region Return code: " + returnCode)
			execution.setVariable(execution.getVariable("prefix")+"queryCloudRegionReturnCode", returnCode)

			if(returnCode == "200"){
				msoLogger.debug("Call AAI Cloud Region is Successful.")

				String regionVersion = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-version")
				msoLogger.debug("Cloud Region Version from AAI for " + backend + " is: " + regionVersion)
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
				msoLogger.debug("Cloud Region Id from AAI " + backend + " is: " + regionId)
			}else if (returnCode == "404"){ // not 200
				if (backend == "PO") {
					regionId = inputCloudRegion
				}else{  // backend not "PO"
					regionId = "AAIAIC25"
				}
				msoLogger.debug("Cloud Region value for code='404' of " + backend + " is: " + regionId)
			}else{
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Call AAI Cloud Region is NOT Successful.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception occured while getting the Cloud Reqion.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e.getMessage());
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, e.getMessage())
		}
		return regionId
	}

	/* returns xml Node with service-type of searchValue */
	def searchServiceType(xmlInput, searchValue){
		def fxml= new XmlSlurper().parseText(xmlInput)
		def ret = fxml.'**'.find {it.'service-type' == searchValue}
		return ret
	}

	/* returns xml Node with service-instance-id of searchValue */
	def searchServiceInstanceId(xmlInput, searchValue){
		def ret = xmlInput.'**'.find {it.'service-instance-id' == searchValue}
		return ret
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
	public int getLowestUnusedVfModuleIndexFromAAIVnfResponse(DelegateExecution execution, String aaiVnfResponse, String key, String value) {
		if (aaiVnfResponse != null) {
			String vfModulesText = taskProcessor.utils.getNodeXml(aaiVnfResponse, "vf-modules")
			if (vfModulesText == null || vfModulesText.isEmpty()) {
				msoLogger.debug("There are no VF modules in this VNF yet")
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
				msoLogger.debug("Matching VF Modules: " + matchingVfModules)
				String lowestUnusedIndex = taskProcessor.utils.getLowestUnusedIndex(matchingVfModules)
				return Integer.parseInt(lowestUnusedIndex)
			}
		}
		else {
			return 0
		}
	}
}

