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
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank

public class DoModifyTnNssi extends AbstractServiceTaskProcessor {
    String Prefix = "TNMOD_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()
    ObjectMapper objectMapper = new ObjectMapper()
    private static final Logger logger = LoggerFactory.getLogger(DoModifyTnNssi.class)


    void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            execution.setVariable("startTime", System.currentTimeMillis())
            msg = tnNssmfUtils.getExecutionInputParams(execution)
            logger.debug("Modify TN NSSI input parameters: " + msg)

            execution.setVariable("prefix", Prefix)

            tnNssmfUtils.setSdncCallbackUrl(execution, true)
            logger.debug("SDNC Callback URL: " + execution.getVariable("sdncCallbackUrl"))

            String additionalPropJsonStr = execution.getVariable("sliceParams")

            String sliceServiceInstanceId = execution.getVariable("serviceInstanceID")
            execution.setVariable("sliceServiceInstanceId", sliceServiceInstanceId)

            String sliceServiceInstanceName = execution.getVariable("servicename")
            execution.setVariable("sliceServiceInstanceName", sliceServiceInstanceName)

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            //here modelVersion is not set, we use modelUuid to decompose the service.
            def isDebugLogEnabled = true
            execution.setVariable("isDebugLogEnabled", isDebugLogEnabled)
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            //additional properties
            String sliceProfile = jsonUtil.getJsonValue(additionalPropJsonStr, "sliceProfile")
            if (isBlank(sliceProfile)) {
                msg = "Input sliceProfile is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("sliceProfile", sliceProfile)
            }

            String transportSliceNetworks = jsonUtil.getJsonValue(additionalPropJsonStr, "transportSliceNetworks")
            if (isBlank(transportSliceNetworks)) {
                msg = "Input transportSliceNetworks is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("transportSliceNetworks", transportSliceNetworks)
            }
            logger.debug("transportSliceNetworks: " + transportSliceNetworks)

            String nsiInfo = jsonUtil.getJsonValue(additionalPropJsonStr, "nsiInfo")
            if (isBlank(nsiInfo)) {
                msg = "Input nsiInfo is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("nsiInfo", nsiInfo)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("Finish preProcessRequest")
    }


    void deleteServiceInstance(DelegateExecution execution) {
        try {
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    execution.getVariable("globalSubscriberId"),
                    execution.getVariable("subscriptionServiceType"),
                    execution.getVariable("serviceInstanceID"))
            client.delete(uri)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoDeallocateTnNssi.deleteServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    void getExistingServiceInstance(DelegateExecution execution) {
        String serviceInstanceId = execution.getVariable("serviceInstanceID")

        AAIResourcesClient resourceClient = new AAIResourcesClient()
        AAIResourceUri ssServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

        try {
            if (resourceClient.exists(ssServiceuri)) {
                execution.setVariable("ssi_resourceLink", ssServiceuri.build().toString())
                org.onap.aai.domain.yang.ServiceInstance ss =
                        resourceClient.get(org.onap.aai.domain.yang.ServiceInstance.class, ssServiceuri)
                org.onap.aai.domain.yang.SliceProfile sliceProfile = ss.getSliceProfiles().getSliceProfile().get(0)
                execution.setVariable("sliceProfileId", sliceProfile.getProfileId())

                org.onap.aai.domain.yang.AllottedResources ars = ss.getAllottedResources()
                List<org.onap.aai.domain.yang.AllottedResource> arList = ars.getAllottedResource()
                List<String> arIdList = new ArrayList<>()
                for (org.onap.aai.domain.yang.AllottedResource ar : arList) {
                    String arId = ar.getId()
                    arIdList.add(arId)
                }
                execution.setVariable("arIdList", arIdList)
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai to " +
                        "associate allotted resource for service :" + serviceInstanceId)
            }
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in getServiceInstance. " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

    }

    public void updateTnNssiInAAI(DelegateExecution execution) {
        getExistingServiceInstance(execution)

        updateServiceInstance(execution)
        updateSliceProfile(execution)
        updateAllottedResource(execution)
    }

    void updateServiceInstance(DelegateExecution execution) {
        String serviceRole = "TN"
        String serviceType = execution.getVariable("subscriptionServiceType")
        Map<String, Object> sliceProfile = execution.getVariable("sliceProfile")
        String ssInstanceId = execution.getVariable("serviceInstanceID")
        try {
            org.onap.aai.domain.yang.ServiceInstance ss = new org.onap.aai.domain.yang.ServiceInstance()
            ss.setServiceInstanceId(ssInstanceId)
            String sliceInstanceName = execution.getVariable("sliceServiceInstanceName")
            ss.setServiceInstanceName(sliceInstanceName)
            ss.setServiceType(serviceType)
            String serviceStatus = "modified"
            ss.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            ss.setModelInvariantId(modelInvariantUuid)
            ss.setModelVersionId(modelUuid)
            String serviceInstanceLocationid = sliceProfile.get("plmnIdList")
            ss.setServiceInstanceLocationId(serviceInstanceLocationid)
            String snssai = sliceProfile.get("snssaiList")
            ss.setEnvironmentContext(snssai)
            ss.setServiceRole(serviceRole)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), ssInstanceId)
            client.update(uri, ss)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }


