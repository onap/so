
/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.apache.commons.lang3.tuple.ImmutablePair
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.core.domain.GroupResource
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ResourceType

import static org.apache.commons.lang3.StringUtils.isBlank

import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject
import org.onap.so.bpmn.common.recipe.BpmnRestClient
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.NetworkResource
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.infrastructure.properties.BPMNProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * input for script :
 * msoRequestId
 * isDebugLogEnabled
 * globalSubscriberId
 * serviceType
 * serviceInstanceId
 * URN_mso_workflow_sdncadapter_callback
 * serviceInputParams
 * deleteResourceList
 * resourceInstanceIDs
 *
 * output from script:
 *
 */

public class DoDeleteResourcesV1 extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteResourcesV1.class);

    String Prefix="DDR_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    CatalogDbUtils catalogDbUtils = new CatalogDbUtilsFactory().create()

    public void preProcessRequest (DelegateExecution execution) {
        logger.debug(" ***** preProcessRequest *****")
        String msg = ""

        try {
            String requestId = execution.getVariable("msoRequestId")
            execution.setVariable("prefix",Prefix)

            //Inputs
            //requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
            String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
            if (globalSubscriberId == null) {
                execution.setVariable("globalSubscriberId", "")
            }

            //requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
            String serviceType = execution.getVariable("serviceType")
            if (serviceType == null)
            {
                execution.setVariable("serviceType", "")
            }

            //Generated in parent for AAI PUT
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)){
                msg = "Input serviceInstanceId is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String sdncCallbackUrl = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback', execution)
            if (isBlank(sdncCallbackUrl)) {
                msg = "URN_mso_workflow_sdncadapter_callback is null"
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
            logger.debug("SDNC Callback URL: " + sdncCallbackUrl)

            StringBuilder sbParams = new StringBuilder()
            Map<String, String> paramsMap = execution.getVariable("serviceInputParams")
            if (paramsMap != null)
            {
                sbParams.append("<service-input-parameters>")
                for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                    String paramsXml
                    String paramName = entry.getKey()
                    String paramValue = entry.getValue()
                    paramsXml =
                            """	<param>
							<name>${MsoUtils.xmlEscape(paramName)}</name>
							<value>${MsoUtils.xmlEscape(paramValue)}</value>
							</param>
							"""
                    sbParams.append(paramsXml)
                }
                sbParams.append("</service-input-parameters>")
            }
            String siParamsXml = sbParams.toString()
            if (siParamsXml == null)
                siParamsXml = ""
            execution.setVariable("siParamsXml", siParamsXml)

        } catch (BpmnError e) {
            throw e;
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug(" ***** Exit preProcessRequest *****",)
    }

    public void sequenceResource(DelegateExecution execution){
        logger.debug(" ======== STARTED sequenceResource Process ======== ")
        List<Resource> sequencedResourceList = new ArrayList<Resource>()
        List<Resource> wanResources = new ArrayList<Resource>()

        // get delete resource list and order list
        List<ImmutablePair<Resource, List<Resource>>> delResourceList = execution.getVariable("deleteResourceList")

        ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
        String serviceModelName = serviceDecomposition.getModelInfo().getModelName();
        String serviceModelUuid = serviceDecomposition.getModelInfo().getModelUuid();

        Map<String, Map<String, Object>> parentVNF = new HashMap<>()

        // get Sequence from properties
        def resourceSequence = BPMNProperties.getResourceSequenceProp(serviceModelName)

        // get Sequence from catalog db csar(model)
        if(resourceSequence == null) {
            resourceSequence = ResourceRequestBuilder.getResourceSequence(serviceModelUuid)
            logger.info("Get Sequence from catalog db csar : " + resourceSequence)
        }

        if(resourceSequence != null) {
            for (resourceType in resourceSequence.reverse()) {

                for (ImmutablePair resourceTuple : delResourceList) {
                    Resource resource = resourceTuple.getKey()
                    List<Resource> groupResources = resourceTuple.getValue()

                    if (StringUtils.containsIgnoreCase(resource.getModelInfo().getModelName(), resourceType)) {



                        // if resource type is vnfResource then check for groups also
                        // Did not use continue because if same model type is used twice
                        // then we would like to add it twice for processing
                        // e.g.  S{ V1{G1, G2, G1}} --> S{ {G2, G1, G1}V1}
                        // we will add in reverse order for deletion
                        if (resource instanceof VnfResource) {
                            if (resource.getGroupOrder() != null && !StringUtils.isEmpty(resource.getGroupOrder())) {
                                String[] grpSequence = resource.getGroupOrder().split(",")

                                Map<String, Object> parentVNFData = new HashMap<>()
                                parentVNFData.put("vfModelInfo", resource.getModelInfo())
                                parentVNFData.put("vnf-id", resource.getResourceId())

                                for (String grpType in grpSequence.reverse()) {
                                    for (GroupResource gResource in groupResources) {
                                        if (StringUtils.containsIgnoreCase(gResource.getModelInfo().getModelName(), grpType)) {
                                            sequencedResourceList.add(gResource)
                                            // Store parent VNF info for the group resource id
                                            parentVNF.put(gResource.getResourceId(), parentVNFData)
                                        }
                                    }
                                }
                            }
                        }

                        sequencedResourceList.add(resource)

                        if (resource instanceof NetworkResource) {
                            wanResources.add(resource)
                        }
                    }
                }
            }
        }else {
            //define sequenced resource list, we deploy vf first and then network and then ar
            //this is defaule sequence
            // While deleting we will delete in resource order group resource, ar, network, then VF.
            List<VnfResource> vnfResourceList = new ArrayList<VnfResource>()
            List<AllottedResource> arResourceList = new ArrayList<AllottedResource>()
            for (ImmutablePair resourceTuple : delResourceList) {
                Resource rc = resourceTuple.getKey()
                List<Resource> groupResources = resourceTuple.getValue()

                if (rc instanceof VnfResource) {
                    vnfResourceList.add(rc)
                    if (rc.getGroupOrder() != null && !StringUtils.isEmpty(rc.getGroupOrder())) {
                        String[] grpSequence = rc.getGroupOrder().split(",")

                        Map<String, Object> parentVNFData = new HashMap<>()
                        parentVNFData.put("vfModelInfo", rc.getModelInfo())
                        parentVNFData.put("vnf-id", rc.getResourceId())

                        for (String grpType in grpSequence.reverse()) {
                            for (GroupResource gResource in groupResources) {
                                if (StringUtils.containsIgnoreCase(gResource.getModelInfo().getModelName(), grpType)) {
                                    sequencedResourceList.add(gResource)
                                    // Store parent VNF info for the group resource id
                                    parentVNF.put(gResource.getResourceId(), parentVNFData)
                                }
                            }
                        }
                    }
                } else if (rc instanceof NetworkResource) {
                	wanResources.add(rc)
                } else if (rc instanceof AllottedResource) {
                    arResourceList.add(rc)
                }
            }

            sequencedResourceList.addAll(arResourceList)
            sequencedResourceList.addAll(wanResources)
            sequencedResourceList.addAll(vnfResourceList)
        }

        String isContainsWanResource = wanResources.isEmpty() ? "false" : "true"
        //if no networkResource, get SDNC config from properties file
        if( "false".equals(isContainsWanResource)) {
            String serviceNeedSDNC = "mso.workflow.custom." + serviceModelName + ".sdnc.need";
            isContainsWanResource = BPMNProperties.getProperty(serviceNeedSDNC, isContainsWanResource)
        }
        execution.setVariable("isContainsWanResource", isContainsWanResource)
        execution.setVariable("currentResourceIndex", 0)
        execution.setVariable("sequencedResourceList", sequencedResourceList)
        execution.setVariable("parentVNF", parentVNF)
        logger.debug("resourceSequence: " + resourceSequence)
        logger.debug("delete resource sequence list : " + sequencedResourceList)
        logger.debug(" ======== END sequenceResource Process ======== ")
    }

    /**
     * prepare delete parameters
     */
    public void preResourceDelete(DelegateExecution execution){
        logger.debug(" ======== STARTED preResourceDelete Process ======== ")

        List<Resource> sequencedResourceList = execution.getVariable("sequencedResourceList")

        int currentIndex = execution.getVariable("currentResourceIndex")
        if(sequencedResourceList != null && sequencedResourceList.size() > currentIndex){
            Resource curResource = sequencedResourceList.get(currentIndex);

            String resourceInstanceUUID = curResource.getResourceId()
            String resourceTemplateUUID = curResource.getModelInfo().getModelUuid()
            execution.setVariable("resourceInstanceId", resourceInstanceUUID)
            execution.setVariable("currentResource", curResource)
            logger.debug("Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: "
                    + resourceInstanceUUID + " resourceModelName: " + curResource.getModelInfo().getModelName())
        }
        else {
            execution.setVariable("resourceInstanceId", "")
        }

        logger.debug(" ======== END preResourceDelete Process ======== ")
    }


    /**
     * Execute delete workflow for resource
     */
    public void executeResourceDelete(DelegateExecution execution) {
        logger.debug("======== Start executeResourceDelete Process ======== ")
        try {
            String requestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String serviceType = execution.getVariable("serviceType")

            String resourceInstanceId = execution.getVariable("resourceInstanceId")

            Resource currentResource = execution.getVariable("currentResource")
            String action = "deleteInstance"
            JSONObject resourceRecipe = catalogDbUtils.getResourceRecipe(execution, currentResource.getModelInfo().getModelUuid(), action)
            String recipeUri = resourceRecipe.getString("orchestrationUri")
            int recipeTimeout = resourceRecipe.getInt("recipeTimeout")
            String recipeParamXsd = resourceRecipe.get("paramXSD")


            ResourceInput resourceInput = new ResourceInput();
            resourceInput.setServiceInstanceId(serviceInstanceId)
            resourceInput.setResourceInstanceName(currentResource.getResourceInstanceName())
            resourceInput.setResourceInstancenUuid(currentResource.getResourceId())
            resourceInput.setOperationId(execution.getVariable("operationId"))
            resourceInput.setOperationType(execution.getVariable("operationType"))
            String globalSubscriberId = execution.getVariable("globalSubscriberId")
            resourceInput.setGlobalSubscriberId(globalSubscriberId)
            resourceInput.setResourceModelInfo(currentResource.getModelInfo());
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            resourceInput.setServiceModelInfo(serviceDecomposition.getModelInfo());
            resourceInput.setServiceType(serviceType)
            resourceInput.getResourceModelInfo().setModelType(currentResource.getResourceType().toString())
            if (currentResource.getResourceType() == ResourceType.GROUP) {
                Map<String, Map<String, Object>> parentVNF = execution.getVariable("parentVNF")
                if((null != parentVNF) && (null!=parentVNF.get(currentResource.getResourceId()))){
                    Map<String, Object> parentVNFData = parentVNF.get(currentResource.getResourceId())
                    ModelInfo parentVNFModel = parentVNFData.get("vfModelInfo")
                    String parentResourceId = parentVNFData.get("vnf-id")
                    resourceInput.setVfModelInfo(parentVNFModel)
                    resourceInput.setVnfId(parentResourceId)
                }
            }

            String recipeURL = BPMNProperties.getProperty("bpelURL", "http://so-bpmn-infra.onap:8081") + recipeUri

            BpmnRestClient bpmnRestClient = new BpmnRestClient()

            HttpResponse resp = bpmnRestClient.post(recipeURL, requestId, recipeTimeout, action, serviceInstanceId, serviceType, resourceInput.toString(), recipeParamXsd)
            logger.debug(" ======== END executeResourceDelete Process ======== ")
        } catch (BpmnError b) {
            logger.error("Rethrowing MSOWorkflowException")
            throw b
        } catch (Exception e) {
            logger.error("Error occured within DoDeleteResourcesV1 executeResourceDelete method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Internal Error - Occured during DoDeleteResourcesV1 executeResourceDelete Catalog")
        }
    }


    public void parseNextResource(DelegateExecution execution){
        logger.debug("======== Start parseNextResource Process ======== ")
        def currentIndex = execution.getVariable("currentResourceIndex")
        def nextIndex =  currentIndex + 1
        execution.setVariable("currentResourceIndex", nextIndex)
        List<String> sequencedResourceList = execution.getVariable("sequencedResourceList")
        if(nextIndex >= sequencedResourceList.size()){
            execution.setVariable("allResourceFinished", "true")
        }else{
            execution.setVariable("allResourceFinished", "false")
        }
        logger.debug("======== COMPLETED parseNextResource Process ======== ")
    }
    
    public void prepareFinishedProgressForResource(DelegateExecution execution) {

        String serviceInstanceId = execution.getVariable("serviceInstanceId")
        String serviceType = execution.getVariable("serviceType")
        String resourceInstanceId = execution.getVariable("resourceInstanceId")
        Resource currentResource = execution.getVariable("currentResource")
        String resourceCustomizationUuid = currentResource.getModelInfo().getModelCustomizationUuid()
        String resourceModelName = currentResource.getModelInfo().getModelName()
        String operationType = execution.getVariable("operationType")
        String progress = "100"
        String status = "finished"
        String statusDescription = "The resource instance does not exist for " + resourceModelName
        String operationId = execution.getVariable("operationId")

        String body = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                <soapenv:Body>
                    <ns:updateResourceOperationStatus>
                               <operType>${MsoUtils.xmlEscape(operationType)}</operType>
                               <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                               <progress>${MsoUtils.xmlEscape(progress)}</progress>
                               <resourceTemplateUUID>${MsoUtils.xmlEscape(resourceCustomizationUuid)}</resourceTemplateUUID>
                               <serviceId>${MsoUtils.xmlEscape(serviceInstanceId)}</serviceId>
                               <status>${MsoUtils.xmlEscape(status)}</status>
                               <statusDescription>${MsoUtils.xmlEscape(statusDescription)}</statusDescription>
                    </ns:updateResourceOperationStatus>
                </soapenv:Body>
                </soapenv:Envelope>""";

        def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
        execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
        execution.setVariable("CVFMI_updateResOperStatusRequest", body)
    }

    public void prepareSDNCServiceDeactivateRequest (DelegateExecution execution) {
    	prepareSDNCServiceRequest (execution, "deactivate")
    }
    
    public void prepareSDNCServiceDeleteRequest (DelegateExecution execution) {
    	prepareSDNCServiceRequest (execution, "delete")
    }
    
    public void prepareSDNCServiceRequest (DelegateExecution execution, String svcAction) {
        logger.debug(" ***** Started prepareSDNCServiceRequest for " + svcAction +  "*****")

        try {
            // get variables
            String sdnc_svcAction = svcAction        
            String sdncCallback = execution.getVariable("URN_mso_workflow_sdncadapter_callback")            
            String hdrRequestId = execution.getVariable("msoRequestId")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            String source = execution.getVariable("source")
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
            String serviceType = execution.getVariable("serviceType")
            String globalCustomerId = execution.getVariable("globalSubscriberId")
            String serviceModelInvariantUuid = serviceDecomposition.getModelInfo().getModelInvariantUuid()
            String serviceModelUuid = serviceDecomposition.getModelInfo().getModelUuid()
            String serviceModelVersion = serviceDecomposition.getModelInfo().getModelVersion()
            String serviceModelName = serviceDecomposition.getModelInfo().getModelName()

            // 1. prepare assign topology via SDNC Adapter SUBFLOW call
            String sndcTopologyDeleteRequest =
                    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                                              xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1" 
                                                              xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
                                 <sdncadapter:RequestHeader>
                                    <sdncadapter:RequestId>${MsoUtils.xmlEscape(hdrRequestId)}</sdncadapter:RequestId>
                                    <sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
                                    <sdncadapter:SvcAction>${MsoUtils.xmlEscape(sdnc_svcAction)}</sdncadapter:SvcAction>
                                    <sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>
                                    <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
                                    <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
                                 </sdncadapter:RequestHeader>
                                 <sdncadapterworkflow:SDNCRequestData>
                                     <request-information>
                                        <request-id>${MsoUtils.xmlEscape(hdrRequestId)}</request-id>
                                        <request-action>DeleteServiceInstance</request-action>
                                        <source>${MsoUtils.xmlEscape(source)}</source>
                                        <notification-url></notification-url>
                                        <order-number></order-number>
                                        <order-version></order-version>
                                     </request-information>
                                     <service-information>
                                        <service-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-id>
                                        <subscription-service-type>${MsoUtils.xmlEscape(serviceType)}</subscription-service-type>
                                        <onap-model-information>
                                             <model-invariant-uuid>${MsoUtils.xmlEscape(serviceModelInvariantUuid)}</model-invariant-uuid>
                                             <model-uuid>${MsoUtils.xmlEscape(serviceModelUuid)}</model-uuid>
                                             <model-version>${MsoUtils.xmlEscape(serviceModelVersion)}</model-version>
                                             <model-name>${MsoUtils.xmlEscape(serviceModelName)}</model-name>
                                        </onap-model-information>
                                        <service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
                                        <global-customer-id>${MsoUtils.xmlEscape(globalCustomerId)}</global-customer-id>
                                     </service-information>
                                     <service-request-input>
                                     </service-request-input>
                                </sdncadapterworkflow:SDNCRequestData>
                             </aetgt:SDNCAdapterWorkflowRequest>""".trim()
            
            String sndcTopologyDeleteRequesAsString = utils.formatXml(sndcTopologyDeleteRequest)
            logger.debug(sndcTopologyDeleteRequesAsString)
            execution.setVariable("sdncAdapterWorkflowRequest", sndcTopologyDeleteRequesAsString)
            logger.debug("sdncAdapterWorkflowRequest - " + "\n" +  sndcTopologyDeleteRequesAsString)

        } catch (Exception ex) {
            String exceptionMessage = " Bpmn error encountered in DoDeleteResourcesV1 flow. prepareSDNCServiceRequest() - " + ex.getMessage()
            logger.debug(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)

        }
       logger.debug("***** Exit prepareSDNCServiceRequest for " + svcAction +  "*****")
	}
}
