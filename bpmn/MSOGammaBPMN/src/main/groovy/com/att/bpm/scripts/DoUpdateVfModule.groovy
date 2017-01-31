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

import groovy.util.Node
import groovy.xml.QName

import java.io.Serializable;

import org.springframework.web.util.UriUtils

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException

public class DoUpdateVfModule extends VfModuleBase {

	ExceptionUtil exceptionUtil = new ExceptionUtil()
	
	/**
	 * Initialize the flow's variables.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void initProcessVariables(Execution execution) {
		execution.setVariable('prefix', 'DOUPVfMod_')
		execution.setVariable('DOUPVfMod_requestInfo', null)
		execution.setVariable('DOUPVfMod_serviceInstanceId', null)
		execution.setVariable('DOUPVfMod_requestId', null)
		execution.setVariable('DOUPVfMod_vnfInputs', null)
		execution.setVariable('DOUPVfMod_vnfId', null)
		execution.setVariable('DOUPVfMod_vnfName', null)
		execution.setVariable('DOUPVfMod_vnfNameFromAAI', null)
		execution.setVariable('DOUPVfMod_vfModuleName', null)
		execution.setVariable('DOUPVfMod_vfModuleId', null)
		execution.setVariable('DOUPVfMod_vnfType', null)
		execution.setVariable('DOUPVfMod_asdcServiceModelVersion', null)
		execution.setVariable('DOUPVfMod_vfModuleModelName', null)
		execution.setVariable("DOUPVfMod_isBaseVfModule", "false")
		execution.setVariable('DOUPVfMod_serviceId', null)
		execution.setVariable('DOUPVfMod_aicCloudRegion', null)
		execution.setVariable('DOUPVfMod_tenantId', null)
		execution.setVariable('DOUPVfMod_volumeGroupId', null)
		execution.setVariable('DOUPVfMod_vfModule', null)
		execution.setVariable('DOUPVfMod_vnfParams', null)
		execution.setVariable("DOUPVfMod_baseVfModuleId", "")
		execution.setVariable("DOUPVfMod_baseVfModuleHeatStackId", "")
		execution.setVariable('DOUPVfMod_prepareUpdateAAIVfModuleRequest', null)
		execution.setVariable('DOUPVfMod_sdncTopologyRequest', null)
		execution.setVariable('DOUPVfMod_sdncTopologyResponse', null)
		execution.setVariable('DOUPVfMod_vnfAdapterRestRequest', null)
		execution.setVariable('DOUPVfMod_updateAAIGenericVnfRequest', null)
		execution.setVariable('DOUPVfMod_updateAAIVfModuleRequest', null)
		execution.setVariable('DOUPVfMod_skipUpdateGenericVnf', false)
		execution.setVariable('DoUpdateVfModuleSuccessIndicator', false)
	}	
	
	/**
	 * Check for missing elements in the received request.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void preProcessRequest(Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessRequest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			initProcessVariables(execution)
			def xml = getVariable(execution, 'DoUpdateVfModuleRequest')
			utils.logAudit("DoUpdateVfModule request: " + xml)
			logDebug('Received request xml:\n' + xml, isDebugLogEnabled)
			
			def requestInfo = getRequiredNodeXml(execution, xml, 'request-info')
			execution.setVariable('DOUPVfMod_requestInfo', requestInfo)
			execution.setVariable('DOUPVfMod_requestId', getRequiredNodeText(execution, requestInfo, 'request-id'))
			def serviceInstanceId = execution.getVariable('att-mso-service-instance-id')
			if (serviceInstanceId == null) {
				serviceInstanceId = ''
			}
			execution.setVariable('DOUPVfMod_serviceInstanceId', serviceInstanceId)
			
			def vnfInputs = getRequiredNodeXml(execution, xml, 'vnf-inputs')
			execution.setVariable('DOUPVfMod_vnfInputs', vnfInputs)
			execution.setVariable('DOUPVfMod_vnfId', getRequiredNodeText(execution, vnfInputs, 'vnf-id'))
			execution.setVariable('DOUPVfMod_vfModuleId', getRequiredNodeText(execution, vnfInputs, 'vf-module-id'))
			execution.setVariable('DOUPVfMod_vfModuleName', getNodeTextForce(vnfInputs, 'vf-module-name'))
			execution.setVariable('DOUPVfMod_vnfType', getNodeTextForce(vnfInputs, 'vnf-type'))
			execution.setVariable('DOUPVfMod_vnfName', getNodeTextForce(vnfInputs, 'vnf-name'))
			execution.setVariable('DOUPVfMod_asdcServiceModelVersion', getNodeTextForce(vnfInputs, 'asdc-service-model-version'))
			execution.setVariable('DOUPVfMod_vfModuleModelName', getRequiredNodeText(execution, vnfInputs, 'vf-module-model-name'))
			execution.setVariable('DOUPVfMod_serviceId', getRequiredNodeText(execution, vnfInputs, 'service-id'))
			execution.setVariable('DOUPVfMod_aicCloudRegion', getRequiredNodeText(execution, vnfInputs, 'aic-cloud-region'))
			execution.setVariable('DOUPVfMod_tenantId', getRequiredNodeText(execution, vnfInputs, 'tenant-id'))
			//isBaseVfModule
			def isBaseVfModule = "false"
			if (utils.nodeExists(xml, "is-base-vf-module")) {
				isBaseVfModule = utils.getNodeText(xml, "is-base-vf-module")
				execution.setVariable("DOUPVfMod_isBaseVfModule", isBaseVfModule)
			}			
			logDebug("isBaseVfModule: " + isBaseVfModule, isDebugLogEnabled)
			
			NetworkUtils networkUtils = new NetworkUtils()
			def backoutOnFailure = networkUtils.isRollbackEnabled(execution, xml)
			execution.setVariable("DOUPVfMod_backoutOnFailure", backoutOnFailure)
			
			def String vgi = getNodeTextForce(vnfInputs, 'volume-group-id')
			execution.setVariable('DOUPVfMod_volumeGroupId', vgi)
			
			execution.setVariable('DOUPVfMod_vnfParams', utils.getNodeXml(xml, 'vnf-params', false))
			
			def sdncCallbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			if (sdncCallbackUrl == null || sdncCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'URN_mso_workflow_sdncadapter_callback\' is missing'
				logError(msg)
				createWorkflowException(execution, 2000, msg)
			}
			def vnfCallbackUrl = (String) execution.getVariable('URN_mso_workflow_vnfadapter_rest_callback')
			if (vnfCallbackUrl == null || vnfCallbackUrl.trim().isEmpty()) {
				def msg = 'Required variable \'URN_mso_workflow_vnfadapter_rest_callback\' is missing'
				logError(msg)
				createWorkflowException(execution, 2000, msg)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in preProcessRequest(): ' + e.getMessage())
		}
	}
	
	/**
	 * Prepare a Request for invoking the PrepareUpdateAAIVfModule subflow.  This will
	 * set the orchestration-status to 'pending-update'.
	 * 
	 * @param execution The flow's execution instance.
	 */
	public void prepPrepareUpdateAAIVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.preparePrepareUpdateAAIVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def orchestrationStatus = 'pending-update'
			
