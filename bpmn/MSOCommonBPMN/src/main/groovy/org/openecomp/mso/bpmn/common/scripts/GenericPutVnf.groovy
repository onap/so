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
 * TODO: Support Putting vnf type = vpe and vce
 *
 * This class supports the GenericPutVnf Sub Flow.
 * This Generic sub flow can be used by any flow for accomplishing
 * the goal of Creating/Updating(PUT) a Vnf Object (in AAI).  The flow
 * supports the Creating/Updating of 3 types of Vnfs (generic-vnf, vce, and vpe).
 * The "type" must be provided by the calling flow and this type should
 * be mapped to the variable GENPV_type. The type should either be
 * "generic-vnf", "vce", or "vpe".  In addition, the Vnf Id and
 * payload should be provided.
 *
 * Upon successful completion of this sub flow the
 * GENPV_SuccessIndicator.  An MSOWorkflowException will
 * be thrown if an error occurs at any time during this
 * sub flow. Please map input variables to the corresponding
 * variable names below.
 *
 *
 * Incoming Required Variables:
 * @param - GENPV_vnfId
 * @param - GENPV_vnfPayload
 * @param - GENPV_type
 *
 *
 * Outgoing Variables:
 * @param - GENPV_SuccessIndicator
 * @param - WorkflowException
 */
class GenericPutVnf  extends AbstractServiceTaskProcessor{

	String Prefix = "GENPV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * generates a Vnf Id if one is not provided.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericPutVnf PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("GENPV_SuccessIndicator", false)
		execution.setVariable("GENPV_FoundIndicator", false)

		try{
			// Get Variables
			String payload = execution.getVariable("GENPV_vnfPayload")
			utils.log("DEBUG", "Incoming Vnf Payload is: " + payload, isDebugEnabled)
			String type = execution.getVariable("GENPV_type")
			utils.log("DEBUG", "Incoming Type of Vnf is: " + type, isDebugEnabled)

			if(isBlank(payload) || isBlank(type)){
				utils.log("ERROR", "Incoming Vnf Payload and/or Type is null. These Variables are required!", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Vnf Payload and/or Type is null. These Variables are required!")
			}else{
				String vnfId = execution.getVariable("GENPV_vnfId")
				if(isBlank(vnfId)){
					vnfId = UUID.randomUUID().toString()
					utils.log("DEBUG", "Generated Vnf Id is: " + vnfId, isDebugEnabled)
					execution.setVariable("GENPV_vnfId", vnfId)
				}else{
					utils.log("DEBUG", "Incoming Vnf Id is: " + vnfId, isDebugEnabled)
				}
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericPutVnf PreProcessRequest method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericPutVnf PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED GenericPutVnf PreProcessRequest Process ***", isDebugEnabled)
	}

	/**
	 * This method executes a Put call to AAI to create
	 * or update a Vnf Object using the provided payload
	 *
	 * @param - execution
	 */
	public void putVnf(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED GenericPutVnf PutVnf Process*** ", isDebugEnabled)
		try {
			String vnfId = execution.getVariable("GENPV_vnfId")
			String payload = execution.getVariable("GENPV_vnfPayload")
			String type = execution.getVariable("GENPV_type")

			AaiUtil aaiUtil = new AaiUtil(this)
			def aai_uri = ""
			if(type.equals("generic-vnf")){
				aai_uri = aaiUtil.getNetworkGenericVnfUri(execution)
			}else if(type.equals("vce")){
				aai_uri = aaiUtil.getNetworkVceUri(execution)
			}else if(type.equals("vpe")){
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "GenericPutVnf does not yet support getting type of vnf = vpe")
			}else{
				utils.log("DEBUG", "Invalid Incoming GENGV_type", isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENPV_type")
			}
			utils.log("DEBUG", "Using AAI Uri: " + aai_uri, isDebugEnabled)

			String path = "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8")
			utils.log("DEBUG", "PUT Vnf Endpoint is: " + path, isDebugEnabled)

			String putVnfAAIPath = execution.getVariable("URN_aai_endpoint") + path
			execution.setVariable("GENPV_putVnfAAIPath", putVnfAAIPath)
			utils.logAudit("PUT Vnf Url is: " + putVnfAAIPath)

			APIResponse apiResponse = aaiUtil.executeAAIPutCall(execution, putVnfAAIPath, payload)
			int responseCode = apiResponse.getStatusCode()
			execution.setVariable("GENPV_putVnfResponseCode", responseCode)
			utils.logAudit("AAI Response Code: " + responseCode)
			String aaiResponse = apiResponse.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENPV_putVnfResponse", aaiResponse)
			utils.logAudit("AAI Response: " + aaiResponse)

			if(responseCode == 200 || responseCode == 201){
				utils.log("DEBUG", "PUT Vnf Received a Good Response Code.", isDebugEnabled)
			}else{
				utils.log("DEBUG", "PUT Vnf Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("ERROR", " Error encountered within GenericPutVnf PutVnf method!" + e, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During PutVnf")
		}
		utils.log("DEBUG", " *** COMPLETED GenericPutVnf PutVnf Process*** ", isDebugEnabled)
	}

}
