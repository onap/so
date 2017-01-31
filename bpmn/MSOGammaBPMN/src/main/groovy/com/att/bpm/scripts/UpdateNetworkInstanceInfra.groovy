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

package com.att.bpm.scripts;

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>UpdateNetworkInstanceInfra.bpmn</class> process.
 */
public class UpdateNetworkInstanceInfra extends AbstractServiceTaskProcessor {
	String Prefix="UPDNETI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>UpdateNetworkInstanceInfra.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("UPDNETI_messageId", "")
		execution.setVariable("BasicAuthHeaderValuePO", "")
		execution.setVariable("BasicAuthHeaderValueSDNC", "")
		execution.setVariable("UPDNETI_UpdateNetworkInstanceInfraJsonRequest", "")
		execution.setVariable("UPDNETI_networkRequest", "")
		execution.setVariable("UPDNETI_networkInputs", "")
		execution.setVariable("UPDNETI_networkOutputs", "")
		execution.setVariable("UPDNETI_requestId", "")
		execution.setVariable("UPDNETI_source", "")
		execution.setVariable("UPDNETI_networkId", "")

		execution.setVariable("UPDNETI_CompleteMsoProcessRequest", "")
		execution.setVariable("UPDNETI_FalloutHandlerRequest", "")
		execution.setVariable("UPDNETI_isSilentSuccess", false)
		execution.setVariable("UPDNETI_isPONR", false)    // Point-of-no-return, means, rollback is not needed

		// AAI query Cloud Region
		execution.setVariable("UPDNETI_queryCloudRegionRequest","")
		execution.setVariable("UPDNETI_queryCloudRegionReturnCode","")
		execution.setVariable("UPDNETI_queryCloudRegionResponse","")
		execution.setVariable("UPDNETI_cloudRegionPo","")
		execution.setVariable("UPDNETI_cloudRegionSdnc","")
		execution.setVariable("UPDNETI_isCloudRegionGood", false)

		// AAI query Id
		execution.setVariable("UPDNETI_queryIdAAIRequest","")
		execution.setVariable("UPDNETI_queryIdAAIResponse", "")
		execution.setVariable("UPDNETI_aaiIdReturnCode", "")

