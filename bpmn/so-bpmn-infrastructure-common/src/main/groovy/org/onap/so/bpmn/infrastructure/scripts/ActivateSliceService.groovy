/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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


import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.*
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.beans.nsmf.CustomerInfo
import org.onap.so.beans.nsmf.NetworkType
import org.onap.so.beans.nsmf.NssInstance
import org.onap.so.beans.nsmf.OperationType
import org.onap.so.beans.nsmf.OrchestrationStatusEnum
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.ws.rs.NotFoundException
import java.util.function.Consumer

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>ActivateSliceService.bpmn</class> process.
 * AlaCarte flow for 1702 slice service activate
 *
 */

class ActivateSliceService extends AbstractServiceTaskProcessor {


    String Prefix = "ACTSS_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    RequestDBUtil requestDBUtil = new RequestDBUtil()

    AAIResourcesClient client = getAAIClient()

    private static final Logger logger = LoggerFactory.getLogger(ActivateSliceService.class)

    void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix + "preProcessRequest Start")
        execution.setVariable("prefix", Prefix)
        String msg

        try {
            // check for incoming json message/input
            String siRequest = Objects.requireNonNull(execution.getVariable("bpmnRequest"))
            logger.debug(siRequest)

            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            logger.info("Input Request:" + siRequest + " reqId:" + requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                msg = "Input serviceInstanceId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            String source = jsonUtil.getJsonValue(siRequest, "source")
            execution.setVariable("source", source)

            //subscriberInfo
            String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                logger.info(msg)
                execution.setVariable("globalSubscriberId", "5GCustomer")
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            //requestParameters
            String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "serviceType")
            if (isBlank(subscriptionServiceType)) {
                msg = "Input subscriptionServiceType is null"
                logger.debug(msg)
                execution.setVariable("subscriptionServiceType", "5G")
            } else {
                execution.setVariable("subscriptionServiceType", subscriptionServiceType)
            }
            String operationId = Objects.requireNonNull(jsonUtil.getJsonValue(siRequest, "operationId"))
            execution.setVariable("operationId", operationId)

            String operationType = Objects.requireNonNull(execution.getVariable("operationType"))
            logger.info("operationType is " + execution.getVariable("operationType") )

            CustomerInfo customerInfo = CustomerInfo.builder().operationId(operationId)
                    .operationType(Objects.requireNonNull(OperationType.getOperationType(operationType)))
                    .globalSubscriberId(globalSubscriberId).serviceInstanceId(serviceInstanceId)
                    .subscriptionServiceType(subscriptionServiceType)
                    .build()

            execution.setVariable("customerInfo", customerInfo)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "preProcessRequest Exit")
    }

    /**
     * Init the service Operation Status
     */
    def prepareInitServiceOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareActivateServiceOperationStatus Start")
        try {
            CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
            String serviceId = customerInfo.getServiceInstanceId()
            String operationId = customerInfo.getOperationId()
            String operationType = customerInfo.getOperationType().getType()
            String userId = customerInfo.getGlobalSubscriberId()
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service activation"

            execution.setVariable("e2eserviceInstanceId", serviceId)
            //execution.setVariable("operationType", operationType)

            OperationStatus initStatus = new OperationStatus()
            initStatus.setServiceId(serviceId)
            initStatus.setOperationId(operationId)
            initStatus.setOperation(operationType)
            initStatus.setUserId(userId)
            initStatus.setResult(result)
            initStatus.setProgress(progress)
            initStatus.setReason(reason)
            initStatus.setOperationContent(operationContent)

            requestDBUtil.prepareUpdateOperationStatus(execution, initStatus)

        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Exception Occured Processing prepareInitServiceOperationStatus.", "BPMN",
                    ErrorCode.UnknownError.getValue(), "Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse",
                    "Error Occurred during prepareInitServiceOperationStatus Method:\n" + e.getMessage())
        }
        logger.debug(Prefix + "prepareInitServiceOperationStatus Exit")
    }


    def sendSyncResponse = { DelegateExecution execution ->
        logger.debug(Prefix + "sendSyncResponse Start")
        try {
            CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
            String operationId = customerInfo.getOperationId()

            // RESTResponse for API Handler (APIH) Reply Task
            String Activate5GsliceServiceRestRequest = """{"operationId":"${operationId}"}""".trim()
            logger.debug(" sendSyncResponse to APIH:" + "\n" + Activate5GsliceServiceRestRequest)

            sendWorkflowResponse(execution, 202, Activate5GsliceServiceRestRequest)
            execution.setVariable("sentSyncResponse", true)
        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "sendSyncResponse Exit")
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

    def checkAAIOrchStatusOfE2ESlice = { DelegateExecution execution ->
        logger.debug(Prefix + "CheckAAIOrchStatus Start")
        execution.setVariable("isContinue", "false")
        CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
        String msg
        String serviceInstanceId = customerInfo.serviceInstanceId
        String globalSubscriberId = customerInfo.globalSubscriberId
        String subscriptionServiceType = customerInfo.subscriptionServiceType

        logger.debug("serviceInstanceId: " + serviceInstanceId)

        //check the e2e slice status
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(globalSubscriberId)
                    .serviceSubscription(subscriptionServiceType)
                    .serviceInstance(serviceInstanceId))

            AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            ServiceInstance serviceInstance = si.orElseThrow()

            boolean isContinue = handleOperation(customerInfo, serviceInstance)
            execution.setVariable("isContinue", isContinue)
            customerInfo.setSnssai(serviceInstance.getEnvironmentContext())

            execution.setVariable("customerInfo", customerInfo)
            execution.setVariable("ssInstance", serviceInstance)
            execution.setVariable("ssiUri", uri)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            execution.setVariable("isContinue", "false")
            msg = "Exception in org.onap.so.bpmn.common.scripts.CompleteMsoProcess.CheckAAIOrchStatus, " +
                    "Requested e2eservice does not exist: " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "CheckAAIOrchStatus Exit")
    }

    static boolean handleOperation(CustomerInfo customerInfo, ServiceInstance serviceInstance) {
        OperationType operationType = customerInfo.operationType
        OrchestrationStatusEnum status = OrchestrationStatusEnum.getStatus(Objects.requireNonNull(
                serviceInstance.getOrchestrationStatus()))

        return ((OrchestrationStatusEnum.ACTIVATED == status && OperationType.DEACTIVATE == operationType)
            || (OrchestrationStatusEnum.DEACTIVATED == status && OperationType.ACTIVATE == operationType))
    }

    void checkAAIOrchStatusOfAllocates(DelegateExecution execution) {
        logger.debug(Prefix + "CheckAAIOrchStatus Start")
        CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
        String msg
        String serviceInstanceId = customerInfo.serviceInstanceId
        String globalSubscriberId = customerInfo.globalSubscriberId
        String subscriptionServiceType = customerInfo.subscriptionServiceType

        logger.debug("serviceInstanceId: " + serviceInstanceId)

        //check the NSI is exist or the status of NSI is active or de-active
        try {

            //get the allotted-resources by e2e slice id
            AAIPluralResourceUri uriAllotted = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(globalSubscriberId)
                    .serviceSubscription(subscriptionServiceType)
                    .serviceInstance(serviceInstanceId)
                    .allottedResources()
            )

            AAIResultWrapper wrapperAllotted = client.get(uriAllotted, NotFoundException.class)
            Optional<AllottedResources> allAllotted = wrapperAllotted.asBean(AllottedResources.class)

            AllottedResources allottedResources = allAllotted.get()
            List<AllottedResource> AllottedResourceList = allottedResources.getAllottedResource()
            if (AllottedResourceList.isEmpty()) {
                execution.setVariable("isContinue", "false")
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                        "allottedResources in aai is empty")
            }
            AllottedResource ar = AllottedResourceList.first()
            String relatedLink = ar.getRelationshipList().getRelationship().first().getRelatedLink()
            String nsiServiceId = relatedLink.substring(relatedLink.lastIndexOf("/") + 1, relatedLink.length())
            customerInfo.setNsiId(nsiServiceId)
            execution.setVariable("customerInfo", customerInfo)
            logger.info("the NSI ID is:" + nsiServiceId)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            logger.info("NSI Service doesnt exist")
            execution.setVariable("isContinue", "false")
            msg = "Exception in org.onap.so.bpmn.common.scripts.CompleteMsoProcess.CheckAAIOrchStatus " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "CheckAAIOrchStatus Exit")
    }

    void checkAAIOrchStatusOfNSI(DelegateExecution execution) {

        logger.debug(Prefix + "CheckAAIOrchStatus Start")
        CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
        String msg = ""
        String globalSubscriberId = customerInfo.globalSubscriberId
        String subscriptionServiceType = customerInfo.subscriptionServiceType
        String nsiServiceId = customerInfo.getNsiId()

        logger.debug("network slice instance id: " + nsiServiceId)

        //check the NSI is exist or the status of NSI is active or de-active
        try {
            //Query nsi by nsi id

            //get the NSI id by e2e slice id
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(globalSubscriberId)
                    .serviceSubscription(subscriptionServiceType)
                    .serviceInstance(nsiServiceId))

            AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)

            ServiceInstance nsInstance = si.get()
            if (!"nsi".equalsIgnoreCase(nsInstance.getServiceRole().toLowerCase())) {
                logger.info("the service id" + nsInstance.getServiceInstanceId() + "is " +
                        nsInstance.getServiceRole())
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
            execution.setVariable("nsInstance", nsInstance)
            execution.setVariable("nsiUri", uri)
            boolean isContinue = handleOperation(customerInfo, nsInstance)
            execution.setVariable("isContinue", isContinue)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            logger.info("NSI Service doesnt exist")
            execution.setVariable("isActivate", "false")
            execution.setVariable("isContinue", "false")
            msg = "Exception in org.onap.so.bpmn.common.scripts.CompleteMsoProcess.CheckAAIOrchStatus " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "CheckAAIOrchStatus Exit")
    }

    void prepareActivation(DelegateExecution execution) {
        logger.debug(Prefix + "prepareActivation Start")

        CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
        String globalSubscriberId = customerInfo.globalSubscriberId
        String subscriptionServiceType = customerInfo.subscriptionServiceType

        logger.debug(" ***** prepare active NSI/AN/CN/TN slice ***** ")

        Queue<NssInstance> nssInstances = new LinkedList<>()
        ServiceInstance nsInstance =
                execution.getVariable("nsInstance") as ServiceInstance
        try {
            //get the TN NSSI id by NSI id, active NSSI TN slicing
            List<Relationship> relatedList = nsInstance.getRelationshipList().getRelationship()
            for (Relationship relationship : relatedList) {
                String relatedTo = relationship.getRelatedTo()
                if (!"service-instance".equalsIgnoreCase(relatedTo)) {
                    continue
                }
                String relatioshipurl = relationship.getRelatedLink()
                String nssiserviceid = relatioshipurl.substring(relatioshipurl.lastIndexOf("/") + 1,
                        relatioshipurl.length())

                AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                        .customer(globalSubscriberId)
                        .serviceSubscription(subscriptionServiceType)
                        .serviceInstance(nssiserviceid))
                if (!client.exists(nsiUri)) {
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                            "Service Instance was not found in aai")
                }
                AAIResultWrapper wrapper01 = client.get(nsiUri, NotFoundException.class)
                Optional<ServiceInstance> nssiSi = wrapper01.asBean(ServiceInstance.class)
                nssiSi.ifPresent(new Consumer<ServiceInstance>() {
                    @Override
                    void accept(ServiceInstance instance) {
                        String env = Objects.requireNonNull(instance.getEnvironmentContext())
                        NssInstance nssi = NssInstance.builder().nssiId(instance.getServiceInstanceId())
                                .modelInvariantId(instance.getModelInvariantId())
                                .modelVersionId(instance.getModelVersionId())
                                .networkType(NetworkType.fromString(env))
                                .operationType(customerInfo.operationType)
                                .snssai(customerInfo.snssai)
                                .serviceType(instance.getServiceType())
                                .build()
                        nssInstances.offer(nssi)
                    }
                })
            }
            execution.setVariable("nssInstances", nssInstances)
            execution.setVariable("nssInstanceInfos", nssInstances)
        } catch (Exception e) {
            String msg = "Requested service does not exist:" + e.getMessage()
            logger.info("Service doesnt exist")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "prepareActivation Exit")
    }

    void isOperationFinished(DelegateExecution execution) {
        Queue<NssInstance> nssInstances = execution.getVariable("nssInstances") as Queue<NssInstance>
        if (nssInstances.isEmpty()) {
            execution.setVariable("isOperationFinished", "true")
        }
    }

    def updateStatusSNSSAIandNSIandNSSI = { DelegateExecution execution ->
        logger.debug(Prefix + "updateStatusSNSSAIandNSIandNSSI Start")
        logger.debug(" ***** update SNSSAI NSI NSSI slicing ***** ")
        ServiceInstance ssInstance = execution.getVariable("ssInstance") as ServiceInstance
        AAIResourceUri ssUri = execution.getVariable("ssiUri") as AAIResourceUri

        CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
        OperationType operationType = customerInfo.operationType

        updateStratus(execution, ssInstance, operationType, ssUri)
        //update the nsi
        ServiceInstance nsInstance = execution.getVariable("nsInstance") as ServiceInstance
        AAIResourceUri nsiUri = execution.getVariable("nsiUri") as AAIResourceUri

        updateStratus(execution, nsInstance, operationType, nsiUri)


        logger.debug(Prefix + "updateStatusSNSSAIandNSIandNSSI Exit")
    }

    void updateStratus(DelegateExecution execution, ServiceInstance serviceInstance,
                       OperationType operationType, AAIResourceUri uri) {

        logger.debug(Prefix + "updateStratus Start")

        try {
            serviceInstance.setOrchestrationStatus()
            if (OperationType.ACTIVATE == operationType) {
                serviceInstance.setOrchestrationStatus(OrchestrationStatusEnum.ACTIVATED.getValue())
            } else {
                serviceInstance.setOrchestrationStatus(OrchestrationStatusEnum.DEACTIVATED.getValue())
            }
            client.update(uri, serviceInstance)
        } catch (Exception e) {
            logger.info("Service is already in active state")
            String msg = "Service is already in active state, " + e.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "updateStratus Exit")
    }

    def prepareCompletionRequest = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareCompletionRequest Start")
        CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
        String serviceId = customerInfo.getServiceInstanceId()
        String operationId = customerInfo.getOperationId()
        String userId = customerInfo.getGlobalSubscriberId()

        String result = "finished"
        String progress = "100"
        String reason = ""
        String operationContent = "action finished success"
        String operationType = customerInfo.operationType.getType()

        OperationStatus initStatus = new OperationStatus()
        initStatus.setServiceId(serviceId)
        initStatus.setOperationId(operationId)
        initStatus.setOperation(operationType)
        initStatus.setUserId(userId)
        initStatus.setResult(result)
        initStatus.setProgress(progress)
        initStatus.setReason(reason)
        initStatus.setOperationContent(operationContent)

        requestDBUtil.prepareUpdateOperationStatus(execution, initStatus)

        logger.debug(Prefix + "prepareCompletionRequest Exit")
    }
}
