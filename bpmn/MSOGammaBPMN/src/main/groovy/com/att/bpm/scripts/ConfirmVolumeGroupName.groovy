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

import org.camunda.bpm.engine.runtime.Execution

import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.core.WorkflowException


public class ConfirmVolumeGroupName extends AbstractServiceTaskProcessor{
	
	def Prefix="CVGN_"
	
	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("CVGN_volumeGroupId",null)
		execution.setVariable("CVGN_volumeGroupName",null)
		execution.setVariable("CVGN_aicCloudRegion", null)
		execution.setVariable("CVGN_volumeGroupGetEndpoint",null)
						
		// ConfirmVolumeGroupName workflow response variable placeholders
		execution.setVariable("CVGN_volumeGroupNameMatches", false)
		execution.setVariable("CVGN_queryVolumeGroupResponseCode",null)
		execution.setVariable("CVGN_queryVolumeGroupResponse","")
		execution.setVariable("CVGN_ResponseCode",null)
//		execution.setVariable("CVGN_ErrorResponse","")
		execution.setVariable("RollbackData", null)
	}	
	
	// store the incoming data in the flow Execution
	public void preProcessRequest(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def volumeGroupId = execution.getVariable("ConfirmVolumeGroupName_volumeGroupId")
		def volumeGroupName= execution.getVariable("ConfirmVolumeGroupName_volumeGroupName")
		def aicCloudRegion = execution.getVariable("ConfirmVolumeGroupName_aicCloudRegion")
		
		initProcessVariables(execution)
		execution.setVariable("CVGN_volumeGroupId", volumeGroupId)
		execution.setVariable("CVGN_volumeGroupName", volumeGroupName)
		execution.setVariable("CVGN_aicCloudRegion", aicCloudRegion)
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		def aai_uri = aaiUriUtil.getCloudInfrastructureCloudRegionUri(execution)
		logDebug('AAI URI is: ' + aai_uri, isDebugLogEnabled)
		
		execution.setVariable("CVGN_volumeGroupGetEndpoint","${aai_uri}/${aicCloudRegion}/volume-groups/volume-group/" +
				volumeGroupId)		
	}
	
	// send a GET request to AA&I to retrieve the Volume information based on volume-group-id
	// expect a 200 response with the information in the response body or a 404 if the volume group id does not exist
	public void queryAAIForVolumeGroupId(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def endPoint = execution.getVariable("URN_aai_endpoint") + execution.getVariable("CVGN_volumeGroupGetEndpoint")
		def aaiRequestId = UUID.randomUUID().toString()

		String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))

		RESTConfig config = new RESTConfig(endPoint);
		utils.log("DEBUG","queryAAIForVolumeGroupId() endpoint-" + endPoint, isDebugLogEnabled)
		def responseData = ""
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").addHeader("Content-Type", "application/xml").
				addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			logDebug("invoking GET call to AAI endpoint :"+System.lineSeparator()+endPoint,isDebugLogEnabled)
			APIResponse response = client.httpGet()
				
			responseData = response.getResponseBodyAsString()
			execution.setVariable("CVGN_queryVolumeGroupResponseCode", response.getStatusCode())
			execution.setVariable("CVGN_queryVolumeGroupResponse", responseData)
			logDebug("Response code:" + response.getStatusCode(), isDebugLogEnabled)
			logDebug("Response:" + System.lineSeparator()+responseData,isDebugLogEnabled)
		} catch (Exception ex) {
			ex.printStackTrace()
			logDebug("Exception occurred while executing AAI GET:" + ex.getMessage(),isDebugLogEnabled)
			execution.setVariable("CVGN_queryVolumeGroupResponseCode", 500)
			execution.setVariable("CVGN_queryVolumeGroupResponse", "AAI GET Failed:" + ex.getMessage())
		}
	}
	
	// process the result from queryAAIVolumeGroupId()
	
	public void checkAAIQueryResult(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		def result = execution.getVariable("CVGN_queryVolumeGroupResponse")
		
		if (execution.getVariable("CVGN_queryVolumeGroupResponseCode") == 404) {
			logDebug('volumeGroupId does not exist in AAI', isDebugLogEnabled)
		}
		else if (execution.getVariable("CVGN_queryVolumeGroupResponseCode") == 200) {
			logDebug("volumeGroupId exists in AAI", isDebugLogEnabled)	
		}
		def xml = execution.getVariable("CVGN_queryVolumeGroupResponse")
		def actualVolumeGroupName = ""
		if (utils.nodeExists(xml, "volume-group-name")) {
			actualVolumeGroupName = utils.getNodeText(xml, "volume-group-name")
		}
		execution.setVariable("CVGN_volumeGroupNameMatches", false)
		def volumeGroupName = execution.getVariable("CVGN_volumeGroupName")
				
		if (volumeGroupName.equals(actualVolumeGroupName)) {
			logDebug('Volume Group Name Matches AAI records', isDebugLogEnabled)				
			execution.setVariable("CVGN_volumeGroupNameMatches", true)
		}				
	}
	
	
	// generates a WorkflowException if the A&AI query returns a response code other than 200/404
	public void handleAAIQueryFailure(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		
		logError("Error occurred attempting to query AAI, Response Code " +
			execution.getVariable("CVGN_queryVolumeGroupResponseCode") + ", Error Response " +
			execution.getVariable("CVGN_queryVolumeGroupResponse"))
		//String processKey = getProcessKey(execution);
		//WorkflowException exception = new WorkflowException(processKey, 5000,
			//execution.getVariable("CVGN_queryVolumeGroupResponse"))
		//execution.setVariable("WorkflowException", exception)
	}
	
	// generates a WorkflowException if the volume group name does not match AAI record for this volume group
	public void handleVolumeGroupNameNoMatch(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		
		def errorNotAssociated = "Error occurred - volume group id " + execution.getVariable("CVGN_volumeGroupId") +
			" is not associated with  " + execution.getVariable("CVGN_volumeGroupName")
		logError(errorNotAssociated)
		createWorkflowException(execution, 1002, errorNotAssociated)
		//String processKey = getProcessKey(execution);
		//WorkflowException exception = new WorkflowException(processKey, 1002,
		//	errorNotAssociated)
		//execution.setVariable("WorkflowException", exception)
	}
	
	// sends a successful WorkflowResponse
	public void reportSuccess(Execution execution) {
		def isDebugLogEnabled=execution.getVariable("isDebugLogEnabled")
		logDebug("Sending 200 back to the caller", isDebugLogEnabled)
		def responseXML = ""
		execution.setVariable("WorkflowResponse", responseXML)
	}
	
	

}
