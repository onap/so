package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.core.type.TypeReference
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.db.request.client.RequestsDbClient
import org.onap.so.db.request.beans.OrchestrationTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

import static org.apache.commons.lang3.StringUtils.isBlank

public class DoCreateSliceServiceOption extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoCreateSliceServiceOption.class)


    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    RequestsDbClient requestsDbClient = new RequestsDbClient()

    OofUtils oofUtils = new OofUtils()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    void preProcessRequest (DelegateExecution execution) {
        String msg = ""
        logger.trace("Enter preProcessRequest()")
        String taskID = execution.getVariable("taskID")
        Boolean isSharable = true
        String resourceSharingLevel = execution.getVariable("resourceSharingLevel")
        if (resourceSharingLevel.equals("shared"))
            isSharable = true
        execution.setVariable("isSharable",isSharable)
        logger.trace("Exit preProcessRequest")

    }


    void getNSIOptionfromOOF(DelegateExecution execution) {

        String urlString = UrnPropertiesReader.getVariable("mso.oof.endpoint", execution)
        logger.debug( "get NSI option OOF Url: " + urlString)
        boolean isNSISuggested = true
        execution.setVariable("isNSISuggested",isNSISuggested)
        String nsiInstanceId = ""
        String nsiName = ""
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
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

        String requestId = execution.getVariable("msoRequestId")
        Map<String, Object> profileInfo = execution.getVariable("serviceProfile")
        String nstModelUuid = execution.getVariable("nstModelUuid")
        String nstModelInvariantUuid = execution.getVariable("nstModelInvariantUuid")
        String nstInfo = """"NSTInfo" : {
        "invariantUUID":"${nstModelInvariantUuid}",
        "UUID":"${nstModelUuid}"
         }"""

        String oofRequest = oofUtils.buildSelectNSIRequest(execution, requestId, nstInfo, profileInfo)

        //send request to get NSI option - Begin
        URL url = new URL(urlString+"/api/oof/v1/selectnsi")
        HttpClient httpClient = new HttpClientFactory().newJsonClient(url, ONAPComponents.OOF)
        httpClient.addAdditionalHeader("Authorization", authHeader)
        Response httpResponse = httpClient.post(oofRequest)

        int responseCode = httpResponse.getStatus()
        logger.debug("OOF sync response code is: " + responseCode)

        if(responseCode != 200){
            exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Received a Bad Sync Response from OOF.")
            logger.debug("Info: No NSI suggested by OOF" )
        }

        if(httpResponse.hasEntity()){
            String OOFResponse = httpResponse.readEntity(String.class)
            execution.setVariable("OOFResponse", OOFResponse)
            int index = 0   //This needs to be changed to derive a value when we add policy to decide the solution options.
            Map OOFResponseObject = new JsonSlurper().parseText(OOFResponse)
            if(execution.getVariable("isSharable" )  == true && OOFResponseObject.get("solutions").containsKey("sharedNSIsolutions")) {
                nsiInstanceId = OOFResponseObject.get("solutions").get("sharedNSIsolutions").get(0).get("NSISolution").NSIId
                nsiName = OOFResponseObject.get("solutions").get("sharedNSIsolutions").get(0).get("NSISolution").NSIName
                sliceTaskParams.setNstId(nsiInstanceId)
                sliceTaskParams.setSuggestNsiName(nsiName)
                execution.setVariable("nsiInstanceId",nsiInstanceId)
                execution.setVariable("nsiName",nsiName)
            }else {
                if(OOFResponseObject.get("solutions").containsKey("newNSISolutions")) {
                    List NSSImap = OOFResponseObject.get("solutions").get("newNSISolutions").get(index).get("NSSISolutions")
                    for(Map nssi :  NSSImap) {
                        String nssiName = nssi.get("NSSISolution").NSSIName
                        String nssiId = nssi.get("NSSISolution").NSSIId
                        String domain = nssi.get("NSSISolution").domain.toUpperCase()
                        switch (domain) {
                            case "AN":
                                sliceTaskParams.setAnSuggestNssiId(nssiId)
                                sliceTaskParams.setAnSuggestNssiName(nssiName)
                                break;
                            case "CN":
                                sliceTaskParams.setCnSuggestNssiId(nssiId)
                                sliceTaskParams.setCnSuggestNssiName(nssiName)
                                break;
                            case "TN":
                                sliceTaskParams.setTnSuggestNssiId(nssiId)
                                sliceTaskParams.setTnSuggestNssiName(nssiName)
                                break;
                            default:
                                break;
                        }
                    }
                }

            }
            execution.setVariable("sliceTaskParams", sliceTaskParams)
            logger.debug("Info: No NSI suggested by OOF" )
        }
        //send request to get NSI option - Begin


        //send request to get NSI service Info - Begin

        /***
         * Need to check whether its needed.
         */
//            logger.debug("Begin to query OOF suggetsed NSI from AAI ")
//            if(isBlank(nsiInstanceId)){
//                isNSISuggested = false
//                execution.setVariable("isNSISuggested",isNSISuggested)
//            }else
//            {
//                try {
//                    String globalSubscriberId = execution.getVariable('globalSubscriberId')
//                    String serviceType = execution.getVariable('subscriptionServiceType')
//                    AAIResourcesClient resourceClient = new AAIResourcesClient()
//                    AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nsiInstanceId)
//                    AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
//                    Optional<org.onap.aai.domain.yang.ServiceInstance> si = wrapper.asBean(org.onap.aai.domain.yang.ServiceInstance.class)
//                    org.onap.aai.domain.yang.ServiceInstance nsiServiceInstance = si.get()
//                    execution.setVariable("nsiServiceInstance",nsiServiceInstance)
//                    isNSISuggested = true
//                    execution.setVariable("isNSISuggested",isNSISuggested)
//                    SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
//                    sliceTaskParams.setSuggestNsiId(nsiInstanceId)
//                    sliceTaskParams.setSuggestNsiName(si.get().getServiceInstanceName())
//                    execution.setVariable("sliceTaskParams", sliceTaskParams)
//                    logger.debug("Info: NSI suggested by OOF exist in AAI ")
//                }catch(BpmnError e) {
//                    throw e
//                }catch(Exception ex) {
//                    String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
//                    //exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
//                    logger.debug("Info: NSI suggested by OOF doesnt exist in AAI " + nsiInstanceId)
//                }
//            }
        //send request to get NSI service Info - End
        //${OrchestrationTaskHandler.createOrchestrationTask(execution.getVariable("OrchestrationTask"))}
        logger.debug( "*** Completed options Call to OOF ***")

    }


    public void parseServiceProfile(DelegateExecution execution) {
        logger.debug("Start parseServiceProfile")
        String serviceType = execution.getVariable("serviceType")
        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")

        // set sliceProfile for three domains
        Map<String, Object> sliceProfileTn = getSliceProfile(serviceType, "TN", serviceProfile)
        Map<String, Object> sliceProfileCn = getSliceProfile(serviceType, "CN", serviceProfile)
        Map<String, Object> sliceProfileAn = getSliceProfile(serviceType, "AN", serviceProfile)

        execution.setVariable("sliceProfileTn", sliceProfileTn)
        execution.setVariable("sliceProfileCn", sliceProfileCn)
        execution.setVariable("sliceProfileAn", sliceProfileAn)
        logger.debug("sliceProfileTn: " + sliceProfileTn)
        logger.debug("sliceProfileCn: " + sliceProfileCn)
        logger.debug("sliceProfileAn: " + sliceProfileAn)

        logger.debug("Finish parseServiceProfile")
    }

    public Map getSliceProfile(String serviceType, String domain, Map<String, Object> serviceProfile) {
        String variablePath = "nsmf." + serviceType + ".profileMap" + domain
        String profileMapStr = UrnPropertiesReader.getVariable(variablePath)
        logger.debug("Profile map for " + domain + " : " + profileMapStr)
        Map<String, String> profileMaps = objectMapper.readValue(profileMapStr, new TypeReference<Map<String, String>>(){})
        Map<String, Object> sliceProfileTn = [:]
        for (Map.Entry<String, String> profileMap : profileMaps) {
            sliceProfileTn.put(profileMap.key, serviceProfile.get(profileMap.value))
        }

        return sliceProfileTn
    }


    void prepareNSSIList(DelegateExecution execution)
    {
        ServiceDecomposition serviceDecomposition= execution.getVariable("serviceDecomposition")
        List<String> nssiAssociated = new ArrayList<>()
        Map<String, String> nssimap = new HashMap<>()
        String nsiInstanceId=execution.getVariable("nsiInstanceId")
        String globalSubscriberId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("subscriptionServiceType")

        try {

            ServiceInstance si = execution.getVariable("nsiServiceInstance")
            //List<Relationship> relationships = si.getRelationshipList().getRelationship().stream().filter(relation ->
            //        relation.getRelatedTo().equalsIgnoreCase("service-instance"))
            RelationshipList relationshipList = si.getRelationshipList()
            List<Relationship> relationships = relationshipList.getRelationship()
            for(Relationship relationship in relationships)
            {
                if(relationship.getRelatedTo().equalsIgnoreCase("service-instance"))
                {
                    String NSSIassociated = relationship.getRelatedLink().substring(relationship.getRelatedLink().lastIndexOf("/") + 1);
                    if(!NSSIassociated.equals(nsiInstanceId))
                        nssiAssociated.add(NSSIassociated)
                }
            }
        }catch(BpmnError e) {
            throw e
        }catch(Exception ex) {
            String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        Map<String, Object> params = execution.getVariable("params")
        SliceTaskParams sliceTaskParams = execution.getVariable("sliceTaskParams")
        for(String nssiID in nssiAssociated)
        {
            try {
                AAIResourcesClient resourceClient = new AAIResourcesClient()
                AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nssiID)
                AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
                Optional<org.onap.aai.domain.yang.ServiceInstance> si = wrapper.asBean(org.onap.aai.domain.yang.ServiceInstance.class)
                org.onap.aai.domain.yang.ServiceInstance nssi = si.get()

                String domain = nssi.getEnvironmentContext().toString().toUpperCase()
                switch (domain) {
                    case "AN":
                        sliceTaskParams.setAnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setAnSuggestNssiName(nssi.getServiceInstanceName())
                        break;
                    case "CN":
                        sliceTaskParams.setCnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setCnSuggestNssiName(nssi.getServiceInstanceName())
                        break;
                    case "TN":
                        sliceTaskParams.setTnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setTnSuggestNssiName(nssi.getServiceInstanceName())
                        break;
                    default:
                        break;
                }
            }catch(NotFoundException e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiID)
            }catch(Exception e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiID)
            }

        }
        String nstName = serviceDecomposition.getModelInfo().getModelName()
        sliceTaskParams.setNstName(nstName)
        String nstId = serviceDecomposition.getModelInfo().getModelUuid()
        sliceTaskParams.setNstId(nstId)
        execution.setVariable("sliceTaskParams",sliceTaskParams)

    }


    void updateOptionsInDB(DelegateExecution execution) {
        logger.debug("Updating options with default value since not sharable : Begin ")
        String taskID = execution.getVariable("taskID")
        String params = execution.getVariable("params")
        logger.debug("Updating options with default value since not sharable : End ")

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


    void updateStatusInDB(DelegateExecution execution) {

        String taskID = execution.getVariable("taskID")
        //OrchestrationTask orchestrationTask = requestsDbClient.getNetworkSliceOption(taskID);
        //orchestrationTask.setTaskStage("wait to confirm")
        //requestsDbClient.updateNetworkSliceOption(orchestrationTask)
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
        if(isBlank(nssiInstanceId)){
            logger.debug( "There is no valid NSST suggested by OOF.")
        }else
        {
            try {
                AAIResourcesClient resourceClient = new AAIResourcesClient()
                AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, nssiInstanceId)
                AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
                Optional<org.onap.aai.domain.yang.ServiceInstance> si = wrapper.asBean(org.onap.aai.domain.yang.ServiceInstance.class)
                org.onap.aai.domain.yang.ServiceInstance nssi = si.get()

                String domain = nssi.getEnvironmentContext().toString().toUpperCase()
                switch (domain) {
                    case "AN":
                        sliceTaskParams.setAnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setAnSuggestNssiName(nssi.getServiceInstanceName())
                        break;
                    case "CN":
                        sliceTaskParams.setCnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setCnSuggestNssiName(nssi.getServiceInstanceName())
                        break;
                    case "TN":
                        sliceTaskParams.setTnSuggestNssiId(nssi.getServiceInstanceId())
                        sliceTaskParams.setTnSuggestNssiName(nssi.getServiceInstanceName())
                        break;
                    default:
                        break;
                }
            }catch(NotFoundException e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiInstanceId)
            }catch(Exception e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiInstanceId)
            }
        }


        //Prepare send request to OOF - End

//        String content = serviceDecomposition.getServiceInfo().getServiceArtifact().get(0).getContent()
//        String nsstID = jsonUtil.getJsonValue(content, "metadata.id")
//        String vendor = jsonUtil.getJsonValue(content, "metadata.vendor")
//        String domain = jsonUtil.getJsonValue(content, "metadata.domainType")
//        String type = jsonUtil.getJsonValue(content, "metadata.type")
//        String nsstContentInfo = """{
//        "NsstID":"${nsstID}",
//        "Vendor":"${vendor}",
//        "type":"${type}"
//         }"""

        logger.debug("Prepare NSSI option completed ")
    }
}

