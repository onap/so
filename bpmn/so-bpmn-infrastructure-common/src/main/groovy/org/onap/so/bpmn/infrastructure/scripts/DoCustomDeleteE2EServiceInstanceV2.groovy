/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import static org.apache.commons.lang3.StringUtils.*;
import javax.ws.rs.NotFoundException
import javax.ws.rs.core.UriBuilder
import org.apache.commons.lang3.*
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.json.JSONArray
import org.json.JSONObject
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.logging.filter.base.ErrorCode
import org.onap.so.bpmn.common.scripts.AbstractServiceTaskProcessor
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.logger.LoggingAnchor
import org.onap.so.logger.MessageEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriUtils;
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
public class DoCustomDeleteE2EServiceInstanceV2 extends AbstractServiceTaskProcessor {
    private static final Logger logger = LoggerFactory.getLogger( DoCustomDeleteE2EServiceInstanceV2.class);


	String Prefix="DDELSI_"
	private static final String DebugFlag = "isDebugEnabled"
	ExceptionUtil exceptionUtil = new ExceptionUtil()
	JsonUtils jsonUtil = new JsonUtils()

	public void preProcessRequest (DelegateExecution execution) {

		def method = getClass().getSimpleName() + '.buildAPPCRequest(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)
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

			String sdncCallbackUrl = execution.getVariable('URN_mso_workflow_sdncadapter_callback')
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
			execution.setVariable("operationStatus", "Waiting delete resource...")
			execution.setVariable("progress", "0")

		} catch (BpmnError e) {
			throw e;
		} catch (Exception ex){
			msg = "Exception in preProcessRequest " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info("Exited " + method)
	}

	/**
	 * Gets the service instance and its relationships from aai
	 *
	 * @author cb645j
	 */
	public void getServiceInstance(DelegateExecution execution) {
		try {
			String serviceInstanceId = execution.getVariable('serviceInstanceId')
			String globalSubscriberId = execution.getVariable('globalSubscriberId')
			String serviceType = execution.getVariable('serviceType')

			AAIResourcesClient resourceClient = new AAIResourcesClient()
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(serviceType).serviceInstance(serviceInstanceId))
			AAIResultWrapper wrapper = resourceClient.get(serviceInstanceUri, NotFoundException.class)
			String json = wrapper.getJson()

			execution.setVariable("serviceInstance", json)

		}catch(BpmnError e) {
			throw e;
		}catch(NotFoundException e) {
			logger.info("SI not found in aai. Silent Success ")
		}catch(Exception ex) {
			String msg = "Internal Error in getServiceInstance: " + ex.getMessage()
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
	}

