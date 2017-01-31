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

package com.att.bpm.scripts;

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import static org.apache.commons.lang3.StringUtils.*;

import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>CreateServiceInstance.bpmn</class> process.
 */
public class DeleteServiceInstanceInfra extends AbstractServiceTaskProcessor {

	String Prefix="DELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("DELSI_DeleteServiceInstanceJasonRequest", "")
		execution.setVariable("DELSI_requestDetails", "")
		execution.setVariable("DELSI_globalSubscriberId", "")
		execution.setVariable("DELSI_serviceInstanceName", "")
		execution.setVariable("DELSI_serviceInstanceId", "")
		execution.setVariable("DELSI_serviceInstance", "")
		execution.setVariable("DELSI_messageId", "")
		execution.setVariable("DELSI_serviceType", "")

		execution.setVariable("DELSI_queryAAISINameResponse", "")
		execution.setVariable("DELSI_queryAAISINameCode", "")

		execution.setVariable("DELSI_createDBRequest", "")
		execution.setVariable("DELSI_dbResponse", "")
		execution.setVariable("DELSI_dbReturnCode", "")

		execution.setVariable("DELSI_createDBInfraErrorRequest", "")
		execution.setVariable("DELSI_errorDBInfraErrorResponse", "")
		execution.setVariable("DELSI_errorDBInfraErrorErrorCode", "")

		execution.setVariable("DELSI_CompleteMsoProcessRequest", "")
		execution.setVariable("DELSI_FalloutHandlerRequest", "")
		execution.setVariable("DELSI_Success", false)
		execution.setVariable("DELSI_unexpectedError", "")
		execution.setVariable("DELSI_siInUse", false)

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DeleteServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest DeleteServiceInstance Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String deleteServiceInstanceIncoming = execution.getVariable("bpmnRequest")
			utils.logAudit(deleteServiceInstanceIncoming)
			execution.setVariable("DELSI_DeleteServiceInstanceJasonRequest", deleteServiceInstanceIncoming);

