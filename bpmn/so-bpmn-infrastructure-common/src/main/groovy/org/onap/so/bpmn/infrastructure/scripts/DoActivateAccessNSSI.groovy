ommit message
bpmn/so-bpmn-infrastructure-common/src/main/groovy/org/onap/so/bpmn/infrastructure/scripts/DoActivateAccessNSSI.groovy
bpmn/so-bpmn-infrastructure-flows/src/main/resources/subprocess/DoActivateAccessNSSI.bpmn
common/src/main/java/org/onap/so/beans/nsmf/ActDeActNssi.java

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

import javax.ws.rs.NotFoundException

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.beans.nsmf.ActDeActNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonObject

/**
 * Internal AN NSSMF to handle NSSI Activation/Deactivation
 *
 */
class DoActivateAccessNSSI extends AbstractServiceTaskProcessor {
	
	String Prefix="DoActivateAccessNSSI"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	RequestDBUtil requestDBUtil = new RequestDBUtil()
	JsonUtils jsonUtil = new JsonUtils()
	ObjectMapper objectMapper = new ObjectMapper()
	private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

	private static final Logger logger = LoggerFactory.getLogger(DoActivateAccessNSSI.class)
	private static final String ROLE_SLICE_PROFILE = "slice-profile-instance"
	private static final String  ROLE_NSSI = "nssi"

	private static final String KEY_SLICE_PROFILE = "SliceProfile"
	private static final String KEY_NSSI = "NSSI"

	private static final String AN_NF = "AN-NF"
	private static final String TN_FH = "TN-FH"
	private static final String TN_MH = "TN-MH"

	private static final String ACTIVATE = "activateInstance"
	private static final String DEACTIVATE = "deactivateInstance"

	private static final String VENDOR_ONAP = "ONAP"

	Map<String,String> orchStatusMap = new HashMap<>()

	@Override
	public void preProcessRequest(DelegateExecution execution) {
		logger.debug("${Prefix} - Start preProcessRequest")

		String sliceParams = execution.getVariable("sliceParams")
		String sNssaiList = jsonUtil.getJsonValue(sliceParams, "snssaiList")
		String anSliceProfileId = jsonUtil.getJsonValue(sliceParams, "sliceProfileId")
		String nsiId = execution.getVariable("nsiId")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		String anNssiId = execution.getVariable("serviceInstanceID")
		String operationType = execution.getVariable("operationType")

		if(isBlank(sNssaiList) || isBlank(anSliceProfileId) || isBlank(nsiId)) {
			String msg = "Input fields cannot be null : Mandatory attributes : [snssaiList, sliceProfileId, nsiId]"
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		if( isBlank(anNssiId) || isBlank(globalSubscriberId) || isBlank(subscriptionServiceType) || isBlank(operationType)) {
			String msg = "Missing Input fields from main process : [serviceInstanceID, globalSubscriberId, subscriptionServiceType, operationType]"
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
		}

		execution.setVariable("sNssaiList", sNssaiList)
		execution.setVariable("anSliceProfileId", anSliceProfileId)
		execution.setVariable("nsiId", nsiId)
		execution.setVariable("anNssiId", anNssiId)

		orchStatusMap.put(ACTIVATE, "activated")
		orchStatusMap.put(DEACTIVATE, "deactivated")

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
		execution.setVariable("relatedSPs", getRelatedInstancesByRole(execution, ROLE_SLICE_PROFILE,KEY_SLICE_PROFILE, anSliceProfileId))

		Map<String,ServiceInstance> relatedNssis = new HashMap<>()
		execution.setVariable("relatedNssis", getRelatedInstancesByRole(execution, ROLE_NSSI,KEY_NSSI, anNssiId))
		logger.trace("${Prefix} - Exit Get Related instances")
	}
	
	/**
	 * Method to check Slice profile orchestration status
	 * @param execution
	 */
	void getSPOrchStatus(DelegateExecution execution) {
		logger.debug("${Prefix} - Start getSPOrchStatus")
		ServiceInstance sliceProfileInstance = execution.getVariable(KEY_SLICE_PROFILE)
		String orchStatus = sliceProfileInstance.getOrchestrationStatus()
		String operationType = execution.getVariable("operationType")
		if(orchStatusMap.get(operationType).equalsIgnoreCase(orchStatus)) {
			execution.setVariable("shouldChangeSPStatus", true)
		}else {
			execution.setVariable("shouldChangeSPStatus", false)
		}
		logger.debug("${Prefix} -  SPOrchStatus  : ${orchStatus}")
	}
	
	/**
	 * Method to check AN NF's  Slice profile instance orchestration status
	 * @param execution
	 */
	void getAnNfSPOrchStatus(DelegateExecution execution) {
		logger.debug("${Prefix} -  getAnNfSPOrchStatus ")
		ServiceInstance sliceProfileInstance = getInstanceByWorkloadContext(execution.getVariable("relatedSPs"), AN_NF)
		String anNfNssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), AN_NF)
		execution.setVariable("anNfNssiId", anNfNssiId)
		String anNfSPId = sliceProfileInstance.getServiceInstanceId()
		execution.setVariable("anNfSPId", anNfSPId)

