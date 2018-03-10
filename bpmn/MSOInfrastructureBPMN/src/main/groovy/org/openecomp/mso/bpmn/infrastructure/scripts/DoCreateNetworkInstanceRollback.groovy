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

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.UUID

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>DoCreateNetworkInstance.bpmn</class> process.
 *
 */
public class DoCreateNetworkInstanceRollback extends AbstractServiceTaskProcessor {
	String Prefix="CRENWKIR_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	def className = getClass().getSimpleName()
	
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoCreateNetworkInstanceRollback.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(Execution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "rollbackNetworkRequest", null)
		execution.setVariable(Prefix + "rollbackSDNCRequest", null)
		execution.setVariable(Prefix + "rollbackActivateSDNCRequest", null)
		execution.setVariable(Prefix + "WorkflowException", null)
		
		execution.setVariable(Prefix + "rollbackNetworkRequest", "")
		execution.setVariable(Prefix + "rollbackNetworkResponse", "")
		execution.setVariable(Prefix + "rollbackNetworkReturnCode", "")

		execution.setVariable(Prefix + "rollbackSDNCRequest", "")
		execution.setVariable(Prefix + "rollbackSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackSDNCReturnCode", "")
		
		execution.setVariable(Prefix + "rollbackActivateSDNCRequest", "")
		execution.setVariable(Prefix + "rollbackActivateSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackActivateSDNCReturnCode", "")

