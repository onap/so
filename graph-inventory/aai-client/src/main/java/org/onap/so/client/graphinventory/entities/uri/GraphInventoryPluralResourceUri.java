package org.onap.so.client.graphinventory.entities.uri;

import org.onap.so.client.graphinventory.GraphInventoryObjectBase;

public interface GraphInventoryPluralResourceUri<T extends GraphInventoryResourceUri<?, ?>, OT extends GraphInventoryObjectBase>
        extends GraphInventoryResourceUri<T, OT> {

}
