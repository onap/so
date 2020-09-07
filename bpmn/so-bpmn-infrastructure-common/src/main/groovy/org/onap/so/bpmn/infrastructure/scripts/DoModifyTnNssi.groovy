/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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
import org.onap.so.bpmn.common.scripts.*
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import static org.apache.commons.lang3.StringUtils.isBlank

public class DoModifyTnNssi extends AbstractServiceTaskProcessor {
    String Prefix = "TNMOD_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()
    ObjectMapper objectMapper = new ObjectMapper()
    private static final Logger logger = LoggerFactory.getLogger(DoModifyTnNssi.class)


    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")
        execution.setVariable("prefix", Prefix)
        String msg = ""

        try {
            String additionalPropJsonStr = execution.getVariable("sliceParams")
            logger.debug(additionalPropJsonStr)

            String requestId = execution.getVariable("msoRequestId")
            logger.debug("Input Request:" + additionalPropJsonStr + " reqId:" + requestId)

            String tnNssiId = execution.getVariable("serviceInstanceID")

            String operationId = UUID.randomUUID().toString()
            execution.setVariable("operationId", operationId)

            logger.debug("Generate new TN NSSI ID:" + tnNssiId)
            tnNssiId = UriUtils.encode(tnNssiId, "UTF-8")
            execution.setVariable("tnNssiId", tnNssiId)

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


    public void preprocessSdncModifyTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncModifyTnNssiRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)
        execution.setVariable("prefix", Prefix)

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceID")

            String createSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "modify")

            execution.setVariable("createSDNCRequest", createSDNCRequest)
            logger.debug("Outgoing CommitSDNCRequest is: \n" + createSDNCRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preprocessSdncModifyTnNssiRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preprocessSdncModifyTnNssiRequest Process")
    }

    public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action) {

        String uuid = execution.getVariable('testReqId') // for junits
        if (uuid == null) {
            uuid = execution.getVariable("msoRequestId") + "-" + System.currentTimeMillis()
        }
        def callbackURL = execution.getVariable("sdncCallbackUrl")
        def requestId = execution.getVariable("msoRequestId")
        def serviceId = execution.getVariable("serviceInstanceID")
        def vnfType = execution.getVariable("serviceType")
        def vnfName = execution.getVariable("sliceServiceInstanceName")
        def tenantId = execution.getVariable("serviceInstanceID")
        def source = execution.getVariable("serviceInstanceID")
        def vnfId = execution.getVariable("serviceInstanceID")
        def cloudSiteId = execution.getVariable("serviceInstanceID")
        def serviceModelInfo = execution.getVariable("serviceModelInfo")
        def vnfModelInfo = execution.getVariable("serviceModelInfo")
        def globalSubscriberId = execution.getVariable("globalSubscriberId")

        String vnfNameString = """<vnf-name>${MsoUtils.xmlEscape(vnfName)}</vnf-name>"""
        String serviceEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(serviceModelInfo)
        String vnfEcompModelInformation = sdncAdapterUtils.modelInfoToEcompModelInformation(vnfModelInfo)

        String sdncVNFParamsXml = ""
        String sdncRequest =
                """<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
	   <sdncadapter:RequestHeader>
				<sdncadapter:RequestId>${MsoUtils.xmlEscape(uuid)}</sdncadapter:RequestId>
				<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(svcInstId)}</sdncadapter:SvcInstanceId>
				<sdncadapter:SvcAction>${MsoUtils.xmlEscape(action)}</sdncadapter:SvcAction>
				<sdncadapter:SvcOperation>vnf-topology-operation</sdncadapter:SvcOperation>
				<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
				<sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
		</sdncadapter:RequestHeader>
	<sdncadapterworkflow:SDNCRequestData>
		<request-information>
			<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
			<request-action>AllocateTnNssi</request-action>
			<source>${MsoUtils.xmlEscape(source)}</source>
			<notification-url/>
			<order-number/>
			<order-version/>
		</request-information>
		<service-information>
			<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
			<subscription-service-type>${MsoUtils.xmlEscape(serviceId)}</subscription-service-type>
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

    public void validateSDNCResponse(DelegateExecution execution, String response, String method) {

        execution.setVariable("prefix", Prefix)
        logger.debug("STARTED ValidateSDNCResponse Process")

        WorkflowException workflowException = execution.getVariable("WorkflowException")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

        logger.debug("workflowException: " + workflowException)

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

        String sdncResponse = response
        if (execution.getVariable(Prefix + 'sdncResponseSuccess') == true) {
            logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
            RollbackData rollbackData = execution.getVariable("rollbackData")

            if (method.equals("modify")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestModify", "true")
                execution.setVariable("CRTGVNF_sdncModifyCompleted", true)
            } else if (method.equals("activate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestActivate", "true")
            }
            execution.setVariable("rollbackData", rollbackData)
        } else {
            logger.debug("Received a BAD Response from SDNC Adapter for " + method + " SDNC Call.")
            throw new BpmnError("MSOWorkflowException")
        }
        logger.trace("COMPLETED ValidateSDNCResponse Process")
    }


    public void updateAAIOrchStatus(DelegateExecution execution) {
        logger.debug("Start updateAAIOrchStatus")
        String tnNssiId = execution.getVariable("tnNssiId")
        String orchStatus = execution.getVariable("orchestrationStatus")

        try {
            ServiceInstance si = new ServiceInstance()
            si.setOrchestrationStatus(orchStatus)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, tnNssiId)
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

