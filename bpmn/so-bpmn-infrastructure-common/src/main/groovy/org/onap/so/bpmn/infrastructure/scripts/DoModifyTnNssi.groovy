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
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aai.domain.yang.AllottedResources
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isEmpty
import static org.apache.commons.lang3.StringUtils.isNotBlank

public class DoModifyTnNssi extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DoModifyTnNssi.class)
    private static final ObjectMapper mapper = new ObjectMapper()
    String Prefix = "TNMOD_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()


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
            if (isBlank(additionalPropJsonStr)) {
                msg = "ERROR: additionalPropJsonStr is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String sliceServiceInstanceId = execution.getVariable("serviceInstanceID")
            if (isBlank(sliceServiceInstanceId)) {
                msg = "ERROR: sliceServiceInstanceId is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("sliceServiceInstanceId", sliceServiceInstanceId)

            String sliceServiceInstanceName = execution.getVariable("servicename")
            execution.setVariable("sliceServiceInstanceName", sliceServiceInstanceName)

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

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

            tnNssmfUtils.setExecVarFromJsonStr(execution, additionalPropJsonStr,
                    "sliceProfile", "sliceProfile", true)

            tnNssmfUtils.setExecVarFromJsonStr(execution, additionalPropJsonStr,
                    "transportSliceNetworks", "transportSliceNetworks", true)
            logger.debug("transportSliceNetworks: " + execution.getVariable("transportSliceNetworks"))

            tnNssmfUtils.setExecVarFromJsonStr(execution, additionalPropJsonStr,
                    "nsiInfo", "nsiInfo", true)

            if (isBlank(tnNssmfUtils.setExecVarFromJsonIfExists(execution, additionalPropJsonStr,
                    "enableSdnc", "enableSdnc"))) {
                tnNssmfUtils.setEnableSdncConfig(execution)
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


    void getExistingServiceInstance(DelegateExecution execution) {
        String serviceInstanceId = execution.getVariable("sliceServiceInstanceId")

        AAIResourcesClient resourceClient = getAAIClient()
        AAIResourceUri ssServiceuri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))

        try {
            Optional<ServiceInstance> ssOpt = resourceClient.get(ServiceInstance.class, ssServiceuri)
            if (ssOpt.isPresent()) {
                ServiceInstance ss = ssOpt.get()
                AllottedResources ars = tnNssmfUtils.getAllottedResourcesFromAai(execution, serviceInstanceId, true)
                if (ars != null) {
                    List<AllottedResource> arList = ars.getAllottedResource()
                    List<String> arIdList = new ArrayList<>()
                    Map<String, String> policyMap = new HashMap<>()
                    Map<String, List<String>> logicalLinksMap = new HashMap<>()
                    for (AllottedResource ar : arList) {
                        String arId = ar.getId()
                        arIdList.add(arId)
                        String policyId = tnNssmfUtils.getPolicyIdFromAr(execution, serviceInstanceId, arId, true)
                        policyMap.put(arId, policyId)
                        List<String> logicalLinkList = tnNssmfUtils.getLogicalLinkNamesFromAr(execution,
                                serviceInstanceId, arId, true)
                        logicalLinksMap.put(arId, logicalLinkList)
                    }
                    execution.setVariable("arIdList", arIdList)
                    execution.setVariable("arPolicyMap", policyMap)
                    execution.setVariable("arLogicalLinkMap", logicalLinksMap)
                } else {
                    logger.error("ERROR: getExistingServiceInstance: getAllottedResources() returned null. ss=" + ss
                            .toString())
                }
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai to " +
                        "associate allotted resource for service :" + serviceInstanceId)
            }
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Exception in getExistingServiceInstance. " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void updateTnNssiInAAI(DelegateExecution execution) {
        getExistingServiceInstance(execution)
        updateTsciNetworks(execution)
    }

    void updateServiceInstance(DelegateExecution execution) {
        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
        try {
            ServiceInstance ss = new ServiceInstance()
            //ss.setServiceInstanceId(ssInstanceId)
            String serviceStatus = "modified"
            ss.setOrchestrationStatus(serviceStatus)
            ss.setEnvironmentContext("tn")
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(
                    AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(ssInstanceId))
            client.update(uri, ss)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void updateSliceProfile(DelegateExecution execution) {

        String sliceserviceInstanceId = execution.getVariable("sliceServiceInstanceId")
        String sliceProfileStr = execution.getVariable("sliceProfile")
        String sliceProfileId = execution.getVariable("sliceProfileId")
        SliceProfile sliceProfile = new SliceProfile();
        sliceProfile.setProfileId(sliceProfileId)
        sliceProfile.setLatency(Integer.parseInt(jsonUtil.getJsonValue(sliceProfileStr, "latency")))
        sliceProfile.setResourceSharingLevel(jsonUtil.getJsonValue(sliceProfileStr, "resourceSharingLevel"))
        sliceProfile.setSNssai(tnNssmfUtils.getFirstSnssaiFromSliceProfile(sliceProfileStr))    //TODO: should be list

        sliceProfile.setE2ELatency(Integer.parseInt(jsonUtil.getJsonValue(sliceProfileStr, "latency")))
        sliceProfile.setMaxBandwidth(Integer.parseInt(jsonUtil.getJsonValue(sliceProfileStr, "maxBandwidth")))

        //TODO: new API
        sliceProfile.setReliability(new Object())
        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(sliceserviceInstanceId).sliceProfile(sliceProfileId))
            client.update(uri, sliceProfile)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in updateSliceProfile. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    String getValidArId(DelegateExecution execution, String arIdStr) {
        List<String> arIdList = execution.getVariable("arIdList")
        /*
         * If arId is not specified by the caller, then we assume the caller
         * wants to modify the first network (i.e., allotted resource) in the TSCi tree.
         */
        String arId = isBlank(arIdStr) ? arIdList.get(0) : arIdStr

        return arId
    }

    void updateLogicalLinksInAr(DelegateExecution execution, String arId, String linkArrayJsonStr) {
        try {
            String serviceInstanceId = execution.getVariable('sliceServiceInstanceId')

            /*
             * Each TSCi connection-link in linkArrayJsonStr is considered as an "ADD" new
             * link to allotted-resource. So, if the link already exists under AR, then do
             * nothing. Otherwise, create logical-link.
             */
            List<String> linkStrList = jsonUtil.StringArrayToList(linkArrayJsonStr)
            for (String linkStr : linkStrList) {
                if (logicalLinkExists(execution, arId, linkStr)) {
                    continue
                }

                createLogicalLinkForAllocatedResource(execution, linkStr, serviceInstanceId, arId)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,
                    "Exception in updateLogicalLinksInAr" + ex.getMessage())
        }
    }

    void updateLogicalLinksInNetwork(DelegateExecution execution, String networkJsonStr) {
        try {
            String arId = getValidArId(execution, jsonUtil.getJsonValue(networkJsonStr, "id"))
            String linkArrayStr = jsonUtil.getJsonValue(networkJsonStr, "connectionLinks")
            updateLogicalLinksInAr(execution, arId, linkArrayStr)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = String.format("ERROR: updateLogicalLinksInNetwork: exception: %s", ex.getMessage())
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }
    }

    void updateTsciNetworks(DelegateExecution execution) {
        try {
            List<String> networkStrList = jsonUtil.StringArrayToList(execution.getVariable("transportSliceNetworks"))
            for (String networkStr : networkStrList) {
                updateLogicalLinksInNetwork(execution, networkStr)
                updateNetworkPolicy(execution, networkStr)
            }

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,
                    "Exception in updateTsciNetworks" + ex.getMessage())
        }
    }

    int getMaxBw(DelegateExecution execution) {
        int maxBw = 0
        try {
            String sliceProfileStr = execution.getVariable("sliceProfile")
            if (isBlank(sliceProfileStr)) {
                String msg = "ERROR: getMaxBw: sliceProfile is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
            String bwStr = jsonUtil.getJsonValue(sliceProfileStr, "maxBandwidth")
            if (isNotBlank(bwStr)) {
                maxBw = Integer.parseInt(bwStr)
            } else {
                logger.error("ERROR: getMaxBw: maxBandwidth is null")
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,
                    "Exception in getMaxBw" + ex.getMessage())
        }

        return maxBw
    }

    void updatePolicyMaxBandwidthInAAI(DelegateExecution execution, String policyId, int maxBw) {
        try {
            NetworkPolicy networkPolicy = new NetworkPolicy()
            networkPolicy.setMaxBandwidth(maxBw)
            AAIResourceUri networkPolicyUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(policyId))
            getAAIClient().update(networkPolicyUri, networkPolicy)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void updateNetworkPolicy(DelegateExecution execution, String networkJsonStr) {
        try {
            int maxBw = getMaxBw(execution)

            String arId = getValidArId(execution, jsonUtil.getJsonValue(networkJsonStr, "id"))
            Map<String, String> policyMap = execution.getVariable("arPolicyMap")
            String policyId = policyMap.get(arId)
            if (isBlank(policyId)) {
                String msg = String.format("ERROR: updateNetworkPolicy: policyId not found. arId=%s", arId)
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }

            updatePolicyMaxBandwidthInAAI(execution, policyId, maxBw)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = String.format("ERROR: updateNetworkPolicy: exception: %s", ex.getMessage())
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
        }
    }


    void createLogicalLinkForAllocatedResource(DelegateExecution execution,
                                               String linkJsonStr, String ssInstanceId,
                                               String allottedResourceId) {
        try {
            AAIResourceUri allottedResourceUri = tnNssmfUtils.buildAllottedResourceUri(execution,
                    ssInstanceId, allottedResourceId)

            if (!getAAIClient().exists(allottedResourceUri)) {
                logger.info("ERROR: createLogicalLinksForAllocatedResource: allottedResource not exist: uri={}",
                        allottedResourceUri)
                return
            }

            String linkId = jsonUtil.getJsonValue(linkJsonStr, "id")
            if (isBlank(linkId)) {
                linkId = "tn-nssmf-" + UUID.randomUUID().toString()
            }
            logger.debug("createLogicalLinkForAllocatedResource: linkId=" + linkId)

            String epA = jsonUtil.getJsonValue(linkJsonStr, "transportEndpointA")
            String epB = jsonUtil.getJsonValue(linkJsonStr, "transportEndpointB")
            String modelInvariantId = execution.getVariable("modelInvariantUuid")
            String modelVersionId = execution.getVariable("modelUuid")

            org.onap.aai.domain.yang.LogicalLink resource = new org.onap.aai.domain.yang.LogicalLink()
            resource.setLinkId(linkId)
            resource.setLinkName(epA)
            resource.setLinkName2(epB)
            resource.setLinkType("TsciConnectionLink")
            resource.setInMaint(false)

            //epA is link-name
            AAIResourceUri logicalLinkUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().logicalLink(epA))
            getAAIClient().create(logicalLinkUri, resource)

            tnNssmfUtils.attachLogicalLinkToAllottedResource(execution, tnNssmfUtils.AAI_VERSION,
                    allottedResourceUri, epA);
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createLogicalLinksForAllocatedResource: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    void preprocessSdncModifyTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncModifyTnNssiRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        try {
            String serviceInstanceId = execution.getVariable("sliceServiceInstanceId")

            String sdncRequest = tnNssmfUtils.buildSDNCRequest(execution, serviceInstanceId, "update")

            execution.setVariable("TNNSSMF_SDNCRequest", sdncRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + sdncRequest)

        } catch (BpmnError e) {
            throw e
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
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(sliceServiceInstanceId))
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
        String operType = "MODIFY"

        ResourceOperationStatus roStatus = tnNssmfUtils.buildRoStatus(modelUuid, ssInstanceId,
                jobId, nsiId, operType, status, progress, statusDescription)

        logger.debug("prepareUpdateJobStatus: roStatus={}", roStatus)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, roStatus)
    }

    boolean logicalLinkExists(DelegateExecution execution, String arIdStr, String linkJsonStr) {
        if (isBlank(arIdStr)) {
            logger.error("ERROR: logicalLinkExists: arIdStr is empty")
            return false
        }
        if (isBlank(linkJsonStr)) {
            logger.error("ERROR: logicalLinkExists: linkJsonStr is empty")
            return false
        }

        Map<String, List<String>> logicalLinksMap = execution.getVariable("arLogicalLinkMap")
        if (logicalLinksMap == null) {
            logger.error("ERROR: logicalLinkExists: logicalLinksMap is null")
            return false
        }

        List<String> logicalLinkNameList = logicalLinksMap.get(arIdStr)
        if (logicalLinksMap == null) {
            logger.error("ERROR: logicalLinkExists: logicalLinkNameList is null. arIdStr=" + arIdStr)
            return false
        }

        String linkName = jsonUtil.getJsonValue(linkJsonStr, "transportEndpointA")
        if (isBlank(linkName)) {
            logger.error("ERROR: logicalLinkExists: epA is empty")
            return false
        }

        return logicalLinkNameList.contains(linkName)
    }
}
