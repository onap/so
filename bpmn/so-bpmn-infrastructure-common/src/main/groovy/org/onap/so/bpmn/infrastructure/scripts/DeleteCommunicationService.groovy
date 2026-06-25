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

import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri

import static org.apache.commons.lang3.StringUtils.isBlank
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.CommunicationServiceProfile
import org.onap.aai.domain.yang.CommunicationServiceProfiles
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectName
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeleteCommunicationService extends AbstractServiceTaskProcessor {
    private final String PREFIX ="DeleteCommunicationService"
    private final Long TIMEOUT = 60 * 60 * 1000

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCommunicationService.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        execution.setVariable("prefix",PREFIX)
        String msg = ""

        LOGGER.trace("Starting preProcessRequest")

        try {
            // check for incoming json message/input
            String siRequest = execution.getVariable("bpmnRequest")
            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            execution.setVariable("operationType", "DELETE")

            //communication service id
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                msg = "communication-service id is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            LOGGER.info("Input Request: ${siRequest}, reqId: ${requestId}, serviceInstanceId: ${serviceInstanceId}")

            //requestParameters
            checkAndSetRequestParam(siRequest,"globalSubscriberId",false,execution)
            checkAndSetRequestParam(siRequest,"serviceType",false,execution)
            checkAndSetRequestParam(siRequest,"operationId",false,execution)

        } catch (BpmnError e) {
            throw e
        } catch (any) {
            msg = "Exception in preProcessRequest " + any.getCause()
            LOGGER.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.trace("Exit preProcessRequest")
    }


    /**
     * prepare update operation status
     * @param execution
     */
    void preInitUpdateOperationStatus(DelegateExecution execution){
        LOGGER.trace(" ======== STARTED initUpdateOperationStatus Process ======== ")
        try{
            execution.setVariable("result","processing")
            execution.setVariable("progress","0")
            execution.setVariable("operationContent","delete communication service operation start")
            setOperationStatus(execution)

        }catch(Exception e){
            LOGGER.error("Exception Occured Processing initUpdateOperationStatus. Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during initUpdateOperationStatus Method:\n" + e.getMessage())
        }
        LOGGER.trace("======== COMPLETED initUpdateOperationStatus Process ======== ")
    }

    /**
     * send sync response
     * @param execution
     */
    void sendSyncResponse(DelegateExecution execution) {
        LOGGER.debug("Begin sendSyncResponse")

        try {
            String operationId = execution.getVariable("operationId")
            String syncResponse = """{"operationId":"${operationId}"}""".trim()
            sendWorkflowResponse(execution, 202, syncResponse)

        } catch (Exception ex) {
            String msg  = "Exception in sendSyncResponse: " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.debug("Exit sendSyncResponse")
    }

    /**
     * query CommunicationSerive from AAI
     * save e2eslice-service instance id and service name
     * @param execution
     */
    void queryCommunicationSeriveFromAAI(DelegateExecution execution)
    {
        LOGGER.trace(" ***** begin queryCommunicationSeriveFromAAI *****")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")

        String errorMsg = "query communication service from aai failed"
        AAIResultWrapper wrapper = queryAAI(execution, Types.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
        Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
        if(si.isPresent())
        {
            String serviceInstName = si.get()?.getServiceInstanceName()
            String e2eSliceServiceInstId
            if(si.isPresent())
            {
                List<Relationship> relationshipList = si.get().getRelationshipList()?.getRelationship()
                for (Relationship relationship : relationshipList)
                {
                    String relatedTo = relationship.getRelatedTo()
                    if (relatedTo == "service-instance")
                    {
                        String relatedLink = relationship.getRelatedLink()?:""
                        e2eSliceServiceInstId = relatedLink ? relatedLink.substring(relatedLink.lastIndexOf("/") + 1,relatedLink.length()) : ""
                        break
                    }
                }
            }
            execution.setVariable("e2eSliceServiceInstanceId", e2eSliceServiceInstId)
            execution.setVariable("serviceInstanceName", serviceInstName ?: "")
            LOGGER.info("communication-service Id: ${serviceInstanceId}, e2eslice-service Id: ${e2eSliceServiceInstId}, serviceName: ${serviceInstName}")
        }
        LOGGER.debug(" ***** Exit queryCommunicationSeriveFromAAI *****")
    }

    /**
     * query AAI
     * @param execution
     * @param aaiObjectName
     * @param instanceId
     * @return AAIResultWrapper
     */
    private AAIResultWrapper queryAAI(DelegateExecution execution, AAIObjectName aaiObjectName, String instanceId, String errorMsg)
    {
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(instanceId))
        if (!getAAIClient().exists(resourceUri)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
        }
        AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
        return wrapper
    }


    /**
     * 再次调用deleteE2EServiceInstance接口，然后获取到operationid,
     */
    void sendRequest2NSMFWF(DelegateExecution execution) {
        LOGGER.debug("begin preRequestSend2NSMF")
        try {
            //url:/onap/so/infra/e2eServiceInstances/v3/{serviceInstanceId}"
            def NSMF_endpoint = UrnPropertiesReader.getVariable("mso.infra.endpoint.url", execution)
            String url = "${NSMF_endpoint}/e2eServiceInstances/v3/${execution.getVariable("e2eSliceServiceInstanceId")}"

            String requestBody = """
                {
                    "globalSubscriberId": "${execution.getVariable("globalSubscriberId")}",
                    "serviceType": "${execution.getVariable("serviceType")}"
                }
            """
            requestBody.replaceAll("\\s+", "")

            String msoKey = UrnPropertiesReader.getVariable("mso.msoKey", execution)
            String basicAuth =  UrnPropertiesReader.getVariable("mso.adapters.po.auth", execution)
            def authHeader = utils.getBasicAuth(basicAuth, msoKey)

//            String basicAuthValue = utils.encrypt(basicAuth, msoKey)
//            String encodeString = utils.getBasicAuth(basicAuthValue, msoKey)

            HttpClient httpClient = getHttpClientFactory().newJsonClient(new URL(url), ONAPComponents.SO)
//            httpClient.addAdditionalHeader("Authorization", encodeString)
            httpClient.addAdditionalHeader("Authorization", authHeader)
            httpClient.addAdditionalHeader("Accept", "application/json")
            Response httpResponse = httpClient.delete(requestBody)
            handleNSSMFWFResponse(httpResponse, execution)

        } catch (BpmnError e) {
            throw e
        } catch (any) {
            String msg = "Exception in DeleteCommunicationService.preRequestSend2NSMF. " + any.getCause()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        LOGGER.debug("exit preRequestSend2NSMF")
    }

    /**
     * prepare update operation status
     * @param execution
     */
    private void handleNSSMFWFResponse(Response httpResponse, DelegateExecution execution){
        LOGGER.debug(" ======== STARTED prepareUpdateOperationStatus Process ======== ")

        int nsmfResponseCode = httpResponse.getStatus()
        LOGGER.debug("nsmfResponseCode${nsmfResponseCode}")

        if (nsmfResponseCode >= 200 && nsmfResponseCode < 204 && httpResponse.hasEntity()) {
            String nsmfResponse = httpResponse.readEntity(String.class)
            def e2eOperationId = jsonUtil.getJsonValue(nsmfResponse, "operationId")
            execution.setVariable("e2eOperationId", e2eOperationId)
            execution.setVariable("progress","20")
            execution.setVariable("operationContent","waiting nsmf service delete finished")

            execution.setVariable("currentCycle",0)
            execution.setVariable("isNSMFTimeOut", "no")
            execution.setVariable("isNSMFWFRspSucceed","yes")
        }
        else
        {
            String serviceName = execution.getVariable("serviceInstanceName")
            execution.setVariable("progress", "100")
            execution.setVariable("result", "error")
            execution.setVariable("operationContent", "terminate service failure.")
            execution.setVariable("reason","NSMF WF asynchronous response failed, status Code:${nsmfResponseCode}")
            execution.setVariable("isNSMFWFRspSucceed","no")
            LOGGER.error("nsmf async response error，nsmfResponseCode：${nsmfResponseCode}，serivceName:${serviceName}")
        }
        setOperationStatus(execution)
        LOGGER.debug("======== COMPLETED prepareUpdateOperationStatus Process ======== ")
    }

    /**
     * prepare to call sub process
     * @param execution
     */
    void prepareCallCheckProcessStatus(DelegateExecution execution)
    {
        LOGGER.debug(PREFIX + "prepareCallCheckProcessStatus Start")

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        execution.setVariable("successConditions", successConditions)

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        execution.setVariable("errorConditions", errorConditions)

        execution.setVariable("processServiceType", "communication service")
        execution.setVariable("subOperationType", "DELETE")
        execution.setVariable("initProgress", 20)
        execution.setVariable("endProgress",90)

        execution.setVariable("timeOut", TIMEOUT)

        LOGGER.debug(PREFIX + "prepareCallCheckProcessStatus Exit")
    }

    /**
     * delete communication profile from AAI
     * @param execution
     */
    void delCSProfileFromAAI(DelegateExecution execution)
    {
        LOGGER.debug("start delete communication service profile from AAI")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")

        String profileId
        try
        {
            AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId).communicationServiceProfiles())
            AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
            Optional<CommunicationServiceProfiles> csProfilesOpt = wrapper.asBean(CommunicationServiceProfiles.class)
            if(csProfilesOpt.isPresent()){
                CommunicationServiceProfiles csProfiles = csProfilesOpt.get()
                CommunicationServiceProfile csProfile = csProfiles.getCommunicationServiceProfile().get(0)
                profileId = csProfile ? csProfile.getProfileId() : ""
            }
            AAISimpleUri profileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId).communicationServiceProfile(profileId))
            if (!getAAIClient().exists(profileUri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "communication service profile was not found in aai")
            }

            getAAIClient().delete(profileUri)
            LOGGER.debug("end delete communication service profile from AAI")
        }
        catch (any)
        {
            String msg = "delete communication service profile from aai failed! cause-"+any.getCause()
            LOGGER.error(any.printStackTrace())
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }

    }

    /**
     * delete communication service from AAI
     * @param execution
     */
    void delCSFromAAI(DelegateExecution execution)
    {
        try
        {
            LOGGER.debug("start delete communication service from AAI")
            AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("serviceType")).serviceInstance(execution.getVariable("serviceInstanceId")))
            getAAIClient().delete(serviceInstanceUri)

            execution.setVariable("progress", "100")
            execution.setVariable("result", "finished")
            execution.setVariable("operationContent", "CSMF completes service terminated.")
            setOperationStatus(execution)
            LOGGER.debug("end delete communication service from AAI")
        }
        catch (any)
        {
            LOGGER.error("Error occured within delCSFromAAI method, cause: ${any.getCause()} ")
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during delete communication service from aai")
        }
    }

    void sendSyncError(DelegateExecution execution)
    {
        LOGGER.debug("Starting sendSyncError")

        try {
            String errorMessage = "Sending Sync Error."
            if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
                WorkflowException wfe = execution.getVariable("WorkflowException")
                errorMessage = wfe.getErrorMessage()
            }

            String buildworkflowException =
                    """<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

            LOGGER.debug(buildworkflowException)
            sendWorkflowResponse(execution, 500, buildworkflowException)

        } catch (Exception ex) {
            LOGGER.error("Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
        }
    }

    /**
     * prepare update operation status
     * @param execution
     */
    void preFailedOperationStatus(DelegateExecution execution)
    {
        LOGGER.debug(" ======== STARTED preFailedOperationStatus Process ======== ")

        execution.setVariable("progress", "100")
        execution.setVariable("result", "error")
        execution.setVariable("operationContent", "terminate service failure")

        WorkflowException wfex = execution.getVariable("WorkflowException") as WorkflowException
        String errorMessage = wfex.getErrorMessage()
        errorMessage = errorMessage.length() > 200 ? errorMessage.substring(0,200) + "......" : errorMessage
        execution.setVariable("reason", errorMessage)
        setOperationStatus(execution)

        LOGGER.debug("======== COMPLETED prepareEndOperationStatus Process ======== ")
    }

    /**
     * prepare Operation status
     * @param execution
     * @param operationType
     */
    private void setOperationStatus(DelegateExecution execution)
    {
        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId(execution.getVariable("serviceInstanceId"))
        operationStatus.setOperationId(execution.getVariable("operationId"))
        operationStatus.setUserId(execution.getVariable("globalSubscriberId"))
        //interface not support update
        operationStatus.setServiceName(execution.getVariable("serviceInstanceName"))
        operationStatus.setResult(execution.getVariable("result"))
        operationStatus.setProgress(execution.getVariable("progress"))
        operationStatus.setOperationContent(execution.getVariable("operationContent"))
        operationStatus.setReason(execution.getVariable("reason")?:"")
        operationStatus.setOperation("DELETE")

        requestDBUtil.prepareUpdateOperationStatus(execution, operationStatus)
    }

    void prepareFailureStatus(DelegateExecution execution)
    {
        execution.setVariable("result", "finished")
        execution.setVariable("progress", "100")
        execution.setVariable("operationContent", "terminate service failure.")
        setOperationStatus(execution)
        LOGGER.debug("${PREFIX}-prepareFailureStatus,result:${execution.getVariable("result")}, reason: ${execution.getVariable("reason")}")
    }

    /**
     * check request json and save parameter to execution
     * @param siRequest
     * @param paraName
     * @param isErrorException
     * @param execution
     */
    private void checkAndSetRequestParam(String siRequest, String paraName, boolean isErrorException, DelegateExecution execution)
    {
        String msg = ""
        String paramValue = jsonUtil.getJsonValue(siRequest, paraName)
        if (isBlank(paramValue)) {
            msg = "Input ${paraName} is null"
            LOGGER.error(msg)
            if(isErrorException)
            {
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

        } else {
            execution.setVariable(paraName, paramValue)
        }
    }

}
