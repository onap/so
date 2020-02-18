/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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


import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.RelatedToProperty
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipData
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.domain.GroupResource
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ResourceType
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.VnfResource
import org.onap.so.bpmn.core.domain.VnfcResource
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.msb.sdk.discovery.common.RouteException
import org.springframework.web.util.UriUtils

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.entity.StringEntity
import org.apache.http.entity.ContentType

import java.util.ArrayList;
import java.util.List;

import org.onap.so.bpmn.core.domain.OperationStatus

import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * This groovy class supports the <class>DoDeleteE2EServiceInstance.bpmn</class> process.
 *
 * Inputs:
 * @param - msoRequestId
 * @param - globalSubscriberId - O
 * @param - subscriptionServiceType - O
 * @param - serviceInstanceId
 * @param - serviceInstanceName - O
 * @param - serviceInputParams (should contain aic_zone for serviceTypes TRANSPORT,ATM)
 * @param - sdncVersion
 * @param - failNotFound - TODO
 * @param - serviceInputParams - TODO
 *
 * @param - delResourceList
 * @param - serviceRelationShip
 *
 * Outputs:
 * @param - WorkflowException
 *
 * Rollback - Deferred
 */
public class DoDeleteE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DDEESI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    JsonUtils jsonUtil = new JsonUtils()
    private static final Logger logger = LoggerFactory.getLogger( DoDeleteE2EServiceInstance.class)


    public void preProcessRequest (DelegateExecution execution) {
        logger.debug(" ***** preProcessRequest *****")
        String msg = ""

        try {
            String requestId = execution.getVariable("msoRequestId")
            execution.setVariable("prefix",Prefix)

            //Inputs
            //requestDetails.subscriberInfo. for AAI GET & PUT & SDNC assignToplology
            String globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId
            if (globalSubscriberId == null)
            {
                execution.setVariable("globalSubscriberId", "")
            }

            //requestDetails.requestParameters. for AAI PUT & SDNC assignTopology
            String serviceType = execution.getVariable("serviceType")
            if (serviceType == null)
            {
                execution.setVariable("serviceType", "")
				logger.info("ServiceType variable is null")
            }
			logger.info(" serviceType Variable set "+execution.getVariable("serviceType"))

            //Generated in parent for AAI PUT
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)){
                msg = "Input serviceInstanceId is null"
                logger.info(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String sdncCallbackUrl = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback', execution)
            if (isBlank(sdncCallbackUrl)) {
                msg = "URN_mso_workflow_sdncadapter_callback is null"
                logger.info(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
            logger.info("SDNC Callback URL: " + sdncCallbackUrl)

            StringBuilder sbParams = new StringBuilder()
            Map<String, String> paramsMap = execution.getVariable("serviceInputParams")

            if (paramsMap != null) {
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
            throw e
        } catch (Exception ex){
            msg = "Exception in preProcessRequest " + ex.getMessage()
            logger.error(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        logger.debug("***** Exit preProcessRequest *****")
    }

    public void postProcessAAIGET(DelegateExecution execution) {
        logger.debug(" ***** postProcessAAIGET ***** ")
        String msg = ""

        try {
            String serviceInstanceId = execution.getVariable('serviceInstanceId')
            String globalSubscriberId = execution.getVariable('globalSubscriberId')
            String serviceType = execution.getVariable('serviceType')
            AAIResourcesClient resourceClient = new AAIResourcesClient()
            AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalSubscriberId, serviceType, serviceInstanceId)
            if (!resourceClient.exists(serviceInstanceUri)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Service Instance was not found in aai")
            }
            AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
            Optional<ServiceInstance> si = wrapper.asBean(ServiceInstance.class)
            // found in AAI
            if (si.isPresent() && StringUtils.isNotEmpty(si.get().getServiceInstanceName())) {
                logger.debug("Found Service-instance in AAI")
                execution.setVariable("serviceInstanceName", si.get().getServiceInstanceName())
                // get model invariant id
                // Get Template uuid and version
                if ((null != si.get().getModelInvariantId()) && (null != si.get().getModelVersionId())) {
                    logger.debug("SI Data model-invariant-id and model-version-id exist")
                    // Set Original Template info
                    execution.setVariable("model-invariant-id-original", si.get().getModelInvariantId())
                    execution.setVariable("model-version-id-original", si.get().getModelVersionId())
                }
                if ((null != si.get().getRelationshipList()) && (null != si.get().getRelationshipList().getRelationship())) {
                    logger.debug("SI Data relationship-list exists")
                    List<Relationship> relationshipList = si.get().getRelationshipList().getRelationship()
                    JSONArray jArray = new JSONArray()
                    for (Relationship relationship : relationshipList) {
                        def jObj = getRelationShipData(relationship)
                        jArray.put(jObj)
                    }
                    execution.setVariable("serviceRelationShip", jArray.toString())
                }
            } else {
                msg = "Service-instance: " + serviceInstanceId + " NOT found in AAI."
                logger.error(msg)
                exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
            }
        } catch (BpmnError e) {
            throw e
        } catch (NotFoundException e) {
            logger.debug("Service Instance does not exist AAI")
            exceptionUtil.buildAndThrowWorkflowException(execution, 404, "Service Instance was not found in aai")
        } catch (Exception ex) {
            msg = "Exception in DoDeleteE2EServiceInstance.postProcessAAIGET. " + ex.getMessage()
            logger.debug(msg)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
		
        logger.debug(" *** Exit postProcessAAIGET *** ")
    }

    private JSONObject getRelationShipData(Relationship relationship) {
        JSONObject jObj = new JSONObject()
        def rt = relationship.getRelatedTo()
        def rl = relationship.getRelatedLink()
        logger.debug("ServiceInstance Related NS/Configuration :" + rl)
        List<RelationshipData> rl_datas = relationship.getRelationshipData()
        for (RelationshipData rl_data : rl_datas) {
            def eKey = rl_data.getRelationshipKey()
            def eValue = rl_data.getRelationshipValue()
            if ((rt.equals("service-instance") && eKey.equals("service-instance.service-instance-id"))
                    //for overlay/underlay
                    || (rt.equals("configuration") && eKey.equals("configuration.configuration-id")
            )) {
                jObj.put("resourceInstanceId", eValue)
            }
            else if (rt.equals("allotted-resource") && eKey.equals("allotted-resource.id")){
                jObj.put("resourceInstanceId", eValue)
            }
            // for sp-partner and others
            else if (eKey.endsWith("-id")) {
                jObj.put("resourceInstanceId", eValue)
                String resourceName = rt + eValue
                jObj.put("resourceType", resourceName)
            }
            jObj.put("resourceLinkUrl", rl)
        }
        List<RelatedToProperty> rl_props = relationship.getRelatedToProperty()
        for (RelatedToProperty rl_prop : rl_props) {
            def eKey = rl_prop.getPropertyKey()
            def eValue = rl_prop.getPropertyValue()
            if ((rt.equals("service-instance") && eKey.equals("service-instance.service-instance-name"))
                    //for overlay/underlay
                    || (rt.equals("configuration") && eKey.equals("configuration.configuration-type"))) {
                jObj.put("resourceType", eValue)
            }
        }
        logger.debug("Relationship related to Resource:" + jObj.toString())
        return jObj
    }

    private Relationship getRelationShipFromNode(groovy.util.slurpersupport.Node relationshipNode) {
        Relationship relationship = new Relationship()
        def rtn = relationshipNode.childNodes()
        List<RelationshipData> relationshipDatas = new ArrayList<>()
        List<RelatedToProperty> relationshipProperties = new ArrayList<>()
        while (rtn.hasNext()) {
            groovy.util.slurpersupport.Node node = rtn.next()
            def key = node.name()

            if(key.equals("related-to")){
                def rt = node.text()
                relationship.setRelatedTo(rt)
            } else if (key.equals("related-link")){
                def rl = node.text()
                relationship.setRelatedLink(rl)
            } else if (key.equals("relationship-label")){
                def label = node.text()
                relationship.setRelationshipLabel(label)
            } else if (key.equals("relationship-data")){
                def rData = node.childNodes()
                RelationshipData relationshipData = new RelationshipData()
                while(rData.hasNext()){
                    groovy.util.slurpersupport.Node datanode = rData.next()
                    def dataKey = datanode.name()
                    if(dataKey.equals("relationship-key")) {
                        relationshipData.setRelationshipKey(datanode.text())
                    } else if(dataKey.equals("relationship-value")) {
                        relationshipData.setRelationshipValue(datanode.text())
                    }
                }
                relationshipDatas.add(relationshipData)
            } else if (key.equals("related-to-property")){
                def rProperty = node.childNodes()
                RelatedToProperty relationshipProperty = new RelatedToProperty()
                while(rProperty.hasNext()){
                    groovy.util.slurpersupport.Node propnode = rProperty.next()

                    def dataKey = propnode.name()
                    if(dataKey.equals("property-key")) {
                        relationshipProperty.setPropertyKey(propnode.text())
                    } else if(dataKey.equals("property-value")) {
                        relationshipProperty.setPropertyValue(propnode.text())
                    }

                }
                relationshipProperties.add(relationshipProperty)
            }

        }
        relationship.getRelationshipData().addAll(relationshipDatas)
        relationship.getRelatedToProperty().addAll(relationshipProperties)

        logger.debug("Relationship related to Resource:" + relationship.toString())
        return relationship
    }

   public void getCurrentNS(DelegateExecution execution){
       logger.info( "======== Start getCurrentNS Process ======== ")

       def currentIndex = execution.getVariable("currentNSIndex")
       List<String> nsSequence = execution.getVariable("nsSequence")
       String nsResourceType =  nsSequence.get(currentIndex)

       // GET AAI by Name, not ID, for process convenient
       execution.setVariable("GENGS_type", "service-instance")
       execution.setVariable("GENGS_serviceInstanceId", "")
       execution.setVariable("GENGS_serviceInstanceName", nsResourceType)

       logger.debug("======== COMPLETED getCurrentNS Process ======== ")
   }

    public void prepareDecomposeService(DelegateExecution execution) {
        try {
            logger.debug(" ***** Inside prepareDecomposeService of create generic e2e service ***** ")
            String modelInvariantUuid = execution.getVariable("model-invariant-id-original")
            String modelVersionId = execution.getVariable("model-version-id-original")

            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelVersionId}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            logger.debug(" ***** Completed prepareDecomposeService of  create generic e2e service ***** ")
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            logger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

	private void generateRelatedResourceInfo(String response, JSONObject jObj, boolean processRelationship){

		def xml = new XmlSlurper().parseText(response)
		def rtn = xml.childNodes()
		while (rtn.hasNext()) {
			groovy.util.slurpersupport.Node node = rtn.next()
			def key = node.name()
            if (key.equals("relationship-list") && processRelationship) {
                def relns = node.childNodes()
                JSONArray jArray = new JSONArray()
                while (relns.hasNext()) {
                    groovy.util.slurpersupport.Node relNode = relns.next()
                    Relationship relationship = getRelationShipFromNode(relNode)
                    def relationObj = getRelationShipData(relationship)
                    jArray.put(relationObj)
                }
                jObj.put(key, jArray)
            } else {
                def value = node.text()
                jObj.put(key, value)
            }
		}
	}

	private JSONObject getRelatedResourceInAAI (DelegateExecution execution, JSONObject jObj, boolean processRelationship)
	{
		logger.debug(" ***** Started getRelatedResourceInAAI *****")

        String aai_endpoint = UrnPropertiesReader.getVariable("aai.endpoint", execution)
		String urlLink = jObj.get("resourceLinkUrl")
		String serviceAaiPath = "${aai_endpoint}${urlLink}"

		URL url = new URL(serviceAaiPath)
		HttpClient client = new HttpClientFactory().newXmlClient(url, ONAPComponents.AAI)
        client.addBasicAuthHeader(UrnPropertiesReader.getVariable("aai.auth", execution), UrnPropertiesReader.getVariable("mso.msoKey", execution))
        client.addAdditionalHeader("X-FromAppId", "MSO")
        client.addAdditionalHeader("X-TransactionId", utils.getRequestID())
        client.setAcceptType(MediaType.APPLICATION_XML)

		Response response = client.get()
		int responseCode = response.getStatus()
		execution.setVariable(Prefix + "GeRelatedResourceResponseCode", responseCode)
		logger.debug("  Get RelatedResource code is: " + responseCode)

		String aaiResponse = response.readEntity(String.class)
		execution.setVariable(Prefix + "GetRelatedResourceResponse", aaiResponse)

		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			logger.debug("GET RelatedResource Received a Good Response")
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable(Prefix + "FoundIndicator", true)

			generateRelatedResourceInfo(aaiResponse, jObj, processRelationship)

			//get model-invariant-uuid and model-uuid
			String modelInvariantId = ""
			String modelUuid = ""
			String modelCustomizationId = ""
			if(jObj.has("model-invariant-id")) {
				modelInvariantId = jObj.get("model-invariant-id")
				modelUuid = jObj.get("model-version-id")
                if (jObj.has("model-customization-id")) {
                    modelCustomizationId = jObj.get("model-customization-id")
                } else {
                    logger.info("resource customization id is not found for :" + url)
                }
			}

			jObj.put("modelInvariantId", modelInvariantId)
			jObj.put("modelVersionId", modelUuid)
			jObj.put("modelCustomizationId", modelCustomizationId)
            logger.info("resource detail from AAI:" + jObj)
		}
		else {
            String exceptionMessage = "Get RelatedResource Received a Bad Response Code. Response Code is: " + responseCode
			logger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }

		logger.debug(" ***** Exit getRelatedResourceInAAI *****")
		return jObj
	}

    public void postDecomposeService(DelegateExecution execution) {
        logger.debug(" ***** Inside postDecomposeService() of  delete generic e2e service flow ***** ")
        try {
            ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")

            // service model info
            execution.setVariable("serviceModelInfo", serviceDecomposition.getModelInfo())

            List<Resource> deleteResourceList = serviceDecomposition.getServiceResources()
            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            def jsonSlurper = new JsonSlurper()
            def jsonOutput = new JsonOutput()

            List relationShipList = null
            if (serviceRelationShip != null) {
                relationShipList = jsonSlurper.parseText(serviceRelationShip)
            }

            List<ImmutablePair<Resource, List<Resource>>> deleteRealResourceList = new ArrayList<ImmutablePair<Resource, List<Resource>>>()

            //Set the real resource instance id to the decomosed resource list
            //reset the resource instance id , because in the decompose flow ,its a random one.
            //match the resource-instance-name and the model name
            if (relationShipList != null) {
                relationShipList.each {

                    JSONObject obj = getRelatedResourceInAAI(execution, (JSONObject)it, true)

                    for (Resource resource : deleteResourceList) {

                        boolean matches = processMatchingResource(resource, obj)
                        if((matches) && resource.getResourceType().equals(ResourceType.VNF))  {
                            List<Resource> delGroupList = new ArrayList<Resource>()
                            JSONArray vfRelationship = obj.getJSONArray("relationship-list")
                            for (int idx = 0; idx < vfRelationship.length(); idx++) {
                                JSONObject vfItem = vfRelationship.getJSONObject(idx)
                                JSONObject groupObject = getRelatedResourceInAAI(execution, vfItem, false)
                                List<GroupResource> groups = ((VnfResource)resource).getGroups()
                                for (GroupResource group : groups){
                                    if(processMatchingResource(group, groupObject)){
                                        delGroupList.add(group)
                                    }
                                }
                            }
                            def delMap = new ImmutablePair(resource, delGroupList)

                            deleteRealResourceList.add(delMap)
                        }
                    }
                }
            }

            // only delete real existing resources
            execution.setVariable("deleteResourceList", deleteRealResourceList)
            
            boolean isDeleteResourceListValid = false
            if(deleteRealResourceList.size() > 0) {
                isDeleteResourceListValid = true
            }
            execution.setVariable("isDeleteResourceListValid", isDeleteResourceListValid)

            logger.debug("delete resource list : " + deleteRealResourceList)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
            logger.error(exceptionMessage)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        logger.debug(" ***** exit postDecomposeService() of  delete generic e2e service flow ***** ")
    }

    private boolean processMatchingResource(Resource resource, JSONObject obj) {
        boolean matches = false
        String modelName = resource.getModelInfo().getModelName()

        String modelCustomizationUuid = resource.getModelInfo().getModelCustomizationUuid()
        String modelUuid = resource.getModelInfo().getModelUuid()
        if (StringUtils.containsIgnoreCase(obj.get("resourceType"), modelName)) {
            resource.setResourceId(obj.get("resourceInstanceId"))
            //deleteRealResourceList.add(resource)
            matches = true
        } else if (modelCustomizationUuid.equals(obj.get("modelCustomizationId")) || modelUuid.equals(obj.get("model-version-id")) ) {
            resource.setResourceId(obj.get("resourceInstanceId"))
            resource.setResourceInstanceName(obj.get("resourceType"))
            //deleteRealResourceList.add(resource)
            matches = true
        }
        return matches
    }

    public void preInitResourcesOperStatus(DelegateExecution execution){
        logger.debug(" ======== STARTED preInitResourcesOperStatus Process ======== ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            logger.debug("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)
            List<ImmutablePair<Resource, List<Resource>>> deleteResourceList = execution.getVariable("deleteResourceList")

            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            for (ImmutablePair rc : deleteResourceList) {
                Resource resource = rc.getKey()
                resourceTemplateUUIDs = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
            }

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("URN_mso_adapters_openecomp_db_endpoint", dbAdapterEndpoint)

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initResourceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <resourceTemplateUUIDs>${MsoUtils.xmlEscape(resourceTemplateUUIDs)}</resourceTemplateUUIDs>
                        </ns:initResourceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_initResOperStatusRequest", payload)
            logger.debug("Outgoing initResourceOperationStatus: \n" + payload)
            logger.debug("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            logger.debug("Exception Occured Processing preInitResourcesOperStatus. Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        logger.debug("======== COMPLETED preInitResourcesOperStatus Process ======== ")
    }
    
    public void prepareUpdateServiceOperationStatus(DelegateExecution execution){
        logger.debug(" ======== STARTED prepareUpdateServiceOperationStatus Process ======== ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String userId = ""
            String result = execution.getVariable("result")
            String progress = execution.getVariable("progress")
            String reason = ""
            String operationContent = execution.getVariable("operationContent")
            
            serviceId = UriUtils.encode(serviceId,"UTF-8")

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

            String payload =
                    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>DELETE</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
            logger.debug("Outgoing updateServiceOperStatusRequest: \n" + payload)

        }catch(Exception e){
            logger.error("Exception Occured Processing prepareUpdateServiceOperationStatus. Exception is:\n" + e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareUpdateServiceOperationStatus Method:\n" + e.getMessage())
        }
        logger.debug("======== COMPLETED prepareUpdateServiceOperationStatus Process ======== ")
    }

     /**
      * post config request.
      */
     public void postConfigRequest(execution){
         //to do
     }

    /**
     * Deletes the service instance in aai
     */
    public void deleteServiceInstance(DelegateExecution execution) {
        logger.trace("Entered deleteServiceInstance")
        try {
            String globalCustId = execution.getVariable("globalSubscriberId")
            String serviceType = execution.getVariable("serviceType")
            String serviceInstanceId = execution.getVariable("serviceInstanceId")

            AAIResourcesClient resourceClient = new AAIResourcesClient()
            AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustId, serviceType, serviceInstanceId)
            resourceClient.delete(serviceInstanceUri)

            logger.trace("Exited deleteServiceInstance")
        }catch(Exception e){
            logger.debug("Error occured within deleteServiceInstance method: " + e)
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during deleteServiceInstance from aai")
        }
    }
	
	public void checkServiceType(DelegateExecution execution) {
		logger.debug(">>> Inside checkServiceType >>>")
		String serviceType = execution.getVariable("serviceType")
		if (serviceType == null)
		{
			execution.setVariable("serviceType", "")
			logger.info("ServiceType variable is null")
		}
		logger.info("checkServiceType serviceType Variable set "+execution.getVariable("serviceType"))
		logger.debug("<<< checkServiceType Ends <<<")
	}
	
	public void getDeleteAccessServiceDetails(DelegateExecution execution) {
		logger.debug(">>> Inside getDeleteAccessServiceDetails >>>")
		try {
			String accessServiceId = execution.getVariable("serviceInstanceId")
			
			String globalSubScriberId = execution.getVariable("globalSubscriberId")
			logger.debug("globalSubScriberId : " + globalSubScriberId)
			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint", execution)
			execution.setVariable("MDONS_dbAdapterEndpoint", dbAdapterEndpoint)
			
			String payload =
			"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	                        xmlns:ns="http://org.onap.so/requestsdb">
	                        <soapenv:Header/>
	                        <soapenv:Body>
	                            <ns:getControllerServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
								<accessServiceId>${MsoUtils.xmlEscape(accessServiceId)}</accessServiceId>
	                        </ns:getControllerServiceOperationStatus>
	                    </soapenv:Body>
	                </soapenv:Envelope>"""
			
			logger.debug("Before format Outgoing getAccessService Payload: \n" + payload)
			execution.setVariable("MDONS_getAccessServiceReq", payload)
			
			HttpPost httpPost = new HttpPost(dbAdapterEndpoint)
			httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk")
			httpPost.addHeader("Content-type", "application/soap+xml")
			httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML))
			String result = httpPostCall(dbAdapterEndpoint, httpPost)
			logger.debug("Result returned from request DB: \n" + result)
			if(validateDBResponse(result)) {
				List<OperationStatus> opStatusList = getOperationStatus(result)
				
				String isAllServiceSuccess = "true"
				String ds = execution.getVariable("Optical_Service_Status")
				if(ds.equals("FAILURE")){
					isAllServiceSuccess = "true"
					RollbackData rollbackData = new RollbackData()
					def disableRollback = execution.getVariable("disableRollback")
					rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
					rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
					rollbackData.put("SERVICEINSTANCE", "rollbackSDNC", "false")
					rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", accessServiceId)
					rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("serviceType"))
					rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
					execution.setVariable("rollbackData", rollbackData)
					logger.debug("Rollback data: " + rollbackData.toString())
					throw new Exception("Unable to reach SDNC")
				}else{
				for(OperationStatus opStatus : opStatusList) {
					if(opStatus.getOperation().equals("DELETE") && opStatus.getResult() == "SUCCESS") {
						logger.debug("operation Completed " + opStatus.getOperationId() + " is " + opStatus.getResult() + " serviceStatus " + isAllServiceSuccess)
						execution.setVariable("opStatusList", opStatusList)
						
					}else if(opStatus.getResult() == "FAILED"){
						isAllServiceSuccess = "true";
						logger.debug("Delete service result of " + opStatus.getOperationId() + " is error " + opStatus.getResult() + " serviceStatus " + isAllServiceSuccess)
						throw new Exception("Delete service result of " + opStatus.getOperationId() + " is error " + opStatus.getResult())
					}else if(opStatus.getResult() == "processing"){
						isAllServiceSuccess = "false";
						logger.debug("Progress of operation " + opStatus.getOperationId() + " is " + opStatus.getResult() + " serviceStatus " + isAllServiceSuccess)
					}
				}
				execution.setVariable("isAllServiceSuccess",isAllServiceSuccess)
				execution.setVariable("opStatusList", opStatusList)
			} 
			} else {
					logger.debug("Result returned is null or empty")
					throw new Exception("Result returned from req db is empty or null")
			}	
			
		} catch(Exception e){
			logger.debug("Error occured within getDeleteAccessServiceDetails method: " + e.printStackTrace())
			logger.debug(e.printStackTrace())
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during getAccessServiceDetails due to "+e.getMessage())
		}		
		logger.debug("<<< getDeleteAccessServiceDetails Ends <<<")
	}
	
	public void getAccessServiceDetails(DelegateExecution execution) {
		logger.debug(">>> Inside getAccessServiceDetails >>>")
		try {
			String accessServiceId = execution.getVariable("serviceInstanceId")
			String globalSubScriberId = execution.getVariable("globalSubscriberId")
			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint", execution)
			execution.setVariable("MDONS_dbAdapterEndpoint", dbAdapterEndpoint)
			String payload =
			"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	                        xmlns:ns="http://org.onap.so/requestsdb">
	                        <soapenv:Header/>
	                        <soapenv:Body>
	                            <ns:getControllerServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
								<accessServiceId>${MsoUtils.xmlEscape(accessServiceId)}</accessServiceId>
	                        </ns:getControllerServiceOperationStatus>
	                    </soapenv:Body>
	                </soapenv:Envelope>"""
			
			logger.debug("Before format Outgoing getAccessService Payload: \n" + payload)
			execution.setVariable("MDONS_getAccessServiceReq", payload)
			
			HttpPost httpPost = new HttpPost(dbAdapterEndpoint);
			httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk");
			httpPost.addHeader("Content-type", "application/soap+xml");
			httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
			String result = httpPostCall(dbAdapterEndpoint, httpPost);
			logger.debug("Result returned from request DB: \n" + result)
			if(validateDBResponse(result)) {
				List<OperationStatus> opStatusList = getOperationStatus(result);
				logger.debug("Operation Status returned from request DB: \n" + opStatusList.toString())
				String domainServiceName = getValueByName("serviceName", result)
				execution.setVariable("domainServiceName", domainServiceName)		
				
				String isAllServiceSuccess = "true";
				for(OperationStatus opStatus : opStatusList) {
						if(opStatus.getOperation().equals("CREATE") && opStatus.getResult() == "SUCCESS") {
							logger.debug("Operation Completed " + opStatus.getOperationId()+ " is " + opStatus.getResult() + " serviceStatus " + isAllServiceSuccess)
							execution.setVariable("opStatusList", opStatusList)	
						}else if(opStatus.getResult() == "FAILED"){
							isAllServiceSuccess = "true";
							logger.debug("Create service result of "+opStatus.getOperationId()+" is error "+opStatus.getResult()+" serviceStatus "+isAllServiceSuccess)
							throw new Exception("Create service result of "+opStatus.getOperationId()+" is error "+opStatus.getResult())
						}else if(opStatus.getResult() == "processing"){
							isAllServiceSuccess = "false";
							logger.debug("Progress of operation "+opStatus.getOperationId()+" is "+opStatus.getResult()+" serviceStatus "+isAllServiceSuccess)
						}
				}
				execution.setVariable("isAllServiceSuccess",isAllServiceSuccess)
				execution.setVariable("opStatusList", opStatusList)
				} else {
					logger.debug("Result returned is null or empty")
					throw new Exception("Result returned from req db is empty or null")
				}	
		} catch(Exception e){
			logger.debug("Error occured within getAccessServiceDetails method: " + e.printStackTrace())
			logger.debug(e.printStackTrace())
            exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during getAccessServiceDetails due to "+e.getMessage())
			RollbackData rollbackData = new RollbackData()
			def disableRollback = execution.getVariable("disableRollback")
			rollbackData.put("SERVICEINSTANCE", "disableRollback", disableRollback.toString())
			rollbackData.put("SERVICEINSTANCE", "rollbackAAI", "true")
			rollbackData.put("SERVICEINSTANCE", "serviceInstanceId", accessServiceId)
			rollbackData.put("SERVICEINSTANCE", "subscriptionServiceType", execution.getVariable("serviceType"))
			rollbackData.put("SERVICEINSTANCE", "globalSubscriberId", execution.getVariable("globalSubscriberId"))
			execution.setVariable("rollbackData", rollbackData)
		}
		
		logger.debug("<<< getAccessServiceDetails Ends <<<")
	}
	
	public void preProcessDomainList(DelegateExecution execution) {
		List<OperationStatus> opStatusList = execution.getVariable("opStatusList")
		int totalDomains = opStatusList.size()
		if(totalDomains == 1) {
			execution.setVariable("isMultiDomain","false")
		} else {
			execution.setVariable("isMultiDomain","true")
		}		
		execution.setVariable("currentDomainIndex", 0)
		execution.setVariable("totalDomains", totalDomains)
		
	}
	
	public void processCurrentDomain(DelegateExecution execution) {
		List<OperationStatus> opStatusList = execution.getVariable("opStatusList")
		OperationStatus opStatus = opStatusList.get(execution.getVariable("currentDomainIndex"))
		String serviceType = execution.getVariable("serviceType")
		String serviceId = opStatus.getServiceId()
		String operationId = opStatus.getOperationId()
		String result = "Processing"
		String progress = "0"
		String operationContent = "Prepare service Deletion"
		
		execution.setVariable("dcServiceId",serviceId)
		execution.setVariable("dcOperationId",operationId)
		execution.setVariable("dcResult",result)
		execution.setVariable("dcProgress",progress)
		execution.setVariable("dcOperationContent",operationContent)
		execution.setVariable("dcServiceType", serviceType)

	}
	
	public void processNextDomain(DelegateExecution execution) {
	String progress = "50"
	String serviceDeletionStatus = execution.getVariable("Optical_Service_DELETE_Status")
	try {
		if (serviceDeletionStatus == "SUCCESS") {
			if (execution.getVariable("isMultiDomain") == "false") {
				execution.setVariable("allDomainsFinished", "true")
				execution.setVariable("dcProgress", progress)
				execution.setVariable("dcResult", "processing")
				execution.setVariable("dcOperationType", "DELETE")
				execution.setVariable("dcOperationContent", "Delete Request Sent")
				prepareUpdateDomainControllerServiceOperationStatus(execution);
			} else {
				def currentIndex = execution.getVariable("currentDomainIndex")
				def nextIndex = currentIndex + 1
				execution.setVariable("currentDomainIndex", nextIndex)
				List < OperationStatus > opStatusList = execution.getVariable("opStatusList")
				opStatusList.get(currentIndex).setSyncStatus(serviceDeletionStatus)
				execution.setVariable("opStatusList", opStatusList)
				execution.setVariable("dcProgress", progress)
				execution.setVariable("dcResult", "processing")
				execution.setVariable("dcOperationType", "DELETE")
				execution.setVariable("dcOperationContent", "Delete Request Sent")
				prepareUpdateDomainControllerServiceOperationStatus(execution);
				if (nextIndex >= opStatusList.size()) {
					execution.setVariable("allDomainsFinished", "true")
					logger.info("allDomainsFinished" + execution.getVariable("allDomainsFinished"))
					
				} else {
					execution.setVariable("allDomainsFinished", "false")
					logger.info("Still pending domains" + execution.getVariable("allDomainsFinished"))
				}
			}
		} else {
			if (execution.getVariable("isMultiDomain") == "true") {
				logger.info("Do Roll Back and Send Work")
				List < OperationStatus > opStatusList = execution.getVariable("opStatusList")
				for (OperationStatus opStatus: opStatusList) {
					if (opStatus.getSyncStatus() == "SUCCESS") {
						execution.setVariable("dcProgress", "100")
						execution.setVariable("dcResult", "finished")
						execution.setVariable("dcOperationType", "CREATE")
						execution.setVariable("dcOperationContent", "Delete Request Failed")
						prepareUpdateDomainControllerServiceOperationStatus(execution);
					}
				}
			}
			throw new Exception("Error occured during Service creation at SDNC")
		}
	} catch(Exception e) {
		logger.debug("Error occured within processing domainservice method: " + e.printStackTrace())
		logger.debug(e.printStackTrace())
		exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during processing domain service due to " + e.getMessage());
	}

}
	
	public void prepareUpdateDomainControllerServiceOperationStatus(DelegateExecution execution){
		logger.debug(" ======== STARTED prepareUpdateServiceOperationStatus Process ======== ")
		try{
			String serviceId = execution.getVariable("dcServiceId")
			String operationId = execution.getVariable("dcOperationId")
			String userId = ""
			String result = execution.getVariable("dcResult")
			String progress = execution.getVariable("dcProgress")
			String reason = ""
			String operationContent = execution.getVariable("dcOperationContent")
			String operationType = execution.getVariable("dcOperationType")
			serviceId = UriUtils.encode(serviceId,"UTF-8")

			def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.db.endpoint", execution)
			execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
			logger.debug("DB Adapter Endpoint is: " + dbAdapterEndpoint)

			String payload =
					"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
                            <userId>${MsoUtils.xmlEscape(userId)}</userId>
                            <result>${MsoUtils.xmlEscape(result)}</result>
                            <operationContent>${MsoUtils.xmlEscape(operationContent)}</operationContent>
                            <progress>${MsoUtils.xmlEscape(progress)}</progress>
                            <reason>${MsoUtils.xmlEscape(reason)}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

		HttpPost httpPost = new HttpPost(dbAdapterEndpoint);
		httpPost.addHeader("Authorization", "Basic YnBlbDpwYXNzd29yZDEk");
		httpPost.addHeader("Content-type", "application/soap+xml");
		httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_XML));
		String dbresult = httpPostCall(dbAdapterEndpoint, httpPost);
		logger.debug("Result returned from request DB: \n" + dbresult)

		}catch(Exception e){
			logger.error("Exception Occured Processing prepareUpdateServiceOperationStatus. Exception is:\n" + e)
			execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during prepareUpdateServiceOperationStatus Method:\n" + e.getMessage())
		}
		logger.debug("======== COMPLETED prepareUpdateServiceOperationStatus Process ======== ")
	}
	
	protected String httpPostCall(String url, HttpPost httpPost) throws Exception {
		String result = null;

		String errorMsg;
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpPost);
			result = EntityUtils.toString(closeableHttpResponse.getEntity());
			logger.debug("result = {}", result);
			if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
				logger.info("exception: fail for status code = {}",
						closeableHttpResponse.getStatusLine().getStatusCode());
				throw new Exception(result);
			}

			closeableHttpResponse.close();
		} catch (Exception e) {
			errorMsg = url + ":httpPostWithJSON connect faild";
			logger.debug("exception: POST_CONNECT_FAILD : {}", errorMsg);
			throw e;
		}
		return result;
	}
	
	public List<OperationStatus> getOperationStatus(String result) {
		List<OperationStatus> statusList = new ArrayList<OperationStatus>();
		List<String> stringList = getValuesByName("return",result);
		for (String opStatus : stringList) {
			OperationStatus status = new OperationStatus(getValueByName("serviceId", opStatus), getValueByName("operationId", opStatus), getValueByName("operation", opStatus), getValueByName("userId", opStatus),getValueByName("result", opStatus), getValueByName("progress", opStatus), getValueByName("reason", opStatus), getValueByName("accessServiceId", opStatus))
		logger.debug("Operation Status extracted " + status)
				statusList.add(status);
		}
		return statusList;
	}
	
	private List<String> getValuesByName(String name, String xml) {
		List<String> stringList = new ArrayList<String>();
		if (!StringUtils.isBlank(xml) && xml.contains(name)) {
			String start = "<" + name + ">";
			String end = "</" + name + ">";
			int startIndex = xml.indexOf(start);
			int endIndex = xml.indexOf(end);
			while(startIndex >= 0) {
				stringList.add(xml.substring(startIndex, endIndex).replace(start, ""))
				startIndex = xml.indexOf(start, startIndex+1)
				endIndex = xml.indexOf(end, endIndex+1)
			}
		}
		return stringList;
		
	}
	
	private String getValueByName(String name, String xml) {
		if (!StringUtils.isBlank(xml) && xml.contains(name)) {
			String start = "<" + name + ">";
			String end = "</" + name + ">";
			return xml.substring(xml.indexOf(start), xml.indexOf(end)).replace(start, "");
		}
		return "";
	}
	
	private boolean validateDBResponse(String xml) {
		if(xml != null && xml != "" && xml.contains("return")) {
			return true
		}
		return false
	}


}