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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.beans.nsmf.DeAllocateNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.NetworkType
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.domain.ServiceArtifact
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.OperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class DoDeallocateNSSI extends AbstractServiceTaskProcessor
{
    private final String PREFIX ="DoDeallocateNSSI"

    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private JsonUtils jsonUtil = new JsonUtils()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

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
        boolean modifyAction = execution.getVariable("terminateNSI")

        String serviceInvariantUuid = currentNSSI['modelInvariantId']
        String serviceUuid = currentNSSI['modelVersionId']
        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String subscriptionServiceType = execution.getVariable("serviceType")
        
        DeAllocateNssi deAllocateNssi = new DeAllocateNssi()
        deAllocateNssi.setNsiId(nsiId)
        deAllocateNssi.setNssiId(nssiId)
        deAllocateNssi.setTerminateNssiOption(0)
        deAllocateNssi.setSnssaiList(Arrays.asList(snssai))
        deAllocateNssi.setScriptName(scriptName)
        deAllocateNssi.setSliceProfileId(profileId)
        deAllocateNssi.setModifyAction(modifyAction)
        
        ServiceInfo serviceInfo = new ServiceInfo()
        serviceInfo.setServiceInvariantUuid(serviceInvariantUuid)
        serviceInfo.setServiceUuid(serviceUuid)
        serviceInfo.setGlobalSubscriberId(globalSubscriberId)
        serviceInfo.setSubscriptionServiceType(subscriptionServiceType)
        
        EsrInfo esrInfo = getEsrInfo(currentNSSI)
        
        execution.setVariable("deAllocateNssi",deAllocateNssi)
        execution.setVariable("esrInfo",esrInfo)
        execution.setVariable("serviceInfo",serviceInfo)
        String nssmfRequest = """
                {
                  "deAllocateNssi": "${execution.getVariable("deAllocateNssi") as JSONObject}",
                  "esrInfo":  ${execution.getVariable("esrInfo") as JSONObject},
                  "serviceInfo": ${execution.getVariable("serviceInfo") as JSONObject}
                }
              """

        String urlStr = String.format("/api/rest/provMns/v1/NSS/nssi/%s",nssiId)

        NssiResponse nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlStr, nssmfRequest, NssiResponse.class)
        if (nssmfResponse != null) {
            currentNSSI['jobId']= nssmfResponse.getJobId() ?: "" 
            currentNSSI['jobProgress'] = 0            
            execution.setVariable("currentNSSI", currentNSSI)    
            } 
            else {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a Bad Response from NSSMF.")
        }
        LOGGER.debug("*****${PREFIX} Exit sendRequestToNSSMF *****")
    }

/**
     * send to nssmf query progress
     * @param execution
     */
    void prepareJobStatusRequest(DelegateExecution execution)
    {
        def currentNSSI = execution.getVariable("currentNSSI")
        String jobId = currentNSSI['jobId']
        String nssiId = currentNSSI['nssiServiceInstanceId']
        String nsiId = currentNSSI['nsiServiceInstanceId']
        String serviceInvariantUuid = currentNSSI['modelInvariantId']
        String serviceUuid = currentNSSI['modelVersionId']
        String globalSubscriberId = currentNSSI['globalSubscriberId']
        String subscriptionServiceType = execution.getVariable("serviceType")
        String sST =  currentNSSI['sST']
        String PLMNIdList = currentNSSI['PLMNIdList']
        String nssiName = currentNSSI['nssiName']
        
        execution.setVariable("responseId", "3")
        execution.setVariable("esrInfo", getEsrInfo(currentNSSI))
        execution.setVariable("jobId", jobId)
        
        Map<String, ?> serviceInfoMap = new HashMap<>()
        serviceInfoMap.put("nssiId", nssiId)
        serviceInfoMap.put("nsiId", nsiId)
        serviceInfoMap.put("nssiName", nssiName)
        serviceInfoMap.put("sST", sST)
        serviceInfoMap.put("PLMNIdList", PLMNIdList)
        serviceInfoMap.put("globalSubscriberId", globalSubscriberId)
        serviceInfoMap.put("subscriptionServiceType", subscriptionServiceType)
        serviceInfoMap.put("serviceInvariantUuid", serviceInvariantUuid)
        serviceInfoMap.put("serviceUuid", serviceUuid)
    
        execution.setVariable("serviceInfo", serviceInfoMap)
    }

    
    /**
     * send to nssmf query progress
     * @param execution
     */
    void handleJobStatus(DelegateExecution execution)
    {
        try 
        {
        String jobStatusResponse = execution.getVariable("responseDescriptor")
        String status = jsonUtil.getJsonValue(jobStatusResponse,"status")
        def statusDescription = jsonUtil.getJsonValue(jobStatusResponse,"statusDescription")
        def progress = jsonUtil.getJsonValue(jobStatusResponse,"progress")
        if(!status.equalsIgnoreCase("failed"))
        {
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
            currentNSSI['status'] = status
            currentNSSI['statusDescription'] = statusDescription

            LOGGER.debug("job status result: nsiId = ${nsiId}, nssiId=${nssiId}, oldProgress=${oldProgress}, progress = ${currentProgress}" )
        }
          else {
            execution.setVariable("isNeedUpdateDB", "true")
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Received a Bad Response from NSSMF.")
        }
        }
        catch (any)
        {
            String msg = "Received a Bad Response from NSSMF." cause-"+any.getCause()"
            LOGGER.error(any.printStackTrace())
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
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
    void prepareUpdateOperationStatus(DelegateExecution execution)
    {
        def currentNSSI = execution.getVariable("currentNSSI")
        int currentProgress = currentNSSI["jobProgress"]
        def proportion = currentNSSI['proportion']
        def statusDes = currentNSSI["statusDescription"]
        int progress = (currentProgress as int) == 0 ? 0 : (currentProgress as int) / 100 * (proportion as int)
        def status = currentNSSI['status']
        
        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId(currentNSSI['e2eServiceInstanceId'] as String)
        operationStatus.setOperationId(currentNSSI['operationId'] as String)
        operationStatus.setOperation("DELETE")
        operationStatus.setResult(status as String)
        operationStatus.setProgress(progress as String)
        operationStatus.setOperationContent(statusDes as String)
        requestDBUtil.prepareUpdateOperationStatus(execution, operationStatus)
        LOGGER.debug("update operation, currentProgress=${currentProgress}, proportion=${proportion}, progress = ${progress}" )
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
            AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(nssiServiceInstanceId).sliceProfile(profileId))
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
