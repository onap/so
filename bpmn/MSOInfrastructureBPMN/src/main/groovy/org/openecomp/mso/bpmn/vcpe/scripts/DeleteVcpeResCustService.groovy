/*
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
package org.openecomp.mso.bpmn.vcpe.scripts

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils;
import org.openecomp.mso.bpmn.common.scripts.VidUtils;
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.AaiUtil

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;
import static org.apache.commons.lang3.StringUtils.*

/**
 * This groovy class supports the <class>DeleteVcpeResCustService.bpmn</class> process.
 *
 * @author dm4252
 *
 */
public class DeleteVcpeResCustService extends AbstractServiceTaskProcessor {

	private static final String DebugFlag = "isDebugLogEnabled"

	String Prefix = "DVRCS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()
	CatalogDbUtils catalogDbUtils = new CatalogDbUtils()
	NetworkUtils networkUtils = new NetworkUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DeleteVcpeResCustService.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("DeleteVcpeResCustServiceRequest", "")
		execution.setVariable("msoRequestId", "")
		execution.setVariable(Prefix+"vnfsDeletedCount", 0)
		execution.setVariable(Prefix+"vnfsCount", 0)
	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest DeleteVcpeResCustService Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String DeleteVcpeResCustServiceRequest = execution.getVariable("bpmnRequest")
			utils.logAudit(DeleteVcpeResCustServiceRequest)
			execution.setVariable("DeleteVcpeResCustServiceRequest", DeleteVcpeResCustServiceRequest);
			println 'DeleteVcpeResCustServiceRequest - ' + DeleteVcpeResCustServiceRequest

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

			String source = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.requestInfo.source")
			if ((source == null) || (source.isEmpty())) {
				source = "VID"
			}
			execution.setVariable("source", source)

			// extract globalSubscriberId
			String globalSubscriberId = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.subscriberInfo.globalSubscriberId")

			// global-customer-id is optional on Delete

			execution.setVariable("globalSubscriberId", globalSubscriberId)
			execution.setVariable("globalCustomerId", globalSubscriberId)
			
			String suppressRollback = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.requestInfo.suppressRollback")
			execution.setVariable("disableRollback", suppressRollback)
			utils.log("DEBUG", "Incoming Suppress/Disable Rollback is: " + suppressRollback, isDebugEnabled)
			
			String productFamilyId = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			utils.log("DEBUG", "Incoming productFamilyId is: " + productFamilyId, isDebugEnabled)
			
			// extract subscriptionServiceType
			String subscriptionServiceType = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.requestParameters.subscriptionServiceType")
			execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			utils.log("DEBUG", "Incoming subscriptionServiceType is: " + subscriptionServiceType, isDebugEnabled)
			
