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
import org.onap.aai.domain.yang.Relationship
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
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

class TnNssmfUtils {
    private static final Logger logger = LoggerFactory.getLogger(TnNssmfUtils.class);


    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    MsoUtils msoUtils = new MsoUtils()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    TnNssmfUtils() {
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
                reqAction = "AllocateTransportSliceInstance"
                break
            case "delete":
                reqAction = "DeleteTransportSliceInstance"
                break
            case "activate":
                reqAction = "ActivateTransportSliceInstance"
                break
            case "deactivate":
                reqAction = "DeactivateTransportSliceInstance"
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

        logger.debug("TnNssmfUtils.validateSDNCResponse: SDNCResponse: " + response)
        logger.debug("TnNssmfUtils.validateSDNCResponse: workflowException: " + workflowException)

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
                msg = "TnNssmfUtils.validateSDNCResponse: bad Response from SDNC Adapter for " + method + " SDNC Call."
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
                "\n networkType=" + execution.getVariable("networkType") +
                "\n subscriptionServiceType=" + execution.getVariable("subscriptionServiceType") +
                "\n jobId=" + execution.getVariable("jobId") +
                "\n sliceParams=" + execution.getVariable("sliceParams") +
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
}
