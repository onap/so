package org.onap.so.client.graphinventory;

public interface GraphInventoryObjectBase
        extends GraphInventoryObjectName, GraphInventoryObjectUriTemplate, GraphInventoryObjectUriPartial {

    public default boolean passThrough() {
        return false;
    }
}
