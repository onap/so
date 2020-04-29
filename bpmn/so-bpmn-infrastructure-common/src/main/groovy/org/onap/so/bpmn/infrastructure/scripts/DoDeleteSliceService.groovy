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
import org.onap.aai.domain.yang.SliceProfiles
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.NotFoundException

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
    void queryE2ESliceSeriveFromAAI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start queryE2ESliceSeriveFromAAI *****")
        String serviceInstanceId = execution.getVariable("serviceInstanceId")

        String errorMsg = "query e2e slice service from aai failed"
        AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, serviceInstanceId, errorMsg)
        Optional<ServiceInstance> si =wrapper.asBean(ServiceInstance.class)
        if(si.isPresent())
        {
            String snssai = si.get()?.getEnvironmentContext()
            execution.setVariable("snssai", snssai ?: "")
            LOGGER.info("serviceInstanceId: ${serviceInstanceId}, snssai: ${snssai}")
        }
        LOGGER.trace(" *****${PREFIX} Exit queryE2ESliceSeriveFromAAI *****")
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
            AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.ALLOTTED_RESOURCE_ALL, serviceInstanceId, errorMsg)
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
        try
        {
            String errorMsg = "query nsi from aai failed."
            AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, nsiId, errorMsg)
            Optional<ServiceInstance> si =wrapper.asBean(ServiceInstance.class)
            List<String> nssiIdList = []
            String msg = "nsiId:${nsiId},nssiIdList:"
            if(si.isPresent())
            {
                List<Relationship> relationshipList = si.get().getRelationshipList()?.getRelationship()
                for (Relationship relationship : relationshipList)
                {
                    String relatedTo = relationship.getRelatedTo()
                    if (relatedTo == "service-instance")
                    {
                        String relatedLink = relationship.getRelatedLink()?:""
                        String nssiId = relatedLink ? relatedLink.substring(relatedLink.lastIndexOf("/") + 1,relatedLink.length()) : ""
                        nssiIdList.add(nssiId)
                        msg+="${nssiId}, "
                    }
                }
            }
            LOGGER.info(msg)
            execution.setVariable("nssiIdList", nssiIdList)
        }
        catch(BpmnError e){
            throw e
        }
        catch (Exception ex){
            String msg = "Exception in getNSIFromAAI " + ex.getMessage()
            LOGGER.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        LOGGER.trace(" *****${PREFIX} Exit getNSIFromAAI *****")
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
            AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SERVICE_INSTANCE, nssiId, errorMsg)
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
        int currentIndex = execution.getVariable("currentNSSIIndex") as int
        ServiceInstance nssi = nssiInstanceList?.get(currentIndex)
        def currentNSSI = [:]
        currentNSSI['nssiServiceInstanceId'] = nssi?.getServiceInstanceId()
        currentNSSI['modelInvariantId'] = nssi?.getModelInvariantId()
        currentNSSI['modelVersionId'] = nssi?.getModelVersionId()
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
     * query sliceProfile from AAI
     * save profileId
     * @param execution
     */
    void querySliceProfileFromAAI(DelegateExecution execution)
    {
        LOGGER.trace(" *****${PREFIX} Start querySliceProfileFromAAI *****")
        def currentNSSI = execution.getVariable("currentNSSI")
        String nssiId = currentNSSI['nssiServiceInstanceId']
        String errorMsg = "query slice profile failed"
        AAIResultWrapper wrapper = queryAAI(execution, AAIObjectType.SLICE_PROFILE_ALL, nssiId, errorMsg)
        Optional<SliceProfiles> sliceProfiles =wrapper.asBean(SliceProfiles.class)
        if(sliceProfiles.isPresent())
        {
            String profileId = sliceProfiles.get().getSliceProfile()?.get(0)?.getProfileId()
            currentNSSI['profileId'] =  profileId ?: ""
            LOGGER.info("nssiId: ${nssiId}, profileId: ${profileId}")
        }
        execution.setVariable("currentNSSI", currentNSSI)
        LOGGER.trace(" *****${PREFIX} Exit querySliceProfileFromAAI *****")
    }

    /**
     * query AAI
     * @param execution
     * @param aaiObjectType
     * @param instanceId
     * @return AAIResultWrapper
     */
    private AAIResultWrapper queryAAI(DelegateExecution execution, AAIObjectType aaiObjectType, String instanceId, String errorMsg)
    {
        LOGGER.trace(" *****${PREFIX} Start queryAAI *****")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(aaiObjectType, globalSubscriberId, serviceType, instanceId)
        if (!getAAIClient().exists(resourceUri)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
        }
        AAIResultWrapper wrapper = getAAIClient().get(resourceUri, NotFoundException.class)
        LOGGER.trace(" *****${PREFIX} Exit queryAAI *****")
        return wrapper
    }

}
