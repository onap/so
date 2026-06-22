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
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectName
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import jakarta.ws.rs.NotFoundException

class AAISliceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AAISliceUtil.class);
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    /**
     * Get NSSI Id from AAI
     * @param execution
     * @param nsiId
     * @return
     */
    List<String> getNSSIIdList(DelegateExecution execution, String nsiId){
        List<String> nssiIdList = []

        try
        {
            String errorMsg = "query nssi from aai failed."
            AAIResultWrapper wrapper = queryAAI(execution, AAIFluentTypeBuilder.Types.SERVICE_INSTANCE, nsiId, errorMsg)
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
                        AAIResultWrapper wrapper1 = queryAAI(execution, AAIFluentTypeBuilder.Types.SERVICE_INSTANCE, instanceId, errorMsg)
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
    List<ServiceInstance> getNSSIListFromAAI(DelegateExecution execution, List<String> nssiIdList)
    {
        LOGGER.trace("***** Start getNSSIListFromAAI *****")
        List<ServiceInstance> nssiInstanceList = []
        String errorMsg = "query nssi list from aai failed"
        for(String nssiId : nssiIdList){
            AAIResultWrapper wrapper = queryAAI(execution, AAIFluentTypeBuilder.Types.SERVICE_INSTANCE, nssiId, errorMsg)
            Optional<ServiceInstance> si =wrapper.asBean(ServiceInstance.class)
            if(si.isPresent()){
                nssiInstanceList.add(si.get())
            }
        }
        LOGGER.trace(" ***** Exit getNSSIListFromAAI *****")
        return nssiInstanceList
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
        LOGGER.trace(" ***** Start queryAAI *****")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("serviceType")

        org.onap.aaiclient.client.generated.fluentbuilders.ServiceInstance serviceInstanceType = AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(instanceId)
        def type
        if (aaiObjectName == AAIFluentTypeBuilder.Types.ALLOTTED_RESOURCE) {
            type = serviceInstanceType.allottedResources()
        } else if (aaiObjectName == AAIFluentTypeBuilder.Types.SLICE_PROFILES) {
            type = serviceInstanceType.sliceProfiles()
        } else {
            type = serviceInstanceType
        }
        def uri = AAIUriFactory.createResourceUri(type)
        if (!getAAIClient().exists(uri)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, errorMsg)
        }
        AAIResultWrapper wrapper = getAAIClient().get(uri, NotFoundException.class)
        LOGGER.trace(" ***** Exit queryAAI *****")
        return wrapper
    }

    AAIResourcesClient getAAIClient(){
        return  new AAIResourcesClient()
    }
}
