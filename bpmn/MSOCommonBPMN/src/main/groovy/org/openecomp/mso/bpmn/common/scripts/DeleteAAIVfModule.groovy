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
import org.camunda.bpm.engine.runtime.Execution
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig


public class DeleteAAIVfModule extends AbstractServiceTaskProcessor{
	
	def Prefix="DAAIVfMod_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
    private MsoUtils utils = new MsoUtils()
	public void initProcessVariables(Execution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("DAAIVfMod_vnfId",null)
		execution.setVariable("DAAIVfMod_vnfName",null)
		execution.setVariable("DAAIVfMod_genVnfRsrcVer",null)
		execution.setVariable("DAAIVfMod_vfModuleId",null)
		execution.setVariable("DAAIVfMod_vfModRsrcVer",null)
		execution.setVariable("DAAIVfMod_genericVnfEndpoint",null)
		execution.setVariable("DAAIVfMod_vfModuleEndpoint",null)
		execution.setVariable("DAAIVfMod_moduleExists",false)
		execution.setVariable("DAAIVfMod_isBaseModule", false)
		execution.setVariable("DAAIVfMod_isLastModule", false)

		// DeleteAAIVfModule workflow response variable placeholders
		execution.setVariable("DAAIVfMod_queryGenericVnfResponseCode",null)
		execution.setVariable("DAAIVfMod_queryGenericVnfResponse","")
		execution.setVariable("DAAIVfMod_parseModuleResponse","")
		execution.setVariable("DAAIVfMod_deleteGenericVnfResponseCode",null)
		execution.setVariable("DAAIVfMod_deleteGenericVnfResponse","")
		execution.setVariable("DAAIVfMod_deleteVfModuleResponseCode",null)
		execution.setVariable("DAAIVfMod_deleteVfModuleResponse","")

	}
	
	// parse the incoming DELETE_VF_MODULE request and store the Generic Vnf
	// and Vf Module Ids in the flow Execution
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("DeleteAAIVfModuleRequest")
		utils.logAudit("DeleteAAIVfModule Request: " + xml)
		utils.log("DEBUG", "input request xml:" + xml, isDebugEnabled)
		initProcessVariables(execution)
		def vnfId = utils.getNodeText(xml,"vnf-id")
		def vfModuleId = utils.getNodeText(xml,"vf-module-id")
		execution.setVariable("DAAIVfMod_vnfId", vnfId)
		execution.setVariable("DAAIVfMod_vfModuleId", vfModuleId)
		
		AaiUtil aaiUriUtil = new AaiUtil(this)
		def aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
		logDebug('AAI URI is: ' + aai_uri, isDebugEnabled)
		
