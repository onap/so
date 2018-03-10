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

import static org.openecomp.mso.bpmn.common.scripts.GenericUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
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
 * be mapped to the variable GENGV_type. The type should either be
 * "generic-vnf" or "vce".  If the Vnf Id is not provided by the calling
 * flow then this sub flow will execute the query to get the
 * Vnf using the Vnf Name. Therefore, the calling flow must provide
 * either the Vnf Id or Vnf Name.
 *
 * Upon successful completion of this sub flow the
 * GENGV_SuccessIndicator will be true and the query response payload
 * will be set to GENGV_vnf.  An MSOWorkflowException will
 * be thrown upon unsuccessful completion or if an error occurs
 * at any time during this sub flow. Please map variables
 * to the corresponding variable names below.
 *
 * Note - if this sub flow receives a Not Found (404) response
 * from AAI at any time this will be considered an acceptable
 * successful response however the GENGV_FoundIndicator
 * set to false.  This will allow the calling flow to distinguish
 * between the two success scenarios, "Success where Vnf is found"
 * and "Success where Vnf is NOT found".
 *
 *
 * Variable Mapping Below
 *
 * In Mapping Variables:
 *   @param - GENGV_vnfId  or  @param - GENGV_vnfName
 *   @param - GENGV_type
 *
 * Out Mapping Variables:
 *   @param - GENGV_vnf
 *   @param - GENGV_SuccessIndicator
 *   @param - GENGV_FoundIndicator
 *   @param - WorkflowException
 *
 *
 */
class GenericGetVnf extends AbstractServiceTaskProcessor{

	String Prefix = "GENGV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines the subsequent event based on which
	 * variables the calling flow provided.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetVnf PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("GENGV_getVnfByName", false)
		execution.setVariable("GENGV_SuccessIndicator", false)
		execution.setVariable("GENGV_FoundIndicator", false)

		try{
			// Get Variables
			String vnfId = execution.getVariable("GENGV_vnfId")
			utils.log("DEBUG", "Incoming Vnf Id is: " + vnfId, isDebugEnabled)
			String vnfName = execution.getVariable("GENGV_vnfName")
			utils.log("DEBUG", "Incoming Vnf Name is: " + vnfName, isDebugEnabled)

			if(isBlank(vnfId) && isBlank(vnfName)){
				utils.log("DEBUG", "Incoming Vnf Name and Vnf Id are null. At least one is required!", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Vnf Name and Vnf Id are null. At least one is required.")
			}else{
				if(isBlank(vnfId)){
					execution.setVariable("GENGV_getVnfByName", true)
				}
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error encountered within GenericGetVnf PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericGetVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED GenericGetVnf PreProcessRequest Process ***", isDebugEnabled)
	}

	/**
	 * This method executes a GET call to AAI to obtain the
	 * Vnf using the Vnf Name
	 *
	 * @param - execution
	 */
	public void getVnfByName(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetVnf GetVnfByName Process*** ", isDebugEnabled)
		try {
			String vnfName = execution.getVariable("GENGV_vnfName")
			utils.log("DEBUG", "Getting Vnf by Vnf Name: " + vnfName, isDebugEnabled)
			String type = execution.getVariable("GENGV_type")
			utils.log("DEBUG", "Type of Vnf Getting is: " + type, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For.
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else{
				utils.log("DEBUG", "Invalid Incoming GENGV_type", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENGV_type")
			}

			String getVnfPath = "${aai_endpoint}${aai_uri}?vnf-name=" + UriUtils.encode(vnfName, "UTF-8") + "&depth=1"

			execution.setVariable("GENGV_getVnfPath", getVnfPath)
			utils.logAudit("Get Vnf Url is: " + getVnfPath)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGV_getVnfResponseCode", responseCode)
			utils.log("DEBUG", "  GET Vnf response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGV_getVnfResponse", aaiResponse)

			//Process Response
			if(responseCode == 200){
				utils.log("DEBUG", "GET Vnf Received a Good Response", isDebugEnabled)
					if(utils.nodeExists(aaiResponse, type)){
						utils.log("DEBUG", "GET Vnf Response Contains a Vnf", isDebugEnabled)
						execution.setVariable("GENGV_FoundIndicator", true)
						execution.setVariable("GENGV_vnf", aaiResponse)
						execution.setVariable("WorkflowResponse", aaiResponse)
					}else{
						utils.log("DEBUG", "GET Vnf Response Does NOT Contain a Vnf", isDebugEnabled)
					}

			}else if(responseCode == 404){
				utils.log("DEBUG", "GET Vnf Received a Not Found (404) Response", isDebugEnabled)
			}else{
				utils.log("DEBUG", "GET Vnf Received a Bad Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericGetVnf GetVnfByName method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetVnfByName")
		}
		utils.log("DEBUG", " *** COMPLETED GenericGetVnf GetVnfByName Process*** ", isDebugEnabled)
	}

	/**
	 * This method executes a GET call to AAI to obtain the
	 * Vnf using the Vnf Id
	 *
	 * @param - execution
	 */
	public void getVnfById(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericGetVnf GetVnfById Process*** ", isDebugEnabled)
		try {
			String vnfId = execution.getVariable("GENGV_vnfId")
			utils.log("DEBUG", "Getting Vnf by Vnf Id: " + vnfId, isDebugEnabled)
			String type = execution.getVariable("GENGV_type")
			utils.log("DEBUG", "Type of Vnf Getting is: " + type, isDebugEnabled)

			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For.
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else if(type.equals("vpe")){
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "GenericGetVnf does not yet support getting type of vnf = vpe")
			}else{
				utils.log("DEBUG", "Invalid Incoming GENGV_type", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENGV_type")
			}
			utils.log("DEBUG", "Using AAI Uri: " + aai_uri, isDebugEnabled)

			String getVnfPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			utils.log("DEBUG", "GET Vnf Endpoint is: " + getVnfPath, isDebugEnabled)

			execution.setVariable("GENGV_getVnfPath", getVnfPath)
			utils.logAudit("Get Vnf Url is: " + getVnfPath)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGV_getVnfResponseCode", responseCode)
			utils.log("DEBUG", "  GET Vnf response code is: " + responseCode, isDebugEnabled)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGV_getVnfResponse", aaiResponse)

			//Process Response
			if(responseCode == 200){
				utils.log("DEBUG", "GET Vnf Received a Good Response", isDebugEnabled)
					if(utils.nodeExists(aaiResponse, type)){
						utils.log("DEBUG", "GET Vnf Response Contains a Vnf", isDebugEnabled)
						execution.setVariable("GENGV_FoundIndicator", true)
						execution.setVariable("GENGV_vnf", aaiResponse)
						execution.setVariable("WorkflowResponse", aaiResponse)
					}else{
						utils.log("DEBUG", "GET Vnf Response Does NOT Contain a Vnf", isDebugEnabled)
					}

			}else if(responseCode == 404){
				utils.log("DEBUG", "GET Vnf Received a Not Found (404) Response", isDebugEnabled)
			}else{
				utils.log("DEBUG", "GET Vnf Received a BAD REST Response: \n" + aaiResponse, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericGetVnf GetVnfById method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetVnfById")
		}
		utils.log("DEBUG", " *** COMPLETED GenericGetVnf GetVnfById Process*** ", isDebugEnabled)
	}

}
