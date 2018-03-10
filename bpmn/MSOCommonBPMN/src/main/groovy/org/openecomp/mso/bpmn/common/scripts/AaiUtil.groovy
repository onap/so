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

package org.openecomp.mso.bpmn.common.scripts
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

class AaiUtil {

	public MsoUtils utils = new MsoUtils()
	public static final String AAI_NAMESPACE_STRING_KEY = 'URN_mso_workflow_global_default_aai_namespace'
	public static final String DEFAULT_VERSION_KEY = 'URN_mso_workflow_global_default_aai_version'

	private String aaiNamespace = null

	private AbstractServiceTaskProcessor taskProcessor

	public AaiUtil(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}

	public String getNetworkGenericVnfEndpoint(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String endpoint = execution.getVariable("URN_aai_endpoint")
		def uri = getNetworkGenericVnfUri(execution)
		taskProcessor.logDebug('AaiUtil.getNetworkGenericVnfEndpoint() - AAI endpoint: ' + endpoint + uri, isDebugLogEnabled)
		return endpoint + uri
	}

	public String getNetworkGenericVnfUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'generic_vnf')
		taskProcessor.logDebug('AaiUtil.getNetworkGenericVnfUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getNetworkVpnBindingUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'vpn_binding')
		taskProcessor.logDebug('AaiUtil.getNetworkVpnBindingUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getNetworkPolicyUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'network_policy')
		taskProcessor.logDebug('AaiUtil.getNetworkPolicyUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getNetworkTableReferencesUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'route_table_reference')
		taskProcessor.logDebug('AaiUtil.getNetworkTableReferencesUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getNetworkVceUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'vce')
		taskProcessor.logDebug('AaiUtil.getNetworkVceUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getNetworkL3NetworkUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'l3_network')
		taskProcessor.logDebug('AaiUtil.getNetworkL3NetworkUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getBusinessCustomerUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'customer')
		taskProcessor.logDebug('AaiUtil.getBusinessCustomerUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	//public String getBusinessCustomerUriv7(Execution execution) {
	//	def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
	//	//def uri = getUri(execution, BUSINESS_CUSTOMERV7)
	//	def uri = getUri(execution, 'Customer')
	//	taskProcessor.logDebug('AaiUtil.getBusinessCustomerUriv7() - AAI URI: ' + uri, isDebugLogEnabled)
	//	return uri
	//}

	public String getCloudInfrastructureCloudRegionEndpoint(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String endpoint = execution.getVariable("URN_aai_endpoint")
		def uri = getCloudInfrastructureCloudRegionUri(execution)
		taskProcessor.logDebug('AaiUtil.getCloudInfrastructureCloudRegionEndpoint() - AAI endpoint: ' + endpoint + uri, isDebugLogEnabled)
		return endpoint + uri
	}

	public String getCloudInfrastructureCloudRegionUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'cloud_region')
		taskProcessor.logDebug('AaiUtil.getCloudInfrastructureCloudRegionUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getCloudInfrastructureTenantUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'tenant')
		taskProcessor.logDebug('AaiUtil.getCloudInfrastructureTenantUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getSearchNodesQueryUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'nodes_query')
		taskProcessor.logDebug('AaiUtil.getSearchNodesQueryUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getSearchNodesQueryEndpoint(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		String endpoint = execution.getVariable("URN_aai_endpoint")
		def uri = getSearchNodesQueryUri(execution)
		taskProcessor.logDebug('AaiUtil.getSearchNodesQueryEndpoint() - AAI endpoint: ' + endpoint + uri, isDebugLogEnabled)
		return endpoint + uri
	}

	public String getSearchGenericQueryUri(Execution execution) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def uri = getUri(execution, 'generic_query')
		taskProcessor.logDebug('AaiUtil.getSearchGenericQueryUri() - AAI URI: ' + uri, isDebugLogEnabled)
		return uri
	}

	public String getVersion(Execution execution, resourceName, processKey) {
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')

		resourceName = resourceName.replaceAll('-', '_')

		def versionWithResourceKey = "URN_mso_workflow_default_aai_${resourceName}_version"
		def versionWithProcessKey = "URN_mso_workflow_custom_${processKey}_aai_version"

		def version = execution.getVariable(versionWithProcessKey)
		if (version) {
			taskProcessor.logDebug("AaiUtil.getVersion() - using flow specific ${versionWithProcessKey}=${version}", isDebugLogEnabled)
			return version
		}

		version = execution.getVariable(versionWithResourceKey)
		if (version) {
			taskProcessor.logDebug("AaiUtil.getVersion() - using resource specific ${versionWithResourceKey}=${version}", isDebugLogEnabled)
			return version
		}

		version = execution.getVariable(DEFAULT_VERSION_KEY)
		if (version) {
			taskProcessor.logDebug("AaiUtil.getVersion() - using default version ${DEFAULT_VERSION_KEY}=${version}", isDebugLogEnabled)
			return version
		}

		(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, "Internal Error: One of the following should be defined in MSO URN properties file: ${versionWithResourceKey}, ${versionWithProcessKey}, ${DEFAULT_VERSION_KEY}")
	}

	public String getUri(Execution execution, resourceName) {

		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		def processKey = taskProcessor.getMainProcessKey(execution)
		resourceName = resourceName.replaceAll('-', '_')

		//set namespace
		setNamespace(execution)

		// Check for flow+resource specific first
		def key = "URN_mso_workflow_${processKey}_aai_${resourceName}_uri"
		def uri = execution.getVariable(key)
		if(uri) {
			taskProcessor.logDebug("AaiUtil.getUri() - using flow+resource specific key: ${key}=${uri}", isDebugLogEnabled)
			return uri
		}

		// Check for versioned key
		def version = getVersion(execution, resourceName, processKey)
		key = "URN_mso_workflow_default_aai_v${version}_${resourceName}_uri"
		uri = execution.getVariable(key)

		if(uri) {
			taskProcessor.logDebug("AaiUtil.getUri() - using versioned URI key: ${key}=${uri}", isDebugLogEnabled)
			return uri
		}

		(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, 'Internal Error: AAI URI entry for ' + key + ' not defined in the MSO URN properties file')
	}

	public String setNamespace(Execution execution) {
		def key = AAI_NAMESPACE_STRING_KEY
		aaiNamespace = execution.getVariable(key)
		if (aaiNamespace == null ) {
			(new ExceptionUtil()).buildAndThrowWorkflowException(execution, 9999, 'Internal Error: AAI URI entry for ' + key + ' not defined in the MSO URN properties file')
		}
	}

	/**
	 * This method can be used for getting the building namespace out of uri.
	 *  NOTE: A getUri() method needs to be invoked first.
	 *        Alternative method is the getNamespaceFromUri(Execution execution, String uri)
	 * return namespace (plus version from uri)
	 *
	 * @param url
	 *
	 * @return namespace
	 */

	public String getNamespaceFromUri(String uri) {
		 if (aaiNamespace == null) {
			throw new Exception('Internal Error: AAI Namespace has not been set yet. A getUri() method needs to be invoked first.')
		}
		String namespace = aaiNamespace
		if(uri!=null){
			String version = getVersionFromUri(uri)
			return namespace + "v"+version
		}else{
			return namespace
		}
	}

	/**
	 * This method can be used for building namespace with aai version out of uri.
	 *   NOTE: 2 arguments: Execution execution & String uri
	 * @param execution
	 * @param url
	 *
	 * @return namespace
	 */
	public String getNamespaceFromUri(Execution execution, String uri) {
	   String namespace = execution.getVariable(AAI_NAMESPACE_STRING_KEY)
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
		def version = ""
		def savedVersion = ""
		for (int x=2; x<6; x++) {
			version = uri.substring(uri.indexOf("v")+1,  uri.indexOf("v")+x)
			if (!Character.isDigit(version.charAt(version.size()-1))) {
				break
			}
			savedVersion = version
		}
		return savedVersion
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
	public APIResponse executeAAIGetCall(Execution execution, String url){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug(" ======== STARTED Execute AAI Get Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)
			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml")

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.get()

			taskProcessor.logDebug( "======== COMPLETED Execute AAI Get Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.logDebug("Exception occured while executing AAI Get Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public APIResponse executeAAIPutCall(Execution execution, String url, String payload){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Put Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)
			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/xml").addHeader("Accept","application/xml")
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPut(payload)

			taskProcessor.logDebug( "======== Completed Execute AAI Put Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Put Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public APIResponse executeAAIPatchCall(Execution execution, String url, String payload){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Patch Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)

			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/merge-patch+json").addHeader("Accept","application/json")
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPatch(payload)

			taskProcessor.logDebug( "======== Completed Execute AAI Patch Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Patch Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public APIResponse executeAAIDeleteCall(Execution execution, String url){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Delete Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)
			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml")
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.delete()

			taskProcessor.logDebug( "======== Completed Execute AAI Delete Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Delete Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public APIResponse executeAAIDeleteCall(Execution execution, String url, String payload, String authHeader){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Delete Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)

			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml").addAuthorizationHeader(authHeader)
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpDelete(payload)

			taskProcessor.logDebug( "======== Completed Execute AAI Delete Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Delete Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public APIResponse executeAAIPostCall(Execution execution, String url, String payload){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Post Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			String uuid = utils.getRequestID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)
			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml")

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPost(payload)

			taskProcessor.logDebug( "======== Completed Execute AAI Post Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Post Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public APIResponse executeAAIPostCall(Execution execution, String url, String payload, String authenticationHeaderValue, String headerName, String headerValue){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Post Process ======== ", isDebugEnabled)
		APIResponse apiResponse = null
		try{
			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url)
			RESTClient client = new RESTClient(config).addAuthorizationHeader(authenticationHeaderValue).addHeader(headerName, headerValue)
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			apiResponse = client.httpPost(payload)

			taskProcessor.logDebug( "======== Completed Execute AAI Post Process ======== ", isDebugEnabled)
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Post Call. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		return apiResponse
	}


	/* Utility to get the Cloud Region from AAI
	 * Returns String cloud region id, (ie, cloud-region-id)
	 * @param execution
	 * @param url  - url for AAI get cloud region
	 * @param backend - "PO" - real region, or "SDNC" - v2.5 (fake region).
	 */

	public String getAAICloudReqion(Execution execution, String url, String backend, inputCloudRegion){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		String regionId = ""
		try{
			APIResponse apiResponse = executeAAIGetCall(execution, url)
			String returnCode = apiResponse.getStatusCode()
			String aaiResponseAsString = apiResponse.getResponseBodyAsString()
			taskProcessor.utils.log("DEBUG", "Call AAI Cloud Region Return code: " + returnCode, isDebugEnabled)
			execution.setVariable(execution.getVariable("prefix")+"queryCloudRegionReturnCode", returnCode)

			if(returnCode == "200"){
				taskProcessor.utils.log("DEBUG", "Call AAI Cloud Region is Successful.", isDebugEnabled)

				String regionVersion = taskProcessor.utils.getNodeText1(aaiResponseAsString, "cloud-region-version")
				taskProcessor.utils.log("DEBUG", "Cloud Region Version from AAI for " + backend + " is: " + regionVersion, isDebugEnabled)
				if (backend == "PO") {
					regionId = taskProcessor.utils.getNodeText1(aaiResponseAsString, "cloud-region-id")
				} else { // backend not "PO"
					if (regionVersion == "2.5" ) {
						regionId = "AAIAIC25"
					} else {
						regionId = taskProcessor.utils.getNodeText1(aaiResponseAsString, "cloud-region-id")
					}
				}
				if(regionId == null){
					throw new BpmnError("MSOWorkflowException")
				}
				taskProcessor.utils.log("DEBUG", "Cloud Region Id from AAI " + backend + " is: " + regionId, isDebugEnabled)
			}else if (returnCode == "404"){ // not 200
				if (backend == "PO") {
					regionId = inputCloudRegion
				}else{  // backend not "PO"
					regionId = "AAIAIC25"
				}
				taskProcessor.utils.log("DEBUG", "Cloud Region value for code='404' of " + backend + " is: " + regionId, isDebugEnabled)
			}else{
				taskProcessor.utils.log("ERROR", "Call AAI Cloud Region is NOT Successful.", isDebugEnabled)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(Exception e) {
			taskProcessor.utils.log("ERROR", "Exception occured while getting the Cloud Reqion. Exception is: \n" + e, isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
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
	public int getLowestUnusedVfModuleIndexFromAAIVnfResponse(Execution execution, String aaiVnfResponse, String key, String value) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		if (aaiVnfResponse != null) {
			String vfModulesText = taskProcessor.utils.getNodeXml(aaiVnfResponse, "vf-modules")
			if (vfModulesText == null || vfModulesText.isEmpty()) {
				taskProcessor.utils.log("DEBUG", "There are no VF modules in this VNF yet", isDebugEnabled)
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
				taskProcessor.utils.log("DEBUG", "Matching VF Modules: " + matchingVfModules, isDebugEnabled)
				String lowestUnusedIndex = taskProcessor.utils.getLowestUnusedIndex(matchingVfModules)
				return Integer.parseInt(lowestUnusedIndex)
			}
		}
		else {
			return 0
		}
	}
}