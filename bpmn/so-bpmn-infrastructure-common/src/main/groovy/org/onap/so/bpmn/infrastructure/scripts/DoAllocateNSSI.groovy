package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aai.domain.yang.SliceProfile
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.AllocateAnNssi
import org.onap.so.beans.nsmf.AllocateCnNssi
import org.onap.so.beans.nsmf.AllocateTnNssi
import org.onap.so.beans.nsmf.AnSliceProfile
import org.onap.so.beans.nsmf.CnSliceProfile
import org.onap.so.beans.nsmf.EsrInfo
import org.onap.so.beans.nsmf.JobStatusRequest
import org.onap.so.beans.nsmf.NetworkType
import org.onap.so.beans.nsmf.NsiInfo
import org.onap.so.beans.nsmf.NssiAllocateRequest
import org.onap.so.beans.nsmf.PerfReq
import org.onap.so.beans.nsmf.PerfReqEmbbList
import org.onap.so.beans.nsmf.PerfReqUrllcList
import org.onap.so.beans.nsmf.ResourceSharingLevel
import org.onap.so.beans.nsmf.ServiceProfile
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.beans.nsmf.TnSliceProfile
import org.onap.so.beans.nsmf.UeMobilityLevel
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.core.Response

import static org.apache.commons.lang3.StringUtils.isBlank


