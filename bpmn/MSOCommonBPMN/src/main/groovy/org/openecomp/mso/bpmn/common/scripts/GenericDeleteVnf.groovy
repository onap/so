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

import org.openecomp.mso.bpmn.core.UrnPropertiesReader

import static org.apache.commons.lang3.StringUtils.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.rest.APIResponse
import org.springframework.web.util.UriUtils
import org.openecomp.mso.logger.MessageEnum
import org.openecomp.mso.logger.MsoLogger




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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, GenericDeleteVnf.class);


	String Prefix = "GENDV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines if the resource version was provided
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericDeleteVnf PreProcessRequest Process")

		execution.setVariable("GENDV_resourceVersionProvided", true)
		execution.setVariable("GENDV_SuccessIndicator", false)
		execution.setVariable("GENDV_FoundIndicator", false)

		try{
			// Get Variables
			String vnfId = execution.getVariable("GENDV_vnfId")
			String type = execution.getVariable("GENDV_type")

			if(isBlank(type) || isBlank(vnfId)){
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Incoming Required Variable is null!", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Required Variable is Missing or Null!")
			}else{
				msoLogger.debug("Incoming Vnf Id is: " + vnfId)

				String resourceVersion = execution.getVariable("GENDV_resourceVersion")
				if(isBlank(resourceVersion)){
					msoLogger.debug("Vnf Resource Version is NOT Provided")
					execution.setVariable("GENDV_resourceVersionProvided", false)
				}else{
					msoLogger.debug("Incoming Vnf Resource Version is: " + resourceVersion)
				}
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericDeleteVnf PreProcessRequest method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericDeleteVnf PreProcessRequest")

		}
		msoLogger.trace("COMPLETED GenericDeleteVnf PreProcessRequest Process ")
	}

	/**
	 * This method executes a GET call to AAI for the Vnf
	 * so that the Vnf's resource-version can be
	 * obtained.
	 *
	 * @param - execution
	 */
	public void getVnfResourceVersion(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericDeleteVnf GetVnfResourceVersion Process")
		try {
			String vnfId = execution.getVariable("GENDV_vnfId")
			String type = execution.getVariable("GENDV_type")
			msoLogger.debug("Type of Vnf Getting is: " + type)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else{
				msoLogger.debug("Invalid Incoming GENDV_type")
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENDV_type")
			}

			String getVnfPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8")

			execution.setVariable("GENDV_getVnfPath", getVnfPath)
			msoLogger.debug("Get Vnf Resource Version Url is: " + getVnfPath)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENDV_getVnfResponseCode", responseCode)
			msoLogger.debug("  GET Vnf response code is: " + responseCode)

			msoLogger.debug("GenericDeleteVnf Get VNF Response Code: " + responseCode)
			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENDV_getVnfResponse", aaiResponse)

			msoLogger.debug("GenericDeleteVnf Get VNF Response: " + aaiResponse)
			//Process Response
			if(responseCode == 200 || responseCode == 202){
				msoLogger.debug("GET Vnf Received a Good Response: \n" + aaiResponse)
				execution.setVariable("GENDV_FoundIndicator", true)
				if(utils.nodeExists(aaiResponse, "resource-version")){
					String resourceVersion = utils.getNodeText1(aaiResponse, "resource-version")
					execution.setVariable("GENDV_resourceVersion", resourceVersion)
					msoLogger.debug("SI Resource Version is: " + resourceVersion)
				}else{
					msoLogger.debug("GET Vnf for Resource Version Response Does NOT Contain a resource-version")
					exceptionUtil.buildAndThrowWorkflowException(execution, 400, "Unable to obtain Vnf resource-version. GET Vnf Response Does NOT Contain a resource-version")
				}

			}else if(responseCode == 404){
				msoLogger.debug("GET Vnf Received a Not Found (404) Response")
				execution.setVariable("GENDV_SuccessIndicator", true)
				execution.setVariable("WorkflowResponse", "  ") // for junits
			}
			else{
				msoLogger.debug("  GET Vnf Received a Bad Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error encountered within GenericDeleteVnf GetVnfResourceVersion method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetVnfResourceVersion")
		}
		msoLogger.trace("COMPLETED GenericDeleteVnf GetVnfResourceVersion Process")
	}

	/**
	 * This method executes a DELETE call to AAI for the provided
	 * Vnf.
	 *
	 * @param - execution
	 */
	public void deleteVnf(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericDeleteVnf DeleteVnf Process")
		try {
			String vnfId = execution.getVariable("GENDV_vnfId")
			String type = execution.getVariable("GENDV_type")
			msoLogger.debug("Type of Vnf Getting is: " + type)
			String resourceVersion = execution.getVariable("GENDV_resourceVersion")
			msoLogger.debug("Incoming Vnf Resource Version is: " + resourceVersion)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else{
				msoLogger.debug("Invalid Incoming GENDV_type")
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENDV_type")
			}

			String deleteVnfPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") +'?resource-version=' + UriUtils.encode(resourceVersion,"UTF-8")

			execution.setVariable("GENDV_deleteVnfPath", deleteVnfPath)
			msoLogger.debug("Delete Vnf Url is: " + deleteVnfPath)

			APIResponse response = aaiUriUtil.executeAAIDeleteCall(execution, deleteVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENDV_deleteVnfResponseCode", responseCode)
			msoLogger.debug("  DELETE Vnf response code is: " + responseCode)

			msoLogger.debug("GenericDeleteVnf Delete VNF Response Code: " + responseCode)
			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENDV_deleteVnfResponse", aaiResponse)

			msoLogger.debug("GenericDeleteVnf Delete VNF Response: " + aaiResponse)
			//Process Response
			if(responseCode == 204){
				msoLogger.debug("  DELETE Vnf Received a Good Response")
				execution.setVariable("GENDV_FoundIndicator", true)
			}else if(responseCode == 404){
				msoLogger.debug("  DELETE Vnf Received a Not Found (404) Response")
			}else if(responseCode == 412){
				msoLogger.debug("DELETE Vnf Received a Resource Version Mismatch Error: \n" + aaiResponse)
				exceptionUtil.buildAndThrowWorkflowException(execution, 412, "Delete Vnf Received a resource-version Mismatch Error Response from AAI")
			}else{
				msoLogger.debug("DELETE Vnf Received a BAD REST Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error encountered within GenericDeleteVnf DeleteVnf method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During Delete Vnf")
		}
		msoLogger.trace("COMPLETED GenericDeleteVnf DeleteVnf Process")
	}


}
