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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.*
import org.onap.so.beans.nsmf.NSSI
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.db.request.beans.OperationStatus
import org.onap.logging.filter.base.ErrorCode
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.NotFoundException
import java.lang.reflect.Type

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

    private static final Logger logger = LoggerFactory.getLogger(ActivateSliceService.class)

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
            String operationId = jsonUtil.getJsonValue(siRequest, "operationId")
            execution.setVariable("operationId", operationId)

            String operationType = execution.getVariable("operationType")
            execution.setVariable("operationType", operationType.toUpperCase())

            logger.info("operationType is " + execution.getVariable("operationType") )
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "preProcessRequest Exit")
    }


    def sendSyncResponse = { DelegateExecution execution ->
        logger.debug(Prefix + "sendSyncResponse Start")
        try {
            String operationId = execution.getVariable("operationId")
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


    def prepareCompletionRequest = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareCompletionRequest Start")
        String serviceId = execution.getVariable("serviceInstanceId")
        String operationId = execution.getVariable("operationId")
        String userId = execution.getVariable("globalSubscriberId")
        //String result = execution.getVariable("result")
        String result = "finished"
        String progress = "100"
        String reason = ""
        String operationContent = execution.getVariable("operationContent")
        String operationType = execution.getVariable("operationType")

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


    /**
     * Init the service Operation Status
     */
    def prepareInitServiceOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareActivateServiceOperationStatus Start")
        try {
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String userId = execution.getVariable("globalSubscriberId")
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service activation"

            execution.setVariable("e2eserviceInstanceId", serviceId)
            execution.setVariable("operationType", operationType)

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


    private getSNSSIStatusByNsi = { DelegateExecution execution, String NSIServiceId ->

        logger.debug(Prefix + "getSNSSIStatusByNsi Start")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        AAIResourcesClient client = new AAIResourcesClient()
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                globalSubscriberId, subscriptionServiceType, NSIServiceId)
        if (!client.exists(uri)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
        }
        AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
        Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
        if (si.isPresent()) {

            List<Relationship> relatedList = si.get().getRelationshipList().getRelationship()
            for (Relationship relationship : relatedList) {
                String relatedTo = relationship.getRelatedTo()
                if (relatedTo.toLowerCase() == "allotted-resource") {
                    //get snssi from allotted resource in list by nsi
                    List<String> SNSSIList = new ArrayList<>()
                    List<RelationshipData> relationshipDataList = relationship.getRelationshipData()
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if (relationshipData.getRelationshipKey() == "service-instance.service-instance-id") {
                            SNSSIList.add(relationshipData.getRelationshipValue())
                        }
                    }
                    for (String snssi : SNSSIList) {
                        AAIResourcesClient client01 = new AAIResourcesClient()
                        AAIResourceUri uri01 = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                                globalSubscriberId, subscriptionServiceType, snssi)
                        if (!client.exists(uri01)) {
                            exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                                    "Service Instance was not found in aai")
                        }
                        AAIResultWrapper wrapper01 = client01.get(uri01, NotFoundException.class)
                        Optional<ServiceInstance> nssiSi = wrapper01.asBean(ServiceInstance.class)
                        if (nssiSi.isPresent()) {
                            return nssiSi.get().getOrchestrationStatus() == "deactivated"
                        }
                    }

                }
            }

        }
        logger.debug(Prefix + "getSNSSIStatusByNsi Exit")
    }


    def updateStatusSNSSAIandNSIandNSSI = { DelegateExecution execution ->
        logger.debug(Prefix + "updateStatusSNSSAIandNSIandNSSI Start")
        logger.debug(" ***** update SNSSAI NSI NSSI slicing ***** ")
        String e2eserviceInstanceId = execution.getVariable("e2eserviceInstanceId")
        String NSIserviceInstanceId = execution.getVariable("NSIserviceid")

        String globalCustId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String operationType = execution.getVariable("operationType")

        String nssiMap = execution.getVariable("nssiMap")
        Type type = new TypeToken<HashMap<String, NSSI>>() {}.getType()
        Map<String, NSSI> activateNssiMap = new Gson().fromJson(nssiMap, type)
        //update tn/cn/an nssi
        for (Map.Entry<String, NSSI> entry : activateNssiMap.entrySet()) {
            NSSI nssi = entry.getValue()
            String nssiid = nssi.getNssiId()
            updateStratus(execution, globalCustId, serviceType, nssiid, operationType)
        }
        if (operationType.equalsIgnoreCase("activation")) {
            //update the s-nssai
            updateStratus(execution, globalCustId, serviceType, e2eserviceInstanceId, operationType)
            //update the nsi
            updateStratus(execution, globalCustId, serviceType, NSIserviceInstanceId, operationType)
        } else {
            //update the s-nssai
            updateStratus(execution, globalCustId, serviceType, e2eserviceInstanceId, operationType)
            boolean flag = getSNSSIStatusByNsi(execution, NSIserviceInstanceId)
            if (flag) {
                //update the nsi
                updateStratus(execution, globalCustId, serviceType, NSIserviceInstanceId, operationType)
            } else {
                logger.error("Service's status update failed")
                String msg = "Service's status update failed"
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
        }
        logger.debug(Prefix + "updateStatusSNSSAIandNSIandNSSI Exit")
    }


    def updateStratus = { DelegateExecution execution, String globalCustId,
                          String serviceType, String serviceId, String operationType ->
        logger.debug(Prefix + "updateStratus Start")

        try {
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    globalCustId, serviceType, serviceId)
            if (!client.exists(uri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
            }
            AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)

            if (si.isPresent()) {
                if (operationType.equalsIgnoreCase("activation")) {
                    if (si.get().getOrchestrationStatus() == "deactivated") {
                        si.get().setOrchestrationStatus("activated")
                        client.update(uri, si.get())
                    }
                } else {
                    if (si.get().getOrchestrationStatus() == "activated") {
                        si.get().setOrchestrationStatus("deactivated")
                        client.update(uri, si.get())
                    }
                }

            }
        } catch (Exception e) {
            logger.info("Service is already in active state")
            String msg = "Service is already in active state, " + e.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "updateStratus Exit")
    }


    def prepareActivation = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareActivation Start")

        logger.debug(" ***** prepare active NSI/AN/CN/TN slice ***** ")
        String NSIserviceInstanceId = execution.getVariable("NSIserviceid")

        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        Map<String, NSSI> nssiMap = new HashMap<>()

        List<String> activationSequence = new ArrayList<>(Arrays.asList("an", "tn", "cn"))

        def activationCount = activationSequence.size()

        execution.setVariable("activationIndex", "0")

        execution.setVariable("activationCount", activationCount)
        try {
            //get the TN NSSI id by NSI id, active NSSI TN slicing
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    globalSubscriberId, subscriptionServiceType, NSIserviceInstanceId)
            if (!client.exists(uri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
            }
            AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            if (si.isPresent()) {

                List<Relationship> relatedList = si.get().getRelationshipList().getRelationship()
                for (Relationship relationship : relatedList) {
                    String relatedTo = relationship.getRelatedTo()
                    if (relatedTo.toLowerCase() == "service-instance") {
                        String relatioshipurl = relationship.getRelatedLink()
                        String nssiserviceid =
                                relatioshipurl.substring(relatioshipurl.lastIndexOf("/") + 1, relatioshipurl.length())

                        AAIResourcesClient client01 = new AAIResourcesClient()
                        AAIResourceUri uri01 = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                                globalSubscriberId, subscriptionServiceType, nssiserviceid)
                        if (!client.exists(uri01)) {
                            exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                                    "Service Instance was not found in aai")
                        }
                        AAIResultWrapper wrapper01 = client01.get(uri01, NotFoundException.class)
                        Optional<ServiceInstance> nssiSi = wrapper01.asBean(ServiceInstance.class)
                        if (nssiSi.isPresent()) {
                            if (nssiSi.get().getEnvironmentContext().toLowerCase().contains("an")
                                    || nssiSi.get().getEnvironmentContext().toLowerCase().contains("cn")
                                    || nssiSi.get().getEnvironmentContext().toLowerCase().contains("tn")) {
                                nssiMap.put(nssiSi.get().getEnvironmentContext(),
                                        new NSSI(nssiSi.get().getServiceInstanceId(),
                                                nssiSi.get().getModelInvariantId(), nssiSi.get().getModelVersionId()))
                            }
                        }
                    }
                }


            }
        } catch (Exception e) {
            String msg = "Requested service does not exist:" + e.getMessage()
            logger.info("Service doesnt exist")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        if (nssiMap.size() > 0) {
            execution.setVariable("isNSSIActivate", "true")
            String nssiMap01 = mapToJsonStr(nssiMap)
            execution.setVariable("nssiMap", nssiMap01)
            execution.setVariable("operation_type", "activate")
            execution.setVariable("activationCount", nssiMap.size())
            logger.info("the nssiMap01 is :" + nssiMap01)
        } else {
            execution.setVariable("isNSSIActivate", "false")
        }

        logger.debug(Prefix + "prepareActivation Exit")
    }


    private mapToJsonStr = { HashMap<String, NSSI> stringNSSIHashMap ->
        HashMap<String, NSSI> map = new HashMap<String, NSSI>()
        for (Map.Entry<String, NSSI> child : stringNSSIHashMap.entrySet()) {
            map.put(child.getKey(), child.getValue())
        }
        return new Gson().toJson(map)
    }


    def checkAAIOrchStatusofslice = { DelegateExecution execution ->
        logger.debug(Prefix + "CheckAAIOrchStatus Start")

        String msg = ""
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        String operationType = execution.getVariable("operationType")

        logger.debug("serviceInstanceId: " + serviceInstanceId)

        //check the e2e slice status
        try {
            try {
                AAIResourcesClient client = new AAIResourcesClient()
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                        globalSubscriberId, subscriptionServiceType, serviceInstanceId)
                if (!client.exists(uri)) {
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                            "Service Instance was not found in aai")
                }
                AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
                Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
                if (si.isPresent()) {
                    if (si.get().getOrchestrationStatus().toLowerCase() == "activated" &&
                            operationType.equalsIgnoreCase("deactivation")) {
                        logger.info("Service is in active state")
                        execution.setVariable("e2eservicestatus", "activated")
                        execution.setVariable("isContinue", "true")
                        String snssai = si.get().getEnvironmentContext()
                        execution.setVariable("snssai", snssai)
                    } else if (si.get().getOrchestrationStatus().toLowerCase() == "deactivated" &&
                            operationType.equalsIgnoreCase("activation")) {
                        logger.info("Service is  in de-activated state")
                        execution.setVariable("e2eservicestatus", "deactivated")
                        execution.setVariable("isContinue", "true")
                        String snssai = si.get().getEnvironmentContext()
                        execution.setVariable("snssai", snssai)
                    } else {
                        execution.setVariable("isContinue", "false")
                    }
                }
            } catch (Exception e) {
                msg = "Requested e2eservice does not exist"
                logger.info("e2eservice doesnt exist")
                execution.setVariable("isContinue", "false")
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }

            //check the NSI is exist or the status of NSI is active or de-active
            try {

                //get the allotted-resources by e2e slice id
                AAIResourcesClient client_allotted = new AAIResourcesClient()
                AAIResourceUri uri_allotted = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE_ALL,
                        globalSubscriberId, subscriptionServiceType, serviceInstanceId)
                if (!client_allotted.exists(uri_allotted)) {
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
                }
                AAIResultWrapper wrapper_allotted = client_allotted.get(uri_allotted, NotFoundException.class)
                Optional<AllottedResources> all_allotted = wrapper_allotted.asBean(AllottedResources.class)

                if (all_allotted.isPresent() && all_allotted.get().getAllottedResource()) {
                    List<AllottedResource> AllottedResourceList = all_allotted.get().getAllottedResource()
                    AllottedResource ar = AllottedResourceList.first()
                    String relatedLink = ar.getRelationshipList().getRelationship().first().getRelatedLink()
                    String nsiserviceid = relatedLink.substring(relatedLink.lastIndexOf("/") + 1, relatedLink.length())
                    execution.setVariable("NSIserviceid", nsiserviceid)
                    logger.info("the NSI ID is:" + nsiserviceid)

                    //Query nsi by nsi id
                    try {
                        //get the NSI id by e2e slice id
                        AAIResourcesClient client = new AAIResourcesClient()
                        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                                globalSubscriberId, subscriptionServiceType, nsiserviceid)
                        if (!client.exists(uri)) {
                            exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                                    "Service Instance was not found in aai")
                        }
                        AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
                        Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)

                        if (si.isPresent()) {
                            if (si.get().getServiceRole().toLowerCase() == "nsi") {
                                if (si.get().getOrchestrationStatus() == "activated") {
                                    logger.info("NSI services is  in activated state")
                                    execution.setVariable("NSIservicestatus", "activated")
                                } else {
                                    logger.info("NSI services is  in deactivated state")
                                    execution.setVariable("NSIservicestatus", "deactivated")
                                }
                            } else {
                                logger.info("the service id" + si.get().getServiceInstanceId() + "is " +
                                        si.get().getServiceRole())
                                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
                            }
                        }
                    } catch (Exception e) {
                        msg = "Requested NSI service does not exist:" + e.getMessage()
                        logger.info("NSI service doesnt exist")
                        execution.setVariable("isContinue", "false")
                        exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
                    }
                }
            } catch (Exception e) {
                msg = "Requested service does not exist: " + e.getMessage()
                logger.info("NSI Service doesnt exist")
                execution.setVariable("isActivate", "false")
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in org.onap.so.bpmn.common.scripts.CompleteMsoProcess.CheckAAIOrchStatus " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "CheckAAIOrchStatus Exit")
    }

}
