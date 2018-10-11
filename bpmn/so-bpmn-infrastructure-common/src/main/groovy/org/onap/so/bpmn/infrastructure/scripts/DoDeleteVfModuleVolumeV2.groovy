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

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.constants.Defaults
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.springframework.web.util.UriUtils

class DoDeleteVfModuleVolumeV2 extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DoDeleteVfModuleVolumeV2.class);

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
			msoLogger.debug("Using cloudConfiguration variable to get tenantId and lcpCloudRegionId - " + cloudConfiguration)
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

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, Defaults.CLOUD_OWNER.toString(), cloudRegion)
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
			msoLogger.debug("AAI Query Cloud Region Unsuccessful.")
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

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, volumeGroupId)
		def queryAAIVolumeGroupRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest)
		msoLogger.debug('Query AAI volume group by ID: ' + queryAAIVolumeGroupRequest)

		APIResponse response = aaiUtil.executeAAIGetCall(execution, queryAAIVolumeGroupRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI query volume group by id return code: " + returnCode)
		msoLogger.debug("AAI query volume group by id response: " + aaiResponseAsString)
		msoLogger.debug('AAI query volume group by id return code: ' + returnCode)
		msoLogger.debug('AAI query volume group by id response: ' + aaiResponseAsString)

		execution.setVariable(prefix+"queryAAIVolGrpResponse", aaiResponseAsString)

		if (returnCode=='200' || returnCode == '204') {

			def heatStackId = getNodeTextForce(aaiResponseAsString, 'heat-stack-id')
			execution.setVariable(prefix+'volumeGroupHeatStackId', heatStackId)

			msoLogger.debug('Heat stack id from AAI response: ' + heatStackId)

			if(hasVfModuleRelationship(aaiResponseAsString)){
				msoLogger.debug('Volume Group ' + volumeGroupId + ' currently in use')
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group ${volumeGroupId} currently in use - found vf-module relationship.")
			}

			def volumeGroupTenantId = getTenantIdFromVolumeGroup(aaiResponseAsString)
			msoLogger.debug('Tenant ID from AAI response: ' + volumeGroupTenantId)

			if (volumeGroupTenantId == null) {
				msoLogger.debug("Could not find Tenant Id element in Volume Group with Volume Group Id ${volumeGroupId}")
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Could not find Tenant Id element in Volume Group with Volume Group Id ${volumeGroupId}")
			}

			if (volumeGroupTenantId != tenantId) {
				def String errorMessage = 'TenantId ' + tenantId + ' in incoming request does not match Tenant Id ' + volumeGroupTenantId +	' retrieved from AAI for Volume Group Id ' + volumeGroupId
				msoLogger.debug("Error in DeleteVfModuleVolume: " + errorMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 5000, errorMessage)
			}
			msoLogger.debug('Received Tenant Id ' + volumeGroupTenantId + ' from AAI for Volume Group with Volume Group Id ' + volumeGroupId )
		}
		else {
			if (returnCode=='404') {
				msoLogger.debug("Volume Group ${volumeGroupId} not found in AAI")
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume Group ${volumeGroupId} not found in AAI. Response code: 404")
			}
			else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
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
		msoLogger.debug('Request for VNFAdapter Rest:\n' + vnfAdapterRestRequest)
	}


	/**
	 * Delete volume group in AAI
	 * @param execution
	 * @param isDebugEnabled
	 */
	public void callRESTDeleteAAIVolumeGroup(DelegateExecution execution, isDebugEnabled) {

		// get variables
		String queryAAIVolGrpIdResponse = execution.getVariable(prefix+"queryAAIVolGrpResponse")
		String groupId = utils.getNodeText(queryAAIVolGrpIdResponse, "volume-group-id")
		String resourceVersion = utils.getNodeText(queryAAIVolGrpIdResponse, "resource-version")

		String messageId = UUID.randomUUID().toString()
		String cloudRegion = execution.getVariable(prefix+'aicCloudRegion')

		AaiUtil aaiUtil = new AaiUtil(this)

		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegion, groupId).queryParam("resource-version", resourceVersion)
		def deleteAAIVolumeGrpIdRequest = aaiUtil.createAaiUri(uri)

		msoLogger.debug('Delete AAI volume group : ' + deleteAAIVolumeGrpIdRequest)
		msoLogger.debug("Delete AAI volume group : " + deleteAAIVolumeGrpIdRequest)

		APIResponse response = aaiUtil.executeAAIDeleteCall(execution, deleteAAIVolumeGrpIdRequest)

		String returnCode = response.getStatusCode()
		String aaiResponseAsString = response.getResponseBodyAsString()

		msoLogger.debug("AAI delete volume group return code: " + returnCode)
		msoLogger.debug("AAI delete volume group response: " + aaiResponseAsString)
		msoLogger.debug("AAI delete volume group return code: " + returnCode)
		msoLogger.debug("AAI delete volume group response: " + aaiResponseAsString)

		ExceptionUtil exceptionUtil = new ExceptionUtil()
		if (returnCode=='200' || (returnCode == '204')) {
			msoLogger.debug("Volume group $groupId deleted.")
		} else {
			if (returnCode=='404') {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Volume group $groupId not found for delete in AAI Response code: 404")
			} else {
				WorkflowException aWorkflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
				throw new BpmnError("MSOWorkflowException")
			}
		}
	}


	/**
	 * Check if volume group has a relationship to vf-module
	 * @param volumeGroupXml
	 * @return
	 */
	private boolean hasVfModuleRelationship(String volumeGroupXml) {
		def Node volumeGroupNode = xmlParser.parseText(volumeGroupXml)
		def Node relationshipList = utils.getChildNode(volumeGroupNode, 'relationship-list')
		if (relationshipList != null) {
			def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
			for (Node relationship in relationships) {
				def Node relatedTo = utils.getChildNode(relationship, 'related-to')
				if ((relatedTo != null) && (relatedTo.text().equals('vf-module'))) {
					def Node relatedLink = utils.getChildNode(relationship, 'related-link')
					if (relatedLink !=null && relatedLink.text() != null){
						return true
					}
				}
			}
		}
		return false
	}


	/**
	 * Extract the Tenant Id from the Volume Group information returned by AAI.
	 * @param volumeGroupXml Volume Group XML returned by AAI.
	 * @return the Tenant Id extracted from the Volume Group information. 'null' is returned if
	 * the Tenant Id is missing or could not otherwise be extracted.
	 */
	private String getTenantIdFromVolumeGroup(String volumeGroupXml) {
		def Node volumeGroupNode = xmlParser.parseText(volumeGroupXml)
		def Node relationshipList = utils.getChildNode(volumeGroupNode, 'relationship-list')
		if (relationshipList != null) {
			def NodeList relationships = utils.getIdenticalChildren(relationshipList, 'relationship')
			for (Node relationship in relationships) {
				def Node relatedTo = utils.getChildNode(relationship, 'related-to')
				if ((relatedTo != null) && (relatedTo.text().equals('tenant'))) {
					def NodeList relationshipDataList = utils.getIdenticalChildren(relationship, 'relationship-data')
					for (Node relationshipData in relationshipDataList) {
						def Node relationshipKey = utils.getChildNode(relationshipData, 'relationship-key')
						if ((relationshipKey != null) && (relationshipKey.text().equals('tenant.tenant-id'))) {
							def Node relationshipValue = utils.getChildNode(relationshipData, 'relationship-value')
							if (relationshipValue != null) {
								return relationshipValue.text()
							}
						}
					}
				}
			}
		}
		return null
	}
}
