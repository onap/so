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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils;
import org.openecomp.mso.rest.APIResponse
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils
import groovy.xml.XmlUtil
import groovy.json.*

public class DeleteNetworkInstance extends AbstractServiceTaskProcessor {
	String Prefix="DELNI_"
	String groovyClassName = "DeleteNetworkInstance"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()


	public InitializeProcessVariables(DelegateExecution execution){
		
		execution.setVariable(Prefix + "Success", false)
		
		execution.setVariable(Prefix + "CompleteMsoProcessRequest", "")
		execution.setVariable(Prefix + "FalloutHandlerRequest", "")
		execution.setVariable(Prefix + "isSilentSuccess", false)
		
	}
	
	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************

	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest() of " + groovyClassName + " ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)
							
			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null || sdncVersion == '1610') {				
				// 'a-la-cart' default, sdncVersion = '1610' 
				execution.setVariable("sdncVersion", "1610")
				String bpmnRequest = execution.getVariable("bpmnRequest")
				// set 'disableRollback'
				if (bpmnRequest != null) {
					String disableRollback = jsonUtil.getJsonValue(bpmnRequest, "requestDetails.requestInfo.suppressRollback")
					if (disableRollback != null) {
						execution.setVariable("disableRollback", disableRollback)
						utils.log("DEBUG", "Received 'suppressRollback': " + disableRollback , isDebugEnabled)
					} else {
					    execution.setVariable("disableRollback", false)
					}
					utils.log("DEBUG", " Set 'disableRollback' : " + execution.getVariable("disableRollback") , isDebugEnabled)
				} else {
					String dataErrorMessage = " Invalid 'bpmnRequest' request."
					utils.log("DEBUG", dataErrorMessage, isDebugEnabled)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}

			} else {
			    // 'macro' test ONLY, sdncVersion = '1702'
			    utils.log("DEBUG", " 'disableRollback' : " + execution.getVariable("disableRollback") , isDebugEnabled)
			}	
			
			// get/set 'msoRequestId' and 'mso-request-id'
			String requestId = execution.getVariable("msoRequestId")
			if (requestId != null) {
				execution.setVariable("mso-request-id", requestId)
			} else {
				requestId = execution.getVariable("mso-request-id")
			}
			execution.setVariable(Prefix + "requestId", requestId)
			
			// get/set 'requestId' 
		    if (execution.getVariable("requestId") == null) {
				execution.setVariable("requestId", requestId)
			}
			
			// set action to "DELETE"
			execution.setVariable("action", "DELETE")
			
			//Place holder for additional code.

			// TODO ???
			// userParams???  1) pre-loads indicator, 2) 'auto-activation'  
			// Tag/Value parameters
			//
			// Map: 'networkInputParams': 'auto-activation''
			// Sample format? 
			// "requestParameters": {
  			//     "userParams": [	
            //          {
			//	         "name": "someUserParam1",
			//	         "value": "someValue1"
			//          }
            //     ]
		    //   }
			// 
			// String userParams = //use json util to extract "userParams"// 
	        // execution.setVariable("networkInputParams", userParams)
			// else: execution.setVariable("networkInputParams", null)
			//


		} catch (Exception ex){
			sendSyncError(execution)
			String exceptionMessage = "Exception Encountered in " + groovyClassName + ", PreProcessRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void getNetworkModelInfo (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside getNetworkModelInfo() of DeleteNetworkInstance ***** ", isDebugEnabled)
		
		try {
			
			// For Ala-Carte (sdnc = 1610): 
			// 1. the Network ModelInfo is expected to be sent 
			//     via requestDetails.modelInfo (modelType = network), ex: modelCustomizationId.
			// 2. the Service ModelInfo is expected to be sent but will be IGNORE 
			//     via requestDetails.relatedInstanceList.relatedInstance.modelInfo (modelType = service)
										 
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstance flow. getNetworkModelInfo() - " + ex.getMessage()
		   utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			
		}

	}
	
	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncResponse() of DeleteNetworkInstance ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String deleteNetworkRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSyncResponse to APIH - " + "\n" + deleteNetworkRestRequest, isDebugEnabled)

			sendWorkflowResponse(execution, 202, deleteNetworkRestRequest)

		} catch (Exception ex) {
			 // caught exception
			String exceptionMessage = "Exception Encountered in  DeleteNetworkInstance, sendSyncResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareCompletion (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside prepareCompletion() of CreateNetworkInstance ***** ", isDebugEnabled)

		try {

			String requestId = execution.getVariable("mso-request-id")
			String source = execution.getVariable(Prefix + "source")

			String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
								xmlns:ns="http://org.openecomp/mso/request/types/v1">
						<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
							<request-id>${requestId}</request-id>
							<action>DELETE</action>
							<source>VID</source>
			   			</request-info>
						<aetgt:status-message>Network has been deleted successfully.</aetgt:status-message>
			   			<aetgt:mso-bpel-name>BPMN Network action: DELETE</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

				// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			// normal path
			execution.setVariable(Prefix + "CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)
		
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in CreateNetworkInstance flow. prepareCompletion() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}
	
	public void prepareDBRequestError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequestError of DeleteNetworkInstance ***** ", isDebugEnabled)
			
			// set DB Header Authorization 
			setBasicDBAuthHeader(execution, isDebugEnabled)
			
			WorkflowException wfe = execution.getVariable("WorkflowException")
			String statusMessage = wfe.getErrorMessage()
			String requestId = execution.getVariable(Prefix +"requestId")

			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<progress></progress>
								<vnfOutputs>&lt;network-outputs xmlns="http://org.openecomp/mso/infra/vnf-request/v1" xmlns:aetgt="http://org.openecomp/mso/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"/&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   execution.setVariable(Prefix + "deleteDBRequest", dbRequest)
		   utils.log("DEBUG", " DB Adapter Request - " + "\n" + dbRequest, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in  DeleteNetworkInstance, prepareDBRequestError() - " + ex.getMessage()
			logError(exceptionMessage)
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void postProcessResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
	
		utils.log("DEBUG", " ***** Inside postProcessResponse() of DeleteNetworkInstance ***** ", isDebugEnabled)
		
		try {
			if (execution.getVariable("CMSO_ResponseCode") == "200") {
			   execution.setVariable(Prefix + "Success", true)
			   utils.log("DEBUG", " ***** DeleteNetworkInstance Success ***** ", isDebugEnabled)
			   //   Place holder for additional code.
			   
			} else {
			   execution.setVariable(Prefix + "Success", false)
			   utils.log("DEBUG", " ***** DeleteNetworkInstance Failed in CompletionMsoProces flow!. ***** ", isDebugEnabled)
			
			}   
			
	
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DeleteNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
	
	    }
	
	}


	// *******************************
	//     Build Error Section
	// *******************************

	// Prepare for FalloutHandler
	public void buildErrorResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler. *****", isDebugEnabled)

		String dbReturnCode = execution.getVariable(Prefix + "dbReturnCode")
		utils.log("DEBUG", " ***** DB Update Response Code  : " + dbReturnCode, isDebugEnabled)
		utils.log("DEBUG", " ***** DB Update Response String: " + '\n' + execution.getVariable(Prefix + "deleteDBResponse"), isDebugEnabled)

		String falloutHandlerRequest = ""
		String requestId = execution.getVariable("mso-request-id")
		String source = execution.getVariable(Prefix + "source")
		execution.setVariable(Prefix + "Success", false)
		try {
			WorkflowException wfe = execution.getVariable("WorkflowException")
			String errorCode = String.valueOf(wfe.getErrorCode())
			String errorMessage = wfe.getErrorMessage()

			falloutHandlerRequest =
			    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>DELETE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.logAudit(falloutHandlerRequest)
			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("ERROR", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstance, buildErrorResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>DELEtE</action>
					      <source>${source}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${exceptionMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>9999</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("DEBUG", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)
		}
	}


	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String deleteNetworkRestError = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSyncResponse to APIH - " + "\n" + deleteNetworkRestError, isDebugEnabled)

			sendWorkflowResponse(execution, 500, deleteNetworkRestError)

		} catch (Exception ex) {
			utils.log("DEBUG", " Sending Sync Error Activity Failed -  DeleteNetworkInstance, sendSyncError(): " + "\n" + ex.getMessage(), isDebugEnabled)
		}
	}

	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception", isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")
			
		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		utils.log("DEBUG", "Completed processJavaException Method of " + Prefix, isDebugEnabled)
	}

}
