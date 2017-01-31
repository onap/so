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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;

import org.springframework.web.util.UriUtils;

/**
 * This groovy class supports the <class>CreateServiceInstance.bpmn</class> process.
 *
 */
public class CreateServiceInstanceInfra extends AbstractServiceTaskProcessor {

	String Prefix="CRESI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils()

	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateServiceInstance.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable("CRESI_CreateServiceInstanceJasonRequest", "")
		execution.setVariable("CRESI_globalSubscriberId", "")
		execution.setVariable("CRESI_serviceInstanceName", "")
		execution.setVariable("CRESI_newServiceInstanceId", "")
		execution.setVariable("CRESI_messageId", "")
		execution.setVariable("CRESI_requestId", "")

		execution.setVariable("CRESI_queryAAIGlobalCustomerIdUrlRequest", "")
		execution.setVariable("CRESI_queryAAIGlobalCustomerIdResponse", "")
		execution.setVariable("CRESI_queryAAIGlobalCustomerIdCode", "")

		execution.setVariable("CRESI_queryAAISINameUrlRequest", "")
		execution.setVariable("CRESI_queryAAISINamePayloadRequest", "")
		execution.setVariable("CRESI_queryAAISINameResponse", "")
		execution.setVariable("CRESI_queryAAISINameCode", "")

		execution.setVariable("CRESI_createSIinAAIUrlRequest", "")
		execution.setVariable("CRESI_createSIinAAIPayloadRequest", "")
		execution.setVariable("CRESI_createSIinAAIResponse", "")
		execution.setVariable("CRESI_createSIinAAICode", "")

		execution.setVariable("CRESI_createDBRequest", "")
		execution.setVariable("CRESI_createDBResponse", "")
		execution.setVariable("CRESI_createDBCode", "")

		execution.setVariable("CRESI_createDBInfraErrorRequest", "")
		execution.setVariable("CRESI_errorDBInfraErrorResponse", "")
		execution.setVariable("CRESI_errorDBInfraErrorErrorCode", "")

		execution.setVariable("CRESI_CompleteMsoProcessRequest", "")
		execution.setVariable("CRESI_FalloutHandlerRequest", "")
		execution.setVariable("CRESI_Success", false)
		execution.setVariable("CRESI_unexpectedError", "")

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

