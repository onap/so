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
package org.openecomp.mso.bpmn.common.resource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.Property;
import org.openecomp.sdc.toscaparser.api.functions.GetInput;
import org.openecomp.sdc.toscaparser.api.parameters.Input;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceRequestBuilder {

    public static String CUSTOMIZATION_UUID = "customizationUUID";
    public static String SERVICE_URL_TOSCA_CSAR = "http://localhost:8080/ecomp/mso/catalog/v3/serviceToscaCsar?serviceModelUuid=";

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    public static Map<String, Object> buildResouceRequest(String serviceUuid,
                                              String resourceCustomizationUuid,
                                              Map<String, Object> serviceInputs) throws SdcToscaParserException {

        Map<String, Object> resouceRequest = new HashMap<>();

        String csarpath = null;
        try {
            csarpath = getCsarFromUuid(serviceUuid);
        } catch (Exception e) {
            LOGGER.debug("csar file is not available for service uuid:" + serviceUuid, e);
            return resouceRequest;
        }

        SdcToscaParserFactory toscaParser = SdcToscaParserFactory.getInstance();
        ISdcCsarHelper iSdcCsarHelper = toscaParser.getSdcCsarHelper(csarpath);

        List<Input> serInput = iSdcCsarHelper.getServiceInputs();
        Optional<NodeTemplate> nodeTemplateOpt = iSdcCsarHelper.getServiceNodeTemplates().stream()
                .filter(e -> e.getMetaData().getValue(CUSTOMIZATION_UUID).equals(resourceCustomizationUuid))
                .findFirst();

        if (nodeTemplateOpt.isPresent()) {
            NodeTemplate nodeTemplate = nodeTemplateOpt.get();
            LinkedHashMap<String, Property> resourceProperties = nodeTemplate.getProperties();

            for (String key: resourceProperties.keySet()) {
                Property property = resourceProperties.get(key);

                Object value = getValue(property.getValue(), serviceInputs, serInput);
                resouceRequest.put(key, value);
            }
        }
        return resouceRequest;
    }

    private static Object getValue(Object value, Map<String, Object> serviceInputs,
                                   List<Input> servInputs) {
        if (value instanceof Map) {
            Map<String, Object> valueMap = new HashMap<>();

            Map<String, Object> propertyMap = (Map<String, Object>) value;

            for (String key: propertyMap.keySet()) {
                valueMap.put(key, getValue(propertyMap.get(key), serviceInputs, servInputs));
            }
            return valueMap; // return if the value is nested hashmap
        } else if (value instanceof GetInput) {
            String inputName = ((GetInput) value).getInputName();

            if (serviceInputs.get(inputName) != null) {
                value = serviceInputs.get(inputName);
            } else {
                for (Input input: servInputs) {
                    if (input.getName().equals(inputName)) {
                        return input.getDefault();  // return default value
                    }
                }
            }
        }
        return value; // return property value
    }

    private static String getCsarFromUuid(String uuid) throws Exception {

        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(SERVICE_URL_TOSCA_CSAR + uuid);
        Response response = target.request().get();
        String value = response.readEntity(String.class);

        HashMap<String,String> map = new Gson().fromJson(value, new TypeToken<HashMap<String, String>>(){}.getType());

        File csarFile = new File(System.getProperty("mso.config.path") + "ASDC/" + map.get("name"));

        if (!csarFile.exists()) {
            throw new Exception("csar file does not exist.");
        }

        return csarFile.getAbsolutePath();
    }
}
