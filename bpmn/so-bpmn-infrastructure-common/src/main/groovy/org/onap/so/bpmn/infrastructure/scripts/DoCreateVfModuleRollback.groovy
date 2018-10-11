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
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils



public class DoCreateVfModuleRollback extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateVfModuleRollback.class);
	
	def Prefix="DCVFMR_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
	}

	// parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
	// and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
	public void preProcessRequest(DelegateExecution execution) {
		

		initProcessVariables(execution)

		try {

			execution.setVariable("rolledBack", null)
			execution.setVariable("rollbackError", null)
			
			def rollbackData = execution.getVariable("rollbackData")
			msoLogger.debug("RollbackData:" + rollbackData)
			
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
				msoLogger.debug("got fqdn # " + i + ": " + fqdn)
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
									<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
									<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
									<orchestration-status>pending-delete</orchestration-status>
								</PrepareUpdateAAIVfModuleRequest>""" as String
			msoLogger.debug("PrepareUpdateAAIVfModuleRequest :" + request)
			execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
		} else {
			execution.setVariable("skipRollback", true)
		}
		
		if (execution.getVariable("disableRollback").equals("true" )) {
			execution.setVariable("skipRollback", true)
		}
		
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			def msg = "Exception in DoCreateVfModuleRollback preProcessRequest " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	// build a SDNC vnf-topology-operation request for the specified action
	// (note: the action passed is expected to be 'changedelete' or 'delete')
	public void prepSDNCAdapterRequest(DelegateExecution execution) {
		
		String srvInstId = execution.getVariable("DCVFMR_serviceInstanceId")

		String uuid = execution.getVariable('testReqId') // for junits
		if(uuid==null){
			uuid = execution.getVariable("DCVFMR_requestId") + "-" +  	System.currentTimeMillis()
		}
		
		def callbackUrl = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)

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
						            <request-action>${MsoUtils.xmlEscape(requestAction)}</request-action>
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
                                    <generic-vnf-name>${MsoUtils.xmlEscape(vnfName)}</generic-vnf-name>
									<generic-vnf-type>${MsoUtils.xmlEscape(vnfType)}</generic-vnf-type>
									<aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
									<tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
						         </vnf-request-information>
						      </sdncadapterworkflow:SDNCRequestData>
						   </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

		msoLogger.debug("sdncAdapterWorkflowRequest: " + request)
		execution.setVariable("sdncAdapterWorkflowRequest", request)
	}

	public void preProcessSDNCDeactivateRequest(DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessSDNCDeactivateRequest")
		
		def serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
	
		try{
			//Build SDNC Request
			
			String deactivateSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "deactivate")
	
			deactivateSDNCRequest = utils.formatXml(deactivateSDNCRequest)
			execution.setVariable("DCVFMR_deactivateSDNCRequest", deactivateSDNCRequest)
			msoLogger.debug("Outgoing DeactivateSDNCRequest is: \n" + deactivateSDNCRequest)
	
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing preProcessSDNCDeactivateRequest.", "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during preProcessSDNCDeactivateRequest Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED preProcessSDNCDeactivateRequest")
	}

	public void preProcessSDNCUnassignRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCUnassignRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		msoLogger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED preProcessSDNCUnassignRequest Process")
		try{
			String serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
	
			String unassignSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "unassign")
	
			execution.setVariable("DCVFMR_unassignSDNCRequest", unassignSDNCRequest)
			msoLogger.debug("Outgoing UnassignSDNCRequest is: \n" + unassignSDNCRequest)
	
		}catch(Exception e){
			msoLogger.debug("Exception Occured Processing preProcessSDNCUnassignRequest. Exception is:\n" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCUnassignRequest Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED  preProcessSDNCUnassignRequest Process")
	}

	public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action){
	
			String uuid = execution.getVariable('testReqId') // for junits
			if(uuid==null){
				uuid = execution.getVariable("DCVFMR_requestId") + "-" +  	System.currentTimeMillis()
			}
			def callbackURL = UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback",execution)
			def requestId = execution.getVariable("DCVFMR_requestId")
			def serviceId = execution.getVariable("DCVFMR_serviceId")
			def serviceInstanceId = execution.getVariable("DCVFMR_serviceInstanceId")
			def vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
			def source = execution.getVariable("DCVFMR_source")
			def vnfId = execution.getVariable("DCVFMR_vnfId")
				
			def sdncVersion = execution.getVariable("sdncVersion")
			
			String sdncRequest =
			"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vf-module-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>DeleteVfModuleInstance</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id/>
			<subscription-service-type/>			
			<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
			<global-customer-id/>
		</service-information>
		<vnf-information>
			<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
			<vnf-type/>			
		</vnf-information>
		<vf-module-information>
			<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
		</vf-module-information>
		<vf-module-request-input/>		
	</sdncadapterworkflow:SDNCRequestData>
	</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""
	
		msoLogger.debug("sdncRequest:  " + sdncRequest)
		return sdncRequest
	}
	
	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing VnfAdapterDeleteV1 request
	public void prepVNFAdapterRequest(DelegateExecution execution) {
		
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
		execution.setVariable("vnfAdapterRestV1Request", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing UpdateAAIVfModuleRequest request
	public void prepUpdateAAIVfModule(DelegateExecution execution) {
		
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
								<heat-stack-id>DELETE</heat-stack-id>
								<orchestration-status>deleted</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
		msoLogger.debug("UpdateAAIVfModuleRequest :" + request)
		execution.setVariable("UpdateAAIVfModuleRequest", request)
	}
	
	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing UpdateAAIVfModuleRequest request
	public void prepUpdateAAIVfModuleToAssigned(DelegateExecution execution) {
		
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<UpdateAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
								<heat-stack-id></heat-stack-id>
								<orchestration-status>Assigned</orchestration-status>
							</UpdateAAIVfModuleRequest>""" as String
		msoLogger.debug("UpdateAAIVfModuleRequest :" + request)
		execution.setVariable("UpdateAAIVfModuleRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing DeleteAAIVfModuleRequest request
	public void prepDeleteAAIVfModule(DelegateExecution execution) {
		
		String vnfId = execution.getVariable("DCVFMR_vnfId")
		String vfModuleId = execution.getVariable("DCVFMR_vfModuleId")
		// formulate the request for UpdateAAIVfModule
		String request = """<DeleteAAIVfModuleRequest>
								<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
								<vf-module-id>${MsoUtils.xmlEscape(vfModuleId)}</vf-module-id>
							</DeleteAAIVfModuleRequest>""" as String
		msoLogger.debug("DeleteAAIVfModuleRequest :" + request)
		execution.setVariable("DeleteAAIVfModuleRequest", request)
	}

	// generates a WorkflowException if
	//		-
	public void handleDoDeleteVfModuleFailure(DelegateExecution execution) {
		
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "AAI error occurred deleting the Generic Vnf"+ execution.getVariable("DoDVfMod_deleteGenericVnfResponse"), "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError);
		String processKey = getProcessKey(execution);
		exceptionUtil.buildWorkflowException(execution, 5000, "Failure in DoDeleteVfModule")

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

	public void deleteNetworkPoliciesFromAAI(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.deleteNetworkPoliciesFromAAI(' +
		'execution=' + execution.getId() +
		')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		msoLogger.trace('Entered ' + method)
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED deleteNetworkPoliciesFromAAI")

		try {
			// get variables
			List fqdnList = execution.getVariable(Prefix + "createdNetworkPolicyFqdnList")
			if (fqdnList == null) {
				msoLogger.debug("No network policies to delete")
				return
			}
			int fqdnCount = fqdnList.size()

			execution.setVariable(Prefix + "networkPolicyFqdnCount", fqdnCount)
			msoLogger.debug("networkPolicyFqdnCount - " + fqdnCount)

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

					msoLogger.debug("AAI request endpoint: "  + queryNetworkPolicyByFqdnAAIRequest)

					def aaiRequestId = UUID.randomUUID().toString()
					APIResponse response = aaiUriUtil.executeAAIGetCall(execution, queryNetworkPolicyByFqdnAAIRequest)
					int returnCode = response.getStatusCode()
					execution.setVariable(Prefix + "aaiQueryNetworkPolicyByFqdnReturnCode", returnCode)
					msoLogger.debug("AAI query network policy Response Code, NetworkPolicy #" + counting + " : " + returnCode)

					String aaiResponseAsString = response.getResponseBodyAsString()

					if (isOneOf(returnCode, 200, 201)) {
						msoLogger.debug("The return code is: "  + returnCode)
						// This network policy FQDN exists in AAI - need to delete it now
						execution.setVariable(Prefix + "queryNetworkPolicyByFqdnAAIResponse", aaiResponseAsString)
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

						def aaiRequestIdDel = UUID.randomUUID().toString()
						msoLogger.debug("Sending DELETE call to AAI with Endpoint /n" + delNetworkPolicyAAIRequest)

						APIResponse responseDel = aaiUriUtil.executeAAIDeleteCall(execution, delNetworkPolicyAAIRequest)

						int returnCodeDel = responseDel.getStatusCode()
						execution.setVariable(Prefix + "aaiDeleteNetworkPolicyReturnCode", returnCodeDel)
						msoLogger.debug("AAI delete network policy Response Code, NetworkPolicy #" + counting + " : " + returnCodeDel)

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
			String exceptionMessage = "Bpmn error encountered in DoCreateVfModuleRollback flow. deleteNetworkPoliciesFromAAI() - " + ex.getMessage()
			msoLogger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}


	/**
	 * Prepare a Request for invoking the UpdateAAIGenericVnf subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void preProcessUpdateAAIGenericVnf(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessUpdateAAIGenericVnf((' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		msoLogger.trace('Entered ' + method)

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
						<vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
						${ipv4OamAddressElement}
						${managementV6AddressElement}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable(Prefix + 'updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				msoLogger.debug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest)


			msoLogger.trace('Exited ' + method)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, 'Caught exception in ' + method, "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessUpdateAAIGenericVnf((): ' + e.getMessage())
		}
	}
	
	public void setSuccessfulRollbackStatus (DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED setSuccessfulRollbackStatus")
	
		try{
			// Set rolledBack to true, rollbackError to null
			execution.setVariable("rolledBack", true)
			execution.setVariable("rollbackError", null)
	
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing setSuccessfulRollbackStatus.", "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setSuccessfulRollbackStatus Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED setSuccessfulRollbackStatus")
	}
	
	public void setFailedRollbackStatus (DelegateExecution execution){
		
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED setFailedRollbackStatus")
	
		try{
			// Set rolledBack to false, rollbackError to actual value, rollbackData to null
			execution.setVariable("rolledBack", false)
			execution.setVariable("rollbackError", 'Caught exception in DoCreateVfModuleRollback')
			execution.setVariable("rollbackData", null)
	
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing setFailedRollbackStatus.", "BPMN", MsoLogger.getServiceName(),MsoLogger.ErrorCode.UnknownError, "Exception is:\n" + e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occurred during setFailedRollbackStatus Method:\n" + e.getMessage())
		}
		msoLogger.trace("COMPLETED setFailedRollbackStatus")
	}
}
