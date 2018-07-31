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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.runtime.Execution;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.onap.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.functions.GetInput;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.HttpClient;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.TargetEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ResourceRequestBuilder {

    public static String CUSTOMIZATION_UUID = "customizationUUID";

    public static String SERVICE_URL_TOSCA_CSAR = "/v3/serviceToscaCsar?serviceModelUuid=";

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ResourceRequestBuilder.class);

    static JsonUtils jsonUtil = new JsonUtils();

        /**
     * build the resource Parameters detail.
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

        try {
            Map<String, Object> resourceInputsFromServiceDeclaredLevel = buildResouceRequest(serviceUuid, resourceCustomizationUuid, serviceInput);
            resourceInputsFromUuiMap.putAll(resourceInputsFromServiceDeclaredLevel);
        } catch(SdcToscaParserException e) {
        	LOGGER.error("SdcToscaParserException", e);
        }
        String resourceInputsStr = getJsonString(resourceInputsFromUuiMap);
        String result = "{\n"
                + "\"locationConstraints\":" + locationConstraints +",\n"
                + "\"requestInputs\":" + resourceInputsStr +"\n"
                +"}";
        return result;
    }

    public static Map<String, Object> buildResouceRequest(String serviceUuid, String resourceCustomizationUuid, Map<String, Object> serviceInputs)
            throws SdcToscaParserException {

        Map<String, Object> resouceRequest = new HashMap<>();

        String csarpath = null;
        try {
            csarpath = getCsarFromUuid(serviceUuid);
        } catch(Exception e) {
            LOGGER.debug("csar file is not available for service uuid:" + serviceUuid, e);
            return resouceRequest;
        }

        SdcToscaParserFactory toscaParser = SdcToscaParserFactory.getInstance();
        ISdcCsarHelper iSdcCsarHelper = toscaParser.getSdcCsarHelper(csarpath, false);

        List<Input> serInput = iSdcCsarHelper.getServiceInputs();
        Optional<NodeTemplate> nodeTemplateOpt = iSdcCsarHelper.getServiceNodeTemplates().stream()
                .filter(e -> e.getMetaData().getValue(CUSTOMIZATION_UUID).equals(resourceCustomizationUuid)).findFirst();

        if(nodeTemplateOpt.isPresent()) {
            NodeTemplate nodeTemplate = nodeTemplateOpt.get();
            LinkedHashMap<String, Property> resourceProperties = nodeTemplate.getProperties();

            for(String key : resourceProperties.keySet()) {
                Property property = resourceProperties.get(key);

                Object value = getValue(property.getValue(), serviceInputs, serInput);
                resouceRequest.put(key, value);
            }
        }
        return resouceRequest;
    }

    private static Object getValue(Object value, Map<String, Object> serviceInputs, List<Input> servInputs) {
        if(value instanceof Map) {
            Map<String, Object> valueMap = new HashMap<>();

            Map<String, Object> propertyMap = (Map<String, Object>)value;

            for(String key : propertyMap.keySet()) {
                valueMap.put(key, getValue(propertyMap.get(key), serviceInputs, servInputs));
            }
            return valueMap; // return if the value is nested hashmap
        } else if(value instanceof GetInput) {
            String inputName = ((GetInput)value).getInputName();

            if(serviceInputs.get(inputName) != null) {
                value = serviceInputs.get(inputName);
            } else {
                for(Input input : servInputs) {
                    if(input.getName().equals(inputName)) {
                        return input.getDefault(); // return default value
                    }
                }
            }
        }
        return value; // return property value
    }

    private static String getCsarFromUuid(String uuid) throws Exception {
		String catalogEndPoint = UrnPropertiesReader.getVariable("mso.catalog.db.endpoint");
    	HttpClient client = new HttpClient(UriBuilder.fromUri(catalogEndPoint + SERVICE_URL_TOSCA_CSAR + uuid).build().toURL(), "application/json", TargetEntity.CATALOG_DB);
    	
        Response response = client.get();
        String value = response.readEntity(String.class);

        HashMap<String, String> map = new Gson().fromJson(value, new TypeToken<HashMap<String, String>>() {}.getType());

        File csarFile = new File(System.getProperty("mso.config.path") + "ASDC/" + map.get("name"));

        if(!csarFile.exists()) {
            throw new Exception("csar file does not exist.");
        }

        return csarFile.getAbsolutePath();
    }
    
    public static <T> T getJsonObject(String jsonstr, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        try {
            return mapper.readValue(jsonstr, type);
        } catch(IOException e) {
            LOGGER.error(MessageEnum.RA_NS_EXC, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "fail to unMarshal json", e);
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
