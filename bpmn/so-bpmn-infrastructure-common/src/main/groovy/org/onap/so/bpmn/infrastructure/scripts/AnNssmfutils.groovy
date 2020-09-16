/*
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, Wipro Limited.
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

import com.google.gson.JsonArray
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.util.List
import static org.apache.commons.lang3.StringUtils.isBlank
import com.google.gson.JsonObject
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import javax.ws.rs.NotFoundException
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.DeAllocateNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.SliceProfiles
import org.onap.aai.domain.yang.Relationship

class AnNssmfUtils {

	private static final Logger logger = LoggerFactory.getLogger(AnNssmfUtils.class)
	ObjectMapper objectMapper = new ObjectMapper();
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	public String buildSelectRANNSSIRequest(String requestId, String messageType, String UUID,String invariantUUID,
		String name, Map<String, Object> profileInfo, List<String> nsstInfoList, JsonArray capabilitiesList, Boolean preferReuse){

	def transactionId = requestId
	logger.debug( "transactionId is: " + transactionId)
	String correlator = requestId
	String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
	ObjectMapper objectMapper = new ObjectMapper();
	String profileJson = objectMapper.writeValueAsString(profileInfo);
	String nsstInfoListString = objectMapper.writeValueAsString(nsstInfoList);
	//Prepare requestInfo object
	JsonObject requestInfo = new JsonObject()
	requestInfo.addProperty("transactionId", transactionId)
	requestInfo.addProperty("requestId", requestId)
	requestInfo.addProperty("callbackUrl", callbackUrl)
	requestInfo.addProperty("sourceId","SO" )
	requestInfo.addProperty("timeout", 600)
	requestInfo.addProperty("numSolutions", 1)

	//Prepare serviceInfo object
	JsonObject ranNsstInfo = new JsonObject()
	ranNsstInfo.addProperty("UUID", UUID)
	ranNsstInfo.addProperty("invariantUUID", invariantUUID)
	ranNsstInfo.addProperty("name", name)

	JsonObject json = new JsonObject()
	json.add("requestInfo", requestInfo)
	json.add("NSTInfo", ranNsstInfo)
	json.addProperty("serviceProfile", profileJson)
	json.addProperty("NSSTInfo", nsstInfoListString)
	json.add("subnetCapabilities", capabilitiesList)
	json.addProperty("preferReuse", preferReuse)

	return json.toString()
}

public String buildCreateTNNSSMFSubnetCapabilityRequest() {
	EsrInfo esrInfo = new EsrInfo()
	esrInfo.setNetworkType("TN")
	esrInfo.setVendor("ONAP")

	JsonArray subnetTypes = new JsonArray()
	subnetTypes.add("TN_FH")
	subnetTypes.add("TN_MH")
	JsonObject response = new JsonObject()
	response.add("subnetCapabilityQuery", subnetTypes)
	response.addProperty("esrInfo", objectMapper.writeValueAsString(esrInfo))
	return response.toString()
}

public String buildCreateANNFNSSMFSubnetCapabilityRequest() {
	EsrInfo esrInfo = new EsrInfo()
	esrInfo.setNetworkType("AN")
	esrInfo.setVendor("ONAP")

	JsonArray subnetTypes = new JsonArray()
	subnetTypes.add("AN_NF")
	JsonObject response = new JsonObject()
	response.add("subnetCapabilityQuery", subnetTypes)
	response.addProperty("esrInfo", objectMapper.writeValueAsString(esrInfo))
	return response.toString()
}
public void createDomainWiseSliceProfiles(List<String> ranConstituentSliceProfiles, DelegateExecution execution) {
	
	for(String profile : ranConstituentSliceProfiles) {
		String domainType = jsonUtil.getJsonValue(profile, "domainType")
		switch(domainType) {
			case "AN_NF":
				execution.setVariable("ranNfSliceProfile", profile)
				break
			case "TN_FH":
				execution.setVariable("tnFhSliceProfile", profile)
				break
			case "TN_MH":
				execution.setVariable("tnMhSliceProfile", profile)
				break
			default:
				logger.debug("No expected match found for current domainType")
				logger.error("No expected match found for current domainType "+ domainType)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1000,"No expected match found for current domainType "+ domainType)
		}
		
	}
}

public void createSliceProfilesInAai(DelegateExecution execution) {
	
	org.onap.aai.domain.yang.ServiceInstance ANNF_sliceProfileInstance = new ServiceInstance();
	org.onap.aai.domain.yang.ServiceInstance TNFH_sliceProfileInstance = new ServiceInstance();
	org.onap.aai.domain.yang.ServiceInstance TNMH_sliceProfileInstance = new ServiceInstance();
	//generate slice profile ids and slice profile instance ids
	String ANNF_sliceProfileInstanceId = UUID.randomUUID().toString()
	String ANNF_sliceProfileId = UUID.randomUUID().toString()
	String TNFH_sliceProfileInstanceId = UUID.randomUUID().toString()
	String TNFH_sliceProfileId = UUID.randomUUID().toString()
	String TNMH_sliceProfileInstanceId = UUID.randomUUID().toString()
	String TNMH_sliceProfileId = UUID.randomUUID().toString()
	execution.setVariable("ANNF_sliceProfileInstanceId",ANNF_sliceProfileInstanceId)
	execution.setVariable("ANNF_sliceProfileId",ANNF_sliceProfileId)
	execution.setVariable("TNFH_sliceProfileInstanceId",TNFH_sliceProfileInstanceId)
	execution.setVariable("TNFH_sliceProfileId",TNFH_sliceProfileId)
	execution.setVariable("TNMH_sliceProfileInstanceId",TNMH_sliceProfileInstanceId)
	execution.setVariable("TNMH_sliceProfileId",TNMH_sliceProfileId)
	//slice profiles assignment
	org.onap.aai.domain.yang.SliceProfiles ANNF_SliceProfiles = new SliceProfiles()
	org.onap.aai.domain.yang.SliceProfiles TNFH_SliceProfiles = new SliceProfiles()
	org.onap.aai.domain.yang.SliceProfiles TNMH_SliceProfiles = new SliceProfiles()
	org.onap.aai.domain.yang.SliceProfile ANNF_SliceProfile = new SliceProfile()
	org.onap.aai.domain.yang.SliceProfile TNFH_SliceProfile = new SliceProfile()
	org.onap.aai.domain.yang.SliceProfile TNMH_SliceProfile = new SliceProfile()
	ANNF_SliceProfile = createSliceProfile("AN-NF", execution)
	TNFH_SliceProfile = createSliceProfile("TN-FH",execution)
	TNMH_SliceProfile = createSliceProfile("TN-MH",execution)
	
	ANNF_SliceProfiles.getSliceProfile().add(ANNF_SliceProfile)
	TNFH_SliceProfiles.getSliceProfile().add(TNFH_SliceProfile)
	TNMH_SliceProfiles.getSliceProfile().add(TNMH_SliceProfile)
	
	logger.debug("sliceProfiles : 1. "+ANNF_SliceProfiles.toString()+"\n 2. "+TNFH_SliceProfiles.toString()+"\n 3. "+TNMH_SliceProfiles.toString())
	//ANNF slice profile instance creation
	ANNF_sliceProfileInstance.setServiceInstanceId(ANNF_sliceProfileInstanceId)
	String sliceInstanceName = "sliceprofile_"+ANNF_sliceProfileId
	ANNF_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
	String serviceType = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile"), "sST")
	ANNF_sliceProfileInstance.setServiceType(serviceType)
	String serviceStatus = "deactivated"
	ANNF_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
	String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile"), "plmnIdList")
	ANNF_sliceProfileInstance.setServiceInstanceLocationId(serviceInstanceLocationid)
	String serviceRole = "slice-profile-instance"
	ANNF_sliceProfileInstance.setServiceRole(serviceRole)
	List<String> snssaiList = objectMapper.readValue(execution.getVariable("snssaiList"), List.class)
	String snssai = snssaiList.get(0)
	ANNF_sliceProfileInstance.setEnvironmentContext(snssai)
	ANNF_sliceProfileInstance.setWorkloadContext("AN-NF")	 
	ANNF_sliceProfileInstance.setSliceProfiles(ANNF_SliceProfiles)
	logger.debug("completed ANNF sliceprofileinstance build "+ ANNF_sliceProfileInstance.toString())
	//TNFH slice profile instance creation
	TNFH_sliceProfileInstance.setServiceInstanceId(TNFH_sliceProfileInstanceId)
	sliceInstanceName = "sliceprofile_"+TNFH_sliceProfileId
	TNFH_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
	serviceType = jsonUtil.getJsonValue(execution.getVariable("tnFhSliceProfile"), "sST")
	TNFH_sliceProfileInstance.setServiceType(serviceType)
	TNFH_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
	serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("tnFhSliceProfile"), "plmnIdList")
	TNFH_sliceProfileInstance.setServiceInstanceLocationId(serviceInstanceLocationid)
	TNFH_sliceProfileInstance.setServiceRole(serviceRole)
	TNFH_sliceProfileInstance.setEnvironmentContext(snssai)
	TNFH_sliceProfileInstance.setWorkloadContext("TN-FH")
	TNFH_sliceProfileInstance.setSliceProfiles(TNFH_SliceProfiles)
	logger.debug("completed TNFH sliceprofileinstance build "+TNFH_sliceProfileInstance)
	//TNMH slice profile instance creation
	TNMH_sliceProfileInstance.setServiceInstanceId(TNMH_sliceProfileInstanceId)
	sliceInstanceName = "sliceprofile_"+TNMH_sliceProfileId
	TNMH_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
	serviceType = jsonUtil.getJsonValue(execution.getVariable("tnMhSliceProfile"), "sST")
	TNMH_sliceProfileInstance.setServiceType(serviceType)
	TNMH_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
	serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("tnMhSliceProfile"), "plmnIdList")
	TNMH_sliceProfileInstance.setServiceInstanceLocationId(serviceInstanceLocationid)
	TNMH_sliceProfileInstance.setServiceRole(serviceRole)
	TNMH_sliceProfileInstance.setEnvironmentContext(snssai)
	TNMH_sliceProfileInstance.setWorkloadContext("TN-MH")
	TNMH_sliceProfileInstance.setSliceProfiles(TNMH_SliceProfiles)
	logger.debug("completed TNMH sliceprofileinstance build "+TNMH_sliceProfileInstance)
	String msg = ""
	try {

		AAIResourcesClient client = new AAIResourcesClient()
		AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), ANNF_sliceProfileInstanceId)
		client.create(sliceProfileUri, ANNF_sliceProfileInstance)

		AAIResourceUri sliceProfileUri1 = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), TNFH_sliceProfileInstanceId)
		client.create(sliceProfileUri1, TNFH_sliceProfileInstance)

		AAIResourceUri sliceProfileUri2 = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), TNMH_sliceProfileInstanceId)
		client.create(sliceProfileUri2, TNMH_sliceProfileInstance)

	} catch (BpmnError e) {
		throw e
	} catch (Exception ex) {
		msg = "Exception in AnNssmfUtils.createSliceProfilesInAai " + ex.getMessage()
		logger.info(msg)
		exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
	}

}
private SliceProfile createSliceProfile(String domainType, DelegateExecution execution) {

	SliceProfile result = new SliceProfile()
	Map<String,Object> profile
	switch(domainType) {
		case "AN-NF":
			profile = objectMapper.readValue(execution.getVariable("ranNfSliceProfile"), Map.class)//pending fields - maxBandwidth, sST, pLMNIdList, cSReliabilityMeanTime, 
																									//msgSizeByte, maxNumberofPDUSessions,overallUserDensity,transferIntervalTarget
			result.setJitter(profile.get("jitter"))
			result.setLatency(profile.get("latency"))
			result.setResourceSharingLevel(profile.get("resourceSharingLevel"))
			result.setSNssai(profile.get("sNSSAI"))
			result.setUeMobilityLevel(profile.get("uEMobilityLevel"))
			result.setMaxNumberOfUEs(profile.get("maxNumberofUEs"))
			result.setActivityFactor(profile.get("activityFactor"))
			result.setCoverageAreaTAList(profile.get("coverageAreaTAList"))
			result.setCsAvailability(profile.get("cSAvailabilityTarget"))
			result.setExpDataRateDL(profile.get("expDataRateDL"))
			result.setExpDataRateUL(profile.get("expDataRateUL"))
			result.setSurvivalTime(profile.get("survivalTime"))
			result.setAreaTrafficCapDL(profile.get("areaTrafficCapDL"))
			result.setAreaTrafficCapUL(profile.get("areaTrafficCapUL"))
			result.setExpDataRate(profile.get("expDataRate"))
			result.setProfileId(execution.getVariable("ANNF_sliceProfileId"))
			break
		case "TN-FH":
			profile = objectMapper.readValue(execution.getVariable("tnFhSliceProfile"), Map.class) //pending fields - maxBandwidth, sST, pLMNIdList
			result.setJitter(profile.get("jitter"))
			result.setLatency(profile.get("latency"))
			result.setResourceSharingLevel(profile.get("resourceSharingLevel"))
			result.setSNssai(profile.get("sNSSAI"))
			result.setProfileId(execution.getVariable("TNFH_sliceProfileId"))
			break
		case "TN-MH":
			profile = objectMapper.readValue(execution.getVariable("tnMhSliceProfile"), Map.class)//pending fields - maxBandwidth, sST, pLMNIdList
			result.setJitter(profile.get("jitter"))
			result.setLatency(profile.get("latency"))
			result.setResourceSharingLevel(profile.get("resourceSharingLevel"))
			result.setSNssai(profile.get("sNSSAI"))
			result.setProfileId(execution.getVariable("TNMH_sliceProfileId"))
			break
		default:
			logger.debug("No expected match found for current domainType")
			logger.error("No expected match found for current domainType "+ domainType)
			exceptionUtil.buildAndThrowWorkflowException(execution, 1000,"No expected match found for current domainType "+ domainType)
	}
	return result
}

 /**
     * create relationship in AAI
     */
    public createRelationShipInAAI = { DelegateExecution execution, final Relationship relationship, String instanceId ->
        logger.debug("createRelationShipInAAI Start")
        String msg
		AAIResourcesClient client = new AAIResourcesClient()
        try {
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,
                    execution.getVariable("globalSubscriberId"),
                    execution.getVariable("subscriptionServiceType"),
                    instanceId).relationshipAPI()
            client.create(uri, relationship)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in CreateCommunicationService.createRelationShipInAAI. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("createRelationShipInAAI Exit")

    }
	
	public void processRanNfModifyRsp(DelegateExecution execution) {
		String status = execution.getVariable("ranNfStatus")
		if(status.equals("success")) {
			logger.debug("completed Ran NF NSSI modification ... proceeding with the flow")
		}
		else {
			logger.error("failed to modify ran Nf nssi")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1000, "modify ran nf nssi not successfull")
		}
	}
	
	public String buildCreateNSSMFRequest(DelegateExecution execution, String domainType, String action) {
		EsrInfo esrInfo = new EsrInfo()
		esrInfo.setNetworkType("TN")
		esrInfo.setVendor("ONAP")
		String esrInfoString = objectMapper.writeValueAsString(esrInfo)
		JsonObject response = new JsonObject()
		JsonObject allocateTnNssi = new JsonObject()
		JsonObject serviceInfo = new JsonObject()
		JsonArray transportSliceNetworksList  = new JsonArray()
		JsonArray connectionLinksList = new JsonArray()
		JsonObject connectionLinks = new JsonObject()
		if(action.equals("allocate")){
			Map<String, String> endpoints
			if(domainType.equals("TN_FH")) {
				serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNFH_modelInvariantUuid"))
				serviceInfo.addProperty("serviceUuid", execution.getVariable("TNFH_modelUuid"))
				allocateTnNssi.addProperty("nsstId", execution.getVariable("TNFH_modelUuid"))
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNFH_modelName"))
				Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("tnFhSliceProfile"), Map.class)
				sliceProfile.put("sliceProfileId", execution.getVariable("TNFH_sliceProfileInstanceId"))
				String sliceProfileString = objectMapper.writeValueAsString(sliceProfile)
				allocateTnNssi.addProperty("sliceProfile", sliceProfileString)
				endpoints.put("transportEndpointA", execution.getVariable("tranportEp_ID_RU"))
				endpoints.put("transportEndpointB", execution.getVariable("tranportEp_ID_DUIN"))
				String endpointsString = objectMapper.writeValueAsString(endpoints)
				connectionLinksList.add(endpointsString)
			}else if(domainType.equals("TN_MH")) {
				serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNMH_modelInvariantUuid"))
				serviceInfo.addProperty("serviceUuid", execution.getVariable("TNMH_modelUuid"))
				allocateTnNssi.addProperty("nsstId", execution.getVariable("TNMH_modelUuid"))
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNMH_modelName"))
				Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("tnMhSliceProfile"), Map.class)
				sliceProfile.put("sliceProfileId", execution.getVariable("TNMH_sliceProfileInstanceId"))
				String sliceProfileString = objectMapper.writeValueAsString(sliceProfile)
				allocateTnNssi.addProperty("sliceProfile", sliceProfileString)
				endpoints.put("transportEndpointA", execution.getVariable("tranportEp_ID_DUEG"))
				endpoints.put("transportEndpointB", execution.getVariable("tranportEp_ID_CUIN"))
				String endpointsString = objectMapper.writeValueAsString(endpoints)
				connectionLinksList.add(endpointsString)
			}
			
			//Connection links
			connectionLinks.add("connectionLinks", connectionLinksList)
			transportSliceNetworksList.add(connectionLinks)
			allocateTnNssi.add("transportSliceNetworks", transportSliceNetworksList)
			allocateTnNssi.addProperty("nssiId", null)
			serviceInfo.addProperty("nssiId", null)
		}else if(action.equals("modify-allocate")) {
			if(domainType.equals("TN_FH")) {
				serviceInfo.addProperty("serviceInvariantUuid", null)
				serviceInfo.addProperty("serviceUuid", null)
				allocateTnNssi.addProperty("nsstId", null)
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNFH_nssiName"))
				allocateTnNssi.addProperty("sliceProfileId", execution.getVariable("TNFH_sliceProfileInstanceId"))
				allocateTnNssi.addProperty("nssiId", execution.getVariable("TNFH_NSSI"))
				serviceInfo.addProperty("nssiId", execution.getVariable("TNFH_NSSI"))
			}else if(domainType.equals("TN_MH")) {
				serviceInfo.addProperty("serviceInvariantUuid", null)
				serviceInfo.addProperty("serviceUuid", null)
				allocateTnNssi.addProperty("nsstId", null)
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNMH_nssiName"))
				allocateTnNssi.addProperty("sliceProfileId", execution.getVariable("TNMH_sliceProfileInstanceId"))
				allocateTnNssi.addProperty("nssiId", execution.getVariable("TNMH_NSSI"))
				serviceInfo.addProperty("nssiId", execution.getVariable("TNMH_NSSI"))
			}
		}
		String nsiInfo = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "nsiInfo")
		allocateTnNssi.addProperty("nsiInfo", nsiInfo)
		allocateTnNssi.addProperty("scriptName", "TN1")
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
		serviceInfo.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId"))
		serviceInfo.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
		response.addProperty("esrInfo", esrInfoString)
		response.add("serviceInfo", serviceInfo)
		response.add("allocateTnNssi", allocateTnNssi)
		return response.toString()
	}
	
	public String buildDeallocateNssiRequest(DelegateExecution execution,String domainType) {
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
	   
		JsonObject deAllocateNssi = new JsonObject()
		deAllocateNssi.addProperty("snssaiList", execution.getVariable("snssaiList"))
		deAllocateNssi.addProperty("nsiId", execution.getVariable("nsiId"))
		deAllocateNssi.addProperty("modifyAction", true)
		deAllocateNssi.addProperty("terminateNssiOption", 0)
		deAllocateNssi.addProperty("scriptName", "TN1")
		
		if(domainType.equals("TN_FH")) {
			deAllocateNssi.addProperty("nssiId", execution.getVariable("TNFH_NSSI"))
			deAllocateNssi.addProperty("sliceProfileId", execution.getVariable("TNFH_sliceProfileInstanceId"))
		}else if(domainType.equals("TN_MH")) {
			deAllocateNssi.addProperty("nssiId", execution.getVariable("TNMH_NSSI"))
			deAllocateNssi.addProperty("sliceProfileId", execution.getVariable("TNMH_sliceProfileInstanceId"))
		}
		
		EsrInfo esrInfo = new EsrInfo()
		esrInfo.setVendor("ONAP")
		esrInfo.setNetworkType("TN")
	   
		JsonObject serviceInfo = new JsonObject()
		serviceInfo.addProperty("serviceInvariantUuid", null)
		serviceInfo.addProperty("serviceUuid", null)
		serviceInfo.addProperty("globalSubscriberId", globalSubscriberId)
		serviceInfo.addProperty("subscriptionServiceType", subscriptionServiceType)
	   
		JsonObject json = new JsonObject()
		json.add("deAllocateNssi", deAllocateNssi)
		json.addProperty("esrInfo", objectMapper.writeValueAsString(esrInfo))
		json.add("serviceInfo", serviceInfo)
		return json.toString()
	   
	}
}