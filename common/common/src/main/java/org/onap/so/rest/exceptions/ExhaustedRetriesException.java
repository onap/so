package org.onap.so.rest.exceptions;

public class ExhaustedRetriesException extends RuntimeException {

    private static final long serialVersionUID = -8303091412739222943L;

    public ExhaustedRetriesException(String s) {
        super(s);
    }

    public ExhaustedRetriesException(Throwable t) {
        super(t);
    }

    public ExhaustedRetriesException(String s, Throwable t) {
        super(s, t);
    }

}