		execution.setVariable("DAAIVfMod_genericVnfEndpoint", "${aai_uri}/" + vnfId)
		execution.setVariable("DAAIVfMod_vfModuleEndpoint", "${aai_uri}/" + vnfId +
			 "/vf-modules/vf-module/" + vfModuleId)
	}
	
	// send a GET request to AA&I to retrieve the Generic Vnf/Vf Module information based on a Vnf Id
	// expect a 200 response with the information in the response body or a 404 if the Generic Vnf does not exist
	public void queryAAIForGenericVnf(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def delModuleId = execution.getVariable("DAAIVfMod_vfModuleId")
		def endPoint = execution.getVariable("URN_aai_endpoint") + execution.getVariable("DAAIVfMod_genericVnfEndpoint") + "?depth=1"
		utils.logAudit("DeleteAAIVfModule endPoint: " + endPoint)
		def aaiRequestId = utils.getRequestID()

		RESTConfig config = new RESTConfig(endPoint)
		utils.log("DEBUG","queryAAIForGenericVnf() endpoint-" + endPoint, isDebugEnabled)
		def responseData = ""
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").
				addHeader("Accept","application/xml")
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
				
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			utils.log("DEBUG", "invoking GET call to AAI endpoint :"+System.lineSeparator()+endPoint,isDebugEnabled)
			APIResponse response = client.httpGet()
			utils.logAudit("DeleteAAIVfModule - invoking httpGet to AAI")

			responseData = response.getResponseBodyAsString()
			execution.setVariable("DAAIVfMod_queryGenericVnfResponseCode", response.getStatusCode())
			execution.setVariable("DAAIVfMod_queryGenericVnfResponse", responseData)
			utils.logAudit("AAI Response: " + responseData)
			utils.log("DEBUG", "Response code:" + response.getStatusCode(), isDebugEnabled)
			utils.log("DEBUG", "Response:" + System.lineSeparator()+responseData,isDebugEnabled)
		} catch (Exception ex) {
			utils.log("DEBUG", "Exception occurred while executing AAI GET:" + ex.getMessage(),isDebugEnabled)
			execution.setVariable("DAAIVfMod_queryGenericVnfResponse", "AAI GET Failed:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during queryAAIForGenericVnf")

		}
	}
	
	// construct and send a DELETE request to A&AI to delete a Generic Vnf
	// note: to get here, all the modules associated with the Generic Vnf must already be deleted
	public void deleteGenericVnf(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def aaiRequestId = utils.getRequestID()
		def endPoint = execution.getVariable("URN_aai_endpoint") + execution.getVariable("DAAIVfMod_genericVnfEndpoint") +
			"/?resource-version=" + execution.getVariable("DAAIVfMod_genVnfRsrcVer")
		utils.logAudit("AAI endPoint: " + endPoint)
		RESTConfig config = new RESTConfig(endPoint)
		utils.log("DEBUG","deleteGenericVnf() endpoint-" + endPoint, isDebugEnabled)
		def responseData = ""
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").
				addHeader("Accept","application/xml")
			
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
					
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse response = client.httpDelete()
				
			responseData = response.getResponseBodyAsString()
			execution.setVariable("DAAIVfMod_deleteGenericVnfResponseCode", response.getStatusCode())
			execution.setVariable("DAAIVfMod_deleteGenericVnfResponse", responseData)
			utils.log("DEBUG", "Response code:" + response.getStatusCode(), isDebugEnabled)
			utils.log("DEBUG", "Response:" + System.lineSeparator()+responseData,isDebugEnabled)
		} catch (Exception ex) {
			ex.printStackTrace()
			utils.log("DEBUG", "Exception occurred while executing AAI DELETE:" + ex.getMessage(),isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during deleteGenericVnf")
		}
	}

	// construct and send a DELETE request to A&AI to delete the Base or Add-on Vf Module
	public void deleteVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def endPoint = execution.getVariable("URN_aai_endpoint") + execution.getVariable("DAAIVfMod_vfModuleEndpoint") +
			"/?resource-version=" + execution.getVariable("DAAIVfMod_vfModRsrcVer")
		def aaiRequestId = utils.getRequestID()

		RESTConfig config = new RESTConfig(endPoint)
		utils.log("DEBUG","deleteVfModule() endpoint-" + endPoint, isDebugEnabled)
		def responseData = ""
		try {
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", aaiRequestId).addHeader("X-FromAppId", "MSO").
				addHeader("Accept","application/xml")
			
			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
					
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse response = client.httpDelete()
			
			utils.logAudit("DeleteAAIVfModule - invoking httpDelete to AAI")
			
			responseData = response.getResponseBodyAsString()
			execution.setVariable("DAAIVfMod_deleteVfModuleResponseCode", response.getStatusCode())
			execution.setVariable("DAAIVfMod_deleteVfModuleResponse", responseData)
			utils.logAudit("DeleteAAIVfModule - AAI Response" + responseData)
			utils.log("DEBUG", "Response code:" + response.getStatusCode(), isDebugEnabled)
			utils.log("DEBUG", "Response:" + System.lineSeparator()+responseData,isDebugEnabled)

		} catch (Exception ex) {
			ex.printStackTrace()
			utils.log("DEBUG", "Exception occurred while executing AAI PUT:" + ex.getMessage(),isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during deleteVfModule")
		}
	}
	
	// parses the output from the result from queryAAIForGenericVnf() to determine if the Vf Module
	// to be deleted exists for the specified Generic Vnf and if it is the Base Module,
	// there are no Add-on Modules present
	public void parseForVfModule(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("DAAIVfMod_queryGenericVnfResponse")
		utils.logAudit("DeleteAAIVfModule - queryGenericVnfResponse" + xml)
		
		def delModuleId = execution.getVariable("DAAIVfMod_vfModuleId")
		utils.log("DEBUG", "Vf Module to be deleted: " + delModuleId, isDebugEnabled)
		List <String> qryModuleIdList = utils.getMultNodes(xml, "vf-module-id")
		List <String> qryBaseModuleList = utils.getMultNodes(xml, "is-base-vf-module")
		List <String> qryResourceVerList = utils.getMultNodes(xml, "resource-version")
		execution.setVariable("DAAIVfMod_moduleExists", false)
		execution.setVariable("DAAIVfMod_isBaseModule", false)
		execution.setVariable("DAAIVfMod_isLastModule", false)
		//
		def isBaseVfModule = "false"
		// loop through the Vf Module Ids looking for a match
		if (qryModuleIdList != null && !qryModuleIdList.empty) {
			utils.log("DEBUG", "Existing Vf Module Id List: " + qryModuleIdList, isDebugEnabled)
			utils.log("DEBUG", "Existing Vf Module Resource Version List: " + qryResourceVerList, isDebugEnabled)
			def moduleCntr = 0
			// the Generic Vnf resource-version in the 1st entry in the query response
			execution.setVariable("DAAIVfMod_genVnfRsrcVer", qryResourceVerList[moduleCntr])
			for (String qryModuleId : qryModuleIdList) {
				if (delModuleId.equals(qryModuleId)) {
					// a Vf Module with the requested Id exists
					execution.setVariable("DAAIVfMod_moduleExists", true)
					// find the corresponding value for the is-base-vf-module field
					isBaseVfModule = qryBaseModuleList[moduleCntr]
					// find the corresponding value for the resource-version field
					// note: the Generic Vnf entry also has a resource-version field, so
					//       add 1 to the index to get the corresponding Vf Module value
					execution.setVariable("DAAIVfMod_vfModRsrcVer", qryResourceVerList[moduleCntr+1])
					utils.log("DEBUG", "Match found for Vf Module Id " + qryModuleId + " for Generic Vnf Id " +
						execution.getVariable("DAAIVfMod_vnfId") + ", Base Module is " + isBaseVfModule +
						", Resource Version is " + execution.getVariable("vfModRsrcVer"), isDebugEnabled)
					break
				}
				moduleCntr++
			}
		}
		
		// determine if the module to be deleted is a Base Module and/or the Last Module
		if (execution.getVariable("DAAIVfMod_moduleExists") == true) {
			if (isBaseVfModule.equals("true") && qryModuleIdList.size() != 1) {
				execution.setVariable("DAAIVfMod_parseModuleResponse",
					"Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
					execution.getVariable("DAAIVfMod_vnfId") + ": is Base Module, not Last Module")
				execution.setVariable("DAAIVfMod_isBaseModule", true)
			} else {
				if (isBaseVfModule.equals("true") && qryModuleIdList.size() == 1) {
					execution.setVariable("DAAIVfMod_parseModuleResponse",
						"Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
						execution.getVariable("DAAIVfMod_vnfId") + ": is Base Module and Last Module")
					execution.setVariable("DAAIVfMod_isBaseModule", true)
					execution.setVariable("DAAIVfMod_isLastModule", true)
				} else {
					if (qryModuleIdList.size() == 1) {
						execution.setVariable("DAAIVfMod_parseModuleResponse",
							"Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
							execution.getVariable("DAAIVfMod_vnfId") + ": is Not Base Module, is Last Module")
						execution.setVariable("DAAIVfMod_isLastModule", true)
					} else {
					execution.setVariable("DAAIVfMod_parseModuleResponse",
						"Found Vf Module Id " + delModuleId + " for Generic Vnf Id " +
						execution.getVariable("DAAIVfMod_vnfId") + ": is Not Base Module and Not Last Module")
					}
				}
			}
			utils.log("DEBUG", execution.getVariable("DAAIVfMod_parseModuleResponse"), isDebugEnabled)
		} else { // (execution.getVariable("DAAIVfMod_moduleExists") == false)
			utils.log("DEBUG", "Vf Module Id " + delModuleId + " does not exist for Generic Vnf Id " +
				 execution.getVariable("DAAIVfMod_vnfId"), isDebugEnabled)
			execution.setVariable("DAAIVfMod_parseModuleResponse",
				"Vf Module Id " + delModuleId + " does not exist for Generic Vnf Id " +
				execution.getVariable("DAAIVfMod_vnfName"))
		}
	}
	
	// parses the output from the result from queryAAIForGenericVnf() to determine if the Vf Module
	// to be deleted exists for the specified Generic Vnf and if it is the Base Module,
	// there are no Add-on Modules present
	public void parseForResourceVersion(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		def xml = execution.getVariable("DAAIVfMod_queryGenericVnfResponse")
		utils.logAudit("DeleteAAIVfModule - queryGenericVnfResponse" + xml)
		String resourceVer = utils.getNodeText1(xml, "resource-version")
		execution.setVariable("DAAIVfMod_genVnfRsrcVer", resourceVer)
		utils.log("DEBUG", "Latest Generic VNF Resource Version: " + resourceVer)
	}
	
	
	// generates a WorkflowException if the A&AI query returns a response code other than 200
	public void handleAAIQueryFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		
		utils.log("ERROR", "Error occurred attempting to query AAI, Response Code " +
			execution.getVariable("DAAIVfMod_queryGenericVnfResponseCode") + ", Error Response " +
			execution.getVariable("DAAIVfMod_queryGenericVnfResponse"), isDebugEnabled)
		def errorCode = 5000
		// set the errorCode to distinguish between a A&AI failure
		// and the Generic Vnf Id not found
		if (execution.getVariable("DAAIVfMod_queryGenericVnfResponseCode") == 404) {
			errorCode = 1002
		}
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode, execution.getVariable("DAAIVfMod_queryGenericVnfResponse"))
	}
	
	// generates a WorkflowException if
	//		- the A&AI Vf Module DELETE returns a response code other than 200
	// 		- the Vf Module is a Base Module that is not the last Vf Module
	//		- the Vf Module does not exist for the Generic Vnf
	public void handleDeleteVfModuleFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		
		def errorCode = 2000
		def errorResponse = ""
		if (execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode") != null &&
			execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode") != 200) {
			utils.log("DEBUG", "AAI failure deleting a Vf Module: " +
				execution.getVariable("DAAIVfMod_deleteVfModuleResponse"), isDebugEnabled)
			errorResponse = execution.getVariable("DAAIVfMod_deleteVfModuleResponse")
			utils.logAudit("DeleteAAIVfModule - deleteVfModuleResponse" + errorResponse)
			errorCode = 5000
		} else {
			if (execution.getVariable("DAAIVfMod_isBaseModule", true) == true &&
					execution.getVariable("DAAIVfMod_isLastModule") == false) {
				// attempt to delete a Base Module that is not the last Vf Module
				utils.log("DEBUG", execution.getVariable("DAAIVfMod_parseModuleResponse"), isDebugEnabled)
				errorResponse = execution.getVariable("DAAIVfMod_parseModuleResponse")
				utils.logAudit("DeleteAAIVfModule - parseModuleResponse" + errorResponse)
				errorCode = 1002
			} else {
				// attempt to delete a non-existant Vf Module
				if (execution.getVariable("DAAIVfMod_moduleExists") == false) {
					utils.log("DEBUG", execution.getVariable("DAAIVfMod_parseModuleResponse"), isDebugEnabled)
					errorResponse = execution.getVariable("DAAIVfMod_parseModuleResponse")
					utils.logAudit("DeleteAAIVfModule - parseModuleResponse" + errorResponse)
					errorCode = 1002
				} else {
					// if the responses get populated corerctly, we should never get here
					errorResponse = "Unknown error occurred during DeleteAAIVfModule flow"
				}
			}
		}

		utils.log("ERROR", "Error occurred during DeleteAAIVfModule flow: " + errorResponse, isDebugEnabled)
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode, errorResponse)

	}

	// generates a WorkflowException if
	//		- the A&AI Generic Vnf DELETE returns a response code other than 200
	public void handleDeleteGenericVnfFailure(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("ERROR", "AAI error occurred deleting the Generic Vnf: "
			+ execution.getVariable("DAAIVfMod_deleteGenericVnfResponse"), isDebugEnabled)
		exceptionUtil.buildAndThrowWorkflowException(execution, 5000, execution.getVariable("DAAIVfMod_deleteGenericVnfResponse"))
	}
}