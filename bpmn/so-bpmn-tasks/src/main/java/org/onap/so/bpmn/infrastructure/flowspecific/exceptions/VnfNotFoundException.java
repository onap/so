package org.onap.so.bpmn.infrastructure.flowspecific.exceptions;

public class VnfNotFoundException extends Exception {
    public VnfNotFoundException(String modelCustomizationUuidOfSearchedVnf) {
        super("Can not find vnf for model customization uuid: " + modelCustomizationUuidOfSearchedVnf);
    };
}
