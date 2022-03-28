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
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isNotBlank

class DoCloudLeasedLineCreate extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DoCloudLeasedLineCreate.class);
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    ServiceIntentUtils serviceIntentUtils = new ServiceIntentUtils()
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    String Prefix = "CLLC_"


    void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            execution.setVariable("startTime", System.currentTimeMillis())

            msg = serviceIntentUtils.getExecutionInputParams(execution)
            logger.debug("Create CLL input parameters: " + msg)

            serviceIntentUtils.setSdncCallbackUrl(execution, true)
            logger.debug("SDNC Callback URL: " + execution.getVariable("sdncCallbackUrl"))

            String additionalPropJsonStr = execution.getVariable("serviceIntentParams")

            String cllId = jsonUtil.getJsonValue(additionalPropJsonStr, "serviceInstanceID") //for debug
            if (isBlank(cllId)) {
                cllId = UUID.randomUUID().toString()
            }

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

            logger.debug("Generate new CLL ID:" + cllId)
            cllId = UriUtils.encode(cllId, "UTF-8")
            execution.setVariable("cllId", cllId)

            String cllName = execution.getVariable("servicename")
            execution.setVariable("cllName", cllName)

            String sst = execution.getVariable("sst")
            execution.setVariable("sst", sst)

            String transportNetworks = jsonUtil.getJsonValue(additionalPropJsonStr, "transportNetworks")
            if (isBlank(transportNetworks)) {
                msg = "ERROR: preProcessRequest: Input transportNetworks is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("transportNetworks", transportNetworks)
            }
            logger.debug("transportNetworks: " + transportNetworks)

            if (isBlank(serviceIntentUtils.setExecVarFromJsonIfExists(execution, additionalPropJsonStr,
                    "enableSdnc", "enableSdnc"))) {
                serviceIntentUtils.setEnableSdncConfig(execution)
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

    void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String cllId = execution.getVariable("cllId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.Types.SERVICE_INSTANCE.getFragment(cllId))
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
        String cllId = execution.getVariable("cllId")
        String modelUuid = execution.getVariable("modelUuid")
        String jobId = execution.getVariable("jobId")
        String nsiId = cllId
        String operType = "CREATE"

        ResourceOperationStatus roStatus = serviceIntentUtils.buildRoStatus(modelUuid, cllId,
                jobId, nsiId, operType, status, progress, statusDescription)

        logger.debug("prepareUpdateJobStatus: roStatus={}", roStatus)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, roStatus)
    }


    void createServiceInstance(DelegateExecution execution) {

        String serviceRole = "cll"
        String serviceType = execution.getVariable("serviceType")
        String cllId = execution.getVariable("cllId")
        try {
            org.onap.aai.domain.yang.ServiceInstance ss = new org.onap.aai.domain.yang.ServiceInstance()
            ss.setServiceInstanceId(cllId)
            String cllName = execution.getVariable("cllName")
            if (isBlank(cllName)) {
                logger.error("ERROR: createServiceInstance: cllName is null")
                cllName = cllId
            }
            ss.setServiceInstanceName(cllName)
            ss.setServiceType(serviceType)
            String serviceStatus = "created"
            ss.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            ss.setModelInvariantId(modelInvariantUuid)
            ss.setModelVersionId(modelUuid)
            ss.setEnvironmentContext("cll")
            ss.setServiceRole(serviceRole)

            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(cllId))
            client.create(uri, ss)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCloudLeasedLineCreate.createServiceInstance: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    void createAllottedResource(DelegateExecution execution) {
        String cllId = execution.getVariable('cllId')

        try {
            List<String> networkStrList = jsonUtil.StringArrayToList(execution.getVariable("transportNetworks"))

            for (String networkStr : networkStrList) {
                String networkId = jsonUtil.getJsonValue(networkStr, "id")
                String allottedResourceId = isBlank(networkId) ? UUID.randomUUID().toString() : networkId

                AAIResourceUri allottedResourceUri =
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                                .customer(execution.getVariable("globalSubscriberId"))
                                .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                                .serviceInstance(execution.getVariable("cllId"))
                                .allottedResource(allottedResourceId))
                execution.setVariable("allottedResourceUri", allottedResourceUri)
                String modelInvariantId = execution.getVariable("modelInvariantUuid")
                String modelVersionId = execution.getVariable("modelUuid")

                String slaStr = jsonUtil.getJsonValue(networkStr, "sla")
                if (slaStr == null || slaStr.isEmpty()) {
                    String msg = "ERROR: createNetworkPolicy: SLA is null"
                    logger.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
                }

                org.onap.aai.domain.yang.AllottedResource resource = new org.onap.aai.domain.yang.AllottedResource()
                resource.setId(allottedResourceId)
                resource.setType("TsciNetwork")
                resource.setAllottedResourceName("network_" + allottedResourceId)
                getAAIClient().create(allottedResourceUri, resource)

                createNetworkPolicyForAllocatedResource(execution, cllId, allottedResourceId, slaStr)

                String linkArrayStr = jsonUtil.getJsonValue(networkStr, "connectionLinks")
                createLogicalLinksForAllocatedResource(execution, linkArrayStr, cllId, allottedResourceId)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCloudLeasedLineCreate.createAllottedResource: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void createNetworkPolicy(DelegateExecution execution, String cllId, String networkPolicyId, String slaStr) {
        try {


            NetworkPolicy networkPolicy = new NetworkPolicy();
            networkPolicy.setNetworkPolicyId(networkPolicyId)
            networkPolicy.setName("TSCi policy")
            networkPolicy.setType("SLA")
            networkPolicy.setNetworkPolicyFqdn(cllId)

            String latencyStr = jsonUtil.getJsonValue(slaStr, "latency")
            if (latencyStr != null && !latencyStr.isEmpty()) {
                networkPolicy.setLatency(Integer.parseInt(latencyStr))
            }

            String bwStr = jsonUtil.getJsonValue(slaStr, "maxBandwidth")
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
                                                 String cllId,
                                                 String allottedResourceId, String slaStr) {
        try {
            AAIResourceUri allottedResourceUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(cllId)
                            .allottedResource(allottedResourceId))

            if (!getAAIClient().exists(allottedResourceUri)) {
                logger.info("ERROR: createLogicalLinksForAllocatedResource: allottedResource not exist: uri={}",
                        allottedResourceUri)
                return
            }

            String networkPolicyId = UUID.randomUUID().toString()
            createNetworkPolicy(execution, cllId, networkPolicyId, slaStr)

            serviceIntentUtils.attachNetworkPolicyToAllottedResource(execution, serviceIntentUtils.AAI_VERSION,
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
                                                String linkArrayStr, String cllId,
                                                String allottedResourceId) {
        try {
            AAIResourceUri allottedResourceUri =
                    AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                            .customer(execution.getVariable("globalSubscriberId"))
                            .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                            .serviceInstance(cllId)
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
                    linkId = "cll-" + UUID.randomUUID().toString()
                }
                logger.debug("createLogicalLinksForAllocatedResource: linkId=" + linkId)

                String epA = jsonUtil.getJsonValue(linkStr, "transportEndpointA")
                String epB = jsonUtil.getJsonValue(linkStr, "transportEndpointB")
                String epBProtect = jsonUtil.getJsonValue(linkStr, "transportEndpointBProtection")
                String modelInvariantId = execution.getVariable("modelInvariantUuid")
                String modelVersionId = execution.getVariable("modelUuid")

                org.onap.aai.domain.yang.LogicalLink resource = new org.onap.aai.domain.yang.LogicalLink()
                resource.setLinkId(linkId)
                resource.setLinkName(epA)
                resource.setLinkName2(epB)
                if (isNotBlank(epBProtect)) {
                    resource.setSegmentId(epBProtect)
                }
                resource.setLinkType("TsciConnectionLink")
                resource.setInMaint(false)

                //epA is link-name
                AAIResourceUri logicalLinkUri =
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().logicalLink(epA))
                getAAIClient().create(logicalLinkUri, resource)

                serviceIntentUtils.attachLogicalLinkToAllottedResource(execution, serviceIntentUtils.AAI_VERSION,
                        allottedResourceUri, epA);
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCloudLeasedLineCreate.createLogicalLinksForAllocatedResource: " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void preprocessSdncCreateCllRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
                'execution=' + execution.getId() +
                ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)

        logger.trace("STARTED preProcessSDNCActivateRequest Process")
        try {
            String serviceInstanceId = execution.getVariable("cllId")

            String createSDNCRequest = serviceIntentUtils.buildSDNCRequest(execution, serviceInstanceId, "create")

            execution.setVariable("CLL_SDNCRequest", createSDNCRequest)
            logger.debug("Outgoing SDNCRequest is: \n" + createSDNCRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002,
                    "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
    }


    void validateSDNCResponse(DelegateExecution execution, String response, String method) {
        serviceIntentUtils.validateSDNCResponse(execution, response, method)
    }
}
