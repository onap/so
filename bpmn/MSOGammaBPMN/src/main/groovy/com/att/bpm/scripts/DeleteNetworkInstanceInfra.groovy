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

import groovy.xml.XmlUtil
import groovy.json.*

public class DeleteNetworkInstanceInfra extends AbstractServiceTaskProcessor {
	String Prefix="DELNWKI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)

	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("DELNWKI_DeleteNetworkInstanceInfraJsonRequest", "")
		execution.setVariable("DELNWKI_networkRequest", "")
		execution.setVariable("DELNWKI_networkRESTRequest", "")
		execution.setVariable("DELNWKI_CompleteMsoProcessRequest", "")
		execution.setVariable("DELNWKI_FalloutHandlerRequest", "")
		execution.setVariable("DELNWKI_isSilentSuccess", false)
		execution.setVariable("DELNWKI_Success", false)
		execution.setVariable("DELNWKI_isPONR", false)    // Point-of-no-return, means, rollback is not needed

		execution.setVariable("DELNWKI_requestId", "")
		execution.setVariable("DELNWKI_source", "")
		execution.setVariable("DELNWKI_networkInputs", "")

		execution.setVariable("DELNWKI_queryAAIRequest","")
		execution.setVariable("DELNWKI_queryAAIResponse", "")
		execution.setVariable("DELNWKI_aaiReturnCode", "")
		execution.setVariable("DELNWKI_isAAIGood", false)
		execution.setVariable("DELNWKI_isVfRelationshipExist", false)

		// AAI query Cloud Region
		execution.setVariable("DELNWKI_queryCloudRegionRequest","")
		execution.setVariable("DELNWKI_queryCloudRegionReturnCode","")
		execution.setVariable("DELNWKI_queryCloudRegionResponse","")
		execution.setVariable("DELNWKI_cloudRegionPo","")
		execution.setVariable("DELNWKI_cloudRegionSdnc","")

		execution.setVariable("DELNWKI_deleteNetworkRequest", "")
		execution.setVariable("DELNWKI_deleteNetworkResponse", "")
		execution.setVariable("DELNWKI_networkReturnCode", "")

		execution.setVariable("DELNWKI_deleteSDNCRequest", "")
		execution.setVariable("DELNWKI_deleteSDNCResponse", "")
		execution.setVariable("DELNWKI_rollbackSDNCRequest", "")
		execution.setVariable("DELNWKI_rollbackSDNCResponse", "")
		execution.setVariable("DELNWKI_sdncReturnCode", "")
		execution.setVariable("DELNWKI_rollbackSDNCReturnCode", "")
		execution.setVariable("DELNWKI_isSdncRollbackNeeded", false)
		execution.setVariable("DELNWKI_sdncResponseSuccess", false)

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************

	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest() DeleteNetworkV2 Request ***** ", isDebugEnabled)

		// initialize flow variables
		InitializeProcessVariables(execution)

		// get incoming message/input
		String deleteNetworkJsonIncoming = execution.getVariable("bpmnRequest")
		utils.logAudit(deleteNetworkJsonIncoming)

		try {
			def prettyJson = JsonOutput.prettyPrint(deleteNetworkJsonIncoming.toString())
			utils.log("DEBUG", " Incoming message formatted . . . : " + '\n' + prettyJson, isDebugEnabled)

		} catch (Exception ex) {
			String dataErrorMessage = " Invalid json format Request - " + ex.getMessage()
			utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
		}

		// prepare messageId
		String messageId = execution.getVariable("DELNWKI_messageId") // test scenario
		if (messageId == null || messageId == "") {
			messageId = UUID.randomUUID()
			utils.log("DEBUG", " DELNWKI_messageId, random generated: " + messageId, isDebugEnabled)
		} else {
			utils.log("DEBUG", " DELNWKI_messageId, pre-assigned: " + messageId, isDebugEnabled)
		}
		execution.setVariable("DELNWKI_messageId", messageId)

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

			execution.setVariable("DELNWKI_DeleteNetworkInstanceInfraJsonRequest", deleteNetworkJsonIncoming)

			// recreate the xml network-request
			String networkRequest =  vidUtils.createXmlNetworkRequestInfra(execution, deleteNetworkJsonIncoming)
			execution.setVariable("DELNWKI_networkRequest", networkRequest)
			utils.log("DEBUG", " network-request - " + '\n' + networkRequest, isDebugEnabled)

			String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable("DELNWKI_networkInputs", networkInputs)