		// AAI query vpn binding
		execution.setVariable("UPDNETI_queryVpnBindingAAIRequest","")
		execution.setVariable("UPDNETI_queryVpnBindingAAIResponse", "")
		execution.setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "")
		execution.setVariable("UPDNETI_vpnBindings", null)
		execution.setVariable("UPDNETI_vpnCount", 0)
		execution.setVariable("UPDNETI_routeCollection", "")

		// AAI query network policy
		execution.setVariable("UPDNETI_queryNetworkPolicyAAIRequest","")
		execution.setVariable("UPDNETI_queryNetworkPolicyAAIResponse", "")
		execution.setVariable("UPDNETI_aaiQqueryNetworkPolicyReturnCode", "")
		execution.setVariable("UPDNETI_networkPolicyUriList", null)
		execution.setVariable("UPDNETI_networkPolicyCount", 0)
		execution.setVariable("UPDNETI_networkCollection", "")

		// AAI query route table reference
		execution.setVariable("UPDNETI_queryNetworkTableRefAAIRequest","")
		execution.setVariable("UPDNETI_queryNetworkTableRefAAIResponse", "")
		execution.setVariable("UPDNETI_aaiQqueryNetworkTableRefReturnCode", "")
		execution.setVariable("UPDNETI_networkTableRefUriList", null)
		execution.setVariable("UPDNETI_networkTableRefCount", 0)
		execution.setVariable("UPDNETI_tableRefCollection", "")
		
		// AAI requery Id
		execution.setVariable("UPDNETI_requeryIdAAIRequest","")
		execution.setVariable("UPDNETI_requeryIdAAIResponse", "")
		execution.setVariable("UPDNETI_aaiRequeryIdReturnCode", "")

		// AAI update contrail
		execution.setVariable("UPDNETI_updateContrailAAIUrlRequest","")
		execution.setVariable("UPDNETI_updateContrailAAIPayloadRequest","")
		execution.setVariable("UPDNETI_updateContrailAAIResponse", "")
		execution.setVariable("UPDNETI_aaiUpdateContrailReturnCode", "")

		execution.setVariable("UPDNETI_updateNetworkRequest", "")
		execution.setVariable("UPDNETI_updateNetworkResponse", "")
		execution.setVariable("UPDNETI_rollbackNetworkRequest", "")
		execution.setVariable("UPDNETI_rollbackNetworkResponse", "")
		execution.setVariable("UPDNETI_networkReturnCode", "")
		execution.setVariable("UPDNETI_rollbackNetworkReturnCode", "")
		execution.setVariable("UPDNETI_isNetworkRollbackNeeded", false)

		execution.setVariable("UPDNETI_changeAssignSDNCRequest", "")
		execution.setVariable("UPDNETI_changeAssignSDNCResponse", "")
		execution.setVariable("UPDNETI_rollbackSDNCRequest", "")
		execution.setVariable("UPDNETI_rollbackSDNCResponse", "")
		execution.setVariable("UPDNETI_sdncReturnCode", "")
		execution.setVariable("UPDNETI_rollbackSDNCReturnCode", "")
		execution.setVariable("UPDNETI_isSdncRollbackNeeded", false)
		execution.setVariable("UPDNETI_sdncResponseSuccess", false)

		execution.setVariable("UPDNETI_updateDBRequest", "")
		execution.setVariable("UPDNETI_updateDBResponse", "")
		execution.setVariable("UPDNETI_dbReturnCode", "")

		execution.setVariable("UPDNETI_isVnfBindingPresent", false)
		execution.setVariable("UPDNETI_Success", false)
		execution.setVariable("UPDNETI_serviceInstanceId", "")
		execution.setVariable("GENGS_type", "service-instance") // Setting for Generic Sub Flow use

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>UpdateNetworkInstanceInfra.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest UpdateNetworkInstanceInfra Request ***** ", isDebugEnabled)

		// initialize flow variables
		InitializeProcessVariables(execution)

		// get Incoming request & validate json format
		String updateNetworkJsonIncoming = execution.getVariable("bpmnRequest")
		utils.logAudit(updateNetworkJsonIncoming)
		try {
			def prettyJson = JsonOutput.prettyPrint(updateNetworkJsonIncoming.toString())
			utils.log("DEBUG", " Incoming message formatted . . . : " + '\n' + prettyJson, isDebugEnabled)

		} catch (Exception ex) {
			String dataErrorMessage = " Invalid json format Request - " + ex.getMessage()
			utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
		}

		// PO Authorization Info / headers Authorization=
		String basicAuthValuePO = execution.getVariable("URN_mso_adapters_po_auth")
		utils.log("DEBUG", " Obtained BasicAuth userid password for PO/SDNC adapter: " + basicAuthValuePO, isDebugEnabled)
		try {
			def encodedString = utils.getBasicAuth(basicAuthValuePO, execution.getVariable("URN_mso_msoKey"))
			execution.setVariable("BasicAuthHeaderValuePO",encodedString)
			execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

		} catch (IOException ex) {
			String dataErrorMessage = " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
			utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
		}

		try {

			execution.setVariable("UPDNETI_UpdateNetworkInstanceInfraJsonRequest", updateNetworkJsonIncoming)

			// recreate the xml network-request
			String networkRequest =  vidUtils.createXmlNetworkRequestInfra(execution, updateNetworkJsonIncoming)
			execution.setVariable("UPDNETI_networkRequest", networkRequest)
			utils.log("DEBUG", " network-request - " + '\n' + networkRequest, isDebugEnabled)

			String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable("UPDNETI_networkInputs", networkInputs)
			utils.log("DEBUG", " networkInputs - " + '\n' + networkInputs, isDebugEnabled)

			String netId = utils.getNodeText1(networkRequest, "network-id")
			String netName = utils.getNodeText1(networkRequest, "network-name")
			String networkOutputs = 
			   """<network-outputs>
                   <network-id>${netId}</network-id>			
                   <network-name>${netName}</network-name>
                 </network-outputs>"""
			execution.setVariable("UPDNETI_networkOutputs", networkOutputs)
			utils.log("DEBUG", " networkOutputs - " + '\n' + networkOutputs, isDebugEnabled)

			String requestId = execution.getVariable("att-mso-request-id")
			if (requestId == null || requestId == "") {
				requestId = execution.getVariable("requestId")
			}
			execution.setVariable("UPDNETI_requestId", requestId)
			execution.setVariable("UPDNETI_source", utils.getNodeText1(networkRequest, "source"))

			// prepare messageId
			String messageId = execution.getVariable("UPDNETI_messageId")  // for testing
			if (messageId == null || messageId == "") {
				messageId = UUID.randomUUID()
				utils.log("DEBUG", " UPDNETI_messageId, random generated: " + messageId, isDebugEnabled)
			} else {
				utils.log("DEBUG", " UPDNETI_messageId, pre-assigned: " + messageId, isDebugEnabled)
			}
			execution.setVariable("UPDNETI_messageId", messageId)

			// validate 'backout-on-failure' to override 'URN_mso_rollback'
			boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, networkRequest)
			execution.setVariable("UPDNETI_rollbackEnabled", rollbackEnabled)

			String networkId = ""
			if (utils.nodeExists(networkRequest, "network-id")) {
				networkId = utils.getNodeText1(networkRequest, "network-id")
				if (networkId == 'null' || networkId == "") {
					sendSyncError(execution)
					// missing value of networkId
					String dataErrorMessage = "Variable 'network-id' value/element is missing."
					utils.log("DEBUG", " Invalid Request - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				}
			}

			String lcpCloudRegion = ""
			if (utils.nodeExists(networkRequest, "aic-cloud-region")) {
				lcpCloudRegion = utils.getNodeText1(networkRequest, "aic-cloud-region")
			    if ((lcpCloudRegion == 'null') || (lcpCloudRegion == "")) {
					sendSyncError(execution)
					String dataErrorMessage = "requestDetails has missing 'aic-cloud-region' value/element."
					utils.log("DEBUG", " Invalid Request - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}

			String serviceInstanceId = ""
			if (utils.nodeExists(networkRequest, "service-instance-id")) {
				serviceInstanceId = utils.getNodeText1(networkRequest, "service-instance-id")
				if ((serviceInstanceId == 'null') || (lcpCloudRegion == "")) {
					sendSyncError(execution)
					String dataErrorMessage = "Variable 'serviceInstanceId' value/element is missing."
					utils.log("DEBUG", " Invalid Request - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}

			execution.setVariable("UPDNETI_serviceInstanceId", serviceInstanceId)

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			sendSyncError(execution)
			 // caught exception
			String exceptionMessage = "Exception Encountered in UpdateNetworkInstanceInfra, PreProcessRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncResponse of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

		try {
			String serviceInstanceId = execution.getVariable("UPDNETI_serviceInstanceId")
			String requestId = execution.getVariable("att-mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String updateNetworkRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSyncResponse to APIH - " + "\n" + updateNetworkRestRequest, isDebugEnabled)
			sendWorkflowResponse(execution, 202, updateNetworkRestRequest)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. sendSyncResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}

	public void callRESTQueryAAICloudRegion (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAICloudRegion of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {
			String networkInputs  = execution.getVariable("UPDNETI_networkInputs")
			String cloudRegion = utils.getNodeText1(networkInputs, "aic-cloud-region")
			cloudRegion = UriUtils.encode(cloudRegion,"UTF-8")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			utils.logAudit(queryCloudRegionRequest)
			execution.setVariable("UPDNETI_queryCloudRegionRequest", queryCloudRegionRequest)
			utils.log("DEBUG", " UPDNETI_queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest, isDebugEnabled)

			String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
			String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

			if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
				execution.setVariable("UPDNETI_cloudRegionPo", cloudRegionPo)
				execution.setVariable("UPDNETI_cloudRegionSdnc", cloudRegionSdnc)
				execution.setVariable("UPDNETI_isCloudRegionGood", true)

			} else {
			    String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable("UPDNETI_queryCloudRegionReturnCode")
			    utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			}

			utils.log("DEBUG", " is Cloud Region Good: " + execution.getVariable("UPDNETI_isCloudRegionGood"), isDebugEnabled)

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			// try error
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow - callRESTQueryAAICloudRegion() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkId(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkId of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String networkRequest = execution.getVariable("UPDNETI_networkRequest")
			String networkId   = utils.getNodeText1(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			execution.setVariable("UPDNETI_networkId", networkId)
			String messageId = execution.getVariable("UPDNETI_messageId")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String queryIdAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId
			utils.logAudit(queryIdAAIRequest)
			execution.setVariable("UPDNETI_queryIdAAIRequest", queryIdAAIRequest)
			utils.log("DEBUG", " UPDNETI_queryIdAAIRequest - " + "\n" + queryIdAAIRequest, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(queryIdAAIRequest);
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}

			APIResponse response = client.get()
			String returnCode = response.getStatusCode()
			execution.setVariable("UPDNETI_aaiIdReturnCode", returnCode)

			utils.log("DEBUG", " ***** AAI Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable("UPDNETI_queryIdAAIResponse", aaiResponseAsString)
				utils.log("DEBUG", " QueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString, isDebugEnabled)

			} else {
				if (returnCode=='404') {
					String dataErrorMessage = "Response Error from QueryAAINetworkId is 404 (Not Found)."
					utils.log("DEBUG", " AAI Query Failed. " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)
					   throw new BpmnError("MSOWorkflowException")

				   } else {
							// aai all errors
							String dataErrorMessage = "Unexpected Response from QueryAAINetworkId - " + returnCode
							utils.log("DEBUG", "Unexpected Response from QueryAAINetworkId - " + dataErrorMessage, isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				  }
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. callRESTQueryAAINetworkId() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTReQueryAAINetworkId(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTReQueryAAINetworkId of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String networkId   = utils.getNodeText1(execution.getVariable("UPDNETI_changeAssignSDNCResponse"), "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			String messageId = execution.getVariable("UPDNETI_messageId")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String requeryIdAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId
			utils.logAudit(requeryIdAAIRequest)
			execution.setVariable("UPDNETI_requeryIdAAIRequest", requeryIdAAIRequest)
			utils.log("DEBUG", " UPDNETI_requeryIdAAIRequest - " + "\n" + requeryIdAAIRequest, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(requeryIdAAIRequest);
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}

			APIResponse response = client.get()
			String returnCode = response.getStatusCode()
			execution.setVariable("UPDNETI_aaiRequeryIdReturnCode", returnCode)
			utils.log("DEBUG", " ***** AAI ReQuery Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable("UPDNETI_requeryIdAAIResponse", aaiResponseAsString)
				utils.log("DEBUG", " ReQueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString, isDebugEnabled)

				String netId = utils.getNodeText1(aaiResponseAsString, "network-id")
				String netName = utils.getNodeText1(aaiResponseAsString, "network-name")
				String networkOutputs =
				   """<network-outputs>
                   <network-id>${netId}</network-id>			
                   <network-name>${netName}</network-name>
                 </network-outputs>"""
				execution.setVariable("UPDNETI_networkOutputs", networkOutputs)
				utils.log("DEBUG", " networkOutputs - " + '\n' + networkOutputs, isDebugEnabled)
				
			} else {
				if (returnCode=='404') {
					String dataErrorMessage = "Response Error from ReQueryAAINetworkId is 404 (Not Found)."
					utils.log("DEBUG", " AAI ReQuery Failed. - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)
					   throw new BpmnError("MSOWorkflowException")

					   } else {
							// aai all errors
							String dataErrorMessage = "Unexpected Response from ReQueryAAINetworkId - " + returnCode
							utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

					}
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. callRESTReQueryAAINetworkId() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkVpnBinding(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkVpnBinding of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {

			// get variables
			String messageId = execution.getVariable("UPDNETI_messageId")
			String queryIdAAIResponse   = execution.getVariable("UPDNETI_requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			utils.log("DEBUG", " relationship - " + relationship, isDebugEnabled)

			// Check if Vnf Binding is present, then build a List of vnfBinding
			List vpnBindingUri = networkUtils.getVnfBindingObject(relationship)
			int vpnCount = vpnBindingUri.size()
			execution.setVariable("UPDNETI_vpnCount", vpnCount)
			utils.log("DEBUG", " UPDNETI_vpnCount - " + vpnCount, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (vpnCount > 0) {
				execution.setVariable("UPDNETI_vpnBindings", vpnBindingUri)
				utils.log("DEBUG", " vpnBindingUri List - " + vpnBindingUri, isDebugEnabled)

				String routeTargets = ""
				// AII loop call using list vpnBindings
				for (i in 0..vpnBindingUri.size()-1) {

					int counting = i+1

					// prepare url using vpnBinding
					String queryVpnBindingAAIRequest = ""
					String aai_uri = aaiUriUtil.getNetworkVpnBindingUri(execution)

					// Note: By default, the vpnBinding url is found in 'related-link' of the response,
					//       so, the default in URN mappings for this is set to "" (ie, space), unless forced to use the URN mapping.
					if (aai_uri == null || aai_uri == "") {
						// using value of 'related-link' from response
						if (vpnBindingUri[i].charAt(vpnBindingUri[i].length()-1) == '/') {
						    queryVpnBindingAAIRequest = "${aai_endpoint}" + vpnBindingUri[i].substring(0, vpnBindingUri[i].length()-1)
						} else {
						    queryVpnBindingAAIRequest = "${aai_endpoint}" + vpnBindingUri[i]
						}

					} else {
					    // using uri value in URN mapping
						String vpnBindingId = vpnBindingUri[i].substring(vpnBindingUri[i].indexOf("/vpn-binding/")+13, vpnBindingUri[i].length())
						if (vpnBindingId.charAt(vpnBindingId.length()-1) == '/') {
							vpnBindingId = vpnBindingId.substring(0, vpnBindingId.length()-1)
						}
					    queryVpnBindingAAIRequest = "${aai_endpoint}${aai_uri}/" + vpnBindingId
					}

					utils.logAudit(queryVpnBindingAAIRequest)
					execution.setVariable("UPDNETI_queryVpnBindingAAIRequest", queryVpnBindingAAIRequest)
					utils.log("DEBUG", " UPDNETI_queryVpnBindingAAIRequest, , vpnBinding #" + counting + " : " + "\n" + queryVpnBindingAAIRequest, isDebugEnabled)

					String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

					RESTConfig config = new RESTConfig(queryVpnBindingAAIRequest);
					RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
															  .addHeader("X-FromAppId", "MSO")
															  .addHeader("Content-Type", "application/xml")
															  .addHeader("Accept","application/xml");
					if (basicAuthCred != null && !"".equals(basicAuthCred)) {
						client.addAuthorizationHeader(basicAuthCred)
					}

					APIResponse response = client.get()
					String returnCode = response.getStatusCode()
					execution.setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", returnCode)
					utils.log("DEBUG", " ***** AAI query vpn binding Response Code, vpnBinding #" + counting + " : " + returnCode, isDebugEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						utils.logAudit(aaiResponseAsString)
						execution.setVariable("UPDNETI_queryVpnBindingAAIResponse", aaiResponseAsString)
						utils.log("DEBUG", " AAI Query Vpn Binding Success REST Response, , vpnBinding #" + counting + " : " + "\n" + aaiResponseAsString, isDebugEnabled)

						String routeTarget = ""
						if (utils.nodeExists(aaiResponseAsString, "global-route-target")) {
							routeTarget  = utils.getNodeText1(aaiResponseAsString, "global-route-target")
							routeTargets += "<routeTargets>" + routeTarget + "</routeTargets>" + '\n'
						}

					} else {
						if (returnCode=='404') {
							String dataErrorMessage = "Response Error from AAINetworkVpnBinding is 404 (Not Found)."
							utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						} else {
						   if (aaiResponseAsString.contains("RESTFault")) {
							   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							   execution.setVariable("WorkflowException", exceptionObject)
							   throw new BpmnError("MSOWorkflowException")

							   } else {
									// aai all errors
									String dataErrorMessage = " Unexpected Response from AAINetworkVpnBinding - " + returnCode
									utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							  }
						}
					}

				} // end loop

				execution.setVariable("UPDNETI_routeCollection", routeTargets)
				utils.log("DEBUG", " UPDNETI_routeCollection - " + '\n' + routeTargets, isDebugEnabled)

			} else {
				// reset return code to success
				execution.setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "200")
				String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
				String schemaVersion = aaiUriUtil.getNamespaceFromUri(aai_uri)
			    String aaiStubResponse =
					"""	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<vpn-binding xmlns="${schemaVersion}">
						      <global-route-target/>
							</vpn-binding>
						</rest:payload>"""
				String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
				execution.setVariable("UPDNETI_queryVpnBindingAAIResponse", aaiStubResponseAsXml)
				execution.setVariable("UPDNETI_routeCollection", "<routeTargets/>")
				utils.log("DEBUG", " No vpnBinding, using this stub as response - " + '\n' + aaiStubResponseAsXml, isDebugEnabled)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. callRESTQueryAAINetworkVpnBinding() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkPolicy(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkPolicy of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String messageId = execution.getVariable("UPDNETI_messageId")
			String queryIdAAIResponse   = execution.getVariable("UPDNETI_requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			utils.log("DEBUG", " relationship - " + relationship, isDebugEnabled)

			// Check if Network Policy is present, then build a List of network policy
			List networkPolicyUriList = networkUtils.getNetworkPolicyObject(relationship)
			int networkPolicyCount = networkPolicyUriList.size()
			execution.setVariable("UPDNETI_networkPolicyCount", networkPolicyCount)
			utils.log("DEBUG", " UPDNETI_networkPolicyCount - " + networkPolicyCount, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkPolicyCount > 0) {
				execution.setVariable("UPDNETI_networkPolicyUriList", networkPolicyUriList)
				utils.log("DEBUG", " networkPolicyUri List - " + networkPolicyUriList, isDebugEnabled)

				String networkPolicies = ""
				// AII loop call using list vpnBindings
				for (i in 0..networkPolicyUriList.size()-1) {

					int counting = i+1

					// prepare url using vpnBinding
					String queryNetworkPolicyAAIRequest = ""

					String aai_uri = aaiUriUtil.getNetworkPolicyUri(execution)

					// Note: By default, the network policy url is found in 'related-link' of the response,
					//       so, the default in URN mappings for this is set to "" (ie, space), unless forced to use the URN mapping.
					if (aai_uri == null || aai_uri == "") {
						// using value of 'related-link' from response
						if (networkPolicyUriList[i].charAt(networkPolicyUriList[i].length()-1) == '/') {
							queryNetworkPolicyAAIRequest = "${aai_endpoint}" + networkPolicyUriList[i].substring(0, networkPolicyUriList[i].length()-1)
						} else {
							queryNetworkPolicyAAIRequest = "${aai_endpoint}" + networkPolicyUriList[i]
						}
					} else {
						// using uri value in URN mapping
						String networkPolicyId = networkPolicyUriList[i].substring(networkPolicyUriList[i].indexOf("/network-policy/")+16, networkPolicyUriList[i].length())
						println " networkPolicyId - " + networkPolicyId
						if (networkPolicyId.charAt(networkPolicyId.length()-1) == '/') {
							networkPolicyId = networkPolicyId.substring(0, networkPolicyId.length()-1)
						}
						queryNetworkPolicyAAIRequest = "${aai_endpoint}${aai_uri}/" + networkPolicyId

					}


					utils.logAudit(queryNetworkPolicyAAIRequest)
					execution.setVariable("UPDNETI_queryNetworkPolicyAAIRequest", queryNetworkPolicyAAIRequest)
					utils.log("DEBUG", " UPDNETI_queryNetworkPolicyAAIRequest, , NetworkPolicy #" + counting + " : " + "\n" + queryNetworkPolicyAAIRequest, isDebugEnabled)

					String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

					RESTConfig config = new RESTConfig(queryNetworkPolicyAAIRequest);
					RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
															  .addHeader("X-FromAppId", "MSO")
															  .addHeader("Content-Type", "application/xml")
															  .addHeader("Accept","application/xml");
					if (basicAuthCred != null && !"".equals(basicAuthCred)) {
						client.addAuthorizationHeader(basicAuthCred)
					}

					APIResponse response = client.get()
					String returnCode = response.getStatusCode()
					execution.setVariable("UPDNETI_aaiQqueryNetworkPolicyReturnCode", returnCode)
					utils.log("DEBUG", " ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode, isDebugEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						utils.logAudit(aaiResponseAsString)
						execution.setVariable("UPDNETI_queryNetworkPolicyAAIResponse", aaiResponseAsString)
						utils.log("DEBUG", " QueryAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsString, isDebugEnabled)

						String networkPolicy = ""
						if (utils.nodeExists(aaiResponseAsString, "network-policy-fqdn")) {
							networkPolicy  = utils.getNodeText1(aaiResponseAsString, "network-policy-fqdn")
							networkPolicies += "<policyFqdns>" + networkPolicy + "</policyFqdns>" + '\n'
						}

					} else {
						if (returnCode=='404') {
							String dataErrorMessage = "Response Error from QueryAAINetworkPolicy is 404 (Not Found)."
							utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						} else {
						   if (aaiResponseAsString.contains("RESTFault")) {
							   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							   execution.setVariable("WorkflowException", exceptionObject)
							   throw new BpmnError("MSOWorkflowException")

							   } else {
									// aai all errors
									String dataErrorMessage = "Unexpected Response from QueryAAINetworkPolicy - " + returnCode
									utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							  }
						}
					}

				} // end loop

				execution.setVariable("UPDNETI_networkCollection", networkPolicies)
				utils.log("DEBUG", " UPDNETI_networkCollection - " + '\n' + networkPolicies, isDebugEnabled)

			} else {
				// reset return code to success
				execution.setVariable("UPDNETI_aaiQqueryNetworkPolicyReturnCode", "200")
				String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
				String schemaVersion = aaiUriUtil.getNamespaceFromUri(aai_uri)
				String aaiStubResponse =
					"""	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<network-policy xmlns="${schemaVersion}">
							  <network-policy-fqdn/>
                            </network-policy>
						</rest:payload>"""
				String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
				execution.setVariable("UPDNETI_queryNetworkPolicyAAIResponse", aaiStubResponseAsXml)
				execution.setVariable("UPDNETI_networkCollection", "<policyFqdns/>")
				utils.log("DEBUG", " No net policies, using this stub as response - " + '\n' + aaiStubResponseAsXml, isDebugEnabled)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. callRESTQueryAAINetworkPolicy() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkTableRef(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkTableRef of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String messageId = execution.getVariable("UPDNETI_messageId")
			String queryIdAAIResponse   = execution.getVariable("UPDNETI_requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			utils.log("DEBUG", " relationship - " + relationship, isDebugEnabled)

			// Check if Network TableREf is present, then build a List of network policy
			List networkTableRefUriList = networkUtils.getNetworkTableRefObject(relationship)
			int networkTableRefCount = networkTableRefUriList.size()
			execution.setVariable("UPDNETI_networkTableRefCount", networkTableRefCount)
			utils.log("DEBUG", " UPDNETI_networkTableRefCount - " + networkTableRefCount, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkTableRefCount > 0) {
				execution.setVariable("UPDNETI_networkTableRefUriList", networkTableRefUriList)
				utils.log("DEBUG", " networkTableRefUri List - " + networkTableRefUriList, isDebugEnabled)

				// AII loop call using list vpnBindings
				String networkTableRefs = ""
				for (i in 0..networkTableRefUriList.size()-1) {

					int counting = i+1

					// prepare url using tableRef
					String queryNetworkTableRefAAIRequest = ""

					String aai_uri = aaiUriUtil.getNetworkTableReferencesUri(execution)

					// Note: By default, the network policy url is found in 'related-link' of the response,
					//       so, the default in URN mappings for this is set to "" (ie, space), unless forced to use the URN mapping.
					if (aai_uri == null || aai_uri == "") {
						// using value of 'related-link' from response
						if (networkTableRefUriList[i].charAt(networkTableRefUriList[i].length()-1) == '/') {
							queryNetworkTableRefAAIRequest = "${aai_endpoint}" + networkTableRefUriList[i].substring(0, networkTableRefUriList[i].length()-1)
						} else {
							queryNetworkTableRefAAIRequest = "${aai_endpoint}" + networkTableRefUriList[i]
						}
					} else {
						// using uri value in URN mapping
						String networkTableRefId = networkTableRefUriList[i].substring(networkTableRefUriList[i].indexOf("/route-table-reference/")+23, networkTableRefUriList[i].length())

						if (networkTableRefId.charAt(networkTableRefId.length()-1) == '/') {
							networkTableRefId = networkTableRefId.substring(0, networkTableRefId.length()-1)
						}
						queryNetworkTableRefAAIRequest = "${aai_endpoint}${aai_uri}/" + networkTableRefId

					}


					utils.logAudit(queryNetworkTableRefAAIRequest)
					execution.setVariable("UPDNETI_queryNetworkTableRefAAIRequest", queryNetworkTableRefAAIRequest)
					utils.log("DEBUG", " UPDNETI_queryNetworkTableRefAAIRequest, , NetworkTableRef #" + counting + " : " + "\n" + queryNetworkTableRefAAIRequest, isDebugEnabled)

					String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

					RESTConfig config = new RESTConfig(queryNetworkTableRefAAIRequest);
					RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
															  .addHeader("X-FromAppId", "MSO")
															  .addHeader("Content-Type", "application/xml")
															  .addHeader("Accept","application/xml");
					if (basicAuthCred != null && !"".equals(basicAuthCred)) {
						client.addAuthorizationHeader(basicAuthCred)
					}

					APIResponse response = client.get()
					String returnCode = response.getStatusCode()
					execution.setVariable("UPDNETI_aaiQqueryNetworkTableRefReturnCode", returnCode)
					utils.log("DEBUG", " ***** AAI query network Table Reference Response Code, NetworkTableRef #" + counting + " : " + returnCode, isDebugEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						utils.logAudit(aaiResponseAsString)
						execution.setVariable("UPDNETI_queryNetworkTableRefAAIResponse", aaiResponseAsString)
						utils.log("DEBUG", " QueryAAINetworkTableRef Success REST Response, , NetworkTableRef #" + counting + " : " + "\n" + aaiResponseAsString, isDebugEnabled)

						String networkTableRef = ""
						if (utils.nodeExists(aaiResponseAsString, "route-table-reference-fqdn")) {
							networkTableRef  = utils.getNodeText1(aaiResponseAsString, "route-table-reference-fqdn")
							networkTableRefs += "<routeTableFqdns>" + networkTableRef + "</routeTableFqdns>" + '\n'
						}

					} else {
						if (returnCode=='404') {
							String dataErrorMessage = "Response Error from QueryAAINetworkTableRef is 404 (Not Found)."
							utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
							
						} else {
						   if (aaiResponseAsString.contains("RESTFault")) {
							   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							   execution.setVariable("WorkflowException", exceptionObject)
							   throw new BpmnError("MSOWorkflowException")
		
							   } else {
									// aai all errors
									String dataErrorMessage = "Unexpected Response from QueryAAINetworkTableRef - " + returnCode
									utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							  }
						}
					}

				} // end loop

				execution.setVariable("UPDNETI_tableRefCollection", networkTableRefs)
				utils.log("DEBUG", " UPDNETI_tableRefCollection - " + '\n' + networkTableRefs, isDebugEnabled)

			} else {
				// reset return code to success
				execution.setVariable("UPDNETI_aaiQqueryNetworkTableRefReturnCode", "200")
				String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
				String schemaVersion = aaiUriUtil.getNamespaceFromUri(aai_uri)
				String aaiStubResponse =
					"""	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<route-table-references xmlns="${schemaVersion}">
							  <route-table-reference-fqdn/>
                            </route-table-references>
						</rest:payload>"""
				String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
				execution.setVariable("UPDNETI_queryNetworkTableRefAAIResponse", aaiStubResponseAsXml)
				execution.setVariable("UPDNETI_tableRefCollection", "<routeTableFqdns/>")
				utils.log("DEBUG", " No net table references, using this stub as response - " + '\n' + aaiStubResponseAsXml, isDebugEnabled)

			}

		} catch (BpmnError e) {
			throw e;
			
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. callRESTQueryAAINetworkTableRef() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	public void callRESTUpdateContrailAAINetwork(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTUpdateContrailAAINetwork of UpdateNetworkInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String networkId   = utils.getNodeText1(execution.getVariable("UPDNETI_changeAssignSDNCResponse"), "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			String requeryIdAAIResponse   = execution.getVariable("UPDNETI_requeryIdAAIResponse")
			String updateNetworkResponse   = execution.getVariable("UPDNETI_updateNetworkResponse")
			String messageId = execution.getVariable("UPDNETI_messageId")

			// Prepare url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String updateContrailAAIUrlRequest = "${aai_endpoint}${aai_uri}/" + networkId

			utils.logAudit(updateContrailAAIUrlRequest)
			execution.setVariable("UPDNETI_updateContrailAAIUrlRequest", updateContrailAAIUrlRequest)
			utils.log("DEBUG", " UPDNETI_updateContrailAAIUrlRequest - " + "\n" + updateContrailAAIUrlRequest, isDebugEnabled)

			//Prepare payload (PUT)
			String schemaVersion = aaiUriUtil.getNamespaceFromUri(aai_uri)
			String payload = networkUtils.ContrailNetworkCreatedUpdate(requeryIdAAIResponse, updateNetworkResponse, schemaVersion)
			String payloadXml = utils.formatXml(payload)
			utils.logAudit(payloadXml)
			execution.setVariable("UPDNETI_updateContrailAAIPayloadRequest", payloadXml)
			utils.log("DEBUG", " 'payload' to Update Contrail - " + "\n" + payloadXml, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			RESTConfig config = new RESTConfig(updateContrailAAIUrlRequest);
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}

			APIResponse response = client.httpPut(payload)
			String returnCode = response.getStatusCode()
			execution.setVariable("UPDNETI_aaiUpdateContrailReturnCode", returnCode)

			utils.log("DEBUG", " ***** AAI Update Contrail Response Code  : " + returnCode, isDebugEnabled)

			String aaiUpdateContrailResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				utils.logAudit(aaiUpdateContrailResponseAsString)
				execution.setVariable("UPDNETI_updateContrailAAIResponse", aaiUpdateContrailResponseAsString)
				utils.log("DEBUG", " AAI Update Contrail Success REST Response - " + "\n" + aaiUpdateContrailResponseAsString, isDebugEnabled)
				// Point-of-no-return is set to false, rollback not needed.
				execution.setVariable("UPDNETI_isPONR", true)

			} else {
				if (returnCode=='404') {
					String dataErrorMessage = " Response Error from UpdateContrailAAINetwork is 404 (Not Found)."
					utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiUpdateContrailResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiUpdateContrailResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)
					   throw new BpmnError("MSOWorkflowException")

					   } else {
							// aai all errors
							String errorMessage = "Unexpected Response from UpdateContrailAAINetwork - " + returnCode
							utils.log("DEBUG", errorMessage, isDebugEnabled)
							exceptionUtil.buildAndThrowWorkflowException(execution, "2500", errorMessage)
					  }
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstanceInfra flow. callRESTUpdateContrailAAINetwork() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareUpdateNetworkRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareUpdateNetworkRequest of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

		try {

			// get variables
			String requestId = execution.getVariable("UPDNETI_requestId")
			String messageId = execution.getVariable("UPDNETI_messageId")
			String source    = execution.getVariable("UPDNETI_source")

			String requestInput = execution.getVariable("UPDNETI_networkRequest")
			String queryIdResponse = execution.getVariable("UPDNETI_requeryIdAAIResponse")
			String cloudRegionId = execution.getVariable("UPDNETI_cloudRegionPo")
			String backoutOnFailure = execution.getVariable("UPDNETI_rollbackEnabled")

			// Prepare Network request
			String routeCollection = execution.getVariable("UPDNETI_routeCollection")
			String policyCollection = execution.getVariable("UPDNETI_networkCollection")
			String tableCollection = execution.getVariable("UPDNETI_tableRefCollection")
			String updateNetworkRequest = networkUtils.UpdateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyCollection, tableCollection, cloudRegionId, backoutOnFailure, source )
			// Format Response
			String buildUpdateNetworkRequestAsString = utils.formatXml(updateNetworkRequest)
			buildUpdateNetworkRequestAsString = buildUpdateNetworkRequestAsString.replace(":w1aac13n0", "").replace("w1aac13n0:", "")
			utils.logAudit(buildUpdateNetworkRequestAsString)

			execution.setVariable("UPDNETI_updateNetworkRequest", buildUpdateNetworkRequestAsString)
			utils.log("DEBUG", " UPDNETI_updateNetworkRequest - " + "\n" +  buildUpdateNetworkRequestAsString, isDebugEnabled)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. prepareUpdateNetworkRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareSDNCRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRequest of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

		try {
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String updateNetworkInput = execution.getVariable("UPDNETI_networkRequest")
			String cloudRegionId = execution.getVariable("UPDNETI_cloudRegionSdnc")

			String networkId = ""
			if (utils.nodeExists(updateNetworkInput, "network-id")) {
			   networkId = utils.getNodeText1(updateNetworkInput, "network-id")
			}
			if (networkId == null) {networkId = ""}

			String serviceInstanceId = utils.getNodeText1(updateNetworkInput, "service-instance-id")

			// 1. prepare assign topology via SDNC Adapter SUBFLOW call
 		   	String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "changeassign", "NetworkActivateRequest", cloudRegionId, networkId, null)

			String sndcTopologyUpdateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
			utils.logAudit(sndcTopologyUpdateRequesAsString)
			execution.setVariable("UPDNETI_changeAssignSDNCRequest", sndcTopologyUpdateRequesAsString)
			utils.log("DEBUG", " UPDNETI_changeAssignSDNCRequest - " + "\n" +  sndcTopologyUpdateRequesAsString, isDebugEnabled)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. prepareSDNCRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareDBRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequest of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

			String networkOutputs = execution.getVariable("UPDNETI_networkOutputs")
			String networkName = ""
			try {
				 networkName = utils.getNodeText1(networkOutputs, "network-name")
				 if (networkName == null) {networkName = ""}
			} catch (Exception ex) {
				networkName = ""
				utils.log("DEBUG", " No 'network-name' found in '<network-outputs>' ! ", isDebugEnabled)
			}
			String networkId = ""
			try {
				networkId = utils.getNodeText1(networkOutputs, "network-id")
				if (networkId == null) {networkId = ""}
			} catch (Exception) {
				networkId = ""
				utils.log("DEBUG", " No 'network-id' found in '<network-outputs>' ! ", isDebugEnabled)
			}
			String requestId = execution.getVariable("UPDNETI_requestId")

			String statusMessage = "Network successfully updated."

			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://com.att.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>COMPLETED</requestStatus>
								<progress>100</progress>
								<vnfOutputs>&lt;network-id&gt;${networkId}&lt;/network-id&gt;&lt;network-name&gt;${networkName}&lt;/network-names&gt;</vnfOutputs>
                                <networkId>${networkId}</networkId>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("UPDNETI_updateDBRequest", buildDeleteDBRequestAsString)
		   utils.log("DEBUG", " DB Adapter Request - " + "\n" + buildDeleteDBRequestAsString, isDebugEnabled)
		   utils.logAudit(buildDeleteDBRequestAsString)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. prepareDBRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	public void prepareDBRequestError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequestError of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

			String statusMessage = ""
			WorkflowException wfe = null
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				wfe = execution.getVariable("WorkflowException")
				statusMessage = wfe.getErrorMessage()
			} else {
			   String workflowException = execution.getVariable("WorkflowException")
			   try {
				  statusMessage = utils.getNodeText1(workflowException, "ErrorMessage")
			   } catch (Exception ex) {
			  	  statusMessage = "Encountered Error during DB Update. " + ex.getMessage()
			   }
			}
			String networkOutputs = execution.getVariable("UPDNETI_networkOutputs")
			String requestId = execution.getVariable("UPDNETI_requestId")
			String networkId = ""
			try {
				networkId = utils.getNodeText1(networkOutputs, "network-id")
				if (networkId == null) {networkId = ""}
			} catch (Exception) {
				networkId = ""
				utils.log("DEBUG", " No 'network-id' found in '<network-outputs>' ! ", isDebugEnabled)
			}
			String networkName = ""
			try {
				 networkName = utils.getNodeText1(networkOutputs, "network-name")
				 if (networkName == null) {networkName = ""}
			} catch (Exception ex) {
				networkName = ""
				utils.log("DEBUG", " No 'network-name' found in '<network-outputs>' ! ", isDebugEnabled)
			}
			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://com.att.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs>&lt;network-id&gt;${networkId}&lt;/network-id&gt;&lt;network-name&gt;${networkName}&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   execution.setVariable("UPDNETI_updateDBRequest", dbRequest)
		   utils.log("DEBUG", " DB Adapter Request - " + "\n" + dbRequest, isDebugEnabled)
		   utils.logAudit(dbRequest)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. prepareDBRequestError() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }


	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void validateUpdateNetworkResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateUpdateNetworkResponse of UpdateNetworkInstanceInfra *****", isDebugEnabled)

		try {
			String returnCode = execution.getVariable("UPDNETI_networkReturnCode")
			String networkResponse = execution.getVariable("UPDNETI_updateNetworkResponse")
			if (networkResponse==null)	{
				networkResponse="" // reset
			}

			utils.log("DEBUG", " Network Adapter update responseCode: " + returnCode, isDebugEnabled)

			String errorMessage = ""
			if (returnCode == "200") {
				execution.setVariable("UPDNETI_isNetworkRollbackNeeded", true)
				utils.logAudit(networkResponse)
				execution.setVariable("UPDNETI_updateNetworkResponse", networkResponse)
				utils.log("DEBUG", " Network Adapter update Success Response - " + "\n" + networkResponse, isDebugEnabled)

				// prepare rollback data
				String rollbackData = utils.getNodeXml(networkResponse, "rollback", false).replace("tag0:","").replace(":tag0","")
  				String rollbackNetwork =
					"""<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://com.att.mso/network">
							${rollbackData}
						</NetworkAdapter:rollbackNetwork>"""
				String rollbackNetworkXml = utils.formatXml(rollbackNetwork)
				execution.setVariable("UPDNETI_rollbackNetworkRequest", rollbackNetworkXml)
				utils.log("DEBUG", " Network Adapter rollback data - " + "\n" + rollbackNetworkXml, isDebugEnabled)

			} else { // network error
			   if (returnCode.toInteger() > 399 && returnCode.toInteger() < 600) {   //4xx, 5xx
				   if (networkResponse.contains("updateNetworkError")) {
					   networkResponse = networkResponse.replace('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>', '')
					   errorMessage = utils.getNodeText1(networkResponse, "message")
					   errorMessage  = "Received error from Network Adapter: " + errorMessage
					   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)

				   } else { // CatchAll exception
				   	   if (returnCode == "500") {
						   errorMessage = "JBWEB000065: HTTP Status 500."
				       } else {
					       errorMessage = "Return code is " + returnCode
				       }
					   errorMessage  = "Received error from Network Adapter: " + errorMessage
					   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)

				   }

			   } else { // CatchAll exception
				   String dataErrorMessage  = "Received error from Network Adapter. Return code is: " + returnCode
				   exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			   }

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. validateUpdateNetworkResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}

	public void validateSDNCResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateSDNCResponse of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

		String response = execution.getVariable("UPDNETI_changeAssignSDNCResponse")
		WorkflowException workflowException = null
		try {
			workflowException = execution.getVariable("UPDNETI_WorkflowException")
			//execution.setVariable("WorkflowException", workflowException)
		} catch (Exception ex) {
			utils.log("DEBUG", " Sdnc 'WorkflowException' object is empty or null. ", isDebugEnabled)
		}

		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String changeAssignSDNCResponseDecodeXml = sdncAdapterUtils.decodeXML(execution.getVariable("UPDNETI_changeAssignSDNCResponse"))
		changeAssignSDNCResponseDecodeXml = changeAssignSDNCResponseDecodeXml.replace("&", "&amp;").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable("UPDNETI_changeAssignSDNCResponse", changeAssignSDNCResponseDecodeXml)

		if (execution.getVariable("UPDNETI_sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
			execution.setVariable("UPDNETI_isSdncRollbackNeeded", true)
			utils.log("DEBUG", "Successfully Validated SDNC Response", isDebugEnabled)

		} else {
			utils.log("DEBUG", "Did NOT Successfully Validated SDNC Response", isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
		}

	}


	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

		try {
			// Display DB response: UPDNETI_updateDBResponse / UPDNETI_dbReturnCode
			String dbReturnCode = execution.getVariable("UPDNETI_dbReturnCode")
			utils.log("DEBUG", " ***** DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
			String updateDBResponse =  execution.getVariable("UPDNETI_updateDBResponse")
			utils.log("DEBUG", " ***** DB Update Response String: " + '\n' + updateDBResponse, isDebugEnabled)
			utils.logAudit(updateDBResponse)

			String source = execution.getVariable("UPDNETI_source")
			String requestId = execution.getVariable("UPDNETI_requestId")

			String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
								xmlns:ns="http://ecomp.att.com/mso/request/types/v1">
						<request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
							<request-id>${requestId}</request-id>
							<action>UPDATE</action>
							<source>${source}</source>
			   			</request-info>
			   			<aetgt:mso-bpel-name>BPMN Network action: UPDATE</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

				// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			if (dbReturnCode == "200") {
				utils.logAudit(updateDBResponse)
				utils.logAudit(xmlMsoCompletionRequest)
				execution.setVariable("UPDNETI_Success", true)
				execution.setVariable("UPDNETI_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
				utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
				
			} else {
				String errorMessage = " DB Update failed, code: " + dbReturnCode
				utils.log("DEBUG", errorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
				
			}


		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. postProcessResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }


	}

	public void prepareSDNCRollbackRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRollbackRequest of UpdateNetworkInstanceInfra ***** ", isDebugEnabled)

		try {
			// for some reason the WorkflowException object is null after the sdnc rollback call task, need to save WorkflowException. 
			execution.setVariable("UPDNETI_WorkflowException", execution.getVariable("WorkflowException"))
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String updateNetworkInput = execution.getVariable("UPDNETI_networkRequest")
			String cloudRegionId = execution.getVariable("UPDNETI_cloudRegionSdnc")
			String changeAssignSDNCResponse = execution.getVariable("UPDNETI_changeAssignSDNCResponse")
			String networkId = utils.getNodeText1(changeAssignSDNCResponse, "network-id")

			String serviceInstanceId = utils.getNodeText1(updateNetworkInput, "service-instance-id")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "rollback", "NetworkActivateRequest", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
			execution.setVariable("UPDNETI_rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
			utils.log("DEBUG", " Preparing request for SDNC Topology assign's rollback/compensation . . . - " + "\n" +  sndcTopologyRollbackRequestAsString, isDebugEnabled)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. prepareSDNCRollbackRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void validateRollbackResponses (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			// Note: Updates do not support rollback.  To restore a previous state, another Update would be needed to revert to the original settings.  
			// This would need to be managed by the client.
			String rollbackNetworkErrorMessages = ""
			Boolean isNetworkRollbackNeeded = execution.getVariable("UPDNETI_isNetworkRollbackNeeded")
			if (isNetworkRollbackNeeded == true) {
				rollbackNetworkErrorMessages = " + PO Network rollback is not supported for Update. Submit another Update to restore/rollback."
			}

			// validate SDNC rollback response
			String rollbackSdncErrorMessages = ""
			Boolean isSdncRollbackNeeded = execution.getVariable("UPDNETI_isSdncRollbackNeeded")
			if (isSdncRollbackNeeded == true) {
				String rollbackSDNCReturnCode = execution.getVariable("UPDNETI_rollbackSDNCReturnCode")
				String rollbackSDNCReturnInnerCode = ""
				String rollbackSDNCResponse = execution.getVariable("UPDNETI_rollbackSDNCResponse")
				SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
				rollbackSDNCResponse = sdncAdapterUtils.decodeXML(rollbackSDNCResponse)
				rollbackSDNCResponse = rollbackSDNCResponse.replace("&", "&amp;").replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")

				if (rollbackSDNCReturnCode == "200") {
					if (utils.nodeExists(rollbackSDNCResponse, "response-code")) {
						rollbackSDNCReturnInnerCode = utils.getNodeText1(rollbackSDNCResponse, "response-code")
						if (rollbackSDNCReturnInnerCode == "200" || rollbackSDNCReturnInnerCode == "" || rollbackSDNCReturnInnerCode == "0") {
						rollbackSdncErrorMessages = " + SNDC rollback completed."
						} else {
							rollbackSdncErrorMessages = " + SDNC rollback failed. "
						}
					} else {
					  rollbackSdncErrorMessages = " + SNDC rollback completed."
					}
				} else {
					  rollbackSdncErrorMessages = " + SDNC rollback failed. "
 				}

				utils.log("DEBUG", " SDNC rollback Code - " + rollbackSDNCReturnCode, isDebugEnabled)
				utils.log("DEBUG", " SDNC rollback  Response - " + rollbackSDNCResponse, isDebugEnabled)

			}

			//WorkflowException wfe = execution.getVariable("WorkflowException")
			//String statusMessage = wfe.getErrorMessage()
			//int errorCode = wfe.getErrorCode()
			
			String statusMessage = ""
			int errorCode = 0
			WorkflowException wfe = execution.getVariable("WorkflowException")
			if (wfe instanceof WorkflowException) { 
			  statusMessage = wfe.getErrorMessage()
			  errorCode = wfe.getErrorCode()
			} else {
			   if (execution.getVariable("UPDNETI_WorkflowException") instanceof WorkflowException) {
				  // get saved WorkflowException  
			      WorkflowException swfe = execution.getVariable("UPDNETI_WorkflowException")
			      statusMessage = swfe.getErrorMessage()
			      errorCode =  swfe.getErrorCode()
			   } else {
			      statusMessage = "Encountered Error, please see previous tasks/activities/steps for error messages."
			      errorCode = 7000
		       }   
			} 

			// recreate WorkflowException to include the rollback Message
			statusMessage =  statusMessage + rollbackNetworkErrorMessages + rollbackSdncErrorMessages
			exceptionUtil.buildWorkflowException(execution, errorCode, statusMessage)

		} catch (Exception ex) {
			execution.setVariable("WorkflowException", null)
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstanceInfra flow. validateRollbackResponses() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}



	}

	// *******************************
	//     Build Error Section
	// *******************************

	// Prepare for FalloutHandler
	public void buildErrorResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler. *****", isDebugEnabled)

		String dbReturnCode = execution.getVariable("UPDNETI_dbReturnCode")
		utils.log("DEBUG", " ***** DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
		String updateDBResponse =  execution.getVariable("UPDNETI_updateDBResponse")
		utils.log("DEBUG", " ***** DB Update Response String: " + '\n' + updateDBResponse, isDebugEnabled)
		utils.logAudit(updateDBResponse)

		String falloutHandlerRequest = ""
		String requestId = execution.getVariable("UPDNETI_requestId")
		String source = execution.getVariable("UPDNETI_source")
		try {
			execution.setVariable("UPDNETI_Success", false)
			WorkflowException wfe = execution.getVariable("WorkflowException")
			String errorCode = String.valueOf(wfe.getErrorCode())
			String errorMessage = wfe.getErrorMessage()
			falloutHandlerRequest =
			    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>UPDATE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.logAudit(falloutHandlerRequest)
			execution.setVariable("UPDNETI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("DEBUG", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		} catch (Exception ex) {
			String errorException = "  Bpmn error encountered in UpdateNetworkInstanceInfra flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
			utils.log("DEBUG", errorException, isDebugEnabled)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>UPDATE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorException}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable("UPDNETI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("DEBUG", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		}

	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {

			String requestId = execution.getVariable("att-mso-request-id")

			// REST Error (for API Handler (APIH) Reply Task)
			String syncError = """{"requestReferences":{"instanceId":"","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 500, syncError)

		} catch (Exception ex) {
			utils.log("DEBUG", " Bpmn error encountered in UpdateNetworkInstanceInfra flow. sendSyncError() - " + ex.getMessage(), isDebugEnabled)
		}

	}


	public void processJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}

}
