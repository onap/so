/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.isBlank
import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipData
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ActivateCommunicationService extends AbstractServiceTaskProcessor {

    String Prefix="ACS_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    RequestDBUtil requestDBUtil = new RequestDBUtil()

    JsonUtils jsonUtil = new JsonUtils()

    AAIResourcesClient client = getAAIClient()

    private static final Logger logger = LoggerFactory.getLogger(ActivateCommunicationService.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix + "preProcessRequest Start")
        execution.setVariable("prefix", Prefix)
        String msg

        try {
            // check for incoming json message/input
            String siRequest = execution.getVariable("bpmnRequest")
            logger.debug(siRequest)

            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            logger.info("Input Request:" + siRequest + " reqId:" + requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                msg = "Input serviceInstanceId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                logger.info(msg)
                execution.setVariable("globalSubscriberId", "5GCustomer")
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "serviceType")
            if (isBlank(subscriptionServiceType)) {
                msg = "Input subscriptionServiceType is null"
                logger.debug(msg)
                execution.setVariable("subscriptionServiceType", "5G")
            } else {
                execution.setVariable("subscriptionServiceType", subscriptionServiceType)
            }

            String operationId = jsonUtil.getJsonValue(siRequest, "operationId")
            execution.setVariable("operationId", operationId)

            String operationType = execution.getVariable("operationType")
            execution.setVariable("operationType", operationType.toUpperCase())

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "preProcessRequest Exit")
    }


    def checkAAIOrchStatus = { DelegateExecution execution ->

        logger.debug(Prefix + "checkAAIOrchStatus Start")

        String msg
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        String operationType = execution.getVariable("operationType")

        logger.debug("serviceInstanceId: " + serviceInstanceId)

        //check the cms status
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))

            if (!client.exists(uri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
            }

            AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            if(si.isPresent()) {

                if (si.get().getOrchestrationStatus().toLowerCase() == "activated" &&
                        operationType.equalsIgnoreCase("deactivation")) {
                    logger.info("Service is in active state")
                    execution.setVariable("serviceExpectStatus", "deactivated")
                    execution.setVariable("isContinue", "true")
                    execution.setVariable("requestParam", "deactivate")

                } else if (si.get().getOrchestrationStatus().toLowerCase()  == "deactivated" &&
                        operationType.equalsIgnoreCase("activation")){
                    logger.info("Service is  in de-activated state")
                    execution.setVariable("serviceExpectStatus", "activated")
                    execution.setVariable("isContinue", "true")
                    execution.setVariable("requestParam", "activate")

                } else {
                    execution.setVariable("isContinue", "false")
                }

                RelationshipList relationshipList = si.get().getRelationshipList()
                List<Relationship> relationship
                if (relationshipList != null && (relationship = relationshipList.getRelationship()) != null
                        && relationship.size() > 0) {
                    List<RelationshipData> relationshipDatas = relationship.get(0).getRelationshipData()

                    for (RelationshipData relationshipData : relationshipDatas) {
                        execution.setVariable("e2e_" + relationshipData.getRelationshipKey(),
                                relationshipData.getRelationshipValue())
                    }
                } else {
                    msg = "the communication service has no e2e service"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
                }
            }

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in " + Prefix + "checkAAIOrchStatus: " + ex.getMessage()
            logger.info( msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "checkAAIOrchStatus Exit")
    }


    def prepareInitOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareInitOperationStatus Start")

        String serviceId = execution.getVariable("serviceInstanceId")

        // 生成 operationId
        String operationId = execution.getVariable("operationId")

        String operationType = execution.getVariable("operationType")

        OperationStatus initStatus = new OperationStatus()
        initStatus.setServiceId(serviceId)
        initStatus.setOperationId(operationId)
        initStatus.setOperation(operationType)
        initStatus.setUserId(execution.getVariable("globalSubscriberId") as String)
        initStatus.setResult("processing")
        initStatus.setProgress("0")
        initStatus.setReason("")
        initStatus.setOperationContent("communication service active operation start")

        requestDBUtil.prepareUpdateOperationStatus(execution, initStatus)

        logger.debug(Prefix + "prepareInitOperationStatus Exit")
    }


    def sendSyncResponse = { DelegateExecution execution ->
        logger.debug(Prefix + "sendSyncResponse Start")
        try {
            String operationId = execution.getVariable("operationId")

            String restRequest = """{"operationId":"${operationId}"}""".trim()
            logger.debug(" sendSyncResponse to APIH:" + "\n" + restRequest)

            sendWorkflowResponse(execution, 202, restRequest)
            execution.setVariable("sentSyncResponse", true)
        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "sendSyncResponse Exit")
    }


    def preRequestSend2NSMF = { DelegateExecution execution ->
        logger.debug(Prefix + "preRequestSend2NSMF Start")
        try {

            String e2eServiceInstanceId = execution.getVariable("e2e_service-instance.service-instance-id")
            execution.setVariable("e2eServiceInstanceId", e2eServiceInstanceId)

            String requestParam = execution.getVariable("requestParam")
            //String NSMF_endpoint = "/onap/so/infra/e2eServiceInstances/v3"
            def NSMF_endpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)
            def url = NSMF_endpoint + "/e2eServiceInstances/v3/${e2eServiceInstanceId}/${requestParam}"
            execution.setVariable("NSMF_endpoint", url)

            //get from model catalog inputs
            String payload = """
                {
                    "globalSubscriberId": "${execution.getVariable("globalSubscriberId")}",
                    "serviceType": "${execution.getVariable("subscriptionServiceType")}"
                }
            """
            execution.setVariable("CSMF_NSMFRequest", payload.replaceAll("\\s+", ""))

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in " + Prefix + "preRequestSend2NSMF. " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "preRequestSend2NSMF Exit")
    }


    def processNSMFResponse = { DelegateExecution execution ->
        logger.debug(Prefix + "processNSMFResponse Start")
        //oof
        try {
            def CSMF_NSMFResponseCode = execution.getVariable("CSMF_NSMFResponseCode") as Integer
            if (CSMF_NSMFResponseCode >= 200 && CSMF_NSMFResponseCode < 400) {
                def CSMF_NSMFResponse = execution.getVariable("CSMF_NSMFResponse") as String
                def e2eOperationId = jsonUtil.getJsonValue(CSMF_NSMFResponse, "operationId")

                execution.setVariable("e2eOperationId", e2eOperationId)
                execution.setVariable("ProcessNsmfSuccess", "OK")
            } else {
                execution.setVariable("ProcessNsmfSuccess", "ERROR")
                execution.setVariable("operationStatus", "error")
                execution.setVariable("operationContent",
                        "communication service " + execution.getVariable("operationType")
                                + " operation error: nsmf response fail")
                execution.setVariable("orchestrationStatus", "error")
            }

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in " + Prefix + "processOOFResponse. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "processNSMFResponse Exit")
    }


    def prepareUpdateOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareUpdateOperationStatus Start")
        // update status creating
        OperationStatus status = new OperationStatus()
        status.setServiceId(execution.getVariable("serviceInstanceId") as String)
        status.setOperationId(execution.getVariable("operationId") as String)
        status.setOperation(execution.getVariable("operationType") as String)
        status.setResult("processing")
        status.setProgress("20")
        status.setOperationContent("communication service "+ execution.getVariable("operationType")
                + " operation processing: waiting nsmf service create finished")
        status.setUserId(execution.getVariable("globalSubscriberId") as String)

        requestDBUtil.prepareUpdateOperationStatus(execution, status)
        logger.debug(Prefix + "prepareUpdateOperationStatus Exit")
    }


    //todo
    def prepareCallCheckProcessStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareCallCheckProcessStatus Start")

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        execution.setVariable("successConditions", successConditions)

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        execution.setVariable("errorConditions", errorConditions)

        execution.setVariable("processServiceType", "communication service")

        execution.setVariable("timeOut", 3 * 60 * 60 * 1000)

        def successParamMap = new HashMap<String, Object>()
        successParamMap.put("orchestrationStatus", execution.getVariable("serviceExpectStatus"))

        execution.setVariable("successParamMap", successParamMap)

        def errorParamMap = new HashMap<String, Object>()
        errorParamMap.put("orchestrationStatus", "error")

        execution.setVariable("errorParamMap", errorParamMap)

        def timeOutParamMap = new HashMap<String, Object>()
        timeOutParamMap.put("orchestrationStatus", "error")

        execution.setVariable("timeOutParamMap", timeOutParamMap)

        execution.setVariable("initProgress", 20)
        execution.setVariable("endProgress", 90)

        logger.debug(Prefix + "prepareCallCheckProcessStatus Exit")
    }


    /**
     * prepare update operation status to complete after NSMF process success
     * @param execution
     */
    def prepareCompleteStatus = { DelegateExecution execution ->

        logger.debug(Prefix + "prepareCompleteStatus Start")
        String operationType = execution.getVariable("operationType")
        OperationStatus status = new OperationStatus()
        status.setServiceId(execution.getVariable("serviceInstanceId") as String)
        status.setOperationId(execution.getVariable("operationId") as String)
        status.setOperation(operationType)
        status.setResult(execution.getVariable("operationStatus") as String)
        status.setProgress("100")
        status.setOperationContent(execution.getVariable("operationContent") as String)
        status.setUserId(execution.getVariable("globalSubscriberId") as String)

        requestDBUtil.prepareUpdateOperationStatus(execution, status)
        logger.debug("prepareCompleteStatus end, serviceInstanceId: " + execution.getVariable("serviceInstanceId")
                + ", operationId: " + execution.getVariable("operationId"))

        logger.debug(Prefix + "prepareCompleteStatus Exit")
    }


    /**
     * update NSMF complete status to AAI when the NSMF process finished
     * @param execution
     */
    def updateFinishStatusInAAI = { DelegateExecution execution ->
        logger.debug(Prefix + "updateFinishStatusInAAI Start")
        String msg
        try {

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            String orchestrationStatus = execution.getVariable("orchestrationStatus")
            // create service
            ServiceInstance csi = new ServiceInstance()
            csi.setOrchestrationStatus(orchestrationStatus)
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))
            client.update(uri, csi)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in complete communication service " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "updateFinishStatusInAAI Exit")
    }


    public sendSyncError = { DelegateExecution execution ->
        logger.debug("sendSyncError Start")
        try {
            String errorMessage
            if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
                WorkflowException wfe = execution.getVariable("WorkflowException") as WorkflowException
                errorMessage = wfe.getErrorMessage()
            } else {
                errorMessage = "Sending Sync Error."
            }

            String buildWorkflowException =
                    """<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

            logger.debug(buildWorkflowException)
            sendWorkflowResponse(execution, 500, buildWorkflowException)

        } catch (Exception ex) {
            logger.debug("Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
        }
        logger.debug(Prefix + "sendSyncError Exit")
    }

}