			// extract cloud configuration
			String cloudConfiguration = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.cloudConfiguration")
			execution.setVariable("cloudConfiguration", cloudConfiguration)
			utils.log("DEBUG","cloudConfiguration: "+ cloudConfiguration, isDebugEnabled)
			String lcpCloudRegionId = jsonUtil.getJsonValue(cloudConfiguration, "lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			utils.log("DEBUG","lcpCloudRegionId: "+ lcpCloudRegionId, isDebugEnabled)
			String tenantId = jsonUtil.getJsonValue(cloudConfiguration, "tenantId")
			execution.setVariable("tenantId", tenantId)
			utils.log("DEBUG","tenantId: "+ tenantId, isDebugEnabled)

			String sdncVersion = "1707"
			execution.setVariable("sdncVersion", sdncVersion)
			utils.log("DEBUG","sdncVersion: "+ sdncVersion, isDebugEnabled)
			
			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>${requestId}</request-id>
					<action>DELETE</action>
					<source>${source}</source>
				   </request-info>"""

			execution.setVariable(Prefix+"requestInfo", requestInfo)
			
			//Setting for Generic Sub Flows
			execution.setVariable("GENGS_type", "service-instance")
			
			utils.log("DEBUG", " ***** Completed preProcessRequest DeleteVcpeResCustServiceRequest Request ***** ", isDebugEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void sendSyncResponse(Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)

		utils.log("DEBUG", " ***** Inside sendSyncResponse of DeleteVcpeResCustService ***** ", isDebugEnabled)

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String requestId = execution.getVariable("mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse ="""{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void prepareServiceDelete(Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " ***** Inside prepareServiceDelete() of DeleteVcpeResCustService ***** ", isDebugEnabled)
		
		try {
			
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			
			// confirm if ServiceInstance was found
			if ( !execution.getVariable("GENGS_FoundIndicator") )
			{
				String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Service Instance was not found in AAI by id: " + serviceInstanceId
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			}
			
			// get variable within incoming json
			String DeleteVcpeResCustServiceRequest = execution.getVariable("DeleteVcpeResCustServiceRequest");
			
			// get SI extracted by GenericGetService
			String serviceInstanceAaiRecord = execution.getVariable("GENGS_service");
			
			utils.log("DEBUG", "serviceInstanceAaiRecord: "+serviceInstanceAaiRecord, isDebugEnabled)
			serviceInstanceAaiRecord = utils.removeXmlNamespaces(serviceInstanceAaiRecord)
			
			def (TXC_found, TXC_id) = new Tuple(false, null)
			def (BRG_found, BRG_id) = new Tuple(false, null)
			List relatedVnfIdList = []
			
			for(Node rel: utils.getMultNodeObjects(serviceInstanceAaiRecord, "relationship")) {
				def relto = utils.getChildNodeText(rel, "related-to")
				def relink = utils.getChildNodeText(rel, "related-link")
				utils.log("DEBUG", "check: "+relto+" link: "+relink, isDebugEnabled)
				
				if(isBlank(relto) || isBlank(relink)) {
					
				} else if(relto == "generic-vnf") {
					def id = relink.substring(relink.indexOf("/generic-vnf/")+13)
					if(id.endsWith("/")) {
						id = id.substring(0, id.length()-1)
					}
					
					relatedVnfIdList.add(id)
					
				} else if(relto == "allotted-resource") {
					def (type, id) = getAaiAr(execution, relink)
					
					if(isBlank(type) || isBlank(id)) {
						
					} else if(type == "TunnelXConn") {
						utils.log("DEBUG","TunnelXConn AR found", isDebugEnabled)
						TXC_found = true
						TXC_id = id
						
					} else if(type == "BRG") {
						utils.log("DEBUG","BRG AR found", isDebugEnabled)
						BRG_found = true
						BRG_id = id
					}
				}
			}
			
			execution.setVariable(Prefix+"TunnelXConn", TXC_found)
			execution.setVariable("TXC_allottedResourceId", TXC_id)
			utils.log("DEBUG", "TXC_allottedResourceId: " + TXC_id, isDebugEnabled)
						
			execution.setVariable(Prefix+"BRG", BRG_found)
			execution.setVariable("BRG_allottedResourceId", BRG_id)
			utils.log("DEBUG", "BRG_allottedResourceId: " + BRG_id, isDebugEnabled)
			
			int vnfsCount = relatedVnfIdList.size()
			execution.setVariable(Prefix+"vnfsCount", vnfsCount)
			utils.log("DEBUG", " "+Prefix+"vnfsCount : " + vnfsCount, isDebugEnabled)
			if(vnfsCount > 0) {
				execution.setVariable(Prefix+"relatedVnfIdList", relatedVnfIdList)
			}
			
			utils.log("DEBUG", " ***** Completed prepareServiceDelete() of DeleteVcpeResCustService ***** ", isDebugEnabled)
		} catch (BpmnError e){
			throw e;
		} catch (Exception ex) {
			sendSyncError(execution)
		    String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. prepareServiceDelete() - " + ex.getMessage()
		    utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}
	
	private getAaiAr(Execution execution, String relink) {
		def isDebugEnabled = execution.getVariable(DebugFlag)
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = execution.getVariable("URN_aai_endpoint") + relink
		
		utils.log("DEBUG", "get AR info " + aaiEndpoint, isDebugEnabled)
		APIResponse response = aaiUtil.executeAAIGetCall(execution, aaiEndpoint)
		
		int responseCode = response.getStatusCode()
		utils.log("DEBUG", "get AR info responseCode:" + responseCode, isDebugEnabled)
		
		String aaiResponse = response.getResponseBodyAsString()
		utils.log("DEBUG", "get AR info " + aaiResponse, isDebugEnabled)
		
		if(responseCode < 200 || responseCode >= 300 || isBlank(aaiResponse)) {
			return new Tuple2(null, null)
		}
		
		def type = utils.getNodeText1(aaiResponse, "type")
		def id = utils.getNodeText1(aaiResponse, "id")
		
		return new Tuple2(type, id)
	}
	
	
	// *******************************
	//     
	// *******************************
	public void prepareVnfAndModulesDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " ***** Inside prepareVnfAndModulesDelete of DeleteVcpeResCustService ***** ", isDebugEnabled)

		try {
			List vnfList = execution.getVariable(Prefix+"relatedVnfIdList")
			int vnfsDeletedCount = execution.getVariable(Prefix+"vnfsDeletedCount")
			String vnfModelInfoString = ""
			String vnfId = ""
			if (vnfList.size() > 0 ) {
				vnfId = vnfList.get(vnfsDeletedCount.intValue())
			}
							
			execution.setVariable("vnfId", vnfId)
			utils.log("DEBUG", "need to delete vnfId:" + vnfId, isDebugEnabled)

			utils.log("DEBUG", " ***** Completed prepareVnfAndModulesDelete of DeleteVcpeResCustService ***** ", isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected Error from method prepareVnfAndModulesDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }
	
	// *******************************
	//     Validate Vnf request Section -> increment count
	// *******************************
	public void validateVnfDelete (Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " ***** Inside validateVnfDelete of DeleteVcpeResCustService ***** ", isDebugEnabled)

		try {
			int vnfsDeletedCount = execution.getVariable(Prefix+"vnfsDeletedCount")
			vnfsDeletedCount++
			
			execution.setVariable(Prefix+"vnfsDeletedCount", vnfsDeletedCount)
			
			utils.log("DEBUG", " ***** Completed validateVnfDelete of DeleteVcpeResCustService ***** "+" vnf # "+vnfsDeletedCount, isDebugEnabled)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected Error from method validateVnfDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	
	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " ***** Inside postProcessResponse of DeleteVcpeResCustService ***** ", isDebugEnabled)

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
							<aetgt:status-message>vCPE Res Cust Service Instance has been deleted successfully.</aetgt:status-message>
							   <aetgt:mso-bpel-name>BPMN Service Instance macro action: DELETE</aetgt:mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			utils.logAudit(xmlMsoCompletionRequest)
			execution.setVariable(Prefix+"Success", true)
			execution.setVariable(Prefix+"CompleteMsoProcessRequest", xmlMsoCompletionRequest)
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
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " *** STARTED DeleteVcpeResCustService prepareFalloutRequest Process *** ", isDebugEnabled)

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			utils.log("DEBUG", " Incoming Workflow Exception: " + wfex.toString(), isDebugEnabled)
			String requestInfo = execution.getVariable(Prefix+"requestInfo")
			utils.log("DEBUG", " Incoming Request Info: " + requestInfo, isDebugEnabled)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable(Prefix+"falloutRequest", falloutRequest)
		} catch (Exception ex) {
			utils.log("DEBUG", "Error Occured in DeleteVcpeResCustService prepareFalloutRequest Process " + ex.getMessage(), isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteVcpeResCustService prepareFalloutRequest Process")
		}
		utils.log("DEBUG", "*** COMPLETED DeleteVcpeResCustService prepareFalloutRequest Process ***", isDebugEnabled)
	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		utils.log("DEBUG", " ***** Inside sendSyncError() of DeleteVcpeResCustService ***** ", isDebugEnabled)

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
		def isDebugEnabled=execution.getVariable(DebugFlag)
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable(Prefix+"unexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable(Prefix+"unexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}


}
