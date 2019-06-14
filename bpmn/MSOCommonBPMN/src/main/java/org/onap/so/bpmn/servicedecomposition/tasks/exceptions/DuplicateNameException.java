package org.onap.so.bpmn.servicedecomposition.tasks.exceptions;

public class DuplicateNameException extends Exception {

    private static final long serialVersionUID = -2850043981787600326L;

    public DuplicateNameException() {
        super();
    }

    public DuplicateNameException(String message) {
        super(message);
    }

    public DuplicateNameException(String objectType, String name) {
        super(objectType + " with name " + name + " already exists. The name must be unique.");
    }
}
