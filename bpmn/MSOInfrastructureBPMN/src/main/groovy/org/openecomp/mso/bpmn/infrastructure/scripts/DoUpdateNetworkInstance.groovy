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
package org.openecomp.mso.bpmn.infrastructure.scripts;

import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>DoUpdateNetworkInstance.bpmn</class> process.
 *
 */
public class DoUpdateNetworkInstance extends AbstractServiceTaskProcessor {
	String Prefix="UPDNETI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DoUpdateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(DelegateExecution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "messageId", "")
		execution.setVariable("BasicAuthHeaderValuePO", "")
		execution.setVariable("BasicAuthHeaderValueSDNC", "")
		execution.setVariable(Prefix + "networkRequest", "")
		execution.setVariable(Prefix + "networkInputs", "")
		execution.setVariable(Prefix + "networkOutputs", "")
		execution.setVariable(Prefix + "requestId", "")
		execution.setVariable(Prefix + "source", "")
		execution.setVariable(Prefix + "networkId", "")

		execution.setVariable(Prefix + "isPONR", false)    // Point-of-no-return, means, rollback is not needed

		// AAI query Cloud Region
		execution.setVariable(Prefix + "queryCloudRegionRequest","")
		execution.setVariable(Prefix + "queryCloudRegionReturnCode","")
		execution.setVariable(Prefix + "queryCloudRegionResponse","")
		execution.setVariable(Prefix + "cloudRegionPo","")
		execution.setVariable(Prefix + "cloudRegionSdnc","")
		execution.setVariable(Prefix + "isCloudRegionGood", false)

		// AAI query Id
		execution.setVariable(Prefix + "queryIdAAIRequest","")
		execution.setVariable(Prefix + "queryIdAAIResponse", "")
		execution.setVariable(Prefix + "aaiIdReturnCode", "")

