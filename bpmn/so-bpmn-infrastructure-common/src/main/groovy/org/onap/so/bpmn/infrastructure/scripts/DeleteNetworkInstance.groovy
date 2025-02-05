/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.scripts

import org.onap.so.logger.LoggingAnchor
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DeleteNetworkInstance extends AbstractServiceTaskProcessor {
	String Prefix="DELNI_"
	String groovyClassName = "DeleteNetworkInstance"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()

    private static final Logger logger = LoggerFactory.getLogger( DeleteNetworkInstance.class);
	

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

		logger.trace("Inside preProcessRequest() of " + groovyClassName + "")

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
						logger.debug("Received 'suppressRollback': " + disableRollback )
					} else {
					    execution.setVariable("disableRollback", false)
					}
					logger.debug(" Set 'disableRollback' : " + execution.getVariable("disableRollback") )
				} else {
					String dataErrorMessage = " Invalid 'bpmnRequest' request."
					logger.debug(dataErrorMessage)
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
				}

			} else {
			    // 'macro' test ONLY, sdncVersion = '1702'
			    logger.debug(" 'disableRollback' : " + execution.getVariable("disableRollback") )
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
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void getNetworkModelInfo (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.trace("Inside getNetworkModelInfo() of DeleteNetworkInstance")
		
		try {
			
			// For Ala-Carte (sdnc = 1610): 
			// 1. the Network ModelInfo is expected to be sent 
			//     via requestDetails.modelInfo (modelType = network), ex: modelCustomizationId.
			// 2. the Service ModelInfo is expected to be sent but will be IGNORE 
			//     via requestDetails.relatedInstanceList.relatedInstance.modelInfo (modelType = service)
										 
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstance flow. getNetworkModelInfo() - " + ex.getMessage()
		   logger.debug(exceptionMessage)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			
		}

	}
	
	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		logger.trace("Inside sendSyncResponse() of DeleteNetworkInstance")

		try {
			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String deleteNetworkRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			logger.debug(" sendSyncResponse to APIH - " + "\n" + deleteNetworkRestRequest)

			sendWorkflowResponse(execution, 202, deleteNetworkRestRequest)

		} catch (Exception ex) {
			 // caught exception
			String exceptionMessage = "Exception Encountered in  DeleteNetworkInstance, sendSyncResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void prepareCompletion (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		logger.trace("Inside prepareCompletion() of CreateNetworkInstance")

		try {

			String requestId = execution.getVariable("mso-request-id")
			String source = execution.getVariable(Prefix + "source")

			String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
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
			logger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)
		
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in CreateNetworkInstance flow. prepareCompletion() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}
	
	public void prepareDBRequestError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		try {
			logger.trace("Inside prepareDBRequestError of DeleteNetworkInstance")
			
			// set DB Header Authorization 
			setBasicDBAuthHeader(execution, isDebugEnabled)
			
			WorkflowException wfe = execution.getVariable("WorkflowException")
			String statusMessage = wfe.getErrorMessage()
			String requestId = execution.getVariable(Prefix +"requestId")

			String dbRequest =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
								<requestId>${MsoUtils.xmlEscape(requestId)}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${MsoUtils.xmlEscape(statusMessage)}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<progress></progress>
								<vnfOutputs>&lt;network-outputs xmlns="http://org.onap/so/infra/vnf-request/v1" xmlns:aetgt="http://org.onap/so/infra/vnf-request/v1" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"/&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   execution.setVariable(Prefix + "deleteDBRequest", dbRequest)
		   logger.debug(" DB Adapter Request - " + "\n" + dbRequest)

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in  DeleteNetworkInstance, prepareDBRequestError() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void postProcessResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
	
		logger.trace("Inside postProcessResponse() of DeleteNetworkInstance")
		
		try {
			if (execution.getVariable("CMSO_ResponseCode") == "200") {
			   execution.setVariable(Prefix + "Success", true)
			   logger.trace("DeleteNetworkInstance Success")
			   //   Place holder for additional code.
			   
			} else {
			   execution.setVariable(Prefix + "Success", false)
			   logger.trace("DeleteNetworkInstance Failed in CompletionMsoProces flow!.")
			
			}   
			
	
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in DeleteNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
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

		logger.trace("Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler.")

		String dbReturnCode = execution.getVariable(Prefix + "dbReturnCode")
		logger.debug("DB Update Response Code  : " + dbReturnCode)
		logger.debug("DB Update Response String: " + '\n' + execution.getVariable(Prefix + "deleteDBResponse"))

		String falloutHandlerRequest = ""
		String requestId = execution.getVariable("mso-request-id")
		String source = execution.getVariable(Prefix + "source")
		execution.setVariable(Prefix + "Success", false)
		try {
			WorkflowException wfe = execution.getVariable("WorkflowException")
			String errorCode = String.valueOf(wfe.getErrorCode())
			String errorMessage = wfe.getErrorMessage()

			falloutHandlerRequest =
			    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>DELETE</action>
					      <source>${MsoUtils.xmlEscape(source)}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			logger.debug(falloutHandlerRequest)
			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, "BPMN",
					ErrorCode.UnknownError.getValue())

		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Bpmn error encountered in DeleteNetworkInstance, buildErrorResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>DELEtE</action>
					      <source>${MsoUtils.xmlEscape(source)}</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(exceptionMessage)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>9999</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest,"BPMN",
					ErrorCode.UnknownError.getValue(), "Exception is:\n" + ex)
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

			logger.debug(" sendSyncResponse to APIH - " + "\n" + deleteNetworkRestError)

			sendWorkflowResponse(execution, 500, deleteNetworkRestError)

		} catch (Exception ex) {
			logger.debug(" Sending Sync Error Activity Failed -  DeleteNetworkInstance, sendSyncError(): " + "\n" + ex.getMessage())
		}
	}

	public void processJavaException(DelegateExecution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			logger.debug("Caught a Java Exception")
			logger.debug("Started processJavaException Method")
			logger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")
			
		}catch(Exception e){
			logger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method")  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method")
		}
		logger.debug("Completed processJavaException Method of " + Prefix)
	}

}
