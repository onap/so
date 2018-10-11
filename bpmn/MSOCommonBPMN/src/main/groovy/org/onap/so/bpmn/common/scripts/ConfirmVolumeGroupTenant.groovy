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

package org.onap.so.bpmn.common.scripts

import org.onap.so.bpmn.core.UrnPropertiesReader

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.rest.APIResponse
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults

/**
 * Vnf Module Subflow for confirming the volume group belongs
 * to the tenant
 *
 * @param tenantId
 * @param volumeGroupId
 *
 */
class ConfirmVolumeGroupTenant extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ConfirmVolumeGroupTenant.class);

	String Prefix="CVGT_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest(DelegateExecution execution){
		execution.setVariable("prefix", Prefix)
		msoLogger.trace("STARTED Confirm Volume Group Tenant Subflow ")
		String processKey = getProcessKey(execution);
		try{
			msoLogger.trace("Started QueryAAIForVolumeGroup Process ")

			String volumeGroupId = execution.getVariable("volumeGroupId")
			String incomingGroupName = execution.getVariable("volumeGroupName")
			String incomingTenantId = execution.getVariable("tenantId")
			def aicCloudRegion = execution.getVariable("aicCloudRegion")

			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), aicCloudRegion, volumeGroupId)
			String path = aaiUriUtil.createAaiUri(uri)

			APIResponse queryAAIForVolumeGroupResponse = aaiUriUtil.executeAAIGetCall(execution, path)

			def responseCode = queryAAIForVolumeGroupResponse.getStatusCode()
			execution.setVariable("queryVolumeGroupResponseCode", responseCode)
			String response = queryAAIForVolumeGroupResponse.getResponseBodyAsString()

			msoLogger.debug("ConfirmVolumeGroup Response: " + response)
			msoLogger.debug("ConfirmVolumeGroup Response Code: " + responseCode)

			if(responseCode == 200 && response != null){
				execution.setVariable("queryAAIVolumeGroupResponse", response)
				msoLogger.debug("QueryAAIForVolumeGroup Received a Good REST Response is: \n" + response)

				String volumeGroupTenantId = ""
				InputSource source = new InputSource(new StringReader(response));
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				docFactory.setNamespaceAware(true)
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
				Document createVCERequestXml = docBuilder.parse(source)
				NodeList nodeList = createVCERequestXml.getElementsByTagNameNS("*", "relationship")
				for (int x = 0; x < nodeList.getLength(); x++) {
					Node node = nodeList.item(x)
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) node
						String e = eElement.getElementsByTagNameNS("*", "related-to").item(0).getTextContent()
						if(e.equals("tenant")){
							NodeList relationDataList = eElement.getElementsByTagNameNS("*", "relationship-data")
							for (int d = 0; d < relationDataList.getLength(); d++) {
								Node dataNode = relationDataList.item(d)
								if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
									Element dElement = (Element) dataNode
									String key = dElement.getElementsByTagNameNS("*", "relationship-key").item(0).getTextContent()
									if(key.equals("tenant.tenant-id")){
										volumeGroupTenantId = dElement.getElementsByTagNameNS("*", "relationship-value").item(0).getTextContent()
									}
								}
							}
						}
					}
				}

				//Determine if Tenant Ids match
				if(incomingTenantId.equals(volumeGroupTenantId)){
					msoLogger.debug("Tenant Ids Match")
					execution.setVariable("tenantIdsMatch", true)
				}else{
					msoLogger.debug("Tenant Ids DO NOT Match")
					execution.setVariable("tenantIdsMatch", false)
				}

				//Determine if Volume Group Names match
				String volumeGroupName = utils.getNodeText(response, "volume-group-name")
				if(incomingGroupName == null || incomingGroupName.length() < 1){
					msoLogger.debug("Incoming Volume Group Name is NOT Provided.")
					execution.setVariable("groupNamesMatch", true)
				}else{
					msoLogger.debug("Incoming Volume Group Name is: " + incomingGroupName)
					if(volumeGroupName.equals(incomingGroupName)){
						msoLogger.debug("Volume Group Names Match.")
						execution.setVariable("groupNamesMatch", true)
					}else{
						msoLogger.debug("Volume Group Names DO NOT Match.")
						execution.setVariable("groupNamesMatch", false)
					}
				}
			}else{
				msoLogger.debug("QueryAAIForVolumeGroup Bad REST Response!")
				exceptionUtil.buildAndThrowWorkflowException(execution, 1, "Error Searching AAI for Volume Group. Received a Bad Response.")
			}

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing queryAAIForVolumeGroup.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in preProcessRequest.")
		}
		msoLogger.trace("COMPLETED queryAAIForVolumeGroup Process ")
	}

	public void assignVolumeHeatId(DelegateExecution execution){
		execution.setVariable("prefix", Prefix)
		try{
			msoLogger.trace("Started assignVolumeHeatId Process ")

			String response = execution.getVariable("queryAAIVolumeGroupResponse")
			String heatStackId = utils.getNodeText(response, "heat-stack-id")
			execution.setVariable("volumeHeatStackId", heatStackId)
			execution.setVariable("ConfirmVolumeGroupTenantResponse", heatStackId)
			// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
			execution.setVariable("WorkflowResponse", heatStackId)
			msoLogger.debug("Volume Heat Stack Id is: " + heatStackId)

		}catch(Exception e){
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing assignVolumeHeatId.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
		exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in assignVolumeHeatId.")
	}
	msoLogger.trace("COMPLETED assignVolumeHeatId Process ")
	msoLogger.trace("COMPLETED Confirm Volume Group Tenant Subflow ")
}

	public void assignWorkflowException(DelegateExecution execution, String message){
		execution.setVariable("prefix", Prefix)
		String processKey = getProcessKey(execution);
		msoLogger.trace("STARTED Assign Workflow Exception ")
		try{
			String volumeGroupId = execution.getVariable("volumeGroupId")
			int errorCode = 1
			String errorMessage = "Volume Group " + volumeGroupId + " " + message

			exceptionUtil.buildWorkflowException(execution, errorCode, errorMessage)
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Exception Occured Processing assignWorkflowException.", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, e);
		}
		msoLogger.trace("COMPLETED Assign Workflow Exception =")
	}



}

