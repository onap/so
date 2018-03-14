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

package org.openecomp.mso.bpmn.common.scripts

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource


/**
 * Vnf Module Subflow for confirming the volume group belongs
 * to the tenant
 *
 * @param tenantId
 * @param volumeGroupId
 *
 */
class ConfirmVolumeGroupTenant extends AbstractServiceTaskProcessor{

	String Prefix="CVGT_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void preProcessRequest(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		utils.log("DEBUG", " ======== STARTED Confirm Volume Group Tenant Subflow ======== ", isDebugEnabled)
		String processKey = getProcessKey(execution);
		try{
			utils.log("DEBUG", " === Started QueryAAIForVolumeGroup Process === ", isDebugEnabled)

			String volumeGroupId = execution.getVariable("volumeGroupId")
			String incomingGroupName = execution.getVariable("volumeGroupName")
			String incomingTenantId = execution.getVariable("tenantId")
			def aicCloudRegion = execution.getVariable("aicCloudRegion")
			String aai = execution.getVariable("URN_aai_endpoint")

			AaiUtil aaiUriUtil = new AaiUtil(this)
			def aai_uri = aaiUriUtil.getCloudInfrastructureCloudRegionUri(execution)
			logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)

			String path = aai + "${aai_uri}/${aicCloudRegion}/volume-groups/volume-group/" + volumeGroupId

			APIResponse queryAAIForVolumeGroupResponse = aaiUriUtil.executeAAIGetCall(execution, path)

			def responseCode = queryAAIForVolumeGroupResponse.getStatusCode()
			execution.setVariable("queryVolumeGroupResponseCode", responseCode)
			String response = queryAAIForVolumeGroupResponse.getResponseBodyAsString()
			response = StringEscapeUtils.unescapeXml(response)

			utils.logAudit("ConfirmVolumeGroup Response: " + response)
			utils.logAudit("ConfirmVolumeGroup Response Code: " + responseCode)

			if(responseCode == 200 && response != null){
				execution.setVariable("queryAAIVolumeGroupResponse", response)
				utils.log("DEBUG", "QueryAAIForVolumeGroup Received a Good REST Response is: \n" + response, isDebugEnabled)

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
					utils.log("DEBUG", "Tenant Ids Match", isDebugEnabled)
					execution.setVariable("tenantIdsMatch", true)
				}else{
					utils.log("DEBUG", "Tenant Ids DO NOT Match", isDebugEnabled)
					execution.setVariable("tenantIdsMatch", false)
				}

				//Determine if Volume Group Names match
				String volumeGroupName = utils.getNodeText1(response, "volume-group-name")
				if(incomingGroupName == null || incomingGroupName.length() < 1){
					utils.log("DEBUG", "Incoming Volume Group Name is NOT Provided.", isDebugEnabled)
					execution.setVariable("groupNamesMatch", true)
				}else{
					utils.log("DEBUG", "Incoming Volume Group Name is: " + incomingGroupName, isDebugEnabled)
					if(volumeGroupName.equals(incomingGroupName)){
						utils.log("DEBUG", "Volume Group Names Match.", isDebugEnabled)
						execution.setVariable("groupNamesMatch", true)
					}else{
						utils.log("DEBUG", "Volume Group Names DO NOT Match.", isDebugEnabled)
						execution.setVariable("groupNamesMatch", false)
					}
				}
			}else{
				utils.log("DEBUG", "QueryAAIForVolumeGroup Bad REST Response!", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1, "Error Searching AAI for Volume Group. Received a Bad Response.")
			}

		}catch(BpmnError b){
			throw b
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing queryAAIForVolumeGroup. Exception is:\n" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in preProcessRequest.")
		}
		utils.log("DEBUG", "=== COMPLETED queryAAIForVolumeGroup Process === ", isDebugEnabled)
	}

	public void assignVolumeHeatId(DelegateExecution execution){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		try{
			utils.log("DEBUG", " === Started assignVolumeHeatId Process === ", isDebugEnabled)

			String response = execution.getVariable("queryAAIVolumeGroupResponse")
			String heatStackId = utils.getNodeText1(response, "heat-stack-id")
			execution.setVariable("volumeHeatStackId", heatStackId)
			execution.setVariable("ConfirmVolumeGroupTenantResponse", heatStackId)
			// TODO: Should deprecate use of processKey+Response variable for the response. Will use "WorkflowResponse" instead
			execution.setVariable("WorkflowResponse", heatStackId)
			utils.log("DEBUG", "Volume Heat Stack Id is: " + heatStackId, isDebugEnabled)

		}catch(Exception e){
		utils.log("ERROR", "Exception Occured Processing assignVolumeHeatId. Exception is:\n" + e, isDebugEnabled)
		exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured in assignVolumeHeatId.")
	}
	utils.log("DEBUG", "=== COMPLETED assignVolumeHeatId Process === ", isDebugEnabled)
	utils.log("DEBUG", "======== COMPLETED Confirm Volume Group Tenant Subflow ======== ", isDebugEnabled)
}

	public void assignWorkflowException(DelegateExecution execution, String message){
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
		String processKey = getProcessKey(execution);
		utils.log("DEBUG", " === STARTED Assign Workflow Exception === ", isDebugEnabled)
		try{
			String volumeGroupId = execution.getVariable("volumeGroupId")
			int errorCode = 1
			String errorMessage = "Volume Group " + volumeGroupId + " " + message

			exceptionUtil.buildWorkflowException(execution, errorCode, errorMessage)
		}catch(Exception e){
			utils.log("ERROR", "Exception Occured Processing assignWorkflowException. Exception is:\n" + e, isDebugEnabled)
		}
		utils.log("DEBUG", "=== COMPLETED Assign Workflow Exception ==== ", isDebugEnabled)
	}



}

