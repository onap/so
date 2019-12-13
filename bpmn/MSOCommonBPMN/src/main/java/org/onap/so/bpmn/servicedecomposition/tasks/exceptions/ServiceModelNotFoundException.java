package org.onap.so.bpmn.servicedecomposition.tasks.exceptions;

public class ServiceModelNotFoundException extends Exception {

    private static final long serialVersionUID = -5551887892983898061L;

    public ServiceModelNotFoundException() {
        super();
    }

    public ServiceModelNotFoundException(String message) {
        super(message);
    }

}
