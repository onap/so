package org.onap.so.bpmn.infrastructure.scripts


import org.onap.aai.domain.yang.AllottedResource

import static org.apache.commons.lang3.StringUtils.*;

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.aai.domain.yang.OwningEntity
import org.onap.aai.domain.yang.ServiceProfile;
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.common.scripts.SDNCAdapterUtils
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.infrastructure.aai.groovyflows.AAICreateResources
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class DoCreateSliceServiceInstance extends AbstractServiceTaskProcessor{

    private static final Logger logger = LoggerFactory.getLogger( DoCreateSliceServiceInstance.class);
    JsonUtils jsonUtil = new JsonUtils()

    ExceptionUtil exceptionUtil = new ExceptionUtil()

    CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

    /**
     * Pre Process the BPMN Flow Request
     * Inclouds:
     * generate the nsOperationKey
     * generate the nsParameters
     */
    void preProcessRequest (DelegateExecution execution) {
        String msg = ""
        logger.trace("Enter preProcessRequest()")
        //Need update
        //1. Prepare service parameter.
        //2. Prepare slice profile parameters.

        String sliceserviceInstanceId = execution.getVariable("serviceInstanceId")
        String allottedResourceId = UUID.randomUUID().toString()
        execution.setVariable("sliceserviceInstanceId", sliceserviceInstanceId)
        execution.setVariable("allottedResourceId", allottedResourceId)

        String uuiRequest = execution.getVariable("uuiRequest")
        String modelInvariantUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceInvariantUuid")
        String modelUuid = jsonUtil.getJsonValue(uuiRequest, "service.serviceUuid")
        //here modelVersion is not set, we use modelUuid to decompose the service.
        def isDebugLogEnabled = true
        execution.setVariable("serviceInstanceId",sliceserviceInstanceId)
        execution.setVariable("isDebugLogEnabled",isDebugLogEnabled)
        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        execution.setVariable("serviceModelInfo", serviceModelInfo)

        logger.trace("Exit preProcessRequest")
    }


    void createServiceProfile(DelegateExecution execution) {

        String sliceserviceInstanceId = execution.getVariable("sliceserviceInstanceId")
        Map<String, Object> serviceProfileMap = execution.getVariable("serviceProfile")
        String serviceProfileID = UUID.randomUUID().toString()
        ServiceProfile serviceProfile = new ServiceProfile();
        serviceProfile.setProfileId(serviceProfileID)
        serviceProfile.setLatency(Integer.parseInt(serviceProfileMap.get("latency").toString()))
        serviceProfile.setMaxNumberOfUEs(Integer.parseInt(serviceProfileMap.get("maxNumberofUEs").toString()))
        serviceProfile.setCoverageAreaTAList(serviceProfileMap.get("coverageAreaTAList").toString())
        serviceProfile.setUeMobilityLevel(serviceProfileMap.get("uEMobilityLevel").toString())
        serviceProfile.setResourceSharingLevel(serviceProfileMap.get("resourceSharingLevel").toString())
        serviceProfile.setExpDataRateUL(Integer.parseInt(serviceProfileMap.get("expDataRateUL").toString()))
        serviceProfile.setExpDataRateDL(Integer.parseInt(serviceProfileMap.get("expDataRateDL").toString()))
        serviceProfile.setAreaTrafficCapUL(Integer.parseInt(serviceProfileMap.get("areaTrafficCapUL").toString()))
        serviceProfile.setAreaTrafficCapDL(Integer.parseInt(serviceProfileMap.get("areaTrafficCapDL").toString()))
        serviceProfile.setActivityFactor(Integer.parseInt(serviceProfileMap.get("activityFactor").toString()))

        serviceProfile.setJitter(0)
        serviceProfile.setSurvivalTime(0)
        serviceProfile.setCsAvailability(new Object())
        serviceProfile.setReliability(new Object())
        serviceProfile.setExpDataRate(0)
        serviceProfile.setTrafficDensity(0)
        serviceProfile.setConnDensity(0)
        try {
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_PROFILE, execution.getVariable("globalSubscriberId"),
                    execution.getVariable("subscriptionServiceType"), sliceserviceInstanceId, serviceProfileID)
            client.create(uri, serviceProfile)

        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in DoCreateSliceServiceInstance.instantiateSliceService. " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    void instantiateSliceService(DelegateExecution execution) {

        ServiceDecomposition serviceDecomposition= execution.getVariable("sliceServiceDecomposition")
        String uuiRequest = execution.getVariable("uuiRequest")
        ModelInfo modelInfo = serviceDecomposition.getModelInfo()
        String serviceRole = "e2eslice-service"
        String serviceType = execution.getVariable("serviceType")
        Map<String, Object> serviceProfile = execution.getVariable("serviceProfile")
        String ssInstanceId = execution.getVariable("serviceInstanceId")
        try {
            org.onap.aai.domain.yang.ServiceInstance ss = new org.onap.aai.domain.yang.ServiceInstance()
            ss.setServiceInstanceId(ssInstanceId)
            String sliceInstanceName = execution.getVariable("serviceInstanceName")
            ss.setServiceInstanceName(sliceInstanceName)
            ss.setServiceType(serviceType)
            String serviceStatus = "deactivated"
            ss.setOrchestrationStatus(serviceStatus)
            String modelInvariantUuid = modelInfo.getModelInvariantUuid()
            String modelUuid = modelInfo.getModelUuid()
            ss.setModelInvariantId(modelInvariantUuid)
            ss.setModelVersionId(modelUuid)
            String serviceInstanceLocationid = serviceProfile.get("plmnIdList")
            ss.setServiceInstanceLocationId(serviceInstanceLocationid)
            String snssai = serviceProfile.get("sNSSAI")
            ss.setEnvironmentContext(snssai)
            ss.setServiceRole(serviceRole)
            AAIResourcesClient client = new AAIResourcesClient()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, execution.getVariable("globalSubscriberId"), execution.getVariable("subscriptionServiceType"), ssInstanceId)
            client.create(uri, ss)
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
        rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", ssInstanceId)
        rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("subscriptionServiceType"))
        rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
        execution.setVariable("rollbackData", rollbackData)
        execution.setVariable("RollbackData", rollbackData)
        logger.debug("RollbackData:" + rollbackData)

    }


    void createAllottedResource(DelegateExecution execution) {
        String serviceInstanceId = execution.getVariable('sliceserviceInstanceId')

        AAIResourcesClient resourceClient = new AAIResourcesClient()
        AAIResourceUri ssServiceuri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId)

