/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.runtime.Execution;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.domain.GroupResource;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ResourceType;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;
import org.onap.logging.filter.base.ONAPComponents;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceRequestBuilder {

    private static String SERVICE_URL_SERVICE_INSTANCE = "/v2/serviceResources";

    private static Logger logger = LoggerFactory.getLogger(ResourceRequestBuilder.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper mapperWithWrap;
    private static final ObjectMapper mapperWithOutWrap;

    static {
        mapperWithWrap = new ObjectMapper();
        mapperWithWrap.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapperWithOutWrap = new ObjectMapper();
        mapperWithOutWrap.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }

    static JsonUtils jsonUtil = new JsonUtils();

    private ResourceRequestBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> getResourceSequence(String serviceUuid) {

        List<String> resourceSequence = new ArrayList();
        try {
            Map<String, Object> serviceResponse = getServiceInstnace(serviceUuid);

            if (serviceResponse.containsKey("serviceResources")) {
                Map<String, Object> serviceResources = (Map<String, Object>) serviceResponse.get("serviceResources");

                if (serviceResources.containsKey("resourceOrder")) {
                    String resourceOrder = (String) serviceResources.get("resourceOrder");
                    if (resourceOrder != null) {
                        resourceSequence.addAll(Arrays.asList(resourceOrder.split(",")));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("not able to retrieve service order.");
        }
        return resourceSequence;
    }

    /*
     * build the resource Parameters detail. It's a json string for resource instantiant { "locationConstraints":[...]
     * "requestInputs":{K,V} } <br>
     *
     * @param execution Execution context
     *
     * @param resource The current Service Resource Object
     *
     * @param uuiServiceParameters the service parameters passed from the API
     *
     * @param currentVFData The object to hold the sequence of execution level for fetching data from UUI inputs
     *
     * @return the resource instantiate parameters
     *
     * @since ONAP Beijing Release
     */
    @SuppressWarnings("unchecked")
    public static String buildResourceRequestParameters(Execution execution, Resource resource,
            String uuiServiceParameters, Map<String, Object> currentVFData) {
        List<String> resourceList = jsonUtil.StringArrayToList(execution,
                (String) JsonUtils.getJsonValue(uuiServiceParameters, "resources"));
        // Get the right location str for resource. default is an empty array.
        String locationConstraints = "[]";
        if (resource.getResourceType() == ResourceType.VNF) {
            for (String eachResource : resourceList) {
                String resCusUuid = JsonUtils.getJsonValue(eachResource, "resourceCustomizationUuid");
                // in case of external api invocation customizatoin id is coming null
                if (resCusUuid == null || resCusUuid.contains("null") || resCusUuid.isEmpty()) {
                    logger.info("resource resolved using model uuid");
                    String uuid = (String) JsonUtils.getJsonValue(eachResource, "resourceUuid");
                    if ((null != uuid) && uuid.equals(resource.getModelInfo().getModelUuid())) {
                        logger.info("found resource uuid {}", uuid);
                        String resourceParameters = JsonUtils.getJsonValue(eachResource, "parameters");
                        locationConstraints = JsonUtils.getJsonValue(resourceParameters, "locationConstraints");
                    }
                } else if (resCusUuid.equals(resource.getModelInfo().getModelCustomizationUuid())) {
                    logger.info("resource resolved using customization-id");
                    String resourceParameters = JsonUtils.getJsonValue(eachResource, "parameters");
                    locationConstraints = JsonUtils.getJsonValue(resourceParameters, "locationConstraints");
                }
            }
        }

        Map<String, Object> uuiRequestInputs = null;
        if (JsonUtils.getJsonValue(uuiServiceParameters, "requestInputs") != null) {
            String uuiRequestInputStr = JsonUtils.getJsonValue(uuiServiceParameters, "requestInputs");
            logger.info("resource input from UUI:{} ", uuiRequestInputStr);
            if (uuiRequestInputStr == null || uuiRequestInputStr.isEmpty()) {
                uuiRequestInputStr = "{}";
            }

            uuiRequestInputs = getJsonObject(uuiRequestInputStr, Map.class);
        }

        if (uuiRequestInputs == null) {
            uuiRequestInputs = new HashMap();
        }

        Map<String, Object> resourceInputsAfterMerge =
                ResourceRequestBuilder.buildResouceRequest(resource, uuiRequestInputs, currentVFData);

        String resourceInputsStr = getJsonString(resourceInputsAfterMerge);
        String result = "{\n" + "\"locationConstraints\":" + locationConstraints + ",\n" + "\"requestInputs\":"
                + resourceInputsStr + "\n" + "}";
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildResouceRequest(Resource resource, Map<String, Object> uuiRequestInputs,
            Map<String, Object> currentVFData) {
        try {
            String resourceInputStr = null;
            // Resource Level is considered as first level by default
            ResourceLevel resourceLevel = ResourceLevel.FIRST;
            switch (resource.getResourceType()) {
                case VNF:
                    resourceInputStr = ((VnfResource) resource).getResourceInput();
                    resourceLevel = ResourceLevel.FIRST;
                    break;
                case GROUP:
                    resourceInputStr = ((GroupResource) resource).getVnfcs().get(0).getResourceInput();
                    resourceLevel = ResourceLevel.SECOND;
                    break;
            }

            if (StringUtils.isNotEmpty(resourceInputStr)) {
                return getResourceInput(resourceInputStr, uuiRequestInputs, resourceLevel, currentVFData);
            }

        } catch (Exception e) {
            logger.error("not able to retrieve service resource input ", e);
        }
        return new HashMap();
    }

    // this method combines resource input with service input
    private static Map<String, Object> getResourceInput(String resourceInputStr, Map<String, Object> uuiRequestInputs,
            ResourceLevel resourceLevel, Map<String, Object> currentVFData) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, Object> resourceInput = gson.fromJson(resourceInputStr, type);
            JsonParser parser = new JsonParser();

            Map<String, Object> uuiServiceInput = uuiRequestInputs;

            int firstLevelIndex = 0;
            int secondLevelIndex = 0;
            String firstLevelKey = null;
            String secondLevelKey = null;
            boolean levelKeyNameUpdated = false;
            int indexToPick = 0;

            if (null != currentVFData) {
                firstLevelIndex = getIntValue(currentVFData.get("currentFirstLevelIndex"), 0);
                secondLevelIndex = getIntValue(currentVFData.get("currentSecondLevelIndex"), 0);
                final String lastFirstLevelKey = firstLevelKey = (String) currentVFData.get("currentFirstLevelKey");
                final String lastSecondLevelKey = secondLevelKey = (String) currentVFData.get("currentSecondLevelKey");

                if (null != currentVFData.get("lastNodeTypeProcessed")) {
                    ResourceLevel lastResourceLevel =
                            ResourceLevel.valueOf(currentVFData.get("lastNodeTypeProcessed").toString());
                    switch (resourceLevel) {
                        case FIRST:
                            // if it is next request for same group then increment first level index
                            boolean isSameLevelRequest = resourceInput.values().stream().anyMatch(item -> {
                                JsonElement tree = parser.parse(((String) item).split("\\|")[0]);
                                return tree.isJsonArray() && tree.getAsJsonArray().get(0).getAsString()
                                        .equalsIgnoreCase(lastFirstLevelKey);
                            });
                            if (isSameLevelRequest) {
                                firstLevelIndex++;
                            } else {
                                firstLevelIndex = 0;
                            }
                            if (lastResourceLevel == ResourceLevel.SECOND) {
                                secondLevelKey = null;
                            }
                            indexToPick = firstLevelIndex;
                            break;
                        case SECOND:
                            // if it is next request for same group then increment second level index
                            switch (lastResourceLevel) {
                                case FIRST:
                                    secondLevelIndex = 0;
                                    break;
                                case SECOND:
                                    boolean isSameSecondLevelRequest =
                                            resourceInput.values().stream().anyMatch(item -> {
                                                JsonElement tree = parser.parse(((String) item).split("\\|")[0]);
                                                return tree.isJsonArray() && tree.getAsJsonArray().get(0).getAsString()
                                                        .equalsIgnoreCase(lastSecondLevelKey);
                                            });
                                    if (isSameSecondLevelRequest) {
                                        secondLevelIndex++;
                                    }
                                    break;
                            }
                            // get actual parent object to search for second level objects
                            if (null != lastFirstLevelKey) {
                                Object currentObject = uuiRequestInputs.get(lastFirstLevelKey);
                                if ((null != currentObject) && (currentObject instanceof List)) {
                                    List currentFirstLevelList = (List) currentObject;
                                    if (currentFirstLevelList.size() > firstLevelIndex) {
                                        uuiServiceInput =
                                                (Map<String, Object>) currentFirstLevelList.get(firstLevelIndex);
                                    }

                                }
                            }
                            indexToPick = secondLevelIndex;
                            break;

                    }
                }


            }

            // replace value if key is available in service input
            for (String key : resourceInput.keySet()) {
                String value = (String) resourceInput.get(key);

                if (value.contains("|")) {

                    // check which level

                    // node it type of getinput
                    String[] split = value.split("\\|");
                    String tmpKey = split[0];

                    JsonElement jsonTree = parser.parse(tmpKey);

                    // check if it is a list type
                    if (jsonTree.isJsonArray()) {
                        JsonArray jsonArray = jsonTree.getAsJsonArray();
                        boolean matchFound = false;
                        if (jsonArray.size() == 3) {
                            String keyName = jsonArray.get(0).getAsString();
                            String keyType = jsonArray.get(2).getAsString();
                            if (!levelKeyNameUpdated) {
                                switch (resourceLevel) {
                                    case FIRST:
                                        firstLevelKey = keyName;
                                        break;
                                    case SECOND:
                                        secondLevelKey = keyName;
                                        break;
                                }
                                levelKeyNameUpdated = true;
                            }

                            if ((null != uuiServiceInput) && (uuiServiceInput.containsKey(keyName))) {
                                Object vfcLevelObject = uuiServiceInput.get(keyName);
                                // it will be always list
                                if (vfcLevelObject instanceof List) {
                                    List vfcObject = (List) vfcLevelObject;
                                    if (vfcObject.size() > indexToPick) {
                                        Map<String, Object> vfMap = (Map<String, Object>) vfcObject.get(indexToPick);
                                        if (vfMap.containsKey(keyType)) {
                                            if (vfMap.get(keyType) instanceof String) {
                                                value = (String) vfMap.get(keyType);
                                            } else {
                                                value = getJsonString(vfMap.get(keyType));
                                            }
                                            matchFound = true;
                                        }
                                    }
                                }
                            }
                        }

                        if (!matchFound) {
                            if (split.length == 1) { // means value is empty e.g. "a":"key1|"
                                value = "";
                            } else {
                                value = split[1];
                            }
                        }

                    } else {

                        // if not a list type
                        if ((null != uuiServiceInput) && (uuiServiceInput.containsKey(tmpKey))) {
                            value = (String) uuiServiceInput.get(tmpKey);
                        } else {
                            if (split.length == 1) { // means value is empty e.g. "a":"key1|"
                                value = "";
                            } else {
                                value = split[1];
                            }
                        }
                    }

                }
                resourceInput.put(key, value);
            }
            // store current processed details into map
            if (null != currentVFData) {
                currentVFData.put("currentFirstLevelKey", firstLevelKey);
                currentVFData.put("currentFirstLevelIndex", firstLevelIndex);
                currentVFData.put("currentSecondLevelKey", secondLevelKey);
                currentVFData.put("currentSecondLevelIndex", secondLevelIndex);
                currentVFData.put("lastNodeTypeProcessed", resourceLevel.toString());
            }

            return resourceInput;

        } catch (Exception e) {
            logger.error("not able to parse and modify service resource input value against UUI ", e);
        }
        return new HashMap();
    }

    private static int getIntValue(Object inputObj, int defaultValue) {
        if (null != inputObj) {
            if (inputObj instanceof Integer) {
                return ((Integer) inputObj).intValue();
            }
            if (StringUtils.isNotEmpty(inputObj.toString())) {
                try {
                    int val = Integer.parseInt(inputObj.toString());
                    return val;
                } catch (NumberFormatException e) {
                    logger.warn("Unable to parse to int", e);
                }
            }
        }
        return defaultValue;
    }

    public static Map<String, Object> getServiceInstnace(String uuid) throws Exception {
        String catalogEndPoint = UrnPropertiesReader.getVariable("mso.catalog.db.endpoint");

        HttpClient client = new HttpClientFactory().newJsonClient(UriBuilder.fromUri(catalogEndPoint)
                .path(SERVICE_URL_SERVICE_INSTANCE).queryParam("serviceModelUuid", uuid).build().toURL(),
                ONAPComponents.CATALOG_DB);

        client.addAdditionalHeader("Accept", "application/json");
        client.addAdditionalHeader("Authorization", UrnPropertiesReader.getVariable("mso.db.auth"));

        Response apiResponse = client.get();

        String value = apiResponse.readEntity(String.class);

        HashMap<String, Object> map = mapper.readValue(value, HashMap.class);
        return map;
    }

    public static <T> T getJsonObject(String jsonstr, Class<T> type) {
        try {
            return mapperWithWrap.readValue(jsonstr, type);
        } catch (IOException e) {
            logger.error("fail to unMarshal json {}", e.getMessage());
        }
        return null;
    }

    public static String getJsonString(Object srcObj) {
        String jsonStr = null;
        try {
            jsonStr = mapperWithOutWrap.writeValueAsString(srcObj);
        } catch (JsonProcessingException e) {
            logger.error("SdcToscaParserException", e);
        }
        return jsonStr;
    }
}
