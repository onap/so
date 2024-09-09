/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.scripts

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonOutput

import static org.hamcrest.CoreMatchers.instanceOf

import javax.json.JsonArray
import jakarta.ws.rs.core.Response
import org.apache.commons.collections.map.HashedMap
import org.apache.commons.lang.StringEscapeUtils
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.common.scripts.RequestDBUtil
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.aai.domain.yang.NetworkRoute
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v20.Relationship
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceProxy
import org.onap.so.bpmn.core.json.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aaiclient.client.aai.AAINamespaceConstants

import jakarta.ws.rs.NotFoundException

class DoAllocateCoreNonSharedSlice extends AbstractServiceTaskProcessor {
    String Prefix="DACNSNSSI_"
    private static final Logger logger = LoggerFactory.getLogger( DoAllocateCoreNonSharedSlice.class);
    private CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()
    private RequestDBUtil requestDBUtil = new RequestDBUtil()
    private ExceptionUtil exceptionUtil = new ExceptionUtil()
    private JsonUtils jsonUtil = new JsonUtils()

    @Override
    public void preProcessRequest(DelegateExecution execution) {
        logger.debug(Prefix+ "**** Enter DoAllocateCoreNonSharedSlice:::  preProcessRequest ****")
        String nssiServiceInstanceId= execution.getVariable("serviceInstanceId")
        logger.debug("nssiServiceInstanceId:  "+nssiServiceInstanceId)
        execution.setVariable("nssiServiceInstanceId", nssiServiceInstanceId)
        //Set orchestration-status as created
        execution.setVariable("orchestrationStatus", "created")
        //networkServiceName
        String networkServiceName = jsonUtil.getJsonValue(execution.getVariable("networkServiceModelInfo"), "modelInfo.modelName") ?: ""
        execution.setVariable("networkServiceName", networkServiceName.replaceAll(" Service Proxy", ""))
        //networkServiceModelUuid
        logger.debug("networkServiceName:  "+networkServiceName)
        String networkServiceModelUuid = jsonUtil.getJsonValue(execution.getVariable("networkServiceModelInfo"), "sourceModelUuid") ?: ""
        execution.setVariable("networkServiceModelUuid", networkServiceModelUuid)
        String sliceParams = execution.getVariable("sliceParams")
        logger.debug("sliceParams "+sliceParams)
        String bhEndPoints = jsonUtil.getJsonValue(sliceParams, "endPoint")
        if(bhEndPoints.empty) {
            logger.debug("End point info is empty")
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, "End point info is empty")
        }else {
            execution.setVariable("bh_endpoint", bhEndPoints)
        }
        logger.debug(Prefix+ " **** Exit DoAllocateCoreNonSharedSlice:::  preProcessRequest ****")
    }

    void createNSSIinAAI(DelegateExecution execution) {
        logger.debug(Prefix+ " ****  Enter DoAllocateCoreNonSharedSlice ::: Enter createNSSIinAAI ****")
        String msg=""
        String serviceInstanceId= execution.getVariable("nssiServiceInstanceId")
        logger.debug("ServiceInstanceId: "+serviceInstanceId)
        try {
            String serviceType = execution.getVariable("subscriptionServiceType")
            String oStatus = execution.getVariable("orchestrationStatus")
            String environmentContext = execution.getVariable("networkType")
            String workloadContext = "CN"
            String modelInvariantUuid = execution.getVariable("modelInvariantUuid")
            String nssiServiceType = execution.getVariable("sst")
            //set shared or non shared value from sliceProfile object in request
            String additionalParams = execution.getVariable("sliceParams")
            //Get resourceSharingLevel from sliceProfile
            String serviceFunction = jsonUtil.getJsonValue(additionalParams, "sliceProfile.resourceSharingLevel")
            String serviceInstanceName = execution.getVariable("servicename")
            ServiceInstance si = new ServiceInstance()
            si.setServiceInstanceId(execution.getVariable("nssiServiceInstanceId"))
            si.setServiceInstanceName(serviceInstanceName)
            si.setServiceType(nssiServiceType)
            si.setServiceRole("nssi")
            si.setOrchestrationStatus(oStatus)
            si.setModelInvariantId(modelInvariantUuid)
            si.setModelVersionId(execution.getVariable("modelUuid"))
            si.setEnvironmentContext(environmentContext)
            si.setWorkloadContext(workloadContext)
            si.setServiceFunction(serviceFunction)
            logger.debug("AAI service Instance Request Payload : "+si.toString())
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(execution.getVariable("globalSubscriberId")).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
            getAAIClient().create(uri, si)

            execution.setVariable("nssiServiceInstance", si)
        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            msg = "Exception in create AAI Instance" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        getVnfInstanceName(execution)
        logger.debug(Prefix+ " Exit DoAllocateCoreNonSharedSlice ::: Enter createNSSIinAAI ****")
    }

    public void prepareServiceOrderRequest(DelegateExecution execution) {
        logger.debug("**** Enter DoAllocateCoreNonSharedSlice :::  prepareServiceOrderRequest ****")
        //extAPI path hardcoded for testing purposes, will be updated in next patch
        String extAPIPath = "https://nbi.onap:8443/nbi/api/v4" + "/serviceOrder"
        execution.setVariable("ExternalAPIURL", extAPIPath)
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> serviceOrder = new LinkedHashMap()
        //ExternalId
        serviceOrder.put("externalId", "ONAP001")
        //Requested Start Date
        String requestedStartDate = utils.generateCurrentTimeInUtc()
        String requestedCompletionDate = utils.generateCurrentTimeInUtc()
        serviceOrder.put("requestedStartDate", requestedStartDate)
        serviceOrder.put("requestedCompletionDate", requestedCompletionDate)
        //RelatedParty Fields
        String relatedPartyId = execution.getVariable("globalSubscriberId")
        String relatedPartyRole = "ONAPcustomer"
        Map<String, String> relatedParty = new LinkedHashMap()
        relatedParty.put("id", relatedPartyId)
        relatedParty.put("role", relatedPartyRole)
        List<Map<String, String>> relatedPartyList = new ArrayList()
        relatedPartyList.add(relatedParty)
        serviceOrder.put("relatedParty", relatedPartyList)
        Map<String, Object> orderItem = new LinkedHashMap()
        //orderItem id
        String orderItemId = "1"
        orderItem.put("id", orderItemId)
        //order item action will always be add as we are triggering request for instantiation
        String orderItemAction = "add"
        orderItem.put("action", orderItemAction)
        // service Details
        Map<String, Object> service = new LinkedHashMap()
        //ServiceName
        String serviceName= "ns_"+execution.getVariable("networkServiceName")+"_"+execution.getVariable("serviceInstanceId")
        service.put("name",  serviceName)
        // Service Type
        service.put("serviceType", execution.getVariable("subscriptionServiceType"))
        //Service State
        service.put("serviceState", "active")
        Map<String, String> serviceSpecification = new LinkedHashMap()
        serviceSpecification.put("id", execution.getVariable("networkServiceModelUuid"))
        service.put("serviceSpecification", serviceSpecification)
        //serviceCharacteristic List
        List serviceCharacteristicList = new ArrayList()
        Map<String, Object> serviceCharacteristic = objectMapper.readValue(execution.getVariable("sliceProfile"), Map.class);
        List serviceCharacteristicListMap = retrieveServiceCharacteristicsAsKeyValue(execution, serviceCharacteristic)
        logger.debug("serviceCharacteristicListMap  "+serviceCharacteristicListMap)
        serviceCharacteristicList.add(serviceCharacteristic)
        //service.put("serviceCharacteristic", serviceCharacteristicList)
        service.put("serviceCharacteristic", serviceCharacteristicListMap)
        orderItem.put("service", service)
        List<Map<String, String>> orderItemList = new ArrayList()
        orderItemList.add(orderItem)
        serviceOrder.put("orderItem", orderItemList)
        String jsonServiceOrder = objectMapper.writeValueAsString(serviceOrder);
        logger.debug("******* ServiceOrder :: "+jsonServiceOrder)
        execution.setVariable("serviceOrderRequest", jsonServiceOrder)
        logger.debug(Prefix+ " **** Exit DoAllocateCoreNonSharedSlice ::: prepareServiceOrderRequest****")
    }

    private void getVnfInstanceName(DelegateExecution execution) {
        //Get NetworkService modelInvariantUuid
        String  networkServiceModelUuid = execution.getVariable("networkServiceModelUuid")
        String json = catalogDbUtils.getServiceResourcesByServiceModelUuid(execution, networkServiceModelUuid, "v2")
        logger.debug("json returned: "+json)
        logger.debug(("Service Vnfs JSON: "+jsonUtil.getJsonValue(json, "serviceResources.serviceVnfs")))
        List serviceVnfs = jsonUtil.StringArrayToList(jsonUtil.getJsonValue(json, "serviceResources.serviceVnfs"))
        String networkServiceVnfJson = serviceVnfs.get(0)
        String vnfInstanceName = (jsonUtil.getJsonValue(networkServiceVnfJson, "modelInfo.modelInstanceName")).replace(" ","") ?: ""
        execution.setVariable("vnfInstanceName", vnfInstanceName)
    }

    private List retrieveServiceCharacteristicsAsKeyValue(DelegateExecution execution, Map serviceCharacteristics) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreNonSharedSlice ::: retrieveServiceCharacteristicsAsKeyValue ****")
        List serviceCharacteristicsList = new ArrayList()
        ObjectMapper mapperObj = new ObjectMapper();
        String vnfInstanceName = execution.getVariable("vnfInstanceName")
        Map<String, Object> serviceCharacteristicsObject = new LinkedHashMap()
        for (Map.Entry<String, Integer> entry : serviceCharacteristics.entrySet()) {
            Map<String, Object> ServiceCharacteristicValueObject = new LinkedHashMap<>()
            System.out.println(entry.getKey() + ":" + entry.getValue());
            //For G Release we are sending single value from  snssaiList
            if(entry.getKey().equals("snssaiList")) {
                List sNssaiValue = entry.getValue()
                serviceCharacteristicsObject.put("name", vnfInstanceName+"_snssai")
                ServiceCharacteristicValueObject.put("serviceCharacteristicValue", sNssaiValue.get(0))
                serviceCharacteristicsObject.put("value", ServiceCharacteristicValueObject)
            }
        }
        serviceCharacteristicsList.add(serviceCharacteristicsObject)
        logger.debug(Prefix+ " **** Exit DoAllocateCoreNonSharedSlice ::: retrieveServiceCharacteristicsAsKeyValue ****")
        return serviceCharacteristicsList
    }

    public void postNBIServiceOrder(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreNonSharedSlice ::: postNBIServiceOrder ****")
        String msg=""
        try {
            String extAPIPath = execution.getVariable("ExternalAPIURL")
            String payload = execution.getVariable("serviceOrderRequest")
            logger.debug("externalAPIURL is: " + extAPIPath)
            logger.debug("ServiceOrder payload is: " + payload)
            ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()
            execution.setVariable("ServiceOrderId", "")
            Response response = externalAPIUtil.executeExternalAPIPostCall(execution, extAPIPath, payload)
            int responseCode = response.getStatus()
            execution.setVariable("PostServiceOrderResponseCode", responseCode)
            logger.debug("Post ServiceOrder response code is: " + responseCode)
            String extApiResponse = response.readEntity(String.class)
            JSONObject responseObj = new JSONObject(extApiResponse)
            execution.setVariable("PostServiceOrderResponse", extApiResponse)
            logger.debug("ServiceOrder response body is: " + extApiResponse)
            //Process Response
            if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
                //200 OK 201 CREATED 202 ACCEPTED
            {
                logger.debug("Post ServiceOrder Received a Good Response")
                String serviceOrderId = responseObj.get("id")
                execution.setVariable("ServiceOrderId", serviceOrderId)
                logger.info("Post ServiceOrderid is: " + serviceOrderId)
            }
            else{
                exceptionUtil.buildAndThrowWorkflowException(execution, responseCode, "Post ServiceOrder Received a bad response from extAPI serviceOrder API")
            }
        }catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            msg = "Exception in ServiceOrder ExtAPI" + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("**** Exit DoAllocateCoreNonSharedSlice ::: postNBIServiceOrder ****")
    }

    public void getNBIServiceOrderProgress(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreNonSharedSlice ::: getNBIServiceOrderProgress ****")
        String msg=""
        try {
            String extAPIPath = execution.getVariable("ExternalAPIURL")
            extAPIPath += "/" + execution.getVariable("ServiceOrderId")
            logger.debug("getNBIServiceOrderProgress externalAPIURL is: " + extAPIPath)
            ExternalAPIUtil externalAPIUtil = new ExternalAPIUtilFactory().create()
            Response response = externalAPIUtil.executeExternalAPIGetCall(execution, extAPIPath)
            int responseCode = response.getStatus()
            execution.setVariable("GetServiceOrderResponseCode", responseCode)
            logger.debug("Get ServiceOrder response code is: " + responseCode)
            String extApiResponse = response.readEntity(String.class)
            JSONObject responseObj = new JSONObject(extApiResponse)
            execution.setVariable("GetServiceOrderResponse", extApiResponse)
            logger.debug("Create response body is: " + extApiResponse)
            //Process Response //200 OK 201 CREATED 202 ACCEPTED
            if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
            {
                logger.debug("Get Create ServiceOrder Received a Good Response")
                String orderState = responseObj.get("state")
                if("REJECTED".equalsIgnoreCase(orderState)) {
                    prepareFailedOperationStatusUpdate(execution)
                    return
                }
                JSONArray items = responseObj.getJSONArray("orderItem")
                JSONObject item = items.get(0)
                JSONObject service = item.get("service")
                String networkServiceId = service.get("id")

                execution.setVariable("networkServiceId", networkServiceId)
                String serviceOrderState = item.get("state")
                execution.setVariable("ServiceOrderState", serviceOrderState)
                // Get serviceOrder State and process progress
                if("ACKNOWLEDGED".equalsIgnoreCase(serviceOrderState) || "INPROGRESS".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable("status", "processing")
                }
                else if("COMPLETED".equalsIgnoreCase(serviceOrderState)) {
                    execution.setVariable("status", "completed")
                }
                else if("FAILED".equalsIgnoreCase(serviceOrderState)) {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
                }
                else {
                    msg = "ServiceOrder failed"
                    exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  msg)
                }
                logger.debug("NBI serviceOrder state: "+serviceOrderState)
            }
            else{
                msg = "Get ServiceOrder Received a Bad Response Code. Response Code is: " + responseCode
                prepareFailedOperationStatusUpdate(execution)
            }

        }catch(Exception e){
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000,  e.getMessage())
        }
        logger.debug(Prefix+ " **** Exit DoAllocateCoreNonSharedSlice ::: getNBIServiceOrderProgress ****")
    }

    public void timeDelay(DelegateExecution execution) {
        try {
            logger.debug(Prefix+ " **** DoAllocateCoreNonSharedSlice ::: timeDelay going to sleep for 5 sec")
            Thread.sleep(5000)
            logger.debug("**** DoAllocateCoreNonSharedSlice ::: timeDelay wakeup after 5 sec")
        } catch(InterruptedException e) {
            logger.error(Prefix+ " **** DoAllocateCoreNonSharedSlice ::: timeDelay exception" + e)
        }
    }

    void updateRelationship(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreNonSharedSlice ::: updateRelationship ****")
        String networkServiceInstanceId = execution.getVariable("networkServiceId")
        String nssiId = execution.getVariable("nssiServiceInstanceId")
        String globalCustId = execution.getVariable("globalSubscriberId")
        String serviceType = execution.getVariable("subscriptionServiceType")
        logger.debug("networkServiceInstanceId: {},  nssiId: {}, globalCustId: {}, serviceType: {}", networkServiceInstanceId, nssiId, globalCustId, serviceType)
        try{
            //Update NSSI orchestration status nssiServiceInstance
            ServiceInstance si = execution.getVariable("nssiServiceInstance")
            logger.debug("nssiServiceInstance "+si)
            si.setOrchestrationStatus("deactivated")
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalCustId).serviceSubscription(serviceType).serviceInstance(nssiId))
            logger.debug("uri to call:  "+uri)

            try {
                getAAIClient().update(uri, si)
            } catch (Exception e) {
                logger.info("Update OrchestrationStatus in AAI failed")
                String msg = "Update OrchestrationStatus in AAI failed, " + e
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
            //URI for NSSI
            AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId));
            logger.debug("nssiUri to update RelationShip :  "+nssiUri)
            //URI for Network Service Instance
            AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(networkServiceInstanceId))
            logger.debug("networkServiceInstanceUri to update RelationShip :  "+networkServiceInstanceUri)
            // Update Relationship in AAI
            getAAIClient().connect(nssiUri, networkServiceInstanceUri, AAIEdgeLabel.COMPOSED_OF);
            //end point update
            createEndPointsInAai(execution)
            execution.setVariable("progress", "100")
            execution.setVariable("status", "finished")
            execution.setVariable("statusDescription", "DoAllocateCoreNonSharedNSSI success")
            setResourceOperationStatus(execution)
        }catch(Exception ex) {
            String msg = "Exception while creating relationship " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(Prefix+ " **** Exit DoAllocateCoreNonSharedSlice ::: updateRelationship ****")
    }

    /**
     * creates EndPoints in AAI
     * @param execution
     */
    private void createEndPointsInAai(DelegateExecution execution) {
        String type = "endpoint"
        String function = "core_EP"
        int prefixLength = 24
        String addressFamily = "ipv4"
        //BH end point update
        String bh_endpoint = execution.getVariable("bh_endpoint")
        String bh_routeId = UUID.randomUUID().toString()
        execution.setVariable("coreEp_ID_bh", bh_routeId)
        String role = "CN"
        String cnIpAddress = jsonUtil.getJsonValue(bh_endpoint, "ipAddress")
        String LogicalLinkId = jsonUtil.getJsonValue(bh_endpoint, "logicInterfaceId")
        String nextHopInfo = jsonUtil.getJsonValue(bh_endpoint, "nextHopInfo")
        NetworkRoute bh_ep = new NetworkRoute()
        logger.debug("bh_endpoint: {}, bh_routeId: {}, cnIpAddress: {}, role: {}, LogicalLinkId: {}, nextHopInfo: {}, bh_ep: {}", bh_endpoint, bh_routeId, cnIpAddress, role, LogicalLinkId, nextHopInfo, bh_ep)
        bh_ep.setRouteId(bh_routeId)
        bh_ep.setFunction(function)
        bh_ep.setRole(role)
        bh_ep.setType(type)
        bh_ep.setIpAddress(cnIpAddress)
        bh_ep.setLogicalInterfaceId(LogicalLinkId)
        bh_ep.setNextHop(nextHopInfo)
        bh_ep.setPrefixLength(prefixLength)
        bh_ep.setAddressFamily(addressFamily)
        try {
            AAIResourcesClient client = new AAIResourcesClient()
            logger.debug("creating bh endpoint . ID : "+bh_routeId+" node details : "+bh_ep.toString())
            AAIResourceUri networkRouteUri = AAIUriFactory.createResourceUri( new AAIObjectType(AAINamespaceConstants.NETWORK, NetworkRoute.class), bh_routeId)
            client.create(networkRouteUri, bh_ep)
            //relationship b/w bh_ep and Core NSSI
            String coreNssi = execution.getVariable("nssiServiceInstanceId")
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            String subscriptionServiceType = execution.getVariable("subscriptionServiceType")
            Relationship relationship = new Relationship()
            String relatedLink = "aai/v21/network/network-routes/network-route/${bh_routeId}"
            relationship.setRelatedLink(relatedLink)
            relationship.setRelatedTo("network-route")
            relationship.setRelationshipLabel("org.onap.relationships.inventory.ComposedOf")
            logger.debug("networkRouteUri: {}, relationship: {}",networkRouteUri,relationship)
            try {
                AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(coreNssi)).relationshipAPI()
                logger.debug("uri: "+uri)
                client.create(uri, relationship)
            } catch (BpmnError e) {
                throw e
            } catch (Exception ex) {
                String msg = "Exception in createRelationShipInAAI. " + ex.getMessage()
                logger.debug(msg+": "+ex)
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
            }
        } catch (BpmnError e) {
            throw e
        } catch (Exception ex) {
            String msg = "Exception in createEndPointsInAai " + ex.getMessage()
            logger.info(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
    }

    private void setResourceOperationStatus(DelegateExecution execution) {
        logger.debug(Prefix+ " **** Enter DoAllocateCoreNonSharedSlice ::: setResourceOperationStatus ****")
        String serviceId = execution.getVariable("nsiId")
        String jobId = execution.getVariable("jobId")
        String nssiId = execution.getVariable("nssiServiceInstanceId")
        String operationType = "ALLOCATE"
        String modelUuid= execution.getVariable("modelUuid")
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setJobId(jobId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setResourceInstanceID(nssiId)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setStatus(execution.getVariable("status"))
        resourceOperationStatus.setProgress(execution.getVariable("progress"))
        resourceOperationStatus.setStatusDescription(execution.getVariable("statusDescription"))
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix+ " **** Exit DoAllocateCoreNonSharedSlice ::: setResourceOperationStatus ****")
    }

    void prepareFailedOperationStatusUpdate(DelegateExecution execution){
        logger.debug(Prefix + " **** Enter DoAllocateCoreNonSharedSlice ::: prepareFailedOperationStatusUpdate ****")
        String serviceId = execution.getVariable("nsiId")
        String jobId = execution.getVariable("jobId")
        String nssiId = execution.getVariable("nssiServiceInstanceId")
        String operationType = "ALLOCATE"
        //modelUuid
        String modelUuid= execution.getVariable("modelUuid")
        logger.debug("serviceId: {}, jobId: {}, nssiId: {}, operationType: {}.", serviceId, jobId, nssiId, operationType)
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus()
        resourceOperationStatus.setServiceId(serviceId)
        resourceOperationStatus.setJobId(jobId)
        resourceOperationStatus.setOperationId(jobId)
        resourceOperationStatus.setResourceInstanceID(nssiId)
        resourceOperationStatus.setResourceTemplateUUID(modelUuid)
        resourceOperationStatus.setOperType(operationType)
        resourceOperationStatus.setProgress("0")
        resourceOperationStatus.setStatus("failed")
        resourceOperationStatus.setStatusDescription("Core NSSI Allocate Failed")
        requestDBUtil.prepareUpdateResourceOperationStatus(execution, resourceOperationStatus)
        logger.debug(Prefix + " **** Exit DoAllocateCoreNonSharedSlice ::: prepareFailedOperationStatusUpdate ****")
    }
}