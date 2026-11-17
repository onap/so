/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

package org.onap.so.asdc.installer.heat;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery.EntityQueryBuilder;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery.TopologyTemplateQueryBuilder;
import org.onap.sdc.tosca.parser.enums.EntityTemplateType;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.functions.GetInput;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ToscaEntityHelper {

    static final String ALLOTTED_RESOURCE = "Allotted Resource";
    static final String MULTI_STAGE_DESIGN = "multi_stage_design";
    static final String SCALABLE = "scalable";
    static final String HEAT = "HEAT";
    static final String MANUAL_RECORD = "MANUAL_RECORD";
    static final String SDNC_MODEL_NAME = "sdnc_model_name";
    static final String SDNC_MODEL_VERSION = "sdnc_model_version";
    private static String CUSTOMIZATION_UUID = "customizationUUID";
    static final String SKIP_POST_INST_CONF = "skip_post_instantiation_configuration";
    static final String CONTROLLER_ACTOR = "controller_actor";
    static final String CDS_MODEL_NAME = "cds_model_name";
    static final String CDS_MODEL_VERSION = "cds_model_version";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ToscaEntityHelper.class);

    private ToscaEntityHelper() {}

    // this of temporary solution
    public static String getValue(Object value, List<Input> inputs) {
        String outInput;
        String defaultValue = null;
        if (value instanceof Map) {
            Collection values = ((LinkedHashMap) value).values();
            outInput = (values.size() > 0) ? values.toArray()[0].toString() : "";
        } else if (value instanceof GetInput) {
            String inputName = ((GetInput) value).getInputName();
            Optional<Input> inputOptional =
                    inputs.stream().filter(input -> input.getName().equals(inputName)).findFirst();
            if (inputOptional.isPresent()) {
                Input input = inputOptional.get();
                defaultValue = input.getDefault() != null ? input.getDefault().toString() : "";
            }
            // Gets a value between [ and ]
            String regex = "\\[.*?\\]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value.toString());
            String valueStr = matcher.find() ? matcher.group() : inputName;
            outInput = valueStr + "|" + defaultValue;
        } else {
            outInput = value != null ? value.toString() : "";
        }
        return outInput;
    }

    public static String getResourceInput(ToscaResourceStructure toscaResourceStructure,
            String resourceCustomizationUuid) throws ArtifactInstallerException {
        Map<String, String> resouceRequest = new HashMap<>();
        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();

        List<IEntityDetails> nodeTemplateEntityList =
                getEntityDetails(toscaResourceStructure, EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                        TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        List<Input> serInput = iSdcCsarHelper.getServiceInputs();
        Optional<IEntityDetails> nodeTemplateOpt = nodeTemplateEntityList.stream()
                .filter(e -> e.getMetadata().getValue(CUSTOMIZATION_UUID).equals(resourceCustomizationUuid))
                .findFirst();
        if (nodeTemplateOpt.isPresent()) {
            IEntityDetails entityDetails = nodeTemplateOpt.get();
            Map<String, Property> resourceProperties = entityDetails.getProperties();

            for (String key : resourceProperties.keySet()) {
                Property property = resourceProperties.get(key);

                String value = getValue(property.getValue(), serInput);
                resouceRequest.put(key, value);
            }
        }

        try {
            String jsonStr = mapper.writeValueAsString(resouceRequest);

            jsonStr = jsonStr.replace("\"", "\\\"");
            logger.debug("resource request for resource customization id {}: {}", resourceCustomizationUuid, jsonStr);
            return jsonStr;
        } catch (JsonProcessingException e) {
            logger.error("resource input could not be deserialized for resource customization id ({})",
                    resourceCustomizationUuid);
            throw new ArtifactInstallerException("resource input could not be parsed", e);
        }
    }

    public static String testNull(Object object) {

        if (object == null) {
            return null;
        } else if ("NULL".equals(object)) {
            return null;
        } else if (object instanceof Integer) {
            return object.toString();
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return "Type not recognized";
        }
    }

    public static List<IEntityDetails> getEntityDetails(ToscaResourceStructure toscaResourceStruct,
            EntityQueryBuilder entityType, TopologyTemplateQueryBuilder topologyTemplateBuilder, boolean nestedSearch) {

        EntityQuery entityQuery = entityType.build();
        TopologyTemplateQuery topologyTemplateQuery = topologyTemplateBuilder.build();
        List<IEntityDetails> entityDetails =
                toscaResourceStruct.getSdcCsarHelper().getEntity(entityQuery, topologyTemplateQuery, nestedSearch);

        return entityDetails;

    }

    public static String getLeafPropertyValue(IEntityDetails entityDetails, String propName) {

        Property leafProperty = entityDetails.getProperties().get(propName);

        if (leafProperty != null && leafProperty.getValue() != null) {
            return leafProperty.getValue().toString();
        }

        return null;
    }

    public static Integer parseIntegerOrNull(String value, String propName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Ignoring non-integer value '{}' of property {}", value, propName);
            return null;
        }
    }

    public static String getCapabilityLeafPropertyValue(CapabilityAssignment capAssign, String propName) {

        Property leafProperty = capAssign.getProperties().get(propName);

        if (leafProperty != null && leafProperty.getValue() != null) {
            return leafProperty.getValue().toString();
        }

        return null;
    }

    public static String getPropertyInput(String propertyName) {

        String inputName = new String();

        if (propertyName != null) {
            int getInputIndex = propertyName.indexOf("{get_input=");
            int getClosingIndex = propertyName.indexOf("}");
            if (getInputIndex > -1) {
                inputName = propertyName.substring(getInputIndex + 11, getClosingIndex);
            }
        }

        return inputName;
    }

    public static Timestamp getCurrentTimeStamp() {

        return new Timestamp(new Date().getTime());
    }
}
