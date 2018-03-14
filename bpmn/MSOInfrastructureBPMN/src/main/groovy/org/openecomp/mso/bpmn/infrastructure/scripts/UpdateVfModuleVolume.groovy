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

import java.util.concurrent.ExecutionException;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.springframework.web.util.UriUtils
import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.VfModuleBase
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil

class UpdateVfModuleVolume extends VfModuleBase {

	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * Initialize the flow's variables.
	 *
	 * @param execution The flow's execution instance.
	 */
	private void initProcessVariables(DelegateExecution execution) {
		execution.setVariable('prefix', 'UPDVfModVol_')
		execution.setVariable('UPDVfModVol_Request', null)
		execution.setVariable('UPDVfModVol_requestInfo', null)
		execution.setVariable('UPDVfModVol_requestId', null)
		execution.setVariable('UPDVfModVol_source', null)
		execution.setVariable('UPDVfModVol_volumeInputs', null)
		execution.setVariable('UPDVfModVol_volumeGroupId', null)
		execution.setVariable('UPDVfModVol_vnfType', null)
		execution.setVariable('UPDVfModVol_serviceId', null)
		execution.setVariable('UPDVfModVol_aicCloudRegion', null)
		execution.setVariable('UPDVfModVol_tenantId', null)
		execution.setVariable('UPDVfModVol_volumeParams', null)
		execution.setVariable('UPDVfModVol_volumeGroupHeatStackId', null)
		execution.setVariable('UPDVfModVol_volumeGroupTenantId', null)
		execution.setVariable('UpdateVfModuleVolumeSuccessIndicator', false)
	}

	/**
	 * Check for missing elements in the received request.
	 *
	 * @param execution The flow's execution instance.
	 */
	@Override
	public void preProcessRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			initProcessVariables(execution)
			String request = validateRequest(execution)

