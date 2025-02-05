/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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


import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.VolumeGroups
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.constants.Defaults
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DoCreateVfModuleVolumeRollback extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoCreateVfModuleVolumeRollback.class);

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
	 * @param cloudRegion
	 * @return
	 */
	private String callRESTQueryAAIVolGrpName(DelegateExecution execution, String cloudRegion) {

		def volumeGroupName = execution.getVariable('DCVFMODVOLRBK_volumeGroupName')

		def testVolumeGroupName = execution.getVariable('test-volume-group-name')
		if (testVolumeGroupName != null && testVolumeGroupName.length() > 0) {
			volumeGroupName = testVolumeGroupName
		}

		AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion).volumeGroups()).queryParam("volume-group-name", volumeGroupName)
		try {
			Optional<VolumeGroups> volumeGroups = getAAIClient().get(VolumeGroups.class, uri)
			if (volumeGroups.isPresent()) {
				return volumeGroups.get().getVolumeGroup().get(0).getVolumeGroupId()
			} else {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupName not found in AAI. Response code: 404")
			}
		} catch (Exception e) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, e.getMessage())
		}
		return null
	}



	public void callRESTDeleteAAIVolumeGroup(DelegateExecution execution, isDebugEnabled) {

		String cloudRegion = execution.getVariable("DCVFMODVOLRBK_lcpCloudRegionId")
		String volumeGroupId = callRESTQueryAAIVolGrpName(execution, cloudRegion)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion).volumeGroup(volumeGroupId))
		try {
			getAAIClient().delete(uri)
		}catch(NotFoundException ignored){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupId not found for delete in AAI Response code: 404")
		}catch(Exception e){
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500,e.getMessage())
		}
	}

	// *******************************
	//     Build Error Section
	// *******************************



	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		try{
			logger.debug("Caught a Java Exception in " + Prefix)
			logger.debug("Started processJavaException Method")
			logger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

		}catch(Exception e){
			logger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method" + Prefix)
		}
		logger.debug("Completed processJavaException Method in " + Prefix)
	}

}
