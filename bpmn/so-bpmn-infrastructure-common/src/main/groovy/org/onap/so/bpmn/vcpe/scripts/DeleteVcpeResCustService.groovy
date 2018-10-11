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
package org.onap.so.bpmn.vcpe.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.onap.so.rest.APIResponse
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.Relationships
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import javax.ws.rs.NotFoundException
import org.json.JSONObject

import static org.apache.commons.lang3.StringUtils.isBlank


/**
 * This groovy class supports the <class>DeleteVcpeResCustService.bpmn</class> process.
 *
 * @author dm4252
 *
 */
public class DeleteVcpeResCustService extends AbstractServiceTaskProcessor {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, DeleteVcpeResCustService.class);

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
	public InitializeProcessVariables(DelegateExecution execution){
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

		msoLogger.trace("Inside preProcessRequest DeleteVcpeResCustService Request ")

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String DeleteVcpeResCustServiceRequest = execution.getVariable("bpmnRequest")
			msoLogger.debug(DeleteVcpeResCustServiceRequest)
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
			msoLogger.debug("Incoming Suppress/Disable Rollback is: " + suppressRollback)

			String productFamilyId = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.requestInfo.productFamilyId")
			execution.setVariable("productFamilyId", productFamilyId)
			msoLogger.debug("Incoming productFamilyId is: " + productFamilyId)

			// extract subscriptionServiceType
			String subscriptionServiceType = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.requestParameters.subscriptionServiceType")
			execution.setVariable("subscriptionServiceType", subscriptionServiceType)
			msoLogger.debug("Incoming subscriptionServiceType is: " + subscriptionServiceType)

			// extract cloud configuration
			String cloudConfiguration = jsonUtil.getJsonValue(DeleteVcpeResCustServiceRequest, "requestDetails.cloudConfiguration")
			execution.setVariable("cloudConfiguration", cloudConfiguration)
			msoLogger.debug("cloudConfiguration: "+ cloudConfiguration)
			String lcpCloudRegionId = jsonUtil.getJsonValue(cloudConfiguration, "lcpCloudRegionId")
			execution.setVariable("lcpCloudRegionId", lcpCloudRegionId)
			msoLogger.debug("lcpCloudRegionId: "+ lcpCloudRegionId)
			String cloudOwner = jsonUtil.getJsonValue(cloudConfiguration, "cloudOwner")
			execution.setVariable("cloudOwner", cloudOwner)
			msoLogger.debug("cloudOwner: "+ cloudOwner)
			String tenantId = jsonUtil.getJsonValue(cloudConfiguration, "tenantId")
			execution.setVariable("tenantId", tenantId)
			msoLogger.debug("tenantId: "+ tenantId)

			String sdncVersion = "1707"
			execution.setVariable("sdncVersion", sdncVersion)
			msoLogger.debug("sdncVersion: "+ sdncVersion)

			//For Completion Handler & Fallout Handler
			String requestInfo =
			"""<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>DELETE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

			execution.setVariable(Prefix+"requestInfo", requestInfo)

			msoLogger.trace("Completed preProcessRequest DeleteVcpeResCustServiceRequest Request ")

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void sendSyncResponse(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)

		msoLogger.trace("Inside sendSyncResponse of DeleteVcpeResCustService ")

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String requestId = execution.getVariable("mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse ="""{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			msoLogger.debug(" sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse)
			sendWorkflowResponse(execution, 202, syncResponse)
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	/**
	 * Gets the service instance and its related resources from aai
	 *
	 * @author cb645j
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

			if(resourceClient.exists(uri)){
				AAIResultWrapper wrapper = resourceClient.get(uri, NotFoundException.class)
				Optional<Relationships> relationships = wrapper.getRelationships()

				def (TXC_found, TXC_id) = new Tuple(false, null)
				def (BRG_found, BRG_id) = new Tuple(false, null)
				List relatedVnfIdList = []

				if(relationships.isPresent()){

					List<AAIResourceUri> vnfUris = relationships.get().getRelatedAAIUris(AAIObjectType.GENERIC_VNF)
					for(AAIResourceUri u:vnfUris){
						Map<String, String> keys = u.getURIKeys()
						String vnfId = keys.get("vnf-id")
						relatedVnfIdList.add(vnfId)
					}
					List<AAIResourceUri> arUris = relationships.get().getRelatedAAIUris(AAIObjectType.ALLOTTED_RESOURCE)
					for(AAIResourceUri u:arUris){
						String ar = resourceClient.get(u).getJson()

						def type = jsonUtil.getJsonValue(ar, "type")
						def id = jsonUtil.getJsonValue(ar, "id")

						if(type == "TunnelXConn" || type == "Tunnel XConn") {
							msoLogger.debug("TunnelXConn AR found")
							TXC_found = true
							TXC_id = id

						}else if(type == "BRG") {
							msoLogger.debug("BRG AR found")
							BRG_found = true
							BRG_id = id
						}

						execution.setVariable(Prefix+"TunnelXConn", TXC_found)
						execution.setVariable("TXC_allottedResourceId", TXC_id)
						msoLogger.debug("TXC_allottedResourceId: " + TXC_id)

						execution.setVariable(Prefix+"BRG", BRG_found)
						execution.setVariable("BRG_allottedResourceId", BRG_id)
						msoLogger.debug("BRG_allottedResourceId: " + BRG_id)

					}
				}

				execution.setVariable(Prefix+"vnfsCount", relatedVnfIdList.size())
				if(relatedVnfIdList.size() > 0) {
					execution.setVariable(Prefix+"relatedVnfIdList", relatedVnfIdList)
				}

			}else{
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
			}

		}catch(BpmnError e) {
			throw e;
		}catch(NotFoundException e) {
			msoLogger.debug("Service Instance does not exist AAI")
			exceptionUtil.buildAndThrowWorkflowException(execution, 404, "Service Instance was not found in aai")
		}catch(Exception ex) {
			String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
			msoLogger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}


	// *******************************
	//
	// *******************************
	public void prepareVnfAndModulesDelete (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		msoLogger.trace("Inside prepareVnfAndModulesDelete of DeleteVcpeResCustService ")

		try {
			List vnfList = execution.getVariable(Prefix+"relatedVnfIdList")
			int vnfsDeletedCount = execution.getVariable(Prefix+"vnfsDeletedCount")
			String vnfModelInfoString = ""
			String vnfId = ""
			if (vnfList.size() > 0 ) {
				vnfId = vnfList.get(vnfsDeletedCount.intValue())
			}

			execution.setVariable("vnfId", vnfId)
			msoLogger.debug("need to delete vnfId:" + vnfId)

			msoLogger.trace("Completed prepareVnfAndModulesDelete of DeleteVcpeResCustService ")
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected Error from method prepareVnfAndModulesDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }

	// *******************************
	//     Validate Vnf request Section -> increment count
	// *******************************
	public void validateVnfDelete (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		msoLogger.trace("Inside validateVnfDelete of DeleteVcpeResCustService ")

		try {
			int vnfsDeletedCount = execution.getVariable(Prefix+"vnfsDeletedCount")
			vnfsDeletedCount++

			execution.setVariable(Prefix+"vnfsDeletedCount", vnfsDeletedCount)

			msoLogger.debug(" ***** Completed validateVnfDelete of DeleteVcpeResCustService ***** "+" vnf # "+vnfsDeletedCount)
		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteVcpeResCustService flow. Unexpected Error from method validateVnfDelete() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	 }


	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		msoLogger.trace("Inside postProcessResponse of DeleteVcpeResCustService ")

		try {
			String source = execution.getVariable("source")
			String requestId = execution.getVariable("msoRequestId")

			String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
									xmlns:ns="http://org.onap/so/request/types/v1">
							<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
								<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
								<action>DELETE</action>
								<source>${MsoUtils.xmlEscape(source)}</source>
							   </request-info>
							<aetgt:status-message>vCPE Res Cust Service Instance has been deleted successfully.</aetgt:status-message>
							   <aetgt:mso-bpel-name>BPMN Service Instance macro action: DELETE</aetgt:mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

			// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			msoLogger.debug(xmlMsoCompletionRequest)
			execution.setVariable(Prefix+"Success", true)
			execution.setVariable(Prefix+"CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			msoLogger.debug(" SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)
		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstance flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void prepareFalloutRequest(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable(DebugFlag)
		msoLogger.trace("STARTED DeleteVcpeResCustService prepareFalloutRequest Process ")

		try {
			WorkflowException wfex = execution.getVariable("WorkflowException")
			msoLogger.debug(" Incoming Workflow Exception: " + wfex.toString())
			String requestInfo = execution.getVariable(Prefix+"requestInfo")
			msoLogger.debug(" Incoming Request Info: " + requestInfo)

			String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)

			execution.setVariable(Prefix+"falloutRequest", falloutRequest)
		} catch (Exception ex) {
			msoLogger.debug("Error Occured in DeleteVcpeResCustService prepareFalloutRequest Process " + ex.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DeleteVcpeResCustService prepareFalloutRequest Process")
		}
		msoLogger.trace("COMPLETED DeleteVcpeResCustService prepareFalloutRequest Process ")
	}


	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable(DebugFlag)
		msoLogger.trace("Inside sendSyncError() of DeleteVcpeResCustService ")

		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
				"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			msoLogger.debug(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)
		} catch (Exception ex) {
			msoLogger.debug(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}
	}

	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable(DebugFlag)
		execution.setVariable("prefix",Prefix)
		try{
			msoLogger.debug("Caught a Java Exception")
			msoLogger.debug("Started processJavaException Method")
			msoLogger.debug("Variables List: " + execution.getVariables())
			execution.setVariable(Prefix+"unexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "Rethrowing MSOWorkflowException", "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, "");
			throw b
		}catch(Exception e){
			msoLogger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable(Prefix+"unexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		msoLogger.debug("Completed processJavaException Method")
	}


}
