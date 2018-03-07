package org.openecomp.mso.bpmn.common.resource;

import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.toscaparser.api.parameters.Input;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class ResourceRequestBuilder {
    public static ResouceRequest buildResouceRequest(String serviceUuid,
                                              String resourceCustomizationUuid,
                                              Map<String, Object> serviceInputs) throws SdcToscaParserException {

        // 1. find the csar file using serviceuuid
        String csarpath = getCsar(serviceUuid);
        // 2. parse the csar file
        SdcToscaParserFactory iToscaParser = SdcToscaParserFactory.getInstance();
        ISdcCsarHelper sdcCsarHelper = iToscaParser.getSdcCsarHelper(csarpath);
        // 3. get the resource inputs from the file using resourcecustomizationuuid

        List<Input> serviceInputs1 = sdcCsarHelper.getServiceInputs();
        // 4. replace all get inputs from the serviceInput and return the result
        return null;
    }

    private static String getCsar(String uuid) {
        String path = ResourceRequestBuilder.class.getClassLoader()
                .getResource("service-HuaweiCcyTest01-csar.csar")
                .getPath();
        return path;
    }
}
