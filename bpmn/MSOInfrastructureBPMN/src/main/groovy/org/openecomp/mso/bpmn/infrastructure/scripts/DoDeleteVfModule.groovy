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

package org.openecomp.mso.bpmn.infrastructure.scripts
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VfModule
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.w3c.dom.Node
import org.w3c.dom.NodeList


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
*
* Outputs:
* @param - WorkflowException
*
*/
public class DoDeleteVfModule extends AbstractServiceTaskProcessor{

	def Prefix="DoDVfMod_"

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("DoDVfMod_contrailNetworkPolicyFqdnList", null)
		execution.setVariable("DoDVfMod_oamManagementV4Address", null)
		execution.setVariable("DoDVfMod_oamManagementV6Address", null)

	}

	// parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
	// and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
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

				utils.logAudit("DoDeleteVfModule Request: " + xml)

				utils.log("DEBUG", "input request xml: " + xml, isDebugEnabled)

				vnfId = utils.getNodeText1(xml,"vnf-id")
				execution.setVariable("vnfId", vnfId)
				vfModuleId = utils.getNodeText1(xml,"vf-module-id")
				execution.setVariable("vfModuleId", vfModuleId)
				def srvInstId = execution.getVariable("mso-service-instance-id")
				execution.setVariable("srvInstId", srvInstId)
				String requestId = ""
				try {
					requestId = execution.getVariable("mso-request-id")
				} catch (Exception ex) {
					requestId = utils.getNodeText1(xml, "request-id")
				}
				execution.setVariable("requestId", requestId)
				String source = utils.getNodeText1(xml, "source")
				execution.setVariable("source", source)
				String serviceId = utils.getNodeText1(xml, "service-id")
				execution.setVariable("serviceId", serviceId)
				String tenantId = utils.getNodeText1(xml, "tenant-id")
				execution.setVariable("tenantId", tenantId)

				String serviceInstanceIdToSdnc = ""
				if (xml.contains("service-instance-id")) {
					serviceInstanceIdToSdnc = utils.getNodeText1(xml, "service-instance-id")
				} else {
					serviceInstanceIdToSdnc = vfModuleId
				}
				execution.setVariable(Prefix + "serviceInstanceIdToSdnc", serviceInstanceIdToSdnc)
				String vfModuleName = utils.getNodeText1(xml, "vf-module-name")
				execution.setVariable("vfModuleName", vfModuleName)
				String vfModuleModelName = utils.getNodeText1(xml, "vf-module-model-name")
				execution.setVariable("vfModuleModelName", vfModuleModelName)
				String cloudSiteId = utils.getNodeText1(xml, "aic-cloud-region")
				execution.setVariable("cloudSiteId", cloudSiteId)
			}

			// formulate the request for PrepareUpdateAAIVfModule
			String request = """<PrepareUpdateAAIVfModuleRequest>
									<vnf-id>${vnfId}</vnf-id>
									<vf-module-id>${vfModuleId}</vf-module-id>
									<orchestration-status>pending-delete</orchestration-status>
								</PrepareUpdateAAIVfModuleRequest>""" as String
			utils.log("DEBUG", "PrepareUpdateAAIVfModuleRequest :" + request, isDebugEnabled)
			utils.logAudit("UpdateAAIVfModule Request: " + request)
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
	public void prepSDNCAdapterRequest(Execution execution, String action) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		String uuid = execution.getVariable('testReqId') // for junits
		if(uuid==null){
			uuid = execution.getVariable("requestId") + "-" +  	System.currentTimeMillis()
		}
		
		def srvInstId = execution.getVariable("srvInstId")
		def callbackUrl = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
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
		String request = """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
						      <sdncadapter:RequestHeader>
						         <sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
						         <sdncadapter:SvcInstanceId>${vfModuleId}</sdncadapter:SvcInstanceId>
						         <sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						         <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						         <sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
						      </sdncadapter:RequestHeader>
						      <sdncadapterworkflow:SDNCRequestData>
						         <request-information>
						            <request-id>${requestId}</request-id>
						            <request-action>DisconnectVNFRequest</request-action>
									${requestSubActionString}
						            <source>${source}</source>
						            <notification-url/>
						            <order-number/>
						            <order-version/>
						         </request-information>
						         <service-information>
						            <service-id>${serviceId}</service-id>
									<service-type>${serviceId}</service-type>
						            <service-instance-id>${serviceInstanceIdToSdnc}</service-instance-id>
						            <subscriber-name>notsurewecare</subscriber-name>
						         </service-information>
						         <vnf-request-information>
						         	<vnf-id>${vfModuleId}</vnf-id>
									<vnf-type>${vfModuleModelName}</vnf-type>
                                    <vnf-name>${vfModuleName}</vnf-name>
									<generic-vnf-id>${vnfId}</generic-vnf-id>
                                    <generic-vnf-name></generic-vnf-name>
									<generic-vnf-type></generic-vnf-type>
									<aic-cloud-region>${cloudSiteId}</aic-cloud-region>
									<tenant>${tenantId}</tenant>
						         </vnf-request-information>
						      </sdncadapterworkflow:SDNCRequestData>
						   </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

		utils.log("DEBUG", "sdncAdapterWorkflowRequest: " + request, isDebugEnabled)
		utils.logAudit("DoDeleteVfModule - SDNCAdapterWorkflowRequest: " + request)
		execution.setVariable("sdncAdapterWorkflowRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing VnfAdapterDeleteV1 request
	public void prepVNFAdapterRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
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
		def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
		if ('true'.equals(useQualifiedHostName)) {
			notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String request = """
			<deleteVfModuleRequest>
			    <cloudSiteId>${aicCloudRegion}</cloudSiteId>
			    <tenantId>${tenantId}</tenantId>
			    <vnfId>${vnfId}</vnfId>
			    <vfModuleId>${vfModuleId}</vfModuleId>
			    <vfModuleStackId>${vfModuleStackId}</vfModuleStackId>
			    <skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${origRequestId}</requestId>
			        <serviceInstanceId>${srvInstId}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${messageId}</messageId>
			    <notificationUrl>${notificationUrl}</notificationUrl>
			</deleteVfModuleRequest>
			""" as String

		utils.log("DEBUG", "vnfAdapterRestV1Request: " + request, isDebugEnabled)
		utils.logAudit("deleteVfModuleRequest: " + request)
		execution.setVariable("vnfAdapterRestV1Request", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing UpdateAAIVfModuleRequest request
	public void prepUpdateAAIVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
								<heat-stack-id>DELETE</heat-stack-id>
								<orchestration-status>deleted</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "UpdateAAIVfModuleRequest :" + request, isDebugEnabled)
		utils.logAudit("UpdateAAIVfModuleRequest: " + request)
		execution.setVariable("UpdateAAIVfModuleRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing DeleteAAIVfModuleRequest request
	public void prepDeleteAAIVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		def vnfId = execution.getVariable("vnfId")
		def vfModuleId = execution.getVariable("vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "DeleteAAIVfModuleRequest :" + request, isDebugEnabled)
		utils.logAudit("DeleteAAIVfModuleRequest: " + request)
		execution.setVariable("DeleteAAIVfModuleRequest", request)
	}

	// generates a WorkflowException if
	//		-
	public void handleDoDeleteVfModuleFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("ERROR", "AAI error occurred deleting the Generic Vnf: "
			+ execution.getVariable("DoDVfMod_deleteGenericVnfResponse"), isDebugEnabled)
		String processKey = getProcessKey(execution)
		WorkflowException exception = new WorkflowException(processKey, 5000,
			execution.getVariable("DoDVfMod_deleteGenericVnfResponse"))
		execution.setVariable("WorkflowException", exception)
	}

	public void sdncValidateResponse(Execution execution, String response){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		if(execution.getVariable(Prefix + 'sdncResponseSuccess') == true){
			utils.log("DEBUG", "Successfully Validated SDNC Response", isDebugEnabled)
		}else{
			throw new BpmnError("MSOWorkflowException")
		}
	}

	public void postProcessVNFAdapterRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessVNFAdapterRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix",Prefix)
		try{
		logDebug(" *** STARTED postProcessVNFAdapterRequest Process*** ", isDebugLogEnabled)

		String vnfResponse = execution.getVariable("DoDVfMod_doDeleteVfModuleResponse")
		logDebug("VNF Adapter Response is: " + vnfResponse, isDebugLogEnabled)
		utils.logAudit("deleteVnfAResponse is: \n"  + vnfResponse)

		if(vnfResponse != null){

			if(vnfResponse.contains("deleteVfModuleResponse")){
				logDebug("Received a Good Response from VNF Adapter for DELETE_VF_MODULE Call.", isDebugLogEnabled)
				execution.setVariable("DoDVfMod_vnfVfModuleDeleteCompleted", true)

				// Parse vnfOutputs for contrail network polcy FQDNs
				if (vnfResponse.contains("vfModuleOutputs")) {
					def vfModuleOutputsXml = utils.getNodeXml(vnfResponse, "vfModuleOutputs")
					InputSource source = new InputSource(new StringReader(vfModuleOutputsXml))
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
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
								logDebug("Obtained contrailNetworkPolicyFqdn: " + contrailNetworkPolicyFqdn, isDebugLogEnabled)
								contrailNetworkPolicyFqdnList.add(contrailNetworkPolicyFqdn)
							}
							else if (key.equals("oam_management_v4_address")) {
								String oamManagementV4Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logDebug("Obtained oamManagementV4Address: " + oamManagementV4Address, isDebugLogEnabled)
								execution.setVariable(Prefix + "oamManagementV4Address", oamManagementV4Address)
							}
							else if (key.equals("oam_management_v6_address")) {
								String oamManagementV6Address = element.getElementsByTagNameNS("*", "value").item(0).getTextContent()
								logDebug("Obtained oamManagementV6Address: " + oamManagementV6Address, isDebugLogEnabled)
								execution.setVariable(Prefix + "oamManagementV6Address", oamManagementV6Address)
							}

						}
					}
					if (!contrailNetworkPolicyFqdnList.isEmpty()) {
						logDebug("Setting the fqdn list", isDebugLogEnabled)
						execution.setVariable("DoDVfMod_contrailNetworkPolicyFqdnList", contrailNetworkPolicyFqdnList)
					}
				}
			}else{
				logDebug("Received a BAD Response from VNF Adapter for DELETE_VF_MODULE Call.", isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "VNF Adapter Error")
			}
		}else{
			logDebug("Response from VNF Adapter is Null for DELETE_VF_MODULE Call.", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Empty response from VNF Adapter")
		}

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			logDebug("Internal Error Occured in PostProcess Method", isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Internal Error Occured in PostProcess Method")
		}
		logDebug(" *** COMPLETED postProcessVnfAdapterResponse Process*** ", isDebugLogEnabled)
	}

	public void deleteNetworkPoliciesFromAAI(Execution execution) {
		def method = getClass().getSimpleName() + '.deleteNetworkPoliciesFromAAI(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED deleteNetworkPoliciesFromAAI ======== ", isDebugLogEnabled)

		try {
			// get variables
			List fqdnList = execution.getVariable("DoDVfMod_contrailNetworkPolicyFqdnList")
			if (fqdnList == null) {
				logDebug("No network policies to delete", isDebugLogEnabled)
				return
			}
			int fqdnCount = fqdnList.size()

			execution.setVariable("DoDVfMod_networkPolicyFqdnCount", fqdnCount)
			logDebug("DoDVfMod_networkPolicyFqdnCount - " + fqdnCount, isDebugLogEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getNetworkPolicyUri(execution)

			if (fqdnCount > 0) {
				// AII loop call over contrail network policy fqdn list
				for (i in 0..fqdnCount-1) {

					int counting = i+1
					String fqdn = fqdnList[i]

					// Query AAI for this network policy FQDN

					String queryNetworkPolicyByFqdnAAIRequest = "${aai_endpoint}${aai_uri}?network-policy-fqdn=" + UriUtils.encode(fqdn, "UTF-8")
					utils.logAudit("AAI request endpoint: " + queryNetworkPolicyByFqdnAAIRequest)
					logDebug("AAI request endpoint: "  + queryNetworkPolicyByFqdnAAIRequest, isDebugLogEnabled)

					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyByFqdnAAIRequest)
					int returnCode = response.getStatusCode()
					execution.setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", returnCode)
					logDebug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode, isDebugLogEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (isOneOf(returnCode, 200, 201)) {
						logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
						// This network policy FQDN exists in AAI - need to delete it now
						utils.logAudit(aaiResponseAsString)
						execution.setVariable("DoDVfMod_queryNetworkPolicyByFqdnAAIResponse", aaiResponseAsString)
						logDebug("QueryAAINetworkPolicyByFQDN Success REST Response, , NetworkPolicy #" + counting + " : " + "\n" + aaiResponseAsString, isDebugLogEnabled)
						// Retrieve the network policy id for this FQDN
						def networkPolicyId = utils.getNodeText1(aaiResponseAsString, "network-policy-id")
						logDebug("Deleting network-policy with network-policy-id " + networkPolicyId, isDebugLogEnabled)

						// Retrieve the resource version for this network policy
						def resourceVersion = utils.getNodeText1(aaiResponseAsString, "resource-version")
						logDebug("Deleting network-policy with resource-version " + resourceVersion, isDebugLogEnabled)

						String delNetworkPolicyAAIRequest = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(networkPolicyId, "UTF-8") +
							"?resource-version=" + UriUtils.encode(resourceVersion, "UTF-8")
						utils.logAudit("AAI request endpoint: " + delNetworkPolicyAAIRequest)
						logDebug("AAI request endpoint: " + delNetworkPolicyAAIRequest, isDebugLogEnabled)

						logDebug("invoking DELETE call to AAI", isDebugLogEnabled)
						utils.logAudit("Sending DELETE call to AAI with Endpoint /n" + delNetworkPolicyAAIRequest)
						APIResponse responseDel = aaiUriUtil.executeAAIDeleteCall(execution, delNetworkPolicyAAIRequest)
						int returnCodeDel = responseDel.getStatusCode()
						execution.setVariable("DoDVfMod_aaiDeleteNetworkPolicyReturnCode", returnCodeDel)
						logDebug(" ***** AAI delete network policy Response Code, NetworkPolicy #" + counting + " : " + returnCodeDel, isDebugLogEnabled)

						if (isOneOf(returnCodeDel, 200, 201, 204)) {
							logDebug("The return code from deleting network policy is: "  + returnCodeDel, isDebugLogEnabled)
							// This network policy was deleted from AAI successfully
							logDebug(" DelAAINetworkPolicy Success REST Response, , NetworkPolicy #" + counting + " : ", isDebugLogEnabled)

						} else {
								// aai all errors
								String delErrorMessage = "Unable to delete network-policy to AAI deleteNetworkPoliciesFromAAI - " + returnCodeDel
							 logDebug(delErrorMessage, isDebugLogEnabled)
							 exceptionUtil.buildAndThrowWorkflowException(execution, 2500, delErrorMessage)
						}
					} else if (returnCode == 404) {
						// This network policy FQDN is not in AAI. No need to delete.
						logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
						logDebug("This network policy FQDN is not in AAI: " + fqdn, isDebugLogEnabled)
						utils.logAudit("Network policy FQDN is not in AAI")
					} else {
					   if (aaiResponseAsString.contains("RESTFault")) {
						   WorkflowException exceptionObject = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
						   execution.setVariable("WorkflowException", exceptionObject)
						   throw new BpmnError("MSOWorkflowException")

						   } else {
								// aai all errors
								String dataErrorMessage = "Unexpected Response from deleteNetworkPoliciesFromAAI - " + returnCode
								logDebug(dataErrorMessage, isDebugLogEnabled)
								exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

						  }
					}



				} // end loop


			} else {
				   logDebug("No contrail network policies to query/create", isDebugLogEnabled)

			}

		} catch (BpmnError e) {
			throw e

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DoDeletVfModule flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
			logDebug(exceptionMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}

	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIGenericVnf(Execution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIGenericVnf(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

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
						<vnf-id>${vnfId}</vnf-id>
						${ipv4OamAddressElement}
						${managementV6AddressElement}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable(Prefix + 'updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				utils.logAudit("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logDebug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest, isDebugLogEnabled)


			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}




}