/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
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
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.ServiceProfile;
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class DoCreateSliceServiceInstance extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoCreateSliceServiceInstance.class)

    JsonUtils jsonUtil = new JsonUtils()

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    AAIResourcesClient client = getAAIClient()
    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    void preProcessRequest (DelegateExecution execution) {
        logger.trace("Enter preProcessRequest()")
        //here modelVersion is not set, we use modelUuid to decompose the service.
        def isDebugLogEnabled = true
        execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)

        logger.trace("Exit preProcessRequest")
    }

    /**
     * prepare decompose service profile instance template
     * @param execution
     */
    public void prepareDecomposeService(DelegateExecution execution) {

        String uuiRequest = execution.getVariable("uuiRequest")
        String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
        String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)
    }

    /**
     * create service-profile instance in aai
     * @param execution
     */
    void createServiceProfileInstance(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        ServiceDecomposition serviceDecomposition =
                execution.getVariable("serviceProfileDecomposition") as ServiceDecomposition
        ModelInfo modelInfo = serviceDecomposition.getModelInfo()
        //String serviceRole = "e2eslice-service"
        /**
         * todo: ServiceProfile params changed
         * todo: role
         */
        String serviceRole = "service-profile"
        String serviceType = execution.getVariable("serviceType")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

        Map<String, Object> serviceProfile = sliceParams.getServiceProfile()
        String ssInstanceId = execution.getVariable("serviceInstanceId")
        try {
            ServiceInstance ss = new ServiceInstance()
            ss.setServiceInstanceId(ssInstanceId)
            String sliceInstanceName = execution.getVariable("serviceInstanceName")
            ss.setServiceInstanceName(sliceInstanceName)
            ss.setServiceType(serviceType)
            String serviceStatus = "deactivated"
            ss.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = modelInfo.getModelInvariantUuid()
            String modelUuid = modelInfo.getModelUuid()
            ss.setModelInvariantId(modelInvariantUuid)
            ss.setModelVersionId(modelUuid)
            String serviceInstanceLocationid = serviceProfile.get("plmnIdList")
            ss.setServiceInstanceLocationId(serviceInstanceLocationid)
            String snssai = serviceProfile.get("sNSSAI")
            ss.setEnvironmentContext(snssai)
            ss.setServiceRole(serviceRole)

            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(globalSubscriberId)
                    .serviceSubscription(subscriptionServiceType)
                    .serviceInstance(ssInstanceId))
            client.create(uri, ss)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }


        RollbackData rollbackData = execution.getVariable("RollbackData")
        if (rollbackData == null) {
            rollbackData = new RollbackData()
        }
        //rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
        rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
        rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", ssInstanceId)
        rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", subscriptionServiceType)
        rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", globalSubscriberId)
        execution.setVariable("rollbackData", rollbackData)
        execution.setVariable("RollbackData", rollbackData)
        logger.debug("RollbackData:" + rollbackData)

    }

    /**
     * create service profile in aai
     * @param execution
     */
    void createServiceProfile(DelegateExecution execution) {

        /**
         * todo: ServiceProfile params changed
         */
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter
        Map<String, Object> serviceProfileMap = sliceParams.getServiceProfile()

        String serviceProfileInstanceId = execution.getVariable("serviceInstanceId")
        String serviceProfileId = UUID.randomUUID().toString()
        sliceParams.serviceProfile.put("profileId", serviceProfileId)

        ServiceProfile serviceProfile = new ServiceProfile()
        serviceProfile.setProfileId(serviceProfileId)
        serviceProfile.setLatency(Integer.parseInt(serviceProfileMap.get("latency").toString()))
        serviceProfile.setMaxNumberOfUEs(Integer.parseInt(serviceProfileMap.get("maxNumberofUEs").toString()))
        serviceProfile.setCoverageAreaTAList(serviceProfileMap.get("coverageAreaTAList").toString())
        serviceProfile.setUeMobilityLevel(serviceProfileMap.get("uEMobilityLevel").toString())
        serviceProfile.setResourceSharingLevel(serviceProfileMap.get("resourceSharingLevel").toString())
        serviceProfile.setDlThptPerSlice(Integer.parseInt(serviceProfileMap.get("dLThptPerSlice").toString()))
        serviceProfile.setDlThptPerUE(Integer.parseInt(serviceProfileMap.get("dLThptPerUE").toString()))
        serviceProfile.setUlThptPerSlice(Integer.parseInt(serviceProfileMap.get("uLThptPerSlice").toString()))
        serviceProfile.setUlThptPerUE(Integer.parseInt(serviceProfileMap.get("uLThptPerUE").toString()))
        serviceProfile.setActivityFactor(Integer.parseInt(serviceProfileMap.get("activityFactor").toString()))

        serviceProfile.setJitter(Integer.parseInt(serviceProfileMap.get("jitter").toString()))
        serviceProfile.setSurvivalTime("0")
        serviceProfile.setReliability("")
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                    .customer(globalSubscriberId)
                    .serviceSubscription(subscriptionServiceType)
                    .serviceInstance(serviceProfileInstanceId)
                    .serviceProfile(serviceProfileId))
            client.create(uri, serviceProfile)
            execution.setVariable("sliceTaskParams", sliceParams)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    /**
     * create allotted resource
     * todo: unfinished
     * @param execution
     */
    public void createAllottedResource(DelegateExecution execution) {

        try {
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
            ServiceDecomposition serviceDecomposition =
                    execution.getVariable("serviceProfileDecomposition") as ServiceDecomposition
            String serviceInstanceId = execution.getVariable("serviceInstanceId")

            List<org.onap.so.bpmn.core.domain.AllottedResource> allottedResourceList = serviceDecomposition.getAllottedResources()
            for(org.onap.so.bpmn.core.domain.AllottedResource allottedResource : allottedResourceList) {
                String allottedResourceId = UUID.randomUUID().toString()

                AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                        .customer(globalSubscriberId)
                        .serviceSubscription(subscriptionServiceType)
                        .serviceInstance(serviceInstanceId)
                        .allottedResource(allottedResourceId))

                execution.setVariable("allottedResourceUri", allottedResourceUri)
                String arType = allottedResource.getAllottedResourceType()
                String arRole = allottedResource.getAllottedResourceRole()
                String modelInvariantId = allottedResource.getModelInfo().getModelInvariantUuid()
                String modelVersionId = allottedResource.getModelInfo().getModelUuid()

                AllottedResource resource = new AllottedResource()
                resource.setId(allottedResourceId)
                resource.setType(arType)
                resource.setAllottedResourceName("Allotted_"+ execution.getVariable("serviceInstanceName"))
                resource.setRole(arRole)
                resource.setModelInvariantId(modelInvariantId)
                resource.setModelVersionId(modelVersionId)

                client.create(allottedResourceUri, resource)

                execution.setVariable("allottedResourceId", allottedResourceId)
            }

        }catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception in createAaiAR " + ex.getMessage())
        }
    }

}