			String prepareUpdateAAIVfModuleRequest = """
				<PrepareUpdateAAIVfModuleRequest>
					<vnf-id>${vnfId}</vnf-id>
					<vf-module-id>${vfModuleId}</vf-module-id>
					<orchestration-status>${orchestrationStatus}</orchestration-status>
				</PrepareUpdateAAIVfModuleRequest>
			"""
			prepareUpdateAAIVfModuleRequest = utils.formatXml(prepareUpdateAAIVfModuleRequest)
			execution.setVariable('DOUPVfMod_prepareUpdateAAIVfModuleRequest', prepareUpdateAAIVfModuleRequest)
			utils.logAudit("DoUpdateAAIVfModule request: " + prepareUpdateAAIVfModuleRequest)
			logDebug('Request for PrepareUpdateAAIVfModule:\n' + prepareUpdateAAIVfModuleRequest, isDebugLogEnabled)
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in preparePrepareUpdateAAIVfModule(): ' + e.getMessage())
		}
	}
	
	/**
	 * Prepare a Request for invoking the ConfirmVolumeGroupTenant subflow.  Currently,
	 * there is really nothing to do, so we just log that we're passing through.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepConfirmVolumeGroupTenant(Execution execution) {
		def method = getClass().getSimpleName() + '.prepConfirmVolumeGroupTenant(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			// Nothing to do - just log that we're passing through here
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepConfirmVolumeGroupTenant(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the SDNC Adapter subflow to perform
	 * a VNF topology 'changeassign' operation.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepSDNCTopologyChg(Execution execution) {
		def method = getClass().getSimpleName() + '.prepSDNCTopologyChg(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			def serviceId = execution.getVariable('DOUPVfMod_serviceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')			
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def VfModule vfModule = (VfModule) execution.getVariable('DOUPVfMod_vfModule')
			def vfModuleName = vfModule.getElementText('vf-module-name')
			def tenantId = execution.getVariable('DOUPVfMod_tenantId')
			def aicCloudRegion = execution.getVariable('DOUPVfMod_aicCloudRegion')
			
			// Retrieve vnf name from AAI response
			def vnfName = execution.getVariable('DOUPVfMod_vnfNameFromAAI')
			execution.setVariable('DOUPVfMod_vnfName', vnfName)
			
			def vnfParamsXml = execution.getVariable('DOUPVfMod_vnfParams')
			def vnfNetworks = transformNetworkParamsToVnfNetworks(vnfParamsXml)
			
			String sdncTopologyRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest
						xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.att.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>   
						<sdncadapter:SvcAction>changeassign</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					         <request-id>${requestId}</request-id>
					         <request-action>ChangeVNFActivateRequest</request-action>
					         <source>PORTAL</source>
					         <notification-url/>
					         <order-number/>
					         <order-version/>
					      </request-information>
					      <service-information>
					         <service-type>${serviceId}</service-type>
					         <service-instance-id>${vnfId}</service-instance-id>
					         <subscriber-name>dontcare</subscriber-name>
					      </service-information>
					      <vnf-request-information>
					         <vnf-id>${vfModuleId}</vnf-id>
					         <vnf-type>${vfModuleModelName}</vnf-type>					         
					         <vnf-name>${vfModuleName}</vnf-name>
					         <generic-vnf-id>${vnfId}</generic-vnf-id>
					         <generic-vnf-name>${vnfName}</generic-vnf-name>
							 <generic-vnf-type>${vnfType}</generic-vnf-type>
					         <tenant>${tenantId}</tenant>
					         <aic-cloud-region>${aicCloudRegion}</aic-cloud-region>	
					         ${vnfNetworks}							
					      </vnf-request-information>
 					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncTopologyRequest', sdncTopologyRequest)
			utils.logAudit("sdncTopologyRequest : " + sdncTopologyRequest)
			logDebug('Request for SDNCAdapter topology/changeassign:\n' + sdncTopologyRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepSDNCTopologyChg(): ' + e.getMessage())
		}
	}
	
	/**
	 * Prepare a Request for invoking the SDNC Adapter subflow to perform
	 * a VNF topology 'query' operation.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepSDNCTopologyQuery(Execution execution) {
		def method = getClass().getSimpleName() + '.prepSDNCTopologyQuery(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			
			def svcInstId = ""
			if (serviceInstanceId == null || serviceInstanceId.isEmpty()) {
				svcInstId = vfModuleId
			}
			else {
				svcInstId = serviceInstanceId
			}
			
			//!!!! TEMPORARY WORKAROUND FOR SDNC REPLICATION ISSUE
			sleep(5000)		

			String sdncTopologyRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest
						xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.att.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${svcInstId}</sdncadapter:SvcInstanceId>   
						<sdncadapter:SvcAction>query</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>/VNF-API:vnfs/vnf-list/${vfModuleId}</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
						<sdncadapter:MsoAction>mobility</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncTopologyRequest', sdncTopologyRequest)
			utils.logAudit("sdncTopologyRequest : " + sdncTopologyRequest)
			logDebug('Request for SDNCAdapter query:\n' + sdncTopologyRequest, isDebugLogEnabled)
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepSDNCTopologyQuery(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the VnfAdapterRest subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepVnfAdapterRest(Execution execution) {
		def method = getClass().getSimpleName() + '.prepVnfAdapterRest(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleName = execution.getVariable('DOUPVfMod_vfModuleName')
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')
			def tenantId = execution.getVariable('DOUPVfMod_tenantId')
			def volumeGroupId = execution.getVariable('DOUPVfMod_volumeGroupId')
			def VfModule vfModule = (VfModule) execution.getVariable('DOUPVfMod_vfModule')
			def heatStackId = vfModule.getElementText('heat-stack-id')
			def cloudId = execution.getVariable('DOUPVfMod_aicCloudRegion')
			def vnfType = execution.getVariable('DOUPVfMod_vnfType')
			def vnfName = execution.getVariable('DOUPVfMod_vnfName')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')			
			def baseVfModuleId = execution.getVariable("DOUPVfMod_baseVfModuleId")		
			def baseVfModuleStackId = execution.getVariable("DOUPVfMod_baseVfModuleHeatStackId")
			def asdcServiceModelVersion = execution.getVariable('DOUPVfMod_asdcServiceModelVersion')
			def backoutOnFailure = execution.getVariable("DOUPVfMod_backoutOnFailure")
			
			def vnfParamsXml = execution.getVariable('DOUPVfMod_vnfParams')
			def vfModuleParamsEntries = transformParamsToEntries(vnfParamsXml)
			
			def messageId = execution.getVariable('att-mso-request-id') + '-' + System.currentTimeMillis()
			def notificationUrl = execution.getVariable("URN_mso_workflow_vnfadapter_rest_callback")
			def useQualifiedHostName = execution.getVariable("URN_mso_use_qualified_host")
			if ('true'.equals(useQualifiedHostName)) {
					notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
			}
			
			String sdncGetResponse = execution.getVariable('DOUPVfMod_sdncTopologyResponse')
			
			String vfModuleParams = buildVfModuleParams(vfModuleParamsEntries, sdncGetResponse, vnfId, vnfName,
					vfModuleId, vfModuleName)		
			
			
			String vnfAdapterRestRequest = """
				<updateVfModuleRequest>
					<cloudSiteId>${cloudId}</cloudSiteId>
					<tenantId>${tenantId}</tenantId>
					<vnfId>${vnfId}</vnfId>
					<vfModuleId>${vfModuleId}</vfModuleId>
					<vfModuleStackId>${heatStackId}</vfModuleStackId>
					<vnfType>${vnfType}</vnfType>
					<vnfVersion>${asdcServiceModelVersion}</vnfVersion>
					<vfModuleType>${vfModuleModelName}</vfModuleType>
					<volumeGroupId>${volumeGroupId}</volumeGroupId>
					<baseVfModuleId>${baseVfModuleId}</baseVfModuleId>
    				<baseVfModuleStackId>${baseVfModuleStackId}</baseVfModuleStackId>
					<skipAAI>true</skipAAI>
					<backout>${backoutOnFailure}</backout>
				    <failIfExists>false</failIfExists>
					<vfModuleParams>						
						${vfModuleParams}
				    </vfModuleParams>
				    <msoRequest>
				        <requestId>${requestId}</requestId>
				        <serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
				    </msoRequest>
				    <messageId>${messageId}</messageId>
				    <notificationUrl>${notificationUrl}</notificationUrl>
				</updateVfModuleRequest>
			"""
			vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
			execution.setVariable('DOUPVfMod_vnfAdapterRestRequest', vnfAdapterRestRequest)
			utils.logAudit("vnfAdapterRestRequest : " + vnfAdapterRestRequest)
			logDebug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepVnfAdapterRest(): ' + e.getMessage())
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
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')
			
			def personaModelId = utils.getNodeText1(vnfInputs, 'vnf-persona-model-id')
			def personaModelVersion = utils.getNodeText1(vnfInputs, 'vnf-persona-model-version')
			if ((personaModelId == null) || (personaModelVersion == null)) {
				logDebug('Skipping update for Generic VNF ' + vnfId +
					' because either \'vnf-persona-model-id\' or \'vnf-persona-model-version\' is absent', isDebugLogEnabled)
				execution.setVariable('DOUPVfMod_skipUpdateGenericVnf', true)
			} else {
				def personaModelIdElement = '<persona-model-id>' + personaModelId + '</persona-model-id>'
				def personaModelVersionElement = '<persona-model-version>' + personaModelVersion + '</persona-model-version>'
				
				String updateAAIGenericVnfRequest = """
					<UpdateAAIGenericVnfRequest>
						<vnf-id>${vnfId}</vnf-id>
						${personaModelIdElement}
						${personaModelVersionElement}
					</UpdateAAIGenericVnfRequest>
				"""
				updateAAIGenericVnfRequest = utils.formatXml(updateAAIGenericVnfRequest)
				execution.setVariable('DOUPVfMod_updateAAIGenericVnfRequest', updateAAIGenericVnfRequest)
				utils.logAudit("updateAAIGenericVnfRequest : " + updateAAIGenericVnfRequest)
				logDebug('Request for UpdateAAIGenericVnf:\n' + updateAAIGenericVnfRequest, isDebugLogEnabled)
			}

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepUpdateAAIGenericVnf(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the UpdateAAIVfModule subflow.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepUpdateAAIVfModule(Execution execution) {
		def method = getClass().getSimpleName() + '.prepUpdateAAIVfModule(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def orchestrationStatus = 'updated'
			def vnfInputs = execution.getVariable('DOUPVfMod_vnfInputs')
			
			def volumeGroupIdElement = ''
			def volumeGroupId = execution.getVariable('DOUPVfMod_volumeGroupId')
			if (volumeGroupId != null) {
				volumeGroupIdElement = '<volume-group-id>' + volumeGroupId + '</volume-group-id>'
			}
			def personaModelIdElement = ''
			def personaModelId = utils.getNodeText1(vnfInputs, 'persona-model-id')
			if (personaModelId != null) {
				personaModelIdElement = '<persona-model-id>' + personaModelId + '</persona-model-id>'
			}
			def personaModelVersionElement = ''
			def personaModelVersion = utils.getNodeText1(vnfInputs, 'persona-model-version')
			if (personaModelVersion != null) {
				personaModelVersionElement = '<persona-model-version>' + personaModelVersion + '</persona-model-version>'
			}
			def contrailServiceInstanceFqdnElement = ''
			def contrailServiceInstanceFqdn = utils.getNodeText1(vnfInputs, 'contrail-service-instance-fqdn')
			if (contrailServiceInstanceFqdn != null) {
				contrailServiceInstanceFqdnElement = '<contrail-service-instance-fqdn>' + contrailServiceInstanceFqdn + '</contrail-service-instance-fqdn>'
			}
			
			String updateAAIVfModuleRequest = """
				<UpdateAAIVfModuleRequest>
					<vnf-id>${vnfId}</vnf-id>
					<vf-module-id>${vfModuleId}</vf-module-id>
					<orchestration-status>${orchestrationStatus}</orchestration-status>
					${volumeGroupIdElement}
					${personaModelIdElement}
					${personaModelVersionElement}
					${contrailServiceInstanceFqdnElement}
				</UpdateAAIVfModuleRequest>
			"""
			updateAAIVfModuleRequest = utils.formatXml(updateAAIVfModuleRequest)
			execution.setVariable('DOUPVfMod_updateAAIVfModuleRequest', updateAAIVfModuleRequest)
			utils.logAudit("updateAAIVfModuleRequest : " + updateAAIVfModuleRequest)
			logDebug('Request for UpdateAAIVfModule:\n' + updateAAIVfModuleRequest, isDebugLogEnabled)

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepUpdateAAIVfModule(): ' + e.getMessage())
		}
	}

	/**
	 * Prepare a Request for invoking the SDNC Adapter subflow to perform
	 * a VNF topology 'activate' operation.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void prepSDNCTopologyAct(Execution execution) {
		def method = getClass().getSimpleName() + '.prepSDNCTopologyAct(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def requestId = execution.getVariable('DOUPVfMod_requestId')
			def serviceInstanceId = execution.getVariable('DOUPVfMod_serviceInstanceId')
			def callbackUrl = (String) execution.getVariable('URN_mso_workflow_sdncadapter_callback')
			def serviceId = execution.getVariable('DOUPVfMod_serviceId')
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')
			def vfModuleModelName = execution.getVariable('DOUPVfMod_vfModuleModelName')
			def VfModule vfModule = (VfModule) execution.getVariable('DOUPVfMod_vfModule')
			def vfModuleName = vfModule.getElementText('vf-module-name')
			def tenantId = execution.getVariable('DOUPVfMod_tenantId')
			def aicCloudRegion = execution.getVariable('DOUPVfMod_aicCloudRegion')
			
			def vnfParamsXml = execution.getVariable('DOUPVfMod_vnfParams')
			def vnfNetworks = transformNetworkParamsToVnfNetworks(vnfParamsXml)

			String sdncTopologyRequest = """
				<sdncadapterworkflow:SDNCAdapterWorkflowRequest
						xmlns:sdncadapterworkflow="http://ecomp.att.com/mso/workflow/schema/v1"
						xmlns:sdncadapter="http://domain2.att.com/workflow/sdnc/adapter/schema/v1">
					<sdncadapter:RequestHeader>
						<sdncadapter:RequestId>${requestId}</sdncadapter:RequestId>
						<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>   
						<sdncadapter:SvcAction>activate</sdncadapter:SvcAction>
						<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
						<sdncadapter:CallbackUrl>${callbackUrl}</sdncadapter:CallbackUrl>
					</sdncadapter:RequestHeader>
					<sdncadapterworkflow:SDNCRequestData>
					      <request-information>
					         <request-id>${requestId}</request-id>
					         <request-action>ChangeVNFActivateRequest</request-action>
					         <source>PORTAL</source>
					         <notification-url/>
					         <order-number/>
					         <order-version/>
					      </request-information>
					      <service-information>
					         <service-type>${serviceId}</service-type>
					         <service-instance-id>${vnfId}</service-instance-id>
					         <subscriber-name>dontcare</subscriber-name>
					      </service-information>
					      <vnf-request-information>
					         <vnf-id>${vfModuleId}</vnf-id>
					         <vnf-type>${vfModuleModelName}</vnf-type>
					         <vnf-name>${vfModuleName}</vnf-name>
					         <tenant>${tenantId}</tenant>
					         <aic-clli>${aicCloudRegion}</aic-clli>	<!-- Optional -->
					         ${vnfNetworks}							<!-- Optional -->
					      </vnf-request-information>
 					</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>
			"""
			sdncTopologyRequest = utils.formatXml(sdncTopologyRequest)
			execution.setVariable('DOUPVfMod_sdncTopologyRequest', sdncTopologyRequest)
			utils.logAudit("sdncTopologyRequest : " + sdncTopologyRequest)
			logDebug('Request for SDNCAdapter topology/activate:\n' + sdncTopologyRequest, isDebugLogEnabled)
			

			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in prepSDNCTopologyAct(): ' + e.getMessage())
		}
	}

	/**
	 * Log a WorkflowException that has been created. 
	 *
	 * @param execution The flow's execution instance.
	 */
	public void handleWorkflowException(Execution execution) {
		def method = getClass().getSimpleName() + '.handleWorkflowException(' +
			'execution=' + execution.getId() +
			')'
		def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def WorkflowException workflowException = (WorkflowException) execution.getVariable('WorkflowException')
			logError(method + ' caught WorkflowException: ' + workflowException.getErrorMessage())
			
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			createWorkflowException(execution, 1002, 'Error in handleWorkflowException(): ' + e.getMessage())
		}
	}
	
	public void validateSDNCResponse(Execution execution, String response, String method){
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def prefix = execution.getVariable("prefix")
		
		logDebug(" *** STARTED ValidateSDNCResponse Process*** ", isDebugLogEnabled)

		WorkflowException workflowException = execution.getVariable("WorkflowException")
		boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

		utils.logAudit("workflowException: " + workflowException)

		SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
		sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

		utils.logAudit("SDNCResponse: " + response)

		String sdncResponse = response
		if(execution.getVariable(prefix + 'sdncResponseSuccess') == true){
			logDebug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse, isDebugLogEnabled)			
		}else{
			logDebug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.", isDebugLogEnabled)
			throw new BpmnError("MSOWorkflowException")
		}
		logDebug(" *** COMPLETED ValidateSDNCResponse Process*** ", isDebugLogEnabled)
	}
	
	/**
	 * Using the received vnfId and vfModuleId, query AAI to get the corresponding VNF info.
	 * A 200 response is expected with the VNF info in the response body. Will find out the base module info.
	 *
	 * @param execution The flow's execution instance.
	 */
	public void queryAAIVfModule(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def method = getClass().getSimpleName() + '.getVfModule(' +
			'execution=' + execution.getId() +
			')'
		logDebug('Entered ' + method, isDebugLogEnabled)

		try {
			def vnfId = execution.getVariable('DOUPVfMod_vnfId')
			def vfModuleId = execution.getVariable('DOUPVfMod_vfModuleId')

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String  aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)

			String endPoint = execution.getVariable("URN_aai_endpoint") + "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8")
			utils.logAudit("AAI endPoint: " + endPoint)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

			try {
				RESTConfig config = new RESTConfig(endPoint);
				def responseData = ''
				def aaiRequestId = UUID.randomUUID().toString()
				RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', aaiRequestId).
					addHeader('X-FromAppId', 'MSO').
					addHeader('Content-Type', 'application/xml').
					addHeader('Accept','application/xml');
				if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
				}
				logDebug('sending GET to AAI endpoint \'' + endPoint + '\'', isDebugLogEnabled)
				APIResponse response = client.httpGet()
				utils.logAudit("createVfModule - invoking httpGet() to AAI")

				responseData = response.getResponseBodyAsString()
				if (responseData != null) {
					logDebug("Received generic VNF data: " + responseData, isDebugLogEnabled)

				}

				utils.logAudit("createVfModule - queryAAIVfModule Response: " + responseData)
				utils.logAudit("createVfModule - queryAAIVfModule ResponseCode: " + response.getStatusCode())

				execution.setVariable('DOUPVfMod_queryAAIVfModuleResponseCode', response.getStatusCode())
				execution.setVariable('DOUPVfMod_queryAAIVfModuleResponse', responseData)
				logDebug('Response code:' + response.getStatusCode(), isDebugLogEnabled)
				logDebug('Response:' + System.lineSeparator() + responseData, isDebugLogEnabled)
				if (response.getStatusCode() == 200) {
					// Parse the VNF record from A&AI to find base module info
					logDebug('Parsing the VNF data to find base module info', isDebugLogEnabled)
					if (responseData != null) {
						def vfModulesText = utils.getNodeXml(responseData, "vf-modules")
						def xmlVfModules= new XmlSlurper().parseText(vfModulesText)
						def vfModules = xmlVfModules.'**'.findAll {it.name() == "vf-module"}
						int vfModulesSize = 0
						for (i in 0..vfModules.size()-1) {
							def vfModuleXml = groovy.xml.XmlUtil.serialize(vfModules[i])
							def isBaseVfModule = utils.getNodeText(vfModuleXml, "is-base-vf-module")

							if (isBaseVfModule == "true") {
							    String baseModuleId = utils.getNodeText1(vfModuleXml, "vf-module-id")
							    execution.setVariable("DOUPVfMod_baseVfModuleId", baseModuleId)
							    logDebug('Received baseVfModuleId: ' + baseModuleId, isDebugLogEnabled)
							    String baseModuleHeatStackId = utils.getNodeText1(vfModuleXml, "heat-stack-id")
							    execution.setVariable("DOUPVfMod_baseVfModuleHeatStackId", baseModuleHeatStackId)
							    logDebug('Received baseVfModuleHeatStackId: ' + baseModuleHeatStackId, isDebugLogEnabled)
							}
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace()
				logDebug('Exception occurred while executing AAI GET:' + ex.getMessage(),isDebugLogEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'AAI GET Failed:' + ex.getMessage())
			}
			logDebug('Exited ' + method, isDebugLogEnabled)
		} catch (BpmnError e) {
			throw e;
		} catch (Exception e) {
			logError('Caught exception in ' + method, e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1002, 'Error in queryAAIVfModule(): ' + e.getMessage())
		}
	}
	
	
}
