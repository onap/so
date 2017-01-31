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

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution;
import static org.apache.commons.lang3.StringUtils.*;

import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;


/**
 * This class supports the CreateVnfInfra Flow
 * with the creation of a generic vnf for
 * infrastructure.
 */
class CreateVnfInfra extends AbstractServiceTaskProcessor {

	String Prefix="CREVI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 */
	public void preProcessRequest(Execution execution) {
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		utils.log("DEBUG", " *** STARTED CreateVnfInfra PreProcessRequest Process*** ", isDebugEnabled)

		execution.setVariable("CREVI_sentSyncResponse", false)

		try{
			// Get Variables
			String createVnfRequest = execution.getVariable("bpmnRequest")
			execution.setVariable("CREVI_createVnfRequest", createVnfRequest)
			utils.logAudit("Incoming CreateVnfInfra Request is: \n" + createVnfRequest)

			if(createVnfRequest != null){

				String requestId = execution.getVariable("att-mso-request-id")
				execution.setVariable("CREVI_requestId", requestId)
				utils.log("DEBUG", "Incoming Request Id is: " + requestId, isDebugEnabled)

				String serviceInstanceId = execution.getVariable("serviceInstanceId")
				execution.setVariable("CREVI_serviceInstanceId", serviceInstanceId)
				utils.log("DEBUG", "Incoming Service Instance Id is: " + serviceInstanceId, isDebugEnabled)

				String vnfType = execution.getVariable("vnfType")
				execution.setVariable("CREVI_vnfType", vnfType)
				utils.log("DEBUG", "Incoming Vnf Type is: " + vnfType, isDebugEnabled)

				String vnfName = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.instanceName")
				execution.setVariable("CREVI_vnfName", vnfName)
				utils.log("DEBUG", "Incoming Vnf Name is: " + vnfName, isDebugEnabled)

				String serviceId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.productFamilyId")
				execution.setVariable("CREVI_serviceId", serviceId)
				utils.log("DEBUG", "Incoming Service Id is: " + serviceId, isDebugEnabled)

				String source = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.source")
				execution.setVariable("CREVI_source", source)
				utils.log("DEBUG", "Incoming Source is: " + source, isDebugEnabled)

				String suppressRollback = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.requestInfo.suppressRollback")
				execution.setVariable("CREVI_suppressRollback", suppressRollback)
				utils.log("DEBUG", "Incoming Suppress Rollback is: " + suppressRollback, isDebugEnabled)

				String modelInvariantId = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelInvariantId")
				execution.setVariable("CREVI_modelInvariantId", modelInvariantId)
				utils.log("DEBUG", "Incoming Invariant Id is: " + modelInvariantId, isDebugEnabled)

				String modelVersion = jsonUtil.getJsonValue(createVnfRequest, "requestDetails.modelInfo.modelVersion")
				execution.setVariable("CREVI_modelVersion", modelVersion)
				utils.log("DEBUG", "Incoming Model Version is: " + modelVersion, isDebugEnabled)

				//For Completion Handler & Fallout Handler
				String requestInfo =
				"""<request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>CREATE</action>
					<source>${source}</source>
				   </request-info>"""

				execution.setVariable("CREVI_requestInfo", requestInfo)

				//TODO: Orch Status - TBD, will come from SDN-C Response in 1702
				String orchStatus = "Created"
				execution.setVariable("CREVI_orchStatus", orchStatus)

				//TODO: Equipment Role - Should come from SDN-C Response in 1702
				String equipmentRole = " "
				execution.setVariable("CREVI_equipmentRole", equipmentRole)

				String vnfId = execution.getVariable("testVnfId") // for junits
				if(isBlank(vnfId)){
					vnfId = UUID.randomUUID().toString()
					utils.log("DEBUG", "Generated Vnf Id is: " + vnfId, isDebugEnabled)
				}
				execution.setVariable("CREVI_vnfId", vnfId)

				// Setting for Sub Flow Calls
				execution.setVariable("CREVI_type", "generic-vnf")
				execution.setVariable("GENGS_type", "service-instance")

			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Incoming Bpmn Request is Null.")
			}

		}catch(BpmnError b){
			utils.log("DEBUG", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", " Error Occured in CreateVnfInfra PreProcessRequest method!" + e.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PreProcessRequest")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra PreProcessRequest Process ***", isDebugEnabled)
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra SendSyncResponse Process *** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("CREVI_requestId")
			String vnfId = execution.getVariable("CREVI_vnfId")

			String createVnfResponse = """{"requestReferences":{"instanceId":"${vnfId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " CreateVnfInfra Sync Response is: \n"  + createVnfResponse, isDebugEnabled)

			sendWorkflowResponse(execution, 202, createVnfResponse)

			execution.setVariable("CREVI_sentSyncResponse", true)

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra SendSyncResponse Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra SendSyncResponse Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra SendSyncResponse Process ***", isDebugEnabled)
	}

	public void prepareCreateGenericVnf (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra PrepareCreateGenericVnf Process *** ", isDebugEnabled)
		try {
			//Get Vnf Info
			String vnfId = execution.getVariable("CREVI_vnfId")
			def vnfName = execution.getVariable("CREVI_vnfName")
			def vnfType = execution.getVariable("CREVI_vnfType")
			def serviceId = execution.getVariable("CREVI_serviceId")
			def orchStatus = execution.getVariable("CREVI_orchStatus")
			def modelInvariantId = execution.getVariable("CREVI_modelInvariantId")
			def modelVersion = execution.getVariable("CREVI_modelVersion")
			// TODO: 1702 Variable
			def equipmentRole = execution.getVariable("CREVI_equipmentRole")

			//Get Service Instance Info
			def serviceInstanceId = execution.getVariable("CREVI_serviceInstanceId")
			String siRelatedLink = execution.getVariable("GENGS_siResourceLink")

			int custStart = siRelatedLink.indexOf("customer/")
			int custEnd = siRelatedLink.indexOf("/service-subscriptions")
			String globalCustId = siRelatedLink.substring(custStart + 9, custEnd)
			int serviceStart = siRelatedLink.indexOf("service-subscription/")
			int serviceEnd = siRelatedLink.indexOf("/service-instances/")
			String serviceType = siRelatedLink.substring(serviceStart + 21, serviceEnd)

			//Get Namespace
			AaiUtil aaiUtil = new AaiUtil(this)
			def aai_uri = aaiUtil.getNetworkGenericVnfUri(execution)
			String namespace = aaiUtil.getNamespaceFromUri(aai_uri)

			String payload =
					"""<generic-vnf xmlns="${namespace}">
				<vnf-id>${vnfId}</vnf-id>
				<vnf-name>${vnfName}</vnf-name>
				<service-id>${serviceId}</service-id>
				<vnf-type>${vnfType}</vnf-type>
				<orchestration-status>${orchStatus}</orchestration-status>
				<persona-model-id>${modelInvariantId}</persona-model-id>
				<persona-model-version>${modelVersion}</persona-model-version>
				<relationship-list>
					<relationship>
               		<related-to>service-instance</related-to>
               		<related-link>${siRelatedLink}</related-link>
               		<relationship-data>
                  		<relationship-key>customer.global-customer-id</relationship-key>
                  		<relationship-value>${globalCustId}</relationship-value>
              		</relationship-data>
               		<relationship-data>
                  		<relationship-key>service-subscription.service-type</relationship-key>
                  		<relationship-value>${serviceType}</relationship-value>
               		</relationship-data>
					<relationship-data>
                  		<relationship-key>service-instance.service-instance-id</relationship-key>
                  		<relationship-value>${serviceInstanceId}</relationship-value>
               		</relationship-data>
            		</relationship>
				</relationship-list>
			</generic-vnf>"""

			execution.setVariable("CREVI_genericVnfPayload", payload)

		}catch(Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra PrepareCreateGenericVnf Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PrepareCreateGenericVnf Process")
		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra PrepareCreateGenericVnf Process ***", isDebugEnabled)
	}

	public void prepareCompletionHandlerRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra PrepareCompletionHandlerRequest Process *** ", isDebugEnabled)

		try {
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			String vnfId = execution.getVariable("CREVI_vnfId")
			requestInfo = utils.removeXmlPreamble(requestInfo)

			String request =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
									xmlns:ns="http://ecomp.att.com/mso/request/types/v1">
							${requestInfo}
							<status-message>Vnf has been created successfully.</status-message>
							<vnfId>${vnfId}</vnfId>
							<mso-bpel-name>CreateVnfInfra</mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			execution.setVariable("CREVI_completionHandlerRequest", request)
			utils.log("DEBUG", "Completion Handler Request is: " + request, isDebugEnabled)

			execution.setVariable("WorkflowResponse", "Success") // for junits

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra PrepareCompletionHandlerRequest Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra PrepareCompletionHandlerRequest Process ***", isDebugEnabled)
	}

	public void sendErrorResponse(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra sendErrorResponse Process *** ", isDebugEnabled)
		try {
			def sentSyncResponse = execution.getVariable("CREVI_sentSyncResponse")
			if(sentSyncResponse == false){
				WorkflowException wfex = execution.getVariable("WorkflowException")
				String response = exceptionUtil.buildErrorResponseXml(wfex)

				utils.logAudit(response)
				sendWorkflowResponse(execution, 500, response)
			}else{
				utils.log("DEBUG", "Not Sending Error Response.  Sync Response Already Sent", isDebugEnabled)
			}

		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra sendErrorResponse Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra sendErrorResponse Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra sendErrorResponse Process ***", isDebugEnabled)
	}

	public void prepareFalloutRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " *** STARTED CreateVnfInfra prepareFalloutRequest Process *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Incoming Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestInfo = execution.getVariable("CREVI_requestInfo")
			utils.log("DEBUG", " Incoming Request Info: " + requestInfo, isDebugEnabled)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable("CREVI_falloutRequest", falloutRequest)


		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in CreateVnfInfra prepareFalloutRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in CreateVnfInfra prepareFalloutRequest Process")

		}
		utils.log("DEBUG", "*** COMPLETED CreateVnfInfra prepareFalloutRequest Process ***", isDebugEnabled)
	}

}
