/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
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

import org.camunda.bpm.engine.runtime.Execution

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
	static private String defaultDbAdapterVersion = "v2"

	public JSONArray getAllNetworksByServiceModelUuid(Execution execution, String serviceModelUuid) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelUuid(Execution execution, String serviceModelUuid, String catalogUtilsVersion) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					networksList = responseJson.getJSONArray("serviceNetworks")
				}
				else {
					networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid, String catalogUtilsVersion) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					networksList = responseJson.getJSONArray("serviceNetworks")
				}
				else {
					networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion, String catalogUtilsVersion) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					networksList = responseJson.getJSONArray("serviceNetworks")
				}
				else {
					networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByNetworkModelCustomizationUuid(Execution execution, String networkModelCustomizationUuid) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?networkModelCustomizationUuid=" + UriUtils.encode(networkModelCustomizationUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByNetworkModelCustomizationUuid(Execution execution, String networkModelCustomizationUuid, String catalogUtilsVersion) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?networkModelCustomizationUuid=" + UriUtils.encode(networkModelCustomizationUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					networksList = responseJson.getJSONArray("serviceNetworks")
				}
				else {
					networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByNetworkType(Execution execution, String networkType) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?networkType=" + UriUtils.encode(networkType, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}

	public JSONArray getAllNetworksByNetworkType(Execution execution, String networkType, String catalogUtilsVersion) {
		JSONArray networksList = null		
		String endPoint = "/serviceNetworks?networkType=" + UriUtils.encode(networkType, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					networksList = responseJson.getJSONArray("serviceNetworks")
				}
				else {
					networksList = parseNetworksJson(catalogDbResponse, "serviceNetworks", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return networksList
	}


	public JSONArray getAllVnfsByServiceModelUuid(Execution execution, String serviceModelUuid) {
		JSONArray vnfsList = null		
		String endPoint = "/serviceVnfs?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelUuid(Execution execution, String serviceModelUuid, String catalogUtilsVersion) {
		JSONArray vnfsList = null		
		String endPoint = "/serviceVnfs?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceVnfs")
				}
				else {
					vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid) {
		JSONArray vnfsList = null		
		String endPoint ="/serviceVnfs?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid, String catalogUtilsVersion) {
		JSONArray vnfsList = null		
		String endPoint = "/serviceVnfs?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceVnfs")
				}
				else {
					vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONArray vnfsList = null		
		String endPoint =  "/serviceVnfs?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion, String catalogUtilsVersion) {
		JSONArray vnfsList = null		
		String endPoint = "/serviceVnfs?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceVnfs")
				}
				else {
					vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllVnfsByVnfModelCustomizationUuid(Execution execution, String vnfModelCustomizationUuid) {
		JSONArray vnfsList = null		
		String endPoint = "/serviceVnfs?vnfModelCustomizationUuid=" + UriUtils.encode(vnfModelCustomizationUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}
	
	/**
	 * This method gets a all vnfs for a particular
	 * service from the catalog database using the
	 * service model's model name.
	 *
	 * @param catalogDbEndpoint
	 * @param serviceModelModelName
	 * @return vnfsList	 *
	 * 
	 */
	public JSONArray getAllVnfsByServiceModelModelName(Execution execution, String serviceModelModelName) {
		JSONArray vnfsList = null
		String endPoint = "/serviceVnfs?serviceModelName=" + UriUtils.encode(serviceModelModelName, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", defaultDbAdapterVersion)
			}
		}catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}
		return vnfsList
	}

	public JSONArray getAllVnfsByVnfModelCustomizationUuid(Execution execution, String vnfModelCustomizationUuid, String catalogUtilsVersion) {
		JSONArray vnfsList = null		
		String endPoint = "/serviceVnfs?vnfModelCustomizationUuid=" + UriUtils.encode(vnfModelCustomizationUuid, "UTF-8")
		try {
			msoLogger.debug("ENDPOINT: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceVnfs")
				}
				else {
					vnfsList = parseVnfsJson(catalogDbResponse, "serviceVnfs", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	/**
	 * This method gets a single vf module from
	 * the catalog database using the vf module's
	 * model name. It returns that vf module as
	 * a JSONObject
	 *
	 * @param catalogDbEndpoint
	 * @param vfModuleModelName
	 * @return vfModule
	 */
	public JSONObject getVfModuleByVfModuleModelName(Execution execution, String vfModuleModelName) {
		JSONObject vfModule = null		
		String endPoint = "/vfModules?vfModuleModelName=" + UriUtils.encode(vfModuleModelName, "UTF-8")
		try{
			msoLogger.debug("Get VfModule By VfModule ModelName Endpoint is: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vfModule = parseVfModuleJson(catalogDbResponse, "vfModules", "v1")
			}
		}
		catch(Exception e){
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vfModule
	}

	/**
	 * This method gets a single vf module from
	 * the catalog database using the vf module's
	 * model name. It returns that vf module as
	 * a JSONObject
	 *
	 * @param catalogDbEndpoint
	 * @param vfModuleModelName
	 * @param catalogUtilsVersion
	 * @return vfModules
	 */
	public JSONObject getVfModuleByVfModuleModelName(Execution execution, String vfModuleModelName, String catalogUtilsVersion)  {
		JSONObject vfModule = null
		String endPoint = "/vfModules?vfModuleModelName=" + UriUtils.encode(vfModuleModelName, "UTF-8")
		try{
			msoLogger.debug("Get VfModule By VfModule ModelName Endpoint is: " + endPoint)
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vfModule = parseVfModuleJson(catalogDbResponse, "vfModules", "v1")
			}
		}
		catch(Exception e){
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vfModule
	}


	public JSONArray getAllottedResourcesByServiceModelUuid(Execution execution, String serviceModelUuid) {
		JSONArray vnfsList = null
		String endPoint = "/ServiceAllottedResources?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelUuid(Execution execution, String serviceModelUuid, String catalogUtilsVersion) {
		JSONArray vnfsList = null
		String endPoint = "/ServiceAllottedResources?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceAllottedResources")
				}
				else {
					vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid) {
		JSONArray vnfsList = null
		String endPoint = "/serviceAllottedResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid, String catalogUtilsVersion) {
		JSONArray vnfsList = null
		String endPoint = "/serviceAllottedResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray()
				}
				else {
					vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.getStackTrace())
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONArray vnfsList = null
		String endPoint = "/serviceAllottedResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion, String catalogUtilsVersion) {
		JSONArray vnfsList = null
		String endPoint = "/serviceAllottedResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceAllottedResources")
				}
				else {
					vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}


	public JSONArray getAllottedResourcesByArModelCustomizationUuid(Execution execution, String arModelCustomizationUuid) {
		JSONArray vnfsList = null
		String endPoint = "/serviceAllottedResources?serviceModelCustomizationUuid=" + UriUtils.encode(arModelCustomizationUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONArray getAllottedResourcesByArModelCustomizationUuid(Execution execution, String arModelCustomizationUuid, String catalogUtilsVersion) {
		JSONArray vnfsList = null
		String endPoint = "/serviceAllottedResources?serviceModelCustomizationUuid=" + UriUtils.encode(arModelCustomizationUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					JSONObject responseJson = new JSONObject(catalogDbResponse)
					vnfsList = responseJson.getJSONArray("serviceAllottedResources")
				}
				else {
					vnfsList = parseAllottedResourcesJson(catalogDbResponse, "serviceAllottedResources", catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return vnfsList
	}

	public JSONObject getServiceResourcesByServiceModelUuid(Execution execution, String serviceModelUuid) {
        JSONObject resources = null
        String endPoint = "/serviceResources?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
        try {
            String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

            if (catalogDbResponse != null) {

                resources = parseServiceResourcesJson(catalogDbResponse, "v1")
            }

        }
        catch (Exception e) {
            utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
        }

        return resources
    }

    public JSONObject getServiceResourcesByServiceModelUuid(Execution execution, String serviceModelUuid, String catalogUtilsVersion) {
        JSONObject resources = null
        String endPoint = "/serviceResources?serviceModelUuid=" + UriUtils.encode(serviceModelUuid, "UTF-8")
        try {
            String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

            if (catalogDbResponse != null) {
                if (!catalogUtilsVersion.equals("v1")) {
                    resources = new JSONObject(catalogDbResponse)
                }
                else {
                    resources = parseServiceResourcesJson(catalogDbResponse, catalogUtilsVersion)
                }
            }

        }
        catch (Exception e) {
            utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
        }

        return resources
    }

	
	public JSONObject getServiceResourcesByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid) {
		JSONObject resources = null
		String endPoint = "/serviceResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {

				resources = parseServiceResourcesJson(catalogDbResponse, "v1")
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return resources
	}

	public JSONObject getServiceResourcesByServiceModelInvariantUuid(Execution execution, String serviceModelInvariantUuid, String catalogUtilsVersion) {
		JSONObject resources = null
		String endPoint = "/serviceResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					resources = new JSONObject(catalogDbResponse)
				}
				else {
					resources = parseServiceResourcesJson(catalogDbResponse, catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return resources
	}


	public JSONObject getServiceResourcesByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion) {
		JSONObject resources = null
		String endPoint = "/serviceResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				resources = parseServiceResourcesJson(catalogDbResponse)
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return resources
	}

	public JSONObject getServiceResourcesByServiceModelInvariantUuidAndServiceModelVersion(Execution execution, String serviceModelInvariantUuid, String serviceModelVersion, String catalogUtilsVersion) {
		JSONObject resources = null
		String endPoint = "/serviceResources?serviceModelInvariantUuid=" + UriUtils.encode(serviceModelInvariantUuid, "UTF-8") + "&serviceModelVersion=" + UriUtils.encode(serviceModelVersion, "UTF-8")
		try {
			String catalogDbResponse = getResponseFromCatalogDb(execution, endPoint)

			if (catalogDbResponse != null) {
				if (!catalogUtilsVersion.equals("v1")) {
					resources = new JSONObject(catalogDbResponse)
				}
				else {
					resources = parseServiceResourcesJson(catalogDbResponse, catalogUtilsVersion)
				}
			}

		}
		catch (Exception e) {
			utils.log("ERROR", "Exception in Querying Catalog DB: " + e.message)
		}

		return resources
	}



	private JSONArray parseNetworksJson (String catalogDbResponse, String arrayName, String catalogUtilsVersion) {
		JSONArray modelInfos = null

		msoLogger.debug("parseNetworksJson - catalogUtilsVersion is " + catalogUtilsVersion)
		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray networks = responseJson.getJSONArray(arrayName)
			modelInfos = new JSONArray()

			for (int i = 0; i < networks.length(); i++) {

				JSONObject network = networks.getJSONObject(i)
				JSONObject modelJson = new JSONObject()
				JSONObject modelInfo = buildModelInfo("network", network, catalogUtilsVersion)
				modelJson.put("modelInfo", modelInfo)
				String networkType = jsonUtils.getJsonValueForKey(network, "networkType")
				modelJson.put("networkType", networkType)

				switch (catalogUtilsVersion) {
					case "v1":
						break
					default:
						String toscaNodeType = jsonUtils.getJsonValueForKey(network, "toscaNodeType")
						modelJson.put("toscaNodeType", toscaNodeType)
						String networkTechnology = jsonUtils.getJsonValueForKey(network, "networkTechnology")
						modelJson.put("networkTechnology", networkTechnology)
						String networkRole = jsonUtils.getJsonValueForKey(network, "networkRole")
						modelJson.put("networkRole", networkRole)
						String networkScope = jsonUtils.getJsonValueForKey(network, "networkScope")
						modelJson.put("networkScope", networkScope)
						break
				}
				modelInfos.put(modelJson)
			}

			String modelInfosString = modelInfos.toString()
			msoLogger.debug("Returning networks JSON: " + modelInfosString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return modelInfos
	}

	private JSONArray parseVnfsJson (String catalogDbResponse, String arrayName, String catalogUtilsVersion) {
		JSONArray modelInfos = null

		msoLogger.debug("parseVnfsJson - catalogUtilsVersion is " + catalogUtilsVersion)

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray vnfs = responseJson.getJSONArray(arrayName)
			modelInfos = new JSONArray()

			for (int i = 0; i < vnfs.length(); i++) {
				JSONObject vnf = vnfs.getJSONObject(i)

				msoLogger.debug(vnf.toString(2))
				JSONObject modelInfo = buildModelInfo("vnf", vnf, catalogUtilsVersion)
				JSONObject modelJson = new JSONObject()
				modelJson.put("modelInfo", modelInfo)
				switch(catalogUtilsVersion) {
					case "v1":
						break
					default:
						String toscaNodeType = jsonUtils.getJsonValueForKey(vnf, "toscaNodeType")
						modelJson.put("toscaNodeType", toscaNodeType)
						String nfType = jsonUtils.getJsonValueForKey(vnf, "nfType")
						modelJson.put("nfType", nfType)
						String nfRole = jsonUtils.getJsonValueForKey(vnf, "nfRole")
						modelJson.put("nfRole", nfRole)
						String nfCode = jsonUtils.getJsonValueForKey(vnf, "nfCode")
						modelJson.put("nfNamingCode", nfCode)
						String nfFunction = jsonUtils.getJsonValueForKey(vnf, "nfFunction")
						modelJson.put("nfFunction", nfFunction)
						break
				}
				
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
						JSONObject vfModuleModelInfo = buildModelInfo("vfModule", vfModule, catalogUtilsVersion)
						JSONObject vfModuleModelJson = new JSONObject()
						vfModuleModelJson.put("modelInfo", vfModuleModelInfo)
						String vfModuleType = jsonUtils.getJsonValueForKey(vfModule, "type")
						vfModuleModelJson.put("vfModuleType", vfModuleType)
						switch(catalogUtilsVersion) {
							case "v1":
								Integer isBase = jsonUtils.getJsonIntValueForKey(vfModule, "isBase")
								if (isBase.intValue() == 1) {
									vfModuleModelJson.put("isBase", "true")
								}
								else {
									vfModuleModelJson.put("isBase", "false")
								}
								break
							default:
								boolean isBase = jsonUtils.getJsonBooleanValueForKey(vfModule, "isBase")
								vfModuleModelJson.put("isBase", isBase)
								break
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

	/**
	 * This method parses a Vf Module from the
	 * Vf Modules array
	 *
	 * @param catalogDbResponse
	 * @param arrayName
	 * @param catalogUtilsVersion
	 * @return vfModulelJson
	 */
	private JSONObject parseVfModuleJson (String catalogDbResponse, String arrayName, String catalogUtilsVersion) {
		JSONObject vfModulelJson = new JSONObject()
		msoLogger.debug("Started Parse Vf Module Json")
		try {
			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray vfModules = responseJson.getJSONArray(arrayName)
			if(vfModules != null){
				JSONObject vfModuleInfo = new JSONObject()
				for (int i = 0; i < vfModules.length(); i++) {
					JSONObject vfModule = vfModules.getJSONObject(i)
					JSONObject vfModuleModelInfo = buildModelInfo("vfModule", vfModule, catalogUtilsVersion)
					vfModulelJson.put("modelInfo", vfModuleModelInfo)
					String vfModuleType = jsonUtils.getJsonValueForKey(vfModule, "type")
					vfModulelJson.put("vfModuleType", vfModuleType)
					switch(catalogUtilsVersion) {
						case "v1":
							Integer isBase = jsonUtils.getJsonIntValueForKey(vfModule, "isBase")
							if (isBase.intValue() == 1) {
								vfModulelJson.put("isBase", "true")
							}
							else {
								vfModulelJson.put("isBase", "false")
							}
							break
						default:
							boolean isBase = jsonUtils.getJsonBooleanValueForKey(vfModule, "isBase")
							vfModulelJson.put("isBase", isBase)
							break
					}
					String vfModuleLabel = jsonUtils.getJsonValueForKey(vfModule, "label")
					vfModulelJson.put("vfModuleLabel", vfModuleLabel)
					Integer initialCount = jsonUtils.getJsonIntValueForKey(vfModule, "initialCount")
					vfModulelJson.put("initialCount", initialCount.intValue())
				}
			}
			msoLogger.debug("Completed Parsing Vf Module: " + vfModulelJson.toString())
		}catch (Exception e){
			utils.log("DEBUG", "Exception while parsing Vf Modules from Catalog DB Response: " + e.message)
		}

		return vfModulelJson
	}

	private JSONArray parseAllottedResourcesJson (String catalogDbResponse, String arrayName, String catalogUtilsVersion) {
		JSONArray modelInfos = null

		msoLogger.debug("parseAllottedResourcesJson - catalogUtilsVersion is " + catalogUtilsVersion)

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONArray allottedResources = responseJson.getJSONArray(arrayName)
			modelInfos = new JSONArray()

			for (int i = 0; i < allottedResources.length(); i++) {
				JSONObject allottedResource = allottedResources.getJSONObject(i)
				JSONObject modelInfo = buildModelInfo("allottedResource", allottedResource, catalogUtilsVersion)
				JSONObject modelJson = new JSONObject()
				modelJson.put("modelInfo", modelInfo)
				switch(catalogUtilsVersion) {
					case "v1":
						break
					default:
						String toscaNodeType = jsonUtils.getJsonValueForKey(allottedResource, "toscaNodeType")
						modelJson.put("toscaNodeType", toscaNodeType)
						String nfType = jsonUtils.getJsonValueForKey(allottedResource, "nfType")
						modelJson.put("nfType", nfType)
						String nfRole = jsonUtils.getJsonValueForKey(allottedResource, "nfRole")
						modelJson.put("nfRole", nfRole)
						String nfCode = jsonUtils.getJsonValueForKey(allottedResource, "nfCode")
						modelJson.put("nfNamingCode", nfCode)
						String nfFunction = jsonUtils.getJsonValueForKey(allottedResource, "nfFunction")
						modelJson.put("nfFunction", nfFunction)
						String parentServiceModelUuid = jsonUtils.getJsonValueForKey(allottedResource, "parentServiceModelUuid")
						modelJson.put("parentServiceModelUuid", parentServiceModelUuid)
						break
				}


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
		String catalogUtilsVersion = "v1"

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONObject serviceResourcesRoot = responseJson.getJSONObject("serviceResources")
			JSONArray vnfsArray = parseVnfsJson(serviceResourcesRoot.toString(), "vnfResources", catalogUtilsVersion)
			serviceResources.put("vnfs", vnfsArray)
			JSONArray networksArray = parseNetworksJson(serviceResourcesRoot.toString(), "networkResourceCustomization", catalogUtilsVersion)
			serviceResources.put("networks", networksArray)
			JSONArray allottedResourcesArray = parseAllottedResourcesJson(serviceResourcesRoot.toString(), "allottedResourceCustomization", catalogUtilsVersion)
			serviceResources.put("allottedResources", allottedResourcesArray)

			String serviceResourcesString = serviceResources.toString()
			msoLogger.debug("Returning serviceResources JSON: " + serviceResourcesString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return serviceResources
	}

	private JSONObject parseServiceResourcesJson (String catalogDbResponse, String catalogUtilsVersion) {
		JSONObject serviceResources = new JSONObject()
		JSONObject serviceResourcesObject = new JSONObject()
		String serviceResourcesString = ""

		try {
			// Create array of jsons

			JSONObject responseJson = new JSONObject(catalogDbResponse)
			JSONObject serviceResourcesRoot = responseJson.getJSONObject("serviceResources")
			JSONObject modelInfo = buildModelInfo("", serviceResourcesRoot, catalogUtilsVersion)
			serviceResources.put("modelInfo", modelInfo)
			JSONArray vnfsArray = parseVnfsJson(serviceResourcesRoot.toString(), "serviceVnfs", catalogUtilsVersion)
			serviceResources.put("serviceVnfs", vnfsArray)
			JSONArray networksArray = parseNetworksJson(serviceResourcesRoot.toString(), "serviceNetworks", catalogUtilsVersion)
			serviceResources.put("serviceNetworks", networksArray)
			JSONArray allottedResourcesArray = parseAllottedResourcesJson(serviceResourcesRoot.toString(), "serviceAllottedResources", catalogUtilsVersion)
			serviceResources.put("serviceAllottedResources", allottedResourcesArray)
			serviceResourcesObject.put("serviceResources", serviceResources)

			serviceResourcesString = serviceResourcesObject.toString()
			msoLogger.debug("Returning serviceResources JSON: " + serviceResourcesString)

		} catch (Exception e) {
			utils.log("ERROR", "Exception in parsing Catalog DB Response: " + e.message)
		}

		return serviceResourcesObject
	}

	private JSONObject buildModelInfo(String modelType, JSONObject modelFromDb, String catalogUtilsVersion) {
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
			if ((!"vfModule".equals(modelType) && !"vnf".equals(modelType)) || !catalogUtilsVersion.equals("v1")) {
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
			switch (catalogUtilsVersion) {
				case "v1":
					modelInfo.put("modelCustomizationId", modelCustomizationId)
					break
				default:
					modelInfo.put("modelCustomizationUuid", modelCustomizationId)
					break
			}
			JSONObject modelJson = new JSONObject()
			modelJson.put("modelInfo", modelInfo)
		}
		catch (Exception e) {
			utils.log("ERROR", "Exception while parsing model information: " + e.message)
		}
		return modelInfo
	}

	private String getResponseFromCatalogDb (Execution execution, String endPoint) {
		try {
			String catalogDbEndpoint = execution.getVariable("URN_mso_catalog_db_endpoint")
			String queryEndpoint = catalogDbEndpoint + "/" + defaultDbAdapterVersion + endPoint
			RESTConfig config = new RESTConfig(queryEndpoint);
			def responseData = ''
			def bpmnRequestId = UUID.randomUUID().toString()
			RESTClient client = new RESTClient(config).
					addHeader('X-TransactionId', bpmnRequestId).
					addHeader('X-FromAppId', 'BPMN').
					addHeader('Content-Type', 'application/json').
					addHeader('Accept','application/json');
					
			String basicAuthCred = execution.getVariable("BasicAuthHeaderValueDB")
			if (basicAuthCred != null && !"".equals(basicAuthCred)) {
					client.addAuthorizationHeader(basicAuthCred)
			}
			msoLogger.debug('sending GET to Catalog DB endpoint: ' + endPoint)
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