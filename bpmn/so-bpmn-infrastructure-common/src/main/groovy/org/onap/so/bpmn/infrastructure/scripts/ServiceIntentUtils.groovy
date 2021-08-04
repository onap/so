/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import org.onap.aai.domain.yang.AllottedResources
import org.onap.aai.domain.yang.LogicalLink
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.AAIVersion
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.apache.commons.lang3.StringUtils.isBlank
import static org.apache.commons.lang3.StringUtils.isNotBlank

class ServiceIntentUtils {
    static final String AAI_VERSION = AAIVersion.LATEST
    private static final Logger logger = LoggerFactory.getLogger(ServiceIntentUtils.class);


    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    MsoUtils msoUtils = new MsoUtils()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    ServiceIntentUtils() {
    }


    void setCommonExecutionVars(DelegateExecution execution) {
        setCommonExecutionVars(execution, true)
    }

    void setCommonExecutionVars(DelegateExecution execution, boolean exceptionOnErr) {
        def msg
        try {
            // get request input
            String bpmnRequestStr = execution.getVariable("bpmnRequest")
            logger.debug("Input Request: " + bpmnRequestStr)

            String requestId = execution.getVariable("mso-request-id")
            execution.setVariable("msoRequestId", requestId)
            logger.debug("requestId: " + requestId)

            //subscriberInfo
            String globalSubscriberId = jsonUtil.getJsonValue(bpmnRequestStr, "globalSubscriberId")
            if (isBlank(globalSubscriberId) && exceptionOnErr) {
                msg = "Input globalSubscriberId' is null"
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("globalSubscriberId", globalSubscriberId)
            }

            String serviceType = jsonUtil.getJsonValue(bpmnRequestStr, "serviceType")
            if (isBlank(serviceType) && exceptionOnErr) {
                msg = "Input serviceType is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("serviceType", serviceType)
            }

            String servicename = jsonUtil.getJsonValue(bpmnRequestStr, "name")
            if (isNotBlank(servicename)) {
                execution.setVariable("servicename", servicename)
            } else {
                logger.debug("erviceIntentUtils.setCommonExecutionVars: servicename is NOT set")
            }

            //requestParameters, subscriptionServiceType is 5G
            String subscriptionServiceType = jsonUtil.getJsonValue(bpmnRequestStr, "subscriptionServiceType")
            if (isBlank(subscriptionServiceType)) {
                msg = "Input subscriptionServiceType is null"
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            } else {
                execution.setVariable("subscriptionServiceType", subscriptionServiceType)
            }

            String jobId = UUID.randomUUID().toString()
            execution.setVariable("jobId", jobId)

            String sliceParams = jsonUtil.getJsonValue(bpmnRequestStr, "additionalProperties")
            execution.setVariable("serviceIntentParams", sliceParams)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in ServiceIntentUtils.setCommonExecutionVars: " + ex.getMessage()
            logger.debug(msg)
            if (exceptionOnErr) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
        }
    }

    void setSdncCallbackUrl(DelegateExecution execution, boolean exceptionOnErr) {
        setSdncCallbackUrl(execution, "sdncCallbackUrl", exceptionOnErr)
    }

    void setSdncCallbackUrl(DelegateExecution execution, String variableName, boolean exceptionOnErr) {
        String sdncCallbackUrl = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback', execution)

        if (isBlank(sdncCallbackUrl) && exceptionOnErr) {
            String msg = "mso.workflow.sdncadapter.callback is null"
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        } else {
            execution.setVariable(variableName, sdncCallbackUrl)
        }
    }

    String buildSDNCRequest(DelegateExecution execution, String svcInstId, String svcAction) {
        String reqAction
        switch (svcAction) {
            case "create":
                reqAction = "CreateCloudLeasedLineInstance"
                break
            case "delete":
                reqAction = "DeleteCloudLeasedLineInstance"
                break
            case "activate":
                reqAction = "ActivateCloudLeasedLineInstance"
                break
            case "deactivate":
                reqAction = "DeactivateCloudLeasedLineInstance"
                break
            case "update":
                reqAction = "ModifyCloudLeasedLineInstance"
                break
            default:
                reqAction = svcAction
        }

        buildSDNCRequest(execution, svcInstId, svcAction, reqAction)
    }

