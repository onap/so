/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.resource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.runtime.Execution;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.TargetEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ResourceRequestBuilder {

    private static String CUSTOMIZATION_UUID = "cuserviceResourcesstomizationUUID";

    private static String SERVICE_URL_SERVICE_INSTANCE = "/v2/serviceResources";

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ResourceRequestBuilder.class);

    static JsonUtils jsonUtil = new JsonUtils();

	public static List<String> getResourceSequence(String serviceUuid) {

        List<String> resourceSequence = new ArrayList();
        try {
            Map<String, Object> serviceResponse = getServiceInstnace(serviceUuid);

            if (serviceResponse.containsKey("serviceResources")) {
                Map<String, Object> serviceResources = (Map<String, Object>) serviceResponse.get("serviceResources");

                if (serviceResources.containsKey("resourceOrder")) {
                    String resourceOrder = (String) serviceResources.get("resourceOrder");
                    if (resourceOrder!= null) {
                        resourceSequence.addAll(Arrays.asList(resourceOrder.split(",")));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("not able to retrieve service order.");
        }
        return resourceSequence;
	}

     /* build the resource Parameters detail.
     * It's a json string for resource instantiant
     * {
     *     "locationConstraints":[...]
     *     "requestInputs":{K,V}
     * }
     * <br>
     *
     * @param execution Execution context
     * @param serviceUuid The service template uuid
     * @param resourceCustomizationUuid The resource customization uuid
     * @param serviceParameters the service parameters passed from the API
     * @return the resource instantiate parameters
     * @since ONAP Beijing Release
     */
    @SuppressWarnings("unchecked")
    public static String buildResourceRequestParameters(Execution execution, String serviceUuid, String resourceCustomizationUuid, String serviceParameters) {
        List<String> resourceList = jsonUtil.StringArrayToList(execution, (String)JsonUtils.getJsonValue(serviceParameters, "resources"));
        //Get the right location str for resource. default is an empty array.
        String locationConstraints ="[]";
        String resourceInputsFromUui = "";
        for(String resource: resourceList){
            String resCusUuid = (String)JsonUtils.getJsonValue(resource, "resourceCustomizationUuid");
            if(resourceCustomizationUuid.equals(resCusUuid)){
                String resourceParameters = JsonUtils.getJsonValue(resource, "parameters");
                locationConstraints = JsonUtils.getJsonValue(resourceParameters, "locationConstraints");
                resourceInputsFromUui = JsonUtils.getJsonValue(resourceParameters, "requestInputs");
            }
        }
        Map<String, Object> serviceInput = null;
        if (JsonUtils.getJsonValue(serviceParameters, "requestInputs") != null) {
            serviceInput = getJsonObject((String)JsonUtils.getJsonValue(serviceParameters, "requestInputs"), Map.class);
        }

        Map<String, Object> resourceInputsFromUuiMap = getJsonObject(resourceInputsFromUui, Map.class);

        if (serviceInput == null) {
            serviceInput = new HashMap();
        }

        if (resourceInputsFromUuiMap == null) {
            resourceInputsFromUuiMap = new HashMap();
        }

        Map<String, Object> resourceInputsFromServiceDeclaredLevel = buildResouceRequest(serviceUuid, resourceCustomizationUuid, serviceInput);
        resourceInputsFromUuiMap.putAll(resourceInputsFromServiceDeclaredLevel);
        String resourceInputsStr = getJsonString(resourceInputsFromUuiMap);
        String result = "{\n"
                + "\"locationConstraints\":" + locationConstraints +",\n"
                + "\"requestInputs\":" + resourceInputsStr +"\n"
                +"}";
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildResouceRequest(String serviceUuid, String resourceCustomizationUuid, Map<String, Object> serviceInputs) {
        try {
            Map<String, Object> serviceInstnace = getServiceInstnace(serviceUuid);

            // find match of customization uuid in vnf
            Map<String, Map<String, Object>> serviceResources = (Map<String, Map<String, Object>>) serviceInstnace.get("serviceResources");

            List<Map<String, Object>> serviceVnfCust = (List<Map<String, Object>>) serviceResources.get("serviceVnfs");
            String resourceInputStr = getResourceInputStr(serviceVnfCust, resourceCustomizationUuid);

            // find match in network resource
            if (resourceInputStr == null) {
                List<Map<String, Object>> serviceNetworkCust = (List<Map<String, Object>>) serviceResources.get("serviceNetworks");
                resourceInputStr = getResourceInputStr(serviceNetworkCust, resourceCustomizationUuid);

                // find match in AR resource
                if (resourceInputStr == null) {
                    List<Map<String, Object>> serviceArCust = (List<Map<String, Object>>) serviceResources.get("serviceAllottedResources");
                    resourceInputStr = getResourceInputStr(serviceArCust, resourceCustomizationUuid);
                }
            }

           if (null != resourceInputStr  || !resourceInputStr.equals("")) {
                return getResourceInput(resourceInputStr, serviceInputs);
           }

        } catch (Exception e) {
            LOGGER.error("not able to retrieve service instance");
        }
        return new HashMap();
    }

    private static String getResourceInputStr(List<Map<String, Object>> resources, String resCustomizationUuid) {

        for (Map<String, Object> resource : resources) {
            Map<String, String> modelInfo = (Map<String, String>) resource.get("modelInfo");

            if (modelInfo.get("modelCustomizationUuid").equalsIgnoreCase(resCustomizationUuid)) {
                return (String) resource.get("resourceInput");
            }
        }
        return null;
    }

    // this method combines resource input with service input
    private static Map<String, Object> getResourceInput(String resourceInputStr, Map<String, Object> serviceInputs) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, Object> resourceInput = gson.fromJson(resourceInputStr, type);

        // replace value if key is available in service input
        for (String key: resourceInput.keySet()) {
            String value = (String) resourceInput.get(key);

            if (value.contains("|")) {
                // node it type of getinput
                String[] split = value.split("\\|");
                String tmpKey = split[0];
                if (serviceInputs.containsKey(tmpKey)) {
                    value = (String) serviceInputs.get(tmpKey);
                } else {
                    value = split[1];
                }
            }
            resourceInput.put(key,value);
        }
        return resourceInput;
    }

    public static Map<String, Object> getServiceInstnace(String uuid) throws Exception {
        String catalogEndPoint = UrnPropertiesReader.getVariable("mso.catalog.db.endpoint");

        HttpClient client = new HttpClientFactory().newJsonClient(
        	    UriBuilder.fromUri(catalogEndPoint).path(SERVICE_URL_SERVICE_INSTANCE).queryParam("serviceModelUuid", uuid).build().toURL(),
                TargetEntity.CATALOG_DB);

        client.addAdditionalHeader("Accept", "application/json");
        client.addAdditionalHeader("Authorization", UrnPropertiesReader.getVariable("mso.db.auth"));

        Response apiResponse = client.get();

        String value = apiResponse.readEntity(String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Object> map = objectMapper.readValue(value, HashMap.class);
        return map;
    }

    public static <T> T getJsonObject(String jsonstr, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        try {
            return mapper.readValue(jsonstr, type);
        } catch(IOException e) {
            LOGGER.error("fail to unMarshal json" + e.getMessage ());
        }
        return null;
    }

    public static String getJsonString(Object srcObj)  {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        String jsonStr = null;
        try {
            jsonStr = mapper.writeValueAsString(srcObj);
        } catch(JsonProcessingException e) {
        	LOGGER.error("SdcToscaParserException", e);
        }
        return jsonStr;
    }
}
