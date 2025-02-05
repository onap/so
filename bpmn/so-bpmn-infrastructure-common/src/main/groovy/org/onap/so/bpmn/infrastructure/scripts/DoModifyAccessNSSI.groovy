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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.beans.nsmf.PerfReqEmbb
import org.onap.so.beans.nsmf.PerfReq
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.util.List
import static org.apache.commons.lang3.StringUtils.isBlank
import com.google.gson.JsonObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonArray
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import com.google.gson.JsonParser
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.SliceProfiles
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aai.domain.yang.AllottedResources
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.aaiclient.client.aai.entities.uri.AAISimplePluralUri
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import jakarta.ws.rs.NotFoundException

class DoModifyAccessNSSI extends AbstractServiceTaskProcessor {

	String Prefix="MASS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	RequestDBUtil requestDBUtil = new RequestDBUtil()
	JsonUtils jsonUtil = new JsonUtils()
	OofUtils oofUtils = new OofUtils()
	ObjectMapper objectMapper = new ObjectMapper();
	AnNssmfUtils anNssmfUtils = new AnNssmfUtils()
	private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

	private static final Logger logger = LoggerFactory.getLogger(DoModifyAccessNSSI.class)

	@Override
	void preProcessRequest(DelegateExecution execution) {
		logger.debug(Prefix + "preProcessRequest Start")
		execution.setVariable("prefix", Prefix)
		execution.setVariable("startTime", System.currentTimeMillis())
		def msg
		try {

			logger.debug("input variables : msoRequestId - "+execution.getVariable("msoRequestId")+
					" globalSubscriberId - "+execution.getVariable("globalSubscriberId")+
					" serviceInstanceID - "+execution.getVariable("serviceInstanceID")+
					" nsiId - "+execution.getVariable("nsiId")+
					" networkType - "+execution.getVariable("networkType")+
					" subscriptionServiceType - "+execution.getVariable("subscriptionServiceType")+
					" jobId - "+execution.getVariable("jobId")+
					" sliceParams - "+execution.getVariable("sliceParams")+
					" servicename - "+ execution.getVariable("servicename"))

			//validate slice subnet inputs

			String sliceParams = execution.getVariable("sliceParams")
			String modifyAction = jsonUtil.getJsonValue(sliceParams, "modifyAction")
			if (isBlank(modifyAction)) {
				msg = "Input modifyAction is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("modifyAction", modifyAction)
				switch(modifyAction) {
					case "allocate":
						execution.setVariable("isModifyallocate", true)
						break
					case "deallocate":
						execution.setVariable("isModifydeallocate", true)
						break
					case "reconfigure":
						execution.setVariable("isModifyreconfigure", true)
						String resourceConfig = jsonUtil.getJsonValue(sliceParams, "resourceConfig")
						execution.setVariable("additionalProperties", resourceConfig)
						break
					default:
						logger.debug("Invalid modify Action")
						exceptionUtil.buildAndThrowWorkflowException(execution, 500, "Invalid modify Action : "+modifyAction)
				}
			}
                        String modelUuid = execution.getVariable("modelUuid")
                        if (isBlank(modelUuid)) {
                                 modelUuid = anNssmfUtils.getModelUuid(execution, execution.getVariable("serviceInstanceID"))
                        }
                        execution.setVariable("modelUuid",modelUuid)
			List<String> snssaiList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceParams, "snssaiList"))
			String sliceProfileId = jsonUtil.getJsonValue(sliceParams, "sliceProfileId")
			if (isBlank(sliceProfileId) || (snssaiList.empty)) {
				msg = "Mandatory fields are empty"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("sliceProfileId", sliceProfileId)
				execution.setVariable("snssaiList", snssaiList)
			}
			String nsiName = jsonUtil.getJsonValue(sliceParams, "nsiInfo.nsiName")
			String scriptName = jsonUtil.getJsonValue(sliceParams, "scriptName")
			execution.setVariable("nsiName", nsiName)
			execution.setVariable("scriptName", scriptName)
			execution.setVariable("job_timeout", 10)
			execution.setVariable("ranNssiPreferReuse", false)
		} catch(BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in DoModifyAccessNSSI.preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(Prefix + "preProcessRequest Exit")
	}
	
	def getSliceProfile = { DelegateExecution execution ->
		logger.debug(Prefix + "getSliceProfiles Start")
		String instanceId = execution.getVariable("sliceProfileId")
		ServiceInstance sliceProfileInstance = getServiceInstance(execution, instanceId)
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		SliceProfile ranSliceProfile = new SliceProfile()
		AAIResourcesClient client = new AAIResourcesClient()
		AAISimplePluralUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                 .customer(globalSubscriberId)
                 .serviceSubscription(subscriptionServiceType)
                 .serviceInstance(instanceId)
                 .sliceProfiles())
	
        if (!client.exists(uri)) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Slice Profiles of service Instance was not found in aai : ${instanceId}")
		}

		AAIResultWrapper wrapper = client.get(uri, NotFoundException.class)
		Optional<SliceProfiles> si = wrapper.asBean(SliceProfiles.class)
		if(si.isPresent()) {
		 ranSliceProfile = si.get().getSliceProfile().get(0)
		}
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                logger.debug("RAN slice profile : "+objectMapper.writeValueAsString(ranSliceProfile))
		execution.setVariable("RANSliceProfile", objectMapper.writeValueAsString(ranSliceProfile))
		execution.setVariable("ranSliceProfileInstance", sliceProfileInstance)
	}
	
	/*
	 * Function to subnet capabilities from nssmf adapter
	 */
	def getSubnetCapabilities = { DelegateExecution execution ->
		logger.debug(Prefix+"getSubnetCapabilities method start")

		String tnNssmfRequest = anNssmfUtils.buildCreateTNNSSMFSubnetCapabilityRequest()

		String urlString = "/api/rest/provMns/v1/NSS/subnetCapabilityQuery"

		String tnNssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, tnNssmfRequest)

		if (tnNssmfResponse != null) {
			String FHCapabilities= jsonUtil.getJsonValue(tnNssmfResponse, "TN_FH")
			String MHCapabilities = jsonUtil.getJsonValue(tnNssmfResponse, "TN_MH")
			execution.setVariable("FHCapabilities",FHCapabilities)
			execution.setVariable("MHCapabilities",MHCapabilities)

		} else {
			logger.error("received error message from NSSMF : "+ tnNssmfResponse)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
		}
		String anNssmfRequest = anNssmfUtils.buildCreateANNFNSSMFSubnetCapabilityRequest()

		String anNssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, anNssmfRequest)

		if (anNssmfResponse != null) {
			String ANNFCapabilities = jsonUtil.getJsonValue(anNssmfResponse, "AN_NF")
			execution.setVariable("ANNFCapabilities",ANNFCapabilities)

		} else {
			logger.error("received error message from NSSMF : "+ anNssmfResponse)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
		}
	}

	
	/*
	 * prepare OOF request for RAN NSSI selection
	 */
	def prepareOofRequestForRanNSS = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareOofRequestForRanNSS method start")

		String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
		logger.debug( "get NSSI option OOF Url: " + urlString)
                JsonParser parser = new JsonParser()
		//build oof request body
		boolean ranNssiPreferReuse = execution.getVariable("ranNssiPreferReuse");
		String requestId = execution.getVariable("msoRequestId")
		String messageType = "NSISelectionResponse"
		Map<String, Object> profileInfo = objectMapper.readValue(execution.getVariable("RANSliceProfile"), Map.class)
                ServiceInstance ranSliceProfileInstance = execution.getVariable("ranSliceProfileInstance")
                profileInfo.put("sST",ranSliceProfileInstance.getServiceType())
                profileInfo.put("snssaiList",execution.getVariable("snssaiList"))
                profileInfo.put("pLMNIdList",Arrays.asList(ranSliceProfileInstance.getServiceInstanceLocationId()))
                profileInfo.put("uEMobilityLevel",profileInfo.get("ueMobilityLevel"))
                profileInfo.put("cSAvailabilityTarget",profileInfo.get("csAvailabilityTarget"))
                profileInfo.put("maxNumberofPDUSession",profileInfo.get("maxNumberOfPDUSession"))
                profileInfo.put("maxNumberofUEs",profileInfo.get("maxNumberOfUEs"))

                PerfReq perfReq = new PerfReq();
                List<PerfReqEmbb> perfReqEmbbs = new ArrayList<>();
                PerfReqEmbb perfReqEmbb = new PerfReqEmbb();
                perfReqEmbb.setExpDataRateDL(profileInfo.get("expDataRateDL"));
                perfReqEmbb.setExpDataRateUL(profileInfo.get("expDataRateUL"));
                perfReqEmbbs.add(perfReqEmbb);
                perfReq.setPerfReqEmbbList(perfReqEmbbs);
                profileInfo.put("perfReq",perfReq)

                profileInfo.remove("maxNumberOfUEs")
                profileInfo.remove("resourceVersion")
                profileInfo.remove("csAvailabilityTarget")
                profileInfo.remove("ueMobilityLevel")
                profileInfo.remove("maxNumberOfPDUSession")
                profileInfo.remove("profileId")
                String modelUuid = ranSliceProfileInstance.getModelVersionId()
                String modelInvariantUuid = ranSliceProfileInstance.getModelInvariantId()
		String modelName = execution.getVariable("servicename")
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
		List<String> nsstInfoList =  new ArrayList<>()
		JsonArray capabilitiesList = new JsonArray()
		String FHCapabilities = execution.getVariable("FHCapabilities")
		String MHCapabilities = execution.getVariable("MHCapabilities")
		String ANNFCapabilities = execution.getVariable("ANNFCapabilities")
		JsonObject FH = new JsonObject()
		JsonObject MH = new JsonObject()
		JsonObject ANNF = new JsonObject()
                FH.addProperty("domainType", "TN_FH")
		FH.add("capabilityDetails", (JsonObject) parser.parse(FHCapabilities))
		MH.addProperty("domainType", "TN_MH")
		MH.add("capabilityDetails", (JsonObject) parser.parse(MHCapabilities))
		ANNF.addProperty("domainType", "AN_NF")
		ANNF.add("capabilityDetails", (JsonObject) parser.parse(ANNFCapabilities))
		capabilitiesList.add(FH)
		capabilitiesList.add(MH)
		capabilitiesList.add(ANNF)

		execution.setVariable("nssiSelection_Url", "/api/oof/selection/nsi/v1")
		execution.setVariable("nssiSelection_messageType",messageType)
		execution.setVariable("nssiSelection_correlator",requestId)
		execution.setVariable("nssiSelection_timeout",timeout)
		String oofRequest = anNssmfUtils.buildSelectRANNSSIRequest(requestId, messageType, modelUuid,modelInvariantUuid,
				modelName, profileInfo, nsstInfoList, capabilitiesList, ranNssiPreferReuse)

		execution.setVariable("nssiSelection_oofRequest",oofRequest)
		logger.debug("Sending request to OOF: " + oofRequest)
	}
	
	/*
	 * process OOF response for RAN NSSI selection
	 */
	def processOofResponseForRanNSS = { DelegateExecution execution ->
		logger.debug(Prefix+"processOofResponseForRanNSS method start")
		String oofResponse = execution.getVariable("nssiSelection_asyncCallbackResponse")
		String requestStatus = jsonUtil.getJsonValue(oofResponse, "requestStatus")
		if(requestStatus.equals("completed")) {
                        String solutions = jsonUtil.getJsonValue(oofResponse, "solutions")
			logger.debug("solutions value : "+solutions)
			JsonParser parser = new JsonParser()
			JsonArray solution = parser.parse(solutions)
			boolean existingNSI = solution.get(0).get("existingNSI").getAsBoolean()
			logger.debug("existingNSI value : "+existingNSI)
			if(!existingNSI) {
                                JsonObject newNSISolution =  solution.get(0).get("newNSISolution").getAsJsonObject()
				JsonArray sliceProfiles =  newNSISolution.get("sliceProfiles")
				logger.debug("sliceProfiles: "+ sliceProfiles.toString())
				execution.setVariable("RanConstituentSliceProfiles", sliceProfiles.toString())
				List<String> ranConstituentSliceProfiles = jsonUtil.StringArrayToList(sliceProfiles.toString())
				anNssmfUtils.createDomainWiseSliceProfiles(ranConstituentSliceProfiles, execution)
				logger.debug("RanConstituentSliceProfiles list from OOF "+sliceProfiles)
			}else {
				String statusMessage = jsonUtil.getJsonValue(oofResponse, "statusMessage")
				logger.error("failed to get slice profiles from oof "+ statusMessage)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"failed to get slice profiles from oof "+statusMessage)
			}
		}else {
			String statusMessage = jsonUtil.getJsonValue(oofResponse, "statusMessage")
			logger.error("received failed status from oof "+ statusMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a failed Async Response from OOF : "+statusMessage)
		}

	}
	def getNssisFromAai = { DelegateExecution execution ->
		logger.debug(Prefix+"getNssisFromAai method start")
		String instanceId = execution.getVariable("serviceInstanceID")
		String role = "nssi"
		Map<String,ServiceInstance> ranConstituentNssis = getRelatedInstancesByRole(execution, role, instanceId)
		logger.debug("getNssisFromAai ranConstituentNssis : "+ranConstituentNssis.toString())
		ranConstituentNssis.each { key, val -> 
			switch(key) {
				case "AN_NF":
					execution.setVariable("ANNF_NSSI", val.getServiceInstanceId())
					execution.setVariable("ANNF_nssiName", val.getServiceInstanceName())
                                        execution.setVariable("ANNF_modelInvariantUuid", val.getModelInvariantId())
					execution.setVariable("ANNF_modelUuid", val.getModelVersionId())
					break
				case "TN_FH":
					execution.setVariable("TNFH_NSSI", val.getServiceInstanceId())
					execution.setVariable("TNFH_nssiName", val.getServiceInstanceName())
                                        execution.setVariable("TNFH_modelInvariantUuid", val.getModelInvariantId())
					execution.setVariable("TNFH_modelUuid", val.getModelVersionId())
                                        getConnectionLinks(execution, key, val)
				        break
				case "TN_MH":
				        execution.setVariable("TNMH_NSSI", val.getServiceInstanceId())
					execution.setVariable("TNMH_nssiName", val.getServiceInstanceName())
                                        execution.setVariable("TNMH_modelInvariantUuid", val.getModelInvariantId())
					execution.setVariable("TNMH_modelUuid", val.getModelVersionId())
                                        getConnectionLinks(execution, key, val)
					break
				default:
					logger.error("No expected match found for current domainType "+ key)
					exceptionUtil.buildAndThrowWorkflowException(execution, 1000,"No expected match found for current domainType "+ key)
			}
		}
		
	}

        private void getConnectionLinks(DelegateExecution execution, String domainType, ServiceInstance instance) {
                AllottedResources allottedResources = instance.getAllottedResources()
				if(allottedResources == null) {
				String msg = "AllottedResourceFromAAI doesn't exist. " + instance
                logger.debug(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
				}

                List<AllottedResource> AllottedResourceList = allottedResources.getAllottedResource()
                for(AllottedResource allottedResource : AllottedResourceList) {
                List<Relationship> relationshipList = allottedResource.getRelationshipList().getRelationship()
                    for (Relationship relationship : relationshipList) {
                        String relatedTo = relationship.getRelatedTo()
                        if (relatedTo.toLowerCase() == "logical-link") {
                                String relatioshipurl = relationship.getRelatedLink()
                                String logicalLinkId=
                                                relatioshipurl.substring(relatioshipurl.lastIndexOf("/") + 1, relatioshipurl.length())
                                AAIResourcesClient client = new AAIResourcesClient()
                                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().logicalLink(logicalLinkId))
                                if (!client.exists(uri)) {
                                        exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
                                                        "Resource was not found in aai: ${logicalLinkId}")
                                }
                                AAIResultWrapper wrapper01 = client.get(uri, NotFoundException.class)
                                Optional<org.onap.aai.domain.yang.LogicalLink> resource = wrapper01.asBean(org.onap.aai.domain.yang.LogicalLink.class)
                                if (resource.isPresent()) {
                                        org.onap.aai.domain.yang.LogicalLink logicalLinkInstance = resource.get()
                                        if(domainType.equalsIgnoreCase("TN_FH"))
                                        {
                                        execution.setVariable("tranportEp_ID_RU",logicalLinkInstance.getLinkName())
                                        execution.setVariable("tranportEp_ID_DUIN",logicalLinkInstance.getLinkName2())
                                        }
                                        else if(domainType.equalsIgnoreCase("TN_MH"))
                                        {
                                        execution.setVariable("tranportEp_ID_DUEG",logicalLinkInstance.getLinkName())
                                        execution.setVariable("tranportEp_ID_CUIN",logicalLinkInstance.getLinkName2())
                                        }
                                }
                        }
                    }
                }
        }

	def createSliceProfiles = { DelegateExecution execution ->
		logger.debug(Prefix+"createSliceProfiles method start")
		anNssmfUtils.createSliceProfilesInAai(execution)
	}
	def updateRelationshipInAai = { DelegateExecution execution ->
		logger.debug(Prefix+"updateRelationshipInAai method start")
		String msg = ""
		try {
			def ANNF_serviceInstanceId = execution.getVariable("ANNF_NSSI")
			def TNFH_serviceInstanceId = execution.getVariable("TNFH_NSSI")
			def TNMH_serviceInstanceId = execution.getVariable("TNMH_NSSI")
			def AN_profileInstanceId = execution.getVariable("sliceProfileId")
			def ANNF_profileInstanceId = execution.getVariable("ANNF_sliceProfileInstanceId")
			def TNFH_profileInstanceId = execution.getVariable("TNFH_sliceProfileInstanceId")
			def TNMH_profileInstanceId = execution.getVariable("TNMH_sliceProfileInstanceId")
			String globalSubscriberId = execution.getVariable("globalSubscriberId")
			String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

			Relationship ANNF_relationship = new Relationship()
			Relationship TNFH_relationship = new Relationship()
			Relationship TNMH_relationship = new Relationship()
			
			String ANNF_relatedLink = "aai/v16/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${ANNF_profileInstanceId}"
			String TNFH_relatedLink = "aai/v16/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${TNFH_profileInstanceId}"
			String TNMH_relatedLink = "aai/v16/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${TNMH_profileInstanceId}"
			
			ANNF_relationship.setRelatedLink(ANNF_relatedLink)
			ANNF_relationship.setRelatedTo("service-instance")
			ANNF_relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
			TNFH_relationship.setRelatedLink(TNFH_relatedLink)
			TNFH_relationship.setRelatedTo("service-instance")
			TNFH_relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
			TNMH_relationship.setRelatedLink(TNMH_relatedLink)
			TNMH_relationship.setRelatedTo("service-instance")
			TNMH_relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
			
			// create SliceProfile and NSSI relationship in AAI
			anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship,ANNF_serviceInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship,TNFH_serviceInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship,TNMH_serviceInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship,AN_profileInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship,AN_profileInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship,AN_profileInstanceId)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {

			msg = "Exception in DoCreateE2EServiceInstance.createCustomRelationship. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}
	
	def processRanNfModifyRsp = { DelegateExecution execution ->
		logger.debug(Prefix+"processRanNfModifyRsp method start")
		anNssmfUtils.processRanNfModifyRsp(execution)
	}
	
	def prepareTnFhRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareTnFhRequest method start")

		String nssmfRequest = anNssmfUtils.buildCreateNSSMFRequest(execution, "TN_FH", "modify-allocate")
		String urlString = "/api/rest/provMns/v1/NSS/SliceProfiles"
		String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)

		if (nssmfResponse != null) {
			execution.setVariable("nssmfResponse", nssmfResponse)
			String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
			execution.setVariable("TNFH_jobId",jobId)
		} else {
			logger.error("received error message from NSSMF : "+ nssmfResponse)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
		}
		logger.debug("Exit prepareTnFhRequest")

	}
	def prepareTnMhRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareTnMhRequest method start")

		String nssmfRequest = anNssmfUtils.buildCreateNSSMFRequest(execution, "TN_MH", "modify-allocate")
		String urlString = "/api/rest/provMns/v1/NSS/SliceProfiles"
		String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)

		if (nssmfResponse != null) {
			execution.setVariable("nssmfResponse", nssmfResponse)
			String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
			execution.setVariable("TNMH_jobId",jobId)
		} else {
			logger.error("received error message from NSSMF : "+ nssmfResponse)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
		}
		logger.debug("Exit prepareTnMhRequest")
	}
	
	def createFhAllocateNssiJobQuery = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		createTnAllocateNssiJobQuery(execution, "TN_FH")
	}
	
	def createMhAllocateNssiJobQuery = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		createTnAllocateNssiJobQuery(execution, "TN_MH")
	}
	
	private void createTnAllocateNssiJobQuery(DelegateExecution execution, String domainType) {
                JsonObject esrInfo = new JsonObject()
		esrInfo.addProperty("networkType", "tn")
	        esrInfo.addProperty("vendor", "ONAP_internal")
		execution.setVariable("esrInfo", esrInfo.toString())
		JsonObject serviceInfo = new JsonObject()
		
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
		serviceInfo.addProperty("PLMNIdList", objectMapper.writeValueAsString(execution.getVariable("plmnIdList")))
		serviceInfo.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId"))
		serviceInfo.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
		if(domainType.equals("TN_FH")) {
			serviceInfo.addProperty("nssiId", execution.getVariable("TNFH_NSSI"))
			serviceInfo.addProperty("nssiName", execution.getVariable("TNFH_nssiName"))
                        serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNFH_modelInvariantUuid"))
			serviceInfo.addProperty("serviceUuid", execution.getVariable("TNFH_modelUuid"))
		}else if(domainType.equals("TN_MH")) {
			serviceInfo.addProperty("nssiId", execution.getVariable("TNMH_NSSI"))
			serviceInfo.addProperty("nssiName", execution.getVariable("TNMH_nssiName"))
                        serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNMH_modelInvariantUuid"))
			serviceInfo.addProperty("serviceUuid", execution.getVariable("TNMH_modelUuid"))
		}
		execution.setVariable("serviceInfo", serviceInfo.toString())
		execution.setVariable("responseId", "")
	}
	
        def processFhAllocateNssiJobStatusRsp = { DelegateExecution execution ->
                logger.debug(Prefix+"processJobStatusRsp method start")
                String jobResponse = execution.getVariable("TNFH_jobResponse")
                logger.debug("Job status response "+jobResponse)
                String status = jsonUtil.getJsonValue(jobResponse, "status")
                String nssi = jsonUtil.getJsonValue(jobResponse, "nssi")
                if(status.equalsIgnoreCase("finished")) {
                        logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
                }
                else {
                        String statusDescription = jsonUtil.getJsonValue(jobResponse, "statusDescription")
                        logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
                }
        }

        def processMhAllocateNssiJobStatusRsp = { DelegateExecution execution ->
                logger.debug(Prefix+"processJobStatusRsp method start")
                String jobResponse = execution.getVariable("TNMH_jobResponse")
                logger.debug("Job status response "+jobResponse)
                String status = jsonUtil.getJsonValue(jobResponse, "status")
                String nssi = jsonUtil.getJsonValue(jobResponse, "nssi")
                if(status.equalsIgnoreCase("finished")) {
                        logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
                }
                else {
                        String statusDescription = jsonUtil.getJsonValue(jobResponse, "statusDescription")
                        logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
                }
        }
	
	def getSliceProfilesFromAai = { DelegateExecution execution ->
		logger.debug(Prefix+"getSliceProfilesFromAai method start")
		String instanceId = execution.getVariable("sliceProfileId")
		String role = "slice-profile-instance"
		Map<String,ServiceInstance> ranConstituentSliceProfiles = getRelatedInstancesByRole(execution, role, instanceId)
		logger.debug("getSliceProfilesFromAai ranConstituentSliceProfiles : "+ranConstituentSliceProfiles.toString())
		ranConstituentSliceProfiles.each { key, val ->
			switch(key) {
				case "AN_NF":
					execution.setVariable("ANNF_sliceProfileInstanceId", val.getServiceInstanceId())
					break
				case "TN_FH":
					execution.setVariable("TNFH_sliceProfileInstanceId", val.getServiceInstanceId())
					break
				case "TN_MH":
					execution.setVariable("TNMH_sliceProfileInstanceId", val.getServiceInstanceId())
					break
				default:
					logger.error("No expected match found for current domainType "+ key)
					exceptionUtil.buildAndThrowWorkflowException(execution, 1000,"No expected match found for current domainType "+ key)
			}
		}
	}
	
	def prepareTnFhDeallocateRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareTnFhDeallocateRequest method start")
		String nssmfRequest = anNssmfUtils.buildDeallocateNssiRequest(execution, "TN_FH")
		String nssiId = execution.getVariable("TNFH_NSSI")
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
	}
	
	def prepareTnMhDeallocateRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareTnFhDeallocateRequest method start")
		String nssmfRequest = anNssmfUtils.buildDeallocateNssiRequest(execution, "TN_MH")
		String nssiId = execution.getVariable("TNFH_NSSI")
		execution.setVariable("tnFHNSSIId", nssiId)
		String urlString = "/api/rest/provMns/v1/NSS/SliceProfiles/" + nssiId
				String nssmfResponse = nssmfAdapterUtils.sendPostRequestNSSMF(execution, urlString, nssmfRequest)
				if (nssmfResponse != null) {
					String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
					execution.setVariable("TN_MH_jobId",jobId)
				} else {
					logger.error("received error message from NSSMF : "+ nssmfResponse)
					exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a Bad Sync Response from NSSMF.")
				}
	}
	
	def createFhDeAllocateNssiJobQuery = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		createTnAllocateNssiJobQuery(execution, "TN_FH")
	}
	
	def createMhDeAllocateNssiJobQuery = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		createTnAllocateNssiJobQuery(execution, "TN_MH")
	}
	def deleteFhSliceProfile = { DelegateExecution execution ->
		logger.debug(Prefix+"deleteFhSliceProfile method start")
		deleteServiceInstanceInAAI(execution,execution.getVariable("TNFH_sliceProfileInstanceId"))
	}
	def deleteMhSliceProfile = { DelegateExecution execution ->
		logger.debug(Prefix+"deleteMhSliceProfile method start")
		deleteServiceInstanceInAAI(execution,execution.getVariable("TNMH_sliceProfileInstanceId"))	
	}
	def deleteAnSliceProfile = { DelegateExecution execution ->
		logger.debug(Prefix+"deleteAnSliceProfile method start")
		deleteServiceInstanceInAAI(execution,execution.getVariable("ANNF_sliceProfileInstanceId"))
	}
	/**
	 * update operation status in request db
	 *
	 */
	def prepareOperationStatusUpdate = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareOperationStatusUpdate Start")

		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String nssiId = execution.getVariable("serviceInstanceID")
                String modelUuid = execution.getVariable("modelUuid")
		logger.debug("Service Instance serviceId:" + nsiId + " jobId:" + jobId)

		ResourceOperationStatus updateStatus = new ResourceOperationStatus()
		updateStatus.setServiceId(nsiId)
		updateStatus.setOperationId(jobId)
		updateStatus.setResourceTemplateUUID(modelUuid)
		updateStatus.setResourceInstanceID(nssiId)
		updateStatus.setOperType("Modify")
		updateStatus.setProgress("100")
		updateStatus.setStatus("finished")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, updateStatus)

		logger.debug(Prefix + "prepareOperationStatusUpdate Exit")
	}

	def prepareFailedOperationStatusUpdate = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareFailedOperationStatusUpdate Start")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String nssiId = execution.getVariable("serviceInstanceID")
                String modelUuid = execution.getVariable("modelUuid")
		logger.debug("Service Instance serviceId:" + nsiId + " jobId:" + jobId)

		ResourceOperationStatus updateStatus = new ResourceOperationStatus()
		updateStatus.setServiceId(nsiId)
		updateStatus.setOperationId(jobId)
		updateStatus.setResourceTemplateUUID(modelUuid)
		updateStatus.setResourceInstanceID(nssiId)
		updateStatus.setOperType("Modify")
		updateStatus.setProgress("0")
		updateStatus.setStatus("failed")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, updateStatus)
	}
	
	/**
	 * @param execution
	 * @param role            - nssi/slice profile instance
	 * @param instanceId    - id to which the related list to be found
	 * @return
	 */
	private Map<String,ServiceInstance> getRelatedInstancesByRole(DelegateExecution execution,String role,String instanceId) {
		logger.debug("${Prefix} - Fetching related ${role} from AAI")
		String globalSubscriberId = execution.getVariable("globalSubscriberId")
		String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
		
		Map<String,ServiceInstance> relatedInstances = new HashMap<>()
		
		AAIResourcesClient client = new AAIResourcesClient()
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

				AAIResourcesClient client01 = new AAIResourcesClient()
				AAIResourceUri uri01 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(serviceInstanceId))
				if (!client.exists(uri01)) {
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500,
							"Service Instance was not found in aai: ${serviceInstanceId} related to ${instanceId}")
				}
				AAIResultWrapper wrapper01 = client01.get(uri01.depth(Depth.TWO), NotFoundException.class)
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
	
	private ServiceInstance getServiceInstance(DelegateExecution execution, String instanceId) {
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
		return serviceInstance
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