    String buildSDNCRequest(DelegateExecution execution, String svcInstId, String svcAction, String reqAction) {

        String uuid = execution.getVariable('testReqId') // for junits
        if (uuid == null) {
            uuid = execution.getVariable("msoRequestId") + "-" + System.currentTimeMillis()
        }

        def callbackURL = execution.getVariable("sdncCallbackUrl")
        def requestId = execution.getVariable("msoRequestId")
        def serviceId = execution.getVariable("sliceServiceInstanceId")
        def subServiceType = execution.getVariable("subscriptionServiceType")
        def vnfType = execution.getVariable("serviceType")
        def vnfName = execution.getVariable("sliceServiceInstanceName")
        def tenantId = execution.getVariable("sliceServiceInstanceId")
        def source = execution.getVariable("sliceServiceInstanceId")
        def vnfId = execution.getVariable("sliceServiceInstanceId")
        def cloudSiteId = execution.getVariable("sliceServiceInstanceId")
        def serviceModelInfo = execution.getVariable("serviceModelInfo")
        def vnfModelInfo = execution.getVariable("serviceModelInfo")
        def globalSubscriberId = execution.getVariable("globalSubscriberId")

        String vnfNameString = """<vnf-name>${MsoUtils.xmlEscape(vnfName)}</vnf-name>"""
        String serviceEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(serviceModelInfo)
        String vnfEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vnfModelInfo)

        String sdncVNFParamsXml = ""

        if (execution.getVariable("vnfParamsExistFlag") == true) {
            sdncVNFParamsXml = buildSDNCParamsXml(execution)
        } else {
            sdncVNFParamsXml = buildDefaultVnfInputParams(vnfId)
        }

