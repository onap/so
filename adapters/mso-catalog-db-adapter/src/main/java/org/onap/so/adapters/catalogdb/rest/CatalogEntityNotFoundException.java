package org.onap.so.adapters.catalogdb.rest;

public class CatalogEntityNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -300157844846680791L;

    public CatalogEntityNotFoundException(String errorMessage) {
        super(errorMessage);
    }

}
