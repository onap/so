package org.onap.so.bpmn.infrastructure.sdnc.exceptions;

public class SDNCErrorResponseException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7807799223298140702L;

    public SDNCErrorResponseException(String message) {
        super(message);
    }
}