	private void loadResourcesProperties(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.loadResourcesProperties(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		logger.info("Entered " + method)
		String loadFilePath = "/etc/mso/config.d/reources.json"
		try{
			def jsonPayload = new File(loadFilePath).text
			logger.info("jsonPayload: " + jsonPayload)

			String resourcesProperties = jsonUtil.prettyJson(jsonPayload.toString())
			logger.info("resourcesProperties: " + resourcesProperties)

			String createResourceSort = jsonUtil.getJsonValue(resourcesProperties, "CreateResourceSort")
			logger.info("createResourceSort: " + createResourceSort)
			execution.setVariable("createResourceSort", createResourceSort)

			String deleteResourceSort = jsonUtil.getJsonValue(resourcesProperties, "DeleteResourceSort")
			logger.info("deleteResourceSort: " + deleteResourceSort)
			execution.setVariable("deleteResourceSort", deleteResourceSort)


			String resourceControllerType = jsonUtil.getJsonValue(resourcesProperties, "ResourceControllerType")
			logger.info("resourceControllerType: " + resourceControllerType)
			execution.setVariable("resourceControllerType", resourceControllerType)


		}catch(Exception ex){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in " + method + " - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    logger.info("Exited " + method)
	}
	private void sortDeleteResource(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.sortDeleteResource(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		logger.info("Entered " + method)
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
	        logger.info("sortResourceList : " + sortResourceList)

			JSONArray newResourceList      = new JSONArray()
			int resSortCount = sortResourceList.size()

			for ( int currentResource = 0 ; currentResource < resSortCount ; currentResource++ ) {
				String currentSortResource = sortResourceList[currentResource]
				String sortResourceType = jsonUtil.getJsonValue(currentSortResource, "resourceType")
				List<String> resourceList = execution.getVariable(Prefix+"resourceList")

				for (String resource : resourceList) {
					logger.info("resource : " + resource)
					String resourceType = jsonUtil.getJsonValue(resource, "resourceType")

					if (StringUtils.containsIgnoreCase(resourceType, sortResourceType)) {
						JSONObject jsonObj = new JSONObject(resource)
						newResourceList.put(jsonObj)
					}
					logger.info("Get next sort type " )
				}
			}

            String newResourceStr = newResourceList.toString()
            List<String> newResourceListStr = jsonUtil.StringArrayToList(execution, newResourceStr)

			execution.setVariable(Prefix+"resourceList", newResourceListStr)
			logger.info("newResourceList : " + newResourceListStr)

		}catch(Exception ex){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in " + method + " - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    logger.info("Exited " + method)

	}
	public void prepareServiceDeleteResource(DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.prepareServiceDeleteResource(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)

		try {

			String serviceInstanceId = execution.getVariable("serviceInstanceId")


			execution.setVariable(Prefix+"resourceList", "")
			execution.setVariable(Prefix+"resourceCount", 0)
			execution.setVariable(Prefix+"nextResource", 0)
			execution.setVariable(Prefix+"resourceFinish", true)

			String aaiJsonRecord = execution.getVariable("serviceInstance");
			logger.info("serviceInstanceAaiRecord: " +aaiJsonRecord)

			logger.info("aaiJsonRecord: " +aaiJsonRecord)
			def serviceInstanceName = jsonUtil.getJsonValue(aaiJsonRecord, "service-instance.service-instance-name")
			execution.setVariable("serviceInstanceName",serviceInstanceName)

			def serviceType = jsonUtil.getJsonValue(aaiJsonRecord, "service-instance.service-type")
			execution.setVariable("serviceType",serviceType)


			String relationshipList = jsonUtil.getJsonValue(aaiJsonRecord, "service-instance.relationship-list")
			logger.info("relationship-list:" + relationshipList)
			if (! isBlank(relationshipList)){
				logger.info("relationship-list exists" )
				String relationShip = jsonUtil.getJsonValue(relationshipList, "relationship")
				logger.info("relationship: " + relationShip)
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
						logger.info("The relationShip fomart is error" )
					}

					List<String> relationList = jsonUtil.StringArrayToList(execution, jsonArray.toString())

					logger.info("relationList: " + relationList)

					int relationNum =relationList.size()
					logger.info("**************relationList size: " + relationNum)

					for ( int currentRelation = 0 ; currentRelation < relationNum ; currentRelation++ ) {
						logger.info("current Relation num: " + currentRelation)
						String relation = relationList[currentRelation]
						logger.info("relation: " + relation)

						String relatedTo = jsonUtil.getJsonValue(relation, "related-to")
            			logger.info("relatedTo: " + relatedTo)

						String relatedLink = jsonUtil.getJsonValue(relation, "related-link")
						logger.info("relatedLink: " + relatedLink)

            			if (StringUtils.equalsIgnoreCase(relatedTo, "allotted-resource")) {
                			logger.info("allotted-resource exists ")

                            Optional<AllottedResource>  aaiArRsp = getAaiAr(execution, relatedLink)
							logger.info("aaiArRsp: " + aaiArRsp)
							if (aaiArRsp.isPresent()) {

								JSONObject jObject = new JSONObject()
								jObject.put("resourceType", aaiArRsp.get().getType())
								jObject.put("resourceInstanceId", aaiArRsp.get().getId())
								jObject.put("resourceRole", aaiArRsp.get().getRole())
								jObject.put("resourceVersion", aaiArRsp.get().getResourceVersion())

								allResources.put(jObject)
								logger.info("allResources: " + allResources)
								allottedResources.put(jObject)
								logger.info("allottedResources: " + allottedResources)
							}
						}
						else if (StringUtils.equalsIgnoreCase(relatedTo, "service-instance")){
                			logger.info("service-instance exists ")
							JSONObject jObject = new JSONObject()

							//relationship-data
							String rsDataStr  = jsonUtil.getJsonValue(relation, "relationship-data")
							logger.info("rsDataStr: " + rsDataStr)
							List<String> rsDataList = jsonUtil.StringArrayToList(execution, rsDataStr)
							logger.info("rsDataList: " + rsDataList)
							for(String rsData : rsDataList){
								logger.info("rsData: " + rsData)
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
							logger.info("related-to-property: " + rPropertyStr)
							if (rPropertyStr instanceof JSONArray){
								List<String> rPropertyList = jsonUtil.StringArrayToList(execution, rPropertyStr)
								for (String rProperty : rPropertyList) {
									logger.info("rProperty: " + rProperty)
									def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 									def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
									if(eKey.equals("service-instance.service-instance-name")){
										jObject.put("resourceName", eValue)
									}
								}
							}
							else {
								String rProperty = rPropertyStr
								logger.info("rProperty: " + rProperty)
								def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 								def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
								if (eKey.equals("service-instance.service-instance-name")) {
									jObject.put("resourceName", eValue)
								}
							}

							allResources.put(jObject)
							logger.info("allResources: " + allResources)

							serviceResources.put(jObject)
							logger.info("serviceResources: " + serviceResources)
						}
						else if (StringUtils.equalsIgnoreCase(relatedTo, "configuration")) {
                			logger.info("configuration ")
							JSONObject jObject = new JSONObject()

							//relationship-data
							String rsDataStr  = jsonUtil.getJsonValue(relation, "relationship-data")
							logger.info("rsDataStr: " + rsDataStr)
							List<String> rsDataList = jsonUtil.StringArrayToList(execution, rsDataStr)
							logger.info("rsDataList: " + rsDataList)
							for (String rsData : rsDataList) {
								logger.info("rsData: " + rsData)
								def eKey =  jsonUtil.getJsonValue(rsData, "relationship-key")
 								def eValue = jsonUtil.getJsonValue(rsData, "relationship-value")
								if(eKey.equals("configuration.configuration-id")){
									jObject.put("resourceInstanceId", eValue)
								}
							}


							//related-to-property
							String rPropertyStr  = jsonUtil.getJsonValue(relation, "related-to-property")
							logger.info("related-to-property: " + rPropertyStr)
							if (rPropertyStr instanceof JSONArray){
								List<String> rPropertyList = jsonUtil.StringArrayToList(execution, rPropertyStr)
								for(String rProperty : rPropertyList){
									logger.info("rProperty: " + rProperty)
									def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 									def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
									if(eKey.equals("configuration.configuration-type")){
										jObject.put("resourceType", eValue)
									}
								}
							}
							else {
								String rProperty = rPropertyStr
								logger.info("rProperty: " + rProperty)
								def eKey =  jsonUtil.getJsonValue(rProperty, "property-key")
 								def eValue = jsonUtil.getJsonValue(rProperty, "property-value")
								if(eKey.equals("configuration.configuration-type")){
									jObject.put("resourceType", eValue)
								}
							}
							allResources.put(jObject)
							logger.info("allResources: " + allResources)

							networkResources.put(jObject)
							logger.info("networkResources: " + networkResources)
						}
						logger.info("Get Next releation resource " )

					}
					logger.info("Get releation finished. " )
				}

				execution.setVariable("serviceRelationShip", allResources.toString())
			    logger.info("allResources: " + allResources.toString())
				String serviceRelationShip = execution.getVariable("serviceRelationShip")
				logger.info("serviceRelationShip: " + serviceRelationShip)
				if ((! isBlank(serviceRelationShip)) && (! serviceRelationShip.isEmpty())) {

					List<String> relationShipList = jsonUtil.StringArrayToList(execution, serviceRelationShip)
					logger.info("relationShipList: " + relationShipList)
					execution.setVariable(Prefix+"resourceList", relationShipList)

					int resourceCount = relationShipList.size()
					logger.info("resourceCount: " + resourceCount)
					execution.setVariable(Prefix+"resourceCount",resourceCount )

					int resourceNum = 0
					execution.setVariable(Prefix+"nextResource", resourceNum)
					logger.info("start sort delete resource: ")
					sortDeleteResource(execution)


					if (resourceNum < resourceCount) {
						execution.setVariable(Prefix+"resourceFinish", false)
					}
					else {
			    		execution.setVariable(Prefix+"resourceFinish", true)
					}
					logger.info("Resource  list set end : " + resourceCount)
                }

				execution.setVariable("serviceResources", serviceResources.toString())
				logger.info("serviceResources: " + serviceResources)
				String serviceResourcesShip = execution.getVariable("serviceResources")
				logger.info("serviceResourcesShip: " + serviceResourcesShip)

				if ((! isBlank(serviceResourcesShip)) && (! serviceResourcesShip.isEmpty())) {
                    List<String> serviceResourcesList = jsonUtil.StringArrayToList(execution, serviceResourcesShip)
					logger.info("serviceResourcesList: " + serviceResourcesList)
					execution.setVariable(Prefix+"serviceResourceList", serviceResourcesList)
			    	execution.setVariable(Prefix+"serviceResourceCount", serviceResourcesList.size())
			    	execution.setVariable(Prefix+"nextServiceResource", 0)
			    	logger.info("Service Resource  list set end : " + serviceResourcesList.size())

                }

				execution.setVariable("allottedResources", allottedResources.toString())
				logger.info("allottedResources: " + allottedResources)
				String allottedResourcesShip = execution.getVariable("allottedResources")
				logger.info("allottedResourcesShip: " + allottedResourcesShip)
				if ((! isBlank(allottedResourcesShip)) && (! allottedResourcesShip.isEmpty())) {
                    List<String> allottedResourcesList = jsonUtil.StringArrayToList(execution, allottedResourcesShip)
					logger.info("allottedResourcesList: " + allottedResourcesList)
					execution.setVariable(Prefix+"allottedResourcesList", allottedResourcesList)
			    	execution.setVariable(Prefix+"allottedResourcesListCount", allottedResourcesList.size())
			    	execution.setVariable(Prefix+"nextAllottedResourcesList", 0)
			    	logger.info("Allotted Resource  list set end : " + allottedResourcesList.size())

                }
				execution.setVariable("networkResources", networkResources.toString())
				logger.info("networkResources: " + networkResources)
				String networkResourcesShip = execution.getVariable("networkResources")
				logger.info("networkResourcesShip: " + networkResourcesShip)
				if ((! isBlank(networkResourcesShip)) && (! networkResourcesShip.isEmpty())) {
                    List<String> networkResourcesList = jsonUtil.StringArrayToList(execution, networkResourcesShip)
					logger.info("networkResourcesList: " + networkResourcesList)
					execution.setVariable(Prefix+"networkResourcesList", networkResourcesList)
			    	execution.setVariable(Prefix+"networkResourcesListCount", networkResourcesList.size())
			    	execution.setVariable(Prefix+"nextNetworkResourcesList", 0)
			    	logger.info("Network Resource  list set end : " + networkResourcesList.size())

                }
			}
		} catch (BpmnError e){
			throw e;
		} catch (Exception ex) {
		    String exceptionMessage = "Bpmn error encountered in DeleteMobileAPNCustService flow. prepareServiceDeleteResource() - " + ex.getMessage()
		    logger.debug(exceptionMessage)
		    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
		}
		logger.info("Exited " + method)
	}

	private Optional<AllottedResource>  getAaiAr(DelegateExecution execution, String relink) {
		def method = getClass().getSimpleName() + '.getAaiAr(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)
		AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.ALLOTTED_RESOURCE, UriBuilder.fromPath(relink).build())
        return getAAIClient().get(AllottedResource.class,uri)
	}
	/**
	 * prepare Decompose next resource to create request
	 */
	public void preProcessDecomposeNextResource(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.getAaiAr(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)
        logger.trace("STARTED preProcessDecomposeNextResource Process ")
        try{
            int resourceNum = execution.getVariable(Prefix+"nextServiceResource")
			List<String> serviceResourceList = execution.getVariable(Prefix+"serviceResourceList")
			logger.info("Service Resource List : " + serviceResourceList)

			String serviceResource = serviceResourceList[resourceNum]
            execution.setVariable(Prefix+"serviceResource", serviceResource)
			logger.info("Current Service Resource : " + serviceResource)

			String resourceType  = jsonUtil.getJsonValue(serviceResource, "resourceType")
			execution.setVariable("resourceType", resourceType)
			logger.info("resourceType : " + resourceType)

			String resourceInstanceId  = jsonUtil.getJsonValue(serviceResource, "resourceInstanceId")
			execution.setVariable("resourceInstanceId", resourceInstanceId)
			logger.info("resourceInstanceId : " + resourceInstanceId)

			String resourceRole  = jsonUtil.getJsonValue(serviceResource, "resourceRole")
			execution.setVariable("resourceRole", resourceRole)
			logger.info("resourceRole : " + resourceRole)

			String resourceVersion  = jsonUtil.getJsonValue(serviceResource, "resourceVersion")
			execution.setVariable("resourceVersion", resourceVersion)
			logger.info("resourceVersion : " + resourceVersion)

			String resourceName = jsonUtil.getJsonValue(serviceResource, "resourceName")
			if (isBlank(resourceName)){
				resourceName = resourceInstanceId
			}
			execution.setVariable(Prefix+"resourceName", resourceName)
			logger.info("resource Name : " + resourceName)


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
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    logger.info("Exited " + method)
	}
	/**
	 * post Decompose next resource to create request
	 */
	public void postProcessDecomposeNextResource(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.postProcessDecomposeNextResource(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)
        logger.trace("STARTED postProcessDecomposeNextResource Process ")
        try{
            String resourceName = execution.getVariable(Prefix+"resourceName")
			int resourceNum = execution.getVariable(Prefix+"nextServiceResource")
			logger.debug("Current Resource count:"+ execution.getVariable(Prefix+"nextServiceResource"))

			int resourceCount = execution.getVariable(Prefix+"serviceResourceCount")
			logger.debug("Total Resource count:"+ execution.getVariable(Prefix+"serviceResourceCount"))

            if (resourceNum < resourceCount) {
				execution.setVariable(Prefix+"resourceFinish", false)
			}
			else {
			    execution.setVariable(Prefix+"resourceFinish", true)
			}

			logger.debug("Resource Finished:"+ execution.getVariable(Prefix+"resourceFinish"))

			if (resourceCount >0 ){
			    int progress = (resourceNum*100) / resourceCount

				execution.setVariable("progress", progress.toString() )
				logger.trace(":"+ execution.getVariable(""))
			}
			execution.setVariable("operationStatus", resourceName )


        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in CreateMobileAPNCustService flow. Unexpected Error from method postProcessDecomposeNextResource() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    logger.info("Exited " + method)
	}
	/**
	* prepare post Unkown Resource Type
	*/
	public void postOtherControllerType(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.postOtherControllerType(' +'execution=' + execution.getId() +')'
		def isDebugEnabled = execution.getVariable("isDebugEnabled")
		logger.info("Entered " + method)

        try{

            String resourceName = execution.getVariable(Prefix+"resourceName")
			String resourceType = execution.getVariable(Prefix+"resourceType")
			String controllerType = execution.getVariable("controllerType")

		    String msg = "Resource name: "+ resourceName + " resource Type: " + resourceType+ " controller Type: " + controllerType + " can not be processed  n the workflow"
			logger.debug(msg)

        }catch(Exception e){
            // try error in method block
			String exceptionMessage = "Bpmn error encountered in DoCreateMobileAPNServiceInstance flow. Unexpected Error from method postOtherControllerType() - " + ex.getMessage()
			logger.debug(exceptionMessage)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, exceptionMessage)
        }
	    logger.info("Exited " + method)
	}

	/**
    * prepare delete parameters
    */
    public void preSDNCResourceDelete(execution, resourceName){
        // we use resource instance ids for delete flow as resourceTemplateUUIDs

        def method = getClass().getSimpleName() + '.preSDNCResourceDelete(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)

        logger.trace("STARTED preSDNCResourceDelete Process ")
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
			        logger.info("Delete Resource Info resourceTemplate Id :" + resourceTemplateUUID + "  resourceInstanceId: " + resourceInstanceUUID + " resourceType: " + resourceName)
				}
            }
        }
        logger.info("Exited " + method)
    }
	public void preProcessSDNCDelete (DelegateExecution execution) {
		def method = getClass().getSimpleName() + '.preProcessSDNCDelete(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)
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
			throw e;
		} catch(Exception ex) {
			msg = "Exception in preProcessSDNCDelete. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Exception Occured in preProcessSDNCDelete.\n" + ex.getMessage())
		}
		logger.info("Exited " + method)
	}

	public void postProcessSDNCDelete(DelegateExecution execution, String response, String action) {

		def method = getClass().getSimpleName() + '.postProcessSDNCDelete(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)
		logger.trace("postProcessSDNC " + action + " ")
		String msg = ""

		/*try {
			WorkflowException workflowException = execution.getVariable("WorkflowException")
			boolean successIndicator = execution.getVariable("SDNCA_SuccessIndicator")
			logger.info("SDNCResponse: " + response)
			logger.info("workflowException: " + workflowException)

			SDNCAdapterUtils sdncAdapterUtils = new SDNCAdapterUtils(this)
			sdncAdapterUtils.validateSDNCResponse(execution, response, workflowException, successIndicator)
			if(execution.getVariable(Prefix + 'sdncResponseSuccess') == "true"){
				logger.info("Good response from SDNC Adapter for service-instance " + action + "response:\n" + response)

			}else{
				msg = "Bad Response from SDNC Adapter for service-instance " + action
				logger.info(msg)
				exceptionUtil.buildAndThrowWorkflowException(execution, 3500, msg)
			}
		} catch (BpmnError e) {
			throw e;
		} catch(Exception ex) {
			msg = "Exception in postProcessSDNC " + action + " Exception:" + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}*/
		logger.info("Exited " + method)
	}

	/**
	 * Init the service Operation Status
	 */
	public void preUpdateServiceOperationStatus(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.preUpdateServiceOperationStatus(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)

        try{
            String serviceId = execution.getVariable("serviceInstanceId")
            String operationId = execution.getVariable("operationId")
            String serviceName = execution.getVariable("serviceInstanceName")
            String operationType = "DELETE"
            String userId = ""
            String result = "processing"
            String progress = execution.getVariable("progress")
			logger.info("progress: " + progress )
			if ("100".equalsIgnoreCase(progress))
			{
				result = "finished"
			}
            String reason = ""
            String operationContent = "Prepare service delete: " + execution.getVariable("operationStatus")
            logger.info("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            logger.info("DB Adapter Endpoint is: " + dbAdapterEndpoint)

			String payload =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                            <serviceId>${MsoUtils.xmlEscape(serviceId)}</serviceId>
                            <operationId>${MsoUtils.xmlEscape(operationId)}</operationId>
                            <serviceName>${MsoUtils.xmlEscape(serviceName)}</serviceName>
                            <operationType>${MsoUtils.xmlEscape(operationType)}</operationType>
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
            logger.info("Outgoing preUpdateServiceOperationStatus: \n" + payload)


        }catch(Exception e){
			logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
					"Exception Occured Processing preUpdateServiceOperationStatus.", "BPMN",
					ErrorCode.UnknownError.getValue(), e);
            execution.setVariable("CVFMI_ErrorResponse", "Error Occurred during preUpdateServiceOperationStatus Method:\n" + e.getMessage())
        }
        logger.trace("COMPLETED preUpdateServiceOperationStatus Process ")
        logger.info("Exited " + method)
	}

	public void preInitResourcesOperStatus(DelegateExecution execution){
        def method = getClass().getSimpleName() + '.preInitResourcesOperStatus(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)

        logger.trace("STARTED preInitResourcesOperStatus Process ")
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
            logger.info("Generated new operation for Service Instance serviceId:" + serviceId + " operationId:" + operationId + " operationType:" + operationType)
            serviceId = UriUtils.encode(serviceId,"UTF-8")
            execution.setVariable("serviceInstanceId", serviceId)
            execution.setVariable("operationId", operationId)
            execution.setVariable("operationType", operationType)

            String serviceRelationShip = execution.getVariable("serviceRelationShip")
            logger.info("serviceRelationShip: " + serviceRelationShip)
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

            def dbAdapterEndpoint = UrnPropertiesReader.getVariable("mso.adapters.openecomp.db.endpoint", execution)
            execution.setVariable("CVFMI_dbAdapterEndpoint", dbAdapterEndpoint)
            logger.info("DB Adapter Endpoint is: " + dbAdapterEndpoint)

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
            logger.debug("DoCustomDeleteE2EServiceInstanceV2 Outgoing initResourceOperationStatus Request: " + payload)

		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoCustomDeleteE2EServiceInstanceV2.preInitResourcesOperStatus. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
        logger.info("Exited " + method)
    }



	/**
    * prepare delete parameters
    */
	public void preProcessVFCResourceDelete(execution){
		// we use resource instance ids for delete flow as resourceTemplateUUIDs

		def method = getClass().getSimpleName() + '.preProcessVFCResourceDelete(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)

		logger.trace("STARTED preProcessVFCResourceDelete Process ")
		try{
			String serviceResource = execution.getVariable("serviceResource")
			logger.info("serviceResource : " + serviceResource)

			String resourceInstanceId  =  execution.getVariable("resourceInstanceId")
			logger.info("resourceInstanceId : " + resourceInstanceId)

			execution.setVariable("resourceTemplateId", resourceInstanceId)
			logger.info("resourceTemplateId : " + resourceInstanceId)

			String resourceType = execution.getVariable("resourceType")
			logger.info("resourceType : " + resourceType)


			String resourceName = execution.getVariable(Prefix+"resourceName")
			if (isBlank(resourceName)){
				resourceName = resourceInstanceId
			}
			execution.setVariable("resourceName", resourceName)
			logger.info("resource Name : " + resourceName)

			logger.info("Delete Resource Info: resourceInstanceId :" + resourceInstanceId + "  resourceTemplateId: " + resourceInstanceId + " resourceType: " + resourceType)
		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.preProcessVFCResourceDelete. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info("Exited " + method)
	}

	public void postProcessVFCDelete(DelegateExecution execution, String response, String action) {
		def method = getClass().getSimpleName() + '.postProcessVFCDelete(' +'execution=' + execution.getId() +')'
		logger.info("Entered " + method)

		logger.trace("STARTED postProcessVFCDelete Process ")
		try{

		}catch (BpmnError e) {
			throw e;
		} catch (Exception ex) {
			msg = "Exception in DoDeleteE2EServiceInstance.postProcessVFCDelete. " + ex.getMessage()
			logger.info(msg)
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg)
		}
		logger.info("Exited " + method)
	}
}

