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
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoCreateTnNssiInstance extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DoCreateTnNssiInstance.class);
    JsonUtils jsonUtil = new JsonUtils()
    TnNssmfUtils tnNssmfUtils = new TnNssmfUtils()
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    String Prefix = "DCTN_"

    void preProcessRequest(DelegateExecution execution) {
        String msg = ""
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

        logger.trace("Exit preProcessRequest")
    }


    void createSliceProfile(DelegateExecution execution) {

        String sliceserviceInstanceId = execution.getVariable("sliceServiceInstanceId")
        String sliceProfileStr = execution.getVariable("sliceProfile")
        String sliceProfileId = UUID.randomUUID().toString()
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
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, execution.getVariable
                    ("globalSubscriberId"),
                    execution.getVariable("subscriptionServiceType"), sliceserviceInstanceId, sliceProfileId)
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

        String serviceRole = "TN"
        String serviceType = execution.getVariable("subscriptionServiceType")
        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
        String sliceProfileStr = execution.getVariable("sliceProfile")
        try {
            org.onap.aai.domain.yang.ServiceInstance ss = new org.onap.aai.domain.yang.ServiceInstance()
            ss.setServiceInstanceId(ssInstanceId)
            String sliceInstanceName = execution.getVariable("sliceServiceInstanceName")
            ss.setServiceInstanceName(sliceInstanceName)
            ss.setServiceType(serviceType)
            String serviceStatus = "allocated"
            ss.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String modelUuid = execution.getVariable("modelUuid")
            ss.setModelInvariantId(modelInvariantUuid)
            ss.setModelVersionId(modelUuid)
            String serviceInstanceLocationid = tnNssmfUtils.getFirstPlmnIdFromSliceProfile(sliceProfileStr)
            ss.setServiceInstanceLocationId(serviceInstanceLocationid)
            String snssai = tnNssmfUtils.getFirstSnssaiFromSliceProfile(sliceProfileStr)
            ss.setEnvironmentContext(snssai)
            ss.setServiceRole(serviceRole)
            AAIResourcesClient client = getAAIClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), ssInstanceId)
            client.create(uri, ss)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateTnNssiInstance.createServiceInstance. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }


    void createAllottedResource(DelegateExecution execution) {
        String serviceInstanceId = execution.getVariable('sliceServiceInstanceId')

        AAIResourcesClient resourceClient = getAAIClient()
        AAIResourceUri ssServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

        try {
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
                //AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.SERVICE_INSTANCE, UriBuilder.fromPath(ssServiceuri).build())
                //getAAIClient().connect(allottedResourceUri,ssServiceuri)
                //execution.setVariable("aaiARPath", allottedResourceUri.build().toString());

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
