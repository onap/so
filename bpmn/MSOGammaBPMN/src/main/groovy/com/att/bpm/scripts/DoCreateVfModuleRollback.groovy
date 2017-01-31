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

		def rollbackData = execution.getVariable("RollbackData")
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
		String requestId = rollbackData.get("VFMODULE", "attmsorequestid")
		execution.setVariable("DCVFMR_requestId", requestId)
		//String serviceInstanceId = rollbackData.get("VFMODULE", "attmsoserviceinstanceid")
		//execution.setVariable("DCVFMR_serviceInstanceId", serviceInstanceId)
		execution.setVariable("DCVFMR_rollbackPrepareUpdateVfModule", rollbackData.get("VFMODULE", "rollbackPrepareUpdateVfModule"))
		execution.setVariable("DCVFMR_rollbackUpdateAAIVfModule", rollbackData.get("VFMODULE", "rollbackUpdateAAIVfModule"))
		execution.setVariable("DCVFMR_rollbackVnfAdapterCreate", rollbackData.get("VFMODULE", "rollbackVnfAdapterCreate"))
		execution.setVariable("DCVFMR_rollbackSDNCRequestAssign", rollbackData.get("VFMODULE", "rollbackSDNCRequestAssign"))
		execution.setVariable("DCVFMR_rollbackSDNCRequestActivate", rollbackData.get("VFMODULE", "rollbackSDNCRequestActivate"))
		execution.setVariable("DCVFMR_rollbackCreateAAIVfModule", rollbackData.get("VFMODULE", "rollbackCreateAAIVfModule"))

		// formulate the request for PrepareUpdateAAIVfModule
		String request = """<PrepareUpdateAAIVfModuleRequest>
								<vnf-id>${vnfId}</vnf-id>
								<vf-module-id>${vfModuleId}</vf-module-id>
								<orchestration-status>pending-delete</orchestration-status>
							</PrepareUpdateAAIVfModuleRequest>""" as String
		utils.log("DEBUG", "PrepareUpdateAAIVfModuleRequest :" + request, isDebugEnabled)
		execution.setVariable("PrepareUpdateAAIVfModuleRequest", request)
	}

	// build a SDNC vnf-topology-operation request for the specified action
	// (note: the action passed is expected to be 'changedelete' or 'delete')
	public void prepSDNCAdapterRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		String srvInstId = execution.getVariable("DCVFMR_serviceInstanceId")
		
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
		    serviceInstandIdToSdnc = vfModuleId
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
		execution.setVariable("sdncAdapterWorkflowRequest", request)
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
		execution.setVariable("DeleteAAIVfModuleRequest", request)
	}

	// generates a WorkflowException if
	//		-
	public void handleDoDeleteVfModuleFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("ERROR", "AAI error occurred deleting the Generic Vnf: "
			+ execution.getVariable("DoDVfMod_deleteGenericVnfResponse"), isDebugEnabled)
		String processKey = getProcessKey(execution);
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
}
