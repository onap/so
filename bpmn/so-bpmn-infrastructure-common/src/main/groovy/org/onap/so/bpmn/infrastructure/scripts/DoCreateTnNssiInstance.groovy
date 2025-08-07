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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isNotBlank

class DoCreateTnNssiInstance extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DoCreateTnNssiInstance.class);
    JsonUtils jsonUtil = new JsonUtils()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    String Prefix = "DCTN_"

    void preProcessRequest(DelegateExecution execution) {
        logger.trace("Enter preProcessRequest()")

        execution.setVariable("prefix", Prefix)

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

        if (isBlank(execution.getVariable("enableSdnc"))) {
            tnNssmfUtils.setEnableSdncConfig(execution)
        }

        logger.trace("Exit preProcessRequest")
    }


    void createSliceProfile(DelegateExecution execution) {

        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
        String sliceProfileStr = execution.getVariable("sliceProfile")
        String sliceProfileId = UUID.randomUUID().toString()
        SliceProfile sliceProfile = new SliceProfile();
        sliceProfile.setProfileId(sliceProfileId)
        sliceProfile.setLatency(Integer.parseInt(jsonUtil.getJsonValue(sliceProfileStr, "latency")))
        sliceProfile.setResourceSharingLevel(jsonUtil.getJsonValue(sliceProfileStr, "resourceSharingLevel"))
        //sliceProfile.setSNssai(tnNssmfUtils.getFirstSnssaiFromSliceProfile(sliceProfileStr))

        sliceProfile.setMaxBandwidth(Integer.parseInt(jsonUtil.getJsonValue(sliceProfileStr, "maxBandwidth")))

        //sliceProfile.setReliability(new Object())
        try {
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(ssInstanceId)
                            .sliceProfile(sliceProfileId))
            client.create(uri, sliceProfile)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    void createServiceInstance(DelegateExecution execution) {

        String serviceRole = "nssi"
        String serviceType = execution.getVariable("subscriptionServiceType")
        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
        String sliceProfileStr = execution.getVariable("sliceProfile")
        String sst = execution.getVariable("sst")
        try {
            if (sliceProfileStr == null || sliceProfileStr.isEmpty()) {
                String msg = "ERROR: createServiceInstance: sliceProfile is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }

            org.onap.aai.domain.yang.ServiceInstance ss = new org.onap.aai.domain.yang.ServiceInstance()
            ss.setServiceInstanceId(ssInstanceId)
            String sliceInstanceName = execution.getVariable("sliceServiceInstanceName")
            if (isBlank(sliceInstanceName)) {
                logger.error("ERROR: createServiceInstance: sliceInstanceName is null")
                sliceInstanceName = ssInstanceId
            }
            ss.setServiceInstanceName(sliceInstanceName)
            ss.setServiceType(sst)
            String serviceStatus = "deactivated"
            ss.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            ss.setModelInvariantId(modelInvariantUuid)
            ss.setModelVersionId(modelUuid)
            String serviceInstanceLocationId = tnNssmfUtils.getFirstPlmnIdFromSliceProfile(sliceProfileStr)
            ss.setServiceInstanceLocationId(serviceInstanceLocationId)
            String snssai = tnNssmfUtils.getFirstSnssaiFromSliceProfile(sliceProfileStr)
            //ss.setEnvironmentContext(snssai)
            ss.setEnvironmentContext("tn")
            ss.setServiceRole(serviceRole)

            String domainTypeStr = jsonUtil.getJsonValue(sliceProfileStr, "domainType")
            if (isNotBlank(domainTypeStr)) {
                ss.setWorkloadContext(domainTypeStr)
            }

            String resourceSharingLevel = jsonUtil.getJsonValue(sliceProfileStr, "resourceSharingLevel")
            if (isNotBlank(resourceSharingLevel)) {
                ss.setServiceFunction(resourceSharingLevel)
            }

            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(ssInstanceId))
            client.create(uri, ss)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createServiceInstance: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    void createAllottedResource(DelegateExecution execution) {
        String ssInstanceId = execution.getVariable('sliceServiceInstanceId')

        try {
            List<String> networkStrList = jsonUtil.StringArrayToList(execution.getVariable("transportSliceNetworks"))

            for (String networkStr : networkStrList) {
                String allottedResourceId = UUID.randomUUID().toString()
                AAIResourceUri allottedResourceUri =
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                                .customer(execution.getVariable("globalSubscriberId"))
                                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                                .serviceInstance(execution.getVariable("sliceServiceInstanceId"))
                                .allottedResource(allottedResourceId))
                execution.setVariable("allottedResourceUri", allottedResourceUri)
                String modelInvariantId = execution.getVariable("modelInvariantUuid")
                String modelVersionId = execution.getVariable("modelUuid")

                org.onap.aai.domain.yang.AllottedResource resource = new org.onap.aai.domain.yang.AllottedResource()
                resource.setId(allottedResourceId)
                resource.setType("TsciNetwork")
                resource.setAllottedResourceName("network_" + execution.getVariable("sliceServiceInstanceName"))
                getAAIClient().create(allottedResourceUri, resource)

                createNetworkPolicyForAllocatedResource(execution, ssInstanceId, allottedResourceId)

                String linkArrayStr = jsonUtil.getJsonValue(networkStr, "connectionLinks")
                createLogicalLinksForAllocatedResource(execution, linkArrayStr, ssInstanceId, allottedResourceId)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createAllottedResource: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void createNetworkPolicy(DelegateExecution execution, String ssInstanceId, String networkPolicyId) {
        try {

            String sliceProfileStr = execution.getVariable("sliceProfile")
            if (sliceProfileStr == null || sliceProfileStr.isEmpty()) {
                String msg = "ERROR: createNetworkPolicy: sliceProfile is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }

            NetworkPolicy networkPolicy = new NetworkPolicy();
            networkPolicy.setNetworkPolicyId(networkPolicyId)
            networkPolicy.setName("TSCi policy")
            networkPolicy.setType("SLA")
            networkPolicy.setNetworkPolicyFqdn(ssInstanceId)

            String latencyStr = jsonUtil.getJsonValue(sliceProfileStr, "latency")
            if (latencyStr != null && !latencyStr.isEmpty()) {
                networkPolicy.setLatency(Integer.parseInt(latencyStr))
            }

            String bwStr = jsonUtil.getJsonValue(sliceProfileStr, "maxBandwidth")
            if (bwStr != null && !bwStr.isEmpty()) {
                networkPolicy.setMaxBandwidth(Integer.parseInt(bwStr))
            } else {
                logger.debug("ERROR: createNetworkPolicy: maxBandwidth is null")
            }

            //networkPolicy.setReliability(new Object())

            AAIResourceUri networkPolicyUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(networkPolicyId))
            getAAIClient().create(networkPolicyUri, networkPolicy)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void createNetworkPolicyForAllocatedResource(DelegateExecution execution,
                                                 String ssInstanceId,
                                                 String allottedResourceId) {
        try {
            AAIResourceUri allottedResourceUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(ssInstanceId)
                            .allottedResource(allottedResourceId))

            if (!getAAIClient().exists(allottedResourceUri)) {
                logger.info("ERROR: createLogicalLinksForAllocatedResource: allottedResource not exist: uri={}",
                        allottedResourceUri)
                return
            }

            String networkPolicyId = UUID.randomUUID().toString()
            createNetworkPolicy(execution, ssInstanceId, networkPolicyId)

            tnNssmfUtils.attachNetworkPolicyToAllottedResource(execution, tnNssmfUtils.AAI_VERSION,
                    allottedResourceUri,
                    networkPolicyId);

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void createLogicalLinksForAllocatedResource(DelegateExecution execution,
                                                String linkArrayStr, String ssInstanceId,
                                                String allottedResourceId) {
        try {
            AAIResourceUri allottedResourceUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(ssInstanceId)
                            .allottedResource(allottedResourceId))

            if (!getAAIClient().exists(allottedResourceUri)) {
                logger.info("ERROR: createLogicalLinksForAllocatedResource: allottedResource not exist: uri={}",
                        allottedResourceUri)
                return
            }

            List<String> linkStrList = jsonUtil.StringArrayToList(linkArrayStr)

            for (String linkStr : linkStrList) {
                String linkId = jsonUtil.getJsonValue(linkStr, "name")
                if (isBlank(linkId)) {
                    linkId = "tn-nssmf-" + UUID.randomUUID().toString()
                }
                logger.debug("createLogicalLinksForAllocatedResource: linkId=" + linkId)

                String epA = jsonUtil.getJsonValue(linkStr, "transportEndpointA")
                String epB = jsonUtil.getJsonValue(linkStr, "transportEndpointB")
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
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createLogicalLinksForAllocatedResource: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void preprocessSdncAllocateTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
                'execution=' + execution.getId() +
                ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        logger.trace("STARTED preProcessSDNCActivateRequest Process")
        try {
            String serviceInstanceId = execution.getVariable("sliceServiceInstanceId")

            String createSDNCRequest = tnNssmfUtils.buildSDNCRequest(execution, serviceInstanceId, "create")

            execution.setVariable("TNNSSMF_SDNCRequest", createSDNCRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + createSDNCRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002,
                    "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
    }


    void validateSDNCResponse(DelegateExecution execution, String response, String method) {
        tnNssmfUtils.validateSDNCResponse(execution, response, method)
    }
}
