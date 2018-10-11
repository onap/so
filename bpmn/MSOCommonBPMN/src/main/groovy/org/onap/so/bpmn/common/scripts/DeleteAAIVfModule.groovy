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
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.client.graphinventory.entities.uri.Depth
import org.onap.so.rest.APIResponse
import org.onap.so.rest.RESTClient;
import org.onap.so.rest.RESTConfig;
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger

public class DeleteAAIVfModule extends AbstractServiceTaskProcessor{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DeleteAAIVfModule.class);

	def Prefix="DAAIVfMod_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
    private MsoUtils utils = new MsoUtils()
	public void initProcessVariables(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		execution.setVariable("DAAIVfMod_vnfId",null)
		execution.setVariable("DAAIVfMod_vnfName",null)
		execution.setVariable("DAAIVfMod_genVnfRsrcVer",null)
		execution.setVariable("DAAIVfMod_vfModuleId",null)
		execution.setVariable("DAAIVfMod_vfModRsrcVer",null)
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
	// and Vf Module Ids in the flow DelegateExecution
	public void preProcessRequest(DelegateExecution execution) {
		def xml = execution.getVariable("DeleteAAIVfModuleRequest")
		msoLogger.debug("DeleteAAIVfModule Request: " + xml)
		msoLogger.debug("input request xml:" + xml)
		initProcessVariables(execution)
		def vnfId = utils.getNodeText(xml,"vnf-id")
		def vfModuleId = utils.getNodeText(xml,"vf-module-id")
		execution.setVariable("DAAIVfMod_vnfId", vnfId)
		execution.setVariable("DAAIVfMod_vfModuleId", vfModuleId)
	}
	
	// send a GET request to AA&I to retrieve the Generic Vnf/Vf Module information based on a Vnf Id
	// expect a 200 response with the information in the response body or a 404 if the Generic Vnf does not exist
	public void queryAAIForGenericVnf(DelegateExecution execution) {
		
		def vnfId = execution.getVariable("DAAIVfMod_vnfId")

		try {
			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
			uri.depth(Depth.ONE)
			String endPoint = aaiUriUtil.createAaiUri(uri)
			
			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, endPoint)
			
			msoLogger.debug('Response code:' + response.getStatusCode())
			msoLogger.debug('Response:' + response.getResponseBodyAsString())

			execution.setVariable("DAAIVfMod_queryGenericVnfResponseCode", response.getStatusCode())
			execution.setVariable("DAAIVfMod_queryGenericVnfResponse", response.getResponseBodyAsString())

		} catch (Exception ex) {
			msoLogger.debug("Exception occurred while executing AAI GET:" + ex.getMessage())
			execution.setVariable("DAAIVfMod_queryGenericVnfResponse", "AAI GET Failed:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during queryAAIForGenericVnf")
		}
	}
	
	// construct and send a DELETE request to A&AI to delete a Generic Vnf
	// note: to get here, all the modules associated with the Generic Vnf must already be deleted
	public void deleteGenericVnf(DelegateExecution execution) {

		try {
			String vnfId = execution.getVariable("DAAIVfMod_vnfId")
			String resourceVersion =  execution.getVariable("DAAIVfMod_genVnfRsrcVer")
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId)
			uri.resourceVersion(resourceVersion)
			String endPoint = aaiUriUtil.createAaiUri(uri)
			
			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, endPoint)
				
			def responseData = response.getResponseBodyAsString()
			execution.setVariable("DAAIVfMod_deleteGenericVnfResponseCode", response.getStatusCode())
			execution.setVariable("DAAIVfMod_deleteGenericVnfResponse", responseData)
			msoLogger.debug("Response code:" + response.getStatusCode())
			msoLogger.debug("Response:" + System.lineSeparator() + responseData)
		} catch (Exception ex) {
			ex.printStackTrace()
			msoLogger.debug("Exception occurred while executing AAI DELETE:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during deleteGenericVnf")
		}
	}

	// construct and send a DELETE request to A&AI to delete the Base or Add-on Vf Module
	public void deleteVfModule(DelegateExecution execution) {
		def responseData = ""
		try {
			String vnfId = execution.getVariable("DAAIVfMod_vnfId")
			String vfModuleId = execution.setVariable("DAAIVfMod_vfModuleId")
			String resourceVersion =  execution.getVariable("DAAIVfMod_vfModRsrcVer")
			
			AaiUtil aaiUriUtil = new AaiUtil(this)
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId)
			uri.resourceVersion(resourceVersion)
			String endPoint = aaiUriUtil.createAaiUri(uri)
			
			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, endPoint)
			
			responseData = response.getResponseBodyAsString()
			execution.setVariable("DAAIVfMod_deleteVfModuleResponseCode", response.getStatusCode())
			execution.setVariable("DAAIVfMod_deleteVfModuleResponse", responseData)
			msoLogger.debug("DeleteAAIVfModule - AAI Response" + responseData)
			msoLogger.debug("Response code:" + response.getStatusCode())
			msoLogger.debug("Response:" + System.lineSeparator() + responseData)

		} catch (Exception ex) {
			ex.printStackTrace()
			msoLogger.debug("Exception occurred while executing AAI PUT:" + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 5000, "Internal Error - Occured during deleteVfModule")
		}
	}
	
	// parses the output from the result from queryAAIForGenericVnf() to determine if the Vf Module
	// to be deleted exists for the specified Generic Vnf and if it is the Base Module,
	// there are no Add-on Modules present
	public void parseForVfModule(DelegateExecution execution) {
		def xml = execution.getVariable("DAAIVfMod_queryGenericVnfResponse")
		msoLogger.debug("DeleteAAIVfModule - queryGenericVnfResponse" + xml)
		
		def delModuleId = execution.getVariable("DAAIVfMod_vfModuleId")
		msoLogger.debug("Vf Module to be deleted: " + delModuleId)
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
			msoLogger.debug("Existing Vf Module Id List: " + qryModuleIdList)
			msoLogger.debug("Existing Vf Module Resource Version List: " + qryResourceVerList)
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
					msoLogger.debug("Match found for Vf Module Id " + qryModuleId + " for Generic Vnf Id " + execution.getVariable("DAAIVfMod_vnfId") + ", Base Module is " + isBaseVfModule + ", Resource Version is " + execution.getVariable("vfModRsrcVer"))
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
			msoLogger.debug(execution.getVariable("DAAIVfMod_parseModuleResponse"))
		} else { // (execution.getVariable("DAAIVfMod_moduleExists") == false)
			msoLogger.debug("Vf Module Id " + delModuleId + " does not exist for Generic Vnf Id " + execution.getVariable("DAAIVfMod_vnfId"))
			execution.setVariable("DAAIVfMod_parseModuleResponse",
				"Vf Module Id " + delModuleId + " does not exist for Generic Vnf Id " +
				execution.getVariable("DAAIVfMod_vnfName"))
		}
	}
	
	// parses the output from the result from queryAAIForGenericVnf() to determine if the Vf Module
	// to be deleted exists for the specified Generic Vnf and if it is the Base Module,
	// there are no Add-on Modules present
	public void parseForResourceVersion(DelegateExecution execution) {
		def xml = execution.getVariable("DAAIVfMod_queryGenericVnfResponse")
		msoLogger.debug("DeleteAAIVfModule - queryGenericVnfResponse" + xml)
		String resourceVer = utils.getNodeText(xml, "resource-version")
		execution.setVariable("DAAIVfMod_genVnfRsrcVer", resourceVer)
		msoLogger.debug("Latest Generic VNF Resource Version: " + resourceVer)
	}
	
	
	// generates a WorkflowException if the A&AI query returns a response code other than 200
	public void handleAAIQueryFailure(DelegateExecution execution) {
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Error occurred attempting to query AAI, Response Code " + execution.getVariable("DAAIVfMod_queryGenericVnfResponseCode") + ", Error Response " + execution.getVariable("DAAIVfMod_queryGenericVnfResponse"), "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
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
	public void handleDeleteVfModuleFailure(DelegateExecution execution) {
		def errorCode = 2000
		def errorResponse = ""
		if (execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode") != null &&
			execution.getVariable("DAAIVfMod_deleteVfModuleResponseCode") != 200) {
			msoLogger.debug("AAI failure deleting a Vf Module: " + execution.getVariable("DAAIVfMod_deleteVfModuleResponse"))
			errorResponse = execution.getVariable("DAAIVfMod_deleteVfModuleResponse")
			msoLogger.debug("DeleteAAIVfModule - deleteVfModuleResponse" + errorResponse)
			errorCode = 5000
		} else {
			if (execution.getVariable("DAAIVfMod_isBaseModule", true) == true &&
					execution.getVariable("DAAIVfMod_isLastModule") == false) {
				// attempt to delete a Base Module that is not the last Vf Module
				msoLogger.debug(execution.getVariable("DAAIVfMod_parseModuleResponse"))
				errorResponse = execution.getVariable("DAAIVfMod_parseModuleResponse")
				msoLogger.debug("DeleteAAIVfModule - parseModuleResponse" + errorResponse)
				errorCode = 1002
			} else {
				// attempt to delete a non-existant Vf Module
				if (execution.getVariable("DAAIVfMod_moduleExists") == false) {
					msoLogger.debug(execution.getVariable("DAAIVfMod_parseModuleResponse"))
					errorResponse = execution.getVariable("DAAIVfMod_parseModuleResponse")
					msoLogger.debug("DeleteAAIVfModule - parseModuleResponse" + errorResponse)
					errorCode = 1002
				} else {
					// if the responses get populated corerctly, we should never get here
					errorResponse = "Unknown error occurred during DeleteAAIVfModule flow"
				}
			}
		}

		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Error occurred during DeleteAAIVfModule flow", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, errorResponse);
		exceptionUtil.buildAndThrowWorkflowException(execution, errorCode, errorResponse)

	}

	// generates a WorkflowException if
	//		- the A&AI Generic Vnf DELETE returns a response code other than 200
	public void handleDeleteGenericVnfFailure(DelegateExecution execution) {
		msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "AAI error occurred deleting the Generic Vnf", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, execution.getVariable("DAAIVfMod_deleteGenericVnfResponse"));
		exceptionUtil.buildAndThrowWorkflowException(execution, 5000, execution.getVariable("DAAIVfMod_deleteGenericVnfResponse"))
	}
}