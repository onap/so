package org.openecomp.mso.bpmn.common.resource;

import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.Property;
import org.openecomp.sdc.toscaparser.api.functions.GetInput;
import org.openecomp.sdc.toscaparser.api.parameters.Input;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceRequestBuilder {

    public static String CUSTOMIZATION_UUID = "customizationUUID";

    public static Map<String, Object> buildResouceRequest(String serviceUuid,
                                              String resourceCustomizationUuid,
                                              Map<String, Object> serviceInputs) throws SdcToscaParserException {

        Map<String, Object> resouceRequest = new HashMap<>();

        String csarpath = getCsarFromUuid(serviceUuid);

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

    private static String getCsarFromUuid(String uuid) {
        String path = ResourceRequestBuilder.class.getClassLoader()
                .getResource("service-HuaweiCcyTest01-csar.csar")
                .getPath();
        return path;
    }
}
