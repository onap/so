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
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.List
import static org.apache.commons.lang3.StringUtils.isBlank
import com.google.gson.JsonObject
import com.google.gson.Gson
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.onap.aai.domain.yang.Relationship
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.aai.AAINamespaceConstants
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.NetworkRoute
import org.json.JSONArray

class DoAllocateAccessNSSI extends AbstractServiceTaskProcessor {

	String Prefix="AASS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	RequestDBUtil requestDBUtil = new RequestDBUtil()
	JsonUtils jsonUtil = new JsonUtils()
	OofUtils oofUtils = new OofUtils()
	AnNssmfUtils anNssmfUtils = new AnNssmfUtils()
	private static final ObjectMapper objectMapper = new ObjectMapper()
	private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

	private static final Logger logger = LoggerFactory.getLogger(DoAllocateAccessNSSI.class)

	@Override
	void preProcessRequest(DelegateExecution execution) {
		logger.debug(Prefix + "preProcessRequest Start")
		execution.setVariable("prefix", Prefix)
		execution.setVariable("startTime", System.currentTimeMillis())
		def msg
		try {

			logger.debug("input variables : msoRequestId - "+execution.getVariable("msoRequestId")
					+" modelInvariantUuid - "+execution.getVariable("modelInvariantUuid")+
					" modelUuid - "+execution.getVariable("modelUuid")+
					" globalSubscriberId - "+execution.getVariable("globalSubscriberId")+
					" dummyServiceId - "+ execution.getVariable("dummyServiceId")+
					" nsiId - "+execution.getVariable("nsiId")+
					" networkType - "+execution.getVariable("networkType")+
					" subscriptionServiceType - "+execution.getVariable("subscriptionServiceType")+
					" jobId - "+execution.getVariable("jobId")+
					" sliceParams - "+execution.getVariable("sliceParams")+
					" servicename - "+ execution.getVariable("servicename")+
                                        " sst - "+ execution.getVariable("sst"))

			//validate slice subnet inputs

			String sliceParams = execution.getVariable("sliceParams")
			String sliceProfile = jsonUtil.getJsonValue(sliceParams, "sliceProfile")
			if (isBlank(sliceProfile)) {
				msg = "Input sliceProfile is null"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("sliceProfile", sliceProfile)
			}
			String sliceProfileId = jsonUtil.getJsonValue(sliceProfile, "sliceProfileId")
			def snssaiList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceProfile, "snssaiList"))
			def plmnIdList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceProfile, "pLMNIdList"))
                        String jsonArray = jsonUtil.getJsonValue(sliceProfile, "coverageAreaTAList")
                        List<Integer> list = new ArrayList<>();
                        JSONArray arr = new JSONArray(jsonArray);
                        for (int i = 0; i < arr.length(); i++) {
                                 int s = (int) arr.get(i);
                                 list.add(s);
                        }
                        def coverageAreaTAList = list;

			if (isBlank(sliceProfileId) || (snssaiList.empty) || (plmnIdList.empty)
			|| (coverageAreaTAList.empty)) {

				msg = "Mandatory slice profile fields are empty"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("sliceProfileId", sliceProfileId)
				execution.setVariable("snssaiList", snssaiList)
				execution.setVariable("pLMNIdList", plmnIdList)
				execution.setVariable("coverageAreaTAList", coverageAreaTAList)
			}
			String nsiName = jsonUtil.getJsonValue(sliceParams, "nsiInfo.nsiName")
			String scriptName = jsonUtil.getJsonValue(sliceParams, "scriptName")
			execution.setVariable("nsiName", nsiName)
			execution.setVariable("scriptName", scriptName)
			//generate RAN,RAN NF NSSIs - will be re assigned if oof returns existing NSSI
			String RANServiceInstanceId = UUID.randomUUID().toString()
			String RANNFServiceInstanceId = UUID.randomUUID().toString()
			logger.debug("RAN serviceInstance Id "+RANServiceInstanceId)
			logger.debug("RAN NF serviceInstance Id "+RANNFServiceInstanceId)
			execution.setVariable("RANServiceInstanceId", RANServiceInstanceId)
			execution.setVariable("RANNFServiceInstanceId", RANNFServiceInstanceId)
			execution.setVariable("ranNssiPreferReuse", true)
			execution.setVariable("ranNfNssiPreferReuse", true)
			execution.setVariable("job_timeout", 10)

			//set BH end point
			def BH_endPoints = jsonUtil.getJsonValue(sliceParams, "endPoint")
                        logger.debug("BH end points list : "+BH_endPoints)
                        if(isBlank(BH_endPoints)) {
                                msg = "End point info is empty"
                                logger.debug(msg)
                                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
                        }else {
                                execution.setVariable("bh_endpoint", BH_endPoints)
                        }

		} catch(BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in DoAllocateAccessNSSI.preProcessRequest " + ex.getMessage()
			logger.debug(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.debug(Prefix + "preProcessRequest Exit")
	}

	/*
	 * Prepare request params for decomposing RAN NSST
	 */

	def prepareDecomposeService = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareDecomposeService method start")
		String RANServiceInstanceId = execution.getVariable("RANServiceInstanceId")
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelUuid = execution.getVariable("modelUuid")
		String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
		execution.setVariable("serviceModelInfo", serviceModelInfo)
		execution.setVariable("serviceInstanceId", RANServiceInstanceId)
		logger.debug("serviceModelInfo : "+serviceModelInfo)
		logger.debug("Finish RAN NSST prepareDecomposeService")
	}

	/* process the decompose service(RAN NSST) response
	 *
	 */
	def processDecomposition = { DelegateExecution execution ->
		logger.debug(Prefix+"processDecomposition method start")
		ServiceDecomposition ranNsstServiceDecomposition = execution.getVariable("ranNsstServiceDecomposition")
		logger.debug("ranNsstServiceDecomposition : "+ranNsstServiceDecomposition.toString())
		//RAN NSST decomposition
		String ranModelVersion = ranNsstServiceDecomposition.getModelInfo().getModelVersion()
		String ranModelName = ranNsstServiceDecomposition.getModelInfo().getModelName()
		String serviceCategory=ranNsstServiceDecomposition.getServiceCategory()
		logger.debug("serviceCategory : "+serviceCategory)
		List<ServiceProxy> serviceProxyList = ranNsstServiceDecomposition.getServiceProxy()
		List<String> nsstInfoList = new ArrayList<>()
		for(ServiceProxy serviceProxy : serviceProxyList)
		{
			String nsstModelUuid = serviceProxy.getSourceModelUuid()
			String nsstModelInvariantUuid = serviceProxy.getModelInfo().getModelInvariantUuid()
			String name = serviceProxy.getModelInfo().getModelName()
			String nsstServiceModelInfo = """{
                               "UUID":"${nsstModelUuid}",
                               "invariantUUID":"${nsstModelInvariantUuid}",
                               "name":"${name}"
                        }"""
			nsstInfoList.add(nsstServiceModelInfo)
		}
		int currentIndex=0
		int maxIndex=nsstInfoList.size()
		if(maxIndex < 1)
		{
			String msg = "Exception in RAN NSST processDecomposition. There is no NSST associated with RAN NSST "
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
                if(maxIndex == 1) {
                        logger.info("RAN NSST have only RAN NF NSST")
                        execution.setVariable("ranNfSliceProfile", execution.getVariable("sliceProfile"))
                        execution.setVariable("IsRANNfAlonePresent", true)
                }
		execution.setVariable("ranNsstInfoList", objectMapper.writeValueAsString(nsstInfoList))
		execution.setVariable("currentIndex",currentIndex)
		execution.setVariable("maxIndex",maxIndex)
		execution.setVariable("ranModelVersion", ranModelVersion)
		execution.setVariable("ranModelName", ranModelName)
		logger.debug(Prefix+"processDecomposition maxIndex value - "+maxIndex)

		execution.setVariable("serviceCategory",serviceCategory)
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
		Map<String, Object> profileInfo = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class)
                profileInfo.put("sST",execution.getVariable("sst"))
		String modelUuid = execution.getVariable("modelUuid")
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelName = execution.getVariable("ranModelName")
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
		List<String> nsstInfoList =  objectMapper.readValue(execution.getVariable("ranNsstInfoList"), List.class)
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
			JsonObject sol = solution.get(0)
			boolean existingNSI = sol.get("existingNSI").getAsBoolean()
			logger.debug("existingNSI value : "+existingNSI)
			if(existingNSI) {
				JsonObject sharedNSISolution = sol.get("sharedNSISolution").getAsJsonObject()
				execution.setVariable("sharedRanNSSISolution", sharedNSISolution.toString())
				logger.debug("sharedRanNSSISolution from OOF "+sharedNSISolution)
				String RANServiceInstanceId = sharedNSISolution.get("NSIId").getAsString()
				execution.setVariable("RANServiceInstanceId", RANServiceInstanceId)
				ServiceInstance serviceInstance = new ServiceInstance();
				serviceInstance.setInstanceId(RANServiceInstanceId);
				ServiceDecomposition serviceDecomposition = execution.getVariable("ranNsstServiceDecomposition")
				serviceDecomposition.setServiceInstance(serviceInstance);
				execution.setVariable("ranNsstServiceDecomposition", serviceDecomposition)
				execution.setVariable("isRspRanNssi", true)
			}else {
				JsonObject newNSISolution = sol.get("newNSISolution").getAsJsonObject()
				JsonArray sliceProfiles = newNSISolution.get("sliceProfiles").getAsJsonArray()
				logger.debug("RanConstituentSliceProfiles list from OOF "+sliceProfiles)
				execution.setVariable("RanConstituentSliceProfiles", sliceProfiles.toString())
			}
		}else {
			String statusMessage = jsonUtil.getJsonValue(oofResponse, "statusMessage")
			logger.error("received failed status from oof "+ statusMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a failed Async Response from OOF : "+statusMessage)
		}

	}

	def prepareModifyAccessNssiInputs = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareModifyAccessNssiInputs method start")
		String jobId = UUID.randomUUID().toString()
		execution.setVariable("modifyRanNssiJobId", jobId)
		String snssaiList = execution.getVariable("snssaiList")
		String sliceParams = execution.getVariable("sliceParams")
		String sliceProfileId = execution.getVariable("sliceProfileId")
		String nsiInfo = jsonUtil.getJsonValue(sliceParams, "nsiInfo")
		String scriptName = execution.getVariable("scriptName")

		JsonObject modifySliceParams = new JsonObject()
		modifySliceParams.addProperty("modifyAction", "allocate")
		modifySliceParams.addProperty("snssaiList", snssaiList)
		modifySliceParams.addProperty("sliceProfileId", sliceProfileId)
		modifySliceParams.addProperty("nsiInfo", nsiInfo)

		execution.setVariable("modifySliceParams", modifySliceParams.toString())
		//create operation status in request db
		String nsiId = execution.getVariable("nsiId")
		String modelUuid = execution.getVariable("modelUuid")
		logger.debug("Generated new job for Service Instance serviceId:" + nsiId + "jobId:" + jobId)

		ResourceOperationStatus initStatus = new ResourceOperationStatus()
		initStatus.setServiceId(nsiId)
		initStatus.setOperationId(jobId)
		initStatus.setResourceTemplateUUID(modelUuid)
		initStatus.setOperType("Modify")
		requestDBUtil.prepareInitResourceOperationStatus(execution, initStatus)
	}

	def createModifyNssiQueryJobStatus = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		JsonObject esrInfo = new JsonObject()
	    esrInfo.addProperty("networkType", "tn")
	    esrInfo.addProperty("vendor", "ONAP_internal")

		execution.setVariable("esrInfo", esrInfo.toString())
		JsonObject serviceInfo = new JsonObject()
		serviceInfo.addProperty("nssiId", execution.getVariable("RANServiceInstanceId"))
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
		serviceInfo.addProperty("nssiName", execution.getVariable("servicename"))
                serviceInfo.addProperty("sST", execution.getVariable("sst"))
		serviceInfo.addProperty("PLMNIdList", objectMapper.writeValueAsString(execution.getVariable("pLMNIdList")))
		serviceInfo.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId"))
		serviceInfo.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
		serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("modelInvariantUuid"))
		serviceInfo.addProperty("serviceUuid", execution.getVariable("modelUuid"))
		execution.setVariable("serviceInfo", serviceInfo.toString())
		execution.setVariable("responseId", "")
	}
	def prepareNsstDecomposeService = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareNsstDecomposeService method start")
		List<String> nsstInfoList = objectMapper.readValue(execution.getVariable("ranNsstInfoList"), List.class)
		int currentIndex = execution.getVariable("currentIndex")
		int maxIndex = execution.getVariable("maxIndex")
		logger.debug(Prefix+"prepareNsstDecomposeService : currentIndex value - "+currentIndex+" maxIndex : "+maxIndex)
		if(currentIndex<maxIndex) {
			String nsstInfo = nsstInfoList.get(currentIndex)
			String modelInvariantUuid = jsonUtil.getJsonValue(nsstInfo, "invariantUUID")
			String modelUuid = jsonUtil.getJsonValue(nsstInfo, "UUID")

			String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
			execution.setVariable("serviceModelInfo", serviceModelInfo)
			execution.setVariable("serviceInstanceId", "")
			logger.debug("serviceModelInfo : "+serviceModelInfo)
			currentIndex++
			execution.setVariable("currentIndex", currentIndex)
		}else {
			logger.error("nsstList decomposition error ")
			exceptionUtil.buildAndThrowWorkflowException(execution, 1000, "nsstList decomposition error ")
		}

	}
	def processNsstDecomposition = { DelegateExecution execution ->
		logger.debug(Prefix+"processNsstDecomposition method start")
		ServiceDecomposition decomposedNsst = execution.getVariable("nsstServiceDecomposition")
		logger.debug("decomposedNsst : "+decomposedNsst.toString())

		String nsstType = decomposedNsst.getServiceCategory() //domainType
		String modelVersion = decomposedNsst.getModelInfo().getModelVersion()
		String modelName = decomposedNsst.getModelInfo().getModelName()
		String modelUuid = decomposedNsst.getModelInfo().getModelUuid()
		String modelInvariantUuid = decomposedNsst.getModelInfo().getModelInvariantUuid()

		switch(nsstType) {
			case "AN NF NSST":
				execution.setVariable("ANNF_modelInvariantUuid", modelInvariantUuid)
				execution.setVariable("ANNF_modelUuid", modelUuid)
				execution.setVariable("ANNF_modelVersion", modelVersion)
				execution.setVariable("ANNF_modelName", modelName)
				execution.setVariable("ANNF_ServiceDecomposition", decomposedNsst)
				break
			case "TN FH NSST":
				execution.setVariable("TNFH_modelInvariantUuid", modelInvariantUuid)
				execution.setVariable("TNFH_modelUuid", modelUuid)
				execution.setVariable("TNFH_modelVersion", modelVersion)
				execution.setVariable("TNFH_modelName", modelName)
				execution.setVariable("TNFH_ServiceDecomposition", decomposedNsst)
				break
			case "TN MH NSST":
				execution.setVariable("TNMH_modelInvariantUuid", modelInvariantUuid)
				execution.setVariable("TNMH_modelUuid", modelUuid)
				execution.setVariable("TNMH_modelVersion", modelVersion)
				execution.setVariable("TNMH_modelName", modelName)
				execution.setVariable("TNMH_ServiceDecomposition", decomposedNsst)
				break
			default:
				logger.debug("No expected match found for current nsstType")
				logger.error("No expected match found for current nsstType "+ nsstType)
				exceptionUtil.buildAndThrowWorkflowException(execution, 1000,"No expected match found for current nsstType "+ nsstType)
		}
		boolean isAllNsstsDecomposed = false
		int currentIndex = execution.getVariable("currentIndex")
		int maxIndex = execution.getVariable("maxIndex")
		if(currentIndex == maxIndex) {
			isAllNsstsDecomposed = true
		}
		execution.setVariable("isAllNsstsDecomposed", isAllNsstsDecomposed)
	}
	/*
	 * prepare OOF request for NF RAN NSSI selection
	 */
	def prepareOofRequestForRanNfNSS = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareOofRequestForRanNfNSS method start")
		String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
		logger.debug( "get NSSI option OOF Url: " + urlString)

		//build oof request body
		boolean ranNfNssiPreferReuse = execution.getVariable("ranNfNssiPreferReuse");
		String requestId = execution.getVariable("msoRequestId")
		String serviceCategory = execution.getVariable("serviceCategory")
		String messageType = "NSSISelectionResponse"
                if(execution.getVariable("maxIndex") > 1) {
                    List<String> ranConstituentSliceProfiles = jsonUtil.StringArrayToList(execution.getVariable("RanConstituentSliceProfiles") as String)
                    anNssmfUtils.createDomainWiseSliceProfiles(ranConstituentSliceProfiles, execution)
                }
		Map<String, Object> profileInfo = objectMapper.readValue(execution.getVariable("ranNfSliceProfile"), Map.class)
		profileInfo.put("sST",execution.getVariable("sst"))
		String modelUuid = execution.getVariable("ANNF_modelUuid")
		String modelInvariantUuid = execution.getVariable("ANNF_modelInvariantUuid")
		String modelName = execution.getVariable("ANNF_modelName")
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);

		execution.setVariable("nssiSelection_Url", "/api/oof/selection/nssi/v1")
		execution.setVariable("nssiSelection_messageType",messageType)
		execution.setVariable("nssiSelection_correlator",requestId)
		execution.setVariable("nssiSelection_timeout",timeout)

		String oofRequest = oofUtils.buildSelectNSSIRequest(requestId, messageType, modelUuid, modelInvariantUuid, modelName, profileInfo)

		execution.setVariable("nssiSelection_oofRequest",oofRequest)
	}
	/*
	 * process OOF response for RAN NF NSSI selection
	 */
	def processOofResponseForRanNfNSS = { DelegateExecution execution ->
		logger.debug(Prefix+"processOofResponseForRanNfNSS method start")
		String oofResponse = execution.getVariable("nfNssiSelection_asyncCallbackResponse")
		String requestStatus = jsonUtil.getJsonValue(oofResponse, "requestStatus")
		if(requestStatus.equals("completed")) {
			String solutions = jsonUtil.getJsonValue(oofResponse, "solutions")
			logger.debug("nssi solutions value : "+solutions)
			JsonParser parser = new JsonParser()
			JsonArray solution = parser.parse(solutions)
			if(solution.size()>=1) {
				JsonObject sol = solution.get(0)
				String ranNfNssiId = sol.get("NSSIId").getAsString()
				String invariantUuid = sol.get("invariantUUID").getAsString()
				String uuid = sol.get("UUID").getAsString()
				String nssiName = sol.get("NSSIName").getAsString()
				execution.setVariable("RANNFServiceInstanceId", ranNfNssiId)
				execution.setVariable("RANNFInvariantUUID", invariantUuid)
				execution.setVariable("RANNFUUID", uuid)
				execution.setVariable("RANNFNssiName", nssiName)
				logger.debug("RANNFServiceInstanceId from OOF "+ranNfNssiId)

				ServiceInstance serviceInstance = new ServiceInstance();
				serviceInstance.setInstanceId(ranNfNssiId);
				ServiceDecomposition serviceDecomposition = execution.getVariable("ANNF_ServiceDecomposition")
				serviceDecomposition.setServiceInstance(serviceInstance);
				execution.setVariable("ANNF_ServiceDecomposition", serviceDecomposition)
                                execution.setVariable("modifyAction","allocate")
				execution.setVariable("isRspRanNfNssi", true)
			}else {
				logger.debug("No solutions returned from OOF .. Create new RAN NF NSSI")
			}
		}else {
			String statusMessage = jsonUtil.getJsonValue(oofResponse, "statusMessage")
			logger.error("received failed status from oof "+ statusMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"Received a failed Async Response from OOF : "+statusMessage)
		}
	}

	def createSliceProfilesInAai = { DelegateExecution execution ->
		logger.debug(Prefix+"createSliceProfilesInAai method start")
		anNssmfUtils.createSliceProfilesInAai(execution)
	}

	def processRanNfModifyRsp = { DelegateExecution execution ->
		logger.debug(Prefix+"processRanNfModifyRsp method start")
		anNssmfUtils.processRanNfModifyRsp(execution)
		//create RAN NSSI
		org.onap.aai.domain.yang.ServiceInstance ANServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
		//AN instance creation
		ANServiceInstance.setServiceInstanceId(execution.getVariable("RANServiceInstanceId"))
		String sliceInstanceName = execution.getVariable("servicename")
		ANServiceInstance.setServiceInstanceName(sliceInstanceName)
		String serviceType = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "sST")
                ANServiceInstance.setServiceType(execution.getVariable("sst"))
		String serviceStatus = "deactivated"
		ANServiceInstance.setOrchestrationStatus(serviceStatus)
		String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "pLMNIdList")
                ANServiceInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
		String serviceRole = "nssi"
		ANServiceInstance.setServiceRole(serviceRole)
                List<String> snssaiList = execution.getVariable("snssaiList")
		String snssai = snssaiList.get(0)
                //ANServiceInstance.setEnvironmentContext(snssai)
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelUuid = execution.getVariable("modelUuid") as String
		ANServiceInstance.setModelInvariantId(modelInvariantUuid)
		ANServiceInstance.setModelVersionId(modelUuid)
                ANServiceInstance.setEnvironmentContext(execution.getVariable("networkType")) //Network Type
		ANServiceInstance.setWorkloadContext("AN") //domain Type

		logger.debug("completed AN service instance build "+ ANServiceInstance.toString())
		String msg = ""
		try {

			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri nssiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(execution.getVariable("subscriptionServiceType")).serviceInstance(execution.getVariable("RANServiceInstanceId")))
			client.create(nssiServiceUri, ANServiceInstance)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in AnNssmfUtils.createSliceProfilesInAai " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		//end point update
		createEndPointsInAai(execution)
	}

	def createSdnrRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"createSdnrRequest method start")
		String callbackUrl = UrnPropertiesReader.getVariable("mso.workflow.message.endpoint") + "/AsyncSdnrResponse/"+execution.getVariable("msoRequestId")
		String sdnrRequest = buildSdnrAllocateRequest(execution, "allocate", "instantiateRANSlice", callbackUrl)
		execution.setVariable("createNSSI_sdnrRequest", sdnrRequest)
		execution.setVariable("createNSSI_timeout", "PT10M")
		execution.setVariable("createNSSI_correlator", execution.getVariable("msoRequestId"))
		execution.setVariable("createNSSI_messageType", "AsyncSdnrResponse");
	}

	def processSdnrResponse = { DelegateExecution execution ->
		logger.debug(Prefix+"processSdnrResponse method start")
		String SDNRResponse = execution.getVariable("SDNR_asyncCallbackResponse")
		String status = jsonUtil.getJsonValue(SDNRResponse, "status")
		if(status.equalsIgnoreCase("success")) {
			String nfIds = jsonUtil.getJsonValue(SDNRResponse, "nfIds")
			execution.setVariable("ranNfIdsJson", nfIds)
		}else {
			String reason = jsonUtil.getJsonValue(SDNRResponse, "reason")
			logger.error("received failed status from SDNR "+ reason)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from SDNR "+ reason)
		}
		logger.debug("response from SDNR "+SDNRResponse)
	}

	def updateAaiWithRANInstances = { DelegateExecution execution ->
		logger.debug(Prefix+"updateAaiWithRANInstances method start")
		//create RAN NSSI
		org.onap.aai.domain.yang.ServiceInstance ANServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
		org.onap.aai.domain.yang.ServiceInstance ANNFServiceInstance = new org.onap.aai.domain.yang.ServiceInstance();
		String serviceCategory = execution.getVariable("serviceCategory")
		String serviceStatus = "deactivated"
		String serviceRole = "nssi"
		//AN instance creation
		ANServiceInstance.setServiceInstanceId(execution.getVariable("RANServiceInstanceId") as String)
		String sliceInstanceName = execution.getVariable("servicename")
		ANServiceInstance.setServiceInstanceName(sliceInstanceName)
		ANServiceInstance.setServiceType(execution.getVariable("sst") as String)
		ANServiceInstance.setOrchestrationStatus(serviceStatus)
		String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "pLMNIdList") as String
                ANServiceInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
		ANServiceInstance.setServiceRole(serviceRole)
		List<String> snssaiList = jsonUtil.StringArrayToList(execution.getVariable("snssaiList") as String)
		String snssai = snssaiList.get(0)
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelUuid = execution.getVariable("modelUuid") as String
		ANServiceInstance.setModelInvariantId(modelInvariantUuid)
		ANServiceInstance.setModelVersionId(modelUuid)
                ANServiceInstance.setEnvironmentContext(execution.getVariable("networkType")) //Network Type
		ANServiceInstance.setWorkloadContext("AN")
		String serviceFunctionAn = jsonUtil.getJsonValue(execution.getVariable("sliceProfile") as String, "resourceSharingLevel")
		ANServiceInstance.setServiceFunction(serviceFunctionAn)
		logger.debug("completed AN service instance build " + ANServiceInstance.toString())
		//create RAN NF NSSI
		ANNFServiceInstance.setServiceInstanceId(execution.getVariable("RANNFServiceInstanceId") as String)
                String ANNF_nssiInstanceId = UUID.randomUUID().toString()
		sliceInstanceName = "nssi_an_nf_" + ANNF_nssiInstanceId
		ANNFServiceInstance.setServiceInstanceName(sliceInstanceName)
		ANNFServiceInstance.setServiceType(execution.getVariable("sst") as String)
		ANNFServiceInstance.setOrchestrationStatus(serviceStatus)
		serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile") as String, "pLMNIdList")
                ANNFServiceInstance.setServiceInstanceLocationId(jsonUtil.StringArrayToList(serviceInstanceLocationid).get(0))
		ANNFServiceInstance.setServiceRole(serviceRole)
		snssaiList = jsonUtil.StringArrayToList(execution.getVariable("snssaiList") as String)
		snssai = snssaiList.get(0)
		ANNFServiceInstance.setEnvironmentContext(execution.getVariable("networkType") as String)
                ANNFServiceInstance.setModelInvariantId(execution.getVariable("ANNF_modelInvariantUuid"))
                ANNFServiceInstance.setModelVersionId(execution.getVariable("ANNF_modelUuid"))
		ANNFServiceInstance.setWorkloadContext("AN_NF")
		String serviceFunctionAnnf = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile") as String, "resourceSharingLevel")
		ANNFServiceInstance.setServiceFunction(serviceFunctionAnnf)
		logger.debug("completed AN service instance build "+ ANNFServiceInstance.toString())

		String msg = ""
		try {

			AAIResourcesClient client = new AAIResourcesClient()
	                AAIResourceUri nssiServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId") as String).serviceSubscription(execution.getVariable("subscriptionServiceType") as String).serviceInstance(execution.getVariable("RANServiceInstanceId") as String))
			client.create(nssiServiceUri, ANServiceInstance)

			AAIResourceUri nssiServiceUri1 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId") as String).serviceSubscription(execution.getVariable("subscriptionServiceType") as String).serviceInstance(execution.getVariable("RANNFServiceInstanceId") as String))
			client.create(nssiServiceUri1, ANNFServiceInstance)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in AnNssmfUtils.createSliceProfilesInAai " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		//end point update
		if (!execution.getVariable("IsRANNfAlonePresent")) {
			createEndPointsInAai(execution)
		}
	}
	def prepareTnFhRequest = { DelegateExecution execution ->
		logger.debug(Prefix+"prepareTnFhRequest method start")

		String nssmfRequest = anNssmfUtils.buildCreateNSSMFRequest(execution, "TN_FH", "allocate")
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

		String nssmfRequest = anNssmfUtils.buildCreateNSSMFRequest(execution, "TN_MH", "allocate")
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
		logger.debug(Prefix+"createFhAllocateNssiJobQuery method start")
		createTnAllocateNssiJobQuery(execution, "TN_FH")
	}

	def createMhAllocateNssiJobQuery = { DelegateExecution execution ->
		logger.debug(Prefix+"createMhAllocateNssiJobQuery method start")
		createTnAllocateNssiJobQuery(execution, "TN_MH")
	}

	private void createTnAllocateNssiJobQuery(DelegateExecution execution, String domainType) {
		JsonObject esrInfo = new JsonObject()
	    esrInfo.addProperty("networkType", "tn")
	    esrInfo.addProperty("vendor", "ONAP_internal")
		execution.setVariable("esrInfo", esrInfo.toString())
		JsonObject serviceInfo = new JsonObject()
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
                serviceInfo.addProperty("sST", execution.getVariable("sst"))
		serviceInfo.addProperty("PLMNIdList", objectMapper.writeValueAsString(execution.getVariable("pLMNIdList")))
		serviceInfo.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId"))
		serviceInfo.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
		if(domainType.equals("TN_FH")) {
			serviceInfo.addProperty("nssiName", execution.getVariable("TNFH_modelName"))
			serviceInfo.addProperty("serviceInvariantUuid", execution.getVariable("TNFH_modelInvariantUuid"))
			serviceInfo.addProperty("serviceUuid", execution.getVariable("TNFH_modelUuid"))
		}else if(domainType.equals("TN_MH")) {
			serviceInfo.addProperty("nssiName", execution.getVariable("TNMH_modelName"))
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
		String nssi = jsonUtil.getJsonValue(jobResponse, "nssiId")
		if(status.equalsIgnoreCase("finished")) {
			execution.setVariable("TNFH_NSSI", nssi)
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
		String nssi = jsonUtil.getJsonValue(jobResponse, "nssiId")
		if(status.equalsIgnoreCase("finished")) {
			execution.setVariable("TNMH_NSSI", nssi)
			logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
		}
		else {
			String statusDescription = jsonUtil.getJsonValue(jobResponse, "statusDescription")
			logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
		}
	}

	def processModifyJobStatusRsp = { DelegateExecution execution ->
		logger.debug(Prefix+"processJobStatusRsp method start")
		String jobResponse = execution.getVariable("jobResponse")
		logger.debug("Job status response "+jobResponse)
		String status = jsonUtil.getJsonValue(jobResponse, "status")
		String nssi = jsonUtil.getJsonValue(jobResponse, "nssiId")
		if(status.equalsIgnoreCase("finished")) {
			logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
		}
		else {
			String statusDescription = jsonUtil.getJsonValue(jobResponse, "statusDescription")
			logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
		}
	}

	def updateAairelationships = { DelegateExecution execution ->
		logger.debug(Prefix + "updateAairelationships Start")
		String serviceCategory = execution.getVariable("serviceCategory")
		String msg = ""
		if (execution.getVariable("IsRANNfAlonePresent")) {
			try {
				def ANNF_serviceInstanceId = execution.getVariable("RANNFServiceInstanceId")
				def AN_profileInstanceId = execution.getVariable("sliceProfileId")
                                def AN_NSSI = execution.getVariable("RANServiceInstanceId")
				def ANNF_profileInstanceId = execution.getVariable("ANNF_sliceProfileInstanceId")
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

				Relationship ANNF_relationship = new Relationship()
				String ANNF_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${ANNF_profileInstanceId}"
				String ANNF_NSSI_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${ANNF_serviceInstanceId}"
				ANNF_relationship.setRelatedLink(ANNF_relatedLink)
				ANNF_relationship.setRelatedTo("service-instance")
				ANNF_relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
				anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship, ANNF_serviceInstanceId)
				anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship, AN_profileInstanceId)
				ANNF_relationship.setRelatedLink(ANNF_NSSI_relatedLink)
				anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship, AN_NSSI)

			} catch (BpmnError e) {
				throw e
			} catch (Exception ex) {

				msg = "Exception in DoCreateE2EServiceInstance.createCustomRelationship. " + ex.getMessage()
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
		else {
			try {
				def ANNF_serviceInstanceId = execution.getVariable("RANNFServiceInstanceId")
				def TNFH_serviceInstanceId = execution.getVariable("TNFH_NSSI")
				def TNMH_serviceInstanceId = execution.getVariable("TNMH_NSSI")
				def AN_profileInstanceId = execution.getVariable("sliceProfileId")
				def AN_NSSI = execution.getVariable("RANServiceInstanceId")
				def ANNF_profileInstanceId = execution.getVariable("ANNF_sliceProfileInstanceId")
				def TNFH_profileInstanceId = execution.getVariable("TNFH_sliceProfileInstanceId")
				def TNMH_profileInstanceId = execution.getVariable("TNMH_sliceProfileInstanceId")
				String globalSubscriberId = execution.getVariable("globalSubscriberId")
				String subscriptionServiceType = execution.getVariable("subscriptionServiceType")

				Relationship ANNF_relationship = new Relationship()
				Relationship TNFH_relationship = new Relationship()
				Relationship TNMH_relationship = new Relationship()

				String ANNF_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${ANNF_profileInstanceId}"
				String TNFH_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${TNFH_profileInstanceId}"
				String TNMH_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${TNMH_profileInstanceId}"

				String ANNF_NSSI_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${ANNF_serviceInstanceId}"
				String TNFH_NSSI_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${TNFH_serviceInstanceId}"
				String TNMH_NSSI_relatedLink = "aai/v21/business/customers/customer/${globalSubscriberId}/service-subscriptions/service-subscription/${subscriptionServiceType}/service-instances/service-instance/${TNMH_serviceInstanceId}"

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
				anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship, ANNF_serviceInstanceId)
				anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship, TNFH_serviceInstanceId)
				anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship, TNMH_serviceInstanceId)
				anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship, AN_profileInstanceId)
				anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship, AN_profileInstanceId)
				anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship, AN_profileInstanceId)

				//create AN NSSI and ANNF,TNFH,TNMH relationship in AAI
				ANNF_relationship.setRelatedLink(ANNF_NSSI_relatedLink)
				TNFH_relationship.setRelatedLink(TNFH_NSSI_relatedLink)
				TNMH_relationship.setRelatedLink(TNMH_NSSI_relatedLink)
				anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship, AN_NSSI)
				anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship, AN_NSSI)
				anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship, AN_NSSI)

			} catch (BpmnError e) {
				throw e
			} catch (Exception ex) {

				msg = "Exception in DoCreateE2EServiceInstance.createCustomRelationship. " + ex.getMessage()
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
			}
		}
	}

	/**
	 * update operation status in request db
	 *
	 */
	def prepareOperationStatusUpdate = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareOperationStatusUpdate Start")

		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String modelUuid = execution.getVariable("modelUuid")
		String nssiId = execution.getVariable("RANServiceInstanceId")
		logger.debug("Service Instance serviceId:" + nsiId + " jobId:" + jobId)

		ResourceOperationStatus updateStatus = new ResourceOperationStatus()
		updateStatus.setServiceId(nsiId)
		updateStatus.setOperationId(jobId)
		updateStatus.setResourceTemplateUUID(modelUuid)
		updateStatus.setResourceInstanceID(nssiId)
		updateStatus.setOperType("Allocate")
		updateStatus.setProgress("100")
		updateStatus.setStatus("finished")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, updateStatus)

		logger.debug(Prefix + "prepareOperationStatusUpdate Exit")
	}

	def prepareFailedOperationStatusUpdate = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareFailedOperationStatusUpdate Start")

		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String modelUuid = execution.getVariable("modelUuid")
		String nssiId = execution.getVariable("RANServiceInstanceId")
		logger.debug("Service Instance serviceId:" + nsiId + " jobId:" + jobId)

		ResourceOperationStatus updateStatus = new ResourceOperationStatus()
		updateStatus.setServiceId(nsiId)
		updateStatus.setOperationId(jobId)
		updateStatus.setResourceTemplateUUID(modelUuid)
		updateStatus.setResourceInstanceID(nssiId)
		updateStatus.setOperType("Allocate")
		updateStatus.setProgress("0")
		updateStatus.setStatus("failed")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, updateStatus)
	}

	private String buildSdnrAllocateRequest(DelegateExecution execution, String action, String rpcName, String callbackUrl) {

		String requestId = execution.getVariable("msoRequestId")
		Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("ranNfSliceProfile"), Map.class)
		sliceProfile.put("sliceProfileId", execution.getVariable("ANNF_sliceProfileInstanceId"))
		sliceProfile.put("maxNumberofConns", sliceProfile.get("maxNumberofPDUSession"))
		sliceProfile.put("uLThptPerSlice", sliceProfile.get("expDataRateUL"))
		sliceProfile.put("dLThptPerSlice", sliceProfile.get("expDataRateDL"))

		JsonObject response = new JsonObject()
		JsonObject body = new JsonObject()
		JsonObject input = new JsonObject()
		JsonObject commonHeader = new JsonObject()
		JsonObject payload = new JsonObject()
		JsonObject payloadInput = new JsonObject()
		commonHeader.addProperty("timestamp",new Date(System.currentTimeMillis()).format("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", TimeZone.getDefault()))
		commonHeader.addProperty("api-ver", "1.0")
                commonHeader.addProperty("originator-id", "testing")
		commonHeader.addProperty("request-id", requestId)
		commonHeader.addProperty("sub-request-id", "1")
		commonHeader.add("flags", new JsonObject())
	        Gson jsonConverter = new Gson()
         	payloadInput.add("sliceProfile", jsonConverter.toJsonTree(sliceProfile))
		payloadInput.addProperty("RANNSSIId", execution.getVariable("RANServiceInstanceId"))
		payloadInput.addProperty("NSIID", execution.getVariable("nsiId"))
		payloadInput.addProperty("RANNFNSSIId", execution.getVariable("RANNFServiceInstanceId"))
		payloadInput.addProperty("callbackURL", callbackUrl)
                payloadInput.addProperty("globalSubscriberId", execution.getVariable("globalSubscriberId"))
		payloadInput.addProperty("subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
		payloadInput.add("additionalproperties", new JsonObject())
		payload.add("input", payloadInput)
		input.add("common-header", commonHeader)
		input.addProperty("action", action)
		input.addProperty("payload", payload.toString())
		body.add("input", input)
		response.add("body", body)
		response.addProperty("version", "1.0")
		response.addProperty("rpc-name", rpcName)
		response.addProperty("correlation-id", (requestId+"-1"))
		response.addProperty("type", "request")
		return response.toString()
	}

	private void createEndPointsInAai(DelegateExecution execution) {
		String type = "endpoint"
		String function = "transport_EP"
		int prefixLength = 24
		String addressFamily = "ipv4"
		//BH RAN end point update
		String bh_endpoint = execution.getVariable("bh_endpoint")
		String bh_routeId = UUID.randomUUID().toString()
		execution.setVariable("tranportEp_ID_bh", bh_routeId)
		String role = "CU"
                String CU_IpAddress = jsonUtil.getJsonValue(bh_endpoint, "ipAddress")
                String LogicalLinkId = jsonUtil.getJsonValue(bh_endpoint, "logicInterfaceId")
		String nextHopInfo = jsonUtil.getJsonValue(bh_endpoint, "nextHopInfo")
		NetworkRoute bh_ep = new NetworkRoute()
		bh_ep.setRouteId(bh_routeId)
		bh_ep.setFunction(function)
		bh_ep.setRole(role)
		bh_ep.setType(type)
		bh_ep.setIpAddress(CU_IpAddress)
		bh_ep.setLogicalInterfaceId(LogicalLinkId)
		bh_ep.setNextHop(nextHopInfo)
		bh_ep.setPrefixLength(prefixLength)
		bh_ep.setAddressFamily(addressFamily)
		//FH RAN end points update
		//RU
		String RU_routeId = UUID.randomUUID().toString()
		execution.setVariable("tranportEp_ID_RU", RU_routeId)
		role = "RU"
		NetworkRoute RU_ep = new NetworkRoute()
		RU_ep.setRouteId(RU_routeId)
		RU_ep.setFunction(function)
		RU_ep.setRole(role)
		RU_ep.setType(type)
		RU_ep.setIpAddress("192.168.100.4")
		RU_ep.setLogicalInterfaceId("1234")
		RU_ep.setNextHop("networkId-providerId-10-clientId-0-topologyId-2-nodeId-10.1.1.1-ltpId-512")
		RU_ep.setPrefixLength(prefixLength)
		RU_ep.setAddressFamily(addressFamily)
		//DU Ingress
		String DUIN_routeId = UUID.randomUUID().toString()
		execution.setVariable("tranportEp_ID_DUIN", DUIN_routeId)
		role = "DU"
		NetworkRoute DU_ep = new NetworkRoute()
		DU_ep.setRouteId(DUIN_routeId)
		DU_ep.setFunction(function)
		DU_ep.setRole(role)
		DU_ep.setType(type)
		DU_ep.setIpAddress("192.168.100.5")
		DU_ep.setLogicalInterfaceId("1234")
		DU_ep.setNextHop("networkId-providerId-20-clientId-0-topologyId-2-nodeId-10.2.1.2-ltpId-512")
		DU_ep.setPrefixLength(prefixLength)
		DU_ep.setAddressFamily(addressFamily)
		//MH RAN end point update
		//DUEG
		String DUEG_routeId = UUID.randomUUID().toString()
		execution.setVariable("tranportEp_ID_DUEG", DUEG_routeId)
		NetworkRoute DUEG_ep = new NetworkRoute()
                DUEG_ep.setRouteId(DUEG_routeId)
                DUEG_ep.setFunction(function)
                DUEG_ep.setRole(role)
                DUEG_ep.setType(type)
                DUEG_ep.setIpAddress("192.168.100.5")
                DUEG_ep.setLogicalInterfaceId("1234")
                DUEG_ep.setPrefixLength(prefixLength)
                DUEG_ep.setAddressFamily(addressFamily)
		DUEG_ep.setNextHop("networkId-providerId-10-clientId-0-topologyId-2-nodeId-10.1.1.1-ltpId-512")
		//CUIN
		String CUIN_routeId = UUID.randomUUID().toString()
		execution.setVariable("tranportEp_ID_CUIN", CUIN_routeId)
		NetworkRoute CUIN_ep = new NetworkRoute()
		CUIN_ep.setRouteId(CUIN_routeId)
		CUIN_ep.setFunction(function)
		CUIN_ep.setRole(role)
		CUIN_ep.setType(type)
		CUIN_ep.setIpAddress("192.168.100.6")
		CUIN_ep.setLogicalInterfaceId("1234")
		CUIN_ep.setNextHop("networkId-providerId-20-clientId-0-topologyId-2-nodeId-10.2.1.2-ltpId-512")
		CUIN_ep.setPrefixLength(prefixLength)
		CUIN_ep.setAddressFamily(addressFamily)
		try {
			AAIResourcesClient client = new AAIResourcesClient()
			logger.debug("creating bh endpoint . ID : "+bh_routeId+" node details : "+bh_ep.toString())
			AAIResourceUri networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), bh_routeId)
			client.create(networkRouteUri, bh_ep)
			logger.debug("creating RU endpoint . ID : "+RU_routeId+" node details : "+RU_ep.toString())
			networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), RU_routeId)
			client.create(networkRouteUri, RU_ep)
			logger.debug("creating DUIN endpoint . ID : "+DUIN_routeId+" node details : "+DU_ep.toString())
			networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), DUIN_routeId)
			client.create(networkRouteUri, DU_ep)
			logger.debug("creating DUEG endpoint . ID : "+DUEG_routeId+" node details : "+DUEG_ep.toString())
			networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), DUEG_routeId)
			client.create(networkRouteUri, DUEG_ep)
			logger.debug("creating CUIN endpoint . ID : "+CUIN_routeId+" node details : "+CUIN_ep.toString())
			networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), CUIN_routeId)
			client.create(networkRouteUri, CUIN_ep)
			//relationship b/w bh_ep and RAN NSSI
			def AN_NSSI = execution.getVariable("RANServiceInstanceId")
			Relationship relationship = new Relationship()
			String relatedLink = "aai/v21/network/network-routes/network-route/${bh_routeId}"
			relationship.setRelatedLink(relatedLink)
			relationship.setRelatedTo("network-route")
			relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
			anNssmfUtils.createRelationShipInAAI(execution, relationship, AN_NSSI)
			def ANNF_serviceInstanceId = execution.getVariable("RANNFServiceInstanceId")
			relatedLink = "aai/v21/network/network-routes/network-route/${RU_routeId}"
			relationship.setRelatedLink(relatedLink)
			anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
			relatedLink = "aai/v21/network/network-routes/network-route/${DUIN_routeId}"
			relationship.setRelatedLink(relatedLink)
			anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
			relatedLink = "aai/v21/network/network-routes/network-route/${DUEG_routeId}"
			relationship.setRelatedLink(relatedLink)
			anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
			relatedLink = "aai/v21/network/network-routes/network-route/${CUIN_routeId}"
			relationship.setRelatedLink(relatedLink)
			anNssmfUtils.createRelationShipInAAI(execution, relationship, ANNF_serviceInstanceId)
		} catch (BpmnError e) {
		throw e
	} catch (Exception ex) {
		String msg = "Exception in createEndPointsInAai " + ex.getMessage()
		logger.info(msg)
		exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
	}
	}
}