//        try {
//
//            if(resourceClient.exists(ssServiceuri)){
//                execution.setVariable("ssi_resourceLink", uri.build().toString())
//            }else{
//                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service instance was not found in aai to " +
//                        "associate allotted resource for service :"+serviceInstanceId)
//            }
//        }catch(BpmnError e) {
//            throw e;
//        }catch (Exception ex){
//            String msg = "Exception in getServiceInstance. " + ex.getMessage()
//            logger.debug(msg)
//            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
//        }

        try {
            String allottedResourceId = execution.getVariable("allottedResourceId")
            ServiceDecomposition serviceDecomposition = execution.getVariable("sliceServiceDecomposition")
            List<org.onap.so.bpmn.core.domain.AllottedResource> allottedResourceList = serviceDecomposition.getAllottedResources()
            for(org.onap.so.bpmn.core.domain.AllottedResource allottedResource : allottedResourceList)
            {
                //AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceFromParentURI(ssServiceuri, AAIObjectType.ALLOTTED_RESOURCE, allottedResourceId)
                AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIObjectType.ALLOTTED_RESOURCE,
                        execution.getVariable("globalSubscriberId"),execution.getVariable("subscriptionServiceType"),
                        execution.getVariable("sliceserviceInstanceId"), allottedResourceId)
                execution.setVariable("allottedResourceUri", allottedResourceUri)
                String arType = allottedResource.getAllottedResourceType()
                String arRole = allottedResource.getAllottedResourceRole()
                String modelInvariantId = allottedResource.getModelInfo().getModelInvariantUuid()
                String modelVersionId = allottedResource.getModelInfo().getModelUuid()

                org.onap.aai.domain.yang.AllottedResource resource = new org.onap.aai.domain.yang.AllottedResource()
                resource.setId(allottedResourceId)
                resource.setType(arType)
                resource.setAllottedResourceName("Allotted_"+ execution.getVariable("serviceInstanceName"))
                resource.setRole(arRole)
                resource.setModelInvariantId(modelInvariantId)
                resource.setModelVersionId(modelVersionId)
                getAAIClient().create(allottedResourceUri, resource)
                //AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceFromExistingURI(AAIObjectType.SERVICE_INSTANCE, UriBuilder.fromPath(ssServiceuri).build())
                //getAAIClient().connect(allottedResourceUri,ssServiceuri)
            }
            //execution.setVariable("aaiARPath", allottedResourceUri.build().toString());

        }catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception in createAaiAR " + ex.getMessage())
        }
    }

}
