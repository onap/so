package org.onap.so.client.adapter.vnf.mapper.exceptions;

public class MissingValueTagException extends Exception {

    private static final long serialVersionUID = -1598147488593823724L;

    public MissingValueTagException() {
        super();
    }

    public MissingValueTagException(String message) {
        super(message);
    }

}
