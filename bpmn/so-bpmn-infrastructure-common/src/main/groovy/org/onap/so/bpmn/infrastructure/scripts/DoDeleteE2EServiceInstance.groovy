/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

import static org.apache.commons.lang3.StringUtils.*;

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.so.bpmn.common.scripts.AaiUtil
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.rest.APIResponse
import org.onap.so.bpmn.core.json.JsonUtils
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import groovy.json.*

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

    public void preProcessRequest (DelegateExecution execution) {
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** preProcessRequest *****",  isDebugEnabled)
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
            }

            //Generated in parent for AAI PUT
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            if (isBlank(serviceInstanceId)){
                msg = "Input serviceInstanceId is null"
                utils.log("INFO", msg, isDebugEnabled)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }

            String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
            if (isBlank(sdncCallbackUrl)) {
                msg = "URN_mso_workflow_sdncadapter_callback is null"
                utils.log("INFO", msg, isDebugEnabled)
                exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
            }
            execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
            utils.log("INFO","SDNC Callback URL: " + sdncCallbackUrl, isDebugEnabled)

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
            utils.log("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        utils.log("INFO"," ***** Exit preProcessRequest *****",  isDebugEnabled)
    }

    public void postProcessAAIGET(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
        utils.log("INFO"," ***** postProcessAAIGET ***** ", isDebugEnabled)
        String msg = ""

        try {
            String serviceInstanceId = execution.getVariable("serviceInstanceId")
            boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
            String serviceType = ""

            if(foundInAAI){
                utils.log("INFO","Found Service-instance in AAI", isDebugEnabled)

                String siData = execution.getVariable("GENGS_service")
                utils.log("INFO", "SI Data", isDebugEnabled)
                if (isBlank(siData))
                {
                    msg = "Could not retrive ServiceInstance data from AAI to delete id:" + serviceInstanceId
                    utils.log("INFO", msg, isDebugEnabled)
                    exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
                }
                else
                {
                    InputSource source = new InputSource(new StringReader(siData));
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
                    Document serviceXml = docBuilder.parse(source)
                    serviceXml.getDocumentElement().normalize()
                    // get model invariant id
                    // Get Template uuid and version
                    if (utils.nodeExists(siData, "model-invariant-id") && utils.nodeExists(siData, "model-version-id") ) {
                        utils.log("INFO", "SI Data model-invariant-id and model-version-id exist:", isDebugEnabled)
                        def modelInvariantId  = serviceXml.getElementsByTagName("model-invariant-id").item(0).getTextContent()
                        def modelVersionId  = serviceXml.getElementsByTagName("model-version-id").item(0).getTextContent()

                        // Set Original Template info
                        execution.setVariable("model-invariant-id-original", modelInvariantId)
                        execution.setVariable("model-version-id-original", modelVersionId)
                    }

                    utils.log("INFO", "SI Data" + siData, isDebugEnabled)
                    //Confirm there are no related service instances (vnf/network or volume)
                    if (utils.nodeExists(siData, "relationship-list")) {
                        utils.log("INFO", "SI Data relationship-list exists:", isDebugEnabled)
                        JSONArray jArray = new JSONArray()

                        XmlParser xmlParser = new XmlParser()
                        Node root = xmlParser.parseText(siData)
                        def relation_list = utils.getChildNode(root, 'relationship-list')
                        def relationships = utils.getIdenticalChildren(relation_list, 'relationship')

                        for (def relation: relationships) {
                        	def jObj = getRelationShipData(relation, isDebugEnabled)
                        	jArray.put(jObj)
                        }

                        execution.setVariable("serviceRelationShip", jArray.toString())
						
//                        //test(siData)
//                        NodeList nodeList = serviceXml.getElementsByTagName("relationship")
//                        JSONArray jArray = new JSONArray()
//                        for (int x = 0; x < nodeList.getLength(); x++) {
//                            Node node = nodeList.item(x)
//                            if (node.getNodeType() == Node.ELEMENT_NODE) {
//                                Element eElement = (Element) node
//                                def e = eElement.getElementsByTagName("related-to").item(0).getTextContent()    								//for ns
//                                if(e.equals("service-instance")){
//                                    def relatedObject = eElement.getElementsByTagName("related-link").item(0).getTextContent()
//                                    utils.log("INFO", "ServiceInstance Related NS :" + relatedObject, isDebugEnabled)
//                                    NodeList dataList = node.getChildNodes()
//                                    if(null != dataList) {
//                                        JSONObject jObj = new JSONObject()
//                                        for (int i = 0; i < dataList.getLength(); i++) {
//                                            Node dNode = dataList.item(i)
//                                            if(dNode.getNodeName() == "relationship-data") {
//                                                Element rDataEle = (Element)dNode
//                                                def eKey =  rDataEle.getElementsByTagName("relationship-key").item(0).getTextContent()
//                                                def eValue = rDataEle.getElementsByTagName("relationship-value").item(0).getTextContent()
//                                                if(eKey.equals("service-instance.service-instance-id")){
//                                                    jObj.put("resourceInstanceId", eValue)
//                                                }
//
//                                            }
//                                            else if(dNode.getNodeName() == "related-to-property"){
//                                                Element rDataEle = (Element)dNode
//                                                def eKey =  rDataEle.getElementsByTagName("property-key").item(0).getTextContent()
//                                                def eValue = rDataEle.getElementsByTagName("property-value").item(0).getTextContent()
//                                                if(eKey.equals("service-instance.service-instance-name")){
//                                                    jObj.put("resourceType", eValue)
//                                                }
//                                            }
//                                        }
//                                        utils.log("INFO", "Relationship related to Resource:" + jObj.toString(), isDebugEnabled)
//                                        jArray.put(jObj)
//                                    }
//                                    //for overlay/underlay
//                                }else if (e.equals("configuration")){
//                                    def relatedObject = eElement.getElementsByTagName("related-link").item(0).getTextContent()
//                                    utils.log("INFO", "ServiceInstance Related Configuration :" + relatedObject, isDebugEnabled)
//                                    NodeList dataList = node.getChildNodes()
//                                    if(null != dataList) {
//                                        JSONObject jObj = new JSONObject()
//                                        for (int i = 0; i < dataList.getLength(); i++) {
//                                            Node dNode = dataList.item(i)
//                                            if(dNode.getNodeName() == "relationship-data") {
//                                                Element rDataEle = (Element)dNode
//                                                def eKey =  rDataEle.getElementsByTagName("relationship-key").item(0).getTextContent()
//                                                def eValue = rDataEle.getElementsByTagName("relationship-value").item(0).getTextContent()
//                                                if(eKey.equals("configuration.configuration-id")){
//                                                    jObj.put("resourceInstanceId", eValue)
//                                                }
//                                            }
//                                            else if(dNode.getNodeName() == "related-to-property"){
//                                                Element rDataEle = (Element)dNode
//                                                def eKey =  rDataEle.getElementsByTagName("property-key").item(0).getTextContent()
//                                                def eValue = rDataEle.getElementsByTagName("property-value").item(0).getTextContent()
//                                                if(eKey.equals("configuration.configuration-type")){
//                                                    jObj.put("resourceType", eValue)
//                                                }
//                                            }
//                                        }
//                                        utils.log("INFO", "Relationship related to Resource:" + jObj.toString(), isDebugEnabled)
//                                        jArray.put(jObj)
//                                    }
//                                // for SP-Partner
//                                }else if (e.equals("sp-partner")){
//									
//								}								
//                            }
//                        }
//                        execution.setVariable("serviceRelationShip", jArray.toString())
                    }
                }
            }else{
                boolean succInAAI = execution.getVariable("GENGS_SuccessIndicator")
                if(!succInAAI){
                    utils.log("INFO","Error getting Service-instance from AAI", + serviceInstanceId, isDebugEnabled)
                    WorkflowException workflowException = execution.getVariable("WorkflowException")
                    utils.logAudit("workflowException: " + workflowException)
                    if(workflowException != null){
                        exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
                    }
                    else
                    {
                        msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
                        utils.log("INFO", msg, isDebugEnabled)
                        exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
                    }
                }

                utils.log("INFO","Service-instance NOT found in AAI. Silent Success", isDebugEnabled)
            }
        }catch (BpmnError e) {
            throw e;
        } catch (Exception ex) {
            msg = "Exception in DoDeleteE2EServiceInstance.postProcessAAIGET. " + ex.getMessage()
            utils.log("INFO", msg, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
        }
        utils.log("INFO"," *** Exit postProcessAAIGET *** ", isDebugEnabled)
    }
	
	private JSONObject getRelationShipData(node, isDebugEnabled){
		JSONObject jObj = new JSONObject()
		
		def relation  = utils.nodeToString(node)
		def rt  = utils.getNodeText(relation, "related-to")
		
		def rl  = utils.getNodeText(relation, "related-link")
		utils.log("INFO", "ServiceInstance Related NS/Configuration :" + rl, isDebugEnabled)
		
		def rl_datas = utils.getIdenticalChildren(node, "relationship-data")
		for(def rl_data : rl_datas) {
			def eKey =  utils.getChildNodeText(rl_data, "relationship-key")
			def eValue = utils.getChildNodeText(rl_data, "relationship-value")

			if ((rt == "service-instance" && eKey.equals("service-instance.service-instance-id"))
			//for overlay/underlay
			|| (rt == "configuration" && eKey.equals("configuration.configuration-id")
			)){
				jObj.put("resourceInstanceId", eValue)
			}
			// for sp-partner and others
			else if(eKey.endsWith("-id")){
				jObj.put("resourceInstanceId", eValue)
				String resourceName = rt + eValue;
				jObj.put("resourceType", resourceName)
			}

			jObj.put("resourceLinkUrl", rl)
		}

		def rl_props = utils.getIdenticalChildren(node, "related-to-property")
		for(def rl_prop : rl_props) {
			def eKey =  utils.getChildNodeText(rl_prop, "property-key")
			def eValue = utils.getChildNodeText(rl_prop, "property-value")
			if((rt == "service-instance" && eKey.equals("service-instance.service-instance-name"))
			//for overlay/underlay
			|| (rt == "configuration" && eKey.equals("configuration.configuration-type"))){
				jObj.put("resourceType", eValue)
			}
		}

		utils.log("INFO", "Relationship related to Resource:" + jObj.toString(), isDebugEnabled)

		return jObj
	}

   public void getCurrentNS(DelegateExecution execution){
       def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
       utils.log("INFO", "======== Start getCurrentNS Process ======== ", isDebugEnabled)

       def currentIndex = execution.getVariable("currentNSIndex")
       List<String> nsSequence = execution.getVariable("nsSequence")
       String nsResourceType =  nsSequence.get(currentIndex)

       // GET AAI by Name, not ID, for process convenient
       execution.setVariable("GENGS_type", "service-instance")
       execution.setVariable("GENGS_serviceInstanceId", "")
       execution.setVariable("GENGS_serviceInstanceName", nsResourceType)

       utils.log("INFO", "======== COMPLETED getCurrentNS Process ======== ", isDebugEnabled)
   }

    public void prepareDecomposeService(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        try {
            utils.log("DEBUG", " ***** Inside prepareDecomposeService of create generic e2e service ***** ", isDebugEnabled)
            String modelInvariantUuid = execution.getVariable("model-invariant-id-original")
            String modelVersionId = execution.getVariable("model-version-id-original")

            String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelVersionId}",
            "modelVersion":""
             }"""
            execution.setVariable("serviceModelInfo", serviceModelInfo)

            utils.log("DEBUG", " ***** Completed prepareDecomposeService of  create generic e2e service ***** ", isDebugEnabled)
        } catch (Exception ex) {
            // try error in method block
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. Unexpected Error from method prepareDecomposeService() - " + ex.getMessage()
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
    }

	private void generateRelatedResourceInfo(String response, JSONObject jObj){
		
		def xml = new XmlSlurper().parseText(response)
		def rtn = xml.childNodes()
		while (rtn.hasNext()) {
			groovy.util.slurpersupport.Node node = rtn.next()
			def key = node.name()
			def value = node.text()
			jObj.put(key, value)
		}
	}
	
	private JSONObject getRelatedResourceInAAI (DelegateExecution execution, JSONObject jObj)
	{
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** Started getRelatedResourceInAAI *****",  isDebugEnabled)		
			
		AaiUtil aaiUriUtil = new AaiUtil()
		String aai_endpoint = execution.getVariable("URN_aai_endpoint")
		String urlLink = jObj.get("resourceLinkUrl")
		String serviceAaiPath = "${aai_endpoint}${urlLink}"
		APIResponse response = aaiUriUtil.executeAAIGetCall(execution, serviceAaiPath)
		int responseCode = response.getStatusCode()
		execution.setVariable(Prefix + "GeRelatedResourceResponseCode", responseCode)
		utils.log("DEBUG", "  Get RelatedResource code is: " + responseCode, isDebugEnabled)

		String aaiResponse = response.getResponseBodyAsString()
		aaiResponse = StringEscapeUtils.unescapeXml(aaiResponse)
		aaiResponse = aaiResponse.replaceAll("&", "&amp;")
		execution.setVariable(Prefix + "GetRelatedResourceResponse", aaiResponse)
		
		//Process Response
		if(responseCode == 200 || responseCode == 201 || responseCode == 202 )
			//200 OK 201 CREATED 202 ACCEPTED
		{
			utils.log("DEBUG", "GET RelatedResource Received a Good Response", isDebugEnabled)
			execution.setVariable(Prefix + "SuccessIndicator", true)
			execution.setVariable(Prefix + "FoundIndicator", true)
			
			generateRelatedResourceInfo(aaiResponse, jObj)
			
			//get model-invariant-uuid and model-uuid
			String modelInvariantId = ""
			String modelUuid = ""
			String modelCustomizationId = ""
			if(jObj.has("model-invariant-id")) {
				modelInvariantId = jObj.get("model-invariant-id")
				modelUuid = jObj.get("model-version-id")
				modelCustomizationId = jObj.get("model-customization-id")
			}
			
			jObj.put("modelInvariantId", modelInvariantId)			
			jObj.put("modelVersionId", modelUuid)			
			jObj.put("modelCustomizationId", modelCustomizationId)
		}
		else
		{
			utils.log("ERROR", "Get RelatedResource Received a Bad Response Code. Response Code is: " + responseCode, isDebugEnabled)			
		}
		
		utils.log("INFO", " ***** Exit getRelatedResourceInAAI *****", isDebugEnabled)
		return jObj;	
		
	}

    public void postDecomposeService(DelegateExecution execution) {
        def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

        utils.log("DEBUG", " ***** Inside processDecomposition() of  delete generic e2e service flow ***** ", isDebugEnabled)
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

            List<Resource> deleteRealResourceList = new ArrayList<Resource>()

            //Set the real resource instance id to the decomosed resource list
            //reset the resource instance id , because in the decompose flow ,its a random one.
            //match the resource-instance-name and the model name
            if (relationShipList != null) {
                relationShipList.each {

                    JSONObject obj = getRelatedResourceInAAI(execution, (JSONObject)it)
					
                    for (Resource resource : deleteResourceList) {

                        String modelName = resource.getModelInfo().getModelName()

                        String modelCustomizationUuid = resource.getModelInfo().getModelCustomizationUuid()
                        if (StringUtils.containsIgnoreCase(obj.get("resourceType"), modelName)) {
                            resource.setResourceId(obj.get("resourceInstanceId"))
                            deleteRealResourceList.add(resource)
                        }
                        else if (modelCustomizationUuid.equals(obj.get("modelCustomizationId"))) {
                            resource.setResourceId(obj.get("resourceInstanceId"))
                            resource.setResourceInstanceName(obj.get("resourceType"))
                            deleteRealResourceList.add(resource)
                        }
                    }
                }
            }          

            // only delete real existing resources
            execution.setVariable("deleteResourceList", deleteRealResourceList)

            utils.log("DEBUG", "delete resource list : " + deleteRealResourceList, isDebugEnabled)
        } catch (Exception ex) {
            String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
            utils.log("DEBUG", exceptionMessage, isDebugEnabled)
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
        utils.log("DEBUG", " ***** exit processDecomposition() of  delete generic e2e service flow ***** ", isDebugEnabled)
    }

    public void preInitResourcesOperStatus(DelegateExecution execution){
        def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

        utils.log("INFO", " ======== STARTED preInitResourcesOperStatus Process ======== ", isDebugEnabled)
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service creation"
            utils.log("INFO", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType, isDebugEnabled)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)
            List<Resource> deleteResourceList = execution.getVariable("deleteResourceList")

            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            for(Resource resource : deleteResourceList){
                    resourceTemplateUUIDs  = resourceTemplateUUIDs + resource.getModelInfo().getModelCustomizationUuid() + ":"
            }


            execution.setVariable("URN_mso_adapters_openecomp_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

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
            utils.log("INFO", "Outgoing initResourceOperationStatus: \n" + payload, isDebugEnabled)
            utils.logAudit("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing preInitResourcesOperStatus. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        utils.log("INFO", "======== COMPLETED preInitResourcesOperStatus Process ======== ", isDebugEnabled)
    }

     /**
      * post config request.
      */
     public void postConfigRequest(execution){
         //to do
     }

}
