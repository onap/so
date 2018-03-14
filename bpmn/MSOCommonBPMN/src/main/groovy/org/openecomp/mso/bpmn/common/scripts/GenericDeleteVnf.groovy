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

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils


/**
 * TODO: Support getting vnf type = vpe
 *
 * This class supports the GenericGetVnf Sub Flow.
 * This Generic sub flow can be used by any flow for accomplishing
 * the goal of getting a Vnf Object (from AAI).  The flow currently
 * supports the querying of 2 types of Vnfs, generic-vnf and vce. The
 * type must be provided by the calling flow and the type should
 * be mapped to the variable GENDV_type. The type should either be
 * "generic-vnf" or "vce".  If the Vnf Id is not provided by the calling
 * flow then this sub flow will execute the query to get the
 * Vnf using the Vnf Name. Therefore, the calling flow must provide
 * either the Vnf Id or Vnf Name.
 *
 * Upon successful completion of this sub flow the
 * GENDV_SuccessIndicator will be true and the query response payload
 * will be set to GENDV_vnf.  An MSOWorkflowException will
 * be thrown upon unsuccessful completion or if an error occurs
 * at any time during this sub flow. Please map variables
 * to the corresponding variable names below.
 *
 * Note - if this sub flow receives a Not Found (404) response
 * from AAI at any time this will be considered an acceptable
 * successful response however the GENDV_FoundIndicator
 * set to false.  This will allow the calling flow to distinguish
 * between the two success scenarios, "Success where Vnf is found"
 * and "Success where Vnf is NOT found".
 *
 *
 * Variable Mapping Below
 *
 * In Mapping Variables:
 *   @param - GENDV_vnfId
 *   @param - GENDV_type
 *   @param (Optional) - GENDV_resourceVersion
 *
 *
 * Out Mapping Variables:
 *   @param - GENDV_SuccessIndicator
 *   @param - GENDV_FoundIndicator
 *   @param - WorkflowException
 *
 *
 */
class GenericDeleteVnf extends AbstractServiceTaskProcessor{

	String Prefix = "GENDV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines if the resource version was provided
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericDeleteVnf PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("GENDV_resourceVersionProvided", true)
		execution.setVariable("GENDV_SuccessIndicator", false)
		execution.setVariable("GENDV_FoundIndicator", false)