		utils.log("DEBUG", " ***** Inside preProcessRequest CreateServiceInstanceInfra Request ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// check for incoming json message/input
			String createServiceInstanceIncoming = execution.getVariable("bpmnRequest")
			utils.logAudit(createServiceInstanceIncoming)
			execution.setVariable("CRESI_CreateServiceInstanceJasonRequest", createServiceInstanceIncoming);
			println 'createServiceInstanceIncoming - ' + createServiceInstanceIncoming

			// extract requestId
			String requestId = execution.getVariable("att-mso-request-id")
			execution.setVariable("CRESI_requestId", requestId)

			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			execution.setVariable("serviceInstanceId", serviceInstanceId)

			String requestAction = execution.getVariable("requestAction")
			execution.setVariable("requestAction", requestAction)

			String source = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.requestInfo.source")
			execution.setVariable("CRESI_source", source)

			// extract globalSubscriberId
			String globalSubscriberId = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.subscriberInfo.globalSubscriberId")

			// prepare messageId
			String messageId = execution.getVariable("CRESI_messageId")  // for testing
			if (messageId == null || messageId == "") {
				messageId = UUID.randomUUID()
				utils.log("DEBUG", " CRESI_messageId, random generated: " + messageId, isDebugEnabled)
			} else {
				utils.log("DEBUG", " CRESI_messageId, pre-assigned: " + messageId, isDebugEnabled)
			}
			execution.setVariable("CRESI_messageId", messageId)

			// verify element global-customer-id is sent from JSON input, throw exception if missing
			if ((globalSubscriberId == null) || (globalSubscriberId.isEmpty())) {
				String dataErrorMessage = " Element 'globalSubscriberId' is missing. "
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

    		} else {
			    execution.setVariable("CRESI_globalSubscriberId", globalSubscriberId)

    		}

			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String aaiNamespace = aaiUriUtil.getNamespaceFromUri(aai_uri)
			logDebug('AAI namespace is: ' + aaiNamespace, isDebugEnabled)
			execution.setVariable("CRESI_aaiNamespace","${aaiNamespace}")

			String newServiceInstanceId = execution.getVariable("CRESI_testServiceInstanceId") // for testing variable
			if ((newServiceInstanceId) == null || (newServiceInstanceId.isEmpty())) {
				newServiceInstanceId = UUID.randomUUID().toString()
				utils.log("DEBUG", " Generated new Service Instance: " + newServiceInstanceId , isDebugEnabled)  // generated
			} else {
				utils.log("DEBUG", " Assigned new Service Instance: " + newServiceInstanceId , isDebugEnabled)
			}
			newServiceInstanceId = UriUtils.encode(newServiceInstanceId,"UTF-8")
			execution.setVariable("CRESI_newServiceInstanceId", newServiceInstanceId)

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex){
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected from method preProcessRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncResponse of CreateServiceInstanceInfra ***** ", isDebugEnabled)

		try {
			String newServiceInstanceId = execution.getVariable("CRESI_newServiceInstanceId")
			String requestId = execution.getVariable("att-mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String syncResponse ="""{"requestReferences":{"instanceId":"${newServiceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse, isDebugEnabled)
			sendWorkflowResponse(execution, 202, syncResponse)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected from method sendSyncResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}

	public void callRESTQueryAAIGlobalSubscriberId (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAICustomer of CreateServiceInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String messageId = execution.getVariable("CRESI_requestId")
			String globalSubscriberId = execution.getVariable("CRESI_globalSubscriberId")
			globalSubscriberId = UriUtils.encode(globalSubscriberId,"UTF-8")

			// Prepare AA&I url with globalCustomerId
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String queryAAISubscriberIdUrlRequest = "${aai_endpoint}${aai_uri}/${globalSubscriberId}"
			utils.logAudit(queryAAISubscriberIdUrlRequest)
			utils.log("DEBUG", " ***** AAI Query Subscriber Id Url: " + queryAAISubscriberIdUrlRequest, isDebugEnabled)
			execution.setVariable("CRESI_queryAAIGlobalCustomerIdUrlRequest", queryAAISubscriberIdUrlRequest)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
								
			RESTConfig config = new RESTConfig(queryAAISubscriberIdUrlRequest);
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse response = client.get()
			String returnCode = response.getStatusCode()
			execution.setVariable("CRESI_queryAAIGlobalCustomerIdCode", returnCode)
			utils.log("DEBUG", " ***** AAI Query Customer Subscriber Id Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()
			execution.setVariable("CRESI_queryAAIGlobalCustomerIdResponse", aaiResponseAsString)
			utils.logAudit(aaiResponseAsString)

			if (returnCode=='200') {
				// Customer found by ID. FLow to proceed.
				utils.log("DEBUG", " CRESI_queryAAIGlobalCustomerIdResponse  : " + aaiResponseAsString, isDebugEnabled)

				//TODO
				//we might verify that service-subscription with matching name exists
				//and throw error if not. If not checked, we will get exception in subsequent step on Create call
				//in 1610 we assume both customer & service subscription were pre-created

			} else {
				if (returnCode=='404') {
					String errorMessage = " Customer Global Id not found (404) in AAI, Global Subscriber id: " + globalSubscriberId
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)

				} else {
					if (aaiResponseAsString.contains("RESTFault")) {
						// build WorkflowException & throw new BpmnError("MSOWorkflowException")
						WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
						execution.setVariable("WorkflowException", workflowException)
						throw new BpmnError("MSOWorkflowException")

					} else {
					 	// aai all errors
						String errorMessage = " Unexpected Error Response from AAI during callRESTQueryAAICustomerSubcriberId() - " + returnCode
						exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMessage)
					}

				}
			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected Error during method callRESTQueryAAICustomerSubcriberId() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}


	public void callRESTQueryAAISIName (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside callRESTQueryAAISIName of CreateServiceInstanceInfra ***** " , isDebugEnabled)

		try {
			// get variables
			String messageId = execution.getVariable("CRESI_requestId")
			String globalSubscriberId = execution.getVariable("CRESI_globalSubscriberId")
			globalSubscriberId = UriUtils.encode(globalSubscriberId,"UTF-8")

			// extract serviceType
			String createServiceInstanceIncoming = execution.getVariable("CRESI_CreateServiceInstanceJasonRequest");
			String serviceInstanceName = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.requestInfo.instanceName")
			serviceInstanceName = UriUtils.encode(serviceInstanceName,'UTF-8')
			String serviceType = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.modelInfo.modelName")
			serviceType = UriUtils.encode(serviceType,'UTF-8')

			// Prepare AA&I url
			//String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getSearchNodesQueryEndpoint(execution)

			// seach by service instance name

			// Currently not designed, but this would search for service-instance-name within realm of customer:
			// https://{serverURL}/aai/{version}/business/customers/customer/{globalCustID}/service-subscriptions/service-subscription/{serviceType}/service-instances?service-instance-name={serviceInstanceName}
			//search for service-instance-name globally:
			String queryAAISINameUrlRequest = "${aai_uri}?search-node-type=service-instance&filter=service-instance-name:EQUALS:${serviceInstanceName}"
			utils.log("DEBUG", " ***** AAI Query Service Name Url: " + queryAAISINameUrlRequest, isDebugEnabled)

			utils.logAudit(queryAAISINameUrlRequest)
			execution.setVariable("CRESI_queryAAISINameUrlRequest", queryAAISINameUrlRequest)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
									
			RESTConfig config = new RESTConfig(queryAAISINameUrlRequest);
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse response = client.get()
			String returnCode = response.getStatusCode()
			execution.setVariable("CRESI_queryAAISINameCode", returnCode)
			utils.log("DEBUG", " ***** AAI Query Service Instance Name Response Code  : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()
			utils.log("DEBUG", " ***** AAI Query Service Instance Name Response : " +'\n'+ aaiResponseAsString, isDebugEnabled)

			if (returnCode=='200') {
				utils.logAudit(aaiResponseAsString)
				execution.setVariable("CRESI_queryAAISINameResponse", aaiResponseAsString)

				if ( (aaiResponseAsString != null) && (aaiResponseAsString.contains("service-instance")) ) {
					// SI with same name was found
					serviceInstanceName = UriUtils.decode(serviceInstanceName,'UTF-8')
					String errorMessage = " Stopped creating Service Instance, already exists in AAI. Service instance name: " + serviceInstanceName
					utils.log("DEBUG", errorMessage, isDebugEnabled)
					exceptionUtil.buildWorkflowException(execution, 2500, errorMessage)
				} else {
					utils.log("DEBUG", "AAI return code 200, but no content found matching ServiceInstance name", isDebugEnabled)
					//Actual meaning is 404 Not Found
					execution.setVariable("CRESI_queryAAISINameCode", '404')
				}

			} else {
			    if (returnCode=='404') {
					utils.log("DEBUG", " AAI Query return code = '404'. Proceed with the Create Service Instance !!! ", isDebugEnabled)

			    } else {
					if (aaiResponseAsString.contains("RESTFault")) {
						// build WorkflowException
						WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
						execution.setVariable("WorkflowException", workflowException)

					} else {
 			        	// aai all errors
						String errorMessage = " Unexpected Error Response from AAI  during callRESTQueryAAISIName() - " + returnCode
						exceptionUtil.buildWorkflowException(execution, 2500, errorMessage)
					}
		      }

			}

		} catch (BpmnError e) {
			throw e;

		} catch (Exception excp) {
			// try error for method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow in method callRESTQueryAAISIName(). Error was - " + excp.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void callRESTCreateServiceInstanceInAAI(Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside callRESTCreateServiceInstanceInAAI of CreateServiceInstanceInfra ***** " , isDebugEnabled)

		try {

			// get variables
			String messageId = execution.getVariable("CRESI_messageId")
			String newServiceInstanceId = execution.getVariable("CRESI_newServiceInstanceId")

			String globalSubcriberId = execution.getVariable("CRESI_globalSubscriberId")
			globalSubcriberId = UriUtils.encode(globalSubcriberId,"UTF-8")

			// get variable within incoming json
			String createServiceInstanceIncoming = execution.getVariable("CRESI_CreateServiceInstanceJasonRequest");
			String serviceType = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.requestParameters.subscriptionServiceType")
			if (serviceType != null || serviceType != "") {
				serviceType = UriUtils.encode(serviceType,"UTF-8")
			}

			String personaModelId = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.modelInfo.modelInvariantId")
			String personaModelVersion = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.modelInfo.modelVersion")
			String workloadContext = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.modelInfo.workload-context")

			String serviceInstanceName = jsonUtil.getJsonValue(createServiceInstanceIncoming, "requestDetails.requestInfo.instanceName")

			// Prepare url
			String aai_endpoint = execution.getVariable("URN_aai_endpoint")
			AaiUtil aaiUriUtil = new AaiUtil(this)
			String aai_uri = aaiUriUtil.getBusinessCustomerUri(execution)
			String createServiceInstanceAAIUrlRequest = "${aai_endpoint}${aai_uri}/${globalSubcriberId}/service-subscriptions/service-subscription/${serviceType}/service-instances/service-instance/${newServiceInstanceId}"
			utils.log("DEBUG", " ***** AAI Create Service Instance Url: " + createServiceInstanceAAIUrlRequest, isDebugEnabled)

			utils.logAudit(createServiceInstanceAAIUrlRequest)
			execution.setVariable("CRESI_createSIinAAIUrlRequest", createServiceInstanceAAIUrlRequest)

			//Prepare payload (PUT)
			String schemaVersion = aaiUriUtil.getNamespaceFromUri(aai_uri)

			String createServiceInstancePayload =
			"""<service-instance xmlns="${execution.getVariable("CRESI_aaiNamespace")}">
				<service-instance-name>${serviceInstanceName}</service-instance-name>
				<persona-model-id>${personaModelId}</persona-model-id>
				<persona-model-version>${personaModelVersion}</persona-model-version>
				</service-instance>
				""".trim()

			utils.logAudit(createServiceInstancePayload)
			execution.setVariable("CRESI_createSIinAAIPayloadRequest", createServiceInstancePayload)
			utils.log("DEBUG", " 'payload' to create Service Instance in AAI - " + "\n" + createServiceInstancePayload, isDebugEnabled)

			String basicAuthCred = utils.getBasicAuth(execution.getVariable("URN_aai_auth"),execution.getVariable("URN_mso_msoKey"))
									
			RESTConfig config = new RESTConfig(createServiceInstanceAAIUrlRequest);
			RESTClient client = new RESTClient(config).addHeader("X-TransactionId", messageId)
													  .addHeader("X-FromAppId", "MSO")
													  .addHeader("Content-Type", "application/xml")
													  .addHeader("Accept","application/xml");
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
				client.addAuthorizationHeader(basicAuthCred)
			}
			APIResponse response = client.httpPut(createServiceInstancePayload)
			String returnCode = response.getStatusCode()
			execution.setVariable("CRESI_createSIinAAICode", returnCode)
			utils.log("DEBUG", " ***** AAI Create Service Instance Response Code : " + returnCode, isDebugEnabled)

			String aaiResponseAsString = response.getResponseBodyAsString()
			utils.logAudit(aaiResponseAsString)

			if (returnCode.toInteger() > 199 && returnCode.toInteger() < 203) {
				//200 OK 201 CREATED 202 ACCEPTED
				utils.log("DEBUG", " AAI Create Service Instance Success REST Response.", isDebugEnabled)

			} else {
				if (returnCode=='404') {
					String dataErrorMessage = " AAI Create Service Instance Failed, Error 404. Proposed new Create Instance Id was: " + newServiceInstanceId
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)

				} else {
				   if (aaiResponseAsString.contains("RESTFault")) {
					   // build WorkflowException & throw new BpmnError("MSOWorkflowException")
					   WorkflowException workflowException = exceptionUtil.MapAAIExceptionToWorkflowException(aaiResponseAsString, execution)
					   execution.setVariable("WorkflowException", workflowException)
					   throw new BpmnError("MSOWorkflowException")

					   } else {
							// aai all errors
							String dataErrorMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected Error during create call in AAI - " + returnCode
							exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
					  }
				}
			}

		} catch (BpmnError e) {
		throw e;

		} catch (Exception excep) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected Error from method callRESTCreateServiceInstanceInAAI() - " + excep.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	// *******************************
	//     Build DB request Section
	// *******************************
	public void prepareDBRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequest of CreateServiceInstanceInfra ***** ", isDebugEnabled)

			String requestId = execution.getVariable("CRESI_requestId")
			String statusMessage = "Service Instance successfully created."
			String serviceInstanceId = execution.getVariable("CRESI_newServiceInstanceId")

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
								<requestStatus>COMPLETED</requestStatus>
								<progress>100</progress>
								<vnfOutputs/>
								<serviceInstanceId>${serviceInstanceId}</serviceInstanceId>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   String buildDeleteDBRequestAsString = utils.formatXml(dbRequest)
		   execution.setVariable("CRESI_createDBRequest", buildDeleteDBRequestAsString)
		   utils.logAudit(buildDeleteDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected Error from method prepareDBRequest() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	 }


	// *****************************************
	//     Prepare Completion request Section
	// *****************************************
	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse of CreateNetworkInstanceInfra ***** ", isDebugEnabled)

		try {
			// Display Success scenario for DB update Response:
			String dbReturnCode = execution.getVariable("CRESI_createDBCode")
			utils.log("DEBUG", " ***** Success DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
			utils.log("DEBUG", " ***** Success DB Update Response String: " + '\n' + execution.getVariable("CRESI_createDBResponse"), isDebugEnabled)

			if (dbReturnCode == "200") {
				String source = execution.getVariable("CRESI_source")
				String requestId = execution.getVariable("CRESI_requestId")

				String msoCompletionRequest =
					"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
									xmlns:ns="http://ecomp.att.com/mso/request/types/v1">
							<request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
								<request-id>${requestId}</request-id>
								<action>CREATE</action>
								<source>${source}</source>
				   			</request-info>
							<aetgt:status-message>Service Instance has been created successfully.</aetgt:status-message>
				   			<aetgt:mso-bpel-name>BPMN Service Instance action: CREATE</aetgt:mso-bpel-name>
						</aetgt:MsoCompletionRequest>"""

					// Format Response
					String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

					utils.logAudit(xmlMsoCompletionRequest)
					execution.setVariable("CRESI_Success", true)
					execution.setVariable("CRESI_CompleteMsoProcessRequest", xmlMsoCompletionRequest)
					utils.log("DEBUG", " SUCCESS flow, going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

			} else {
				String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected Error from DB adapter, return code: " + dbReturnCode
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, exceptionMessage)

			}

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstance flow. Unexpected Error from method postProcessResponse() - " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	// *******************************
	//     Build Error Section
	// *******************************
	public void prepareDBRequestError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside prepareDBRequestError of CreateServiceInstanceInfra ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("CRESI_requestId")
			String statusMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				statusMessage = wfe.getErrorMessage()

			} else {
				statusMessage = "Encountered Error during CreateServiceInstance proccessing. "
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
		   execution.setVariable("CRESI_createDBInfraErrorRequest", buildDBRequestAsString)
		   utils.logAudit(buildDBRequestAsString)

		} catch (Exception ex) {
			// try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateServiceInstanceInfra flow. Unexpected Error from method prepareDBRequestError() - " + ex.getMessage()
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
			String dbReturnCode = execution.getVariable("CRESI_errorDBInfraErrorErrorCode")
			utils.log("DEBUG", " ***** Error DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
			utils.log("DEBUG", " ***** Error DB Update Response String: " + '\n' + execution.getVariable("CRESI_errorDBInfraErrorResponse"), isDebugEnabled)

			String requestId = execution.getVariable("CRESI_requestId")
			String source = execution.getVariable("CRESI_source")

			execution.setVariable("CRESI_Success", false)
			String errorMessage = ""
			String errorCode = ""

			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
				errorCode = wfe.getErrorCode().toString()

			} else {
				errorMessage = "Bpmn error encountered in CreateServiceInstance flow."
				errorCode = "2500"

			}

			falloutHandlerRequest =
				"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>CREATE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.logAudit(falloutHandlerRequest)
			execution.setVariable("CRESI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("ERROR", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		} catch (Exception ex) {
			// rebuild workflow exception
			String requestId = execution.getVariable("CRESI_requestId")
			String source = execution.getVariable("CRESI_source")
			String errorException = " Build Error Response exception encountered during method buildErrorResponse(), preparing request for FalloutHandler: - " + ex.getMessage()
			utils.log("DEBUG", errorException, isDebugEnabled)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1"
					                             xmlns:ns="http://ecomp.att.com/mso/request/types/v1"
					                             xmlns:wfsch="http://ecomp.att.com/mso/workflow/schema/v1">
					   <request-info xmlns="http://ecomp.att.com/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>CREATE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://ecomp.att.com/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorException}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
			execution.setVariable("CRESI_FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("ERROR", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		}

	}

	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncError() of CreateServiceInstanceInfra ***** ", isDebugEnabled)

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
			execution.setVariable("CRESI_unexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Caught a Java Lang Exception")
		}catch(BpmnError b){
			utils.log("ERROR", "Rethrowing MSOWorkflowException", isDebugEnabled)
			throw b
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("CRESI_unexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method", isDebugEnabled)
	}


}
