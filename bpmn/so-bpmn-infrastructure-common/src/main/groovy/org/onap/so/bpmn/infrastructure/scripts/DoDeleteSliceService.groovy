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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aai.domain.yang.AllottedResources
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectName
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.client.oof.adapter.beans.payload.OofRequest
import org.onap.so.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper

import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoDeleteSliceService.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId - O
 * @param - subscriptionServiceType - O
 * @param - serviceInstanceId
 *
 */
class DoDeleteSliceService extends AbstractServiceTaskProcessor {
    private final String PREFIX ="DoDeleteSliceService"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    OofUtils oofUtils = new OofUtils()
    private static final Logger LOGGER = LoggerFactory.getLogger( DoDeleteSliceService.class)

    @Override
    void preProcessRequest(DelegateExecution execution) {
        LOGGER.debug(" *****${PREFIX} preProcessRequest *****")
        String msg = ""

        try {
            //String requestId = execution.getVariable("msoRequestId")
            execution.setVariable("prefix",PREFIX)

            //Inputs
            //requestDetails.subscriberInfo. for AAI GET & PUT
             execution.getVariable("globalSubscriberId") ?: execution.setVariable("globalSubscriberId", "")

            //requestDetails.requestParameters. for AAI PUT
            execution.getVariable("serviceType") ?: execution.setVariable("serviceType", "")

            //Generated in parent for AAI PUT
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)){
                msg = "Input serviceInstanceId is null"
                LOGGER.info(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.debug("*****${PREFIX} Exit preProcessRequest *****")
    }

    /**
     * query E2ESliceService from AAI
     * save snssai
     * @param execution
     */
    void queryServiceProfileFromAAI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start queryE2ESliceSeriveFromAAI *****")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        try
        {
        String errorMsg = "query e2e slice service from aai failed"
        AAIResultWrapper wrapper = queryAAI(execution, Types.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
        Optional<ServiceInstance> si =wrapper.asBean(ServiceInstance.class)
        if(si.isPresent())
        {
            String snssai = si.get()?.getEnvironmentContext()
            execution.setVariable("snssai", snssai ?: "")
//            ServiceProfiles serviceProfiles = si.get()?.getServiceProfiles()
//            ServiceProfile serviceProfile = serviceProfiles.getServiceProfile().get(0)
//            String serviceProfileId = serviceProfile ? serviceProfile.getProfileId() : ""
//            execution.setVariable("serviceProfileId", serviceProfileId)
            List<ServiceInstance> sliceProfileList = []
            List<Relationship> relationshipList = si.get().getRelationshipList().getRelationship()
            for (Relationship relationship : relationshipList) {
                String relatedTo = relationship.getRelatedTo()
                if (relatedTo.toLowerCase() == "service-instance") {
                    String relatioshipurl = relationship.getRelatedLink()
                    String instanceId = relatioshipurl.substring(relatioshipurl.lastIndexOf("/") + 1, relatioshipurl.length())
                    AAIResultWrapper wrapper1 = queryAAI(execution, Types.SERVICE_INSTANCE, instanceId, errorMsg)
                    Optional<ServiceInstance> serviceInstance = wrapper1.asBean(ServiceInstance.class)
                    if (serviceInstance.isPresent()) {
                        ServiceInstance instance = serviceInstance.get()
                        if ("slice-profile".equalsIgnoreCase(instance.getServiceRole())) {
                            sliceProfileList.add(instance)
                        }
                    }
                }
            }
            execution.setVariable("sliceProfileList",sliceProfileList)
            LOGGER.info("serviceInstanceId: ${serviceInstanceId}, snssai: ${snssai}, sliceProfileList: ${sliceProfileList}")
        }
        LOGGER.trace(" *****${PREFIX} Exit queryE2ESliceSeriveFromAAI *****")
        }
        catch (any)
        {
            String msg = "query E2E slice service from aai failed! cause-"+any.getCause()
            LOGGER.error(any.printStackTrace())
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }
    }

    /**
     * get allotted resource from AAI
     * save nsi id
     * @param execution
     */
    void getAllottedResFromAAI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start getAllottedResFromAAI *****")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        try
        {
            String errorMsg = "query allotted resource from aai failed."
            AAIResultWrapper wrapper = queryAAI(execution, Types.ALLOTTED_RESOURCE, serviceInstanceId, errorMsg)
            Optional<AllottedResources> ars = wrapper?.asBean(AllottedResources.class)
            if(ars.isPresent() && ars.get().getAllottedResource())
            {
                List<AllottedResource> allottedResourceList = ars.get().getAllottedResource()
                AllottedResource ar = allottedResourceList.first()
                String relatedLink = ar?.getRelationshipList()?.getRelationship()?.first()?.getRelatedLink()
                String nsiId = relatedLink ? relatedLink.substring(relatedLink.lastIndexOf("/") + 1,relatedLink.length()) : ""
                execution.setVariable("nsiId", nsiId)
                LOGGER.info("serviceInstanceId: ${serviceInstanceId}, nsiId:${nsiId}")
            }
        }
        catch(BpmnError e){
            throw e
        }
        catch (Exception ex){
            String msg = "Exception in getAllottedResFromAAI " + ex.getMessage()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.trace(" *****${PREFIX} Exit getAllottedResFromAAI *****")
    }

    /**
     * get nsi service instance from aai
     * save nssi id
     * @param execution
     */
    void getNSIFromAAI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start getNSIFromAAI *****")
        String nsiId = execution.getVariable("nsiId")
        List<String> nssiIdList = getNSSIIdList(execution, nsiId)
        String msg = "nsiId: ${nsiId}, nssiIdList:"
        msg+= nssiIdList.join(",")
        LOGGER.info(msg)
        execution.setVariable("nssiIdList", nssiIdList)
        LOGGER.trace(" *****${PREFIX} Exit getNSIFromAAI *****")
    }
    /**
     * Get NSSI Id from AAI
     * @param execution
     * @param nsiId
     * @return
     */
    private List<String> getNSSIIdList(DelegateExecution execution, String nsiId){
        List<String> nssiIdList = []

        try
        {
            String errorMsg = "query nssi from aai failed."
            AAIResultWrapper wrapper = queryAAI(execution, Types.SERVICE_INSTANCE, nsiId, errorMsg)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            if(si.isPresent())
            {
                List<Relationship> relationshipList = si.get().getRelationshipList()?.getRelationship()
                for (Relationship relationship : relationshipList)
                {
                    String relatedTo = relationship.getRelatedTo()
                    if (relatedTo == "service-instance")
                    {
                        String relatedLink = relationship.getRelatedLink()?:""
                        String instanceId = relatedLink ? relatedLink.substring(relatedLink.lastIndexOf("/") + 1,relatedLink.length()) : ""
                        AAIResultWrapper wrapper1 = queryAAI(execution, Types.SERVICE_INSTANCE, instanceId, errorMsg)
                        Optional<ServiceInstance> serviceInstance = wrapper1.asBean(ServiceInstance.class)
                        def nssiId
                        if (serviceInstance.isPresent()) {
                            ServiceInstance instance = serviceInstance.get()
                            if ("nssi".equalsIgnoreCase(instance.getServiceRole())) {
                                nssiId = instance.getServiceInstanceId()
                                nssiIdList.add(nssiId)
                            }
                        }
                    }
                }
            }
        }
        catch(BpmnError e){
            throw e
        }
        catch (Exception ex){
            String msg = "Exception in getNSIFromAAI " + ex.getMessage()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        return nssiIdList
    }

    /**
     * get nssi service from AAI
     * prepare list
     * @param execution
     */
    void getNSSIListFromAAI(DelegateExecution execution)
    {
        LOGGER.trace("*****${PREFIX} Start getNSSIListFromAAI *****")
        List<String> nssiIdList = execution.getVariable("nssiIdList")
        List<ServiceInstance> nssiInstanceList = []
        String errorMsg = "query nssi list from aai failed"
        for(String nssiId : nssiIdList)
        {
            AAIResultWrapper wrapper = queryAAI(execution, Types.SERVICE_INSTANCE, nssiId, errorMsg)
            Optional<ServiceInstance> si =wrapper.asBean(ServiceInstance.class)
            if(si.isPresent())
            {
                nssiInstanceList.add(si.get())
            }
        }
        int size = nssiInstanceList.size()
        int proportion = size >0 ?((90/size) as int) : 90
        execution.setVariable("nssiInstanceList", nssiInstanceList)
        execution.setVariable("currentNSSIIndex", 0)
        execution.setVariable("proportion", proportion)
        String msg ="nssiInstanceList size: ${nssiInstanceList.size()}, proportion:${proportion}"
        LOGGER.info(msg)
        LOGGER.trace(" *****${PREFIX} Exit getNSSIListFromAAI *****")
    }

    /**
     * get current NSSI
     * @param execution
     */
    void getCurrentNSSI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start getCurrentNSSI *****")
        List<ServiceInstance> nssiInstanceList = execution.getVariable("nssiInstanceList")
        List<ServiceInstance> sliceProfileList = execution.getVariable("sliceProfileList")
        int currentIndex = execution.getVariable("currentNSSIIndex") as int
        String profileInstId = ""
        ServiceInstance nssi = nssiInstanceList?.get(currentIndex)
        List<Relationship> relationshipList = nssi.getRelationshipList()?.getRelationship()
        for(ServiceInstance sliceProfileInstance : sliceProfileList) {
            for (Relationship relationship : relationshipList) {
                String relatedTo = relationship.getRelatedTo()
                if (relatedTo == "service-instance"){
                    String relatedLink = relationship.getRelatedLink()?:""
                    String instanceId = relatedLink ? relatedLink.substring(relatedLink.lastIndexOf("/") + 1,relatedLink.length()) : ""
                    if(instanceId.equals(sliceProfileInstance.getServiceInstanceId())){
                        profileInstId = sliceProfileInstance.getServiceInstanceId()
                        break
                    }
                }
            }
            if(profileInstId){
                break
            }
        }

        //@TODO Temp begin*******************
//        AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("serviceType")).serviceInstance(profileInstId).sliceProfiles())
//        AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
//        Optional<SliceProfiles> sliceProfilesOpt =wrapper.asBean(SliceProfiles.class)
//        SliceProfiles sliceProfiles
//        String sliceProfileId
//        if(sliceProfilesOpt.isPresent()){
//            sliceProfiles = sliceProfilesOpt.get()
//            org.onap.aai.domain.yang.SliceProfile sliceProfile = sliceProfiles.getSliceProfile().get(0)
//            sliceProfileId = sliceProfile ? sliceProfile.getProfileId() : ""
//        }
        //@TODO Temp end*******************

        def currentNSSI = [:]
        currentNSSI['nssiServiceInstanceId'] = nssi?.getServiceInstanceId()
        currentNSSI['modelInvariantId'] = nssi?.getModelInvariantId()
        currentNSSI['modelVersionId'] = nssi?.getModelVersionId()
        currentNSSI['nssiName'] = nssi?.getServiceInstanceName()
        currentNSSI['sST'] = nssi?.getServiceType()
        currentNSSI['PLMNIdList'] = nssi?.getServiceInstanceLocationId()
        //@TODO Temp

        currentNSSI['profileId'] =  profileInstId
//        currentNSSI['profileId'] =  sliceProfileId
        currentNSSI['snssai'] = execution.getVariable("snssai") ?: ""
        currentNSSI['nsiServiceInstanceId'] = execution.getVariable("nsiId") ?: ""
        currentNSSI['operationId'] = execution.getVariable("operationId") ?: ""
        currentNSSI['e2eServiceInstanceId'] = execution.getVariable("serviceInstanceId") ?: ""
        currentNSSI['msoRequestId'] = execution.getVariable("msoRequestId") ?: ""
        currentNSSI['globalSubscriberId'] = execution.getVariable("globalSubscriberId") ?: ""
        currentNSSI['serviceType'] = execution.getVariable("serviceType") ?: ""
        currentNSSI['serviceModelInfo'] = execution.getVariable("serviceModelInfo") ?: ""
        currentNSSI['proportion'] = (execution.getVariable("proportion") as int)*(currentIndex+1)
        execution.setVariable("currentNSSI", currentNSSI)
        String msg = "Now we deal with nssiServiceInstanceId: ${currentNSSI['nssiServiceInstanceId']}, current Index: ${currentIndex}, current proportion:${currentNSSI['proportion']}"
        LOGGER.info(msg)
        LOGGER.trace(" *****${PREFIX} Exit getCurrentNSSI *****")
    }