class DoAllocateNSSI extends org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoAllocateNSSI.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    void preProcessRequest (DelegateExecution execution) {
        logger.trace("Enter preProcessRequest()")
        String msg = ""
        String nssmfOperation = ""
        String msoRequestId = execution.getVariable("msoRequestId")
        String nsstInput = execution.getVariable("nsstInput")
        String modelUuid = jsonUtil.getJsonValue(nsstInput, "modelUuid")
        //modelUuid="2763777c-27bd-4df7-93b8-c690e23f4d3f"
        String nssiInstanceId = jsonUtil.getJsonValue(nsstInput, "serviceInstanceId")
        String serviceModelInfo = """{
            "modelInvariantUuid":"",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo",serviceModelInfo)
        execution.setVariable("nssiInstanceId",nssiInstanceId)
        String nssiProfileID = UUID.randomUUID().toString()
        execution.setVariable("nssiProfileID",nssiProfileID)
        if(isBlank(nssiInstanceId))
        {
            nssmfOperation="create"
            nssiInstanceId = UUID.randomUUID().toString()
        }else {
            nssmfOperation = "update"
        }
        execution.setVariable("nssmfOperation",nssmfOperation)
        execution.setVariable("nssiInstanceId",nssiInstanceId)

        def isDebugLogEnabled ="false"
        def isNSSICreated = false
        execution.setVariable("isNSSICreated",isNSSICreated)

        int currentCycle = 0
        execution.setVariable("currentCycle", currentCycle)

        logger.trace("Exit preProcessRequest")
    }


    void getNSSTInfo(DelegateExecution execution){
        logger.trace("Enter getNSSTInfo in DoAllocateNSSI()")
        ServiceDecomposition serviceDecomposition= execution.getVariable("serviceDecomposition")
        ModelInfo modelInfo = serviceDecomposition.getModelInfo()
        String serviceRole = "nssi"
        String nssiServiceInvariantUuid = serviceDecomposition.modelInfo.getModelInvariantUuid()
        String nssiServiceUuid = serviceDecomposition.modelInfo.getModelUuid()
        String nssiServiceType = serviceDecomposition.getServiceType()
        String uuiRequest = execution.getVariable("uuiRequest")
        String nssiServiceName = "nssi_"+jsonUtil.getJsonValue(uuiRequest, "service.name")
        execution.setVariable("nssiServiceName",nssiServiceName)
        execution.setVariable("nssiServiceType",nssiServiceType)
        execution.setVariable("nssiServiceInvariantUuid",nssiServiceInvariantUuid)
        execution.setVariable("nssiServiceUuid",nssiServiceUuid)
        execution.setVariable("serviceRole",serviceRole)

        String content = serviceDecomposition.getServiceInfo().getServiceArtifact().get(0).getContent()
        String nsstID = jsonUtil.getJsonValue(content, "metadata.id")
        String nsstVendor = jsonUtil.getJsonValue(content, "metadata.vendor")
        String nsstDomain = jsonUtil.getJsonValue(content, "metadata.domainType")
        String nsstType = jsonUtil.getJsonValue(content, "metadata.type")

        execution.setVariable("nsstID",nsstID)
        execution.setVariable("nsstVendor",nsstVendor)
        execution.setVariable("nsstDomain",nsstDomain)
        execution.setVariable("nssiServiceUuid",nssiServiceUuid)
        execution.setVariable("nsstType",nsstType)

        String nsstContentInfo = """{
        "NsstID":"${nsstID}",
        "Vendor":"${nsstVendor}",
        "type":"${nsstType}"
         }"""

        logger.trace("Exit getNSSTInfo in DoAllocateNSSI()")
    }

    void timeDelay(DelegateExecution execution) {
        logger.trace("Enter timeDelay in DoAllocateNSSI()")
        try {
            Thread.sleep(60000);
            int currentCycle = execution.getVariable("currentCycle")
            currentCycle=currentCycle+1
            if(currentCycle>60)
            {
                logger.trace("Completed all the retry times... but still nssmf havent completed the creation process...")
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, "NSSMF creation didnt complete by time...")
            }
            execution.setVariable("currentCycle",currentCycle)
        } catch(InterruptedException e) {
            logger.info("Time Delay exception" + e)
        }
        logger.trace("Exit timeDelay in DoAllocateNSSI()")
    }


    void sendUpdateRequestNSSMF(DelegateExecution execution) {
        logger.trace("Enter sendUpdateRequestNSSMF in DoAllocateNSSI()")
        String urlString = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint", execution)
        logger.debug( "get NSSMF: " + urlString)

       //Prepare auth for NSSMF - Begin
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.nssmf.auth", execution)
        String domain = execution.getVariable("nsstDomain")
        String nssmfRequest = buildUpdateNSSMFRequest(execution, domain.toUpperCase())

        //send request to update NSSI option - Begin
        URL url = new URL(urlString+"/api/rest/provMns/v1/NSS/SliceProfiles")
        HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.EXTERNAL)
        Response httpResponse = httpClient.post(nssmfRequest)

        int responseCode = httpResponse.getStatus()
        logger.debug("NSSMF sync response code is: " + responseCode)

        if(responseCode < 199 && responseCode > 299){
            String nssmfResponse ="NSSMF response have nobody"
            if(httpResponse.hasEntity())
                nssmfResponse = httpResponse.readEntity(String.class)
            logger.trace("received error message from NSSMF : "+nssmfResponse)
            logger.trace("Exit sendCreateRequestNSSMF in DoAllocateNSSI()")
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from NSSMF.")
        }

        if(httpResponse.hasEntity()){
            String nssmfResponse = httpResponse.readEntity(String.class)
            execution.setVariable("nssmfResponse", nssmfResponse)
            String nssiId = jsonUtil.getJsonValue(nssmfResponse, "nssiId")
            String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
            execution.setVariable("nssiId",nssiId)
            execution.setVariable("jobId",jobId)
        }else{
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from NSSMF.")
        }
        logger.trace("Exit sendUpdateRequestNSSMF in DoAllocateNSSI()")
    }

    void sendCreateRequestNSSMF(DelegateExecution execution) {
        logger.trace("Enter sendCreateRequestNSSMF in DoAllocateNSSI()")
        String urlString = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint", execution)
        logger.debug( "get NSSMF: " + urlString)

        //Prepare auth for NSSMF - Begin
        String domain = execution.getVariable("nsstDomain")
        String nssmfRequest = buildCreateNSSMFRequest(execution, domain.toUpperCase())

        //send request to get NSI option - Begin
        URL url = new URL(urlString+"/api/rest/provMns/v1/NSS/SliceProfiles")
        HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.EXTERNAL)
        Response httpResponse = httpClient.post(nssmfRequest)

        int responseCode = httpResponse.getStatus()
        logger.debug("NSSMF sync response code is: " + responseCode)

        if(responseCode < 199 || responseCode > 299 ){
            String nssmfResponse ="NSSMF response have nobody"
            if(httpResponse.hasEntity())
                nssmfResponse = httpResponse.readEntity(String.class)
            logger.trace("received error message from NSSMF : "+nssmfResponse)
            logger.trace("Exit sendCreateRequestNSSMF in DoAllocateNSSI()")
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from NSSMF.")
        }

        if(httpResponse.hasEntity()){
            String nssmfResponse = httpResponse.readEntity(String.class)
            execution.setVariable("nssmfResponse", nssmfResponse)
            String nssiId = jsonUtil.getJsonValue(nssmfResponse, "nssiId")
            String jobId = jsonUtil.getJsonValue(nssmfResponse, "jobId")
            execution.setVariable("nssiId",nssiId)
            execution.setVariable("jobId",jobId)
        }else{
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from NSSMF.")
        }
        logger.trace("Exit sendCreateRequestNSSMF in DoAllocateNSSI()")

    }

    void getNSSMFProgresss(DelegateExecution execution) {
        logger.trace("Enter getNSSMFProgresss in DoAllocateNSSI()")

        String endpoint = UrnPropertiesReader.getVariable("mso.adapters.nssmf.endpoint", execution)
        logger.debug( "get NSSMF: " + endpoint)

        //Prepare auth for NSSMF - Begin
        def authHeader = ""
        String basicAuth = UrnPropertiesReader.getVariable("mso.nssmf.auth", execution)

        String nssmfRequest = buildNSSMFProgressRequest(execution)
        String strUrl="/api/rest/provMns/v1/NSS/jobs/"+execution.getVariable("jobId")
        //send request to update NSSI option - Begin
        URL url = new URL(endpoint+strUrl)
        HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.EXTERNAL)
        Response httpResponse = httpClient.post(nssmfRequest)

        int responseCode = httpResponse.getStatus()
        logger.debug("NSSMF sync response code is: " + responseCode)

        if(responseCode < 199 || responseCode > 299){
            String nssmfResponse ="NSSMF response have nobody"
            if(httpResponse.hasEntity())
                nssmfResponse = httpResponse.readEntity(String.class)
            logger.trace("received error message from NSSMF : "+nssmfResponse)
            logger.trace("Exit sendCreateRequestNSSMF in DoAllocateNSSI()")
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from NSSMF.")
        }

        if(httpResponse.hasEntity()){
            String nssmfResponse = httpResponse.readEntity(String.class)
            Boolean isNSSICreated = false
            execution.setVariable("nssmfResponse", nssmfResponse)
            Integer progress = java.lang.Integer.parseInt(jsonUtil.getJsonValue(nssmfResponse, "responseDescriptor.progress"))
            String status = jsonUtil.getJsonValue(nssmfResponse, "responseDescriptor.status")
            String statusDescription = jsonUtil.getJsonValue(nssmfResponse, "responseDescriptor.statusDescription")
            execution.setVariable("nssmfProgress",progress)
            execution.setVariable("nssmfStatus",status)
            execution.setVariable("nddmfStatusDescription",statusDescription)
            if(progress>99)
                isNSSICreated = true
            execution.setVariable("isNSSICreated",isNSSICreated)

        }else{
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from NSSMF.")
        }
        logger.trace("Exit getNSSMFProgresss in DoAllocateNSSI()")

    }

    void updateRelationship(DelegateExecution execution) {
        logger.trace("Enter updateRelationship in DoAllocateNSSI()")
        String nssiInstanceId = execution.getVariable("nssiInstanceId")
        String nsiInstanceId = execution.getVariable("nsiServiceInstanceId")
        try{
            AAIResourceUri nsiServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nsiInstanceId);
            AAIResourceUri nssiServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiInstanceId)
            getAAIClient().connect(nsiServiceuri, nssiServiceUri, AAIEdgeLabel.COMPOSED_OF);
        }catch(Exception ex) {
            String msg = "Exception in DoAllocateNSSI InstantiateNSSI service while creating relationship " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.trace("Exit updateRelationship in DoAllocateNSSI()")
    }


    void instantiateNSSIService(DelegateExecution execution) {
        logger.trace("Enter instantiateNSSIService in DoAllocateNSSI()")
        //String nssiInstanceId = execution.getVariable("nssiInstanceId")
        String nssiInstanceId = execution.getVariable("nssiId")
        execution.setVariable("nssiInstanceId",nssiInstanceId)
        String sliceInstanceId = execution.getVariable("nsiServiceInstanceId")
        try {
            org.onap.aai.domain.yang.ServiceInstance nssi = new ServiceInstance();
            Map<String, Object> serviceProfileMap = execution.getVariable("serviceProfile")

            nssi.setServiceInstanceId(nssiInstanceId)
            nssi.setServiceInstanceName(execution.getVariable("nssiServiceName"))
            //nssi.setServiceType(execution.getVariable("nssiServiceType"))
            nssi.setServiceType(serviceProfileMap.get("sST").toString())
            String serviceStatus = "deactivated"
            nssi.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = execution.getVariable("nssiServiceInvariantUuid")
            String modelUuid = execution.getVariable("nssiServiceUuid")
            nssi.setModelInvariantId(modelInvariantUuid)
            nssi.setModelVersionId(modelUuid)
            String uuiRequest = execution.getVariable("uuiRequest")
            String serviceInstanceLocationid = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.plmnIdList")
            nssi.setServiceInstanceLocationId(serviceInstanceLocationid)
            //String snssai = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.sNSSAI")
            String envContext=execution.getVariable("nsstDomain")
            nssi.setEnvironmentContext(envContext)
            nssi.setServiceRole(execution.getVariable("serviceRole"))
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), nssiInstanceId)
            client.create(uri, nssi)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        try{
            AAIResourceUri nsiServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, sliceInstanceId);
            AAIResourceUri nssiServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiInstanceId)
            getAAIClient().connect(nsiServiceuri, nssiServiceUri, AAIEdgeLabel.COMPOSED_OF);
        }catch(Exception ex) {
            String msg = "Exception in DoAllocateNSSI InstantiateNSSI service while creating relationship " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }



        def rollbackData = execution.getVariable("RollbackData")
        if (rollbackData == null) {
            rollbackData = new RollbackData();
        }
        //rollbackData.put("SERVICEINSTANCE", "disableRollback", idisableRollback.toStrng())
        rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
        rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", nssiInstanceId)
        rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
        rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
        execution.setVariable("rollbackData", rollbackData)
        execution.setVariable("RollbackData", rollbackData)
        logger.debug("RollbackData:" + rollbackData)
        logger.trace("Exit instantiateNSSIService in DoAllocateNSSI()")
    }


    void createSliceProfile(DelegateExecution execution) {
        logger.trace("Enter createSliceProfile in DoAllocateNSSI()")
        String sliceserviceInstanceId = execution.getVariable("nssiInstanceId")
        String nssiProfileID = execution.getVariable("nssiProfileID")
        Map<String, Object> sliceProfileMap = execution.getVariable("sliceProfileCn")
        Map<String, Object> serviceProfileMap = execution.getVariable("serviceProfile")
        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setServiceAreaDimension("")
        sliceProfile.setPayloadSize(0)
        sliceProfile.setJitter(0)
        sliceProfile.setSurvivalTime(0)
        //sliceProfile.setCsAvailability()
        //sliceProfile.setReliability()
        sliceProfile.setExpDataRate(0)
        sliceProfile.setTrafficDensity(0)
        sliceProfile.setConnDensity(0)
        sliceProfile.setExpDataRateUL(Integer.parseInt(sliceProfileMap.get("expDataRateUL").toString()))
        sliceProfile.setExpDataRateDL(Integer.parseInt(sliceProfileMap.get("expDataRateDL").toString()))
        sliceProfile.setActivityFactor(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        sliceProfile.setResourceSharingLevel(sliceProfileMap.get("activityFactor").toString())
        sliceProfile.setUeMobilityLevel(serviceProfileMap.get("uEMobilityLevel").toString())
        sliceProfile.setCoverageAreaTAList(serviceProfileMap.get("coverageAreaTAList").toString())
        sliceProfile.setMaxNumberOfUEs(Integer.parseInt(sliceProfileMap.get("activityFactor").toString()))
        sliceProfile.setLatency(Integer.parseInt(sliceProfileMap.get("latency").toString()))
        sliceProfile.setProfileId(nssiProfileID)
        sliceProfile.setE2ELatency(0)

        try {
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE,
                    execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), sliceserviceInstanceId, nssiProfileID)
            client.create(uri, sliceProfile)
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }

        def rollbackData = execution.getVariable("RollbackData")
        if (rollbackData == null) {
            rollbackData = new RollbackData();
        }
        //rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
        rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
        rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", sliceserviceInstanceId)
        rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("serviceType"))
        rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
        execution.setVariable("rollbackData", rollbackData)
        execution.setVariable("RollbackData", rollbackData)
        logger.debug("RollbackData:" + rollbackData)
        logger.trace("Exit createSliceProfile in DoAllocateNSSI()")
    }


    String buildCreateNSSMFRequest(DelegateExecution execution, String domain) {

        NssiAllocateRequest request = new NssiAllocateRequest()
        String strRequest = ""
        //String uuiRequest = execution.getVariable("uuiRequest")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")

        switch (domain) {
            case "AN":
                EsrInfo esrInfo = new EsrInfo()
                esrInfo.setNetworkType(execution.getVariable("networkType"))
                esrInfo.setVendor(execution.getVariable("nsstVendor"))

                NsiInfo nsiInfo = new NsiInfo()
                nsiInfo.setNsiId(execution.getVariable("nsiInstanceID"))
                nsiInfo.setNsiName(execution.getVariable("nsiInstanceName"))

                AnSliceProfile anSliceProfile = new AnSliceProfile()
                anSliceProfile.setLatency(execution.getVariable("latency"))
                anSliceProfile.setCoverageAreaTAList(execution.getVariable("coverageAreaList"))
                anSliceProfile.setQi(execution.getVariable("qi"))

                AllocateAnNssi allocateAnNssi = new AllocateAnNssi()
                allocateAnNssi.setNsstId(execution.getVariable("nsstId"))
                allocateAnNssi.setNssiName(execution.getVariable("nssiName"))
                allocateAnNssi.setNsiInfo(nsiInfo)
                allocateAnNssi.setSliceProfile(anSliceProfile)
                String anScriptName = sliceTaskParams.getAnScriptName()
                allocateAnNssi.setScriptName(anScriptName)

                request.setAllocateAnNssi(allocateAnNssi)
                request.setEsrInfo(esrInfo)
                break;
            case "CN":
                Map<String, Object> sliceProfileCn =execution.getVariable("sliceProfileCn")
                Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")
                NsiInfo nsiInfo = new NsiInfo()
                nsiInfo.setNsiId(execution.getVariable("nsiServiceInstanceId"))
                nsiInfo.setNsiName(execution.getVariable("nsiServiceInstanceName"))

                EsrInfo esrInfo = new EsrInfo()
                esrInfo.setNetworkType(NetworkType.fromString(domain))
                esrInfo.setVendor(execution.getVariable("nsstVendor"))
                execution.setVariable("esrInfo",esrInfo)


                PerfReqEmbbList perfReqEmbb = new PerfReqEmbbList()
                perfReqEmbb.setActivityFactor(sliceProfileCn.get("activityFactor"))
                perfReqEmbb.setAreaTrafficCapDL(sliceProfileCn.get("areaTrafficCapDL"))
                perfReqEmbb.setAreaTrafficCapUL(sliceProfileCn.get("areaTrafficCapUL"))
                perfReqEmbb.setExpDataRateDL(sliceProfileCn.get("expDataRateDL"))
                perfReqEmbb.setExpDataRateUL(sliceProfileCn.get("expDataRateUL"))

                List<PerfReqEmbbList> listPerfReqEmbbList = new ArrayList<>()
                listPerfReqEmbbList.add(perfReqEmbb)

                PerfReq perfReq = new PerfReq()
                perfReq.setPerfReqEmbbList(listPerfReqEmbbList)

                PerfReqUrllcList perfReqUrllc = new PerfReqUrllcList()
                perfReqUrllc.setConnDensity(0)
                perfReqUrllc.setTrafficDensity(0)
                perfReqUrllc.setExpDataRate(0)
                perfReqUrllc.setReliability(0)
                perfReqUrllc.setCsAvailability(0)
                perfReqUrllc.setSurvivalTime(0)
                perfReqUrllc.setJitter(0)
                perfReqUrllc.setE2eLatency(0)
                perfReqUrllc.setPayloadSize("0")
                perfReqUrllc.setServiceAreaDimension("")

                List<PerfReqUrllcList> perfReqUrllcList = new ArrayList<>()
                perfReqUrllcList.add(perfReqUrllc)
                perfReq.setPerfReqUrllcList(perfReqUrllcList)

                CnSliceProfile cnSliceProfile = new CnSliceProfile()
                cnSliceProfile.setSliceProfileId(execution.getVariable("nssiProfileID"))
                String plmnStr = serviceProfile.get("plmnIdList")
                List<String> plmnIdList=Arrays.asList(plmnStr.split("\\|"))
                cnSliceProfile.setPlmnIdList(plmnIdList)

                String resourceSharingLevel = serviceProfile.get("resourceSharingLevel").toString()
                cnSliceProfile.setResourceSharingLevel(ResourceSharingLevel.fromString(resourceSharingLevel))

                String coverageArea = serviceProfile.get("coverageAreaTAList")
                List<String> coverageAreaList=Arrays.asList(coverageArea.split("\\|"))
                cnSliceProfile.setCoverageAreaTAList(coverageAreaList)

                String ueMobilityLevel = serviceProfile.get("uEMobilityLevel").toString()
                cnSliceProfile.setUeMobilityLevel(UeMobilityLevel.fromString(ueMobilityLevel))

                int latency = serviceProfile.get("latency")
                cnSliceProfile.setLatency(latency)

                int maxUE = serviceProfile.get("maxNumberofUEs")
                cnSliceProfile.setMaxNumberofUEs(maxUE)

                String snssai = serviceProfile.get("sNSSAI")
                List<String> snssaiList = Arrays.asList(snssai.split("\\|"))
                cnSliceProfile.setSnssaiList(snssaiList)

                cnSliceProfile.setPerfReq(perfReq)

                AllocateCnNssi allocateCnNssi = new AllocateCnNssi()
                allocateCnNssi.setNsstId(execution.getVariable("nsstid"))
                allocateCnNssi.setNssiName(execution.getVariable("nssiName"))
                allocateCnNssi.setSliceProfile(cnSliceProfile)
                allocateCnNssi.setNsiInfo(nsiInfo)
                String cnScriptName = sliceTaskParams.getCnScriptName()
                allocateCnNssi.setScriptName(cnScriptName)
                request.setAllocateCnNssi(allocateCnNssi)
                request.setEsrInfo(esrInfo)
                break;
            case "TN":
                EsrInfo esrInfo = new EsrInfo()
                esrInfo.setNetworkType(execution.getVariable("networkType"))
                esrInfo.setVendor(execution.getVariable("vendor"))

                TnSliceProfile tnSliceProfile = new TnSliceProfile()
                tnSliceProfile.setLatency(execution.getVariable("latency"))
                tnSliceProfile.setBandwidth(execution.getVariable("bandWidth"))

                NsiInfo nsiInfo = new NsiInfo()
                nsiInfo.setNsiId(execution.getVariable("nsiInstanceID"))
                nsiInfo.setNsiName(execution.getVariable("nsiInstanceName"))

                AllocateTnNssi allocateTnNssi = new AllocateTnNssi()
                allocateTnNssi.setSliceProfile(tnSliceProfile)
                allocateTnNssi.setNsiInfo(nsiInfo)
                allocateTnNssi.setNsstId(execution.getVariable("nsstid"))
                String tnScriptName = sliceTaskParams.getTnScriptName()
                allocateTnNssi.setScriptName(tnScriptName)

                request.setAllocateTnNssi(allocateTnNssi)
                request.setEsrInfo(esrInfo)
                break;
            default:
                break;
        }
        try {
            strRequest = MAPPER.writeValueAsString(request);
        } catch (IOException e) {
            logger.error("Invalid get progress request bean to convert as string");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Invalid get progress request bean to convert as string")
        }
        return strRequest
    }


    String buildUpdateNSSMFRequest(DelegateExecution execution, String domain) {
        NssiAllocateRequest request = new NssiAllocateRequest()
        String nsstInput = execution.getVariable("nsstInput")
        String nssiId = jsonUtil.getJsonValue(nsstInput, "serviceInstanceId")
        String strRequest = ""
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        switch (domain) {
            case "AN":
                EsrInfo esrInfo = new EsrInfo()
                esrInfo.setNetworkType(execution.getVariable("nsstType"))
                esrInfo.setVendor(execution.getVariable("vendor"))

                NsiInfo nsiInfo = new NsiInfo()
                nsiInfo.setNsiId(execution.getVariable("nsiInstanceID"))
                nsiInfo.setNsiName(execution.getVariable("nsiInstanceName"))

                AnSliceProfile anSliceProfile = new AnSliceProfile()
                anSliceProfile.setLatency(execution.getVariable("latency"))
                anSliceProfile.setCoverageAreaTAList(execution.getVariable("coverageAreaList"))
                anSliceProfile.setQi(execution.getVariable("qi"))

                AllocateAnNssi allocateAnNssi = new AllocateAnNssi()
                allocateAnNssi.setNsstId(execution.getVariable("nsstId"))
                allocateAnNssi.setNssiName(execution.getVariable("nssiName"))
                allocateAnNssi.setNsiInfo(nsiInfo)
                allocateAnNssi.setSliceProfile(anSliceProfile)
                String anScriptName = sliceTaskParams.getAnScriptName()
                allocateAnNssi.setScriptName(anScriptName)
                request.setAllocateAnNssi(allocateAnNssi)
                request.setEsrInfo(esrInfo)
                break;
            case "CN":
                Map<String, Object> sliceProfileCn =execution.getVariable("sliceProfileCn")
                Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")
                NsiInfo nsiInfo = new NsiInfo()
                nsiInfo.setNsiId(execution.getVariable("nsiServiceInstanceId"))
                nsiInfo.setNsiName(execution.getVariable("nsiServiceInstanceName"))

                EsrInfo esrInfo = new EsrInfo()
                esrInfo.setNetworkType(NetworkType.fromString(domain))
                esrInfo.setVendor(execution.getVariable("nsstVendor"))
                execution.setVariable("esrInfo",esrInfo)


                PerfReqEmbbList perfReqEmbb = new PerfReqEmbbList()
                perfReqEmbb.setActivityFactor(sliceProfileCn.get("activityFactor"))
                perfReqEmbb.setAreaTrafficCapDL(sliceProfileCn.get("areaTrafficCapDL"))
                perfReqEmbb.setAreaTrafficCapUL(sliceProfileCn.get("areaTrafficCapUL"))
                perfReqEmbb.setExpDataRateDL(sliceProfileCn.get("expDataRateDL"))
                perfReqEmbb.setExpDataRateUL(sliceProfileCn.get("expDataRateUL"))

                List<PerfReqEmbbList> listPerfReqEmbbList = new ArrayList<>()
                listPerfReqEmbbList.add(perfReqEmbb)

                PerfReq perfReq = new PerfReq()
                perfReq.setPerfReqEmbbList(listPerfReqEmbbList)

                PerfReqUrllcList perfReqUrllc = new PerfReqUrllcList()
                perfReqUrllc.setConnDensity(0)
                perfReqUrllc.setTrafficDensity(0)
                perfReqUrllc.setExpDataRate(0)
                perfReqUrllc.setReliability(0)
                perfReqUrllc.setCsAvailability(0)
                perfReqUrllc.setSurvivalTime(0)
                perfReqUrllc.setJitter(0)
                perfReqUrllc.setE2eLatency(0)
                perfReqUrllc.setPayloadSize("0")
                perfReqUrllc.setServiceAreaDimension("")

                List<PerfReqUrllcList> perfReqUrllcList = new ArrayList<>()
                perfReqUrllcList.add(perfReqUrllc)
                perfReq.setPerfReqUrllcList(perfReqUrllcList)

                CnSliceProfile cnSliceProfile = new CnSliceProfile()
                cnSliceProfile.setSliceProfileId(execution.getVariable("nssiProfileID"))
                String plmnStr = serviceProfile.get("plmnIdList")
                List<String> plmnIdList=Arrays.asList(plmnStr.split("\\|"))
                cnSliceProfile.setPlmnIdList(plmnIdList)

                String resourceSharingLevel = serviceProfile.get("resourceSharingLevel").toString()
                cnSliceProfile.setResourceSharingLevel(ResourceSharingLevel.fromString(resourceSharingLevel))

                String coverageArea = serviceProfile.get("coverageAreaTAList")
                List<String> coverageAreaList=Arrays.asList(coverageArea.split("\\|"))
                cnSliceProfile.setCoverageAreaTAList(coverageAreaList)

                String ueMobilityLevel = serviceProfile.get("uEMobilityLevel").toString()
                cnSliceProfile.setUeMobilityLevel(UeMobilityLevel.fromString(ueMobilityLevel))

                int latency = serviceProfile.get("latency")
                cnSliceProfile.setLatency(latency)

                int maxUE = serviceProfile.get("maxNumberofUEs")
                cnSliceProfile.setMaxNumberofUEs(maxUE)

                String snssai = serviceProfile.get("sNSSAI")
                List<String> snssaiList = Arrays.asList(snssai.split("\\|"))
                cnSliceProfile.setSnssaiList(snssaiList)

                cnSliceProfile.setPerfReq(perfReq)

                AllocateCnNssi allocateCnNssi = new AllocateCnNssi()
                allocateCnNssi.setNsstId(execution.getVariable("nsstid"))
                allocateCnNssi.setNssiName(execution.getVariable("nssiName"))
                allocateCnNssi.setSliceProfile(cnSliceProfile)
                allocateCnNssi.setNsiInfo(nsiInfo)
                allocateCnNssi.setNssiId(nssiId)              // need to check this
                String cnScriptName = sliceTaskParams.getCnScriptName()
                allocateCnNssi.setScriptName(cnScriptName)
                request.setAllocateCnNssi(allocateCnNssi)
                request.setEsrInfo(esrInfo)
                break;
            case "TN":
                EsrInfo esrInfo = new EsrInfo()
                esrInfo.setNetworkType(execution.getVariable("networkType"))
                esrInfo.setVendor(execution.getVariable("vendor"))

                TnSliceProfile tnSliceProfile = new TnSliceProfile()
                tnSliceProfile.setLatency(execution.getVariable("latency"))
                tnSliceProfile.setBandwidth(execution.getVariable("bandWidth"))

                NsiInfo nsiInfo = new NsiInfo()
                nsiInfo.setNsiId(execution.getVariable("nsiInstanceID"))
                nsiInfo.setNsiName(execution.getVariable("nsiInstanceName"))

                AllocateTnNssi allocateTnNssi = new AllocateTnNssi()
                allocateTnNssi.setSliceProfile(tnSliceProfile)
                allocateTnNssi.setNsiInfo(nsiInfo)
                allocateTnNssi.setNsstId(execution.getVariable("nsstid"))
                String tnScriptName = sliceTaskParams.getTnScriptName()
                allocateTnNssi.setScriptName(tnScriptName)
                request.setAllocateTnNssi(allocateTnNssi)
                request.setEsrInfo(esrInfo)
                break;
            default:
                break;
        }
        try {
            strRequest = MAPPER.writeValueAsString(request);
        } catch (IOException e) {
            logger.error("Invalid get progress request bean to convert as string");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Invalid get progress request bean to convert as string")
        }
        return strRequest
    }

    String buildNSSMFProgressRequest(DelegateExecution execution){
        JobStatusRequest request = new JobStatusRequest()
        String strRequest = ""
        EsrInfo esrInfo = execution.getVariable("esrInfo")
        request.setNsiId(execution.getVariable("nsiServiceInstanceId"))
        request.setNssiId(execution.getVariable("nssiId"))
        request.setEsrInfo(esrInfo)

        try {
            strRequest = MAPPER.writeValueAsString(request);
        } catch (IOException e) {
            logger.error("Invalid get progress request bean to convert as string");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Invalid get progress request bean to convert as string")
        }
        return strRequest
    }

    public void prepareUpdateOrchestrationTask(DelegateExecution execution) {
        logger.debug("Start prepareUpdateOrchestrationTask progress")
        String requestMethod = "PUT"
        String progress = execution.getVariable("nssmfProgress")
        String status = execution.getVariable("nssmfStatus")
        String statusDescription=execution.getVariable("nddmfStatusDescription")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        String domain = execution.getVariable("nsstDomain")
        switch (domain.toUpperCase()) {
            case "AN":
                sliceTaskParams.setAnProgress(progress)
                sliceTaskParams.setAnStatus(status)
                sliceTaskParams.setAnStatusDescription(statusDescription)
                break;
            case "CN":
                sliceTaskParams.setCnProgress(progress)
                sliceTaskParams.setCnStatus(status)
                sliceTaskParams.setCnStatusDescription(statusDescription)
                break;
            case "TN":
                sliceTaskParams.setTnProgress(progress)
                sliceTaskParams.setTnStatus(status)
                sliceTaskParams.setTnStatusDescription(statusDescription)
                break;
            default:
                break;
        }
        String paramJson = sliceTaskParams.convertToJson()
        execution.setVariable("CSSOT_paramJson", paramJson)
        execution.setVariable("CSSOT_requestMethod", requestMethod)
        logger.debug("Finish prepareUpdateOrchestrationTask progress")
    }

}
