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
package org.openecomp.mso.bpmn.infrastructure.scripts;

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;


import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>DeleteViprAtmService.bpmn</class> process.
 *
 */
public class DeleteGenericMacroServiceNetworkVnf extends AbstractServiceTaskProcessor {

	String Prefix="DELVAS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtils()
	NetworkUtils networkUtils = new NetworkUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DeleteViprAtmService.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("deleteViprAtmServiceRequest", "")
		execution.setVariable("msoRequestId", "")
		execution.setVariable("DELVAS_vnfsDeletedCount", 0)
		execution.setVariable("DELVAS_vnfsCount", 0)
		execution.setVariable("DELVAS_networksCount", 0)
		execution.setVariable("DELVAS_networksDeletedCount", 0)
	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest DeleteViprAtmService Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String deleteViprAtmServiceRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(deleteViprAtmServiceRequest)
			execution.setVariable("deleteViprAtmServiceRequest", deleteViprAtmServiceRequest);
			println 'deleteViprAtmServiceRequest - ' + deleteViprAtmServiceRequest

			// extract requestId
			String requestId = execution.getVariable("mso-request-id")
			execution.setVariable("msoRequestId", requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			if ((serviceInstanceId == null) || (serviceInstanceId.isEmpty())) {
				String dataErrorMessage = " Element 'serviceInstanceId' is missing. "
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}
			
			String requestAction = execution.getVariable("requestAction")
			execution.setVariable("requestAction", requestAction)

			String source = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.requestInfo.source")
			if ((source == null) || (source.isEmpty())) {
				execution.setVariable("source", "VID")
			} else {
				execution.setVariable("source", source)
			}

			// extract globalSubscriberId
			String globalSubscriberId = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.subscriberInfo.globalSubscriberId")

			// global-customer-id is optional on Delete

			execution.setVariable("globalSubscriberId", globalSubscriberId)
			execution.setVariable("globalCustomerId", globalSubscriberId)
			
			String suppressRollback = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.requestInfo.suppressRollback")
			execution.setVariable("disableRollback", suppressRollback)
			utils.log("DEBUG", "Incoming Suppress/Disable Rollback is: " + suppressRollback, isDebugEnabled)
			
			String productFamilyId = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			utils.log("DEBUG", "Incoming productFamilyId is: " + productFamilyId, isDebugEnabled)
			
			// extract subscriptionServiceType
			String subscriptionServiceType = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.requestParameters.subscriptionServiceType")
			execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			utils.log("DEBUG", "Incoming subscriptionServiceType is: " + subscriptionServiceType, isDebugEnabled)
			
			// extract cloud configuration
			String lcpCloudRegionId = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.cloudConfiguration.lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			utils.log("DEBUG","lcpCloudRegionId: "+ lcpCloudRegionId, isDebugEnabled)
			String tenantId = jsonUtil.getJsonValue(deleteViprAtmServiceRequest, "requestDetails.cloudConfiguration.tenantId")
			execution.setVariable("tenantId", tenantId)
			utils.log("DEBUG","tenantId: "+ tenantId, isDebugEnabled)

			String sdncVersion = "1702"
			execution.setVariable("sdncVersion", sdncVersion)
			utils.log("DEBUG","sdncVersion: "+ sdncVersion, isDebugEnabled)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>DELETE</action>
					<source>${source}</source>
				   </request-info>"""

			execution.setVariable("DELVAS_requestInfo", requestInfo)
			
			//Setting for Generic Sub Flows
			execution.setVariable("GENGS_type", "service-instance")
			
			utils.log("DEBUG", " ***** Completed preProcessRequest DeleteViprAtmService Request ***** ", isDebugEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in DeleteViprAtmService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside sendSyncResponse of DeleteViprAtmService ***** ", isDebugEnabled)

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String requestId = execution.getVariable("mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse ="""{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DeleteViprAtmService flow. Unexpected from method sendSyncResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void prepareServiceInstanceDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** Inside prepareServiceInstanceDelete() of DeleteViprAtmService ***** ", isDebugEnabled)
		
		try {
			
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			
			// confirm if ServiceInstance was found
			if ( !execution.getVariable("GENGS_FoundIndicator") )
			{
				String exceptionMessage = "Bpmn error encountered in DeleteViprAtmService flow. Service Instance was not found in AAI by id: " + serviceInstanceId
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			}
			
			// get variable within incoming json
			String deleteViprAtmServiceRequest = execution.getVariable("deleteViprAtmServiceRequest");
			
			// get SI extracted by GenericGetService
			String serviceInstanceAaiRecord = execution.getVariable("GENGS_service");
			
			utils.log("DEBUG", "serviceInstanceAaiRecord: "+serviceInstanceAaiRecord, isDebugEnabled)
			
			String relationship = ""
			try {
				relationship = networkUtils.getFirstNodeXml(serviceInstanceAaiRecord, "relationship-list")
			} catch (Exception ex) {
				//no relationships found
			}
			utils.log("DEBUG", " relationship string - " + relationship, isDebugEnabled)
			
			int vnfsCount = 0
			int networksCount = 0
			
			if (relationship != null && relationship.length() > 0){
				relationship = relationship.trim().replace("tag0:","").replace(":tag0","")
				
				// Check if Network TableREf is present, then build a List of network policy
				List relatedVnfIdList = networkUtils.getRelatedVnfIdList(relationship)
				vnfsCount = relatedVnfIdList.size()
				execution.setVariable("DELVAS_vnfsCount", vnfsCount)
				utils.log("DEBUG", " DELVAS_vnfsCount : " + vnfsCount, isDebugEnabled)
				execution.setVariable("DELVAS_relatedVnfIdList", relatedVnfIdList)
				
				// Check if Network TableREf is present, then build a List of network policy
				List relatedNetworkIdList = networkUtils.getRelatedNetworkIdList(relationship)
				networksCount = relatedNetworkIdList.size()
				execution.setVariable("DELVAS_networksCount", networksCount)
				utils.log("DEBUG", " DELVAS_networksCount : " + networksCount, isDebugEnabled)
				execution.setVariable("DELVAS_relatedNetworkIdList", relatedNetworkIdList)
			} else {
				execution.setVariable("DELVAS_vnfsCount", 0)
				utils.log("DEBUG", " DELVAS_vnfsCount : " + vnfsCount, isDebugEnabled)
				execution.setVariable("DELVAS_networksCount", 0)
				utils.log("DEBUG", " DELVAS_networksCount : " + networksCount, isDebugEnabled)
			}
			
			utils.log("DEBUG", " ***** Completed prepareServiceInstanceDelete() of DeleteViprAtmService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in DeleteViprAtmService flow. prepareServiceInstanceDelete() - " + ex.getMessage()
		   utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}
	
	
	// *******************************
	//     
	// *******************************
	public void prepareVnfAndModulesDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareVnfAndModulesDelete of DeleteServiceInstanceMacro ***** ", isDebugEnabled)

			List vnfList = execution.getVariable("DELVAS_relatedVnfIdList")
			Integer vnfsDeletedCount = execution.getVariable("DELVAS_vnfsDeletedCount")
			String vnfModelInfoString = ""
			String vnfId = ""
			if (vnfList.size() > 0 ) {
				vnfId = vnfList.get(vnfsDeletedCount.intValue())
			}
							
			execution.setVariable("vnfId", vnfId)
			utils.log("DEBUG", "need to delete vnfId:" + vnfId, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed prepareVnfAndModulesDelete of DeleteServiceInstanceMacro ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstanceMacro flow. Unexpected Error from method prepareVnfAndModulesDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Validate Vnf request Section -> increment count
	// *******************************
	public void validateVnfDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateVnfDelete of DeleteViprAtmService ***** ", isDebugEnabled)

			String vnfsDeletedCount = execution.getVariable("DELVAS_vnfsDeletedCount")
			vnfsDeletedCount++
			
			execution.setVariable("DELVAS_vnfsDeletedCount", vnfsDeletedCount)
			
			utils.log("DEBUG", " ***** Completed validateVnfDelete of DeleteViprAtmService ***** "+" vnf # "+vnfsDeletedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteViprAtmService flow. Unexpected Error from method validateVnfDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Generate Network request Section
	// *******************************
	public void prepareNetworkDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareNetworkDelete of DeleteViprAtmService ***** ", isDebugEnabled)

			List networkList = execution.getVariable("DELVAS_relatedNetworkIdList")
			Integer networksDeletedCount = execution.getVariable("DELVAS_networksDeletedCount")

			String networkId = ""
			if (networkList.size() > 0) {
				networkId = networkList.get(networksDeletedCount.intValue())
			}
							
			execution.setVariable("networkId", networkId)
			utils.log("DEBUG", "need to delete networkId:" + networkId, isDebugEnabled)
			
			utils.log("DEBUG", " ***** Completed prepareNetworkDelete of DeleteViprAtmService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = q"Bpmn error encountered in DeleteViprAtmService flow. Unexpected Error from method prepareNetworkDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Validate Network request Section
	// *******************************
	public void validateNetworkDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside validateNetworkDelete of DeleteViprAtmService ***** ", isDebugEnabled)

			Integer networksDeletedCount = execution.getVariable("DELVAS_networksDeletedCount")
			networksDeletedCount++
			
			execution.setVariable("DELVAS_networksDeletedCount", networksDeletedCount)
			
			utils.log("DEBUG", " ***** Completed validateNetworkDelete of DeleteViprAtmService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteViprAtmService flow. Unexpected Error from method validateNetworkDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	
	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** Inside postProcessResponse of DeleteViprAtmService ***** ", isDebugEnabled)

		try {
			String source = execution.getVariable("source")
			String requestId = execution.getVariable("msoRequestId")

			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
									xmlns:ns="http://org.openecomp/mso/request/types/v1">
							<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
								<request-id>${requestId}</request-id>
								<action>DELETE</action>
								<source>${source}</source>
							   </request-info>
							<aetgt:status-message>vIPR ATM Service Instance has been deleted successfully.</aetgt:status-message>
							   <aetgt:mso-bpel-name>BPMN Service Instance macro action: DELETE</aetgt:mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			utils.logAudit(xmlMsoCompletionRequest)
			execution.setVariable("DELVAS_Success", true)
			execution.setVariable("DELVAS_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			utils.log("DEBUG", " SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstance flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void prepareFalloutRequest(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " *** STARTED DeleteViprAtmService prepareFalloutRequest Process *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Incoming Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestInfo = execution.getVariable("DELVAS_requestInfo")
			utils.log("DEBUG", " Incoming Request Info: " + requestInfo, isDebugEnabled)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable("DELVAS_falloutRequest", falloutRequest)
		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in DeleteViprAtmService prepareFalloutRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteViprAtmService prepareFalloutRequest Process")
		}
		utils.log("DEBUG", "*** COMPLETED DeleteViprAtmService prepareFalloutRequest Process ***", isDebugEnabled)
	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("DEBUG", " ***** Inside sendSyncError() of DeleteServiceInstanceInfra ***** ", isDebugEnabled)

		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
				"""<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			utils.logAudit(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)
		} catch (Exception ex) {
			utils.log("DEBUG", " Sending Sync Error Activity Failed. " + "\n" + ex.getMessage(), isDebugEnabled)
		}
	}

	public void processJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("DELVAS_unexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("DELVAS_unexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}


}