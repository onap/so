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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.DeAllocateNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.JobStatusRequest
import org.onap.so.beans.nsmf.JobStatusResponse
import org.onap.so.beans.nsmf.NetworkType
import org.onap.so.beans.nsmf.NssiDeAllocateRequest
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceArtifact
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.Response


class DoDeallocateNSSI extends AbstractServiceTaskProcessor
{
    private final String PREFIX ="DoDeallocateNSSI"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private JsonUtils jsonUtil = new JsonUtils()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private static final Logger LOGGER = LoggerFactory.getLogger( DoDeallocateNSSI.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.trace(" ***** ${PREFIX} Start preProcessRequest *****")

        def currentNSSI = execution.getVariable("currentNSSI")
        if (!currentNSSI) {
            String msg = "currentNSSI is null"
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }

        LOGGER.trace("***** ${PREFIX} Exit preProcessRequest *****")
    }

    /**
     *
     * @param execution
     */
    void prepareDecomposeService(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start prepareDecomposeService *****")
        try
        {
            def currentNSSI = execution.getVariable("currentNSSI")
            String modelInvariantUuid = currentNSSI['modelInvariantId']
            String modelVersionId = currentNSSI['modelVersionId']
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelVersionId}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)
        }
        catch (any)
        {
            String exceptionMessage = "Bpmn error encountered in deallocate nssi. Unexpected Error from method prepareDecomposeService() - " + any.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        LOGGER.debug(" ***** ${PREFIX} Exit prepareDecomposeService *****")
    }

    /**
     * get vendor Info
     * @param execution
     */
     void processDecomposition(DelegateExecution execution) {
        LOGGER.debug("*****${PREFIX} start processDecomposition *****")

        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition") as ServiceDecomposition
            ServiceArtifact serviceArtifact = serviceDecomposition ?.getServiceInfo()?.getServiceArtifact()?.get(0)
            String content = serviceArtifact.getContent()
            String vendor = jsonUtil.getJsonValue(content, "metadata.vendor")
            String domainType  = jsonUtil.getJsonValue(content, "metadata.domainType")

            def currentNSSI = execution.getVariable("currentNSSI")
            currentNSSI['vendor'] = vendor
            currentNSSI['domainType'] = domainType
            LOGGER.info("processDecomposition, current vendor-domainType:" +String.join("-", vendor, domainType))

        } catch (any) {
            String exceptionMessage = "Bpmn error encountered in deallocate nssi. processDecomposition() - " + any.getMessage()
            LOGGER.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        LOGGER.debug("*****${PREFIX} Exit processDecomposition *****")
    }

    /**
     * send deallocate request to nssmf
     * @param execution
     */
    void sendRequestToNSSMF(DelegateExecution execution)
    {
        LOGGER.debug("*****${PREFIX} start sendRequestToNSSMF *****")
        def currentNSSI = execution.getVariable("currentNSSI")
        String snssai= currentNSSI['snssai']
        String profileId = currentNSSI['profileId']
        String nssiId = currentNSSI['nssiServiceInstanceId']
        String nsiId = currentNSSI['nsiServiceInstanceId']
        String scriptName = execution.getVariable("scriptName")

        DeAllocateNssi deAllocateNssi = new DeAllocateNssi()
        deAllocateNssi.setNsiId(nsiId)
        deAllocateNssi.setNssiId(nssiId)
        deAllocateNssi.setTerminateNssiOption(0)
        deAllocateNssi.setSnssaiList(Arrays.asList(snssai))
        deAllocateNssi.setScriptName(scriptName)

        NssiDeAllocateRequest deAllocateRequest = new NssiDeAllocateRequest()
        deAllocateRequest.setDeAllocateNssi(deAllocateNssi)
        deAllocateRequest.setEsrInfo(getEsrInfo(currentNSSI))

        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(deAllocateRequest)

        //Prepare auth for NSSMF - Begin
        String nssmfRequest = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint", execution)
        nssmfRequest = nssmfRequest + String.format("/api/rest/provMns/v1/NSS/SliceProfiles/%s",profileId)
        //nssmfRequest = nssmfRequest + String.format(NssmfAdapterUtil.NSSMI_DEALLOCATE_URL,profileId)
        //send request to active  NSSI TN option
        URL url = new URL(nssmfRequest)
        LOGGER.info("deallocate nssmfRequest:${nssmfRequest}, reqBody: ${json}")

        HttpClient httpClient = getHttpClientFactory().newJsonClient(url, ONAPComponents.EXTERNAL)
        Response httpResponse = httpClient.post(json)
        checkNssmfResponse(httpResponse, execution)

        NssiResponse nssmfResponse = httpResponse.readEntity(NssiResponse.class)
        currentNSSI['jobId']= nssmfResponse.getJobId() ?: ""
        currentNSSI['jobProgress'] = 0
        execution.setVariable("currentNSSI", currentNSSI)

        LOGGER.debug("*****${PREFIX} Exit sendRequestToNSSMF *****")
    }

    /**
     * send to nssmf query progress
     * @param execution
     */
    void getJobStatus(DelegateExecution execution)
    {
        def currentNSSI = execution.getVariable("currentNSSI")
        String jobId = currentNSSI['jobId']
        String nssiId = currentNSSI['nssiServiceInstanceId']
        String nsiId = currentNSSI['nsiServiceInstanceId']

        JobStatusRequest jobStatusRequest = new JobStatusRequest()
        jobStatusRequest.setNssiId(nssiId)
        jobStatusRequest.setNsiId(nsiId)
        jobStatusRequest.setEsrInfo(getEsrInfo(currentNSSI))

        ObjectMapper mapper = new ObjectMapper()
        String json = mapper.writeValueAsString(jobStatusRequest)

        //Prepare auth for NSSMF - Begin
        String nssmfRequest = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint", execution)
        nssmfRequest = nssmfRequest + String.format("/api/rest/provMns/v1/NSS/jobs/%s",jobId)
        //send request to active  NSSI TN option
        URL url = new URL(nssmfRequest)
        LOGGER.info("get deallocate job status, nssmfRequest:${nssmfRequest}, requestBody: ${json}")

        HttpClient httpClient = getHttpClientFactory().newJsonClient(url, ONAPComponents.EXTERNAL)
        Response httpResponse = httpClient.post(json)
        checkNssmfResponse(httpResponse, execution)

        JobStatusResponse jobStatusResponse = httpResponse.readEntity(JobStatusResponse.class)
        def progress = jobStatusResponse?.getResponseDescriptor()?.getProgress()
        if(!progress)
        {
            LOGGER.error("job progress is null or empty!")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a Bad Job progress from NSSMF.")
        }
        int oldProgress = currentNSSI['jobProgress']
        int currentProgress = progress

        execution.setVariable("isNSSIDeAllocated", (currentProgress == 100))
        execution.setVariable("isNeedUpdateDB", (oldProgress != currentProgress))
        currentNSSI['jobProgress'] = currentProgress

        def statusDescription = jobStatusResponse?.getResponseDescriptor()?.getStatusDescription()
        currentNSSI['statusDescription'] = statusDescription

        LOGGER.debug("job status result: nsiId = ${nsiId}, nssiId=${nssiId}, oldProgress=${oldProgress}, progress = ${currentProgress}" )
    }

    private void checkNssmfResponse(Response httpResponse, DelegateExecution execution) {
        int responseCode = httpResponse.getStatus()
        LOGGER.debug("NSSMF response code is: " + responseCode)

        if ( responseCode < 200 || responseCode > 204 || !httpResponse.hasEntity()) {
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Response from NSSMF.")
        }
    }


    private EsrInfo getEsrInfo(def currentNSSI)
    {
        String domaintype = currentNSSI['domainType']
        String vendor = currentNSSI['vendor']

        EsrInfo info = new EsrInfo()
        info.setNetworkType(NetworkType.fromString(domaintype))
        info.setVendor(vendor)
        return info
    }

    /**
     * handle job status
     * prepare update requestdb
     * @param execution
     */
    void handleJobStatus(DelegateExecution execution)
    {
        def currentNSSI = execution.getVariable("currentNSSI")
        int currentProgress = currentNSSI["jobProgress"]
        def proportion = currentNSSI['proportion']
        def statusDes = currentNSSI["statusDescription"]
        int progress = (currentProgress as int) == 0 ? 0 : (currentProgress as int) / 100 * (proportion as int)

        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId(currentNSSI['e2eServiceInstanceId'] as String)
        operationStatus.setOperationId(currentNSSI['operationId'] as String)
        operationStatus.setOperation("DELETE")
        operationStatus.setResult("processing")
        operationStatus.setProgress(progress as String)
        operationStatus.setOperationContent(statusDes as String)
        requestDBUtil.prepareUpdateOperationStatus(execution, operationStatus)
        LOGGER.debug("update operation, currentProgress=${currentProgress}, proportion=${proportion}, progress = ${progress}" )
    }

    void timeDelay(DelegateExecution execution) {
        try {
            Thread.sleep(10000);
        } catch(InterruptedException e) {
            LOGGER.error("Time Delay exception" + e)
        }
    }

    /**
     * delete slice profile from aai
     * @param execution
     */
    void delSliceProfileFromAAI(DelegateExecution execution)
    {
        LOGGER.debug("*****${PREFIX} start delSliceProfileFromAAI *****")
        def currentNSSI = execution.getVariable("currentNSSI")
        String nssiServiceInstanceId = currentNSSI['nssiServiceInstanceId']
        String profileId = currentNSSI['profileId']
        String globalSubscriberId = currentNSSI["globalSubscriberId"]
        String serviceType = currentNSSI["serviceType"]

        try
        {
            LOGGER.debug("delete nssiServiceInstanceId:${nssiServiceInstanceId}, profileId:${profileId}")
            AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, globalSubscriberId, serviceType, nssiServiceInstanceId, profileId)
            if (!getAAIClient().exists(resourceUri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
            }
            getAAIClient().delete(resourceUri)
        }
        catch (any)
        {
            String msg = "delete slice profile from aai failed! cause-"+any.getCause()
            LOGGER.error(any.printStackTrace())
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.debug("*****${PREFIX} Exist delSliceProfileFromAAI *****")
    }
}
