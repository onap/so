/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.oof.adapter.beans.payload.OofRequest
import javax.ws.rs.core.Response
import org.onap.so.logging.filter.base.ONAPComponents
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isEmpty

class DoDeallocateTnNssi extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DoDeallocateTnNssi.class)
    private static final ObjectMapper objectMapper = new ObjectMapper()
    String Prefix = "TNDEALLOC_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()
    OofUtils oofUtils = new OofUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()


    void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")

        execution.setVariable("startTime", System.currentTimeMillis())
        String msg = tnNssmfUtils.getExecutionInputParams(execution)
        logger.debug("Deallocate TN NSSI input parameters: " + msg)

        execution.setVariable("prefix", Prefix)

        tnNssmfUtils.setSdncCallbackUrl(execution, true)
        logger.debug("SDNC Callback URL: " + execution.getVariable("sdncCallbackUrl"))

        String sliceServiceInstanceId = execution.getVariable("serviceInstanceID")
        execution.setVariable("sliceServiceInstanceId", sliceServiceInstanceId)

        String sliceServiceInstanceName = execution.getVariable("servicename")
        execution.setVariable("sliceServiceInstanceName", sliceServiceInstanceName)

        String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
        String modelUuid = execution.getVariable("modelUuid")
        if (isEmpty(modelUuid)) {
            modelUuid = tnNssmfUtils.getModelUuidFromServiceInstance(execution.getVariable("serviceInstanceID"))
        }
        def isDebugLogEnabled = true
        execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)

        String additionalPropJsonStr = execution.getVariable("sliceParams")
        if (isBlank(additionalPropJsonStr) ||
                isBlank(tnNssmfUtils.setExecVarFromJsonIfExists(execution,
                        additionalPropJsonStr,
                        "enableSdnc", "enableSdnc"))) {
            tnNssmfUtils.setEnableSdncConfig(execution)
        }
        if (isBlank(additionalPropJsonStr) ||
                isBlank(tnNssmfUtils.setExecVarFromJsonIfExists(execution,
                        additionalPropJsonStr,
                        "enableOof", "enableOof"))) {
            tnNssmfUtils.setEnableOofConfig(execution)
        }

		String nsiId = jsonUtil.getJsonValue(additionalPropJsonStr, "nsiId")
        execution.setVariable("nsiId", nsiId)
        logger.debug("Finish preProcessRequest")
    }

    void prepareOOFNssiTerminationRequest(DelegateExecution execution) {
        logger.debug("Start prepareOOFTnNssiTerminationRequest")
		String requestId = execution.getVariable("msoRequestId")
		String messageType = "TN_NSSITermination"
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
		String serviceInstanceId = execution.getVariable("sliceServiceInstanceId")

        String relatedNsiId = execution.getVariable("nsiId")

		String oofRequest = oofUtils.buildTerminateNxiRequest(requestId,serviceInstanceId, "NSSI",messageType,relatedNsiId)
		execution.setVariable("oofTnNssiPayload", oofRequest)
		logger.debug("Finish prepareOOFTnNssiTerminationRequest")
    }

    void performOofNSSITerminationCall(DelegateExecution execution) {
        boolean terminateTnNSSI = callOofAdapter(execution,execution.getVariable("oofTnNssiPayload"))
		execution.setVariable("terminateTnNSSI", terminateTnNSSI)
    }

    /**
	 * @param execution
	 * @param oofRequest - Request payload to be sent to adapter
	 * @return
	 */
	boolean callOofAdapter(DelegateExecution execution, Object oofRequest) {
		logger.debug("Start callOofAdapter")
		String requestId = execution.getVariable("msoRequestId")
		String oofAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.oof.endpoint", execution)
		URL requestUrl = new URL(oofAdapterEndpoint)
		OofRequest oofPayload = new OofRequest()
		oofPayload.setApiPath("/api/oof/terminate/nxi/v1")
		oofPayload.setRequestDetails(oofRequest)
		String requestJson = objectMapper.writeValueAsString(oofPayload)
		logger.debug("Calling OOF adapter  : ${requestUrl} with payload : ${requestJson}")
		HttpClient httpClient = new HttpClientFactory().newJsonClient(requestUrl, ONAPComponents.EXTERNAL)
		Response httpResponse = httpClient.post(requestJson)
		int responseCode = httpResponse.getStatus()
		logger.debug("OOF sync response code is: " + responseCode)
		if(responseCode < 200 || responseCode >= 300){
			logger.debug("OOF request failed with reason : " + httpResponse)
			exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
		}else {
			Map<String,Object> response = objectMapper.readValue(httpResponse.getEntity(),Map.class)
			boolean terminateResponse =  response.get("terminateResponse")
			if(!terminateResponse) {
				logger.debug("Terminate response is false because " + response.get("reason"))
			}
			return terminateResponse
		}
	}

    void preprocessSdncDeallocateTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncDeallocateTnNssiRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceID")

            String sdncRequest = tnNssmfUtils.buildSDNCRequest(execution, serviceInstanceId, "delete")

            execution.setVariable("TNNSSMF_SDNCRequest", sdncRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + sdncRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preprocessSdncDeallocateTnNssiRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preprocessSdncDeallocateTnNssiRequest Process")
    }


    void validateSDNCResponse(DelegateExecution execution, String response, String method) {
        tnNssmfUtils.validateSDNCResponse(execution, response, method)
    }

    void deleteServiceInstance(DelegateExecution execution) {
        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(execution.getVariable("serviceInstanceID")))
            client.delete(uri)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoDeallocateTnNssi.deleteServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    public void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String tnNssiId = execution.getVariable("sliceServiceInstanceId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(tnNssiId))
            client.update(uri, si)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in CreateSliceService.updateAAIOrchStatus " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        logger.debug("Finish updateAAIOrchStatus")
    }

    void prepareUpdateJobStatus(DelegateExecution execution,
                                String status,
                                String progress,
                                String statusDescription) {
        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
        String modelUuid = execution.getVariable("modelUuid")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")

        ResourceOperationStatus roStatus = tnNssmfUtils.buildRoStatus(modelUuid, ssInstanceId,
                jobId, nsiId, "DEALLOCATE", status, progress, statusDescription)

        logger.debug("DoDeallocateTnNssi: roStatus={}", roStatus)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, roStatus)
    }
}
