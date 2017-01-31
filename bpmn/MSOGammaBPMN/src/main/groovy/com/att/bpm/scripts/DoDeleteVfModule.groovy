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

package com.att.bpm.scripts
import java.io.Serializable;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException



public class DoDeleteVfModule extends AbstractServiceTaskProcessor{

	def Prefix="DoDVfMod_"

	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
	}

	// parse the incoming DELETE_VF_MODULE request for the Generic Vnf and Vf Module Ids
	// and formulate the outgoing request for PrepareUpdateAAIVfModuleRequest
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("DoDeleteVfModuleRequest")
		utils.logAudit("DoDeleteVfModule Request: " + xml)
		
		utils.log("DEBUG", "input request xml: " + xml, isDebugEnabled)
		initProcessVariables(execution)
		def vnfId = utils.getNodeText1(xml,"vnf-id")
		def vfModuleId = utils.getNodeText1(xml,"vf-module-id")
//		execution.setVariable("DoDVfMod_vnfId", vnfId)
//		execution.setVariable("DoDVfMod_vfModuleId", vfModuleId)
		// formulate the request for PrepareUpdateAAIVfModule
		String request = """<PrepareUpdateAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
								<orchestration-status>pending-delete</orchestration-status>
							</PrepareUpdateAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "PrepareUpdateAAIVfModuleRequest :" + request, isDebugEnabled)
		utils.logAudit("UpdateAAIVfModule Request: " + request)
		execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
		execution.setVariable("DoDVfMod_vfModuleFromAAI", null)
	}

	// build a SDNC vnf-topology-operation request for the specified action
	// (note: the action passed is expected to be 'changedelete' or 'delete')
	public void prepSDNCAdapterRequest(Execution execution, String action) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("DoDeleteVfModuleRequest")
		utils.logAudit("DoDeleteVfModule Request: " + xml)
		
		def srvInstId = execution.getVariable("att-mso-service-instance-id")
		def callbackUrl = execution.getVariable("URN_mso_workflow_sdncadapter_callback")
		utils.log("DEBUG", "input request xml: " + xml, isDebugEnabled)

		String requestId = ""
		try {
			requestId = execution.getVariable("att-mso-request-id")
		} catch (Exception ex) {
			requestId = utils.getNodeText1(xml, "request-id")
		}

		String source = utils.getNodeText1(xml, "source")
		String serviceId = utils.getNodeText1(xml, "service-id")
		String vnfId = utils.getNodeText1(xml, "vnf-id")
		String vnfType = utils.getNodeText1(xml, "vnf-type")
		String vnfName = utils.getNodeText1(xml, "vnf-name")
		String tenantId = utils.getNodeText1(xml, "tenant-id")
		String vfModuleId = utils.getNodeText1(xml, "vf-module-id")
		String serviceInstanceIdToSdnc = ""
		if (xml.contains("service-instance-id")) {
			serviceInstanceIdToSdnc = utils.getNodeText1(xml, "service-instance-id")
		} else {
		    serviceInstanceIdToSdnc = vfModuleId
		}
		String vfModuleName = utils.getNodeText1(xml, "vf-module-name")
		// Get vfModuleName from AAI response if it was not specified on the request
		if (vfModuleName == null || vfModuleName.isEmpty()) {
			if (execution.getVariable("DoDVfMod_vfModuleFromAAI") != null) {
				VfModule vfModuleFromAAI = execution.getVariable("DoDVfMod_vfModuleFromAAI")
				vfModuleName = vfModuleFromAAI.getElementText("vf-module-name")			
			}		
		}
		String vfModuleModelName = utils.getNodeText1(xml, "vf-module-model-name")
		String cloudSiteId = utils.getNodeText1(xml, "aic-cloud-region")
		String request = """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://ecomp.att.com/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://domain2.att.com/workflow/sdnc/adapter/schema/v1">
						      <sdncadapter:RequestHeader>
						         <sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						         <sdncadapter:SvcInstanceId>${vfModuleId}</sdncadapter:SvcInstanceId>
						         <sdncadapter:SvcAction>${action}</sdncadapter:SvcAction>
						         <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						         <sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
						      </sdncadapter:RequestHeader>
						      <sdncadapterworkflow:SDNCRequestData>
						         <request-information>
						            <request-id>${requestId}</request-id>
						            <request-action>DisconnectVNFRequest</request-action>
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
		utils.logAudit("DoDeleteVfModule - SDNCAdapterWorkflowRequest: " + request)
		execution.setVariable("sdncAdapterWorkflowRequest", request)
	}

	// parse the incoming DELETE_VF_MODULE request
	// and formulate the outgoing VnfAdapterDeleteV1 request
	public void prepVNFAdapterRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("DoDeleteVfModuleRequest")
		def requestId = UUID.randomUUID().toString()
		def origRequestId = execution.getVariable('att-mso-request-id')
		def srvInstId = execution.getVariable('att-mso-service-instance-id')
		def aicCloudRegion = utils.getNodeText1(xml, "aic-cloud-region")
		def vnfId = utils.getNodeText1(xml, "vnf-id")
		def vnfName = utils.getNodeText1(xml, "vnf-name")
		def vfModuleId = utils.getNodeText1(xml, "vf-module-id")
		def vfModuleStackId = execution.getVariable('DoDVfMod_heatStackId')
		def tenantId = utils.getNodeText1(xml, "tenant-id")
		def messageId = execution.getVariable('att-mso-request-id') + '-' +
			System.currentTimeMillis()
		def notificationUrl = execution.getVariable("URN_mso_workflow_vnfadapter_rest_callback")
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
		def xml = execution.getVariable("DoDeleteVfModuleRequest")
		utils.log("DEBUG", "input request xml: " + xml, isDebugEnabled)
		initProcessVariables(execution)
		def vnfId = utils.getNodeText1(xml,"vnf-id")
		def vfModuleId = utils.getNodeText1(xml,"vf-module-id")
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
		def xml = execution.getVariable("DoDeleteVfModuleRequest")
		utils.log("DEBUG", "input request xml: " + xml, isDebugEnabled)
		initProcessVariables(execution)
		def vnfId = utils.getNodeText1(xml,"vnf-id")
		def vfModuleId = utils.getNodeText1(xml,"vf-module-id")
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
		String processKey = getProcessKey(execution);
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
}
