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

import javax.ws.rs.core.UriBuilder

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
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.graphinventory.entities.uri.Depth
import org.onap.so.constants.Defaults
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse;
import org.springframework.web.util.UriUtils
import org.onap.aai.domain.yang.VpnBinding
import org.onap.aai.domain.yang.RouteTarget

import com.fasterxml.jackson.jaxrs.util.EndpointAsBeanProperty

import javax.ws.rs.NotFoundException

import groovy.json.*
import groovy.xml.XmlUtil

/**
 * This groovy class supports the <class>DoCreateNetworkInstance.bpmn</class> process.
 *
 */
public class DoCreateNetworkInstance extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateNetworkInstance.class);

	String Prefix="CRENWKI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	def className = getClass().getSimpleName()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DoCreateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(DelegateExecution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "networkRequest", "")
		execution.setVariable(Prefix + "rollbackEnabled", null)
		execution.setVariable(Prefix + "networkInputs", "")
		//execution.setVariable(Prefix + "requestId", "")
		execution.setVariable(Prefix + "messageId", "")
		execution.setVariable(Prefix + "source", "")
		execution.setVariable("BasicAuthHeaderValuePO", "")
		execution.setVariable("BasicAuthHeaderValueSDNC", "")
		execution.setVariable(Prefix + "serviceInstanceId","")
		execution.setVariable("GENGS_type", "")
		execution.setVariable(Prefix + "rsrc_endpoint", null)
		execution.setVariable(Prefix + "networkOutputs", "")
		execution.setVariable(Prefix + "networkId","")
		execution.setVariable(Prefix + "networkName","")

		// AAI query Name
		execution.setVariable(Prefix + "queryNameAAIRequest","")
		execution.setVariable(Prefix + "queryNameAAIResponse", "")
		execution.setVariable(Prefix + "aaiNameReturnCode", "")
		execution.setVariable(Prefix + "isAAIqueryNameGood", false)

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

		execution.setVariable(Prefix + "createNetworkRequest", "")
		execution.setVariable(Prefix + "createNetworkResponse", "")
		execution.setVariable(Prefix + "rollbackNetworkRequest", "")
		//execution.setVariable(Prefix + "rollbackNetworkResponse", "")
		execution.setVariable(Prefix + "networkReturnCode", "")
		//execution.setVariable(Prefix + "rollbackNetworkReturnCode", "")
		execution.setVariable(Prefix + "isNetworkRollbackNeeded", false)

		execution.setVariable(Prefix + "assignSDNCRequest", "")
		execution.setVariable(Prefix + "assignSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackSDNCRequest", "")
		//execution.setVariable(Prefix + "rollbackSDNCResponse", "")
		execution.setVariable(Prefix + "sdncReturnCode", "")
		//execution.setVariable(Prefix + "rollbackSDNCReturnCode", "")
		execution.setVariable(Prefix + "isSdncRollbackNeeded", false)
		execution.setVariable(Prefix + "sdncResponseSuccess", false)

		execution.setVariable(Prefix + "activateSDNCRequest", "")
		execution.setVariable(Prefix + "activateSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackActivateSDNCRequest", "")
		//execution.setVariable(Prefix + "rollbackActivateSDNCResponse", "")
		execution.setVariable(Prefix + "sdncActivateReturnCode", "")
		//execution.setVariable(Prefix + "rollbackActivateSDNCReturnCode", "")
		execution.setVariable(Prefix + "isSdncActivateRollbackNeeded", false)
		execution.setVariable(Prefix + "sdncActivateResponseSuccess", false)

		execution.setVariable(Prefix + "orchestrationStatus", "")
		execution.setVariable(Prefix + "isVnfBindingPresent", false)
		execution.setVariable(Prefix + "Success", false)

		execution.setVariable(Prefix + "isException", false)

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoCreateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		msoLogger.trace("Inside preProcessRequest() of " + className + ".groovy")

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// GET Incoming request & validate 3 kinds of format.
			execution.setVariable("action", "CREATE")
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
			execution.setVariable(Prefix + "networkRequest", networkRequest)
			msoLogger.debug(Prefix + "networkRequest - " + '\n' + networkRequest)

			// validate 'backout-on-failure' to override 'mso.rollback'
			boolean rollbackEnabled = networkUtils.isRollbackEnabled(execution, networkRequest)
			execution.setVariable(Prefix + "rollbackEnabled", rollbackEnabled)
			msoLogger.debug(Prefix + "rollbackEnabled - " + rollbackEnabled)

			String networkInputs = utils.getNodeXml(networkRequest, "network-inputs", false).replace("tag0:","").replace(":tag0","")
			execution.setVariable(Prefix + "networkInputs", networkInputs)
			msoLogger.debug(Prefix + "networkInputs - " + '\n' + networkInputs)

			// prepare messageId
			String messageId = execution.getVariable("testMessageId")  // for testing
			if (messageId == null || messageId == "") {
				messageId = UUID.randomUUID()
				msoLogger.debug(Prefix + "messageId, random generated: " + messageId)
			} else {
				msoLogger.debug(Prefix + "messageId, pre-assigned: " + messageId)
			}
			execution.setVariable(Prefix + "messageId", messageId)

			String source = utils.getNodeText(networkRequest, "source")
			execution.setVariable(Prefix + "source", source)
			msoLogger.debug(Prefix + "source - " + source)

			// validate cloud region
			String lcpCloudRegionId = utils.getNodeText(networkRequest, "aic-cloud-region")
			if ((lcpCloudRegionId == null) || (lcpCloudRegionId == "") || (lcpCloudRegionId == "null")) {
				String dataErrorMessage = "Missing value/element: 'lcpCloudRegionId' or 'cloudConfiguration' or 'aic-cloud-region'."
				msoLogger.debug(" Invalid Request - " + dataErrorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}

			// validate service instance id
			String serviceInstanceId = utils.getNodeText(networkRequest, "service-instance-id")
			if ((serviceInstanceId == null) || (serviceInstanceId == "") || (serviceInstanceId == "null")) {
				String dataErrorMessage = "Missing value/element: 'serviceInstanceId'."
				msoLogger.debug(" Invalid Request - " + dataErrorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}

			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth",execution)

			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey",execution))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

			} catch (IOException ex) {
				String exceptionMessage = "Exception Encountered in DoCreateNetworkInstance, PreProcessRequest() - "
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
			String networkId = utils.getNodeText(networkRequest, "network-id")
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

		} catch (Exception ex) {
			sendSyncError(execution)
			// caught exception
			String exceptionMessage = "Exception Encountered in PreProcessRequest() of " + className + ".groovy ***** : " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	/**
	 * Gets the service instance uri from aai
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('CRENWKI_serviceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

			if(!resourceClient.exists(uri)){
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai")
			}else{
				Map<String, String> keys = uri.getURIKeys()
				execution.setVariable("serviceType", keys.get("service-type"))
				execution.setVariable("subscriberName", keys.get("global-customer-id"))
			}

		}catch(BpmnError e) {
			throw e;
		}catch (Exception ex){
			String msg = "Exception in getServiceInstance. " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}


	public void callRESTQueryAAINetworkName (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkName() of DoCreateNetworkInstance ***** " )

		// get variables
		String networkInputs  = execution.getVariable(Prefix + "networkInputs")
		String networkName   = utils.getNodeText(networkInputs, "network-name")
		networkName = UriUtils.encode(networkName,"UTF-8")

		// Prepare AA&I url with network-name
		String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
		AaiUtil aaiUriUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L3_NETWORK)
		uri.queryParam("network-name", networkName)
		String queryAAINameRequest = aaiUriUtil.createAaiUri(uri)

		execution.setVariable(Prefix + "queryNameAAIRequest", queryAAINameRequest)
		msoLogger.debug(Prefix + "queryNameAAIRequest - " + "\n" + queryAAINameRequest)

		try {
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryAAINameRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiNameReturnCode", returnCode)
			msoLogger.debug(" ***** AAI Query Name Response Code  : " + returnCode)

			String aaiResponseAsString = response.getResponseBodyAsString()
			msoLogger.debug(" ***** AAI Query Name Response : " +'\n'+ aaiResponseAsString)

			if (returnCode=='200') {
				execution.setVariable(Prefix + "queryNameAAIResponse", aaiResponseAsString)
				execution.setVariable(Prefix + "isAAIqueryNameGood", true)
				String orchestrationStatus = ""
				try {
					// response is NOT empty
					orchestrationStatus = utils.getNodeText(aaiResponseAsString, "orchestration-status")
					execution.setVariable(Prefix + "orchestrationStatus", orchestrationStatus.toUpperCase())
					msoLogger.debug(Prefix + "orchestrationStatus - " + orchestrationStatus.toUpperCase())
					execution.setVariable("orchestrationStatus", orchestrationStatus)

				} catch (Exception ex) {
				    // response is empty
					execution.setVariable(Prefix + "orchestrationStatus", orchestrationStatus)
					msoLogger.debug(Prefix + "orchestrationStatus - " + orchestrationStatus)
				}

			} else {
			    if (returnCode=='404') {
					msoLogger.debug(" QueryAAINetworkName return code = '404' (Not Found).  Proceed with the Create !!! ")

			    } else {
 			        // aai all errors
					String dataErrorMessage = "Unexpected Error Response from QueryAAINetworkName - " + returnCode
					msoLogger.debug(dataErrorMessage)
					exceptionUtil.buildWorkflowException(execution, 2500, dataErrorMessage)

		      }

			}

			msoLogger.debug(Prefix + "isAAIqueryNameGood? : " + execution.getVariable(Prefix + "isAAIqueryNameGood"))

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// try error
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow - callRESTQueryAAINetworkName() -  " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAICloudRegion (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAICloudRegion() of DoCreateNetworkInstance ***** " )

		try {
			String networkInputs  = execution.getVariable(Prefix + "networkInputs")
			String cloudRegion = utils.getNodeText(networkInputs, "aic-cloud-region")

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
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow - callRESTQueryAAICloudRegion() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkId(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkId() of DoCreateNetworkInstance ***** " )

		try {
			// get variables
			String networkId = ""
			String assignSDNCResponse = execution.getVariable(Prefix + "assignSDNCResponse")
			if (execution.getVariable("sdncVersion") != "1610") {
			   String networkResponseInformation = ""
			   try {
			      networkResponseInformation = utils.getNodeXml(assignSDNCResponse, "network-response-information", false).replace("tag0:","").replace(":tag0","")
				  networkId = utils.getNodeText(networkResponseInformation, "instance-id")
			   } catch (Exception ex) {
			      String dataErrorMessage = " SNDC Response network validation for 'instance-id' (network-id) failed: Empty <network-response-information>"
			      msoLogger.debug(dataErrorMessage)
				  exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			   }

			} else {
			   networkId = utils.getNodeText(assignSDNCResponse, "network-id")
			}
			if (networkId == null || networkId == "null") {
				String dataErrorMessage = "SNDC Response did not contains 'instance-id' or 'network-id' element, or the value is null."
				msoLogger.debug(dataErrorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			} else {
			   msoLogger.debug(" SNDC Response network validation for 'instance-id' (network-id)' is good: " + networkId)
			}


			execution.setVariable(Prefix + "networkId", networkId)
			String networkName   = utils.getNodeText(assignSDNCResponse, "network-name")
			execution.setVariable(Prefix + "networkName", networkName)

			networkId = UriUtils.encode(networkId,"UTF-8")

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId)
			uri.depth(Depth.ALL)
			String queryIdAAIRequest = aaiUriUtil.createAaiUri(uri)

			execution.setVariable(Prefix + "queryIdAAIRequest", queryIdAAIRequest)
			msoLogger.debug(Prefix + "queryIdAAIRequest - " + "\n" + queryIdAAIRequest)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryIdAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiIdReturnCode", returnCode)

			msoLogger.debug(" ***** AAI Response Code  : " + returnCode)

			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				execution.setVariable(Prefix + "queryIdAAIResponse", aaiResponseAsString)
				msoLogger.debug(" QueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString)

				String netId   = utils.getNodeText(aaiResponseAsString, "network-id")
				execution.setVariable(Prefix + "networkId", netId)
				String netName   = utils.getNodeText(aaiResponseAsString, "network-name")
				execution.setVariable(Prefix + "networkName", netName)

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
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkId() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTReQueryAAINetworkId(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTReQueryAAINetworkId() of DoCreateNetworkInstance ***** " )

		try {
			// get variables
			String networkId   = execution.getVariable(Prefix + "networkId")
			String netId = networkId
			networkId = UriUtils.encode(networkId,"UTF-8")

			// Prepare AA&I url
			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId)
			uri.depth(Depth.ALL)
			String requeryIdAAIRequest = aaiUriUtil.createAaiUri(uri)

			execution.setVariable(Prefix + "requeryIdAAIRequest", requeryIdAAIRequest)
			msoLogger.debug(Prefix + "requeryIdAAIRequest - " + "\n" + requeryIdAAIRequest)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, requeryIdAAIRequest)
			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiRequeryIdReturnCode", returnCode)
			msoLogger.debug(" ***** AAI ReQuery Response Code  : " + returnCode)

			String aaiResponseAsString = response.getResponseBodyAsString()

			if (returnCode=='200') {
				execution.setVariable(Prefix + "requeryIdAAIResponse", aaiResponseAsString)
				msoLogger.debug(" ReQueryAAINetworkId Success REST Response - " + "\n" + aaiResponseAsString)

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
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTReQueryAAINetworkId() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkVpnBinding(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkVpnBinding() of DoCreateNetworkInstance ***** " )

		try {

			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "queryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			msoLogger.debug(" relationship - " + relationship)

			// Check if Vnf Binding is present, then build a List of vnfBinding
			List vpnBindingUri = networkUtils.getVnfBindingObject(relationship)
			int vpnCount = vpnBindingUri.size()
			execution.setVariable(Prefix + "vpnCount", vpnCount)
			msoLogger.debug(Prefix + "vpnCount - " + vpnCount)

			if (vpnCount > 0) {
				execution.setVariable(Prefix + "vpnBindings", vpnBindingUri)
				msoLogger.debug(" vpnBindingUri List - " + vpnBindingUri)

				String routeTargets = ""
				// AII loop call using list vpnBindings
				for(i in 0..vpnBindingUri.size()-1) {
					int counting = i+1

					String vpnBindingId = vpnBindingUri[i].substring(vpnBindingUri[i].indexOf("/vpn-binding/")+13, vpnBindingUri[i].length())
					if (vpnBindingId.charAt(vpnBindingId.length()-1) == '/') {
						vpnBindingId = vpnBindingId.substring(0, vpnBindingId.length()-1)
					}

					AAIResourcesClient resourceClient = new AAIResourcesClient()
					AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VPN_BINDING, vpnBindingId)
					AAIResultWrapper wrapper = resourceClient.get(uri.depth(Depth.TWO), NotFoundException.class)

					Optional<VpnBinding> binding = wrapper.asBean(VpnBinding.class)

					String routeTarget = ""
					String routeRole = ""
					if(binding.get().getRouteTargets() != null) {
						List<RouteTarget> targets = binding.get().getRouteTargets().getRouteTarget()
						for(RouteTarget target : targets) {
							routeTarget  = target.getGlobalRouteTarget()
							routeRole  = target.getRouteTargetRole()
							routeTargets += "<routeTargets>" + '\n' +
									" <routeTarget>" + routeTarget + "</routeTarget>" + '\n' +
									" <routeTargetRole>" + routeRole + "</routeTargetRole>" + '\n' +
									"</routeTargets>" + '\n'
						}
					}

				} // end loop

				execution.setVariable(Prefix + "routeCollection", routeTargets)
				msoLogger.debug(Prefix + "routeCollection - " + '\n' + routeTargets)

			} else {
				// reset return code to success
				execution.setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")
				AaiUtil aaiUriUtil = new AaiUtil(this)
				String schemaVersion = aaiUriUtil.getNamespace()
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

		} catch (NotFoundException e) {
			msoLogger.debug("Response Error from AAINetworkVpnBinding is 404 (Not Found).")
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Response Error from AAINetworkVpnBinding is 404 (Not Found).")
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkVpnBinding() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkPolicy(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkPolicy() of DoCreateNetworkInstance ***** " )

		try {
			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "queryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			msoLogger.debug(" relationship - " + relationship)

			// Check if Network Policy is present, then build a List of network policy
			List networkPolicyUriList = networkUtils.getNetworkPolicyObject(relationship)
			int networkPolicyCount = networkPolicyUriList.size()
			execution.setVariable(Prefix + "networkPolicyCount", networkPolicyCount)
			msoLogger.debug(Prefix + "networkPolicyCount - " + networkPolicyCount)

			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (networkPolicyCount > 0) {
				execution.setVariable(Prefix + "networkPolicyUriList", networkPolicyUriList)
				msoLogger.debug(" networkPolicyUri List - " + networkPolicyUriList)

				String networkPolicies = ""
				// AII loop call using list vpnBindings
				for (i in 0..networkPolicyUriList.size()-1) {

					int counting = i+1

					// Note: By default, the network policy url is found in 'related-link' of the response,
					//       so, the default in URN mappings for this is set to "" (ie, space), unless forced to use the URN mapping.

					URI uri = UriBuilder.fromUri(networkPolicyUriList[i]).build()

					AAIResourceUri aaiUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.NETWORK_POLICY, uri)
					aaiUri.depth(Depth.ALL)
					String queryNetworkPolicyAAIRequest = aaiUriUtil.createAaiUri(aaiUri)

					execution.setVariable(Prefix + "queryNetworkPolicyAAIRequest", queryNetworkPolicyAAIRequest)
					msoLogger.debug(Prefix + "queryNetworkPolicyAAIRequest, , NetworkPolicy #" + counting + " : " + "\n" + queryNetworkPolicyAAIRequest)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", returnCode)
					msoLogger.debug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
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
				msoLogger.debug(Prefix + "networkCollection - " + '\n' + networkPolicies)

			} else {
				// reset return code to success
				execution.setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")
				String schemaVersion = aaiUriUtil.getNamespace()
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
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkPolicy() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTQueryAAINetworkTableRef(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTQueryAAINetworkTableRef() of DoCreateNetworkInstance ***** " )

		try {
			// get variables
			String queryIdAAIResponse   = execution.getVariable(Prefix + "queryIdAAIResponse").replace('<?xml version="1.0" encoding="UTF-8"?>', "")
			String relationship = networkUtils.getFirstNodeXml(queryIdAAIResponse, "relationship-list").trim().replace("tag0:","").replace(":tag0","")
			msoLogger.debug(" relationship - " + relationship)

			// Check if Network TableREf is present, then build a List of network policy
			List networkTableRefUriList = networkUtils.getNetworkTableRefObject(relationship)
			int networkTableRefCount = networkTableRefUriList.size()
			execution.setVariable(Prefix + "networkTableRefCount", networkTableRefCount)
			msoLogger.debug(Prefix + "networkTableRefCount - " + networkTableRefCount)

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
					URI uri = UriBuilder.fromUri(networkTableRefUriList[i]).build()

					AAIResourceUri aaiUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.ROUTE_TABLE_REFERENCE, uri)
					aaiUri.depth(Depth.ALL)
					String queryNetworkTableRefAAIRequest = aaiUriUtil.createAaiUri(aaiUri)

					execution.setVariable(Prefix + "queryNetworkTableRefAAIRequest", queryNetworkTableRefAAIRequest)
					msoLogger.debug(Prefix + "queryNetworkTableRefAAIRequest, , NetworkTableRef #" + counting + " : " + "\n" + queryNetworkTableRefAAIRequest)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkTableRefAAIRequest)
					String returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", returnCode)
					msoLogger.debug(" ***** AAI query network Table Reference Response Code, NetworkTableRef #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (returnCode=='200') {
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
				msoLogger.debug(Prefix + "tableRefCollection - " + '\n' + networkTableRefs)

			} else {
				// reset return code to success
				execution.setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")
				String schemaVersion = aaiUriUtil.getNamespace()
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
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTQueryAAINetworkTableRef() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	public void callRESTUpdateContrailAAINetwork(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.debug(" ***** Inside callRESTUpdateContrailAAINetwork() of DoCreateNetworkInstance ***** " )

		try {
			// get variables
			String networkId   = execution.getVariable(Prefix + "networkId")
			networkId = UriUtils.encode(networkId,"UTF-8")
			String requeryIdAAIResponse   = execution.getVariable(Prefix + "requeryIdAAIResponse")
			String createNetworkResponse   = execution.getVariable(Prefix + "createNetworkResponse")

			// Prepare url
			AaiUtil aaiUriUtil = new AaiUtil(this)

			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId)
			uri.depth(Depth.ALL)
			String updateContrailAAIUrlRequest = aaiUriUtil.createAaiUri(uri)

			execution.setVariable(Prefix + "updateContrailAAIUrlRequest", updateContrailAAIUrlRequest)
			msoLogger.debug(Prefix + "updateContrailAAIUrlRequest - " + "\n" + updateContrailAAIUrlRequest)

			//Prepare payload (PUT)
			String schemaVersion = aaiUriUtil.getNamespaceFromUri(updateContrailAAIUrlRequest)
			String payload = networkUtils.ContrailNetworkCreatedUpdate(requeryIdAAIResponse, createNetworkResponse, schemaVersion)
			String payloadXml = utils.formatXml(payload)
			execution.setVariable(Prefix + "updateContrailAAIPayloadRequest", payloadXml)
			msoLogger.debug(" 'payload' to Update Contrail - " + "\n" + payloadXml)

			APIResponse response = aaiUriUtil.executeAAIPutCall(execution, updateContrailAAIUrlRequest, payloadXml)

			String returnCode = response.getStatusCode()
			execution.setVariable(Prefix + "aaiUpdateContrailReturnCode", returnCode)
			msoLogger.debug(" ***** AAI Update Contrail Response Code  : " + returnCode)
			String aaiUpdateContrailResponseAsString = response.getResponseBodyAsString()
			if (returnCode=='200') {
				execution.setVariable(Prefix + "updateContrailAAIResponse", aaiUpdateContrailResponseAsString)
				msoLogger.debug(" AAI Update Contrail Success REST Response - " + "\n" + aaiUpdateContrailResponseAsString)
				// Point-of-no-return is set to false, rollback not needed.
				String rollbackEnabled = execution.getVariable(Prefix + "rollbackEnabled")
				if (rollbackEnabled == "true") {
				   execution.setVariable(Prefix + "isPONR", false)
				} else {
				   execution.setVariable(Prefix + "isPONR", true)
				}
				msoLogger.debug(Prefix + "isPONR" + ": " + execution.getVariable(Prefix + "isPONR"))
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
			String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. callRESTUpdateContrailAAINetwork() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareCreateNetworkRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareCreateNetworkRequest() of DoCreateNetworkInstance")

		try {

			// get variables
			String requestId = execution.getVariable("msoRequestId")
			if (requestId == null) {
				requestId = execution.getVariable("mso-request-id")
			}
			String messageId = execution.getVariable(Prefix + "messageId")
			String source    = execution.getVariable(Prefix + "source")

			String requestInput = execution.getVariable(Prefix + "networkRequest")
			String queryIdResponse = execution.getVariable(Prefix + "queryIdAAIResponse")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionPo")
			String backoutOnFailure = execution.getVariable(Prefix + "rollbackEnabled")

			// Prepare Network request
			String routeCollection = execution.getVariable(Prefix + "routeCollection")
			String policyCollection = execution.getVariable(Prefix + "networkCollection")
			String tableCollection = execution.getVariable(Prefix + "tableRefCollection")
			String createNetworkRequest = networkUtils.CreateNetworkRequestV2(execution, requestId, messageId, requestInput, queryIdResponse, routeCollection, policyCollection, tableCollection, cloudRegionId, backoutOnFailure, source )
			// Format Response
			String buildDeleteNetworkRequestAsString = utils.formatXml(createNetworkRequest)
			buildDeleteNetworkRequestAsString = buildDeleteNetworkRequestAsString.replace(":w1aac13n0", "").replace("w1aac13n0:", "")

			execution.setVariable(Prefix + "createNetworkRequest", buildDeleteNetworkRequestAsString)
			msoLogger.debug(Prefix + "createNetworkRequest - " + "\n" +  buildDeleteNetworkRequestAsString)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareCreateNetworkRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareSDNCRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareSDNCRequest() of DoCreateNetworkInstance")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

			String networkId = execution.getVariable(Prefix + "networkId")
			String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

			// get/set 'msoRequestId' and 'mso-request-id'
			String requestId = execution.getVariable("msoRequestId")
			if (requestId != null) {
				execution.setVariable("mso-request-id", requestId)
			} else {
			    requestId = execution.getVariable("mso-request-id")
			}
			execution.setVariable(Prefix + "requestId", requestId)

			// 1. prepare assign topology via SDNC Adapter SUBFLOW call
 		   	String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, createNetworkInput, serviceInstanceId, sdncCallback, "assign", "NetworkActivateRequest", cloudRegionId, networkId, null, null)

			String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
			execution.setVariable(Prefix + "assignSDNCRequest", sndcTopologyCreateRequesAsString)
			msoLogger.debug(Prefix + "assignSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareSDNCRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCRequest() of DoCreateNetworkInstance")

		try {
			// get variables

			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")

			String networkId = execution.getVariable(Prefix + "networkId")
			String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

			// 1. prepare assign topology via SDNC Adapter SUBFLOW call
			String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "assign", "CreateNetworkInstance", cloudRegionId, networkId, null)

			String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
			execution.setVariable(Prefix + "assignSDNCRequest", sndcTopologyCreateRequesAsString)
			msoLogger.debug(Prefix + "assignSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCActivateRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCActivateRequest() of DoCreateNetworkInstance")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String networkId = execution.getVariable(Prefix + "networkId")
			String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

			// 1. prepare assign topology via SDNC Adapter SUBFLOW call
			String sndcTopologyCreateRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "activate", "CreateNetworkInstance", cloudRegionId, networkId, null)

			String sndcTopologyCreateRequesAsString = utils.formatXml(sndcTopologyCreateRequest)
			execution.setVariable(Prefix + "activateSDNCRequest", sndcTopologyCreateRequesAsString)
			msoLogger.debug(Prefix + "activateSDNCRequest - " + "\n" +  sndcTopologyCreateRequesAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCActivateRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}




	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void validateCreateNetworkResponse (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside validateNetworkResponse() of DoCreateNetworkInstance")

		try {
			String returnCode = execution.getVariable(Prefix + "networkReturnCode")
			String networkResponse = execution.getVariable(Prefix + "createNetworkResponse")
			if (networkResponse==null)	{
				networkResponse="" // reset
			}

			msoLogger.debug(" Network Adapter create responseCode: " + returnCode)

			String errorMessage = ""
			if (returnCode == "200") {
				execution.setVariable(Prefix + "isNetworkRollbackNeeded", true)
				execution.setVariable(Prefix + "createNetworkResponse", networkResponse)
				msoLogger.debug(" Network Adapter create Success Response - " + "\n" + networkResponse)

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
				   if (networkResponse.contains("createNetworkError")) {
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
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. validateCreateNetworkResponse() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}

	public void validateSDNCResponse (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside validateSDNCResponse() of DoCreateNetworkInstance")

		String response = execution.getVariable(Prefix + "assignSDNCResponse")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String assignSDNCResponseDecodeXml = execution.getVariable(Prefix + "assignSDNCResponse")
		assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "assignSDNCResponse", assignSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncRollbackNeeded", true)
			msoLogger.debug("Successfully Validated SDNC Response")

		} else {
			msoLogger.debug("Did NOT Successfully Validated SDNC Response")
			throw new BpmnError("MSOWorkflowException")
		}

	}

	public void validateRpcSDNCActivateResponse (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside validateRpcSDNCActivateResponse() of DoCreateNetworkInstance")

		String response = execution.getVariable(Prefix + "activateSDNCResponse")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
		WorkflowException workflowException = execution.getVariable("WorkflowException")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
		// reset variable
		String assignSDNCResponseDecodeXml = execution.getVariable(Prefix + "activateSDNCResponse")
		assignSDNCResponseDecodeXml = assignSDNCResponseDecodeXml.replace('<?xml version="1.0" encoding="UTF-8"?>', "")
		execution.setVariable(Prefix + "activateSDNCResponse", assignSDNCResponseDecodeXml)

		if (execution.getVariable(Prefix + "sdncResponseSuccess") == true) {  // from sdnc util, Prefix+'sdncResponseSuccess'
			execution.setVariable(Prefix + "isSdncActivateRollbackNeeded", true)
			msoLogger.debug("Successfully Validated Rpc SDNC Activate Response")

		} else {
			msoLogger.debug("Did NOT Successfully Validated Rpc SDNC Activate Response")
			throw new BpmnError("MSOWorkflowException")
		}

	}


	public void prepareSDNCRollbackRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareSDNCRollbackRequest() of DoCreateNetworkInstance")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String assignSDNCResponse = execution.getVariable(Prefix + "assignSDNCResponse")
			String networkId = execution.getVariable(Prefix + "networkId")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRequest = sdncAdapterUtils.sdncTopologyRequestV2(execution, createNetworkInput, serviceInstanceId, sdncCallback, "rollback", "NetworkActivateRequest", cloudRegionId, networkId, null, null)
			String sndcTopologyRollbackRequestAsString = utils.formatXml(sndcTopologyRollbackRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRequestAsString)
			msoLogger.debug(" Preparing request for SDNC Topology 'rollback-NetworkActivateRequest' rollback . . . - " + "\n" +  sndcTopologyRollbackRequestAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareSDNCRollbackRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCRollbackRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCRollbackRequest() of DoCreateNetworkInstance")

		try {
			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String assignSDNCResponse = execution.getVariable(Prefix + "assignSDNCResponse")
			String networkId = execution.getVariable(Prefix + "networkId")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "unassign", "DeleteNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
			msoLogger.debug(" Preparing request for SDNC Topology 'unassign-DeleteNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCRollbackRequest() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRpcSDNCActivateRollback(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRpcSDNCActivateRollback() of DoCreateNetworkInstance")

		try {

			// get variables
			String sdncCallback = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			String createNetworkInput = execution.getVariable(Prefix + "networkRequest")
			String cloudRegionId = execution.getVariable(Prefix + "cloudRegionSdnc")
			String activateSDNCResponse = execution.getVariable(Prefix + "activateSDNCResponse")
			String networkId = execution.getVariable(Prefix + "networkId")
			if (networkId == 'null') {networkId = ""}
			String serviceInstanceId = execution.getVariable(Prefix + "serviceInstanceId")

			// 2. prepare rollback topology via SDNC Adapter SUBFLOW call
			String sndcTopologyRollbackRpcRequest = sdncAdapterUtils.sdncTopologyRequestRsrc(execution, createNetworkInput, serviceInstanceId, sdncCallback, "deactivate", "DeleteNetworkInstance", cloudRegionId, networkId, null)
			String sndcTopologyRollbackRpcRequestAsString = utils.formatXml(sndcTopologyRollbackRpcRequest)
			execution.setVariable(Prefix + "rollbackActivateSDNCRequest", sndcTopologyRollbackRpcRequestAsString)
			msoLogger.debug(" Preparing request for RPC SDNC Topology 'deactivate-DeleteNetworkInstance' rollback . . . - " + "\n" +  sndcTopologyRollbackRpcRequestAsString)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRpcSDNCActivateRollback() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareRollbackData(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareRollbackData() of DoCreateNetworkInstance")

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
			String rollbackActivateSDNCRequest = execution.getVariable(Prefix + "rollbackActivateSDNCRequest")
			if (rollbackActivateSDNCRequest != null) {
				if (rollbackActivateSDNCRequest != "") {
			        rollbackData.put("rollbackActivateSDNCRequest", execution.getVariable(Prefix + "rollbackActivateSDNCRequest"))
				}
			}
			execution.setVariable("rollbackData", rollbackData)
			msoLogger.debug("** rollbackData : " + rollbackData)

			execution.setVariable("WorkflowException", execution.getVariable(Prefix + "WorkflowException"))
			msoLogger.debug("** WorkflowException : " + execution.getVariable("WorkflowException"))

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareRollbackData() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void postProcessResponse(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside postProcessResponse() of DoCreateNetworkInstance")

		try {

			//Conditions:
			// 1. Silent Success: execution.getVariable("CRENWKI_orchestrationStatus") == "ACTIVE"
			// 2. Success: execution.getVariable("WorkflowException") == null (NULL)
			// 3. WorkflowException: execution.getVariable("WorkflowException") != null (NOT NULL)

			msoLogger.debug(" ***** Is Exception Encountered (isException)? : " + execution.getVariable(Prefix + "isException"))
			// successful flow
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
   			   // inside sub-flow logic
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
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
			throw new BpmnError("MSOWorkflowException")

		}



	}

	public void prepareSuccessRollbackData(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside prepareSuccessRollbackData() of DoCreateNetworkInstance")

		try {

			if (execution.getVariable("sdncVersion") != '1610') {
			    prepareRpcSDNCRollbackRequest(execution)
				prepareRpcSDNCActivateRollback(execution)
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
			String rollbackActivateSDNCRequest = execution.getVariable(Prefix + "rollbackActivateSDNCRequest")
			if (rollbackActivateSDNCRequest != null) {
				if (rollbackActivateSDNCRequest != "") {
					rollbackData.put("rollbackActivateSDNCRequest", rollbackActivateSDNCRequest)
				}
			}
			execution.setVariable("rollbackData", rollbackData)

			msoLogger.debug("** 'rollbackData' for Full Rollback : " + rollbackData)
			execution.setVariable("WorkflowException", null)


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstance flow. prepareSuccessRollbackData() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void setExceptionFlag(DelegateExecution execution){

		execution.setVariable("prefix",Prefix)

		msoLogger.trace("Inside setExceptionFlag() of DoCreateNetworkInstance")

		try {

			execution.setVariable(Prefix + "isException", true)

			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			msoLogger.debug(Prefix + "WorkflowException - " +execution.getVariable(Prefix + "WorkflowException"))

		} catch(Exception ex){
		  	String exceptionMessage = "Bpmn error encountered in DoCreateNetworkInstance flow. setExceptionFlag(): " + ex.getMessage()
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
			msoLogger.debug( "Caught a Java Exception in " + Prefix)
			msoLogger.debug("Started processJavaException Method")
			msoLogger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

		}catch(Exception e){
			msoLogger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method" + Prefix)
		}
		msoLogger.debug( "Completed processJavaException Method in " + Prefix)
	}

}
