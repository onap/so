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
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.rest.APIResponse
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

public class ConfirmVolumeGroupName extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, ConfirmVolumeGroupName.class);
	
	def Prefix="CVGN_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	public void initProcessVariables(DelegateExecution execution) {
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
		execution.setVariable("RollbackData", null)
	}

	// store the incoming data in the flow DelegateExecution
	public void preProcessRequest(DelegateExecution execution) {
		def volumeGroupId = execution.getVariable("ConfirmVolumeGroupName_volumeGroupId")
		def volumeGroupName= execution.getVariable("ConfirmVolumeGroupName_volumeGroupName")
		def aicCloudRegion = execution.getVariable("ConfirmVolumeGroupName_aicCloudRegion")
		
		initProcessVariables(execution)
		execution.setVariable("CVGN_volumeGroupId", volumeGroupId)
		execution.setVariable("CVGN_volumeGroupName", volumeGroupName)
		execution.setVariable("CVGN_aicCloudRegion", aicCloudRegion)
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		def aai_uri = aaiUriUtil.getCloudInfrastructureCloudRegionUri(execution)
		msoLogger.debug('AAI URI is: ' + aai_uri)
		msoLogger.debug("AAI URI: " + aai_uri)
		execution.setVariable("CVGN_volumeGroupGetEndpoint","${aai_uri}/${aicCloudRegion}/volume-groups/volume-group/" +
				volumeGroupId)		
	}
	
	// send a GET request to AA&I to retrieve the Volume information based on volume-group-id
	// expect a 200 response with the information in the response body or a 404 if the volume group id does not exist
	public void queryAAIForVolumeGroupId(DelegateExecution execution) {
		def endPoint = UrnPropertiesReader.getVariable("aai.endpoint", execution) + execution.getVariable("CVGN_volumeGroupGetEndpoint")
		
		try {
			msoLogger.debug("invoking GET call to AAI endpoint :"+System.lineSeparator()+endPoint)
			msoLogger.debug("queryAAIForVolumeGroupId() endpoint-" + endPoint)
			msoLogger.debug("ConfirmVolumeGroup sending GET call to AAI Endpoint: " + endPoint)

			AaiUtil aaiUtil = new AaiUtil(this)
			APIResponse response = aaiUtil.executeAAIGetCall(execution, endPoint)
			def responseData = response.getResponseBodyAsString()
			def responseStatusCode = response.getStatusCode()
			execution.setVariable("CVGN_queryVolumeGroupResponseCode", responseStatusCode)
			execution.setVariable("CVGN_queryVolumeGroupResponse", responseData)

			msoLogger.debug("Response code:" + responseStatusCode)
			msoLogger.debug("Response:" + responseData)
			msoLogger.debug("Response code:" + responseStatusCode)
			msoLogger.debug("Response:" + System.lineSeparator()+responseData)
		} catch (Exception ex) {
	//		ex.printStackTrace()
			msoLogger.debug("Exception occurred while executing AAI GET:" + ex.getMessage())
			execution.setVariable("CVGN_queryVolumeGroupResponseCode", 500)
			execution.setVariable("CVGN_queryVolumeGroupResponse", "AAI GET Failed:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "AAI GET Failed")
		}
	}

	// process the result from queryAAIVolumeGroupId()

	public void checkAAIQueryResult(DelegateExecution execution) {
		def result = execution.getVariable("CVGN_queryVolumeGroupResponse")

		if (execution.getVariable("CVGN_queryVolumeGroupResponseCode") == 404) {
			msoLogger.debug('volumeGroupId does not exist in AAI')
		}
		else if (execution.getVariable("CVGN_queryVolumeGroupResponseCode") == 200) {
			msoLogger.debug("volumeGroupId exists in AAI")
		}
		def xml = execution.getVariable("CVGN_queryVolumeGroupResponse")
		def actualVolumeGroupName = ""
		if (utils.nodeExists(xml, "volume-group-name")) {
			actualVolumeGroupName = utils.getNodeText(xml, "volume-group-name")
		}
		execution.setVariable("CVGN_volumeGroupNameMatches", false)
		def volumeGroupName = execution.getVariable("CVGN_volumeGroupName")

		if (volumeGroupName.equals(actualVolumeGroupName)) {
			msoLogger.debug('Volume Group Name Matches AAI records')
			execution.setVariable("CVGN_volumeGroupNameMatches", true)
		}
	}


	// generates a WorkflowException if the A&AI query returns a response code other than 200/404
	public void handleAAIQueryFailure(DelegateExecution execution) {
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Error occurred attempting to query AAI, Response Code " + execution.getVariable("CVGN_queryVolumeGroupResponseCode"), "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "ErrorResponse is:\n" + execution.getVariable("CVGN_queryVolumeGroupResponse"));
	}

	// generates a WorkflowException if the volume group name does not match AAI record for this volume group
	public void handleVolumeGroupNameNoMatch(DelegateExecution execution) {
		def errorNotAssociated = "Error occurred - volume group id " + execution.getVariable("CVGN_volumeGroupId") +
			" is not associated with  " + execution.getVariable("CVGN_volumeGroupName")
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, errorNotAssociated, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
		exceptionUtil.buildAndThrowWorkflowException(execution, 1002, errorNotAssociated)
	}

}