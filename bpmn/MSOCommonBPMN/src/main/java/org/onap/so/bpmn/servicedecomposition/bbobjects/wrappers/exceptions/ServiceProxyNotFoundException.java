package org.onap.so.bpmn.servicedecomposition.bbobjects.wrappers.exceptions;

public class ServiceProxyNotFoundException extends Exception {

    private static final long serialVersionUID = 717577158109655720L;

    public ServiceProxyNotFoundException() {
        super();
    }

    public ServiceProxyNotFoundException(String message) {
        super(message);
    }
}
