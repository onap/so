package org.onap.so.bpmn.servicedecomposition.tasks.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    static final long serialVersionUID = -2741357347054072719L;

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
