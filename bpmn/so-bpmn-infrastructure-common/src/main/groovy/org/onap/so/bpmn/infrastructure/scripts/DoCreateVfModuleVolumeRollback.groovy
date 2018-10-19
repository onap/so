/*
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

import groovy.xml.XmlUtil

import groovy.json.*


import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.constants.Defaults
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.onap.so.logger.MsoLogger
import org.onap.so.logger.MessageEnum


public class DoCreateVfModuleVolumeRollback extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoCreateVfModuleVolumeRollback.class);

	String Prefix="DCVFMODVOLRBK_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)

	def className = getClass().getSimpleName()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DoCreateVfModuleVolumeRollback.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(DelegateExecution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "volumeGroupName", null)
		execution.setVariable(Prefix + "lcpCloudRegionId", null)
		execution.setVariable(Prefix + "rollbackVnfARequest", null)

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoCreateVfModuleVolumeRollback.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		InitializeProcessVariables(execution)
//		rollbackData.put("DCVFMODULEVOL", "aiccloudregion", cloudSiteId)
		RollbackData rollbackData = execution.getVariable("rollbackData")

//		String vnfId = rollbackData.get("DCVFMODULEVOL", "vnfid")
//		execution.setVariable("DCVFMODVOLRBK_vnfId", vnfId)
//		String vfModuleId = rollbackData.get("DCVFMODULEVOL", "vfmoduleid")
//		execution.setVariable("DCVFMODVOLRBK_vfModuleId", vfModuleId)
//		String source = rollbackData.get("DCVFMODULEVOL", "source")
//		execution.setVariable("DCVFMODVOLRBK_source", source)
//		String serviceInstanceId = rollbackData.get("DCVFMODULEVOL", "serviceInstanceId")
//		execution.setVariable("DCVFMODVOLRBK_serviceInstanceId", serviceInstanceId)
//		String serviceId = rollbackData.get("DCVFMODULEVOL", "service-id")
//		execution.setVariable("DCVFMODVOLRBK_serviceId", serviceId)
//		String vnfType = rollbackData.get("DCVFMODULEVOL", "vnftype")
//		execution.setVariable("DCVFMODVOLRBK_vnfType", vnfType)
//		String vnfName = rollbackData.get("DCVFMODULEVOL", "vnfname")
//		execution.setVariable("DCVFMODVOLRBK_vnfName", vnfName)
//		String tenantId = rollbackData.get("DCVFMODULEVOL", "tenantid")
//		execution.setVariable("DCVFMODVOLRBK_tenantId", tenantId)
//		String vfModuleName = rollbackData.get("DCVFMODULEVOL", "vfmodulename")
//		execution.setVariable("DCVFMODVOLRBK_vfModuleName", vfModuleName)
//		String vfModuleModelName = rollbackData.get("DCVFMODULEVOL", "vfmodulemodelname")
//		execution.setVariable("DCVFMODVOLRBK_vfModuleModelName", vfModuleModelName)
//		String cloudSiteId = rollbackData.get("DCVFMODULEVOL", "aiccloudregion")
//		execution.setVariable("DCVFMODVOLRBK_cloudSiteId", cloudSiteId)
//		String heatStackId = rollbackData.get("DCVFMODULEVOL", "heatstackid")
//		execution.setVariable("DCVFMODVOLRBK_heatStackId", heatStackId)
//		String requestId = rollbackData.get("DCVFMODULEVOL", "msorequestid")
//		execution.setVariable("DCVFMODVOLRBK_requestId", requestId)

		String volumeGroupName = rollbackData.get("DCVFMODULEVOL", "volumeGroupName")
		execution.setVariable("DCVFMODVOLRBK_volumeGroupName", volumeGroupName)

		String lcpCloudRegionId = rollbackData.get("DCVFMODULEVOL", "aiccloudregion")
		execution.setVariable("DCVFMODVOLRBK_lcpCloudRegionId", lcpCloudRegionId)

		execution.setVariable("DCVFMODVOLRBK_rollbackVnfARequest", rollbackData.get("DCVFMODULEVOL", "rollbackVnfARequest"))
		execution.setVariable("DCVFMODVOLRBK_backoutOnFailure", rollbackData.get("DCVFMODULEVOL", "backoutOnFailure"))
		execution.setVariable("DCVFMODVOLRBK_isCreateVnfRollbackNeeded", rollbackData.get("DCVFMODULEVOL", "isCreateVnfRollbackNeeded"))
		execution.setVariable("DCVFMODVOLRBK_isAAIRollbackNeeded", rollbackData.get("DCVFMODULEVOL", "isAAIRollbackNeeded"))

	}

	/**
	 * Query AAI volume group by name
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAIVolGrpName(DelegateExecution execution, isDebugEnabled) {

		def volumeGroupName = execution.getVariable('DCVFMODVOLRBK_volumeGroupName')
		def cloudRegion = execution.getVariable('DCVFMODVOLRBK_lcpCloudRegionId')

		// This is for stub testing
		def testVolumeGroupName = execution.getVariable('test-volume-group-name')
		if (testVolumeGroupName != null && testVolumeGroupName.length() > 0) {
			volumeGroupName = testVolumeGroupName
		}

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion).queryParam("volume-group-name", volumeGroupName)
		def queryAAIVolumeNameRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Query AAI volume group by name: ' + queryAAIVolumeNameRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeNameRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query volume group by name return code: " + returnCode)
		msoLogger.debug("AAI query volume group by name response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		execution.setVariable(prefix+"queryAAIVolGrpNameResponse", aaiResponseAsString)
		execution.setVariable(prefix+'AaiReturnCode', returnCode)

		if (returnCode=='200') {
			// @TODO: verify error code
			// @TODO: create class of literals representing error codes
			execution.setVariable(prefix+'queryAAIVolGrpNameResponse', aaiResponseAsString)
			msoLogger.debug("Volume Group Name $volumeGroupName exists in AAI.")
		} else {
			if (returnCode=='404') {
				msoLogger.debug("Volume Group Name $volumeGroupName does not exist in AAI.")
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupName not found in AAI. Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}



	public void callRESTDeleteAAIVolumeGroup(DelegateExecution execution, isDebugEnabled) {

		callRESTQueryAAIVolGrpName(execution, isDebugEnabled)

		def queryAaiVolumeGroupResponse = execution.getVariable(prefix+'queryAAIVolGrpNameResponse')

		def volumeGroupId = utils.getNodeText(queryAaiVolumeGroupResponse, "volume-group-id")
		def resourceVersion = utils.getNodeText(queryAaiVolumeGroupResponse, "resource-version")

		def cloudRegion = execution.getVariable("DCVFMODVOLRBK_lcpCloudRegionId")

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, volumeGroupId).resourceVersion(resourceVersion)
		def deleteAAIVolumeGrpIdRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Delete AAI volume group : ' + deleteAAIVolumeGrpIdRequest)

		APIResponse response = aaiUtil.executeAAIDeleteCall(execution, deleteAAIVolumeGrpIdRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI delete volume group return code: " + returnCode)
		msoLogger.debug("AAI delete volume group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()

		def volumeGroupNameFound = prefix+'volumeGroupNameFound'
		if (returnCode=='200' || returnCode=='204' ) {
			msoLogger.debug("Volume group $volumeGroupId deleted.")
		} else {
			if (returnCode=='404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupId not found for delete in AAI Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}

	// *******************************
	//     Build Error Section
	// *******************************



	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		try{
			msoLogger.debug("Caught a Java Exception in " + Prefix)
			msoLogger.debug("Started processJavaException Method")
			msoLogger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

		}catch(Exception e){
			msoLogger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method" + Prefix)
		}
		msoLogger.debug("Completed processJavaException Method in " + Prefix)
	}

}