		String orchStatus = sliceProfileInstance.getOrchestrationStatus()
		String operationType = execution.getVariable("operationType")
		if(orchStatusMap.get(operationType).equalsIgnoreCase(orchStatus)) {
			execution.setVariable("shouldChangeAN_NF_SPStatus", true)
		}else {
			execution.setVariable("shouldChangeAN_NF_SPStatus", false)
		}
		logger.debug("${Prefix} -  getAnNfSPOrchStatus AN_NF SP ID:${anNfSPId}  : ${orchStatus}")
	}

	void prepareSdnrActivationRequest(DelegateExecution execution) {
		logger.debug("${Prefix} - start prepareSdnrActivationRequest")
		String operationType = execution.getVariable("operationType")
		String action = operationType.equalsIgnoreCase(ACTIVATE) ? "activate":"deactivate"

		String anNfNssiId = execution.getVariable("anNfNssiId")
		String sNssai = execution.getVariable("sNssaiList")
		String reqId = execution.getVariable("msoRequestId")
		String messageType = "SDNRActivateResponse"
		StringBuilder callbackURL = new StringBuilder(UrnPropertiesReader.getVariable("mso.workflow.message.endpoint", execution))
		callbackURL.append("/").append(messageType).append("/").append(reqId)

		JsonObject input = new JsonObject()
		input.addProperty("RANNFNSSIId", anNfNssiId)
		input.addProperty("callbackURL", callbackURL.toString())
		input.addProperty("s-NSSAI", sNssai)

		JsonObject Payload = new JsonObject()
		Payload.addProperty("version", "1.0")
		Payload.addProperty("rpc-name", "activateRANSlice")
		Payload.addProperty("correlation-id", reqId)
		Payload.addProperty("type", "request")

		JsonObject wrapinput = new JsonObject()
		wrapinput.addProperty("Action", action)

		JsonObject CommonHeader = new JsonObject()
		CommonHeader.addProperty("TimeStamp", new Date(System.currentTimeMillis()).format("yyyy-MM-ddTHH:mm:ss.sss", TimeZone.getDefault()))
		CommonHeader.addProperty("APIver", "1.0")
		CommonHeader.addProperty("RequestID", reqId)
		CommonHeader.addProperty("SubRequestID", "1")

		JsonObject body = new JsonObject()
		body.add("input", wrapinput)

		JsonObject sdnrRequest = new JsonObject()
		Payload.add("input", input)
		wrapinput.add("Payload", Payload)
		wrapinput.add("CommonHeader", CommonHeader)
		body.add("input", wrapinput)
		sdnrRequest.add("body", body)

		String json = sdnrRequest.toString()
		execution.setVariable("sdnrRequest", sdnrRequest)
		execution.setVariable("SDNR_messageType", messageType)
		execution.setVariable("SDNR_timeout", "PT10M")

		logger.debug("${Prefix} -  prepareSdnrActivationRequest : SDNR Request : ${json}")
	}

	void processSdnrResponse(DelegateExecution execution) {
		logger.debug("${Prefix} processing SdnrResponse")
		Map<String, Object> resMap = objectMapper.readValue(execution.getVariable("SDNR_Response"),Map.class)
		String status = resMap.get("status")
		String reason = resMap.get("reason")
		if("success".equalsIgnoreCase(status)) {
			execution.setVariable("isANactivationSuccess", true)
		}else {
			execution.setVariable("isANactivationSuccess", false)
			logger.debug("AN NF Activation/Deactivation failed with reason ${reason}")
		}
		logger.debug("${Prefix} processed SdnrResponse")
	}
	
	/**
	 * Update AN NF - NSSI and SP Instance status
	 * @param execution
	 */
	void updateAnNfStatus(DelegateExecution execution) {
		logger.debug("${Prefix}Start updateAnNfStatus")
		String anNfNssiId = execution.getVariable("anNfNssiId")
		String anNfSPId =  execution.getVariable("anNfSPId")

		updateOrchStatus(execution, anNfSPId)
		updateOrchStatus(execution, anNfNssiId)
		logger.debug("${Prefix}Exit  updateAnNfStatus")
	}
	
	/**
	 * Method to check AN NF's  Slice profile instance orchestration status
	 * @param execution
	 */
	void getTnFhSPOrchStatus(DelegateExecution execution) {
		logger.debug("${Prefix} start getTnFhSPOrchStatus ")
		ServiceInstance sliceProfileInstance = getInstanceByWorkloadContext(execution.getVariable("relatedSPs"), TN_FH)
		String tnFhNssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), TN_FH)
		execution.setVariable("tnFhNssiId", tnFhNssiId)
		String tnFhSPId = sliceProfileInstance.getServiceInstanceId()
		execution.setVariable("tnFhSPId", tnFhSPId)

		String orchStatus = sliceProfileInstance.getOrchestrationStatus()
		String operationType = execution.getVariable("operationType")
		if(orchStatusMap.get(operationType).equalsIgnoreCase(orchStatus)) {
			execution.setVariable("shouldChangeTN_FH_SPStatus", true)
		}else {
			execution.setVariable("shouldChangeTN_FH_SPStatus", false)
		}

		logger.debug("${Prefix} Exit getTnFhSPOrchStatus TN_FH SP ID:${tnFhSPId}  : ${orchStatus}")
	}
	
	void doTnFhNssiActivation(DelegateExecution execution){
		logger.debug("Start doTnFhNssiActivation in ${Prefix}")
		String nssmfRequest = buildTNActivateNssiRequest(execution, TN_FH)
		String operationType = execution.getVariable("operationType")
		String urlOpType = operationType.equalsIgnoreCase(ACTIVATE) ? "activation":"deactivation"

		List<String> sNssaiList =  execution.getVariable("sNssaiList")
		String snssai = sNssaiList != null ? sNssaiList.get(0) : ""

		String urlString = "/api/rest/provMns/v1/NSS/" + snssai + urlOpType
				String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)
				if (nssmfResponse != null) {
					String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
					execution.setVariable("TN_FH_jobId",jobId)
				} else {
					logger.error("received error message from NSSMF : "+ nssmfResponse)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
				}
		logger.debug("Exit doTnFhNssiActivation in ${Prefix}")
	}

	void getTnMhSPOrchStatus(DelegateExecution execution) {
		logger.debug("${Prefix} Start getTnMhSPOrchStatus ")
		ServiceInstance sliceProfileInstance = getInstanceByWorkloadContext(execution.getVariable("relatedSPs"), TN_MH)
		String tnFhNssiId = getInstanceIdByWorkloadContext(execution.getVariable("relatedNssis"), TN_MH)
		execution.setVariable("tnMhNssiId", tnFhNssiId)
		String tnFhSPId = sliceProfileInstance.getServiceInstanceId()
		execution.setVariable("tnMhSPId", tnFhSPId)

		String orchStatus = sliceProfileInstance.getOrchestrationStatus()
		String operationType = execution.getVariable("operationType")
		if(orchStatusMap.get(operationType).equalsIgnoreCase(orchStatus)) {
			execution.setVariable("shouldChangeTN_MH_SPStatus", true)
		}else {
			execution.setVariable("shouldChangeTN_MH_SPStatus", false)
		}
			logger.debug("${Prefix} Exit getTnMhSPOrchStatus TN_MH SP ID:${tnFhSPId}  : ${orchStatus}")
	}
	
	void doTnMhNssiActivation(DelegateExecution execution){
		logger.debug("Start doTnMhNssiActivation in ${Prefix}")
		String nssmfRequest = buildTNActivateNssiRequest(execution, TN_MH)
		String operationType = execution.getVariable("operationType")
		String urlOpType = operationType.equalsIgnoreCase(ACTIVATE) ? "activation":"deactivation"

		List<String> sNssaiList =  execution.getVariable("sNssaiList")
		String snssai = sNssaiList != null ? sNssaiList.get(0) : ""

		String urlString = "/api/rest/provMns/v1/NSS/" + snssai + urlOpType
				String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)
				if (nssmfResponse != null) {
					String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
					execution.setVariable("TN_MH_jobId",jobId)
				} else {
					logger.error("received error message from NSSMF : "+ nssmfResponse)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
				}
				logger.debug("Exit doTnMhNssiActivation in ${Prefix}")
		
	}
	
	/**
	 * Update TN FH - NSSI and SP Instance status
	 * @param execution
	 */
	void updateTNFHStatus(DelegateExecution execution) {
		logger.debug("${Prefix} Start updateTNFHStatus")

		String tnFhNssiId = execution.getVariable("tnFhNssiId")
		String tnFhSPId =  execution.getVariable("tnFhSPId")
		updateOrchStatus(execution, tnFhSPId)
		updateOrchStatus(execution, tnFhNssiId)

		logger.debug("${Prefix} Exit updateTNFHStatus")
		
	}
	
	/**
	 * Update TN MH - NSSI and SP Instance status
	 * @param execution
	 */
	void updateTNMHStatus(DelegateExecution execution) {
		logger.debug("${Prefix} Start updateTNMHStatus")

		String tnMhNssiId = execution.getVariable("tnMhNssiId")
		String tnMhSPId =  execution.getVariable("tnMhSPId")
		updateOrchStatus(execution, tnMhSPId)
		updateOrchStatus(execution, tnMhNssiId)

		logger.debug("${Prefix} Exit updateTNMHStatus")
	}
	
	/**
	 * Update AN - NSSI and SP Instance status
	 * @param execution
	 */
	void updateANStatus(DelegateExecution execution) {
		logger.debug("${Prefix} Start updateANStatus")
		String anNssiId = execution.getVariable("anNssiId")
		String anSliceProfileId =  execution.getVariable("anSliceProfileId")
		updateOrchStatus(execution, anNssiId)
		updateOrchStatus(execution, anSliceProfileId)
		logger.debug("${Prefix} Start updateANStatus")
	}
	
	void prepareQueryJobStatus(DelegateExecution execution,String jobId,String networkType,String instanceId) {
		logger.debug("${Prefix} Start prepareQueryJobStatus : ${jobId}")
		String responseId = "1"
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

		EsrInfo esrInfo = new EsrInfo()
		esrInfo.setNetworkType(networkType)
		esrInfo.setVendor(VENDOR_ONAP)

		ServiceInfo serviceInfo = new ServiceInfo()
		serviceInfo.setNssiId(instanceId)
		serviceInfo.setNsiId(execution.getVariable("nsiId"))
		serviceInfo.setGlobalSubscriberId(globalSubscriberId)
		serviceInfo.setSubscriptionServiceType(subscriptionServiceType)

		execution.setVariable("${networkType}_esrInfo", esrInfo)
		execution.setVariable("${networkType}_responseId", responseId)
		execution.setVariable("${networkType}_serviceInfo", serviceInfo)
		
	}
	
	void validateJobStatus(DelegateExecution execution,String responseDescriptor) {
		logger.debug("validateJobStatus ${responseDescriptor}")
		String status = jsonUtil.getJsonValue(responseDescriptor, "responseDescriptor.status")
		String statusDescription = jsonUtil.getJsonValue(responseDescriptor, "responseDescriptor.statusDescription")
		if("finished".equalsIgnoreCase(status)) {
			execution.setVariable("isSuccess", true)
		}else {
			execution.setVariable("isSuccess", false)
		}
	}
	
	
	private void updateOrchStatus(DelegateExecution execution,String serviceId) {
		logger.debug("${Prefix} Start updateOrchStatus : ${serviceId}")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		String operationType = execution.getVariable("operationType")

		try {
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
					globalSubscriberId, subscriptionServiceType, serviceId)
			if (!client.exists(uri)) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
			}
			AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
			Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
			if (si.isPresent()) {
				String orchStatus = si.get().getOrchestrationStatus()
				logger.debug("Orchestration status of instance ${serviceId} is ${orchStatus}")
				if (ACTIVATE.equalsIgnoreCase(operationType) && "deactivated".equalsIgnoreCase(orchStatus)) {
						si.get().setOrchestrationStatus("activated")
						client.update(uri, si.get())
				} else if(DEACTIVATE.equalsIgnoreCase(operationType) && "activated".equalsIgnoreCase(orchStatus)){
						si.get().setOrchestrationStatus("deactivated")
						client.update(uri, si.get())
				}
			}
		} catch (Exception e) {
			logger.info("Service is already in active state")
			String msg = "Service is already in active state, " + e.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug("${Prefix} Exit updateOrchStatus : ${serviceId}")
	}
	
	void prepareUpdateJobStatus(DelegateExecution execution,String status,String progress,String statusDescription) {
		logger.debug("${Prefix} Start prepareUpdateJobStatus : ${statusDescription}")
		String serviceId = execution.getVariable("anNssiId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String operationType = execution.getVariable("operationType")

		ResourceOperationStatus roStatus = new ResourceOperationStatus()
		roStatus.setServiceId(serviceId)
		roStatus.setOperationId(jobId)
		roStatus.setResourceTemplateUUID(nsiId)
		roStatus.setOperType(operationType)
		roStatus.setProgress(progress)
		roStatus.setStatus(status)
		roStatus.setStatusDescription(statusDescription)
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, status)
	}
	
	
	
	/**
	 * Fetches a collection of service instances with the specific role and maps it based on workload context
	 * (AN-NF,TN-FH,TN-MH)
	 * @param execution
	 * @param role			- nssi/slice profile instance
	 * @param key 			- NSSI/Sliceprofile corresponding to instanceId
	 * @param instanceId	- id to which the related list to be found
	 * @return
	 */
	private Map<String,ServiceInstance> getRelatedInstancesByRole(DelegateExecution execution,String role,String key, String instanceId) {
		logger.debug("${Prefix} - Fetching related ${role} from AAI")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		
		if( isBlank(role) || isBlank(instanceId)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Role and instanceId are mandatory")
		}

		Map<String,ServiceInstance> relatedInstances = new HashMap<>()
		
		AAIResourcesClient client = getAAIClient()
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
				globalSubscriberId, subscriptionServiceType, instanceId)
		if (!client.exists(uri)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai : ${instanceId}")
		}
		AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
		Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
		if(si.isPresent()) {
		execution.setVariable(key, si.get())
		List<Relationship> relationshipList = si.get().getRelationshipList().getRelationship()
		for (Relationship relationship : relationshipList) {
			String relatedTo = relationship.getRelatedTo()
			if (relatedTo.toLowerCase() == "service-instance") {
				String relatioshipurl = relationship.getRelatedLink()
				String serviceInstanceId =
						relatioshipurl.substring(relatioshipurl.lastIndexOf("/") + 1, relatioshipurl.length())
				uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
						globalSubscriberId, subscriptionServiceType, serviceInstanceId)
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
	
	private ServiceInstance getInstanceByWorkloadContext(Map<String,ServiceInstance> instances,String workloadContext ) {
		ServiceInstance instance = instances.get(workloadContext)
		if(instance == null) {
			throw new BpmnError( 2500, "${workloadContext} Instance ID is not found.")
		}
		return instance
	}
	
	private String getInstanceIdByWorkloadContext(Map<String,ServiceInstance> instances,String workloadContext ) {
		String instanceId = instances.get(workloadContext).getServiceInstanceId()
		if(instanceId == null) {
			throw new BpmnError( 2500, "${workloadContext} instance ID is not found.")
		}
		return instanceId
	}
	
	
	/**
	 * Method to handle deallocation of RAN NSSI constituents(TN_FH/TN_MH)
	 * @param execution
	 * @param serviceFunction - TN_FH/TN_MH
	 * @return
	 */
	private String buildTNActivateNssiRequest(DelegateExecution execution,String serviceFunction) {
		logger.debug("${Prefix} Exit buildTNActivateNssiRequest : ${serviceFunction}")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		Map<String, ServiceInstance> relatedNssis = execution.getVariable("relatedNssis")

		String anNssiId = execution.getVariable("anNssiId")
		List<String> sNssaiList =  execution.getVariable("sNssaiList")

		ServiceInstance tnNssi = relatedNssis.get(serviceFunction)
		String nssiId = tnNssi.getServiceInstanceId()

		Map<String, ServiceInstance> relatedSPs = execution.getVariable("relatedSPs")

		ActDeActNssi actDeactNssi = new ActDeActNssi()
		actDeactNssi.setNssiId(nssiId)
		actDeactNssi.setNsiId(anNssiId)
		actDeactNssi.setSliceProfileId(relatedSPs.get(serviceFunction).getServiceInstanceId())
		actDeactNssi.setSnssaiList(sNssaiList)

		EsrInfo esrInfo = new EsrInfo()
		esrInfo.setVendor(VENDOR_ONAP)
		esrInfo.setNetworkType("TN")

		ServiceInfo serviceInfo = new ServiceInfo()
		serviceInfo.setServiceInvariantUuid(tnNssi.getModelInvariantId())
		serviceInfo.setServiceUuid(tnNssi.getModelVersionId())
		serviceInfo.setGlobalSubscriberId(globalSubscriberId)
		serviceInfo.setSubscriptionServiceType(subscriptionServiceType)

		JsonObject json = new JsonObject()
		json.addProperty("actDeActNssi", objectMapper.writeValueAsString(actDeactNssi))
		json.addProperty("esrInfo", objectMapper.writeValueAsString(esrInfo))
		json.addProperty("serviceInfo", objectMapper.writeValueAsString(serviceInfo))
		return json.toString()
		
	}
	
}
