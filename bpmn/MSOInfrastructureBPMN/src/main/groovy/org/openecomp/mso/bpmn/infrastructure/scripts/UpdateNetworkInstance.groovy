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
package org.openecomp.mso.bpmn.infrastructure.scripts

import groovy.xml.XmlUtil
import groovy.json.*
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>UpdateNetworkInstance.bpmn</class> process.
 *
 */
public class UpdateNetworkInstance extends AbstractServiceTaskProcessor {
	String Prefix="UPDNI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public InitializeProcessVariables(Execution execution){

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
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest() of UpdateNetworkInstance Request ***** ", isDebugEnabled)

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
			    // 'macro' TEST ONLY, sdncVersion = '1702'
			    utils.log("DEBUG", " 'disableRollback' : " + execution.getVariable("disableRollback") , isDebugEnabled)
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
		    throw e

		} catch (Exception ex){
			sendSyncError(execution)
			 // caught exception
			String exceptionMessage = "Exception Encountered in UpdateNetworkInstance, PreProcessRequest() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}
	}

	public void sendSyncResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncResponse() of UpdateNetworkInstance ***** ", isDebugEnabled)

		try {
			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// RESTResponse (for API Handler (APIH) Reply Task)
			String updateNetworkRestRequest = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			utils.log("DEBUG", " sendSyncResponse to APIH - " + "\n" + updateNetworkRestRequest, isDebugEnabled)
			sendWorkflowResponse(execution, 202, updateNetworkRestRequest)

		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstance flow. sendSyncResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}

	}


	public void getNetworkModelInfo (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside getNetworkModelInfo() of UpdateNetworkInstance ***** ", isDebugEnabled)

		try {

			// For Ala-Carte (sdnc = 1610): 
			// 1. the Network ModelInfo is expected to be sent 
			//     via requestDetails.modelInfo (modelType = network), ex: modelCustomizationId
			// 2. the Service ModelInfo is expected to be sent but will be IGNORE 
			//     via requestDetails.relatedInstanceList.relatedInstance.modelInfo (modelType = service)
			
		} catch (Exception ex) {
			sendSyncError(execution)
		   String exceptionMessage = "Bpmn error encountered in UpdateNetworkInstance flow. getNetworkModelInfo() - " + ex.getMessage()
		   utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		   exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}


	public void sendSyncError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside sendSyncError() of UpdateNetworkInstance ***** ", isDebugEnabled)

		try {

			String requestId = execution.getVariable("mso-request-id")
			String serviceInstanceId = execution.getVariable("serviceInstanceId")

			// REST Error (for API Handler (APIH) Reply Task)
			String syncError = """{"requestReferences":{"instanceId":"${serviceInstanceId}","requestId":"${requestId}"}}""".trim()

			sendWorkflowResponse(execution, 500, syncError)

		} catch (Exception ex) {
			utils.log("DEBUG", " Bpmn error encountered in UpdateNetworkInstance flow. sendSyncError() - " + ex.getMessage(), isDebugEnabled)
		}

	}

	public void prepareDBRequestError (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		try {
			utils.log("DEBUG", " ***** Inside prepareDBRequestError() of UpdateNetworkInstance ***** ", isDebugEnabled)

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
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
								<requestId>${requestId}</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>${statusMessage}</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs>&lt;network-id&gt;${networkId}&lt;/network-id&gt;&lt;network-name&gt;${networkName}&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

		   execution.setVariable(Prefix + "createDBRequest", dbRequest)
		   utils.log("DEBUG", " DB Adapter Request - " + "\n" + dbRequest, isDebugEnabled)
		   utils.logAudit(dbRequest)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstance flow. prepareDBRequestError() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	 }

	public void prepareCompletion (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside prepareCompletion() of UpdateNetworkInstance ***** ", isDebugEnabled)

		try {

			String requestId = execution.getVariable("mso-request-id")
			String source = execution.getVariable(Prefix + "source")

			String msoCompletionRequest =
				"""<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
								xmlns:ns="http://org.openecomp/mso/request/types/v1">
						<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
							<request-id>${requestId}</request-id>
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
			utils.log("DEBUG", " Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest, isDebugEnabled)

		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstance flow. prepareCompletion() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}


	}




	// **************************************************
	//     Post or Validate Response Section
	// **************************************************

	public void postProcessResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside postProcessResponse() of UpdateNetworkInstance ***** ", isDebugEnabled)

		try {

			if (execution.getVariable("CMSO_ResponseCode") == "200") {
				execution.setVariable(Prefix + "Success", true)
				utils.log("DEBUG", " ***** UpdateNetworkInstance Success ***** ", isDebugEnabled)
				//   Place holder for additional code.

			 } else {
				execution.setVariable(Prefix + "Success", false)
				utils.log("DEBUG", " ***** UpdateNetworkInstance Failed in CompletionMsoProces flow!. ***** ", isDebugEnabled)

			 }


		} catch (Exception ex) {
			String exceptionMessage = " Bpmn error encountered in UpdateNetworkInstance flow. postProcessResponse() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

	    }

	}


	// *******************************
	//     Build Error Section
	// *******************************

	public void processRollbackData (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", " ***** Inside processRollbackData() of UpdateNetworkInstance ***** ", isDebugEnabled)

		try {
			//execution.getVariable("orchestrationStatus")
			//execution.getVariable("networkId")
			//execution.getVariable("networkName")
			//networkOutputParams
			//rollbackData
			//rolledBack

		} catch (Exception ex) {
			utils.log("DEBUG", " Bpmn error encountered in UpdateNetworkInstance flow. callDBCatalog() - " + ex.getMessage(), isDebugEnabled)
		}

	}

	// Prepare for FalloutHandler
	public void buildErrorResponse (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix", Prefix)

		utils.log("DEBUG", "DB updateInfraRequest ResponseCode: " + execution.getVariable(Prefix + "dbReturnCode"), isDebugEnabled)
		utils.log("DEBUG", "DB updateInfraRequest Response: " + execution.getVariable(Prefix + "createDBResponse"), isDebugEnabled)
		
		utils.log("DEBUG", " ***** Prepare for FalloutHandler. FAILURE - prepare request for sub-process FalloutHandler. *****", isDebugEnabled)
		
		String falloutHandlerRequest = ""
		String requestId = execution.getVariable("mso-request-id")

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
					      <action>UPDATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorMessage}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>${errorCode}</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			utils.logAudit(falloutHandlerRequest)
			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("DEBUG", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		} catch (Exception ex) {
			String errorException = "  Bpmn error encountered in UpdateNetworkInstance flow. FalloutHandlerRequest,  buildErrorResponse() - "
			utils.log("DEBUG", "Exception error in UpdateNetworkInstance flow,  buildErrorResponse(): " +  ex.getMessage(), isDebugEnabled)
			falloutHandlerRequest =
			"""<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>${requestId}</request-id>
					      <action>UPDATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>${errorException}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

			execution.setVariable(Prefix + "FalloutHandlerRequest", falloutHandlerRequest)
			utils.log("DEBUG", "  Overall Error Response going to FalloutHandler: " + "\n" + falloutHandlerRequest, isDebugEnabled)

		}

	}

	public void processJavaException(Execution execution){
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		try{
			utils.log("DEBUG", "Caught a Java Exception in " + Prefix, isDebugEnabled)
			utils.log("DEBUG", "Started processJavaException Method", isDebugEnabled)
			utils.log("DEBUG", "Variables List: " + execution.getVariables(), isDebugEnabled)
			execution.setVariable("UnexpectedError", "Caught a Java Lang Exception - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Caught a Java Lang Exception")

		}catch(Exception e){
			utils.log("DEBUG", "Caught Exception during processJavaException Method: " + e, isDebugEnabled)
			execution.setVariable("UnexpectedError", "Exception in processJavaException method - " + Prefix)  // Adding this line temporarily until this flows error handling gets updated
			exceptionUtil.buildWorkflowException(execution, 500, "Exception in processJavaException method" + Prefix)
		}
		utils.log("DEBUG", "Completed processJavaException Method in " + Prefix, isDebugEnabled)
	}

}
