/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package com.att.bpm.scripts
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution

import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

class AaiUtil {

	public MsoUtils utils = new MsoUtils()
	public static final String NETWORK_GENERIC_VNF = 'aai_network_generic_vnf_uri'
	public static final String NETWORK_VPN_BINDING = 'aai_network_vpn_binding_uri'
	public static final String NETWORK_POLICY = 'aai_network_policy_uri'
	public static final String NETWORK_VCE = 'aai_network_vce_uri'
	public static final String NETWORK_L3_NETWORK = 'aai_network_l3_network_uri'
	public static final String NETWORK_TABLE_REFERENCES = 'aai_network_table_reference_uri'
	public static final String BUSINESS_CUSTOMER = 'aai_business_customer_uri'
	public static final String BUSINESS_CUSTOMERV7 = 'aaiv7_business_customer_uri'
	public static final String CLOUD_INFRASTRUCTURE_VOLUME_GROUP = 'aai_cloud_infrastructure_volume_group_uri'
	public static final String CLOUD_INFRASTRUCTURE_CLOUD_REGION = 'aai_cloud_infrastructure_cloud_region_uri'
	public static final String CLOUD_INFRASTRUCTURE_TENANT = 'aai_cloud_infrastructure_tenant_uri'
	public static final String SEARCH_GENERIC_QUERY = 'aai_search_generic_query_uri'
	public static final String SEARCH_NODES_QUERY = 'aai_search_nodes_query_uri'
	public static final String AAI_NAMESPACE_STRING = 'http://org.openecomp.aai.inventory/'

	private AbstractServiceTaskProcessor taskProcessor

	public AaiUtil(AbstractServiceTaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor
	}
	
