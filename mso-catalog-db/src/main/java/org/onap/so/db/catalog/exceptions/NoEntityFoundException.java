package org.onap.so.db.catalog.exceptions;


public class NoEntityFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 545820289784366486L;

    public NoEntityFoundException(String errorMessage) {
       super(errorMessage);
    }



}