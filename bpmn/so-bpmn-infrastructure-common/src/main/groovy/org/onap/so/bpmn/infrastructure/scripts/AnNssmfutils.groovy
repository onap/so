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
import com.google.gson.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import javax.ws.rs.NotFoundException
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.DeAllocateNssi
import org.onap.so.beans.nsmf.ServiceInfo
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.SliceProfiles
import org.onap.aai.domain.yang.Relationship
import com.google.gson.Gson

class AnNssmfUtils {

	private static final Logger logger = LoggerFactory.getLogger(AnNssmfUtils.class)
	private static final ObjectMapper objectMapper = new ObjectMapper();
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	public String buildSelectRANNSSIRequest(String requestId, String messageType, String UUID,String invariantUUID,
		String name, Map<String, Object> profileInfo, List<String> nsstInfoList, JsonArray capabilitiesList, Boolean preferReuse){
	JsonParser parser = new JsonParser()
	def transactionId = requestId
	logger.debug( "transactionId is: " + transactionId)
	String correlator = requestId
	String callbackUrl = UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint") + "/" + messageType + "/" + correlator
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
	json.add("serviceProfile", (JsonObject) parser.parse(profileJson))
	//json.add("NSSTInfo", (JsonArray) parser.parse(nsstInfoListString))
	json.add("subnetCapabilities", capabilitiesList)
	json.addProperty("preferReuse", preferReuse)

	return json.toString()
}

public String buildCreateTNNSSMFSubnetCapabilityRequest() {
	JsonObject esrInfo = new JsonObject()
	esrInfo.addProperty("networkType", "tn")
	esrInfo.addProperty("vendor", "ONAP_internal")

	JsonArray subnetTypes = new JsonArray()
	subnetTypes.add("TN_FH")
	subnetTypes.add("TN_MH")
	JsonObject response = new JsonObject()
	JsonObject subnetTypesObj = new JsonObject()
	subnetTypesObj.add("subnetTypes", subnetTypes)
	response.add("subnetCapabilityQuery", subnetTypesObj)
	response.add("esrInfo", esrInfo)
	return response.toString()
}

public String buildCreateANNFNSSMFSubnetCapabilityRequest() {
	JsonObject esrInfo = new JsonObject()
	esrInfo.addProperty("networkType", "an")
	esrInfo.addProperty("vendor", "ONAP_internal")

	JsonArray subnetTypes = new JsonArray()
	subnetTypes.add("AN_NF")
	JsonObject response = new JsonObject()
	JsonObject subnetTypesObj = new JsonObject()
	subnetTypesObj.add("subnetTypes", subnetTypes)
	response.add("subnetCapabilityQuery", subnetTypesObj)
	response.add("esrInfo", esrInfo)
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

	String serviceCategory = execution.getVariable("serviceCategory")
	if (execution.getVariable("IsRANNfAlonePresent")) {
		ServiceInstance ANNF_sliceProfileInstance = new ServiceInstance();
		String ANNF_sliceProfileInstanceId = UUID.randomUUID().toString()
		String ANNF_sliceProfileId = UUID.randomUUID().toString()
		execution.setVariable("ANNF_sliceProfileInstanceId", ANNF_sliceProfileInstanceId)
		execution.setVariable("ANNF_sliceProfileId", ANNF_sliceProfileId)
		SliceProfiles ANNF_SliceProfiles = new SliceProfiles()
		SliceProfile ANNF_SliceProfile = createSliceProfile("AN_NF", execution)
		ANNF_SliceProfiles.getSliceProfile().add(ANNF_SliceProfile)
		logger.debug("sliceProfiles : 1. " + ANNF_SliceProfiles.toString())
		//ANNF slice profile instance creation
		ANNF_sliceProfileInstance.setServiceInstanceId(ANNF_sliceProfileInstanceId)
		String sliceInstanceName = "sliceprofile_" + ANNF_sliceProfileId
		ANNF_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
		String serviceType = execution.getVariable("sst") as String
		ANNF_sliceProfileInstance.setServiceType(serviceType)
		String serviceStatus = "deactivated"
		ANNF_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
		String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile") as String, "pLMNIdList")
		ANNF_sliceProfileInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
		String serviceRole = "slice-profile-instance"
		ANNF_sliceProfileInstance.setServiceRole(serviceRole)
		ArrayList<String> snssaiList = jsonUtil.StringArrayToList(execution.getVariable("snssaiList") as String)
		String snssai = snssaiList.get(0)
		ANNF_sliceProfileInstance.setEnvironmentContext(snssai)
		ANNF_sliceProfileInstance.setWorkloadContext("AN_NF")
		ANNF_sliceProfileInstance.setSliceProfiles(ANNF_SliceProfiles)
		String serviceFunctionAnnf = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile") as String, "resourceSharingLevel")
		ANNF_sliceProfileInstance.setServiceFunction(serviceFunctionAnnf)
		logger.debug("completed ANNF sliceprofileinstance build : " + ANNF_sliceProfileInstance.toString())
		String msg = ""
		try {

			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId") as String).serviceSubscription(execution.getVariable("subscriptionServiceType") as String).serviceInstance(ANNF_sliceProfileInstanceId))
			client.create(sliceProfileUri, ANNF_sliceProfileInstance)
		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in AnNssmfUtils.createSliceProfilesInAai " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}
else{
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
	execution.setVariable("ANNF_sliceProfileInstanceId", ANNF_sliceProfileInstanceId)
	execution.setVariable("ANNF_sliceProfileId", ANNF_sliceProfileId)
	execution.setVariable("TNFH_sliceProfileInstanceId", TNFH_sliceProfileInstanceId)
	execution.setVariable("TNFH_sliceProfileId", TNFH_sliceProfileId)
	execution.setVariable("TNMH_sliceProfileInstanceId", TNMH_sliceProfileInstanceId)
	execution.setVariable("TNMH_sliceProfileId", TNMH_sliceProfileId)
	//slice profiles assignment
	org.onap.aai.domain.yang.SliceProfiles ANNF_SliceProfiles = new SliceProfiles()
	org.onap.aai.domain.yang.SliceProfiles TNFH_SliceProfiles = new SliceProfiles()
	org.onap.aai.domain.yang.SliceProfiles TNMH_SliceProfiles = new SliceProfiles()
	org.onap.aai.domain.yang.SliceProfile ANNF_SliceProfile = new SliceProfile()
	org.onap.aai.domain.yang.SliceProfile TNFH_SliceProfile = new SliceProfile()
	org.onap.aai.domain.yang.SliceProfile TNMH_SliceProfile = new SliceProfile()
	ANNF_SliceProfile = createSliceProfile("AN_NF", execution)
	TNFH_SliceProfile = createSliceProfile("TN_FH", execution)
	TNMH_SliceProfile = createSliceProfile("TN_MH", execution)

	ANNF_SliceProfiles.getSliceProfile().add(ANNF_SliceProfile)
	TNFH_SliceProfiles.getSliceProfile().add(TNFH_SliceProfile)
	TNMH_SliceProfiles.getSliceProfile().add(TNMH_SliceProfile)

	logger.debug("sliceProfiles : 1. " + ANNF_SliceProfiles.toString() + "\n 2. " + TNFH_SliceProfiles.toString() + "\n 3. " + TNMH_SliceProfiles.toString())
	//ANNF slice profile instance creation
	ANNF_sliceProfileInstance.setServiceInstanceId(ANNF_sliceProfileInstanceId)
	String sliceInstanceName = "sliceprofile_" + ANNF_sliceProfileId
	ANNF_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
	String serviceType = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile"), "sST")
	ANNF_sliceProfileInstance.setServiceType(serviceType)
	String serviceStatus = "deactivated"
	ANNF_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
	String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile"), "pLMNIdList")
        ANNF_sliceProfileInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
	String serviceRole = "slice-profile-instance"
	ANNF_sliceProfileInstance.setServiceRole(serviceRole)
	ArrayList<String> snssaiList = jsonUtil.StringArrayToList(execution.getVariable("snssaiList") as String)
	String snssai = snssaiList.get(0)
	ANNF_sliceProfileInstance.setEnvironmentContext(snssai)
	ANNF_sliceProfileInstance.setWorkloadContext("AN_NF")
	ANNF_sliceProfileInstance.setSliceProfiles(ANNF_SliceProfiles)
	String serviceFunctionAnnf = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile"), "resourceSharingLevel")
	ANNF_sliceProfileInstance.setServiceFunction(serviceFunctionAnnf)
	logger.debug("completed ANNF sliceprofileinstance build :  "+ ANNF_sliceProfileInstance.toString())

	//TNFH slice profile instance creation
	TNFH_sliceProfileInstance.setServiceInstanceId(TNFH_sliceProfileInstanceId)
	sliceInstanceName = "sliceprofile_"+TNFH_sliceProfileId
	TNFH_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
	serviceType = jsonUtil.getJsonValue(execution.getVariable("tnFhSliceProfile"), "sST")
	TNFH_sliceProfileInstance.setServiceType(serviceType)
	TNFH_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
	serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("tnFhSliceProfile"), "pLMNIdList")
        TNFH_sliceProfileInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
	TNFH_sliceProfileInstance.setServiceRole(serviceRole)
	TNFH_sliceProfileInstance.setEnvironmentContext(snssai)
	TNFH_sliceProfileInstance.setWorkloadContext("TN_FH")
	TNFH_sliceProfileInstance.setSliceProfiles(TNFH_SliceProfiles)
	String serviceFunctionTnFH = jsonUtil.getJsonValue(execution.getVariable("tnFhSliceProfile"), "resourceSharingLevel")
	TNFH_sliceProfileInstance.setServiceFunction(serviceFunctionTnFH)
	logger.debug("completed TNFH sliceprofileinstance build :   "+TNFH_sliceProfileInstance)

	//TNMH slice profile instance creation
	TNMH_sliceProfileInstance.setServiceInstanceId(TNMH_sliceProfileInstanceId)
	sliceInstanceName = "sliceprofile_"+TNMH_sliceProfileId
	TNMH_sliceProfileInstance.setServiceInstanceName(sliceInstanceName)
	serviceType = jsonUtil.getJsonValue(execution.getVariable("tnMhSliceProfile"), "sST")
	TNMH_sliceProfileInstance.setServiceType(serviceType)
	TNMH_sliceProfileInstance.setOrchestrationStatus(serviceStatus)
	serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("tnMhSliceProfile"), "pLMNIdList")
        TNMH_sliceProfileInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
	TNMH_sliceProfileInstance.setServiceRole(serviceRole)
	TNMH_sliceProfileInstance.setEnvironmentContext(snssai)
	TNMH_sliceProfileInstance.setWorkloadContext("TN_MH")
	TNMH_sliceProfileInstance.setSliceProfiles(TNMH_SliceProfiles)
	String serviceFunctionTnMH = jsonUtil.getJsonValue(execution.getVariable("tnMhSliceProfile"), "resourceSharingLevel")
	TNMH_sliceProfileInstance.setServiceFunction(serviceFunctionTnMH)
	logger.debug("completed TNMH sliceprofileinstance build :   "+TNMH_sliceProfileInstance)

	String msg = ""
	try {

		AAIResourcesClient client = new AAIResourcesClient()
		AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(ANNF_sliceProfileInstanceId))
		client.create(sliceProfileUri, ANNF_sliceProfileInstance)

		AAIResourceUri sliceProfileUri1 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(TNFH_sliceProfileInstanceId))
		client.create(sliceProfileUri1, TNFH_sliceProfileInstance)

		AAIResourceUri sliceProfileUri2 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(TNMH_sliceProfileInstanceId))
		client.create(sliceProfileUri2, TNMH_sliceProfileInstance)

	} catch (BpmnError e) {
		throw e
	} catch (Exception ex) {
		msg = "Exception in AnNssmfUtils.createSliceProfilesInAai " + ex.getMessage()
		logger.info(msg)
		exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
	}
	}

}
private SliceProfile createSliceProfile(String domainType, DelegateExecution execution) {

	SliceProfile result = new SliceProfile()
	Map<String,Object> profile
	switch(domainType) {
		case "AN_NF":
			profile = objectMapper.readValue(execution.getVariable("ranNfSliceProfile"), Map.class)//pending fields - cSReliabilityMeanTime, cSAvailabilityTarget, terminalDensity, msgSizeByte
			result.setJitter(profile.get("jitter"))
			result.setLatency(profile.get("latency"))
                        result.setMaxBandwidth(profile.get("maxbandwidth"))
			result.setResourceSharingLevel(profile.get("resourceSharingLevel"))
			result.setUeMobilityLevel(profile.get("uEMobilityLevel"))
			result.setMaxNumberOfUEs(profile.get("maxNumberofUEs"))
			result.setActivityFactor(profile.get("activityFactor"))
			result.setCoverageAreaTAList(profile.get("coverageAreaTAList").toString())
			result.setExpDataRateDL(profile.get("expDataRateDL"))
			result.setExpDataRateUL(profile.get("expDataRateUL"))
			result.setSurvivalTime(profile.get("survivalTime"))
			result.setMaxNumberOfPDUSession(profile.get("maxNumberofPDUSession"))
			result.setAreaTrafficCapDL(profile.get("areaTrafficCapDL"))
			result.setAreaTrafficCapUL(profile.get("areaTrafficCapUL"))
			result.setOverallUserDensity(profile.get("overallUserDensity"))
			result.setTransferIntervalTarget(profile.get("transferIntervalTarget"))
			result.setExpDataRate(profile.get("expDataRate"))
			result.setProfileId(execution.getVariable("ANNF_sliceProfileId"))
			break
		case "TN_FH":
			profile = objectMapper.readValue(execution.getVariable("tnFhSliceProfile"), Map.class)
			result.setJitter(profile.get("jitter"))
			result.setLatency(profile.get("latency"))
                        result.setMaxBandwidth(profile.get("maxbandwidth"))
			result.setResourceSharingLevel(profile.get("resourceSharingLevel"))
			result.setProfileId(execution.getVariable("TNFH_sliceProfileId"))
			break
		case "TN_MH":
			profile = objectMapper.readValue(execution.getVariable("tnMhSliceProfile"), Map.class)
			result.setJitter(profile.get("jitter"))
			result.setLatency(profile.get("latency"))
                        result.setMaxBandwidth(profile.get("maxbandwidth"))
			result.setResourceSharingLevel(profile.get("resourceSharingLevel"))
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
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(instanceId)).relationshipAPI()
            client.create(uri, relationship)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in AN NSSMF Utils : CreateRelationShipInAAI. " + ex.getMessage()
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
		JsonObject esrInfo = new JsonObject()
	        esrInfo.addProperty("networkType", "tn")
	        esrInfo.addProperty("vendor", "ONAP_internal")
		JsonObject response = new JsonObject()
		JsonObject allocateTnNssi = new JsonObject()
		JsonObject serviceInfo = new JsonObject()
		JsonArray transportSliceNetworksList  = new JsonArray()
		JsonArray connectionLinksList = new JsonArray()
		JsonObject connectionLinks = new JsonObject()
		Gson jsonConverter = new Gson()
         	String TNFH_nssiInstanceId = UUID.randomUUID().toString()
                String TNMH_nssiInstanceId = UUID.randomUUID().toString()

		if(action.equals("allocate")){
			JsonObject endpoints = new JsonObject()
			if(domainType.equals("TN_FH")) {
				serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNFH_modelInvariantUuid"))
				serviceInfo.addProperty("serviceUuid", execution.getVariable("TNFH_modelUuid"))
				serviceInfo.addProperty("nssiName", "nssi_tn_fh_"+TNFH_nssiInstanceId)
				serviceInfo.addProperty("sst",  execution.getVariable("sst"))
				allocateTnNssi.addProperty("nsstId", execution.getVariable("TNFH_modelUuid"))
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNFH_modelName"))
				Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("tnFhSliceProfile"), Map.class)
				sliceProfile.put("sliceProfileId", execution.getVariable("TNFH_sliceProfileInstanceId"))
				allocateTnNssi.add("sliceProfile", jsonConverter.toJsonTree(sliceProfile))
				endpoints.addProperty("transportEndpointA", execution.getVariable("tranportEp_ID_RU"))
				endpoints.addProperty("transportEndpointB", execution.getVariable("tranportEp_ID_DUIN"))
				connectionLinksList.add(endpoints)
			}else if(domainType.equals("TN_MH")) {
				serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNMH_modelInvariantUuid"))
				serviceInfo.addProperty("serviceUuid", execution.getVariable("TNMH_modelUuid"))
				serviceInfo.addProperty("nssiName", "nssi_tn_mh_"+TNMH_nssiInstanceId)
				serviceInfo.addProperty("sst",  execution.getVariable("sst"))
				allocateTnNssi.addProperty("nsstId", execution.getVariable("TNMH_modelUuid"))
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNMH_modelName"))
				Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("tnMhSliceProfile"), Map.class)
				sliceProfile.put("sliceProfileId", execution.getVariable("TNMH_sliceProfileInstanceId"))
				allocateTnNssi.add("sliceProfile", jsonConverter.toJsonTree(sliceProfile))
                                endpoints.addProperty("transportEndpointA", execution.getVariable("tranportEp_ID_DUEG"))
				endpoints.addProperty("transportEndpointB", execution.getVariable("tranportEp_ID_CUIN"))
				connectionLinksList.add(endpoints)
			}
		}else if(action.equals("modify-allocate")) {
                        JsonObject endpoints = new JsonObject()
			if(domainType.equals("TN_FH")) {
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNFH_nssiName"))
				allocateTnNssi.addProperty("sliceProfileId", execution.getVariable("TNFH_sliceProfileInstanceId"))
				allocateTnNssi.addProperty("nssiId", execution.getVariable("TNFH_NSSI"))
				serviceInfo.addProperty("nssiId", execution.getVariable("TNFH_NSSI"))
                                serviceInfo.addProperty("nssiName", execution.getVariable("TNFH_nssiName"))
                                Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("tnFhSliceProfile"), Map.class)
                                sliceProfile.put("sliceProfileId", execution.getVariable("TNFH_sliceProfileInstanceId"))
                                allocateTnNssi.add("sliceProfile", jsonConverter.toJsonTree(sliceProfile))
                                endpoints.addProperty("transportEndpointA", execution.getVariable("tranportEp_ID_RU"))
                                endpoints.addProperty("transportEndpointB", execution.getVariable("tranportEp_ID_DUIN"))
                                connectionLinksList.add(endpoints)
			}else if(domainType.equals("TN_MH")) {
				allocateTnNssi.addProperty("nssiName", execution.getVariable("TNMH_nssiName"))
				allocateTnNssi.addProperty("sliceProfileId", execution.getVariable("TNMH_sliceProfileInstanceId"))
				allocateTnNssi.addProperty("nssiId", execution.getVariable("TNMH_NSSI"))
				serviceInfo.addProperty("nssiId", execution.getVariable("TNMH_NSSI"))
                                serviceInfo.addProperty("nssiName", execution.getVariable("TNMH_nssiName"))
                                Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("tnMhSliceProfile"), Map.class)
                                sliceProfile.put("sliceProfileId", execution.getVariable("TNMH_sliceProfileInstanceId"))
                                allocateTnNssi.add("sliceProfile", jsonConverter.toJsonTree(sliceProfile))
                                endpoints.addProperty("transportEndpointA", execution.getVariable("tranportEp_ID_DUEG"))
                                endpoints.addProperty("transportEndpointB", execution.getVariable("tranportEp_ID_CUIN"))
                                connectionLinksList.add(endpoints)
			}
		}

                //Connection links
                connectionLinks.add("connectionLinks", connectionLinksList)
                transportSliceNetworksList.add(connectionLinks)
                allocateTnNssi.add("transportSliceNetworks", transportSliceNetworksList)

		JsonParser parser = new JsonParser()
		String nsiInfo = jsonUtil.getJsonValue(execution.getVariable("sliceParams"), "nsiInfo")
		allocateTnNssi.add("nsiInfo",(JsonObject) parser.parse(nsiInfo))
		allocateTnNssi.addProperty("scriptName", "TN1")
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
		serviceInfo.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId"))
		serviceInfo.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
		response.add("esrInfo", esrInfo)
		response.add("serviceInfo", serviceInfo)
		response.add("allocateTnNssi", allocateTnNssi)
		return response.toString()
	}

	public String buildDeallocateNssiRequest(DelegateExecution execution,String domainType) {
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

                List<String> sNssaiList =  execution.getVariable("snssaiList") as List<String>

	        DeAllocateNssi deallocateNssi = new DeAllocateNssi()
		deallocateNssi.setNsiId(execution.getVariable("nsiId") as String)
		deallocateNssi.setScriptName("TN1")
		deallocateNssi.setSnssaiList(sNssaiList)
                deallocateNssi.setTerminateNssiOption(0)

		JsonObject esrInfo = new JsonObject()
	        esrInfo.addProperty("networkType", "tn")
	        esrInfo.addProperty("vendor", "ONAP_internal")

		JsonObject serviceInfo = new JsonObject()
		serviceInfo.addProperty("globalSubscriberId", globalSubscriberId)
		serviceInfo.addProperty("subscriptionServiceType", subscriptionServiceType)
       		serviceInfo.addProperty("modifyAction", true)

                if(domainType.equals("TN_FH")) {
			deallocateNssi.setNssiId(execution.getVariable("TNFH_NSSI") as String)
			deallocateNssi.setSliceProfileId(execution.getVariable("TNFH_sliceProfileInstanceId") as String)
                        serviceInfo.addProperty("nssiId", execution.getVariable("TNFH_NSSI") as String)
                }else if(domainType.equals("TN_MH")) {
			deallocateNssi.setNssiId(execution.getVariable("TNMH_NSSI") as String)
			deallocateNssi.setSliceProfileId(execution.getVariable("TNMH_sliceProfileInstanceId") as String)
                        serviceInfo.addProperty("nssiId", execution.getVariable("TNMH_NSSI") as String)
                }

		JsonObject json = new JsonObject()
                Gson jsonConverter = new Gson()
                json.add("deAllocateNssi", jsonConverter.toJsonTree(deallocateNssi))
		json.add("esrInfo", esrInfo)
		json.add("serviceInfo", serviceInfo)
		return json.toString()

	}

        public String getModelUuid(DelegateExecution execution, String instanceId) {
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		ServiceInstance serviceInstance = new ServiceInstance()
		AAIResourcesClient client = new AAIResourcesClient()
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(instanceId))
		if (!client.exists(uri)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai : ${instanceId}")
		}
		AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
		Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)

		if(si.isPresent()) {
			serviceInstance = si.get()
		}
		return serviceInstance.getModelVersionId()
	}
}
