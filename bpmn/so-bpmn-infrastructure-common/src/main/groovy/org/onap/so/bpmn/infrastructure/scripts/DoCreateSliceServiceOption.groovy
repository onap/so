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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.beans.nsmf.AnSliceProfile
import org.onap.so.beans.nsmf.CnSliceProfile
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.NetworkType
import org.onap.so.beans.nsmf.NssmfAdapterNBIRequest
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter
import org.onap.so.beans.nsmf.TnSliceProfile
import org.onap.so.beans.nsmf.oof.SubnetCapability
import org.onap.so.beans.nsmf.oof.SubnetType
import org.onap.so.beans.nsmf.oof.TemplateInfo
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity

class DoCreateSliceServiceOption extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger(DoCreateSliceServiceOption.class)

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    OofUtils oofUtils = new OofUtils()

    private static final ObjectMapper objectMapper = new ObjectMapper()

    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

    private static final String QUERY_SUB_NET_CAPABILITY = "/api/rest/provMns/v1/NSS/subnetCapabilityQuery"

    private static final String QUERY_NSSI_SELECTION_CAPABILITY = "/api/rest/provMns/v1/NSS/NSSISelectionCapability"

    void preProcessRequest (DelegateExecution execution) {
    }

    /**
     * prepare the params for decompose nst
     * @param execution
     */
    public void prepareDecomposeNST(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceTaskParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        String modelUuid = sliceTaskParams.getNSTInfo().getUUID()
        String modelInvariantUuid = sliceTaskParams.getNSTInfo().getInvariantUUID()

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

        List<TemplateInfo> nsstInfos = new ArrayList<>()
        ServiceDecomposition nstServiceDecomposition =
                execution.getVariable("nstServiceDecomposition") as ServiceDecomposition
        //todo:
        List<AllottedResource> allottedResources = nstServiceDecomposition.getAllottedResources()
        for (AllottedResource allottedResource : allottedResources) {
            TemplateInfo nsstInfo = new TemplateInfo()
            nsstInfo.setUUID(allottedResource.getProvidingServiceModelUuid())
            nsstInfo.setInvariantUUID(allottedResource.getProvidingServiceModelInvariantUuid())
            nsstInfo.setName(allottedResource.getProvidingServiceModelName())
            nsstInfos.add(nsstInfo)
        }
        execution.setVariable("nsstInfos", nsstInfos)

        execution.setVariable("maxNsstIndex", allottedResources.size() - 1)
        execution.setVariable("currentNsstIndex", 0)

        List<ServiceDecomposition> nsstServiceDecompositions = new ArrayList<>()
        execution.setVariable("nsstServiceDecompositions", nsstServiceDecompositions)
    }

    /**
     * prepare the params for decompose nsst
     * @param execution
     */
    public void prepareDecomposeNSST(DelegateExecution execution) {

        List<TemplateInfo> nsstInfos = execution.getVariable("nsstInfos") as List<TemplateInfo>
        int index = execution.getVariable("currentNsstIndex") as Integer

        String modelUuid = nsstInfos.get(index).getUUID()
        String modelInvariantUuid = nsstInfos.get(index).getInvariantUUID()

        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("nsstServiceModelInfo", serviceModelInfo)

    }

    /**
     * process the result of NSST Decomposition
     * @param execution
     */
    public void processDecompositionNSST(DelegateExecution execution) {

        List<ServiceDecomposition> nsstServiceDecompositions =
                execution.getVariable("nsstServiceDecompositions") as List<ServiceDecomposition>

        ServiceDecomposition nsstServiceDecomposition =
                execution.getVariable("nsstServiceDecomposition") as ServiceDecomposition

        nsstServiceDecompositions.add(nsstServiceDecomposition)

        execution.setVariable("nsstServiceDecompositions", nsstServiceDecompositions)




        int num = execution.getVariable("maxNsstIndex") as Integer
        int index = execution.getVariable("currentNsstIndex") as Integer

        execution.setVariable("currentNsstIndex", index + 1)

        if (index >= num) {
            execution.setVariable("nsstHandleContinue", false)
        } else {
            execution.setVariable("nsstHandleContinue", true)
        }

    }

    /**
     * set nsst info to sliceTaskParams by type
     * @param execution
     */
    public void handleNsstByType(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        List<ServiceDecomposition> nsstServiceDecompositions =
                execution.getVariable("nsstServiceDecompositions") as List<ServiceDecomposition>

        List<SubnetCapability> subnetCapabilities = new ArrayList<>()



        for (ServiceDecomposition serviceDecomposition : nsstServiceDecompositions) {
            SubnetCapability subnetCapability = new SubnetCapability()
            handleByType(execution, serviceDecomposition, sliceParams, subnetCapability)
            subnetCapabilities.add(subnetCapability)
        }

        execution.setVariable("sliceTaskParams", sliceParams)
        execution.setVariable("subnetCapabilities", subnetCapabilities)
    }

    private void handleByType(DelegateExecution execution, ServiceDecomposition serviceDecomposition,
                              SliceTaskParamsAdapter sliceParams, SubnetCapability subnetCapability) {
        ModelInfo modelInfo = serviceDecomposition.getModelInfo()
        String vendor = serviceDecomposition.getServiceRole()
        SubnetType subnetType = convertServiceCategory(serviceDecomposition.getServiceCategory())

        switch (subnetType) {
            case SubnetType.TN_BH:
                sliceParams.tnBHSliceTaskInfo.vendor = vendor
                sliceParams.tnBHSliceTaskInfo.subnetType = subnetType
                sliceParams.tnBHSliceTaskInfo.networkType = subnetType.networkType
                sliceParams.tnBHSliceTaskInfo.NSSTInfo.UUID = modelInfo.getModelUuid()
                sliceParams.tnBHSliceTaskInfo.NSSTInfo.invariantUUID = modelInfo.getModelInvariantUuid()
                sliceParams.tnBHSliceTaskInfo.NSSTInfo.name = modelInfo.getModelName()

                break
            case SubnetType.TN_MH:
                sliceParams.tnMHSliceTaskInfo.vendor = vendor
                sliceParams.tnMHSliceTaskInfo.subnetType = subnetType
                sliceParams.tnMHSliceTaskInfo.networkType = subnetType.networkType
                sliceParams.tnMHSliceTaskInfo.NSSTInfo.UUID = modelInfo.getModelUuid()
                sliceParams.tnMHSliceTaskInfo.NSSTInfo.invariantUUID = modelInfo.getModelInvariantUuid()
                sliceParams.tnMHSliceTaskInfo.NSSTInfo.name = modelInfo.getModelName()

                break
            case SubnetType.AN_NF:
                sliceParams.anSliceTaskInfo.vendor = vendor
                sliceParams.anSliceTaskInfo.subnetType = subnetType
                sliceParams.anSliceTaskInfo.networkType = subnetType.networkType
                sliceParams.anSliceTaskInfo.NSSTInfo.UUID = modelInfo.getModelUuid()
                sliceParams.anSliceTaskInfo.NSSTInfo.invariantUUID = modelInfo.getModelInvariantUuid()
                sliceParams.anSliceTaskInfo.NSSTInfo.name = modelInfo.getModelName()
                break
            case SubnetType.CN:
                sliceParams.cnSliceTaskInfo.vendor = vendor
                sliceParams.cnSliceTaskInfo.subnetType = subnetType
                sliceParams.cnSliceTaskInfo.networkType = subnetType.networkType
                sliceParams.cnSliceTaskInfo.NSSTInfo.UUID = modelInfo.getModelUuid()
                sliceParams.cnSliceTaskInfo.NSSTInfo.invariantUUID = modelInfo.getModelInvariantUuid()
                sliceParams.cnSliceTaskInfo.NSSTInfo.name = modelInfo.getModelName()
                break
            default:
                subnetType = null
                break

        //todo

        }
        if (null == subnetType) {
            def msg = "Get subnetType failed, modelUUId=" + modelInfo.getModelUuid()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
        }
        String response = querySubnetCapability(execution, vendor, subnetType)
        ResponseEntity responseEntity = objectMapper.readValue(response, ResponseEntity.class)

        Map<String, Object> result = responseEntity.getBody() as Map
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            subnetCapability.setDomainType(entry.getKey())
            subnetCapability.setCapabilityDetails(entry.getValue())
        }
    }

    /**
     * get subnetType from serviceCategory
     * @return
     */
    private SubnetType convertServiceCategory(String serviceCategory){
        if(serviceCategory ==~ /CN.*/){
            return SubnetType.CN
        }
        if (serviceCategory ==~ /AN.*NF.*/){
            return SubnetType.AN_NF
        }
        if (serviceCategory ==~ /TN.*BH.*/){
            return SubnetType.TN_BH
        }
        if(serviceCategory ==~ /TN.*MH.*/){
            return SubnetType.TN_MH
        }
        return null
    }

    /**
     * query Subnet Capability of TN AN CN
     * @param execution
     */
    private String querySubnetCapability(DelegateExecution execution, String vendor, SubnetType subnetType) {

        String strRequest = objectMapper.writeValueAsString(buildQuerySubnetCapRequest(vendor, subnetType))

        String response = nssmfAdapterUtils.sendPostRequestNSSMF(execution, QUERY_SUB_NET_CAPABILITY, strRequest)
        return response
    }

    /**
     * build request body for querying Subnet Capability
     * @param vendor
     * @param subnetTypes
     * @param networkType
     * @return
     */
    private static String buildQuerySubnetCapRequest(String vendor, SubnetType subnetType) {
        NssmfAdapterNBIRequest request = new NssmfAdapterNBIRequest()

        List<String> subnetTypes =  new ArrayList<>()
        subnetTypes.add(subnetType.subnetType)
        Map<String, Object> paramMap = new HashMap()
        paramMap.put("subnetTypes", subnetTypes)

        request.setSubnetCapabilityQuery(objectMapper.writeValueAsString(paramMap))

        EsrInfo esrInfo = new EsrInfo()
        esrInfo.setVendor(vendor)
        esrInfo.setNetworkType(subnetType.networkType)

        request.setEsrInfo(esrInfo)

        String strRequest = objectMapper.writeValueAsString(request)

        return strRequest
    }

    /**
     * todo: need rewrite
     * prepare select nsi request
     * @param execution
     */
    public void preNSIRequest(DelegateExecution execution, boolean preferReuse) {

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug( "get NSI option OOF Url: " + urlString)

        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSISelectionResponse"

        execution.setVariable("nsiSelectionUrl", "/api/oof/selection/nsi/v1")
        execution.setVariable("nsiSelection_messageType", messageType)
        execution.setVariable("nsiSelection_correlator", requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution)
        execution.setVariable("nsiSelection_timeout", timeout)

        SliceTaskParamsAdapter sliceParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        Map<String, Object> profileInfo = sliceParams.getServiceProfile()
        TemplateInfo nstInfo = sliceParams.getNSTInfo()

        List<TemplateInfo> nsstInfos = execution.getVariable("nsstInfos") as List<TemplateInfo>

        List<SubnetCapability> subnetCapabilities =
                execution.getVariable("subnetCapabilities") as List<SubnetCapability>

        String oofRequest = oofUtils.buildSelectNSIRequest(requestId, nstInfo, nsstInfos,
                messageType, profileInfo, subnetCapabilities, timeout as Integer, preferReuse)

        execution.setVariable("nsiSelection_oofRequest", oofRequest)
        logger.debug("Sending request to OOF: " + oofRequest)
    }

    /**
     * todo: need rewrite
     * process select nsi response
     * @param execution
     */
    public void processNSIResp(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceTaskParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        String oofResponse = execution.getVariable("nsiSelection_oofResponse")
        logger.debug("NSI oofResponse is: " + oofResponse)
        execution.setVariable("oofResponse", oofResponse)
        //This needs to be changed to derive a value when we add policy to decide the solution options.

        Map<String, Object> resMap = objectMapper.readValue(oofResponse, Map.class)
        List<Map<String, Object>> nsiSolutions = (List<Map<String, Object>>) resMap.get("solutions")
        Map<String, Object> solution = nsiSolutions.get(0)

        String resourceSharingLevel = execution.getVariable("resourceSharingLevel")
        Boolean isSharable = resourceSharingLevel == "shared"

        if (solution != null) {
            if (isSharable && solution.get("existingNSI")) {
                //sharedNSISolution
                processSharedNSI(solution, sliceTaskParams)
            }
            else if(solution.containsKey("newNSISolution")) {
                processNewNSI(solution, sliceTaskParams)
            }
        }
        execution.setVariable("sliceTaskParams", sliceTaskParams)
        //logger.debug("sliceTaskParams: " + sliceTaskParams.convertToJson())
        logger.debug("*** Completed options Call to OOF ***")
    }

    private void processSharedNSI(Map<String, Object> solution, SliceTaskParamsAdapter sliceParams) {
        Map<String, Object> sharedNSISolution = solution.get("sharedNSISolution") as Map

        String nsiId = sharedNSISolution.get("NSIId")
        String nsiName = sharedNSISolution.get("NSIName")
        sliceParams.setSuggestNsiId(nsiId)
        sliceParams.setSuggestNsiName(nsiName)
    }

    private void processNewNSI(Map<String, Object> solution, SliceTaskParamsAdapter sliceParams) {
        Map<String, Object> newNSISolution = solution.get("newNSISolution") as Map
        List<Map> sliceProfiles = newNSISolution.get("sliceProfiles") as List<Map>
        for (Map sliceProfile : sliceProfiles) {
            String domainType = sliceProfile.get("domainType")
            switch (domainType.toLowerCase()) {
                case "tn-bh":
                    sliceParams.tnBHSliceTaskInfo.sliceProfile = sliceProfile as TnSliceProfile
                    break
                case "an-nf":
                    sliceParams.anSliceTaskInfo.sliceProfile = sliceProfile as AnSliceProfile
                    break
                case "cn":
                    sliceParams.cnSliceTaskInfo.sliceProfile = sliceProfile as CnSliceProfile
                    break
                default:
                    break
            }

            //todo

        }
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

        return objectMapper.writeValueAsString(request)
    }

    /**
     * if exist nssi need to select?
     * @param execution
     */
    public void handleNssiSelect(DelegateExecution execution) {

        SliceTaskParamsAdapter sliceTaskParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter

        //todo
        boolean needCnNssiSelection = execution.getVariable("NEED_CN_NSSI_SELECTION") as Boolean
        boolean needAnNssiSelection = execution.getVariable("NEED_AN_NSSI_SELECTION") as Boolean
        boolean needTnNssiSelection = execution.getVariable("NEED_TN_NSSI_SELECTION") as Boolean

        /**
         * [
         * ​	{
         * ​		"subType":  subtype,
         * ​		"nsstInfo": object,
         * ​		"sliceProfile": object
         * ​	},
         *      {
         *          "subType":  subtype,
         *          "nsstInfo": object,
         *          "sliceProfile": object
         *      }
         * ]
         */
        List<Map> nssiNeedHandlerInfos = new ArrayList<>()
        Map<String, Object> nssiNeedHandlerMap = new HashMap()

        //List<TemplateInfo> nssiNeedHandlers = new ArrayList<>()
        //List<Object> nssiProfileNeedHandlers = new ArrayList<>()
        if (needCnNssiSelection) {
            nssiNeedHandlerMap.put("subnetType", sliceTaskParams.cnSliceTaskInfo.subnetType)
            nssiNeedHandlerMap.put("nsstInfo", sliceTaskParams.cnSliceTaskInfo.NSSTInfo)
            nssiNeedHandlerMap.put("sliceProfile", sliceTaskParams.cnSliceTaskInfo.sliceProfile)
            nssiNeedHandlerInfos.add(nssiNeedHandlerMap)
        }
        if (needAnNssiSelection) {
            nssiNeedHandlerMap.clear()
            nssiNeedHandlerMap.put("subnetType", sliceTaskParams.anSliceTaskInfo.subnetType)
            nssiNeedHandlerMap.put("nsstInfo", sliceTaskParams.anSliceTaskInfo.NSSTInfo)
            nssiNeedHandlerMap.put("sliceProfile", sliceTaskParams.anSliceTaskInfo.sliceProfile)
            nssiNeedHandlerInfos.add(nssiNeedHandlerMap)
        }
        if (needTnNssiSelection) {
            nssiNeedHandlerMap.clear()
            nssiNeedHandlerMap.put("subnetType", sliceTaskParams.tnBHSliceTaskInfo.subnetType)
            nssiNeedHandlerMap.put("nsstInfo", sliceTaskParams.tnBHSliceTaskInfo.NSSTInfo)
            nssiNeedHandlerMap.put("sliceProfile", sliceTaskParams.tnBHSliceTaskInfo.sliceProfile)
            nssiNeedHandlerInfos.add(nssiNeedHandlerMap)

            nssiNeedHandlerMap.clear()
            nssiNeedHandlerMap.put("subnetType", sliceTaskParams.tnMHSliceTaskInfo.subnetType)
            nssiNeedHandlerMap.put("nsstInfo", sliceTaskParams.tnMHSliceTaskInfo.NSSTInfo)
            nssiNeedHandlerMap.put("sliceProfile", sliceTaskParams.tnMHSliceTaskInfo.sliceProfile)
            nssiNeedHandlerInfos.add(nssiNeedHandlerMap)

            nssiNeedHandlerMap.clear()
            nssiNeedHandlerMap.put("subnetType", sliceTaskParams.tnFHSliceTaskInfo.subnetType)
            nssiNeedHandlerMap.put("nsstInfo", sliceTaskParams.tnFHSliceTaskInfo.NSSTInfo)
            nssiNeedHandlerMap.put("sliceProfile", sliceTaskParams.tnFHSliceTaskInfo.sliceProfile)
            nssiNeedHandlerInfos.add(nssiNeedHandlerMap)

        }

        if (nssiNeedHandlerInfos.size() > 0) {
            execution.setVariable("needSelectNssi", true)
            execution.setVariable("currNssiIndex", 0)
            execution.setVariable("nssiNeedHandlerInfos", nssiNeedHandlerInfos)
        } else {
            execution.setVariable("needSelectNssi", false)
        }

        execution.setVariable("sliceTaskParams", sliceTaskParams)
    }

    /**
     * prepare select nssi request
     * @param execution
     */
    public void preNSSIRequest(DelegateExecution execution) {

        List<Map> nssiNeedHandlerInfos =
                execution.getVariable("nssiNeedHandlerInfos") as List<Map>

        int currNssiIndex = execution.getVariable("currNssiIndex") as Integer
        Map nssiNeedHandlerInfo = nssiNeedHandlerInfos.get(currNssiIndex) as Map

        TemplateInfo nsstInfo = nssiNeedHandlerInfo.get("nsstInfo") as TemplateInfo
        Object profileInfo = nssiNeedHandlerInfo.get("sliceProfile")

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug( "get NSI option OOF Url: " + urlString)

        String requestId = execution.getVariable("msoRequestId")
        String messageType = "NSSISelectionResponse"

        execution.setVariable("nssiSelectionUrl", "/api/oof/selection/nssi/v1")
        execution.setVariable("nssiSelection_messageType", messageType)
        execution.setVariable("nssiSelection_correlator", requestId)
        String timeout = UrnPropertiesReader.getVariable("mso.adapters.oof.timeout", execution)
        execution.setVariable("nssiSelection_timeout", timeout)

        String oofRequest = oofUtils.buildSelectNSSIRequest(requestId, nsstInfo, messageType,
                profileInfo, timeout as Integer)

        execution.setVariable("nssiSelection_oofRequest", oofRequest)
        logger.debug("Sending request to OOF: " + oofRequest)
    }

    /**
     * process select nssi response
     * @param execution
     */
    public void processNSSIResp(DelegateExecution execution) {

        List<Map> nssiNeedHandlerInfos =
                execution.getVariable("nssiNeedHandlerInfos") as List<Map>

        int currNssiIndex = execution.getVariable("currNssiIndex") as Integer
        Map nssiNeedHandlerInfo = nssiNeedHandlerInfos.get(currNssiIndex) as Map
        SubnetType subnetType = nssiNeedHandlerInfo.get("subnetType") as SubnetType

        SliceTaskParamsAdapter sliceTaskParams =
                execution.getVariable("sliceTaskParams") as SliceTaskParamsAdapter


        String OOFResponse = execution.getVariable("nssiSelection_oofResponse")
        logger.debug("NSI OOFResponse is: " + OOFResponse)
        execution.setVariable("OOFResponse", OOFResponse)
        //This needs to be changed to derive a value when we add policy to decide the solution options.

        Map<String, Object> resMap = objectMapper.readValue(OOFResponse, Map.class)
        List<Map<String, Object>> nsiSolutions = (List<Map<String, Object>>) resMap.get("solutions")
        Map<String, Object> solution = nsiSolutions.get(0)

        String resourceSharingLevel = execution.getVariable("resourceSharingLevel")
        Boolean isSharable = resourceSharingLevel == "shared"   //todo

        if (isSharable && solution != null) {
            processNssiResult(sliceTaskParams, subnetType, solution)
        }

        execution.setVariable("sliceTaskParams", sliceTaskParams)
        //logger.debug("sliceTaskParams: "+ sliceTaskParams.convertToJson())
        logger.debug("*** Completed options Call to OOF ***")

        logger.debug("start parseServiceProfile")
        //parseServiceProfile(execution)
        logger.debug("end parseServiceProfile")

        if (currNssiIndex >= nssiNeedHandlerInfos.size() - 1) {
            execution.setVariable("needSelectNssi", false)
        } else {
            execution.setVariable("currNssiIndex", currNssiIndex + 1)
            execution.setVariable("needSelectNssi", true)
        }

    }

    private void processNssiResult(SliceTaskParamsAdapter sliceTaskParams, SubnetType subnetType,
                                   Map<String, Object> solution) {
        switch (subnetType) {
            case SubnetType.CN:
                sliceTaskParams.cnSliceTaskInfo.suggestNssiId = solution.get("NSSIId")
                sliceTaskParams.cnSliceTaskInfo.suggestNssiName = solution.get("NSSIName")
                break
            case SubnetType.AN_NF:
                sliceTaskParams.anSliceTaskInfo.suggestNssiId = solution.get("NSSIId")
                sliceTaskParams.anSliceTaskInfo.suggestNssiName = solution.get("NSSIName")
                break
            case SubnetType.TN_BH:
                sliceTaskParams.tnBHSliceTaskInfo.suggestNssiId = solution.get("NSSIId")
                sliceTaskParams.tnBHSliceTaskInfo.suggestNssiName = solution.get("NSSIName")
                break
            case SubnetType.TN_FH:
                sliceTaskParams.tnFHSliceTaskInfo.suggestNssiId = solution.get("NSSIId")
                sliceTaskParams.tnFHSliceTaskInfo.suggestNssiName = solution.get("NSSIName")
                break
            case SubnetType.TN_MH:
                sliceTaskParams.tnMHSliceTaskInfo.suggestNssiId = solution.get("NSSIId")
                sliceTaskParams.tnMHSliceTaskInfo.suggestNssiName = solution.get("NSSIName")
                break
        }
    }

}
