package org.onap.aaiclient.client.aai.entities.uri;

import javax.ws.rs.core.UriBuilder;
import org.onap.aaiclient.client.aai.AAIObjectPlurals;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectBase;

public class NodesPluralUri extends AAISimplePluralUri implements NodesUri {

    private static final long serialVersionUID = -6743170679667245998L;

    protected NodesPluralUri(AAIObjectPlurals type) {
        super(type);
    }

    @Override
    public String getTemplate(GraphInventoryObjectBase type) {
        return UriBuilder.fromUri("/nodes").path(type.partialUri()).toTemplate();
    }
}
