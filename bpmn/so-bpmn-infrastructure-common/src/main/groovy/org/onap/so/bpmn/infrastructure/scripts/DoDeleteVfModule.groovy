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

package org.onap.so.bpmn.infrastructure.scripts

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VfModule
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient
import org.onap.so.rest.RESTConfig
import org.springframework.web.util.UriUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource

/* Subflow for Delete VF Module. When no DoDeleteVfModuleRequest is specified on input,
 * functions as a building block subflow

* Inputs for building block interface:
* @param - requestId
* @param - isDebugLogEnabled
* @param - vnfId
* @param - vfModuleId
* @param - serviceInstanceId
* @param - vfModuleName O
* @param - vfModuleModelInfo
* @param - cloudConfiguration*
* @param - sdncVersion ("1610")
* @param - retainResources 
* @param - aLaCarte
*
* Outputs:
* @param - WorkflowException
*
*/
public class DoDeleteVfModule extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteVfModule.class);

	def Prefix="DoDVfMod_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("DoDVfMod_contrailNetworkPolicyFqdnList", null)
		execution.setVariable("DoDVfMod_oamManagementV4Address", null)
		execution.setVariable("DoDVfMod_oamManagementV6Address", null)

	}

	// parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
	// and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
	public void preProcessRequest(DelegateExecution execution) {

		initProcessVariables(execution)

		try {
			def xml = execution.getVariable("DoDeleteVfModuleRequest")
			String vnfId = ""
			String vfModuleId = ""

			if (xml == null || xml.isEmpty()) {
				// Building Block-type request

				// Set mso-request-id to request-id for VNF Adapter interface
				String requestId = execution.getVariable("requestId")
				execution.setVariable("mso-request-id", requestId)

				String cloudConfiguration = execution.getVariable("cloudConfiguration")
				String vfModuleModelInfo = execution.getVariable("vfModuleModelInfo")
				String tenantId = jsonUtil.getJsonValue(cloudConfiguration, "tenantId")
				execution.setVariable("tenantId", tenantId)
				String cloudSiteId = jsonUtil.getJsonValue(cloudConfiguration, "lcpCloudRegionId")
				execution.setVariable("cloudSiteId", cloudSiteId)
				// Source is HARDCODED
				String source = "VID"
				execution.setVariable("source", source)
				// SrvInstId is hardcoded to empty
				execution.setVariable("srvInstId", "")
				// ServiceId is hardcoded to empty
				execution.setVariable("serviceId", "")
				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				vnfId = execution.getVariable("vnfId")
				vfModuleId = execution.getVariable("vfModuleId")
				if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
					execution.setVariable(Prefix + "serviceInstanceIdToSdnc", vfModuleId)
				}
				else {
					execution.setVariable(Prefix + "serviceInstanceIdToSdnc", serviceInstanceId)
				}
				//vfModuleModelName
				def vfModuleModelName = jsonUtil.getJsonValue(vfModuleModelInfo, "modelName")
				execution.setVariable("vfModuleModelName", vfModuleModelName)
				// retainResources
				def retainResources = execution.getVariable("retainResources")			
				if (retainResources == null) {
					retainResources  = false
				}
				execution.setVariable("retainResources", retainResources)				
			}
			else {

				msoLogger.debug("DoDeleteVfModule Request: " + xml)

				msoLogger.debug("input request xml: " + xml)

				vnfId = utils.getNodeText(xml,"vnf-id")
				execution.setVariable("vnfId", vnfId)
				vfModuleId = utils.getNodeText(xml,"vf-module-id")
				execution.setVariable("vfModuleId", vfModuleId)
				def srvInstId = execution.getVariable("mso-service-instance-id")
				execution.setVariable("srvInstId", srvInstId)
				String requestId = ""
				try {
					requestId = execution.getVariable("mso-request-id")
				} catch (Exception ex) {
					requestId = utils.getNodeText(xml, "request-id")
				}
				execution.setVariable("requestId", requestId)
				String source = utils.getNodeText(xml, "source")
				execution.setVariable("source", source)
				String serviceId = utils.getNodeText(xml, "service-id")
				execution.setVariable("serviceId", serviceId)
				String tenantId = utils.getNodeText(xml, "tenant-id")
				execution.setVariable("tenantId", tenantId)

				String serviceInstanceIdToSdnc = ""
				if (xml.contains("service-instance-id")) {
					serviceInstanceIdToSdnc = utils.getNodeText(xml, "service-instance-id")
				} else {
					serviceInstanceIdToSdnc = vfModuleId
				}
				execution.setVariable(Prefix + "serviceInstanceIdToSdnc", serviceInstanceIdToSdnc)
				String vfModuleName = utils.getNodeText(xml, "vf-module-name")
				execution.setVariable("vfModuleName", vfModuleName)
				String vfModuleModelName = utils.getNodeText(xml, "vf-module-model-name")
				execution.setVariable("vfModuleModelName", vfModuleModelName)
				String cloudSiteId = utils.getNodeText(xml, "aic-cloud-region")
				execution.setVariable("cloudSiteId", cloudSiteId)
			}

			// formulate the request for PrepareUpdateAAIVfModule
			String request = """<PrepareUpdateAAIVfModuleRequest>
									<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
									<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
									<orchestration-status>pending-delete</orchestration-status>
								</PrepareUpdateAAIVfModuleRequest>""" as String
			msoLogger.debug("PrepareUpdateAAIVfModuleRequest :" + request)
			msoLogger.debug("UpdateAAIVfModule Request: " + request)
			execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
			execution.setVariable("vfModuleFromAAI", null)
		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2000, "Internal Error encountered in PreProcess method!")
		}
	}

	// build a SDNC vnf-topology-operation request for the specified action
	// (note: the action passed is expected to be 'changedelete' or 'delete')
	public void prepSDNCAdapterRequest(DelegateExecution execution, String action) {


		String uuid = execution.getVariable('testReqId') // for junits
		if(uuid==null){
			uuid = execution.getVariable("requestId") + "-" +  	System.currentTimeMillis()
		}
		
		def srvInstId = execution.getVariable("srvInstId")
		def callbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
		String requestId = execution.getVariable("requestId")
		String source = execution.getVariable("source")
		String serviceId = execution.getVariable("serviceId")
		String vnfId = execution.getVariable("vnfId")
		String tenantId = execution.getVariable("tenantId")
		String vfModuleId = execution.getVariable("vfModuleId")
		String serviceInstanceIdToSdnc = execution.getVariable(Prefix + "serviceInstanceIdToSdnc")
		String vfModuleName = execution.getVariable("vfModuleName")
		// Get vfModuleName from AAI response if it was not specified on the request
		if (vfModuleName == null || vfModuleName.isEmpty()) {
			if (execution.getVariable("vfModuleFromAAI") != null) {
				VfModule vfModuleFromAAI = execution.getVariable("vfModuleFromAAI")
				vfModuleName = vfModuleFromAAI.getElementText("vf-module-name")
			}
		}
		String vfModuleModelName = execution.getVariable("vfModuleModelName")
		String cloudSiteId = execution.getVariable("cloudSiteId")
		boolean retainResources = execution.getVariable("retainResources")
		String requestSubActionString = ""
		if (retainResources) {
			requestSubActionString = "<request-sub-action>RetainResource</request-sub-action>"			
		}
		String request = """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
						      <sdncadapter:RequestHeader>
						         <sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
						         <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(vfModuleId)}</sdncadapter:SvcInstanceId>
						         <sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
						         <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						         <sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackUrl)}</sdncadapter:CallbackUrl>
						      </sdncadapter:RequestHeader>
						      <sdncadapterworkflow:SDNCRequestData>
						         <request-information>
						            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						            <request-action>DisconnectVNFRequest</request-action>
									${requestSubActionString}
						            <source>${MsoUtils.xmlEscape(source)}</source>
						            <notification-url/>
						            <order-number/>
						            <order-version/>
						         </request-information>
						         <service-information>
						            <service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
									<service-type>${MsoUtils.xmlEscape(serviceId)}</service-type>
						            <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceIdToSdnc)}</service-instance-id>
						            <subscriber-name>notsurewecare</subscriber-name>
						         </service-information>
						         <vnf-request-information>
						         	<vnf-id>${MsoUtils.xmlEscape(vfModuleId)}</vnf-id>
									<vnf-type>${MsoUtils.xmlEscape(vfModuleModelName)}</vnf-type>
                                    <vnf-name>${MsoUtils.xmlEscape(vfModuleName)}</vnf-name>
									<generic-vnf-id>${MsoUtils.xmlEscape(vnfId)}</generic-vnf-id>
                                    <generic-vnf-name></generic-vnf-name>
									<generic-vnf-type></generic-vnf-type>
									<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
									<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
						         </vnf-request-information>
						      </sdncadapterworkflow:SDNCRequestData>
						   </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

		msoLogger.debug("sdncAdapterWorkflowRequest: " + request)
		msoLogger.debug("DoDeleteVfModule - SDNCAdapterWorkflowRequest: " + request)
		execution.setVariable("sdncAdapterWorkflowRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing VnfAdapterDeleteV1 request
	public void prepVNFAdapterRequest(DelegateExecution execution) {

		def requestId = UUID.randomUUID().toString()
		def origRequestId = execution.getVariable('requestId')
		def srvInstId = execution.getVariable("serviceInstanceId")
		def aicCloudRegion = execution.getVariable("cloudSiteId")
		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		def vfModuleStackId = execution.getVariable('DoDVfMod_heatStackId')
		def tenantId = execution.getVariable("tenantId")
		def messageId = execution.getVariable('requestId') + '-' +
			System.currentTimeMillis()
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String request = """
			<deleteVfModuleRequest>
			    <cloudSiteId>${MsoUtils.xmlEscape(aicCloudRegion)}</cloudSiteId>
			    <tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
			    <vnfId>${MsoUtils.xmlEscape(vnfId)}</vnfId>
			    <vfModuleId>${MsoUtils.xmlEscape(vfModuleId)}</vfModuleId>
			    <vfModuleStackId>${MsoUtils.xmlEscape(vfModuleStackId)}</vfModuleStackId>
			    <skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${MsoUtils.xmlEscape(origRequestId)}</requestId>
			        <serviceInstanceId>${MsoUtils.xmlEscape(srvInstId)}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
			</deleteVfModuleRequest>
			""" as String

		msoLogger.debug("vnfAdapterRestV1Request: " + request)
		msoLogger.debug("deleteVfModuleRequest: " + request)
		execution.setVariable("vnfAdapterRestV1Request", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing UpdateAAIVfModuleRequest request
	public void prepUpdateAAIVfModule(DelegateExecution execution) {

		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
								<heat-stack-id>DELETE</heat-stack-id>
								<orchestration-status>deleted</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
		msoLogger.debug("UpdateAAIVfModuleRequest :" + request)
		msoLogger.debug("UpdateAAIVfModuleRequest: " + request)
		execution.setVariable("UpdateAAIVfModuleRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing DeleteAAIVfModuleRequest request
	public void prepDeleteAAIVfModule(DelegateExecution execution) {


		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
		msoLogger.debug("DeleteAAIVfModuleRequest :" + request)
		msoLogger.debug("DeleteAAIVfModuleRequest: " + request)
		execution.setVariable("DeleteAAIVfModuleRequest", request)
	}

	// generates a WorkflowException if
	//		-
	public void handleDoDeleteVfModuleFailure(DelegateExecution execution) {
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "AAI error occurred deleting the Generic Vnf: " + execution.getVariable("DoDVfMod_deleteGenericVnfResponse"), "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception");
		String processKey = getProcessKey(execution);
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable("DoDVfMod_deleteGenericVnfResponse"))
		execution.setVariable("WorkflowException", exception)
	}

	public void sdncValidateResponse(DelegateExecution execution, String response){

		execution.setVariable("prefix",Prefix)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			msoLogger.debug("Successfully Validated SDNC Response")
		}else{
			throw new BpmnError("MSOWorkflowException")
		}
	}

	public void postProcessVNFAdapterRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.postProcessVNFAdapterRequest(' +
			'execution=' + execution.getId() +
			')'

		msoLogger.trace('Entered ' + method)
		execution.setVariable("prefix",Prefix)
		try{
		msoLogger.trace("STARTED postProcessVNFAdapterRequest Process")

		String vnfResponse = execution.getVariable("DoDVfMod_doDeleteVfModuleResponse")
		msoLogger.debug("VNF Adapter Response is: " + vnfResponse)
		msoLogger.debug("deleteVnfAResponse is: \n"  + vnfResponse)

		if(vnfResponse != null){

			if(vnfResponse.contains("deleteVfModuleResponse")){
				msoLogger.debug("Received a Good Response from VNF Adapter for DELETE_VF_MODULE Call.")
				execution.setVariable("DoDVfMod_vnfVfModuleDeleteCompleted", true)

				// Parse vnfOutputs for contrail network polcy FQDNs
				if (vnfResponse.contains("vfModuleOutputs")) {
					def vfModuleOutputsXml = utils.getNodeXml(vnfResponse, "vfModuleOutputs")
					InputSource source = new InputSource(new StringReader(vfModuleOutputsXml));
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true)
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
			Document outputsXml = docBuilder.parse(source)

					NodeList entries = outputsXml.getElementsByTagNameNS("*", "entry")
					List contrailNetworkPolicyFqdnList = []
					for (int i = 0; i< entries.getLength(); i++) {
						Node node = entries.item(i)
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node
							String key = element.getElementsByTagNameNS("*", "key").item(0).getTextContent()
							if (key.endsWith("contrail_network_policy_fqdn")) {
								String contrailNetworkPolicyFqdn = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								msoLogger.debug("Obtained contrailNetworkPolicyFqdn: " + contrailNetworkPolicyFqdn)
								contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn)
							}
							else if (key.equals("oam_management_v4_address")) {
								String oamManagementV4Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								msoLogger.debug("Obtained oamManagementV4Address: " + oamManagementV4Address)
								execution.setVariable(Prefix + "oamManagementV4Address", oamManagementV4Address)
							}
							else if (key.equals("oam_management_v6_address")) {
								String oamManagementV6Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								msoLogger.debug("Obtained oamManagementV6Address: " + oamManagementV6Address)
								execution.setVariable(Prefix + "oamManagementV6Address", oamManagementV6Address)
							}

						}
					}
					if (!contrailNetworkPolicyFqdnList.isEmpty()) {
						msoLogger.debug("Setting the fqdn list")
						execution.setVariable("DoDVfMod_contrailNetworkPolicyFqdnList", contrailNetworkPolicyFqdnList)
					}
				}
			}else{
				msoLogger.debug("Received a BAD Response from VNF Adapter for DELETE_VF_MODULE Call.")
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
			}
		}else{
			msoLogger.debug("Response from VNF Adapter is Null for DELETE_VF_MODULE Call.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Empty response from VNF Adapter")
		}

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			msoLogger.debug("Internal Error Occured in PostProcess Method")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Internal Error Occured in PostProcess Method")
		}
		msoLogger.trace("COMPLETED postProcessVnfAdapterResponse Process")
	}

	public void deleteNetworkPoliciesFromAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.deleteNetworkPoliciesFromAAI(' +
		'execution=' + execution.getId() +
		')'

		msoLogger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED deleteNetworkPoliciesFromAAI ")

		try {
			// get variables
			List fqdnList = execution.getVariable("DoDVfMod_contrailNetworkPolicyFqdnList")
			if (fqdnList == null) {
				msoLogger.debug("No network policies to delete")
				return
			}
			int fqdnCount = fqdnList.size()

			execution.setVariable("DoDVfMod_networkPolicyFqdnCount", fqdnCount)
			msoLogger.debug("DoDVfMod_networkPolicyFqdnCount - " + fqdnCount)

			AaiUtil aaiUriUtil = new AaiUtil(this)

			if (fqdnCount > 0) {
				// AII loop call over contrail network policy fqdn list
				for (i in 0..fqdnCount-1) {

					int counting = i+1
					String fqdn = fqdnList[i]

					// Query AAI for this network policy FQDN
					AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.NETWORK_POLICY)
					uri.queryParam("network-policy-fqdn", fqdn)
					String queryNetworkPolicyByFqdnAAIRequest = aaiUriUtil.createAaiUri(uri)
					msoLogger.debug("AAI request endpoint: " + queryNetworkPolicyByFqdnAAIRequest)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyByFqdnAAIRequest)
					int returnCode = response.getStatusCode()
					execution.setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", returnCode)
					msoLogger.debug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (isOneOf(returnCode, 200, 201)) {
						msoLogger.debug("The return code is: "  + returnCode)
						// This network policy FQDN exists in AAI - need to delete it now
						msoLogger.debug(aaiResponseAsString)
						execution.setVariable("DoDVfMod_queryNetworkPolicyByFqdnAAIResponse", aaiResponseAsString)
						msoLogger.debug("QueryAAINetworkPolicyByFQDN Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsString)
						// Retrieve the network policy id for this FQDN
						def networkPolicyId = utils.getNodeText(aaiResponseAsString, "network-policy-id")
						msoLogger.debug("Deleting network-policy with network-policy-id " + networkPolicyId)

						// Retrieve the resource version for this network policy
						def resourceVersion = utils.getNodeText(aaiResponseAsString, "resource-version")
						msoLogger.debug("Deleting network-policy with resource-version " + resourceVersion)

						AAIResourceUri delUri = AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_POLICY, networkPolicyId)
						delUri.resourceVersion(resourceVersion)
						String delNetworkPolicyAAIRequest = aaiUriUtil.createAaiUri(delUri)

						msoLogger.debug("AAI request endpoint: " + delNetworkPolicyAAIRequest)

						msoLogger.debug("invoking DELETE call to AAI")
						msoLogger.debug("Sending DELETE call to AAI with Endpoint /n" + delNetworkPolicyAAIRequest)
						APIResponse responseDel = aaiUriUtil.executeAAIDeleteCall(execution, delNetworkPolicyAAIRequest)
						int returnCodeDel = responseDel.getStatusCode()
						execution.setVariable("DoDVfMod_aaiDeleteNetworkPolicyReturnCode", returnCodeDel)
						msoLogger.debug(" ***** AAI delete network policy Response Code, NetworkPolicy #" + counting + " : " + returnCodeDel)

						if (isOneOf(returnCodeDel, 200, 201, 204)) {
							msoLogger.debug("The return code from deleting network policy is: "  + returnCodeDel)
							// This network policy was deleted from AAI successfully
							msoLogger.debug(" DelAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : ")

						} else {
								// aai all errors
								String delErrorMessage = "Unable to delete network-policy to AAI deleteNetworkPoliciesFromAAI - " + returnCodeDel
							 msoLogger.debug(delErrorMessage)
							 exceptionUtil.buildAndThrowWorkflowException(execution, 2500, delErrorMessage)
						}
					} else if (returnCode == 404) {
						// This network policy FQDN is not in AAI. No need to delete.
						msoLogger.debug("The return code is: "  + returnCode)
						msoLogger.debug("This network policy FQDN is not in AAI: " + fqdn)
						msoLogger.debug("Network policy FQDN is not in AAI")
					} else {
					   if (aaiResponseAsString.contains("RESTFault")) {
						   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
						   execution.setVariable("WorkflowException", exceptionObject)
						   throw new BpmnError("MSOWorkflowException")

						   } else {
								// aai all errors
								String dataErrorMessage = "Unexpected Response from deleteNetworkPoliciesFromAAI - " + returnCode
								msoLogger.debug(dataErrorMessage)
								exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						  }
					}



				} // end loop


			} else {
				   msoLogger.debug("No contrail network policies to query/create")

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoDeletVfModule flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}

	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
			'execution=' + execution.getId() +
			')'

		msoLogger.trace('Entered ' + method)

		try {
			def vnfId = execution.getVariable('vnfId')
			def oamManagementV4Address = execution.getVariable(Prefix + 'oamManagementV4Address')
			def oamManagementV6Address = execution.getVariable(Prefix + 'oamManagementV6Address')
			def ipv4OamAddressElement = ''
			def managementV6AddressElement = ''

			if (oamManagementV4Address != null) {
				ipv4OamAddressElement = '<ipv4-oam-address>' + 'DELETE' + '</ipv4-oam-address>'
			}

			if (oamManagementV6Address != null) {
				managementV6AddressElement = '<management-v6-address>' + 'DELETE' + '</management-v6-address>'
			}


			String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						${ipv4OamAddressElement}
						${managementV6AddressElement}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable(Prefix + 'updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				msoLogger.debug("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				msoLogger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}
	
	/**
	 * Using the vnfId and vfModuleId provided in the inputs,
	 * query AAI to get the corresponding VF Module info.
	 * A 200 response is expected with the VF Module info in the response body,
	 * Will determine VF Module's orchestration status if one exists
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModuleForStatus(DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.queryAAIVfModuleForStatus(' +
			'execution=' + execution.getId() +
			')'
		msoLogger.trace('Entered ' + method)
		
		execution.setVariable(Prefix + 'orchestrationStatus', '')

		try {
			def vnfId = execution.getVariable('vnfId')
			def vfModuleId = execution.getVariable('vfModuleId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId)
			String endPoint = aaiUriUtil.createAaiUri(uri)

			msoLogger.debug("AAI endPoint: " + endPoint)

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				msoLogger.debug('sending GET to AAI endpoint \'' + endPoint + '\'')
				APIResponse response = client.httpGet()
				msoLogger.debug("createVfModule - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					msoLogger.debug("Received generic VNF data: " + responseData)

				}

				msoLogger.debug("deleteVfModule - queryAAIVfModule Response: " + responseData)
				msoLogger.debug("deleteVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable(Prefix + 'queryAAIVfModuleForStatusResponseCode', response.getStatusCode())
				execution.setVariable(Prefix + 'queryAAIVfModuleForStatusResponse', responseData)
				msoLogger.debug('Response code:' + response.getStatusCode())
				msoLogger.debug('Response:' + System.lineSeparator() + responseData)
				// Retrieve VF Module info and its orchestration status; if not found, do nothing
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					msoLogger.debug('Parsing the VNF data to find orchestration status')
					if (responseData != null) {
						def vfModuleText = utils.getNodeXml(responseData, "vf-module")
						//def xmlVfModule= new XmlSlurper().parseText(vfModuleText)
						def orchestrationStatus = utils.getNodeText(vfModuleText, "orchestration-status")
						execution.setVariable(Prefix + "orchestrationStatus", orchestrationStatus)
						msoLogger.debug("Received orchestration status from A&AI: " + orchestrationStatus)
						
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace()
				msoLogger.debug('Exception occurred while executing AAI GET:' + ex.getMessage())
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModuleForStatus(): ' + e.getMessage())
		}
	}





}