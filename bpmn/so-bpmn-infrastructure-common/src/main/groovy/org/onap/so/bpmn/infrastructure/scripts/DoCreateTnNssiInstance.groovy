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
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DoCreateTnNssiInstance extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DoCreateTnNssiInstance.class);
    JsonUtils jsonUtil = new JsonUtils()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    String Prefix = "DCTN_"

    void preProcessRequest(DelegateExecution execution) {
        String msg = ""
        logger.trace("Enter preProcessRequest()")

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
        Map<String, Object> sliceProfileMap = execution.getVariable("sliceProfile")
        String sliceProfileId = UUID.randomUUID().toString()
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
        Map<String, Object> sliceProfile = execution.getVariable("sliceProfile")
        String ssInstanceId = execution.getVariable("sliceServiceInstanceId")
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
            String serviceInstanceLocationid = sliceProfile.get("plmnIdList")
            ss.setServiceInstanceLocationId(serviceInstanceLocationid)
            String snssai = sliceProfile.get("snssaiList")
            ss.setEnvironmentContext(snssai)
            ss.setServiceRole(serviceRole)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), ssInstanceId)
            client.create(uri, ss)
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


    void createAllottedResource(DelegateExecution execution) {
        String serviceInstanceId = execution.getVariable('sliceServiceInstanceId')

        AAIResourcesClient resourceClient = new AAIResourcesClient()
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

    public void preprocessSdncAllocateTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preProcessSDNCActivateRequest(' +
                'execution=' + execution.getId() +
                ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)
        execution.setVariable("prefix", Prefix)
        logger.trace("STARTED preProcessSDNCActivateRequest Process")
        try {
            String serviceInstanceId = execution.getVariable("sliceServiceInstanceId")

            String createSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, "create")

            execution.setVariable("createSDNCRequest", createSDNCRequest)
            logger.debug("Outgoing CommitSDNCRequest is: \n" + createSDNCRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preProcessSDNCActivateRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED  preProcessSDNCActivateRequest Process")
    }

    public String buildSDNCRequest(DelegateExecution execution, String svcInstId, String action) {

        String uuid = execution.getVariable('testReqId') // for junits
        if (uuid == null) {
            uuid = execution.getVariable("msoRequestId") + "-" + System.currentTimeMillis()
        }
        def callbackURL = execution.getVariable("sdncCallbackUrl")
        def requestId = execution.getVariable("msoRequestId")
        def serviceId = execution.getVariable("sliceServiceInstanceId")
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
            sdncVNFParamsXml = ""
        }

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

    public String buildSDNCParamsXml(DelegateExecution execution) {

        String params = ""
        StringBuilder sb = new StringBuilder()
        Map<String, String> paramsMap = execution.getVariable("DCVFM_vnfParamsMap")

        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String paramsXml
            String key = entry.getKey();
            if (key.endsWith("_network")) {
                String requestKey = key.substring(0, key.indexOf("_network"))
                String requestValue = entry.getValue()
                paramsXml =
                        """<vnf-networks>
	<network-role>{ functx:substring-before-match(data($param/@name), '_network') }</network-role>
	<network-name>{ $param/text() }</network-name>
</vnf-networks>"""
            } else {
                paramsXml = ""
            }
            params = sb.append(paramsXml)
        }
        return params
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

            if (method.equals("allocate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestAllocate", "true")
                execution.setVariable("CRTGVNF_sdncAllocateCompleted", true)
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
}
