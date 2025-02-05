/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import static org.apache.commons.lang3.StringUtils.isBlank
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.logging.filter.base.ErrorCode
import javax.ws.rs.NotFoundException
import org.onap.so.logging.filter.base.ErrorCode
import jakarta.ws.rs.NotFoundException
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.beans.nsmf.*
import org.onap.so.beans.nsmf.oof.SubnetType
import org.onap.so.bpmn.common.scripts.*
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ServiceArtifact
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils

import java.lang.reflect.Type

/**
 * This class supports the DoCreateVnf building block subflow
 * with the creation of a generic vnf for
 * infrastructure.
 *
 */
class DoActivateSliceService extends AbstractServiceTaskProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DoActivateSliceService.class)

    private static final NSSMF_ACTIVATION_URL = "/api/rest/provMns/v1/NSS/%s/activation"

    private static final NSSMF_DEACTIVATION_URL = "/api/rest/provMns/v1/NSS/%s/deactivation"

    private static final NSSMF_QUERY_JOB_STATUS_URL = "/api/rest/provMns/v1/NSS/jobs/%s"
    private static final ObjectMapper objectMapper = new ObjectMapper()

    String Prefix="DoCNSSMF_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()


    SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils()

    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

	/**
	 * This method gets and validates the incoming
	 * request.
	 *
	 * @param - execution
	 *
	 */
	public void preProcessRequest(DelegateExecution execution) {

		execution.setVariable("prefix",Prefix)
		logger.debug("STARTED Do sendcommandtoNssmf PreProcessRequest Process")

		/*******************/
		try{
            Queue<NssInstance> nssInstances = execution.getVariable("nssInstances") as Queue<NssInstance>
            NssInstance nssInstance = nssInstances.poll()
            execution.setVariable("nssInstances", nssInstances)
            execution.setVariable("nssInstance", nssInstance)

			logger.info("the end !!")
		}catch(BpmnError b){
			logger.debug("Rethrowing MSOWorkflowException")
			throw b
		}catch(Exception e){
			logger.info("the end of catch !!")
			logger.debug(" Error Occured in DoSendCommandToNSSMF PreProcessRequest method!" + e.getMessage())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured in DoSendCommandToNSSMF PreProcessRequest")

		}
		logger.trace("COMPLETED DoSendCommandToNSSMF PreProcessRequest Process")
	}

    void prepareCompose(DelegateExecution execution) {
        NssInstance nssInstance = execution.getVariable("nssInstance") as NssInstance
        execution.setVariable("nssInstanceId", nssInstance.nssiId)
        String serviceModelInfo = """{
                        "modelInvariantUuid":"${nssInstance.modelInvariantId}",
                        "modelUuid":"${nssInstance.modelVersionId}",
                        "modelVersion":""
                    }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)
    }

	/**
	 * get vendor Info
	 * @param execution
	 */
	void processDecomposition(DelegateExecution execution) {
		logger.debug("***** processDecomposition *****")

		try {
			ServiceDecomposition serviceDecomposition =
                    execution.getVariable("serviceDecomposition") as ServiceDecomposition

			String vendor = serviceDecomposition.getServiceRole()
            CustomerInfo customerInfo = execution.getVariable("customerInfo") as CustomerInfo
            NssInstance nssInstance = execution.getVariable("nssInstance") as NssInstance
            String reqUrl
            String actionType
            if (OperationType.ACTIVATE == nssInstance.operationType) {
                reqUrl = String.format(NSSMF_ACTIVATION_URL, nssInstance.snssai)
                actionType = "activate"
            } else {
                reqUrl = String.format(NSSMF_DEACTIVATION_URL, nssInstance.snssai)
                actionType = "deactivate"
            }
            execution.setVariable("reqUrl", reqUrl)

            NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()

            EsrInfo esrInfo = new EsrInfo()
            esrInfo.setVendor(vendor)
            esrInfo.setNetworkType(nssInstance.networkType)

            ServiceInfo serviceInfo = ServiceInfo.builder()
                    .nssiId(nssInstance.nssiId)
                    .subscriptionServiceType(customerInfo.subscriptionServiceType)
                    .globalSubscriberId(customerInfo.globalSubscriberId)
                    .nsiId(customerInfo.nsiId)
                    .serviceInvariantUuid(nssInstance.modelInvariantId)
                    .serviceUuid(nssInstance.modelVersionId)
                    .serviceType(nssInstance.serviceType)
                    .actionType(actionType)
                    .build()

            ActDeActNssi actDeActNssi = new ActDeActNssi()
            actDeActNssi.setNsiId(customerInfo.nsiId)
            actDeActNssi.setNssiId(nssInstance.nssiId)
            actDeActNssi.setSnssaiList(Arrays.asList(customerInfo.snssai))

            String sliceProfileId = getRelatedSliceProfileId(execution, customerInfo.globalSubscriberId, customerInfo.subscriptionServiceType, nssInstance.nssiId, customerInfo.snssai, "slice-profile")
            actDeActNssi.setSliceProfileId(sliceProfileId)

            nbiRequest.setEsrInfo(esrInfo)
            nbiRequest.setServiceInfo(serviceInfo)
            nbiRequest.setActDeActNssi(actDeActNssi)
            execution.setVariable("nbiRequest", nbiRequest)
            execution.setVariable("esrInfo", esrInfo)
            execution.setVariable("serviceInfo", serviceInfo)

		} catch (any) {
			String exceptionMessage = "Bpmn error encountered in deallocate nssi. processDecomposition() - " + any.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.debug("***** Exit processDecomposition *****")
	}

        private String getRelatedSliceProfileId(DelegateExecution execution, String globalSubscriberId, String subscriptionServiceType, String instanceId, String snssai, String role) {
                logger.debug("${Prefix} - Get Related Slice Profile")
		if( isBlank(role) || isBlank(instanceId)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Role and instanceId are mandatory")
		}

                String nssiId;
		AAIResourcesClient client = getAAIClient()
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(instanceId))
		if (!client.exists(uri)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai : ${instanceId}")
		}
		AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
		Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
		if(si.isPresent()) {
		List<Relationship> relationshipList = si.get().getRelationshipList().getRelationship()
		for (Relationship relationship : relationshipList) {
			String relatedTo = relationship.getRelatedTo()
			if (relatedTo.toLowerCase() == "service-instance") {
				String relatioshipurl = relationship.getRelatedLink()
				String serviceInstanceId =
						relatioshipurl.substring(relatioshipurl.lastIndexOf("/") + 1, relatioshipurl.length())
				uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))
				if (!client.exists(uri)) {
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
							"Service Instance was not found in aai: ${serviceInstanceId} related to ${instanceId}")
				}
				AAIResultWrapper wrapper01 = client.get(uri, NotFoundException.class)
				Optional<ServiceInstance> serviceInstance = wrapper01.asBean(ServiceInstance.class)
				if (serviceInstance.isPresent()) {
					ServiceInstance instance = serviceInstance.get()
					if (role.equalsIgnoreCase(instance.getServiceRole()) && snssai.equalsIgnoreCase(instance.getEnvironmentContext())) {
                        nssiId = instance.getServiceInstanceId()
					}
				}
			}
		}
		}
		return nssiId
		logger.debug("${Prefix} - Exit Get Related Slice Profile instances")
    }

    /**
     * send Create Request NSSMF
     * @param execution
     */
    void sendCreateRequestNSSMF(DelegateExecution execution) {
        NssmfAdapterNBIRequest nbiRequest = execution.getVariable("nbiRequest") as NssmfAdapterNBIRequest
        String nssmfRequest = objectMapper.writeValueAsString(nbiRequest)
        logger.debug("sendCreateRequestNSSMF: " + nssmfRequest)

        String reqUrl = execution.getVariable("reqUrl")
        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, reqUrl, nssmfRequest)

        if (response != null) {
            NssiResponse nssiResponse = objectMapper.readValue(response, NssiResponse.class)
            execution.setVariable("nssiAllocateResult", nssiResponse)
        }
        //todo: error
    }

    /**
     * query nssi allocate status
     * @param execution
     */
    void queryNSSIStatus(DelegateExecution execution) {
        NssmfAdapterNBIRequest nbiRequest = new NssmfAdapterNBIRequest()
        EsrInfo esrInfo = execution.getVariable("esrInfo") as EsrInfo
        ServiceInfo serviceInfo = execution.getVariable("serviceInfo") as ServiceInfo
        nbiRequest.setEsrInfo(esrInfo)
        nbiRequest.setServiceInfo(serviceInfo)

        NssiResponse nssiAllocateResult = execution.getVariable("nssiAllocateResult") as NssiResponse
        String jobId = nssiAllocateResult.getJobId()

        String endpoint = String.format(NSSMF_QUERY_JOB_STATUS_URL, jobId)

        String response =
                nssmfAdapterUtils.sendPostRequestNSSMF(execution, endpoint, objectMapper.writeValueAsString(nbiRequest))

        logger.debug("nssmf response nssiAllocateStatus:" + response)

        if (response != null) {
            JobStatusResponse jobStatusResponse = objectMapper.readValue(response, JobStatusResponse.class)

            execution.setVariable("nssiAllocateStatus", jobStatusResponse)
            if (jobStatusResponse.getResponseDescriptor().getProgress() == 100) {
                execution.setVariable("jobFinished", true)
            }
        }
    }

    void timeDelay(DelegateExecution execution) {
        logger.trace("Enter timeDelay in DoAllocateNSSI()")
        try {
            Thread.sleep(60000)

            int currentCycle = execution.hasVariable("currentCycle") ?
                    execution.getVariable("currentCycle") as Integer : 1

            currentCycle = currentCycle + 1
            if(currentCycle >  60)
            {
                logger.trace("Completed all the retry times... but still nssmf havent completed the creation process...")
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, "NSSMF creation didnt complete by time...")
            }
            execution.setVariable("currentCycle", currentCycle)
        } catch(InterruptedException e) {
            logger.info("Time Delay exception" + e)
        }
        logger.trace("Exit timeDelay in DoAllocateNSSI()")
    }

    void sendSyncError (DelegateExecution execution) {
		logger.trace("start sendSyncError")
		try {
			String errorMessage = ""
			if (execution.getVariable("WorkflowException") instanceof WorkflowException) {
				WorkflowException wfe = execution.getVariable("WorkflowException")
				errorMessage = wfe.getErrorMessage()
			} else {
				errorMessage = "Sending Sync Error."
			}

			String buildworkflowException =
					"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>${MsoUtils.xmlEscape(errorMessage)}</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7000</aetgt:ErrorCode>
				   </aetgt:WorkflowException>"""

			logger.debug(buildworkflowException)
			sendWorkflowResponse(execution, 500, buildworkflowException)

		} catch (Exception ex) {
			logger.debug("Sending Sync Error Activity Failed. " + "\n" + ex.getMessage())
		}
		logger.trace("finished sendSyncError")
	}
}
