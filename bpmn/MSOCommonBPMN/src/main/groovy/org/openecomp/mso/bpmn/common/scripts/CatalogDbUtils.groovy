/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.common.scripts

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.XML;
import org.springframework.web.util.UriUtils;

import org.openecomp.mso.bpmn.core.json.JsonUtils


import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.QName;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.rest.APIResponse;
import org.openecomp.mso.rest.RESTClient
import org.openecomp.mso.rest.RESTConfig


/***
 * Utilities for accessing Catalog DB Adapter to retrieve Networks, VNF/VFModules, AllottedResources and complete ServiceResources information
 *
 */

class CatalogDbUtils {

	MsoUtils utils = new MsoUtils()
	JsonUtils jsonUtils = new JsonUtils()
	MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	public JSONArray getAllNetworksByServiceModelUuid(String catalogDbEndpoint, String serviceModelUuid) {
		JSONArray networksList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceNetworks?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelInvariantUuid(String catalogDbEndpoint, String serviceModelInvariantUuid) {
		JSONArray networksList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceNetworks?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelInvariantUuidAndServiceModelVersion(String catalogDbEndpoint, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONArray networksList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceNetworks?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByNetworkModelCustomizationUuid(String catalogDbEndpoint, String networkModelCustomizationUuid) {
		JSONArray networksList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceNetworks?networkModelCustomizationUuid=" + UriUtils.encode(networkModelCustomizationUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByNetworkType(String catalogDbEndpoint, String networkType) {
		JSONArray networksList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceNetworks?networkType=" + UriUtils.encode(networkType, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}


	public JSONArray getAllVnfsByServiceModelUuid(String catalogDbEndpoint, String serviceModelUuid) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceVnfs?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelInvariantUuid(String catalogDbEndpoint, String serviceModelInvariantUuid) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceVnfs?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelInvariantUuidAndServiceModelVersion(String catalogDbEndpoint, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceVnfs?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByVnfModelCustomizationUuid(String catalogDbEndpoint, String vnfModelCustomizationUuid) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceVnfs?vnfModelCustomizationUuid=" + UriUtils.encode(vnfModelCustomizationUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}


	public JSONArray getAllottedResourcesByServiceModelUuid(String catalogDbEndpoint, String serviceModelUuid) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/ServiceAllottedResources?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelInvariantUuid(String catalogDbEndpoint, String serviceModelInvariantUuid) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceAllottedResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelInvariantUuidAndServiceModelVersion(String catalogDbEndpoint, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceAllottedResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByArModelCustomizationUuid(String catalogDbEndpoint, String arModelCustomizationUuid) {
		JSONArray vnfsList = null
		String endPoint = catalogDbEndpoint + "/v1/serviceAllottedResources?serviceModelCustomizationUuid=" + UriUtils.encode(arModelCustomizationUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONObject getServiceResourcesByServiceModelInvariantUuid(String catalogDbEndpoint, String serviceModelInvariantUuid) {
		JSONObject resources = null
		String endPoint = catalogDbEndpoint + "/v1/serviceResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				resources = parseServiceResourcesJson(catalogDbResponse)
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return resources
	}

	public JSONObject getServiceResourcesByServiceModelInvariantUuidAndServiceModelVersion(String catalogDbEndpoint, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONObject resources = null
		String endPoint = catalogDbEndpoint + "/v1/serviceResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(endPoint)

			if (catalogDbResponse != null) {
				resources = parseServiceResourcesJson(catalogDbResponse)
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return resources
	}



	private JSONArray parseNetworksJson (String catalogDbResponse, String arrayName) {
		JSONArray modelInfos = null

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray networks = responseJson.getJSONArray(arrayName)
			modelInfos = new JSONArray()

			for (int i = 0; i < networks.length(); i++) {

				JSONObject network = networks.getJSONObject(i)
				JSONObject modelInfo = buildModelInfo("network", network)
				JSONObject modelJson = new JSONObject()
				modelJson.put("modelInfo", modelInfo)
				String networkType = jsonUtils.getJsonValueForKey(network, "networkType")
				modelJson.put("networkType", networkType)
				modelInfos.put(modelJson)
			}

			String modelInfosString = modelInfos.toString()
			msoLogger.debug("Returning networks JSON: " + modelInfosString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return modelInfos
	}

	private JSONArray parseVnfsJson (String catalogDbResponse, String arrayName) {
		JSONArray modelInfos = null

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray vnfs = responseJson.getJSONArray(arrayName)
			modelInfos = new JSONArray()

			for (int i = 0; i < vnfs.length(); i++) {
				JSONObject vnf = vnfs.getJSONObject(i)

				msoLogger.debug(vnf.toString(2))
				JSONObject modelInfo = buildModelInfo("vnf", vnf)
				JSONObject modelJson = new JSONObject()
				modelJson.put("modelInfo", modelInfo)

				JSONArray vfModules = null
				try {
					vfModules = vnf.getJSONArray("vfModules")
				} catch (Exception e)
				{
					msoLogger.debug("Cannot find VF MODULE ARRAY: " + i + ", exception is " + e.message)
				}

				if (vfModules != null) {
					JSONArray vfModuleInfo = new JSONArray()
					for (int j = 0; j < vfModules.length(); j++) {
						JSONObject vfModule = vfModules.getJSONObject(j)
						JSONObject vfModuleModelInfo = buildModelInfo("vfModule", vfModule)
						JSONObject vfModuleModelJson = new JSONObject()
						vfModuleModelJson.put("modelInfo", vfModuleModelInfo)
						String vfModuleType = jsonUtils.getJsonValueForKey(vfModule, "type")
						vfModuleModelJson.put("vfModuleType", vfModuleType)
						Integer isBase = jsonUtils.getJsonIntValueForKey(vfModule, "isBase")
						if (isBase.intValue() == 1) {
							vfModuleModelJson.put("isBase", "true")
						}
						else {
							vfModuleModelJson.put("isBase", "false")
						}
						String vfModuleLabel = jsonUtils.getJsonValueForKey(vfModule, "label")
						vfModuleModelJson.put("vfModuleLabel", vfModuleLabel)
						Integer initialCount = jsonUtils.getJsonIntValueForKey(vfModule, "initialCount")
						vfModuleModelJson.put("initialCount", initialCount.intValue())
						vfModuleInfo.put(vfModuleModelJson)
					}
					modelJson.put("vfModules", vfModuleInfo)
				}
				modelInfos.put(modelJson)
			}

			String modelInfosString = modelInfos.toString()
			msoLogger.debug("Returning vnfs JSON: " + modelInfosString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return modelInfos
	}

	private JSONArray parseAllottedResourcesJson (String catalogDbResponse, String arrayName) {
		JSONArray modelInfos = null

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray allottedResources = responseJson.getJSONArray(arrayName)
			modelInfos = new JSONArray()

			for (int i = 0; i < allottedResources.length(); i++) {
				JSONObject allottedResource = allottedResources.getJSONObject(i)
				JSONObject modelInfo = buildModelInfo("allottedResource", allottedResource)
				JSONObject modelJson = new JSONObject()
				modelJson.put("modelInfo", modelInfo)
				modelInfos.put(modelJson)
			}

			String modelInfosString = modelInfos.toString()
			msoLogger.debug("Returning allottedResources JSON: " + modelInfosString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return modelInfos
	}

	private JSONObject parseServiceResourcesJson (String catalogDbResponse) {
		JSONObject serviceResources = new JSONObject()

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONObject serviceResourcesRoot = responseJson.getJSONObject("serviceResources")
			JSONArray vnfsArray = parseVnfsJson(serviceResourcesRoot.toString(), "vnfResources")
			serviceResources.put("vnfs", vnfsArray)
			JSONArray networksArray = parseNetworksJson(serviceResourcesRoot.toString(), "networkResourceCustomization")
			serviceResources.put("networks", networksArray)
			JSONArray allottedResourcesArray = parseAllottedResourcesJson(serviceResourcesRoot.toString(), "allottedResourceCustomization")
			serviceResources.put("allottedResources", allottedResourcesArray)

			String serviceResourcesString = serviceResources.toString()
			msoLogger.debug("Returning serviceResources JSON: " + serviceResourcesString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return serviceResources
	}

	private JSONObject buildModelInfo(String modelType, JSONObject modelFromDb) {
		JSONObject modelInfo = null
		try {
			modelInfo = new JSONObject()
			modelInfo.put("modelType", modelType)
			String modelInvariantId = jsonUtils.getJsonValueForKey(modelFromDb, "modelInvariantUuid")
			modelInfo.put("modelInvariantId", modelInvariantId)
			if(modelType.equalsIgnoreCase("allottedResource")){
				String modelInstanceName = jsonUtils.getJsonValueForKey(modelFromDb, "modelInstanceName")
				modelInfo.put("modelInstanceName", modelInstanceName)
			}
			if (!"vfModule".equals(modelType) && !"vnf".equals(modelType)) {
				String modelVersionId = jsonUtils.getJsonValueForKey(modelFromDb, "modelUuid")
				modelInfo.put("modelVersionId", modelVersionId)
			}
			else {
				String modelVersionId = jsonUtils.getJsonValueForKey(modelFromDb, "asdcUuid")
				modelInfo.put("modelVersionId", modelVersionId)
			}
			String modelName = jsonUtils.getJsonValueForKey(modelFromDb, "modelName")
			modelInfo.put("modelName", modelName)
			String modelVersion = jsonUtils.getJsonValueForKey(modelFromDb, "modelVersion")
			modelInfo.put("modelVersion", modelVersion)
			if (!"vfModule".equals(modelType)) {
				String modelCustomizationName = jsonUtils.getJsonValueForKey(modelFromDb, "modelCustomizationName")
				modelInfo.put("modelCustomizationName", modelCustomizationName)
			}
			String modelCustomizationId = jsonUtils.getJsonValueForKey(modelFromDb, "modelCustomizationUuid")
			modelInfo.put("modelCustomizationId", modelCustomizationId)
			JSONObject modelJson = new JSONObject()
			modelJson.put("modelInfo", modelInfo)
		}
		catch (Exception e) {
			utils.log("ERROR", "Exception while parsing model information: " + e.message)
		}
		return modelInfo
	}

	private String getResponseFromCatalogDb (String endPoint) {
		try {
			RESTConfig config = new RESTConfig(endPoint);
			def responseData = ''
			def bpmnRequestId = UUID.randomUUID().toString()
			RESTClient client = new RESTClient(config).
				addHeader('X-TransactionId', bpmnRequestId).
				addHeader('X-FromAppId', 'BPMN').
				addHeader('Content-Type', 'application/json').
				addHeader('Accept','application/json');
			msoLogger.debug('sending GET to Catalog DB endpoint' + endPoint)
			APIResponse response = client.httpGet()

			responseData = response.getResponseBodyAsString()
			if (responseData != null) {
				msoLogger.debug("Received data from Catalog DB: " + responseData)
			}

			msoLogger.debug('Response code:' + response.getStatusCode())
			msoLogger.debug('Response:' + System.lineSeparator() + responseData)
			if (response.getStatusCode() == 200) {
				// parse response as needed
				return responseData
			}
			else {
				return null
			}
		}
		catch (Exception e) {
			msoLogger.debug("ERROR WHILE QUERYING CATALOG DB: " + e.message)
			return null
		}

	}
}
