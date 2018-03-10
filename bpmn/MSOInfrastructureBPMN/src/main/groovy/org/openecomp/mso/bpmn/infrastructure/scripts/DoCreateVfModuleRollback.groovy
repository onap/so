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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils



public class DoCreateVfModuleRollback extends AbstractServiceTaskProcessor{

	def Prefix="DCVFMR_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
	}

	// parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
	// and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		initProcessVariables(execution)

		try {

			execution.setVariable("rolledBack", null)
			execution.setVariable("rollbackError", null)
			
			def rollbackData = execution.getVariable("rollbackData")
			utils.log("DEBUG", "RollbackData:" + rollbackData, isDebugEnabled)
			
			if (rollbackData != null) {
			String vnfId = rollbackData.get("VFMODULE", "vnfid")
			execution.setVariable("DCVFMR_vnfId", vnfId)
			String vfModuleId = rollbackData.get("VFMODULE", "vfmoduleid")
			execution.setVariable("DCVFMR_vfModuleId", vfModuleId)
			String source = rollbackData.get("VFMODULE", "source")
			execution.setVariable("DCVFMR_source", source)
			String serviceInstanceId = rollbackData.get("VFMODULE", "serviceInstanceId")
			execution.setVariable("DCVFMR_serviceInstanceId", serviceInstanceId)
			String serviceId = rollbackData.get("VFMODULE", "service-id")
			execution.setVariable("DCVFMR_serviceId", serviceId)
			String vnfType = rollbackData.get("VFMODULE", "vnftype")
			execution.setVariable("DCVFMR_vnfType", vnfType)
			String vnfName = rollbackData.get("VFMODULE", "vnfname")
			execution.setVariable("DCVFMR_vnfName", vnfName)
			String tenantId = rollbackData.get("VFMODULE", "tenantid")
			execution.setVariable("DCVFMR_tenantId", tenantId)
			String vfModuleName = rollbackData.get("VFMODULE", "vfmodulename")
			execution.setVariable("DCVFMR_vfModuleName", vfModuleName)
			String vfModuleModelName = rollbackData.get("VFMODULE", "vfmodulemodelname")
			execution.setVariable("DCVFMR_vfModuleModelName", vfModuleModelName)
			String cloudSiteId = rollbackData.get("VFMODULE", "aiccloudregion")
			execution.setVariable("DCVFMR_cloudSiteId", cloudSiteId)
			String heatStackId = rollbackData.get("VFMODULE", "heatstackid")
			execution.setVariable("DCVFMR_heatStackId", heatStackId)
			String requestId = rollbackData.get("VFMODULE", "msorequestid")
			execution.setVariable("DCVFMR_requestId", requestId)
			// Set mso-request-id to request-id for VNF Adapter interface
			execution.setVariable("mso-request-id", requestId)
			List createdNetworkPolicyFqdnList = []
			int i = 0
			while (i < 100) {
				String fqdn = rollbackData.get("VFMODULE", "contrailNetworkPolicyFqdn" + i)
				if (fqdn == null) {
					break
				}
				createdNetworkPolicyFqdnList.add(fqdn)
				logDebug("got fqdn # " + i + ": " + fqdn, isDebugEnabled)
				i = i + 1
	
			}
	
			execution.setVariable("DCVFMR_createdNetworkPolicyFqdnList", createdNetworkPolicyFqdnList)
			String oamManagementV4Address = rollbackData.get("VFMODULE", "oamManagementV4Address")
			execution.setVariable("DCVFMR_oamManagementV4Address", oamManagementV4Address)
			String oamManagementV6Address = rollbackData.get("VFMODULE", "oamManagementV6Address")
			execution.setVariable("DCVFMR_oamManagementV6Address", oamManagementV6Address)
			//String serviceInstanceId = rollbackData.get("VFMODULE", "msoserviceinstanceid")
			//execution.setVariable("DCVFMR_serviceInstanceId", serviceInstanceId)
			execution.setVariable("DCVFMR_rollbackPrepareUpdateVfModule", rollbackData.get("VFMODULE", "rollbackPrepareUpdateVfModule"))
			execution.setVariable("DCVFMR_rollbackUpdateAAIVfModule", rollbackData.get("VFMODULE", "rollbackUpdateAAIVfModule"))
			execution.setVariable("DCVFMR_rollbackVnfAdapterCreate", rollbackData.get("VFMODULE", "rollbackVnfAdapterCreate"))
			execution.setVariable("DCVFMR_rollbackSDNCRequestAssign", rollbackData.get("VFMODULE", "rollbackSDNCRequestAssign"))
			execution.setVariable("DCVFMR_rollbackSDNCRequestActivate", rollbackData.get("VFMODULE", "rollbackSDNCRequestActivate"))
			execution.setVariable("DCVFMR_rollbackCreateAAIVfModule", rollbackData.get("VFMODULE", "rollbackCreateAAIVfModule"))
			execution.setVariable("DCVFMR_rollbackCreateNetworkPoliciesAAI", rollbackData.get("VFMODULE", "rollbackCreateNetworkPoliciesAAI"))
			execution.setVariable("DCVFMR_rollbackUpdateVnfAAI", rollbackData.get("VFMODULE", "rollbackUpdateVnfAAI"))
	
			// formulate the request for PrepareUpdateAAIVfModule
			String request = """<PrepareUpdateAAIVfModuleRequest>
									<vnf-id>${vnfId}</vnf-id>
									<vf-module-id>${vfModuleId}</vf-module-id>
									<orchestration-status>pending-delete</orchestration-status>
								</PrepareUpdateAAIVfModuleRequest>""" as String
			utils.log("DEBUG", "PrepareUpdateAAIVfModuleRequest :" + request, isDebugEnabled)
			utils.logAudit("DoCreateVfModuleRollback PrepareUpdateAAIVfModule Request: " + request)
			execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
		} else {
			execution.setVariable("skipRollback", true)
		}
		
		if (execution.getVariable("disableRollback").equals("true" )) {
			execution.setVariable("skipRollback", true)
		}
		
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex){
			def msg = "Exception in DoCreateVfModuleRollback preProcessRequest " + ex.getMessage()
			utils.log("DEBUG", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	// build a SDNC vnf-topology-operation request for the specified action
	// (note: the action passed is expected to be 'changedelete' or 'delete')
	public void prepSDNCAdapterRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String srvInstId = execution.getVariable("DCVFMR_serviceInstanceId")

		String uuid = execution.getVariable('testReqId') // for junits
		if(uuid==null){
			uuid = execution.getVariable("DCVFMR_requestId") + "-" +  	System.currentTimeMillis()
		}
		
		def callbackUrl = execution.getVariable("URN_mso_workflow_sdncadapter_callback")

		String source = execution.getVariable("DCVFMR_source")
		String serviceId = execution.getVariable("DCVFMR_serviceId")
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vnfType = execution.getVariable("DCVFMR_vnfType")
		String vnfName = execution.getVariable("DCVFMR_vnfName")
		String tenantId = execution.getVariable("DCVFMR_tenantId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		String vfModuleName = execution.getVariable("DCVFMR_vfModuleName")
		String vfModuleModelName = execution.getVariable("DCVFMR_vfModuleModelName")
		String cloudSiteId = execution.getVariable("DCVFMR_cloudSiteId")
		String requestId = execution.getVariable("DCVFMR_requestId")

		String serviceInstanceIdToSdnc = ""
		if (srvInstId != null && !srvInstId.isEmpty()) {
			serviceInstanceIdToSdnc = srvInstId
		} else {
		    serviceInstanceIdToSdnc = vfModuleId
		}

		def doSDNCActivateRollback = execution.getVariable("DCVFMR_rollbackSDNCRequestActivate")
		def doSDNCAssignRollback = execution.getVariable("DCVFMR_rollbackSDNCRequestAssign")

		def action = ""
		def requestAction = ""

		if (doSDNCActivateRollback.equals("true")) {
			action = "delete"
			requestAction = "DisconnectVNFRequest"
		}
		else if (doSDNCAssignRollback.equals("true")) {
			action = "rollback"
			requestAction = "VNFActivateRequest"
		}
		else
			return


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
						            <request-action>${requestAction}</request-action>
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
                                    <generic-vnf-name>${vnfName}</generic-vnf-name>
									<generic-vnf-type>${vnfType}</generic-vnf-type>
									<aic-cloud-region>${cloudSiteId}</aic-cloud-region>
									<tenant>${tenantId}</tenant>
						         </vnf-request-information>
						      </sdncadapterworkflow:SDNCRequestData>
						   </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

		utils.log("DEBUG", "sdncAdapterWorkflowRequest: " + request, isDebugEnabled)
		utils.logAudit("DoCreateVfModuleRollback sdncAdapterWorkflow Request: " + request)
		execution.setVariable("sdncAdapterWorkflowRequest", request)
	}

	public void preProcessSDNCDeactivateRequest(Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
		
		def serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
	
		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")
	
			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable("DCVFMR_deactivateSDNCRequest", deactivateSDNCRequest)
			logDebug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)
	
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing preProcessSDNCDeactivateRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED preProcessSDNCDeactivateRequest ======== ", isDebugLogEnabled)
	}

	public void preProcessSDNCUnassignRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
		try{
			String serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
	
			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")
	
			execution.setVariable("DCVFMR_unassignSDNCRequest", unassignSDNCRequest)
			logDebug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest, isDebugLogEnabled)
			utils.logAudit("Outgoing UnassignSDNCRequest is: \n"  + unassignSDNCRequest)
	
		}catch(Exception e){
			log.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED  preProcessSDNCUnassignRequest Process ======== ", isDebugLogEnabled)
	}

	public String buildSDNCRequest(Execution execution, String svcInstId, String action){
	
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DCVFMR_requestId") + "-" +  	System.currentTimeMillis()
			}
			def callbackURL = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
			def requestId = execution.getVariable("DCVFMR_requestId")
			def serviceId = execution.getVariable("DCVFMR_serviceId")
			def serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
			def vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
			def source = execution.getVariable("DCVFMR_source")
			def vnfId = execution.getVariable("DCVFMR_vnfId")
				
			def sdncVersion = execution.getVariable("sdncVersion")
			
			String sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${uuid}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${requestId}</request-id>
			<request-action>DeleteVfModuleInstance</request-action>
			<source>${source}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id/>
			<subscription-service-type/>			
			<service-instance-id>${serviceInstanceId}</service-instance-id>
			<global-customer-id/>
		</service-information>
		<vnf-information>
			<vnf-id>${vnfId}</vnf-id>
			<vnf-type/>			
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${vfModuleId}</vf-module-id>
		</vf-module-information>
		<vf-module-request-input/>		
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
	
		utils.logAudit("sdncRequest:  " + sdncRequest)
		return sdncRequest
	}
	
	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing VnfAdapterDeleteV1 request
	public void prepVNFAdapterRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String requestId = UUID.randomUUID().toString()
		String origRequestId = execution.getVariable("DCVFMR_requestId")
		String srvInstId = execution.getVariable("DCVFMR_serviceInstanceId")
		String aicCloudRegion = execution.getVariable("DCVFMR_cloudSiteId")
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		String vfModuleStackId = execution.getVariable("DCVFMR_heatStackId")
		String tenantId = execution.getVariable("DCVFMR_tenantId")
		def messageId = execution.getVariable('mso-request-id') + '-' +
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
		utils.logAudit("PrepareUpdateAAIVfModule vnfAdapterRestV1 Request: " + request)
		execution.setVariable("vnfAdapterRestV1Request", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing UpdateAAIVfModuleRequest request
	public void prepUpdateAAIVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
								<heat-stack-id>DELETE</heat-stack-id>
								<orchestration-status>deleted</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "UpdateAAIVfModuleRequest :" + request, isDebugEnabled)
		utils.logAudit("UpdateAAIVfModule Request: " + request)
		execution.setVariable("UpdateAAIVfModuleRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing DeleteAAIVfModuleRequest request
	public void prepDeleteAAIVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "DeleteAAIVfModuleRequest :" + request, isDebugEnabled)
		utils.logAudit("DeleteAAIVfModule Request: " + request)
		execution.setVariable("DeleteAAIVfModuleRequest", request)
	}

	// generates a WorkflowException if
	//		-
	public void handleDoDeleteVfModuleFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("ERROR", "AAI error occurred deleting the Generic Vnf: "
			+ execution.getVariable("DoDVfMod_deleteGenericVnfResponse"), isDebugEnabled)
		String processKey = getProcessKey(execution)
		exceptionUtil.buildWorkflowException(execution, 5000, "Failure in DoDeleteVfModule")

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
			List fqdnList = execution.getVariable(Prefix + "createdNetworkPolicyFqdnList")
			if (fqdnList == null) {
				logDebug("No network policies to delete", isDebugLogEnabled)
				return
			}
			int fqdnCount = fqdnList.size()

			execution.setVariable(Prefix + "networkPolicyFqdnCount", fqdnCount)
			logDebug("networkPolicyFqdnCount - " + fqdnCount, isDebugLogEnabled)

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

					def aaiRequestId = UUID.randomUUID().toString()
					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyByFqdnAAIRequest)
					int returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQueryNetworkPolicyByFqdnReturnCode", returnCode)
					logDebug(" ***** AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode, isDebugLogEnabled)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (isOneOf(returnCode, 200, 201)) {
						logDebug("The return code is: "  + returnCode, isDebugLogEnabled)
						// This network policy FQDN exists in AAI - need to delete it now
						utils.logAudit(aaiResponseAsString)
						execution.setVariable(Prefix + "queryNetworkPolicyByFqdnAAIResponse", aaiResponseAsString)
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

						def aaiRequestIdDel = UUID.randomUUID().toString()
						logDebug("invoking DELETE call to AAI", isDebugLogEnabled)
						utils.logAudit("Sending DELETE call to AAI with Endpoint /n" + delNetworkPolicyAAIRequest)

						APIResponse responseDel = aaiUriUtil.executeAAIDeleteCall(execution, delNetworkPolicyAAIRequest)

						int returnCodeDel = responseDel.getStatusCode()
						execution.setVariable(Prefix + "aaiDeleteNetworkPolicyReturnCode", returnCodeDel)
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
			String exceptionMessage = "Bpmn error encountered in DoCreateVfModuleRollback flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
			logDebug(exceptionMessage, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}


	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessUpdateAAIGenericVnf(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIGenericVnf((' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DCVFMR_vnfId')
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessUpdateAAIGenericVnf((): ' + e.getMessage())
		}
	}
	
	public void setSuccessfulRollbackStatus (Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED setSuccessfulRollbackStatus ======== ", isDebugLogEnabled)
	
		try{
			// Set rolledBack to true, rollbackError to null
			execution.setVariable("rolledBack", true)
			execution.setVariable("rollbackError", null)
	
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing setSuccessfulRollbackStatus. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setSuccessfulRollbackStatus Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED setSuccessfulRollbackStatus ======== ", isDebugLogEnabled)
	}
	
	public void setFailedRollbackStatus (Execution execution){
		def isDebugLogEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		logDebug(" ======== STARTED setFailedRollbackStatus ======== ", isDebugLogEnabled)
	
		try{
			// Set rolledBack to false, rollbackError to actual value, rollbackData to null
			execution.setVariable("rolledBack", false)
			execution.setVariable("rollbackError", 'Caught exception in DoCreateVfModuleRollback')
			execution.setVariable("rollbackData", null)
	
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing setFailedRollbackStatus. Exception is:\n" + e, isDebugLogEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setFailedRollbackStatus Method:\n" + e.getMessage())
		}
		logDebug("======== COMPLETED setFailedRollbackStatus ======== ", isDebugLogEnabled)
	}
}
