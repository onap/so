package org.onap.aaiclient.client.graphinventory.entities.uri;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;

public interface GraphInventorySingleResourceUri<T extends GraphInventorySingleResourceUri<?, ?, ?, ?>, P extends GraphInventoryPluralResourceUri<?, ?>, SingleObject extends GraphInventoryObjectType, PluralObject extends GraphInventoryObjectPlurals>
        extends GraphInventoryResourceUri<T, SingleObject> {

    public T resourceVersion(String version);

    public T relationshipAPI();

    public P relatedTo(PluralObject plural);

    public T relatedTo(SingleObject type, String... values);
}
