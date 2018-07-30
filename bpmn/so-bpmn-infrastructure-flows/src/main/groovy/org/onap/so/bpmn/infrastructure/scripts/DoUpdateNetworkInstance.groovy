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

package org.onap.so.bpmn.infrastructure.scripts;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils

import groovy.json.*
import groovy.xml.XmlUtil

/**
 * This groovy class supports the <class>DoUpdateNetworkInstance.bpmn</class> process.
 *
 */
public class DoUpdateNetworkInstance extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoUpdateNetworkInstance.class);
	
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

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside preProcessRequest DoUpdateNetworkInstance Request ")

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
						msoLogger.debug(" Incoming message formatted . . . : " + '\n' + prettyJson)
						networkRequest =  vidUtils.createXmlNetworkRequestInfra(execution, networkRequest)
		
					} catch (Exception ex) {
						String dataErrorMessage = " Invalid json format Request - " + ex.getMessage()
						msoLogger.debug(dataErrorMessage)
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
			msoLogger.debug(networkRequest)
			execution.setVariable(Prefix + "networkRequest", networkRequest)
			msoLogger.debug(" network-request - " + '\n' + networkRequest)
		
			// validate 'disableRollback'  (aka, 'suppressRollback')
			boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, networkRequest)
			execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
			msoLogger.debug(Prefix + "rollbackEnabled - " + rollbackEnabled)
										
			String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable(Prefix + "networkInputs", networkInputs)
			msoLogger.debug(Prefix + "networkInputs - " + '\n' + networkInputs)
			
			// prepare messageId
			String messageId = execution.getVariable(Prefix + "messageId")  // for testing
			if (messageId == null || messageId == "") {
				messageId = UUID.randomUUID()
				msoLogger.debug(" UPDNETI_messageId, random generated: " + messageId)
			} else {
				msoLogger.debug(" UPDNETI_messageId, pre-assigned: " + messageId)
			}
			execution.setVariable(Prefix + "messageId", messageId)
			
			String source = utils.getNodeText(networkRequest, "source")
			execution.setVariable(Prefix + "source", source)
			msoLogger.debug(Prefix + "source - " + source)
			
			String networkId = ""
			if (utils.nodeExists(networkRequest, "network-id")) {
				networkId = utils.getNodeText(networkRequest, "network-id")
				if (networkId == 'null' || networkId == "") {
					sendSyncError(execution)
					// missing value of networkId
					String dataErrorMessage = "Variable 'network-id' value/element is missing."
					msoLogger.debug(" Invalid Request - " + dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
	
				}
			}
	
			String lcpCloudRegion = ""
			if (utils.nodeExists(networkRequest, "aic-cloud-region")) {
				lcpCloudRegion = utils.getNodeText(networkRequest, "aic-cloud-region")
				if ((lcpCloudRegion == 'null') || (lcpCloudRegion == "")) {
					sendSyncError(execution)
					String dataErrorMessage = "requestDetails has missing 'aic-cloud-region' value/element."
					msoLogger.debug(" Invalid Request - " + dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}
	
			String serviceInstanceId = ""
			if (utils.nodeExists(networkRequest, "service-instance-id")) {
				serviceInstanceId = utils.getNodeText(networkRequest, "service-instance-id")
				if ((serviceInstanceId == 'null') || (lcpCloudRegion == "")) {
					sendSyncError(execution)
					String dataErrorMessage = "Variable 'serviceInstanceId' value/element is missing."
					msoLogger.debug(" Invalid Request - " + dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}
			
			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth",execution)
			

			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey", execution))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)
	
			} catch (IOException ex) {
				String exceptionMessage = "Exception Encountered in DoUpdateNetworkInstance, PreProcessRequest() - "
				String dataErrorMessage = exceptionMessage + " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
				msoLogger.debug(dataErrorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}
			
			// Set variables for Generic Get Sub Flow use
			execution.setVariable(Prefix + "serviceInstanceId", serviceInstanceId)
			msoLogger.debug(Prefix + "serviceInstanceId - " + serviceInstanceId)
	
			execution.setVariable("GENGS_type", "service-instance")
			msoLogger.debug("GENGS_type - " + "service-instance")
			msoLogger.debug(" Url for SDNC adapter: " + UrnPropertiesReader.getVariable("mso.adapters.sdnc.endpoint",execution))
			
			String sdncVersion = execution.getVariable("sdncVersion")
			msoLogger.debug("sdncVersion? : " + sdncVersion)
			
			// build 'networkOutputs'			
			networkId = utils.getNodeText(networkRequest, "network-id")
			if ((networkId == null) || (networkId == "null")) {
				networkId = ""
			}
			String networkName = utils.getNodeText(networkRequest, "network-name")
			if ((networkName == null) || (networkName == "null")) {
				networkName = ""
			}
			String networkOutputs =
			   """<network-outputs>
	                   <network-id>${MsoUtils.xmlEscape(networkId)}</network-id>
	                   <network-name>${MsoUtils.xmlEscape(networkName)}</network-name>
	                 </network-outputs>"""
			execution.setVariable(Prefix + "networkOutputs", networkOutputs)
			msoLogger.debug(Prefix + "networkOutputs - " + '\n' + networkOutputs)
			execution.setVariable(Prefix + "networkId", networkId)
			execution.setVariable(Prefix + "networkName", networkName)
		

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			sendSyncError(execution)
			 // caught exception
			String exceptionMessage = "Exception Encountered in DoUpdateNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	public void callRESTQueryAAICloudRegion (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAICloudRegion of DoUpdateNetworkInstance ***** " )

		try {
			String networkInputs  = execution.getVariable(Prefix + "networkInputs")
			String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
			cloudRegion = UriUtils.encode(cloudRegion,"UTF-8")

			// Prepare AA&I url
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			msoLogger.debug(queryCloudRegionRequest)
			execution.setVariable(Prefix + "queryCloudRegionRequest", queryCloudRegionRequest)
			msoLogger.debug(" UPDNETI_queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest)

			String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
			String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

			if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
				execution.setVariable(Prefix + "cloudRegionPo", cloudRegionPo)
				execution.setVariable(Prefix + "cloudRegionSdnc", cloudRegionSdnc)
				execution.setVariable(Prefix + "isCloudRegionGood", true)

			} else {
			    String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable(Prefix + "queryCloudRegionReturnCode")
			    msoLogger.debug(dataErrorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			}

			msoLogger.debug(" is Cloud Region Good: " + execution.getVariable(Prefix + "isCloudRegionGood"))

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			// try error
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow - callRESTQueryAAICloudRegion() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkId(DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkId of DoUpdateNetworkInstance ***** " )

		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String networkId   = utils.getNodeText(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			execution.setVariable(Prefix + "networkId", networkId)

			// Prepare AA&I url
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String queryIdAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=all"
			msoLogger.debug(queryIdAAIRequest)
			execution.setVariable(Prefix + "queryIdAAIRequest", queryIdAAIRequest)
			msoLogger.debug(Prefix + "queryIdAAIRequest - " + "\n" + queryIdAAIRequest)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryIdAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiIdReturnCode", returnCode)
			msoLogger.debug(" ***** AAI Response Code  : " + returnCode)
			
			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				msoLogger.debug(aaiResponseAsString)
				execution.setVariable(Prefix + "queryIdAAIResponse", aaiResponseAsString)
				msoLogger.debug(" QueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString)

			} else {
				if (returnCode=='404') {
					String dataErrorMessage = "Response Error from QueryAAINetworkId is 404 (Not Found)."
					msoLogger.debug(" AAI Query Failed. " + dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)
					   throw new BpmnError("MSOWorkflowException")

				   } else {
							// aai all errors
							String dataErrorMessage = "Unexpected Response from QueryAAINetworkId - " + returnCode
							msoLogger.debug("Unexpected Response from QueryAAINetworkId - " + dataErrorMessage)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				  }
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkId() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTReQueryAAINetworkId(DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTReQueryAAINetworkId of DoUpdateNetworkInstance ***** " )

		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String networkId   = utils.getNodeText(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")

			// Prepare AA&I url
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String requeryIdAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=all"
			msoLogger.debug(requeryIdAAIRequest)
			execution.setVariable(Prefix + "requeryIdAAIRequest", requeryIdAAIRequest)
			msoLogger.debug(" UPDNETI_requeryIdAAIRequest - " + "\n" + requeryIdAAIRequest)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, requeryIdAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiRequeryIdReturnCode", returnCode)
			msoLogger.debug(" ***** AAI ReQuery Response Code  : " + returnCode)

			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				msoLogger.debug(aaiResponseAsString)
				execution.setVariable(Prefix + "requeryIdAAIResponse", aaiResponseAsString)
				msoLogger.debug(" ReQueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString)

				String netId = utils.getNodeText(aaiResponseAsString, "network-id")
				String netName = utils.getNodeText(aaiResponseAsString, "network-name")
				String networkOutputs =
				   """<network-outputs>
                   <network-id>${MsoUtils.xmlEscape(netId)}</network-id>			
                   <network-name>${MsoUtils.xmlEscape(netName)}</network-name>
                 </network-outputs>"""
				execution.setVariable(Prefix + "networkOutputs", networkOutputs)
				msoLogger.debug(" networkOutputs - " + '\n' + networkOutputs)
				
			} else {
				if (returnCode=='404') {
					String dataErrorMessage = "Response Error from ReQueryAAINetworkId is 404 (Not Found)."
					msoLogger.debug(" AAI ReQuery Failed. - " + dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)
					   throw new BpmnError("MSOWorkflowException")

					   } else {
							// aai all errors
							String dataErrorMessage = "Unexpected Response from ReQueryAAINetworkId - " + returnCode
							msoLogger.debug(dataErrorMessage)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

					}
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTReQueryAAINetworkId() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkVpnBinding(DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkVpnBinding of DoUpdateNetworkInstance ***** " )

		try {

			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			msoLogger.debug(" relationship - " + relationship)

			// Check if Vnf Binding is present, then build a List of vnfBinding
			List vpnBindingUri = networkUtils.getVnfBindingObject(relationship)
			int vpnCount = vpnBindingUri.size()
			execution.setVariable(Prefix + "vpnCount", vpnCount)
			msoLogger.debug(" UPDNETI_vpnCount - " + vpnCount)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (vpnCount > 0) {
				execution.setVariable(Prefix + "vpnBindings", vpnBindingUri)
				msoLogger.debug(" vpnBindingUri List - " + vpnBindingUri)

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

					msoLogger.debug(queryVpnBindingAAIRequest)
					execution.setVariable(Prefix + "queryVpnBindingAAIRequest", queryVpnBindingAAIRequest)
					msoLogger.debug(" UPDNETI_queryVpnBindingAAIRequest, , vpnBinding #" + counting + " : " + "\n" + queryVpnBindingAAIRequest)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryVpnBindingAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", returnCode)
					msoLogger.debug(" ***** AAI query vpn binding Response Code, vpnBinding #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						msoLogger.debug(aaiResponseAsString)
						execution.setVariable(Prefix + "queryVpnBindingAAIResponse", aaiResponseAsString)
						msoLogger.debug(" AAI Query Vpn Binding Success REST Response, , vpnBinding #" + counting + " : " + "\n" + aaiResponseAsString)

						String routeTarget = ""
						String routeRole = ""
						if (utils.nodeExists(aaiResponseAsString, "route-targets")) {
							String aaiRouteTargets = utils.getNodeXml(aaiResponseAsString, "route-targets", false)
							def aaiRouteTargetsXml = new XmlSlurper().parseText(aaiRouteTargets)
							def aaiRouteTarget = aaiRouteTargetsXml.'**'.findAll {it.name() == "route-target"}
							for (j in 0..aaiRouteTarget.size()-1) {
								routeTarget  = utils.getNodeText(XmlUtil.serialize(aaiRouteTarget[j]), "global-route-target")
								routeRole  = utils.getNodeText(XmlUtil.serialize(aaiRouteTarget[j]), "route-target-role")
								routeTargets += "<routeTargets>" + '\n' +
								                " <routeTarget>" + routeTarget + "</routeTarget>" + '\n' +
												" <routeTargetRole>" + routeRole + "</routeTargetRole>" + '\n' +
												"</routeTargets>" + '\n'
							}
						}

					} else {
						if (returnCode=='404') {
							String dataErrorMessage = "Response Error from AAINetworkVpnBinding is 404 (Not Found)."
							msoLogger.debug(dataErrorMessage)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						} else {
						   if (aaiResponseAsString.contains("RESTFault")) {
							   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							   execution.setVariable("WorkflowException", exceptionObject)
							   throw new BpmnError("MSOWorkflowException")

							   } else {
									// aai all errors
									String dataErrorMessage = " Unexpected Response from AAINetworkVpnBinding - " + returnCode
									msoLogger.debug(dataErrorMessage)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							  }
						}
					}

				} // end loop

				execution.setVariable(Prefix + "routeCollection", routeTargets)
				msoLogger.debug(" UPDNETI_routeCollection - " + '\n' + routeTargets)

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
				msoLogger.debug(" No vpnBinding, using this stub as response - " + '\n' + aaiStubResponseAsXml)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkVpnBinding() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkPolicy(DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkPolicy of DoUpdateNetworkInstance ***** " )

		try {
			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			msoLogger.debug(" relationship - " + relationship)

			// Check if Network Policy is present, then build a List of network policy
			List networkPolicyUriList = networkUtils.getNetworkPolicyObject(relationship)
			int networkPolicyCount = networkPolicyUriList.size()
			execution.setVariable(Prefix + "networkPolicyCount", networkPolicyCount)
			msoLogger.debug(" UPDNETI_networkPolicyCount - " + networkPolicyCount)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkPolicyCount > 0) {
				execution.setVariable(Prefix + "networkPolicyUriList", networkPolicyUriList)
				msoLogger.debug(" networkPolicyUri List - " + networkPolicyUriList)

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


					msoLogger.debug(queryNetworkPolicyAAIRequest)
					execution.setVariable(Prefix + "queryNetworkPolicyAAIRequest", queryNetworkPolicyAAIRequest)
					msoLogger.debug(" UPDNETI_queryNetworkPolicyAAIRequest, , NetworkPolicy #" + counting + " : " + "\n" + queryNetworkPolicyAAIRequest)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", returnCode)
					msoLogger.debug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						msoLogger.debug(aaiResponseAsString)
						execution.setVariable(Prefix + "queryNetworkPolicyAAIResponse", aaiResponseAsString)
						msoLogger.debug(" QueryAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsString)

						String networkPolicy = ""
						if (utils.nodeExists(aaiResponseAsString, "network-policy-fqdn")) {
							networkPolicy  = utils.getNodeText(aaiResponseAsString, "network-policy-fqdn")
							networkPolicies += "<policyFqdns>" + networkPolicy + "</policyFqdns>" + '\n'
						}

					} else {
						if (returnCode=='404') {
							String dataErrorMessage = "Response Error from QueryAAINetworkPolicy is 404 (Not Found)."
							msoLogger.debug(dataErrorMessage)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						} else {
						   if (aaiResponseAsString.contains("RESTFault")) {
							   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							   execution.setVariable("WorkflowException", exceptionObject)
							   throw new BpmnError("MSOWorkflowException")

							   } else {
									// aai all errors
									String dataErrorMessage = "Unexpected Response from QueryAAINetworkPolicy - " + returnCode
									msoLogger.debug(dataErrorMessage)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							  }
						}
					}

				} // end loop

				execution.setVariable(Prefix + "networkCollection", networkPolicies)
				msoLogger.debug(" UPDNETI_networkCollection - " + '\n' + networkPolicies)

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
				msoLogger.debug(" No net policies, using this stub as response - " + '\n' + aaiStubResponseAsXml)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkPolicy() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkTableRef(DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkTableRef of DoUpdateNetworkInstance ***** " )

		try {
			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			msoLogger.debug(" relationship - " + relationship)

			// Check if Network TableREf is present, then build a List of network policy
			List networkTableRefUriList = networkUtils.getNetworkTableRefObject(relationship)
			int networkTableRefCount = networkTableRefUriList.size()
			execution.setVariable(Prefix + "networkTableRefCount", networkTableRefCount)
			msoLogger.debug(" UPDNETI_networkTableRefCount - " + networkTableRefCount)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkTableRefCount > 0) {
				execution.setVariable(Prefix + "networkTableRefUriList", networkTableRefUriList)
				msoLogger.debug(" networkTableRefUri List - " + networkTableRefUriList)

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


					msoLogger.debug(queryNetworkTableRefAAIRequest)
					execution.setVariable(Prefix + "queryNetworkTableRefAAIRequest", queryNetworkTableRefAAIRequest)
					msoLogger.debug(" UPDNETI_queryNetworkTableRefAAIRequest, , NetworkTableRef #" + counting + " : " + "\n" + queryNetworkTableRefAAIRequest)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkTableRefAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", returnCode)
					msoLogger.debug(" ***** AAI query network Table Reference Response Code, NetworkTableRef #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
						msoLogger.debug(aaiResponseAsString)
						execution.setVariable(Prefix + "queryNetworkTableRefAAIResponse", aaiResponseAsString)
						msoLogger.debug(" QueryAAINetworkTableRef Success REST Response, , NetworkTableRef #" + counting + " : " + "\n" + aaiResponseAsString)

						String networkTableRef = ""
						if (utils.nodeExists(aaiResponseAsString, "route-table-reference-fqdn")) {
							networkTableRef  = utils.getNodeText(aaiResponseAsString, "route-table-reference-fqdn")
							networkTableRefs += "<routeTableFqdns>" + networkTableRef + "</routeTableFqdns>" + '\n'
						}

					} else {
						if (returnCode=='404') {
							String dataErrorMessage = "Response Error from QueryAAINetworkTableRef is 404 (Not Found)."
							msoLogger.debug(dataErrorMessage)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
							
						} else {
						   if (aaiResponseAsString.contains("RESTFault")) {
							   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
							   execution.setVariable("WorkflowException", exceptionObject)
							   throw new BpmnError("MSOWorkflowException")
		
							   } else {
									// aai all errors
									String dataErrorMessage = "Unexpected Response from QueryAAINetworkTableRef - " + returnCode
									msoLogger.debug(dataErrorMessage)
									exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

							  }
						}
					}

				} // end loop

				execution.setVariable(Prefix + "tableRefCollection", networkTableRefs)
				msoLogger.debug(" UPDNETI_tableRefCollection - " + '\n' + networkTableRefs)

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
				msoLogger.debug(" No net table references, using this stub as response - " + '\n' + aaiStubResponseAsXml)

			}

		} catch (BpmnError e) {
			throw e;
			
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTQueryAAINetworkTableRef() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	public void callRESTUpdateContrailAAINetwork(DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTUpdateContrailAAINetwork of DoUpdateNetworkInstance ***** " )

		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String networkId   = utils.getNodeText(networkRequest, "network-id")
			networkId = UriUtils.encode(networkId,"UTF-8")
			String requeryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse")
			String updateNetworkResponse   = execution.getVariable(Prefix + "updateNetworkResponse")

			// Prepare url
			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
			String updateContrailAAIUrlRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=all"

			msoLogger.debug(updateContrailAAIUrlRequest)
			execution.setVariable(Prefix + "updateContrailAAIUrlRequest", updateContrailAAIUrlRequest)
			msoLogger.debug(" UPDNETI_updateContrailAAIUrlRequest - " + "\n" + updateContrailAAIUrlRequest)

			//Prepare payload (PUT)
			String schemaVersion = aaiUriUtil.getNamespaceFromUri(execution, aai_uri)
			String payload = networkUtils.ContrailNetworkCreatedUpdate(requeryIdAAIResponse, updateNetworkResponse, schemaVersion)
			String payloadXml = utils.formatXml(payload)
			msoLogger.debug(payloadXml)
			execution.setVariable(Prefix + "updateContrailAAIPayloadRequest", payloadXml)
			msoLogger.debug(" 'payload' to Update Contrail - " + "\n" + payloadXml)

			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, updateContrailAAIUrlRequest, payload)
			String returnCode = response.getStatusCode()
			String aaiUpdateContrailResponseAsString = response.getResponseBodyAsString()
			
			execution.setVariable(Prefix + "aaiUpdateContrailReturnCode", returnCode)
			msoLogger.debug(" ***** AAI Update Contrail Response Code  : " + returnCode)


			if (returnCode=='200') {
				msoLogger.debug(aaiUpdateContrailResponseAsString)
				execution.setVariable(Prefix + "updateContrailAAIResponse", aaiUpdateContrailResponseAsString)
				msoLogger.debug(" AAI Update Contrail Success REST Response - " + "\n" + aaiUpdateContrailResponseAsString)
				// Point-of-no-return is set to false, rollback not needed.
				execution.setVariable(Prefix + "isPONR", true)

			} else {
				if (returnCode=='404') {
					String dataErrorMessage = " Response Error from UpdateContrailAAINetwork is 404 (Not Found)."
					msoLogger.debug(dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiUpdateContrailResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiUpdateContrailResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)
					   throw new BpmnError("MSOWorkflowException")

					   } else {
							// aai all errors
							String errorMessage = "Unexpected Response from UpdateContrailAAINetwork - " + returnCode
							msoLogger.debug(errorMessage)
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
					  }
				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. callRESTUpdateContrailAAINetwork() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareUpdateNetworkRequest (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareUpdateNetworkRequest of DoUpdateNetworkInstance ")

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
			msoLogger.debug(buildUpdateNetworkRequestAsString)

			execution.setVariable(Prefix + "updateNetworkRequest", buildUpdateNetworkRequestAsString)
			msoLogger.debug(" UPDNETI_updateNetworkRequest - " + "\n" +  buildUpdateNetworkRequestAsString)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareUpdateNetworkRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareSDNCRequest (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareSDNCRequest of DoUpdateNetworkInstance ")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String updateNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

			String networkId = ""
			if (utils.nodeExists(updateNetworkInput, "network-id")) {
			   networkId = utils.getNodeText(updateNetworkInput, "network-id")
			}
			if (networkId == null) {networkId = ""}

			String serviceInstanceId = utils.getNodeText(updateNetworkInput, "service-instance-id")
			
			String queryAAIResponse = execution.getVariable(Prefix + "queryIdAAIResponse")

			// 1. prepare assign topology via SDNC Adapter SUBFLOW call
 		   	String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "changeassign", "NetworkActivateRequest", cloudRegionId, networkId, queryAAIResponse, null)

			String sndcTopologyUpdateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
			msoLogger.debug(sndcTopologyUpdateRequesAsString)
			execution.setVariable(Prefix + "changeAssignSDNCRequest", sndcTopologyUpdateRequesAsString)
			msoLogger.debug(" UPDNETI_changeAssignSDNCRequest - " + "\n" +  sndcTopologyUpdateRequesAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSDNCRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	


	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void validateUpdateNetworkResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside validateUpdateNetworkResponse of DoUpdateNetworkInstance ")

		try {
			String returnCode = execution.getVariable(Prefix + "networkReturnCode")
			String networkResponse = execution.getVariable(Prefix + "updateNetworkResponse")
			if (networkResponse==null)	{
				networkResponse="" // reset
			}

			msoLogger.debug(" Network Adapter update responseCode: " + returnCode)

			String errorMessage = ""
			if (returnCode == "200") {
				execution.setVariable(Prefix + "isNetworkRollbackNeeded", true)
				msoLogger.debug(networkResponse)
				execution.setVariable(Prefix + "updateNetworkResponse", networkResponse)
				msoLogger.debug(" Network Adapter update Success Response - " + "\n" + networkResponse)

				// prepare rollback data
				String rollbackData = utils.getNodeXml(networkResponse, "rollback", false).replace("tag0:","").replace(":tag0","")
				rollbackData = rollbackData.replace("rollback>", "networkRollback>")
  				String rollbackNetwork =
					"""<rollbackNetworkRequest>
							${rollbackData}
						</rollbackNetworkRequest>"""
				String rollbackNetworkXml = utils.formatXml(rollbackNetwork)
				execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkXml)
				msoLogger.debug(" Network Adapter rollback data - " + "\n" + rollbackNetworkXml)

			} else { // network error
			   if (returnCode.toInteger() > 399 && returnCode.toInteger() < 600) {   //4xx, 5xx
				   if (networkResponse.contains("updateNetworkError")) {
					   networkResponse = networkResponse.replace('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>', '')
					   errorMessage = utils.getNodeText(networkResponse, "message")
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
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}

	public void validateSDNCResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside validateSDNCResponse of DoUpdateNetworkInstance ")

		String response = execution.getVariable(Prefix + "changeAssignSDNCResponse")
		WorkflowException workflowException = null
		try {
			workflowException = execution.getVariable(Prefix + "WorkflowException")
			//execution.setVariable("WorkflowException", workflowException)
		} catch (Exception ex) {
			msoLogger.debug(" Sdnc 'WorkflowException' object is empty or null. ")
		}

		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String changeAssignSDNCResponseDecodeXml = execution.getVariable(Prefix + "changeAssignSDNCResponse")
		changeAssignSDNCResponseDecodeXml = changeAssignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "changeAssignSDNCResponse", changeAssignSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncRollbackNeeded", true)
			msoLogger.debug("Successfully Validated SDNC Response")

		} else {
			msoLogger.debug("Did NOT Successfully Validated SDNC Response")
			throw new BpmnError("MSOWorkflowException")
		}

	}


	public void postProcessResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside postProcessResponse of DoUpdateNetworkInstance ")

		try {
			msoLogger.debug(" ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"))
			if (execution.getVariable(Prefix + "isException") == false) {
				// set rollback data
				execution.setVariable("orchestrationStatus", "")
				execution.setVariable("networkId", execution.getVariable(Prefix + "networkId"))
				execution.setVariable("networkName", execution.getVariable(Prefix + "networkName"))
				prepareSuccessRollbackData(execution) // populate rollbackData
				execution.setVariable("WorkflowException", null)
				execution.setVariable(Prefix + "Success", true)
				msoLogger.debug(" ***** postProcessResponse(), GOOD !!!")
			} else {
				execution.setVariable(Prefix + "Success", false) 
				execution.setVariable("rollbackData", null)
				String exceptionMessage = " Exception encountered in MSO Bpmn. "
				if (execution.getVariable("workflowException") != null) {  // Output of Rollback flow.
				   msoLogger.debug(" ***** workflowException: " + execution.getVariable("workflowException"))
				   WorkflowException wfex = execution.getVariable("workflowException")
				   exceptionMessage = wfex.getErrorMessage()
   				} else {
			       if (execution.getVariable(Prefix + "WorkflowException") != null) {
				      WorkflowException pwfex = execution.getVariable(Prefix + "WorkflowException")
				      exceptionMessage = pwfex.getErrorMessage()
			       }   
   				}
			    // going to the Main flow: a-la-carte or macro
			    msoLogger.debug(" ***** postProcessResponse(), BAD !!!")
			    exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
				throw new BpmnError("MSOWorkflowException")
			}
							
		} catch(BpmnError b){
		     msoLogger.debug("Rethrowing MSOWorkflowException")
		     throw b
			

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }


	}

	public void prepareSDNCRollbackRequest (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareSDNCRollbackRequest of DoUpdateNetworkInstance ")

		try {
			// for some reason the WorkflowException object is null after the sdnc rollback call task, need to save WorkflowException. 
			execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String updateNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String changeAssignSDNCResponse = execution.getVariable(Prefix + "changeAssignSDNCResponse")
			String networkId = utils.getNodeText(changeAssignSDNCResponse, "network-id")

			String serviceInstanceId = utils.getNodeText(updateNetworkInput, "service-instance-id")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, updateNetworkInput, serviceInstanceId, sdncCallback, "rollback", "NetworkActivateRequest", cloudRegionId, networkId, null, null)
			String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
			msoLogger.debug(" Preparing request for SDNC Topology assign's rollback/compensation . . . - " + "\n" +  sndcTopologyRollbackRequestAsString)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSDNCRollbackRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRollbackData(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		
		msoLogger.trace("Inside prepareRollbackData() of DoUpdateNetworkInstance ")
		
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
			msoLogger.debug("** rollbackData : " + rollbackData)
			
			execution.setVariable("WorkflowException", execution.getVariable(Prefix + "WorkflowException"))
			msoLogger.debug("** WorkflowException : " + execution.getVariable("WorkflowException"))
			
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		
		}
		
	}
	
	public void prepareSuccessRollbackData(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		
		msoLogger.trace("Inside prepareSuccessRollbackData() of DoUpdateNetworkInstance ")
		
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
			
			msoLogger.debug("** 'rollbackData' for Full Rollback : " + rollbackData)
			execution.setVariable("WorkflowException", null)

			
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		
		}
		
	}
	
	public void setExceptionFlag(DelegateExecution execution){

		execution.setVariable("prefix",Prefix)
		
		msoLogger.trace("Inside setExceptionFlag() of DoUpdateNetworkInstance ")
		
		try {
			
			execution.setVariable(Prefix + "isException", true)
			
			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			msoLogger.debug(Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"))
			
		} catch(Exception ex){
			  String exceptionMessage = "Bpmn error encountered in DoUpdateNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		}
		
	}


	// *******************************
	//     Build Error Section
	// *******************************

	public void processJavaException(DelegateExecution execution){

		execution.setVariable("prefix",Prefix)
		try{
			msoLogger.debug("Caught a Java Exception")
			msoLogger.debug("Started processJavaException Method")
			msoLogger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - "  + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")
			
		}catch(Exception e){
			msoLogger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		msoLogger.debug("Completed processJavaException Method")
	}

}
