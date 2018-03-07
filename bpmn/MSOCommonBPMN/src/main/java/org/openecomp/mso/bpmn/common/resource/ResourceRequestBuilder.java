package org.openecomp.mso.bpmn.common.resource;

import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.toscaparser.api.NodeTemplate;
import org.openecomp.sdc.toscaparser.api.Property;
import org.openecomp.sdc.toscaparser.api.functions.GetInput;
import org.openecomp.sdc.toscaparser.api.parameters.Input;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceRequestBuilder {
    public static ResouceRequest buildResouceRequest(String serviceUuid,
                                              String resourceCustomizationUuid,
                                              Map<String, Object> serviceInputs) throws SdcToscaParserException {

        ResouceRequest resouceRequest = new ResouceRequest();
        // 1. find the csar file using serviceuuid
        String csarpath = getCsar(serviceUuid);
        // 2. parse the csar file
        SdcToscaParserFactory toscaParser = SdcToscaParserFactory.getInstance();
        ISdcCsarHelper iSdcCsarHelper = toscaParser.getSdcCsarHelper(csarpath);
        // 3. get the resource inputs from the file using resourcecustomizationuuid


        //sdcCsarHelper.getServiceNodeTemplates().stream().map(n -> n.getProperties()).collect(Collectors.toList())
        List<Input> serInput = iSdcCsarHelper.getServiceInputs();
        Optional<NodeTemplate> nodeTemplateOpt = iSdcCsarHelper.getServiceNodeTemplates().stream()
                //.filter(e -> e.getMetaData().getValue("customizationUUID") == resourceCustomizationUuid)
                .findFirst();

        if (nodeTemplateOpt.isPresent()) {
            NodeTemplate nodeTemplate = nodeTemplateOpt.get();
            LinkedHashMap<String, Property> resourceProperties = nodeTemplate.getProperties();

            // create ResourceRequest
            // 1. fill design time
            // 2. run time
            // 3. default value
            for (String key: resourceProperties.keySet()) {
                Property property = resourceProperties.get(key);
                Object value = property.getValue();
                if (value instanceof GetInput) {
                    String inputName = ((GetInput) value).getInputName();

                    value = serviceInputs.get(inputName) != null ? serviceInputs.get(inputName)
                            : serInput.stream().filter(e -> e.getName().equals(inputName))
                            .map(e -> e.getDefault())
                            .findFirst().get();

                }
                resouceRequest.addResourceInput(key, value);
            }

        }
        // 4. replace all get inputs from the serviceInput and return the result
        return resouceRequest;
    }

    private static String getCsar(String uuid) {
        String path = ResourceRequestBuilder.class.getClassLoader()
                .getResource("service-HuaweiCcyTest01-csar.csar")
                .getPath();
        return path;
    }
}
