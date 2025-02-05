/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 CMCC. All rights reserved.
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
import org.onap.so.logging.filter.base.ErrorCode

import static org.apache.commons.lang3.StringUtils.*

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.so.utils.UUIDChecker
import org.springframework.web.util.UriUtils


/**
 * This groovy class supports the <class>ScaleCustomE2EServiceInstance.bpmn</class> process.
 *
 */
public class ScaleCustomE2EServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( ScaleCustomE2EServiceInstance.class);

    String Prefix = "CRESI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()

    public void preProcessRequest(DelegateExecution execution) {
        execution.setVariable("prefix", Prefix)
        String msg = ""
        logger.trace("preProcessRequest() ")

        try {

            String siRequest = execution.getVariable("bpmnRequest")
            logger.debug(siRequest)

            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            logger.debug("Input Request:" + siRequest + " reqId:" + requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                serviceInstanceId = "NULL".toString()
            }
            logger.debug("Generated new Service Instance:" + serviceInstanceId)
            serviceInstanceId = UriUtils.encode(serviceInstanceId, "UTF-8")
            execution.setVariable("serviceInstanceId", serviceInstanceId)

            // service instance ID is also service ID
            execution.setVariable("serviceId", serviceInstanceId)
            // service instance name
            String serviceInstanceName = jsonUtil.getJsonValue(siRequest, "service.serviceInstanceName")
            execution.setVariable("serviceInstanceName", serviceInstanceName)

            // service instance name
            String serviceType = jsonUtil.getJsonValue(siRequest, "service.serviceType")
            execution.setVariable("serviceType", serviceType)

            // operationa ID (key)
            //String operationKey = UUIDChecker.generateUUID(msoLogger)
            String operationId = jsonUtil.getJsonValue(siRequest, "operationId")
            execution.setVariable("operationId", operationId)
            logger.debug("Input Request:" + siRequest + " operationId:" + operationId)


            String resources = jsonUtil.getJsonValue(siRequest, "service.resources")
            execution.setVariable("resources", resources)

            // node template UUID
            String nodeTemplateUUID = UUIDChecker.getUUID();
            execution.setVariable("nodeTemplateUUID", nodeTemplateUUID)

            //subscriberInfo
            String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "service.globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            String requestDescription = "request description for test"
            execution.setVariable("requestDescription", requestDescription)
            execution.setVariable("URN_mso_adapters_openecomp_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit preProcessRequest ")
    }

    public void sendSyncResponse(DelegateExecution execution) {
        logger.trace("sendSyncResponse ")

        try {
            String operationId = execution.getVariable("operationId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            // RESTResponse for API Handler (APIH) Reply Task
			String scaleServiceRestRequest = """{"operationId":"${operationId}"}""".trim()
            logger.debug(" sendSyncResponse to APIH:" + "\n" + scaleServiceRestRequest)
            sendWorkflowResponse(execution, 202, scaleServiceRestRequest)
            execution.setVariable("sentSyncResponse", true)

        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit sendSyncResopnse ")
    }


    public void sendSyncError(DelegateExecution execution) {
        logger.trace("sendSyncError ")

        try {
            String errorMessage = ""
            if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
                WorkflowException wfe = execution.getVariable("WorkflowException")
                errorMessage = wfe.getErrorMessage()
            } else {
                errorMessage = "Sending Sync Error."
            }

            String buildworkflowException =
                    """<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

            logger.debug(buildworkflowException)
            sendWorkflowResponse(execution, 500, buildworkflowException)

        } catch (Exception ex) {
            logger.debug(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
        }

    }

    public void prepareCompletionRequest(DelegateExecution execution) {
        logger.trace("prepareCompletion ")

        try {
            String requestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String source = execution.getVariable("source")

            String msoCompletionRequest =
                    """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
								xmlns:ns="http://org.onap/so/request/types/v1">
						<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
							<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
							<action>SCALE</action>
							<source>${MsoUtils.xmlEscape(source)}</source>
						   </request-info>
						<status-message>Service Instance was scaled successfully.</status-message>
						<serviceInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceInstanceId>
						   <mso-bpel-name>ScaleGenericALaCarteServiceInstance</mso-bpel-name>
					</aetgt:MsoCompletionRequest>"""

            // Format Response
            String xmlMsoCompletionRequest = utils.formatXml(msoCompletionRequest)

            execution.setVariable("CompleteMsoProcessRequest", xmlMsoCompletionRequest)
            logger.debug(" Overall SUCCESS Response going to CompleteMsoProcess - " + "\n" + xmlMsoCompletionRequest)

        } catch (Exception ex) {
            String msg = " Exception in prepareCompletion:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit prepareCompletionRequest ")
    }

    public void prepareFalloutRequest(DelegateExecution execution) {
        logger.trace("prepareFalloutRequest ")

        try {
            WorkflowException wfex = execution.getVariable("WorkflowException")
            logger.debug(" Input Workflow Exception: " + wfex.toString())
            String requestId = execution.getVariable("msoRequestId")
            String source = execution.getVariable("source")
            String requestInfo =
                    """<request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
					<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
					<action>SCALE</action>
					<source>${MsoUtils.xmlEscape(source)}</source>
				   </request-info>"""

            String falloutRequest = exceptionUtil.processMainflowsBPMNException(execution, requestInfo)
            execution.setVariable("falloutRequest", falloutRequest)
        } catch (Exception ex) {
            logger.debug("Exception prepareFalloutRequest:" + ex.getMessage())
            String errorException = "  Bpmn error encountered in ScaleGenericALaCarteServiceInstance flow. FalloutHandlerRequest,  buildErrorResponse() - " + ex.getMessage()
            String requestId = execution.getVariable("msoRequestId")
            String falloutRequest =
                    """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
												 xmlns:ns="http://org.onap/so/request/types/v1"
												 xmlns:wfsch="http://org.onap/so/workflow/schema/v1">
					   <request-info xmlns="http://org.onap/so/infra/vnf-request/v1">
						  <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						  <action>SCALE</action>
						  <source>UUI</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
							<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorException)}</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""

            execution.setVariable("falloutRequest", falloutRequest)
        }
        logger.trace("Exit prepareFalloutRequest ")
    }


    /**
     * Init the service Operation Status
     */
    public void prepareInitServiceOperationStatus(DelegateExecution execution){
        logger.trace("STARTED prepareInitServiceOperationStatus Process ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            //String serviceName = execution.getVariable("serviceInstanceName")
            String operationId = execution.getVariable("operationId")
            String operationType = "SCALE"
            String userId = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service scaling"
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            logger.info("DB Adapter Endpoint is: " + dbAdapterEndpoint)

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <serviceName>${MsoUtils.xmlEscape(serviceName)}</serviceName>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
            logger.info("Outgoing updateServiceOperStatusRequest: \n" + payload)
            logger.debug("Scale network service updateServiceOperStatusRequest Request: " + payload)

        }catch(Exception e){
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing prepareInitServiceOperationStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), e);
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareInitServiceOperationStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED prepareInitServiceOperationStatus Process ")
    }
}
