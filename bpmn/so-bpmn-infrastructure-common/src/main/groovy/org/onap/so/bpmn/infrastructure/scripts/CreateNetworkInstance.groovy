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
 * This groovy class supports the <class>CreateNetworkInstance.bpmn</class> process.
 *
 */
public class CreateNetworkInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( CreateNetworkInstance.class);

	String Prefix="CRENI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	
	public InitializeProcessVariables(DelegateExecution execution){
		
		execution.setVariable(Prefix + "source", "")
		execution.setVariable(Prefix + "Success", false)
				
		execution.setVariable(Prefix + "CompleteMsoProcessRequest", "")
		execution.setVariable(Prefix + "FalloutHandlerRequest", "")
		execution.setVariable(Prefix + "isSilentSuccess", false)
		
	}
	
	
	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>CreateNetworkInstance.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		logger.trace("Start preProcessRequest")
	
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
			    // 'macro' TEST ONLY, sdncVersion = '1702'
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
			String exceptionMessage = "Exception Encountered in CreateNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	public void sendSyncResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		logger.trace("Start sendSyncResponse")

		try {
			String requestId = execution.getVariable("mso-request-id")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String createNetworkRestRequest = """{"requestReferences":{"instanceId":"","requestId":"${requestId}"}}""".trim()

			logger.debug(" sendSyncResponse to APIH - " + "\n" + createNetworkRestRequest)
			sendWorkflowResponse(execution, 202, createNetworkRestRequest)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in CreateNetworkInstance flow. sendSyncResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}
	
	
	public void getNetworkModelInfo (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.trace("Start getNetworkModelInfo")
		
		try {
			
			// For Ala-Carte (sdnc = 1610): 
			// 1. the Network ModelInfo is expected to be sent 
			//     via requestDetails.modelInfo (modelType = network).
			// 2. the Service ModelInfo is expected to be sent but will be IGNORE 
			//     via requestDetails.relatedInstanceList.relatedInstance.modelInfo (modelType = service)
										 
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in CreateNetworkInstance flow. getNetworkModelInfo() - " + ex.getMessage()
		   logger.debug(exceptionMessage)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			
		}

	}
	
	
	public void sendSyncError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.trace("Start sendSyncError")
		
		try {

			String requestId = execution.getVariable("mso-request-id")

			// REST Error (for API Handler (APIH) Reply Task)
			String syncError = """{"requestReferences":{"instanceId":"","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 500, syncError)

		} catch (Exception ex) {
			logger.debug(" Bpmn error encountered in CreateNetworkInstance flow. sendSyncError() - " + ex.getMessage())
		}

	}

	public void prepareDBRequestError (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		try {
			logger.trace("Start prepareDBRequestError")

			// set DB Header Authorization
			setBasicDBAuthHeader(execution, isDebugEnabled)
			
			String statusMessage = ""
			WorkflowException wfe = null
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				wfe = execution.getVariable("WorkflowException")
				statusMessage = wfe.getErrorMessage()
			}
			
			String requestId = execution.getVariable(Prefix + "requestId")
			String networkName = execution.getVariable("networkName") !=null ? execution.getVariable("networkName") : ""
			String networkId = execution.getVariable("networkId") !=null ? execution.getVariable("networkId") : ""
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
								<vnfOutputs>&lt;network-id&gt;${MsoUtils.xmlEscape(networkId)}&lt;/network-id&gt;&lt;network-name&gt;${MsoUtils.xmlEscape(networkName)}&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   execution.setVariable(Prefix + "createDBRequest", dbRequest)
		   logger.debug(" DB Adapter Request - " + "\n" + dbRequest)
		   logger.debug(dbRequest)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in CreateNetworkInstance flow. prepareDBRequestError() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }
	
	public void prepareCompletion (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		logger.trace("Start prepareCompletion")

		try {

			String requestId = execution.getVariable("mso-request-id")
			String source = execution.getVariable(Prefix + "source")
			String networkId = execution.getVariable("networkId") !=null ? execution.getVariable("networkId") : ""

			String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>CREATE</action>
							<source>VID</source>
			   			</request-info>
						<aetgt:status-message>Network has been created successfully.</aetgt:status-message>
                        <aetgt:networkId>${MsoUtils.xmlEscape(networkId)}</aetgt:networkId>
			   			<aetgt:mso-bpel-name>BPMN Network action: CREATE</aetgt:mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

				// Format Response
			String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

			// normal path
			execution.setVariable(Prefix + "Success", true)
			execution.setVariable(Prefix + "CompleteMsoProcessRequest", xmlMsoCompletionRequest)
			logger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)
		
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in CreateNetworkInstance flow. prepareCompletion() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}

	
	
	
	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void postProcessResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)
	
		logger.trace("Start postProcessResponse")
		
		try {
			
			if (execution.getVariable("CMSO_ResponseCode") == "200") {
				execution.setVariable(Prefix + "Success", true)
				logger.trace("CreateNetworkInstance Success ****")
				//   Place holder for additional code.
				
			 } else {
				execution.setVariable(Prefix + "Success", false)
				logger.trace("CreateNetworkInstance Failed in CompletionMsoProces flow!. ****")
			 
			 }
				
	
		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in CreateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
	
	    }
	
	}


	// *******************************
	//     Build Error Section
	// *******************************

	public void processRollbackData (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.trace("Start processRollbackData")
	
		try {
			//execution.getVariable("orchestrationStatus")
			//execution.getVariable("networkId")
			//execution.getVariable("networkName")
			//networkOutputParams
			//rollbackData
			//rolledBack

		} catch (Exception ex) {
			logger.debug(" Bpmn error encountered in CreateNetworkInstance flow. callDBCatalog() - " + ex.getMessage())
		}
		
	}
	
	// Prepare for FalloutHandler
	public void buildErrorResponse (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		logger.debug("DB updateInfraRequest ResponseCode: " + execution.getVariable(Prefix + "dbReturnCode"))
		logger.debug("DB updateInfraRequest Response: " + execution.getVariable(Prefix + "createDBResponse"))
		
		logger.trace("Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler.")

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
					      <action>CREATE</action>
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
			String errorException = "  Bpmn error encountered in CreateNetworkInstance flow. FalloutHandlerRequest,  buildErrorResponse()"
			logger.debug("Exception error in CreateNetworkInstance flow,  buildErrorResponse(): {}", ex.getMessage(), ex)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
					                             xmlns:ns="http://org.onap/so/request/types/v1"
					                             xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					      <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					      <action>CREATE</action>
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
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
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