			String requestId = execution.getVariable("att-mso-request-id")
			if (requestId == null || requestId == "") {
				requestId = utils.getNodeText(networkRequest, "request-id")
			}
			execution.setVariable("DELNWKI_requestId", requestId)
			execution.setVariable("DELNWKI_source", utils.getNodeText(networkRequest, "source"))

			String networkId = ""
			if (utils.nodeExists(networkInputs, "network-id")) {
				networkId = utils.getNodeText1(networkInputs, "network-id")
				if (networkId == 'null' || networkId == "") {
					sendSyncError(execution)
					// missing value of network-name
					String dataErrorMessage = "network-request has missing 'network-id' element/value."
					utils.log("DEBUG", " Invalid Request - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}

			String lcpCloudRegion = ""
			if (utils.nodeExists(networkInputs, "aic-cloud-region")) {
				lcpCloudRegion = utils.getNodeText1(networkInputs, "aic-cloud-region")
				if ((lcpCloudRegion == 'null') || (lcpCloudRegion == "")) {
					sendSyncError(execution)
					String dataErrorMessage = "network-request has missing 'aic-cloud-region' element/value."
					utils.log("DEBUG", " Invalid Request - " + dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}


		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			 // caught exception
			String exceptionMessage = "Exception Encountered in DeleteNetworkInstanceInfra, PreProcessRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncResponse() of DeleteNetworkV2 ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("att-mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String deleteNetworkRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSyncResponse to APIH - " + "\n" + deleteNetworkRestRequest, isDebugEnabled)

			sendWorkflowResponse(execution, 202, deleteNetworkRestRequest)

		} catch (Exception ex) {
			 // caught exception
			String exceptionMessage = "Exception Encountered in DeleteNetworkInstanceInfra, sendSyncResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAI (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAI() of DeleteNetworkV2 ***** " , isDebugEnabled)

		// get variables
		String networkInputs  = execution.getVariable("DELNWKI_networkInputs")
		String networkId   = utils.getNodeText(networkInputs, "network-id")
		networkId = UriUtils.encode(networkId,"UTF-8")
		String messageId = execution.getVariable("DELNWKI_messageId")

		// Prepare AA&I url
		String aai_endpoint = execution.getVariable("URN_aai_endpoint")
		AaiUtil aaiUriUtil = new AaiUtil(this)
		String aai_uri = aaiUriUtil.getNetworkL3NetworkUri(execution)
		String queryAAIRequest = "${aai_endpoint}${aai_uri}/" + networkId
		utils.logAudit(queryAAIRequest)
		execution.setVariable("DELNWKI_queryAAIRequest", queryAAIRequest)
		utils.log("DEBUG", " DELNWKI_AAIRequest - " + "\n" + queryAAIRequest, isDebugEnabled)

		String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

		RESTConfig config = new RESTConfig(queryAAIRequest);

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		Boolean isVfRelationshipExist = false
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse response = client.get()
			String returnCode = response.getStatusCode()
			execution.setVariable("DELNWKI_aaiReturnCode", returnCode)

			utils.log("DEBUG", " ***** AAI Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()
			aaiResponseAsString = StringEscapeUtils.unescapeXml(aaiResponseAsString)
			execution.setVariable("DELNWKI_queryAAIResponse", aaiResponseAsString)

			if (returnCode=='200' || returnCode=='204') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable("DELNWKI_isAAIGood", true)
				utils.log("DEBUG", " AAI Query Success REST Response - " + "\n" + aaiResponseAsString, isDebugEnabled)
				// verify if vf or vnf relationship exist
				if (utils.nodeExists(aaiResponseAsString, "relationship")) {
					NetworkUtils networkUtils = new NetworkUtils()
			        isVfRelationshipExist = networkUtils.isVfRelationshipExist(aaiResponseAsString)
					execution.setVariable("DELNWKI_isVfRelationshipExist", isVfRelationshipExist)
					if (isVfRelationshipExist == true) {
						String relationshipMessage = "AAI Query Success Response but 'vf-module' relationship exist, not allowed to delete: network Id: " + networkId
						exceptionUtil.buildWorkflowException(execution, 2500, relationshipMessage)

					}
				}
				utils.log("DEBUG", " DELNWKI_isVfRelationshipExist - " + isVfRelationshipExist, isDebugEnabled)

			} else {
				execution.setVariable("DELNWKI_isAAIGood", false)
			    if (returnCode=='404' || aaiResponseAsString == "" || aaiResponseAsString == null) {
					// not found // empty aai response
					execution.setVariable("DELNWKI_isSilentSuccess", true)
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

			utils.log("DEBUG", " AAI Query call, isAAIGood? : " + execution.getVariable("DELNWKI_isAAIGood"), isDebugEnabled)

		} catch (Exception ex) {
		   // caught exception
		   String exceptionMessage = "Exception Encountered in DeleteNetworkInstanceInfra, callRESTQueryAAI() - " + ex.getMessage()
		   utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAICloudRegion (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAICloudRegion of DeleteNetworkV2 ***** " , isDebugEnabled)

		try {
			String networkInputs  = execution.getVariable("DELNWKI_networkInputs")
			String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
			cloudRegion = UriUtils.encode(cloudRegion,"UTF-8")
			// Prepare AA&I url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUtil = new AaiUtil(this)
			String aai_uri = aaiUtil.getCloudInfrastructureCloudRegionUri(execution)
			String queryCloudRegionRequest = "${aai_endpoint}${aai_uri}/" + cloudRegion
			utils.logAudit(queryCloudRegionRequest)
			execution.setVariable("DELNWKI_queryCloudRegionRequest", queryCloudRegionRequest)
			utils.log("DEBUG", " DELNWKI_queryCloudRegionRequest - " + "\n" + queryCloudRegionRequest, isDebugEnabled)

			String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
			String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

			if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
				execution.setVariable("DELNWKI_cloudRegionPo", cloudRegionPo)
				execution.setVariable("DELNWKI_cloudRegionSdnc", cloudRegionSdnc)

			} else {
				String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable("DELNWKI_queryCloudRegionReturnCode")
				utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			}

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, callRESTQueryAAICloudRegion(). Unexpected Response from AAI - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareNetworkRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareNetworkRequest of DeleteNetworkV2 ***** ", isDebugEnabled)
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		try {
			// get variables
			String cloudSiteId = execution.getVariable("DELNWKI_cloudRegionPo")
			String networkInputs = execution.getVariable("DELNWKI_networkInputs")
			String tenantId = utils.getNodeText(networkInputs, "tenant-id")
			String networkType = utils.getNodeText(networkInputs, "network-type")
			String networkId = utils.getNodeText(networkInputs, "network-id")

			String networkStackId = ""
			networkStackId = utils.getNodeText1(execution.getVariable("DELNWKI_queryAAIResponse"), "heat-stack-id")
			if (networkStackId == 'null' || networkStackId == "") {
				networkStackId = "force_delete"
			}

			String requestId = execution.getVariable("DELNWKI_requestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// Added new Elements
			String messageId = execution.getVariable("DELNWKI_messageId")
			String notificationUrl = ""                                   //TODO - is this coming from URN? What variable/value to use?
			//String notificationUrl = execution.getVariable("URN_?????") //TODO - is this coming from URN? What variable/value to use?

			String deleteNetworkRequest = """
					  <deleteNetworkRequest>
					    <cloudSiteId>${cloudSiteId}</cloudSiteId>
					    <tenantId>${tenantId}</tenantId>
					    <networkId>${networkId}</networkId>
						<networkStackId>${networkStackId}</networkStackId>
					    <networkType>${networkType}</networkType>
						<skipAAI>true</skipAAI>
					    <msoRequest>
					       <requestId>${requestId}</requestId>
					       <serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
					    </msoRequest>
						<messageId>${messageId}</messageId>
						<notificationUrl>${notificationUrl}</notificationUrl>
					  </deleteNetworkRequest>
						""".trim()

			utils.log("DEBUG", " DELNWKI_deleteNetworkRequest - " + "\n" +  deleteNetworkRequest, isDebugEnabled)
			// Format Response
			String buildDeleteNetworkRequestAsString = utils.formatXml(deleteNetworkRequest)
			utils.logAudit(buildDeleteNetworkRequestAsString)
			utils.log("DEBUG", " DELNWKI_deleteNetworkRequestAsString - " + "\n" +  buildDeleteNetworkRequestAsString, isDebugEnabled)

			String restURL = execution.getVariable("URN_mso_adapters_network_rest_endpoint")
			execution.setVariable("URN_mso_adapters_network_rest_endpoint", restURL + "/" + networkId)
			utils.log("DEBUG", "URN_mso_adapters_network_rest_endpoint - " + "\n" +  restURL + "/" + networkId, isDebugEnabled)

			execution.setVariable("DELNWKI_deleteNetworkRequest", buildDeleteNetworkRequestAsString)
			utils.log("DEBUG", " DELNWKI_deleteNetworkRequest - " + "\n" +  buildDeleteNetworkRequestAsString, isDebugEnabled)
		}
		catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, prepareNetworkRequest(). Unexpected Response from AAI - " + ex.getMessage()
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
			String vnfAdapterRequest = execution.getVariable("DELNWKI_deleteNetworkRequest")

			RESTConfig config = new RESTConfig(vnfAdapterUrl)
			RESTClient client = new RESTClient(config).
				addHeader("Content-Type", "application/xml").
				addAuthorizationHeader(execution.getVariable("BasicAuthHeaderValuePO"));

			APIResponse response;

			response = client.httpDelete(vnfAdapterRequest)

			execution.setVariable("DELNWKI_networkReturnCode", response.getStatusCode())
			execution.setVariable("DELNWKI_deleteNetworkResponse", response.getResponseBodyAsString())

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, sendRequestToVnfAdapter() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}


	public void prepareSDNCRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareSDNCRequest of DeleteNetworkV2 ***** ", isDebugEnabled)

		try {
			// get variables
			String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			String deleteNetworkInput = execution.getVariable("DELNWKI_networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText1(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}

			String serviceInstanceId = utils.getNodeText1(deleteNetworkInput, "service-instance-id")

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable("DELNWKI_cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyDeleteRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "delete", "DisconnectNetworkRequest", cloudRegionId, networkId, null)
		    String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
			utils.logAudit(sndcTopologyDeleteRequesAsString)
			execution.setVariable("DELNWKI_deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
			utils.log("DEBUG", " DELNWKI_deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString, isDebugEnabled)

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "rollback", "DisconnectNetworkRequest", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
			execution.setVariable("DELNWKI_rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
			utils.log("DEBUG", " DELNWKI_rollbackSDNCRequest (prepared if NEEDED later for SDNC Topology delete's rollback/compensation . . . - " + "\n" +  sndcTopologyRollbackRequestAsString, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, prepareSDNCRequest() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareDBRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequest of DeleteNetworkV2 ***** ", isDebugEnabled)

			String aaiReturnCode = execution.getVariable("DELNWKI_aaiReturnCode")
			String requestId = execution.getVariable("DELNWKI_requestId")

			String statusMessage = ""
			if (aaiReturnCode == '404' || execution.getVariable("DELNWKI_isSilentSuccess") == true) {
				// SILENT SUCCESS
				statusMessage = "Network Id to be deleted NOT found. Silent success."
			} else {
				statusMessage = "Network successfully deleted."
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
								<requestStatus>COMPLETED</requestStatus>
								<progress>100</progress>
								<vnfOutputs>&lt;network-outputs xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1" xmlns:aetgt="http://ecomp.att.com/mso/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"/&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		   utils.logAudit(buildDeleteDBRequestAsString)
		   execution.setVariable("DELNWKI_deleteDBRequest", buildDeleteDBRequestAsString)
		   utils.log("DEBUG", " DB Adapter Request - " + "\n" + buildDeleteDBRequestAsString, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, prepareDBRequest() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	public void prepareDBRequestError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequestError of DeleteNetworkV2 ***** ", isDebugEnabled)

			WorkflowException wfe = execution.getVariable("WorkflowException")
			String statusMessage = wfe.getErrorMessage()
			String requestId = execution.getVariable("DELNWKI_requestId")

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
								<progress></progress>
								<vnfOutputs>&lt;network-outputs xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1" xmlns:aetgt="http://ecomp.att.com/mso/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"/&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   execution.setVariable("DELNWKI_deleteDBRequest", dbRequest)
		   utils.log("DEBUG", " DB Adapter Request - " + "\n" + dbRequest, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, prepareDBRequestError() - " + ex.getMessage()
			logError(exceptionMessage)
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

		utils.log("DEBUG", " ***** Inside validateNetworkResponse of DeleteNetworkV2 *****", isDebugEnabled)

		try {
			String returnCode = execution.getVariable("DELNWKI_networkReturnCode")
			String networkResponse = execution.getVariable("DELNWKI_deleteNetworkResponse")

			utils.log("DEBUG", " Network Adapter responseCode: " + returnCode, isDebugEnabled)
			utils.log("DEBUG", "Network Adapter Error Response - " + "\n" + networkResponse, isDebugEnabled)

			String errorMessage = ""
			if (returnCode == "200") {
				utils.logAudit(networkResponse)
				execution.setVariable("DELNWKI_deleteNetworkResponse", networkResponse)
				utils.log("DEBUG", " Network Adapter Success Response - " + "\n" + networkResponse, isDebugEnabled)

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
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, validateNetworkResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void validateSDNCResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside validateSDNCResponse of DeleteNetworkV2 ***** ", isDebugEnabled)

		String response = execution.getVariable("DELNWKI_deleteSDNCResponse")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String deleteSDNCResponseDecodeXml = sdncAdapterUtils.decodeXML(execution.getVariable("DELNWKI_deleteSDNCResponse"))
		deleteSDNCResponseDecodeXml = deleteSDNCResponseDecodeXml.replace("&", "&amp;").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable("DELNWKI_deleteSDNCResponse", deleteSDNCResponseDecodeXml)

		if (execution.getVariable("DELNWKI_sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
			execution.setVariable("DELNWKI_isSdncRollbackNeeded", true)      //
			execution.setVariable("DELNWKI_isPONR", true)
			utils.log("DEBUG", "Successfully Validated SDNC Response", isDebugEnabled)
		} else {
			utils.log("DEBUG", "Did NOT Successfully Validated SDNC Response", isDebugEnabled)
		 	throw new BpmnError("MSOWorkflowException")
		}

	}


	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse of DeleteNetworkV2 ***** ", isDebugEnabled)

		try {
			// Display DB response: DELNWKI_deleteDBResponse / DELNWKI_dbReturnCode
			String dbReturnCode = execution.getVariable("DELNWKI_dbReturnCode")
			utils.log("DEBUG", " ***** DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
			utils.log("DEBUG", " ***** DB Update Response String: " + '\n' + execution.getVariable("DELNWKI_deleteDBResponse"), isDebugEnabled)

			if (dbReturnCode == '200') {

				String source = execution.getVariable("DELNWKI_source")
				String requestId = execution.getVariable("DELNWKI_requestId")

				String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
									xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
				    	    		xmlns:ns8="http://ecomp.att.com/mso/workflow/schema/v1">
							<request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
								<request-id>${requestId}</request-id>
								<action>DELETE</action>
								<source>${source}</source>
				   			</request-info>
							<ns8:status-message>Network has been deleted successfully.</ns8:status-message>
				   			<ns8:mso-bpel-name>BPMN Network action: DELETE</ns8:mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

					// Format Response
					String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

				utils.logAudit(xmlMsoCompletionRequest)
				execution.setVariable("DELNWKI_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
				execution.setVariable("DELNWKI_Success", true)
				if (execution.getVariable("DELNWKI_isSilentSuccess") == false) {
					utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
				} else {
					utils.log("DEBUG", " Silent SUCCESS going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
				}

			} else {
				// caught exception
				String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, DB Update Failed: " + dbReturnCode
				utils.log("DEBUG", exceptionMessage, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, exceptionMessage)

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, postProcessResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

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

		String dbReturnCode = execution.getVariable("DELNWKI_dbReturnCode")
		utils.log("DEBUG", " ***** DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
		utils.log("DEBUG", " ***** DB Update Response String: " + '\n' + execution.getVariable("DELNWKI_deleteDBResponse"), isDebugEnabled)

		String falloutHandlerRequest = ""
		String workflowException = ""
		String requestId = execution.getVariable("DELNWKI_requestId")
		String source = execution.getVariable("DELNWKI_source")
		execution.setVariable("DELNWKI_Success", false)
		try {
			WorkflowException wfe = execution.getVariable("WorkflowException")
			String errorCode = String.valueOf(wfe.getErrorCode())
			String errorMessage = wfe.getErrorMessage()

			falloutHandlerRequest =
			    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>DELETE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.logAudit(falloutHandlerRequest)
			execution.setVariable("DELNWKI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("ERROR", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstanceInfra, buildErrorResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>CREATE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${exceptionMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>9999</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
			execution.setVariable("DELNWKI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("DEBUG", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)
		}
	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			String requestId = execution.getVariable("att-mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String deleteNetworkRestError = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSyncResponse to APIH - " + "\n" + deleteNetworkRestError, isDebugEnabled)

			sendWorkflowResponse(execution, 500, deleteNetworkRestError)

		} catch (Exception ex) {
			utils.log("DEBUG", " Sending Sync Error Activity Failed - DeleteNetworkInstanceInfra, sendSyncError(): " + "\n" + ex.getMessage(), isDebugEnabled)
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