			// extract requestId
			String requestId = execution.getVariable("att-mso-request-id")
			execution.setVariable("DELSI_requestId", requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			execution.setVariable("DELSI_serviceInstanceId", serviceInstanceId)

			String requestAction = execution.getVariable("requestAction")
			execution.setVariable("requestAction", requestAction)

			String source = jsonUtil.getJsonValue(deleteServiceInstanceIncoming, "requestDetails.requestInfo.source")
			execution.setVariable("DELSI_source", source)

			// get variables
			// extract requestDetails
			String xmlRequestDetails = vidUtils.getJsonRequestDetailstoXml(deleteServiceInstanceIncoming)
			execution.setVariable("DELSI_requestDetails", xmlRequestDetails)

			utils.log("DEBUG", "xmlRequestDetails: " + xmlRequestDetails , isDebugEnabled)

			String xmlParameters = utils.getNodeXml(xmlRequestDetails, "requestParameters", false)
			utils.log("DEBUG","xmlParameters: " +  xmlParameters , isDebugEnabled)

			String serviceType = jsonUtil.getJsonValue(deleteServiceInstanceIncoming, "requestDetails.requestParameters.subscriptionServiceType")
			execution.setVariable("DELSI_serviceType", serviceType)

			// extract globalSubscriberId
			//String globalSubscriberId = jsonUtil.getJsonValue(deleteServiceInstanceIncoming, "requestDetails.subscriberInfo.globalSubscriberId")

			// prepare messageId
			String messageId = execution.getVariable("DELSI_messageId")  // for testing
			if (messageId == null || messageId == "") {
				messageId = UUID.randomUUID()
				utils.log("DEBUG", " DELSI_messageId, random generated: " + messageId, isDebugEnabled)
			} else {
				utils.log("DEBUG", " DELSI_messageId, pre-assigned: " + messageId, isDebugEnabled)
			}
			execution.setVariable("DELSI_messageId", messageId)

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String aaiNamespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
			logDebug('AAI namespace is: ' + aaiNamespace, isDebugEnabled)
			execution.setVariable("DELSI_aaiNamespace","${aaiNamespace}")

			//Setting for Generic Sub Flows
			execution.setVariable("GENGS_type", "service-instance")

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstance flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncResponse of DeleteServiceInstance ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("att-mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstance flow. Unexpected from method sendSyncResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}


	public void processGetServiceInstanceResponse(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside processGetServiceInstanceResponse of DeleteServiceInstance ***** " , isDebugEnabled)

		try {

			//Extract Global Sub Id
			String messageId = execution.getVariable("DELSI_requestId")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			String siRelatedLink = execution.getVariable("GENGSI_siResourceLink")

			int custStart = siRelatedLink.indexOf("customer/")
			int custEnd = siRelatedLink.indexOf("/service-subscriptions")
			String globalCustId = siRelatedLink.substring(custStart + 9, custEnd)

			execution.setVariable("DELSI_globalSubscriberId",globalCustId)

			//Extract Service Type if not provided on request
			String serviceType = execution.getVariable("DELSI_serviceType")
			if(isBlank(serviceType)){
				int serviceStart = siRelatedLink.indexOf("service-subscription/")
				int serviceEnd = siRelatedLink.indexOf("/service-instances/")
				String serviceTypeEncoded = siRelatedLink.substring(serviceStart + 21, serviceEnd)

				serviceType = UriUtils.decode(serviceTypeEncoded, "UTF-8")
			}
			execution.setVariable("serviceType", serviceType)

			String serviceInstanceData = execution.getVariable("DELSI_serviceInstance");
			utils.log("DEBUG", " DELSI_serviceInstance:  " + serviceInstanceData, isDebugEnabled)

				//Confirm there are no related service instances (vnf/network or volume)
				if (utils.nodeExists(serviceInstanceData, "relationship-list")) {

					InputSource source = new InputSource(new StringReader(serviceInstanceData));
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
					Document serviceXml = docBuilder.parse(source)

					NodeList nodeList = serviceXml.getElementsByTagName("relationship")
					for (int x = 0; x < nodeList.getLength(); x++) {
						Node node = nodeList.item(x)
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) node
							def e = eElement.getElementsByTagName("related-to").item(0).getTextContent()
							if(e.equals("generic-vnf") || e.equals("l3-network")){
								utils.log("DEBUG", "ServiceInstance still has relationship(s) to OpenStack.", isDebugEnabled)
								execution.setVariable("DELSI_siInUse", true)
								//there are relationship dependencies to this Service Instance
								String errorMessage = " Stopped deleting Service Instance, it has dependencies. Service instance id: " + serviceInstanceId
								utils.log("DEBUG", errorMessage, isDebugEnabled)
								exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
							}else{
								utils.log("DEBUG", "Relationship NOT related to OpenStack", isDebugEnabled)
							}
						}
					}
				}

		} catch (BpmnError e) {
			throw e;
		} catch (Exception excp) {
			// try error for method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstance flow in method processGetServiceInstanceResponse. Error was - " + excp.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		utils.log("DEBUG", " ***** Completed processGetServiceInstanceResponse of DeleteServiceInstance ***** " , isDebugEnabled)
	}

	// *******************************
	//     Build DB request Section
	// *******************************
	public void prepareDBRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequest of DeleteServiceInstance ***** ", isDebugEnabled)

			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = "Service Instance successfully deleted."

			//TODO - verify the format for Service Instance Delete,
			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://com.att.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>COMPLETED</requestStatus>
								<progress>100</progress>
								<vnfOutputs/>
							</ns:updateInfraRequest>
						   </soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("DELSI_createDBRequest", buildDeleteDBRequestAsString)
		   utils.logAudit(buildDeleteDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstance flow. Unexpected Error from method prepareDBRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }


	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse of DeleteServiceInstanceInfra ***** ", isDebugEnabled)

		try {
			// Display Success scenario for DB update Response:
			String dbReturnCode = execution.getVariable("DELSI_dbReturnCode")
			utils.log("DEBUG", " ***** Success DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
			utils.log("DEBUG", " ***** Success DB Update Response String: " + '\n' + execution.getVariable("DELSI_dbResponse"), isDebugEnabled)

			if (dbReturnCode == "200") {
				String source = execution.getVariable("DELSI_source")
				String requestId = execution.getVariable("DELSI_requestId")

				String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
									xmlns:ns="http://ecomp.att.com/mso/request/types/v1">
							<request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
								<request-id>${requestId}</request-id>
								<action>DELETE</action>
								<source>${source}</source>
							   </request-info>
							   <aetgt:status-message>Service Instance has been deleted successfully.</aetgt:status-message>
							   <aetgt:mso-bpel-name>BPMN Service Instance action: DELETE</aetgt:mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

					// Format Response
					String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

					utils.logAudit(xmlMsoCompletionRequest)
					execution.setVariable("DELSI_Success", true)
					execution.setVariable("DELSI_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
					utils.log("DEBUG", " SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

			} else {
				String exceptionMessage = "Bpmn error encountered in DeleteServiceInstanceInfra flow. Unexpected Error from DB adapter, return code: " + dbReturnCode
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, exceptionMessage)

			}

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstanceInfra flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	// *******************************
	//     Build Error Section
	// *******************************
	public void prepareDBRequestError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareDBRequestError of DeleteServiceInstanceInfra ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("DELSI_requestId")
			String statusMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				statusMessage = wfe.getErrorMessage()

			} else {
				statusMessage = "Encountered Error during DeleteServiceInstanceInfra proccessing. "
			}

			//TODO - verify the format for Service Instance Create,
			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://com.att.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs/>
							</ns:updateInfraRequest>
						   </soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("DELSI_createDBInfraErrorRequest", buildDBRequestAsString)
		   utils.logAudit(buildDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in DeleteServiceInstanceInfra flow. Unexpected Error from method prepareDBRequestError() - " + ex.getMessage()
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	// Prepare for FalloutHandler
	public void buildErrorResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("ERROR", " ***** Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler. *****", isDebugEnabled)

		String falloutHandlerRequest = ""
		try {
			// Display Success scenario for DB update Response:
			String dbReturnCode = execution.getVariable("DELSI_errorDBInfraErrorErrorCode")
			utils.log("DEBUG", " ***** Error DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
			utils.log("DEBUG", " ***** Error DB Update Response String: " + '\n' + execution.getVariable("DELSI_errorDBInfraErrorResponse"), isDebugEnabled)

			String requestId = execution.getVariable("DELSI_requestId")
			String source = execution.getVariable("DELSI_source")

			execution.setVariable("DELSI_Success", false)
			String errorMessage = ""
			String errorCode = ""

			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
				errorCode = wfe.getErrorCode().toString()

			} else {
				errorMessage = "Bpmn error encountered in DeleteServiceInstanceInfra flow."
				errorCode = "2500"

			}

			falloutHandlerRequest =
				"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
												 xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
												 xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
						  <request-id>${requestId}</request-id>
						  <action>DELETE</action>
						  <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.logAudit(falloutHandlerRequest)
			execution.setVariable("DELSI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("ERROR", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		} catch (Exception ex) {
			// rebuild workflow exception
			String requestId = execution.getVariable("DELSI_requestId")
			String source = execution.getVariable("DELSI_source")
			String errorException = " Build Error Response exception encountered during method buildErrorResponse(), preparing request for FalloutHandler: - " + ex.getMessage()
			utils.log("DEBUG", errorException, isDebugEnabled)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
												 xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
												 xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
						  <request-id>${requestId}</request-id>
						  <action>DELETE</action>
						  <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorException}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
			execution.setVariable("DELSI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("ERROR", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		}

	}

	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

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
				"""<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
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
			execution.setVariable("DELSI_unexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("DELSI_unexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}

}
