package org.onap.aaiclient.client.graphinventory.entities.uri;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectPlurals;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectType;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPluralFragment;
import org.onap.aaiclient.client.graphinventory.GraphInventorySingleFragment;

public interface GraphInventorySingleResourceUri<T extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, P extends GraphInventoryPluralResourceUri<?, ?>, SingleObject extends GraphInventoryObjectType, PluralObject extends GraphInventoryObjectPlurals, SingleFragment extends GraphInventorySingleFragment, PluralFragment extends GraphInventoryPluralFragment>
        extends GraphInventoryResourceUri<T, SingleObject> {

    public T resourceVersion(String version);

    public T relationshipAPI();

    public P relatedTo(PluralObject plural);

    public T relatedTo(SingleObject type, String... values);

    public P relatedTo(PluralFragment fragment);

    public T relatedTo(SingleFragment fragment);
}
