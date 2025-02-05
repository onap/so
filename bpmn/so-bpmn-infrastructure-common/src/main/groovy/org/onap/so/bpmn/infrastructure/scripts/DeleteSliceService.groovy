/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019, CMCC Technologies Co., Ltd.
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

import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri

import static org.apache.commons.lang3.StringUtils.isBlank
import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceProfile
import org.onap.aai.domain.yang.ServiceProfiles
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeleteSliceService extends AbstractServiceTaskProcessor {

    private final String PREFIX ="DeleteSliceService"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()

    private static final Logger LOGGER = LoggerFactory.getLogger( DeleteSliceService.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        execution.setVariable("prefix", PREFIX)
        String msg = ""

        LOGGER.debug("*****${PREFIX} preProcessRequest *****")

        try {
            // check for incoming json message/input
            String siRequest = execution.getVariable("bpmnRequest")
            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)

            //e2eslice-service instance id
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)) {
                msg = "e2eslice-service id is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            LOGGER.info("Input Request: ${siRequest}, reqId: ${requestId}, e2eslice-service: ${serviceInstanceId}")

            //subscriberInfo
            checkAndSetRequestParam(siRequest,"globalSubscriberId",false, execution)
            checkAndSetRequestParam(siRequest,"serviceType",false, execution)
            checkAndSetRequestParam(siRequest,"operationId",false, execution)
            checkAndSetRequestParam(siRequest,"scriptName",false, execution)
            //prepare init operation status
            execution.setVariable("progress", "0")
            execution.setVariable("result", "processing")
            execution.setVariable("operationType", "DELETE")
            execution.setVariable("operationContent", "Delete Slice service operation start")
            updateServiceOperationStatus(execution)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            LOGGER.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.debug("*****${PREFIX} Exit preProcessRequest *****")
    }

    /**
     * send asynchronous response
     * @param execution
     */
    void sendAsyncResponse(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start sendSyncResponse ")

        try {
            String operationId = execution.getVariable("operationId")
            String syncResponse = """{"operationId":"${operationId}"}""".trim()
            LOGGER.info("sendSynchResponse: xmlSyncResponse - " + "\n" + syncResponse)
            sendWorkflowResponse(execution, 202, syncResponse)

        } catch (Exception ex) {
            String msg  = "Exception in sendSyncResponse: " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.trace("${PREFIX} Exit sendSyncResponse")
    }

    /**
     * Deletes the slice service instance in aai
     */
    void deleteSliceServiceInstance(DelegateExecution execution) {
        LOGGER.trace("${PREFIX} Start deleteSliceServiceInstance")
        try {

            AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("serviceType")).serviceInstance(execution.getVariable("serviceInstanceId")))
            getAAIClient().delete(serviceInstanceUri)

            execution.setVariable("progress", "100")
            execution.setVariable("result", "finished")
            execution.setVariable("operationContent", "NSMF completes slicing service termination.")
            updateServiceOperationStatus(execution)

            LOGGER.trace("${PREFIX} Exited deleteSliceServiceInstance")
        }catch(Exception e){
            LOGGER.debug("Error occured within deleteSliceServiceInstance method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during deleteSliceServiceInstance from aai")
        }
    }

    /**
     * update operation status
     * @param execution
     */
    private void updateServiceOperationStatus(DelegateExecution execution){

        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId(execution.getVariable("serviceInstanceId"))
        operationStatus.setOperationId(execution.getVariable("operationId"))
        operationStatus.setUserId(execution.getVariable("globalSubscriberId"))
        operationStatus.setResult(execution.getVariable("result"))
        operationStatus.setProgress(execution.getVariable("progress"))
        operationStatus.setOperationContent(execution.getVariable("operationContent"))
        operationStatus.setReason(execution.getVariable("reason"))
        operationStatus.setOperation(execution.getVariable("operationType"))

        requestDBUtil.prepareUpdateOperationStatus(execution, operationStatus)
    }

    /**
     * delete service profile from aai
     * @param execution
     */
    void delServiceProfileFromAAI(DelegateExecution execution)
    {
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String profileId = ""

        try
        {
            AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId).serviceProfiles())
            AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
            Optional<ServiceProfiles> serviceProfilesOpt =wrapper.asBean(ServiceProfiles.class)
            if(serviceProfilesOpt.isPresent()){
                ServiceProfiles serviceProfiles = serviceProfilesOpt.get()
                ServiceProfile serviceProfile = serviceProfiles.getServiceProfile().get(0)
                profileId = serviceProfile ? serviceProfile.getProfileId() : ""
            }
            AAISimpleUri profileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId).serviceProfile(profileId))
            if (!getAAIClient().exists(profileUri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
            }
            getAAIClient().delete(profileUri)
        }
        catch (any)
        {
            String msg = "delete service profile from aai failed! cause-"+any.getCause()
            LOGGER.error(any.printStackTrace())
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }
    }

     void sendSyncError(DelegateExecution execution) {
        LOGGER.debug("${PREFIX} Start sendSyncError")

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

    void prepareEndOperationStatus(DelegateExecution execution){
        LOGGER.debug(" ======== ${PREFIX} STARTED prepareEndOperationStatus Process ======== ")

        execution.setVariable("progress", "100")
        execution.setVariable("result", "error")
        execution.setVariable("operationContent", "NSSMF Terminate service failure")

        WorkflowException wfex = execution.getVariable("WorkflowException") as WorkflowException
        String errorMessage = wfex.getErrorMessage()
        errorMessage = errorMessage.length() > 200 ? errorMessage.substring(0,200) + "......" : errorMessage
        execution.setVariable("reason",errorMessage)
        updateServiceOperationStatus(execution)

        LOGGER.debug("======== ${PREFIX} COMPLETED prepareEndOperationStatus Process ======== ")
    }

    /**
     * check parameters from request body
     * set to execution
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