		try{
			// Get Variables
			String vnfId = execution.getVariable("GENDV_vnfId")
			String type = execution.getVariable("GENDV_type")

			if(isBlank(type) || isBlank(vnfId)){
				utils.log("ERROR", "Incoming Required Variable is null!", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
			}else{
				utils.log("DEBUG", "Incoming Vnf Id is: " + vnfId, isDebugEnabled)

				String resourceVersion = execution.getVariable("GENDV_resourceVersion")
				if(isBlank(resourceVersion)){
					utils.log("DEBUG", "Vnf Resource Version is NOT Provided", isDebugEnabled)
					execution.setVariable("GENDV_resourceVersionProvided", false)
				}else{
					utils.log("DEBUG", "Incoming Vnf Resource Version is: " + resourceVersion, isDebugEnabled)
				}
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericDeleteVnf PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericDeleteVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED GenericDeleteVnf PreProcessRequest Process ***", isDebugEnabled)
	}

	/**
	 * This method executes a GET call to AAI for the Vnf
	 * so that the Vnf's resource-version can be
	 * obtained.
	 *
	 * @param - execution
	 */
	public void getVnfResourceVersion(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericDeleteVnf GetVnfResourceVersion Process*** ", isDebugEnabled)
		try {
			String vnfId = execution.getVariable("GENDV_vnfId")
			String type = execution.getVariable("GENDV_type")
			utils.log("DEBUG", "Type of Vnf Getting is: " + type, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else{
				utils.log("DEBUG", "Invalid Incoming GENDV_type", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENDV_type")
			}

			String getVnfPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8")

			execution.setVariable("GENDV_getVnfPath", getVnfPath)
			utils.logAudit("Get Vnf Resource Version Url is: " + getVnfPath)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENDV_getVnfResponseCode", responseCode)
			utils.log("DEBUG", "  GET Vnf response code is: " + responseCode, isDebugEnabled)

			utils.logAudit("GenericDeleteVnf Get VNF Response Code: " + responseCode)
			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENDV_getVnfResponse", aaiResponse)

			utils.logAudit("GenericDeleteVnf Get VNF Response: " + aaiResponse)
			//Process Response
			if(responseCode == 200 || responseCode == 202){
				utils.log("DEBUG", "GET Vnf Received a Good Response: \n" + aaiResponse, isDebugEnabled)
				execution.setVariable("GENDV_FoundIndicator", true)
				if(utils.nodeExists(aaiResponse, "resource-version")){
					String resourceVersion = utils.getNodeText1(aaiResponse, "resource-version")
					execution.setVariable("GENDV_resourceVersion", resourceVersion)
					utils.log("DEBUG", "SI Resource Version is: " + resourceVersion, isDebugEnabled)
				}else{
					utils.log("DEBUG", "GET Vnf for Resource Version Response Does NOT Contain a resource-version", isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Unable to obtain Vnf resource-version. GET Vnf Response Does NOT Contain a resource-version")
				}

			}else if(responseCode == 404){
				utils.log("DEBUG", "GET Vnf Received a Not Found (404) Response", isDebugEnabled)
				execution.setVariable("GENDV_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") // for junits
			}
			else{
				utils.log("DEBUG", "  GET Vnf Received a Bad Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GenericDeleteVnf GetVnfResourceVersion method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetVnfResourceVersion")
		}
		utils.log("DEBUG", " *** COMPLETED GenericDeleteVnf GetVnfResourceVersion Process*** ", isDebugEnabled)
	}

	/**
	 * This method executes a DELETE call to AAI for the provided
	 * Vnf.
	 *
	 * @param - execution
	 */
	public void deleteVnf(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericDeleteVnf DeleteVnf Process*** ", isDebugEnabled)
		try {
			String vnfId = execution.getVariable("GENDV_vnfId")
			String type = execution.getVariable("GENDV_type")
			utils.log("DEBUG", "Type of Vnf Getting is: " + type, isDebugEnabled)
			String resourceVersion = execution.getVariable("GENDV_resourceVersion")
			utils.log("DEBUG", "Incoming Vnf Resource Version is: " + resourceVersion, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else{
				utils.log("DEBUG", "Invalid Incoming GENDV_type", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENDV_type")
			}

			String deleteVnfPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") +'?resource-version=' + UriUtils.encode(resourceVersion,"UTF-8")

			execution.setVariable("GENDV_deleteVnfPath", deleteVnfPath)
			utils.logAudit("Delete Vnf Url is: " + deleteVnfPath)

			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, deleteVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENDV_deleteVnfResponseCode", responseCode)
			utils.log("DEBUG", "  DELETE Vnf response code is: " + responseCode, isDebugEnabled)

			utils.logAudit("GenericDeleteVnf Delete VNF Response Code: " + responseCode)
			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENDV_deleteVnfResponse", aaiResponse)

			utils.logAudit("GenericDeleteVnf Delete VNF Response: " + aaiResponse)
			//Process Response
			if(responseCode == 204){
				utils.log("DEBUG", "  DELETE Vnf Received a Good Response", isDebugEnabled)
				execution.setVariable("GENDV_FoundIndicator", true)
			}else if(responseCode == 404){
				utils.log("DEBUG", "  DELETE Vnf Received a Not Found (404) Response", isDebugEnabled)
			}else if(responseCode == 412){
				utils.log("DEBUG", "DELETE Vnf Received a Resource Version Mismatch Error: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 412, "Delete Vnf Received a resource-version Mismatch Error Response from AAI")
			}else{
				utils.log("DEBUG", "DELETE Vnf Received a BAD REST Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GenericDeleteVnf DeleteVnf method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Delete Vnf")
		}
		utils.log("DEBUG", " *** COMPLETED GenericDeleteVnf DeleteVnf Process*** ", isDebugEnabled)
	}


}
