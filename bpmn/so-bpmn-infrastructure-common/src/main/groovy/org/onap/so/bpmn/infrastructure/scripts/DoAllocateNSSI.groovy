package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest
import org.onap.so.beans.nsmf.ResponseDescriptor
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.beans.nsmf.SliceTaskInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity

import static org.apache.commons.lang3.StringUtils.isBlank

class DoAllocateNSSI extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DoAllocateNSSI.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    ObjectMapper objectMapper = new ObjectMapper()

    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

    private static final NSSMF_ALLOCATE_URL = "/api/rest/provMns/v1/NSS/SliceProfiles"

    private static final NSSMF_QUERY_JOB_STATUS_URL = "/NSS/jobs/%s"

    @Override
    void preProcessRequest(DelegateExecution execution) {
        logger.trace("Enter preProcessRequest()")

        NssmfAdapterNBIRequest nbiRequest = execution.getVariable("nbiRequest") as NssmfAdapterNBIRequest

        //SliceTaskInfo sliceTaskInfo = execution.getVariable("sliceTaskInfo") as SliceTaskInfo
        boolean isNSIOptionAvailable = execution.getVariable("isNSIOptionAvailable") as Boolean

        if (isNSIOptionAvailable) {
            nbiRequest.serviceInfo.setActionType("modify")
        } else {
            nbiRequest.serviceInfo.setActionType("allocate")
        }
        execution.setVariable("nbiRequest", nbiRequest)
        logger.trace("Exit preProcessRequest")
    }

    /**
     * send Create Request NSSMF
     * @param execution
     */
    void sendCreateRequestNSSMF(DelegateExecution execution) {
        NssmfAdapterNBIRequest nbiRequest = execution.getVariable("nbiRequest") as NssmfAdapterNBIRequest
        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, NSSMF_ALLOCATE_URL,
                objectMapper.writeValueAsString(nbiRequest))
        ResponseEntity responseEntity = objectMapper.readValue(response, ResponseEntity.class)
        String respBody = responseEntity.getBody()
        NssiResponse result = objectMapper.readValue(respBody, NssiResponse.class)
        //todo: if success
        //todo:
        execution.setVariable("serviceInfo", nbiRequest.getServiceInfo())
        execution.setVariable("esrInfo", nbiRequest.getEsrInfo())
    }

    /**
     * query nssi allocate status
     * @param execution
     */
    void queryNSSIStatus(DelegateExecution execution) {
        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()
        NssiResponse nssiAllocateResult = execution.getVariable("nssiAllocateResult") as NssiResponse
        String jobId = nssiAllocateResult.getJobId()
        String nssiId = nssiAllocateResult.getNssiId()

        ServiceInfo serviceInfo = execution.getVariable("serviceInfo") as ServiceInfo
        serviceInfo.setNssiId(nssiId)
        EsrInfo esrInfo = execution.getVariable("esrInfo") as EsrInfo

        //nbiRequest.setResponseId(jobId)
        nbiRequest.setServiceInfo(serviceInfo)
        nbiRequest.setEsrInfo(esrInfo)

        String endpoint = String.format(NSSMF_QUERY_JOB_STATUS_URL, jobId)

        String response =
                nssmfAdapterUtils.sendPostRequestNSSMF(execution, endpoint, objectMapper.writeValueAsString(nbiRequest))

        ResponseEntity responseEntity = objectMapper.readValue(response, ResponseEntity.class)
        String result = responseEntity.getBody()
        //todo；if success
        ResponseDescriptor responseDescriptor = objectMapper.readValue(result, ResponseDescriptor.class)

        //todo: handle status
        execution.setVariable("nssiAllocateResult", responseDescriptor)
    }

    void prepareUpdateOrchestrationTask(DelegateExecution execution) {
        //todo；update orchestration task
    }

    void timeDelay(DelegateExecution execution) {
        //todo: time delay
    }

}
