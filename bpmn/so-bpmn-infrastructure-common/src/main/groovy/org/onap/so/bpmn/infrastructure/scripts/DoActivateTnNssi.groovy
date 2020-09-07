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

public class DoActivateTnNssi extends AbstractServiceTaskProcessor {
    String Prefix = "TNACT_"

    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    RequestDBUtil requestDBUtil = new RequestDBUtil()
    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
    JsonSlurper jsonSlurper = new JsonSlurper()
    ObjectMapper objectMapper = new ObjectMapper()
    private static final Logger logger = LoggerFactory.getLogger(DoActivateTnNssi.class)


    public void preProcessRequest(DelegateExecution execution) {
        logger.debug("Start preProcessRequest")

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

        String operationType = execution.getVariable("operationType")
        String actionType = operationType.equals("activateInstance") ? "activate" : "deactivate"
        execution.setVariable("actionType", actionType)


        logger.debug("Finish preProcessRequest")
    }

    public void preprocessSdncActOrDeactTnNssiRequest(DelegateExecution execution) {
        def method = getClass().getSimpleName() + '.preprocessSdncActivateTnNssiRequest(' +
                'execution=' + execution.getId() + ')'
        def isDebugLogEnabled = execution.getVariable('isDebugLogEnabled')
        logger.trace('Entered ' + method)
        execution.setVariable("prefix", Prefix)

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceID")
            String actionType = execution.getVariable("actionType")

            String createSDNCRequest = buildSDNCRequest(execution, serviceInstanceId, actionType)

            execution.setVariable("createSDNCRequest", createSDNCRequest)
            logger.debug("Outgoing CommitSDNCRequest is: \n" + createSDNCRequest)

        } catch (Exception e) {
            logger.debug("Exception Occured Processing preprocessSdncDeallocateTnNssiRequest. Exception is:\n" + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 1002, "Error Occured during  preProcessSDNCActivateRequest Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preprocessSdncActivateTnNssiRequest Process")
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
			<request-action>${MsoUtils.xmlEscape(action)}</request-action>
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

    public void validateSDNCResponse(DelegateExecution execution, String response) {

        execution.setVariable("prefix", Prefix)
        logger.debug("STARTED ValidateSDNCResponse Process")

        String method = execution.getVariable("actionType")

        WorkflowException workflowException = execution.getVariable("WorkflowException")
        boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")

        logger.debug("workflowException: " + workflowException)

        SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()
        sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)

        String sdncResponse = response
        if (execution.getVariable(Prefix + 'sdncResponseSuccess') == true) {
            logger.debug("Received a Good Response from SDNC Adapter for " + method + " SDNC Call.  Response is: \n" + sdncResponse)
            RollbackData rollbackData = execution.getVariable("rollbackData")

            if (method.equals("deactivate")) {
                rollbackData.put("VNFMODULE", "rollbackSDNCRequestDeactivate", "true")
                execution.setVariable("CRTGVNF_sdncActivateCompleted", true)
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
        String operType = execution.getVariable("actionType")


        ResourceOperationStatus roStatus = new ResourceOperationStatus()
        roStatus.setServiceId(serviceId)
        roStatus.setOperationId(jobId)
        roStatus.setResourceTemplateUUID(nsiId)
        roStatus.setOperType(operType)
        roStatus.setProgress(progress)
        roStatus.setStatus(status)
        roStatus.setStatusDescription(statusDescription)
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, status)
    }

}