    /**
     * parse next nssi
     * @param execution
     */
    void parseNextNSSI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start parseNextNSSI *****")
        if(execution.getVariable("WorkflowException") != null){
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "current job failure!")
        }
        def currentIndex = execution.getVariable("currentNSSIIndex")
        List<ServiceInstance> nssiInstanceList = execution.getVariable("nssiInstanceList")
        def nextIndex = ++currentIndex
        LOGGER.info("nextIndex: ${nextIndex}")
        if(nextIndex >= nssiInstanceList.size()){
            execution.setVariable("isAllNSSIFinished", "true")
        }else{
            execution.setVariable("isAllNSSIFinished", "false")
            execution.setVariable("currentNSSIIndex", nextIndex)
        }
        LOGGER.trace(" *****${PREFIX} Exit parseNextNSSI *****")
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
        LOGGER.trace(" *****${PREFIX} Start queryAAI *****")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")

        org.onap.aaiclient.client.generated.fluentbuilders.ServiceInstance serviceInstanceType = AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(instanceId)
        def type
        if (aaiObjectName == Types.ALLOTTED_RESOURCE) {
            type = serviceInstanceType.allottedResources()
        } else if (aaiObjectName == Types.SLICE_PROFILES) {
            type = serviceInstanceType.sliceProfiles()
        } else {
            type = serviceInstanceType
        }
        def uri = AAIUriFactory.createResourceUri(type)
        if (!getAAIClient().exists(uri)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
        }
        AAIResultWrapper wrapper = getAAIClient().get(uri, NotFoundException.class)
        LOGGER.trace(" *****${PREFIX} Exit queryAAI *****")
        return wrapper
    }

    void terminateNSIQuery(DelegateExecution execution)
    {
        LOGGER.debug("Start terminateNSIQuery")

        //return

        //To test
        String requestId = execution.getVariable("msoRequestId")
        String nxlId = execution.getVariable("nsiId")
        //String nxlId = currentNSSI['nsiServiceInstanceId']
        String nxlType = "NSI"
        String messageType = "nsiTerminationResponse"
        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
        String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuthValue = utils.encrypt(basicAuth, msokey)
        if (basicAuthValue != null) {
            LOGGER.debug( "Obtained BasicAuth username and password for OOF: " + basicAuthValue)
            try {
                authHeader = utils.getBasicAuth(basicAuthValue, msokey)
                execution.setVariable("BasicAuthHeaderValue", authHeader)
            } catch (Exception ex) {
                LOGGER.debug( "Unable to encode username and password string: " + ex)
                exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to " +
                        "encode username and password string")
            }
        } else {
            LOGGER.debug( "Unable to obtain BasicAuth - BasicAuth value null")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                    "value null")
        }
        String oofUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.endpoint", execution)
        URL requestUrl = new URL(oofUrl)
        String oofRequest = oofUtils.buildTerminateNxiRequest(requestId, nxlId, nxlType, messageType, serviceInstanceId)
        OofRequest oofPayload = new OofRequest()
	oofPayload.setApiPath("/api/oof/terminate/nxi/v1")
	oofPayload.setRequestDetails(oofRequest)
	ObjectMapper objectMapper = new ObjectMapper()
	String requestJson = objectMapper.writeValueAsString(oofPayload)
        HttpClient httpClient = new HttpClientFactory().newJsonClient(requestUrl, ONAPComponents.OOF)
        httpClient.addAdditionalHeader("Authorization", authHeader)
        Response httpResponse = httpClient.post(requestJson)

        int responseCode = httpResponse.getStatus()
        LOGGER.debug("OOF sync response code is: " + responseCode)

        if(responseCode != 200){
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
        }       
        try {
            Map<String, String> resMap = httpResponse.readEntity(Map.class)
            boolean terminateResponse = resMap.get("terminateResponse")
            execution.setVariable("terminateNSI", terminateResponse)
        } catch (Exception ex) {
            LOGGER.debug( "Failed to get terminate Response suggested by OOF.")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Failed to get terminate Response suggested by OOF.")
        }
        LOGGER.debug("Finish terminateNSIQuery")
    }


    /**
     * If no nssi,delete NSI from AAI
     * @param execution
     */
    void deleteNSIInstance(DelegateExecution execution){
        def currentNSSI = execution.getVariable("currentNSSI")
        def nsiId = currentNSSI['nsiServiceInstanceId']
        List<String> nssiIdList = getNSSIIdList(execution, nsiId)
        try
        {
            if(0 == nssiIdList.size()){
                AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("serviceType")).serviceInstance(nsiId))
                getAAIClient().delete(serviceInstanceUri)
            }
        } catch (Exception ex) {
            LOGGER.debug( "Failed to delete NSI instance.")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Failed to delete NSI instance.")
        }

    }
}
