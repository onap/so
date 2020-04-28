package org.onap.so.client.aai.entities.uri;

import javax.ws.rs.core.UriBuilder;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.graphinventory.GraphInventoryObjectBase;

public class NodesSingleUri extends AAISimpleUri implements NodesUri {

    private static final long serialVersionUID = 2721165364903444248L;

    protected NodesSingleUri(AAIObjectType type, Object... values) {
        super(type, values);
    }


    @Override
    public String getTemplate(GraphInventoryObjectBase type) {
        return UriBuilder.fromUri("/nodes").path(type.partialUri()).toTemplate();
    }

}
