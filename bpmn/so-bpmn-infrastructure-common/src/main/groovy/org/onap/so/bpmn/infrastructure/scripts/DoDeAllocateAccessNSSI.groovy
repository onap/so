/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
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

import static org.apache.commons.lang3.StringUtils.isBlank

import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.*
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.DeAllocateNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.NetworkType
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.beans.nsmf.oof.SubnetType
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.oof.adapter.beans.payload.OofRequest
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonObject
import com.google.gson.Gson


/**
 * Internal AN NSSMF to handle NSSI Deallocation
 */
class DoDeAllocateAccessNSSI extends AbstractServiceTaskProcessor {

	String Prefix="DoDeAllocateAccessNSSI"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	RequestDBUtil requestDBUtil = new RequestDBUtil()
	JsonUtils jsonUtil = new JsonUtils()
	OofUtils oofUtils = new OofUtils()
	ObjectMapper objectMapper = new ObjectMapper()
	private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)
	
	private static final Logger logger = LoggerFactory.getLogger(DoDeAllocateAccessNSSI.class)
	private static final String ROLE_SLICE_PROFILE = "slice-profile-instance"
	private static final String ROLE_NSSI = "nssi"

	private static final String AN_NF = "AN_NF"
	private static final String TN_FH = "TN_FH"
	private static final String TN_MH = "TN_MH"

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		logger.debug("${Prefix} - Start preProcessRequest")

		String sliceParams = execution.getVariable("sliceParams")
	        def sNssaiList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceParams, "snssaiList"))
		String anSliceProfileId = jsonUtil.getJsonValue(sliceParams, "sliceProfileId")
		String nsiId = jsonUtil.getJsonValue(sliceParams, "nsiId")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		String anNssiId = execution.getVariable("serviceInstanceID")

		if((sNssaiList.empty) || isBlank(anSliceProfileId) || isBlank(nsiId)) {
			String msg = "Input fields cannot be null : Mandatory attributes : [snssaiList, sliceProfileId, nsiId]"
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		if( isBlank(anNssiId) || isBlank(globalSubscriberId) || isBlank(subscriptionServiceType)) {
			String msg = "Missing Input fields from main process : [serviceInstanceID, globalSubscriberId, subscriptionServiceType]"
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		execution.setVariable("sNssaiList", sNssaiList)
		execution.setVariable("anSliceProfileId", anSliceProfileId)
		execution.setVariable("nsiId", nsiId)
		execution.setVariable("anNssiId", anNssiId)

		logger.debug("${Prefix} - Preprocessing completed with sliceProfileId : ${anSliceProfileId} , nsiId : ${nsiId} , nssiId : ${anNssiId}")
	}

	/**
	 * Method to fetch AN NSSI Constituents and Slice Profile constituents
	 * @param execution
	 */
	void getRelatedInstances(DelegateExecution execution) {
		logger.debug("${Prefix} - Get Related Instances")
		String anSliceProfileId = execution.getVariable("anSliceProfileId")
		String anNssiId = execution.getVariable("anNssiId")

		Map<String,ServiceInstance> relatedSPs = new HashMap<>()
		execution.setVariable("relatedSPs", getRelatedInstancesByRole(execution, ROLE_SLICE_PROFILE, anSliceProfileId))
		execution.setVariable("anNfSliceProfileId", getInstanceIdByWorkloadContext(execution.getVariable("relatedSPs"), AN_NF))

		Map<String,ServiceInstance> relatedNssis = new HashMap<>()
                relatedNssis = getRelatedInstancesByRole(execution, ROLE_NSSI, anNssiId)
		execution.setVariable("relatedNssis", relatedNssis)
                if(relatedNssis.size() == 1) {
                        execution.setVariable("IsRANNfAlonePresent", true)
                }
	}
	

	/**
	 * @param execution
	 */
	void prepareOOFAnNssiTerminationRequest(DelegateExecution execution) {
		logger.debug("Start prepareOOFTerminationRequest")
        String requestId = execution.getVariable("msoRequestId")
		String messageType = "AN_NSSITermination"
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
		String serviceInstanceId = execution.getVariable("nsiId")
		String anNssiId = execution.getVariable("anNssiId")
        String oofRequest = oofUtils.buildTerminateNxiRequest(requestId,anNssiId, "NSSI",messageType,serviceInstanceId)
		execution.setVariable("oofAnNssiPayload", oofRequest)
        logger.debug("Finish prepareOOFTerminationRequest")

	}
	
	void performOofAnNSSITerminationCall(DelegateExecution execution) {
		boolean terminateAnNSSI = callOofAdapter(execution,execution.getVariable("oofAnNssiPayload"))
		execution.setVariable("terminateAnNSSI", terminateAnNSSI)
	}
	
	/**
	 * @param execution
	 */
	void prepareOOFAnNfNssiTerminationRequest(DelegateExecution execution) {
		logger.debug("Start prepareOOFAnNfNssiTerminationRequest")
		String requestId = execution.getVariable("msoRequestId")
		String messageType = "AN_NF_NSSITermination"
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
		String serviceInstanceId = execution.getVariable("anNssiId")

		String anNfNssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"),AN_NF)
		execution.setVariable("anNfNssiId", anNfNssiId)

		String oofRequest = oofUtils.buildTerminateNxiRequest(requestId,anNfNssiId, "NSSI",messageType,serviceInstanceId)
		execution.setVariable("oofAnNfNssiPayload", oofRequest)
		logger.debug("Finish prepareOOFAnNfNssiTerminationRequest")

	}

	void performOofAnNfNSSITerminationCall(DelegateExecution execution) {
		boolean terminateAnNfNSSI = callOofAdapter(execution,execution.getVariable("oofAnNfNssiPayload"))
		execution.setVariable("terminateAnNfNSSI", terminateAnNfNSSI)
		if(!terminateAnNfNSSI) {
			execution.setVariable("modifyAction","deallocate")
		}
	}
	
	void prepareSdnrRequest(DelegateExecution execution) {

		String anNfNssiId = execution.getVariable("anNfNssiId")
		String sNssai = execution.getVariable("sNssaiList").get(0)
		String sliceProfileId = execution.getVariable("anNfSliceProfileId")
		String reqId = execution.getVariable("msoRequestId")
		String messageType = "SDNRTerminateResponse"
		StringBuilder callbackURL = new StringBuilder(UrnPropertiesReader.getVariable("mso.workflow.message.endpoint", execution))
		callbackURL.append("/").append(messageType).append("/").append(reqId)

		JsonObject input = new JsonObject()
		input.addProperty("RANNFNSSIId", anNfNssiId)
		input.addProperty("callbackURL", callbackURL.toString())
		input.addProperty("sNSSAI", sNssai)
                input.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId") as String)
		input.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType") as String)
                input.addProperty("sliceProfileId",sliceProfileId)
		input.add("additionalproperties", new JsonObject())

		JsonObject Payload = new JsonObject()

		JsonObject wrapinput = new JsonObject()
		wrapinput.addProperty("action", "deallocate")

		JsonObject CommonHeader = new JsonObject()
		CommonHeader.addProperty("timestamp", new Date(System.currentTimeMillis()).format("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", TimeZone.getDefault()))
		CommonHeader.addProperty("api-ver", "1.0")
		CommonHeader.addProperty("originator-id", "testing")
		CommonHeader.addProperty("request-id", reqId)
		CommonHeader.addProperty("sub-request-id", "1")
		CommonHeader.add("flags", new JsonObject())

		JsonObject body = new JsonObject()

		JsonObject sdnrRequest = new JsonObject()
		Payload.add("input", input)
		wrapinput.addProperty("payload", Payload.toString())
		wrapinput.add("common-header", CommonHeader)
		body.add("input", wrapinput)
		sdnrRequest.add("body", body)
                sdnrRequest.addProperty("version", "1.0")
		sdnrRequest.addProperty("rpc-name", "terminateRANSliceInstance")
		sdnrRequest.addProperty("correlation-id", reqId)
		sdnrRequest.addProperty("type", "request")

		String json = sdnrRequest.toString()
		execution.setVariable("sdnrRequest", json)
		execution.setVariable("SDNR_messageType", messageType)
		execution.setVariable("SDNR_timeout", "PT10M")

	}
	
	void processSdnrResponse(DelegateExecution execution) {
		logger.debug("${Prefix} processing SdnrResponse")
		Map<String, Object> resMap = objectMapper.readValue(execution.getVariable("SDNR_Response"),Map.class)
		String status = resMap.get("status")
		String reason = resMap.get("reason")
		if("success".equalsIgnoreCase(status)) {
			execution.setVariable("isAnNfTerminated", true)
		}else {
			execution.setVariable("isAnNfTerminated", false)
			logger.debug("AN NF Termination failed with reason ${reason}")
		}
		logger.debug("${Prefix} processed SdnrResponse")
	}
	
	/**
	 * @param execution
	 * @param oofRequest - Request payload to be sent to adapter
	 * @return
	 */
	boolean callOofAdapter(DelegateExecution execution, Object oofRequest) {
		logger.debug("Start callOofAdapter")
		String requestId = execution.getVariable("msoRequestId")
		String oofAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.oof.endpoint", execution)
		URL requestUrl = new URL(oofAdapterEndpoint)
		OofRequest oofPayload = new OofRequest()
		oofPayload.setApiPath("/api/oof/terminate/nxi/v1")
		oofPayload.setRequestDetails(oofRequest)
		String requestJson = objectMapper.writeValueAsString(oofPayload)
		logger.debug("Calling OOF adapter  : ${requestUrl} with payload : ${requestJson}")
		HttpClient httpClient = new HttpClientFactory().newJsonClient(requestUrl, ONAPComponents.EXTERNAL)
		Response httpResponse = httpClient.post(requestJson)
		int responseCode = httpResponse.getStatus()
		logger.debug("OOF sync response code is: " + responseCode)
		if(responseCode < 200 || responseCode >= 300){
			logger.debug("OOF request failed with reason : " + httpResponse)
			exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
		}else {
			Map<String,Object> response = objectMapper.readValue(httpResponse.getEntity(),Map.class)
			boolean terminateResponse =  response.get("terminateResponse")
			if(!terminateResponse) {
				logger.debug("Terminate response is false because " + response.get("reason"))
			}
			return terminateResponse
		}
	}
	
	void deallocateAnNfNssi(DelegateExecution execution) {
		logger.debug("${Prefix} - call deallocateAnNfNssi ")
		String anNfNssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), AN_NF)
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

		AAIResourcesClient client = new AAIResourcesClient()
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(anNfNssiId))
		if (!client.exists(uri)) {
			logger.debug("AN NF Service Instance was not found in aai : ${anNfNssiId}")
		}else {
			client.delete(uri)
		}
	}
	
	/**
	 * Removes relationship between AN NSSI and AN_NF NSSI
	 * @param execution
	 */
	void dissociateAnNfNssi(DelegateExecution execution) {
		logger.debug("${Prefix} - call dissociateAnNfNssi ")
		String anNfNssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), AN_NF)
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

		AAIResourcesClient client = new AAIResourcesClient()
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(anNfNssiId))
		if (!client.exists(uri)) {
			logger.debug("AN NF Service Instance was not found in aai : ${anNfNssiId}")
		}else {
			client.delete(uri)
		}
	}
	
	/**
	 * Method to prepare request for AN NSSI modification
	 * Call Modify AN NSSI in case OOF sends Terminate NSSI=False
	 * @param execution
	 */
	void preparejobForANNSSIModification(DelegateExecution execution) {

		String modificationJobId = UUID.randomUUID().toString()
		execution.setVariable("modificationJobId", modificationJobId)

		Map<String,Object> sliceParams = objectMapper.readValue(execution.getVariable("sliceParams"), Map.class)
		sliceParams.put("modifyAction", "deallocate")
		execution.setVariable("modificationsliceParams", objectMapper.writeValueAsString(sliceParams))

		String serviceId = execution.getVariable("serviceInstanceID")
		String nsiId = execution.getVariable("nsiId")
		logger.debug("Generated new job for Service Instance serviceId:" + serviceId + " operationId:" + modificationJobId)

		ResourceOperationStatus initStatus = new ResourceOperationStatus()
		initStatus.setServiceId(nsiId)
		initStatus.setOperationId(modificationJobId)
		initStatus.setResourceTemplateUUID(serviceId)
		initStatus.setOperType("Modify-Deallocate")
		requestDBUtil.prepareInitResourceOperationStatus(execution, initStatus)

		logger.debug(Prefix + "prepareInitOperationStatus Exit")
	}

	void prepareQueryJobStatus(DelegateExecution execution,String jobId,String networkType,String instanceId) {

		String responseId = "1"
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		String anNssiId = execution.getVariable("anNssiId")

                JsonObject esrInfo = new JsonObject()
                esrInfo.addProperty("networkType", networkType)
                esrInfo.addProperty("vendor", "ONAP_internal")

                JsonObject serviceInfo = new JsonObject()
                if (networkType.equals(SubnetType.AN.getSubnetType())) {
                    serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
                } else {
                    serviceInfo.addProperty("nsiId", anNssiId)
                }
		serviceInfo.addProperty("nssiId", instanceId)
                serviceInfo.addProperty("globalSubscriberId", globalSubscriberId)
		serviceInfo.addProperty("subscriptionServiceType", subscriptionServiceType)

		execution.setVariable("${networkType}_esrInfo", esrInfo.toString())
		execution.setVariable("${networkType}_responseId", responseId)
		execution.setVariable("${networkType}_serviceInfo", serviceInfo.toString())

	}

	void validateJobStatus(DelegateExecution execution,String responseDescriptor) {
		logger.debug("validateJobStatus ${responseDescriptor}")
		String status = jsonUtil.getJsonValue(responseDescriptor, "status")
		String statusDescription = jsonUtil.getJsonValue(responseDescriptor, "statusDescription")
		if("finished".equalsIgnoreCase(status)) {
			execution.setVariable("isSuccess", true)
		}else {
			execution.setVariable("isSuccess", false)
		}
	}
	
	void prepareUpdateJobStatus(DelegateExecution execution,String status,String progress,String statusDescription) {
		String nssiId = execution.getVariable("anNssiId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")

		ResourceOperationStatus roStatus = new ResourceOperationStatus()
		roStatus.setServiceId(nsiId)
		roStatus.setOperationId(jobId)
		//roStatus.setResourceTemplateUUID(nsiId)
		roStatus.setResourceInstanceID(nssiId)
		roStatus.setOperType("DeAllocate")
		roStatus.setProgress(progress)
		roStatus.setStatus(status)
		roStatus.setStatusDescription(statusDescription)
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, roStatus)
	}
	
	void terminateTNFHNssi(DelegateExecution execution) {
		logger.debug("Start terminateTNFHNssi in ${Prefix}")
		String nssmfRequest = buildDeallocateNssiRequest(execution, TN_FH)
		String nssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), TN_FH)
		execution.setVariable("tnFHNSSIId", nssiId)
		String urlString = "/api/rest/provMns/v1/NSS/SliceProfiles/" + nssiId
				String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)
				if (nssmfResponse != null) {
					String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
					execution.setVariable("TN_FH_jobId",jobId)
				} else {
					logger.error("received error message from NSSMF : "+ nssmfResponse)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
				}
				logger.debug("Exit terminateTNFHNssi in ${Prefix}")
	}
	
	void terminateTNMHNssi(DelegateExecution execution) {
		logger.debug("Start terminateTNMHNssi in ${Prefix}")
		String nssmfRequest = buildDeallocateNssiRequest(execution, TN_MH)
		String nssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), TN_MH)
		execution.setVariable("tnMHNSSIId", nssiId)
		String urlString = "/api/rest/provMns/v1/NSS/SliceProfiles/" + nssiId
				String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)
				if (nssmfResponse != null) {
					String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
					execution.setVariable("TN_MH_jobId",jobId)
				} else {
					logger.error("received error message from NSSMF : "+ nssmfResponse)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
				}
				logger.debug("Exit terminateTNMHNssi in ${Prefix}")
	}
	
	void deleteRanNfSliceProfileInAAI(DelegateExecution execution) {
		logger.debug("${Prefix} delete Ran NF SliceProfile In AAI")
		String spId = execution.getVariable("anNfSliceProfileId")
		deleteServiceInstanceInAAI(execution, spId)
	}
	
	void deleteTNSliceProfileInAAI(DelegateExecution execution) {
		logger.debug("${Prefix} delete TN FH SliceProfile In AAI")
		String fhSP = getInstanceIdByWorkloadContext(execution.getVariable("relatedSPs"), TN_FH)
		deleteServiceInstanceInAAI(execution, fhSP)
		logger.debug("${Prefix} delete TN MH SliceProfile In AAI")
		String mhSP = getInstanceIdByWorkloadContext(execution.getVariable("relatedSPs"), TN_MH)
		deleteServiceInstanceInAAI(execution, mhSP)
	}
	
	void deleteANNSSI(DelegateExecution execution) {
		logger.debug("${Prefix} delete AN NSSI")
		String nssiId = execution.getVariable("anNssiId")
		deleteServiceInstanceInAAI(execution, nssiId)
	}
	
	/**
	 * Fetches a collection of service instances with the specific role and maps it based on workload context
	 * (AN-NF,TN-FH,TN-MH)
	 * @param execution
	 * @param role			- nssi/slice profile instance
	 * @param instanceId	- id to which the related list to be found
	 * @return
	 */
	private Map<String,ServiceInstance> getRelatedInstancesByRole(DelegateExecution execution,String role,String instanceId) {
		logger.debug("${Prefix} - Fetching related ${role} from AAI")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

		if( isBlank(role) || isBlank(instanceId)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Role and instanceId are mandatory")
		}

		Map<String,ServiceInstance> relatedInstances = new HashMap<>()

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
					if (role.equalsIgnoreCase(instance.getServiceRole())) {
						relatedInstances.put(instance.getWorkloadContext(),instance)
					}
				}
			}
		}
		}
		logger.debug("Found ${relatedInstances.size()} ${role} related to ${instanceId} ")
		return relatedInstances
	}
	
	private String getInstanceIdByWorkloadContext(Map<String,ServiceInstance> instances,String workloadContext ) {
		String instanceId = instances.get(workloadContext).getServiceInstanceId()
		if(instanceId == null) {
			throw new BpmnError( 2500, "${workloadContext} NSSI ID is not found.")
		}
		return instanceId
	}
	
	/**
	 * Method to handle deallocation of RAN NSSI constituents(TN_FH/TN_MH)
	 * @param execution
	 * @param serviceFunction - TN_FH/TN_MH
	 * @return
	 */
	private String buildDeallocateNssiRequest(DelegateExecution execution,String serviceFunction) {
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		Map<String, ServiceInstance> relatedNssis = execution.getVariable("relatedNssis")

		String anNssiId = execution.getVariable("anNssiId")
                List<String> sNssaiList = execution.getVariable("sNssaiList")

		Map<String, ServiceInstance> relatedSPs = execution.getVariable("relatedSPs")

		DeAllocateNssi deallocateNssi = new DeAllocateNssi()
		deallocateNssi.setNsiId(anNssiId)
		ServiceInstance tnNssi = relatedNssis.get(serviceFunction)
		String nssiId = tnNssi.getServiceInstanceId()

		deallocateNssi.setNssiId(nssiId)
		deallocateNssi.setScriptName("TN1")
		deallocateNssi.setSnssaiList(sNssaiList)
		deallocateNssi.setSliceProfileId(relatedSPs.get(serviceFunction).getServiceInstanceId())

		JsonObject esrInfo = new JsonObject() 
                esrInfo.addProperty("networkType", "tn")
                esrInfo.addProperty("vendor", "ONAP_internal")

		ServiceInfo serviceInfo = new ServiceInfo()
		serviceInfo.setServiceInvariantUuid(tnNssi.getModelInvariantId())
		serviceInfo.setServiceUuid(tnNssi.getModelVersionId())
		serviceInfo.setGlobalSubscriberId(globalSubscriberId)
		serviceInfo.setSubscriptionServiceType(subscriptionServiceType)
                serviceInfo.setNssiId(nssiId)
		serviceInfo.setNssiName(tnNssi.getServiceInstanceName())

		JsonObject json = new JsonObject()
                Gson jsonConverter = new Gson()
                json.add("deAllocateNssi", jsonConverter.toJsonTree(deallocateNssi))
		json.add("esrInfo", esrInfo)
		json.add("serviceInfo", jsonConverter.toJsonTree(serviceInfo))
		return json.toString()
		
	}
	
	private void deleteServiceInstanceInAAI(DelegateExecution execution,String instanceId) {
		try {
			AAIResourcesClient client = getAAIClient()
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(instanceId))
                        client.delete(serviceInstanceUri)
			logger.debug("${Prefix} Exited deleteServiceInstance")
		}catch(Exception e){
			logger.debug("Error occured within deleteServiceInstance method: " + e)
		}
	}

}
