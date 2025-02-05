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


import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NetworkUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.common.scripts.VidUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.logging.filter.base.ONAPComponents;

import jakarta.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution

/**
 * This groovy class supports the <class>DoCreateNetworkInstance.bpmn</class> process.
 *
 */
public class DoCreateNetworkInstanceRollback extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoCreateNetworkInstanceRollback.class);

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
    public InitializeProcessVariables(DelegateExecution execution){
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
    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        execution.setVariable("prefix",Prefix)

        logger.trace("Inside preProcessRequest() of " + className + ".groovy")

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
            // caught exception
            String exceptionMessage = "Exception Encountered in PreProcessRequest() of " + className + ".groovy ***** : " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    public void setNetworkAdapterResponseCode (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        execution.setVariable("prefix",Prefix)
        try {

            execution.setVariable(Prefix + "rollbackNetworkReturnCode", "200")

        } catch (Exception ex) {
            String exceptionMessage = "Exception Encountered in callPONetworkAdapter() of DoCreateNetworkInstanceRollback flow - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }

    }


    public void validateRollbackResponses (DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        execution.setVariable("prefix",Prefix)

        logger.trace("Inside validateRollbackResponses() of DoCreateNetworkInstanceRollback")

        try {
            // validate PO network rollback response
            String rollbackNetworkErrorMessages = ""
            String rollbackNetworkReturnCode = "200"
            if (execution.getVariable(Prefix + "rollbackNetworkRequest") != null) {
                rollbackNetworkReturnCode = execution.getVariable(Prefix + "rollbackNetworkReturnCode")
                logger.debug(" NetworkRollback Code - " + rollbackNetworkReturnCode)
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
                SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
                rollbackSDNCResponse = rollbackSDNCResponse
                rollbackSDNCResponse = rollbackSDNCResponse.replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")
                if (rollbackSDNCReturnCode == "200") {
                    if (utils.nodeExists(rollbackSDNCResponse, "response-code")) {
                        rollbackSDNCReturnInnerCode = utils.getNodeText(rollbackSDNCResponse, "response-code")
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
                logger.debug(" SDNC assign rollback Code - " + rollbackSDNCReturnCode)
                logger.debug(" SDNC assign rollback  Response - " + rollbackSDNCResponse)
            }

            // validate SDNC activate rollback response
            String rollbackActivateSdncErrorMessages = ""
            String rollbackActivateSDNCReturnCode = "200"
            if (execution.getVariable(Prefix + "rollbackActivateSDNCRequest") != null) {
                rollbackActivateSDNCReturnCode = execution.getVariable(Prefix + "rollbackActivateSDNCReturnCode")
                String rollbackActivateSDNCResponse = execution.getVariable(Prefix + "rollbackActivateSDNCResponse")
                String rollbackActivateSDNCReturnInnerCode = ""
                rollbackActivateSDNCResponse = rollbackActivateSDNCResponse
                rollbackActivateSDNCResponse = rollbackActivateSDNCResponse.replace('$', '').replace('<?xml version="1.0" encoding="UTF-8"?>', "")
                if (rollbackActivateSDNCReturnCode == "200") {
                    if (utils.nodeExists(rollbackActivateSDNCResponse, "response-code")) {
                        rollbackActivateSDNCReturnInnerCode = utils.getNodeText(rollbackActivateSDNCResponse, "response-code")
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
                logger.debug(" SDNC activate rollback Code - " + rollbackActivateSDNCReturnCode)
                logger.debug(" SDNC activate rollback  Response - " + rollbackActivateSDNCResponse)
            }


            String statusMessage = ""
            int errorCode = 7000
            logger.debug("*** fullRollback? : " + execution.getVariable(Prefix + "fullRollback"))
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
                logger.debug("Final DoCreateNetworkInstanceRollback status message: " + statusMessage)
                String processKey = getProcessKey(execution);
                WorkflowException exception = new WorkflowException(processKey, errorCode, statusMessage);
                execution.setVariable("workflowException", exception);

            } else {
                // rollback due to failures in Main flow (Macro) - Full rollback
                // WorkflowException = null
                if (rollbackNetworkReturnCode == "200" && rollbackSDNCReturnCode == "200" && rollbackActivateSDNCReturnCode == "200") {
                    execution.setVariable("rollbackSuccessful", true)
                    execution.setVariable("rollbackError", false)
                } else {
                    String exceptionMessage = "Network Create Rollback was not Successful. "
                    logger.debug(exceptionMessage)
                    execution.setVariable("rollbackSuccessful", false)
                    execution.setVariable("rollbackError", true)
                    exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)
                    throw new BpmnError("MSOWorkflowException")
                }

            }


        } catch (Exception ex) {
            String errorMessage = "See Previous Camunda flows for cause of Error: Undetermined Exception."
            String exceptionMessage = " Bpmn error encountered in DoCreateNetworkInstanceRollback flow. validateRollbackResponses() - " + errorMessage + ": " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildWorkflowException(execution, 7000, exceptionMessage)

        }

    }

    // *******************************
    //     Build Error Section
    // *******************************



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
