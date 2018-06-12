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
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, GenericPutVnf.class);


	String Prefix = "GENPV_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()

	/**
	 * This method validates the incoming variables and
	 * generates a Vnf Id if one is not provided.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(DelegateExecution execution) {
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericPutVnf PreProcessRequest Process")

		execution.setVariable("GENPV_SuccessIndicator", false)
		execution.setVariable("GENPV_FoundIndicator", false)

		try{
			// Get Variables
			String payload = execution.getVariable("GENPV_vnfPayload")
			msoLogger.debug("Incoming Vnf Payload is: " + payload)
			String type = execution.getVariable("GENPV_type")
			msoLogger.debug("Incoming Type of Vnf is: " + type)

			if(isBlank(payload) || isBlank(type)){
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Incoming Vnf Payload and/or Type is null. These Variables are required!", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Vnf Payload and/or Type is null. These Variables are required!")
			}else{
				String vnfId = execution.getVariable("GENPV_vnfId")
				if(isBlank(vnfId)){
					vnfId = UUID.randomUUID().toString()
					msoLogger.debug("Generated Vnf Id is: " + vnfId)
					execution.setVariable("GENPV_vnfId", vnfId)
				}else{
					msoLogger.debug("Incoming Vnf Id is: " + vnfId)
				}
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericPutVnf PreProcessRequest method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in GenericPutVnf PreProcessRequest")

		}
		msoLogger.trace("COMPLETED GenericPutVnf PreProcessRequest Process ")
	}

	/**
	 * This method executes a Put call to AAI to create
	 * or update a Vnf Object using the provided payload
	 *
	 * @param - execution
	 */
	public void putVnf(DelegateExecution execution){
		execution.setVariable("prefix",Prefix)
		msoLogger.trace("STARTED GenericPutVnf PutVnf Process")
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
				msoLogger.debug("Invalid Incoming GENGV_type")
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid Incoming GENPV_type")
			}
			msoLogger.debug("Using AAI Uri: " + aai_uri)

			String path = "${aai_uri}/" + UriUtils.encode(vnfId, "UTF-8")
			msoLogger.debug("PUT Vnf Endpoint is: " + path)

			String putVnfAAIPath = UrnPropertiesReader.getVariable("aai.endpoint", execution) + path
			execution.setVariable("GENPV_putVnfAAIPath", putVnfAAIPath)
			msoLogger.debug("PUT Vnf Url is: " + putVnfAAIPath)

			APIResponse apiResponse = aaiUtil.executeAAIPutCall(execution, putVnfAAIPath, payload)
			int responseCode = apiResponse.getStatusCode()
			execution.setVariable("GENPV_putVnfResponseCode", responseCode)
			msoLogger.debug("AAI Response Code: " + responseCode)
			String aaiResponse = apiResponse.getResponseBodyAsString()
			aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
			execution.setVariable("GENPV_putVnfResponse", aaiResponse)
			msoLogger.debug("AAI Response: " + aaiResponse)

			if(responseCode == 200 || responseCode == 201){
				msoLogger.debug("PUT Vnf Received a Good Response Code.")
			}else{
				msoLogger.debug("PUT Vnf Received a Bad Response Code. Response Code is: " + responseCode)
				exceptionUtil.MapAAIExceptionToWorkflowExceptionGeneric(execution, aaiResponse, responseCode)
				throw new BpmnError("MSOWorkflowException")
			}
		}catch(BpmnError b){
			msoLogger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, " Error encountered within GenericPutVnf PutVnf method!" + e, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured During PutVnf")
		}
		msoLogger.trace("COMPLETED GenericPutVnf PutVnf Process")
	}

}
