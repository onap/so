/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.*
import org.onap.so.beans.nsmf.oof.SubnetCapability
import org.onap.so.beans.nsmf.oof.TemplateInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

class DoCreateSliceServiceOptionV2 extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoCreateSliceServiceOptionV2.class)

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    OofUtils oofUtils = new OofUtils()

    ObjectMapper objectMapper = new ObjectMapper()

    void preProcessRequest (DelegateExecution execution) {
    }


    /**
     * prepare select nsi request
     * @param execution
     */
    void prepareSelectNSIRequest(DelegateExecution execution) {

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug( "get NSI option OOF Url: " + urlString)

        boolean isNSISuggested = true
        execution.setVariable("isNSISuggested",isNSISuggested)
        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSISelectionResponse"

        Map<String, Object> profileInfo = execution.getVariable("serviceProfile")
        Map<String, Object> nstSolution = execution.getVariable("nstSolution")
        logger.debug("Get NST selection from OOF: " + nstSolution.toString())
        String nstInfo = """{
            "modelInvariantId":"${nstSolution.invariantUUID}",
            "modelVersionId":"${nstSolution.UUID}",
            "modelName":"${nstSolution.NSTName}"
         }"""

        execution.setVariable("nsiSelectionUrl", "/api/oof/selection/nsi/v1")
        execution.setVariable("nsiSelection_messageType",messageType)
        execution.setVariable("nsiSelection_correlator",requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution);
        execution.setVariable("nsiSelection_timeout",timeout)
        String oofRequest = oofUtils.buildSelectNSIRequest(requestId, nstInfo,messageType, profileInfo)
        execution.setVariable("nsiSelection_oofRequest",oofRequest)
        logger.debug("Sending request to OOF: " + oofRequest)
    }

    /**
     * process select nsi response
     * @param execution
     */
    void processOOFResponse(DelegateExecution execution) {

        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams") as SliceTaskParams
        String OOFResponse = execution.getVariable("nsiSelection_oofResponse")
        logger.debug("NSI OOFResponse is: " + OOFResponse)
        execution.setVariable("OOFResponse", OOFResponse)
        //This needs to be changed to derive a value when we add policy to decide the solution options.

        Map<String, Object> resMap = objectMapper.readValue(OOFResponse, Map.class)
        List<Map<String, Object>> nsiSolutions = (List<Map<String, Object>>) resMap.get("solutions")
        Map<String, Object> solutions = nsiSolutions.get(0)

        String resourceSharingLevel = execution.getVariable("resourceSharingLevel")
        Boolean isSharable = resourceSharingLevel == "shared"

        if (solutions != null) {
            if (isSharable && solutions.get("existingNSI")) {
                //sharedNSISolution
                processSharedNSISolutions(solutions, execution)
            }
            else if(solutions.containsKey("newNSISolution")) {
                processNewNSISolutions(solutions, execution)
            }
        }
        execution.setVariable("sliceTaskParams", sliceTaskParams)
        logger.debug("sliceTaskParams: "+sliceTaskParams.convertToJson())
        logger.debug("*** Completed options Call to OOF ***")

        logger.debug("start parseServiceProfile")
        parseServiceProfile(execution)
        logger.debug("end parseServiceProfile")
    }


    private void processNewNSISolutions(Map solutions, DelegateExecution execution) {
        int index = 0
        List<Map> newNSISolutions = solutions.get("newNSISolution")
        List<Map> NSSImap = new ArrayList<>()
        if (newNSISolutions != null && newNSISolutions.size() > 0) {
            NSSImap = newNSISolutions.get(index).get("NSSISolutions") as List<Map>
            for (Map nssi : NSSImap) {
                Map oofSliceProfile = nssi.get("sliceProfile")
                String domain = oofSliceProfile.getOrDefault("domainType","")
                logger.debug("OOF newNSISolutions SliceProfile: " +oofSliceProfile.toString()+",domain:${domain}")
                if(null != domain){
                    //TODO
//                    def nssiSolution = nssi.get("NSSISolution") as Map<String, ?>
//                    String nssiName = nssiSolution.getOrDefault("NSSIName", "")
//                    String nssiId = nssiSolution.getOrDefault("NSSIId", "")
//                    saveNSSIId(nssi, sliceTaskParams)
                    Map<String, Object> sliceProfile = getSliceProfile(domain, execution, oofSliceProfile)
                    saveSliceProfile(execution, domain, sliceProfile)

                }
            }
        }
    }

    private void processSharedNSISolutions(Map solutions, DelegateExecution execution) {
        String nsiName, nsiInstanceId, nssiId, nssiName
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")

        Map sharedNSIsolution = ((List) solutions.get("sharedNSISolutions"))?.get(0)
        nsiInstanceId = sharedNSIsolution.getOrDefault("NSIId", "")
        nsiName = sharedNSIsolution.getOrDefault("NSIName", "")
        sliceTaskParams.setSuggestNsiId(nsiInstanceId)
        sliceTaskParams.setSuggestNsiName(nsiName)

        //Temporary modification
        List NSSIs = sharedNSIsolution.get("NSSIs")
        for(Map nssi : NSSIs){
            Map oofSliceProfile = ((List)nssi.get("sliceProfile"))?.get(0)
            String domain = oofSliceProfile.getOrDefault("domainType","")
            nssiId = nssi.getOrDefault("NSSIId","")
            nssiName = nssi.getOrDefault("NSSIName","")
            saveNSSIId(domain, nssiId, nssiName,execution)
            Map<String, Object> sliceProfile = getSliceProfile(domain, execution, oofSliceProfile)
            saveSliceProfile(execution, domain, sliceProfile)
            logger.debug("OOF sharedNSISolution SliceProfile:"+oofSliceProfile.toString()+",domain:${domain}")
            logger.debug("OOF sharedNSISolution nsiInstanceId:${nsiInstanceId}, nsiName:${nsiName}, nssiId:${nssiId}, nssiName:${nssiName}")
        }
    }

    private void parseServiceProfile(DelegateExecution execution) {
        logger.debug("Start parseServiceProfile")
        String serviceType = execution.getVariable("serviceType")
        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        // set sliceProfile for three domains
        if(!sliceTaskParams.getSliceProfileAn()){
            Map<String, Object> sliceProfileAn = getSliceProfile( "AN", execution,null)
            saveSliceProfile(execution,"AN",sliceProfileAn)
        }

        if(!sliceTaskParams.getSliceProfileTn()){
            Map<String, Object> sliceProfileTn = getSliceProfile( "TN", execution,null)
            saveSliceProfile(execution,"TN",sliceProfileTn)
        }

        if(!sliceTaskParams.getSliceProfileCn()){
            Map<String, Object> sliceProfileCn = getSliceProfile( "CN", execution,null, )
            saveSliceProfile(execution,"CN",sliceProfileCn)
        }

        logger.debug("Finish parseServiceProfile")
    }

    private void saveSliceProfile(DelegateExecution execution, String domain, Map<String, Object> sliceProfile){
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        if(domain.equalsIgnoreCase("AN")){
            execution.setVariable("sliceProfileAn", sliceProfile)
            sliceTaskParams.setSliceProfileAn(sliceProfile)
            logger.debug("sliceProfileAn: " + sliceProfile)
        }
        else if(domain.equalsIgnoreCase("TN")){
            execution.setVariable("sliceProfileTn", sliceProfile)
            sliceTaskParams.setSliceProfileTn(sliceProfile)
            logger.debug("sliceProfileTn: " + sliceProfile)
        }
        else if(domain.equalsIgnoreCase("CN")){
            execution.setVariable("sliceProfileCn", sliceProfile)
            sliceTaskParams.setSliceProfileCn(sliceProfile)
            logger.debug("sliceProfileCn: " + sliceProfile)
        }
    }

    private void saveNSSIId(String domain, String nssiId, String nssiName, DelegateExecution execution) {
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        if(domain.equalsIgnoreCase("AN")){
            sliceTaskParams.setAnSuggestNssiId(nssiId)
            sliceTaskParams.setAnSuggestNssiName(nssiName)
        }
        else if(domain.equalsIgnoreCase("CN")){
            sliceTaskParams.setCnSuggestNssiId(nssiId)
            sliceTaskParams.setCnSuggestNssiName(nssiName)
        }
        else if(domain.equalsIgnoreCase("TN")){
            sliceTaskParams.setTnSuggestNssiId(nssiId)
            sliceTaskParams.setTnSuggestNssiName(nssiName)
        }
    }

    private Map getSliceProfile(String domain, DelegateExecution execution, Map<String, Object> oofSliceProfile) {
        String profileMapStr
        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")
        Integer domainLatency = (Integer) serviceProfile.get("latency")/3

        if(domain.equalsIgnoreCase("AN")){
            profileMapStr = """ {
                    "latency": ${domainLatency}, 
                    "sNSSAI": "sNSSAI", 
                    "uEMobilityLevel": "uEMobilityLevel", 
                    "coverageAreaTAList": "coverageAreaTAList", 
                    "5QI": 100
                }"""
        }
        else if(domain.equalsIgnoreCase("TN")){
            profileMapStr =""" {
                    "latency":${domainLatency},
                    "sNSSAI":"sNSSAI", 
                    "e2eLatency":"latency", 
                    "bandwidth": 100
                }"""
        }
        else if(domain.equalsIgnoreCase("CN")){
            profileMapStr = """ {
                    "areaTrafficCapDL":"areaTrafficCapDL",
                    "maxNumberofUEs":"maxNumberofUEs",
                    "latency":${domainLatency},
                    "expDataRateUL":"expDataRateUL", 
                    "sNSSAI":"sNSSAI", 
                    "areaTrafficCapUL":"areaTrafficCapUL",
                    "uEMobilityLevel":"uEMobilityLevel", 
                    "expDataRateDL":"expDataRateDL",  
                    "activityFactor":"activityFactor",
                    "resourceSharingLevel":"resourceSharingLevel"
                }"""
        }

        logger.debug("Profile map for " + domain + " : " + profileMapStr)
        Map<String, Object> profileMaps = objectMapper.readValue(profileMapStr.trim().replaceAll(" ", ""), new TypeReference<Map<String, String>>(){})
        Map<String, Object> sliceProfile = [:]
        for (Map.Entry<String, String> profileMap : profileMaps) {
            String key = profileMap.key
            String value = profileMaps.get(key)
            if(null != oofSliceProfile && oofSliceProfile.keySet().contains(key)){
                sliceProfile.put(key, oofSliceProfile.get(key))
                logger.debug("Get from oof, key:${key}, value: ${oofSliceProfile.get(key)}")
            }
            else if(serviceProfile.keySet().contains(value)){
                sliceProfile.put(key, serviceProfile.get(value))
            }
            else{
                sliceProfile.put(key, profileMaps.get(key))
            }
        }
        return sliceProfile
    }

    void processDecomposition(DelegateExecution execution){
        logger.debug("Start processDecomposition")

        ServiceDecomposition serviceDecomposition= execution.getVariable("serviceDecomposition")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        String nstName = serviceDecomposition.getModelInfo().getModelName()
        String nstId = serviceDecomposition.getModelInfo().getModelUuid()
        sliceTaskParams.setNstName(nstName)
        sliceTaskParams.setNstId(nstId)

        logger.debug("End processDecomposition")
    }


    void prepareNSTDecompose(DelegateExecution execution) {

        String modelUuid = execution.getVariable("nstModelUuid")
        String modelInvariantUuid = execution.getVariable("nstModelInvariantUuid")

        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)
    }

    void prepareNSSTDecompose(DelegateExecution execution) {
        Boolean isMoreNSSTtoProcess = false
        Integer maxNSST = execution.getVariable("maxNSST")
        Integer currentNSST=execution.getVariable("currentNSST")
        List<String> nsstModelUUIDList = new ArrayList<>()
        nsstModelUUIDList = execution.getVariable("nsstModelUUIDList")
        String modelUuid = nsstModelUUIDList.get(currentNSST)
        String serviceModelInfo = """{
            "modelInvariantUuid":"",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)
        currentNSST=currentNSST+1
        if(currentNSST<maxNSST)
            isMoreNSSTtoProcess=true
        execution.setVariable("isMoreNSSTtoProcess",isMoreNSSTtoProcess)
        execution.setVariable("maxNSST",maxNSST)
        execution.setVariable("currentNSST",currentNSST)
    }


    void prepareNSSTlistfromNST(DelegateExecution execution) {
        //Need to update this part from decomposition.
        logger.trace("Enter prepareNSSTlistfromNST()")
        Boolean isMoreNSSTtoProcess = false
        ServiceDecomposition serviceDecomposition= execution.getVariable("serviceDecomposition")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        String nstName = serviceDecomposition.getModelInfo().getModelName()
        sliceTaskParams.setNstName(nstName)
        String nstId = serviceDecomposition.getModelInfo().getModelUuid()
        sliceTaskParams.setNstId(nstId)
        execution.setVariable("sliceTaskParams",sliceTaskParams)

        List<ServiceProxy> proxyList = serviceDecomposition.getServiceProxy()
        List<String> nsstModelUUIDList = new ArrayList<>()
        for(ServiceProxy serviceProxy:proxyList)
            nsstModelUUIDList.add(serviceProxy.getSourceModelUuid())
        execution.setVariable("nsstModelUUIDList",nsstModelUUIDList)
        Integer maxNSST = nsstModelUUIDList.size()
        Integer currentNSST=0
        execution.setVariable("maxNSST",maxNSST)
        execution.setVariable("currentNSST",currentNSST)
        if(currentNSST<maxNSST)
            isMoreNSSTtoProcess=true
        execution.setVariable("isMoreNSSTtoProcess",isMoreNSSTtoProcess)
        logger.trace("Exit prepareNSSTlistfromNST()")

    }

    void getNSSTOption(DelegateExecution execution) {
        ServiceDecomposition serviceDecomposition= execution.getVariable("serviceDecomposition")
        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("subscriptionServiceType")
        String nssiInstanceId =""
        String nssiName =""
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        logger.debug( "get NSI option OOF Url: " + urlString)
        boolean isNSISuggested = false
        execution.setVariable("isNSISuggested",isNSISuggested)

        //Prepare auth for OOF - Begin
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.oof.auth", execution)
        String msokey = UrnPropertiesReader.getVariable("mso.msoKey", execution)

        String basicAuthValue = utils.encrypt(basicAuth, msokey)
        if (basicAuthValue != null) {
            logger.debug( "Obtained BasicAuth username and password for OOF: " + basicAuthValue)
            try {
                authHeader = utils.getBasicAuth(basicAuthValue, msokey)
                execution.setVariable("BasicAuthHeaderValue", authHeader)
            } catch (Exception ex) {
                logger.debug( "Unable to encode username and password string: " + ex)
                exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - Unable to " +
                        "encode username and password string")
            }
        } else {
            logger.debug( "Unable to obtain BasicAuth - BasicAuth value null")
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Internal Error - BasicAuth " +
                    "value null")
        }
        //Prepare auth for OOF - End
        //Prepare send request to OOF - Begin
        String requestId = execution.getVariable("msoRequestId")
        Map<String, Object> profileInfo = execution.getVariable("serviceProfile")
        String nsstModelInvariantUuid = serviceDecomposition.getModelInfo().getModelInvariantUuid()
        String nsstModelUuid = serviceDecomposition.getModelInfo().getModelUuid()
        String nsstInfo = """"NSSTInfo": {
        "invariantUUID":"${nsstModelInvariantUuid}",
        "UUID":"${nsstModelUuid}"
         }"""
        String oofRequest = oofUtils.buildSelectNSSIRequest(execution, requestId, nsstInfo ,profileInfo)


        URL url = new URL(urlString+"/api/oof/v1/selectnssi")
        HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.OOF)
        httpClient.addAdditionalHeader("Authorization", authHeader)
        Response httpResponse = httpClient.post(oofRequest)

        int responseCode = httpResponse.getStatus()
        logger.debug("OOF sync response code is: " + responseCode)

        if(responseCode != 200){
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
        }

        if(httpResponse.hasEntity()){
            String OOFResponse = httpResponse.readEntity(String.class)
            execution.setVariable("OOFResponse", OOFResponse)
            nssiInstanceId = jsonUtil.getJsonValue(OOFResponse, "NSSIIInfo.NSSIID")
            nssiName = jsonUtil.getJsonValue(OOFResponse, "NSSIInfo.NSSIName")
            execution.setVariable("nssiInstanceId",nssiInstanceId)
            execution.setVariable("nssiName",nssiName)
        }
        if(StringUtils.isBlank(nssiInstanceId)){
            logger.debug( "There is no valid NSST suggested by OOF.")
        }else
        {
            try {
                AAIResourcesClient resourceClient = new AAIResourcesClient()
                AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nssiInstanceId)
                AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
                Optional<ServiceInstance> si = wrapper.asBean(org.onap.aai.domain.yang.ServiceInstance.class)
                org.onap.aai.domain.yang.ServiceInstance nssi = si.get()

                String domain = nssi.getEnvironmentContext().toString().toUpperCase()
                switch (domain) {
                    case "AN":
                        sliceTaskParams.setAnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setAnSuggestNssiName(nssi.getServiceInstanceName())
                        break
                    case "CN":
                        sliceTaskParams.setCnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setCnSuggestNssiName(nssi.getServiceInstanceName())
                        break
                    case "TN":
                        sliceTaskParams.setTnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setTnSuggestNssiName(nssi.getServiceInstanceName())
                        break
                    default:
                        break
                }
            }catch(NotFoundException e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiInstanceId)
            }catch(Exception e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiInstanceId)
            }
        }
        logger.debug("Prepare NSSI option completed ")
    }


    /**
     * new
     */

    private static final ObjectMapper MAPPER = new ObjectMapper()

    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

    private static final String QUERY_SUB_NET_CAPABILITY = "/api/rest/provMns/v1/NSS/subnetCapabilityQuery"

    private static final String QUERY_NSSI_SELECTION_CAPABILITY = "/api/rest/provMns/v1/NSS/NSSISelectionCapability"

    /**
     * query Subnet Capability of TN
     * @param execution
     */
    public void queryTNSubnetCapability(DelegateExecution execution) {

        String vendor = execution.getVariable("vendor")

        List<String> subnetTypes =  new ArrayList<>()
        subnetTypes.add("TN_FH")
        subnetTypes.add("TN_MH")
        subnetTypes.add("TN_BH")

        String strRequest = MAPPER.writeValueAsString(
                buildQuerySubnetCapRequest(vendor, subnetTypes, NetworkType.TRANSPORT))

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_SUB_NET_CAPABILITY, strRequest)
        execution.setVariable("subnetCapabilityOfTN", response)
    }

    /**
     * query Subnet Capability of CN
     * @param execution
     */
    public void queryCNSubnetCapability(DelegateExecution execution) {

        String vendor = execution.getVariable("vendor")

        List<String> subnetTypes =  new ArrayList<>()
        subnetTypes.add("CN")

        String strRequest = MAPPER.writeValueAsString(buildQuerySubnetCapRequest(vendor, subnetTypes, NetworkType.CORE))

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_SUB_NET_CAPABILITY, strRequest)

        execution.setVariable("subnetCapabilityOfCN", response)
    }

    /**
     * query Subnet Capability of AN
     * @param execution
     */
    public void queryANSubnetCapability(DelegateExecution execution) {

        String vendor = execution.getVariable("vendor")

        List<String> subnetTypes =  new ArrayList<>()
        subnetTypes.add("AN-NF")

        String strRequest = MAPPER.writeValueAsString(buildQuerySubnetCapRequest(vendor, subnetTypes, NetworkType.ACCESS))

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_SUB_NET_CAPABILITY, strRequest)

        execution.setVariable("subnetCapabilityOfAN", response)
    }

    /**
     * build request body for querying Subnet Capability
     * @param vendor
     * @param subnetTypes
     * @param networkType
     * @return
     */
    private static String buildQuerySubnetCapRequest(String vendor, List<String> subnetTypes, NetworkType networkType) {
        NssmfAdapterNBIRequest request = new NssmfAdapterNBIRequest()

        Map<String, Object> paramMap = new HashMap()
        paramMap.put("subnetTypes", subnetTypes)

        request.setSubnetCapabilityQuery(MAPPER.writeValueAsString(paramMap))

        EsrInfo esrInfo = new EsrInfo()
        esrInfo.setVendor(vendor)
        esrInfo.setNetworkType(networkType)

        request.setEsrInfo(esrInfo)

        String strRequest = MAPPER.writeValueAsString(request)

        return strRequest
    }

    /**
     * handle response of Subnet Capability, generate SubnetCapabilities Info for request to oof
     * @param execution
     */
    public void generateSubnetCapabilities(DelegateExecution execution) {
        //todo:
        execution.setVariable("subnetCapabilities", [])
    }

    /**
     * prepare the params for decompose nst
     * @param execution
     */
    public void prepareDecomposeNST(DelegateExecution execution) {

        String modelUuid = execution.getVariable("nstModelUuid")
        String modelInvariantUuid = execution.getVariable("nstModelInvariantUuid")

        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("nstServiceModelInfo", serviceModelInfo)
    }

    /**
     * process the result of NST Decomposition
     * @param execution
     */
    public void processDecompositionNST(DelegateExecution execution) {

        ServiceDecomposition nstServiceDecomposition = execution.getVariable("nstServiceDecomposition")
        //todo:

    }


    /**
     * prepare the params for decompose nsst
     * @param execution
     */
    public void prepareDecomposeNSST(DelegateExecution execution) {
        Boolean isMoreNSSTtoProcess = false
        def maxNSST = execution.getVariable("maxNSST") as Integer
        def currentNSST = execution.getVariable("currentNSST") as Integer
        def nsstModelUUIDList = execution.getVariable("nsstModelUUIDList") as List
        String modelUuid = nsstModelUUIDList.get(currentNSST)
        String serviceModelInfo = """{
            "modelInvariantUuid":"",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("nsstServiceModelInfo", serviceModelInfo)
        currentNSST = currentNSST + 1

        if(currentNSST < maxNSST) {
            isMoreNSSTtoProcess = true
        }

        execution.setVariable("isMoreNSSTtoProcess", isMoreNSSTtoProcess)
        execution.setVariable("maxNSST", maxNSST)
        execution.setVariable("currentNSST", currentNSST)

        //todo:
    }

    /**
     * process the result of NSST Decomposition
     * @param execution
     */
    public void processDecompositionNSST(DelegateExecution execution) {
        ServiceDecomposition nsstServiceDecomposition = execution.getVariable("nsstServiceDecomposition")
        //todo:
    }

    /**
     * todo: need rewrite
     * prepare select nsi request
     * @param execution
     */
    public void preNSIRequest(DelegateExecution execution) {

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug( "get NSI option OOF Url: " + urlString)

        boolean isNSISuggested = true
        execution.setVariable("isNSISuggested", isNSISuggested)
        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSISelectionResponse"

        ServiceProfile profileInfo = execution.getVariable("serviceProfile") as ServiceProfile
        Map<String, Object> nstSolution = execution.getVariable("nstSolution") as Map
        logger.debug("Get NST selection from OOF: " + nstSolution.toString())

        execution.setVariable("nsiSelectionUrl", "/api/oof/selection/nsi/v1")
        execution.setVariable("nsiSelection_messageType", messageType)
        execution.setVariable("nsiSelection_correlator", requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution)
        execution.setVariable("nsiSelection_timeout", timeout)


        TemplateInfo nstInfo = new TemplateInfo()
        nstInfo.setInvariantUUID(nstSolution.get("invariantUUID") as String)
        nstInfo.setUUID(nstSolution.get("UUID") as String)
        nstInfo.setName(nstSolution.get("NSTName") as String)

        execution.setVariable("NSTInfo", nstInfo)

        List<TemplateInfo> nsstInfos = execution.getVariable("NSSTInfos") as List<TemplateInfo>

        List<SubnetCapability> subnetCapabilities = execution.getVariable("subnetCapabilities") as List<SubnetCapability>

        String oofRequest = oofUtils.buildSelectNSIRequest(requestId, nstInfo, nsstInfos,
                messageType, profileInfo, subnetCapabilities, timeout as Integer)

        execution.setVariable("nsiSelection_oofRequest", oofRequest)
        logger.debug("Sending request to OOF: " + oofRequest)
    }

    /**
     * todo: need rewrite
     * process select nsi response
     * @param execution
     */
    public void processNSIResp(DelegateExecution execution) {

        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams") as SliceTaskParams
        String OOFResponse = execution.getVariable("nsiSelection_oofResponse")
        logger.debug("NSI OOFResponse is: " + OOFResponse)
        execution.setVariable("OOFResponse", OOFResponse)
        //This needs to be changed to derive a value when we add policy to decide the solution options.

        Map<String, Object> resMap = objectMapper.readValue(OOFResponse, Map.class)
        List<Map<String, Object>> nsiSolutions = (List<Map<String, Object>>) resMap.get("solutions")
        Map<String, Object> solutions = nsiSolutions.get(0)

        String resourceSharingLevel = execution.getVariable("resourceSharingLevel")
        Boolean isSharable = resourceSharingLevel == "shared"

        if (solutions != null) {
            if (isSharable && solutions.get("existingNSI")) {
                //sharedNSISolution
                processSharedNSISolutions(solutions, execution)
            }
            else if(solutions.containsKey("newNSISolution")) {
                processNewNSISolutions(solutions, execution)
            }
        }
        execution.setVariable("sliceTaskParams", sliceTaskParams)
        logger.debug("sliceTaskParams: " + sliceTaskParams.convertToJson())
        logger.debug("*** Completed options Call to OOF ***")

        logger.debug("start parseServiceProfile")
        parseServiceProfile(execution)
        logger.debug("end parseServiceProfile")
    }

    /**
     * get NSSI Selection Capability for AN
     * @param execution
     */
    public void getNSSISelectionCap4AN(DelegateExecution execution) {

        def vendor = execution.getVariable("vendor") as String

        String strRequest = buildNSSISelectionReq(vendor, NetworkType.ACCESS)

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_NSSI_SELECTION_CAPABILITY, strRequest)

        Map<String, Object> resMap = objectMapper.readValue(response, Map.class)

        String selection = resMap.get("selection")


        if ("NSMF".equalsIgnoreCase(selection)) {
            execution.setVariable("NEED_AN_NSSI_SELECTION", true)
        }
    }

    /**
     * get NSSI Selection Capability for TN
     * @param execution
     */
    public void getNSSISelectionCap4TN(DelegateExecution execution) {

        def vendor = execution.getVariable("vendor") as String

        String strRequest = buildNSSISelectionReq(vendor, NetworkType.TRANSPORT)

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_NSSI_SELECTION_CAPABILITY, strRequest)

        Map<String, Object> resMap = objectMapper.readValue(response, Map.class)

        String selection = resMap.get("selection")

        if ("NSMF".equalsIgnoreCase(selection)) {
            execution.setVariable("NEED_TN_NSSI_SELECTION", true)
        }
    }

    /**
     * get NSSI Selection Capability for CN
     * @param execution
     */
    public void getNSSISelectionCap4CN(DelegateExecution execution) {

        def vendor = execution.getVariable("vendor") as String

        String strRequest = buildNSSISelectionReq(vendor, NetworkType.CORE)

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_NSSI_SELECTION_CAPABILITY, strRequest)

        Map<String, Object> resMap = objectMapper.readValue(response, Map.class)

        String selection = resMap.get("selection")

        if ("NSMF".equalsIgnoreCase(selection)) {
            execution.setVariable("NEED_CN_NSSI_SELECTION", true)
        }
    }

    /**
     * build NSSI Selection Capability Request body to nssmf adapter
     * @param vendor
     * @param networkType
     * @return
     */
    private static String buildNSSISelectionReq(String vendor, NetworkType networkType) {
        NssmfAdapterNBIRequest request = new NssmfAdapterNBIRequest()
        EsrInfo esrInfo = new EsrInfo()
        esrInfo.setVendor(vendor)
        esrInfo.setNetworkType(networkType)
        request.setEsrInfo(esrInfo)

        return MAPPER.writeValueAsString(request)
    }

    /**
     * if exist nssi need to select?
     * @param execution
     */
    public void handleNssiSelect(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceTaskParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        execution.setVariable()
    }

    /**
     * todo: need rewrite
     * prepare select nssi request
     * @param execution
     */
    public void preNSSIRequest(DelegateExecution execution) {

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug( "get NSI option OOF Url: " + urlString)

        boolean isNSISuggested = true
        execution.setVariable("isNSISuggested", isNSISuggested)
        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSISelectionResponse"

        Map<String, Object> profileInfo = execution.getVariable("serviceProfile") as Map
        Map<String, Object> nstSolution = execution.getVariable("nstSolution") as Map
        logger.debug("Get NST selection from OOF: " + nstSolution.toString())
        String nstInfo = """{
            "modelInvariantId":"${nstSolution.invariantUUID}",
            "modelVersionId":"${nstSolution.UUID}",
            "modelName":"${nstSolution.NSTName}"
         }"""

        execution.setVariable("nsiSelectionUrl", "/api/oof/selection/nsi/v1")
        execution.setVariable("nsiSelection_messageType", messageType)
        execution.setVariable("nsiSelection_correlator", requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution)
        execution.setVariable("nsiSelection_timeout", timeout)

        //todo
        String oofRequest = oofUtils.buildSelectNSIRequest(requestId, nstInfo, messageType, profileInfo)

        execution.setVariable("nsiSelection_oofRequest", oofRequest)
        logger.debug("Sending request to OOF: " + oofRequest)
    }

    /**
     * process select nssi response
     * @param execution
     */
    public void processNSSIResp(DelegateExecution execution) {

        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams") as SliceTaskParams
        String OOFResponse = execution.getVariable("nsiSelection_oofResponse")
        logger.debug("NSI OOFResponse is: " + OOFResponse)
        execution.setVariable("OOFResponse", OOFResponse)
        //This needs to be changed to derive a value when we add policy to decide the solution options.

        Map<String, Object> resMap = objectMapper.readValue(OOFResponse, Map.class)
        List<Map<String, Object>> nsiSolutions = (List<Map<String, Object>>) resMap.get("solutions")
        Map<String, Object> solutions = nsiSolutions.get(0)

        String resourceSharingLevel = execution.getVariable("resourceSharingLevel")
        Boolean isSharable = resourceSharingLevel == "shared"

        if (solutions != null) {
            if (isSharable && solutions.get("existingNSI")) {
                //sharedNSISolution
                processSharedNSISolutions(solutions, execution)
            }
            else if(solutions.containsKey("newNSISolution")) {
                processNewNSISolutions(solutions, execution)
            }
        }
        execution.setVariable("sliceTaskParams", sliceTaskParams)
        logger.debug("sliceTaskParams: "+sliceTaskParams.convertToJson())
        logger.debug("*** Completed options Call to OOF ***")

        logger.debug("start parseServiceProfile")
        parseServiceProfile(execution)
        logger.debug("end parseServiceProfile")
    }


}