	public String getNetworkGenericVnfEndpoint(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkGenericVnfUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		String endpoint = execution.getVariable("URN_aai_endpoint")
		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_GENERIC_VNF)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return endpoint + uri
	}

	public String getNetworkGenericVnfUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkGenericVnfUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_GENERIC_VNF)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}
	
	

	public String getNetworkVpnBindingUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkVpnBindingUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_VPN_BINDING)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getNetworkPolicyUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkPolicyUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_POLICY)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getNetworkTableReferencesUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkTableReferencesUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_TABLE_REFERENCES)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}
	
	public String getNetworkVceUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkVceUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_VCE)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getNetworkL3NetworkUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getNetworkL3NetworkUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, NETWORK_L3_NETWORK)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getBusinessCustomerUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getBusinessCustomerUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, BUSINESS_CUSTOMER)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getBusinessCustomerUriv7(Execution execution) {
		def method = getClass().getSimpleName() + '.getBusinessCustomerUriv7(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, BUSINESS_CUSTOMERV7)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getCloudInfrastructureCloudRegionEndpoint(Execution execution) {
		def method = getClass().getSimpleName() + '.getCloudInfrastructureCloudRegionUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		String endpoint = execution.getVariable("URN_aai_endpoint")
		
		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, CLOUD_INFRASTRUCTURE_CLOUD_REGION)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return endpoint + uri
	}
	
	/**
	 * This method is depracated, use getCloudInfrastructureRegionEndpoint instead
	 */
	//@Deprecated
	public String getCloudInfrastructureCloudRegionUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getCloudInfrastructureCloudRegionUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, CLOUD_INFRASTRUCTURE_CLOUD_REGION)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getCloudInfrastructureVolumeGroupEndpoint(Execution execution) {
		def method = getClass().getSimpleName() + '.getCloudInfrastructureVolumeGroupUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		String endpoint = execution.getVariable("URN_aai_endpoint")
		
		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, CLOUD_INFRASTRUCTURE_VOLUME_GROUP)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return endpoint + uri
	}
	
	public String getCloudInfrastructureVolumeGroupUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getCloudInfrastructureVolumeGroupUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, CLOUD_INFRASTRUCTURE_VOLUME_GROUP)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getCloudInfrastructureTenantUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getCloudInfrastructureTenantUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, CLOUD_INFRASTRUCTURE_TENANT)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getSearchNodesQueryUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getSearchNodesQueryUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, SEARCH_NODES_QUERY)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}
	
	public String getSearchNodesQueryEndpoint(Execution execution) {
		def method = getClass().getSimpleName() + '.getSearchNodesQueryUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)
		
		String endpoint = execution.getVariable("URN_aai_endpoint")

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, SEARCH_NODES_QUERY)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return endpoint + uri
	}
	
	public String getSearchGenericQueryUri(Execution execution) {
		def method = getClass().getSimpleName() + '.getSearchGenericQueryUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uri = getUri(execution, SEARCH_GENERIC_QUERY)

		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getUri(Execution execution, String key) {
		def method = getClass().getSimpleName() + '.getUri(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		taskProcessor.logDebug('Entered ' + method, isDebugLogEnabled)

		def processKey = taskProcessor.getProcessKey(execution)
		def uriKey = "URN_mso_workflow_${processKey}_${key}"
		def defaultUriKey = "URN_mso_workflow_default_${key}"

		taskProcessor.logDebug('URI Key ' + uriKey, isDebugLogEnabled)
		taskProcessor.logDebug('Default URI Key ' + defaultUriKey, isDebugLogEnabled)

		def uri = execution.getVariable(uriKey)
		if (uri == null || uri == "") {
			taskProcessor.logDebug("Process specific key not defined, using default key $defaultUriKey", isDebugLogEnabled)
			uri = execution.getVariable(defaultUriKey)
		}

		taskProcessor.logDebug('AAI URI is ' + uri, isDebugLogEnabled)
		taskProcessor.logDebug('Exited ' + method, isDebugLogEnabled)
		return uri
	}

	public String getNamespaceFromUri(String uri) {
			String namespace = AAI_NAMESPACE_STRING
			if(uri!=null){
				return namespace + uri.substring(uri.indexOf("v"),  uri.indexOf("v")+2)
			}else{
				return namespace
			}
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
	 */
	public APIResponse executeAAIGetCall(Execution execution, String url){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug(" ======== STARTED Execute AAI Get Process ======== ", isDebugEnabled)
		try{
			String uuid = UUID.randomUUID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)
			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)
					
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml");

			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}

			APIResponse apiResponse = client.get()
			return apiResponse

		}catch(Exception e){
			taskProcessor.logDebug("Exception occured while executing AAI Get Call. Exception is: \n" + e, isDebugEnabled)
			return e
		}
		taskProcessor.logDebug( "======== COMPLETED Execute AAI Get Process ======== ", isDebugEnabled)
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
	 */
	public APIResponse executeAAIPutCall(Execution execution, String url, String payload){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Put Process ======== ", isDebugEnabled)
		try{
			String uuid = UUID.randomUUID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)

			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Content-Type", "application/xml").addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse apiResponse = client.httpPut(payload)

			return apiResponse
		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Put Call. Exception is: \n" + e, isDebugEnabled)
			return e
		}
		taskProcessor.logDebug( "======== Completed Execute AAI Put Process ======== ", isDebugEnabled)
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
	 */
	public APIResponse executeAAIDeleteCall(Execution execution, String url){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		taskProcessor.logDebug( " ======== Started Execute AAI Delete Process ======== ", isDebugEnabled)
		try{
			String uuid = UUID.randomUUID()
			taskProcessor.logDebug( "Generated uuid is: " + uuid, isDebugEnabled)

			taskProcessor.logDebug( "URL to be used is: " + url, isDebugEnabled)
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(url);
			RESTClient client = new RESTClient(config).addHeader("X-FromAppId", "MSO").addHeader("X-TransactionId", uuid).addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse apiResponse = client.delete()

			return apiResponse

		}catch(Exception e){
			taskProcessor.utils.log("ERROR", "Exception occured while executing AAI Delete Call. Exception is: \n" + e, isDebugEnabled)
			return e
		}
		taskProcessor.logDebug( "======== Completed Execute AAI Delete Process ======== ", isDebugEnabled)
	}


	/** Utilitty to get the Cloud Region from AAI
	 * Returns String cloud region id, (ie, cloud-region-id)
	 * @param execution
	 * @param url  - url for AAI get cloud region
	 * @param backend - "PO" - real region, or "SDNC" - v2.5 (fake region).
	 */

	//TODO: We should refactor this method to return WorkflowException instead of Error. Also to throw MSOWorkflowException which the calling flow will then catch.

	public String getAAICloudReqion(Execution execution, String url, String backend, inputCloudRegion){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

		try {
		  APIResponse apiResponse = executeAAIGetCall(execution, url)
		  String returnCode = apiResponse.getStatusCode()
		  String aaiResponseAsString = apiResponse.getResponseBodyAsString()
		  taskProcessor.utils.log("DEBUG", "Call AAI Cloud Region Return code: " + returnCode, isDebugEnabled)
		  execution.setVariable(execution.getVariable("prefix")+"queryCloudRegionReturnCode", returnCode)
		  //taskProcessor.utils.log("DEBUG", "Call AAI Cloud Region Response: " + aaiResponseAsString, isDebugEnabled)
		  //execution.setVariable(execution.getVariable("prefix")+"queryCloudRegionResponse", aaiResponseAsString)
		  String regionId = ""
		  if (returnCode == "200") {
			 taskProcessor.utils.log("DEBUG", "Call AAI Cloud Region is Successful.", isDebugEnabled)
			   try {
			   String regionVersion = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-version")
			   taskProcessor.utils.log("DEBUG", "Cloud Region Version from AAI for " + backend + " is: " + regionVersion, isDebugEnabled)
			   if (backend == "PO") {
				  regionId = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-id")
			   } else { // backend not "PO"
				  if (regionVersion == "2.5" ) {
					  regionId = "AAIAIC25"
				  } else {
					  regionId = taskProcessor.utils.getNodeText(aaiResponseAsString, "cloud-region-id")
				  }
			   }

			   taskProcessor.utils.log("DEBUG", "Cloud Region Id from AAI " + backend + " is: " + regionId, isDebugEnabled)
			   return regionId

			 } catch (Exception e) {
				  taskProcessor.utils.log("ERROR", "Exception occured while getting the Cloud Reqion. Exception is: \n" + e, isDebugEnabled)
				  return "ERROR"
			 }
		  } else { // not 200
		      if (returnCode == "404") {
				 if (backend == "PO") {
					  regionId = inputCloudRegion
				 } else  {  // backend not "PO"
					  regionId = "AAIAIC25"
				 }
				 taskProcessor.utils.log("DEBUG", "Cloud Region value for code='404' of " + backend + " is: " + regionId, isDebugEnabled)
				  return regionId
		      } else {
			      taskProcessor.utils.log("ERROR", "Call AAI Cloud Region is NOT Successful.", isDebugEnabled)
			      return "ERROR"
		      }
		  }
		}catch(Exception e) {
		   taskProcessor.utils.log("ERROR", "Exception occured while getting the Cloud Reqion. Exception is: \n" + e, isDebugEnabled)
		   return "ERROR"
		}
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



}