        def rollbackData = execution.getVariable("RollbackData")
        if (rollbackData == null) {
            rollbackData = new RollbackData();
        }
        //rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
        rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
        rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", ssInstanceId)
        rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
        rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
        execution.setVariable("rollbackData", rollbackData)
        execution.setVariable("RollbackData", rollbackData)
        logger.debug("RollbackData:" + rollbackData)
    }

    void updateSliceProfile(DelegateExecution execution) {

        String sliceserviceInstanceId = execution.getVariable("serviceInstanceID")
        Map<String, Object> sliceProfileMap = execution.getVariable("sliceProfile")
        String sliceProfileId = execution.getVariable("sliceProfileId")
        SliceProfile sliceProfile = new SliceProfile();
        sliceProfile.setProfileId(sliceProfileId)
        sliceProfile.setLatency(Integer.parseInt(sliceProfileMap.get("latency").toString()))
        sliceProfile.setResourceSharingLevel(sliceProfileMap.get("resourceSharingLevel").toString())
        sliceProfile.setSNssai(sliceProfileMap.get("snssaiList"))    //TODO: should be list

        sliceProfile.setE2ELatency(Integer.parseInt(sliceProfileMap.get("latency")))
        sliceProfile.setMaxBandwidth(Integer.parseInt(sliceProfileMap.get("maxBandwidth")))    //TODO: new API
        sliceProfile.setReliability(new Object())
        try {
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, execution.getVariable
                    ("globalSubscriberId"),
                    execution.getVariable("subscriptionServiceType"), sliceserviceInstanceId, sliceProfileId)
            client.update(uri, sliceProfile)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in updateSliceProfile. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void updateAllottedResource(DelegateExecution execution) {
        String serviceInstanceId = execution.getVariable('serviceInstanceID')

        List<String> arIdList = execution.getVariable("arIdList")
        try {
            for (String arId : arIdList) {
                AAIResourceUri arUri = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE,
                        execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"),
                        serviceInstanceId, arId)

                getAAIClient().delete(arUri)
            }

            List<String> networkStrList = jsonUtil.StringArrayToList(execution.getVariable("transportSliceNetworks"))

            for (String networkStr : networkStrList) {
                String allottedResourceId = UUID.randomUUID().toString()
                AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE,
                        execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"),
                        execution.getVariable("sliceserviceInstanceId"), allottedResourceId)
                execution.setVariable("allottedResourceUri", allottedResourceUri)
                String modelInvariantId = execution.getVariable("modelInvariantUuid")
                String modelVersionId = execution.getVariable("modelUuid")

                org.onap.aai.domain.yang.AllottedResource resource = new org.onap.aai.domain.yang.AllottedResource()
                resource.setId(allottedResourceId)
                resource.setType("TsciNetwork")
                resource.setAllottedResourceName("network_" + execution.getVariable("sliceServiceInstanceName"))
                resource.setModelInvariantId(modelInvariantId)
                resource.setModelVersionId(modelVersionId)
                getAAIClient().create(allottedResourceUri, resource)

                String linkArrayStr = jsonUtil.getJsonValue(networkStr, "connectionLinks")
                createLogicalLinksForAllocatedResource(execution, linkArrayStr, serviceInstanceId, allottedResourceId)
            }

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception in createAaiAR " + ex.getMessage())
        }
    }

    void createLogicalLinksForAllocatedResource(DelegateExecution execution,
                                                String linkArrayStr, String serviceInstanceId,
                                                String allottedResourceId) {

        try {
            List<String> linkStrList = jsonUtil.StringArrayToList(linkArrayStr)

            for (String linkStr : linkStrList) {
                String logicalLinkId = UUID.randomUUID().toString()
                String epA = jsonUtil.getJsonValue(linkStr, "transportEndpointA")
                String epB = jsonUtil.getJsonValue(linkStr, "transportEndpointB")
                String modelInvariantId = execution.getVariable("modelInvariantUuid")
                String modelVersionId = execution.getVariable("modelUuid")

                org.onap.aai.domain.yang.LogicalLink resource = new org.onap.aai.domain.yang.LogicalLink()
                resource.setLinkId(logicalLinkId)
                resource.setLinkName(epA)
                resource.setLinkName2(epB)
                resource.setModelInvariantId(modelInvariantId)
                resource.setModelVersionId(modelVersionId)

                AAIResourceUri logicalLinkUri = AAIUriFactory.createResourceUri(AAIObjectType.LOGICAL_LINK, logicalLinkId)
                getAAIClient().create(logicalLinkUri, resource)
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,
                    "Exception in createLogicalLinksForAllocatedResource" + ex.getMessage())
        }
    }


    void preprocessSdncModifyTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncModifyTnNssiRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceID")

            String sdncRequest = tnNssmfUtils.buildSDNCRequest(execution, serviceInstanceId, "modify")

            execution.setVariable("TNNSSMF_SDNCRequest", sdncRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + sdncRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preprocessSdncModifyTnNssiRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preprocessSdncModifyTnNssiRequest Process")
    }


    void validateSDNCResponse(DelegateExecution execution, String response, String method) {
        tnNssmfUtils.validateSDNCResponse(execution, response, method)
    }


    void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String sliceServiceInstanceId = execution.getVariable("sliceServiceInstanceId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, sliceServiceInstanceId)
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
        String serviceId = execution.getVariable("serviceInstanceID")
        String jobId = execution.getVariable("jobId")
        String nsiId = execution.getVariable("nsiId")

        ResourceOperationStatus roStatus = new ResourceOperationStatus()
        roStatus.setServiceId(serviceId)
        roStatus.setOperationId(jobId)
        roStatus.setResourceTemplateUUID(nsiId)
        roStatus.setOperType("Modify")
        roStatus.setProgress(progress)
        roStatus.setStatus(status)
        roStatus.setStatusDescription(statusDescription)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, status)
    }

}

