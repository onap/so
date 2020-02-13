package org.onap.so.adapters.cloudregion;

public class CloudException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -2631715095942372451L;

    public CloudException(String error, Exception e) {
        super(error, e);
    }

}
