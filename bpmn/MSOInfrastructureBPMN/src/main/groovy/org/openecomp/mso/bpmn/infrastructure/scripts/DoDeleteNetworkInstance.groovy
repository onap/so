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

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

import groovy.xml.XmlUtil
import groovy.json.*

public class DoDeleteNetworkInstance extends AbstractServiceTaskProcessor {
	String Prefix= "DELNWKI_"
	String groovyClassName = "DoDeleteNetworkInstance"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "networkRequest", "")
		execution.setVariable(Prefix + "isSilentSuccess", false)
		execution.setVariable(Prefix + "Success", false)

		execution.setVariable(Prefix + "requestId", "")
		execution.setVariable(Prefix + "source", "")
		execution.setVariable(Prefix + "lcpCloudRegion", "")
		execution.setVariable(Prefix + "networkInputs", "")
		execution.setVariable(Prefix + "tenantId", "")

		execution.setVariable(Prefix + "queryAAIRequest","")
		execution.setVariable(Prefix + "queryAAIResponse", "")
		execution.setVariable(Prefix + "aaiReturnCode", "")
		execution.setVariable(Prefix + "isAAIGood", false)
		execution.setVariable(Prefix + "isVfRelationshipExist", false)

		// AAI query Cloud Region
		execution.setVariable(Prefix + "queryCloudRegionRequest","")
		execution.setVariable(Prefix + "queryCloudRegionReturnCode","")
		execution.setVariable(Prefix + "queryCloudRegionResponse","")
		execution.setVariable(Prefix + "cloudRegionPo","")
		execution.setVariable(Prefix + "cloudRegionSdnc","")

		execution.setVariable(Prefix + "deleteNetworkRequest", "")
		execution.setVariable(Prefix + "deleteNetworkResponse", "")
		execution.setVariable(Prefix + "networkReturnCode", "")
		execution.setVariable(Prefix + "rollbackNetworkRequest", "")

		execution.setVariable(Prefix + "deleteSDNCRequest", "")
		execution.setVariable(Prefix + "deleteSDNCResponse", "")
		execution.setVariable(Prefix + "sdncReturnCode", "")
		execution.setVariable(Prefix + "sdncResponseSuccess", false)

		execution.setVariable(Prefix + "deactivateSDNCRequest", "")
		execution.setVariable(Prefix + "deactivateSDNCResponse", "")
		execution.setVariable(Prefix + "deactivateSdncReturnCode", "")
		execution.setVariable(Prefix + "isSdncDeactivateRollbackNeeded", "")
		
		execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", "")
		execution.setVariable(Prefix + "isException", false)
		

	}
	
	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************

	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest() of " + groovyClassName + " Request ***** ", isDebugEnabled)

		// initialize flow variables
		InitializeProcessVariables(execution)
		
		try {
			// get incoming message/input
			execution.setVariable("action", "DELETE")
			String deleteNetwork = execution.getVariable("bpmnRequest")
			if (deleteNetwork != null) {
				if (deleteNetwork.contains("requestDetails")) {
					// JSON format request is sent, create xml 
					try {
						def prettyJson = JsonOutput.prettyPrint(deleteNetwork.toString())
						utils.log("DEBUG", " Incoming message formatted . . . : " + '\n' + prettyJson, isDebugEnabled)
						deleteNetwork =  vidUtils.createXmlNetworkRequestInfra(execution, deleteNetwork)
		
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
				deleteNetwork = vidUtils.createXmlNetworkRequestInstance(execution)
			}
				
			deleteNetwork = utils.formatXml(deleteNetwork)
			utils.logAudit(deleteNetwork)
			execution.setVariable(Prefix + "networkRequest", deleteNetwork)
			utils.log("DEBUG", Prefix + "networkRequest - " + '\n' + deleteNetwork, isDebugEnabled)
				
			// validate 'backout-on-failure' to override 'URN_mso_rollback'
			boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, deleteNetwork)
			execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
			utils.log("DEBUG", Prefix + "rollbackEnabled - " + rollbackEnabled, isDebugEnabled)
			
			String networkInputs = utils.getNodeXml(deleteNetwork, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable(Prefix + "networkInputs", networkInputs)
			
			// prepare messageId
			String messageId = execution.getVariable("testMessageId")  // for testing
			if (messageId == null || messageId == "") {
					messageId = UUID.randomUUID()
					utils.log("DEBUG", Prefix + "messageId, random generated: " + messageId, isDebugEnabled)
			} else {
					utils.log("DEBUG", Prefix + "messageId, pre-assigned: " + messageId, isDebugEnabled)
			}
			execution.setVariable(Prefix + "messageId", messageId)
				
			String source = utils.getNodeText1(deleteNetwork, "source")
			execution.setVariable(Prefix + "source", source)
			utils.log("DEBUG", Prefix + "source - " + source, isDebugEnabled)
			
			String networkId = ""
			if (utils.nodeExists(networkInputs, "network-id")) {
				networkId = utils.getNodeText1(networkInputs, "network-id")
				if (networkId == null || networkId == "" || networkId == 'null' ) {
					sendSyncError(execution)
					// missing value of network-id
					String dataErrorMessage = "network-request has missing 'network-id' element/value."
					utils.log("DEBUG", " Invalid Request - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}

			// lcpCloudRegion or tenantId not sent, will be extracted from query AA&I
			def lcpCloudRegion = null
			if (utils.nodeExists(networkInputs, "aic-cloud-region")) {
				lcpCloudRegion = utils.getNodeText1(networkInputs, "aic-cloud-region")
				if (lcpCloudRegion == 'null') {
					lcpCloudRegion = null
				}
			}
			execution.setVariable(Prefix + "lcpCloudRegion", lcpCloudRegion)
			utils.log("DEBUG", "lcpCloudRegion : " + lcpCloudRegion, isDebugEnabled)
			
			String tenantId = null
			if (utils.nodeExists(networkInputs, "tenant-id")) {
				tenantId = utils.getNodeText1(networkInputs, "tenant-id")
				if (tenantId == 'null') {
					tenantId = null
				}

			}
			execution.setVariable(Prefix + "tenantId", tenantId)
			utils.log("DEBUG", "tenantId : " + tenantId, isDebugEnabled)
			
			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG", "sdncVersion? : " + sdncVersion, isDebugEnabled)
			
			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = execution.getVariable("URN_mso_adapters_po_auth")
			utils.log("DEBUG", " Obtained BasicAuth userid password for PO/SDNC adapter: " + basicAuthValuePO, isDebugEnabled)
			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, execution.getVariable("URN_mso_msoKey"))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)
	
			} catch (IOException ex) {
				String dataErrorMessage = " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
				utils.log("DEBUG", dataErrorMessage, , isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			 // caught exception
			String exceptionMessage = "Exception Encountered in DoDeleteNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
		
	}


	public void callRESTQueryAAI (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAI() of DoDoDeleteNetworkInstance ***** " , isDebugEnabled)

		// get variables
		String networkInputs  = execution.getVariable(Prefix + "networkInputs")
		String networkId   = utils.getNodeText(networkInputs, "network-id")
		networkId = UriUtils.encode(networkId,"UTF-8")

		// Prepare AA&I url
		String aai_endpoint = execution.getVariable("URN_aai_endpoint")
		AaiUtil aaiUriUtil = new AaiUtil(this)
		String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
		String queryAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId + "?depth=1"
		utils.logAudit(queryAAIRequest)
		execution.setVariable(Prefix + "queryAAIRequest", queryAAIRequest)
		utils.log("DEBUG", Prefix + "AAIRequest - " + "\n" + queryAAIRequest, isDebugEnabled)

		RESTConfig config = new RESTConfig(queryAAIRequest);

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		Boolean isVfRelationshipExist = false
		try {
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiReturnCode", returnCode)

			utils.log("DEBUG", " ***** AAI Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()
			aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
			execution.setVariable(Prefix + "queryAAIResponse", aaiResponseAsString)

			if (returnCode=='200' || returnCode=='204') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable(Prefix + "isAAIGood", true)
				utils.log("DEBUG", " AAI Query Success REST Response - " + "\n" + aaiResponseAsString, isDebugEnabled)
				// verify if vf or vnf relationship exist
				if (utils.nodeExists(aaiResponseAsString, "relationship")) {
					NetworkUtils networkUtils = new NetworkUtils()
			        isVfRelationshipExist = networkUtils.isVfRelationshipExist(aaiResponseAsString)
					execution.setVariable(Prefix + "isVfRelationshipExist", isVfRelationshipExist)
					if (isVfRelationshipExist) {
						String relationshipMessage = "AAI Query Success Response but 'vf-module' relationship exist, not allowed to delete: network Id: " + networkId
						exceptionUtil.buildWorkflowException(execution, 2500, relationshipMessage)

					} else {
					    // verify if lcpCloudRegion was sent as input, if not get value from AAI Response 
					    if (execution.getVariable(Prefix + "lcpCloudRegion") == null ) {
							String lcpCloudRegion = networkUtils.getCloudRegion(aaiResponseAsString)
							execution.setVariable(Prefix + "lcpCloudRegion", lcpCloudRegion)
							utils.log("DEBUG", " Get AAI getCloudRegion()  : " + lcpCloudRegion, isDebugEnabled)
						}
						if (execution.getVariable(Prefix + "tenantId") == null ) {
							String tenantId = networkUtils.getTenantId(aaiResponseAsString)
							execution.setVariable(Prefix + "tenantId", tenantId)
							utils.log("DEBUG", " Get AAI getTenantId()  : " + tenantId, isDebugEnabled)
						}
					
					}
				}
				utils.log("DEBUG", Prefix + "isVfRelationshipExist - " + isVfRelationshipExist, isDebugEnabled)

			} else {
				execution.setVariable(Prefix + "isAAIGood", false)
			    if (returnCode=='404' || aaiResponseAsString == "" || aaiResponseAsString == null) {
					// not found // empty aai response
					execution.setVariable(Prefix + "isSilentSuccess", true)
					utils.log("DEBUG", " AAI Query is Silent Success", isDebugEnabled)

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)

				   } else {
 			       	  // aai all errors
						 String dataErrorMessage = "Unexpected Error Response from callRESTQueryAAI() - " + returnCode
						 utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
						 exceptionUtil.buildWorkflowException(execution, 2500, dataErrorMessage)

			      }
				}
			}

			utils.log("DEBUG", " AAI Query call, isAAIGood? : " + execution.getVariable(Prefix + "isAAIGood"), isDebugEnabled)

		} catch (Exception ex) {
		   // caught exception
		   String exceptionMessage = "Exception Encountered in DoDeleteNetworkInstance, callRESTQueryAAI() - " + ex.getMessage()
		   utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAICloudRegion (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAICloudRegion of DoDeleteNetworkInstance ***** " , isDebugEnabled)

		try {
			String networkInputs  = execution.getVariable(Prefix + "networkInputs")
			// String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
			String cloudRegion = execution.getVariable(Prefix + "lcpCloudRegion")
			cloudRegion = UriUtils.encode(cloudRegion,"UTF-8")
			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			utils.logAudit(queryCloudRegionRequest)
			execution.setVariable(Prefix + "queryCloudRegionRequest", queryCloudRegionRequest)
			utils.log("DEBUG", Prefix + "queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest, isDebugEnabled)

			String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
			String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

			if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
				execution.setVariable(Prefix + "cloudRegionPo", cloudRegionPo)
				execution.setVariable(Prefix + "cloudRegionSdnc", cloudRegionSdnc)

			} else {
				String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable(Prefix + "queryCloudRegionReturnCode")
				utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			}

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, callRESTQueryAAICloudRegion(). Unexpected Response from AAI - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareNetworkRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareNetworkRequest of DoDeleteNetworkInstance ***** ", isDebugEnabled)
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String cloudSiteId = execution.getVariable(Prefix + "cloudRegionPo")
			String tenantId = execution.getVariable(Prefix + "tenantId")

			String queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")
			String networkType = utils.getNodeText1(queryAAIResponse, "network-type")
			String networkId = utils.getNodeText1(queryAAIResponse, "network-id")
			String backoutOnFailure = execution.getVariable(Prefix + "rollbackEnabled")
			
			String networkStackId = ""
			networkStackId = utils.getNodeText1(queryAAIResponse, "heat-stack-id")
			if (networkStackId == 'null' || networkStackId == "" || networkStackId == null) {
				networkStackId = "force_delete"
			}

			String requestId = execution.getVariable("msoRequestId")
			if (requestId != null) {
				execution.setVariable("mso-request-id", requestId)
			} else {
				requestId = execution.getVariable("mso-request-id")
			}
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// Added new Elements
			String messageId = execution.getVariable(Prefix + "messageId")
			String notificationUrl = ""                                   //TODO - is this coming from URN? What variable/value to use?
			//String notificationUrl = execution.getVariable("URN_?????") //TODO - is this coming from URN? What variable/value to use?

			String modelCustomizationUuid = ""			
			if (utils.nodeExists(networkRequest, "networkModelInfo")) {
				String networkModelInfo = utils.getNodeXml(networkRequest, "networkModelInfo", false).replace("tag0:","").replace(":tag0","")
				modelCustomizationUuid = utils.getNodeText1(networkModelInfo, "modelCustomizationUuid")
			} else {
			    modelCustomizationUuid = utils.getNodeText1(networkRequest, "modelCustomizationId")
			}
			
			String deleteNetworkRequest = """
					  <deleteNetworkRequest>
					    <cloudSiteId>${cloudSiteId}</cloudSiteId>
					    <tenantId>${tenantId}</tenantId>
					    <networkId>${networkId}</networkId>
						<networkStackId>${networkStackId}</networkStackId>
					    <networkType>${networkType}</networkType>
						<modelCustomizationUuid>${modelCustomizationUuid}</modelCustomizationUuid>
						<skipAAI>true</skipAAI>
					    <msoRequest>
					       <requestId>${requestId}</requestId>
					       <serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
					    </msoRequest>
						<messageId>${messageId}</messageId>
						<notificationUrl>${notificationUrl}</notificationUrl>
					  </deleteNetworkRequest>
						""".trim()

			utils.log("DEBUG", Prefix + "deleteNetworkRequest - " + "\n" +  deleteNetworkRequest, isDebugEnabled)
			// Format Response
			String buildDeleteNetworkRequestAsString = utils.formatXml(deleteNetworkRequest)
			utils.logAudit(buildDeleteNetworkRequestAsString)
			utils.log("DEBUG", Prefix + "deleteNetworkRequestAsString - " + "\n" +  buildDeleteNetworkRequestAsString, isDebugEnabled)

			String restURL = execution.getVariable("URN_mso_adapters_network_rest_endpoint")
			execution.setVariable("URN_mso_adapters_network_rest_endpoint", restURL + "/" + networkId)
			utils.log("DEBUG", "URN_mso_adapters_network_rest_endpoint - " + "\n" +  restURL + "/" + networkId, isDebugEnabled)

			execution.setVariable(Prefix + "deleteNetworkRequest", buildDeleteNetworkRequestAsString)
			utils.log("DEBUG", Prefix + "deleteNetworkRequest - " + "\n" +  buildDeleteNetworkRequestAsString, isDebugEnabled)
		}
		catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareNetworkRequest(). Unexpected Response from AAI - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	/**
	 * This method is used instead of an HTTP Connector task because the
	 * connector does not allow DELETE with a body.
	 */
	public void sendRequestToVnfAdapter(Execution execution) {
		def method = getClass().getSimpleName() + '.sendRequestToVnfAdapter(' +
			'execution=' + execution.getId() +
			')'
		def isDebugEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugEnabled)

		try {

			String vnfAdapterUrl = execution.getVariable("URN_mso_adapters_network_rest_endpoint")
			String vnfAdapterRequest = execution.getVariable(Prefix + "deleteNetworkRequest")

			RESTConfig config = new RESTConfig(vnfAdapterUrl)
			RESTClient client = new RESTClient(config).
				addHeader("Content-Type", "application/xml").
				addAuthorizationHeader(execution.getVariable("BasicAuthHeaderValuePO"));

			APIResponse response;

			response = client.httpDelete(vnfAdapterRequest)

			execution.setVariable(Prefix + "networkReturnCode", response.getStatusCode())
			execution.setVariable(Prefix + "deleteNetworkResponse", response.getResponseBodyAsString())

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, sendRequestToVnfAdapter() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}


	public void prepareSDNCRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRequest of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText1(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}
			
			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")

			// get/set 'msoRequestId' and 'mso-request-id'
			String requestId = execution.getVariable("msoRequestId")
			if (requestId != null) {
				execution.setVariable("mso-request-id", requestId)
			} else {
			    requestId = execution.getVariable("mso-request-id")
			} 	
			execution.setVariable(Prefix + "requestId", requestId)
			utils.log("DEBUG", Prefix + "requestId " + requestId, isDebugEnabled)
			String queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")
			
			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyDeleteRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "delete", "DisconnectNetworkRequest", cloudRegionId, networkId, queryAAIResponse, null)
		    String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
			utils.logAudit(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
			utils.log("DEBUG", Prefix + "deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRequest() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareRpcSDNCRequest of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText1(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}

			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")
			
			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "unassign", "DeleteNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			utils.logAudit(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
			utils.log("DEBUG", Prefix + "deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRequest() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	
	public void prepareRpcSDNCDeactivate(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside prepareRpcSDNCDeactivate() of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
		
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText1(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")
			
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "deactivate", "DeleteNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			execution.setVariable(Prefix + "deactivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
			utils.log("DEBUG", " Preparing request for RPC SDNC Topology deactivate - " + "\n" +  sndcTopologyRollbackRpcRequestAsString, isDebugEnabled)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCActivateRollback() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void validateNetworkResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateNetworkResponse of DoDeleteNetworkInstance *****", isDebugEnabled)

		try {
			String returnCode = execution.getVariable(Prefix + "networkReturnCode")
			String networkResponse = execution.getVariable(Prefix + "deleteNetworkResponse")

			utils.log("DEBUG", " Network Adapter responseCode: " + returnCode, isDebugEnabled)
			utils.log("DEBUG", "Network Adapter Response - " + "\n" + networkResponse, isDebugEnabled)
			utils.logAudit(networkResponse)
			
			String errorMessage = ""
			if (returnCode == "200") {
				utils.log("DEBUG", " Network Adapter Response is successful - " + "\n" + networkResponse, isDebugEnabled)

				// prepare rollback data
				String rollbackData = utils.getNodeXml(networkResponse, "rollback", false).replace("tag0:","").replace(":tag0","")
				if ((rollbackData == null) || (rollbackData.isEmpty())) {
					utils.log("DEBUG", " Network Adapter 'rollback' data is not Sent: " + "\n" + networkResponse, isDebugEnabled)
					execution.setVariable(Prefix + "rollbackNetworkRequest", "")
				} else {
				    String rollbackNetwork =
					  """<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.openecomp.mso/network">
						  	${rollbackData}
						 </NetworkAdapter:rollbackNetwork>"""
				    String rollbackNetworkXml = utils.formatXml(rollbackNetwork)
				    execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkXml)
				    utils.log("DEBUG", " Network Adapter rollback data - " + "\n" + rollbackNetworkXml, isDebugEnabled)
				}	
				
				
			} else { // network error
			   if (returnCode.toInteger() > 399 && returnCode.toInteger() < 600) {   //4xx, 5xx
				   if (networkResponse.contains("deleteNetworkError")  ) {
					   networkResponse = networkResponse.replace('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>', '')
					   errorMessage  = utils.getNodeText1(networkResponse, "message")
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
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, validateNetworkResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void validateSDNCResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateSDNCResponse of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		String response = execution.getVariable(Prefix + "deleteSDNCResponse")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String deleteSDNCResponseDecodeXml = sdncAdapterUtils.decodeXML(execution.getVariable(Prefix + "deleteSDNCResponse"))
		deleteSDNCResponseDecodeXml = deleteSDNCResponseDecodeXml.replace("&", "&amp;").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "deleteSDNCResponse", deleteSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncRollbackNeeded", true)      //
			execution.setVariable(Prefix + "isPONR", true)
			utils.log("DEBUG", "Successfully Validated SDNC Response", isDebugEnabled)
		} else {
			utils.log("DEBUG", "Did NOT Successfully Validated SDNC Response", isDebugEnabled)
		 	throw new BpmnError("MSOWorkflowException")
		}

	}

	public void validateRpcSDNCDeactivateResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside validateRpcSDNCDeactivateResponse() of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		String response = execution.getVariable(Prefix + "deactivateSDNCResponse")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String assignSDNCResponseDecodeXml = sdncAdapterUtils.decodeXML(execution.getVariable(Prefix + "deactivateSDNCResponse"))
		assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace("&", "&amp;").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "deactivateSDNCResponse", assignSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncDeactivateRollbackNeeded", true)
			utils.log("DEBUG", "Successfully Validated Rpc SDNC Activate Response", isDebugEnabled)

		} else {
			utils.log("DEBUG", "Did NOT Successfully Validated Rpc SDNC Deactivate Response", isDebugEnabled)
			throw new BpmnError("MSOWorkflowException")
		}

	}
	
	public void prepareRpcSDNCDeactivateRollback(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside prepareRpcSDNCDeactivateRollback() of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
		
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String deactivateSDNCResponse = execution.getVariable(Prefix + "deactivateSDNCResponse")
			String networkId = utils.getNodeText1(deactivateSDNCResponse, "network-id")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "activate", "CreateNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
			utils.log("DEBUG", " Preparing request for RPC SDNC Topology 'activate-CreateNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString, isDebugEnabled)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCDeactivateRollback() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	public void prepareRollbackData(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside prepareRollbackData() of DoDeleteNetworkInstance ***** ", isDebugEnabled)
		
		try {
			
			Map<String, String> rollbackData = new HashMap<String, String>();
			String rollbackNetworkRequest = execution.getVariable(Prefix + "rollbackNetworkRequest")
			if (rollbackNetworkRequest != null) {
				if (rollbackNetworkRequest != "") {
				   rollbackData.put("rollbackNetworkRequest", execution.getVariable(Prefix + "rollbackNetworkRequest"))
			    }
			}	
			String rollbackDeactivateSDNCRequest = execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest")
			if (rollbackDeactivateSDNCRequest != null) {
				if (rollbackDeactivateSDNCRequest != "") {
			        rollbackData.put("rollbackDeactivateSDNCRequest", execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest"))
			    }
			}		
			execution.setVariable("rollbackData", rollbackData)
			utils.log("DEBUG", "** rollbackData : " + rollbackData, isDebugEnabled)
			
			execution.setVariable("WorkflowException", execution.getVariable("WorkflowException"))
			utils.log("DEBUG", "** WorkflowException : " + execution.getVariable("WorkflowException"), isDebugEnabled)
			
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		
		}
		
	}
	
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
			
			utils.log("DEBUG", " ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"), isDebugEnabled)
			if (execution.getVariable(Prefix + "isException") == false) {
				execution.setVariable(Prefix + "Success", true)
				execution.setVariable("WorkflowException", null)
				if (execution.getVariable(Prefix + "isSilentSuccess") == true) {
					execution.setVariable("rolledBack", false)
				} else {
				    execution.setVariable("rolledBack", true) 
				}
				prepareSuccessRollbackData(execution) // populate rollbackData
				
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
			       } else {
				      if (execution.getVariable("WorkflowException") != null) {
					     WorkflowException pwfex = execution.getVariable("WorkflowException")
					     exceptionMessage = pwfex.getErrorMessage()
					  }
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
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, postProcessResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
			throw new BpmnError("MSOWorkflowException")
			
        }

	}

	public void prepareSuccessRollbackData(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside prepareSuccessRollbackData() of DoDeleteNetworkInstance ***** ", isDebugEnabled)
		
		try {
			
			if (execution.getVariable("sdncVersion") != '1610') {
				prepareRpcSDNCDeactivateRollback(execution)
				prepareRpcSDNCUnassignRollback(execution)
			} else {
			    prepareSDNCRollback(execution)
			}	
			
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
			String rollbackDeactivateSDNCRequest = execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest")
			if (rollbackDeactivateSDNCRequest != null) {
				if (rollbackDeactivateSDNCRequest != "") {
			        rollbackData.put("rollbackDeactivateSDNCRequest", execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest"))
			    }
			}		
			execution.setVariable("rollbackData", rollbackData)
			
			utils.log("DEBUG", "** rollbackData : " + rollbackData, isDebugEnabled)
			execution.setVariable("WorkflowException", null)

			
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		
		}
		
	}

	public void prepareRpcSDNCUnassignRollback(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside prepareRpcSDNCUnassignRollbac() of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
		
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String deleteSDNCResponse = execution.getVariable(Prefix + "deleteSDNCResponse")
			String networkId = utils.getNodeText1(deleteSDNCResponse, "network-id")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")
				
			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "assign", "CreateNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			utils.logAudit(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyDeleteRequesAsString)
			utils.log("DEBUG", Prefix + "rollbackSDNCRequest" + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)
			utils.log("DEBUG", " Preparing request for RPC SDNC Topology 'assign-CreateNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)
	

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCUnassignRollback() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	public void prepareSDNCRollback (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRollback of DoDeleteNetworkInstance ***** ", isDebugEnabled)

		try {
			
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText1(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}
			
			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")

			// get/set 'msoRequestId' and 'mso-request-id'
			String requestId = execution.getVariable("msoRequestId")
			if (requestId != null) {
				execution.setVariable("mso-request-id", requestId)
			} else {
			    requestId = execution.getVariable("mso-request-id")
			} 	
			execution.setVariable(Prefix + "requestId", requestId)
			
			String queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")
			
			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyDeleteRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "rollback", "DisconnectNetworkRequest", cloudRegionId, networkId, queryAAIResponse, null)
		    String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
			utils.logAudit(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyDeleteRequesAsString)
			utils.log("DEBUG", Prefix + "rollbackSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)
			utils.log("DEBUG", " Preparing request for RPC SDNC Topology 'rollback-DisconnectNetworkRequest' rollback . . . - " + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)


		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRollback() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}
	
	public void setExceptionFlag(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside setExceptionFlag() of DoDeleteNetworkInstance ***** ", isDebugEnabled)
		
		try {

			execution.setVariable(Prefix + "isException", true)
			
			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			utils.log("DEBUG", Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"), isDebugEnabled)
			
		} catch(Exception ex){
			  String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
		}
		
	}
	
	
	// *******************************
	//     Build Error Section
	// *******************************

	public void processJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")
			
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method of " + Prefix, isDebugEnabled)
	}

}
