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
import java.sql.Timestamp
import java.util.List
import static org.apache.commons.lang3.StringUtils.isBlank
import com.google.gson.JsonObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonArray
import org.onap.aai.domain.yang.Relationship
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.aai.AAINamespaceConstants
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.NetworkRoute

class DoAllocateAccessNSSI extends AbstractServiceTaskProcessor {

	String Prefix="AASS_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	RequestDBUtil requestDBUtil = new RequestDBUtil()
	JsonUtils jsonUtil = new JsonUtils()
	OofUtils oofUtils = new OofUtils()
	AnNssmfUtils anNssmfUtils = new AnNssmfUtils()
	ObjectMapper objectMapper = new ObjectMapper();
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
					" servicename - "+ execution.getVariable("servicename"))

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
			def plmnIdList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceProfile, "plmnIdList"))
			def coverageAreaTAList = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceProfile, "coverageAreaTAList"))

			if (isBlank(sliceProfileId) || (snssaiList.empty) || (plmnIdList.empty)
			|| (coverageAreaTAList.empty)) {

				msg = "Mandatory slice profile fields are empty"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			} else {
				execution.setVariable("sliceProfileId", sliceProfileId)
				execution.setVariable("snssaiList", snssaiList)
				execution.setVariable("plmnIdList", plmnIdList)
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
			List<String> BH_endPoints = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(sliceParams, "endPoints"))
			logger.debug("BH end points list : "+BH_endPoints)
			if(BH_endPoints.empty) {
				msg = "End point info is empty"
				logger.debug(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}else {
				execution.setVariable("bh_endpoint", BH_endPoints.get(0))
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
		List<ServiceProxy> serviceProxyList = ranNsstServiceDecomposition.getServiceProxy()
		List<String> nsstInfoList = new ArrayList<>()
		for(ServiceProxy serviceProxy : serviceProxyList)
		{
			String nsstModelUuid = serviceProxy.getModelInfo().getModelUuid()
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
		execution.setVariable("ranNsstInfoList",nsstInfoList)
		execution.setVariable("ranModelVersion", ranModelVersion)
		execution.setVariable("ranModelName", ranModelName)
		execution.setVariable("currentIndex",currentIndex)
		execution.setVariable("maxIndex",maxIndex)
		logger.debug(Prefix+"processDecomposition maxIndex value - "+maxIndex)
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

		//build oof request body
		boolean ranNssiPreferReuse = execution.getVariable("ranNssiPreferReuse");
		String requestId = execution.getVariable("msoRequestId")
		String messageType = "NSISelectionResponse"
		Map<String, Object> profileInfo = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class)
		String modelUuid = execution.getVariable("modelUuid")
		String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
		String modelName = execution.getVariable("ranModelName")
		String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
		List<String> nsstInfoList =  objectMapper.readValue(execution.getVariable("nsstInfoList"), List.class)
		JsonArray capabilitiesList = new JsonArray()
		String FHCapabilities = execution.getVariable("FHCapabilities")
		String MHCapabilities = execution.getVariable("MHCapabilities")
		String ANNFCapabilities = execution.getVariable("ANNFCapabilities")
		JsonObject FH = new JsonObject()
		JsonObject MH = new JsonObject()
		JsonObject ANNF = new JsonObject()
		FH.addProperty("domainType", "TN_FH")
		FH.addProperty("capabilityDetails", FHCapabilities)
		MH.addProperty("domainType", "TN_MH")
		MH.addProperty("capabilityDetails", MHCapabilities)
		ANNF.addProperty("domainType", "AN_NF")
		ANNF.addProperty("capabilityDetails", FHCapabilities)
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
			List<String> solution = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(oofResponse, "solutions"))
			boolean existingNSI = jsonUtil.getJsonValue(solution.get(0), "existingNSI")
			if(existingNSI) {
				def sharedNSISolution = jsonUtil.getJsonValue(solution.get(0), "sharedNSISolution")
				execution.setVariable("sharedRanNSSISolution", sharedNSISolution)
				logger.debug("sharedRanNSSISolution from OOF "+sharedNSISolution)
				String RANServiceInstanceId = jsonUtil.getJsonValue(solution.get(0), "sharedNSISolution.NSIId")
				execution.setVariable("RANServiceInstanceId", RANServiceInstanceId)
				ServiceInstance serviceInstance = new ServiceInstance();
				serviceInstance.setInstanceId(RANServiceInstanceId);
				ServiceDecomposition serviceDecomposition = execution.getVariable("ranNsstServiceDecomposition")
				serviceDecomposition.setServiceInstance(serviceInstance);
				execution.setVariable("ranNsstServiceDecomposition", serviceDecomposition)
				execution.setVariable("isRspRanNssi", true)
			}else {
				def sliceProfiles = jsonUtil.getJsonValue(solution.get(0), "newNSISolution.sliceProfiles")
				execution.setVariable("RanConstituentSliceProfiles", sliceProfiles)
				logger.debug("RanConstituentSliceProfiles list from OOF "+sliceProfiles)
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
		modifySliceParams.addProperty("scriptName", scriptName)
		
		execution.setVariable("modifySliceParams", modifySliceParams.toString())
		//create operation status in request db
		String serviceId = execution.getVariable("RANServiceInstanceId")
		String nsiId = execution.getVariable("nsiId")
		logger.debug("Generated new job for Service Instance serviceId:" + serviceId + "jobId:" + jobId)

		ResourceOperationStatus initStatus = new ResourceOperationStatus()
		initStatus.setServiceId(serviceId)
		initStatus.setOperationId(jobId)
		initStatus.setResourceTemplateUUID(nsiId)
		initStatus.setOperType("Modify")
		requestDBUtil.prepareInitResourceOperationStatus(execution, initStatus)
	}
	
	def createModifyNssiQueryJobStatus = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		EsrInfo esrInfo = new EsrInfo()
		esrInfo.setNetworkType("AN")
		esrInfo.setVendor("ONAP")
		String esrInfoString = objectMapper.writeValueAsString(esrInfo)
		execution.setVariable("esrInfo", esrInfoString)
		JsonObject serviceInfo = new JsonObject()
		serviceInfo.addProperty("nssiId", execution.getVariable("RANServiceInstanceId"))
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
		serviceInfo.addProperty("nssiName", execution.getVariable("servicename"))
		String sST = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "sST")
		serviceInfo.addProperty("sST", sST)
		serviceInfo.addProperty("PLMNIdList", objectMapper.writeValueAsString(execution.getVariable("plmnIdList")))
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
		
		String nsstType = decomposedNsst.getServiceRole() //domainType
		String modelVersion = decomposedNsst.getModelInfo().getModelVersion()
		String modelName = decomposedNsst.getModelInfo().getModelName()
		String modelUuid = decomposedNsst.getModelInfo().getModelUuid()
		String modelInvariantUuid = decomposedNsst.getModelInfo().getModelInvariantUuid()
		
		switch(nsstType) {
			case "AN_NF":
				execution.setVariable("ANNF_modelInvariantUuid", modelInvariantUuid)
				execution.setVariable("ANNF_modelUuid", modelUuid)
				execution.setVariable("ANNF_modelVersion", modelVersion)
				execution.setVariable("ANNF_modelName", modelName)
				execution.setVariable("ANNF_ServiceDecomposition", decomposedNsst)
				break
			case "TN_FH":
				execution.setVariable("TNFH_modelInvariantUuid", modelInvariantUuid)
				execution.setVariable("TNFH_modelUuid", modelUuid)
				execution.setVariable("TNFH_modelVersion", modelVersion)
				execution.setVariable("TNFH_modelName", modelName)
				execution.setVariable("TNFH_ServiceDecomposition", decomposedNsst)
				break
			case "TN_MH":
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
		String messageType = "NSSISelectionResponse"
		List<String> ranConstituentSliceProfiles = jsonUtil.StringArrayToList(execution.getVariable("RanConstituentSliceProfiles"))
		anNssmfUtils.createDomainWiseSliceProfiles(ranConstituentSliceProfiles, execution)
		Map<String, Object> profileInfo = objectMapper.readValue(execution.getVariable("ranNfSliceProfile"), Map.class)
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
			List<String> solution = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(oofResponse, "solutions"))		
			if(solution.size()>=1) {
				String ranNfNssiId = jsonUtil.getJsonValue(solution.get(0), "NSSIId")
				String invariantUuid = jsonUtil.getJsonValue(solution.get(0), "invariantUUID")
				String uuid = jsonUtil.getJsonValue(solution.get(0), "UUID")
				String nssiName = jsonUtil.getJsonValue(solution.get(0), "NSSIName")
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
		org.onap.aai.domain.yang.ServiceInstance ANServiceInstance = new ServiceInstance();
		//AN instance creation
		ANServiceInstance.setServiceInstanceId(execution.getVariable("RANServiceInstanceId"))
		String sliceInstanceName = execution.getVariable("servicename")
		ANServiceInstance.setServiceInstanceName(sliceInstanceName)
		String serviceType = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "sST")
		ANServiceInstance.setServiceType(serviceType)
		String serviceStatus = "deactivated"
		ANServiceInstance.setOrchestrationStatus(serviceStatus)
		String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "plmnIdList")
		ANServiceInstance.setServiceInstanceLocationId(serviceInstanceLocationid)
		String serviceRole = "nssi"
		ANServiceInstance.setServiceRole(serviceRole)
		List<String> snssaiList = objectMapper.readValue(execution.getVariable("snssaiList"), List.class)
		String snssai = snssaiList.get(0)
		ANServiceInstance.setEnvironmentContext(snssai)
		ANServiceInstance.setWorkloadContext("AN")
		
		logger.debug("completed AN service instance build "+ ANServiceInstance.toString())
		String msg = ""
		try {
	
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri nssiServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), execution.getVariable("RANServiceInstanceId"))
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
		String sdnrRequest = buildSdnrAllocateRequest(execution, "allocate", "InstantiateRANSlice", callbackUrl)
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
		org.onap.aai.domain.yang.ServiceInstance ANServiceInstance = new ServiceInstance();
		org.onap.aai.domain.yang.ServiceInstance ANNFServiceInstance = new ServiceInstance();
		//AN instance creation
		ANServiceInstance.setServiceInstanceId(execution.getVariable("RANServiceInstanceId"))
		String sliceInstanceName = execution.getVariable("servicename")
		ANServiceInstance.setServiceInstanceName(sliceInstanceName)
		String serviceType = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "sST")
		ANServiceInstance.setServiceType(serviceType)
		String serviceStatus = "deactivated"
		ANServiceInstance.setOrchestrationStatus(serviceStatus)
		String serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "plmnIdList")
		ANServiceInstance.setServiceInstanceLocationId(serviceInstanceLocationid)
		String serviceRole = "nssi"
		ANServiceInstance.setServiceRole(serviceRole)
		List<String> snssaiList = objectMapper.readValue(execution.getVariable("snssaiList"), List.class)
		String snssai = snssaiList.get(0)
		ANServiceInstance.setEnvironmentContext(snssai)
		ANServiceInstance.setWorkloadContext("AN")
		
		logger.debug("completed AN service instance build "+ ANServiceInstance.toString())
		//create RAN NF NSSI
		ANNFServiceInstance.setServiceInstanceId(execution.getVariable("RANNFServiceInstanceId"))
		sliceInstanceName = execution.getVariable("ANNF_modelName")
		ANNFServiceInstance.setServiceInstanceName(sliceInstanceName)
		ANNFServiceInstance.setServiceType(serviceType)
		ANNFServiceInstance.setOrchestrationStatus(serviceStatus)
		serviceInstanceLocationid = jsonUtil.getJsonValue(execution.getVariable("ranNfSliceProfile"), "plmnIdList")
		ANNFServiceInstance.setServiceInstanceLocationId(serviceInstanceLocationid)
		ANNFServiceInstance.setServiceRole(serviceRole)
		snssaiList = objectMapper.readValue(execution.getVariable("snssaiList"), List.class)
		snssai = snssaiList.get(0)
		ANNFServiceInstance.setEnvironmentContext(snssai)
		ANNFServiceInstance.setWorkloadContext("AN-NF")
		logger.debug("completed AN service instance build "+ ANNFServiceInstance.toString())
		
		String msg = ""
		try {
	
			AAIResourcesClient client = new AAIResourcesClient()
			AAIResourceUri nssiServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), execution.getVariable("RANServiceInstanceId"))
			client.create(nssiServiceUri, ANServiceInstance)
	
			AAIResourceUri nssiServiceUri1 = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), execution.getVariable("RANNFServiceInstanceId"))
			client.create(nssiServiceUri1, ANNFServiceInstance)
	
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
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		createTnAllocateNssiJobQuery(execution, "TN_FH")		
	}
	
	def createMhAllocateNssiJobQuery = { DelegateExecution execution ->
		logger.debug(Prefix+"createModifyNssiQueryJobStatus method start")
		createTnAllocateNssiJobQuery(execution, "TN_MH")
	}
	
	private void createTnAllocateNssiJobQuery(DelegateExecution execution, String domainType) {
		EsrInfo esrInfo = new EsrInfo()
		esrInfo.setNetworkType("TN")
		esrInfo.setVendor("ONAP")
		String esrInfoString = objectMapper.writeValueAsString(esrInfo)
		execution.setVariable("esrInfo", esrInfoString)
		JsonObject serviceInfo = new JsonObject()
		serviceInfo.addProperty("nssiId", null)
		serviceInfo.addProperty("nsiId", execution.getVariable("nsiId"))
		String sST = jsonUtil.getJsonValue(execution.getVariable("sliceProfile"), "sST")
		serviceInfo.addProperty("sST", sST)
		serviceInfo.addProperty("PLMNIdList", objectMapper.writeValueAsString(execution.getVariable("plmnIdList")))
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
		String status = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.status")
		String nssi = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.nssi")
		if(status.equalsIgnoreCase("finished")) {
			execution.setVariable("TNFH_NSSI", nssi)
			logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
		}
		else {
			String statusDescription = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.statusDescription")
			logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
		}
	}
	
	def processMhAllocateNssiJobStatusRsp = { DelegateExecution execution ->
		logger.debug(Prefix+"processJobStatusRsp method start")
		String jobResponse = execution.getVariable("TNMH_jobResponse")
		logger.debug("Job status response "+jobResponse)
		String status = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.status")
		String nssi = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.nssi")
		if(status.equalsIgnoreCase("finished")) {
			execution.setVariable("TNMH_NSSI", nssi)
			logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
		}
		else {
			String statusDescription = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.statusDescription")
			logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
		}
	}
	
	def processModifyJobStatusRsp = { DelegateExecution execution ->
		logger.debug(Prefix+"processJobStatusRsp method start")
		String jobResponse = execution.getVariable("jobResponse")
		logger.debug("Job status response "+jobResponse)
		String status = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.status")
		String nssi = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.nssi")
		if(status.equalsIgnoreCase("finished")) {
			logger.debug("Job successfully completed ... proceeding with flow for nssi : "+nssi)
		}
		else {
			String statusDescription = jsonUtil.getJsonValue(jobResponse, "responseDescriptor.statusDescription")
			logger.error("received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000,"received failed status from job status query for nssi : "+nssi+" with status description : "+ statusDescription)
		}
	}

	def updateAairelationships = { DelegateExecution execution ->
		logger.debug(Prefix + "updateAairelationships Start")
		String msg = ""
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
			anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship,ANNF_serviceInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship,TNFH_serviceInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship,TNMH_serviceInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship,AN_profileInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship,AN_profileInstanceId)
			anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship,AN_profileInstanceId)
			
			//create AN NSSI and ANNF,TNFH,TNMH relationship in AAI
			ANNF_relationship.setRelatedLink(ANNF_NSSI_relatedLink)
			TNFH_relationship.setRelatedLink(TNFH_NSSI_relatedLink)
			TNMH_relationship.setRelatedLink(TNMH_NSSI_relatedLink)
			anNssmfUtils.createRelationShipInAAI(execution, ANNF_relationship,AN_NSSI)
			anNssmfUtils.createRelationShipInAAI(execution, TNFH_relationship,AN_NSSI)
			anNssmfUtils.createRelationShipInAAI(execution, TNMH_relationship,AN_NSSI)

		} catch (BpmnError e) {
			throw e
		} catch (Exception ex) {

			msg = "Exception in DoCreateE2EServiceInstance.createCustomRelationship. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}
	
	/**
	 * update operation status in request db
	 *
	 */
	def prepareOperationStatusUpdate = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareOperationStatusUpdate Start")

		String serviceId = execution.getVariable("dummyServiceId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String nssiId = execution.getVariable("RANServiceInstanceId")
		logger.debug("Service Instance serviceId:" + serviceId + " jobId:" + jobId)

		ResourceOperationStatus updateStatus = new ResourceOperationStatus()
		updateStatus.setServiceId(serviceId)
		updateStatus.setOperationId(jobId)
		updateStatus.setResourceTemplateUUID(nsiId)
		updateStatus.setResourceInstanceID(nssiId)
		updateStatus.setOperType("Allocate")
		updateStatus.setProgress(100)
		updateStatus.setStatus("finished")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, updateStatus)

		logger.debug(Prefix + "prepareOperationStatusUpdate Exit")
	}

	def prepareFailedOperationStatusUpdate = { DelegateExecution execution ->
		logger.debug(Prefix + "prepareFailedOperationStatusUpdate Start")
		
		String serviceId = execution.getVariable("dummyServiceId")
		String jobId = execution.getVariable("jobId")
		String nsiId = execution.getVariable("nsiId")
		String nssiId = execution.getVariable("RANServiceInstanceId")
		logger.debug("Service Instance serviceId:" + serviceId + " jobId:" + jobId)

		ResourceOperationStatus updateStatus = new ResourceOperationStatus()
		updateStatus.setServiceId(serviceId)
		updateStatus.setOperationId(jobId)
		updateStatus.setResourceTemplateUUID(nsiId)
		updateStatus.setResourceInstanceID(nssiId)
		updateStatus.setOperType("Allocate")
		updateStatus.setProgress(0)
		updateStatus.setStatus("failed")
		requestDBUtil.prepareUpdateResourceOperationStatus(execution, updateStatus)
	}
	
	private String buildSdnrAllocateRequest(DelegateExecution execution, String action, String rpcName, String callbackUrl) {
		
		String requestId = execution.getVariable("msoRequestId")
		Date date = new Date().getTime()
		Timestamp time = new Timestamp(date)
		Map<String,Object> sliceProfile = objectMapper.readValue(execution.getVariable("ranNfSliceProfile"), Map.class)
		sliceProfile.put("sliceProfileId", execution.getVariable("ANNF_sliceProfileInstanceId"))
		sliceProfile.put("maxNumberofConns", sliceProfile.get("maxNumberofPDUSessions"))
		sliceProfile.put("uLThptPerSlice", sliceProfile.get("expDataRateUL"))
		sliceProfile.put("dLThptPerSlice", sliceProfile.get("expDataRateDL"))
		String sliceProfileString = objectMapper.writeValueAsString(sliceProfile)
		JsonObject response = new JsonObject()
		JsonObject body = new JsonObject()
		JsonObject input = new JsonObject()
		JsonObject commonHeader = new JsonObject()
		JsonObject payload = new JsonObject()
		JsonObject payloadInput = new JsonObject()
		commonHeader.addProperty("TimeStamp", time.toString())
		commonHeader.addProperty("APIver", "1.0")
		commonHeader.addProperty("RequestID", requestId)
		commonHeader.addProperty("SubRequestID", "1")
		commonHeader.add("RequestTrack", new JsonObject())
		commonHeader.add("Flags", new JsonObject())
		payloadInput.addProperty("sliceProfile", sliceProfileString)
		payloadInput.addProperty("RANNSSIId", execution.getVariable("RANServiceInstanceId"))
		payloadInput.addProperty("NSIID", execution.getVariable("nsiId"))
		payloadInput.addProperty("RANNFNSSIId", execution.getVariable("RANNFServiceInstanceId"))
		payloadInput.addProperty("callbackURL", callbackUrl)
		payloadInput.add("additionalproperties", new JsonObject())
		payload.add("input", payloadInput)
		input.add("CommonHeader", commonHeader)
		input.addProperty("Action", action)
		input.add("Payload", payload)
		body.add("input", input)
		response.add("body", body)
		response.addProperty("version", "1.0")
		response.addProperty("rpc-name", rpcName)
		response.addProperty("correlation-id", requestId+"-1")
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
		String CU_IpAddress = jsonUtil.getJsonValue(bh_endpoint, "IpAddress")
		String LogicalLinkId = jsonUtil.getJsonValue(bh_endpoint, "LogicalLinkId")
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
		RU_ep.setNextHop("Host1")
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
		DU_ep.setNextHop("Host2")
		DU_ep.setPrefixLength(prefixLength)
		DU_ep.setAddressFamily(addressFamily)
		//MH RAN end point update
		//DUEG
		String DUEG_routeId = UUID.randomUUID().toString()
		execution.setVariable("tranportEp_ID_DUEG", DUEG_routeId)
		NetworkRoute DUEG_ep = new NetworkRoute()
		DU_ep.setRouteId(DUEG_routeId)
		DU_ep.setNextHop("Host3")
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
		CUIN_ep.setNextHop("Host4")
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
			logger.debug("creating DUEG endpoint . ID : "+DUEG_routeId+" node details : "+DU_ep.toString())
			networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), DUEG_routeId)
			client.create(networkRouteUri, DU_ep)
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
