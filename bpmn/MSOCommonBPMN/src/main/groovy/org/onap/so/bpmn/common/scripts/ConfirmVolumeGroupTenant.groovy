/*-
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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.constants.Defaults
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Vnf Module Subflow for confirming the volume group belongs
 * to the tenant
 *
 * @param tenantId
 * @param volumeGroupId
 *
 */
class ConfirmVolumeGroupTenant extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( ConfirmVolumeGroupTenant.class);

	String Prefix="CVGT_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest(DelegateExecution execution){
		execution.setVariable("prefix", Prefix)
		logger.trace("STARTED Confirm Volume Group Tenant Subflow ")
		try{
			logger.trace("Started QueryAAIForVolumeGroup Process ")

			String volumeGroupId = execution.getVariable("volumeGroupId")
			String incomingGroupName = execution.getVariable("volumeGroupName")
			String incomingTenantId = execution.getVariable("tenantId")
			String aicCloudRegion = execution.getVariable("aicCloudRegion")
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), aicCloudRegion).volumeGroup(volumeGroupId))
			AAIResultWrapper wrapper = getAAIClient().get(uri);
			Optional<VolumeGroup> volumeGroup = wrapper.asBean(VolumeGroup.class)
			Optional<Relationships> relationships = wrapper.getRelationships()
			if(volumeGroup.isPresent()){
				execution.setVariable("queryAAIVolumeGroupResponse", volumeGroup.get())
				String volumeGroupTenantId = ""
				if(relationships.isPresent()){
					List<AAIResourceUri> tenantUris = relationships.get().getRelatedUris(Types.TENANT)
					for (AAIResourceUri tenantURI: tenantUris){
							volumeGroupTenantId = tenantURI.getURIKeys().get(AAIFluentTypeBuilder.Types.TENANT.getUriParams().tenantId)
					}
				}
				//Determine if Tenant Ids match
				if(incomingTenantId.equals(volumeGroupTenantId)){
					logger.debug("Tenant Ids Match")
					execution.setVariable("tenantIdsMatch", true)
				}else{
					logger.debug("Tenant Ids DO NOT Match")
					execution.setVariable("tenantIdsMatch", false)
				}

				//Determine if Volume Group Names match
				String volumeGroupName = volumeGroup.get().getVolumeGroupName()
				if(incomingGroupName == null || incomingGroupName.length() < 1){
					logger.debug("Incoming Volume Group Name is NOT Provided.")
					execution.setVariable("groupNamesMatch", true)
				}else{
					logger.debug("Incoming Volume Group Name is: " + incomingGroupName)
					if(volumeGroupName.equals(incomingGroupName)){
						logger.debug("Volume Group Names Match.")
						execution.setVariable("groupNamesMatch", true)
					}else{
						logger.debug("Volume Group Names DO NOT Match.")
						execution.setVariable("groupNamesMatch", false)
					}
				}
			}else{
				logger.debug("QueryAAIForVolumeGroup Bad REST Response!")
				exceptionUtil.buildAndThrowWorkflowException(execution, 1, "Error Searching AAI for Volume Group. Received a Bad Response.")
			}

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing queryAAIForVolumeGroup.", "BPMN",
					ErrorCode.UnknownError.getValue(), e.getMessage());
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in preProcessRequest.")
		}
		logger.trace("COMPLETED queryAAIForVolumeGroup Process ")
	}

	public void assignVolumeHeatId(DelegateExecution execution){
		execution.setVariable("prefix", Prefix)
		try{
			logger.trace("Started assignVolumeHeatId Process ")

			VolumeGroup volumeGroup = execution.getVariable("queryAAIVolumeGroupResponse")
			String heatStackId = volumeGroup.getHeatStackId()
			execution.setVariable("volumeHeatStackId", heatStackId)
			execution.setVariable("ConfirmVolumeGroupTenantResponse", heatStackId)
			// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
			execution.setVariable("WorkflowResponse", heatStackId)
			logger.debug("Volume Heat Stack Id is: " + heatStackId)

		}catch(Exception e){
		logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
				"Exception Occured Processing assignVolumeHeatId.", "BPMN",
				ErrorCode.UnknownError.getValue(), e);
		exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in assignVolumeHeatId.")
	}
	logger.trace("COMPLETED assignVolumeHeatId Process ")
	logger.trace("COMPLETED Confirm Volume Group Tenant Subflow ")
}

	public void assignWorkflowException(DelegateExecution execution, String message){
		execution.setVariable("prefix", Prefix)
		String processKey = getProcessKey(execution);
		logger.trace("STARTED Assign Workflow Exception ")
		try{
			String volumeGroupId = execution.getVariable("volumeGroupId")
			int errorCode = 1
			String errorMessage = "Volume Group " + volumeGroupId + " " + message

			exceptionUtil.buildWorkflowException(execution, errorCode, errorMessage)
		}catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing assignWorkflowException.", "BPMN",
					ErrorCode.UnknownError.getValue(), e);
		}
		logger.trace("COMPLETED Assign Workflow Exception =")
	}



}

