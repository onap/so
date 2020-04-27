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

import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInfo
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoCreateCommunicationService.bpmn</class> process.
 * AlaCarte flow for 1702 ServiceInstance Create
 *
 */
class CreateCommunicationService extends AbstractServiceTaskProcessor {

    String Prefix="CCS_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    RequestDBUtil requestDBUtil = new RequestDBUtil()

    JsonUtils jsonUtil = new JsonUtils()

    AAIResourcesClient client = getAAIClient()

    private static final Logger logger = LoggerFactory.getLogger(CreateCommunicationService.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix + "preProcessRequest Start")
        execution.setVariable("prefix", Prefix)
        execution.setVariable("startTime", System.currentTimeMillis())
        def msg
        //execution.setVariable("bpmnRequest", InputString)
        try {
            // get request input
            String siRequest = execution.getVariable("bpmnRequest")
            logger.debug(siRequest)

            //String requestId = execution.getVariable("mso-request-id")
            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            logger.debug("Input Request:" + siRequest + " reqId:" + requestId)

            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                serviceInstanceId = UUID.randomUUID().toString()
            }
            logger.debug("Generated new Service Instance:" + serviceInstanceId)
            serviceInstanceId = UriUtils.encode(serviceInstanceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceInstanceId)

            //subscriberInfo
            String globalSubscriberId = jsonUtil.getJsonValue(siRequest, "requestDetails.subscriberInfo.globalSubscriberId")
            if (isBlank(globalSubscriberId)) {
                msg = "Input globalSubscriberId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            //requestInfo
            execution.setVariable("source", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.source"))
            execution.setVariable("serviceInstanceName", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.instanceName"))
            execution.setVariable("disableRollback", jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.suppressRollback"))
            String productFamilyId = jsonUtil.getJsonValue(siRequest, "requestDetails.requestInfo.productFamilyId")
            if (isBlank(productFamilyId))
            {
                msg = "Input productFamilyId is null"
                logger.debug(msg)
                //exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("productFamilyId", productFamilyId)
            }

            //modelInfo
            String serviceModelInfo = jsonUtil.getJsonValue(siRequest, "requestDetails.modelInfo")
            if (isBlank(serviceModelInfo)) {
                msg = "Input serviceModelInfo is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else
            {
                execution.setVariable("csServiceModelInfo", serviceModelInfo)
            }

            logger.debug("modelInfo: " + serviceModelInfo)

            //requestParameters, subscriptionServiceType is 5G
            String subscriptionServiceType = jsonUtil.getJsonValue(siRequest, "requestDetails.requestParameters.subscriptionServiceType")
            if (isBlank(subscriptionServiceType)) {
                msg = "Input subscriptionServiceType is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                // todo: in create e2e interface, this value is write "MOG", so write it as "5G"
                execution.setVariable("subscriptionServiceType", "5G")
            }


            /*
             * Extracting User Parameters from incoming Request and converting into a Map
             */
            def jsonSlurper = new JsonSlurper()

            Map reqMap = jsonSlurper.parseText(siRequest) as Map

            //InputParams
            def userParamsList = reqMap.requestDetails?.requestParameters?.userParams

            Map<String, String> inputMap = [:]
            if (userParamsList) {
                for (def i=0; i<userParamsList.size(); i++) {
                    def userParams1 = userParamsList.get(i)
                    userParams1.each { param -> inputMap.put(param.key, param.value)}
                }
            }

            logger.debug("User Input Parameters map: " + inputMap.toString())
            execution.setVariable("serviceInputParams", inputMap)
            execution.setVariable("uuiRequest", inputMap.get("UUIRequest"))
            execution.setVariable("isAllNSMFFinished", "false")
            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

        } catch(BpmnError e) {
            throw e
        } catch(Exception ex) {
            msg = "Exception in CreateCommunicationService.preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "preProcessRequest Exit")
    }


    /**
     * create operation status in request db
     *
     * Init the Operation Status
     */
    def prepareInitOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareInitOperationStatus Start")

        String serviceId = execution.getVariable("serviceInstanceId")
        // 生成 operationId
        String operationId = execution.getVariable("operationId")
        logger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)

        OperationStatus initStatus = new OperationStatus()
        initStatus.setServiceId(serviceId)
        initStatus.setOperationId(operationId)
        initStatus.setOperation("CREATE")
        initStatus.setUserId(execution.getVariable("globalSubscriberId") as String)
        initStatus.setResult("processing")
        initStatus.setProgress("0")
        initStatus.setReason("")
        initStatus.setOperationContent("communication service create operation start")

        requestDBUtil.prepareUpdateOperationStatus(execution, initStatus)

        logger.debug(Prefix + "prepareInitOperationStatus Exit")
    }


    /**
     * return sync response
     */
    def sendSyncResponse = { DelegateExecution execution ->
        logger.debug(Prefix + "sendSyncResponse Start")
        try {
            String operationId = execution.getVariable("operationId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String createServiceRestRequest = """
                    {
                        "service": {
                            "serviceId":"${serviceInstanceId}",
                            "operationId":"${operationId}"
                        }
                    }
                    """.trim().replaceAll(" ", "")

            logger.debug("sendSyncResponse to APIH:" + "\n" + createServiceRestRequest)
            sendWorkflowResponse(execution, 202, createServiceRestRequest)

            execution.setVariable("sentSyncResponse", true)
        } catch (Exception ex) {
            String msg = "Exceptuion in sendSyncResponse:" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "sendSyncResponse Exit")
    }


    /**
     * query e2e service
     * @param execution
     */
    def prepareDoComposeE2E = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareDoComposeE2E Start")
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable(
                    "csServiceDecomposition") as ServiceDecomposition

            logger.debug("serviceDecomposition is:" + serviceDecomposition.toJsonString())

            List<ServiceProxy> serviceProxies = serviceDecomposition.getServiceProxy()
            String sourceModelUuid = serviceProxies.get(0).getSourceModelUuid()

            JSONObject queryJson = new JSONObject()
            queryJson.put("modelUuid", sourceModelUuid)

            execution.setVariable("e2eServiceModelInfo", queryJson.toString())
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in CreateCommunicationService.prepareDoComposeE2E. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "prepareDoComposeE2E Exit")
    }


    /**
     * parse communication service params from request
     * @param execution
     */
    def parseCSParamsFromReq = { DelegateExecution execution ->
        logger.debug(Prefix + "parseCSParamsFromReq Start")
        try {
            //1. CMS info

            String modelInfo = execution.getVariable("csServiceModelInfo")
            String modelInvariantUuid = jsonUtil.getJsonValue(modelInfo, "modelInvariantUuid")
            String modelUuid = jsonUtil.getJsonValue(modelInfo, "modelUuid")

            //String modelInvariantUuid = execution.getVariable("modelInvariantId")
            //String modelUuid = execution.getVariable("modelUuid")
            String uuiRequest = execution.getVariable("uuiRequest")
            String useInterval = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.useInterval")
            String csServiceName = jsonUtil.getJsonValue(uuiRequest, "service.name")
            String csServiceDescription = jsonUtil.getJsonValue(uuiRequest, "service.description")

            execution.setVariable("modelInvariantUuid", modelInvariantUuid)
            execution.setVariable("modelUuid", modelUuid)
            execution.setVariable("useInterval", useInterval)
            execution.setVariable("csServiceName", csServiceName)
            execution.setVariable("csServiceDescription", csServiceDescription)


            //2. profile info
            Integer expDataRateDL = jsonUtil.getJsonIntValue(uuiRequest, "service.parameters.requestInputs.expDataRateDL")
            Integer expDataRateUL = jsonUtil.getJsonIntValue(uuiRequest, "service.parameters.requestInputs.expDataRateUL")
            Integer latency = jsonUtil.getJsonIntValue(uuiRequest, "service.parameters.requestInputs.latency")
            Integer maxNumberOfUEs = jsonUtil.getJsonIntValue(uuiRequest, "service.parameters.requestInputs.maxNumberofUEs")
            String uEMobilityLevel = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.uemobilityLevel")
            String resourceSharingLevel = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.resourceSharingLevel")
            String coverageArea = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.coverageAreaList")

            // from template construct input map
            //String cstTemplate = execution.getVariable("cstTemplate")
            ServiceDecomposition csServiceDecomposition = execution.getVariable(
                    "csServiceDecomposition") as ServiceDecomposition
            //String csServiceType = jsonUtil.getJsonValue(cstTemplate, "serviceResources.serviceType")
            String csServiceType = csServiceDecomposition.getServiceType()
            execution.setVariable("csServiceType", csServiceType)

            //String cstTemplateInfo = jsonUtil.getJsonValue(cstTemplate, "serviceResources.serviceInfo.serviceInput")
            ServiceInfo csServiceInfo = csServiceDecomposition.getServiceInfo()
            String cstTemplateInfo = csServiceInfo.getServiceProperties()

            List<String> csInputs = jsonUtil.StringArrayToList(cstTemplateInfo)

            Map<String, ?> csInputMap = new HashMap<>()
            for (String csInput : csInputs) {
                def value
                if (jsonUtil.getJsonValue(csInput, "type") == "integer") {
                    value = jsonUtil.getJsonValue(csInput, "default")
                    csInputMap.put(jsonUtil.getJsonValue(csInput, "name"), isBlank(value) ? 0 : (value as Integer))
                } else if (jsonUtil.getJsonValue(csInput, "type") == "string") {
                    csInputMap.put(jsonUtil.getJsonValue(csInput, "name"),
                            jsonUtil.getJsonValue(csInput, "default"))
                }
            }
            csInputMap.put("expDataRateDL", expDataRateDL)
            csInputMap.put("expDataRateUL", expDataRateUL)
            csInputMap.put("latency", latency)
            csInputMap.put("maxNumberofUEs", maxNumberOfUEs)
            csInputMap.put("uEMobilityLevel", uEMobilityLevel)
            csInputMap.put("resourceSharingLevel", resourceSharingLevel)
            csInputMap.put("coverageAreaTAList", coverageArea)
            csInputMap.put("useInterval", useInterval)

            execution.setVariable("csInputMap", csInputMap)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in CreateCommunicationService.parseCSParamsFromReq. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "parseCSParamsFromReq Exit")
    }


    /**
     * get E2EST id through CST id and change communication profile to E2E service profile
     * 1. get E2EST id from cst
     * 1.1 查source service id
     * 1.2 source service
     * 1.3 source service input, init e2e profile
     */
    def generateE2EServiceProfile = { DelegateExecution execution ->
        logger.debug(Prefix + "generateE2EServiceProfile Start")
        try {
            ServiceDecomposition e2eServiceDecomposition = execution.getVariable(
                    "e2eServiceDecomposition") as ServiceDecomposition
            String e2estTemplateInfo = e2eServiceDecomposition.getServiceInfo().getServiceProperties()

            List<String> e2eInputs = jsonUtil.StringArrayToList(e2estTemplateInfo)

            Map<String, ?> csInputMap = execution.getVariable("csInputMap") as Map
            Map<String, ?> e2eInputMap = new HashMap<>()
            String key
            def value


            for (String e2eInput in e2eInputs) {
                if (jsonUtil.getJsonValue(e2eInput, "type") == "integer") {
                    def temp
                    key = jsonUtil.getJsonValue(e2eInput, "name")
                    value = csInputMap.containsKey(key) ? csInputMap.getOrDefault(key, 0) : (isBlank(temp = jsonUtil.getJsonValue(e2eInput, "default")) ? 0 : temp)

                    e2eInputMap.put(key, value as Integer)
                } else {
                    e2eInputMap.put(key = jsonUtil.getJsonValue(e2eInput, "name"), csInputMap.containsKey(key)
                            ? csInputMap.getOrDefault(key, null) : (jsonUtil.getJsonValue(e2eInput, "default")))
                }
            }

            e2eInputMap.put("sNSSAI", execution.getVariable("sNSSAI_id"))

            execution.setVariable("e2eInputMap", e2eInputMap)
            execution.setVariable("e2eServiceType", e2eServiceDecomposition.getServiceType())
            execution.setVariable("e2eModelInvariantUuid", e2eServiceDecomposition.getModelInfo().getModelInvariantUuid())
            execution.setVariable("e2eModelUuid", e2eServiceDecomposition.getModelInfo().getModelUuid())

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateE2EServiceInstance.createRelationShipInAAI. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug(Prefix + "generateE2EServiceProfile Exit")
    }


    /**
     * call createE2EService get operation id,
     * created/processing
     */
    def preRequestSend2NSMF(DelegateExecution execution) {
        logger.debug(Prefix + "preRequestSend2NSMF Start")
        try {

            //String NSMF_endpoint = "/onap/so/infra/e2eServiceInstances/v3"
            def NSMF_endpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)
            def url = NSMF_endpoint + "/e2eServiceInstances/v3"
            execution.setVariable("NSMF_endpoint", url)
            //get from model catalog inputs
            String payload = """
                {
                    "service":{
                        "name": "${execution.getVariable("csServiceName")}",
                        "description": "e2eService of ${execution.getVariable("modelUuid")}",
                        "serviceInvariantUuid": "${execution.getVariable("e2eModelInvariantUuid")}",
                        "serviceUuid": "${execution.getVariable("e2eModelUuid")}",
                        "globalSubscriberId": "${execution.getVariable("globalSubscriberId")}",
                        "serviceType": "${execution.getVariable("subscriptionServiceType")}",
                        "parameters":{
                            "requestInputs": ${execution.getVariable("e2eInputMap") as JSONObject}
                        }
                    }
                }
            """
            execution.setVariable("CSMF_NSMFRequest", payload.replaceAll("\\s+", ""))

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in CreateCommunicationService.preRequestSend2NSMF. " + ex.getMessage()
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
                def e2eServiceInstanceId = jsonUtil.getJsonValue(CSMF_NSMFResponse, "service.serviceId")
                def e2eOperationId = jsonUtil.getJsonValue(CSMF_NSMFResponse, "service.operationId")

                execution.setVariable("e2eServiceInstanceId", e2eServiceInstanceId)
                execution.setVariable("e2eOperationId", e2eOperationId)
                execution.setVariable("ProcessNsmfSuccess", "OK")
            } else {
                execution.setVariable("ProcessNsmfSuccess", "ERROR")
                execution.setVariable("operationStatus", "error")
                execution.setVariable("operationContent",
                        "communication service create operation error: nsmf response fail")
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


    /**
     * create communication service and e2e service relationship
     *
     */
    def createCSAndSSRelationship = { DelegateExecution execution ->
        logger.debug(Prefix + "createCSAndSSRelationship Start")
        String msg = ""
        try {
            def e2eServiceInstanceId = execution.getVariable("e2eServiceInstanceId")
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

            Relationship relationship = new Relationship()
            String relatedLink = "aai/v16/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${e2eServiceInstanceId}"
            relationship.setRelatedLink(relatedLink)

            // create CS and SS relationship in AAI
            createRelationShipInAAI(execution, relationship)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {

            msg = "Exception in DoCreateE2EServiceInstance.createCustomRelationship. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "createCSAndSSRelationship Exit")
    }


    /**
     * prepare update operation status to 50% after create relationship in aai
     * @param execution
     */
    def prepareUpdateOperationStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareUpdateOperationStatus Start")
        // update status creating
        OperationStatus status = new OperationStatus()
        status.setServiceId(execution.getVariable("serviceInstanceId") as String)
        status.setOperationId(execution.getVariable("operationId") as String)
        status.setOperation("CREATE")
        status.setResult("processing")
        status.setProgress("20")
        status.setOperationContent("communication service create operation processing: waiting nsmf service create finished")
        status.setUserId(execution.getVariable("globalSubscriberId") as String)

        requestDBUtil.prepareUpdateOperationStatus(execution, status)
        logger.debug(Prefix + "prepareUpdateOperationStatus Exit")
    }


    /**
     * create relationship in AAI
     */
    private createRelationShipInAAI = { DelegateExecution execution, final Relationship relationship ->
        logger.debug(Prefix + "createRelationShipInAAI Start")
        String msg
        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    execution.getVariable("globalSubscriberId"),
                    execution.getVariable("subscriptionServiceType"),
                    serviceInstanceId).relationshipAPI()
            client.create(uri, relationship)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in CreateCommunicationService.createRelationShipInAAI. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix + "createRelationShipInAAI Exit")

    }


    def prepareCallCheckProcessStatus = { DelegateExecution execution ->
        logger.debug(Prefix + "prepareCallCheckProcessStatus Start")

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        execution.setVariable("successConditions", successConditions)

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        execution.setVariable("errorConditions", errorConditions)

        execution.setVariable("processServiceType", "communication service")

        execution.setVariable("subOperationType", "CREATE")

        execution.setVariable("timeOut", 3 * 60 * 60 * 1000)

        def successParamMap = new HashMap<String, Object>()
        successParamMap.put("orchestrationStatus", "deactivated")

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
        OperationStatus status = new OperationStatus()
        status.setServiceId(execution.getVariable("serviceInstanceId") as String)
        status.setOperationId(execution.getVariable("operationId") as String)
        status.setOperation("CREATE")
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
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    globalSubscriberId, subscriptionServiceType, serviceInstanceId)
            client.update(uri, csi)
            logger.debug(Prefix + "updateFinishStatusInAAI update communication service status to deactivated")

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
