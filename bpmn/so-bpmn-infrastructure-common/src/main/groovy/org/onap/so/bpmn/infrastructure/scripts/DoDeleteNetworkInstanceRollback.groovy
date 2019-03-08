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

import groovy.xml.XmlUtil
import groovy.json.*
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException


import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils

import org.onap.so.logger.MessageEnum
import org.onap.so.logger.MsoLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This groovy class supports the <class>DoCreateNetworkInstanceRollback.bpmn</class> process.
 *
 */
public class DoDeleteNetworkInstanceRollback extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteNetworkInstanceRollback.class);

	String Prefix="DELNWKIR_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	VidUtils vidUtils = new VidUtils(this)
	NetworkUtils networkUtils = new NetworkUtils()
	SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

	def className = getClass().getSimpleName()

	/**
	 * This method is executed during the preProcessRequest task of the <class>DoDeleteNetworkInstanceRollback.bpmn</class> process.
	 * @param execution
	 */
	public InitializeProcessVariables(DelegateExecution execution){
		/* Initialize all the process variables in this block */

		execution.setVariable(Prefix + "WorkflowException", null)

		execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", null)
		execution.setVariable(Prefix + "rollbackDeactivateSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackDeactivateSDNCReturnCode", "")

		execution.setVariable(Prefix + "rollbackSDNCRequest", "")
		execution.setVariable(Prefix + "rollbackSDNCResponse", "")
		execution.setVariable(Prefix + "rollbackSDNCReturnCode", "")

		execution.setVariable(Prefix + "rollbackNetworkRequest", null)
		execution.setVariable(Prefix + "rollbackNetworkResponse", "")
		execution.setVariable(Prefix + "rollbackNetworkReturnCode", "")

		execution.setVariable(Prefix + "Success", false)
		execution.setVariable(Prefix + "fullRollback", false)

	}

	// **************************************************
	//     Pre or Prepare Request Section
	// **************************************************
	/**
	 * This method is executed during the preProcessRequest task of the <class>DoDeleteNetworkInstanceRollback.bpmn</class> process.
	 * @param execution
	 */
	public void preProcessRequest (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		logger.trace("Inside preProcessRequest() of " + className + ".groovy ")

		try {
			// initialize flow variables
			InitializeProcessVariables(execution)

			// GET Incoming request/variables
			String rollbackDeactivateSDNCRequest = null
			String rollbackSDNCRequest = null
			String rollbackNetworkRequest = null

			Map<String, String> rollbackData = execution.getVariable("rollbackData")
			if (rollbackData != null && rollbackData instanceof Map) {

				if(rollbackData.containsKey("rollbackDeactivateSDNCRequest")) {
				   rollbackDeactivateSDNCRequest = rollbackData["rollbackDeactivateSDNCRequest"]
				}

				if(rollbackData.containsKey("rollbackSDNCRequest")) {
					rollbackSDNCRequest = rollbackData["rollbackSDNCRequest"]
				 }

				if(rollbackData.containsKey("rollbackNetworkRequest")) {
					rollbackNetworkRequest = rollbackData["rollbackNetworkRequest"]
				 }
			}

			execution.setVariable(Prefix + "rollbackNetworkRequest", rollbackNetworkRequest)
			execution.setVariable(Prefix + "rollbackSDNCRequest", rollbackSDNCRequest)
			execution.setVariable(Prefix + "rollbackDeactivateSDNCRequest", rollbackDeactivateSDNCRequest)
			logger.debug("'rollbackData': " + '\n' + execution.getVariable("rollbackData"))

			String sdncVersion = execution.getVariable("sdncVersion")
			logger.debug("sdncVersion? : " + sdncVersion)

			// PO Authorization Info / headers Authorization=
			String basicAuthValuePO = UrnPropertiesReader.getVariable("mso.adapters.po.auth",execution)
			try {
				def encodedString = utils.getBasicAuth(basicAuthValuePO, UrnPropertiesReader.getVariable("mso.msoKey", execution))
				execution.setVariable("BasicAuthHeaderValuePO",encodedString)
				execution.setVariable("BasicAuthHeaderValueSDNC", encodedString)

			} catch (IOException ex) {
				String exceptionMessage = "Exception Encountered in DoCreateNetworkInstance, PreProcessRequest() - "
				String dataErrorMessage = exceptionMessage + " Unable to encode PO/SDNC user/password string - " + ex.getMessage()
				logger.debug(dataErrorMessage )
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, dataErrorMessage)
			}

			if (execution.getVariable("SavedWorkflowException1") != null) {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("SavedWorkflowException1"))
			} else {
				execution.setVariable(Prefix + "WorkflowException", execution.getVariable("WorkflowException"))
			}
			logger.debug("*** WorkflowException : " + execution.getVariable(Prefix + "WorkflowException"))
			if(execution.getVariable(Prefix + "WorkflowException") != null) {
				// called by: DoCreateNetworkInstance, partial rollback
				execution.setVariable(Prefix + "fullRollback", false)

			} else {
			   // called by: Macro - Full Rollback, WorkflowException = null
			   execution.setVariable(Prefix + "fullRollback", true)

			}

			logger.debug("*** fullRollback? : " + execution.getVariable(Prefix + "fullRollback"))

		} catch (BpmnError e) {
		throw e;

		} catch (Exception ex) {
			sendSyncError(execution)
			// caught exception
			String exceptionMessage = "Exception Encountered in PreProcessRequest() of " + className + ".groovy ***** : " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	public void validateRollbackResponses (DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)

		logger.trace("Inside validateRollbackResponses() of DoDeleteNetworkInstanceRollback ")

		try {

			// validate SDNC activate response
			String rollbackDeactivateSDNCMessages = ""
			String rollbackDeactivateSDNCReturnCode = "200"
			if (execution.getVariable(Prefix + "rollbackDeactivateSDNCRequest") != null) {
				rollbackDeactivateSDNCReturnCode = execution.getVariable(Prefix + "rollbackDeactivateSDNCReturnCode")
				String rollbackDeactivateSDNCResponse = execution.getVariable(Prefix + "rollbackDeactivateSDNCResponse")
				String rollbackDeactivateSDNCReturnInnerCode = ""
				rollbackDeactivateSDNCResponse = rollbackDeactivateSDNCResponse.replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")
				if (rollbackDeactivateSDNCReturnCode == "200") {
					if (utils.nodeExists(rollbackDeactivateSDNCResponse, "response-code")) {
						rollbackDeactivateSDNCReturnInnerCode = utils.getNodeText(rollbackDeactivateSDNCResponse, "response-code")
						if (rollbackDeactivateSDNCReturnInnerCode == "200" || rollbackDeactivateSDNCReturnInnerCode == "" || rollbackDeactivateSDNCReturnInnerCode == "0") {
						   rollbackDeactivateSDNCMessages = " + SNDC deactivate rollback completed."
						} else {
							rollbackDeactivateSDNCMessages = " + SDNC deactivate rollback failed. "
						}
					} else {
						 rollbackDeactivateSDNCMessages = " + SNDC deactivate rollback completed."
					}
				} else {
					  rollbackDeactivateSDNCMessages = " + SDNC deactivate rollback failed. "
				}
				logger.debug(" SDNC deactivate rollback Code - " + rollbackDeactivateSDNCReturnCode)
				logger.debug(" SDNC deactivate rollback  Response - " + rollbackDeactivateSDNCResponse)
			}

			// validate SDNC rollback response
			String rollbackSdncErrorMessages = ""
			String rollbackSDNCReturnCode = "200"
			if (execution.getVariable(Prefix + "rollbackSDNCRequest") != null) {
				rollbackSDNCReturnCode = execution.getVariable(Prefix + "rollbackSDNCReturnCode")
				String rollbackSDNCResponse = execution.getVariable(Prefix + "rollbackSDNCResponse")
				String rollbackSDNCReturnInnerCode = ""
				SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
				rollbackSDNCResponse = rollbackSDNCResponse.replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")
				if (rollbackSDNCReturnCode == "200") {
					if (utils.nodeExists(rollbackSDNCResponse, "response-code")) {
						rollbackSDNCReturnInnerCode = utils.getNodeText(rollbackSDNCResponse, "response-code")
						if (rollbackSDNCReturnInnerCode == "200" || rollbackSDNCReturnInnerCode == "" || rollbackSDNCReturnInnerCode == "0") {
							rollbackSdncErrorMessages = " + SNDC unassign rollback completed."
						} else {
							rollbackSdncErrorMessages = " + SDNC unassign rollback failed. "
						}
					} else {
						  rollbackSdncErrorMessages = " + SNDC unassign rollback completed."
					}
				 } else {
					  rollbackSdncErrorMessages = " + SDNC unassign rollback failed. "
				 }
				logger.debug(" SDNC assign rollback Code - " + rollbackSDNCReturnCode)
				logger.debug(" SDNC assign rollback  Response - " + rollbackSDNCResponse)
			}

			// validate PO network rollback response
			String rollbackNetworkErrorMessages = ""
			String rollbackNetworkReturnCode = "200"
			if (execution.getVariable(Prefix + "rollbackNetworkRequest") != null) {
				rollbackNetworkReturnCode = execution.getVariable(Prefix + "rollbackNetworkReturnCode")
				String rollbackNetworkResponse = execution.getVariable(Prefix + "rollbackNetworkResponse")
				if (rollbackNetworkReturnCode != "200") {
					rollbackNetworkErrorMessages = " + PO Network rollback failed. "
				} else {
					rollbackNetworkErrorMessages = " + PO Network rollback completed."
				}

				logger.debug(" NetworkRollback Code - " + rollbackNetworkReturnCode)
				logger.debug(" NetworkRollback Response - " + rollbackNetworkResponse)
			}

			String statusMessage = ""
			int errorCode = 7000
			logger.debug("*** fullRollback? : " + execution.getVariable(Prefix + "fullRollback"))
			if (execution.getVariable(Prefix + "fullRollback") == false) {
				WorkflowException wfe = execution.getVariable(Prefix + "WorkflowException") // original WorkflowException
				if (wfe != null) {
					statusMessage = wfe.getErrorMessage()
					errorCode = wfe.getErrorCode()
				} else {
					statusMessage = "See Previous Camunda flows for cause of Error: Undetermined Exception."
					errorCode = '7000'
				}

				// set if all rolledbacks are successful
				if (rollbackDeactivateSDNCReturnCode == "200" && rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200") {
					execution.setVariable("rolledBack", true)
					execution.setVariable("wasDeleted", true)

				} else {
					execution.setVariable("rolledBack", false)
					execution.setVariable("wasDeleted", true)
				}

				statusMessage =  statusMessage + rollbackDeactivateSDNCMessages + rollbackNetworkErrorMessages + rollbackSdncErrorMessages
				logger.debug("Final DoDeleteNetworkInstanceRollback status message: " + statusMessage)
				String processKey = getProcessKey(execution);
				WorkflowException exception = new WorkflowException(processKey, errorCode, statusMessage);
				execution.setVariable("workflowException", exception);

			} else {
				// rollback due to failures in Main flow (Macro or a-ala-carte) - Full rollback
			    if (rollbackDeactivateSDNCReturnCode == "200" && rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200") {
				    execution.setVariable("rollbackSuccessful", true)
				    execution.setVariable("rollbackError", false)
			    } else {
				    String exceptionMessage = "Network Delete Rollback was not Successful. "
                    logger.debug(exceptionMessage)
					execution.setVariable("rollbackSuccessful", false)
				    execution.setVariable("rollbackError", true)
					exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
					throw new BpmnError("MSOWorkflowException")
			    }
			}

		} catch (Exception ex) {
			String errorMessage = "See Previous Camunda flows for cause of Error: Undetermined Exception."
			String exceptionMessage = " Bpmn error encountered in DoDeleteNetworkInstanceRollback flow. validateRollbackResponses() - " + errorMessage + ": " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

		}

	}

	// *******************************
	//     Build Error Section
	// *******************************



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
