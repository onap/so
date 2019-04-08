package org.onap.so.adapters.inventory.create;

public class InventoryException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 912652713891488731L;

    public InventoryException(String errorMessage) {
        super(errorMessage);
    }

}
