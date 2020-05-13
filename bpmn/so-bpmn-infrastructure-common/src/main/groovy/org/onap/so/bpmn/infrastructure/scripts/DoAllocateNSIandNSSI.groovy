package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.NssmfAdapterUtils
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.NotFoundException

import static org.apache.commons.lang3.StringUtils.isBlank

class DoAllocateNSIandNSSI extends org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoAllocateNSIandNSSI.class);

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    JsonUtils jsonUtil = new JsonUtils()

    private NssmfAdapterUtils nssmfAdapterUtils = new NssmfAdapterUtils(httpClientFactory, jsonUtil)

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */

    void preProcessRequest (DelegateExecution execution) {
        String msg = ""
        logger.trace("Enter preProcessRequest()")
        Map<String, Object> nssiMap = new HashMap<>()
        execution.setVariable("nssiMap", nssiMap)
        boolean isMoreNSSTtoProcess = true
        execution.setVariable("isMoreNSSTtoProcess", isMoreNSSTtoProcess)
        List<String> nsstSequence = new ArrayList<>(Arrays.asList("cn"))
        execution.setVariable("nsstSequence", nsstSequence)
        logger.trace("Exit preProcessRequest")
    }

    void retriveSliceOption(DelegateExecution execution) {
        logger.trace("Enter retriveSliceOption() of DoAllocateNSIandNSSI")
        String uuiRequest = execution.getVariable("uuiRequest")
        boolean isNSIOptionAvailable = false
        List<String> nssiAssociated = new ArrayList<>()
        SliceTaskParams sliceParams = execution.getVariable("sliceTaskParams")
        try
        {
            Map<String, Object> nstSolution = execution.getVariable("nstSolution") as Map
            String modelUuid = nstSolution.get("UUID")
            String modelInvariantUuid = nstSolution.get("invariantUUID")
            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)
            //Params sliceParams = new Gson().fromJson(params, new TypeToken<Params>() {}.getType());
            execution.setVariable("sliceParams", sliceParams)
        }catch (Exception ex) {
            logger.debug( "Unable to get the task information from request DB: " + ex)
            exceptionUtil.buildAndThrowWorkflowException(execution, 401, "Unable to get task information from request DB.")
        }

        if(isBlank(sliceParams.getSuggestNsiId()))
        {
            isNSIOptionAvailable=false
        }
        else
        {
            isNSIOptionAvailable=true
            execution.setVariable('nsiServiceInstanceId',sliceParams.getSuggestNsiId())
            execution.setVariable('nsiServiceInstanceName',sliceParams.getSuggestNsiName())
        }
        execution.setVariable("isNSIOptionAvailable",isNSIOptionAvailable)
        logger.trace("Exit retriveSliceOption() of DoAllocateNSIandNSSI")
    }

    void updateRelationship(DelegateExecution execution) {
        logger.trace("Enter update relationship in DoAllocateNSIandNSSI()")
        String nsiServiceInstanceId = execution.getVariable("nsiServiceInstanceId")
        String allottedResourceId = execution.getVariable("allottedResourceId")
        //Need to check whether nsi exist : Begin
        org.onap.aai.domain.yang.ServiceInstance nsiServiceInstance = new org.onap.aai.domain.yang.ServiceInstance()
        SliceTaskParams sliceParams = execution.getVariable("sliceParams")

        String nsiServiceInstanceID = sliceParams.getSuggestNsiId()

        AAIResourcesClient resourceClient = new AAIResourcesClient()
        AAIResourceUri nsiServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), nsiServiceInstanceID)
        //AAIResourceUri nsiServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.QUERY_ALLOTTED_RESOURCE, execution.getVariable("globalSubscriberId"), execution.getVariable("serviceType"), nsiServiceInstanceID)

        try {
            AAIResultWrapper wrapper = resourceClient.get(nsiServiceuri, NotFoundException.class)
            Optional<org.onap.aai.domain.yang.ServiceInstance> si = wrapper.asBean(org.onap.aai.domain.yang.ServiceInstance.class)
            nsiServiceInstance = si.get()
            //allottedResourceId=nsiServiceInstance.getAllottedResources().getAllottedResource().get(0).getId()

//            if(resourceClient.exists(nsiServiceuri)){
//                execution.setVariable("nsi_resourceLink", nsiServiceuri.build().toString())
//            }else{
//                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai to " +
//                        "associate for service :"+serviceInstanceId)
//            }
        }catch(BpmnError e) {
            throw e;
        }catch (Exception ex){
            String msg = "NSI suggested in the option doesn't exist. " + nsiServiceInstanceID
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE, execution.getVariable("globalSubscriberId"), execution.getVariable("serviceType"), nsiServiceInstanceId, allottedResourceId)
        getAAIClient().connect(allottedResourceUri,nsiServiceuri)

        List<String> nssiAssociated = new ArrayList<>()
        RelationshipList relationshipList = nsiServiceInstance.getRelationshipList()
        List<Relationship> relationships = relationshipList.getRelationship()
        for(Relationship relationship in relationships)
        {
            if(relationship.getRelatedTo().equalsIgnoreCase("service-instance"))
            {
                String NSSIassociated = relationship.getRelatedLink().substring(relationship.getRelatedLink().lastIndexOf("/") + 1);
                if(!NSSIassociated.equals(nsiServiceInstanceID))
                    nssiAssociated.add(NSSIassociated)
            }
        }
        execution.setVariable("nssiAssociated",nssiAssociated)
        execution.setVariable("nsiServiceInstanceName",nsiServiceInstance.getServiceInstanceName())
        logger.trace("Exit update relationship in DoAllocateNSIandNSSI()")
    }

    void prepareNssiModelInfo(DelegateExecution execution){
        logger.trace("Enter prepareNssiModelInfo in DoAllocateNSIandNSSI()")
        List<String> nssiAssociated = new ArrayList<>()
        Map<String, Object> nssiMap = new HashMap<>()
        nssiAssociated=execution.getVariable("nssiAssociated")
        for(String nssiID in nssiAssociated)
        {
            try {
                AAIResourcesClient resourceClient = new AAIResourcesClient()
                AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), nssiID)
                AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
                Optional<org.onap.aai.domain.yang.ServiceInstance> si = wrapper.asBean(org.onap.aai.domain.yang.ServiceInstance.class)
                org.onap.aai.domain.yang.ServiceInstance nssi = si.get()
                nssiMap.put(nssi.getEnvironmentContext(),"""{
                    "serviceInstanceId":"${nssi.getServiceInstanceId()}",
                    "modelUuid":"${nssi.getModelVersionId()}"
                     }""")

            }catch(NotFoundException e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiID)
            }catch(Exception e)
            {
                logger.debug("NSSI Service Instance not found in AAI: " + nssiID)
            }
            execution.setVariable("nssiMap",nssiMap)

        }
        logger.trace("Exit prepareNssiModelInfo in DoAllocateNSIandNSSI()")
    }

    void createNSIinAAI(DelegateExecution execution) {
        logger.debug("Enter CreateNSIinAAI in DoAllocateNSIandNSSI()")
        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        org.onap.aai.domain.yang.ServiceInstance nsi = new ServiceInstance();
        String sliceInstanceId = UUID.randomUUID().toString()
        execution.setVariable("sliceInstanceId",sliceInstanceId)
        nsi.setServiceInstanceId(sliceInstanceId)
        String sliceInstanceName = "nsi_"+execution.getVariable("sliceServiceInstanceName")
        nsi.setServiceInstanceName(sliceInstanceName)
        String serviceType = execution.getVariable("serviceType")
        nsi.setServiceType(serviceType)
        String serviceStatus = "deactivated"
        nsi.setOrchestrationStatus(serviceStatus)
        String modelInvariantUuid = serviceDecomposition.getModelInfo().getModelInvariantUuid()
        String modelUuid = serviceDecomposition.getModelInfo().getModelUuid()
        nsi.setModelInvariantId(modelInvariantUuid)
        nsi.setModelVersionId(modelUuid)
        String uuiRequest = execution.getVariable("uuiRequest")
        String serviceInstanceLocationid = jsonUtil.getJsonValue(uuiRequest, "service.parameters.requestInputs.plmnIdList")
        nsi.setServiceInstanceLocationId(serviceInstanceLocationid)
        //String snssai = jsonUtil.getJsonValue(uuiRequest, "service.requestInputs.snssai")
        //nsi.setEnvironmentContext(snssai)
        String serviceRole = "nsi"
        nsi.setServiceRole(serviceRole)
        String msg = ""
        try {

            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri nsiServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), sliceInstanceId)
            client.create(nsiServiceUri, nsi)

            Relationship relationship = new Relationship()
            logger.info("Creating Allotted resource relationship, nsiServiceUri: " + nsiServiceUri.build().toString())
            relationship.setRelatedLink(nsiServiceUri.build().toString())
            AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE,
                    execution.getVariable("globalSubscriberId"),execution.getVariable("subscriptionServiceType"),
                    execution.getVariable("sliceServiceInstanceId"), execution.getVariable("allottedResourceId")).relationshipAPI()
            client.create(allottedResourceUri, relationship)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        Map<String, Object> nssiMap = new HashMap<>()
        List<ServiceProxy> serviceProxyList = serviceDecomposition.getServiceProxy()
        List<String> nsstModelInfoList = new ArrayList<>()
        for(ServiceProxy serviceProxy : serviceProxyList)
        {
            //String nsstModelUuid = serviceProxy.getModelInfo().getModelUuid()
            String nsstModelUuid = serviceProxy.getSourceModelUuid()
            //String nsstModelInvariantUuid = serviceProxy.getModelInfo().getModelInvariantUuid()
            String nsstServiceModelInfo = """{
            "modelInvariantUuid":"",
            "modelUuid":"${nsstModelUuid}",
            "modelVersion":""
             }"""
            nsstModelInfoList.add(nsstServiceModelInfo)
        }
        int currentIndex=0
        int maxIndex=nsstModelInfoList.size()
        if(maxIndex < 1)
        {
            msg = "Exception in DoAllocateNSIandNSSI. There is no NSST associated with NST "
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        execution.setVariable("nsstModelInfoList",nsstModelInfoList)
        execution.setVariable("currentIndex",currentIndex)
        execution.setVariable("maxIndex",maxIndex)
        execution.setVariable('nsiServiceInstanceId',sliceInstanceId)
        execution.setVariable("nsiServiceInstanceName",sliceInstanceName)
        logger.debug("Exit CreateNSIinAAI in DoAllocateNSIandNSSI()")
    }

    void getOneNsstInfo(DelegateExecution execution){
        List<String> nsstModelInfoList = new ArrayList<>()
        nsstModelInfoList = execution.getVariable("nsstModelInfoList")
        int currentIndex = execution.getVariable("currentIndex")
        int maxIndex = execution.getVariable("maxIndex")
        boolean isMoreNSSTtoProcess = true
        String nsstServiceModelInfo = nsstModelInfoList.get(currentIndex)
        execution.setVariable("serviceModelInfo", nsstServiceModelInfo)
        execution.setVariable("currentIndex", currentIndex)
        currentIndex = currentIndex+1
        if(currentIndex <= maxIndex )
            isMoreNSSTtoProcess = false
        execution.setVariable("isMoreNSSTtoProcess", isMoreNSSTtoProcess)
    }

    void createNSSTMap(DelegateExecution execution){
        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        String modelUuid= serviceDecomposition.getModelInfo().getModelUuid()
        String content = serviceDecomposition.getServiceInfo().getServiceArtifact().get(0).getContent()
        //String nsstID = jsonUtil.getJsonValue(content, "metadata.id")
        //String vendor = jsonUtil.getJsonValue(content, "metadata.vendor")
        //String type = jsonUtil.getJsonValue(content, "metadata.type")
        String domain = jsonUtil.getJsonValue(content, "metadata.domainType")

        Map<String, Object> nssiMap = execution.getVariable("nssiMap")
        String servicename = execution.getVariable("sliceServiceInstanceName")
        String nsiname = "nsi_"+servicename
        nssiMap.put(domain,"""{
                    "serviceInstanceId":"",
                    "modelUuid":"${modelUuid}"
                     }""")
        execution.setVariable("nssiMap",nssiMap)
    }

    void prepareNSSIList(DelegateExecution execution){
        logger.trace("Enter prepareNSSIList in DoAllocateNSIandNSSI()")
        Map<String, Object> nssiMap = new HashMap<>()
        Boolean isMoreNSSI = false
        nssiMap = execution.getVariable("nssiMap")
        List<String> keys=new ArrayList<String>(nssiMap.values())
        List<String> nsstSequence = execution.getVariable("nsstSequence")
        Integer currentIndex=0;
        execution.setVariable("currentNssiIndex",currentIndex)
        Integer maxIndex=keys.size()
        execution.setVariable("maxIndex",maxIndex)
        if(maxIndex>0)
            isMoreNSSI=true
        execution.setVariable("isMoreNSSI",isMoreNSSI)
        logger.trace("Exit prepareNSSIList in DoAllocateNSIandNSSI()")
    }


    void getOneNSSIInfo(DelegateExecution execution){
        logger.trace("Enter getOneNSSIInfo in DoAllocateNSIandNSSI()")

        //ServiceDecomposition serviceDecompositionObj = execution.getVariable("serviceDecompositionObj")
        Map<String, Object> nssiMap=execution.getVariable("nssiMap")
        List<String> nsstSequence = execution.getVariable("nsstSequence")
        String currentNSST= nsstSequence.get(execution.getVariable("currentNssiIndex"))
        boolean isNSSIOptionAvailable = false
        String nsstInput=nssiMap.get(currentNSST)
        execution.setVariable("nsstInput",nsstInput)
        String modelUuid = jsonUtil.getJsonValue(nsstInput, "modelUuid")
        String nssiInstanceId = jsonUtil.getJsonValue(nsstInput, "serviceInstanceId")
        String nssiserviceModelInfo = """{
            "modelInvariantUuid":"",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        Integer currentIndex = execution.getVariable("currentNssiIndex")
        currentIndex=currentIndex+1;
        execution.setVariable("currentNssiIndex",currentIndex)
        execution.setVariable("nssiserviceModelInfo",nssiserviceModelInfo)
        execution.setVariable("nssiInstanceId",nssiInstanceId)
        logger.trace("Exit getOneNSSIInfo in DoAllocateNSIandNSSI()")
    }

    void updateCurrentIndex(DelegateExecution execution){

        logger.trace("Enter updateCurrentIndex in DoAllocateNSIandNSSI()")
        Integer currentIndex = execution.getVariable("currentNssiIndex")
        Integer maxIndex = execution.getVariable("maxIndex")
        if(currentIndex>=maxIndex)
        {
            Boolean isMoreNSSI=false
            execution.setVariable("isMoreNSSI",isMoreNSSI)
        }
        logger.trace("Exit updateCurrentIndex in DoAllocateNSIandNSSI()")
    }
}
