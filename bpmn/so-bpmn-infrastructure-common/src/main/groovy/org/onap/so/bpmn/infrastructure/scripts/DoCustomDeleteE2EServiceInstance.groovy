/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import org.onap.so.logger.LoggingAnchor
import org.onap.logging.filter.base.ErrorCode

import static org.apache.commons.lang3.StringUtils.*

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.AAIResourcesClient

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
 * Outputs:
 * @param - WorkflowException
 *
 * Rollback - Deferred
 */
public class DoCustomDeleteE2EServiceInstance extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoCustomDeleteE2EServiceInstance.class)


	String Prefix="DDELSI_"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {
		logger.trace("preProcessRequest ")
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
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}

			String sdncCallbackUrl = UrnPropertiesReader.getVariable('mso.workflow.sdncadapter.callback',execution)
			if (isBlank(sdncCallbackUrl)) {
				msg = "URN_mso_workflow_sdncadapter_callback is null"
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
			}
			execution.setVariable("sdncCallbackUrl", sdncCallbackUrl)
			logger.info("SDNC Callback URL: " + sdncCallbackUrl)

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
			throw e
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit preProcessRequest ")
	}


	public void preProcessVFCDelete (DelegateExecution execution) {
	}

	public void postProcessVFCDelete(DelegateExecution execution, String response, String method) {
	}

	public void preProcessSDNCDelete (DelegateExecution execution) {
		logger.trace("preProcessSDNCDelete ")
		String msg = ""

		try {
			def serviceInstanceId = execution.getVariable("serviceInstanceId")
			def serviceInstanceName = execution.getVariable("serviceInstanceName")
			def callbackURL = execution.getVariable("sdncCallbackUrl")
			def requestId = execution.getVariable("msoRequestId")
			def serviceId = execution.getVariable("productFamilyId")
			def subscriptionServiceType = execution.getVariable("subscriptionServiceType")
			def globalSubscriberId = execution.getVariable("globalSubscriberId") //globalCustomerId

			String serviceModelInfo = execution.getVariable("serviceModelInfo")
			def modelInvariantUuid = ""
			def modelVersion = ""
			def modelUuid = ""
			def modelName = ""
			if (!isBlank(serviceModelInfo))
			{
				modelInvariantUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelInvariantUuid")
				modelVersion = jsonUtil.getJsonValue(serviceModelInfo, "modelVersion")
				modelUuid = jsonUtil.getJsonValue(serviceModelInfo, "modelUuid")
				modelName = jsonUtil.getJsonValue(serviceModelInfo, "modelName")

				if (modelInvariantUuid == null) {
					modelInvariantUuid = ""
				}
				if (modelVersion == null) {
					modelVersion = ""
				}
				if (modelUuid == null) {
					modelUuid = ""
				}
				if (modelName == null) {
					modelName = ""
				}
			}
			if (serviceInstanceName == null) {
				serviceInstanceName = ""
			}
			if (serviceId == null) {
				serviceId = ""
			}

			def siParamsXml = execution.getVariable("siParamsXml")
			def serviceType = execution.getVariable("serviceType")
			if (serviceType == null)
			{
				serviceType = ""
			}

			def sdncRequestId = UUID.randomUUID().toString()

			String sdncDelete =
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.onap/so/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
													xmlns:sdncadapter="http://org.onap/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${MsoUtils.xmlEscape(sdncRequestId)}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${MsoUtils.xmlEscape(serviceInstanceId)}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>delete</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${MsoUtils.xmlEscape(callbackURL)}</sdncadapter:CallbackUrl>
							<sdncadapter:MsoAction>${MsoUtils.xmlEscape(serviceType)}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${MsoUtils.xmlEscape(requestId)}</request-id>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
						<request-action>DeleteServiceInstance</request-action>
					</request-information>
					<service-information>
						<service-id>${MsoUtils.xmlEscape(serviceId)}</service-id>
						<subscription-service-type>${MsoUtils.xmlEscape(subscriptionServiceType)}</subscription-service-type>
						<onap-model-information>
					         <model-invariant-uuid>${MsoUtils.xmlEscape(modelInvariantUuid)}</model-invariant-uuid>
					         <model-uuid>${MsoUtils.xmlEscape(modelUuid)}</model-uuid>
					         <model-version>${MsoUtils.xmlEscape(modelVersion)}</model-version>
					         <model-name>${MsoUtils.xmlEscape(modelName)}</model-name>
					    </onap-model-information>
						<service-instance-id>${MsoUtils.xmlEscape(serviceInstanceId)}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${MsoUtils.xmlEscape(globalSubscriberId)}</global-customer-id>
					</service-information>
					<service-request-input>
						<service-instance-name>${MsoUtils.xmlEscape(serviceInstanceName)}</service-instance-name>
						${siParamsXml}
					</service-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			sdncDelete = utils.formatXml(sdncDelete)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncDeactivate = sdncDelete.replace(">delete<", ">deactivate<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			execution.setVariable("sdncDelete", sdncDelete)
			execution.setVariable("sdncDeactivate", sdncDeactivate)
			logger.info("sdncDeactivate:\n" + sdncDeactivate)
			logger.info("sdncDelete:\n" + sdncDelete)

		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception Occured in preProcessSDNCDelete.\n" + ex.getMessage())
		}
		logger.info(" *****Exit preProcessSDNCDelete *****")
	}

	public void postProcessSDNCDelete(DelegateExecution execution, String response, String method) {

		logger.trace("postProcessSDNC " + method + " ")

		/*try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			logger.info("SDNCResponse: " + response)
			logger.info("workflowException: " + workflowException)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == "true"){
				logger.info("Good response from SDNC Adapter for service-instance " + method + "response:\n" + response)

			}else{
				msg = "Bad Response from SDNC Adapter for service-instance " + method
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 3500, msg)
			}
		} catch (BpmnError e) {
			throw e
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNC " + method + " Exception:" + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}*/
		logger.trace("Exit postProcessSDNC " + method + " ")
	}

	public void postProcessAAIGET(DelegateExecution execution) {
		logger.trace("postProcessAAIGET ")
		String msg = ""

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")

			if(foundInAAI){
				logger.info("Found Service-instance in AAI")

				String siData = execution.getVariable("GENGS_service")
				logger.info("SI Data")
				if (isBlank(siData))
				{
					msg = "Could not retrive ServiceInstance data from AAI to delete id:" + serviceInstanceId
					logger.info(msg)
					exceptionUtil.buildAndThrowWorkflowException(execution, 500, msg)
				}
				else
				{
					logger.info("SI Data" + siData)
					//Confirm there are no related service instances (vnf/network or volume)
					if (utils.nodeExists(siData, "relationship-list")) {
						logger.info("SI Data relationship-list exists:")
						InputSource source = new InputSource(new StringReader(siData))
						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
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
									logger.info("ServiceInstance Related NS :" + relatedObject)
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
                                        logger.info("Relationship related to Resource:" + jObj.toString())
                                        jArray.put(jObj)
                                    }
						        //for overlay/underlay
								}else if (e.equals("configuration")){
                                    def relatedObject = eElement.getElementsByTagName("related-link").item(0).getTextContent()
                                    logger.info("ServiceInstance Related Configuration :" + relatedObject)
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
										logger.info("Relationship related to Resource:" + jObj.toString())
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
					logger.info("Error getting Service-instance from AAI", + serviceInstanceId)
					WorkflowException workflowException = execution.getVariable("WorkflowException")
					logger.debug("workflowException: " + workflowException)
					if(workflowException != null){
						exceptionUtil.buildAndThrowWorkflowException(execution, workflowException.getErrorCode(), workflowException.getErrorMessage())
					}
					else
					{
						msg = "Failure in postProcessAAIGET GENGS_SuccessIndicator:" + succInAAI
						logger.info(msg)
						exceptionUtil.buildAndThrowWorkflowException(execution, 2500, msg)
					}
				}

				logger.info("Service-instance NOT found in AAI. Silent Success")
			}
		}catch (BpmnError e) {
			throw e
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.postProcessAAIGET. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.trace("Exit postProcessAAIGET ")
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
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalCustId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
			resourceClient.delete(serviceInstanceUri)

			logger.trace("Exited deleteServiceInstance")
		}catch(Exception e){
			logger.debug("Error occured within deleteServiceInstance method: " + e)
			exceptionUtil.buildAndThrowWorkflowException(execution, 2500, "Error occured during deleteServiceInstance from aai")
		}
	}

   public void preInitResourcesOperStatus(DelegateExecution execution){
        logger.trace("STARTED preInitResourcesOperStatus Process ")
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = execution.getVariable("operationType")
            String resourceTemplateUUIDs = ""
            logger.info("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
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
            logger.info("Outgoing initResourceOperationStatus: \n" + payload)
            logger.debug("CreateVfModuleInfra Outgoing initResourceOperationStatus Request: " + payload)

        }catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preInitResourcesOperStatus.", "BPMN",
					ErrorCode.UnknownError.getValue(), e)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preInitResourcesOperStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preInitResourcesOperStatus Process ")
    }

   /**
    * prepare delete parameters
    */
   public void preResourceDelete(execution, resourceName){
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
       logger.trace("STARTED preResourceDelete Process ")
       String serviceRelationShip = execution.getVariable("serviceRelationShip")
       def jsonSlurper = new JsonSlurper()
       def jsonOutput = new JsonOutput()
       List relationShipList =  jsonSlurper.parseText(serviceRelationShip)

       if (relationShipList != null) {
           relationShipList.each {
               if(StringUtils.containsIgnoreCase(it.resourceType, resourceName)) {
				   String resourceInstanceUUID = it.resourceInstanceId
				   String resourceTemplateUUID = it.resourceInstanceId
				   execution.setVariable("resourceTemplateId", resourceTemplateUUID)
				   execution.setVariable("resourceInstanceId", resourceInstanceUUID)
				   execution.setVariable("resourceType", resourceName)
			       logger.info("Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: " + resourceInstanceUUID + " resourceType: " + resourceName)
			   }
           }
       }
       logger.trace("END preResourceDelete Process ")
   }

   public void sequenceResource(execution){
       logger.trace("STARTED sequenceResource Process ")
       List<String> nsResources = new ArrayList<String>()
       List<String> wanResources = new ArrayList<String>()
       List<String> resourceSequence = new  ArrayList<String>()

       String serviceRelationShip = execution.getVariable("serviceRelationShip")


       def jsonSlurper = new JsonSlurper()
       def jsonOutput = new JsonOutput()
       List relationShipList =  jsonSlurper.parseText(serviceRelationShip)

       if (relationShipList != null) {
           relationShipList.each {
               if(StringUtils.containsIgnoreCase(it.resourceType, "overlay") || StringUtils.containsIgnoreCase(it.resourceType, "underlay")){
                   wanResources.add(it.resourceType)
               }else{
                   nsResources.add(it.resourceType)
               }
           }
       }
       resourceSequence.addAll(wanResources)
       resourceSequence.addAll(nsResources)
       String isContainsWanResource = wanResources.isEmpty() ? "false" : "true"
       execution.setVariable("isContainsWanResource", isContainsWanResource)
       execution.setVariable("currentResourceIndex", 0)
       execution.setVariable("resourceSequence", resourceSequence)
       logger.info("resourceSequence: " + resourceSequence)
       execution.setVariable("wanResources", wanResources)
       logger.trace("END sequenceResource Process ")
   }

   public void getCurrentResource(execution){
       logger.trace("Start getCurrentResoure Process ")
       def currentIndex = execution.getVariable("currentResourceIndex")
       List<String> resourceSequence = execution.getVariable("resourceSequence")
       List<String> wanResources = execution.getVariable("wanResources")
       String resourceName =  resourceSequence.get(currentIndex)
       execution.setVariable("resourceType",resourceName)
       if(wanResources.contains(resourceName)){
           execution.setVariable("controllerInfo", "SDN-C")
       }else{
           execution.setVariable("controllerInfo", "VF-C")
       }
       logger.trace("COMPLETED getCurrentResoure Process ")
   }

   public void parseNextResource(execution){
       logger.trace("Start parseNextResource Process ")
       def currentIndex = execution.getVariable("currentResourceIndex")
       def nextIndex =  currentIndex + 1
       execution.setVariable("currentResourceIndex", nextIndex)
       List<String> resourceSequence = execution.getVariable("resourceSequence")
       if(nextIndex >= resourceSequence.size()){
           execution.setVariable("allResourceFinished", "true")
       }else{
           execution.setVariable("allResourceFinished", "false")
       }
       logger.trace("COMPLETED parseNextResource Process ")
   }

}