        String sdncRequest =
                """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
                                                    xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
                                                    xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
         <sdncadapter:RequestHeader>
            <sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
            <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
            <sdncadapter:SvcAction>${MsoUtils.xmlEscape(svcAction)}</sdncadapter:SvcAction>
            <sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
            <sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
            <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
         </sdncadapter:RequestHeader>
    <sdncadapterworkflow:SDNCRequestData>
        <request-information>
            <request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
            <request-action>${MsoUtils.xmlEscape(reqAction)}</request-action>
            <source>${MsoUtils.xmlEscape(source)}</source>
            <notification-url/>
            <order-number/>
            <order-version/>
        </request-information>
        <service-information>
            <service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
            <subscription-service-type>${MsoUtils.xmlEscape(subServiceType)}</subscription-service-type>
            ${serviceEcompModelInformation}
            <service-instance-id>${MsoUtils.xmlEscape(svcInstId)}</service-instance-id>
            <global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
        </service-information>
        <vnf-information>
            <vnf-id>${MsoUtils.xmlEscape(vnfId)}</vnf-id>
            <vnf-type>${MsoUtils.xmlEscape(vnfType)}</vnf-type>
            ${vnfEcompModelInformation}
        </vnf-information>
        <vnf-request-input>
            ${vnfNameString}
            <tenant>${MsoUtils.xmlEscape(tenantId)}</tenant>
            <aic-cloud-region>${MsoUtils.xmlEscape(cloudSiteId)}</aic-cloud-region>
            ${sdncVNFParamsXml}
        </vnf-request-input>
    </sdncadapterworkflow:SDNCRequestData>
    </sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

        logger.debug("sdncRequest:  " + sdncRequest)
        return sdncRequest
    }


    String buildDefaultVnfInputParams(String vnfName) {
        String res =
                """<vnf-input-parameters>
                      <param>
                          <name>${MsoUtils.xmlEscape(vnfName)}</name>
                      </param>
                   </vnf-input-parameters>"""

        return res
    }

    String buildSDNCParamsXml(DelegateExecution execution) {
        String params = ""
        StringBuilder sb = new StringBuilder()
        Map<String, String> paramsMap = execution.getVariable("TNNSSMF_vnfParamsMap")

        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String paramsXml
            String key = entry.getKey();
            String value = entry.getValue()
            paramsXml = """<${key}>$value</$key>"""
            params = sb.append(paramsXml)
        }
        return params
    }

    void validateSDNCResponse(DelegateExecution execution, String response, String method) {
        validateSDNCResponse(execution, response, method, true)
    }

    void validateSDNCResponse(DelegateExecution execution, String response, String method, boolean exceptionOnErr) {
        logger.debug("STARTED ValidateSDNCResponse Process")

        String msg

        String prefix = execution.getVariable("prefix")
        if (isBlank(prefix)) {
            if (exceptionOnErr) {
                msg = "validateSDNCResponse: prefix is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            return
        }

        WorkflowException workflowException = execution.getVariable("WorkflowException")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

        logger.debug("ServiceIntentUtils.validateSDNCResponse: SDNCResponse: " + response)
        logger.debug("ServiceIntentUtils.validateSDNCResponse: workflowException: " + workflowException)

        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

        String sdncResponse = response
        if (execution.getVariable(prefix + 'sdncResponseSuccess') == true) {
            logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
            RollbackData rollbackData = execution.getVariable("rollbackData")
            if (rollbackData == null) {
                rollbackData = new RollbackData()
            }

            if (method.equals("allocate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestAllocate", "true")
            } else if (method.equals("deallocate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestDeallocate", "true")
            } else if (method.equals("activate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestActivate", "true")
            } else if (method.equals("deactivate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestDeactivate", "true")
            } else if (method.equals("modify")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestModify", "true")
            }
            execution.setVariable("rollbackData", rollbackData)
        } else {
            if (exceptionOnErr) {
                msg = "ServiceIntentUtils.validateSDNCResponse: bad Response from SDNC Adapter for " + method + " SDNC Call."
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
        }

        logger.debug("COMPLETED ValidateSDNCResponse Process")
    }

    String getExecutionInputParams(DelegateExecution execution) {
        String res = "\n msoRequestId=" + execution.getVariable("msoRequestId") +
                "\n modelInvariantUuid=" + execution.getVariable("modelInvariantUuid") +
                "\n modelUuid=" + execution.getVariable("modelUuid") +
                "\n serviceInstanceID=" + execution.getVariable("serviceInstanceID") +
                "\n operationType=" + execution.getVariable("operationType") +
                "\n globalSubscriberId=" + execution.getVariable("globalSubscriberId") +
                "\n dummyServiceId=" + execution.getVariable("dummyServiceId") +
                "\n nsiId=" + execution.getVariable("nsiId") +
                "\n serviceType=" + execution.getVariable("serviceType") +
                "\n subscriptionServiceType=" + execution.getVariable("subscriptionServiceType") +
                "\n jobId=" + execution.getVariable("jobId") +
                "\n serviceIntentParams=" + execution.getVariable("serviceIntentParams") +
                "\n servicename=" + execution.getVariable("servicename")

        return res
    }

    String getFirstSnssaiFromSliceProfile(String sliceProfileStr) {
        String snssaiListStr = jsonUtil.getJsonValue(sliceProfileStr, "snssaiList")
        String snssai = jsonUtil.StringArrayToList(snssaiListStr).get(0)

        return snssai
    }

    String getFirstPlmnIdFromSliceProfile(String sliceProfileStr) {
        String plmnListStr = jsonUtil.getJsonValue(sliceProfileStr, "plmnIdList")
        String res = jsonUtil.StringArrayToList(plmnListStr).get(0)

        return res
    }

    void createRelationShipInAAI(DelegateExecution execution, AAIResourceUri uri, Relationship relationship) {
        logger.debug("createRelationShipInAAI Start")
        String msg
        AAIResourcesClient client = new AAIResourcesClient()
        try {
            if (!client.exists(uri)) {
                logger.info("ERROR: createRelationShipInAAI: not exist: uri={}", uri)
                return
            }
            AAIResourceUri from = ((AAIResourceUri) (uri.clone())).relationshipAPI()
            client.create(from, relationship)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in createRelationShipInAAI. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("createRelationShipInAAI Exit")
    }

    void attachLogicalLinkToAllottedResource(DelegateExecution execution, String aaiVersion, AAIResourceUri arUri,
                                             String logicalLinkId) {

        String toLink = "aai/${aaiVersion}/network/logical-links/logical-link/${logicalLinkId}"

        Relationship relationship = new Relationship()
        relationship.setRelatedLink(toLink)
        relationship.setRelatedTo("logical-link")
        relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")

        createRelationShipInAAI(execution, arUri, relationship)
    }

    void attachNetworkPolicyToAllottedResource(DelegateExecution execution, String aaiVersion,
                                               AAIResourceUri aaiResourceUri, String networkPolicyId) {

        String toLink = "aai/${aaiVersion}/network/network-policies/network-policy/${networkPolicyId}"

        Relationship relationship = new Relationship()
        relationship.setRelatedLink(toLink)
        relationship.setRelatedTo("network-policy")
        relationship.setRelationshipLabel("org.onap.relationships.inventory.Uses")

        createRelationShipInAAI(execution, aaiResourceUri, relationship)

    }

    ResourceOperationStatus buildRoStatus(String nsstId,
                                          String nssiId,
                                          String jobId,
                                          String nsiId,
                                          String action,
                                          String status,
                                          String progress,
                                          String statusDescription) {
        ResourceOperationStatus roStatus = new ResourceOperationStatus()
        roStatus.setResourceTemplateUUID(nsstId)
        roStatus.setResourceInstanceID(nssiId)
        roStatus.setServiceId(nsiId)
        roStatus.setOperationId(jobId)
        roStatus.setOperType(action)
        roStatus.setProgress(progress)
        roStatus.setStatus(status)
        roStatus.setStatusDescription(statusDescription)

        return roStatus
    }


    void setEnableSdncConfig(DelegateExecution execution) {
        String enableSdnc = UrnPropertiesReader.getVariable(
                "mso.workflow.TnNssmf.enableSDNCNetworkConfig")
        if (isBlank(enableSdnc)) {
            logger.debug("mso.workflow.TnNssmf.enableSDNCNetworkConfig is undefined, so use default value (true)")
            enableSdnc = "true"
        }

        logger.debug("setEnableSdncConfig: enableSdnc=" + enableSdnc)

        execution.setVariable("enableSdnc", enableSdnc)
    }

    String setExecVarFromJsonIfExists(DelegateExecution execution,
                                      String jsonStr, String jsonKey, String varName) {
        return setExecVarFromJsonStr(execution, jsonStr, jsonKey, varName, false)
    }

    String setExecVarFromJsonStr(DelegateExecution execution,
                                 String jsonStr, String jsonKey, String varName,
                                 boolean exceptionOnErr) {
        String msg = ""
        String valueStr = jsonUtil.getJsonValue(jsonStr, jsonKey)
        if (isBlank(valueStr)) {
            if (exceptionOnErr) {
                msg = "cannot find " + jsonKey + " in " + jsonStr
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
        } else {
            execution.setVariable(varName, valueStr)
        }

        return valueStr
    }

    ServiceInstance getServiceInstanceFromAai(String serviceInstanceId) {
        if (isBlank(serviceInstanceId)) {
            logger.error("ERROR: getServiceInstanceFromAai: serviceInstanceId is blank")
            return null
        }

        ServiceInstance nssi = null
        AAIResourcesClient client = new AAIResourcesClient()
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.Types.SERVICE_INSTANCE
                .getFragment(serviceInstanceId))
        Optional<ServiceInstance> nssiOpt = client.get(ServiceInstance.class, uri)

        if (nssiOpt.isPresent()) {
            nssi = nssiOpt.get()
            return nssi
        } else {
            String msg = String.format("ERROR: getServiceInstanceFromAai: NSSI %s not found in AAI", serviceInstanceId)
            logger.error(msg)
        }

        return nssi;
    }

    String getModelUuidFromServiceInstance(String serviceInstanceId) {
        ServiceInstance si = getServiceInstanceFromAai(serviceInstanceId)
        if (si == null) {
            String msg = String.format("ERROR: getModelUuidFromServiceInstance: getServiceInstanceFromAai() failed. " +
                    "serviceInstanceId=%s", serviceInstanceId)
            logger.error(msg)
            return null
        }

        return si.getModelVersionId()
    }

    AAIResourceUri buildNetworkPolicyUri(String networkPolicyId) {
        AAIResourceUri networkPolicyUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(networkPolicyId))

        return networkPolicyUri
    }

    AAIResourceUri buildAllottedResourceUri(DelegateExecution execution, String serviceInstanceId,
                                            String allottedResourceId) {

        AAIResourceUri allottedResourceUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                        .customer(execution.getVariable("globalSubscriberId"))
                        .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                        .serviceInstance(serviceInstanceId)
                        .allottedResource(allottedResourceId))

        return allottedResourceUri
    }

    AAIPluralResourceUri buildAllottedResourcesUri(DelegateExecution execution, String serviceInstanceId) {

        AAIPluralResourceUri arsUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                        .customer(execution.getVariable("globalSubscriberId"))
                        .serviceSubscription(execution.getVariable("subscriptionServiceType"))
                        .serviceInstance(serviceInstanceId)
                        .allottedResources())

        return arsUri
    }

    AllottedResources getAllottedResourcesFromAai(DelegateExecution execution, String serviceInstanceId, boolean exceptionOnErr) {
        AllottedResources res
        try {
            AAIResourcesClient client = new AAIResourcesClient()

            AAIPluralResourceUri arsUri = buildAllottedResourcesUri(execution, serviceInstanceId)

            //AAIResultWrapper wrapperAllotted = client.get(arsUri, NotFoundException.class)
            //Optional<AllottedResources> allAllotted = wrapperAllotted.asBean(AllottedResources.class)
            //AllottedResources allottedResources = allAllotted.get()

            Optional<AllottedResources> arsOpt = client.get(AllottedResources.class, arsUri)
            if (arsOpt.isPresent()) {
                res = arsOpt.get()
                return res
            } else {
                String msg = String.format("ERROR: getAllottedResourcesFromAai: ars not found. nssiId=%s", serviceInstanceId)
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        } catch (BpmnError e) {
            if (exceptionOnErr) {
                throw e;
            }
        } catch (Exception ex) {
            if (exceptionOnErr) {
                String msg = String.format("ERROR: getAllottedResourcesFromAai: %s", ex.getMessage())
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        }

        return res
    }

    String getPolicyIdFromAr(DelegateExecution execution, String serviceInstanceId,
                             String arId, boolean exceptionOnErr) {
        String res
        try {
            AAIResourcesClient client = new AAIResourcesClient()

            AAIResourceUri arUri = buildAllottedResourceUri(execution, serviceInstanceId, arId)
            List<AAIResourceUri> policyUriList = getRelationshipUriListInAai(execution, arUri,
                    AAIFluentTypeBuilder.Types.NETWORK_POLICY, exceptionOnErr)
            for (AAIResourceUri policyUri : policyUriList) {
                Optional<NetworkPolicy> policyOpt = client.get(NetworkPolicy.class, policyUri)
                if (policyOpt.isPresent()) {
                    NetworkPolicy policy = policyOpt.get()
                    return policy.getNetworkPolicyId()
                } else {
                    String msg = String.format("ERROR: getPolicyIdFromAr: arUri=%s", policyUri)
                    logger.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                }
            }
        } catch (BpmnError e) {
            if (exceptionOnErr) {
                throw e;
            }
        } catch (Exception ex) {
            if (exceptionOnErr) {
                String msg = String.format("ERROR: getPolicyIdFromAr: %s", ex.getMessage())
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        }

        return res
    }


    List<AAIResourceUri> getRelationshipUriListInAai(DelegateExecution execution,
                                                     AAIResourceUri uri,
                                                     Object info,
                                                     boolean exceptionOnErr) {
        AAIResourcesClient client = new AAIResourcesClient()
        AAIResultWrapper wrapper = client.get(uri);
        Optional<Relationships> relationships = wrapper.getRelationships()
        if (relationships.isPresent()) {
            return relationships.get().getRelatedUris(info)
        } else {
            if (exceptionOnErr) {
                String msg = "ERROR: getRelationshipUriListInAai: No relationship found"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        }

        return null
    }

    List<String> getLogicalLinkNamesFromAr(DelegateExecution execution, String serviceInstanceId,
                                           String arId, boolean exceptionOnErr) {
        List<String> res = new ArrayList<>()
        try {
            AAIResourcesClient client = new AAIResourcesClient()

            AAIResourceUri arUri = buildAllottedResourceUri(execution, serviceInstanceId, arId)
            List<AAIResourceUri> logicalLinkUriList = getRelationshipUriListInAai(execution, arUri,
                    AAIFluentTypeBuilder.Types.LOGICAL_LINK, exceptionOnErr)
            for (AAIResourceUri logicalLinkUri : logicalLinkUriList) {
                Optional<LogicalLink> logicalLinkOpt = client.get(LogicalLink.class, logicalLinkUri)
                if (logicalLinkOpt.isPresent()) {
                    LogicalLink logicalLink = logicalLinkOpt.get()
                    res.add(logicalLink.getLinkName())
                } else {
                    String msg = String.format("ERROR: getLogicalLinkNamesFromAr: logicalLinkUri=%s", logicalLinkUri)
                    logger.error(msg)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                }
            }
        } catch (BpmnError e) {
            if (exceptionOnErr) {
                throw e;
            }
        } catch (Exception ex) {
            if (exceptionOnErr) {
                String msg = String.format("ERROR: getLogicalLinkNamesFromAr: %s", ex.getMessage())
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        }

        return res
    }
}