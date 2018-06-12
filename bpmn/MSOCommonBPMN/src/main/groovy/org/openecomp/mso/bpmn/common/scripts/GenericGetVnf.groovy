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
import static org.openecomp.mso.bpmn.common.scripts.GenericUtils.*;

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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, GenericGetVnf.class);


	String Prefix = "GENGV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * determines the subsequent event based on which
	 * variables the calling flow provided.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetVnf PreProcessRequest Process")

		execution.setVariable("GENGV_getVnfByName", false)
		execution.setVariable("GENGV_SuccessIndicator", false)
		execution.setVariable("GENGV_FoundIndicator", false)

		try{
			// Get Variables
			String vnfId = execution.getVariable("GENGV_vnfId")
			msoLogger.debug("Incoming Vnf Id is: " + vnfId)
			String vnfName = execution.getVariable("GENGV_vnfName")
			msoLogger.debug("Incoming Vnf Name is: " + vnfName)

			if(isBlank(vnfId) && isBlank(vnfName)){
				msoLogger.debug("Incoming Vnf Name and Vnf Id are null. At least one is required!")
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Vnf Name and Vnf Id are null. At least one is required.")
			}else{
				if(isBlank(vnfId)){
					execution.setVariable("GENGV_getVnfByName", true)
				}
			}

		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.debug(" Error encountered within GenericGetVnf PreProcessRequest method!" + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericGetVnf PreProcessRequest")

		}
		msoLogger.trace("COMPLETED GenericGetVnf PreProcessRequest Process ")
	}

	/**
	 * This method executes a GET call to AAI to obtain the
	 * Vnf using the Vnf Name
	 *
	 * @param - execution
	 */
	public void getVnfByName(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetVnf GetVnfByName Process")
		try {
			String vnfName = execution.getVariable("GENGV_vnfName")
			msoLogger.debug("Getting Vnf by Vnf Name: " + vnfName)
			String type = execution.getVariable("GENGV_type")
			msoLogger.debug("Type of Vnf Getting is: " + type)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
			AaiUtil aaiUriUtil = new AaiUtil(this)

			//Determine Type of Vnf Querying For.
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUriUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUriUtil.getNetworkVceUri(execution)
			}else{
				msoLogger.debug("Invalid Incoming GENGV_type")
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENGV_type")
			}

			String getVnfPath = "${aai_endpoint}${aai_uri}?vnf-name=" + UriUtils.encode(vnfName, "UTF-8") + "&depth=1"

			execution.setVariable("GENGV_getVnfPath", getVnfPath)
			msoLogger.debug("Get Vnf Url is: " + getVnfPath)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGV_getVnfResponseCode", responseCode)
			msoLogger.debug("  GET Vnf response code is: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGV_getVnfResponse", aaiResponse)

			//Process Response
			if(responseCode == 200){
				msoLogger.debug("GET Vnf Received a Good Response")
					if(utils.nodeExists(aaiResponse, type)){
						msoLogger.debug("GET Vnf Response Contains a Vnf")
						execution.setVariable("GENGV_FoundIndicator", true)
						execution.setVariable("GENGV_vnf", aaiResponse)
						execution.setVariable("WorkflowResponse", aaiResponse)
					}else{
						msoLogger.debug("GET Vnf Response Does NOT Contain a Vnf")
					}

			}else if(responseCode == 404){
				msoLogger.debug("GET Vnf Received a Not Found (404) Response")
			}else{
				msoLogger.debug("GET Vnf Received a Bad Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericGetVnf GetVnfByName method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetVnfByName")
		}
		msoLogger.trace("COMPLETED GenericGetVnf GetVnfByName Process")
	}

	/**
	 * This method executes a GET call to AAI to obtain the
	 * Vnf using the Vnf Id
	 *
	 * @param - execution
	 */
	public void getVnfById(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericGetVnf GetVnfById Process")
		try {
			String vnfId = execution.getVariable("GENGV_vnfId")
			msoLogger.debug("Getting Vnf by Vnf Id: " + vnfId)
			String type = execution.getVariable("GENGV_type")
			msoLogger.debug("Type of Vnf Getting is: " + type)

			String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
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
				msoLogger.debug("Invalid Incoming GENGV_type")
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENGV_type")
			}
			msoLogger.debug("Using AAI Uri: " + aai_uri)

			String getVnfPath = "${aai_endpoint}${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8") + "?depth=1"
			msoLogger.debug("GET Vnf Endpoint is: " + getVnfPath)

			execution.setVariable("GENGV_getVnfPath", getVnfPath)
			msoLogger.debug("Get Vnf Url is: " + getVnfPath)

			APIResponse response = aaiUriUtil.executeAAIGetCall(execution, getVnfPath)
			int responseCode = response.getStatusCode()
			execution.setVariable("GENGV_getVnfResponseCode", responseCode)
			msoLogger.debug("  GET Vnf response code is: " + responseCode)

			String aaiResponse = response.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENGV_getVnfResponse", aaiResponse)

			//Process Response
			if(responseCode == 200){
				msoLogger.debug("GET Vnf Received a Good Response")
					if(utils.nodeExists(aaiResponse, type)){
						msoLogger.debug("GET Vnf Response Contains a Vnf")
						execution.setVariable("GENGV_FoundIndicator", true)
						execution.setVariable("GENGV_vnf", aaiResponse)
						execution.setVariable("WorkflowResponse", aaiResponse)
					}else{
						msoLogger.debug("GET Vnf Response Does NOT Contain a Vnf")
					}

			}else if(responseCode == 404){
				msoLogger.debug("GET Vnf Received a Not Found (404) Response")
			}else{
				msoLogger.debug("GET Vnf Received a BAD REST Response: \n" + aaiResponse)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericGetVnf GetVnfById method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During GetVnfById")
		}
		msoLogger.trace("COMPLETED GenericGetVnf GetVnfById Process")
	}

}