		execution.setVariable(Prefix + "Success", false)
		execution.setVariable(Prefix + "fullRollback", false)
		execution.setVariable(Prefix + "networkId", "")
		execution.setVariable(Prefix + "urlRollbackPoNetwork", "")

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoCreateNetworkInstanceRollback.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)
		
		utils.log("DEBUG", " ***** Inside preProcessRequest() of " + className + ".groovy ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)
			
			// GET Incoming request/variables
			String rollbackNetworkRequest = null
			String rollbackSDNCRequest = null
			String rollbackActivateSDNCRequest = null
			
			// Partial Rollback
			Map<String, String> rollbackData = execution.getVariable("rollbackData")
			if (rollbackData != null && rollbackData instanceof Map) {

					if(rollbackData.containsKey("rollbackSDNCRequest")) {
					   rollbackSDNCRequest = rollbackData["rollbackSDNCRequest"]
					}
					
					if(rollbackData.containsKey("rollbackNetworkRequest")) {
						rollbackNetworkRequest = rollbackData["rollbackNetworkRequest"]
					}

					if(rollbackData.containsKey("rollbackActivateSDNCRequest")) {
					   rollbackActivateSDNCRequest = rollbackData["rollbackActivateSDNCRequest"]
					}
					
			}
			
			execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", rollbackSDNCRequest)
			execution.setVariable(Prefix + "rollbackActivateSDNCRequest", rollbackActivateSDNCRequest)
			utils.log("DEBUG", "'rollbackData': " + '\n' + execution.getVariable("rollbackData"), isDebugEnabled)
			
			String sdncVersion = execution.getVariable("sdncVersion")
			utils.log("DEBUG", "sdncVersion? : " + sdncVersion, isDebugEnabled)

			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = execution.getVariable("URN_mso_adapters_po_auth")
			utils.log("DEBUG", " Obtained BasicAuth userid password for PO/SDNC adapter: " + basicAuthValuePO, isDebugEnabled)
			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, execution.getVariable("URN_mso_msoKey"))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)
	
			} catch (IOException ex) {
				String exceptionMessage = "Exception Encountered in DoCreateNetworkInstance, PreProcessRequest() - "
				String dataErrorMessage = exceptionMessage + " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
				utils.log("DEBUG", dataErrorMessage , isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}

			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			utils.log("DEBUG", "*** WorkflowException : " + execution.getVariable(Prefix + "WorkflowException"), isDebugEnabled)
			if(execution.getVariable(Prefix + "WorkflowException") != null) {
				// called by: DoCreateNetworkInstance, partial rollback
				execution.setVariable(Prefix + "fullRollback", false)

			} else {
			   // called by: Macro - Full Rollback, WorkflowException = null
			   execution.setVariable(Prefix + "fullRollback", true)
			
			}
			utils.log("DEBUG", "*** fullRollback? : " + execution.getVariable(Prefix + "fullRollback"), isDebugEnabled)
			
		
		} catch (BpmnError e) {
		throw e
		
		} catch (Exception ex) {
			// caught exception
			String exceptionMessage = "Exception Encountered in PreProcessRequest() of " + className + ".groovy ***** : " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		
		}

	}
	
	public void callPONetworkAdapter (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside callPONetworkAdapter() of " + className + " ***** ", isDebugEnabled)
		
		try {
			String poUrl = execution.getVariable("URN_mso_adapters_network_rest_endpoint")
			String rollbackSDNCRequest = execution.getVariable(Prefix + "rollbackSDNCRequest")
			String networkId = utils.getNodeText1(rollbackSDNCRequest, "network-id")
		
			String rollbackNetworkRequest  = execution.getVariable(Prefix + "rollbackNetworkRequest")

			String urlRollbackPoNetwork = poUrl+ "/" + networkId + "/rollback"
			utils.log("DEBUG", "'urlRollbackPoNetwork': " + urlRollbackPoNetwork, isDebugEnabled)
			execution.setVariable(Prefix + "urlRollbackPoNetwork", urlRollbackPoNetwork)

			RESTConfig config = new RESTConfig(urlRollbackPoNetwork)
			RESTClient client = new RESTClient(config).
									     addHeader("Content-Type", "application/xml").
									  addAuthorizationHeader(execution.getVariable("BasicAuthHeaderValuePO"))

		    APIResponse response = client.httpDelete(rollbackNetworkRequest)
			String responseCode = response.getStatusCode()
			String responseBody = response.getResponseBodyAsString() 
		
			execution.setVariable(Prefix + "rollbackNetworkReturnCode", responseCode)
			execution.setVariable(Prefix + "rollbackNetworkResponse", responseBody)
		
			utils.log("DEBUG", " Network Adapter rollback responseCode: " + responseCode, isDebugEnabled)
			utils.log("DEBUG", " Network Adapter rollback responseBody: " + responseBody, isDebugEnabled)
		
			
		} catch (Exception ex) {
			String exceptionMessage = "Exception Encountered in callPONetworkAdapter() of DoCreateNetworkInstanceRollback flow - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		
	}
	
	
	public void validateRollbackResponses (Execution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside validateRollbackResponses() of DoCreateNetworkInstanceRollback ***** ", isDebugEnabled)
		
		try {
			// validate PO network rollback response
			String rollbackNetworkErrorMessages = ""
			String rollbackNetworkReturnCode = "200"
			if (execution.getVariable(Prefix + "rollbackNetworkRequest") != null) {
				rollbackNetworkReturnCode = execution.getVariable(Prefix + "rollbackNetworkReturnCode")
				String rollbackNetworkResponse = execution.getVariable(Prefix + "rollbackNetworkResponse")
				utils.log("DEBUG", " NetworkRollback Code - " + rollbackNetworkReturnCode, isDebugEnabled)
				utils.log("DEBUG", " NetworkRollback Response - " + rollbackNetworkResponse, isDebugEnabled)
				if (rollbackNetworkReturnCode != "200") {
					rollbackNetworkErrorMessages = " + PO Network rollback failed. "
				} else {
					rollbackNetworkErrorMessages = " + PO Network rollback completed."
				}
			}
			
			// validate SDNC rollback response
			String rollbackSdncErrorMessages = ""
			String rollbackSDNCReturnCode = "200"
			if (execution.getVariable(Prefix + "rollbackSDNCRequest") != null) {
				rollbackSDNCReturnCode = execution.getVariable(Prefix + "rollbackSDNCReturnCode")
				String rollbackSDNCResponse = execution.getVariable(Prefix + "rollbackSDNCResponse")
				String rollbackSDNCReturnInnerCode = ""
				SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
				rollbackSDNCResponse = sdncAdapterUtils.decodeXML(rollbackSDNCResponse)
				rollbackSDNCResponse = rollbackSDNCResponse.replace("&", "&amp;").replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")
				if (rollbackSDNCReturnCode == "200") {
					if (utils.nodeExists(rollbackSDNCResponse, "response-code")) {
						rollbackSDNCReturnInnerCode = utils.getNodeText1(rollbackSDNCResponse, "response-code")
						if (rollbackSDNCReturnInnerCode == "200" || rollbackSDNCReturnInnerCode == "" || rollbackSDNCReturnInnerCode == "0") {
						    rollbackSdncErrorMessages = " + SNDC assign rollback completed."
						} else {
							rollbackSdncErrorMessages = " + SDNC assign rollback failed. "
						}
					} else {
						  rollbackSdncErrorMessages = " + SNDC assign rollback completed."
					}  
				 } else {
					  rollbackSdncErrorMessages = " + SDNC assign rollback failed. "
			     }
				utils.log("DEBUG", " SDNC assign rollback Code - " + rollbackSDNCReturnCode, isDebugEnabled)
				utils.log("DEBUG", " SDNC assign rollback  Response - " + rollbackSDNCResponse, isDebugEnabled)
			}	

			// validate SDNC activate rollback response
			String rollbackActivateSdncErrorMessages = ""
			String rollbackActivateSDNCReturnCode = "200"
			if (execution.getVariable(Prefix + "rollbackActivateSDNCRequest") != null) {
				rollbackActivateSDNCReturnCode = execution.getVariable(Prefix + "rollbackActivateSDNCReturnCode")
				String rollbackActivateSDNCResponse = execution.getVariable(Prefix + "rollbackActivateSDNCResponse")
				String rollbackActivateSDNCReturnInnerCode = ""
				rollbackActivateSDNCResponse = sdncAdapterUtils.decodeXML(rollbackActivateSDNCResponse)
				rollbackActivateSDNCResponse = rollbackActivateSDNCResponse.replace("&", "&amp;").replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")
				if (rollbackActivateSDNCReturnCode == "200") {
					if (utils.nodeExists(rollbackActivateSDNCResponse, "response-code")) {
						rollbackActivateSDNCReturnInnerCode = utils.getNodeText1(rollbackActivateSDNCResponse, "response-code")
						if (rollbackActivateSDNCReturnInnerCode == "200" || rollbackActivateSDNCReturnInnerCode == "" || rollbackActivateSDNCReturnInnerCode == "0") {
						   rollbackActivateSdncErrorMessages = " + SNDC activate rollback completed."
						} else {
							rollbackActivateSdncErrorMessages = " + SDNC activate rollback failed. "
						}
					} else {
						 rollbackActivateSdncErrorMessages = " + SNDC activate rollback completed."
					}
				} else {
					  rollbackActivateSdncErrorMessages = " + SDNC activate rollback failed. "
				}
				utils.log("DEBUG", " SDNC activate rollback Code - " + rollbackActivateSDNCReturnCode, isDebugEnabled)
				utils.log("DEBUG", " SDNC activate rollback  Response - " + rollbackActivateSDNCResponse, isDebugEnabled)
			}	


			String statusMessage = ""
			int errorCode = 7000
			utils.log("DEBUG", "*** fullRollback? : " + execution.getVariable(Prefix + "fullRollback"), isDebugEnabled)
			if (execution.getVariable(Prefix + "fullRollback") == false) { 
				// original WorkflowException, 
				WorkflowException wfe = execution.getVariable(Prefix + "WorkflowException") 
				if (wfe != null) {
				   // rollback due to failure in DoCreate - Partial rollback
				   statusMessage = wfe.getErrorMessage()
				   errorCode = wfe.getErrorCode()
				} else {
				   statusMessage = "See Previous Camunda flows for cause of Error: Undetermined Exception."
				   errorCode = '7000'
			    }

				// set if all rolledbacks are successful
				if (rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200" && rollbackActivateSDNCReturnCode == "200") {
					execution.setVariable("rolledBack", true)
					execution.setVariable("wasDeleted", true)
					
				} else {
					execution.setVariable("rolledBack", false)
					execution.setVariable("wasDeleted", true)
				}

				statusMessage = statusMessage + rollbackActivateSdncErrorMessages + rollbackNetworkErrorMessages + rollbackSdncErrorMessages
				utils.log("DEBUG", "Final DoCreateNetworkInstanceRollback status message: " + statusMessage, isDebugEnabled)
				String processKey = getProcessKey(execution)
				WorkflowException exception = new WorkflowException(processKey, errorCode, statusMessage)
				execution.setVariable("workflowException", exception)
				
			} else { 
				// rollback due to failures in Main flow (Macro) - Full rollback
				// WorkflowException = null
			    if (rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200" && rollbackActivateSDNCReturnCode == "200") {
					execution.setVariable("rollbackSuccessful", true)
					execution.setVariable("rollbackError", false)
			    } else {
				    String exceptionMessage = "Network Create Rollback was not Successful. "
                    utils.log("DEBUG", exceptionMessage, isDebugEnabled)
					execution.setVariable("rollbackSuccessful", false)
				    execution.setVariable("rollbackError", true)
					exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
					throw new BpmnError("MSOWorkflowException")
			    }
				  
			} 
			

		} catch (Exception ex) {
			String errorMessage = "See Previous Camunda flows for cause of Error: Undetermined Exception."
			String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstanceRollback flow. validateRollbackResponses() - " + errorMessage + ": " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	// *******************************
	//     Build Error Section
	// *******************************


	
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
