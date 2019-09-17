package org.onap.so.bpmn.infrastructure.flowspecific.exceptions;

public class UnassignNetworkException extends Exception {

    private static final long serialVersionUID = 2864418350216433736L;

    public UnassignNetworkException() {
        super();
    }

    public UnassignNetworkException(String message) {
        super(message);
    }
}
