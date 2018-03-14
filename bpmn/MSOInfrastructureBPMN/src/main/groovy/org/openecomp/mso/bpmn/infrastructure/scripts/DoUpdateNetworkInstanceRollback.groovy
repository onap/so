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

import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.common.scripts.AaiUtil
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.NetworkUtils
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.common.scripts.VidUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.rest.APIResponse

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

/**
 * This groovy class supports the <class>DoCreateNetworkInstance.bpmn</class> process.
 *
 */
public class DoUpdateNetworkInstanceRollback extends AbstractServiceTaskProcessor {
	String Prefix="UPDNETIR_"
   	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	def className = getClass().getSimpleName()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DoUpdateNetworkInstanceRollback.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(DelegateExecution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "rollbackNetworkRequest", null)
		execution.setVariable(Prefix + "rollbackSDNCRequest", null)
		execution.setVariable(Prefix + "WorkflowException", null)

		execution.setVariable(Prefix + "rollbackNetworkRequest", "")
		execution.setVariable(Prefix + "rollbackNetworkResponse", "")
		execution.setVariable(Prefix + "rollbackNetworkReturnCode", "")

		execution.setVariable(Prefix + "rollbackSDNCRequest", "")
		execution.setVariable(Prefix + "rollbackSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackSDNCReturnCode", "")

		execution.setVariable(Prefix + "Success", false)
		execution.setVariable(Prefix + "fullRollback", false)

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoUpdateNetworkInstanceRollback.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside preProcessRequest() of " + className + ".groovy ***** ", isDebugEnabled)

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// GET Incoming request/variables
			String rollbackNetworkRequest = null
			String rollbackSDNCRequest = null

			// Partial Rollback
			Map<String, String> rollbackData = execution.getVariable("rollbackData")
			if (rollbackData != null && rollbackData instanceof Map) {

					if(rollbackData.containsKey("rollbackNetworkRequest")) {
						rollbackNetworkRequest = rollbackData["rollbackNetworkRequest"]
					}

					if(rollbackData.containsKey("rollbackSDNCRequest")) {
					   rollbackSDNCRequest = rollbackData["rollbackSDNCRequest"]
					}
			}

			execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", rollbackSDNCRequest)
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
		throw e;

		} catch (Exception ex) {
			sendSyncError(execution)
			// caught exception
			String exceptionMessage = "Exception Encountered in PreProcessRequest() of " + className + ".groovy ***** : " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void validateRollbackResponses (DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		execution.setVariable("prefix",Prefix)

		utils.log("DEBUG", " ***** Inside validateRollbackResponses() of DoUpdateNetworkInstanceRollback ***** ", isDebugEnabled)

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
						    rollbackSdncErrorMessages = " + SNDC changeassign rollback completed."
						} else {
							rollbackSdncErrorMessages = " + SDNC changeassign rollback failed. "
						}
					} else {
						  rollbackSdncErrorMessages = " + SNDC changeassign rollback completed."
					}
				 } else {
					  rollbackSdncErrorMessages = " + SDNC changeassign rollback failed. "
			     }
				utils.log("DEBUG", " SDNC changeassign rollback Code - " + rollbackSDNCReturnCode, isDebugEnabled)
				utils.log("DEBUG", " SDNC changeassign rollback  Response - " + rollbackSDNCResponse, isDebugEnabled)
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
				if (rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200") {
					execution.setVariable("rolledBack", true)

				} else {
					execution.setVariable("rolledBack", false)

				}

				statusMessage = statusMessage + rollbackNetworkErrorMessages + rollbackSdncErrorMessages
				utils.log("DEBUG", "Final DoUpdateNetworkInstanceRollback status message: " + statusMessage, isDebugEnabled)
				String processKey = getProcessKey(execution);
				WorkflowException exception = new WorkflowException(processKey, errorCode, statusMessage);
				execution.setVariable("workflowException", exception);

			} else {
				// rollback due to failures in Main flow (Macro) - Full rollback
				// WorkflowException = null
				if (rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200") {
					execution.setVariable("rollbackSuccessful", true)
					execution.setVariable("rollbackError", false)
				} else {
					String exceptionMessage = "Network Update Rollback was not Successful. "
					utils.log("DEBUG", exceptionMessage, isDebugEnabled)
					execution.setVariable("rollbackSuccessful", false)
					execution.setVariable("rollbackError", true)
					exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
					throw new BpmnError("MSOWorkflowException")
				}

			}


		} catch (Exception ex) {
			execution.setVariable("WorkflowException", null)
			String errorMessage = "See Previous Camunda flows for cause of Error: Undetermined Exception."
			String exceptionMessage = " Bpmn error encountered in DoUpdateNetworkInstanceRollback flow. validateRollbackResponses() - " + errorMessage + ": " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
			execution.setVariable("WorkflowException", execution.getVariable("WorkflowException"))

		}

	}

	// *******************************
	//     Build Error Section
	// *******************************



	public void processJavaException(DelegateExecution execution){
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
