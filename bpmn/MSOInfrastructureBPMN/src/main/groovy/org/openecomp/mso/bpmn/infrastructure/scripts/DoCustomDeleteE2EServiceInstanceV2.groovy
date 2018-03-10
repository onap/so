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

import org.json.JSONArray;

import static org.apache.commons.lang3.StringUtils.*;
import groovy.xml.XmlUtil
import groovy.json.*

import org.openecomp.mso.bpmn.core.json.JsonUtils
import org.openecomp.mso.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig

import org.openecomp.mso.bpmn.common.scripts.AaiUtil

import java.util.UUID;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.runtime.Execution
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
public class DoCustomDeleteE2EServiceInstanceV2 extends AbstractServiceTaskProcessor {

	String Prefix="DDELSI_"
	private static final String DebugFlag = "isDebugEnabled"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (Execution execution) {
		
		def method = getClass().getSimpleName() + '.buildAPPCRequest(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
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
			execution.setVariable("operationStatus", "Waiting delete resource...")
			execution.setVariable("progress", "0")			

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	


	public void postProcessAAIGET(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessAAIGET(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		
		String msg = ""

		try {
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			utils.log("INFO","serviceInstanceId: "+serviceInstanceId, isDebugEnabled)
			
			boolean foundInAAI = execution.getVariable("GENGS_FoundIndicator")
			utils.log("INFO","foundInAAI: "+foundInAAI, isDebugEnabled)
			
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
			msg = "Bpmn error encountered in " + method + "--" + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	private void loadResourcesProperties(Execution execution) {
		def method = getClass().getSimpleName() + '.loadResourcesProperties(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String loadFilePath = "/etc/mso/config.d/reources.json"
		try{
			def jsonPayload = new File(loadFilePath).text
			utils.log("INFO","jsonPayload: " + jsonPayload, isDebugEnabled)

			String resourcesProperties = jsonUtil.prettyJson(jsonPayload.toString())
			utils.log("INFO","resourcesProperties: " + resourcesProperties, isDebugEnabled)
			
			String createResourceSort = jsonUtil.getJsonValue(resourcesProperties, "CreateResourceSort")
			//utils.log("INFO","createResourceSort: " + createResourceSort, isDebugEnabled)
			execution.setVariable("createResourceSort", createResourceSort)
			
			String deleteResourceSort = jsonUtil.getJsonValue(resourcesProperties, "DeleteResourceSort")
			//utils.log("INFO","deleteResourceSort: " + deleteResourceSort, isDebugEnabled)
			execution.setVariable("deleteResourceSort", deleteResourceSort)
			
			
			String resourceControllerType = jsonUtil.getJsonValue(resourcesProperties, "ResourceControllerType")
			//utils.log("INFO","resourceControllerType: " + resourceControllerType, isDebugEnabled)
			execution.setVariable("resourceControllerType", resourceControllerType)			
			
			
		}catch(Exception ex){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in " + method + " - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	private void sortDeleteResource(Execution execution) {
		def method = getClass().getSimpleName() + '.sortDeleteResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		String deleteResourceSortDef = """[
                {
                    "resourceType":"GRE_SAR"
                },
                {
                    "resourceType":"VPN_SAR"
                },
                {
                    "resourceType":"APN_AAR"
                },
				{
                    "resourceType":"GRE_AAR"
                },
                {
                    "resourceType":"Overlay"
                },
				{
                    "resourceType":"Underlay"
                },
                {
                    "resourceType":"vIMS"
                },
                {
                    "resourceType":"vCPE"
                },
                {
                    "resourceType":"vFW"
                },
                {
                    "resourceType":"vEPC"
                }
				
                
            ]""".trim()
		
        try{
			loadResourcesProperties(execution)
			String deleteResourceSort = execution.getVariable("deleteResourceSort")
			if (isBlank(deleteResourceSort)) {
				deleteResourceSort = deleteResourceSortDef;
			}
			
			List<String> sortResourceList = jsonUtil.StringArrayToList(execution, deleteResourceSort)
	        utils.log("INFO", "sortResourceList : " + sortResourceList, isDebugEnabled) 		 

			JSONArray newResourceList      = new JSONArray()
			int resSortCount = sortResourceList.size()
			
			for ( int currentResource = 0 ; currentResource < resSortCount ; currentResource++ ) { 
				String currentSortResource = sortResourceList[currentResource]
				String sortResourceType = jsonUtil.getJsonValue(currentSortResource, "resourceType")				
				List<String> resourceList = execution.getVariable(Prefix+"resourceList")

				for (String resource : resourceList) {
					//utils.log("INFO", "resource : " + resource, isDebugEnabled)
					String resourceType = jsonUtil.getJsonValue(resource, "resourceType")
	
					if (StringUtils.containsIgnoreCase(resourceType, sortResourceType)) {
						JSONObject jsonObj = new JSONObject(resource)
						newResourceList.put(jsonObj)
					}
					utils.log("INFO", "Get next sort type " , isDebugEnabled)
				}
			} 

            String newResourceStr = newResourceList.toString()		
            List<String> newResourceListStr = jsonUtil.StringArrayToList(execution, newResourceStr)			
		
			execution.setVariable(Prefix+"resourceList", newResourceListStr)
			utils.log("INFO", "newResourceList : " + newResourceListStr, isDebugEnabled) 
			
		}catch(Exception ex){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in " + method + " - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)
		
	}
	public void prepareServiceDeleteResource(Execution execution) {
		def method = getClass().getSimpleName() + '.prepareServiceDeleteResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		
		try {
			
			String serviceInstanceId = execution.getVariable("serviceInstanceId")
			
			// confirm if ServiceInstance was found
			if ( !execution.getVariable("GENGS_FoundIndicator") )
			{
				String exceptionMessage = "Bpmn error encountered in DeleteMobileAPNCustService flow. Service Instance was not found in AAI by id: " + serviceInstanceId
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
			}
			
			execution.setVariable(Prefix+"resourceList", "")
			execution.setVariable(Prefix+"resourceCount", 0)
			execution.setVariable(Prefix+"nextResource", 0)
			execution.setVariable(Prefix+"resourceFinish", true)
			
			// get SI extracted by GenericGetService
			String serviceInstanceAaiRecord = execution.getVariable("GENGS_service");
			utils.log("INFO", "serviceInstanceAaiRecord: " +serviceInstanceAaiRecord, isDebugEnabled)
			
			String aaiJsonRecord = jsonUtil.xml2json(serviceInstanceAaiRecord)
			
			//utils.log("INFO", "aaiJsonRecord: " +aaiJsonRecord, isDebugEnabled)
			def serviceInstanceName = jsonUtil.getJsonValue(aaiJsonRecord, "service-instance.service-instance-name")
			execution.setVariable("serviceInstanceName",serviceInstanceName)
			
			def serviceType = jsonUtil.getJsonValue(aaiJsonRecord, "service-instance.service-type")
			execution.setVariable("serviceType",serviceType)
			
			
			String relationshipList = jsonUtil.getJsonValue(aaiJsonRecord, "service-instance.relationship-list")  
			//utils.log("INFO", "relationship-list:" + relationshipList, isDebugEnabled)
			if (! isBlank(relationshipList)){
				utils.log("INFO", "relationship-list exists" , isDebugEnabled)
				String relationShip = jsonUtil.getJsonValue(relationshipList, "relationship")
				utils.log("INFO", "relationship: " + relationShip, isDebugEnabled)
				JSONArray allResources      = new JSONArray()
				JSONArray serviceResources  = new JSONArray()
				JSONArray networkResources  = new JSONArray()
				JSONArray allottedResources = new JSONArray()
				
				
				if (! isBlank(relationShip)){
					JSONArray jsonArray = new JSONArray();
					if (relationShip.startsWith("{") && relationShip.endsWith("}")) {
						JSONObject jsonObject = new JSONObject(relationShip);
						jsonArray.put(jsonObject);
					} else if (relationShip.startsWith("[") && relationShip.endsWith("]")) {
						jsonArray = new JSONArray(relationShip);
					} else {
						utils.log("INFO", "The relationShip fomart is error" , isDebugEnabled)			
					}
				
					List<String> relationList = jsonUtil.StringArrayToList(execution, jsonArray.toString())
					
					utils.log("INFO", "relationList: " + relationList, isDebugEnabled)
					
					int relationNum =relationList.size()
					utils.log("INFO", "**************relationList size: " + relationNum, isDebugEnabled)
					
					for ( int currentRelation = 0 ; currentRelation < relationNum ; currentRelation++ ) {  
						utils.log("INFO", "current Relation num: " + currentRelation, isDebugEnabled)
						String relation = relationList[currentRelation]
						utils.log("INFO", "relation: " + relation, isDebugEnabled)
						
						String relatedTo = jsonUtil.getJsonValue(relation, "related-to")  
            			utils.log("INFO", "relatedTo: " + relatedTo, isDebugEnabled)
						
						String relatedLink = jsonUtil.getJsonValue(relation, "related-link")  
						utils.log("INFO", "relatedLink: " + relatedLink, isDebugEnabled)
						
            			if (StringUtils.equalsIgnoreCase(relatedTo, "allotted-resource")) {
                			utils.log("INFO", "allotted-resource exists ", isDebugEnabled)

							String aaiArRsp = getAaiAr(execution, relatedLink)
							utils.log("INFO", "aaiArRsp: " + aaiArRsp, isDebugEnabled)
							if (! isBlank(aaiArRsp)) {
								def type = utils.getNodeText1(aaiArRsp, "type")
								def id = utils.getNodeText1(aaiArRsp, "id")
							    def role = utils.getNodeText1(aaiArRsp, "role")
								def resourceVersion = utils.getNodeText1(aaiArRsp, "resource-version")
								
								JSONObject jObject = new JSONObject()
								jObject.put("resourceType", type)
								jObject.put("resourceInstanceId", id)
								jObject.put("resourceRole", role)
								jObject.put("resourceVersion", resourceVersion)
								
								allResources.put(jObject)							
								utils.log("INFO", "allResources: " + allResources, isDebugEnabled)
								allottedResources.put(jObject)
								utils.log("INFO", "allottedResources: " + allottedResources, isDebugEnabled)
							}
						}
						else if (StringUtils.equalsIgnoreCase(relatedTo, "service-instance")){
                			utils.log("INFO", "service-instance exists ", isDebugEnabled)
							JSONObject jObject = new JSONObject()
							
							//relationship-data
							String rsDataStr  = jsonUtil.getJsonValue(relation, "relationship-data")
							utils.log("INFO", "rsDataStr: " + rsDataStr, isDebugEnabled)
							List<String> rsDataList = jsonUtil.StringArrayToList(execution, rsDataStr)
							utils.log("INFO", "rsDataList: " + rsDataList, isDebugEnabled)
							for(String rsData : rsDataList){ 
								utils.log("INFO", "rsData: " + rsData, isDebugEnabled)								
								def eKey =  jsonUtil.getJsonValue(rsData, "relationship-key")
 								def eValue = jsonUtil.getJsonValue(rsData, "relationship-value")
								if(eKey.equals("service-instance.service-instance-id")){
									jObject.put("resourceInstanceId", eValue)
								}
								if(eKey.equals("service-subscription.service-type")){
									jObject.put("resourceType", eValue)
								}
							}
							
							//related-to-property
							String rPropertyStr  = jsonUtil.getJsonValue(relation, "related-to-property")
							utils.log("INFO", "related-to-property: " + rPropertyStr, isDebugEnabled)
							if (rPropertyStr instanceof JSONArray){
								List<String> rPropertyList = jsonUtil.StringArrayToList(execution, rPropertyStr)
								for (String rProperty : rPropertyList) { 
									utils.log("INFO", "rProperty: " + rProperty, isDebugEnabled)								
									def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 									def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
									if(eKey.equals("service-instance.service-instance-name")){
										jObject.put("resourceName", eValue)
									}
								}
							}
							else {
								String rProperty = rPropertyStr
								utils.log("INFO", "rProperty: " + rProperty, isDebugEnabled)								
								def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 								def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
								if (eKey.equals("service-instance.service-instance-name")) {
									jObject.put("resourceName", eValue)
								}	
							}
							
							allResources.put(jObject)
							utils.log("INFO", "allResources: " + allResources, isDebugEnabled)
							
							serviceResources.put(jObject)
							utils.log("INFO", "serviceResources: " + serviceResources, isDebugEnabled)
						}
						else if (StringUtils.equalsIgnoreCase(relatedTo, "configuration")) {
                			utils.log("INFO", "configuration ", isDebugEnabled)
							JSONObject jObject = new JSONObject()
							
							//relationship-data
							String rsDataStr  = jsonUtil.getJsonValue(relation, "relationship-data")
							utils.log("INFO", "rsDataStr: " + rsDataStr, isDebugEnabled)
							List<String> rsDataList = jsonUtil.StringArrayToList(execution, rsDataStr)
							utils.log("INFO", "rsDataList: " + rsDataList, isDebugEnabled)
							for (String rsData : rsDataList) { 
								utils.log("INFO", "rsData: " + rsData, isDebugEnabled)								
								def eKey =  jsonUtil.getJsonValue(rsData, "relationship-key")
 								def eValue = jsonUtil.getJsonValue(rsData, "relationship-value")
								if(eKey.equals("configuration.configuration-id")){
									jObject.put("resourceInstanceId", eValue)
								}
							}

							
							//related-to-property
							String rPropertyStr  = jsonUtil.getJsonValue(relation, "related-to-property")
							utils.log("INFO", "related-to-property: " + rPropertyStr, isDebugEnabled)
							if (rPropertyStr instanceof JSONArray){
								List<String> rPropertyList = jsonUtil.StringArrayToList(execution, rPropertyStr)
								for(String rProperty : rPropertyList){ 
									utils.log("INFO", "rProperty: " + rProperty, isDebugEnabled)								
									def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 									def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
									if(eKey.equals("configuration.configuration-type")){
										jObject.put("resourceType", eValue)
									}
								}
							}
							else {
								String rProperty = rPropertyStr
								utils.log("INFO", "rProperty: " + rProperty, isDebugEnabled)								
								def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 								def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
								if(eKey.equals("configuration.configuration-type")){
									jObject.put("resourceType", eValue)
								}	
							}
							allResources.put(jObject)
							utils.log("INFO", "allResources: " + allResources, isDebugEnabled)
							
							networkResources.put(jObject)
							utils.log("INFO", "networkResources: " + networkResources, isDebugEnabled)
						}
						utils.log("INFO", "Get Next releation resource " , isDebugEnabled)
						
					}
					utils.log("INFO", "Get releation finished. " , isDebugEnabled)
				}
				
				execution.setVariable("serviceRelationShip", allResources.toString())
			    utils.log("INFO", "allResources: " + allResources.toString(), isDebugEnabled)
				String serviceRelationShip = execution.getVariable("serviceRelationShip")  
				utils.log("INFO", "serviceRelationShip: " + serviceRelationShip, isDebugEnabled)
				if ((! isBlank(serviceRelationShip)) && (! serviceRelationShip.isEmpty())) {
 
					List<String> relationShipList = jsonUtil.StringArrayToList(execution, serviceRelationShip) 
					utils.log("INFO", "relationShipList: " + relationShipList, isDebugEnabled) 
					execution.setVariable(Prefix+"resourceList", relationShipList)
			    	
					int resourceCount = relationShipList.size()
					utils.log("INFO", "resourceCount: " + resourceCount, isDebugEnabled)
					execution.setVariable(Prefix+"resourceCount",resourceCount )
			    	
					int resourceNum = 0
					execution.setVariable(Prefix+"nextResource", resourceNum)
					utils.log("INFO", "start sort delete resource: ", isDebugEnabled)
					sortDeleteResource(execution)
					
					
					if (resourceNum < resourceCount) {
						execution.setVariable(Prefix+"resourceFinish", false)
					}
					else {
			    		execution.setVariable(Prefix+"resourceFinish", true)
					}
					utils.log("INFO", "Resource  list set end : " + resourceCount, isDebugEnabled) 
                }
				
				execution.setVariable("serviceResources", serviceResources.toString()) 
				utils.log("INFO", "serviceResources: " + serviceResources, isDebugEnabled) 
				String serviceResourcesShip = execution.getVariable("serviceResources") 
				utils.log("INFO", "serviceResourcesShip: " + serviceResourcesShip, isDebugEnabled)
				
				if ((! isBlank(serviceResourcesShip)) && (! serviceResourcesShip.isEmpty())) {
                    List<String> serviceResourcesList = jsonUtil.StringArrayToList(execution, serviceResourcesShip)   
					utils.log("INFO", "serviceResourcesList: " + serviceResourcesList, isDebugEnabled) 
					execution.setVariable(Prefix+"serviceResourceList", serviceResourcesList)	
			    	execution.setVariable(Prefix+"serviceResourceCount", serviceResourcesList.size())
			    	execution.setVariable(Prefix+"nextServiceResource", 0)
			    	utils.log("INFO", "Service Resource  list set end : " + serviceResourcesList.size(), isDebugEnabled) 
                   
                }
				
				execution.setVariable("allottedResources", allottedResources.toString())
				utils.log("INFO", "allottedResources: " + allottedResources, isDebugEnabled)
				String allottedResourcesShip = execution.getVariable("allottedResources") 
				utils.log("INFO", "allottedResourcesShip: " + allottedResourcesShip, isDebugEnabled)
				if ((! isBlank(allottedResourcesShip)) && (! allottedResourcesShip.isEmpty())) {
                    List<String> allottedResourcesList = jsonUtil.StringArrayToList(execution, allottedResourcesShip)
					utils.log("INFO", "allottedResourcesList: " + allottedResourcesList, isDebugEnabled)
					execution.setVariable(Prefix+"allottedResourcesList", allottedResourcesList)	
			    	execution.setVariable(Prefix+"allottedResourcesListCount", allottedResourcesList.size())
			    	execution.setVariable(Prefix+"nextAllottedResourcesList", 0)
			    	utils.log("INFO", "Allotted Resource  list set end : " + allottedResourcesList.size(), isDebugEnabled) 
                   
                }
				execution.setVariable("networkResources", networkResources.toString())
				utils.log("INFO", "networkResources: " + networkResources, isDebugEnabled)
				String networkResourcesShip = execution.getVariable("networkResources") 
				utils.log("INFO", "networkResourcesShip: " + networkResourcesShip, isDebugEnabled)
				if ((! isBlank(networkResourcesShip)) && (! networkResourcesShip.isEmpty())) {
                    List<String> networkResourcesList = jsonUtil.StringArrayToList(execution, networkResourcesShip)       
					utils.log("INFO", "networkResourcesList: " + networkResourcesList, isDebugEnabled)
					execution.setVariable(Prefix+"networkResourcesList", networkResourcesList)	
			    	execution.setVariable(Prefix+"networkResourcesListCount", networkResourcesList.size())
			    	execution.setVariable(Prefix+"nextNetworkResourcesList", 0)
			    	utils.log("INFO", "Network Resource  list set end : " + networkResourcesList.size(), isDebugEnabled)
                   
                }
			}
		} catch (BpmnError e){
			throw e;
		} catch (Exception ex) {
		    String exceptionMessage = "Bpmn error encountered in DeleteMobileAPNCustService flow. prepareServiceDeleteResource() - " + ex.getMessage()
		    utils.log("DEBUG", exceptionMessage, isDebugEnabled)
		    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	private String getAaiAr(Execution execution, String relink) {
		def method = getClass().getSimpleName() + '.getAaiAr(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		AaiUtil aaiUtil = new AaiUtil(this)
		String aaiEndpoint = execution.getVariable("URN_aai_endpoint") + relink
		
		utils.log("DEBUG", "get AR info " + aaiEndpoint, isDebugEnabled)
		APIResponse response = aaiUtil.executeAAIGetCall(execution, aaiEndpoint)
		
		int responseCode = response.getStatusCode()
		utils.log("DEBUG", "get AR info responseCode:" + responseCode, isDebugEnabled)
		
		String aaiResponse = response.getResponseBodyAsString()
		utils.log("DEBUG", "get AR info " + aaiResponse, isDebugEnabled)
		
		if(responseCode < 200 || responseCode >= 300 || isBlank(aaiResponse)) {
			return null
		}
	
		utils.log("INFO", "Exited " + method, isDebugEnabled)
		return aaiResponse
	}
	/**
	 * prepare Decompose next resource to create request
	 */
	public void preProcessDecomposeNextResource(Execution execution){
        def method = getClass().getSimpleName() + '.getAaiAr(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== STARTED preProcessDecomposeNextResource Process ======== ", isDebugEnabled)
        try{
            int resourceNum = execution.getVariable(Prefix+"nextServiceResource")
			List<String> serviceResourceList = execution.getVariable(Prefix+"serviceResourceList")
			utils.log("INFO", "Service Resource List : " + serviceResourceList, isDebugEnabled)
			
			String serviceResource = serviceResourceList[resourceNum]
            execution.setVariable(Prefix+"serviceResource", serviceResource)
			utils.log("INFO", "Current Service Resource : " + serviceResource, isDebugEnabled)
			
			String resourceType  = jsonUtil.getJsonValue(serviceResource, "resourceType")
			execution.setVariable("resourceType", resourceType)
			utils.log("INFO", "resourceType : " + resourceType, isDebugEnabled)
            
			String resourceInstanceId  = jsonUtil.getJsonValue(serviceResource, "resourceInstanceId")
			execution.setVariable("resourceInstanceId", resourceInstanceId)
			utils.log("INFO", "resourceInstanceId : " + resourceInstanceId, isDebugEnabled)
			
			String resourceRole  = jsonUtil.getJsonValue(serviceResource, "resourceRole")
			execution.setVariable("resourceRole", resourceRole)
			utils.log("INFO", "resourceRole : " + resourceRole, isDebugEnabled)
			
			String resourceVersion  = jsonUtil.getJsonValue(serviceResource, "resourceVersion")
			execution.setVariable("resourceVersion", resourceVersion)
			utils.log("INFO", "resourceVersion : " + resourceVersion, isDebugEnabled)
			
			String resourceName = jsonUtil.getJsonValue(serviceResource, "resourceName")  
			if (isBlank(resourceName)){
				resourceName = resourceInstanceId
			}
			execution.setVariable(Prefix+"resourceName", resourceName)
			utils.log("INFO", "resource Name : " + resourceName, isDebugEnabled)
			

			execution.setVariable(Prefix+"nextServiceResource", resourceNum + 1)
			
			int serviceResourceCount = execution.getVariable(Prefix+"serviceResourceCount")
			if (serviceResourceCount >0 ){
			    int progress = (resourceNum*100) / serviceResourceCount
				execution.setVariable("progress", progress.toString() )
			}
			execution.setVariable("operationStatus", resourceName )

        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateMobileAPNCustService flow. Unexpected Error from method preProcessDecomposeNextResource() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}
	/**
	 * post Decompose next resource to create request
	 */
	public void postProcessDecomposeNextResource(Execution execution){
        def method = getClass().getSimpleName() + '.postProcessDecomposeNextResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        utils.log("INFO", " ======== STARTED postProcessDecomposeNextResource Process ======== ", isDebugEnabled)
        try{
            String resourceName = execution.getVariable(Prefix+"resourceName")
			int resourceNum = execution.getVariable(Prefix+"nextServiceResource")
			utils.log("DEBUG", "Current Resource count:"+ execution.getVariable(Prefix+"nextServiceResource"), isDebugEnabled)
			
			int resourceCount = execution.getVariable(Prefix+"serviceResourceCount")
			utils.log("DEBUG", "Total Resource count:"+ execution.getVariable(Prefix+"serviceResourceCount"), isDebugEnabled)

            if (resourceNum < resourceCount) {
				execution.setVariable(Prefix+"resourceFinish", false)
			}
			else {
			    execution.setVariable(Prefix+"resourceFinish", true)
			}
			
			utils.log("DEBUG", "Resource Finished:"+ execution.getVariable(Prefix+"resourceFinish"), isDebugEnabled)
			
			if (resourceCount >0 ){
			    int progress = (resourceNum*100) / resourceCount

				execution.setVariable("progress", progress.toString() )
				utils.log("DEBUG", "progress :"+ execution.getVariable("progress"), isDebugEnabled)
			}
			execution.setVariable("operationStatus", resourceName )


        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateMobileAPNCustService flow. Unexpected Error from method postProcessDecomposeNextResource() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)   
	}
	/**
	* prepare post Unkown Resource Type 
	*/
	public void postOtherControllerType(Execution execution){
        def method = getClass().getSimpleName() + '.postOtherControllerType(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		
        try{

            String resourceName = execution.getVariable(Prefix+"resourceName") 
			String resourceType = execution.getVariable(Prefix+"resourceType") 
			String controllerType = execution.getVariable("controllerType")
			
		    String msg = "Resource name: "+ resourceName + " resource Type: " + resourceType+ " controller Type: " + controllerType + " can not be processed  n the workflow"
			utils.log("DEBUG", msg, isDebugEnabled)
			
        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in DoCreateMobileAPNServiceInstance flow. Unexpected Error from method postOtherControllerType() - " + ex.getMessage()
			utils.log("DEBUG", exceptionMessage, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    utils.log("INFO", "Exited " + method, isDebugEnabled)   
	}

	/**
    * prepare delete parameters
    */
    public void preSDNCResourceDelete(execution, resourceName){
        // we use resource instance ids for delete flow as resourceTemplateUUIDs

        def method = getClass().getSimpleName() + '.preSDNCResourceDelete(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)

        utils.log("INFO", " ======== STARTED preSDNCResourceDelete Process ======== ", isDebugEnabled)
        String networkResources = execution.getVariable("networkResources") 
        		
       
        execution.setVariable("foundResource", false)       
        if (networkResources != null) {
            def jsonSlurper = new JsonSlurper()        
            List relationShipList =  jsonSlurper.parseText(networkResources)
			relationShipList.each {
                if(StringUtils.containsIgnoreCase(it.resourceType, resourceName)) {
			 	    String resourceInstanceUUID = it.resourceInstanceId
				    String resourceTemplateUUID = it.resourceInstanceId
				    execution.setVariable("resourceTemplateId", resourceTemplateUUID)
				    execution.setVariable("resourceInstanceId", resourceInstanceUUID)
				    execution.setVariable("resourceType", resourceName)
					execution.setVariable("foundResource", true)
			        utils.log("INFO", "Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: " + resourceInstanceUUID + " resourceType: " + resourceName, isDebugEnabled)
				}
            }
        }    
        utils.log("INFO", "Exited " + method, isDebugEnabled)
    }
	public void preProcessSDNCDelete (Execution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCDelete(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** preProcessSDNCDelete *****", isDebugEnabled)
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
					"""<sdncadapterworkflow:SDNCAdapterWorkflowRequest xmlns:ns5="http://org.openecomp/mso/request/types/v1"
													xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
													xmlns:sdncadapter="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
				   <sdncadapter:RequestHeader>
							<sdncadapter:RequestId>${sdncRequestId}</sdncadapter:RequestId>
							<sdncadapter:SvcInstanceId>${serviceInstanceId}</sdncadapter:SvcInstanceId>
							<sdncadapter:SvcAction>delete</sdncadapter:SvcAction>
							<sdncadapter:SvcOperation>service-topology-operation</sdncadapter:SvcOperation>
							<sdncadapter:CallbackUrl>${callbackURL}</sdncadapter:CallbackUrl>
							<sdncadapter:MsoAction>${serviceType}</sdncadapter:MsoAction>
					</sdncadapter:RequestHeader>
				<sdncadapterworkflow:SDNCRequestData>
					<request-information>
						<request-id>${requestId}</request-id>
						<source>MSO</source>
						<notification-url/>
						<order-number/>
						<order-version/>
						<request-action>DeleteServiceInstance</request-action>
					</request-information>
					<service-information>
						<service-id>${serviceId}</service-id>
						<subscription-service-type>${subscriptionServiceType}</subscription-service-type>
						<onap-model-information>
					         <model-invariant-uuid>${modelInvariantUuid}</model-invariant-uuid>
					         <model-uuid>${modelUuid}</model-uuid>
					         <model-version>${modelVersion}</model-version>
					         <model-name>${modelName}</model-name>
					    </onap-model-information>
						<service-instance-id>${serviceInstanceId}</service-instance-id>
						<subscriber-name/>
						<global-customer-id>${globalSubscriberId}</global-customer-id>
					</service-information>
					<service-request-input>
						<service-instance-name>${serviceInstanceName}</service-instance-name>
						${siParamsXml}
					</service-request-input>
				</sdncadapterworkflow:SDNCRequestData>
				</sdncadapterworkflow:SDNCAdapterWorkflowRequest>"""

			sdncDelete = utils.formatXml(sdncDelete)
			def sdncRequestId2 = UUID.randomUUID().toString()
			String sdncDeactivate = sdncDelete.replace(">delete<", ">deactivate<").replace(">${sdncRequestId}<", ">${sdncRequestId2}<")
			execution.setVariable("sdncDelete", sdncDelete)
			execution.setVariable("sdncDeactivate", sdncDeactivate)
			utils.log("INFO","sdncDeactivate:\n" + sdncDeactivate, isDebugEnabled)
			utils.log("INFO","sdncDelete:\n" + sdncDelete, isDebugEnabled)

		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception Occured in preProcessSDNCDelete.\n" + ex.getMessage())
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}

	public void postProcessSDNCDelete(Execution execution, String response, String action) {

		def method = getClass().getSimpleName() + '.postProcessSDNCDelete(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
		utils.log("INFO"," ***** postProcessSDNC " + action + " *****", isDebugEnabled)
		String msg = ""

		/*try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			utils.log("INFO", "SDNCResponse: " + response, isDebugEnabled)
			utils.log("INFO", "workflowException: " + workflowException, isDebugEnabled)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == "true"){
				utils.log("INFO","Good response from SDNC Adapter for service-instance " + action + "response:\n" + response, isDebugEnabled)

			}else{
				msg = "Bad Response from SDNC Adapter for service-instance " + action
				utils.log("INFO", msg, isDebugEnabled)
				exceptionUtil.buildAndThrowWorkflowException(execution, 3500, msg)
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNC " + action + " Exception:" + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}*/
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	public void postProcessAAIDEL(Execution execution) {
		def method = getClass().getSimpleName() + '.postProcessAAIDEL(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
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
		utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	/**
	 * Init the service Operation Status
	 */
	public void preUpdateServiceOperationStatus(Execution execution){
        def method = getClass().getSimpleName() + '.preUpdateServiceOperationStatus(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)
        
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String serviceName = execution.getVariable("serviceInstanceName")
            String operationType = "DELETE"
            String userId = ""
            String result = "processing"
            String progress = execution.getVariable("progress")
			utils.log("INFO", "progress: " + progress , isDebugEnabled)
			if ("100".equalsIgnoreCase(progress))
			{
				result = "finished"
			}
            String reason = ""
            String operationContent = "Prepare service delete: " + execution.getVariable("operationStatus")
            utils.log("INFO", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId, isDebugEnabled)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = execution.getVariable("URN_mso_adapters_openecomp_db_endpoint")
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            utils.log("INFO", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

            execution.setVariable("URN_mso_openecomp_adapters_db_endpoint","http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter")
			
			String payload =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.openecomp.mso/requestsdb">
                            <serviceId>${serviceId}</serviceId>
                            <operationId>${operationId}</operationId>
                            <serviceName>${serviceName}</serviceName>
                            <operationType>${operationType}</operationType>
                            <userId>${userId}</userId>
                            <result>${result}</result>
                            <operationContent>${operationContent}</operationContent>
                            <progress>${progress}</progress>
                            <reason>${reason}</reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

            payload = utils.formatXml(payload)
            execution.setVariable("CVFMI_updateServiceOperStatusRequest", payload)
            utils.log("INFO", "Outgoing preUpdateServiceOperationStatus: \n" + payload, isDebugEnabled)
           

        }catch(Exception e){
            utils.log("ERROR", "Exception Occured Processing preUpdateServiceOperationStatus. Exception is:\n" + e, isDebugEnabled)
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preUpdateServiceOperationStatus Method:\n" + e.getMessage())
        }
        utils.log("INFO", "======== COMPLETED preUpdateServiceOperationStatus Process ======== ", isDebugEnabled)  
        utils.log("INFO", "Exited " + method, isDebugEnabled)
	}
	
	public void preInitResourcesOperStatus(Execution execution){
        def method = getClass().getSimpleName() + '.preInitResourcesOperStatus(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)

        utils.log("INFO", " ======== STARTED preInitResourcesOperStatus Process ======== ", isDebugEnabled)
		String msg=""
        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String operationType = "DELETE"
            String resourceTemplateUUIDs = ""
            String result = "processing"
            String progress = "0"
            String reason = ""
            String operationContent = "Prepare service delete"
            utils.log("INFO", "Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType, isDebugEnabled)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            utils.log("INFO", "serviceRelationShip: " + serviceRelationShip, isDebugEnabled)
			if (! isBlank(serviceRelationShip)) {
                def jsonSlurper = new JsonSlurper()
                def jsonOutput = new JsonOutput()         
                List relationShipList =  jsonSlurper.parseText(serviceRelationShip)
                    
                if (relationShipList != null) {
                    relationShipList.each {
                        resourceTemplateUUIDs  = resourceTemplateUUIDs + it.resourceInstanceId + ":"
                    }
                }
            }			
            def dbAdapterEndpoint = "http://mso.mso.testlab.openecomp.org:8080/dbadapters/RequestsDbAdapter"
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            utils.log("INFO", "DB Adapter Endpoint is: " + dbAdapterEndpoint, isDebugEnabled)

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
            utils.logAudit("DoCustomDeleteE2EServiceInstanceV2 Outgoing initResourceOperationStatus Request: " + payload)

		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCustomDeleteE2EServiceInstanceV2.preInitResourcesOperStatus. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
        utils.log("INFO", "Exited " + method, isDebugEnabled)  
    }
    

   
	/**
    * prepare delete parameters
    */
	public void preProcessVFCResourceDelete(execution){
		// we use resource instance ids for delete flow as resourceTemplateUUIDs

		def method = getClass().getSimpleName() + '.preProcessVFCResourceDelete(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)

		utils.log("INFO", " ======== STARTED preProcessVFCResourceDelete Process ======== ", isDebugEnabled)
		try{
			String serviceResource = execution.getVariable("serviceResource")  
			utils.log("INFO", "serviceResource : " + serviceResource, isDebugEnabled)
						
			String resourceInstanceId  =  execution.getVariable("resourceInstanceId")
			utils.log("INFO", "resourceInstanceId : " + resourceInstanceId, isDebugEnabled)
			
			execution.setVariable("resourceTemplateId", resourceInstanceId)
			utils.log("INFO", "resourceTemplateId : " + resourceInstanceId, isDebugEnabled)
			
			String resourceType = execution.getVariable("resourceType")
			utils.log("INFO", "resourceType : " + resourceType, isDebugEnabled)

			
			String resourceName = execution.getVariable(Prefix+"resourceName") 
			if (isBlank(resourceName)){
				resourceName = resourceInstanceId
			}
			execution.setVariable("resourceName", resourceName)
			utils.log("INFO", "resource Name : " + resourceName, isDebugEnabled)
		
			utils.log("INFO", "Delete Resource Info: resourceInstanceId :" + resourceInstanceId + "  resourceTemplateId: " + resourceInstanceId + " resourceType: " + resourceType, isDebugEnabled)
		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.preProcessVFCResourceDelete. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}

	public void postProcessVFCDelete(Execution execution, String response, String action) {
		def method = getClass().getSimpleName() + '.postProcessVFCDelete(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugLogEnabled")
		utils.log("INFO","Entered " + method, isDebugEnabled)

		utils.log("INFO", " ======== STARTED postProcessVFCDelete Process ======== ", isDebugEnabled)
		try{
		
		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.postProcessVFCDelete. " + ex.getMessage()
			utils.log("INFO", msg, isDebugEnabled)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		utils.log("INFO", "Exited " + method, isDebugEnabled)  
	}
}
 