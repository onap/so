/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved. 
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
package org.openecomp.mso.bpmn.infrastructure.scripts

import org.json.JSONArray
import org.openecomp.mso.bpmn.common.resource.ResourceRequestBuilder
import org.openecomp.mso.bpmn.core.domain.Resource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.infrastructure.properties.BPMNProperties;
import org.apache.http.HttpResponse
import org.json.JSONArray
import org.openecomp.mso.bpmn.common.recipe.BpmnRestClient
import org.openecomp.mso.bpmn.common.recipe.ResourceInput;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import org.openecomp.mso.bpmn.common.scripts.CatalogDbUtils;
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONObject;
import org.apache.commons.lang3.*
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.util.UriUtils;
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import com.fasterxml.jackson.jaxrs.json.annotation.JSONP.Def;

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
 * Outputs:
 * @param - WorkflowException
 * 
 * Rollback - Deferred
 */
public class DoCustomDeleteE2EServiceInstance extends AbstractServiceTaskProcessor {

	String Prefix="DDELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()
	CatalogDbUtils cutils = new CatalogDbUtils()

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
							<name>${paramName}</name>
							<value>${paramValue}</value>
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
						InputSource source = new InputSource(new StringReader(siData));
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder()
						Document serviceXml = docBuilder.parse(source)
						serviceXml.getDocumentElement().normalize()
						//test(siData)
						NodeList nodeList = serviceXml.getElementsByTagName("relationship")
	                    JSONArray jArray = new JSONArray()
						for (int x = 0; x < nodeList.getLength(); x++) {
							Node node = nodeList.item(x)
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) node
								def e = eElement.getElementsByTagName("related-to").item(0).getTextContent()    								//for ns
								if(e.equals("service-instance")){
								    def relatedObject = eElement.getElementsByTagName("related-link").item(0).getTextContent()
									utils.log("INFO", "ServiceInstance Related NS :" + relatedObject, isDebugEnabled)
                                    NodeList dataList = node.getChildNodes()
                                    if(null != dataList) {
                                        JSONObject jObj = new JSONObject()
                                        for (int i = 0; i < dataList.getLength(); i++) {
                                            Node dNode = dataList.item(i)
                                            if(dNode.getNodeName() == "relationship-data") {
                                                Element rDataEle = (Element)dNode
                                                def eKey =  rDataEle.getElementsByTagName("relationship-key").item(0).getTextContent()
                                                def eValue = rDataEle.getElementsByTagName("relationship-value").item(0).getTextContent()
                                                if(eKey.equals("service-instance.service-instance-id")){
                                                    jObj.put("resourceInstanceId", eValue)
                                                }
                                            }
                                            else if(dNode.getNodeName() == "related-to-property"){
                                                 Element rDataEle = (Element)dNode
                                                 def eKey =  rDataEle.getElementsByTagName("property-key").item(0).getTextContent()
                                                 def eValue = rDataEle.getElementsByTagName("property-value").item(0).getTextContent()
                                                 if(eKey.equals("service-instance.service-instance-name")){
                                                        jObj.put("resourceType", eValue)
                                                    }
                                            }
                                        }
                                        utils.log("INFO", "Relationship related to Resource:" + jObj.toString(), isDebugEnabled)
                                        jArray.put(jObj)
                                    }
						        //for overlay/underlay
								}else if (e.equals("configuration")){
                                    def relatedObject = eElement.getElementsByTagName("related-link").item(0).getTextContent()
                                    utils.log("INFO", "ServiceInstance Related Configuration :" + relatedObject, isDebugEnabled)
									NodeList dataList = node.getChildNodes()
									if(null != dataList) {
										JSONObject jObj = new JSONObject()
										for (int i = 0; i < dataList.getLength(); i++) {
											Node dNode = dataList.item(i)
											if(dNode.getNodeName() == "relationship-data") {
												Element rDataEle = (Element)dNode
												def eKey =  rDataEle.getElementsByTagName("relationship-key").item(0).getTextContent()
												def eValue = rDataEle.getElementsByTagName("relationship-value").item(0).getTextContent()
												if(eKey.equals("configuration.configuration-id")){
												    jObj.put("resourceInstanceId", eValue)
												}
											}
											else if(dNode.getNodeName() == "related-to-property"){
	                                             Element rDataEle = (Element)dNode
	                                             def eKey =  rDataEle.getElementsByTagName("property-key").item(0).getTextContent()
	                                             def eValue = rDataEle.getElementsByTagName("property-value").item(0).getTextContent()
	                                             if(eKey.equals("configuration.configuration-type")){
	                                                    jObj.put("resourceType", eValue)
	                                                }
											}
										}
										utils.log("INFO", "Relationship related to Resource:" + jObj.toString(), isDebugEnabled)
                                        jArray.put(jObj)
									}									
								}
							}
						}
                        execution.setVariable("serviceRelationShip", jArray.toString())
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

	public void prepareDecomposeService(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		try {
			utils.log("DEBUG", " ***** Inside prepareDecomposeService of create generic e2e service ***** ", isDebugEnabled)
			String modelInvariantUuid = execution.getVariable("model-invariant-id")
			//here modelVersion is not set, we use modelUuid to decompose the service.
			String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
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

	public void postDecomposeService(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")

		utils.log("DEBUG", " ***** Inside processDecomposition() of  create generic e2e service flow ***** ", isDebugEnabled)
		try {
			ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
			List<Resource> deleteResourceList = serviceDecomposition.getServiceResources()
			execution.setVariable("deleteResourceList", deleteResourceList)
		} catch (Exception ex) {
			String exceptionMessage = "Bpmn error encountered in  create generic e2e service flow. processDecomposition() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
	}

	public void postProcessAAIDEL(DelegateExecution execution) {
		def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
		utils.log("INFO"," ***** postProcessAAIDEL ***** ", isDebugEnabled)
		String msg = ""
		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean succInAAI = execution.getVariable("GENDS_SuccessIndicator")
			if(!succInAAI){
				msg = "Error deleting Service-instance in AAI" + serviceInstanceId
				utils.log("INFO", msg, isDebugEnabled)
				WorkflowException workflowException = execution.getVariable("WorkflowException")
				utils.logAudit("workflowException: " + workflowException)
				if(workflowException != null){
					exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
				}
				else
				{
					exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
				}
			}
		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.postProcessAAIDEL. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO"," *** Exit postProcessAAIDEL *** ", isDebugEnabled)
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
            // we use resource instance ids for delete flow as resourceTemplateUUIDs
            /*[
             {
                 "resourceInstanceId":"1111",
                 "resourceType":"vIMS"
             },
             {
                 "resourceInstanceId":"222",
                 "resourceType":"vEPC"
             },
             {
                 "resourceInstanceId":"3333",
                 "resourceType":"overlay"
             },
             {
                 "resourceInstanceId":"4444",
                 "resourceType":"underlay"
             }
         ]*/
            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            
            def jsonSlurper = new JsonSlurper()
            def jsonOutput = new JsonOutput()         
            List relationShipList =  jsonSlurper.parseText(serviceRelationShip)
                    
            if (relationShipList != null) {
                relationShipList.each {
                    resourceTemplateUUIDs  = resourceTemplateUUIDs + it.resourceInstanceId + ":"
                }
            }           
            execution.setVariable("URN_mso_adapters_openecomp_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")

            String payload =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:initResourceOperationStatus xmlns:ns="http://org.openecomp.mso/requestsdb">
                            <serviceId>${serviceId}</serviceId>
                            <operationId>${operationId}</operationId>
                            <operationType>${operationType}</operationType>
                            <resourceTemplateUUIDs>${resourceTemplateUUIDs}</resourceTemplateUUIDs>
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
    * prepare delete parameters
    */
   public void preResourceDelete(execution, resourceName){

	   def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
       // we use resource instance ids for delete flow as resourceTemplateUUIDs
       /*[
        {
            "resourceInstanceId":"1111",
            "resourceType":"vIMS"
        },
        {
            "resourceInstanceId":"222",
            "resourceType":"vEPC"
        },
        {
            "resourceInstanceId":"3333",
            "resourceType":"overlay"
        },
        {
            "resourceInstanceId":"4444",
            "resourceType":"underlay"
        }
    ]*/

       utils.log("INFO", " ======== STARTED preResourceDelete Process ======== ", isDebugEnabled)

	   List<Resource> deleteResourceList = execution.getVariable("deleteResourceList")
	   Resource resource = deleteResourceList.get(execution.getVariable("currentResourceIndex"))

//	   String serviceRelationShip = execution.getVariable("serviceRelationShip")
//       def jsonSlurper = new JsonSlurper()
//       def jsonOutput = new JsonOutput()
//       List relationShipList =  jsonSlurper.parseText(serviceRelationShip)


	   ResourceInput resourceInput = new ResourceInput()
	   String serviceInstanceName = execution.getVariable("serviceInstanceName")
	   String resourceInstanceName = resource.getResourceType() + "_" + serviceInstanceName
	   resourceInput.setResourceInstanceName(resourceInstanceName)
	   utils.log("INFO", "Prepare Resource Request resourceInstanceName:" + resourceInstanceName, isDebugEnabled)
	   String globalSubscriberId = execution.getVariable("globalSubscriberId")
	   String serviceType = execution.getVariable("serviceType")
	   String serviceInstanceId = execution.getVariable("serviceInstanceId")
	   String operationId = execution.getVariable("operationId")
	   String operationType = execution.getVariable("operationType")
	   resourceInput.setGlobalSubscriberId(globalSubscriberId)
	   resourceInput.setServiceType(serviceType)
	   resourceInput.setServiceInstanceId(serviceInstanceId)
	   resourceInput.setOperationId(operationId)
	   resourceInput.setOperationType(operationType);

	   resourceInput.setResourceModelInfo(resource.getModelInfo());
	   ServiceDecomposition serviceDecomposition = execution.getVariable("serviceDecomposition")
	   resourceInput.setServiceModelInfo(serviceDecomposition.getModelInfo());

	   //set the requestInputs from template  To Be Done
	   String serviceModelUuid = execution.getVariable("modelUuid")

	   String resourceParameters = ResourceRequestBuilder.buildResourceRequestParameters(execution, serviceModelUuid, resourceCustomizationUuid, serviceParameters)
	   resourceInput.setResourceParameters(resourceParameters)
	   execution.setVariable("resourceInput", resourceInput)

//       if (relationShipList != null) {
//           relationShipList.each {
//               if(StringUtils.containsIgnoreCase(it.resourceType, resourceName)) {
//				   String resourceInstanceUUID = it.resourceInstanceId
//				   String resourceTemplateUUID = it.resourceInstanceId
//				   execution.setVariable("resourceTemplateId", resourceTemplateUUID)
//				   execution.setVariable("resourceInstanceId", resourceInstanceUUID)
//				   execution.setVariable("resourceType", resourceName)
//			       utils.log("INFO", "Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: " + resourceInstanceUUID + " resourceType: " + resourceName, isDebugEnabled)
//			   }
//           }
//       }
       utils.log("INFO", " ======== END preResourceDelete Process ======== ", isDebugEnabled)
   }

	/**
	 * Execute delete workflow for resource
	 */
   public void executeResourceDelete(execution) {
	   def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
	   utils.log("INFO", "======== Start executeResourceDelete Process ======== ", isDebugEnabled)
	   String requestId = execution.getVariable("msoRequestId")
	   String serviceInstanceId = execution.getVariable("serviceInstanceId")
	   String serviceType = execution.getVariable("serviceType")
	   ResourceInput resourceInput = execution.getVariable("resourceInput")
	   String requestAction = resourceInput.getOperationType()
	   JSONObject resourceRecipe = cutils.getResourceRecipe(execution, resourceInput.getResourceUuid(), requestAction)
	   String recipeUri = resourceRecipe.getString("orchestrationUri")
	   String recipeTimeOut = resourceRecipe.getString("recipeTimeout")
	   String recipeParamXsd = resourceRecipe.get("paramXSD")
	   HttpResponse resp = BpmnRestClient.post(recipeUri, requestId, recipeTimeout, requestAction, serviceInstanceId, serviceType, resourceInput.toString(), recipeParamXsd)
	   utils.log("INFO", " ======== END executeResourceDelete Process ======== ", isDebugEnabled)
   }

   public void sequenceResource(execution){
       def isDebugEnabled = execution.getVariable("isDebugLogEnabled")

       utils.log("INFO", " ======== STARTED sequenceResource Process ======== ", isDebugEnabled)
       List<String> nsResources = new ArrayList<String>()
       List<String> wanResources = new ArrayList<String>()
       List<String> resourceSequence = new  ArrayList<String>()

	   List<Resource> deleteResourceList = execution.getVariable("deleteResourceList")
       List<Resource> deleteResourceSeq = new ArrayList();

	   String serviceRelationShip = execution.getVariable("serviceRelationShip")
               
       
       def jsonSlurper = new JsonSlurper()
       List relationShipList =  jsonSlurper.parseText(serviceRelationShip)

	   List<String> resouceTypeSeq = BPMNProperties.getResourceSequenceProp();

//	   if (relationShipList != null) {
//		   for (String resourceType in resouceTypeSeq) {
//			   relationShipList.each {
//				   if(StringUtils.containsIgnoreCase(it.resourceType, resouceType)){
//					   resourceSequence.add(it.resourceType)
//				   }
//			   }
//		   }
//	   }

       if (relationShipList != null) {
           relationShipList.each {
               if(StringUtils.containsIgnoreCase(it.resourceType, "overlay") || StringUtils.containsIgnoreCase(it.resourceType, "underlay")){
                   wanResources.add(it.resourceType)
               }else{
                   nsResources.add(it.resourceType)
               }
           }
       }

	   for (String resourceType in resouceTypeSeq) {
		   for (Resource resource in deleteResourceList) {
			   if (StringUtils.containsIgnoreCase(resource.getModelInfo().getModelName(), resouceType)) {

				   // fill the resource with reseoruce-instance-id retrieved from aai
				   relationShipList.each {
					   if (StringUtils.containsIgnoreCase(it.resourceType, resource.getResourceType())) {
						   resource.setResourceId(it.resourceInstanceId)
					   }
				   }

				   deleteResourceSeq.add(resource);

			   }
		   }
	   }

       String isContainsWanResource = wanResources.isEmpty() ? "false" : "true"
       execution.setVariable("isContainsWanResource", isContainsWanResource)
       execution.setVariable("currentResourceIndex", 0)
       execution.setVariable("resourceSequence", deleteResourceSeq)
       utils.log("INFO", "resourceSequence: " + deleteResourceSeq, isDebugEnabled)
       execution.setVariable("wanResources", wanResources)
       utils.log("INFO", " ======== END sequenceResource Process ======== ", isDebugEnabled)
   }
   
//   public void getCurrentResource(execution){
//       def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
//       utils.log("INFO", "======== Start getCurrentResoure Process ======== ", isDebugEnabled)
//       def currentIndex = execution.getVariable("currentResourceIndex")
//       List<Resource> resourceSequence = execution.getVariable("resourceSequence")
////       List<String> wanResources = execution.getVariable("wanResources")
////       String resourceName =  resourceSequence.get(currentIndex)
////       execution.setVariable("resourceType",resourceName)
////       if(wanResources.contains(resourceName)){
////           execution.setVariable("controllerInfo", "SDN-C")
////       }else{
////           execution.setVariable("controllerInfo", "VF-C")
////       }
//       utils.log("INFO", "======== COMPLETED getCurrentResoure Process ======== ", isDebugEnabled)
//   }
   
   public void parseNextResource(execution){
       def isDebugEnabled=execution.getVariable("isDebugLogEnabled")
       utils.log("INFO", "======== Start parseNextResource Process ======== ", isDebugEnabled)    
       def currentIndex = execution.getVariable("currentResourceIndex")
       def nextIndex =  currentIndex + 1
       execution.setVariable("currentResourceIndex", nextIndex)
       List<String> resourceSequence = execution.getVariable("resourceSequence")    
       if(nextIndex >= resourceSequence.size()){
           execution.setVariable("allResourceFinished", "true")
       }else{
           execution.setVariable("allResourceFinished", "false")
       }
       utils.log("INFO", "======== COMPLETED parseNextResource Process ======== ", isDebugEnabled)            
   }
   
}
 