		// AAI query vpn binding
		execution.setVariable(Prefix + "queryVpnBindingAAIRequest","")
		execution.setVariable(Prefix + "queryVpnBindingAAIResponse", "")
		execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "")
		execution.setVariable(Prefix + "vpnBindings", null)
		execution.setVariable(Prefix + "vpnCount", 0)
		execution.setVariable(Prefix + "routeCollection", "")

		// AAI query network policy
		execution.setVariable(Prefix + "queryNetworkPolicyAAIRequest","")
		execution.setVariable(Prefix + "queryNetworkPolicyAAIResponse", "")
		execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "")
		execution.setVariable(Prefix + "networkPolicyUriList", null)
		execution.setVariable(Prefix + "networkPolicyCount", 0)
		execution.setVariable(Prefix + "networkCollection", "")

		// AAI query route table reference
		execution.setVariable(Prefix + "queryNetworkTableRefAAIRequest","")
		execution.setVariable(Prefix + "queryNetworkTableRefAAIResponse", "")
		execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "")
		execution.setVariable(Prefix + "networkTableRefUriList", null)
		execution.setVariable(Prefix + "networkTableRefCount", 0)
		execution.setVariable(Prefix + "tableRefCollection", "")
		
		// AAI requery Id
		execution.setVariable(Prefix + "requeryIdAAIRequest","")
		execution.setVariable(Prefix + "requeryIdAAIResponse", "")
		execution.setVariable(Prefix + "aaiRequeryIdReturnCode", "")

		// AAI update contrail
		execution.setVariable(Prefix + "updateContrailAAIUrlRequest","")
		execution.setVariable(Prefix + "updateContrailAAIPayloadRequest","")
		execution.setVariable(Prefix + "updateContrailAAIResponse", "")
		execution.setVariable(Prefix + "aaiUpdateContrailReturnCode", "")

		execution.setVariable(Prefix + "updateNetworkRequest", "")
		execution.setVariable(Prefix + "updateNetworkResponse", "")
		execution.setVariable(Prefix + "rollbackNetworkRequest", "")
		execution.setVariable(Prefix + "networkReturnCode", "")
		execution.setVariable(Prefix + "isNetworkRollbackNeeded", false)

		execution.setVariable(Prefix + "changeAssignSDNCRequest", "")
		execution.setVariable(Prefix + "changeAssignSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackSDNCRequest", "")
		execution.setVariable(Prefix + "sdncReturnCode", "")
		execution.setVariable(Prefix + "isSdncRollbackNeeded", false)
		execution.setVariable(Prefix + "sdncResponseSuccess", false)

		execution.setVariable(Prefix + "isVnfBindingPresent", false)
		execution.setVariable(Prefix + "Success", false)
		execution.setVariable(Prefix + "serviceInstanceId", "")
		
		execution.setVariable(Prefix + "isException", false)
		
	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoUpdateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest DoUpdateNetworkInstance Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)
	
			// GET Incoming request & validate 3 kinds of format.
			execution.setVariable("action", "UPDATE")
			String networkRequest = execution.getVariable("bpmnRequest")
			if (networkRequest != null) {
				if (networkRequest.contains("requestDetails")) {
					// JSON format request is sent, create xml
					try {
						def prettyJson = JsonOutput.prettyPrint(networkRequest.toString())
						utils.log("DEBUG", " Incoming message formatted . . . : " + '\n' + prettyJson, isDebugEnabled)
						networkRequest =  vidUtils.createXmlNetworkRequestInfra(execution, networkRequest)
		
					} catch (Exception ex) {
						String dataErrorMessage = " Invalid json format Request - " + ex.getMessage()
						utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
						exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
					}
				} else {
					 // XML format request is sent
				   
				}
			} else {
				// vIPR format request is sent, create xml from individual variables
				networkRequest = vidUtils.createXmlNetworkRequestInstance(execution)
			}
			
			networkRequest = utils.formatXml(networkRequest)
			utils.logAudit(networkRequest)
			execution.setVariable(Prefix + "networkRequest", networkRequest)
			utils.log("DEBUG", " network-request - " + '\n' + networkRequest, isDebugEnabled)
		
			// validate 'disableRollback'  (aka, 'suppressRollback')
			boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, networkRequest)
			execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
			utils.log("DEBUG", Prefix + "rollbackEnabled - " + rollbackEnabled, isDebugEnabled)
										
			String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable(Prefix + "networkInputs", networkInputs)
			utils.log("DEBUG", Prefix + "networkInputs - " + '\n' + networkInputs, isDebugEnabled)
			
			// prepare messageId
			String messageId = execution.getVariable(Prefix + "messageId")  // for testing
			if (messageId == null || messageId == "") {
				messageId = UUID.randomUUID()
				utils.log("DEBUG", " UPDNETI_messageId, random generated: " + messageId, isDebugEnabled)
			} else {
				utils.log("DEBUG", " UPDNETI_messageId, pre-assigned: " + messageId, isDebugEnabled)
			}
			execution.setVariable(Prefix + "messageId", messageId)
			
			String source = utils.getNodeText1(networkRequest, "source")
			execution.setVariable(Prefix + "source", source)
			utils.log("DEBUG", Prefix + "source - " + source, isDebugEnabled)
			
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
			
			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = execution.getVariable("URN_mso_adapters_po_auth")
			utils.log("DEBUG", " Obtained BasicAuth userid password for PO/SDNC adapter: " + basicAuthValuePO, isDebugEnabled)
			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, execution.getVariable("URN_mso_msoKey"))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)
	
			} catch (IOException ex) {
				String exceptionMessage = "Exception Encountered in DoUpdateNetworkInstance, PreProcessRequest() - "
				String dataErrorMessage = exceptionMessage + " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
				utils.log("DEBUG", dataErrorMessage, , isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}
			
			// Set variables for Generic Get Sub Flow use
			execution.setVariable(Prefix + "serviceInstanceId", serviceInstanceId)
			utils.log("DEBUG", Prefix + "serviceInstanceId - " + serviceInstanceId, isDebugEnabled)
	
			execution.setVariable("GENGS_type", "service-instance")
			utils.log("DEBUG", "GENGS_type - " + "service-instance", isDebugEnabled)
			utils.log("DEBUG", " Url for SDNC adapter: " + execution.getVariable("URN_mso_adapters_sdnc_endpoint"), isDebugEnabled)
			
			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG", "sdncVersion? : " + sdncVersion, isDebugEnabled)
			
			// build 'networkOutputs'			
			networkId = utils.getNodeText1(networkRequest, "network-id")
			if ((networkId == null) || (networkId == "null")) {
				networkId = ""
			}
			String networkName = utils.getNodeText1(networkRequest, "network-name")
			if ((networkName == null) || (networkName == "null")) {
				networkName = ""
			}
			String networkOutputs =
			   """<network-outputs>
	                   <network-id>${networkId}</network-id>
	                   <network-name>${networkName}</network-name>
	                 </network-outputs>"""
			execution.setVariable(Prefix + "networkOutputs", networkOutputs)
			utils.log("DEBUG", Prefix + "networkOutputs - " + '\n' + networkOutputs, isDebugEnabled)
			execution.setVariable(Prefix + "networkId", networkId)
			execution.setVariable(Prefix + "networkName", networkName)
		

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			sendSyncError(execution)
			 // caught exception
			String exceptionMessage = "Exception Encountered in DoUpdateNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	public void callRESTQueryAAICloudRegion (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAICloudRegion of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {
			String networkInputs  = execution.getVariable(Prefix + "networkInputs")
			String cloudRegion = utils.getNodeText1(networkInputs, "aic-cloud-region")
			cloudRegion = UriUtils.encode(cloudRegion,"UTF-8")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			utils.logAudit(queryCloudRegionRequest)
			execution.setVariable(Prefix + "queryCloudRegionRequest", queryCloudRegionRequest)
			utils.log("DEBUG", " UPDNETI_queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest, isDebugEnabled)

			String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
			String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

			if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
				execution.setVariable(Prefix + "cloudRegionPo", cloudRegionPo)
				execution.setVariable(Prefix + "cloudRegionSdnc", cloudRegionSdnc)
				execution.setVariable(Prefix + "isCloudRegionGood", true)

			} else {
			    String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable(Prefix + "queryCloudRegionReturnCode")
			    utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			}

			utils.log("DEBUG", " is Cloud Region Good: " + execution.getVariable(Prefix + "isCloudRegionGood"), isDebugEnabled)

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			// try error
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow - callRESTQueryAAICloudRegion() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkId(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkId of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String networkId   = utils.getNodeText1(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			execution.setVariable(Prefix + "networkId", networkId)

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String queryIdAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=all"
			utils.logAudit(queryIdAAIRequest)
			execution.setVariable(Prefix + "queryIdAAIRequest", queryIdAAIRequest)
			utils.log("DEBUG", Prefix + "queryIdAAIRequest - " + "\n" + queryIdAAIRequest, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryIdAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiIdReturnCode", returnCode)
			utils.log("DEBUG", " ***** AAI Response Code  : " + returnCode, isDebugEnabled)
			
			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable(Prefix + "queryIdAAIResponse", aaiResponseAsString)
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
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkId() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTReQueryAAINetworkId(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTReQueryAAINetworkId of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String networkId   = utils.getNodeText1(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")

			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String requeryIdAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=all"
			utils.logAudit(requeryIdAAIRequest)
			execution.setVariable(Prefix + "requeryIdAAIRequest", requeryIdAAIRequest)
			utils.log("DEBUG", " UPDNETI_requeryIdAAIRequest - " + "\n" + requeryIdAAIRequest, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, requeryIdAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiRequeryIdReturnCode", returnCode)
			utils.log("DEBUG", " ***** AAI ReQuery Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable(Prefix + "requeryIdAAIResponse", aaiResponseAsString)
				utils.log("DEBUG", " ReQueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString, isDebugEnabled)

				String netId = utils.getNodeText1(aaiResponseAsString, "network-id")
				String netName = utils.getNodeText1(aaiResponseAsString, "network-name")
				String networkOutputs =
				   """<network-outputs>
                   <network-id>${netId}</network-id>			
                   <network-name>${netName}</network-name>
                 </network-outputs>"""
				execution.setVariable(Prefix + "networkOutputs", networkOutputs)
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
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTReQueryAAINetworkId() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkVpnBinding(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkVpnBinding of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {

			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			utils.log("DEBUG", " relationship - " + relationship, isDebugEnabled)

			// Check if Vnf Binding is present, then build a List of vnfBinding
			List vpnBindingUri = networkUtils.getVnfBindingObject(relationship)
			int vpnCount = vpnBindingUri.size()
			execution.setVariable(Prefix + "vpnCount", vpnCount)
			utils.log("DEBUG", " UPDNETI_vpnCount - " + vpnCount, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (vpnCount > 0) {
				execution.setVariable(Prefix + "vpnBindings", vpnBindingUri)
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
						    queryVpnBindingAAIRequest = "${aai_endpoint}" + vpnBindingUri[i].substring(0, vpnBindingUri[i].length()-1) + "?depth=all"
						} else {
						    queryVpnBindingAAIRequest = "${aai_endpoint}" + vpnBindingUri[i] + "?depth=all"
						}

					} else {
					    // using uri value in URN mapping
						String vpnBindingId = vpnBindingUri[i].substring(vpnBindingUri[i].indexOf("/vpn-binding/")+13, vpnBindingUri[i].length())
						if (vpnBindingId.charAt(vpnBindingId.length()-1) == '/') {
							vpnBindingId = vpnBindingId.substring(0, vpnBindingId.length()-1)
						}
					    queryVpnBindingAAIRequest = "${aai_endpoint}${aai_uri}/" + vpnBindingId + "?depth=all"
					}

					utils.logAudit(queryVpnBindingAAIRequest)
					execution.setVariable(Prefix + "queryVpnBindingAAIRequest", queryVpnBindingAAIRequest)
					utils.log("DEBUG", " UPDNETI_queryVpnBindingAAIRequest, , vpnBinding #" + counting + " : " + "\n" + queryVpnBindingAAIRequest, isDebugEnabled)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryVpnBindingAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", returnCode)
					utils.log("DEBUG", " ***** AAI query vpn binding Response Code, vpnBinding #" + counting + " : " + returnCode, isDebugEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						utils.logAudit(aaiResponseAsString)
						execution.setVariable(Prefix + "queryVpnBindingAAIResponse", aaiResponseAsString)
						utils.log("DEBUG", " AAI Query Vpn Binding Success REST Response, , vpnBinding #" + counting + " : " + "\n" + aaiResponseAsString, isDebugEnabled)

						String routeTarget = ""
						String routeRole = ""
						if (utils.nodeExists(aaiResponseAsString, "route-targets")) {
							String aaiRouteTargets = utils.getNodeXml(aaiResponseAsString, "route-targets", false)
							def aaiRouteTargetsXml = new XmlSlurper().parseText(aaiRouteTargets)
							def aaiRouteTarget = aaiRouteTargetsXml.'**'.findAll {it.name() == "route-target"}
							for (j in 0..aaiRouteTarget.size()-1) {
								routeTarget  = utils.getNodeText1(XmlUtil.serialize(aaiRouteTarget[j]), "global-route-target")
								routeRole  = utils.getNodeText1(XmlUtil.serialize(aaiRouteTarget[j]), "route-target-role")
								routeTargets += "<routeTargets>" + '\n' +
								                " <routeTarget>" + routeTarget + "</routeTarget>" + '\n' +
												" <routeTargetRole>" + routeRole + "</routeTargetRole>" + '\n' +
												"</routeTargets>" + '\n'
							}
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

				execution.setVariable(Prefix + "routeCollection", routeTargets)
				utils.log("DEBUG", " UPDNETI_routeCollection - " + '\n' + routeTargets, isDebugEnabled)

			} else {
				// reset return code to success
				execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")
				String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
				String schemaVersion = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
			    String aaiStubResponse =
					"""	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<vpn-binding xmlns="${schemaVersion}">
						      <global-route-target/>
							</vpn-binding>
						</rest:payload>"""
				String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
				execution.setVariable(Prefix + "queryVpnBindingAAIResponse", aaiStubResponseAsXml)
				execution.setVariable(Prefix + "routeCollection", "<routeTargets/>")
				utils.log("DEBUG", " No vpnBinding, using this stub as response - " + '\n' + aaiStubResponseAsXml, isDebugEnabled)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkVpnBinding() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkPolicy(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkPolicy of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {
			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			utils.log("DEBUG", " relationship - " + relationship, isDebugEnabled)

			// Check if Network Policy is present, then build a List of network policy
			List networkPolicyUriList = networkUtils.getNetworkPolicyObject(relationship)
			int networkPolicyCount = networkPolicyUriList.size()
			execution.setVariable(Prefix + "networkPolicyCount", networkPolicyCount)
			utils.log("DEBUG", " UPDNETI_networkPolicyCount - " + networkPolicyCount, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkPolicyCount > 0) {
				execution.setVariable(Prefix + "networkPolicyUriList", networkPolicyUriList)
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
							queryNetworkPolicyAAIRequest = "${aai_endpoint}" + networkPolicyUriList[i].substring(0, networkPolicyUriList[i].length()-1) + "?depth=all"
						} else {
							queryNetworkPolicyAAIRequest = "${aai_endpoint}" + networkPolicyUriList[i] + "?depth=all"
						}
					} else {
						// using uri value in URN mapping
						String networkPolicyId = networkPolicyUriList[i].substring(networkPolicyUriList[i].indexOf("/network-policy/")+16, networkPolicyUriList[i].length())
						println " networkPolicyId - " + networkPolicyId
						if (networkPolicyId.charAt(networkPolicyId.length()-1) == '/') {
							networkPolicyId = networkPolicyId.substring(0, networkPolicyId.length()-1)
						}
						queryNetworkPolicyAAIRequest = "${aai_endpoint}${aai_uri}/" + networkPolicyId + "?depth=all"

					}


					utils.logAudit(queryNetworkPolicyAAIRequest)
					execution.setVariable(Prefix + "queryNetworkPolicyAAIRequest", queryNetworkPolicyAAIRequest)
					utils.log("DEBUG", " UPDNETI_queryNetworkPolicyAAIRequest, , NetworkPolicy #" + counting + " : " + "\n" + queryNetworkPolicyAAIRequest, isDebugEnabled)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", returnCode)
					utils.log("DEBUG", " ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode, isDebugEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						utils.logAudit(aaiResponseAsString)
						execution.setVariable(Prefix + "queryNetworkPolicyAAIResponse", aaiResponseAsString)
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

				execution.setVariable(Prefix + "networkCollection", networkPolicies)
				utils.log("DEBUG", " UPDNETI_networkCollection - " + '\n' + networkPolicies, isDebugEnabled)

			} else {
				// reset return code to success
				execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")
				String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
				String schemaVersion = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
				String aaiStubResponse =
					"""	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<network-policy xmlns="${schemaVersion}">
							  <network-policy-fqdn/>
                            </network-policy>
						</rest:payload>"""
				String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
				execution.setVariable(Prefix + "queryNetworkPolicyAAIResponse", aaiStubResponseAsXml)
				execution.setVariable(Prefix + "networkCollection", "<policyFqdns/>")
				utils.log("DEBUG", " No net policies, using this stub as response - " + '\n' + aaiStubResponseAsXml, isDebugEnabled)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkPolicy() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkTableRef(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAINetworkTableRef of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {
			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			utils.log("DEBUG", " relationship - " + relationship, isDebugEnabled)

			// Check if Network TableREf is present, then build a List of network policy
			List networkTableRefUriList = networkUtils.getNetworkTableRefObject(relationship)
			int networkTableRefCount = networkTableRefUriList.size()
			execution.setVariable(Prefix + "networkTableRefCount", networkTableRefCount)
			utils.log("DEBUG", " UPDNETI_networkTableRefCount - " + networkTableRefCount, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkTableRefCount > 0) {
				execution.setVariable(Prefix + "networkTableRefUriList", networkTableRefUriList)
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
							queryNetworkTableRefAAIRequest = "${aai_endpoint}" + networkTableRefUriList[i].substring(0, networkTableRefUriList[i].length()-1) + "?depth=all"
						} else {
							queryNetworkTableRefAAIRequest = "${aai_endpoint}" + networkTableRefUriList[i] + "?depth=all"
						}
					} else {
						// using uri value in URN mapping
						String networkTableRefId = networkTableRefUriList[i].substring(networkTableRefUriList[i].indexOf("/route-table-reference/")+23, networkTableRefUriList[i].length())

						if (networkTableRefId.charAt(networkTableRefId.length()-1) == '/') {
							networkTableRefId = networkTableRefId.substring(0, networkTableRefId.length()-1)
						}
						queryNetworkTableRefAAIRequest = "${aai_endpoint}${aai_uri}/" + networkTableRefId + "?depth=all"

					}


					utils.logAudit(queryNetworkTableRefAAIRequest)
					execution.setVariable(Prefix + "queryNetworkTableRefAAIRequest", queryNetworkTableRefAAIRequest)
					utils.log("DEBUG", " UPDNETI_queryNetworkTableRefAAIRequest, , NetworkTableRef #" + counting + " : " + "\n" + queryNetworkTableRefAAIRequest, isDebugEnabled)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkTableRefAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", returnCode)
					utils.log("DEBUG", " ***** AAI query network Table Reference Response Code, NetworkTableRef #" + counting + " : " + returnCode, isDebugEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						utils.logAudit(aaiResponseAsString)
						execution.setVariable(Prefix + "queryNetworkTableRefAAIResponse", aaiResponseAsString)
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

				execution.setVariable(Prefix + "tableRefCollection", networkTableRefs)
				utils.log("DEBUG", " UPDNETI_tableRefCollection - " + '\n' + networkTableRefs, isDebugEnabled)

			} else {
				// reset return code to success
				execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")
				String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
				String schemaVersion = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
				String aaiStubResponse =
					"""	<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
							<route-table-references xmlns="${schemaVersion}">
							  <route-table-reference-fqdn/>
                            </route-table-references>
						</rest:payload>"""
				String aaiStubResponseAsXml = utils.formatXml(aaiStubResponse)
				execution.setVariable(Prefix + "queryNetworkTableRefAAIResponse", aaiStubResponseAsXml)
				execution.setVariable(Prefix + "tableRefCollection", "<routeTableFqdns/>")
				utils.log("DEBUG", " No net table references, using this stub as response - " + '\n' + aaiStubResponseAsXml, isDebugEnabled)

			}

		} catch (BpmnError e) {
			throw e;
			
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkTableRef() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	public void callRESTUpdateContrailAAINetwork(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTUpdateContrailAAINetwork of DoUpdateNetworkInstance ***** " , isDebugEnabled)

		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String networkId   = utils.getNodeText1(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			String requeryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse")
			String updateNetworkResponse   = execution.getVariable(Prefix + "updateNetworkResponse")

			// Prepare url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String updateContrailAAIUrlRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=all"

			utils.logAudit(updateContrailAAIUrlRequest)
			execution.setVariable(Prefix + "updateContrailAAIUrlRequest", updateContrailAAIUrlRequest)
			utils.log("DEBUG", " UPDNETI_updateContrailAAIUrlRequest - " + "\n" + updateContrailAAIUrlRequest, isDebugEnabled)

			//Prepare payload (PUT)
			String schemaVersion = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
			String payload = networkUtils.ContrailNetworkCreatedUpdate(requeryIdAAIResponse, updateNetworkResponse, schemaVersion)
			String payloadXml = utils.formatXml(payload)
			utils.logAudit(payloadXml)
			execution.setVariable(Prefix + "updateContrailAAIPayloadRequest", payloadXml)
			utils.log("DEBUG", " 'payload' to Update Contrail - " + "\n" + payloadXml, isDebugEnabled)

			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, updateContrailAAIUrlRequest, payload)
			String returnCode = response.getStatusCode()
			String aaiUpdateContrailResponseAsString = response.getResponseBodyAsString()
			
			execution.setVariable(Prefix + "aaiUpdateContrailReturnCode", returnCode)
			utils.log("DEBUG", " ***** AAI Update Contrail Response Code  : " + returnCode, isDebugEnabled)


			if (returnCode=='200') {
				utils.logAudit(aaiUpdateContrailResponseAsString)
				execution.setVariable(Prefix + "updateContrailAAIResponse", aaiUpdateContrailResponseAsString)
				utils.log("DEBUG", " AAI Update Contrail Success REST Response - " + "\n" + aaiUpdateContrailResponseAsString, isDebugEnabled)
				// Point-of-no-return is set to false, rollback not needed.
				execution.setVariable(Prefix + "isPONR", true)

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
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
					  }
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTUpdateContrailAAINetwork() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareUpdateNetworkRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareUpdateNetworkRequest of DoUpdateNetworkInstance ***** ", isDebugEnabled)

		try {

			// get variables
			String requestId = execution.getVariable(Prefix + "requestId")
			String messageId = execution.getVariable(Prefix + "messageId")
			String source    = execution.getVariable(Prefix + "source")

			String requestInput = execution.getVariable(Prefix + "networkRequest")
			String queryIdResponse = execution.getVariable(Prefix + "requeryIdAAIResponse")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionPo")
			String backoutOnFailure = execution.getVariable(Prefix + "rollbackEnabled")
			
			// Prepare Network request
			String routeCollection = execution.getVariable(Prefix + "routeCollection")
			String policyCollection = execution.getVariable(Prefix + "networkCollection")
			String tableCollection = execution.getVariable(Prefix + "tableRefCollection")
			String updateNetworkRequest = networkUtils.UpdateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyCollection, tableCollection, cloudRegionId, backoutOnFailure, source )
			// Format Response
			String buildUpdateNetworkRequestAsString = utils.formatXml(updateNetworkRequest)
			buildUpdateNetworkRequestAsString = buildUpdateNetworkRequestAsString.replace(":w1aac13n0", "").replace("w1aac13n0:", "")
			utils.logAudit(buildUpdateNetworkRequestAsString)

			execution.setVariable(Prefix + "updateNetworkRequest", buildUpdateNetworkRequestAsString)
			utils.log("DEBUG", " UPDNETI_updateNetworkRequest - " + "\n" +  buildUpdateNetworkRequestAsString, isDebugEnabled)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareUpdateNetworkRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareSDNCRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRequest of DoUpdateNetworkInstance ***** ", isDebugEnabled)

		try {
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String updateNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

			String networkId = ""
			if (utils.nodeExists(updateNetworkInput, "network-id")) {
			   networkId = utils.getNodeText1(updateNetworkInput, "network-id")
			}
			if (networkId == null) {networkId = ""}

			String serviceInstanceId = utils.getNodeText1(updateNetworkInput, "service-instance-id")

			// 1. prepare assign topology via SDNC Adapter SUBFLOW call
 		   	String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "changeassign", "NetworkActivateRequest", cloudRegionId, networkId, null, null)

			String sndcTopologyUpdateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
			utils.logAudit(sndcTopologyUpdateRequesAsString)
			execution.setVariable(Prefix + "changeAssignSDNCRequest", sndcTopologyUpdateRequesAsString)
			utils.log("DEBUG", " UPDNETI_changeAssignSDNCRequest - " + "\n" +  sndcTopologyUpdateRequesAsString, isDebugEnabled)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSDNCRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	


	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void validateUpdateNetworkResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateUpdateNetworkResponse of DoUpdateNetworkInstance *****", isDebugEnabled)

		try {
			String returnCode = execution.getVariable(Prefix + "networkReturnCode")
			String networkResponse = execution.getVariable(Prefix + "updateNetworkResponse")
			if (networkResponse==null)	{
				networkResponse="" // reset
			}

			utils.log("DEBUG", " Network Adapter update responseCode: " + returnCode, isDebugEnabled)

			String errorMessage = ""
			if (returnCode == "200") {
				execution.setVariable(Prefix + "isNetworkRollbackNeeded", true)
				utils.logAudit(networkResponse)
				execution.setVariable(Prefix + "updateNetworkResponse", networkResponse)
				utils.log("DEBUG", " Network Adapter update Success Response - " + "\n" + networkResponse, isDebugEnabled)

				// prepare rollback data
				String rollbackData = utils.getNodeXml(networkResponse, "rollback", false).replace("tag0:","").replace(":tag0","")
				rollbackData = rollbackData.replace("rollback>", "networkRollback>")
  				String rollbackNetwork =
					"""<rollbackNetworkRequest>
							${rollbackData}
						</rollbackNetworkRequest>"""
				String rollbackNetworkXml = utils.formatXml(rollbackNetwork)
				execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkXml)
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
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. validateUpdateNetworkResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}

	public void validateSDNCResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateSDNCResponse of DoUpdateNetworkInstance ***** ", isDebugEnabled)

		String response = execution.getVariable(Prefix + "changeAssignSDNCResponse")
		WorkflowException workflowException = null
		try {
			workflowException = execution.getVariable(Prefix + "WorkflowException")
			//execution.setVariable("WorkflowException", workflowException)
		} catch (Exception ex) {
			utils.log("DEBUG", " Sdnc 'WorkflowException' object is empty or null. ", isDebugEnabled)
		}

		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String changeAssignSDNCResponseDecodeXml = sdncAdapterUtils.decodeXML(execution.getVariable(Prefix + "changeAssignSDNCResponse"))
		changeAssignSDNCResponseDecodeXml = changeAssignSDNCResponseDecodeXml.replace("&", "&amp;").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "changeAssignSDNCResponse", changeAssignSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncRollbackNeeded", true)
			utils.log("DEBUG", "Successfully Validated SDNC Response", isDebugEnabled)

		} else {
			utils.log("DEBUG", "Did NOT Successfully Validated SDNC Response", isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
		}

	}


	public void postProcessResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse of DoUpdateNetworkInstance ***** ", isDebugEnabled)

		try {
			utils.log("DEBUG", " ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"), isDebugEnabled)
			if (execution.getVariable(Prefix + "isException") == false) {
				// set rollback data
				execution.setVariable("orchestrationStatus", "")
				execution.setVariable("networkId", execution.getVariable(Prefix + "networkId"))
				execution.setVariable("networkName", execution.getVariable(Prefix + "networkName"))
				prepareSuccessRollbackData(execution) // populate rollbackData
				execution.setVariable("WorkflowException", null)
				execution.setVariable(Prefix + "Success", true)
				utils.log("DEBUG", " ***** postProcessResponse(), GOOD !!!", isDebugEnabled)
			} else {
				execution.setVariable(Prefix + "Success", false) 
				execution.setVariable("rollbackData", null)
				String exceptionMessage = " Exception encountered in MSO Bpmn. "
				if (execution.getVariable("workflowException") != null) {  // Output of Rollback flow.
				   utils.log("DEBUG", " ***** workflowException: " + execution.getVariable("workflowException"), isDebugEnabled)
				   WorkflowException wfex = execution.getVariable("workflowException")
				   exceptionMessage = wfex.getErrorMessage()
   				} else {
			       if (execution.getVariable(Prefix + "WorkflowException") != null) {
				      WorkflowException pwfex = execution.getVariable(Prefix + "WorkflowException")
				      exceptionMessage = pwfex.getErrorMessage()
			       }   
   				}
			    // going to the Main flow: a-la-carte or macro
			    utils.log("DEBUG", " ***** postProcessResponse(), BAD !!!", isDebugEnabled)
			    exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
				throw new BpmnError("MSOWorkflowException")
			}
							
		} catch(BpmnError b){
		     utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
		     throw b
			

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }


	}

	public void prepareSDNCRollbackRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRollbackRequest of DoUpdateNetworkInstance ***** ", isDebugEnabled)

		try {
			// for some reason the WorkflowException object is null after the sdnc rollback call task, need to save WorkflowException. 
			execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String updateNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String changeAssignSDNCResponse = execution.getVariable(Prefix + "changeAssignSDNCResponse")
			String networkId = utils.getNodeText1(changeAssignSDNCResponse, "network-id")

			String serviceInstanceId = utils.getNodeText1(updateNetworkInput, "service-instance-id")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "rollback", "NetworkActivateRequest", cloudRegionId, networkId, null, null)
			String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
			utils.log("DEBUG", " Preparing request for SDNC Topology assign's rollback/compensation . . . - " + "\n" +  sndcTopologyRollbackRequestAsString, isDebugEnabled)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSDNCRollbackRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRollbackData(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside prepareRollbackData() of DoUpdateNetworkInstance ***** ", isDebugEnabled)
		
		try {
			
			Map<String, String> rollbackData = new HashMap<String, String>();
			String rollbackSDNCRequest = execution.getVariable(Prefix + "rollbackSDNCRequest")
			if (rollbackSDNCRequest != null) {
				if (rollbackSDNCRequest != "") {
					rollbackData.put("rollbackSDNCRequest", execution.getVariable(Prefix + "rollbackSDNCRequest"))
				}
			}
			String rollbackNetworkRequest = execution.getVariable(Prefix + "rollbackNetworkRequest")
			if (rollbackNetworkRequest != null) {
				if (rollbackNetworkRequest != "") {
					rollbackData.put("rollbackNetworkRequest", execution.getVariable(Prefix + "rollbackNetworkRequest"))
				}
			}
			execution.setVariable("rollbackData", rollbackData)
			utils.log("DEBUG", "** rollbackData : " + rollbackData, isDebugEnabled)
			
			execution.setVariable("WorkflowException", execution.getVariable(Prefix + "WorkflowException"))
			utils.log("DEBUG", "** WorkflowException : " + execution.getVariable("WorkflowException"), isDebugEnabled)
			
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		
		}
		
	}
	
	public void prepareSuccessRollbackData(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside prepareSuccessRollbackData() of DoUpdateNetworkInstance ***** ", isDebugEnabled)
		
		try {
			
			if (execution.getVariable("sdncVersion") != '1610') {
				// skip: 1702 for 'changeassign' or equivalent not yet defined in SNDC, so no rollback.
			} else {
				prepareSDNCRollbackRequest(execution)
			}
			
			Map<String, String> rollbackData = new HashMap<String, String>();
			String rollbackSDNCRequest = execution.getVariable(Prefix + "rollbackSDNCRequest")
			if (rollbackSDNCRequest != null) {
				if (rollbackSDNCRequest != "") {
					rollbackData.put("rollbackSDNCRequest", rollbackSDNCRequest)
				}
			}
			String rollbackNetworkRequest = execution.getVariable(Prefix + "rollbackNetworkRequest")
			if (rollbackNetworkRequest != null) {
				if (rollbackNetworkRequest != "") {
					rollbackData.put("rollbackNetworkRequest", rollbackNetworkRequest)
				}
			}
			execution.setVariable("rollbackData", rollbackData)
			
			utils.log("DEBUG", "** 'rollbackData' for Full Rollback : " + rollbackData, isDebugEnabled)
			execution.setVariable("WorkflowException", null)

			
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		
		}
		
	}
	
	public void setExceptionFlag(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside setExceptionFlag() of DoUpdateNetworkInstance ***** ", isDebugEnabled)
		
		try {
			
			execution.setVariable(Prefix + "isException", true)
			
			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			utils.log("DEBUG", Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"), isDebugEnabled)
			
		} catch(Exception ex){
			  String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		}
		
	}


	// *******************************
	//     Build Error Section
	// *******************************

	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - "  + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")
			
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}

}