			def requestInfo = getRequiredNodeXml(execution, request, 'request-info')
			execution.setVariable('UPDVfModVol_requestInfo', requestInfo)
			execution.setVariable('UPDVfModVol_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
			execution.setVariable('UPDVfModVol_source', getNodeTextForce(requestInfo, 'source'))

			def volumeInputs = getRequiredNodeXml(execution, request, 'volume-inputs')
			execution.setVariable('UPDVfModVol_volumeInputs', volumeInputs)
			execution.setVariable('UPDVfModVol_volumeGroupId', getRequiredNodeText(execution, volumeInputs, 'volume-group-id'))
			execution.setVariable('UPDVfModVol_vnfType', getRequiredNodeText(execution, volumeInputs, 'vnf-type'))
			execution.setVariable('UPDVfModVol_serviceId', getRequiredNodeText(execution, volumeInputs, 'service-id'))
			execution.setVariable('UPDVfModVol_aicCloudRegion', getRequiredNodeText(execution, volumeInputs, 'aic-cloud-region'))
			execution.setVariable('UPDVfModVol_tenantId', getRequiredNodeText(execution, volumeInputs, 'tenant-id'))

			def volumeParams = utils.getNodeXml(request, 'volume-params')
			execution.setVariable('UPDVfModVol_volumeParams', volumeParams)

			logDebug('Exited ' + method, isDebugLogEnabled)
			utils.logAudit("UpdateVfModuleVolume request: " + request)
		} catch (BpmnError bpmnError) {
			throw bpmnError
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare and send the synchronous response.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void sendSynchResponse(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sendSynchResponse(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('UPDVfModVol_requestInfo')
			def requestId = execution.getVariable('UPDVfModVol_requestId')
			def source = execution.getVariable('UPDVfModVol_source')
			def progress = getNodeTextForce(requestInfo, 'progress')
			if (progress.isEmpty()) {
				progress = '0'
			}
			def startTime = getNodeTextForce(requestInfo, 'start-time')
			if (startTime.isEmpty()) {
				startTime = System.currentTimeMillis()
			}
			def volumeInputs = execution.getVariable('UPDVfModVol_volumeInputs')

			String synchResponse = """
				<volume-request xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-info>
						<request-id>${requestId}</request-id>
						<action>UPDATE_VF_MODULE_VOL</action>
						<request-status>IN_PROGRESS</request-status>
						<progress>${progress}</progress>
						<start-time>${startTime}</start-time>
						<source>${source}</source>
					</request-info>
					${volumeInputs}
				</volume-request>
			"""

			synchResponse = utils.formatXml(synchResponse)
			sendWorkflowResponse(execution, 200, synchResponse)
			utils.logAudit("UpdateVfModuleVolume Synch Response: " + synchResponse)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in sendSynchResponse(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for querying AAI for Volume Group information using the
	 * Volume Group Id and Aic Cloud Region.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIForVolumeGroup(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.queryAAIForVolumeGroup(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
			def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
			def endPoint = execution.getVariable('URN_aai_endpoint') +
				'/aai/v7/cloud-infrastructure/cloud-regions/cloud-region/att-aic/' + UriUtils.encode(aicCloudRegion, "UTF-8") +
				'/volume-groups/volume-group/' + UriUtils.encode(volumeGroupId, "UTF-8")

			logDebug('Sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
			utils.logAudit("UpdateVfModuleVolume sending GET for quering AAI endpoint: " + endPoint)

			AaiUtil aaiUtil = new AaiUtil(this)
			APIResponse response = aaiUtil.executeAAIGetCall(execution, endPoint)
			def int statusCode = response.getStatusCode()
			def responseData = response.getResponseBodyAsString()
			logDebug('Response code:' + statusCode, isDebugLogEnabled)
			logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
			utils.logAudit("UpdateVfModuleVolume response data: " + responseData)

			def volumeGroup = responseData
			def heatStackId = getNodeTextForce(volumeGroup, 'heat-stack-id')
			execution.setVariable('UPDVfModVol_volumeGroupHeatStackId', heatStackId)
			if ((statusCode == 200) || (statusCode == 204)) {
				def volumeGroupTenantId = getTenantIdFromVolumeGroup(volumeGroup)
				if (volumeGroupTenantId == null) {
					throw new Exception('Could not find Tenant Id element in Volume Group with Volume Group Id \'' + volumeGroupId + '\''
						+ '\', AIC Cloud Region \'' + aicCloudRegion + '\'')
				}
				execution.setVariable('UPDVfModVol_volumeGroupTenantId', volumeGroupTenantId)
				logDebug('Received Tenant Id \'' + volumeGroupTenantId + '\' from AAI for Volume Group with Volume Group Id \'' + volumeGroupId + '\''
					+ '\', AIC Cloud Region \'' + aicCloudRegion + '\'', isDebugLogEnabled)
			} else if (statusCode == 404) {
				throw new Exception('Volume Group \'' + volumeGroupId + '\' not found at AAI')
			} else {
				throw new Exception('Bad status code ' + statusCode + ' received from AAI; Response data: ' + responseData)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIForVolumeGroup(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the VnfAdapterRest subflow to do
	 * a Volume Group update.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepVnfAdapterRest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepVnfAdapterRest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
			def tenantId = execution.getVariable('UPDVfModVol_tenantId')
			def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
			def volumeGroupHeatStackId = execution.getVariable('UPDVfModVol_volumeGroupHeatStackId')
			def vnfType = execution.getVariable('UPDVfModVol_vnfType')

			def volumeParamsXml = execution.getVariable('UPDVfModVol_volumeParams')
			def volumeGroupParams = transformParamsToEntries(volumeParamsXml)

			def requestId = execution.getVariable('UPDVfModVol_requestId')
			def serviceId = execution.getVariable('UPDVfModVol_serviceId')

			def messageId = execution.getVariable('mso-request-id') + '-' + System.currentTimeMillis()
			def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
			def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
			if ('true'.equals(useQualifiedHostName)) {
					notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
			}

			String vnfAdapterRestRequest = """
				<updateVolumeGroupRequest>
					<cloudSiteId>${aicCloudRegion}</cloudSiteId>
					<tenantId>${tenantId}</tenantId>
					<volumeGroupId>${volumeGroupId}</volumeGroupId>
					<volumeGroupStackId>${volumeGroupHeatStackId}</volumeGroupStackId>
					<vnfType>${vnfType}</vnfType>
					<vnfVersion></vnfVersion>
					<vfModuleType></vfModuleType>
					<volumeGroupParams>
						${volumeGroupParams}
				    </volumeGroupParams>
					<skipAAI>true</skipAAI>
				    <msoRequest>
				        <requestId>${requestId}</requestId>
				        <serviceInstanceId>${serviceId}</serviceInstanceId>
				    </msoRequest>
				    <messageId>${messageId}</messageId>
				    <notificationUrl>${notificationUrl}</notificationUrl>
				</updateVolumeGroupRequest>
			"""
			vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
			execution.setVariable('UPDVfModVol_vnfAdapterRestRequest', vnfAdapterRestRequest)
			logDebug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest, isDebugLogEnabled)

			utils.logAudit("UpdateVfModuleVolume Request for VNFAdapter Rest: " + vnfAdapterRestRequest)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepVnfAdapterRest(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for updating the DB for this Infra request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepDbInfraDbRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepDbInfraDbRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('UPDVfMod_requestId')

			String updateInfraRequest = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
						xmlns:req="http://org.openecomp.mso/requestsdb">
					<soapenv:Header/>
					<soapenv:Body>
						<req:updateInfraRequest>
							<requestId>${requestId}</requestId>
							<lastModifiedBy>BPEL</lastModifiedBy>
							<requestStatus>COMPLETE</requestStatus>
							<progress>100</progress>
						</req:updateInfraRequest>
					</soapenv:Body>
				</soapenv:Envelope>
			"""

			updateInfraRequest = utils.formatXml(updateInfraRequest)
			execution.setVariable('UPDVfModVol_updateInfraRequest', updateInfraRequest)
			logDebug('Request for Update Infra Request:\n' + updateInfraRequest, isDebugLogEnabled)

			utils.logAudit("UpdateVfModuleVolume Request for Updating DB for Infra: " + updateInfraRequest)
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildWorkflowException(execution, 1002, 'Error in prepDbInfraDbRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Build a "CompletionHandler" request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepCompletionHandlerRequest(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepCompletionHandlerRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('UPDVfModVol_requestInfo')

			String content = """
				<sdncadapterworkflow:MsoCompletionRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1">
					${requestInfo}
					<sdncadapterworkflow:mso-bpel-name>MSO_ACTIVATE_BPEL</sdncadapterworkflow:mso-bpel-name>
				</sdncadapterworkflow:MsoCompletionRequest>
			"""

			content = utils.formatXml(content)
			logDebug('Request for Completion Handler:\n' + content, isDebugLogEnabled)
			utils.logAudit("UpdateVfModuleVolume Completion Handler request: " + content)
			execution.setVariable('UPDVfModVol_CompletionHandlerRequest', content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in prepCompletionHandlerRequest(): ' + e.getMessage())
		}
	}

	/**
	 * Build a "FalloutHandler" request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepFalloutHandler(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepFalloutHandler(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestInfo = execution.getVariable('UPDVfModVol_requestInfo')

			def WorkflowException workflowException = execution.getVariable("WorkflowException")
			def errorResponseCode = workflowException.getErrorCode()
			def errorResponseMsg = workflowException.getErrorMessage()
			def encErrorResponseMsg = ""
			if (errorResponseMsg != null) {
				encErrorResponseMsg = errorResponseMsg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
			}

			String content = """
				<sdncadapterworkflow:FalloutHandlerRequest xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
						xmlns:reqtype="http://org.openecomp/mso/request/types/v1"
						xmlns:msoservtypes="http://org.openecomp/mso/request/types/v1"
						xmlns:structuredtypes="http://org.openecomp/mso/structured/types/v1">
					${requestInfo}
					<sdncadapterworkflow:WorkflowException>
						<sdncadapterworkflow:ErrorMessage>${encErrorResponseMsg}</sdncadapterworkflow:ErrorMessage>
						<sdncadapterworkflow:ErrorCode>${errorResponseCode}</sdncadapterworkflow:ErrorCode>
					</sdncadapterworkflow:WorkflowException>
				</sdncadapterworkflow:FalloutHandlerRequest>
			"""
			content = utils.formatXml(content)
			logDebug('Request for Fallout Handler:\n' + content, isDebugLogEnabled)
			utils.logAudit("UpdateVfModuleVolume Fallout request: " + content)
			execution.setVariable('UPDVfModVol_FalloutHandlerRequest', content)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildWorkflowException(execution, 1002, 'Error in prepFalloutHandler(): ' + e.getMessage())
		}
	}

	/**
	 * Create a WorkflowException for the error case where the Tenant Id from
	 * AAI did not match the Tenant Id in the incoming request.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleTenantIdMismatch(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.handleTenantIdMismatch(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		String processKey = getProcessKey(execution);
		def volumeGroupId = execution.getVariable('UPDVfModVol_volumeGroupId')
		def aicCloudRegion = execution.getVariable('UPDVfModVol_aicCloudRegion')
		def tenantId = execution.getVariable('UPDVfModVol_tenantId')
		def volumeGroupTenantId = execution.getVariable('UPDVfModVol_volumeGroupTenantId')

		def String errorMessage = 'TenantId \'' + tenantId + '\' in incoming request does not match Tenant Id \'' + volumeGroupTenantId +
			'\' retrieved from AAI for Volume Group Id \'' + volumeGroupId + '\', AIC Cloud Region \'' + aicCloudRegion + '\''

		logError('Error in UpdateVfModuleVol: ' + errorMessage)

		WorkflowException exception = new WorkflowException(processKey, 5000, errorMessage);
		execution.setVariable("WorkflowException", exception);

		logDebug('Exited ' + method, isDebugLogEnabled)
		utils.logAudit("UpdateVfModuleVolume workflowException in Tenant Mismatch: " + errorMessage)
	}
}
