package org.onap.so.client.exception;


public class UnexpectedDataException extends Exception {

    public UnexpectedDataException() {}

    public UnexpectedDataException(String message, String system) {
        super("Unexpected data found in " + system + ". " + message);
    }

    public UnexpectedDataException(String message) {
        super("Unexpected data found. " + message);
    }


}
