package org.onap.aaiclient.client.graphinventory.entities.uri;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectBase;

public interface GraphInventoryPluralResourceUri<T extends GraphInventoryResourceUri<?, ?>, OT extends GraphInventoryObjectBase>
        extends GraphInventoryResourceUri<T, OT> {

}
