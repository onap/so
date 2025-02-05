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

package org.onap.so.bpmn.common.scripts

import org.onap.so.logger.LoggingAnchor
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.db.request.beans.InfraActiveRequests
import org.onap.so.db.request.client.RequestsDbClient
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.core.UrnPropertiesReader;
import java.text.SimpleDateFormat

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class FalloutHandler extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FalloutHandler.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()


    public void preProcessRequest (DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessRequest(' +'execution=' + execution.getId() +')'
        logger.trace('Entered ' + method)

        execution.setVariable("FH_success", true)

        try {
            def xml = execution.getVariable("FalloutHandlerRequest")
            logger.debug("FalloutHandler request: " + xml)

            if (utils.nodeExists(xml, "request-information")) {
                throw new BpmnError("500", "FalloutHandler subflow does not support this request type.")
            }

            //Check request_id for the incoming request type
            //For INFRA_ACTIVE_REQUESTS payload request-id IS optional (Not sure why this is option since req id is primary key ... also tried exe through SOAP UI to check if MSO code handles null like auto generated seq not it does not)
            //For ACTIVE_REQUESTS payload request-id is NOT optional
            def request_id = ""
            if (utils.nodeExists(xml, "request-id")) {
                execution.setVariable("FH_request_id",utils.getNodeText(xml,"request-id"))
            }
            logger.debug("FH_request_id: " + execution.getVariable("FH_request_id"))

            //Check if ErrorCode node exists. If yes, initialize it from request xml, if no, it will stay with defaulf value already set in initializeProcessVariables() method above.
            def errorCode = "500"
            if (utils.nodeExists(xml, "ErrorCode")) {
                if(errorCode != null && !errorCode.isEmpty()) {
                    errorCode = utils.getNodeText(xml,"ErrorCode")
                }
            }
            execution.setVariable("FH_ErrorCode", errorCode)

            //Check if ErrorMessage node exists. If yes, initialize it from request xml, if no, it will stay with defaulf value already set in initializeProcessVariables() method above.
            String errorMessage = "Internal Error occured in MSO, unable to determine error message"
            if (utils.nodeExists(xml, "ErrorMessage")) {
                if(errorCode != null && !errorCode.isEmpty()) {
                    errorMessage = utils.getNodeText(xml,"ErrorMessage")
                }
            }
            execution.setVariable("FH_ErrorMessage", errorMessage)

            //Check for Parameter List
            if (utils.nodeExists(xml, "parameter-list")) {
                def parameterList = utils.getNodeXml(xml, "parameter-list", false)
                execution.setVariable("FH_parameterList", parameterList)
            }


        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    'Caught exception in ' + method, "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            //	exceptionUtil.buildWorkflowException(execution, 2000, "Internal Error - Occured in" + method)
        }

    }

    public String updateInfraRequestDB(DelegateExecution execution){
        try {
            RequestsDbClient client = getDbClient()
            InfraActiveRequests infraRequest = client.getInfraActiveRequests(execution.getVariable("FH_request_id"), UrnPropertiesReader.getVariable("mso.adapters.requestDb.auth"), UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint"))
            if(infraRequest == null){
                infraRequest = new InfraActiveRequests();
                infraRequest.setRequestId(execution.getVariable("CMSO_request_id"))
            }
            infraRequest.setLastModifiedBy("BPMN")
            infraRequest.setStatusMessage(MsoUtils.xmlEscape(execution.getVariable("FH_ErrorMessage")))
            infraRequest.setRequestStatus("FAILED")
            infraRequest.setProgress(100)
            client.updateInfraActiveRequests(infraRequest, UrnPropertiesReader.getVariable("mso.adapters.requestDb.auth"), UrnPropertiesReader.getVariable("mso.adapters.requestDb.endpoint"))
        } catch (Exception e) {
            execution.setVariable("FH_success", false)
            logger.error("Exception Occured while updating infra request db", e)
        }
    }

    /**
     * Used to create a workflow response in success and failure cases.
     */
    public void postProcessResponse (DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.postProcessResponse(' +'execution=' + execution.getId() +')'
        logger.trace('Entered ' + method)

        try {
            Boolean success = (Boolean) execution.getVariable("FH_success")
            String out = success ? "Fallout Handler Succeeded" : "Fallout Handler Failed";

            String falloutHandlerResponse = """
					<workflow:FalloutHandlerResponse xmlns:workflow="http://org.onap/so/workflow/schema/v1">
					   <workflow:out>${MsoUtils.xmlEscape(out)}</workflow:out>
					</workflow:FalloutHandlerResponse>
				"""

            falloutHandlerResponse = utils.formatXml(falloutHandlerResponse)

            execution.setVariable("FalloutHandlerResponse", falloutHandlerResponse)
            execution.setVariable("WorkflowResponse", falloutHandlerResponse)
            execution.setVariable("FH_ResponseCode", success ? "200" : "500")
            setSuccessIndicator(execution, success)

            logger.debug("FalloutHandlerResponse =\n" + falloutHandlerResponse)
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    'Caught exception in ' + method, "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
        }
    }

    protected RequestsDbClient getDbClient(){
        return new RequestsDbClient()
    }
}
