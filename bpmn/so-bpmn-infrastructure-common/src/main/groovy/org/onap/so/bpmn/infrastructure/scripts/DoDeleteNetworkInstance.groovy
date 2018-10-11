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
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.graphinventory.entities.uri.Depth
import org.onap.so.constants.Defaults
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse;
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.springframework.web.util.UriUtils

import groovy.json.*

public class DoDeleteNetworkInstance extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteNetworkInstance.class);

	String Prefix= "DELNWKI_"
	String groovyClassName = "DoDeleteNetworkInstance"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	public InitializeProcessVariables(DelegateExecution execution){
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

	public void preProcessRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside preProcessRequest() of " + groovyClassName + " Request ")

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
						msoLogger.debug(" Incoming message formatted . . . : " + '\n' + prettyJson)
						deleteNetwork =  vidUtils.createXmlNetworkRequestInfra(execution, deleteNetwork)

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
				deleteNetwork = vidUtils.createXmlNetworkRequestInstance(execution)
			}

			deleteNetwork = utils.formatXml(deleteNetwork)
			msoLogger.debug(deleteNetwork)
			execution.setVariable(Prefix + "networkRequest", deleteNetwork)
			msoLogger.debug(Prefix + "networkRequest - " + '\n' + deleteNetwork)

			// validate 'backout-on-failure' to override 'mso.rollback'
			boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, deleteNetwork)
			execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
			msoLogger.debug(Prefix + "rollbackEnabled - " + rollbackEnabled)

			String networkInputs = utils.getNodeXml(deleteNetwork, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable(Prefix + "networkInputs", networkInputs)

			// prepare messageId
			String messageId = execution.getVariable("testMessageId")  // for testing
			if (messageId == null || messageId == "") {
					messageId = UUID.randomUUID()
					msoLogger.debug(Prefix + "messageId, random generated: " + messageId)
			} else {
					msoLogger.debug(Prefix + "messageId, pre-assigned: " + messageId)
			}
			execution.setVariable(Prefix + "messageId", messageId)

			String source = utils.getNodeText(deleteNetwork, "source")
			execution.setVariable(Prefix + "source", source)
			msoLogger.debug(Prefix + "source - " + source)

			String networkId = ""
			if (utils.nodeExists(networkInputs, "network-id")) {
				networkId = utils.getNodeText(networkInputs, "network-id")
				if (networkId == null || networkId == "" || networkId == 'null' ) {
					sendSyncError(execution)
					// missing value of network-id
					String dataErrorMessage = "network-request has missing 'network-id' element/value."
					msoLogger.debug(" Invalid Request - " + dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}
			}

			// lcpCloudRegion or tenantId not sent, will be extracted from query AA&I
			def lcpCloudRegion = null
			if (utils.nodeExists(networkInputs, "aic-cloud-region")) {
				lcpCloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
				if (lcpCloudRegion == 'null') {
					lcpCloudRegion = null
				}
			}
			execution.setVariable(Prefix + "lcpCloudRegion", lcpCloudRegion)
			msoLogger.debug("lcpCloudRegion : " + lcpCloudRegion)

			String tenantId = null
			if (utils.nodeExists(networkInputs, "tenant-id")) {
				tenantId = utils.getNodeText(networkInputs, "tenant-id")
				if (tenantId == 'null') {
					tenantId = null
				}

			}
			execution.setVariable(Prefix + "tenantId", tenantId)
			msoLogger.debug("tenantId : " + tenantId)

			String sdncVersion = execution.getVariable("sdncVersion")
			msoLogger.debug("sdncVersion? : " + sdncVersion)

			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)

			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey", execution))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

			} catch (IOException ex) {
				String dataErrorMessage = " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
				msoLogger.debug(dataErrorMessage )
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			 // caught exception
			String exceptionMessage = "Exception Encountered in DoDeleteNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	public void callRESTQueryAAI (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAI() of DoDoDeleteNetworkInstance ***** " )

		// get variables
		String networkInputs  = execution.getVariable(Prefix + "networkInputs")
		String networkId   = utils.getNodeText(networkInputs, "network-id")
		networkId = UriUtils.encode(networkId,"UTF-8")

		// Prepare AA&I url
		AaiUtil aaiUriUtil = new AaiUtil(this)
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId)
		uri.depth(Depth.ALL)

		String queryAAIRequest = aaiUriUtil.createAaiUri(uri)
		msoLogger.debug(queryAAIRequest)
		execution.setVariable(Prefix + "queryAAIRequest", queryAAIRequest)
		msoLogger.debug(Prefix + "AAIRequest - " + "\n" + queryAAIRequest)

		RESTConfig config = new RESTConfig(queryAAIRequest);

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		Boolean isVfRelationshipExist = false
		try {
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiReturnCode", returnCode)

			msoLogger.debug(" ***** AAI Response Code  : " + returnCode)

			String aaiResponseAsString = response.getResponseBodyAsString()
			execution.setVariable(Prefix + "queryAAIResponse", aaiResponseAsString)

			if (returnCode=='200' || returnCode=='204') {
				msoLogger.debug(aaiResponseAsString)
				execution.setVariable(Prefix + "isAAIGood", true)
				msoLogger.debug(" AAI Query Success REST Response - " + "\n" + aaiResponseAsString)
				// verify if vf or vnf relationship exist
				if (utils.nodeExists(aaiResponseAsString, "relationship")) {
					NetworkUtils networkUtils = new NetworkUtils()
			        isVfRelationshipExist = networkUtils.isVfRelationshipExist(aaiResponseAsString)
					execution.setVariable(Prefix + "isVfRelationshipExist", isVfRelationshipExist)
					if (isVfRelationshipExist == true) {
						String relationshipMessage = "AAI Query Success Response but 'vf-module' relationship exist, not allowed to delete: network Id: " + networkId
						exceptionUtil.buildWorkflowException(execution, 2500, relationshipMessage)

					} else {
					    // verify if lcpCloudRegion was sent as input, if not get value from AAI Response
					    if (execution.getVariable(Prefix + "lcpCloudRegion") == null ) {
							String lcpCloudRegion = networkUtils.getCloudRegion(aaiResponseAsString)
							execution.setVariable(Prefix + "lcpCloudRegion", lcpCloudRegion)
							msoLogger.debug(" Get AAI getCloudRegion()  : " + lcpCloudRegion)
						}
						if (execution.getVariable(Prefix + "tenantId") == null ) {
							String tenantId = networkUtils.getTenantId(aaiResponseAsString)
							execution.setVariable(Prefix + "tenantId", tenantId)
							msoLogger.debug(" Get AAI getTenantId()  : " + tenantId)
						}

					}
				}
				msoLogger.debug(Prefix + "isVfRelationshipExist - " + isVfRelationshipExist)

			} else {
				execution.setVariable(Prefix + "isAAIGood", false)
			    if (returnCode=='404' || aaiResponseAsString == "" || aaiResponseAsString == null) {
					// not found // empty aai response
					execution.setVariable(Prefix + "isSilentSuccess", true)
					msoLogger.debug(" AAI Query is Silent Success")

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", exceptionObject)

				   } else {
 			       	  // aai all errors
						 String dataErrorMessage = "Unexpected Error Response from callRESTQueryAAI() - " + returnCode
						 msoLogger.debug(dataErrorMessage)
						 exceptionUtil.buildWorkflowException(execution, 2500, dataErrorMessage)

			      }
				}
			}

			msoLogger.debug(" AAI Query call, isAAIGood? : " + execution.getVariable(Prefix + "isAAIGood"))

		} catch (Exception ex) {
		   // caught exception
		   String exceptionMessage = "Exception Encountered in DoDeleteNetworkInstance, callRESTQueryAAI() - " + ex.getMessage()
		   msoLogger.debug(exceptionMessage)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAICloudRegion (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAICloudRegion of DoDeleteNetworkInstance ***** " )

		try {
			String networkInputs  = execution.getVariable(Prefix + "networkInputs")
			// String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")
			String cloudRegion = execution.getVariable(Prefix + "lcpCloudRegion")
			// Prepare AA&I url
			AaiUtil aaiUtil = new AaiUtil(this)

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, Defaults.CLOUD_OWNER.toString(), cloudRegion)
			def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

			execution.setVariable(Prefix + "queryCloudRegionRequest", queryCloudRegionRequest)

			String cloudRegionPo = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)
			String cloudRegionSdnc = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "SDNC", cloudRegion)

			if ((cloudRegionPo != "ERROR") && (cloudRegionSdnc != "ERROR")) {
				execution.setVariable(Prefix + "cloudRegionPo", cloudRegionPo)
				execution.setVariable(Prefix + "cloudRegionSdnc", cloudRegionSdnc)

			} else {
				String dataErrorMessage = "QueryAAICloudRegion Unsuccessful. Return Code: " + execution.getVariable(Prefix + "queryCloudRegionReturnCode")
				msoLogger.debug(dataErrorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

			}

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, callRESTQueryAAICloudRegion(). Unexpected Response from AAI - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareNetworkRequest (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareNetworkRequest of DoDeleteNetworkInstance ")
		ExceptionUtil exceptionUtil = new ExceptionUtil()
		try {
			// get variables
			String networkRequest = execution.getVariable(Prefix + "networkRequest")
			String cloudSiteId = execution.getVariable(Prefix + "cloudRegionPo")
			String tenantId = execution.getVariable(Prefix + "tenantId")

			String queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")
			String networkType = utils.getNodeText(queryAAIResponse, "network-type")
			String networkId = utils.getNodeText(queryAAIResponse, "network-id")
			String backoutOnFailure = execution.getVariable(Prefix + "rollbackEnabled")

			String networkStackId = ""
			networkStackId = utils.getNodeText(queryAAIResponse, "heat-stack-id")
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
				modelCustomizationUuid = utils.getNodeText(networkModelInfo, "modelCustomizationUuid")
			} else {
			    modelCustomizationUuid = utils.getNodeText(networkRequest, "modelCustomizationId")
			}

			String deleteNetworkRequest = """
					  <deleteNetworkRequest>
					    <cloudSiteId>${MsoUtils.xmlEscape(cloudSiteId)}</cloudSiteId>
					    <tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
					    <networkId>${MsoUtils.xmlEscape(networkId)}</networkId>
						<networkStackId>${MsoUtils.xmlEscape(networkStackId)}</networkStackId>
					    <networkType>${MsoUtils.xmlEscape(networkType)}</networkType>
						<modelCustomizationUuid>${MsoUtils.xmlEscape(modelCustomizationUuid)}</modelCustomizationUuid>
						<skipAAI>true</skipAAI>
					    <msoRequest>
					       <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
					       <serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
					    </msoRequest>
						<messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
						<notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
					  </deleteNetworkRequest>
						""".trim()

			msoLogger.debug(Prefix + "deleteNetworkRequest - " + "\n" +  deleteNetworkRequest)
			// Format Response
			String buildDeleteNetworkRequestAsString = utils.formatXml(deleteNetworkRequest)
			msoLogger.debug(buildDeleteNetworkRequestAsString)
			msoLogger.debug(Prefix + "deleteNetworkRequestAsString - " + "\n" +  buildDeleteNetworkRequestAsString)

			String restURL = UrnPropertiesReader.getVariable("mso.adapters.network.rest.endpoint", execution)
			execution.setVariable("mso.adapters.network.rest.endpoint", restURL + "/" + networkId)
			msoLogger.debug("mso.adapters.network.rest.endpoint - " + "\n" +  restURL + "/" + networkId)

			execution.setVariable(Prefix + "deleteNetworkRequest", buildDeleteNetworkRequestAsString)
			msoLogger.debug(Prefix + "deleteNetworkRequest - " + "\n" +  buildDeleteNetworkRequestAsString)
		}
		catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareNetworkRequest(). Unexpected Response from AAI - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	/**
	 * This method is used instead of an HTTP Connector task because the
	 * connector does not allow DELETE with a body.
	 */
	public void sendRequestToVnfAdapter(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendRequestToVnfAdapter(' +
			'execution=' + execution.getId() +
			')'

		msoLogger.trace('Entered ' + method)

		try {

			String vnfAdapterUrl = UrnPropertiesReader.getVariable("mso.adapters.network.rest.endpoint",execution)
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
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, exceptionMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + ex);
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}


	public void prepareSDNCRequest (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareSDNCRequest of DoDeleteNetworkInstance ")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}

			String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

			// get/set 'msoRequestId' and 'mso-request-id'
			String requestId = execution.getVariable("msoRequestId")
			if (requestId != null) {
				execution.setVariable("mso-request-id", requestId)
			} else {
			    requestId = execution.getVariable("mso-request-id")
			}
			execution.setVariable(Prefix + "requestId", requestId)
			msoLogger.debug(Prefix + "requestId " + requestId)
			String queryAAIResponse = execution.getVariable(Prefix + "queryAAIResponse")

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyDeleteRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "delete", "DisconnectNetworkRequest", cloudRegionId, networkId, queryAAIResponse, null)
		    String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
			msoLogger.debug(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
			msoLogger.debug(Prefix + "deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRequest() - " + ex.getMessage()
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, exceptionMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + ex);
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCRequest (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareRpcSDNCRequest of DoDeleteNetworkInstance ")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}

			String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "unassign", "DeleteNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			msoLogger.debug(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "deleteSDNCRequest", sndcTopologyDeleteRequesAsString)
			msoLogger.debug(Prefix + "deleteSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRequest() - " + ex.getMessage()
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, exceptionMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + ex);
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	public void prepareRpcSDNCDeactivate(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCDeactivate() of DoDeleteNetworkInstance ")

		try {

			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "deactivate", "DeleteNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			execution.setVariable(Prefix + "deactivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
			msoLogger.debug(" Preparing request for RPC SDNC Topology deactivate - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCActivateRollback() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void validateNetworkResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside validateNetworkResponse of DoDeleteNetworkInstance ")

		try {
			String returnCode = execution.getVariable(Prefix + "networkReturnCode")
			String networkResponse = execution.getVariable(Prefix + "deleteNetworkResponse")

			msoLogger.debug(" Network Adapter responseCode: " + returnCode)
			msoLogger.debug("Network Adapter Response - " + "\n" + networkResponse)
			msoLogger.debug(networkResponse)

			String errorMessage = ""
			if (returnCode == "200") {
				msoLogger.debug(" Network Adapter Response is successful - " + "\n" + networkResponse)

				// prepare rollback data
				String rollbackData = utils.getNodeXml(networkResponse, "rollback", false).replace("tag0:","").replace(":tag0","")
				if ((rollbackData == null) || (rollbackData.isEmpty())) {
					msoLogger.debug(" Network Adapter 'rollback' data is not Sent: " + "\n" + networkResponse)
					execution.setVariable(Prefix + "rollbackNetworkRequest", "")
				} else {
				    String rollbackNetwork =
					  """<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.onap.so/network">
						  	${rollbackData}
						 </NetworkAdapter:rollbackNetwork>"""
				    String rollbackNetworkXml = utils.formatXml(rollbackNetwork)
				    execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkXml)
				    msoLogger.debug(" Network Adapter rollback data - " + "\n" + rollbackNetworkXml)
				}


			} else { // network error
			   if (returnCode.toInteger() > 399 && returnCode.toInteger() < 600) {   //4xx, 5xx
				   if (networkResponse.contains("deleteNetworkError")  ) {
					   networkResponse = networkResponse.replace('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>', '')
					   errorMessage  = utils.getNodeText(networkResponse, "message")
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
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void validateSDNCResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside validateSDNCResponse of DoDeleteNetworkInstance ")

		String response = execution.getVariable(Prefix + "deleteSDNCResponse")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String deleteSDNCResponseDecodeXml = execution.getVariable(Prefix + "deleteSDNCResponse")
		deleteSDNCResponseDecodeXml = deleteSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "deleteSDNCResponse", deleteSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncRollbackNeeded", true)      //
			execution.setVariable(Prefix + "isPONR", true)
			msoLogger.debug("Successfully Validated SDNC Response")
		} else {
			msoLogger.debug("Did NOT Successfully Validated SDNC Response")
		 	throw new BpmnError("MSOWorkflowException")
		}

	}

	public void validateRpcSDNCDeactivateResponse (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside validateRpcSDNCDeactivateResponse() of DoDeleteNetworkInstance ")

		String response = execution.getVariable(Prefix + "deactivateSDNCResponse")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String assignSDNCResponseDecodeXml = execution.getVariable(Prefix + "deactivateSDNCResponse")
		assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "deactivateSDNCResponse", assignSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncDeactivateRollbackNeeded", true)
			msoLogger.debug("Successfully Validated Rpc SDNC Activate Response")

		} else {
			msoLogger.debug("Did NOT Successfully Validated Rpc SDNC Deactivate Response")
			throw new BpmnError("MSOWorkflowException")
		}

	}

	public void prepareRpcSDNCDeactivateRollback(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCDeactivateRollback() of DoDeleteNetworkInstance ")

		try {

			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String deactivateSDNCResponse = execution.getVariable(Prefix + "deactivateSDNCResponse")
			String networkId = utils.getNodeText(deactivateSDNCResponse, "network-id")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "activate", "CreateNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
			msoLogger.debug(" Preparing request for RPC SDNC Topology 'activate-CreateNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCDeactivateRollback() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRollbackData(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRollbackData() of DoDeleteNetworkInstance ")

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
			msoLogger.debug("** rollbackData : " + rollbackData)

			execution.setVariable("WorkflowException", execution.getVariable("WorkflowException"))
			msoLogger.debug("** WorkflowException : " + execution.getVariable("WorkflowException"))

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void postProcessResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside postProcessResponse of DoDeleteNetworkInstance ")

		try {

			msoLogger.debug(" ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"))
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
				   msoLogger.debug(" ***** workflowException: " + execution.getVariable("workflowException"))
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
				msoLogger.debug(" ***** postProcessResponse(), BAD !!!")
				exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
				throw new BpmnError("MSOWorkflowException")

			}

		} catch(BpmnError b){
		    msoLogger.debug("Rethrowing MSOWorkflowException")
		    throw b

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, postProcessResponse() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
			throw new BpmnError("MSOWorkflowException")

        }

	}

	public void prepareSuccessRollbackData(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareSuccessRollbackData() of DoDeleteNetworkInstance ")

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

			msoLogger.debug("** rollbackData : " + rollbackData)
			execution.setVariable("WorkflowException", null)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCUnassignRollback(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCUnassignRollbac() of DoDeleteNetworkInstance ")

		try {

			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String deleteSDNCResponse = execution.getVariable(Prefix + "deleteSDNCResponse")
			String networkId = utils.getNodeText(deleteSDNCResponse, "network-id")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			// 1. prepare delete topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, deleteNetworkInput, serviceInstanceId, sdncCallback, "assign", "CreateNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			msoLogger.debug(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyDeleteRequesAsString)
			msoLogger.debug(Prefix + "rollbackSDNCRequest" + "\n" +  sndcTopologyDeleteRequesAsString)
			msoLogger.debug(" Preparing request for RPC SDNC Topology 'assign-CreateNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyDeleteRequesAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstance flow. prepareRpcSDNCUnassignRollback() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareSDNCRollback (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		msoLogger.trace("Inside prepareSDNCRollback of DoDeleteNetworkInstance ")

		try {

			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String deleteNetworkInput = execution.getVariable(Prefix + "networkRequest")

			String networkId = ""
			if (utils.nodeExists(deleteNetworkInput, "network-id")) {
				networkId = utils.getNodeText(deleteNetworkInput, "network-id")
			}
			if (networkId == 'null') {networkId = ""}

			String serviceInstanceId = utils.getNodeText(deleteNetworkInput, "service-instance-id")

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
			msoLogger.debug(sndcTopologyDeleteRequesAsString)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyDeleteRequesAsString)
			msoLogger.debug(Prefix + "rollbackSDNCRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)
			msoLogger.debug(" Preparing request for RPC SDNC Topology 'rollback-DisconnectNetworkRequest' rollback . . . - " + "\n" +  sndcTopologyDeleteRequesAsString)


		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance, prepareSDNCRollback() - " + ex.getMessage()
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, exceptionMessage, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + ex);
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void setExceptionFlag(DelegateExecution execution){

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside setExceptionFlag() of DoDeleteNetworkInstance ")

		try {

			execution.setVariable(Prefix + "isException", true)

			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			msoLogger.debug(Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"))

		} catch(Exception ex){
			  String exceptionMessage = "Bpmn error encountered in DoDeleteNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
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
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

		}catch(Exception e){
			msoLogger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		msoLogger.debug("Completed processJavaException Method of " + Prefix)
	}

}
