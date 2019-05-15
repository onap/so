package org.onap.so.client.exception;


public class ExpectedDataException extends Exception {

    public ExpectedDataException() {}

    public ExpectedDataException(String message, String system) {
        super("Expected data not found in " + system + ". " + message);
    }

    public ExpectedDataException(String message) {
        super("Expected data not found. " + message);
    }


}
