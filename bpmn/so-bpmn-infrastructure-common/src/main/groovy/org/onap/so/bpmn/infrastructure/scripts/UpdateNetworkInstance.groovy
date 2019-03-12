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

package org.onap.so.bpmn.infrastructure.scripts;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.*

/**
 * This groovy class supports the <class>UpdateNetworkInstance.bpmn</class> process.
 *
 */
public class UpdateNetworkInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( UpdateNetworkInstance.class);
	
	String Prefix="UPDNI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public InitializeProcessVariables(DelegateExecution execution){

		execution.setVariable(Prefix + "source", "")
		execution.setVariable(Prefix + "Success", false)

		execution.setVariable(Prefix + "CompleteMsoProcessRequest", "")
		execution.setVariable(Prefix + "FalloutHandlerRequest", "")

	}


	/**
	 * This method is executed during the preProcessRequest task of the <class>UpdateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>UpdateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		logger.trace("Inside preProcessRequest() of UpdateNetworkInstance Request")

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			String sdncVersion = execution.getVariable("sdncVersion")
			if (sdncVersion == null || sdncVersion == "1610") {
				// 'a-la-cart' default, sdncVersion = '1610'
				execution.setVariable("sdncVersion", "1610")
				String bpmnRequest = execution.getVariable("bpmnRequest")
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
			    // 'macro' TEST ONLY, sdncVersion = '1702'
			    logger.debug(" \'disableRollback\' : " + execution.getVariable("disableRollback") )
			}

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

		} catch (BpmnError e) {
		    throw e;

		} catch (Exception ex){
			sendSyncError(execution)
			 // caught exception
			String exceptionMessage = "Exception Encountered in UpdateNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	public void sendSyncResponse (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		logger.trace("Inside sendSyncResponse() of UpdateNetworkInstance")

		try {
			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String updateNetworkRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			logger.debug(" sendSyncResponse to APIH - " + "\n" + updateNetworkRestRequest)
			sendWorkflowResponse(execution, 202, updateNetworkRestRequest)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstance flow. sendSyncResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}


	public void getNetworkModelInfo (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		logger.trace("Inside getNetworkModelInfo() of UpdateNetworkInstance")

		try {

			// For Ala-Carte (sdnc = 1610): 
			// 1. the Network ModelInfo is expected to be sent 
			//     via requestDetails.modelInfo (modelType = network), ex: modelCustomizationId
			// 2. the Service ModelInfo is expected to be sent but will be IGNORE 
			//     via requestDetails.relatedInstanceList.relatedInstance.modelInfo (modelType = service)
			
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstance flow. getNetworkModelInfo() - " + ex.getMessage()
		   logger.debug(exceptionMessage)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	public void sendSyncError (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		logger.trace("Inside sendSyncError() of UpdateNetworkInstance")

		try {

			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// REST Error (for API Handler (APIH) Reply Task)
			String syncError = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 500, syncError)

		} catch (Exception ex) {
			logger.debug(" Bpmn error encountered in UpdateNetworkInstance flow. sendSyncError() - " + ex.getMessage())
		}

	}

	public void prepareCompletion (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		logger.trace("Inside prepareCompletion() of UpdateNetworkInstance")

		try {

			String requestId = execution.getVariable("mso-request-id")
			String source = execution.getVariable(Prefix + "source")

			String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>UPDATE</action>
							<source>VID</source>
			   			</request-info>
						<aetgt:status-message>Network has been updated successfully.</aetgt:status-message>
			   			<aetgt:mso-bpel-name>BPMN Network action: UPDATE</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

				// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			// normal path
			execution.setVariable(Prefix + "Success", true)
			execution.setVariable(Prefix + "CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			logger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstance flow. prepareCompletion() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}




	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void postProcessResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		logger.trace("Inside postProcessResponse() of UpdateNetworkInstance")

		try {

			if (execution.getVariable("CMSO_ResponseCode") == "200") {
				execution.setVariable(Prefix + "Success", true)
				logger.trace("UpdateNetworkInstance Success ")
				//   Place holder for additional code.

			 } else {
				execution.setVariable(Prefix + "Success", false)
				logger.trace("UpdateNetworkInstance Failed in CompletionMsoProces flow!. ")

			 }


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

	    }

	}


	// *******************************
	//     Build Error Section
	// *******************************

	public void processRollbackData (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)

		logger.trace("Inside processRollbackData() of UpdateNetworkInstance")

		try {
			//execution.getVariable("orchestrationStatus")
			//execution.getVariable("networkId")
			//execution.getVariable("networkName")
			//networkOutputParams
			//rollbackData
			//rolledBack

		} catch (Exception ex) {
			logger.debug(" Bpmn error encountered in UpdateNetworkInstance flow. callDBCatalog() - " + ex.getMessage())
		}

	}

	// Prepare for FalloutHandler
	public void buildErrorResponse (DelegateExecution execution) {

		execution.setVariable("prefix", Prefix)
		
		logger.trace("Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler. ")
		
		String falloutHandlerRequest = ""
		String requestId = execution.getVariable("mso-request-id")

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
					      <action>UPDATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${MsoUtils.xmlEscape(errorCode)}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			logger.debug(falloutHandlerRequest)
			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			logger.debug("  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest)

		} catch (Exception ex) {
			String errorException = "  Bpmn error encountered in UpdateNetworkInstance flow. FalloutHandlerRequest,  buildErrorResponse() - "
			logger.debug("Exception error in UpdateNetworkInstance flow,  buildErrorResponse(): " +  ex.getMessage())
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>UPDATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorException)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			logger.debug("  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest)

		}

	}

	public void processJavaException(DelegateExecution execution){

		execution.setVariable("prefix",Prefix)
		try{
			logger.debug("Caught a Java Exception in " + Prefix)
			logger.debug("Started processJavaException Method")
			logger.debug("Variables List: " + execution.getVariables())
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

		}catch(Exception e){
			logger.debug("Caught Exception during processJavaException Method: " + e)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method" + Prefix)
		}
		logger.debug("Completed processJavaException Method in " + Prefix)
	}

}
