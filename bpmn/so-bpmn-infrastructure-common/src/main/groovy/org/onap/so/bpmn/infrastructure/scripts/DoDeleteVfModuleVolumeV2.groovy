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

package org.onap.so.bpmn.infrastructure.scripts

import jakarta.ws.rs.NotFoundException
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
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.constants.Defaults
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoDeleteVfModuleVolumeV2 extends AbstractServiceTaskProcessor{
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteVfModuleVolumeV2.class);

	String prefix="DDVMV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	XmlParser xmlParser = new XmlParser()
	JsonUtils jsonUtil = new JsonUtils()

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		preProcessRequest(execution, isDebugEnabled)
	}

	/**
	 * Set default variable values
	 * @param execution
	 * @param isDebugLogEnabled
	 */
	public void preProcessRequest (DelegateExecution execution, isDebugEnabled) {

		//Input:
		//  msoRequestId
		//  isDebugLogEnabled
		//  failIfNotFound (Optional)
		//  serviceInstanceId (Optional)
		//  vnfId (Optional)
		//  volumeGroupId
		//  vfModuleModelInfo (Optional)
		//  lcpCloudRegionId (Optional)			@TODO: this is actually required
		//  tenantId (Optional)					@TODO: this is actually required
		//  cloudConfiguration					@TODO: temporary solution? this contains lcpCloudregion and tenantId
		//
		//Output:
		//  workflowException					@TODO: actual variable name is WorkflowException
		//  rolledBack
		//  wasDeleted

		execution.setVariable('prefix', prefix)
		execution.setVariable('wasDeleted', 'false')

		def tenantId = execution.getVariable("tenantId")
		def cloudSiteId = execution.getVariable("lcpCloudRegionId")

		// if tenantId or lcpCloudregionId is not passed, get it from cloudRegionConfiguration variable
		if(!tenantId || !cloudSiteId) {
			def cloudConfiguration = execution.getVariable("cloudConfiguration")
			logger.debug("Using cloudConfiguration variable to get tenantId and lcpCloudRegionId - " + cloudConfiguration)
			tenantId = jsonUtil.getJsonValue(cloudConfiguration, "tenantId")
			execution.setVariable("tenantId", tenantId)
			cloudSiteId = jsonUtil.getJsonValue(cloudConfiguration, "lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", cloudSiteId)
			cloudOwner = jsonUtil.getJsonValue(cloudConfiguration, "cloudOwner")
			execution.setVariable("cloudOwner", cloudOwner)
		}
	}


	/**
	 * Set out 'wasDeleted' variable to 'true'
	 * @param execution
	 * @param isDebugLogEnabled
	 */
	public void postProcess(DelegateExecution execution, isDebugLogEnabled) {
		execution.setVariable('wasDeleted', 'true')
	}


	/**
	 * Query and set cloud region to use for AAI calls
	 * Output variables: prefix+'aicCloudRegion', prefix+'cloudRegion'
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTQueryAAICloudRegion(DelegateExecution execution, isDebugEnabled) {

		String cloudRegion = execution.getVariable('lcpCloudRegionId')
		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion))
		def queryCloudRegionRequest = aaiUtil.createAaiUri(uri)

		cloudRegion = aaiUtil.getAAICloudReqion(execution,  queryCloudRegionRequest, "PO", cloudRegion)

		if ((cloudRegion != "ERROR")) {
			if(execution.getVariable(prefix+"queryCloudRegionReturnCode") == "404") {
				execution.setVariable(prefix+"aicCloudRegion", "AAIAIC25")
			}
			else{
				execution.setVariable(prefix+"aicCloudRegion", cloudRegion)
			}
		}
		else {
			logger.debug("AAI Query Cloud Region Unsuccessful.")
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "AAI Query Cloud Region Unsuccessful. Return Code: " + execution.getVariable(prefix+"queryCloudRegionReturnCode"))
		}
	}


	/**
	 * Query AAI Volume Group
	 * Output variables: prefix+'queryAAIVolGrpResponse'; prefix+'volumeGroupHeatStackId'
	 * @param execution
	 * @param isDebugLogEnabled
	 */
	public void callRESTQueryAAIForVolumeGroup(DelegateExecution execution, isDebugLogEnabled) {

		def tenantId = execution.getVariable('tenantId')
		def volumeGroupId = execution.getVariable('volumeGroupId')
		if(volumeGroupId == null) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, 'volumeGroupId is not provided in the request')
			throw new Exception('volume-group-id is not provided in the request')
		}
		String cloudRegion = execution.getVariable(prefix+'aicCloudRegion')

		try {
			AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion).volumeGroup(volumeGroupId))
			Optional<VolumeGroup> volumeGroupOps = getAAIClient().get(VolumeGroup.class,resourceUri)
            if(volumeGroupOps.present) {
                VolumeGroup volumeGroup = volumeGroupOps.get()
                execution.setVariable(prefix + "queryAAIVolGrpResponse", volumeGroup)
                def heatStackId = volumeGroup.getHeatStackId()==null ? '' : volumeGroup.getHeatStackId()
                execution.setVariable(prefix+'volumeGroupHeatStackId', heatStackId)

                logger.debug('Heat stack id from AAI response: ' + heatStackId)
				AAIResultWrapper wrapper = getAAIClient().get(resourceUri);
				Optional<Relationships> relationships = wrapper.getRelationships()
				String volumeGroupTenantId = null

				if(relationships.isPresent()){
					if(relationships.get().getRelatedUris(Types.VF_MODULE)){
						logger.debug('Volume Group ' + volumeGroupId + ' currently in use')
						exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group ${volumeGroupId} currently in use - found vf-module relationship.")
					}
					for(AAIResourceUri aaiResourceUri: relationships.get().getRelatedUris(Types.TENANT)){
						volumeGroupTenantId = aaiResourceUri.getURIKeys().get(AAIFluentTypeBuilder.Types.TENANT.getUriParams().tenantId)
					}
				}

                logger.debug('Tenant ID from AAI response: ' + volumeGroupTenantId)

                if (volumeGroupTenantId == null) {
                    logger.debug("Could not find Tenant Id element in Volume Group with Volume Group Id ${volumeGroupId}")
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Could not find Tenant Id element in Volume Group with Volume Group Id ${volumeGroupId}")
                }

                if (volumeGroupTenantId != tenantId) {
                    def String errorMessage = 'TenantId ' + tenantId + ' in incoming request does not match Tenant Id ' + volumeGroupTenantId +	' retrieved from AAI for Volume Group Id ' + volumeGroupId
                    logger.debug("Error in DeleteVfModuleVolume: " + errorMessage)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 5000, errorMessage)
                }
                logger.debug('Received Tenant Id ' + volumeGroupTenantId + ' from AAI for Volume Group with Volume Group Id ' + volumeGroupId )
            }else{
                execution.setVariable(prefix + "queryAAIVolGrpResponse", "Volume Group ${volumeGroupId} not found in AAI. Response code: 404")
                logger.debug("Volume Group ${volumeGroupId} not found in AAI")
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group ${volumeGroupId} not found in AAI. Response code: 404")
            }
		}catch (Exception ex) {
            execution.setVariable(prefix+"queryAAIVolGrpResponse", ex.getMessage())
            WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(ex.getMessage(), execution)
            throw new BpmnError("MSOWorkflowException")
		}
	}

	/**
	 * Format VNF Adapter subflow request XML
	 * Variables: prefix+'deleteVnfARequest'
	 * @param execution
	 * @param isDebugLogEnabled
	 */
	public void prepareVnfAdapterDeleteRequest(DelegateExecution execution, isDebugLogEnabled) {
		def cloudRegion = execution.getVariable(prefix+'aicCloudRegion')
		def cloudOwner = execution.getVariable(prefix+'cloudOwner')
		def tenantId = execution.getVariable('tenantId')										// input parameter (optional) - see preProcessRequest
		def volumeGroupId = execution.getVariable('volumeGroupId')								// input parameter (required)
		def volumeGroupHeatStackId = execution.getVariable(prefix+'volumeGroupHeatStackId')		// from AAI query volume group
		def requestId = execution.getVariable('msoRequestId')									// input parameter (required)
		def serviceId = execution.getVariable('serviceInstanceId')								// imput parameter (optional)

		def messageId = UUID.randomUUID().toString()
		def notificationUrl = createCallbackURL(execution, "VNFAResponse", messageId)
		def useQualifiedHostName = UrnPropertiesReader.getVariable("mso.use.qualified.host",execution)
		if ('true'.equals(useQualifiedHostName)) {
				notificationUrl = utils.getQualifiedHostNameForCallback(notificationUrl)
		}

		String vnfAdapterRestRequest = """
			<deleteVolumeGroupRequest>
				<cloudSiteId>${MsoUtils.xmlEscape(cloudRegion)}</cloudSiteId>
				<cloudOwner>${MsoUtils.xmlEscape(cloudOwner)}</cloudOwner>
				<tenantId>${MsoUtils.xmlEscape(tenantId)}</tenantId>
				<volumeGroupId>${MsoUtils.xmlEscape(volumeGroupId)}</volumeGroupId>
				<volumeGroupStackId>${MsoUtils.xmlEscape(volumeGroupHeatStackId)}</volumeGroupStackId>
				<skipAAI>true</skipAAI>
			    <msoRequest>
			        <requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
			        <serviceInstanceId>${MsoUtils.xmlEscape(serviceId)}</serviceInstanceId>
			    </msoRequest>
			    <messageId>${MsoUtils.xmlEscape(messageId)}</messageId>
			    <notificationUrl>${MsoUtils.xmlEscape(notificationUrl)}</notificationUrl>
			</deleteVolumeGroupRequest>
		"""
		vnfAdapterRestRequest = utils.formatXml(vnfAdapterRestRequest)
		execution.setVariable(prefix+'deleteVnfARequest', vnfAdapterRestRequest)
		logger.debug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest)
	}


	/**
	 * Delete volume group in AAI
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTDeleteAAIVolumeGroup(DelegateExecution execution, isDebugEnabled) {

		// get variables
		VolumeGroup volumeGroupResponse = execution.getVariable(prefix+"queryAAIVolGrpResponse")
		String volumeGroupId = volumeGroupResponse.getVolumeGroupId()
		String cloudRegion = execution.getVariable(prefix+'aicCloudRegion')

        try {
            AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), cloudRegion).volumeGroup(volumeGroupId))
            getAAIClient().delete(resourceUri)
            logger.debug("Volume group $volumeGroupId deleted.")
        }catch (NotFoundException ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $volumeGroupId not found for delete in AAI Response code: 404")
        }catch (Exception ex) {
            WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(ex.getMessage(), execution)
            throw new BpmnError("MSOWorkflowException")
        }
	}

}
